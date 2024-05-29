package com.android.launcher3.graphics;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.DrawableWrapper;
import android.util.AttributeSet;
import org.xmlpull.v1.XmlPullParser;
@TargetApi(24)
/* loaded from: classes.dex */
public class FixedScaleDrawable extends DrawableWrapper {
    private static final float LEGACY_ICON_SCALE = 0.46669f;
    private float mScaleX;
    private float mScaleY;

    public FixedScaleDrawable() {
        super(new ColorDrawable());
        this.mScaleX = LEGACY_ICON_SCALE;
        this.mScaleY = LEGACY_ICON_SCALE;
    }

    @Override // android.graphics.drawable.DrawableWrapper, android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        int save = canvas.save();
        canvas.scale(this.mScaleX, this.mScaleY, getBounds().exactCenterX(), getBounds().exactCenterY());
        super.draw(canvas);
        canvas.restoreToCount(save);
    }

    @Override // android.graphics.drawable.Drawable
    public void inflate(Resources resources, XmlPullParser xmlPullParser, AttributeSet attributeSet) {
    }

    @Override // android.graphics.drawable.DrawableWrapper, android.graphics.drawable.Drawable
    public void inflate(Resources resources, XmlPullParser xmlPullParser, AttributeSet attributeSet, Resources.Theme theme) {
    }

    public void setScale(float f) {
        float intrinsicHeight = getIntrinsicHeight();
        float intrinsicWidth = getIntrinsicWidth();
        float f2 = f * LEGACY_ICON_SCALE;
        this.mScaleX = f2;
        this.mScaleY = f2;
        if (intrinsicHeight > intrinsicWidth && intrinsicWidth > 0.0f) {
            this.mScaleX *= intrinsicWidth / intrinsicHeight;
        } else if (intrinsicWidth > intrinsicHeight && intrinsicHeight > 0.0f) {
            this.mScaleY *= intrinsicHeight / intrinsicWidth;
        }
    }
}
