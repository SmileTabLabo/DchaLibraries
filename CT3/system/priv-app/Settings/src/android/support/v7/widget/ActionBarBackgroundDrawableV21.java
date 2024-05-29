package android.support.v7.widget;

import android.graphics.Outline;
import android.support.annotation.NonNull;
/* loaded from: classes.dex */
class ActionBarBackgroundDrawableV21 extends ActionBarBackgroundDrawable {
    public ActionBarBackgroundDrawableV21(ActionBarContainer container) {
        super(container);
    }

    @Override // android.graphics.drawable.Drawable
    public void getOutline(@NonNull Outline outline) {
        if (this.mContainer.mIsSplit) {
            if (this.mContainer.mSplitBackground == null) {
                return;
            }
            this.mContainer.mSplitBackground.getOutline(outline);
        } else if (this.mContainer.mBackground == null) {
        } else {
            this.mContainer.mBackground.getOutline(outline);
        }
    }
}
