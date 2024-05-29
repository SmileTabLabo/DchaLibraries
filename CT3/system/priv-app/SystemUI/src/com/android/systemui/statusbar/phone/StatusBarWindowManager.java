package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.os.SystemProperties;
import android.view.View;
import android.view.WindowManager;
import com.android.systemui.statusbar.BaseStatusBar;
import com.android.systemui.statusbar.RemoteInputController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.reflect.Field;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/StatusBarWindowManager.class */
public class StatusBarWindowManager implements RemoteInputController.Callback {
    private int mBarHeight;
    private final Context mContext;
    private final State mCurrentState = new State(null);
    private final boolean mKeyguardScreenRotation = shouldEnableKeyguardScreenRotation();
    private WindowManager.LayoutParams mLp;
    private WindowManager.LayoutParams mLpChanged;
    private final float mScreenBrightnessDoze;
    private View mStatusBarView;
    private final WindowManager mWindowManager;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/statusbar/phone/StatusBarWindowManager$State.class */
    public static class State {
        boolean backdropShowing;
        boolean bouncerShowing;
        boolean forceCollapsed;
        boolean forceDozeBrightness;
        boolean forceStatusBarVisible;
        boolean forceUserActivity;
        boolean headsUpShowing;
        boolean keyguardFadingAway;
        boolean keyguardNeedsInput;
        boolean keyguardOccluded;
        boolean keyguardShowing;
        boolean panelExpanded;
        boolean panelVisible;
        boolean qsExpanded;
        boolean remoteInputActive;
        boolean statusBarFocusable;
        int statusBarState;

        private State() {
        }

        /* synthetic */ State(State state) {
            this();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public boolean isKeyguardShowingAndNotOccluded() {
            boolean z = false;
            if (this.keyguardShowing) {
                z = !this.keyguardOccluded;
            }
            return z;
        }

        public String toString() {
            Field[] declaredFields;
            StringBuilder sb = new StringBuilder();
            sb.append("Window State {");
            sb.append("\n");
            for (Field field : getClass().getDeclaredFields()) {
                sb.append("  ");
                try {
                    sb.append(field.getName());
                    sb.append(": ");
                    sb.append(field.get(this));
                } catch (IllegalAccessException e) {
                }
                sb.append("\n");
            }
            sb.append("}");
            return sb.toString();
        }
    }

    public StatusBarWindowManager(Context context) {
        this.mContext = context;
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        this.mScreenBrightnessDoze = this.mContext.getResources().getInteger(17694815) / 255.0f;
    }

    private void adjustScreenOrientation(State state) {
        if (!state.isKeyguardShowingAndNotOccluded()) {
            this.mLpChanged.screenOrientation = -1;
        } else if (this.mKeyguardScreenRotation) {
            this.mLpChanged.screenOrientation = 2;
        } else {
            this.mLpChanged.screenOrientation = 5;
        }
    }

    private void apply(State state) {
        applyKeyguardFlags(state);
        applyForceStatusBarVisibleFlag(state);
        applyFocusableFlag(state);
        adjustScreenOrientation(state);
        applyHeight(state);
        applyUserActivityTimeout(state);
        applyInputFeatures(state);
        applyFitsSystemWindows(state);
        applyModalFlag(state);
        applyBrightness(state);
        if (this.mLp.copyFrom(this.mLpChanged) != 0) {
            this.mWindowManager.updateViewLayout(this.mStatusBarView, this.mLp);
        }
    }

    private void applyBrightness(State state) {
        if (!state.forceDozeBrightness) {
            this.mLpChanged.screenBrightness = -1.0f;
            return;
        }
        this.mLpChanged.screenBrightness = this.mScreenBrightnessDoze;
    }

    private void applyFitsSystemWindows(State state) {
        this.mStatusBarView.setFitsSystemWindows(!state.isKeyguardShowingAndNotOccluded());
    }

    private void applyFocusableFlag(State state) {
        boolean z = state.statusBarFocusable ? state.panelExpanded : false;
        if ((state.keyguardShowing && state.keyguardNeedsInput && state.bouncerShowing) || (BaseStatusBar.ENABLE_REMOTE_INPUT && state.remoteInputActive)) {
            this.mLpChanged.flags &= -9;
            this.mLpChanged.flags &= -131073;
        } else if (state.isKeyguardShowingAndNotOccluded() || z) {
            this.mLpChanged.flags &= -9;
            this.mLpChanged.flags |= 131072;
        } else {
            this.mLpChanged.flags |= 8;
            this.mLpChanged.flags &= -131073;
        }
        this.mLpChanged.softInputMode = 16;
    }

    private void applyForceStatusBarVisibleFlag(State state) {
        if (state.forceStatusBarVisible) {
            this.mLpChanged.privateFlags |= 4096;
            return;
        }
        this.mLpChanged.privateFlags &= -4097;
    }

    private void applyHeight(State state) {
        if (isExpanded(state)) {
            this.mLpChanged.height = -1;
            return;
        }
        this.mLpChanged.height = this.mBarHeight;
    }

    private void applyInputFeatures(State state) {
        if (!state.isKeyguardShowingAndNotOccluded() || state.statusBarState != 1 || state.qsExpanded || state.forceUserActivity) {
            this.mLpChanged.inputFeatures &= -5;
            return;
        }
        this.mLpChanged.inputFeatures |= 4;
    }

    private void applyKeyguardFlags(State state) {
        if (state.keyguardShowing) {
            this.mLpChanged.privateFlags |= 1024;
        } else {
            this.mLpChanged.privateFlags &= -1025;
        }
        if (!state.keyguardShowing || state.backdropShowing) {
            this.mLpChanged.flags &= -1048577;
            return;
        }
        this.mLpChanged.flags |= 1048576;
    }

    private void applyModalFlag(State state) {
        if (state.headsUpShowing) {
            this.mLpChanged.flags |= 32;
            return;
        }
        this.mLpChanged.flags &= -33;
    }

    private void applyUserActivityTimeout(State state) {
        if (state.isKeyguardShowingAndNotOccluded() && state.statusBarState == 1 && !state.qsExpanded) {
            this.mLpChanged.userActivityTimeout = 10000L;
        } else {
            this.mLpChanged.userActivityTimeout = -1L;
        }
    }

    private boolean isExpanded(State state) {
        return !state.forceCollapsed ? (state.isKeyguardShowingAndNotOccluded() || state.panelVisible || state.keyguardFadingAway || state.bouncerShowing) ? true : state.headsUpShowing : false;
    }

    private boolean shouldEnableKeyguardScreenRotation() {
        return !SystemProperties.getBoolean("lockscreen.rot_override", false) ? this.mContext.getResources().getBoolean(2131623970) : true;
    }

    public void add(View view, int i) {
        this.mLp = new WindowManager.LayoutParams(-1, i, 2000, -2138832824, -3);
        this.mLp.flags |= 16777216;
        this.mLp.gravity = 48;
        this.mLp.softInputMode = 16;
        this.mLp.setTitle("StatusBar");
        this.mLp.packageName = this.mContext.getPackageName();
        this.mStatusBarView = view;
        this.mBarHeight = i;
        this.mWindowManager.addView(this.mStatusBarView, this.mLp);
        this.mLpChanged = new WindowManager.LayoutParams();
        this.mLpChanged.copyFrom(this.mLp);
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("StatusBarWindowManager state:");
        printWriter.println(this.mCurrentState);
    }

    public boolean isShowingWallpaper() {
        return !this.mCurrentState.backdropShowing;
    }

    @Override // com.android.systemui.statusbar.RemoteInputController.Callback
    public void onRemoteInputActive(boolean z) {
        this.mCurrentState.remoteInputActive = z;
        apply(this.mCurrentState);
    }

    public void setBackdropShowing(boolean z) {
        this.mCurrentState.backdropShowing = z;
        apply(this.mCurrentState);
    }

    public void setBarHeight(int i) {
        this.mBarHeight = i;
        apply(this.mCurrentState);
    }

    public void setBouncerShowing(boolean z) {
        this.mCurrentState.bouncerShowing = z;
        apply(this.mCurrentState);
    }

    public void setForceDozeBrightness(boolean z) {
        this.mCurrentState.forceDozeBrightness = z;
        apply(this.mCurrentState);
    }

    public void setForceStatusBarVisible(boolean z) {
        this.mCurrentState.forceStatusBarVisible = z;
        apply(this.mCurrentState);
    }

    public void setForceWindowCollapsed(boolean z) {
        this.mCurrentState.forceCollapsed = z;
        apply(this.mCurrentState);
    }

    public void setHeadsUpShowing(boolean z) {
        this.mCurrentState.headsUpShowing = z;
        apply(this.mCurrentState);
    }

    public void setKeyguardFadingAway(boolean z) {
        this.mCurrentState.keyguardFadingAway = z;
        apply(this.mCurrentState);
    }

    public void setKeyguardNeedsInput(boolean z) {
        this.mCurrentState.keyguardNeedsInput = z;
        apply(this.mCurrentState);
    }

    public void setKeyguardOccluded(boolean z) {
        this.mCurrentState.keyguardOccluded = z;
        apply(this.mCurrentState);
    }

    public void setKeyguardShowing(boolean z) {
        this.mCurrentState.keyguardShowing = z;
        apply(this.mCurrentState);
    }

    public void setPanelExpanded(boolean z) {
        this.mCurrentState.panelExpanded = z;
        apply(this.mCurrentState);
    }

    public void setPanelVisible(boolean z) {
        this.mCurrentState.panelVisible = z;
        this.mCurrentState.statusBarFocusable = z;
        apply(this.mCurrentState);
    }

    public void setQsExpanded(boolean z) {
        this.mCurrentState.qsExpanded = z;
        apply(this.mCurrentState);
    }

    public void setStatusBarFocusable(boolean z) {
        this.mCurrentState.statusBarFocusable = z;
        apply(this.mCurrentState);
    }

    public void setStatusBarState(int i) {
        this.mCurrentState.statusBarState = i;
        apply(this.mCurrentState);
    }
}
