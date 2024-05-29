package android.support.v4.widget;

import android.widget.PopupWindow;
import java.lang.reflect.Method;
/* loaded from: classes.dex */
class PopupWindowCompatGingerbread {
    private static Method sSetWindowLayoutTypeMethod;
    private static boolean sSetWindowLayoutTypeMethodAttempted;

    PopupWindowCompatGingerbread() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void setWindowLayoutType(PopupWindow popupWindow, int layoutType) {
        if (!sSetWindowLayoutTypeMethodAttempted) {
            try {
                sSetWindowLayoutTypeMethod = PopupWindow.class.getDeclaredMethod("setWindowLayoutType", Integer.TYPE);
                sSetWindowLayoutTypeMethod.setAccessible(true);
            } catch (Exception e) {
            }
            sSetWindowLayoutTypeMethodAttempted = true;
        }
        if (sSetWindowLayoutTypeMethod == null) {
            return;
        }
        try {
            sSetWindowLayoutTypeMethod.invoke(popupWindow, Integer.valueOf(layoutType));
        } catch (Exception e2) {
        }
    }
}
