package com.android.systemui.statusbar.phone;

import android.app.ActivityManager;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.os.UserHandle;
import android.os.UserManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/ManagedProfileController.class */
public class ManagedProfileController {
    private final Context mContext;
    private int mCurrentUser;
    private boolean mListening;
    private final UserManager mUserManager;
    private final List<Callback> mCallbacks = new ArrayList();
    private final BroadcastReceiver mReceiver = new BroadcastReceiver(this) { // from class: com.android.systemui.statusbar.phone.ManagedProfileController.1
        final ManagedProfileController this$0;

        {
            this.this$0 = this;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            this.this$0.reloadManagedProfiles();
            for (Callback callback : this.this$0.mCallbacks) {
                callback.onManagedProfileChanged();
            }
        }
    };
    private final LinkedList<UserInfo> mProfiles = new LinkedList<>();

    /* loaded from: a.zip:com/android/systemui/statusbar/phone/ManagedProfileController$Callback.class */
    public interface Callback {
        void onManagedProfileChanged();

        void onManagedProfileRemoved();
    }

    public ManagedProfileController(QSTileHost qSTileHost) {
        this.mContext = qSTileHost.getContext();
        this.mUserManager = UserManager.get(this.mContext);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void reloadManagedProfiles() {
        synchronized (this.mProfiles) {
            boolean z = this.mProfiles.size() > 0;
            int currentUser = ActivityManager.getCurrentUser();
            this.mProfiles.clear();
            for (UserInfo userInfo : this.mUserManager.getEnabledProfiles(currentUser)) {
                if (userInfo.isManagedProfile()) {
                    this.mProfiles.add(userInfo);
                }
            }
            if (this.mProfiles.size() == 0 && z && currentUser == this.mCurrentUser) {
                for (Callback callback : this.mCallbacks) {
                    callback.onManagedProfileRemoved();
                }
            }
            this.mCurrentUser = currentUser;
        }
    }

    private void setListening(boolean z) {
        this.mListening = z;
        if (!z) {
            this.mContext.unregisterReceiver(this.mReceiver);
            return;
        }
        reloadManagedProfiles();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        intentFilter.addAction("android.intent.action.MANAGED_PROFILE_ADDED");
        intentFilter.addAction("android.intent.action.MANAGED_PROFILE_REMOVED");
        intentFilter.addAction("android.intent.action.MANAGED_PROFILE_AVAILABLE");
        intentFilter.addAction("android.intent.action.MANAGED_PROFILE_UNAVAILABLE");
        this.mContext.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, intentFilter, null, null);
    }

    public void addCallback(Callback callback) {
        this.mCallbacks.add(callback);
        if (this.mCallbacks.size() == 1) {
            setListening(true);
        }
        callback.onManagedProfileChanged();
    }

    public boolean hasActiveProfile() {
        boolean z = false;
        if (!this.mListening) {
            reloadManagedProfiles();
        }
        synchronized (this.mProfiles) {
            if (this.mProfiles.size() > 0) {
                z = true;
            }
        }
        return z;
    }

    public boolean isWorkModeEnabled() {
        if (!this.mListening) {
            reloadManagedProfiles();
        }
        synchronized (this.mProfiles) {
            Iterator<T> it = this.mProfiles.iterator();
            do {
                if (!it.hasNext()) {
                    return true;
                }
            } while (!((UserInfo) it.next()).isQuietModeEnabled());
            return false;
        }
    }

    public void removeCallback(Callback callback) {
        if (this.mCallbacks.remove(callback) && this.mCallbacks.size() == 0) {
            setListening(false);
        }
    }

    public void setWorkModeEnabled(boolean z) {
        synchronized (this.mProfiles) {
            for (UserInfo userInfo : this.mProfiles) {
                if (!z) {
                    this.mUserManager.setQuietModeEnabled(userInfo.id, true);
                } else if (!this.mUserManager.trySetQuietModeDisabled(userInfo.id, null)) {
                    ((StatusBarManager) this.mContext.getSystemService("statusbar")).collapsePanels();
                }
            }
        }
    }
}
