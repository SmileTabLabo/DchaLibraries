package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.net.INetworkPolicyListener;
import android.net.NetworkPolicyManager;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/systemui/statusbar/policy/DataSaverController.class */
public class DataSaverController {
    private final Handler mHandler = new Handler();
    private final ArrayList<Listener> mListeners = new ArrayList<>();
    private final INetworkPolicyListener mPolicyListener = new AnonymousClass1(this);
    private final NetworkPolicyManager mPolicyManager;

    /* renamed from: com.android.systemui.statusbar.policy.DataSaverController$1  reason: invalid class name */
    /* loaded from: a.zip:com/android/systemui/statusbar/policy/DataSaverController$1.class */
    class AnonymousClass1 extends INetworkPolicyListener.Stub {
        final DataSaverController this$0;

        AnonymousClass1(DataSaverController dataSaverController) {
            this.this$0 = dataSaverController;
        }

        public void onMeteredIfacesChanged(String[] strArr) throws RemoteException {
        }

        public void onRestrictBackgroundBlacklistChanged(int i, boolean z) {
        }

        public void onRestrictBackgroundChanged(boolean z) throws RemoteException {
            Log.d("DataSaverController", "onRestrictBackgroundChanged isDataSaving = " + z);
            this.this$0.mHandler.post(new Runnable(this, z) { // from class: com.android.systemui.statusbar.policy.DataSaverController.1.1
                final AnonymousClass1 this$1;
                final boolean val$isDataSaving;

                {
                    this.this$1 = this;
                    this.val$isDataSaving = z;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$1.this$0.handleRestrictBackgroundChanged(this.val$isDataSaving);
                }
            });
        }

        public void onRestrictBackgroundWhitelistChanged(int i, boolean z) {
        }

        public void onUidRulesChanged(int i, int i2) throws RemoteException {
        }
    }

    /* loaded from: a.zip:com/android/systemui/statusbar/policy/DataSaverController$Listener.class */
    public interface Listener {
        void onDataSaverChanged(boolean z);
    }

    public DataSaverController(Context context) {
        this.mPolicyManager = NetworkPolicyManager.from(context);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleRestrictBackgroundChanged(boolean z) {
        synchronized (this.mListeners) {
            for (int i = 0; i < this.mListeners.size(); i++) {
                this.mListeners.get(i).onDataSaverChanged(z);
            }
        }
    }

    public void addListener(Listener listener) {
        synchronized (this.mListeners) {
            this.mListeners.add(listener);
            if (this.mListeners.size() == 1) {
                this.mPolicyManager.registerListener(this.mPolicyListener);
            }
        }
        listener.onDataSaverChanged(isDataSaverEnabled());
    }

    public boolean isDataSaverEnabled() {
        return this.mPolicyManager.getRestrictBackground();
    }

    public void remListener(Listener listener) {
        synchronized (this.mListeners) {
            this.mListeners.remove(listener);
            if (this.mListeners.size() == 0) {
                this.mPolicyManager.unregisterListener(this.mPolicyListener);
            }
        }
    }

    public void setDataSaverEnabled(boolean z) {
        Log.d("DataSaverController", "setDataSaverEnabled enabled = " + z);
        this.mPolicyManager.setRestrictBackground(z);
        try {
            this.mPolicyListener.onRestrictBackgroundChanged(z);
        } catch (RemoteException e) {
        }
    }
}
