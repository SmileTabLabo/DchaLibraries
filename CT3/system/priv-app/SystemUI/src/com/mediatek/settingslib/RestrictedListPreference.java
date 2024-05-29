package com.mediatek.settingslib;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceViewHolder;
import android.support.v7.preference.R$attr;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import com.android.settingslib.R$id;
import com.android.settingslib.R$layout;
import com.android.settingslib.R$string;
import com.android.settingslib.R$styleable;
import com.android.settingslib.RestrictedPreferenceHelper;
/* loaded from: a.zip:com/mediatek/settingslib/RestrictedListPreference.class */
public class RestrictedListPreference extends ListPreference {
    RestrictedPreferenceHelper mHelper;
    String mRestrictedSwitchSummary;
    boolean mUseAdditionalSummary;

    public RestrictedListPreference(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, TypedArrayUtils.getAttr(context, R$attr.preferenceStyle, 16842894));
    }

    public RestrictedListPreference(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v34, types: [java.lang.CharSequence] */
    public RestrictedListPreference(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mUseAdditionalSummary = false;
        this.mRestrictedSwitchSummary = null;
        setWidgetLayoutResource(R$layout.restricted_icon);
        this.mHelper = new RestrictedPreferenceHelper(context, this, attributeSet);
        if (attributeSet != null) {
            TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.RestrictedSwitchPreference);
            TypedValue peekValue = obtainStyledAttributes.peekValue(R$styleable.RestrictedSwitchPreference_useAdditionalSummary);
            if (peekValue != null) {
                this.mUseAdditionalSummary = peekValue.type == 18 ? peekValue.data != 0 : false;
            }
            TypedValue peekValue2 = obtainStyledAttributes.peekValue(R$styleable.RestrictedSwitchPreference_restrictedSwitchSummary);
            String str = null;
            if (peekValue2 != null) {
                str = null;
                if (peekValue2.type == 3) {
                    str = peekValue2.resourceId != 0 ? context.getString(peekValue2.resourceId) : peekValue2.string;
                }
            }
            this.mRestrictedSwitchSummary = str == null ? null : str.toString();
        }
        if (this.mRestrictedSwitchSummary == null) {
            this.mRestrictedSwitchSummary = context.getString(R$string.disabled_by_admin);
        }
        if (this.mUseAdditionalSummary) {
            setLayoutResource(R$layout.restricted_switch_preference);
            useAdminDisabledSummary(false);
        }
    }

    public boolean isDisabledByAdmin() {
        return this.mHelper.isDisabledByAdmin();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.preference.Preference
    public void onAttachedToHierarchy(PreferenceManager preferenceManager) {
        this.mHelper.onAttachedToHierarchy();
        super.onAttachedToHierarchy(preferenceManager);
    }

    @Override // android.support.v7.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
        super.onBindViewHolder(preferenceViewHolder);
        this.mHelper.onBindViewHolder(preferenceViewHolder);
        View findViewById = preferenceViewHolder.findViewById(R$id.restricted_icon);
        if (findViewById != null) {
            findViewById.setVisibility(isDisabledByAdmin() ? 0 : 8);
        }
        if (!this.mUseAdditionalSummary) {
            TextView textView = (TextView) preferenceViewHolder.findViewById(16908304);
            if (textView == null || !isDisabledByAdmin()) {
                return;
            }
            textView.setText(this.mRestrictedSwitchSummary);
            textView.setVisibility(0);
            return;
        }
        TextView textView2 = (TextView) preferenceViewHolder.findViewById(R$id.additional_summary);
        if (textView2 != null) {
            if (!isDisabledByAdmin()) {
                textView2.setVisibility(8);
                return;
            }
            textView2.setText(this.mRestrictedSwitchSummary);
            textView2.setVisibility(0);
        }
    }

    @Override // android.support.v7.preference.Preference
    public void performClick() {
        if (this.mHelper.performClick()) {
            return;
        }
        super.performClick();
    }

    @Override // android.support.v7.preference.Preference
    public void setEnabled(boolean z) {
        if (z && isDisabledByAdmin()) {
            this.mHelper.setDisabledByAdmin(null);
        } else {
            super.setEnabled(z);
        }
    }

    public void useAdminDisabledSummary(boolean z) {
        this.mHelper.useAdminDisabledSummary(z);
    }
}
