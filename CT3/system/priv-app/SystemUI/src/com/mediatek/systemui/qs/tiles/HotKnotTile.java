package com.mediatek.systemui.qs.tiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BenesseExtension;
import android.util.Log;
import com.android.systemui.qs.QSTile;
import com.mediatek.hotknot.HotKnotAdapter;
import com.mediatek.systemui.statusbar.policy.HotKnotController;
/* loaded from: a.zip:com/mediatek/systemui/qs/tiles/HotKnotTile.class */
public class HotKnotTile extends QSTile<QSTile.BooleanState> {
    private final HotKnotController mController;
    private final QSTile<QSTile.BooleanState>.AnimationIcon mDisable;
    private final QSTile<QSTile.BooleanState>.AnimationIcon mEnable;
    private boolean mListening;
    private final BroadcastReceiver mReceiver;

    public HotKnotTile(QSTile.Host host) {
        super(host);
        this.mEnable = new QSTile.AnimationIcon(this, 2130837787, 2130837784);
        this.mDisable = new QSTile.AnimationIcon(this, 2130837785, 2130837786);
        this.mReceiver = new BroadcastReceiver(this) { // from class: com.mediatek.systemui.qs.tiles.HotKnotTile.1
            final HotKnotTile this$0;

            {
                this.this$0 = this;
            }

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if ("com.mediatek.hotknot.action.ADAPTER_STATE_CHANGED".equals(intent.getAction())) {
                    Log.d("HotKnotTile", "HotKnotAdapter onReceive DAPTER_STATE_CHANGED");
                    this.this$0.refreshState();
                }
            }
        };
        this.mController = host.getHotKnotController();
    }

    @Override // com.android.systemui.qs.QSTile
    public Intent getLongClickIntent() {
        if (BenesseExtension.getDchaState() != 0) {
            return null;
        }
        Intent intent = new Intent("mediatek.settings.HOTKNOT_SETTINGS");
        intent.setFlags(335544320);
        return intent;
    }

    @Override // com.android.systemui.qs.QSTile
    public int getMetricsCategory() {
        return 111;
    }

    @Override // com.android.systemui.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(2131493268);
    }

    @Override // com.android.systemui.qs.QSTile
    protected void handleClick() {
        HotKnotAdapter adapter = this.mController.getAdapter();
        boolean z = !this.mController.isHotKnotOn();
        Log.d("HotKnotTile", "hotknot desiredState=" + z);
        if (z) {
            adapter.enable();
        } else {
            adapter.disable();
        }
    }

    @Override // com.android.systemui.qs.QSTile
    protected void handleLongClick() {
        if (BenesseExtension.getDchaState() != 0) {
            return;
        }
        Intent intent = new Intent("mediatek.settings.HOTKNOT_SETTINGS");
        intent.setFlags(335544320);
        this.mHost.startActivityDismissingKeyguard(intent);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSTile
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        booleanState.label = this.mContext.getString(2131493268);
        boolean isHotKnotOn = this.mController.isHotKnotOn();
        Log.d("HotKnotTile", "HotKnot UpdateState desiredState=" + isHotKnotOn);
        if (isHotKnotOn) {
            booleanState.icon = this.mEnable;
        } else {
            booleanState.icon = this.mDisable;
        }
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.android.systemui.qs.QSTile
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.QSTile
    public void setListening(boolean z) {
        if (this.mListening == z) {
            return;
        }
        this.mListening = z;
        if (!z) {
            this.mContext.unregisterReceiver(this.mReceiver);
            return;
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.mediatek.hotknot.action.ADAPTER_STATE_CHANGED");
        this.mContext.registerReceiver(this.mReceiver, intentFilter);
    }
}
