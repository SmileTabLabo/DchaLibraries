package com.android.launcher3.util;

import android.content.ComponentName;
import android.content.Context;
import com.android.launcher3.compat.UserHandleCompat;
import com.android.launcher3.compat.UserManagerCompat;
import java.util.Arrays;
/* loaded from: a.zip:com/android/launcher3/util/ComponentKey.class */
public class ComponentKey {

    /* renamed from: -assertionsDisabled  reason: not valid java name */
    static final boolean f3assertionsDisabled;
    public final ComponentName componentName;
    private final int mHashCode;
    public final UserHandleCompat user;

    static {
        f3assertionsDisabled = !ComponentKey.class.desiredAssertionStatus();
    }

    public ComponentKey(ComponentName componentName, UserHandleCompat userHandleCompat) {
        if (!f3assertionsDisabled) {
            if (!(componentName != null)) {
                throw new AssertionError();
            }
        }
        if (!f3assertionsDisabled) {
            if (!(userHandleCompat != null)) {
                throw new AssertionError();
            }
        }
        this.componentName = componentName;
        this.user = userHandleCompat;
        this.mHashCode = Arrays.hashCode(new Object[]{componentName, userHandleCompat});
    }

    public boolean equals(Object obj) {
        ComponentKey componentKey = (ComponentKey) obj;
        return componentKey.componentName.equals(this.componentName) ? componentKey.user.equals(this.user) : false;
    }

    public String flattenToString(Context context) {
        String flattenToString = this.componentName.flattenToString();
        String str = flattenToString;
        if (this.user != null) {
            str = flattenToString + "#" + UserManagerCompat.getInstance(context).getSerialNumberForUser(this.user);
        }
        return str;
    }

    public int hashCode() {
        return this.mHashCode;
    }
}
