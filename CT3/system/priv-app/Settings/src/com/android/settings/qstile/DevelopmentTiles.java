package com.android.settings.qstile;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.SystemProperties;
import android.service.quicksettings.TileService;
import com.android.settings.DevelopmentSettings;
/* loaded from: classes.dex */
public class DevelopmentTiles {
    static final Class[] TILE_CLASSES = {ShowLayout.class, GPUProfiling.class};

    public static void setTilesEnabled(Context context, boolean enable) {
        Class[] clsArr;
        PackageManager pm = context.getPackageManager();
        for (Class cls : TILE_CLASSES) {
            pm.setComponentEnabledSetting(new ComponentName(context, cls), enable ? 1 : 0, 1);
        }
    }

    /* loaded from: classes.dex */
    public static class ShowLayout extends TileService {
        @Override // android.service.quicksettings.TileService
        public void onStartListening() {
            super.onStartListening();
            refresh();
        }

        public void refresh() {
            boolean enabled = SystemProperties.getBoolean("debug.layout", false);
            getQsTile().setState(enabled ? 2 : 1);
            getQsTile().updateTile();
        }

        @Override // android.service.quicksettings.TileService
        public void onClick() {
            SystemProperties.set("debug.layout", getQsTile().getState() == 1 ? "true" : "false");
            new DevelopmentSettings.SystemPropPoker().execute(new Void[0]);
            refresh();
        }
    }

    /* loaded from: classes.dex */
    public static class GPUProfiling extends TileService {
        @Override // android.service.quicksettings.TileService
        public void onStartListening() {
            super.onStartListening();
            refresh();
        }

        public void refresh() {
            String value = SystemProperties.get("debug.hwui.profile");
            getQsTile().setState(value.equals("visual_bars") ? 2 : 1);
            getQsTile().updateTile();
        }

        @Override // android.service.quicksettings.TileService
        public void onClick() {
            SystemProperties.set("debug.hwui.profile", getQsTile().getState() == 1 ? "visual_bars" : "");
            new DevelopmentSettings.SystemPropPoker().execute(new Void[0]);
            refresh();
        }
    }
}
