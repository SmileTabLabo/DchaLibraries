package com.mediatek.keyguard.ext;

import android.content.Context;
import com.mediatek.common.PluginImpl;
import com.mediatek.keyguard.ext.IOperatorSIMString;
@PluginImpl(interfaceName = "com.mediatek.keyguard.ext.IOperatorSIMString")
/* loaded from: a.zip:com/mediatek/keyguard/ext/DefaultOperatorSIMString.class */
public class DefaultOperatorSIMString implements IOperatorSIMString {
    @Override // com.mediatek.keyguard.ext.IOperatorSIMString
    public String getOperatorSIMString(String str, int i, IOperatorSIMString.SIMChangedTag sIMChangedTag, Context context) {
        return str;
    }
}
