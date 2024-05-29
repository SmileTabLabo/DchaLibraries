package com.android.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v14.preference.PreferenceDialogFragment;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
/* loaded from: classes.dex */
public class CustomDialogPreference extends DialogPreference {
    private CustomPreferenceDialogFragment mFragment;

    public CustomDialogPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CustomDialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomDialogPreference(Context context) {
        super(context);
    }

    public Dialog getDialog() {
        if (this.mFragment != null) {
            return this.mFragment.getDialog();
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onPrepareDialogBuilder(AlertDialog.Builder builder, DialogInterface.OnClickListener listener) {
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
    public static class CustomPreferenceDialogFragment extends PreferenceDialogFragment {
        public static CustomPreferenceDialogFragment newInstance(String key) {
            CustomPreferenceDialogFragment fragment = new CustomPreferenceDialogFragment();
            Bundle b = new Bundle(1);
            b.putString("key", key);
            fragment.setArguments(b);
            return fragment;
        }

        private CustomDialogPreference getCustomizablePreference() {
            return (CustomDialogPreference) getPreference();
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.support.v14.preference.PreferenceDialogFragment
        public void onPrepareDialogBuilder(AlertDialog.Builder builder) {
            super.onPrepareDialogBuilder(builder);
            if (getCustomizablePreference() == null) {
                return;
            }
            getCustomizablePreference().setFragment(this);
            getCustomizablePreference().onPrepareDialogBuilder(builder, this);
        }

        @Override // android.support.v14.preference.PreferenceDialogFragment
        public void onDialogClosed(boolean positiveResult) {
            if (getCustomizablePreference() == null) {
                return;
            }
            getCustomizablePreference().onDialogClosed(positiveResult);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.support.v14.preference.PreferenceDialogFragment
        public void onBindDialogView(View view) {
            super.onBindDialogView(view);
            if (getCustomizablePreference() == null) {
                return;
            }
            getCustomizablePreference().onBindDialogView(view);
        }

        @Override // android.support.v14.preference.PreferenceDialogFragment, android.content.DialogInterface.OnClickListener
        public void onClick(DialogInterface dialog, int which) {
            super.onClick(dialog, which);
            if (getCustomizablePreference() == null) {
                return;
            }
            getCustomizablePreference().onClick(dialog, which);
        }
    }
}
