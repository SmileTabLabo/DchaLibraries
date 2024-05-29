package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.util.ArrayUtils;
import com.android.systemui.FontSizeUtils;
import com.android.systemui.R$styleable;
import com.android.systemui.statusbar.phone.UserAvatarView;
/* loaded from: a.zip:com/android/systemui/qs/tiles/UserDetailItemView.class */
public class UserDetailItemView extends LinearLayout {
    private Typeface mActivatedTypeface;
    private UserAvatarView mAvatar;
    private TextView mName;
    private Typeface mRegularTypeface;
    private View mRestrictedPadlock;

    public UserDetailItemView(Context context) {
        this(context, null);
    }

    public UserDetailItemView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public UserDetailItemView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public UserDetailItemView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.UserDetailItemView, i, i2);
        int indexCount = obtainStyledAttributes.getIndexCount();
        for (int i3 = 0; i3 < indexCount; i3++) {
            int index = obtainStyledAttributes.getIndex(i3);
            switch (index) {
                case 0:
                    this.mRegularTypeface = Typeface.create(obtainStyledAttributes.getString(index), 0);
                    break;
                case 1:
                    this.mActivatedTypeface = Typeface.create(obtainStyledAttributes.getString(index), 0);
                    break;
            }
        }
        obtainStyledAttributes.recycle();
    }

    public static UserDetailItemView convertOrInflate(Context context, View view, ViewGroup viewGroup) {
        View view2 = view;
        if (!(view instanceof UserDetailItemView)) {
            view2 = LayoutInflater.from(context).inflate(2130968769, viewGroup, false);
        }
        return (UserDetailItemView) view2;
    }

    private String cutNameIfTooLong(String str) {
        String str2 = str;
        if (str.length() > 30) {
            str2 = str.subSequence(0, 30) + "...";
        }
        return str2;
    }

    private void updateTypeface() {
        this.mName.setTypeface(ArrayUtils.contains(getDrawableState(), 16843518) ? this.mActivatedTypeface : this.mRegularTypeface);
    }

    public void bind(String str, Bitmap bitmap, int i) {
        this.mName.setText(cutNameIfTooLong(str));
        this.mAvatar.setAvatarWithBadge(bitmap, i);
    }

    public void bind(String str, Drawable drawable, int i) {
        this.mName.setText(cutNameIfTooLong(str));
        this.mAvatar.setDrawableWithBadge(drawable, i);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        updateTypeface();
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        FontSizeUtils.updateFontSize(this.mName, 2131689853);
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        this.mAvatar = (UserAvatarView) findViewById(2131886359);
        this.mName = (TextView) findViewById(2131886264);
        if (this.mRegularTypeface == null) {
            this.mRegularTypeface = this.mName.getTypeface();
        }
        if (this.mActivatedTypeface == null) {
            this.mActivatedTypeface = this.mName.getTypeface();
        }
        updateTypeface();
        this.mRestrictedPadlock = findViewById(2131886590);
    }

    public void setAvatarEnabled(boolean z) {
        this.mAvatar.setEnabled(z);
    }

    public void setDisabledByAdmin(boolean z) {
        this.mRestrictedPadlock.setVisibility(z ? 0 : 8);
        this.mName.setEnabled(!z);
        this.mAvatar.setEnabled(!z);
    }

    @Override // android.view.View
    public void setEnabled(boolean z) {
        this.mName.setEnabled(z);
        this.mAvatar.setEnabled(z);
    }
}
