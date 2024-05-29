package android.support.v17.leanback.widget;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
/* loaded from: a.zip:android/support/v17/leanback/widget/Util.class */
public class Util {
    public static boolean isDescendant(ViewGroup viewGroup, View view) {
        while (view != null) {
            if (view == viewGroup) {
                return true;
            }
            ViewParent parent = view.getParent();
            if (!(parent instanceof View)) {
                return false;
            }
            view = (View) parent;
        }
        return false;
    }
}
