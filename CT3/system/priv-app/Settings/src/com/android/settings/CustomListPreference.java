package com.android.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v14.preference.ListPreferenceDialogFragment;
import android.support.v7.preference.ListPreference;
import android.util.AttributeSet;
/* loaded from: classes.dex */
public class CustomListPreference extends ListPreference {
    public CustomListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    protected void onPrepareDialogBuilder(AlertDialog.Builder builder, DialogInterface.OnClickListener listener) {
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onDialogClosed(boolean positiveResult) {
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onDialogCreated(Dialog dialog) {
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isAutoClosePreference() {
        return true;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onDialogStateRestored(Dialog dialog, Bundle savedInstanceState) {
    }

    /* loaded from: classes.dex */
    public static class CustomListPreferenceDialogFragment extends ListPreferenceDialogFragment {
        private int mClickedDialogEntryIndex;

        public static ListPreferenceDialogFragment newInstance(String key) {
            ListPreferenceDialogFragment fragment = new CustomListPreferenceDialogFragment();
            Bundle b = new Bundle(1);
            b.putString("key", key);
            fragment.setArguments(b);
            return fragment;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public CustomListPreference getCustomizablePreference() {
            return (CustomListPreference) getPreference();
        }

        @Override // android.support.v14.preference.ListPreferenceDialogFragment, android.support.v14.preference.PreferenceDialogFragment
        protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
            super.onPrepareDialogBuilder(builder);
            this.mClickedDialogEntryIndex = getCustomizablePreference().findIndexOfValue(getCustomizablePreference().getValue());
            getCustomizablePreference().onPrepareDialogBuilder(builder, getOnItemClickListener());
            if (getCustomizablePreference().isAutoClosePreference()) {
                return;
            }
            builder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() { // from class: com.android.settings.CustomListPreference.CustomListPreferenceDialogFragment.1
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialog, int which) {
                    CustomListPreferenceDialogFragment.this.onClick(dialog, -1);
                    dialog.dismiss();
                }
            });
        }

        @Override // android.support.v14.preference.PreferenceDialogFragment, android.app.DialogFragment
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Dialog dialog = super.onCreateDialog(savedInstanceState);
            if (savedInstanceState != null) {
                this.mClickedDialogEntryIndex = savedInstanceState.getInt("settings.CustomListPrefDialog.KEY_CLICKED_ENTRY_INDEX", this.mClickedDialogEntryIndex);
            }
            getCustomizablePreference().onDialogCreated(dialog);
            return dialog;
        }

        @Override // android.support.v14.preference.ListPreferenceDialogFragment, android.support.v14.preference.PreferenceDialogFragment, android.app.DialogFragment, android.app.Fragment
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putInt("settings.CustomListPrefDialog.KEY_CLICKED_ENTRY_INDEX", this.mClickedDialogEntryIndex);
        }

        @Override // android.app.DialogFragment, android.app.Fragment
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            getCustomizablePreference().onDialogStateRestored(getDialog(), savedInstanceState);
        }

        protected DialogInterface.OnClickListener getOnItemClickListener() {
            return new DialogInterface.OnClickListener() { // from class: com.android.settings.CustomListPreference.CustomListPreferenceDialogFragment.2
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialog, int which) {
                    CustomListPreferenceDialogFragment.this.setClickedDialogEntryIndex(which);
                    if (!CustomListPreferenceDialogFragment.this.getCustomizablePreference().isAutoClosePreference()) {
                        return;
                    }
                    CustomListPreferenceDialogFragment.this.onClick(dialog, -1);
                    dialog.dismiss();
                }
            };
        }

        /* JADX INFO: Access modifiers changed from: protected */
        public void setClickedDialogEntryIndex(int which) {
            this.mClickedDialogEntryIndex = which;
        }

        @Override // android.support.v14.preference.ListPreferenceDialogFragment, android.support.v14.preference.PreferenceDialogFragment
        public void onDialogClosed(boolean positiveResult) {
            getCustomizablePreference().onDialogClosed(positiveResult);
            ListPreference preference = getCustomizablePreference();
            if (!positiveResult || this.mClickedDialogEntryIndex < 0 || preference.getEntryValues() == null) {
                return;
            }
            String value = preference.getEntryValues()[this.mClickedDialogEntryIndex].toString();
            if (!preference.callChangeListener(value)) {
                return;
            }
            preference.setValue(value);
        }
    }
}
