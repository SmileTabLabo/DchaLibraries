package com.android.launcher3.allapps;
/* loaded from: a.zip:com/android/launcher3/allapps/DefaultAppSearchController.class */
public class DefaultAppSearchController extends AllAppsSearchBarController {
    @Override // com.android.launcher3.allapps.AllAppsSearchBarController
    public DefaultAppSearchAlgorithm onInitializeSearch() {
        return new DefaultAppSearchAlgorithm(this.mApps.getApps());
    }
}
