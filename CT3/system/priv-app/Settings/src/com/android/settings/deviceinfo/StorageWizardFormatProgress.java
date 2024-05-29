package com.android.settings.deviceinfo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.IPackageMoveObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.text.TextUtils;
import android.util.Log;
import com.android.settings.R;
import java.util.Objects;
/* loaded from: classes.dex */
public class StorageWizardFormatProgress extends StorageWizardBase {
    private boolean mFormatPrivate;
    private PartitionTask mTask;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.deviceinfo.StorageWizardBase, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.mDisk == null) {
            finish();
            return;
        }
        setContentView(R.layout.storage_wizard_progress);
        setKeepScreenOn(true);
        this.mFormatPrivate = getIntent().getBooleanExtra("format_private", false);
        setIllustrationType(this.mFormatPrivate ? 1 : 2);
        setHeaderText(R.string.storage_wizard_format_progress_title, this.mDisk.getDescription());
        setBodyText(R.string.storage_wizard_format_progress_body, this.mDisk.getDescription());
        getNextButton().setVisibility(8);
        this.mTask = (PartitionTask) getLastNonConfigurationInstance();
        if (this.mTask == null) {
            this.mTask = new PartitionTask();
            this.mTask.setActivity(this);
            this.mTask.execute(new Void[0]);
            return;
        }
        this.mTask.setActivity(this);
    }

    @Override // android.app.Activity
    public Object onRetainNonConfigurationInstance() {
        return this.mTask;
    }

    /* loaded from: classes.dex */
    public static class PartitionTask extends AsyncTask<Void, Integer, Exception> {
        public StorageWizardFormatProgress mActivity;
        private volatile long mInternalBench;
        private volatile long mPrivateBench;
        private volatile int mProgress = 20;

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public Exception doInBackground(Void... params) {
            StorageWizardFormatProgress activity = this.mActivity;
            StorageManager storage = this.mActivity.mStorage;
            try {
                if (activity.mFormatPrivate) {
                    storage.partitionPrivate(activity.mDisk.getId());
                    publishProgress(40);
                    this.mInternalBench = storage.benchmark(null);
                    publishProgress(60);
                    VolumeInfo privateVol = activity.findFirstVolume(1);
                    this.mPrivateBench = storage.benchmark(privateVol.getId());
                    if (activity.mDisk.isDefaultPrimary() && Objects.equals(storage.getPrimaryStorageUuid(), "primary_physical")) {
                        Log.d("StorageSettings", "Just formatted primary physical; silently moving storage to new emulated volume");
                        storage.setPrimaryStorageUuid(privateVol.getFsUuid(), new SilentObserver(null));
                    }
                } else {
                    storage.partitionPublic(activity.mDisk.getId());
                }
                return null;
            } catch (Exception e) {
                return e;
            }
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onProgressUpdate(Integer... progress) {
            this.mProgress = progress[0].intValue();
            this.mActivity.setCurrentProgress(this.mProgress);
        }

        public void setActivity(StorageWizardFormatProgress activity) {
            this.mActivity = activity;
            this.mActivity.setCurrentProgress(this.mProgress);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(Exception e) {
            StorageWizardFormatProgress activity = this.mActivity;
            if (activity.isDestroyed()) {
                return;
            }
            if (e != null) {
                Log.e("StorageSettings", "Failed to partition", e);
                activity.finishAffinity();
            } else if (activity.mFormatPrivate) {
                float pct = ((float) this.mInternalBench) / ((float) this.mPrivateBench);
                Log.d("StorageSettings", "New volume is " + pct + "x the speed of internal");
                if (Float.isNaN(pct) || pct < 0.25d) {
                    SlowWarningFragment dialog = new SlowWarningFragment();
                    dialog.showAllowingStateLoss(activity.getFragmentManager(), "slow_warning");
                    return;
                }
                activity.onFormatFinished();
            } else {
                activity.onFormatFinished();
            }
        }
    }

    /* loaded from: classes.dex */
    public static class SlowWarningFragment extends DialogFragment {
        @Override // android.app.DialogFragment
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Context context = getActivity();
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            StorageWizardFormatProgress target = (StorageWizardFormatProgress) getActivity();
            String descrip = target.getDiskDescription();
            String genericDescip = target.getGenericDiskDescription();
            builder.setMessage(TextUtils.expandTemplate(getText(R.string.storage_wizard_slow_body), descrip, genericDescip));
            builder.setPositiveButton(17039370, new DialogInterface.OnClickListener() { // from class: com.android.settings.deviceinfo.StorageWizardFormatProgress.SlowWarningFragment.1
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialog, int which) {
                    StorageWizardFormatProgress target2 = (StorageWizardFormatProgress) SlowWarningFragment.this.getActivity();
                    target2.onFormatFinished();
                }
            });
            return builder.create();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String getDiskDescription() {
        return this.mDisk.getDescription();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String getGenericDiskDescription() {
        if (this.mDisk.isSd()) {
            return getString(17040574);
        }
        if (this.mDisk.isUsb()) {
            return getString(17040576);
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onFormatFinished() {
        boolean offerMigrate;
        String forgetUuid = getIntent().getStringExtra("forget_uuid");
        if (!TextUtils.isEmpty(forgetUuid)) {
            this.mStorage.forgetVolume(forgetUuid);
        }
        if (this.mFormatPrivate) {
            VolumeInfo privateVol = getPackageManager().getPrimaryStorageCurrentVolume();
            if (privateVol == null) {
                offerMigrate = false;
            } else {
                offerMigrate = "private".equals(privateVol.getId());
            }
        } else {
            offerMigrate = false;
        }
        if (offerMigrate) {
            Intent intent = new Intent(this, StorageWizardMigrate.class);
            intent.putExtra("android.os.storage.extra.DISK_ID", this.mDisk.getId());
            startActivity(intent);
        } else {
            Intent intent2 = new Intent(this, StorageWizardReady.class);
            intent2.putExtra("android.os.storage.extra.DISK_ID", this.mDisk.getId());
            startActivity(intent2);
        }
        finishAffinity();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class SilentObserver extends IPackageMoveObserver.Stub {
        /* synthetic */ SilentObserver(SilentObserver silentObserver) {
            this();
        }

        private SilentObserver() {
        }

        public void onCreated(int moveId, Bundle extras) {
        }

        public void onStatusChanged(int moveId, int status, long estMillis) {
        }
    }
}
