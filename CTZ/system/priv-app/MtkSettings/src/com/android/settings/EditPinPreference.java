package com.android.settings;

import android.app.Dialog;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import com.android.settingslib.CustomEditTextPreference;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class EditPinPreference extends CustomEditTextPreference {
    private OnPinEnteredListener mPinListener;

    /* loaded from: classes.dex */
    interface OnPinEnteredListener {
        void onPinEntered(EditPinPreference editPinPreference, boolean z);
    }

    public EditPinPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public EditPinPreference(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public void setOnPinEnteredListener(OnPinEnteredListener onPinEnteredListener) {
        this.mPinListener = onPinEnteredListener;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settingslib.CustomEditTextPreference
    public void onBindDialogView(View view) {
        super.onBindDialogView(view);
        EditText editText = (EditText) view.findViewById(16908291);
        if (editText != null) {
            editText.setInputType(18);
            editText.setTextAlignment(5);
            Log.d("EditPinPreference", "onBindDialogView, disable auto fill.");
            editText.setImportantForAutofill(8);
        }
    }

    @Override // com.android.settingslib.CustomEditTextPreference
    public boolean isDialogOpen() {
        Dialog dialog = getDialog();
        return dialog != null && dialog.isShowing();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settingslib.CustomEditTextPreference
    public void onDialogClosed(boolean z) {
        super.onDialogClosed(z);
        if (this.mPinListener != null) {
            this.mPinListener.onPinEntered(this, z);
        }
    }

    public void showPinDialog() {
        Dialog dialog = getDialog();
        if (dialog == null || !dialog.isShowing()) {
            onClick();
        }
    }
}
