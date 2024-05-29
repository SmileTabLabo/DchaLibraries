package com.android.launcher3.widget;

import android.os.Process;
import android.os.UserHandle;
import com.android.launcher3.model.WidgetItem;
import java.text.Collator;
import java.util.Comparator;
/* loaded from: classes.dex */
public class WidgetItemComparator implements Comparator<WidgetItem> {
    private final UserHandle mMyUserHandle = Process.myUserHandle();
    private final Collator mCollator = Collator.getInstance();

    @Override // java.util.Comparator
    public int compare(WidgetItem widgetItem, WidgetItem widgetItem2) {
        boolean z = !this.mMyUserHandle.equals(widgetItem.user);
        if ((!this.mMyUserHandle.equals(widgetItem2.user)) ^ z) {
            return z ? 1 : -1;
        }
        int compare = this.mCollator.compare(widgetItem.label, widgetItem2.label);
        if (compare != 0) {
            return compare;
        }
        int i = widgetItem.spanX * widgetItem.spanY;
        int i2 = widgetItem2.spanX * widgetItem2.spanY;
        if (i == i2) {
            return Integer.compare(widgetItem.spanY, widgetItem2.spanY);
        }
        return Integer.compare(i, i2);
    }
}
