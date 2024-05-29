package com.android.settingslib.drawer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import com.android.settingslib.R$string;
/* loaded from: a.zip:com/android/settingslib/drawer/ProfileSelectDialog.class */
public class ProfileSelectDialog extends DialogFragment implements DialogInterface.OnClickListener {
    private Tile mSelectedTile;

    public static void show(FragmentManager fragmentManager, Tile tile) {
        ProfileSelectDialog profileSelectDialog = new ProfileSelectDialog();
        Bundle bundle = new Bundle();
        bundle.putParcelable("selectedTile", tile);
        profileSelectDialog.setArguments(bundle);
        profileSelectDialog.show(fragmentManager, "select_profile");
    }

    @Override // android.content.DialogInterface.OnClickListener
    public void onClick(DialogInterface dialogInterface, int i) {
        UserHandle userHandle = this.mSelectedTile.userHandle.get(i);
        this.mSelectedTile.intent.putExtra("show_drawer_menu", true);
        this.mSelectedTile.intent.addFlags(32768);
        getActivity().startActivityAsUser(this.mSelectedTile.intent, userHandle);
        ((SettingsDrawerActivity) getActivity()).onProfileTileOpen();
    }

    @Override // android.app.DialogFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mSelectedTile = (Tile) getArguments().getParcelable("selectedTile");
    }

    @Override // android.app.DialogFragment
    public Dialog onCreateDialog(Bundle bundle) {
        Activity activity = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R$string.choose_profile).setAdapter(UserAdapter.createUserAdapter(UserManager.get(activity), activity, this.mSelectedTile.userHandle), this);
        return builder.create();
    }
}
