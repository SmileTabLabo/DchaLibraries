package com.android.launcher3.accessibility;

import com.android.launcher3.CellLayout;
import com.android.launcher3.FolderPagedView;
/* loaded from: a.zip:com/android/launcher3/accessibility/FolderAccessibilityHelper.class */
public class FolderAccessibilityHelper extends DragAndDropAccessibilityDelegate {
    private final FolderPagedView mParent;
    private final int mStartPosition;

    public FolderAccessibilityHelper(CellLayout cellLayout) {
        super(cellLayout);
        this.mParent = (FolderPagedView) cellLayout.getParent();
        this.mStartPosition = cellLayout.getCountX() * this.mParent.indexOfChild(cellLayout) * cellLayout.getCountY();
    }

    @Override // com.android.launcher3.accessibility.DragAndDropAccessibilityDelegate
    protected String getConfirmationForIconDrop(int i) {
        return this.mContext.getString(2131558473);
    }

    @Override // com.android.launcher3.accessibility.DragAndDropAccessibilityDelegate
    protected String getLocationDescriptionForIconDrop(int i) {
        return this.mContext.getString(2131558471, Integer.valueOf(this.mStartPosition + i + 1));
    }

    @Override // com.android.launcher3.accessibility.DragAndDropAccessibilityDelegate
    protected int intersectsValidDropTarget(int i) {
        return Math.min(i, (this.mParent.getAllocatedContentSize() - this.mStartPosition) - 1);
    }
}
