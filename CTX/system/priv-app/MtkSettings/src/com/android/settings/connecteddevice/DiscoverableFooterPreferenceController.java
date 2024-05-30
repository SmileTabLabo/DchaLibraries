package com.android.settings.connecteddevice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.preference.PreferenceScreen;
import android.text.BidiFormatter;
import android.text.TextUtils;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.bluetooth.AlwaysDiscoverable;
import com.android.settings.bluetooth.Utils;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settingslib.bluetooth.LocalBluetoothAdapter;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.android.settingslib.widget.FooterPreference;
import com.android.settingslib.widget.FooterPreferenceMixin;
import com.android.settingslib.wifi.AccessPoint;
/* loaded from: classes.dex */
public class DiscoverableFooterPreferenceController extends BasePreferenceController implements LifecycleObserver, OnPause, OnResume {
    private static final String KEY = "discoverable_footer_preference";
    private AlwaysDiscoverable mAlwaysDiscoverable;
    @VisibleForTesting
    BroadcastReceiver mBluetoothChangedReceiver;
    private FooterPreferenceMixin mFooterPreferenceMixin;
    private LocalBluetoothAdapter mLocalAdapter;
    private LocalBluetoothManager mLocalManager;
    private FooterPreference mPreference;

    public DiscoverableFooterPreferenceController(Context context) {
        super(context, KEY);
        this.mLocalManager = Utils.getLocalBtManager(context);
        if (this.mLocalManager == null) {
            return;
        }
        this.mLocalAdapter = this.mLocalManager.getBluetoothAdapter();
        this.mAlwaysDiscoverable = new AlwaysDiscoverable(context, this.mLocalAdapter);
        initReceiver();
    }

    private void initReceiver() {
        this.mBluetoothChangedReceiver = new BroadcastReceiver() { // from class: com.android.settings.connecteddevice.DiscoverableFooterPreferenceController.1
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.bluetooth.adapter.action.STATE_CHANGED")) {
                    DiscoverableFooterPreferenceController.this.updateFooterPreferenceTitle(intent.getIntExtra("android.bluetooth.adapter.extra.STATE", AccessPoint.UNREACHABLE_RSSI));
                }
            }
        };
    }

    public void init(DashboardFragment dashboardFragment) {
        this.mFooterPreferenceMixin = new FooterPreferenceMixin(dashboardFragment, dashboardFragment.getLifecycle());
    }

    @VisibleForTesting
    void init(FooterPreferenceMixin footerPreferenceMixin, FooterPreference footerPreference, AlwaysDiscoverable alwaysDiscoverable) {
        this.mFooterPreferenceMixin = footerPreferenceMixin;
        this.mPreference = footerPreference;
        this.mAlwaysDiscoverable = alwaysDiscoverable;
    }

    @Override // com.android.settings.core.BasePreferenceController, com.android.settingslib.core.AbstractPreferenceController
    public void displayPreference(PreferenceScreen preferenceScreen) {
        super.displayPreference(preferenceScreen);
        addFooterPreference(preferenceScreen);
    }

    @Override // com.android.settings.core.BasePreferenceController
    public int getAvailabilityStatus() {
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.bluetooth")) {
            return 0;
        }
        return 2;
    }

    private void addFooterPreference(PreferenceScreen preferenceScreen) {
        this.mPreference = this.mFooterPreferenceMixin.createFooterPreference();
        this.mPreference.setKey(KEY);
        preferenceScreen.addPreference(this.mPreference);
    }

    @Override // com.android.settingslib.core.lifecycle.events.OnResume
    public void onResume() {
        this.mContext.registerReceiver(this.mBluetoothChangedReceiver, new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED"));
        this.mAlwaysDiscoverable.start();
        updateFooterPreferenceTitle(this.mLocalAdapter.getState());
    }

    @Override // com.android.settingslib.core.lifecycle.events.OnPause
    public void onPause() {
        this.mContext.unregisterReceiver(this.mBluetoothChangedReceiver);
        this.mAlwaysDiscoverable.stop();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateFooterPreferenceTitle(int i) {
        if (i == 12) {
            this.mPreference.setTitle(getPreferenceTitle());
        } else {
            this.mPreference.setTitle(R.string.bluetooth_off_footer);
        }
    }

    private CharSequence getPreferenceTitle() {
        String name = this.mLocalAdapter.getName();
        if (TextUtils.isEmpty(name)) {
            return null;
        }
        return TextUtils.expandTemplate(this.mContext.getText(R.string.bluetooth_device_name_summary), BidiFormatter.getInstance().unicodeWrap(name));
    }
}
