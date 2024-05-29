package com.mediatek.keyguard.Plugin;

import android.content.Context;
import android.util.Log;
import com.mediatek.common.MPlugin;
import com.mediatek.keyguard.ext.DefaultCarrierTextExt;
import com.mediatek.keyguard.ext.DefaultEmergencyButtonExt;
import com.mediatek.keyguard.ext.DefaultKeyguardUtilExt;
import com.mediatek.keyguard.ext.DefaultOperatorSIMString;
import com.mediatek.keyguard.ext.ICarrierTextExt;
import com.mediatek.keyguard.ext.IEmergencyButtonExt;
import com.mediatek.keyguard.ext.IKeyguardUtilExt;
import com.mediatek.keyguard.ext.IOperatorSIMString;
/* loaded from: a.zip:com/mediatek/keyguard/Plugin/KeyguardPluginFactory.class */
public class KeyguardPluginFactory {
    private static IEmergencyButtonExt mEmergencyButtonExt = null;
    private static ICarrierTextExt mCarrierTextExt = null;
    private static IKeyguardUtilExt mKeyguardUtilExt = null;
    private static IOperatorSIMString mOperatorSIMString = null;

    public static ICarrierTextExt getCarrierTextExt(Context context) {
        ICarrierTextExt iCarrierTextExt;
        synchronized (KeyguardPluginFactory.class) {
            try {
                if (mCarrierTextExt == null) {
                    mCarrierTextExt = (ICarrierTextExt) MPlugin.createInstance(ICarrierTextExt.class.getName(), context);
                    Log.d("KeyguardPluginFactory", "getCarrierTextExt carrierTextExt= " + mCarrierTextExt);
                    if (mCarrierTextExt == null) {
                        mCarrierTextExt = new DefaultCarrierTextExt();
                        Log.d("KeyguardPluginFactory", "getCarrierTextExt get DefaultCarrierTextExt = " + mCarrierTextExt);
                    }
                }
                iCarrierTextExt = mCarrierTextExt;
            } catch (Throwable th) {
                throw th;
            }
        }
        return iCarrierTextExt;
    }

    public static IEmergencyButtonExt getEmergencyButtonExt(Context context) {
        IEmergencyButtonExt iEmergencyButtonExt;
        synchronized (KeyguardPluginFactory.class) {
            try {
                if (mEmergencyButtonExt == null) {
                    mEmergencyButtonExt = (IEmergencyButtonExt) MPlugin.createInstance(IEmergencyButtonExt.class.getName(), context);
                    Log.d("KeyguardPluginFactory", "getEmergencyButtonExt emergencyButtonExt= " + mEmergencyButtonExt);
                    if (mEmergencyButtonExt == null) {
                        mEmergencyButtonExt = new DefaultEmergencyButtonExt();
                        Log.d("KeyguardPluginFactory", "getEmergencyButtonExt get DefaultEmergencyButtonExt = " + mEmergencyButtonExt);
                    }
                }
                iEmergencyButtonExt = mEmergencyButtonExt;
            } catch (Throwable th) {
                throw th;
            }
        }
        return iEmergencyButtonExt;
    }

    public static IKeyguardUtilExt getKeyguardUtilExt(Context context) {
        IKeyguardUtilExt iKeyguardUtilExt;
        synchronized (KeyguardPluginFactory.class) {
            try {
                if (mKeyguardUtilExt == null) {
                    mKeyguardUtilExt = (IKeyguardUtilExt) MPlugin.createInstance(IKeyguardUtilExt.class.getName(), context);
                    Log.d("KeyguardPluginFactory", "getKeyguardUtilExt keyguardUtilExt= " + mKeyguardUtilExt);
                    if (mKeyguardUtilExt == null) {
                        mKeyguardUtilExt = new DefaultKeyguardUtilExt();
                        Log.d("KeyguardPluginFactory", "getKeyguardUtilExt get DefaultKeyguardUtilExt = " + mKeyguardUtilExt);
                    }
                }
                iKeyguardUtilExt = mKeyguardUtilExt;
            } catch (Throwable th) {
                throw th;
            }
        }
        return iKeyguardUtilExt;
    }

    public static IOperatorSIMString getOperatorSIMString(Context context) {
        IOperatorSIMString iOperatorSIMString;
        synchronized (KeyguardPluginFactory.class) {
            try {
                if (mOperatorSIMString == null) {
                    mOperatorSIMString = (IOperatorSIMString) MPlugin.createInstance(IOperatorSIMString.class.getName(), context);
                    Log.d("KeyguardPluginFactory", "getOperatorSIMString operatorSIMString= " + mOperatorSIMString);
                    if (mOperatorSIMString == null) {
                        mOperatorSIMString = new DefaultOperatorSIMString();
                        Log.d("KeyguardPluginFactory", "getOperatorSIMString get DefaultOperatorSIMString = " + mOperatorSIMString);
                    }
                }
                iOperatorSIMString = mOperatorSIMString;
            } catch (Throwable th) {
                throw th;
            }
        }
        return iOperatorSIMString;
    }
}
