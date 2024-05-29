package com.android.keyguard;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import com.android.keyguard.PasswordTextView;
/* loaded from: a.zip:com/android/keyguard/KeyguardPinBasedInputView.class */
public abstract class KeyguardPinBasedInputView extends KeyguardAbsKeyInputView implements View.OnKeyListener {
    private View mButton0;
    private View mButton1;
    private View mButton2;
    private View mButton3;
    private View mButton4;
    private View mButton5;
    private View mButton6;
    private View mButton7;
    private View mButton8;
    private View mButton9;
    private View mDeleteButton;
    private View mOkButton;
    protected PasswordTextView mPasswordEntry;

    public KeyguardPinBasedInputView(Context context) {
        this(context, null);
    }

    public KeyguardPinBasedInputView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    private void performClick(View view) {
        view.performClick();
    }

    private void performNumberClick(int i) {
        switch (i) {
            case 0:
                performClick(this.mButton0);
                return;
            case 1:
                performClick(this.mButton1);
                return;
            case 2:
                performClick(this.mButton2);
                return;
            case 3:
                performClick(this.mButton3);
                return;
            case 4:
                performClick(this.mButton4);
                return;
            case 5:
                performClick(this.mButton5);
                return;
            case 6:
                performClick(this.mButton6);
                return;
            case 7:
                performClick(this.mButton7);
                return;
            case 8:
                performClick(this.mButton8);
                return;
            case 9:
                performClick(this.mButton9);
                return;
            default:
                return;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public String getPasswordText() {
        return this.mPasswordEntry.getText();
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    protected int getPromtReasonStringRes(int i) {
        switch (i) {
            case 0:
                return 0;
            case 1:
                return R$string.kg_prompt_reason_restart_pin;
            case 2:
                return R$string.kg_prompt_reason_timeout_pin;
            case 3:
                return R$string.kg_prompt_reason_device_admin;
            case 4:
                return R$string.kg_prompt_reason_user_request;
            default:
                return R$string.kg_prompt_reason_timeout_pin;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView, android.view.View
    public void onFinishInflate() {
        this.mPasswordEntry = (PasswordTextView) findViewById(getPasswordTextViewId());
        this.mPasswordEntry.setOnKeyListener(this);
        this.mPasswordEntry.setSelected(true);
        this.mPasswordEntry.setUserActivityListener(new PasswordTextView.UserActivityListener(this) { // from class: com.android.keyguard.KeyguardPinBasedInputView.1
            final KeyguardPinBasedInputView this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.keyguard.PasswordTextView.UserActivityListener
            public void onUserActivity() {
                this.this$0.onUserInput();
            }
        });
        this.mOkButton = findViewById(R$id.key_enter);
        if (this.mOkButton != null) {
            this.mOkButton.setOnClickListener(new View.OnClickListener(this) { // from class: com.android.keyguard.KeyguardPinBasedInputView.2
                final KeyguardPinBasedInputView this$0;

                {
                    this.this$0 = this;
                }

                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    this.this$0.doHapticKeyClick();
                    if (this.this$0.mPasswordEntry.isEnabled()) {
                        this.this$0.verifyPasswordAndUnlock();
                    }
                }
            });
            this.mOkButton.setOnHoverListener(new LiftToActivateListener(getContext()));
        }
        this.mDeleteButton = findViewById(R$id.delete_button);
        this.mDeleteButton.setVisibility(0);
        this.mDeleteButton.setOnClickListener(new View.OnClickListener(this) { // from class: com.android.keyguard.KeyguardPinBasedInputView.3
            final KeyguardPinBasedInputView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (this.this$0.mPasswordEntry.isEnabled()) {
                    this.this$0.mPasswordEntry.deleteLastChar();
                }
                this.this$0.doHapticKeyClick();
            }
        });
        this.mDeleteButton.setOnLongClickListener(new View.OnLongClickListener(this) { // from class: com.android.keyguard.KeyguardPinBasedInputView.4
            final KeyguardPinBasedInputView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnLongClickListener
            public boolean onLongClick(View view) {
                if (this.this$0.mPasswordEntry.isEnabled()) {
                    this.this$0.resetPasswordText(true, true);
                }
                this.this$0.doHapticKeyClick();
                return true;
            }
        });
        this.mButton0 = findViewById(R$id.key0);
        this.mButton1 = findViewById(R$id.key1);
        this.mButton2 = findViewById(R$id.key2);
        this.mButton3 = findViewById(R$id.key3);
        this.mButton4 = findViewById(R$id.key4);
        this.mButton5 = findViewById(R$id.key5);
        this.mButton6 = findViewById(R$id.key6);
        this.mButton7 = findViewById(R$id.key7);
        this.mButton8 = findViewById(R$id.key8);
        this.mButton9 = findViewById(R$id.key9);
        this.mPasswordEntry.requestFocus();
        super.onFinishInflate();
    }

    @Override // android.view.View.OnKeyListener
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        if (keyEvent.getAction() == 0) {
            Log.d("KeyguardPinBasedInputView", "keyCode: " + i + " event: " + keyEvent);
            return onKeyDown(i, keyEvent);
        }
        return false;
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView, android.view.View, android.view.KeyEvent.Callback
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (KeyEvent.isConfirmKey(i)) {
            performClick(this.mOkButton);
            return true;
        } else if (i == 67) {
            performClick(this.mDeleteButton);
            return true;
        } else if (i >= 7 && i <= 16) {
            performNumberClick(i - 7);
            return true;
        } else if (i < 144 || i > 153) {
            return super.onKeyDown(i, keyEvent);
        } else {
            performNumberClick(i - 144);
            return true;
        }
    }

    @Override // android.view.ViewGroup
    protected boolean onRequestFocusInDescendants(int i, Rect rect) {
        return this.mPasswordEntry.requestFocus(i, rect);
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public void reset() {
        this.mPasswordEntry.requestFocus();
        super.reset();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public void resetPasswordText(boolean z, boolean z2) {
        this.mPasswordEntry.reset(z, z2);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public void resetState() {
        setPasswordEntryEnabled(true);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public void setPasswordEntryEnabled(boolean z) {
        this.mPasswordEntry.setEnabled(z);
        this.mOkButton.setEnabled(z);
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    protected void setPasswordEntryInputEnabled(boolean z) {
        this.mPasswordEntry.setEnabled(z);
        this.mOkButton.setEnabled(z);
    }
}
