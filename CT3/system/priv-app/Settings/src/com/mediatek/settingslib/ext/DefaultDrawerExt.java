package com.mediatek.settingslib.ext;

import android.content.Context;
import android.content.ContextWrapper;
import com.mediatek.common.PluginImpl;
@PluginImpl(interfaceName = "com.mediatek.settingslib.ext.IDrawerExt")
/* loaded from: classes.dex */
public class DefaultDrawerExt extends ContextWrapper implements IDrawerExt {
    public DefaultDrawerExt(Context base) {
        super(base);
    }

    @Override // com.mediatek.settingslib.ext.IDrawerExt
    public String customizeSimDisplayString(String simString, int slotId) {
        return simString;
    }

    @Override // com.mediatek.settingslib.ext.IDrawerExt
    public void setFactoryResetTitle(Object obj) {
    }
}
