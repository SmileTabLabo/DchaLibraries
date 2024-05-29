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
import com.android.launcher3.LauncherSettings;
import com.android.launcher3.Utilities;
import com.android.launcher3.compat.AppWidgetManagerCompat;
import com.android.launcher3.compat.PackageInstallerCompat;
import com.android.launcher3.util.GridOccupancy;
import com.android.launcher3.util.LongArrayMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
/* loaded from: classes.dex */
public class GridSizeMigrationTask {
    private static final boolean DEBUG = true;
    public static boolean ENABLED = Utilities.ATLEAST_NOUGAT;
    private static final String KEY_MIGRATION_SRC_HOTSEAT_COUNT = "migration_src_hotseat_count";
    private static final String KEY_MIGRATION_SRC_WORKSPACE_SIZE = "migration_src_workspace_size";
    private static final String TAG = "GridSizeMigrationTask";
    private static final float WT_APPLICATION = 0.8f;
    private static final float WT_FOLDER_FACTOR = 0.5f;
    private static final float WT_SHORTCUT = 1.0f;
    private static final float WT_WIDGET_FACTOR = 0.6f;
    private static final float WT_WIDGET_MIN = 2.0f;
    protected final ArrayList<DbEntry> mCarryOver;
    private final Context mContext;
    private final int mDestHotseatSize;
    protected final ArrayList<Long> mEntryToRemove;
    private final InvariantDeviceProfile mIdp;
    private final boolean mShouldRemoveX;
    private final boolean mShouldRemoveY;
    private final int mSrcHotseatSize;
    private final int mSrcX;
    private final int mSrcY;
    private final ContentValues mTempValues;
    private final int mTrgX;
    private final int mTrgY;
    private final ArrayList<ContentProviderOperation> mUpdateOperations;
    private final HashSet<String> mValidPackages;

    /* JADX INFO: Access modifiers changed from: protected */
    public GridSizeMigrationTask(Context context, InvariantDeviceProfile invariantDeviceProfile, HashSet<String> hashSet, Point point, Point point2) {
        this.mTempValues = new ContentValues();
        this.mEntryToRemove = new ArrayList<>();
        this.mUpdateOperations = new ArrayList<>();
        this.mCarryOver = new ArrayList<>();
        this.mContext = context;
        this.mValidPackages = hashSet;
        this.mIdp = invariantDeviceProfile;
        this.mSrcX = point.x;
        this.mSrcY = point.y;
        this.mTrgX = point2.x;
        this.mTrgY = point2.y;
        this.mShouldRemoveX = this.mTrgX < this.mSrcX;
        this.mShouldRemoveY = this.mTrgY < this.mSrcY;
        this.mDestHotseatSize = -1;
        this.mSrcHotseatSize = -1;
    }

    protected GridSizeMigrationTask(Context context, InvariantDeviceProfile invariantDeviceProfile, HashSet<String> hashSet, int i, int i2) {
        this.mTempValues = new ContentValues();
        this.mEntryToRemove = new ArrayList<>();
        this.mUpdateOperations = new ArrayList<>();
        this.mCarryOver = new ArrayList<>();
        this.mContext = context;
        this.mIdp = invariantDeviceProfile;
        this.mValidPackages = hashSet;
        this.mSrcHotseatSize = i;
        this.mDestHotseatSize = i2;
        this.mTrgY = -1;
        this.mTrgX = -1;
        this.mSrcY = -1;
        this.mSrcX = -1;
        this.mShouldRemoveY = false;
        this.mShouldRemoveX = false;
    }

    private boolean applyOperations() throws Exception {
        if (!this.mUpdateOperations.isEmpty()) {
            this.mContext.getContentResolver().applyBatch(LauncherProvider.AUTHORITY, this.mUpdateOperations);
        }
        if (!this.mEntryToRemove.isEmpty()) {
            Log.d(TAG, "Removing items: " + TextUtils.join(", ", this.mEntryToRemove));
            this.mContext.getContentResolver().delete(LauncherSettings.Favorites.CONTENT_URI, Utilities.createDbSelectionQuery("_id", this.mEntryToRemove), null);
        }
        return (this.mUpdateOperations.isEmpty() && this.mEntryToRemove.isEmpty()) ? false : true;
    }

    protected boolean migrateHotseat() throws Exception {
        ArrayList<DbEntry> loadHotseatEntries = loadHotseatEntries();
        int i = this.mDestHotseatSize;
        while (loadHotseatEntries.size() > i) {
            DbEntry dbEntry = loadHotseatEntries.get(loadHotseatEntries.size() / 2);
            Iterator<DbEntry> it = loadHotseatEntries.iterator();
            while (it.hasNext()) {
                DbEntry next = it.next();
                if (next.weight < dbEntry.weight) {
                    dbEntry = next;
                }
            }
            this.mEntryToRemove.add(Long.valueOf(dbEntry.id));
            loadHotseatEntries.remove(dbEntry);
        }
        Iterator<DbEntry> it2 = loadHotseatEntries.iterator();
        int i2 = 0;
        while (it2.hasNext()) {
            DbEntry next2 = it2.next();
            long j = i2;
            if (next2.screenId != j) {
                next2.screenId = j;
                next2.cellX = i2;
                next2.cellY = 0;
                update(next2);
            }
            i2++;
        }
        return applyOperations();
    }

    protected boolean migrateWorkspace() throws Exception {
        ArrayList<Long> loadWorkspaceScreensDb = LauncherModel.loadWorkspaceScreensDb(this.mContext);
        if (loadWorkspaceScreensDb.isEmpty()) {
            throw new Exception("Unable to get workspace screens");
        }
        Iterator<Long> it = loadWorkspaceScreensDb.iterator();
        while (it.hasNext()) {
            long longValue = it.next().longValue();
            Log.d(TAG, "Migrating " + longValue);
            migrateScreen(longValue);
        }
        if (!this.mCarryOver.isEmpty()) {
            LongArrayMap longArrayMap = new LongArrayMap();
            Iterator<DbEntry> it2 = this.mCarryOver.iterator();
            while (it2.hasNext()) {
                DbEntry next = it2.next();
                longArrayMap.put(next.id, next);
            }
            do {
                OptimalPlacementSolution optimalPlacementSolution = new OptimalPlacementSolution(new GridOccupancy(this.mTrgX, this.mTrgY), deepCopy(this.mCarryOver), 0, true);
                optimalPlacementSolution.find();
                if (optimalPlacementSolution.finalPlacedItems.size() > 0) {
                    long j = LauncherSettings.Settings.call(this.mContext.getContentResolver(), LauncherSettings.Settings.METHOD_NEW_SCREEN_ID).getLong(LauncherSettings.Settings.EXTRA_VALUE);
                    loadWorkspaceScreensDb.add(Long.valueOf(j));
                    Iterator<DbEntry> it3 = optimalPlacementSolution.finalPlacedItems.iterator();
                    while (it3.hasNext()) {
                        DbEntry next2 = it3.next();
                        if (!this.mCarryOver.remove(longArrayMap.get(next2.id))) {
                            throw new Exception("Unable to find matching items");
                        }
                        next2.screenId = j;
                        update(next2);
                    }
                } else {
                    throw new Exception("None of the items can be placed on an empty screen");
                }
            } while (!this.mCarryOver.isEmpty());
            Uri uri = LauncherSettings.WorkspaceScreens.CONTENT_URI;
            this.mUpdateOperations.add(ContentProviderOperation.newDelete(uri).build());
            int size = loadWorkspaceScreensDb.size();
            for (int i = 0; i < size; i++) {
                ContentValues contentValues = new ContentValues();
                contentValues.put("_id", Long.valueOf(loadWorkspaceScreensDb.get(i).longValue()));
                contentValues.put(LauncherSettings.WorkspaceScreens.SCREEN_RANK, Integer.valueOf(i));
                this.mUpdateOperations.add(ContentProviderOperation.newInsert(uri).withValues(contentValues).build());
            }
        }
        return applyOperations();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void migrateScreen(long j) {
        ArrayList<DbEntry> loadWorkspaceEntries = loadWorkspaceEntries(j);
        float[] fArr = new float[2];
        int i = Integer.MAX_VALUE;
        ArrayList<DbEntry> arrayList = null;
        float f = Float.MAX_VALUE;
        float f2 = Float.MAX_VALUE;
        int i2 = Integer.MAX_VALUE;
        for (int i3 = 0; i3 < this.mSrcX; i3++) {
            int i4 = i2;
            int i5 = i;
            ArrayList<DbEntry> arrayList2 = arrayList;
            float f3 = f;
            float f4 = f2;
            for (int i6 = this.mSrcY - 1; i6 >= 0; i6--) {
                ArrayList<DbEntry> tryRemove = tryRemove(i3, i6, 0, deepCopy(loadWorkspaceEntries), fArr);
                if (fArr[0] < f3 || (fArr[0] == f3 && fArr[1] < f4)) {
                    float f5 = fArr[0];
                    float f6 = fArr[1];
                    if (this.mShouldRemoveX) {
                        i5 = i3;
                    }
                    if (this.mShouldRemoveY) {
                        i4 = i6;
                    }
                    arrayList2 = tryRemove;
                    f3 = f5;
                    f4 = f6;
                }
                if (!this.mShouldRemoveY) {
                    break;
                }
            }
            f = f3;
            f2 = f4;
            i = i5;
            i2 = i4;
            arrayList = arrayList2;
            if (!this.mShouldRemoveX) {
                break;
            }
        }
        Log.d(TAG, String.format("Removing row %d, column %d on screen %d", Integer.valueOf(i2), Integer.valueOf(i), Long.valueOf(j)));
        LongArrayMap longArrayMap = new LongArrayMap();
        Iterator<DbEntry> it = deepCopy(loadWorkspaceEntries).iterator();
        while (it.hasNext()) {
            DbEntry next = it.next();
            longArrayMap.put(next.id, next);
        }
        Iterator<DbEntry> it2 = arrayList.iterator();
        while (it2.hasNext()) {
            DbEntry next2 = it2.next();
            longArrayMap.remove(next2.id);
            if (!next2.columnsSame((DbEntry) longArrayMap.get(next2.id))) {
                update(next2);
            }
        }
        Iterator it3 = longArrayMap.iterator();
        while (it3.hasNext()) {
            this.mCarryOver.add((DbEntry) it3.next());
        }
        if (!this.mCarryOver.isEmpty() && f == 0.0f) {
            GridOccupancy gridOccupancy = new GridOccupancy(this.mTrgX, this.mTrgY);
            gridOccupancy.markCells(0, 0, this.mTrgX, 0, true);
            Iterator<DbEntry> it4 = arrayList.iterator();
            while (it4.hasNext()) {
                gridOccupancy.markCells((ItemInfo) it4.next(), true);
            }
            OptimalPlacementSolution optimalPlacementSolution = new OptimalPlacementSolution(gridOccupancy, deepCopy(this.mCarryOver), 0, true);
            optimalPlacementSolution.find();
            if (optimalPlacementSolution.lowestWeightLoss == 0.0f) {
                Iterator<DbEntry> it5 = optimalPlacementSolution.finalPlacedItems.iterator();
                while (it5.hasNext()) {
                    DbEntry next3 = it5.next();
                    next3.screenId = j;
                    update(next3);
                }
                this.mCarryOver.clear();
            }
        }
    }

    protected void update(DbEntry dbEntry) {
        this.mTempValues.clear();
        dbEntry.addToContentValues(this.mTempValues);
        this.mUpdateOperations.add(ContentProviderOperation.newUpdate(LauncherSettings.Favorites.getContentUri(dbEntry.id)).withValues(this.mTempValues).build());
    }

    private ArrayList<DbEntry> tryRemove(int i, int i2, int i3, ArrayList<DbEntry> arrayList, float[] fArr) {
        GridOccupancy gridOccupancy = new GridOccupancy(this.mTrgX, this.mTrgY);
        gridOccupancy.markCells(0, 0, this.mTrgX, i3, true);
        if (!this.mShouldRemoveX) {
            i = Integer.MAX_VALUE;
        }
        if (!this.mShouldRemoveY) {
            i2 = Integer.MAX_VALUE;
        }
        ArrayList<DbEntry> arrayList2 = new ArrayList<>();
        ArrayList arrayList3 = new ArrayList();
        Iterator<DbEntry> it = arrayList.iterator();
        while (it.hasNext()) {
            DbEntry next = it.next();
            if ((next.cellX <= i && next.spanX + next.cellX > i) || (next.cellY <= i2 && next.spanY + next.cellY > i2)) {
                arrayList3.add(next);
                if (next.cellX >= i) {
                    next.cellX--;
                }
                if (next.cellY >= i2) {
                    next.cellY--;
                }
            } else {
                if (next.cellX > i) {
                    next.cellX--;
                }
                if (next.cellY > i2) {
                    next.cellY--;
                }
                arrayList2.add(next);
                gridOccupancy.markCells((ItemInfo) next, true);
            }
        }
        OptimalPlacementSolution optimalPlacementSolution = new OptimalPlacementSolution(this, gridOccupancy, arrayList3, i3);
        optimalPlacementSolution.find();
        arrayList2.addAll(optimalPlacementSolution.finalPlacedItems);
        fArr[0] = optimalPlacementSolution.lowestWeightLoss;
        fArr[1] = optimalPlacementSolution.lowestMoveCost;
        return arrayList2;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class OptimalPlacementSolution {
        ArrayList<DbEntry> finalPlacedItems;
        private final boolean ignoreMove;
        private final ArrayList<DbEntry> itemsToPlace;
        float lowestMoveCost;
        float lowestWeightLoss;
        private final GridOccupancy occupied;
        private final int startY;

        public OptimalPlacementSolution(GridSizeMigrationTask gridSizeMigrationTask, GridOccupancy gridOccupancy, ArrayList<DbEntry> arrayList, int i) {
            this(gridOccupancy, arrayList, i, false);
        }

        public OptimalPlacementSolution(GridOccupancy gridOccupancy, ArrayList<DbEntry> arrayList, int i, boolean z) {
            this.lowestWeightLoss = Float.MAX_VALUE;
            this.lowestMoveCost = Float.MAX_VALUE;
            this.occupied = gridOccupancy;
            this.itemsToPlace = arrayList;
            this.ignoreMove = z;
            this.startY = i;
            Collections.sort(this.itemsToPlace);
        }

        public void find() {
            find(0, 0.0f, 0.0f, new ArrayList<>());
        }

        public void find(int i, float f, float f2, ArrayList<DbEntry> arrayList) {
            float f3;
            float f4;
            int i2;
            float f5;
            float f6;
            int i3;
            int i4;
            float f7 = f;
            if (f7 < this.lowestWeightLoss) {
                if (f7 == this.lowestWeightLoss && f2 >= this.lowestMoveCost) {
                    return;
                }
                if (i >= this.itemsToPlace.size()) {
                    this.lowestWeightLoss = f7;
                    this.lowestMoveCost = f2;
                    this.finalPlacedItems = GridSizeMigrationTask.deepCopy(arrayList);
                    return;
                }
                DbEntry dbEntry = this.itemsToPlace.get(i);
                int i5 = dbEntry.cellX;
                int i6 = dbEntry.cellY;
                ArrayList<DbEntry> arrayList2 = new ArrayList<>(arrayList.size() + 1);
                arrayList2.addAll(arrayList);
                arrayList2.add(dbEntry);
                if (dbEntry.spanX > 1 || dbEntry.spanY > 1) {
                    int i7 = dbEntry.spanX;
                    int i8 = dbEntry.spanY;
                    for (int i9 = this.startY; i9 < GridSizeMigrationTask.this.mTrgY; i9++) {
                        int i10 = 0;
                        while (i10 < GridSizeMigrationTask.this.mTrgX) {
                            if (i10 != i5) {
                                dbEntry.cellX = i10;
                                f3 = 1.0f;
                                f4 = f2 + 1.0f;
                            } else {
                                f3 = 1.0f;
                                f4 = f2;
                            }
                            if (i9 != i6) {
                                dbEntry.cellY = i9;
                                f4 += f3;
                            }
                            if (this.ignoreMove) {
                                f4 = f2;
                            }
                            if (this.occupied.isRegionVacant(i10, i9, i7, i8)) {
                                this.occupied.markCells((ItemInfo) dbEntry, true);
                                find(i + 1, f7, f4, arrayList2);
                                this.occupied.markCells((ItemInfo) dbEntry, false);
                            }
                            if (i7 > dbEntry.minSpanX && this.occupied.isRegionVacant(i10, i9, i7 - 1, i8)) {
                                dbEntry.spanX--;
                                this.occupied.markCells((ItemInfo) dbEntry, true);
                                find(i + 1, f7, f4 + 1.0f, arrayList2);
                                this.occupied.markCells((ItemInfo) dbEntry, false);
                                dbEntry.spanX++;
                            }
                            if (i8 > dbEntry.minSpanY && this.occupied.isRegionVacant(i10, i9, i7, i8 - 1)) {
                                dbEntry.spanY--;
                                this.occupied.markCells((ItemInfo) dbEntry, true);
                                find(i + 1, f7, f4 + 1.0f, arrayList2);
                                this.occupied.markCells((ItemInfo) dbEntry, false);
                                dbEntry.spanY++;
                            }
                            if (i8 > dbEntry.minSpanY && i7 > dbEntry.minSpanX) {
                                i2 = i7;
                                if (this.occupied.isRegionVacant(i10, i9, i7 - 1, i8 - 1)) {
                                    dbEntry.spanX--;
                                    dbEntry.spanY--;
                                    this.occupied.markCells((ItemInfo) dbEntry, true);
                                    find(i + 1, f7, f4 + GridSizeMigrationTask.WT_WIDGET_MIN, arrayList2);
                                    this.occupied.markCells((ItemInfo) dbEntry, false);
                                    dbEntry.spanX++;
                                    dbEntry.spanY++;
                                    dbEntry.cellX = i5;
                                    dbEntry.cellY = i6;
                                    i10++;
                                    i7 = i2;
                                }
                            } else {
                                i2 = i7;
                            }
                            dbEntry.cellX = i5;
                            dbEntry.cellY = i6;
                            i10++;
                            i7 = i2;
                        }
                    }
                    find(i + 1, f7 + dbEntry.weight, f2, arrayList);
                    return;
                }
                int i11 = Integer.MAX_VALUE;
                int i12 = Integer.MAX_VALUE;
                int i13 = Integer.MAX_VALUE;
                for (int i14 = this.startY; i14 < GridSizeMigrationTask.this.mTrgY; i14++) {
                    for (int i15 = 0; i15 < GridSizeMigrationTask.this.mTrgX; i15++) {
                        if (this.occupied.cells[i15][i14]) {
                            i3 = i11;
                        } else {
                            if (!this.ignoreMove) {
                                i3 = i11;
                                i4 = ((dbEntry.cellX - i15) * (dbEntry.cellX - i15)) + ((dbEntry.cellY - i14) * (dbEntry.cellY - i14));
                            } else {
                                i3 = i11;
                                i4 = 0;
                            }
                            if (i4 < i13) {
                                i12 = i14;
                                i13 = i4;
                                i11 = i15;
                            }
                        }
                        i11 = i3;
                    }
                }
                if (i11 < GridSizeMigrationTask.this.mTrgX && i12 < GridSizeMigrationTask.this.mTrgY) {
                    if (i11 != i5) {
                        dbEntry.cellX = i11;
                        f5 = 1.0f;
                        f6 = f2 + 1.0f;
                    } else {
                        f5 = 1.0f;
                        f6 = f2;
                    }
                    if (i12 != i6) {
                        dbEntry.cellY = i12;
                        f6 += f5;
                    }
                    if (this.ignoreMove) {
                        f6 = f2;
                    }
                    this.occupied.markCells((ItemInfo) dbEntry, true);
                    int i16 = i + 1;
                    find(i16, f7, f6, arrayList2);
                    this.occupied.markCells((ItemInfo) dbEntry, false);
                    dbEntry.cellX = i5;
                    dbEntry.cellY = i6;
                    if (i16 < this.itemsToPlace.size() && this.itemsToPlace.get(i16).weight >= dbEntry.weight && !this.ignoreMove) {
                        find(i16, f7 + dbEntry.weight, f2, arrayList);
                        return;
                    }
                    return;
                }
                for (int i17 = i + 1; i17 < this.itemsToPlace.size(); i17++) {
                    f7 += this.itemsToPlace.get(i17).weight;
                }
                find(this.itemsToPlace.size(), f7 + dbEntry.weight, f2, arrayList);
            }
        }
    }

    private ArrayList<DbEntry> loadHotseatEntries() {
        Cursor query = this.mContext.getContentResolver().query(LauncherSettings.Favorites.CONTENT_URI, new String[]{"_id", LauncherSettings.BaseLauncherColumns.ITEM_TYPE, LauncherSettings.BaseLauncherColumns.INTENT, LauncherSettings.Favorites.SCREEN}, "container = -101", null, null, null);
        int columnIndexOrThrow = query.getColumnIndexOrThrow("_id");
        int columnIndexOrThrow2 = query.getColumnIndexOrThrow(LauncherSettings.BaseLauncherColumns.ITEM_TYPE);
        int columnIndexOrThrow3 = query.getColumnIndexOrThrow(LauncherSettings.BaseLauncherColumns.INTENT);
        int columnIndexOrThrow4 = query.getColumnIndexOrThrow(LauncherSettings.Favorites.SCREEN);
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
                    int i = dbEntry.itemType;
                    if (i != 6) {
                        switch (i) {
                            case 0:
                            case 1:
                                break;
                            case 2:
                                int folderItemsCount = getFolderItemsCount(dbEntry.id);
                                if (folderItemsCount == 0) {
                                    throw new Exception("Folder is empty");
                                }
                                dbEntry.weight = 0.5f * folderItemsCount;
                                break;
                            default:
                                throw new Exception("Invalid item type");
                        }
                        arrayList.add(dbEntry);
                    }
                    verifyIntent(query.getString(columnIndexOrThrow3));
                    dbEntry.weight = dbEntry.itemType == 0 ? WT_APPLICATION : 1.0f;
                    arrayList.add(dbEntry);
                } catch (Exception e) {
                    Log.d(TAG, "Removing item " + dbEntry.id, e);
                    this.mEntryToRemove.add(Long.valueOf(dbEntry.id));
                }
            }
        }
        query.close();
        return arrayList;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public ArrayList<DbEntry> loadWorkspaceEntries(long j) {
        long j2 = j;
        String[] strArr = {"_id", LauncherSettings.BaseLauncherColumns.ITEM_TYPE, LauncherSettings.Favorites.CELLX, LauncherSettings.Favorites.CELLY, LauncherSettings.Favorites.SPANX, LauncherSettings.Favorites.SPANY, LauncherSettings.BaseLauncherColumns.INTENT, LauncherSettings.Favorites.APPWIDGET_PROVIDER, LauncherSettings.Favorites.APPWIDGET_ID};
        Cursor queryWorkspace = queryWorkspace(strArr, "container = -100 AND screen = " + j2);
        int columnIndexOrThrow = queryWorkspace.getColumnIndexOrThrow("_id");
        int columnIndexOrThrow2 = queryWorkspace.getColumnIndexOrThrow(LauncherSettings.BaseLauncherColumns.ITEM_TYPE);
        int columnIndexOrThrow3 = queryWorkspace.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLX);
        int columnIndexOrThrow4 = queryWorkspace.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLY);
        int columnIndexOrThrow5 = queryWorkspace.getColumnIndexOrThrow(LauncherSettings.Favorites.SPANX);
        int columnIndexOrThrow6 = queryWorkspace.getColumnIndexOrThrow(LauncherSettings.Favorites.SPANY);
        int columnIndexOrThrow7 = queryWorkspace.getColumnIndexOrThrow(LauncherSettings.BaseLauncherColumns.INTENT);
        int columnIndexOrThrow8 = queryWorkspace.getColumnIndexOrThrow(LauncherSettings.Favorites.APPWIDGET_PROVIDER);
        int columnIndexOrThrow9 = queryWorkspace.getColumnIndexOrThrow(LauncherSettings.Favorites.APPWIDGET_ID);
        ArrayList<DbEntry> arrayList = new ArrayList<>();
        while (queryWorkspace.moveToNext()) {
            DbEntry dbEntry = new DbEntry();
            int i = columnIndexOrThrow9;
            ArrayList<DbEntry> arrayList2 = arrayList;
            dbEntry.id = queryWorkspace.getLong(columnIndexOrThrow);
            dbEntry.itemType = queryWorkspace.getInt(columnIndexOrThrow2);
            dbEntry.cellX = queryWorkspace.getInt(columnIndexOrThrow3);
            dbEntry.cellY = queryWorkspace.getInt(columnIndexOrThrow4);
            dbEntry.spanX = queryWorkspace.getInt(columnIndexOrThrow5);
            dbEntry.spanY = queryWorkspace.getInt(columnIndexOrThrow6);
            dbEntry.screenId = j2;
            try {
                int i2 = dbEntry.itemType;
                if (i2 != 4) {
                    if (i2 != 6) {
                        switch (i2) {
                            case 0:
                            case 1:
                                break;
                            case 2:
                                int folderItemsCount = getFolderItemsCount(dbEntry.id);
                                if (folderItemsCount == 0) {
                                    throw new Exception("Folder is empty");
                                }
                                dbEntry.weight = 0.5f * folderItemsCount;
                                break;
                            default:
                                throw new Exception("Invalid item type");
                        }
                        columnIndexOrThrow9 = i;
                    }
                    verifyIntent(queryWorkspace.getString(columnIndexOrThrow7));
                    dbEntry.weight = dbEntry.itemType == 0 ? WT_APPLICATION : 1.0f;
                    columnIndexOrThrow9 = i;
                } else {
                    verifyPackage(ComponentName.unflattenFromString(queryWorkspace.getString(columnIndexOrThrow8)).getPackageName());
                    dbEntry.weight = Math.max((float) WT_WIDGET_MIN, WT_WIDGET_FACTOR * dbEntry.spanX * dbEntry.spanY);
                    columnIndexOrThrow9 = i;
                    try {
                        LauncherAppWidgetProviderInfo launcherAppWidgetInfo = AppWidgetManagerCompat.getInstance(this.mContext).getLauncherAppWidgetInfo(queryWorkspace.getInt(columnIndexOrThrow9));
                        Point point = null;
                        if (launcherAppWidgetInfo != null) {
                            point = launcherAppWidgetInfo.getMinSpans();
                        }
                        if (point != null) {
                            dbEntry.minSpanX = point.x > 0 ? point.x : dbEntry.spanX;
                            dbEntry.minSpanY = point.y > 0 ? point.y : dbEntry.spanY;
                        } else {
                            dbEntry.minSpanY = 2;
                            dbEntry.minSpanX = 2;
                        }
                        if (dbEntry.minSpanX > this.mTrgX || dbEntry.minSpanY > this.mTrgY) {
                            arrayList = arrayList2;
                            try {
                                throw new Exception("Widget can't be resized down to fit the grid");
                            } catch (Exception e) {
                                e = e;
                                Log.d(TAG, "Removing item " + dbEntry.id, e);
                                this.mEntryToRemove.add(Long.valueOf(dbEntry.id));
                                columnIndexOrThrow = columnIndexOrThrow;
                                columnIndexOrThrow2 = columnIndexOrThrow2;
                                j2 = j;
                            }
                        }
                    } catch (Exception e2) {
                        e = e2;
                        arrayList = arrayList2;
                        Log.d(TAG, "Removing item " + dbEntry.id, e);
                        this.mEntryToRemove.add(Long.valueOf(dbEntry.id));
                        columnIndexOrThrow = columnIndexOrThrow;
                        columnIndexOrThrow2 = columnIndexOrThrow2;
                        j2 = j;
                    }
                }
                arrayList = arrayList2;
                arrayList.add(dbEntry);
            } catch (Exception e3) {
                e = e3;
                columnIndexOrThrow9 = i;
            }
        }
        queryWorkspace.close();
        return arrayList;
    }

    private int getFolderItemsCount(long j) {
        String[] strArr = {"_id", LauncherSettings.BaseLauncherColumns.INTENT};
        Cursor queryWorkspace = queryWorkspace(strArr, "container = " + j);
        int i = 0;
        while (queryWorkspace.moveToNext()) {
            try {
                verifyIntent(queryWorkspace.getString(1));
                i++;
            } catch (Exception e) {
                this.mEntryToRemove.add(Long.valueOf(queryWorkspace.getLong(0)));
            }
        }
        queryWorkspace.close();
        return i;
    }

    protected Cursor queryWorkspace(String[] strArr, String str) {
        return this.mContext.getContentResolver().query(LauncherSettings.Favorites.CONTENT_URI, strArr, str, null, null, null);
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

    /* JADX INFO: Access modifiers changed from: protected */
    /* loaded from: classes.dex */
    public static class DbEntry extends ItemInfo implements Comparable<DbEntry> {
        public float weight;

        public DbEntry copy() {
            DbEntry dbEntry = new DbEntry();
            dbEntry.copyFrom(this);
            dbEntry.weight = this.weight;
            dbEntry.minSpanX = this.minSpanX;
            dbEntry.minSpanY = this.minSpanY;
            return dbEntry;
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

        public boolean columnsSame(DbEntry dbEntry) {
            return dbEntry.cellX == this.cellX && dbEntry.cellY == this.cellY && dbEntry.spanX == this.spanX && dbEntry.spanY == this.spanY && dbEntry.screenId == this.screenId;
        }

        public void addToContentValues(ContentValues contentValues) {
            contentValues.put(LauncherSettings.Favorites.SCREEN, Long.valueOf(this.screenId));
            contentValues.put(LauncherSettings.Favorites.CELLX, Integer.valueOf(this.cellX));
            contentValues.put(LauncherSettings.Favorites.CELLY, Integer.valueOf(this.cellY));
            contentValues.put(LauncherSettings.Favorites.SPANX, Integer.valueOf(this.spanX));
            contentValues.put(LauncherSettings.Favorites.SPANY, Integer.valueOf(this.spanY));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static ArrayList<DbEntry> deepCopy(ArrayList<DbEntry> arrayList) {
        ArrayList<DbEntry> arrayList2 = new ArrayList<>(arrayList.size());
        Iterator<DbEntry> it = arrayList.iterator();
        while (it.hasNext()) {
            arrayList2.add(it.next().copy());
        }
        return arrayList2;
    }

    private static Point parsePoint(String str) {
        String[] split = str.split(",");
        return new Point(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
    }

    private static String getPointString(int i, int i2) {
        return String.format(Locale.ENGLISH, "%d,%d", Integer.valueOf(i), Integer.valueOf(i2));
    }

    public static void markForMigration(Context context, int i, int i2, int i3) {
        Utilities.getPrefs(context).edit().putString(KEY_MIGRATION_SRC_WORKSPACE_SIZE, getPointString(i, i2)).putInt(KEY_MIGRATION_SRC_HOTSEAT_COUNT, i3).apply();
    }

    public static boolean migrateGridIfNeeded(Context context) {
        SharedPreferences prefs = Utilities.getPrefs(context);
        InvariantDeviceProfile idp = LauncherAppState.getIDP(context);
        String pointString = getPointString(idp.numColumns, idp.numRows);
        if (pointString.equals(prefs.getString(KEY_MIGRATION_SRC_WORKSPACE_SIZE, "")) && idp.numHotseatIcons == prefs.getInt(KEY_MIGRATION_SRC_HOTSEAT_COUNT, idp.numHotseatIcons)) {
            return true;
        }
        long currentTimeMillis = System.currentTimeMillis();
        try {
            try {
                HashSet<String> validPackages = getValidPackages(context);
                int i = prefs.getInt(KEY_MIGRATION_SRC_HOTSEAT_COUNT, idp.numHotseatIcons);
                boolean migrateHotseat = i != idp.numHotseatIcons ? new GridSizeMigrationTask(context, LauncherAppState.getIDP(context), validPackages, i, idp.numHotseatIcons).migrateHotseat() : false;
                if (new MultiStepMigrationTask(validPackages, context).migrate(parsePoint(prefs.getString(KEY_MIGRATION_SRC_WORKSPACE_SIZE, pointString)), new Point(idp.numColumns, idp.numRows))) {
                    migrateHotseat = true;
                }
                if (migrateHotseat) {
                    Cursor query = context.getContentResolver().query(LauncherSettings.Favorites.CONTENT_URI, null, null, null, null);
                    boolean moveToNext = query.moveToNext();
                    query.close();
                    if (!moveToNext) {
                        throw new Exception("Removed every thing during grid resize");
                    }
                }
                Log.v(TAG, "Workspace migration completed in " + (System.currentTimeMillis() - currentTimeMillis));
                prefs.edit().putString(KEY_MIGRATION_SRC_WORKSPACE_SIZE, pointString).putInt(KEY_MIGRATION_SRC_HOTSEAT_COUNT, idp.numHotseatIcons).apply();
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error during grid migration", e);
                Log.v(TAG, "Workspace migration completed in " + (System.currentTimeMillis() - currentTimeMillis));
                prefs.edit().putString(KEY_MIGRATION_SRC_WORKSPACE_SIZE, pointString).putInt(KEY_MIGRATION_SRC_HOTSEAT_COUNT, idp.numHotseatIcons).apply();
                return false;
            }
        } catch (Throwable th) {
            Log.v(TAG, "Workspace migration completed in " + (System.currentTimeMillis() - currentTimeMillis));
            prefs.edit().putString(KEY_MIGRATION_SRC_WORKSPACE_SIZE, pointString).putInt(KEY_MIGRATION_SRC_HOTSEAT_COUNT, idp.numHotseatIcons).apply();
            throw th;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public static HashSet<String> getValidPackages(Context context) {
        HashSet<String> hashSet = new HashSet<>();
        for (PackageInfo packageInfo : context.getPackageManager().getInstalledPackages(8192)) {
            hashSet.add(packageInfo.packageName);
        }
        hashSet.addAll(PackageInstallerCompat.getInstance(context).updateAndGetActiveSessionCache().keySet());
        return hashSet;
    }

    public static LongArrayMap<Object> removeBrokenHotseatItems(Context context) throws Exception {
        GridSizeMigrationTask gridSizeMigrationTask = new GridSizeMigrationTask(context, LauncherAppState.getIDP(context), getValidPackages(context), Integer.MAX_VALUE, Integer.MAX_VALUE);
        ArrayList<DbEntry> loadHotseatEntries = gridSizeMigrationTask.loadHotseatEntries();
        gridSizeMigrationTask.applyOperations();
        LongArrayMap<Object> longArrayMap = new LongArrayMap<>();
        Iterator<DbEntry> it = loadHotseatEntries.iterator();
        while (it.hasNext()) {
            DbEntry next = it.next();
            longArrayMap.put(next.screenId, next);
        }
        return longArrayMap;
    }

    /* loaded from: classes.dex */
    protected static class MultiStepMigrationTask {
        private final Context mContext;
        private final HashSet<String> mValidPackages;

        public MultiStepMigrationTask(HashSet<String> hashSet, Context context) {
            this.mValidPackages = hashSet;
            this.mContext = context;
        }

        public boolean migrate(Point point, Point point2) throws Exception {
            boolean z = false;
            if (!point2.equals(point)) {
                if (point.x < point2.x) {
                    point.x = point2.x;
                }
                if (point.y < point2.y) {
                    point.y = point2.y;
                }
                while (!point2.equals(point)) {
                    Point point3 = new Point(point);
                    if (point2.x < point3.x) {
                        point3.x--;
                    }
                    if (point2.y < point3.y) {
                        point3.y--;
                    }
                    if (runStepTask(point, point3)) {
                        z = true;
                    }
                    point.set(point3.x, point3.y);
                }
            }
            return z;
        }

        protected boolean runStepTask(Point point, Point point2) throws Exception {
            return new GridSizeMigrationTask(this.mContext, LauncherAppState.getIDP(this.mContext), this.mValidPackages, point, point2).migrateWorkspace();
        }
    }
}
