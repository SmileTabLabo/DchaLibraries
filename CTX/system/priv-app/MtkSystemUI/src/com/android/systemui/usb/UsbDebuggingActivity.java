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
import android.util.EventLog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.Toast;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import com.android.systemui.R;
/* loaded from: classes.dex */
public class UsbDebuggingActivity extends AlertActivity implements DialogInterface.OnClickListener {
    private CheckBox mAlwaysAllow;
    private UsbDisconnectedReceiver mDisconnectedReceiver;
    private String mKey;

    /* JADX WARN: Multi-variable type inference failed */
    public void onCreate(Bundle bundle) {
        Window window = getWindow();
        window.addPrivateFlags(524288);
        window.setType(2008);
        super.onCreate(bundle);
        if (SystemProperties.getInt("service.adb.tcp.port", 0) == 0) {
            this.mDisconnectedReceiver = new UsbDisconnectedReceiver(this);
        }
        Intent intent = getIntent();
        String stringExtra = intent.getStringExtra("fingerprints");
        this.mKey = intent.getStringExtra("key");
        if (stringExtra == null || this.mKey == null) {
            finish();
            return;
        }
        AlertController.AlertParams alertParams = this.mAlertParams;
        alertParams.mTitle = getString(R.string.usb_debugging_title);
        alertParams.mMessage = getString(R.string.usb_debugging_message, new Object[]{stringExtra});
        alertParams.mPositiveButtonText = getString(17039370);
        alertParams.mNegativeButtonText = getString(17039360);
        alertParams.mPositiveButtonListener = this;
        alertParams.mNegativeButtonListener = this;
        View inflate = LayoutInflater.from(alertParams.mContext).inflate(17367090, (ViewGroup) null);
        this.mAlwaysAllow = (CheckBox) inflate.findViewById(16908711);
        this.mAlwaysAllow.setText(getString(R.string.usb_debugging_always));
        alertParams.mView = inflate;
        setupAlert();
        this.mAlert.getButton(-1).setOnTouchListener(new View.OnTouchListener() { // from class: com.android.systemui.usb.-$$Lambda$UsbDebuggingActivity$XWt--qGCtWBJlTLnAvCSF7AuSg8
            @Override // android.view.View.OnTouchListener
            public final boolean onTouch(View view, MotionEvent motionEvent) {
                return UsbDebuggingActivity.lambda$onCreate$0(view, motionEvent);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ boolean lambda$onCreate$0(View view, MotionEvent motionEvent) {
        if ((motionEvent.getFlags() & 1) == 0 && (motionEvent.getFlags() & 2) == 0) {
            return false;
        }
        if (motionEvent.getAction() == 1) {
            EventLog.writeEvent(1397638484, "62187985");
            Toast.makeText(view.getContext(), (int) R.string.touch_filtered_warning, 0).show();
        }
        return true;
    }

    public void onWindowAttributesChanged(WindowManager.LayoutParams layoutParams) {
        super.onWindowAttributesChanged(layoutParams);
    }

    /* loaded from: classes.dex */
    private class UsbDisconnectedReceiver extends BroadcastReceiver {
        private final Activity mActivity;

        public UsbDisconnectedReceiver(Activity activity) {
            this.mActivity = activity;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("android.hardware.usb.action.USB_STATE".equals(intent.getAction()) && !intent.getBooleanExtra("connected", false)) {
                this.mActivity.finish();
            }
        }
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

    @Override // android.content.DialogInterface.OnClickListener
    public void onClick(DialogInterface dialogInterface, int i) {
        boolean z = false;
        boolean z2 = i == -1;
        if (z2 && this.mAlwaysAllow.isChecked()) {
            z = true;
        }
        try {
            IUsbManager asInterface = IUsbManager.Stub.asInterface(ServiceManager.getService("usb"));
            if (z2) {
                asInterface.allowUsbDebugging(z, this.mKey);
            } else {
                asInterface.denyUsbDebugging();
            }
        } catch (Exception e) {
            Log.e("UsbDebuggingActivity", "Unable to notify Usb service", e);
        }
        finish();
    }
}
