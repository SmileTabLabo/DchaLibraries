package com.android.systemui.doze;
/* loaded from: classes.dex */
public interface DozeHost {

    /* loaded from: classes.dex */
    public interface PulseCallback {
        void onPulseFinished();

        void onPulseStarted();
    }

    void addCallback(Callback callback);

    void dozeTimeTick();

    void extendPulse();

    boolean isBlockingDoze();

    boolean isPowerSaveActive();

    boolean isProvisioned();

    boolean isPulsingBlocked();

    void onDoubleTap(float f, float f2);

    void onIgnoreTouchWhilePulsing(boolean z);

    void pulseWhileDozing(PulseCallback pulseCallback, int i);

    void removeCallback(Callback callback);

    void setAnimateScreenOff(boolean z);

    void setAnimateWakeup(boolean z);

    void setDozeScreenBrightness(int i);

    void startDozing();

    void stopDozing();

    default void setAodDimmingScrim(float f) {
    }

    /* loaded from: classes.dex */
    public interface Callback {
        default void onNotificationHeadsUp() {
        }

        default void onPowerSaveChanged(boolean z) {
        }
    }
}
