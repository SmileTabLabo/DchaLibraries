package com.mediatek.settingslib;

import android.content.Context;
import com.mediatek.common.MPlugin;
import com.mediatek.settingslib.ext.DefaultDrawerExt;
import com.mediatek.settingslib.ext.IDrawerExt;
/* loaded from: a.zip:com/mediatek/settingslib/UtilsExt.class */
public class UtilsExt {
    private static final String TAG = UtilsExt.class.getSimpleName();

    public static IDrawerExt getDrawerPlugin(Context context) {
        IDrawerExt iDrawerExt = (IDrawerExt) MPlugin.createInstance(IDrawerExt.class.getName(), context);
        DefaultDrawerExt defaultDrawerExt = iDrawerExt;
        if (iDrawerExt == null) {
            defaultDrawerExt = new DefaultDrawerExt(context);
        }
        return defaultDrawerExt;
    }
}
