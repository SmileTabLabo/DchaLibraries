package com.android.systemui.statusbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.Interpolators;
import com.android.systemui.statusbar.notification.TransformState;
import java.util.Stack;
/* loaded from: a.zip:com/android/systemui/statusbar/ViewTransformationHelper.class */
public class ViewTransformationHelper implements TransformableView {
    private ValueAnimator mViewTransformationAnimation;
    private ArrayMap<Integer, View> mTransformedViews = new ArrayMap<>();
    private ArrayMap<Integer, CustomTransformation> mCustomTransformations = new ArrayMap<>();

    /* loaded from: a.zip:com/android/systemui/statusbar/ViewTransformationHelper$CustomTransformation.class */
    public static abstract class CustomTransformation {
        public boolean customTransformTarget(TransformState transformState, TransformState transformState2) {
            return false;
        }

        public boolean initTransformation(TransformState transformState, TransformState transformState2) {
            return false;
        }

        public abstract boolean transformFrom(TransformState transformState, TransformableView transformableView, float f);

        public abstract boolean transformTo(TransformState transformState, TransformableView transformableView, float f);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void abortTransformations() {
        for (Integer num : this.mTransformedViews.keySet()) {
            TransformState currentState = getCurrentState(num.intValue());
            if (currentState != null) {
                currentState.abortTransformation();
                currentState.recycle();
            }
        }
    }

    public void addRemainingTransformTypes(View view) {
        int id;
        int size = this.mTransformedViews.size();
        for (int i = 0; i < size; i++) {
            Object valueAt = this.mTransformedViews.valueAt(i);
            while (true) {
                View view2 = (View) valueAt;
                if (view2 != view.getParent()) {
                    view2.setTag(2131886147, true);
                    valueAt = view2.getParent();
                }
            }
        }
        Stack stack = new Stack();
        stack.push(view);
        while (!stack.isEmpty()) {
            View view3 = (View) stack.pop();
            if (view3.getVisibility() != 8) {
                if (((Boolean) view3.getTag(2131886147)) != null || (id = view3.getId()) == -1) {
                    view3.setTag(2131886147, null);
                    if ((view3 instanceof ViewGroup) && !this.mTransformedViews.containsValue(view3)) {
                        ViewGroup viewGroup = (ViewGroup) view3;
                        for (int i2 = 0; i2 < viewGroup.getChildCount(); i2++) {
                            stack.push(viewGroup.getChildAt(i2));
                        }
                    }
                } else {
                    addTransformedView(id, view3);
                }
            }
        }
    }

    public void addTransformedView(int i, View view) {
        this.mTransformedViews.put(Integer.valueOf(i), view);
    }

    public ArraySet<View> getAllTransformingViews() {
        return new ArraySet<>(this.mTransformedViews.values());
    }

    @Override // com.android.systemui.statusbar.TransformableView
    public TransformState getCurrentState(int i) {
        View view = this.mTransformedViews.get(Integer.valueOf(i));
        if (view == null || view.getVisibility() == 8) {
            return null;
        }
        return TransformState.createFrom(view);
    }

    public void reset() {
        this.mTransformedViews.clear();
    }

    public void resetTransformedView(View view) {
        TransformState createFrom = TransformState.createFrom(view);
        createFrom.setVisible(true, true);
        createFrom.recycle();
    }

    public void setCustomTransformation(CustomTransformation customTransformation, int i) {
        this.mCustomTransformations.put(Integer.valueOf(i), customTransformation);
    }

    @Override // com.android.systemui.statusbar.TransformableView
    public void setVisible(boolean z) {
        if (this.mViewTransformationAnimation != null) {
            this.mViewTransformationAnimation.cancel();
        }
        for (Integer num : this.mTransformedViews.keySet()) {
            TransformState currentState = getCurrentState(num.intValue());
            if (currentState != null) {
                currentState.setVisible(z, false);
                currentState.recycle();
            }
        }
    }

    @Override // com.android.systemui.statusbar.TransformableView
    public void transformFrom(TransformableView transformableView) {
        if (this.mViewTransformationAnimation != null) {
            this.mViewTransformationAnimation.cancel();
        }
        this.mViewTransformationAnimation = ValueAnimator.ofFloat(0.0f, 1.0f);
        this.mViewTransformationAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this, transformableView) { // from class: com.android.systemui.statusbar.ViewTransformationHelper.3
            final ViewTransformationHelper this$0;
            final TransformableView val$notification;

            {
                this.this$0 = this;
                this.val$notification = transformableView;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.this$0.transformFrom(this.val$notification, valueAnimator.getAnimatedFraction());
            }
        });
        this.mViewTransformationAnimation.addListener(new AnimatorListenerAdapter(this) { // from class: com.android.systemui.statusbar.ViewTransformationHelper.4
            public boolean mCancelled;
            final ViewTransformationHelper this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                this.mCancelled = true;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (this.mCancelled) {
                    this.this$0.abortTransformations();
                } else {
                    this.this$0.setVisible(true);
                }
            }
        });
        this.mViewTransformationAnimation.setInterpolator(Interpolators.LINEAR);
        this.mViewTransformationAnimation.setDuration(360L);
        this.mViewTransformationAnimation.start();
    }

    @Override // com.android.systemui.statusbar.TransformableView
    public void transformFrom(TransformableView transformableView, float f) {
        for (Integer num : this.mTransformedViews.keySet()) {
            TransformState currentState = getCurrentState(num.intValue());
            if (currentState != null) {
                CustomTransformation customTransformation = this.mCustomTransformations.get(num);
                if (customTransformation == null || !customTransformation.transformFrom(currentState, transformableView, f)) {
                    TransformState currentState2 = transformableView.getCurrentState(num.intValue());
                    if (currentState2 != null) {
                        currentState.transformViewFrom(currentState2, f);
                        currentState2.recycle();
                    } else {
                        if (f == 0.0f) {
                            currentState.prepareFadeIn();
                        }
                        CrossFadeHelper.fadeIn(this.mTransformedViews.get(num), f);
                    }
                    currentState.recycle();
                } else {
                    currentState.recycle();
                }
            }
        }
    }

    @Override // com.android.systemui.statusbar.TransformableView
    public void transformTo(TransformableView transformableView, float f) {
        for (Integer num : this.mTransformedViews.keySet()) {
            TransformState currentState = getCurrentState(num.intValue());
            if (currentState != null) {
                CustomTransformation customTransformation = this.mCustomTransformations.get(num);
                if (customTransformation == null || !customTransformation.transformTo(currentState, transformableView, f)) {
                    TransformState currentState2 = transformableView.getCurrentState(num.intValue());
                    if (currentState2 != null) {
                        currentState.transformViewTo(currentState2, f);
                        currentState2.recycle();
                    } else {
                        CrossFadeHelper.fadeOut(this.mTransformedViews.get(num), f);
                    }
                    currentState.recycle();
                } else {
                    currentState.recycle();
                }
            }
        }
    }

    @Override // com.android.systemui.statusbar.TransformableView
    public void transformTo(TransformableView transformableView, Runnable runnable) {
        if (this.mViewTransformationAnimation != null) {
            this.mViewTransformationAnimation.cancel();
        }
        this.mViewTransformationAnimation = ValueAnimator.ofFloat(0.0f, 1.0f);
        this.mViewTransformationAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this, transformableView) { // from class: com.android.systemui.statusbar.ViewTransformationHelper.1
            final ViewTransformationHelper this$0;
            final TransformableView val$notification;

            {
                this.this$0 = this;
                this.val$notification = transformableView;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.this$0.transformTo(this.val$notification, valueAnimator.getAnimatedFraction());
            }
        });
        this.mViewTransformationAnimation.setInterpolator(Interpolators.LINEAR);
        this.mViewTransformationAnimation.setDuration(360L);
        this.mViewTransformationAnimation.addListener(new AnimatorListenerAdapter(this, runnable) { // from class: com.android.systemui.statusbar.ViewTransformationHelper.2
            public boolean mCancelled;
            final ViewTransformationHelper this$0;
            final Runnable val$endRunnable;

            {
                this.this$0 = this;
                this.val$endRunnable = runnable;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                this.mCancelled = true;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (this.mCancelled) {
                    this.this$0.abortTransformations();
                    return;
                }
                if (this.val$endRunnable != null) {
                    this.val$endRunnable.run();
                }
                this.this$0.setVisible(false);
            }
        });
        this.mViewTransformationAnimation.start();
    }
}
