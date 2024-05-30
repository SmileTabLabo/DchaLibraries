package com.android.settings.development.qstile;

import android.content.ComponentName;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.service.quicksettings.TileService;
import android.util.EventLog;
import android.util.Log;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;
import android.widget.Toast;
import com.android.internal.app.LocalePicker;
import com.android.internal.statusbar.IStatusBarService;
import com.android.settingslib.development.DevelopmentSettingsEnabler;
import com.android.settingslib.development.SystemPropPoker;
/* loaded from: classes.dex */
public abstract class DevelopmentTiles extends TileService {
    protected abstract boolean isEnabled();

    protected abstract void setIsEnabled(boolean z);

    @Override // android.service.quicksettings.TileService
    public void onStartListening() {
        super.onStartListening();
        refresh();
    }

    public void refresh() {
        int i = 0;
        if (!DevelopmentSettingsEnabler.isDevelopmentSettingsEnabled(this)) {
            if (isEnabled()) {
                setIsEnabled(false);
                SystemPropPoker.getInstance().poke();
            }
            ComponentName componentName = new ComponentName(getPackageName(), getClass().getName());
            try {
                getPackageManager().setComponentEnabledSetting(componentName, 2, 1);
                IStatusBarService asInterface = IStatusBarService.Stub.asInterface(ServiceManager.checkService("statusbar"));
                if (asInterface != null) {
                    EventLog.writeEvent(1397638484, "117770924");
                    asInterface.remTile(componentName);
                }
            } catch (RemoteException e) {
                Log.e("DevelopmentTiles", "Failed to modify QS tile for component " + componentName.toString(), e);
            }
        } else {
            i = isEnabled() ? 2 : 1;
        }
        getQsTile().setState(i);
        getQsTile().updateTile();
    }

    @Override // android.service.quicksettings.TileService
    public void onClick() {
        setIsEnabled(getQsTile().getState() == 1);
        SystemPropPoker.getInstance().poke();
        refresh();
    }

    /* loaded from: classes.dex */
    public static class ShowLayout extends DevelopmentTiles {
        @Override // com.android.settings.development.qstile.DevelopmentTiles
        protected boolean isEnabled() {
            return SystemProperties.getBoolean("debug.layout", false);
        }

        @Override // com.android.settings.development.qstile.DevelopmentTiles
        protected void setIsEnabled(boolean z) {
            SystemProperties.set("debug.layout", z ? "true" : "false");
        }
    }

    /* loaded from: classes.dex */
    public static class GPUProfiling extends DevelopmentTiles {
        @Override // com.android.settings.development.qstile.DevelopmentTiles
        protected boolean isEnabled() {
            return SystemProperties.get("debug.hwui.profile").equals("visual_bars");
        }

        @Override // com.android.settings.development.qstile.DevelopmentTiles
        protected void setIsEnabled(boolean z) {
            SystemProperties.set("debug.hwui.profile", z ? "visual_bars" : "");
        }
    }

    /* loaded from: classes.dex */
    public static class ForceRTL extends DevelopmentTiles {
        @Override // com.android.settings.development.qstile.DevelopmentTiles
        protected boolean isEnabled() {
            return Settings.Global.getInt(getContentResolver(), "debug.force_rtl", 0) != 0;
        }

        @Override // com.android.settings.development.qstile.DevelopmentTiles
        protected void setIsEnabled(boolean z) {
            Settings.Global.putInt(getContentResolver(), "debug.force_rtl", z ? 1 : 0);
            SystemProperties.set("debug.force_rtl", z ? "1" : "0");
            LocalePicker.updateLocales(getResources().getConfiguration().getLocales());
        }
    }

    /* loaded from: classes.dex */
    public static class AnimationSpeed extends DevelopmentTiles {
        @Override // com.android.settings.development.qstile.DevelopmentTiles
        protected boolean isEnabled() {
            try {
                return WindowManagerGlobal.getWindowManagerService().getAnimationScale(0) != 1.0f;
            } catch (RemoteException e) {
                return false;
            }
        }

        @Override // com.android.settings.development.qstile.DevelopmentTiles
        protected void setIsEnabled(boolean z) {
            IWindowManager windowManagerService = WindowManagerGlobal.getWindowManagerService();
            float f = z ? 10.0f : 1.0f;
            try {
                windowManagerService.setAnimationScale(0, f);
                windowManagerService.setAnimationScale(1, f);
                windowManagerService.setAnimationScale(2, f);
            } catch (RemoteException e) {
            }
        }
    }

    /* loaded from: classes.dex */
    public static class WinscopeTrace extends DevelopmentTiles {
        static final int SURFACE_FLINGER_LAYER_TRACE_CONTROL_CODE = 1025;
        static final int SURFACE_FLINGER_LAYER_TRACE_STATUS_CODE = 1026;
        private IBinder mSurfaceFlinger;
        private Toast mToast;
        private IWindowManager mWindowManager;

        @Override // android.app.Service
        public void onCreate() {
            super.onCreate();
            this.mWindowManager = WindowManagerGlobal.getWindowManagerService();
            this.mSurfaceFlinger = ServiceManager.getService("SurfaceFlinger");
            this.mToast = Toast.makeText(getApplicationContext(), "Trace files written to /data/misc/wmtrace", 1);
        }

        private boolean isWindowTraceEnabled() {
            try {
                return this.mWindowManager.isWindowTraceEnabled();
            } catch (RemoteException e) {
                Log.e("DevelopmentTiles", "Could not get window trace status, defaulting to false." + e.toString());
                return false;
            }
        }

        /* JADX WARN: Removed duplicated region for block: B:30:0x0065  */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
        */
        private boolean isLayerTraceEnabled() {
            Parcel parcel;
            Parcel parcel2;
            Parcel parcel3;
            RemoteException e;
            Parcel parcel4 = null;
            boolean z = false;
            try {
                try {
                    if (this.mSurfaceFlinger != null) {
                        parcel = Parcel.obtain();
                        try {
                            parcel3 = Parcel.obtain();
                            try {
                                parcel3.writeInterfaceToken("android.ui.ISurfaceComposer");
                                this.mSurfaceFlinger.transact(SURFACE_FLINGER_LAYER_TRACE_STATUS_CODE, parcel3, parcel, 0);
                                z = parcel.readBoolean();
                                parcel4 = parcel3;
                            } catch (RemoteException e2) {
                                e = e2;
                                Log.e("DevelopmentTiles", "Could not get layer trace status, defaulting to false." + e.toString());
                                if (parcel3 != null) {
                                    parcel3.recycle();
                                    parcel.recycle();
                                }
                                return z;
                            }
                        } catch (RemoteException e3) {
                            parcel3 = null;
                            e = e3;
                        } catch (Throwable th) {
                            th = th;
                            parcel2 = null;
                            th = th;
                            if (parcel2 != null) {
                            }
                            throw th;
                        }
                    } else {
                        parcel = null;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (parcel2 != null) {
                        parcel2.recycle();
                        parcel.recycle();
                    }
                    throw th;
                }
            } catch (RemoteException e4) {
                parcel3 = null;
                e = e4;
                parcel = null;
            } catch (Throwable th3) {
                th = th3;
                parcel = null;
                parcel2 = null;
            }
            if (parcel4 != null) {
                parcel4.recycle();
                parcel.recycle();
            }
            return z;
        }

        @Override // com.android.settings.development.qstile.DevelopmentTiles
        protected boolean isEnabled() {
            return isWindowTraceEnabled() || isLayerTraceEnabled();
        }

        private void setWindowTraceEnabled(boolean z) {
            try {
                if (z) {
                    this.mWindowManager.startWindowTrace();
                } else {
                    this.mWindowManager.stopWindowTrace();
                }
            } catch (RemoteException e) {
                Log.e("DevelopmentTiles", "Could not set window trace status." + e.toString());
            }
        }

        private void setLayerTraceEnabled(boolean z) {
            Parcel parcel = null;
            try {
                try {
                    if (this.mSurfaceFlinger != null) {
                        Parcel obtain = Parcel.obtain();
                        try {
                            obtain.writeInterfaceToken("android.ui.ISurfaceComposer");
                            obtain.writeInt(z ? 1 : 0);
                            this.mSurfaceFlinger.transact(SURFACE_FLINGER_LAYER_TRACE_CONTROL_CODE, obtain, null, 0);
                            parcel = obtain;
                        } catch (RemoteException e) {
                            e = e;
                            parcel = obtain;
                            Log.e("DevelopmentTiles", "Could not set layer tracing." + e.toString());
                            if (parcel == null) {
                                return;
                            }
                            parcel.recycle();
                        } catch (Throwable th) {
                            th = th;
                            parcel = obtain;
                            if (parcel != null) {
                                parcel.recycle();
                            }
                            throw th;
                        }
                    }
                    if (parcel == null) {
                        return;
                    }
                } catch (Throwable th2) {
                    th = th2;
                }
            } catch (RemoteException e2) {
                e = e2;
            }
            parcel.recycle();
        }

        @Override // com.android.settings.development.qstile.DevelopmentTiles
        protected void setIsEnabled(boolean z) {
            setWindowTraceEnabled(z);
            setLayerTraceEnabled(z);
            if (!z) {
                this.mToast.show();
            }
        }
    }
}
