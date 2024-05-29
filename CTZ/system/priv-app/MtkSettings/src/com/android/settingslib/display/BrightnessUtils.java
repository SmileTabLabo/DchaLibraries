package com.android.settingslib.display;

import android.util.MathUtils;
/* loaded from: classes.dex */
public class BrightnessUtils {
    public static final int convertLinearToGamma(int i, int i2, int i3) {
        float log;
        float norm = MathUtils.norm(i2, i3, i) * 12.0f;
        if (norm <= 1.0f) {
            log = MathUtils.sqrt(norm) * 0.5f;
        } else {
            log = 0.5599107f + (0.17883277f * MathUtils.log(norm - 0.28466892f));
        }
        return Math.round(MathUtils.lerp(0.0f, 1023.0f, log));
    }
}
