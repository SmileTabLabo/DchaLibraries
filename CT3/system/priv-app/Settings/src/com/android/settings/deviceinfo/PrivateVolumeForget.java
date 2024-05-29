package com.android.settings.deviceinfo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.os.storage.VolumeRecord;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.android.settings.InstrumentedFragment;
import com.android.settings.R;
/* loaded from: classes.dex */
public class PrivateVolumeForget extends InstrumentedFragment {
    private final View.OnClickListener mConfirmListener = new View.OnClickListener() { // from class: com.android.settings.deviceinfo.PrivateVolumeForget.1
        @Override // android.view.View.OnClickListener
        public void onClick(View v) {
            ForgetConfirmFragment.show(PrivateVolumeForget.this, PrivateVolumeForget.this.mRecord.getFsUuid());
        }
    };
    private VolumeRecord mRecord;

    @Override // com.android.settings.InstrumentedFragment
    protected int getMetricsCategory() {
        return 42;
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        StorageManager storage = (StorageManager) getActivity().getSystemService(StorageManager.class);
        String fsUuid = getArguments().getString("android.os.storage.extra.FS_UUID");
        this.mRecord = storage.findRecordByUuid(fsUuid);
        View view = inflater.inflate(R.layout.storage_internal_forget, container, false);
        TextView body = (TextView) view.findViewById(R.id.body);
        Button confirm = (Button) view.findViewById(R.id.confirm);
        body.setText(TextUtils.expandTemplate(getText(R.string.storage_internal_forget_details), this.mRecord.getNickname()));
        confirm.setOnClickListener(this.mConfirmListener);
        return view;
    }

    /* loaded from: classes.dex */
    public static class ForgetConfirmFragment extends DialogFragment {
        public static void show(Fragment parent, String fsUuid) {
            Bundle args = new Bundle();
            args.putString("android.os.storage.extra.FS_UUID", fsUuid);
            ForgetConfirmFragment dialog = new ForgetConfirmFragment();
            dialog.setArguments(args);
            dialog.setTargetFragment(parent, 0);
            dialog.show(parent.getFragmentManager(), "forget_confirm");
        }

        @Override // android.app.DialogFragment
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Context context = getActivity();
            final StorageManager storage = (StorageManager) context.getSystemService(StorageManager.class);
            final String fsUuid = getArguments().getString("android.os.storage.extra.FS_UUID");
            VolumeRecord record = storage.findRecordByUuid(fsUuid);
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(TextUtils.expandTemplate(getText(R.string.storage_internal_forget_confirm_title), record.getNickname()));
            builder.setMessage(TextUtils.expandTemplate(getText(R.string.storage_internal_forget_confirm), record.getNickname()));
            builder.setPositiveButton(R.string.storage_menu_forget, new DialogInterface.OnClickListener() { // from class: com.android.settings.deviceinfo.PrivateVolumeForget.ForgetConfirmFragment.1
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialog, int which) {
                    storage.forgetVolume(fsUuid);
                    ForgetConfirmFragment.this.getActivity().finish();
                }
            });
            builder.setNegativeButton(R.string.cancel, (DialogInterface.OnClickListener) null);
            return builder.create();
        }
    }
}
