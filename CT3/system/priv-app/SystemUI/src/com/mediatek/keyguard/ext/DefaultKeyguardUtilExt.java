package com.mediatek.keyguard.ext;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import com.mediatek.common.PluginImpl;
@PluginImpl(interfaceName = "com.mediatek.keyguard.ext.IKeyguardUtilExt")
/* loaded from: a.zip:com/mediatek/keyguard/ext/DefaultKeyguardUtilExt.class */
public class DefaultKeyguardUtilExt implements IKeyguardUtilExt {
    private static final String TAG = "DefaultKeyguardUtilExt";

    @Override // com.mediatek.keyguard.ext.IKeyguardUtilExt
    public void customizeCarrierTextGravity(TextView textView) {
        Log.d(TAG, "customizeCarrierTextGravity view = " + textView);
    }

    @Override // com.mediatek.keyguard.ext.IKeyguardUtilExt
    public void customizePinPukLockView(int i, ImageView imageView, TextView textView) {
        Log.d(TAG, "customizePinPukLockView");
    }

    @Override // com.mediatek.keyguard.ext.IKeyguardUtilExt
    public boolean lockImmediatelyWhenScreenTimeout() {
        Log.d(TAG, "lockImmediatelyWhenScreenTimeout, ret=false");
        return false;
    }

    @Override // com.mediatek.keyguard.ext.IKeyguardUtilExt
    public void showToastWhenUnlockPinPuk(Context context, int i) {
        Log.d(TAG, "showToastWhenUnlockPinPuk");
    }
}
