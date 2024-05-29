package com.google.common.util.concurrent;

import com.google.common.base.Preconditions;
import java.lang.Thread;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
/* loaded from: a.zip:com/google/common/util/concurrent/ThreadFactoryBuilder.class */
public final class ThreadFactoryBuilder {
    private String nameFormat = null;
    private Boolean daemon = null;
    private Integer priority = null;
    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler = null;
    private ThreadFactory backingThreadFactory = null;

    private static ThreadFactory build(ThreadFactoryBuilder threadFactoryBuilder) {
        String str = threadFactoryBuilder.nameFormat;
        return new ThreadFactory(threadFactoryBuilder.backingThreadFactory != null ? threadFactoryBuilder.backingThreadFactory : Executors.defaultThreadFactory(), str, str != null ? new AtomicLong(0L) : null, threadFactoryBuilder.daemon, threadFactoryBuilder.priority, threadFactoryBuilder.uncaughtExceptionHandler) { // from class: com.google.common.util.concurrent.ThreadFactoryBuilder.1
            final ThreadFactory val$backingThreadFactory;
            final AtomicLong val$count;
            final Boolean val$daemon;
            final String val$nameFormat;
            final Integer val$priority;
            final Thread.UncaughtExceptionHandler val$uncaughtExceptionHandler;

            {
                this.val$backingThreadFactory = r4;
                this.val$nameFormat = str;
                this.val$count = r6;
                this.val$daemon = r7;
                this.val$priority = r8;
                this.val$uncaughtExceptionHandler = r9;
            }

            @Override // java.util.concurrent.ThreadFactory
            public Thread newThread(Runnable runnable) {
                Thread newThread = this.val$backingThreadFactory.newThread(runnable);
                if (this.val$nameFormat != null) {
                    newThread.setName(String.format(this.val$nameFormat, Long.valueOf(this.val$count.getAndIncrement())));
                }
                if (this.val$daemon != null) {
                    newThread.setDaemon(this.val$daemon.booleanValue());
                }
                if (this.val$priority != null) {
                    newThread.setPriority(this.val$priority.intValue());
                }
                if (this.val$uncaughtExceptionHandler != null) {
                    newThread.setUncaughtExceptionHandler(this.val$uncaughtExceptionHandler);
                }
                return newThread;
            }
        };
    }

    public ThreadFactory build() {
        return build(this);
    }

    public ThreadFactoryBuilder setNameFormat(String str) {
        String.format(str, 0);
        this.nameFormat = str;
        return this;
    }

    public ThreadFactoryBuilder setThreadFactory(ThreadFactory threadFactory) {
        this.backingThreadFactory = (ThreadFactory) Preconditions.checkNotNull(threadFactory);
        return this;
    }
}
