package com.android.systemui.recents.misc;

import android.content.Context;
import com.android.systemui.shared.system.TaskStackChangeListener;
/* loaded from: classes.dex */
public abstract class SysUiTaskStackChangeListener extends TaskStackChangeListener {
    /* JADX INFO: Access modifiers changed from: protected */
    public final boolean checkCurrentUserId(Context context, boolean z) {
        return checkCurrentUserId(SystemServicesProxy.getInstance(context).getCurrentUser(), z);
    }
}
