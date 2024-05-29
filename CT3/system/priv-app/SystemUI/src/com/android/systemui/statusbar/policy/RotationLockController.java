package com.android.systemui.statusbar.policy;
/* loaded from: a.zip:com/android/systemui/statusbar/policy/RotationLockController.class */
public interface RotationLockController extends Listenable {

    /* loaded from: a.zip:com/android/systemui/statusbar/policy/RotationLockController$RotationLockControllerCallback.class */
    public interface RotationLockControllerCallback {
        void onRotationLockStateChanged(boolean z, boolean z2);
    }

    void addRotationLockControllerCallback(RotationLockControllerCallback rotationLockControllerCallback);

    int getRotationLockOrientation();

    boolean isRotationLocked();

    void removeRotationLockControllerCallback(RotationLockControllerCallback rotationLockControllerCallback);

    void setRotationLocked(boolean z);
}
