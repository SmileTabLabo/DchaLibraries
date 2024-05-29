package com.android.settingslib;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.UserHandle;
import android.support.v14.preference.SwitchPreference;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import com.android.settingslib.RestrictedLockUtils;
/* loaded from: classes.dex */
public class RestrictedSwitchPreference extends SwitchPreference {
    RestrictedPreferenceHelper mHelper;
    String mRestrictedSwitchSummary;
    boolean mUseAdditionalSummary;

    public RestrictedSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        boolean z;
        this.mUseAdditionalSummary = false;
        this.mRestrictedSwitchSummary = null;
        setWidgetLayoutResource(R$layout.restricted_switch_widget);
        this.mHelper = new RestrictedPreferenceHelper(context, this, attrs);
        if (attrs != null) {
            TypedArray attributes = context.obtainStyledAttributes(attrs, R$styleable.RestrictedSwitchPreference);
            TypedValue useAdditionalSummary = attributes.peekValue(R$styleable.RestrictedSwitchPreference_useAdditionalSummary);
            if (useAdditionalSummary != null) {
                if (useAdditionalSummary.type == 18) {
                    z = useAdditionalSummary.data != 0;
                } else {
                    z = false;
                }
                this.mUseAdditionalSummary = z;
            }
            TypedValue restrictedSwitchSummary = attributes.peekValue(R$styleable.RestrictedSwitchPreference_restrictedSwitchSummary);
            CharSequence data = null;
            if (restrictedSwitchSummary != null && restrictedSwitchSummary.type == 3) {
                data = restrictedSwitchSummary.resourceId != 0 ? context.getString(restrictedSwitchSummary.resourceId) : restrictedSwitchSummary.string;
            }
            this.mRestrictedSwitchSummary = data == null ? null : data.toString();
        }
        if (this.mRestrictedSwitchSummary == null) {
            this.mRestrictedSwitchSummary = context.getString(R$string.disabled_by_admin);
        }
        if (!this.mUseAdditionalSummary) {
            return;
        }
        setLayoutResource(R$layout.restricted_switch_preference);
        useAdminDisabledSummary(false);
    }

    public RestrictedSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RestrictedSwitchPreference(Context context, AttributeSet attrs) {
        this(context, attrs, TypedArrayUtils.getAttr(context, android.support.v7.preference.R$attr.switchPreferenceStyle, 16843629));
    }

    public RestrictedSwitchPreference(Context context) {
        this(context, null);
    }

    @Override // android.support.v14.preference.SwitchPreference, android.support.v7.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        this.mHelper.onBindViewHolder(holder);
        View restrictedIcon = holder.findViewById(R$id.restricted_icon);
        View switchWidget = holder.findViewById(16908352);
        if (restrictedIcon != null) {
            restrictedIcon.setVisibility(isDisabledByAdmin() ? 0 : 8);
        }
        if (switchWidget != null) {
            switchWidget.setVisibility(isDisabledByAdmin() ? 8 : 0);
        }
        if (this.mUseAdditionalSummary) {
            TextView additionalSummaryView = (TextView) holder.findViewById(R$id.additional_summary);
            if (additionalSummaryView == null) {
                return;
            }
            if (isDisabledByAdmin()) {
                additionalSummaryView.setText(this.mRestrictedSwitchSummary);
                additionalSummaryView.setVisibility(0);
                return;
            }
            additionalSummaryView.setVisibility(8);
            return;
        }
        TextView summaryView = (TextView) holder.findViewById(16908304);
        if (summaryView == null || !isDisabledByAdmin()) {
            return;
        }
        summaryView.setText(this.mRestrictedSwitchSummary);
        summaryView.setVisibility(0);
    }

    @Override // android.support.v7.preference.Preference
    public void performClick() {
        if (this.mHelper.performClick()) {
            return;
        }
        super.performClick();
    }

    public void useAdminDisabledSummary(boolean useSummary) {
        this.mHelper.useAdminDisabledSummary(useSummary);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.preference.Preference
    public void onAttachedToHierarchy(PreferenceManager preferenceManager) {
        this.mHelper.onAttachedToHierarchy();
        super.onAttachedToHierarchy(preferenceManager);
    }

    public void checkRestrictionAndSetDisabled(String userRestriction) {
        this.mHelper.checkRestrictionAndSetDisabled(userRestriction, UserHandle.myUserId());
    }

    public void checkRestrictionAndSetDisabled(String userRestriction, int userId) {
        this.mHelper.checkRestrictionAndSetDisabled(userRestriction, userId);
    }

    @Override // android.support.v7.preference.Preference
    public void setEnabled(boolean enabled) {
        if (enabled && isDisabledByAdmin()) {
            this.mHelper.setDisabledByAdmin(null);
        } else {
            super.setEnabled(enabled);
        }
    }

    public void setDisabledByAdmin(RestrictedLockUtils.EnforcedAdmin admin) {
        if (!this.mHelper.setDisabledByAdmin(admin)) {
            return;
        }
        notifyChanged();
    }

    public boolean isDisabledByAdmin() {
        return this.mHelper.isDisabledByAdmin();
    }
}
