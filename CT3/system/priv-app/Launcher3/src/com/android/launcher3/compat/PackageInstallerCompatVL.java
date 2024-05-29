package com.android.launcher3.compat;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageInstaller;
import android.os.Handler;
import android.util.SparseArray;
import com.android.launcher3.IconCache;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherModel;
import com.android.launcher3.compat.PackageInstallerCompat;
import java.util.HashMap;
@TargetApi(21)
/* loaded from: a.zip:com/android/launcher3/compat/PackageInstallerCompatVL.class */
public class PackageInstallerCompatVL extends PackageInstallerCompat {
    final PackageInstaller mInstaller;
    final SparseArray<String> mActiveSessions = new SparseArray<>();
    private final PackageInstaller.SessionCallback mCallback = new PackageInstaller.SessionCallback(this) { // from class: com.android.launcher3.compat.PackageInstallerCompatVL.1
        final PackageInstallerCompatVL this$0;

        {
            this.this$0 = this;
        }

        private void pushSessionDisplayToLauncher(int i) {
            PackageInstaller.SessionInfo sessionInfo = this.this$0.mInstaller.getSessionInfo(i);
            if (sessionInfo == null || sessionInfo.getAppPackageName() == null) {
                return;
            }
            this.this$0.addSessionInfoToCahce(sessionInfo, UserHandleCompat.myUserHandle());
            LauncherAppState instanceNoCreate = LauncherAppState.getInstanceNoCreate();
            if (instanceNoCreate != null) {
                instanceNoCreate.getModel().updateSessionDisplayInfo(sessionInfo.getAppPackageName());
            }
        }

        @Override // android.content.pm.PackageInstaller.SessionCallback
        public void onActiveChanged(int i, boolean z) {
        }

        @Override // android.content.pm.PackageInstaller.SessionCallback
        public void onBadgingChanged(int i) {
            pushSessionDisplayToLauncher(i);
        }

        @Override // android.content.pm.PackageInstaller.SessionCallback
        public void onCreated(int i) {
            pushSessionDisplayToLauncher(i);
        }

        @Override // android.content.pm.PackageInstaller.SessionCallback
        public void onFinished(int i, boolean z) {
            String str = this.this$0.mActiveSessions.get(i);
            this.this$0.mActiveSessions.remove(i);
            if (str != null) {
                this.this$0.sendUpdate(new PackageInstallerCompat.PackageInstallInfo(str, z ? 0 : 2, 0));
            }
        }

        @Override // android.content.pm.PackageInstaller.SessionCallback
        public void onProgressChanged(int i, float f) {
            PackageInstaller.SessionInfo sessionInfo = this.this$0.mInstaller.getSessionInfo(i);
            if (sessionInfo == null || sessionInfo.getAppPackageName() == null) {
                return;
            }
            this.this$0.sendUpdate(new PackageInstallerCompat.PackageInstallInfo(sessionInfo.getAppPackageName(), 1, (int) (sessionInfo.getProgress() * 100.0f)));
        }
    };
    private final IconCache mCache = LauncherAppState.getInstance().getIconCache();
    private final Handler mWorker = new Handler(LauncherModel.getWorkerLooper());

    /* JADX INFO: Access modifiers changed from: package-private */
    public PackageInstallerCompatVL(Context context) {
        this.mInstaller = context.getPackageManager().getPackageInstaller();
        this.mInstaller.registerSessionCallback(this.mCallback, this.mWorker);
    }

    void addSessionInfoToCahce(PackageInstaller.SessionInfo sessionInfo, UserHandleCompat userHandleCompat) {
        String appPackageName = sessionInfo.getAppPackageName();
        if (appPackageName != null) {
            this.mCache.cachePackageInstallInfo(appPackageName, userHandleCompat, sessionInfo.getAppIcon(), sessionInfo.getAppLabel());
        }
    }

    @Override // com.android.launcher3.compat.PackageInstallerCompat
    public void onStop() {
        this.mInstaller.unregisterSessionCallback(this.mCallback);
    }

    void sendUpdate(PackageInstallerCompat.PackageInstallInfo packageInstallInfo) {
        LauncherAppState instanceNoCreate = LauncherAppState.getInstanceNoCreate();
        if (instanceNoCreate != null) {
            instanceNoCreate.getModel().setPackageState(packageInstallInfo);
        }
    }

    @Override // com.android.launcher3.compat.PackageInstallerCompat
    public HashMap<String, Integer> updateAndGetActiveSessionCache() {
        HashMap<String, Integer> hashMap = new HashMap<>();
        UserHandleCompat myUserHandle = UserHandleCompat.myUserHandle();
        for (PackageInstaller.SessionInfo sessionInfo : this.mInstaller.getAllSessions()) {
            addSessionInfoToCahce(sessionInfo, myUserHandle);
            if (sessionInfo.getAppPackageName() != null) {
                hashMap.put(sessionInfo.getAppPackageName(), Integer.valueOf((int) (sessionInfo.getProgress() * 100.0f)));
                this.mActiveSessions.put(sessionInfo.getSessionId(), sessionInfo.getAppPackageName());
            }
        }
        return hashMap;
    }
}
