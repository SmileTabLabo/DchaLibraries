package com.android.systemui.qs.tiles;

import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Switch;
import com.android.systemui.Dependency;
import com.android.systemui.Prefs;
import com.android.systemui.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import com.android.systemui.statusbar.policy.DataSaverController;
import com.android.systemui.statusbar.policy.NetworkController;
/* loaded from: classes.dex */
public class DataSaverTile extends QSTileImpl<QSTile.BooleanState> implements DataSaverController.Listener {
    private final DataSaverController mDataSaverController;

    public DataSaverTile(QSHost qSHost) {
        super(qSHost);
        this.mDataSaverController = ((NetworkController) Dependency.get(NetworkController.class)).getDataSaverController();
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
        if (z) {
            this.mDataSaverController.addCallback(this);
        } else {
            this.mDataSaverController.removeCallback(this);
        }
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return CellularTile.getCellularSettingIntent();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleClick() {
        if (((QSTile.BooleanState) this.mState).value || Prefs.getBoolean(this.mContext, "QsDataSaverDialogShown", false)) {
            toggleDataSaver();
            return;
        }
        SystemUIDialog systemUIDialog = new SystemUIDialog(this.mContext);
        systemUIDialog.setTitle(17039737);
        systemUIDialog.setMessage(17039735);
        systemUIDialog.setPositiveButton(17039736, new DialogInterface.OnClickListener() { // from class: com.android.systemui.qs.tiles.-$$Lambda$DataSaverTile$7vpE4nfIgph7ByTloh1_igU2EhI
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                DataSaverTile.this.toggleDataSaver();
            }
        });
        systemUIDialog.setNegativeButton(17039360, null);
        systemUIDialog.setShowForAllUsers(true);
        systemUIDialog.show();
        Prefs.putBoolean(this.mContext, "QsDataSaverDialogShown", true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void toggleDataSaver() {
        ((QSTile.BooleanState) this.mState).value = !this.mDataSaverController.isDataSaverEnabled();
        this.mDataSaverController.setDataSaverEnabled(((QSTile.BooleanState) this.mState).value);
        refreshState(Boolean.valueOf(((QSTile.BooleanState) this.mState).value));
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.data_saver);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        booleanState.value = obj instanceof Boolean ? ((Boolean) obj).booleanValue() : this.mDataSaverController.isDataSaverEnabled();
        booleanState.state = booleanState.value ? 2 : 1;
        booleanState.label = this.mContext.getString(R.string.data_saver);
        booleanState.contentDescription = booleanState.label;
        booleanState.icon = QSTileImpl.ResourceIcon.get(booleanState.value ? R.drawable.ic_data_saver : R.drawable.ic_data_saver_off);
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return 284;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected String composeChangeAnnouncement() {
        if (((QSTile.BooleanState) this.mState).value) {
            return this.mContext.getString(R.string.accessibility_quick_settings_data_saver_changed_on);
        }
        return this.mContext.getString(R.string.accessibility_quick_settings_data_saver_changed_off);
    }

    @Override // com.android.systemui.statusbar.policy.DataSaverController.Listener
    public void onDataSaverChanged(boolean z) {
        refreshState(Boolean.valueOf(z));
    }
}
