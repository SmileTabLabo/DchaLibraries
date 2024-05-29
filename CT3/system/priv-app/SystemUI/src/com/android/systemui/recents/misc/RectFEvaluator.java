package com.android.systemui.recents.misc;

import android.animation.TypeEvaluator;
import android.graphics.RectF;
/* loaded from: a.zip:com/android/systemui/recents/misc/RectFEvaluator.class */
public class RectFEvaluator implements TypeEvaluator<RectF> {
    private RectF mRect = new RectF();

    @Override // android.animation.TypeEvaluator
    public RectF evaluate(float f, RectF rectF, RectF rectF2) {
        this.mRect.set(rectF.left + ((rectF2.left - rectF.left) * f), rectF.top + ((rectF2.top - rectF.top) * f), rectF.right + ((rectF2.right - rectF.right) * f), rectF.bottom + ((rectF2.bottom - rectF.bottom) * f));
        return this.mRect;
    }
}
