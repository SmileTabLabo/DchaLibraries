package com.android.systemui.statusbar.policy;

import android.app.ActivityManager;
import android.content.Context;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.settings.CurrentUserTracker;
import java.util.concurrent.CopyOnWriteArrayList;
/* loaded from: a.zip:com/android/systemui/statusbar/policy/KeyguardMonitor.class */
public final class KeyguardMonitor extends KeyguardUpdateMonitorCallback {
    private final CopyOnWriteArrayList<Callback> mCallbacks = new CopyOnWriteArrayList<>();
    private boolean mCanSkipBouncer;
    private final Context mContext;
    private int mCurrentUser;
    private final KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private boolean mListening;
    private boolean mSecure;
    private boolean mShowing;
    private final CurrentUserTracker mUserTracker;

    /* loaded from: a.zip:com/android/systemui/statusbar/policy/KeyguardMonitor$Callback.class */
    public interface Callback {
        void onKeyguardChanged();
    }

    public KeyguardMonitor(Context context) {
        this.mContext = context;
        this.mKeyguardUpdateMonitor = KeyguardUpdateMonitor.getInstance(this.mContext);
        this.mUserTracker = new CurrentUserTracker(this, this.mContext) { // from class: com.android.systemui.statusbar.policy.KeyguardMonitor.1
            final KeyguardMonitor this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.systemui.settings.CurrentUserTracker
            public void onUserSwitched(int i) {
                this.this$0.mCurrentUser = i;
                this.this$0.updateCanSkipBouncerState();
            }
        };
    }

    private void notifyKeyguardChanged() {
        for (Callback callback : this.mCallbacks) {
            callback.onKeyguardChanged();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateCanSkipBouncerState() {
        this.mCanSkipBouncer = this.mKeyguardUpdateMonitor.getUserCanSkipBouncer(this.mCurrentUser);
    }

    public void addCallback(Callback callback) {
        this.mCallbacks.add(callback);
        if (this.mCallbacks.size() == 0 || this.mListening) {
            return;
        }
        this.mListening = true;
        this.mCurrentUser = ActivityManager.getCurrentUser();
        updateCanSkipBouncerState();
        this.mKeyguardUpdateMonitor.registerCallback(this);
        this.mUserTracker.startTracking();
    }

    public boolean canSkipBouncer() {
        return this.mCanSkipBouncer;
    }

    public boolean isSecure() {
        return this.mSecure;
    }

    public boolean isShowing() {
        return this.mShowing;
    }

    public void notifyKeyguardState(boolean z, boolean z2) {
        if (this.mShowing == z && this.mSecure == z2) {
            return;
        }
        this.mShowing = z;
        this.mSecure = z2;
        notifyKeyguardChanged();
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onTrustChanged(int i) {
        updateCanSkipBouncerState();
        notifyKeyguardChanged();
    }

    public void removeCallback(Callback callback) {
        if (this.mCallbacks.remove(callback) && this.mCallbacks.size() == 0 && this.mListening) {
            this.mListening = false;
            this.mKeyguardUpdateMonitor.removeCallback(this);
            this.mUserTracker.stopTracking();
        }
    }
}
