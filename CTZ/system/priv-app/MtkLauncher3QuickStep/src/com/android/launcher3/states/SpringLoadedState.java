package com.android.launcher3.states;

import android.graphics.Rect;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.InstallShortcutReceiver;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherState;
import com.android.launcher3.Workspace;
/* loaded from: classes.dex */
public class SpringLoadedState extends LauncherState {
    private static final int STATE_FLAGS = 319;

    public SpringLoadedState(int i) {
        super(i, 6, 150, STATE_FLAGS);
    }

    @Override // com.android.launcher3.LauncherState
    public float[] getWorkspaceScaleAndTranslation(Launcher launcher) {
        Rect insets;
        DeviceProfile deviceProfile = launcher.getDeviceProfile();
        Workspace workspace = launcher.getWorkspace();
        if (workspace.getChildCount() == 0) {
            return super.getWorkspaceScaleAndTranslation(launcher);
        }
        if (deviceProfile.isVerticalBarLayout()) {
            return new float[]{deviceProfile.workspaceSpringLoadShrinkFactor, 0.0f, 0.0f};
        }
        float f = deviceProfile.workspaceSpringLoadShrinkFactor;
        float f2 = launcher.getDragLayer().getInsets().top + deviceProfile.dropTargetBarSizePx;
        float measuredHeight = f2 + ((((((workspace.getMeasuredHeight() - insets.bottom) - deviceProfile.workspacePadding.bottom) - deviceProfile.workspaceSpringLoadedBottomSpace) - f2) - (workspace.getNormalChildHeight() * f)) / 2.0f);
        float height = workspace.getHeight() / 2;
        return new float[]{f, 0.0f, (measuredHeight - ((workspace.getTop() + height) - ((height - workspace.getChildAt(0).getTop()) * f))) / f};
    }

    @Override // com.android.launcher3.LauncherState
    public void onStateEnabled(Launcher launcher) {
        Workspace workspace = launcher.getWorkspace();
        workspace.showPageIndicatorAtCurrentScroll();
        workspace.getPageIndicator().setShouldAutoHide(false);
        InstallShortcutReceiver.enableInstallQueue(4);
        launcher.getRotationHelper().setCurrentStateRequest(2);
    }

    @Override // com.android.launcher3.LauncherState
    public float getWorkspaceScrimAlpha(Launcher launcher) {
        return 0.3f;
    }

    @Override // com.android.launcher3.LauncherState
    public void onStateDisabled(Launcher launcher) {
        launcher.getWorkspace().getPageIndicator().setShouldAutoHide(true);
        InstallShortcutReceiver.disableAndFlushInstallQueue(4, launcher);
    }
}
