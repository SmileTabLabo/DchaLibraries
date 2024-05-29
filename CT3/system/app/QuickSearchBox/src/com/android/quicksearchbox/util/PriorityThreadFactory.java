package com.android.quicksearchbox.util;

import android.os.Process;
import java.util.concurrent.ThreadFactory;
/* loaded from: a.zip:com/android/quicksearchbox/util/PriorityThreadFactory.class */
public class PriorityThreadFactory implements ThreadFactory {
    private final int mPriority;

    public PriorityThreadFactory(int i) {
        this.mPriority = i;
    }

    @Override // java.util.concurrent.ThreadFactory
    public Thread newThread(Runnable runnable) {
        return new Thread(this, runnable) { // from class: com.android.quicksearchbox.util.PriorityThreadFactory.1
            final PriorityThreadFactory this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                Process.setThreadPriority(this.this$0.mPriority);
                super.run();
            }
        };
    }
}
