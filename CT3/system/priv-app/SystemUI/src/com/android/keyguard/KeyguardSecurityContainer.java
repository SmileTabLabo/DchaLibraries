package com.android.keyguard;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Slog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardSecurityModel;
import com.mediatek.keyguard.AntiTheft.AntiTheftManager;
import com.mediatek.keyguard.Telephony.KeyguardSimPinPukMeView;
import com.mediatek.keyguard.VoiceWakeup.VoiceWakeupManager;
/* loaded from: a.zip:com/android/keyguard/KeyguardSecurityContainer.class */
public class KeyguardSecurityContainer extends FrameLayout implements KeyguardSecurityView {

    /* renamed from: -com-android-keyguard-KeyguardSecurityModel$SecurityModeSwitchesValues  reason: not valid java name */
    private static final int[] f3x1cbe7e58 = null;
    private KeyguardSecurityCallback mCallback;
    private KeyguardSecurityModel.SecurityMode mCurrentSecuritySelection;
    private LockPatternUtils mLockPatternUtils;
    private ViewGroup mNotificatonPanelView;
    private KeyguardSecurityCallback mNullCallback;
    private SecurityCallback mSecurityCallback;
    private KeyguardSecurityModel mSecurityModel;
    private KeyguardSecurityViewFlipper mSecurityViewFlipper;
    private final KeyguardUpdateMonitor mUpdateMonitor;

    /* loaded from: a.zip:com/android/keyguard/KeyguardSecurityContainer$SecurityCallback.class */
    public interface SecurityCallback {
        boolean dismiss(boolean z);

        void finish(boolean z);

        void onSecurityModeChanged(KeyguardSecurityModel.SecurityMode securityMode, boolean z);

        void reset();

        void updateNavbarStatus();

        void userActivity();
    }

    /* renamed from: -getcom-android-keyguard-KeyguardSecurityModel$SecurityModeSwitchesValues  reason: not valid java name */
    private static /* synthetic */ int[] m613xec5d63fc() {
        if (f3x1cbe7e58 != null) {
            return f3x1cbe7e58;
        }
        int[] iArr = new int[KeyguardSecurityModel.SecurityMode.valuesCustom().length];
        try {
            iArr[KeyguardSecurityModel.SecurityMode.AlarmBoot.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[KeyguardSecurityModel.SecurityMode.AntiTheft.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[KeyguardSecurityModel.SecurityMode.Biometric.ordinal()] = 13;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[KeyguardSecurityModel.SecurityMode.Invalid.ordinal()] = 3;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[KeyguardSecurityModel.SecurityMode.None.ordinal()] = 4;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[KeyguardSecurityModel.SecurityMode.PIN.ordinal()] = 5;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[KeyguardSecurityModel.SecurityMode.Password.ordinal()] = 6;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[KeyguardSecurityModel.SecurityMode.Pattern.ordinal()] = 7;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[KeyguardSecurityModel.SecurityMode.SimPinPukMe1.ordinal()] = 8;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[KeyguardSecurityModel.SecurityMode.SimPinPukMe2.ordinal()] = 9;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[KeyguardSecurityModel.SecurityMode.SimPinPukMe3.ordinal()] = 10;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[KeyguardSecurityModel.SecurityMode.SimPinPukMe4.ordinal()] = 11;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[KeyguardSecurityModel.SecurityMode.Voice.ordinal()] = 12;
        } catch (NoSuchFieldError e13) {
        }
        f3x1cbe7e58 = iArr;
        return iArr;
    }

    public KeyguardSecurityContainer(Context context) {
        this(context, null, 0);
    }

    public KeyguardSecurityContainer(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public KeyguardSecurityContainer(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mCurrentSecuritySelection = KeyguardSecurityModel.SecurityMode.Invalid;
        this.mCallback = new KeyguardSecurityCallback(this) { // from class: com.android.keyguard.KeyguardSecurityContainer.1
            final KeyguardSecurityContainer this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void dismiss(boolean z) {
                this.this$0.mSecurityCallback.dismiss(z);
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void reportUnlockAttempt(int i2, boolean z, int i3) {
                KeyguardUpdateMonitor keyguardUpdateMonitor = KeyguardUpdateMonitor.getInstance(this.this$0.mContext);
                if (z) {
                    keyguardUpdateMonitor.clearFailedUnlockAttempts();
                    this.this$0.mLockPatternUtils.reportSuccessfulPasswordAttempt(i2);
                } else if (this.this$0.mCurrentSecuritySelection == KeyguardSecurityModel.SecurityMode.Biometric || this.this$0.mCurrentSecuritySelection == KeyguardSecurityModel.SecurityMode.Voice) {
                    keyguardUpdateMonitor.reportFailedBiometricUnlockAttempt();
                } else {
                    this.this$0.reportFailedUnlockAttempt(i2, i3);
                }
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void reset() {
                this.this$0.mSecurityCallback.reset();
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void userActivity() {
                if (this.this$0.mSecurityCallback != null) {
                    this.this$0.mSecurityCallback.userActivity();
                }
            }
        };
        this.mNullCallback = new KeyguardSecurityCallback(this) { // from class: com.android.keyguard.KeyguardSecurityContainer.2
            final KeyguardSecurityContainer this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void dismiss(boolean z) {
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void reportUnlockAttempt(int i2, boolean z, int i3) {
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void reset() {
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void userActivity() {
            }
        };
        this.mSecurityModel = new KeyguardSecurityModel(context);
        this.mLockPatternUtils = new LockPatternUtils(context);
        this.mUpdateMonitor = KeyguardUpdateMonitor.getInstance(this.mContext);
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v34, types: [com.android.keyguard.KeyguardSecurityView] */
    /* JADX WARN: Type inference failed for: r0v49, types: [com.android.keyguard.KeyguardSecurityView] */
    private KeyguardSecurityView getSecurityView(KeyguardSecurityModel.SecurityMode securityMode) {
        KeyguardSimPinPukMeView keyguardSimPinPukMeView;
        KeyguardSimPinPukMeView keyguardSimPinPukMeView2;
        int securityViewIdForMode = getSecurityViewIdForMode(securityMode);
        int childCount = this.mSecurityViewFlipper.getChildCount();
        int i = 0;
        while (true) {
            keyguardSimPinPukMeView = null;
            if (i >= childCount) {
                break;
            } else if (this.mSecurityViewFlipper.getChildAt(i).getId() == securityViewIdForMode) {
                keyguardSimPinPukMeView = (KeyguardSecurityView) this.mSecurityViewFlipper.getChildAt(i);
                break;
            } else {
                i++;
            }
        }
        int layoutIdFor = getLayoutIdFor(securityMode);
        if (keyguardSimPinPukMeView != null || layoutIdFor == 0) {
            keyguardSimPinPukMeView2 = keyguardSimPinPukMeView;
            if (keyguardSimPinPukMeView != null) {
                keyguardSimPinPukMeView2 = keyguardSimPinPukMeView;
                if (keyguardSimPinPukMeView instanceof KeyguardSimPinPukMeView) {
                    keyguardSimPinPukMeView2 = keyguardSimPinPukMeView;
                    if (securityMode != this.mCurrentSecuritySelection) {
                        Log.i("KeyguardSecurityView", "getSecurityView, here, we will refresh the layout");
                        keyguardSimPinPukMeView.setPhoneId(this.mSecurityModel.getPhoneIdUsingSecurityMode(securityMode));
                        keyguardSimPinPukMeView2 = keyguardSimPinPukMeView;
                    }
                }
            }
        } else {
            LayoutInflater from = LayoutInflater.from(this.mContext);
            Log.v("KeyguardSecurityView", "inflating id = " + layoutIdFor);
            View inflate = from.inflate(layoutIdFor, (ViewGroup) this.mSecurityViewFlipper, false);
            keyguardSimPinPukMeView2 = (KeyguardSecurityView) inflate;
            if (keyguardSimPinPukMeView2 instanceof KeyguardSimPinPukMeView) {
                keyguardSimPinPukMeView2.setPhoneId(this.mSecurityModel.getPhoneIdUsingSecurityMode(securityMode));
            }
            this.mSecurityViewFlipper.addView(inflate);
            updateSecurityView(inflate);
        }
        return keyguardSimPinPukMeView2;
    }

    private int getSecurityViewIdForMode(KeyguardSecurityModel.SecurityMode securityMode) {
        switch (m613xec5d63fc()[securityMode.ordinal()]) {
            case 1:
                return R$id.power_off_alarm_view;
            case 2:
                return AntiTheftManager.getAntiTheftViewId();
            case 3:
            case 4:
            default:
                return 0;
            case 5:
                return R$id.keyguard_pin_view;
            case 6:
                return R$id.keyguard_password_view;
            case 7:
                return R$id.keyguard_pattern_view;
            case 8:
            case 9:
            case 10:
            case 11:
                return R$id.keyguard_sim_pin_puk_me_view;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void reportFailedUnlockAttempt(int i, int i2) {
        KeyguardUpdateMonitor keyguardUpdateMonitor = KeyguardUpdateMonitor.getInstance(this.mContext);
        int failedUnlockAttempts = keyguardUpdateMonitor.getFailedUnlockAttempts(i) + 1;
        Log.d("KeyguardSecurityView", "reportFailedPatternAttempt: #" + failedUnlockAttempts);
        DevicePolicyManager devicePolicyManager = this.mLockPatternUtils.getDevicePolicyManager();
        int maximumFailedPasswordsForWipe = devicePolicyManager.getMaximumFailedPasswordsForWipe(null, i);
        int i3 = maximumFailedPasswordsForWipe > 0 ? maximumFailedPasswordsForWipe - failedUnlockAttempts : Integer.MAX_VALUE;
        if (i3 < 5) {
            int profileWithMinimumFailedPasswordsForWipe = devicePolicyManager.getProfileWithMinimumFailedPasswordsForWipe(i);
            int i4 = 1;
            if (profileWithMinimumFailedPasswordsForWipe == i) {
                if (profileWithMinimumFailedPasswordsForWipe != 0) {
                    i4 = 3;
                }
            } else if (profileWithMinimumFailedPasswordsForWipe != -10000) {
                i4 = 2;
            }
            if (i3 > 0) {
                showAlmostAtWipeDialog(failedUnlockAttempts, i3, i4);
            } else {
                Slog.i("KeyguardSecurityView", "Too many unlock attempts; user " + profileWithMinimumFailedPasswordsForWipe + " will be wiped!");
                showWipeDialog(failedUnlockAttempts, i4);
            }
        }
        keyguardUpdateMonitor.reportFailedStrongAuthUnlockAttempt(i);
        this.mLockPatternUtils.reportFailedPasswordAttempt(i);
        if (i2 > 0) {
            Log.d("KeyguardSecurityView", "timeoutMs " + i2);
            showTimeoutDialog(i2);
        }
    }

    private void showAlmostAtWipeDialog(int i, int i2, int i3) {
        String str = null;
        switch (i3) {
            case 1:
                str = this.mContext.getString(R$string.kg_failed_attempts_almost_at_wipe, Integer.valueOf(i), Integer.valueOf(i2));
                break;
            case 2:
                str = this.mContext.getString(R$string.kg_failed_attempts_almost_at_erase_profile, Integer.valueOf(i), Integer.valueOf(i2));
                break;
            case 3:
                str = this.mContext.getString(R$string.kg_failed_attempts_almost_at_erase_user, Integer.valueOf(i), Integer.valueOf(i2));
                break;
        }
        showDialog(null, str);
    }

    private void showDialog(String str, String str2) {
        AlertDialog create = new AlertDialog.Builder(this.mContext).setTitle(str).setMessage(str2).setNeutralButton(R$string.ok, (DialogInterface.OnClickListener) null).create();
        if (!(this.mContext instanceof Activity)) {
            create.getWindow().setType(2009);
        }
        create.show();
    }

    private void showSecurityScreen(KeyguardSecurityModel.SecurityMode securityMode) {
        Log.d("KeyguardSecurityView", "showSecurityScreen(" + securityMode + ")");
        if (securityMode != this.mCurrentSecuritySelection || securityMode == KeyguardSecurityModel.SecurityMode.AntiTheft) {
            VoiceWakeupManager.getInstance().notifySecurityModeChange(this.mCurrentSecuritySelection, securityMode);
            Log.d("KeyguardSecurityView", "showSecurityScreen() - get oldview for" + this.mCurrentSecuritySelection);
            KeyguardSecurityView securityView = getSecurityView(this.mCurrentSecuritySelection);
            Log.d("KeyguardSecurityView", "showSecurityScreen() - get newview for" + securityMode);
            KeyguardSecurityView securityView2 = getSecurityView(securityMode);
            if (securityView != null) {
                securityView.onPause();
                Log.d("KeyguardSecurityView", "showSecurityScreen() - oldview.setKeyguardCallback(mNullCallback)");
                securityView.setKeyguardCallback(this.mNullCallback);
            }
            if (securityMode != KeyguardSecurityModel.SecurityMode.None) {
                securityView2.setKeyguardCallback(this.mCallback);
                Log.d("KeyguardSecurityView", "showSecurityScreen() - newview.setKeyguardCallback(mCallback)");
                securityView2.onResume(2);
            }
            int childCount = this.mSecurityViewFlipper.getChildCount();
            int securityViewIdForMode = getSecurityViewIdForMode(securityMode);
            int i = 0;
            while (true) {
                if (i >= childCount) {
                    break;
                } else if (this.mSecurityViewFlipper.getChildAt(i).getId() == securityViewIdForMode) {
                    this.mSecurityViewFlipper.setDisplayedChild(i);
                    break;
                } else {
                    i++;
                }
            }
            Log.d("KeyguardSecurityView", "Before update, mCurrentSecuritySelection = " + this.mCurrentSecuritySelection);
            this.mCurrentSecuritySelection = securityMode;
            Log.d("KeyguardSecurityView", "After update, mCurrentSecuritySelection = " + this.mCurrentSecuritySelection);
            this.mSecurityCallback.onSecurityModeChanged(securityMode, securityMode != KeyguardSecurityModel.SecurityMode.None ? securityView2.needsInput() : false);
        }
    }

    private void showTimeoutDialog(int i) {
        int i2 = i / 1000;
        int i3 = 0;
        switch (m613xec5d63fc()[this.mSecurityModel.getSecurityMode().ordinal()]) {
            case 3:
            case 4:
            case 8:
            case 9:
            case 10:
            case 11:
                break;
            case 5:
                i3 = R$string.kg_too_many_failed_pin_attempts_dialog_message;
                break;
            case 6:
                i3 = R$string.kg_too_many_failed_password_attempts_dialog_message;
                break;
            case 7:
                i3 = R$string.kg_too_many_failed_pattern_attempts_dialog_message;
                break;
            default:
                i3 = 0;
                break;
        }
        if (i3 != 0) {
            showDialog(null, this.mContext.getString(i3, Integer.valueOf(KeyguardUpdateMonitor.getInstance(this.mContext).getFailedUnlockAttempts(KeyguardUpdateMonitor.getCurrentUser())), Integer.valueOf(i2)));
        }
    }

    private void showWipeDialog(int i, int i2) {
        String str = null;
        switch (i2) {
            case 1:
                str = this.mContext.getString(R$string.kg_failed_attempts_now_wiping, Integer.valueOf(i));
                break;
            case 2:
                str = this.mContext.getString(R$string.kg_failed_attempts_now_erasing_profile, Integer.valueOf(i));
                break;
            case 3:
                str = this.mContext.getString(R$string.kg_failed_attempts_now_erasing_user, Integer.valueOf(i));
                break;
        }
        showDialog(null, str);
    }

    private void updateSecurityView(View view) {
        if (!(view instanceof KeyguardSecurityView)) {
            Log.w("KeyguardSecurityView", "View " + view + " is not a KeyguardSecurityView");
            return;
        }
        KeyguardSecurityView keyguardSecurityView = (KeyguardSecurityView) view;
        keyguardSecurityView.setKeyguardCallback(this.mCallback);
        keyguardSecurityView.setLockPatternUtils(this.mLockPatternUtils);
    }

    public KeyguardSecurityModel.SecurityMode getCurrentSecurityMode() {
        return this.mCurrentSecuritySelection;
    }

    public CharSequence getCurrentSecurityModeContentDescription() {
        View view = (View) getSecurityView(this.mCurrentSecuritySelection);
        return view != null ? view.getContentDescription() : "";
    }

    protected int getLayoutIdFor(KeyguardSecurityModel.SecurityMode securityMode) {
        Log.d("KeyguardSecurityView", "getLayoutIdFor, SecurityMode-->" + securityMode);
        switch (m613xec5d63fc()[securityMode.ordinal()]) {
            case 1:
                return R$layout.mtk_power_off_alarm_view;
            case 2:
                return AntiTheftManager.getAntiTheftLayoutId();
            case 3:
            case 4:
            default:
                return 0;
            case 5:
                return R$layout.keyguard_pin_view;
            case 6:
                return R$layout.keyguard_password_view;
            case 7:
                return R$layout.keyguard_pattern_view;
            case 8:
            case 9:
            case 10:
            case 11:
                return R$layout.mtk_keyguard_sim_pin_puk_me_view;
        }
    }

    public KeyguardSecurityModel.SecurityMode getSecurityMode() {
        return this.mSecurityModel.getSecurityMode();
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public boolean needsInput() {
        return this.mSecurityViewFlipper.needsInput();
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        this.mSecurityViewFlipper = (KeyguardSecurityViewFlipper) findViewById(R$id.view_flipper);
        this.mSecurityViewFlipper.setLockPatternUtils(this.mLockPatternUtils);
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void onPause() {
        Log.d("KeyguardSecurityView", "onPause()");
        if (this.mCurrentSecuritySelection != KeyguardSecurityModel.SecurityMode.None) {
            getSecurityView(this.mCurrentSecuritySelection).onPause();
        }
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void onResume(int i) {
        Log.d("KeyguardSecurityView", "onResume(reason = " + i + ")");
        Log.d("KeyguardSecurityView", "onResume(mCurrentSecuritySelection = " + this.mCurrentSecuritySelection + ")");
        if (this.mCurrentSecuritySelection != KeyguardSecurityModel.SecurityMode.None) {
            getSecurityView(this.mCurrentSecuritySelection).onResume(i);
        }
    }

    public void onScreenTurnedOff() {
        Log.d("KeyguardSecurityView", "onScreenTurnedOff");
    }

    void setCurrentSecurityMode(KeyguardSecurityModel.SecurityMode securityMode) {
        this.mCurrentSecuritySelection = securityMode;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void setKeyguardCallback(KeyguardSecurityCallback keyguardSecurityCallback) {
        this.mSecurityViewFlipper.setKeyguardCallback(keyguardSecurityCallback);
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void setLockPatternUtils(LockPatternUtils lockPatternUtils) {
        this.mLockPatternUtils = lockPatternUtils;
        this.mSecurityModel.setLockPatternUtils(lockPatternUtils);
        this.mSecurityViewFlipper.setLockPatternUtils(this.mLockPatternUtils);
    }

    public void setNotificationPanelView(ViewGroup viewGroup) {
        this.mNotificatonPanelView = viewGroup;
    }

    public void setSecurityCallback(SecurityCallback securityCallback) {
        this.mSecurityCallback = securityCallback;
    }

    void setSecurityMode(KeyguardSecurityModel keyguardSecurityModel) {
        this.mSecurityModel = keyguardSecurityModel;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void showMessage(String str, int i) {
        if (this.mCurrentSecuritySelection != KeyguardSecurityModel.SecurityMode.None) {
            getSecurityView(this.mCurrentSecuritySelection).showMessage(str, i);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean showNextSecurityScreenOrFinish(boolean z) {
        boolean z2;
        boolean z3;
        Log.d("KeyguardSecurityView", "showNextSecurityScreenOrFinish(" + z + ")");
        Log.d("KeyguardSecurityView", "showNext.. mCurrentSecuritySelection = " + this.mCurrentSecuritySelection);
        if (!this.mUpdateMonitor.getUserCanSkipBouncer(KeyguardUpdateMonitor.getCurrentUser())) {
            if (KeyguardSecurityModel.SecurityMode.None != this.mCurrentSecuritySelection) {
                z2 = false;
                z3 = false;
                if (z) {
                    Log.d("KeyguardSecurityView", "showNextSecurityScreenOrFinish() - authenticated is True, and mCurrentSecuritySelection = " + this.mCurrentSecuritySelection);
                    KeyguardSecurityModel.SecurityMode securityMode = this.mSecurityModel.getSecurityMode();
                    Log.v("KeyguardSecurityView", "securityMode = " + securityMode);
                    Log.d("KeyguardSecurityView", "mCurrentSecuritySelection: " + this.mCurrentSecuritySelection);
                    switch (m613xec5d63fc()[this.mCurrentSecuritySelection.ordinal()]) {
                        case 2:
                            KeyguardSecurityModel.SecurityMode securityMode2 = this.mSecurityModel.getSecurityMode();
                            Log.v("KeyguardSecurityView", "now is Antitheft, next securityMode = " + securityMode2);
                            if (securityMode2 == KeyguardSecurityModel.SecurityMode.None) {
                                z2 = true;
                                z3 = false;
                                break;
                            } else {
                                showSecurityScreen(securityMode2);
                                z2 = false;
                                z3 = false;
                                break;
                            }
                        case 3:
                        case 4:
                        default:
                            Log.v("KeyguardSecurityView", "Bad security screen " + this.mCurrentSecuritySelection + ", fail safe");
                            showPrimarySecurityScreen(false);
                            z2 = false;
                            z3 = false;
                            break;
                        case 5:
                        case 6:
                        case 7:
                            z3 = true;
                            z2 = true;
                            break;
                        case 8:
                        case 9:
                        case 10:
                        case 11:
                            if (securityMode == KeyguardSecurityModel.SecurityMode.None) {
                                z2 = true;
                                z3 = false;
                                break;
                            } else {
                                showSecurityScreen(securityMode);
                                z2 = false;
                                z3 = false;
                                break;
                            }
                        case 12:
                            if (!this.mSecurityModel.isSimPinPukSecurityMode(securityMode)) {
                                z2 = true;
                                z3 = false;
                                break;
                            } else {
                                showSecurityScreen(securityMode);
                                z2 = false;
                                z3 = false;
                                break;
                            }
                    }
                }
            } else {
                KeyguardSecurityModel.SecurityMode securityMode3 = this.mSecurityModel.getSecurityMode();
                if (KeyguardSecurityModel.SecurityMode.None == securityMode3) {
                    Log.d("KeyguardSecurityView", "showNextSecurityScreenOrFinish() - securityMode is None, just finish.");
                    z2 = true;
                    z3 = false;
                } else {
                    Log.d("KeyguardSecurityView", "showNextSecurityScreenOrFinish()- switch to the alternate security view for None mode.");
                    showSecurityScreen(securityMode3);
                    z2 = false;
                    z3 = false;
                }
            }
        } else {
            z2 = true;
            z3 = false;
        }
        this.mSecurityCallback.updateNavbarStatus();
        if (z2) {
            this.mSecurityCallback.finish(z3);
            Log.d("KeyguardSecurityView", "finish ");
        }
        Log.d("KeyguardSecurityView", "showNextSecurityScreenOrFinish() - return finish = " + z2);
        return z2;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void showPrimarySecurityScreen(boolean z) {
        KeyguardSecurityModel.SecurityMode securityMode = this.mSecurityModel.getSecurityMode();
        Log.v("KeyguardSecurityView", "showPrimarySecurityScreen(turningOff=" + z + ")");
        Log.v("KeyguardSecurityView", "showPrimarySecurityScreen(securityMode=" + securityMode + ")");
        if (this.mSecurityModel.isSimPinPukSecurityMode(this.mCurrentSecuritySelection)) {
            Log.d("KeyguardSecurityView", "showPrimarySecurityScreen() - current is " + this.mCurrentSecuritySelection);
            int phoneIdUsingSecurityMode = this.mSecurityModel.getPhoneIdUsingSecurityMode(this.mCurrentSecuritySelection);
            Log.d("KeyguardSecurityView", "showPrimarySecurityScreen() - phoneId of currentView is " + phoneIdUsingSecurityMode);
            boolean isSimPinSecure = this.mUpdateMonitor.isSimPinSecure(phoneIdUsingSecurityMode);
            Log.d("KeyguardSecurityView", "showPrimarySecurityScreen() - isCurrentModeSimPinSecure = " + isSimPinSecure);
            if (isSimPinSecure) {
                Log.d("KeyguardSecurityView", "Skip show security because it already shows SimPinPukMeView");
                return;
            }
            Log.d("KeyguardSecurityView", "showPrimarySecurityScreen() - since current simpinview not secured, we should call showSecurityScreen() to set correct PhoneId for next view.");
        }
        KeyguardSecurityModel.SecurityMode securityMode2 = securityMode;
        if (!z) {
            securityMode2 = securityMode;
            if (KeyguardUpdateMonitor.getInstance(this.mContext).isAlternateUnlockEnabled()) {
                Log.d("KeyguardSecurityView", "showPrimarySecurityScreen() - will be call getAlternateFor");
                securityMode2 = this.mSecurityModel.getAlternateFor(securityMode);
            }
        }
        showSecurityScreen(securityMode2);
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void showPromptReason(int i) {
        if (this.mCurrentSecuritySelection != KeyguardSecurityModel.SecurityMode.None) {
            if (i != 0) {
                Log.i("KeyguardSecurityView", "Strong auth required, reason: " + i);
            }
            getSecurityView(this.mCurrentSecuritySelection).showPromptReason(i);
        }
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void startAppearAnimation() {
        if (this.mCurrentSecuritySelection != KeyguardSecurityModel.SecurityMode.None) {
            getSecurityView(this.mCurrentSecuritySelection).startAppearAnimation();
        }
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public boolean startDisappearAnimation(Runnable runnable) {
        if (this.mCurrentSecuritySelection != KeyguardSecurityModel.SecurityMode.None) {
            return getSecurityView(this.mCurrentSecuritySelection).startDisappearAnimation(runnable);
        }
        return false;
    }
}
