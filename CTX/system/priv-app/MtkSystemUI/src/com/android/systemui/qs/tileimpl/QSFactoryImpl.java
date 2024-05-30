package com.android.systemui.qs.tileimpl;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.ContextThemeWrapper;
import com.android.systemui.R;
import com.android.systemui.plugins.qs.QSFactory;
import com.android.systemui.plugins.qs.QSIconView;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSTileHost;
import com.android.systemui.qs.external.CustomTile;
import com.android.systemui.qs.tiles.AirplaneModeTile;
import com.android.systemui.qs.tiles.BatterySaverTile;
import com.android.systemui.qs.tiles.BluetoothTile;
import com.android.systemui.qs.tiles.CastTile;
import com.android.systemui.qs.tiles.CellularTile;
import com.android.systemui.qs.tiles.ColorInversionTile;
import com.android.systemui.qs.tiles.DataSaverTile;
import com.android.systemui.qs.tiles.DndTile;
import com.android.systemui.qs.tiles.FlashlightTile;
import com.android.systemui.qs.tiles.HotspotTile;
import com.android.systemui.qs.tiles.IntentTile;
import com.android.systemui.qs.tiles.LocationTile;
import com.android.systemui.qs.tiles.NfcTile;
import com.android.systemui.qs.tiles.NightDisplayTile;
import com.android.systemui.qs.tiles.RotationLockTile;
import com.android.systemui.qs.tiles.UserTile;
import com.android.systemui.qs.tiles.WifiTile;
import com.android.systemui.qs.tiles.WorkModeTile;
import com.android.systemui.util.leak.GarbageMonitor;
import com.mediatek.systemui.ext.IQuickSettingsPlugin;
import com.mediatek.systemui.ext.OpSystemUICustomizationFactoryBase;
import com.mediatek.systemui.qs.tiles.ext.ApnSettingsTile;
import com.mediatek.systemui.qs.tiles.ext.DualSimSettingsTile;
import com.mediatek.systemui.qs.tiles.ext.MobileDataTile;
import com.mediatek.systemui.qs.tiles.ext.SimDataConnectionTile;
import com.mediatek.systemui.statusbar.util.SIMHelper;
/* loaded from: classes.dex */
public class QSFactoryImpl implements QSFactory {
    private final QSTileHost mHost;

    public QSFactoryImpl(QSTileHost qSTileHost) {
        this.mHost = qSTileHost;
    }

    @Override // com.android.systemui.plugins.qs.QSFactory
    public QSTile createTile(String str) {
        QSTileImpl createTileInternal = createTileInternal(str);
        if (createTileInternal != null) {
            createTileInternal.handleStale();
        }
        return createTileInternal;
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    private QSTileImpl createTileInternal(String str) {
        char c;
        Context context = this.mHost.getContext();
        IQuickSettingsPlugin makeQuickSettings = OpSystemUICustomizationFactoryBase.getOpFactory(context).makeQuickSettings(context);
        switch (str.hashCode()) {
            case -2016941037:
                if (str.equals("inversion")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case -1183073498:
                if (str.equals("flashlight")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case -677011630:
                if (str.equals("airplane")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case -331239923:
                if (str.equals("battery")) {
                    c = '\r';
                    break;
                }
                c = 65535;
                break;
            case -40300674:
                if (str.equals("rotation")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 3154:
                if (str.equals("bt")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 99610:
                if (str.equals("dnd")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 108971:
                if (str.equals("nfc")) {
                    c = 16;
                    break;
                }
                c = 65535;
                break;
            case 3046207:
                if (str.equals("cast")) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case 3049826:
                if (str.equals("cell")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 3599307:
                if (str.equals("user")) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case 3649301:
                if (str.equals("wifi")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 3655441:
                if (str.equals("work")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 104817688:
                if (str.equals("night")) {
                    c = 15;
                    break;
                }
                c = 65535;
                break;
            case 109211285:
                if (str.equals("saver")) {
                    c = 14;
                    break;
                }
                c = 65535;
                break;
            case 1099603663:
                if (str.equals("hotspot")) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case 1901043637:
                if (str.equals("location")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return new WifiTile(this.mHost);
            case 1:
                return new BluetoothTile(this.mHost);
            case 2:
                return new CellularTile(this.mHost);
            case 3:
                return new DndTile(this.mHost);
            case 4:
                return new ColorInversionTile(this.mHost);
            case 5:
                return new AirplaneModeTile(this.mHost);
            case 6:
                return new WorkModeTile(this.mHost);
            case 7:
                return new RotationLockTile(this.mHost);
            case '\b':
                return new FlashlightTile(this.mHost);
            case '\t':
                return new LocationTile(this.mHost);
            case '\n':
                return new CastTile(this.mHost);
            case 11:
                return new HotspotTile(this.mHost);
            case '\f':
                return new UserTile(this.mHost);
            case '\r':
                return new BatterySaverTile(this.mHost);
            case 14:
                return new DataSaverTile(this.mHost);
            case 15:
                return new NightDisplayTile(this.mHost);
            case 16:
                return new NfcTile(this.mHost);
            default:
                if (str.equals("dataconnection") && !SIMHelper.isWifiOnlyDevice()) {
                    return new MobileDataTile(this.mHost);
                }
                if (str.equals("simdataconnection") && !SIMHelper.isWifiOnlyDevice() && makeQuickSettings.customizeAddQSTile(new SimDataConnectionTile(this.mHost)) != null) {
                    return (SimDataConnectionTile) makeQuickSettings.customizeAddQSTile(new SimDataConnectionTile(this.mHost));
                }
                if (str.equals("dulsimsettings") && !SIMHelper.isWifiOnlyDevice() && makeQuickSettings.customizeAddQSTile(new DualSimSettingsTile(this.mHost)) != null) {
                    return (DualSimSettingsTile) makeQuickSettings.customizeAddQSTile(new DualSimSettingsTile(this.mHost));
                }
                if (str.equals("apnsettings") && !SIMHelper.isWifiOnlyDevice() && makeQuickSettings.customizeAddQSTile(new ApnSettingsTile(this.mHost)) != null) {
                    return (ApnSettingsTile) makeQuickSettings.customizeAddQSTile(new ApnSettingsTile(this.mHost));
                }
                if (str.startsWith("intent(")) {
                    return IntentTile.create(this.mHost, str);
                }
                if (str.startsWith("custom(")) {
                    return CustomTile.create(this.mHost, str);
                }
                if (Build.IS_DEBUGGABLE && str.equals("dbg:mem")) {
                    return new GarbageMonitor.MemoryTile(this.mHost);
                }
                Log.w("QSFactory", "Bad tile spec: " + str);
                return null;
        }
    }

    @Override // com.android.systemui.plugins.qs.QSFactory
    public com.android.systemui.plugins.qs.QSTileView createTileView(QSTile qSTile, boolean z) {
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(this.mHost.getContext(), (int) R.style.qs_theme);
        QSIconView createTileView = qSTile.createTileView(contextThemeWrapper);
        if (z) {
            return new QSTileBaseView(contextThemeWrapper, createTileView, z);
        }
        return new QSTileView(contextThemeWrapper, createTileView);
    }
}
