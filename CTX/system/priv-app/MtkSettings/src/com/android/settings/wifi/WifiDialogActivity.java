package com.android.settings.wifi;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import com.android.settings.SetupWizardUtils;
import com.android.settings.wifi.WifiDialog;
import com.android.settingslib.wifi.AccessPoint;
import com.android.setupwizardlib.util.WizardManagerHelper;
/* loaded from: classes.dex */
public class WifiDialogActivity extends Activity implements DialogInterface.OnDismissListener, WifiDialog.WifiDialogListener {
    static final String KEY_CONNECT_FOR_CALLER = "connect_for_caller";

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        Intent intent = getIntent();
        if (WizardManagerHelper.isSetupWizardIntent(intent)) {
            setTheme(SetupWizardUtils.getTransparentTheme(intent));
        }
        super.onCreate(bundle);
        Bundle bundleExtra = intent.getBundleExtra("access_point_state");
        AccessPoint accessPoint = null;
        if (bundleExtra != null) {
            accessPoint = new AccessPoint(this, bundleExtra);
        }
        WifiDialog createModal = WifiDialog.createModal(this, this, accessPoint, 1);
        createModal.show();
        createModal.setOnDismissListener(this);
    }

    @Override // android.app.Activity
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override // com.android.settings.wifi.WifiDialog.WifiDialogListener
    public void onForget(WifiDialog wifiDialog) {
        WifiManager wifiManager = (WifiManager) getSystemService(WifiManager.class);
        AccessPoint accessPoint = wifiDialog.getController().getAccessPoint();
        if (accessPoint != null) {
            if (!accessPoint.isSaved()) {
                if (accessPoint.getNetworkInfo() != null && accessPoint.getNetworkInfo().getState() != NetworkInfo.State.DISCONNECTED) {
                    wifiManager.disableEphemeralNetwork(AccessPoint.convertToQuotedString(accessPoint.getSsidStr()));
                } else {
                    Log.e("WifiDialogActivity", "Failed to forget invalid network " + accessPoint.getConfig());
                }
            } else {
                wifiManager.forget(accessPoint.getConfig().networkId, null);
            }
        }
        Intent intent = new Intent();
        if (accessPoint != null) {
            Bundle bundle = new Bundle();
            accessPoint.saveWifiState(bundle);
            intent.putExtra("access_point_state", bundle);
        }
        setResult(2);
        finish();
    }

    @Override // com.android.settings.wifi.WifiDialog.WifiDialogListener
    public void onSubmit(WifiDialog wifiDialog) {
        NetworkInfo networkInfo;
        WifiConfiguration config = wifiDialog.getController().getConfig();
        AccessPoint accessPoint = wifiDialog.getController().getAccessPoint();
        WifiManager wifiManager = (WifiManager) getSystemService(WifiManager.class);
        if (getIntent().getBooleanExtra(KEY_CONNECT_FOR_CALLER, true)) {
            if (config == null) {
                if (accessPoint != null && accessPoint.isSaved()) {
                    wifiManager.connect(accessPoint.getConfig(), null);
                }
            } else {
                wifiManager.save(config, null);
                if (accessPoint != null && ((networkInfo = accessPoint.getNetworkInfo()) == null || !networkInfo.isConnected())) {
                    wifiManager.connect(config, null);
                }
            }
        }
        Intent intent = new Intent();
        if (accessPoint != null) {
            Bundle bundle = new Bundle();
            accessPoint.saveWifiState(bundle);
            intent.putExtra("access_point_state", bundle);
        }
        if (config != null) {
            intent.putExtra("wifi_configuration", config);
        }
        setResult(1, intent);
        finish();
    }

    @Override // android.content.DialogInterface.OnDismissListener
    public void onDismiss(DialogInterface dialogInterface) {
        finish();
    }
}
