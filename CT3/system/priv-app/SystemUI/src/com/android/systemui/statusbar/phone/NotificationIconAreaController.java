package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.internal.util.NotificationColorUtil;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.StatusBarIconView;
import com.android.systemui.statusbar.notification.NotificationUtils;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/NotificationIconAreaController.class */
public class NotificationIconAreaController {
    private int mIconHPadding;
    private int mIconSize;
    private ImageView mMoreIcon;
    private final NotificationColorUtil mNotificationColorUtil;
    protected View mNotificationIconArea;
    private IconMerger mNotificationIcons;
    private PhoneStatusBar mPhoneStatusBar;
    private int mIconTint = -1;
    private final Rect mTintArea = new Rect();

    public NotificationIconAreaController(Context context, PhoneStatusBar phoneStatusBar) {
        this.mPhoneStatusBar = phoneStatusBar;
        this.mNotificationColorUtil = NotificationColorUtil.getInstance(context);
        initializeNotificationAreaViews(context);
    }

    private void applyNotificationIconsTint() {
        for (int i = 0; i < this.mNotificationIcons.getChildCount(); i++) {
            StatusBarIconView statusBarIconView = (StatusBarIconView) this.mNotificationIcons.getChildAt(i);
            if (Boolean.TRUE.equals(statusBarIconView.getTag(2131886141)) ? NotificationUtils.isGrayscale(statusBarIconView, this.mNotificationColorUtil) : true) {
                statusBarIconView.setImageTintList(ColorStateList.valueOf(StatusBarIconController.getTint(this.mTintArea, statusBarIconView, this.mIconTint)));
            }
        }
    }

    @NonNull
    private LinearLayout.LayoutParams generateIconLayoutParams() {
        return new LinearLayout.LayoutParams(this.mIconSize + (this.mIconHPadding * 2), getHeight());
    }

    private void reloadDimens(Context context) {
        Resources resources = context.getResources();
        this.mIconSize = resources.getDimensionPixelSize(17104926);
        this.mIconHPadding = resources.getDimensionPixelSize(2131689796);
    }

    protected int getHeight() {
        return this.mPhoneStatusBar.getStatusBarHeight();
    }

    public View getNotificationInnerAreaView() {
        return this.mNotificationIconArea;
    }

    protected View inflateIconArea(LayoutInflater layoutInflater) {
        return layoutInflater.inflate(2130968726, (ViewGroup) null);
    }

    protected void initializeNotificationAreaViews(Context context) {
        reloadDimens(context);
        this.mNotificationIconArea = inflateIconArea(LayoutInflater.from(context));
        this.mNotificationIcons = (IconMerger) this.mNotificationIconArea.findViewById(2131886559);
        this.mMoreIcon = (ImageView) this.mNotificationIconArea.findViewById(2131886558);
        if (this.mMoreIcon != null) {
            this.mMoreIcon.setImageTintList(ColorStateList.valueOf(this.mIconTint));
            this.mNotificationIcons.setOverflowIndicator(this.mMoreIcon);
        }
    }

    public void onDensityOrFontScaleChanged(Context context) {
        reloadDimens(context);
        LinearLayout.LayoutParams generateIconLayoutParams = generateIconLayoutParams();
        for (int i = 0; i < this.mNotificationIcons.getChildCount(); i++) {
            this.mNotificationIcons.getChildAt(i).setLayoutParams(generateIconLayoutParams);
        }
    }

    public void setIconTint(int i) {
        this.mIconTint = i;
        if (this.mMoreIcon != null) {
            this.mMoreIcon.setImageTintList(ColorStateList.valueOf(this.mIconTint));
        }
        applyNotificationIconsTint();
    }

    public void setTintArea(Rect rect) {
        if (rect == null) {
            this.mTintArea.setEmpty();
        } else {
            this.mTintArea.set(rect);
        }
        applyNotificationIconsTint();
    }

    protected boolean shouldShowNotification(NotificationData.Entry entry, NotificationData notificationData) {
        return (!notificationData.isAmbient(entry.key) || NotificationData.showNotificationEvenIfUnprovisioned(entry.notification)) && PhoneStatusBar.isTopLevelChild(entry) && entry.row.getVisibility() != 8;
    }

    public void updateNotificationIcons(NotificationData notificationData) {
        LinearLayout.LayoutParams generateIconLayoutParams = generateIconLayoutParams();
        ArrayList<NotificationData.Entry> activeNotifications = notificationData.getActiveNotifications();
        int size = activeNotifications.size();
        ArrayList arrayList = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            NotificationData.Entry entry = activeNotifications.get(i);
            if (shouldShowNotification(entry, notificationData)) {
                arrayList.add(entry.icon);
            }
        }
        ArrayList arrayList2 = new ArrayList();
        for (int i2 = 0; i2 < this.mNotificationIcons.getChildCount(); i2++) {
            View childAt = this.mNotificationIcons.getChildAt(i2);
            if (!arrayList.contains(childAt)) {
                arrayList2.add(childAt);
            }
        }
        int size2 = arrayList2.size();
        for (int i3 = 0; i3 < size2; i3++) {
            this.mNotificationIcons.removeView((View) arrayList2.get(i3));
        }
        for (int i4 = 0; i4 < arrayList.size(); i4++) {
            View view = (View) arrayList.get(i4);
            if (view.getParent() == null) {
                this.mNotificationIcons.addView(view, i4, generateIconLayoutParams);
            }
        }
        int childCount = this.mNotificationIcons.getChildCount();
        for (int i5 = 0; i5 < childCount; i5++) {
            View childAt2 = this.mNotificationIcons.getChildAt(i5);
            StatusBarIconView statusBarIconView = (StatusBarIconView) arrayList.get(i5);
            if (childAt2 != statusBarIconView) {
                this.mNotificationIcons.removeView(statusBarIconView);
                this.mNotificationIcons.addView(statusBarIconView, i5);
            }
        }
        applyNotificationIconsTint();
    }
}
