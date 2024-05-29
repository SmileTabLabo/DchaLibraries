package com.android.systemui.statusbar.phone;

import android.content.Context;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/UnlockMethodCache.class */
public class UnlockMethodCache {
    private static UnlockMethodCache sInstance;
    private boolean mCanSkipBouncer;
    private boolean mFaceUnlockRunning;
    private final KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private final LockPatternUtils mLockPatternUtils;
    private boolean mSecure;
    private boolean mTrustManaged;
    private boolean mTrusted;
    private final ArrayList<OnUnlockMethodChangedListener> mListeners = new ArrayList<>();
    private final KeyguardUpdateMonitorCallback mCallback = new KeyguardUpdateMonitorCallback(this) { // from class: com.android.systemui.statusbar.phone.UnlockMethodCache.1
        final UnlockMethodCache this$0;

        {
            this.this$0 = this;
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onFaceUnlockStateChanged(boolean z, int i) {
            this.this$0.update(false);
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onFingerprintAuthenticated(int i) {
            if (this.this$0.mKeyguardUpdateMonitor.isUnlockingWithFingerprintAllowed()) {
                this.this$0.update(false);
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onStartedWakingUp() {
            this.this$0.update(false);
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onStrongAuthStateChanged(int i) {
            this.this$0.update(false);
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onTrustChanged(int i) {
            this.this$0.update(false);
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onTrustManagedChanged(int i) {
            this.this$0.update(false);
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onUserSwitchComplete(int i) {
            this.this$0.update(false);
        }
    };

    /* loaded from: a.zip:com/android/systemui/statusbar/phone/UnlockMethodCache$OnUnlockMethodChangedListener.class */
    public interface OnUnlockMethodChangedListener {
        void onUnlockMethodStateChanged();
    }

    private UnlockMethodCache(Context context) {
        this.mLockPatternUtils = new LockPatternUtils(context);
        this.mKeyguardUpdateMonitor = KeyguardUpdateMonitor.getInstance(context);
        KeyguardUpdateMonitor.getInstance(context).registerCallback(this.mCallback);
        update(true);
    }

    public static UnlockMethodCache getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new UnlockMethodCache(context);
        }
        return sInstance;
    }

    private void notifyListeners() {
        for (OnUnlockMethodChangedListener onUnlockMethodChangedListener : this.mListeners) {
            onUnlockMethodChangedListener.onUnlockMethodStateChanged();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void update(boolean z) {
        int currentUser = KeyguardUpdateMonitor.getCurrentUser();
        boolean isSecure = this.mLockPatternUtils.isSecure(currentUser);
        boolean userCanSkipBouncer = isSecure ? this.mKeyguardUpdateMonitor.getUserCanSkipBouncer(currentUser) : true;
        boolean userTrustIsManaged = this.mKeyguardUpdateMonitor.getUserTrustIsManaged(currentUser);
        boolean userHasTrust = this.mKeyguardUpdateMonitor.getUserHasTrust(currentUser);
        boolean z2 = this.mKeyguardUpdateMonitor.isFaceUnlockRunning(currentUser) ? userTrustIsManaged : false;
        boolean z3 = true;
        if (isSecure == this.mSecure) {
            if (userCanSkipBouncer != this.mCanSkipBouncer) {
                z3 = true;
            } else {
                z3 = true;
                if (userTrustIsManaged == this.mTrustManaged) {
                    z3 = true;
                    if (z2 == this.mFaceUnlockRunning) {
                        z3 = false;
                    }
                }
            }
        }
        if (z3 || z) {
            this.mSecure = isSecure;
            this.mCanSkipBouncer = userCanSkipBouncer;
            this.mTrusted = userHasTrust;
            this.mTrustManaged = userTrustIsManaged;
            this.mFaceUnlockRunning = z2;
            notifyListeners();
        }
    }

    public void addListener(OnUnlockMethodChangedListener onUnlockMethodChangedListener) {
        this.mListeners.add(onUnlockMethodChangedListener);
    }

    public boolean canSkipBouncer() {
        return this.mCanSkipBouncer;
    }

    public boolean isFaceUnlockRunning() {
        return this.mFaceUnlockRunning;
    }

    public boolean isMethodSecure() {
        return this.mSecure;
    }

    public boolean isTrustManaged() {
        return this.mTrustManaged;
    }

    public boolean isTrusted() {
        return this.mTrusted;
    }
}
