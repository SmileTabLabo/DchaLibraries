package com.android.systemui.statusbar;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.util.AttributeSet;
import android.view.View;
/* loaded from: a.zip:com/android/systemui/statusbar/NotificationBackgroundView.class */
public class NotificationBackgroundView extends View {
    private int mActualHeight;
    private Drawable mBackground;
    private int mClipTopAmount;

    public NotificationBackgroundView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    private void draw(Canvas canvas, Drawable drawable) {
        if (drawable == null || this.mActualHeight <= this.mClipTopAmount) {
            return;
        }
        drawable.setBounds(0, this.mClipTopAmount, getWidth(), this.mActualHeight);
        drawable.draw(canvas);
    }

    private void drawableStateChanged(Drawable drawable) {
        if (drawable == null || !drawable.isStateful()) {
            return;
        }
        drawable.setState(getDrawableState());
    }

    @Override // android.view.View
    public void drawableHotspotChanged(float f, float f2) {
        if (this.mBackground != null) {
            this.mBackground.setHotspot(f, f2);
        }
    }

    @Override // android.view.View
    protected void drawableStateChanged() {
        drawableStateChanged(this.mBackground);
    }

    public int getActualHeight() {
        return this.mActualHeight;
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        draw(canvas, this.mBackground);
    }

    public void setActualHeight(int i) {
        this.mActualHeight = i;
        invalidate();
    }

    public void setClipTopAmount(int i) {
        this.mClipTopAmount = i;
        invalidate();
    }

    public void setCustomBackground(int i) {
        setCustomBackground(this.mContext.getDrawable(i));
    }

    public void setCustomBackground(Drawable drawable) {
        if (this.mBackground != null) {
            this.mBackground.setCallback(null);
            unscheduleDrawable(this.mBackground);
        }
        this.mBackground = drawable;
        if (this.mBackground != null) {
            this.mBackground.setCallback(this);
        }
        if (this.mBackground instanceof RippleDrawable) {
            ((RippleDrawable) this.mBackground).setForceSoftware(true);
        }
        invalidate();
    }

    public void setRippleColor(int i) {
        if (this.mBackground instanceof RippleDrawable) {
            ((RippleDrawable) this.mBackground).setColor(ColorStateList.valueOf(i));
        }
    }

    public void setState(int[] iArr) {
        this.mBackground.setState(iArr);
    }

    public void setTint(int i) {
        if (i != 0) {
            this.mBackground.setColorFilter(i, PorterDuff.Mode.SRC_ATOP);
        } else {
            this.mBackground.clearColorFilter();
        }
        invalidate();
    }

    @Override // android.view.View
    protected boolean verifyDrawable(Drawable drawable) {
        boolean z = true;
        if (!super.verifyDrawable(drawable)) {
            z = drawable == this.mBackground;
        }
        return z;
    }
}
