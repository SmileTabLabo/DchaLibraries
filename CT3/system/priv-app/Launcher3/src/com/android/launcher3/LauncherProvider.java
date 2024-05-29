package com.android.launcher3;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Process;
import android.os.StrictMode;
import android.os.UserManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import com.android.launcher3.AutoInstallsLayout;
import com.android.launcher3.compat.UserHandleCompat;
import com.android.launcher3.compat.UserManagerCompat;
import com.android.launcher3.config.ProviderConfig;
import com.android.launcher3.util.ManagedProfileHeuristic;
import com.mediatek.launcher3.LauncherLog;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
/* loaded from: a.zip:com/android/launcher3/LauncherProvider.class */
public class LauncherProvider extends ContentProvider {
    public static final String AUTHORITY = ProviderConfig.AUTHORITY;
    LauncherProviderChangeListener mListener;
    protected DatabaseHelper mOpenHelper;

    /* JADX INFO: Access modifiers changed from: protected */
    /* loaded from: a.zip:com/android/launcher3/LauncherProvider$DatabaseHelper.class */
    public static class DatabaseHelper extends SQLiteOpenHelper implements AutoInstallsLayout.LayoutParserCallback {
        final AppWidgetHost mAppWidgetHost;
        private final Context mContext;
        LauncherProviderChangeListener mListener;
        private long mMaxItemId;
        private long mMaxScreenId;
        private boolean mNewDbCreated;

        DatabaseHelper(Context context) {
            super(context, "launcher.db", (SQLiteDatabase.CursorFactory) null, 26);
            this.mMaxItemId = -1L;
            this.mMaxScreenId = -1L;
            this.mNewDbCreated = false;
            this.mContext = context;
            this.mAppWidgetHost = new AppWidgetHost(context, 1024);
            if (!tableExists("favorites") || !tableExists("workspaceScreens")) {
                Log.e("LauncherProvider", "Tables are missing after onCreate has been called. Trying to recreate");
                addFavoritesTable(getWritableDatabase(), true);
                addWorkspacesTable(getWritableDatabase(), true);
            }
            if (this.mMaxItemId == -1) {
                this.mMaxItemId = initializeMaxItemId(getWritableDatabase());
            }
            if (this.mMaxScreenId == -1) {
                this.mMaxScreenId = initializeMaxScreenId(getWritableDatabase());
            }
        }

        private void addFavoritesTable(SQLiteDatabase sQLiteDatabase, boolean z) {
            sQLiteDatabase.execSQL("CREATE TABLE " + (z ? " IF NOT EXISTS " : "") + "favorites (_id INTEGER PRIMARY KEY,title TEXT,intent TEXT,container INTEGER,screen INTEGER,cellX INTEGER,cellY INTEGER,spanX INTEGER,spanY INTEGER,itemType INTEGER,appWidgetId INTEGER NOT NULL DEFAULT -1,isShortcut INTEGER,iconType INTEGER,iconPackage TEXT,iconResource TEXT,icon BLOB,uri TEXT,displayMode INTEGER,appWidgetProvider TEXT,modified INTEGER NOT NULL DEFAULT 0,restored INTEGER NOT NULL DEFAULT 0,profileId INTEGER DEFAULT " + getDefaultUserSerial() + ",rank INTEGER NOT NULL DEFAULT 0,options INTEGER NOT NULL DEFAULT 0);");
        }

        private boolean addIntegerColumn(SQLiteDatabase sQLiteDatabase, String str, long j) {
            sQLiteDatabase.beginTransaction();
            try {
                try {
                    sQLiteDatabase.execSQL("ALTER TABLE favorites ADD COLUMN " + str + " INTEGER NOT NULL DEFAULT " + j + ";");
                    sQLiteDatabase.setTransactionSuccessful();
                    sQLiteDatabase.endTransaction();
                    return true;
                } catch (SQLException e) {
                    Log.e("LauncherProvider", e.getMessage(), e);
                    sQLiteDatabase.endTransaction();
                    return false;
                }
            } catch (Throwable th) {
                sQLiteDatabase.endTransaction();
                throw th;
            }
        }

        private boolean addProfileColumn(SQLiteDatabase sQLiteDatabase) {
            return addIntegerColumn(sQLiteDatabase, "profileId", UserManagerCompat.getInstance(this.mContext).getSerialNumberForUser(UserHandleCompat.myUserHandle()));
        }

        private boolean addScreenIdIfNecessary(long j) {
            if (hasScreenId(j)) {
                return true;
            }
            int maxScreenRank = getMaxScreenRank();
            ContentValues contentValues = new ContentValues();
            contentValues.put("_id", Long.valueOf(j));
            contentValues.put("screenRank", Integer.valueOf(maxScreenRank + 1));
            return LauncherProvider.dbInsertAndCheck(this, getWritableDatabase(), "workspaceScreens", null, contentValues) >= 0;
        }

        private void addWorkspacesTable(SQLiteDatabase sQLiteDatabase, boolean z) {
            sQLiteDatabase.execSQL("CREATE TABLE " + (z ? " IF NOT EXISTS " : "") + "workspaceScreens (_id INTEGER PRIMARY KEY,screenRank INTEGER,modified INTEGER NOT NULL DEFAULT 0);");
        }

        private int getMaxScreenRank() {
            Cursor rawQuery = getWritableDatabase().rawQuery("SELECT MAX(screenRank) FROM workspaceScreens", null);
            int i = -1;
            if (rawQuery != null) {
                i = -1;
                if (rawQuery.moveToNext()) {
                    i = rawQuery.getInt(0);
                }
            }
            if (rawQuery != null) {
                rawQuery.close();
            }
            return i;
        }

        private boolean hasScreenId(long j) {
            boolean z = false;
            Cursor rawQuery = getWritableDatabase().rawQuery("SELECT * FROM workspaceScreens WHERE _id = " + j, null);
            if (rawQuery != null) {
                int count = rawQuery.getCount();
                rawQuery.close();
                if (count > 0) {
                    z = true;
                }
                return z;
            }
            return false;
        }

        private long initializeMaxItemId(SQLiteDatabase sQLiteDatabase) {
            return LauncherProvider.getMaxId(sQLiteDatabase, "favorites");
        }

        private long initializeMaxScreenId(SQLiteDatabase sQLiteDatabase) {
            return LauncherProvider.getMaxId(sQLiteDatabase, "workspaceScreens");
        }

        private void removeOrphanedItems(SQLiteDatabase sQLiteDatabase) {
            sQLiteDatabase.execSQL("DELETE FROM favorites WHERE screen NOT IN (SELECT _id FROM workspaceScreens) AND container = -100");
            sQLiteDatabase.execSQL("DELETE FROM favorites WHERE container <> -100 AND container <> -101 AND container NOT IN (SELECT _id FROM favorites WHERE itemType = 2)");
        }

        private void setFlagJustLoadedOldDb() {
            Utilities.getPrefs(this.mContext).edit().putBoolean("EMPTY_DATABASE_CREATED", false).commit();
        }

        private boolean tableExists(String str) {
            boolean z = true;
            Cursor query = getReadableDatabase().query(true, "sqlite_master", new String[]{"tbl_name"}, "tbl_name = ?", new String[]{str}, null, null, null, null, null);
            try {
                if (query.getCount() <= 0) {
                    z = false;
                }
                query.close();
                return z;
            } catch (Throwable th) {
                query.close();
                throw th;
            }
        }

        public void checkId(String str, ContentValues contentValues) {
            long longValue = contentValues.getAsLong("_id").longValue();
            if (str == "workspaceScreens") {
                this.mMaxScreenId = Math.max(longValue, this.mMaxScreenId);
            } else {
                this.mMaxItemId = Math.max(longValue, this.mMaxItemId);
            }
        }

        void convertShortcutsToLauncherActivities(SQLiteDatabase sQLiteDatabase) {
            sQLiteDatabase.beginTransaction();
            Cursor cursor = null;
            SQLiteStatement sQLiteStatement = null;
            Cursor cursor2 = null;
            SQLiteStatement sQLiteStatement2 = null;
            try {
                try {
                    Cursor query = sQLiteDatabase.query("favorites", new String[]{"_id", "intent"}, "itemType=1 AND profileId=" + UserManagerCompat.getInstance(this.mContext).getSerialNumberForUser(UserHandleCompat.myUserHandle()), null, null, null, null);
                    SQLiteStatement compileStatement = sQLiteDatabase.compileStatement("UPDATE favorites SET itemType=0 WHERE _id=?");
                    int columnIndexOrThrow = query.getColumnIndexOrThrow("_id");
                    int columnIndexOrThrow2 = query.getColumnIndexOrThrow("intent");
                    while (query.moveToNext()) {
                        try {
                            if (Utilities.isLauncherAppTarget(Intent.parseUri(query.getString(columnIndexOrThrow2), 0))) {
                                compileStatement.bindLong(1, query.getLong(columnIndexOrThrow));
                                compileStatement.executeUpdateDelete();
                            }
                        } catch (URISyntaxException e) {
                            Log.e("LauncherProvider", "Unable to parse intent", e);
                        }
                    }
                    cursor = query;
                    sQLiteStatement = compileStatement;
                    cursor2 = query;
                    sQLiteStatement2 = compileStatement;
                    sQLiteDatabase.setTransactionSuccessful();
                    sQLiteDatabase.endTransaction();
                    if (query != null) {
                        query.close();
                    }
                    if (compileStatement != null) {
                        compileStatement.close();
                    }
                } catch (SQLException e2) {
                    Log.w("LauncherProvider", "Error deduping shortcuts", e2);
                    sQLiteDatabase.endTransaction();
                    if (cursor != null) {
                        cursor.close();
                    }
                    if (sQLiteStatement != null) {
                        sQLiteStatement.close();
                    }
                }
            } catch (Throwable th) {
                sQLiteDatabase.endTransaction();
                if (cursor2 != null) {
                    cursor2.close();
                }
                if (sQLiteStatement2 != null) {
                    sQLiteStatement2.close();
                }
                throw th;
            }
        }

        public void createEmptyDB(SQLiteDatabase sQLiteDatabase) {
            sQLiteDatabase.execSQL("DROP TABLE IF EXISTS favorites");
            sQLiteDatabase.execSQL("DROP TABLE IF EXISTS workspaceScreens");
            onCreate(sQLiteDatabase);
        }

        @Override // com.android.launcher3.AutoInstallsLayout.LayoutParserCallback
        public long generateNewItemId() {
            if (this.mMaxItemId < 0) {
                throw new RuntimeException("Error: max item id was not initialized");
            }
            this.mMaxItemId++;
            return this.mMaxItemId;
        }

        public long generateNewScreenId() {
            if (this.mMaxScreenId < 0) {
                throw new RuntimeException("Error: max screen id was not initialized");
            }
            this.mMaxScreenId++;
            return this.mMaxScreenId;
        }

        protected long getDefaultUserSerial() {
            return UserManagerCompat.getInstance(this.mContext).getSerialNumberForUser(UserHandleCompat.myUserHandle());
        }

        boolean initializeExternalAdd(ContentValues contentValues) {
            contentValues.put("_id", Long.valueOf(generateNewItemId()));
            Integer asInteger = contentValues.getAsInteger("itemType");
            if (asInteger != null && asInteger.intValue() == 4 && !contentValues.containsKey("appWidgetId")) {
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.mContext);
                ComponentName unflattenFromString = ComponentName.unflattenFromString(contentValues.getAsString("appWidgetProvider"));
                if (unflattenFromString == null) {
                    return false;
                }
                try {
                    int allocateAppWidgetId = this.mAppWidgetHost.allocateAppWidgetId();
                    contentValues.put("appWidgetId", Integer.valueOf(allocateAppWidgetId));
                    if (!appWidgetManager.bindAppWidgetIdIfAllowed(allocateAppWidgetId, unflattenFromString)) {
                        return false;
                    }
                } catch (RuntimeException e) {
                    Log.e("LauncherProvider", "Failed to initialize external widget", e);
                    return false;
                }
            }
            return addScreenIdIfNecessary(contentValues.getAsLong("screen").longValue());
        }

        @Override // com.android.launcher3.AutoInstallsLayout.LayoutParserCallback
        public long insertAndCheck(SQLiteDatabase sQLiteDatabase, ContentValues contentValues) {
            return LauncherProvider.dbInsertAndCheck(this, sQLiteDatabase, "favorites", null, contentValues);
        }

        int loadFavorites(SQLiteDatabase sQLiteDatabase, AutoInstallsLayout autoInstallsLayout) {
            ArrayList<Long> arrayList = new ArrayList<>();
            int loadLayout = autoInstallsLayout.loadLayout(sQLiteDatabase, arrayList);
            Collections.sort(arrayList);
            int i = 0;
            ContentValues contentValues = new ContentValues();
            for (Long l : arrayList) {
                contentValues.clear();
                contentValues.put("_id", l);
                contentValues.put("screenRank", Integer.valueOf(i));
                if (LauncherProvider.dbInsertAndCheck(this, sQLiteDatabase, "workspaceScreens", null, contentValues) < 0) {
                    throw new RuntimeException("Failed initialize screen tablefrom default layout");
                }
                i++;
            }
            this.mMaxItemId = initializeMaxItemId(sQLiteDatabase);
            this.mMaxScreenId = initializeMaxScreenId(sQLiteDatabase);
            return loadLayout;
        }

        void migrateLauncher2Shortcuts(SQLiteDatabase sQLiteDatabase, Uri uri) {
            Cursor cursor;
            UserHandleCompat myUserHandle;
            long serialNumberForUser;
            int i = 0;
            int i2 = 0;
            try {
                cursor = this.mContext.getContentResolver().query(uri, null, null, null, "title ASC");
            } catch (Exception e) {
                cursor = null;
            }
            if (cursor != null) {
                i = 0;
                i2 = 0;
                try {
                    if (cursor.getCount() > 0) {
                        int columnIndexOrThrow = cursor.getColumnIndexOrThrow("_id");
                        int columnIndexOrThrow2 = cursor.getColumnIndexOrThrow("intent");
                        int columnIndexOrThrow3 = cursor.getColumnIndexOrThrow("title");
                        int columnIndexOrThrow4 = cursor.getColumnIndexOrThrow("iconType");
                        int columnIndexOrThrow5 = cursor.getColumnIndexOrThrow("icon");
                        int columnIndexOrThrow6 = cursor.getColumnIndexOrThrow("iconPackage");
                        int columnIndexOrThrow7 = cursor.getColumnIndexOrThrow("iconResource");
                        int columnIndexOrThrow8 = cursor.getColumnIndexOrThrow("container");
                        int columnIndexOrThrow9 = cursor.getColumnIndexOrThrow("itemType");
                        int columnIndexOrThrow10 = cursor.getColumnIndexOrThrow("screen");
                        int columnIndexOrThrow11 = cursor.getColumnIndexOrThrow("cellX");
                        int columnIndexOrThrow12 = cursor.getColumnIndexOrThrow("cellY");
                        int columnIndexOrThrow13 = cursor.getColumnIndexOrThrow("uri");
                        int columnIndexOrThrow14 = cursor.getColumnIndexOrThrow("displayMode");
                        int columnIndex = cursor.getColumnIndex("profileId");
                        InvariantDeviceProfile invariantDeviceProfile = LauncherAppState.getInstance().getInvariantDeviceProfile();
                        int i3 = invariantDeviceProfile.numColumns;
                        int i4 = invariantDeviceProfile.numRows;
                        int i5 = invariantDeviceProfile.numHotseatIcons;
                        HashSet hashSet = new HashSet(cursor.getCount());
                        ArrayList arrayList = new ArrayList();
                        ArrayList arrayList2 = new ArrayList();
                        SparseArray sparseArray = new SparseArray();
                        while (cursor.moveToNext()) {
                            int i6 = cursor.getInt(columnIndexOrThrow9);
                            if (i6 == 0 || i6 == 1 || i6 == 2) {
                                int i7 = cursor.getInt(columnIndexOrThrow11);
                                int i8 = cursor.getInt(columnIndexOrThrow12);
                                int i9 = cursor.getInt(columnIndexOrThrow10);
                                int i10 = cursor.getInt(columnIndexOrThrow8);
                                String string = cursor.getString(columnIndexOrThrow2);
                                UserManagerCompat userManagerCompat = UserManagerCompat.getInstance(this.mContext);
                                if (columnIndex == -1 || cursor.isNull(columnIndex)) {
                                    myUserHandle = UserHandleCompat.myUserHandle();
                                    serialNumberForUser = userManagerCompat.getSerialNumberForUser(myUserHandle);
                                } else {
                                    serialNumberForUser = cursor.getInt(columnIndex);
                                    myUserHandle = userManagerCompat.getUserForSerialNumber(serialNumberForUser);
                                }
                                if (myUserHandle == null) {
                                    Launcher.addDumpLog("LauncherProvider", "skipping deleted user", true);
                                } else {
                                    Launcher.addDumpLog("LauncherProvider", "migrating \"" + cursor.getString(columnIndexOrThrow3) + "\" (" + i7 + "," + i8 + "@" + LauncherSettings$Favorites.containerToString(i10) + "/" + i9 + "): " + string, true);
                                    if (i6 != 2) {
                                        try {
                                            Intent parseUri = Intent.parseUri(string, 0);
                                            ComponentName component = parseUri.getComponent();
                                            if (TextUtils.isEmpty(string)) {
                                                Launcher.addDumpLog("LauncherProvider", "skipping empty intent", true);
                                            } else if (component != null && !LauncherModel.isValidPackageActivity(this.mContext, component, myUserHandle)) {
                                                Launcher.addDumpLog("LauncherProvider", "skipping item whose component no longer exists.", true);
                                            } else if (i10 == -100) {
                                                parseUri.setPackage(null);
                                                int flags = parseUri.getFlags();
                                                parseUri.setFlags(0);
                                                String uri2 = parseUri.toUri(0);
                                                parseUri.setFlags(flags);
                                                if (hashSet.contains(uri2)) {
                                                    Launcher.addDumpLog("LauncherProvider", "skipping duplicate", true);
                                                } else {
                                                    hashSet.add(uri2);
                                                }
                                            }
                                        } catch (URISyntaxException e2) {
                                            Launcher.addDumpLog("LauncherProvider", "skipping invalid intent uri", true);
                                        }
                                    }
                                    ContentValues contentValues = new ContentValues(cursor.getColumnCount());
                                    contentValues.put("_id", Integer.valueOf(cursor.getInt(columnIndexOrThrow)));
                                    contentValues.put("intent", string);
                                    contentValues.put("title", cursor.getString(columnIndexOrThrow3));
                                    contentValues.put("iconType", Integer.valueOf(cursor.getInt(columnIndexOrThrow4)));
                                    contentValues.put("icon", cursor.getBlob(columnIndexOrThrow5));
                                    contentValues.put("iconPackage", cursor.getString(columnIndexOrThrow6));
                                    contentValues.put("iconResource", cursor.getString(columnIndexOrThrow7));
                                    contentValues.put("itemType", Integer.valueOf(i6));
                                    contentValues.put("appWidgetId", (Integer) (-1));
                                    contentValues.put("uri", cursor.getString(columnIndexOrThrow13));
                                    contentValues.put("displayMode", Integer.valueOf(cursor.getInt(columnIndexOrThrow14)));
                                    contentValues.put("profileId", Long.valueOf(serialNumberForUser));
                                    if (i10 == -101) {
                                        sparseArray.put(i9, contentValues);
                                    }
                                    if (i10 != -100) {
                                        contentValues.put("screen", Integer.valueOf(i9));
                                        contentValues.put("cellX", Integer.valueOf(i7));
                                        contentValues.put("cellY", Integer.valueOf(i8));
                                    }
                                    contentValues.put("container", Integer.valueOf(i10));
                                    if (i6 != 2) {
                                        arrayList.add(contentValues);
                                    } else {
                                        arrayList2.add(contentValues);
                                    }
                                }
                            }
                        }
                        int size = sparseArray.size();
                        for (int i11 = 0; i11 < size; i11++) {
                            int keyAt = sparseArray.keyAt(i11);
                            ContentValues contentValues2 = (ContentValues) sparseArray.valueAt(i11);
                            int i12 = keyAt;
                            if (keyAt == invariantDeviceProfile.hotseatAllAppsRank) {
                                int i13 = keyAt;
                                while (true) {
                                    int i14 = i13 + 1;
                                    i12 = i14;
                                    if (i14 >= i5) {
                                        break;
                                    }
                                    i13 = i14;
                                    if (sparseArray.get(i14) == null) {
                                        contentValues2.put("screen", Integer.valueOf(i14));
                                        i12 = i14;
                                        break;
                                    }
                                }
                            }
                            if (i12 >= i5) {
                                contentValues2.put("container", (Integer) (-100));
                            }
                        }
                        ArrayList<ContentValues> arrayList3 = new ArrayList();
                        arrayList3.addAll(arrayList2);
                        arrayList3.addAll(arrayList);
                        int i15 = 0;
                        int i16 = 0;
                        i2 = 0;
                        for (ContentValues contentValues3 : arrayList3) {
                            if (contentValues3.getAsInteger("container").intValue() == -100) {
                                contentValues3.put("screen", Integer.valueOf(i2));
                                contentValues3.put("cellX", Integer.valueOf(i16));
                                contentValues3.put("cellY", Integer.valueOf(i15));
                                int i17 = (i16 + 1) % i3;
                                int i18 = i15;
                                if (i17 == 0) {
                                    i18 = i15 + 1;
                                }
                                i16 = i17;
                                i15 = i18;
                                if (i18 == i4 - 1) {
                                    i2 = (int) generateNewScreenId();
                                    i15 = 0;
                                    i16 = i17;
                                }
                            }
                        }
                        i = 0;
                        if (arrayList3.size() > 0) {
                            sQLiteDatabase.beginTransaction();
                            i = 0;
                            for (ContentValues contentValues4 : arrayList3) {
                                if (contentValues4 != null) {
                                    if (LauncherProvider.dbInsertAndCheck(this, sQLiteDatabase, "favorites", null, contentValues4) < 0) {
                                        sQLiteDatabase.endTransaction();
                                        cursor.close();
                                        return;
                                    }
                                    i++;
                                }
                            }
                            sQLiteDatabase.setTransactionSuccessful();
                            sQLiteDatabase.endTransaction();
                        }
                        sQLiteDatabase.beginTransaction();
                        for (int i19 = 0; i19 <= i2; i19++) {
                            ContentValues contentValues5 = new ContentValues();
                            contentValues5.put("_id", Integer.valueOf(i19));
                            contentValues5.put("screenRank", Integer.valueOf(i19));
                            if (LauncherProvider.dbInsertAndCheck(this, sQLiteDatabase, "workspaceScreens", null, contentValues5) < 0) {
                                sQLiteDatabase.endTransaction();
                                cursor.close();
                                return;
                            }
                        }
                        sQLiteDatabase.setTransactionSuccessful();
                        sQLiteDatabase.endTransaction();
                        updateFolderItemsRank(sQLiteDatabase, false);
                    }
                    cursor.close();
                } catch (Throwable th) {
                    cursor.close();
                    throw th;
                }
            }
            Launcher.addDumpLog("LauncherProvider", "migrated " + i + " icons from Launcher2 into " + (i2 + 1) + " screens", true);
            setFlagJustLoadedOldDb();
            this.mMaxItemId = initializeMaxItemId(sQLiteDatabase);
            this.mMaxScreenId = initializeMaxScreenId(sQLiteDatabase);
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onCreate(SQLiteDatabase sQLiteDatabase) {
            this.mMaxItemId = 1L;
            this.mMaxScreenId = 0L;
            this.mNewDbCreated = true;
            addFavoritesTable(sQLiteDatabase, false);
            addWorkspacesTable(sQLiteDatabase, false);
            if (this.mAppWidgetHost != null) {
                this.mAppWidgetHost.deleteHost();
                new MainThreadExecutor().execute(new Runnable(this) { // from class: com.android.launcher3.LauncherProvider.DatabaseHelper.1
                    final DatabaseHelper this$1;

                    {
                        this.this$1 = this;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        if (this.this$1.mListener != null) {
                            this.this$1.mListener.onAppWidgetHostReset();
                        }
                    }
                });
            }
            this.mMaxItemId = initializeMaxItemId(sQLiteDatabase);
            onEmptyDbCreated();
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onDowngrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
            Log.w("LauncherProvider", "Database version downgrade from: " + i + " to " + i2 + ". Wiping databse.");
            createEmptyDB(sQLiteDatabase);
        }

        protected void onEmptyDbCreated() {
            Utilities.getPrefs(this.mContext).edit().putBoolean("EMPTY_DATABASE_CREATED", true).commit();
            ManagedProfileHeuristic.processAllUsers(Collections.emptyList(), this.mContext);
        }

        /* JADX WARN: Code restructure failed: missing block: B:14:0x009c, code lost:
            if (addIntegerColumn(r7, "restored", 0) == false) goto L3;
         */
        /* JADX WARN: Code restructure failed: missing block: B:18:0x00b0, code lost:
            if (addProfileColumn(r7) != false) goto L35;
         */
        /* JADX WARN: Code restructure failed: missing block: B:20:0x00b9, code lost:
            if (updateFolderItemsRank(r7, true) != false) goto L37;
         */
        /* JADX WARN: Code restructure failed: missing block: B:22:0x00c1, code lost:
            if (recreateWorkspaceTable(r7) != false) goto L39;
         */
        /* JADX WARN: Code restructure failed: missing block: B:24:0x00cd, code lost:
            if (addIntegerColumn(r7, "options", 0) != false) goto L41;
         */
        @Override // android.database.sqlite.SQLiteOpenHelper
        /*
            Code decompiled incorrectly, please refer to instructions dump.
        */
        public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
            switch (i) {
                case 12:
                    this.mMaxScreenId = 0L;
                    addWorkspacesTable(sQLiteDatabase, false);
                    sQLiteDatabase.beginTransaction();
                    try {
                        sQLiteDatabase.execSQL("ALTER TABLE favorites ADD COLUMN appWidgetProvider TEXT;");
                        sQLiteDatabase.setTransactionSuccessful();
                        sQLiteDatabase.endTransaction();
                        sQLiteDatabase.beginTransaction();
                        try {
                            sQLiteDatabase.execSQL("ALTER TABLE favorites ADD COLUMN modified INTEGER NOT NULL DEFAULT 0;");
                            sQLiteDatabase.execSQL("ALTER TABLE workspaceScreens ADD COLUMN modified INTEGER NOT NULL DEFAULT 0;");
                            sQLiteDatabase.setTransactionSuccessful();
                            break;
                        } catch (SQLException e) {
                            Log.e("LauncherProvider", e.getMessage(), e);
                            break;
                        } finally {
                        }
                    } catch (SQLException e2) {
                        Log.e("LauncherProvider", e2.getMessage(), e2);
                        break;
                    } finally {
                    }
                case 13:
                    sQLiteDatabase.beginTransaction();
                    sQLiteDatabase.execSQL("ALTER TABLE favorites ADD COLUMN appWidgetProvider TEXT;");
                    sQLiteDatabase.setTransactionSuccessful();
                    sQLiteDatabase.endTransaction();
                    sQLiteDatabase.beginTransaction();
                    sQLiteDatabase.execSQL("ALTER TABLE favorites ADD COLUMN modified INTEGER NOT NULL DEFAULT 0;");
                    sQLiteDatabase.execSQL("ALTER TABLE workspaceScreens ADD COLUMN modified INTEGER NOT NULL DEFAULT 0;");
                    sQLiteDatabase.setTransactionSuccessful();
                    break;
                case 14:
                    sQLiteDatabase.beginTransaction();
                    sQLiteDatabase.execSQL("ALTER TABLE favorites ADD COLUMN modified INTEGER NOT NULL DEFAULT 0;");
                    sQLiteDatabase.execSQL("ALTER TABLE workspaceScreens ADD COLUMN modified INTEGER NOT NULL DEFAULT 0;");
                    sQLiteDatabase.setTransactionSuccessful();
                    break;
                case 15:
                    break;
                case 16:
                    LauncherClings.markFirstRunClingDismissed(this.mContext);
                    removeOrphanedItems(sQLiteDatabase);
                    break;
                case 17:
                case 18:
                    removeOrphanedItems(sQLiteDatabase);
                    break;
                case 19:
                    break;
                case 20:
                    break;
                case 21:
                    break;
                case 22:
                    break;
                case 23:
                case 24:
                    ManagedProfileHeuristic.markExistingUsersForNoFolderCreation(this.mContext);
                    convertShortcutsToLauncherActivities(sQLiteDatabase);
                    return;
                case 25:
                    convertShortcutsToLauncherActivities(sQLiteDatabase);
                    return;
                case 26:
                    return;
                default:
                    Log.w("LauncherProvider", "Destroying all old data.");
                    createEmptyDB(sQLiteDatabase);
                    return;
            }
        }

        public boolean recreateWorkspaceTable(SQLiteDatabase sQLiteDatabase) {
            sQLiteDatabase.beginTransaction();
            try {
                try {
                    Cursor query = sQLiteDatabase.query("workspaceScreens", new String[]{"_id"}, null, null, null, null, "screenRank");
                    ArrayList arrayList = new ArrayList();
                    long j = 0;
                    while (query.moveToNext()) {
                        try {
                            Long valueOf = Long.valueOf(query.getLong(0));
                            if (!arrayList.contains(valueOf)) {
                                arrayList.add(valueOf);
                                j = Math.max(j, valueOf.longValue());
                            }
                        } catch (Throwable th) {
                            query.close();
                            throw th;
                        }
                    }
                    query.close();
                    sQLiteDatabase.execSQL("DROP TABLE IF EXISTS workspaceScreens");
                    addWorkspacesTable(sQLiteDatabase, false);
                    int size = arrayList.size();
                    for (int i = 0; i < size; i++) {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("_id", (Long) arrayList.get(i));
                        contentValues.put("screenRank", Integer.valueOf(i));
                        LauncherProvider.addModifiedTime(contentValues);
                        sQLiteDatabase.insertOrThrow("workspaceScreens", null, contentValues);
                    }
                    sQLiteDatabase.setTransactionSuccessful();
                    this.mMaxScreenId = j;
                    sQLiteDatabase.endTransaction();
                    return true;
                } catch (SQLException e) {
                    Log.e("LauncherProvider", e.getMessage(), e);
                    sQLiteDatabase.endTransaction();
                    return false;
                }
            } catch (Throwable th2) {
                sQLiteDatabase.endTransaction();
                throw th2;
            }
        }

        boolean updateFolderItemsRank(SQLiteDatabase sQLiteDatabase, boolean z) {
            sQLiteDatabase.beginTransaction();
            if (z) {
                try {
                    try {
                        sQLiteDatabase.execSQL("ALTER TABLE favorites ADD COLUMN rank INTEGER NOT NULL DEFAULT 0;");
                    } catch (SQLException e) {
                        Log.e("LauncherProvider", e.getMessage(), e);
                        sQLiteDatabase.endTransaction();
                        return false;
                    }
                } catch (Throwable th) {
                    sQLiteDatabase.endTransaction();
                    throw th;
                }
            }
            Cursor rawQuery = sQLiteDatabase.rawQuery("SELECT container, MAX(cellX) FROM favorites WHERE container IN (SELECT _id FROM favorites WHERE itemType = ?) GROUP BY container;", new String[]{Integer.toString(2)});
            while (rawQuery.moveToNext()) {
                sQLiteDatabase.execSQL("UPDATE favorites SET rank=cellX+(cellY*?) WHERE container=? AND cellX IS NOT NULL AND cellY IS NOT NULL;", new Object[]{Long.valueOf(rawQuery.getLong(1) + 1), Long.valueOf(rawQuery.getLong(0))});
            }
            rawQuery.close();
            sQLiteDatabase.setTransactionSuccessful();
            sQLiteDatabase.endTransaction();
            return true;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:com/android/launcher3/LauncherProvider$SqlArguments.class */
    public static class SqlArguments {
        public final String[] args;
        public final String table;
        public final String where;

        SqlArguments(Uri uri) {
            if (uri.getPathSegments().size() != 1) {
                throw new IllegalArgumentException("Invalid URI: " + uri);
            }
            this.table = uri.getPathSegments().get(0);
            this.where = null;
            this.args = null;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public SqlArguments(Uri uri, String str, String[] strArr) {
            if (uri.getPathSegments().size() == 1) {
                this.table = uri.getPathSegments().get(0);
                this.where = str;
                this.args = strArr;
            } else if (uri.getPathSegments().size() != 2) {
                throw new IllegalArgumentException("Invalid URI: " + uri);
            } else {
                if (!TextUtils.isEmpty(str)) {
                    throw new UnsupportedOperationException("WHERE clause not supported: " + uri);
                }
                this.table = uri.getPathSegments().get(0);
                this.where = "_id=" + ContentUris.parseId(uri);
                this.args = null;
            }
        }
    }

    static void addModifiedTime(ContentValues contentValues) {
        contentValues.put("modified", Long.valueOf(System.currentTimeMillis()));
    }

    @TargetApi(18)
    private AutoInstallsLayout createWorkspaceLoaderFromAppRestriction() {
        String string;
        if (Utilities.ATLEAST_JB_MR2) {
            Context context = getContext();
            Bundle applicationRestrictions = ((UserManager) context.getSystemService("user")).getApplicationRestrictions(context.getPackageName());
            if (applicationRestrictions == null || (string = applicationRestrictions.getString("workspace.configuration.package.name")) == null) {
                return null;
            }
            try {
                return AutoInstallsLayout.get(context, string, context.getPackageManager().getResourcesForApplication(string), this.mOpenHelper.mAppWidgetHost, this.mOpenHelper);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e("LauncherProvider", "Target package for restricted profile not found", e);
                return null;
            }
        }
        return null;
    }

    static long dbInsertAndCheck(DatabaseHelper databaseHelper, SQLiteDatabase sQLiteDatabase, String str, String str2, ContentValues contentValues) {
        if (contentValues == null) {
            throw new RuntimeException("Error: attempting to insert null values");
        }
        if (contentValues.containsKey("_id")) {
            databaseHelper.checkId(str, contentValues);
            return sQLiteDatabase.insert(str, str2, contentValues);
        }
        throw new RuntimeException("Error: attempting to add item without specifying an id");
    }

    private DefaultLayoutParser getDefaultLayoutParser() {
        return new DefaultLayoutParser(getContext(), this.mOpenHelper.mAppWidgetHost, this.mOpenHelper, getContext().getResources(), LauncherAppState.getInstance().getInvariantDeviceProfile().defaultLayoutId);
    }

    static long getMaxId(SQLiteDatabase sQLiteDatabase, String str) {
        Cursor rawQuery = sQLiteDatabase.rawQuery("SELECT MAX(_id) FROM " + str, null);
        long j = -1;
        if (rawQuery != null) {
            j = -1;
            if (rawQuery.moveToNext()) {
                j = rawQuery.getLong(0);
            }
        }
        if (rawQuery != null) {
            rawQuery.close();
        }
        if (j == -1) {
            throw new RuntimeException("Error: could not query max id in " + str);
        }
        return j;
    }

    private void reloadLauncherIfExternal() {
        LauncherAppState instanceNoCreate;
        if (!Utilities.ATLEAST_MARSHMALLOW || Binder.getCallingPid() == Process.myPid() || (instanceNoCreate = LauncherAppState.getInstanceNoCreate()) == null) {
            return;
        }
        instanceNoCreate.reloadWorkspace();
    }

    @Override // android.content.ContentProvider
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> arrayList) throws OperationApplicationException {
        SQLiteDatabase writableDatabase = this.mOpenHelper.getWritableDatabase();
        writableDatabase.beginTransaction();
        try {
            ContentProviderResult[] applyBatch = super.applyBatch(arrayList);
            writableDatabase.setTransactionSuccessful();
            reloadLauncherIfExternal();
            return applyBatch;
        } finally {
            writableDatabase.endTransaction();
        }
    }

    @Override // android.content.ContentProvider
    public int bulkInsert(Uri uri, ContentValues[] contentValuesArr) {
        SqlArguments sqlArguments = new SqlArguments(uri);
        SQLiteDatabase writableDatabase = this.mOpenHelper.getWritableDatabase();
        writableDatabase.beginTransaction();
        try {
            int length = contentValuesArr.length;
            for (int i = 0; i < length; i++) {
                addModifiedTime(contentValuesArr[i]);
                if (dbInsertAndCheck(this.mOpenHelper, writableDatabase, sqlArguments.table, null, contentValuesArr[i]) < 0) {
                    writableDatabase.endTransaction();
                    return 0;
                }
            }
            writableDatabase.setTransactionSuccessful();
            writableDatabase.endTransaction();
            notifyListeners();
            reloadLauncherIfExternal();
            return contentValuesArr.length;
        } catch (Throwable th) {
            writableDatabase.endTransaction();
            throw th;
        }
    }

    @Override // android.content.ContentProvider
    public Bundle call(String str, String str2, Bundle bundle) {
        if (Binder.getCallingUid() != Process.myUid()) {
            return null;
        }
        if (str.equals("get_boolean_setting")) {
            Bundle bundle2 = new Bundle();
            if ("pref_allowRotation".equals(str2)) {
                bundle2.putBoolean("value", Utilities.isAllowRotationPrefEnabled(getContext()));
            } else {
                bundle2.putBoolean("value", Utilities.getPrefs(getContext()).getBoolean(str2, bundle.getBoolean("default_value")));
            }
            return bundle2;
        } else if (str.equals("set_boolean_setting")) {
            boolean z = bundle.getBoolean("value");
            Utilities.getPrefs(getContext()).edit().putBoolean(str2, z).apply();
            if (this.mListener != null) {
                this.mListener.onSettingsChanged(str2, z);
            }
            if (bundle.getBoolean("notify_backup")) {
                LauncherBackupAgentHelper.dataChanged(getContext());
            }
            Bundle bundle3 = new Bundle();
            bundle3.putBoolean("value", z);
            return bundle3;
        } else {
            return null;
        }
    }

    public void clearFlagEmptyDbCreated() {
        Utilities.getPrefs(getContext()).edit().remove("EMPTY_DATABASE_CREATED").commit();
    }

    public void convertShortcutsToLauncherActivities() {
        this.mOpenHelper.convertShortcutsToLauncherActivities(this.mOpenHelper.getWritableDatabase());
    }

    public void createEmptyDB() {
        synchronized (this) {
            this.mOpenHelper.createEmptyDB(this.mOpenHelper.getWritableDatabase());
        }
    }

    @Override // android.content.ContentProvider
    public int delete(Uri uri, String str, String[] strArr) {
        SqlArguments sqlArguments = new SqlArguments(uri, str, strArr);
        int delete = this.mOpenHelper.getWritableDatabase().delete(sqlArguments.table, sqlArguments.where, sqlArguments.args);
        if (delete > 0) {
            notifyListeners();
        }
        reloadLauncherIfExternal();
        return delete;
    }

    public void deleteDatabase() {
        this.mOpenHelper.createEmptyDB(this.mOpenHelper.getWritableDatabase());
    }

    public List<Long> deleteEmptyFolders() {
        ArrayList arrayList = new ArrayList();
        SQLiteDatabase writableDatabase = this.mOpenHelper.getWritableDatabase();
        writableDatabase.beginTransaction();
        try {
            Cursor query = writableDatabase.query("favorites", new String[]{"_id"}, "itemType = 2 AND _id NOT IN (SELECT container FROM favorites)", null, null, null, null);
            while (query.moveToNext()) {
                arrayList.add(Long.valueOf(query.getLong(0)));
            }
            query.close();
            if (arrayList.size() > 0) {
                writableDatabase.delete("favorites", Utilities.createDbSelectionQuery("_id", arrayList), null);
            }
            writableDatabase.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e("LauncherProvider", e.getMessage(), e);
            arrayList.clear();
        } finally {
            writableDatabase.endTransaction();
        }
        return arrayList;
    }

    public long generateNewItemId() {
        return this.mOpenHelper.generateNewItemId();
    }

    public long generateNewScreenId() {
        return this.mOpenHelper.generateNewScreenId();
    }

    @Override // android.content.ContentProvider
    public String getType(Uri uri) {
        SqlArguments sqlArguments = new SqlArguments(uri, null, null);
        return TextUtils.isEmpty(sqlArguments.where) ? "vnd.android.cursor.dir/" + sqlArguments.table : "vnd.android.cursor.item/" + sqlArguments.table;
    }

    @Override // android.content.ContentProvider
    public Uri insert(Uri uri, ContentValues contentValues) {
        SqlArguments sqlArguments = new SqlArguments(uri);
        if (Binder.getCallingPid() == Process.myPid() || this.mOpenHelper.initializeExternalAdd(contentValues)) {
            SQLiteDatabase writableDatabase = this.mOpenHelper.getWritableDatabase();
            addModifiedTime(contentValues);
            long dbInsertAndCheck = dbInsertAndCheck(this.mOpenHelper, writableDatabase, sqlArguments.table, null, contentValues);
            if (dbInsertAndCheck < 0) {
                return null;
            }
            Uri withAppendedId = ContentUris.withAppendedId(uri, dbInsertAndCheck);
            notifyListeners();
            if (Utilities.ATLEAST_MARSHMALLOW) {
                reloadLauncherIfExternal();
            } else {
                LauncherAppState instanceNoCreate = LauncherAppState.getInstanceNoCreate();
                if (instanceNoCreate != null && "true".equals(withAppendedId.getQueryParameter("isExternalAdd"))) {
                    instanceNoCreate.reloadWorkspace();
                }
                String queryParameter = withAppendedId.getQueryParameter("notify");
                if (queryParameter == null || "true".equals(queryParameter)) {
                    getContext().getContentResolver().notifyChange(withAppendedId, null);
                }
            }
            return withAppendedId;
        }
        return null;
    }

    public void loadDefaultFavoritesIfNecessary() {
        synchronized (this) {
            if (Utilities.getPrefs(getContext()).getBoolean("EMPTY_DATABASE_CREATED", false)) {
                Log.d("LauncherProvider", "loading default workspace");
                AutoInstallsLayout createWorkspaceLoaderFromAppRestriction = createWorkspaceLoaderFromAppRestriction();
                AutoInstallsLayout autoInstallsLayout = createWorkspaceLoaderFromAppRestriction;
                if (createWorkspaceLoaderFromAppRestriction == null) {
                    autoInstallsLayout = AutoInstallsLayout.get(getContext(), this.mOpenHelper.mAppWidgetHost, this.mOpenHelper);
                }
                AutoInstallsLayout autoInstallsLayout2 = autoInstallsLayout;
                if (autoInstallsLayout == null) {
                    Partner partner = Partner.get(getContext().getPackageManager());
                    autoInstallsLayout2 = autoInstallsLayout;
                    if (partner != null) {
                        autoInstallsLayout2 = autoInstallsLayout;
                        if (partner.hasDefaultLayout()) {
                            Resources resources = partner.getResources();
                            int identifier = resources.getIdentifier("partner_default_layout", "xml", partner.getPackageName());
                            autoInstallsLayout2 = autoInstallsLayout;
                            if (identifier != 0) {
                                autoInstallsLayout2 = new DefaultLayoutParser(getContext(), this.mOpenHelper.mAppWidgetHost, this.mOpenHelper, resources, identifier);
                            }
                        }
                    }
                }
                boolean z = autoInstallsLayout2 != null;
                DefaultLayoutParser defaultLayoutParser = autoInstallsLayout2;
                if (autoInstallsLayout2 == null) {
                    defaultLayoutParser = getDefaultLayoutParser();
                }
                if (this.mOpenHelper.loadFavorites(this.mOpenHelper.getWritableDatabase(), defaultLayoutParser) <= 0 && z) {
                    createEmptyDB();
                    this.mOpenHelper.loadFavorites(this.mOpenHelper.getWritableDatabase(), getDefaultLayoutParser());
                }
                clearFlagEmptyDbCreated();
            }
        }
    }

    public void migrateLauncher2Shortcuts() {
        this.mOpenHelper.migrateLauncher2Shortcuts(this.mOpenHelper.getWritableDatabase(), Uri.parse(getContext().getString(2131558401)));
    }

    protected void notifyListeners() {
        LauncherBackupAgentHelper.dataChanged(getContext());
        if (this.mListener != null) {
            this.mListener.onLauncherProviderChange();
        }
    }

    @Override // android.content.ContentProvider
    public boolean onCreate() {
        LauncherLog.d("LauncherProvider", "(LauncherProvider)onCreate");
        Context context = getContext();
        LauncherAppState.setApplicationContext(context.getApplicationContext());
        StrictMode.ThreadPolicy allowThreadDiskWrites = StrictMode.allowThreadDiskWrites();
        this.mOpenHelper = new DatabaseHelper(context);
        StrictMode.setThreadPolicy(allowThreadDiskWrites);
        LauncherAppState.setLauncherProvider(this);
        return true;
    }

    @Override // android.content.ContentProvider
    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        SqlArguments sqlArguments = new SqlArguments(uri, str, strArr2);
        SQLiteQueryBuilder sQLiteQueryBuilder = new SQLiteQueryBuilder();
        sQLiteQueryBuilder.setTables(sqlArguments.table);
        Cursor query = sQLiteQueryBuilder.query(this.mOpenHelper.getWritableDatabase(), strArr, sqlArguments.where, sqlArguments.args, null, null, str2);
        if (query != null) {
            query.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return query;
    }

    public void setLauncherProviderChangeListener(LauncherProviderChangeListener launcherProviderChangeListener) {
        this.mListener = launcherProviderChangeListener;
        this.mOpenHelper.mListener = this.mListener;
    }

    @Override // android.content.ContentProvider
    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        SqlArguments sqlArguments = new SqlArguments(uri, str, strArr);
        addModifiedTime(contentValues);
        int update = this.mOpenHelper.getWritableDatabase().update(sqlArguments.table, contentValues, sqlArguments.where, sqlArguments.args);
        if (update > 0) {
            notifyListeners();
        }
        reloadLauncherIfExternal();
        return update;
    }

    public void updateFolderItemsRank() {
        this.mOpenHelper.updateFolderItemsRank(this.mOpenHelper.getWritableDatabase(), false);
    }
}
