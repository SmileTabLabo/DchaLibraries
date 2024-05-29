package com.android.keyguard;

import android.content.Context;
import android.graphics.Rect;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.TextKeyListener;
import android.util.AttributeSet;
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
/* loaded from: classes.dex */
public class KeyguardPasswordView extends KeyguardAbsKeyInputView implements TextWatcher, TextView.OnEditorActionListener, KeyguardSecurityView {
    private final int mDisappearYTranslation;
    private Interpolator mFastOutLinearInInterpolator;
    InputMethodManager mImm;
    private Interpolator mLinearOutSlowInInterpolator;
    private TextView mPasswordEntry;
    private TextViewInputDisabler mPasswordEntryDisabler;
    private final boolean mShowImeAtScreenOn;
    private View mSwitchImeButton;

    public KeyguardPasswordView(Context context) {
        this(context, null);
    }

    public KeyguardPasswordView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mShowImeAtScreenOn = context.getResources().getBoolean(com.android.systemui.R.bool.kg_show_ime_at_screen_on);
        this.mDisappearYTranslation = getResources().getDimensionPixelSize(com.android.systemui.R.dimen.disappear_y_translation);
        this.mLinearOutSlowInInterpolator = AnimationUtils.loadInterpolator(context, 17563662);
        this.mFastOutLinearInInterpolator = AnimationUtils.loadInterpolator(context, 17563663);
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    protected void resetState() {
        this.mPasswordEntry.setRestrictedAcrossUser(true);
        this.mSecurityMessageDisplay.setMessage("");
        boolean isEnabled = this.mPasswordEntry.isEnabled();
        setPasswordEntryEnabled(true);
        setPasswordEntryInputEnabled(true);
        if (isEnabled) {
            this.mImm.showSoftInput(this.mPasswordEntry, 1);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public int getPasswordTextViewId() {
        return com.android.systemui.R.id.passwordEntry;
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView, com.android.keyguard.KeyguardSecurityView
    public boolean needsInput() {
        return true;
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView, com.android.keyguard.KeyguardSecurityView
    public void onResume(final int i) {
        super.onResume(i);
        post(new Runnable() { // from class: com.android.keyguard.KeyguardPasswordView.1
            @Override // java.lang.Runnable
            public void run() {
                if (KeyguardPasswordView.this.isShown() && KeyguardPasswordView.this.mPasswordEntry.isEnabled()) {
                    KeyguardPasswordView.this.mPasswordEntry.requestFocus();
                    if (i != 1 || KeyguardPasswordView.this.mShowImeAtScreenOn) {
                        KeyguardPasswordView.this.mImm.showSoftInput(KeyguardPasswordView.this.mPasswordEntry, 1);
                    }
                }
            }
        });
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    protected int getPromptReasonStringRes(int i) {
        switch (i) {
            case 0:
                return 0;
            case 1:
                return com.android.systemui.R.string.kg_prompt_reason_restart_password;
            case 2:
                return com.android.systemui.R.string.kg_prompt_reason_timeout_password;
            case 3:
                return com.android.systemui.R.string.kg_prompt_reason_device_admin;
            case 4:
                return com.android.systemui.R.string.kg_prompt_reason_user_request;
            default:
                return com.android.systemui.R.string.kg_prompt_reason_timeout_password;
        }
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView, com.android.keyguard.KeyguardSecurityView
    public void onPause() {
        super.onPause();
        this.mImm.hideSoftInputFromWindow(getWindowToken(), 0);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateSwitchImeButton() {
        boolean z = this.mSwitchImeButton.getVisibility() == 0;
        boolean hasMultipleEnabledIMEsOrSubtypes = hasMultipleEnabledIMEsOrSubtypes(this.mImm, false);
        if (z != hasMultipleEnabledIMEsOrSubtypes) {
            this.mSwitchImeButton.setVisibility(hasMultipleEnabledIMEsOrSubtypes ? 0 : 8);
        }
        if (this.mSwitchImeButton.getVisibility() != 0) {
            ViewGroup.LayoutParams layoutParams = this.mPasswordEntry.getLayoutParams();
            if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                ((ViewGroup.MarginLayoutParams) layoutParams).setMarginStart(0);
                this.mPasswordEntry.setLayoutParams(layoutParams);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView, android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mImm = (InputMethodManager) getContext().getSystemService("input_method");
        this.mPasswordEntry = (TextView) findViewById(getPasswordTextViewId());
        this.mPasswordEntry.setRestrictedAcrossUser(true);
        this.mPasswordEntryDisabler = new TextViewInputDisabler(this.mPasswordEntry);
        this.mPasswordEntry.setKeyListener(TextKeyListener.getInstance());
        this.mPasswordEntry.setInputType(129);
        this.mPasswordEntry.setOnEditorActionListener(this);
        this.mPasswordEntry.addTextChangedListener(this);
        this.mPasswordEntry.setOnClickListener(new View.OnClickListener() { // from class: com.android.keyguard.KeyguardPasswordView.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                KeyguardPasswordView.this.mCallback.userActivity();
            }
        });
        this.mPasswordEntry.setSelected(true);
        this.mSwitchImeButton = findViewById(com.android.systemui.R.id.switch_ime_button);
        this.mSwitchImeButton.setOnClickListener(new View.OnClickListener() { // from class: com.android.keyguard.KeyguardPasswordView.3
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                KeyguardPasswordView.this.mCallback.userActivity();
                KeyguardPasswordView.this.mImm.showInputMethodPicker(false);
            }
        });
        View findViewById = findViewById(com.android.systemui.R.id.cancel_button);
        if (findViewById != null) {
            findViewById.setOnClickListener(new View.OnClickListener() { // from class: com.android.keyguard.-$$Lambda$KeyguardPasswordView$o6rdkANQuxgpLXMWWI2lzhbd_0k
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    KeyguardPasswordView.this.mCallback.reset();
                }
            });
        }
        updateSwitchImeButton();
        postDelayed(new Runnable() { // from class: com.android.keyguard.KeyguardPasswordView.4
            @Override // java.lang.Runnable
            public void run() {
                KeyguardPasswordView.this.updateSwitchImeButton();
            }
        }, 500L);
    }

    @Override // android.view.ViewGroup
    protected boolean onRequestFocusInDescendants(int i, Rect rect) {
        return this.mPasswordEntry.requestFocus(i, rect);
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    protected void resetPasswordText(boolean z, boolean z2) {
        this.mPasswordEntry.setText("");
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    protected String getPasswordText() {
        return this.mPasswordEntry.getText().toString();
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    protected void setPasswordEntryEnabled(boolean z) {
        this.mPasswordEntry.setEnabled(z);
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    protected void setPasswordEntryInputEnabled(boolean z) {
        this.mPasswordEntryDisabler.setInputEnabled(z);
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
        return i > 1 || inputMethodManager.getEnabledInputMethodSubtypeList(null, false).size() > 1;
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public int getWrongPasswordStringId() {
        return com.android.systemui.R.string.kg_wrong_password;
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

    @Override // android.text.TextWatcher
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        if (this.mCallback != null) {
            this.mCallback.userActivity();
        }
    }

    @Override // android.text.TextWatcher
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
    }

    @Override // android.text.TextWatcher
    public void afterTextChanged(Editable editable) {
        if (!TextUtils.isEmpty(editable)) {
            onUserInput();
        }
    }

    @Override // android.widget.TextView.OnEditorActionListener
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        boolean z = keyEvent == null && (i == 0 || i == 6 || i == 5);
        boolean z2 = keyEvent != null && KeyEvent.isConfirmKey(keyEvent.getKeyCode()) && keyEvent.getAction() == 0;
        if (!z && !z2) {
            return false;
        }
        verifyPasswordAndUnlock();
        return true;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public CharSequence getTitle() {
        return getContext().getString(17040057);
    }
}
