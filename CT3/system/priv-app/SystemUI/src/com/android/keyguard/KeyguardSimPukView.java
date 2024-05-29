package com.android.keyguard;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.IccCardConstants;
/* loaded from: a.zip:com/android/keyguard/KeyguardSimPukView.class */
public class KeyguardSimPukView extends KeyguardPinBasedInputView {
    private CheckSimPuk mCheckSimPukThread;
    KeyguardUtils mKeyguardUtils;
    private int mPhoneId;
    private String mPinText;
    private String mPukText;
    private AlertDialog mRemainingAttemptsDialog;
    private ProgressDialog mSimUnlockProgressDialog;
    private StateMachine mStateMachine;
    KeyguardUpdateMonitorCallback mUpdateMonitorCallback;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.android.keyguard.KeyguardSimPukView$2  reason: invalid class name */
    /* loaded from: a.zip:com/android/keyguard/KeyguardSimPukView$2.class */
    public class AnonymousClass2 extends CheckSimPuk {
        final KeyguardSimPukView this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        AnonymousClass2(KeyguardSimPukView keyguardSimPukView, KeyguardSimPukView keyguardSimPukView2, String str, String str2) {
            super(keyguardSimPukView, str, str2);
            this.this$0 = keyguardSimPukView2;
        }

        @Override // com.android.keyguard.KeyguardSimPukView.CheckSimPuk
        void onSimLockChangedResponse(int i, int i2) {
            this.this$0.post(new Runnable(this, i, i2) { // from class: com.android.keyguard.KeyguardSimPukView.2.1
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
                    if (this.this$1.this$0.mSimUnlockProgressDialog != null) {
                        this.this$1.this$0.mSimUnlockProgressDialog.hide();
                    }
                    if (this.val$result == 0) {
                        KeyguardUpdateMonitor.getInstance(this.this$1.this$0.getContext()).reportSimUnlocked(this.this$1.this$0.mPhoneId);
                        this.this$1.this$0.mCallback.dismiss(true);
                    } else {
                        if (this.val$result != 1) {
                            this.this$1.this$0.mSecurityMessageDisplay.setMessage((CharSequence) this.this$1.this$0.getContext().getString(R$string.kg_password_puk_failed), true);
                        } else if (this.val$attemptsRemaining <= 2) {
                            this.this$1.this$0.getPukRemainingAttemptsDialog(this.val$attemptsRemaining).show();
                        } else {
                            this.this$1.this$0.mSecurityMessageDisplay.setMessage((CharSequence) this.this$1.this$0.getPukPasswordErrorMessage(this.val$attemptsRemaining), true);
                        }
                        Log.d("KeyguardSimPukView", "verifyPasswordAndUnlock  UpdateSim.onSimCheckResponse:  attemptsRemaining=" + this.val$attemptsRemaining);
                        this.this$1.this$0.mStateMachine.reset();
                    }
                    this.this$1.this$0.mCheckSimPukThread = null;
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/keyguard/KeyguardSimPukView$CheckSimPuk.class */
    public abstract class CheckSimPuk extends Thread {
        private final String mPin;
        private final String mPuk;
        final KeyguardSimPukView this$0;

        protected CheckSimPuk(KeyguardSimPukView keyguardSimPukView, String str, String str2) {
            this.this$0 = keyguardSimPukView;
            this.mPuk = str;
            this.mPin = str2;
        }

        abstract void onSimLockChangedResponse(int i, int i2);

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            try {
                Log.v("KeyguardSimPukView", "call supplyPukReportResultForSubscriber() mPhoneId = " + this.this$0.mPhoneId);
                int[] supplyPukReportResultForSubscriber = ITelephony.Stub.asInterface(ServiceManager.checkService("phone")).supplyPukReportResultForSubscriber(KeyguardUtils.getSubIdUsingPhoneId(this.this$0.mPhoneId), this.mPuk, this.mPin);
                Log.v("KeyguardSimPukView", "supplyPukReportResultForSubscriber returned: " + supplyPukReportResultForSubscriber[0] + " " + supplyPukReportResultForSubscriber[1]);
                this.this$0.post(new Runnable(this, supplyPukReportResultForSubscriber) { // from class: com.android.keyguard.KeyguardSimPukView.CheckSimPuk.1
                    final CheckSimPuk this$1;
                    final int[] val$result;

                    {
                        this.this$1 = this;
                        this.val$result = supplyPukReportResultForSubscriber;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.this$1.onSimLockChangedResponse(this.val$result[0], this.val$result[1]);
                    }
                });
            } catch (RemoteException e) {
                Log.e("KeyguardSimPukView", "RemoteException for supplyPukReportResult:", e);
                this.this$0.post(new Runnable(this) { // from class: com.android.keyguard.KeyguardSimPukView.CheckSimPuk.2
                    final CheckSimPuk this$1;

                    {
                        this.this$1 = this;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.this$1.onSimLockChangedResponse(2, -1);
                    }
                });
            }
        }
    }

    /* loaded from: a.zip:com/android/keyguard/KeyguardSimPukView$StateMachine.class */
    private class StateMachine {
        final int CONFIRM_PIN;
        final int DONE;
        final int ENTER_PIN;
        final int ENTER_PUK;
        private int state;
        final KeyguardSimPukView this$0;

        private StateMachine(KeyguardSimPukView keyguardSimPukView) {
            this.this$0 = keyguardSimPukView;
            this.ENTER_PUK = 0;
            this.ENTER_PIN = 1;
            this.CONFIRM_PIN = 2;
            this.DONE = 3;
            this.state = 0;
        }

        /* synthetic */ StateMachine(KeyguardSimPukView keyguardSimPukView, StateMachine stateMachine) {
            this(keyguardSimPukView);
        }

        public void next() {
            int i = 0;
            if (this.state == 0) {
                if (this.this$0.checkPuk()) {
                    this.state = 1;
                    i = R$string.kg_puk_enter_pin_hint;
                } else {
                    i = R$string.kg_invalid_sim_puk_hint;
                }
            } else if (this.state == 1) {
                if (this.this$0.checkPin()) {
                    this.state = 2;
                    i = R$string.kg_enter_confirm_pin_hint;
                } else {
                    i = R$string.kg_invalid_sim_pin_hint;
                }
            } else if (this.state == 2) {
                if (this.this$0.confirmPin()) {
                    this.state = 3;
                    i = R$string.keyguard_sim_unlock_progress_dialog_message;
                    this.this$0.updateSim();
                } else {
                    this.state = 1;
                    i = R$string.kg_invalid_confirm_pin_hint;
                }
            }
            this.this$0.resetPasswordText(true, true);
            if (i != 0) {
                this.this$0.mSecurityMessageDisplay.setMessage(i, true);
            }
        }

        void reset() {
            this.this$0.mPinText = "";
            this.this$0.mPukText = "";
            this.state = 0;
            this.this$0.mSecurityMessageDisplay.setMessage(R$string.kg_puk_enter_puk_hint, true);
            this.this$0.mPasswordEntry.requestFocus();
        }
    }

    public KeyguardSimPukView(Context context) {
        this(context, null);
    }

    public KeyguardSimPukView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mSimUnlockProgressDialog = null;
        this.mStateMachine = new StateMachine(this, null);
        this.mPhoneId = 0;
        this.mUpdateMonitorCallback = new KeyguardUpdateMonitorCallback(this) { // from class: com.android.keyguard.KeyguardSimPukView.1

            /* renamed from: -com-android-internal-telephony-IccCardConstants$StateSwitchesValues  reason: not valid java name */
            private static final int[] f6x8dbfd0b5 = null;
            final KeyguardSimPukView this$0;

            /* renamed from: -getcom-android-internal-telephony-IccCardConstants$StateSwitchesValues  reason: not valid java name */
            private static /* synthetic */ int[] m635xf663cf59() {
                if (f6x8dbfd0b5 != null) {
                    return f6x8dbfd0b5;
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
                f6x8dbfd0b5 = iArr;
                return iArr;
            }

            {
                this.this$0 = this;
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onSimStateChangedUsingPhoneId(int i, IccCardConstants.State state) {
                Log.d("KeyguardSimPukView", "onSimStateChangedUsingPhoneId: " + state + ", phoneId=" + i);
                switch (m635xf663cf59()[state.ordinal()]) {
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

    /* JADX INFO: Access modifiers changed from: private */
    public boolean checkPin() {
        int length = this.mPasswordEntry.getText().length();
        if (length < 4 || length > 8) {
            return false;
        }
        this.mPinText = this.mPasswordEntry.getText();
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean checkPuk() {
        if (this.mPasswordEntry.getText().length() == 8) {
            this.mPukText = this.mPasswordEntry.getText();
            return true;
        }
        return false;
    }

    private void dealwithSIMInfoChanged() {
        String str = null;
        try {
            str = this.mKeyguardUtils.getOptrNameUsingPhoneId(this.mPhoneId, this.mContext);
        } catch (IndexOutOfBoundsException e) {
            Log.w("KeyguardSimPukView", "getOptrNameBySlot exception, mPhoneId=" + this.mPhoneId);
        }
        Log.i("KeyguardSimPukView", "dealwithSIMInfoChanged, mPhoneId=" + this.mPhoneId + ", operName=" + str);
        TextView textView = (TextView) findViewById(R$id.for_text);
        ImageView imageView = (ImageView) findViewById(R$id.sub_icon);
        TextView textView2 = (TextView) findViewById(R$id.sim_card_name);
        if (str == null) {
            Log.d("KeyguardSimPukView", "mPhoneId " + this.mPhoneId + " is new subInfo record");
            setForTextNewCard(this.mPhoneId, textView);
            imageView.setVisibility(8);
            textView2.setVisibility(8);
            return;
        }
        Log.d("KeyguardSimPukView", "dealwithSIMInfoChanged, show operName for mPhoneId=" + this.mPhoneId);
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
    public String getPukPasswordErrorMessage(int i) {
        String string = i == 0 ? getContext().getString(R$string.kg_password_wrong_puk_code_dead) : i > 0 ? getContext().getResources().getQuantityString(R$plurals.kg_password_wrong_puk_code, i, Integer.valueOf(i)) : getContext().getString(R$string.kg_password_puk_failed);
        Log.d("KeyguardSimPukView", "getPukPasswordErrorMessage: attemptsRemaining=" + i + " displayMessage=" + string);
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

    private Dialog getSimUnlockProgressDialog() {
        if (this.mSimUnlockProgressDialog == null) {
            this.mSimUnlockProgressDialog = new ProgressDialog(this.mContext);
            this.mSimUnlockProgressDialog.setMessage(this.mContext.getString(R$string.kg_sim_unlock_progress_dialog_message));
            this.mSimUnlockProgressDialog.setIndeterminate(true);
            this.mSimUnlockProgressDialog.setCancelable(false);
            if (!(this.mContext instanceof Activity)) {
                this.mSimUnlockProgressDialog.getWindow().setType(2009);
            }
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

    /* JADX INFO: Access modifiers changed from: private */
    public void updateSim() {
        getSimUnlockProgressDialog().show();
        if (this.mCheckSimPukThread == null) {
            this.mCheckSimPukThread = new AnonymousClass2(this, this, this.mPukText, this.mPinText);
            this.mCheckSimPukThread.start();
        }
    }

    public boolean confirmPin() {
        return this.mPinText.equals(this.mPasswordEntry.getText());
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public int getPasswordTextViewId() {
        return R$id.pukEntry;
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

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        KeyguardUpdateMonitor.getInstance(this.mContext).removeCallback(this.mUpdateMonitorCallback);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardPinBasedInputView, com.android.keyguard.KeyguardAbsKeyInputView, android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mPhoneId = KeyguardUpdateMonitor.getInstance(getContext()).getSimPukLockPhoneId();
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
        this.mStateMachine.reset();
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
        this.mStateMachine.next();
    }
}
