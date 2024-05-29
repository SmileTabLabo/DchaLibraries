package com.android.settings;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.UserHandle;
import android.service.persistentdata.PersistentDataBlockManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.settingslib.RestrictedLockUtils;
/* loaded from: classes.dex */
public class MasterClearConfirm extends OptionsMenuFragment {
    private View mContentView;
    private boolean mEraseSdCard;
    private View.OnClickListener mFinalClickListener = new View.OnClickListener() { // from class: com.android.settings.MasterClearConfirm.1
        /* JADX WARN: Type inference failed for: r3v12, types: [com.android.settings.MasterClearConfirm$1$1] */
        @Override // android.view.View.OnClickListener
        public void onClick(View v) {
            if (Utils.isMonkeyRunning()) {
                return;
            }
            IntentFilter filter = new IntentFilter("android.intent.action.BATTERY_CHANGED");
            Intent batteryStatus = MasterClearConfirm.this.getActivity().registerReceiver(null, filter);
            if (!Utils.isCharging(batteryStatus)) {
                MasterClearConfirm.this.showNeedToConnectAcDialog();
                return;
            }
            final PersistentDataBlockManager pdbManager = (PersistentDataBlockManager) MasterClearConfirm.this.getActivity().getSystemService("persistent_data_block");
            if (pdbManager != null && !pdbManager.getOemUnlockEnabled() && Utils.isDeviceProvisioned(MasterClearConfirm.this.getActivity())) {
                new AsyncTask<Void, Void, Void>() { // from class: com.android.settings.MasterClearConfirm.1.1
                    int mOldOrientation;
                    ProgressDialog mProgressDialog;

                    /* JADX INFO: Access modifiers changed from: protected */
                    @Override // android.os.AsyncTask
                    public Void doInBackground(Void... params) {
                        pdbManager.wipe();
                        return null;
                    }

                    /* JADX INFO: Access modifiers changed from: protected */
                    @Override // android.os.AsyncTask
                    public void onPostExecute(Void aVoid) {
                        this.mProgressDialog.hide();
                        if (MasterClearConfirm.this.getActivity() == null) {
                            return;
                        }
                        MasterClearConfirm.this.getActivity().setRequestedOrientation(this.mOldOrientation);
                        MasterClearConfirm.this.doMasterClear();
                    }

                    @Override // android.os.AsyncTask
                    protected void onPreExecute() {
                        this.mProgressDialog = getProgressDialog();
                        this.mProgressDialog.show();
                        this.mOldOrientation = MasterClearConfirm.this.getActivity().getRequestedOrientation();
                        MasterClearConfirm.this.getActivity().setRequestedOrientation(14);
                    }
                }.execute(new Void[0]);
            } else {
                MasterClearConfirm.this.doMasterClear();
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public ProgressDialog getProgressDialog() {
            ProgressDialog progressDialog = new ProgressDialog(MasterClearConfirm.this.getActivity());
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.setTitle(MasterClearConfirm.this.getActivity().getString(R.string.master_clear_progress_title));
            progressDialog.setMessage(MasterClearConfirm.this.getActivity().getString(R.string.master_clear_progress_text));
            return progressDialog;
        }
    };
    private DialogInterface.OnClickListener mNeedToConnectAcListener = new DialogInterface.OnClickListener() { // from class: com.android.settings.MasterClearConfirm.2
        @Override // android.content.DialogInterface.OnClickListener
        public void onClick(DialogInterface dialog, int which) {
            MasterClearConfirm.this.getActivity().finish();
        }
    };

    /* JADX INFO: Access modifiers changed from: private */
    public void doMasterClear() {
        Intent intent = new Intent("android.intent.action.MASTER_CLEAR");
        intent.addFlags(268435456);
        intent.putExtra("android.intent.extra.REASON", "MasterClearConfirm");
        intent.putExtra("android.intent.extra.WIPE_EXTERNAL_STORAGE", this.mEraseSdCard);
        getActivity().sendBroadcast(intent);
    }

    private void establishFinalConfirmationState() {
        this.mContentView.findViewById(R.id.execute_master_clear).setOnClickListener(this.mFinalClickListener);
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RestrictedLockUtils.EnforcedAdmin admin = RestrictedLockUtils.checkIfRestrictionEnforced(getActivity(), "no_factory_reset", UserHandle.myUserId());
        if (RestrictedLockUtils.hasBaseUserRestriction(getActivity(), "no_factory_reset", UserHandle.myUserId())) {
            return inflater.inflate(R.layout.master_clear_disallowed_screen, (ViewGroup) null);
        }
        if (admin != null) {
            View view = inflater.inflate(R.layout.admin_support_details_empty_view, (ViewGroup) null);
            ShowAdminSupportDetailsDialog.setAdminSupportDetails(getActivity(), view, admin, false);
            view.setVisibility(0);
            return view;
        }
        this.mContentView = inflater.inflate(R.layout.master_clear_confirm, (ViewGroup) null);
        establishFinalConfirmationState();
        setAccessibilityTitle();
        return this.mContentView;
    }

    private void setAccessibilityTitle() {
        CharSequence currentTitle = getActivity().getTitle();
        TextView confirmationMessage = (TextView) this.mContentView.findViewById(R.id.master_clear_confirm);
        if (confirmationMessage == null) {
            return;
        }
        String accessibileText = currentTitle + "," + confirmationMessage.getText();
        getActivity().setTitle(Utils.createAccessibleSequence(currentTitle, accessibileText));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showNeedToConnectAcDialog() {
        Resources res = getActivity().getResources();
        new AlertDialog.Builder(getActivity()).setTitle(res.getText(R.string.master_clear_title)).setMessage(res.getText(R.string.master_clear_need_ac_message)).setPositiveButton(res.getText(R.string.master_clear_need_ac_label), this.mNeedToConnectAcListener).show();
    }

    @Override // com.android.settings.InstrumentedFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle savedInstanceState) {
        boolean z;
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args == null) {
            z = false;
        } else {
            z = args.getBoolean("erase_sd");
        }
        this.mEraseSdCard = z;
    }

    @Override // com.android.settings.InstrumentedFragment
    protected int getMetricsCategory() {
        return 67;
    }
}
