package com.mediatek.systemui.ext;

import android.content.Context;
import android.graphics.drawable.Drawable;
import com.mediatek.common.PluginImpl;
@PluginImpl(interfaceName = "com.mediatek.systemui.ext.INavigationBarPlugin")
/* loaded from: a.zip:com/mediatek/systemui/ext/DefaultNavigationBarPlugin.class */
public class DefaultNavigationBarPlugin implements INavigationBarPlugin {
    private Context mContext;

    public DefaultNavigationBarPlugin(Context context) {
        this.mContext = context;
    }

    @Override // com.mediatek.systemui.ext.INavigationBarPlugin
    public Drawable getBackImage(Drawable drawable) {
        return drawable;
    }

    @Override // com.mediatek.systemui.ext.INavigationBarPlugin
    public Drawable getBackImeImage(Drawable drawable) {
        return drawable;
    }

    @Override // com.mediatek.systemui.ext.INavigationBarPlugin
    public Drawable getBackImelandImage(Drawable drawable) {
        return drawable;
    }

    @Override // com.mediatek.systemui.ext.INavigationBarPlugin
    public Drawable getBackLandImage(Drawable drawable) {
        return drawable;
    }

    @Override // com.mediatek.systemui.ext.INavigationBarPlugin
    public Drawable getHomeImage(Drawable drawable) {
        return drawable;
    }

    @Override // com.mediatek.systemui.ext.INavigationBarPlugin
    public Drawable getHomeLandImage(Drawable drawable) {
        return drawable;
    }

    @Override // com.mediatek.systemui.ext.INavigationBarPlugin
    public Drawable getRecentImage(Drawable drawable) {
        return drawable;
    }

    @Override // com.mediatek.systemui.ext.INavigationBarPlugin
    public Drawable getRecentLandImage(Drawable drawable) {
        return drawable;
    }
}
