package com.mediatek.keyguard.PowerOffAlarm.multiwaveview;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
/* loaded from: classes.dex */
public class TargetDrawable {
    private Drawable mDrawable;
    private int mNumDrawables;
    private final int mResourceId;
    public static final int[] STATE_ACTIVE = {16842910, 16842914};
    public static final int[] STATE_INACTIVE = {16842910, -16842914};
    public static final int[] STATE_FOCUSED = {16842910, -16842914, 16842908};
    private float mTranslationX = 0.0f;
    private float mTranslationY = 0.0f;
    private float mPositionX = 0.0f;
    private float mPositionY = 0.0f;
    private float mScaleX = 1.0f;
    private float mScaleY = 1.0f;
    private float mAlpha = 1.0f;
    private boolean mEnabled = true;

    public TargetDrawable(Resources resources, int i, int i2) {
        this.mNumDrawables = 1;
        this.mResourceId = i;
        setDrawable(resources, i);
        this.mNumDrawables = i2;
    }

    public void setDrawable(Resources resources, int i) {
        Drawable drawable;
        if (i != 0) {
            drawable = resources.getDrawable(i);
        } else {
            drawable = null;
        }
        this.mDrawable = drawable != null ? drawable.mutate() : null;
        resizeDrawables();
        setState(STATE_INACTIVE);
    }

    public void setState(int[] iArr) {
        if (this.mDrawable instanceof StateListDrawable) {
            ((StateListDrawable) this.mDrawable).setState(iArr);
        }
    }

    public boolean isEnabled() {
        return this.mDrawable != null && this.mEnabled;
    }

    private void resizeDrawables() {
        if (!(this.mDrawable instanceof StateListDrawable)) {
            if (this.mDrawable != null) {
                this.mDrawable.setBounds(0, 0, this.mDrawable.getIntrinsicWidth(), this.mDrawable.getIntrinsicHeight());
                return;
            }
            return;
        }
        StateListDrawable stateListDrawable = (StateListDrawable) this.mDrawable;
        int i = 0;
        int i2 = 0;
        for (int i3 = 0; i3 < this.mNumDrawables; i3++) {
            stateListDrawable.selectDrawable(i3);
            Drawable current = stateListDrawable.getCurrent();
            i = Math.max(i, current.getIntrinsicWidth());
            i2 = Math.max(i2, current.getIntrinsicHeight());
        }
        stateListDrawable.setBounds(0, 0, i, i2);
        for (int i4 = 0; i4 < this.mNumDrawables; i4++) {
            stateListDrawable.selectDrawable(i4);
            stateListDrawable.getCurrent().setBounds(0, 0, i, i2);
        }
    }

    public void setX(float f) {
        this.mTranslationX = f;
    }

    public void setY(float f) {
        this.mTranslationY = f;
    }

    public void setAlpha(float f) {
        this.mAlpha = f;
    }

    public float getX() {
        return this.mTranslationX;
    }

    public float getY() {
        return this.mTranslationY;
    }

    public void setPositionX(float f) {
        this.mPositionX = f;
    }

    public void setPositionY(float f) {
        this.mPositionY = f;
    }

    public int getWidth() {
        if (this.mDrawable != null) {
            return this.mDrawable.getIntrinsicWidth();
        }
        return 0;
    }

    public int getHeight() {
        if (this.mDrawable != null) {
            return this.mDrawable.getIntrinsicHeight();
        }
        return 0;
    }

    public void draw(Canvas canvas) {
        if (this.mDrawable == null || !this.mEnabled) {
            return;
        }
        canvas.save(1);
        canvas.scale(this.mScaleX, this.mScaleY, this.mPositionX, this.mPositionY);
        canvas.translate(this.mTranslationX + this.mPositionX, this.mTranslationY + this.mPositionY);
        canvas.translate(getWidth() * (-0.5f), (-0.5f) * getHeight());
        this.mDrawable.setAlpha(Math.round(this.mAlpha * 255.0f));
        this.mDrawable.draw(canvas);
        canvas.restore();
    }

    public int getResourceId() {
        return this.mResourceId;
    }
}
