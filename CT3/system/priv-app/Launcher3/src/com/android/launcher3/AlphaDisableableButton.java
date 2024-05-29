package com.android.launcher3;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
/* loaded from: a.zip:com/android/launcher3/AlphaDisableableButton.class */
public class AlphaDisableableButton extends Button {
    public static float DISABLED_ALPHA_VALUE = 0.4f;

    public AlphaDisableableButton(Context context) {
        this(context, null);
    }

    public AlphaDisableableButton(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public AlphaDisableableButton(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        setLayerType(2, null);
    }

    @Override // android.widget.TextView, android.view.View
    public void setEnabled(boolean z) {
        super.setEnabled(z);
        if (z) {
            setAlpha(1.0f);
        } else {
            setAlpha(DISABLED_ALPHA_VALUE);
        }
    }
}
