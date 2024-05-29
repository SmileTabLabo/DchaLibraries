package android.support.v13.app;

import android.app.Fragment;
/* loaded from: b.zip:android/support/v13/app/FragmentCompatICSMR1.class */
class FragmentCompatICSMR1 {
    FragmentCompatICSMR1() {
    }

    public static void setUserVisibleHint(Fragment fragment, boolean z) {
        if (fragment.getFragmentManager() != null) {
            fragment.setUserVisibleHint(z);
        }
    }
}
