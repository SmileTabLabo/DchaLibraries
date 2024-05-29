package com.android.settingslib.bluetooth;

import android.content.Context;
/* loaded from: a.zip:com/android/settingslib/bluetooth/Utils.class */
public class Utils {
    private static ErrorListener sErrorListener;

    /* loaded from: a.zip:com/android/settingslib/bluetooth/Utils$ErrorListener.class */
    public interface ErrorListener {
        void onShowError(Context context, String str, int i);
    }

    public static void setErrorListener(ErrorListener errorListener) {
        sErrorListener = errorListener;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void showError(Context context, String str, int i) {
        if (sErrorListener != null) {
            sErrorListener.onShowError(context, str, i);
        }
    }
}
