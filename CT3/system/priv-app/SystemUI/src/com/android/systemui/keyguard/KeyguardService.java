package com.android.systemui.keyguard;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
import android.os.IBinder;
import android.util.Log;
import com.android.internal.policy.IKeyguardDrawnCallback;
import com.android.internal.policy.IKeyguardExitCallback;
import com.android.internal.policy.IKeyguardService;
import com.android.internal.policy.IKeyguardStateCallback;
import com.android.systemui.SystemUIApplication;
/* loaded from: a.zip:com/android/systemui/keyguard/KeyguardService.class */
public class KeyguardService extends Service {
    private final IKeyguardService.Stub mBinder = new IKeyguardService.Stub(this) { // from class: com.android.systemui.keyguard.KeyguardService.1
        final KeyguardService this$0;

        {
            this.this$0 = this;
        }

        public void addStateMonitorCallback(IKeyguardStateCallback iKeyguardStateCallback) {
            this.this$0.checkPermission();
            this.this$0.mKeyguardViewMediator.addStateMonitorCallback(iKeyguardStateCallback);
        }

        public void dismiss() {
            this.this$0.checkPermission();
            this.this$0.mKeyguardViewMediator.dismiss();
        }

        public void doKeyguardTimeout(Bundle bundle) {
            this.this$0.checkPermission();
            this.this$0.mKeyguardViewMediator.doKeyguardTimeout(bundle);
        }

        public void keyguardDone(boolean z, boolean z2) {
            this.this$0.checkPermission();
            this.this$0.mKeyguardViewMediator.keyguardDone(z);
        }

        public void onActivityDrawn() {
            this.this$0.checkPermission();
            this.this$0.mKeyguardViewMediator.onActivityDrawn();
        }

        public void onBootCompleted() {
            this.this$0.checkPermission();
            this.this$0.mKeyguardViewMediator.onBootCompleted();
        }

        public void onDreamingStarted() {
            this.this$0.checkPermission();
            this.this$0.mKeyguardViewMediator.onDreamingStarted();
        }

        public void onDreamingStopped() {
            this.this$0.checkPermission();
            this.this$0.mKeyguardViewMediator.onDreamingStopped();
        }

        public void onFinishedGoingToSleep(int i, boolean z) {
            this.this$0.checkPermission();
            this.this$0.mKeyguardViewMediator.onFinishedGoingToSleep(i, z);
        }

        public void onScreenTurnedOff() {
            this.this$0.checkPermission();
            this.this$0.mKeyguardViewMediator.onScreenTurnedOff();
        }

        public void onScreenTurnedOn() {
            this.this$0.checkPermission();
            this.this$0.mKeyguardViewMediator.onScreenTurnedOn();
        }

        public void onScreenTurningOn(IKeyguardDrawnCallback iKeyguardDrawnCallback) {
            this.this$0.checkPermission();
            this.this$0.mKeyguardViewMediator.onScreenTurningOn(iKeyguardDrawnCallback);
        }

        public void onStartedGoingToSleep(int i) {
            this.this$0.checkPermission();
            this.this$0.mKeyguardViewMediator.onStartedGoingToSleep(i);
        }

        public void onStartedWakingUp() {
            this.this$0.checkPermission();
            this.this$0.mKeyguardViewMediator.onStartedWakingUp();
        }

        public void onSystemReady() {
            this.this$0.checkPermission();
            this.this$0.mKeyguardViewMediator.onSystemReady();
        }

        public void setCurrentUser(int i) {
            this.this$0.checkPermission();
            this.this$0.mKeyguardViewMediator.setCurrentUser(i);
        }

        public void setKeyguardEnabled(boolean z) {
            this.this$0.checkPermission();
            this.this$0.mKeyguardViewMediator.setKeyguardEnabled(z);
        }

        public void setOccluded(boolean z) {
            this.this$0.checkPermission();
            this.this$0.mKeyguardViewMediator.setOccluded(z);
        }

        public void startKeyguardExitAnimation(long j, long j2) {
            this.this$0.checkPermission();
            this.this$0.mKeyguardViewMediator.startKeyguardExitAnimation(j, j2);
        }

        public void verifyUnlock(IKeyguardExitCallback iKeyguardExitCallback) {
            this.this$0.checkPermission();
            this.this$0.mKeyguardViewMediator.verifyUnlock(iKeyguardExitCallback);
        }
    };
    private KeyguardViewMediator mKeyguardViewMediator;

    void checkPermission() {
        if (Binder.getCallingUid() == 1000 || getBaseContext().checkCallingOrSelfPermission("android.permission.CONTROL_KEYGUARD") == 0) {
            return;
        }
        Log.w("KeyguardService", "Caller needs permission 'android.permission.CONTROL_KEYGUARD' to call " + Debug.getCaller());
        throw new SecurityException("Access denied to process: " + Binder.getCallingPid() + ", must have permission android.permission.CONTROL_KEYGUARD");
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    @Override // android.app.Service
    public void onCreate() {
        ((SystemUIApplication) getApplication()).startServicesIfNeeded();
        this.mKeyguardViewMediator = (KeyguardViewMediator) ((SystemUIApplication) getApplication()).getComponent(KeyguardViewMediator.class);
    }
}
