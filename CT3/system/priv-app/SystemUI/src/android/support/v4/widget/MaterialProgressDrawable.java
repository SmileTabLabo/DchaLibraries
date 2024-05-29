package android.support.v4.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import java.util.ArrayList;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: a.zip:android/support/v4/widget/MaterialProgressDrawable.class */
public class MaterialProgressDrawable extends Drawable implements Animatable {
    private static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
    private static final Interpolator MATERIAL_INTERPOLATOR = new FastOutSlowInInterpolator();
    private Animation mAnimation;
    boolean mFinishing;
    private double mHeight;
    private View mParent;
    private Resources mResources;
    private float mRotation;
    private float mRotationCount;
    private double mWidth;
    private final int[] COLORS = {-16777216};
    private final ArrayList<Animation> mAnimators = new ArrayList<>();
    private final Drawable.Callback mCallback = new Drawable.Callback(this) { // from class: android.support.v4.widget.MaterialProgressDrawable.1
        final MaterialProgressDrawable this$0;

        {
            this.this$0 = this;
        }

        @Override // android.graphics.drawable.Drawable.Callback
        public void invalidateDrawable(Drawable drawable) {
            this.this$0.invalidateSelf();
        }

        @Override // android.graphics.drawable.Drawable.Callback
        public void scheduleDrawable(Drawable drawable, Runnable runnable, long j) {
            this.this$0.scheduleSelf(runnable, j);
        }

        @Override // android.graphics.drawable.Drawable.Callback
        public void unscheduleDrawable(Drawable drawable, Runnable runnable) {
            this.this$0.unscheduleSelf(runnable);
        }
    };
    private final Ring mRing = new Ring(this.mCallback);

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:android/support/v4/widget/MaterialProgressDrawable$Ring.class */
    public static class Ring {
        private int mAlpha;
        private Path mArrow;
        private int mArrowHeight;
        private float mArrowScale;
        private int mArrowWidth;
        private int mBackgroundColor;
        private final Drawable.Callback mCallback;
        private int mColorIndex;
        private int[] mColors;
        private int mCurrentColor;
        private double mRingCenterRadius;
        private boolean mShowArrow;
        private float mStartingEndTrim;
        private float mStartingRotation;
        private float mStartingStartTrim;
        private final RectF mTempBounds = new RectF();
        private final Paint mPaint = new Paint();
        private final Paint mArrowPaint = new Paint();
        private float mStartTrim = 0.0f;
        private float mEndTrim = 0.0f;
        private float mRotation = 0.0f;
        private float mStrokeWidth = 5.0f;
        private float mStrokeInset = 2.5f;
        private final Paint mCirclePaint = new Paint(1);

        public Ring(Drawable.Callback callback) {
            this.mCallback = callback;
            this.mPaint.setStrokeCap(Paint.Cap.SQUARE);
            this.mPaint.setAntiAlias(true);
            this.mPaint.setStyle(Paint.Style.STROKE);
            this.mArrowPaint.setStyle(Paint.Style.FILL);
            this.mArrowPaint.setAntiAlias(true);
        }

        private void drawTriangle(Canvas canvas, float f, float f2, Rect rect) {
            if (this.mShowArrow) {
                if (this.mArrow == null) {
                    this.mArrow = new Path();
                    this.mArrow.setFillType(Path.FillType.EVEN_ODD);
                } else {
                    this.mArrow.reset();
                }
                float f3 = ((int) this.mStrokeInset) / 2;
                float f4 = this.mArrowScale;
                float cos = (float) ((this.mRingCenterRadius * Math.cos(0.0d)) + rect.exactCenterX());
                float sin = (float) ((this.mRingCenterRadius * Math.sin(0.0d)) + rect.exactCenterY());
                this.mArrow.moveTo(0.0f, 0.0f);
                this.mArrow.lineTo(this.mArrowWidth * this.mArrowScale, 0.0f);
                this.mArrow.lineTo((this.mArrowWidth * this.mArrowScale) / 2.0f, this.mArrowHeight * this.mArrowScale);
                this.mArrow.offset(cos - (f3 * f4), sin);
                this.mArrow.close();
                this.mArrowPaint.setColor(this.mCurrentColor);
                canvas.rotate((f + f2) - 5.0f, rect.exactCenterX(), rect.exactCenterY());
                canvas.drawPath(this.mArrow, this.mArrowPaint);
            }
        }

        private int getNextColorIndex() {
            return (this.mColorIndex + 1) % this.mColors.length;
        }

        private void invalidateSelf() {
            this.mCallback.invalidateDrawable(null);
        }

        public void draw(Canvas canvas, Rect rect) {
            RectF rectF = this.mTempBounds;
            rectF.set(rect);
            rectF.inset(this.mStrokeInset, this.mStrokeInset);
            float f = (this.mStartTrim + this.mRotation) * 360.0f;
            float f2 = ((this.mEndTrim + this.mRotation) * 360.0f) - f;
            this.mPaint.setColor(this.mCurrentColor);
            canvas.drawArc(rectF, f, f2, false, this.mPaint);
            drawTriangle(canvas, f, f2, rect);
            if (this.mAlpha < 255) {
                this.mCirclePaint.setColor(this.mBackgroundColor);
                this.mCirclePaint.setAlpha(255 - this.mAlpha);
                canvas.drawCircle(rect.exactCenterX(), rect.exactCenterY(), rect.width() / 2, this.mCirclePaint);
            }
        }

        public int getAlpha() {
            return this.mAlpha;
        }

        public double getCenterRadius() {
            return this.mRingCenterRadius;
        }

        public float getEndTrim() {
            return this.mEndTrim;
        }

        public int getNextColor() {
            return this.mColors[getNextColorIndex()];
        }

        public float getStartTrim() {
            return this.mStartTrim;
        }

        public int getStartingColor() {
            return this.mColors[this.mColorIndex];
        }

        public float getStartingEndTrim() {
            return this.mStartingEndTrim;
        }

        public float getStartingRotation() {
            return this.mStartingRotation;
        }

        public float getStartingStartTrim() {
            return this.mStartingStartTrim;
        }

        public float getStrokeWidth() {
            return this.mStrokeWidth;
        }

        public void goToNextColor() {
            setColorIndex(getNextColorIndex());
        }

        public void resetOriginals() {
            this.mStartingStartTrim = 0.0f;
            this.mStartingEndTrim = 0.0f;
            this.mStartingRotation = 0.0f;
            setStartTrim(0.0f);
            setEndTrim(0.0f);
            setRotation(0.0f);
        }

        public void setAlpha(int i) {
            this.mAlpha = i;
        }

        public void setArrowDimensions(float f, float f2) {
            this.mArrowWidth = (int) f;
            this.mArrowHeight = (int) f2;
        }

        public void setArrowScale(float f) {
            if (f != this.mArrowScale) {
                this.mArrowScale = f;
                invalidateSelf();
            }
        }

        public void setBackgroundColor(int i) {
            this.mBackgroundColor = i;
        }

        public void setCenterRadius(double d) {
            this.mRingCenterRadius = d;
        }

        public void setColor(int i) {
            this.mCurrentColor = i;
        }

        public void setColorFilter(ColorFilter colorFilter) {
            this.mPaint.setColorFilter(colorFilter);
            invalidateSelf();
        }

        public void setColorIndex(int i) {
            this.mColorIndex = i;
            this.mCurrentColor = this.mColors[this.mColorIndex];
        }

        public void setColors(@NonNull int[] iArr) {
            this.mColors = iArr;
            setColorIndex(0);
        }

        public void setEndTrim(float f) {
            this.mEndTrim = f;
            invalidateSelf();
        }

        public void setInsets(int i, int i2) {
            float min;
            this.mStrokeInset = (this.mRingCenterRadius <= 0.0d || Math.min(i, i2) < 0.0f) ? (float) Math.ceil(this.mStrokeWidth / 2.0f) : (float) ((min / 2.0f) - this.mRingCenterRadius);
        }

        public void setRotation(float f) {
            this.mRotation = f;
            invalidateSelf();
        }

        public void setShowArrow(boolean z) {
            if (this.mShowArrow != z) {
                this.mShowArrow = z;
                invalidateSelf();
            }
        }

        public void setStartTrim(float f) {
            this.mStartTrim = f;
            invalidateSelf();
        }

        public void setStrokeWidth(float f) {
            this.mStrokeWidth = f;
            this.mPaint.setStrokeWidth(f);
            invalidateSelf();
        }

        public void storeOriginals() {
            this.mStartingStartTrim = this.mStartTrim;
            this.mStartingEndTrim = this.mEndTrim;
            this.mStartingRotation = this.mRotation;
        }
    }

    public MaterialProgressDrawable(Context context, View view) {
        this.mParent = view;
        this.mResources = context.getResources();
        this.mRing.setColors(this.COLORS);
        updateSizes(1);
        setupAnimators();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void applyFinishTranslation(float f, Ring ring) {
        updateRingColor(f, ring);
        float floor = (float) (Math.floor(ring.getStartingRotation() / 0.8f) + 1.0d);
        ring.setStartTrim(ring.getStartingStartTrim() + (((ring.getStartingEndTrim() - getMinProgressArc(ring)) - ring.getStartingStartTrim()) * f));
        ring.setEndTrim(ring.getStartingEndTrim());
        ring.setRotation(ring.getStartingRotation() + ((floor - ring.getStartingRotation()) * f));
    }

    private int evaluateColorChange(float f, int i, int i2) {
        int intValue = Integer.valueOf(i).intValue();
        int i3 = (intValue >> 24) & 255;
        int i4 = (intValue >> 16) & 255;
        int i5 = (intValue >> 8) & 255;
        int i6 = intValue & 255;
        int intValue2 = Integer.valueOf(i2).intValue();
        return ((((int) ((((intValue2 >> 24) & 255) - i3) * f)) + i3) << 24) | ((((int) ((((intValue2 >> 16) & 255) - i4) * f)) + i4) << 16) | ((((int) ((((intValue2 >> 8) & 255) - i5) * f)) + i5) << 8) | (((int) (((intValue2 & 255) - i6) * f)) + i6);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public float getMinProgressArc(Ring ring) {
        return (float) Math.toRadians(ring.getStrokeWidth() / (ring.getCenterRadius() * 6.283185307179586d));
    }

    private void setSizeParameters(double d, double d2, double d3, double d4, float f, float f2) {
        Ring ring = this.mRing;
        float f3 = this.mResources.getDisplayMetrics().density;
        this.mWidth = f3 * d;
        this.mHeight = f3 * d2;
        ring.setStrokeWidth(((float) d4) * f3);
        ring.setCenterRadius(f3 * d3);
        ring.setColorIndex(0);
        ring.setArrowDimensions(f * f3, f2 * f3);
        ring.setInsets((int) this.mWidth, (int) this.mHeight);
    }

    private void setupAnimators() {
        Ring ring = this.mRing;
        Animation animation = new Animation(this, ring) { // from class: android.support.v4.widget.MaterialProgressDrawable.2
            final MaterialProgressDrawable this$0;
            final Ring val$ring;

            {
                this.this$0 = this;
                this.val$ring = ring;
            }

            @Override // android.view.animation.Animation
            public void applyTransformation(float f, Transformation transformation) {
                if (this.this$0.mFinishing) {
                    this.this$0.applyFinishTranslation(f, this.val$ring);
                    return;
                }
                float minProgressArc = this.this$0.getMinProgressArc(this.val$ring);
                float startingEndTrim = this.val$ring.getStartingEndTrim();
                float startingStartTrim = this.val$ring.getStartingStartTrim();
                float startingRotation = this.val$ring.getStartingRotation();
                this.this$0.updateRingColor(f, this.val$ring);
                if (f <= 0.5f) {
                    this.val$ring.setStartTrim(startingStartTrim + ((0.8f - minProgressArc) * MaterialProgressDrawable.MATERIAL_INTERPOLATOR.getInterpolation(f / 0.5f)));
                }
                if (f > 0.5f) {
                    this.val$ring.setEndTrim(startingEndTrim + (MaterialProgressDrawable.MATERIAL_INTERPOLATOR.getInterpolation((f - 0.5f) / 0.5f) * (0.8f - minProgressArc)));
                }
                this.val$ring.setRotation(startingRotation + (0.25f * f));
                this.this$0.setRotation((216.0f * f) + ((this.this$0.mRotationCount / 5.0f) * 1080.0f));
            }
        };
        animation.setRepeatCount(-1);
        animation.setRepeatMode(1);
        animation.setInterpolator(LINEAR_INTERPOLATOR);
        animation.setAnimationListener(new Animation.AnimationListener(this, ring) { // from class: android.support.v4.widget.MaterialProgressDrawable.3
            final MaterialProgressDrawable this$0;
            final Ring val$ring;

            {
                this.this$0 = this;
                this.val$ring = ring;
            }

            @Override // android.view.animation.Animation.AnimationListener
            public void onAnimationEnd(Animation animation2) {
            }

            @Override // android.view.animation.Animation.AnimationListener
            public void onAnimationRepeat(Animation animation2) {
                this.val$ring.storeOriginals();
                this.val$ring.goToNextColor();
                this.val$ring.setStartTrim(this.val$ring.getEndTrim());
                if (!this.this$0.mFinishing) {
                    this.this$0.mRotationCount = (this.this$0.mRotationCount + 1.0f) % 5.0f;
                    return;
                }
                this.this$0.mFinishing = false;
                animation2.setDuration(1332L);
                this.val$ring.setShowArrow(false);
            }

            @Override // android.view.animation.Animation.AnimationListener
            public void onAnimationStart(Animation animation2) {
                this.this$0.mRotationCount = 0.0f;
            }
        });
        this.mAnimation = animation;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateRingColor(float f, Ring ring) {
        if (f > 0.75f) {
            ring.setColor(evaluateColorChange((f - 0.75f) / 0.25f, ring.getStartingColor(), ring.getNextColor()));
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();
        int save = canvas.save();
        canvas.rotate(this.mRotation, bounds.exactCenterX(), bounds.exactCenterY());
        this.mRing.draw(canvas, bounds);
        canvas.restoreToCount(save);
    }

    @Override // android.graphics.drawable.Drawable
    public int getAlpha() {
        return this.mRing.getAlpha();
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicHeight() {
        return (int) this.mHeight;
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicWidth() {
        return (int) this.mWidth;
    }

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        return -3;
    }

    @Override // android.graphics.drawable.Animatable
    public boolean isRunning() {
        ArrayList<Animation> arrayList = this.mAnimators;
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            Animation animation = arrayList.get(i);
            if (animation.hasStarted() && !animation.hasEnded()) {
                return true;
            }
        }
        return false;
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int i) {
        this.mRing.setAlpha(i);
    }

    public void setArrowScale(float f) {
        this.mRing.setArrowScale(f);
    }

    public void setBackgroundColor(int i) {
        this.mRing.setBackgroundColor(i);
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter colorFilter) {
        this.mRing.setColorFilter(colorFilter);
    }

    public void setProgressRotation(float f) {
        this.mRing.setRotation(f);
    }

    void setRotation(float f) {
        this.mRotation = f;
        invalidateSelf();
    }

    public void setStartEndTrim(float f, float f2) {
        this.mRing.setStartTrim(f);
        this.mRing.setEndTrim(f2);
    }

    public void showArrow(boolean z) {
        this.mRing.setShowArrow(z);
    }

    @Override // android.graphics.drawable.Animatable
    public void start() {
        this.mAnimation.reset();
        this.mRing.storeOriginals();
        if (this.mRing.getEndTrim() != this.mRing.getStartTrim()) {
            this.mFinishing = true;
            this.mAnimation.setDuration(666L);
            this.mParent.startAnimation(this.mAnimation);
            return;
        }
        this.mRing.setColorIndex(0);
        this.mRing.resetOriginals();
        this.mAnimation.setDuration(1332L);
        this.mParent.startAnimation(this.mAnimation);
    }

    @Override // android.graphics.drawable.Animatable
    public void stop() {
        this.mParent.clearAnimation();
        setRotation(0.0f);
        this.mRing.setShowArrow(false);
        this.mRing.setColorIndex(0);
        this.mRing.resetOriginals();
    }

    public void updateSizes(@ProgressDrawableSize int i) {
        if (i == 0) {
            setSizeParameters(56.0d, 56.0d, 12.5d, 3.0d, 12.0f, 6.0f);
        } else {
            setSizeParameters(40.0d, 40.0d, 8.75d, 2.5d, 10.0f, 5.0f);
        }
    }
}
