package com.android.systemui;

import android.os.Handler;
import android.os.Looper;
import android.view.Choreographer;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/systemui/DejankUtils.class */
public class DejankUtils {
    private static final Choreographer sChoreographer = Choreographer.getInstance();
    private static final Handler sHandler = new Handler();
    private static final ArrayList<Runnable> sPendingRunnables = new ArrayList<>();
    private static final Runnable sAnimationCallbackRunnable = new Runnable() { // from class: com.android.systemui.DejankUtils.1
        @Override // java.lang.Runnable
        public void run() {
            for (int i = 0; i < DejankUtils.sPendingRunnables.size(); i++) {
                DejankUtils.sHandler.post((Runnable) DejankUtils.sPendingRunnables.get(i));
            }
            DejankUtils.sPendingRunnables.clear();
        }
    };

    public static void postAfterTraversal(Runnable runnable) {
        throwIfNotCalledOnMainThread();
        sPendingRunnables.add(runnable);
        postAnimationCallback();
    }

    private static void postAnimationCallback() {
        sChoreographer.postCallback(1, sAnimationCallbackRunnable, null);
    }

    public static void removeCallbacks(Runnable runnable) {
        throwIfNotCalledOnMainThread();
        sPendingRunnables.remove(runnable);
        sHandler.removeCallbacks(runnable);
    }

    private static void throwIfNotCalledOnMainThread() {
        if (!Looper.getMainLooper().isCurrentThread()) {
            throw new IllegalStateException("should be called from the main thread.");
        }
    }
}
