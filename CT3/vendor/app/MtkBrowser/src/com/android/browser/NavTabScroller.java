package com.android.browser;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.android.browser.view.ScrollerView;
/* loaded from: b.zip:com/android/browser/NavTabScroller.class */
public class NavTabScroller extends ScrollerView {
    static final float[] PULL_FACTOR = {2.5f, 0.9f};
    private BaseAdapter mAdapter;
    private AnimatorSet mAnimator;
    private ContentLayout mContentView;
    DecelerateInterpolator mCubic;
    private float mFlingVelocity;
    private int mGap;
    private ObjectAnimator mGapAnimator;
    private int mGapPosition;
    private OnLayoutListener mLayoutListener;
    private boolean mNeedsScroll;
    int mPullValue;
    private OnRemoveListener mRemoveListener;
    private int mScrollPosition;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: b.zip:com/android/browser/NavTabScroller$ContentLayout.class */
    public static class ContentLayout extends LinearLayout {
        NavTabScroller mScroller;

        public ContentLayout(Context context, NavTabScroller navTabScroller) {
            super(context);
            this.mScroller = navTabScroller;
        }

        @Override // android.widget.LinearLayout, android.view.View
        protected void onMeasure(int i, int i2) {
            View childAt;
            super.onMeasure(i, i2);
            if (this.mScroller.getGap() == 0 || (childAt = getChildAt(0)) == null) {
                return;
            }
            if (this.mScroller.isHorizontal()) {
                setMeasuredDimension(childAt.getMeasuredWidth() + getMeasuredWidth(), getMeasuredHeight());
            } else {
                setMeasuredDimension(getMeasuredWidth(), childAt.getMeasuredHeight() + getMeasuredHeight());
            }
        }
    }

    /* loaded from: b.zip:com/android/browser/NavTabScroller$OnLayoutListener.class */
    interface OnLayoutListener {
        void onLayout(int i, int i2, int i3, int i4);
    }

    /* loaded from: b.zip:com/android/browser/NavTabScroller$OnRemoveListener.class */
    interface OnRemoveListener {
        void onRemovePosition(int i);
    }

    public NavTabScroller(Context context) {
        super(context);
        init(context);
    }

    public NavTabScroller(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    public NavTabScroller(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init(context);
    }

    private void adjustViewGap(View view, int i) {
        if ((this.mGap >= 0 || i <= this.mGapPosition) && (this.mGap <= 0 || i >= this.mGapPosition)) {
            return;
        }
        if (this.mHorizontal) {
            view.setTranslationX(this.mGap);
        } else {
            view.setTranslationY(this.mGap);
        }
    }

    private void animateOut(View view, float f) {
        animateOut(view, f, this.mHorizontal ? view.getTranslationY() : view.getTranslationX());
    }

    private void animateOut(View view, float f, float f2) {
        int height;
        int i;
        int i2;
        if (view == null || this.mAnimator != null) {
            return;
        }
        int indexOfChild = this.mContentView.indexOfChild(view);
        if (f < 0.0f) {
            height = -(this.mHorizontal ? getHeight() : getWidth());
        } else {
            height = this.mHorizontal ? getHeight() : getWidth();
        }
        long abs = (Math.abs(height - (this.mHorizontal ? view.getTop() : view.getLeft())) * 1000) / Math.abs(f);
        int i3 = 0;
        int width = this.mHorizontal ? view.getWidth() : view.getHeight();
        int viewCenter = getViewCenter(view);
        int screenCenter = getScreenCenter();
        if (viewCenter < screenCenter - (width / 2)) {
            i = -((screenCenter - viewCenter) - width);
            i3 = indexOfChild > 0 ? width : 0;
            i2 = indexOfChild;
        } else if (viewCenter > (width / 2) + screenCenter) {
            int i4 = -((screenCenter + width) - viewCenter);
            i2 = -1;
            i = i4;
            if (indexOfChild < this.mAdapter.getCount() - 1) {
                i3 = -width;
                i2 = -1;
                i = i4;
            }
        } else {
            i = -(screenCenter - viewCenter);
            if (indexOfChild < this.mAdapter.getCount() - 1) {
                i3 = -width;
                i2 = -1;
            } else {
                i -= width;
                i2 = -1;
            }
        }
        this.mGapPosition = indexOfChild;
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(view, this.mHorizontal ? TRANSLATION_Y : TRANSLATION_X, f2, height);
        ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(view, ALPHA, getAlpha(view, f2), getAlpha(view, height));
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(ofFloat, ofFloat2);
        animatorSet.setDuration(abs);
        this.mAnimator = new AnimatorSet();
        ObjectAnimator objectAnimator = null;
        ObjectAnimator objectAnimator2 = null;
        if (i != 0) {
            objectAnimator2 = this.mHorizontal ? ObjectAnimator.ofInt(this, "scrollX", getScrollX(), getScrollX() + i) : ObjectAnimator.ofInt(this, "scrollY", getScrollY(), getScrollY() + i);
        }
        if (i3 != 0) {
            objectAnimator = ObjectAnimator.ofInt(this, "gap", 0, i3);
        }
        if (objectAnimator2 != null) {
            if (objectAnimator != null) {
                AnimatorSet animatorSet2 = new AnimatorSet();
                animatorSet2.playTogether(objectAnimator2, objectAnimator);
                animatorSet2.setDuration(200L);
                this.mAnimator.playSequentially(animatorSet, animatorSet2);
            } else {
                objectAnimator2.setDuration(200L);
                this.mAnimator.playSequentially(animatorSet, objectAnimator2);
            }
        } else if (objectAnimator != null) {
            objectAnimator.setDuration(200L);
            this.mAnimator.playSequentially(animatorSet, objectAnimator);
        }
        this.mAnimator.addListener(new AnimatorListenerAdapter(this, indexOfChild, i2) { // from class: com.android.browser.NavTabScroller.2
            final NavTabScroller this$0;
            final int val$pos;
            final int val$position;

            {
                this.this$0 = this;
                this.val$position = indexOfChild;
                this.val$pos = i2;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (this.this$0.mRemoveListener != null) {
                    this.this$0.mRemoveListener.onRemovePosition(this.val$position);
                    this.this$0.mAnimator = null;
                    this.this$0.mGapPosition = -1;
                    this.this$0.mGap = 0;
                    this.this$0.handleDataChanged(this.val$pos);
                }
            }
        });
        this.mAnimator.start();
    }

    private void calcPadding() {
        if (this.mAdapter.getCount() > 0) {
            View childAt = this.mContentView.getChildAt(0);
            if (this.mHorizontal) {
                int measuredWidth = ((getMeasuredWidth() - childAt.getMeasuredWidth()) / 2) + 2;
                this.mContentView.setPadding(measuredWidth, 0, measuredWidth, 0);
                return;
            }
            int measuredHeight = ((getMeasuredHeight() - childAt.getMeasuredHeight()) / 2) + 2;
            this.mContentView.setPadding(0, measuredHeight, 0, measuredHeight);
        }
    }

    private float ease(DecelerateInterpolator decelerateInterpolator, float f, float f2, float f3, float f4) {
        return (decelerateInterpolator.getInterpolation(f / f4) * f3) + f2;
    }

    private float getAlpha(View view, float f) {
        return 1.0f - (Math.abs(f) / (this.mHorizontal ? view.getHeight() : view.getWidth()));
    }

    private int getScreenCenter() {
        return this.mHorizontal ? getScrollX() + (getWidth() / 2) : getScrollY() + (getHeight() / 2);
    }

    private int getViewCenter(View view) {
        return this.mHorizontal ? view.getLeft() + (view.getWidth() / 2) : view.getTop() + (view.getHeight() / 2);
    }

    private void init(Context context) {
        this.mCubic = new DecelerateInterpolator(1.5f);
        this.mGapPosition = -1;
        setHorizontalScrollBarEnabled(false);
        setVerticalScrollBarEnabled(false);
        setLayoutDirection(0);
        this.mContentView = new ContentLayout(context, this);
        this.mContentView.setOrientation(0);
        addView(this.mContentView);
        this.mContentView.setLayoutParams(new FrameLayout.LayoutParams(-2, -1));
        setGap(getGap());
        this.mFlingVelocity = getContext().getResources().getDisplayMetrics().density * 1500.0f;
    }

    private void offsetView(View view, float f) {
        view.setAlpha(getAlpha(view, f));
        if (this.mHorizontal) {
            view.setTranslationY(f);
        } else {
            view.setTranslationX(f);
        }
    }

    void adjustGap() {
        for (int i = 0; i < this.mContentView.getChildCount(); i++) {
            adjustViewGap(this.mContentView.getChildAt(i), i);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void animateOut(View view) {
        if (view == null) {
            return;
        }
        animateOut(view, -this.mFlingVelocity);
    }

    @Override // android.view.View
    public void draw(Canvas canvas) {
        if (this.mGapPosition > -1) {
            adjustGap();
        }
        super.draw(canvas);
    }

    @Override // com.android.browser.view.ScrollerView
    protected View findViewAt(int i, int i2) {
        int i3 = i + this.mScrollX;
        int i4 = i2 + this.mScrollY;
        for (int childCount = this.mContentView.getChildCount() - 1; childCount >= 0; childCount--) {
            View childAt = this.mContentView.getChildAt(childCount);
            if (childAt.getVisibility() == 0 && i3 >= childAt.getLeft() && i3 < childAt.getRight() && i4 >= childAt.getTop() && i4 < childAt.getBottom()) {
                return childAt;
            }
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void finishScroller() {
        this.mScroller.forceFinished(true);
    }

    public int getGap() {
        return this.mGap;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public int getScrollValue() {
        return this.mHorizontal ? this.mScrollX : this.mScrollY;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public NavTabView getTabView(int i) {
        return (NavTabView) this.mContentView.getChildAt(i);
    }

    protected void handleDataChanged() {
        handleDataChanged(-1);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void handleDataChanged(int i) {
        int scrollValue = getScrollValue();
        if (this.mGapAnimator != null) {
            this.mGapAnimator.cancel();
        }
        this.mContentView.removeAllViews();
        for (int i2 = 0; i2 < this.mAdapter.getCount(); i2++) {
            View view = this.mAdapter.getView(i2, null, this.mContentView);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
            layoutParams.gravity = this.mHorizontal ? 16 : 1;
            this.mContentView.addView(view, layoutParams);
            if (this.mGapPosition > -1) {
                adjustViewGap(view, i2);
            }
        }
        if (i <= -1) {
            setScrollValue(scrollValue);
            return;
        }
        int min = Math.min(this.mAdapter.getCount() - 1, i);
        this.mNeedsScroll = true;
        this.mScrollPosition = min;
        requestLayout();
    }

    protected boolean isHorizontal() {
        return this.mHorizontal;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.browser.view.ScrollerView, android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        if (this.mNeedsScroll) {
            this.mScroller.forceFinished(true);
            snapToSelected(this.mScrollPosition, false);
            this.mNeedsScroll = false;
        }
        if (this.mLayoutListener != null) {
            this.mLayoutListener.onLayout(i, i2, i3, i4);
            this.mLayoutListener = null;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.browser.view.ScrollerView, android.widget.FrameLayout, android.view.View
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        calcPadding();
    }

    @Override // com.android.browser.view.ScrollerView
    protected void onOrthoDrag(View view, float f) {
        if (view == null || this.mAnimator != null) {
            return;
        }
        offsetView(view, f);
    }

    @Override // com.android.browser.view.ScrollerView
    protected void onOrthoDragFinished(View view) {
        if (this.mAnimator == null && this.mIsOrthoDragged && view != null) {
            float translationY = this.mHorizontal ? view.getTranslationY() : view.getTranslationX();
            if (Math.abs(translationY) > (this.mHorizontal ? view.getHeight() : view.getWidth()) / 2) {
                animateOut(view, Math.signum(translationY) * this.mFlingVelocity, translationY);
            } else {
                offsetView(view, 0.0f);
            }
        }
    }

    @Override // com.android.browser.view.ScrollerView
    protected void onOrthoFling(View view, float f) {
        if (view == null) {
            return;
        }
        if (this.mAnimator != null || Math.abs(f) <= this.mFlingVelocity / 2.0f) {
            offsetView(view, 0.0f);
        } else {
            animateOut(view, f);
        }
    }

    @Override // com.android.browser.view.ScrollerView
    protected void onPull(int i) {
        boolean z;
        if (i == 0 && this.mPullValue == 0) {
            return;
        }
        if (i != 0 || this.mPullValue == 0) {
            z = this.mPullValue == 0;
            this.mPullValue += i;
        } else {
            for (int i2 = 0; i2 < 2; i2++) {
                View childAt = this.mContentView.getChildAt(this.mPullValue < 0 ? i2 : (this.mContentView.getChildCount() - 1) - i2);
                if (childAt == null) {
                    break;
                }
                ObjectAnimator ofFloat = ObjectAnimator.ofFloat(childAt, this.mHorizontal ? "translationX" : "translationY", this.mHorizontal ? getTranslationX() : getTranslationY(), 0.0f);
                ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(childAt, this.mHorizontal ? "rotationY" : "rotationX", this.mHorizontal ? getRotationY() : getRotationX(), 0.0f);
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(ofFloat, ofFloat2);
                animatorSet.setDuration(100L);
                animatorSet.start();
            }
            this.mPullValue = 0;
            z = false;
        }
        int width = this.mHorizontal ? getWidth() : getHeight();
        int abs = Math.abs(this.mPullValue);
        int i3 = this.mPullValue <= 0 ? 1 : -1;
        for (int i4 = 0; i4 < 2; i4++) {
            View childAt2 = this.mContentView.getChildAt(this.mPullValue < 0 ? i4 : (this.mContentView.getChildCount() - 1) - i4);
            if (childAt2 == null) {
                return;
            }
            if (z) {
            }
            float f = PULL_FACTOR[i4];
            float ease = (-i3) * ease(this.mCubic, abs, 0.0f, f * 2.0f, width);
            int ease2 = i3 * ((int) ease(this.mCubic, abs, 0.0f, f * 20.0f, width));
            if (this.mHorizontal) {
                childAt2.setTranslationX(ease2);
            } else {
                childAt2.setTranslationY(ease2);
            }
            if (this.mHorizontal) {
                childAt2.setRotationY(-ease);
            } else {
                childAt2.setRotationX(ease);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setAdapter(BaseAdapter baseAdapter, int i) {
        this.mAdapter = baseAdapter;
        this.mAdapter.registerDataSetObserver(new DataSetObserver(this) { // from class: com.android.browser.NavTabScroller.1
            final NavTabScroller this$0;

            {
                this.this$0 = this;
            }

            @Override // android.database.DataSetObserver
            public void onChanged() {
                super.onChanged();
                this.this$0.handleDataChanged();
            }

            @Override // android.database.DataSetObserver
            public void onInvalidated() {
                super.onInvalidated();
            }
        });
        handleDataChanged(i);
    }

    public void setGap(int i) {
        if (this.mGapPosition != -1) {
            this.mGap = i;
            postInvalidate();
        }
    }

    public void setOnLayoutListener(OnLayoutListener onLayoutListener) {
        this.mLayoutListener = onLayoutListener;
    }

    public void setOnRemoveListener(OnRemoveListener onRemoveListener) {
        this.mRemoveListener = onRemoveListener;
    }

    @Override // com.android.browser.view.ScrollerView
    public void setOrientation(int i) {
        this.mContentView.setOrientation(i);
        if (i == 0) {
            this.mContentView.setLayoutParams(new FrameLayout.LayoutParams(-2, -1));
        } else {
            this.mContentView.setLayoutParams(new FrameLayout.LayoutParams(-1, -2));
        }
        super.setOrientation(i);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setScrollValue(int i) {
        int i2 = this.mHorizontal ? i : 0;
        if (this.mHorizontal) {
            i = 0;
        }
        scrollTo(i2, i);
    }

    void snapToSelected(int i, boolean z) {
        View childAt;
        if (i >= 0 && (childAt = this.mContentView.getChildAt(i)) != null) {
            int i2 = 0;
            int i3 = 0;
            if (this.mHorizontal) {
                i2 = ((childAt.getLeft() + childAt.getRight()) - getWidth()) / 2;
            } else {
                i3 = ((childAt.getTop() + childAt.getBottom()) - getHeight()) / 2;
            }
            if (i2 == this.mScrollX && i3 == this.mScrollY) {
                return;
            }
            if (z) {
                smoothScrollTo(i2, i3);
            } else {
                scrollTo(i2, i3);
            }
        }
    }
}
