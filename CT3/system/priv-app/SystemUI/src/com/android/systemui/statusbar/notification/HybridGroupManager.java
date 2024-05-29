package com.android.systemui.statusbar.notification;

import android.app.Notification;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
/* loaded from: a.zip:com/android/systemui/statusbar/notification/HybridGroupManager.class */
public class HybridGroupManager {
    private final Context mContext;
    private int mOverflowNumberColor;
    private ViewGroup mParent;

    public HybridGroupManager(Context context, ViewGroup viewGroup) {
        this.mContext = context;
        this.mParent = viewGroup;
    }

    private HybridNotificationView inflateHybridView() {
        HybridNotificationView hybridNotificationView = (HybridNotificationView) ((LayoutInflater) this.mContext.getSystemService(LayoutInflater.class)).inflate(2130968622, this.mParent, false);
        this.mParent.addView(hybridNotificationView);
        return hybridNotificationView;
    }

    private TextView inflateOverflowNumber() {
        TextView textView = (TextView) ((LayoutInflater) this.mContext.getSystemService(LayoutInflater.class)).inflate(2130968623, this.mParent, false);
        this.mParent.addView(textView);
        updateOverFlowNumberColor(textView);
        return textView;
    }

    private CharSequence resolveText(Notification notification) {
        CharSequence charSequence = notification.extras.getCharSequence("android.text");
        CharSequence charSequence2 = charSequence;
        if (charSequence == null) {
            charSequence2 = notification.extras.getCharSequence("android.bigText");
        }
        return charSequence2;
    }

    private CharSequence resolveTitle(Notification notification) {
        CharSequence charSequence = notification.extras.getCharSequence("android.title");
        CharSequence charSequence2 = charSequence;
        if (charSequence == null) {
            charSequence2 = notification.extras.getCharSequence("android.title.big");
        }
        return charSequence2;
    }

    private void updateOverFlowNumberColor(TextView textView) {
        textView.setTextColor(this.mOverflowNumberColor);
    }

    public HybridNotificationView bindFromNotification(HybridNotificationView hybridNotificationView, Notification notification) {
        HybridNotificationView hybridNotificationView2 = hybridNotificationView;
        if (hybridNotificationView == null) {
            hybridNotificationView2 = inflateHybridView();
        }
        hybridNotificationView2.bind(resolveTitle(notification), resolveText(notification));
        return hybridNotificationView2;
    }

    public TextView bindOverflowNumber(TextView textView, int i) {
        TextView textView2 = textView;
        if (textView == null) {
            textView2 = inflateOverflowNumber();
        }
        String string = this.mContext.getResources().getString(2131493510, Integer.valueOf(i));
        if (!string.equals(textView2.getText())) {
            textView2.setText(string);
        }
        textView2.setContentDescription(String.format(this.mContext.getResources().getQuantityString(2132017153, i), Integer.valueOf(i)));
        return textView2;
    }

    public void setOverflowNumberColor(TextView textView, int i) {
        this.mOverflowNumberColor = i;
        if (textView != null) {
            updateOverFlowNumberColor(textView);
        }
    }
}
