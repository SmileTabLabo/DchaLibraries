package com.android.systemui.media;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.projection.IMediaProjection;
import android.media.projection.IMediaProjectionManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.BidiFormatter;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
/* loaded from: a.zip:com/android/systemui/media/MediaProjectionPermissionActivity.class */
public class MediaProjectionPermissionActivity extends Activity implements DialogInterface.OnClickListener, CompoundButton.OnCheckedChangeListener, DialogInterface.OnCancelListener {
    private AlertDialog mDialog;
    private String mPackageName;
    private boolean mPermanentGrant;
    private IMediaProjectionManager mService;
    private int mUid;

    private Intent getMediaProjectionIntent(int i, String str, boolean z) throws RemoteException {
        IMediaProjection createProjection = this.mService.createProjection(i, str, 0, z);
        Intent intent = new Intent();
        intent.putExtra("android.media.projection.extra.EXTRA_MEDIA_PROJECTION", createProjection.asBinder());
        return intent;
    }

    @Override // android.content.DialogInterface.OnCancelListener
    public void onCancel(DialogInterface dialogInterface) {
        finish();
    }

    @Override // android.widget.CompoundButton.OnCheckedChangeListener
    public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
        this.mPermanentGrant = z;
    }

    @Override // android.content.DialogInterface.OnClickListener
    public void onClick(DialogInterface dialogInterface, int i) {
        try {
            if (i == -1) {
                try {
                    setResult(-1, getMediaProjectionIntent(this.mUid, this.mPackageName, this.mPermanentGrant));
                } catch (RemoteException e) {
                    Log.e("MediaProjectionPermissionActivity", "Error granting projection permission", e);
                    setResult(0);
                    if (this.mDialog != null) {
                        this.mDialog.dismiss();
                    }
                    finish();
                    return;
                }
            }
            if (this.mDialog != null) {
                this.mDialog.dismiss();
            }
            finish();
        } catch (Throwable th) {
            if (this.mDialog != null) {
                this.mDialog.dismiss();
            }
            finish();
            throw th;
        }
    }

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        int i;
        String str;
        super.onCreate(bundle);
        this.mPackageName = getCallingPackage();
        this.mService = IMediaProjectionManager.Stub.asInterface(ServiceManager.getService("media_projection"));
        if (this.mPackageName == null) {
            finish();
            return;
        }
        PackageManager packageManager = getPackageManager();
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(this.mPackageName, 0);
            this.mUid = applicationInfo.uid;
            try {
                if (this.mService.hasProjectionPermission(this.mUid, this.mPackageName)) {
                    setResult(-1, getMediaProjectionIntent(this.mUid, this.mPackageName, false));
                    finish();
                    return;
                }
                TextPaint textPaint = new TextPaint();
                textPaint.setTextSize(42.0f);
                String charSequence = applicationInfo.loadLabel(packageManager).toString();
                int length = charSequence.length();
                int i2 = 0;
                while (true) {
                    i = i2;
                    str = charSequence;
                    if (i >= length) {
                        break;
                    }
                    int codePointAt = charSequence.codePointAt(i);
                    int type = Character.getType(codePointAt);
                    if (type == 13 || type == 15 || type == 14) {
                        break;
                    }
                    i2 = i + Character.charCount(codePointAt);
                }
                str = charSequence.substring(0, i) + "…";
                String str2 = str;
                if (str.isEmpty()) {
                    str2 = this.mPackageName;
                }
                String unicodeWrap = BidiFormatter.getInstance().unicodeWrap(TextUtils.ellipsize(str2, textPaint, 500.0f, TextUtils.TruncateAt.END).toString());
                String string = getString(2131493652, new Object[]{unicodeWrap});
                SpannableString spannableString = new SpannableString(string);
                int indexOf = string.indexOf(unicodeWrap);
                if (indexOf >= 0) {
                    spannableString.setSpan(new StyleSpan(1), indexOf, unicodeWrap.length() + indexOf, 0);
                }
                this.mDialog = new AlertDialog.Builder(this).setIcon(applicationInfo.loadIcon(packageManager)).setMessage(spannableString).setPositiveButton(2131493655, this).setNegativeButton(17039360, this).setView(2130968789).setOnCancelListener(this).create();
                this.mDialog.create();
                this.mDialog.getButton(-1).setFilterTouchesWhenObscured(true);
                ((CheckBox) this.mDialog.findViewById(2131886633)).setOnCheckedChangeListener(this);
                Window window = this.mDialog.getWindow();
                window.setType(2003);
                window.addPrivateFlags(524288);
                this.mDialog.show();
            } catch (RemoteException e) {
                Log.e("MediaProjectionPermissionActivity", "Error checking projection permissions", e);
                finish();
            }
        } catch (PackageManager.NameNotFoundException e2) {
            Log.e("MediaProjectionPermissionActivity", "unable to look up package name", e2);
            finish();
        }
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        if (this.mDialog != null) {
            this.mDialog.dismiss();
        }
    }
}
