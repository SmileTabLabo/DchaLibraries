package com.android.settings.bluetooth;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.android.settings.R;
import com.android.settings.bluetooth.DockService;
import com.android.settings.search.Index;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.settingslib.bluetooth.Utils;
/* loaded from: classes.dex */
public final class Utils {
    private static final Utils.ErrorListener mErrorListener = new Utils.ErrorListener() { // from class: com.android.settings.bluetooth.Utils.1
        @Override // com.android.settingslib.bluetooth.Utils.ErrorListener
        public void onShowError(Context context, String name, int messageResId) {
            Utils.showError(context, name, messageResId);
        }
    };
    private static final LocalBluetoothManager.BluetoothManagerCallback mOnInitCallback = new LocalBluetoothManager.BluetoothManagerCallback() { // from class: com.android.settings.bluetooth.Utils.2
        @Override // com.android.settingslib.bluetooth.LocalBluetoothManager.BluetoothManagerCallback
        public void onBluetoothManagerInitialized(Context appContext, LocalBluetoothManager bluetoothManager) {
            bluetoothManager.getEventManager().registerCallback(new DockService.DockBluetoothCallback(appContext));
            com.android.settingslib.bluetooth.Utils.setErrorListener(Utils.mErrorListener);
        }
    };

    private Utils() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static AlertDialog showDisconnectDialog(Context context, AlertDialog dialog, DialogInterface.OnClickListener disconnectListener, CharSequence title, CharSequence message) {
        if (dialog == null) {
            dialog = new AlertDialog.Builder(context).setPositiveButton(17039370, disconnectListener).setNegativeButton(17039360, (DialogInterface.OnClickListener) null).create();
        } else {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            CharSequence okText = context.getText(17039370);
            dialog.setButton(-1, okText, disconnectListener);
        }
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.show();
        return dialog;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void showError(Context context, String name, int messageResId) {
        String message = context.getString(messageResId, name);
        LocalBluetoothManager manager = getLocalBtManager(context);
        if (manager == null) {
            return;
        }
        Activity activity = (Activity) manager.getForegroundActivity();
        if (manager.isForegroundActivity()) {
            Log.d("Bluetooth.Utils", "show ErrorDialogFragment, message is " + message);
            ErrorDialogFragment dialog = new ErrorDialogFragment();
            Bundle args = new Bundle();
            args.putString("errorMessage", message);
            dialog.setArguments(args);
            dialog.show(activity.getFragmentManager(), "Error");
            return;
        }
        Toast.makeText(context, message, 0).show();
    }

    public static void updateSearchIndex(Context context, String className, String title, String screenTitle, int iconResId, boolean enabled) {
        SearchIndexableRaw data = new SearchIndexableRaw(context);
        data.className = className;
        data.title = title;
        data.screenTitle = screenTitle;
        data.iconResId = iconResId;
        data.enabled = enabled;
        Index.getInstance(context).updateFromSearchIndexableData(data);
    }

    /* loaded from: classes.dex */
    public static class ErrorDialogFragment extends DialogFragment {
        @Override // android.app.DialogFragment
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            String message = getArguments().getString("errorMessage");
            return new AlertDialog.Builder(getActivity()).setIcon(17301543).setTitle(R.string.bluetooth_error_title).setMessage(message).setPositiveButton(17039370, (DialogInterface.OnClickListener) null).show();
        }
    }

    public static LocalBluetoothManager getLocalBtManager(Context context) {
        return LocalBluetoothManager.getInstance(context, mOnInitCallback);
    }
}
