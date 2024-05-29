package com.android.systemui;

import android.content.Context;
import android.widget.Toast;
/* loaded from: a.zip:com/android/systemui/SysUIToast.class */
public class SysUIToast {
    public static Toast makeText(Context context, CharSequence charSequence, int i) {
        Toast makeText = Toast.makeText(context, charSequence, i);
        makeText.getWindowParams().privateFlags |= 16;
        return makeText;
    }
}
