package com.android.settings.applications;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.applications.ProcStatsData;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
/* loaded from: classes.dex */
public class ProcessStatsUi extends ProcessStatsBase {
    private PreferenceGroup mAppListGroup;
    private MenuItem mMenuAvg;
    private MenuItem mMenuMax;
    private PackageManager mPm;
    private boolean mShowMax;
    public static final int[] BACKGROUND_AND_SYSTEM_PROC_STATES = {0, 2, 3, 4, 5, 6, 7, 8, 9};
    public static final int[] FOREGROUND_PROC_STATES = {1};
    public static final int[] CACHED_PROC_STATES = {11, 12, 13};
    static final Comparator<ProcStatsPackageEntry> sPackageEntryCompare = new Comparator<ProcStatsPackageEntry>() { // from class: com.android.settings.applications.ProcessStatsUi.1
        @Override // java.util.Comparator
        public int compare(ProcStatsPackageEntry lhs, ProcStatsPackageEntry rhs) {
            double rhsWeight = Math.max(rhs.mRunWeight, rhs.mBgWeight);
            double lhsWeight = Math.max(lhs.mRunWeight, lhs.mBgWeight);
            if (lhsWeight == rhsWeight) {
                return 0;
            }
            return lhsWeight < rhsWeight ? 1 : -1;
        }
    };
    static final Comparator<ProcStatsPackageEntry> sMaxPackageEntryCompare = new Comparator<ProcStatsPackageEntry>() { // from class: com.android.settings.applications.ProcessStatsUi.2
        @Override // java.util.Comparator
        public int compare(ProcStatsPackageEntry lhs, ProcStatsPackageEntry rhs) {
            double rhsMax = Math.max(rhs.mMaxBgMem, rhs.mMaxRunMem);
            double lhsMax = Math.max(lhs.mMaxBgMem, lhs.mMaxRunMem);
            if (lhsMax == rhsMax) {
                return 0;
            }
            return lhsMax < rhsMax ? 1 : -1;
        }
    };

    @Override // com.android.settings.applications.ProcessStatsBase, com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mPm = getActivity().getPackageManager();
        addPreferencesFromResource(R.xml.process_stats_ui);
        this.mAppListGroup = (PreferenceGroup) findPreference("app_list");
        setHasOptionsMenu(true);
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.app.Fragment
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        this.mMenuAvg = menu.add(0, 1, 0, R.string.sort_avg_use);
        this.mMenuMax = menu.add(0, 2, 0, R.string.sort_max_use);
        updateMenu();
    }

    @Override // android.app.Fragment
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
            case 2:
                this.mShowMax = !this.mShowMax;
                refreshUi();
                updateMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateMenu() {
        this.mMenuMax.setVisible(!this.mShowMax);
        this.mMenuAvg.setVisible(this.mShowMax);
    }

    @Override // com.android.settings.InstrumentedPreferenceFragment
    protected int getMetricsCategory() {
        return 23;
    }

    @Override // com.android.settings.applications.ProcessStatsBase, com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.support.v7.preference.PreferenceManager.OnPreferenceTreeClickListener
    public boolean onPreferenceTreeClick(Preference preference) {
        if (!(preference instanceof ProcessStatsPreference)) {
            return false;
        }
        ProcessStatsPreference pgp = (ProcessStatsPreference) preference;
        ProcStatsData.MemInfo memInfo = this.mStatsManager.getMemInfo();
        launchMemoryDetail((SettingsActivity) getActivity(), memInfo, pgp.getEntry(), true);
        return super.onPreferenceTreeClick(preference);
    }

    @Override // com.android.settings.applications.ProcessStatsBase
    public void refreshUi() {
        this.mAppListGroup.removeAll();
        this.mAppListGroup.setOrderingAsAdded(false);
        this.mAppListGroup.setTitle(this.mShowMax ? R.string.maximum_memory_use : R.string.average_memory_use);
        Context context = getActivity();
        ProcStatsData.MemInfo memInfo = this.mStatsManager.getMemInfo();
        List<ProcStatsPackageEntry> pkgEntries = this.mStatsManager.getEntries();
        int N = pkgEntries.size();
        for (int i = 0; i < N; i++) {
            pkgEntries.get(i).updateMetrics();
        }
        Collections.sort(pkgEntries, this.mShowMax ? sMaxPackageEntryCompare : sPackageEntryCompare);
        double maxMemory = this.mShowMax ? memInfo.realTotalRam : memInfo.usedWeight * memInfo.weightToRam;
        for (int i2 = 0; i2 < pkgEntries.size(); i2++) {
            ProcStatsPackageEntry pkg = pkgEntries.get(i2);
            ProcessStatsPreference pref = new ProcessStatsPreference(getPrefContext());
            pkg.retrieveUiData(context, this.mPm);
            pref.init(pkg, this.mPm, maxMemory, memInfo.weightToRam, memInfo.totalScale, !this.mShowMax);
            pref.setOrder(i2);
            this.mAppListGroup.addPreference(pref);
        }
    }
}
