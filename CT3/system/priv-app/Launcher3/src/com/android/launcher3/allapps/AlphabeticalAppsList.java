package com.android.launcher3.allapps;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import com.android.launcher3.AppInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.compat.AlphabeticIndexCompat;
import com.android.launcher3.compat.PackageInstallerCompat;
import com.android.launcher3.model.AppNameComparator;
import com.android.launcher3.util.ComponentKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
/* loaded from: a.zip:com/android/launcher3/allapps/AlphabeticalAppsList.class */
public class AlphabeticalAppsList {
    private RecyclerView.Adapter mAdapter;
    private AppNameComparator mAppNameComparator;
    private AlphabeticIndexCompat mIndexer;
    private Launcher mLauncher;
    private MergeAlgorithm mMergeAlgorithm;
    private int mNumAppRowsInAdapter;
    private int mNumAppsPerRow;
    private int mNumPredictedAppsPerRow;
    private ArrayList<ComponentKey> mSearchResults;
    private final int mFastScrollDistributionMode = 1;
    private final List<AppInfo> mApps = new ArrayList();
    private final HashMap<ComponentKey, AppInfo> mComponentToAppMap = new HashMap<>();
    private List<AppInfo> mFilteredApps = new ArrayList();
    private List<AdapterItem> mAdapterItems = new ArrayList();
    private List<SectionInfo> mSections = new ArrayList();
    private List<FastScrollSectionInfo> mFastScrollerSections = new ArrayList();
    private List<ComponentKey> mPredictedAppComponents = new ArrayList();
    private List<AppInfo> mPredictedApps = new ArrayList();
    private HashMap<CharSequence, String> mCachedSectionNames = new HashMap<>();

    /* loaded from: a.zip:com/android/launcher3/allapps/AlphabeticalAppsList$AdapterItem.class */
    public static class AdapterItem {
        public int position;
        public int rowAppIndex;
        public int rowIndex;
        public SectionInfo sectionInfo;
        public int viewType;
        public String sectionName = null;
        public int sectionAppIndex = -1;
        public AppInfo appInfo = null;
        public int appIndex = -1;

        public static AdapterItem asApp(int i, SectionInfo sectionInfo, String str, int i2, AppInfo appInfo, int i3) {
            AdapterItem adapterItem = new AdapterItem();
            adapterItem.viewType = 1;
            adapterItem.position = i;
            adapterItem.sectionInfo = sectionInfo;
            adapterItem.sectionName = str;
            adapterItem.sectionAppIndex = i2;
            adapterItem.appInfo = appInfo;
            adapterItem.appIndex = i3;
            return adapterItem;
        }

        public static AdapterItem asDivider(int i) {
            AdapterItem adapterItem = new AdapterItem();
            adapterItem.viewType = 4;
            adapterItem.position = i;
            return adapterItem;
        }

        public static AdapterItem asEmptySearch(int i) {
            AdapterItem adapterItem = new AdapterItem();
            adapterItem.viewType = 3;
            adapterItem.position = i;
            return adapterItem;
        }

        public static AdapterItem asMarketSearch(int i) {
            AdapterItem adapterItem = new AdapterItem();
            adapterItem.viewType = 5;
            adapterItem.position = i;
            return adapterItem;
        }

        public static AdapterItem asPredictedApp(int i, SectionInfo sectionInfo, String str, int i2, AppInfo appInfo, int i3) {
            AdapterItem asApp = asApp(i, sectionInfo, str, i2, appInfo, i3);
            asApp.viewType = 2;
            return asApp;
        }

        public static AdapterItem asSectionBreak(int i, SectionInfo sectionInfo) {
            AdapterItem adapterItem = new AdapterItem();
            adapterItem.viewType = 0;
            adapterItem.position = i;
            adapterItem.sectionInfo = sectionInfo;
            sectionInfo.sectionBreakItem = adapterItem;
            return adapterItem;
        }
    }

    /* loaded from: a.zip:com/android/launcher3/allapps/AlphabeticalAppsList$FastScrollSectionInfo.class */
    public static class FastScrollSectionInfo {
        public AdapterItem fastScrollToItem;
        public String sectionName;
        public float touchFraction;

        public FastScrollSectionInfo(String str) {
            this.sectionName = str;
        }
    }

    /* loaded from: a.zip:com/android/launcher3/allapps/AlphabeticalAppsList$MergeAlgorithm.class */
    public interface MergeAlgorithm {
        boolean continueMerging(SectionInfo sectionInfo, SectionInfo sectionInfo2, int i, int i2, int i3);
    }

    /* loaded from: a.zip:com/android/launcher3/allapps/AlphabeticalAppsList$SectionInfo.class */
    public static class SectionInfo {
        public AdapterItem firstAppItem;
        public int numApps;
        public AdapterItem sectionBreakItem;
    }

    public AlphabeticalAppsList(Context context) {
        this.mLauncher = (Launcher) context;
        this.mIndexer = new AlphabeticIndexCompat(context);
        this.mAppNameComparator = new AppNameComparator(context);
    }

    private String getAndUpdateCachedSectionName(CharSequence charSequence) {
        String str = this.mCachedSectionNames.get(charSequence);
        String str2 = str;
        if (str == null) {
            str2 = this.mIndexer.computeSectionName(charSequence);
            this.mCachedSectionNames.put(charSequence, str2);
        }
        return str2;
    }

    private List<AppInfo> getFiltersAppInfos() {
        if (this.mSearchResults == null) {
            return this.mApps;
        }
        ArrayList arrayList = new ArrayList();
        for (ComponentKey componentKey : this.mSearchResults) {
            AppInfo appInfo = this.mComponentToAppMap.get(componentKey);
            if (appInfo != null) {
                arrayList.add(appInfo);
            }
        }
        return arrayList;
    }

    private void mergeSections() {
        if (this.mMergeAlgorithm == null || this.mNumAppsPerRow == 0 || hasFilter()) {
            return;
        }
        for (int i = 0; i < this.mSections.size() - 1; i++) {
            SectionInfo sectionInfo = this.mSections.get(i);
            int i2 = sectionInfo.numApps;
            for (int i3 = 1; i < this.mSections.size() - 1 && this.mMergeAlgorithm.continueMerging(sectionInfo, this.mSections.get(i + 1), i2, this.mNumAppsPerRow, i3); i3++) {
                SectionInfo remove = this.mSections.remove(i + 1);
                this.mAdapterItems.remove(remove.sectionBreakItem);
                int indexOf = this.mAdapterItems.indexOf(sectionInfo.firstAppItem) + sectionInfo.numApps;
                for (int i4 = indexOf; i4 < remove.numApps + indexOf; i4++) {
                    AdapterItem adapterItem = this.mAdapterItems.get(i4);
                    adapterItem.sectionInfo = sectionInfo;
                    adapterItem.sectionAppIndex += sectionInfo.numApps;
                }
                for (int indexOf2 = this.mAdapterItems.indexOf(remove.firstAppItem); indexOf2 < this.mAdapterItems.size(); indexOf2++) {
                    this.mAdapterItems.get(indexOf2).position--;
                }
                sectionInfo.numApps += remove.numApps;
                i2 += remove.numApps;
            }
        }
    }

    private void onAppsUpdated() {
        this.mApps.clear();
        this.mApps.addAll(this.mComponentToAppMap.values());
        Collections.sort(this.mApps, this.mAppNameComparator.getAppInfoComparator());
        if (this.mLauncher.getResources().getConfiguration().locale.equals(Locale.SIMPLIFIED_CHINESE)) {
            TreeMap treeMap = new TreeMap(this.mAppNameComparator.getSectionNameComparator());
            for (AppInfo appInfo : this.mApps) {
                String andUpdateCachedSectionName = getAndUpdateCachedSectionName(appInfo.title);
                ArrayList arrayList = (ArrayList) treeMap.get(andUpdateCachedSectionName);
                ArrayList arrayList2 = arrayList;
                if (arrayList == null) {
                    arrayList2 = new ArrayList();
                    treeMap.put(andUpdateCachedSectionName, arrayList2);
                }
                arrayList2.add(appInfo);
            }
            ArrayList arrayList3 = new ArrayList(this.mApps.size());
            for (Map.Entry entry : treeMap.entrySet()) {
                arrayList3.addAll((Collection) entry.getValue());
            }
            this.mApps.clear();
            this.mApps.addAll(arrayList3);
        } else {
            for (AppInfo appInfo2 : this.mApps) {
                getAndUpdateCachedSectionName(appInfo2.title);
            }
        }
        updateAdapterItems();
    }

    private void updateAdapterItems() {
        int i;
        int i2;
        SectionInfo sectionInfo;
        Object obj;
        Object obj2 = null;
        int i3 = 0;
        this.mFilteredApps.clear();
        this.mFastScrollerSections.clear();
        this.mAdapterItems.clear();
        this.mSections.clear();
        this.mPredictedApps.clear();
        int i4 = 0;
        SectionInfo sectionInfo2 = null;
        int i5 = 0;
        FastScrollSectionInfo fastScrollSectionInfo = null;
        if (this.mPredictedAppComponents != null) {
            if (this.mPredictedAppComponents.isEmpty()) {
                fastScrollSectionInfo = null;
                i5 = 0;
                sectionInfo2 = null;
                i4 = 0;
            } else {
                i4 = 0;
                sectionInfo2 = null;
                i5 = 0;
                fastScrollSectionInfo = null;
                if (!hasFilter()) {
                    for (ComponentKey componentKey : this.mPredictedAppComponents) {
                        AppInfo appInfo = this.mComponentToAppMap.get(componentKey);
                        if (appInfo != null) {
                            this.mPredictedApps.add(appInfo);
                        } else if (LauncherAppState.isDogfoodBuild()) {
                            Log.e("AlphabeticalAppsList", "Predicted app not found: " + componentKey.flattenToString(this.mLauncher));
                        }
                        if (this.mPredictedApps.size() == this.mNumPredictedAppsPerRow) {
                            break;
                        }
                    }
                    i4 = 0;
                    sectionInfo2 = null;
                    i5 = 0;
                    fastScrollSectionInfo = null;
                    if (!this.mPredictedApps.isEmpty()) {
                        SectionInfo sectionInfo3 = new SectionInfo();
                        FastScrollSectionInfo fastScrollSectionInfo2 = new FastScrollSectionInfo("");
                        int i6 = 1;
                        AdapterItem asSectionBreak = AdapterItem.asSectionBreak(0, sectionInfo3);
                        this.mSections.add(sectionInfo3);
                        this.mFastScrollerSections.add(fastScrollSectionInfo2);
                        this.mAdapterItems.add(asSectionBreak);
                        Iterator<T> it = this.mPredictedApps.iterator();
                        while (true) {
                            i4 = i6;
                            sectionInfo2 = sectionInfo3;
                            i5 = i3;
                            fastScrollSectionInfo = fastScrollSectionInfo2;
                            if (!it.hasNext()) {
                                break;
                            }
                            AppInfo appInfo2 = (AppInfo) it.next();
                            int i7 = sectionInfo3.numApps;
                            sectionInfo3.numApps = i7 + 1;
                            AdapterItem asPredictedApp = AdapterItem.asPredictedApp(i6, sectionInfo3, "", i7, appInfo2, i3);
                            if (sectionInfo3.firstAppItem == null) {
                                sectionInfo3.firstAppItem = asPredictedApp;
                                fastScrollSectionInfo2.fastScrollToItem = asPredictedApp;
                            }
                            this.mAdapterItems.add(asPredictedApp);
                            this.mFilteredApps.add(appInfo2);
                            i3++;
                            i6++;
                        }
                    }
                }
            }
        }
        SectionInfo sectionInfo4 = sectionInfo2;
        for (AppInfo appInfo3 : getFiltersAppInfos()) {
            String andUpdateCachedSectionName = getAndUpdateCachedSectionName(appInfo3.title);
            if (sectionInfo4 == null || !andUpdateCachedSectionName.equals(obj2)) {
                SectionInfo sectionInfo5 = new SectionInfo();
                FastScrollSectionInfo fastScrollSectionInfo3 = new FastScrollSectionInfo(andUpdateCachedSectionName);
                this.mSections.add(sectionInfo5);
                this.mFastScrollerSections.add(fastScrollSectionInfo3);
                i2 = i4;
                sectionInfo = sectionInfo5;
                fastScrollSectionInfo = fastScrollSectionInfo3;
                obj = andUpdateCachedSectionName;
                if (!hasFilter()) {
                    this.mAdapterItems.add(AdapterItem.asSectionBreak(i4, sectionInfo5));
                    i2 = i4 + 1;
                    sectionInfo = sectionInfo5;
                    fastScrollSectionInfo = fastScrollSectionInfo3;
                    obj = andUpdateCachedSectionName;
                }
            } else {
                obj = obj2;
                sectionInfo = sectionInfo4;
                i2 = i4;
            }
            int i8 = sectionInfo.numApps;
            sectionInfo.numApps = i8 + 1;
            AdapterItem asApp = AdapterItem.asApp(i2, sectionInfo, andUpdateCachedSectionName, i8, appInfo3, i5);
            if (sectionInfo.firstAppItem == null) {
                sectionInfo.firstAppItem = asApp;
                fastScrollSectionInfo.fastScrollToItem = asApp;
            }
            this.mAdapterItems.add(asApp);
            this.mFilteredApps.add(appInfo3);
            i5++;
            i4 = i2 + 1;
            sectionInfo4 = sectionInfo;
            obj2 = obj;
        }
        if (hasFilter()) {
            if (hasNoFilteredResults()) {
                this.mAdapterItems.add(AdapterItem.asEmptySearch(i4));
                i = i4 + 1;
            } else {
                this.mAdapterItems.add(AdapterItem.asDivider(i4));
                i = i4 + 1;
            }
            this.mAdapterItems.add(AdapterItem.asMarketSearch(i));
        }
        mergeSections();
        if (this.mNumAppsPerRow != 0) {
            int i9 = 0;
            int i10 = 0;
            int i11 = -1;
            for (AdapterItem adapterItem : this.mAdapterItems) {
                adapterItem.rowIndex = 0;
                if (adapterItem.viewType == 0) {
                    i9 = 0;
                } else if (adapterItem.viewType == 1 || adapterItem.viewType == 2) {
                    int i12 = i11;
                    if (i9 % this.mNumAppsPerRow == 0) {
                        i10 = 0;
                        i12 = i11 + 1;
                    }
                    adapterItem.rowIndex = i12;
                    adapterItem.rowAppIndex = i10;
                    i9++;
                    i10++;
                    i11 = i12;
                }
            }
            this.mNumAppRowsInAdapter = i11 + 1;
            switch (1) {
                case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                    float f = 1.0f / this.mNumAppRowsInAdapter;
                    for (FastScrollSectionInfo fastScrollSectionInfo4 : this.mFastScrollerSections) {
                        AdapterItem adapterItem2 = fastScrollSectionInfo4.fastScrollToItem;
                        if (adapterItem2.viewType == 1 || adapterItem2.viewType == 2) {
                            fastScrollSectionInfo4.touchFraction = (adapterItem2.rowIndex * f) + (adapterItem2.rowAppIndex * (f / this.mNumAppsPerRow));
                        } else {
                            fastScrollSectionInfo4.touchFraction = 0.0f;
                        }
                    }
                    break;
                case 1:
                    float size = 1.0f / this.mFastScrollerSections.size();
                    float f2 = 0.0f;
                    for (FastScrollSectionInfo fastScrollSectionInfo5 : this.mFastScrollerSections) {
                        AdapterItem adapterItem3 = fastScrollSectionInfo5.fastScrollToItem;
                        if (adapterItem3.viewType == 1 || adapterItem3.viewType == 2) {
                            fastScrollSectionInfo5.touchFraction = f2;
                            f2 += size;
                        } else {
                            fastScrollSectionInfo5.touchFraction = 0.0f;
                        }
                    }
                    break;
            }
        }
        if (this.mAdapter != null) {
            this.mAdapter.notifyDataSetChanged();
        }
    }

    public void addApps(List<AppInfo> list) {
        updateApps(list);
    }

    public List<AdapterItem> getAdapterItems() {
        return this.mAdapterItems;
    }

    public List<AppInfo> getApps() {
        return this.mApps;
    }

    public List<FastScrollSectionInfo> getFastScrollerSections() {
        return this.mFastScrollerSections;
    }

    public int getNumAppRows() {
        return this.mNumAppRowsInAdapter;
    }

    public int getNumFilteredApps() {
        return this.mFilteredApps.size();
    }

    public boolean hasFilter() {
        return this.mSearchResults != null;
    }

    public boolean hasNoFilteredResults() {
        return this.mSearchResults != null ? this.mFilteredApps.isEmpty() : false;
    }

    public boolean hasPredictedComponents() {
        boolean z = false;
        if (this.mPredictedAppComponents != null) {
            z = false;
            if (this.mPredictedAppComponents.size() > 0) {
                z = true;
            }
        }
        return z;
    }

    public void removeApps(List<AppInfo> list) {
        for (AppInfo appInfo : list) {
            this.mComponentToAppMap.remove(appInfo.toComponentKey());
        }
        onAppsUpdated();
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        this.mAdapter = adapter;
    }

    public void setApps(List<AppInfo> list) {
        this.mComponentToAppMap.clear();
        addApps(list);
    }

    public void setNumAppsPerRow(int i, int i2, MergeAlgorithm mergeAlgorithm) {
        this.mNumAppsPerRow = i;
        this.mNumPredictedAppsPerRow = i2;
        this.mMergeAlgorithm = mergeAlgorithm;
        updateAdapterItems();
    }

    public boolean setOrderedFilter(ArrayList<ComponentKey> arrayList) {
        if (this.mSearchResults != arrayList) {
            boolean equals = this.mSearchResults != null ? this.mSearchResults.equals(arrayList) : false;
            this.mSearchResults = arrayList;
            updateAdapterItems();
            return !equals;
        }
        return false;
    }

    public void setPredictedApps(List<ComponentKey> list) {
        this.mPredictedAppComponents.clear();
        this.mPredictedAppComponents.addAll(list);
        onAppsUpdated();
    }

    public void updateApps(List<AppInfo> list) {
        if (list == null) {
            return;
        }
        for (AppInfo appInfo : list) {
            Log.e("AlphabeticalAppsList", "app: " + appInfo.componentName.getPackageName());
            if (appInfo.componentName.getPackageName().startsWith("com.android.settings") || appInfo.componentName.getPackageName().startsWith("com.android.cts.verifier") || appInfo.componentName.getPackageName().startsWith("jp.co.benesse.dcha.gp.calibration")) {
                this.mComponentToAppMap.put(appInfo.toComponentKey(), appInfo);
            }
        }
        onAppsUpdated();
    }
}
