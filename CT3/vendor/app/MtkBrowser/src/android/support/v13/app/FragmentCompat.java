package android.support.v13.app;

import android.app.Fragment;
import android.os.Build;
import android.support.v4.os.BuildCompat;
/* loaded from: b.zip:android/support/v13/app/FragmentCompat.class */
public class FragmentCompat {
    static final FragmentCompatImpl IMPL;

    /* loaded from: b.zip:android/support/v13/app/FragmentCompat$BaseFragmentCompatImpl.class */
    static class BaseFragmentCompatImpl implements FragmentCompatImpl {
        BaseFragmentCompatImpl() {
        }

        @Override // android.support.v13.app.FragmentCompat.FragmentCompatImpl
        public void setMenuVisibility(Fragment fragment, boolean z) {
        }

        @Override // android.support.v13.app.FragmentCompat.FragmentCompatImpl
        public void setUserVisibleHint(Fragment fragment, boolean z) {
        }
    }

    /* loaded from: b.zip:android/support/v13/app/FragmentCompat$FragmentCompatImpl.class */
    interface FragmentCompatImpl {
        void setMenuVisibility(Fragment fragment, boolean z);

        void setUserVisibleHint(Fragment fragment, boolean z);
    }

    /* loaded from: b.zip:android/support/v13/app/FragmentCompat$ICSFragmentCompatImpl.class */
    static class ICSFragmentCompatImpl extends BaseFragmentCompatImpl {
        ICSFragmentCompatImpl() {
        }

        @Override // android.support.v13.app.FragmentCompat.BaseFragmentCompatImpl, android.support.v13.app.FragmentCompat.FragmentCompatImpl
        public void setMenuVisibility(Fragment fragment, boolean z) {
            FragmentCompatICS.setMenuVisibility(fragment, z);
        }
    }

    /* loaded from: b.zip:android/support/v13/app/FragmentCompat$ICSMR1FragmentCompatImpl.class */
    static class ICSMR1FragmentCompatImpl extends ICSFragmentCompatImpl {
        ICSMR1FragmentCompatImpl() {
        }

        @Override // android.support.v13.app.FragmentCompat.BaseFragmentCompatImpl, android.support.v13.app.FragmentCompat.FragmentCompatImpl
        public void setUserVisibleHint(Fragment fragment, boolean z) {
            FragmentCompatICSMR1.setUserVisibleHint(fragment, z);
        }
    }

    /* loaded from: b.zip:android/support/v13/app/FragmentCompat$MncFragmentCompatImpl.class */
    static class MncFragmentCompatImpl extends ICSMR1FragmentCompatImpl {
        MncFragmentCompatImpl() {
        }
    }

    /* loaded from: b.zip:android/support/v13/app/FragmentCompat$NFragmentCompatImpl.class */
    static class NFragmentCompatImpl extends MncFragmentCompatImpl {
        NFragmentCompatImpl() {
        }

        @Override // android.support.v13.app.FragmentCompat.ICSMR1FragmentCompatImpl, android.support.v13.app.FragmentCompat.BaseFragmentCompatImpl, android.support.v13.app.FragmentCompat.FragmentCompatImpl
        public void setUserVisibleHint(Fragment fragment, boolean z) {
            FragmentCompatApi24.setUserVisibleHint(fragment, z);
        }
    }

    static {
        if (BuildCompat.isAtLeastN()) {
            IMPL = new NFragmentCompatImpl();
        } else if (Build.VERSION.SDK_INT >= 23) {
            IMPL = new MncFragmentCompatImpl();
        } else if (Build.VERSION.SDK_INT >= 15) {
            IMPL = new ICSMR1FragmentCompatImpl();
        } else if (Build.VERSION.SDK_INT >= 14) {
            IMPL = new ICSFragmentCompatImpl();
        } else {
            IMPL = new BaseFragmentCompatImpl();
        }
    }

    public static void setMenuVisibility(Fragment fragment, boolean z) {
        IMPL.setMenuVisibility(fragment, z);
    }

    public static void setUserVisibleHint(Fragment fragment, boolean z) {
        IMPL.setUserVisibleHint(fragment, z);
    }
}
