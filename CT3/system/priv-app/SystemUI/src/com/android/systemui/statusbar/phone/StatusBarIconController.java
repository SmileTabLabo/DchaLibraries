package com.android.systemui.statusbar.phone;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.keyguard.R$id;
import com.android.systemui.BatteryMeterView;
import com.android.systemui.FontSizeUtils;
import com.android.systemui.Interpolators;
import com.android.systemui.SystemUIFactory;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.SignalClusterView;
import com.android.systemui.statusbar.StatusBarIconView;
import com.android.systemui.tuner.TunerService;
import java.io.PrintWriter;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/StatusBarIconController.class */
public class StatusBarIconController extends StatusBarIconList implements TunerService.Tunable {
    private BatteryMeterView mBatteryMeterView;
    private BatteryMeterView mBatteryMeterViewKeyguard;
    private TextView mClock;
    private Context mContext;
    private float mDarkIntensity;
    private int mDarkModeIconColorSingleTone;
    private DemoStatusIcons mDemoStatusIcons;
    private final Handler mHandler;
    private final ArraySet<String> mIconBlacklist;
    private int mIconHPadding;
    private int mIconSize;
    private int mIconTint;
    private int mLightModeIconColorSingleTone;
    private NotificationIconAreaController mNotificationIconAreaController;
    private View mNotificationIconAreaInner;
    private float mPendingDarkIntensity;
    private PhoneStatusBar mPhoneStatusBar;
    private SignalClusterView mSignalCluster;
    private LinearLayout mStatusIcons;
    private LinearLayout mStatusIconsKeyguard;
    private LinearLayout mSystemIconArea;
    private ValueAnimator mTintAnimator;
    private final Rect mTintArea;
    private boolean mTintChangePending;
    private boolean mTransitionDeferring;
    private final Runnable mTransitionDeferringDoneRunnable;
    private long mTransitionDeferringDuration;
    private long mTransitionDeferringStartTime;
    private boolean mTransitionPending;
    private static final Rect sTmpRect = new Rect();
    private static final int[] sTmpInt2 = new int[2];

    public StatusBarIconController(Context context, View view, View view2, PhoneStatusBar phoneStatusBar) {
        super(context.getResources().getStringArray(17235982));
        this.mIconTint = -1;
        this.mTintArea = new Rect();
        this.mIconBlacklist = new ArraySet<>();
        this.mTransitionDeferringDoneRunnable = new Runnable(this) { // from class: com.android.systemui.statusbar.phone.StatusBarIconController.1
            final StatusBarIconController this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mTransitionDeferring = false;
            }
        };
        this.mContext = context;
        this.mPhoneStatusBar = phoneStatusBar;
        this.mSystemIconArea = (LinearLayout) view.findViewById(2131886678);
        this.mStatusIcons = (LinearLayout) view.findViewById(2131886719);
        this.mSignalCluster = (SignalClusterView) view.findViewById(2131886656);
        this.mNotificationIconAreaController = SystemUIFactory.getInstance().createNotificationIconAreaController(context, phoneStatusBar);
        this.mNotificationIconAreaInner = this.mNotificationIconAreaController.getNotificationInnerAreaView();
        ((ViewGroup) view.findViewById(2131886677)).addView(this.mNotificationIconAreaInner);
        this.mStatusIconsKeyguard = (LinearLayout) view2.findViewById(2131886719);
        this.mBatteryMeterView = (BatteryMeterView) view.findViewById(2131886720);
        this.mBatteryMeterViewKeyguard = (BatteryMeterView) view2.findViewById(2131886720);
        scaleBatteryMeterViews(context);
        this.mClock = (TextView) view.findViewById(R$id.clock);
        this.mDarkModeIconColorSingleTone = context.getColor(2131558575);
        this.mLightModeIconColorSingleTone = context.getColor(2131558578);
        this.mHandler = new Handler();
        loadDimens();
        TunerService.get(this.mContext).addTunable(this, "icon_blacklist");
    }

    private void addSystemIcon(int i, StatusBarIcon statusBarIcon) {
        String slot = getSlot(i);
        int viewIndex = getViewIndex(i);
        boolean contains = this.mIconBlacklist.contains(slot);
        StatusBarIconView statusBarIconView = new StatusBarIconView(this.mContext, slot, null, contains);
        statusBarIconView.set(statusBarIcon);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, this.mIconSize);
        layoutParams.setMargins(this.mIconHPadding, 0, this.mIconHPadding, 0);
        this.mStatusIcons.addView(statusBarIconView, viewIndex, layoutParams);
        StatusBarIconView statusBarIconView2 = new StatusBarIconView(this.mContext, slot, null, contains);
        statusBarIconView2.set(statusBarIcon);
        this.mStatusIconsKeyguard.addView(statusBarIconView2, viewIndex, new LinearLayout.LayoutParams(-2, this.mIconSize));
        applyIconTint();
    }

    private void animateHide(View view, boolean z) {
        view.animate().cancel();
        if (z) {
            view.animate().alpha(0.0f).setDuration(160L).setStartDelay(0L).setInterpolator(Interpolators.ALPHA_OUT).withEndAction(new Runnable(this, view) { // from class: com.android.systemui.statusbar.phone.StatusBarIconController.2
                final StatusBarIconController this$0;
                final View val$v;

                {
                    this.this$0 = this;
                    this.val$v = view;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.val$v.setVisibility(4);
                }
            });
            return;
        }
        view.setAlpha(0.0f);
        view.setVisibility(4);
    }

    private void animateIconTint(float f, long j, long j2) {
        if (this.mTintAnimator != null) {
            this.mTintAnimator.cancel();
        }
        if (this.mDarkIntensity == f) {
            return;
        }
        this.mTintAnimator = ValueAnimator.ofFloat(this.mDarkIntensity, f);
        this.mTintAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this) { // from class: com.android.systemui.statusbar.phone.StatusBarIconController.3
            final StatusBarIconController this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.this$0.setIconTintInternal(((Float) valueAnimator.getAnimatedValue()).floatValue());
            }
        });
        this.mTintAnimator.setDuration(j2);
        this.mTintAnimator.setStartDelay(j);
        this.mTintAnimator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        this.mTintAnimator.start();
    }

    private void animateShow(View view, boolean z) {
        view.animate().cancel();
        view.setVisibility(0);
        if (!z) {
            view.setAlpha(1.0f);
            return;
        }
        view.animate().alpha(1.0f).setDuration(320L).setInterpolator(Interpolators.ALPHA_IN).setStartDelay(50L).withEndAction(null);
        if (this.mPhoneStatusBar.isKeyguardFadingAway()) {
            view.animate().setDuration(this.mPhoneStatusBar.getKeyguardFadingAwayDuration()).setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN).setStartDelay(this.mPhoneStatusBar.getKeyguardFadingAwayDelay()).start();
        }
    }

    private void applyIconTint() {
        for (int i = 0; i < this.mStatusIcons.getChildCount(); i++) {
            StatusBarIconView statusBarIconView = (StatusBarIconView) this.mStatusIcons.getChildAt(i);
            statusBarIconView.setImageTintList(ColorStateList.valueOf(getTint(this.mTintArea, statusBarIconView, this.mIconTint)));
        }
        this.mSignalCluster.setIconTint(this.mIconTint, this.mDarkIntensity, this.mTintArea);
        this.mBatteryMeterView.setDarkIntensity(isInArea(this.mTintArea, this.mBatteryMeterView) ? this.mDarkIntensity : 0.0f);
        this.mClock.setTextColor(getTint(this.mTintArea, this.mClock, this.mIconTint));
    }

    private void deferIconTintChange(float f) {
        if (this.mTintChangePending && f == this.mPendingDarkIntensity) {
            return;
        }
        this.mTintChangePending = true;
        this.mPendingDarkIntensity = f;
    }

    public static float getDarkIntensity(Rect rect, View view, float f) {
        if (isInArea(rect, view)) {
            return f;
        }
        return 0.0f;
    }

    public static ArraySet<String> getIconBlacklist(String str) {
        String[] split;
        ArraySet<String> arraySet = new ArraySet<>();
        String str2 = str;
        if (str == null) {
            str2 = "rotate,";
        }
        for (String str3 : str2.split(",")) {
            if (!TextUtils.isEmpty(str3)) {
                arraySet.add(str3);
            }
        }
        return arraySet;
    }

    public static int getTint(Rect rect, View view, int i) {
        if (isInArea(rect, view)) {
            return i;
        }
        return -1;
    }

    private void handleSet(int i, StatusBarIcon statusBarIcon) {
        int viewIndex = getViewIndex(i);
        ((StatusBarIconView) this.mStatusIcons.getChildAt(viewIndex)).set(statusBarIcon);
        ((StatusBarIconView) this.mStatusIconsKeyguard.getChildAt(viewIndex)).set(statusBarIcon);
        applyIconTint();
    }

    private static boolean isInArea(Rect rect, View view) {
        boolean z = true;
        if (rect.isEmpty()) {
            return true;
        }
        sTmpRect.set(rect);
        view.getLocationOnScreen(sTmpInt2);
        int i = sTmpInt2[0];
        int max = Math.max(0, Math.min(view.getWidth() + i, rect.right) - Math.max(i, rect.left));
        boolean z2 = rect.top <= 0;
        if (max * 2 <= view.getWidth()) {
            z = false;
        }
        if (!z) {
            z2 = false;
        }
        return z2;
    }

    private void loadDimens() {
        this.mIconSize = this.mContext.getResources().getDimensionPixelSize(17104926);
        this.mIconHPadding = this.mContext.getResources().getDimensionPixelSize(2131689796);
    }

    private void scaleBatteryMeterViews(Context context) {
        Resources resources = context.getResources();
        TypedValue typedValue = new TypedValue();
        resources.getValue(2131689784, typedValue, true);
        float f = typedValue.getFloat();
        int dimensionPixelSize = resources.getDimensionPixelSize(2131689777);
        int dimensionPixelSize2 = resources.getDimensionPixelSize(2131689778);
        int dimensionPixelSize3 = resources.getDimensionPixelSize(2131689944);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams((int) (dimensionPixelSize2 * f), (int) (dimensionPixelSize * f));
        layoutParams.setMarginsRelative(0, 0, 0, dimensionPixelSize3);
        this.mBatteryMeterView.setLayoutParams(layoutParams);
        this.mBatteryMeterViewKeyguard.setLayoutParams(layoutParams);
    }

    private void setHeightAndCenter(ImageView imageView, int i) {
        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
        layoutParams.height = i;
        if (layoutParams instanceof LinearLayout.LayoutParams) {
            ((LinearLayout.LayoutParams) layoutParams).gravity = 16;
        }
        imageView.setLayoutParams(layoutParams);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setIconTintInternal(float f) {
        this.mDarkIntensity = f;
        this.mIconTint = ((Integer) ArgbEvaluator.getInstance().evaluate(f, Integer.valueOf(this.mLightModeIconColorSingleTone), Integer.valueOf(this.mDarkModeIconColorSingleTone))).intValue();
        this.mNotificationIconAreaController.setIconTint(this.mIconTint);
        applyIconTint();
    }

    private void updateClock() {
        FontSizeUtils.updateFontSize(this.mClock, 2131689779);
        this.mClock.setPaddingRelative(this.mContext.getResources().getDimensionPixelSize(2131689780), 0, this.mContext.getResources().getDimensionPixelSize(2131689781), 0);
    }

    public void appTransitionCancelled() {
        if (this.mTransitionPending && this.mTintChangePending) {
            this.mTintChangePending = false;
            animateIconTint(this.mPendingDarkIntensity, 0L, 120L);
        }
        this.mTransitionPending = false;
    }

    public void appTransitionPending() {
        this.mTransitionPending = true;
    }

    public void appTransitionStarting(long j, long j2) {
        if (this.mTransitionPending && this.mTintChangePending) {
            this.mTintChangePending = false;
            animateIconTint(this.mPendingDarkIntensity, Math.max(0L, j - SystemClock.uptimeMillis()), j2);
        } else if (this.mTransitionPending) {
            this.mTransitionDeferring = true;
            this.mTransitionDeferringStartTime = j;
            this.mTransitionDeferringDuration = j2;
            this.mHandler.removeCallbacks(this.mTransitionDeferringDoneRunnable);
            this.mHandler.postAtTime(this.mTransitionDeferringDoneRunnable, j);
        }
        this.mTransitionPending = false;
    }

    public void dispatchDemoCommand(String str, Bundle bundle) {
        if (this.mDemoStatusIcons == null) {
            this.mDemoStatusIcons = new DemoStatusIcons(this.mStatusIcons, this.mIconSize);
        }
        this.mDemoStatusIcons.dispatchDemoCommand(str, bundle);
    }

    public void dump(PrintWriter printWriter) {
        int childCount = this.mStatusIcons.getChildCount();
        printWriter.println("  system icons: " + childCount);
        for (int i = 0; i < childCount; i++) {
            printWriter.println("    [" + i + "] icon=" + ((StatusBarIconView) this.mStatusIcons.getChildAt(i)));
        }
    }

    public void hideNotificationIconArea(boolean z) {
        animateHide(this.mNotificationIconAreaInner, z);
    }

    public void hideSystemIconArea(boolean z) {
        animateHide(this.mSystemIconArea, z);
    }

    public void onDensityOrFontScaleChanged() {
        loadDimens();
        this.mNotificationIconAreaController.onDensityOrFontScaleChanged(this.mContext);
        updateClock();
        for (int i = 0; i < this.mStatusIcons.getChildCount(); i++) {
            View childAt = this.mStatusIcons.getChildAt(i);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, this.mIconSize);
            layoutParams.setMargins(this.mIconHPadding, 0, this.mIconHPadding, 0);
            childAt.setLayoutParams(layoutParams);
        }
        for (int i2 = 0; i2 < this.mStatusIconsKeyguard.getChildCount(); i2++) {
            this.mStatusIconsKeyguard.getChildAt(i2).setLayoutParams(new LinearLayout.LayoutParams(-2, this.mIconSize));
        }
        scaleBatteryMeterViews(this.mContext);
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        if ("icon_blacklist".equals(str)) {
            this.mIconBlacklist.clear();
            this.mIconBlacklist.addAll((ArraySet<? extends String>) getIconBlacklist(str2));
            ArrayList arrayList = new ArrayList();
            for (int i = 0; i < this.mStatusIcons.getChildCount(); i++) {
                arrayList.add((StatusBarIconView) this.mStatusIcons.getChildAt(i));
            }
            for (int size = arrayList.size() - 1; size >= 0; size--) {
                removeIcon(((StatusBarIconView) arrayList.get(size)).getSlot());
            }
            for (int i2 = 0; i2 < arrayList.size(); i2++) {
                setIcon(((StatusBarIconView) arrayList.get(i2)).getSlot(), ((StatusBarIconView) arrayList.get(i2)).getStatusBarIcon());
            }
        }
    }

    @Override // com.android.systemui.statusbar.phone.StatusBarIconList
    public void removeIcon(int i) {
        if (getIcon(i) == null) {
            return;
        }
        super.removeIcon(i);
        int viewIndex = getViewIndex(i);
        this.mStatusIcons.removeViewAt(viewIndex);
        this.mStatusIconsKeyguard.removeViewAt(viewIndex);
    }

    public void removeIcon(String str) {
        removeIcon(getSlotIndex(str));
    }

    public void setClockVisibility(boolean z) {
        this.mClock.setVisibility(z ? 0 : 8);
    }

    public void setExternalIcon(String str) {
        int viewIndex = getViewIndex(getSlotIndex(str));
        int dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(2131689794);
        ImageView imageView = (ImageView) this.mStatusIcons.getChildAt(viewIndex);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setAdjustViewBounds(true);
        setHeightAndCenter(imageView, dimensionPixelSize);
        ImageView imageView2 = (ImageView) this.mStatusIconsKeyguard.getChildAt(viewIndex);
        imageView2.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView2.setAdjustViewBounds(true);
        setHeightAndCenter(imageView2, dimensionPixelSize);
    }

    @Override // com.android.systemui.statusbar.phone.StatusBarIconList
    public void setIcon(int i, StatusBarIcon statusBarIcon) {
        if (statusBarIcon == null) {
            removeIcon(i);
            return;
        }
        boolean z = getIcon(i) == null;
        super.setIcon(i, statusBarIcon);
        if (z) {
            addSystemIcon(i, statusBarIcon);
        } else {
            handleSet(i, statusBarIcon);
        }
    }

    public void setIcon(String str, int i, CharSequence charSequence) {
        int slotIndex = getSlotIndex(str);
        StatusBarIcon icon = getIcon(slotIndex);
        if (icon == null) {
            setIcon(str, new StatusBarIcon(UserHandle.SYSTEM, this.mContext.getPackageName(), Icon.createWithResource(this.mContext, i), 0, 0, charSequence));
            return;
        }
        icon.icon = Icon.createWithResource(this.mContext, i);
        icon.contentDescription = charSequence;
        handleSet(slotIndex, icon);
    }

    public void setIcon(String str, StatusBarIcon statusBarIcon) {
        setIcon(getSlotIndex(str), statusBarIcon);
    }

    public void setIconVisibility(String str, boolean z) {
        int slotIndex = getSlotIndex(str);
        StatusBarIcon icon = getIcon(slotIndex);
        if (icon == null || icon.visible == z) {
            return;
        }
        icon.visible = z;
        handleSet(slotIndex, icon);
    }

    public void setIconsDark(boolean z, boolean z2) {
        float f = 1.0f;
        if (!z2) {
            if (!z) {
                f = 0.0f;
            }
            setIconTintInternal(f);
        } else if (this.mTransitionPending) {
            if (!z) {
                f = 0.0f;
            }
            deferIconTintChange(f);
        } else if (this.mTransitionDeferring) {
            float f2 = 0.0f;
            if (z) {
                f2 = 1.0f;
            }
            animateIconTint(f2, Math.max(0L, this.mTransitionDeferringStartTime - SystemClock.uptimeMillis()), this.mTransitionDeferringDuration);
        } else {
            float f3 = 0.0f;
            if (z) {
                f3 = 1.0f;
            }
            animateIconTint(f3, 0L, 120L);
        }
    }

    public void setIconsDarkArea(Rect rect) {
        if (rect == null && this.mTintArea.isEmpty()) {
            return;
        }
        if (rect == null) {
            this.mTintArea.setEmpty();
        } else {
            this.mTintArea.set(rect);
        }
        applyIconTint();
        this.mNotificationIconAreaController.setTintArea(rect);
    }

    public void setSignalCluster(SignalClusterView signalClusterView) {
        this.mSignalCluster = signalClusterView;
    }

    public void showNotificationIconArea(boolean z) {
        animateShow(this.mNotificationIconAreaInner, z);
    }

    public void showSystemIconArea(boolean z) {
        animateShow(this.mSystemIconArea, z);
    }

    public void updateNotificationIcons(NotificationData notificationData) {
        this.mNotificationIconAreaController.updateNotificationIcons(notificationData);
    }
}
