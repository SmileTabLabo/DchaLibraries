package com.android.settings.wifi.p2p;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pGroup;
import android.support.v7.preference.Preference;
/* loaded from: classes.dex */
public class WifiP2pPersistentGroup extends Preference {
    public WifiP2pGroup mGroup;

    public WifiP2pPersistentGroup(Context context, WifiP2pGroup group) {
        super(context);
        this.mGroup = group;
        setTitle(this.mGroup.getNetworkName());
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getNetworkId() {
        return this.mGroup.getNetworkId();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public String getGroupName() {
        return this.mGroup.getNetworkName();
    }
}
