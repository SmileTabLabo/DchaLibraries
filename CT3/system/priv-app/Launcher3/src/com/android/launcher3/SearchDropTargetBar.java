package com.android.launcher3;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import com.android.launcher3.DragController;
/* loaded from: a.zip:com/android/launcher3/SearchDropTargetBar.class */
public class SearchDropTargetBar extends FrameLayout implements DragController.DragListener {
    boolean mAccessibilityEnabled;
    private AnimatorSet mCurrentAnimation;
    private boolean mDeferOnDragEnd;
    private ButtonDropTarget mDeleteDropTarget;
    View mDropTargetBar;
    private ButtonDropTarget mInfoDropTarget;
    View mQSB;
    private State mState;
    private ButtonDropTarget mUninstallDropTarget;
    private static final TimeInterpolator MOVE_DOWN_INTERPOLATOR = new DecelerateInterpolator(0.6f);
    private static final TimeInterpolator MOVE_UP_INTERPOLATOR = new DecelerateInterpolator(1.5f);
    private static final TimeInterpolator DEFAULT_INTERPOLATOR = new AccelerateInterpolator();
    private static int DEFAULT_DRAG_FADE_DURATION = 175;

    /* loaded from: a.zip:com/android/launcher3/SearchDropTargetBar$State.class */
    public enum State {
        INVISIBLE(0.0f, 0.0f, 0.0f),
        INVISIBLE_TRANSLATED(0.0f, 0.0f, -1.0f),
        SEARCH_BAR(1.0f, 0.0f, 0.0f),
        DROP_TARGET(0.0f, 1.0f, 0.0f);
        
        private final float mDropTargetBarAlpha;
        private final float mSearchBarAlpha;
        private final float mTranslation;

        State(float f, float f2, float f3) {
            this.mSearchBarAlpha = f;
            this.mDropTargetBarAlpha = f2;
            this.mTranslation = f3;
        }

        /* renamed from: values  reason: to resolve conflict with enum method */
        public static State[] valuesCustom() {
            return values();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/launcher3/SearchDropTargetBar$ViewVisiblilyUpdateHandler.class */
    public class ViewVisiblilyUpdateHandler extends AnimatorListenerAdapter {
        private final View mView;
        final SearchDropTargetBar this$0;

        ViewVisiblilyUpdateHandler(SearchDropTargetBar searchDropTargetBar, View view) {
            this.this$0 = searchDropTargetBar;
            this.mView = view;
        }

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animator) {
            AlphaUpdateListener.updateVisibility(this.mView, this.this$0.mAccessibilityEnabled);
        }

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationStart(Animator animator) {
            this.mView.setVisibility(0);
        }
    }

    public SearchDropTargetBar(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public SearchDropTargetBar(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mState = State.SEARCH_BAR;
        this.mDeferOnDragEnd = false;
        this.mAccessibilityEnabled = false;
    }

    private void animateAlpha(View view, float f, TimeInterpolator timeInterpolator) {
        if (Float.compare(view.getAlpha(), f) != 0) {
            ObjectAnimator ofFloat = ObjectAnimator.ofFloat(view, View.ALPHA, f);
            ofFloat.setInterpolator(timeInterpolator);
            ofFloat.addListener(new ViewVisiblilyUpdateHandler(this, view));
            this.mCurrentAnimation.play(ofFloat);
        }
    }

    public void animateToState(State state, int i) {
        animateToState(state, i, null);
    }

    public void animateToState(State state, int i, AnimatorSet animatorSet) {
        if (this.mState != state) {
            this.mState = state;
            this.mAccessibilityEnabled = ((AccessibilityManager) getContext().getSystemService("accessibility")).isEnabled();
            if (this.mCurrentAnimation != null) {
                this.mCurrentAnimation.cancel();
                this.mCurrentAnimation = null;
            }
            this.mCurrentAnimation = null;
            if (i > 0) {
                this.mCurrentAnimation = new AnimatorSet();
                this.mCurrentAnimation.setDuration(i);
                animateAlpha(this.mDropTargetBar, this.mState.mDropTargetBarAlpha, DEFAULT_INTERPOLATOR);
            } else {
                this.mDropTargetBar.setAlpha(this.mState.mDropTargetBarAlpha);
                AlphaUpdateListener.updateVisibility(this.mDropTargetBar, this.mAccessibilityEnabled);
            }
            if (this.mQSB != null) {
                float measuredHeight = ((Launcher) getContext()).getDeviceProfile().isVerticalBarLayout() ? 0.0f : this.mState.mTranslation * getMeasuredHeight();
                if (i > 0) {
                    int compare = Float.compare(this.mQSB.getTranslationY(), measuredHeight);
                    animateAlpha(this.mQSB, this.mState.mSearchBarAlpha, compare == 0 ? DEFAULT_INTERPOLATOR : compare < 0 ? MOVE_DOWN_INTERPOLATOR : MOVE_UP_INTERPOLATOR);
                    if (compare != 0) {
                        this.mCurrentAnimation.play(ObjectAnimator.ofFloat(this.mQSB, View.TRANSLATION_Y, measuredHeight));
                    }
                } else {
                    this.mQSB.setTranslationY(measuredHeight);
                    this.mQSB.setAlpha(this.mState.mSearchBarAlpha);
                    AlphaUpdateListener.updateVisibility(this.mQSB, this.mAccessibilityEnabled);
                }
            }
            if (i > 0) {
                if (animatorSet != null) {
                    animatorSet.play(this.mCurrentAnimation);
                } else {
                    this.mCurrentAnimation.start();
                }
            }
        }
    }

    public void deferOnDragEnd() {
        this.mDeferOnDragEnd = true;
    }

    public void enableAccessibleDrag(boolean z) {
        if (this.mQSB != null) {
            this.mQSB.setVisibility(z ? 8 : 0);
        }
        this.mInfoDropTarget.enableAccessibleDrag(z);
        this.mDeleteDropTarget.enableAccessibleDrag(z);
        this.mUninstallDropTarget.enableAccessibleDrag(z);
    }

    public Rect getSearchBarBounds() {
        if (this.mQSB != null) {
            int[] iArr = new int[2];
            this.mQSB.getLocationOnScreen(iArr);
            Rect rect = new Rect();
            rect.left = iArr[0];
            rect.top = iArr[1];
            rect.right = iArr[0] + this.mQSB.getWidth();
            rect.bottom = iArr[1] + this.mQSB.getHeight();
            return rect;
        }
        return null;
    }

    @Override // com.android.launcher3.DragController.DragListener
    public void onDragEnd() {
        if (this.mDeferOnDragEnd) {
            this.mDeferOnDragEnd = false;
        } else {
            animateToState(State.SEARCH_BAR, DEFAULT_DRAG_FADE_DURATION);
        }
    }

    @Override // com.android.launcher3.DragController.DragListener
    public void onDragStart(DragSource dragSource, Object obj, int i) {
        animateToState(State.DROP_TARGET, DEFAULT_DRAG_FADE_DURATION);
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mDropTargetBar = findViewById(2131296307);
        this.mInfoDropTarget = (ButtonDropTarget) this.mDropTargetBar.findViewById(2131296309);
        this.mDeleteDropTarget = (ButtonDropTarget) this.mDropTargetBar.findViewById(2131296308);
        this.mUninstallDropTarget = (ButtonDropTarget) this.mDropTargetBar.findViewById(2131296310);
        this.mInfoDropTarget.setSearchDropTargetBar(this);
        this.mDeleteDropTarget.setSearchDropTargetBar(this);
        this.mUninstallDropTarget.setSearchDropTargetBar(this);
        this.mDropTargetBar.setAlpha(0.0f);
        AlphaUpdateListener.updateVisibility(this.mDropTargetBar, this.mAccessibilityEnabled);
    }

    public void setQsbSearchBar(View view) {
        this.mQSB = view;
    }

    public void setup(Launcher launcher, DragController dragController) {
        dragController.addDragListener(this);
        dragController.setFlingToDeleteDropTarget(this.mDeleteDropTarget);
        dragController.addDragListener(this.mInfoDropTarget);
        dragController.addDragListener(this.mDeleteDropTarget);
        dragController.addDragListener(this.mUninstallDropTarget);
        dragController.addDropTarget(this.mInfoDropTarget);
        dragController.addDropTarget(this.mDeleteDropTarget);
        dragController.addDropTarget(this.mUninstallDropTarget);
        this.mInfoDropTarget.setLauncher(launcher);
        this.mDeleteDropTarget.setLauncher(launcher);
        this.mUninstallDropTarget.setLauncher(launcher);
    }
}
