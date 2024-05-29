package com.android.systemui.statusbar.phone;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.BenesseExtension;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.logging.MetricsLogger;
import com.android.keyguard.KeyguardStatusView;
import com.android.keyguard.R$id;
import com.android.systemui.FontSizeUtils;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.qs.QuickQSPanel;
import com.android.systemui.qs.TouchAnimator;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.NextAlarmController;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.tuner.TunerService;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/QuickStatusBarHeader.class */
public class QuickStatusBarHeader extends BaseStatusBarHeader implements NextAlarmController.NextAlarmChangeCallback, View.OnClickListener, UserInfoController.OnUserInfoChangedListener {
    private ActivityStarter mActivityStarter;
    private boolean mAlarmShowing;
    private TextView mAlarmStatus;
    private View mAlarmStatusCollapsed;
    private TouchAnimator mAlarmTranslation;
    private float mDateScaleFactor;
    private TouchAnimator mDateSizeAnimator;
    private ViewGroup mDateTimeAlarmGroup;
    private float mDateTimeAlarmTranslation;
    private ViewGroup mDateTimeGroup;
    private float mDateTimeTranslation;
    private TextView mEmergencyOnly;
    protected ExpandableIndicator mExpandIndicator;
    private boolean mExpanded;
    private float mExpansionAmount;
    private TouchAnimator mFirstHalfAnimator;
    protected float mGearTranslation;
    private QuickQSPanel mHeaderQsPanel;
    private QSTileHost mHost;
    private boolean mListening;
    private ImageView mMultiUserAvatar;
    protected MultiUserSwitch mMultiUserSwitch;
    private AlarmManager.AlarmClockInfo mNextAlarm;
    private NextAlarmController mNextAlarmController;
    private QSPanel mQsPanel;
    private TouchAnimator mSecondHalfAnimator;
    protected TouchAnimator mSettingsAlpha;
    private SettingsButton mSettingsButton;
    protected View mSettingsContainer;
    private boolean mShowEmergencyCallsOnly;
    private boolean mShowFullAlarm;

    public QuickStatusBarHeader(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: startSettingsActivity */
    public void m1762x4530d584() {
        if (BenesseExtension.getDchaState() != 0) {
            return;
        }
        this.mActivityStarter.startActivity(new Intent("android.settings.SETTINGS"), true);
    }

    private void updateAlarmVisibilities() {
        this.mAlarmStatus.setVisibility((this.mAlarmShowing && this.mShowFullAlarm) ? 0 : 4);
        this.mAlarmStatusCollapsed.setVisibility(this.mAlarmShowing ? 0 : 4);
    }

    private void updateDateTimePosition() {
        this.mAlarmTranslation = new TouchAnimator.Builder().addFloat(this.mDateTimeAlarmGroup, "translationY", 0.0f, this.mAlarmShowing ? this.mDateTimeAlarmTranslation : this.mDateTimeTranslation).build();
        this.mAlarmTranslation.setPosition(this.mExpansionAmount);
    }

    private void updateListeners() {
        if (this.mListening) {
            this.mNextAlarmController.addStateChangedCallback(this);
        } else {
            this.mNextAlarmController.removeStateChangedCallback(this);
        }
    }

    private void updateResources() {
        FontSizeUtils.updateFontSize(this.mAlarmStatus, 2131689917);
        FontSizeUtils.updateFontSize(this.mEmergencyOnly, 2131689916);
        this.mGearTranslation = this.mContext.getResources().getDimension(2131689832);
        this.mDateTimeTranslation = this.mContext.getResources().getDimension(2131689828);
        this.mDateTimeAlarmTranslation = this.mContext.getResources().getDimension(2131689829);
        this.mDateScaleFactor = this.mContext.getResources().getDimension(2131689831) / this.mContext.getResources().getDimension(2131689830);
        updateDateTimePosition();
        this.mSecondHalfAnimator = new TouchAnimator.Builder().addFloat(this.mShowFullAlarm ? this.mAlarmStatus : findViewById(2131886680), "alpha", 0.0f, 1.0f).addFloat(this.mEmergencyOnly, "alpha", 0.0f, 1.0f).setStartDelay(0.5f).build();
        if (this.mShowFullAlarm) {
            this.mFirstHalfAnimator = new TouchAnimator.Builder().addFloat(this.mAlarmStatusCollapsed, "alpha", 1.0f, 0.0f).setEndDelay(0.5f).build();
        }
        this.mDateSizeAnimator = new TouchAnimator.Builder().addFloat(this.mDateTimeGroup, "scaleX", 1.0f, this.mDateScaleFactor).addFloat(this.mDateTimeGroup, "scaleY", 1.0f, this.mDateScaleFactor).setStartDelay(0.36f).build();
        updateSettingsAnimator();
    }

    /* renamed from: -com_android_systemui_statusbar_phone_QuickStatusBarHeader_lambda$1  reason: not valid java name */
    /* synthetic */ void m1760x4530d582() {
        post(new Runnable(this) { // from class: com.android.systemui.statusbar.phone.QuickStatusBarHeader$_void__com_android_systemui_statusbar_phone_QuickStatusBarHeader_lambda$1__LambdaImpl0
            private QuickStatusBarHeader val$this;

            {
                this.val$this = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.val$this.m1761x4530d583();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: -com_android_systemui_statusbar_phone_QuickStatusBarHeader_lambda$2  reason: not valid java name */
    public /* synthetic */ void m1761x4530d583() {
        if (TunerService.isTunerEnabled(this.mContext)) {
            TunerService.showResetRequest(this.mContext, new Runnable(this) { // from class: com.android.systemui.statusbar.phone.QuickStatusBarHeader$_void__com_android_systemui_statusbar_phone_QuickStatusBarHeader_lambda$2__LambdaImpl0
                private QuickStatusBarHeader val$this;

                {
                    this.val$this = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.val$this.m1762x4530d584();
                }
            });
        } else {
            Toast.makeText(getContext(), 2131493735, 1).show();
            TunerService.setTunerEnabled(this.mContext, true);
        }
        m1762x4530d584();
    }

    @Override // com.android.systemui.statusbar.phone.BaseStatusBarHeader
    public int getCollapsedHeight() {
        return getHeight();
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (view == this.mSettingsButton) {
            MetricsLogger.action(this.mContext, 406);
            if (this.mSettingsButton.isTunerClick()) {
                this.mHost.startRunnableDismissingKeyguard(new Runnable(this) { // from class: com.android.systemui.statusbar.phone.QuickStatusBarHeader._void_onClick_android_view_View_v_LambdaImpl0
                    private QuickStatusBarHeader val$this;

                    {
                        this.val$this = this;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.val$this.m1760x4530d582();
                    }
                });
            } else {
                m1762x4530d584();
            }
        } else if (view != this.mAlarmStatus || this.mNextAlarm == null) {
        } else {
            this.mActivityStarter.startPendingIntentDismissingKeyguard(this.mNextAlarm.getShowIntent());
        }
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        updateResources();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        setListening(false);
        this.mHost.getUserInfoController().remListener(this);
        this.mHost.getNetworkController().removeEmergencyListener(this);
        super.onDetachedFromWindow();
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mEmergencyOnly = (TextView) findViewById(2131886600);
        this.mDateTimeAlarmGroup = (ViewGroup) findViewById(2131886601);
        this.mDateTimeAlarmGroup.findViewById(2131886673).setVisibility(8);
        this.mDateTimeGroup = (ViewGroup) findViewById(2131886603);
        this.mDateTimeGroup.setPivotX(0.0f);
        this.mDateTimeGroup.setPivotY(0.0f);
        this.mShowFullAlarm = getResources().getBoolean(2131623967);
        this.mExpandIndicator = (ExpandableIndicator) findViewById(2131886599);
        this.mHeaderQsPanel = (QuickQSPanel) findViewById(2131886602);
        this.mSettingsButton = (SettingsButton) findViewById(2131886597);
        this.mSettingsContainer = findViewById(2131886596);
        this.mSettingsButton.setOnClickListener(this);
        this.mAlarmStatusCollapsed = findViewById(2131886679);
        this.mAlarmStatus = (TextView) findViewById(R$id.alarm_status);
        this.mAlarmStatus.setOnClickListener(this);
        this.mMultiUserSwitch = (MultiUserSwitch) findViewById(2131886349);
        this.mMultiUserAvatar = (ImageView) this.mMultiUserSwitch.findViewById(2131886350);
        ((RippleDrawable) this.mSettingsButton.getBackground()).setForceSoftware(true);
        ((RippleDrawable) this.mExpandIndicator.getBackground()).setForceSoftware(true);
        updateResources();
    }

    @Override // com.android.systemui.statusbar.policy.NextAlarmController.NextAlarmChangeCallback
    public void onNextAlarmChanged(AlarmManager.AlarmClockInfo alarmClockInfo) {
        this.mNextAlarm = alarmClockInfo;
        if (alarmClockInfo != null) {
            String formatNextAlarm = KeyguardStatusView.formatNextAlarm(getContext(), alarmClockInfo);
            this.mAlarmStatus.setText(formatNextAlarm);
            this.mAlarmStatus.setContentDescription(this.mContext.getString(2131493477, formatNextAlarm));
            this.mAlarmStatusCollapsed.setContentDescription(this.mContext.getString(2131493477, formatNextAlarm));
        }
        if (this.mAlarmShowing != (alarmClockInfo != null)) {
            this.mAlarmShowing = alarmClockInfo != null;
            updateEverything();
        }
    }

    @Override // android.view.View
    public void onRtlPropertiesChanged(int i) {
        super.onRtlPropertiesChanged(i);
        updateResources();
    }

    @Override // com.android.systemui.statusbar.policy.UserInfoController.OnUserInfoChangedListener
    public void onUserInfoChanged(String str, Drawable drawable) {
        this.mMultiUserAvatar.setImageDrawable(drawable);
    }

    @Override // com.android.systemui.statusbar.phone.BaseStatusBarHeader
    public void setActivityStarter(ActivityStarter activityStarter) {
        this.mActivityStarter = activityStarter;
    }

    public void setBatteryController(BatteryController batteryController) {
    }

    @Override // com.android.systemui.statusbar.phone.BaseStatusBarHeader
    public void setCallback(QSPanel.Callback callback) {
        this.mHeaderQsPanel.setCallback(callback);
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

    @Override // com.android.systemui.statusbar.phone.BaseStatusBarHeader
    public void setExpanded(boolean z) {
        this.mExpanded = z;
        this.mHeaderQsPanel.setExpanded(z);
        updateEverything();
    }

    @Override // com.android.systemui.statusbar.phone.BaseStatusBarHeader
    public void setExpansion(float f) {
        this.mExpansionAmount = f;
        this.mSecondHalfAnimator.setPosition(f);
        if (this.mShowFullAlarm) {
            this.mFirstHalfAnimator.setPosition(f);
        }
        this.mDateSizeAnimator.setPosition(f);
        this.mAlarmTranslation.setPosition(f);
        this.mSettingsAlpha.setPosition(f);
        updateAlarmVisibilities();
        this.mExpandIndicator.setExpanded(f > 0.93f);
    }

    @Override // com.android.systemui.statusbar.phone.BaseStatusBarHeader
    public void setListening(boolean z) {
        if (z == this.mListening) {
            return;
        }
        this.mHeaderQsPanel.setListening(z);
        this.mListening = z;
        updateListeners();
    }

    public void setNextAlarmController(NextAlarmController nextAlarmController) {
        this.mNextAlarmController = nextAlarmController;
    }

    @Override // com.android.systemui.statusbar.phone.BaseStatusBarHeader
    public void setQSPanel(QSPanel qSPanel) {
        this.mQsPanel = qSPanel;
        setupHost(qSPanel.getHost());
        if (this.mQsPanel != null) {
            this.mMultiUserSwitch.setQsPanel(qSPanel);
        }
    }

    public void setUserInfoController(UserInfoController userInfoController) {
        userInfoController.addListener(this);
    }

    public void setupHost(QSTileHost qSTileHost) {
        this.mHost = qSTileHost;
        qSTileHost.setHeaderView(this.mExpandIndicator);
        this.mHeaderQsPanel.setQSPanelAndHeader(this.mQsPanel, this);
        this.mHeaderQsPanel.setHost(qSTileHost, null);
        setUserInfoController(qSTileHost.getUserInfoController());
        setBatteryController(qSTileHost.getBatteryController());
        setNextAlarmController(qSTileHost.getNextAlarmController());
        if (this.mHost.getNetworkController().hasVoiceCallingFeature()) {
            this.mHost.getNetworkController().addEmergencyListener(this);
        }
    }

    @Override // com.android.systemui.statusbar.phone.BaseStatusBarHeader
    public void updateEverything() {
        updateDateTimePosition();
        updateVisibilities();
        setClickable(false);
    }

    protected void updateSettingsAnimator() {
        int i = 0;
        this.mSettingsAlpha = new TouchAnimator.Builder().addFloat(this.mSettingsContainer, "translationY", -this.mGearTranslation, 0.0f).addFloat(this.mMultiUserSwitch, "translationY", -this.mGearTranslation, 0.0f).addFloat(this.mSettingsButton, "rotation", -90.0f, 0.0f).addFloat(this.mSettingsContainer, "alpha", 0.0f, 1.0f).addFloat(this.mMultiUserSwitch, "alpha", 0.0f, 1.0f).setStartDelay(0.7f).build();
        boolean isLayoutRtl = isLayoutRtl();
        if (isLayoutRtl && this.mDateTimeGroup.getWidth() == 0) {
            this.mDateTimeGroup.addOnLayoutChangeListener(new View.OnLayoutChangeListener(this) { // from class: com.android.systemui.statusbar.phone.QuickStatusBarHeader.1
                final QuickStatusBarHeader this$0;

                {
                    this.this$0 = this;
                }

                @Override // android.view.View.OnLayoutChangeListener
                public void onLayoutChange(View view, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9) {
                    this.this$0.mDateTimeGroup.setPivotX(this.this$0.getWidth());
                    this.this$0.mDateTimeGroup.removeOnLayoutChangeListener(this);
                }
            });
            return;
        }
        ViewGroup viewGroup = this.mDateTimeGroup;
        if (isLayoutRtl) {
            i = this.mDateTimeGroup.getWidth();
        }
        viewGroup.setPivotX(i);
    }

    protected void updateVisibilities() {
        updateAlarmVisibilities();
        this.mEmergencyOnly.setVisibility((this.mExpanded && this.mShowEmergencyCallsOnly) ? 0 : 4);
        this.mSettingsContainer.setVisibility(this.mExpanded ? 0 : 4);
        this.mSettingsContainer.findViewById(2131886598).setVisibility(TunerService.isTunerEnabled(this.mContext) ? 0 : 4);
        this.mMultiUserSwitch.setVisibility((this.mExpanded && this.mMultiUserSwitch.hasMultipleUsers()) ? 0 : 4);
    }
}
