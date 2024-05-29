package com.android.launcher3.compat;

import android.annotation.TargetApi;
import android.content.Context;
@TargetApi(24)
/* loaded from: a.zip:com/android/launcher3/compat/UserManagerCompatVN.class */
public class UserManagerCompatVN extends UserManagerCompatVL {
    private static final String TAG = "UserManagerCompatVN";

    /* JADX INFO: Access modifiers changed from: package-private */
    public UserManagerCompatVN(Context context) {
        super(context);
    }

    @Override // com.android.launcher3.compat.UserManagerCompatV16, com.android.launcher3.compat.UserManagerCompat
    public boolean isQuietModeEnabled(UserHandleCompat userHandleCompat) {
        if (userHandleCompat != null) {
            try {
                return this.mUserManager.isQuietModeEnabled(userHandleCompat.getUser());
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        return false;
    }
}
