package com.android.systemui.statusbar.phone;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.WindowManager;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/SystemUIDialog.class */
public class SystemUIDialog extends AlertDialog {
    private final Context mContext;

    public SystemUIDialog(Context context) {
        this(context, 2131952122);
    }

    public SystemUIDialog(Context context, int i) {
        super(context, i);
        this.mContext = context;
        applyFlags(this);
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.setTitle(getClass().getSimpleName());
        getWindow().setAttributes(attributes);
    }

    public static void applyFlags(AlertDialog alertDialog) {
        alertDialog.getWindow().setType(2014);
        alertDialog.getWindow().addFlags(655360);
    }

    public static void setShowForAllUsers(AlertDialog alertDialog, boolean z) {
        if (z) {
            alertDialog.getWindow().getAttributes().privateFlags |= 16;
            return;
        }
        alertDialog.getWindow().getAttributes().privateFlags &= -17;
    }

    public void setMessage(int i) {
        setMessage(this.mContext.getString(i));
    }

    public void setNegativeButton(int i, DialogInterface.OnClickListener onClickListener) {
        setButton(-2, this.mContext.getString(i), onClickListener);
    }

    public void setPositiveButton(int i, DialogInterface.OnClickListener onClickListener) {
        setButton(-1, this.mContext.getString(i), onClickListener);
    }

    public void setShowForAllUsers(boolean z) {
        setShowForAllUsers(this, z);
    }
}
