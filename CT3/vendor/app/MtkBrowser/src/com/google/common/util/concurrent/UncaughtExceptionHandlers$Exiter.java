package com.google.common.util.concurrent;

import com.google.common.annotations.VisibleForTesting;
import java.lang.Thread;
import java.util.logging.Level;
import java.util.logging.Logger;
@VisibleForTesting
/* loaded from: b.zip:com/google/common/util/concurrent/UncaughtExceptionHandlers$Exiter.class */
final class UncaughtExceptionHandlers$Exiter implements Thread.UncaughtExceptionHandler {
    private static final Logger logger = Logger.getLogger(UncaughtExceptionHandlers$Exiter.class.getName());
    private final Runtime runtime;

    @Override // java.lang.Thread.UncaughtExceptionHandler
    public void uncaughtException(Thread thread, Throwable th) {
        try {
            logger.log(Level.SEVERE, String.format("Caught an exception in %s.  Shutting down.", thread), th);
        } catch (Throwable th2) {
            try {
                System.err.println(th.getMessage());
                System.err.println(th2.getMessage());
            } finally {
                this.runtime.exit(1);
            }
        }
    }
}
