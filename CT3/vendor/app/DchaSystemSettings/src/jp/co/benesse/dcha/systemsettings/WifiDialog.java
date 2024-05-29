package jp.co.benesse.dcha.systemsettings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import jp.co.benesse.dcha.util.Logger;
import jp.co.benesse.dcha.util.RestrictedLockUtils;
import jp.co.benesse.dcha.util.WifiSettings;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: s.zip:jp/co/benesse/dcha/systemsettings/WifiDialog.class */
public class WifiDialog extends AlertDialog implements WifiConfigUiBase, DialogInterface.OnClickListener {
    private final AccessPoint mAccessPoint;
    private Context mContext;
    private WifiConfigController mController;
    private boolean mHideSubmitButton;
    private final WifiDialogListener mListener;
    private final int mMode;

    /* loaded from: s.zip:jp/co/benesse/dcha/systemsettings/WifiDialog$WifiDialogListener.class */
    public interface WifiDialogListener {
        void onForget(WifiDialog wifiDialog);

        void onSubmit(WifiDialog wifiDialog);
    }

    public WifiDialog(Context context, WifiDialogListener wifiDialogListener, AccessPoint accessPoint, int i) {
        super(context, 2131296273);
        Logger.d("WifiDialog", "WifiDialog 0003");
        this.mMode = i;
        this.mListener = wifiDialogListener;
        this.mAccessPoint = accessPoint;
        this.mHideSubmitButton = false;
        this.mContext = context;
        Logger.d("WifiDialog", "WifiDialog 0004");
    }

    @Override // jp.co.benesse.dcha.systemsettings.WifiConfigUiBase
    public void dispatchSubmit() {
        Logger.d("WifiDialog", "dispatchSubmit 0001");
        if (this.mListener != null) {
            Logger.d("WifiDialog", "dispatchSubmit 0002");
            this.mListener.onSubmit(this);
        }
        dismiss();
        Logger.d("WifiDialog", "dispatchSubmit 0003");
    }

    public WifiConfigController getController() {
        Logger.d("WifiDialog", "WifiConfigController 0001");
        return this.mController;
    }

    @Override // jp.co.benesse.dcha.systemsettings.WifiConfigUiBase
    public Button getForgetButton() {
        Logger.d("WifiDialog", "getForgetButton 0001");
        return getButton(-3);
    }

    @Override // jp.co.benesse.dcha.systemsettings.WifiConfigUiBase
    public Button getSubmitButton() {
        Logger.d("WifiDialog", "getSubmitButton 0001");
        return getButton(-1);
    }

    @Override // android.content.DialogInterface.OnClickListener
    public void onClick(DialogInterface dialogInterface, int i) {
        Logger.d("WifiDialog", "onClick 0001");
        if (this.mListener != null) {
            Logger.d("WifiDialog", "onClick 0002");
            switch (i) {
                case -3:
                    Logger.d("WifiDialog", "onClick 0004");
                    if (!WifiSettings.isEditabilityLockedDown(getContext(), this.mAccessPoint.getConfig())) {
                        this.mListener.onForget(this);
                        break;
                    } else {
                        Logger.d("WifiDialog", "onClick 0005");
                        RestrictedLockUtils.sendShowAdminSupportDetailsIntent(getContext(), RestrictedLockUtils.getDeviceOwner(getContext()));
                        return;
                    }
                case -1:
                    Logger.d("WifiDialog", "onClick 0003");
                    this.mListener.onSubmit(this);
                    break;
            }
        }
        Logger.d("WifiDialog", "onClick 0006");
    }

    @Override // android.app.AlertDialog, android.app.Dialog
    protected void onCreate(Bundle bundle) {
        Logger.d("WifiDialog", "onCreate 0001");
        View inflate = getLayoutInflater().inflate(2130903051, (ViewGroup) null);
        setView(inflate);
        setInverseBackgroundForced(false);
        this.mController = new WifiConfigController((NetworkSettingActivity) this.mContext, this, inflate, this.mAccessPoint, this.mMode);
        super.onCreate(bundle);
        this.mController.setSubmitOrEnable();
        if (this.mHideSubmitButton) {
            Logger.d("WifiDialog", "onCreate 0002");
            this.mController.hideSubmitButton();
        } else {
            Logger.d("WifiDialog", "onCreate 0003");
            this.mController.enableSubmitIfAppropriate();
        }
        if (this.mAccessPoint == null) {
            Logger.d("WifiDialog", "onCreate 0004");
            this.mController.hideForgetButton();
        }
        Logger.d("WifiDialog", "onCreate 0005");
    }

    @Override // android.app.Dialog
    public void onRestoreInstanceState(Bundle bundle) {
        Logger.d("WifiDialog", "onRestoreInstanceState 0001");
        super.onRestoreInstanceState(bundle);
        this.mController.updatePassword();
        Logger.d("WifiDialog", "onRestoreInstanceState 0002");
    }

    @Override // jp.co.benesse.dcha.systemsettings.WifiConfigUiBase
    public void setCancelButton(CharSequence charSequence) {
        Logger.d("WifiDialog", "setCancelButton 0001");
        setButton(-2, charSequence, this);
        Logger.d("WifiDialog", "setCancelButton 0002");
    }

    @Override // jp.co.benesse.dcha.systemsettings.WifiConfigUiBase
    public void setForgetButton(CharSequence charSequence) {
        Logger.d("WifiDialog", "setForgetButton 0001");
        setButton(-3, charSequence, this);
        Logger.d("WifiDialog", "setForgetButton 0002");
    }

    @Override // jp.co.benesse.dcha.systemsettings.WifiConfigUiBase
    public void setSubmitButton(CharSequence charSequence) {
        Logger.d("WifiDialog", "setSubmitButton 0001");
        setButton(-1, charSequence, this);
        Logger.d("WifiDialog", "setSubmitButton 0002");
    }
}
