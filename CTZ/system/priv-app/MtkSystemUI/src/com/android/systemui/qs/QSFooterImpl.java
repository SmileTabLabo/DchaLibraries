package com.android.systemui.qs;

import android.content.Context;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.BenesseExtension;
import android.os.Bundle;
import android.os.UserManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.android.internal.logging.MetricsLogger;
import com.android.keyguard.CarrierText;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.settingslib.Utils;
import com.android.settingslib.drawable.UserIconDrawable;
import com.android.settingslib.graph.SignalDrawable;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.qs.TouchAnimator;
import com.android.systemui.statusbar.phone.MultiUserSwitch;
import com.android.systemui.statusbar.phone.SettingsButton;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.tuner.TunerService;
import com.mediatek.systemui.ext.ISystemUIStatusBarExt;
import com.mediatek.systemui.ext.OpSystemUICustomizationFactoryBase;
/* loaded from: classes.dex */
public class QSFooterImpl extends FrameLayout implements View.OnClickListener, QSFooter, NetworkController.EmergencyListener, NetworkController.SignalCallback, UserInfoController.OnUserInfoChangedListener {
    private View mActionsContainer;
    private ActivityStarter mActivityStarter;
    private CarrierText mCarrierText;
    private final int mColorForeground;
    private View mDivider;
    private View mDragHandle;
    protected View mEdit;
    private View.OnClickListener mExpandClickListener;
    private boolean mExpanded;
    private float mExpansionAmount;
    protected TouchAnimator mFooterAnimator;
    private final CellSignalState mInfo;
    private boolean mListening;
    private View mMobileGroup;
    private ImageView mMobileRoaming;
    private ImageView mMobileSignal;
    private ImageView mMultiUserAvatar;
    protected MultiUserSwitch mMultiUserSwitch;
    private PageIndicator mPageIndicator;
    private boolean mQsDisabled;
    private QSPanel mQsPanel;
    private SettingsButton mSettingsButton;
    private TouchAnimator mSettingsCogAnimator;
    protected View mSettingsContainer;
    private boolean mShowEmergencyCallsOnly;
    private ISystemUIStatusBarExt mStatusBarExt;
    private UserInfoController mUserInfoController;

    public QSFooterImpl(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mInfo = new CellSignalState();
        this.mColorForeground = Utils.getColorAttr(context, 16842800);
        this.mStatusBarExt = OpSystemUICustomizationFactoryBase.getOpFactory(this.mContext).makeSystemUIStatusBar(this.mContext);
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mDivider = findViewById(R.id.qs_footer_divider);
        this.mEdit = findViewById(16908291);
        this.mEdit.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.qs.-$$Lambda$QSFooterImpl$3QBg0cgvu2IRpUDq3RvpL257x8c
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                ((ActivityStarter) Dependency.get(ActivityStarter.class)).postQSRunnableDismissingKeyguard(new Runnable() { // from class: com.android.systemui.qs.-$$Lambda$QSFooterImpl$BPGtDaa2eU-tTCTVDpjGrKOXYOs
                    @Override // java.lang.Runnable
                    public final void run() {
                        QSFooterImpl.this.mQsPanel.showEdit(view);
                    }
                });
            }
        });
        this.mPageIndicator = (PageIndicator) findViewById(R.id.footer_page_indicator);
        this.mSettingsButton = (SettingsButton) findViewById(R.id.settings_button);
        this.mSettingsContainer = findViewById(R.id.settings_button_container);
        this.mSettingsButton.setOnClickListener(this);
        this.mMobileGroup = findViewById(R.id.mobile_combo);
        this.mMobileSignal = (ImageView) findViewById(R.id.mobile_signal);
        this.mMobileRoaming = (ImageView) findViewById(R.id.mobile_roaming);
        this.mCarrierText = (CarrierText) findViewById(R.id.qs_carrier_text);
        this.mCarrierText.setDisplayFlags(3);
        this.mMultiUserSwitch = (MultiUserSwitch) findViewById(R.id.multi_user_switch);
        this.mMultiUserAvatar = (ImageView) this.mMultiUserSwitch.findViewById(R.id.multi_user_avatar);
        this.mDragHandle = findViewById(R.id.qs_drag_handle_view);
        this.mActionsContainer = findViewById(R.id.qs_footer_actions_container);
        ((RippleDrawable) this.mSettingsButton.getBackground()).setForceSoftware(true);
        updateResources();
        this.mUserInfoController = (UserInfoController) Dependency.get(UserInfoController.class);
        this.mActivityStarter = (ActivityStarter) Dependency.get(ActivityStarter.class);
        addOnLayoutChangeListener(new View.OnLayoutChangeListener() { // from class: com.android.systemui.qs.-$$Lambda$QSFooterImpl$GSAG9gEF755NpvH4khVvAa75uPs
            @Override // android.view.View.OnLayoutChangeListener
            public final void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                QSFooterImpl.this.updateAnimator(i3 - i);
            }
        });
        setImportantForAccessibility(1);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateAnimator(int i) {
        int numQuickTiles = QuickQSPanel.getNumQuickTiles(this.mContext);
        int dimensionPixelSize = (i - ((this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_quick_tile_size) - this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_quick_tile_padding)) * numQuickTiles)) / (numQuickTiles - 1);
        int dimensionPixelOffset = this.mContext.getResources().getDimensionPixelOffset(R.dimen.default_gear_space);
        TouchAnimator.Builder builder = new TouchAnimator.Builder();
        View view = this.mSettingsContainer;
        float[] fArr = new float[2];
        fArr[0] = isLayoutRtl() ? dimensionPixelSize - dimensionPixelOffset : -(dimensionPixelSize - dimensionPixelOffset);
        fArr[1] = 0.0f;
        this.mSettingsCogAnimator = builder.addFloat(view, "translationX", fArr).addFloat(this.mSettingsButton, "rotation", -120.0f, 0.0f).build();
        setExpansion(this.mExpansionAmount);
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        updateResources();
    }

    @Override // android.view.View
    public void onRtlPropertiesChanged(int i) {
        super.onRtlPropertiesChanged(i);
        updateResources();
    }

    private void updateResources() {
        updateFooterAnimator();
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mActionsContainer.getLayoutParams();
        layoutParams.width = this.mContext.getResources().getInteger(R.integer.qs_footer_actions_width);
        layoutParams.weight = this.mContext.getResources().getInteger(R.integer.qs_footer_actions_weight);
        this.mActionsContainer.setLayoutParams(layoutParams);
    }

    private void updateFooterAnimator() {
        this.mFooterAnimator = createFooterAnimator();
    }

    private TouchAnimator createFooterAnimator() {
        return new TouchAnimator.Builder().addFloat(this.mDivider, "alpha", 0.0f, 1.0f).addFloat(this.mCarrierText, "alpha", 0.0f, 0.0f, 1.0f).addFloat(this.mMobileGroup, "alpha", 0.0f, 1.0f).addFloat(this.mActionsContainer, "alpha", 0.0f, 1.0f).addFloat(this.mDragHandle, "alpha", 1.0f, 0.0f, 0.0f).addFloat(this.mPageIndicator, "alpha", 0.0f, 1.0f).setStartDelay(0.15f).build();
    }

    @Override // com.android.systemui.qs.QSFooter
    public void setKeyguardShowing(boolean z) {
        setExpansion(this.mExpansionAmount);
    }

    @Override // com.android.systemui.qs.QSFooter
    public void setExpandClickListener(View.OnClickListener onClickListener) {
        this.mExpandClickListener = onClickListener;
    }

    @Override // com.android.systemui.qs.QSFooter
    public void setExpanded(boolean z) {
        if (this.mExpanded == z) {
            return;
        }
        this.mExpanded = z;
        updateEverything();
    }

    @Override // com.android.systemui.qs.QSFooter
    public void setExpansion(float f) {
        this.mExpansionAmount = f;
        if (this.mSettingsCogAnimator != null) {
            this.mSettingsCogAnimator.setPosition(f);
        }
        if (this.mFooterAnimator != null) {
            this.mFooterAnimator.setPosition(f);
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onDetachedFromWindow() {
        setListening(false);
        super.onDetachedFromWindow();
    }

    @Override // com.android.systemui.qs.QSFooter
    public void setListening(boolean z) {
        if (z == this.mListening) {
            return;
        }
        this.mListening = z;
        updateListeners();
    }

    @Override // android.view.View
    public boolean performAccessibilityAction(int i, Bundle bundle) {
        if (i == 262144 && this.mExpandClickListener != null) {
            this.mExpandClickListener.onClick(null);
            return true;
        }
        return super.performAccessibilityAction(i, bundle);
    }

    @Override // android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_EXPAND);
    }

    @Override // com.android.systemui.qs.QSFooter
    public void disable(int i, int i2, boolean z) {
        boolean z2 = (i2 & 1) != 0;
        if (z2 == this.mQsDisabled) {
            return;
        }
        this.mQsDisabled = z2;
        updateEverything();
    }

    public void updateEverything() {
        post(new Runnable() { // from class: com.android.systemui.qs.-$$Lambda$QSFooterImpl$FK1In3z-Y3ppRrcllMggnruYa_s
            @Override // java.lang.Runnable
            public final void run() {
                QSFooterImpl.lambda$updateEverything$3(QSFooterImpl.this);
            }
        });
    }

    public static /* synthetic */ void lambda$updateEverything$3(QSFooterImpl qSFooterImpl) {
        qSFooterImpl.updateVisibilities();
        qSFooterImpl.setClickable(false);
    }

    private void updateVisibilities() {
        int i = 0;
        this.mSettingsContainer.setVisibility(this.mQsDisabled ? 8 : 0);
        this.mSettingsContainer.findViewById(R.id.tuner_icon).setVisibility(TunerService.isTunerEnabled(this.mContext) ? 0 : 4);
        boolean isDeviceInDemoMode = UserManager.isDeviceInDemoMode(this.mContext);
        this.mMultiUserSwitch.setVisibility(showUserSwitcher(isDeviceInDemoMode) ? 0 : 4);
        this.mEdit.setVisibility((isDeviceInDemoMode || !this.mExpanded) ? 4 : 0);
        SettingsButton settingsButton = this.mSettingsButton;
        if (isDeviceInDemoMode || !this.mExpanded) {
            i = 4;
        }
        settingsButton.setVisibility(i);
    }

    private boolean showUserSwitcher(boolean z) {
        int i = 0;
        if (this.mExpanded && !z && UserManager.supportsMultipleUsers()) {
            UserManager userManager = UserManager.get(this.mContext);
            if (userManager.hasUserRestriction("no_user_switch")) {
                return false;
            }
            for (UserInfo userInfo : userManager.getUsers(true)) {
                if (userInfo.supportsSwitchToByUser() && (i = i + 1) > 1) {
                    return true;
                }
            }
            return getResources().getBoolean(R.bool.qs_show_user_switcher_for_single_user);
        }
        return false;
    }

    private void updateListeners() {
        if (this.mListening) {
            this.mUserInfoController.addCallback(this);
            if (((NetworkController) Dependency.get(NetworkController.class)).hasVoiceCallingFeature()) {
                ((NetworkController) Dependency.get(NetworkController.class)).addEmergencyListener(this);
                ((NetworkController) Dependency.get(NetworkController.class)).addCallback((NetworkController.SignalCallback) this);
                return;
            }
            return;
        }
        this.mUserInfoController.removeCallback(this);
        ((NetworkController) Dependency.get(NetworkController.class)).removeEmergencyListener(this);
        ((NetworkController) Dependency.get(NetworkController.class)).removeCallback((NetworkController.SignalCallback) this);
    }

    @Override // com.android.systemui.qs.QSFooter
    public void setQSPanel(QSPanel qSPanel) {
        this.mQsPanel = qSPanel;
        if (this.mQsPanel != null) {
            this.mMultiUserSwitch.setQsPanel(qSPanel);
            this.mQsPanel.setFooterPageIndicator(this.mPageIndicator);
        }
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (this.mExpanded && view == this.mSettingsButton) {
            if (!((DeviceProvisionedController) Dependency.get(DeviceProvisionedController.class)).isCurrentUserSetup()) {
                this.mActivityStarter.postQSRunnableDismissingKeyguard(new Runnable() { // from class: com.android.systemui.qs.-$$Lambda$QSFooterImpl$ORlOcuwnOcEc1bdhJcTagEFJfI4
                    @Override // java.lang.Runnable
                    public final void run() {
                        QSFooterImpl.lambda$onClick$4();
                    }
                });
                return;
            }
            MetricsLogger.action(this.mContext, this.mExpanded ? 406 : 490);
            if (this.mSettingsButton.isTunerClick()) {
                ((ActivityStarter) Dependency.get(ActivityStarter.class)).postQSRunnableDismissingKeyguard(new Runnable() { // from class: com.android.systemui.qs.-$$Lambda$QSFooterImpl$QqFCwKmpQEaqoIsbaA3_odDeJWo
                    @Override // java.lang.Runnable
                    public final void run() {
                        QSFooterImpl.lambda$onClick$6(QSFooterImpl.this);
                    }
                });
            } else {
                startSettingsActivity();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ void lambda$onClick$4() {
    }

    public static /* synthetic */ void lambda$onClick$6(final QSFooterImpl qSFooterImpl) {
        if (TunerService.isTunerEnabled(qSFooterImpl.mContext)) {
            TunerService.showResetRequest(qSFooterImpl.mContext, new Runnable() { // from class: com.android.systemui.qs.-$$Lambda$QSFooterImpl$p6Eelc3uV5Rv_Va6Mn0QpjivHN4
                @Override // java.lang.Runnable
                public final void run() {
                    QSFooterImpl.this.startSettingsActivity();
                }
            });
        } else {
            Toast.makeText(qSFooterImpl.getContext(), (int) R.string.tuner_toast, 1).show();
            TunerService.setTunerEnabled(qSFooterImpl.mContext, true);
        }
        qSFooterImpl.startSettingsActivity();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startSettingsActivity() {
        if (BenesseExtension.getDchaState() != 0) {
            return;
        }
        this.mActivityStarter.startActivity(new Intent("android.settings.SETTINGS"), true);
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.EmergencyListener
    public void setEmergencyCallsOnly(boolean z) {
        if (z != this.mShowEmergencyCallsOnly) {
            this.mShowEmergencyCallsOnly = z;
            if (this.mExpanded) {
                updateEverything();
            }
        }
    }

    @Override // com.android.systemui.statusbar.policy.UserInfoController.OnUserInfoChangedListener
    public void onUserInfoChanged(String str, Drawable drawable, String str2) {
        if (drawable != null && UserManager.get(this.mContext).isGuestUser(KeyguardUpdateMonitor.getCurrentUser()) && !(drawable instanceof UserIconDrawable)) {
            drawable = drawable.getConstantState().newDrawable(this.mContext.getResources()).mutate();
            drawable.setColorFilter(Utils.getColorAttr(this.mContext, 16842800), PorterDuff.Mode.SRC_IN);
        }
        this.mMultiUserAvatar.setImageDrawable(drawable);
    }

    private void handleUpdateState() {
        this.mMobileGroup.setVisibility(this.mInfo.visible ? 0 : 8);
        if (this.mInfo.visible) {
            this.mMobileRoaming.setVisibility(this.mInfo.roaming ? 0 : 8);
            this.mMobileRoaming.setImageTintList(ColorStateList.valueOf(this.mColorForeground));
            SignalDrawable signalDrawable = new SignalDrawable(this.mContext);
            signalDrawable.setDarkIntensity(QuickStatusBarHeader.getColorIntensity(this.mColorForeground));
            this.mMobileSignal.setImageDrawable(signalDrawable);
            this.mMobileSignal.setImageLevel(this.mStatusBarExt.getCommonSignalIconId(this.mInfo.mobileSignalIconId));
            StringBuilder sb = new StringBuilder();
            if (this.mInfo.contentDescription != null) {
                sb.append(this.mInfo.contentDescription);
                sb.append(", ");
            }
            if (this.mInfo.roaming) {
                sb.append(this.mContext.getString(R.string.data_connection_roaming));
                sb.append(", ");
            }
            if (TextUtils.equals(this.mInfo.typeContentDescription, this.mContext.getString(R.string.data_connection_no_internet)) || TextUtils.equals(this.mInfo.typeContentDescription, this.mContext.getString(R.string.cell_data_off_content_description))) {
                sb.append(this.mInfo.typeContentDescription);
            }
            this.mMobileSignal.setContentDescription(sb);
        }
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setMobileDataIndicators(NetworkController.IconState iconState, NetworkController.IconState iconState2, int i, int i2, int i3, int i4, boolean z, boolean z2, String str, String str2, boolean z3, int i5, boolean z4, boolean z5) {
        this.mInfo.visible = iconState.visible;
        this.mInfo.mobileSignalIconId = iconState.icon;
        this.mInfo.contentDescription = iconState.contentDescription;
        this.mInfo.typeContentDescription = str;
        this.mInfo.roaming = z4;
        handleUpdateState();
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setNoSims(boolean z, boolean z2) {
        if (z) {
            this.mInfo.visible = false;
        }
        handleUpdateState();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public final class CellSignalState {
        public String contentDescription;
        int mobileSignalIconId;
        boolean roaming;
        String typeContentDescription;
        boolean visible;

        private CellSignalState() {
        }
    }
}
