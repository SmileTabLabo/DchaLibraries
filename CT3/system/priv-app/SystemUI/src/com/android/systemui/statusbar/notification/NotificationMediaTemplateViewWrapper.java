package com.android.systemui.statusbar.notification;

import android.content.Context;
import android.service.notification.StatusBarNotification;
import android.view.View;
import com.android.systemui.statusbar.ExpandableNotificationRow;
/* loaded from: a.zip:com/android/systemui/statusbar/notification/NotificationMediaTemplateViewWrapper.class */
public class NotificationMediaTemplateViewWrapper extends NotificationTemplateViewWrapper {
    View mActions;

    /* JADX INFO: Access modifiers changed from: protected */
    public NotificationMediaTemplateViewWrapper(Context context, View view, ExpandableNotificationRow expandableNotificationRow) {
        super(context, view, expandableNotificationRow);
    }

    private void resolveViews(StatusBarNotification statusBarNotification) {
        this.mActions = this.mView.findViewById(16909235);
    }

    @Override // com.android.systemui.statusbar.notification.NotificationTemplateViewWrapper, com.android.systemui.statusbar.notification.NotificationHeaderViewWrapper, com.android.systemui.statusbar.notification.NotificationViewWrapper
    public void notifyContentUpdated(StatusBarNotification statusBarNotification) {
        resolveViews(statusBarNotification);
        super.notifyContentUpdated(statusBarNotification);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.NotificationTemplateViewWrapper, com.android.systemui.statusbar.notification.NotificationHeaderViewWrapper
    public void updateTransformedTypes() {
        super.updateTransformedTypes();
        if (this.mActions != null) {
            this.mTransformationHelper.addTransformedView(5, this.mActions);
        }
    }
}
