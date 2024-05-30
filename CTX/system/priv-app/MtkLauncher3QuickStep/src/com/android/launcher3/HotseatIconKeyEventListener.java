package com.android.launcher3;

import android.view.KeyEvent;
import android.view.View;
/* compiled from: FocusHelper.java */
/* loaded from: classes.dex */
class HotseatIconKeyEventListener implements View.OnKeyListener {
    HotseatIconKeyEventListener() {
    }

    @Override // android.view.View.OnKeyListener
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        return FocusHelper.handleHotseatButtonKeyEvent(view, i, keyEvent);
    }
}
