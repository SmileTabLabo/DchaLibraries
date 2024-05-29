package com.android.launcher3.compat;

import android.content.Context;
import com.android.launcher3.Utilities;
import java.util.List;
/* loaded from: a.zip:com/android/launcher3/compat/UserManagerCompat.class */
public abstract class UserManagerCompat {
    private static UserManagerCompat sInstance;
    private static final Object sInstanceLock = new Object();

    public static UserManagerCompat getInstance(Context context) {
        UserManagerCompat userManagerCompat;
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                if (Utilities.ATLEAST_N) {
                    sInstance = new UserManagerCompatVN(context.getApplicationContext());
                } else if (Utilities.ATLEAST_LOLLIPOP) {
                    sInstance = new UserManagerCompatVL(context.getApplicationContext());
                } else if (Utilities.ATLEAST_JB_MR1) {
                    sInstance = new UserManagerCompatV17(context.getApplicationContext());
                } else {
                    sInstance = new UserManagerCompatV16();
                }
            }
            userManagerCompat = sInstance;
        }
        return userManagerCompat;
    }

    public abstract void enableAndResetCache();

    public abstract CharSequence getBadgedLabelForUser(CharSequence charSequence, UserHandleCompat userHandleCompat);

    public abstract long getSerialNumberForUser(UserHandleCompat userHandleCompat);

    public abstract long getUserCreationTime(UserHandleCompat userHandleCompat);

    public abstract UserHandleCompat getUserForSerialNumber(long j);

    public abstract List<UserHandleCompat> getUserProfiles();

    public abstract boolean isQuietModeEnabled(UserHandleCompat userHandleCompat);
}
