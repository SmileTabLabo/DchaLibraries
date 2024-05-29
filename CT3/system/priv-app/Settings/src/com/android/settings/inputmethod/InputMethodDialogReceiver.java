package com.android.settings.inputmethod;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.inputmethod.InputMethodManager;
/* loaded from: classes.dex */
public class InputMethodDialogReceiver extends BroadcastReceiver {
    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        if (!"android.settings.SHOW_INPUT_METHOD_PICKER".equals(intent.getAction())) {
            return;
        }
        ((InputMethodManager) context.getSystemService("input_method")).showInputMethodPicker(true);
    }
}
