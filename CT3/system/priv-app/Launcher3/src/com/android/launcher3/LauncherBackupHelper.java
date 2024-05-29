package com.android.launcher3;

import android.app.backup.BackupDataInputStream;
import android.app.backup.BackupDataOutput;
import android.app.backup.BackupHelper;
import android.app.backup.BackupManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.android.launcher3.backup.nano.BackupProtos$CheckedMessage;
import com.android.launcher3.backup.nano.BackupProtos$DeviceProfieData;
import com.android.launcher3.backup.nano.BackupProtos$Favorite;
import com.android.launcher3.backup.nano.BackupProtos$Journal;
import com.android.launcher3.backup.nano.BackupProtos$Key;
import com.android.launcher3.backup.nano.BackupProtos$Resource;
import com.android.launcher3.backup.nano.BackupProtos$Screen;
import com.android.launcher3.backup.nano.BackupProtos$Widget;
import com.android.launcher3.compat.AppWidgetManagerCompat;
import com.android.launcher3.compat.UserHandleCompat;
import com.android.launcher3.compat.UserManagerCompat;
import com.android.launcher3.model.GridSizeMigrationTask;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.protobuf.nano.MessageNano;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.zip.CRC32;
import org.xmlpull.v1.XmlPullParserException;
/* loaded from: a.zip:com/android/launcher3/LauncherBackupHelper.class */
public class LauncherBackupHelper implements BackupHelper {
    private static final String[] FAVORITE_PROJECTION = {"_id", "modified", "intent", "appWidgetProvider", "appWidgetId", "cellX", "cellY", "container", "icon", "iconPackage", "iconResource", "iconType", "itemType", "screen", "spanX", "spanY", "title", "profileId", "rank"};
    private static final String[] SCREEN_PROJECTION = {"_id", "modified", "screenRank"};
    private boolean mBackupDataWasUpdated;
    private BackupManager mBackupManager;
    final Context mContext;
    private BackupProtos$DeviceProfieData mDeviceProfileData;
    private IconCache mIconCache;
    private InvariantDeviceProfile mIdp;
    private long mLastBackupRestoreTime;
    private final long mUserSerial;
    BackupProtos$DeviceProfieData migrationCompatibleProfileData;
    private byte[] mBuffer = new byte[512];
    HashSet<String> widgetSizes = new HashSet<>();
    int restoredBackupVersion = 1;
    private int mHotseatShift = 0;
    private final HashSet<String> mExistingKeys = new HashSet<>();
    private final ArrayList<BackupProtos$Key> mKeys = new ArrayList<>();
    boolean restoreSuccessful = true;
    private final ItemTypeMatcher[] mItemTypeMatchers = new ItemTypeMatcher[7];

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:com/android/launcher3/LauncherBackupHelper$InvalidBackupException.class */
    public class InvalidBackupException extends IOException {
        private static final long serialVersionUID = 8931456637211665082L;
        final LauncherBackupHelper this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        InvalidBackupException(LauncherBackupHelper launcherBackupHelper, String str) {
            super(str);
            this.this$0 = launcherBackupHelper;
        }

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        InvalidBackupException(LauncherBackupHelper launcherBackupHelper, Throwable th) {
            super(th);
            this.this$0 = launcherBackupHelper;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/launcher3/LauncherBackupHelper$ItemTypeMatcher.class */
    public class ItemTypeMatcher {
        private final ArrayList<Intent> mIntents;
        final LauncherBackupHelper this$0;

        ItemTypeMatcher(LauncherBackupHelper launcherBackupHelper, int i) {
            this.this$0 = launcherBackupHelper;
            this.mIntents = i == 0 ? new ArrayList<>() : parseIntents(i);
        }

        private ArrayList<Intent> parseIntents(int i) {
            ArrayList<Intent> arrayList = new ArrayList<>();
            XmlResourceParser xml = this.this$0.mContext.getResources().getXml(i);
            try {
                DefaultLayoutParser.beginDocument(xml, "resolve");
                int depth = xml.getDepth();
                while (true) {
                    int next = xml.next();
                    if ((next != 3 || xml.getDepth() > depth) && next != 1) {
                        if (next == 2 && "favorite".equals(xml.getName())) {
                            arrayList.add(Intent.parseUri(DefaultLayoutParser.getAttributeValue(xml, "uri"), 0));
                        }
                    }
                }
            } catch (IOException | URISyntaxException | XmlPullParserException e) {
                Log.e("LauncherBackupHelper", "Unable to parse " + i, e);
            } finally {
                xml.close();
            }
            return arrayList;
        }

        public boolean matches(ActivityInfo activityInfo, PackageManager packageManager) {
            for (Intent intent : this.mIntents) {
                intent.setPackage(activityInfo.packageName);
                ResolveInfo resolveActivity = packageManager.resolveActivity(intent, 0);
                if (resolveActivity != null && (resolveActivity.activityInfo.name.equals(activityInfo.name) || resolveActivity.activityInfo.name.equals(activityInfo.targetActivity))) {
                    return true;
                }
            }
            return false;
        }
    }

    public LauncherBackupHelper(Context context) {
        this.mContext = context;
        this.mUserSerial = UserManagerCompat.getInstance(this.mContext).getSerialNumberForUser(UserHandleCompat.myUserHandle());
    }

    private void applyJournal(BackupProtos$Journal backupProtos$Journal) {
        this.mLastBackupRestoreTime = backupProtos$Journal.t;
        this.mExistingKeys.clear();
        if (backupProtos$Journal.key != null) {
            for (BackupProtos$Key backupProtos$Key : backupProtos$Journal.key) {
                this.mExistingKeys.add(keyToBackupKey(backupProtos$Key));
            }
        }
        this.restoredBackupVersion = backupProtos$Journal.backupVersion;
    }

    private void backupFavorites(BackupDataOutput backupDataOutput) throws IOException {
        Cursor query = this.mContext.getContentResolver().query(LauncherSettings$Favorites.CONTENT_URI, FAVORITE_PROJECTION, getUserSelectionArg(), null, null);
        try {
            query.moveToPosition(-1);
            while (query.moveToNext()) {
                long j = query.getLong(0);
                long j2 = query.getLong(1);
                BackupProtos$Key key = getKey(1, j);
                this.mKeys.add(key);
                if (!this.mExistingKeys.contains(keyToBackupKey(key)) || j2 >= this.mLastBackupRestoreTime || this.restoredBackupVersion < 4) {
                    writeRowToBackup(key, packFavorite(query), backupDataOutput);
                }
            }
        } finally {
            query.close();
        }
    }

    private void backupIcons(BackupDataOutput backupDataOutput) throws IOException {
        ContentResolver contentResolver = this.mContext.getContentResolver();
        int i = this.mContext.getResources().getDisplayMetrics().densityDpi;
        UserHandleCompat myUserHandle = UserHandleCompat.myUserHandle();
        int i2 = 0;
        Cursor query = contentResolver.query(LauncherSettings$Favorites.CONTENT_URI, FAVORITE_PROJECTION, "(itemType=0 OR itemType=1) AND " + getUserSelectionArg(), null, null);
        try {
            query.moveToPosition(-1);
            while (query.moveToNext()) {
                long j = query.getLong(0);
                try {
                    try {
                        Intent parseUri = Intent.parseUri(query.getString(2), 0);
                        ComponentName component = parseUri.getComponent();
                        BackupProtos$Key backupProtos$Key = null;
                        String str = null;
                        if (component != null) {
                            backupProtos$Key = getKey(3, component.flattenToShortString());
                            str = keyToBackupKey(backupProtos$Key);
                        } else {
                            Log.w("LauncherBackupHelper", "empty intent on application favorite: " + j);
                        }
                        if (this.mExistingKeys.contains(str)) {
                            this.mKeys.add(backupProtos$Key);
                        } else if (str != null) {
                            if (i2 < 10) {
                                Bitmap icon = this.mIconCache.getIcon(parseUri, myUserHandle);
                                if (icon != null && !this.mIconCache.isDefaultIcon(icon, myUserHandle)) {
                                    writeRowToBackup(backupProtos$Key, packIcon(i, icon), backupDataOutput);
                                    this.mKeys.add(backupProtos$Key);
                                    i2++;
                                }
                            } else {
                                dataChanged();
                            }
                        }
                    } catch (URISyntaxException e) {
                        Log.e("LauncherBackupHelper", "invalid URI on application favorite: " + j);
                    }
                } catch (IOException e2) {
                    Log.e("LauncherBackupHelper", "unable to save application icon for favorite: " + j);
                }
            }
        } finally {
            query.close();
        }
    }

    private BackupProtos$Key backupKeyToKey(String str) throws InvalidBackupException {
        try {
            BackupProtos$Key parseFrom = BackupProtos$Key.parseFrom(Base64.decode(str, 0));
            if (parseFrom.checksum != checkKey(parseFrom)) {
                throw new InvalidBackupException(this, "invalid key read from stream" + str);
            }
            return parseFrom;
        } catch (InvalidProtocolBufferNanoException | IllegalArgumentException e) {
            throw new InvalidBackupException(this, e);
        }
    }

    private void backupScreens(BackupDataOutput backupDataOutput) throws IOException {
        Cursor query = this.mContext.getContentResolver().query(LauncherSettings$WorkspaceScreens.CONTENT_URI, SCREEN_PROJECTION, null, null, null);
        try {
            query.moveToPosition(-1);
            while (query.moveToNext()) {
                long j = query.getLong(0);
                long j2 = query.getLong(1);
                BackupProtos$Key key = getKey(2, j);
                this.mKeys.add(key);
                if (!this.mExistingKeys.contains(keyToBackupKey(key)) || j2 >= this.mLastBackupRestoreTime) {
                    writeRowToBackup(key, packScreen(query), backupDataOutput);
                }
            }
        } finally {
            query.close();
        }
    }

    private void backupWidgets(BackupDataOutput backupDataOutput) throws IOException {
        ContentResolver contentResolver = this.mContext.getContentResolver();
        int i = this.mContext.getResources().getDisplayMetrics().densityDpi;
        int i2 = 0;
        Cursor query = contentResolver.query(LauncherSettings$Favorites.CONTENT_URI, FAVORITE_PROJECTION, "itemType=4 AND " + getUserSelectionArg(), null, null);
        AppWidgetManagerCompat appWidgetManagerCompat = AppWidgetManagerCompat.getInstance(this.mContext);
        try {
            query.moveToPosition(-1);
            while (query.moveToNext()) {
                long j = query.getLong(0);
                String string = query.getString(3);
                BackupProtos$Key backupProtos$Key = null;
                String str = null;
                if (ComponentName.unflattenFromString(string) != null) {
                    backupProtos$Key = getKey(4, string);
                    str = keyToBackupKey(backupProtos$Key);
                } else {
                    Log.w("LauncherBackupHelper", "empty intent on appwidget: " + j);
                }
                if (this.mExistingKeys.contains(str) && this.restoredBackupVersion >= 3) {
                    this.mKeys.add(backupProtos$Key);
                } else if (str != null) {
                    if (i2 < 5) {
                        LauncherAppWidgetProviderInfo launcherAppWidgetInfo = appWidgetManagerCompat.getLauncherAppWidgetInfo(query.getInt(4));
                        if (launcherAppWidgetInfo != null) {
                            writeRowToBackup(backupProtos$Key, packWidget(i, launcherAppWidgetInfo), backupDataOutput);
                            this.mKeys.add(backupProtos$Key);
                            i2++;
                        }
                    } else {
                        dataChanged();
                    }
                }
            }
        } finally {
            query.close();
        }
    }

    private long checkKey(BackupProtos$Key backupProtos$Key) {
        CRC32 crc32 = new CRC32();
        crc32.update(backupProtos$Key.type);
        crc32.update((int) (backupProtos$Key.id & 65535));
        crc32.update((int) ((backupProtos$Key.id >> 32) & 65535));
        if (!TextUtils.isEmpty(backupProtos$Key.name)) {
            crc32.update(backupProtos$Key.name.getBytes());
        }
        return crc32.getValue();
    }

    private void dataChanged() {
        if (this.mBackupManager == null) {
            this.mBackupManager = new BackupManager(this.mContext);
        }
        this.mBackupManager.dataChanged();
    }

    private int getAppVersion() {
        try {
            return this.mContext.getPackageManager().getPackageInfo(this.mContext.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }

    private BackupProtos$Journal getCurrentStateJournal() {
        BackupProtos$Journal backupProtos$Journal = new BackupProtos$Journal();
        backupProtos$Journal.t = this.mLastBackupRestoreTime;
        backupProtos$Journal.key = (BackupProtos$Key[]) this.mKeys.toArray(new BackupProtos$Key[this.mKeys.size()]);
        backupProtos$Journal.appVersion = getAppVersion();
        backupProtos$Journal.backupVersion = 4;
        backupProtos$Journal.profile = this.mDeviceProfileData;
        return backupProtos$Journal;
    }

    private BackupProtos$Key getKey(int i, long j) {
        BackupProtos$Key backupProtos$Key = new BackupProtos$Key();
        backupProtos$Key.type = i;
        backupProtos$Key.id = j;
        backupProtos$Key.checksum = checkKey(backupProtos$Key);
        return backupProtos$Key;
    }

    private BackupProtos$Key getKey(int i, String str) {
        BackupProtos$Key backupProtos$Key = new BackupProtos$Key();
        backupProtos$Key.type = i;
        backupProtos$Key.name = str;
        backupProtos$Key.checksum = checkKey(backupProtos$Key);
        return backupProtos$Key;
    }

    private String getUserSelectionArg() {
        return "profileId=" + UserManagerCompat.getInstance(this.mContext).getSerialNumberForUser(UserHandleCompat.myUserHandle());
    }

    private BackupProtos$DeviceProfieData initDeviceProfileData(InvariantDeviceProfile invariantDeviceProfile) {
        BackupProtos$DeviceProfieData backupProtos$DeviceProfieData = new BackupProtos$DeviceProfieData();
        backupProtos$DeviceProfieData.desktopRows = invariantDeviceProfile.numRows;
        backupProtos$DeviceProfieData.desktopCols = invariantDeviceProfile.numColumns;
        backupProtos$DeviceProfieData.hotseatCount = invariantDeviceProfile.numHotseatIcons;
        backupProtos$DeviceProfieData.allappsRank = invariantDeviceProfile.hotseatAllAppsRank;
        return backupProtos$DeviceProfieData;
    }

    private boolean isBackupCompatible(BackupProtos$Journal backupProtos$Journal) {
        BackupProtos$DeviceProfieData backupProtos$DeviceProfieData = this.mDeviceProfileData;
        BackupProtos$DeviceProfieData backupProtos$DeviceProfieData2 = backupProtos$Journal.profile;
        if (backupProtos$DeviceProfieData2 == null || backupProtos$DeviceProfieData2.desktopCols == 0.0f) {
            return true;
        }
        boolean z = false;
        if (backupProtos$DeviceProfieData.allappsRank >= backupProtos$DeviceProfieData2.hotseatCount) {
            z = true;
            this.mHotseatShift = 0;
        }
        boolean z2 = z;
        if (backupProtos$DeviceProfieData.allappsRank >= backupProtos$DeviceProfieData2.allappsRank) {
            z2 = z;
            if (backupProtos$DeviceProfieData.hotseatCount - backupProtos$DeviceProfieData.allappsRank >= backupProtos$DeviceProfieData2.hotseatCount - backupProtos$DeviceProfieData2.allappsRank) {
                z2 = true;
                this.mHotseatShift = backupProtos$DeviceProfieData.allappsRank - backupProtos$DeviceProfieData2.allappsRank;
            }
        }
        if (z2) {
            if (backupProtos$DeviceProfieData.desktopCols < backupProtos$DeviceProfieData2.desktopCols || backupProtos$DeviceProfieData.desktopRows < backupProtos$DeviceProfieData2.desktopRows) {
                if (GridSizeMigrationTask.ENABLED) {
                    this.migrationCompatibleProfileData = initDeviceProfileData(this.mIdp);
                    this.migrationCompatibleProfileData.desktopCols = backupProtos$DeviceProfieData2.desktopCols;
                    this.migrationCompatibleProfileData.desktopRows = backupProtos$DeviceProfieData2.desktopRows;
                    this.migrationCompatibleProfileData.hotseatCount = backupProtos$DeviceProfieData2.hotseatCount;
                    this.migrationCompatibleProfileData.allappsRank = backupProtos$DeviceProfieData2.allappsRank;
                    return true;
                }
                return false;
            }
            return true;
        }
        return false;
    }

    private boolean isReplaceableHotseatItem(BackupProtos$Favorite backupProtos$Favorite) {
        boolean z;
        if (backupProtos$Favorite.container != -101 || backupProtos$Favorite.intent == null) {
            z = false;
        } else {
            z = true;
            if (backupProtos$Favorite.itemType != 0) {
                z = backupProtos$Favorite.itemType == 1;
            }
        }
        return z;
    }

    private String keyToBackupKey(BackupProtos$Key backupProtos$Key) {
        return Base64.encodeToString(BackupProtos$Key.toByteArray(backupProtos$Key), 2);
    }

    private boolean launcherIsReady() {
        Cursor query = this.mContext.getContentResolver().query(LauncherSettings$Favorites.CONTENT_URI, FAVORITE_PROJECTION, null, null, null);
        if (query == null) {
            return false;
        }
        query.close();
        return LauncherAppState.getInstanceNoCreate() != null;
    }

    private BackupProtos$Favorite packFavorite(Cursor cursor) {
        BackupProtos$Favorite backupProtos$Favorite = new BackupProtos$Favorite();
        backupProtos$Favorite.id = cursor.getLong(0);
        backupProtos$Favorite.screen = cursor.getInt(13);
        backupProtos$Favorite.container = cursor.getInt(7);
        backupProtos$Favorite.cellX = cursor.getInt(5);
        backupProtos$Favorite.cellY = cursor.getInt(6);
        backupProtos$Favorite.spanX = cursor.getInt(14);
        backupProtos$Favorite.spanY = cursor.getInt(15);
        backupProtos$Favorite.iconType = cursor.getInt(11);
        backupProtos$Favorite.rank = cursor.getInt(18);
        String string = cursor.getString(16);
        if (!TextUtils.isEmpty(string)) {
            backupProtos$Favorite.title = string;
        }
        String string2 = cursor.getString(2);
        Intent intent = null;
        if (!TextUtils.isEmpty(string2)) {
            intent = null;
            try {
                Intent parseUri = Intent.parseUri(string2, 0);
                parseUri.removeExtra("profile");
                intent = parseUri;
                backupProtos$Favorite.intent = parseUri.toUri(0);
                intent = parseUri;
            } catch (URISyntaxException e) {
                Log.e("LauncherBackupHelper", "Invalid intent", e);
            }
        }
        backupProtos$Favorite.itemType = cursor.getInt(12);
        if (backupProtos$Favorite.itemType == 4) {
            backupProtos$Favorite.appWidgetId = cursor.getInt(4);
            String string3 = cursor.getString(3);
            if (!TextUtils.isEmpty(string3)) {
                backupProtos$Favorite.appWidgetProvider = string3;
            }
        } else if (backupProtos$Favorite.itemType == 1) {
            if (backupProtos$Favorite.iconType == 0) {
                String string4 = cursor.getString(9);
                if (!TextUtils.isEmpty(string4)) {
                    backupProtos$Favorite.iconPackage = string4;
                }
                String string5 = cursor.getString(10);
                if (!TextUtils.isEmpty(string5)) {
                    backupProtos$Favorite.iconResource = string5;
                }
            }
            byte[] blob = cursor.getBlob(8);
            if (blob != null && blob.length > 0) {
                backupProtos$Favorite.icon = blob;
            }
        }
        if (isReplaceableHotseatItem(backupProtos$Favorite) && intent != null && intent.getComponent() != null) {
            PackageManager packageManager = this.mContext.getPackageManager();
            ActivityInfo activityInfo = null;
            try {
                activityInfo = packageManager.getActivityInfo(intent.getComponent(), 0);
            } catch (PackageManager.NameNotFoundException e2) {
                Log.e("LauncherBackupHelper", "Target not found", e2);
            }
            if (activityInfo == null) {
                return backupProtos$Favorite;
            }
            int i = 0;
            while (true) {
                if (i >= this.mItemTypeMatchers.length) {
                    break;
                }
                if (this.mItemTypeMatchers[i] == null) {
                    this.mItemTypeMatchers[i] = new ItemTypeMatcher(this, CommonAppTypeParser.getResourceForItemType(i));
                }
                if (this.mItemTypeMatchers[i].matches(activityInfo, packageManager)) {
                    backupProtos$Favorite.targetType = i;
                    break;
                }
                i++;
            }
        }
        return backupProtos$Favorite;
    }

    private BackupProtos$Resource packIcon(int i, Bitmap bitmap) {
        BackupProtos$Resource backupProtos$Resource = new BackupProtos$Resource();
        backupProtos$Resource.dpi = i;
        backupProtos$Resource.data = Utilities.flattenBitmap(bitmap);
        return backupProtos$Resource;
    }

    private BackupProtos$Screen packScreen(Cursor cursor) {
        BackupProtos$Screen backupProtos$Screen = new BackupProtos$Screen();
        backupProtos$Screen.id = cursor.getLong(0);
        backupProtos$Screen.rank = cursor.getInt(2);
        return backupProtos$Screen;
    }

    private BackupProtos$Widget packWidget(int i, LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo) {
        boolean z = false;
        BackupProtos$Widget backupProtos$Widget = new BackupProtos$Widget();
        backupProtos$Widget.provider = launcherAppWidgetProviderInfo.provider.flattenToShortString();
        backupProtos$Widget.label = launcherAppWidgetProviderInfo.label;
        if (launcherAppWidgetProviderInfo.configure != null) {
            z = true;
        }
        backupProtos$Widget.configure = z;
        if (launcherAppWidgetProviderInfo.icon != 0) {
            backupProtos$Widget.icon = new BackupProtos$Resource();
            backupProtos$Widget.icon.data = Utilities.flattenBitmap(Utilities.createIconBitmap(this.mIconCache.getFullResIcon(launcherAppWidgetProviderInfo.provider.getPackageName(), launcherAppWidgetProviderInfo.icon), this.mContext));
            backupProtos$Widget.icon.dpi = i;
        }
        Point minSpans = launcherAppWidgetProviderInfo.getMinSpans(this.mIdp, this.mContext);
        backupProtos$Widget.minSpanX = minSpans.x;
        backupProtos$Widget.minSpanY = minSpans.y;
        return backupProtos$Widget;
    }

    private static byte[] readCheckedBytes(byte[] bArr, int i) throws InvalidProtocolBufferNanoException {
        BackupProtos$CheckedMessage backupProtos$CheckedMessage = new BackupProtos$CheckedMessage();
        MessageNano.mergeFrom(backupProtos$CheckedMessage, bArr, 0, i);
        CRC32 crc32 = new CRC32();
        crc32.update(backupProtos$CheckedMessage.payload);
        if (backupProtos$CheckedMessage.checksum != crc32.getValue()) {
            throw new InvalidProtocolBufferNanoException("checksum does not match");
        }
        return backupProtos$CheckedMessage.payload;
    }

    private BackupProtos$Journal readJournal(ParcelFileDescriptor parcelFileDescriptor) {
        BackupProtos$Journal backupProtos$Journal = new BackupProtos$Journal();
        if (parcelFileDescriptor == null) {
            return backupProtos$Journal;
        }
        FileInputStream fileInputStream = new FileInputStream(parcelFileDescriptor.getFileDescriptor());
        try {
            int available = fileInputStream.available();
            if (available < 1000000) {
                byte[] bArr = new byte[available];
                int i = 0;
                boolean z = false;
                InvalidProtocolBufferNanoException e = null;
                while (available > 0) {
                    try {
                        int read = fileInputStream.read(bArr, i, 1);
                        if (read > 0) {
                            available -= read;
                            i += read;
                        } else {
                            Log.w("LauncherBackupHelper", "unexpected end of file while reading journal.");
                            available = 0;
                        }
                    } catch (IOException e2) {
                        bArr = null;
                        available = 0;
                    }
                    try {
                        MessageNano.mergeFrom(backupProtos$Journal, readCheckedBytes(bArr, i));
                        z = true;
                        available = 0;
                    } catch (InvalidProtocolBufferNanoException e3) {
                        e = e3;
                        backupProtos$Journal.clear();
                    }
                }
                if (!z) {
                    Log.w("LauncherBackupHelper", "could not find a valid journal", e);
                }
            }
        } catch (IOException e4) {
            Log.w("LauncherBackupHelper", "failed to close the journal", e4);
        }
        return backupProtos$Journal;
    }

    private void restoreFavorite(BackupProtos$Key backupProtos$Key, byte[] bArr, int i) throws IOException {
        this.mContext.getContentResolver().insert(LauncherSettings$Favorites.CONTENT_URI, unpackFavorite(bArr, i));
    }

    private void restoreIcon(BackupProtos$Key backupProtos$Key, byte[] bArr, int i) throws IOException {
        BackupProtos$Resource backupProtos$Resource = (BackupProtos$Resource) unpackProto(new BackupProtos$Resource(), bArr, i);
        Bitmap decodeByteArray = BitmapFactory.decodeByteArray(backupProtos$Resource.data, 0, backupProtos$Resource.data.length);
        if (decodeByteArray == null) {
            Log.w("LauncherBackupHelper", "failed to unpack icon for " + backupProtos$Key.name);
        } else {
            this.mIconCache.preloadIcon(ComponentName.unflattenFromString(backupProtos$Key.name), decodeByteArray, backupProtos$Resource.dpi, "", this.mUserSerial, this.mIdp);
        }
    }

    private void restoreScreen(BackupProtos$Key backupProtos$Key, byte[] bArr, int i) throws IOException {
        this.mContext.getContentResolver().insert(LauncherSettings$WorkspaceScreens.CONTENT_URI, unpackScreen(bArr, i));
    }

    private void restoreWidget(BackupProtos$Key backupProtos$Key, byte[] bArr, int i) throws IOException {
        BackupProtos$Widget backupProtos$Widget = (BackupProtos$Widget) unpackProto(new BackupProtos$Widget(), bArr, i);
        if (backupProtos$Widget.icon.data != null) {
            Bitmap decodeByteArray = BitmapFactory.decodeByteArray(backupProtos$Widget.icon.data, 0, backupProtos$Widget.icon.data.length);
            if (decodeByteArray == null) {
                Log.w("LauncherBackupHelper", "failed to unpack widget icon for " + backupProtos$Key.name);
            } else {
                this.mIconCache.preloadIcon(ComponentName.unflattenFromString(backupProtos$Widget.provider), decodeByteArray, backupProtos$Widget.icon.dpi, backupProtos$Widget.label, this.mUserSerial, this.mIdp);
            }
        }
        this.widgetSizes.add(backupProtos$Widget.provider + "#" + backupProtos$Widget.minSpanX + "," + backupProtos$Widget.minSpanY);
    }

    private ContentValues unpackFavorite(byte[] bArr, int i) throws IOException {
        BackupProtos$Favorite backupProtos$Favorite = (BackupProtos$Favorite) unpackProto(new BackupProtos$Favorite(), bArr, i);
        if (backupProtos$Favorite.container == -101) {
            backupProtos$Favorite.screen += this.mHotseatShift;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put("_id", Long.valueOf(backupProtos$Favorite.id));
        contentValues.put("screen", Integer.valueOf(backupProtos$Favorite.screen));
        contentValues.put("container", Integer.valueOf(backupProtos$Favorite.container));
        contentValues.put("cellX", Integer.valueOf(backupProtos$Favorite.cellX));
        contentValues.put("cellY", Integer.valueOf(backupProtos$Favorite.cellY));
        contentValues.put("spanX", Integer.valueOf(backupProtos$Favorite.spanX));
        contentValues.put("spanY", Integer.valueOf(backupProtos$Favorite.spanY));
        contentValues.put("rank", Integer.valueOf(backupProtos$Favorite.rank));
        if (backupProtos$Favorite.itemType == 1) {
            contentValues.put("iconType", Integer.valueOf(backupProtos$Favorite.iconType));
            if (backupProtos$Favorite.iconType == 0) {
                contentValues.put("iconPackage", backupProtos$Favorite.iconPackage);
                contentValues.put("iconResource", backupProtos$Favorite.iconResource);
            }
            contentValues.put("icon", backupProtos$Favorite.icon);
        }
        if (TextUtils.isEmpty(backupProtos$Favorite.title)) {
            contentValues.put("title", "");
        } else {
            contentValues.put("title", backupProtos$Favorite.title);
        }
        if (!TextUtils.isEmpty(backupProtos$Favorite.intent)) {
            contentValues.put("intent", backupProtos$Favorite.intent);
        }
        contentValues.put("itemType", Integer.valueOf(backupProtos$Favorite.itemType));
        contentValues.put("profileId", Long.valueOf(UserManagerCompat.getInstance(this.mContext).getSerialNumberForUser(UserHandleCompat.myUserHandle())));
        BackupProtos$DeviceProfieData backupProtos$DeviceProfieData = this.migrationCompatibleProfileData == null ? this.mDeviceProfileData : this.migrationCompatibleProfileData;
        if (backupProtos$Favorite.itemType == 4) {
            if (!TextUtils.isEmpty(backupProtos$Favorite.appWidgetProvider)) {
                contentValues.put("appWidgetProvider", backupProtos$Favorite.appWidgetProvider);
            }
            contentValues.put("appWidgetId", Integer.valueOf(backupProtos$Favorite.appWidgetId));
            contentValues.put("restored", (Integer) 7);
            if (backupProtos$Favorite.cellX + backupProtos$Favorite.spanX > backupProtos$DeviceProfieData.desktopCols || backupProtos$Favorite.cellY + backupProtos$Favorite.spanY > backupProtos$DeviceProfieData.desktopRows) {
                this.restoreSuccessful = false;
                throw new InvalidBackupException(this, "Widget not in screen bounds, aborting restore");
            }
        } else {
            if (!isReplaceableHotseatItem(backupProtos$Favorite) || backupProtos$Favorite.targetType == 0 || backupProtos$Favorite.targetType >= 7) {
                contentValues.put("restored", (Integer) 1);
            } else {
                Log.e("LauncherBackupHelper", "Added item type flag");
                contentValues.put("restored", Integer.valueOf(CommonAppTypeParser.encodeItemTypeToFlag(backupProtos$Favorite.targetType) | 1));
            }
            if (backupProtos$Favorite.container == -101) {
                if (backupProtos$Favorite.screen >= backupProtos$DeviceProfieData.hotseatCount || backupProtos$Favorite.screen == backupProtos$DeviceProfieData.allappsRank) {
                    this.restoreSuccessful = false;
                    throw new InvalidBackupException(this, "Item not in hotseat bounds, aborting restore");
                }
            } else if (backupProtos$Favorite.cellX >= backupProtos$DeviceProfieData.desktopCols || backupProtos$Favorite.cellY >= backupProtos$DeviceProfieData.desktopRows) {
                this.restoreSuccessful = false;
                throw new InvalidBackupException(this, "Item not in desktop bounds, aborting restore");
            }
        }
        return contentValues;
    }

    private <T extends MessageNano> T unpackProto(T t, byte[] bArr, int i) throws InvalidProtocolBufferNanoException {
        MessageNano.mergeFrom(t, readCheckedBytes(bArr, i));
        return t;
    }

    private ContentValues unpackScreen(byte[] bArr, int i) throws InvalidProtocolBufferNanoException {
        BackupProtos$Screen backupProtos$Screen = (BackupProtos$Screen) unpackProto(new BackupProtos$Screen(), bArr, i);
        ContentValues contentValues = new ContentValues();
        contentValues.put("_id", Long.valueOf(backupProtos$Screen.id));
        contentValues.put("screenRank", Integer.valueOf(backupProtos$Screen.rank));
        return contentValues;
    }

    private byte[] writeCheckedBytes(MessageNano messageNano) {
        BackupProtos$CheckedMessage backupProtos$CheckedMessage = new BackupProtos$CheckedMessage();
        backupProtos$CheckedMessage.payload = MessageNano.toByteArray(messageNano);
        CRC32 crc32 = new CRC32();
        crc32.update(backupProtos$CheckedMessage.payload);
        backupProtos$CheckedMessage.checksum = crc32.getValue();
        return MessageNano.toByteArray(backupProtos$CheckedMessage);
    }

    private void writeJournal(ParcelFileDescriptor parcelFileDescriptor, BackupProtos$Journal backupProtos$Journal) {
        try {
            new FileOutputStream(parcelFileDescriptor.getFileDescriptor()).write(writeCheckedBytes(backupProtos$Journal));
        } catch (IOException e) {
            Log.w("LauncherBackupHelper", "failed to write backup journal", e);
        }
    }

    private void writeRowToBackup(BackupProtos$Key backupProtos$Key, MessageNano messageNano, BackupDataOutput backupDataOutput) throws IOException {
        writeRowToBackup(keyToBackupKey(backupProtos$Key), messageNano, backupDataOutput);
    }

    private void writeRowToBackup(String str, MessageNano messageNano, BackupDataOutput backupDataOutput) throws IOException {
        byte[] writeCheckedBytes = writeCheckedBytes(messageNano);
        backupDataOutput.writeEntityHeader(str, writeCheckedBytes.length);
        backupDataOutput.writeEntityData(writeCheckedBytes, writeCheckedBytes.length);
        this.mBackupDataWasUpdated = true;
    }

    @Override // android.app.backup.BackupHelper
    public void performBackup(ParcelFileDescriptor parcelFileDescriptor, BackupDataOutput backupDataOutput, ParcelFileDescriptor parcelFileDescriptor2) {
        BackupProtos$Journal readJournal = readJournal(parcelFileDescriptor);
        if (!launcherIsReady()) {
            dataChanged();
            writeJournal(parcelFileDescriptor2, readJournal);
            return;
        }
        if (this.mDeviceProfileData == null) {
            LauncherAppState launcherAppState = LauncherAppState.getInstance();
            this.mIdp = launcherAppState.getInvariantDeviceProfile();
            this.mDeviceProfileData = initDeviceProfileData(this.mIdp);
            this.mIconCache = launcherAppState.getIconCache();
        }
        Log.v("LauncherBackupHelper", "lastBackupTime = " + readJournal.t);
        this.mKeys.clear();
        applyJournal(readJournal);
        long currentTimeMillis = System.currentTimeMillis();
        this.mBackupDataWasUpdated = false;
        try {
            backupFavorites(backupDataOutput);
            backupScreens(backupDataOutput);
            backupIcons(backupDataOutput);
            backupWidgets(backupDataOutput);
            HashSet hashSet = new HashSet();
            for (BackupProtos$Key backupProtos$Key : this.mKeys) {
                hashSet.add(keyToBackupKey(backupProtos$Key));
            }
            this.mExistingKeys.removeAll(hashSet);
            for (String str : this.mExistingKeys) {
                backupDataOutput.writeEntityHeader(str, -1);
                this.mBackupDataWasUpdated = true;
            }
            this.mExistingKeys.clear();
            if (!this.mBackupDataWasUpdated) {
                this.mBackupDataWasUpdated = (readJournal.profile != null && Arrays.equals(BackupProtos$DeviceProfieData.toByteArray(readJournal.profile), BackupProtos$DeviceProfieData.toByteArray(this.mDeviceProfileData)) && readJournal.backupVersion == 4) ? readJournal.appVersion != getAppVersion() : true;
            }
            if (this.mBackupDataWasUpdated) {
                this.mLastBackupRestoreTime = currentTimeMillis;
                writeRowToBackup("#", getCurrentStateJournal(), backupDataOutput);
            }
        } catch (IOException e) {
            Log.e("LauncherBackupHelper", "launcher backup has failed", e);
        }
        writeNewStateDescription(parcelFileDescriptor2);
    }

    @Override // android.app.backup.BackupHelper
    public void restoreEntity(BackupDataInputStream backupDataInputStream) {
        if (this.restoreSuccessful) {
            if (this.mDeviceProfileData == null) {
                this.mIdp = new InvariantDeviceProfile(this.mContext);
                this.mDeviceProfileData = initDeviceProfileData(this.mIdp);
                this.mIconCache = new IconCache(this.mContext, this.mIdp);
            }
            int size = backupDataInputStream.size();
            if (this.mBuffer.length < size) {
                this.mBuffer = new byte[size];
            }
            try {
                backupDataInputStream.read(this.mBuffer, 0, size);
                String key = backupDataInputStream.getKey();
                if ("#".equals(key)) {
                    if (!this.mKeys.isEmpty()) {
                        Log.wtf("LauncherBackupHelper", keyToBackupKey(this.mKeys.get(0)) + " received after #");
                        this.restoreSuccessful = false;
                        return;
                    }
                    BackupProtos$Journal backupProtos$Journal = new BackupProtos$Journal();
                    MessageNano.mergeFrom(backupProtos$Journal, readCheckedBytes(this.mBuffer, size));
                    applyJournal(backupProtos$Journal);
                    this.restoreSuccessful = isBackupCompatible(backupProtos$Journal);
                } else if (this.mExistingKeys.isEmpty() || this.mExistingKeys.contains(key)) {
                    BackupProtos$Key backupKeyToKey = backupKeyToKey(key);
                    this.mKeys.add(backupKeyToKey);
                    switch (backupKeyToKey.type) {
                        case 1:
                            restoreFavorite(backupKeyToKey, this.mBuffer, size);
                            return;
                        case 2:
                            restoreScreen(backupKeyToKey, this.mBuffer, size);
                            return;
                        case 3:
                            restoreIcon(backupKeyToKey, this.mBuffer, size);
                            return;
                        case 4:
                            restoreWidget(backupKeyToKey, this.mBuffer, size);
                            return;
                        default:
                            Log.w("LauncherBackupHelper", "unknown restore entity type: " + backupKeyToKey.type);
                            this.mKeys.remove(backupKeyToKey);
                            return;
                    }
                }
            } catch (IOException e) {
                Log.w("LauncherBackupHelper", "ignoring unparsable backup entry", e);
            }
        }
    }

    public boolean shouldAttemptWorkspaceMigration() {
        return this.migrationCompatibleProfileData != null;
    }

    @Override // android.app.backup.BackupHelper
    public void writeNewStateDescription(ParcelFileDescriptor parcelFileDescriptor) {
        writeJournal(parcelFileDescriptor, getCurrentStateJournal());
    }
}
