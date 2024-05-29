package com.mediatek.systemui.statusbar.networktype;

import android.telephony.ServiceState;
import android.util.Log;
import com.android.systemui.statusbar.policy.NetworkControllerImpl;
import java.util.HashMap;
import java.util.Map;
/* loaded from: a.zip:com/mediatek/systemui/statusbar/networktype/NetworkTypeUtils.class */
public class NetworkTypeUtils {
    static final Map<Integer, Integer> sNetworkTypeIcons = new HashMap<Integer, Integer>() { // from class: com.mediatek.systemui.statusbar.networktype.NetworkTypeUtils.1
        {
            put(5, 2130838284);
            put(6, 2130838284);
            put(12, 2130838284);
            put(14, 2130838284);
            put(4, 2130838283);
            put(7, 2130838283);
            put(2, 2130838286);
            put(3, 2130838284);
            put(13, 2130838285);
            put(8, 2130838284);
            put(9, 2130838284);
            put(10, 2130838284);
            put(15, 2130838284);
            put(18, 0);
        }
    };

    /* JADX WARN: Code restructure failed: missing block: B:5:0x000e, code lost:
        if (r4 == 139) goto L8;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static int getDataNetTypeFromServiceState(int i, ServiceState serviceState) {
        int i2;
        if (i != 13) {
            i2 = i;
        }
        i2 = i;
        if (serviceState != null) {
            i2 = serviceState.getProprietaryDataRadioTechnology() == 0 ? 13 : 139;
        }
        Log.d("NetworkTypeUtils", "getDataNetTypeFromServiceState:srcDataNetType = " + i + ", destDataNetType " + i2);
        return i2;
    }

    private static int getNetworkType(ServiceState serviceState) {
        int i = 0;
        if (serviceState != null) {
            i = serviceState.getDataNetworkType() != 0 ? serviceState.getDataNetworkType() : serviceState.getVoiceNetworkType();
        }
        Log.d("NetworkTypeUtils", "getNetworkType: type=" + i);
        return i;
    }

    public static int getNetworkTypeIcon(ServiceState serviceState, NetworkControllerImpl.Config config, boolean z) {
        int i = 0;
        if (z) {
            int networkType = getNetworkType(serviceState);
            Integer num = sNetworkTypeIcons.get(Integer.valueOf(networkType));
            Integer num2 = num;
            if (num == null) {
                if (networkType != 0) {
                    i = config.showAtLeast3G ? 2130838284 : 2130838287;
                }
                num2 = Integer.valueOf(i);
            }
            Log.d("NetworkTypeUtils", "getNetworkTypeIcon iconId = " + num2);
            return num2.intValue();
        }
        return 0;
    }
}
