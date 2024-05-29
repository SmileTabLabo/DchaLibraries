package com.android.systemui.statusbar.phone;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.view.View;
import com.android.systemui.Interpolators;
import com.mediatek.systemui.statusbar.util.FeatureOptions;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/BarTransitions.class */
public class BarTransitions {
    public static final boolean HIGH_END = ActivityManager.isHighEndGfx();
    private boolean mAlwaysOpaque = false;
    private final BarBackgroundDrawable mBarBackground;
    private int mMode;
    private final String mTag;
    private final View mView;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/statusbar/phone/BarTransitions$BarBackgroundDrawable.class */
    public static class BarBackgroundDrawable extends Drawable {
        private boolean mAnimating;
        private int mColor;
        private int mColorStart;
        private long mEndTime;
        private final Drawable mGradient;
        private int mGradientAlpha;
        private int mGradientAlphaStart;
        private final int mOpaque;
        private final int mSemiTransparent;
        private long mStartTime;
        private PorterDuffColorFilter mTintFilter;
        private final int mTransparent;
        private final int mWarning;
        private int mMode = -1;
        private Paint mPaint = new Paint();

        public BarBackgroundDrawable(Context context, int i) {
            context.getResources();
            this.mOpaque = context.getColor(2131558510);
            this.mSemiTransparent = context.getColor(17170546);
            this.mTransparent = context.getColor(2131558511);
            this.mWarning = context.getColor(17170522);
            this.mGradient = context.getDrawable(i);
        }

        public void applyModeBackground(int i, int i2, boolean z) {
            if (this.mMode == i2) {
                return;
            }
            this.mMode = i2;
            this.mAnimating = z;
            if (z) {
                long elapsedRealtime = SystemClock.elapsedRealtime();
                this.mStartTime = elapsedRealtime;
                this.mEndTime = 200 + elapsedRealtime;
                this.mGradientAlphaStart = this.mGradientAlpha;
                this.mColorStart = this.mColor;
            }
            invalidateSelf();
        }

        @Override // android.graphics.drawable.Drawable
        public void draw(Canvas canvas) {
            int i = this.mMode == 5 ? this.mWarning : this.mMode == 2 ? this.mSemiTransparent : this.mMode == 1 ? this.mSemiTransparent : (this.mMode == 4 || this.mMode == 6) ? this.mTransparent : this.mOpaque;
            if (this.mAnimating) {
                long elapsedRealtime = SystemClock.elapsedRealtime();
                if (elapsedRealtime >= this.mEndTime) {
                    this.mAnimating = false;
                    this.mColor = i;
                    this.mGradientAlpha = 0;
                } else {
                    float max = Math.max(0.0f, Math.min(Interpolators.LINEAR.getInterpolation(((float) (elapsedRealtime - this.mStartTime)) / ((float) (this.mEndTime - this.mStartTime))), 1.0f));
                    this.mGradientAlpha = (int) ((max * 0.0f) + (this.mGradientAlphaStart * (1.0f - max)));
                    this.mColor = Color.argb((int) ((Color.alpha(i) * max) + (Color.alpha(this.mColorStart) * (1.0f - max))), (int) ((Color.red(i) * max) + (Color.red(this.mColorStart) * (1.0f - max))), (int) ((Color.green(i) * max) + (Color.green(this.mColorStart) * (1.0f - max))), (int) ((Color.blue(i) * max) + (Color.blue(this.mColorStart) * (1.0f - max))));
                }
            } else {
                this.mColor = i;
                this.mGradientAlpha = 0;
            }
            if (this.mGradientAlpha > 0) {
                this.mGradient.setAlpha(this.mGradientAlpha);
                this.mGradient.draw(canvas);
            }
            if (Color.alpha(this.mColor) > 0) {
                this.mPaint.setColor(this.mColor);
                if (this.mTintFilter != null) {
                    this.mPaint.setColorFilter(this.mTintFilter);
                }
                canvas.drawPaint(this.mPaint);
            }
            if (this.mAnimating) {
                invalidateSelf();
            }
        }

        public void finishAnimation() {
            if (this.mAnimating) {
                this.mAnimating = false;
                invalidateSelf();
            }
        }

        @Override // android.graphics.drawable.Drawable
        public int getOpacity() {
            return -3;
        }

        @Override // android.graphics.drawable.Drawable
        protected void onBoundsChange(Rect rect) {
            super.onBoundsChange(rect);
            this.mGradient.setBounds(rect);
        }

        @Override // android.graphics.drawable.Drawable
        public void setAlpha(int i) {
        }

        @Override // android.graphics.drawable.Drawable
        public void setColorFilter(ColorFilter colorFilter) {
        }

        @Override // android.graphics.drawable.Drawable
        public void setTint(int i) {
            if (this.mTintFilter == null) {
                this.mTintFilter = new PorterDuffColorFilter(i, PorterDuff.Mode.SRC_IN);
            } else {
                this.mTintFilter.setColor(i);
            }
            invalidateSelf();
        }

        @Override // android.graphics.drawable.Drawable
        public void setTintMode(PorterDuff.Mode mode) {
            if (this.mTintFilter == null) {
                this.mTintFilter = new PorterDuffColorFilter(0, mode);
            } else {
                this.mTintFilter.setMode(mode);
            }
            invalidateSelf();
        }
    }

    public BarTransitions(View view, int i) {
        this.mTag = "BarTransitions." + view.getClass().getSimpleName();
        this.mView = view;
        this.mBarBackground = new BarBackgroundDrawable(this.mView.getContext(), i);
        if (HIGH_END || FeatureOptions.LOW_RAM_SUPPORT) {
            this.mView.setBackground(this.mBarBackground);
        }
    }

    public static String modeToString(int i) {
        if (i == 0) {
            return "MODE_OPAQUE";
        }
        if (i == 1) {
            return "MODE_SEMI_TRANSPARENT";
        }
        if (i == 2) {
            return "MODE_TRANSLUCENT";
        }
        if (i == 3) {
            return "MODE_LIGHTS_OUT";
        }
        if (i == 4) {
            return "MODE_TRANSPARENT";
        }
        if (i == 5) {
            return "MODE_WARNING";
        }
        if (i == 6) {
            return "MODE_LIGHTS_OUT_TRANSPARENT";
        }
        throw new IllegalArgumentException("Unknown mode " + i);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void applyModeBackground(int i, int i2, boolean z) {
        this.mBarBackground.applyModeBackground(i, i2, z);
    }

    public void finishAnimations() {
        this.mBarBackground.finishAnimation();
    }

    public int getMode() {
        return this.mMode;
    }

    public boolean isAlwaysOpaque() {
        return HIGH_END ? this.mAlwaysOpaque : true;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isLightsOut(int i) {
        boolean z = true;
        if (i != 3) {
            z = i == 6;
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onTransition(int i, int i2, boolean z) {
        if (HIGH_END || FeatureOptions.LOW_RAM_SUPPORT) {
            applyModeBackground(i, i2, z);
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:22:0x0041, code lost:
        if (r6 == 4) goto L9;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void transitionTo(int i, boolean z) {
        int i2 = i;
        if (isAlwaysOpaque()) {
            if (i != 1 && i != 2) {
                i2 = i;
            }
            i2 = FeatureOptions.LOW_RAM_SUPPORT ? i : 0;
        }
        int i3 = i2;
        if (isAlwaysOpaque()) {
            i3 = i2;
            if (i2 == 6) {
                i3 = FeatureOptions.LOW_RAM_SUPPORT ? i2 : 3;
            }
        }
        if (this.mMode == i3) {
            return;
        }
        int i4 = this.mMode;
        this.mMode = i3;
        onTransition(i4, this.mMode, z);
    }
}
