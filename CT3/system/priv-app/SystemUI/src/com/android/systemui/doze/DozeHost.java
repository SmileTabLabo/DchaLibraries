package com.android.systemui.doze;
/* loaded from: a.zip:com/android/systemui/doze/DozeHost.class */
public interface DozeHost {

    /* loaded from: a.zip:com/android/systemui/doze/DozeHost$Callback.class */
    public interface Callback {
        void onBuzzBeepBlinked();

        void onNewNotifications();

        void onNotificationLight(boolean z);

        void onPowerSaveChanged(boolean z);
    }

    /* loaded from: a.zip:com/android/systemui/doze/DozeHost$PulseCallback.class */
    public interface PulseCallback {
        void onPulseFinished();

        void onPulseStarted();
    }

    void addCallback(Callback callback);

    boolean isNotificationLightOn();

    boolean isPowerSaveActive();

    boolean isPulsingBlocked();

    void pulseWhileDozing(PulseCallback pulseCallback, int i);

    void removeCallback(Callback callback);

    void startDozing(Runnable runnable);

    void stopDozing();
}
