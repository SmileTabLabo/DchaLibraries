package com.android.systemui.usb;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemProperties;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
/* loaded from: a.zip:com/android/systemui/usb/UsbDebuggingSecondaryUserActivity.class */
public class UsbDebuggingSecondaryUserActivity extends AlertActivity implements DialogInterface.OnClickListener {
    private UsbDisconnectedReceiver mDisconnectedReceiver;

    /* loaded from: a.zip:com/android/systemui/usb/UsbDebuggingSecondaryUserActivity$UsbDisconnectedReceiver.class */
    private class UsbDisconnectedReceiver extends BroadcastReceiver {
        private final Activity mActivity;
        final UsbDebuggingSecondaryUserActivity this$0;

        public UsbDisconnectedReceiver(UsbDebuggingSecondaryUserActivity usbDebuggingSecondaryUserActivity, Activity activity) {
            this.this$0 = usbDebuggingSecondaryUserActivity;
            this.mActivity = activity;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (!"android.hardware.usb.action.USB_STATE".equals(intent.getAction()) || intent.getBooleanExtra("connected", false)) {
                return;
            }
            this.mActivity.finish();
        }
    }

    @Override // android.content.DialogInterface.OnClickListener
    public void onClick(DialogInterface dialogInterface, int i) {
        finish();
    }

    /* JADX WARN: Multi-variable type inference failed */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (SystemProperties.getInt("service.adb.tcp.port", 0) == 0) {
            this.mDisconnectedReceiver = new UsbDisconnectedReceiver(this, this);
        }
        AlertController.AlertParams alertParams = this.mAlertParams;
        alertParams.mTitle = getString(2131493330);
        alertParams.mMessage = getString(2131493331);
        alertParams.mPositiveButtonText = getString(17039370);
        alertParams.mPositiveButtonListener = this;
        setupAlert();
    }

    public void onStart() {
        super.onStart();
        registerReceiver(this.mDisconnectedReceiver, new IntentFilter("android.hardware.usb.action.USB_STATE"));
    }

    protected void onStop() {
        if (this.mDisconnectedReceiver != null) {
            unregisterReceiver(this.mDisconnectedReceiver);
        }
        super.onStop();
    }
}
