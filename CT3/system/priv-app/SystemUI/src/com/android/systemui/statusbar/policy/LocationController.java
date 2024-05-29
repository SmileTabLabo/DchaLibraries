package com.android.systemui.statusbar.policy;
/* loaded from: a.zip:com/android/systemui/statusbar/policy/LocationController.class */
public interface LocationController {

    /* loaded from: a.zip:com/android/systemui/statusbar/policy/LocationController$LocationSettingsChangeCallback.class */
    public interface LocationSettingsChangeCallback {
        void onLocationSettingsChanged(boolean z);
    }

    void addSettingsChangedCallback(LocationSettingsChangeCallback locationSettingsChangeCallback);

    boolean isLocationEnabled();

    void removeSettingsChangedCallback(LocationSettingsChangeCallback locationSettingsChangeCallback);

    boolean setLocationEnabled(boolean z);
}
