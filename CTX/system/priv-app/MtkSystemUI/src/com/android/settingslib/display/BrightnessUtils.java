package com.android.settingslib.display;

import android.util.MathUtils;
/* loaded from: classes.dex */
public class BrightnessUtils {
    public static final int convertGammaToLinear(int i, int i2, int i3) {
        float exp;
        float norm = MathUtils.norm(0.0f, 1023.0f, i);
        if (norm <= 0.5f) {
            exp = MathUtils.sq(norm / 0.5f);
        } else {
            exp = MathUtils.exp((norm - 0.5599107f) / 0.17883277f) + 0.28466892f;
        }
        return Math.round(MathUtils.lerp(i2, i3, exp / 12.0f));
    }

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
