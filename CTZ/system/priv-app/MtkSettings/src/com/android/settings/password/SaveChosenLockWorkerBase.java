package com.android.settings.password;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.UserManager;
import com.android.internal.widget.LockPatternUtils;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public abstract class SaveChosenLockWorkerBase extends Fragment {
    private boolean mBlocking;
    protected long mChallenge;
    private boolean mFinished;
    protected boolean mHasChallenge;
    private Listener mListener;
    private Intent mResultData;
    protected int mUserId;
    protected LockPatternUtils mUtils;
    protected boolean mWasSecureBefore;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public interface Listener {
        void onChosenLockSaveFinished(boolean z, Intent intent);
    }

    protected abstract Intent saveAndVerifyInBackground();

    @Override // android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setRetainInstance(true);
    }

    public void setListener(Listener listener) {
        if (this.mListener == listener) {
            return;
        }
        this.mListener = listener;
        if (this.mFinished && this.mListener != null) {
            this.mListener.onChosenLockSaveFinished(this.mWasSecureBefore, this.mResultData);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void prepare(LockPatternUtils lockPatternUtils, boolean z, boolean z2, long j, int i) {
        this.mUtils = lockPatternUtils;
        this.mUserId = i;
        this.mHasChallenge = z2;
        this.mChallenge = j;
        this.mWasSecureBefore = this.mUtils.isSecure(this.mUserId);
        Context context = getContext();
        if (context == null || UserManager.get(context).getUserInfo(this.mUserId).isPrimary()) {
            this.mUtils.setCredentialRequiredToDecrypt(z);
        }
        this.mFinished = false;
        this.mResultData = null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void start() {
        if (this.mBlocking) {
            finish(saveAndVerifyInBackground());
        } else {
            new Task().execute(new Void[0]);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void finish(Intent intent) {
        this.mFinished = true;
        this.mResultData = intent;
        if (this.mListener != null) {
            this.mListener.onChosenLockSaveFinished(this.mWasSecureBefore, this.mResultData);
        }
    }

    public void setBlocking(boolean z) {
        this.mBlocking = z;
    }

    /* loaded from: classes.dex */
    private class Task extends AsyncTask<Void, Void, Intent> {
        private Task() {
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public Intent doInBackground(Void... voidArr) {
            return SaveChosenLockWorkerBase.this.saveAndVerifyInBackground();
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(Intent intent) {
            SaveChosenLockWorkerBase.this.finish(intent);
        }
    }
}
