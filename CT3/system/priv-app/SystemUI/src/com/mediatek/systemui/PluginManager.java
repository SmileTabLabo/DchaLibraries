package com.mediatek.systemui;

import android.content.Context;
import com.mediatek.common.MPlugin;
import com.mediatek.systemui.ext.DefaultMobileIconExt;
import com.mediatek.systemui.ext.DefaultQuickSettingsPlugin;
import com.mediatek.systemui.ext.DefaultStatusBarPlmnPlugin;
import com.mediatek.systemui.ext.DefaultSystemUIStatusBarExt;
import com.mediatek.systemui.ext.IMobileIconExt;
import com.mediatek.systemui.ext.IQuickSettingsPlugin;
import com.mediatek.systemui.ext.IStatusBarPlmnPlugin;
import com.mediatek.systemui.ext.ISystemUIStatusBarExt;
/* loaded from: a.zip:com/mediatek/systemui/PluginManager.class */
public class PluginManager {
    private static IMobileIconExt sMobileIconExt = null;
    private static IQuickSettingsPlugin sQuickSettingsPlugin = null;
    private static IStatusBarPlmnPlugin sStatusBarPlmnPlugin = null;
    private static ISystemUIStatusBarExt sSystemUIStatusBarExt = null;

    public static IMobileIconExt getMobileIconExt(Context context) {
        IMobileIconExt iMobileIconExt;
        synchronized (PluginManager.class) {
            try {
                if (sMobileIconExt == null) {
                    sMobileIconExt = (IMobileIconExt) MPlugin.createInstance(IMobileIconExt.class.getName(), context);
                    if (sMobileIconExt == null) {
                        sMobileIconExt = new DefaultMobileIconExt();
                    }
                }
                iMobileIconExt = sMobileIconExt;
            } catch (Throwable th) {
                throw th;
            }
        }
        return iMobileIconExt;
    }

    public static IQuickSettingsPlugin getQuickSettingsPlugin(Context context) {
        IQuickSettingsPlugin iQuickSettingsPlugin;
        synchronized (PluginManager.class) {
            try {
                if (sQuickSettingsPlugin == null) {
                    sQuickSettingsPlugin = (IQuickSettingsPlugin) MPlugin.createInstance(IQuickSettingsPlugin.class.getName(), context);
                    if (sQuickSettingsPlugin == null) {
                        sQuickSettingsPlugin = new DefaultQuickSettingsPlugin(context);
                    }
                }
                iQuickSettingsPlugin = sQuickSettingsPlugin;
            } catch (Throwable th) {
                throw th;
            }
        }
        return iQuickSettingsPlugin;
    }

    public static IStatusBarPlmnPlugin getStatusBarPlmnPlugin(Context context) {
        IStatusBarPlmnPlugin iStatusBarPlmnPlugin;
        synchronized (PluginManager.class) {
            try {
                if (sStatusBarPlmnPlugin == null) {
                    sStatusBarPlmnPlugin = (IStatusBarPlmnPlugin) MPlugin.createInstance(IStatusBarPlmnPlugin.class.getName(), context);
                    if (sStatusBarPlmnPlugin == null) {
                        sStatusBarPlmnPlugin = new DefaultStatusBarPlmnPlugin(context);
                    }
                }
                iStatusBarPlmnPlugin = sStatusBarPlmnPlugin;
            } catch (Throwable th) {
                throw th;
            }
        }
        return iStatusBarPlmnPlugin;
    }

    public static ISystemUIStatusBarExt getSystemUIStatusBarExt(Context context) {
        ISystemUIStatusBarExt iSystemUIStatusBarExt;
        synchronized (PluginManager.class) {
            try {
                if (sSystemUIStatusBarExt == null) {
                    sSystemUIStatusBarExt = new DefaultSystemUIStatusBarExt(context);
                }
                ISystemUIStatusBarExt iSystemUIStatusBarExt2 = (ISystemUIStatusBarExt) MPlugin.createInstance(ISystemUIStatusBarExt.class.getName(), context);
                iSystemUIStatusBarExt = iSystemUIStatusBarExt2;
                if (iSystemUIStatusBarExt2 == null) {
                    iSystemUIStatusBarExt = sSystemUIStatusBarExt;
                }
            } catch (Throwable th) {
                throw th;
            }
        }
        return iSystemUIStatusBarExt;
    }
}
