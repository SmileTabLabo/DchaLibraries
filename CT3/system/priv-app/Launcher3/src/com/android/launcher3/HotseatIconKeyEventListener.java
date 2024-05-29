package com.android.launcher3;

import android.view.KeyEvent;
import android.view.View;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: a.zip:com/android/launcher3/HotseatIconKeyEventListener.class */
public class HotseatIconKeyEventListener implements View.OnKeyListener {
    @Override // android.view.View.OnKeyListener
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        return FocusHelper.handleHotseatButtonKeyEvent(view, i, keyEvent);
    }
}
