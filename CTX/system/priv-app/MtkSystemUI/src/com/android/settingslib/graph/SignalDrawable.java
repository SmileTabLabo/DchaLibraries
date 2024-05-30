package com.android.settingslib.graph;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import com.android.settingslib.R;
import com.android.settingslib.Utils;
/* loaded from: classes.dex */
public class SignalDrawable extends Drawable {
    private boolean mAnimating;
    private final float mAppliedCornerInset;
    private int mCurrentDot;
    private final int mDarkModeBackgroundColor;
    private final int mDarkModeFillColor;
    private int mIntrinsicSize;
    private int mLevel;
    private final int mLightModeBackgroundColor;
    private final int mLightModeFillColor;
    private int mState;
    private boolean mVisible;
    private static final float[] FIT = {2.26f, -3.02f, 1.76f};
    private static float[][] X_PATH = {new float[]{0.91249996f, 0.7083333f}, new float[]{-0.045833334f, -0.045833334f}, new float[]{-0.079166666f, 0.079166666f}, new float[]{-0.079166666f, -0.079166666f}, new float[]{-0.045833334f, 0.045833334f}, new float[]{0.079166666f, 0.079166666f}, new float[]{-0.079166666f, 0.079166666f}, new float[]{0.045833334f, 0.045833334f}, new float[]{0.079166666f, -0.079166666f}, new float[]{0.079166666f, 0.079166666f}, new float[]{0.045833334f, -0.045833334f}, new float[]{-0.079166666f, -0.079166666f}};
    private static final float INV_TAN = 1.0f / ((float) Math.tan(0.39269908169872414d));
    private final PointF mVirtualTop = new PointF();
    private final PointF mVirtualLeft = new PointF();
    private final Paint mPaint = new Paint(1);
    private final Paint mForegroundPaint = new Paint(1);
    private final Path mFullPath = new Path();
    private final Path mForegroundPath = new Path();
    private final Path mXPath = new Path();
    private final Path mCutPath = new Path();
    private final SlashArtist mSlash = new SlashArtist();
    private float mOldDarkIntensity = -1.0f;
    private float mNumLevels = 1.0f;
    private final Runnable mChangeDot = new Runnable() { // from class: com.android.settingslib.graph.SignalDrawable.1
        @Override // java.lang.Runnable
        public void run() {
            if (SignalDrawable.access$104(SignalDrawable.this) == 3) {
                SignalDrawable.this.mCurrentDot = 0;
            }
            SignalDrawable.this.invalidateSelf();
            SignalDrawable.this.mHandler.postDelayed(SignalDrawable.this.mChangeDot, 1000L);
        }
    };
    private final Handler mHandler = new Handler();

    static /* synthetic */ int access$104(SignalDrawable signalDrawable) {
        int i = signalDrawable.mCurrentDot + 1;
        signalDrawable.mCurrentDot = i;
        return i;
    }

    public SignalDrawable(Context context) {
        this.mDarkModeBackgroundColor = Utils.getDefaultColor(context, R.color.dark_mode_icon_color_dual_tone_background);
        this.mDarkModeFillColor = Utils.getDefaultColor(context, R.color.dark_mode_icon_color_dual_tone_fill);
        this.mLightModeBackgroundColor = Utils.getDefaultColor(context, R.color.light_mode_icon_color_dual_tone_background);
        this.mLightModeFillColor = Utils.getDefaultColor(context, R.color.light_mode_icon_color_dual_tone_fill);
        this.mIntrinsicSize = context.getResources().getDimensionPixelSize(R.dimen.signal_icon_size);
        setDarkIntensity(0.0f);
        this.mAppliedCornerInset = context.getResources().getDimensionPixelSize(R.dimen.stat_sys_mobile_signal_circle_inset);
    }

    public void setIntrinsicSize(int i) {
        this.mIntrinsicSize = i;
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicWidth() {
        return this.mIntrinsicSize;
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicHeight() {
        return this.mIntrinsicSize;
    }

    public void setNumLevels(int i) {
        float f = i;
        if (f == this.mNumLevels) {
            return;
        }
        this.mNumLevels = f;
        invalidateSelf();
    }

    private void setSignalState(int i) {
        if (i == this.mState) {
            return;
        }
        this.mState = i;
        updateAnimation();
        invalidateSelf();
    }

    private void updateAnimation() {
        boolean z = this.mState == 3 && this.mVisible;
        if (z == this.mAnimating) {
            return;
        }
        this.mAnimating = z;
        if (z) {
            this.mChangeDot.run();
        } else {
            this.mHandler.removeCallbacks(this.mChangeDot);
        }
    }

    @Override // android.graphics.drawable.Drawable
    protected boolean onLevelChange(int i) {
        setNumLevels(getNumLevels(i));
        setSignalState(getState(i));
        int level = getLevel(i);
        if (level != this.mLevel) {
            this.mLevel = level;
            invalidateSelf();
            return true;
        }
        return true;
    }

    public void setColors(int i, int i2) {
        this.mPaint.setColor(i);
        this.mForegroundPaint.setColor(i2);
    }

    public void setDarkIntensity(float f) {
        if (f == this.mOldDarkIntensity) {
            return;
        }
        this.mPaint.setColor(getBackgroundColor(f));
        this.mForegroundPaint.setColor(getFillColor(f));
        this.mOldDarkIntensity = f;
        invalidateSelf();
    }

    private int getFillColor(float f) {
        return getColorForDarkIntensity(f, this.mLightModeFillColor, this.mDarkModeFillColor);
    }

    private int getBackgroundColor(float f) {
        return getColorForDarkIntensity(f, this.mLightModeBackgroundColor, this.mDarkModeBackgroundColor);
    }

    private int getColorForDarkIntensity(float f, int i, int i2) {
        return ((Integer) ArgbEvaluator.getInstance().evaluate(f, Integer.valueOf(i), Integer.valueOf(i2))).intValue();
    }

    @Override // android.graphics.drawable.Drawable
    protected void onBoundsChange(Rect rect) {
        super.onBoundsChange(rect);
        invalidateSelf();
    }

    /* JADX WARN: Removed duplicated region for block: B:18:0x015e  */
    /* JADX WARN: Removed duplicated region for block: B:19:0x01c0  */
    /* JADX WARN: Removed duplicated region for block: B:27:0x0223  */
    /* JADX WARN: Removed duplicated region for block: B:33:0x0264  */
    /* JADX WARN: Removed duplicated region for block: B:36:? A[RETURN, SYNTHETIC] */
    @Override // android.graphics.drawable.Drawable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void draw(Canvas canvas) {
        float f;
        float f2;
        float f3;
        float f4;
        float width = getBounds().width();
        float height = getBounds().height();
        boolean z = getLayoutDirection() == 1;
        if (z) {
            canvas.save();
            canvas.translate(width, 0.0f);
            canvas.scale(-1.0f, 1.0f);
        }
        this.mFullPath.reset();
        this.mFullPath.setFillType(Path.FillType.WINDING);
        float round = Math.round(0.083333336f * width);
        float f5 = 0.04411765f * height;
        float f6 = 0.707107f * f5;
        float f7 = width - round;
        float f8 = height - round;
        float f9 = f8 - f5;
        this.mFullPath.moveTo(f7, f9);
        float f10 = round + f5;
        this.mFullPath.lineTo(f7, f10 + this.mAppliedCornerInset);
        float f11 = 2.0f * f5;
        float f12 = f7 - f11;
        this.mFullPath.arcTo(f12, round + this.mAppliedCornerInset, f7, this.mAppliedCornerInset + round + f11, 0.0f, -135.0f, false);
        this.mFullPath.lineTo(((this.mAppliedCornerInset + round) + f5) - f6, f9 - f6);
        float f13 = f8 - f11;
        this.mFullPath.arcTo(round + this.mAppliedCornerInset, f13, this.mAppliedCornerInset + round + f11, f8, -135.0f, -135.0f, false);
        this.mFullPath.lineTo(f7 - f5, f8);
        this.mFullPath.arcTo(f12, f13, f7, f8, 90.0f, -90.0f, false);
        if (this.mState == 3) {
            float f14 = 0.5833334f * width;
            float f15 = 0.16666667f * width;
            float f16 = 0.125f * height;
            this.mFullPath.moveTo(f7, f8);
            this.mFullPath.rLineTo(-f14, 0.0f);
            this.mFullPath.rLineTo(0.0f, -f15);
            this.mFullPath.rLineTo(f14, 0.0f);
            this.mFullPath.rLineTo(0.0f, f15);
            float f17 = (0.041666668f * height * 2.0f) + f16;
            float f18 = f7 - f16;
            float f19 = f8 - f16;
            this.mForegroundPath.reset();
            f = f8;
            f2 = f7;
            f3 = round;
            drawDot(this.mFullPath, this.mForegroundPath, f18, f19, f16, 2);
            drawDot(this.mFullPath, this.mForegroundPath, f18 - f17, f19, f16, 1);
            drawDot(this.mFullPath, this.mForegroundPath, f18 - (f17 * 2.0f), f19, f16, 0);
        } else {
            f = f8;
            f2 = f7;
            f3 = round;
            if (this.mState == 2) {
                float f20 = 0.32916668f * width;
                f4 = f2;
                this.mFullPath.moveTo(f4, f);
                float f21 = -f20;
                this.mFullPath.rLineTo(f21, 0.0f);
                this.mFullPath.rLineTo(0.0f, f21);
                this.mFullPath.rLineTo(f20, 0.0f);
                this.mFullPath.rLineTo(0.0f, f20);
                if (this.mState != 1) {
                    this.mVirtualTop.set(f4, (f10 + this.mAppliedCornerInset) - (INV_TAN * f5));
                    this.mVirtualLeft.set((f10 + this.mAppliedCornerInset) - (INV_TAN * f5), f);
                    float f22 = 0.083333336f * height;
                    float f23 = INV_TAN * f22;
                    this.mCutPath.reset();
                    this.mCutPath.setFillType(Path.FillType.WINDING);
                    float f24 = f4 - f22;
                    float f25 = f - f22;
                    this.mCutPath.moveTo(f24, f25);
                    this.mCutPath.lineTo(f24, this.mVirtualTop.y + f23);
                    this.mCutPath.lineTo(this.mVirtualLeft.x + f23, f25);
                    this.mCutPath.lineTo(f24, f25);
                    this.mForegroundPath.reset();
                    this.mFullPath.op(this.mCutPath, Path.Op.DIFFERENCE);
                } else if (this.mState == 4) {
                    this.mForegroundPath.reset();
                    this.mSlash.draw((int) height, (int) width, canvas, this.mPaint);
                } else if (this.mState != 3) {
                    this.mForegroundPath.reset();
                    this.mForegroundPath.addRect(f3, f3, f3 + Math.round(calcFit(this.mLevel / (this.mNumLevels - 1.0f)) * (width - (2.0f * f3))), f, Path.Direction.CW);
                    this.mForegroundPath.op(this.mFullPath, Path.Op.INTERSECT);
                }
                canvas.drawPath(this.mFullPath, this.mPaint);
                canvas.drawPath(this.mForegroundPath, this.mForegroundPaint);
                if (this.mState == 2) {
                    this.mXPath.reset();
                    this.mXPath.moveTo(X_PATH[0][0] * width, X_PATH[0][1] * height);
                    for (int i = 1; i < X_PATH.length; i++) {
                        this.mXPath.rLineTo(X_PATH[i][0] * width, X_PATH[i][1] * height);
                    }
                    canvas.drawPath(this.mXPath, this.mForegroundPaint);
                }
                if (!z) {
                    canvas.restore();
                    return;
                }
                return;
            }
        }
        f4 = f2;
        if (this.mState != 1) {
        }
        canvas.drawPath(this.mFullPath, this.mPaint);
        canvas.drawPath(this.mForegroundPath, this.mForegroundPaint);
        if (this.mState == 2) {
        }
        if (!z) {
        }
    }

    private void drawDot(Path path, Path path2, float f, float f2, float f3, int i) {
        (i == this.mCurrentDot ? path2 : path).addRect(f, f2, f + f3, f2 + f3, Path.Direction.CW);
    }

    private float calcFit(float f) {
        float f2 = 0.0f;
        float f3 = f;
        for (int i = 0; i < FIT.length; i++) {
            f2 += FIT[i] * f3;
            f3 *= f;
        }
        return f2;
    }

    @Override // android.graphics.drawable.Drawable
    public int getAlpha() {
        return this.mPaint.getAlpha();
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int i) {
        this.mPaint.setAlpha(i);
        this.mForegroundPaint.setAlpha(i);
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter colorFilter) {
        this.mPaint.setColorFilter(colorFilter);
        this.mForegroundPaint.setColorFilter(colorFilter);
    }

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        return 255;
    }

    @Override // android.graphics.drawable.Drawable
    public boolean setVisible(boolean z, boolean z2) {
        this.mVisible = z;
        updateAnimation();
        return super.setVisible(z, z2);
    }

    public static int getLevel(int i) {
        return i & 255;
    }

    public static int getState(int i) {
        return (i & 16711680) >> 16;
    }

    public static int getNumLevels(int i) {
        return (i & 65280) >> 8;
    }

    public static int getState(int i, int i2, boolean z) {
        return i | (i2 << 8) | ((z ? 2 : 0) << 16);
    }

    public static int getCarrierChangeState(int i) {
        return (i << 8) | 196608;
    }

    public static int getEmptyState(int i) {
        return (i << 8) | 65536;
    }

    public static int getAirplaneModeState(int i) {
        return (i << 8) | 262144;
    }

    /* loaded from: classes.dex */
    private final class SlashArtist {
        private final Path mPath;
        private final RectF mSlashRect;

        private SlashArtist() {
            this.mPath = new Path();
            this.mSlashRect = new RectF();
        }

        void draw(int i, int i2, Canvas canvas, Paint paint) {
            Matrix matrix = new Matrix();
            float scale = scale(1.0f, i2);
            updateRect(scale(0.40544835f, i2), scale(0.20288496f, i), scale(0.4820516f, i2), scale(1.1195517f, i));
            this.mPath.reset();
            this.mPath.addRoundRect(this.mSlashRect, scale, scale, Path.Direction.CW);
            float f = i2 / 2;
            float f2 = i / 2;
            matrix.setRotate(-45.0f, f, f2);
            this.mPath.transform(matrix);
            canvas.drawPath(this.mPath, paint);
            matrix.setRotate(45.0f, f, f2);
            this.mPath.transform(matrix);
            matrix.setTranslate(this.mSlashRect.width(), 0.0f);
            this.mPath.transform(matrix);
            this.mPath.addRoundRect(this.mSlashRect, scale, scale, Path.Direction.CW);
            matrix.setRotate(-45.0f, f, f2);
            this.mPath.transform(matrix);
            canvas.clipOutPath(this.mPath);
        }

        void updateRect(float f, float f2, float f3, float f4) {
            this.mSlashRect.left = f;
            this.mSlashRect.top = f2;
            this.mSlashRect.right = f3;
            this.mSlashRect.bottom = f4;
        }

        private float scale(float f, int i) {
            return f * i;
        }
    }
}
