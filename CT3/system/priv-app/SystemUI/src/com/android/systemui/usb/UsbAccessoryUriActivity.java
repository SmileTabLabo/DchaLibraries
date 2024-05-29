package com.android.systemui.usb;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.usb.UsbAccessory;
import android.net.Uri;
import android.os.BenesseExtension;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.Log;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
/* loaded from: a.zip:com/android/systemui/usb/UsbAccessoryUriActivity.class */
public class UsbAccessoryUriActivity extends AlertActivity implements DialogInterface.OnClickListener {
    private UsbAccessory mAccessory;
    private Uri mUri;

    @Override // android.content.DialogInterface.OnClickListener
    public void onClick(DialogInterface dialogInterface, int i) {
        if (i == -1) {
            Intent intent = new Intent("android.intent.action.VIEW", this.mUri);
            intent.addCategory("android.intent.category.BROWSABLE");
            intent.addFlags(268435456);
            try {
                if (BenesseExtension.getDchaState() == 0) {
                    startActivityAsUser(intent, UserHandle.CURRENT);
                }
            } catch (ActivityNotFoundException e) {
                Log.e("UsbAccessoryUriActivity", "startActivity failed for " + this.mUri);
            }
        }
        finish();
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Intent intent = getIntent();
        this.mAccessory = (UsbAccessory) intent.getParcelableExtra("accessory");
        String stringExtra = intent.getStringExtra("uri");
        this.mUri = stringExtra == null ? null : Uri.parse(stringExtra);
        if (this.mUri == null) {
            Log.e("UsbAccessoryUriActivity", "could not parse Uri " + stringExtra);
            finish();
            return;
        }
        String scheme = this.mUri.getScheme();
        if (!"http".equals(scheme) && !"https".equals(scheme)) {
            Log.e("UsbAccessoryUriActivity", "Uri not http or https: " + this.mUri);
            finish();
            return;
        }
        AlertController.AlertParams alertParams = this.mAlertParams;
        alertParams.mTitle = this.mAccessory.getDescription();
        if (alertParams.mTitle == null || alertParams.mTitle.length() == 0) {
            alertParams.mTitle = getString(2131493323);
        }
        alertParams.mMessage = getString(2131493322, new Object[]{this.mUri});
        alertParams.mPositiveButtonText = getString(2131493324);
        alertParams.mNegativeButtonText = getString(17039360);
        alertParams.mPositiveButtonListener = this;
        alertParams.mNegativeButtonListener = this;
        setupAlert();
    }
}
