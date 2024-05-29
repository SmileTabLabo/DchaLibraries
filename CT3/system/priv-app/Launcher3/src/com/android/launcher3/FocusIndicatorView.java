package com.android.launcher3;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
/* loaded from: a.zip:com/android/launcher3/FocusIndicatorView.class */
public class FocusIndicatorView extends View implements View.OnFocusChangeListener {
    private ObjectAnimator mCurrentAnimation;
    private final View.OnFocusChangeListener mHideIndicatorOnFocusListener;
    private final int[] mIndicatorPos;
    private boolean mInitiated;
    private View mLastFocusedView;
    private Pair<View, Boolean> mPendingCall;
    private ViewAnimState mTargetState;
    private final int[] mTargetViewPos;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:com/android/launcher3/FocusIndicatorView$ViewAnimState.class */
    public static final class ViewAnimState {
        float scaleX;
        float scaleY;
        float x;
        float y;

        ViewAnimState() {
        }
    }

    public FocusIndicatorView(Context context) {
        this(context, null);
    }

    public FocusIndicatorView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mIndicatorPos = new int[2];
        this.mTargetViewPos = new int[2];
        setAlpha(0.0f);
        setBackgroundColor(getResources().getColor(2131361796));
        this.mHideIndicatorOnFocusListener = new View.OnFocusChangeListener(this) { // from class: com.android.launcher3.FocusIndicatorView.1
            final FocusIndicatorView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnFocusChangeListener
            public void onFocusChange(View view, boolean z) {
                if (z) {
                    this.this$0.endCurrentAnimation();
                    this.this$0.setAlpha(0.0f);
                }
            }
        };
    }

    private void applyState(ViewAnimState viewAnimState) {
        setTranslationX(viewAnimState.x);
        setTranslationY(viewAnimState.y);
        setScaleX(viewAnimState.scaleX);
        setScaleY(viewAnimState.scaleY);
    }

    private static void computeLocationRelativeToParent(View view, View view2, int[] iArr) {
        iArr[1] = 0;
        iArr[0] = 0;
        computeLocationRelativeToParentHelper(view, view2, iArr);
        iArr[0] = (int) (iArr[0] + (((1.0f - view.getScaleX()) * view.getWidth()) / 2.0f));
        iArr[1] = (int) (iArr[1] + (((1.0f - view.getScaleY()) * view.getHeight()) / 2.0f));
    }

    private static void computeLocationRelativeToParentHelper(View view, View view2, int[] iArr) {
        View view3 = (View) view.getParent();
        iArr[0] = iArr[0] + view.getLeft();
        iArr[1] = iArr[1] + view.getTop();
        if (view3 instanceof PagedView) {
            PagedView pagedView = (PagedView) view3;
            iArr[0] = iArr[0] - pagedView.getScrollForPage(pagedView.indexOfChild(view));
        }
        if (view3 != view2) {
            computeLocationRelativeToParentHelper(view3, view2, iArr);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void endCurrentAnimation() {
        if (this.mCurrentAnimation != null) {
            this.mCurrentAnimation.cancel();
            this.mCurrentAnimation = null;
        }
        if (this.mTargetState != null) {
            applyState(this.mTargetState);
            this.mTargetState = null;
        }
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        if (this.mPendingCall != null) {
            onFocusChange((View) this.mPendingCall.first, ((Boolean) this.mPendingCall.second).booleanValue());
        }
    }

    @Override // android.view.View.OnFocusChangeListener
    public void onFocusChange(View view, boolean z) {
        this.mPendingCall = null;
        if (!this.mInitiated && getWidth() == 0) {
            this.mPendingCall = Pair.create(view, Boolean.valueOf(z));
            invalidate();
            return;
        }
        if (!this.mInitiated) {
            computeLocationRelativeToParent(this, (View) getParent(), this.mIndicatorPos);
            this.mInitiated = true;
        }
        if (z) {
            int width = getWidth();
            int height = getHeight();
            endCurrentAnimation();
            ViewAnimState viewAnimState = new ViewAnimState();
            viewAnimState.scaleX = (view.getScaleX() * view.getWidth()) / width;
            viewAnimState.scaleY = (view.getScaleY() * view.getHeight()) / height;
            computeLocationRelativeToParent(view, (View) getParent(), this.mTargetViewPos);
            viewAnimState.x = (this.mTargetViewPos[0] - this.mIndicatorPos[0]) - (((1.0f - viewAnimState.scaleX) * width) / 2.0f);
            viewAnimState.y = (this.mTargetViewPos[1] - this.mIndicatorPos[1]) - (((1.0f - viewAnimState.scaleY) * height) / 2.0f);
            if (getAlpha() > 0.2f) {
                this.mTargetState = viewAnimState;
                this.mCurrentAnimation = LauncherAnimUtils.ofPropertyValuesHolder(this, PropertyValuesHolder.ofFloat(View.ALPHA, 1.0f), PropertyValuesHolder.ofFloat(View.TRANSLATION_X, this.mTargetState.x), PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, this.mTargetState.y), PropertyValuesHolder.ofFloat(View.SCALE_X, this.mTargetState.scaleX), PropertyValuesHolder.ofFloat(View.SCALE_Y, this.mTargetState.scaleY));
            } else {
                applyState(viewAnimState);
                this.mCurrentAnimation = LauncherAnimUtils.ofPropertyValuesHolder(this, PropertyValuesHolder.ofFloat(View.ALPHA, 1.0f));
            }
            this.mLastFocusedView = view;
        } else if (this.mLastFocusedView == view) {
            this.mLastFocusedView = null;
            endCurrentAnimation();
            this.mCurrentAnimation = LauncherAnimUtils.ofPropertyValuesHolder(this, PropertyValuesHolder.ofFloat(View.ALPHA, 0.0f));
        }
        if (this.mCurrentAnimation != null) {
            this.mCurrentAnimation.setDuration(150L).start();
        }
    }

    @Override // android.view.View
    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        if (this.mLastFocusedView != null) {
            this.mPendingCall = Pair.create(this.mLastFocusedView, Boolean.TRUE);
            invalidate();
        }
    }
}
