package com.mediatek.systemui.qs.tiles.ext;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BenesseExtension;
import android.telephony.SubscriptionManager;
import android.util.Log;
import com.android.systemui.qs.QSTile;
import com.mediatek.systemui.PluginManager;
import com.mediatek.systemui.ext.IQuickSettingsPlugin;
import com.mediatek.systemui.statusbar.extcb.IconIdWrapper;
/* loaded from: a.zip:com/mediatek/systemui/qs/tiles/ext/DualSimSettingsTile.class */
public class DualSimSettingsTile extends QSTile<QSTile.BooleanState> {
    private static final Intent DUAL_SIM_SETTINGS = new Intent().setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$SimSettingsActivity"));
    private static final String TAG = "DualSimSettingsTile";
    private final IconIdWrapper mDisableIconIdWrapper;
    private final IconIdWrapper mEnableIconIdWrapper;
    private BroadcastReceiver mSimStateIntentReceiver;
    private CharSequence mTileLabel;

    public DualSimSettingsTile(QSTile.Host host) {
        super(host);
        this.mEnableIconIdWrapper = new IconIdWrapper();
        this.mDisableIconIdWrapper = new IconIdWrapper();
        this.mSimStateIntentReceiver = new BroadcastReceiver(this) { // from class: com.mediatek.systemui.qs.tiles.ext.DualSimSettingsTile.1
            final DualSimSettingsTile this$0;

            {
                this.this$0 = this;
            }

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d(DualSimSettingsTile.TAG, "onReceive action is " + action);
                if (action.equals("android.intent.action.SIM_STATE_CHANGED")) {
                    String stringExtra = intent.getStringExtra("ss");
                    Log.d(DualSimSettingsTile.TAG, "onReceive action is " + action + " stateExtra=" + stringExtra);
                    if ("ABSENT".equals(stringExtra)) {
                        this.this$0.handleRefreshState(false);
                    } else {
                        this.this$0.handleRefreshState(true);
                    }
                }
            }
        };
        registerSimStateReceiver();
    }

    private void registerSimStateReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
        this.mContext.registerReceiver(this.mSimStateIntentReceiver, intentFilter);
    }

    @Override // com.android.systemui.qs.QSTile
    public Intent getLongClickIntent() {
        return null;
    }

    @Override // com.android.systemui.qs.QSTile
    public int getMetricsCategory() {
        return 111;
    }

    @Override // com.android.systemui.qs.QSTile
    public CharSequence getTileLabel() {
        this.mTileLabel = PluginManager.getQuickSettingsPlugin(this.mContext).getTileLabel("dulsimsettings");
        return this.mTileLabel;
    }

    @Override // com.android.systemui.qs.QSTile
    protected void handleClick() {
        if (BenesseExtension.getDchaState() != 0) {
            return;
        }
        long defaultDataSubscriptionId = SubscriptionManager.getDefaultDataSubscriptionId();
        Log.d(TAG, "handleClick, " + DUAL_SIM_SETTINGS);
        DUAL_SIM_SETTINGS.putExtra("subscription", defaultDataSubscriptionId);
        this.mHost.startActivityDismissingKeyguard(DUAL_SIM_SETTINGS);
        refreshState();
    }

    @Override // com.android.systemui.qs.QSTile
    protected void handleLongClick() {
        handleClick();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSTile
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        Boolean bool = (Boolean) obj;
        Log.d(TAG, "handleUpdateState,  simInserted=" + bool);
        IQuickSettingsPlugin quickSettingsPlugin = PluginManager.getQuickSettingsPlugin(this.mContext);
        if (bool == null || !bool.booleanValue()) {
            booleanState.label = quickSettingsPlugin.customizeDualSimSettingsTile(true, this.mEnableIconIdWrapper, "");
            booleanState.icon = QsIconWrapper.get(this.mEnableIconIdWrapper.getIconId(), this.mEnableIconIdWrapper);
        } else {
            booleanState.label = quickSettingsPlugin.customizeDualSimSettingsTile(false, this.mDisableIconIdWrapper, "");
            booleanState.icon = QsIconWrapper.get(this.mDisableIconIdWrapper.getIconId(), this.mDisableIconIdWrapper);
        }
        this.mTileLabel = booleanState.label;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.android.systemui.qs.QSTile
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.QSTile
    public void setListening(boolean z) {
    }
}
