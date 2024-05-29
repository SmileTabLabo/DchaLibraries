package com.android.systemui.statusbar.policy;
/* loaded from: a.zip:com/android/systemui/statusbar/policy/HotspotController.class */
public interface HotspotController {

    /* loaded from: a.zip:com/android/systemui/statusbar/policy/HotspotController$Callback.class */
    public interface Callback {
        void onHotspotChanged(boolean z);
    }

    void addCallback(Callback callback);

    boolean isHotspotEnabled();

    boolean isHotspotSupported();

    void removeCallback(Callback callback);

    void setHotspotEnabled(boolean z);
}
