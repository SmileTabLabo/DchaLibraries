package com.android.launcher3;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;
import android.view.animation.DecelerateInterpolator;
/* loaded from: a.zip:com/android/launcher3/FastBitmapDrawable.class */
public class FastBitmapDrawable extends Drawable {

    /* renamed from: -com-android-launcher3-FastBitmapDrawable$StateSwitchesValues  reason: not valid java name */
    private static final int[] f2comandroidlauncher3FastBitmapDrawable$StateSwitchesValues = null;
    public static final TimeInterpolator CLICK_FEEDBACK_INTERPOLATOR = new TimeInterpolator() { // from class: com.android.launcher3.FastBitmapDrawable.1
        @Override // android.animation.TimeInterpolator
        public float getInterpolation(float f) {
            if (f < 0.05f) {
                return f / 0.05f;
            }
            if (f < 0.3f) {
                return 1.0f;
            }
            return (1.0f - f) / 0.7f;
        }
    };
    private static final SparseArray<ColorFilter> sCachedFilter = new SparseArray<>();
    private static final ColorMatrix sTempBrightnessMatrix = new ColorMatrix();
    private static final ColorMatrix sTempFilterMatrix = new ColorMatrix();
    private final Bitmap mBitmap;
    private AnimatorSet mPropertyAnimator;
    private final Paint mPaint = new Paint(2);
    private State mState = State.NORMAL;
    private int mDesaturation = 0;
    private int mBrightness = 0;
    private int mAlpha = 255;
    private int mPrevUpdateKey = Integer.MAX_VALUE;

    /* loaded from: a.zip:com/android/launcher3/FastBitmapDrawable$State.class */
    public enum State {
        NORMAL(0.0f, 0.0f, 1.0f, new DecelerateInterpolator()),
        PRESSED(0.0f, 0.39215687f, 1.0f, FastBitmapDrawable.CLICK_FEEDBACK_INTERPOLATOR),
        FAST_SCROLL_HIGHLIGHTED(0.0f, 0.0f, 1.15f, new DecelerateInterpolator()),
        FAST_SCROLL_UNHIGHLIGHTED(0.0f, 0.0f, 1.0f, new DecelerateInterpolator()),
        DISABLED(1.0f, 0.5f, 1.0f, new DecelerateInterpolator());
        
        public final float brightness;
        public final float desaturation;
        public final TimeInterpolator interpolator;
        public final float viewScale;

        State(float f, float f2, float f3, TimeInterpolator timeInterpolator) {
            this.desaturation = f;
            this.brightness = f2;
            this.viewScale = f3;
            this.interpolator = timeInterpolator;
        }

        /* renamed from: values  reason: to resolve conflict with enum method */
        public static State[] valuesCustom() {
            return values();
        }
    }

    /* renamed from: -getcom-android-launcher3-FastBitmapDrawable$StateSwitchesValues  reason: not valid java name */
    private static /* synthetic */ int[] m164getcomandroidlauncher3FastBitmapDrawable$StateSwitchesValues() {
        if (f2comandroidlauncher3FastBitmapDrawable$StateSwitchesValues != null) {
            return f2comandroidlauncher3FastBitmapDrawable$StateSwitchesValues;
        }
        int[] iArr = new int[State.valuesCustom().length];
        try {
            iArr[State.DISABLED.ordinal()] = 5;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[State.FAST_SCROLL_HIGHLIGHTED.ordinal()] = 1;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[State.FAST_SCROLL_UNHIGHLIGHTED.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[State.NORMAL.ordinal()] = 3;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[State.PRESSED.ordinal()] = 4;
        } catch (NoSuchFieldError e5) {
        }
        f2comandroidlauncher3FastBitmapDrawable$StateSwitchesValues = iArr;
        return iArr;
    }

    public FastBitmapDrawable(Bitmap bitmap) {
        this.mBitmap = bitmap;
        setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
    }

    private AnimatorSet cancelAnimator(AnimatorSet animatorSet) {
        if (animatorSet != null) {
            animatorSet.removeAllListeners();
            animatorSet.cancel();
            return null;
        }
        return null;
    }

    public static int getDurationForStateChange(State state, State state2) {
        switch (m164getcomandroidlauncher3FastBitmapDrawable$StateSwitchesValues()[state2.ordinal()]) {
            case 1:
                return 225;
            case 2:
                switch (m164getcomandroidlauncher3FastBitmapDrawable$StateSwitchesValues()[state.ordinal()]) {
                    case 3:
                        return 225;
                    default:
                        return 150;
                }
            case 3:
                switch (m164getcomandroidlauncher3FastBitmapDrawable$StateSwitchesValues()[state.ordinal()]) {
                    case 1:
                    case 2:
                        return 275;
                    case 3:
                    default:
                        return 2000;
                    case 4:
                        return 0;
                }
            case 4:
                return 2000;
            default:
                return 0;
        }
    }

    public static int getStartDelayForStateChange(State state, State state2) {
        switch (m164getcomandroidlauncher3FastBitmapDrawable$StateSwitchesValues()[state2.ordinal()]) {
            case 2:
                switch (m164getcomandroidlauncher3FastBitmapDrawable$StateSwitchesValues()[state.ordinal()]) {
                    case 3:
                        return 37;
                    default:
                        return 0;
                }
            default:
                return 0;
        }
    }

    private void updateFilter() {
        boolean z = false;
        int i = -1;
        if (this.mDesaturation > 0) {
            i = (this.mDesaturation << 16) | this.mBrightness;
        } else if (this.mBrightness > 0) {
            i = 65536 | this.mBrightness;
            z = true;
        }
        if (i == this.mPrevUpdateKey) {
            return;
        }
        this.mPrevUpdateKey = i;
        if (i != -1) {
            ColorFilter colorFilter = sCachedFilter.get(i);
            PorterDuffColorFilter porterDuffColorFilter = colorFilter;
            if (colorFilter == null) {
                float brightness = getBrightness();
                int i2 = (int) (255.0f * brightness);
                if (z) {
                    porterDuffColorFilter = new PorterDuffColorFilter(Color.argb(i2, 255, 255, 255), PorterDuff.Mode.SRC_ATOP);
                } else {
                    sTempFilterMatrix.setSaturation(1.0f - getDesaturation());
                    if (this.mBrightness > 0) {
                        float f = 1.0f - brightness;
                        float[] array = sTempBrightnessMatrix.getArray();
                        array[0] = f;
                        array[6] = f;
                        array[12] = f;
                        array[4] = i2;
                        array[9] = i2;
                        array[14] = i2;
                        sTempFilterMatrix.preConcat(sTempBrightnessMatrix);
                    }
                    porterDuffColorFilter = new ColorMatrixColorFilter(sTempFilterMatrix);
                }
                sCachedFilter.append(i, porterDuffColorFilter);
            }
            this.mPaint.setColorFilter(porterDuffColorFilter);
        } else {
            this.mPaint.setColorFilter(null);
        }
        invalidateSelf();
    }

    public boolean animateState(State state) {
        State state2 = this.mState;
        if (this.mState != state) {
            this.mState = state;
            this.mPropertyAnimator = cancelAnimator(this.mPropertyAnimator);
            this.mPropertyAnimator = new AnimatorSet();
            this.mPropertyAnimator.playTogether(ObjectAnimator.ofFloat(this, "desaturation", state.desaturation), ObjectAnimator.ofFloat(this, "brightness", state.brightness));
            this.mPropertyAnimator.setInterpolator(state.interpolator);
            this.mPropertyAnimator.setDuration(getDurationForStateChange(state2, state));
            this.mPropertyAnimator.setStartDelay(getStartDelayForStateChange(state2, state));
            this.mPropertyAnimator.start();
            return true;
        }
        return false;
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        canvas.drawBitmap(this.mBitmap, (Rect) null, getBounds(), this.mPaint);
    }

    @Override // android.graphics.drawable.Drawable
    public int getAlpha() {
        return this.mAlpha;
    }

    public Bitmap getBitmap() {
        return this.mBitmap;
    }

    public float getBrightness() {
        return this.mBrightness / 48.0f;
    }

    public State getCurrentState() {
        return this.mState;
    }

    public float getDesaturation() {
        return this.mDesaturation / 48.0f;
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicHeight() {
        return this.mBitmap.getHeight();
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicWidth() {
        return this.mBitmap.getWidth();
    }

    @Override // android.graphics.drawable.Drawable
    public int getMinimumHeight() {
        return getBounds().height();
    }

    @Override // android.graphics.drawable.Drawable
    public int getMinimumWidth() {
        return getBounds().width();
    }

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        return -3;
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int i) {
        this.mAlpha = i;
        this.mPaint.setAlpha(i);
    }

    public void setBrightness(float f) {
        int floor = (int) Math.floor(48.0f * f);
        if (this.mBrightness != floor) {
            this.mBrightness = floor;
            updateFilter();
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter colorFilter) {
    }

    public void setDesaturation(float f) {
        int floor = (int) Math.floor(48.0f * f);
        if (this.mDesaturation != floor) {
            this.mDesaturation = floor;
            updateFilter();
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void setFilterBitmap(boolean z) {
        this.mPaint.setFilterBitmap(z);
        this.mPaint.setAntiAlias(z);
    }

    public boolean setState(State state) {
        if (this.mState != state) {
            this.mState = state;
            this.mPropertyAnimator = cancelAnimator(this.mPropertyAnimator);
            setDesaturation(state.desaturation);
            setBrightness(state.brightness);
            return true;
        }
        return false;
    }
}
