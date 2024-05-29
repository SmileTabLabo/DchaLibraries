package com.android.settings.datausage;

import android.content.Context;
import android.net.INetworkPolicyListener;
import android.net.INetworkPolicyManager;
import android.net.NetworkPolicyManager;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.SparseBooleanArray;
import com.android.internal.logging.MetricsLogger;
import java.util.ArrayList;
/* loaded from: classes.dex */
public class DataSaverBackend {
    private SparseBooleanArray mBlacklist;
    private final Context mContext;
    private final NetworkPolicyManager mPolicyManager;
    private SparseBooleanArray mWhitelist;
    private final Handler mHandler = new Handler();
    private final ArrayList<Listener> mListeners = new ArrayList<>();
    private final INetworkPolicyListener mPolicyListener = new INetworkPolicyListener.Stub() { // from class: com.android.settings.datausage.DataSaverBackend.1
        public void onUidRulesChanged(int uid, int uidRules) throws RemoteException {
        }

        public void onRestrictBackgroundBlacklistChanged(final int uid, final boolean blacklisted) {
            if (DataSaverBackend.this.mBlacklist == null) {
                DataSaverBackend.this.loadBlacklist();
            }
            DataSaverBackend.this.mBlacklist.put(uid, blacklisted);
            DataSaverBackend.this.mHandler.post(new Runnable() { // from class: com.android.settings.datausage.DataSaverBackend.1.1
                @Override // java.lang.Runnable
                public void run() {
                    DataSaverBackend.this.handleBlacklistChanged(uid, blacklisted);
                }
            });
        }

        public void onRestrictBackgroundWhitelistChanged(final int uid, final boolean whitelisted) {
            if (DataSaverBackend.this.mWhitelist == null) {
                DataSaverBackend.this.loadWhitelist();
            }
            DataSaverBackend.this.mWhitelist.put(uid, whitelisted);
            DataSaverBackend.this.mHandler.post(new Runnable() { // from class: com.android.settings.datausage.DataSaverBackend.1.2
                @Override // java.lang.Runnable
                public void run() {
                    DataSaverBackend.this.handleWhitelistChanged(uid, whitelisted);
                }
            });
        }

        public void onMeteredIfacesChanged(String[] strings) throws RemoteException {
        }

        public void onRestrictBackgroundChanged(final boolean isDataSaving) throws RemoteException {
            DataSaverBackend.this.mHandler.post(new Runnable() { // from class: com.android.settings.datausage.DataSaverBackend.1.3
                @Override // java.lang.Runnable
                public void run() {
                    DataSaverBackend.this.handleRestrictBackgroundChanged(isDataSaving);
                }
            });
        }
    };
    private final INetworkPolicyManager mIPolicyManager = INetworkPolicyManager.Stub.asInterface(ServiceManager.getService("netpolicy"));

    /* loaded from: classes.dex */
    public interface Listener {
        void onBlacklistStatusChanged(int i, boolean z);

        void onDataSaverChanged(boolean z);

        void onWhitelistStatusChanged(int i, boolean z);
    }

    public DataSaverBackend(Context context) {
        this.mContext = context;
        this.mPolicyManager = NetworkPolicyManager.from(context);
    }

    public void addListener(Listener listener) {
        this.mListeners.add(listener);
        if (this.mListeners.size() == 1) {
            this.mPolicyManager.registerListener(this.mPolicyListener);
        }
        listener.onDataSaverChanged(isDataSaverEnabled());
    }

    public void remListener(Listener listener) {
        this.mListeners.remove(listener);
        if (this.mListeners.size() != 0) {
            return;
        }
        this.mPolicyManager.unregisterListener(this.mPolicyListener);
    }

    public boolean isDataSaverEnabled() {
        return this.mPolicyManager.getRestrictBackground();
    }

    public void setDataSaverEnabled(boolean enabled) {
        this.mPolicyManager.setRestrictBackground(enabled);
        MetricsLogger.action(this.mContext, 394, enabled ? 1 : 0);
    }

    public void refreshWhitelist() {
        loadWhitelist();
    }

    public void setIsWhitelisted(int uid, String packageName, boolean whitelisted) {
        this.mWhitelist.put(uid, whitelisted);
        try {
            if (whitelisted) {
                this.mIPolicyManager.addRestrictBackgroundWhitelistedUid(uid);
            } else {
                this.mIPolicyManager.removeRestrictBackgroundWhitelistedUid(uid);
            }
        } catch (RemoteException e) {
            Log.w("DataSaverBackend", "Can't reach policy manager", e);
        }
        if (!whitelisted) {
            return;
        }
        MetricsLogger.action(this.mContext, 395, packageName);
    }

    public boolean isWhitelisted(int uid) {
        if (this.mWhitelist == null) {
            loadWhitelist();
        }
        return this.mWhitelist.get(uid);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void loadWhitelist() {
        int[] restrictBackgroundWhitelistedUids;
        this.mWhitelist = new SparseBooleanArray();
        try {
            for (int uid : this.mIPolicyManager.getRestrictBackgroundWhitelistedUids()) {
                this.mWhitelist.put(uid, true);
            }
        } catch (RemoteException e) {
        }
    }

    public void refreshBlacklist() {
        loadBlacklist();
    }

    public void setIsBlacklisted(int uid, String packageName, boolean blacklisted) {
        this.mPolicyManager.setUidPolicy(uid, blacklisted ? 1 : 0);
        if (!blacklisted) {
            return;
        }
        MetricsLogger.action(this.mContext, 396, packageName);
    }

    public boolean isBlacklisted(int uid) {
        if (this.mBlacklist == null) {
            loadBlacklist();
        }
        return this.mBlacklist.get(uid);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void loadBlacklist() {
        int[] uidsWithPolicy;
        this.mBlacklist = new SparseBooleanArray();
        try {
            for (int uid : this.mIPolicyManager.getUidsWithPolicy(1)) {
                this.mBlacklist.put(uid, true);
            }
        } catch (RemoteException e) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleRestrictBackgroundChanged(boolean isDataSaving) {
        for (int i = 0; i < this.mListeners.size(); i++) {
            this.mListeners.get(i).onDataSaverChanged(isDataSaving);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleWhitelistChanged(int uid, boolean isWhitelisted) {
        for (int i = 0; i < this.mListeners.size(); i++) {
            this.mListeners.get(i).onWhitelistStatusChanged(uid, isWhitelisted);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleBlacklistChanged(int uid, boolean isBlacklisted) {
        for (int i = 0; i < this.mListeners.size(); i++) {
            this.mListeners.get(i).onBlacklistStatusChanged(uid, isBlacklisted);
        }
    }
}
