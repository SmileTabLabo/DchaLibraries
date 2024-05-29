package com.android.systemui.statusbar;

import android.content.Context;
import android.content.DialogInterface;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import com.android.systemui.statusbar.policy.UserSwitcherController;
/* loaded from: a.zip:com/android/systemui/statusbar/UserUtil.class */
public class UserUtil {

    /* loaded from: a.zip:com/android/systemui/statusbar/UserUtil$RemoveUserDialog.class */
    private static final class RemoveUserDialog extends SystemUIDialog implements DialogInterface.OnClickListener {
        private final int mUserId;
        private final UserSwitcherController mUserSwitcherController;

        public RemoveUserDialog(Context context, int i, UserSwitcherController userSwitcherController) {
            super(context);
            setTitle(2131493646);
            setMessage(context.getString(2131493647));
            setButton(-2, context.getString(17039360), this);
            setButton(-1, context.getString(2131493648), this);
            setCanceledOnTouchOutside(false);
            this.mUserId = i;
            this.mUserSwitcherController = userSwitcherController;
        }

        @Override // android.content.DialogInterface.OnClickListener
        public void onClick(DialogInterface dialogInterface, int i) {
            if (i == -2) {
                cancel();
                return;
            }
            dismiss();
            this.mUserSwitcherController.removeUserId(this.mUserId);
        }
    }

    public static void deleteUserWithPrompt(Context context, int i, UserSwitcherController userSwitcherController) {
        new RemoveUserDialog(context, i, userSwitcherController).show();
    }
}
