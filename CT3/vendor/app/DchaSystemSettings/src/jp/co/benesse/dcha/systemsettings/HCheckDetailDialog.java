package jp.co.benesse.dcha.systemsettings;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import jp.co.benesse.dcha.util.Logger;
/* loaded from: s.zip:jp/co/benesse/dcha/systemsettings/HCheckDetailDialog.class */
public class HCheckDetailDialog extends DialogFragment implements View.OnClickListener {
    private DialogInterface.OnDismissListener dismissListener = null;
    private HealthCheckDto healthCheckDto;

    private void close(View view) {
        Logger.d("HCheckDetailDialog", "close 0001");
        view.setClickable(false);
        dismiss();
        Logger.d("HCheckDetailDialog", "close 0002");
    }

    protected void drawingResultView(Dialog dialog, int i, int i2, String str) {
        Logger.d("HCheckDetailDialog", "drawingResultView 0001");
        TextView textView = (TextView) dialog.findViewById(i2);
        if (i == 2131230962) {
            Logger.d("HCheckDetailDialog", "drawingResultView 0002");
            textView.setText(getString(2131230962));
            textView.setTextColor(getResources().getColor(2131099648));
        } else if (i == 2131230960) {
            if (TextUtils.isEmpty(str)) {
                Logger.d("HCheckDetailDialog", "drawingResultView 0003");
                textView.setText(getString(2131230962));
                textView.setTextColor(getResources().getColor(2131099648));
            } else {
                Logger.d("HCheckDetailDialog", "drawingResultView 0004");
                textView.setText(str);
                textView.setTextColor(getResources().getColor(2131099650));
            }
        } else if (TextUtils.isEmpty(str)) {
            Logger.d("HCheckDetailDialog", "drawingResultView 0005");
            textView.setText(getString(2131230962));
            textView.setTextColor(getResources().getColor(2131099648));
        } else {
            Logger.d("HCheckDetailDialog", "drawingResultView 0006");
            textView.setText(str);
            textView.setTextColor(getResources().getColor(2131099652));
        }
        Logger.d("HCheckDetailDialog", "drawingResultView 0007");
    }

    @Override // android.app.DialogFragment, android.app.Fragment
    public void onActivityCreated(Bundle bundle) {
        Logger.d("HCheckDetailDialog", "onActivityCreated 0001");
        super.onActivityCreated(bundle);
        Dialog dialog = getDialog();
        WindowManager.LayoutParams attributes = dialog.getWindow().getAttributes();
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int i = displayMetrics.widthPixels;
        int i2 = displayMetrics.heightPixels;
        attributes.width = i;
        attributes.height = i2 + 23;
        dialog.getWindow().setFlags(0, 2);
        dialog.getWindow().setAttributes(attributes);
        Logger.d("HCheckDetailDialog", "onActivityCreated 0002");
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        Logger.d("HCheckDetailDialog", "onClick 0001");
        switch (view.getId()) {
            case 2131361847:
                Logger.d("HCheckDetailDialog", "onClick 0002");
                close(view);
                break;
        }
        Logger.d("HCheckDetailDialog", "onClick 0003");
    }

    @Override // android.app.DialogFragment
    public Dialog onCreateDialog(Bundle bundle) {
        Logger.d("HCheckDetailDialog", "onCreateDialog 0001");
        Dialog dialog = new Dialog(getActivity());
        try {
            dialog.getWindow().requestFeature(1);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            dialog.setContentView(2130903048);
            this.healthCheckDto = (HealthCheckDto) getArguments().getSerializable("healthCheckDto");
            dialog.findViewById(2131361847).setOnClickListener(this);
            dialog.findViewById(2131361847).setClickable(true);
            drawingResultView(dialog, 2131230960, 2131361842, this.healthCheckDto.myMacaddress);
            drawingResultView(dialog, this.healthCheckDto.isCheckedSsid, 2131361795, this.healthCheckDto.mySsid);
            drawingResultView(dialog, this.healthCheckDto.isCheckedWifi, 2131361798, getString(this.healthCheckDto.isCheckedWifi));
            drawingResultView(dialog, this.healthCheckDto.isCheckedIpAddress, 2131361801, this.healthCheckDto.myIpAddress);
            drawingResultView(dialog, this.healthCheckDto.isCheckedIpAddress, 2131361843, this.healthCheckDto.mySubnetMask);
            drawingResultView(dialog, this.healthCheckDto.isCheckedIpAddress, 2131361844, this.healthCheckDto.myDefaultGateway);
            drawingResultView(dialog, this.healthCheckDto.isCheckedIpAddress, 2131361845, this.healthCheckDto.myDns1);
            drawingResultView(dialog, this.healthCheckDto.isCheckedIpAddress, 2131361846, this.healthCheckDto.myDns2);
            drawingResultView(dialog, this.healthCheckDto.isCheckedNetConnection, 2131361804, getString(this.healthCheckDto.isCheckedNetConnection));
            if (this.healthCheckDto.isCheckedDSpeed == 2131230962) {
                Logger.d("HCheckDetailDialog", "onCreateDialog 0002");
                dialog.findViewById(2131361807).setVisibility(0);
            } else {
                Logger.d("HCheckDetailDialog", "onCreateDialog 0003");
                ImageView imageView = (ImageView) dialog.findViewById(2131361808);
                imageView.setImageResource(this.healthCheckDto.myDSpeedImage);
                imageView.setVisibility(0);
                TextView textView = (TextView) dialog.findViewById(2131361809);
                textView.setText(this.healthCheckDto.myDownloadSpeed);
                textView.setVisibility(0);
            }
        } catch (RuntimeException e) {
            Logger.d("HCheckDetailDialog", "onCreateDialog 0004", e);
            dialog.dismiss();
        }
        Logger.d("HCheckDetailDialog", "onCreateDialog 0005");
        return dialog;
    }

    @Override // android.app.DialogFragment, android.content.DialogInterface.OnDismissListener
    public void onDismiss(DialogInterface dialogInterface) {
        Logger.d("HCheckDetailDialog", "onDismiss 0001");
        super.onDismiss(dialogInterface);
        try {
            if (this.dismissListener != null) {
                this.dismissListener.onDismiss(dialogInterface);
            }
        } catch (RuntimeException e) {
            Logger.d("HCheckDetailDialog", "onDismiss 0002", e);
        }
        Logger.d("HCheckDetailDialog", "onDismiss 0003");
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        Logger.d("HCheckDetailDialog", "setOnDismissListener 0001");
        this.dismissListener = onDismissListener;
        Logger.d("HCheckDetailDialog", "setOnDismissListener 0002");
    }
}
