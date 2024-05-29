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
import com.android.systemui.R;
/* loaded from: classes.dex */
public class MediaProjectionPermissionActivity extends Activity implements DialogInterface.OnCancelListener, DialogInterface.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private AlertDialog mDialog;
    private String mPackageName;
    private boolean mPermanentGrant;
    private IMediaProjectionManager mService;
    private int mUid;

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
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
                int i = 0;
                while (i < length) {
                    int codePointAt = charSequence.codePointAt(i);
                    int type = Character.getType(codePointAt);
                    if (type == 13 || type == 15 || type == 14) {
                        charSequence = charSequence.substring(0, i) + "…";
                        break;
                    }
                    i += Character.charCount(codePointAt);
                }
                if (charSequence.isEmpty()) {
                    charSequence = this.mPackageName;
                }
                String unicodeWrap = BidiFormatter.getInstance().unicodeWrap(TextUtils.ellipsize(charSequence, textPaint, 500.0f, TextUtils.TruncateAt.END).toString());
                String string = getString(R.string.media_projection_dialog_text, new Object[]{unicodeWrap});
                SpannableString spannableString = new SpannableString(string);
                int indexOf = string.indexOf(unicodeWrap);
                if (indexOf >= 0) {
                    spannableString.setSpan(new StyleSpan(1), indexOf, unicodeWrap.length() + indexOf, 0);
                }
                this.mDialog = new AlertDialog.Builder(this).setIcon(applicationInfo.loadIcon(packageManager)).setMessage(spannableString).setPositiveButton(R.string.media_projection_action_text, this).setNegativeButton(17039360, this).setView(R.layout.remember_permission_checkbox).setOnCancelListener(this).create();
                this.mDialog.create();
                this.mDialog.getButton(-1).setFilterTouchesWhenObscured(true);
                ((CheckBox) this.mDialog.findViewById(R.id.remember)).setOnCheckedChangeListener(this);
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

    /* JADX WARN: Code restructure failed: missing block: B:11:0x0021, code lost:
        if (r2.mDialog == null) goto L7;
     */
    /* JADX WARN: Code restructure failed: missing block: B:22:0x003e, code lost:
        return;
     */
    @Override // android.content.DialogInterface.OnClickListener
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void onClick(DialogInterface dialogInterface, int i) {
        try {
            if (i == -1) {
                try {
                    setResult(-1, getMediaProjectionIntent(this.mUid, this.mPackageName, this.mPermanentGrant));
                } catch (RemoteException e) {
                    Log.e("MediaProjectionPermissionActivity", "Error granting projection permission", e);
                    setResult(0);
                }
            }
        } finally {
            if (this.mDialog != null) {
                this.mDialog.dismiss();
            }
            finish();
        }
    }

    @Override // android.widget.CompoundButton.OnCheckedChangeListener
    public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
        this.mPermanentGrant = z;
    }

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
}
