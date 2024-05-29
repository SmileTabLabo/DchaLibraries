package android.support.v17.leanback.app;

import android.content.Context;
import android.support.v17.leanback.widget.Util;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
/* loaded from: a.zip:android/support/v17/leanback/app/GuidedStepRootLayout.class */
class GuidedStepRootLayout extends LinearLayout {
    private boolean mFocusOutEnd;
    private boolean mFocusOutStart;

    public GuidedStepRootLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mFocusOutStart = false;
        this.mFocusOutEnd = false;
    }

    public GuidedStepRootLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mFocusOutStart = false;
        this.mFocusOutEnd = false;
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public View focusSearch(View view, int i) {
        View focusSearch = super.focusSearch(view, i);
        if (i == 17 || i == 66) {
            if (Util.isDescendant(this, focusSearch)) {
                return focusSearch;
            }
            if (getLayoutDirection() != 0 ? i == 66 : i == 17) {
                if (!this.mFocusOutStart) {
                    return view;
                }
            } else if (!this.mFocusOutEnd) {
                return view;
            }
        }
        return focusSearch;
    }
}
