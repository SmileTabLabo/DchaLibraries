package com.android.launcher3;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.UserManager;
import android.util.AttributeSet;
import android.util.Pair;
import com.android.launcher3.DropTarget;
import com.android.launcher3.compat.UserHandleCompat;
/* loaded from: a.zip:com/android/launcher3/UninstallDropTarget.class */
public class UninstallDropTarget extends ButtonDropTarget {

    /* loaded from: a.zip:com/android/launcher3/UninstallDropTarget$UninstallSource.class */
    public interface UninstallSource {
        void deferCompleteDropAfterUninstallActivity();

        void onUninstallActivityReturned(boolean z);
    }

    public UninstallDropTarget(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public UninstallDropTarget(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    private static Pair<ComponentName, Integer> getAppInfoFlags(Object obj) {
        if (obj instanceof AppInfo) {
            AppInfo appInfo = (AppInfo) obj;
            return Pair.create(appInfo.componentName, Integer.valueOf(appInfo.flags));
        } else if (obj instanceof ShortcutInfo) {
            ShortcutInfo shortcutInfo = (ShortcutInfo) obj;
            ComponentName targetComponent = shortcutInfo.getTargetComponent();
            if (shortcutInfo.itemType != 0 || targetComponent == null) {
                return null;
            }
            return Pair.create(targetComponent, Integer.valueOf(shortcutInfo.flags));
        } else {
            return null;
        }
    }

    public static boolean startUninstallActivity(Launcher launcher, Object obj) {
        Pair<ComponentName, Integer> appInfoFlags = getAppInfoFlags(obj);
        return launcher.startApplicationUninstallActivity((ComponentName) appInfoFlags.first, ((Integer) appInfoFlags.second).intValue(), ((ItemInfo) obj).user);
    }

    @TargetApi(18)
    public static boolean supportsDrop(Context context, Object obj) {
        if (Utilities.ATLEAST_JB_MR2) {
            Bundle userRestrictions = ((UserManager) context.getSystemService("user")).getUserRestrictions();
            if (userRestrictions.getBoolean("no_control_apps", false) || userRestrictions.getBoolean("no_uninstall_apps", false)) {
                return false;
            }
        }
        Pair<ComponentName, Integer> appInfoFlags = getAppInfoFlags(obj);
        return (appInfoFlags == null || (((Integer) appInfoFlags.second).intValue() & 1) == 0) ? false : true;
    }

    @Override // com.android.launcher3.ButtonDropTarget
    void completeDrop(DropTarget.DragObject dragObject) {
        Pair<ComponentName, Integer> appInfoFlags = getAppInfoFlags(dragObject.dragInfo);
        UserHandleCompat userHandleCompat = ((ItemInfo) dragObject.dragInfo).user;
        if (!startUninstallActivity(this.mLauncher, dragObject.dragInfo)) {
            sendUninstallResult(dragObject.dragSource, false);
            return;
        }
        this.mLauncher.addOnResumeCallback(new Runnable(this, appInfoFlags, userHandleCompat, dragObject) { // from class: com.android.launcher3.UninstallDropTarget.1
            final UninstallDropTarget this$0;
            final Pair val$componentInfo;
            final DropTarget.DragObject val$d;
            final UserHandleCompat val$user;

            {
                this.this$0 = this;
                this.val$componentInfo = appInfoFlags;
                this.val$user = userHandleCompat;
                this.val$d = dragObject;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.sendUninstallResult(this.val$d.dragSource, !AllAppsList.packageHasActivities(this.this$0.getContext(), ((ComponentName) this.val$componentInfo.first).getPackageName(), this.val$user));
            }
        });
    }

    @Override // com.android.launcher3.ButtonDropTarget, com.android.launcher3.DropTarget
    public void onDrop(DropTarget.DragObject dragObject) {
        if (dragObject.dragSource instanceof UninstallSource) {
            ((UninstallSource) dragObject.dragSource).deferCompleteDropAfterUninstallActivity();
        }
        super.onDrop(dragObject);
    }

    @Override // com.android.launcher3.ButtonDropTarget, android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mHoverColor = getResources().getColor(2131361793);
        setDrawable(2130837535);
    }

    void sendUninstallResult(DragSource dragSource, boolean z) {
        if (dragSource instanceof UninstallSource) {
            ((UninstallSource) dragSource).onUninstallActivityReturned(z);
        }
    }

    @Override // com.android.launcher3.ButtonDropTarget
    protected boolean supportsDrop(DragSource dragSource, Object obj) {
        return supportsDrop(getContext(), obj);
    }
}
