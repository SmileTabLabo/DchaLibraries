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
/* loaded from: classes.dex */
public class KeyguardSimPukView extends KeyguardPinBasedInputView {
    private static final boolean DEBUG = KeyguardConstants.DEBUG;
    private CheckSimPuk mCheckSimPukThread;
    KeyguardUtils mKeyguardUtils;
    private int mPhoneId;
    private String mPinText;
    private String mPukText;
    private int mRemainingAttempts;
    private AlertDialog mRemainingAttemptsDialog;
    private boolean mShowDefaultMessage;
    private ProgressDialog mSimUnlockProgressDialog;
    private StateMachine mStateMachine;
    private int mSubId;
    KeyguardUpdateMonitorCallback mUpdateMonitorCallback;

    /* renamed from: com.android.keyguard.KeyguardSimPukView$3  reason: invalid class name */
    /* loaded from: classes.dex */
    static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$IccCardConstants$State = new int[IccCardConstants.State.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.ABSENT.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.READY.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    public KeyguardSimPukView(Context context) {
        this(context, null);
    }

    public KeyguardSimPukView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mSimUnlockProgressDialog = null;
        this.mShowDefaultMessage = true;
        this.mRemainingAttempts = -1;
        this.mStateMachine = new StateMachine();
        this.mSubId = -1;
        this.mPhoneId = 0;
        this.mUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() { // from class: com.android.keyguard.KeyguardSimPukView.1
            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onSimStateChangedUsingPhoneId(int i, IccCardConstants.State state) {
                if (KeyguardSimPukView.DEBUG) {
                    Log.d("KeyguardSimPukView", "onSimStateChangedUsingPhoneId: " + state + ", phoneId=" + i);
                }
                switch (AnonymousClass3.$SwitchMap$com$android$internal$telephony$IccCardConstants$State[state.ordinal()]) {
                    case 1:
                    case 2:
                        if (i == KeyguardSimPukView.this.mPhoneId) {
                            KeyguardUpdateMonitor.getInstance(KeyguardSimPukView.this.getContext()).reportSimUnlocked(KeyguardSimPukView.this.mPhoneId);
                            if (KeyguardSimPukView.this.mCallback != null) {
                                KeyguardSimPukView.this.mCallback.dismiss(true, KeyguardUpdateMonitor.getCurrentUser());
                                return;
                            }
                            return;
                        }
                        return;
                    default:
                        KeyguardSimPukView.this.resetState();
                        return;
                }
            }
        };
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class StateMachine {
        final int CONFIRM_PIN;
        final int DONE;
        final int ENTER_PIN;
        final int ENTER_PUK;
        private int state;

        private StateMachine() {
            this.ENTER_PUK = 0;
            this.ENTER_PIN = 1;
            this.CONFIRM_PIN = 2;
            this.DONE = 3;
            this.state = 0;
        }

        public void next() {
            int i;
            if (this.state == 0) {
                if (KeyguardSimPukView.this.checkPuk()) {
                    this.state = 1;
                    i = com.android.systemui.R.string.kg_puk_enter_pin_hint;
                } else {
                    i = com.android.systemui.R.string.kg_invalid_sim_puk_hint;
                }
            } else if (this.state == 1) {
                if (KeyguardSimPukView.this.checkPin()) {
                    this.state = 2;
                    i = com.android.systemui.R.string.kg_enter_confirm_pin_hint;
                } else {
                    i = com.android.systemui.R.string.kg_invalid_sim_pin_hint;
                }
            } else if (this.state == 2) {
                if (KeyguardSimPukView.this.confirmPin()) {
                    this.state = 3;
                    i = com.android.systemui.R.string.keyguard_sim_unlock_progress_dialog_message;
                    KeyguardSimPukView.this.updateSim();
                } else {
                    this.state = 1;
                    i = com.android.systemui.R.string.kg_invalid_confirm_pin_hint;
                }
            } else {
                i = 0;
            }
            KeyguardSimPukView.this.resetPasswordText(true, true);
            if (i != 0) {
                KeyguardSimPukView.this.mSecurityMessageDisplay.setMessage(i);
            }
        }

        void reset() {
            KeyguardSimPukView.this.mPinText = "";
            KeyguardSimPukView.this.mPukText = "";
            this.state = 0;
            KeyguardSimPukView.this.mSecurityMessageDisplay.setMessage(com.android.systemui.R.string.kg_puk_enter_puk_hint);
            KeyguardSimPukView.this.mPasswordEntry.requestFocus();
        }
    }

    @Override // com.android.keyguard.KeyguardPinBasedInputView, com.android.keyguard.KeyguardAbsKeyInputView
    protected int getPromptReasonStringRes(int i) {
        return 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String getPukPasswordErrorMessage(int i, boolean z) {
        String string;
        if (i == 0) {
            string = getContext().getString(com.android.systemui.R.string.kg_password_wrong_puk_code_dead);
        } else if (i > 0) {
            string = getContext().getResources().getQuantityString(z ? com.android.systemui.R.plurals.kg_password_default_puk_message : com.android.systemui.R.plurals.kg_password_wrong_puk_code, i, Integer.valueOf(i));
        } else {
            string = getContext().getString(z ? com.android.systemui.R.string.kg_puk_enter_puk_hint : com.android.systemui.R.string.kg_password_puk_failed);
        }
        if (KeyguardEsimArea.isEsimLocked(this.mContext, this.mSubId)) {
            string = getResources().getString(com.android.systemui.R.string.kg_sim_lock_esim_instructions, string);
        }
        if (DEBUG) {
            Log.d("KeyguardSimPukView", "getPukPasswordErrorMessage: attemptsRemaining=" + i + " displayMessage=" + string);
        }
        return string;
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

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public int getPasswordTextViewId() {
        return com.android.systemui.R.id.pukEntry;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardPinBasedInputView, com.android.keyguard.KeyguardAbsKeyInputView, android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mPhoneId = KeyguardUpdateMonitor.getInstance(getContext()).getSimPukLockPhoneId();
        if (KeyguardUtils.getNumOfPhone() > 1) {
            View findViewById = findViewById(com.android.systemui.R.id.keyguard_sim);
            if (findViewById != null) {
                findViewById.setVisibility(8);
            }
            View findViewById2 = findViewById(com.android.systemui.R.id.sim_info_message);
            if (findViewById2 != null) {
                findViewById2.setVisibility(0);
            }
            dealwithSIMInfoChanged();
        }
        if (this.mEcaView instanceof EmergencyCarrierArea) {
            ((EmergencyCarrierArea) this.mEcaView).setCarrierTextVisible(true);
        }
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

    @Override // com.android.keyguard.KeyguardAbsKeyInputView, com.android.keyguard.KeyguardSecurityView
    public void onPause() {
        if (this.mSimUnlockProgressDialog != null) {
            this.mSimUnlockProgressDialog.dismiss();
            this.mSimUnlockProgressDialog = null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public abstract class CheckSimPuk extends Thread {
        private final String mPin;
        private final String mPuk;

        abstract void onSimLockChangedResponse(int i, int i2);

        protected CheckSimPuk(String str, String str2) {
            this.mPuk = str;
            this.mPin = str2;
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            try {
                Log.v("KeyguardSimPukView", "call supplyPukReportResultForSubscriber() mPhoneId = " + KeyguardSimPukView.this.mPhoneId);
                final int[] supplyPukReportResultForSubscriber = ITelephony.Stub.asInterface(ServiceManager.checkService("phone")).supplyPukReportResultForSubscriber(KeyguardUtils.getSubIdUsingPhoneId(KeyguardSimPukView.this.mPhoneId), this.mPuk, this.mPin);
                Log.v("KeyguardSimPukView", "supplyPukReportResultForSubscriber returned: " + supplyPukReportResultForSubscriber[0] + " " + supplyPukReportResultForSubscriber[1]);
                KeyguardSimPukView.this.post(new Runnable() { // from class: com.android.keyguard.KeyguardSimPukView.CheckSimPuk.1
                    @Override // java.lang.Runnable
                    public void run() {
                        CheckSimPuk.this.onSimLockChangedResponse(supplyPukReportResultForSubscriber[0], supplyPukReportResultForSubscriber[1]);
                    }
                });
            } catch (RemoteException e) {
                Log.e("KeyguardSimPukView", "RemoteException for supplyPukReportResult:", e);
                KeyguardSimPukView.this.post(new Runnable() { // from class: com.android.keyguard.KeyguardSimPukView.CheckSimPuk.2
                    @Override // java.lang.Runnable
                    public void run() {
                        CheckSimPuk.this.onSimLockChangedResponse(2, -1);
                    }
                });
            }
        }
    }

    private Dialog getSimUnlockProgressDialog() {
        if (this.mSimUnlockProgressDialog == null) {
            this.mSimUnlockProgressDialog = new ProgressDialog(this.mContext);
            this.mSimUnlockProgressDialog.setMessage(this.mContext.getString(com.android.systemui.R.string.kg_sim_unlock_progress_dialog_message));
            this.mSimUnlockProgressDialog.setIndeterminate(true);
            this.mSimUnlockProgressDialog.setCancelable(false);
            if (!(this.mContext instanceof Activity)) {
                this.mSimUnlockProgressDialog.getWindow().setType(2009);
            }
        }
        return this.mSimUnlockProgressDialog;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Dialog getPukRemainingAttemptsDialog(int i) {
        String pukPasswordErrorMessage = getPukPasswordErrorMessage(i, false);
        if (this.mRemainingAttemptsDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
            builder.setMessage(pukPasswordErrorMessage);
            builder.setCancelable(false);
            builder.setNeutralButton(com.android.systemui.R.string.ok, (DialogInterface.OnClickListener) null);
            this.mRemainingAttemptsDialog = builder.create();
            this.mRemainingAttemptsDialog.getWindow().setType(2009);
        } else {
            this.mRemainingAttemptsDialog.setMessage(pukPasswordErrorMessage);
        }
        return this.mRemainingAttemptsDialog;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean checkPuk() {
        if (this.mPasswordEntry.getText().length() == 8) {
            this.mPukText = this.mPasswordEntry.getText();
            return true;
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean checkPin() {
        int length = this.mPasswordEntry.getText().length();
        if (length >= 4 && length <= 8) {
            this.mPinText = this.mPasswordEntry.getText();
            return true;
        }
        return false;
    }

    public boolean confirmPin() {
        return this.mPinText.equals(this.mPasswordEntry.getText());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateSim() {
        getSimUnlockProgressDialog().show();
        if (this.mCheckSimPukThread == null) {
            this.mCheckSimPukThread = new CheckSimPuk(this.mPukText, this.mPinText) { // from class: com.android.keyguard.KeyguardSimPukView.2
                @Override // com.android.keyguard.KeyguardSimPukView.CheckSimPuk
                void onSimLockChangedResponse(final int i, final int i2) {
                    KeyguardSimPukView.this.post(new Runnable() { // from class: com.android.keyguard.KeyguardSimPukView.2.1
                        @Override // java.lang.Runnable
                        public void run() {
                            if (KeyguardSimPukView.this.mSimUnlockProgressDialog != null) {
                                KeyguardSimPukView.this.mSimUnlockProgressDialog.hide();
                            }
                            if (i == 0) {
                                KeyguardUpdateMonitor.getInstance(KeyguardSimPukView.this.getContext()).reportSimUnlocked(KeyguardSimPukView.this.mPhoneId);
                                KeyguardSimPukView.this.mRemainingAttempts = -1;
                                KeyguardSimPukView.this.mShowDefaultMessage = true;
                                if (KeyguardSimPukView.this.mCallback != null) {
                                    KeyguardSimPukView.this.mCallback.dismiss(true, KeyguardUpdateMonitor.getCurrentUser());
                                }
                            } else {
                                KeyguardSimPukView.this.mShowDefaultMessage = false;
                                if (i == 1) {
                                    KeyguardSimPukView.this.mSecurityMessageDisplay.setMessage(KeyguardSimPukView.this.getPukPasswordErrorMessage(i2, false));
                                    if (i2 <= 2) {
                                        KeyguardSimPukView.this.getPukRemainingAttemptsDialog(i2).show();
                                    } else {
                                        KeyguardSimPukView.this.mSecurityMessageDisplay.setMessage(KeyguardSimPukView.this.getPukPasswordErrorMessage(i2, false));
                                    }
                                } else {
                                    KeyguardSimPukView.this.mSecurityMessageDisplay.setMessage(KeyguardSimPukView.this.getContext().getString(com.android.systemui.R.string.kg_password_puk_failed));
                                }
                                if (KeyguardSimPukView.DEBUG) {
                                    Log.d("KeyguardSimPukView", "verifyPasswordAndUnlock  UpdateSim.onSimCheckResponse:  attemptsRemaining=" + i2);
                                }
                                KeyguardSimPukView.this.mStateMachine.reset();
                            }
                            KeyguardSimPukView.this.mCheckSimPukThread = null;
                        }
                    });
                }
            };
            this.mCheckSimPukThread.start();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public void verifyPasswordAndUnlock() {
        this.mStateMachine.next();
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void startAppearAnimation() {
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView, com.android.keyguard.KeyguardSecurityView
    public boolean startDisappearAnimation(Runnable runnable) {
        return false;
    }

    @Override // com.android.keyguard.KeyguardPinBasedInputView, com.android.keyguard.KeyguardSecurityView
    public CharSequence getTitle() {
        return getContext().getString(17040062);
    }

    private void dealwithSIMInfoChanged() {
        String str;
        try {
            str = this.mKeyguardUtils.getOptrNameUsingPhoneId(this.mPhoneId, this.mContext);
        } catch (IndexOutOfBoundsException e) {
            Log.w("KeyguardSimPukView", "getOptrNameBySlot exception, mPhoneId=" + this.mPhoneId);
            str = null;
        }
        if (DEBUG) {
            Log.i("KeyguardSimPukView", "dealwithSIMInfoChanged, mPhoneId=" + this.mPhoneId + ", operName=" + str);
        }
        TextView textView = (TextView) findViewById(com.android.systemui.R.id.for_text);
        ImageView imageView = (ImageView) findViewById(com.android.systemui.R.id.sub_icon);
        TextView textView2 = (TextView) findViewById(com.android.systemui.R.id.sim_card_name);
        if (str == null) {
            if (DEBUG) {
                Log.d("KeyguardSimPukView", "mPhoneId " + this.mPhoneId + " is new subInfo record");
            }
            setForTextNewCard(this.mPhoneId, textView);
            imageView.setVisibility(8);
            textView2.setVisibility(8);
            return;
        }
        if (DEBUG) {
            Log.d("KeyguardSimPukView", "dealwithSIMInfoChanged, show operName for mPhoneId=" + this.mPhoneId);
        }
        textView.setText(this.mContext.getString(com.android.systemui.R.string.kg_slot_id, Integer.valueOf(this.mPhoneId + 1)) + " ");
        if (str == null) {
            str = this.mContext.getString(com.android.systemui.R.string.kg_detecting_simcard);
        }
        textView2.setText(str);
        imageView.setImageBitmap(this.mKeyguardUtils.getOptrBitmapUsingPhoneId(this.mPhoneId, this.mContext));
        imageView.setVisibility(0);
        textView2.setVisibility(0);
    }

    private void setForTextNewCard(int i, TextView textView) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(this.mContext.getString(com.android.systemui.R.string.kg_slot_id, Integer.valueOf(i + 1)));
        stringBuffer.append(" ");
        stringBuffer.append(this.mContext.getText(com.android.systemui.R.string.kg_new_simcard));
        textView.setText(stringBuffer.toString());
    }
}
