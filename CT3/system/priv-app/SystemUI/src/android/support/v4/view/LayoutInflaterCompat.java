package android.support.v4.view;

import android.os.Build;
import android.view.LayoutInflater;
/* loaded from: a.zip:android/support/v4/view/LayoutInflaterCompat.class */
public final class LayoutInflaterCompat {
    static final LayoutInflaterCompatImpl IMPL;

    /* loaded from: a.zip:android/support/v4/view/LayoutInflaterCompat$LayoutInflaterCompatImpl.class */
    interface LayoutInflaterCompatImpl {
        void setFactory(LayoutInflater layoutInflater, LayoutInflaterFactory layoutInflaterFactory);
    }

    /* loaded from: a.zip:android/support/v4/view/LayoutInflaterCompat$LayoutInflaterCompatImplBase.class */
    static class LayoutInflaterCompatImplBase implements LayoutInflaterCompatImpl {
        LayoutInflaterCompatImplBase() {
        }

        @Override // android.support.v4.view.LayoutInflaterCompat.LayoutInflaterCompatImpl
        public void setFactory(LayoutInflater layoutInflater, LayoutInflaterFactory layoutInflaterFactory) {
            LayoutInflaterCompatBase.setFactory(layoutInflater, layoutInflaterFactory);
        }
    }

    /* loaded from: a.zip:android/support/v4/view/LayoutInflaterCompat$LayoutInflaterCompatImplV11.class */
    static class LayoutInflaterCompatImplV11 extends LayoutInflaterCompatImplBase {
        LayoutInflaterCompatImplV11() {
        }

        @Override // android.support.v4.view.LayoutInflaterCompat.LayoutInflaterCompatImplBase, android.support.v4.view.LayoutInflaterCompat.LayoutInflaterCompatImpl
        public void setFactory(LayoutInflater layoutInflater, LayoutInflaterFactory layoutInflaterFactory) {
            LayoutInflaterCompatHC.setFactory(layoutInflater, layoutInflaterFactory);
        }
    }

    /* loaded from: a.zip:android/support/v4/view/LayoutInflaterCompat$LayoutInflaterCompatImplV21.class */
    static class LayoutInflaterCompatImplV21 extends LayoutInflaterCompatImplV11 {
        LayoutInflaterCompatImplV21() {
        }

        @Override // android.support.v4.view.LayoutInflaterCompat.LayoutInflaterCompatImplV11, android.support.v4.view.LayoutInflaterCompat.LayoutInflaterCompatImplBase, android.support.v4.view.LayoutInflaterCompat.LayoutInflaterCompatImpl
        public void setFactory(LayoutInflater layoutInflater, LayoutInflaterFactory layoutInflaterFactory) {
            LayoutInflaterCompatLollipop.setFactory(layoutInflater, layoutInflaterFactory);
        }
    }

    static {
        int i = Build.VERSION.SDK_INT;
        if (i >= 21) {
            IMPL = new LayoutInflaterCompatImplV21();
        } else if (i >= 11) {
            IMPL = new LayoutInflaterCompatImplV11();
        } else {
            IMPL = new LayoutInflaterCompatImplBase();
        }
    }

    private LayoutInflaterCompat() {
    }

    public static void setFactory(LayoutInflater layoutInflater, LayoutInflaterFactory layoutInflaterFactory) {
        IMPL.setFactory(layoutInflater, layoutInflaterFactory);
    }
}
