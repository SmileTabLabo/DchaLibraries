package com.android.systemui.car;

import android.content.Context;
import android.service.notification.StatusBarNotification;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.NotificationEntryManager;
/* loaded from: classes.dex */
public class CarNotificationEntryManager extends NotificationEntryManager {
    public CarNotificationEntryManager(Context context) {
        super(context);
    }

    @Override // com.android.systemui.statusbar.NotificationEntryManager
    public ExpandableNotificationRow.LongPressListener getNotificationLongClicker() {
        return null;
    }

    @Override // com.android.systemui.statusbar.NotificationEntryManager
    public boolean shouldPeek(NotificationData.Entry entry, StatusBarNotification statusBarNotification) {
        if (!this.mPresenter.isPresenterFullyCollapsed()) {
            return false;
        }
        return super.shouldPeek(entry, statusBarNotification);
    }
}
