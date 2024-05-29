package android.support.v4.graphics.drawable;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableWrapperDonut;
/* loaded from: classes.dex */
class DrawableWrapperKitKat extends DrawableWrapperHoneycomb {
    /* JADX INFO: Access modifiers changed from: package-private */
    public DrawableWrapperKitKat(Drawable drawable) {
        super(drawable);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public DrawableWrapperKitKat(DrawableWrapperDonut.DrawableWrapperState state, Resources resources) {
        super(state, resources);
    }

    @Override // android.graphics.drawable.Drawable
    public void setAutoMirrored(boolean mirrored) {
        this.mDrawable.setAutoMirrored(mirrored);
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

    /* loaded from: classes.dex */
    private static class DrawableWrapperStateKitKat extends DrawableWrapperDonut.DrawableWrapperState {
        DrawableWrapperStateKitKat(@Nullable DrawableWrapperDonut.DrawableWrapperState orig, @Nullable Resources res) {
            super(orig, res);
        }

        @Override // android.support.v4.graphics.drawable.DrawableWrapperDonut.DrawableWrapperState, android.graphics.drawable.Drawable.ConstantState
        public Drawable newDrawable(@Nullable Resources res) {
            return new DrawableWrapperKitKat(this, res);
        }
    }
}
