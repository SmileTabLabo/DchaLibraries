package com.mediatek.settings.sim;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SubscriptionManager;
import android.util.Log;
import java.util.Arrays;
/* loaded from: classes.dex */
public class SimHotSwapHandler {
    private Context mContext;
    private OnSimHotSwapListener mListener;
    private BroadcastReceiver mSubReceiver = new BroadcastReceiver() { // from class: com.mediatek.settings.sim.SimHotSwapHandler.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            SimHotSwapHandler.this.handleHotSwap();
        }
    };
    private int[] mSubscriptionIdListCache;
    private SubscriptionManager mSubscriptionManager;

    /* loaded from: classes.dex */
    public interface OnSimHotSwapListener {
        void onSimHotSwap();
    }

    public SimHotSwapHandler(Context context) {
        this.mContext = context;
        this.mSubscriptionManager = SubscriptionManager.from(context);
        this.mSubscriptionIdListCache = this.mSubscriptionManager.getActiveSubscriptionIdList();
        print("Cache list: ", this.mSubscriptionIdListCache);
    }

    public void registerOnSimHotSwap(OnSimHotSwapListener listener) {
        if (this.mContext == null) {
            return;
        }
        this.mContext.registerReceiver(this.mSubReceiver, new IntentFilter("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED"));
        this.mListener = listener;
    }

    public void unregisterOnSimHotSwap() {
        if (this.mContext != null) {
            this.mContext.unregisterReceiver(this.mSubReceiver);
        }
        this.mListener = null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleHotSwap() {
        int[] subscriptionIdListCurrent = this.mSubscriptionManager.getActiveSubscriptionIdList();
        print("handleHotSwap, current subId list: ", subscriptionIdListCurrent);
        boolean isEqual = Arrays.equals(this.mSubscriptionIdListCache, subscriptionIdListCurrent);
        Log.d("SimHotSwapHandler", "isEqual: " + isEqual);
        if (isEqual || this.mListener == null) {
            return;
        }
        this.mListener.onSimHotSwap();
    }

    private void print(String msg, int[] lists) {
        if (lists != null) {
            for (int i : lists) {
                Log.d("SimHotSwapHandler", msg + i);
            }
            return;
        }
        Log.d("SimHotSwapHandler", msg + "is null");
    }
}
