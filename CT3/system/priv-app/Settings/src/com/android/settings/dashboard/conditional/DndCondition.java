package com.android.settings.dashboard.conditional;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.PersistableBundle;
import android.service.notification.ZenModeConfig;
import com.android.settings.R;
/* loaded from: classes.dex */
public class DndCondition extends Condition {
    private ZenModeConfig mConfig;
    private int mZen;

    public DndCondition(ConditionManager manager) {
        super(manager);
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    public void refreshState() {
        NotificationManager notificationManager = (NotificationManager) this.mManager.getContext().getSystemService(NotificationManager.class);
        this.mZen = notificationManager.getZenMode();
        boolean zenModeEnabled = this.mZen != 0;
        if (zenModeEnabled) {
            this.mConfig = notificationManager.getZenModeConfig();
        } else {
            this.mConfig = null;
        }
        setActive(zenModeEnabled);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.android.settings.dashboard.conditional.Condition
    public boolean saveState(PersistableBundle bundle) {
        bundle.putInt("state", this.mZen);
        return super.saveState(bundle);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.android.settings.dashboard.conditional.Condition
    public void restoreState(PersistableBundle bundle) {
        super.restoreState(bundle);
        this.mZen = bundle.getInt("state", 0);
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    protected Class<?> getReceiverClass() {
        return Receiver.class;
    }

    private CharSequence getZenState() {
        switch (this.mZen) {
            case 1:
                return this.mManager.getContext().getString(R.string.zen_mode_option_important_interruptions);
            case 2:
                return this.mManager.getContext().getString(R.string.zen_mode_option_no_interruptions);
            case 3:
                return this.mManager.getContext().getString(R.string.zen_mode_option_alarms);
            default:
                return null;
        }
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    public Icon getIcon() {
        return Icon.createWithResource(this.mManager.getContext(), (int) R.drawable.ic_zen);
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    public CharSequence getTitle() {
        return this.mManager.getContext().getString(R.string.condition_zen_title, getZenState());
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    public CharSequence getSummary() {
        boolean isForever;
        if (this.mConfig == null || this.mConfig.manualRule == null) {
            isForever = false;
        } else {
            isForever = this.mConfig.manualRule.conditionId == null;
        }
        return isForever ? this.mManager.getContext().getString(17040820) : ZenModeConfig.getConditionSummary(this.mManager.getContext(), this.mConfig, ActivityManager.getCurrentUser(), false);
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    public CharSequence[] getActions() {
        return new CharSequence[]{this.mManager.getContext().getString(R.string.condition_turn_off)};
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    public void onPrimaryClick() {
        StatusBarManager statusBar = (StatusBarManager) this.mManager.getContext().getSystemService(StatusBarManager.class);
        statusBar.expandSettingsPanel("dnd");
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    public void onActionClick(int index) {
        if (index == 0) {
            NotificationManager notificationManager = (NotificationManager) this.mManager.getContext().getSystemService(NotificationManager.class);
            notificationManager.setZenMode(0, null, "DndCondition");
            setActive(false);
            return;
        }
        throw new IllegalArgumentException("Unexpected index " + index);
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    public int getMetricsConstant() {
        return 381;
    }

    /* loaded from: classes.dex */
    public static class Receiver extends BroadcastReceiver {
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (!"android.app.action.INTERRUPTION_FILTER_CHANGED_INTERNAL".equals(intent.getAction())) {
                return;
            }
            ((DndCondition) ConditionManager.get(context).getCondition(DndCondition.class)).refreshState();
        }
    }
}
