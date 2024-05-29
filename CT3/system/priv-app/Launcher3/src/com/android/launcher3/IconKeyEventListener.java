package com.android.launcher3;

import android.view.KeyEvent;
import android.view.View;
/* loaded from: a.zip:com/android/launcher3/IconKeyEventListener.class */
class IconKeyEventListener implements View.OnKeyListener {
    @Override // android.view.View.OnKeyListener
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        return FocusHelper.handleIconKeyEvent(view, i, keyEvent);
    }
}
