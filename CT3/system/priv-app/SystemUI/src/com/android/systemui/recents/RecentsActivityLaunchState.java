package com.android.systemui.recents;
/* loaded from: a.zip:com/android/systemui/recents/RecentsActivityLaunchState.class */
public class RecentsActivityLaunchState {
    public boolean launchedFromApp;
    public boolean launchedFromHome;
    public int launchedNumVisibleTasks;
    public int launchedNumVisibleThumbnails;
    public int launchedToTaskId;
    public boolean launchedViaDockGesture;
    public boolean launchedViaDragGesture;
    public boolean launchedWithAltTab;

    public int getInitialFocusTaskIndex(int i) {
        RecentsDebugFlags debugFlags = Recents.getDebugFlags();
        RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
        if (this.launchedFromApp) {
            return (launchState.launchedWithAltTab || !debugFlags.isFastToggleRecentsEnabled()) ? Math.max(0, i - 2) : i - 1;
        } else if (launchState.launchedWithAltTab || !debugFlags.isFastToggleRecentsEnabled()) {
            return i - 1;
        } else {
            return -1;
        }
    }

    public void reset() {
        this.launchedFromHome = false;
        this.launchedFromApp = false;
        this.launchedToTaskId = -1;
        this.launchedWithAltTab = false;
        this.launchedViaDragGesture = false;
        this.launchedViaDockGesture = false;
    }
}
