package com.google.common.util.concurrent;

import java.lang.Thread;
import java.util.logging.Level;
import java.util.logging.Logger;
/* loaded from: classes.dex */
public final class UncaughtExceptionHandlers {

    /* loaded from: classes.dex */
    static final class Exiter implements Thread.UncaughtExceptionHandler {
        private static final Logger logger = Logger.getLogger(Exiter.class.getName());
        private final Runtime runtime;

        @Override // java.lang.Thread.UncaughtExceptionHandler
        public void uncaughtException(Thread thread, Throwable th) {
            try {
                logger.log(Level.SEVERE, String.format("Caught an exception in %s.  Shutting down.", thread), th);
            } finally {
                try {
                } finally {
                }
            }
        }
    }
}
