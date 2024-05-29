package com.mediatek.settingslib.ext;

import android.content.Context;
import android.content.ContextWrapper;
import com.mediatek.common.PluginImpl;
@PluginImpl(interfaceName = "com.mediatek.settingslib.ext.IDrawerExt")
/* loaded from: a.zip:com/mediatek/settingslib/ext/DefaultDrawerExt.class */
public class DefaultDrawerExt extends ContextWrapper implements IDrawerExt {
    public DefaultDrawerExt(Context context) {
        super(context);
    }

    @Override // com.mediatek.settingslib.ext.IDrawerExt
    public String customizeSimDisplayString(String str, int i) {
        return str;
    }

    @Override // com.mediatek.settingslib.ext.IDrawerExt
    public void setFactoryResetTitle(Object obj) {
    }
}
