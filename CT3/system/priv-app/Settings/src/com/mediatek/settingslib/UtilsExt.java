package com.mediatek.settingslib;

import android.content.Context;
import com.mediatek.common.MPlugin;
import com.mediatek.settingslib.ext.DefaultDrawerExt;
import com.mediatek.settingslib.ext.IDrawerExt;
/* loaded from: classes.dex */
public class UtilsExt {
    private static final String TAG = UtilsExt.class.getSimpleName();

    public static IDrawerExt getDrawerPlugin(Context context) {
        IDrawerExt ext = (IDrawerExt) MPlugin.createInstance(IDrawerExt.class.getName(), context);
        if (ext == null) {
            return new DefaultDrawerExt(context);
        }
        return ext;
    }
}
