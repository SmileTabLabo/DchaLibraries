package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import com.android.settingslib.drawable.UserIconDrawable;
import com.android.systemui.R$styleable;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/UserAvatarView.class */
public class UserAvatarView extends View {
    private final UserIconDrawable mDrawable;

    public UserAvatarView(Context context) {
        this(context, null);
    }

    public UserAvatarView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public UserAvatarView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public UserAvatarView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mDrawable = new UserIconDrawable();
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.UserAvatarView, i, i2);
        int indexCount = obtainStyledAttributes.getIndexCount();
        for (int i3 = 0; i3 < indexCount; i3++) {
            int index = obtainStyledAttributes.getIndex(i3);
            switch (index) {
                case 0:
                    setFrameColor(obtainStyledAttributes.getColorStateList(index));
                    break;
                case 1:
                    setAvatarPadding(obtainStyledAttributes.getDimension(index, 0.0f));
                    break;
                case 2:
                    setFrameWidth(obtainStyledAttributes.getDimension(index, 0.0f));
                    break;
                case 3:
                    setFramePadding(obtainStyledAttributes.getDimension(index, 0.0f));
                    break;
                case 5:
                    setBadgeDiameter(obtainStyledAttributes.getDimension(index, 0.0f));
                    break;
                case 6:
                    setBadgeMargin(obtainStyledAttributes.getDimension(index, 0.0f));
                    break;
            }
        }
        obtainStyledAttributes.recycle();
        setBackground(this.mDrawable);
    }

    public void setAvatarPadding(float f) {
        this.mDrawable.setPadding(f);
    }

    public void setAvatarWithBadge(Bitmap bitmap, int i) {
        this.mDrawable.setIcon(bitmap);
        this.mDrawable.setBadgeIfManagedUser(getContext(), i);
    }

    public void setBadgeDiameter(float f) {
        this.mDrawable.setBadgeRadius(0.5f * f);
    }

    public void setBadgeMargin(float f) {
        this.mDrawable.setBadgeMargin(f);
    }

    public void setDrawableWithBadge(Drawable drawable, int i) {
        if (drawable instanceof UserIconDrawable) {
            throw new RuntimeException("Recursively adding UserIconDrawable");
        }
        this.mDrawable.setIconDrawable(drawable);
        this.mDrawable.setBadgeIfManagedUser(getContext(), i);
    }

    public void setFrameColor(ColorStateList colorStateList) {
        this.mDrawable.setFrameColor(colorStateList);
    }

    public void setFramePadding(float f) {
        this.mDrawable.setFramePadding(f);
    }

    public void setFrameWidth(float f) {
        this.mDrawable.setFrameWidth(f);
    }
}
