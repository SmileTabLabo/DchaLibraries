package com.android.settings.dashboard.conditional;

import android.app.NotificationManager;
import android.graphics.drawable.Drawable;
import com.android.settings.R;
/* loaded from: classes.dex */
public class RingerMutedCondition extends AbnormalRingerConditionBase {
    private final NotificationManager mNotificationManager;

    /* JADX INFO: Access modifiers changed from: package-private */
    public RingerMutedCondition(ConditionManager conditionManager) {
        super(conditionManager);
        this.mNotificationManager = (NotificationManager) this.mManager.getContext().getSystemService("notification");
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    public void refreshState() {
        int i;
        boolean z = false;
        if (this.mNotificationManager != null) {
            i = this.mNotificationManager.getZenMode();
        } else {
            i = 0;
        }
        boolean z2 = i != 0;
        if ((this.mAudioManager.getRingerModeInternal() == 0) && !z2) {
            z = true;
        }
        setActive(z);
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    public int getMetricsConstant() {
        return 1368;
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    public Drawable getIcon() {
        return this.mManager.getContext().getDrawable(R.drawable.ic_notifications_off_24dp);
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    public CharSequence getTitle() {
        return this.mManager.getContext().getText(R.string.condition_device_muted_title);
    }

    @Override // com.android.settings.dashboard.conditional.Condition
    public CharSequence getSummary() {
        return this.mManager.getContext().getText(R.string.condition_device_muted_summary);
    }
}
