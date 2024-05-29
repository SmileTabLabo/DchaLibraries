package com.android.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v14.preference.EditTextPreferenceDialogFragment;
import android.support.v7.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
/* loaded from: classes.dex */
public class CustomEditTextPreference extends EditTextPreference {
    private CustomPreferenceDialogFragment mFragment;

    public CustomEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CustomEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomEditTextPreference(Context context) {
        super(context);
    }

    public EditText getEditText() {
        if (this.mFragment != null) {
            return (EditText) this.mFragment.getDialog().findViewById(16908291);
        }
        return null;
    }

    public Dialog getDialog() {
        if (this.mFragment != null) {
            return this.mFragment.getDialog();
        }
        return null;
    }

    protected void onPrepareDialogBuilder(AlertDialog.Builder builder, DialogInterface.OnClickListener listener) {
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onDialogClosed(boolean positiveResult) {
    }

    protected void onClick(DialogInterface dialog, int which) {
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onBindDialogView(View view) {
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setFragment(CustomPreferenceDialogFragment fragment) {
        this.mFragment = fragment;
    }

    /* loaded from: classes.dex */
    public static class CustomPreferenceDialogFragment extends EditTextPreferenceDialogFragment {
        public static CustomPreferenceDialogFragment newInstance(String key) {
            CustomPreferenceDialogFragment fragment = new CustomPreferenceDialogFragment();
            Bundle b = new Bundle(1);
            b.putString("key", key);
            fragment.setArguments(b);
            return fragment;
        }

        private CustomEditTextPreference getCustomizablePreference() {
            return (CustomEditTextPreference) getPreference();
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.support.v14.preference.EditTextPreferenceDialogFragment, android.support.v14.preference.PreferenceDialogFragment
        public void onBindDialogView(View view) {
            super.onBindDialogView(view);
            getCustomizablePreference().onBindDialogView(view);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.support.v14.preference.PreferenceDialogFragment
        public void onPrepareDialogBuilder(AlertDialog.Builder builder) {
            super.onPrepareDialogBuilder(builder);
            getCustomizablePreference().setFragment(this);
            getCustomizablePreference().onPrepareDialogBuilder(builder, this);
        }

        @Override // android.support.v14.preference.EditTextPreferenceDialogFragment, android.support.v14.preference.PreferenceDialogFragment
        public void onDialogClosed(boolean positiveResult) {
            super.onDialogClosed(positiveResult);
            getCustomizablePreference().onDialogClosed(positiveResult);
        }

        @Override // android.support.v14.preference.PreferenceDialogFragment, android.content.DialogInterface.OnClickListener
        public void onClick(DialogInterface dialog, int which) {
            super.onClick(dialog, which);
            getCustomizablePreference().onClick(dialog, which);
        }
    }
}
