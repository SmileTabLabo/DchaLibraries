package com.android.systemui.statusbar;
/* loaded from: classes.dex */
public class StatusBarState {
    public static String toShortString(int i) {
        switch (i) {
            case 0:
                return "SHD";
            case 1:
                return "KGRD";
            case 2:
                return "SHD_LCK";
            case 3:
                return "FS_USRSW";
            default:
                return "bad_value_" + i;
        }
    }
}
