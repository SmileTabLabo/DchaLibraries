package android.support.v13.app;

import android.app.Fragment;
import android.os.Build;
@Deprecated
/* loaded from: classes.dex */
public class FragmentCompat {
    static final FragmentCompatImpl IMPL;

    /* loaded from: classes.dex */
    interface FragmentCompatImpl {
        void setUserVisibleHint(Fragment fragment, boolean z);
    }

    /* loaded from: classes.dex */
    static class FragmentCompatBaseImpl implements FragmentCompatImpl {
        FragmentCompatBaseImpl() {
        }

        @Override // android.support.v13.app.FragmentCompat.FragmentCompatImpl
        public void setUserVisibleHint(Fragment f, boolean deferStart) {
        }
    }

    /* loaded from: classes.dex */
    static class FragmentCompatApi15Impl extends FragmentCompatBaseImpl {
        FragmentCompatApi15Impl() {
        }

        @Override // android.support.v13.app.FragmentCompat.FragmentCompatBaseImpl, android.support.v13.app.FragmentCompat.FragmentCompatImpl
        public void setUserVisibleHint(Fragment f, boolean deferStart) {
            f.setUserVisibleHint(deferStart);
        }
    }

    /* loaded from: classes.dex */
    static class FragmentCompatApi23Impl extends FragmentCompatApi15Impl {
        FragmentCompatApi23Impl() {
        }
    }

    /* loaded from: classes.dex */
    static class FragmentCompatApi24Impl extends FragmentCompatApi23Impl {
        FragmentCompatApi24Impl() {
        }

        @Override // android.support.v13.app.FragmentCompat.FragmentCompatApi15Impl, android.support.v13.app.FragmentCompat.FragmentCompatBaseImpl, android.support.v13.app.FragmentCompat.FragmentCompatImpl
        public void setUserVisibleHint(Fragment f, boolean deferStart) {
            f.setUserVisibleHint(deferStart);
        }
    }

    static {
        if (Build.VERSION.SDK_INT >= 24) {
            IMPL = new FragmentCompatApi24Impl();
        } else if (Build.VERSION.SDK_INT >= 23) {
            IMPL = new FragmentCompatApi23Impl();
        } else if (Build.VERSION.SDK_INT >= 15) {
            IMPL = new FragmentCompatApi15Impl();
        } else {
            IMPL = new FragmentCompatBaseImpl();
        }
    }

    @Deprecated
    public static void setUserVisibleHint(Fragment f, boolean deferStart) {
        IMPL.setUserVisibleHint(f, deferStart);
    }
}
