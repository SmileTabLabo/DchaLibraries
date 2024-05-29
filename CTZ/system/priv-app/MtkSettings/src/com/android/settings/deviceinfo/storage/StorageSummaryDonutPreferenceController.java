package com.android.settings.deviceinfo.storage;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.text.format.Formatter;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
/* loaded from: classes.dex */
public class StorageSummaryDonutPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private StorageSummaryDonutPreference mSummary;
    private long mTotalBytes;
    private long mUsedBytes;

    public StorageSummaryDonutPreferenceController(Context context) {
        super(context);
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public void displayPreference(PreferenceScreen preferenceScreen) {
        this.mSummary = (StorageSummaryDonutPreference) preferenceScreen.findPreference("pref_summary");
        this.mSummary.setEnabled(true);
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public void updateState(Preference preference) {
        super.updateState(preference);
        StorageSummaryDonutPreference storageSummaryDonutPreference = (StorageSummaryDonutPreference) preference;
        Formatter.BytesResult formatBytes = Formatter.formatBytes(this.mContext.getResources(), this.mUsedBytes, 0);
        storageSummaryDonutPreference.setTitle(TextUtils.expandTemplate(this.mContext.getText(R.string.storage_size_large_alternate), formatBytes.value, formatBytes.units));
        storageSummaryDonutPreference.setSummary(this.mContext.getString(R.string.storage_volume_total, Formatter.formatShortFileSize(this.mContext, this.mTotalBytes)));
        storageSummaryDonutPreference.setPercent(this.mUsedBytes, this.mTotalBytes);
        storageSummaryDonutPreference.setEnabled(true);
    }

    public void invalidateData() {
        if (this.mSummary != null) {
            updateState(this.mSummary);
        }
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public boolean isAvailable() {
        return true;
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public String getPreferenceKey() {
        return "pref_summary";
    }

    public void updateBytes(long j, long j2) {
        this.mUsedBytes = j;
        this.mTotalBytes = j2;
        invalidateData();
    }
}
