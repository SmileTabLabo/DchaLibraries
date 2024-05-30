package com.android.systemui.shared.system;

import android.content.res.Resources;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
/* loaded from: classes.dex */
public class NavigationBarCompat {
    public static final int FLAG_DISABLE_QUICK_SCRUB = 2;
    public static final int FLAG_DISABLE_SWIPE_UP = 1;
    public static final int FLAG_SHOW_OVERVIEW_BUTTON = 4;
    public static final int HIT_TARGET_BACK = 1;
    public static final int HIT_TARGET_HOME = 2;
    public static final int HIT_TARGET_NONE = 0;
    public static final int HIT_TARGET_OVERVIEW = 3;
    public static final int HIT_TARGET_ROTATION = 4;

    @Retention(RetentionPolicy.SOURCE)
    /* loaded from: classes.dex */
    public @interface HitTarget {
    }

    @Retention(RetentionPolicy.SOURCE)
    /* loaded from: classes.dex */
    public @interface InteractionType {
    }

    public static int getQuickStepDragSlopPx() {
        return convertDpToPixel(10.0f);
    }

    public static int getQuickScrubDragSlopPx() {
        return convertDpToPixel(20.0f);
    }

    public static int getQuickStepTouchSlopPx() {
        return convertDpToPixel(24.0f);
    }

    public static int getQuickScrubTouchSlopPx() {
        return convertDpToPixel(35.0f);
    }

    private static int convertDpToPixel(float dp) {
        return (int) (Resources.getSystem().getDisplayMetrics().density * dp);
    }
}
