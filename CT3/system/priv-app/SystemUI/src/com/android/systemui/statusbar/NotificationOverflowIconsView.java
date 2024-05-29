package com.android.systemui.statusbar;

import android.app.Notification;
import android.content.Context;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.util.NotificationColorUtil;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.phone.IconMerger;
/* loaded from: a.zip:com/android/systemui/statusbar/NotificationOverflowIconsView.class */
public class NotificationOverflowIconsView extends IconMerger {
    private int mIconSize;
    private TextView mMoreText;
    private NotificationColorUtil mNotificationColorUtil;
    private int mTintColor;

    public NotificationOverflowIconsView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    private void applyColor(Notification notification, StatusBarIconView statusBarIconView) {
        statusBarIconView.setColorFilter(this.mTintColor, PorterDuff.Mode.MULTIPLY);
    }

    private void updateMoreText() {
        this.mMoreText.setText(getResources().getString(2131493605, Integer.valueOf(getChildCount())));
    }

    public void addNotification(NotificationData.Entry entry) {
        StatusBarIconView statusBarIconView = new StatusBarIconView(getContext(), "", entry.notification.getNotification());
        statusBarIconView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        addView(statusBarIconView, this.mIconSize, this.mIconSize);
        statusBarIconView.set(entry.icon.getStatusBarIcon());
        applyColor(entry.notification.getNotification(), statusBarIconView);
        updateMoreText();
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mNotificationColorUtil = NotificationColorUtil.getInstance(getContext());
        this.mTintColor = getContext().getColor(2131558536);
        this.mIconSize = getResources().getDimensionPixelSize(17104926);
    }

    public void setMoreText(TextView textView) {
        this.mMoreText = textView;
    }
}
