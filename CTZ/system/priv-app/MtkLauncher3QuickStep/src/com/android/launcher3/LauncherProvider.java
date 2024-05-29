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
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.launcher3.AutoInstallsLayout;
import com.android.launcher3.LauncherSettings;
import com.android.launcher3.compat.UserManagerCompat;
import com.android.launcher3.config.FeatureFlags;
import com.android.launcher3.logging.FileLog;
import com.android.launcher3.model.DbDowngradeHelper;
import com.android.launcher3.provider.LauncherDbUtils;
import com.android.launcher3.provider.RestoreDbTask;
import com.android.launcher3.util.NoLocaleSQLiteHelper;
import com.android.launcher3.util.Preconditions;
import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
/* loaded from: classes.dex */
public class LauncherProvider extends ContentProvider {
    public static final String AUTHORITY = FeatureFlags.AUTHORITY;
    private static final String DOWNGRADE_SCHEMA_FILE = "downgrade_schema.json";
    static final String EMPTY_DATABASE_CREATED = "EMPTY_DATABASE_CREATED";
    private static final boolean LOGD = false;
    private static final String RESTRICTION_PACKAGE_NAME = "workspace.configuration.package.name";
    public static final int SCHEMA_VERSION = 27;
    private static final String TAG = "LauncherProvider";
    private Handler mListenerHandler;
    private final ChangeListenerWrapper mListenerWrapper = new ChangeListenerWrapper();
    protected DatabaseHelper mOpenHelper;

    @Override // android.content.ContentProvider
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        LauncherAppState instanceNoCreate = LauncherAppState.getInstanceNoCreate();
        if (instanceNoCreate == null || !instanceNoCreate.getModel().isModelLoaded()) {
            return;
        }
        instanceNoCreate.getModel().dumpState("", fileDescriptor, printWriter, strArr);
    }

    @Override // android.content.ContentProvider
    public boolean onCreate() {
        this.mListenerHandler = new Handler(this.mListenerWrapper);
        MainProcessInitializer.initialize(getContext().getApplicationContext());
        return true;
    }

    public void setLauncherProviderChangeListener(LauncherProviderChangeListener launcherProviderChangeListener) {
        Preconditions.assertUIThread();
        this.mListenerWrapper.mListener = launcherProviderChangeListener;
    }

    @Override // android.content.ContentProvider
    public String getType(Uri uri) {
        SqlArguments sqlArguments = new SqlArguments(uri, null, null);
        if (TextUtils.isEmpty(sqlArguments.where)) {
            return "vnd.android.cursor.dir/" + sqlArguments.table;
        }
        return "vnd.android.cursor.item/" + sqlArguments.table;
    }

    protected synchronized void createDbIfNotExists() {
        if (this.mOpenHelper == null) {
            this.mOpenHelper = new DatabaseHelper(getContext(), this.mListenerHandler);
            if (RestoreDbTask.isPending(getContext())) {
                if (!RestoreDbTask.performRestore(this.mOpenHelper)) {
                    this.mOpenHelper.createEmptyDB(this.mOpenHelper.getWritableDatabase());
                }
                RestoreDbTask.setPending(getContext(), false);
            }
        }
    }

    @Override // android.content.ContentProvider
    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        createDbIfNotExists();
        SqlArguments sqlArguments = new SqlArguments(uri, str, strArr2);
        SQLiteQueryBuilder sQLiteQueryBuilder = new SQLiteQueryBuilder();
        sQLiteQueryBuilder.setTables(sqlArguments.table);
        Cursor query = sQLiteQueryBuilder.query(this.mOpenHelper.getWritableDatabase(), strArr, sqlArguments.where, sqlArguments.args, null, null, str2);
        query.setNotificationUri(getContext().getContentResolver(), uri);
        return query;
    }

    static long dbInsertAndCheck(DatabaseHelper databaseHelper, SQLiteDatabase sQLiteDatabase, String str, String str2, ContentValues contentValues) {
        if (contentValues == null) {
            throw new RuntimeException("Error: attempting to insert null values");
        }
        if (!contentValues.containsKey("_id")) {
            throw new RuntimeException("Error: attempting to add item without specifying an id");
        }
        databaseHelper.checkId(str, contentValues);
        return sQLiteDatabase.insert(str, str2, contentValues);
    }

    private void reloadLauncherIfExternal() {
        LauncherAppState instanceNoCreate;
        if (Utilities.ATLEAST_MARSHMALLOW && Binder.getCallingPid() != Process.myPid() && (instanceNoCreate = LauncherAppState.getInstanceNoCreate()) != null) {
            instanceNoCreate.getModel().forceReload();
        }
    }

    @Override // android.content.ContentProvider
    public Uri insert(Uri uri, ContentValues contentValues) {
        createDbIfNotExists();
        SqlArguments sqlArguments = new SqlArguments(uri);
        if (Binder.getCallingPid() == Process.myPid() || initializeExternalAdd(contentValues)) {
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
                    instanceNoCreate.getModel().forceReload();
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

    private boolean initializeExternalAdd(ContentValues contentValues) {
        SQLiteStatement sQLiteStatement;
        Throwable th;
        contentValues.put("_id", Long.valueOf(this.mOpenHelper.generateNewItemId()));
        Integer asInteger = contentValues.getAsInteger(LauncherSettings.BaseLauncherColumns.ITEM_TYPE);
        if (asInteger != null && asInteger.intValue() == 4 && !contentValues.containsKey(LauncherSettings.Favorites.APPWIDGET_ID)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getContext());
            ComponentName unflattenFromString = ComponentName.unflattenFromString(contentValues.getAsString(LauncherSettings.Favorites.APPWIDGET_PROVIDER));
            if (unflattenFromString == null) {
                return false;
            }
            try {
                AppWidgetHost newLauncherWidgetHost = this.mOpenHelper.newLauncherWidgetHost();
                int allocateAppWidgetId = newLauncherWidgetHost.allocateAppWidgetId();
                contentValues.put(LauncherSettings.Favorites.APPWIDGET_ID, Integer.valueOf(allocateAppWidgetId));
                if (!appWidgetManager.bindAppWidgetIdIfAllowed(allocateAppWidgetId, unflattenFromString)) {
                    newLauncherWidgetHost.deleteAppWidgetId(allocateAppWidgetId);
                    return false;
                }
            } catch (RuntimeException e) {
                Log.e(TAG, "Failed to initialize external widget", e);
                return false;
            }
        }
        long longValue = contentValues.getAsLong(LauncherSettings.Favorites.SCREEN).longValue();
        try {
            sQLiteStatement = this.mOpenHelper.getWritableDatabase().compileStatement("INSERT OR IGNORE INTO workspaceScreens (_id, screenRank) select ?, (ifnull(MAX(screenRank), -1)+1) from workspaceScreens");
        } catch (Exception e2) {
            sQLiteStatement = null;
        } catch (Throwable th2) {
            sQLiteStatement = null;
            th = th2;
        }
        try {
            sQLiteStatement.bindLong(1, longValue);
            ContentValues contentValues2 = new ContentValues();
            contentValues2.put("_id", Long.valueOf(sQLiteStatement.executeInsert()));
            this.mOpenHelper.checkId(LauncherSettings.WorkspaceScreens.TABLE_NAME, contentValues2);
            Utilities.closeSilently(sQLiteStatement);
            return true;
        } catch (Exception e3) {
            Utilities.closeSilently(sQLiteStatement);
            return false;
        } catch (Throwable th3) {
            th = th3;
            Utilities.closeSilently(sQLiteStatement);
            throw th;
        }
    }

    @Override // android.content.ContentProvider
    public int bulkInsert(Uri uri, ContentValues[] contentValuesArr) {
        createDbIfNotExists();
        SqlArguments sqlArguments = new SqlArguments(uri);
        SQLiteDatabase writableDatabase = this.mOpenHelper.getWritableDatabase();
        LauncherDbUtils.SQLiteTransaction sQLiteTransaction = new LauncherDbUtils.SQLiteTransaction(writableDatabase);
        try {
            int length = contentValuesArr.length;
            for (int i = 0; i < length; i++) {
                addModifiedTime(contentValuesArr[i]);
                if (dbInsertAndCheck(this.mOpenHelper, writableDatabase, sqlArguments.table, null, contentValuesArr[i]) < 0) {
                    $closeResource(null, sQLiteTransaction);
                    return 0;
                }
            }
            sQLiteTransaction.commit();
            $closeResource(null, sQLiteTransaction);
            notifyListeners();
            reloadLauncherIfExternal();
            return contentValuesArr.length;
        } finally {
        }
    }

    private static /* synthetic */ void $closeResource(Throwable th, AutoCloseable autoCloseable) {
        if (th == null) {
            autoCloseable.close();
            return;
        }
        try {
            autoCloseable.close();
        } catch (Throwable th2) {
            th.addSuppressed(th2);
        }
    }

    @Override // android.content.ContentProvider
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> arrayList) throws OperationApplicationException {
        createDbIfNotExists();
        LauncherDbUtils.SQLiteTransaction sQLiteTransaction = new LauncherDbUtils.SQLiteTransaction(this.mOpenHelper.getWritableDatabase());
        try {
            ContentProviderResult[] applyBatch = super.applyBatch(arrayList);
            sQLiteTransaction.commit();
            reloadLauncherIfExternal();
            $closeResource(null, sQLiteTransaction);
            return applyBatch;
        } finally {
        }
    }

    @Override // android.content.ContentProvider
    public int delete(Uri uri, String str, String[] strArr) {
        createDbIfNotExists();
        SqlArguments sqlArguments = new SqlArguments(uri, str, strArr);
        SQLiteDatabase writableDatabase = this.mOpenHelper.getWritableDatabase();
        if (Binder.getCallingPid() != Process.myPid() && LauncherSettings.Favorites.TABLE_NAME.equalsIgnoreCase(sqlArguments.table)) {
            this.mOpenHelper.removeGhostWidgets(this.mOpenHelper.getWritableDatabase());
        }
        int delete = writableDatabase.delete(sqlArguments.table, sqlArguments.where, sqlArguments.args);
        if (delete > 0) {
            notifyListeners();
            reloadLauncherIfExternal();
        }
        return delete;
    }

    @Override // android.content.ContentProvider
    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        createDbIfNotExists();
        SqlArguments sqlArguments = new SqlArguments(uri, str, strArr);
        addModifiedTime(contentValues);
        int update = this.mOpenHelper.getWritableDatabase().update(sqlArguments.table, contentValues, sqlArguments.where, sqlArguments.args);
        if (update > 0) {
            notifyListeners();
        }
        reloadLauncherIfExternal();
        return update;
    }

    @Override // android.content.ContentProvider
    public Bundle call(String str, String str2, Bundle bundle) {
        if (Binder.getCallingUid() != Process.myUid()) {
            return null;
        }
        createDbIfNotExists();
        char c = 65535;
        switch (str.hashCode()) {
            case -1999597249:
                if (str.equals(LauncherSettings.Settings.METHOD_DELETE_EMPTY_FOLDERS)) {
                    c = 2;
                    break;
                }
                break;
            case -1565944700:
                if (str.equals(LauncherSettings.Settings.METHOD_REMOVE_GHOST_WIDGETS)) {
                    c = 7;
                    break;
                }
                break;
            case -1107339682:
                if (str.equals(LauncherSettings.Settings.METHOD_NEW_ITEM_ID)) {
                    c = 3;
                    break;
                }
                break;
            case -1029923675:
                if (str.equals(LauncherSettings.Settings.METHOD_NEW_SCREEN_ID)) {
                    c = 4;
                    break;
                }
                break;
            case -1008511191:
                if (str.equals(LauncherSettings.Settings.METHOD_CLEAR_EMPTY_DB_FLAG)) {
                    c = 0;
                    break;
                }
                break;
            case 476749504:
                if (str.equals(LauncherSettings.Settings.METHOD_LOAD_DEFAULT_FAVORITES)) {
                    c = 6;
                    break;
                }
                break;
            case 684076146:
                if (str.equals(LauncherSettings.Settings.METHOD_WAS_EMPTY_DB_CREATED)) {
                    c = 1;
                    break;
                }
                break;
            case 2117515411:
                if (str.equals(LauncherSettings.Settings.METHOD_CREATE_EMPTY_DB)) {
                    c = 5;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                clearFlagEmptyDbCreated();
                return null;
            case 1:
                Bundle bundle2 = new Bundle();
                bundle2.putBoolean(LauncherSettings.Settings.EXTRA_VALUE, Utilities.getPrefs(getContext()).getBoolean(EMPTY_DATABASE_CREATED, false));
                return bundle2;
            case 2:
                Bundle bundle3 = new Bundle();
                bundle3.putSerializable(LauncherSettings.Settings.EXTRA_VALUE, deleteEmptyFolders());
                return bundle3;
            case 3:
                Bundle bundle4 = new Bundle();
                bundle4.putLong(LauncherSettings.Settings.EXTRA_VALUE, this.mOpenHelper.generateNewItemId());
                return bundle4;
            case 4:
                Bundle bundle5 = new Bundle();
                bundle5.putLong(LauncherSettings.Settings.EXTRA_VALUE, this.mOpenHelper.generateNewScreenId());
                return bundle5;
            case 5:
                this.mOpenHelper.createEmptyDB(this.mOpenHelper.getWritableDatabase());
                return null;
            case 6:
                loadDefaultFavoritesIfNecessary();
                return null;
            case 7:
                this.mOpenHelper.removeGhostWidgets(this.mOpenHelper.getWritableDatabase());
                return null;
            default:
                return null;
        }
    }

    private ArrayList<Long> deleteEmptyFolders() {
        LauncherDbUtils.SQLiteTransaction sQLiteTransaction;
        Cursor query;
        ArrayList<Long> arrayList = new ArrayList<>();
        SQLiteDatabase writableDatabase = this.mOpenHelper.getWritableDatabase();
        try {
            sQLiteTransaction = new LauncherDbUtils.SQLiteTransaction(writableDatabase);
            query = writableDatabase.query(LauncherSettings.Favorites.TABLE_NAME, new String[]{"_id"}, "itemType = 2 AND _id NOT IN (SELECT container FROM favorites)", null, null, null, null);
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
            arrayList.clear();
        }
        try {
            LauncherDbUtils.iterateCursor(query, 0, arrayList);
            if (query != null) {
                $closeResource(null, query);
            }
            if (!arrayList.isEmpty()) {
                writableDatabase.delete(LauncherSettings.Favorites.TABLE_NAME, Utilities.createDbSelectionQuery("_id", arrayList), null);
            }
            sQLiteTransaction.commit();
            $closeResource(null, sQLiteTransaction);
            return arrayList;
        } finally {
        }
    }

    protected void notifyListeners() {
        this.mListenerHandler.sendEmptyMessage(1);
    }

    static void addModifiedTime(ContentValues contentValues) {
        contentValues.put(LauncherSettings.ChangeLogColumns.MODIFIED, Long.valueOf(System.currentTimeMillis()));
    }

    private void clearFlagEmptyDbCreated() {
        Utilities.getPrefs(getContext()).edit().remove(EMPTY_DATABASE_CREATED).commit();
    }

    private synchronized void loadDefaultFavoritesIfNecessary() {
        Partner partner;
        Resources resources;
        int identifier;
        if (Utilities.getPrefs(getContext()).getBoolean(EMPTY_DATABASE_CREATED, false)) {
            Log.d(TAG, "loading default workspace");
            AppWidgetHost newLauncherWidgetHost = this.mOpenHelper.newLauncherWidgetHost();
            AutoInstallsLayout createWorkspaceLoaderFromAppRestriction = createWorkspaceLoaderFromAppRestriction(newLauncherWidgetHost);
            if (createWorkspaceLoaderFromAppRestriction == null) {
                createWorkspaceLoaderFromAppRestriction = AutoInstallsLayout.get(getContext(), newLauncherWidgetHost, this.mOpenHelper);
            }
            if (createWorkspaceLoaderFromAppRestriction == null && (partner = Partner.get(getContext().getPackageManager())) != null && partner.hasDefaultLayout() && (identifier = (resources = partner.getResources()).getIdentifier(Partner.RES_DEFAULT_LAYOUT, "xml", partner.getPackageName())) != 0) {
                createWorkspaceLoaderFromAppRestriction = new DefaultLayoutParser(getContext(), newLauncherWidgetHost, this.mOpenHelper, resources, identifier);
            }
            boolean z = createWorkspaceLoaderFromAppRestriction != null;
            if (createWorkspaceLoaderFromAppRestriction == null) {
                createWorkspaceLoaderFromAppRestriction = getDefaultLayoutParser(newLauncherWidgetHost);
            }
            this.mOpenHelper.createEmptyDB(this.mOpenHelper.getWritableDatabase());
            if (this.mOpenHelper.loadFavorites(this.mOpenHelper.getWritableDatabase(), createWorkspaceLoaderFromAppRestriction) <= 0 && z) {
                this.mOpenHelper.createEmptyDB(this.mOpenHelper.getWritableDatabase());
                this.mOpenHelper.loadFavorites(this.mOpenHelper.getWritableDatabase(), getDefaultLayoutParser(newLauncherWidgetHost));
            }
            clearFlagEmptyDbCreated();
        }
    }

    private AutoInstallsLayout createWorkspaceLoaderFromAppRestriction(AppWidgetHost appWidgetHost) {
        String string;
        Context context = getContext();
        Bundle applicationRestrictions = ((UserManager) context.getSystemService("user")).getApplicationRestrictions(context.getPackageName());
        if (applicationRestrictions == null || (string = applicationRestrictions.getString(RESTRICTION_PACKAGE_NAME)) == null) {
            return null;
        }
        try {
            return AutoInstallsLayout.get(context, string, context.getPackageManager().getResourcesForApplication(string), appWidgetHost, this.mOpenHelper);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Target package for restricted profile not found", e);
            return null;
        }
    }

    private DefaultLayoutParser getDefaultLayoutParser(AppWidgetHost appWidgetHost) {
        InvariantDeviceProfile idp = LauncherAppState.getIDP(getContext());
        int i = idp.defaultLayoutId;
        if (UserManagerCompat.getInstance(getContext()).isDemoUser() && idp.demoModeLayoutId != 0) {
            i = idp.demoModeLayoutId;
        }
        return new DefaultLayoutParser(getContext(), appWidgetHost, this.mOpenHelper, getContext().getResources(), i);
    }

    /* loaded from: classes.dex */
    public static class DatabaseHelper extends NoLocaleSQLiteHelper implements AutoInstallsLayout.LayoutParserCallback {
        private final Context mContext;
        private long mMaxItemId;
        private long mMaxScreenId;
        private final Handler mWidgetHostResetHandler;

        DatabaseHelper(Context context, Handler handler) {
            this(context, handler, LauncherFiles.LAUNCHER_DB);
            if (!tableExists(LauncherSettings.Favorites.TABLE_NAME) || !tableExists(LauncherSettings.WorkspaceScreens.TABLE_NAME)) {
                Log.e(LauncherProvider.TAG, "Tables are missing after onCreate has been called. Trying to recreate");
                addFavoritesTable(getWritableDatabase(), true);
                addWorkspacesTable(getWritableDatabase(), true);
            }
            initIds();
        }

        public DatabaseHelper(Context context, Handler handler, String str) {
            super(context, str, 27);
            this.mMaxItemId = -1L;
            this.mMaxScreenId = -1L;
            this.mContext = context;
            this.mWidgetHostResetHandler = handler;
        }

        protected void initIds() {
            if (this.mMaxItemId == -1) {
                this.mMaxItemId = initializeMaxItemId(getWritableDatabase());
            }
            if (this.mMaxScreenId == -1) {
                this.mMaxScreenId = initializeMaxScreenId(getWritableDatabase());
            }
        }

        private boolean tableExists(String str) {
            Cursor query = getReadableDatabase().query(true, "sqlite_master", new String[]{"tbl_name"}, "tbl_name = ?", new String[]{str}, null, null, null, null, null);
            try {
                return query.getCount() > 0;
            } finally {
                query.close();
            }
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onCreate(SQLiteDatabase sQLiteDatabase) {
            this.mMaxItemId = 1L;
            this.mMaxScreenId = 0L;
            addFavoritesTable(sQLiteDatabase, false);
            addWorkspacesTable(sQLiteDatabase, false);
            this.mMaxItemId = initializeMaxItemId(sQLiteDatabase);
            onEmptyDbCreated();
        }

        protected void onEmptyDbCreated() {
            if (this.mWidgetHostResetHandler != null) {
                newLauncherWidgetHost().deleteHost();
                this.mWidgetHostResetHandler.sendEmptyMessage(2);
            }
            Utilities.getPrefs(this.mContext).edit().putBoolean(LauncherProvider.EMPTY_DATABASE_CREATED, true).commit();
        }

        public long getDefaultUserSerial() {
            return UserManagerCompat.getInstance(this.mContext).getSerialNumberForUser(Process.myUserHandle());
        }

        private void addFavoritesTable(SQLiteDatabase sQLiteDatabase, boolean z) {
            LauncherSettings.Favorites.addTableToDb(sQLiteDatabase, getDefaultUserSerial(), z);
        }

        private void addWorkspacesTable(SQLiteDatabase sQLiteDatabase, boolean z) {
            String str = z ? " IF NOT EXISTS " : "";
            sQLiteDatabase.execSQL("CREATE TABLE " + str + LauncherSettings.WorkspaceScreens.TABLE_NAME + " (_id INTEGER PRIMARY KEY," + LauncherSettings.WorkspaceScreens.SCREEN_RANK + " INTEGER," + LauncherSettings.ChangeLogColumns.MODIFIED + " INTEGER NOT NULL DEFAULT 0);");
        }

        private void removeOrphanedItems(SQLiteDatabase sQLiteDatabase) {
            sQLiteDatabase.execSQL("DELETE FROM favorites WHERE screen NOT IN (SELECT _id FROM workspaceScreens) AND container = -100");
            sQLiteDatabase.execSQL("DELETE FROM favorites WHERE container <> -100 AND container <> -101 AND container NOT IN (SELECT _id FROM favorites WHERE itemType = 2)");
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onOpen(SQLiteDatabase sQLiteDatabase) {
            super.onOpen(sQLiteDatabase);
            File fileStreamPath = this.mContext.getFileStreamPath(LauncherProvider.DOWNGRADE_SCHEMA_FILE);
            if (!fileStreamPath.exists()) {
                handleOneTimeDataUpgrade(sQLiteDatabase);
            }
            DbDowngradeHelper.updateSchemaFile(fileStreamPath, 27, this.mContext, R.raw.downgrade_schema);
        }

        protected void handleOneTimeDataUpgrade(SQLiteDatabase sQLiteDatabase) {
            UserManagerCompat userManagerCompat = UserManagerCompat.getInstance(this.mContext);
            for (UserHandle userHandle : userManagerCompat.getUserProfiles()) {
                long serialNumberForUser = userManagerCompat.getSerialNumberForUser(userHandle);
                sQLiteDatabase.execSQL("update favorites set intent = replace(intent, ';l.profile=" + serialNumberForUser + ";', ';') where itemType = 0;");
            }
        }

        /* JADX WARN: Code restructure failed: missing block: B:13:0x003b, code lost:
            if (addIntegerColumn(r4, com.android.launcher3.LauncherSettings.Favorites.RESTORED, 0) != false) goto L6;
         */
        /* JADX WARN: Code restructure failed: missing block: B:17:0x0045, code lost:
            if (addProfileColumn(r4) != false) goto L9;
         */
        /* JADX WARN: Code restructure failed: missing block: B:20:0x004d, code lost:
            if (updateFolderItemsRank(r4, true) == false) goto L18;
         */
        /* JADX WARN: Code restructure failed: missing block: B:23:0x0054, code lost:
            if (recreateWorkspaceTable(r4) == false) goto L18;
         */
        /* JADX WARN: Code restructure failed: missing block: B:26:0x005d, code lost:
            if (addIntegerColumn(r4, com.android.launcher3.LauncherSettings.Favorites.OPTIONS, 0) == false) goto L18;
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
                    try {
                        LauncherDbUtils.SQLiteTransaction sQLiteTransaction = new LauncherDbUtils.SQLiteTransaction(sQLiteDatabase);
                        sQLiteDatabase.execSQL("ALTER TABLE favorites ADD COLUMN appWidgetProvider TEXT;");
                        sQLiteTransaction.commit();
                        $closeResource(null, sQLiteTransaction);
                        try {
                            LauncherDbUtils.SQLiteTransaction sQLiteTransaction2 = new LauncherDbUtils.SQLiteTransaction(sQLiteDatabase);
                            sQLiteDatabase.execSQL("ALTER TABLE favorites ADD COLUMN modified INTEGER NOT NULL DEFAULT 0;");
                            sQLiteDatabase.execSQL("ALTER TABLE workspaceScreens ADD COLUMN modified INTEGER NOT NULL DEFAULT 0;");
                            sQLiteTransaction2.commit();
                            $closeResource(null, sQLiteTransaction2);
                            break;
                        } catch (SQLException e) {
                            Log.e(LauncherProvider.TAG, e.getMessage(), e);
                            break;
                        }
                    } catch (SQLException e2) {
                        Log.e(LauncherProvider.TAG, e2.getMessage(), e2);
                        break;
                    }
                case 13:
                    LauncherDbUtils.SQLiteTransaction sQLiteTransaction3 = new LauncherDbUtils.SQLiteTransaction(sQLiteDatabase);
                    sQLiteDatabase.execSQL("ALTER TABLE favorites ADD COLUMN appWidgetProvider TEXT;");
                    sQLiteTransaction3.commit();
                    $closeResource(null, sQLiteTransaction3);
                    LauncherDbUtils.SQLiteTransaction sQLiteTransaction22 = new LauncherDbUtils.SQLiteTransaction(sQLiteDatabase);
                    sQLiteDatabase.execSQL("ALTER TABLE favorites ADD COLUMN modified INTEGER NOT NULL DEFAULT 0;");
                    sQLiteDatabase.execSQL("ALTER TABLE workspaceScreens ADD COLUMN modified INTEGER NOT NULL DEFAULT 0;");
                    sQLiteTransaction22.commit();
                    $closeResource(null, sQLiteTransaction22);
                    break;
                case 14:
                    LauncherDbUtils.SQLiteTransaction sQLiteTransaction222 = new LauncherDbUtils.SQLiteTransaction(sQLiteDatabase);
                    sQLiteDatabase.execSQL("ALTER TABLE favorites ADD COLUMN modified INTEGER NOT NULL DEFAULT 0;");
                    sQLiteDatabase.execSQL("ALTER TABLE workspaceScreens ADD COLUMN modified INTEGER NOT NULL DEFAULT 0;");
                    sQLiteTransaction222.commit();
                    $closeResource(null, sQLiteTransaction222);
                    break;
                case 15:
                    break;
                case 16:
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
                case 25:
                    convertShortcutsToLauncherActivities(sQLiteDatabase);
                    return;
                case 26:
                case 27:
                    return;
                default:
                    Log.w(LauncherProvider.TAG, "Destroying all old data.");
                    createEmptyDB(sQLiteDatabase);
                    return;
            }
        }

        private static /* synthetic */ void $closeResource(Throwable th, AutoCloseable autoCloseable) {
            if (th == null) {
                autoCloseable.close();
                return;
            }
            try {
                autoCloseable.close();
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onDowngrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
            try {
                DbDowngradeHelper.parse(this.mContext.getFileStreamPath(LauncherProvider.DOWNGRADE_SCHEMA_FILE)).onDowngrade(sQLiteDatabase, i, i2);
            } catch (Exception e) {
                Log.d(LauncherProvider.TAG, "Unable to downgrade from: " + i + " to " + i2 + ". Wiping databse.", e);
                createEmptyDB(sQLiteDatabase);
            }
        }

        public void createEmptyDB(SQLiteDatabase sQLiteDatabase) {
            LauncherDbUtils.SQLiteTransaction sQLiteTransaction = new LauncherDbUtils.SQLiteTransaction(sQLiteDatabase);
            try {
                sQLiteDatabase.execSQL("DROP TABLE IF EXISTS favorites");
                sQLiteDatabase.execSQL("DROP TABLE IF EXISTS workspaceScreens");
                onCreate(sQLiteDatabase);
                sQLiteTransaction.commit();
                $closeResource(null, sQLiteTransaction);
            } finally {
            }
        }

        @TargetApi(26)
        public void removeGhostWidgets(SQLiteDatabase sQLiteDatabase) {
            int i;
            AppWidgetHost newLauncherWidgetHost = newLauncherWidgetHost();
            try {
                int[] appWidgetIds = newLauncherWidgetHost.getAppWidgetIds();
                HashSet hashSet = new HashSet();
                try {
                    Cursor query = sQLiteDatabase.query(LauncherSettings.Favorites.TABLE_NAME, new String[]{LauncherSettings.Favorites.APPWIDGET_ID}, "itemType=4", null, null, null, null);
                    while (true) {
                        if (!query.moveToNext()) {
                            break;
                        }
                        hashSet.add(Integer.valueOf(query.getInt(0)));
                    }
                    if (query != null) {
                        $closeResource(null, query);
                    }
                    for (int i2 : appWidgetIds) {
                        if (!hashSet.contains(Integer.valueOf(i2))) {
                            try {
                                FileLog.d(LauncherProvider.TAG, "Deleting invalid widget " + i2);
                                newLauncherWidgetHost.deleteAppWidgetId(i2);
                            } catch (RuntimeException e) {
                            }
                        }
                    }
                } catch (SQLException e2) {
                    Log.w(LauncherProvider.TAG, "Error getting widgets list", e2);
                }
            } catch (IncompatibleClassChangeError e3) {
                Log.e(LauncherProvider.TAG, "getAppWidgetIds not supported", e3);
            }
        }

        /* JADX WARN: Removed duplicated region for block: B:32:0x008a A[Catch: Throwable -> 0x0091, TRY_ENTER, TryCatch #4 {Throwable -> 0x0091, blocks: (B:5:0x002f, B:32:0x008a, B:33:0x008d, B:20:0x0073), top: B:55:0x002f }] */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
        */
        void convertShortcutsToLauncherActivities(SQLiteDatabase sQLiteDatabase) {
            Throwable th;
            Throwable th2;
            try {
                LauncherDbUtils.SQLiteTransaction sQLiteTransaction = new LauncherDbUtils.SQLiteTransaction(sQLiteDatabase);
                Cursor query = sQLiteDatabase.query(LauncherSettings.Favorites.TABLE_NAME, new String[]{"_id", LauncherSettings.BaseLauncherColumns.INTENT}, "itemType=1 AND profileId=" + getDefaultUserSerial(), null, null, null, null);
                try {
                    SQLiteStatement compileStatement = sQLiteDatabase.compileStatement("UPDATE favorites SET itemType=0 WHERE _id=?");
                    try {
                        int columnIndexOrThrow = query.getColumnIndexOrThrow("_id");
                        int columnIndexOrThrow2 = query.getColumnIndexOrThrow(LauncherSettings.BaseLauncherColumns.INTENT);
                        while (query.moveToNext()) {
                            try {
                                if (Utilities.isLauncherAppTarget(Intent.parseUri(query.getString(columnIndexOrThrow2), 0))) {
                                    compileStatement.bindLong(1, query.getLong(columnIndexOrThrow));
                                    compileStatement.executeUpdateDelete();
                                }
                            } catch (URISyntaxException e) {
                                Log.e(LauncherProvider.TAG, "Unable to parse intent", e);
                            } catch (Throwable th3) {
                                th2 = th3;
                                th = null;
                                if (compileStatement != null) {
                                }
                                throw th2;
                            }
                        }
                        sQLiteTransaction.commit();
                        if (compileStatement != null) {
                            $closeResource(null, compileStatement);
                        }
                        if (query != null) {
                            $closeResource(null, query);
                        }
                        $closeResource(null, sQLiteTransaction);
                    } catch (Throwable th4) {
                        try {
                            throw th4;
                        } catch (Throwable th5) {
                            th = th4;
                            th2 = th5;
                            if (compileStatement != null) {
                                $closeResource(th, compileStatement);
                            }
                            throw th2;
                        }
                    }
                } catch (Throwable th6) {
                    try {
                        throw th6;
                    } catch (Throwable th7) {
                        if (query != null) {
                            $closeResource(th6, query);
                        }
                        throw th7;
                    }
                }
            } catch (SQLException e2) {
                Log.w(LauncherProvider.TAG, "Error deduping shortcuts", e2);
            }
        }

        public boolean recreateWorkspaceTable(SQLiteDatabase sQLiteDatabase) {
            try {
                LauncherDbUtils.SQLiteTransaction sQLiteTransaction = new LauncherDbUtils.SQLiteTransaction(sQLiteDatabase);
                Cursor query = sQLiteDatabase.query(LauncherSettings.WorkspaceScreens.TABLE_NAME, new String[]{"_id"}, null, null, null, null, LauncherSettings.WorkspaceScreens.SCREEN_RANK);
                try {
                    ArrayList arrayList = new ArrayList(LauncherDbUtils.iterateCursor(query, 0, new LinkedHashSet()));
                    if (query != null) {
                        $closeResource(null, query);
                    }
                    sQLiteDatabase.execSQL("DROP TABLE IF EXISTS workspaceScreens");
                    addWorkspacesTable(sQLiteDatabase, false);
                    int size = arrayList.size();
                    for (int i = 0; i < size; i++) {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("_id", (Long) arrayList.get(i));
                        contentValues.put(LauncherSettings.WorkspaceScreens.SCREEN_RANK, Integer.valueOf(i));
                        LauncherProvider.addModifiedTime(contentValues);
                        sQLiteDatabase.insertOrThrow(LauncherSettings.WorkspaceScreens.TABLE_NAME, null, contentValues);
                    }
                    sQLiteTransaction.commit();
                    this.mMaxScreenId = arrayList.isEmpty() ? 0L : ((Long) Collections.max(arrayList)).longValue();
                    $closeResource(null, sQLiteTransaction);
                    return true;
                } finally {
                }
            } catch (SQLException e) {
                Log.e(LauncherProvider.TAG, e.getMessage(), e);
                return false;
            }
        }

        boolean updateFolderItemsRank(SQLiteDatabase sQLiteDatabase, boolean z) {
            try {
                LauncherDbUtils.SQLiteTransaction sQLiteTransaction = new LauncherDbUtils.SQLiteTransaction(sQLiteDatabase);
                if (z) {
                    sQLiteDatabase.execSQL("ALTER TABLE favorites ADD COLUMN rank INTEGER NOT NULL DEFAULT 0;");
                }
                Cursor rawQuery = sQLiteDatabase.rawQuery("SELECT container, MAX(cellX) FROM favorites WHERE container IN (SELECT _id FROM favorites WHERE itemType = ?) GROUP BY container;", new String[]{Integer.toString(2)});
                while (rawQuery.moveToNext()) {
                    sQLiteDatabase.execSQL("UPDATE favorites SET rank=cellX+(cellY*?) WHERE container=? AND cellX IS NOT NULL AND cellY IS NOT NULL;", new Object[]{Long.valueOf(rawQuery.getLong(1) + 1), Long.valueOf(rawQuery.getLong(0))});
                }
                rawQuery.close();
                sQLiteTransaction.commit();
                $closeResource(null, sQLiteTransaction);
                return true;
            } catch (SQLException e) {
                Log.e(LauncherProvider.TAG, e.getMessage(), e);
                return false;
            }
        }

        private boolean addProfileColumn(SQLiteDatabase sQLiteDatabase) {
            return addIntegerColumn(sQLiteDatabase, LauncherSettings.Favorites.PROFILE_ID, getDefaultUserSerial());
        }

        private boolean addIntegerColumn(SQLiteDatabase sQLiteDatabase, String str, long j) {
            try {
                LauncherDbUtils.SQLiteTransaction sQLiteTransaction = new LauncherDbUtils.SQLiteTransaction(sQLiteDatabase);
                sQLiteDatabase.execSQL("ALTER TABLE favorites ADD COLUMN " + str + " INTEGER NOT NULL DEFAULT " + j + ";");
                sQLiteTransaction.commit();
                $closeResource(null, sQLiteTransaction);
                return true;
            } catch (SQLException e) {
                Log.e(LauncherProvider.TAG, e.getMessage(), e);
                return false;
            }
        }

        @Override // com.android.launcher3.AutoInstallsLayout.LayoutParserCallback
        public long generateNewItemId() {
            if (this.mMaxItemId < 0) {
                throw new RuntimeException("Error: max item id was not initialized");
            }
            this.mMaxItemId++;
            return this.mMaxItemId;
        }

        public AppWidgetHost newLauncherWidgetHost() {
            return new LauncherAppWidgetHost(this.mContext);
        }

        @Override // com.android.launcher3.AutoInstallsLayout.LayoutParserCallback
        public long insertAndCheck(SQLiteDatabase sQLiteDatabase, ContentValues contentValues) {
            return LauncherProvider.dbInsertAndCheck(this, sQLiteDatabase, LauncherSettings.Favorites.TABLE_NAME, null, contentValues);
        }

        public void checkId(String str, ContentValues contentValues) {
            long longValue = contentValues.getAsLong("_id").longValue();
            if (LauncherSettings.WorkspaceScreens.TABLE_NAME.equals(str)) {
                this.mMaxScreenId = Math.max(longValue, this.mMaxScreenId);
            } else {
                this.mMaxItemId = Math.max(longValue, this.mMaxItemId);
            }
        }

        private long initializeMaxItemId(SQLiteDatabase sQLiteDatabase) {
            return LauncherProvider.getMaxId(sQLiteDatabase, LauncherSettings.Favorites.TABLE_NAME);
        }

        public long generateNewScreenId() {
            if (this.mMaxScreenId < 0) {
                throw new RuntimeException("Error: max screen id was not initialized");
            }
            this.mMaxScreenId++;
            return this.mMaxScreenId;
        }

        private long initializeMaxScreenId(SQLiteDatabase sQLiteDatabase) {
            return LauncherProvider.getMaxId(sQLiteDatabase, LauncherSettings.WorkspaceScreens.TABLE_NAME);
        }

        int loadFavorites(SQLiteDatabase sQLiteDatabase, AutoInstallsLayout autoInstallsLayout) {
            ArrayList<Long> arrayList = new ArrayList<>();
            int loadLayout = autoInstallsLayout.loadLayout(sQLiteDatabase, arrayList);
            Collections.sort(arrayList);
            ContentValues contentValues = new ContentValues();
            Iterator<Long> it = arrayList.iterator();
            int i = 0;
            while (it.hasNext()) {
                contentValues.clear();
                contentValues.put("_id", it.next());
                contentValues.put(LauncherSettings.WorkspaceScreens.SCREEN_RANK, Integer.valueOf(i));
                if (LauncherProvider.dbInsertAndCheck(this, sQLiteDatabase, LauncherSettings.WorkspaceScreens.TABLE_NAME, null, contentValues) < 0) {
                    throw new RuntimeException("Failed initialize screen tablefrom default layout");
                }
                i++;
            }
            this.mMaxItemId = initializeMaxItemId(sQLiteDatabase);
            this.mMaxScreenId = initializeMaxScreenId(sQLiteDatabase);
            return loadLayout;
        }
    }

    static long getMaxId(SQLiteDatabase sQLiteDatabase, String str) {
        long j;
        Cursor rawQuery = sQLiteDatabase.rawQuery("SELECT MAX(_id) FROM " + str, null);
        if (rawQuery != null && rawQuery.moveToNext()) {
            j = rawQuery.getLong(0);
        } else {
            j = -1;
        }
        if (rawQuery != null) {
            rawQuery.close();
        }
        if (j == -1) {
            throw new RuntimeException("Error: could not query max id in " + str);
        }
        return j;
    }

    /* loaded from: classes.dex */
    static class SqlArguments {
        public final String[] args;
        public final String table;
        public final String where;

        /* JADX INFO: Access modifiers changed from: package-private */
        public SqlArguments(Uri uri, String str, String[] strArr) {
            if (uri.getPathSegments().size() == 1) {
                this.table = uri.getPathSegments().get(0);
                this.where = str;
                this.args = strArr;
            } else if (uri.getPathSegments().size() != 2) {
                throw new IllegalArgumentException("Invalid URI: " + uri);
            } else if (!TextUtils.isEmpty(str)) {
                throw new UnsupportedOperationException("WHERE clause not supported: " + uri);
            } else {
                this.table = uri.getPathSegments().get(0);
                this.where = "_id=" + ContentUris.parseId(uri);
                this.args = null;
            }
        }

        SqlArguments(Uri uri) {
            if (uri.getPathSegments().size() == 1) {
                this.table = uri.getPathSegments().get(0);
                this.where = null;
                this.args = null;
                return;
            }
            throw new IllegalArgumentException("Invalid URI: " + uri);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class ChangeListenerWrapper implements Handler.Callback {
        private static final int MSG_APP_WIDGET_HOST_RESET = 2;
        private static final int MSG_LAUNCHER_PROVIDER_CHANGED = 1;
        private LauncherProviderChangeListener mListener;

        private ChangeListenerWrapper() {
        }

        @Override // android.os.Handler.Callback
        public boolean handleMessage(Message message) {
            if (this.mListener != null) {
                switch (message.what) {
                    case 1:
                        this.mListener.onLauncherProviderChanged();
                        return true;
                    case 2:
                        this.mListener.onAppWidgetHostReset();
                        return true;
                    default:
                        return true;
                }
            }
            return true;
        }
    }
}
