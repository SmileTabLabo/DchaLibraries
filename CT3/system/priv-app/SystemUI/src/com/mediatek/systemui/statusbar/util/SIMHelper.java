package com.mediatek.systemui.statusbar.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.telephony.ITelephony;
import java.util.List;
/* loaded from: a.zip:com/mediatek/systemui/statusbar/util/SIMHelper.class */
public class SIMHelper {
    private static boolean bMtkHotKnotSupport = SystemProperties.get("ro.mtk_hotknot_support").equals("1");
    public static Context sContext;
    private static List<SubscriptionInfo> sSimInfos;

    private SIMHelper() {
    }

    public static int getFirstSubInSlot(int i) {
        int[] subId = SubscriptionManager.getSubId(i);
        if (subId == null || subId.length <= 0) {
            Log.d("SIMHelper", "Cannot get first sub in slot: " + i);
            return -1;
        }
        return subId[0];
    }

    public static int getSlotCount() {
        return TelephonyManager.getDefault().getPhoneCount();
    }

    public static final boolean isMtkHotKnotSupport() {
        Log.d("@M_SIMHelper", "isMtkHotKnotSupport, bMtkHotKnotSupport = " + bMtkHotKnotSupport);
        return bMtkHotKnotSupport;
    }

    public static boolean isRadioOn(int i) {
        ITelephony asInterface = ITelephony.Stub.asInterface(ServiceManager.getService("phone"));
        if (asInterface != null) {
            try {
                return asInterface.isRadioOnForSubscriber(i, sContext.getPackageName());
            } catch (RemoteException e) {
                Log.e("SIMHelper", "mTelephony exception");
                return false;
            }
        }
        return false;
    }

    public static boolean isWifiOnlyDevice() {
        boolean z = false;
        if (!((ConnectivityManager) sContext.getSystemService("connectivity")).isNetworkSupported(0)) {
            z = true;
        }
        return z;
    }

    public static void setContext(Context context) {
        sContext = context;
    }

    public static void updateSIMInfos(Context context) {
        sSimInfos = SubscriptionManager.from(context).getActiveSubscriptionInfoList();
    }
}
