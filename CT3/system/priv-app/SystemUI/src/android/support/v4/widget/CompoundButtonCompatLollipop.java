package android.support.v4.widget;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.widget.CompoundButton;
/* loaded from: a.zip:android/support/v4/widget/CompoundButtonCompatLollipop.class */
class CompoundButtonCompatLollipop {
    CompoundButtonCompatLollipop() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void setButtonTintList(CompoundButton compoundButton, ColorStateList colorStateList) {
        compoundButton.setButtonTintList(colorStateList);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void setButtonTintMode(CompoundButton compoundButton, PorterDuff.Mode mode) {
        compoundButton.setButtonTintMode(mode);
    }
}
