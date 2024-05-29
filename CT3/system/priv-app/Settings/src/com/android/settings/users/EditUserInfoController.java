package com.android.settings.users;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import com.android.settings.R;
import com.android.settingslib.Utils;
import com.android.settingslib.drawable.CircleFramedDrawable;
import java.io.File;
/* loaded from: classes.dex */
public class EditUserInfoController {
    private Dialog mEditUserInfoDialog;
    private EditUserPhotoController mEditUserPhotoController;
    private Bitmap mSavedPhoto;
    private UserHandle mUser;
    private UserManager mUserManager;
    private boolean mWaitingForActivityResult = false;

    /* loaded from: classes.dex */
    public interface OnContentChangedCallback {
        void onLabelChanged(CharSequence charSequence);

        void onPhotoChanged(Drawable drawable);
    }

    public void clear() {
        this.mEditUserPhotoController.removeNewUserPhotoBitmapFile();
        this.mEditUserInfoDialog = null;
        this.mSavedPhoto = null;
    }

    public void onRestoreInstanceState(Bundle icicle) {
        String pendingPhoto = icicle.getString("pending_photo");
        if (pendingPhoto != null) {
            this.mSavedPhoto = EditUserPhotoController.loadNewUserPhotoBitmap(new File(pendingPhoto));
        }
        this.mWaitingForActivityResult = icicle.getBoolean("awaiting_result", false);
    }

    public void onSaveInstanceState(Bundle outState) {
        File file;
        if (this.mEditUserInfoDialog != null && this.mEditUserInfoDialog.isShowing() && this.mEditUserPhotoController != null && (file = this.mEditUserPhotoController.saveNewUserPhotoBitmap()) != null) {
            outState.putString("pending_photo", file.getPath());
        }
        if (!this.mWaitingForActivityResult) {
            return;
        }
        outState.putBoolean("awaiting_result", this.mWaitingForActivityResult);
    }

    public void startingActivityForResult() {
        this.mWaitingForActivityResult = true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        this.mWaitingForActivityResult = false;
        if (this.mEditUserInfoDialog != null && this.mEditUserInfoDialog.isShowing() && !this.mEditUserPhotoController.onActivityResult(requestCode, resultCode, data)) {
        }
    }

    public Dialog createDialog(final Fragment fragment, final Drawable currentUserIcon, final CharSequence currentUserName, int titleResId, final OnContentChangedCallback callback, UserHandle user) {
        Drawable drawable;
        Activity activity = fragment.getActivity();
        this.mUser = user;
        if (this.mUserManager == null) {
            this.mUserManager = UserManager.get(activity);
        }
        LayoutInflater inflater = activity.getLayoutInflater();
        View content = inflater.inflate(R.layout.edit_user_info_dialog_content, (ViewGroup) null);
        UserInfo info = this.mUserManager.getUserInfo(this.mUser.getIdentifier());
        final EditText userNameView = (EditText) content.findViewById(R.id.user_name);
        userNameView.setText(info.name);
        ImageView userPhotoView = (ImageView) content.findViewById(R.id.user_photo);
        if (this.mSavedPhoto != null) {
            drawable = CircleFramedDrawable.getInstance(activity, this.mSavedPhoto);
        } else {
            drawable = currentUserIcon;
            if (currentUserIcon == null) {
                drawable = Utils.getUserIcon(activity, this.mUserManager, info);
            }
        }
        userPhotoView.setImageDrawable(drawable);
        this.mEditUserPhotoController = new EditUserPhotoController(fragment, userPhotoView, this.mSavedPhoto, drawable, this.mWaitingForActivityResult);
        this.mEditUserInfoDialog = new AlertDialog.Builder(activity).setTitle(R.string.profile_info_settings_title).setView(content).setCancelable(true).setPositiveButton(17039370, new DialogInterface.OnClickListener() { // from class: com.android.settings.users.EditUserInfoController.1
            /* JADX WARN: Type inference failed for: r4v11, types: [com.android.settings.users.EditUserInfoController$1$1] */
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                if (which == -1) {
                    CharSequence userName = userNameView.getText();
                    if (!TextUtils.isEmpty(userName) && (currentUserName == null || !userName.toString().equals(currentUserName.toString()))) {
                        if (callback != null) {
                            callback.onLabelChanged(userName.toString());
                        }
                        EditUserInfoController.this.mUserManager.setUserName(EditUserInfoController.this.mUser.getIdentifier(), userName.toString());
                    }
                    Drawable drawable2 = EditUserInfoController.this.mEditUserPhotoController.getNewUserPhotoDrawable();
                    Bitmap bitmap = EditUserInfoController.this.mEditUserPhotoController.getNewUserPhotoBitmap();
                    if (drawable2 != null && bitmap != null && !drawable2.equals(currentUserIcon)) {
                        if (callback != null) {
                            callback.onPhotoChanged(drawable2);
                        }
                        new AsyncTask<Void, Void, Void>() { // from class: com.android.settings.users.EditUserInfoController.1.1
                            /* JADX INFO: Access modifiers changed from: protected */
                            @Override // android.os.AsyncTask
                            public Void doInBackground(Void... params) {
                                EditUserInfoController.this.mUserManager.setUserIcon(EditUserInfoController.this.mUser.getIdentifier(), EditUserInfoController.this.mEditUserPhotoController.getNewUserPhotoBitmap());
                                return null;
                            }
                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
                    }
                    fragment.getActivity().removeDialog(1);
                }
                EditUserInfoController.this.clear();
            }
        }).setNegativeButton(17039360, new DialogInterface.OnClickListener() { // from class: com.android.settings.users.EditUserInfoController.2
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                EditUserInfoController.this.clear();
            }
        }).create();
        this.mEditUserInfoDialog.getWindow().setSoftInputMode(4);
        return this.mEditUserInfoDialog;
    }
}
