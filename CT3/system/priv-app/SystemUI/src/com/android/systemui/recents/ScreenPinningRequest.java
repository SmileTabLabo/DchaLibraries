package com.android.systemui.recents;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.view.IWindowManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/systemui/recents/ScreenPinningRequest.class */
public class ScreenPinningRequest implements View.OnClickListener {
    private final AccessibilityManager mAccessibilityService;
    private final Context mContext;
    private RequestWindowView mRequestWindow;
    private final WindowManager mWindowManager;
    private IWindowManager mWindowManagerService = WindowManagerGlobal.getWindowManagerService();
    private int taskId;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/recents/ScreenPinningRequest$RequestWindowView.class */
    public class RequestWindowView extends FrameLayout {
        private final ColorDrawable mColor;
        private ValueAnimator mColorAnim;
        private ViewGroup mLayout;
        private final BroadcastReceiver mReceiver;
        private boolean mShowCancel;
        private final Runnable mUpdateLayoutRunnable;
        final ScreenPinningRequest this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public RequestWindowView(ScreenPinningRequest screenPinningRequest, Context context, boolean z) {
            super(context);
            this.this$0 = screenPinningRequest;
            this.mColor = new ColorDrawable(0);
            this.mUpdateLayoutRunnable = new Runnable(this) { // from class: com.android.systemui.recents.ScreenPinningRequest.RequestWindowView.1
                final RequestWindowView this$1;

                {
                    this.this$1 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    if (this.this$1.mLayout == null || this.this$1.mLayout.getParent() == null) {
                        return;
                    }
                    this.this$1.mLayout.setLayoutParams(this.this$1.this$0.getRequestLayoutParams(this.this$1.isLandscapePhone(this.this$1.mContext)));
                }
            };
            this.mReceiver = new BroadcastReceiver(this) { // from class: com.android.systemui.recents.ScreenPinningRequest.RequestWindowView.2
                final RequestWindowView this$1;

                {
                    this.this$1 = this;
                }

                @Override // android.content.BroadcastReceiver
                public void onReceive(Context context2, Intent intent) {
                    if (intent.getAction().equals("android.intent.action.CONFIGURATION_CHANGED")) {
                        this.this$1.post(this.this$1.mUpdateLayoutRunnable);
                    } else if (intent.getAction().equals("android.intent.action.USER_SWITCHED") || intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
                        this.this$1.this$0.clearPrompt();
                    }
                }
            };
            setClickable(true);
            setOnClickListener(screenPinningRequest);
            setBackground(this.mColor);
            this.mShowCancel = z;
        }

        private void inflateView(boolean z) {
            this.mLayout = (ViewGroup) View.inflate(getContext(), z ? 2130968798 : 2130968795, null);
            this.mLayout.setClickable(true);
            this.mLayout.setLayoutDirection(0);
            this.mLayout.findViewById(2131886647).setLayoutDirection(3);
            View findViewById = this.mLayout.findViewById(2131886641);
            if (Recents.getSystemServices().hasSoftNavigationBar()) {
                findViewById.setLayoutDirection(3);
                swapChildrenIfRtlAndVertical(findViewById);
            } else {
                findViewById.setVisibility(8);
            }
            ((Button) this.mLayout.findViewById(2131886650)).setOnClickListener(this.this$0);
            if (this.mShowCancel) {
                ((Button) this.mLayout.findViewById(2131886651)).setOnClickListener(this.this$0);
            } else {
                ((Button) this.mLayout.findViewById(2131886651)).setVisibility(4);
            }
            ((TextView) this.mLayout.findViewById(2131886649)).setText(2131493685);
            int i = this.this$0.mAccessibilityService.isEnabled() ? 4 : 0;
            this.mLayout.findViewById(2131886644).setVisibility(i);
            this.mLayout.findViewById(2131886643).setVisibility(i);
            addView(this.mLayout, this.this$0.getRequestLayoutParams(z));
        }

        /* JADX INFO: Access modifiers changed from: private */
        public boolean isLandscapePhone(Context context) {
            Configuration configuration = this.mContext.getResources().getConfiguration();
            boolean z = false;
            if (configuration.orientation == 2) {
                z = false;
                if (configuration.smallestScreenWidthDp < 600) {
                    z = true;
                }
            }
            return z;
        }

        private void swapChildrenIfRtlAndVertical(View view) {
            if (this.mContext.getResources().getConfiguration().getLayoutDirection() != 1) {
                return;
            }
            LinearLayout linearLayout = (LinearLayout) view;
            if (linearLayout.getOrientation() == 1) {
                int childCount = linearLayout.getChildCount();
                ArrayList arrayList = new ArrayList(childCount);
                for (int i = 0; i < childCount; i++) {
                    arrayList.add(linearLayout.getChildAt(i));
                }
                linearLayout.removeAllViews();
                for (int i2 = childCount - 1; i2 >= 0; i2--) {
                    linearLayout.addView((View) arrayList.get(i2));
                }
            }
        }

        @Override // android.view.ViewGroup, android.view.View
        public void onAttachedToWindow() {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            this.this$0.mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
            float f = displayMetrics.density;
            boolean isLandscapePhone = isLandscapePhone(this.mContext);
            inflateView(isLandscapePhone);
            int color = this.mContext.getColor(2131558571);
            if (ActivityManager.isHighEndGfx()) {
                this.mLayout.setAlpha(0.0f);
                if (isLandscapePhone) {
                    this.mLayout.setTranslationX(96.0f * f);
                } else {
                    this.mLayout.setTranslationY(96.0f * f);
                }
                this.mLayout.animate().alpha(1.0f).translationX(0.0f).translationY(0.0f).setDuration(300L).setInterpolator(new DecelerateInterpolator()).start();
                this.mColorAnim = ValueAnimator.ofObject(new ArgbEvaluator(), 0, Integer.valueOf(color));
                this.mColorAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this) { // from class: com.android.systemui.recents.ScreenPinningRequest.RequestWindowView.3
                    final RequestWindowView this$1;

                    {
                        this.this$1 = this;
                    }

                    @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        this.this$1.mColor.setColor(((Integer) valueAnimator.getAnimatedValue()).intValue());
                    }
                });
                this.mColorAnim.setDuration(1000L);
                this.mColorAnim.start();
            } else {
                this.mColor.setColor(color);
            }
            IntentFilter intentFilter = new IntentFilter("android.intent.action.CONFIGURATION_CHANGED");
            intentFilter.addAction("android.intent.action.USER_SWITCHED");
            intentFilter.addAction("android.intent.action.SCREEN_OFF");
            this.mContext.registerReceiver(this.mReceiver, intentFilter);
        }

        protected void onConfigurationChanged() {
            removeAllViews();
            inflateView(isLandscapePhone(this.mContext));
        }

        @Override // android.view.ViewGroup, android.view.View
        public void onDetachedFromWindow() {
            this.mContext.unregisterReceiver(this.mReceiver);
        }
    }

    public ScreenPinningRequest(Context context) {
        this.mContext = context;
        this.mAccessibilityService = (AccessibilityManager) this.mContext.getSystemService("accessibility");
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
    }

    private WindowManager.LayoutParams getWindowLayoutParams() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, -1, 2024, 16777480, -3);
        layoutParams.privateFlags |= 16;
        layoutParams.setTitle("ScreenPinningConfirmation");
        layoutParams.gravity = 119;
        return layoutParams;
    }

    public void clearPrompt() {
        if (this.mRequestWindow != null) {
            this.mWindowManager.removeView(this.mRequestWindow);
            this.mRequestWindow = null;
        }
    }

    public FrameLayout.LayoutParams getRequestLayoutParams(boolean z) {
        return new FrameLayout.LayoutParams(-2, -2, z ? 21 : 81);
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (view.getId() == 2131886650 || this.mRequestWindow == view) {
            try {
                ActivityManagerNative.getDefault().startSystemLockTaskMode(this.taskId);
            } catch (RemoteException e) {
            }
        }
        clearPrompt();
    }

    public void onConfigurationChanged() {
        if (this.mRequestWindow != null) {
            this.mRequestWindow.onConfigurationChanged();
        }
    }

    public void showPrompt(int i, boolean z) {
        try {
            clearPrompt();
        } catch (IllegalArgumentException e) {
        }
        this.taskId = i;
        this.mRequestWindow = new RequestWindowView(this, this.mContext, z);
        this.mRequestWindow.setSystemUiVisibility(256);
        this.mWindowManager.addView(this.mRequestWindow, getWindowLayoutParams());
    }
}
