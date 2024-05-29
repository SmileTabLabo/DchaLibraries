package com.android.systemui.statusbar.policy;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.UserManager;
import android.util.Log;
import com.android.systemui.statusbar.policy.HotspotController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/systemui/statusbar/policy/HotspotControllerImpl.class */
public class HotspotControllerImpl implements HotspotController {
    private static final boolean DEBUG = Log.isLoggable("HotspotController", 3);
    private final ConnectivityManager mConnectivityManager;
    private final Context mContext;
    private int mHotspotState;
    private final ArrayList<HotspotController.Callback> mCallbacks = new ArrayList<>();
    private final Receiver mReceiver = new Receiver(this, null);

    /* loaded from: a.zip:com/android/systemui/statusbar/policy/HotspotControllerImpl$OnStartTetheringCallback.class */
    static final class OnStartTetheringCallback extends ConnectivityManager.OnStartTetheringCallback {
        OnStartTetheringCallback() {
        }

        public void onTetheringFailed() {
        }

        public void onTetheringStarted() {
        }
    }

    /* loaded from: a.zip:com/android/systemui/statusbar/policy/HotspotControllerImpl$Receiver.class */
    private final class Receiver extends BroadcastReceiver {
        private boolean mRegistered;
        final HotspotControllerImpl this$0;

        private Receiver(HotspotControllerImpl hotspotControllerImpl) {
            this.this$0 = hotspotControllerImpl;
        }

        /* synthetic */ Receiver(HotspotControllerImpl hotspotControllerImpl, Receiver receiver) {
            this(hotspotControllerImpl);
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (HotspotControllerImpl.DEBUG) {
                Log.d("HotspotController", "onReceive " + intent.getAction());
            }
            this.this$0.mHotspotState = intent.getIntExtra("wifi_state", 14);
            this.this$0.fireCallback(this.this$0.mHotspotState == 13);
        }

        public void setListening(boolean z) {
            if (z && !this.mRegistered) {
                if (HotspotControllerImpl.DEBUG) {
                    Log.d("HotspotController", "Registering receiver");
                }
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
                this.this$0.mContext.registerReceiver(this, intentFilter);
                this.mRegistered = true;
            } else if (z || !this.mRegistered) {
            } else {
                if (HotspotControllerImpl.DEBUG) {
                    Log.d("HotspotController", "Unregistering receiver");
                }
                this.this$0.mContext.unregisterReceiver(this);
                this.mRegistered = false;
            }
        }
    }

    public HotspotControllerImpl(Context context) {
        this.mContext = context;
        this.mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void fireCallback(boolean z) {
        synchronized (this.mCallbacks) {
            for (HotspotController.Callback callback : this.mCallbacks) {
                callback.onHotspotChanged(z);
            }
        }
    }

    private static String stateToString(int i) {
        switch (i) {
            case 10:
                return "DISABLING";
            case 11:
                return "DISABLED";
            case 12:
                return "ENABLING";
            case 13:
                return "ENABLED";
            case 14:
                return "FAILED";
            default:
                return null;
        }
    }

    @Override // com.android.systemui.statusbar.policy.HotspotController
    public void addCallback(HotspotController.Callback callback) {
        synchronized (this.mCallbacks) {
            if (callback != null) {
                if (!this.mCallbacks.contains(callback)) {
                    if (DEBUG) {
                        Log.d("HotspotController", "addCallback " + callback);
                    }
                    this.mCallbacks.add(callback);
                    this.mReceiver.setListening(!this.mCallbacks.isEmpty());
                }
            }
        }
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("HotspotController state:");
        printWriter.print("  mHotspotEnabled=");
        printWriter.println(stateToString(this.mHotspotState));
    }

    @Override // com.android.systemui.statusbar.policy.HotspotController
    public boolean isHotspotEnabled() {
        return this.mHotspotState == 13;
    }

    @Override // com.android.systemui.statusbar.policy.HotspotController
    public boolean isHotspotSupported() {
        boolean z = false;
        if (this.mConnectivityManager.isTetheringSupported()) {
            z = false;
            if (this.mConnectivityManager.getTetherableWifiRegexs().length != 0) {
                z = UserManager.get(this.mContext).isUserAdmin(ActivityManager.getCurrentUser());
            }
        }
        return z;
    }

    @Override // com.android.systemui.statusbar.policy.HotspotController
    public void removeCallback(HotspotController.Callback callback) {
        if (callback == null) {
            return;
        }
        if (DEBUG) {
            Log.d("HotspotController", "removeCallback " + callback);
        }
        synchronized (this.mCallbacks) {
            this.mCallbacks.remove(callback);
            this.mReceiver.setListening(!this.mCallbacks.isEmpty());
        }
    }

    @Override // com.android.systemui.statusbar.policy.HotspotController
    public void setHotspotEnabled(boolean z) {
        if (!z) {
            this.mConnectivityManager.stopTethering(0);
            return;
        }
        this.mConnectivityManager.startTethering(0, false, new OnStartTetheringCallback());
    }
}
