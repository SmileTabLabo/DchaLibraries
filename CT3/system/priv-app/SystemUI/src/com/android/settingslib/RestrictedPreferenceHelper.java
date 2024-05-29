package com.android.settingslib;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;
import com.android.settingslib.RestrictedLockUtils;
/* loaded from: a.zip:com/android/settingslib/RestrictedPreferenceHelper.class */
public class RestrictedPreferenceHelper {
    private String mAttrUserRestriction;
    private final Context mContext;
    private boolean mDisabledByAdmin;
    private RestrictedLockUtils.EnforcedAdmin mEnforcedAdmin;
    private final Preference mPreference;
    private boolean mUseAdminDisabledSummary;

    public RestrictedPreferenceHelper(Context context, Preference preference, AttributeSet attributeSet) {
        this.mAttrUserRestriction = null;
        this.mUseAdminDisabledSummary = false;
        this.mContext = context;
        this.mPreference = preference;
        if (attributeSet != null) {
            TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.RestrictedPreference);
            TypedValue peekValue = obtainStyledAttributes.peekValue(R$styleable.RestrictedPreference_userRestriction);
            CharSequence charSequence = null;
            if (peekValue != null) {
                charSequence = null;
                if (peekValue.type == 3) {
                    charSequence = peekValue.resourceId != 0 ? context.getText(peekValue.resourceId) : peekValue.string;
                }
            }
            this.mAttrUserRestriction = charSequence == null ? null : charSequence.toString();
            if (RestrictedLockUtils.hasBaseUserRestriction(this.mContext, this.mAttrUserRestriction, UserHandle.myUserId())) {
                this.mAttrUserRestriction = null;
                return;
            }
            TypedValue peekValue2 = obtainStyledAttributes.peekValue(R$styleable.RestrictedPreference_useAdminDisabledSummary);
            if (peekValue2 != null) {
                this.mUseAdminDisabledSummary = peekValue2.type == 18 ? peekValue2.data != 0 : false;
            }
        }
    }

    public void checkRestrictionAndSetDisabled(String str, int i) {
        setDisabledByAdmin(RestrictedLockUtils.checkIfRestrictionEnforced(this.mContext, str, i));
    }

    public boolean isDisabledByAdmin() {
        return this.mDisabledByAdmin;
    }

    public void onAttachedToHierarchy() {
        if (this.mAttrUserRestriction != null) {
            checkRestrictionAndSetDisabled(this.mAttrUserRestriction, UserHandle.myUserId());
        }
    }

    public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
        TextView textView;
        if (this.mDisabledByAdmin) {
            preferenceViewHolder.itemView.setEnabled(true);
        }
        if (!this.mUseAdminDisabledSummary || (textView = (TextView) preferenceViewHolder.findViewById(16908304)) == null) {
            return;
        }
        if (!this.mDisabledByAdmin) {
            textView.setVisibility(8);
            return;
        }
        textView.setText(R$string.disabled_by_admin_summary_text);
        textView.setVisibility(0);
    }

    public boolean performClick() {
        if (this.mDisabledByAdmin) {
            RestrictedLockUtils.sendShowAdminSupportDetailsIntent(this.mContext, this.mEnforcedAdmin);
            return true;
        }
        return false;
    }

    public boolean setDisabledByAdmin(RestrictedLockUtils.EnforcedAdmin enforcedAdmin) {
        boolean z = enforcedAdmin != null;
        this.mEnforcedAdmin = enforcedAdmin;
        boolean z2 = false;
        if (this.mDisabledByAdmin != z) {
            this.mDisabledByAdmin = z;
            z2 = true;
        }
        this.mPreference.setEnabled(!z);
        return z2;
    }

    public void useAdminDisabledSummary(boolean z) {
        this.mUseAdminDisabledSummary = z;
    }
}
