package android.support.v4.widget;

import android.widget.PopupWindow;
import java.lang.reflect.Method;
/* loaded from: a.zip:android/support/v4/widget/PopupWindowCompatGingerbread.class */
class PopupWindowCompatGingerbread {
    private static Method sSetWindowLayoutTypeMethod;
    private static boolean sSetWindowLayoutTypeMethodAttempted;

    PopupWindowCompatGingerbread() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void setWindowLayoutType(PopupWindow popupWindow, int i) {
        if (!sSetWindowLayoutTypeMethodAttempted) {
            try {
                sSetWindowLayoutTypeMethod = PopupWindow.class.getDeclaredMethod("setWindowLayoutType", Integer.TYPE);
                sSetWindowLayoutTypeMethod.setAccessible(true);
            } catch (Exception e) {
            }
            sSetWindowLayoutTypeMethodAttempted = true;
        }
        if (sSetWindowLayoutTypeMethod != null) {
            try {
                sSetWindowLayoutTypeMethod.invoke(popupWindow, Integer.valueOf(i));
            } catch (Exception e2) {
            }
        }
    }
}
