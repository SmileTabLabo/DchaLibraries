package com.mediatek.settingslib.ext;

import com.mediatek.common.PluginImpl;
@PluginImpl(interfaceName = "com.mediatek.settingslib.ext.IWifiLibExt")
/* loaded from: a.zip:com/mediatek/settingslib/ext/DefaultWifiLibExt.class */
public class DefaultWifiLibExt implements IWifiLibExt {
    @Override // com.mediatek.settingslib.ext.IWifiLibExt
    public void appendApSummary(StringBuilder sb, int i, String str, String str2) {
        sb.append(str);
    }
}
