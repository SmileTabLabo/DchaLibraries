package com.android.settings.users;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.users.EditUserInfoController;
/* loaded from: classes.dex */
public class RestrictedProfileSettings extends AppRestrictionsFragment implements EditUserInfoController.OnContentChangedCallback {
    private ImageView mDeleteButton;
    private EditUserInfoController mEditUserInfoController = new EditUserInfoController();
    private View mHeaderView;
    private ImageView mUserIconView;
    private TextView mUserNameView;

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (icicle != null) {
            this.mEditUserInfoController.onRestoreInstanceState(icicle);
        }
        init(icicle);
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onActivityCreated(Bundle savedInstanceState) {
        this.mHeaderView = setPinnedHeaderView(R.layout.user_info_header);
        this.mHeaderView.setOnClickListener(this);
        this.mUserIconView = (ImageView) this.mHeaderView.findViewById(16908294);
        this.mUserNameView = (TextView) this.mHeaderView.findViewById(16908310);
        this.mDeleteButton = (ImageView) this.mHeaderView.findViewById(R.id.delete);
        this.mDeleteButton.setOnClickListener(this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override // com.android.settings.users.AppRestrictionsFragment, com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        this.mEditUserInfoController.onSaveInstanceState(outState);
    }

    @Override // com.android.settings.users.AppRestrictionsFragment, com.android.settings.SettingsPreferenceFragment, com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        UserInfo info = Utils.getExistingUser(this.mUserManager, this.mUser);
        if (info == null) {
            finishFragment();
            return;
        }
        ((TextView) this.mHeaderView.findViewById(16908310)).setText(info.name);
        ((ImageView) this.mHeaderView.findViewById(16908294)).setImageDrawable(com.android.settingslib.Utils.getUserIcon(getActivity(), this.mUserManager, info));
    }

    @Override // android.app.Fragment
    public void startActivityForResult(Intent intent, int requestCode) {
        this.mEditUserInfoController.startingActivityForResult();
        super.startActivityForResult(intent, requestCode);
    }

    @Override // com.android.settings.users.AppRestrictionsFragment, android.app.Fragment
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.mEditUserInfoController.onActivityResult(requestCode, resultCode, data);
    }

    @Override // com.android.settings.users.AppRestrictionsFragment, android.view.View.OnClickListener
    public void onClick(View view) {
        if (view == this.mHeaderView) {
            showDialog(1);
        } else if (view == this.mDeleteButton) {
            showDialog(2);
        } else {
            super.onClick(view);
        }
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settings.DialogCreatable
    public Dialog onCreateDialog(int dialogId) {
        if (dialogId == 1) {
            return this.mEditUserInfoController.createDialog(this, this.mUserIconView.getDrawable(), this.mUserNameView.getText(), R.string.profile_info_settings_title, this, this.mUser);
        }
        if (dialogId == 2) {
            Dialog dlg = UserDialogs.createRemoveDialog(getActivity(), this.mUser.getIdentifier(), new DialogInterface.OnClickListener() { // from class: com.android.settings.users.RestrictedProfileSettings.1
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialog, int which) {
                    RestrictedProfileSettings.this.removeUser();
                }
            });
            return dlg;
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void removeUser() {
        getView().post(new Runnable() { // from class: com.android.settings.users.RestrictedProfileSettings.2
            @Override // java.lang.Runnable
            public void run() {
                RestrictedProfileSettings.this.mUserManager.removeUser(RestrictedProfileSettings.this.mUser.getIdentifier());
                RestrictedProfileSettings.this.finishFragment();
            }
        });
    }

    @Override // com.android.settings.users.EditUserInfoController.OnContentChangedCallback
    public void onPhotoChanged(Drawable photo) {
        this.mUserIconView.setImageDrawable(photo);
    }

    @Override // com.android.settings.users.EditUserInfoController.OnContentChangedCallback
    public void onLabelChanged(CharSequence label) {
        this.mUserNameView.setText(label);
    }
}
