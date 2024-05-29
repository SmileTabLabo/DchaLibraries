package com.android.launcher3.compat;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Process;
import android.os.UserHandle;
import com.android.launcher3.Utilities;
/* loaded from: a.zip:com/android/launcher3/compat/UserHandleCompat.class */
public class UserHandleCompat {
    private UserHandle mUser;

    private UserHandleCompat() {
    }

    private UserHandleCompat(UserHandle userHandle) {
        this.mUser = userHandle;
    }

    public static UserHandleCompat fromIntent(Intent intent) {
        UserHandle userHandle;
        if (!Utilities.ATLEAST_LOLLIPOP || (userHandle = (UserHandle) intent.getParcelableExtra("android.intent.extra.USER")) == null) {
            return null;
        }
        return fromUser(userHandle);
    }

    public static UserHandleCompat fromUser(UserHandle userHandle) {
        if (userHandle == null) {
            return null;
        }
        return new UserHandleCompat(userHandle);
    }

    @TargetApi(17)
    public static UserHandleCompat myUserHandle() {
        return Utilities.ATLEAST_JB_MR1 ? new UserHandleCompat(Process.myUserHandle()) : new UserHandleCompat();
    }

    public void addToIntent(Intent intent, String str) {
        if (!Utilities.ATLEAST_LOLLIPOP || this.mUser == null) {
            return;
        }
        intent.putExtra(str, this.mUser);
    }

    public boolean equals(Object obj) {
        if (obj instanceof UserHandleCompat) {
            if (Utilities.ATLEAST_JB_MR1) {
                return this.mUser.equals(((UserHandleCompat) obj).mUser);
            }
            return true;
        }
        return false;
    }

    public UserHandle getUser() {
        return this.mUser;
    }

    public int hashCode() {
        if (Utilities.ATLEAST_JB_MR1) {
            return this.mUser.hashCode();
        }
        return 0;
    }

    public String toString() {
        return Utilities.ATLEAST_JB_MR1 ? this.mUser.toString() : "";
    }
}
