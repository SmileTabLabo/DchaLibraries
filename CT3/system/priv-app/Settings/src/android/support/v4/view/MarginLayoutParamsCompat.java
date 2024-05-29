package android.support.v4.view;

import android.os.Build;
import android.view.ViewGroup;
/* loaded from: classes.dex */
public final class MarginLayoutParamsCompat {
    static final MarginLayoutParamsCompatImpl IMPL;

    /* loaded from: classes.dex */
    interface MarginLayoutParamsCompatImpl {
        int getMarginEnd(ViewGroup.MarginLayoutParams marginLayoutParams);

        int getMarginStart(ViewGroup.MarginLayoutParams marginLayoutParams);
    }

    /* loaded from: classes.dex */
    static class MarginLayoutParamsCompatImplBase implements MarginLayoutParamsCompatImpl {
        MarginLayoutParamsCompatImplBase() {
        }

        @Override // android.support.v4.view.MarginLayoutParamsCompat.MarginLayoutParamsCompatImpl
        public int getMarginStart(ViewGroup.MarginLayoutParams lp) {
            return lp.leftMargin;
        }

        @Override // android.support.v4.view.MarginLayoutParamsCompat.MarginLayoutParamsCompatImpl
        public int getMarginEnd(ViewGroup.MarginLayoutParams lp) {
            return lp.rightMargin;
        }
    }

    /* loaded from: classes.dex */
    static class MarginLayoutParamsCompatImplJbMr1 implements MarginLayoutParamsCompatImpl {
        MarginLayoutParamsCompatImplJbMr1() {
        }

        @Override // android.support.v4.view.MarginLayoutParamsCompat.MarginLayoutParamsCompatImpl
        public int getMarginStart(ViewGroup.MarginLayoutParams lp) {
            return MarginLayoutParamsCompatJellybeanMr1.getMarginStart(lp);
        }

        @Override // android.support.v4.view.MarginLayoutParamsCompat.MarginLayoutParamsCompatImpl
        public int getMarginEnd(ViewGroup.MarginLayoutParams lp) {
            return MarginLayoutParamsCompatJellybeanMr1.getMarginEnd(lp);
        }
    }

    static {
        int version = Build.VERSION.SDK_INT;
        if (version >= 17) {
            IMPL = new MarginLayoutParamsCompatImplJbMr1();
        } else {
            IMPL = new MarginLayoutParamsCompatImplBase();
        }
    }

    public static int getMarginStart(ViewGroup.MarginLayoutParams lp) {
        return IMPL.getMarginStart(lp);
    }

    public static int getMarginEnd(ViewGroup.MarginLayoutParams lp) {
        return IMPL.getMarginEnd(lp);
    }

    private MarginLayoutParamsCompat() {
    }
}
