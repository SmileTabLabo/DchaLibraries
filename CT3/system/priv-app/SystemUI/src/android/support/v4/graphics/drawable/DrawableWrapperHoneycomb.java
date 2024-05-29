package android.support.v4.graphics.drawable;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableWrapperDonut;
/* loaded from: a.zip:android/support/v4/graphics/drawable/DrawableWrapperHoneycomb.class */
class DrawableWrapperHoneycomb extends DrawableWrapperDonut {

    /* loaded from: a.zip:android/support/v4/graphics/drawable/DrawableWrapperHoneycomb$DrawableWrapperStateHoneycomb.class */
    private static class DrawableWrapperStateHoneycomb extends DrawableWrapperDonut.DrawableWrapperState {
        DrawableWrapperStateHoneycomb(@Nullable DrawableWrapperDonut.DrawableWrapperState drawableWrapperState, @Nullable Resources resources) {
            super(drawableWrapperState, resources);
        }

        @Override // android.support.v4.graphics.drawable.DrawableWrapperDonut.DrawableWrapperState, android.graphics.drawable.Drawable.ConstantState
        public Drawable newDrawable(@Nullable Resources resources) {
            return new DrawableWrapperHoneycomb(this, resources);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public DrawableWrapperHoneycomb(Drawable drawable) {
        super(drawable);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public DrawableWrapperHoneycomb(DrawableWrapperDonut.DrawableWrapperState drawableWrapperState, Resources resources) {
        super(drawableWrapperState, resources);
    }

    @Override // android.graphics.drawable.Drawable
    public void jumpToCurrentState() {
        this.mDrawable.jumpToCurrentState();
    }

    @Override // android.support.v4.graphics.drawable.DrawableWrapperDonut
    @NonNull
    DrawableWrapperDonut.DrawableWrapperState mutateConstantState() {
        return new DrawableWrapperStateHoneycomb(this.mState, null);
    }
}
