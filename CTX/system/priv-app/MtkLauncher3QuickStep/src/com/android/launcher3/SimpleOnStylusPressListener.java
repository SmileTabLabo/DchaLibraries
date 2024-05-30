package com.android.launcher3;

import android.view.MotionEvent;
import android.view.View;
import com.android.launcher3.StylusEventHelper;
/* loaded from: classes.dex */
public class SimpleOnStylusPressListener implements StylusEventHelper.StylusButtonListener {
    private View mView;

    public SimpleOnStylusPressListener(View view) {
        this.mView = view;
    }

    @Override // com.android.launcher3.StylusEventHelper.StylusButtonListener
    public boolean onPressed(MotionEvent motionEvent) {
        return this.mView.isLongClickable() && this.mView.performLongClick();
    }

    @Override // com.android.launcher3.StylusEventHelper.StylusButtonListener
    public boolean onReleased(MotionEvent motionEvent) {
        return false;
    }
}
