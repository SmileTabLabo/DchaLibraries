package com.android.launcher3;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import com.android.launcher3.FastBitmapDrawable;
/* loaded from: a.zip:com/android/launcher3/PendingAppWidgetHostView.class */
public class PendingAppWidgetHostView extends LauncherAppWidgetHostView implements View.OnClickListener {
    private static Resources.Theme sPreloaderTheme;
    private Drawable mCenterDrawable;
    private View.OnClickListener mClickListener;
    private View mDefaultView;
    private final boolean mDisabledForSafeMode;
    private boolean mDrawableSizeChanged;
    private Bitmap mIcon;
    private final Intent mIconLookupIntent;
    private final LauncherAppWidgetInfo mInfo;
    private Launcher mLauncher;
    private final TextPaint mPaint;
    private final Rect mRect;
    private Drawable mSettingIconDrawable;
    private Layout mSetupTextLayout;
    private final int mStartState;

    @TargetApi(21)
    public PendingAppWidgetHostView(Context context, LauncherAppWidgetInfo launcherAppWidgetInfo, boolean z) {
        super(context);
        this.mRect = new Rect();
        this.mLauncher = (Launcher) context;
        this.mInfo = launcherAppWidgetInfo;
        this.mStartState = launcherAppWidgetInfo.restoreStatus;
        this.mIconLookupIntent = new Intent().setComponent(launcherAppWidgetInfo.providerName);
        this.mDisabledForSafeMode = z;
        this.mPaint = new TextPaint();
        this.mPaint.setColor(-1);
        this.mPaint.setTextSize(TypedValue.applyDimension(0, this.mLauncher.getDeviceProfile().iconTextSizePx, getResources().getDisplayMetrics()));
        setBackgroundResource(2130837551);
        setWillNotDraw(false);
        if (Utilities.ATLEAST_LOLLIPOP) {
            setElevation(getResources().getDimension(2131230811));
        }
    }

    private void updateDrawableBounds() {
        DeviceProfile deviceProfile = this.mLauncher.getDeviceProfile();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int dimensionPixelSize = getResources().getDimensionPixelSize(2131230810);
        int width = ((getWidth() - paddingLeft) - paddingRight) - (dimensionPixelSize * 2);
        int height = ((getHeight() - paddingTop) - paddingBottom) - (dimensionPixelSize * 2);
        if (this.mSettingIconDrawable == null) {
            int outset = this.mCenterDrawable instanceof PreloadIconDrawable ? ((PreloadIconDrawable) this.mCenterDrawable).getOutset() : 0;
            int min = Math.min(deviceProfile.iconSizePx + (outset * 2), Math.min(width, height));
            this.mRect.set(0, 0, min, min);
            this.mRect.inset(outset, outset);
            this.mRect.offsetTo((getWidth() - this.mRect.width()) / 2, (getHeight() - this.mRect.height()) / 2);
            this.mCenterDrawable.setBounds(this.mRect);
            return;
        }
        float max = Math.max(0, Math.min(width, height));
        int max2 = Math.max(width, height);
        float f = max;
        if (1.8f * max > max2) {
            f = max2 / 1.8f;
        }
        int min2 = (int) Math.min(f, deviceProfile.iconSizePx);
        int height2 = (getHeight() - min2) / 2;
        this.mSetupTextLayout = null;
        int i = height2;
        if (width > 0) {
            this.mSetupTextLayout = new StaticLayout(getResources().getText(2131558433), this.mPaint, width, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, true);
            int height3 = this.mSetupTextLayout.getHeight();
            if (height3 + (min2 * 1.8f) + deviceProfile.iconDrawablePaddingPx < height) {
                i = (((getHeight() - height3) - deviceProfile.iconDrawablePaddingPx) - min2) / 2;
            } else {
                this.mSetupTextLayout = null;
                i = height2;
            }
        }
        this.mRect.set(0, 0, min2, min2);
        this.mRect.offset((getWidth() - min2) / 2, i);
        this.mCenterDrawable.setBounds(this.mRect);
        this.mRect.left = paddingLeft + dimensionPixelSize;
        this.mRect.right = this.mRect.left + ((int) (min2 * 0.4f));
        this.mRect.top = paddingTop + dimensionPixelSize;
        this.mRect.bottom = this.mRect.top + ((int) (min2 * 0.4f));
        this.mSettingIconDrawable.setBounds(this.mRect);
        if (this.mSetupTextLayout != null) {
            this.mRect.left = paddingLeft + dimensionPixelSize;
            this.mRect.top = this.mCenterDrawable.getBounds().bottom + deviceProfile.iconDrawablePaddingPx;
        }
    }

    private void updateSettingColor() {
        Color.colorToHSV(Utilities.findDominantColorByHue(this.mIcon, 20), r0);
        float[] fArr = {0.0f, Math.min(fArr[1], 0.7f), 1.0f};
        this.mSettingIconDrawable.setColorFilter(Color.HSVToColor(fArr), PorterDuff.Mode.SRC_IN);
    }

    public void applyState() {
        if (this.mCenterDrawable != null) {
            this.mCenterDrawable.setLevel(Math.max(this.mInfo.installProgress, 0));
        }
    }

    @Override // android.appwidget.AppWidgetHostView
    protected View getDefaultView() {
        if (this.mDefaultView == null) {
            this.mDefaultView = this.mInflater.inflate(2130968588, (ViewGroup) this, false);
            this.mDefaultView.setOnClickListener(this);
            applyState();
        }
        return this.mDefaultView;
    }

    public boolean isReadyForClickSetup() {
        boolean z = false;
        if ((this.mInfo.restoreStatus & 2) == 0) {
            z = false;
            if ((this.mInfo.restoreStatus & 4) != 0) {
                z = true;
            }
        }
        return z;
    }

    @Override // com.android.launcher3.LauncherAppWidgetHostView
    public boolean isReinflateRequired() {
        return this.mStartState != this.mInfo.restoreStatus;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (this.mClickListener != null) {
            this.mClickListener.onClick(this);
        }
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        if (this.mCenterDrawable == null) {
            return;
        }
        if (this.mDrawableSizeChanged) {
            updateDrawableBounds();
            this.mDrawableSizeChanged = false;
        }
        this.mCenterDrawable.draw(canvas);
        if (this.mSettingIconDrawable != null) {
            this.mSettingIconDrawable.draw(canvas);
        }
        if (this.mSetupTextLayout != null) {
            canvas.save();
            canvas.translate(this.mRect.left, this.mRect.top);
            this.mSetupTextLayout.draw(canvas);
            canvas.restore();
        }
    }

    @Override // android.view.View
    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        this.mDrawableSizeChanged = true;
    }

    @Override // android.view.View
    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.mClickListener = onClickListener;
    }

    @Override // android.appwidget.AppWidgetHostView
    public void updateAppWidgetSize(Bundle bundle, int i, int i2, int i3, int i4) {
    }

    public void updateIcon(IconCache iconCache) {
        Bitmap icon = iconCache.getIcon(this.mIconLookupIntent, this.mInfo.user);
        if (this.mIcon == icon) {
            return;
        }
        this.mIcon = icon;
        if (this.mCenterDrawable != null) {
            this.mCenterDrawable.setCallback(null);
            this.mCenterDrawable = null;
        }
        if (this.mIcon != null) {
            if (this.mDisabledForSafeMode) {
                FastBitmapDrawable createIconDrawable = this.mLauncher.createIconDrawable(this.mIcon);
                createIconDrawable.setState(FastBitmapDrawable.State.DISABLED);
                this.mCenterDrawable = createIconDrawable;
                this.mSettingIconDrawable = null;
            } else if (isReadyForClickSetup()) {
                this.mCenterDrawable = new FastBitmapDrawable(this.mIcon);
                this.mSettingIconDrawable = getResources().getDrawable(2130837532).mutate();
                updateSettingColor();
            } else {
                if (sPreloaderTheme == null) {
                    sPreloaderTheme = getResources().newTheme();
                    sPreloaderTheme.applyStyle(2131755017, true);
                }
                this.mCenterDrawable = new PreloadIconDrawable(this.mLauncher.createIconDrawable(this.mIcon), sPreloaderTheme);
                this.mCenterDrawable.setCallback(this);
                this.mSettingIconDrawable = null;
                applyState();
            }
            this.mDrawableSizeChanged = true;
        }
    }

    @Override // android.view.View
    protected boolean verifyDrawable(Drawable drawable) {
        return drawable != this.mCenterDrawable ? super.verifyDrawable(drawable) : true;
    }
}
