package com.android.systemui.recents.misc;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/systemui/recents/misc/ReferenceCountedTrigger.class */
public class ReferenceCountedTrigger {
    int mCount;
    Runnable mDecrementRunnable;
    Runnable mErrorRunnable;
    ArrayList<Runnable> mFirstIncRunnables;
    Runnable mIncrementRunnable;
    ArrayList<Runnable> mLastDecRunnables;

    public ReferenceCountedTrigger() {
        this(null, null, null);
    }

    public ReferenceCountedTrigger(Runnable runnable, Runnable runnable2, Runnable runnable3) {
        this.mFirstIncRunnables = new ArrayList<>();
        this.mLastDecRunnables = new ArrayList<>();
        this.mIncrementRunnable = new Runnable(this) { // from class: com.android.systemui.recents.misc.ReferenceCountedTrigger.1
            final ReferenceCountedTrigger this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.increment();
            }
        };
        this.mDecrementRunnable = new Runnable(this) { // from class: com.android.systemui.recents.misc.ReferenceCountedTrigger.2
            final ReferenceCountedTrigger this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.decrement();
            }
        };
        if (runnable != null) {
            this.mFirstIncRunnables.add(runnable);
        }
        if (runnable2 != null) {
            this.mLastDecRunnables.add(runnable2);
        }
        this.mErrorRunnable = runnable3;
    }

    public void addLastDecrementRunnable(Runnable runnable) {
        this.mLastDecRunnables.add(runnable);
    }

    public void decrement() {
        this.mCount--;
        if (this.mCount == 0) {
            flushLastDecrementRunnables();
        } else if (this.mCount < 0) {
            if (this.mErrorRunnable == null) {
                throw new RuntimeException("Invalid ref count");
            }
            this.mErrorRunnable.run();
        }
    }

    public Animator.AnimatorListener decrementOnAnimationEnd() {
        return new AnimatorListenerAdapter(this) { // from class: com.android.systemui.recents.misc.ReferenceCountedTrigger.3
            private boolean hasEnded;
            final ReferenceCountedTrigger this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (this.hasEnded) {
                    return;
                }
                this.this$0.decrement();
                this.hasEnded = true;
            }
        };
    }

    public void flushLastDecrementRunnables() {
        if (!this.mLastDecRunnables.isEmpty()) {
            int size = this.mLastDecRunnables.size();
            for (int i = 0; i < size; i++) {
                this.mLastDecRunnables.get(i).run();
            }
        }
        this.mLastDecRunnables.clear();
    }

    public void increment() {
        if (this.mCount == 0 && !this.mFirstIncRunnables.isEmpty()) {
            int size = this.mFirstIncRunnables.size();
            for (int i = 0; i < size; i++) {
                this.mFirstIncRunnables.get(i).run();
            }
        }
        this.mCount++;
    }
}
