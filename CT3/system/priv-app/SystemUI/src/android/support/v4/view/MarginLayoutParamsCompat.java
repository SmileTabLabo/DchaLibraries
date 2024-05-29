package android.support.v4.view;

import android.os.Build;
import android.view.ViewGroup;
/* loaded from: a.zip:android/support/v4/view/MarginLayoutParamsCompat.class */
public final class MarginLayoutParamsCompat {
    static final MarginLayoutParamsCompatImpl IMPL;

    /* loaded from: a.zip:android/support/v4/view/MarginLayoutParamsCompat$MarginLayoutParamsCompatImpl.class */
    interface MarginLayoutParamsCompatImpl {
        int getMarginEnd(ViewGroup.MarginLayoutParams marginLayoutParams);

        int getMarginStart(ViewGroup.MarginLayoutParams marginLayoutParams);
    }

    /* loaded from: a.zip:android/support/v4/view/MarginLayoutParamsCompat$MarginLayoutParamsCompatImplBase.class */
    static class MarginLayoutParamsCompatImplBase implements MarginLayoutParamsCompatImpl {
        MarginLayoutParamsCompatImplBase() {
        }

        @Override // android.support.v4.view.MarginLayoutParamsCompat.MarginLayoutParamsCompatImpl
        public int getMarginEnd(ViewGroup.MarginLayoutParams marginLayoutParams) {
            return marginLayoutParams.rightMargin;
        }

        @Override // android.support.v4.view.MarginLayoutParamsCompat.MarginLayoutParamsCompatImpl
        public int getMarginStart(ViewGroup.MarginLayoutParams marginLayoutParams) {
            return marginLayoutParams.leftMargin;
        }
    }

    /* loaded from: a.zip:android/support/v4/view/MarginLayoutParamsCompat$MarginLayoutParamsCompatImplJbMr1.class */
    static class MarginLayoutParamsCompatImplJbMr1 implements MarginLayoutParamsCompatImpl {
        MarginLayoutParamsCompatImplJbMr1() {
        }

        @Override // android.support.v4.view.MarginLayoutParamsCompat.MarginLayoutParamsCompatImpl
        public int getMarginEnd(ViewGroup.MarginLayoutParams marginLayoutParams) {
            return MarginLayoutParamsCompatJellybeanMr1.getMarginEnd(marginLayoutParams);
        }

        @Override // android.support.v4.view.MarginLayoutParamsCompat.MarginLayoutParamsCompatImpl
        public int getMarginStart(ViewGroup.MarginLayoutParams marginLayoutParams) {
            return MarginLayoutParamsCompatJellybeanMr1.getMarginStart(marginLayoutParams);
        }
    }

    static {
        if (Build.VERSION.SDK_INT >= 17) {
            IMPL = new MarginLayoutParamsCompatImplJbMr1();
        } else {
            IMPL = new MarginLayoutParamsCompatImplBase();
        }
    }

    private MarginLayoutParamsCompat() {
    }

    public static int getMarginEnd(ViewGroup.MarginLayoutParams marginLayoutParams) {
        return IMPL.getMarginEnd(marginLayoutParams);
    }

    public static int getMarginStart(ViewGroup.MarginLayoutParams marginLayoutParams) {
        return IMPL.getMarginStart(marginLayoutParams);
    }
}
