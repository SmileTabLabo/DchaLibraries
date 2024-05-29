package com.android.keyguard;

import android.app.ActivityManagerNative;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.telecom.TelecomManager;
import android.telephony.ServiceState;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Slog;
import android.view.View;
import android.widget.Button;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.widget.LockPatternUtils;
import com.mediatek.internal.telephony.ITelephonyEx;
import com.mediatek.keyguard.AntiTheft.AntiTheftManager;
import com.mediatek.keyguard.Plugin.KeyguardPluginFactory;
import com.mediatek.keyguard.ext.IEmergencyButtonExt;
/* loaded from: a.zip:com/android/keyguard/EmergencyButton.class */
public class EmergencyButton extends Button {
    private static final Intent INTENT_EMERGENCY_DIAL = new Intent().setAction("com.android.phone.EmergencyDialer.DIAL").setPackage("com.android.phone").setFlags(343932928);
    private int mEccPhoneIdForNoneSecurityMode;
    private EmergencyButtonCallback mEmergencyButtonCallback;
    private IEmergencyButtonExt mEmergencyButtonExt;
    private final boolean mEnableEmergencyCallWhileSimLocked;
    KeyguardUpdateMonitorCallback mInfoCallback;
    private boolean mIsSecure;
    private final boolean mIsVoiceCapable;
    private boolean mLocateAtNonSecureView;
    private LockPatternUtils mLockPatternUtils;
    private PowerManager mPowerManager;
    private KeyguardUpdateMonitor mUpdateMonitor;

    /* loaded from: a.zip:com/android/keyguard/EmergencyButton$EmergencyButtonCallback.class */
    public interface EmergencyButtonCallback {
        void onEmergencyButtonClickedWhenInCall();
    }

    public EmergencyButton(Context context) {
        this(context, null);
    }

    public EmergencyButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mUpdateMonitor = null;
        this.mEccPhoneIdForNoneSecurityMode = -1;
        this.mLocateAtNonSecureView = false;
        this.mInfoCallback = new KeyguardUpdateMonitorCallback(this) { // from class: com.android.keyguard.EmergencyButton.1
            final EmergencyButton this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onPhoneStateChanged(int i) {
                this.this$0.updateEmergencyCallButton();
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onRefreshCarrierInfo() {
                this.this$0.updateEmergencyCallButton();
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onSimStateChangedUsingPhoneId(int i, IccCardConstants.State state) {
                Log.d("EmergencyButton", "onSimStateChangedUsingSubId: " + state + ", phoneId=" + i);
                this.this$0.updateEmergencyCallButton();
            }
        };
        this.mIsVoiceCapable = context.getResources().getBoolean(17956953);
        this.mEnableEmergencyCallWhileSimLocked = this.mContext.getResources().getBoolean(17956937);
        this.mUpdateMonitor = KeyguardUpdateMonitor.getInstance(this.mContext);
        try {
            this.mEmergencyButtonExt = KeyguardPluginFactory.getEmergencyButtonExt(context);
        } catch (Exception e) {
            Log.d("EmergencyButton", "EmergencyButton() - error in calling getEmergencyButtonExt().");
            e.printStackTrace();
        }
        this.mLocateAtNonSecureView = context.obtainStyledAttributes(attributeSet, R$styleable.ECCButtonAttr).getBoolean(R$styleable.ECCButtonAttr_locateAtNonSecureView, this.mLocateAtNonSecureView);
    }

    private boolean eccButtonShouldShow() {
        int numOfPhone = KeyguardUtils.getNumOfPhone();
        boolean[] zArr = new boolean[numOfPhone];
        try {
            ITelephonyEx asInterface = ITelephonyEx.Stub.asInterface(ServiceManager.checkService("phoneEx"));
            if (asInterface != null) {
                this.mEccPhoneIdForNoneSecurityMode = -1;
                for (int i = 0; i < numOfPhone; i++) {
                    Bundle serviceState = asInterface.getServiceState(KeyguardUtils.getSubIdUsingPhoneId(i));
                    if (serviceState != null) {
                        ServiceState newFromBundle = ServiceState.newFromBundle(serviceState);
                        Log.i("EmergencyButton", "ss.getState() = " + newFromBundle.getState() + " ss.isEmergencyOnly()=" + newFromBundle.isEmergencyOnly() + " for simId=" + i);
                        if (newFromBundle.getState() == 0 || newFromBundle.isEmergencyOnly()) {
                            zArr[i] = true;
                            if (this.mEccPhoneIdForNoneSecurityMode == -1) {
                                this.mEccPhoneIdForNoneSecurityMode = i;
                            }
                        } else {
                            zArr[i] = false;
                        }
                    }
                }
            }
        } catch (RemoteException e) {
            Log.i("EmergencyButton", "getServiceState error e:" + e.getMessage());
        }
        return this.mEmergencyButtonExt.showEccByServiceState(zArr, getCurPhoneId());
    }

    private int getCurPhoneId() {
        KeyguardSecurityModel keyguardSecurityModel = new KeyguardSecurityModel(this.mContext);
        return keyguardSecurityModel.getPhoneIdUsingSecurityMode(keyguardSecurityModel.getSecurityMode());
    }

    private TelecomManager getTelecommManager() {
        return (TelecomManager) this.mContext.getSystemService("telecom");
    }

    private boolean isInCall() {
        return getTelecommManager().isInCall();
    }

    private void resumeCall() {
        getTelecommManager().showInCallScreen(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateEmergencyCallButton() {
        boolean z;
        boolean z2 = false;
        if (isInCall()) {
            z2 = true;
        } else if (this.mLockPatternUtils.isEmergencyCallCapable()) {
            z2 = KeyguardUpdateMonitor.getInstance(this.mContext).isSimPinVoiceSecure() ? this.mEnableEmergencyCallWhileSimLocked : this.mLockPatternUtils.isSecure(KeyguardUpdateMonitor.getCurrentUser());
        }
        boolean isAntiTheftLocked = AntiTheftManager.isAntiTheftLocked();
        boolean eccButtonShouldShow = eccButtonShouldShow();
        Log.i("EmergencyButton", "mLocateAtNonSecureView = " + this.mLocateAtNonSecureView);
        if (!this.mLocateAtNonSecureView || this.mEmergencyButtonExt.showEccInNonSecureUnlock()) {
            z = (z2 || isAntiTheftLocked || this.mEmergencyButtonExt.showEccInNonSecureUnlock()) ? eccButtonShouldShow : false;
            Log.i("EmergencyButton", "show = " + z + " --> visible= " + z2 + ", antiTheftLocked=" + isAntiTheftLocked + ", mEmergencyButtonExt.showEccInNonSecureUnlock() =" + this.mEmergencyButtonExt.showEccInNonSecureUnlock() + ", eccShouldShow=" + eccButtonShouldShow);
        } else {
            Log.i("EmergencyButton", "ECC Button is located on Notification Keygaurd and OP do not ask to show. So this is a normal case ,we never show it.");
            z = false;
        }
        if (!this.mLocateAtNonSecureView || z) {
            this.mLockPatternUtils.updateEmergencyCallButtonState(this, z, false);
            return;
        }
        Log.i("EmergencyButton", "If the button is on NotificationKeyguard and will not show, we should just set it View.GONE to give more space to IndicationText.");
        setVisibility(8);
    }

    @Override // android.widget.TextView, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        KeyguardUpdateMonitor.getInstance(this.mContext).registerCallback(this.mInfoCallback);
    }

    @Override // android.widget.TextView, android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        updateEmergencyCallButton();
        setText(R$string.kg_emergency_call_label);
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
        setOnClickListener(new View.OnClickListener(this) { // from class: com.android.keyguard.EmergencyButton.2
            final EmergencyButton this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                this.this$0.takeEmergencyCallAction();
            }
        });
        this.mIsSecure = this.mLockPatternUtils.isSecure(KeyguardUpdateMonitor.getCurrentUser());
        updateEmergencyCallButton();
    }

    public void setCallback(EmergencyButtonCallback emergencyButtonCallback) {
        this.mEmergencyButtonCallback = emergencyButtonCallback;
    }

    public void takeEmergencyCallAction() {
        MetricsLogger.action(this.mContext, 200);
        this.mPowerManager.userActivity(SystemClock.uptimeMillis(), true);
        try {
            ActivityManagerNative.getDefault().stopSystemLockTaskMode();
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
        int i = curPhoneId;
        if (curPhoneId == -1) {
            i = this.mEccPhoneIdForNoneSecurityMode;
        }
        this.mEmergencyButtonExt.customizeEmergencyIntent(INTENT_EMERGENCY_DIAL, i);
        getContext().startActivityAsUser(INTENT_EMERGENCY_DIAL, ActivityOptions.makeCustomAnimation(getContext(), 0, 0).toBundle(), new UserHandle(KeyguardUpdateMonitor.getCurrentUser()));
    }
}
