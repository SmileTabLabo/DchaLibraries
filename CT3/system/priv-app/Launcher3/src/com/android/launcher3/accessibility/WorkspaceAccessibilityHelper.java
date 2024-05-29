package com.android.launcher3.accessibility;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import com.android.launcher3.AppInfo;
import com.android.launcher3.CellLayout;
import com.android.launcher3.FolderInfo;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.ShortcutInfo;
import com.android.launcher3.accessibility.LauncherAccessibilityDelegate;
/* loaded from: a.zip:com/android/launcher3/accessibility/WorkspaceAccessibilityHelper.class */
public class WorkspaceAccessibilityHelper extends DragAndDropAccessibilityDelegate {
    public WorkspaceAccessibilityHelper(CellLayout cellLayout) {
        super(cellLayout);
    }

    public static String getDescriptionForDropOver(View view, Context context) {
        ItemInfo itemInfo = (ItemInfo) view.getTag();
        if (itemInfo instanceof ShortcutInfo) {
            return context.getString(2131558477, itemInfo.title);
        }
        if (itemInfo instanceof FolderInfo) {
            if (TextUtils.isEmpty(itemInfo.title)) {
                ShortcutInfo shortcutInfo = null;
                for (ShortcutInfo shortcutInfo2 : ((FolderInfo) itemInfo).contents) {
                    if (shortcutInfo == null || shortcutInfo.rank > shortcutInfo2.rank) {
                        shortcutInfo = shortcutInfo2;
                    }
                }
                if (shortcutInfo != null) {
                    return context.getString(2131558475, shortcutInfo.title);
                }
            }
            return context.getString(2131558474, itemInfo.title);
        }
        return "";
    }

    @Override // com.android.launcher3.accessibility.DragAndDropAccessibilityDelegate
    protected String getConfirmationForIconDrop(int i) {
        int countX = this.mView.getCountX();
        int countX2 = i / this.mView.getCountX();
        LauncherAccessibilityDelegate.DragInfo dragInfo = this.mDelegate.getDragInfo();
        View childAt = this.mView.getChildAt(i % countX, countX2);
        if (childAt == null || childAt == dragInfo.item) {
            return this.mContext.getString(2131558473);
        }
        ItemInfo itemInfo = (ItemInfo) childAt.getTag();
        return ((itemInfo instanceof AppInfo) || (itemInfo instanceof ShortcutInfo)) ? this.mContext.getString(2131558478) : itemInfo instanceof FolderInfo ? this.mContext.getString(2131558476) : "";
    }

    @Override // com.android.launcher3.accessibility.DragAndDropAccessibilityDelegate
    protected String getLocationDescriptionForIconDrop(int i) {
        int countX = i % this.mView.getCountX();
        int countX2 = i / this.mView.getCountX();
        LauncherAccessibilityDelegate.DragInfo dragInfo = this.mDelegate.getDragInfo();
        View childAt = this.mView.getChildAt(countX, countX2);
        return (childAt == null || childAt == dragInfo.item) ? this.mView.isHotseat() ? this.mContext.getString(2131558472, Integer.valueOf(i + 1)) : this.mContext.getString(2131558470, Integer.valueOf(countX2 + 1), Integer.valueOf(countX + 1)) : getDescriptionForDropOver(childAt, this.mContext);
    }

    @Override // com.android.launcher3.accessibility.DragAndDropAccessibilityDelegate
    protected int intersectsValidDropTarget(int i) {
        boolean z;
        int countX = this.mView.getCountX();
        int countY = this.mView.getCountY();
        int i2 = i % countX;
        int i3 = i / countX;
        LauncherAccessibilityDelegate.DragInfo dragInfo = this.mDelegate.getDragInfo();
        if (dragInfo.dragType == LauncherAccessibilityDelegate.DragType.WIDGET && this.mView.isHotseat()) {
            return -1;
        }
        if (dragInfo.dragType != LauncherAccessibilityDelegate.DragType.WIDGET) {
            View childAt = this.mView.getChildAt(i2, i3);
            if (childAt == null || childAt == dragInfo.item) {
                return i;
            }
            if (dragInfo.dragType != LauncherAccessibilityDelegate.DragType.FOLDER) {
                ItemInfo itemInfo = (ItemInfo) childAt.getTag();
                if ((itemInfo instanceof AppInfo) || (itemInfo instanceof FolderInfo) || (itemInfo instanceof ShortcutInfo)) {
                    return i;
                }
                return -1;
            }
            return -1;
        }
        int i4 = dragInfo.info.spanX;
        int i5 = dragInfo.info.spanY;
        for (int i6 = 0; i6 < i4; i6++) {
            for (int i7 = 0; i7 < i5; i7++) {
                boolean z2 = true;
                int i8 = i2 - i6;
                int i9 = i3 - i7;
                if (i8 >= 0 && i9 >= 0) {
                    int i10 = i8;
                    while (i10 < i8 + i4 && z2) {
                        int i11 = i9;
                        while (true) {
                            z = z2;
                            if (i11 >= i9 + i5) {
                                break;
                            } else if (i10 >= countX || i11 >= countY || this.mView.isOccupied(i10, i11)) {
                                break;
                            } else {
                                i11++;
                            }
                        }
                        i10++;
                        z2 = z;
                    }
                    if (z2) {
                        return (countX * i9) + i8;
                    }
                }
            }
        }
        return -1;
    }
}
