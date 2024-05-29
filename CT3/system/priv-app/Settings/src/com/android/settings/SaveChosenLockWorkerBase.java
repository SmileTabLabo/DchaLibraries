package com.android.settings;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void setListener(Listener listener) {
        if (this.mListener == listener) {
            return;
        }
        this.mListener = listener;
        if (!this.mFinished || this.mListener == null) {
            return;
        }
        this.mListener.onChosenLockSaveFinished(this.mWasSecureBefore, this.mResultData);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void prepare(LockPatternUtils utils, boolean credentialRequired, boolean hasChallenge, long challenge, int userId) {
        this.mUtils = utils;
        this.mUserId = userId;
        this.mHasChallenge = hasChallenge;
        this.mChallenge = challenge;
        this.mWasSecureBefore = this.mUtils.isSecure(this.mUserId);
        Context context = getContext();
        if (context == null || UserManager.get(context).getUserInfo(this.mUserId).isPrimary()) {
            this.mUtils.setCredentialRequiredToDecrypt(credentialRequired);
        }
        this.mFinished = false;
        this.mResultData = null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void start() {
        if (this.mBlocking) {
            finish(saveAndVerifyInBackground());
        } else {
            new Task(this, null).execute(new Void[0]);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void finish(Intent resultData) {
        this.mFinished = true;
        this.mResultData = resultData;
        if (this.mListener == null) {
            return;
        }
        this.mListener.onChosenLockSaveFinished(this.mWasSecureBefore, this.mResultData);
    }

    public void setBlocking(boolean blocking) {
        this.mBlocking = blocking;
    }

    /* loaded from: classes.dex */
    private class Task extends AsyncTask<Void, Void, Intent> {
        /* synthetic */ Task(SaveChosenLockWorkerBase this$0, Task task) {
            this();
        }

        private Task() {
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public Intent doInBackground(Void... params) {
            return SaveChosenLockWorkerBase.this.saveAndVerifyInBackground();
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(Intent resultData) {
            SaveChosenLockWorkerBase.this.finish(resultData);
        }
    }
}
