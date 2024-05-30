package com.android.systemui.shared.system;

import android.os.RemoteException;
import android.util.Log;
import android.view.IRemoteAnimationFinishedCallback;
import android.view.IRemoteAnimationRunner;
import android.view.RemoteAnimationAdapter;
import android.view.RemoteAnimationTarget;
/* loaded from: classes.dex */
public class RemoteAnimationAdapterCompat {
    private final RemoteAnimationAdapter mWrapped;

    public RemoteAnimationAdapterCompat(RemoteAnimationRunnerCompat runner, long duration, long statusBarTransitionDelay) {
        this.mWrapped = new RemoteAnimationAdapter(wrapRemoteAnimationRunner(runner), duration, statusBarTransitionDelay);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public RemoteAnimationAdapter getWrapped() {
        return this.mWrapped;
    }

    private static IRemoteAnimationRunner.Stub wrapRemoteAnimationRunner(final RemoteAnimationRunnerCompat remoteAnimationAdapter) {
        return new IRemoteAnimationRunner.Stub() { // from class: com.android.systemui.shared.system.RemoteAnimationAdapterCompat.1
            public void onAnimationStart(RemoteAnimationTarget[] apps, final IRemoteAnimationFinishedCallback finishedCallback) throws RemoteException {
                RemoteAnimationTargetCompat[] appsCompat = RemoteAnimationTargetCompat.wrap(apps);
                Runnable animationFinishedCallback = new Runnable() { // from class: com.android.systemui.shared.system.RemoteAnimationAdapterCompat.1.1
                    @Override // java.lang.Runnable
                    public void run() {
                        try {
                            finishedCallback.onAnimationFinished();
                        } catch (RemoteException e) {
                            Log.e("ActivityOptionsCompat", "Failed to call app controlled animation finished callback", e);
                        }
                    }
                };
                RemoteAnimationRunnerCompat.this.onAnimationStart(appsCompat, animationFinishedCallback);
            }

            public void onAnimationCancelled() throws RemoteException {
                RemoteAnimationRunnerCompat.this.onAnimationCancelled();
            }
        };
    }
}
