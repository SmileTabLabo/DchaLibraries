package com.android.systemui.keyboard;

import android.content.Context;
import com.android.systemui.statusbar.phone.SystemUIDialog;
/* loaded from: a.zip:com/android/systemui/keyboard/BluetoothDialog.class */
public class BluetoothDialog extends SystemUIDialog {
    public BluetoothDialog(Context context) {
        super(context);
        getWindow().setType(2008);
        setShowForAllUsers(true);
    }
}
