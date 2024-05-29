package com.android.launcher3.model;

import android.content.Context;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.compat.UserHandleCompat;
import com.android.launcher3.compat.UserManagerCompat;
import java.util.Comparator;
/* loaded from: a.zip:com/android/launcher3/model/AbstractUserComparator.class */
public abstract class AbstractUserComparator<T extends ItemInfo> implements Comparator<T> {
    private final UserHandleCompat mMyUser = UserHandleCompat.myUserHandle();
    private final UserManagerCompat mUserManager;

    public AbstractUserComparator(Context context) {
        this.mUserManager = UserManagerCompat.getInstance(context);
    }

    @Override // java.util.Comparator
    public int compare(T t, T t2) {
        if (this.mMyUser.equals(t.user)) {
            return -1;
        }
        return Long.valueOf(this.mUserManager.getSerialNumberForUser(t.user)).compareTo(Long.valueOf(this.mUserManager.getSerialNumberForUser(t2.user)));
    }
}
