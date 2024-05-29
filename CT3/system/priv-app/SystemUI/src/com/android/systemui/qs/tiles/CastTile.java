package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.Intent;
import android.hardware.display.WifiDisplayStatus;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.BenesseExtension;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.qs.QSDetailItems;
import com.android.systemui.qs.QSTile;
import com.android.systemui.statusbar.policy.CastController;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
/* loaded from: a.zip:com/android/systemui/qs/tiles/CastTile.class */
public class CastTile extends QSTile<QSTile.BooleanState> {
    private static final Intent CAST_SETTINGS = new Intent("android.settings.CAST_SETTINGS");
    private static final Intent WFD_SINK_SETTINGS = new Intent("mediatek.settings.WFD_SINK_SETTINGS");
    private final Callback mCallback;
    private final CastController mController;
    private final CastDetailAdapter mDetailAdapter;
    private final KeyguardMonitor mKeyguard;

    /* loaded from: a.zip:com/android/systemui/qs/tiles/CastTile$Callback.class */
    private final class Callback implements CastController.Callback, KeyguardMonitor.Callback {
        final CastTile this$0;

        private Callback(CastTile castTile) {
            this.this$0 = castTile;
        }

        /* synthetic */ Callback(CastTile castTile, Callback callback) {
            this(castTile);
        }

        @Override // com.android.systemui.statusbar.policy.CastController.Callback
        public void onCastDevicesChanged() {
            Log.d(this.this$0.TAG, "onCastDevicesChanged");
            this.this$0.refreshState();
        }

        @Override // com.android.systemui.statusbar.policy.KeyguardMonitor.Callback
        public void onKeyguardChanged() {
            Log.d(this.this$0.TAG, "onKeyguardChanged");
            this.this$0.refreshState();
        }

        @Override // com.android.systemui.statusbar.policy.CastController.Callback
        public void onWfdStatusChanged(WifiDisplayStatus wifiDisplayStatus, boolean z) {
            Log.d(this.this$0.TAG, "onWfdStatusChanged: " + wifiDisplayStatus.getActiveDisplayState());
            this.this$0.mDetailAdapter.wfdStatusChanged(wifiDisplayStatus, z);
            this.this$0.refreshState();
        }

        @Override // com.android.systemui.statusbar.policy.CastController.Callback
        public void onWifiP2pDeviceChanged(WifiP2pDevice wifiP2pDevice) {
            Log.d(this.this$0.TAG, "onWifiP2pDeviceChanged");
            this.this$0.mDetailAdapter.updateDeviceName(wifiP2pDevice);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/qs/tiles/CastTile$CastDetailAdapter.class */
    public final class CastDetailAdapter implements QSTile.DetailAdapter, QSDetailItems.Callback {
        private LinearLayout mDetailView;
        private QSDetailItems mItems;
        private boolean mSinkViewEnabledBak;
        private final LinkedHashMap<String, CastController.CastDevice> mVisibleOrder;
        private View mWfdSinkView;
        final CastTile this$0;

        private CastDetailAdapter(CastTile castTile) {
            this.this$0 = castTile;
            this.mVisibleOrder = new LinkedHashMap<>();
            this.mSinkViewEnabledBak = true;
        }

        /* synthetic */ CastDetailAdapter(CastTile castTile, CastDetailAdapter castDetailAdapter) {
            this(castTile);
        }

        private void handleWfdStateChanged(int i, boolean z) {
            switch (i) {
                case 0:
                    if (z) {
                        return;
                    }
                    setSinkViewEnabled(true);
                    setSinkViewChecked(false);
                    this.this$0.mController.updateWfdFloatMenu(false);
                    return;
                case 1:
                    if (z) {
                        return;
                    }
                    setSinkViewEnabled(false);
                    return;
                case 2:
                    if (z) {
                        return;
                    }
                    setSinkViewEnabled(false);
                    return;
                default:
                    return;
            }
        }

        private void setEnabledStateOnViews(View view, boolean z) {
            view.setEnabled(z);
            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                for (int childCount = viewGroup.getChildCount() - 1; childCount >= 0; childCount--) {
                    setEnabledStateOnViews(viewGroup.getChildAt(childCount), z);
                }
            }
        }

        private void setSinkViewChecked(boolean z) {
            if (this.mWfdSinkView == null) {
                return;
            }
            Log.d(this.this$0.TAG, "setSinkViewChecked: " + z);
            ((Switch) this.mWfdSinkView.findViewById(16908289)).setChecked(z);
        }

        private void setSinkViewEnabled(boolean z) {
            this.mSinkViewEnabledBak = z;
            if (this.mWfdSinkView == null) {
                return;
            }
            Log.d(this.this$0.TAG, "setSinkViewEnabled: " + z);
            setEnabledStateOnViews(this.mWfdSinkView, z);
        }

        private void setSinkViewVisible(boolean z) {
            if (this.mWfdSinkView == null) {
                return;
            }
            Log.d(this.this$0.TAG, "setSinkViewVisible: " + z);
            if (!z) {
                this.mWfdSinkView.setVisibility(8);
            } else if (this.mWfdSinkView.getVisibility() != 0) {
                updateDeviceName(this.this$0.mController.getWifiP2pDev());
                this.mWfdSinkView.setVisibility(0);
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void updateDeviceName(WifiP2pDevice wifiP2pDevice) {
            if (wifiP2pDevice == null || this.mWfdSinkView == null) {
                return;
            }
            Log.d(this.this$0.TAG, "updateDeviceName: " + wifiP2pDevice.deviceName);
            TextView textView = (TextView) this.mWfdSinkView.findViewById(16908310);
            if (TextUtils.isEmpty(wifiP2pDevice.deviceName)) {
                textView.setText(wifiP2pDevice.deviceAddress);
            } else {
                textView.setText(wifiP2pDevice.deviceName);
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void updateItems(Set<CastController.CastDevice> set) {
            QSDetailItems.Item[] itemArr;
            Log.d(this.this$0.TAG, "update items: " + set.size());
            if (this.mItems == null) {
                return;
            }
            QSDetailItems.Item[] itemArr2 = null;
            if (set != null) {
                if (set.isEmpty()) {
                    itemArr2 = null;
                } else {
                    Iterator<T> it = set.iterator();
                    while (true) {
                        itemArr = null;
                        if (!it.hasNext()) {
                            break;
                        }
                        CastController.CastDevice castDevice = (CastController.CastDevice) it.next();
                        if (castDevice.state == 2) {
                            QSDetailItems.Item item = new QSDetailItems.Item();
                            item.icon = 2130837712;
                            item.line1 = this.this$0.getDeviceName(castDevice);
                            item.line2 = this.this$0.mContext.getString(2131493565);
                            item.tag = castDevice;
                            item.canDisconnect = true;
                            itemArr = new QSDetailItems.Item[]{item};
                            break;
                        }
                    }
                    itemArr2 = itemArr;
                    if (itemArr == null) {
                        for (CastController.CastDevice castDevice2 : set) {
                            this.mVisibleOrder.put(castDevice2.id, castDevice2);
                        }
                        QSDetailItems.Item[] itemArr3 = new QSDetailItems.Item[set.size()];
                        int i = 0;
                        Iterator<T> it2 = this.mVisibleOrder.keySet().iterator();
                        while (true) {
                            itemArr2 = itemArr3;
                            if (!it2.hasNext()) {
                                break;
                            }
                            CastController.CastDevice castDevice3 = this.mVisibleOrder.get((String) it2.next());
                            if (set.contains(castDevice3)) {
                                QSDetailItems.Item item2 = new QSDetailItems.Item();
                                item2.icon = 2130837711;
                                item2.line1 = this.this$0.getDeviceName(castDevice3);
                                if (castDevice3.state == 1) {
                                    item2.line2 = this.this$0.mContext.getString(2131493566);
                                }
                                item2.tag = castDevice3;
                                itemArr3[i] = item2;
                                i++;
                            }
                        }
                    }
                }
            }
            this.mItems.setItems(itemArr2);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void updateSinkView() {
            if (this.mWfdSinkView == null) {
                return;
            }
            Log.d(this.this$0.TAG, "updateSinkView summary");
            TextView textView = (TextView) this.mWfdSinkView.findViewById(16908304);
            textView.post(new Runnable(this, textView) { // from class: com.android.systemui.qs.tiles.CastTile.CastDetailAdapter.3
                final CastDetailAdapter this$1;
                final TextView val$summary;

                {
                    this.this$1 = this;
                    this.val$summary = textView;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.val$summary.setText(2131493269);
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void wfdStatusChanged(WifiDisplayStatus wifiDisplayStatus, boolean z) {
            boolean isNeedShowWfdSink = this.this$0.mController.isNeedShowWfdSink();
            setSinkViewVisible(isNeedShowWfdSink);
            handleWfdStateChanged(isNeedShowWfdSink ? wifiDisplayStatus.getActiveDisplayState() : 0, z);
        }

        @Override // com.android.systemui.qs.QSTile.DetailAdapter
        public View createDetailView(Context context, View view, ViewGroup viewGroup) {
            if (this.this$0.mController.isWfdSinkSupported()) {
                this.mItems = QSDetailItems.convertOrInflate(context, this.mItems, viewGroup);
            } else {
                this.mItems = QSDetailItems.convertOrInflate(context, view, viewGroup);
            }
            this.mItems.setTagSuffix("Cast");
            if (view == null) {
                Log.d(this.this$0.TAG, "addOnAttachStateChangeListener");
                this.mItems.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener(this) { // from class: com.android.systemui.qs.tiles.CastTile.CastDetailAdapter.1
                    final CastDetailAdapter this$1;

                    {
                        this.this$1 = this;
                    }

                    @Override // android.view.View.OnAttachStateChangeListener
                    public void onViewAttachedToWindow(View view2) {
                        Log.d(this.this$1.this$0.TAG, "onViewAttachedToWindow");
                    }

                    @Override // android.view.View.OnAttachStateChangeListener
                    public void onViewDetachedFromWindow(View view2) {
                        Log.d(this.this$1.this$0.TAG, "onViewDetachedFromWindow");
                        this.this$1.mVisibleOrder.clear();
                    }
                });
            }
            this.mItems.setEmptyState(2130837710, 2131493558);
            this.mItems.setCallback(this);
            updateItems(this.this$0.mController.getCastDevices());
            this.this$0.mController.setDiscovering(true);
            if (this.this$0.mController.isWfdSinkSupported()) {
                Log.d(this.this$0.TAG, "add WFD sink view: " + (this.mWfdSinkView == null));
                if (this.mWfdSinkView == null) {
                    LayoutInflater from = LayoutInflater.from(context);
                    this.mWfdSinkView = from.inflate(2130968770, viewGroup, false);
                    from.inflate(2130968771, (ViewGroup) this.mWfdSinkView.findViewById(16908312));
                    ImageView imageView = (ImageView) this.mWfdSinkView.findViewById(16908294);
                    if (context.getResources().getBoolean(17956953)) {
                        imageView.setImageResource(2130837828);
                    } else {
                        imageView.setImageResource(2130837829);
                    }
                    ((TextView) this.mWfdSinkView.findViewById(16908304)).setText(2131493269);
                    this.mWfdSinkView.setOnClickListener(new View.OnClickListener(this) { // from class: com.android.systemui.qs.tiles.CastTile.CastDetailAdapter.2
                        final CastDetailAdapter this$1;

                        {
                            this.this$1 = this;
                        }

                        @Override // android.view.View.OnClickListener
                        public void onClick(View view2) {
                            Switch r0 = (Switch) view2.findViewById(16908289);
                            boolean isChecked = r0.isChecked();
                            if (!isChecked) {
                                this.this$1.this$0.getHost().startActivityDismissingKeyguard(CastTile.WFD_SINK_SETTINGS);
                            }
                            r0.setChecked(!isChecked);
                        }
                    });
                }
                if (view instanceof LinearLayout) {
                    this.mDetailView = (LinearLayout) view;
                    updateSinkView();
                } else {
                    this.mDetailView = new LinearLayout(context);
                    this.mDetailView.setOrientation(1);
                    ViewGroup viewGroup2 = (ViewGroup) this.mWfdSinkView.getParent();
                    if (viewGroup2 != null) {
                        Log.d(this.this$0.TAG, "mWfdSinkView needs remove from parent: " + viewGroup2.toString());
                        viewGroup2.removeView(this.mWfdSinkView);
                    }
                    ViewGroup viewGroup3 = (ViewGroup) this.mItems.getParent();
                    if (viewGroup3 != null) {
                        Log.d(this.this$0.TAG, "mItems needs remove from parent: " + viewGroup3.toString());
                        viewGroup3.removeView(this.mItems);
                    }
                    this.mDetailView.addView(this.mWfdSinkView);
                    View view2 = new View(context);
                    view2.setLayoutParams(new ViewGroup.LayoutParams(-1, context.getResources().getDimensionPixelSize(2131689837)));
                    view2.setBackgroundColor(context.getResources().getColor(2131558523));
                    this.mDetailView.addView(view2);
                    this.mDetailView.addView(this.mItems);
                }
                updateDeviceName(this.this$0.mController.getWifiP2pDev());
                setSinkViewVisible(this.this$0.mController.isNeedShowWfdSink());
                setSinkViewEnabled(this.mSinkViewEnabledBak);
            }
            return this.mDetailView != null ? this.mDetailView : this.mItems;
        }

        @Override // com.android.systemui.qs.QSTile.DetailAdapter
        public int getMetricsCategory() {
            return 151;
        }

        @Override // com.android.systemui.qs.QSTile.DetailAdapter
        public Intent getSettingsIntent() {
            if (BenesseExtension.getDchaState() != 0) {
                return null;
            }
            return CastTile.CAST_SETTINGS;
        }

        @Override // com.android.systemui.qs.QSTile.DetailAdapter
        public CharSequence getTitle() {
            return this.this$0.mContext.getString(2131493554);
        }

        @Override // com.android.systemui.qs.QSTile.DetailAdapter
        public Boolean getToggleState() {
            return null;
        }

        @Override // com.android.systemui.qs.QSDetailItems.Callback
        public void onDetailItemClick(QSDetailItems.Item item) {
            if (item == null || item.tag == null) {
                return;
            }
            MetricsLogger.action(this.this$0.mContext, 157);
            CastController.CastDevice castDevice = (CastController.CastDevice) item.tag;
            Log.d(this.this$0.TAG, "onDetailItemClick: " + castDevice.name);
            this.this$0.mController.startCasting(castDevice);
            this.this$0.mController.updateWfdFloatMenu(true);
        }

        @Override // com.android.systemui.qs.QSDetailItems.Callback
        public void onDetailItemDisconnect(QSDetailItems.Item item) {
            if (item == null || item.tag == null) {
                return;
            }
            MetricsLogger.action(this.this$0.mContext, 158);
            CastController.CastDevice castDevice = (CastController.CastDevice) item.tag;
            Log.d(this.this$0.TAG, "onDetailItemDisconnect: " + castDevice.name);
            this.this$0.mController.stopCasting(castDevice);
            this.this$0.mController.updateWfdFloatMenu(false);
        }

        @Override // com.android.systemui.qs.QSTile.DetailAdapter
        public void setToggleState(boolean z) {
        }
    }

    public CastTile(QSTile.Host host) {
        super(host);
        this.mCallback = new Callback(this, null);
        this.mController = host.getCastController();
        this.mDetailAdapter = new CastDetailAdapter(this, null);
        this.mKeyguard = host.getKeyguardMonitor();
        this.mController.setListening(true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String getDeviceName(CastController.CastDevice castDevice) {
        return castDevice.name != null ? castDevice.name : this.mContext.getString(2131493556);
    }

    @Override // com.android.systemui.qs.QSTile
    protected String composeChangeAnnouncement() {
        if (((QSTile.BooleanState) this.mState).value) {
            return null;
        }
        return this.mContext.getString(2131493490);
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
        return new Intent("android.settings.CAST_SETTINGS");
    }

    @Override // com.android.systemui.qs.QSTile
    public int getMetricsCategory() {
        return 114;
    }

    @Override // com.android.systemui.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(2131493554);
    }

    @Override // com.android.systemui.qs.QSTile
    protected void handleClick() {
        if (this.mKeyguard.isSecure() && !this.mKeyguard.canSkipBouncer()) {
            this.mHost.startRunnableDismissingKeyguard(new Runnable(this) { // from class: com.android.systemui.qs.tiles.CastTile.1
                final CastTile this$0;

                {
                    this.this$0 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    MetricsLogger.action(this.this$0.mContext, this.this$0.getMetricsCategory());
                    this.this$0.showDetail(true);
                    this.this$0.mHost.openPanels();
                }
            });
            return;
        }
        MetricsLogger.action(this.mContext, getMetricsCategory());
        showDetail(true);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSTile
    public void handleDestroy() {
        super.handleDestroy();
        if (this.mController == null) {
            return;
        }
        Log.d(this.TAG, "handle destroy");
        this.mController.setListening(false);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSTile
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        booleanState.label = this.mContext.getString(2131493554);
        booleanState.contentDescription = booleanState.label;
        booleanState.value = false;
        booleanState.autoMirrorDrawable = false;
        Set<CastController.CastDevice> castDevices = this.mController.getCastDevices();
        boolean z = false;
        for (CastController.CastDevice castDevice : castDevices) {
            if (castDevice.state == 2) {
                booleanState.value = true;
                booleanState.label = getDeviceName(castDevice);
                booleanState.contentDescription += "," + this.mContext.getString(2131493385, booleanState.label);
            } else if (castDevice.state == 1) {
                z = true;
            }
        }
        if (!booleanState.value && z) {
            booleanState.label = this.mContext.getString(2131493566);
        }
        booleanState.icon = QSTile.ResourceIcon.get(booleanState.value ? 2130837712 : 2130837711);
        this.mDetailAdapter.updateItems(castDevices);
        String name = Button.class.getName();
        booleanState.expandedAccessibilityClassName = name;
        booleanState.minimalAccessibilityClassName = name;
        booleanState.contentDescription += "," + this.mContext.getString(2131493910);
        this.mDetailAdapter.updateSinkView();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSTile
    public void handleUserSwitch(int i) {
        super.handleUserSwitch(i);
        if (this.mController == null) {
            return;
        }
        this.mController.setCurrentUserId(i);
    }

    @Override // com.android.systemui.qs.QSTile
    public boolean isAvailable() {
        return this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.direct");
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.android.systemui.qs.QSTile
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.QSTile
    public void setListening(boolean z) {
        if (this.mController == null) {
            return;
        }
        Log.d(this.TAG, "setListening " + z);
        if (z) {
            this.mController.addCallback(this.mCallback);
            this.mKeyguard.addCallback(this.mCallback);
            return;
        }
        this.mController.setDiscovering(false);
        this.mController.removeCallback(this.mCallback);
        this.mKeyguard.removeCallback(this.mCallback);
    }
}
