package com.android.settings.notification;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.UserManager;
import android.provider.Settings;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.android.settings.R;
import com.android.settings.RestrictedListPreference;
import com.android.settings.Utils;
import com.android.settingslib.RestrictedLockUtils;
/* loaded from: classes.dex */
public class NotificationLockscreenPreference extends RestrictedListPreference {
    private RestrictedLockUtils.EnforcedAdmin mAdminRestrictingRemoteInput;
    private boolean mAllowRemoteInput;
    private Listener mListener;
    private boolean mRemoteInputCheckBoxEnabled;
    private boolean mShowRemoteInput;
    private int mUserId;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.preference.DialogPreference, android.support.v7.preference.Preference
    public void onClick() {
        Context context = getContext();
        if (Utils.startQuietModeDialogIfNecessary(context, UserManager.get(context), this.mUserId)) {
            return;
        }
        super.onClick();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.RestrictedListPreference, com.android.settings.CustomListPreference
    public void onPrepareDialogBuilder(AlertDialog.Builder builder, DialogInterface.OnClickListener innerListener) {
        this.mListener = new Listener(innerListener);
        builder.setSingleChoiceItems(createListAdapter(), getSelectedValuePos(), this.mListener);
        this.mShowRemoteInput = getEntryValues().length == 3;
        this.mAllowRemoteInput = Settings.Secure.getInt(getContext().getContentResolver(), "lock_screen_allow_remote_input", 0) != 0;
        builder.setView(R.layout.lockscreen_remote_input);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.CustomListPreference
    public void onDialogCreated(Dialog dialog) {
        super.onDialogCreated(dialog);
        dialog.create();
        CheckBox checkbox = (CheckBox) dialog.findViewById(R.id.lockscreen_remote_input);
        checkbox.setChecked(!this.mAllowRemoteInput);
        checkbox.setOnCheckedChangeListener(this.mListener);
        checkbox.setEnabled(this.mAdminRestrictingRemoteInput == null);
        View restricted = dialog.findViewById(R.id.restricted_lock_icon_remote_input);
        restricted.setVisibility(this.mAdminRestrictingRemoteInput == null ? 8 : 0);
        if (this.mAdminRestrictingRemoteInput == null) {
            return;
        }
        checkbox.setClickable(false);
        dialog.findViewById(16909094).setOnClickListener(this.mListener);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.CustomListPreference
    public void onDialogStateRestored(Dialog dialog, Bundle savedInstanceState) {
        super.onDialogStateRestored(dialog, savedInstanceState);
        ListView listView = ((AlertDialog) dialog).getListView();
        int selectedPosition = listView.getCheckedItemPosition();
        View panel = dialog.findViewById(16909094);
        panel.setVisibility(checkboxVisibilityForSelectedIndex(selectedPosition, this.mShowRemoteInput));
        this.mListener.setView(panel);
    }

    @Override // com.android.settings.RestrictedListPreference
    protected ListAdapter createListAdapter() {
        return new RestrictedListPreference.RestrictedArrayAdapter(getContext(), getEntries(), -1);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.CustomListPreference
    public void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        Settings.Secure.putInt(getContext().getContentResolver(), "lock_screen_allow_remote_input", this.mAllowRemoteInput ? 1 : 0);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.CustomListPreference
    public boolean isAutoClosePreference() {
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int checkboxVisibilityForSelectedIndex(int selected, boolean showRemoteAtAll) {
        return (selected == 1 && showRemoteAtAll && this.mRemoteInputCheckBoxEnabled) ? 0 : 8;
    }

    /* loaded from: classes.dex */
    private class Listener implements DialogInterface.OnClickListener, CompoundButton.OnCheckedChangeListener, View.OnClickListener {
        private final DialogInterface.OnClickListener mInner;
        private View mView;

        public Listener(DialogInterface.OnClickListener inner) {
            this.mInner = inner;
        }

        @Override // android.content.DialogInterface.OnClickListener
        public void onClick(DialogInterface dialog, int which) {
            this.mInner.onClick(dialog, which);
            ListView listView = ((AlertDialog) dialog).getListView();
            int selectedPosition = listView.getCheckedItemPosition();
            if (this.mView == null) {
                return;
            }
            this.mView.setVisibility(NotificationLockscreenPreference.this.checkboxVisibilityForSelectedIndex(selectedPosition, NotificationLockscreenPreference.this.mShowRemoteInput));
        }

        @Override // android.widget.CompoundButton.OnCheckedChangeListener
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            NotificationLockscreenPreference.this.mAllowRemoteInput = !isChecked;
        }

        public void setView(View view) {
            this.mView = view;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View v) {
            if (v.getId() != 16909094) {
                return;
            }
            RestrictedLockUtils.sendShowAdminSupportDetailsIntent(NotificationLockscreenPreference.this.getContext(), NotificationLockscreenPreference.this.mAdminRestrictingRemoteInput);
        }
    }
}
