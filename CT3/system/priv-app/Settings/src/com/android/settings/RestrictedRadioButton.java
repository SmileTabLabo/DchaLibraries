package com.android.settings;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.widget.RadioButton;
import com.android.settingslib.RestrictedLockUtils;
/* loaded from: classes.dex */
public class RestrictedRadioButton extends RadioButton {
    private Context mContext;
    private boolean mDisabledByAdmin;
    private RestrictedLockUtils.EnforcedAdmin mEnforcedAdmin;

    public RestrictedRadioButton(Context context) {
        this(context, null);
    }

    public RestrictedRadioButton(Context context, AttributeSet attrs) {
        this(context, attrs, 16842878);
    }

    public RestrictedRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RestrictedRadioButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mContext = context;
    }

    @Override // android.widget.CompoundButton, android.view.View
    public boolean performClick() {
        if (this.mDisabledByAdmin) {
            RestrictedLockUtils.sendShowAdminSupportDetailsIntent(this.mContext, this.mEnforcedAdmin);
            return true;
        }
        return super.performClick();
    }

    public void setDisabledByAdmin(RestrictedLockUtils.EnforcedAdmin admin) {
        boolean disabled = admin != null;
        this.mEnforcedAdmin = admin;
        if (this.mDisabledByAdmin == disabled) {
            return;
        }
        this.mDisabledByAdmin = disabled;
        RestrictedLockUtils.setTextViewAsDisabledByAdmin(this.mContext, this, this.mDisabledByAdmin);
        if (this.mDisabledByAdmin) {
            getButtonDrawable().setColorFilter(this.mContext.getColor(R.color.disabled_text_color), PorterDuff.Mode.MULTIPLY);
        } else {
            getButtonDrawable().clearColorFilter();
        }
    }

    public boolean isDisabledByAdmin() {
        return this.mDisabledByAdmin;
    }
}
