package com.android.systemui.statusbar.notification;

import android.content.Context;
import android.graphics.drawable.Icon;
import android.service.notification.StatusBarNotification;
import android.view.View;
import com.android.systemui.statusbar.ExpandableNotificationRow;
/* loaded from: a.zip:com/android/systemui/statusbar/notification/NotificationBigPictureTemplateViewWrapper.class */
public class NotificationBigPictureTemplateViewWrapper extends NotificationTemplateViewWrapper {
    /* JADX INFO: Access modifiers changed from: protected */
    public NotificationBigPictureTemplateViewWrapper(Context context, View view, ExpandableNotificationRow expandableNotificationRow) {
        super(context, view, expandableNotificationRow);
    }

    private void updateImageTag(StatusBarNotification statusBarNotification) {
        Icon icon = (Icon) statusBarNotification.getNotification().extras.getParcelable("android.largeIcon.big");
        if (icon != null) {
            this.mPicture.setTag(2131886146, icon);
        }
    }

    @Override // com.android.systemui.statusbar.notification.NotificationTemplateViewWrapper, com.android.systemui.statusbar.notification.NotificationHeaderViewWrapper, com.android.systemui.statusbar.notification.NotificationViewWrapper
    public void notifyContentUpdated(StatusBarNotification statusBarNotification) {
        super.notifyContentUpdated(statusBarNotification);
        updateImageTag(statusBarNotification);
    }
}
