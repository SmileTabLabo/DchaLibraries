package com.android.launcher3.widget;

import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import com.android.launcher3.PendingAddItemInfo;
/* loaded from: a.zip:com/android/launcher3/widget/PendingAddShortcutInfo.class */
public class PendingAddShortcutInfo extends PendingAddItemInfo {
    ActivityInfo activityInfo;

    public PendingAddShortcutInfo(ActivityInfo activityInfo) {
        this.activityInfo = activityInfo;
        this.componentName = new ComponentName(activityInfo.packageName, activityInfo.name);
        this.itemType = 1;
    }

    @Override // com.android.launcher3.ItemInfo
    public String toString() {
        return String.format("PendingAddShortcutInfo package=%s, name=%s", this.activityInfo.packageName, this.activityInfo.name);
    }
}
