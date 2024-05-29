package com.android.systemui.usb;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.IUsbManager;
import android.os.Bundle;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.Toast;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
/* loaded from: a.zip:com/android/systemui/usb/UsbDebuggingActivity.class */
public class UsbDebuggingActivity extends AlertActivity implements DialogInterface.OnClickListener {
    private CheckBox mAlwaysAllow;
    private UsbDisconnectedReceiver mDisconnectedReceiver;
    private String mKey;

    /* loaded from: a.zip:com/android/systemui/usb/UsbDebuggingActivity$UsbDisconnectedReceiver.class */
    private class UsbDisconnectedReceiver extends BroadcastReceiver {
        private final Activity mActivity;
        final UsbDebuggingActivity this$0;

        public UsbDisconnectedReceiver(UsbDebuggingActivity usbDebuggingActivity, Activity activity) {
            this.this$0 = usbDebuggingActivity;
            this.mActivity = activity;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("android.hardware.usb.action.USB_STATE".equals(intent.getAction()) && !intent.getBooleanExtra("connected", false)) {
                this.mActivity.finish();
            }
        }
    }

    @Override // android.content.DialogInterface.OnClickListener
    public void onClick(DialogInterface dialogInterface, int i) {
        boolean z = i == -1;
        boolean isChecked = z ? this.mAlwaysAllow.isChecked() : false;
        try {
            IUsbManager asInterface = IUsbManager.Stub.asInterface(ServiceManager.getService("usb"));
            if (z) {
                asInterface.allowUsbDebugging(isChecked, this.mKey);
            } else {
                asInterface.denyUsbDebugging();
            }
        } catch (Exception e) {
            Log.e("UsbDebuggingActivity", "Unable to notify Usb service", e);
        }
        finish();
    }

    /* JADX WARN: Multi-variable type inference failed */
    public void onCreate(Bundle bundle) {
        Window window = getWindow();
        window.addPrivateFlags(524288);
        window.setType(2008);
        super.onCreate(bundle);
        if (SystemProperties.getInt("service.adb.tcp.port", 0) == 0) {
            this.mDisconnectedReceiver = new UsbDisconnectedReceiver(this, this);
        }
        Intent intent = getIntent();
        String stringExtra = intent.getStringExtra("fingerprints");
        this.mKey = intent.getStringExtra("key");
        if (stringExtra == null || this.mKey == null) {
            finish();
            return;
        }
        AlertController.AlertParams alertParams = this.mAlertParams;
        alertParams.mTitle = getString(2131493327);
        alertParams.mMessage = getString(2131493328, new Object[]{stringExtra});
        alertParams.mPositiveButtonText = getString(17039370);
        alertParams.mNegativeButtonText = getString(17039360);
        alertParams.mPositiveButtonListener = this;
        alertParams.mNegativeButtonListener = this;
        View inflate = LayoutInflater.from(alertParams.mContext).inflate(17367089, (ViewGroup) null);
        this.mAlwaysAllow = (CheckBox) inflate.findViewById(16909103);
        this.mAlwaysAllow.setText(getString(2131493329));
        alertParams.mView = inflate;
        setupAlert();
        this.mAlert.getButton(-1).setOnTouchListener(new View.OnTouchListener(this) { // from class: com.android.systemui.usb.UsbDebuggingActivity.1
            final UsbDebuggingActivity this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if ((motionEvent.getFlags() & 1) == 0 && (motionEvent.getFlags() & 2) == 0) {
                    return false;
                }
                if (motionEvent.getAction() == 1) {
                    Toast.makeText(view.getContext(), 2131493914, 0).show();
                    return true;
                }
                return true;
            }
        });
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
