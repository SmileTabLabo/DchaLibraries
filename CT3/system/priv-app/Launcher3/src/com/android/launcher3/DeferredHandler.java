package com.android.launcher3;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import java.util.LinkedList;
/* loaded from: a.zip:com/android/launcher3/DeferredHandler.class */
public class DeferredHandler {
    LinkedList<Runnable> mQueue = new LinkedList<>();
    private MessageQueue mMessageQueue = Looper.myQueue();
    private Impl mHandler = new Impl(this);

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/launcher3/DeferredHandler$IdleRunnable.class */
    public class IdleRunnable implements Runnable {
        Runnable mRunnable;
        final DeferredHandler this$0;

        IdleRunnable(DeferredHandler deferredHandler, Runnable runnable) {
            this.this$0 = deferredHandler;
            this.mRunnable = runnable;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.mRunnable.run();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:com/android/launcher3/DeferredHandler$Impl.class */
    public class Impl extends Handler implements MessageQueue.IdleHandler {
        final DeferredHandler this$0;

        Impl(DeferredHandler deferredHandler) {
            this.this$0 = deferredHandler;
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            synchronized (this.this$0.mQueue) {
                if (this.this$0.mQueue.size() == 0) {
                    return;
                }
                this.this$0.mQueue.removeFirst().run();
                synchronized (this.this$0.mQueue) {
                    this.this$0.scheduleNextLocked();
                }
            }
        }

        @Override // android.os.MessageQueue.IdleHandler
        public boolean queueIdle() {
            handleMessage(null);
            return false;
        }
    }

    public void cancelAll() {
        synchronized (this.mQueue) {
            this.mQueue.clear();
        }
    }

    public void flush() {
        LinkedList<Runnable> linkedList = new LinkedList();
        synchronized (this.mQueue) {
            linkedList.addAll(this.mQueue);
            this.mQueue.clear();
        }
        for (Runnable runnable : linkedList) {
            runnable.run();
        }
    }

    public void post(Runnable runnable) {
        synchronized (this.mQueue) {
            this.mQueue.add(runnable);
            if (this.mQueue.size() == 1) {
                scheduleNextLocked();
            }
        }
    }

    public void postIdle(Runnable runnable) {
        post(new IdleRunnable(this, runnable));
    }

    void scheduleNextLocked() {
        if (this.mQueue.size() > 0) {
            if (this.mQueue.getFirst() instanceof IdleRunnable) {
                this.mMessageQueue.addIdleHandler(this.mHandler);
            } else {
                this.mHandler.sendEmptyMessage(1);
            }
        }
    }
}
