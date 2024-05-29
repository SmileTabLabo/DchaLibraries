package com.android.keyguard;
/* loaded from: a.zip:com/android/keyguard/KeyguardSecurityCallback.class */
public interface KeyguardSecurityCallback {
    void dismiss(boolean z);

    void reportUnlockAttempt(int i, boolean z, int i2);

    void reset();

    void userActivity();
}
