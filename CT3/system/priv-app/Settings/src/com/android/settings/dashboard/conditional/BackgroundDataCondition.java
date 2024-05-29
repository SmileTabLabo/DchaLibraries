package com.android.settings.dashboard.conditional;

import android.content.Intent;
import android.graphics.drawable.Icon;
import android.net.NetworkPolicyManager;
import com.android.settings.R;
import com.android.settings.Settings;
/* loaded from: classes.dex */
public class BackgroundDataCondition extends Condition {
    public BackgroundDataCondition(ConditionManager manager) {
        super(manager);
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    public void refreshState() {
        setActive(NetworkPolicyManager.from(this.mManager.getContext()).getRestrictBackground());
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    public Icon getIcon() {
        return Icon.createWithResource(this.mManager.getContext(), (int) R.drawable.ic_data_saver);
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    public CharSequence getTitle() {
        return this.mManager.getContext().getString(R.string.condition_bg_data_title);
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    public CharSequence getSummary() {
        return this.mManager.getContext().getString(R.string.condition_bg_data_summary);
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    public CharSequence[] getActions() {
        return new CharSequence[]{this.mManager.getContext().getString(R.string.condition_turn_off)};
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    public void onPrimaryClick() {
        this.mManager.getContext().startActivity(new Intent(this.mManager.getContext(), Settings.DataUsageSummaryActivity.class));
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    public int getMetricsConstant() {
        return 378;
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    public void onActionClick(int index) {
        if (index == 0) {
            NetworkPolicyManager.from(this.mManager.getContext()).setRestrictBackground(false);
            setActive(false);
            return;
        }
        throw new IllegalArgumentException("Unexpected index " + index);
    }
}
