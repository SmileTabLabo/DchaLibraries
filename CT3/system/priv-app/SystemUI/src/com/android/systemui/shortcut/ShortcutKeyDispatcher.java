package com.android.systemui.shortcut;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.os.RemoteException;
import android.util.Log;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;
import com.android.internal.policy.DividerSnapAlgorithm;
import com.android.systemui.SystemUI;
import com.android.systemui.recents.Recents;
import com.android.systemui.shortcut.ShortcutKeyServiceProxy;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.stackdivider.DividerView;
/* loaded from: a.zip:com/android/systemui/shortcut/ShortcutKeyDispatcher.class */
public class ShortcutKeyDispatcher extends SystemUI implements ShortcutKeyServiceProxy.Callbacks {
    private ShortcutKeyServiceProxy mShortcutKeyServiceProxy = new ShortcutKeyServiceProxy(this);
    private IWindowManager mWindowManagerService = WindowManagerGlobal.getWindowManagerService();
    private IActivityManager mActivityManager = ActivityManagerNative.getDefault();
    protected final long META_MASK = 281474976710656L;
    protected final long ALT_MASK = 8589934592L;
    protected final long CTRL_MASK = 17592186044416L;
    protected final long SHIFT_MASK = 4294967296L;
    protected final long SC_DOCK_LEFT = 281474976710727L;
    protected final long SC_DOCK_RIGHT = 281474976710728L;

    private void handleDockKey(long j) {
        try {
            if (this.mWindowManagerService.getDockedStackSide() == -1) {
                ((Recents) getComponent(Recents.class)).dockTopTask(-1, j == 281474976710727L ? 0 : 1, null, 352);
            } else {
                DividerView view = ((Divider) getComponent(Divider.class)).getView();
                DividerSnapAlgorithm snapAlgorithm = view.getSnapAlgorithm();
                DividerSnapAlgorithm.SnapTarget cycleNonDismissTarget = snapAlgorithm.cycleNonDismissTarget(snapAlgorithm.calculateNonDismissingSnapTarget(view.getCurrentPosition()), j == 281474976710727L ? -1 : 1);
                view.startDragging(true, false);
                view.stopDragging(cycleNonDismissTarget.position, 0.0f, true, true);
            }
        } catch (RemoteException e) {
            Log.e("ShortcutKeyDispatcher", "handleDockKey() failed.");
        }
    }

    @Override // com.android.systemui.shortcut.ShortcutKeyServiceProxy.Callbacks
    public void onShortcutKeyPressed(long j) {
        int i = this.mContext.getResources().getConfiguration().orientation;
        if ((j == 281474976710727L || j == 281474976710728L) && i == 2) {
            handleDockKey(j);
        }
    }

    public void registerShortcutKey(long j) {
        try {
            this.mWindowManagerService.registerShortcutKey(j, this.mShortcutKeyServiceProxy);
        } catch (RemoteException e) {
        }
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        registerShortcutKey(281474976710727L);
        registerShortcutKey(281474976710728L);
    }
}
