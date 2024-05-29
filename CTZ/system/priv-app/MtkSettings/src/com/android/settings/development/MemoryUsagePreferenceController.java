package com.android.settings.development;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.format.Formatter;
import com.android.settings.R;
import com.android.settings.applications.ProcStatsData;
import com.android.settings.applications.ProcessStatsBase;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;
import com.android.settingslib.utils.ThreadUtils;
/* loaded from: classes.dex */
public class MemoryUsagePreferenceController extends DeveloperOptionsPreferenceController implements PreferenceControllerMixin {
    private ProcStatsData mProcStatsData;

    public MemoryUsagePreferenceController(Context context) {
        super(context);
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public String getPreferenceKey() {
        return "memory";
    }

    @Override // com.android.settingslib.development.DeveloperOptionsPreferenceController, com.android.settingslib.core.AbstractPreferenceController
    public void displayPreference(PreferenceScreen preferenceScreen) {
        super.displayPreference(preferenceScreen);
        this.mProcStatsData = getProcStatsData();
        setDuration();
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public void updateState(Preference preference) {
        ThreadUtils.postOnBackgroundThread(new Runnable() { // from class: com.android.settings.development.-$$Lambda$MemoryUsagePreferenceController$2UovDioLDVLRpJrL4IsFsRdoZts
            @Override // java.lang.Runnable
            public final void run() {
                MemoryUsagePreferenceController.lambda$updateState$1(MemoryUsagePreferenceController.this);
            }
        });
    }

    public static /* synthetic */ void lambda$updateState$1(final MemoryUsagePreferenceController memoryUsagePreferenceController) {
        memoryUsagePreferenceController.mProcStatsData.refreshStats(true);
        ProcStatsData.MemInfo memInfo = memoryUsagePreferenceController.mProcStatsData.getMemInfo();
        final String formatShortFileSize = Formatter.formatShortFileSize(memoryUsagePreferenceController.mContext, (long) memInfo.realUsedRam);
        final String formatShortFileSize2 = Formatter.formatShortFileSize(memoryUsagePreferenceController.mContext, (long) memInfo.realTotalRam);
        ThreadUtils.postOnMainThread(new Runnable() { // from class: com.android.settings.development.-$$Lambda$MemoryUsagePreferenceController$jVfwyLcntt7OQNk4ZzyeXShgglc
            @Override // java.lang.Runnable
            public final void run() {
                r0.mPreference.setSummary(MemoryUsagePreferenceController.this.mContext.getString(R.string.memory_summary, formatShortFileSize, formatShortFileSize2));
            }
        });
    }

    void setDuration() {
        this.mProcStatsData.setDuration(ProcessStatsBase.sDurations[0]);
    }

    ProcStatsData getProcStatsData() {
        return new ProcStatsData(this.mContext, false);
    }
}
