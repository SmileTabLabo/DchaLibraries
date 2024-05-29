package com.android.keyguard;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.IccCardConstants;
/* loaded from: a.zip:com/android/keyguard/KeyguardSimPinView.class */
public class KeyguardSimPinView extends KeyguardPinBasedInputView {
    private CheckSimPin mCheckSimPinThread;
    KeyguardUtils mKeyguardUtils;
    private int mPhoneId;
    private AlertDialog mRemainingAttemptsDialog;
    private ProgressDialog mSimUnlockProgressDialog;
    private int mSubId;
    KeyguardUpdateMonitorCallback mUpdateMonitorCallback;

    /* renamed from: com.android.keyguard.KeyguardSimPinView$2  reason: invalid class name */
    /* loaded from: a.zip:com/android/keyguard/KeyguardSimPinView$2.class */
    class AnonymousClass2 extends CheckSimPin {
        final KeyguardSimPinView this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        AnonymousClass2(KeyguardSimPinView keyguardSimPinView, KeyguardSimPinView keyguardSimPinView2, String str) {
            super(keyguardSimPinView, str);
            this.this$0 = keyguardSimPinView2;
        }

        @Override // com.android.keyguard.KeyguardSimPinView.CheckSimPin
        void onSimCheckResponse(int i, int i2) {
            this.this$0.post(new Runnable(this, i, i2) { // from class: com.android.keyguard.KeyguardSimPinView.2.1
                final AnonymousClass2 this$1;
                final int val$attemptsRemaining;
                final int val$result;

                {
                    this.this$1 = this;
                    this.val$result = i;
                    this.val$attemptsRemaining = i2;
                }

                @Override // java.lang.Runnable
                public void run() {
                    boolean z = false;
                    if (this.this$1.this$0.mSimUnlockProgressDialog != null) {
                        this.this$1.this$0.mSimUnlockProgressDialog.hide();
                    }
                    if (this.val$result == 0) {
                        KeyguardUpdateMonitor.getInstance(this.this$1.this$0.getContext()).reportSimUnlocked(this.this$1.this$0.mPhoneId);
                        this.this$1.this$0.mCallback.dismiss(true);
                    } else {
                        if (this.val$result != 1) {
                            this.this$1.this$0.mSecurityMessageDisplay.setMessage((CharSequence) this.this$1.this$0.getContext().getString(R$string.kg_password_pin_failed), true);
                        } else if (this.val$attemptsRemaining <= 2) {
                            this.this$1.this$0.getSimRemainingAttemptsDialog(this.val$attemptsRemaining).show();
                        } else {
                            this.this$1.this$0.mSecurityMessageDisplay.setMessage((CharSequence) this.this$1.this$0.getPinPasswordErrorMessage(this.val$attemptsRemaining), true);
                        }
                        Log.d("KeyguardSimPinView", "verifyPasswordAndUnlock  CheckSimPin.onSimCheckResponse: " + this.val$result + " attemptsRemaining=" + this.val$attemptsRemaining);
                        KeyguardSimPinView keyguardSimPinView = this.this$1.this$0;
                        if (this.val$result != 0) {
                            z = true;
                        }
                        keyguardSimPinView.resetPasswordText(true, z);
                    }
                    this.this$1.this$0.mCallback.userActivity();
                    this.this$1.this$0.mCheckSimPinThread = null;
                }
            });
        }
    }

    /* loaded from: a.zip:com/android/keyguard/KeyguardSimPinView$CheckSimPin.class */
    private abstract class CheckSimPin extends Thread {
        private final String mPin;
        final KeyguardSimPinView this$0;

        protected CheckSimPin(KeyguardSimPinView keyguardSimPinView, String str) {
            this.this$0 = keyguardSimPinView;
            this.mPin = str;
        }

        abstract void onSimCheckResponse(int i, int i2);

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            try {
                Log.v("KeyguardSimPinView", "call supplyPinReportResultForSubscriber(subid=" + this.this$0.mSubId + ")");
                Log.d("KeyguardSimPinView", "call supplyPinReportResultForSubscriber() mPhoneId = " + this.this$0.mPhoneId);
                KeyguardUtils.getSubIdUsingPhoneId(this.this$0.mPhoneId);
                int[] supplyPinReportResultForSubscriber = ITelephony.Stub.asInterface(ServiceManager.checkService("phone")).supplyPinReportResultForSubscriber(this.this$0.mSubId, this.mPin);
                Log.v("KeyguardSimPinView", "supplyPinReportResult returned: " + supplyPinReportResultForSubscriber[0] + " " + supplyPinReportResultForSubscriber[1]);
                this.this$0.post(new Runnable(this, supplyPinReportResultForSubscriber) { // from class: com.android.keyguard.KeyguardSimPinView.CheckSimPin.1
                    final CheckSimPin this$1;
                    final int[] val$result;

                    {
                        this.this$1 = this;
                        this.val$result = supplyPinReportResultForSubscriber;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.this$1.onSimCheckResponse(this.val$result[0], this.val$result[1]);
                    }
                });
            } catch (RemoteException e) {
                Log.e("KeyguardSimPinView", "RemoteException for supplyPinReportResult:", e);
                this.this$0.post(new Runnable(this) { // from class: com.android.keyguard.KeyguardSimPinView.CheckSimPin.2
                    final CheckSimPin this$1;

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

    public KeyguardSimPinView(Context context) {
        this(context, null);
    }

    public KeyguardSimPinView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mSimUnlockProgressDialog = null;
        this.mPhoneId = 0;
        this.mUpdateMonitorCallback = new KeyguardUpdateMonitorCallback(this) { // from class: com.android.keyguard.KeyguardSimPinView.1

            /* renamed from: -com-android-internal-telephony-IccCardConstants$StateSwitchesValues  reason: not valid java name */
            private static final int[] f5x8dbfd0b5 = null;
            final KeyguardSimPinView this$0;

            /* renamed from: -getcom-android-internal-telephony-IccCardConstants$StateSwitchesValues  reason: not valid java name */
            private static /* synthetic */ int[] m623xf663cf59() {
                if (f5x8dbfd0b5 != null) {
                    return f5x8dbfd0b5;
                }
                int[] iArr = new int[IccCardConstants.State.values().length];
                try {
                    iArr[IccCardConstants.State.ABSENT.ordinal()] = 1;
                } catch (NoSuchFieldError e) {
                }
                try {
                    iArr[IccCardConstants.State.CARD_IO_ERROR.ordinal()] = 3;
                } catch (NoSuchFieldError e2) {
                }
                try {
                    iArr[IccCardConstants.State.NETWORK_LOCKED.ordinal()] = 4;
                } catch (NoSuchFieldError e3) {
                }
                try {
                    iArr[IccCardConstants.State.NOT_READY.ordinal()] = 2;
                } catch (NoSuchFieldError e4) {
                }
                try {
                    iArr[IccCardConstants.State.PERM_DISABLED.ordinal()] = 5;
                } catch (NoSuchFieldError e5) {
                }
                try {
                    iArr[IccCardConstants.State.PIN_REQUIRED.ordinal()] = 6;
                } catch (NoSuchFieldError e6) {
                }
                try {
                    iArr[IccCardConstants.State.PUK_REQUIRED.ordinal()] = 7;
                } catch (NoSuchFieldError e7) {
                }
                try {
                    iArr[IccCardConstants.State.READY.ordinal()] = 8;
                } catch (NoSuchFieldError e8) {
                }
                try {
                    iArr[IccCardConstants.State.UNKNOWN.ordinal()] = 9;
                } catch (NoSuchFieldError e9) {
                }
                f5x8dbfd0b5 = iArr;
                return iArr;
            }

            {
                this.this$0 = this;
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onSimStateChangedUsingPhoneId(int i, IccCardConstants.State state) {
                Log.d("KeyguardSimPinView", "onSimStateChangedUsingSubId: " + state + ", phoneId=" + i);
                switch (m623xf663cf59()[state.ordinal()]) {
                    case 1:
                    case 2:
                        if (i == this.this$0.mPhoneId) {
                            KeyguardUpdateMonitor.getInstance(this.this$0.getContext()).reportSimUnlocked(this.this$0.mPhoneId);
                            this.this$0.mCallback.dismiss(true);
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
        };
        this.mKeyguardUtils = new KeyguardUtils(context);
    }

    private void dealwithSIMInfoChanged() {
        String str = null;
        try {
            str = this.mKeyguardUtils.getOptrNameUsingPhoneId(this.mPhoneId, this.mContext);
        } catch (IndexOutOfBoundsException e) {
            Log.w("KeyguardSimPinView", "getOptrNameBySlot exception, mPhoneId=" + this.mPhoneId);
        }
        Log.i("KeyguardSimPinView", "dealwithSIMInfoChanged, mPhoneId=" + this.mPhoneId + ", operName=" + str);
        TextView textView = (TextView) findViewById(R$id.for_text);
        ImageView imageView = (ImageView) findViewById(R$id.sub_icon);
        TextView textView2 = (TextView) findViewById(R$id.sim_card_name);
        if (str == null) {
            Log.d("KeyguardSimPinView", "mPhoneId " + this.mPhoneId + " is new subInfo record");
            setForTextNewCard(this.mPhoneId, textView);
            imageView.setVisibility(8);
            textView2.setVisibility(8);
            return;
        }
        Log.d("KeyguardSimPinView", "dealwithSIMInfoChanged, show operName for mPhoneId=" + this.mPhoneId);
        textView.setText(this.mContext.getString(R$string.kg_slot_id, Integer.valueOf(this.mPhoneId + 1)) + " ");
        String str2 = str;
        if (str == null) {
            str2 = this.mContext.getString(R$string.kg_detecting_simcard);
        }
        textView2.setText(str2);
        imageView.setImageBitmap(this.mKeyguardUtils.getOptrBitmapUsingPhoneId(this.mPhoneId, this.mContext));
        imageView.setVisibility(0);
        textView2.setVisibility(0);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String getPinPasswordErrorMessage(int i) {
        String string = i == 0 ? getContext().getString(R$string.kg_password_wrong_pin_code_pukked) : i > 0 ? getContext().getResources().getQuantityString(R$plurals.kg_password_wrong_pin_code, i, Integer.valueOf(i)) : getContext().getString(R$string.kg_password_pin_failed);
        Log.d("KeyguardSimPinView", "getPinPasswordErrorMessage: attemptsRemaining=" + i + " displayMessage=" + string);
        return string;
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
            this.mSimUnlockProgressDialog.setMessage(this.mContext.getString(R$string.kg_sim_unlock_progress_dialog_message));
            this.mSimUnlockProgressDialog.setIndeterminate(true);
            this.mSimUnlockProgressDialog.setCancelable(false);
            this.mSimUnlockProgressDialog.getWindow().setType(2009);
        }
        return this.mSimUnlockProgressDialog;
    }

    private void setForTextNewCard(int i, TextView textView) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(this.mContext.getString(R$string.kg_slot_id, Integer.valueOf(i + 1)));
        stringBuffer.append(" ");
        stringBuffer.append(this.mContext.getText(R$string.kg_new_simcard));
        textView.setText(stringBuffer.toString());
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public int getPasswordTextViewId() {
        return R$id.simPinEntry;
    }

    @Override // com.android.keyguard.KeyguardPinBasedInputView, com.android.keyguard.KeyguardAbsKeyInputView
    protected int getPromtReasonStringRes(int i) {
        return 0;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        KeyguardUpdateMonitor.getInstance(this.mContext).registerCallback(this.mUpdateMonitorCallback);
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        resetState();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        KeyguardUpdateMonitor.getInstance(this.mContext).removeCallback(this.mUpdateMonitorCallback);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardPinBasedInputView, com.android.keyguard.KeyguardAbsKeyInputView, android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mPhoneId = KeyguardUpdateMonitor.getInstance(getContext()).getSimPinLockPhoneId();
        if (KeyguardUtils.getNumOfPhone() > 1) {
            View findViewById = findViewById(R$id.keyguard_sim);
            if (findViewById != null) {
                findViewById.setVisibility(8);
            }
            View findViewById2 = findViewById(R$id.sim_info_message);
            if (findViewById2 != null) {
                findViewById2.setVisibility(0);
            }
            dealwithSIMInfoChanged();
        }
        this.mSecurityMessageDisplay.setTimeout(0);
        if (this.mEcaView instanceof EmergencyCarrierArea) {
            ((EmergencyCarrierArea) this.mEcaView).setCarrierTextVisible(true);
        }
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView, com.android.keyguard.KeyguardSecurityView
    public void onPause() {
        if (this.mSimUnlockProgressDialog != null) {
            this.mSimUnlockProgressDialog.dismiss();
            this.mSimUnlockProgressDialog = null;
        }
    }

    @Override // com.android.keyguard.KeyguardPinBasedInputView, com.android.keyguard.KeyguardAbsKeyInputView
    public void resetState() {
        super.resetState();
        Log.v("KeyguardSimPinView", "Resetting state");
        this.mSecurityMessageDisplay.setMessage(R$string.kg_sim_pin_instructions, true);
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
        if (this.mPasswordEntry.getText().length() < 4) {
            this.mSecurityMessageDisplay.setMessage(R$string.kg_invalid_sim_pin_hint, true);
            resetPasswordText(true, true);
            this.mCallback.userActivity();
            return;
        }
        getSimUnlockProgressDialog().show();
        if (this.mCheckSimPinThread == null) {
            this.mCheckSimPinThread = new AnonymousClass2(this, this, this.mPasswordEntry.getText());
            this.mCheckSimPinThread.start();
        }
    }
}
