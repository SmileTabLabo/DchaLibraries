package com.android.systemui.usb;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
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
import com.android.internal.app.IntentForwarderActivity;
import com.android.internal.app.ResolverActivity;
import com.android.systemui.R;
import java.util.ArrayList;
import java.util.Iterator;
/* loaded from: classes.dex */
public class UsbResolverActivity extends ResolverActivity {
    private UsbAccessory mAccessory;
    private UsbDevice mDevice;
    private UsbDisconnectedReceiver mDisconnectedReceiver;
    private ResolveInfo mForwardResolveInfo;
    private Intent mOtherProfileIntent;

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
        ArrayList arrayList = new ArrayList(intent.getParcelableArrayListExtra("rlist"));
        ArrayList<? extends Parcelable> arrayList2 = new ArrayList<>();
        this.mForwardResolveInfo = null;
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            ResolveInfo resolveInfo = (ResolveInfo) it.next();
            if (resolveInfo.getComponentInfo().name.equals(IntentForwarderActivity.FORWARD_INTENT_TO_MANAGED_PROFILE)) {
                this.mForwardResolveInfo = resolveInfo;
            } else if (UserHandle.getUserId(resolveInfo.activityInfo.applicationInfo.uid) != UserHandle.myUserId()) {
                it.remove();
                arrayList2.add(resolveInfo);
            }
        }
        this.mDevice = (UsbDevice) intent2.getParcelableExtra("device");
        if (this.mDevice != null) {
            this.mDisconnectedReceiver = new UsbDisconnectedReceiver((Activity) this, this.mDevice);
        } else {
            this.mAccessory = (UsbAccessory) intent2.getParcelableExtra("accessory");
            if (this.mAccessory == null) {
                Log.e("UsbResolverActivity", "no device or accessory");
                finish();
                return;
            }
            this.mDisconnectedReceiver = new UsbDisconnectedReceiver((Activity) this, this.mAccessory);
        }
        if (this.mForwardResolveInfo != null) {
            if (arrayList2.size() > 1) {
                this.mOtherProfileIntent = new Intent(intent);
                this.mOtherProfileIntent.putParcelableArrayListExtra("rlist", arrayList2);
            } else {
                this.mOtherProfileIntent = new Intent((Context) this, (Class<?>) UsbConfirmActivity.class);
                this.mOtherProfileIntent.putExtra("rinfo", (Parcelable) arrayList2.get(0));
                if (this.mDevice != null) {
                    this.mOtherProfileIntent.putExtra("device", this.mDevice);
                }
                if (this.mAccessory != null) {
                    this.mOtherProfileIntent.putExtra("accessory", this.mAccessory);
                }
            }
        }
        super.onCreate(bundle, intent2, getResources().getText(17039618), (Intent[]) null, arrayList, true);
        CheckBox checkBox = (CheckBox) findViewById(16908711);
        if (checkBox != null) {
            if (this.mDevice == null) {
                checkBox.setText(R.string.always_use_accessory);
            } else {
                checkBox.setText(R.string.always_use_device);
            }
        }
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
        if (resolveInfo == this.mForwardResolveInfo) {
            startActivityAsUser(this.mOtherProfileIntent, null, UserHandle.of(this.mForwardResolveInfo.targetUserId));
            return true;
        }
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
                targetInfo.startAsUser(this, (Bundle) null, UserHandle.of(myUserId));
            } catch (ActivityNotFoundException e) {
                Log.e("UsbResolverActivity", "startActivity failed", e);
            }
        } catch (RemoteException e2) {
            Log.e("UsbResolverActivity", "onIntentSelected failed", e2);
        }
        return true;
    }
}
