package com.android.settings.fingerprint;

import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settings.R;
/* loaded from: classes.dex */
public class FingerprintUiHelper extends FingerprintManager.AuthenticationCallback {
    private Callback mCallback;
    private CancellationSignal mCancellationSignal;
    private TextView mErrorTextView;
    private FingerprintManager mFingerprintManager;
    private ImageView mIcon;
    private Runnable mResetErrorTextRunnable = new Runnable() { // from class: com.android.settings.fingerprint.FingerprintUiHelper.1
        @Override // java.lang.Runnable
        public void run() {
            FingerprintUiHelper.this.mErrorTextView.setText("");
            FingerprintUiHelper.this.mIcon.setImageResource(R.drawable.ic_fingerprint);
        }
    };
    private int mUserId;

    /* loaded from: classes.dex */
    public interface Callback {
        void onAuthenticated();

        void onFingerprintIconVisibilityChanged(boolean z);
    }

    public FingerprintUiHelper(ImageView icon, TextView errorTextView, Callback callback, int userId) {
        this.mFingerprintManager = (FingerprintManager) icon.getContext().getSystemService(FingerprintManager.class);
        this.mIcon = icon;
        this.mErrorTextView = errorTextView;
        this.mCallback = callback;
        this.mUserId = userId;
    }

    public void startListening() {
        if (this.mFingerprintManager == null || !this.mFingerprintManager.isHardwareDetected() || this.mFingerprintManager.getEnrolledFingerprints(this.mUserId).size() <= 0) {
            return;
        }
        this.mCancellationSignal = new CancellationSignal();
        this.mFingerprintManager.setActiveUser(this.mUserId);
        this.mFingerprintManager.authenticate(null, this.mCancellationSignal, 0, this, null, this.mUserId);
        setFingerprintIconVisibility(true);
        this.mIcon.setImageResource(R.drawable.ic_fingerprint);
    }

    public void stopListening() {
        if (this.mCancellationSignal == null) {
            return;
        }
        this.mCancellationSignal.cancel();
        this.mCancellationSignal = null;
    }

    private boolean isListening() {
        return (this.mCancellationSignal == null || this.mCancellationSignal.isCanceled()) ? false : true;
    }

    private void setFingerprintIconVisibility(boolean visible) {
        this.mIcon.setVisibility(visible ? 0 : 8);
        this.mCallback.onFingerprintIconVisibilityChanged(visible);
    }

    @Override // android.hardware.fingerprint.FingerprintManager.AuthenticationCallback
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        showError(errString);
        setFingerprintIconVisibility(false);
    }

    @Override // android.hardware.fingerprint.FingerprintManager.AuthenticationCallback
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        showError(helpString);
    }

    @Override // android.hardware.fingerprint.FingerprintManager.AuthenticationCallback
    public void onAuthenticationFailed() {
        showError(this.mIcon.getResources().getString(R.string.fingerprint_not_recognized));
    }

    @Override // android.hardware.fingerprint.FingerprintManager.AuthenticationCallback
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        this.mIcon.setImageResource(R.drawable.ic_fingerprint_success);
        this.mCallback.onAuthenticated();
    }

    private void showError(CharSequence error) {
        if (!isListening()) {
            return;
        }
        this.mIcon.setImageResource(R.drawable.ic_fingerprint_error);
        this.mErrorTextView.setText(error);
        this.mErrorTextView.removeCallbacks(this.mResetErrorTextRunnable);
        this.mErrorTextView.postDelayed(this.mResetErrorTextRunnable, 1300L);
    }
}
