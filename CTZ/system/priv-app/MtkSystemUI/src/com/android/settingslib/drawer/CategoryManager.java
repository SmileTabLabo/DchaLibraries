package com.android.settingslib.drawer;

import android.content.ComponentName;
import android.content.Context;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.util.Pair;
import com.android.settingslib.applications.InterestingConfigChanges;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
/* loaded from: classes.dex */
public class CategoryManager {
    private static CategoryManager sInstance;
    private List<DashboardCategory> mCategories;
    private String mExtraAction;
    private final Map<Pair<String, String>, Tile> mTileByComponentCache = new ArrayMap();
    private final Map<String, DashboardCategory> mCategoryByKeyMap = new ArrayMap();
    private final InterestingConfigChanges mInterestingConfigChanges = new InterestingConfigChanges();

    public static CategoryManager get(Context context) {
        return get(context, null);
    }

    public static CategoryManager get(Context context, String str) {
        if (sInstance == null) {
            sInstance = new CategoryManager(context, str);
        }
        return sInstance;
    }

    CategoryManager(Context context, String str) {
        this.mInterestingConfigChanges.applyNewConfig(context.getResources());
        this.mExtraAction = str;
    }

    public synchronized void reloadAllCategories(Context context, String str) {
        boolean applyNewConfig = this.mInterestingConfigChanges.applyNewConfig(context.getResources());
        this.mCategories = null;
        tryInitCategories(context, applyNewConfig, str);
    }

    public synchronized void updateCategoryFromBlacklist(Set<ComponentName> set) {
        if (this.mCategories == null) {
            Log.w("CategoryManager", "Category is null, skipping blacklist update");
        }
        for (int i = 0; i < this.mCategories.size(); i++) {
            DashboardCategory dashboardCategory = this.mCategories.get(i);
            int i2 = 0;
            while (i2 < dashboardCategory.getTilesCount()) {
                if (set.contains(dashboardCategory.getTile(i2).intent.getComponent())) {
                    int i3 = i2 - 1;
                    dashboardCategory.removeTile(i2);
                    i2 = i3;
                }
                i2++;
            }
        }
    }

    private synchronized void tryInitCategories(Context context, boolean z, String str) {
        if (this.mCategories == null) {
            if (z) {
                this.mTileByComponentCache.clear();
            }
            this.mCategoryByKeyMap.clear();
            this.mCategories = TileUtils.getCategories(context, this.mTileByComponentCache, false, this.mExtraAction, str);
            for (DashboardCategory dashboardCategory : this.mCategories) {
                this.mCategoryByKeyMap.put(dashboardCategory.key, dashboardCategory);
            }
            backwardCompatCleanupForCategory(this.mTileByComponentCache, this.mCategoryByKeyMap);
            sortCategories(context, this.mCategoryByKeyMap);
            filterDuplicateTiles(this.mCategoryByKeyMap);
        }
    }

    synchronized void backwardCompatCleanupForCategory(Map<Pair<String, String>, Tile> map, Map<String, DashboardCategory> map2) {
        HashMap hashMap = new HashMap();
        for (Map.Entry<Pair<String, String>, Tile> entry : map.entrySet()) {
            String str = (String) entry.getKey().first;
            List list = (List) hashMap.get(str);
            if (list == null) {
                list = new ArrayList();
                hashMap.put(str, list);
            }
            list.add(entry.getValue());
        }
        for (Map.Entry entry2 : hashMap.entrySet()) {
            List<Tile> list2 = (List) entry2.getValue();
            Iterator it = list2.iterator();
            boolean z = true;
            boolean z2 = false;
            while (true) {
                if (it.hasNext()) {
                    if (!CategoryKey.KEY_COMPAT_MAP.containsKey(((Tile) it.next()).category)) {
                        break;
                    }
                    z2 = true;
                } else {
                    z = false;
                    break;
                }
            }
            if (z2 && !z) {
                for (Tile tile : list2) {
                    String str2 = CategoryKey.KEY_COMPAT_MAP.get(tile.category);
                    tile.category = str2;
                    DashboardCategory dashboardCategory = map2.get(str2);
                    if (dashboardCategory == null) {
                        dashboardCategory = new DashboardCategory();
                        map2.put(str2, dashboardCategory);
                    }
                    dashboardCategory.addTile(tile);
                }
            }
        }
    }

    synchronized void sortCategories(Context context, Map<String, DashboardCategory> map) {
        for (Map.Entry<String, DashboardCategory> entry : map.entrySet()) {
            entry.getValue().sortTiles(context.getPackageName());
        }
    }

    synchronized void filterDuplicateTiles(Map<String, DashboardCategory> map) {
        for (Map.Entry<String, DashboardCategory> entry : map.entrySet()) {
            DashboardCategory value = entry.getValue();
            int tilesCount = value.getTilesCount();
            ArraySet arraySet = new ArraySet();
            for (int i = tilesCount - 1; i >= 0; i--) {
                Tile tile = value.getTile(i);
                if (tile.intent != null) {
                    ComponentName component = tile.intent.getComponent();
                    if (arraySet.contains(component)) {
                        value.removeTile(i);
                    } else {
                        arraySet.add(component);
                    }
                }
            }
        }
    }
}
