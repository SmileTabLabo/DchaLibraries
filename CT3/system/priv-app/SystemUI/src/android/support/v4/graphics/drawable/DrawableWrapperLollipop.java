package android.support.v4.graphics.drawable;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Outline;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableWrapperDonut;
/* loaded from: a.zip:android/support/v4/graphics/drawable/DrawableWrapperLollipop.class */
class DrawableWrapperLollipop extends DrawableWrapperKitKat {

    /* loaded from: a.zip:android/support/v4/graphics/drawable/DrawableWrapperLollipop$DrawableWrapperStateLollipop.class */
    private static class DrawableWrapperStateLollipop extends DrawableWrapperDonut.DrawableWrapperState {
        DrawableWrapperStateLollipop(@Nullable DrawableWrapperDonut.DrawableWrapperState drawableWrapperState, @Nullable Resources resources) {
            super(drawableWrapperState, resources);
        }

        @Override // android.support.v4.graphics.drawable.DrawableWrapperDonut.DrawableWrapperState, android.graphics.drawable.Drawable.ConstantState
        public Drawable newDrawable(@Nullable Resources resources) {
            return new DrawableWrapperLollipop(this, resources);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public DrawableWrapperLollipop(Drawable drawable) {
        super(drawable);
    }

    DrawableWrapperLollipop(DrawableWrapperDonut.DrawableWrapperState drawableWrapperState, Resources resources) {
        super(drawableWrapperState, resources);
    }

    @Override // android.graphics.drawable.Drawable
    public Rect getDirtyBounds() {
        return this.mDrawable.getDirtyBounds();
    }

    @Override // android.graphics.drawable.Drawable
    public void getOutline(Outline outline) {
        this.mDrawable.getOutline(outline);
    }

    @Override // android.support.v4.graphics.drawable.DrawableWrapperDonut
    protected boolean isCompatTintEnabled() {
        if (Build.VERSION.SDK_INT == 21) {
            Drawable drawable = this.mDrawable;
            return ((drawable instanceof GradientDrawable) || (drawable instanceof DrawableContainer)) ? true : drawable instanceof InsetDrawable;
        }
        return false;
    }

    @Override // android.support.v4.graphics.drawable.DrawableWrapperKitKat, android.support.v4.graphics.drawable.DrawableWrapperHoneycomb, android.support.v4.graphics.drawable.DrawableWrapperDonut
    @NonNull
    DrawableWrapperDonut.DrawableWrapperState mutateConstantState() {
        return new DrawableWrapperStateLollipop(this.mState, null);
    }

    @Override // android.graphics.drawable.Drawable
    public void setHotspot(float f, float f2) {
        this.mDrawable.setHotspot(f, f2);
    }

    @Override // android.graphics.drawable.Drawable
    public void setHotspotBounds(int i, int i2, int i3, int i4) {
        this.mDrawable.setHotspotBounds(i, i2, i3, i4);
    }

    @Override // android.support.v4.graphics.drawable.DrawableWrapperDonut, android.graphics.drawable.Drawable
    public boolean setState(int[] iArr) {
        if (super.setState(iArr)) {
            invalidateSelf();
            return true;
        }
        return false;
    }

    @Override // android.support.v4.graphics.drawable.DrawableWrapperDonut, android.graphics.drawable.Drawable, android.support.v4.graphics.drawable.TintAwareDrawable
    public void setTint(int i) {
        if (isCompatTintEnabled()) {
            super.setTint(i);
        } else {
            this.mDrawable.setTint(i);
        }
    }

    @Override // android.support.v4.graphics.drawable.DrawableWrapperDonut, android.graphics.drawable.Drawable, android.support.v4.graphics.drawable.TintAwareDrawable
    public void setTintList(ColorStateList colorStateList) {
        if (isCompatTintEnabled()) {
            super.setTintList(colorStateList);
        } else {
            this.mDrawable.setTintList(colorStateList);
        }
    }

    @Override // android.support.v4.graphics.drawable.DrawableWrapperDonut, android.graphics.drawable.Drawable, android.support.v4.graphics.drawable.TintAwareDrawable
    public void setTintMode(PorterDuff.Mode mode) {
        if (isCompatTintEnabled()) {
            super.setTintMode(mode);
        } else {
            this.mDrawable.setTintMode(mode);
        }
    }
}
