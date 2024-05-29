package com.android.systemui.statusbar.stack;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import com.android.systemui.Interpolators;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.ExpandableView;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.stack.NotificationStackScrollLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Stack;
/* loaded from: a.zip:com/android/systemui/statusbar/stack/StackStateAnimator.class */
public class StackStateAnimator {
    private ValueAnimator mBottomOverScrollAnimator;
    private long mCurrentAdditionalDelay;
    private int mCurrentLastNotAddedIndex;
    private long mCurrentLength;
    private final int mGoToFullShadeAppearingTranslation;
    private int mHeadsUpAppearHeightBottom;
    public NotificationStackScrollLayout mHostLayout;
    private boolean mShadeExpanded;
    private ValueAnimator mTopOverScrollAnimator;
    private final StackViewState mTmpState = new StackViewState();
    private ArrayList<NotificationStackScrollLayout.AnimationEvent> mNewEvents = new ArrayList<>();
    private ArrayList<View> mNewAddChildren = new ArrayList<>();
    private HashSet<View> mHeadsUpAppearChildren = new HashSet<>();
    private HashSet<View> mHeadsUpDisappearChildren = new HashSet<>();
    private HashSet<Animator> mAnimatorSet = new HashSet<>();
    private Stack<AnimatorListenerAdapter> mAnimationListenerPool = new Stack<>();
    private AnimationFilter mAnimationFilter = new AnimationFilter();
    private ArrayList<View> mChildrenToClearFromOverlay = new ArrayList<>();
    private final Interpolator mHeadsUpAppearInterpolator = new HeadsUpAppearInterpolator();

    public StackStateAnimator(NotificationStackScrollLayout notificationStackScrollLayout) {
        this.mHostLayout = notificationStackScrollLayout;
        this.mGoToFullShadeAppearingTranslation = notificationStackScrollLayout.getContext().getResources().getDimensionPixelSize(2131689926);
    }

    private void abortAnimation(View view, int i) {
        Animator animator = (Animator) getChildTag(view, i);
        if (animator != null) {
            animator.cancel();
        }
    }

    private boolean applyWithoutAnimation(ExpandableView expandableView, StackViewState stackViewState, StackScrollState stackScrollState) {
        if (this.mShadeExpanded || getChildTag(expandableView, 2131886108) != null || this.mHeadsUpDisappearChildren.contains(expandableView) || this.mHeadsUpAppearChildren.contains(expandableView) || NotificationStackScrollLayout.isPinnedHeadsUp(expandableView)) {
            return false;
        }
        stackScrollState.applyState(expandableView, stackViewState);
        return true;
    }

    private long calculateChildAnimationDelay(StackViewState stackViewState, StackScrollState stackScrollState) {
        int i;
        if (this.mAnimationFilter.hasDarkEvent) {
            return calculateDelayDark(stackViewState);
        }
        if (this.mAnimationFilter.hasGoToFullShadeEvent) {
            return calculateDelayGoToFullShade(stackViewState);
        }
        if (this.mAnimationFilter.hasHeadsUpDisappearClickEvent) {
            return 120L;
        }
        long j = 0;
        for (NotificationStackScrollLayout.AnimationEvent animationEvent : this.mNewEvents) {
            long j2 = 80;
            switch (animationEvent.animationType) {
                case 0:
                    j = Math.max((2 - Math.max(0, Math.min(2, Math.abs(stackViewState.notGoneIndex - stackScrollState.getViewStateForView(animationEvent.changingView).notGoneIndex) - 1))) * 80, j);
                    continue;
                case 2:
                    j2 = 32;
                    break;
            }
            int i2 = stackViewState.notGoneIndex;
            int i3 = i2;
            if (i2 >= stackScrollState.getViewStateForView(animationEvent.viewAfterChangingView == null ? this.mHostLayout.getLastChildNotGone() : animationEvent.viewAfterChangingView).notGoneIndex) {
                i3 = i2 + 1;
            }
            j = Math.max(Math.max(0, Math.min(2, Math.abs(i3 - i) - 1)) * j2, j);
        }
        return j;
    }

    private long calculateDelayDark(StackViewState stackViewState) {
        return Math.abs((this.mAnimationFilter.darkAnimationOriginIndex == -1 ? 0 : this.mAnimationFilter.darkAnimationOriginIndex == -2 ? this.mHostLayout.getNotGoneChildCount() - 1 : this.mAnimationFilter.darkAnimationOriginIndex) - stackViewState.notGoneIndex) * 24;
    }

    private long calculateDelayGoToFullShade(StackViewState stackViewState) {
        return 48.0f * ((float) Math.pow(stackViewState.notGoneIndex, 0.699999988079071d));
    }

    private long cancelAnimatorAndGetNewDuration(long j, ValueAnimator valueAnimator) {
        long j2 = j;
        if (valueAnimator != null) {
            j2 = Math.max(valueAnimator.getDuration() - valueAnimator.getCurrentPlayTime(), j);
            valueAnimator.cancel();
        }
        return j2;
    }

    private int findLastNotAddedIndex(StackScrollState stackScrollState) {
        for (int childCount = this.mHostLayout.getChildCount() - 1; childCount >= 0; childCount--) {
            ExpandableView expandableView = (ExpandableView) this.mHostLayout.getChildAt(childCount);
            StackViewState viewStateForView = stackScrollState.getViewStateForView(expandableView);
            if (viewStateForView != null && expandableView.getVisibility() != 8 && !this.mNewAddChildren.contains(expandableView)) {
                return viewStateForView.notGoneIndex;
            }
        }
        return -1;
    }

    public static <T> T getChildTag(View view, int i) {
        return (T) view.getTag(i);
    }

    public static int getFinalActualHeight(ExpandableView expandableView) {
        if (expandableView == null) {
            return 0;
        }
        return ((ValueAnimator) getChildTag(expandableView, 2131886112)) == null ? expandableView.getActualHeight() : ((Integer) getChildTag(expandableView, 2131886118)).intValue();
    }

    public static float getFinalTranslationY(View view) {
        if (view == null) {
            return 0.0f;
        }
        return ((ValueAnimator) getChildTag(view, 2131886108)) == null ? view.getTranslationY() : ((Float) getChildTag(view, 2131886114)).floatValue();
    }

    private AnimatorListenerAdapter getGlobalAnimationFinishedListener() {
        return !this.mAnimationListenerPool.empty() ? this.mAnimationListenerPool.pop() : new AnimatorListenerAdapter(this) { // from class: com.android.systemui.statusbar.stack.StackStateAnimator.10
            private boolean mWasCancelled;
            final StackStateAnimator this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                this.mWasCancelled = true;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.this$0.mAnimatorSet.remove(animator);
                if (this.this$0.mAnimatorSet.isEmpty() && !this.mWasCancelled) {
                    this.this$0.onAnimationFinished();
                }
                this.this$0.mAnimationListenerPool.push(this);
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                this.mWasCancelled = false;
            }
        };
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onAnimationFinished() {
        this.mHostLayout.onChildAnimationFinished();
        for (View view : this.mChildrenToClearFromOverlay) {
            removeFromOverlay(view);
        }
        this.mChildrenToClearFromOverlay.clear();
    }

    private void processAnimationEvents(ArrayList<NotificationStackScrollLayout.AnimationEvent> arrayList, StackScrollState stackScrollState) {
        for (NotificationStackScrollLayout.AnimationEvent animationEvent : arrayList) {
            ExpandableView expandableView = (ExpandableView) animationEvent.changingView;
            if (animationEvent.animationType == 0) {
                StackViewState viewStateForView = stackScrollState.getViewStateForView(expandableView);
                if (viewStateForView != null) {
                    stackScrollState.applyState(expandableView, viewStateForView);
                    this.mNewAddChildren.add(expandableView);
                    this.mNewEvents.add(animationEvent);
                }
            } else {
                if (animationEvent.animationType == 1) {
                    if (expandableView.getVisibility() == 8) {
                        removeFromOverlay(expandableView);
                    } else {
                        StackViewState viewStateForView2 = stackScrollState.getViewStateForView(animationEvent.viewAfterChangingView);
                        int actualHeight = expandableView.getActualHeight();
                        expandableView.performRemoveAnimation(464L, viewStateForView2 != null ? Math.max(Math.min(((viewStateForView2.yTranslation - (expandableView.getTranslationY() + (actualHeight / 2.0f))) * 2.0f) / actualHeight, 1.0f), -1.0f) : -1.0f, new Runnable(this, expandableView) { // from class: com.android.systemui.statusbar.stack.StackStateAnimator.11
                            final StackStateAnimator this$0;
                            final ExpandableView val$changingView;

                            {
                                this.this$0 = this;
                                this.val$changingView = expandableView;
                            }

                            @Override // java.lang.Runnable
                            public void run() {
                                StackStateAnimator.removeFromOverlay(this.val$changingView);
                            }
                        });
                    }
                } else if (animationEvent.animationType == 2) {
                    this.mHostLayout.getOverlay().remove(expandableView);
                    if (Math.abs(expandableView.getTranslation()) == expandableView.getWidth() && expandableView.getTransientContainer() != null) {
                        expandableView.getTransientContainer().removeTransientView(expandableView);
                    }
                } else if (animationEvent.animationType == 13) {
                    ((ExpandableNotificationRow) animationEvent.changingView).prepareExpansionChanged(stackScrollState);
                } else if (animationEvent.animationType == 14) {
                    this.mTmpState.copyFrom(stackScrollState.getViewStateForView(expandableView));
                    if (animationEvent.headsUpFromBottom) {
                        this.mTmpState.yTranslation = this.mHeadsUpAppearHeightBottom;
                    } else {
                        this.mTmpState.yTranslation = -this.mTmpState.height;
                    }
                    this.mHeadsUpAppearChildren.add(expandableView);
                    stackScrollState.applyState(expandableView, this.mTmpState);
                } else if (animationEvent.animationType == 15 || animationEvent.animationType == 16) {
                    this.mHeadsUpDisappearChildren.add(expandableView);
                    if (expandableView.getParent() == null) {
                        this.mHostLayout.getOverlay().add(expandableView);
                        this.mTmpState.initFrom(expandableView);
                        this.mTmpState.yTranslation = -expandableView.getActualHeight();
                        this.mAnimationFilter.animateY = true;
                        startViewAnimations(expandableView, this.mTmpState, animationEvent.animationType == 16 ? 120 : 0, 230L);
                        this.mChildrenToClearFromOverlay.add(expandableView);
                    }
                }
                this.mNewEvents.add(animationEvent);
            }
        }
    }

    public static void removeFromOverlay(View view) {
        ViewGroup viewGroup = (ViewGroup) view.getParent();
        if (viewGroup != null) {
            viewGroup.removeView(view);
        }
    }

    private void startAlphaAnimation(View view, ViewState viewState, long j, long j2) {
        Float f = (Float) getChildTag(view, 2131886122);
        Float f2 = (Float) getChildTag(view, 2131886116);
        float f3 = viewState.alpha;
        if (f2 == null || f2.floatValue() != f3) {
            ObjectAnimator objectAnimator = (ObjectAnimator) getChildTag(view, 2131886110);
            if (!this.mAnimationFilter.animateAlpha) {
                if (objectAnimator != null) {
                    PropertyValuesHolder[] values = objectAnimator.getValues();
                    float floatValue = f.floatValue() + (f3 - f2.floatValue());
                    values[0].setFloatValues(floatValue, f3);
                    view.setTag(2131886122, Float.valueOf(floatValue));
                    view.setTag(2131886116, Float.valueOf(f3));
                    objectAnimator.setCurrentPlayTime(objectAnimator.getCurrentPlayTime());
                    return;
                }
                view.setAlpha(f3);
                if (f3 == 0.0f) {
                    view.setVisibility(4);
                }
            }
            ObjectAnimator ofFloat = ObjectAnimator.ofFloat(view, View.ALPHA, view.getAlpha(), f3);
            ofFloat.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
            view.setLayerType(2, null);
            ofFloat.addListener(new AnimatorListenerAdapter(this, view, f3) { // from class: com.android.systemui.statusbar.stack.StackStateAnimator.7
                public boolean mWasCancelled;
                final StackStateAnimator this$0;
                final View val$child;
                final float val$newEndValue;

                {
                    this.this$0 = this;
                    this.val$child = view;
                    this.val$newEndValue = f3;
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationCancel(Animator animator) {
                    this.mWasCancelled = true;
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    this.val$child.setLayerType(0, null);
                    if (this.val$newEndValue == 0.0f && !this.mWasCancelled) {
                        this.val$child.setVisibility(4);
                    }
                    this.val$child.setTag(2131886110, null);
                    this.val$child.setTag(2131886122, null);
                    this.val$child.setTag(2131886116, null);
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationStart(Animator animator) {
                    this.mWasCancelled = false;
                }
            });
            ofFloat.setDuration(cancelAnimatorAndGetNewDuration(j, objectAnimator));
            if (j2 > 0 && (objectAnimator == null || objectAnimator.getAnimatedFraction() == 0.0f)) {
                ofFloat.setStartDelay(j2);
            }
            ofFloat.addListener(getGlobalAnimationFinishedListener());
            startAnimator(ofFloat);
            view.setTag(2131886110, ofFloat);
            view.setTag(2131886122, Float.valueOf(view.getAlpha()));
            view.setTag(2131886116, Float.valueOf(f3));
        }
    }

    private void startAnimator(ValueAnimator valueAnimator) {
        this.mAnimatorSet.add(valueAnimator);
        valueAnimator.start();
    }

    private void startHeightAnimation(ExpandableView expandableView, StackViewState stackViewState, long j, long j2) {
        Integer num = (Integer) getChildTag(expandableView, 2131886124);
        Integer num2 = (Integer) getChildTag(expandableView, 2131886118);
        int i = stackViewState.height;
        if (num2 == null || num2.intValue() != i) {
            ValueAnimator valueAnimator = (ValueAnimator) getChildTag(expandableView, 2131886112);
            if (!this.mAnimationFilter.animateHeight) {
                if (valueAnimator == null) {
                    expandableView.setActualHeight(i, false);
                    return;
                }
                PropertyValuesHolder[] values = valueAnimator.getValues();
                int intValue = num.intValue() + (i - num2.intValue());
                values[0].setIntValues(intValue, i);
                expandableView.setTag(2131886124, Integer.valueOf(intValue));
                expandableView.setTag(2131886118, Integer.valueOf(i));
                valueAnimator.setCurrentPlayTime(valueAnimator.getCurrentPlayTime());
                return;
            }
            ValueAnimator ofInt = ValueAnimator.ofInt(expandableView.getActualHeight(), i);
            ofInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this, expandableView) { // from class: com.android.systemui.statusbar.stack.StackStateAnimator.3
                final StackStateAnimator this$0;
                final ExpandableView val$child;

                {
                    this.this$0 = this;
                    this.val$child = expandableView;
                }

                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator2) {
                    this.val$child.setActualHeight(((Integer) valueAnimator2.getAnimatedValue()).intValue(), false);
                }
            });
            ofInt.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
            ofInt.setDuration(cancelAnimatorAndGetNewDuration(j, valueAnimator));
            if (j2 > 0 && (valueAnimator == null || valueAnimator.getAnimatedFraction() == 0.0f)) {
                ofInt.setStartDelay(j2);
            }
            ofInt.addListener(getGlobalAnimationFinishedListener());
            ofInt.addListener(new AnimatorListenerAdapter(this, expandableView) { // from class: com.android.systemui.statusbar.stack.StackStateAnimator.4
                boolean mWasCancelled;
                final StackStateAnimator this$0;
                final ExpandableView val$child;

                {
                    this.this$0 = this;
                    this.val$child = expandableView;
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationCancel(Animator animator) {
                    this.mWasCancelled = true;
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    this.val$child.setTag(2131886112, null);
                    this.val$child.setTag(2131886124, null);
                    this.val$child.setTag(2131886118, null);
                    this.val$child.setActualHeightAnimating(false);
                    if (this.mWasCancelled || !(this.val$child instanceof ExpandableNotificationRow)) {
                        return;
                    }
                    ((ExpandableNotificationRow) this.val$child).setGroupExpansionChanging(false);
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationStart(Animator animator) {
                    this.mWasCancelled = false;
                }
            });
            startAnimator(ofInt);
            expandableView.setTag(2131886112, ofInt);
            expandableView.setTag(2131886124, Integer.valueOf(expandableView.getActualHeight()));
            expandableView.setTag(2131886118, Integer.valueOf(i));
            expandableView.setActualHeightAnimating(true);
        }
    }

    private void startInsetAnimation(ExpandableView expandableView, StackViewState stackViewState, long j, long j2) {
        Integer num = (Integer) getChildTag(expandableView, 2131886123);
        Integer num2 = (Integer) getChildTag(expandableView, 2131886117);
        int i = stackViewState.clipTopAmount;
        if (num2 == null || num2.intValue() != i) {
            ValueAnimator valueAnimator = (ValueAnimator) getChildTag(expandableView, 2131886111);
            if (!this.mAnimationFilter.animateTopInset) {
                if (valueAnimator == null) {
                    expandableView.setClipTopAmount(i);
                    return;
                }
                PropertyValuesHolder[] values = valueAnimator.getValues();
                int intValue = num.intValue() + (i - num2.intValue());
                values[0].setIntValues(intValue, i);
                expandableView.setTag(2131886123, Integer.valueOf(intValue));
                expandableView.setTag(2131886117, Integer.valueOf(i));
                valueAnimator.setCurrentPlayTime(valueAnimator.getCurrentPlayTime());
                return;
            }
            ValueAnimator ofInt = ValueAnimator.ofInt(expandableView.getClipTopAmount(), i);
            ofInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this, expandableView) { // from class: com.android.systemui.statusbar.stack.StackStateAnimator.5
                final StackStateAnimator this$0;
                final ExpandableView val$child;

                {
                    this.this$0 = this;
                    this.val$child = expandableView;
                }

                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator2) {
                    this.val$child.setClipTopAmount(((Integer) valueAnimator2.getAnimatedValue()).intValue());
                }
            });
            ofInt.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
            ofInt.setDuration(cancelAnimatorAndGetNewDuration(j, valueAnimator));
            if (j2 > 0 && (valueAnimator == null || valueAnimator.getAnimatedFraction() == 0.0f)) {
                ofInt.setStartDelay(j2);
            }
            ofInt.addListener(getGlobalAnimationFinishedListener());
            ofInt.addListener(new AnimatorListenerAdapter(this, expandableView) { // from class: com.android.systemui.statusbar.stack.StackStateAnimator.6
                final StackStateAnimator this$0;
                final ExpandableView val$child;

                {
                    this.this$0 = this;
                    this.val$child = expandableView;
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    this.val$child.setTag(2131886111, null);
                    this.val$child.setTag(2131886123, null);
                    this.val$child.setTag(2131886117, null);
                }
            });
            startAnimator(ofInt);
            expandableView.setTag(2131886111, ofInt);
            expandableView.setTag(2131886123, Integer.valueOf(expandableView.getClipTopAmount()));
            expandableView.setTag(2131886117, Integer.valueOf(i));
        }
    }

    private void startShadowAlphaAnimation(ExpandableView expandableView, StackViewState stackViewState, long j, long j2) {
        Float f = (Float) getChildTag(expandableView, 2131886125);
        Float f2 = (Float) getChildTag(expandableView, 2131886119);
        float f3 = stackViewState.shadowAlpha;
        if (f2 == null || f2.floatValue() != f3) {
            ValueAnimator valueAnimator = (ValueAnimator) getChildTag(expandableView, 2131886113);
            if (!this.mAnimationFilter.animateShadowAlpha) {
                if (valueAnimator == null) {
                    expandableView.setShadowAlpha(f3);
                    return;
                }
                PropertyValuesHolder[] values = valueAnimator.getValues();
                float floatValue = f.floatValue() + (f3 - f2.floatValue());
                values[0].setFloatValues(floatValue, f3);
                expandableView.setTag(2131886125, Float.valueOf(floatValue));
                expandableView.setTag(2131886119, Float.valueOf(f3));
                valueAnimator.setCurrentPlayTime(valueAnimator.getCurrentPlayTime());
                return;
            }
            ValueAnimator ofFloat = ValueAnimator.ofFloat(expandableView.getShadowAlpha(), f3);
            ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this, expandableView) { // from class: com.android.systemui.statusbar.stack.StackStateAnimator.1
                final StackStateAnimator this$0;
                final ExpandableView val$child;

                {
                    this.this$0 = this;
                    this.val$child = expandableView;
                }

                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator2) {
                    this.val$child.setShadowAlpha(((Float) valueAnimator2.getAnimatedValue()).floatValue());
                }
            });
            ofFloat.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
            ofFloat.setDuration(cancelAnimatorAndGetNewDuration(j, valueAnimator));
            if (j2 > 0 && (valueAnimator == null || valueAnimator.getAnimatedFraction() == 0.0f)) {
                ofFloat.setStartDelay(j2);
            }
            ofFloat.addListener(getGlobalAnimationFinishedListener());
            ofFloat.addListener(new AnimatorListenerAdapter(this, expandableView) { // from class: com.android.systemui.statusbar.stack.StackStateAnimator.2
                final StackStateAnimator this$0;
                final ExpandableView val$child;

                {
                    this.this$0 = this;
                    this.val$child = expandableView;
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    this.val$child.setTag(2131886113, null);
                    this.val$child.setTag(2131886125, null);
                    this.val$child.setTag(2131886119, null);
                }
            });
            startAnimator(ofFloat);
            expandableView.setTag(2131886113, ofFloat);
            expandableView.setTag(2131886125, Float.valueOf(expandableView.getShadowAlpha()));
            expandableView.setTag(2131886119, Float.valueOf(f3));
        }
    }

    private void startYTranslationAnimation(View view, ViewState viewState, long j, long j2) {
        Float f = (Float) getChildTag(view, 2131886120);
        Float f2 = (Float) getChildTag(view, 2131886114);
        float f3 = viewState.yTranslation;
        if (f2 == null || f2.floatValue() != f3) {
            ObjectAnimator objectAnimator = (ObjectAnimator) getChildTag(view, 2131886108);
            if (!this.mAnimationFilter.animateY) {
                if (objectAnimator == null) {
                    view.setTranslationY(f3);
                    return;
                }
                PropertyValuesHolder[] values = objectAnimator.getValues();
                float floatValue = f.floatValue() + (f3 - f2.floatValue());
                values[0].setFloatValues(floatValue, f3);
                view.setTag(2131886120, Float.valueOf(floatValue));
                view.setTag(2131886114, Float.valueOf(f3));
                objectAnimator.setCurrentPlayTime(objectAnimator.getCurrentPlayTime());
                return;
            }
            ObjectAnimator ofFloat = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, view.getTranslationY(), f3);
            ofFloat.setInterpolator(this.mHeadsUpAppearChildren.contains(view) ? this.mHeadsUpAppearInterpolator : Interpolators.FAST_OUT_SLOW_IN);
            ofFloat.setDuration(cancelAnimatorAndGetNewDuration(j, objectAnimator));
            if (j2 > 0 && (objectAnimator == null || objectAnimator.getAnimatedFraction() == 0.0f)) {
                ofFloat.setStartDelay(j2);
            }
            ofFloat.addListener(getGlobalAnimationFinishedListener());
            ofFloat.addListener(new AnimatorListenerAdapter(this, view, this.mHeadsUpDisappearChildren.contains(view)) { // from class: com.android.systemui.statusbar.stack.StackStateAnimator.9
                final StackStateAnimator this$0;
                final View val$child;
                final boolean val$isHeadsUpDisappear;

                {
                    this.this$0 = this;
                    this.val$child = view;
                    this.val$isHeadsUpDisappear = r6;
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    HeadsUpManager.setIsClickedNotification(this.val$child, false);
                    this.val$child.setTag(2131886108, null);
                    this.val$child.setTag(2131886120, null);
                    this.val$child.setTag(2131886114, null);
                    if (this.val$isHeadsUpDisappear) {
                        ((ExpandableNotificationRow) this.val$child).setHeadsupDisappearRunning(false);
                    }
                }
            });
            startAnimator(ofFloat);
            view.setTag(2131886108, ofFloat);
            view.setTag(2131886120, Float.valueOf(view.getTranslationY()));
            view.setTag(2131886114, Float.valueOf(f3));
        }
    }

    private void startZTranslationAnimation(View view, ViewState viewState, long j, long j2) {
        Float f = (Float) getChildTag(view, 2131886121);
        Float f2 = (Float) getChildTag(view, 2131886115);
        float f3 = viewState.zTranslation;
        if (f2 == null || f2.floatValue() != f3) {
            ObjectAnimator objectAnimator = (ObjectAnimator) getChildTag(view, 2131886109);
            if (!this.mAnimationFilter.animateZ) {
                if (objectAnimator != null) {
                    PropertyValuesHolder[] values = objectAnimator.getValues();
                    float floatValue = f.floatValue() + (f3 - f2.floatValue());
                    values[0].setFloatValues(floatValue, f3);
                    view.setTag(2131886121, Float.valueOf(floatValue));
                    view.setTag(2131886115, Float.valueOf(f3));
                    objectAnimator.setCurrentPlayTime(objectAnimator.getCurrentPlayTime());
                    return;
                }
                view.setTranslationZ(f3);
            }
            ObjectAnimator ofFloat = ObjectAnimator.ofFloat(view, View.TRANSLATION_Z, view.getTranslationZ(), f3);
            ofFloat.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
            ofFloat.setDuration(cancelAnimatorAndGetNewDuration(j, objectAnimator));
            if (j2 > 0 && (objectAnimator == null || objectAnimator.getAnimatedFraction() == 0.0f)) {
                ofFloat.setStartDelay(j2);
            }
            ofFloat.addListener(getGlobalAnimationFinishedListener());
            ofFloat.addListener(new AnimatorListenerAdapter(this, view) { // from class: com.android.systemui.statusbar.stack.StackStateAnimator.8
                final StackStateAnimator this$0;
                final View val$child;

                {
                    this.this$0 = this;
                    this.val$child = view;
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    this.val$child.setTag(2131886109, null);
                    this.val$child.setTag(2131886121, null);
                    this.val$child.setTag(2131886115, null);
                }
            });
            startAnimator(ofFloat);
            view.setTag(2131886109, ofFloat);
            view.setTag(2131886121, Float.valueOf(view.getTranslationZ()));
            view.setTag(2131886115, Float.valueOf(f3));
        }
    }

    public void animateOverScrollToAmount(float f, boolean z, boolean z2) {
        float currentOverScrollAmount = this.mHostLayout.getCurrentOverScrollAmount(z);
        if (f == currentOverScrollAmount) {
            return;
        }
        cancelOverScrollAnimators(z);
        ValueAnimator ofFloat = ValueAnimator.ofFloat(currentOverScrollAmount, f);
        ofFloat.setDuration(360L);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this, z, z2) { // from class: com.android.systemui.statusbar.stack.StackStateAnimator.12
            final StackStateAnimator this$0;
            final boolean val$isRubberbanded;
            final boolean val$onTop;

            {
                this.this$0 = this;
                this.val$onTop = z;
                this.val$isRubberbanded = z2;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.this$0.mHostLayout.setOverScrollAmount(((Float) valueAnimator.getAnimatedValue()).floatValue(), this.val$onTop, false, false, this.val$isRubberbanded);
            }
        });
        ofFloat.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        ofFloat.addListener(new AnimatorListenerAdapter(this, z) { // from class: com.android.systemui.statusbar.stack.StackStateAnimator.13
            final StackStateAnimator this$0;
            final boolean val$onTop;

            {
                this.this$0 = this;
                this.val$onTop = z;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (this.val$onTop) {
                    this.this$0.mTopOverScrollAnimator = null;
                } else {
                    this.this$0.mBottomOverScrollAnimator = null;
                }
            }
        });
        ofFloat.start();
        if (z) {
            this.mTopOverScrollAnimator = ofFloat;
        } else {
            this.mBottomOverScrollAnimator = ofFloat;
        }
    }

    public void cancelOverScrollAnimators(boolean z) {
        ValueAnimator valueAnimator = z ? this.mTopOverScrollAnimator : this.mBottomOverScrollAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
    }

    public boolean isRunning() {
        return !this.mAnimatorSet.isEmpty();
    }

    public void setHeadsUpAppearHeightBottom(int i) {
        this.mHeadsUpAppearHeightBottom = i;
    }

    public void setShadeExpanded(boolean z) {
        this.mShadeExpanded = z;
    }

    public void startAnimationForEvents(ArrayList<NotificationStackScrollLayout.AnimationEvent> arrayList, StackScrollState stackScrollState, long j) {
        processAnimationEvents(arrayList, stackScrollState);
        int childCount = this.mHostLayout.getChildCount();
        this.mAnimationFilter.applyCombination(this.mNewEvents);
        this.mCurrentAdditionalDelay = j;
        this.mCurrentLength = NotificationStackScrollLayout.AnimationEvent.combineLength(this.mNewEvents);
        this.mCurrentLastNotAddedIndex = findLastNotAddedIndex(stackScrollState);
        for (int i = 0; i < childCount; i++) {
            ExpandableView expandableView = (ExpandableView) this.mHostLayout.getChildAt(i);
            StackViewState viewStateForView = stackScrollState.getViewStateForView(expandableView);
            if (viewStateForView != null && expandableView.getVisibility() != 8 && !applyWithoutAnimation(expandableView, viewStateForView, stackScrollState)) {
                startStackAnimations(expandableView, viewStateForView, stackScrollState, i, -1L);
            }
        }
        if (!isRunning()) {
            onAnimationFinished();
        }
        this.mHeadsUpAppearChildren.clear();
        this.mHeadsUpDisappearChildren.clear();
        this.mNewEvents.clear();
        this.mNewAddChildren.clear();
    }

    /* JADX WARN: Code restructure failed: missing block: B:72:0x01c5, code lost:
        if (r0 != false) goto L67;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void startStackAnimations(ExpandableView expandableView, StackViewState stackViewState, StackScrollState stackScrollState, int i, long j) {
        boolean contains = this.mNewAddChildren.contains(expandableView);
        long j2 = this.mCurrentLength;
        long j3 = j2;
        if (contains) {
            j3 = j2;
            if (this.mAnimationFilter.hasGoToFullShadeEvent) {
                expandableView.setTranslationY(expandableView.getTranslationY() + this.mGoToFullShadeAppearingTranslation);
                j3 = 514 + (100.0f * ((float) Math.pow(stackViewState.notGoneIndex - this.mCurrentLastNotAddedIndex, 0.699999988079071d)));
            }
        }
        boolean z = expandableView.getTranslationY() != stackViewState.yTranslation;
        boolean z2 = expandableView.getTranslationZ() != stackViewState.zTranslation;
        boolean z3 = stackViewState.alpha != expandableView.getAlpha();
        boolean z4 = stackViewState.height != expandableView.getActualHeight();
        boolean z5 = stackViewState.shadowAlpha != expandableView.getShadowAlpha();
        boolean z6 = stackViewState.dark != expandableView.isDark();
        boolean z7 = stackViewState.clipTopAmount != expandableView.getClipTopAmount();
        boolean z8 = this.mAnimationFilter.hasDelays;
        boolean z9 = (z || z2 || z3 || z4 || z7 || z6) ? true : z5;
        if (j == -1) {
            if (!z8 || !z9) {
                j = 0;
            }
            j = this.mCurrentAdditionalDelay + calculateChildAnimationDelay(stackViewState, stackScrollState);
        }
        startViewAnimations(expandableView, stackViewState, j, j3);
        if (z4) {
            startHeightAnimation(expandableView, stackViewState, j3, j);
        } else {
            abortAnimation(expandableView, 2131886112);
        }
        if (z5) {
            startShadowAlphaAnimation(expandableView, stackViewState, j3, j);
        } else {
            abortAnimation(expandableView, 2131886113);
        }
        if (z7) {
            startInsetAnimation(expandableView, stackViewState, j3, j);
        } else {
            abortAnimation(expandableView, 2131886111);
        }
        expandableView.setDimmed(stackViewState.dimmed, this.mAnimationFilter.animateDimmed);
        expandableView.setBelowSpeedBump(stackViewState.belowSpeedBump);
        expandableView.setHideSensitive(stackViewState.hideSensitive, this.mAnimationFilter.animateHideSensitive, j, j3);
        expandableView.setDark(stackViewState.dark, this.mAnimationFilter.animateDark, j);
        if (contains) {
            expandableView.performAddAnimation(j, this.mCurrentLength);
        }
        if (expandableView instanceof ExpandableNotificationRow) {
            ((ExpandableNotificationRow) expandableView).startChildAnimation(stackScrollState, this, j, j3);
        }
    }

    public void startViewAnimations(View view, ViewState viewState, long j, long j2) {
        boolean z = view.getVisibility() == 0;
        float f = viewState.alpha;
        if (!z && ((f != 0.0f || view.getAlpha() != 0.0f) && !viewState.gone && !viewState.hidden)) {
            view.setVisibility(0);
        }
        boolean z2 = view.getTranslationY() != viewState.yTranslation;
        boolean z3 = view.getTranslationZ() != viewState.zTranslation;
        boolean z4 = viewState.alpha != view.getAlpha();
        boolean z5 = z4;
        if (view instanceof ExpandableView) {
            z5 = z4 & (!((ExpandableView) view).willBeGone());
        }
        if (z2) {
            startYTranslationAnimation(view, viewState, j2, j);
        } else {
            abortAnimation(view, 2131886108);
        }
        if (z3) {
            startZTranslationAnimation(view, viewState, j2, j);
        } else {
            abortAnimation(view, 2131886109);
        }
        if (z5 && view.getTranslationX() == 0.0f) {
            startAlphaAnimation(view, viewState, j2, j);
        } else {
            abortAnimation(view, 2131886110);
        }
    }
}
