package com.android.systemui.statusbar.stack;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.view.View;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.ExpandableView;
/* loaded from: classes.dex */
public class ExpandableViewState extends ViewState {
    public boolean belowSpeedBump;
    public int clipTopAmount;
    public boolean dark;
    public boolean dimmed;
    public boolean headsUpIsVisible;
    public int height;
    public boolean hideSensitive;
    public boolean inShelf;
    public int location;
    public int notGoneIndex;
    public float shadowAlpha;

    @Override // com.android.systemui.statusbar.stack.ViewState
    public void copyFrom(ViewState viewState) {
        super.copyFrom(viewState);
        if (viewState instanceof ExpandableViewState) {
            ExpandableViewState expandableViewState = (ExpandableViewState) viewState;
            this.height = expandableViewState.height;
            this.dimmed = expandableViewState.dimmed;
            this.shadowAlpha = expandableViewState.shadowAlpha;
            this.dark = expandableViewState.dark;
            this.hideSensitive = expandableViewState.hideSensitive;
            this.belowSpeedBump = expandableViewState.belowSpeedBump;
            this.clipTopAmount = expandableViewState.clipTopAmount;
            this.notGoneIndex = expandableViewState.notGoneIndex;
            this.location = expandableViewState.location;
            this.headsUpIsVisible = expandableViewState.headsUpIsVisible;
        }
    }

    @Override // com.android.systemui.statusbar.stack.ViewState
    public void applyToView(View view) {
        super.applyToView(view);
        if (view instanceof ExpandableView) {
            ExpandableView expandableView = (ExpandableView) view;
            int actualHeight = expandableView.getActualHeight();
            int i = this.height;
            if (actualHeight != i) {
                expandableView.setActualHeight(i, false);
            }
            float shadowAlpha = expandableView.getShadowAlpha();
            float f = this.shadowAlpha;
            if (shadowAlpha != f) {
                expandableView.setShadowAlpha(f);
            }
            expandableView.setDimmed(this.dimmed, false);
            expandableView.setHideSensitive(this.hideSensitive, false, 0L, 0L);
            expandableView.setBelowSpeedBump(this.belowSpeedBump);
            expandableView.setDark(this.dark, false, 0L);
            if (expandableView.getClipTopAmount() != this.clipTopAmount) {
                expandableView.setClipTopAmount(this.clipTopAmount);
            }
            expandableView.setTransformingInShelf(false);
            expandableView.setInShelf(this.inShelf);
            if (this.headsUpIsVisible) {
                expandableView.setHeadsUpIsVisible();
            }
        }
    }

    @Override // com.android.systemui.statusbar.stack.ViewState
    public void animateTo(View view, AnimationProperties animationProperties) {
        super.animateTo(view, animationProperties);
        if (!(view instanceof ExpandableView)) {
            return;
        }
        ExpandableView expandableView = (ExpandableView) view;
        AnimationFilter animationFilter = animationProperties.getAnimationFilter();
        if (this.height != expandableView.getActualHeight()) {
            startHeightAnimation(expandableView, animationProperties);
        } else {
            abortAnimation(view, R.id.height_animator_tag);
        }
        if (this.shadowAlpha != expandableView.getShadowAlpha()) {
            startShadowAlphaAnimation(expandableView, animationProperties);
        } else {
            abortAnimation(view, R.id.shadow_alpha_animator_tag);
        }
        if (this.clipTopAmount != expandableView.getClipTopAmount()) {
            startInsetAnimation(expandableView, animationProperties);
        } else {
            abortAnimation(view, R.id.top_inset_animator_tag);
        }
        expandableView.setDimmed(this.dimmed, animationFilter.animateDimmed);
        expandableView.setBelowSpeedBump(this.belowSpeedBump);
        expandableView.setHideSensitive(this.hideSensitive, animationFilter.animateHideSensitive, animationProperties.delay, animationProperties.duration);
        expandableView.setDark(this.dark, animationFilter.animateDark, animationProperties.delay);
        if (animationProperties.wasAdded(view) && !this.hidden) {
            expandableView.performAddAnimation(animationProperties.delay, animationProperties.duration, false);
        }
        if (!expandableView.isInShelf() && this.inShelf) {
            expandableView.setTransformingInShelf(true);
        }
        expandableView.setInShelf(this.inShelf);
        if (this.headsUpIsVisible) {
            expandableView.setHeadsUpIsVisible();
        }
    }

    private void startHeightAnimation(final ExpandableView expandableView, AnimationProperties animationProperties) {
        Integer num = (Integer) getChildTag(expandableView, R.id.height_animator_start_value_tag);
        Integer num2 = (Integer) getChildTag(expandableView, R.id.height_animator_end_value_tag);
        int i = this.height;
        if (num2 != null && num2.intValue() == i) {
            return;
        }
        ValueAnimator valueAnimator = (ValueAnimator) getChildTag(expandableView, R.id.height_animator_tag);
        if (!animationProperties.getAnimationFilter().animateHeight) {
            if (valueAnimator != null) {
                PropertyValuesHolder[] values = valueAnimator.getValues();
                int intValue = num.intValue() + (i - num2.intValue());
                values[0].setIntValues(intValue, i);
                expandableView.setTag(R.id.height_animator_start_value_tag, Integer.valueOf(intValue));
                expandableView.setTag(R.id.height_animator_end_value_tag, Integer.valueOf(i));
                valueAnimator.setCurrentPlayTime(valueAnimator.getCurrentPlayTime());
                return;
            }
            expandableView.setActualHeight(i, false);
            return;
        }
        ValueAnimator ofInt = ValueAnimator.ofInt(expandableView.getActualHeight(), i);
        ofInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.statusbar.stack.ExpandableViewState.1
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator2) {
                expandableView.setActualHeight(((Integer) valueAnimator2.getAnimatedValue()).intValue(), false);
            }
        });
        ofInt.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        ofInt.setDuration(cancelAnimatorAndGetNewDuration(animationProperties.duration, valueAnimator));
        if (animationProperties.delay > 0 && (valueAnimator == null || valueAnimator.getAnimatedFraction() == 0.0f)) {
            ofInt.setStartDelay(animationProperties.delay);
        }
        AnimatorListenerAdapter animationFinishListener = animationProperties.getAnimationFinishListener();
        if (animationFinishListener != null) {
            ofInt.addListener(animationFinishListener);
        }
        ofInt.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.stack.ExpandableViewState.2
            boolean mWasCancelled;

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                expandableView.setTag(R.id.height_animator_tag, null);
                expandableView.setTag(R.id.height_animator_start_value_tag, null);
                expandableView.setTag(R.id.height_animator_end_value_tag, null);
                expandableView.setActualHeightAnimating(false);
                if (!this.mWasCancelled && (expandableView instanceof ExpandableNotificationRow)) {
                    ((ExpandableNotificationRow) expandableView).setGroupExpansionChanging(false);
                }
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                this.mWasCancelled = false;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                this.mWasCancelled = true;
            }
        });
        startAnimator(ofInt, animationFinishListener);
        expandableView.setTag(R.id.height_animator_tag, ofInt);
        expandableView.setTag(R.id.height_animator_start_value_tag, Integer.valueOf(expandableView.getActualHeight()));
        expandableView.setTag(R.id.height_animator_end_value_tag, Integer.valueOf(i));
        expandableView.setActualHeightAnimating(true);
    }

    private void startShadowAlphaAnimation(final ExpandableView expandableView, AnimationProperties animationProperties) {
        Float f = (Float) getChildTag(expandableView, R.id.shadow_alpha_animator_start_value_tag);
        Float f2 = (Float) getChildTag(expandableView, R.id.shadow_alpha_animator_end_value_tag);
        float f3 = this.shadowAlpha;
        if (f2 != null && f2.floatValue() == f3) {
            return;
        }
        ValueAnimator valueAnimator = (ValueAnimator) getChildTag(expandableView, R.id.shadow_alpha_animator_tag);
        if (!animationProperties.getAnimationFilter().animateShadowAlpha) {
            if (valueAnimator != null) {
                PropertyValuesHolder[] values = valueAnimator.getValues();
                float floatValue = f.floatValue() + (f3 - f2.floatValue());
                values[0].setFloatValues(floatValue, f3);
                expandableView.setTag(R.id.shadow_alpha_animator_start_value_tag, Float.valueOf(floatValue));
                expandableView.setTag(R.id.shadow_alpha_animator_end_value_tag, Float.valueOf(f3));
                valueAnimator.setCurrentPlayTime(valueAnimator.getCurrentPlayTime());
                return;
            }
            expandableView.setShadowAlpha(f3);
            return;
        }
        ValueAnimator ofFloat = ValueAnimator.ofFloat(expandableView.getShadowAlpha(), f3);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.statusbar.stack.ExpandableViewState.3
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator2) {
                expandableView.setShadowAlpha(((Float) valueAnimator2.getAnimatedValue()).floatValue());
            }
        });
        ofFloat.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        ofFloat.setDuration(cancelAnimatorAndGetNewDuration(animationProperties.duration, valueAnimator));
        if (animationProperties.delay > 0 && (valueAnimator == null || valueAnimator.getAnimatedFraction() == 0.0f)) {
            ofFloat.setStartDelay(animationProperties.delay);
        }
        AnimatorListenerAdapter animationFinishListener = animationProperties.getAnimationFinishListener();
        if (animationFinishListener != null) {
            ofFloat.addListener(animationFinishListener);
        }
        ofFloat.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.stack.ExpandableViewState.4
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                expandableView.setTag(R.id.shadow_alpha_animator_tag, null);
                expandableView.setTag(R.id.shadow_alpha_animator_start_value_tag, null);
                expandableView.setTag(R.id.shadow_alpha_animator_end_value_tag, null);
            }
        });
        startAnimator(ofFloat, animationFinishListener);
        expandableView.setTag(R.id.shadow_alpha_animator_tag, ofFloat);
        expandableView.setTag(R.id.shadow_alpha_animator_start_value_tag, Float.valueOf(expandableView.getShadowAlpha()));
        expandableView.setTag(R.id.shadow_alpha_animator_end_value_tag, Float.valueOf(f3));
    }

    private void startInsetAnimation(final ExpandableView expandableView, AnimationProperties animationProperties) {
        Integer num = (Integer) getChildTag(expandableView, R.id.top_inset_animator_start_value_tag);
        Integer num2 = (Integer) getChildTag(expandableView, R.id.top_inset_animator_end_value_tag);
        int i = this.clipTopAmount;
        if (num2 != null && num2.intValue() == i) {
            return;
        }
        ValueAnimator valueAnimator = (ValueAnimator) getChildTag(expandableView, R.id.top_inset_animator_tag);
        if (!animationProperties.getAnimationFilter().animateTopInset) {
            if (valueAnimator != null) {
                PropertyValuesHolder[] values = valueAnimator.getValues();
                int intValue = num.intValue() + (i - num2.intValue());
                values[0].setIntValues(intValue, i);
                expandableView.setTag(R.id.top_inset_animator_start_value_tag, Integer.valueOf(intValue));
                expandableView.setTag(R.id.top_inset_animator_end_value_tag, Integer.valueOf(i));
                valueAnimator.setCurrentPlayTime(valueAnimator.getCurrentPlayTime());
                return;
            }
            expandableView.setClipTopAmount(i);
            return;
        }
        ValueAnimator ofInt = ValueAnimator.ofInt(expandableView.getClipTopAmount(), i);
        ofInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.statusbar.stack.ExpandableViewState.5
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator2) {
                expandableView.setClipTopAmount(((Integer) valueAnimator2.getAnimatedValue()).intValue());
            }
        });
        ofInt.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        ofInt.setDuration(cancelAnimatorAndGetNewDuration(animationProperties.duration, valueAnimator));
        if (animationProperties.delay > 0 && (valueAnimator == null || valueAnimator.getAnimatedFraction() == 0.0f)) {
            ofInt.setStartDelay(animationProperties.delay);
        }
        AnimatorListenerAdapter animationFinishListener = animationProperties.getAnimationFinishListener();
        if (animationFinishListener != null) {
            ofInt.addListener(animationFinishListener);
        }
        ofInt.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.stack.ExpandableViewState.6
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                expandableView.setTag(R.id.top_inset_animator_tag, null);
                expandableView.setTag(R.id.top_inset_animator_start_value_tag, null);
                expandableView.setTag(R.id.top_inset_animator_end_value_tag, null);
            }
        });
        startAnimator(ofInt, animationFinishListener);
        expandableView.setTag(R.id.top_inset_animator_tag, ofInt);
        expandableView.setTag(R.id.top_inset_animator_start_value_tag, Integer.valueOf(expandableView.getClipTopAmount()));
        expandableView.setTag(R.id.top_inset_animator_end_value_tag, Integer.valueOf(i));
    }

    public static int getFinalActualHeight(ExpandableView expandableView) {
        if (expandableView == null) {
            return 0;
        }
        if (((ValueAnimator) getChildTag(expandableView, R.id.height_animator_tag)) == null) {
            return expandableView.getActualHeight();
        }
        return ((Integer) getChildTag(expandableView, R.id.height_animator_end_value_tag)).intValue();
    }

    @Override // com.android.systemui.statusbar.stack.ViewState
    public void cancelAnimations(View view) {
        super.cancelAnimations(view);
        Animator animator = (Animator) getChildTag(view, R.id.height_animator_tag);
        if (animator != null) {
            animator.cancel();
        }
        Animator animator2 = (Animator) getChildTag(view, R.id.shadow_alpha_animator_tag);
        if (animator2 != null) {
            animator2.cancel();
        }
        Animator animator3 = (Animator) getChildTag(view, R.id.top_inset_animator_tag);
        if (animator3 != null) {
            animator3.cancel();
        }
    }
}
