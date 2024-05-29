package com.android.launcher3;

import android.content.ComponentName;
import android.content.Context;
import android.util.AttributeSet;
import com.android.launcher3.DropTarget;
import com.android.launcher3.compat.UserHandleCompat;
/* loaded from: a.zip:com/android/launcher3/InfoDropTarget.class */
public class InfoDropTarget extends ButtonDropTarget {
    public InfoDropTarget(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public InfoDropTarget(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public static void startDetailsActivityForInfo(Object obj, Launcher launcher) {
        ComponentName componentName = null;
        if (obj instanceof AppInfo) {
            componentName = ((AppInfo) obj).componentName;
        } else if (obj instanceof ShortcutInfo) {
            componentName = ((ShortcutInfo) obj).intent.getComponent();
        } else if (obj instanceof PendingAddItemInfo) {
            componentName = ((PendingAddItemInfo) obj).componentName;
        }
        UserHandleCompat myUserHandle = obj instanceof ItemInfo ? ((ItemInfo) obj).user : UserHandleCompat.myUserHandle();
        if (componentName != null) {
            launcher.startApplicationDetailsActivity(componentName, myUserHandle);
        }
    }

    public static boolean supportsDrop(Context context, Object obj) {
        return !(obj instanceof AppInfo) ? obj instanceof PendingAddItemInfo : true;
    }

    @Override // com.android.launcher3.ButtonDropTarget
    void completeDrop(DropTarget.DragObject dragObject) {
        startDetailsActivityForInfo(dragObject.dragInfo, this.mLauncher);
    }

    @Override // com.android.launcher3.ButtonDropTarget, android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mHoverColor = getResources().getColor(2131361794);
        setDrawable(2130837523);
    }

    @Override // com.android.launcher3.ButtonDropTarget
    protected boolean supportsDrop(DragSource dragSource, Object obj) {
        return dragSource.supportsAppInfoDropTarget() ? supportsDrop(getContext(), obj) : false;
    }
}
