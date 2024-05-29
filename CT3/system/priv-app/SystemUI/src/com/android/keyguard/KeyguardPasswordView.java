package com.android.keyguard;

import android.content.Context;
import android.graphics.Rect;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.TextKeyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.TextView;
import com.android.internal.widget.TextViewInputDisabler;
import java.util.List;
/* loaded from: a.zip:com/android/keyguard/KeyguardPasswordView.class */
public class KeyguardPasswordView extends KeyguardAbsKeyInputView implements KeyguardSecurityView, TextView.OnEditorActionListener, TextWatcher {
    private final int mDisappearYTranslation;
    private Interpolator mFastOutLinearInInterpolator;
    InputMethodManager mImm;
    private Interpolator mLinearOutSlowInInterpolator;
    private TextView mPasswordEntry;
    private TextViewInputDisabler mPasswordEntryDisabler;
    private final boolean mShowImeAtScreenOn;

    public KeyguardPasswordView(Context context) {
        this(context, null);
    }

    public KeyguardPasswordView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mShowImeAtScreenOn = context.getResources().getBoolean(R$bool.kg_show_ime_at_screen_on);
        this.mDisappearYTranslation = getResources().getDimensionPixelSize(R$dimen.disappear_y_translation);
        this.mLinearOutSlowInInterpolator = AnimationUtils.loadInterpolator(context, 17563662);
        this.mFastOutLinearInInterpolator = AnimationUtils.loadInterpolator(context, 17563663);
    }

    private boolean hasMultipleEnabledIMEsOrSubtypes(InputMethodManager inputMethodManager, boolean z) {
        int i = 0;
        for (InputMethodInfo inputMethodInfo : inputMethodManager.getEnabledInputMethodList()) {
            if (i > 1) {
                return true;
            }
            List<InputMethodSubtype> enabledInputMethodSubtypeList = inputMethodManager.getEnabledInputMethodSubtypeList(inputMethodInfo, true);
            if (enabledInputMethodSubtypeList.isEmpty()) {
                i++;
            } else {
                int i2 = 0;
                for (InputMethodSubtype inputMethodSubtype : enabledInputMethodSubtypeList) {
                    if (inputMethodSubtype.isAuxiliary()) {
                        i2++;
                    }
                }
                if (enabledInputMethodSubtypeList.size() - i2 > 0 || (z && i2 > 1)) {
                    i++;
                }
            }
        }
        return i <= 1 ? inputMethodManager.getEnabledInputMethodSubtypeList(null, false).size() > 1 : true;
    }

    @Override // android.text.TextWatcher
    public void afterTextChanged(Editable editable) {
        if (TextUtils.isEmpty(editable)) {
            return;
        }
        onUserInput();
    }

    @Override // android.text.TextWatcher
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        if (this.mCallback != null) {
            this.mCallback.userActivity();
        }
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    protected String getPasswordText() {
        return this.mPasswordEntry.getText().toString();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public int getPasswordTextViewId() {
        return R$id.passwordEntry;
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    protected int getPromtReasonStringRes(int i) {
        switch (i) {
            case 0:
                return 0;
            case 1:
                return R$string.kg_prompt_reason_restart_password;
            case 2:
                return R$string.kg_prompt_reason_timeout_password;
            case 3:
                return R$string.kg_prompt_reason_device_admin;
            case 4:
                return R$string.kg_prompt_reason_user_request;
            default:
                return R$string.kg_prompt_reason_timeout_password;
        }
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public int getWrongPasswordStringId() {
        return R$string.kg_wrong_password;
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView, com.android.keyguard.KeyguardSecurityView
    public boolean needsInput() {
        Log.d("KeyguardPasswordView", "needsInput() - returns true.");
        return true;
    }

    @Override // android.widget.TextView.OnEditorActionListener
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        boolean z = keyEvent == null ? (i == 0 || i == 6) ? true : i == 5 : false;
        boolean z2 = (keyEvent == null || !KeyEvent.isConfirmKey(keyEvent.getKeyCode())) ? false : keyEvent.getAction() == 0;
        if (z || z2) {
            verifyPasswordAndUnlock();
            return true;
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView, android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mImm = (InputMethodManager) getContext().getSystemService("input_method");
        this.mPasswordEntry = (TextView) findViewById(getPasswordTextViewId());
        this.mPasswordEntryDisabler = new TextViewInputDisabler(this.mPasswordEntry);
        this.mPasswordEntry.setKeyListener(TextKeyListener.getInstance());
        this.mPasswordEntry.setInputType(129);
        this.mPasswordEntry.setOnEditorActionListener(this);
        this.mPasswordEntry.addTextChangedListener(this);
        this.mPasswordEntry.setOnClickListener(new View.OnClickListener(this) { // from class: com.android.keyguard.KeyguardPasswordView.2
            final KeyguardPasswordView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                this.this$0.mCallback.userActivity();
            }
        });
        this.mPasswordEntry.setSelected(true);
        this.mPasswordEntry.requestFocus();
        View findViewById = findViewById(R$id.switch_ime_button);
        boolean z = false;
        if (findViewById != null) {
            z = false;
            if (hasMultipleEnabledIMEsOrSubtypes(this.mImm, false)) {
                findViewById.setVisibility(0);
                z = true;
                findViewById.setOnClickListener(new View.OnClickListener(this) { // from class: com.android.keyguard.KeyguardPasswordView.3
                    final KeyguardPasswordView this$0;

                    {
                        this.this$0 = this;
                    }

                    @Override // android.view.View.OnClickListener
                    public void onClick(View view) {
                        this.this$0.mCallback.userActivity();
                        this.this$0.mImm.showInputMethodPicker(false);
                    }
                });
            }
        }
        if (z) {
            return;
        }
        ViewGroup.LayoutParams layoutParams = this.mPasswordEntry.getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) layoutParams).setMarginStart(0);
            this.mPasswordEntry.setLayoutParams(layoutParams);
        }
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView, com.android.keyguard.KeyguardSecurityView
    public void onPause() {
        super.onPause();
        this.mImm.hideSoftInputFromWindow(getWindowToken(), 0);
    }

    @Override // android.view.ViewGroup
    protected boolean onRequestFocusInDescendants(int i, Rect rect) {
        return this.mPasswordEntry.requestFocus(i, rect);
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView, com.android.keyguard.KeyguardSecurityView
    public void onResume(int i) {
        super.onResume(i);
        post(new Runnable(this, i) { // from class: com.android.keyguard.KeyguardPasswordView.1
            final KeyguardPasswordView this$0;
            final int val$reason;

            {
                this.this$0 = this;
                this.val$reason = i;
            }

            @Override // java.lang.Runnable
            public void run() {
                if (this.this$0.isShown() && this.this$0.mPasswordEntry.isEnabled()) {
                    this.this$0.mPasswordEntry.requestFocus();
                    Log.d("KeyguardPasswordView", "reason = " + this.val$reason + ", mShowImeAtScreenOn = " + this.this$0.mShowImeAtScreenOn);
                    if (this.val$reason != 1 || this.this$0.mShowImeAtScreenOn) {
                        Log.d("KeyguardPasswordView", "onResume() - call showSoftInput()");
                        this.this$0.mImm.showSoftInput(this.this$0.mPasswordEntry, 1);
                    }
                }
            }
        });
    }

    @Override // android.text.TextWatcher
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public void reset() {
        super.reset();
        this.mPasswordEntry.requestFocus();
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    protected void resetPasswordText(boolean z, boolean z2) {
        this.mPasswordEntry.setText("");
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    protected void resetState() {
        this.mSecurityMessageDisplay.setMessage(R$string.kg_password_instructions, true);
        boolean isEnabled = this.mPasswordEntry.isEnabled();
        setPasswordEntryEnabled(true);
        setPasswordEntryInputEnabled(true);
        if (isEnabled) {
            this.mImm.showSoftInput(this.mPasswordEntry, 1);
        }
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    protected void setPasswordEntryEnabled(boolean z) {
        this.mPasswordEntry.setEnabled(z);
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    protected void setPasswordEntryInputEnabled(boolean z) {
        this.mPasswordEntryDisabler.setInputEnabled(z);
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void startAppearAnimation() {
        setAlpha(0.0f);
        setTranslationY(0.0f);
        animate().alpha(1.0f).withLayer().setDuration(300L).setInterpolator(this.mLinearOutSlowInInterpolator);
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView, com.android.keyguard.KeyguardSecurityView
    public boolean startDisappearAnimation(Runnable runnable) {
        animate().alpha(0.0f).translationY(this.mDisappearYTranslation).setInterpolator(this.mFastOutLinearInInterpolator).setDuration(100L).withEndAction(runnable);
        return true;
    }
}
