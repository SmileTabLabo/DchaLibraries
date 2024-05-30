package com.android.launcher3;

import android.content.ComponentName;
/* loaded from: classes.dex */
public class PendingAddItemInfo extends ItemInfo {
    public ComponentName componentName;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.launcher3.ItemInfo
    public String dumpProperties() {
        return super.dumpProperties() + " componentName=" + this.componentName;
    }
}
