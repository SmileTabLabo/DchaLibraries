package com.android.launcher3.model;

import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import com.android.launcher3.InvariantDeviceProfile;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherAppWidgetProviderInfo;
import com.android.launcher3.LauncherModel;
import com.android.launcher3.LauncherProvider;
import com.android.launcher3.LauncherSettings$Favorites;
import com.android.launcher3.LauncherSettings$WorkspaceScreens;
import com.android.launcher3.Utilities;
import com.android.launcher3.backup.nano.BackupProtos$DeviceProfieData;
import com.android.launcher3.compat.AppWidgetManagerCompat;
import com.android.launcher3.compat.PackageInstallerCompat;
import com.android.launcher3.util.LongArrayMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
/* loaded from: a.zip:com/android/launcher3/model/GridSizeMigrationTask.class */
public class GridSizeMigrationTask {
    public static boolean ENABLED = Utilities.ATLEAST_N;
    private final ArrayList<DbEntry> mCarryOver;
    private final Context mContext;
    private final int mDestAllAppsRank;
    private final int mDestHotseatSize;
    private final ArrayList<Long> mEntryToRemove;
    private final InvariantDeviceProfile mIdp;
    private final boolean mShouldRemoveX;
    private final boolean mShouldRemoveY;
    private final int mSrcAllAppsRank;
    private final int mSrcHotseatSize;
    private final int mSrcX;
    private final int mSrcY;
    private final ContentValues mTempValues;
    private final int mTrgX;
    private final int mTrgY;
    private final ArrayList<ContentProviderOperation> mUpdateOperations;
    private final HashSet<String> mValidPackages;
    private final HashMap<String, Point> mWidgetMinSize;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/launcher3/model/GridSizeMigrationTask$DbEntry.class */
    public static class DbEntry extends ItemInfo implements Comparable<DbEntry> {
        public float weight;

        public void addToContentValues(ContentValues contentValues) {
            contentValues.put("screen", Long.valueOf(this.screenId));
            contentValues.put("cellX", Integer.valueOf(this.cellX));
            contentValues.put("cellY", Integer.valueOf(this.cellY));
            contentValues.put("spanX", Integer.valueOf(this.spanX));
            contentValues.put("spanY", Integer.valueOf(this.spanY));
        }

        public boolean columnsSame(DbEntry dbEntry) {
            boolean z = false;
            if (dbEntry.cellX == this.cellX) {
                z = false;
                if (dbEntry.cellY == this.cellY) {
                    z = false;
                    if (dbEntry.spanX == this.spanX) {
                        z = false;
                        if (dbEntry.spanY == this.spanY) {
                            z = false;
                            if (dbEntry.screenId == this.screenId) {
                                z = true;
                            }
                        }
                    }
                }
            }
            return z;
        }

        @Override // java.lang.Comparable
        public int compareTo(DbEntry dbEntry) {
            if (this.itemType == 4) {
                if (dbEntry.itemType == 4) {
                    return (dbEntry.spanY * dbEntry.spanX) - (this.spanX * this.spanY);
                }
                return -1;
            } else if (dbEntry.itemType == 4) {
                return 1;
            } else {
                return Float.compare(dbEntry.weight, this.weight);
            }
        }

        public DbEntry copy() {
            DbEntry dbEntry = new DbEntry();
            dbEntry.copyFrom(this);
            dbEntry.weight = this.weight;
            dbEntry.minSpanX = this.minSpanX;
            dbEntry.minSpanY = this.minSpanY;
            return dbEntry;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/launcher3/model/GridSizeMigrationTask$OptimalPlacementSolution.class */
    public class OptimalPlacementSolution {
        ArrayList<DbEntry> finalPlacedItems;
        private final boolean ignoreMove;
        private final ArrayList<DbEntry> itemsToPlace;
        float lowestMoveCost;
        float lowestWeightLoss;
        private final boolean[][] occupied;
        final GridSizeMigrationTask this$0;

        public OptimalPlacementSolution(GridSizeMigrationTask gridSizeMigrationTask, boolean[][] zArr, ArrayList<DbEntry> arrayList) {
            this(gridSizeMigrationTask, zArr, arrayList, false);
        }

        public OptimalPlacementSolution(GridSizeMigrationTask gridSizeMigrationTask, boolean[][] zArr, ArrayList<DbEntry> arrayList, boolean z) {
            this.this$0 = gridSizeMigrationTask;
            this.lowestWeightLoss = Float.MAX_VALUE;
            this.lowestMoveCost = Float.MAX_VALUE;
            this.occupied = zArr;
            this.itemsToPlace = arrayList;
            this.ignoreMove = z;
            Collections.sort(this.itemsToPlace);
        }

        public void find() {
            find(0, 0.0f, 0.0f, new ArrayList<>());
        }

        public void find(int i, float f, float f2, ArrayList<DbEntry> arrayList) {
            if (f >= this.lowestWeightLoss) {
                return;
            }
            if (f == this.lowestWeightLoss && f2 >= this.lowestMoveCost) {
                return;
            }
            if (i >= this.itemsToPlace.size()) {
                this.lowestWeightLoss = f;
                this.lowestMoveCost = f2;
                this.finalPlacedItems = GridSizeMigrationTask.deepCopy(arrayList);
                return;
            }
            DbEntry dbEntry = this.itemsToPlace.get(i);
            int i2 = dbEntry.cellX;
            int i3 = dbEntry.cellY;
            ArrayList<DbEntry> arrayList2 = new ArrayList<>(arrayList.size() + 1);
            arrayList2.addAll(arrayList);
            arrayList2.add(dbEntry);
            if (dbEntry.spanX > 1 || dbEntry.spanY > 1) {
                int i4 = dbEntry.spanX;
                int i5 = dbEntry.spanY;
                for (int i6 = 0; i6 < this.this$0.mTrgY; i6++) {
                    for (int i7 = 0; i7 < this.this$0.mTrgX; i7++) {
                        float f3 = f2;
                        if (i7 != i2) {
                            dbEntry.cellX = i7;
                            f3 = f2 + 1.0f;
                        }
                        float f4 = f3;
                        if (i6 != i3) {
                            dbEntry.cellY = i6;
                            f4 = f3 + 1.0f;
                        }
                        if (this.ignoreMove) {
                            f4 = f2;
                        }
                        if (this.this$0.isVacant(this.occupied, i7, i6, i4, i5)) {
                            this.this$0.markCells(this.occupied, dbEntry, true);
                            find(i + 1, f, f4, arrayList2);
                            this.this$0.markCells(this.occupied, dbEntry, false);
                        }
                        if (i4 > dbEntry.minSpanX && this.this$0.isVacant(this.occupied, i7, i6, i4 - 1, i5)) {
                            dbEntry.spanX--;
                            this.this$0.markCells(this.occupied, dbEntry, true);
                            find(i + 1, f, 1.0f + f4, arrayList2);
                            this.this$0.markCells(this.occupied, dbEntry, false);
                            dbEntry.spanX++;
                        }
                        if (i5 > dbEntry.minSpanY && this.this$0.isVacant(this.occupied, i7, i6, i4, i5 - 1)) {
                            dbEntry.spanY--;
                            this.this$0.markCells(this.occupied, dbEntry, true);
                            find(i + 1, f, 1.0f + f4, arrayList2);
                            this.this$0.markCells(this.occupied, dbEntry, false);
                            dbEntry.spanY++;
                        }
                        if (i5 > dbEntry.minSpanY && i4 > dbEntry.minSpanX && this.this$0.isVacant(this.occupied, i7, i6, i4 - 1, i5 - 1)) {
                            dbEntry.spanX--;
                            dbEntry.spanY--;
                            this.this$0.markCells(this.occupied, dbEntry, true);
                            find(i + 1, f, 2.0f + f4, arrayList2);
                            this.this$0.markCells(this.occupied, dbEntry, false);
                            dbEntry.spanX++;
                            dbEntry.spanY++;
                        }
                        dbEntry.cellX = i2;
                        dbEntry.cellY = i3;
                    }
                }
                find(i + 1, dbEntry.weight + f, f2, arrayList);
                return;
            }
            int i8 = Integer.MAX_VALUE;
            int i9 = Integer.MAX_VALUE;
            int i10 = Integer.MAX_VALUE;
            for (int i11 = 0; i11 < this.this$0.mTrgY; i11++) {
                int i12 = 0;
                while (i12 < this.this$0.mTrgX) {
                    int i13 = i8;
                    int i14 = i9;
                    int i15 = i10;
                    if (!this.occupied[i12][i11]) {
                        int i16 = this.ignoreMove ? 0 : ((dbEntry.cellX - i12) * (dbEntry.cellX - i12)) + ((dbEntry.cellY - i11) * (dbEntry.cellY - i11));
                        i13 = i8;
                        i14 = i9;
                        i15 = i10;
                        if (i16 < i8) {
                            i14 = i12;
                            i15 = i11;
                            i13 = i16;
                        }
                    }
                    i12++;
                    i8 = i13;
                    i9 = i14;
                    i10 = i15;
                }
            }
            if (i9 < this.this$0.mTrgX && i10 < this.this$0.mTrgY) {
                float f5 = f2;
                if (i9 != i2) {
                    dbEntry.cellX = i9;
                    f5 = f2 + 1.0f;
                }
                float f6 = f5;
                if (i10 != i3) {
                    dbEntry.cellY = i10;
                    f6 = f5 + 1.0f;
                }
                float f7 = f6;
                if (this.ignoreMove) {
                    f7 = f2;
                }
                this.this$0.markCells(this.occupied, dbEntry, true);
                find(i + 1, f, f7, arrayList2);
                this.this$0.markCells(this.occupied, dbEntry, false);
                dbEntry.cellX = i2;
                dbEntry.cellY = i3;
                if (i + 1 >= this.itemsToPlace.size() || this.itemsToPlace.get(i + 1).weight < dbEntry.weight || this.ignoreMove) {
                    return;
                }
                find(i + 1, dbEntry.weight + f, f2, arrayList);
                return;
            }
            while (true) {
                i++;
                if (i >= this.itemsToPlace.size()) {
                    find(this.itemsToPlace.size(), dbEntry.weight + f, f2, arrayList);
                    return;
                }
                f += this.itemsToPlace.get(i).weight;
            }
        }
    }

    protected GridSizeMigrationTask(Context context, InvariantDeviceProfile invariantDeviceProfile, HashSet<String> hashSet, int i, int i2, int i3, int i4) {
        this.mWidgetMinSize = new HashMap<>();
        this.mTempValues = new ContentValues();
        this.mEntryToRemove = new ArrayList<>();
        this.mUpdateOperations = new ArrayList<>();
        this.mCarryOver = new ArrayList<>();
        this.mContext = context;
        this.mIdp = invariantDeviceProfile;
        this.mValidPackages = hashSet;
        this.mSrcHotseatSize = i;
        this.mSrcAllAppsRank = i2;
        this.mDestHotseatSize = i3;
        this.mDestAllAppsRank = i4;
        this.mTrgY = -1;
        this.mTrgX = -1;
        this.mSrcY = -1;
        this.mSrcX = -1;
        this.mShouldRemoveY = false;
        this.mShouldRemoveX = false;
    }

    protected GridSizeMigrationTask(Context context, InvariantDeviceProfile invariantDeviceProfile, HashSet<String> hashSet, HashMap<String, Point> hashMap, Point point, Point point2) {
        this.mWidgetMinSize = new HashMap<>();
        this.mTempValues = new ContentValues();
        this.mEntryToRemove = new ArrayList<>();
        this.mUpdateOperations = new ArrayList<>();
        this.mCarryOver = new ArrayList<>();
        this.mContext = context;
        this.mValidPackages = hashSet;
        this.mWidgetMinSize.putAll(hashMap);
        this.mIdp = invariantDeviceProfile;
        this.mSrcX = point.x;
        this.mSrcY = point.y;
        this.mTrgX = point2.x;
        this.mTrgY = point2.y;
        this.mShouldRemoveX = this.mTrgX < this.mSrcX;
        this.mShouldRemoveY = this.mTrgY < this.mSrcY;
        this.mDestAllAppsRank = -1;
        this.mDestHotseatSize = -1;
        this.mSrcAllAppsRank = -1;
        this.mSrcHotseatSize = -1;
    }

    private boolean applyOperations() throws Exception {
        if (!this.mUpdateOperations.isEmpty()) {
            this.mContext.getContentResolver().applyBatch(LauncherProvider.AUTHORITY, this.mUpdateOperations);
        }
        if (!this.mEntryToRemove.isEmpty()) {
            Log.d("GridSizeMigrationTask", "Removing items: " + TextUtils.join(", ", this.mEntryToRemove));
            this.mContext.getContentResolver().delete(LauncherSettings$Favorites.CONTENT_URI, Utilities.createDbSelectionQuery("_id", this.mEntryToRemove), null);
        }
        boolean z = true;
        if (this.mUpdateOperations.isEmpty()) {
            z = true;
            if (this.mEntryToRemove.isEmpty()) {
                z = false;
            }
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static ArrayList<DbEntry> deepCopy(ArrayList<DbEntry> arrayList) {
        ArrayList<DbEntry> arrayList2 = new ArrayList<>(arrayList.size());
        for (DbEntry dbEntry : arrayList) {
            arrayList2.add(dbEntry.copy());
        }
        return arrayList2;
    }

    private int getFolderItemsCount(long j) {
        Cursor query = this.mContext.getContentResolver().query(LauncherSettings$Favorites.CONTENT_URI, new String[]{"_id", "intent"}, "container = " + j, null, null, null);
        int i = 0;
        while (query.moveToNext()) {
            try {
                verifyIntent(query.getString(1));
                i++;
            } catch (Exception e) {
                this.mEntryToRemove.add(Long.valueOf(query.getLong(0)));
            }
        }
        query.close();
        return i;
    }

    private static String getPointString(int i, int i2) {
        return String.format(Locale.ENGLISH, "%d,%d", Integer.valueOf(i), Integer.valueOf(i2));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isVacant(boolean[][] zArr, int i, int i2, int i3, int i4) {
        if (i + i3 <= this.mTrgX && i2 + i4 <= this.mTrgY) {
            for (int i5 = 0; i5 < i3; i5++) {
                for (int i6 = 0; i6 < i4; i6++) {
                    if (zArr[i5 + i][i6 + i2]) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    private ArrayList<DbEntry> loadHotseatEntries() {
        Cursor query = this.mContext.getContentResolver().query(LauncherSettings$Favorites.CONTENT_URI, new String[]{"_id", "itemType", "intent", "screen"}, "container = -101", null, null, null);
        int columnIndexOrThrow = query.getColumnIndexOrThrow("_id");
        int columnIndexOrThrow2 = query.getColumnIndexOrThrow("itemType");
        int columnIndexOrThrow3 = query.getColumnIndexOrThrow("intent");
        int columnIndexOrThrow4 = query.getColumnIndexOrThrow("screen");
        ArrayList<DbEntry> arrayList = new ArrayList<>();
        while (query.moveToNext()) {
            DbEntry dbEntry = new DbEntry();
            dbEntry.id = query.getLong(columnIndexOrThrow);
            dbEntry.itemType = query.getInt(columnIndexOrThrow2);
            dbEntry.screenId = query.getLong(columnIndexOrThrow4);
            if (dbEntry.screenId >= this.mSrcHotseatSize) {
                this.mEntryToRemove.add(Long.valueOf(dbEntry.id));
            } else {
                try {
                    switch (dbEntry.itemType) {
                        case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                        case 1:
                            verifyIntent(query.getString(columnIndexOrThrow3));
                            dbEntry.weight = dbEntry.itemType == 1 ? 1.0f : 0.8f;
                            break;
                        case 2:
                            int folderItemsCount = getFolderItemsCount(dbEntry.id);
                            if (folderItemsCount != 0) {
                                dbEntry.weight = folderItemsCount * 0.5f;
                                break;
                            } else {
                                throw new Exception("Folder is empty");
                            }
                        default:
                            throw new Exception("Invalid item type");
                    }
                    arrayList.add(dbEntry);
                } catch (Exception e) {
                    Log.d("GridSizeMigrationTask", "Removing item " + dbEntry.id, e);
                    this.mEntryToRemove.add(Long.valueOf(dbEntry.id));
                }
            }
        }
        query.close();
        return arrayList;
    }

    private ArrayList<DbEntry> loadWorkspaceEntries(long j) {
        Cursor query = this.mContext.getContentResolver().query(LauncherSettings$Favorites.CONTENT_URI, new String[]{"_id", "itemType", "cellX", "cellY", "spanX", "spanY", "intent", "appWidgetProvider", "appWidgetId"}, "container = -100 AND screen = " + j, null, null, null);
        int columnIndexOrThrow = query.getColumnIndexOrThrow("_id");
        int columnIndexOrThrow2 = query.getColumnIndexOrThrow("itemType");
        int columnIndexOrThrow3 = query.getColumnIndexOrThrow("cellX");
        int columnIndexOrThrow4 = query.getColumnIndexOrThrow("cellY");
        int columnIndexOrThrow5 = query.getColumnIndexOrThrow("spanX");
        int columnIndexOrThrow6 = query.getColumnIndexOrThrow("spanY");
        int columnIndexOrThrow7 = query.getColumnIndexOrThrow("intent");
        int columnIndexOrThrow8 = query.getColumnIndexOrThrow("appWidgetProvider");
        int columnIndexOrThrow9 = query.getColumnIndexOrThrow("appWidgetId");
        ArrayList<DbEntry> arrayList = new ArrayList<>();
        while (query.moveToNext()) {
            DbEntry dbEntry = new DbEntry();
            dbEntry.id = query.getLong(columnIndexOrThrow);
            dbEntry.itemType = query.getInt(columnIndexOrThrow2);
            dbEntry.cellX = query.getInt(columnIndexOrThrow3);
            dbEntry.cellY = query.getInt(columnIndexOrThrow4);
            dbEntry.spanX = query.getInt(columnIndexOrThrow5);
            dbEntry.spanY = query.getInt(columnIndexOrThrow6);
            dbEntry.screenId = j;
            try {
                switch (dbEntry.itemType) {
                    case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                    case 1:
                        verifyIntent(query.getString(columnIndexOrThrow7));
                        dbEntry.weight = dbEntry.itemType == 1 ? 1.0f : 0.8f;
                        break;
                    case 2:
                        int folderItemsCount = getFolderItemsCount(dbEntry.id);
                        if (folderItemsCount != 0) {
                            dbEntry.weight = folderItemsCount * 0.5f;
                            break;
                        } else {
                            throw new Exception("Folder is empty");
                        }
                    case 3:
                    default:
                        throw new Exception("Invalid item type");
                    case 4:
                        String string = query.getString(columnIndexOrThrow8);
                        verifyPackage(ComponentName.unflattenFromString(string).getPackageName());
                        dbEntry.weight = Math.max(2.0f, dbEntry.spanX * 0.6f * dbEntry.spanY);
                        LauncherAppWidgetProviderInfo launcherAppWidgetInfo = AppWidgetManagerCompat.getInstance(this.mContext).getLauncherAppWidgetInfo(query.getInt(columnIndexOrThrow9));
                        Point minSpans = launcherAppWidgetInfo == null ? this.mWidgetMinSize.get(string) : launcherAppWidgetInfo.getMinSpans(this.mIdp, this.mContext);
                        if (minSpans != null) {
                            dbEntry.minSpanX = minSpans.x > 0 ? minSpans.x : dbEntry.spanX;
                            dbEntry.minSpanY = minSpans.y > 0 ? minSpans.y : dbEntry.spanY;
                        } else {
                            dbEntry.minSpanY = 2;
                            dbEntry.minSpanX = 2;
                        }
                        if (dbEntry.minSpanX > this.mTrgX || dbEntry.minSpanY > this.mTrgY) {
                            throw new Exception("Widget can't be resized down to fit the grid");
                        }
                        break;
                }
                arrayList.add(dbEntry);
            } catch (Exception e) {
                Log.d("GridSizeMigrationTask", "Removing item " + dbEntry.id, e);
                this.mEntryToRemove.add(Long.valueOf(dbEntry.id));
            }
        }
        query.close();
        return arrayList;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void markCells(boolean[][] zArr, DbEntry dbEntry, boolean z) {
        for (int i = dbEntry.cellX; i < dbEntry.cellX + dbEntry.spanX; i++) {
            for (int i2 = dbEntry.cellY; i2 < dbEntry.cellY + dbEntry.spanY; i2++) {
                zArr[i][i2] = z;
            }
        }
    }

    public static void markForMigration(Context context, HashSet<String> hashSet, BackupProtos$DeviceProfieData backupProtos$DeviceProfieData) {
        Utilities.getPrefs(context).edit().putString("migration_src_workspace_size", getPointString((int) backupProtos$DeviceProfieData.desktopCols, (int) backupProtos$DeviceProfieData.desktopRows)).putString("migration_src_hotseat_size", getPointString((int) backupProtos$DeviceProfieData.hotseatCount, backupProtos$DeviceProfieData.allappsRank)).putStringSet("migration_widget_min_size", hashSet).apply();
    }

    public static boolean migrateGridIfNeeded(Context context) {
        boolean z;
        int i;
        SharedPreferences prefs = Utilities.getPrefs(context);
        InvariantDeviceProfile invariantDeviceProfile = LauncherAppState.getInstance().getInvariantDeviceProfile();
        String pointString = getPointString(invariantDeviceProfile.numColumns, invariantDeviceProfile.numRows);
        String pointString2 = getPointString(invariantDeviceProfile.numHotseatIcons, invariantDeviceProfile.hotseatAllAppsRank);
        if (pointString.equals(prefs.getString("migration_src_workspace_size", "")) && pointString2.equals(prefs.getString("migration_src_hotseat_size", ""))) {
            return true;
        }
        long currentTimeMillis = System.currentTimeMillis();
        boolean z2 = false;
        try {
            try {
                HashSet hashSet = new HashSet();
                for (PackageInfo packageInfo : context.getPackageManager().getInstalledPackages(0)) {
                    hashSet.add(packageInfo.packageName);
                }
                hashSet.addAll(PackageInstallerCompat.getInstance(context).updateAndGetActiveSessionCache().keySet());
                Point parsePoint = parsePoint(prefs.getString("migration_src_hotseat_size", pointString2));
                if (parsePoint.x != invariantDeviceProfile.numHotseatIcons || parsePoint.y != invariantDeviceProfile.hotseatAllAppsRank) {
                    z2 = new GridSizeMigrationTask(context, LauncherAppState.getInstance().getInvariantDeviceProfile(), hashSet, parsePoint.x, parsePoint.y, invariantDeviceProfile.numHotseatIcons, invariantDeviceProfile.hotseatAllAppsRank).migrateHotseat();
                }
                Point point = new Point(invariantDeviceProfile.numColumns, invariantDeviceProfile.numRows);
                Point parsePoint2 = parsePoint(prefs.getString("migration_src_workspace_size", pointString));
                boolean z3 = z2;
                if (!point.equals(parsePoint2)) {
                    ArrayList arrayList = new ArrayList();
                    arrayList.add(new Point(3, 2));
                    arrayList.add(new Point(3, 3));
                    arrayList.add(new Point(4, 3));
                    arrayList.add(new Point(4, 4));
                    arrayList.add(new Point(5, 5));
                    arrayList.add(new Point(6, 5));
                    arrayList.add(new Point(6, 6));
                    arrayList.add(new Point(7, 7));
                    int indexOf = arrayList.indexOf(parsePoint2);
                    int indexOf2 = arrayList.indexOf(point);
                    if (indexOf <= -1 || indexOf2 <= -1) {
                        throw new Exception("Unable to migrate grid size from " + parsePoint2 + " to " + point);
                    }
                    HashMap hashMap = new HashMap();
                    Iterator<T> it = Utilities.getPrefs(context).getStringSet("migration_widget_min_size", Collections.emptySet()).iterator();
                    while (true) {
                        z = z2;
                        i = indexOf;
                        if (!it.hasNext()) {
                            break;
                        }
                        String[] split = ((String) it.next()).split("#");
                        hashMap.put(split[0], parsePoint(split[1]));
                    }
                    while (true) {
                        z3 = z;
                        if (indexOf2 >= i) {
                            break;
                        }
                        if (new GridSizeMigrationTask(context, LauncherAppState.getInstance().getInvariantDeviceProfile(), hashSet, hashMap, (Point) arrayList.get(i), (Point) arrayList.get(i - 1)).migrateWorkspace()) {
                            z = true;
                        }
                        i--;
                    }
                }
                if (z3) {
                    Cursor query = context.getContentResolver().query(LauncherSettings$Favorites.CONTENT_URI, null, null, null, null);
                    boolean moveToNext = query.moveToNext();
                    query.close();
                    if (!moveToNext) {
                        throw new Exception("Removed every thing during grid resize");
                    }
                }
                Log.v("GridSizeMigrationTask", "Workspace migration completed in " + (System.currentTimeMillis() - currentTimeMillis));
                prefs.edit().putString("migration_src_workspace_size", pointString).putString("migration_src_hotseat_size", pointString2).remove("migration_widget_min_size").apply();
                return true;
            } catch (Exception e) {
                Log.e("GridSizeMigrationTask", "Error during grid migration", e);
                Log.v("GridSizeMigrationTask", "Workspace migration completed in " + (System.currentTimeMillis() - currentTimeMillis));
                prefs.edit().putString("migration_src_workspace_size", pointString).putString("migration_src_hotseat_size", pointString2).remove("migration_widget_min_size").apply();
                return false;
            }
        } catch (Throwable th) {
            Log.v("GridSizeMigrationTask", "Workspace migration completed in " + (System.currentTimeMillis() - currentTimeMillis));
            prefs.edit().putString("migration_src_workspace_size", pointString).putString("migration_src_hotseat_size", pointString2).remove("migration_widget_min_size").apply();
            throw th;
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:13:0x00b9, code lost:
        if (r0[1] < r15) goto L65;
     */
    /* JADX WARN: Removed duplicated region for block: B:30:0x015d A[LOOP:1: B:6:0x0049->B:30:0x015d, LOOP_END] */
    /* JADX WARN: Removed duplicated region for block: B:61:0x00f1 A[EDGE_INSN: B:61:0x00f1->B:23:0x00f1 ?: BREAK  , SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private void migrateScreen(long j) {
        ArrayList<DbEntry> arrayList;
        float f;
        int i;
        int i2;
        float f2;
        ArrayList<DbEntry> loadWorkspaceEntries = loadWorkspaceEntries(j);
        int i3 = Integer.MAX_VALUE;
        int i4 = Integer.MAX_VALUE;
        float f3 = Float.MAX_VALUE;
        float f4 = Float.MAX_VALUE;
        float[] fArr = new float[2];
        ArrayList<DbEntry> arrayList2 = null;
        int i5 = 0;
        while (true) {
            arrayList = arrayList2;
            f = f3;
            i = i3;
            i2 = i4;
            if (i5 >= this.mSrcX) {
                break;
            }
            int i6 = 0;
            float f5 = f3;
            ArrayList<DbEntry> arrayList3 = arrayList2;
            while (true) {
                arrayList2 = arrayList3;
                f2 = f4;
                f3 = f5;
                i = i3;
                i2 = i4;
                if (i6 >= this.mSrcY) {
                    break;
                }
                ArrayList<DbEntry> tryRemove = tryRemove(i5, i6, deepCopy(loadWorkspaceEntries), fArr);
                if (fArr[0] >= f5) {
                    arrayList2 = arrayList3;
                    f2 = f4;
                    f3 = f5;
                    i = i3;
                    i2 = i4;
                    if (fArr[0] == f5) {
                        arrayList2 = arrayList3;
                        f2 = f4;
                        f3 = f5;
                        i = i3;
                        i2 = i4;
                    }
                    if (this.mShouldRemoveY) {
                        break;
                    }
                    i6++;
                    arrayList3 = arrayList2;
                    f4 = f2;
                    f5 = f3;
                    i3 = i;
                    i4 = i2;
                }
                f3 = fArr[0];
                f2 = fArr[1];
                if (this.mShouldRemoveX) {
                    i3 = i5;
                }
                if (this.mShouldRemoveY) {
                    i4 = i6;
                }
                arrayList2 = tryRemove;
                i2 = i4;
                i = i3;
                if (this.mShouldRemoveY) {
                }
            }
            if (!this.mShouldRemoveX) {
                f = f3;
                arrayList = arrayList2;
                break;
            }
            i5++;
            f4 = f2;
            i3 = i;
            i4 = i2;
        }
        Log.d("GridSizeMigrationTask", String.format("Removing row %d, column %d on screen %d", Integer.valueOf(i2), Integer.valueOf(i), Long.valueOf(j)));
        LongArrayMap<DbEntry> longArrayMap = new LongArrayMap();
        for (DbEntry dbEntry : deepCopy(loadWorkspaceEntries)) {
            longArrayMap.put(dbEntry.id, dbEntry);
        }
        for (DbEntry dbEntry2 : arrayList) {
            longArrayMap.remove(dbEntry2.id);
            if (!dbEntry2.columnsSame((DbEntry) longArrayMap.get(dbEntry2.id))) {
                update(dbEntry2);
            }
        }
        for (DbEntry dbEntry3 : longArrayMap) {
            this.mCarryOver.add(dbEntry3);
        }
        if (this.mCarryOver.isEmpty() || f != 0.0f) {
            return;
        }
        boolean[][] zArr = new boolean[this.mTrgX][this.mTrgY];
        for (DbEntry dbEntry4 : arrayList) {
            markCells(zArr, dbEntry4, true);
        }
        OptimalPlacementSolution optimalPlacementSolution = new OptimalPlacementSolution(this, zArr, deepCopy(this.mCarryOver), true);
        optimalPlacementSolution.find();
        if (optimalPlacementSolution.lowestWeightLoss == 0.0f) {
            for (DbEntry dbEntry5 : optimalPlacementSolution.finalPlacedItems) {
                dbEntry5.screenId = j;
                update(dbEntry5);
            }
            this.mCarryOver.clear();
        }
    }

    private static Point parsePoint(String str) {
        String[] split = str.split(",");
        return new Point(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
    }

    private ArrayList<DbEntry> tryRemove(int i, int i2, ArrayList<DbEntry> arrayList, float[] fArr) {
        boolean[][] zArr = new boolean[this.mTrgX][this.mTrgY];
        if (!this.mShouldRemoveX) {
            i = Integer.MAX_VALUE;
        }
        if (!this.mShouldRemoveY) {
            i2 = Integer.MAX_VALUE;
        }
        ArrayList<DbEntry> arrayList2 = new ArrayList<>();
        ArrayList arrayList3 = new ArrayList();
        for (DbEntry dbEntry : arrayList) {
            if ((dbEntry.cellX > i || dbEntry.spanX + dbEntry.cellX <= i) && (dbEntry.cellY > i2 || dbEntry.spanY + dbEntry.cellY <= i2)) {
                if (dbEntry.cellX > i) {
                    dbEntry.cellX--;
                }
                if (dbEntry.cellY > i2) {
                    dbEntry.cellY--;
                }
                arrayList2.add(dbEntry);
                markCells(zArr, dbEntry, true);
            } else {
                arrayList3.add(dbEntry);
                if (dbEntry.cellX >= i) {
                    dbEntry.cellX--;
                }
                if (dbEntry.cellY >= i2) {
                    dbEntry.cellY--;
                }
            }
        }
        OptimalPlacementSolution optimalPlacementSolution = new OptimalPlacementSolution(this, zArr, arrayList3);
        optimalPlacementSolution.find();
        arrayList2.addAll(optimalPlacementSolution.finalPlacedItems);
        fArr[0] = optimalPlacementSolution.lowestWeightLoss;
        fArr[1] = optimalPlacementSolution.lowestMoveCost;
        return arrayList2;
    }

    private void update(DbEntry dbEntry) {
        this.mTempValues.clear();
        dbEntry.addToContentValues(this.mTempValues);
        this.mUpdateOperations.add(ContentProviderOperation.newUpdate(LauncherSettings$Favorites.getContentUri(dbEntry.id)).withValues(this.mTempValues).build());
    }

    private void verifyIntent(String str) throws Exception {
        Intent parseUri = Intent.parseUri(str, 0);
        if (parseUri.getComponent() != null) {
            verifyPackage(parseUri.getComponent().getPackageName());
        } else if (parseUri.getPackage() != null) {
            verifyPackage(parseUri.getPackage());
        }
    }

    private void verifyPackage(String str) throws Exception {
        if (!this.mValidPackages.contains(str)) {
            throw new Exception("Package not available");
        }
    }

    protected boolean migrateHotseat() throws Exception {
        ArrayList<DbEntry> loadHotseatEntries = loadHotseatEntries();
        int i = this.mDestHotseatSize;
        while (loadHotseatEntries.size() > i - 1) {
            DbEntry dbEntry = loadHotseatEntries.get(loadHotseatEntries.size() / 2);
            for (DbEntry dbEntry2 : loadHotseatEntries) {
                if (dbEntry2.weight < dbEntry.weight) {
                    dbEntry = dbEntry2;
                }
            }
            this.mEntryToRemove.add(Long.valueOf(dbEntry.id));
            loadHotseatEntries.remove(dbEntry);
        }
        int i2 = 0;
        for (DbEntry dbEntry3 : loadHotseatEntries) {
            if (dbEntry3.screenId != i2) {
                dbEntry3.screenId = i2;
                dbEntry3.cellX = i2;
                dbEntry3.cellY = 0;
                update(dbEntry3);
            }
            int i3 = i2 + 1;
            i2 = i3;
            if (i3 == this.mDestAllAppsRank) {
                i2 = i3 + 1;
            }
        }
        return applyOperations();
    }

    protected boolean migrateWorkspace() throws Exception {
        ArrayList<Long> loadWorkspaceScreensDb = LauncherModel.loadWorkspaceScreensDb(this.mContext);
        if (loadWorkspaceScreensDb.isEmpty()) {
            throw new Exception("Unable to get workspace screens");
        }
        for (Long l : loadWorkspaceScreensDb) {
            long longValue = l.longValue();
            Log.d("GridSizeMigrationTask", "Migrating " + longValue);
            migrateScreen(longValue);
        }
        if (!this.mCarryOver.isEmpty()) {
            LongArrayMap longArrayMap = new LongArrayMap();
            for (DbEntry dbEntry : this.mCarryOver) {
                longArrayMap.put(dbEntry.id, dbEntry);
            }
            do {
                OptimalPlacementSolution optimalPlacementSolution = new OptimalPlacementSolution(this, new boolean[this.mTrgX][this.mTrgY], deepCopy(this.mCarryOver), true);
                optimalPlacementSolution.find();
                if (optimalPlacementSolution.finalPlacedItems.size() <= 0) {
                    throw new Exception("None of the items can be placed on an empty screen");
                }
                long generateNewScreenId = LauncherAppState.getLauncherProvider().generateNewScreenId();
                loadWorkspaceScreensDb.add(Long.valueOf(generateNewScreenId));
                for (DbEntry dbEntry2 : optimalPlacementSolution.finalPlacedItems) {
                    if (!this.mCarryOver.remove(longArrayMap.get(dbEntry2.id))) {
                        throw new Exception("Unable to find matching items");
                    }
                    dbEntry2.screenId = generateNewScreenId;
                    update(dbEntry2);
                }
            } while (!this.mCarryOver.isEmpty());
            Uri uri = LauncherSettings$WorkspaceScreens.CONTENT_URI;
            this.mUpdateOperations.add(ContentProviderOperation.newDelete(uri).build());
            int size = loadWorkspaceScreensDb.size();
            for (int i = 0; i < size; i++) {
                ContentValues contentValues = new ContentValues();
                contentValues.put("_id", Long.valueOf(loadWorkspaceScreensDb.get(i).longValue()));
                contentValues.put("screenRank", Integer.valueOf(i));
                this.mUpdateOperations.add(ContentProviderOperation.newInsert(uri).withValues(contentValues).build());
            }
        }
        return applyOperations();
    }
}
