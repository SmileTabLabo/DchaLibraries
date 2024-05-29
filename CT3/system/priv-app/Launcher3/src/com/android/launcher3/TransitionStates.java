package com.android.launcher3;

import com.android.launcher3.Workspace;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: a.zip:com/android/launcher3/TransitionStates.class */
public class TransitionStates {
    final boolean allAppsToWorkspace;
    final boolean oldStateIsNormal;
    final boolean oldStateIsNormalHidden;
    final boolean oldStateIsOverview;
    final boolean oldStateIsOverviewHidden;
    final boolean oldStateIsSpringLoaded;
    final boolean overviewToAllApps;
    final boolean overviewToWorkspace;
    final boolean stateIsNormal;
    final boolean stateIsNormalHidden;
    final boolean stateIsOverview;
    final boolean stateIsOverviewHidden;
    final boolean stateIsSpringLoaded;
    final boolean workspaceToAllApps;
    final boolean workspaceToOverview;

    public TransitionStates(Workspace.State state, Workspace.State state2) {
        this.oldStateIsNormal = state == Workspace.State.NORMAL;
        this.oldStateIsSpringLoaded = state == Workspace.State.SPRING_LOADED;
        this.oldStateIsNormalHidden = state == Workspace.State.NORMAL_HIDDEN;
        this.oldStateIsOverviewHidden = state == Workspace.State.OVERVIEW_HIDDEN;
        this.oldStateIsOverview = state == Workspace.State.OVERVIEW;
        this.stateIsNormal = state2 == Workspace.State.NORMAL;
        this.stateIsSpringLoaded = state2 == Workspace.State.SPRING_LOADED;
        this.stateIsNormalHidden = state2 == Workspace.State.NORMAL_HIDDEN;
        this.stateIsOverviewHidden = state2 == Workspace.State.OVERVIEW_HIDDEN;
        this.stateIsOverview = state2 == Workspace.State.OVERVIEW;
        this.workspaceToOverview = this.oldStateIsNormal ? this.stateIsOverview : false;
        this.workspaceToAllApps = this.oldStateIsNormal ? this.stateIsNormalHidden : false;
        this.overviewToWorkspace = this.oldStateIsOverview ? this.stateIsNormal : false;
        this.overviewToAllApps = this.oldStateIsOverview ? this.stateIsOverviewHidden : false;
        this.allAppsToWorkspace = this.stateIsNormalHidden ? this.stateIsNormal : false;
    }
}
