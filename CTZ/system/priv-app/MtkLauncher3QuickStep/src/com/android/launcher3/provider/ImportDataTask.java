package com.android.launcher3.provider;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ProviderInfo;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Process;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.LongSparseArray;
import com.android.launcher3.AutoInstallsLayout;
import com.android.launcher3.DefaultLayoutParser;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherProvider;
import com.android.launcher3.LauncherSettings;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.logging.FileLog;
import com.android.launcher3.model.GridSizeMigrationTask;
import com.android.launcher3.util.LongArrayMap;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
/* loaded from: classes.dex */
public class ImportDataTask {
    private static final int BATCH_INSERT_SIZE = 15;
    public static final String KEY_DATA_IMPORT_SRC_AUTHORITY = "data_import_src_authority";
    public static final String KEY_DATA_IMPORT_SRC_PKG = "data_import_src_pkg";
    private static final int MIN_ITEM_COUNT_FOR_SUCCESSFUL_MIGRATION = 6;
    private static final String TAG = "ImportDataTask";
    private final Context mContext;
    private int mHotseatSize;
    private int mMaxGridSizeX;
    private int mMaxGridSizeY;
    private final Uri mOtherFavoritesUri;
    private final Uri mOtherScreensUri;

    private ImportDataTask(Context context, String str) {
        this.mContext = context;
        this.mOtherScreensUri = Uri.parse("content://" + str + "/" + LauncherSettings.WorkspaceScreens.TABLE_NAME);
        this.mOtherFavoritesUri = Uri.parse("content://" + str + "/" + LauncherSettings.Favorites.TABLE_NAME);
    }

    public boolean importWorkspace() throws Exception {
        ArrayList<Long> screenIdsFromCursor = LauncherDbUtils.getScreenIdsFromCursor(this.mContext.getContentResolver().query(this.mOtherScreensUri, null, null, null, LauncherSettings.WorkspaceScreens.SCREEN_RANK));
        FileLog.d(TAG, "Importing DB from " + this.mOtherFavoritesUri);
        if (screenIdsFromCursor.isEmpty()) {
            FileLog.e(TAG, "No data found to import");
            return false;
        }
        this.mMaxGridSizeY = 0;
        this.mMaxGridSizeX = 0;
        this.mHotseatSize = 0;
        ArrayList<ContentProviderOperation> arrayList = new ArrayList<>();
        int size = screenIdsFromCursor.size();
        LongSparseArray<Long> longSparseArray = new LongSparseArray<>(size);
        for (int i = 0; i < size; i++) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("_id", Integer.valueOf(i));
            contentValues.put(LauncherSettings.WorkspaceScreens.SCREEN_RANK, Integer.valueOf(i));
            longSparseArray.put(screenIdsFromCursor.get(i).longValue(), Long.valueOf(i));
            arrayList.add(ContentProviderOperation.newInsert(LauncherSettings.WorkspaceScreens.CONTENT_URI).withValues(contentValues).build());
        }
        this.mContext.getContentResolver().applyBatch(LauncherProvider.AUTHORITY, arrayList);
        importWorkspaceItems(screenIdsFromCursor.get(0).longValue(), longSparseArray);
        GridSizeMigrationTask.markForMigration(this.mContext, this.mMaxGridSizeX, this.mMaxGridSizeY, this.mHotseatSize);
        LauncherSettings.Settings.call(this.mContext.getContentResolver(), LauncherSettings.Settings.METHOD_CLEAR_EMPTY_DB_FLAG);
        return true;
    }

    /*  JADX ERROR: JadxRuntimeException in pass: BlockProcessor
        jadx.core.utils.exceptions.JadxRuntimeException: Found unreachable blocks
        	at jadx.core.dex.visitors.blocks.DominatorTree.sortBlocks(DominatorTree.java:35)
        	at jadx.core.dex.visitors.blocks.DominatorTree.compute(DominatorTree.java:25)
        	at jadx.core.dex.visitors.blocks.BlockProcessor.computeDominators(BlockProcessor.java:202)
        	at jadx.core.dex.visitors.blocks.BlockProcessor.processBlocksTree(BlockProcessor.java:45)
        	at jadx.core.dex.visitors.blocks.BlockProcessor.visit(BlockProcessor.java:39)
        */
    private void importWorkspaceItems(long r47, android.util.LongSparseArray<java.lang.Long> r49) throws java.lang.Exception {
        /*
            Method dump skipped, instructions count: 1046
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.launcher3.provider.ImportDataTask.importWorkspaceItems(long, android.util.LongSparseArray):void");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static String getPackage(Intent intent) {
        return intent.getComponent() != null ? intent.getComponent().getPackageName() : intent.getPackage();
    }

    public static boolean performImportIfPossible(Context context) throws Exception {
        SharedPreferences devicePrefs = Utilities.getDevicePrefs(context);
        String string = devicePrefs.getString(KEY_DATA_IMPORT_SRC_PKG, "");
        String string2 = devicePrefs.getString(KEY_DATA_IMPORT_SRC_AUTHORITY, "");
        if (TextUtils.isEmpty(string) || TextUtils.isEmpty(string2)) {
            return false;
        }
        devicePrefs.edit().remove(KEY_DATA_IMPORT_SRC_PKG).remove(KEY_DATA_IMPORT_SRC_AUTHORITY).commit();
        if (LauncherSettings.Settings.call(context.getContentResolver(), LauncherSettings.Settings.METHOD_WAS_EMPTY_DB_CREATED).getBoolean(LauncherSettings.Settings.EXTRA_VALUE, false)) {
            for (ProviderInfo providerInfo : context.getPackageManager().queryContentProviders(null, context.getApplicationInfo().uid, 0)) {
                if (string.equals(providerInfo.packageName)) {
                    if ((providerInfo.applicationInfo.flags & 1) == 0) {
                        return false;
                    }
                    if (string2.equals(providerInfo.authority) && (TextUtils.isEmpty(providerInfo.readPermission) || context.checkPermission(providerInfo.readPermission, Process.myPid(), Process.myUid()) == 0)) {
                        return new ImportDataTask(context, string2).importWorkspace();
                    }
                }
            }
            return false;
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int getMyHotseatLayoutId(Context context) {
        if (LauncherAppState.getIDP(context).numHotseatIcons <= 5) {
            return R.xml.dw_phone_hotseat;
        }
        return R.xml.dw_tablet_hotseat;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class HotseatLayoutParser extends DefaultLayoutParser {
        public HotseatLayoutParser(Context context, AutoInstallsLayout.LayoutParserCallback layoutParserCallback) {
            super(context, null, layoutParserCallback, context.getResources(), ImportDataTask.getMyHotseatLayoutId(context));
        }

        @Override // com.android.launcher3.DefaultLayoutParser, com.android.launcher3.AutoInstallsLayout
        protected ArrayMap<String, AutoInstallsLayout.TagParser> getLayoutElementsMap() {
            ArrayMap<String, AutoInstallsLayout.TagParser> arrayMap = new ArrayMap<>();
            arrayMap.put("favorite", new DefaultLayoutParser.AppShortcutWithUriParser());
            arrayMap.put("shortcut", new DefaultLayoutParser.UriShortcutParser(this.mSourceRes));
            arrayMap.put("resolve", new DefaultLayoutParser.ResolveParser());
            return arrayMap;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class HotseatParserCallback implements AutoInstallsLayout.LayoutParserCallback {
        private final HashSet<String> mExistingApps;
        private final LongArrayMap<Object> mExistingItems;
        private final ArrayList<ContentProviderOperation> mOutOps;
        private final int mRequiredSize;
        private int mStartItemId;

        HotseatParserCallback(HashSet<String> hashSet, LongArrayMap<Object> longArrayMap, ArrayList<ContentProviderOperation> arrayList, int i, int i2) {
            this.mExistingApps = hashSet;
            this.mExistingItems = longArrayMap;
            this.mOutOps = arrayList;
            this.mRequiredSize = i2;
            this.mStartItemId = i;
        }

        @Override // com.android.launcher3.AutoInstallsLayout.LayoutParserCallback
        public long generateNewItemId() {
            int i = this.mStartItemId;
            this.mStartItemId = i + 1;
            return i;
        }

        @Override // com.android.launcher3.AutoInstallsLayout.LayoutParserCallback
        public long insertAndCheck(SQLiteDatabase sQLiteDatabase, ContentValues contentValues) {
            if (this.mExistingItems.size() >= this.mRequiredSize) {
                return 0L;
            }
            try {
                Intent parseUri = Intent.parseUri(contentValues.getAsString(LauncherSettings.BaseLauncherColumns.INTENT), 0);
                String str = ImportDataTask.getPackage(parseUri);
                if (str == null || this.mExistingApps.contains(str)) {
                    return 0L;
                }
                this.mExistingApps.add(str);
                long j = 0;
                while (this.mExistingItems.get(j) != null) {
                    j++;
                }
                this.mExistingItems.put(j, parseUri);
                contentValues.put(LauncherSettings.Favorites.SCREEN, Long.valueOf(j));
                this.mOutOps.add(ContentProviderOperation.newInsert(LauncherSettings.Favorites.CONTENT_URI).withValues(contentValues).build());
                return 0L;
            } catch (URISyntaxException e) {
                return 0L;
            }
        }
    }
}
