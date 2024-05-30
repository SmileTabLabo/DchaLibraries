package com.android.quickstep;

import android.util.SparseArray;
/* loaded from: classes.dex */
public class MultiStateCallback {
    private final SparseArray<Runnable> mCallbacks = new SparseArray<>();
    private int mState = 0;

    public void setState(int i) {
        Runnable valueAt;
        this.mState = i | this.mState;
        int size = this.mCallbacks.size();
        for (int i2 = 0; i2 < size; i2++) {
            int keyAt = this.mCallbacks.keyAt(i2);
            if ((this.mState & keyAt) == keyAt && (valueAt = this.mCallbacks.valueAt(i2)) != null) {
                this.mCallbacks.setValueAt(i2, null);
                valueAt.run();
            }
        }
    }

    public void addCallback(int i, Runnable runnable) {
        this.mCallbacks.put(i, runnable);
    }

    public int getState() {
        return this.mState;
    }

    public boolean hasStates(int i) {
        return (this.mState & i) == i;
    }
}
