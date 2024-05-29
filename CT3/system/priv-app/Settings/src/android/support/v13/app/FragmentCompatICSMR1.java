package android.support.v13.app;

import android.app.Fragment;
/* loaded from: classes.dex */
class FragmentCompatICSMR1 {
    FragmentCompatICSMR1() {
    }

    public static void setUserVisibleHint(Fragment f, boolean isVisible) {
        if (f.getFragmentManager() == null) {
            return;
        }
        f.setUserVisibleHint(isVisible);
    }
}
