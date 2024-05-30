package com.android.launcher3.widget;

import android.support.v7.widget.RecyclerView;
import com.android.launcher3.IconCache;
import com.android.launcher3.model.PackageItemInfo;
import com.android.launcher3.widget.WidgetsListAdapter;
import java.util.ArrayList;
import java.util.Iterator;
/* loaded from: classes.dex */
public class WidgetsDiffReporter {
    private static final boolean DEBUG = false;
    private static final String TAG = "WidgetsDiffReporter";
    private final IconCache mIconCache;
    private final RecyclerView.Adapter mListener;

    public WidgetsDiffReporter(IconCache iconCache, RecyclerView.Adapter adapter) {
        this.mIconCache = iconCache;
        this.mListener = adapter;
    }

    public void process(ArrayList<WidgetListRowEntry> arrayList, ArrayList<WidgetListRowEntry> arrayList2, WidgetsListAdapter.WidgetListRowEntryComparator widgetListRowEntryComparator) {
        if (arrayList.isEmpty() || arrayList2.isEmpty()) {
            if (arrayList.size() != arrayList2.size()) {
                arrayList.clear();
                arrayList.addAll(arrayList2);
                this.mListener.notifyDataSetChanged();
                return;
            }
            return;
        }
        Iterator it = ((ArrayList) arrayList.clone()).iterator();
        Iterator<WidgetListRowEntry> it2 = arrayList2.iterator();
        WidgetListRowEntry widgetListRowEntry = (WidgetListRowEntry) it.next();
        WidgetListRowEntry next = it2.next();
        while (true) {
            int comparePackageName = comparePackageName(widgetListRowEntry, next, widgetListRowEntryComparator);
            if (comparePackageName < 0) {
                int indexOf = arrayList.indexOf(widgetListRowEntry);
                this.mListener.notifyItemRemoved(indexOf);
                arrayList.remove(indexOf);
                widgetListRowEntry = it.hasNext() ? (WidgetListRowEntry) it.next() : null;
            } else {
                if (comparePackageName > 0) {
                    int indexOf2 = widgetListRowEntry != null ? arrayList.indexOf(widgetListRowEntry) : arrayList.size();
                    arrayList.add(indexOf2, next);
                    r4 = it2.hasNext() ? it2.next() : null;
                    this.mListener.notifyItemInserted(indexOf2);
                } else {
                    if (!isSamePackageItemInfo(widgetListRowEntry.pkgItem, next.pkgItem) || !widgetListRowEntry.widgets.equals(next.widgets)) {
                        int indexOf3 = arrayList.indexOf(widgetListRowEntry);
                        arrayList.set(indexOf3, next);
                        this.mListener.notifyItemChanged(indexOf3);
                    }
                    widgetListRowEntry = it.hasNext() ? (WidgetListRowEntry) it.next() : null;
                    if (it2.hasNext()) {
                        r4 = it2.next();
                    }
                }
                next = r4;
            }
            if (widgetListRowEntry == null && next == null) {
                return;
            }
        }
    }

    private int comparePackageName(WidgetListRowEntry widgetListRowEntry, WidgetListRowEntry widgetListRowEntry2, WidgetsListAdapter.WidgetListRowEntryComparator widgetListRowEntryComparator) {
        if (widgetListRowEntry == null && widgetListRowEntry2 == null) {
            throw new IllegalStateException("Cannot compare PackageItemInfo if both rows are null.");
        }
        if (widgetListRowEntry == null && widgetListRowEntry2 != null) {
            return 1;
        }
        if (widgetListRowEntry != null && widgetListRowEntry2 == null) {
            return -1;
        }
        return widgetListRowEntryComparator.compare(widgetListRowEntry, widgetListRowEntry2);
    }

    private boolean isSamePackageItemInfo(PackageItemInfo packageItemInfo, PackageItemInfo packageItemInfo2) {
        return packageItemInfo.iconBitmap.equals(packageItemInfo2.iconBitmap) && !this.mIconCache.isDefaultIcon(packageItemInfo.iconBitmap, packageItemInfo.user);
    }
}
