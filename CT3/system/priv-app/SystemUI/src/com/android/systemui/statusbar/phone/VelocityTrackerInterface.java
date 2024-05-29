package com.android.systemui.statusbar.phone;

import android.view.MotionEvent;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/VelocityTrackerInterface.class */
public interface VelocityTrackerInterface {
    void addMovement(MotionEvent motionEvent);

    void computeCurrentVelocity(int i);

    float getXVelocity();

    float getYVelocity();

    void recycle();
}
