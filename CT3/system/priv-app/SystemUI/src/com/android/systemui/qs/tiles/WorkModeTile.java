package com.android.systemui.qs.tiles;

import android.content.Intent;
import android.os.BenesseExtension;
import android.widget.Switch;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.qs.QSTile;
import com.android.systemui.statusbar.phone.ManagedProfileController;
/* loaded from: a.zip:com/android/systemui/qs/tiles/WorkModeTile.class */
public class WorkModeTile extends QSTile<QSTile.BooleanState> implements ManagedProfileController.Callback {
    private final QSTile<QSTile.BooleanState>.AnimationIcon mDisable;
    private final QSTile<QSTile.BooleanState>.AnimationIcon mEnable;
    private final ManagedProfileController mProfileController;

    public WorkModeTile(QSTile.Host host) {
        super(host);
        this.mEnable = new QSTile.AnimationIcon(this, 2130837795, 2130837792);
        this.mDisable = new QSTile.AnimationIcon(this, 2130837793, 2130837794);
        this.mProfileController = host.getManagedProfileController();
    }

    @Override // com.android.systemui.qs.QSTile
    protected String composeChangeAnnouncement() {
        return ((QSTile.BooleanState) this.mState).value ? this.mContext.getString(2131493494) : this.mContext.getString(2131493493);
    }

    @Override // com.android.systemui.qs.QSTile
    public Intent getLongClickIntent() {
        if (BenesseExtension.getDchaState() != 0) {
            return null;
        }
        return new Intent("android.settings.SYNC_SETTINGS");
    }

    @Override // com.android.systemui.qs.QSTile
    public int getMetricsCategory() {
        return 257;
    }

    @Override // com.android.systemui.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(2131493578);
    }

    @Override // com.android.systemui.qs.QSTile
    public void handleClick() {
        MetricsLogger.action(this.mContext, getMetricsCategory(), !((QSTile.BooleanState) this.mState).value);
        this.mProfileController.setWorkModeEnabled(!((QSTile.BooleanState) this.mState).value);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSTile
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        if (obj instanceof Boolean) {
            booleanState.value = ((Boolean) obj).booleanValue();
        } else {
            booleanState.value = this.mProfileController.isWorkModeEnabled();
        }
        booleanState.label = this.mContext.getString(2131493578);
        if (booleanState.value) {
            booleanState.icon = this.mEnable;
            booleanState.contentDescription = this.mContext.getString(2131493492);
        } else {
            booleanState.icon = this.mDisable;
            booleanState.contentDescription = this.mContext.getString(2131493491);
        }
        String name = Switch.class.getName();
        booleanState.expandedAccessibilityClassName = name;
        booleanState.minimalAccessibilityClassName = name;
    }

    @Override // com.android.systemui.qs.QSTile
    public boolean isAvailable() {
        return this.mProfileController.hasActiveProfile();
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.android.systemui.qs.QSTile
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.statusbar.phone.ManagedProfileController.Callback
    public void onManagedProfileChanged() {
        refreshState(Boolean.valueOf(this.mProfileController.isWorkModeEnabled()));
    }

    @Override // com.android.systemui.statusbar.phone.ManagedProfileController.Callback
    public void onManagedProfileRemoved() {
        this.mHost.removeTile(getTileSpec());
    }

    @Override // com.android.systemui.qs.QSTile
    public void setListening(boolean z) {
        if (z) {
            this.mProfileController.addCallback(this);
        } else {
            this.mProfileController.removeCallback(this);
        }
    }
}
