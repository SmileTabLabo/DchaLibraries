package com.google.common.util.concurrent;
/* loaded from: classes.dex */
public final class MoreExecutors {

    /* loaded from: classes.dex */
    static class Application {
        Application() {
        }

        void addShutdownHook(Thread thread) {
            Runtime.getRuntime().addShutdownHook(thread);
        }
    }
}
