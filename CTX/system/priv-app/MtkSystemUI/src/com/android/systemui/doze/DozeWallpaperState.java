package com.android.systemui.doze;

import android.app.IWallpaperManager;
import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.doze.DozeMachine;
import com.android.systemui.statusbar.phone.DozeParameters;
import java.io.PrintWriter;
/* loaded from: classes.dex */
public class DozeWallpaperState implements DozeMachine.Part {
    private static final boolean DEBUG = Log.isLoggable("DozeWallpaperState", 3);
    private final DozeParameters mDozeParameters;
    private boolean mIsAmbientMode;
    private final IWallpaperManager mWallpaperManagerService;

    public DozeWallpaperState(Context context) {
        this(IWallpaperManager.Stub.asInterface(ServiceManager.getService("wallpaper")), DozeParameters.getInstance(context));
    }

    @VisibleForTesting
    DozeWallpaperState(IWallpaperManager iWallpaperManager, DozeParameters dozeParameters) {
        this.mWallpaperManagerService = iWallpaperManager;
        this.mDozeParameters = dozeParameters;
    }

    @Override // com.android.systemui.doze.DozeMachine.Part
    public void transitionTo(DozeMachine.State state, DozeMachine.State state2) {
        boolean z;
        boolean z2;
        switch (state2) {
            case DOZE:
            case DOZE_AOD:
            case DOZE_AOD_PAUSING:
            case DOZE_AOD_PAUSED:
            case DOZE_REQUEST_PULSE:
            case DOZE_PULSING:
            case DOZE_PULSE_DONE:
                z = true;
                break;
            default:
                z = false;
                break;
        }
        if (z) {
            z2 = this.mDozeParameters.shouldControlScreenOff();
        } else {
            z2 = !this.mDozeParameters.getDisplayNeedsBlanking() || (state == DozeMachine.State.DOZE_PULSING && state2 == DozeMachine.State.FINISH);
        }
        if (z != this.mIsAmbientMode) {
            this.mIsAmbientMode = z;
            try {
                if (DEBUG) {
                    Log.i("DozeWallpaperState", "AOD wallpaper state changed to: " + this.mIsAmbientMode + ", animated: " + z2);
                }
                this.mWallpaperManagerService.setInAmbientMode(this.mIsAmbientMode, z2);
            } catch (RemoteException e) {
                Log.w("DozeWallpaperState", "Cannot notify state to WallpaperManagerService: " + this.mIsAmbientMode);
            }
        }
    }

    @Override // com.android.systemui.doze.DozeMachine.Part
    public void dump(PrintWriter printWriter) {
        printWriter.println("DozeWallpaperState:");
        printWriter.println(" isAmbientMode: " + this.mIsAmbientMode);
    }
}
