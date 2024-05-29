package com.android.launcher3.compat;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.UserHandle;
import com.android.launcher3.Utilities;
import com.android.launcher3.util.LongArrayMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
@TargetApi(21)
/* loaded from: a.zip:com/android/launcher3/compat/UserManagerCompatVL.class */
public class UserManagerCompatVL extends UserManagerCompatV17 {
    private static final String USER_CREATION_TIME_KEY = "user_creation_time_";
    private final Context mContext;
    private final PackageManager mPm;

    /* JADX INFO: Access modifiers changed from: package-private */
    public UserManagerCompatVL(Context context) {
        super(context);
        this.mPm = context.getPackageManager();
        this.mContext = context;
    }

    @Override // com.android.launcher3.compat.UserManagerCompatV17, com.android.launcher3.compat.UserManagerCompatV16, com.android.launcher3.compat.UserManagerCompat
    public void enableAndResetCache() {
        synchronized (this) {
            this.mUsers = new LongArrayMap<>();
            this.mUserToSerialMap = new HashMap<>();
            List<UserHandle> userProfiles = this.mUserManager.getUserProfiles();
            if (userProfiles != null) {
                for (UserHandle userHandle : userProfiles) {
                    long serialNumberForUser = this.mUserManager.getSerialNumberForUser(userHandle);
                    UserHandleCompat fromUser = UserHandleCompat.fromUser(userHandle);
                    this.mUsers.put(serialNumberForUser, fromUser);
                    this.mUserToSerialMap.put(fromUser, Long.valueOf(serialNumberForUser));
                }
            }
        }
    }

    @Override // com.android.launcher3.compat.UserManagerCompatV16, com.android.launcher3.compat.UserManagerCompat
    public CharSequence getBadgedLabelForUser(CharSequence charSequence, UserHandleCompat userHandleCompat) {
        return userHandleCompat == null ? charSequence : this.mPm.getUserBadgedLabel(charSequence, userHandleCompat.getUser());
    }

    @Override // com.android.launcher3.compat.UserManagerCompatV16, com.android.launcher3.compat.UserManagerCompat
    public long getUserCreationTime(UserHandleCompat userHandleCompat) {
        if (Utilities.ATLEAST_MARSHMALLOW) {
            return this.mUserManager.getUserCreationTime(userHandleCompat.getUser());
        }
        SharedPreferences prefs = Utilities.getPrefs(this.mContext);
        String str = USER_CREATION_TIME_KEY + getSerialNumberForUser(userHandleCompat);
        if (!prefs.contains(str)) {
            prefs.edit().putLong(str, System.currentTimeMillis()).apply();
        }
        return prefs.getLong(str, 0L);
    }

    @Override // com.android.launcher3.compat.UserManagerCompatV16, com.android.launcher3.compat.UserManagerCompat
    public List<UserHandleCompat> getUserProfiles() {
        synchronized (this) {
            if (this.mUsers != null) {
                ArrayList arrayList = new ArrayList();
                arrayList.addAll(this.mUserToSerialMap.keySet());
                return arrayList;
            }
            List<UserHandle> userProfiles = this.mUserManager.getUserProfiles();
            if (userProfiles == null) {
                return Collections.emptyList();
            }
            ArrayList arrayList2 = new ArrayList(userProfiles.size());
            for (UserHandle userHandle : userProfiles) {
                arrayList2.add(UserHandleCompat.fromUser(userHandle));
            }
            return arrayList2;
        }
    }
}
