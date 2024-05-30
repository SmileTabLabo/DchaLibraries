package com.android.systemui.pip.phone;
/* loaded from: classes.dex */
public abstract class PipTouchGesture {
    /* JADX INFO: Access modifiers changed from: package-private */
    public void onDown(PipTouchState pipTouchState) {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean onMove(PipTouchState pipTouchState) {
        return false;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean onUp(PipTouchState pipTouchState) {
        return false;
    }
}
