package android.support.v17.leanback.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.R$attr;
import android.support.v17.leanback.R$integer;
import android.support.v17.leanback.R$styleable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import java.util.ArrayList;
/* loaded from: a.zip:android/support/v17/leanback/widget/BaseCardView.class */
public class BaseCardView extends FrameLayout {
    private static final int[] LB_PRESSED_STATE_SET = {16842919};
    private final int mActivatedAnimDuration;
    private Animation mAnim;
    private final Runnable mAnimationTrigger;
    private int mCardType;
    private boolean mDelaySelectedAnim;
    private ArrayList<View> mExtraViewList;
    private int mExtraVisibility;
    private float mInfoAlpha;
    private float mInfoOffset;
    private ArrayList<View> mInfoViewList;
    private float mInfoVisFraction;
    private int mInfoVisibility;
    private ArrayList<View> mMainViewList;
    private int mMeasuredHeight;
    private int mMeasuredWidth;
    private final int mSelectedAnimDuration;
    private int mSelectedAnimationDelay;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:android/support/v17/leanback/widget/BaseCardView$InfoAlphaAnimation.class */
    public class InfoAlphaAnimation extends Animation {
        private float mDelta;
        private float mStartValue;
        final BaseCardView this$0;

        public InfoAlphaAnimation(BaseCardView baseCardView, float f, float f2) {
            this.this$0 = baseCardView;
            this.mStartValue = f;
            this.mDelta = f2 - f;
        }

        @Override // android.view.animation.Animation
        protected void applyTransformation(float f, Transformation transformation) {
            this.this$0.mInfoAlpha = this.mStartValue + (this.mDelta * f);
            for (int i = 0; i < this.this$0.mInfoViewList.size(); i++) {
                ((View) this.this$0.mInfoViewList.get(i)).setAlpha(this.this$0.mInfoAlpha);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:android/support/v17/leanback/widget/BaseCardView$InfoHeightAnimation.class */
    public class InfoHeightAnimation extends Animation {
        private float mDelta;
        private float mStartValue;
        final BaseCardView this$0;

        public InfoHeightAnimation(BaseCardView baseCardView, float f, float f2) {
            this.this$0 = baseCardView;
            this.mStartValue = f;
            this.mDelta = f2 - f;
        }

        @Override // android.view.animation.Animation
        protected void applyTransformation(float f, Transformation transformation) {
            this.this$0.mInfoVisFraction = this.mStartValue + (this.mDelta * f);
            this.this$0.requestLayout();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:android/support/v17/leanback/widget/BaseCardView$InfoOffsetAnimation.class */
    public class InfoOffsetAnimation extends Animation {
        private float mDelta;
        private float mStartValue;
        final BaseCardView this$0;

        public InfoOffsetAnimation(BaseCardView baseCardView, float f, float f2) {
            this.this$0 = baseCardView;
            this.mStartValue = f;
            this.mDelta = f2 - f;
        }

        @Override // android.view.animation.Animation
        protected void applyTransformation(float f, Transformation transformation) {
            this.this$0.mInfoOffset = this.mStartValue + (this.mDelta * f);
            this.this$0.requestLayout();
        }
    }

    /* loaded from: a.zip:android/support/v17/leanback/widget/BaseCardView$LayoutParams.class */
    public static class LayoutParams extends FrameLayout.LayoutParams {
        @ViewDebug.ExportedProperty(category = "layout", mapping = {@ViewDebug.IntToString(from = 0, to = "MAIN"), @ViewDebug.IntToString(from = 1, to = "INFO"), @ViewDebug.IntToString(from = 2, to = "EXTRA")})
        public int viewType;

        public LayoutParams(int i, int i2) {
            super(i, i2);
            this.viewType = 0;
        }

        public LayoutParams(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            this.viewType = 0;
            TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.lbBaseCardView_Layout);
            this.viewType = obtainStyledAttributes.getInt(R$styleable.lbBaseCardView_Layout_layout_viewType, 0);
            obtainStyledAttributes.recycle();
        }

        public LayoutParams(LayoutParams layoutParams) {
            super((ViewGroup.MarginLayoutParams) layoutParams);
            this.viewType = 0;
            this.viewType = layoutParams.viewType;
        }

        public LayoutParams(ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
            this.viewType = 0;
        }
    }

    public BaseCardView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R$attr.baseCardViewStyle);
    }

    public BaseCardView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mInfoAlpha = 1.0f;
        this.mAnimationTrigger = new Runnable(this) { // from class: android.support.v17.leanback.widget.BaseCardView.1
            final BaseCardView this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.animateInfoOffset(true);
            }
        };
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.lbBaseCardView, i, 0);
        try {
            this.mCardType = obtainStyledAttributes.getInteger(R$styleable.lbBaseCardView_cardType, 0);
            Drawable drawable = obtainStyledAttributes.getDrawable(R$styleable.lbBaseCardView_cardForeground);
            if (drawable != null) {
                setForeground(drawable);
            }
            Drawable drawable2 = obtainStyledAttributes.getDrawable(R$styleable.lbBaseCardView_cardBackground);
            if (drawable2 != null) {
                setBackground(drawable2);
            }
            this.mInfoVisibility = obtainStyledAttributes.getInteger(R$styleable.lbBaseCardView_infoVisibility, 1);
            this.mExtraVisibility = obtainStyledAttributes.getInteger(R$styleable.lbBaseCardView_extraVisibility, 2);
            if (this.mExtraVisibility < this.mInfoVisibility) {
                this.mExtraVisibility = this.mInfoVisibility;
            }
            this.mSelectedAnimationDelay = obtainStyledAttributes.getInteger(R$styleable.lbBaseCardView_selectedAnimationDelay, getResources().getInteger(R$integer.lb_card_selected_animation_delay));
            this.mSelectedAnimDuration = obtainStyledAttributes.getInteger(R$styleable.lbBaseCardView_selectedAnimationDuration, getResources().getInteger(R$integer.lb_card_selected_animation_duration));
            this.mActivatedAnimDuration = obtainStyledAttributes.getInteger(R$styleable.lbBaseCardView_activatedAnimationDuration, getResources().getInteger(R$integer.lb_card_activated_animation_duration));
            obtainStyledAttributes.recycle();
            this.mDelaySelectedAnim = true;
            this.mMainViewList = new ArrayList<>();
            this.mInfoViewList = new ArrayList<>();
            this.mExtraViewList = new ArrayList<>();
            this.mInfoOffset = 0.0f;
            this.mInfoVisFraction = 0.0f;
        } catch (Throwable th) {
            obtainStyledAttributes.recycle();
            throw th;
        }
    }

    private void animateInfoAlpha(boolean z) {
        cancelAnimations();
        if (z) {
            for (int i = 0; i < this.mInfoViewList.size(); i++) {
                this.mInfoViewList.get(i).setVisibility(0);
            }
        }
        this.mAnim = new InfoAlphaAnimation(this, this.mInfoAlpha, z ? 1.0f : 0.0f);
        this.mAnim.setDuration(this.mActivatedAnimDuration);
        this.mAnim.setInterpolator(new DecelerateInterpolator());
        this.mAnim.setAnimationListener(new Animation.AnimationListener(this) { // from class: android.support.v17.leanback.widget.BaseCardView.4
            final BaseCardView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.animation.Animation.AnimationListener
            public void onAnimationEnd(Animation animation) {
                if (this.this$0.mInfoAlpha == 0.0d) {
                    for (int i2 = 0; i2 < this.this$0.mInfoViewList.size(); i2++) {
                        ((View) this.this$0.mInfoViewList.get(i2)).setVisibility(8);
                    }
                }
            }

            @Override // android.view.animation.Animation.AnimationListener
            public void onAnimationRepeat(Animation animation) {
            }

            @Override // android.view.animation.Animation.AnimationListener
            public void onAnimationStart(Animation animation) {
            }
        });
        startAnimation(this.mAnim);
    }

    private void animateInfoHeight(boolean z) {
        cancelAnimations();
        int i = 0;
        if (z) {
            int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(this.mMeasuredWidth, 1073741824);
            int makeMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(0, 0);
            for (int i2 = 0; i2 < this.mExtraViewList.size(); i2++) {
                View view = this.mExtraViewList.get(i2);
                view.setVisibility(0);
                view.measure(makeMeasureSpec, makeMeasureSpec2);
                i = Math.max(i, view.getMeasuredHeight());
            }
        }
        this.mAnim = new InfoHeightAnimation(this, this.mInfoVisFraction, z ? 1.0f : 0.0f);
        this.mAnim.setDuration(this.mSelectedAnimDuration);
        this.mAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        this.mAnim.setAnimationListener(new Animation.AnimationListener(this) { // from class: android.support.v17.leanback.widget.BaseCardView.3
            final BaseCardView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.animation.Animation.AnimationListener
            public void onAnimationEnd(Animation animation) {
                if (this.this$0.mInfoOffset == 0.0f) {
                    for (int i3 = 0; i3 < this.this$0.mExtraViewList.size(); i3++) {
                        ((View) this.this$0.mExtraViewList.get(i3)).setVisibility(8);
                    }
                }
            }

            @Override // android.view.animation.Animation.AnimationListener
            public void onAnimationRepeat(Animation animation) {
            }

            @Override // android.view.animation.Animation.AnimationListener
            public void onAnimationStart(Animation animation) {
            }
        });
        startAnimation(this.mAnim);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void animateInfoOffset(boolean z) {
        cancelAnimations();
        int i = 0;
        int i2 = 0;
        if (z) {
            int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(this.mMeasuredWidth, 1073741824);
            int makeMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(0, 0);
            int i3 = 0;
            while (true) {
                i = i2;
                if (i3 >= this.mExtraViewList.size()) {
                    break;
                }
                View view = this.mExtraViewList.get(i3);
                view.setVisibility(0);
                view.measure(makeMeasureSpec, makeMeasureSpec2);
                i2 = Math.max(i2, view.getMeasuredHeight());
                i3++;
            }
        }
        float f = this.mInfoOffset;
        if (!z) {
            i = 0;
        }
        this.mAnim = new InfoOffsetAnimation(this, f, i);
        this.mAnim.setDuration(this.mSelectedAnimDuration);
        this.mAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        this.mAnim.setAnimationListener(new Animation.AnimationListener(this) { // from class: android.support.v17.leanback.widget.BaseCardView.2
            final BaseCardView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.animation.Animation.AnimationListener
            public void onAnimationEnd(Animation animation) {
                if (this.this$0.mInfoOffset == 0.0f) {
                    for (int i4 = 0; i4 < this.this$0.mExtraViewList.size(); i4++) {
                        ((View) this.this$0.mExtraViewList.get(i4)).setVisibility(8);
                    }
                }
            }

            @Override // android.view.animation.Animation.AnimationListener
            public void onAnimationRepeat(Animation animation) {
            }

            @Override // android.view.animation.Animation.AnimationListener
            public void onAnimationStart(Animation animation) {
            }
        });
        startAnimation(this.mAnim);
    }

    private void applyActiveState(boolean z) {
        if (hasInfoRegion() && this.mInfoVisibility <= 1) {
            setInfoViewVisibility(z);
        }
        if (!hasExtraRegion() || this.mExtraVisibility <= 1) {
        }
    }

    private void applySelectedState(boolean z) {
        removeCallbacks(this.mAnimationTrigger);
        if (this.mCardType != 3) {
            if (this.mInfoVisibility == 2) {
                setInfoViewVisibility(z);
            }
        } else if (!z) {
            animateInfoOffset(false);
        } else if (this.mDelaySelectedAnim) {
            postDelayed(this.mAnimationTrigger, this.mSelectedAnimationDelay);
        } else {
            post(this.mAnimationTrigger);
            this.mDelaySelectedAnim = true;
        }
    }

    private void cancelAnimations() {
        if (this.mAnim != null) {
            this.mAnim.cancel();
            this.mAnim = null;
        }
    }

    private void findChildrenViews() {
        this.mMainViewList.clear();
        this.mInfoViewList.clear();
        this.mExtraViewList.clear();
        int childCount = getChildCount();
        boolean isRegionVisible = isRegionVisible(this.mInfoVisibility);
        boolean z = hasExtraRegion() && this.mInfoOffset > 0.0f;
        boolean z2 = isRegionVisible;
        if (this.mCardType == 2) {
            z2 = isRegionVisible;
            if (this.mInfoVisibility == 2) {
                z2 = isRegionVisible && this.mInfoVisFraction > 0.0f;
            }
        }
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            if (childAt != null) {
                LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
                if (layoutParams.viewType == 1) {
                    this.mInfoViewList.add(childAt);
                    childAt.setVisibility(z2 ? 0 : 8);
                } else if (layoutParams.viewType == 2) {
                    this.mExtraViewList.add(childAt);
                    childAt.setVisibility(z ? 0 : 8);
                } else {
                    this.mMainViewList.add(childAt);
                    childAt.setVisibility(0);
                }
            }
        }
    }

    private boolean hasExtraRegion() {
        return this.mCardType == 3;
    }

    private boolean hasInfoRegion() {
        boolean z = false;
        if (this.mCardType != 0) {
            z = true;
        }
        return z;
    }

    private boolean isRegionVisible(int i) {
        boolean z = false;
        switch (i) {
            case 0:
                return true;
            case 1:
                return isActivated();
            case 2:
                if (isActivated()) {
                    z = isSelected();
                }
                return z;
            default:
                return false;
        }
    }

    private void setInfoViewVisibility(boolean z) {
        if (this.mCardType != 3) {
            if (this.mCardType != 2) {
                if (this.mCardType == 1) {
                    animateInfoAlpha(z);
                }
            } else if (this.mInfoVisibility == 2) {
                animateInfoHeight(z);
            } else {
                for (int i = 0; i < this.mInfoViewList.size(); i++) {
                    this.mInfoViewList.get(i).setVisibility(z ? 0 : 8);
                }
            }
        } else if (z) {
            for (int i2 = 0; i2 < this.mInfoViewList.size(); i2++) {
                this.mInfoViewList.get(i2).setVisibility(0);
            }
        } else {
            for (int i3 = 0; i3 < this.mInfoViewList.size(); i3++) {
                this.mInfoViewList.get(i3).setVisibility(8);
            }
            for (int i4 = 0; i4 < this.mExtraViewList.size(); i4++) {
                this.mExtraViewList.get(i4).setVisibility(8);
            }
            this.mInfoOffset = 0.0f;
        }
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup
    protected boolean checkLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return layoutParams instanceof LayoutParams;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.widget.FrameLayout, android.view.ViewGroup
    public LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-2, -2);
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup
    public LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        return new LayoutParams(getContext(), attributeSet);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.widget.FrameLayout, android.view.ViewGroup
    public LayoutParams generateLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return layoutParams instanceof LayoutParams ? new LayoutParams((LayoutParams) layoutParams) : new LayoutParams(layoutParams);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected int[] onCreateDrawableState(int i) {
        int[] onCreateDrawableState = super.onCreateDrawableState(i);
        int length = onCreateDrawableState.length;
        boolean z = false;
        boolean z2 = false;
        for (int i2 = 0; i2 < length; i2++) {
            if (onCreateDrawableState[i2] == 16842919) {
                z = true;
            }
            if (onCreateDrawableState[i2] == 16842910) {
                z2 = true;
            }
        }
        return (z && z2) ? View.PRESSED_ENABLED_STATE_SET : z ? LB_PRESSED_STATE_SET : z2 ? View.ENABLED_STATE_SET : View.EMPTY_STATE_SET;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.ViewGroup, android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(this.mAnimationTrigger);
        cancelAnimations();
        this.mInfoOffset = 0.0f;
        this.mInfoVisFraction = 0.0f;
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        float f;
        float f2;
        float f3;
        float paddingTop = getPaddingTop();
        int i5 = 0;
        while (i5 < this.mMainViewList.size()) {
            View view = this.mMainViewList.get(i5);
            float f4 = paddingTop;
            if (view.getVisibility() != 8) {
                view.layout(getPaddingLeft(), (int) paddingTop, this.mMeasuredWidth + getPaddingLeft(), (int) (view.getMeasuredHeight() + paddingTop));
                f4 = paddingTop + view.getMeasuredHeight();
            }
            i5++;
            paddingTop = f4;
        }
        if (hasInfoRegion()) {
            float f5 = 0.0f;
            for (int i6 = 0; i6 < this.mInfoViewList.size(); i6++) {
                f5 += this.mInfoViewList.get(i6).getMeasuredHeight();
            }
            if (this.mCardType == 1) {
                float f6 = paddingTop - f5;
                f = f6;
                f2 = f5;
                if (f6 < 0.0f) {
                    f = 0.0f;
                    f2 = f5;
                }
            } else if (this.mCardType == 2) {
                f = paddingTop;
                f2 = f5;
                if (this.mInfoVisibility == 2) {
                    f2 = f5 * this.mInfoVisFraction;
                    f = paddingTop;
                }
            } else {
                f = paddingTop - this.mInfoOffset;
                f2 = f5;
            }
            int i7 = 0;
            while (true) {
                f3 = f;
                if (i7 >= this.mInfoViewList.size()) {
                    break;
                }
                View view2 = this.mInfoViewList.get(i7);
                float f7 = f;
                float f8 = f2;
                if (view2.getVisibility() != 8) {
                    int measuredHeight = view2.getMeasuredHeight();
                    int i8 = measuredHeight;
                    if (measuredHeight > f2) {
                        i8 = (int) f2;
                    }
                    view2.layout(getPaddingLeft(), (int) f, this.mMeasuredWidth + getPaddingLeft(), (int) (i8 + f));
                    float f9 = f + i8;
                    float f10 = f2 - i8;
                    f7 = f9;
                    f8 = f10;
                    if (f10 <= 0.0f) {
                        f3 = f9;
                        break;
                    }
                }
                i7++;
                f = f7;
                f2 = f8;
            }
            if (hasExtraRegion()) {
                int i9 = 0;
                while (i9 < this.mExtraViewList.size()) {
                    View view3 = this.mExtraViewList.get(i9);
                    float f11 = f3;
                    if (view3.getVisibility() != 8) {
                        view3.layout(getPaddingLeft(), (int) f3, this.mMeasuredWidth + getPaddingLeft(), (int) (view3.getMeasuredHeight() + f3));
                        f11 = f3 + view3.getMeasuredHeight();
                    }
                    i9++;
                    f3 = f11;
                }
            }
        }
        onSizeChanged(0, 0, i3 - i, i4 - i2);
    }

    @Override // android.widget.FrameLayout, android.view.View
    protected void onMeasure(int i, int i2) {
        this.mMeasuredWidth = 0;
        this.mMeasuredHeight = 0;
        int i3 = 0;
        int i4 = 0;
        int i5 = 0;
        findChildrenViews();
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, 0);
        int i6 = 0;
        while (i6 < this.mMainViewList.size()) {
            View view = this.mMainViewList.get(i6);
            int i7 = i4;
            int i8 = i3;
            if (view.getVisibility() != 8) {
                measureChild(view, makeMeasureSpec, makeMeasureSpec);
                this.mMeasuredWidth = Math.max(this.mMeasuredWidth, view.getMeasuredWidth());
                i7 = i4 + view.getMeasuredHeight();
                i8 = View.combineMeasuredStates(i3, view.getMeasuredState());
            }
            i6++;
            i4 = i7;
            i3 = i8;
        }
        setPivotX(this.mMeasuredWidth / 2);
        setPivotY(i4 / 2);
        int makeMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(this.mMeasuredWidth, 1073741824);
        int i9 = 0;
        int i10 = i3;
        if (hasInfoRegion()) {
            int i11 = 0;
            int i12 = 0;
            while (i11 < this.mInfoViewList.size()) {
                View view2 = this.mInfoViewList.get(i11);
                int i13 = i12;
                int i14 = i3;
                if (view2.getVisibility() != 8) {
                    measureChild(view2, makeMeasureSpec2, makeMeasureSpec);
                    i13 = i12;
                    if (this.mCardType != 1) {
                        i13 = i12 + view2.getMeasuredHeight();
                    }
                    i14 = View.combineMeasuredStates(i3, view2.getMeasuredState());
                }
                i11++;
                i12 = i13;
                i3 = i14;
            }
            i9 = 0;
            i5 = i12;
            i10 = i3;
            if (hasExtraRegion()) {
                int i15 = 0;
                int i16 = 0;
                while (true) {
                    i9 = i16;
                    i5 = i12;
                    i10 = i3;
                    if (i15 >= this.mExtraViewList.size()) {
                        break;
                    }
                    View view3 = this.mExtraViewList.get(i15);
                    int i17 = i16;
                    int i18 = i3;
                    if (view3.getVisibility() != 8) {
                        measureChild(view3, makeMeasureSpec2, makeMeasureSpec);
                        i17 = i16 + view3.getMeasuredHeight();
                        i18 = View.combineMeasuredStates(i3, view3.getMeasuredState());
                    }
                    i15++;
                    i16 = i17;
                    i3 = i18;
                }
            }
        }
        boolean z = hasInfoRegion() && this.mInfoVisibility == 2;
        this.mMeasuredHeight = (int) ((i9 + ((z ? i5 * this.mInfoVisFraction : i5) + i4)) - (z ? 0.0f : this.mInfoOffset));
        setMeasuredDimension(View.resolveSizeAndState(this.mMeasuredWidth + getPaddingLeft() + getPaddingRight(), i, i10), View.resolveSizeAndState(this.mMeasuredHeight + getPaddingTop() + getPaddingBottom(), i2, i10 << 16));
    }

    @Override // android.view.View
    public void setActivated(boolean z) {
        if (z != isActivated()) {
            super.setActivated(z);
            applyActiveState(isActivated());
        }
    }

    @Override // android.view.View
    public void setSelected(boolean z) {
        if (z != isSelected()) {
            super.setSelected(z);
            applySelectedState(isSelected());
        }
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    @Override // android.view.View
    public String toString() {
        return super.toString();
    }
}
