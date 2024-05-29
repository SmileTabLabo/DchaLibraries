package android.support.v7.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Checkable;
import android.widget.CompoundButton;
/* loaded from: a.zip:android/support/v7/preference/SwitchPreferenceCompat.class */
public class SwitchPreferenceCompat extends TwoStatePreference {
    private final Listener mListener;
    private CharSequence mSwitchOff;
    private CharSequence mSwitchOn;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:android/support/v7/preference/SwitchPreferenceCompat$Listener.class */
    public class Listener implements CompoundButton.OnCheckedChangeListener {
        final SwitchPreferenceCompat this$0;

        private Listener(SwitchPreferenceCompat switchPreferenceCompat) {
            this.this$0 = switchPreferenceCompat;
        }

        /* synthetic */ Listener(SwitchPreferenceCompat switchPreferenceCompat, Listener listener) {
            this(switchPreferenceCompat);
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

    public SwitchPreferenceCompat(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R$attr.switchPreferenceCompatStyle);
    }

    public SwitchPreferenceCompat(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public SwitchPreferenceCompat(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mListener = new Listener(this, null);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.SwitchPreferenceCompat, i, i2);
        setSummaryOn(TypedArrayUtils.getString(obtainStyledAttributes, R$styleable.SwitchPreferenceCompat_summaryOn, R$styleable.SwitchPreferenceCompat_android_summaryOn));
        setSummaryOff(TypedArrayUtils.getString(obtainStyledAttributes, R$styleable.SwitchPreferenceCompat_summaryOff, R$styleable.SwitchPreferenceCompat_android_summaryOff));
        setSwitchTextOn(TypedArrayUtils.getString(obtainStyledAttributes, R$styleable.SwitchPreferenceCompat_switchTextOn, R$styleable.SwitchPreferenceCompat_android_switchTextOn));
        setSwitchTextOff(TypedArrayUtils.getString(obtainStyledAttributes, R$styleable.SwitchPreferenceCompat_switchTextOff, R$styleable.SwitchPreferenceCompat_android_switchTextOff));
        setDisableDependentsState(TypedArrayUtils.getBoolean(obtainStyledAttributes, R$styleable.SwitchPreferenceCompat_disableDependentsState, R$styleable.SwitchPreferenceCompat_android_disableDependentsState, false));
        obtainStyledAttributes.recycle();
    }

    private void syncSwitchView(View view) {
        if (view instanceof SwitchCompat) {
            ((SwitchCompat) view).setOnCheckedChangeListener(null);
        }
        if (view instanceof Checkable) {
            ((Checkable) view).setChecked(this.mChecked);
        }
        if (view instanceof SwitchCompat) {
            SwitchCompat switchCompat = (SwitchCompat) view;
            switchCompat.setTextOn(this.mSwitchOn);
            switchCompat.setTextOff(this.mSwitchOff);
            switchCompat.setOnCheckedChangeListener(this.mListener);
        }
    }

    private void syncViewIfAccessibilityEnabled(View view) {
        if (((AccessibilityManager) getContext().getSystemService("accessibility")).isEnabled()) {
            syncSwitchView(view.findViewById(R$id.switchWidget));
            syncSummaryView(view.findViewById(16908304));
        }
    }

    @Override // android.support.v7.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
        super.onBindViewHolder(preferenceViewHolder);
        syncSwitchView(preferenceViewHolder.findViewById(R$id.switchWidget));
        syncSummaryView(preferenceViewHolder);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.preference.Preference
    public void performClick(View view) {
        super.performClick(view);
        syncViewIfAccessibilityEnabled(view);
    }

    public void setSwitchTextOff(CharSequence charSequence) {
        this.mSwitchOff = charSequence;
        notifyChanged();
    }

    public void setSwitchTextOn(CharSequence charSequence) {
        this.mSwitchOn = charSequence;
        notifyChanged();
    }
}
