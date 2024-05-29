package com.android.settings.wifi;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.android.settings.R;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.wifi.AccessPoint;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class WifiDialog extends AlertDialog implements WifiConfigUiBase, DialogInterface.OnClickListener {
    private final AccessPoint mAccessPoint;
    private WifiConfigController mController;
    private boolean mHideSubmitButton;
    private final WifiDialogListener mListener;
    private final int mMode;
    private View mView;

    /* loaded from: classes.dex */
    public interface WifiDialogListener {
        void onForget(WifiDialog wifiDialog);

        void onSubmit(WifiDialog wifiDialog);
    }

    public WifiDialog(Context context, WifiDialogListener listener, AccessPoint accessPoint, int mode, boolean hideSubmitButton) {
        this(context, listener, accessPoint, mode);
        this.mHideSubmitButton = hideSubmitButton;
    }

    public WifiDialog(Context context, WifiDialogListener listener, AccessPoint accessPoint, int mode) {
        super(context);
        this.mMode = mode;
        this.mListener = listener;
        this.mAccessPoint = accessPoint;
        this.mHideSubmitButton = false;
    }

    public WifiConfigController getController() {
        return this.mController;
    }

    @Override // android.app.AlertDialog, android.app.Dialog
    protected void onCreate(Bundle savedInstanceState) {
        this.mView = getLayoutInflater().inflate(R.layout.wifi_dialog, (ViewGroup) null);
        setView(this.mView);
        setInverseBackgroundForced(true);
        this.mController = new WifiConfigController(this, this.mView, this.mAccessPoint, this.mMode);
        super.onCreate(savedInstanceState);
        if (this.mHideSubmitButton) {
            this.mController.hideSubmitButton();
        } else {
            this.mController.enableSubmitIfAppropriate();
        }
        if (this.mAccessPoint != null) {
            return;
        }
        this.mController.hideForgetButton();
    }

    @Override // android.app.Dialog
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.mController.updatePassword();
    }

    @Override // com.android.settings.wifi.WifiConfigUiBase
    public void dispatchSubmit() {
        if (this.mListener != null) {
            this.mListener.onSubmit(this);
        }
        dismiss();
    }

    @Override // android.content.DialogInterface.OnClickListener
    public void onClick(DialogInterface dialogInterface, int id) {
        if (this.mListener == null) {
            return;
        }
        switch (id) {
            case -3:
                if (WifiSettings.isEditabilityLockedDown(getContext(), this.mAccessPoint.getConfig())) {
                    RestrictedLockUtils.sendShowAdminSupportDetailsIntent(getContext(), RestrictedLockUtils.getDeviceOwner(getContext()));
                    return;
                } else {
                    this.mListener.onForget(this);
                    return;
                }
            case -2:
            default:
                return;
            case -1:
                this.mListener.onSubmit(this);
                return;
        }
    }

    @Override // com.android.settings.wifi.WifiConfigUiBase
    public Button getSubmitButton() {
        return getButton(-1);
    }

    @Override // com.android.settings.wifi.WifiConfigUiBase
    public Button getForgetButton() {
        return getButton(-3);
    }

    @Override // com.android.settings.wifi.WifiConfigUiBase
    public void setSubmitButton(CharSequence text) {
        setButton(-1, text, this);
    }

    @Override // com.android.settings.wifi.WifiConfigUiBase
    public void setForgetButton(CharSequence text) {
        setButton(-3, text, this);
    }

    @Override // com.android.settings.wifi.WifiConfigUiBase
    public void setCancelButton(CharSequence text) {
        setButton(-2, text, this);
    }
}
