package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.Intent;
import android.os.BenesseExtension;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import com.android.internal.logging.MetricsLogger;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.systemui.qs.QSDetailItems;
import com.android.systemui.qs.QSTile;
import com.android.systemui.statusbar.policy.BluetoothController;
import java.util.ArrayList;
import java.util.Collection;
/* loaded from: a.zip:com/android/systemui/qs/tiles/BluetoothTile.class */
public class BluetoothTile extends QSTile<QSTile.BooleanState> {
    private static final Intent BLUETOOTH_SETTINGS = new Intent("android.settings.BLUETOOTH_SETTINGS");
    private final BluetoothController.Callback mCallback;
    private final BluetoothController mController;
    private final BluetoothDetailAdapter mDetailAdapter;

    /* renamed from: com.android.systemui.qs.tiles.BluetoothTile$1  reason: invalid class name */
    /* loaded from: a.zip:com/android/systemui/qs/tiles/BluetoothTile$1.class */
    class AnonymousClass1 implements BluetoothController.Callback {
        final BluetoothTile this$0;

        AnonymousClass1(BluetoothTile bluetoothTile) {
            this.this$0 = bluetoothTile;
        }

        @Override // com.android.systemui.statusbar.policy.BluetoothController.Callback
        public void onBluetoothDevicesChanged() {
            this.this$0.mUiHandler.post(new Runnable(this) { // from class: com.android.systemui.qs.tiles.BluetoothTile.1.1
                final AnonymousClass1 this$1;

                {
                    this.this$1 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$1.this$0.mDetailAdapter.updateItems();
                }
            });
            this.this$0.refreshState();
        }

        @Override // com.android.systemui.statusbar.policy.BluetoothController.Callback
        public void onBluetoothStateChange(boolean z) {
            this.this$0.refreshState();
        }
    }

    /* loaded from: a.zip:com/android/systemui/qs/tiles/BluetoothTile$BluetoothDetailAdapter.class */
    private final class BluetoothDetailAdapter implements QSTile.DetailAdapter, QSDetailItems.Callback {
        private QSDetailItems mItems;
        final BluetoothTile this$0;

        private BluetoothDetailAdapter(BluetoothTile bluetoothTile) {
            this.this$0 = bluetoothTile;
        }

        /* synthetic */ BluetoothDetailAdapter(BluetoothTile bluetoothTile, BluetoothDetailAdapter bluetoothDetailAdapter) {
            this(bluetoothTile);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void updateItems() {
            if (this.mItems == null) {
                return;
            }
            ArrayList arrayList = new ArrayList();
            Collection<CachedBluetoothDevice> devices = this.this$0.mController.getDevices();
            if (devices != null) {
                for (CachedBluetoothDevice cachedBluetoothDevice : devices) {
                    if (cachedBluetoothDevice.getBondState() != 10) {
                        QSDetailItems.Item item = new QSDetailItems.Item();
                        item.icon = 2130837704;
                        item.line1 = cachedBluetoothDevice.getName();
                        int maxConnectionState = cachedBluetoothDevice.getMaxConnectionState();
                        if (maxConnectionState == 2) {
                            item.icon = 2130837700;
                            item.line2 = this.this$0.mContext.getString(2131493565);
                            item.canDisconnect = true;
                        } else if (maxConnectionState == 1) {
                            item.icon = 2130837701;
                            item.line2 = this.this$0.mContext.getString(2131493566);
                        }
                        item.tag = cachedBluetoothDevice;
                        arrayList.add(item);
                    }
                }
            }
            this.mItems.setItems((QSDetailItems.Item[]) arrayList.toArray(new QSDetailItems.Item[arrayList.size()]));
        }

        @Override // com.android.systemui.qs.QSTile.DetailAdapter
        public View createDetailView(Context context, View view, ViewGroup viewGroup) {
            this.mItems = QSDetailItems.convertOrInflate(context, view, viewGroup);
            this.mItems.setTagSuffix("Bluetooth");
            this.mItems.setEmptyState(2130837702, 2131493529);
            this.mItems.setCallback(this);
            updateItems();
            setItemsVisible(((QSTile.BooleanState) this.this$0.mState).value);
            return this.mItems;
        }

        @Override // com.android.systemui.qs.QSTile.DetailAdapter
        public int getMetricsCategory() {
            return 150;
        }

        @Override // com.android.systemui.qs.QSTile.DetailAdapter
        public Intent getSettingsIntent() {
            if (BenesseExtension.getDchaState() != 0) {
                return null;
            }
            return BluetoothTile.BLUETOOTH_SETTINGS;
        }

        @Override // com.android.systemui.qs.QSTile.DetailAdapter
        public CharSequence getTitle() {
            return this.this$0.mContext.getString(2131493526);
        }

        @Override // com.android.systemui.qs.QSTile.DetailAdapter
        public Boolean getToggleState() {
            return Boolean.valueOf(((QSTile.BooleanState) this.this$0.mState).value);
        }

        @Override // com.android.systemui.qs.QSDetailItems.Callback
        public void onDetailItemClick(QSDetailItems.Item item) {
            CachedBluetoothDevice cachedBluetoothDevice;
            if (item == null || item.tag == null || (cachedBluetoothDevice = (CachedBluetoothDevice) item.tag) == null || cachedBluetoothDevice.getMaxConnectionState() != 0) {
                return;
            }
            this.this$0.mController.connect(cachedBluetoothDevice);
        }

        @Override // com.android.systemui.qs.QSDetailItems.Callback
        public void onDetailItemDisconnect(QSDetailItems.Item item) {
            CachedBluetoothDevice cachedBluetoothDevice;
            if (item == null || item.tag == null || (cachedBluetoothDevice = (CachedBluetoothDevice) item.tag) == null) {
                return;
            }
            this.this$0.mController.disconnect(cachedBluetoothDevice);
        }

        public void setItemsVisible(boolean z) {
            if (this.mItems == null) {
                return;
            }
            this.mItems.setItemsVisible(z);
        }

        @Override // com.android.systemui.qs.QSTile.DetailAdapter
        public void setToggleState(boolean z) {
            MetricsLogger.action(this.this$0.mContext, 154, z);
            this.this$0.mController.setBluetoothEnabled(z);
            this.this$0.showDetail(false);
        }
    }

    public BluetoothTile(QSTile.Host host) {
        super(host);
        this.mCallback = new AnonymousClass1(this);
        this.mController = host.getBluetoothController();
        this.mDetailAdapter = new BluetoothDetailAdapter(this, null);
    }

    @Override // com.android.systemui.qs.QSTile
    protected String composeChangeAnnouncement() {
        return ((QSTile.BooleanState) this.mState).value ? this.mContext.getString(2131493472) : this.mContext.getString(2131493471);
    }

    @Override // com.android.systemui.qs.QSTile
    public QSTile.DetailAdapter getDetailAdapter() {
        return this.mDetailAdapter;
    }

    @Override // com.android.systemui.qs.QSTile
    public Intent getLongClickIntent() {
        if (BenesseExtension.getDchaState() != 0) {
            return null;
        }
        return new Intent("android.settings.BLUETOOTH_SETTINGS");
    }

    @Override // com.android.systemui.qs.QSTile
    public int getMetricsCategory() {
        return 113;
    }

    @Override // com.android.systemui.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(2131493526);
    }

    @Override // com.android.systemui.qs.QSTile
    protected void handleClick() {
        if (!this.mController.canConfigBluetooth() && BenesseExtension.getDchaState() == 0) {
            this.mHost.startActivityDismissingKeyguard(new Intent("android.settings.BLUETOOTH_SETTINGS"));
            return;
        }
        if (!((QSTile.BooleanState) this.mState).value) {
            ((QSTile.BooleanState) this.mState).value = true;
            this.mController.setBluetoothEnabled(true);
        }
        showDetail(true);
    }

    @Override // com.android.systemui.qs.QSTile
    protected void handleSecondaryClick() {
        boolean booleanValue = Boolean.valueOf(((QSTile.BooleanState) this.mState).value).booleanValue();
        MetricsLogger.action(this.mContext, getMetricsCategory(), !booleanValue);
        this.mController.setBluetoothEnabled(!booleanValue);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSTile
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        boolean isBluetoothEnabled = this.mController.isBluetoothEnabled();
        boolean isBluetoothConnected = this.mController.isBluetoothConnected();
        boolean isBluetoothConnecting = this.mController.isBluetoothConnecting();
        booleanState.value = isBluetoothEnabled;
        booleanState.autoMirrorDrawable = false;
        booleanState.minimalContentDescription = this.mContext.getString(2131493466);
        if (isBluetoothEnabled) {
            booleanState.label = null;
            if (isBluetoothConnected) {
                booleanState.icon = QSTile.ResourceIcon.get(2130837700);
                booleanState.label = this.mController.getLastDeviceName();
                booleanState.contentDescription = this.mContext.getString(2131493384, booleanState.label);
                booleanState.minimalContentDescription += "," + booleanState.contentDescription;
            } else if (isBluetoothConnecting) {
                booleanState.icon = QSTile.ResourceIcon.get(2130837701);
                booleanState.contentDescription = this.mContext.getString(2131493469);
                booleanState.label = this.mContext.getString(2131493526);
                booleanState.minimalContentDescription += "," + booleanState.contentDescription;
            } else {
                booleanState.icon = QSTile.ResourceIcon.get(2130837704);
                booleanState.contentDescription = this.mContext.getString(2131493468) + "," + this.mContext.getString(2131493394);
                booleanState.minimalContentDescription += "," + this.mContext.getString(2131493394);
            }
            if (TextUtils.isEmpty(booleanState.label)) {
                booleanState.label = this.mContext.getString(2131493526);
            }
        } else {
            booleanState.icon = QSTile.ResourceIcon.get(2130837703);
            booleanState.label = this.mContext.getString(2131493526);
            booleanState.contentDescription = this.mContext.getString(2131493467);
        }
        String str = booleanState.label;
        if (isBluetoothConnected) {
            str = this.mContext.getString(2131493384, booleanState.label);
            booleanState.dualLabelContentDescription = str;
        }
        booleanState.dualLabelContentDescription = str;
        booleanState.contentDescription += "," + this.mContext.getString(2131493911, getTileLabel());
        booleanState.expandedAccessibilityClassName = Button.class.getName();
        booleanState.minimalAccessibilityClassName = Switch.class.getName();
    }

    @Override // com.android.systemui.qs.QSTile
    public boolean isAvailable() {
        return this.mController.isBluetoothSupported();
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.android.systemui.qs.QSTile
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.QSTile
    public void setListening(boolean z) {
        if (z) {
            this.mController.addStateChangedCallback(this.mCallback);
        } else {
            this.mController.removeStateChangedCallback(this.mCallback);
        }
    }
}
