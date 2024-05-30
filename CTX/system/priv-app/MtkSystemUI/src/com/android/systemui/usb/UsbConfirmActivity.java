package com.android.systemui.usb;

import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.hardware.usb.IUsbManager;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import com.android.systemui.R;
/* loaded from: classes.dex */
public class UsbConfirmActivity extends AlertActivity implements DialogInterface.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private UsbAccessory mAccessory;
    private CheckBox mAlwaysUse;
    private TextView mClearDefaultHint;
    private UsbDevice mDevice;
    private UsbDisconnectedReceiver mDisconnectedReceiver;
    private ResolveInfo mResolveInfo;

    /* JADX WARN: Multi-variable type inference failed */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Intent intent = getIntent();
        this.mDevice = (UsbDevice) intent.getParcelableExtra("device");
        this.mAccessory = (UsbAccessory) intent.getParcelableExtra("accessory");
        this.mResolveInfo = (ResolveInfo) intent.getParcelableExtra("rinfo");
        String charSequence = this.mResolveInfo.loadLabel(getPackageManager()).toString();
        AlertController.AlertParams alertParams = this.mAlertParams;
        alertParams.mTitle = charSequence;
        if (this.mDevice == null) {
            alertParams.mMessage = getString(R.string.usb_accessory_confirm_prompt, new Object[]{charSequence, this.mAccessory.getDescription()});
            this.mDisconnectedReceiver = new UsbDisconnectedReceiver((Activity) this, this.mAccessory);
        } else {
            alertParams.mMessage = getString(R.string.usb_device_confirm_prompt, new Object[]{charSequence, this.mDevice.getProductName()});
            this.mDisconnectedReceiver = new UsbDisconnectedReceiver((Activity) this, this.mDevice);
        }
        alertParams.mPositiveButtonText = getString(17039370);
        alertParams.mNegativeButtonText = getString(17039360);
        alertParams.mPositiveButtonListener = this;
        alertParams.mNegativeButtonListener = this;
        alertParams.mView = ((LayoutInflater) getSystemService("layout_inflater")).inflate(17367090, (ViewGroup) null);
        this.mAlwaysUse = (CheckBox) alertParams.mView.findViewById(16908711);
        if (this.mDevice == null) {
            this.mAlwaysUse.setText(getString(R.string.always_use_accessory, new Object[]{charSequence, this.mAccessory.getDescription()}));
        } else {
            this.mAlwaysUse.setText(getString(R.string.always_use_device, new Object[]{charSequence, this.mDevice.getProductName()}));
        }
        this.mAlwaysUse.setOnCheckedChangeListener(this);
        this.mClearDefaultHint = (TextView) alertParams.mView.findViewById(16908796);
        this.mClearDefaultHint.setVisibility(8);
        setupAlert();
    }

    protected void onDestroy() {
        if (this.mDisconnectedReceiver != null) {
            unregisterReceiver(this.mDisconnectedReceiver);
        }
        super.onDestroy();
    }

    @Override // android.content.DialogInterface.OnClickListener
    public void onClick(DialogInterface dialogInterface, int i) {
        Intent intent;
        if (i == -1) {
            try {
                IUsbManager asInterface = IUsbManager.Stub.asInterface(ServiceManager.getService("usb"));
                int i2 = this.mResolveInfo.activityInfo.applicationInfo.uid;
                int myUserId = UserHandle.myUserId();
                boolean isChecked = this.mAlwaysUse.isChecked();
                if (this.mDevice != null) {
                    intent = new Intent("android.hardware.usb.action.USB_DEVICE_ATTACHED");
                    intent.putExtra("device", this.mDevice);
                    asInterface.grantDevicePermission(this.mDevice, i2);
                    if (isChecked) {
                        asInterface.setDevicePackage(this.mDevice, this.mResolveInfo.activityInfo.packageName, myUserId);
                    } else {
                        asInterface.setDevicePackage(this.mDevice, (String) null, myUserId);
                    }
                } else if (this.mAccessory != null) {
                    intent = new Intent("android.hardware.usb.action.USB_ACCESSORY_ATTACHED");
                    intent.putExtra("accessory", this.mAccessory);
                    asInterface.grantAccessoryPermission(this.mAccessory, i2);
                    if (isChecked) {
                        asInterface.setAccessoryPackage(this.mAccessory, this.mResolveInfo.activityInfo.packageName, myUserId);
                    } else {
                        asInterface.setAccessoryPackage(this.mAccessory, (String) null, myUserId);
                    }
                } else {
                    intent = null;
                }
                intent.addFlags(268435456);
                intent.setComponent(new ComponentName(this.mResolveInfo.activityInfo.packageName, this.mResolveInfo.activityInfo.name));
                startActivityAsUser(intent, new UserHandle(myUserId));
            } catch (Exception e) {
                Log.e("UsbConfirmActivity", "Unable to start activity", e);
            }
        }
        finish();
    }

    @Override // android.widget.CompoundButton.OnCheckedChangeListener
    public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
        if (this.mClearDefaultHint == null) {
            return;
        }
        if (z) {
            this.mClearDefaultHint.setVisibility(0);
        } else {
            this.mClearDefaultHint.setVisibility(8);
        }
    }
}
