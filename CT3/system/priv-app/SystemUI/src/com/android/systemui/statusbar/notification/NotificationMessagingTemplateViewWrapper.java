package com.android.systemui.statusbar.notification;

import android.content.Context;
import android.service.notification.StatusBarNotification;
import android.view.View;
import com.android.internal.widget.MessagingLinearLayout;
import com.android.systemui.statusbar.ExpandableNotificationRow;
/* loaded from: a.zip:com/android/systemui/statusbar/notification/NotificationMessagingTemplateViewWrapper.class */
public class NotificationMessagingTemplateViewWrapper extends NotificationTemplateViewWrapper {
    private View mContractedMessage;

    /* JADX INFO: Access modifiers changed from: protected */
    public NotificationMessagingTemplateViewWrapper(Context context, View view, ExpandableNotificationRow expandableNotificationRow) {
        super(context, view, expandableNotificationRow);
    }

    private void resolveViews() {
        this.mContractedMessage = null;
        MessagingLinearLayout findViewById = this.mView.findViewById(16909246);
        if (!(findViewById instanceof MessagingLinearLayout) || findViewById.getChildCount() <= 0) {
            return;
        }
        MessagingLinearLayout messagingLinearLayout = findViewById;
        View childAt = messagingLinearLayout.getChildAt(0);
        if (childAt.getId() == messagingLinearLayout.getContractedChildId()) {
            this.mContractedMessage = childAt;
        }
    }

    @Override // com.android.systemui.statusbar.notification.NotificationTemplateViewWrapper, com.android.systemui.statusbar.notification.NotificationHeaderViewWrapper, com.android.systemui.statusbar.notification.NotificationViewWrapper
    public void notifyContentUpdated(StatusBarNotification statusBarNotification) {
        resolveViews();
        super.notifyContentUpdated(statusBarNotification);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.NotificationTemplateViewWrapper, com.android.systemui.statusbar.notification.NotificationHeaderViewWrapper
    public void updateTransformedTypes() {
        super.updateTransformedTypes();
        if (this.mContractedMessage != null) {
            this.mTransformationHelper.addTransformedView(2, this.mContractedMessage);
        }
    }
}
