package com.android.settingslib.bluetooth;

import android.content.Context;
/* loaded from: classes.dex */
public class Utils {
    private static ErrorListener sErrorListener;

    /* loaded from: classes.dex */
    public interface ErrorListener {
        void onShowError(Context context, String str, int i);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void showError(Context context, String str, int i) {
        if (sErrorListener != null) {
            sErrorListener.onShowError(context, str, i);
        }
    }

    public static void setErrorListener(ErrorListener errorListener) {
        sErrorListener = errorListener;
    }
}
