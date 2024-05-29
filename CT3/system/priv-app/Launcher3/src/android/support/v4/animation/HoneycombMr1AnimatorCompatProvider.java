package android.support.v4.animation;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.view.View;
/* loaded from: a.zip:android/support/v4/animation/HoneycombMr1AnimatorCompatProvider.class */
class HoneycombMr1AnimatorCompatProvider implements AnimatorProvider {
    private TimeInterpolator mDefaultInterpolator;

    @Override // android.support.v4.animation.AnimatorProvider
    public void clearInterpolator(View view) {
        if (this.mDefaultInterpolator == null) {
            this.mDefaultInterpolator = new ValueAnimator().getInterpolator();
        }
        view.animate().setInterpolator(this.mDefaultInterpolator);
    }
}
