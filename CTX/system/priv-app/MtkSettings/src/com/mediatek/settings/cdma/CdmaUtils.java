package com.mediatek.settings.cdma;

import android.content.Context;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.mediatek.telephony.MtkTelephonyManagerEx;
/* loaded from: classes.dex */
public class CdmaUtils {
    public static boolean isSupportCdma(int i) {
        boolean z;
        if (TelephonyManager.getDefault().getCurrentPhoneType(i) == 2) {
            z = true;
        } else {
            z = false;
        }
        Log.d("CdmaUtils", "isSupportCdma=" + z + ", subId=" + i);
        return z;
    }

    public static boolean isCdmaCard(int i) {
        int simType = getSimType(i);
        boolean z = simType == 2 || simType == 3;
        Log.d("CdmaUtils", "isCdmaCard, simType=" + simType + ", isCdma=" + z);
        return z;
    }

    public static int getSimType(int i) {
        int i2;
        MtkTelephonyManagerEx mtkTelephonyManagerEx = MtkTelephonyManagerEx.getDefault();
        if (mtkTelephonyManagerEx != null) {
            i2 = mtkTelephonyManagerEx.getIccAppFamily(i);
        } else {
            i2 = 0;
        }
        Log.d("CdmaUtils", "simType=" + i2 + ", slotId=" + i);
        return i2;
    }

    public static boolean isCdmaCardCompetion(Context context) {
        int i;
        boolean z;
        boolean z2;
        if (context != null) {
            i = TelephonyManager.from(context).getSimCount();
        } else {
            i = 0;
        }
        if (i == 2) {
            int i2 = 0;
            z = true;
            z2 = true;
            while (true) {
                if (i2 < i) {
                    z = z && isCdmaCard(i2);
                    SubscriptionInfo activeSubscriptionInfoForSimSlotIndex = SubscriptionManager.from(context).getActiveSubscriptionInfoForSimSlotIndex(i2);
                    if (activeSubscriptionInfoForSimSlotIndex != null) {
                        z2 = z2 && MtkTelephonyManagerEx.getDefault().isInHomeNetwork(activeSubscriptionInfoForSimSlotIndex.getSubscriptionId());
                        i2++;
                    } else {
                        z2 = false;
                        break;
                    }
                } else {
                    break;
                }
            }
        } else {
            z = false;
            z2 = false;
        }
        Log.d("CdmaUtils", "isCdma=" + z + ", isCompletition=" + z2);
        return z && z2;
    }

    public static boolean isCdmaCardCompetionForData(Context context) {
        return isCdmaCardCompetion(context);
    }
}
