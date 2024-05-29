package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.IApplicationThread;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.ProfilerInfo;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.IPackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.AsyncTask;
import android.os.BenesseExtension;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.ArraySet;
import android.util.DisplayMetrics;
import android.util.EventLog;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ThreadedRenderer;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.statusbar.NotificationVisibility;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.keyguard.KeyguardHostView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.R$id;
import com.android.keyguard.ViewMediatorCallback;
import com.android.systemui.AutoReinflateContainer;
import com.android.systemui.BatteryMeterView;
import com.android.systemui.DemoMode;
import com.android.systemui.EventLogTags;
import com.android.systemui.Interpolators;
import com.android.systemui.Prefs;
import com.android.systemui.SystemUIFactory;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.classifier.FalsingLog;
import com.android.systemui.classifier.FalsingManager;
import com.android.systemui.doze.DozeHost;
import com.android.systemui.doze.DozeLog;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.qs.QSContainer;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.ScreenPinningRequest;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.AppTransitionFinishedEvent;
import com.android.systemui.recents.events.activity.UndockingTaskEvent;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.stackdivider.WindowManagerProxy;
import com.android.systemui.statusbar.ActivatableNotificationView;
import com.android.systemui.statusbar.BackDropView;
import com.android.systemui.statusbar.BaseStatusBar;
import com.android.systemui.statusbar.DismissView;
import com.android.systemui.statusbar.DragDownHelper;
import com.android.systemui.statusbar.EmptyShadeView;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.GestureRecorder;
import com.android.systemui.statusbar.KeyboardShortcuts;
import com.android.systemui.statusbar.KeyguardIndicationController;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.NotificationOverflowContainer;
import com.android.systemui.statusbar.RemoteInputController;
import com.android.systemui.statusbar.ScrimView;
import com.android.systemui.statusbar.SignalClusterView;
import com.android.systemui.statusbar.phone.ActivityStarter;
import com.android.systemui.statusbar.phone.LockscreenWallpaper;
import com.android.systemui.statusbar.phone.NavigationBarView;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.statusbar.phone.UnlockMethodCache;
import com.android.systemui.statusbar.policy.AccessibilityController;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.BatteryControllerImpl;
import com.android.systemui.statusbar.policy.BluetoothControllerImpl;
import com.android.systemui.statusbar.policy.BrightnessMirrorController;
import com.android.systemui.statusbar.policy.CastControllerImpl;
import com.android.systemui.statusbar.policy.FlashlightController;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.HotspotControllerImpl;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
import com.android.systemui.statusbar.policy.KeyguardUserSwitcher;
import com.android.systemui.statusbar.policy.LocationControllerImpl;
import com.android.systemui.statusbar.policy.NetworkControllerImpl;
import com.android.systemui.statusbar.policy.NextAlarmController;
import com.android.systemui.statusbar.policy.PreviewInflater;
import com.android.systemui.statusbar.policy.RotationLockControllerImpl;
import com.android.systemui.statusbar.policy.SecurityControllerImpl;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.statusbar.stack.NotificationStackScrollLayout;
import com.android.systemui.volume.VolumeComponent;
import com.mediatek.multiwindow.MultiWindowManager;
import com.mediatek.systemui.PluginManager;
import com.mediatek.systemui.ext.IStatusBarPlmnPlugin;
import com.mediatek.systemui.statusbar.policy.HotKnotControllerImpl;
import com.mediatek.systemui.statusbar.util.FeatureOptions;
import com.mediatek.systemui.statusbar.util.SIMHelper;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/PhoneStatusBar.class */
public class PhoneStatusBar extends BaseStatusBar implements DemoMode, DragDownHelper.DragDownCallback, ActivityStarter, UnlockMethodCache.OnUnlockMethodChangedListener, HeadsUpManager.OnHeadsUpChangedListener {
    public static final Interpolator ALPHA_IN;
    public static final Interpolator ALPHA_OUT;
    private static final boolean FREEFORM_WINDOW_MANAGEMENT;
    private static final boolean ONLY_CORE_APPS;
    private static final AudioAttributes VIBRATION_ATTRIBUTES = new AudioAttributes.Builder().setContentType(4).setUsage(13).build();
    AccessibilityController mAccessibilityController;
    private boolean mAutohideSuspended;
    private BackDropView mBackdrop;
    private ImageView mBackdropBack;
    private ImageView mBackdropFront;
    protected BatteryController mBatteryController;
    BluetoothControllerImpl mBluetoothController;
    BrightnessMirrorController mBrightnessMirrorController;
    CastControllerImpl mCastController;
    private boolean mDemoMode;
    private boolean mDemoModeAllowed;
    private int mDisabledUnmodified1;
    private int mDisabledUnmodified2;
    Display mDisplay;
    protected DozeScrimController mDozeScrimController;
    private DozeServiceHost mDozeServiceHost;
    private boolean mDozing;
    private boolean mDozingRequested;
    private ExpandableNotificationRow mDraggedDownRow;
    View mExpandedContents;
    boolean mExpandedVisible;
    private FalsingManager mFalsingManager;
    FingerprintUnlockController mFingerprintUnlockController;
    FlashlightController mFlashlightController;
    private PowerManager.WakeLock mGestureWakeLock;
    private HandlerThread mHandlerThread;
    BaseStatusBarHeader mHeader;
    HotKnotControllerImpl mHotKnotController;
    HotspotControllerImpl mHotspotController;
    protected StatusBarIconController mIconController;
    PhoneStatusBarPolicy mIconPolicy;
    private int mInteractingWindows;
    KeyguardBottomAreaView mKeyguardBottomArea;
    private boolean mKeyguardFadingAway;
    private long mKeyguardFadingAwayDelay;
    private long mKeyguardFadingAwayDuration;
    private boolean mKeyguardGoingAway;
    KeyguardIndicationController mKeyguardIndicationController;
    protected KeyguardMonitor mKeyguardMonitor;
    protected KeyguardStatusBarView mKeyguardStatusBar;
    View mKeyguardStatusView;
    KeyguardUserSwitcher mKeyguardUserSwitcher;
    private ViewMediatorCallback mKeyguardViewMediatorCallback;
    private int mLastCameraLaunchSource;
    private int mLastLoggedStateFingerprint;
    private long mLastVisibilityReportUptimeMs;
    private NotificationListenerService.RankingMap mLatestRankingMap;
    private boolean mLaunchCameraOnFinishedGoingToSleep;
    private boolean mLaunchCameraOnScreenTurningOn;
    private Runnable mLaunchTransitionEndRunnable;
    private boolean mLaunchTransitionFadingAway;
    boolean mLeaveOpenOnKeyguardHide;
    LightStatusBarController mLightStatusBarController;
    LocationControllerImpl mLocationController;
    protected LockscreenWallpaper mLockscreenWallpaper;
    int mMaxAllowedKeyguardNotifications;
    private int mMaxKeyguardNotifications;
    private MediaController mMediaController;
    private MediaMetadata mMediaMetadata;
    private String mMediaNotificationKey;
    private MediaSessionManager mMediaSessionManager;
    private int mNavigationBarMode;
    NetworkControllerImpl mNetworkController;
    NextAlarmController mNextAlarmController;
    private boolean mNoAnimationOnNextBarModeChange;
    protected NotificationPanelView mNotificationPanel;
    private View mPendingRemoteInputView;
    private View mPendingWorkRemoteInputView;
    int mPixelFormat;
    private QSPanel mQSPanel;
    RotationLockControllerImpl mRotationLockController;
    private ScreenPinningRequest mScreenPinningRequest;
    private boolean mScreenTurningOn;
    protected ScrimController mScrimController;
    protected boolean mScrimSrcModeEnabled;
    SecurityControllerImpl mSecurityController;
    protected boolean mStartedGoingToSleep;
    private int mStatusBarMode;
    protected PhoneStatusBarView mStatusBarView;
    protected StatusBarWindowView mStatusBarWindow;
    protected StatusBarWindowManager mStatusBarWindowManager;
    boolean mTracking;
    int mTrackingPosition;
    private UnlockMethodCache mUnlockMethodCache;
    UserInfoController mUserInfoController;
    protected UserSwitcherController mUserSwitcherController;
    private Vibrator mVibrator;
    VolumeComponent mVolumeComponent;
    private boolean mWaitingForKeyguardExit;
    private boolean mWakeUpComingFromTouch;
    private PointF mWakeUpTouchLocation;
    protected ZenModeController mZenModeController;
    int mNaturalBarHeight = -1;
    Point mCurrentDisplaySize = new Point();
    private int mStatusBarWindowState = 0;
    Object mQueueLock = new Object();
    private int mNavigationBarWindowState = 0;
    int[] mAbsPos = new int[2];
    ArrayList<Runnable> mPostCollapseRunnables = new ArrayList<>();
    int mDisabled1 = 0;
    int mDisabled2 = 0;
    int mSystemUiVisibility = 0;
    private final Rect mLastFullscreenStackBounds = new Rect();
    private final Rect mLastDockedStackBounds = new Rect();
    private int mLastDispatchedSystemUiVisibility = -1;
    DisplayMetrics mDisplayMetrics = new DisplayMetrics();
    private final GestureRecorder mGestureRec = null;
    private int mNavigationIconHints = 0;
    private boolean mUserSetup = false;
    private ContentObserver mUserSetupObserver = new ContentObserver(this, new Handler()) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.1
        final PhoneStatusBar this$0;

        {
            this.this$0 = this;
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            boolean z2 = Settings.Secure.getIntForUser(this.this$0.mContext.getContentResolver(), "user_setup_complete", 0, this.this$0.mCurrentUserId) != 0;
            if (z2 != this.this$0.mUserSetup) {
                this.this$0.mUserSetup = z2;
                if (!this.this$0.mUserSetup && this.this$0.mStatusBarView != null) {
                    this.this$0.animateCollapseQuickSettings();
                }
                if (this.this$0.mKeyguardBottomArea != null) {
                    this.this$0.mKeyguardBottomArea.setUserSetupComplete(this.this$0.mUserSetup);
                }
                if (this.this$0.mNetworkController != null) {
                    this.this$0.mNetworkController.setUserSetupComplete(this.this$0.mUserSetup);
                }
            }
            if (this.this$0.mIconPolicy != null) {
                this.this$0.mIconPolicy.setCurrentUserSetup(this.this$0.mUserSetup);
            }
        }
    };
    private final ContentObserver mHeadsUpObserver = new ContentObserver(this, this.mHandler) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.2
        final PhoneStatusBar this$0;

        {
            this.this$0 = this;
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            boolean z2 = this.this$0.mUseHeadsUp;
            this.this$0.mUseHeadsUp = this.this$0.mDisableNotificationAlerts ? false : Settings.Global.getInt(this.this$0.mContext.getContentResolver(), "heads_up_notifications_enabled", 0) != 0;
            PhoneStatusBar phoneStatusBar = this.this$0;
            boolean z3 = false;
            if (this.this$0.mUseHeadsUp) {
                z3 = false;
                if (Settings.Global.getInt(this.this$0.mContext.getContentResolver(), "ticker_gets_heads_up", 0) != 0) {
                    z3 = true;
                }
            }
            phoneStatusBar.mHeadsUpTicker = z3;
            Log.d("PhoneStatusBar", "heads up is " + (this.this$0.mUseHeadsUp ? "enabled" : "disabled"));
            if (z2 == this.this$0.mUseHeadsUp || this.this$0.mUseHeadsUp) {
                return;
            }
            Log.d("PhoneStatusBar", "dismissing any existing heads up notification on disable event");
            this.this$0.mHeadsUpManager.releaseAllImmediately();
        }
    };
    private final Runnable mAutohide = new Runnable(this) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.3
        final PhoneStatusBar this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            int i = this.this$0.mSystemUiVisibility & (-201326593);
            if (this.this$0.mSystemUiVisibility != i) {
                this.this$0.notifyUiVisibilityChanged(i);
            }
        }
    };
    private PorterDuffXfermode mSrcXferMode = new PorterDuffXfermode(PorterDuff.Mode.SRC);
    private PorterDuffXfermode mSrcOverXferMode = new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER);
    private MediaController.Callback mMediaListener = new MediaController.Callback(this) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.4
        final PhoneStatusBar this$0;

        {
            this.this$0 = this;
        }

        @Override // android.media.session.MediaController.Callback
        public void onMetadataChanged(MediaMetadata mediaMetadata) {
            super.onMetadataChanged(mediaMetadata);
            this.this$0.mMediaMetadata = mediaMetadata;
            this.this$0.updateMediaMetaData(true, true);
        }

        @Override // android.media.session.MediaController.Callback
        public void onPlaybackStateChanged(PlaybackState playbackState) {
            super.onPlaybackStateChanged(playbackState);
            if (playbackState == null || this.this$0.isPlaybackActive(playbackState.getState())) {
                return;
            }
            this.this$0.clearCurrentMediaNotification();
            this.this$0.updateMediaMetaData(true, true);
        }
    };
    private final NotificationStackScrollLayout.OnChildLocationsChangedListener mOnChildLocationsChangedListener = new NotificationStackScrollLayout.OnChildLocationsChangedListener(this) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.5
        final PhoneStatusBar this$0;

        {
            this.this$0 = this;
        }

        @Override // com.android.systemui.statusbar.stack.NotificationStackScrollLayout.OnChildLocationsChangedListener
        public void onChildLocationsChanged(NotificationStackScrollLayout notificationStackScrollLayout) {
            this.this$0.userActivity();
        }
    };
    private final ArraySet<NotificationVisibility> mCurrentlyVisibleNotifications = new ArraySet<>();
    private final ShadeUpdates mShadeUpdates = new ShadeUpdates(this, null);
    private final NotificationStackScrollLayout.OnChildLocationsChangedListener mNotificationLocationsChangedListener = new NotificationStackScrollLayout.OnChildLocationsChangedListener(this) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.6
        final PhoneStatusBar this$0;

        {
            this.this$0 = this;
        }

        @Override // com.android.systemui.statusbar.stack.NotificationStackScrollLayout.OnChildLocationsChangedListener
        public void onChildLocationsChanged(NotificationStackScrollLayout notificationStackScrollLayout) {
            if (this.this$0.mHandler.hasCallbacks(this.this$0.mVisibilityReporter)) {
                return;
            }
            this.this$0.mHandler.postAtTime(this.this$0.mVisibilityReporter, this.this$0.mLastVisibilityReportUptimeMs + 500);
        }
    };
    private final Runnable mVisibilityReporter = new Runnable(this) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.7
        final PhoneStatusBar this$0;
        private final ArraySet<NotificationVisibility> mTmpNewlyVisibleNotifications = new ArraySet<>();
        private final ArraySet<NotificationVisibility> mTmpCurrentlyVisibleNotifications = new ArraySet<>();
        private final ArraySet<NotificationVisibility> mTmpNoLongerVisibleNotifications = new ArraySet<>();

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.this$0.mLastVisibilityReportUptimeMs = SystemClock.uptimeMillis();
            this.this$0.getCurrentMediaNotificationKey();
            ArrayList<NotificationData.Entry> activeNotifications = this.this$0.mNotificationData.getActiveNotifications();
            int size = activeNotifications.size();
            for (int i = 0; i < size; i++) {
                NotificationData.Entry entry = activeNotifications.get(i);
                String key = entry.notification.getKey();
                boolean z = (this.this$0.mStackScroller.getChildLocation(entry.row) & 5) != 0;
                NotificationVisibility obtain = NotificationVisibility.obtain(key, i, z);
                boolean contains = this.this$0.mCurrentlyVisibleNotifications.contains(obtain);
                if (z) {
                    this.mTmpCurrentlyVisibleNotifications.add(obtain);
                    if (!contains) {
                        this.mTmpNewlyVisibleNotifications.add(obtain);
                    }
                } else {
                    obtain.recycle();
                }
            }
            this.mTmpNoLongerVisibleNotifications.addAll(this.this$0.mCurrentlyVisibleNotifications);
            this.mTmpNoLongerVisibleNotifications.removeAll((ArraySet<? extends NotificationVisibility>) this.mTmpCurrentlyVisibleNotifications);
            this.this$0.logNotificationVisibilityChanges(this.mTmpNewlyVisibleNotifications, this.mTmpNoLongerVisibleNotifications);
            this.this$0.recycleAllVisibilityObjects(this.this$0.mCurrentlyVisibleNotifications);
            this.this$0.mCurrentlyVisibleNotifications.addAll((ArraySet) this.mTmpCurrentlyVisibleNotifications);
            this.this$0.recycleAllVisibilityObjects(this.mTmpNoLongerVisibleNotifications);
            this.mTmpCurrentlyVisibleNotifications.clear();
            this.mTmpNewlyVisibleNotifications.clear();
            this.mTmpNoLongerVisibleNotifications.clear();
        }
    };
    private final View.OnClickListener mOverflowClickListener = new View.OnClickListener(this) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.8
        final PhoneStatusBar this$0;

        {
            this.this$0 = this;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            this.this$0.goToLockedShade(null);
        }
    };
    private HashMap<ExpandableNotificationRow, List<ExpandableNotificationRow>> mTmpChildOrderMap = new HashMap<>();
    private View.OnClickListener mRecentsClickListener = new View.OnClickListener(this) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.9
        final PhoneStatusBar this$0;

        {
            this.this$0 = this;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            this.this$0.awakenDreams();
            this.this$0.toggleRecentApps();
        }
    };
    private View.OnClickListener mRestoreClickListener = new View.OnClickListener(this) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.10
        final PhoneStatusBar this$0;

        {
            this.this$0 = this;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            Log.d("PhoneStatusBar", "mRestoreClickListener");
            Recents.getSystemServices().restoreWindow();
        }
    };
    private View.OnLongClickListener mLongPressBackListener = new View.OnLongClickListener(this) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.11
        final PhoneStatusBar this$0;

        {
            this.this$0 = this;
        }

        @Override // android.view.View.OnLongClickListener
        public boolean onLongClick(View view) {
            return this.this$0.handleLongPressBack();
        }
    };
    private View.OnLongClickListener mRecentsLongClickListener = new View.OnLongClickListener(this) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.12
        final PhoneStatusBar this$0;

        {
            this.this$0 = this;
        }

        @Override // android.view.View.OnLongClickListener
        public boolean onLongClick(View view) {
            if (this.this$0.mRecents != null && ActivityManager.supportsMultiWindow() && ((Divider) this.this$0.getComponent(Divider.class)).getView().getSnapAlgorithm().isSplitScreenFeasible()) {
                this.this$0.toggleSplitScreenMode(271, 286);
                return true;
            }
            return false;
        }
    };
    private final View.OnLongClickListener mLongPressHomeListener = new View.OnLongClickListener(this) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.13
        final PhoneStatusBar this$0;

        {
            this.this$0 = this;
        }

        @Override // android.view.View.OnLongClickListener
        public boolean onLongClick(View view) {
            if (this.this$0.shouldDisableNavbarGestures()) {
                return false;
            }
            MetricsLogger.action(this.this$0.mContext, 239);
            this.this$0.mAssistManager.startAssist(new Bundle());
            this.this$0.awakenDreams();
            if (this.this$0.mNavigationBarView != null) {
                this.this$0.mNavigationBarView.abortCurrentGesture();
                return true;
            }
            return true;
        }
    };
    private final View.OnTouchListener mHomeActionListener = new View.OnTouchListener(this) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.14
        final PhoneStatusBar this$0;

        {
            this.this$0 = this;
        }

        @Override // android.view.View.OnTouchListener
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case 1:
                case 3:
                    this.this$0.awakenDreams();
                    return false;
                case 2:
                default:
                    return false;
            }
        }
    };
    private Runnable mHideBackdropFront = new Runnable(this) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.15
        final PhoneStatusBar this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.this$0.mBackdropFront.setVisibility(4);
            this.this$0.mBackdropFront.animate().cancel();
            this.this$0.mBackdropFront.setImageDrawable(null);
        }
    };
    private final Runnable mAnimateCollapsePanels = new Runnable(this) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.16
        final PhoneStatusBar this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.this$0.animateCollapsePanels();
        }
    };
    private final Runnable mCheckBarModes = new Runnable(this) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.17
        final PhoneStatusBar this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.this$0.checkBarModes();
        }
    };
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver(this) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.18
        final PhoneStatusBar this$0;

        {
            this.this$0 = this;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Log.v("PhoneStatusBar", "onReceive: " + intent);
            String action = intent.getAction();
            if (!"android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(action)) {
                if (!"android.intent.action.SCREEN_OFF".equals(action)) {
                    if ("android.intent.action.SCREEN_ON".equals(action)) {
                        this.this$0.notifyNavigationBarScreenOn(true);
                        return;
                    }
                    return;
                }
                this.this$0.notifyNavigationBarScreenOn(false);
                this.this$0.notifyHeadsUpScreenOff();
                this.this$0.finishBarAnimations();
                this.this$0.resetUserExpandedStates();
                return;
            }
            KeyboardShortcuts.dismiss();
            if (this.this$0.mRemoteInputController != null) {
                this.this$0.mRemoteInputController.closeRemoteInputs();
            }
            if (this.this$0.isCurrentProfile(getSendingUserId())) {
                String stringExtra = intent.getStringExtra("reason");
                int i = 0;
                if (stringExtra != null) {
                    i = 0;
                    if (stringExtra.equals("recentapps")) {
                        i = 2;
                    }
                }
                this.this$0.animateCollapsePanels(i);
            }
        }
    };
    private BroadcastReceiver mDemoReceiver = new BroadcastReceiver(this) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.19
        final PhoneStatusBar this$0;

        {
            this.this$0 = this;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Log.v("PhoneStatusBar", "onReceive: " + intent);
            String action = intent.getAction();
            if (!"com.android.systemui.demo".equals(action)) {
                if ("fake_artwork".equals(action)) {
                }
                return;
            }
            Bundle extras = intent.getExtras();
            if (extras != null) {
                String lowerCase = extras.getString("command", "").trim().toLowerCase();
                if (lowerCase.length() > 0) {
                    try {
                        this.this$0.dispatchDemoCommand(lowerCase, extras);
                    } catch (Throwable th) {
                        Log.w("PhoneStatusBar", "Error running demo command, intent=" + intent, th);
                    }
                }
            }
        }
    };
    Runnable mStartTracing = new Runnable(this) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.20
        final PhoneStatusBar this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.this$0.vibrate();
            SystemClock.sleep(250L);
            Log.d("PhoneStatusBar", "startTracing");
            Debug.startMethodTracing("/data/statusbar-traces/trace");
            this.this$0.mHandler.postDelayed(this.this$0.mStopTracing, 10000L);
        }
    };
    Runnable mStopTracing = new Runnable(this) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.21
        final PhoneStatusBar this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            Debug.stopMethodTracing();
            Log.d("PhoneStatusBar", "stopTracing");
            this.this$0.vibrate();
        }
    };
    private IStatusBarPlmnPlugin mStatusBarPlmnPlugin = null;
    private View mCustomizeCarrierLabel = null;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.android.systemui.statusbar.phone.PhoneStatusBar$31  reason: invalid class name */
    /* loaded from: a.zip:com/android/systemui/statusbar/phone/PhoneStatusBar$31.class */
    public class AnonymousClass31 implements RemoteInputController.Callback {
        final PhoneStatusBar this$0;

        AnonymousClass31(PhoneStatusBar phoneStatusBar) {
            this.this$0 = phoneStatusBar;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        /* renamed from: -com_android_systemui_statusbar_phone_PhoneStatusBar$31_lambda$1  reason: not valid java name */
        public /* synthetic */ void m1736com_android_systemui_statusbar_phone_PhoneStatusBar$31_lambda$1(NotificationData.Entry entry) {
            if (this.this$0.mRemoteInputEntriesToRemoveOnCollapse.remove(entry)) {
                this.this$0.removeNotification(entry.key, null);
            }
        }

        @Override // com.android.systemui.statusbar.RemoteInputController.Callback
        public void onRemoteInputSent(final NotificationData.Entry entry) {
            if (PhoneStatusBar.FORCE_REMOTE_INPUT_HISTORY && this.this$0.mKeysKeptForRemoteInput.contains(entry.key)) {
                this.this$0.removeNotification(entry.key, null);
            } else if (this.this$0.mRemoteInputEntriesToRemoveOnCollapse.contains(entry)) {
                this.this$0.mHandler.postDelayed(new Runnable(this, entry) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar$31$_void_onRemoteInputSent_com_android_systemui_statusbar_NotificationData$Entry_entry_LambdaImpl0
                    private NotificationData.Entry val$entry;
                    private PhoneStatusBar.AnonymousClass31 val$this;

                    {
                        this.val$this = this;
                        this.val$entry = entry;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.val$this.m1736com_android_systemui_statusbar_phone_PhoneStatusBar$31_lambda$1(this.val$entry);
                    }
                }, 200L);
            }
        }
    }

    /* renamed from: com.android.systemui.statusbar.phone.PhoneStatusBar$44  reason: invalid class name */
    /* loaded from: a.zip:com/android/systemui/statusbar/phone/PhoneStatusBar$44.class */
    class AnonymousClass44 implements Runnable {
        final PhoneStatusBar this$0;
        final Runnable val$beforeFading;

        AnonymousClass44(PhoneStatusBar phoneStatusBar, Runnable runnable) {
            this.this$0 = phoneStatusBar;
            this.val$beforeFading = runnable;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.this$0.mLaunchTransitionFadingAway = true;
            if (this.val$beforeFading != null) {
                this.val$beforeFading.run();
            }
            this.this$0.mScrimController.forceHideScrims(true);
            this.this$0.updateMediaMetaData(false, true);
            this.this$0.mNotificationPanel.setAlpha(1.0f);
            this.this$0.mStackScroller.setParentFadingOut(true);
            this.this$0.mNotificationPanel.animate().alpha(0.0f).setStartDelay(100L).setDuration(300L).withLayer().withEndAction(new Runnable(this) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.44.1
                final AnonymousClass44 this$1;

                {
                    this.this$1 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$1.this$0.onLaunchTransitionFadingEnded();
                }
            });
            this.this$0.mIconController.appTransitionStarting(SystemClock.uptimeMillis(), 120L);
        }
    }

    /* renamed from: com.android.systemui.statusbar.phone.PhoneStatusBar$46  reason: invalid class name */
    /* loaded from: a.zip:com/android/systemui/statusbar/phone/PhoneStatusBar$46.class */
    class AnonymousClass46 implements Runnable {
        final PhoneStatusBar this$0;

        /* renamed from: com.android.systemui.statusbar.phone.PhoneStatusBar$46$1  reason: invalid class name */
        /* loaded from: a.zip:com/android/systemui/statusbar/phone/PhoneStatusBar$46$1.class */
        class AnonymousClass1 implements Runnable {
            final AnonymousClass46 this$1;
            final ExpandableNotificationRow val$row;
            final NotificationStackScrollLayout val$scrollLayout;

            AnonymousClass1(AnonymousClass46 anonymousClass46, NotificationStackScrollLayout notificationStackScrollLayout, ExpandableNotificationRow expandableNotificationRow) {
                this.this$1 = anonymousClass46;
                this.val$scrollLayout = notificationStackScrollLayout;
                this.val$row = expandableNotificationRow;
            }

            @Override // java.lang.Runnable
            public void run() {
                Runnable runnable = new Runnable(this, this.val$scrollLayout) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.46.1.1
                    final AnonymousClass1 this$2;
                    final NotificationStackScrollLayout val$scrollLayout;

                    {
                        this.this$2 = this;
                        this.val$scrollLayout = r5;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.this$2.this$1.this$0.mPendingWorkRemoteInputView.callOnClick();
                        this.this$2.this$1.this$0.mPendingWorkRemoteInputView = null;
                        this.val$scrollLayout.setFinishScrollingCallback(null);
                    }
                };
                if (this.val$scrollLayout.scrollTo(this.val$row)) {
                    this.val$scrollLayout.setFinishScrollingCallback(runnable);
                } else {
                    runnable.run();
                }
            }
        }

        AnonymousClass46(PhoneStatusBar phoneStatusBar) {
            this.this$0 = phoneStatusBar;
        }

        @Override // java.lang.Runnable
        public void run() {
            if (this.this$0.mPendingWorkRemoteInputView == null) {
                return;
            }
            ViewParent parent = this.this$0.mPendingWorkRemoteInputView.getParent();
            while (true) {
                ViewParent viewParent = parent;
                if (viewParent == null) {
                    return;
                }
                if (viewParent instanceof ExpandableNotificationRow) {
                    ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) viewParent;
                    ViewParent parent2 = expandableNotificationRow.getParent();
                    if (parent2 instanceof NotificationStackScrollLayout) {
                        expandableNotificationRow.makeActionsVisibile();
                        expandableNotificationRow.post(new AnonymousClass1(this, (NotificationStackScrollLayout) parent2, expandableNotificationRow));
                        return;
                    }
                    return;
                }
                parent = viewParent.getParent();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/statusbar/phone/PhoneStatusBar$DozeServiceHost.class */
    public final class DozeServiceHost extends KeyguardUpdateMonitorCallback implements DozeHost {
        private final ArrayList<DozeHost.Callback> mCallbacks;
        private final H mHandler;
        private boolean mNotificationLightOn;
        final PhoneStatusBar this$0;

        /* loaded from: a.zip:com/android/systemui/statusbar/phone/PhoneStatusBar$DozeServiceHost$H.class */
        private final class H extends Handler {
            final DozeServiceHost this$1;

            private H(DozeServiceHost dozeServiceHost) {
                this.this$1 = dozeServiceHost;
            }

            /* synthetic */ H(DozeServiceHost dozeServiceHost, H h) {
                this(dozeServiceHost);
            }

            @Override // android.os.Handler
            public void handleMessage(Message message) {
                switch (message.what) {
                    case 1:
                        this.this$1.handleStartDozing((Runnable) message.obj);
                        return;
                    case 2:
                        this.this$1.handlePulseWhileDozing((DozeHost.PulseCallback) message.obj, message.arg1);
                        return;
                    case 3:
                        this.this$1.handleStopDozing();
                        return;
                    default:
                        return;
                }
            }
        }

        private DozeServiceHost(PhoneStatusBar phoneStatusBar) {
            this.this$0 = phoneStatusBar;
            this.mCallbacks = new ArrayList<>();
            this.mHandler = new H(this, null);
        }

        /* synthetic */ DozeServiceHost(PhoneStatusBar phoneStatusBar, DozeServiceHost dozeServiceHost) {
            this(phoneStatusBar);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void handlePulseWhileDozing(DozeHost.PulseCallback pulseCallback, int i) {
            this.this$0.mDozeScrimController.pulse(new DozeHost.PulseCallback(this, pulseCallback) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.DozeServiceHost.1
                final DozeServiceHost this$1;
                final DozeHost.PulseCallback val$callback;

                {
                    this.this$1 = this;
                    this.val$callback = pulseCallback;
                }

                @Override // com.android.systemui.doze.DozeHost.PulseCallback
                public void onPulseFinished() {
                    this.val$callback.onPulseFinished();
                    this.this$1.this$0.mStackScroller.setPulsing(false);
                }

                @Override // com.android.systemui.doze.DozeHost.PulseCallback
                public void onPulseStarted() {
                    this.val$callback.onPulseStarted();
                    this.this$1.this$0.mStackScroller.setPulsing(true);
                }
            }, i);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void handleStartDozing(Runnable runnable) {
            if (!this.this$0.mDozingRequested) {
                this.this$0.mDozingRequested = true;
                DozeLog.traceDozing(this.this$0.mContext, this.this$0.mDozing);
                this.this$0.updateDozing();
            }
            runnable.run();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void handleStopDozing() {
            if (this.this$0.mDozingRequested) {
                this.this$0.mDozingRequested = false;
                DozeLog.traceDozing(this.this$0.mContext, this.this$0.mDozing);
                this.this$0.updateDozing();
            }
        }

        @Override // com.android.systemui.doze.DozeHost
        public void addCallback(DozeHost.Callback callback) {
            this.mCallbacks.add(callback);
        }

        public void fireBuzzBeepBlinked() {
            for (DozeHost.Callback callback : this.mCallbacks) {
                callback.onBuzzBeepBlinked();
            }
        }

        public void fireNewNotifications() {
            for (DozeHost.Callback callback : this.mCallbacks) {
                callback.onNewNotifications();
            }
        }

        public void fireNotificationLight(boolean z) {
            this.mNotificationLightOn = z;
            for (DozeHost.Callback callback : this.mCallbacks) {
                callback.onNotificationLight(z);
            }
        }

        public void firePowerSaveChanged(boolean z) {
            for (DozeHost.Callback callback : this.mCallbacks) {
                callback.onPowerSaveChanged(z);
            }
        }

        @Override // com.android.systemui.doze.DozeHost
        public boolean isNotificationLightOn() {
            return this.mNotificationLightOn;
        }

        @Override // com.android.systemui.doze.DozeHost
        public boolean isPowerSaveActive() {
            return this.this$0.mBatteryController != null ? this.this$0.mBatteryController.isPowerSave() : false;
        }

        @Override // com.android.systemui.doze.DozeHost
        public boolean isPulsingBlocked() {
            boolean z = true;
            if (this.this$0.mFingerprintUnlockController.getMode() != 1) {
                z = false;
            }
            return z;
        }

        @Override // com.android.systemui.doze.DozeHost
        public void pulseWhileDozing(DozeHost.PulseCallback pulseCallback, int i) {
            this.mHandler.obtainMessage(2, i, 0, pulseCallback).sendToTarget();
        }

        @Override // com.android.systemui.doze.DozeHost
        public void removeCallback(DozeHost.Callback callback) {
            this.mCallbacks.remove(callback);
        }

        @Override // com.android.systemui.doze.DozeHost
        public void startDozing(Runnable runnable) {
            this.mHandler.obtainMessage(1, runnable).sendToTarget();
        }

        @Override // com.android.systemui.doze.DozeHost
        public void stopDozing() {
            this.mHandler.obtainMessage(3).sendToTarget();
        }

        public String toString() {
            return "PSB.DozeServiceHost[mCallbacks=" + this.mCallbacks.size() + "]";
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/statusbar/phone/PhoneStatusBar$FastColorDrawable.class */
    public static class FastColorDrawable extends Drawable {
        private final int mColor;

        public FastColorDrawable(int i) {
            this.mColor = (-16777216) | i;
        }

        @Override // android.graphics.drawable.Drawable
        public void draw(Canvas canvas) {
            canvas.drawColor(this.mColor, PorterDuff.Mode.SRC);
        }

        @Override // android.graphics.drawable.Drawable
        public int getOpacity() {
            return -1;
        }

        @Override // android.graphics.drawable.Drawable
        public void setAlpha(int i) {
        }

        @Override // android.graphics.drawable.Drawable
        public void setBounds(int i, int i2, int i3, int i4) {
        }

        @Override // android.graphics.drawable.Drawable
        public void setBounds(Rect rect) {
        }

        @Override // android.graphics.drawable.Drawable
        public void setColorFilter(ColorFilter colorFilter) {
        }
    }

    /* loaded from: a.zip:com/android/systemui/statusbar/phone/PhoneStatusBar$H.class */
    private class H extends BaseStatusBar.H {
        final PhoneStatusBar this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        private H(PhoneStatusBar phoneStatusBar) {
            super(phoneStatusBar);
            this.this$0 = phoneStatusBar;
        }

        /* synthetic */ H(PhoneStatusBar phoneStatusBar, H h) {
            this(phoneStatusBar);
        }

        @Override // com.android.systemui.statusbar.BaseStatusBar.H, android.os.Handler
        public void handleMessage(Message message) {
            super.handleMessage(message);
            switch (message.what) {
                case 1000:
                    this.this$0.animateExpandNotificationsPanel();
                    return;
                case 1001:
                    this.this$0.animateCollapsePanels();
                    return;
                case 1002:
                    this.this$0.animateExpandSettingsPanel((String) message.obj);
                    return;
                case 1003:
                    this.this$0.onLaunchTransitionTimeout();
                    return;
                default:
                    return;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/statusbar/phone/PhoneStatusBar$ShadeUpdates.class */
    public final class ShadeUpdates {
        private final ArraySet<String> mNewVisibleNotifications;
        private final ArraySet<String> mVisibleNotifications;
        final PhoneStatusBar this$0;

        private ShadeUpdates(PhoneStatusBar phoneStatusBar) {
            this.this$0 = phoneStatusBar;
            this.mVisibleNotifications = new ArraySet<>();
            this.mNewVisibleNotifications = new ArraySet<>();
        }

        /* synthetic */ ShadeUpdates(PhoneStatusBar phoneStatusBar, ShadeUpdates shadeUpdates) {
            this(phoneStatusBar);
        }

        public void check() {
            this.mNewVisibleNotifications.clear();
            ArrayList<NotificationData.Entry> activeNotifications = this.this$0.mNotificationData.getActiveNotifications();
            for (int i = 0; i < activeNotifications.size(); i++) {
                NotificationData.Entry entry = activeNotifications.get(i);
                if (entry.row != null ? entry.row.getVisibility() == 0 : false) {
                    this.mNewVisibleNotifications.add(entry.key + entry.notification.getPostTime());
                }
            }
            boolean z = !this.mVisibleNotifications.containsAll(this.mNewVisibleNotifications);
            this.mVisibleNotifications.clear();
            this.mVisibleNotifications.addAll((ArraySet<? extends String>) this.mNewVisibleNotifications);
            if (!z || this.this$0.mDozeServiceHost == null) {
                return;
            }
            this.this$0.mDozeServiceHost.fireNewNotifications();
        }
    }

    static {
        boolean z;
        boolean z2;
        try {
            IPackageManager asInterface = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
            z = asInterface.isOnlyCoreApps();
            z2 = asInterface.hasSystemFeature("android.software.freeform_window_management", 0);
        } catch (RemoteException e) {
            z = false;
            z2 = false;
        }
        ONLY_CORE_APPS = z;
        FREEFORM_WINDOW_MANAGEMENT = z2;
        ALPHA_IN = Interpolators.ALPHA_IN;
        ALPHA_OUT = Interpolators.ALPHA_OUT;
    }

    private void addNotificationChildrenAndSort() {
        boolean z = false;
        for (int i = 0; i < this.mStackScroller.getChildCount(); i++) {
            View childAt = this.mStackScroller.getChildAt(i);
            if (childAt instanceof ExpandableNotificationRow) {
                ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) childAt;
                List<ExpandableNotificationRow> notificationChildren = expandableNotificationRow.getNotificationChildren();
                List<ExpandableNotificationRow> list = this.mTmpChildOrderMap.get(expandableNotificationRow);
                for (int i2 = 0; list != null && i2 < list.size(); i2++) {
                    ExpandableNotificationRow expandableNotificationRow2 = list.get(i2);
                    if (notificationChildren == null || !notificationChildren.contains(expandableNotificationRow2)) {
                        expandableNotificationRow.addChildNotification(expandableNotificationRow2, i2);
                        this.mStackScroller.notifyGroupChildAdded(expandableNotificationRow2);
                    }
                }
                z |= expandableNotificationRow.applyChildOrder(list);
            }
        }
        if (z) {
            this.mStackScroller.generateChildOrderChangedEvent();
        }
    }

    private void addStatusBarWindow() {
        makeStatusBarView();
        this.mStatusBarWindowManager = new StatusBarWindowManager(this.mContext);
        this.mRemoteInputController = new RemoteInputController(this.mStatusBarWindowManager, this.mHeadsUpManager);
        this.mStatusBarWindowManager.add(this.mStatusBarWindow, getStatusBarHeight());
    }

    private boolean areLightsOn() {
        boolean z = false;
        if ((this.mSystemUiVisibility & 1) == 0) {
            z = true;
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void awakenDreams() {
        if (this.mDreamManager != null) {
            try {
                this.mDreamManager.awaken();
            } catch (RemoteException e) {
            }
        }
    }

    private int barMode(int i, int i2, int i3, int i4) {
        int i5 = i4 | 1;
        return (i & i2) != 0 ? 1 : (i & i3) != 0 ? 2 : (i & i5) == i5 ? 6 : (i & i4) != 0 ? 4 : (i & 1) != 0 ? 3 : 0;
    }

    private void cancelAutohide() {
        this.mAutohideSuspended = false;
        this.mHandler.removeCallbacks(this.mAutohide);
    }

    /* JADX WARN: Code restructure failed: missing block: B:33:0x008a, code lost:
        if (r6 == 4) goto L28;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private void checkBarMode(int i, int i2, BarTransitions barTransitions, boolean z) {
        boolean isPowerSave = this.mBatteryController.isPowerSave();
        boolean z2 = (z || !this.mDeviceInteractive || i2 == 2) ? false : !isPowerSave;
        int i3 = i;
        if (isPowerSave) {
            i3 = i;
            if (getBarState() == 0) {
                i3 = 5;
            }
        }
        boolean z3 = z2;
        int i4 = i3;
        if (FeatureOptions.LOW_RAM_SUPPORT) {
            if (ActivityManager.isHighEndGfx()) {
                i4 = i3;
                z3 = z2;
            } else {
                z3 = z2;
                i4 = i3;
                if (getBarState() != 1) {
                    if (i3 != 1 && i3 != 2) {
                        z3 = z2;
                        i4 = i3;
                    }
                    i4 = 0;
                    z3 = false;
                }
            }
        }
        barTransitions.transitionTo(i4, z3);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void checkBarModes() {
        if (this.mDemoMode) {
            return;
        }
        checkBarMode(this.mStatusBarMode, this.mStatusBarWindowState, this.mStatusBarView.getBarTransitions(), this.mNoAnimationOnNextBarModeChange);
        if (this.mNavigationBarView != null) {
            checkBarMode(this.mNavigationBarMode, this.mNavigationBarWindowState, this.mNavigationBarView.getBarTransitions(), this.mNoAnimationOnNextBarModeChange);
        }
        this.mNoAnimationOnNextBarModeChange = false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void checkUserAutohide(View view, MotionEvent motionEvent) {
        if ((this.mSystemUiVisibility & 201326592) != 0 && motionEvent.getAction() == 4 && motionEvent.getX() == 0.0f && motionEvent.getY() == 0.0f && !this.mRemoteInputController.isRemoteInputActive()) {
            userAutohide();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void clearCurrentMediaNotification() {
        this.mMediaNotificationKey = null;
        this.mMediaMetadata = null;
        if (this.mMediaController != null) {
            this.mMediaController.unregisterCallback(this.mMediaListener);
        }
        this.mMediaController = null;
    }

    private int computeBarMode(int i, int i2, BarTransitions barTransitions, int i3, int i4, int i5) {
        int barMode = barMode(i, i3, i4, i5);
        int barMode2 = barMode(i2, i3, i4, i5);
        if (barMode == barMode2) {
            return -1;
        }
        return barMode2;
    }

    private void dismissKeyguardThenExecute(KeyguardHostView.OnDismissAction onDismissAction, Runnable runnable, boolean z) {
        if (this.mStatusBarKeyguardViewManager.isShowing()) {
            this.mStatusBarKeyguardViewManager.dismissWithAction(onDismissAction, runnable, z);
        } else {
            onDismissAction.onDismiss();
        }
    }

    private void dismissVolumeDialog() {
        if (this.mVolumeComponent != null) {
            this.mVolumeComponent.dismissNow();
        }
    }

    private void dispatchDemoCommandToView(String str, Bundle bundle, int i) {
        if (this.mStatusBarView == null) {
            return;
        }
        View findViewById = this.mStatusBarView.findViewById(i);
        if (findViewById instanceof DemoMode) {
            ((DemoMode) findViewById).dispatchDemoCommand(str, bundle);
        }
    }

    private static void dumpBarTransitions(PrintWriter printWriter, String str, BarTransitions barTransitions) {
        printWriter.print("  ");
        printWriter.print(str);
        printWriter.print(".BarTransitions.mMode=");
        printWriter.println(BarTransitions.modeToString(barTransitions.getMode()));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void finishBarAnimations() {
        this.mStatusBarView.getBarTransitions().finishAnimations();
        if (this.mNavigationBarView != null) {
            this.mNavigationBarView.getBarTransitions().finishAnimations();
        }
    }

    private static int getLoggingFingerprint(int i, boolean z, boolean z2, boolean z3, boolean z4, boolean z5) {
        int i2 = 1;
        int i3 = z ? 1 : 0;
        int i4 = z2 ? 1 : 0;
        int i5 = z3 ? 1 : 0;
        int i6 = z4 ? 1 : 0;
        if (!z5) {
            i2 = 0;
        }
        return (i2 << 12) | (i6 << 11) | (i & 255) | (i3 << 8) | (i4 << 9) | (i5 << 10);
    }

    private int getMediaControllerPlaybackState(MediaController mediaController) {
        PlaybackState playbackState;
        if (mediaController == null || (playbackState = mediaController.getPlaybackState()) == null) {
            return 0;
        }
        return playbackState.getState();
    }

    private WindowManager.LayoutParams getNavigationBarLayoutParams() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, -1, 2019, 8650856, -3);
        if (ActivityManager.isHighEndGfx()) {
            layoutParams.flags |= 16777216;
        }
        layoutParams.setTitle("NavigationBar");
        layoutParams.windowAnimations = 0;
        return layoutParams;
    }

    private void handleGroupSummaryRemoved(String str, NotificationListenerService.RankingMap rankingMap) {
        NotificationData.Entry entry = this.mNotificationData.get(str);
        if (entry == null || entry.row == null || !entry.row.isSummaryWithChildren()) {
            return;
        }
        if (entry.notification.getOverrideGroupKey() == null || entry.row.isDismissed()) {
            ArrayList arrayList = new ArrayList(entry.row.getNotificationChildren());
            for (int i = 0; i < arrayList.size(); i++) {
                ((ExpandableNotificationRow) arrayList.get(i)).setKeepInParent(true);
                ((ExpandableNotificationRow) arrayList.get(i)).setRemoved();
            }
            for (int i2 = 0; i2 < arrayList.size(); i2++) {
                removeNotification(((ExpandableNotificationRow) arrayList.get(i2)).getStatusBarNotification().getKey(), rankingMap);
                this.mStackScroller.removeViewStateForView((View) arrayList.get(i2));
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean handleLongPressBack() {
        try {
            IActivityManager iActivityManager = ActivityManagerNative.getDefault();
            if (iActivityManager.isInLockTaskMode()) {
                iActivityManager.stopSystemLockTaskMode();
                this.mNavigationBarView.setDisabledFlags(this.mDisabled1, true);
                return true;
            }
            return false;
        } catch (RemoteException e) {
            Log.d("PhoneStatusBar", "Unable to reach activity manager", e);
            return false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleStartActivityDismissingKeyguard(Intent intent, boolean z) {
        startActivityDismissingKeyguard(intent, z, true);
    }

    private void inflateDismissView() {
        this.mDismissView = (DismissView) LayoutInflater.from(this.mContext).inflate(2130968814, (ViewGroup) this.mStackScroller, false);
        this.mDismissView.setOnButtonClickListener(new View.OnClickListener(this) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.26
            final PhoneStatusBar this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MetricsLogger.action(this.this$0.mContext, 148);
                this.this$0.clearAllNotifications();
            }
        });
        this.mStackScroller.setDismissView(this.mDismissView);
    }

    private void inflateEmptyShadeView() {
        this.mEmptyShadeView = (EmptyShadeView) LayoutInflater.from(this.mContext).inflate(2130968812, (ViewGroup) this.mStackScroller, false);
        this.mStackScroller.setEmptyShadeView(this.mEmptyShadeView);
    }

    private void inflateOverflowContainer() {
        this.mKeyguardIconOverflowContainer = (NotificationOverflowContainer) LayoutInflater.from(this.mContext).inflate(2130968815, (ViewGroup) this.mStackScroller, false);
        this.mKeyguardIconOverflowContainer.setOnActivatedListener(this);
        this.mKeyguardIconOverflowContainer.setOnClickListener(this.mOverflowClickListener);
        this.mStackScroller.setOverflowContainer(this.mKeyguardIconOverflowContainer);
    }

    private void inflateSignalClusters() {
        this.mIconController.setSignalCluster(reinflateSignalCluster(this.mStatusBarView));
        reinflateSignalCluster(this.mKeyguardStatusBar);
    }

    private void instantCollapseNotificationPanel() {
        this.mNotificationPanel.instantCollapse();
    }

    private void instantExpandNotificationsPanel() {
        makeExpandedVisible(true);
        this.mNotificationPanel.expand(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isPlaybackActive(int i) {
        return (i == 1 || i == 7 || i == 0) ? false : true;
    }

    public static boolean isTopLevelChild(NotificationData.Entry entry) {
        return entry.row.getParent() instanceof NotificationStackScrollLayout;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void logNotificationVisibilityChanges(Collection<NotificationVisibility> collection, Collection<NotificationVisibility> collection2) {
        if (collection.isEmpty() && collection2.isEmpty()) {
            return;
        }
        NotificationVisibility[] notificationVisibilityArr = (NotificationVisibility[]) collection.toArray(new NotificationVisibility[collection.size()]);
        try {
            this.mBarService.onNotificationVisibilityChanged(notificationVisibilityArr, (NotificationVisibility[]) collection2.toArray(new NotificationVisibility[collection2.size()]));
        } catch (RemoteException e) {
        }
        int size = collection.size();
        if (size > 0) {
            String[] strArr = new String[size];
            for (int i = 0; i < size; i++) {
                strArr[i] = notificationVisibilityArr[i].key;
            }
            setNotificationsShown(strArr);
        }
    }

    private void logStateToEventlog() {
        int i = 1;
        boolean isShowing = this.mStatusBarKeyguardViewManager.isShowing();
        boolean isOccluded = this.mStatusBarKeyguardViewManager.isOccluded();
        boolean isBouncerShowing = this.mStatusBarKeyguardViewManager.isBouncerShowing();
        boolean isMethodSecure = this.mUnlockMethodCache.isMethodSecure();
        boolean canSkipBouncer = this.mUnlockMethodCache.canSkipBouncer();
        int loggingFingerprint = getLoggingFingerprint(this.mState, isShowing, isOccluded, isBouncerShowing, isMethodSecure, canSkipBouncer);
        if (loggingFingerprint != this.mLastLoggedStateFingerprint) {
            int i2 = this.mState;
            int i3 = isShowing ? 1 : 0;
            int i4 = isOccluded ? 1 : 0;
            int i5 = isBouncerShowing ? 1 : 0;
            int i6 = isMethodSecure ? 1 : 0;
            if (!canSkipBouncer) {
                i = 0;
            }
            EventLogTags.writeSysuiStatusBarState(i2, i3, i4, i5, i6, i);
            this.mLastLoggedStateFingerprint = loggingFingerprint;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void notifyNavigationBarScreenOn(boolean z) {
        if (this.mNavigationBarView == null) {
            return;
        }
        this.mNavigationBarView.notifyScreenOn(z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void notifyUiVisibilityChanged(int i) {
        try {
            if (this.mLastDispatchedSystemUiVisibility != i) {
                this.mWindowManagerService.statusBarVisibilityChanged(i);
                this.mLastDispatchedSystemUiVisibility = i;
            }
        } catch (RemoteException e) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onLaunchTransitionFadingEnded() {
        this.mNotificationPanel.setAlpha(1.0f);
        this.mNotificationPanel.onAffordanceLaunchEnded();
        releaseGestureWakeLock();
        runLaunchTransitionEndRunnable();
        this.mLaunchTransitionFadingAway = false;
        this.mScrimController.forceHideScrims(false);
        updateMediaMetaData(true, true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onLaunchTransitionTimeout() {
        Log.w("PhoneStatusBar", "Launch transition: Timeout!");
        this.mNotificationPanel.onAffordanceLaunchEnded();
        releaseGestureWakeLock();
        this.mNotificationPanel.resetViews();
    }

    private boolean packageHasVisibilityOverride(String str) {
        boolean z = false;
        if (this.mNotificationData.getVisibilityOverride(str) == 0) {
            z = true;
        }
        return z;
    }

    private void performDismissAllAnimations(ArrayList<View> arrayList) {
        Runnable runnable = new Runnable(this) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.30
            final PhoneStatusBar this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.animateCollapsePanels(0);
            }
        };
        this.mStackScroller.setDismissAllInProgress(true);
        int i = 140;
        int i2 = 180;
        for (int size = arrayList.size() - 1; size >= 0; size--) {
            View view = arrayList.get(size);
            Runnable runnable2 = null;
            if (size == 0) {
                runnable2 = runnable;
            }
            this.mStackScroller.dismissViewAnimated(view, runnable2, i2, 260L);
            i = Math.max(50, i - 10);
            i2 += i;
        }
    }

    private void prepareNavigationBarView() {
        this.mNavigationBarView.reorient();
        ButtonDispatcher recentsButton = this.mNavigationBarView.getRecentsButton();
        recentsButton.setOnClickListener(this.mRecentsClickListener);
        recentsButton.setOnTouchListener(this.mRecentsPreloadOnTouchListener);
        recentsButton.setLongClickable(true);
        recentsButton.setOnLongClickListener(this.mRecentsLongClickListener);
        ButtonDispatcher backButton = this.mNavigationBarView.getBackButton();
        backButton.setLongClickable(true);
        backButton.setOnLongClickListener(this.mLongPressBackListener);
        ButtonDispatcher homeButton = this.mNavigationBarView.getHomeButton();
        homeButton.setOnTouchListener(this.mHomeActionListener);
        homeButton.setOnLongClickListener(this.mLongPressHomeListener);
        if (MultiWindowManager.isSupported()) {
            this.mNavigationBarView.getRestoreButton().setOnClickListener(this.mRestoreClickListener);
        }
        this.mAssistManager.onConfigurationChanged();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void recycleAllVisibilityObjects(ArraySet<NotificationVisibility> arraySet) {
        int size = arraySet.size();
        for (int i = 0; i < size; i++) {
            arraySet.valueAt(i).recycle();
        }
        arraySet.clear();
    }

    private SignalClusterView reinflateSignalCluster(View view) {
        SignalClusterView signalClusterView = (SignalClusterView) view.findViewById(2131886656);
        if (signalClusterView != null) {
            ViewParent parent = signalClusterView.getParent();
            if (parent instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) parent;
                int indexOfChild = viewGroup.indexOfChild(signalClusterView);
                viewGroup.removeView(signalClusterView);
                SignalClusterView signalClusterView2 = (SignalClusterView) LayoutInflater.from(this.mContext).inflate(2130968806, viewGroup, false);
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) viewGroup.getLayoutParams();
                marginLayoutParams.setMarginsRelative(this.mContext.getResources().getDimensionPixelSize(2131689953), 0, 0, 0);
                signalClusterView2.setLayoutParams(marginLayoutParams);
                signalClusterView2.setSecurityController(this.mSecurityController);
                signalClusterView2.setNetworkController(this.mNetworkController);
                viewGroup.addView(signalClusterView2, indexOfChild);
                return signalClusterView2;
            }
            return signalClusterView;
        }
        return null;
    }

    private void releaseGestureWakeLock() {
        if (this.mGestureWakeLock.isHeld()) {
            this.mGestureWakeLock.release();
        }
    }

    private void removeNotificationChildren() {
        ArrayList<ExpandableNotificationRow> arrayList = new ArrayList();
        for (int i = 0; i < this.mStackScroller.getChildCount(); i++) {
            View childAt = this.mStackScroller.getChildAt(i);
            if (childAt instanceof ExpandableNotificationRow) {
                ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) childAt;
                List<ExpandableNotificationRow> notificationChildren = expandableNotificationRow.getNotificationChildren();
                List<ExpandableNotificationRow> list = this.mTmpChildOrderMap.get(expandableNotificationRow);
                if (notificationChildren != null) {
                    arrayList.clear();
                    for (ExpandableNotificationRow expandableNotificationRow2 : notificationChildren) {
                        if (list == null || !list.contains(expandableNotificationRow2)) {
                            if (!expandableNotificationRow2.keepInParent()) {
                                arrayList.add(expandableNotificationRow2);
                            }
                        }
                    }
                    for (ExpandableNotificationRow expandableNotificationRow3 : arrayList) {
                        expandableNotificationRow.removeChildNotification(expandableNotificationRow3);
                        if (this.mNotificationData.get(expandableNotificationRow3.getStatusBarNotification().getKey()) == null) {
                            this.mStackScroller.notifyGroupChildRemoved(expandableNotificationRow3, expandableNotificationRow.getChildrenContainer());
                        }
                    }
                }
            }
        }
    }

    private void removeRemoteInputEntriesKeptUntilCollapsed() {
        for (int i = 0; i < this.mRemoteInputEntriesToRemoveOnCollapse.size(); i++) {
            NotificationData.Entry valueAt = this.mRemoteInputEntriesToRemoveOnCollapse.valueAt(i);
            this.mRemoteInputController.removeRemoteInput(valueAt);
            removeNotification(valueAt.key, this.mLatestRankingMap);
        }
        this.mRemoteInputEntriesToRemoveOnCollapse.clear();
    }

    private void resetUserSetupObserver() {
        this.mContext.getContentResolver().unregisterContentObserver(this.mUserSetupObserver);
        this.mUserSetupObserver.onChange(false);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("user_setup_complete"), true, this.mUserSetupObserver, this.mCurrentUserId);
    }

    private void resumeSuspendedAutohide() {
        if (this.mAutohideSuspended) {
            scheduleAutohide();
            this.mHandler.postDelayed(this.mCheckBarModes, 500L);
        }
    }

    private void runLaunchTransitionEndRunnable() {
        if (this.mLaunchTransitionEndRunnable != null) {
            Runnable runnable = this.mLaunchTransitionEndRunnable;
            this.mLaunchTransitionEndRunnable = null;
            runnable.run();
        }
    }

    private void runPostCollapseRunnables() {
        ArrayList arrayList = new ArrayList(this.mPostCollapseRunnables);
        this.mPostCollapseRunnables.clear();
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            ((Runnable) arrayList.get(i)).run();
        }
    }

    private boolean sameSessions(MediaController mediaController, MediaController mediaController2) {
        if (mediaController == mediaController2) {
            return true;
        }
        if (mediaController == null) {
            return false;
        }
        return mediaController.controlsSameSession(mediaController2);
    }

    private void scheduleAutohide() {
        cancelAutohide();
        this.mHandler.postDelayed(this.mAutohide, 3000L);
    }

    private void setControllerUsers() {
        if (this.mZenModeController != null) {
            this.mZenModeController.setUserId(this.mCurrentUserId);
        }
        if (this.mSecurityController != null) {
            this.mSecurityController.onUserSwitched(this.mCurrentUserId);
        }
    }

    private void setNavigationIconHints(int i) {
        if (i == this.mNavigationIconHints) {
            return;
        }
        this.mNavigationIconHints = i;
        if (this.mNavigationBarView != null) {
            this.mNavigationBarView.setNavigationIconHints(i);
        }
        checkBarModes();
    }

    private boolean shouldSuppressFullScreenIntent(String str) {
        if (isDeviceInVrMode()) {
            return true;
        }
        return this.mPowerManager.isInteractive() ? this.mNotificationData.shouldSuppressScreenOn(str) : this.mNotificationData.shouldSuppressScreenOff(str);
    }

    private void showBouncer() {
        if (this.mState == 1 || this.mState == 2) {
            this.mWaitingForKeyguardExit = this.mStatusBarKeyguardViewManager.isShowing();
            this.mStatusBarKeyguardViewManager.dismiss();
        }
    }

    private void startNotificationLogging() {
        this.mStackScroller.setChildLocationsChangedListener(this.mNotificationLocationsChangedListener);
        this.mNotificationLocationsChangedListener.onChildLocationsChanged(this.mStackScroller);
    }

    private void stopNotificationLogging() {
        if (!this.mCurrentlyVisibleNotifications.isEmpty()) {
            logNotificationVisibilityChanges(Collections.emptyList(), this.mCurrentlyVisibleNotifications);
            recycleAllVisibilityObjects(this.mCurrentlyVisibleNotifications);
        }
        this.mHandler.removeCallbacks(this.mVisibilityReporter);
        this.mStackScroller.setChildLocationsChangedListener(null);
    }

    private final boolean supportCustomizeCarrierLabel() {
        return (this.mStatusBarPlmnPlugin == null || !this.mStatusBarPlmnPlugin.supportCustomizeCarrierLabel() || this.mNetworkController == null) ? false : this.mNetworkController.hasMobileDataFeature();
    }

    private void suspendAutohide() {
        boolean z = false;
        this.mHandler.removeCallbacks(this.mAutohide);
        this.mHandler.removeCallbacks(this.mCheckBarModes);
        if ((this.mSystemUiVisibility & 201326592) != 0) {
            z = true;
        }
        this.mAutohideSuspended = z;
    }

    private void updateClearAll() {
        this.mStackScroller.updateDismissView(this.mState != 1 ? this.mNotificationData.hasActiveClearableNotifications() : false);
    }

    private final void updateCustomizeCarrierLabelVisibility(boolean z) {
        Log.d("PhoneStatusBar", "updateCustomizeCarrierLabelVisibility(), force = " + z + ", mState = " + this.mState);
        this.mStatusBarPlmnPlugin.updateCarrierLabelVisibility(z, this.mStackScroller.getVisibility() == 0 ? this.mState != 1 : false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateDozing() {
        boolean z = true;
        if ((!this.mDozingRequested || this.mState != 1) && this.mFingerprintUnlockController.getMode() != 2) {
            z = false;
        }
        this.mDozing = z;
        updateDozingState();
    }

    private void updateDozingState() {
        boolean isPulsing = !this.mDozing ? this.mDozeScrimController.isPulsing() : false;
        this.mNotificationPanel.setDozing(this.mDozing, isPulsing);
        this.mStackScroller.setDark(this.mDozing, isPulsing, this.mWakeUpTouchLocation);
        this.mScrimController.setDozing(this.mDozing);
        DozeScrimController dozeScrimController = this.mDozeScrimController;
        boolean z = false;
        if (this.mDozing) {
            z = false;
            if (this.mFingerprintUnlockController.getMode() != 2) {
                z = true;
            }
        }
        dozeScrimController.setDozing(z, isPulsing);
    }

    private void updateEmptyShadeView() {
        this.mNotificationPanel.setShadeEmpty(this.mState != 1 ? this.mNotificationData.getActiveNotifications().size() == 0 : false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateNotificationShade() {
        if (this.mStackScroller == null) {
            return;
        }
        if (isCollapsing()) {
            addPostCollapseAction(new Runnable(this) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.32
                final PhoneStatusBar this$0;

                {
                    this.this$0 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$0.updateNotificationShade();
                }
            });
            return;
        }
        ArrayList<NotificationData.Entry> activeNotifications = this.mNotificationData.getActiveNotifications();
        ArrayList arrayList = new ArrayList(activeNotifications.size());
        int size = activeNotifications.size();
        for (int i = 0; i < size; i++) {
            NotificationData.Entry entry = activeNotifications.get(i);
            int i2 = entry.notification.getNotification().visibility;
            boolean z = !userAllowsPrivateNotificationsInPublic(entry.notification.getUserId());
            boolean z2 = i2 == 0;
            boolean packageHasVisibilityOverride = packageHasVisibilityOverride(entry.notification.getKey());
            if (z2 && z) {
                packageHasVisibilityOverride = true;
            }
            boolean isLockscreenPublicMode = packageHasVisibilityOverride ? isLockscreenPublicMode() : false;
            if (isLockscreenPublicMode) {
                updatePublicContentView(entry, entry.notification);
            }
            entry.row.setSensitive(packageHasVisibilityOverride, z);
            if (entry.autoRedacted && entry.legacy) {
                if (isLockscreenPublicMode) {
                    entry.row.setShowingLegacyBackground(false);
                } else {
                    entry.row.setShowingLegacyBackground(true);
                }
            }
            if (this.mGroupManager.isChildInGroupWithSummary(entry.row.getStatusBarNotification())) {
                ExpandableNotificationRow groupSummary = this.mGroupManager.getGroupSummary(entry.row.getStatusBarNotification());
                List<ExpandableNotificationRow> list = this.mTmpChildOrderMap.get(groupSummary);
                ArrayList arrayList2 = list;
                if (list == null) {
                    arrayList2 = new ArrayList();
                    this.mTmpChildOrderMap.put(groupSummary, arrayList2);
                }
                arrayList2.add(entry.row);
            } else {
                arrayList.add(entry.row);
            }
        }
        ArrayList<ExpandableNotificationRow> arrayList3 = new ArrayList();
        for (int i3 = 0; i3 < this.mStackScroller.getChildCount(); i3++) {
            View childAt = this.mStackScroller.getChildAt(i3);
            if (!arrayList.contains(childAt) && (childAt instanceof ExpandableNotificationRow)) {
                arrayList3.add((ExpandableNotificationRow) childAt);
            }
        }
        for (ExpandableNotificationRow expandableNotificationRow : arrayList3) {
            if (this.mGroupManager.isChildInGroupWithSummary(expandableNotificationRow.getStatusBarNotification())) {
                this.mStackScroller.setChildTransferInProgress(true);
            }
            if (expandableNotificationRow.isSummaryWithChildren()) {
                expandableNotificationRow.removeAllChildren();
            }
            this.mStackScroller.removeView(expandableNotificationRow);
            this.mStackScroller.setChildTransferInProgress(false);
        }
        removeNotificationChildren();
        for (int i4 = 0; i4 < arrayList.size(); i4++) {
            View view = (View) arrayList.get(i4);
            if (view.getParent() == null) {
                this.mStackScroller.addView(view);
            }
        }
        addNotificationChildrenAndSort();
        int i5 = 0;
        for (int i6 = 0; i6 < this.mStackScroller.getChildCount(); i6++) {
            View childAt2 = this.mStackScroller.getChildAt(i6);
            if (childAt2 instanceof ExpandableNotificationRow) {
                ExpandableNotificationRow expandableNotificationRow2 = (ExpandableNotificationRow) arrayList.get(i5);
                if (childAt2 != expandableNotificationRow2) {
                    this.mStackScroller.changeViewPosition(expandableNotificationRow2, i6);
                }
                i5++;
            }
        }
        this.mTmpChildOrderMap.clear();
        updateRowStates();
        updateSpeedbump();
        updateClearAll();
        updateEmptyShadeView();
        updateQsExpansionEnabled();
        this.mShadeUpdates.check();
    }

    private void updatePublicMode() {
        boolean z = false;
        if (this.mStatusBarKeyguardViewManager.isShowing()) {
            int size = this.mCurrentProfiles.size() - 1;
            while (true) {
                z = false;
                if (size < 0) {
                    break;
                }
                if (this.mStatusBarKeyguardViewManager.isSecure(this.mCurrentProfiles.valueAt(size).id)) {
                    z = true;
                    break;
                }
                size--;
            }
        }
        setLockscreenPublicMode(z);
    }

    private void updateQsExpansionEnabled() {
        NotificationPanelView notificationPanelView = this.mNotificationPanel;
        boolean z = false;
        if (isDeviceProvisioned()) {
            if (this.mUserSetup || this.mUserSwitcherController == null || !this.mUserSwitcherController.isSimpleUserSwitcher()) {
                z = false;
                if ((this.mDisabled2 & 1) == 0) {
                    z = !ONLY_CORE_APPS;
                }
            } else {
                z = false;
            }
        }
        notificationPanelView.setQsExpansionEnabled(z);
    }

    private void updateSpeedbump() {
        int i;
        int i2 = 0;
        int childCount = this.mStackScroller.getChildCount();
        int i3 = 0;
        while (true) {
            i = -1;
            if (i3 >= childCount) {
                break;
            }
            View childAt = this.mStackScroller.getChildAt(i3);
            int i4 = i2;
            if (childAt.getVisibility() != 8) {
                i4 = i2;
                if (childAt instanceof ExpandableNotificationRow) {
                    if (this.mNotificationData.isAmbient(((ExpandableNotificationRow) childAt).getStatusBarNotification().getKey())) {
                        i = i2;
                        break;
                    }
                    i4 = i2 + 1;
                } else {
                    continue;
                }
            }
            i3++;
            i2 = i4;
        }
        this.mStackScroller.updateSpeedBumpIndex(i);
    }

    private void userAutohide() {
        cancelAutohide();
        this.mHandler.postDelayed(this.mAutohide, 350L);
    }

    private void vibrateForCameraGesture() {
        this.mVibrator.vibrate(new long[]{0, 750}, -1);
    }

    public static String viewInfo(View view) {
        return "[(" + view.getLeft() + "," + view.getTop() + ")(" + view.getRight() + "," + view.getBottom() + ") " + view.getWidth() + "x" + view.getHeight() + "]";
    }

    protected void addNavigationBar() {
        Log.v("PhoneStatusBar", "addNavigationBar: about to add " + this.mNavigationBarView);
        if (this.mNavigationBarView == null) {
            return;
        }
        prepareNavigationBarView();
        this.mWindowManager.addView(this.mNavigationBarView, getNavigationBarLayoutParams());
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar
    public void addNotification(StatusBarNotification statusBarNotification, NotificationListenerService.RankingMap rankingMap, NotificationData.Entry entry) {
        Log.d("PhoneStatusBar", "addNotification key=" + statusBarNotification.getKey());
        if (statusBarNotification != null && statusBarNotification.getNotification() != null && (statusBarNotification.getNotification().flags & 268435456) != 0) {
            Log.d("PhoneStatusBar", "Will not add the notification.flags contains FLAG_HIDE_NOTIFICATION");
            return;
        }
        this.mNotificationData.updateRanking(rankingMap);
        NotificationData.Entry createNotificationViews = createNotificationViews(statusBarNotification);
        if (createNotificationViews == null) {
            return;
        }
        boolean shouldPeek = shouldPeek(createNotificationViews);
        if (shouldPeek) {
            this.mHeadsUpManager.showNotification(createNotificationViews);
            setNotificationShown(statusBarNotification);
        }
        if (!shouldPeek && statusBarNotification.getNotification().fullScreenIntent != null) {
            if (shouldSuppressFullScreenIntent(statusBarNotification.getKey())) {
                Log.d("PhoneStatusBar", "No Fullscreen intent: suppressed by DND: " + statusBarNotification.getKey());
            } else if (this.mNotificationData.getImportance(statusBarNotification.getKey()) < 5) {
                Log.d("PhoneStatusBar", "No Fullscreen intent: not important enough: " + statusBarNotification.getKey());
            } else {
                awakenDreams();
                Log.d("PhoneStatusBar", "Notification has fullScreenIntent; sending fullScreenIntent");
                try {
                    EventLog.writeEvent(36002, statusBarNotification.getKey());
                    statusBarNotification.getNotification().fullScreenIntent.send();
                    createNotificationViews.notifyFullScreenIntentLaunched();
                    MetricsLogger.count(this.mContext, "note_fullscreen", 1);
                } catch (PendingIntent.CanceledException e) {
                }
            }
        }
        addNotificationViews(createNotificationViews, rankingMap);
        setAreThereNotifications();
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar
    public void addPostCollapseAction(Runnable runnable) {
        this.mPostCollapseRunnables.add(runnable);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void addQsTile(ComponentName componentName) {
        this.mQSPanel.getHost().addTile(componentName);
    }

    /* JADX WARN: Code restructure failed: missing block: B:14:0x0028, code lost:
        if (r3.mWaitingForKeyguardExit != false) goto L12;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    protected int adjustDisableFlags(int i) {
        int i2 = i;
        if (!this.mLaunchTransitionFadingAway) {
            if (this.mKeyguardFadingAway) {
                i2 = i;
            } else {
                if (!this.mExpandedVisible && !this.mBouncerShowing) {
                    i2 = i;
                }
                i2 = i | 131072 | 1048576;
            }
        }
        return i2;
    }

    public void animateCollapsePanels() {
        animateCollapsePanels(0);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void animateCollapsePanels(int i) {
        animateCollapsePanels(i, false, false, 1.0f);
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar
    public void animateCollapsePanels(int i, boolean z) {
        animateCollapsePanels(i, z, false, 1.0f);
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar
    public void animateCollapsePanels(int i, boolean z, boolean z2) {
        animateCollapsePanels(i, z, z2, 1.0f);
    }

    public void animateCollapsePanels(int i, boolean z, boolean z2, float f) {
        if (!z && this.mState != 0) {
            runPostCollapseRunnables();
            return;
        }
        if ((i & 2) == 0 && !this.mHandler.hasMessages(1020)) {
            this.mHandler.removeMessages(1020);
            this.mHandler.sendEmptyMessage(1020);
        }
        if (this.mStatusBarWindow != null) {
            this.mStatusBarWindowManager.setStatusBarFocusable(false);
            this.mStatusBarWindow.cancelExpandHelper();
            this.mStatusBarView.collapsePanel(true, z2, f);
        }
    }

    public void animateCollapseQuickSettings() {
        if (this.mState == 0) {
            this.mStatusBarView.collapsePanel(true, false, 1.0f);
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void animateExpandNotificationsPanel() {
        if (panelsEnabled()) {
            this.mNotificationPanel.expand(true);
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void animateExpandSettingsPanel(String str) {
        if (panelsEnabled() && this.mUserSetup) {
            if (str != null) {
                this.mQSPanel.openDetails(str);
            }
            this.mNotificationPanel.expandWithQs();
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void appTransitionCancelled() {
        this.mIconController.appTransitionCancelled();
        EventBus.getDefault().send(new AppTransitionFinishedEvent());
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void appTransitionFinished() {
        EventBus.getDefault().send(new AppTransitionFinishedEvent());
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void appTransitionPending() {
        if (this.mKeyguardFadingAway) {
            return;
        }
        this.mIconController.appTransitionPending();
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void appTransitionStarting(long j, long j2) {
        if (!this.mKeyguardGoingAway) {
            this.mIconController.appTransitionStarting(j, j2);
        }
        if (this.mIconPolicy != null) {
            this.mIconPolicy.appTransitionStarting(j, j2);
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void buzzBeepBlinked() {
        if (this.mDozeServiceHost != null) {
            this.mDozeServiceHost.fireBuzzBeepBlinked();
        }
    }

    public long calculateGoingToFullShadeDelay() {
        return this.mKeyguardFadingAwayDelay + this.mKeyguardFadingAwayDuration;
    }

    public void clearAllNotifications() {
        int childCount = this.mStackScroller.getChildCount();
        ArrayList<View> arrayList = new ArrayList<>(childCount);
        for (int i = 0; i < childCount; i++) {
            View childAt = this.mStackScroller.getChildAt(i);
            if (childAt instanceof ExpandableNotificationRow) {
                if (this.mStackScroller.canChildBeDismissed(childAt) && childAt.getVisibility() == 0) {
                    arrayList.add(childAt);
                }
                ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) childAt;
                List<ExpandableNotificationRow> notificationChildren = expandableNotificationRow.getNotificationChildren();
                if (expandableNotificationRow.areChildrenExpanded() && notificationChildren != null) {
                    for (ExpandableNotificationRow expandableNotificationRow2 : notificationChildren) {
                        if (expandableNotificationRow2.getVisibility() == 0) {
                            arrayList.add(expandableNotificationRow2);
                        }
                    }
                }
            }
        }
        if (arrayList.isEmpty()) {
            animateCollapsePanels(0);
            return;
        }
        addPostCollapseAction(new Runnable(this) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.29
            final PhoneStatusBar this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mStackScroller.setDismissAllInProgress(false);
                try {
                    this.this$0.mBarService.onClearAllNotifications(this.this$0.mCurrentUserId);
                } catch (Exception e) {
                }
            }
        });
        performDismissAllAnimations(arrayList);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void clickTile(ComponentName componentName) {
        this.mQSPanel.clickTile(componentName);
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar
    public void createAndAddWindows() {
        addStatusBarWindow();
    }

    protected BatteryController createBatteryController() {
        return new BatteryControllerImpl(this.mContext);
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar
    protected BaseStatusBar.H createHandler() {
        return new H(this, null);
    }

    protected void createIconController() {
        this.mIconController = new StatusBarIconController(this.mContext, this.mStatusBarView, this.mKeyguardStatusBar, this);
    }

    protected void createNavigationBarView(Context context) {
        inflateNavigationBarView(context);
        this.mNavigationBarView.setDisabledFlags(this.mDisabled1);
        this.mNavigationBarView.setComponents(this.mRecents, (Divider) getComponent(Divider.class));
        this.mNavigationBarView.setOnVerticalChangedListener(new NavigationBarView.OnVerticalChangedListener(this) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.27
            final PhoneStatusBar this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.systemui.statusbar.phone.NavigationBarView.OnVerticalChangedListener
            public void onVerticalChanged(boolean z) {
                if (this.this$0.mAssistManager != null) {
                    this.this$0.mAssistManager.onConfigurationChanged();
                }
                this.this$0.mNotificationPanel.setQsScrimEnabled(!z);
            }
        });
        this.mNavigationBarView.setOnTouchListener(new View.OnTouchListener(this) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.28
            final PhoneStatusBar this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                this.this$0.checkUserAutohide(view, motionEvent);
                return false;
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void createUserSwitcher() {
        this.mKeyguardUserSwitcher = new KeyguardUserSwitcher(this.mContext, (ViewStub) this.mStatusBarWindow.findViewById(2131886685), this.mKeyguardStatusBar, this.mNotificationPanel, this.mUserSwitcherController);
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar
    public void destroy() {
        super.destroy();
        if (this.mStatusBarWindow != null) {
            this.mWindowManager.removeViewImmediate(this.mStatusBarWindow);
            this.mStatusBarWindow = null;
        }
        if (this.mNavigationBarView != null) {
            this.mWindowManager.removeViewImmediate(this.mNavigationBarView);
            this.mNavigationBarView = null;
        }
        if (this.mHandlerThread != null) {
            this.mHandlerThread.quitSafely();
            this.mHandlerThread = null;
        }
        this.mContext.unregisterReceiver(this.mBroadcastReceiver);
        this.mContext.unregisterReceiver(this.mDemoReceiver);
        this.mAssistManager.destroy();
        SignalClusterView signalClusterView = (SignalClusterView) this.mStatusBarView.findViewById(2131886656);
        SignalClusterView signalClusterView2 = (SignalClusterView) this.mKeyguardStatusBar.findViewById(2131886656);
        SignalClusterView signalClusterView3 = (SignalClusterView) this.mHeader.findViewById(2131886656);
        this.mNetworkController.removeSignalCallback(signalClusterView);
        this.mNetworkController.removeSignalCallback(signalClusterView2);
        this.mNetworkController.removeSignalCallback(signalClusterView3);
        if (this.mQSPanel == null || this.mQSPanel.getHost() == null) {
            return;
        }
        this.mQSPanel.getHost().destroy();
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void disable(int i, int i2, boolean z) {
        boolean z2 = z & (this.mStatusBarWindowState != 2);
        this.mDisabledUnmodified1 = i;
        this.mDisabledUnmodified2 = i2;
        int adjustDisableFlags = adjustDisableFlags(i);
        int i3 = this.mDisabled1;
        int i4 = adjustDisableFlags ^ i3;
        this.mDisabled1 = adjustDisableFlags;
        int i5 = this.mDisabled2;
        int i6 = i2 ^ i5;
        this.mDisabled2 = i2;
        Log.d("PhoneStatusBar", String.format("disable1: 0x%08x -> 0x%08x (diff1: 0x%08x)", Integer.valueOf(i3), Integer.valueOf(adjustDisableFlags), Integer.valueOf(i4)));
        Log.d("PhoneStatusBar", String.format("disable2: 0x%08x -> 0x%08x (diff2: 0x%08x)", Integer.valueOf(i5), Integer.valueOf(i2), Integer.valueOf(i6)));
        StringBuilder sb = new StringBuilder();
        sb.append("disable: < ");
        sb.append((65536 & adjustDisableFlags) != 0 ? "EXPAND" : "expand");
        sb.append((65536 & i4) != 0 ? "* " : " ");
        sb.append((131072 & adjustDisableFlags) != 0 ? "ICONS" : "icons");
        sb.append((131072 & i4) != 0 ? "* " : " ");
        sb.append((262144 & adjustDisableFlags) != 0 ? "ALERTS" : "alerts");
        sb.append((262144 & i4) != 0 ? "* " : " ");
        sb.append((1048576 & adjustDisableFlags) != 0 ? "SYSTEM_INFO" : "system_info");
        sb.append((1048576 & i4) != 0 ? "* " : " ");
        sb.append((4194304 & adjustDisableFlags) != 0 ? "BACK" : "back");
        sb.append((4194304 & i4) != 0 ? "* " : " ");
        sb.append((2097152 & adjustDisableFlags) != 0 ? "HOME" : "home");
        sb.append((2097152 & i4) != 0 ? "* " : " ");
        sb.append((16777216 & adjustDisableFlags) != 0 ? "RECENT" : "recent");
        sb.append((16777216 & i4) != 0 ? "* " : " ");
        sb.append((8388608 & adjustDisableFlags) != 0 ? "CLOCK" : "clock");
        sb.append((8388608 & i4) != 0 ? "* " : " ");
        sb.append((33554432 & adjustDisableFlags) != 0 ? "SEARCH" : "search");
        sb.append((33554432 & i4) != 0 ? "* " : " ");
        sb.append((i2 & 1) != 0 ? "QUICK_SETTINGS" : "quick_settings");
        sb.append((i6 & 1) != 0 ? "* " : " ");
        sb.append(">");
        Log.d("PhoneStatusBar", sb.toString());
        if ((1048576 & i4) != 0) {
            if ((1048576 & adjustDisableFlags) != 0) {
                this.mIconController.hideSystemIconArea(z2);
                this.mStatusBarPlmnPlugin.setPlmnVisibility(8);
            } else {
                this.mIconController.showSystemIconArea(z2);
                this.mStatusBarPlmnPlugin.setPlmnVisibility(0);
            }
        }
        if ((8388608 & i4) != 0) {
            this.mIconController.setClockVisibility((8388608 & adjustDisableFlags) == 0);
        }
        if ((65536 & i4) != 0 && (65536 & adjustDisableFlags) != 0) {
            animateCollapsePanels();
        }
        if ((56623104 & i4) != 0) {
            if (this.mNavigationBarView != null) {
                this.mNavigationBarView.setDisabledFlags(adjustDisableFlags);
            }
            if ((16777216 & adjustDisableFlags) != 0) {
                this.mHandler.removeMessages(1020);
                this.mHandler.sendEmptyMessage(1020);
            }
        }
        if ((131072 & i4) != 0) {
            if ((131072 & adjustDisableFlags) != 0) {
                this.mIconController.hideNotificationIconArea(z2);
            } else {
                this.mIconController.showNotificationIconArea(z2);
            }
        }
        if ((262144 & i4) != 0) {
            this.mDisableNotificationAlerts = (262144 & adjustDisableFlags) != 0;
            this.mHeadsUpObserver.onChange(true);
        }
        if ((i6 & 1) != 0) {
            updateQsExpansionEnabled();
        }
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar
    protected void dismissKeyguardThenExecute(KeyguardHostView.OnDismissAction onDismissAction, boolean z) {
        dismissKeyguardThenExecute(onDismissAction, null, z);
    }

    @Override // com.android.systemui.DemoMode
    public void dispatchDemoCommand(String str, Bundle bundle) {
        View view = null;
        if (!this.mDemoModeAllowed) {
            this.mDemoModeAllowed = Settings.Global.getInt(this.mContext.getContentResolver(), "sysui_demo_allowed", 0) != 0;
        }
        if (this.mDemoModeAllowed) {
            if (str.equals("enter")) {
                this.mDemoMode = true;
            } else if (str.equals("exit")) {
                this.mDemoMode = false;
                checkBarModes();
            } else if (!this.mDemoMode) {
                dispatchDemoCommand("enter", new Bundle());
            }
            boolean equals = !str.equals("enter") ? str.equals("exit") : true;
            if ((equals || str.equals("volume")) && this.mVolumeComponent != null) {
                this.mVolumeComponent.dispatchDemoCommand(str, bundle);
            }
            if (equals || str.equals("clock")) {
                dispatchDemoCommandToView(str, bundle, R$id.clock);
            }
            if (equals || str.equals("battery")) {
                this.mBatteryController.dispatchDemoCommand(str, bundle);
            }
            if (equals || str.equals("status")) {
                this.mIconController.dispatchDemoCommand(str, bundle);
            }
            if (this.mNetworkController != null && (equals || str.equals("network"))) {
                this.mNetworkController.dispatchDemoCommand(str, bundle);
            }
            if (equals || str.equals("notifications")) {
                if (this.mStatusBarView != null) {
                    view = this.mStatusBarView.findViewById(2131886677);
                }
                if (view != null) {
                    view.setVisibility((this.mDemoMode && "false".equals(bundle.getString("visible"))) ? 4 : 0);
                }
            }
            if (str.equals("bars")) {
                String string = bundle.getString("mode");
                int i = "opaque".equals(string) ? 0 : "translucent".equals(string) ? 2 : "semi-transparent".equals(string) ? 1 : "transparent".equals(string) ? 4 : "warning".equals(string) ? 5 : -1;
                if (i != -1) {
                    if (this.mStatusBarView != null) {
                        this.mStatusBarView.getBarTransitions().transitionTo(i, true);
                    }
                    if (this.mNavigationBarView != null) {
                        this.mNavigationBarView.getBarTransitions().transitionTo(i, true);
                    }
                }
            }
        }
    }

    @Override // com.android.systemui.SystemUI
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        synchronized (this.mQueueLock) {
            printWriter.println("Current Status Bar state:");
            printWriter.println("  mExpandedVisible=" + this.mExpandedVisible + ", mTrackingPosition=" + this.mTrackingPosition);
            printWriter.println("  mTracking=" + this.mTracking);
            printWriter.println("  mDisplayMetrics=" + this.mDisplayMetrics);
            printWriter.println("  mStackScroller: " + viewInfo(this.mStackScroller));
            printWriter.println("  mStackScroller: " + viewInfo(this.mStackScroller) + " scroll " + this.mStackScroller.getScrollX() + "," + this.mStackScroller.getScrollY());
        }
        printWriter.print("  mInteractingWindows=");
        printWriter.println(this.mInteractingWindows);
        printWriter.print("  mStatusBarWindowState=");
        printWriter.println(StatusBarManager.windowStateToString(this.mStatusBarWindowState));
        printWriter.print("  mStatusBarMode=");
        printWriter.println(BarTransitions.modeToString(this.mStatusBarMode));
        printWriter.print("  mDozing=");
        printWriter.println(this.mDozing);
        printWriter.print("  mZenMode=");
        printWriter.println(Settings.Global.zenModeToString(this.mZenMode));
        printWriter.print("  mUseHeadsUp=");
        printWriter.println(this.mUseHeadsUp);
        dumpBarTransitions(printWriter, "mStatusBarView", this.mStatusBarView.getBarTransitions());
        if (this.mNavigationBarView != null) {
            printWriter.print("  mNavigationBarWindowState=");
            printWriter.println(StatusBarManager.windowStateToString(this.mNavigationBarWindowState));
            printWriter.print("  mNavigationBarMode=");
            printWriter.println(BarTransitions.modeToString(this.mNavigationBarMode));
            dumpBarTransitions(printWriter, "mNavigationBarView", this.mNavigationBarView.getBarTransitions());
        }
        printWriter.print("  mNavigationBarView=");
        if (this.mNavigationBarView == null) {
            printWriter.println("null");
        } else {
            this.mNavigationBarView.dump(fileDescriptor, printWriter, strArr);
        }
        printWriter.print("  mMediaSessionManager=");
        printWriter.println(this.mMediaSessionManager);
        printWriter.print("  mMediaNotificationKey=");
        printWriter.println(this.mMediaNotificationKey);
        printWriter.print("  mMediaController=");
        printWriter.print(this.mMediaController);
        if (this.mMediaController != null) {
            printWriter.print(" state=" + this.mMediaController.getPlaybackState());
        }
        printWriter.println();
        printWriter.print("  mMediaMetadata=");
        printWriter.print(this.mMediaMetadata);
        if (this.mMediaMetadata != null) {
            printWriter.print(" title=" + this.mMediaMetadata.getText("android.media.metadata.TITLE"));
        }
        printWriter.println();
        printWriter.println("  Panels: ");
        if (this.mNotificationPanel != null) {
            printWriter.println("    mNotificationPanel=" + this.mNotificationPanel + " params=" + this.mNotificationPanel.getLayoutParams().debug(""));
            printWriter.print("      ");
            this.mNotificationPanel.dump(fileDescriptor, printWriter, strArr);
        }
        DozeLog.dump(printWriter);
        synchronized (this.mNotificationData) {
            this.mNotificationData.dump(printWriter, "  ");
        }
        this.mIconController.dump(printWriter);
        if (this.mStatusBarWindowManager != null) {
            this.mStatusBarWindowManager.dump(fileDescriptor, printWriter, strArr);
        }
        if (this.mNetworkController != null) {
            this.mNetworkController.dump(fileDescriptor, printWriter, strArr);
        }
        if (this.mBluetoothController != null) {
            this.mBluetoothController.dump(fileDescriptor, printWriter, strArr);
        }
        if (this.mHotspotController != null) {
            this.mHotspotController.dump(fileDescriptor, printWriter, strArr);
        }
        if (this.mCastController != null) {
            this.mCastController.dump(fileDescriptor, printWriter, strArr);
        }
        if (this.mUserSwitcherController != null) {
            this.mUserSwitcherController.dump(fileDescriptor, printWriter, strArr);
        }
        if (this.mBatteryController != null) {
            this.mBatteryController.dump(fileDescriptor, printWriter, strArr);
        }
        if (this.mNextAlarmController != null) {
            this.mNextAlarmController.dump(fileDescriptor, printWriter, strArr);
        }
        if (this.mSecurityController != null) {
            this.mSecurityController.dump(fileDescriptor, printWriter, strArr);
        }
        if (this.mHeadsUpManager != null) {
            this.mHeadsUpManager.dump(fileDescriptor, printWriter, strArr);
        } else {
            printWriter.println("  mHeadsUpManager: null");
        }
        if (this.mGroupManager != null) {
            this.mGroupManager.dump(fileDescriptor, printWriter, strArr);
        } else {
            printWriter.println("  mGroupManager: null");
        }
        if (KeyguardUpdateMonitor.getInstance(this.mContext) != null) {
            KeyguardUpdateMonitor.getInstance(this.mContext).dump(fileDescriptor, printWriter, strArr);
        }
        FalsingManager.getInstance(this.mContext).dump(printWriter);
        FalsingLog.dump(printWriter);
        printWriter.println("SharedPreferences:");
        Iterator<T> it = Prefs.getAll(this.mContext).entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            printWriter.print("  ");
            printWriter.print((String) entry.getKey());
            printWriter.print("=");
            printWriter.println(entry.getValue());
        }
    }

    public void endAffordanceLaunch() {
        releaseGestureWakeLock();
        this.mNotificationPanel.onAffordanceLaunchEnded();
    }

    public void executeRunnableDismissingKeyguard(Runnable runnable, Runnable runnable2, boolean z, boolean z2, boolean z3) {
        dismissKeyguardThenExecute(new KeyguardHostView.OnDismissAction(this, z, z3, this.mStatusBarKeyguardViewManager.isShowing(), z2, runnable) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.40
            final PhoneStatusBar this$0;
            final boolean val$afterKeyguardGone;
            final boolean val$deferred;
            final boolean val$dismissShade;
            final boolean val$keyguardShowing;
            final Runnable val$runnable;

            {
                this.this$0 = this;
                this.val$dismissShade = z;
                this.val$deferred = z3;
                this.val$keyguardShowing = r7;
                this.val$afterKeyguardGone = z2;
                this.val$runnable = runnable;
            }

            @Override // com.android.keyguard.KeyguardHostView.OnDismissAction
            public boolean onDismiss() {
                AsyncTask.execute(new Runnable(this, this.val$keyguardShowing, this.val$afterKeyguardGone, this.val$runnable) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.40.1
                    final AnonymousClass40 this$1;
                    final boolean val$afterKeyguardGone;
                    final boolean val$keyguardShowing;
                    final Runnable val$runnable;

                    {
                        this.this$1 = this;
                        this.val$keyguardShowing = r5;
                        this.val$afterKeyguardGone = r6;
                        this.val$runnable = r7;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        try {
                            if (this.val$keyguardShowing && !this.val$afterKeyguardGone) {
                                ActivityManagerNative.getDefault().keyguardWaitingForActivityDrawn();
                            }
                            if (this.val$runnable != null) {
                                this.val$runnable.run();
                            }
                        } catch (RemoteException e) {
                        }
                    }
                });
                if (this.val$dismissShade) {
                    this.this$0.animateCollapsePanels(2, true, true);
                }
                return this.val$deferred;
            }
        }, runnable2, z2);
    }

    public void fadeKeyguardAfterLaunchTransition(Runnable runnable, Runnable runnable2) {
        this.mHandler.removeMessages(1003);
        this.mLaunchTransitionEndRunnable = runnable2;
        AnonymousClass44 anonymousClass44 = new AnonymousClass44(this, runnable);
        if (this.mNotificationPanel.isLaunchTransitionRunning()) {
            this.mNotificationPanel.setLaunchTransitionEndRunnable(anonymousClass44);
        } else {
            anonymousClass44.run();
        }
    }

    public void fadeKeyguardWhilePulsing() {
        this.mNotificationPanel.animate().alpha(0.0f).setStartDelay(0L).setDuration(96L).setInterpolator(ScrimController.KEYGUARD_FADE_OUT_INTERPOLATOR).start();
    }

    public void findAndUpdateMediaNotifications() {
        MediaController mediaController;
        NotificationData.Entry entry;
        boolean z;
        MediaSession.Token token;
        synchronized (this.mNotificationData) {
            ArrayList<NotificationData.Entry> activeNotifications = this.mNotificationData.getActiveNotifications();
            int size = activeNotifications.size();
            int i = 0;
            while (true) {
                mediaController = null;
                entry = null;
                if (i >= size) {
                    break;
                }
                entry = activeNotifications.get(i);
                if (isMediaNotification(entry) && (token = (MediaSession.Token) entry.notification.getNotification().extras.getParcelable("android.mediaSession")) != null) {
                    mediaController = new MediaController(this.mContext, token);
                    if (3 == getMediaControllerPlaybackState(mediaController)) {
                        break;
                    }
                }
                i++;
            }
            MediaController mediaController2 = mediaController;
            NotificationData.Entry entry2 = entry;
            if (entry == null) {
                mediaController2 = mediaController;
                entry2 = entry;
                if (this.mMediaSessionManager != null) {
                    Iterator it = this.mMediaSessionManager.getActiveSessionsForUser(null, -1).iterator();
                    while (true) {
                        mediaController2 = mediaController;
                        entry2 = entry;
                        if (!it.hasNext()) {
                            break;
                        }
                        MediaController mediaController3 = (MediaController) it.next();
                        if (3 == getMediaControllerPlaybackState(mediaController3)) {
                            String packageName = mediaController3.getPackageName();
                            int i2 = 0;
                            while (true) {
                                if (i2 < size) {
                                    NotificationData.Entry entry3 = activeNotifications.get(i2);
                                    if (entry3.notification.getPackageName().equals(packageName)) {
                                        mediaController = mediaController3;
                                        entry = entry3;
                                        break;
                                    }
                                    i2++;
                                }
                            }
                        }
                    }
                }
            }
            z = false;
            if (mediaController2 != null) {
                if (sameSessions(this.mMediaController, mediaController2)) {
                    z = false;
                } else {
                    clearCurrentMediaNotification();
                    this.mMediaController = mediaController2;
                    this.mMediaController.registerCallback(this.mMediaListener);
                    this.mMediaMetadata = this.mMediaController.getMetadata();
                    if (entry2 != null) {
                        this.mMediaNotificationKey = entry2.notification.getKey();
                    }
                    z = true;
                }
            }
        }
        if (z) {
            updateNotifications();
        }
        updateMediaMetaData(z, true);
    }

    public void finishKeyguardFadingAway() {
        this.mKeyguardFadingAway = false;
        this.mKeyguardGoingAway = false;
    }

    public int getBarState() {
        return this.mState;
    }

    protected ViewGroup getBouncerContainer() {
        return this.mStatusBarWindow;
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar, com.android.systemui.statusbar.NotificationData.Environment
    public String getCurrentMediaNotificationKey() {
        return this.mMediaNotificationKey;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public float getDisplayDensity() {
        return this.mDisplayMetrics.density;
    }

    public GestureRecorder getGestureRecorder() {
        return this.mGestureRec;
    }

    public long getKeyguardFadingAwayDelay() {
        return this.mKeyguardFadingAwayDelay;
    }

    public long getKeyguardFadingAwayDuration() {
        return this.mKeyguardFadingAwayDuration;
    }

    public int getMaxKeyguardNotifications() {
        return getMaxKeyguardNotifications(false);
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar
    protected int getMaxKeyguardNotifications(boolean z) {
        if (z) {
            this.mMaxKeyguardNotifications = Math.max(1, this.mNotificationPanel.computeMaxKeyguardNotifications(this.mMaxAllowedKeyguardNotifications));
            return this.mMaxKeyguardNotifications;
        }
        return this.mMaxKeyguardNotifications;
    }

    public NavigationBarView getNavigationBarView() {
        return this.mNavigationBarView;
    }

    public int getStatusBarHeight() {
        if (this.mNaturalBarHeight < 0) {
            this.mNaturalBarHeight = this.mContext.getResources().getDimensionPixelSize(17104919);
        }
        return this.mNaturalBarHeight;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public View getStatusBarView() {
        return this.mStatusBarView;
    }

    public StatusBarWindowView getStatusBarWindow() {
        return this.mStatusBarWindow;
    }

    public void goToKeyguard() {
        if (this.mState == 2) {
            this.mStackScroller.onGoToKeyguard();
            setBarState(1);
            updateKeyguardState(false, true);
        }
    }

    public void goToLockedShade(View view) {
        ExpandableNotificationRow expandableNotificationRow = null;
        if (view instanceof ExpandableNotificationRow) {
            expandableNotificationRow = (ExpandableNotificationRow) view;
            expandableNotificationRow.setUserExpanded(true, true);
            expandableNotificationRow.setGroupExpansionChanging(true);
        }
        boolean shouldEnforceBouncer = (userAllowsPrivateNotificationsInPublic(this.mCurrentUserId) && this.mShowLockscreenNotifications) ? this.mFalsingManager.shouldEnforceBouncer() : true;
        if (!isLockscreenPublicMode() || !shouldEnforceBouncer) {
            this.mNotificationPanel.animateToFullShade(0L);
            setBarState(2);
            updateKeyguardState(false, false);
            return;
        }
        this.mLeaveOpenOnKeyguardHide = true;
        showBouncer();
        this.mDraggedDownRow = expandableNotificationRow;
        this.mPendingRemoteInputView = null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.BaseStatusBar
    public void handleVisibleToUserChanged(boolean z) {
        if (z) {
            super.handleVisibleToUserChanged(z);
            startNotificationLogging();
            return;
        }
        stopNotificationLogging();
        super.handleVisibleToUserChanged(z);
    }

    public boolean hasActiveNotifications() {
        return !this.mNotificationData.getActiveNotifications().isEmpty();
    }

    public boolean hideKeyguard() {
        boolean z = this.mLeaveOpenOnKeyguardHide;
        setBarState(0);
        View view = null;
        if (this.mLeaveOpenOnKeyguardHide) {
            this.mLeaveOpenOnKeyguardHide = false;
            long calculateGoingToFullShadeDelay = calculateGoingToFullShadeDelay();
            this.mNotificationPanel.animateToFullShade(calculateGoingToFullShadeDelay);
            if (this.mDraggedDownRow != null) {
                this.mDraggedDownRow.setUserLocked(false);
                this.mDraggedDownRow = null;
            }
            View view2 = this.mPendingRemoteInputView;
            this.mPendingRemoteInputView = null;
            view = view2;
            if (this.mNavigationBarView != null) {
                this.mNavigationBarView.setLayoutTransitionsEnabled(false);
                this.mNavigationBarView.postDelayed(new Runnable(this) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.45
                    final PhoneStatusBar this$0;

                    {
                        this.this$0 = this;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.this$0.mNavigationBarView.setLayoutTransitionsEnabled(true);
                    }
                }, 448 + calculateGoingToFullShadeDelay);
                view = view2;
            }
        } else {
            instantCollapseNotificationPanel();
        }
        updateKeyguardState(z, false);
        if (view != null) {
            view.callOnClick();
        }
        if (this.mQSPanel != null) {
            this.mQSPanel.refreshAllTiles();
        }
        this.mHandler.removeMessages(1003);
        releaseGestureWakeLock();
        this.mNotificationPanel.onAffordanceLaunchEnded();
        this.mNotificationPanel.animate().cancel();
        this.mNotificationPanel.setAlpha(1.0f);
        return z;
    }

    protected void inflateNavigationBarView(Context context) {
        this.mNavigationBarView = (NavigationBarView) View.inflate(context, 2130968718, null);
    }

    protected void inflateStatusBarWindow(Context context) {
        this.mStatusBarWindow = (StatusBarWindowView) View.inflate(context, 2130968818, null);
    }

    protected void initSignalCluster(View view) {
        SignalClusterView signalClusterView = (SignalClusterView) view.findViewById(2131886656);
        if (signalClusterView != null) {
            signalClusterView.setSecurityController(this.mSecurityController);
            signalClusterView.setNetworkController(this.mNetworkController);
        }
    }

    public boolean interceptMediaKey(KeyEvent keyEvent) {
        return this.mState == 1 ? this.mStatusBarKeyguardViewManager.interceptMediaKey(keyEvent) : false;
    }

    public boolean interceptTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() != 2) {
            Log.d("PhoneStatusBar", String.format("panel: %s at (%f, %f) mDisabled1=0x%08x mDisabled2=0x%08x", MotionEvent.actionToString(motionEvent.getAction()), Float.valueOf(motionEvent.getRawX()), Float.valueOf(motionEvent.getRawY()), Integer.valueOf(this.mDisabled1), Integer.valueOf(this.mDisabled2)));
        }
        if (this.mStatusBarWindowState == 0) {
            if (!(motionEvent.getAction() != 1 ? motionEvent.getAction() == 3 : true) || this.mExpandedVisible) {
                setInteracting(1, true);
                return false;
            }
            setInteracting(1, false);
            return false;
        }
        return false;
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar
    public boolean isCollapsing() {
        return this.mNotificationPanel.isCollapsing();
    }

    public boolean isDozing() {
        return this.mDozing;
    }

    public boolean isFalsingThresholdNeeded() {
        boolean z = true;
        if (getBarState() != 1) {
            z = false;
        }
        return z;
    }

    public boolean isGoingToNotificationShade() {
        return this.mLeaveOpenOnKeyguardHide;
    }

    public boolean isHeadsUp(String str) {
        return this.mHeadsUpManager.isHeadsUp(str);
    }

    public boolean isInLaunchTransition() {
        return !this.mNotificationPanel.isLaunchTransitionRunning() ? this.mNotificationPanel.isLaunchTransitionFinished() : true;
    }

    public boolean isKeyguardCurrentlySecure() {
        return !this.mUnlockMethodCache.canSkipBouncer();
    }

    public boolean isKeyguardFadingAway() {
        return this.mKeyguardFadingAway;
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar
    public boolean isPanelFullyCollapsed() {
        return this.mNotificationPanel.isFullyCollapsed();
    }

    public boolean isScrimSrcModeEnabled() {
        return this.mScrimSrcModeEnabled;
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar
    protected boolean isSnoozedPackage(StatusBarNotification statusBarNotification) {
        return this.mHeadsUpManager.isSnoozed(statusBarNotification.getPackageName());
    }

    public boolean isWakeUpComingFromTouch() {
        return this.mWakeUpComingFromTouch;
    }

    public void keyguardGoingAway() {
        this.mKeyguardGoingAway = true;
        this.mIconController.appTransitionPending();
    }

    protected void loadDimens() {
        Resources resources = this.mContext.getResources();
        int i = this.mNaturalBarHeight;
        this.mNaturalBarHeight = resources.getDimensionPixelSize(17104919);
        if (this.mStatusBarWindowManager != null && this.mNaturalBarHeight != i) {
            this.mStatusBarWindowManager.setBarHeight(this.mNaturalBarHeight);
        }
        this.mMaxAllowedKeyguardNotifications = resources.getInteger(2131755076);
        Log.v("PhoneStatusBar", "defineSlots");
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void makeExpandedInvisible() {
        if (!this.mExpandedVisible || this.mStatusBarWindow == null) {
            return;
        }
        this.mStatusBarView.collapsePanel(false, false, 1.0f);
        this.mNotificationPanel.closeQs();
        this.mExpandedVisible = false;
        if (this.mNavigationBarView != null) {
            this.mNavigationBarView.setSlippery(false);
        }
        visibilityChanged(false);
        this.mStatusBarWindowManager.setPanelVisible(false);
        this.mStatusBarWindowManager.setForceStatusBarVisible(false);
        dismissPopups();
        runPostCollapseRunnables();
        setInteracting(1, false);
        showBouncer();
        disable(this.mDisabledUnmodified1, this.mDisabledUnmodified2, true);
        if (this.mStatusBarKeyguardViewManager.isShowing()) {
            return;
        }
        WindowManagerGlobal.getInstance().trimMemory(20);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void makeExpandedVisible(boolean z) {
        if (z || (!this.mExpandedVisible && panelsEnabled())) {
            this.mExpandedVisible = true;
            if (this.mNavigationBarView != null) {
                this.mNavigationBarView.setSlippery(true);
            }
            updateCarrierLabelVisibility(true);
            this.mStatusBarWindowManager.setPanelVisible(true);
            visibilityChanged(true);
            this.mWaitingForKeyguardExit = false;
            disable(this.mDisabledUnmodified1, this.mDisabledUnmodified2, !z);
            setInteracting(1, true);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public PhoneStatusBarView makeStatusBarView() {
        Context context = this.mContext;
        updateDisplaySize();
        updateResources();
        inflateStatusBarWindow(context);
        this.mStatusBarWindow.setService(this);
        this.mStatusBarWindow.setOnTouchListener(new View.OnTouchListener(this) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.22
            final PhoneStatusBar this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                this.this$0.checkUserAutohide(view, motionEvent);
                if (motionEvent.getAction() == 0 && this.this$0.mExpandedVisible) {
                    this.this$0.animateCollapsePanels();
                }
                return this.this$0.mStatusBarWindow.onTouchEvent(motionEvent);
            }
        });
        this.mNotificationPanel = (NotificationPanelView) this.mStatusBarWindow.findViewById(2131886681);
        this.mNotificationPanel.setStatusBar(this);
        this.mNotificationPanel.setGroupManager(this.mGroupManager);
        this.mStatusBarView = (PhoneStatusBarView) this.mStatusBarWindow.findViewById(2131886674);
        this.mStatusBarView.setBar(this);
        this.mStatusBarView.setPanel(this.mNotificationPanel);
        if (!ActivityManager.isHighEndGfx() && !FeatureOptions.LOW_RAM_SUPPORT) {
            this.mStatusBarWindow.setBackground(null);
            this.mNotificationPanel.setBackground(new FastColorDrawable(context.getColor(2131558512)));
        }
        this.mHeadsUpManager = new HeadsUpManager(context, this.mStatusBarWindow, this.mGroupManager);
        this.mHeadsUpManager.setBar(this);
        this.mHeadsUpManager.addListener(this);
        this.mHeadsUpManager.addListener(this.mNotificationPanel);
        this.mHeadsUpManager.addListener(this.mGroupManager);
        this.mNotificationPanel.setHeadsUpManager(this.mHeadsUpManager);
        this.mNotificationData.setHeadsUpManager(this.mHeadsUpManager);
        this.mGroupManager.setHeadsUpManager(this.mHeadsUpManager);
        try {
            boolean hasNavigationBar = this.mWindowManagerService.hasNavigationBar();
            Log.v("PhoneStatusBar", "hasNavigationBar=" + hasNavigationBar);
            if (hasNavigationBar) {
                createNavigationBarView(context);
            }
        } catch (RemoteException e) {
        }
        this.mAssistManager = new AssistManager(this, context);
        this.mPixelFormat = -1;
        this.mStackScroller = (NotificationStackScrollLayout) this.mStatusBarWindow.findViewById(2131886684);
        this.mStackScroller.setLongPressListener(getNotificationLongClicker());
        this.mStackScroller.setPhoneStatusBar(this);
        this.mStackScroller.setGroupManager(this.mGroupManager);
        this.mStackScroller.setHeadsUpManager(this.mHeadsUpManager);
        this.mGroupManager.setOnGroupChangeListener(this.mStackScroller);
        inflateOverflowContainer();
        inflateEmptyShadeView();
        inflateDismissView();
        this.mExpandedContents = this.mStackScroller;
        this.mBackdrop = (BackDropView) this.mStatusBarWindow.findViewById(2131886709);
        this.mBackdropFront = (ImageView) this.mBackdrop.findViewById(2131886711);
        this.mBackdropBack = (ImageView) this.mBackdrop.findViewById(2131886710);
        this.mScrimController = SystemUIFactory.getInstance().createScrimController((ScrimView) this.mStatusBarWindow.findViewById(2131886712), (ScrimView) this.mStatusBarWindow.findViewById(2131886715), this.mStatusBarWindow.findViewById(2131886713));
        if (this.mScrimSrcModeEnabled) {
            Runnable runnable = new Runnable(this) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.23
                final PhoneStatusBar this$0;

                {
                    this.this$0 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    boolean z = this.this$0.mBackdrop.getVisibility() != 0;
                    this.this$0.mScrimController.setDrawBehindAsSrc(z);
                    this.this$0.mStackScroller.setDrawBackgroundAsSrc(z);
                }
            };
            this.mBackdrop.setOnVisibilityChangedRunnable(runnable);
            runnable.run();
        }
        this.mHeadsUpManager.addListener(this.mScrimController);
        this.mStackScroller.setScrimController(this.mScrimController);
        this.mStatusBarView.setScrimController(this.mScrimController);
        this.mDozeScrimController = new DozeScrimController(this.mScrimController, context);
        this.mKeyguardStatusBar = (KeyguardStatusBarView) this.mStatusBarWindow.findViewById(2131886348);
        this.mKeyguardStatusView = this.mStatusBarWindow.findViewById(2131886355);
        this.mKeyguardBottomArea = (KeyguardBottomAreaView) this.mStatusBarWindow.findViewById(2131886291);
        this.mKeyguardBottomArea.setActivityStarter(this);
        this.mKeyguardBottomArea.setAssistManager(this.mAssistManager);
        this.mKeyguardIndicationController = new KeyguardIndicationController(this.mContext, (KeyguardIndicationTextView) this.mStatusBarWindow.findViewById(2131886292), this.mKeyguardBottomArea.getLockIcon());
        this.mKeyguardBottomArea.setKeyguardIndicationController(this.mKeyguardIndicationController);
        this.mLockscreenWallpaper = new LockscreenWallpaper(this.mContext, this, this.mHandler);
        setAreThereNotifications();
        createIconController();
        this.mHandlerThread = new HandlerThread("PhoneStatusBar", 10);
        this.mHandlerThread.start();
        this.mLocationController = new LocationControllerImpl(this.mContext, this.mHandlerThread.getLooper());
        this.mBatteryController = createBatteryController();
        this.mBatteryController.addStateChangedCallback(new BatteryController.BatteryStateChangeCallback(this) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.24
            final PhoneStatusBar this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
            public void onBatteryLevelChanged(int i, boolean z, boolean z2) {
            }

            @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
            public void onPowerSaveChanged(boolean z) {
                this.this$0.mHandler.post(this.this$0.mCheckBarModes);
                if (this.this$0.mDozeServiceHost != null) {
                    this.this$0.mDozeServiceHost.firePowerSaveChanged(z);
                }
            }
        });
        this.mNetworkController = new NetworkControllerImpl(this.mContext, this.mHandlerThread.getLooper());
        this.mNetworkController.setUserSetupComplete(this.mUserSetup);
        this.mHotspotController = new HotspotControllerImpl(this.mContext);
        this.mBluetoothController = new BluetoothControllerImpl(this.mContext, this.mHandlerThread.getLooper());
        this.mSecurityController = new SecurityControllerImpl(this.mContext);
        if (SIMHelper.isMtkHotKnotSupport()) {
            Log.d("PhoneStatusBar", "makeStatusBarView : HotKnotControllerImpl");
            this.mHotKnotController = new HotKnotControllerImpl(this.mContext);
        } else {
            this.mHotKnotController = null;
        }
        SIMHelper.setContext(this.mContext);
        if (this.mContext.getResources().getBoolean(2131623949)) {
            this.mRotationLockController = new RotationLockControllerImpl(this.mContext);
        }
        this.mUserInfoController = new UserInfoController(this.mContext);
        this.mVolumeComponent = (VolumeComponent) getComponent(VolumeComponent.class);
        if (this.mVolumeComponent != null) {
            this.mZenModeController = this.mVolumeComponent.getZenController();
        }
        Log.d("PhoneStatusBar", "makeStatusBarView : CastControllerImpl +");
        this.mCastController = new CastControllerImpl(this.mContext);
        initSignalCluster(this.mStatusBarView);
        initSignalCluster(this.mKeyguardStatusBar);
        this.mStatusBarPlmnPlugin = PluginManager.getStatusBarPlmnPlugin(context);
        if (supportCustomizeCarrierLabel()) {
            this.mCustomizeCarrierLabel = this.mStatusBarPlmnPlugin.customizeCarrierLabel(this.mNotificationPanel, null);
        }
        this.mFlashlightController = new FlashlightController(this.mContext);
        this.mKeyguardBottomArea.setFlashlightController(this.mFlashlightController);
        this.mKeyguardBottomArea.setPhoneStatusBar(this);
        this.mKeyguardBottomArea.setUserSetupComplete(this.mUserSetup);
        this.mAccessibilityController = new AccessibilityController(this.mContext);
        this.mKeyguardBottomArea.setAccessibilityController(this.mAccessibilityController);
        this.mNextAlarmController = new NextAlarmController(this.mContext);
        this.mLightStatusBarController = new LightStatusBarController(this.mIconController, this.mBatteryController);
        this.mKeyguardMonitor = new KeyguardMonitor(this.mContext);
        if (UserManager.get(this.mContext).isUserSwitcherEnabled()) {
            this.mUserSwitcherController = new UserSwitcherController(this.mContext, this.mKeyguardMonitor, this.mHandler, this);
            createUserSwitcher();
        }
        AutoReinflateContainer autoReinflateContainer = (AutoReinflateContainer) this.mStatusBarWindow.findViewById(2131886683);
        if (autoReinflateContainer != null) {
            QSTileHost createQSTileHost = SystemUIFactory.getInstance().createQSTileHost(this.mContext, this, this.mBluetoothController, this.mLocationController, this.mRotationLockController, this.mNetworkController, this.mZenModeController, this.mHotspotController, this.mCastController, this.mFlashlightController, this.mUserSwitcherController, this.mUserInfoController, this.mKeyguardMonitor, this.mSecurityController, this.mBatteryController, this.mIconController, this.mNextAlarmController, this.mHotKnotController);
            this.mBrightnessMirrorController = new BrightnessMirrorController(this.mStatusBarWindow);
            autoReinflateContainer.addInflateListener(new AutoReinflateContainer.InflateListener(this, createQSTileHost) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.25
                final PhoneStatusBar this$0;
                final QSTileHost val$qsh;

                {
                    this.this$0 = this;
                    this.val$qsh = createQSTileHost;
                }

                @Override // com.android.systemui.AutoReinflateContainer.InflateListener
                public void onInflated(View view) {
                    QSContainer qSContainer = (QSContainer) view.findViewById(2131886585);
                    qSContainer.setHost(this.val$qsh);
                    this.this$0.mQSPanel = qSContainer.getQsPanel();
                    this.this$0.mQSPanel.setBrightnessMirror(this.this$0.mBrightnessMirrorController);
                    this.this$0.mKeyguardStatusBar.setQSPanel(this.this$0.mQSPanel);
                    this.this$0.mHeader = qSContainer.getHeader();
                    this.this$0.initSignalCluster(this.this$0.mHeader);
                    this.this$0.mHeader.setActivityStarter(this.this$0);
                }
            });
        }
        this.mKeyguardStatusBar.setUserInfoController(this.mUserInfoController);
        this.mKeyguardStatusBar.setUserSwitcherController(this.mUserSwitcherController);
        this.mUserInfoController.reloadUserInfo();
        ((BatteryMeterView) this.mStatusBarView.findViewById(2131886720)).setBatteryController(this.mBatteryController);
        this.mKeyguardStatusBar.setBatteryController(this.mBatteryController);
        PowerManager powerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mBroadcastReceiver.onReceive(this.mContext, new Intent(powerManager.isScreenOn() ? "android.intent.action.SCREEN_ON" : "android.intent.action.SCREEN_OFF"));
        this.mGestureWakeLock = powerManager.newWakeLock(10, "GestureWakeLock");
        this.mVibrator = (Vibrator) this.mContext.getSystemService(Vibrator.class);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        context.registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, intentFilter, null, null);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("com.android.systemui.demo");
        context.registerReceiverAsUser(this.mDemoReceiver, UserHandle.ALL, intentFilter2, "android.permission.DUMP", null);
        resetUserSetupObserver();
        ThreadedRenderer.overrideProperty("disableProfileBars", "true");
        ThreadedRenderer.overrideProperty("ambientRatio", String.valueOf(1.5f));
        this.mStatusBarPlmnPlugin.addPlmn((LinearLayout) this.mStatusBarView.findViewById(2131886676), this.mContext);
        return this.mStatusBarView;
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar
    public void maybeEscalateHeadsUp() {
        for (HeadsUpManager.HeadsUpEntry headsUpEntry : this.mHeadsUpManager.getAllEntries()) {
            StatusBarNotification statusBarNotification = headsUpEntry.entry.notification;
            Notification notification = statusBarNotification.getNotification();
            if (notification.fullScreenIntent != null) {
                Log.d("PhoneStatusBar", "converting a heads up to fullScreen");
                try {
                    EventLog.writeEvent(36003, statusBarNotification.getKey());
                    notification.fullScreenIntent.send();
                    headsUpEntry.entry.notifyFullScreenIntentLaunched();
                } catch (PendingIntent.CanceledException e) {
                }
            }
        }
        this.mHeadsUpManager.releaseAllImmediately();
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void notificationLightOff() {
        if (this.mDozeServiceHost != null) {
            this.mDozeServiceHost.fireNotificationLight(false);
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void notificationLightPulse(int i, int i2, int i3) {
        if (this.mDozeServiceHost != null) {
            this.mDozeServiceHost.fireNotificationLight(true);
        }
    }

    public void notifyFpAuthModeChanged() {
        updateDozing();
    }

    @Override // com.android.systemui.statusbar.ActivatableNotificationView.OnActivatedListener
    public void onActivated(ActivatableNotificationView activatableNotificationView) {
        EventLogTags.writeSysuiLockscreenGesture(7, 0, 0);
        this.mKeyguardIndicationController.showTransientIndication(2131493607);
        ActivatableNotificationView activatedChild = this.mStackScroller.getActivatedChild();
        if (activatedChild != null) {
            activatedChild.makeInactive(true);
        }
        this.mStackScroller.setActivatedChild(activatableNotificationView);
    }

    @Override // com.android.systemui.statusbar.ActivatableNotificationView.OnActivatedListener
    public void onActivationReset(ActivatableNotificationView activatableNotificationView) {
        if (activatableNotificationView == this.mStackScroller.getActivatedChild()) {
            this.mKeyguardIndicationController.hideTransientIndication();
            this.mStackScroller.setActivatedChild(null);
        }
    }

    public boolean onBackPressed() {
        if (this.mStatusBarKeyguardViewManager.onBackPressed()) {
            return true;
        }
        if (this.mNotificationPanel.isQsExpanded()) {
            if (this.mNotificationPanel.isQsDetailShowing()) {
                this.mNotificationPanel.closeQsDetail();
                return true;
            }
            this.mNotificationPanel.animateCloseQs();
            return true;
        } else if (this.mState == 1 || this.mState == 2) {
            return false;
        } else {
            animateCollapsePanels();
            return true;
        }
    }

    public void onCameraHintStarted() {
        this.mFalsingManager.onCameraHintStarted();
        this.mKeyguardIndicationController.showTransientIndication(2131493611);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void onCameraLaunchGestureDetected(int i) {
        this.mLastCameraLaunchSource = i;
        if (this.mStartedGoingToSleep) {
            this.mLaunchCameraOnFinishedGoingToSleep = true;
            return;
        }
        if (this.mNotificationPanel.canCameraGestureBeLaunched(this.mStatusBarKeyguardViewManager.isShowing() ? this.mExpandedVisible : false)) {
            if (!this.mDeviceInteractive) {
                ((PowerManager) this.mContext.getSystemService(PowerManager.class)).wakeUp(SystemClock.uptimeMillis(), "com.android.systemui:CAMERA_GESTURE");
                this.mStatusBarKeyguardViewManager.notifyDeviceWakeUpRequested();
            }
            vibrateForCameraGesture();
            if (!this.mStatusBarKeyguardViewManager.isShowing()) {
                startActivity(KeyguardBottomAreaView.INSECURE_CAMERA_INTENT, true);
                return;
            }
            if (!this.mDeviceInteractive) {
                this.mScrimController.dontAnimateBouncerChangesUntilNextFrame();
                this.mGestureWakeLock.acquire(6000L);
            }
            if (this.mScreenTurningOn || this.mStatusBarKeyguardViewManager.isScreenTurnedOn()) {
                this.mNotificationPanel.launchCamera(this.mDeviceInteractive, i);
            } else {
                this.mLaunchCameraOnScreenTurningOn = true;
            }
        }
    }

    public void onClosingFinished() {
        runPostCollapseRunnables();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.BaseStatusBar, com.android.systemui.SystemUI
    public void onConfigurationChanged(Configuration configuration) {
        updateResources();
        updateDisplaySize();
        super.onConfigurationChanged(configuration);
        Log.v("PhoneStatusBar", "configuration changed: " + this.mContext.getResources().getConfiguration());
        repositionNavigationBar();
        updateRowStates();
        this.mScreenPinningRequest.onConfigurationChanged();
        this.mNetworkController.onConfigurationChanged();
    }

    @Override // com.android.systemui.statusbar.DragDownHelper.DragDownCallback
    public void onCrossedThreshold(boolean z) {
        this.mStackScroller.setDimmed(!z, true);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.BaseStatusBar
    public void onDensityOrFontScaleChanged() {
        super.onDensityOrFontScaleChanged();
        this.mScrimController.onDensityOrFontScaleChanged();
        this.mStatusBarView.onDensityOrFontScaleChanged();
        if (this.mBrightnessMirrorController != null) {
            this.mBrightnessMirrorController.onDensityOrFontScaleChanged();
        }
        inflateSignalClusters();
        this.mIconController.onDensityOrFontScaleChanged();
        inflateDismissView();
        updateClearAll();
        inflateEmptyShadeView();
        updateEmptyShadeView();
        inflateOverflowContainer();
        this.mStatusBarKeyguardViewManager.onDensityOrFontScaleChanged();
        this.mUserInfoController.onDensityOrFontScaleChanged();
        if (this.mUserSwitcherController != null) {
            this.mUserSwitcherController.onDensityOrFontScaleChanged();
        }
        if (this.mKeyguardUserSwitcher != null) {
            this.mKeyguardUserSwitcher.onDensityOrFontScaleChanged();
        }
    }

    @Override // com.android.systemui.statusbar.DragDownHelper.DragDownCallback
    public void onDragDownReset() {
        this.mStackScroller.setDimmed(true, true);
        this.mStackScroller.resetScrollPosition();
    }

    @Override // com.android.systemui.statusbar.DragDownHelper.DragDownCallback
    public boolean onDraggedDown(View view, int i) {
        if (hasActiveNotifications()) {
            EventLogTags.writeSysuiLockscreenGesture(2, (int) (i / this.mDisplayMetrics.density), 0);
            goToLockedShade(view);
            if (view instanceof ExpandableNotificationRow) {
                ((ExpandableNotificationRow) view).onExpandedByGesture(true);
                return true;
            }
            return true;
        }
        return false;
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar, com.android.systemui.statusbar.ExpandableNotificationRow.OnExpandClickListener
    public void onExpandClicked(NotificationData.Entry entry, boolean z) {
        this.mHeadsUpManager.setExpanded(entry, z);
        if (this.mState == 1 && z) {
            goToLockedShade(entry.row);
        }
    }

    public void onFinishedGoingToSleep() {
        this.mNotificationPanel.onAffordanceLaunchEnded();
        releaseGestureWakeLock();
        this.mLaunchCameraOnScreenTurningOn = false;
        this.mStartedGoingToSleep = false;
        this.mDeviceInteractive = false;
        this.mWakeUpComingFromTouch = false;
        this.mWakeUpTouchLocation = null;
        this.mStackScroller.setAnimationsEnabled(false);
        updateVisibleToUser();
        if (this.mLaunchCameraOnFinishedGoingToSleep) {
            this.mLaunchCameraOnFinishedGoingToSleep = false;
            this.mHandler.post(new Runnable(this) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.48
                final PhoneStatusBar this$0;

                {
                    this.this$0 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$0.onCameraLaunchGestureDetected(this.this$0.mLastCameraLaunchSource);
                }
            });
        }
    }

    @Override // com.android.systemui.statusbar.policy.HeadsUpManager.OnHeadsUpChangedListener
    public void onHeadsUpPinned(ExpandableNotificationRow expandableNotificationRow) {
        dismissVolumeDialog();
    }

    @Override // com.android.systemui.statusbar.policy.HeadsUpManager.OnHeadsUpChangedListener
    public void onHeadsUpPinnedModeChanged(boolean z) {
        if (!z) {
            if (!this.mNotificationPanel.isFullyCollapsed() || this.mNotificationPanel.isTracking()) {
                this.mStatusBarWindowManager.setHeadsUpShowing(false);
                return;
            }
            this.mHeadsUpManager.setHeadsUpGoingAway(true);
            this.mStackScroller.runAfterAnimationFinished(new Runnable(this) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.37
                final PhoneStatusBar this$0;

                {
                    this.this$0 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    if (this.this$0.mHeadsUpManager.hasPinnedHeadsUp()) {
                        return;
                    }
                    this.this$0.mStatusBarWindowManager.setHeadsUpShowing(false);
                    this.this$0.mHeadsUpManager.setHeadsUpGoingAway(false);
                }
            });
            return;
        }
        this.mStatusBarWindowManager.setHeadsUpShowing(true);
        this.mStatusBarWindowManager.setForceStatusBarVisible(true);
        if (this.mNotificationPanel.isFullyCollapsed()) {
            this.mNotificationPanel.requestLayout();
            this.mStatusBarWindowManager.setForceWindowCollapsed(true);
            this.mNotificationPanel.post(new Runnable(this) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.36
                final PhoneStatusBar this$0;

                {
                    this.this$0 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$0.mStatusBarWindowManager.setForceWindowCollapsed(false);
                }
            });
        }
    }

    @Override // com.android.systemui.statusbar.policy.HeadsUpManager.OnHeadsUpChangedListener
    public void onHeadsUpStateChanged(NotificationData.Entry entry, boolean z) {
        if (z || !this.mHeadsUpEntriesToRemoveOnSwitch.contains(entry)) {
            updateNotificationRanking(null);
            return;
        }
        removeNotification(entry.key, this.mLatestRankingMap);
        this.mHeadsUpEntriesToRemoveOnSwitch.remove(entry);
        if (this.mHeadsUpEntriesToRemoveOnSwitch.isEmpty()) {
            this.mLatestRankingMap = null;
        }
    }

    @Override // com.android.systemui.statusbar.policy.HeadsUpManager.OnHeadsUpChangedListener
    public void onHeadsUpUnPinned(ExpandableNotificationRow expandableNotificationRow) {
    }

    public void onHintFinished() {
        this.mKeyguardIndicationController.hideTransientIndicationDelayed(1200L);
    }

    public void onKeyguardViewManagerStatesUpdated() {
        logStateToEventlog();
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar
    public void onLockedNotificationImportanceChange(KeyguardHostView.OnDismissAction onDismissAction) {
        this.mLeaveOpenOnKeyguardHide = true;
        dismissKeyguardThenExecute(onDismissAction, true);
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar
    protected void onLockedRemoteInput(ExpandableNotificationRow expandableNotificationRow, View view) {
        this.mLeaveOpenOnKeyguardHide = true;
        showBouncer();
        this.mPendingRemoteInputView = view;
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar
    protected void onLockedWorkRemoteInput(int i, ExpandableNotificationRow expandableNotificationRow, View view) {
        animateCollapsePanels();
        startWorkChallengeIfNecessary(i, null, null);
        this.mPendingWorkRemoteInputView = view;
    }

    public boolean onMenuPressed() {
        if (this.mDeviceInteractive && this.mState != 0 && this.mStatusBarKeyguardViewManager.shouldDismissOnMenuPressed()) {
            animateCollapsePanels(2, true);
            return true;
        }
        return false;
    }

    public void onPhoneHintStarted() {
        this.mFalsingManager.onLeftAffordanceHintStarted();
        this.mKeyguardIndicationController.showTransientIndication(2131493609);
    }

    public void onScreenTurnedOff() {
        this.mFalsingManager.onScreenOff();
    }

    public void onScreenTurnedOn() {
        this.mScreenTurningOn = false;
        this.mDozeScrimController.onScreenTurnedOn();
    }

    public void onScreenTurningOn() {
        this.mScreenTurningOn = true;
        this.mFalsingManager.onScreenTurningOn();
        this.mNotificationPanel.onScreenTurningOn();
        if (this.mLaunchCameraOnScreenTurningOn) {
            this.mNotificationPanel.launchCamera(false, this.mLastCameraLaunchSource);
            this.mLaunchCameraOnScreenTurningOn = false;
        }
    }

    public boolean onSpacePressed() {
        if (!this.mDeviceInteractive || this.mState == 0) {
            return false;
        }
        animateCollapsePanels(2, true);
        return true;
    }

    public void onStartedGoingToSleep() {
        this.mStartedGoingToSleep = true;
    }

    public void onStartedWakingUp() {
        this.mDeviceInteractive = true;
        this.mStackScroller.setAnimationsEnabled(true);
        this.mNotificationPanel.setTouchDisabled(false);
        updateVisibleToUser();
    }

    @Override // com.android.systemui.statusbar.DragDownHelper.DragDownCallback
    public void onTouchSlopExceeded() {
        this.mStackScroller.removeLongPressCallback();
    }

    public void onTrackingStarted() {
        runPostCollapseRunnables();
    }

    public void onTrackingStopped(boolean z) {
        if ((this.mState != 1 && this.mState != 2) || z || this.mUnlockMethodCache.canSkipBouncer()) {
            return;
        }
        showBouncer();
    }

    public void onUnlockHintStarted() {
        this.mFalsingManager.onUnlockHintStarted();
        this.mKeyguardIndicationController.showTransientIndication(2131493608);
    }

    @Override // com.android.systemui.statusbar.phone.UnlockMethodCache.OnUnlockMethodChangedListener
    public void onUnlockMethodStateChanged() {
        logStateToEventlog();
    }

    public void onVoiceAssistHintStarted() {
        this.mFalsingManager.onLeftAffordanceHintStarted();
        this.mKeyguardIndicationController.showTransientIndication(2131493610);
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar
    protected void onWorkChallengeUnlocked() {
        if (this.mPendingWorkRemoteInputView != null) {
            View view = this.mPendingWorkRemoteInputView;
            this.mNotificationPanel.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener(this, new AnonymousClass46(this)) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.47
                final PhoneStatusBar this$0;
                final Runnable val$clickPendingViewRunnable;

                {
                    this.this$0 = this;
                    this.val$clickPendingViewRunnable = r5;
                }

                @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
                public void onGlobalLayout() {
                    if (this.this$0.mNotificationPanel.mStatusBar.getStatusBarWindow().getHeight() != this.this$0.mNotificationPanel.mStatusBar.getStatusBarHeight()) {
                        this.this$0.mNotificationPanel.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        this.this$0.mNotificationPanel.post(this.val$clickPendingViewRunnable);
                    }
                }
            });
            instantExpandNotificationsPanel();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean panelsEnabled() {
        boolean z = false;
        if ((this.mDisabled1 & 65536) == 0) {
            if (ONLY_CORE_APPS) {
                z = false;
            } else {
                z = false;
                if (BenesseExtension.getDchaState() == 0) {
                    z = true;
                }
            }
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.BaseStatusBar
    public void performRemoveNotification(StatusBarNotification statusBarNotification, boolean z) {
        NotificationData.Entry entry = this.mNotificationData.get(statusBarNotification.getKey());
        if (this.mRemoteInputController.isRemoteInputActive(entry)) {
            this.mRemoteInputController.removeRemoteInput(entry);
        }
        super.performRemoveNotification(statusBarNotification, z);
    }

    public void postAnimateCollapsePanels() {
        this.mHandler.post(this.mAnimateCollapsePanels);
    }

    public void postAnimateOpenPanels() {
        this.mHandler.sendEmptyMessage(1002);
    }

    public void postQSRunnableDismissingKeyguard(Runnable runnable) {
        this.mHandler.post(new Runnable(this, runnable) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.41
            final PhoneStatusBar this$0;
            final Runnable val$runnable;

            {
                this.this$0 = this;
                this.val$runnable = runnable;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mLeaveOpenOnKeyguardHide = true;
                this.this$0.executeRunnableDismissingKeyguard(this.val$runnable, null, false, false, false);
            }
        });
    }

    public void postStartActivityDismissingKeyguard(PendingIntent pendingIntent) {
        this.mHandler.post(new Runnable(this, pendingIntent) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.42
            final PhoneStatusBar this$0;
            final PendingIntent val$intent;

            {
                this.this$0 = this;
                this.val$intent = pendingIntent;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.startPendingIntentDismissingKeyguard(this.val$intent);
            }
        });
    }

    public void postStartActivityDismissingKeyguard(Intent intent, int i) {
        this.mHandler.postDelayed(new Runnable(this, intent) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.43
            final PhoneStatusBar this$0;
            final Intent val$intent;

            {
                this.this$0 = this;
                this.val$intent = intent;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.handleStartActivityDismissingKeyguard(this.val$intent, true);
            }
        }, i);
    }

    @Override // com.android.systemui.statusbar.phone.ActivityStarter
    public void preventNextAnimation() {
        overrideActivityPendingAppTransition(true);
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar
    protected void refreshLayout(int i) {
        if (this.mNavigationBarView != null) {
            this.mNavigationBarView.setLayoutDirection(i);
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void remQsTile(ComponentName componentName) {
        this.mQSPanel.getHost().removeTile(componentName);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void removeIcon(String str) {
        this.mIconController.removeIcon(str);
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar
    public void removeNotification(String str, NotificationListenerService.RankingMap rankingMap) {
        CharSequence[] charSequenceArr;
        boolean z = false;
        if (this.mHeadsUpManager.isHeadsUp(str)) {
            z = !this.mHeadsUpManager.removeNotification(str, this.mRemoteInputController.isSpinning(str) ? !FORCE_REMOTE_INPUT_HISTORY : false);
        }
        if (str.equals(this.mMediaNotificationKey)) {
            clearCurrentMediaNotification();
            updateMediaMetaData(true, true);
        }
        if (FORCE_REMOTE_INPUT_HISTORY && this.mRemoteInputController.isSpinning(str)) {
            NotificationData.Entry entry = this.mNotificationData.get(str);
            StatusBarNotification statusBarNotification = entry.notification;
            Notification.Builder recoverBuilder = Notification.Builder.recoverBuilder(this.mContext, statusBarNotification.getNotification().clone());
            CharSequence[] charSequenceArray = statusBarNotification.getNotification().extras.getCharSequenceArray("android.remoteInputHistory");
            if (charSequenceArray != null) {
                CharSequence[] charSequenceArr2 = new CharSequence[charSequenceArray.length + 1];
                int i = 0;
                while (true) {
                    charSequenceArr = charSequenceArr2;
                    if (i >= charSequenceArray.length) {
                        break;
                    }
                    charSequenceArr2[i + 1] = charSequenceArray[i];
                    i++;
                }
            } else {
                charSequenceArr = new CharSequence[1];
            }
            charSequenceArr[0] = String.valueOf(entry.remoteInputText);
            recoverBuilder.setRemoteInputHistory(charSequenceArr);
            Notification build = recoverBuilder.build();
            build.contentView = statusBarNotification.getNotification().contentView;
            build.bigContentView = statusBarNotification.getNotification().bigContentView;
            build.headsUpContentView = statusBarNotification.getNotification().headsUpContentView;
            updateNotification(new StatusBarNotification(statusBarNotification.getPackageName(), statusBarNotification.getOpPkg(), statusBarNotification.getId(), statusBarNotification.getTag(), statusBarNotification.getUid(), statusBarNotification.getInitialPid(), 0, build, statusBarNotification.getUser(), statusBarNotification.getPostTime()), null);
            this.mKeysKeptForRemoteInput.add(entry.key);
        } else if (z) {
            this.mLatestRankingMap = rankingMap;
            this.mHeadsUpEntriesToRemoveOnSwitch.add(this.mHeadsUpManager.getEntry(str));
        } else {
            NotificationData.Entry entry2 = this.mNotificationData.get(str);
            if (entry2 != null && this.mRemoteInputController.isRemoteInputActive(entry2)) {
                this.mLatestRankingMap = rankingMap;
                this.mRemoteInputEntriesToRemoveOnCollapse.add(entry2);
                return;
            }
            if (entry2 != null && entry2.row != null) {
                entry2.row.setRemoved();
            }
            handleGroupSummaryRemoved(str, rankingMap);
            StatusBarNotification removeNotificationViews = removeNotificationViews(str, rankingMap);
            Log.d("PhoneStatusBar", "removeNotification key=" + str + " old=" + removeNotificationViews);
            if (removeNotificationViews != null && !hasActiveNotifications() && !this.mNotificationPanel.isTracking() && !this.mNotificationPanel.isQsExpanded()) {
                if (this.mState == 0) {
                    animateCollapsePanels();
                } else if (this.mState == 2) {
                    goToKeyguard();
                }
            }
            setAreThereNotifications();
        }
    }

    protected void repositionNavigationBar() {
        if (this.mNavigationBarView == null || !this.mNavigationBarView.isAttachedToWindow()) {
            return;
        }
        prepareNavigationBarView();
        this.mWindowManager.updateViewLayout(this.mNavigationBarView, getNavigationBarLayoutParams());
    }

    public void requestNotificationUpdate() {
        updateNotifications();
    }

    public void resetUserExpandedStates() {
        ArrayList<NotificationData.Entry> activeNotifications = this.mNotificationData.getActiveNotifications();
        int size = activeNotifications.size();
        for (int i = 0; i < size; i++) {
            NotificationData.Entry entry = activeNotifications.get(i);
            if (entry.row != null) {
                entry.row.resetUserExpansion();
            }
        }
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar
    protected void setAreThereNotifications() {
        View findViewById = this.mStatusBarView.findViewById(2131886675);
        boolean z = hasActiveNotifications() && !areLightsOn();
        if (z != (findViewById.getAlpha() == 1.0f)) {
            if (z) {
                findViewById.setAlpha(0.0f);
                findViewById.setVisibility(0);
            }
            findViewById.animate().alpha(z ? 1 : 0).setDuration(z ? 750 : 250).setInterpolator(new AccelerateInterpolator(2.0f)).setListener(z ? null : new AnimatorListenerAdapter(this, findViewById) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.33
                final PhoneStatusBar this$0;
                final View val$nlo;

                {
                    this.this$0 = this;
                    this.val$nlo = findViewById;
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    this.val$nlo.setVisibility(8);
                }
            }).start();
        }
        findAndUpdateMediaNotifications();
        updateCarrierLabelVisibility(false);
    }

    public void setBarState(int i) {
        if (i != this.mState && this.mVisible && (i == 2 || (i == 0 && isGoingToNotificationShade()))) {
            clearNotificationEffects();
        }
        if (i == 1) {
            removeRemoteInputEntriesKeptUntilCollapsed();
        }
        this.mState = i;
        this.mGroupManager.setStatusBarState(i);
        this.mFalsingManager.setStatusBarState(i);
        this.mStatusBarWindowManager.setStatusBarState(i);
        updateDozing();
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar
    public void setBouncerShowing(boolean z) {
        super.setBouncerShowing(z);
        this.mStatusBarView.setBouncerShowing(z);
        disable(this.mDisabledUnmodified1, this.mDisabledUnmodified2, true);
    }

    @Override // com.android.systemui.statusbar.DragDownHelper.DragDownCallback
    public void setEmptyDragAmount(float f) {
        this.mNotificationPanel.setEmptyDragAmount(f);
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar
    protected void setHeadsUpUser(int i) {
        if (this.mHeadsUpManager != null) {
            this.mHeadsUpManager.setUser(i);
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void setIcon(String str, StatusBarIcon statusBarIcon) {
        this.mIconController.setIcon(str, statusBarIcon);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void setImeWindowStatus(IBinder iBinder, int i, int i2, boolean z) {
        boolean z2 = (i & 2) != 0;
        int i3 = this.mNavigationIconHints;
        int i4 = (i2 == 2 || z2) ? i3 | 1 : i3 & (-2);
        setNavigationIconHints(z ? i4 | 2 : i4 & (-3));
    }

    public void setInteracting(int i, boolean z) {
        boolean z2 = false;
        if ((this.mInteractingWindows & i) != 0) {
            z2 = true;
        }
        boolean z3 = z2 != z;
        this.mInteractingWindows = z ? this.mInteractingWindows | i : this.mInteractingWindows & (i ^ (-1));
        if (this.mInteractingWindows != 0) {
            suspendAutohide();
        } else {
            resumeSuspendedAutohide();
        }
        if (z3 && z && i == 2) {
            dismissVolumeDialog();
        }
        checkBarModes();
    }

    public void setKeyguardFadingAway(long j, long j2, long j3) {
        boolean z = true;
        this.mKeyguardFadingAway = true;
        this.mKeyguardFadingAwayDelay = j2;
        this.mKeyguardFadingAwayDuration = j3;
        this.mWaitingForKeyguardExit = false;
        this.mIconController.appTransitionStarting((j + j3) - 120, 120L);
        int i = this.mDisabledUnmodified1;
        int i2 = this.mDisabledUnmodified2;
        if (j3 <= 0) {
            z = false;
        }
        disable(i, i2, z);
    }

    public void setLightsOn(boolean z) {
        Log.v("PhoneStatusBar", "setLightsOn(" + z + ")");
        if (z) {
            setSystemUiVisibility(0, 0, 0, 1, this.mLastFullscreenStackBounds, this.mLastDockedStackBounds);
        } else {
            setSystemUiVisibility(1, 0, 0, 1, this.mLastFullscreenStackBounds, this.mLastDockedStackBounds);
        }
    }

    public void setPanelExpanded(boolean z) {
        this.mStatusBarWindowManager.setPanelExpanded(z);
        if (z && getBarState() != 1) {
            Log.v("PhoneStatusBar", "clearing notification effects from setPanelExpanded");
            clearNotificationEffects();
        }
        if (z) {
            return;
        }
        removeRemoteInputEntriesKeptUntilCollapsed();
    }

    public void setQsExpanded(boolean z) {
        this.mStatusBarWindowManager.setQsExpanded(z);
        this.mKeyguardStatusView.setImportantForAccessibility(z ? 4 : 0);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void setSystemUiVisibility(int i, int i2, int i3, int i4, Rect rect, Rect rect2) {
        int i5 = this.mSystemUiVisibility;
        int i6 = ((i4 ^ (-1)) & i5) | (i & i4);
        int i7 = i6 ^ i5;
        Log.d("PhoneStatusBar", String.format("setSystemUiVisibility vis=%s mask=%s oldVal=%s newVal=%s diff=%s", Integer.toHexString(i), Integer.toHexString(i4), Integer.toHexString(i5), Integer.toHexString(i6), Integer.toHexString(i7)));
        boolean z = false;
        if (i7 != 0) {
            boolean z2 = (this.mSystemUiVisibility & 16384) > 0;
            this.mSystemUiVisibility = i6;
            if ((i7 & 1) != 0) {
                setAreThereNotifications();
            }
            if ((268435456 & i) != 0) {
                this.mSystemUiVisibility &= -268435457;
                this.mNoAnimationOnNextBarModeChange = true;
            }
            int computeBarMode = computeBarMode(i5, i6, this.mStatusBarView.getBarTransitions(), 67108864, 1073741824, 8);
            int computeBarMode2 = this.mNavigationBarView == null ? -1 : computeBarMode(i5, i6, this.mNavigationBarView.getBarTransitions(), 134217728, Integer.MIN_VALUE, 32768);
            z = computeBarMode != -1;
            boolean z3 = computeBarMode2 != -1;
            boolean z4 = false;
            if (z) {
                z4 = false;
                if (computeBarMode != this.mStatusBarMode) {
                    this.mStatusBarMode = computeBarMode;
                    z4 = true;
                }
            }
            boolean z5 = z4;
            if (z3) {
                z5 = z4;
                if (computeBarMode2 != this.mNavigationBarMode) {
                    this.mNavigationBarMode = computeBarMode2;
                    z5 = true;
                }
            }
            if (z5) {
                checkBarModes();
            }
            if (z || z3) {
                if (this.mStatusBarMode == 1 || this.mNavigationBarMode == 1) {
                    scheduleAutohide();
                } else {
                    cancelAutohide();
                }
            }
            if ((536870912 & i) != 0) {
                this.mSystemUiVisibility &= -536870913;
            }
            if (z2) {
                this.mSystemUiVisibility |= 16384;
            }
            notifyUiVisibilityChanged(this.mSystemUiVisibility);
        }
        this.mLightStatusBarController.onSystemUiVisibilityChanged(i2, i3, i4, rect, rect2, z, this.mStatusBarMode);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void setWindowState(int i, int i2) {
        boolean z = i2 == 0;
        if (this.mStatusBarWindow != null && i == 1 && this.mStatusBarWindowState != i2) {
            this.mStatusBarWindowState = i2;
            if (!z && this.mState == 0) {
                this.mStatusBarView.collapsePanel(false, false, 1.0f);
            }
        }
        if (this.mNavigationBarView == null || i != 2 || this.mNavigationBarWindowState == i2) {
            return;
        }
        this.mNavigationBarWindowState = i2;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.BaseStatusBar
    public void setZenMode(int i) {
        super.setZenMode(i);
        if (this.mIconPolicy != null) {
            this.mIconPolicy.setZenMode(i);
        }
    }

    public boolean shouldDisableNavbarGestures() {
        boolean z = true;
        if (isDeviceProvisioned()) {
            z = (this.mDisabled1 & 33554432) != 0;
        }
        return z;
    }

    public void showKeyguard() {
        if (this.mLaunchTransitionFadingAway) {
            this.mNotificationPanel.animate().cancel();
            onLaunchTransitionFadingEnded();
        }
        this.mHandler.removeMessages(1003);
        if (this.mUserSwitcherController == null || !this.mUserSwitcherController.useFullscreenUserSwitcher()) {
            setBarState(1);
        } else {
            setBarState(3);
        }
        updateKeyguardState(false, false);
        if (!this.mDeviceInteractive) {
            this.mNotificationPanel.setTouchDisabled(true);
        }
        if (this.mState == 1) {
            instantExpandNotificationsPanel();
        } else if (this.mState == 3) {
            instantCollapseNotificationPanel();
        }
        this.mLeaveOpenOnKeyguardHide = false;
        if (this.mDraggedDownRow != null) {
            this.mDraggedDownRow.setUserLocked(false);
            this.mDraggedDownRow.notifyHeightChanged(false);
            this.mDraggedDownRow = null;
        }
        this.mPendingRemoteInputView = null;
        this.mAssistManager.onLockscreenShown();
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void showScreenPinningRequest(int i) {
        if (this.mKeyguardMonitor.isShowing()) {
            return;
        }
        showScreenPinningRequest(i, true);
    }

    public void showScreenPinningRequest(int i, boolean z) {
        this.mScreenPinningRequest.showPrompt(i, z);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void showTvPictureInPictureMenu() {
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar, com.android.systemui.SystemUI
    public void start() {
        this.mDisplay = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
        updateDisplaySize();
        this.mScrimSrcModeEnabled = this.mContext.getResources().getBoolean(2131623951);
        super.start();
        this.mMediaSessionManager = (MediaSessionManager) this.mContext.getSystemService("media_session");
        addNavigationBar();
        this.mIconPolicy = new PhoneStatusBarPolicy(this.mContext, this.mIconController, this.mCastController, this.mHotspotController, this.mUserInfoController, this.mBluetoothController, this.mRotationLockController, this.mNetworkController.getDataSaverController());
        this.mIconPolicy.setCurrentUserSetup(this.mUserSetup);
        this.mSettingsObserver.onChange(false);
        this.mHeadsUpObserver.onChange(true);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("heads_up_notifications_enabled"), true, this.mHeadsUpObserver);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("ticker_gets_heads_up"), true, this.mHeadsUpObserver);
        this.mUnlockMethodCache = UnlockMethodCache.getInstance(this.mContext);
        this.mUnlockMethodCache.addListener(this);
        startKeyguard();
        this.mDozeServiceHost = new DozeServiceHost(this, null);
        KeyguardUpdateMonitor.getInstance(this.mContext).registerCallback(this.mDozeServiceHost);
        putComponent(DozeHost.class, this.mDozeServiceHost);
        putComponent(PhoneStatusBar.class, this);
        setControllerUsers();
        notifyUserAboutHiddenNotifications();
        this.mScreenPinningRequest = new ScreenPinningRequest(this.mContext);
        this.mFalsingManager = FalsingManager.getInstance(this.mContext);
    }

    @Override // com.android.systemui.statusbar.phone.ActivityStarter
    public void startActivity(Intent intent, boolean z) {
        startActivityDismissingKeyguard(intent, false, z);
    }

    @Override // com.android.systemui.statusbar.phone.ActivityStarter
    public void startActivity(Intent intent, boolean z, ActivityStarter.Callback callback) {
        startActivityDismissingKeyguard(intent, false, z, callback);
    }

    public void startActivityDismissingKeyguard(Intent intent, boolean z, boolean z2) {
        startActivityDismissingKeyguard(intent, z, z2, null);
    }

    public void startActivityDismissingKeyguard(Intent intent, boolean z, boolean z2, ActivityStarter.Callback callback) {
        if (!z || isDeviceProvisioned()) {
            boolean wouldLaunchResolverActivity = PreviewInflater.wouldLaunchResolverActivity(this.mContext, intent, this.mCurrentUserId);
            executeRunnableDismissingKeyguard(new Runnable(this, intent, this.mStatusBarKeyguardViewManager.isShowing(), wouldLaunchResolverActivity, callback) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.38
                final PhoneStatusBar this$0;
                final boolean val$afterKeyguardGone;
                final ActivityStarter.Callback val$callback;
                final Intent val$intent;
                final boolean val$keyguardShowing;

                {
                    this.this$0 = this;
                    this.val$intent = intent;
                    this.val$keyguardShowing = r6;
                    this.val$afterKeyguardGone = wouldLaunchResolverActivity;
                    this.val$callback = callback;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$0.mAssistManager.hideAssist();
                    this.val$intent.setFlags(335544320);
                    int i = -6;
                    try {
                        i = ActivityManagerNative.getDefault().startActivityAsUser((IApplicationThread) null, this.this$0.mContext.getBasePackageName(), this.val$intent, this.val$intent.resolveTypeIfNeeded(this.this$0.mContext.getContentResolver()), (IBinder) null, (String) null, 0, 268435456, (ProfilerInfo) null, this.this$0.getActivityOptions(), UserHandle.CURRENT.getIdentifier());
                    } catch (RemoteException e) {
                        Log.w("PhoneStatusBar", "Unable to start activity", e);
                    }
                    this.this$0.overrideActivityPendingAppTransition(this.val$keyguardShowing && !this.val$afterKeyguardGone);
                    if (this.val$callback != null) {
                        this.val$callback.onActivityStarted(i);
                    }
                }
            }, new Runnable(this, callback) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.39
                final PhoneStatusBar this$0;
                final ActivityStarter.Callback val$callback;

                {
                    this.this$0 = this;
                    this.val$callback = callback;
                }

                @Override // java.lang.Runnable
                public void run() {
                    if (this.val$callback != null) {
                        this.val$callback.onActivityStarted(-6);
                    }
                }
            }, z2, wouldLaunchResolverActivity, true);
        }
    }

    protected void startKeyguard() {
        KeyguardViewMediator keyguardViewMediator = (KeyguardViewMediator) getComponent(KeyguardViewMediator.class);
        this.mFingerprintUnlockController = new FingerprintUnlockController(this.mContext, this.mStatusBarWindowManager, this.mDozeScrimController, keyguardViewMediator, this.mScrimController, this);
        this.mStatusBarKeyguardViewManager = keyguardViewMediator.registerStatusBar(this, getBouncerContainer(), this.mStatusBarWindowManager, this.mScrimController, this.mFingerprintUnlockController);
        this.mKeyguardIndicationController.setStatusBarKeyguardViewManager(this.mStatusBarKeyguardViewManager);
        this.mFingerprintUnlockController.setStatusBarKeyguardViewManager(this.mStatusBarKeyguardViewManager);
        this.mIconPolicy.setStatusBarKeyguardViewManager(this.mStatusBarKeyguardViewManager);
        this.mRemoteInputController.addCallback(this.mStatusBarKeyguardViewManager);
        this.mRemoteInputController.addCallback(new AnonymousClass31(this));
        this.mKeyguardViewMediatorCallback = keyguardViewMediator.getViewMediatorCallback();
        this.mLightStatusBarController.setFingerprintUnlockController(this.mFingerprintUnlockController);
    }

    public void startLaunchTransitionTimeout() {
        this.mHandler.sendEmptyMessageDelayed(1003, 5000L);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.BaseStatusBar
    public boolean startWorkChallengeIfNecessary(int i, IntentSender intentSender, String str) {
        this.mPendingWorkRemoteInputView = null;
        return super.startWorkChallengeIfNecessary(i, intentSender, str);
    }

    public void stopWaitingForKeyguardExit() {
        this.mWaitingForKeyguardExit = false;
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar
    protected void toggleSplitScreenMode(int i, int i2) {
        if (this.mRecents == null) {
            return;
        }
        if (WindowManagerProxy.getInstance().getDockSide() == -1) {
            this.mRecents.dockTopTask(-1, 0, null, i);
            return;
        }
        EventBus.getDefault().send(new UndockingTaskEvent());
        if (i2 != -1) {
            MetricsLogger.action(this.mContext, i2);
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void topAppWindowChanged(boolean z) {
        if (this.mNavigationBarView != null) {
            this.mNavigationBarView.setMenuVisibility(z);
        }
        if (z) {
            setLightsOn(true);
        }
    }

    protected void updateCarrierLabelVisibility(boolean z) {
        if (supportCustomizeCarrierLabel()) {
            if (this.mState != 1 && !this.mNotificationPanel.isPanelVisibleBecauseOfHeadsUp()) {
                updateCustomizeCarrierLabelVisibility(z);
            } else if (this.mCustomizeCarrierLabel != null) {
                this.mCustomizeCarrierLabel.setVisibility(8);
            }
        }
    }

    void updateDisplaySize() {
        this.mDisplay.getMetrics(this.mDisplayMetrics);
        this.mDisplay.getSize(this.mCurrentDisplaySize);
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar
    protected void updateHeadsUp(String str, NotificationData.Entry entry, boolean z, boolean z2) {
        if (isHeadsUp(str)) {
            if (z) {
                this.mHeadsUpManager.updateNotification(entry, z2);
            } else {
                this.mHeadsUpManager.removeNotification(str, false);
            }
        } else if (this.mUseHeadsUp && z && z2) {
            this.mHeadsUpManager.showNotification(entry);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void updateKeyguardState(boolean z, boolean z2) {
        if (this.mState == 1) {
            this.mKeyguardIndicationController.setVisible(true);
            this.mNotificationPanel.resetViews();
            if (this.mKeyguardUserSwitcher != null) {
                this.mKeyguardUserSwitcher.setKeyguard(true, z2);
            }
            this.mStatusBarView.removePendingHideExpandedRunnables();
        } else {
            this.mKeyguardIndicationController.setVisible(false);
            if (this.mKeyguardUserSwitcher != null) {
                this.mKeyguardUserSwitcher.setKeyguard(false, (z || this.mState == 2) ? true : z2);
            }
        }
        if (this.mState == 1 || this.mState == 2) {
            this.mScrimController.setKeyguardShowing(true);
        } else {
            this.mScrimController.setKeyguardShowing(false);
        }
        this.mIconPolicy.notifyKeyguardShowingChanged();
        this.mNotificationPanel.setBarState(this.mState, this.mKeyguardFadingAway, z);
        updateDozingState();
        updatePublicMode();
        updateStackScrollerState(z, z2);
        updateNotifications();
        checkBarModes();
        updateCarrierLabelVisibility(false);
        updateMediaMetaData(false, this.mState != 1);
        this.mKeyguardMonitor.notifyKeyguardState(this.mStatusBarKeyguardViewManager.isShowing(), this.mStatusBarKeyguardViewManager.isSecure());
    }

    public void updateMediaMetaData(boolean z, boolean z2) {
        if (this.mBackdrop == null) {
            return;
        }
        if (this.mLaunchTransitionFadingAway) {
            this.mBackdrop.setVisibility(4);
            return;
        }
        BitmapDrawable bitmapDrawable = null;
        if (this.mMediaMetadata != null) {
            Bitmap bitmap = this.mMediaMetadata.getBitmap("android.media.metadata.ART");
            Bitmap bitmap2 = bitmap;
            if (bitmap == null) {
                bitmap2 = this.mMediaMetadata.getBitmap("android.media.metadata.ALBUM_ART");
            }
            bitmapDrawable = null;
            if (bitmap2 != null) {
                bitmapDrawable = new BitmapDrawable(this.mBackdropBack.getResources(), bitmap2);
            }
        }
        boolean z3 = false;
        BitmapDrawable bitmapDrawable2 = bitmapDrawable;
        if (bitmapDrawable == null) {
            Bitmap bitmap3 = this.mLockscreenWallpaper.getBitmap();
            z3 = false;
            bitmapDrawable2 = bitmapDrawable;
            if (bitmap3 != null) {
                bitmapDrawable2 = new LockscreenWallpaper.WallpaperDrawable(this.mBackdropBack.getResources(), bitmap3);
                z3 = this.mStatusBarKeyguardViewManager != null ? this.mStatusBarKeyguardViewManager.isShowing() : false;
            }
        }
        boolean isOccluded = this.mStatusBarKeyguardViewManager != null ? this.mStatusBarKeyguardViewManager.isOccluded() : false;
        if (!(bitmapDrawable2 != null) || ((this.mState == 0 && !z3) || this.mFingerprintUnlockController.getMode() == 2 || isOccluded)) {
            if (this.mBackdrop.getVisibility() != 8) {
                if (this.mFingerprintUnlockController.getMode() == 2 || isOccluded) {
                    this.mBackdrop.setVisibility(8);
                    this.mBackdropBack.setImageDrawable(null);
                    this.mStatusBarWindowManager.setBackdropShowing(false);
                    return;
                }
                this.mStatusBarWindowManager.setBackdropShowing(false);
                this.mBackdrop.animate().alpha(0.002f).setInterpolator(Interpolators.ACCELERATE_DECELERATE).setDuration(300L).setStartDelay(0L).withEndAction(new Runnable(this) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.35
                    final PhoneStatusBar this$0;

                    {
                        this.this$0 = this;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.this$0.mBackdrop.setVisibility(8);
                        this.this$0.mBackdropFront.animate().cancel();
                        this.this$0.mBackdropBack.setImageDrawable(null);
                        this.this$0.mHandler.post(this.this$0.mHideBackdropFront);
                    }
                });
                if (this.mKeyguardFadingAway) {
                    this.mBackdrop.animate().setDuration(this.mKeyguardFadingAwayDuration / 2).setStartDelay(this.mKeyguardFadingAwayDelay).setInterpolator(Interpolators.LINEAR).start();
                    return;
                }
                return;
            }
            return;
        }
        if (this.mBackdrop.getVisibility() != 0) {
            this.mBackdrop.setVisibility(0);
            if (z2) {
                this.mBackdrop.animate().alpha(1.0f).withEndAction(new Runnable(this) { // from class: com.android.systemui.statusbar.phone.PhoneStatusBar.34
                    final PhoneStatusBar this$0;

                    {
                        this.this$0 = this;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.this$0.mStatusBarWindowManager.setBackdropShowing(true);
                    }
                });
            } else {
                this.mBackdrop.animate().cancel();
                this.mBackdrop.setAlpha(1.0f);
                this.mStatusBarWindowManager.setBackdropShowing(true);
            }
            z = true;
        }
        if (z) {
            if (this.mBackdropBack.getDrawable() != null) {
                this.mBackdropFront.setImageDrawable(this.mBackdropBack.getDrawable().getConstantState().newDrawable(this.mBackdropFront.getResources()).mutate());
                if (this.mScrimSrcModeEnabled) {
                    this.mBackdropFront.getDrawable().mutate().setXfermode(this.mSrcOverXferMode);
                }
                this.mBackdropFront.setAlpha(1.0f);
                this.mBackdropFront.setVisibility(0);
            } else {
                this.mBackdropFront.setVisibility(4);
            }
            this.mBackdropBack.setImageDrawable(bitmapDrawable2);
            if (this.mScrimSrcModeEnabled) {
                this.mBackdropBack.getDrawable().mutate().setXfermode(this.mSrcXferMode);
            }
            if (this.mBackdropFront.getVisibility() == 0) {
                this.mBackdropFront.animate().setDuration(250L).alpha(0.0f).withEndAction(this.mHideBackdropFront);
            }
        }
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar
    protected void updateNotificationRanking(NotificationListenerService.RankingMap rankingMap) {
        this.mNotificationData.updateRanking(rankingMap);
        updateNotifications();
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar
    protected void updateNotifications() {
        this.mNotificationData.filterAndSort();
        updateNotificationShade();
        this.mIconController.updateNotificationIcons(this.mNotificationData);
    }

    public void updateRecentsVisibility(boolean z) {
        if (z) {
            this.mSystemUiVisibility |= 16384;
        } else {
            this.mSystemUiVisibility &= -16385;
        }
        notifyUiVisibilityChanged(this.mSystemUiVisibility);
    }

    void updateResources() {
        if (this.mQSPanel != null) {
            this.mQSPanel.updateResources();
        }
        loadDimens();
        if (this.mNotificationPanel != null) {
            this.mNotificationPanel.updateResources();
        }
        if (this.mBrightnessMirrorController != null) {
            this.mBrightnessMirrorController.updateResources();
        }
    }

    public void updateStackScrollerState(boolean z, boolean z2) {
        if (this.mStackScroller == null) {
            return;
        }
        boolean z3 = this.mState == 1;
        this.mStackScroller.setHideSensitive(isLockscreenPublicMode(), z);
        this.mStackScroller.setDimmed(z3, z2);
        NotificationStackScrollLayout notificationStackScrollLayout = this.mStackScroller;
        boolean z4 = true;
        if (z3) {
            z4 = false;
        }
        notificationStackScrollLayout.setExpandingEnabled(z4);
        ActivatableNotificationView activatedChild = this.mStackScroller.getActivatedChild();
        this.mStackScroller.setActivatedChild(null);
        if (activatedChild != null) {
            activatedChild.makeInactive(false);
        }
    }

    public void userActivity() {
        if (this.mState == 1) {
            this.mKeyguardViewMediatorCallback.userActivity();
        }
    }

    @Override // com.android.systemui.statusbar.BaseStatusBar
    public void userSwitched(int i) {
        super.userSwitched(i);
        animateCollapsePanels();
        updatePublicMode();
        updateNotifications();
        resetUserSetupObserver();
        setControllerUsers();
        clearCurrentMediaNotification();
        this.mLockscreenWallpaper.setCurrentUser(i);
        updateMediaMetaData(true, false);
    }

    void vibrate() {
        ((Vibrator) this.mContext.getSystemService("vibrator")).vibrate(250L, VIBRATION_ATTRIBUTES);
    }

    public void wakeUpIfDozing(long j, MotionEvent motionEvent) {
        if (this.mDozing && this.mDozeScrimController.isPulsing()) {
            PowerManager powerManager = (PowerManager) this.mContext.getSystemService("power");
            this.mWakeUpComingFromTouch = true;
            this.mWakeUpTouchLocation = new PointF(motionEvent.getX(), motionEvent.getY());
            this.mNotificationPanel.setTouchDisabled(false);
            this.mStatusBarKeyguardViewManager.notifyDeviceWakeUpRequested();
            this.mFalsingManager.onScreenOnFromTouch();
        }
    }
}
