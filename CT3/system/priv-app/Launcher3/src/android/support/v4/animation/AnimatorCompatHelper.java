package android.support.v4.animation;

import android.os.Build;
import android.view.View;
/* loaded from: a.zip:android/support/v4/animation/AnimatorCompatHelper.class */
public final class AnimatorCompatHelper {
    private static final AnimatorProvider IMPL;

    static {
        if (Build.VERSION.SDK_INT >= 12) {
            IMPL = new HoneycombMr1AnimatorCompatProvider();
        } else {
            IMPL = new DonutAnimatorCompatProvider();
        }
    }

    private AnimatorCompatHelper() {
    }

    public static void clearInterpolator(View view) {
        IMPL.clearInterpolator(view);
    }
}
