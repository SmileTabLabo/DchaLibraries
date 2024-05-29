package com.mediatek.keyguard.Telephony;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.INotificationManager;
import android.app.ITransientNotification;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.SubscriptionInfo;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManagerImpl;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.IccCardConstants;
import com.android.keyguard.EmergencyCarrierArea;
import com.android.keyguard.KeyguardPinBasedInputView;
import com.android.keyguard.KeyguardSecurityModel;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.KeyguardUtils;
import com.android.keyguard.R$id;
import com.android.keyguard.R$plurals;
import com.android.keyguard.R$string;
import com.mediatek.internal.telephony.ITelephonyEx;
import com.mediatek.keyguard.Plugin.KeyguardPluginFactory;
import com.mediatek.keyguard.ext.IKeyguardUtilExt;
import com.mediatek.keyguard.ext.IOperatorSIMString;
/* loaded from: a.zip:com/mediatek/keyguard/Telephony/KeyguardSimPinPukMeView.class */
public class KeyguardSimPinPukMeView extends KeyguardPinBasedInputView {
    private Runnable mDismissSimPinPukRunnable;
    private Handler mHandler;
    private IOperatorSIMString mIOperatorSIMString;
    private IKeyguardUtilExt mKeyguardUtilExt;
    private KeyguardUtils mKeyguardUtils;
    private IccCardConstants.State mLastSimState;
    private String mNewPinText;
    private int mNextRepollStatePhoneId;
    private int mPhoneId;
    private String mPukText;
    private AlertDialog mRemainingAttemptsDialog;
    private StringBuffer mSb;
    private KeyguardSecurityModel mSecurityModel;
    private AlertDialog mSimCardDialog;
    private volatile boolean mSimCheckInProgress;
    private ImageView mSimImageView;
    private ProgressDialog mSimUnlockProgressDialog;
    private int mUnlockEnterState;
    KeyguardUpdateMonitor mUpdateMonitor;
    KeyguardUpdateMonitorCallback mUpdateMonitorCallback;
    private String[] strLockName;

    /* loaded from: a.zip:com/mediatek/keyguard/Telephony/KeyguardSimPinPukMeView$CheckSimMe.class */
    private abstract class CheckSimMe extends Thread {
        private final String mPasswd;
        private int mResult;
        final KeyguardSimPinPukMeView this$0;

        protected CheckSimMe(KeyguardSimPinPukMeView keyguardSimPinPukMeView, String str, int i) {
            this.this$0 = keyguardSimPinPukMeView;
            this.mPasswd = str;
        }

        abstract void onSimMeCheckResponse(int i);

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            try {
                Log.d("KeyguardSimPinPukMeView", "CheckMe, mPhoneId =" + this.this$0.mPhoneId);
                this.mResult = ITelephonyEx.Stub.asInterface(ServiceManager.getService("phoneEx")).supplyNetworkDepersonalization(KeyguardUtils.getSubIdUsingPhoneId(this.this$0.mPhoneId), this.mPasswd);
                Log.d("KeyguardSimPinPukMeView", "CheckMe, mPhoneId =" + this.this$0.mPhoneId + " mResult=" + this.mResult);
                if (this.mResult == 0) {
                    Log.d("KeyguardSimPinPukMeView", "CheckSimMe.run(), VERIFY_RESULT_PASS == ret, so we postDelayed a timeout runnable object");
                }
                this.this$0.mHandler.post(new Runnable(this) { // from class: com.mediatek.keyguard.Telephony.KeyguardSimPinPukMeView.CheckSimMe.1
                    final CheckSimMe this$1;

                    {
                        this.this$1 = this;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.this$1.onSimMeCheckResponse(this.this$1.mResult);
                    }
                });
            } catch (RemoteException e) {
                this.this$0.mHandler.post(new Runnable(this) { // from class: com.mediatek.keyguard.Telephony.KeyguardSimPinPukMeView.CheckSimMe.2
                    final CheckSimMe this$1;

                    {
                        this.this$1 = this;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.this$1.onSimMeCheckResponse(2);
                    }
                });
            }
        }
    }

    /* loaded from: a.zip:com/mediatek/keyguard/Telephony/KeyguardSimPinPukMeView$CheckSimPinPuk.class */
    private abstract class CheckSimPinPuk extends Thread {
        private final String mPin;
        private final String mPuk;
        private int[] mResult;
        final KeyguardSimPinPukMeView this$0;

        protected CheckSimPinPuk(KeyguardSimPinPukMeView keyguardSimPinPukMeView, String str, int i) {
            this.this$0 = keyguardSimPinPukMeView;
            this.mPin = str;
            this.mPuk = null;
        }

        protected CheckSimPinPuk(KeyguardSimPinPukMeView keyguardSimPinPukMeView, String str, String str2, int i) {
            this.this$0 = keyguardSimPinPukMeView;
            this.mPin = str2;
            this.mPuk = str;
        }

        abstract void onSimCheckResponse(int i, int i2);

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            try {
                Log.d("KeyguardSimPinPukMeView", "CheckSimPinPuk, mPhoneId =" + this.this$0.mPhoneId);
                if (this.this$0.mUpdateMonitor.getSimStateOfPhoneId(this.this$0.mPhoneId) == IccCardConstants.State.PIN_REQUIRED) {
                    ITelephony asInterface = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
                    if (asInterface != null) {
                        this.mResult = asInterface.supplyPinReportResultForSubscriber(KeyguardUtils.getSubIdUsingPhoneId(this.this$0.mPhoneId), this.mPin);
                    } else {
                        Log.d("KeyguardSimPinPukMeView", "phoneService is gone, skip supplyPinForSubscriber().");
                    }
                } else if (this.this$0.mUpdateMonitor.getSimStateOfPhoneId(this.this$0.mPhoneId) == IccCardConstants.State.PUK_REQUIRED) {
                    ITelephony asInterface2 = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
                    if (asInterface2 != null) {
                        this.mResult = asInterface2.supplyPukReportResultForSubscriber(KeyguardUtils.getSubIdUsingPhoneId(this.this$0.mPhoneId), this.mPuk, this.mPin);
                    } else {
                        Log.d("KeyguardSimPinPukMeView", "phoneService is gone, skip supplyPukForSubscriber().");
                    }
                }
                if (this.mResult == null) {
                    this.this$0.mHandler.post(new Runnable(this) { // from class: com.mediatek.keyguard.Telephony.KeyguardSimPinPukMeView.CheckSimPinPuk.2
                        final CheckSimPinPuk this$1;

                        {
                            this.this$1 = this;
                        }

                        @Override // java.lang.Runnable
                        public void run() {
                            this.this$1.onSimCheckResponse(2, -1);
                        }
                    });
                    Log.d("KeyguardSimPinPukMeView", "there is an error with sim fw");
                    return;
                }
                Log.v("KeyguardSimPinPukMeView", "supplyPinReportResultForSubscriber returned: " + this.mResult[0] + " " + this.mResult[1]);
                Log.d("KeyguardSimPinPukMeView", "CheckSimPinPuk.run(),mResult is true(success), so we postDelayed a timeout runnable object");
                this.this$0.mHandler.post(new Runnable(this) { // from class: com.mediatek.keyguard.Telephony.KeyguardSimPinPukMeView.CheckSimPinPuk.1
                    final CheckSimPinPuk this$1;

                    {
                        this.this$1 = this;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.this$1.onSimCheckResponse(this.this$1.mResult[0], this.this$1.mResult[1]);
                    }
                });
            } catch (RemoteException e) {
                this.this$0.mHandler.post(new Runnable(this) { // from class: com.mediatek.keyguard.Telephony.KeyguardSimPinPukMeView.CheckSimPinPuk.3
                    final CheckSimPinPuk this$1;

                    {
                        this.this$1 = this;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.this$1.onSimCheckResponse(2, -1);
                    }
                });
            }
        }
    }

    /* loaded from: a.zip:com/mediatek/keyguard/Telephony/KeyguardSimPinPukMeView$Toast.class */
    public static class Toast {
        final Context mContext;
        private INotificationManager mService;
        View mView;
        int mY;
        final Handler mHandler = new Handler();
        int mGravity = 81;
        final TN mTN = new TN(this);

        /* JADX INFO: Access modifiers changed from: private */
        /* loaded from: a.zip:com/mediatek/keyguard/Telephony/KeyguardSimPinPukMeView$Toast$TN.class */
        public class TN extends ITransientNotification.Stub {
            WindowManagerImpl mWM;
            final Toast this$1;
            final Runnable mShow = new Runnable(this) { // from class: com.mediatek.keyguard.Telephony.KeyguardSimPinPukMeView.Toast.TN.1
                final TN this$2;

                {
                    this.this$2 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$2.handleShow();
                }
            };
            final Runnable mHide = new Runnable(this) { // from class: com.mediatek.keyguard.Telephony.KeyguardSimPinPukMeView.Toast.TN.2
                final TN this$2;

                {
                    this.this$2 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$2.handleHide();
                }
            };
            private final WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();

            TN(Toast toast) {
                this.this$1 = toast;
                WindowManager.LayoutParams layoutParams = this.mParams;
                layoutParams.height = -2;
                layoutParams.width = -2;
                layoutParams.flags = 152;
                layoutParams.format = -3;
                layoutParams.windowAnimations = 16973828;
                layoutParams.type = 2009;
                layoutParams.setTitle("Toast");
            }

            public void handleHide() {
                if (this.this$1.mView != null) {
                    if (this.this$1.mView.getParent() != null) {
                        this.mWM.removeView(this.this$1.mView);
                    }
                    this.this$1.mView = null;
                }
            }

            public void handleShow() {
                this.mWM = (WindowManagerImpl) this.this$1.mContext.getSystemService("window");
                int i = this.this$1.mGravity;
                this.mParams.gravity = i;
                if ((i & 7) == 7) {
                    this.mParams.horizontalWeight = 1.0f;
                }
                if ((i & 112) == 112) {
                    this.mParams.verticalWeight = 1.0f;
                }
                this.mParams.y = this.this$1.mY;
                if (this.this$1.mView != null) {
                    if (this.this$1.mView.getParent() != null) {
                        this.mWM.removeView(this.this$1.mView);
                    }
                    this.mWM.addView(this.this$1.mView, this.mParams);
                }
            }

            public void hide() {
                this.this$1.mHandler.post(this.mHide);
            }

            public void show() {
                this.this$1.mHandler.post(this.mShow);
            }
        }

        public Toast(Context context) {
            this.mContext = context;
            this.mY = context.getResources().getDimensionPixelSize(17104918);
        }

        private INotificationManager getService() {
            if (this.mService != null) {
                return this.mService;
            }
            this.mService = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
            return this.mService;
        }

        public static Toast makeText(Context context, CharSequence charSequence) {
            Toast toast = new Toast(context);
            View inflate = ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(17367295, (ViewGroup) null);
            ((TextView) inflate.findViewById(16908299)).setText(charSequence);
            toast.mView = inflate;
            return toast;
        }

        public void show() {
            if (this.mView == null) {
                throw new RuntimeException("setView must have been called");
            }
            try {
                getService().enqueueToast(this.mContext.getPackageName(), this.mTN, 0);
            } catch (RemoteException e) {
            }
        }
    }

    public KeyguardSimPinPukMeView(Context context) {
        this(context, null);
    }

    public KeyguardSimPinPukMeView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mSimUnlockProgressDialog = null;
        this.mUpdateMonitor = null;
        this.mSb = null;
        this.mNextRepollStatePhoneId = -1;
        this.mLastSimState = IccCardConstants.State.UNKNOWN;
        this.strLockName = new String[]{" [NP]", " [NSP]", " [SP]", " [CP]", " [SIMP]"};
        this.mHandler = new Handler(Looper.myLooper(), null, true);
        this.mDismissSimPinPukRunnable = new Runnable(this) { // from class: com.mediatek.keyguard.Telephony.KeyguardSimPinPukMeView.1
            final KeyguardSimPinPukMeView this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mUpdateMonitor.reportSimUnlocked(this.this$0.mPhoneId);
            }
        };
        this.mUpdateMonitorCallback = new KeyguardUpdateMonitorCallback(this) { // from class: com.mediatek.keyguard.Telephony.KeyguardSimPinPukMeView.2
            final KeyguardSimPinPukMeView this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onAirPlaneModeChanged(boolean z) {
                Log.d("KeyguardSimPinPukMeView", "onAirPlaneModeChanged(airPlaneModeEnabled = " + z + ")");
                if (z) {
                    Log.d("KeyguardSimPinPukMeView", "Flight-Mode turns on & keyguard is showing, dismiss keyguard.");
                    this.this$0.mPasswordEntry.reset(true, true);
                    this.this$0.mCallback.userActivity();
                    this.this$0.mCallback.dismiss(true);
                }
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onSimStateChangedUsingPhoneId(int i, IccCardConstants.State state) {
                Log.d("KeyguardSimPinPukMeView", "onSimStateChangedUsingPhoneId: " + state + ", phoneId = " + i + ", mPhoneId = " + this.this$0.mPhoneId);
                Log.d("KeyguardSimPinPukMeView", "onSimStateChangedUsingPhoneId: mCallback = " + this.this$0.mCallback);
                if (i != this.this$0.mPhoneId) {
                    if (i == this.this$0.mNextRepollStatePhoneId) {
                        Log.d("KeyguardSimPinPukMeView", "onSimStateChanged: mNextRepollStatePhoneId = " + this.this$0.mNextRepollStatePhoneId);
                        if (this.this$0.mSimUnlockProgressDialog != null) {
                            this.this$0.mSimUnlockProgressDialog.hide();
                        }
                        if (IccCardConstants.State.READY != state) {
                            this.this$0.mCallback.dismiss(true);
                            this.this$0.mLastSimState = state;
                            return;
                        }
                        this.this$0.mLastSimState = IccCardConstants.State.NETWORK_LOCKED;
                        this.this$0.simStateReadyProcess();
                        return;
                    }
                    return;
                }
                this.this$0.resetState(true);
                if (this.this$0.mSimUnlockProgressDialog != null) {
                    this.this$0.mSimUnlockProgressDialog.hide();
                }
                this.this$0.mHandler.removeCallbacks(this.this$0.mDismissSimPinPukRunnable);
                if (IccCardConstants.State.READY == state) {
                    this.this$0.simStateReadyProcess();
                } else if (IccCardConstants.State.NOT_READY == state || IccCardConstants.State.ABSENT == state) {
                    Log.d("KeyguardSimPinPukMeView", "onSimStateChangedUsingPhoneId: not ready, phoneId = " + i);
                    this.this$0.mCallback.dismiss(true);
                    this.this$0.mSimCheckInProgress = false;
                    Log.d("KeyguardSimPinPukMeView", "set mSimCheckInProgress false");
                } else if (IccCardConstants.State.NETWORK_LOCKED == state) {
                    if (!KeyguardUtils.isMediatekSimMeLockSupport()) {
                        this.this$0.mCallback.dismiss(true);
                    } else if (this.this$0.getRetryMeCount(this.this$0.mPhoneId) == 0) {
                        Log.d("KeyguardSimPinPukMeView", "onSimStateChanged: ME retrycount is 0, dismiss it");
                        this.this$0.mUpdateMonitor.setPinPukMeDismissFlagOfPhoneId(i, true);
                        this.this$0.mCallback.dismiss(true);
                    }
                }
                this.this$0.mLastSimState = state;
                Log.d("KeyguardSimPinPukMeView", "assign mLastSimState=" + this.this$0.mLastSimState);
            }
        };
        this.mKeyguardUtils = new KeyguardUtils(context);
        this.mSb = new StringBuffer();
        this.mUpdateMonitor = KeyguardUpdateMonitor.getInstance(getContext());
        this.mSecurityModel = new KeyguardSecurityModel(getContext());
        try {
            this.mKeyguardUtilExt = KeyguardPluginFactory.getKeyguardUtilExt(context);
            this.mIOperatorSIMString = KeyguardPluginFactory.getOperatorSIMString(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* JADX WARN: Type inference failed for: r0v5, types: [com.mediatek.keyguard.Telephony.KeyguardSimPinPukMeView$5] */
    private void checkMe(int i) {
        getSimUnlockProgressDialog().show();
        if (this.mSimCheckInProgress) {
            return;
        }
        this.mSimCheckInProgress = true;
        new CheckSimMe(this, this, this.mPasswordEntry.getText().toString(), i) { // from class: com.mediatek.keyguard.Telephony.KeyguardSimPinPukMeView.5
            final KeyguardSimPinPukMeView this$0;

            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            {
                super(this, r8, i);
                this.this$0 = this;
            }

            @Override // com.mediatek.keyguard.Telephony.KeyguardSimPinPukMeView.CheckSimMe
            void onSimMeCheckResponse(int i2) {
                Log.d("KeyguardSimPinPukMeView", "checkMe onSimChangedResponse, ret = " + i2);
                if (i2 == 0) {
                    Log.d("KeyguardSimPinPukMeView", "checkMe VERIFY_RESULT_PASS == ret(we had sent runnable before");
                    this.this$0.mUpdateMonitor.reportSimUnlocked(this.this$0.mPhoneId);
                    this.this$0.mCallback.dismiss(true);
                } else if (1 == i2) {
                    this.this$0.mSb.delete(0, this.this$0.mSb.length());
                    this.this$0.minusRetryMeCount(this.this$0.mPhoneId);
                    if (this.this$0.mSimUnlockProgressDialog != null) {
                        this.this$0.mSimUnlockProgressDialog.hide();
                    }
                    if (this.this$0.mUnlockEnterState == 5) {
                        if (this.this$0.getRetryMeCount(this.this$0.mPhoneId) == 0) {
                            this.this$0.setInputInvalidAlertDialog(this.this$0.mContext.getText(R$string.simlock_slot_locked_message), true);
                            this.this$0.mUpdateMonitor.setPinPukMeDismissFlagOfPhoneId(this.this$0.mPhoneId, true);
                            this.this$0.mCallback.dismiss(true);
                        } else {
                            int simMeCategoryOfPhoneId = this.this$0.mUpdateMonitor.getSimMeCategoryOfPhoneId(this.this$0.mPhoneId);
                            this.this$0.mSb.append(this.this$0.mContext.getText(R$string.keyguard_wrong_code_input));
                            this.this$0.mSb.append(this.this$0.mContext.getText(R$string.simlock_entersimmelock));
                            this.this$0.mSb.append(this.this$0.strLockName[simMeCategoryOfPhoneId] + this.this$0.getRetryMeString(this.this$0.mPhoneId));
                        }
                        Log.d("KeyguardSimPinPukMeView", "checkMe() - VERIFY_INCORRECT_PASSWORD == ret, mSecurityMessageDisplay.setMessage = " + this.this$0.mSb.toString());
                        this.this$0.mSecurityMessageDisplay.setMessage((CharSequence) this.this$0.mSb.toString(), true);
                        this.this$0.mPasswordEntry.reset(true, true);
                    }
                } else if (2 == i2) {
                    if (this.this$0.mSimUnlockProgressDialog != null) {
                        this.this$0.mSimUnlockProgressDialog.hide();
                    }
                    this.this$0.setInputInvalidAlertDialog("Exception happen, fail to unlock", true);
                    this.this$0.mUpdateMonitor.setPinPukMeDismissFlagOfPhoneId(this.this$0.mPhoneId, true);
                    this.this$0.mCallback.dismiss(true);
                }
                this.this$0.mCallback.userActivity();
                this.this$0.mSimCheckInProgress = false;
            }
        }.start();
    }

    /* JADX WARN: Type inference failed for: r0v7, types: [com.mediatek.keyguard.Telephony.KeyguardSimPinPukMeView$3] */
    private void checkPin(int i) {
        getSimUnlockProgressDialog().show();
        Log.d("KeyguardSimPinPukMeView", "mSimCheckInProgress: " + this.mSimCheckInProgress);
        if (this.mSimCheckInProgress) {
            return;
        }
        this.mSimCheckInProgress = true;
        new CheckSimPinPuk(this, this, this.mPasswordEntry.getText().toString(), i) { // from class: com.mediatek.keyguard.Telephony.KeyguardSimPinPukMeView.3
            final KeyguardSimPinPukMeView this$0;

            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            {
                super(this, r8, i);
                this.this$0 = this;
            }

            @Override // com.mediatek.keyguard.Telephony.KeyguardSimPinPukMeView.CheckSimPinPuk
            void onSimCheckResponse(int i2, int i3) {
                this.this$0.resetPasswordText(true, i2 != 0);
                if (i2 == 0) {
                    this.this$0.mKeyguardUtilExt.showToastWhenUnlockPinPuk(this.this$0.mContext, 501);
                    Log.d("KeyguardSimPinPukMeView", "checkPin() success");
                    this.this$0.mUpdateMonitor.reportSimUnlocked(this.this$0.mPhoneId);
                    this.this$0.mCallback.dismiss(true);
                } else {
                    if (this.this$0.mSimUnlockProgressDialog != null) {
                        this.this$0.mSimUnlockProgressDialog.hide();
                    }
                    if (i2 != 1) {
                        this.this$0.mSecurityMessageDisplay.setMessage((CharSequence) this.this$0.getContext().getString(R$string.kg_password_pin_failed), true);
                    } else if (i3 <= 2) {
                        this.this$0.getSimRemainingAttemptsDialog(i3).show();
                        this.this$0.mSecurityMessageDisplay.setMessage((CharSequence) this.this$0.getPinPasswordErrorMessage(i3), true);
                    } else {
                        this.this$0.mSecurityMessageDisplay.setMessage((CharSequence) this.this$0.getPinPasswordErrorMessage(i3), true);
                    }
                    Log.d("KeyguardSimPinPukMeView", "verifyPasswordAndUnlock  CheckSimPin.onSimCheckResponse: " + i2 + " attemptsRemaining=" + i3);
                    this.this$0.resetPasswordText(true, i2 != 0);
                }
                this.this$0.mCallback.userActivity();
                this.this$0.mSimCheckInProgress = false;
            }
        }.start();
    }

    /* JADX WARN: Type inference failed for: r0v10, types: [com.mediatek.keyguard.Telephony.KeyguardSimPinPukMeView$4] */
    private void checkPuk(int i) {
        updatePinEnterScreen();
        if (this.mUnlockEnterState != 4) {
            return;
        }
        getSimUnlockProgressDialog().show();
        Log.d("KeyguardSimPinPukMeView", "mSimCheckInProgress: " + this.mSimCheckInProgress);
        if (this.mSimCheckInProgress) {
            return;
        }
        this.mSimCheckInProgress = true;
        new CheckSimPinPuk(this, this, this.mPukText, this.mNewPinText, i) { // from class: com.mediatek.keyguard.Telephony.KeyguardSimPinPukMeView.4
            final KeyguardSimPinPukMeView this$0;

            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            {
                super(this, r9, r10, i);
                this.this$0 = this;
            }

            @Override // com.mediatek.keyguard.Telephony.KeyguardSimPinPukMeView.CheckSimPinPuk
            void onSimCheckResponse(int i2, int i3) {
                this.this$0.resetPasswordText(true, i2 != 0);
                if (i2 == 0) {
                    Log.d("KeyguardSimPinPukMeView", "checkPuk onSimCheckResponse, success!");
                    this.this$0.mKeyguardUtilExt.showToastWhenUnlockPinPuk(this.this$0.mContext, 502);
                    this.this$0.mUpdateMonitor.reportSimUnlocked(this.this$0.mPhoneId);
                    this.this$0.mCallback.dismiss(true);
                } else {
                    if (this.this$0.mSimUnlockProgressDialog != null) {
                        this.this$0.mSimUnlockProgressDialog.hide();
                    }
                    this.this$0.mUnlockEnterState = 1;
                    if (i2 != 1) {
                        this.this$0.mSecurityMessageDisplay.setMessage((CharSequence) this.this$0.getContext().getString(R$string.kg_password_puk_failed), true);
                    } else if (i3 <= 2) {
                        this.this$0.getPukRemainingAttemptsDialog(i3).show();
                        this.this$0.mSecurityMessageDisplay.setMessage((CharSequence) this.this$0.getPukPasswordErrorMessage(i3), true);
                    } else {
                        this.this$0.mSecurityMessageDisplay.setMessage((CharSequence) this.this$0.getPukPasswordErrorMessage(i3), true);
                    }
                    Log.d("KeyguardSimPinPukMeView", "verifyPasswordAndUnlock  UpdateSim.onSimCheckResponse:  attemptsRemaining=" + i3);
                }
                this.this$0.mCallback.userActivity();
                this.this$0.mSimCheckInProgress = false;
            }
        }.start();
    }

    private void dealWithPinOrPukUnlock() {
        if (this.mUpdateMonitor.getSimStateOfPhoneId(this.mPhoneId) == IccCardConstants.State.PIN_REQUIRED) {
            Log.d("KeyguardSimPinPukMeView", "onClick, check PIN, mPhoneId=" + this.mPhoneId);
            checkPin(this.mPhoneId);
        } else if (this.mUpdateMonitor.getSimStateOfPhoneId(this.mPhoneId) == IccCardConstants.State.PUK_REQUIRED) {
            Log.d("KeyguardSimPinPukMeView", "onClick, check PUK, mPhoneId=" + this.mPhoneId);
            checkPuk(this.mPhoneId);
        } else if (this.mUpdateMonitor.getSimStateOfPhoneId(this.mPhoneId) != IccCardConstants.State.NETWORK_LOCKED || !KeyguardUtils.isMediatekSimMeLockSupport()) {
            Log.d("KeyguardSimPinPukMeView", "wrong status, mPhoneId=" + this.mPhoneId);
        } else {
            Log.d("KeyguardSimPinPukMeView", "onClick, check ME, mPhoneId=" + this.mPhoneId);
            checkMe(this.mPhoneId);
        }
    }

    private int getNextRepollStatePhoneId() {
        if (IccCardConstants.State.NETWORK_LOCKED == this.mLastSimState && KeyguardUtils.isMediatekSimMeLockSupport()) {
            for (int i = 0; i < KeyguardUtils.getNumOfPhone(); i++) {
                if (this.mSecurityModel.isPinPukOrMeRequiredOfPhoneId(i)) {
                    if (this.mUpdateMonitor.getSimStateOfPhoneId(i) == IccCardConstants.State.NETWORK_LOCKED) {
                        return i;
                    }
                    return -1;
                }
            }
            return -1;
        }
        return -1;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String getPinPasswordErrorMessage(int i) {
        String string = i == 0 ? getContext().getString(R$string.kg_password_wrong_pin_code_pukked) : i > 0 ? getContext().getResources().getQuantityString(R$plurals.kg_password_wrong_pin_code, i, Integer.valueOf(i)) : getContext().getString(R$string.kg_password_pin_failed);
        Log.d("KeyguardSimPinPukMeView", "getPinPasswordErrorMessage: attemptsRemaining=" + i + " displayMessage=" + string);
        return string;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String getPukPasswordErrorMessage(int i) {
        String string = i == 0 ? getContext().getString(R$string.kg_password_wrong_puk_code_dead) : i > 0 ? getContext().getResources().getQuantityString(R$plurals.kg_password_wrong_puk_code, i, Integer.valueOf(i)) : getContext().getString(R$string.kg_password_puk_failed);
        Log.d("KeyguardSimPinPukMeView", "getPukPasswordErrorMessage: attemptsRemaining=" + i + " displayMessage=" + string);
        return string;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Dialog getPukRemainingAttemptsDialog(int i) {
        String pukPasswordErrorMessage = getPukPasswordErrorMessage(i);
        if (this.mRemainingAttemptsDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
            builder.setMessage(pukPasswordErrorMessage);
            builder.setCancelable(false);
            builder.setNeutralButton(R$string.ok, (DialogInterface.OnClickListener) null);
            this.mRemainingAttemptsDialog = builder.create();
            this.mRemainingAttemptsDialog.getWindow().setType(2009);
        } else {
            this.mRemainingAttemptsDialog.setMessage(pukPasswordErrorMessage);
        }
        return this.mRemainingAttemptsDialog;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int getRetryMeCount(int i) {
        return this.mUpdateMonitor.getSimMeLeftRetryCountOfPhoneId(i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String getRetryMeString(int i) {
        return "(" + this.mContext.getString(R$string.retries_left, Integer.valueOf(getRetryMeCount(i))) + ")";
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Dialog getSimRemainingAttemptsDialog(int i) {
        String pinPasswordErrorMessage = getPinPasswordErrorMessage(i);
        if (this.mRemainingAttemptsDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
            builder.setMessage(pinPasswordErrorMessage);
            builder.setCancelable(false);
            builder.setNeutralButton(R$string.ok, (DialogInterface.OnClickListener) null);
            this.mRemainingAttemptsDialog = builder.create();
            this.mRemainingAttemptsDialog.getWindow().setType(2009);
        } else {
            this.mRemainingAttemptsDialog.setMessage(pinPasswordErrorMessage);
        }
        return this.mRemainingAttemptsDialog;
    }

    private Dialog getSimUnlockProgressDialog() {
        if (this.mSimUnlockProgressDialog == null) {
            this.mSimUnlockProgressDialog = new ProgressDialog(this.mContext);
            this.mSimUnlockProgressDialog.setMessage(this.mIOperatorSIMString.getOperatorSIMString(this.mContext.getString(R$string.kg_sim_unlock_progress_dialog_message), this.mPhoneId, IOperatorSIMString.SIMChangedTag.DELSIM, this.mContext));
            this.mSimUnlockProgressDialog.setIndeterminate(true);
            this.mSimUnlockProgressDialog.setCancelable(false);
            if (!(this.mContext instanceof Activity)) {
                this.mSimUnlockProgressDialog.getWindow().setType(2009);
            }
        }
        return this.mSimUnlockProgressDialog;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void minusRetryMeCount(int i) {
        this.mUpdateMonitor.minusSimMeLeftRetryCountOfPhoneId(i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setInputInvalidAlertDialog(CharSequence charSequence, boolean z) {
        StringBuilder sb = new StringBuilder(charSequence);
        if (!z) {
            Toast.makeText(this.mContext, sb).show();
            return;
        }
        AlertDialog create = new AlertDialog.Builder(this.mContext).setMessage(sb).setPositiveButton(17039370, (DialogInterface.OnClickListener) null).setCancelable(true).create();
        create.getWindow().setType(2009);
        create.getWindow().addFlags(2);
        create.show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void simStateReadyProcess() {
        this.mNextRepollStatePhoneId = getNextRepollStatePhoneId();
        Log.d("KeyguardSimPinPukMeView", "simStateReadyProcess mNextRepollStatePhoneId =" + this.mNextRepollStatePhoneId);
        if (this.mNextRepollStatePhoneId == -1) {
            this.mCallback.dismiss(true);
            return;
        }
        try {
            getSimUnlockProgressDialog().show();
            Log.d("KeyguardSimPinPukMeView", "repollIccStateForNetworkLock phoneId =" + this.mNextRepollStatePhoneId);
            ITelephonyEx.Stub.asInterface(ServiceManager.getService("phoneEx")).repollIccStateForNetworkLock(KeyguardUtils.getSubIdUsingPhoneId(this.mNextRepollStatePhoneId), true);
        } catch (RemoteException e) {
            Log.d("KeyguardSimPinPukMeView", "repollIccStateForNetworkLock exception caught");
        }
    }

    private void updatePinEnterScreen() {
        switch (this.mUnlockEnterState) {
            case 1:
                this.mPukText = this.mPasswordEntry.getText().toString();
                if (!validatePin(this.mPukText, true)) {
                    Log.d("KeyguardSimPinPukMeView", "updatePinEnterScreen() - STATE_ENTER_PUK, validatePin = false ,mSecurityMessageDisplay.setMessage = R.string.invalidPuk");
                    this.mSecurityMessageDisplay.setMessage(R$string.invalidPuk, true);
                    break;
                } else {
                    this.mUnlockEnterState = 2;
                    this.mSb.delete(0, this.mSb.length());
                    this.mSb.append(this.mContext.getText(R$string.keyguard_password_enter_new_pin_code));
                    Log.d("KeyguardSimPinPukMeView", "updatePinEnterScreen() - STATE_ENTER_PUK, validatePin = true ,mSecurityMessageDisplay.setMessage = " + this.mSb.toString());
                    this.mSecurityMessageDisplay.setMessage((CharSequence) this.mSb.toString(), true);
                    break;
                }
            case 2:
                this.mNewPinText = this.mPasswordEntry.getText().toString();
                if (!validatePin(this.mNewPinText, false)) {
                    Log.d("KeyguardSimPinPukMeView", "updatePinEnterScreen() - STATE_ENTER_NEW, validatePin = false ,mSecurityMessageDisplay.setMessage = R.string.keyguard_code_length_prompt");
                    this.mSecurityMessageDisplay.setMessage(R$string.keyguard_code_length_prompt, true);
                    break;
                } else {
                    this.mUnlockEnterState = 3;
                    this.mSb.delete(0, this.mSb.length());
                    this.mSb.append(this.mContext.getText(R$string.keyguard_password_Confirm_pin_code));
                    Log.d("KeyguardSimPinPukMeView", "updatePinEnterScreen() - STATE_ENTER_NEW, validatePin = true ,mSecurityMessageDisplay.setMessage = " + this.mSb.toString());
                    this.mSecurityMessageDisplay.setMessage((CharSequence) this.mSb.toString(), true);
                    break;
                }
            case 3:
                if (!this.mNewPinText.equals(this.mPasswordEntry.getText().toString())) {
                    this.mUnlockEnterState = 2;
                    this.mSb.delete(0, this.mSb.length());
                    this.mSb.append(this.mContext.getText(R$string.keyguard_code_donnot_mismatch));
                    this.mSb.append(this.mContext.getText(R$string.keyguard_password_enter_new_pin_code));
                    Log.d("KeyguardSimPinPukMeView", "updatePinEnterScreen() - STATE_REENTER_NEW, true ,mSecurityMessageDisplay.setMessage = " + this.mSb.toString());
                    this.mSecurityMessageDisplay.setMessage((CharSequence) this.mSb.toString(), true);
                    break;
                } else {
                    Log.d("KeyguardSimPinPukMeView", "updatePinEnterScreen() - STATE_REENTER_NEW, false ,mSecurityMessageDisplay.setMessage = empty string.");
                    this.mUnlockEnterState = 4;
                    this.mSecurityMessageDisplay.setMessage((CharSequence) "", true);
                    break;
                }
        }
        this.mPasswordEntry.reset(true, true);
        this.mCallback.userActivity();
    }

    private boolean validatePin(String str, boolean z) {
        return str != null && str.length() >= (z ? 8 : 4) && str.length() <= 8;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public int getPasswordTextViewId() {
        return R$id.simPinPukMeEntry;
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.d("KeyguardSimPinPukMeView", "onAttachedToWindow");
        this.mUpdateMonitor.registerCallback(this.mUpdateMonitorCallback);
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d("KeyguardSimPinPukMeView", "onDetachedFromWindow");
        this.mHandler.removeCallbacks(this.mDismissSimPinPukRunnable);
        this.mUpdateMonitor.removeCallback(this.mUpdateMonitorCallback);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardPinBasedInputView, com.android.keyguard.KeyguardAbsKeyInputView, android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mPhoneId = -1;
        this.mSecurityMessageDisplay.setTimeout(0);
        if (this.mEcaView instanceof EmergencyCarrierArea) {
            ((EmergencyCarrierArea) this.mEcaView).setCarrierTextVisible(true);
            this.mKeyguardUtilExt.customizeCarrierTextGravity((TextView) this.mEcaView.findViewById(R$id.carrier_text));
        }
        this.mSimImageView = (ImageView) findViewById(R$id.keyguard_sim);
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView, com.android.keyguard.KeyguardSecurityView
    public void onPause() {
        if (this.mSimUnlockProgressDialog != null) {
            this.mSimUnlockProgressDialog.dismiss();
            this.mSimUnlockProgressDialog = null;
        }
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView, com.android.keyguard.KeyguardSecurityView
    public void onResume(int i) {
        if (this.mSimUnlockProgressDialog != null) {
            this.mSimUnlockProgressDialog.dismiss();
            this.mSimUnlockProgressDialog = null;
        }
        InputMethodManager inputMethodManager = (InputMethodManager) this.mContext.getSystemService("input_method");
        if (inputMethodManager.isActive()) {
            Log.i("KeyguardSimPinPukMeView", "IME is showing, we should hide it");
            inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 2);
        }
    }

    @Override // android.view.View
    public void onWindowFocusChanged(boolean z) {
        Log.d("KeyguardSimPinPukMeView", "onWindowFocusChanged(hasWindowFocus = " + z + ")");
        if (z) {
            resetPasswordText(true, false);
            KeyguardUtils.requestImeStatusRefresh(this.mContext);
        }
    }

    @Override // com.android.keyguard.KeyguardPinBasedInputView, com.android.keyguard.KeyguardAbsKeyInputView
    public void resetState() {
        resetState(false);
    }

    public void resetState(boolean z) {
        String str;
        int i;
        String str2;
        Log.v("KeyguardSimPinPukMeView", "Resetting state");
        super.resetState();
        TextView textView = (TextView) findViewById(R$id.slot_num_text);
        textView.setText(this.mContext.getString(R$string.kg_slot_id, Integer.valueOf(this.mPhoneId + 1)) + " ");
        Resources resources = getResources();
        int numOfPhone = KeyguardUtils.getNumOfPhone();
        IccCardConstants.State simStateOfPhoneId = this.mUpdateMonitor.getSimStateOfPhoneId(this.mPhoneId);
        if (numOfPhone >= 2) {
            int subIdUsingPhoneId = KeyguardUtils.getSubIdUsingPhoneId(this.mPhoneId);
            SubscriptionInfo subscriptionInfoForSubId = this.mUpdateMonitor.getSubscriptionInfoForSubId(subIdUsingPhoneId, z);
            String displayName = subscriptionInfoForSubId != null ? subscriptionInfoForSubId.getDisplayName() : "";
            if (subscriptionInfoForSubId == null) {
                displayName = "CARD " + Integer.toString(this.mPhoneId + 1);
                Log.d("KeyguardSimPinPukMeView", "we set a displayname");
            }
            Log.d("KeyguardSimPinPukMeView", "resetState() - subId = " + subIdUsingPhoneId + ", displayName = " + ((CharSequence) displayName));
            if (simStateOfPhoneId == IccCardConstants.State.PIN_REQUIRED) {
                str = resources.getString(R$string.kg_sim_pin_instructions_multi, displayName);
                this.mUnlockEnterState = 0;
            } else if (simStateOfPhoneId == IccCardConstants.State.PUK_REQUIRED) {
                str = resources.getString(R$string.kg_puk_enter_puk_hint_multi, displayName);
                this.mUnlockEnterState = 1;
            } else {
                str = "";
                if (IccCardConstants.State.NETWORK_LOCKED == simStateOfPhoneId) {
                    str = "";
                    if (KeyguardUtils.isMediatekSimMeLockSupport()) {
                        str = resources.getString(R$string.simlock_entersimmelock) + this.strLockName[this.mUpdateMonitor.getSimMeCategoryOfPhoneId(this.mPhoneId)] + getRetryMeString(this.mPhoneId);
                        this.mUnlockEnterState = 5;
                    }
                }
            }
            i = -1;
            str2 = str;
            if (subscriptionInfoForSubId != null) {
                i = subscriptionInfoForSubId.getIconTint();
                str2 = str;
            }
        } else if (simStateOfPhoneId == IccCardConstants.State.PIN_REQUIRED) {
            str2 = resources.getString(R$string.kg_sim_pin_instructions);
            this.mUnlockEnterState = 0;
            i = -1;
        } else if (simStateOfPhoneId == IccCardConstants.State.PUK_REQUIRED) {
            str2 = resources.getString(R$string.kg_puk_enter_puk_hint);
            this.mUnlockEnterState = 1;
            i = -1;
        } else {
            i = -1;
            str2 = "";
            if (IccCardConstants.State.NETWORK_LOCKED == simStateOfPhoneId) {
                i = -1;
                str2 = "";
                if (KeyguardUtils.isMediatekSimMeLockSupport()) {
                    str2 = resources.getString(R$string.simlock_entersimmelock) + this.strLockName[this.mUpdateMonitor.getSimMeCategoryOfPhoneId(this.mPhoneId)] + getRetryMeString(this.mPhoneId);
                    this.mUnlockEnterState = 5;
                    i = -1;
                }
            }
        }
        this.mKeyguardUtilExt.customizePinPukLockView(this.mPhoneId, this.mSimImageView, textView);
        this.mSimImageView.setImageTintList(ColorStateList.valueOf(i));
        String operatorSIMString = this.mIOperatorSIMString.getOperatorSIMString(str2, this.mPhoneId, IOperatorSIMString.SIMChangedTag.DELSIM, this.mContext);
        Log.d("KeyguardSimPinPukMeView", "resetState() - mSecurityMessageDisplay.setMessage = " + operatorSIMString);
        this.mSecurityMessageDisplay.setMessage((CharSequence) operatorSIMString, true);
    }

    public void setPhoneId(int i) {
        this.mPhoneId = i;
        Log.i("KeyguardSimPinPukMeView", "setPhoneId=" + i);
        resetState();
        if (this.mSimCardDialog != null) {
            if (this.mSimCardDialog.isShowing()) {
                this.mSimCardDialog.dismiss();
            }
            this.mSimCardDialog = null;
        }
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    protected boolean shouldLockout(long j) {
        return false;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void startAppearAnimation() {
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView, com.android.keyguard.KeyguardSecurityView
    public boolean startDisappearAnimation(Runnable runnable) {
        return false;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public void verifyPasswordAndUnlock() {
        if (validatePin(this.mPasswordEntry.getText().toString(), false) || !(this.mUpdateMonitor.getSimStateOfPhoneId(this.mPhoneId) == IccCardConstants.State.PIN_REQUIRED || (this.mUpdateMonitor.getSimStateOfPhoneId(this.mPhoneId) == IccCardConstants.State.NETWORK_LOCKED && KeyguardUtils.isMediatekSimMeLockSupport()))) {
            this.mPasswordEntry.setEnabled(true);
            dealWithPinOrPukUnlock();
            return;
        }
        if (this.mUpdateMonitor.getSimStateOfPhoneId(this.mPhoneId) == IccCardConstants.State.PIN_REQUIRED) {
            this.mSecurityMessageDisplay.setMessage(R$string.kg_invalid_sim_pin_hint, true);
        } else {
            this.mSecurityMessageDisplay.setMessage(R$string.keyguard_code_length_prompt, true);
        }
        this.mPasswordEntry.reset(true, true);
        this.mPasswordEntry.setEnabled(true);
        this.mCallback.userActivity();
    }
}
