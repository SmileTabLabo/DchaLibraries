package com.android.launcher3.model;

import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.PackageInstaller;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Process;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.MutableInt;
import com.android.launcher3.AllAppsList;
import com.android.launcher3.AppInfo;
import com.android.launcher3.FolderInfo;
import com.android.launcher3.IconCache;
import com.android.launcher3.InstallShortcutReceiver;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherAppWidgetInfo;
import com.android.launcher3.LauncherModel;
import com.android.launcher3.LauncherSettings;
import com.android.launcher3.ShortcutInfo;
import com.android.launcher3.Utilities;
import com.android.launcher3.compat.AppWidgetManagerCompat;
import com.android.launcher3.compat.LauncherAppsCompat;
import com.android.launcher3.compat.PackageInstallerCompat;
import com.android.launcher3.compat.UserManagerCompat;
import com.android.launcher3.folder.Folder;
import com.android.launcher3.folder.FolderIconPreviewVerifier;
import com.android.launcher3.graphics.LauncherIcons;
import com.android.launcher3.logging.FileLog;
import com.android.launcher3.provider.ImportDataTask;
import com.android.launcher3.shortcuts.DeepShortcutManager;
import com.android.launcher3.shortcuts.ShortcutInfoCompat;
import com.android.launcher3.shortcuts.ShortcutKey;
import com.android.launcher3.util.ComponentKey;
import com.android.launcher3.util.LooperIdleLock;
import com.android.launcher3.util.MultiHashMap;
import com.android.launcher3.util.PackageManagerHelper;
import com.android.launcher3.util.Provider;
import com.android.launcher3.util.TraceHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CancellationException;
/* loaded from: classes.dex */
public class LoaderTask implements Runnable {
    private static final String TAG = "LoaderTask";
    private final LauncherAppState mApp;
    private final AppWidgetManagerCompat mAppWidgetManager;
    private final AllAppsList mBgAllAppsList;
    private final BgDataModel mBgDataModel;
    private FirstScreenBroadcast mFirstScreenBroadcast;
    private final IconCache mIconCache;
    private final LauncherAppsCompat mLauncherApps;
    private final PackageInstallerCompat mPackageInstaller;
    private final LoaderResults mResults;
    private final DeepShortcutManager mShortcutManager;
    private boolean mStopped;
    private final UserManagerCompat mUserManager;

    public LoaderTask(LauncherAppState launcherAppState, AllAppsList allAppsList, BgDataModel bgDataModel, LoaderResults loaderResults) {
        this.mApp = launcherAppState;
        this.mBgAllAppsList = allAppsList;
        this.mBgDataModel = bgDataModel;
        this.mResults = loaderResults;
        this.mLauncherApps = LauncherAppsCompat.getInstance(this.mApp.getContext());
        this.mUserManager = UserManagerCompat.getInstance(this.mApp.getContext());
        this.mShortcutManager = DeepShortcutManager.getInstance(this.mApp.getContext());
        this.mPackageInstaller = PackageInstallerCompat.getInstance(this.mApp.getContext());
        this.mAppWidgetManager = AppWidgetManagerCompat.getInstance(this.mApp.getContext());
        this.mIconCache = this.mApp.getIconCache();
    }

    protected synchronized void waitForIdle() {
        LooperIdleLock newIdleLock = this.mResults.newIdleLock(this);
        while (!this.mStopped && newIdleLock.awaitLocked(1000L)) {
        }
    }

    private synchronized void verifyNotStopped() throws CancellationException {
        if (this.mStopped) {
            throw new CancellationException("Loader stopped");
        }
    }

    private void sendFirstScreenActiveInstallsBroadcast() {
        long longValue;
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        synchronized (this.mBgDataModel) {
            arrayList2.addAll(this.mBgDataModel.workspaceItems);
            arrayList2.addAll(this.mBgDataModel.appWidgets);
        }
        if (this.mBgDataModel.workspaceScreens.isEmpty()) {
            longValue = -1;
        } else {
            longValue = this.mBgDataModel.workspaceScreens.get(0).longValue();
        }
        LoaderResults.filterCurrentWorkspaceItems(longValue, arrayList2, arrayList, new ArrayList());
        this.mFirstScreenBroadcast.sendBroadcasts(this.mApp.getContext(), arrayList);
    }

    @Override // java.lang.Runnable
    public void run() {
        synchronized (this) {
            if (this.mStopped) {
                return;
            }
            TraceHelper.beginSection(TAG);
            try {
                LauncherModel.LoaderTransaction beginLoader = this.mApp.getModel().beginLoader(this);
                TraceHelper.partitionSection(TAG, "step 1.1: loading workspace");
                loadWorkspace();
                verifyNotStopped();
                TraceHelper.partitionSection(TAG, "step 1.2: bind workspace workspace");
                this.mResults.bindWorkspace();
                TraceHelper.partitionSection(TAG, "step 1.3: send first screen broadcast");
                sendFirstScreenActiveInstallsBroadcast();
                TraceHelper.partitionSection(TAG, "step 1 completed, wait for idle");
                waitForIdle();
                verifyNotStopped();
                TraceHelper.partitionSection(TAG, "step 2.1: loading all apps");
                loadAllApps();
                TraceHelper.partitionSection(TAG, "step 2.2: Binding all apps");
                verifyNotStopped();
                this.mResults.bindAllApps();
                verifyNotStopped();
                TraceHelper.partitionSection(TAG, "step 2.3: Update icon cache");
                updateIconCache();
                TraceHelper.partitionSection(TAG, "step 2 completed, wait for idle");
                waitForIdle();
                verifyNotStopped();
                TraceHelper.partitionSection(TAG, "step 3.1: loading deep shortcuts");
                loadDeepShortcuts();
                verifyNotStopped();
                TraceHelper.partitionSection(TAG, "step 3.2: bind deep shortcuts");
                this.mResults.bindDeepShortcuts();
                TraceHelper.partitionSection(TAG, "step 3 completed, wait for idle");
                waitForIdle();
                verifyNotStopped();
                TraceHelper.partitionSection(TAG, "step 4.1: loading widgets");
                this.mBgDataModel.widgetsModel.update(this.mApp, null);
                verifyNotStopped();
                TraceHelper.partitionSection(TAG, "step 4.2: Binding widgets");
                this.mResults.bindWidgets();
                beginLoader.commit();
                if (beginLoader != null) {
                    beginLoader.close();
                }
            } catch (CancellationException e) {
                TraceHelper.partitionSection(TAG, "Cancelled");
            }
            TraceHelper.endSection(TAG);
        }
    }

    public synchronized void stopLocked() {
        this.mStopped = true;
        notify();
    }

    /* JADX WARN: Can't wrap try/catch for region: R(27:237|(1:481)(1:241)|(3:244|245|(1:247)(5:248|249|(5:251|252|253|254|(27:256|257|258|259|260|261|(3:264|265|(2:267|(2:270|(1:272)(2:273|274))(1:269))(22:277|(1:279)(1:(3:454|455|456)(4:457|458|459|276))|280|281|282|283|(1:285)|(2:444|445)|287|(7:289|290|291|292|293|294|(12:296|297|298|299|(2:427|428)(2:301|(2:424|425)(9:303|304|(9:306|307|308|309|310|311|312|313|(4:315|316|317|(2:319|320)(18:321|322|323|324|325|326|327|328|329|330|331|332|333|334|336|337|(1:339)|340))(2:397|398))(6:408|409|410|(1:414)|415|(1:423))|(13:349|350|351|352|353|354|(1:371)(4:358|359|360|361)|362|(2:366|(1:368)(1:369))|370|182|85|86)(3:342|343|347)|345|346|95|85|86))|426|(0)(0)|345|346|95|85|86))(1:443)|435|297|298|299|(0)(0)|426|(0)(0)|345|346|95|85|86))|463|281|282|283|(0)|(0)|287|(0)(0)|435|297|298|299|(0)(0)|426|(0)(0)|345|346|95|85|86)(2:470|471))(2:475|476)|275|276))|480|260|261|(3:264|265|(0)(0))|463|281|282|283|(0)|(0)|287|(0)(0)|435|297|298|299|(0)(0)|426|(0)(0)|345|346|95|85|86) */
    /* JADX WARN: Code restructure failed: missing block: B:397:0x08de, code lost:
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:398:0x08df, code lost:
        r40 = r6;
        r41 = r8;
        r43 = r15;
        r6 = r22;
        r5 = r33;
        r42 = r34;
     */
    /* JADX WARN: Code restructure failed: missing block: B:399:0x08ec, code lost:
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:400:0x08ed, code lost:
        r39 = r5;
        r40 = r6;
        r41 = r8;
        r6 = r22;
        r10 = r23;
     */
    /* JADX WARN: Code restructure failed: missing block: B:401:0x08f8, code lost:
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:402:0x08f9, code lost:
        r39 = r5;
        r40 = r6;
        r41 = r8;
        r6 = r22;
        r10 = r23;
        r4 = r24;
     */
    /* JADX WARN: Code restructure failed: missing block: B:403:0x0905, code lost:
        r5 = r33;
        r42 = r34;
        r43 = r35;
     */
    /* JADX WARN: Removed duplicated region for block: B:244:0x0643 A[Catch: Exception -> 0x068f, all -> 0x0aed, TRY_ENTER, TryCatch #19 {all -> 0x0af7, blocks: (B:19:0x008e, B:419:0x0988, B:421:0x0990, B:422:0x0995, B:424:0x0997, B:426:0x099d, B:427:0x09b1, B:429:0x09b7, B:430:0x09df, B:431:0x09e4, B:432:0x09f1, B:434:0x09f7, B:436:0x0a09, B:438:0x0a0d, B:440:0x0a13, B:442:0x0a19, B:443:0x0a2c, B:445:0x0a32, B:446:0x0a4a, B:448:0x0a50, B:450:0x0a5a, B:452:0x0a5e, B:454:0x0a66, B:462:0x0a78, B:464:0x0a7d, B:466:0x0a83, B:467:0x0a9e, B:468:0x0aaf, B:470:0x0ab5, B:472:0x0ac5, B:474:0x0acf, B:476:0x0ad7, B:478:0x0add, B:479:0x0aeb, B:486:0x0af5, B:20:0x0098, B:21:0x00d8, B:23:0x00e0, B:25:0x010c, B:27:0x011f, B:28:0x0123, B:30:0x0129, B:34:0x0140, B:35:0x0153, B:36:0x0163, B:38:0x0167, B:40:0x016d, B:42:0x0171, B:416:0x097b, B:47:0x01a3, B:52:0x01c9, B:56:0x01d2, B:58:0x01dd, B:60:0x01e4, B:62:0x01eb, B:66:0x01f7, B:73:0x021d, B:75:0x0221, B:77:0x0227, B:82:0x023e, B:84:0x025c, B:87:0x026d, B:88:0x026f, B:112:0x030e, B:114:0x0316, B:115:0x031c, B:117:0x0335, B:120:0x034f, B:122:0x035b, B:124:0x0361, B:125:0x037a, B:127:0x037e, B:128:0x0399, B:92:0x0287, B:94:0x0297, B:96:0x02c5, B:100:0x02db, B:111:0x030c, B:110:0x0308, B:104:0x02e6, B:106:0x02ee, B:99:0x02d0, B:156:0x0454, B:158:0x0468, B:160:0x046e, B:169:0x04e0, B:171:0x04e6, B:175:0x0510, B:177:0x0514, B:181:0x0526, B:183:0x052c, B:188:0x0550, B:190:0x055c, B:192:0x0561, B:194:0x057b, B:196:0x057f, B:198:0x0585, B:200:0x058b, B:202:0x0590, B:204:0x0596, B:206:0x059c, B:213:0x05af, B:215:0x05b9, B:218:0x05bf, B:220:0x05c5, B:222:0x05c9, B:224:0x05cf, B:226:0x05da, B:239:0x0637, B:242:0x063f, B:244:0x0643, B:247:0x0663, B:249:0x0669, B:250:0x0679, B:254:0x0691, B:256:0x0699, B:268:0x06f1, B:273:0x06fb, B:277:0x0701, B:280:0x0709, B:282:0x070f, B:293:0x073c, B:295:0x0740, B:369:0x087c, B:371:0x0888, B:372:0x088c, B:374:0x0893, B:376:0x0899, B:378:0x089d, B:380:0x08a1, B:382:0x08a5, B:384:0x08ab, B:386:0x08b3, B:387:0x08ba, B:388:0x08c4, B:393:0x08d2, B:394:0x08da, B:299:0x0754, B:301:0x0758, B:303:0x075d, B:306:0x0762, B:308:0x076a, B:310:0x076e, B:313:0x077d, B:315:0x0785, B:317:0x07a4, B:319:0x07a8, B:321:0x07ae, B:323:0x07b5, B:325:0x07bc, B:326:0x07c0, B:328:0x07d3, B:329:0x07d9, B:342:0x07f8, B:352:0x0839, B:354:0x0843, B:359:0x0851, B:361:0x0857, B:363:0x085d, B:365:0x0869, B:367:0x0875, B:259:0x06a2, B:261:0x06bc, B:262:0x06c0, B:227:0x05e9, B:232:0x0605, B:187:0x054c), top: B:526:0x0053 }] */
    /* JADX WARN: Removed duplicated region for block: B:254:0x0691 A[Catch: Exception -> 0x06df, all -> 0x0aed, TRY_ENTER, TryCatch #9 {Exception -> 0x06df, blocks: (B:242:0x063f, B:254:0x0691, B:256:0x0699, B:259:0x06a2), top: B:506:0x063f }] */
    /* JADX WARN: Removed duplicated region for block: B:271:0x06f8  */
    /* JADX WARN: Removed duplicated region for block: B:279:0x0707  */
    /* JADX WARN: Removed duplicated region for block: B:290:0x0735  */
    /* JADX WARN: Removed duplicated region for block: B:299:0x0754 A[Catch: Exception -> 0x08de, all -> 0x0aed, TRY_ENTER, TRY_LEAVE, TryCatch #12 {Exception -> 0x08de, blocks: (B:293:0x073c, B:299:0x0754, B:303:0x075d), top: B:512:0x073c }] */
    /* JADX WARN: Removed duplicated region for block: B:393:0x08d2 A[Catch: Exception -> 0x08db, all -> 0x0aed, TryCatch #18 {Exception -> 0x08db, blocks: (B:378:0x089d, B:380:0x08a1, B:382:0x08a5, B:384:0x08ab, B:386:0x08b3, B:387:0x08ba, B:388:0x08c4, B:393:0x08d2, B:394:0x08da), top: B:524:0x089d }] */
    /* JADX WARN: Removed duplicated region for block: B:543:0x087c A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:561:0x0740 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:563:0x06fb A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private void loadWorkspace() {
        boolean z;
        MultiHashMap multiHashMap;
        LongSparseArray longSparseArray;
        int i;
        int i2;
        int i3;
        int i4;
        FolderIconPreviewVerifier folderIconPreviewVerifier;
        LongSparseArray longSparseArray2;
        PackageManagerHelper packageManagerHelper;
        HashMap hashMap;
        HashMap<String, PackageInstaller.SessionInfo> hashMap2;
        Context context;
        int i5;
        MultiHashMap multiHashMap2;
        int i6;
        LongSparseArray longSparseArray3;
        HashMap<ComponentKey, AppWidgetProviderInfo> hashMap3;
        LongSparseArray longSparseArray4;
        FolderIconPreviewVerifier folderIconPreviewVerifier2;
        LongSparseArray longSparseArray5;
        FolderIconPreviewVerifier folderIconPreviewVerifier3;
        HashMap hashMap4;
        Intent parseIntent;
        int i7;
        ComponentName component;
        String str;
        boolean z2;
        boolean z3;
        final ShortcutInfo restoredItemInfo;
        ShortcutKey fromIntent;
        Provider<Bitmap> provider;
        LauncherIcons obtain;
        LongSparseArray longSparseArray6;
        PackageManagerHelper packageManagerHelper2;
        FolderInfo findOrMakeFolder;
        int i8;
        LauncherAppWidgetInfo launcherAppWidgetInfo;
        boolean z4;
        boolean z5;
        boolean z6;
        Context context2 = this.mApp.getContext();
        ContentResolver contentResolver = context2.getContentResolver();
        PackageManagerHelper packageManagerHelper3 = new PackageManagerHelper(context2);
        boolean isSafeMode = packageManagerHelper3.isSafeMode();
        boolean isBootCompleted = Utilities.isBootCompleted();
        MultiHashMap multiHashMap3 = new MultiHashMap();
        try {
            ImportDataTask.performImportIfPossible(context2);
            z = false;
        } catch (Exception e) {
            z = true;
        }
        if (!z && GridSizeMigrationTask.ENABLED && !GridSizeMigrationTask.migrateGridIfNeeded(context2)) {
            z = true;
        }
        if (z) {
            Log.d(TAG, "loadWorkspace: resetting launcher database");
            LauncherSettings.Settings.call(contentResolver, LauncherSettings.Settings.METHOD_CREATE_EMPTY_DB);
        }
        Log.d(TAG, "loadWorkspace: loading default favorites");
        LauncherSettings.Settings.call(contentResolver, LauncherSettings.Settings.METHOD_LOAD_DEFAULT_FAVORITES);
        synchronized (this.mBgDataModel) {
            try {
                try {
                    this.mBgDataModel.clear();
                    HashMap<String, PackageInstaller.SessionInfo> updateAndGetActiveSessionCache = this.mPackageInstaller.updateAndGetActiveSessionCache();
                    this.mFirstScreenBroadcast = new FirstScreenBroadcast(updateAndGetActiveSessionCache);
                    this.mBgDataModel.workspaceScreens.addAll(LauncherModel.loadWorkspaceScreensDb(context2));
                    HashMap hashMap5 = new HashMap();
                    HashMap<String, PackageInstaller.SessionInfo> hashMap6 = updateAndGetActiveSessionCache;
                    final LoaderCursor loaderCursor = new LoaderCursor(contentResolver.query(LauncherSettings.Favorites.CONTENT_URI, null, null, null, null), this.mApp);
                    int columnIndexOrThrow = loaderCursor.getColumnIndexOrThrow(LauncherSettings.Favorites.APPWIDGET_ID);
                    int columnIndexOrThrow2 = loaderCursor.getColumnIndexOrThrow(LauncherSettings.Favorites.APPWIDGET_PROVIDER);
                    int columnIndexOrThrow3 = loaderCursor.getColumnIndexOrThrow(LauncherSettings.Favorites.SPANX);
                    int columnIndexOrThrow4 = loaderCursor.getColumnIndexOrThrow(LauncherSettings.Favorites.SPANY);
                    int columnIndexOrThrow5 = loaderCursor.getColumnIndexOrThrow(LauncherSettings.Favorites.RANK);
                    int columnIndexOrThrow6 = loaderCursor.getColumnIndexOrThrow(LauncherSettings.Favorites.OPTIONS);
                    LongSparseArray<UserHandle> longSparseArray7 = loaderCursor.allUsers;
                    LongSparseArray longSparseArray8 = new LongSparseArray();
                    Context context3 = context2;
                    LongSparseArray longSparseArray9 = new LongSparseArray();
                    int i9 = columnIndexOrThrow5;
                    Iterator<UserHandle> it = this.mUserManager.getUserProfiles().iterator();
                    while (true) {
                        multiHashMap = multiHashMap3;
                        if (!it.hasNext()) {
                            break;
                        }
                        UserHandle next = it.next();
                        Iterator<UserHandle> it2 = it;
                        int i10 = columnIndexOrThrow6;
                        long serialNumberForUser = this.mUserManager.getSerialNumberForUser(next);
                        longSparseArray7.put(serialNumberForUser, next);
                        LongSparseArray<UserHandle> longSparseArray10 = longSparseArray7;
                        longSparseArray8.put(serialNumberForUser, Boolean.valueOf(this.mUserManager.isQuietModeEnabled(next)));
                        boolean isUserUnlocked = this.mUserManager.isUserUnlocked(next);
                        if (isUserUnlocked) {
                            z4 = isUserUnlocked;
                            z5 = isBootCompleted;
                            List<ShortcutInfoCompat> queryForPinnedShortcuts = this.mShortcutManager.queryForPinnedShortcuts(null, next);
                            if (this.mShortcutManager.wasLastCallSuccess()) {
                                for (ShortcutInfoCompat shortcutInfoCompat : queryForPinnedShortcuts) {
                                    hashMap5.put(ShortcutKey.fromInfo(shortcutInfoCompat), shortcutInfoCompat);
                                }
                            } else {
                                z6 = false;
                                longSparseArray9.put(serialNumberForUser, Boolean.valueOf(z6));
                                multiHashMap3 = multiHashMap;
                                it = it2;
                                columnIndexOrThrow6 = i10;
                                longSparseArray7 = longSparseArray10;
                                isBootCompleted = z5;
                            }
                        } else {
                            z4 = isUserUnlocked;
                            z5 = isBootCompleted;
                        }
                        z6 = z4;
                        longSparseArray9.put(serialNumberForUser, Boolean.valueOf(z6));
                        multiHashMap3 = multiHashMap;
                        it = it2;
                        columnIndexOrThrow6 = i10;
                        longSparseArray7 = longSparseArray10;
                        isBootCompleted = z5;
                    }
                    int i11 = columnIndexOrThrow6;
                    boolean z7 = isBootCompleted;
                    FolderIconPreviewVerifier folderIconPreviewVerifier4 = new FolderIconPreviewVerifier(this.mApp.getInvariantDeviceProfile());
                    HashMap<ComponentKey, AppWidgetProviderInfo> hashMap7 = null;
                    while (!this.mStopped && loaderCursor.moveToNext()) {
                        try {
                        } catch (Exception e2) {
                            e = e2;
                            longSparseArray = longSparseArray9;
                            i = columnIndexOrThrow;
                            i2 = columnIndexOrThrow2;
                            i3 = columnIndexOrThrow3;
                            i4 = columnIndexOrThrow4;
                            folderIconPreviewVerifier = folderIconPreviewVerifier4;
                            longSparseArray2 = longSparseArray8;
                            packageManagerHelper = packageManagerHelper3;
                        }
                        if (loaderCursor.user != null) {
                            switch (loaderCursor.itemType) {
                                case 0:
                                case 1:
                                case 6:
                                    longSparseArray5 = longSparseArray9;
                                    i = columnIndexOrThrow;
                                    i2 = columnIndexOrThrow2;
                                    folderIconPreviewVerifier3 = folderIconPreviewVerifier4;
                                    LongSparseArray longSparseArray11 = longSparseArray8;
                                    PackageManagerHelper packageManagerHelper4 = packageManagerHelper3;
                                    hashMap3 = hashMap7;
                                    hashMap4 = hashMap5;
                                    hashMap2 = hashMap6;
                                    int i12 = i11;
                                    try {
                                        parseIntent = loaderCursor.parseIntent();
                                    } catch (Exception e3) {
                                        e = e3;
                                        i6 = i12;
                                        i3 = columnIndexOrThrow3;
                                        i4 = columnIndexOrThrow4;
                                        context = context3;
                                        i5 = i9;
                                        multiHashMap2 = multiHashMap;
                                        hashMap = hashMap4;
                                        longSparseArray = longSparseArray5;
                                        folderIconPreviewVerifier = folderIconPreviewVerifier3;
                                        packageManagerHelper = packageManagerHelper4;
                                        longSparseArray2 = longSparseArray11;
                                    }
                                    if (parseIntent == null) {
                                        loaderCursor.markDeleted("Invalid or null intent");
                                        i6 = i12;
                                        i3 = columnIndexOrThrow3;
                                        i4 = columnIndexOrThrow4;
                                        i5 = i9;
                                        multiHashMap2 = multiHashMap;
                                        hashMap = hashMap4;
                                        longSparseArray4 = longSparseArray5;
                                        folderIconPreviewVerifier2 = folderIconPreviewVerifier3;
                                        packageManagerHelper = packageManagerHelper4;
                                        longSparseArray3 = longSparseArray11;
                                        break;
                                    } else {
                                        longSparseArray3 = longSparseArray11;
                                        try {
                                            i7 = ((Boolean) longSparseArray3.get(loaderCursor.serialNumber)).booleanValue() ? 8 : 0;
                                            component = parseIntent.getComponent();
                                            if (component == null) {
                                                try {
                                                    str = parseIntent.getPackage();
                                                } catch (Exception e4) {
                                                    e = e4;
                                                    i6 = i12;
                                                    i3 = columnIndexOrThrow3;
                                                    i4 = columnIndexOrThrow4;
                                                    longSparseArray2 = longSparseArray3;
                                                    context = context3;
                                                    i5 = i9;
                                                    multiHashMap2 = multiHashMap;
                                                    hashMap7 = hashMap3;
                                                    hashMap = hashMap4;
                                                    longSparseArray = longSparseArray5;
                                                    folderIconPreviewVerifier = folderIconPreviewVerifier3;
                                                    packageManagerHelper = packageManagerHelper4;
                                                    Log.e(TAG, "Desktop items loading interrupted", e);
                                                    multiHashMap = multiHashMap2;
                                                    hashMap5 = hashMap;
                                                    context3 = context;
                                                    hashMap6 = hashMap2;
                                                    i9 = i5;
                                                    packageManagerHelper3 = packageManagerHelper;
                                                    columnIndexOrThrow = i;
                                                    columnIndexOrThrow2 = i2;
                                                    i11 = i6;
                                                    columnIndexOrThrow3 = i3;
                                                    columnIndexOrThrow4 = i4;
                                                    longSparseArray8 = longSparseArray2;
                                                    longSparseArray9 = longSparseArray;
                                                    folderIconPreviewVerifier4 = folderIconPreviewVerifier;
                                                }
                                            } else {
                                                str = component.getPackageName();
                                            }
                                            if (!Process.myUserHandle().equals(loaderCursor.user)) {
                                                if (loaderCursor.itemType == 1) {
                                                    loaderCursor.markDeleted("Legacy shortcuts are only allowed for default user");
                                                } else if (loaderCursor.restoreFlag != 0) {
                                                    loaderCursor.markDeleted("Restore from managed profile not supported");
                                                }
                                                i6 = i12;
                                                i3 = columnIndexOrThrow3;
                                                i4 = columnIndexOrThrow4;
                                                i5 = i9;
                                                multiHashMap2 = multiHashMap;
                                                hashMap = hashMap4;
                                                longSparseArray4 = longSparseArray5;
                                                folderIconPreviewVerifier2 = folderIconPreviewVerifier3;
                                                packageManagerHelper = packageManagerHelper4;
                                                break;
                                            }
                                        } catch (Exception e5) {
                                            e = e5;
                                            i6 = i12;
                                            i3 = columnIndexOrThrow3;
                                            i4 = columnIndexOrThrow4;
                                            longSparseArray2 = longSparseArray3;
                                            context = context3;
                                            i5 = i9;
                                            multiHashMap2 = multiHashMap;
                                            hashMap = hashMap4;
                                            longSparseArray = longSparseArray5;
                                            folderIconPreviewVerifier = folderIconPreviewVerifier3;
                                            packageManagerHelper = packageManagerHelper4;
                                        }
                                        if (!TextUtils.isEmpty(str) || loaderCursor.itemType == 1) {
                                            boolean z8 = TextUtils.isEmpty(str) || this.mLauncherApps.isPackageEnabledForProfile(str, loaderCursor.user);
                                            if (component != null && z8) {
                                                try {
                                                } catch (Exception e6) {
                                                    e = e6;
                                                    i6 = i12;
                                                    packageManagerHelper = packageManagerHelper4;
                                                }
                                                if (this.mLauncherApps.isActivityEnabledForProfile(component, loaderCursor.user)) {
                                                    loaderCursor.markRestored();
                                                } else {
                                                    if (loaderCursor.hasRestoreFlag(2)) {
                                                        packageManagerHelper = packageManagerHelper4;
                                                        try {
                                                            parseIntent = packageManagerHelper.getAppLaunchIntent(str, loaderCursor.user);
                                                        } catch (Exception e7) {
                                                            e = e7;
                                                            i6 = i12;
                                                        }
                                                        if (parseIntent != null) {
                                                            loaderCursor.restoreFlag = 0;
                                                            i6 = i12;
                                                            try {
                                                                loaderCursor.updater().put(LauncherSettings.BaseLauncherColumns.INTENT, parseIntent.toUri(0)).commit();
                                                                parseIntent.getComponent();
                                                            } catch (Exception e8) {
                                                                e = e8;
                                                                i3 = columnIndexOrThrow3;
                                                                i4 = columnIndexOrThrow4;
                                                                longSparseArray2 = longSparseArray3;
                                                                context = context3;
                                                                i5 = i9;
                                                                multiHashMap2 = multiHashMap;
                                                                hashMap7 = hashMap3;
                                                                hashMap = hashMap4;
                                                                longSparseArray = longSparseArray5;
                                                                folderIconPreviewVerifier = folderIconPreviewVerifier3;
                                                                Log.e(TAG, "Desktop items loading interrupted", e);
                                                                multiHashMap = multiHashMap2;
                                                                hashMap5 = hashMap;
                                                                context3 = context;
                                                                hashMap6 = hashMap2;
                                                                i9 = i5;
                                                                packageManagerHelper3 = packageManagerHelper;
                                                                columnIndexOrThrow = i;
                                                                columnIndexOrThrow2 = i2;
                                                                i11 = i6;
                                                                columnIndexOrThrow3 = i3;
                                                                columnIndexOrThrow4 = i4;
                                                                longSparseArray8 = longSparseArray2;
                                                                longSparseArray9 = longSparseArray;
                                                                folderIconPreviewVerifier4 = folderIconPreviewVerifier;
                                                            }
                                                            if (!TextUtils.isEmpty(str) && !z8) {
                                                                try {
                                                                } catch (Exception e9) {
                                                                    e = e9;
                                                                    multiHashMap2 = multiHashMap;
                                                                    i3 = columnIndexOrThrow3;
                                                                    i4 = columnIndexOrThrow4;
                                                                    longSparseArray2 = longSparseArray3;
                                                                    context = context3;
                                                                    i5 = i9;
                                                                    hashMap7 = hashMap3;
                                                                    hashMap = hashMap4;
                                                                    longSparseArray = longSparseArray5;
                                                                    folderIconPreviewVerifier = folderIconPreviewVerifier3;
                                                                    Log.e(TAG, "Desktop items loading interrupted", e);
                                                                    multiHashMap = multiHashMap2;
                                                                    hashMap5 = hashMap;
                                                                    context3 = context;
                                                                    hashMap6 = hashMap2;
                                                                    i9 = i5;
                                                                    packageManagerHelper3 = packageManagerHelper;
                                                                    columnIndexOrThrow = i;
                                                                    columnIndexOrThrow2 = i2;
                                                                    i11 = i6;
                                                                    columnIndexOrThrow3 = i3;
                                                                    columnIndexOrThrow4 = i4;
                                                                    longSparseArray8 = longSparseArray2;
                                                                    longSparseArray9 = longSparseArray;
                                                                    folderIconPreviewVerifier4 = folderIconPreviewVerifier;
                                                                }
                                                                if (loaderCursor.restoreFlag == 0) {
                                                                    FileLog.d(TAG, "package not yet restored: " + str);
                                                                    if (!loaderCursor.hasRestoreFlag(8)) {
                                                                        if (hashMap2.containsKey(str)) {
                                                                            loaderCursor.restoreFlag |= 8;
                                                                            loaderCursor.updater().commit();
                                                                        } else {
                                                                            loaderCursor.markDeleted("Unrestored app removed: " + str);
                                                                        }
                                                                    }
                                                                } else {
                                                                    if (packageManagerHelper.isAppOnSdcard(str, loaderCursor.user)) {
                                                                        i7 |= 2;
                                                                        multiHashMap2 = multiHashMap;
                                                                    } else if (z7) {
                                                                        multiHashMap2 = multiHashMap;
                                                                        loaderCursor.markDeleted("Invalid package removed: " + str);
                                                                        i3 = columnIndexOrThrow3;
                                                                        i4 = columnIndexOrThrow4;
                                                                        i5 = i9;
                                                                        hashMap = hashMap4;
                                                                        longSparseArray4 = longSparseArray5;
                                                                        folderIconPreviewVerifier2 = folderIconPreviewVerifier3;
                                                                        break;
                                                                    } else {
                                                                        Log.d(TAG, "Missing pkg, will check later: " + str);
                                                                        multiHashMap2 = multiHashMap;
                                                                        multiHashMap2.addToList(loaderCursor.user, str);
                                                                    }
                                                                    z2 = true;
                                                                    if ((loaderCursor.restoreFlag & 16) != 0) {
                                                                        z8 = false;
                                                                    }
                                                                    if (z8) {
                                                                        try {
                                                                            loaderCursor.markRestored();
                                                                        } catch (Exception e10) {
                                                                            e = e10;
                                                                            i3 = columnIndexOrThrow3;
                                                                            i4 = columnIndexOrThrow4;
                                                                            longSparseArray2 = longSparseArray3;
                                                                            context = context3;
                                                                            i5 = i9;
                                                                            hashMap7 = hashMap3;
                                                                            hashMap = hashMap4;
                                                                            longSparseArray = longSparseArray5;
                                                                            folderIconPreviewVerifier = folderIconPreviewVerifier3;
                                                                            Log.e(TAG, "Desktop items loading interrupted", e);
                                                                            multiHashMap = multiHashMap2;
                                                                            hashMap5 = hashMap;
                                                                            context3 = context;
                                                                            hashMap6 = hashMap2;
                                                                            i9 = i5;
                                                                            packageManagerHelper3 = packageManagerHelper;
                                                                            columnIndexOrThrow = i;
                                                                            columnIndexOrThrow2 = i2;
                                                                            i11 = i6;
                                                                            columnIndexOrThrow3 = i3;
                                                                            columnIndexOrThrow4 = i4;
                                                                            longSparseArray8 = longSparseArray2;
                                                                            longSparseArray9 = longSparseArray;
                                                                            folderIconPreviewVerifier4 = folderIconPreviewVerifier;
                                                                        }
                                                                    }
                                                                    if (loaderCursor.isOnWorkspaceOrHotseat()) {
                                                                        i5 = i9;
                                                                        folderIconPreviewVerifier2 = folderIconPreviewVerifier3;
                                                                    } else {
                                                                        i5 = i9;
                                                                        try {
                                                                            folderIconPreviewVerifier2 = folderIconPreviewVerifier3;
                                                                        } catch (Exception e11) {
                                                                            e = e11;
                                                                            i3 = columnIndexOrThrow3;
                                                                            i4 = columnIndexOrThrow4;
                                                                            longSparseArray2 = longSparseArray3;
                                                                            context = context3;
                                                                            hashMap7 = hashMap3;
                                                                            hashMap = hashMap4;
                                                                            longSparseArray = longSparseArray5;
                                                                            folderIconPreviewVerifier = folderIconPreviewVerifier3;
                                                                            Log.e(TAG, "Desktop items loading interrupted", e);
                                                                            multiHashMap = multiHashMap2;
                                                                            hashMap5 = hashMap;
                                                                            context3 = context;
                                                                            hashMap6 = hashMap2;
                                                                            i9 = i5;
                                                                            packageManagerHelper3 = packageManagerHelper;
                                                                            columnIndexOrThrow = i;
                                                                            columnIndexOrThrow2 = i2;
                                                                            i11 = i6;
                                                                            columnIndexOrThrow3 = i3;
                                                                            columnIndexOrThrow4 = i4;
                                                                            longSparseArray8 = longSparseArray2;
                                                                            longSparseArray9 = longSparseArray;
                                                                            folderIconPreviewVerifier4 = folderIconPreviewVerifier;
                                                                        }
                                                                        try {
                                                                        } catch (Exception e12) {
                                                                            e = e12;
                                                                            i3 = columnIndexOrThrow3;
                                                                            i4 = columnIndexOrThrow4;
                                                                            longSparseArray2 = longSparseArray3;
                                                                            folderIconPreviewVerifier = folderIconPreviewVerifier2;
                                                                            context = context3;
                                                                            hashMap7 = hashMap3;
                                                                            hashMap = hashMap4;
                                                                            longSparseArray = longSparseArray5;
                                                                            Log.e(TAG, "Desktop items loading interrupted", e);
                                                                            multiHashMap = multiHashMap2;
                                                                            hashMap5 = hashMap;
                                                                            context3 = context;
                                                                            hashMap6 = hashMap2;
                                                                            i9 = i5;
                                                                            packageManagerHelper3 = packageManagerHelper;
                                                                            columnIndexOrThrow = i;
                                                                            columnIndexOrThrow2 = i2;
                                                                            i11 = i6;
                                                                            columnIndexOrThrow3 = i3;
                                                                            columnIndexOrThrow4 = i4;
                                                                            longSparseArray8 = longSparseArray2;
                                                                            longSparseArray9 = longSparseArray;
                                                                            folderIconPreviewVerifier4 = folderIconPreviewVerifier;
                                                                        }
                                                                        if (!folderIconPreviewVerifier2.isItemInPreview(loaderCursor.getInt(i5))) {
                                                                            z3 = true;
                                                                            i3 = columnIndexOrThrow3;
                                                                            if (loaderCursor.restoreFlag == 0) {
                                                                                try {
                                                                                    restoredItemInfo = loaderCursor.getRestoredItemInfo(parseIntent);
                                                                                } catch (Exception e13) {
                                                                                    e = e13;
                                                                                    i4 = columnIndexOrThrow4;
                                                                                    longSparseArray2 = longSparseArray3;
                                                                                    folderIconPreviewVerifier = folderIconPreviewVerifier2;
                                                                                    context = context3;
                                                                                    hashMap7 = hashMap3;
                                                                                    hashMap = hashMap4;
                                                                                    longSparseArray = longSparseArray5;
                                                                                    Log.e(TAG, "Desktop items loading interrupted", e);
                                                                                    multiHashMap = multiHashMap2;
                                                                                    hashMap5 = hashMap;
                                                                                    context3 = context;
                                                                                    hashMap6 = hashMap2;
                                                                                    i9 = i5;
                                                                                    packageManagerHelper3 = packageManagerHelper;
                                                                                    columnIndexOrThrow = i;
                                                                                    columnIndexOrThrow2 = i2;
                                                                                    i11 = i6;
                                                                                    columnIndexOrThrow3 = i3;
                                                                                    columnIndexOrThrow4 = i4;
                                                                                    longSparseArray8 = longSparseArray2;
                                                                                    longSparseArray9 = longSparseArray;
                                                                                    folderIconPreviewVerifier4 = folderIconPreviewVerifier;
                                                                                }
                                                                            } else if (loaderCursor.itemType == 0) {
                                                                                restoredItemInfo = loaderCursor.getAppShortcutInfo(parseIntent, z2, z3);
                                                                            } else {
                                                                                if (loaderCursor.itemType == 6) {
                                                                                    try {
                                                                                        fromIntent = ShortcutKey.fromIntent(parseIntent, loaderCursor.user);
                                                                                        i4 = columnIndexOrThrow4;
                                                                                    } catch (Exception e14) {
                                                                                        e = e14;
                                                                                        i4 = columnIndexOrThrow4;
                                                                                    }
                                                                                    try {
                                                                                        longSparseArray4 = longSparseArray5;
                                                                                        try {
                                                                                            if (((Boolean) longSparseArray4.get(loaderCursor.serialNumber)).booleanValue()) {
                                                                                                hashMap = hashMap4;
                                                                                                try {
                                                                                                    ShortcutInfoCompat shortcutInfoCompat2 = (ShortcutInfoCompat) hashMap.get(fromIntent);
                                                                                                    if (shortcutInfoCompat2 == null) {
                                                                                                        loaderCursor.markDeleted("Pinned shortcut not found");
                                                                                                        break;
                                                                                                    } else {
                                                                                                        context = context3;
                                                                                                        try {
                                                                                                            restoredItemInfo = new ShortcutInfo(shortcutInfoCompat2, context);
                                                                                                            longSparseArray2 = longSparseArray3;
                                                                                                        } catch (Exception e15) {
                                                                                                            e = e15;
                                                                                                            longSparseArray2 = longSparseArray3;
                                                                                                        }
                                                                                                        try {
                                                                                                            provider = new Provider<Bitmap>() { // from class: com.android.launcher3.model.LoaderTask.1
                                                                                                                /* JADX WARN: Can't rename method to resolve collision */
                                                                                                                @Override // com.android.launcher3.util.Provider
                                                                                                                public Bitmap get() {
                                                                                                                    if (loaderCursor.loadIcon(restoredItemInfo)) {
                                                                                                                        return restoredItemInfo.iconBitmap;
                                                                                                                    }
                                                                                                                    return null;
                                                                                                                }
                                                                                                            };
                                                                                                            longSparseArray = longSparseArray4;
                                                                                                            try {
                                                                                                                obtain = LauncherIcons.obtain(context);
                                                                                                                folderIconPreviewVerifier = folderIconPreviewVerifier2;
                                                                                                                try {
                                                                                                                } catch (Exception e16) {
                                                                                                                    e = e16;
                                                                                                                }
                                                                                                            } catch (Exception e17) {
                                                                                                                e = e17;
                                                                                                                folderIconPreviewVerifier = folderIconPreviewVerifier2;
                                                                                                                hashMap7 = hashMap3;
                                                                                                                Log.e(TAG, "Desktop items loading interrupted", e);
                                                                                                                multiHashMap = multiHashMap2;
                                                                                                                hashMap5 = hashMap;
                                                                                                                context3 = context;
                                                                                                                hashMap6 = hashMap2;
                                                                                                                i9 = i5;
                                                                                                                packageManagerHelper3 = packageManagerHelper;
                                                                                                                columnIndexOrThrow = i;
                                                                                                                columnIndexOrThrow2 = i2;
                                                                                                                i11 = i6;
                                                                                                                columnIndexOrThrow3 = i3;
                                                                                                                columnIndexOrThrow4 = i4;
                                                                                                                longSparseArray8 = longSparseArray2;
                                                                                                                longSparseArray9 = longSparseArray;
                                                                                                                folderIconPreviewVerifier4 = folderIconPreviewVerifier;
                                                                                                            }
                                                                                                        } catch (Exception e18) {
                                                                                                            e = e18;
                                                                                                            longSparseArray = longSparseArray4;
                                                                                                            folderIconPreviewVerifier = folderIconPreviewVerifier2;
                                                                                                            hashMap7 = hashMap3;
                                                                                                            Log.e(TAG, "Desktop items loading interrupted", e);
                                                                                                            multiHashMap = multiHashMap2;
                                                                                                            hashMap5 = hashMap;
                                                                                                            context3 = context;
                                                                                                            hashMap6 = hashMap2;
                                                                                                            i9 = i5;
                                                                                                            packageManagerHelper3 = packageManagerHelper;
                                                                                                            columnIndexOrThrow = i;
                                                                                                            columnIndexOrThrow2 = i2;
                                                                                                            i11 = i6;
                                                                                                            columnIndexOrThrow3 = i3;
                                                                                                            columnIndexOrThrow4 = i4;
                                                                                                            longSparseArray8 = longSparseArray2;
                                                                                                            longSparseArray9 = longSparseArray;
                                                                                                            folderIconPreviewVerifier4 = folderIconPreviewVerifier;
                                                                                                        }
                                                                                                        try {
                                                                                                            obtain.createShortcutIcon(shortcutInfoCompat2, true, provider).applyTo(restoredItemInfo);
                                                                                                            obtain.recycle();
                                                                                                            if (packageManagerHelper.isAppSuspended(shortcutInfoCompat2.getPackage(), restoredItemInfo.user)) {
                                                                                                                restoredItemInfo.runtimeStatusFlags |= 4;
                                                                                                            }
                                                                                                            parseIntent = restoredItemInfo.intent;
                                                                                                        } catch (Exception e19) {
                                                                                                            e = e19;
                                                                                                            hashMap7 = hashMap3;
                                                                                                            Log.e(TAG, "Desktop items loading interrupted", e);
                                                                                                            multiHashMap = multiHashMap2;
                                                                                                            hashMap5 = hashMap;
                                                                                                            context3 = context;
                                                                                                            hashMap6 = hashMap2;
                                                                                                            i9 = i5;
                                                                                                            packageManagerHelper3 = packageManagerHelper;
                                                                                                            columnIndexOrThrow = i;
                                                                                                            columnIndexOrThrow2 = i2;
                                                                                                            i11 = i6;
                                                                                                            columnIndexOrThrow3 = i3;
                                                                                                            columnIndexOrThrow4 = i4;
                                                                                                            longSparseArray8 = longSparseArray2;
                                                                                                            longSparseArray9 = longSparseArray;
                                                                                                            folderIconPreviewVerifier4 = folderIconPreviewVerifier;
                                                                                                        }
                                                                                                    }
                                                                                                } catch (Exception e20) {
                                                                                                    e = e20;
                                                                                                    longSparseArray2 = longSparseArray3;
                                                                                                    longSparseArray = longSparseArray4;
                                                                                                    folderIconPreviewVerifier = folderIconPreviewVerifier2;
                                                                                                    context = context3;
                                                                                                }
                                                                                            } else {
                                                                                                longSparseArray2 = longSparseArray3;
                                                                                                longSparseArray = longSparseArray4;
                                                                                                folderIconPreviewVerifier = folderIconPreviewVerifier2;
                                                                                                context = context3;
                                                                                                hashMap = hashMap4;
                                                                                                restoredItemInfo = loaderCursor.loadSimpleShortcut();
                                                                                                restoredItemInfo.runtimeStatusFlags |= 32;
                                                                                            }
                                                                                        } catch (Exception e21) {
                                                                                            e = e21;
                                                                                            longSparseArray2 = longSparseArray3;
                                                                                            longSparseArray = longSparseArray4;
                                                                                            folderIconPreviewVerifier = folderIconPreviewVerifier2;
                                                                                            context = context3;
                                                                                            hashMap = hashMap4;
                                                                                        }
                                                                                    } catch (Exception e22) {
                                                                                        e = e22;
                                                                                        longSparseArray2 = longSparseArray3;
                                                                                        folderIconPreviewVerifier = folderIconPreviewVerifier2;
                                                                                        context = context3;
                                                                                        hashMap = hashMap4;
                                                                                        longSparseArray = longSparseArray5;
                                                                                        hashMap7 = hashMap3;
                                                                                        Log.e(TAG, "Desktop items loading interrupted", e);
                                                                                        multiHashMap = multiHashMap2;
                                                                                        hashMap5 = hashMap;
                                                                                        context3 = context;
                                                                                        hashMap6 = hashMap2;
                                                                                        i9 = i5;
                                                                                        packageManagerHelper3 = packageManagerHelper;
                                                                                        columnIndexOrThrow = i;
                                                                                        columnIndexOrThrow2 = i2;
                                                                                        i11 = i6;
                                                                                        columnIndexOrThrow3 = i3;
                                                                                        columnIndexOrThrow4 = i4;
                                                                                        longSparseArray8 = longSparseArray2;
                                                                                        longSparseArray9 = longSparseArray;
                                                                                        folderIconPreviewVerifier4 = folderIconPreviewVerifier;
                                                                                    }
                                                                                } else {
                                                                                    i4 = columnIndexOrThrow4;
                                                                                    longSparseArray2 = longSparseArray3;
                                                                                    folderIconPreviewVerifier = folderIconPreviewVerifier2;
                                                                                    context = context3;
                                                                                    hashMap = hashMap4;
                                                                                    longSparseArray = longSparseArray5;
                                                                                    restoredItemInfo = loaderCursor.loadSimpleShortcut();
                                                                                    if (!TextUtils.isEmpty(str) && packageManagerHelper.isAppSuspended(str, loaderCursor.user)) {
                                                                                        i7 |= 4;
                                                                                    }
                                                                                    if (parseIntent.getAction() != null && parseIntent.getCategories() != null && parseIntent.getAction().equals("android.intent.action.MAIN") && parseIntent.getCategories().contains("android.intent.category.LAUNCHER")) {
                                                                                        parseIntent.addFlags(270532608);
                                                                                    }
                                                                                }
                                                                                if (restoredItemInfo == null) {
                                                                                    throw new RuntimeException("Unexpected null ShortcutInfo");
                                                                                    break;
                                                                                } else {
                                                                                    try {
                                                                                        loaderCursor.applyCommonProperties(restoredItemInfo);
                                                                                        restoredItemInfo.intent = parseIntent;
                                                                                        restoredItemInfo.rank = loaderCursor.getInt(i5);
                                                                                        try {
                                                                                            restoredItemInfo.spanX = 1;
                                                                                            restoredItemInfo.spanY = 1;
                                                                                            restoredItemInfo.runtimeStatusFlags = i7 | restoredItemInfo.runtimeStatusFlags;
                                                                                            if (isSafeMode && !Utilities.isSystemApp(context, parseIntent)) {
                                                                                                try {
                                                                                                    restoredItemInfo.runtimeStatusFlags |= 1;
                                                                                                } catch (Exception e23) {
                                                                                                    e = e23;
                                                                                                }
                                                                                            }
                                                                                            if (loaderCursor.restoreFlag != 0 && !TextUtils.isEmpty(str)) {
                                                                                                PackageInstaller.SessionInfo sessionInfo = hashMap2.get(str);
                                                                                                if (sessionInfo == null) {
                                                                                                    restoredItemInfo.status &= -5;
                                                                                                } else {
                                                                                                    restoredItemInfo.setInstallProgress((int) (sessionInfo.getProgress() * 100.0f));
                                                                                                }
                                                                                            }
                                                                                            loaderCursor.checkAndAddItem(restoredItemInfo, this.mBgDataModel);
                                                                                            hashMap7 = hashMap3;
                                                                                        } catch (Exception e24) {
                                                                                            e = e24;
                                                                                        }
                                                                                    } catch (Exception e25) {
                                                                                        e = e25;
                                                                                        hashMap7 = hashMap3;
                                                                                        Log.e(TAG, "Desktop items loading interrupted", e);
                                                                                        multiHashMap = multiHashMap2;
                                                                                        hashMap5 = hashMap;
                                                                                        context3 = context;
                                                                                        hashMap6 = hashMap2;
                                                                                        i9 = i5;
                                                                                        packageManagerHelper3 = packageManagerHelper;
                                                                                        columnIndexOrThrow = i;
                                                                                        columnIndexOrThrow2 = i2;
                                                                                        i11 = i6;
                                                                                        columnIndexOrThrow3 = i3;
                                                                                        columnIndexOrThrow4 = i4;
                                                                                        longSparseArray8 = longSparseArray2;
                                                                                        longSparseArray9 = longSparseArray;
                                                                                        folderIconPreviewVerifier4 = folderIconPreviewVerifier;
                                                                                    }
                                                                                    multiHashMap = multiHashMap2;
                                                                                    hashMap5 = hashMap;
                                                                                    context3 = context;
                                                                                    hashMap6 = hashMap2;
                                                                                    i9 = i5;
                                                                                    packageManagerHelper3 = packageManagerHelper;
                                                                                    columnIndexOrThrow = i;
                                                                                    columnIndexOrThrow2 = i2;
                                                                                    i11 = i6;
                                                                                    columnIndexOrThrow3 = i3;
                                                                                    columnIndexOrThrow4 = i4;
                                                                                    longSparseArray8 = longSparseArray2;
                                                                                    longSparseArray9 = longSparseArray;
                                                                                    folderIconPreviewVerifier4 = folderIconPreviewVerifier;
                                                                                }
                                                                                e = e23;
                                                                                hashMap7 = hashMap3;
                                                                                Log.e(TAG, "Desktop items loading interrupted", e);
                                                                                multiHashMap = multiHashMap2;
                                                                                hashMap5 = hashMap;
                                                                                context3 = context;
                                                                                hashMap6 = hashMap2;
                                                                                i9 = i5;
                                                                                packageManagerHelper3 = packageManagerHelper;
                                                                                columnIndexOrThrow = i;
                                                                                columnIndexOrThrow2 = i2;
                                                                                i11 = i6;
                                                                                columnIndexOrThrow3 = i3;
                                                                                columnIndexOrThrow4 = i4;
                                                                                longSparseArray8 = longSparseArray2;
                                                                                longSparseArray9 = longSparseArray;
                                                                                folderIconPreviewVerifier4 = folderIconPreviewVerifier;
                                                                            }
                                                                            i4 = columnIndexOrThrow4;
                                                                            longSparseArray2 = longSparseArray3;
                                                                            folderIconPreviewVerifier = folderIconPreviewVerifier2;
                                                                            context = context3;
                                                                            hashMap = hashMap4;
                                                                            longSparseArray = longSparseArray5;
                                                                            if (restoredItemInfo == null) {
                                                                            }
                                                                            e = e23;
                                                                            hashMap7 = hashMap3;
                                                                            Log.e(TAG, "Desktop items loading interrupted", e);
                                                                            multiHashMap = multiHashMap2;
                                                                            hashMap5 = hashMap;
                                                                            context3 = context;
                                                                            hashMap6 = hashMap2;
                                                                            i9 = i5;
                                                                            packageManagerHelper3 = packageManagerHelper;
                                                                            columnIndexOrThrow = i;
                                                                            columnIndexOrThrow2 = i2;
                                                                            i11 = i6;
                                                                            columnIndexOrThrow3 = i3;
                                                                            columnIndexOrThrow4 = i4;
                                                                            longSparseArray8 = longSparseArray2;
                                                                            longSparseArray9 = longSparseArray;
                                                                            folderIconPreviewVerifier4 = folderIconPreviewVerifier;
                                                                        }
                                                                    }
                                                                    z3 = false;
                                                                    i3 = columnIndexOrThrow3;
                                                                    if (loaderCursor.restoreFlag == 0) {
                                                                    }
                                                                    i4 = columnIndexOrThrow4;
                                                                    longSparseArray2 = longSparseArray3;
                                                                    folderIconPreviewVerifier = folderIconPreviewVerifier2;
                                                                    context = context3;
                                                                    hashMap = hashMap4;
                                                                    longSparseArray = longSparseArray5;
                                                                    if (restoredItemInfo == null) {
                                                                    }
                                                                    e = e23;
                                                                    hashMap7 = hashMap3;
                                                                    Log.e(TAG, "Desktop items loading interrupted", e);
                                                                    multiHashMap = multiHashMap2;
                                                                    hashMap5 = hashMap;
                                                                    context3 = context;
                                                                    hashMap6 = hashMap2;
                                                                    i9 = i5;
                                                                    packageManagerHelper3 = packageManagerHelper;
                                                                    columnIndexOrThrow = i;
                                                                    columnIndexOrThrow2 = i2;
                                                                    i11 = i6;
                                                                    columnIndexOrThrow3 = i3;
                                                                    columnIndexOrThrow4 = i4;
                                                                    longSparseArray8 = longSparseArray2;
                                                                    longSparseArray9 = longSparseArray;
                                                                    folderIconPreviewVerifier4 = folderIconPreviewVerifier;
                                                                }
                                                            }
                                                            multiHashMap2 = multiHashMap;
                                                            z2 = false;
                                                            if ((loaderCursor.restoreFlag & 16) != 0) {
                                                            }
                                                            if (z8) {
                                                            }
                                                            if (loaderCursor.isOnWorkspaceOrHotseat()) {
                                                            }
                                                            z3 = false;
                                                            i3 = columnIndexOrThrow3;
                                                            if (loaderCursor.restoreFlag == 0) {
                                                            }
                                                            i4 = columnIndexOrThrow4;
                                                            longSparseArray2 = longSparseArray3;
                                                            folderIconPreviewVerifier = folderIconPreviewVerifier2;
                                                            context = context3;
                                                            hashMap = hashMap4;
                                                            longSparseArray = longSparseArray5;
                                                            if (restoredItemInfo == null) {
                                                            }
                                                            e = e23;
                                                            hashMap7 = hashMap3;
                                                            Log.e(TAG, "Desktop items loading interrupted", e);
                                                            multiHashMap = multiHashMap2;
                                                            hashMap5 = hashMap;
                                                            context3 = context;
                                                            hashMap6 = hashMap2;
                                                            i9 = i5;
                                                            packageManagerHelper3 = packageManagerHelper;
                                                            columnIndexOrThrow = i;
                                                            columnIndexOrThrow2 = i2;
                                                            i11 = i6;
                                                            columnIndexOrThrow3 = i3;
                                                            columnIndexOrThrow4 = i4;
                                                            longSparseArray8 = longSparseArray2;
                                                            longSparseArray9 = longSparseArray;
                                                            folderIconPreviewVerifier4 = folderIconPreviewVerifier;
                                                        } else {
                                                            i6 = i12;
                                                            loaderCursor.markDeleted("Unable to find a launch target");
                                                        }
                                                    } else {
                                                        i6 = i12;
                                                        packageManagerHelper = packageManagerHelper4;
                                                        loaderCursor.markDeleted("Invalid component removed: " + component);
                                                    }
                                                    i3 = columnIndexOrThrow3;
                                                    i4 = columnIndexOrThrow4;
                                                    i5 = i9;
                                                    multiHashMap2 = multiHashMap;
                                                    hashMap = hashMap4;
                                                    longSparseArray4 = longSparseArray5;
                                                    folderIconPreviewVerifier2 = folderIconPreviewVerifier3;
                                                }
                                            }
                                            i6 = i12;
                                            packageManagerHelper = packageManagerHelper4;
                                            if (!TextUtils.isEmpty(str)) {
                                                if (loaderCursor.restoreFlag == 0) {
                                                }
                                            }
                                            multiHashMap2 = multiHashMap;
                                            z2 = false;
                                            if ((loaderCursor.restoreFlag & 16) != 0) {
                                            }
                                            if (z8) {
                                            }
                                            if (loaderCursor.isOnWorkspaceOrHotseat()) {
                                            }
                                            z3 = false;
                                            i3 = columnIndexOrThrow3;
                                            if (loaderCursor.restoreFlag == 0) {
                                            }
                                            i4 = columnIndexOrThrow4;
                                            longSparseArray2 = longSparseArray3;
                                            folderIconPreviewVerifier = folderIconPreviewVerifier2;
                                            context = context3;
                                            hashMap = hashMap4;
                                            longSparseArray = longSparseArray5;
                                            if (restoredItemInfo == null) {
                                            }
                                            e = e23;
                                            hashMap7 = hashMap3;
                                            Log.e(TAG, "Desktop items loading interrupted", e);
                                            multiHashMap = multiHashMap2;
                                            hashMap5 = hashMap;
                                            context3 = context;
                                            hashMap6 = hashMap2;
                                            i9 = i5;
                                            packageManagerHelper3 = packageManagerHelper;
                                            columnIndexOrThrow = i;
                                            columnIndexOrThrow2 = i2;
                                            i11 = i6;
                                            columnIndexOrThrow3 = i3;
                                            columnIndexOrThrow4 = i4;
                                            longSparseArray8 = longSparseArray2;
                                            longSparseArray9 = longSparseArray;
                                            folderIconPreviewVerifier4 = folderIconPreviewVerifier;
                                        } else {
                                            loaderCursor.markDeleted("Only legacy shortcuts can have null package");
                                            i6 = i12;
                                            i3 = columnIndexOrThrow3;
                                            i4 = columnIndexOrThrow4;
                                            i5 = i9;
                                            multiHashMap2 = multiHashMap;
                                            hashMap = hashMap4;
                                            longSparseArray4 = longSparseArray5;
                                            folderIconPreviewVerifier2 = folderIconPreviewVerifier3;
                                            packageManagerHelper = packageManagerHelper4;
                                        }
                                    }
                                    break;
                                case 2:
                                    longSparseArray5 = longSparseArray9;
                                    i = columnIndexOrThrow;
                                    i2 = columnIndexOrThrow2;
                                    folderIconPreviewVerifier3 = folderIconPreviewVerifier4;
                                    longSparseArray6 = longSparseArray8;
                                    packageManagerHelper2 = packageManagerHelper3;
                                    hashMap3 = hashMap7;
                                    hashMap4 = hashMap5;
                                    hashMap2 = hashMap6;
                                    try {
                                        findOrMakeFolder = this.mBgDataModel.findOrMakeFolder(loaderCursor.id);
                                        loaderCursor.applyCommonProperties(findOrMakeFolder);
                                        findOrMakeFolder.title = loaderCursor.getString(loaderCursor.titleIndex);
                                        try {
                                            findOrMakeFolder.spanX = 1;
                                            findOrMakeFolder.spanY = 1;
                                            i8 = i11;
                                        } catch (Exception e26) {
                                            e = e26;
                                            i3 = columnIndexOrThrow3;
                                            i4 = columnIndexOrThrow4;
                                            context = context3;
                                            i5 = i9;
                                            multiHashMap2 = multiHashMap;
                                            i6 = i11;
                                            hashMap7 = hashMap3;
                                            hashMap = hashMap4;
                                            longSparseArray = longSparseArray5;
                                            folderIconPreviewVerifier = folderIconPreviewVerifier3;
                                            packageManagerHelper = packageManagerHelper2;
                                            longSparseArray2 = longSparseArray6;
                                        }
                                    } catch (Exception e27) {
                                        e = e27;
                                        i3 = columnIndexOrThrow3;
                                        i4 = columnIndexOrThrow4;
                                        context = context3;
                                        i5 = i9;
                                        multiHashMap2 = multiHashMap;
                                        i6 = i11;
                                    }
                                    try {
                                        findOrMakeFolder.options = loaderCursor.getInt(i8);
                                        loaderCursor.markRestored();
                                        loaderCursor.checkAndAddItem(findOrMakeFolder, this.mBgDataModel);
                                        i6 = i8;
                                        i3 = columnIndexOrThrow3;
                                        i4 = columnIndexOrThrow4;
                                        context = context3;
                                        i5 = i9;
                                        multiHashMap2 = multiHashMap;
                                        hashMap = hashMap4;
                                        longSparseArray = longSparseArray5;
                                        folderIconPreviewVerifier = folderIconPreviewVerifier3;
                                        packageManagerHelper = packageManagerHelper2;
                                        longSparseArray2 = longSparseArray6;
                                        hashMap7 = hashMap3;
                                    } catch (Exception e28) {
                                        e = e28;
                                        i6 = i8;
                                        i3 = columnIndexOrThrow3;
                                        i4 = columnIndexOrThrow4;
                                        context = context3;
                                        i5 = i9;
                                        multiHashMap2 = multiHashMap;
                                        hashMap7 = hashMap3;
                                        hashMap = hashMap4;
                                        longSparseArray = longSparseArray5;
                                        folderIconPreviewVerifier = folderIconPreviewVerifier3;
                                        packageManagerHelper = packageManagerHelper2;
                                        longSparseArray2 = longSparseArray6;
                                        Log.e(TAG, "Desktop items loading interrupted", e);
                                        multiHashMap = multiHashMap2;
                                        hashMap5 = hashMap;
                                        context3 = context;
                                        hashMap6 = hashMap2;
                                        i9 = i5;
                                        packageManagerHelper3 = packageManagerHelper;
                                        columnIndexOrThrow = i;
                                        columnIndexOrThrow2 = i2;
                                        i11 = i6;
                                        columnIndexOrThrow3 = i3;
                                        columnIndexOrThrow4 = i4;
                                        longSparseArray8 = longSparseArray2;
                                        longSparseArray9 = longSparseArray;
                                        folderIconPreviewVerifier4 = folderIconPreviewVerifier;
                                    }
                                    multiHashMap = multiHashMap2;
                                    hashMap5 = hashMap;
                                    context3 = context;
                                    hashMap6 = hashMap2;
                                    i9 = i5;
                                    packageManagerHelper3 = packageManagerHelper;
                                    columnIndexOrThrow = i;
                                    columnIndexOrThrow2 = i2;
                                    i11 = i6;
                                    columnIndexOrThrow3 = i3;
                                    columnIndexOrThrow4 = i4;
                                    longSparseArray8 = longSparseArray2;
                                    longSparseArray9 = longSparseArray;
                                    folderIconPreviewVerifier4 = folderIconPreviewVerifier;
                                case 3:
                                default:
                                    longSparseArray = longSparseArray9;
                                    i = columnIndexOrThrow;
                                    i2 = columnIndexOrThrow2;
                                    i3 = columnIndexOrThrow3;
                                    i4 = columnIndexOrThrow4;
                                    folderIconPreviewVerifier = folderIconPreviewVerifier4;
                                    longSparseArray2 = longSparseArray8;
                                    packageManagerHelper = packageManagerHelper3;
                                    hashMap3 = hashMap7;
                                    hashMap = hashMap5;
                                    hashMap2 = hashMap6;
                                    context = context3;
                                    i5 = i9;
                                    multiHashMap2 = multiHashMap;
                                    i6 = i11;
                                    hashMap7 = hashMap3;
                                    multiHashMap = multiHashMap2;
                                    hashMap5 = hashMap;
                                    context3 = context;
                                    hashMap6 = hashMap2;
                                    i9 = i5;
                                    packageManagerHelper3 = packageManagerHelper;
                                    columnIndexOrThrow = i;
                                    columnIndexOrThrow2 = i2;
                                    i11 = i6;
                                    columnIndexOrThrow3 = i3;
                                    columnIndexOrThrow4 = i4;
                                    longSparseArray8 = longSparseArray2;
                                    longSparseArray9 = longSparseArray;
                                    folderIconPreviewVerifier4 = folderIconPreviewVerifier;
                                    break;
                                case 4:
                                case 5:
                                    try {
                                        boolean z9 = loaderCursor.itemType == 5;
                                        int i13 = loaderCursor.getInt(columnIndexOrThrow);
                                        String string = loaderCursor.getString(columnIndexOrThrow2);
                                        i = columnIndexOrThrow;
                                        try {
                                            ComponentName unflattenFromString = ComponentName.unflattenFromString(string);
                                            i2 = columnIndexOrThrow2;
                                            try {
                                                boolean z10 = !loaderCursor.hasRestoreFlag(1);
                                                try {
                                                    boolean z11 = !loaderCursor.hasRestoreFlag(2);
                                                    if (hashMap7 == null) {
                                                        hashMap3 = hashMap7;
                                                        try {
                                                            hashMap7 = this.mAppWidgetManager.getAllProvidersMap();
                                                        } catch (Exception e29) {
                                                            e = e29;
                                                            longSparseArray = longSparseArray9;
                                                            i3 = columnIndexOrThrow3;
                                                            i4 = columnIndexOrThrow4;
                                                            folderIconPreviewVerifier = folderIconPreviewVerifier4;
                                                            longSparseArray2 = longSparseArray8;
                                                            packageManagerHelper = packageManagerHelper3;
                                                            hashMap = hashMap5;
                                                            hashMap2 = hashMap6;
                                                            context = context3;
                                                            i5 = i9;
                                                            multiHashMap2 = multiHashMap;
                                                            i6 = i11;
                                                            hashMap7 = hashMap3;
                                                            Log.e(TAG, "Desktop items loading interrupted", e);
                                                            multiHashMap = multiHashMap2;
                                                            hashMap5 = hashMap;
                                                            context3 = context;
                                                            hashMap6 = hashMap2;
                                                            i9 = i5;
                                                            packageManagerHelper3 = packageManagerHelper;
                                                            columnIndexOrThrow = i;
                                                            columnIndexOrThrow2 = i2;
                                                            i11 = i6;
                                                            columnIndexOrThrow3 = i3;
                                                            columnIndexOrThrow4 = i4;
                                                            longSparseArray8 = longSparseArray2;
                                                            longSparseArray9 = longSparseArray;
                                                            folderIconPreviewVerifier4 = folderIconPreviewVerifier;
                                                        }
                                                    }
                                                    hashMap4 = hashMap5;
                                                    try {
                                                        longSparseArray5 = longSparseArray9;
                                                        try {
                                                            folderIconPreviewVerifier3 = folderIconPreviewVerifier4;
                                                        } catch (Exception e30) {
                                                            e = e30;
                                                            folderIconPreviewVerifier3 = folderIconPreviewVerifier4;
                                                        }
                                                    } catch (Exception e31) {
                                                        e = e31;
                                                        folderIconPreviewVerifier3 = folderIconPreviewVerifier4;
                                                        hashMap2 = hashMap6;
                                                        longSparseArray = longSparseArray9;
                                                        i3 = columnIndexOrThrow3;
                                                        i4 = columnIndexOrThrow4;
                                                        longSparseArray2 = longSparseArray8;
                                                        packageManagerHelper = packageManagerHelper3;
                                                        context = context3;
                                                        i5 = i9;
                                                        multiHashMap2 = multiHashMap;
                                                        i6 = i11;
                                                        hashMap = hashMap4;
                                                        folderIconPreviewVerifier = folderIconPreviewVerifier3;
                                                        Log.e(TAG, "Desktop items loading interrupted", e);
                                                        multiHashMap = multiHashMap2;
                                                        hashMap5 = hashMap;
                                                        context3 = context;
                                                        hashMap6 = hashMap2;
                                                        i9 = i5;
                                                        packageManagerHelper3 = packageManagerHelper;
                                                        columnIndexOrThrow = i;
                                                        columnIndexOrThrow2 = i2;
                                                        i11 = i6;
                                                        columnIndexOrThrow3 = i3;
                                                        columnIndexOrThrow4 = i4;
                                                        longSparseArray8 = longSparseArray2;
                                                        longSparseArray9 = longSparseArray;
                                                        folderIconPreviewVerifier4 = folderIconPreviewVerifier;
                                                    }
                                                    try {
                                                        AppWidgetProviderInfo appWidgetProviderInfo = hashMap7.get(new ComponentKey(ComponentName.unflattenFromString(string), loaderCursor.user));
                                                        boolean isValidProvider = isValidProvider(appWidgetProviderInfo);
                                                        if (isSafeMode || z9 || !z11 || isValidProvider) {
                                                            if (isValidProvider) {
                                                                LauncherAppWidgetInfo launcherAppWidgetInfo2 = new LauncherAppWidgetInfo(i13, appWidgetProviderInfo.provider);
                                                                int i14 = loaderCursor.restoreFlag & (-9) & (-3);
                                                                if (!z11 && z10) {
                                                                    i14 |= 4;
                                                                }
                                                                launcherAppWidgetInfo2.restoreStatus = i14;
                                                                launcherAppWidgetInfo = launcherAppWidgetInfo2;
                                                                longSparseArray6 = longSparseArray8;
                                                                packageManagerHelper2 = packageManagerHelper3;
                                                                hashMap2 = hashMap6;
                                                            } else {
                                                                StringBuilder sb = new StringBuilder();
                                                                sb.append("Widget restore pending id=");
                                                                longSparseArray6 = longSparseArray8;
                                                                packageManagerHelper2 = packageManagerHelper3;
                                                                try {
                                                                    sb.append(loaderCursor.id);
                                                                    sb.append(" appWidgetId=");
                                                                    sb.append(i13);
                                                                    sb.append(" status =");
                                                                    sb.append(loaderCursor.restoreFlag);
                                                                    Log.v(TAG, sb.toString());
                                                                    launcherAppWidgetInfo = new LauncherAppWidgetInfo(i13, unflattenFromString);
                                                                    launcherAppWidgetInfo.restoreStatus = loaderCursor.restoreFlag;
                                                                    hashMap2 = hashMap6;
                                                                    PackageInstaller.SessionInfo sessionInfo2 = hashMap2.get(unflattenFromString.getPackageName());
                                                                    Integer valueOf = sessionInfo2 == null ? null : Integer.valueOf((int) (sessionInfo2.getProgress() * 100.0f));
                                                                    if (!loaderCursor.hasRestoreFlag(8)) {
                                                                        if (valueOf != null) {
                                                                            launcherAppWidgetInfo.restoreStatus |= 8;
                                                                        } else if (!isSafeMode) {
                                                                            loaderCursor.markDeleted("Unrestored widget removed: " + unflattenFromString);
                                                                            hashMap6 = hashMap2;
                                                                            columnIndexOrThrow = i;
                                                                            columnIndexOrThrow2 = i2;
                                                                            hashMap5 = hashMap4;
                                                                            longSparseArray9 = longSparseArray5;
                                                                            folderIconPreviewVerifier4 = folderIconPreviewVerifier3;
                                                                            packageManagerHelper3 = packageManagerHelper2;
                                                                            longSparseArray8 = longSparseArray6;
                                                                        }
                                                                    }
                                                                    launcherAppWidgetInfo.installProgress = valueOf == null ? 0 : valueOf.intValue();
                                                                } catch (Exception e32) {
                                                                    e = e32;
                                                                    hashMap2 = hashMap6;
                                                                    i3 = columnIndexOrThrow3;
                                                                    i4 = columnIndexOrThrow4;
                                                                    context = context3;
                                                                    i5 = i9;
                                                                    multiHashMap2 = multiHashMap;
                                                                    i6 = i11;
                                                                    hashMap = hashMap4;
                                                                    longSparseArray = longSparseArray5;
                                                                    folderIconPreviewVerifier = folderIconPreviewVerifier3;
                                                                    packageManagerHelper = packageManagerHelper2;
                                                                    longSparseArray2 = longSparseArray6;
                                                                    Log.e(TAG, "Desktop items loading interrupted", e);
                                                                    multiHashMap = multiHashMap2;
                                                                    hashMap5 = hashMap;
                                                                    context3 = context;
                                                                    hashMap6 = hashMap2;
                                                                    i9 = i5;
                                                                    packageManagerHelper3 = packageManagerHelper;
                                                                    columnIndexOrThrow = i;
                                                                    columnIndexOrThrow2 = i2;
                                                                    i11 = i6;
                                                                    columnIndexOrThrow3 = i3;
                                                                    columnIndexOrThrow4 = i4;
                                                                    longSparseArray8 = longSparseArray2;
                                                                    longSparseArray9 = longSparseArray;
                                                                    folderIconPreviewVerifier4 = folderIconPreviewVerifier;
                                                                }
                                                            }
                                                            if (launcherAppWidgetInfo.hasRestoreFlag(32)) {
                                                                try {
                                                                    launcherAppWidgetInfo.bindOptions = loaderCursor.parseIntent();
                                                                } catch (Exception e33) {
                                                                    e = e33;
                                                                    i3 = columnIndexOrThrow3;
                                                                    i4 = columnIndexOrThrow4;
                                                                    context = context3;
                                                                    i5 = i9;
                                                                    multiHashMap2 = multiHashMap;
                                                                    i6 = i11;
                                                                    hashMap = hashMap4;
                                                                    longSparseArray = longSparseArray5;
                                                                    folderIconPreviewVerifier = folderIconPreviewVerifier3;
                                                                    packageManagerHelper = packageManagerHelper2;
                                                                    longSparseArray2 = longSparseArray6;
                                                                    Log.e(TAG, "Desktop items loading interrupted", e);
                                                                    multiHashMap = multiHashMap2;
                                                                    hashMap5 = hashMap;
                                                                    context3 = context;
                                                                    hashMap6 = hashMap2;
                                                                    i9 = i5;
                                                                    packageManagerHelper3 = packageManagerHelper;
                                                                    columnIndexOrThrow = i;
                                                                    columnIndexOrThrow2 = i2;
                                                                    i11 = i6;
                                                                    columnIndexOrThrow3 = i3;
                                                                    columnIndexOrThrow4 = i4;
                                                                    longSparseArray8 = longSparseArray2;
                                                                    longSparseArray9 = longSparseArray;
                                                                    folderIconPreviewVerifier4 = folderIconPreviewVerifier;
                                                                }
                                                            }
                                                            loaderCursor.applyCommonProperties(launcherAppWidgetInfo);
                                                            launcherAppWidgetInfo.spanX = loaderCursor.getInt(columnIndexOrThrow3);
                                                            launcherAppWidgetInfo.spanY = loaderCursor.getInt(columnIndexOrThrow4);
                                                            launcherAppWidgetInfo.user = loaderCursor.user;
                                                            if (loaderCursor.isOnWorkspaceOrHotseat()) {
                                                                if (!z9) {
                                                                    String flattenToString = launcherAppWidgetInfo.providerName.flattenToString();
                                                                    if (!flattenToString.equals(string) || launcherAppWidgetInfo.restoreStatus != loaderCursor.restoreFlag) {
                                                                        loaderCursor.updater().put(LauncherSettings.Favorites.APPWIDGET_PROVIDER, flattenToString).put(LauncherSettings.Favorites.RESTORED, Integer.valueOf(launcherAppWidgetInfo.restoreStatus)).commit();
                                                                    }
                                                                }
                                                                if (launcherAppWidgetInfo.restoreStatus != 0) {
                                                                    launcherAppWidgetInfo.pendingItemInfo = new PackageItemInfo(launcherAppWidgetInfo.providerName.getPackageName());
                                                                    launcherAppWidgetInfo.pendingItemInfo.user = launcherAppWidgetInfo.user;
                                                                    this.mIconCache.getTitleAndIconForApp(launcherAppWidgetInfo.pendingItemInfo, false);
                                                                }
                                                                loaderCursor.checkAndAddItem(launcherAppWidgetInfo, this.mBgDataModel);
                                                            } else {
                                                                loaderCursor.markDeleted("Widget found where container != CONTAINER_DESKTOP nor CONTAINER_HOTSEAT - ignoring!");
                                                                hashMap6 = hashMap2;
                                                                columnIndexOrThrow = i;
                                                                columnIndexOrThrow2 = i2;
                                                                hashMap5 = hashMap4;
                                                                longSparseArray9 = longSparseArray5;
                                                                folderIconPreviewVerifier4 = folderIconPreviewVerifier3;
                                                                packageManagerHelper3 = packageManagerHelper2;
                                                                longSparseArray8 = longSparseArray6;
                                                            }
                                                        } else {
                                                            try {
                                                                loaderCursor.markDeleted("Deleting widget that isn't installed anymore: " + appWidgetProviderInfo);
                                                                longSparseArray6 = longSparseArray8;
                                                                packageManagerHelper2 = packageManagerHelper3;
                                                                hashMap2 = hashMap6;
                                                            } catch (Exception e34) {
                                                                e = e34;
                                                                i3 = columnIndexOrThrow3;
                                                                i4 = columnIndexOrThrow4;
                                                                longSparseArray2 = longSparseArray8;
                                                                packageManagerHelper = packageManagerHelper3;
                                                                hashMap2 = hashMap6;
                                                                context = context3;
                                                                i5 = i9;
                                                                multiHashMap2 = multiHashMap;
                                                                i6 = i11;
                                                                hashMap = hashMap4;
                                                                longSparseArray = longSparseArray5;
                                                                folderIconPreviewVerifier = folderIconPreviewVerifier3;
                                                                Log.e(TAG, "Desktop items loading interrupted", e);
                                                                multiHashMap = multiHashMap2;
                                                                hashMap5 = hashMap;
                                                                context3 = context;
                                                                hashMap6 = hashMap2;
                                                                i9 = i5;
                                                                packageManagerHelper3 = packageManagerHelper;
                                                                columnIndexOrThrow = i;
                                                                columnIndexOrThrow2 = i2;
                                                                i11 = i6;
                                                                columnIndexOrThrow3 = i3;
                                                                columnIndexOrThrow4 = i4;
                                                                longSparseArray8 = longSparseArray2;
                                                                longSparseArray9 = longSparseArray;
                                                                folderIconPreviewVerifier4 = folderIconPreviewVerifier;
                                                            }
                                                        }
                                                        i3 = columnIndexOrThrow3;
                                                        i4 = columnIndexOrThrow4;
                                                        context = context3;
                                                        i5 = i9;
                                                        multiHashMap2 = multiHashMap;
                                                        i6 = i11;
                                                        hashMap = hashMap4;
                                                        longSparseArray = longSparseArray5;
                                                        folderIconPreviewVerifier = folderIconPreviewVerifier3;
                                                        packageManagerHelper = packageManagerHelper2;
                                                        longSparseArray2 = longSparseArray6;
                                                    } catch (Exception e35) {
                                                        e = e35;
                                                        hashMap2 = hashMap6;
                                                        i3 = columnIndexOrThrow3;
                                                        i4 = columnIndexOrThrow4;
                                                        longSparseArray2 = longSparseArray8;
                                                        packageManagerHelper = packageManagerHelper3;
                                                        context = context3;
                                                        i5 = i9;
                                                        multiHashMap2 = multiHashMap;
                                                        i6 = i11;
                                                        hashMap = hashMap4;
                                                        longSparseArray = longSparseArray5;
                                                        folderIconPreviewVerifier = folderIconPreviewVerifier3;
                                                        Log.e(TAG, "Desktop items loading interrupted", e);
                                                        multiHashMap = multiHashMap2;
                                                        hashMap5 = hashMap;
                                                        context3 = context;
                                                        hashMap6 = hashMap2;
                                                        i9 = i5;
                                                        packageManagerHelper3 = packageManagerHelper;
                                                        columnIndexOrThrow = i;
                                                        columnIndexOrThrow2 = i2;
                                                        i11 = i6;
                                                        columnIndexOrThrow3 = i3;
                                                        columnIndexOrThrow4 = i4;
                                                        longSparseArray8 = longSparseArray2;
                                                        longSparseArray9 = longSparseArray;
                                                        folderIconPreviewVerifier4 = folderIconPreviewVerifier;
                                                    }
                                                } catch (Exception e36) {
                                                    e = e36;
                                                    folderIconPreviewVerifier3 = folderIconPreviewVerifier4;
                                                    hashMap2 = hashMap6;
                                                    longSparseArray = longSparseArray9;
                                                    i3 = columnIndexOrThrow3;
                                                    i4 = columnIndexOrThrow4;
                                                    longSparseArray2 = longSparseArray8;
                                                    packageManagerHelper = packageManagerHelper3;
                                                    hashMap = hashMap5;
                                                    context = context3;
                                                    i5 = i9;
                                                    multiHashMap2 = multiHashMap;
                                                    i6 = i11;
                                                    folderIconPreviewVerifier = folderIconPreviewVerifier3;
                                                    Log.e(TAG, "Desktop items loading interrupted", e);
                                                    multiHashMap = multiHashMap2;
                                                    hashMap5 = hashMap;
                                                    context3 = context;
                                                    hashMap6 = hashMap2;
                                                    i9 = i5;
                                                    packageManagerHelper3 = packageManagerHelper;
                                                    columnIndexOrThrow = i;
                                                    columnIndexOrThrow2 = i2;
                                                    i11 = i6;
                                                    columnIndexOrThrow3 = i3;
                                                    columnIndexOrThrow4 = i4;
                                                    longSparseArray8 = longSparseArray2;
                                                    longSparseArray9 = longSparseArray;
                                                    folderIconPreviewVerifier4 = folderIconPreviewVerifier;
                                                }
                                            } catch (Exception e37) {
                                                e = e37;
                                                FolderIconPreviewVerifier folderIconPreviewVerifier5 = folderIconPreviewVerifier4;
                                                hashMap2 = hashMap6;
                                                longSparseArray = longSparseArray9;
                                                i3 = columnIndexOrThrow3;
                                                i4 = columnIndexOrThrow4;
                                                longSparseArray2 = longSparseArray8;
                                                packageManagerHelper = packageManagerHelper3;
                                                hashMap = hashMap5;
                                                context = context3;
                                                i5 = i9;
                                                multiHashMap2 = multiHashMap;
                                                i6 = i11;
                                                folderIconPreviewVerifier = folderIconPreviewVerifier5;
                                            }
                                        } catch (Exception e38) {
                                            e = e38;
                                            i2 = columnIndexOrThrow2;
                                            folderIconPreviewVerifier3 = folderIconPreviewVerifier4;
                                            hashMap2 = hashMap6;
                                            longSparseArray = longSparseArray9;
                                            i3 = columnIndexOrThrow3;
                                            i4 = columnIndexOrThrow4;
                                            longSparseArray2 = longSparseArray8;
                                            packageManagerHelper = packageManagerHelper3;
                                            hashMap = hashMap5;
                                            context = context3;
                                            i5 = i9;
                                            multiHashMap2 = multiHashMap;
                                            i6 = i11;
                                            folderIconPreviewVerifier = folderIconPreviewVerifier3;
                                            Log.e(TAG, "Desktop items loading interrupted", e);
                                            multiHashMap = multiHashMap2;
                                            hashMap5 = hashMap;
                                            context3 = context;
                                            hashMap6 = hashMap2;
                                            i9 = i5;
                                            packageManagerHelper3 = packageManagerHelper;
                                            columnIndexOrThrow = i;
                                            columnIndexOrThrow2 = i2;
                                            i11 = i6;
                                            columnIndexOrThrow3 = i3;
                                            columnIndexOrThrow4 = i4;
                                            longSparseArray8 = longSparseArray2;
                                            longSparseArray9 = longSparseArray;
                                            folderIconPreviewVerifier4 = folderIconPreviewVerifier;
                                        }
                                    } catch (Exception e39) {
                                        e = e39;
                                        i = columnIndexOrThrow;
                                    }
                                    multiHashMap = multiHashMap2;
                                    hashMap5 = hashMap;
                                    context3 = context;
                                    hashMap6 = hashMap2;
                                    i9 = i5;
                                    packageManagerHelper3 = packageManagerHelper;
                                    columnIndexOrThrow = i;
                                    columnIndexOrThrow2 = i2;
                                    i11 = i6;
                                    columnIndexOrThrow3 = i3;
                                    columnIndexOrThrow4 = i4;
                                    longSparseArray8 = longSparseArray2;
                                    longSparseArray9 = longSparseArray;
                                    folderIconPreviewVerifier4 = folderIconPreviewVerifier;
                                    break;
                            }
                        } else {
                            try {
                                loaderCursor.markDeleted("User has been deleted");
                                i = columnIndexOrThrow;
                                i2 = columnIndexOrThrow2;
                                i3 = columnIndexOrThrow3;
                                i4 = columnIndexOrThrow4;
                                longSparseArray3 = longSparseArray8;
                                packageManagerHelper = packageManagerHelper3;
                                hashMap3 = hashMap7;
                                hashMap = hashMap5;
                                i5 = i9;
                                multiHashMap2 = multiHashMap;
                                i6 = i11;
                                longSparseArray4 = longSparseArray9;
                                folderIconPreviewVerifier2 = folderIconPreviewVerifier4;
                                hashMap2 = hashMap6;
                            } catch (Exception e40) {
                                e = e40;
                                longSparseArray = longSparseArray9;
                                i = columnIndexOrThrow;
                                i2 = columnIndexOrThrow2;
                                i3 = columnIndexOrThrow3;
                                i4 = columnIndexOrThrow4;
                                folderIconPreviewVerifier = folderIconPreviewVerifier4;
                                longSparseArray2 = longSparseArray8;
                                packageManagerHelper = packageManagerHelper3;
                                hashMap = hashMap5;
                                hashMap2 = hashMap6;
                                context = context3;
                                i5 = i9;
                                multiHashMap2 = multiHashMap;
                                i6 = i11;
                                Log.e(TAG, "Desktop items loading interrupted", e);
                                multiHashMap = multiHashMap2;
                                hashMap5 = hashMap;
                                context3 = context;
                                hashMap6 = hashMap2;
                                i9 = i5;
                                packageManagerHelper3 = packageManagerHelper;
                                columnIndexOrThrow = i;
                                columnIndexOrThrow2 = i2;
                                i11 = i6;
                                columnIndexOrThrow3 = i3;
                                columnIndexOrThrow4 = i4;
                                longSparseArray8 = longSparseArray2;
                                longSparseArray9 = longSparseArray;
                                folderIconPreviewVerifier4 = folderIconPreviewVerifier;
                            }
                        }
                        multiHashMap = multiHashMap2;
                        hashMap6 = hashMap2;
                        longSparseArray8 = longSparseArray3;
                        i9 = i5;
                        packageManagerHelper3 = packageManagerHelper;
                        longSparseArray9 = longSparseArray4;
                        folderIconPreviewVerifier4 = folderIconPreviewVerifier2;
                        columnIndexOrThrow = i;
                        columnIndexOrThrow2 = i2;
                        hashMap7 = hashMap3;
                        i11 = i6;
                        columnIndexOrThrow4 = i4;
                        hashMap5 = hashMap;
                        columnIndexOrThrow3 = i3;
                    }
                    HashMap hashMap8 = hashMap5;
                    Context context4 = context3;
                    MultiHashMap multiHashMap4 = multiHashMap;
                    Utilities.closeSilently(loaderCursor);
                    if (this.mStopped) {
                        this.mBgDataModel.clear();
                        return;
                    }
                    if (loaderCursor.commitDeleted()) {
                        Iterator it3 = ((ArrayList) LauncherSettings.Settings.call(contentResolver, LauncherSettings.Settings.METHOD_DELETE_EMPTY_FOLDERS).getSerializable(LauncherSettings.Settings.EXTRA_VALUE)).iterator();
                        while (it3.hasNext()) {
                            long longValue = ((Long) it3.next()).longValue();
                            this.mBgDataModel.workspaceItems.remove(this.mBgDataModel.folders.get(longValue));
                            this.mBgDataModel.folders.remove(longValue);
                            this.mBgDataModel.itemsIdMap.remove(longValue);
                        }
                        LauncherSettings.Settings.call(contentResolver, LauncherSettings.Settings.METHOD_REMOVE_GHOST_WIDGETS);
                    }
                    HashSet<ShortcutKey> pendingShortcuts = InstallShortcutReceiver.getPendingShortcuts(context4);
                    for (ShortcutKey shortcutKey : hashMap8.keySet()) {
                        MutableInt mutableInt = this.mBgDataModel.pinnedShortcutCounts.get(shortcutKey);
                        if ((mutableInt == null || mutableInt.value == 0) && !pendingShortcuts.contains(shortcutKey)) {
                            this.mShortcutManager.unpinShortcut(shortcutKey);
                        }
                    }
                    FolderIconPreviewVerifier folderIconPreviewVerifier6 = new FolderIconPreviewVerifier(this.mApp.getInvariantDeviceProfile());
                    Iterator<FolderInfo> it4 = this.mBgDataModel.folders.iterator();
                    while (it4.hasNext()) {
                        FolderInfo next2 = it4.next();
                        Collections.sort(next2.contents, Folder.ITEM_POS_COMPARATOR);
                        folderIconPreviewVerifier6.setFolderInfo(next2);
                        Iterator<ShortcutInfo> it5 = next2.contents.iterator();
                        int i15 = 0;
                        while (it5.hasNext()) {
                            ShortcutInfo next3 = it5.next();
                            if (next3.usingLowResIcon && next3.itemType == 0 && folderIconPreviewVerifier6.isItemInPreview(next3.rank)) {
                                this.mIconCache.getTitleAndIcon(next3, false);
                                i15++;
                            }
                            if (i15 >= 4) {
                                break;
                            }
                        }
                    }
                    loaderCursor.commitRestoredItems();
                    if (!z7 && !multiHashMap4.isEmpty()) {
                        context4.registerReceiver(new SdCardAvailableReceiver(this.mApp, multiHashMap4), new IntentFilter("android.intent.action.BOOT_COMPLETED"), null, new Handler(LauncherModel.getWorkerLooper()));
                    }
                    ArrayList arrayList = new ArrayList(this.mBgDataModel.workspaceScreens);
                    Iterator<ItemInfo> it6 = this.mBgDataModel.itemsIdMap.iterator();
                    while (it6.hasNext()) {
                        ItemInfo next4 = it6.next();
                        long j = next4.screenId;
                        if (next4.container == -100 && arrayList.contains(Long.valueOf(j))) {
                            arrayList.remove(Long.valueOf(j));
                        }
                    }
                    if (arrayList.size() != 0) {
                        this.mBgDataModel.workspaceScreens.removeAll(arrayList);
                        LauncherModel.updateWorkspaceScreenOrder(context4, this.mBgDataModel.workspaceScreens);
                    }
                } catch (Throwable th) {
                    th = th;
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
            }
        }
    }

    private void updateIconCache() {
        HashSet hashSet = new HashSet();
        synchronized (this.mBgDataModel) {
            Iterator<ItemInfo> it = this.mBgDataModel.itemsIdMap.iterator();
            while (it.hasNext()) {
                ItemInfo next = it.next();
                if (next instanceof ShortcutInfo) {
                    ShortcutInfo shortcutInfo = (ShortcutInfo) next;
                    if (shortcutInfo.isPromise() && shortcutInfo.getTargetComponent() != null) {
                        hashSet.add(shortcutInfo.getTargetComponent().getPackageName());
                    }
                } else if (next instanceof LauncherAppWidgetInfo) {
                    LauncherAppWidgetInfo launcherAppWidgetInfo = (LauncherAppWidgetInfo) next;
                    if (launcherAppWidgetInfo.hasRestoreFlag(2)) {
                        hashSet.add(launcherAppWidgetInfo.providerName.getPackageName());
                    }
                }
            }
        }
        this.mIconCache.updateDbIcons(hashSet);
    }

    private void loadAllApps() {
        List<UserHandle> userProfiles = this.mUserManager.getUserProfiles();
        this.mBgAllAppsList.clear();
        for (UserHandle userHandle : userProfiles) {
            List<LauncherActivityInfo> activityList = this.mLauncherApps.getActivityList(null, userHandle);
            if (activityList == null || activityList.isEmpty()) {
                return;
            }
            boolean isQuietModeEnabled = this.mUserManager.isQuietModeEnabled(userHandle);
            for (int i = 0; i < activityList.size(); i++) {
                LauncherActivityInfo launcherActivityInfo = activityList.get(i);
                this.mBgAllAppsList.add(new AppInfo(launcherActivityInfo, userHandle, isQuietModeEnabled), launcherActivityInfo);
            }
        }
        this.mBgAllAppsList.added = new ArrayList<>();
    }

    private void loadDeepShortcuts() {
        this.mBgDataModel.deepShortcutMap.clear();
        this.mBgDataModel.hasShortcutHostPermission = this.mShortcutManager.hasHostPermission();
        if (this.mBgDataModel.hasShortcutHostPermission) {
            for (UserHandle userHandle : this.mUserManager.getUserProfiles()) {
                if (this.mUserManager.isUserUnlocked(userHandle)) {
                    this.mBgDataModel.updateDeepShortcutMap(null, userHandle, this.mShortcutManager.queryForAllShortcuts(userHandle));
                }
            }
        }
    }

    public static boolean isValidProvider(AppWidgetProviderInfo appWidgetProviderInfo) {
        return (appWidgetProviderInfo == null || appWidgetProviderInfo.provider == null || appWidgetProviderInfo.provider.getPackageName() == null) ? false : true;
    }
}
