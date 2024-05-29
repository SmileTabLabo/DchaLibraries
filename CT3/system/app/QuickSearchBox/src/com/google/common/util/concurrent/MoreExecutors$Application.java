package com.google.common.util.concurrent;

import com.google.common.annotations.VisibleForTesting;
@VisibleForTesting
/* loaded from: a.zip:com/google/common/util/concurrent/MoreExecutors$Application.class */
class MoreExecutors$Application {
    MoreExecutors$Application() {
    }

    @VisibleForTesting
    void addShutdownHook(Thread thread) {
        Runtime.getRuntime().addShutdownHook(thread);
    }
}
