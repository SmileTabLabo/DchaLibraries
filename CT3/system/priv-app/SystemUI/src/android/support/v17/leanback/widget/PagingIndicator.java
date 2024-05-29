package android.support.v17.leanback.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.annotation.VisibleForTesting;
import android.support.v17.leanback.R$color;
import android.support.v17.leanback.R$dimen;
import android.support.v17.leanback.R$drawable;
import android.support.v17.leanback.R$styleable;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
/* loaded from: a.zip:android/support/v17/leanback/widget/PagingIndicator.class */
public class PagingIndicator extends View {
    private static final TimeInterpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final Property<Dot, Float> DOT_ALPHA = new Property<Dot, Float>(Float.class, "alpha") { // from class: android.support.v17.leanback.widget.PagingIndicator.1
        @Override // android.util.Property
        public Float get(Dot dot) {
            return Float.valueOf(dot.getAlpha());
        }

        @Override // android.util.Property
        public void set(Dot dot, Float f) {
            dot.setAlpha(f.floatValue());
        }
    };
    private static final Property<Dot, Float> DOT_DIAMETER = new Property<Dot, Float>(Float.class, "diameter") { // from class: android.support.v17.leanback.widget.PagingIndicator.2
        @Override // android.util.Property
        public Float get(Dot dot) {
            return Float.valueOf(dot.getDiameter());
        }

        @Override // android.util.Property
        public void set(Dot dot, Float f) {
            dot.setDiameter(f.floatValue());
        }
    };
    private static final Property<Dot, Float> DOT_TRANSLATION_X = new Property<Dot, Float>(Float.class, "translation_x") { // from class: android.support.v17.leanback.widget.PagingIndicator.3
        @Override // android.util.Property
        public Float get(Dot dot) {
            return Float.valueOf(dot.getTranslationX());
        }

        @Override // android.util.Property
        public void set(Dot dot, Float f) {
            dot.setTranslationX(f.floatValue());
        }
    };
    private final AnimatorSet mAnimator;
    private Bitmap mArrow;
    private final int mArrowDiameter;
    private final int mArrowGap;
    private final int mArrowRadius;
    private final Rect mArrowRect;
    private final float mArrowToBgRatio;
    private final Paint mBgPaint;
    private int mCurrentPage;
    private int mDotCenterY;
    private final int mDotDiameter;
    @ColorInt
    private final int mDotFgSelectColor;
    private final int mDotGap;
    private final int mDotRadius;
    private int[] mDotSelectedNextX;
    private int[] mDotSelectedPrevX;
    private int[] mDotSelectedX;
    private Dot[] mDots;
    private final Paint mFgPaint;
    private final AnimatorSet mHideAnimator;
    private boolean mIsLtr;
    private int mPageCount;
    private int mPreviousPage;
    private final int mShadowRadius;
    private final AnimatorSet mShowAnimator;

    /* loaded from: a.zip:android/support/v17/leanback/widget/PagingIndicator$Dot.class */
    public class Dot {
        float mAlpha;
        float mArrowImageRadius;
        float mCenterX;
        float mDiameter;
        float mDirection;
        @ColorInt
        int mFgColor;
        float mLayoutDirection;
        float mRadius;
        float mTranslationX;
        final PagingIndicator this$0;

        public void adjustAlpha() {
            this.mFgColor = Color.argb(Math.round(this.mAlpha * 255.0f), Color.red(this.this$0.mDotFgSelectColor), Color.green(this.this$0.mDotFgSelectColor), Color.blue(this.this$0.mDotFgSelectColor));
        }

        void deselect() {
            this.mTranslationX = 0.0f;
            this.mCenterX = 0.0f;
            this.mDiameter = this.this$0.mDotDiameter;
            this.mRadius = this.this$0.mDotRadius;
            this.mArrowImageRadius = this.mRadius * this.this$0.mArrowToBgRatio;
            this.mAlpha = 0.0f;
            adjustAlpha();
        }

        void draw(Canvas canvas) {
            float f = this.mCenterX + this.mTranslationX;
            canvas.drawCircle(f, this.this$0.mDotCenterY, this.mRadius, this.this$0.mBgPaint);
            if (this.mAlpha > 0.0f) {
                this.this$0.mFgPaint.setColor(this.mFgColor);
                canvas.drawCircle(f, this.this$0.mDotCenterY, this.mRadius, this.this$0.mFgPaint);
                canvas.drawBitmap(this.this$0.mArrow, this.this$0.mArrowRect, new Rect((int) (f - this.mArrowImageRadius), (int) (this.this$0.mDotCenterY - this.mArrowImageRadius), (int) (this.mArrowImageRadius + f), (int) (this.this$0.mDotCenterY + this.mArrowImageRadius)), (Paint) null);
            }
        }

        public float getAlpha() {
            return this.mAlpha;
        }

        public float getDiameter() {
            return this.mDiameter;
        }

        public float getTranslationX() {
            return this.mTranslationX;
        }

        void onRtlPropertiesChanged() {
            this.mLayoutDirection = this.this$0.mIsLtr ? 1.0f : -1.0f;
        }

        void select() {
            this.mTranslationX = 0.0f;
            this.mCenterX = 0.0f;
            this.mDiameter = this.this$0.mArrowDiameter;
            this.mRadius = this.this$0.mArrowRadius;
            this.mArrowImageRadius = this.mRadius * this.this$0.mArrowToBgRatio;
            this.mAlpha = 1.0f;
            adjustAlpha();
        }

        public void setAlpha(float f) {
            this.mAlpha = f;
            adjustAlpha();
            this.this$0.invalidate();
        }

        public void setDiameter(float f) {
            this.mDiameter = f;
            this.mRadius = f / 2.0f;
            this.mArrowImageRadius = (f / 2.0f) * this.this$0.mArrowToBgRatio;
            this.this$0.invalidate();
        }

        public void setTranslationX(float f) {
            this.mTranslationX = this.mDirection * f * this.mLayoutDirection;
            this.this$0.invalidate();
        }
    }

    public PagingIndicator(Context context) {
        this(context, null, 0);
    }

    public PagingIndicator(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public PagingIndicator(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mAnimator = new AnimatorSet();
        Resources resources = getResources();
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.PagingIndicator, i, 0);
        this.mDotRadius = getDimensionFromTypedArray(obtainStyledAttributes, R$styleable.PagingIndicator_dotRadius, R$dimen.lb_page_indicator_dot_radius);
        this.mDotDiameter = this.mDotRadius * 2;
        this.mArrowRadius = getDimensionFromTypedArray(obtainStyledAttributes, R$styleable.PagingIndicator_arrowRadius, R$dimen.lb_page_indicator_arrow_radius);
        this.mArrowDiameter = this.mArrowRadius * 2;
        this.mDotGap = getDimensionFromTypedArray(obtainStyledAttributes, R$styleable.PagingIndicator_dotToDotGap, R$dimen.lb_page_indicator_dot_gap);
        this.mArrowGap = getDimensionFromTypedArray(obtainStyledAttributes, R$styleable.PagingIndicator_dotToArrowGap, R$dimen.lb_page_indicator_arrow_gap);
        int colorFromTypedArray = getColorFromTypedArray(obtainStyledAttributes, R$styleable.PagingIndicator_dotBgColor, R$color.lb_page_indicator_dot);
        this.mBgPaint = new Paint(1);
        this.mBgPaint.setColor(colorFromTypedArray);
        this.mDotFgSelectColor = getColorFromTypedArray(obtainStyledAttributes, R$styleable.PagingIndicator_arrowBgColor, R$color.lb_page_indicator_arrow_background);
        obtainStyledAttributes.recycle();
        this.mIsLtr = resources.getConfiguration().getLayoutDirection() == 0;
        int color = resources.getColor(R$color.lb_page_indicator_arrow_shadow);
        this.mShadowRadius = resources.getDimensionPixelSize(R$dimen.lb_page_indicator_arrow_shadow_radius);
        this.mFgPaint = new Paint(1);
        int dimensionPixelSize = resources.getDimensionPixelSize(R$dimen.lb_page_indicator_arrow_shadow_offset);
        this.mFgPaint.setShadowLayer(this.mShadowRadius, dimensionPixelSize, dimensionPixelSize, color);
        this.mArrow = loadArrow();
        this.mArrowRect = new Rect(0, 0, this.mArrow.getWidth(), this.mArrow.getHeight());
        this.mArrowToBgRatio = this.mArrow.getWidth() / this.mArrowDiameter;
        this.mShowAnimator = new AnimatorSet();
        this.mShowAnimator.playTogether(createDotAlphaAnimator(0.0f, 1.0f), createDotDiameterAnimator(this.mDotRadius * 2, this.mArrowRadius * 2), createDotTranslationXAnimator());
        this.mHideAnimator = new AnimatorSet();
        this.mHideAnimator.playTogether(createDotAlphaAnimator(1.0f, 0.0f), createDotDiameterAnimator(this.mArrowRadius * 2, this.mDotRadius * 2), createDotTranslationXAnimator());
        this.mAnimator.playTogether(this.mShowAnimator, this.mHideAnimator);
        setLayerType(1, null);
    }

    private void adjustDotPosition() {
        int i = 0;
        while (i < this.mCurrentPage) {
            this.mDots[i].deselect();
            this.mDots[i].mDirection = i == this.mPreviousPage ? -1.0f : 1.0f;
            this.mDots[i].mCenterX = this.mDotSelectedPrevX[i];
            i++;
        }
        this.mDots[this.mCurrentPage].select();
        this.mDots[this.mCurrentPage].mDirection = this.mPreviousPage < this.mCurrentPage ? -1.0f : 1.0f;
        this.mDots[this.mCurrentPage].mCenterX = this.mDotSelectedX[this.mCurrentPage];
        for (int i2 = this.mCurrentPage + 1; i2 < this.mPageCount; i2++) {
            this.mDots[i2].deselect();
            this.mDots[i2].mDirection = 1.0f;
            this.mDots[i2].mCenterX = this.mDotSelectedNextX[i2];
        }
    }

    private void calculateDotPositions() {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int width = getWidth();
        int paddingRight = getPaddingRight();
        int requiredWidth = getRequiredWidth();
        int i = (paddingLeft + (width - paddingRight)) / 2;
        this.mDotSelectedX = new int[this.mPageCount];
        this.mDotSelectedPrevX = new int[this.mPageCount];
        this.mDotSelectedNextX = new int[this.mPageCount];
        if (this.mIsLtr) {
            int i2 = i - (requiredWidth / 2);
            this.mDotSelectedX[0] = ((this.mDotRadius + i2) - this.mDotGap) + this.mArrowGap;
            this.mDotSelectedPrevX[0] = this.mDotRadius + i2;
            this.mDotSelectedNextX[0] = ((this.mDotRadius + i2) - (this.mDotGap * 2)) + (this.mArrowGap * 2);
            for (int i3 = 1; i3 < this.mPageCount; i3++) {
                this.mDotSelectedX[i3] = this.mDotSelectedPrevX[i3 - 1] + this.mArrowGap;
                this.mDotSelectedPrevX[i3] = this.mDotSelectedPrevX[i3 - 1] + this.mDotGap;
                this.mDotSelectedNextX[i3] = this.mDotSelectedX[i3 - 1] + this.mArrowGap;
            }
        } else {
            int i4 = i + (requiredWidth / 2);
            this.mDotSelectedX[0] = ((i4 - this.mDotRadius) + this.mDotGap) - this.mArrowGap;
            this.mDotSelectedPrevX[0] = i4 - this.mDotRadius;
            this.mDotSelectedNextX[0] = ((i4 - this.mDotRadius) + (this.mDotGap * 2)) - (this.mArrowGap * 2);
            for (int i5 = 1; i5 < this.mPageCount; i5++) {
                this.mDotSelectedX[i5] = this.mDotSelectedPrevX[i5 - 1] - this.mArrowGap;
                this.mDotSelectedPrevX[i5] = this.mDotSelectedPrevX[i5 - 1] - this.mDotGap;
                this.mDotSelectedNextX[i5] = this.mDotSelectedX[i5 - 1] - this.mArrowGap;
            }
        }
        this.mDotCenterY = this.mArrowRadius + paddingTop;
        adjustDotPosition();
    }

    private Animator createDotAlphaAnimator(float f, float f2) {
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat((Object) null, DOT_ALPHA, f, f2);
        ofFloat.setDuration(167L);
        ofFloat.setInterpolator(DECELERATE_INTERPOLATOR);
        return ofFloat;
    }

    private Animator createDotDiameterAnimator(float f, float f2) {
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat((Object) null, DOT_DIAMETER, f, f2);
        ofFloat.setDuration(417L);
        ofFloat.setInterpolator(DECELERATE_INTERPOLATOR);
        return ofFloat;
    }

    private Animator createDotTranslationXAnimator() {
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat((Object) null, DOT_TRANSLATION_X, (-this.mArrowGap) + this.mDotGap, 0.0f);
        ofFloat.setDuration(417L);
        ofFloat.setInterpolator(DECELERATE_INTERPOLATOR);
        return ofFloat;
    }

    private int getColorFromTypedArray(TypedArray typedArray, int i, int i2) {
        return typedArray.getColor(i, getResources().getColor(i2));
    }

    private int getDesiredHeight() {
        return getPaddingTop() + this.mArrowDiameter + getPaddingBottom() + this.mShadowRadius;
    }

    private int getDesiredWidth() {
        return getPaddingLeft() + getRequiredWidth() + getPaddingRight();
    }

    private int getDimensionFromTypedArray(TypedArray typedArray, int i, int i2) {
        return typedArray.getDimensionPixelOffset(i, getResources().getDimensionPixelOffset(i2));
    }

    private int getRequiredWidth() {
        return (this.mDotRadius * 2) + (this.mArrowGap * 2) + ((this.mPageCount - 3) * this.mDotGap);
    }

    private Bitmap loadArrow() {
        Bitmap decodeResource = BitmapFactory.decodeResource(getResources(), R$drawable.lb_ic_nav_arrow);
        if (this.mIsLtr) {
            return decodeResource;
        }
        Matrix matrix = new Matrix();
        matrix.preScale(-1.0f, 1.0f);
        return Bitmap.createBitmap(decodeResource, 0, 0, decodeResource.getWidth(), decodeResource.getHeight(), matrix, false);
    }

    @VisibleForTesting
    int[] getDotSelectedLeftX() {
        return this.mDotSelectedPrevX;
    }

    @VisibleForTesting
    int[] getDotSelectedRightX() {
        return this.mDotSelectedNextX;
    }

    @VisibleForTesting
    int[] getDotSelectedX() {
        return this.mDotSelectedX;
    }

    @VisibleForTesting
    int getPageCount() {
        return this.mPageCount;
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        for (int i = 0; i < this.mPageCount; i++) {
            this.mDots[i].draw(canvas);
        }
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        int min;
        int min2;
        int desiredHeight = getDesiredHeight();
        switch (View.MeasureSpec.getMode(i2)) {
            case Integer.MIN_VALUE:
                min = Math.min(desiredHeight, View.MeasureSpec.getSize(i2));
                break;
            case 1073741824:
                min = View.MeasureSpec.getSize(i2);
                break;
            default:
                min = desiredHeight;
                break;
        }
        int desiredWidth = getDesiredWidth();
        switch (View.MeasureSpec.getMode(i)) {
            case Integer.MIN_VALUE:
                min2 = Math.min(desiredWidth, View.MeasureSpec.getSize(i));
                break;
            case 1073741824:
                min2 = View.MeasureSpec.getSize(i);
                break;
            default:
                min2 = desiredWidth;
                break;
        }
        setMeasuredDimension(min2, min);
    }

    @Override // android.view.View
    public void onRtlPropertiesChanged(int i) {
        super.onRtlPropertiesChanged(i);
        boolean z = i == 0;
        if (this.mIsLtr != z) {
            this.mIsLtr = z;
            this.mArrow = loadArrow();
            if (this.mDots != null) {
                for (Dot dot : this.mDots) {
                    dot.onRtlPropertiesChanged();
                }
            }
            calculateDotPositions();
            invalidate();
        }
    }

    @Override // android.view.View
    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        setMeasuredDimension(i, i2);
        calculateDotPositions();
    }
}
