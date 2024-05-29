package android.support.v4.app;

import android.app.RemoteInput;
/* loaded from: classes.dex */
class RemoteInputCompatApi20 {
    RemoteInputCompatApi20() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static android.app.RemoteInput[] fromCompat(RemoteInputCompatBase$RemoteInput[] srcArray) {
        if (srcArray == null) {
            return null;
        }
        android.app.RemoteInput[] result = new android.app.RemoteInput[srcArray.length];
        for (int i = 0; i < srcArray.length; i++) {
            RemoteInputCompatBase$RemoteInput src = srcArray[i];
            result[i] = new RemoteInput.Builder(src.getResultKey()).setLabel(src.getLabel()).setChoices(src.getChoices()).setAllowFreeFormInput(src.getAllowFreeFormInput()).addExtras(src.getExtras()).build();
        }
        return result;
    }
}
