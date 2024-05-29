package com.android.settings.vpn2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import com.android.settings.R;
/* loaded from: classes.dex */
class AppDialog extends AlertDialog implements DialogInterface.OnClickListener {
    private final String mLabel;
    private final Listener mListener;
    private final PackageInfo mPackageInfo;

    /* loaded from: classes.dex */
    public interface Listener {
        void onForget(DialogInterface dialogInterface);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public AppDialog(Context context, Listener listener, PackageInfo pkgInfo, String label) {
        super(context);
        this.mListener = listener;
        this.mPackageInfo = pkgInfo;
        this.mLabel = label;
    }

    @Override // android.app.AlertDialog, android.app.Dialog
    protected void onCreate(Bundle savedState) {
        setTitle(this.mLabel);
        setMessage(getContext().getString(R.string.vpn_version, this.mPackageInfo.versionName));
        createButtons();
        super.onCreate(savedState);
    }

    protected void createButtons() {
        Context context = getContext();
        setButton(-2, context.getString(R.string.vpn_forget), this);
        setButton(-1, context.getString(R.string.vpn_done), this);
    }

    @Override // android.content.DialogInterface.OnClickListener
    public void onClick(DialogInterface dialog, int which) {
        if (which == -2) {
            this.mListener.onForget(dialog);
        }
        dismiss();
    }
}
