package com.android.systemui.statusbar.phone;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.BenesseExtension;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.IDockedStackListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import com.android.systemui.RecentsComponent;
import com.android.systemui.SystemUIApplication;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.statusbar.policy.DeadZone;
import com.mediatek.common.MPlugin;
import com.mediatek.multiwindow.IFreeformStackListener;
import com.mediatek.multiwindow.MultiWindowManager;
import com.mediatek.systemui.ext.DefaultNavigationBarPlugin;
import com.mediatek.systemui.ext.INavigationBarPlugin;
import java.io.FileDescriptor;
import java.io.PrintWriter;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/NavigationBarView.class */
public class NavigationBarView extends LinearLayout {
    private Drawable mBackAltCarModeIcon;
    private Drawable mBackAltIcon;
    private Drawable mBackAltLandCarModeIcon;
    private Drawable mBackAltLandIcon;
    private Drawable mBackCarModeIcon;
    private Drawable mBackIcon;
    private Drawable mBackLandCarModeIcon;
    private Drawable mBackLandIcon;
    private final NavigationBarTransitions mBarTransitions;
    private final SparseArray<ButtonDispatcher> mButtonDisatchers;
    private boolean mCarMode;
    private Configuration mConfiguration;
    View mCurrentView;
    private DeadZone mDeadZone;
    int mDisabledFlags;
    final Display mDisplay;
    private Drawable mDockedIcon;
    private boolean mDockedStackExists;
    private NavigationBarGestureHelper mGestureHelper;
    private H mHandler;
    private Drawable mHomeCarModeIcon;
    private Drawable mHomeDefaultIcon;
    private Drawable mImeIcon;
    private final View.OnClickListener mImeSwitcherClickListener;
    private KeyguardViewMediator mKeyguardViewMediator;
    private boolean mLayoutTransitionsEnabled;
    private Drawable mMenuIcon;
    private INavigationBarPlugin mNavBarPlugin;
    int mNavigationIconHints;
    private OnVerticalChangedListener mOnVerticalChangedListener;
    private Drawable mRecentIcon;
    private boolean mResizeMode;
    private Drawable mRestoreIcon;
    private boolean mRestoreShow;
    View[] mRotatedViews;
    boolean mScreenOn;
    boolean mShowMenu;
    private final NavTransitionListener mTransitionListener;
    boolean mVertical;
    private boolean mWakeAndUnlocking;

    /* renamed from: com.android.systemui.statusbar.phone.NavigationBarView$3  reason: invalid class name */
    /* loaded from: a.zip:com/android/systemui/statusbar/phone/NavigationBarView$3.class */
    class AnonymousClass3 extends IDockedStackListener.Stub {
        final NavigationBarView this$0;

        AnonymousClass3(NavigationBarView navigationBarView) {
            this.this$0 = navigationBarView;
        }

        public void onAdjustedForImeChanged(boolean z, long j) throws RemoteException {
        }

        public void onDividerVisibilityChanged(boolean z) throws RemoteException {
        }

        public void onDockSideChanged(int i) throws RemoteException {
        }

        public void onDockedStackExistsChanged(boolean z) throws RemoteException {
            this.this$0.mHandler.post(new Runnable(this, z) { // from class: com.android.systemui.statusbar.phone.NavigationBarView.3.1
                final AnonymousClass3 this$1;
                final boolean val$exists;

                {
                    this.this$1 = this;
                    this.val$exists = z;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$1.this$0.mDockedStackExists = this.val$exists;
                    this.this$1.this$0.updateRecentsIcon();
                }
            });
        }

        public void onDockedStackMinimizedChanged(boolean z, long j) throws RemoteException {
        }
    }

    /* renamed from: com.android.systemui.statusbar.phone.NavigationBarView$4  reason: invalid class name */
    /* loaded from: a.zip:com/android/systemui/statusbar/phone/NavigationBarView$4.class */
    class AnonymousClass4 extends IFreeformStackListener.Stub {
        final NavigationBarView this$0;

        AnonymousClass4(NavigationBarView navigationBarView) {
            this.this$0 = navigationBarView;
        }

        public void onShowRestoreButtonChanged(boolean z) throws RemoteException {
            this.this$0.mHandler.post(new Runnable(this, z) { // from class: com.android.systemui.statusbar.phone.NavigationBarView.4.1
                final AnonymousClass4 this$1;
                final boolean val$isShown;

                {
                    this.this$1 = this;
                    this.val$isShown = z;
                }

                @Override // java.lang.Runnable
                public void run() {
                    boolean z2 = false;
                    if (this.this$1.this$0.mKeyguardViewMediator != null) {
                        z2 = this.this$1.this$0.mKeyguardViewMediator.isShowing();
                    }
                    this.this$1.this$0.mRestoreShow = this.val$isShown;
                    NavigationBarView navigationBarView = this.this$1.this$0;
                    boolean z3 = false;
                    if (this.val$isShown) {
                        z3 = !z2;
                    }
                    navigationBarView.mResizeMode = z3;
                    this.this$1.this$0.updateRestoreIcon();
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/statusbar/phone/NavigationBarView$H.class */
    public class H extends Handler {
        final NavigationBarView this$0;

        private H(NavigationBarView navigationBarView) {
            this.this$0 = navigationBarView;
        }

        /* synthetic */ H(NavigationBarView navigationBarView, H h) {
            this(navigationBarView);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            switch (message.what) {
                case 8686:
                    String str = "" + message.obj;
                    int width = this.this$0.getWidth();
                    int height = this.this$0.getHeight();
                    int width2 = this.this$0.getCurrentView().getWidth();
                    int height2 = this.this$0.getCurrentView().getHeight();
                    if (height == height2 && width == width2) {
                        return;
                    }
                    Log.w("PhoneStatusBar/NavigationBarView", String.format("*** Invalid layout in navigation bar (%s this=%dx%d cur=%dx%d)", str, Integer.valueOf(width), Integer.valueOf(height), Integer.valueOf(width2), Integer.valueOf(height2)));
                    this.this$0.requestLayout();
                    return;
                default:
                    return;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/statusbar/phone/NavigationBarView$NavTransitionListener.class */
    public class NavTransitionListener implements LayoutTransition.TransitionListener {
        private boolean mBackTransitioning;
        private long mDuration;
        private boolean mHomeAppearing;
        private TimeInterpolator mInterpolator;
        private long mStartDelay;
        final NavigationBarView this$0;

        private NavTransitionListener(NavigationBarView navigationBarView) {
            this.this$0 = navigationBarView;
        }

        /* synthetic */ NavTransitionListener(NavigationBarView navigationBarView, NavTransitionListener navTransitionListener) {
            this(navigationBarView);
        }

        @Override // android.animation.LayoutTransition.TransitionListener
        public void endTransition(LayoutTransition layoutTransition, ViewGroup viewGroup, View view, int i) {
            if (view.getId() == 2131886255) {
                this.mBackTransitioning = false;
            } else if (view.getId() == 2131886099 && i == 2) {
                this.mHomeAppearing = false;
            }
        }

        public void onBackAltCleared() {
            ButtonDispatcher backButton = this.this$0.getBackButton();
            if (!this.mBackTransitioning && backButton.getVisibility() == 0 && this.mHomeAppearing && this.this$0.getHomeButton().getAlpha() == 0.0f) {
                this.this$0.getBackButton().setAlpha(0);
                ObjectAnimator ofFloat = ObjectAnimator.ofFloat(backButton, "alpha", 0.0f, 1.0f);
                ofFloat.setStartDelay(this.mStartDelay);
                ofFloat.setDuration(this.mDuration);
                ofFloat.setInterpolator(this.mInterpolator);
                ofFloat.start();
            }
        }

        @Override // android.animation.LayoutTransition.TransitionListener
        public void startTransition(LayoutTransition layoutTransition, ViewGroup viewGroup, View view, int i) {
            if (view.getId() == 2131886255) {
                this.mBackTransitioning = true;
            } else if (view.getId() == 2131886099 && i == 2) {
                this.mHomeAppearing = true;
                this.mStartDelay = layoutTransition.getStartDelay(i);
                this.mDuration = layoutTransition.getDuration(i);
                this.mInterpolator = layoutTransition.getInterpolator(i);
            }
        }
    }

    /* loaded from: a.zip:com/android/systemui/statusbar/phone/NavigationBarView$OnVerticalChangedListener.class */
    public interface OnVerticalChangedListener {
        void onVerticalChanged(boolean z);
    }

    public NavigationBarView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mCurrentView = null;
        this.mRotatedViews = new View[4];
        this.mDisabledFlags = 0;
        this.mNavigationIconHints = 0;
        this.mTransitionListener = new NavTransitionListener(this, null);
        this.mLayoutTransitionsEnabled = true;
        this.mCarMode = false;
        this.mButtonDisatchers = new SparseArray<>();
        this.mImeSwitcherClickListener = new View.OnClickListener(this) { // from class: com.android.systemui.statusbar.phone.NavigationBarView.1
            final NavigationBarView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                ((InputMethodManager) this.this$0.mContext.getSystemService(InputMethodManager.class)).showInputMethodPicker(true);
            }
        };
        this.mHandler = new H(this, null);
        this.mDisplay = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        this.mVertical = false;
        this.mShowMenu = false;
        this.mGestureHelper = new NavigationBarGestureHelper(context);
        this.mConfiguration = new Configuration();
        this.mConfiguration.updateFrom(context.getResources().getConfiguration());
        updateIcons(context, Configuration.EMPTY, this.mConfiguration);
        try {
            this.mNavBarPlugin = (INavigationBarPlugin) MPlugin.createInstance(INavigationBarPlugin.class.getName(), context);
        } catch (Exception e) {
            Log.e("PhoneStatusBar/NavigationBarView", "Catch INavigationBarPlugin exception: ", e);
        }
        if (this.mNavBarPlugin == null) {
            Log.d("PhoneStatusBar/NavigationBarView", "DefaultNavigationBarPlugin");
            this.mNavBarPlugin = new DefaultNavigationBarPlugin(context);
        }
        this.mBarTransitions = new NavigationBarTransitions(this);
        context.getContentResolver().registerContentObserver(Settings.System.getUriFor("dcha_state"), false, new ContentObserver(this, this.mHandler) { // from class: com.android.systemui.statusbar.phone.NavigationBarView.2
            final NavigationBarView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                this.this$0.setDisabledFlags(this.this$0.mDisabledFlags, true);
            }
        }, -1);
        this.mButtonDisatchers.put(2131886255, new ButtonDispatcher(2131886255));
        this.mButtonDisatchers.put(2131886099, new ButtonDispatcher(2131886099));
        this.mButtonDisatchers.put(2131886605, new ButtonDispatcher(2131886605));
        if (MultiWindowManager.isSupported()) {
            this.mButtonDisatchers.put(2131886638, new ButtonDispatcher(2131886638));
            this.mKeyguardViewMediator = (KeyguardViewMediator) ((SystemUIApplication) context).getComponent(KeyguardViewMediator.class);
        }
        this.mButtonDisatchers.put(2131886480, new ButtonDispatcher(2131886480));
        this.mButtonDisatchers.put(2131886481, new ButtonDispatcher(2131886481));
    }

    private static void dumpButton(PrintWriter printWriter, String str, ButtonDispatcher buttonDispatcher) {
        printWriter.print("      " + str + ": ");
        if (buttonDispatcher == null) {
            printWriter.print("null");
        } else {
            printWriter.print(visibilityToString(buttonDispatcher.getVisibility()) + " alpha=" + buttonDispatcher.getAlpha());
        }
        printWriter.println();
    }

    private Drawable getBackIcon(boolean z, boolean z2) {
        return z2 ? z ? this.mBackLandCarModeIcon : this.mBackLandIcon : z ? this.mBackCarModeIcon : this.mBackIcon;
    }

    private Drawable getBackIconWithAlt(boolean z, boolean z2) {
        return z2 ? z ? this.mBackAltLandCarModeIcon : this.mBackAltLandIcon : z ? this.mBackAltCarModeIcon : this.mBackAltIcon;
    }

    private String getResourceName(int i) {
        if (i != 0) {
            try {
                return getContext().getResources().getResourceName(i);
            } catch (Resources.NotFoundException e) {
                return "(unknown)";
            }
        }
        return "(null)";
    }

    private boolean inLockTask() {
        try {
            return ActivityManagerNative.getDefault().isInLockTaskMode();
        } catch (RemoteException e) {
            return false;
        }
    }

    private void notifyVerticalChangedListener(boolean z) {
        if (this.mOnVerticalChangedListener != null) {
            this.mOnVerticalChangedListener.onVerticalChanged(z);
        }
    }

    private void postCheckForInvalidLayout(String str) {
        this.mHandler.obtainMessage(8686, 0, 0, str).sendToTarget();
    }

    private void setUseFadingAnimations(boolean z) {
        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) getLayoutParams();
        if (layoutParams != null) {
            boolean z2 = layoutParams.windowAnimations != 0;
            if (!z2 && z) {
                layoutParams.windowAnimations = 2131952118;
            } else if (!z2 || z) {
                return;
            } else {
                layoutParams.windowAnimations = 0;
            }
            ((WindowManager) getContext().getSystemService("window")).updateViewLayout(this, layoutParams);
        }
    }

    private boolean updateCarMode(Configuration configuration) {
        boolean z = false;
        if (configuration != null) {
            int i = configuration.uiMode & 15;
            if (!this.mCarMode || i == 3) {
                z = false;
                if (i == 3) {
                    this.mCarMode = true;
                    z = true;
                }
            } else {
                this.mCarMode = false;
                z = true;
            }
        }
        return z;
    }

    private void updateCarModeIcons(Context context) {
        this.mBackCarModeIcon = context.getDrawable(2130837797);
        this.mBackLandCarModeIcon = this.mBackCarModeIcon;
        this.mBackAltCarModeIcon = context.getDrawable(2130837799);
        this.mBackAltLandCarModeIcon = this.mBackAltCarModeIcon;
        this.mHomeCarModeIcon = context.getDrawable(2130837802);
    }

    private void updateCurrentView() {
        int rotation = this.mDisplay.getRotation();
        for (int i = 0; i < 4; i++) {
            this.mRotatedViews[i].setVisibility(8);
        }
        this.mCurrentView = this.mRotatedViews[rotation];
        this.mCurrentView.setVisibility(0);
        for (int i2 = 0; i2 < this.mButtonDisatchers.size(); i2++) {
            this.mButtonDisatchers.valueAt(i2).setCurrentView(this.mCurrentView);
        }
        updateLayoutTransitionsEnabled();
    }

    private void updateIcons(Context context, Configuration configuration, Configuration configuration2) {
        if (configuration.orientation != configuration2.orientation || configuration.densityDpi != configuration2.densityDpi) {
            this.mDockedIcon = context.getDrawable(2130837800);
        }
        if (configuration.densityDpi != configuration2.densityDpi) {
            this.mBackIcon = context.getDrawable(2130837796);
            this.mBackLandIcon = this.mBackIcon;
            this.mBackAltIcon = context.getDrawable(2130837798);
            this.mBackAltLandIcon = this.mBackAltIcon;
            this.mHomeDefaultIcon = context.getDrawable(2130837801);
            this.mRecentIcon = context.getDrawable(2130837806);
            this.mMenuIcon = context.getDrawable(2130837805);
            this.mImeIcon = context.getDrawable(2130837661);
            if (MultiWindowManager.isSupported()) {
                this.mRestoreIcon = context.getDrawable(2130837807);
            }
            updateCarModeIcons(context);
        }
    }

    private void updateLayoutTransitionsEnabled() {
        boolean z = !this.mWakeAndUnlocking ? this.mLayoutTransitionsEnabled : false;
        LayoutTransition layoutTransition = ((ViewGroup) getCurrentView().findViewById(2131886266)).getLayoutTransition();
        if (layoutTransition != null) {
            if (z) {
                layoutTransition.enableTransitionType(2);
                layoutTransition.enableTransitionType(3);
                layoutTransition.enableTransitionType(0);
                layoutTransition.enableTransitionType(1);
                return;
            }
            layoutTransition.disableTransitionType(2);
            layoutTransition.disableTransitionType(3);
            layoutTransition.disableTransitionType(0);
            layoutTransition.disableTransitionType(1);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateRecentsIcon() {
        getRecentsButton().setImageDrawable(this.mNavBarPlugin.getRecentImage(this.mDockedStackExists ? this.mDockedIcon : this.mRecentIcon));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateRestoreIcon() {
        int i = 4;
        if (MultiWindowManager.DEBUG) {
            Log.d("PhoneStatusBar/NavigationBarView", "BMW, updateRestoreIcon, mResizeMode = " + this.mResizeMode);
        }
        getRestoreButton().setImageDrawable(this.mRestoreIcon);
        if (!this.mContext.getPackageManager().hasSystemFeature("android.software.freeform_window_management")) {
            getRestoreButton().setVisibility(4);
            return;
        }
        ButtonDispatcher restoreButton = getRestoreButton();
        if (this.mResizeMode) {
            i = 0;
        }
        restoreButton.setVisibility(i);
    }

    private void updateTaskSwitchHelper() {
        this.mGestureHelper.setBarState(this.mVertical, getLayoutDirection() == 1);
    }

    private static String visibilityToString(int i) {
        switch (i) {
            case 4:
                return "INVISIBLE";
            case 8:
                return "GONE";
            default:
                return "VISIBLE";
        }
    }

    public void abortCurrentGesture() {
        getHomeButton().abortCurrentGesture();
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("NavigationBarView {");
        Rect rect = new Rect();
        Point point = new Point();
        this.mDisplay.getRealSize(point);
        printWriter.println(String.format("      this: " + PhoneStatusBar.viewInfo(this) + " " + visibilityToString(getVisibility()), new Object[0]));
        getWindowVisibleDisplayFrame(rect);
        printWriter.println("      window: " + rect.toShortString() + " " + visibilityToString(getWindowVisibility()) + (rect.right > point.x || rect.bottom > point.y ? " OFFSCREEN!" : ""));
        printWriter.println(String.format("      mCurrentView: id=%s (%dx%d) %s", getResourceName(getCurrentView().getId()), Integer.valueOf(getCurrentView().getWidth()), Integer.valueOf(getCurrentView().getHeight()), visibilityToString(getCurrentView().getVisibility())));
        printWriter.println(String.format("      disabled=0x%08x vertical=%s menu=%s", Integer.valueOf(this.mDisabledFlags), this.mVertical ? "true" : "false", this.mShowMenu ? "true" : "false"));
        dumpButton(printWriter, "back", getBackButton());
        dumpButton(printWriter, "home", getHomeButton());
        dumpButton(printWriter, "rcnt", getRecentsButton());
        dumpButton(printWriter, "menu", getMenuButton());
        printWriter.println("    }");
    }

    public ButtonDispatcher getBackButton() {
        return this.mButtonDisatchers.get(2131886255);
    }

    public BarTransitions getBarTransitions() {
        return this.mBarTransitions;
    }

    public View getCurrentView() {
        return this.mCurrentView;
    }

    public ButtonDispatcher getHomeButton() {
        return this.mButtonDisatchers.get(2131886099);
    }

    public ButtonDispatcher getImeSwitchButton() {
        return this.mButtonDisatchers.get(2131886481);
    }

    public ButtonDispatcher getMenuButton() {
        return this.mButtonDisatchers.get(2131886480);
    }

    public ButtonDispatcher getRecentsButton() {
        return this.mButtonDisatchers.get(2131886605);
    }

    public ButtonDispatcher getRestoreButton() {
        return this.mButtonDisatchers.get(2131886638);
    }

    public void notifyScreenOn(boolean z) {
        this.mScreenOn = z;
        setDisabledFlags(this.mDisabledFlags, true);
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        boolean updateCarMode = updateCarMode(configuration);
        updateTaskSwitchHelper();
        updateIcons(getContext(), this.mConfiguration, configuration);
        updateRecentsIcon();
        if (updateCarMode || this.mConfiguration.densityDpi != configuration.densityDpi) {
            setNavigationIconHints(this.mNavigationIconHints, true);
        }
        this.mConfiguration.updateFrom(configuration);
    }

    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:9:0x0050 -> B:4:0x0034). Please submit an issue!!! */
    @Override // android.view.View
    public void onFinishInflate() {
        updateRotatedViews();
        ((NavigationBarInflaterView) findViewById(2131886538)).setButtonDispatchers(this.mButtonDisatchers);
        getImeSwitchButton().setOnClickListener(this.mImeSwitcherClickListener);
        try {
            WindowManagerGlobal.getWindowManagerService().registerDockedStackListener(new AnonymousClass3(this));
        } catch (RemoteException e) {
            Log.e("PhoneStatusBar/NavigationBarView", "Failed registering docked stack exists listener", e);
        }
        if (MultiWindowManager.isSupported()) {
            try {
                WindowManagerGlobal.getWindowManagerService().registerFreeformStackListener(new AnonymousClass4(this));
            } catch (RemoteException e2) {
                Log.e("PhoneStatusBar/NavigationBarView", "Failed registering freeform stack exists listener", e2);
            }
        }
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        return this.mGestureHelper.onInterceptTouchEvent(motionEvent);
    }

    @Override // android.view.View
    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        boolean z = i > 0 && i2 > i;
        if (z != this.mVertical) {
            this.mVertical = z;
            reorient();
            notifyVerticalChangedListener(z);
        }
        postCheckForInvalidLayout("sizeChanged");
        super.onSizeChanged(i, i2, i3, i4);
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (this.mGestureHelper.onTouchEvent(motionEvent)) {
            return true;
        }
        if (this.mDeadZone != null && motionEvent.getAction() == 4) {
            this.mDeadZone.poke(motionEvent);
        }
        return super.onTouchEvent(motionEvent);
    }

    public void reorient() {
        updateCurrentView();
        getImeSwitchButton().setOnClickListener(this.mImeSwitcherClickListener);
        this.mDeadZone = (DeadZone) this.mCurrentView.findViewById(2131886541);
        this.mBarTransitions.init();
        setDisabledFlags(this.mDisabledFlags, true);
        setMenuVisibility(this.mShowMenu, true);
        updateTaskSwitchHelper();
        setNavigationIconHints(this.mNavigationIconHints, true);
    }

    public void setComponents(RecentsComponent recentsComponent, Divider divider) {
        this.mGestureHelper.setComponents(recentsComponent, divider, this);
    }

    public void setDisabledFlags(int i) {
        setDisabledFlags(i, false);
    }

    public void setDisabledFlags(int i, boolean z) {
        int i2;
        LayoutTransition layoutTransition;
        if (z || this.mDisabledFlags != i) {
            this.mDisabledFlags = i;
            boolean z2 = (2097152 & i) != 0;
            boolean z3 = !this.mCarMode ? (16777216 & i) != 0 : true;
            boolean z4 = (4194304 & i) != 0 ? (this.mNavigationIconHints & 1) == 0 : false;
            boolean z5 = (33554432 & i) != 0;
            if (!z2 || !z3 || !z4) {
                z5 = false;
            }
            setSlippery(z5);
            ViewGroup viewGroup = (ViewGroup) getCurrentView().findViewById(2131886266);
            if (viewGroup != null && (layoutTransition = viewGroup.getLayoutTransition()) != null && !layoutTransition.getTransitionListeners().contains(this.mTransitionListener)) {
                layoutTransition.addTransitionListener(this.mTransitionListener);
            }
            boolean z6 = z3;
            if (inLockTask()) {
                z6 = z3;
                if (z3) {
                    z6 = z2 ? z3 : false;
                }
            }
            getBackButton().setVisibility(z4 ? 4 : 0);
            getHomeButton().setVisibility(z2 ? 4 : 0);
            ButtonDispatcher recentsButton = getRecentsButton();
            if (BenesseExtension.getDchaState() != 0) {
                i2 = 4;
            } else {
                i2 = 4;
                if (!z6) {
                    i2 = 0;
                }
            }
            recentsButton.setVisibility(i2);
            if (!MultiWindowManager.isSupported() || this.mKeyguardViewMediator == null) {
                return;
            }
            boolean isShowing = this.mKeyguardViewMediator.isShowing();
            boolean z7 = false;
            if (this.mRestoreShow) {
                z7 = !isShowing;
            }
            this.mResizeMode = z7;
            updateRestoreIcon();
        }
    }

    @Override // android.view.View
    public void setLayoutDirection(int i) {
        updateIcons(getContext(), Configuration.EMPTY, this.mConfiguration);
        super.setLayoutDirection(i);
    }

    public void setLayoutTransitionsEnabled(boolean z) {
        this.mLayoutTransitionsEnabled = z;
        updateLayoutTransitionsEnabled();
    }

    public void setMenuVisibility(boolean z) {
        setMenuVisibility(z, false);
    }

    public void setMenuVisibility(boolean z, boolean z2) {
        if (z2 || this.mShowMenu != z) {
            this.mShowMenu = z;
            getMenuButton().setVisibility(this.mShowMenu ? (this.mNavigationIconHints & 2) == 0 : false ? 0 : 4);
        }
    }

    public void setNavigationIconHints(int i) {
        setNavigationIconHints(i, false);
    }

    public void setNavigationIconHints(int i, boolean z) {
        if (z || i != this.mNavigationIconHints) {
            boolean z2 = (i & 1) != 0;
            if ((this.mNavigationIconHints & 1) != 0 && !z2) {
                this.mTransitionListener.onBackAltCleared();
            }
            this.mNavigationIconHints = i;
            getBackButton().setImageDrawable(this.mNavBarPlugin.getBackImage(z2 ? getBackIconWithAlt(this.mCarMode, this.mVertical) : getBackIcon(this.mCarMode, this.mVertical)));
            updateRecentsIcon();
            if (MultiWindowManager.isSupported()) {
                updateRestoreIcon();
            }
            if (this.mCarMode) {
                getHomeButton().setImageDrawable(this.mHomeCarModeIcon);
            } else {
                getHomeButton().setImageDrawable(this.mNavBarPlugin.getHomeImage(this.mHomeDefaultIcon));
            }
            getImeSwitchButton().setVisibility((i & 2) != 0 ? 0 : 4);
            getImeSwitchButton().setImageDrawable(this.mImeIcon);
            setMenuVisibility(this.mShowMenu, true);
            getMenuButton().setImageDrawable(this.mMenuIcon);
            setDisabledFlags(this.mDisabledFlags, true);
        }
    }

    public void setOnVerticalChangedListener(OnVerticalChangedListener onVerticalChangedListener) {
        this.mOnVerticalChangedListener = onVerticalChangedListener;
        notifyVerticalChangedListener(this.mVertical);
    }

    public void setSlippery(boolean z) {
        boolean z2 = false;
        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) getLayoutParams();
        if (layoutParams != null) {
            if ((layoutParams.flags & 536870912) != 0) {
                z2 = true;
            }
            if (!z2 && z) {
                layoutParams.flags |= 536870912;
            } else if (!z2 || z) {
                return;
            } else {
                layoutParams.flags &= -536870913;
            }
            ((WindowManager) getContext().getSystemService("window")).updateViewLayout(this, layoutParams);
        }
    }

    public void setWakeAndUnlocking(boolean z) {
        setUseFadingAnimations(z);
        this.mWakeAndUnlocking = z;
        updateLayoutTransitionsEnabled();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void updateRotatedViews() {
        View[] viewArr = this.mRotatedViews;
        View findViewById = findViewById(2131886532);
        this.mRotatedViews[2] = findViewById;
        viewArr[0] = findViewById;
        View[] viewArr2 = this.mRotatedViews;
        View findViewById2 = findViewById(2131886533);
        this.mRotatedViews[1] = findViewById2;
        viewArr2[3] = findViewById2;
        updateCurrentView();
    }
}
