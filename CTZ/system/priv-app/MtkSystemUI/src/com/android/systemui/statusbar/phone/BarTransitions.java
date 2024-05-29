package com.android.systemui.statusbar.phone;

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
import com.android.settingslib.Utils;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
/* loaded from: classes.dex */
public class BarTransitions {
    private boolean mAlwaysOpaque = false;
    private final BarBackgroundDrawable mBarBackground;
    private int mMode;
    private final String mTag;
    private final View mView;

    public BarTransitions(View view, int i) {
        this.mTag = "BarTransitions." + view.getClass().getSimpleName();
        this.mView = view;
        this.mBarBackground = new BarBackgroundDrawable(this.mView.getContext(), i);
        this.mView.setBackground(this.mBarBackground);
    }

    public int getMode() {
        return this.mMode;
    }

    public void setAutoDim(boolean z) {
    }

    public boolean isAlwaysOpaque() {
        return this.mAlwaysOpaque;
    }

    public void transitionTo(int i, boolean z) {
        if (isAlwaysOpaque() && (i == 1 || i == 2 || i == 4)) {
            i = 0;
        }
        if (isAlwaysOpaque() && i == 6) {
            i = 3;
        }
        if (this.mMode == i) {
            return;
        }
        int i2 = this.mMode;
        this.mMode = i;
        onTransition(i2, this.mMode, z);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onTransition(int i, int i2, boolean z) {
        applyModeBackground(i, i2, z);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void applyModeBackground(int i, int i2, boolean z) {
        this.mBarBackground.applyModeBackground(i, i2, z);
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

    public void finishAnimations() {
        this.mBarBackground.finishAnimation();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isLightsOut(int i) {
        return i == 3 || i == 6;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
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
            this.mOpaque = context.getColor(R.color.system_bar_background_opaque);
            this.mSemiTransparent = context.getColor(17170773);
            this.mTransparent = context.getColor(R.color.system_bar_background_transparent);
            this.mWarning = Utils.getColorAttr(context, 16844099);
            this.mGradient = context.getDrawable(i);
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

        @Override // android.graphics.drawable.Drawable
        protected void onBoundsChange(Rect rect) {
            super.onBoundsChange(rect);
            this.mGradient.setBounds(rect);
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
                this.mEndTime = elapsedRealtime + 200;
                this.mGradientAlphaStart = this.mGradientAlpha;
                this.mColorStart = this.mColor;
            }
            invalidateSelf();
        }

        @Override // android.graphics.drawable.Drawable
        public int getOpacity() {
            return -3;
        }

        public void finishAnimation() {
            if (this.mAnimating) {
                this.mAnimating = false;
                invalidateSelf();
            }
        }

        @Override // android.graphics.drawable.Drawable
        public void draw(Canvas canvas) {
            int i;
            if (this.mMode == 5) {
                i = this.mWarning;
            } else if (this.mMode == 2) {
                i = this.mSemiTransparent;
            } else if (this.mMode == 1) {
                i = this.mSemiTransparent;
            } else if (this.mMode == 4 || this.mMode == 6) {
                i = this.mTransparent;
            } else {
                i = this.mOpaque;
            }
            if (!this.mAnimating) {
                this.mColor = i;
                this.mGradientAlpha = 0;
            } else {
                long elapsedRealtime = SystemClock.elapsedRealtime();
                if (elapsedRealtime >= this.mEndTime) {
                    this.mAnimating = false;
                    this.mColor = i;
                    this.mGradientAlpha = 0;
                } else {
                    float max = Math.max(0.0f, Math.min(Interpolators.LINEAR.getInterpolation(((float) (elapsedRealtime - this.mStartTime)) / ((float) (this.mEndTime - this.mStartTime))), 1.0f));
                    float f = 1.0f - max;
                    this.mGradientAlpha = (int) ((0 * max) + (this.mGradientAlphaStart * f));
                    this.mColor = Color.argb((int) ((Color.alpha(i) * max) + (Color.alpha(this.mColorStart) * f)), (int) ((Color.red(i) * max) + (Color.red(this.mColorStart) * f)), (int) ((Color.green(i) * max) + (Color.green(this.mColorStart) * f)), (int) ((max * Color.blue(i)) + (Color.blue(this.mColorStart) * f)));
                }
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
    }
}
