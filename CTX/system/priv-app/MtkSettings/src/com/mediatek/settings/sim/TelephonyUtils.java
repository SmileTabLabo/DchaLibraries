package com.mediatek.settings.sim;

import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import com.android.internal.telephony.ITelephony;
import com.mediatek.internal.telephony.IMtkTelephonyEx;
import com.mediatek.internal.telephony.RadioCapabilitySwitchUtil;
/* loaded from: classes.dex */
public class TelephonyUtils {
    private static boolean DBG = SystemProperties.get("ro.build.type").equals("eng");

    public static boolean isAirplaneModeOn(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), "airplane_mode_on", 0) != 0;
    }

    public static boolean isRadioOn(int i, Context context) {
        ITelephony asInterface = ITelephony.Stub.asInterface(ServiceManager.getService("phone"));
        boolean z = false;
        if (asInterface != null) {
            if (i != -1) {
                try {
                    z = asInterface.isRadioOnForSubscriber(i, context.getPackageName());
                } catch (RemoteException e) {
                    Log.e("TelephonyUtils", "isRadioOn, RemoteException=" + e);
                }
            }
        } else {
            Log.e("TelephonyUtils", "isRadioOn, ITelephony is null.");
        }
        log("isRadioOn=" + z + ", subId=" + i);
        return z;
    }

    public static boolean isCapabilitySwitching() {
        boolean isCapabilitySwitching;
        IMtkTelephonyEx asInterface = IMtkTelephonyEx.Stub.asInterface(ServiceManager.getService("phoneEx"));
        if (asInterface != null) {
            try {
                isCapabilitySwitching = asInterface.isCapabilitySwitching();
            } catch (RemoteException e) {
                Log.e("TelephonyUtils", "isCapabilitySwitching, RemoteException=" + e);
            }
            log("isSwitching=" + isCapabilitySwitching);
            return isCapabilitySwitching;
        }
        log("isCapabilitySwitching, IMtkTelephonyEx service not ready.");
        isCapabilitySwitching = false;
        log("isSwitching=" + isCapabilitySwitching);
        return isCapabilitySwitching;
    }

    private static void log(String str) {
        if (DBG) {
            Log.d("TelephonyUtils", str);
        }
    }

    public static int getMainCapabilityPhoneId() {
        IMtkTelephonyEx asInterface = IMtkTelephonyEx.Stub.asInterface(ServiceManager.getService("phoneEx"));
        if (asInterface != null) {
            try {
                return asInterface.getMainCapabilityPhoneId();
            } catch (RemoteException e) {
                log("getMainCapabilityPhoneId, RemoteException=" + e);
                return -1;
            }
        }
        log("IMtkTelephonyEx service not ready.");
        return RadioCapabilitySwitchUtil.getMainCapabilityPhoneId();
    }
}
