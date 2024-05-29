package android.support.design.internal;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.support.transition.Transition;
import android.support.transition.TransitionValues;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.Map;
/* loaded from: classes.dex */
public class TextScale extends Transition {
    @Override // android.support.transition.Transition
    public void captureStartValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    @Override // android.support.transition.Transition
    public void captureEndValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    private void captureValues(TransitionValues transitionValues) {
        if (transitionValues.view instanceof TextView) {
            TextView textview = (TextView) transitionValues.view;
            transitionValues.values.put("android:textscale:scale", Float.valueOf(textview.getScaleX()));
        }
    }

    @Override // android.support.transition.Transition
    public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
        if (startValues == null || endValues == null || !(startValues.view instanceof TextView) || !(endValues.view instanceof TextView)) {
            return null;
        }
        final TextView view = (TextView) endValues.view;
        Map<String, Object> startVals = startValues.values;
        Map<String, Object> endVals = endValues.values;
        float startSize = startVals.get("android:textscale:scale") != null ? ((Float) startVals.get("android:textscale:scale")).floatValue() : 1.0f;
        float endSize = endVals.get("android:textscale:scale") != null ? ((Float) endVals.get("android:textscale:scale")).floatValue() : 1.0f;
        if (startSize == endSize) {
            return null;
        }
        ValueAnimator animator = ValueAnimator.ofFloat(startSize, endSize);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: android.support.design.internal.TextScale.1
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                view.setScaleX(animatedValue);
                view.setScaleY(animatedValue);
            }
        });
        return animator;
    }
}
