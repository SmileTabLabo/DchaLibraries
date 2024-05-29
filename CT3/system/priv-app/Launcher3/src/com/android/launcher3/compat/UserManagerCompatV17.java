package com.android.launcher3.compat;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.UserManager;
import com.android.launcher3.util.LongArrayMap;
import java.util.HashMap;
@TargetApi(17)
/* loaded from: a.zip:com/android/launcher3/compat/UserManagerCompatV17.class */
public class UserManagerCompatV17 extends UserManagerCompatV16 {
    protected UserManager mUserManager;
    protected HashMap<UserHandleCompat, Long> mUserToSerialMap;
    protected LongArrayMap<UserHandleCompat> mUsers;

    /* JADX INFO: Access modifiers changed from: package-private */
    public UserManagerCompatV17(Context context) {
        this.mUserManager = (UserManager) context.getSystemService("user");
    }

    @Override // com.android.launcher3.compat.UserManagerCompatV16, com.android.launcher3.compat.UserManagerCompat
    public void enableAndResetCache() {
        synchronized (this) {
            this.mUsers = new LongArrayMap<>();
            this.mUserToSerialMap = new HashMap<>();
            UserHandleCompat myUserHandle = UserHandleCompat.myUserHandle();
            long serialNumberForUser = this.mUserManager.getSerialNumberForUser(myUserHandle.getUser());
            this.mUsers.put(serialNumberForUser, myUserHandle);
            this.mUserToSerialMap.put(myUserHandle, Long.valueOf(serialNumberForUser));
        }
    }

    @Override // com.android.launcher3.compat.UserManagerCompatV16, com.android.launcher3.compat.UserManagerCompat
    public long getSerialNumberForUser(UserHandleCompat userHandleCompat) {
        synchronized (this) {
            if (this.mUserToSerialMap != null) {
                Long l = this.mUserToSerialMap.get(userHandleCompat);
                return l == null ? 0L : l.longValue();
            }
            return this.mUserManager.getSerialNumberForUser(userHandleCompat.getUser());
        }
    }

    @Override // com.android.launcher3.compat.UserManagerCompatV16, com.android.launcher3.compat.UserManagerCompat
    public UserHandleCompat getUserForSerialNumber(long j) {
        synchronized (this) {
            if (this.mUsers != null) {
                return this.mUsers.get(j);
            }
            return UserHandleCompat.fromUser(this.mUserManager.getUserForSerialNumber(j));
        }
    }
}
