package com.android.systemui.usb;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.hardware.usb.IUsbManager;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Log;
import android.widget.CheckBox;
import com.android.internal.app.ResolverActivity;
/* loaded from: a.zip:com/android/systemui/usb/UsbResolverActivity.class */
public class UsbResolverActivity extends ResolverActivity {
    private UsbAccessory mAccessory;
    private UsbDevice mDevice;
    private UsbDisconnectedReceiver mDisconnectedReceiver;

    /* JADX WARN: Multi-variable type inference failed */
    protected void onCreate(Bundle bundle) {
        Intent intent = getIntent();
        Parcelable parcelableExtra = intent.getParcelableExtra("android.intent.extra.INTENT");
        if (!(parcelableExtra instanceof Intent)) {
            Log.w("UsbResolverActivity", "Target is not an intent: " + parcelableExtra);
            finish();
            return;
        }
        Intent intent2 = (Intent) parcelableExtra;
        super.onCreate(bundle, intent2, getResources().getText(17040265), (Intent[]) null, intent.getParcelableArrayListExtra("rlist"), true);
        CheckBox checkBox = (CheckBox) findViewById(16909103);
        if (checkBox != null) {
            if (this.mDevice == null) {
                checkBox.setText(2131493326);
            } else {
                checkBox.setText(2131493325);
            }
        }
        this.mDevice = (UsbDevice) intent2.getParcelableExtra("device");
        if (this.mDevice != null) {
            this.mDisconnectedReceiver = new UsbDisconnectedReceiver((Activity) this, this.mDevice);
            return;
        }
        this.mAccessory = (UsbAccessory) intent2.getParcelableExtra("accessory");
        if (this.mAccessory != null) {
            this.mDisconnectedReceiver = new UsbDisconnectedReceiver((Activity) this, this.mAccessory);
            return;
        }
        Log.e("UsbResolverActivity", "no device or accessory");
        finish();
    }

    protected void onDestroy() {
        if (this.mDisconnectedReceiver != null) {
            unregisterReceiver(this.mDisconnectedReceiver);
        }
        super.onDestroy();
    }

    /* JADX WARN: Multi-variable type inference failed */
    protected boolean onTargetSelected(ResolverActivity.TargetInfo targetInfo, boolean z) {
        ResolveInfo resolveInfo = targetInfo.getResolveInfo();
        try {
            IUsbManager asInterface = IUsbManager.Stub.asInterface(ServiceManager.getService("usb"));
            int i = resolveInfo.activityInfo.applicationInfo.uid;
            int myUserId = UserHandle.myUserId();
            if (this.mDevice != null) {
                asInterface.grantDevicePermission(this.mDevice, i);
                if (z) {
                    asInterface.setDevicePackage(this.mDevice, resolveInfo.activityInfo.packageName, myUserId);
                } else {
                    asInterface.setDevicePackage(this.mDevice, (String) null, myUserId);
                }
            } else if (this.mAccessory != null) {
                asInterface.grantAccessoryPermission(this.mAccessory, i);
                if (z) {
                    asInterface.setAccessoryPackage(this.mAccessory, resolveInfo.activityInfo.packageName, myUserId);
                } else {
                    asInterface.setAccessoryPackage(this.mAccessory, (String) null, myUserId);
                }
            }
            try {
                targetInfo.startAsUser(this, (Bundle) null, new UserHandle(myUserId));
                return true;
            } catch (ActivityNotFoundException e) {
                Log.e("UsbResolverActivity", "startActivity failed", e);
                return true;
            }
        } catch (RemoteException e2) {
            Log.e("UsbResolverActivity", "onIntentSelected failed", e2);
            return true;
        }
    }
}
