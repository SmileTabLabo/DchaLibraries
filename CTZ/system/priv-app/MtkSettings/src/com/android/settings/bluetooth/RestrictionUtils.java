package com.android.settings.bluetooth;

import android.content.Context;
import android.os.UserHandle;
import com.android.settingslib.RestrictedLockUtils;
/* loaded from: classes.dex */
public class RestrictionUtils {
    public RestrictedLockUtils.EnforcedAdmin checkIfRestrictionEnforced(Context context, String str) {
        return RestrictedLockUtils.checkIfRestrictionEnforced(context, str, UserHandle.myUserId());
    }
}
