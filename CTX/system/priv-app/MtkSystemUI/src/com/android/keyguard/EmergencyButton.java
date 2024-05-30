package com.android.keyguard;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.telecom.TelecomManager;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Slog;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.util.EmergencyAffordanceManager;
import com.android.internal.widget.LockPatternUtils;
import com.mediatek.keyguard.AntiTheft.AntiTheftManager;
import com.mediatek.keyguard.ext.IEmergencyButtonExt;
import com.mediatek.keyguard.ext.OpKeyguardCustomizationFactoryBase;
/* loaded from: classes.dex */
public class EmergencyButton extends Button {
    private static final boolean DEBUG = KeyguardConstants.DEBUG;
    private static final Intent INTENT_EMERGENCY_DIAL = new Intent().setAction("com.android.phone.EmergencyDialer.DIAL").setPackage("com.android.phone").setFlags(343932928);
    private int mDownX;
    private int mDownY;
    private int mEccPhoneIdForNoneSecurityMode;
    private final EmergencyAffordanceManager mEmergencyAffordanceManager;
    private EmergencyButtonCallback mEmergencyButtonCallback;
    private IEmergencyButtonExt mEmergencyButtonExt;
    private final boolean mEnableEmergencyCallWhileSimLocked;
    KeyguardUpdateMonitorCallback mInfoCallback;
    private boolean mIsSecure;
    private final boolean mIsVoiceCapable;
    private boolean mLocateAtNonSecureView;
    private LockPatternUtils mLockPatternUtils;
    private boolean mLongPressWasDragged;
    private PowerManager mPowerManager;
    private TelephonyManager mTelephonyManager;

    /* loaded from: classes.dex */
    public interface EmergencyButtonCallback {
        void onEmergencyButtonClickedWhenInCall();
    }

    public EmergencyButton(Context context) {
        this(context, null);
    }

    public EmergencyButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mInfoCallback = new KeyguardUpdateMonitorCallback() { // from class: com.android.keyguard.EmergencyButton.1
            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onPhoneStateChanged(int i) {
                EmergencyButton.this.updateEmergencyCallButton();
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onSimStateChangedUsingPhoneId(int i, IccCardConstants.State state) {
                if (EmergencyButton.DEBUG) {
                    Slog.d("EmergencyButton", "onSimStateChangedUsingSubId: " + state + ", phoneId=" + i);
                }
                EmergencyButton.this.updateEmergencyCallButton();
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onRefreshCarrierInfo() {
                EmergencyButton.this.updateEmergencyCallButton();
            }
        };
        this.mEccPhoneIdForNoneSecurityMode = -1;
        this.mLocateAtNonSecureView = false;
        this.mIsVoiceCapable = context.getResources().getBoolean(17957067);
        this.mEnableEmergencyCallWhileSimLocked = this.mContext.getResources().getBoolean(17956970);
        this.mEmergencyAffordanceManager = new EmergencyAffordanceManager(context);
        try {
            this.mEmergencyButtonExt = OpKeyguardCustomizationFactoryBase.getOpFactory(this.mContext).makeEmergencyButton();
        } catch (Exception e) {
            Slog.d("EmergencyButton", "EmergencyButton() - error in calling getEmergencyButtonExt().");
            e.printStackTrace();
        }
        this.mLocateAtNonSecureView = context.obtainStyledAttributes(attributeSet, R.styleable.ECCButtonAttr).getBoolean(0, this.mLocateAtNonSecureView);
        this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
    }

    @Override // android.widget.TextView, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        KeyguardUpdateMonitor.getInstance(this.mContext).registerCallback(this.mInfoCallback);
    }

    @Override // android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        KeyguardUpdateMonitor.getInstance(this.mContext).removeCallback(this.mInfoCallback);
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mLockPatternUtils = new LockPatternUtils(this.mContext);
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        setOnClickListener(new View.OnClickListener() { // from class: com.android.keyguard.EmergencyButton.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                EmergencyButton.this.takeEmergencyCallAction();
            }
        });
        this.mIsSecure = this.mLockPatternUtils.isSecure(KeyguardUpdateMonitor.getCurrentUser());
        setOnLongClickListener(new View.OnLongClickListener() { // from class: com.android.keyguard.EmergencyButton.3
            @Override // android.view.View.OnLongClickListener
            public boolean onLongClick(View view) {
                if (!EmergencyButton.this.mLongPressWasDragged && EmergencyButton.this.mEmergencyAffordanceManager.needsEmergencyAffordance()) {
                    EmergencyButton.this.mEmergencyAffordanceManager.performEmergencyCall();
                    return true;
                }
                return false;
            }
        });
        updateEmergencyCallButton();
    }

    @Override // android.widget.TextView, android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        int x = (int) motionEvent.getX();
        int y = (int) motionEvent.getY();
        if (motionEvent.getActionMasked() == 0) {
            this.mDownX = x;
            this.mDownY = y;
            this.mLongPressWasDragged = false;
        } else {
            int abs = Math.abs(x - this.mDownX);
            int abs2 = Math.abs(y - this.mDownY);
            int scaledTouchSlop = ViewConfiguration.get(this.mContext).getScaledTouchSlop();
            if (Math.abs(abs2) > scaledTouchSlop || Math.abs(abs) > scaledTouchSlop) {
                this.mLongPressWasDragged = true;
            }
        }
        return super.onTouchEvent(motionEvent);
    }

    @Override // android.widget.TextView, android.view.View
    public boolean performLongClick() {
        return super.performLongClick();
    }

    @Override // android.widget.TextView, android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        setText(com.android.systemui.R.string.kg_emergency_call_label);
        updateEmergencyCallButton();
    }

    public void takeEmergencyCallAction() {
        MetricsLogger.action(this.mContext, 200);
        this.mPowerManager.userActivity(SystemClock.uptimeMillis(), true);
        try {
            ActivityManager.getService().stopSystemLockTaskMode();
        } catch (RemoteException e) {
            Slog.w("EmergencyButton", "Failed to stop app pinning");
        }
        if (isInCall()) {
            resumeCall();
            if (this.mEmergencyButtonCallback != null) {
                this.mEmergencyButtonCallback.onEmergencyButtonClickedWhenInCall();
                return;
            }
            return;
        }
        KeyguardUpdateMonitor.getInstance(this.mContext).reportEmergencyCallAction(true);
        int curPhoneId = getCurPhoneId();
        if (curPhoneId == -1) {
            curPhoneId = this.mEccPhoneIdForNoneSecurityMode;
        }
        this.mEmergencyButtonExt.customizeEmergencyIntent(INTENT_EMERGENCY_DIAL, curPhoneId);
        getContext().startActivityAsUser(INTENT_EMERGENCY_DIAL, ActivityOptions.makeCustomAnimation(getContext(), 0, 0).toBundle(), new UserHandle(KeyguardUpdateMonitor.getCurrentUser()));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateEmergencyCallButton() {
        boolean z;
        boolean isSecure = isInCall() ? true : this.mIsVoiceCapable ? KeyguardUpdateMonitor.getInstance(this.mContext).isSimPinVoiceSecure() ? this.mEnableEmergencyCallWhileSimLocked : this.mLockPatternUtils.isSecure(KeyguardUpdateMonitor.getCurrentUser()) : false;
        boolean isAntiTheftLocked = AntiTheftManager.isAntiTheftLocked();
        boolean eccButtonShouldShow = eccButtonShouldShow();
        if (DEBUG) {
            Slog.i("EmergencyButton", "mLocateAtNonSecureView = " + this.mLocateAtNonSecureView);
        }
        if (this.mLocateAtNonSecureView && !this.mEmergencyButtonExt.showEccInNonSecureUnlock()) {
            if (DEBUG) {
                Slog.i("EmergencyButton", "ECC Button is located on Notification Keygaurd and OP do not ask to show. So this is a normal case ,we never show it.");
            }
            z = false;
        } else {
            z = (isSecure || isAntiTheftLocked || this.mEmergencyButtonExt.showEccInNonSecureUnlock()) && eccButtonShouldShow;
            Slog.i("EmergencyButton", "show = " + z);
            if (DEBUG) {
                Slog.i("EmergencyButton", "visible= " + isSecure + ", antiTheftLocked=" + isAntiTheftLocked + ", mEmergencyButtonExt.showEccInNonSecureUnlock() =" + this.mEmergencyButtonExt.showEccInNonSecureUnlock() + ", eccShouldShow=" + eccButtonShouldShow);
            }
        }
        if (this.mLocateAtNonSecureView && !z) {
            setVisibility(8);
        } else {
            updateEmergencyCallButtonState(z, false);
        }
    }

    public void setCallback(EmergencyButtonCallback emergencyButtonCallback) {
        this.mEmergencyButtonCallback = emergencyButtonCallback;
    }

    private void resumeCall() {
        getTelecommManager().showInCallScreen(false);
    }

    private boolean isInCall() {
        return getTelecommManager().isInCall();
    }

    private TelecomManager getTelecommManager() {
        return (TelecomManager) this.mContext.getSystemService("telecom");
    }

    private boolean eccButtonShouldShow() {
        int numOfPhone = KeyguardUtils.getNumOfPhone();
        boolean[] zArr = new boolean[numOfPhone];
        TelephonyManager telephonyManager = TelephonyManager.getDefault();
        this.mEccPhoneIdForNoneSecurityMode = -1;
        if (telephonyManager != null) {
            for (int i = 0; i < numOfPhone; i++) {
                ServiceState serviceStateForSubscriber = this.mTelephonyManager.getServiceStateForSubscriber(KeyguardUtils.getSubIdUsingPhoneId(i));
                if (serviceStateForSubscriber != null) {
                    Slog.i("EmergencyButton", "ss.getState()=" + serviceStateForSubscriber.getState() + " ss.isEmergencyOnly()=" + serviceStateForSubscriber.isEmergencyOnly() + " for simId=" + i);
                    if (serviceStateForSubscriber.getState() == 0 || serviceStateForSubscriber.isEmergencyOnly()) {
                        zArr[i] = true;
                        if (this.mEccPhoneIdForNoneSecurityMode == -1) {
                            this.mEccPhoneIdForNoneSecurityMode = i;
                        }
                    } else {
                        zArr[i] = false;
                    }
                } else {
                    Slog.e("EmergencyButton", "Service state is null");
                }
            }
        }
        return this.mEmergencyButtonExt.showEccByServiceState(zArr, getCurPhoneId());
    }

    private int getCurPhoneId() {
        KeyguardSecurityModel keyguardSecurityModel = new KeyguardSecurityModel(this.mContext);
        return keyguardSecurityModel.getPhoneIdUsingSecurityMode(keyguardSecurityModel.getSecurityMode(KeyguardUpdateMonitor.getCurrentUser()));
    }

    public void updateEmergencyCallButtonState(boolean z, boolean z2) {
        int i;
        if (this.mIsVoiceCapable && z) {
            setVisibility(0);
            if (isInCall()) {
                i = 17040168;
                setCompoundDrawablesWithIntrinsicBounds(z2 ? 17301636 : 0, 0, 0, 0);
            } else {
                i = 17040141;
                setCompoundDrawablesWithIntrinsicBounds(z2 ? 17302366 : 0, 0, 0, 0);
            }
            setText(i);
            return;
        }
        setVisibility(4);
    }
}
