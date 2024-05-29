package android.support.v4.widget;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.CompoundButton;
/* loaded from: a.zip:android/support/v4/widget/CompoundButtonCompat.class */
public final class CompoundButtonCompat {
    private static final CompoundButtonCompatImpl IMPL;

    /* loaded from: a.zip:android/support/v4/widget/CompoundButtonCompat$Api23CompoundButtonImpl.class */
    static class Api23CompoundButtonImpl extends LollipopCompoundButtonImpl {
        Api23CompoundButtonImpl() {
        }

        @Override // android.support.v4.widget.CompoundButtonCompat.BaseCompoundButtonCompat, android.support.v4.widget.CompoundButtonCompat.CompoundButtonCompatImpl
        public Drawable getButtonDrawable(CompoundButton compoundButton) {
            return CompoundButtonCompatApi23.getButtonDrawable(compoundButton);
        }
    }

    /* loaded from: a.zip:android/support/v4/widget/CompoundButtonCompat$BaseCompoundButtonCompat.class */
    static class BaseCompoundButtonCompat implements CompoundButtonCompatImpl {
        BaseCompoundButtonCompat() {
        }

        @Override // android.support.v4.widget.CompoundButtonCompat.CompoundButtonCompatImpl
        public Drawable getButtonDrawable(CompoundButton compoundButton) {
            return CompoundButtonCompatDonut.getButtonDrawable(compoundButton);
        }

        @Override // android.support.v4.widget.CompoundButtonCompat.CompoundButtonCompatImpl
        public void setButtonTintList(CompoundButton compoundButton, ColorStateList colorStateList) {
            CompoundButtonCompatDonut.setButtonTintList(compoundButton, colorStateList);
        }

        @Override // android.support.v4.widget.CompoundButtonCompat.CompoundButtonCompatImpl
        public void setButtonTintMode(CompoundButton compoundButton, PorterDuff.Mode mode) {
            CompoundButtonCompatDonut.setButtonTintMode(compoundButton, mode);
        }
    }

    /* loaded from: a.zip:android/support/v4/widget/CompoundButtonCompat$CompoundButtonCompatImpl.class */
    interface CompoundButtonCompatImpl {
        Drawable getButtonDrawable(CompoundButton compoundButton);

        void setButtonTintList(CompoundButton compoundButton, ColorStateList colorStateList);

        void setButtonTintMode(CompoundButton compoundButton, PorterDuff.Mode mode);
    }

    /* loaded from: a.zip:android/support/v4/widget/CompoundButtonCompat$LollipopCompoundButtonImpl.class */
    static class LollipopCompoundButtonImpl extends BaseCompoundButtonCompat {
        LollipopCompoundButtonImpl() {
        }

        @Override // android.support.v4.widget.CompoundButtonCompat.BaseCompoundButtonCompat, android.support.v4.widget.CompoundButtonCompat.CompoundButtonCompatImpl
        public void setButtonTintList(CompoundButton compoundButton, ColorStateList colorStateList) {
            CompoundButtonCompatLollipop.setButtonTintList(compoundButton, colorStateList);
        }

        @Override // android.support.v4.widget.CompoundButtonCompat.BaseCompoundButtonCompat, android.support.v4.widget.CompoundButtonCompat.CompoundButtonCompatImpl
        public void setButtonTintMode(CompoundButton compoundButton, PorterDuff.Mode mode) {
            CompoundButtonCompatLollipop.setButtonTintMode(compoundButton, mode);
        }
    }

    static {
        int i = Build.VERSION.SDK_INT;
        if (i >= 23) {
            IMPL = new Api23CompoundButtonImpl();
        } else if (i >= 21) {
            IMPL = new LollipopCompoundButtonImpl();
        } else {
            IMPL = new BaseCompoundButtonCompat();
        }
    }

    private CompoundButtonCompat() {
    }

    @Nullable
    public static Drawable getButtonDrawable(@NonNull CompoundButton compoundButton) {
        return IMPL.getButtonDrawable(compoundButton);
    }

    public static void setButtonTintList(@NonNull CompoundButton compoundButton, @Nullable ColorStateList colorStateList) {
        IMPL.setButtonTintList(compoundButton, colorStateList);
    }

    public static void setButtonTintMode(@NonNull CompoundButton compoundButton, @Nullable PorterDuff.Mode mode) {
        IMPL.setButtonTintMode(compoundButton, mode);
    }
}
