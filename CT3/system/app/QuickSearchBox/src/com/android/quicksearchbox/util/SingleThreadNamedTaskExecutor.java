package com.android.quicksearchbox.util;

import android.util.Log;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
/* loaded from: a.zip:com/android/quicksearchbox/util/SingleThreadNamedTaskExecutor.class */
public class SingleThreadNamedTaskExecutor implements NamedTaskExecutor {
    private volatile boolean mClosed = false;
    private final LinkedBlockingQueue<NamedTask> mQueue = new LinkedBlockingQueue<>();
    private final Thread mWorker;

    /* loaded from: a.zip:com/android/quicksearchbox/util/SingleThreadNamedTaskExecutor$Worker.class */
    private class Worker implements Runnable {
        final SingleThreadNamedTaskExecutor this$0;

        private Worker(SingleThreadNamedTaskExecutor singleThreadNamedTaskExecutor) {
            this.this$0 = singleThreadNamedTaskExecutor;
        }

        /* synthetic */ Worker(SingleThreadNamedTaskExecutor singleThreadNamedTaskExecutor, Worker worker) {
            this(singleThreadNamedTaskExecutor);
        }

        private void loop() {
            Thread currentThread = Thread.currentThread();
            String name = currentThread.getName();
            while (!this.this$0.mClosed) {
                try {
                    NamedTask namedTask = (NamedTask) this.this$0.mQueue.take();
                    currentThread.setName(name + " " + namedTask.getName());
                    try {
                        namedTask.run();
                    } catch (RuntimeException e) {
                        Log.e("QSB.SingleThreadNamedTaskExecutor", "Task " + namedTask.getName() + " failed", e);
                    }
                } catch (InterruptedException e2) {
                }
            }
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                loop();
            } finally {
                if (!this.this$0.mClosed) {
                    Log.w("QSB.SingleThreadNamedTaskExecutor", "Worker exited before close");
                }
            }
        }
    }

    public SingleThreadNamedTaskExecutor(ThreadFactory threadFactory) {
        this.mWorker = threadFactory.newThread(new Worker(this, null));
        this.mWorker.start();
    }

    public static Factory<NamedTaskExecutor> factory(ThreadFactory threadFactory) {
        return new Factory<NamedTaskExecutor>(threadFactory) { // from class: com.android.quicksearchbox.util.SingleThreadNamedTaskExecutor.1
            final ThreadFactory val$threadFactory;

            {
                this.val$threadFactory = threadFactory;
            }

            /* JADX WARN: Can't rename method to resolve collision */
            @Override // com.android.quicksearchbox.util.Factory
            public NamedTaskExecutor create() {
                return new SingleThreadNamedTaskExecutor(this.val$threadFactory);
            }
        };
    }

    @Override // com.android.quicksearchbox.util.NamedTaskExecutor
    public void execute(NamedTask namedTask) {
        if (this.mClosed) {
            throw new IllegalStateException("execute() after close()");
        }
        this.mQueue.add(namedTask);
    }
}
