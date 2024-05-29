package com.mediatek.systemui.statusbar.util;

import android.os.SystemProperties;
/* loaded from: a.zip:com/mediatek/systemui/statusbar/util/FeatureOptions.class */
public class FeatureOptions {
    public static final boolean LOW_RAM_SUPPORT = isPropertyEnabledBoolean("ro.config.low_ram");
    public static final boolean MTK_CTA_SET = isPropertyEnabledInt("ro.mtk_cta_set");
    public static final boolean MTK_A1_SUPPORT = isPropertyEnabledInt("ro.mtk_a1_feature");

    private static boolean isPropertyEnabledBoolean(String str) {
        return "true".equals(SystemProperties.get(str, "true"));
    }

    private static boolean isPropertyEnabledInt(String str) {
        return "1".equals(SystemProperties.get(str));
    }
}
