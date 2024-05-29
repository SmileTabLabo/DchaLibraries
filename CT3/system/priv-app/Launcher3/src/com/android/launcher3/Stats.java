package com.android.launcher3;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewParent;
/* loaded from: a.zip:com/android/launcher3/Stats.class */
public class Stats {
    private final String mLaunchBroadcastPermission;
    private final Launcher mLauncher;

    /* loaded from: a.zip:com/android/launcher3/Stats$LaunchSourceProvider.class */
    public interface LaunchSourceProvider {
        void fillInLaunchSourceData(View view, Bundle bundle);
    }

    /* loaded from: a.zip:com/android/launcher3/Stats$LaunchSourceUtils.class */
    public static class LaunchSourceUtils {
        public static Bundle createSourceData() {
            Bundle bundle = new Bundle();
            bundle.putString("container", "homescreen");
            bundle.putInt("container_page", 0);
            bundle.putInt("sub_container_page", 0);
            return bundle;
        }

        public static void populateSourceDataFromAncestorProvider(View view, Bundle bundle) {
            LaunchSourceProvider launchSourceProvider;
            if (view == null) {
                return;
            }
            ViewParent parent = view.getParent();
            while (true) {
                ViewParent viewParent = parent;
                launchSourceProvider = null;
                if (viewParent == null) {
                    break;
                }
                launchSourceProvider = null;
                if (!(viewParent instanceof View)) {
                    break;
                } else if (viewParent instanceof LaunchSourceProvider) {
                    launchSourceProvider = (LaunchSourceProvider) viewParent;
                    break;
                } else {
                    parent = viewParent.getParent();
                }
            }
            if (launchSourceProvider != null) {
                launchSourceProvider.fillInLaunchSourceData(view, bundle);
            } else if (LauncherAppState.isDogfoodBuild()) {
                throw new RuntimeException("Expected LaunchSourceProvider");
            }
        }
    }

    public Stats(Launcher launcher) {
        this.mLauncher = launcher;
        this.mLaunchBroadcastPermission = launcher.getResources().getString(2131558402);
    }

    public void recordLaunch(View view, Intent intent, ShortcutInfo shortcutInfo) {
        if ("eng".equals(Build.TYPE)) {
            Intent intent2 = new Intent(intent);
            intent2.setSourceBounds(null);
            Intent putExtra = new Intent("com.android.launcher3.action.LAUNCH").putExtra("intent", intent2.toUri(0));
            if (shortcutInfo != null) {
                putExtra.putExtra("container", shortcutInfo.container).putExtra("screen", shortcutInfo.screenId).putExtra("cellX", shortcutInfo.cellX).putExtra("cellY", shortcutInfo.cellY);
            }
            Bundle createSourceData = LaunchSourceUtils.createSourceData();
            LaunchSourceUtils.populateSourceDataFromAncestorProvider(view, createSourceData);
            putExtra.putExtra("source", createSourceData);
            for (String str : this.mLauncher.getResources().getStringArray(2131623936)) {
                putExtra.setPackage(str);
                this.mLauncher.sendBroadcast(putExtra, this.mLaunchBroadcastPermission);
            }
        }
    }
}
