package android.support.v4.graphics.drawable;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableWrapperDonut;
/* loaded from: a.zip:android/support/v4/graphics/drawable/DrawableWrapperKitKat.class */
class DrawableWrapperKitKat extends DrawableWrapperHoneycomb {

    /* loaded from: a.zip:android/support/v4/graphics/drawable/DrawableWrapperKitKat$DrawableWrapperStateKitKat.class */
    private static class DrawableWrapperStateKitKat extends DrawableWrapperDonut.DrawableWrapperState {
        DrawableWrapperStateKitKat(@Nullable DrawableWrapperDonut.DrawableWrapperState drawableWrapperState, @Nullable Resources resources) {
            super(drawableWrapperState, resources);
        }

        @Override // android.support.v4.graphics.drawable.DrawableWrapperDonut.DrawableWrapperState, android.graphics.drawable.Drawable.ConstantState
        public Drawable newDrawable(@Nullable Resources resources) {
            return new DrawableWrapperKitKat(this, resources);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public DrawableWrapperKitKat(Drawable drawable) {
        super(drawable);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public DrawableWrapperKitKat(DrawableWrapperDonut.DrawableWrapperState drawableWrapperState, Resources resources) {
        super(drawableWrapperState, resources);
    }

    @Override // android.graphics.drawable.Drawable
    public boolean isAutoMirrored() {
        return this.mDrawable.isAutoMirrored();
    }

    @Override // android.support.v4.graphics.drawable.DrawableWrapperHoneycomb, android.support.v4.graphics.drawable.DrawableWrapperDonut
    @NonNull
    DrawableWrapperDonut.DrawableWrapperState mutateConstantState() {
        return new DrawableWrapperStateKitKat(this.mState, null);
    }

    @Override // android.graphics.drawable.Drawable
    public void setAutoMirrored(boolean z) {
        this.mDrawable.setAutoMirrored(z);
    }
}
