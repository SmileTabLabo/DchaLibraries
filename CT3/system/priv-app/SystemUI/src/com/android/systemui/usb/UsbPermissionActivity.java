package com.android.systemui.usb;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.hardware.usb.IUsbManager;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.RemoteException;
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
/* loaded from: a.zip:com/android/systemui/usb/UsbPermissionActivity.class */
public class UsbPermissionActivity extends AlertActivity implements DialogInterface.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private UsbAccessory mAccessory;
    private CheckBox mAlwaysUse;
    private TextView mClearDefaultHint;
    private UsbDevice mDevice;
    private UsbDisconnectedReceiver mDisconnectedReceiver;
    private String mPackageName;
    private PendingIntent mPendingIntent;
    private boolean mPermissionGranted;
    private int mUid;

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

    @Override // android.content.DialogInterface.OnClickListener
    public void onClick(DialogInterface dialogInterface, int i) {
        if (i == -1) {
            this.mPermissionGranted = true;
        }
        finish();
    }

    /* JADX WARN: Multi-variable type inference failed */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Intent intent = getIntent();
        this.mDevice = (UsbDevice) intent.getParcelableExtra("device");
        this.mAccessory = (UsbAccessory) intent.getParcelableExtra("accessory");
        this.mPendingIntent = (PendingIntent) intent.getParcelableExtra("android.intent.extra.INTENT");
        this.mUid = intent.getIntExtra("android.intent.extra.UID", -1);
        this.mPackageName = intent.getStringExtra("package");
        PackageManager packageManager = getPackageManager();
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(this.mPackageName, 0);
            String charSequence = applicationInfo.loadLabel(packageManager).toString();
            AlertController.AlertParams alertParams = this.mAlertParams;
            alertParams.mIcon = applicationInfo.loadIcon(packageManager);
            alertParams.mTitle = charSequence;
            if (this.mDevice == null) {
                alertParams.mMessage = getString(2131493319, new Object[]{charSequence});
                this.mDisconnectedReceiver = new UsbDisconnectedReceiver((Activity) this, this.mAccessory);
            } else {
                alertParams.mMessage = getString(2131493318, new Object[]{charSequence});
                this.mDisconnectedReceiver = new UsbDisconnectedReceiver((Activity) this, this.mDevice);
            }
            alertParams.mPositiveButtonText = getString(17039370);
            alertParams.mNegativeButtonText = getString(17039360);
            alertParams.mPositiveButtonListener = this;
            alertParams.mNegativeButtonListener = this;
            alertParams.mView = ((LayoutInflater) getSystemService("layout_inflater")).inflate(17367089, (ViewGroup) null);
            this.mAlwaysUse = (CheckBox) alertParams.mView.findViewById(16909103);
            if (this.mDevice == null) {
                this.mAlwaysUse.setText(2131493326);
            } else {
                this.mAlwaysUse.setText(2131493325);
            }
            this.mAlwaysUse.setOnCheckedChangeListener(this);
            this.mClearDefaultHint = (TextView) alertParams.mView.findViewById(16909104);
            this.mClearDefaultHint.setVisibility(8);
            setupAlert();
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("UsbPermissionActivity", "unable to look up package name", e);
            finish();
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    public void onDestroy() {
        IUsbManager asInterface = IUsbManager.Stub.asInterface(ServiceManager.getService("usb"));
        Intent intent = new Intent();
        try {
            if (this.mDevice != null) {
                intent.putExtra("device", this.mDevice);
                if (this.mPermissionGranted) {
                    asInterface.grantDevicePermission(this.mDevice, this.mUid);
                    if (this.mAlwaysUse.isChecked()) {
                        asInterface.setDevicePackage(this.mDevice, this.mPackageName, UserHandle.getUserId(this.mUid));
                    }
                }
            }
            if (this.mAccessory != null) {
                intent.putExtra("accessory", this.mAccessory);
                if (this.mPermissionGranted) {
                    asInterface.grantAccessoryPermission(this.mAccessory, this.mUid);
                    if (this.mAlwaysUse.isChecked()) {
                        asInterface.setAccessoryPackage(this.mAccessory, this.mPackageName, UserHandle.getUserId(this.mUid));
                    }
                }
            }
            intent.putExtra("permission", this.mPermissionGranted);
            this.mPendingIntent.send((Context) this, 0, intent);
        } catch (PendingIntent.CanceledException e) {
            Log.w("UsbPermissionActivity", "PendingIntent was cancelled");
        } catch (RemoteException e2) {
            Log.e("UsbPermissionActivity", "IUsbService connection failed", e2);
        }
        if (this.mDisconnectedReceiver != null) {
            unregisterReceiver(this.mDisconnectedReceiver);
        }
        super.onDestroy();
    }
}
