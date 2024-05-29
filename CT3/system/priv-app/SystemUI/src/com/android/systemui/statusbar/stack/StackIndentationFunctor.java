package com.android.systemui.statusbar.stack;
/* loaded from: a.zip:com/android/systemui/statusbar/stack/StackIndentationFunctor.class */
public abstract class StackIndentationFunctor {
    protected int mDistanceToPeekStart;
    protected int mMaxItemsInStack;
    protected int mPeekSize;
    protected boolean mStackStartsAtPeek;
    protected int mTotalTransitionDistance;

    /* JADX INFO: Access modifiers changed from: package-private */
    public StackIndentationFunctor(int i, int i2, int i3) {
        boolean z = false;
        this.mDistanceToPeekStart = i3;
        this.mStackStartsAtPeek = this.mDistanceToPeekStart == 0 ? true : z;
        this.mMaxItemsInStack = i;
        this.mPeekSize = i2;
        updateTotalTransitionDistance();
    }

    private void updateTotalTransitionDistance() {
        this.mTotalTransitionDistance = this.mDistanceToPeekStart + this.mPeekSize;
    }

    public abstract float getValue(float f);
}
