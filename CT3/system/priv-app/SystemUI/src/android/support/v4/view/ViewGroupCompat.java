package android.support.v4.view;

import android.os.Build;
import android.view.ViewGroup;
/* loaded from: a.zip:android/support/v4/view/ViewGroupCompat.class */
public final class ViewGroupCompat {
    static final ViewGroupCompatImpl IMPL;

    /* loaded from: a.zip:android/support/v4/view/ViewGroupCompat$ViewGroupCompatHCImpl.class */
    static class ViewGroupCompatHCImpl extends ViewGroupCompatStubImpl {
        ViewGroupCompatHCImpl() {
        }

        @Override // android.support.v4.view.ViewGroupCompat.ViewGroupCompatStubImpl, android.support.v4.view.ViewGroupCompat.ViewGroupCompatImpl
        public void setMotionEventSplittingEnabled(ViewGroup viewGroup, boolean z) {
            ViewGroupCompatHC.setMotionEventSplittingEnabled(viewGroup, z);
        }
    }

    /* loaded from: a.zip:android/support/v4/view/ViewGroupCompat$ViewGroupCompatIcsImpl.class */
    static class ViewGroupCompatIcsImpl extends ViewGroupCompatHCImpl {
        ViewGroupCompatIcsImpl() {
        }
    }

    /* loaded from: a.zip:android/support/v4/view/ViewGroupCompat$ViewGroupCompatImpl.class */
    interface ViewGroupCompatImpl {
        void setMotionEventSplittingEnabled(ViewGroup viewGroup, boolean z);
    }

    /* loaded from: a.zip:android/support/v4/view/ViewGroupCompat$ViewGroupCompatJellybeanMR2Impl.class */
    static class ViewGroupCompatJellybeanMR2Impl extends ViewGroupCompatIcsImpl {
        ViewGroupCompatJellybeanMR2Impl() {
        }
    }

    /* loaded from: a.zip:android/support/v4/view/ViewGroupCompat$ViewGroupCompatLollipopImpl.class */
    static class ViewGroupCompatLollipopImpl extends ViewGroupCompatJellybeanMR2Impl {
        ViewGroupCompatLollipopImpl() {
        }
    }

    /* loaded from: a.zip:android/support/v4/view/ViewGroupCompat$ViewGroupCompatStubImpl.class */
    static class ViewGroupCompatStubImpl implements ViewGroupCompatImpl {
        ViewGroupCompatStubImpl() {
        }

        @Override // android.support.v4.view.ViewGroupCompat.ViewGroupCompatImpl
        public void setMotionEventSplittingEnabled(ViewGroup viewGroup, boolean z) {
        }
    }

    static {
        int i = Build.VERSION.SDK_INT;
        if (i >= 21) {
            IMPL = new ViewGroupCompatLollipopImpl();
        } else if (i >= 18) {
            IMPL = new ViewGroupCompatJellybeanMR2Impl();
        } else if (i >= 14) {
            IMPL = new ViewGroupCompatIcsImpl();
        } else if (i >= 11) {
            IMPL = new ViewGroupCompatHCImpl();
        } else {
            IMPL = new ViewGroupCompatStubImpl();
        }
    }

    private ViewGroupCompat() {
    }

    public static void setMotionEventSplittingEnabled(ViewGroup viewGroup, boolean z) {
        IMPL.setMotionEventSplittingEnabled(viewGroup, z);
    }
}
