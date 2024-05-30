package com.android.systemui.usb;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.XmlResourceParser;
import android.hardware.usb.AccessoryFilter;
import android.hardware.usb.DeviceFilter;
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
import com.android.internal.util.XmlUtils;
import com.android.systemui.R;
/* loaded from: classes.dex */
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
            String charSequence = packageManager.getApplicationInfo(this.mPackageName, 0).loadLabel(packageManager).toString();
            AlertController.AlertParams alertParams = this.mAlertParams;
            alertParams.mTitle = charSequence;
            if (this.mDevice == null) {
                alertParams.mMessage = getString(R.string.usb_accessory_permission_prompt, new Object[]{charSequence, this.mAccessory.getDescription()});
                this.mDisconnectedReceiver = new UsbDisconnectedReceiver((Activity) this, this.mAccessory);
            } else {
                alertParams.mMessage = getString(R.string.usb_device_permission_prompt, new Object[]{charSequence, this.mDevice.getProductName()});
                this.mDisconnectedReceiver = new UsbDisconnectedReceiver((Activity) this, this.mDevice);
            }
            alertParams.mPositiveButtonText = getString(17039370);
            alertParams.mNegativeButtonText = getString(17039360);
            alertParams.mPositiveButtonListener = this;
            alertParams.mNegativeButtonListener = this;
            try {
                PackageInfo packageInfo = packageManager.getPackageInfo(this.mPackageName, 129);
                if ((this.mDevice != null && canBeDefault(this.mDevice, packageInfo)) || (this.mAccessory != null && canBeDefault(this.mAccessory, packageInfo))) {
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
                }
            } catch (PackageManager.NameNotFoundException e) {
            }
            setupAlert();
        } catch (PackageManager.NameNotFoundException e2) {
            Log.e("UsbPermissionActivity", "unable to look up package name", e2);
            finish();
        }
    }

    private boolean canBeDefault(UsbDevice usbDevice, PackageInfo packageInfo) {
        ActivityInfo[] activityInfoArr = packageInfo.activities;
        if (activityInfoArr != null) {
            for (ActivityInfo activityInfo : activityInfoArr) {
                try {
                    XmlResourceParser loadXmlMetaData = activityInfo.loadXmlMetaData(getPackageManager(), "android.hardware.usb.action.USB_DEVICE_ATTACHED");
                    if (loadXmlMetaData != null) {
                        XmlUtils.nextElement(loadXmlMetaData);
                        while (loadXmlMetaData.getEventType() != 1) {
                            if ("usb-device".equals(loadXmlMetaData.getName()) && DeviceFilter.read(loadXmlMetaData).matches(usbDevice)) {
                                if (loadXmlMetaData != null) {
                                    $closeResource(null, loadXmlMetaData);
                                }
                                return true;
                            }
                            XmlUtils.nextElement(loadXmlMetaData);
                        }
                        if (loadXmlMetaData != null) {
                            $closeResource(null, loadXmlMetaData);
                        }
                    } else if (loadXmlMetaData != null) {
                        $closeResource(null, loadXmlMetaData);
                    }
                } catch (Exception e) {
                    Log.w("UsbPermissionActivity", "Unable to load component info " + activityInfo.toString(), e);
                }
            }
        }
        return false;
    }

    private static /* synthetic */ void $closeResource(Throwable th, AutoCloseable autoCloseable) {
        if (th == null) {
            autoCloseable.close();
            return;
        }
        try {
            autoCloseable.close();
        } catch (Throwable th2) {
            th.addSuppressed(th2);
        }
    }

    private boolean canBeDefault(UsbAccessory usbAccessory, PackageInfo packageInfo) {
        ActivityInfo[] activityInfoArr = packageInfo.activities;
        if (activityInfoArr != null) {
            for (ActivityInfo activityInfo : activityInfoArr) {
                try {
                    XmlResourceParser loadXmlMetaData = activityInfo.loadXmlMetaData(getPackageManager(), "android.hardware.usb.action.USB_ACCESSORY_ATTACHED");
                    if (loadXmlMetaData != null) {
                        XmlUtils.nextElement(loadXmlMetaData);
                        while (loadXmlMetaData.getEventType() != 1) {
                            if ("usb-accessory".equals(loadXmlMetaData.getName()) && AccessoryFilter.read(loadXmlMetaData).matches(usbAccessory)) {
                                if (loadXmlMetaData != null) {
                                    $closeResource(null, loadXmlMetaData);
                                }
                                return true;
                            }
                            XmlUtils.nextElement(loadXmlMetaData);
                        }
                        if (loadXmlMetaData != null) {
                            $closeResource(null, loadXmlMetaData);
                        }
                    } else if (loadXmlMetaData != null) {
                        $closeResource(null, loadXmlMetaData);
                    }
                } catch (Exception e) {
                    Log.w("UsbPermissionActivity", "Unable to load component info " + activityInfo.toString(), e);
                }
            }
        }
        return false;
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
                    if (this.mAlwaysUse != null && this.mAlwaysUse.isChecked()) {
                        asInterface.setDevicePackage(this.mDevice, this.mPackageName, UserHandle.getUserId(this.mUid));
                    }
                }
            }
            if (this.mAccessory != null) {
                intent.putExtra("accessory", this.mAccessory);
                if (this.mPermissionGranted) {
                    asInterface.grantAccessoryPermission(this.mAccessory, this.mUid);
                    if (this.mAlwaysUse != null && this.mAlwaysUse.isChecked()) {
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

    @Override // android.content.DialogInterface.OnClickListener
    public void onClick(DialogInterface dialogInterface, int i) {
        if (i == -1) {
            this.mPermissionGranted = true;
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
