package com.android.settings.datausage;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.NetworkPolicyManager;
import android.support.v7.preference.PreferenceViewHolder;
import android.view.View;
import android.widget.Checkable;
import com.android.settings.CustomDialogPreference;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.dashboard.conditional.BackgroundDataCondition;
import com.android.settings.dashboard.conditional.ConditionManager;
/* loaded from: classes.dex */
public class RestrictBackgroundDataPreference extends CustomDialogPreference {
    private boolean mChecked;
    private NetworkPolicyManager mPolicyManager;

    @Override // android.support.v7.preference.Preference
    public void onAttached() {
        super.onAttached();
        this.mPolicyManager = NetworkPolicyManager.from(getContext());
        setChecked(this.mPolicyManager.getRestrictBackground());
    }

    public void setRestrictBackground(boolean restrictBackground) {
        this.mPolicyManager.setRestrictBackground(restrictBackground);
        setChecked(restrictBackground);
        ((BackgroundDataCondition) ConditionManager.get(getContext()).getCondition(BackgroundDataCondition.class)).refreshState();
    }

    private void setChecked(boolean checked) {
        if (this.mChecked == checked) {
            return;
        }
        this.mChecked = checked;
        notifyChanged();
    }

    @Override // android.support.v7.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        View switchView = holder.findViewById(16908352);
        switchView.setClickable(false);
        ((Checkable) switchView).setChecked(this.mChecked);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.preference.Preference
    public void performClick(View view) {
        if (this.mChecked) {
            setRestrictBackground(false);
        } else {
            super.performClick(view);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.CustomDialogPreference
    public void onPrepareDialogBuilder(AlertDialog.Builder builder, DialogInterface.OnClickListener listener) {
        super.onPrepareDialogBuilder(builder, listener);
        builder.setTitle(R.string.data_usage_restrict_background_title);
        if (Utils.hasMultipleUsers(getContext())) {
            builder.setMessage(R.string.data_usage_restrict_background_multiuser);
        } else {
            builder.setMessage(R.string.data_usage_restrict_background);
        }
        builder.setPositiveButton(17039370, listener);
        builder.setNegativeButton(17039360, (DialogInterface.OnClickListener) null);
    }

    @Override // com.android.settings.CustomDialogPreference
    protected void onClick(DialogInterface dialog, int which) {
        if (which != -1) {
            return;
        }
        setRestrictBackground(true);
    }
}
