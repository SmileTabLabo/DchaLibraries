package com.android.systemui.statusbar.phone;

import android.app.ActivityManagerNative;
import android.app.IApplicationThread;
import android.app.ProfilerInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.UserHandle;
import android.telecom.TelecomManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.EmergencyButton;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.EventLogTags;
import com.android.systemui.Interpolators;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.statusbar.KeyguardAffordanceView;
import com.android.systemui.statusbar.KeyguardIndicationController;
import com.android.systemui.statusbar.phone.ActivityStarter;
import com.android.systemui.statusbar.phone.UnlockMethodCache;
import com.android.systemui.statusbar.policy.AccessibilityController;
import com.android.systemui.statusbar.policy.FlashlightController;
import com.android.systemui.statusbar.policy.PreviewInflater;
import com.mediatek.keyguard.Plugin.KeyguardPluginFactory;
import com.mediatek.keyguard.ext.IEmergencyButtonExt;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/KeyguardBottomAreaView.class */
public class KeyguardBottomAreaView extends FrameLayout implements View.OnClickListener, UnlockMethodCache.OnUnlockMethodChangedListener, AccessibilityController.AccessibilityStateChangedCallback, View.OnLongClickListener {
    private AccessibilityController mAccessibilityController;
    private View.AccessibilityDelegate mAccessibilityDelegate;
    private ActivityStarter mActivityStarter;
    private AssistManager mAssistManager;
    private KeyguardAffordanceView mCameraImageView;
    private View mCameraPreview;
    private final BroadcastReceiver mDevicePolicyReceiver;
    private EmergencyButton mEmergencyButton;
    private IEmergencyButtonExt mEmergencyButtonExt;
    private FlashlightController mFlashlightController;
    private KeyguardIndicationController mIndicationController;
    private TextView mIndicationText;
    private KeyguardAffordanceView mLeftAffordanceView;
    private boolean mLeftIsVoiceAssist;
    private View mLeftPreview;
    private LockIcon mLockIcon;
    private LockPatternUtils mLockPatternUtils;
    private PhoneStatusBar mPhoneStatusBar;
    private ViewGroup mPreviewContainer;
    private PreviewInflater mPreviewInflater;
    private boolean mPrewarmBound;
    private final ServiceConnection mPrewarmConnection;
    private Messenger mPrewarmMessenger;
    private UnlockMethodCache mUnlockMethodCache;
    private final KeyguardUpdateMonitorCallback mUpdateMonitorCallback;
    private boolean mUserSetupComplete;
    private static final Intent SECURE_CAMERA_INTENT = new Intent("android.media.action.STILL_IMAGE_CAMERA_SECURE").addFlags(8388608);
    public static final Intent INSECURE_CAMERA_INTENT = new Intent("android.media.action.STILL_IMAGE_CAMERA");
    private static final Intent PHONE_INTENT = new Intent("android.intent.action.DIAL");

    /* renamed from: com.android.systemui.statusbar.phone.KeyguardBottomAreaView$3  reason: invalid class name */
    /* loaded from: a.zip:com/android/systemui/statusbar/phone/KeyguardBottomAreaView$3.class */
    class AnonymousClass3 extends BroadcastReceiver {
        final KeyguardBottomAreaView this$0;

        AnonymousClass3(KeyguardBottomAreaView keyguardBottomAreaView) {
            this.this$0 = keyguardBottomAreaView;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            this.this$0.post(new Runnable(this) { // from class: com.android.systemui.statusbar.phone.KeyguardBottomAreaView.3.1
                final AnonymousClass3 this$1;

                {
                    this.this$1 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$1.this$0.updateCameraVisibility();
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.android.systemui.statusbar.phone.KeyguardBottomAreaView$5  reason: invalid class name */
    /* loaded from: a.zip:com/android/systemui/statusbar/phone/KeyguardBottomAreaView$5.class */
    public class AnonymousClass5 implements Runnable {
        final KeyguardBottomAreaView this$0;
        final Intent val$intent;

        AnonymousClass5(KeyguardBottomAreaView keyguardBottomAreaView, Intent intent) {
            this.this$0 = keyguardBottomAreaView;
            this.val$intent = intent;
        }

        @Override // java.lang.Runnable
        public void run() {
            int i;
            try {
                this.val$intent.setFlags(67108864);
                i = ActivityManagerNative.getDefault().startActivityAsUser((IApplicationThread) null, this.this$0.getContext().getBasePackageName(), this.val$intent, this.val$intent.resolveTypeIfNeeded(this.this$0.getContext().getContentResolver()), (IBinder) null, (String) null, 0, 268435456, (ProfilerInfo) null, (Bundle) null, UserHandle.CURRENT.getIdentifier());
            } catch (RemoteException e) {
                Log.w("PhoneStatusBar/KeyguardBottomAreaView", "Unable to start camera activity", e);
                i = -6;
            }
            this.this$0.mActivityStarter.preventNextAnimation();
            this.this$0.post(new Runnable(this, KeyguardBottomAreaView.isSuccessfulLaunch(i)) { // from class: com.android.systemui.statusbar.phone.KeyguardBottomAreaView.5.1
                final AnonymousClass5 this$1;
                final boolean val$launched;

                {
                    this.this$1 = this;
                    this.val$launched = r5;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$1.this$0.unbindCameraPrewarmService(this.val$launched);
                }
            });
        }
    }

    public KeyguardBottomAreaView(Context context) {
        this(context, null);
    }

    public KeyguardBottomAreaView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public KeyguardBottomAreaView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public KeyguardBottomAreaView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mPrewarmConnection = new ServiceConnection(this) { // from class: com.android.systemui.statusbar.phone.KeyguardBottomAreaView.1
            final KeyguardBottomAreaView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.content.ServiceConnection
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                this.this$0.mPrewarmMessenger = new Messenger(iBinder);
            }

            @Override // android.content.ServiceConnection
            public void onServiceDisconnected(ComponentName componentName) {
                this.this$0.mPrewarmMessenger = null;
            }
        };
        this.mAccessibilityDelegate = new View.AccessibilityDelegate(this) { // from class: com.android.systemui.statusbar.phone.KeyguardBottomAreaView.2
            final KeyguardBottomAreaView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.AccessibilityDelegate
            public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfo accessibilityNodeInfo) {
                super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfo);
                String str = null;
                if (view == this.this$0.mLockIcon) {
                    str = this.this$0.getResources().getString(2131493358);
                } else if (view == this.this$0.mCameraImageView) {
                    str = this.this$0.getResources().getString(2131493361);
                } else if (view == this.this$0.mLeftAffordanceView) {
                    str = this.this$0.mLeftIsVoiceAssist ? this.this$0.getResources().getString(2131493360) : this.this$0.getResources().getString(2131493359);
                }
                accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(16, str));
            }

            @Override // android.view.View.AccessibilityDelegate
            public boolean performAccessibilityAction(View view, int i3, Bundle bundle) {
                if (i3 == 16) {
                    if (view == this.this$0.mLockIcon) {
                        this.this$0.mPhoneStatusBar.animateCollapsePanels(2, true);
                        return true;
                    } else if (view == this.this$0.mCameraImageView) {
                        this.this$0.launchCamera("lockscreen_affordance");
                        return true;
                    } else if (view == this.this$0.mLeftAffordanceView) {
                        this.this$0.launchLeftAffordance();
                        return true;
                    }
                }
                return super.performAccessibilityAction(view, i3, bundle);
            }
        };
        this.mDevicePolicyReceiver = new AnonymousClass3(this);
        this.mUpdateMonitorCallback = new KeyguardUpdateMonitorCallback(this) { // from class: com.android.systemui.statusbar.phone.KeyguardBottomAreaView.4
            final KeyguardBottomAreaView this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onFingerprintRunningStateChanged(boolean z) {
                this.this$0.mLockIcon.update();
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onFinishedGoingToSleep(int i3) {
                this.this$0.mLockIcon.setDeviceInteractive(false);
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onKeyguardVisibilityChanged(boolean z) {
                this.this$0.mLockIcon.update();
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onScreenTurnedOff() {
                this.this$0.mLockIcon.setScreenOn(false);
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onScreenTurnedOn() {
                this.this$0.mLockIcon.setScreenOn(true);
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onStartedWakingUp() {
                this.this$0.mLockIcon.setDeviceInteractive(true);
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onStrongAuthStateChanged(int i3) {
                this.this$0.mLockIcon.update();
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onUserSwitchComplete(int i3) {
                this.this$0.updateCameraVisibility();
            }
        };
        this.mEmergencyButtonExt = KeyguardPluginFactory.getEmergencyButtonExt(context);
    }

    private boolean canLaunchVoiceAssist() {
        return this.mAssistManager.canVoiceAssistBeLaunchedFromKeyguard();
    }

    private Intent getCameraIntent() {
        return (!this.mLockPatternUtils.isSecure(KeyguardUpdateMonitor.getCurrentUser()) || KeyguardUpdateMonitor.getInstance(this.mContext).getUserCanSkipBouncer(KeyguardUpdateMonitor.getCurrentUser())) ? INSECURE_CAMERA_INTENT : SECURE_CAMERA_INTENT;
    }

    private void handleTrustCircleClick() {
        EventLogTags.writeSysuiLockscreenGesture(6, 0, 0);
        this.mIndicationController.showTransientIndication(2131493675);
        this.mLockPatternUtils.requireCredentialEntry(KeyguardUpdateMonitor.getCurrentUser());
    }

    private void inflateCameraPreview() {
        this.mCameraPreview = this.mPreviewInflater.inflatePreview(getCameraIntent());
        if (this.mCameraPreview != null) {
            this.mPreviewContainer.addView(this.mCameraPreview);
            this.mCameraPreview.setVisibility(4);
        }
    }

    private void initAccessibility() {
        this.mLockIcon.setAccessibilityDelegate(this.mAccessibilityDelegate);
        this.mLeftAffordanceView.setAccessibilityDelegate(this.mAccessibilityDelegate);
        this.mCameraImageView.setAccessibilityDelegate(this.mAccessibilityDelegate);
    }

    private boolean isPhoneVisible() {
        PackageManager packageManager = this.mContext.getPackageManager();
        boolean z = false;
        if (packageManager.hasSystemFeature("android.hardware.telephony")) {
            z = false;
            if (packageManager.resolveActivity(PHONE_INTENT, 0) != null) {
                z = true;
            }
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static boolean isSuccessfulLaunch(int i) {
        boolean z = true;
        if (i != 0) {
            if (i == 3) {
                z = true;
            } else {
                z = true;
                if (i != 2) {
                    z = false;
                }
            }
        }
        return z;
    }

    private void launchPhone() {
        TelecomManager from = TelecomManager.from(this.mContext);
        if (from.isInCall()) {
            AsyncTask.execute(new Runnable(this, from) { // from class: com.android.systemui.statusbar.phone.KeyguardBottomAreaView.8
                final KeyguardBottomAreaView this$0;
                final TelecomManager val$tm;

                {
                    this.this$0 = this;
                    this.val$tm = from;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.val$tm.showInCallScreen(false);
                }
            });
        } else {
            this.mActivityStarter.startActivity(PHONE_INTENT, false);
        }
    }

    private void launchVoiceAssist() {
        Runnable runnable = new Runnable(this) { // from class: com.android.systemui.statusbar.phone.KeyguardBottomAreaView.7
            final KeyguardBottomAreaView this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mAssistManager.launchVoiceAssistFromKeyguard();
                this.this$0.mActivityStarter.preventNextAnimation();
            }
        };
        if (this.mPhoneStatusBar.isKeyguardCurrentlySecure()) {
            AsyncTask.execute(runnable);
        } else {
            this.mPhoneStatusBar.executeRunnableDismissingKeyguard(runnable, null, false, false, true);
        }
    }

    private void startFinishDozeAnimationElement(View view, long j) {
        view.setAlpha(0.0f);
        view.setTranslationY(view.getHeight() / 2);
        view.animate().alpha(1.0f).translationY(0.0f).setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN).setStartDelay(j).setDuration(250L);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateCameraVisibility() {
        if (this.mCameraImageView == null) {
            return;
        }
        this.mCameraImageView.setVisibility(((this.mPhoneStatusBar != null && !this.mPhoneStatusBar.isCameraAllowedByAdmin()) || resolveCameraIntent() == null || !getResources().getBoolean(2131623950)) ? false : this.mUserSetupComplete ? 0 : 8);
    }

    private void updateLeftAffordanceIcon() {
        int i;
        int i2;
        this.mLeftIsVoiceAssist = canLaunchVoiceAssist();
        boolean z = this.mUserSetupComplete;
        if (this.mLeftIsVoiceAssist) {
            i = 2130837684;
            i2 = 2131493354;
        } else {
            z &= isPhoneVisible();
            i = 2130837691;
            i2 = 2131493353;
        }
        this.mLeftAffordanceView.setVisibility(z ? 0 : 8);
        this.mLeftAffordanceView.setImageDrawable(this.mContext.getDrawable(i));
        this.mLeftAffordanceView.setContentDescription(this.mContext.getString(i2));
    }

    private void updateLeftPreview() {
        View view = this.mLeftPreview;
        if (view != null) {
            this.mPreviewContainer.removeView(view);
        }
        if (this.mLeftIsVoiceAssist) {
            this.mLeftPreview = this.mPreviewInflater.inflatePreviewFromService(this.mAssistManager.getVoiceInteractorComponentName());
        } else {
            this.mLeftPreview = this.mPreviewInflater.inflatePreview(PHONE_INTENT);
        }
        if (this.mLeftPreview != null) {
            this.mPreviewContainer.addView(this.mLeftPreview);
            this.mLeftPreview.setVisibility(4);
        }
    }

    private void watchForCameraPolicyChanges() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED");
        getContext().registerReceiverAsUser(this.mDevicePolicyReceiver, UserHandle.ALL, intentFilter, null, null);
        KeyguardUpdateMonitor.getInstance(this.mContext).registerCallback(this.mUpdateMonitorCallback);
    }

    public void bindCameraPrewarmService() {
        String string;
        ActivityInfo targetActivityInfo = PreviewInflater.getTargetActivityInfo(this.mContext, getCameraIntent(), KeyguardUpdateMonitor.getCurrentUser());
        if (targetActivityInfo == null || targetActivityInfo.metaData == null || (string = targetActivityInfo.metaData.getString("android.media.still_image_camera_preview_service")) == null) {
            return;
        }
        Intent intent = new Intent();
        intent.setClassName(targetActivityInfo.packageName, string);
        intent.setAction("android.service.media.CameraPrewarmService.ACTION_PREWARM");
        try {
            if (getContext().bindServiceAsUser(intent, this.mPrewarmConnection, 67108865, new UserHandle(-2))) {
                this.mPrewarmBound = true;
            }
        } catch (SecurityException e) {
            Log.w("PhoneStatusBar/KeyguardBottomAreaView", "Unable to bind to prewarm service package=" + targetActivityInfo.packageName + " class=" + string, e);
        }
    }

    public View getIndicationView() {
        return this.mIndicationText;
    }

    public View getLeftPreview() {
        return this.mLeftPreview;
    }

    public KeyguardAffordanceView getLeftView() {
        return this.mLeftAffordanceView;
    }

    public LockIcon getLockIcon() {
        return this.mLockIcon;
    }

    public View getRightPreview() {
        return this.mCameraPreview;
    }

    public KeyguardAffordanceView getRightView() {
        return this.mCameraImageView;
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    public boolean isLeftVoiceAssist() {
        return this.mLeftIsVoiceAssist;
    }

    public void launchCamera(String str) {
        Intent cameraIntent = getCameraIntent();
        cameraIntent.putExtra("com.android.systemui.camera_launch_source", str);
        boolean wouldLaunchResolverActivity = PreviewInflater.wouldLaunchResolverActivity(this.mContext, cameraIntent, KeyguardUpdateMonitor.getCurrentUser());
        if (cameraIntent != SECURE_CAMERA_INTENT || wouldLaunchResolverActivity) {
            this.mActivityStarter.startActivity(cameraIntent, false, new ActivityStarter.Callback(this) { // from class: com.android.systemui.statusbar.phone.KeyguardBottomAreaView.6
                final KeyguardBottomAreaView this$0;

                {
                    this.this$0 = this;
                }

                @Override // com.android.systemui.statusbar.phone.ActivityStarter.Callback
                public void onActivityStarted(int i) {
                    this.this$0.unbindCameraPrewarmService(KeyguardBottomAreaView.isSuccessfulLaunch(i));
                }
            });
        } else {
            AsyncTask.execute(new AnonymousClass5(this, cameraIntent));
        }
    }

    public void launchLeftAffordance() {
        if (this.mLeftIsVoiceAssist) {
            launchVoiceAssist();
        } else {
            launchPhone();
        }
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (view == this.mCameraImageView) {
            launchCamera("lockscreen_affordance");
        } else if (view == this.mLeftAffordanceView) {
            launchLeftAffordance();
        }
        if (view == this.mLockIcon) {
            if (this.mAccessibilityController.isAccessibilityEnabled()) {
                this.mPhoneStatusBar.animateCollapsePanels(0, true);
            } else {
                handleTrustCircleClick();
            }
        }
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        getResources().getDimensionPixelSize(2131689937);
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mIndicationText.getLayoutParams();
        this.mIndicationText.setTextSize(0, getResources().getDimensionPixelSize(17105168));
        getRightView().setContentDescription(getResources().getString(2131493352));
        getLockIcon().setContentDescription(getResources().getString(2131493355));
        getLeftView().setContentDescription(getResources().getString(2131493353));
        ViewGroup.LayoutParams layoutParams = this.mCameraImageView.getLayoutParams();
        layoutParams.width = getResources().getDimensionPixelSize(2131689934);
        layoutParams.height = getResources().getDimensionPixelSize(2131689933);
        this.mCameraImageView.setLayoutParams(layoutParams);
        this.mCameraImageView.setImageDrawable(this.mContext.getDrawable(2130837631));
        ViewGroup.LayoutParams layoutParams2 = this.mLockIcon.getLayoutParams();
        layoutParams2.width = getResources().getDimensionPixelSize(2131689934);
        layoutParams2.height = getResources().getDimensionPixelSize(2131689933);
        this.mLockIcon.setLayoutParams(layoutParams2);
        this.mLockIcon.update(true);
        ViewGroup.LayoutParams layoutParams3 = this.mLeftAffordanceView.getLayoutParams();
        layoutParams3.width = getResources().getDimensionPixelSize(2131689934);
        layoutParams3.height = getResources().getDimensionPixelSize(2131689933);
        this.mLeftAffordanceView.setLayoutParams(layoutParams3);
        updateLeftAffordanceIcon();
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mLockPatternUtils = new LockPatternUtils(this.mContext);
        this.mPreviewContainer = (ViewGroup) findViewById(2131886294);
        this.mCameraImageView = (KeyguardAffordanceView) findViewById(2131886295);
        this.mLeftAffordanceView = (KeyguardAffordanceView) findViewById(2131886296);
        this.mLockIcon = (LockIcon) findViewById(2131886297);
        this.mIndicationText = (TextView) findViewById(2131886292);
        this.mEmergencyButton = (EmergencyButton) findViewById(2131886293);
        watchForCameraPolicyChanges();
        updateCameraVisibility();
        this.mUnlockMethodCache = UnlockMethodCache.getInstance(getContext());
        this.mUnlockMethodCache.addListener(this);
        this.mLockIcon.update();
        setClipChildren(false);
        setClipToPadding(false);
        this.mPreviewInflater = new PreviewInflater(this.mContext, new LockPatternUtils(this.mContext));
        inflateCameraPreview();
        this.mLockIcon.setOnClickListener(this);
        this.mLockIcon.setOnLongClickListener(this);
        this.mCameraImageView.setOnClickListener(this);
        this.mLeftAffordanceView.setOnClickListener(this);
        initAccessibility();
    }

    @Override // android.view.View.OnLongClickListener
    public boolean onLongClick(View view) {
        handleTrustCircleClick();
        return true;
    }

    @Override // com.android.systemui.statusbar.policy.AccessibilityController.AccessibilityStateChangedCallback
    public void onStateChanged(boolean z, boolean z2) {
        this.mCameraImageView.setClickable(z2);
        this.mLeftAffordanceView.setClickable(z2);
        this.mCameraImageView.setFocusable(z);
        this.mLeftAffordanceView.setFocusable(z);
        this.mLockIcon.update();
    }

    @Override // com.android.systemui.statusbar.phone.UnlockMethodCache.OnUnlockMethodChangedListener
    public void onUnlockMethodStateChanged() {
        this.mLockIcon.update();
        updateCameraVisibility();
    }

    @Override // android.view.View
    protected void onVisibilityChanged(View view, int i) {
        super.onVisibilityChanged(view, i);
        if (view == this && i == 0) {
            this.mLockIcon.update();
            updateCameraVisibility();
        }
    }

    public ResolveInfo resolveCameraIntent() {
        return this.mContext.getPackageManager().resolveActivityAsUser(getCameraIntent(), 65536, KeyguardUpdateMonitor.getCurrentUser());
    }

    public void setAccessibilityController(AccessibilityController accessibilityController) {
        this.mAccessibilityController = accessibilityController;
        this.mLockIcon.setAccessibilityController(accessibilityController);
        accessibilityController.addStateChangedCallback(this);
    }

    public void setActivityStarter(ActivityStarter activityStarter) {
        this.mActivityStarter = activityStarter;
    }

    @Override // android.view.View
    public void setAlpha(float f) {
        super.setAlpha(f);
        this.mEmergencyButtonExt.setEmergencyButtonVisibility(this.mEmergencyButton, f);
    }

    public void setAssistManager(AssistManager assistManager) {
        this.mAssistManager = assistManager;
        updateLeftAffordance();
    }

    public void setFlashlightController(FlashlightController flashlightController) {
        this.mFlashlightController = flashlightController;
    }

    public void setKeyguardIndicationController(KeyguardIndicationController keyguardIndicationController) {
        this.mIndicationController = keyguardIndicationController;
    }

    public void setPhoneStatusBar(PhoneStatusBar phoneStatusBar) {
        this.mPhoneStatusBar = phoneStatusBar;
        updateCameraVisibility();
    }

    public void setUserSetupComplete(boolean z) {
        this.mUserSetupComplete = z;
        updateCameraVisibility();
        updateLeftAffordanceIcon();
    }

    public void startFinishDozeAnimation() {
        long j = 0;
        if (this.mLeftAffordanceView.getVisibility() == 0) {
            startFinishDozeAnimationElement(this.mLeftAffordanceView, 0L);
            j = 48;
        }
        startFinishDozeAnimationElement(this.mLockIcon, j);
        if (this.mCameraImageView.getVisibility() == 0) {
            startFinishDozeAnimationElement(this.mCameraImageView, j + 48);
        }
        this.mIndicationText.setAlpha(0.0f);
        this.mIndicationText.animate().alpha(1.0f).setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN).setDuration(700L);
    }

    public void unbindCameraPrewarmService(boolean z) {
        if (this.mPrewarmBound) {
            if (this.mPrewarmMessenger != null && z) {
                try {
                    this.mPrewarmMessenger.send(Message.obtain((Handler) null, 1));
                } catch (RemoteException e) {
                    Log.w("PhoneStatusBar/KeyguardBottomAreaView", "Error sending camera fired message", e);
                }
            }
            this.mContext.unbindService(this.mPrewarmConnection);
            this.mPrewarmBound = false;
        }
    }

    public void updateLeftAffordance() {
        updateLeftAffordanceIcon();
        updateLeftPreview();
    }
}
