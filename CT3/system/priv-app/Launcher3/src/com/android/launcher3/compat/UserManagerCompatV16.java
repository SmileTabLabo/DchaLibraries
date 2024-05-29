package com.android.launcher3.compat;

import java.util.ArrayList;
import java.util.List;
/* loaded from: a.zip:com/android/launcher3/compat/UserManagerCompatV16.class */
public class UserManagerCompatV16 extends UserManagerCompat {
    @Override // com.android.launcher3.compat.UserManagerCompat
    public void enableAndResetCache() {
    }

    @Override // com.android.launcher3.compat.UserManagerCompat
    public CharSequence getBadgedLabelForUser(CharSequence charSequence, UserHandleCompat userHandleCompat) {
        return charSequence;
    }

    @Override // com.android.launcher3.compat.UserManagerCompat
    public long getSerialNumberForUser(UserHandleCompat userHandleCompat) {
        return 0L;
    }

    @Override // com.android.launcher3.compat.UserManagerCompat
    public long getUserCreationTime(UserHandleCompat userHandleCompat) {
        return 0L;
    }

    @Override // com.android.launcher3.compat.UserManagerCompat
    public UserHandleCompat getUserForSerialNumber(long j) {
        return UserHandleCompat.myUserHandle();
    }

    @Override // com.android.launcher3.compat.UserManagerCompat
    public List<UserHandleCompat> getUserProfiles() {
        ArrayList arrayList = new ArrayList(1);
        arrayList.add(UserHandleCompat.myUserHandle());
        return arrayList;
    }

    @Override // com.android.launcher3.compat.UserManagerCompat
    public boolean isQuietModeEnabled(UserHandleCompat userHandleCompat) {
        return false;
    }
}
