package com.android.systemui.qs.tiles;

import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Switch;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.Prefs;
import com.android.systemui.qs.QSTile;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import com.android.systemui.statusbar.policy.DataSaverController;
/* loaded from: a.zip:com/android/systemui/qs/tiles/DataSaverTile.class */
public class DataSaverTile extends QSTile<QSTile.BooleanState> implements DataSaverController.Listener {
    private final DataSaverController mDataSaverController;

    public DataSaverTile(QSTile.Host host) {
        super(host);
        this.mDataSaverController = host.getNetworkController().getDataSaverController();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void toggleDataSaver() {
        ((QSTile.BooleanState) this.mState).value = !this.mDataSaverController.isDataSaverEnabled();
        MetricsLogger.action(this.mContext, getMetricsCategory(), ((QSTile.BooleanState) this.mState).value);
        this.mDataSaverController.setDataSaverEnabled(((QSTile.BooleanState) this.mState).value);
        refreshState(Boolean.valueOf(((QSTile.BooleanState) this.mState).value));
    }

    @Override // com.android.systemui.qs.QSTile
    protected String composeChangeAnnouncement() {
        return ((QSTile.BooleanState) this.mState).value ? this.mContext.getString(2131493496) : this.mContext.getString(2131493495);
    }

    @Override // com.android.systemui.qs.QSTile
    public Intent getLongClickIntent() {
        return CellularTile.CELLULAR_SETTINGS;
    }

    @Override // com.android.systemui.qs.QSTile
    public int getMetricsCategory() {
        return 284;
    }

    @Override // com.android.systemui.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(2131493852);
    }

    @Override // com.android.systemui.qs.QSTile
    protected void handleClick() {
        if (((QSTile.BooleanState) this.mState).value || Prefs.getBoolean(this.mContext, "QsDataSaverDialogShown", false)) {
            toggleDataSaver();
            return;
        }
        SystemUIDialog systemUIDialog = new SystemUIDialog(this.mContext);
        systemUIDialog.setTitle(17040815);
        systemUIDialog.setMessage(17040814);
        systemUIDialog.setPositiveButton(17040816, new DialogInterface.OnClickListener(this) { // from class: com.android.systemui.qs.tiles.DataSaverTile.1
            final DataSaverTile this$0;

            {
                this.this$0 = this;
            }

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                this.this$0.toggleDataSaver();
            }
        });
        systemUIDialog.setNegativeButton(17039360, null);
        systemUIDialog.setShowForAllUsers(true);
        systemUIDialog.show();
        Prefs.putBoolean(this.mContext, "QsDataSaverDialogShown", true);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSTile
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        booleanState.value = obj instanceof Boolean ? ((Boolean) obj).booleanValue() : this.mDataSaverController.isDataSaverEnabled();
        booleanState.label = this.mContext.getString(2131493852);
        booleanState.contentDescription = booleanState.label;
        booleanState.icon = QSTile.ResourceIcon.get(booleanState.value ? 2130837636 : 2130837637);
        String name = Switch.class.getName();
        booleanState.expandedAccessibilityClassName = name;
        booleanState.minimalAccessibilityClassName = name;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.android.systemui.qs.QSTile
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.statusbar.policy.DataSaverController.Listener
    public void onDataSaverChanged(boolean z) {
        refreshState(Boolean.valueOf(z));
    }

    @Override // com.android.systemui.qs.QSTile
    public void setListening(boolean z) {
        if (z) {
            this.mDataSaverController.addListener(this);
        } else {
            this.mDataSaverController.remListener(this);
        }
    }
}
