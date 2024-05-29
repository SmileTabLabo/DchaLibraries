package com.android.quickstep;

import com.android.launcher3.util.LooperExecutor;
import com.android.launcher3.util.TraceHelper;
import com.android.launcher3.util.UiThreadHelper;
import com.android.quickstep.util.RemoteAnimationTargetSet;
import com.android.systemui.shared.system.RecentsAnimationControllerCompat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
/* loaded from: classes.dex */
public class RecentsAnimationWrapper {
    private RecentsAnimationControllerCompat mController;
    public RemoteAnimationTargetSet targetSet;
    private final ArrayList<Runnable> mCallbacks = new ArrayList<>();
    private boolean mInputConsumerEnabled = false;
    private boolean mBehindSystemBars = true;
    private boolean mSplitScreenMinimized = false;
    private final ExecutorService mExecutorService = new LooperExecutor(UiThreadHelper.getBackgroundLooper());

    public synchronized void setController(RecentsAnimationControllerCompat recentsAnimationControllerCompat, RemoteAnimationTargetSet remoteAnimationTargetSet) {
        TraceHelper.partitionSection("RecentsController", "Set controller " + recentsAnimationControllerCompat);
        this.mController = recentsAnimationControllerCompat;
        this.targetSet = remoteAnimationTargetSet;
        if (this.mInputConsumerEnabled) {
            enableInputConsumer();
        }
        if (!this.mCallbacks.isEmpty()) {
            Iterator it = new ArrayList(this.mCallbacks).iterator();
            while (it.hasNext()) {
                ((Runnable) it.next()).run();
            }
            this.mCallbacks.clear();
        }
    }

    public synchronized void runOnInit(Runnable runnable) {
        if (this.targetSet == null) {
            this.mCallbacks.add(runnable);
        } else {
            runnable.run();
        }
    }

    public void finish(final boolean z, final Runnable runnable) {
        this.mExecutorService.submit(new Runnable() { // from class: com.android.quickstep.-$$Lambda$RecentsAnimationWrapper$t0651d-J6Ex6rVOzuxBL-Fg45bs
            @Override // java.lang.Runnable
            public final void run() {
                RecentsAnimationWrapper.lambda$finish$0(RecentsAnimationWrapper.this, z, runnable);
            }
        });
    }

    public static /* synthetic */ void lambda$finish$0(RecentsAnimationWrapper recentsAnimationWrapper, boolean z, Runnable runnable) {
        RecentsAnimationControllerCompat recentsAnimationControllerCompat = recentsAnimationWrapper.mController;
        recentsAnimationWrapper.mController = null;
        TraceHelper.endSection("RecentsController", "Finish " + recentsAnimationControllerCompat + ", toHome=" + z);
        if (recentsAnimationControllerCompat != null) {
            recentsAnimationControllerCompat.setInputConsumerEnabled(false);
            recentsAnimationControllerCompat.finish(z);
            if (runnable != null) {
                runnable.run();
            }
        }
    }

    public void enableInputConsumer() {
        this.mInputConsumerEnabled = true;
        if (this.mInputConsumerEnabled) {
            this.mExecutorService.submit(new Runnable() { // from class: com.android.quickstep.-$$Lambda$RecentsAnimationWrapper$9c3qO5H-3-3OmkdTM70q4HUnQYM
                @Override // java.lang.Runnable
                public final void run() {
                    RecentsAnimationWrapper.lambda$enableInputConsumer$1(RecentsAnimationWrapper.this);
                }
            });
        }
    }

    public static /* synthetic */ void lambda$enableInputConsumer$1(RecentsAnimationWrapper recentsAnimationWrapper) {
        RecentsAnimationControllerCompat recentsAnimationControllerCompat = recentsAnimationWrapper.mController;
        TraceHelper.partitionSection("RecentsController", "Enabling consumer on " + recentsAnimationControllerCompat);
        if (recentsAnimationControllerCompat != null) {
            recentsAnimationControllerCompat.setInputConsumerEnabled(true);
        }
    }

    public void setAnimationTargetsBehindSystemBars(final boolean z) {
        if (this.mBehindSystemBars == z) {
            return;
        }
        this.mBehindSystemBars = z;
        this.mExecutorService.submit(new Runnable() { // from class: com.android.quickstep.-$$Lambda$RecentsAnimationWrapper$QrJiGACPfNMszQcu6iJp-4ImttA
            @Override // java.lang.Runnable
            public final void run() {
                RecentsAnimationWrapper.lambda$setAnimationTargetsBehindSystemBars$2(RecentsAnimationWrapper.this, z);
            }
        });
    }

    public static /* synthetic */ void lambda$setAnimationTargetsBehindSystemBars$2(RecentsAnimationWrapper recentsAnimationWrapper, boolean z) {
        RecentsAnimationControllerCompat recentsAnimationControllerCompat = recentsAnimationWrapper.mController;
        TraceHelper.partitionSection("RecentsController", "Setting behind system bars on " + recentsAnimationControllerCompat);
        if (recentsAnimationControllerCompat != null) {
            recentsAnimationControllerCompat.setAnimationTargetsBehindSystemBars(z);
        }
    }

    public void setSplitScreenMinimizedForTransaction(final boolean z) {
        if (this.mSplitScreenMinimized || !z) {
            return;
        }
        this.mSplitScreenMinimized = z;
        this.mExecutorService.submit(new Runnable() { // from class: com.android.quickstep.-$$Lambda$RecentsAnimationWrapper$ykLqYkc8Y_V0VQVp5BD2XcRvDC4
            @Override // java.lang.Runnable
            public final void run() {
                RecentsAnimationWrapper.lambda$setSplitScreenMinimizedForTransaction$3(RecentsAnimationWrapper.this, z);
            }
        });
    }

    public static /* synthetic */ void lambda$setSplitScreenMinimizedForTransaction$3(RecentsAnimationWrapper recentsAnimationWrapper, boolean z) {
        RecentsAnimationControllerCompat recentsAnimationControllerCompat = recentsAnimationWrapper.mController;
        TraceHelper.partitionSection("RecentsController", "Setting minimize dock on " + recentsAnimationControllerCompat);
        if (recentsAnimationControllerCompat != null) {
            recentsAnimationControllerCompat.setSplitScreenMinimized(z);
        }
    }

    public void hideCurrentInputMethod() {
        this.mExecutorService.submit(new Runnable() { // from class: com.android.quickstep.-$$Lambda$RecentsAnimationWrapper$MFk8IdJZdT6NsP_HIZZL9XUWA0c
            @Override // java.lang.Runnable
            public final void run() {
                RecentsAnimationWrapper.lambda$hideCurrentInputMethod$4(RecentsAnimationWrapper.this);
            }
        });
    }

    public static /* synthetic */ void lambda$hideCurrentInputMethod$4(RecentsAnimationWrapper recentsAnimationWrapper) {
        RecentsAnimationControllerCompat recentsAnimationControllerCompat = recentsAnimationWrapper.mController;
        TraceHelper.partitionSection("RecentsController", "Hiding currentinput method on " + recentsAnimationControllerCompat);
        if (recentsAnimationControllerCompat != null) {
            recentsAnimationControllerCompat.hideCurrentInputMethod();
        }
    }

    public RecentsAnimationControllerCompat getController() {
        return this.mController;
    }
}
