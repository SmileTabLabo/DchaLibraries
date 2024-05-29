package android.support.v17.leanback.widget;

import android.graphics.Outline;
import android.view.View;
import android.view.ViewOutlineProvider;
/* loaded from: a.zip:android/support/v17/leanback/widget/ShadowHelperApi21.class */
class ShadowHelperApi21 {
    static final ViewOutlineProvider sOutlineProvider = new ViewOutlineProvider() { // from class: android.support.v17.leanback.widget.ShadowHelperApi21.1
        @Override // android.view.ViewOutlineProvider
        public void getOutline(View view, Outline outline) {
            outline.setRect(0, 0, view.getWidth(), view.getHeight());
            outline.setAlpha(1.0f);
        }
    };

    ShadowHelperApi21() {
    }

    public static void setZ(View view, float f) {
        view.setZ(f);
    }
}
