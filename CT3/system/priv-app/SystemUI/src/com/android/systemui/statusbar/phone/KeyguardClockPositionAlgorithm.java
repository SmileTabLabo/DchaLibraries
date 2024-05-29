package com.android.systemui.statusbar.phone;

import android.content.res.Resources;
import android.graphics.Path;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.PathInterpolator;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/KeyguardClockPositionAlgorithm.class */
public class KeyguardClockPositionAlgorithm {
    private static final PathInterpolator sSlowDownInterpolator;
    private AccelerateInterpolator mAccelerateInterpolator = new AccelerateInterpolator();
    private int mClockNotificationsMarginMax;
    private int mClockNotificationsMarginMin;
    private float mClockYFractionMax;
    private float mClockYFractionMin;
    private float mDensity;
    private float mEmptyDragAmount;
    private float mExpandedHeight;
    private int mHeight;
    private int mKeyguardStatusHeight;
    private int mMaxKeyguardNotifications;
    private int mMaxPanelHeight;
    private float mMoreCardNotificationAmount;
    private int mNotificationCount;

    /* loaded from: a.zip:com/android/systemui/statusbar/phone/KeyguardClockPositionAlgorithm$Result.class */
    public static class Result {
        public float clockAlpha;
        public float clockScale;
        public int clockY;
        public int stackScrollerPadding;
        public int stackScrollerPaddingAdjustment;
    }

    static {
        Path path = new Path();
        path.moveTo(0.0f, 0.0f);
        path.cubicTo(0.3f, 0.875f, 0.6f, 1.0f, 1.0f, 1.0f);
        sSlowDownInterpolator = new PathInterpolator(path);
    }

    private float getClockAlpha(float f) {
        float f2 = getNotificationAmountT() == 0.0f ? 0.5f : 0.75f;
        return Math.max(0.0f, Math.min(1.0f, (f - f2) / (0.95f - f2)));
    }

    private int getClockNotificationsPadding() {
        float min = Math.min(getNotificationAmountT(), 1.0f);
        return (int) ((this.mClockNotificationsMarginMin * min) + ((1.0f - min) * this.mClockNotificationsMarginMax));
    }

    private float getClockScale(int i, int i2, int i3) {
        float f = i2 - (this.mKeyguardStatusHeight * (getNotificationAmountT() == 0.0f ? 6.0f : 5.0f));
        return (float) (this.mAccelerateInterpolator.getInterpolation(Math.max(0.0f, Math.min((i - f) / (i3 - f), 1.0f))) * Math.pow(((this.mEmptyDragAmount / this.mDensity) / 300.0f) + 1.0f, 0.30000001192092896d));
    }

    private int getClockY() {
        return (int) (getClockYFraction() * this.mHeight);
    }

    private float getClockYExpansionAdjustment() {
        float clockYExpansionRubberbandFactor = getClockYExpansionRubberbandFactor() * (this.mMaxPanelHeight - this.mExpandedHeight);
        float f = (-sSlowDownInterpolator.getInterpolation(clockYExpansionRubberbandFactor / this.mMaxPanelHeight)) * 0.4f * this.mMaxPanelHeight;
        return this.mNotificationCount == 0 ? (((-2.0f) * clockYExpansionRubberbandFactor) + f) / 3.0f : f;
    }

    private float getClockYExpansionRubberbandFactor() {
        float pow = (float) Math.pow(Math.min(getNotificationAmountT(), 1.0f), 0.30000001192092896d);
        return ((1.0f - pow) * 0.8f) + (0.08f * pow);
    }

    private float getClockYFraction() {
        float min = Math.min(getNotificationAmountT(), 1.0f);
        return ((1.0f - min) * this.mClockYFractionMax) + (this.mClockYFractionMin * min);
    }

    private float getNotificationAmountT() {
        return this.mNotificationCount / (this.mMaxKeyguardNotifications + this.mMoreCardNotificationAmount);
    }

    private float getTopPaddingAdjMultiplier() {
        float min = Math.min(getNotificationAmountT(), 1.0f);
        return ((1.0f - min) * 1.4f) + (3.2f * min);
    }

    public float getMinStackScrollerPadding(int i, int i2) {
        return (this.mClockYFractionMin * i) + (i2 / 2) + this.mClockNotificationsMarginMin;
    }

    public void loadDimens(Resources resources) {
        this.mClockNotificationsMarginMin = resources.getDimensionPixelSize(2131689885);
        this.mClockNotificationsMarginMax = resources.getDimensionPixelSize(2131689886);
        this.mClockYFractionMin = resources.getFraction(2131820555, 1, 1);
        this.mClockYFractionMax = resources.getFraction(2131820554, 1, 1);
        this.mMoreCardNotificationAmount = resources.getDimensionPixelSize(2131689790) / resources.getDimensionPixelSize(2131689785);
        this.mDensity = resources.getDisplayMetrics().density;
    }

    public void run(Result result) {
        int clockY = getClockY() - (this.mKeyguardStatusHeight / 2);
        result.stackScrollerPaddingAdjustment = (int) (getClockYExpansionAdjustment() * getTopPaddingAdjMultiplier());
        int clockNotificationsPadding = getClockNotificationsPadding();
        int i = result.stackScrollerPaddingAdjustment;
        result.clockY = clockY;
        result.stackScrollerPadding = this.mKeyguardStatusHeight + clockY + clockNotificationsPadding + i;
        result.clockScale = getClockScale(result.stackScrollerPadding, result.clockY, getClockNotificationsPadding() + clockY + this.mKeyguardStatusHeight);
        result.clockAlpha = getClockAlpha(result.clockScale);
    }

    public void setup(int i, int i2, float f, int i3, int i4, int i5, float f2) {
        this.mMaxKeyguardNotifications = i;
        this.mMaxPanelHeight = i2;
        this.mExpandedHeight = f;
        this.mNotificationCount = i3;
        this.mHeight = i4;
        this.mKeyguardStatusHeight = i5;
        this.mEmptyDragAmount = f2;
    }
}
