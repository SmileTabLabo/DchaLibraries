package com.android.systemui.statusbar.stack;

import java.util.ArrayList;
/* loaded from: a.zip:com/android/systemui/statusbar/stack/PiecewiseLinearIndentationFunctor.class */
public class PiecewiseLinearIndentationFunctor extends StackIndentationFunctor {
    private final ArrayList<Float> mBaseValues;
    private final float mLinearPart;

    /* JADX INFO: Access modifiers changed from: package-private */
    public PiecewiseLinearIndentationFunctor(int i, int i2, int i3, float f) {
        super(i, i2, i3);
        this.mBaseValues = new ArrayList<>(i + 1);
        initBaseValues();
        this.mLinearPart = f;
    }

    private int getSumOfSquares(int i) {
        return (((i + 1) * i) * ((i * 2) + 1)) / 6;
    }

    private void initBaseValues() {
        int sumOfSquares = getSumOfSquares(this.mMaxItemsInStack - 1);
        int i = 0;
        this.mBaseValues.add(Float.valueOf(0.0f));
        for (int i2 = 0; i2 < this.mMaxItemsInStack - 1; i2++) {
            i += ((this.mMaxItemsInStack - i2) - 1) * ((this.mMaxItemsInStack - i2) - 1);
            this.mBaseValues.add(Float.valueOf(i / sumOfSquares));
        }
    }

    @Override // com.android.systemui.statusbar.stack.StackIndentationFunctor
    public float getValue(float f) {
        float f2 = f;
        if (this.mStackStartsAtPeek) {
            f2 = f + 1.0f;
        }
        if (f2 < 0.0f) {
            return 0.0f;
        }
        if (f2 >= this.mMaxItemsInStack) {
            return this.mTotalTransitionDistance;
        }
        int i = (int) f2;
        float f3 = f2 - i;
        if (i == 0) {
            return this.mDistanceToPeekStart * f3;
        }
        return this.mDistanceToPeekStart + ((((1.0f - this.mLinearPart) * (((1.0f - f3) * this.mBaseValues.get(i - 1).floatValue()) + (this.mBaseValues.get(i).floatValue() * f3))) + (((f2 - 1.0f) / (this.mMaxItemsInStack - 1)) * this.mLinearPart)) * this.mPeekSize);
    }
}
