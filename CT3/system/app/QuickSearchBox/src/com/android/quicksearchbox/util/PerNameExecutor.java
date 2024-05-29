package com.android.quicksearchbox.util;

import java.util.HashMap;
/* loaded from: a.zip:com/android/quicksearchbox/util/PerNameExecutor.class */
public class PerNameExecutor implements NamedTaskExecutor {
    private final Factory<NamedTaskExecutor> mExecutorFactory;
    private HashMap<String, NamedTaskExecutor> mExecutors;

    public PerNameExecutor(Factory<NamedTaskExecutor> factory) {
        this.mExecutorFactory = factory;
    }

    @Override // com.android.quicksearchbox.util.NamedTaskExecutor
    public void execute(NamedTask namedTask) {
        synchronized (this) {
            if (this.mExecutors == null) {
                this.mExecutors = new HashMap<>();
            }
            String name = namedTask.getName();
            NamedTaskExecutor namedTaskExecutor = this.mExecutors.get(name);
            NamedTaskExecutor namedTaskExecutor2 = namedTaskExecutor;
            if (namedTaskExecutor == null) {
                namedTaskExecutor2 = this.mExecutorFactory.create();
                this.mExecutors.put(name, namedTaskExecutor2);
            }
            namedTaskExecutor2.execute(namedTask);
        }
    }
}
