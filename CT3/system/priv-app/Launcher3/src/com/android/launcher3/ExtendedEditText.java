package com.android.launcher3;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;
/* loaded from: a.zip:com/android/launcher3/ExtendedEditText.class */
public class ExtendedEditText extends EditText {
    private OnBackKeyListener mBackKeyListener;

    /* loaded from: a.zip:com/android/launcher3/ExtendedEditText$OnBackKeyListener.class */
    public interface OnBackKeyListener {
        boolean onBackKey();
    }

    public ExtendedEditText(Context context) {
        super(context);
    }

    public ExtendedEditText(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public ExtendedEditText(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    @Override // android.widget.TextView, android.view.View
    public boolean onKeyPreIme(int i, KeyEvent keyEvent) {
        if (i == 4 && keyEvent.getAction() == 1) {
            if (this.mBackKeyListener != null) {
                return this.mBackKeyListener.onBackKey();
            }
            return false;
        }
        return super.onKeyPreIme(i, keyEvent);
    }

    public void setOnBackKeyListener(OnBackKeyListener onBackKeyListener) {
        this.mBackKeyListener = onBackKeyListener;
    }
}
