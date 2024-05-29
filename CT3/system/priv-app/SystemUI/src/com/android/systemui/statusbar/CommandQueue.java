package com.android.systemui.statusbar;

import android.content.ComponentName;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Pair;
import com.android.internal.os.SomeArgs;
import com.android.internal.statusbar.IStatusBar;
import com.android.internal.statusbar.StatusBarIcon;
/* loaded from: a.zip:com/android/systemui/statusbar/CommandQueue.class */
public class CommandQueue extends IStatusBar.Stub {
    private Callbacks mCallbacks;
    private final Object mLock = new Object();
    private Handler mHandler = new H(this, null);

    /* loaded from: a.zip:com/android/systemui/statusbar/CommandQueue$Callbacks.class */
    public interface Callbacks {
        void addQsTile(ComponentName componentName);

        void animateCollapsePanels(int i);

        void animateExpandNotificationsPanel();

        void animateExpandSettingsPanel(String str);

        void appTransitionCancelled();

        void appTransitionFinished();

        void appTransitionPending();

        void appTransitionStarting(long j, long j2);

        void buzzBeepBlinked();

        void cancelPreloadRecentApps();

        void clickTile(ComponentName componentName);

        void disable(int i, int i2, boolean z);

        void dismissKeyboardShortcutsMenu();

        void hideRecentApps(boolean z, boolean z2);

        void notificationLightOff();

        void notificationLightPulse(int i, int i2, int i3);

        void onCameraLaunchGestureDetected(int i);

        void preloadRecentApps();

        void remQsTile(ComponentName componentName);

        void removeIcon(String str);

        void setIcon(String str, StatusBarIcon statusBarIcon);

        void setImeWindowStatus(IBinder iBinder, int i, int i2, boolean z);

        void setSystemUiVisibility(int i, int i2, int i3, int i4, Rect rect, Rect rect2);

        void setWindowState(int i, int i2);

        void showAssistDisclosure();

        void showRecentApps(boolean z, boolean z2);

        void showScreenPinningRequest(int i);

        void showTvPictureInPictureMenu();

        void startAssist(Bundle bundle);

        void toggleKeyboardShortcutsMenu(int i);

        void toggleRecentApps();

        void toggleSplitScreen();

        void topAppWindowChanged(boolean z);
    }

    /* loaded from: a.zip:com/android/systemui/statusbar/CommandQueue$H.class */
    private final class H extends Handler {
        final CommandQueue this$0;

        private H(CommandQueue commandQueue) {
            this.this$0 = commandQueue;
        }

        /* synthetic */ H(CommandQueue commandQueue, H h) {
            this(commandQueue);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            boolean z = true;
            boolean z2 = true;
            switch (message.what & (-65536)) {
                case 65536:
                    switch (message.arg1) {
                        case 1:
                            Pair pair = (Pair) message.obj;
                            this.this$0.mCallbacks.setIcon((String) pair.first, (StatusBarIcon) pair.second);
                            return;
                        case 2:
                            this.this$0.mCallbacks.removeIcon((String) message.obj);
                            return;
                        default:
                            return;
                    }
                case 131072:
                    this.this$0.mCallbacks.disable(message.arg1, message.arg2, true);
                    return;
                case 196608:
                    this.this$0.mCallbacks.animateExpandNotificationsPanel();
                    return;
                case 262144:
                    this.this$0.mCallbacks.animateCollapsePanels(0);
                    return;
                case 327680:
                    this.this$0.mCallbacks.animateExpandSettingsPanel((String) message.obj);
                    return;
                case 393216:
                    SomeArgs someArgs = (SomeArgs) message.obj;
                    this.this$0.mCallbacks.setSystemUiVisibility(someArgs.argi1, someArgs.argi2, someArgs.argi3, someArgs.argi4, (Rect) someArgs.arg1, (Rect) someArgs.arg2);
                    someArgs.recycle();
                    return;
                case 458752:
                    Callbacks callbacks = this.this$0.mCallbacks;
                    if (message.arg1 == 0) {
                        z2 = false;
                    }
                    callbacks.topAppWindowChanged(z2);
                    return;
                case 524288:
                    this.this$0.mCallbacks.setImeWindowStatus((IBinder) message.obj, message.arg1, message.arg2, message.getData().getBoolean("showImeSwitcherKey", false));
                    return;
                case 589824:
                    this.this$0.mCallbacks.toggleRecentApps();
                    return;
                case 655360:
                    this.this$0.mCallbacks.preloadRecentApps();
                    return;
                case 720896:
                    this.this$0.mCallbacks.cancelPreloadRecentApps();
                    return;
                case 786432:
                    this.this$0.mCallbacks.setWindowState(message.arg1, message.arg2);
                    return;
                case 851968:
                    Callbacks callbacks2 = this.this$0.mCallbacks;
                    boolean z3 = message.arg1 != 0;
                    if (message.arg2 == 0) {
                        z = false;
                    }
                    callbacks2.showRecentApps(z3, z);
                    return;
                case 917504:
                    this.this$0.mCallbacks.hideRecentApps(message.arg1 != 0, message.arg2 != 0);
                    return;
                case 983040:
                    this.this$0.mCallbacks.buzzBeepBlinked();
                    return;
                case 1048576:
                    this.this$0.mCallbacks.notificationLightOff();
                    return;
                case 1114112:
                    this.this$0.mCallbacks.notificationLightPulse(((Integer) message.obj).intValue(), message.arg1, message.arg2);
                    return;
                case 1179648:
                    this.this$0.mCallbacks.showScreenPinningRequest(message.arg1);
                    return;
                case 1245184:
                    this.this$0.mCallbacks.appTransitionPending();
                    return;
                case 1310720:
                    this.this$0.mCallbacks.appTransitionCancelled();
                    return;
                case 1376256:
                    Pair pair2 = (Pair) message.obj;
                    this.this$0.mCallbacks.appTransitionStarting(((Long) pair2.first).longValue(), ((Long) pair2.second).longValue());
                    return;
                case 1441792:
                    this.this$0.mCallbacks.showAssistDisclosure();
                    return;
                case 1507328:
                    this.this$0.mCallbacks.startAssist((Bundle) message.obj);
                    return;
                case 1572864:
                    this.this$0.mCallbacks.onCameraLaunchGestureDetected(message.arg1);
                    return;
                case 1638400:
                    this.this$0.mCallbacks.toggleKeyboardShortcutsMenu(message.arg1);
                    return;
                case 1703936:
                    this.this$0.mCallbacks.showTvPictureInPictureMenu();
                    return;
                case 1769472:
                    this.this$0.mCallbacks.addQsTile((ComponentName) message.obj);
                    return;
                case 1835008:
                    this.this$0.mCallbacks.remQsTile((ComponentName) message.obj);
                    return;
                case 1900544:
                    this.this$0.mCallbacks.clickTile((ComponentName) message.obj);
                    return;
                case 1966080:
                    this.this$0.mCallbacks.toggleSplitScreen();
                    return;
                case 2031616:
                    this.this$0.mCallbacks.appTransitionFinished();
                    return;
                case 2097152:
                    this.this$0.mCallbacks.dismissKeyboardShortcutsMenu();
                    return;
                default:
                    return;
            }
        }
    }

    public CommandQueue(Callbacks callbacks) {
        this.mCallbacks = callbacks;
    }

    public void addQsTile(ComponentName componentName) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(1769472, componentName).sendToTarget();
        }
    }

    public void animateCollapsePanels() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(262144);
            this.mHandler.sendEmptyMessage(262144);
        }
    }

    public void animateExpandNotificationsPanel() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(196608);
            this.mHandler.sendEmptyMessage(196608);
        }
    }

    public void animateExpandSettingsPanel(String str) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(327680);
            this.mHandler.obtainMessage(327680, str).sendToTarget();
        }
    }

    public void appTransitionCancelled() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(1245184);
            this.mHandler.sendEmptyMessage(1245184);
        }
    }

    public void appTransitionFinished() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(2031616);
            this.mHandler.sendEmptyMessage(2031616);
        }
    }

    public void appTransitionPending() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(1245184);
            this.mHandler.sendEmptyMessage(1245184);
        }
    }

    public void appTransitionStarting(long j, long j2) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(1376256);
            this.mHandler.obtainMessage(1376256, Pair.create(Long.valueOf(j), Long.valueOf(j2))).sendToTarget();
        }
    }

    public void buzzBeepBlinked() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(983040);
            this.mHandler.sendEmptyMessage(983040);
        }
    }

    public void cancelPreloadRecentApps() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(720896);
            this.mHandler.obtainMessage(720896, 0, 0, null).sendToTarget();
        }
    }

    public void clickQsTile(ComponentName componentName) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(1900544, componentName).sendToTarget();
        }
    }

    public void disable(int i, int i2) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(131072);
            this.mHandler.obtainMessage(131072, i, i2, null).sendToTarget();
        }
    }

    public void dismissKeyboardShortcutsMenu() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(2097152);
            this.mHandler.obtainMessage(2097152).sendToTarget();
        }
    }

    public void hideRecentApps(boolean z, boolean z2) {
        int i = 1;
        synchronized (this.mLock) {
            this.mHandler.removeMessages(917504);
            Handler handler = this.mHandler;
            int i2 = z ? 1 : 0;
            if (!z2) {
                i = 0;
            }
            handler.obtainMessage(917504, i2, i, null).sendToTarget();
        }
    }

    public void notificationLightOff() {
        synchronized (this.mLock) {
            this.mHandler.sendEmptyMessage(1048576);
        }
    }

    public void notificationLightPulse(int i, int i2, int i3) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(1114112, i2, i3, Integer.valueOf(i)).sendToTarget();
        }
    }

    public void onCameraLaunchGestureDetected(int i) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(1572864);
            this.mHandler.obtainMessage(1572864, i, 0).sendToTarget();
        }
    }

    public void preloadRecentApps() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(655360);
            this.mHandler.obtainMessage(655360, 0, 0, null).sendToTarget();
        }
    }

    public void remQsTile(ComponentName componentName) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(1835008, componentName).sendToTarget();
        }
    }

    public void removeIcon(String str) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(65536, 2, 0, str).sendToTarget();
        }
    }

    public void setIcon(String str, StatusBarIcon statusBarIcon) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(65536, 1, 0, new Pair(str, statusBarIcon)).sendToTarget();
        }
    }

    public void setImeWindowStatus(IBinder iBinder, int i, int i2, boolean z) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(524288);
            Message obtainMessage = this.mHandler.obtainMessage(524288, i, i2, iBinder);
            obtainMessage.getData().putBoolean("showImeSwitcherKey", z);
            obtainMessage.sendToTarget();
        }
    }

    public void setSystemUiVisibility(int i, int i2, int i3, int i4, Rect rect, Rect rect2) {
        synchronized (this.mLock) {
            SomeArgs obtain = SomeArgs.obtain();
            obtain.argi1 = i;
            obtain.argi2 = i2;
            obtain.argi3 = i3;
            obtain.argi4 = i4;
            obtain.arg1 = rect;
            obtain.arg2 = rect2;
            this.mHandler.obtainMessage(393216, obtain).sendToTarget();
        }
    }

    public void setWindowState(int i, int i2) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(786432, i, i2, null).sendToTarget();
        }
    }

    public void showAssistDisclosure() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(1441792);
            this.mHandler.obtainMessage(1441792).sendToTarget();
        }
    }

    public void showRecentApps(boolean z, boolean z2) {
        int i = 1;
        synchronized (this.mLock) {
            this.mHandler.removeMessages(851968);
            Handler handler = this.mHandler;
            int i2 = z ? 1 : 0;
            if (!z2) {
                i = 0;
            }
            handler.obtainMessage(851968, i2, i, null).sendToTarget();
        }
    }

    public void showScreenPinningRequest(int i) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(1179648, i, 0, null).sendToTarget();
        }
    }

    public void showTvPictureInPictureMenu() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(1703936);
            this.mHandler.obtainMessage(1703936).sendToTarget();
        }
    }

    public void startAssist(Bundle bundle) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(1507328);
            this.mHandler.obtainMessage(1507328, bundle).sendToTarget();
        }
    }

    public void toggleKeyboardShortcutsMenu(int i) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(1638400);
            this.mHandler.obtainMessage(1638400, i, 0).sendToTarget();
        }
    }

    public void toggleRecentApps() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(589824);
            this.mHandler.obtainMessage(589824, 0, 0, null).sendToTarget();
        }
    }

    public void toggleSplitScreen() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(1966080);
            this.mHandler.obtainMessage(1966080, 0, 0, null).sendToTarget();
        }
    }

    public void topAppWindowChanged(boolean z) {
        int i = 0;
        synchronized (this.mLock) {
            this.mHandler.removeMessages(458752);
            Handler handler = this.mHandler;
            if (z) {
                i = 1;
            }
            handler.obtainMessage(458752, i, 0, null).sendToTarget();
        }
    }
}
