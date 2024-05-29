package com.mediatek.keyguard.PowerOffAlarm.multiwaveview;

import android.animation.TimeInterpolator;
/* loaded from: a.zip:com/mediatek/keyguard/PowerOffAlarm/multiwaveview/Ease$Cubic.class */
class Ease$Cubic {
    public static final TimeInterpolator easeIn = new TimeInterpolator() { // from class: com.mediatek.keyguard.PowerOffAlarm.multiwaveview.Ease$Cubic.1
        @Override // android.animation.TimeInterpolator
        public float getInterpolation(float f) {
            float f2 = f / 1.0f;
            return (1.0f * f2 * f2 * f2) + 0.0f;
        }
    };
    public static final TimeInterpolator easeOut = new TimeInterpolator() { // from class: com.mediatek.keyguard.PowerOffAlarm.multiwaveview.Ease$Cubic.2
        @Override // android.animation.TimeInterpolator
        public float getInterpolation(float f) {
            float f2 = (f / 1.0f) - 1.0f;
            return (((f2 * f2 * f2) + 1.0f) * 1.0f) + 0.0f;
        }
    };
    public static final TimeInterpolator easeInOut = new TimeInterpolator() { // from class: com.mediatek.keyguard.PowerOffAlarm.multiwaveview.Ease$Cubic.3
        @Override // android.animation.TimeInterpolator
        public float getInterpolation(float f) {
            float f2;
            float f3 = f / 0.5f;
            if (f3 < 1.0f) {
                f2 = (0.5f * f3 * f3 * f3) + 0.0f;
            } else {
                float f4 = f3 - 2.0f;
                f2 = (((f4 * f4 * f4) + 2.0f) * 0.5f) + 0.0f;
            }
            return f2;
        }
    };

    Ease$Cubic() {
    }
}
