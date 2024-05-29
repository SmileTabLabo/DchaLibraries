package com.android.keyguard;

import com.android.internal.widget.LockPatternUtils;
/* loaded from: a.zip:com/android/keyguard/KeyguardSecurityView.class */
public interface KeyguardSecurityView {
    boolean needsInput();

    void onPause();

    void onResume(int i);

    void setKeyguardCallback(KeyguardSecurityCallback keyguardSecurityCallback);

    void setLockPatternUtils(LockPatternUtils lockPatternUtils);

    void showMessage(String str, int i);

    void showPromptReason(int i);

    void startAppearAnimation();

    boolean startDisappearAnimation(Runnable runnable);
}
