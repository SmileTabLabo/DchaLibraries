package android.support.v7.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.content.res.TypedArrayUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Checkable;
import android.widget.CompoundButton;
/* loaded from: a.zip:android/support/v7/preference/CheckBoxPreference.class */
public class CheckBoxPreference extends TwoStatePreference {
    private final Listener mListener;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:android/support/v7/preference/CheckBoxPreference$Listener.class */
    public class Listener implements CompoundButton.OnCheckedChangeListener {
        final CheckBoxPreference this$0;

        private Listener(CheckBoxPreference checkBoxPreference) {
            this.this$0 = checkBoxPreference;
        }

        /* synthetic */ Listener(CheckBoxPreference checkBoxPreference, Listener listener) {
            this(checkBoxPreference);
        }

        @Override // android.widget.CompoundButton.OnCheckedChangeListener
        public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
            if (this.this$0.callChangeListener(Boolean.valueOf(z))) {
                this.this$0.setChecked(z);
            } else {
                compoundButton.setChecked(!z);
            }
        }
    }

    public CheckBoxPreference(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, TypedArrayUtils.getAttr(context, R$attr.checkBoxPreferenceStyle, 16842895));
    }

    public CheckBoxPreference(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public CheckBoxPreference(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mListener = new Listener(this, null);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.CheckBoxPreference, i, i2);
        setSummaryOn(TypedArrayUtils.getString(obtainStyledAttributes, R$styleable.CheckBoxPreference_summaryOn, R$styleable.CheckBoxPreference_android_summaryOn));
        setSummaryOff(TypedArrayUtils.getString(obtainStyledAttributes, R$styleable.CheckBoxPreference_summaryOff, R$styleable.CheckBoxPreference_android_summaryOff));
        setDisableDependentsState(TypedArrayUtils.getBoolean(obtainStyledAttributes, R$styleable.CheckBoxPreference_disableDependentsState, R$styleable.CheckBoxPreference_android_disableDependentsState, false));
        obtainStyledAttributes.recycle();
    }

    private void syncCheckboxView(View view) {
        if (view instanceof CompoundButton) {
            ((CompoundButton) view).setOnCheckedChangeListener(null);
        }
        if (view instanceof Checkable) {
            ((Checkable) view).setChecked(this.mChecked);
        }
        if (view instanceof CompoundButton) {
            ((CompoundButton) view).setOnCheckedChangeListener(this.mListener);
        }
    }

    private void syncViewIfAccessibilityEnabled(View view) {
        if (((AccessibilityManager) getContext().getSystemService("accessibility")).isEnabled()) {
            syncCheckboxView(view.findViewById(16908289));
            syncSummaryView(view.findViewById(16908304));
        }
    }

    @Override // android.support.v7.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
        super.onBindViewHolder(preferenceViewHolder);
        syncCheckboxView(preferenceViewHolder.findViewById(16908289));
        syncSummaryView(preferenceViewHolder);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.preference.Preference
    public void performClick(View view) {
        super.performClick(view);
        syncViewIfAccessibilityEnabled(view);
    }
}
