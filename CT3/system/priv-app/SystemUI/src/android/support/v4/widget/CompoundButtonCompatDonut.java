package android.support.v4.widget;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.CompoundButton;
import java.lang.reflect.Field;
/* loaded from: a.zip:android/support/v4/widget/CompoundButtonCompatDonut.class */
class CompoundButtonCompatDonut {
    private static Field sButtonDrawableField;
    private static boolean sButtonDrawableFieldFetched;

    CompoundButtonCompatDonut() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static Drawable getButtonDrawable(CompoundButton compoundButton) {
        if (!sButtonDrawableFieldFetched) {
            try {
                sButtonDrawableField = CompoundButton.class.getDeclaredField("mButtonDrawable");
                sButtonDrawableField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                Log.i("CompoundButtonCompatDonut", "Failed to retrieve mButtonDrawable field", e);
            }
            sButtonDrawableFieldFetched = true;
        }
        if (sButtonDrawableField != null) {
            try {
                return (Drawable) sButtonDrawableField.get(compoundButton);
            } catch (IllegalAccessException e2) {
                Log.i("CompoundButtonCompatDonut", "Failed to get button drawable via reflection", e2);
                sButtonDrawableField = null;
                return null;
            }
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void setButtonTintList(CompoundButton compoundButton, ColorStateList colorStateList) {
        if (compoundButton instanceof TintableCompoundButton) {
            ((TintableCompoundButton) compoundButton).setSupportButtonTintList(colorStateList);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void setButtonTintMode(CompoundButton compoundButton, PorterDuff.Mode mode) {
        if (compoundButton instanceof TintableCompoundButton) {
            ((TintableCompoundButton) compoundButton).setSupportButtonTintMode(mode);
        }
    }
}
