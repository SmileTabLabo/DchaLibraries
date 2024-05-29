package com.android.systemui;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import com.android.systemui.statusbar.policy.BatteryController;
/* loaded from: a.zip:com/android/systemui/BatteryMeterDrawable.class */
public class BatteryMeterDrawable extends Drawable implements BatteryController.BatteryStateChangeCallback {
    public static final String TAG = BatteryMeterDrawable.class.getSimpleName();
    private BatteryController mBatteryController;
    private final Paint mBatteryPaint;
    private final Paint mBoltPaint;
    private final float[] mBoltPoints;
    private float mButtonHeightFraction;
    private int mChargeColor;
    private boolean mCharging;
    private final int[] mColors;
    private final Context mContext;
    private final int mCriticalLevel;
    private int mDarkModeBackgroundColor;
    private int mDarkModeFillColor;
    private final Paint mFramePaint;
    private final Handler mHandler;
    private int mHeight;
    private final int mIntrinsicHeight;
    private final int mIntrinsicWidth;
    private int mLightModeBackgroundColor;
    private int mLightModeFillColor;
    private boolean mListening;
    private boolean mPluggedIn;
    private final Paint mPlusPaint;
    private final float[] mPlusPoints;
    private boolean mPowerSaveEnabled;
    private boolean mShowPercent;
    private float mSubpixelSmoothingLeft;
    private float mSubpixelSmoothingRight;
    private float mTextHeight;
    private final Paint mTextPaint;
    private String mWarningString;
    private float mWarningTextHeight;
    private final Paint mWarningTextPaint;
    private int mWidth;
    private int mIconTint = -1;
    private float mOldDarkIntensity = 0.0f;
    private final Path mBoltPath = new Path();
    private final Path mPlusPath = new Path();
    private final RectF mFrame = new RectF();
    private final RectF mButtonFrame = new RectF();
    private final RectF mBoltFrame = new RectF();
    private final RectF mPlusFrame = new RectF();
    private final Path mShapePath = new Path();
    private final Path mClipPath = new Path();
    private final Path mTextPath = new Path();
    private final SettingObserver mSettingObserver = new SettingObserver(this);
    private int mLevel = -1;

    /* loaded from: a.zip:com/android/systemui/BatteryMeterDrawable$SettingObserver.class */
    private final class SettingObserver extends ContentObserver {
        final BatteryMeterDrawable this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public SettingObserver(BatteryMeterDrawable batteryMeterDrawable) {
            super(new Handler());
            this.this$0 = batteryMeterDrawable;
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            super.onChange(z, uri);
            this.this$0.updateShowPercent();
            this.this$0.postInvalidate();
        }
    }

    public BatteryMeterDrawable(Context context, Handler handler, int i) {
        this.mContext = context;
        this.mHandler = handler;
        Resources resources = context.getResources();
        TypedArray obtainTypedArray = resources.obtainTypedArray(2131427366);
        TypedArray obtainTypedArray2 = resources.obtainTypedArray(2131427367);
        int length = obtainTypedArray.length();
        this.mColors = new int[length * 2];
        for (int i2 = 0; i2 < length; i2++) {
            this.mColors[i2 * 2] = obtainTypedArray.getInt(i2, 0);
            this.mColors[(i2 * 2) + 1] = obtainTypedArray2.getColor(i2, 0);
        }
        obtainTypedArray.recycle();
        obtainTypedArray2.recycle();
        updateShowPercent();
        this.mWarningString = context.getString(2131493596);
        this.mCriticalLevel = this.mContext.getResources().getInteger(17694798);
        this.mButtonHeightFraction = context.getResources().getFraction(2131820556, 1, 1);
        this.mSubpixelSmoothingLeft = context.getResources().getFraction(2131820557, 1, 1);
        this.mSubpixelSmoothingRight = context.getResources().getFraction(2131820558, 1, 1);
        this.mFramePaint = new Paint(1);
        this.mFramePaint.setColor(i);
        this.mFramePaint.setDither(true);
        this.mFramePaint.setStrokeWidth(0.0f);
        this.mFramePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        this.mBatteryPaint = new Paint(1);
        this.mBatteryPaint.setDither(true);
        this.mBatteryPaint.setStrokeWidth(0.0f);
        this.mBatteryPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        this.mTextPaint = new Paint(1);
        this.mTextPaint.setTypeface(Typeface.create("sans-serif-condensed", 1));
        this.mTextPaint.setTextAlign(Paint.Align.CENTER);
        this.mWarningTextPaint = new Paint(1);
        this.mWarningTextPaint.setColor(this.mColors[1]);
        this.mWarningTextPaint.setTypeface(Typeface.create("sans-serif", 1));
        this.mWarningTextPaint.setTextAlign(Paint.Align.CENTER);
        this.mChargeColor = context.getColor(2131558515);
        this.mBoltPaint = new Paint(1);
        this.mBoltPaint.setColor(context.getColor(2131558516));
        this.mBoltPoints = loadBoltPoints(resources);
        this.mPlusPaint = new Paint(this.mBoltPaint);
        this.mPlusPoints = loadPlusPoints(resources);
        this.mDarkModeBackgroundColor = context.getColor(2131558576);
        this.mDarkModeFillColor = context.getColor(2131558577);
        this.mLightModeBackgroundColor = context.getColor(2131558579);
        this.mLightModeFillColor = context.getColor(2131558580);
        this.mIntrinsicWidth = context.getResources().getDimensionPixelSize(2131689983);
        this.mIntrinsicHeight = context.getResources().getDimensionPixelSize(2131689982);
    }

    private int getBackgroundColor(float f) {
        return getColorForDarkIntensity(f, this.mLightModeBackgroundColor, this.mDarkModeBackgroundColor);
    }

    private int getColorForDarkIntensity(float f, int i, int i2) {
        return ((Integer) ArgbEvaluator.getInstance().evaluate(f, Integer.valueOf(i), Integer.valueOf(i2))).intValue();
    }

    private int getColorForLevel(int i) {
        if (this.mPowerSaveEnabled) {
            return this.mColors[this.mColors.length - 1];
        }
        int i2 = 0;
        int i3 = 0;
        while (i3 < this.mColors.length) {
            int i4 = this.mColors[i3];
            i2 = this.mColors[i3 + 1];
            if (i <= i4) {
                return i3 == this.mColors.length - 2 ? this.mIconTint : i2;
            }
            i3 += 2;
        }
        return i2;
    }

    private int getFillColor(float f) {
        return getColorForDarkIntensity(f, this.mLightModeFillColor, this.mDarkModeFillColor);
    }

    private static float[] loadBoltPoints(Resources resources) {
        int[] intArray = resources.getIntArray(2131427368);
        int i = 0;
        int i2 = 0;
        for (int i3 = 0; i3 < intArray.length; i3 += 2) {
            i = Math.max(i, intArray[i3]);
            i2 = Math.max(i2, intArray[i3 + 1]);
        }
        float[] fArr = new float[intArray.length];
        for (int i4 = 0; i4 < intArray.length; i4 += 2) {
            fArr[i4] = intArray[i4] / i;
            fArr[i4 + 1] = intArray[i4 + 1] / i2;
        }
        return fArr;
    }

    private static float[] loadPlusPoints(Resources resources) {
        int[] intArray = resources.getIntArray(2131427369);
        int i = 0;
        int i2 = 0;
        for (int i3 = 0; i3 < intArray.length; i3 += 2) {
            i = Math.max(i, intArray[i3]);
            i2 = Math.max(i2, intArray[i3 + 1]);
        }
        float[] fArr = new float[intArray.length];
        for (int i4 = 0; i4 < intArray.length; i4 += 2) {
            fArr[i4] = intArray[i4] / i;
            fArr[i4 + 1] = intArray[i4 + 1] / i2;
        }
        return fArr;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void postInvalidate() {
        this.mHandler.post(new Runnable(this) { // from class: com.android.systemui.BatteryMeterDrawable.1
            final BatteryMeterDrawable this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.invalidateSelf();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateShowPercent() {
        boolean z = false;
        if (Settings.System.getInt(this.mContext.getContentResolver(), "status_bar_show_battery_percent", 0) != 0) {
            z = true;
        }
        this.mShowPercent = z;
    }

    public void disableShowPercent() {
        this.mShowPercent = false;
        postInvalidate();
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        int i = this.mLevel;
        if (i == -1) {
            return;
        }
        float f = i / 100.0f;
        int i2 = this.mHeight;
        int i3 = (int) (this.mHeight * 0.6551724f);
        int i4 = (this.mWidth - i3) / 2;
        int i5 = (int) (i2 * this.mButtonHeightFraction);
        this.mFrame.set(0.0f, 0.0f, i3, i2);
        this.mFrame.offset(i4, 0.0f);
        this.mButtonFrame.set(this.mFrame.left + Math.round(i3 * 0.25f), this.mFrame.top, this.mFrame.right - Math.round(i3 * 0.25f), this.mFrame.top + i5);
        this.mButtonFrame.top += this.mSubpixelSmoothingLeft;
        this.mButtonFrame.left += this.mSubpixelSmoothingLeft;
        this.mButtonFrame.right -= this.mSubpixelSmoothingRight;
        this.mFrame.top += i5;
        this.mFrame.left += this.mSubpixelSmoothingLeft;
        this.mFrame.top += this.mSubpixelSmoothingLeft;
        this.mFrame.right -= this.mSubpixelSmoothingRight;
        this.mFrame.bottom -= this.mSubpixelSmoothingRight;
        this.mBatteryPaint.setColor(this.mPluggedIn ? this.mChargeColor : getColorForLevel(i));
        if (i >= 96) {
            f = 1.0f;
        } else if (i <= this.mCriticalLevel) {
            f = 0.0f;
        }
        float height = f == 1.0f ? this.mButtonFrame.top : this.mFrame.top + (this.mFrame.height() * (1.0f - f));
        this.mShapePath.reset();
        this.mShapePath.moveTo(this.mButtonFrame.left, this.mButtonFrame.top);
        this.mShapePath.lineTo(this.mButtonFrame.right, this.mButtonFrame.top);
        this.mShapePath.lineTo(this.mButtonFrame.right, this.mFrame.top);
        this.mShapePath.lineTo(this.mFrame.right, this.mFrame.top);
        this.mShapePath.lineTo(this.mFrame.right, this.mFrame.bottom);
        this.mShapePath.lineTo(this.mFrame.left, this.mFrame.bottom);
        this.mShapePath.lineTo(this.mFrame.left, this.mFrame.top);
        this.mShapePath.lineTo(this.mButtonFrame.left, this.mFrame.top);
        this.mShapePath.lineTo(this.mButtonFrame.left, this.mButtonFrame.top);
        if (this.mPluggedIn && this.mCharging) {
            float width = this.mFrame.left + (this.mFrame.width() / 4.0f);
            float height2 = this.mFrame.top + (this.mFrame.height() / 6.0f);
            float width2 = this.mFrame.right - (this.mFrame.width() / 4.0f);
            float height3 = this.mFrame.bottom - (this.mFrame.height() / 10.0f);
            if (this.mBoltFrame.left != width || this.mBoltFrame.top != height2 || this.mBoltFrame.right != width2 || this.mBoltFrame.bottom != height3) {
                this.mBoltFrame.set(width, height2, width2, height3);
                this.mBoltPath.reset();
                this.mBoltPath.moveTo(this.mBoltFrame.left + (this.mBoltPoints[0] * this.mBoltFrame.width()), this.mBoltFrame.top + (this.mBoltPoints[1] * this.mBoltFrame.height()));
                for (int i6 = 2; i6 < this.mBoltPoints.length; i6 += 2) {
                    this.mBoltPath.lineTo(this.mBoltFrame.left + (this.mBoltPoints[i6] * this.mBoltFrame.width()), this.mBoltFrame.top + (this.mBoltPoints[i6 + 1] * this.mBoltFrame.height()));
                }
                this.mBoltPath.lineTo(this.mBoltFrame.left + (this.mBoltPoints[0] * this.mBoltFrame.width()), this.mBoltFrame.top + (this.mBoltPoints[1] * this.mBoltFrame.height()));
            }
            if (Math.min(Math.max((this.mBoltFrame.bottom - height) / (this.mBoltFrame.bottom - this.mBoltFrame.top), 0.0f), 1.0f) <= 0.3f) {
                canvas.drawPath(this.mBoltPath, this.mBoltPaint);
            } else {
                this.mShapePath.op(this.mBoltPath, Path.Op.DIFFERENCE);
            }
        } else if (this.mPowerSaveEnabled) {
            float width3 = (this.mFrame.width() * 2.0f) / 3.0f;
            float width4 = this.mFrame.left + ((this.mFrame.width() - width3) / 2.0f);
            float height4 = this.mFrame.top + ((this.mFrame.height() - width3) / 2.0f);
            float width5 = this.mFrame.right - ((this.mFrame.width() - width3) / 2.0f);
            float height5 = this.mFrame.bottom - ((this.mFrame.height() - width3) / 2.0f);
            if (this.mPlusFrame.left != width4 || this.mPlusFrame.top != height4 || this.mPlusFrame.right != width5 || this.mPlusFrame.bottom != height5) {
                this.mPlusFrame.set(width4, height4, width5, height5);
                this.mPlusPath.reset();
                this.mPlusPath.moveTo(this.mPlusFrame.left + (this.mPlusPoints[0] * this.mPlusFrame.width()), this.mPlusFrame.top + (this.mPlusPoints[1] * this.mPlusFrame.height()));
                for (int i7 = 2; i7 < this.mPlusPoints.length; i7 += 2) {
                    this.mPlusPath.lineTo(this.mPlusFrame.left + (this.mPlusPoints[i7] * this.mPlusFrame.width()), this.mPlusFrame.top + (this.mPlusPoints[i7 + 1] * this.mPlusFrame.height()));
                }
                this.mPlusPath.lineTo(this.mPlusFrame.left + (this.mPlusPoints[0] * this.mPlusFrame.width()), this.mPlusFrame.top + (this.mPlusPoints[1] * this.mPlusFrame.height()));
            }
            if (Math.min(Math.max((this.mPlusFrame.bottom - height) / (this.mPlusFrame.bottom - this.mPlusFrame.top), 0.0f), 1.0f) <= 0.3f) {
                canvas.drawPath(this.mPlusPath, this.mPlusPaint);
            } else {
                this.mShapePath.op(this.mPlusPath, Path.Op.DIFFERENCE);
            }
        }
        String str = null;
        float f2 = 0.0f;
        float f3 = 0.0f;
        boolean z = false;
        if (!this.mPluggedIn) {
            if (this.mPowerSaveEnabled) {
                z = false;
                f3 = 0.0f;
                f2 = 0.0f;
                str = null;
            } else {
                str = null;
                f2 = 0.0f;
                f3 = 0.0f;
                z = false;
                if (i > this.mCriticalLevel) {
                    str = null;
                    f2 = 0.0f;
                    f3 = 0.0f;
                    z = false;
                    if (this.mShowPercent) {
                        this.mTextPaint.setColor(getColorForLevel(i));
                        this.mTextPaint.setTextSize((this.mLevel == 100 ? 0.38f : 0.5f) * i2);
                        this.mTextHeight = -this.mTextPaint.getFontMetrics().ascent;
                        String valueOf = String.valueOf(i);
                        float f4 = this.mWidth * 0.5f;
                        float f5 = (this.mHeight + this.mTextHeight) * 0.47f;
                        boolean z2 = height > f5;
                        str = valueOf;
                        f2 = f4;
                        f3 = f5;
                        z = z2;
                        if (!z2) {
                            this.mTextPath.reset();
                            this.mTextPaint.getTextPath(valueOf, 0, valueOf.length(), f4, f5, this.mTextPath);
                            this.mShapePath.op(this.mTextPath, Path.Op.DIFFERENCE);
                            str = valueOf;
                            f2 = f4;
                            f3 = f5;
                            z = z2;
                        }
                    }
                }
            }
        }
        canvas.drawPath(this.mShapePath, this.mFramePaint);
        this.mFrame.top = height;
        this.mClipPath.reset();
        this.mClipPath.addRect(this.mFrame, Path.Direction.CCW);
        this.mShapePath.op(this.mClipPath, Path.Op.INTERSECT);
        canvas.drawPath(this.mShapePath, this.mBatteryPaint);
        if (this.mPluggedIn || this.mPowerSaveEnabled) {
            return;
        }
        if (i <= this.mCriticalLevel) {
            canvas.drawText(this.mWarningString, this.mWidth * 0.5f, (this.mHeight + this.mWarningTextHeight) * 0.48f, this.mWarningTextPaint);
        } else if (z) {
            canvas.drawText(str, f2, f3, this.mTextPaint);
        }
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicHeight() {
        return this.mIntrinsicHeight;
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicWidth() {
        return this.mIntrinsicWidth;
    }

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        return 0;
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onBatteryLevelChanged(int i, boolean z, boolean z2) {
        this.mLevel = i;
        this.mPluggedIn = z;
        this.mCharging = z2;
        Log.d(TAG, "onBatteryLevelChanged level:" + i + ",plugedIn:" + z + ",charging:" + z2);
        postInvalidate();
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onPowerSaveChanged(boolean z) {
        this.mPowerSaveEnabled = z;
        invalidateSelf();
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int i) {
    }

    public void setBatteryController(BatteryController batteryController) {
        this.mBatteryController = batteryController;
        this.mPowerSaveEnabled = this.mBatteryController.isPowerSave();
    }

    @Override // android.graphics.drawable.Drawable
    public void setBounds(int i, int i2, int i3, int i4) {
        super.setBounds(i, i2, i3, i4);
        this.mHeight = i4 - i2;
        this.mWidth = i3 - i;
        this.mWarningTextPaint.setTextSize(this.mHeight * 0.75f);
        this.mWarningTextHeight = -this.mWarningTextPaint.getFontMetrics().ascent;
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter colorFilter) {
    }

    public void setDarkIntensity(float f) {
        if (f == this.mOldDarkIntensity) {
            return;
        }
        int backgroundColor = getBackgroundColor(f);
        int fillColor = getFillColor(f);
        this.mIconTint = fillColor;
        this.mFramePaint.setColor(backgroundColor);
        this.mBoltPaint.setColor(fillColor);
        this.mChargeColor = fillColor;
        invalidateSelf();
        this.mOldDarkIntensity = f;
    }

    public void startListening() {
        this.mListening = true;
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("status_bar_show_battery_percent"), false, this.mSettingObserver);
        updateShowPercent();
        this.mBatteryController.addStateChangedCallback(this);
    }

    public void stopListening() {
        this.mListening = false;
        this.mContext.getContentResolver().unregisterContentObserver(this.mSettingObserver);
        this.mBatteryController.removeStateChangedCallback(this);
    }
}
