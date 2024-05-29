package android.support.v4.graphics.drawable;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableWrapperDonut;
/* loaded from: a.zip:android/support/v4/graphics/drawable/DrawableWrapperEclair.class */
class DrawableWrapperEclair extends DrawableWrapperDonut {

    /* loaded from: a.zip:android/support/v4/graphics/drawable/DrawableWrapperEclair$DrawableWrapperStateEclair.class */
    private static class DrawableWrapperStateEclair extends DrawableWrapperDonut.DrawableWrapperState {
        DrawableWrapperStateEclair(@Nullable DrawableWrapperDonut.DrawableWrapperState drawableWrapperState, @Nullable Resources resources) {
            super(drawableWrapperState, resources);
        }

        @Override // android.support.v4.graphics.drawable.DrawableWrapperDonut.DrawableWrapperState, android.graphics.drawable.Drawable.ConstantState
        public Drawable newDrawable(@Nullable Resources resources) {
            return new DrawableWrapperEclair(this, resources);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public DrawableWrapperEclair(Drawable drawable) {
        super(drawable);
    }

    DrawableWrapperEclair(DrawableWrapperDonut.DrawableWrapperState drawableWrapperState, Resources resources) {
        super(drawableWrapperState, resources);
    }

    @Override // android.support.v4.graphics.drawable.DrawableWrapperDonut
    DrawableWrapperDonut.DrawableWrapperState mutateConstantState() {
        return new DrawableWrapperStateEclair(this.mState, null);
    }

    @Override // android.support.v4.graphics.drawable.DrawableWrapperDonut
    protected Drawable newDrawableFromState(Drawable.ConstantState constantState, Resources resources) {
        return constantState.newDrawable(resources);
    }
}
