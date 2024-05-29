package com.android.keyguard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActivityManager;
import android.app.IActivityManager;
import android.app.IStopUserCallback;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.support.v4.graphics.ColorUtils;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Slog;
import android.view.View;
import android.widget.GridLayout;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.ViewClippingUtil;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.util.wakelock.KeepAwakeAnimationListener;
import com.google.android.collect.Sets;
import java.util.Iterator;
import java.util.Locale;
/* loaded from: classes.dex */
public class KeyguardStatusView extends GridLayout implements View.OnLayoutChangeListener, ConfigurationController.ConfigurationListener {
    private static final boolean DEBUG = KeyguardConstants.DEBUG;
    private View mClockSeparator;
    private TextClock mClockView;
    private float mDarkAmount;
    private Handler mHandler;
    private final IActivityManager mIActivityManager;
    private KeyguardUpdateMonitorCallback mInfoCallback;
    private KeyguardSliceView mKeyguardSlice;
    private int mLastLayoutHeight;
    private final LockPatternUtils mLockPatternUtils;
    private TextView mLogoutView;
    private TextView mOwnerInfo;
    private Runnable mPendingMarqueeStart;
    private boolean mPulsing;
    private final float mSmallClockScale;
    private int mTextColor;
    private ArraySet<View> mVisibleInDoze;
    private float mWidgetPadding;

    public KeyguardStatusView(Context context) {
        this(context, null, 0);
    }

    public KeyguardStatusView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public KeyguardStatusView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mDarkAmount = 0.0f;
        this.mInfoCallback = new KeyguardUpdateMonitorCallback() { // from class: com.android.keyguard.KeyguardStatusView.1
            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onTimeChanged() {
                KeyguardStatusView.this.refreshTime();
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onKeyguardVisibilityChanged(boolean z) {
                if (z) {
                    if (KeyguardStatusView.DEBUG) {
                        Slog.v("KeyguardStatusView", "refresh statusview showing:" + z);
                    }
                    KeyguardStatusView.this.refreshTime();
                    KeyguardStatusView.this.updateOwnerInfo();
                    KeyguardStatusView.this.updateLogoutView();
                }
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onStartedWakingUp() {
                KeyguardStatusView.this.setEnableMarquee(true);
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onFinishedGoingToSleep(int i2) {
                KeyguardStatusView.this.setEnableMarquee(false);
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onUserSwitchComplete(int i2) {
                KeyguardStatusView.this.refreshFormat();
                KeyguardStatusView.this.updateOwnerInfo();
                KeyguardStatusView.this.updateLogoutView();
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onLogoutEnabledChanged() {
                KeyguardStatusView.this.updateLogoutView();
            }
        };
        this.mIActivityManager = ActivityManager.getService();
        this.mLockPatternUtils = new LockPatternUtils(getContext());
        this.mHandler = new Handler(Looper.myLooper());
        this.mSmallClockScale = getResources().getDimension(com.android.systemui.R.dimen.widget_small_font_size) / getResources().getDimension(com.android.systemui.R.dimen.widget_big_font_size);
        onDensityOrFontScaleChanged();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setEnableMarquee(boolean z) {
        if (DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Schedule setEnableMarquee: ");
            sb.append(z ? "Enable" : "Disable");
            Log.v("KeyguardStatusView", sb.toString());
        }
        if (z) {
            if (this.mPendingMarqueeStart == null) {
                this.mPendingMarqueeStart = new Runnable() { // from class: com.android.keyguard.-$$Lambda$KeyguardStatusView$ps9yj97ShIVR2u2hJB8SKuKk-kQ
                    @Override // java.lang.Runnable
                    public final void run() {
                        KeyguardStatusView.lambda$setEnableMarquee$0(KeyguardStatusView.this);
                    }
                };
                this.mHandler.postDelayed(this.mPendingMarqueeStart, 2000L);
                return;
            }
            return;
        }
        if (this.mPendingMarqueeStart != null) {
            this.mHandler.removeCallbacks(this.mPendingMarqueeStart);
            this.mPendingMarqueeStart = null;
        }
        setEnableMarqueeImpl(false);
    }

    public static /* synthetic */ void lambda$setEnableMarquee$0(KeyguardStatusView keyguardStatusView) {
        keyguardStatusView.setEnableMarqueeImpl(true);
        keyguardStatusView.mPendingMarqueeStart = null;
    }

    private void setEnableMarqueeImpl(boolean z) {
        if (DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append(z ? "Enable" : "Disable");
            sb.append(" transport text marquee");
            Log.v("KeyguardStatusView", sb.toString());
        }
        if (this.mOwnerInfo != null) {
            this.mOwnerInfo.setSelected(z);
        }
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mLogoutView = (TextView) findViewById(com.android.systemui.R.id.logout);
        if (this.mLogoutView != null) {
            this.mLogoutView.setOnClickListener(new View.OnClickListener() { // from class: com.android.keyguard.-$$Lambda$KeyguardStatusView$Pryio69yVoRI9F153p5QiMZe-bw
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    KeyguardStatusView.this.onLogoutClicked(view);
                }
            });
        }
        this.mClockView = (TextClock) findViewById(com.android.systemui.R.id.clock_view);
        this.mClockView.setShowCurrentUserTime(true);
        if (KeyguardClockAccessibilityDelegate.isNeeded(this.mContext)) {
            this.mClockView.setAccessibilityDelegate(new KeyguardClockAccessibilityDelegate(this.mContext));
        }
        this.mOwnerInfo = (TextView) findViewById(com.android.systemui.R.id.owner_info);
        this.mKeyguardSlice = (KeyguardSliceView) findViewById(com.android.systemui.R.id.keyguard_status_area);
        this.mClockSeparator = findViewById(com.android.systemui.R.id.clock_separator);
        this.mVisibleInDoze = Sets.newArraySet(new View[]{this.mClockView, this.mKeyguardSlice});
        this.mTextColor = this.mClockView.getCurrentTextColor();
        this.mClockView.getPaint().setStrokeWidth(getResources().getDimensionPixelSize(com.android.systemui.R.dimen.widget_small_font_stroke));
        this.mClockView.addOnLayoutChangeListener(this);
        this.mClockSeparator.addOnLayoutChangeListener(this);
        this.mKeyguardSlice.setContentChangeListener(new Runnable() { // from class: com.android.keyguard.-$$Lambda$KeyguardStatusView$Xo7rGDTjuOiD9nJpe80IUZ1ddFw
            @Override // java.lang.Runnable
            public final void run() {
                KeyguardStatusView.this.onSliceContentChanged();
            }
        });
        onSliceContentChanged();
        setEnableMarquee(KeyguardUpdateMonitor.getInstance(this.mContext).isDeviceInteractive());
        refreshFormat();
        updateOwnerInfo();
        updateLogoutView();
        updateDark();
        this.mClockView.setElegantTextHeight(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onSliceContentChanged() {
        boolean z;
        if (this.mKeyguardSlice.hasHeader() || this.mPulsing) {
            z = true;
        } else {
            z = false;
        }
        float f = z ? this.mSmallClockScale : 1.0f;
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.mClockView.getLayoutParams();
        float height = this.mClockView.getHeight();
        layoutParams.bottomMargin = (int) (-(height - (f * height)));
        this.mClockView.setLayoutParams(layoutParams);
        RelativeLayout.LayoutParams layoutParams2 = (RelativeLayout.LayoutParams) this.mClockSeparator.getLayoutParams();
        layoutParams2.topMargin = z ? (int) this.mWidgetPadding : 0;
        layoutParams2.bottomMargin = layoutParams2.topMargin;
        this.mClockSeparator.setLayoutParams(layoutParams2);
    }

    @Override // android.view.View.OnLayoutChangeListener
    public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        int height;
        float f;
        if (!this.mPulsing) {
            height = getHeight() - this.mLastLayoutHeight;
        } else {
            height = 0;
        }
        boolean hasHeader = this.mKeyguardSlice.hasHeader();
        boolean z = hasHeader || this.mPulsing;
        long j = z ? 0L : 137L;
        boolean z2 = this.mKeyguardSlice.getLayoutTransition() != null && this.mKeyguardSlice.getLayoutTransition().isRunning();
        if (view != this.mClockView) {
            if (view == this.mClockSeparator) {
                f = hasHeader && !this.mPulsing ? 1.0f : 0.0f;
                this.mClockSeparator.animate().cancel();
                if (z2) {
                    boolean z3 = this.mDarkAmount != 0.0f;
                    this.mClockSeparator.setY(i6 + height);
                    this.mClockSeparator.animate().setInterpolator(Interpolators.FAST_OUT_SLOW_IN).setDuration(550L).setListener(z3 ? null : new KeepAwakeAnimationListener(getContext())).setStartDelay(j).y(i2).alpha(f).start();
                    return;
                }
                this.mClockSeparator.setY(i2);
                this.mClockSeparator.setAlpha(f);
                return;
            }
            return;
        }
        f = z ? this.mSmallClockScale : 1.0f;
        final Paint.Style style = z ? Paint.Style.FILL_AND_STROKE : Paint.Style.FILL;
        this.mClockView.animate().cancel();
        if (z2) {
            this.mClockView.setY(i6 + height);
            this.mClockView.animate().setInterpolator(Interpolators.FAST_OUT_SLOW_IN).setDuration(550L).setListener(new ClipChildrenAnimationListener()).setStartDelay(j).y(i2).scaleX(f).scaleY(f).withEndAction(new Runnable() { // from class: com.android.keyguard.-$$Lambda$KeyguardStatusView$_ou77KlqH-CgaqfAz1VhLZdzKgc
                @Override // java.lang.Runnable
                public final void run() {
                    KeyguardStatusView.lambda$onLayoutChange$1(KeyguardStatusView.this, style);
                }
            }).start();
            return;
        }
        this.mClockView.setY(i2);
        this.mClockView.setScaleX(f);
        this.mClockView.setScaleY(f);
        this.mClockView.getPaint().setStyle(style);
        this.mClockView.invalidate();
    }

    public static /* synthetic */ void lambda$onLayoutChange$1(KeyguardStatusView keyguardStatusView, Paint.Style style) {
        keyguardStatusView.mClockView.getPaint().setStyle(style);
        keyguardStatusView.mClockView.invalidate();
    }

    @Override // android.widget.GridLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        this.mClockView.setPivotX(this.mClockView.getWidth() / 2);
        this.mClockView.setPivotY(0.0f);
        this.mLastLayoutHeight = getHeight();
        layoutOwnerInfo();
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onDensityOrFontScaleChanged() {
        this.mWidgetPadding = getResources().getDimension(com.android.systemui.R.dimen.widget_vertical_padding);
        if (this.mClockView != null) {
            this.mClockView.setTextSize(0, getResources().getDimensionPixelSize(com.android.systemui.R.dimen.widget_big_font_size));
            this.mClockView.getPaint().setStrokeWidth(getResources().getDimensionPixelSize(com.android.systemui.R.dimen.widget_small_font_stroke));
        }
        if (this.mOwnerInfo != null) {
            this.mOwnerInfo.setTextSize(0, getResources().getDimensionPixelSize(com.android.systemui.R.dimen.widget_label_font_size));
        }
    }

    public void dozeTimeTick() {
        refreshTime();
        this.mKeyguardSlice.refresh();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void refreshTime() {
        this.mClockView.refresh();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void refreshFormat() {
        Patterns.update(this.mContext);
        this.mClockView.setFormat12Hour(Patterns.clockView12);
        this.mClockView.setFormat24Hour(Patterns.clockView24);
    }

    public int getLogoutButtonHeight() {
        if (this.mLogoutView != null && this.mLogoutView.getVisibility() == 0) {
            return this.mLogoutView.getHeight();
        }
        return 0;
    }

    public float getClockTextSize() {
        return this.mClockView.getTextSize();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateLogoutView() {
        if (this.mLogoutView == null) {
            return;
        }
        this.mLogoutView.setVisibility(shouldShowLogout() ? 0 : 8);
        this.mLogoutView.setText(this.mContext.getResources().getString(17039947));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateOwnerInfo() {
        if (this.mOwnerInfo == null) {
            return;
        }
        String deviceOwnerInfo = this.mLockPatternUtils.getDeviceOwnerInfo();
        if (deviceOwnerInfo == null && this.mLockPatternUtils.isOwnerInfoEnabled(KeyguardUpdateMonitor.getCurrentUser())) {
            deviceOwnerInfo = this.mLockPatternUtils.getOwnerInfo(KeyguardUpdateMonitor.getCurrentUser());
        }
        if (!TextUtils.isEmpty(deviceOwnerInfo)) {
            this.mOwnerInfo.setVisibility(0);
        }
        this.mOwnerInfo.setText(deviceOwnerInfo);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        KeyguardUpdateMonitor.getInstance(this.mContext).registerCallback(this.mInfoCallback);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        KeyguardUpdateMonitor.getInstance(this.mContext).removeCallback(this.mInfoCallback);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).removeCallback(this);
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onLocaleListChanged() {
        refreshFormat();
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static final class Patterns {
        static String cacheKey;
        static String clockView12;
        static String clockView24;

        static void update(Context context) {
            Locale locale = Locale.getDefault();
            Resources resources = context.getResources();
            String string = resources.getString(com.android.systemui.R.string.clock_12hr_format);
            String string2 = resources.getString(com.android.systemui.R.string.clock_24hr_format);
            String str = locale.toString() + string + string2;
            if (str.equals(cacheKey)) {
                return;
            }
            clockView12 = DateFormat.getBestDateTimePattern(locale, string);
            if (!string.contains("a")) {
                clockView12 = clockView12.replaceAll("a", "").trim();
            }
            clockView24 = DateFormat.getBestDateTimePattern(locale, string2);
            clockView24 = clockView24.replace(':', (char) 60929);
            clockView12 = clockView12.replace(':', (char) 60929);
            cacheKey = str;
        }
    }

    public void setDarkAmount(float f) {
        if (this.mDarkAmount == f) {
            return;
        }
        this.mDarkAmount = f;
        updateDark();
    }

    private void updateDark() {
        boolean z = this.mDarkAmount == 1.0f;
        if (this.mLogoutView != null) {
            this.mLogoutView.setAlpha(z ? 0.0f : 1.0f);
        }
        if (this.mOwnerInfo != null) {
            this.mOwnerInfo.setVisibility(TextUtils.isEmpty(this.mOwnerInfo.getText()) ^ true ? 0 : 8);
            layoutOwnerInfo();
        }
        int blendARGB = ColorUtils.blendARGB(this.mTextColor, -1, this.mDarkAmount);
        updateDozeVisibleViews();
        this.mKeyguardSlice.setDarkAmount(this.mDarkAmount);
        this.mClockView.setTextColor(blendARGB);
        this.mClockSeparator.setBackgroundColor(blendARGB);
    }

    private void layoutOwnerInfo() {
        if (this.mOwnerInfo != null && this.mOwnerInfo.getVisibility() != 8) {
            this.mOwnerInfo.setAlpha(1.0f - this.mDarkAmount);
            setBottom(getMeasuredHeight() - ((int) (((this.mOwnerInfo.getBottom() + this.mOwnerInfo.getPaddingBottom()) - (this.mOwnerInfo.getTop() - this.mOwnerInfo.getPaddingTop())) * this.mDarkAmount)));
        }
    }

    public void setPulsing(boolean z, boolean z2) {
        this.mPulsing = z;
        this.mKeyguardSlice.setPulsing(z, z2);
        updateDozeVisibleViews();
    }

    private void updateDozeVisibleViews() {
        Iterator<View> it = this.mVisibleInDoze.iterator();
        while (it.hasNext()) {
            View next = it.next();
            float f = 1.0f;
            if (this.mDarkAmount == 1.0f && this.mPulsing) {
                f = 0.8f;
            }
            next.setAlpha(f);
        }
    }

    private boolean shouldShowLogout() {
        return KeyguardUpdateMonitor.getInstance(this.mContext).isLogoutEnabled() && KeyguardUpdateMonitor.getCurrentUser() != 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onLogoutClicked(View view) {
        int currentUser = KeyguardUpdateMonitor.getCurrentUser();
        try {
            this.mIActivityManager.switchUser(0);
            this.mIActivityManager.stopUser(currentUser, true, (IStopUserCallback) null);
        } catch (RemoteException e) {
            Log.e("KeyguardStatusView", "Failed to logout user", e);
        }
    }

    /* loaded from: classes.dex */
    private class ClipChildrenAnimationListener extends AnimatorListenerAdapter implements ViewClippingUtil.ClippingParameters {
        ClipChildrenAnimationListener() {
            ViewClippingUtil.setClippingDeactivated(KeyguardStatusView.this.mClockView, true, this);
        }

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animator) {
            ViewClippingUtil.setClippingDeactivated(KeyguardStatusView.this.mClockView, false, this);
        }

        public boolean shouldFinish(View view) {
            return view == KeyguardStatusView.this.getParent();
        }
    }
}
