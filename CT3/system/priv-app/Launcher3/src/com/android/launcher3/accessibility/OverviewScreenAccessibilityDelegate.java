package com.android.launcher3.accessibility;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import com.android.launcher3.Utilities;
import com.android.launcher3.Workspace;
@TargetApi(21)
/* loaded from: a.zip:com/android/launcher3/accessibility/OverviewScreenAccessibilityDelegate.class */
public class OverviewScreenAccessibilityDelegate extends View.AccessibilityDelegate {
    private final SparseArray<AccessibilityNodeInfo.AccessibilityAction> mActions = new SparseArray<>();
    private final Workspace mWorkspace;

    public OverviewScreenAccessibilityDelegate(Workspace workspace) {
        this.mWorkspace = workspace;
        Context context = this.mWorkspace.getContext();
        boolean isRtl = Utilities.isRtl(context.getResources());
        this.mActions.put(2131296266, new AccessibilityNodeInfo.AccessibilityAction(2131296266, context.getText(isRtl ? 2131558481 : 2131558480)));
        this.mActions.put(2131296267, new AccessibilityNodeInfo.AccessibilityAction(2131296267, context.getText(isRtl ? 2131558480 : 2131558481)));
    }

    private void movePage(int i, View view) {
        this.mWorkspace.onStartReordering();
        this.mWorkspace.removeView(view);
        this.mWorkspace.addView(view, i);
        this.mWorkspace.onEndReordering();
        this.mWorkspace.announceForAccessibility(this.mWorkspace.getContext().getText(2131558482));
        this.mWorkspace.updateAccessibilityFlags();
        view.performAccessibilityAction(64, null);
    }

    @Override // android.view.View.AccessibilityDelegate
    public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfo);
        int indexOfChild = this.mWorkspace.indexOfChild(view);
        if (indexOfChild < this.mWorkspace.getChildCount() - 1) {
            accessibilityNodeInfo.addAction(this.mActions.get(2131296267));
        }
        if (indexOfChild > this.mWorkspace.numCustomPages()) {
            accessibilityNodeInfo.addAction(this.mActions.get(2131296266));
        }
    }

    @Override // android.view.View.AccessibilityDelegate
    public boolean performAccessibilityAction(View view, int i, Bundle bundle) {
        if (view != null) {
            if (i == 64) {
                this.mWorkspace.setCurrentPage(this.mWorkspace.indexOfChild(view));
            } else if (i == 2131296267) {
                movePage(this.mWorkspace.indexOfChild(view) + 1, view);
                return true;
            } else if (i == 2131296266) {
                movePage(this.mWorkspace.indexOfChild(view) - 1, view);
                return true;
            }
        }
        return super.performAccessibilityAction(view, i, bundle);
    }
}
