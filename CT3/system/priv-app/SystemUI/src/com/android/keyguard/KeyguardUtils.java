package com.android.keyguard;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.SystemProperties;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
/* loaded from: a.zip:com/android/keyguard/KeyguardUtils.class */
public class KeyguardUtils {
    private SubscriptionManager mSubscriptionManager;
    private KeyguardUpdateMonitor mUpdateMonitor;
    private static final boolean mIsOwnerSdcardOnlySupport = SystemProperties.get("ro.mtk_owner_sdcard_support").equals("1");
    private static final boolean mIsPrivacyProtectionLockSupport = SystemProperties.get("ro.mtk_privacy_protection_lock").equals("1");
    private static final boolean mIsMediatekSimMeLockSupport = SystemProperties.get("ro.sim_me_lock_mode", "0").equals("0");
    private static int sPhoneCount = 0;

    public KeyguardUtils(Context context) {
        this.mUpdateMonitor = KeyguardUpdateMonitor.getInstance(context);
        this.mSubscriptionManager = SubscriptionManager.from(context);
    }

    public static int getNumOfPhone() {
        int i = 4;
        if (sPhoneCount == 0) {
            sPhoneCount = TelephonyManager.getDefault().getPhoneCount();
            if (sPhoneCount <= 4) {
                i = sPhoneCount;
            }
            sPhoneCount = i;
        }
        return sPhoneCount;
    }

    public static int getPhoneIdUsingSubId(int i) {
        Log.e("KeyguardUtils", "getPhoneIdUsingSubId: subId = " + i);
        int phoneId = SubscriptionManager.getPhoneId(i);
        if (phoneId < 0 || phoneId >= getNumOfPhone()) {
            Log.e("KeyguardUtils", "getPhoneIdUsingSubId: invalid phonId = " + phoneId);
        } else {
            Log.e("KeyguardUtils", "getPhoneIdUsingSubId: get phone ID = " + phoneId);
        }
        return phoneId;
    }

    public static int getSubIdUsingPhoneId(int i) {
        int subIdUsingPhoneId = SubscriptionManager.getSubIdUsingPhoneId(i);
        Log.d("KeyguardUtils", "getSubIdUsingPhoneId(phoneId = " + i + ") = " + subIdUsingPhoneId);
        return subIdUsingPhoneId;
    }

    public static boolean isFlightModePowerOffMd() {
        boolean equals = SystemProperties.get("ro.mtk_flight_mode_power_off_md").equals("1");
        Log.d("KeyguardUtils", "powerOffMd = " + equals);
        return equals;
    }

    public static final boolean isMediatekSimMeLockSupport() {
        return mIsMediatekSimMeLockSupport;
    }

    public static final boolean isPrivacyProtectionLockSupport() {
        return mIsPrivacyProtectionLockSupport;
    }

    public static boolean isSystemEncrypted() {
        boolean z;
        String str = SystemProperties.get("ro.crypto.type");
        String str2 = SystemProperties.get("ro.crypto.state");
        String str3 = SystemProperties.get("vold.decrypt");
        if ("unsupported".equals(str2)) {
            return false;
        }
        if ("unencrypted".equals(str2)) {
            z = true;
            if ("".equals(str3)) {
                z = false;
            }
        } else {
            z = true;
            if (!"".equals(str2)) {
                z = true;
                if ("encrypted".equals(str2)) {
                    if ("file".equals(str)) {
                        z = false;
                    } else {
                        z = true;
                        if ("block".equals(str)) {
                            z = true;
                            if ("trigger_restart_framework".equals(str3)) {
                                z = false;
                            }
                        }
                    }
                }
            }
        }
        Log.d("KeyguardUtils", "cryptoType=" + str + " ro.crypto.state=" + str2 + " vold.decrypt=" + str3 + " sysEncrypted=" + z);
        return z;
    }

    public static boolean isValidPhoneId(int i) {
        boolean z = false;
        if (i != Integer.MAX_VALUE) {
            z = false;
            if (i >= 0) {
                z = false;
                if (i < getNumOfPhone()) {
                    z = true;
                }
            }
        }
        return z;
    }

    public static final boolean isVoiceWakeupSupport(Context context) {
        boolean z = false;
        AudioManager audioManager = (AudioManager) context.getSystemService("audio");
        if (audioManager == null) {
            Log.d("KeyguardUtils", "isVoiceWakeupSupport() - get AUDIO_SERVICE fails, return false.");
            return false;
        }
        String parameters = audioManager.getParameters("MTK_VOW_SUPPORT");
        if (parameters != null) {
            z = parameters.equalsIgnoreCase("MTK_VOW_SUPPORT=true");
        }
        return z;
    }

    public static void requestImeStatusRefresh(Context context) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService("input_method");
        if (inputMethodManager != null) {
            Log.d("KeyguardUtils", "call imm.requestImeStatusRefresh()");
            inputMethodManager.refreshImeWindowVisibility();
        }
    }

    public Bitmap getOptrBitmapUsingPhoneId(int i, Context context) {
        Bitmap createIconBitmap;
        SubscriptionInfo activeSubscriptionInfo = this.mSubscriptionManager.getActiveSubscriptionInfo(getSubIdUsingPhoneId(i));
        if (activeSubscriptionInfo == null) {
            Log.d("KeyguardUtils", "getOptrBitmapUsingPhoneId, return null");
            createIconBitmap = null;
        } else {
            createIconBitmap = activeSubscriptionInfo.createIconBitmap(context);
        }
        return createIconBitmap;
    }

    public String getOptrNameUsingPhoneId(int i, Context context) {
        SubscriptionInfo activeSubscriptionInfo = this.mSubscriptionManager.getActiveSubscriptionInfo(getSubIdUsingPhoneId(i));
        if (activeSubscriptionInfo == null) {
            Log.d("KeyguardUtils", "getOptrNameUsingPhoneId, return null");
            return null;
        }
        Log.d("KeyguardUtils", "getOptrNameUsingPhoneId mDisplayName=" + activeSubscriptionInfo.getDisplayName());
        if (activeSubscriptionInfo.getDisplayName() != null) {
            return activeSubscriptionInfo.getDisplayName().toString();
        }
        return null;
    }
}
