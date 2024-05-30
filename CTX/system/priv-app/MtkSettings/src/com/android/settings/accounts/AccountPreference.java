package com.android.settings.accounts;

import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.Log;
import android.widget.ImageView;
import com.android.settings.R;
/* loaded from: classes.dex */
public class AccountPreference extends Preference {
    private boolean mShowTypeIcon;
    private int mStatus;
    private ImageView mSyncStatusIcon;

    @Override // android.support.v7.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
        super.onBindViewHolder(preferenceViewHolder);
        if (!this.mShowTypeIcon) {
            this.mSyncStatusIcon = (ImageView) preferenceViewHolder.findViewById(16908294);
            this.mSyncStatusIcon.setImageResource(getSyncStatusIcon(this.mStatus));
            this.mSyncStatusIcon.setContentDescription(getSyncContentDescription(this.mStatus));
        }
    }

    private int getSyncStatusIcon(int i) {
        switch (i) {
            case 0:
            case 3:
                return R.drawable.ic_settings_sync;
            case 1:
                return R.drawable.ic_sync_grey_holo;
            case 2:
                return R.drawable.ic_sync_red_holo;
            default:
                Log.e("AccountPreference", "Unknown sync status: " + i);
                return R.drawable.ic_sync_red_holo;
        }
    }

    private String getSyncContentDescription(int i) {
        switch (i) {
            case 0:
                return getContext().getString(R.string.accessibility_sync_enabled);
            case 1:
                return getContext().getString(R.string.accessibility_sync_disabled);
            case 2:
                return getContext().getString(R.string.accessibility_sync_error);
            case 3:
                return getContext().getString(R.string.accessibility_sync_in_progress);
            default:
                Log.e("AccountPreference", "Unknown sync status: " + i);
                return getContext().getString(R.string.accessibility_sync_error);
        }
    }
}
