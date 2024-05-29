package com.android.systemui.statusbar.tv;

import android.content.ComponentName;
import android.graphics.Rect;
import android.os.IBinder;
import android.os.RemoteException;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.statusbar.ActivatableNotificationView;
import com.android.systemui.statusbar.BaseStatusBar;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.tv.pip.PipManager;
/* loaded from: a.zip:com/android/systemui/statusbar/tv/TvStatusBar.class */
public class TvStatusBar extends BaseStatusBar {
    int mSystemUiVisibility = 0;
    private int mLastDispatchedSystemUiVisibility = -1;

    private void notifyUiVisibilityChanged(int i) {
        try {
            if (this.mLastDispatchedSystemUiVisibility != i) {
                this.mWindowManagerService.statusBarVisibilityChanged(i);
                this.mLastDispatchedSystemUiVisibility = i;
            }
        } catch (RemoteException e) {
        }
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar
    public void addNotification(StatusBarNotification statusBarNotification, NotificationListenerService.RankingMap rankingMap, NotificationData.Entry entry) {
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void addQsTile(ComponentName componentName) {
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void animateCollapsePanels(int i) {
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void animateExpandNotificationsPanel() {
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void animateExpandSettingsPanel(String str) {
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void appTransitionCancelled() {
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void appTransitionFinished() {
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void appTransitionPending() {
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void appTransitionStarting(long j, long j2) {
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void buzzBeepBlinked() {
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void clickTile(ComponentName componentName) {
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar
    protected void createAndAddWindows() {
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void disable(int i, int i2, boolean z) {
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar
    protected int getMaxKeyguardNotifications(boolean z) {
        return 0;
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar
    public boolean isPanelFullyCollapsed() {
        return false;
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar
    protected boolean isSnoozedPackage(StatusBarNotification statusBarNotification) {
        return false;
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar
    public void maybeEscalateHeadsUp() {
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void notificationLightOff() {
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void notificationLightPulse(int i, int i2, int i3) {
    }

    @Override // com.android.systemui.statusbar.ActivatableNotificationView.OnActivatedListener
    public void onActivated(ActivatableNotificationView activatableNotificationView) {
    }

    @Override // com.android.systemui.statusbar.ActivatableNotificationView.OnActivatedListener
    public void onActivationReset(ActivatableNotificationView activatableNotificationView) {
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void onCameraLaunchGestureDetected(int i) {
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar
    protected void refreshLayout(int i) {
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void remQsTile(ComponentName componentName) {
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void removeIcon(String str) {
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar
    public void removeNotification(String str, NotificationListenerService.RankingMap rankingMap) {
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar
    protected void setAreThereNotifications() {
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar
    protected void setHeadsUpUser(int i) {
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void setIcon(String str, StatusBarIcon statusBarIcon) {
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void setImeWindowStatus(IBinder iBinder, int i, int i2, boolean z) {
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void setSystemUiVisibility(int i, int i2, int i3, int i4, Rect rect, Rect rect2) {
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void setWindowState(int i, int i2) {
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void showScreenPinningRequest(int i) {
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void showTvPictureInPictureMenu() {
        PipManager.getInstance().showTvPictureInPictureMenu();
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar, com.android.systemui.SystemUI
    public void start() {
        super.start();
        putComponent(TvStatusBar.class, this);
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar
    protected void toggleSplitScreenMode(int i, int i2) {
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void topAppWindowChanged(boolean z) {
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar
    protected void updateHeadsUp(String str, NotificationData.Entry entry, boolean z, boolean z2) {
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar
    protected void updateNotificationRanking(NotificationListenerService.RankingMap rankingMap) {
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar
    protected void updateNotifications() {
    }

    public void updatePipVisibility(boolean z) {
        if (z) {
            this.mSystemUiVisibility |= 65536;
        } else {
            this.mSystemUiVisibility &= -65537;
        }
        notifyUiVisibilityChanged(this.mSystemUiVisibility);
    }

    public void updateRecentsVisibility(boolean z) {
        if (z) {
            this.mSystemUiVisibility |= 16384;
        } else {
            this.mSystemUiVisibility &= -16385;
        }
        notifyUiVisibilityChanged(this.mSystemUiVisibility);
    }
}
