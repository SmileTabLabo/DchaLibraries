package com.android.systemui.tuner;

import android.content.Intent;
import com.android.systemui.Prefs;
import com.android.systemui.qs.QSTile;
import com.android.systemui.statusbar.policy.NightModeController;
/* loaded from: a.zip:com/android/systemui/tuner/NightModeTile.class */
public class NightModeTile extends QSTile<QSTile.State> implements NightModeController.Listener {
    private final NightModeController mNightModeController;

    public NightModeTile(QSTile.Host host) {
        super(host);
        this.mNightModeController = host.getNightModeController();
    }

    @Override // com.android.systemui.qs.QSTile
    public Intent getLongClickIntent() {
        return new Intent(this.mContext, TunerActivity.class).putExtra("show_night_mode", true);
    }

    @Override // com.android.systemui.qs.QSTile
    public int getMetricsCategory() {
        return 267;
    }

    @Override // com.android.systemui.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(2131493780);
    }

    @Override // com.android.systemui.qs.QSTile
    protected void handleClick() {
        this.mNightModeController.setNightMode(!this.mNightModeController.isEnabled());
        refreshState();
    }

    @Override // com.android.systemui.qs.QSTile
    protected void handleUpdateState(QSTile.State state, Object obj) {
        state.icon = QSTile.ResourceIcon.get(this.mNightModeController.isEnabled() ? 2130837685 : 2130837686);
        state.label = this.mContext.getString(2131493780);
        state.contentDescription = this.mContext.getString(2131493780);
    }

    @Override // com.android.systemui.qs.QSTile
    public boolean isAvailable() {
        boolean z = false;
        if (Prefs.getBoolean(this.mContext, "QsNightAdded", false)) {
            z = TunerService.isTunerEnabled(this.mContext);
        }
        return z;
    }

    @Override // com.android.systemui.qs.QSTile
    public QSTile.State newTileState() {
        return new QSTile.State();
    }

    @Override // com.android.systemui.statusbar.policy.NightModeController.Listener
    public void onNightModeChanged() {
        refreshState();
    }

    @Override // com.android.systemui.qs.QSTile
    public void setListening(boolean z) {
        if (!z) {
            this.mNightModeController.removeListener(this);
            return;
        }
        this.mNightModeController.addListener(this);
        refreshState();
    }
}
