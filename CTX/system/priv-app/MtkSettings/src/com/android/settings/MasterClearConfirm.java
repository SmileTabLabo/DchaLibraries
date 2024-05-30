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
import android.service.oemlock.OemLockManager;
import android.service.persistentdata.PersistentDataBlockManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.settings.core.InstrumentedFragment;
import com.android.settings.enterprise.ActionDisabledByAdminDialogHelper;
import com.android.settingslib.RestrictedLockUtils;
/* loaded from: classes.dex */
public class MasterClearConfirm extends InstrumentedFragment {
    private View mContentView;
    private boolean mEraseEsims;
    private boolean mEraseSdCard;
    private View.OnClickListener mFinalClickListener = new View.OnClickListener() { // from class: com.android.settings.MasterClearConfirm.1
        /* JADX WARN: Type inference failed for: r0v12, types: [com.android.settings.MasterClearConfirm$1$1] */
        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            if (Utils.isMonkeyRunning()) {
                return;
            }
            if (!Utils.isCharging(MasterClearConfirm.this.getActivity().registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED")))) {
                MasterClearConfirm.this.showNeedToConnectAcDialog();
                return;
            }
            final PersistentDataBlockManager persistentDataBlockManager = (PersistentDataBlockManager) MasterClearConfirm.this.getActivity().getSystemService("persistent_data_block");
            OemLockManager oemLockManager = (OemLockManager) MasterClearConfirm.this.getActivity().getSystemService("oem_lock");
            if (persistentDataBlockManager == null || oemLockManager.isOemUnlockAllowed() || !Utils.isDeviceProvisioned(MasterClearConfirm.this.getActivity())) {
                MasterClearConfirm.this.doMasterClear();
            } else {
                new AsyncTask<Void, Void, Void>() { // from class: com.android.settings.MasterClearConfirm.1.1
                    int mOldOrientation;
                    ProgressDialog mProgressDialog;

                    /* JADX INFO: Access modifiers changed from: protected */
                    @Override // android.os.AsyncTask
                    public Void doInBackground(Void... voidArr) {
                        persistentDataBlockManager.wipe();
                        return null;
                    }

                    /* JADX INFO: Access modifiers changed from: protected */
                    @Override // android.os.AsyncTask
                    public void onPostExecute(Void r2) {
                        this.mProgressDialog.hide();
                        if (MasterClearConfirm.this.getActivity() != null) {
                            MasterClearConfirm.this.getActivity().setRequestedOrientation(this.mOldOrientation);
                            MasterClearConfirm.this.doMasterClear();
                        }
                    }

                    @Override // android.os.AsyncTask
                    protected void onPreExecute() {
                        this.mProgressDialog = getProgressDialog();
                        this.mProgressDialog.show();
                        this.mOldOrientation = MasterClearConfirm.this.getActivity().getRequestedOrientation();
                        MasterClearConfirm.this.getActivity().setRequestedOrientation(14);
                    }
                }.execute(new Void[0]);
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
        public void onClick(DialogInterface dialogInterface, int i) {
            MasterClearConfirm.this.getActivity().finish();
        }
    };

    /* JADX INFO: Access modifiers changed from: private */
    public void doMasterClear() {
        Intent intent = new Intent("android.intent.action.FACTORY_RESET");
        intent.setPackage("android");
        intent.addFlags(268435456);
        intent.putExtra("android.intent.extra.REASON", "MasterClearConfirm");
        intent.putExtra("android.intent.extra.WIPE_EXTERNAL_STORAGE", this.mEraseSdCard);
        intent.putExtra("com.android.internal.intent.extra.WIPE_ESIMS", this.mEraseEsims);
        getActivity().sendBroadcast(intent);
    }

    private void establishFinalConfirmationState() {
        this.mContentView.findViewById(R.id.execute_master_clear).setOnClickListener(this.mFinalClickListener);
    }

    @Override // android.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        RestrictedLockUtils.EnforcedAdmin checkIfRestrictionEnforced = RestrictedLockUtils.checkIfRestrictionEnforced(getActivity(), "no_factory_reset", UserHandle.myUserId());
        if (RestrictedLockUtils.hasBaseUserRestriction(getActivity(), "no_factory_reset", UserHandle.myUserId())) {
            return layoutInflater.inflate(R.layout.master_clear_disallowed_screen, (ViewGroup) null);
        }
        if (checkIfRestrictionEnforced != null) {
            new ActionDisabledByAdminDialogHelper(getActivity()).prepareDialogBuilder("no_factory_reset", checkIfRestrictionEnforced).setOnDismissListener(new DialogInterface.OnDismissListener() { // from class: com.android.settings.-$$Lambda$MasterClearConfirm$weRgiuD2TQnm7jx9NX_-qHWwsHU
                @Override // android.content.DialogInterface.OnDismissListener
                public final void onDismiss(DialogInterface dialogInterface) {
                    MasterClearConfirm.this.getActivity().finish();
                }
            }).show();
            return new View(getActivity());
        }
        this.mContentView = layoutInflater.inflate(R.layout.master_clear_confirm, (ViewGroup) null);
        establishFinalConfirmationState();
        setAccessibilityTitle();
        return this.mContentView;
    }

    private void setAccessibilityTitle() {
        CharSequence title = getActivity().getTitle();
        TextView textView = (TextView) this.mContentView.findViewById(R.id.master_clear_confirm);
        if (textView != null) {
            getActivity().setTitle(Utils.createAccessibleSequence(title, title + "," + textView.getText()));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showNeedToConnectAcDialog() {
        Resources resources = getActivity().getResources();
        new AlertDialog.Builder(getActivity()).setTitle(resources.getText(R.string.master_clear_title)).setMessage(resources.getText(R.string.master_clear_need_ac_message)).setPositiveButton(resources.getText(R.string.master_clear_need_ac_label), this.mNeedToConnectAcListener).show();
    }

    @Override // com.android.settingslib.core.lifecycle.ObservableFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Bundle arguments = getArguments();
        boolean z = false;
        this.mEraseSdCard = arguments != null && arguments.getBoolean("erase_sd");
        if (arguments != null && arguments.getBoolean("erase_esim")) {
            z = true;
        }
        this.mEraseEsims = z;
    }

    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 67;
    }
}
