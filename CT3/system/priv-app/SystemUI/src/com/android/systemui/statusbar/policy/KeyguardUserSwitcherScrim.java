package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.view.View;
/* loaded from: a.zip:com/android/systemui/statusbar/policy/KeyguardUserSwitcherScrim.class */
public class KeyguardUserSwitcherScrim extends Drawable implements View.OnLayoutChangeListener {
    private int mDarkColor;
    private int mLayoutWidth;
    private int mTop;
    private int mAlpha = 255;
    private Paint mRadialGradientPaint = new Paint();

    public KeyguardUserSwitcherScrim(Context context) {
        this.mDarkColor = context.getColor(2131558565);
    }

    private void updatePaint() {
        if (this.mLayoutWidth == 0) {
            return;
        }
        float f = this.mLayoutWidth * 2.5f;
        boolean z = getLayoutDirection() == 0;
        Paint paint = this.mRadialGradientPaint;
        int i = z ? this.mLayoutWidth : 0;
        paint.setShader(new RadialGradient(i, 0.0f, f, new int[]{Color.argb((int) ((Color.alpha(this.mDarkColor) * this.mAlpha) / 255.0f), 0, 0, 0), 0}, new float[]{Math.max(0.0f, (this.mLayoutWidth * 0.75f) / f), 1.0f}, Shader.TileMode.CLAMP));
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        boolean z = getLayoutDirection() == 0;
        Rect bounds = getBounds();
        float width = bounds.width() * 2.5f;
        canvas.translate(0.0f, -this.mTop);
        canvas.scale(1.0f, ((this.mTop + bounds.height()) * 2.5f) / width);
        canvas.drawRect(z ? bounds.right - width : 0.0f, 0.0f, z ? bounds.right : bounds.left + width, width, this.mRadialGradientPaint);
    }

    @Override // android.graphics.drawable.Drawable
    public int getAlpha() {
        return this.mAlpha;
    }

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        return -3;
    }

    @Override // android.view.View.OnLayoutChangeListener
    public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        if (i == i5 && i2 == i6 && i3 == i7 && i4 == i8) {
            return;
        }
        this.mLayoutWidth = i3 - i;
        this.mTop = i2;
        updatePaint();
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int i) {
        this.mAlpha = i;
        updatePaint();
        invalidateSelf();
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter colorFilter) {
    }
}
