package com.android.launcher3.accessibility;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.widget.ExploreByTouchHelper;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import com.android.launcher3.CellLayout;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.accessibility.LauncherAccessibilityDelegate;
import java.util.List;
/* loaded from: a.zip:com/android/launcher3/accessibility/DragAndDropAccessibilityDelegate.class */
public abstract class DragAndDropAccessibilityDelegate extends ExploreByTouchHelper implements View.OnClickListener {
    private static final int[] sTempArray = new int[2];
    protected final Context mContext;
    protected final LauncherAccessibilityDelegate mDelegate;
    private final Rect mTempRect;
    protected final CellLayout mView;

    public DragAndDropAccessibilityDelegate(CellLayout cellLayout) {
        super(cellLayout);
        this.mTempRect = new Rect();
        this.mView = cellLayout;
        this.mContext = this.mView.getContext();
        this.mDelegate = LauncherAppState.getInstance().getAccessibilityDelegate();
    }

    private Rect getItemBounds(int i) {
        int countX = this.mView.getCountX();
        int countX2 = i / this.mView.getCountX();
        LauncherAccessibilityDelegate.DragInfo dragInfo = this.mDelegate.getDragInfo();
        this.mView.cellToRect(i % countX, countX2, dragInfo.info.spanX, dragInfo.info.spanY, this.mTempRect);
        return this.mTempRect;
    }

    protected abstract String getConfirmationForIconDrop(int i);

    protected abstract String getLocationDescriptionForIconDrop(int i);

    @Override // android.support.v4.widget.ExploreByTouchHelper
    protected int getVirtualViewAt(float f, float f2) {
        if (f < 0.0f || f2 < 0.0f || f > this.mView.getMeasuredWidth() || f2 > this.mView.getMeasuredHeight()) {
            return Integer.MIN_VALUE;
        }
        this.mView.pointToCellExact((int) f, (int) f2, sTempArray);
        return intersectsValidDropTarget(sTempArray[0] + (sTempArray[1] * this.mView.getCountX()));
    }

    @Override // android.support.v4.widget.ExploreByTouchHelper
    protected void getVisibleVirtualViews(List<Integer> list) {
        int countX = this.mView.getCountX();
        int countY = this.mView.getCountY();
        for (int i = 0; i < countX * countY; i++) {
            if (intersectsValidDropTarget(i) == i) {
                list.add(Integer.valueOf(i));
            }
        }
    }

    protected abstract int intersectsValidDropTarget(int i);

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        onPerformActionForVirtualView(getFocusedVirtualView(), 16, null);
    }

    @Override // android.support.v4.widget.ExploreByTouchHelper
    protected boolean onPerformActionForVirtualView(int i, int i2, Bundle bundle) {
        if (i2 != 16 || i == Integer.MIN_VALUE) {
            return false;
        }
        this.mDelegate.handleAccessibleDrop(this.mView, getItemBounds(i), getConfirmationForIconDrop(i));
        return true;
    }

    @Override // android.support.v4.widget.ExploreByTouchHelper
    protected void onPopulateEventForVirtualView(int i, AccessibilityEvent accessibilityEvent) {
        if (i == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Invalid virtual view id");
        }
        accessibilityEvent.setContentDescription(this.mContext.getString(2131558466));
    }

    @Override // android.support.v4.widget.ExploreByTouchHelper
    protected void onPopulateNodeForVirtualView(int i, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
        if (i == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Invalid virtual view id");
        }
        accessibilityNodeInfoCompat.setContentDescription(getLocationDescriptionForIconDrop(i));
        accessibilityNodeInfoCompat.setBoundsInParent(getItemBounds(i));
        accessibilityNodeInfoCompat.addAction(16);
        accessibilityNodeInfoCompat.setClickable(true);
        accessibilityNodeInfoCompat.setFocusable(true);
    }
}
