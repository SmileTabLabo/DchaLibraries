package com.mediatek.settingslib.ext;

import com.mediatek.common.PluginImpl;
@PluginImpl(interfaceName = "com.mediatek.settingslib.ext.IWifiLibExt")
/* loaded from: classes.dex */
public class DefaultWifiLibExt implements IWifiLibExt {
    @Override // com.mediatek.settingslib.ext.IWifiLibExt
    public void appendApSummary(StringBuilder summary, int autoJoinStatus, String connectFail, String disabled) {
        summary.append(connectFail);
    }
}
