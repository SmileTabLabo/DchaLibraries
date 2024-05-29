package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.keyguard.CarrierText;
import com.android.systemui.BatteryMeterView;
import com.android.systemui.Interpolators;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.KeyguardUserSwitcher;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import java.text.NumberFormat;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/KeyguardStatusBarView.class */
public class KeyguardStatusBarView extends RelativeLayout implements BatteryController.BatteryStateChangeCallback {
    private boolean mBatteryCharging;
    private BatteryController mBatteryController;
    private TextView mBatteryLevel;
    private boolean mBatteryListening;
    private CarrierText mCarrierLabel;
    private KeyguardUserSwitcher mKeyguardUserSwitcher;
    private boolean mKeyguardUserSwitcherShowing;
    private ImageView mMultiUserAvatar;
    private MultiUserSwitch mMultiUserSwitch;
    private View mSystemIconsContainer;
    private View mSystemIconsSuperContainer;
    private int mSystemIconsSwitcherHiddenExpandedMargin;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.android.systemui.statusbar.phone.KeyguardStatusBarView$2  reason: invalid class name */
    /* loaded from: a.zip:com/android/systemui/statusbar/phone/KeyguardStatusBarView$2.class */
    public class AnonymousClass2 implements ViewTreeObserver.OnPreDrawListener {
        final KeyguardStatusBarView this$0;
        final int val$systemIconsCurrentX;
        final boolean val$userSwitcherVisible;

        AnonymousClass2(KeyguardStatusBarView keyguardStatusBarView, boolean z, int i) {
            this.this$0 = keyguardStatusBarView;
            this.val$userSwitcherVisible = z;
            this.val$systemIconsCurrentX = i;
        }

        @Override // android.view.ViewTreeObserver.OnPreDrawListener
        public boolean onPreDraw() {
            this.this$0.getViewTreeObserver().removeOnPreDrawListener(this);
            boolean z = this.val$userSwitcherVisible ? this.this$0.mMultiUserSwitch.getParent() != this.this$0 : false;
            this.this$0.mSystemIconsSuperContainer.setX(this.val$systemIconsCurrentX);
            this.this$0.mSystemIconsSuperContainer.animate().translationX(0.0f).setDuration(400L).setStartDelay(z ? 300 : 0).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).start();
            if (z) {
                this.this$0.getOverlay().add(this.this$0.mMultiUserSwitch);
                this.this$0.mMultiUserSwitch.animate().alpha(0.0f).setDuration(300L).setStartDelay(0L).setInterpolator(Interpolators.ALPHA_OUT).withEndAction(new Runnable(this) { // from class: com.android.systemui.statusbar.phone.KeyguardStatusBarView.2.1
                    final AnonymousClass2 this$1;

                    {
                        this.this$1 = this;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.this$1.this$0.mMultiUserSwitch.setAlpha(1.0f);
                        this.this$1.this$0.getOverlay().remove(this.this$1.this$0.mMultiUserSwitch);
                    }
                }).start();
                return true;
            }
            this.this$0.mMultiUserSwitch.setAlpha(0.0f);
            this.this$0.mMultiUserSwitch.animate().alpha(1.0f).setDuration(300L).setStartDelay(200L).setInterpolator(Interpolators.ALPHA_IN);
            return true;
        }
    }

    public KeyguardStatusBarView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    private void animateNextLayoutChange() {
        getViewTreeObserver().addOnPreDrawListener(new AnonymousClass2(this, this.mMultiUserSwitch.getParent() == this, this.mSystemIconsSuperContainer.getLeft()));
    }

    private void loadDimens() {
        this.mSystemIconsSwitcherHiddenExpandedMargin = getResources().getDimensionPixelSize(2131689902);
    }

    private void updateSystemIconsLayoutParams() {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.mSystemIconsSuperContainer.getLayoutParams();
        int i = this.mKeyguardUserSwitcherShowing ? this.mSystemIconsSwitcherHiddenExpandedMargin : 0;
        if (i != layoutParams.getMarginEnd()) {
            layoutParams.setMarginEnd(i);
            this.mSystemIconsSuperContainer.setLayoutParams(layoutParams);
        }
    }

    private void updateUserSwitcher() {
        boolean z = this.mKeyguardUserSwitcher != null;
        this.mMultiUserSwitch.setClickable(z);
        this.mMultiUserSwitch.setFocusable(z);
        this.mMultiUserSwitch.setKeyguardMode(z);
    }

    private void updateVisibilities() {
        int i = 0;
        if (this.mMultiUserSwitch.getParent() != this && !this.mKeyguardUserSwitcherShowing) {
            if (this.mMultiUserSwitch.getParent() != null) {
                getOverlay().remove(this.mMultiUserSwitch);
            }
            addView(this.mMultiUserSwitch, 0);
        } else if (this.mMultiUserSwitch.getParent() == this && this.mKeyguardUserSwitcherShowing) {
            removeView(this.mMultiUserSwitch);
        }
        TextView textView = this.mBatteryLevel;
        if (!this.mBatteryCharging) {
            i = 8;
        }
        textView.setVisibility(i);
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onBatteryLevelChanged(int i, boolean z, boolean z2) {
        this.mBatteryLevel.setText(NumberFormat.getPercentInstance().format(i / 100.0d));
        boolean z3 = this.mBatteryCharging != z2;
        this.mBatteryCharging = z2;
        if (z3) {
            updateVisibilities();
        }
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mMultiUserAvatar.getLayoutParams();
        int dimensionPixelSize = getResources().getDimensionPixelSize(2131689912);
        marginLayoutParams.height = dimensionPixelSize;
        marginLayoutParams.width = dimensionPixelSize;
        this.mMultiUserAvatar.setLayoutParams(marginLayoutParams);
        ViewGroup.MarginLayoutParams marginLayoutParams2 = (ViewGroup.MarginLayoutParams) this.mMultiUserSwitch.getLayoutParams();
        marginLayoutParams2.width = getResources().getDimensionPixelSize(2131689910);
        marginLayoutParams2.setMarginEnd(getResources().getDimensionPixelSize(2131689901));
        this.mMultiUserSwitch.setLayoutParams(marginLayoutParams2);
        ViewGroup.MarginLayoutParams marginLayoutParams3 = (ViewGroup.MarginLayoutParams) this.mSystemIconsSuperContainer.getLayoutParams();
        marginLayoutParams3.height = getResources().getDimensionPixelSize(2131689810);
        marginLayoutParams3.setMarginStart(getResources().getDimensionPixelSize(2131689813));
        this.mSystemIconsSuperContainer.setLayoutParams(marginLayoutParams3);
        this.mSystemIconsSuperContainer.setPaddingRelative(this.mSystemIconsSuperContainer.getPaddingStart(), this.mSystemIconsSuperContainer.getPaddingTop(), getResources().getDimensionPixelSize(2131689925), this.mSystemIconsSuperContainer.getPaddingBottom());
        ViewGroup.MarginLayoutParams marginLayoutParams4 = (ViewGroup.MarginLayoutParams) this.mSystemIconsContainer.getLayoutParams();
        marginLayoutParams4.height = getResources().getDimensionPixelSize(2131690071);
        this.mSystemIconsContainer.setLayoutParams(marginLayoutParams4);
        ViewGroup.MarginLayoutParams marginLayoutParams5 = (ViewGroup.MarginLayoutParams) this.mBatteryLevel.getLayoutParams();
        marginLayoutParams5.setMarginStart(getResources().getDimensionPixelSize(2131689924));
        this.mBatteryLevel.setLayoutParams(marginLayoutParams5);
        this.mBatteryLevel.setPaddingRelative(this.mBatteryLevel.getPaddingStart(), this.mBatteryLevel.getPaddingTop(), getResources().getDimensionPixelSize(2131689918), this.mBatteryLevel.getPaddingBottom());
        this.mBatteryLevel.setTextSize(0, getResources().getDimensionPixelSize(2131689938));
        this.mCarrierLabel.setTextSize(0, getResources().getDimensionPixelSize(17105168));
        ViewGroup.MarginLayoutParams marginLayoutParams6 = (ViewGroup.MarginLayoutParams) this.mCarrierLabel.getLayoutParams();
        marginLayoutParams6.setMarginStart(getResources().getDimensionPixelSize(2131689922));
        this.mCarrierLabel.setLayoutParams(marginLayoutParams6);
        ViewGroup.MarginLayoutParams marginLayoutParams7 = (ViewGroup.MarginLayoutParams) getLayoutParams();
        marginLayoutParams7.height = getResources().getDimensionPixelSize(2131689812);
        setLayoutParams(marginLayoutParams7);
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mSystemIconsSuperContainer = findViewById(2131886351);
        this.mSystemIconsContainer = findViewById(2131886352);
        this.mMultiUserSwitch = (MultiUserSwitch) findViewById(2131886349);
        this.mMultiUserAvatar = (ImageView) findViewById(2131886350);
        this.mBatteryLevel = (TextView) findViewById(2131886353);
        this.mCarrierLabel = (CarrierText) findViewById(2131886354);
        loadDimens();
        updateUserSwitcher();
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onPowerSaveChanged(boolean z) {
    }

    public void setBatteryController(BatteryController batteryController) {
        this.mBatteryController = batteryController;
        ((BatteryMeterView) findViewById(2131886720)).setBatteryController(batteryController);
    }

    public void setKeyguardUserSwitcher(KeyguardUserSwitcher keyguardUserSwitcher) {
        this.mKeyguardUserSwitcher = keyguardUserSwitcher;
        this.mMultiUserSwitch.setKeyguardUserSwitcher(keyguardUserSwitcher);
        updateUserSwitcher();
    }

    public void setKeyguardUserSwitcherShowing(boolean z, boolean z2) {
        this.mKeyguardUserSwitcherShowing = z;
        if (z2) {
            animateNextLayoutChange();
        }
        updateVisibilities();
        updateSystemIconsLayoutParams();
    }

    public void setListening(boolean z) {
        if (z == this.mBatteryListening) {
            return;
        }
        this.mBatteryListening = z;
        if (this.mBatteryListening) {
            this.mBatteryController.addStateChangedCallback(this);
        } else {
            this.mBatteryController.removeStateChangedCallback(this);
        }
    }

    public void setQSPanel(QSPanel qSPanel) {
        this.mMultiUserSwitch.setQsPanel(qSPanel);
    }

    public void setUserInfoController(UserInfoController userInfoController) {
        userInfoController.addListener(new UserInfoController.OnUserInfoChangedListener(this) { // from class: com.android.systemui.statusbar.phone.KeyguardStatusBarView.1
            final KeyguardStatusBarView this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.systemui.statusbar.policy.UserInfoController.OnUserInfoChangedListener
            public void onUserInfoChanged(String str, Drawable drawable) {
                Log.d("KeyguardStatusBarView", "onUserInfoChanged and set new profile icon");
                this.this$0.mMultiUserAvatar.setImageDrawable(drawable);
            }
        });
    }

    public void setUserSwitcherController(UserSwitcherController userSwitcherController) {
        this.mMultiUserSwitch.setUserSwitcherController(userSwitcherController);
    }

    @Override // android.view.View
    public void setVisibility(int i) {
        super.setVisibility(i);
        if (i != 0) {
            this.mSystemIconsSuperContainer.animate().cancel();
            this.mMultiUserSwitch.animate().cancel();
            this.mMultiUserSwitch.setAlpha(1.0f);
        }
    }
}
