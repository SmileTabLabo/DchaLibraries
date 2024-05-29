package com.android.systemui.statusbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.ActivityOptions;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.app.TaskStackBuilder;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.AsyncTask;
import android.os.BenesseExtension;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IRemoteCallback;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.service.dreams.IDreamManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.service.vr.IVrManager;
import android.service.vr.IVrStateCallbacks;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.Display;
import android.view.IWindowManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.view.accessibility.AccessibilityManager;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardHostView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.DejankUtils;
import com.android.systemui.Interpolators;
import com.android.systemui.RecentsComponent;
import com.android.systemui.SwipeHelper;
import com.android.systemui.SystemUI;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.recents.Recents;
import com.android.systemui.statusbar.ActivatableNotificationView;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.NotificationGuts;
import com.android.systemui.statusbar.phone.NavigationBarView;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.PreviewInflater;
import com.android.systemui.statusbar.policy.RemoteInputView;
import com.android.systemui.statusbar.stack.NotificationStackScrollLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
/* loaded from: a.zip:com/android/systemui/statusbar/BaseStatusBar.class */
public abstract class BaseStatusBar extends SystemUI implements CommandQueue.Callbacks, ActivatableNotificationView.OnActivatedListener, ExpandableNotificationRow.ExpansionLogger, NotificationData.Environment, ExpandableNotificationRow.OnExpandClickListener, NotificationGuts.OnGutsClosedListener {
    protected AccessibilityManager mAccessibilityManager;
    protected boolean mAllowLockscreenRemoteInput;
    protected AssistManager mAssistManager;
    protected IStatusBarService mBarService;
    protected boolean mBouncerShowing;
    protected CommandQueue mCommandQueue;
    private int mDensity;
    protected boolean mDeviceInteractive;
    protected DevicePolicyManager mDevicePolicyManager;
    protected DismissView mDismissView;
    protected Display mDisplay;
    protected IDreamManager mDreamManager;
    protected EmptyShadeView mEmptyShadeView;
    private float mFontScale;
    protected HeadsUpManager mHeadsUpManager;
    protected NotificationOverflowContainer mKeyguardIconOverflowContainer;
    private KeyguardManager mKeyguardManager;
    private Locale mLocale;
    private LockPatternUtils mLockPatternUtils;
    protected NotificationData mNotificationData;
    private NotificationGuts mNotificationGutsExposed;
    protected PowerManager mPowerManager;
    protected RecentsComponent mRecents;
    protected RemoteInputController mRemoteInputController;
    protected boolean mShowLockscreenNotifications;
    protected NotificationStackScrollLayout mStackScroller;
    protected int mState;
    protected StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
    private UserManager mUserManager;
    protected boolean mVisible;
    private boolean mVisibleToUser;
    protected boolean mVrMode;
    protected WindowManager mWindowManager;
    protected IWindowManager mWindowManagerService;
    protected int mZenMode;
    public static final boolean ENABLE_REMOTE_INPUT = SystemProperties.getBoolean("debug.enable_remote_input", true);
    public static final boolean ENABLE_CHILD_NOTIFICATIONS = SystemProperties.getBoolean("debug.child_notifs", true);
    public static final boolean FORCE_REMOTE_INPUT_HISTORY = SystemProperties.getBoolean("debug.force_remoteinput_history", false);
    private static boolean ENABLE_LOCK_SCREEN_ALLOW_REMOTE_INPUT = false;
    protected H mHandler = createHandler();
    protected NotificationGroupManager mGroupManager = new NotificationGroupManager();
    protected int mCurrentUserId = 0;
    protected final SparseArray<UserInfo> mCurrentProfiles = new SparseArray<>();
    protected int mLayoutDirection = -1;
    protected NavigationBarView mNavigationBarView = null;
    protected ArraySet<NotificationData.Entry> mHeadsUpEntriesToRemoveOnSwitch = new ArraySet<>();
    protected ArraySet<NotificationData.Entry> mRemoteInputEntriesToRemoveOnCollapse = new ArraySet<>();
    protected ArraySet<String> mKeysKeptForRemoteInput = new ArraySet<>();
    protected boolean mUseHeadsUp = false;
    protected boolean mHeadsUpTicker = false;
    protected boolean mDisableNotificationAlerts = false;
    private boolean mLockscreenPublicMode = false;
    private final SparseBooleanArray mUsersAllowingPrivateNotifications = new SparseBooleanArray();
    private final SparseBooleanArray mUsersAllowingNotifications = new SparseBooleanArray();
    private boolean mDeviceProvisioned = false;
    private NotificationClicker mNotificationClicker = new NotificationClicker(this, null);
    private final IVrStateCallbacks mVrStateCallbacks = new IVrStateCallbacks.Stub(this) { // from class: com.android.systemui.statusbar.BaseStatusBar.1
        final BaseStatusBar this$0;

        {
            this.this$0 = this;
        }

        public void onVrStateChanged(boolean z) {
            this.this$0.mVrMode = z;
        }
    };
    protected final ContentObserver mSettingsObserver = new ContentObserver(this, this.mHandler) { // from class: com.android.systemui.statusbar.BaseStatusBar.2
        final BaseStatusBar this$0;

        {
            this.this$0 = this;
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            boolean z2 = Settings.Global.getInt(this.this$0.mContext.getContentResolver(), "device_provisioned", 0) != 0;
            if (z2 != this.this$0.mDeviceProvisioned) {
                this.this$0.mDeviceProvisioned = z2;
                this.this$0.updateNotifications();
            }
            this.this$0.setZenMode(Settings.Global.getInt(this.this$0.mContext.getContentResolver(), "zen_mode", 0));
            this.this$0.updateLockscreenNotificationSetting();
        }
    };
    private final ContentObserver mLockscreenSettingsObserver = new ContentObserver(this, this.mHandler) { // from class: com.android.systemui.statusbar.BaseStatusBar.3
        final BaseStatusBar this$0;

        {
            this.this$0 = this;
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            this.this$0.mUsersAllowingPrivateNotifications.clear();
            this.this$0.mUsersAllowingNotifications.clear();
            this.this$0.updateNotifications();
        }
    };
    private RemoteViews.OnClickHandler mOnClickHandler = new AnonymousClass4(this);
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver(this) { // from class: com.android.systemui.statusbar.BaseStatusBar.5
        final BaseStatusBar this$0;

        {
            this.this$0 = this;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            UserInfo userInfo;
            String action = intent.getAction();
            if ("android.intent.action.USER_SWITCHED".equals(action)) {
                this.this$0.mCurrentUserId = intent.getIntExtra("android.intent.extra.user_handle", -1);
                this.this$0.updateCurrentProfilesCache();
                Log.v("StatusBar", "userId " + this.this$0.mCurrentUserId + " is in the house");
                this.this$0.updateLockscreenNotificationSetting();
                this.this$0.userSwitched(this.this$0.mCurrentUserId);
            } else if ("android.intent.action.USER_ADDED".equals(action)) {
                this.this$0.updateCurrentProfilesCache();
            } else if ("android.intent.action.USER_PRESENT".equals(action)) {
                List list = null;
                try {
                    list = ActivityManagerNative.getDefault().getRecentTasks(1, 5, this.this$0.mCurrentUserId).getList();
                } catch (RemoteException e) {
                }
                if (list == null || list.size() <= 0 || (userInfo = this.this$0.mUserManager.getUserInfo(((ActivityManager.RecentTaskInfo) list.get(0)).userId)) == null || !userInfo.isManagedProfile()) {
                    return;
                }
                Toast makeText = Toast.makeText(this.this$0.mContext, 2131493696, 0);
                TextView textView = (TextView) makeText.getView().findViewById(16908299);
                textView.setCompoundDrawablesRelativeWithIntrinsicBounds(2130838281, 0, 0, 0);
                textView.setCompoundDrawablePadding(this.this$0.mContext.getResources().getDimensionPixelSize(2131689972));
                makeText.show();
            } else if ("com.android.systemui.statusbar.banner_action_cancel".equals(action) || "com.android.systemui.statusbar.banner_action_setup".equals(action)) {
                ((NotificationManager) this.this$0.mContext.getSystemService("notification")).cancel(2131886134);
                Settings.Secure.putInt(this.this$0.mContext.getContentResolver(), "show_note_about_notification_hiding", 0);
                if ("com.android.systemui.statusbar.banner_action_setup".equals(action) && BenesseExtension.getDchaState() == 0) {
                    this.this$0.animateCollapsePanels(2, true);
                    this.this$0.mContext.startActivity(new Intent("android.settings.ACTION_APP_NOTIFICATION_REDACTION").addFlags(268435456));
                }
            } else if ("com.android.systemui.statusbar.work_challenge_unlocked_notification_action".equals(action)) {
                IntentSender intentSender = (IntentSender) intent.getParcelableExtra("android.intent.extra.INTENT");
                String stringExtra = intent.getStringExtra("android.intent.extra.INDEX");
                if (intentSender != null) {
                    try {
                        this.this$0.mContext.startIntentSender(intentSender, null, 0, 0, 0);
                    } catch (IntentSender.SendIntentException e2) {
                    }
                }
                if (stringExtra != null) {
                    try {
                        this.this$0.mBarService.onNotificationClick(stringExtra);
                    } catch (RemoteException e3) {
                    }
                }
                this.this$0.onWorkChallengeUnlocked();
            }
        }
    };
    private final BroadcastReceiver mAllUsersReceiver = new BroadcastReceiver(this) { // from class: com.android.systemui.statusbar.BaseStatusBar.6
        final BaseStatusBar this$0;

        {
            this.this$0 = this;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED".equals(intent.getAction()) && this.this$0.isCurrentProfile(getSendingUserId())) {
                this.this$0.mUsersAllowingPrivateNotifications.clear();
                this.this$0.updateLockscreenNotificationSetting();
                this.this$0.updateNotifications();
            }
        }
    };
    private final NotificationListenerService mNotificationListener = new AnonymousClass7(this);
    protected View.OnTouchListener mRecentsPreloadOnTouchListener = new View.OnTouchListener(this) { // from class: com.android.systemui.statusbar.BaseStatusBar.8
        final BaseStatusBar this$0;

        {
            this.this$0 = this;
        }

        @Override // android.view.View.OnTouchListener
        public boolean onTouch(View view, MotionEvent motionEvent) {
            int action = motionEvent.getAction() & 255;
            if (action == 0) {
                this.this$0.preloadRecents();
                return false;
            } else if (action == 3) {
                this.this$0.cancelPreloadingRecents();
                return false;
            } else if (action != 1 || view.isPressed()) {
                return false;
            } else {
                this.this$0.cancelPreloadingRecents();
                return false;
            }
        }
    };

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.android.systemui.statusbar.BaseStatusBar$10  reason: invalid class name */
    /* loaded from: a.zip:com/android/systemui/statusbar/BaseStatusBar$10.class */
    public class AnonymousClass10 implements KeyguardHostView.OnDismissAction {
        final BaseStatusBar this$0;
        final int val$appUid;
        final Intent val$intent;
        final boolean val$keyguardShowing;

        AnonymousClass10(BaseStatusBar baseStatusBar, boolean z, Intent intent, int i) {
            this.this$0 = baseStatusBar;
            this.val$keyguardShowing = z;
            this.val$intent = intent;
            this.val$appUid = i;
        }

        @Override // com.android.keyguard.KeyguardHostView.OnDismissAction
        public boolean onDismiss() {
            AsyncTask.execute(new Runnable(this, this.val$keyguardShowing, this.val$intent, this.val$appUid) { // from class: com.android.systemui.statusbar.BaseStatusBar.10.1
                final AnonymousClass10 this$1;
                final int val$appUid;
                final Intent val$intent;
                final boolean val$keyguardShowing;

                {
                    this.this$1 = this;
                    this.val$keyguardShowing = r5;
                    this.val$intent = r6;
                    this.val$appUid = r7;
                }

                @Override // java.lang.Runnable
                public void run() {
                    try {
                        if (this.val$keyguardShowing) {
                            ActivityManagerNative.getDefault().keyguardWaitingForActivityDrawn();
                        }
                        TaskStackBuilder.create(this.this$1.this$0.mContext).addNextIntentWithParentStack(this.val$intent).startActivities(this.this$1.this$0.getActivityOptions(), new UserHandle(UserHandle.getUserId(this.val$appUid)));
                        this.this$1.this$0.overrideActivityPendingAppTransition(this.val$keyguardShowing);
                    } catch (RemoteException e) {
                    }
                }
            });
            this.this$0.animateCollapsePanels(2, true);
            return true;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.android.systemui.statusbar.BaseStatusBar$12  reason: invalid class name */
    /* loaded from: a.zip:com/android/systemui/statusbar/BaseStatusBar$12.class */
    public class AnonymousClass12 implements View.OnClickListener {
        final BaseStatusBar this$0;
        final NotificationGuts val$guts;
        final ExpandableNotificationRow val$row;
        final StatusBarNotification val$sbn;

        AnonymousClass12(BaseStatusBar baseStatusBar, NotificationGuts notificationGuts, StatusBarNotification statusBarNotification, ExpandableNotificationRow expandableNotificationRow) {
            this.this$0 = baseStatusBar;
            this.val$guts = notificationGuts;
            this.val$sbn = statusBarNotification;
            this.val$row = expandableNotificationRow;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            if (!this.val$guts.hasImportanceChanged() || !this.this$0.isLockscreenPublicMode() || (this.this$0.mState != 1 && this.this$0.mState != 2)) {
                this.this$0.saveImportanceCloseControls(this.val$sbn, this.val$row, this.val$guts, view);
                return;
            }
            this.this$0.onLockedNotificationImportanceChange(new KeyguardHostView.OnDismissAction(this, this.val$sbn, this.val$row, this.val$guts, view) { // from class: com.android.systemui.statusbar.BaseStatusBar.12.1
                final AnonymousClass12 this$1;
                final NotificationGuts val$guts;
                final ExpandableNotificationRow val$row;
                final StatusBarNotification val$sbn;
                final View val$v;

                {
                    this.this$1 = this;
                    this.val$sbn = r5;
                    this.val$row = r6;
                    this.val$guts = r7;
                    this.val$v = view;
                }

                @Override // com.android.keyguard.KeyguardHostView.OnDismissAction
                public boolean onDismiss() {
                    this.this$1.this$0.saveImportanceCloseControls(this.val$sbn, this.val$row, this.val$guts, this.val$v);
                    return true;
                }
            });
        }
    }

    /* renamed from: com.android.systemui.statusbar.BaseStatusBar$13  reason: invalid class name */
    /* loaded from: a.zip:com/android/systemui/statusbar/BaseStatusBar$13.class */
    class AnonymousClass13 implements SwipeHelper.LongPressListener {
        final BaseStatusBar this$0;

        AnonymousClass13(BaseStatusBar baseStatusBar) {
            this.this$0 = baseStatusBar;
        }

        @Override // com.android.systemui.SwipeHelper.LongPressListener
        public boolean onLongPress(View view, int i, int i2) {
            if (view instanceof ExpandableNotificationRow) {
                if (view.getWindowToken() == null) {
                    Log.e("StatusBar", "Trying to show notification guts, but not attached to window");
                    return false;
                }
                ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
                this.this$0.bindGuts(expandableNotificationRow);
                NotificationGuts guts = expandableNotificationRow.getGuts();
                if (guts == null) {
                    return false;
                }
                if (guts.getVisibility() == 0) {
                    this.this$0.dismissPopups(i, i2);
                    return false;
                }
                MetricsLogger.action(this.this$0.mContext, 204);
                guts.setVisibility(4);
                guts.post(new Runnable(this, guts, i, i2, expandableNotificationRow) { // from class: com.android.systemui.statusbar.BaseStatusBar.13.1
                    final AnonymousClass13 this$1;
                    final NotificationGuts val$guts;
                    final ExpandableNotificationRow val$row;
                    final int val$x;
                    final int val$y;

                    {
                        this.this$1 = this;
                        this.val$guts = guts;
                        this.val$x = i;
                        this.val$y = i2;
                        this.val$row = expandableNotificationRow;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        boolean z = false;
                        this.this$1.this$0.dismissPopups(-1, -1, false, false);
                        this.val$guts.setVisibility(0);
                        Animator createCircularReveal = ViewAnimationUtils.createCircularReveal(this.val$guts, this.val$x, this.val$y, 0.0f, (float) Math.hypot(Math.max(this.val$guts.getWidth() - this.val$x, this.val$x), Math.max(this.val$guts.getHeight() - this.val$y, this.val$y)));
                        createCircularReveal.setDuration(360L);
                        createCircularReveal.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
                        createCircularReveal.addListener(new AnimatorListenerAdapter(this, this.val$row) { // from class: com.android.systemui.statusbar.BaseStatusBar.13.1.1
                            final AnonymousClass1 this$2;
                            final ExpandableNotificationRow val$row;

                            {
                                this.this$2 = this;
                                this.val$row = r5;
                            }

                            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                            public void onAnimationEnd(Animator animator) {
                                super.onAnimationEnd(animator);
                                this.val$row.resetTranslation();
                            }
                        });
                        createCircularReveal.start();
                        NotificationGuts notificationGuts = this.val$guts;
                        if (this.this$1.this$0.mState == 1) {
                            z = true;
                        }
                        notificationGuts.setExposed(true, z);
                        this.val$row.closeRemoteInput();
                        this.this$1.this$0.mStackScroller.onHeightChanged(null, true);
                        this.this$1.this$0.mNotificationGutsExposed = this.val$guts;
                    }
                });
                return true;
            }
            return false;
        }
    }

    /* renamed from: com.android.systemui.statusbar.BaseStatusBar$14  reason: invalid class name */
    /* loaded from: a.zip:com/android/systemui/statusbar/BaseStatusBar$14.class */
    class AnonymousClass14 implements KeyguardHostView.OnDismissAction {
        final BaseStatusBar this$0;
        final boolean val$afterKeyguardGone;
        final PendingIntent val$intent;
        final boolean val$keyguardShowing;

        AnonymousClass14(BaseStatusBar baseStatusBar, boolean z, boolean z2, PendingIntent pendingIntent) {
            this.this$0 = baseStatusBar;
            this.val$keyguardShowing = z;
            this.val$afterKeyguardGone = z2;
            this.val$intent = pendingIntent;
        }

        /* JADX WARN: Type inference failed for: r0v0, types: [com.android.systemui.statusbar.BaseStatusBar$14$1] */
        @Override // com.android.keyguard.KeyguardHostView.OnDismissAction
        public boolean onDismiss() {
            new Thread(this, this.val$keyguardShowing, this.val$afterKeyguardGone, this.val$intent) { // from class: com.android.systemui.statusbar.BaseStatusBar.14.1
                final AnonymousClass14 this$1;
                final boolean val$afterKeyguardGone;
                final PendingIntent val$intent;
                final boolean val$keyguardShowing;

                {
                    this.this$1 = this;
                    this.val$keyguardShowing = r5;
                    this.val$afterKeyguardGone = r6;
                    this.val$intent = r7;
                }

                @Override // java.lang.Thread, java.lang.Runnable
                public void run() {
                    try {
                        if (this.val$keyguardShowing && !this.val$afterKeyguardGone) {
                            ActivityManagerNative.getDefault().keyguardWaitingForActivityDrawn();
                        }
                        ActivityManagerNative.getDefault().resumeAppSwitches();
                    } catch (RemoteException e) {
                    }
                    try {
                        this.val$intent.send(null, 0, null, null, null, null, this.this$1.this$0.getActivityOptions());
                    } catch (PendingIntent.CanceledException e2) {
                        Log.w("StatusBar", "Sending intent failed: " + e2);
                    }
                    if (this.val$intent.isActivity()) {
                        this.this$1.this$0.mAssistManager.hideAssist();
                        this.this$1.this$0.overrideActivityPendingAppTransition(this.val$keyguardShowing ? !this.val$afterKeyguardGone : false);
                    }
                }
            }.start();
            this.this$0.animateCollapsePanels(2, true, true);
            this.this$0.visibilityChanged(false);
            return true;
        }
    }

    /* renamed from: com.android.systemui.statusbar.BaseStatusBar$4  reason: invalid class name */
    /* loaded from: a.zip:com/android/systemui/statusbar/BaseStatusBar$4.class */
    class AnonymousClass4 extends RemoteViews.OnClickHandler {
        final BaseStatusBar this$0;

        AnonymousClass4(BaseStatusBar baseStatusBar) {
            this.this$0 = baseStatusBar;
        }

        private String getNotificationKeyForParent(ViewParent viewParent) {
            while (viewParent != null) {
                if (viewParent instanceof ExpandableNotificationRow) {
                    return ((ExpandableNotificationRow) viewParent).getStatusBarNotification().getKey();
                }
                viewParent = viewParent.getParent();
            }
            return null;
        }

        private boolean handleRemoteInput(View view, PendingIntent pendingIntent, Intent intent) {
            RemoteInputView remoteInputView;
            ExpandableNotificationRow expandableNotificationRow;
            Object tag = view.getTag(16908380);
            RemoteInput[] remoteInputArr = tag instanceof RemoteInput[] ? (RemoteInput[]) tag : null;
            if (remoteInputArr == null) {
                return false;
            }
            RemoteInput remoteInput = null;
            for (RemoteInput remoteInput2 : remoteInputArr) {
                if (remoteInput2.getAllowFreeFormInput()) {
                    remoteInput = remoteInput2;
                }
            }
            if (remoteInput == null) {
                return false;
            }
            ViewParent parent = view.getParent();
            while (true) {
                remoteInputView = null;
                if (parent == null) {
                    break;
                }
                if (parent instanceof View) {
                    View view2 = (View) parent;
                    if (view2.isRootNamespace()) {
                        remoteInputView = (RemoteInputView) view2.findViewWithTag(RemoteInputView.VIEW_TAG);
                        break;
                    }
                }
                parent = parent.getParent();
            }
            while (true) {
                expandableNotificationRow = null;
                if (parent == null) {
                    break;
                } else if (parent instanceof ExpandableNotificationRow) {
                    expandableNotificationRow = (ExpandableNotificationRow) parent;
                    break;
                } else {
                    parent = parent.getParent();
                }
            }
            if (remoteInputView == null || expandableNotificationRow == null) {
                return false;
            }
            expandableNotificationRow.setUserExpanded(true);
            if (!this.this$0.mAllowLockscreenRemoteInput) {
                if (this.this$0.isLockscreenPublicMode()) {
                    this.this$0.onLockedRemoteInput(expandableNotificationRow, view);
                    return true;
                }
                int identifier = pendingIntent.getCreatorUserHandle().getIdentifier();
                if (this.this$0.mUserManager.getUserInfo(identifier).isManagedProfile() && this.this$0.mKeyguardManager.isDeviceLocked(identifier)) {
                    this.this$0.onLockedWorkRemoteInput(identifier, expandableNotificationRow, view);
                    return true;
                }
            }
            remoteInputView.setVisibility(0);
            int left = view.getLeft() + (view.getWidth() / 2);
            int top = view.getTop() + (view.getHeight() / 2);
            int width = remoteInputView.getWidth();
            int height = remoteInputView.getHeight();
            ViewAnimationUtils.createCircularReveal(remoteInputView, left, top, 0.0f, Math.max(Math.max(left + top, (height - top) + left), Math.max((width - left) + top, (width - left) + (height - top)))).start();
            remoteInputView.setPendingIntent(pendingIntent);
            remoteInputView.setRemoteInput(remoteInputArr, remoteInput);
            remoteInputView.focus();
            return true;
        }

        private void logActionClick(View view) {
            ViewParent parent = view.getParent();
            String notificationKeyForParent = getNotificationKeyForParent(parent);
            if (notificationKeyForParent == null) {
                Log.w("StatusBar", "Couldn't determine notification for click.");
                return;
            }
            int i = -1;
            if (view.getId() == 16909215) {
                i = -1;
                if (parent != null) {
                    i = -1;
                    if (parent instanceof ViewGroup) {
                        i = ((ViewGroup) parent).indexOfChild(view);
                    }
                }
            }
            try {
                this.this$0.mBarService.onNotificationActionClick(notificationKeyForParent, i);
            } catch (RemoteException e) {
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public boolean superOnClickHandler(View view, PendingIntent pendingIntent, Intent intent) {
            return super.onClickHandler(view, pendingIntent, intent, 1);
        }

        public boolean onClickHandler(View view, PendingIntent pendingIntent, Intent intent) {
            if (handleRemoteInput(view, pendingIntent, intent)) {
                return true;
            }
            logActionClick(view);
            try {
                ActivityManagerNative.getDefault().resumeAppSwitches();
            } catch (RemoteException e) {
            }
            if (pendingIntent.isActivity()) {
                boolean isShowing = this.this$0.mStatusBarKeyguardViewManager.isShowing();
                boolean wouldLaunchResolverActivity = PreviewInflater.wouldLaunchResolverActivity(this.this$0.mContext, pendingIntent.getIntent(), this.this$0.mCurrentUserId);
                this.this$0.dismissKeyguardThenExecute(new KeyguardHostView.OnDismissAction(this, isShowing, wouldLaunchResolverActivity, view, pendingIntent, intent) { // from class: com.android.systemui.statusbar.BaseStatusBar.4.1
                    final AnonymousClass4 this$1;
                    final boolean val$afterKeyguardGone;
                    final Intent val$fillInIntent;
                    final boolean val$keyguardShowing;
                    final PendingIntent val$pendingIntent;
                    final View val$view;

                    {
                        this.this$1 = this;
                        this.val$keyguardShowing = isShowing;
                        this.val$afterKeyguardGone = wouldLaunchResolverActivity;
                        this.val$view = view;
                        this.val$pendingIntent = pendingIntent;
                        this.val$fillInIntent = intent;
                    }

                    @Override // com.android.keyguard.KeyguardHostView.OnDismissAction
                    public boolean onDismiss() {
                        if (this.val$keyguardShowing && !this.val$afterKeyguardGone) {
                            try {
                                ActivityManagerNative.getDefault().keyguardWaitingForActivityDrawn();
                                ActivityManagerNative.getDefault().resumeAppSwitches();
                            } catch (RemoteException e2) {
                            }
                        }
                        boolean superOnClickHandler = this.this$1.superOnClickHandler(this.val$view, this.val$pendingIntent, this.val$fillInIntent);
                        this.this$1.this$0.overrideActivityPendingAppTransition(this.val$keyguardShowing && !this.val$afterKeyguardGone);
                        if (superOnClickHandler) {
                            this.this$1.this$0.animateCollapsePanels(2, true);
                            this.this$1.this$0.visibilityChanged(false);
                            this.this$1.this$0.mAssistManager.hideAssist();
                        }
                        return superOnClickHandler;
                    }
                }, wouldLaunchResolverActivity);
                return true;
            }
            return superOnClickHandler(view, pendingIntent, intent);
        }
    }

    /* renamed from: com.android.systemui.statusbar.BaseStatusBar$7  reason: invalid class name */
    /* loaded from: a.zip:com/android/systemui/statusbar/BaseStatusBar$7.class */
    class AnonymousClass7 extends NotificationListenerService {
        final BaseStatusBar this$0;

        AnonymousClass7(BaseStatusBar baseStatusBar) {
            this.this$0 = baseStatusBar;
        }

        @Override // android.service.notification.NotificationListenerService
        public void onListenerConnected() {
            this.this$0.mHandler.post(new Runnable(this, getActiveNotifications(), getCurrentRanking()) { // from class: com.android.systemui.statusbar.BaseStatusBar.7.1
                final AnonymousClass7 this$1;
                final NotificationListenerService.RankingMap val$currentRanking;
                final StatusBarNotification[] val$notifications;

                {
                    this.this$1 = this;
                    this.val$notifications = r5;
                    this.val$currentRanking = r6;
                }

                @Override // java.lang.Runnable
                public void run() {
                    for (StatusBarNotification statusBarNotification : this.val$notifications) {
                        this.this$1.this$0.addNotification(statusBarNotification, this.val$currentRanking, null);
                    }
                }
            });
        }

        @Override // android.service.notification.NotificationListenerService
        public void onNotificationPosted(StatusBarNotification statusBarNotification, NotificationListenerService.RankingMap rankingMap) {
            Log.d("StatusBar", "onNotificationPosted: " + statusBarNotification);
            if (statusBarNotification != null) {
                this.this$0.mHandler.post(new Runnable(this, statusBarNotification, rankingMap) { // from class: com.android.systemui.statusbar.BaseStatusBar.7.2
                    final AnonymousClass7 this$1;
                    final NotificationListenerService.RankingMap val$rankingMap;
                    final StatusBarNotification val$sbn;

                    {
                        this.this$1 = this;
                        this.val$sbn = statusBarNotification;
                        this.val$rankingMap = rankingMap;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.this$1.this$0.processForRemoteInput(this.val$sbn.getNotification());
                        String key = this.val$sbn.getKey();
                        this.this$1.this$0.mKeysKeptForRemoteInput.remove(key);
                        boolean z = this.this$1.this$0.mNotificationData.get(key) != null;
                        if (BaseStatusBar.ENABLE_CHILD_NOTIFICATIONS || !this.this$1.this$0.mGroupManager.isChildInGroupWithSummary(this.val$sbn)) {
                            if (z) {
                                this.this$1.this$0.updateNotification(this.val$sbn, this.val$rankingMap);
                            } else {
                                this.this$1.this$0.addNotification(this.val$sbn, this.val$rankingMap, null);
                            }
                        } else if (z) {
                            this.this$1.this$0.removeNotification(key, this.val$rankingMap);
                        } else {
                            this.this$1.this$0.mNotificationData.updateRanking(this.val$rankingMap);
                        }
                    }
                });
            }
        }

        @Override // android.service.notification.NotificationListenerService
        public void onNotificationRankingUpdate(NotificationListenerService.RankingMap rankingMap) {
            if (rankingMap != null) {
                this.this$0.mHandler.post(new Runnable(this, rankingMap) { // from class: com.android.systemui.statusbar.BaseStatusBar.7.4
                    final AnonymousClass7 this$1;
                    final NotificationListenerService.RankingMap val$rankingMap;

                    {
                        this.this$1 = this;
                        this.val$rankingMap = rankingMap;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.this$1.this$0.updateNotificationRanking(this.val$rankingMap);
                    }
                });
            }
        }

        @Override // android.service.notification.NotificationListenerService
        public void onNotificationRemoved(StatusBarNotification statusBarNotification, NotificationListenerService.RankingMap rankingMap) {
            Log.d("StatusBar", "onNotificationRemoved: " + statusBarNotification);
            if (statusBarNotification != null) {
                this.this$0.mHandler.post(new Runnable(this, statusBarNotification.getKey(), rankingMap) { // from class: com.android.systemui.statusbar.BaseStatusBar.7.3
                    final AnonymousClass7 this$1;
                    final String val$key;
                    final NotificationListenerService.RankingMap val$rankingMap;

                    {
                        this.this$1 = this;
                        this.val$key = r5;
                        this.val$rankingMap = rankingMap;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.this$1.this$0.removeNotification(this.val$key, this.val$rankingMap);
                    }
                });
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* loaded from: a.zip:com/android/systemui/statusbar/BaseStatusBar$H.class */
    public class H extends Handler {
        final BaseStatusBar this$0;

        /* JADX INFO: Access modifiers changed from: protected */
        public H(BaseStatusBar baseStatusBar) {
            this.this$0 = baseStatusBar;
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            boolean z = true;
            switch (message.what) {
                case 1019:
                    BaseStatusBar baseStatusBar = this.this$0;
                    boolean z2 = message.arg1 > 0;
                    if (message.arg2 == 0) {
                        z = false;
                    }
                    baseStatusBar.showRecents(z2, z);
                    return;
                case 1020:
                    this.this$0.hideRecents(message.arg1 > 0, message.arg2 > 0);
                    return;
                case 1021:
                    this.this$0.toggleRecents();
                    return;
                case 1022:
                    this.this$0.preloadRecents();
                    return;
                case 1023:
                    this.this$0.cancelPreloadingRecents();
                    return;
                case 1024:
                    this.this$0.showRecentsNextAffiliatedTask();
                    return;
                case 1025:
                    this.this$0.showRecentsPreviousAffiliatedTask();
                    return;
                case 1026:
                    this.this$0.toggleKeyboardShortcuts(message.arg1);
                    return;
                case 1027:
                    this.this$0.dismissKeyboardShortcuts();
                    return;
                default:
                    return;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/statusbar/BaseStatusBar$NotificationClicker.class */
    public final class NotificationClicker implements View.OnClickListener {
        final BaseStatusBar this$0;

        /* renamed from: com.android.systemui.statusbar.BaseStatusBar$NotificationClicker$2  reason: invalid class name */
        /* loaded from: a.zip:com/android/systemui/statusbar/BaseStatusBar$NotificationClicker$2.class */
        class AnonymousClass2 implements KeyguardHostView.OnDismissAction {
            final NotificationClicker this$1;
            final boolean val$afterKeyguardGone;
            final PendingIntent val$intent;
            final boolean val$keyguardShowing;
            final String val$notificationKey;
            final ExpandableNotificationRow val$row;
            final StatusBarNotification val$sbn;

            /* renamed from: com.android.systemui.statusbar.BaseStatusBar$NotificationClicker$2$1  reason: invalid class name */
            /* loaded from: a.zip:com/android/systemui/statusbar/BaseStatusBar$NotificationClicker$2$1.class */
            class AnonymousClass1 extends Thread {
                final AnonymousClass2 this$2;
                final boolean val$afterKeyguardGone;
                final PendingIntent val$intent;
                final boolean val$keyguardShowing;
                final String val$notificationKey;
                final StatusBarNotification val$parentToCancelFinal;

                /* renamed from: com.android.systemui.statusbar.BaseStatusBar$NotificationClicker$2$1$1  reason: invalid class name and collision with other inner class name */
                /* loaded from: a.zip:com/android/systemui/statusbar/BaseStatusBar$NotificationClicker$2$1$1.class */
                class RunnableC00071 implements Runnable {
                    final AnonymousClass1 this$3;
                    final StatusBarNotification val$parentToCancelFinal;

                    RunnableC00071(AnonymousClass1 anonymousClass1, StatusBarNotification statusBarNotification) {
                        this.this$3 = anonymousClass1;
                        this.val$parentToCancelFinal = statusBarNotification;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        Runnable runnable = new Runnable(this, this.val$parentToCancelFinal) { // from class: com.android.systemui.statusbar.BaseStatusBar.NotificationClicker.2.1.1.1
                            final RunnableC00071 this$4;
                            final StatusBarNotification val$parentToCancelFinal;

                            {
                                this.this$4 = this;
                                this.val$parentToCancelFinal = r5;
                            }

                            @Override // java.lang.Runnable
                            public void run() {
                                this.this$4.this$3.this$2.this$1.this$0.performRemoveNotification(this.val$parentToCancelFinal, true);
                            }
                        };
                        if (this.this$3.this$2.this$1.this$0.isCollapsing()) {
                            this.this$3.this$2.this$1.this$0.addPostCollapseAction(runnable);
                        } else {
                            runnable.run();
                        }
                    }
                }

                AnonymousClass1(AnonymousClass2 anonymousClass2, boolean z, boolean z2, PendingIntent pendingIntent, String str, StatusBarNotification statusBarNotification) {
                    this.this$2 = anonymousClass2;
                    this.val$keyguardShowing = z;
                    this.val$afterKeyguardGone = z2;
                    this.val$intent = pendingIntent;
                    this.val$notificationKey = str;
                    this.val$parentToCancelFinal = statusBarNotification;
                }

                @Override // java.lang.Thread, java.lang.Runnable
                public void run() {
                    try {
                        if (this.val$keyguardShowing && !this.val$afterKeyguardGone) {
                            ActivityManagerNative.getDefault().keyguardWaitingForActivityDrawn();
                        }
                        ActivityManagerNative.getDefault().resumeAppSwitches();
                    } catch (RemoteException e) {
                    }
                    if (this.val$intent != null) {
                        if (this.val$intent.isActivity()) {
                            int identifier = this.val$intent.getCreatorUserHandle().getIdentifier();
                            if (this.this$2.this$1.this$0.mLockPatternUtils.isSeparateProfileChallengeEnabled(identifier) && this.this$2.this$1.this$0.mKeyguardManager.isDeviceLocked(identifier) && this.this$2.this$1.this$0.startWorkChallengeIfNecessary(identifier, this.val$intent.getIntentSender(), this.val$notificationKey)) {
                                return;
                            }
                        }
                        try {
                            this.val$intent.send(null, 0, null, null, null, null, this.this$2.this$1.this$0.getActivityOptions());
                        } catch (PendingIntent.CanceledException e2) {
                            Log.w("StatusBar", "Sending contentIntent failed: " + e2);
                        }
                        if (this.val$intent.isActivity()) {
                            this.this$2.this$1.this$0.mAssistManager.hideAssist();
                            this.this$2.this$1.this$0.overrideActivityPendingAppTransition(this.val$keyguardShowing ? !this.val$afterKeyguardGone : false);
                        }
                    }
                    try {
                        this.this$2.this$1.this$0.mBarService.onNotificationClick(this.val$notificationKey);
                    } catch (RemoteException e3) {
                    }
                    if (this.val$parentToCancelFinal != null) {
                        this.this$2.this$1.this$0.mHandler.post(new RunnableC00071(this, this.val$parentToCancelFinal));
                    }
                }
            }

            AnonymousClass2(NotificationClicker notificationClicker, String str, ExpandableNotificationRow expandableNotificationRow, StatusBarNotification statusBarNotification, boolean z, boolean z2, PendingIntent pendingIntent) {
                this.this$1 = notificationClicker;
                this.val$notificationKey = str;
                this.val$row = expandableNotificationRow;
                this.val$sbn = statusBarNotification;
                this.val$keyguardShowing = z;
                this.val$afterKeyguardGone = z2;
                this.val$intent = pendingIntent;
            }

            @Override // com.android.keyguard.KeyguardHostView.OnDismissAction
            public boolean onDismiss() {
                if (this.this$1.this$0.mHeadsUpManager != null && this.this$1.this$0.mHeadsUpManager.isHeadsUp(this.val$notificationKey)) {
                    if (this.this$1.this$0.isPanelFullyCollapsed()) {
                        HeadsUpManager.setIsClickedNotification(this.val$row, true);
                    }
                    this.this$1.this$0.mHeadsUpManager.releaseImmediately(this.val$notificationKey);
                }
                StatusBarNotification statusBarNotification = null;
                if (this.this$1.shouldAutoCancel(this.val$sbn)) {
                    statusBarNotification = null;
                    if (this.this$1.this$0.mGroupManager.isOnlyChildInGroup(this.val$sbn)) {
                        StatusBarNotification statusBarNotification2 = this.this$1.this$0.mGroupManager.getLogicalGroupSummary(this.val$sbn).getStatusBarNotification();
                        statusBarNotification = null;
                        if (this.this$1.shouldAutoCancel(statusBarNotification2)) {
                            statusBarNotification = statusBarNotification2;
                        }
                    }
                }
                new AnonymousClass1(this, this.val$keyguardShowing, this.val$afterKeyguardGone, this.val$intent, this.val$notificationKey, statusBarNotification).start();
                this.this$1.this$0.animateCollapsePanels(2, true, true);
                this.this$1.this$0.visibilityChanged(false);
                return true;
            }
        }

        private NotificationClicker(BaseStatusBar baseStatusBar) {
            this.this$0 = baseStatusBar;
        }

        /* synthetic */ NotificationClicker(BaseStatusBar baseStatusBar, NotificationClicker notificationClicker) {
            this(baseStatusBar);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public boolean shouldAutoCancel(StatusBarNotification statusBarNotification) {
            int i = statusBarNotification.getNotification().flags;
            return (i & 16) == 16 && (i & 64) == 0;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            if (!(view instanceof ExpandableNotificationRow)) {
                Log.e("StatusBar", "NotificationClicker called on a view that is not a notification row.");
                return;
            }
            ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
            StatusBarNotification statusBarNotification = expandableNotificationRow.getStatusBarNotification();
            if (statusBarNotification == null) {
                Log.e("StatusBar", "NotificationClicker called on an unclickable notification,");
            } else if (expandableNotificationRow.getSettingsRow() != null && expandableNotificationRow.getSettingsRow().isVisible()) {
                expandableNotificationRow.animateTranslateNotification(0.0f);
            } else {
                Notification notification = statusBarNotification.getNotification();
                PendingIntent pendingIntent = notification.contentIntent != null ? notification.contentIntent : notification.fullScreenIntent;
                String key = statusBarNotification.getKey();
                expandableNotificationRow.setJustClicked(true);
                DejankUtils.postAfterTraversal(new Runnable(this, expandableNotificationRow) { // from class: com.android.systemui.statusbar.BaseStatusBar.NotificationClicker.1
                    final NotificationClicker this$1;
                    final ExpandableNotificationRow val$row;

                    {
                        this.this$1 = this;
                        this.val$row = expandableNotificationRow;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.val$row.setJustClicked(false);
                    }
                });
                boolean isShowing = this.this$0.mStatusBarKeyguardViewManager.isShowing();
                boolean wouldLaunchResolverActivity = pendingIntent.isActivity() ? PreviewInflater.wouldLaunchResolverActivity(this.this$0.mContext, pendingIntent.getIntent(), this.this$0.mCurrentUserId) : false;
                this.this$0.dismissKeyguardThenExecute(new AnonymousClass2(this, key, expandableNotificationRow, statusBarNotification, isShowing, wouldLaunchResolverActivity, pendingIntent), wouldLaunchResolverActivity);
            }
        }

        public void register(ExpandableNotificationRow expandableNotificationRow, StatusBarNotification statusBarNotification) {
            Notification notification = statusBarNotification.getNotification();
            if (notification.contentIntent == null && notification.fullScreenIntent == null) {
                expandableNotificationRow.setOnClickListener(null);
            } else {
                expandableNotificationRow.setOnClickListener(this);
            }
        }
    }

    private boolean adminAllowsUnredactedNotifications(int i) {
        boolean z = true;
        if (i == -1) {
            return true;
        }
        if ((this.mDevicePolicyManager.getKeyguardDisabledFeatures(null, i) & 8) != 0) {
            z = false;
        }
        return z;
    }

    private boolean alertAgain(NotificationData.Entry entry, Notification notification) {
        boolean z = true;
        if (entry != null) {
            z = true;
            if (entry.hasInterrupted()) {
                z = (notification.flags & 8) == 0;
            }
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void bindGuts(ExpandableNotificationRow expandableNotificationRow) {
        int i;
        expandableNotificationRow.inflateGuts();
        StatusBarNotification statusBarNotification = expandableNotificationRow.getStatusBarNotification();
        PackageManager packageManagerForUser = getPackageManagerForUser(this.mContext, statusBarNotification.getUser().getIdentifier());
        expandableNotificationRow.setTag(statusBarNotification.getPackageName());
        NotificationGuts guts = expandableNotificationRow.getGuts();
        guts.setClosedListener(this);
        String packageName = statusBarNotification.getPackageName();
        Drawable drawable = null;
        String str = packageName;
        try {
            ApplicationInfo applicationInfo = packageManagerForUser.getApplicationInfo(packageName, 8704);
            i = -1;
            str = packageName;
            if (applicationInfo != null) {
                String valueOf = String.valueOf(packageManagerForUser.getApplicationLabel(applicationInfo));
                drawable = packageManagerForUser.getApplicationIcon(applicationInfo);
                str = valueOf;
                i = applicationInfo.uid;
                str = valueOf;
            }
        } catch (PackageManager.NameNotFoundException e) {
            drawable = packageManagerForUser.getDefaultActivityIcon();
            i = -1;
        }
        ((ImageView) guts.findViewById(2131886545)).setImageDrawable(drawable);
        ((TextView) guts.findViewById(2131886546)).setText(str);
        TextView textView = (TextView) guts.findViewById(2131886555);
        if (i >= 0) {
            textView.setOnClickListener(new View.OnClickListener(this, guts, packageName, i) { // from class: com.android.systemui.statusbar.BaseStatusBar.11
                final BaseStatusBar this$0;
                final int val$appUidF;
                final NotificationGuts val$guts;
                final String val$pkg;

                {
                    this.this$0 = this;
                    this.val$guts = guts;
                    this.val$pkg = packageName;
                    this.val$appUidF = i;
                }

                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    MetricsLogger.action(this.this$0.mContext, 205);
                    this.val$guts.resetFalsingCheck();
                    this.this$0.startAppNotificationSettingsActivity(this.val$pkg, this.val$appUidF);
                }
            });
            textView.setText(2131493776);
        } else {
            textView.setVisibility(8);
        }
        guts.bindImportance(packageManagerForUser, statusBarNotification, this.mNotificationData.getImportance(statusBarNotification.getKey()));
        TextView textView2 = (TextView) guts.findViewById(2131886556);
        textView2.setText(2131493777);
        textView2.setOnClickListener(new AnonymousClass12(this, guts, statusBarNotification, expandableNotificationRow));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dismissPopups(int i, int i2) {
        dismissPopups(i, i2, true, false);
    }

    public static PackageManager getPackageManagerForUser(Context context, int i) {
        Context context2 = context;
        if (i >= 0) {
            try {
                context2 = context.createPackageContextAsUser(context.getPackageName(), 4, new UserHandle(i));
            } catch (PackageManager.NameNotFoundException e) {
                context2 = context;
            }
        }
        return context2.getPackageManager();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void processForRemoteInput(Notification notification) {
        Notification.Action action;
        Notification.Action action2;
        if (ENABLE_REMOTE_INPUT && notification.extras != null && notification.extras.containsKey("android.wearable.EXTENSIONS")) {
            if (notification.actions == null || notification.actions.length == 0) {
                Notification.Action action3 = null;
                List<Notification.Action> actions = new Notification.WearableExtender(notification).getActions();
                int size = actions.size();
                int i = 0;
                while (true) {
                    action = action3;
                    if (i >= size) {
                        break;
                    }
                    Notification.Action action4 = actions.get(i);
                    if (action4 == null) {
                        action2 = action3;
                    } else {
                        RemoteInput[] remoteInputs = action4.getRemoteInputs();
                        action2 = action3;
                        if (remoteInputs != null) {
                            int length = remoteInputs.length;
                            int i2 = 0;
                            while (true) {
                                action = action3;
                                if (i2 >= length) {
                                    break;
                                } else if (remoteInputs[i2].getAllowFreeFormInput()) {
                                    action = action4;
                                    break;
                                } else {
                                    i2++;
                                }
                            }
                            action2 = action;
                            if (action != null) {
                                break;
                            }
                        } else {
                            continue;
                        }
                    }
                    i++;
                    action3 = action2;
                }
                if (action != null) {
                    Notification.Builder recoverBuilder = Notification.Builder.recoverBuilder(this.mContext, notification);
                    recoverBuilder.setActions(action);
                    recoverBuilder.build();
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void saveImportanceCloseControls(StatusBarNotification statusBarNotification, ExpandableNotificationRow expandableNotificationRow, NotificationGuts notificationGuts, View view) {
        notificationGuts.resetFalsingCheck();
        notificationGuts.saveImportance(statusBarNotification);
        int[] iArr = new int[2];
        int[] iArr2 = new int[2];
        expandableNotificationRow.getLocationOnScreen(iArr);
        view.getLocationOnScreen(iArr2);
        dismissPopups((iArr2[0] - iArr[0]) + (view.getWidth() / 2), (iArr2[1] - iArr[1]) + (view.getHeight() / 2));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startAppNotificationSettingsActivity(String str, int i) {
        if (BenesseExtension.getDchaState() != 0) {
            return;
        }
        Intent intent = new Intent("android.settings.APP_NOTIFICATION_SETTINGS");
        intent.putExtra("app_package", str);
        intent.putExtra("app_uid", i);
        startNotificationGutsIntent(intent, i);
    }

    private void startNotificationGutsIntent(Intent intent, int i) {
        dismissKeyguardThenExecute(new AnonymousClass10(this, this.mStatusBarKeyguardViewManager.isShowing(), intent, i), false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateCurrentProfilesCache() {
        synchronized (this.mCurrentProfiles) {
            this.mCurrentProfiles.clear();
            if (this.mUserManager != null) {
                for (UserInfo userInfo : this.mUserManager.getProfiles(this.mCurrentUserId)) {
                    this.mCurrentProfiles.put(userInfo.id, userInfo);
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateLockscreenNotificationSetting() {
        boolean z = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "lock_screen_show_notifications", 1, this.mCurrentUserId) != 0;
        this.mUsersAllowingNotifications.put(this.mCurrentUserId, z);
        int keyguardDisabledFeatures = this.mDevicePolicyManager.getKeyguardDisabledFeatures(null, this.mCurrentUserId);
        boolean z2 = (keyguardDisabledFeatures & 4) == 0;
        if (!z) {
            z2 = false;
        }
        setShowLockscreenNotifications(z2);
        if (!ENABLE_LOCK_SCREEN_ALLOW_REMOTE_INPUT) {
            setLockScreenAllowRemoteInput(false);
            return;
        }
        boolean z3 = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "lock_screen_allow_remote_input", 0, this.mCurrentUserId) != 0;
        boolean z4 = (keyguardDisabledFeatures & 64) == 0;
        if (!z3) {
            z4 = false;
        }
        setLockScreenAllowRemoteInput(z4);
    }

    private void updateNotificationViews(NotificationData.Entry entry, StatusBarNotification statusBarNotification) {
        RemoteViews remoteViews = entry.cachedContentView;
        RemoteViews remoteViews2 = entry.cachedBigContentView;
        RemoteViews remoteViews3 = entry.cachedHeadsUpContentView;
        RemoteViews remoteViews4 = entry.cachedPublicContentView;
        remoteViews.reapply(this.mContext, entry.getContentView(), this.mOnClickHandler);
        if (remoteViews2 != null && entry.getExpandedContentView() != null) {
            remoteViews2.reapply(statusBarNotification.getPackageContext(this.mContext), entry.getExpandedContentView(), this.mOnClickHandler);
        }
        View headsUpContentView = entry.getHeadsUpContentView();
        if (remoteViews3 != null && headsUpContentView != null) {
            remoteViews3.reapply(statusBarNotification.getPackageContext(this.mContext), headsUpContentView, this.mOnClickHandler);
        }
        if (remoteViews4 != null && entry.getPublicContentView() != null) {
            remoteViews4.reapply(statusBarNotification.getPackageContext(this.mContext), entry.getPublicContentView(), this.mOnClickHandler);
        }
        this.mNotificationClicker.register(entry.row, statusBarNotification);
        entry.row.onNotificationUpdated(entry);
        entry.row.resetHeight();
    }

    public abstract void addNotification(StatusBarNotification statusBarNotification, NotificationListenerService.RankingMap rankingMap, NotificationData.Entry entry);

    /* JADX INFO: Access modifiers changed from: protected */
    public void addNotificationViews(NotificationData.Entry entry, NotificationListenerService.RankingMap rankingMap) {
        if (entry == null) {
            return;
        }
        this.mNotificationData.add(entry, rankingMap);
        updateNotifications();
    }

    public void addPostCollapseAction(Runnable runnable) {
    }

    public void animateCollapsePanels(int i, boolean z) {
    }

    public void animateCollapsePanels(int i, boolean z, boolean z2) {
    }

    protected void applyColorsAndBackgrounds(StatusBarNotification statusBarNotification, NotificationData.Entry entry) {
        boolean z = true;
        if (entry.getContentView().getId() != 16909232 && entry.targetSdk >= 9 && entry.targetSdk < 21) {
            entry.row.setShowingLegacyBackground(true);
            entry.legacy = true;
        }
        if (entry.icon != null) {
            StatusBarIconView statusBarIconView = entry.icon;
            if (entry.targetSdk >= 21) {
                z = false;
            }
            statusBarIconView.setTag(2131886141, Boolean.valueOf(z));
        }
    }

    protected void bindDismissListener(ExpandableNotificationRow expandableNotificationRow) {
        expandableNotificationRow.setOnDismissListener(new View.OnClickListener(this, expandableNotificationRow) { // from class: com.android.systemui.statusbar.BaseStatusBar.9
            final BaseStatusBar this$0;
            final ExpandableNotificationRow val$row;

            {
                this.this$0 = this;
                this.val$row = expandableNotificationRow;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                view.announceForAccessibility(this.this$0.mContext.getString(2131493442));
                this.this$0.performRemoveNotification(this.val$row.getStatusBarNotification(), false);
            }
        });
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void cancelPreloadRecentApps() {
        this.mHandler.removeMessages(1023);
        this.mHandler.sendEmptyMessage(1023);
    }

    protected void cancelPreloadingRecents() {
        if (this.mRecents != null) {
            this.mRecents.cancelPreloadingRecents();
        }
    }

    public void clearNotificationEffects() {
        try {
            this.mBarService.clearNotificationEffects();
        } catch (RemoteException e) {
        }
    }

    protected abstract void createAndAddWindows();

    protected H createHandler() {
        return new H(this);
    }

    public StatusBarIconView createIcon(StatusBarNotification statusBarNotification) {
        Notification notification = statusBarNotification.getNotification();
        StatusBarIconView statusBarIconView = new StatusBarIconView(this.mContext, statusBarNotification.getPackageName() + "/0x" + Integer.toHexString(statusBarNotification.getId()), notification);
        statusBarIconView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        Icon smallIcon = notification.getSmallIcon();
        if (smallIcon == null) {
            handleNotificationError(statusBarNotification, "No small icon in notification from " + statusBarNotification.getPackageName());
            return null;
        }
        StatusBarIcon statusBarIcon = new StatusBarIcon(statusBarNotification.getUser(), statusBarNotification.getPackageName(), smallIcon, notification.iconLevel, notification.number, StatusBarIconView.contentDescForNotification(this.mContext, notification));
        if (statusBarIconView.set(statusBarIcon)) {
            return statusBarIconView;
        }
        handleNotificationError(statusBarNotification, "Couldn't create icon: " + statusBarIcon);
        return null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public NotificationData.Entry createNotificationViews(StatusBarNotification statusBarNotification) {
        StatusBarIconView createIcon = createIcon(statusBarNotification);
        if (createIcon == null) {
            return null;
        }
        NotificationData.Entry entry = new NotificationData.Entry(statusBarNotification, createIcon);
        if (inflateViews(entry, this.mStackScroller)) {
            return entry;
        }
        handleNotificationError(statusBarNotification, "Couldn't expand RemoteViews for: " + statusBarNotification);
        return null;
    }

    public void destroy() {
        this.mContext.unregisterReceiver(this.mBroadcastReceiver);
        try {
            this.mNotificationListener.unregisterAsSystemService();
        } catch (RemoteException e) {
        }
    }

    protected void dismissKeyboardShortcuts() {
        KeyboardShortcuts.dismiss();
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void dismissKeyboardShortcutsMenu() {
        this.mHandler.removeMessages(1027);
        this.mHandler.sendEmptyMessage(1027);
    }

    protected void dismissKeyguardThenExecute(KeyguardHostView.OnDismissAction onDismissAction, boolean z) {
        onDismissAction.onDismiss();
    }

    public void dismissPopups() {
        dismissPopups(-1, -1, true, false);
    }

    public void dismissPopups(int i, int i2, boolean z, boolean z2) {
        if (this.mNotificationGutsExposed != null) {
            this.mNotificationGutsExposed.closeControls(i, i2, true);
        }
        if (z) {
            this.mStackScroller.resetExposedGearView(z2, true);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public Bundle getActivityOptions() {
        ActivityOptions makeBasic = ActivityOptions.makeBasic();
        makeBasic.setLaunchStackId(1);
        return makeBasic.toBundle();
    }

    @Override // com.android.systemui.statusbar.NotificationData.Environment
    public String getCurrentMediaNotificationKey() {
        return null;
    }

    public NotificationGuts getExposedGuts() {
        return this.mNotificationGutsExposed;
    }

    @Override // com.android.systemui.statusbar.NotificationData.Environment
    public NotificationGroupManager getGroupManager() {
        return this.mGroupManager;
    }

    protected abstract int getMaxKeyguardNotifications(boolean z);

    /* JADX INFO: Access modifiers changed from: protected */
    public SwipeHelper.LongPressListener getNotificationLongClicker() {
        return new AnonymousClass13(this);
    }

    void handleNotificationError(StatusBarNotification statusBarNotification, String str) {
        removeNotification(statusBarNotification.getKey(), null);
        try {
            this.mBarService.onNotificationError(statusBarNotification.getPackageName(), statusBarNotification.getTag(), statusBarNotification.getId(), statusBarNotification.getUid(), statusBarNotification.getInitialPid(), str, statusBarNotification.getUserId());
        } catch (RemoteException e) {
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void handleVisibleToUserChanged(boolean z) {
        try {
            if (!z) {
                this.mBarService.onPanelHidden();
                return;
            }
            boolean hasPinnedHeadsUp = this.mHeadsUpManager.hasPinnedHeadsUp();
            boolean z2 = !isPanelFullyCollapsed() ? this.mState == 0 || this.mState == 2 : false;
            int size = this.mNotificationData.getActiveNotifications().size();
            if (hasPinnedHeadsUp && isPanelFullyCollapsed()) {
                size = 1;
            } else {
                MetricsLogger.histogram(this.mContext, "note_load", size);
            }
            this.mBarService.onPanelRevealed(z2, size);
        } catch (RemoteException e) {
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void hideRecentApps(boolean z, boolean z2) {
        int i = 1;
        this.mHandler.removeMessages(1020);
        H h = this.mHandler;
        int i2 = z ? 1 : 0;
        if (!z2) {
            i = 0;
        }
        h.obtainMessage(1020, i2, i).sendToTarget();
    }

    protected void hideRecents(boolean z, boolean z2) {
        if (this.mRecents != null) {
            this.mRecents.hideRecents(z, z2);
        }
    }

    protected boolean inflateViews(NotificationData.Entry entry, ViewGroup viewGroup) {
        String str;
        ExpandableNotificationRow expandableNotificationRow;
        PackageManager packageManagerForUser = getPackageManagerForUser(this.mContext, entry.notification.getUser().getIdentifier());
        StatusBarNotification statusBarNotification = entry.notification;
        entry.cacheContentViews(this.mContext, null);
        RemoteViews remoteViews = entry.cachedContentView;
        RemoteViews remoteViews2 = entry.cachedBigContentView;
        RemoteViews remoteViews3 = entry.cachedHeadsUpContentView;
        RemoteViews remoteViews4 = entry.cachedPublicContentView;
        if (remoteViews == null) {
            Log.v("StatusBar", "no contentView for: " + statusBarNotification.getNotification());
            return false;
        }
        boolean z = false;
        boolean z2 = false;
        boolean z3 = false;
        if (entry.row != null) {
            ExpandableNotificationRow expandableNotificationRow2 = entry.row;
            boolean hasUserChangedExpansion = expandableNotificationRow2.hasUserChangedExpansion();
            boolean isUserExpanded = expandableNotificationRow2.isUserExpanded();
            boolean isUserLocked = expandableNotificationRow2.isUserLocked();
            entry.reset();
            z = hasUserChangedExpansion;
            expandableNotificationRow = expandableNotificationRow2;
            z2 = isUserExpanded;
            z3 = isUserLocked;
            if (hasUserChangedExpansion) {
                expandableNotificationRow2.setUserExpanded(isUserExpanded);
                z3 = isUserLocked;
                z2 = isUserExpanded;
                expandableNotificationRow = expandableNotificationRow2;
                z = hasUserChangedExpansion;
            }
        } else {
            ExpandableNotificationRow expandableNotificationRow3 = (ExpandableNotificationRow) ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(2130968816, viewGroup, false);
            expandableNotificationRow3.setExpansionLogger(this, entry.notification.getKey());
            expandableNotificationRow3.setGroupManager(this.mGroupManager);
            expandableNotificationRow3.setHeadsUpManager(this.mHeadsUpManager);
            expandableNotificationRow3.setRemoteInputController(this.mRemoteInputController);
            expandableNotificationRow3.setOnExpandClickListener(this);
            String packageName = statusBarNotification.getPackageName();
            try {
                ApplicationInfo applicationInfo = packageManagerForUser.getApplicationInfo(packageName, 8704);
                str = packageName;
                if (applicationInfo != null) {
                    str = String.valueOf(packageManagerForUser.getApplicationLabel(applicationInfo));
                }
            } catch (PackageManager.NameNotFoundException e) {
                str = packageName;
            }
            expandableNotificationRow3.setAppName(str);
            expandableNotificationRow = expandableNotificationRow3;
        }
        workAroundBadLayerDrawableOpacity(expandableNotificationRow);
        bindDismissListener(expandableNotificationRow);
        NotificationContentView privateLayout = expandableNotificationRow.getPrivateLayout();
        NotificationContentView publicLayout = expandableNotificationRow.getPublicLayout();
        expandableNotificationRow.setLayoutDirection(3);
        expandableNotificationRow.setDescendantFocusability(393216);
        if (ENABLE_REMOTE_INPUT) {
            expandableNotificationRow.setDescendantFocusability(131072);
        }
        this.mNotificationClicker.register(expandableNotificationRow, statusBarNotification);
        View view = null;
        View view2 = null;
        View view3 = null;
        try {
            View apply = remoteViews.apply(statusBarNotification.getPackageContext(this.mContext), privateLayout, this.mOnClickHandler);
            if (remoteViews2 != null) {
                view = remoteViews2.apply(statusBarNotification.getPackageContext(this.mContext), privateLayout, this.mOnClickHandler);
            }
            if (remoteViews3 != null) {
                view2 = remoteViews3.apply(statusBarNotification.getPackageContext(this.mContext), privateLayout, this.mOnClickHandler);
            }
            if (remoteViews4 != null) {
                view3 = remoteViews4.apply(statusBarNotification.getPackageContext(this.mContext), publicLayout, this.mOnClickHandler);
            }
            if (apply != null) {
                apply.setIsRootNamespace(true);
                privateLayout.setContractedChild(apply);
            }
            if (view != null) {
                view.setIsRootNamespace(true);
                privateLayout.setExpandedChild(view);
            }
            if (view2 != null) {
                view2.setIsRootNamespace(true);
                privateLayout.setHeadsUpChild(view2);
            }
            if (view3 != null) {
                view3.setIsRootNamespace(true);
                publicLayout.setContractedChild(view3);
            }
            try {
                entry.targetSdk = packageManagerForUser.getApplicationInfo(statusBarNotification.getPackageName(), 0).targetSdkVersion;
            } catch (PackageManager.NameNotFoundException e2) {
                Log.e("StatusBar", "Failed looking up ApplicationInfo for " + statusBarNotification.getPackageName(), e2);
            }
            entry.autoRedacted = entry.notification.getNotification().publicVersion == null;
            entry.row = expandableNotificationRow;
            entry.row.setOnActivatedListener(this);
            entry.row.setExpandable(view != null);
            applyColorsAndBackgrounds(statusBarNotification, entry);
            if (z) {
                expandableNotificationRow.setUserExpanded(z2);
            }
            expandableNotificationRow.setUserLocked(z3);
            expandableNotificationRow.onNotificationUpdated(entry);
            return true;
        } catch (RuntimeException e3) {
            Log.e("StatusBar", "couldn't inflate view for notification " + (statusBarNotification.getPackageName() + "/0x" + Integer.toHexString(statusBarNotification.getId())), e3);
            return false;
        }
    }

    public boolean isBouncerShowing() {
        return this.mBouncerShowing;
    }

    public boolean isCameraAllowedByAdmin() {
        boolean z = true;
        if (this.mDevicePolicyManager.getCameraDisabled(null, this.mCurrentUserId)) {
            return false;
        }
        if (isKeyguardShowing() && isKeyguardSecure()) {
            if ((this.mDevicePolicyManager.getKeyguardDisabledFeatures(null, this.mCurrentUserId) & 2) != 0) {
                z = false;
            }
            return z;
        }
        return true;
    }

    public boolean isCollapsing() {
        return false;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isCurrentProfile(int i) {
        boolean z;
        synchronized (this.mCurrentProfiles) {
            z = true;
            if (i != -1) {
                z = this.mCurrentProfiles.get(i) != null;
            }
        }
        return z;
    }

    public boolean isDeviceInVrMode() {
        return this.mVrMode;
    }

    @Override // com.android.systemui.statusbar.NotificationData.Environment
    public boolean isDeviceProvisioned() {
        if (this.mDeviceProvisioned) {
            if ("eng".equalsIgnoreCase(Build.TYPE)) {
                Log.d("StatusBar", "mDeviceProvisioned is true");
            }
            return this.mDeviceProvisioned;
        }
        Log.d("StatusBar", "mDeviceProvisioned is false, so get DEVICE_PROVISIONED from db again !!");
        boolean z = Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 0;
        if (z != this.mDeviceProvisioned) {
            Log.d("StatusBar", "mDeviceProvisioned is changed, re-call onchange!");
            this.mSettingsObserver.onChange(false);
        }
        return z;
    }

    public boolean isKeyguardSecure() {
        if (this.mStatusBarKeyguardViewManager == null) {
            Slog.w("StatusBar", "isKeyguardSecure() called before startKeyguard(), returning false", new Throwable());
            return false;
        }
        return this.mStatusBarKeyguardViewManager.isSecure();
    }

    public boolean isKeyguardShowing() {
        if (this.mStatusBarKeyguardViewManager == null) {
            Slog.i("StatusBar", "isKeyguardShowing() called before startKeyguard(), returning true");
            return true;
        }
        return this.mStatusBarKeyguardViewManager.isShowing();
    }

    public boolean isLockscreenPublicMode() {
        return this.mLockscreenPublicMode;
    }

    public boolean isMediaNotification(NotificationData.Entry entry) {
        boolean z = false;
        if (entry.getExpandedContentView() != null) {
            z = false;
            if (entry.getExpandedContentView().findViewById(16909235) != null) {
                z = true;
            }
        }
        return z;
    }

    @Override // com.android.systemui.statusbar.NotificationData.Environment
    public boolean isNotificationForCurrentProfiles(StatusBarNotification statusBarNotification) {
        int i = this.mCurrentUserId;
        return isCurrentProfile(statusBarNotification.getUserId());
    }

    public abstract boolean isPanelFullyCollapsed();

    protected abstract boolean isSnoozedPackage(StatusBarNotification statusBarNotification);

    @Override // com.android.systemui.statusbar.ExpandableNotificationRow.ExpansionLogger
    public void logNotificationExpansion(String str, boolean z, boolean z2) {
        try {
            this.mBarService.onNotificationExpansionChanged(str, z, z2);
        } catch (RemoteException e) {
        }
    }

    public abstract void maybeEscalateHeadsUp();

    /* JADX INFO: Access modifiers changed from: protected */
    public void notifyHeadsUpScreenOff() {
        maybeEscalateHeadsUp();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void notifyUserAboutHiddenNotifications() {
        if (Settings.Secure.getInt(this.mContext.getContentResolver(), "show_note_about_notification_hiding", 1) != 0) {
            Log.d("StatusBar", "user hasn't seen notification about hidden notifications");
            if (!this.mLockPatternUtils.isSecure(KeyguardUpdateMonitor.getCurrentUser())) {
                Log.d("StatusBar", "insecure lockscreen, skipping notification");
                Settings.Secure.putInt(this.mContext.getContentResolver(), "show_note_about_notification_hiding", 0);
                return;
            }
            Log.d("StatusBar", "disabling lockecreen notifications and alerting the user");
            Settings.Secure.putInt(this.mContext.getContentResolver(), "lock_screen_show_notifications", 0);
            Settings.Secure.putInt(this.mContext.getContentResolver(), "lock_screen_allow_private_notifications", 0);
            String packageName = this.mContext.getPackageName();
            PendingIntent broadcast = PendingIntent.getBroadcast(this.mContext, 0, new Intent("com.android.systemui.statusbar.banner_action_cancel").setPackage(packageName), 268435456);
            PendingIntent broadcast2 = PendingIntent.getBroadcast(this.mContext, 0, new Intent("com.android.systemui.statusbar.banner_action_setup").setPackage(packageName), 268435456);
            Notification.Builder addAction = new Notification.Builder(this.mContext).setSmallIcon(2130837622).setContentTitle(this.mContext.getString(2131493676)).setContentText(this.mContext.getString(2131493677)).setPriority(1).setOngoing(true).setColor(this.mContext.getColor(17170521)).setContentIntent(broadcast2).addAction(2130837634, this.mContext.getString(2131493678), broadcast).addAction(2130837772, this.mContext.getString(2131493679), broadcast2);
            overrideNotificationAppName(this.mContext, addAction);
            ((NotificationManager) this.mContext.getSystemService("notification")).notify(2131886134, addAction.build());
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.SystemUI
    public void onConfigurationChanged(Configuration configuration) {
        Locale locale = this.mContext.getResources().getConfiguration().locale;
        int layoutDirectionFromLocale = TextUtils.getLayoutDirectionFromLocale(locale);
        float f = configuration.fontScale;
        int i = configuration.densityDpi;
        if (i != this.mDensity || this.mFontScale != f) {
            onDensityOrFontScaleChanged();
            this.mDensity = i;
            this.mFontScale = f;
        }
        if (locale.equals(this.mLocale) && layoutDirectionFromLocale == this.mLayoutDirection) {
            return;
        }
        this.mLocale = locale;
        this.mLayoutDirection = layoutDirectionFromLocale;
        refreshLayout(layoutDirectionFromLocale);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onDensityOrFontScaleChanged() {
        ArrayList<NotificationData.Entry> activeNotifications = this.mNotificationData.getActiveNotifications();
        for (int i = 0; i < activeNotifications.size(); i++) {
            NotificationData.Entry entry = activeNotifications.get(i);
            boolean z = entry.row.getGuts() == this.mNotificationGutsExposed;
            entry.row.reInflateViews();
            if (z) {
                this.mNotificationGutsExposed = entry.row.getGuts();
                bindGuts(entry.row);
            }
            entry.cacheContentViews(this.mContext, null);
            inflateViews(entry, this.mStackScroller);
        }
    }

    @Override // com.android.systemui.statusbar.ExpandableNotificationRow.OnExpandClickListener
    public void onExpandClicked(NotificationData.Entry entry, boolean z) {
    }

    @Override // com.android.systemui.statusbar.NotificationGuts.OnGutsClosedListener
    public void onGutsClosed(NotificationGuts notificationGuts) {
        this.mStackScroller.onHeightChanged(null, true);
        this.mNotificationGutsExposed = null;
    }

    protected void onLockedNotificationImportanceChange(KeyguardHostView.OnDismissAction onDismissAction) {
    }

    protected void onLockedRemoteInput(ExpandableNotificationRow expandableNotificationRow, View view) {
    }

    protected void onLockedWorkRemoteInput(int i, ExpandableNotificationRow expandableNotificationRow, View view) {
    }

    public void onPanelLaidOut() {
        if (this.mState != 1 || getMaxKeyguardNotifications(false) == getMaxKeyguardNotifications(true)) {
            return;
        }
        updateRowStates();
    }

    @Override // com.android.systemui.statusbar.NotificationData.Environment
    public boolean onSecureLockScreen() {
        return isLockscreenPublicMode();
    }

    protected void onWorkChallengeUnlocked() {
    }

    public void overrideActivityPendingAppTransition(boolean z) {
        if (z) {
            try {
                this.mWindowManagerService.overridePendingAppTransition((String) null, 0, 0, (IRemoteCallback) null);
            } catch (RemoteException e) {
                Log.w("StatusBar", "Error overriding app transition: " + e);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void performRemoveNotification(StatusBarNotification statusBarNotification, boolean z) {
        try {
            this.mBarService.onNotificationClear(statusBarNotification.getPackageName(), statusBarNotification.getTag(), statusBarNotification.getId(), statusBarNotification.getUserId());
            boolean z2 = z;
            if (FORCE_REMOTE_INPUT_HISTORY) {
                z2 = z;
                if (this.mKeysKeptForRemoteInput.contains(statusBarNotification.getKey())) {
                    this.mKeysKeptForRemoteInput.remove(statusBarNotification.getKey());
                    z2 = true;
                }
            }
            if (this.mRemoteInputEntriesToRemoveOnCollapse.remove(this.mNotificationData.get(statusBarNotification.getKey()))) {
                z2 = true;
            }
            if (z2) {
                removeNotification(statusBarNotification.getKey(), null);
            }
        } catch (RemoteException e) {
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void preloadRecentApps() {
        this.mHandler.removeMessages(1022);
        this.mHandler.sendEmptyMessage(1022);
    }

    protected void preloadRecents() {
        if (this.mRecents != null) {
            this.mRecents.preloadRecents();
        }
    }

    protected abstract void refreshLayout(int i);

    public abstract void removeNotification(String str, NotificationListenerService.RankingMap rankingMap);

    /* JADX INFO: Access modifiers changed from: protected */
    public StatusBarNotification removeNotificationViews(String str, NotificationListenerService.RankingMap rankingMap) {
        NotificationData.Entry remove = this.mNotificationData.remove(str, rankingMap);
        if (remove == null) {
            Log.w("StatusBar", "removeNotification for unknown key: " + str);
            return null;
        }
        updateNotifications();
        return remove.notification;
    }

    protected void sendCloseSystemWindows(String str) {
        if (ActivityManagerNative.isSystemReady()) {
            try {
                ActivityManagerNative.getDefault().closeSystemDialogs(str);
            } catch (RemoteException e) {
            }
        }
    }

    protected abstract void setAreThereNotifications();

    public void setBouncerShowing(boolean z) {
        this.mBouncerShowing = z;
    }

    protected abstract void setHeadsUpUser(int i);

    protected void setLockScreenAllowRemoteInput(boolean z) {
        this.mAllowLockscreenRemoteInput = z;
    }

    public void setLockscreenPublicMode(boolean z) {
        this.mLockscreenPublicMode = z;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setNotificationShown(StatusBarNotification statusBarNotification) {
        setNotificationsShown(new String[]{statusBarNotification.getKey()});
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setNotificationsShown(String[] strArr) {
        try {
            this.mNotificationListener.setNotificationsShown(strArr);
        } catch (RuntimeException e) {
            Log.d("StatusBar", "failed setNotificationsShown: ", e);
        }
    }

    protected void setShowLockscreenNotifications(boolean z) {
        this.mShowLockscreenNotifications = z;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setZenMode(int i) {
        if (isDeviceProvisioned()) {
            this.mZenMode = i;
            updateNotifications();
        }
    }

    @Override // com.android.systemui.statusbar.NotificationData.Environment
    public boolean shouldHideNotifications(int i) {
        boolean z = false;
        if (isLockscreenPublicMode()) {
            z = !userAllowsNotificationsInPublic(i);
        }
        return z;
    }

    @Override // com.android.systemui.statusbar.NotificationData.Environment
    public boolean shouldHideNotifications(String str) {
        boolean z = false;
        if (isLockscreenPublicMode()) {
            z = false;
            if (this.mNotificationData.getVisibilityOverride(str) == -1) {
                z = true;
            }
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean shouldPeek(NotificationData.Entry entry) {
        return shouldPeek(entry, entry.notification);
    }

    /* JADX WARN: Removed duplicated region for block: B:28:0x007f  */
    /* JADX WARN: Removed duplicated region for block: B:35:0x00bf  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    protected boolean shouldPeek(NotificationData.Entry entry, StatusBarNotification statusBarNotification) {
        if (!this.mUseHeadsUp || isDeviceInVrMode()) {
            return false;
        }
        if (this.mNotificationData.shouldFilterOut(statusBarNotification)) {
            Log.d("StatusBar", "No peeking: filtered notification: " + statusBarNotification.getKey());
            return false;
        }
        boolean z = (!this.mPowerManager.isScreenOn() || (this.mStatusBarKeyguardViewManager.isShowing() && !this.mStatusBarKeyguardViewManager.isOccluded())) ? false : !this.mStatusBarKeyguardViewManager.isInputRestricted();
        if (z) {
            try {
            } catch (RemoteException e) {
                Log.d("StatusBar", "failed to query dream manager", e);
            }
            if (!this.mDreamManager.isDreaming()) {
                z = true;
                if (z) {
                    Log.d("StatusBar", "No peeking: not in use: " + statusBarNotification.getKey());
                    return false;
                } else if (this.mNotificationData.shouldSuppressScreenOn(statusBarNotification.getKey())) {
                    Log.d("StatusBar", "No peeking: suppressed by DND: " + statusBarNotification.getKey());
                    return false;
                } else if (entry.hasJustLaunchedFullScreenIntent()) {
                    Log.d("StatusBar", "No peeking: recent fullscreen: " + statusBarNotification.getKey());
                    return false;
                } else if (isSnoozedPackage(statusBarNotification)) {
                    Log.d("StatusBar", "No peeking: snoozed package: " + statusBarNotification.getKey());
                    return false;
                } else if (this.mNotificationData.getImportance(statusBarNotification.getKey()) < 4) {
                    Log.d("StatusBar", "No peeking: unimportant notification: " + statusBarNotification.getKey());
                    return false;
                } else if (statusBarNotification.getNotification().fullScreenIntent == null) {
                    Log.d("StatusBar", "shouldPeek: true");
                    return true;
                } else if (this.mAccessibilityManager.isTouchExplorationEnabled()) {
                    Log.d("StatusBar", "No peeking: accessible fullscreen: " + statusBarNotification.getKey());
                    return false;
                } else {
                    return true;
                }
            }
        }
        z = false;
        if (z) {
        }
    }

    public boolean shouldShowOnKeyguard(StatusBarNotification statusBarNotification) {
        boolean z = false;
        if (this.mShowLockscreenNotifications) {
            z = !this.mNotificationData.isAmbient(statusBarNotification.getKey());
        }
        return z;
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void showAssistDisclosure() {
        if (this.mAssistManager != null) {
            this.mAssistManager.showDisclosure();
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void showRecentApps(boolean z, boolean z2) {
        int i = 1;
        this.mHandler.removeMessages(1019);
        H h = this.mHandler;
        int i2 = z ? 1 : 0;
        if (!z2) {
            i = 0;
        }
        h.obtainMessage(1019, i2, i).sendToTarget();
    }

    protected void showRecents(boolean z, boolean z2) {
        if (this.mRecents != null) {
            sendCloseSystemWindows("recentapps");
            this.mRecents.showRecents(z, z2);
        }
    }

    protected void showRecentsNextAffiliatedTask() {
        if (this.mRecents != null) {
            this.mRecents.showNextAffiliatedTask();
        }
    }

    protected void showRecentsPreviousAffiliatedTask() {
        if (this.mRecents != null) {
            this.mRecents.showPrevAffiliatedTask();
        }
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        this.mWindowManagerService = WindowManagerGlobal.getWindowManagerService();
        this.mDisplay = this.mWindowManager.getDefaultDisplay();
        this.mDevicePolicyManager = (DevicePolicyManager) this.mContext.getSystemService("device_policy");
        this.mNotificationData = new NotificationData(this);
        this.mAccessibilityManager = (AccessibilityManager) this.mContext.getSystemService("accessibility");
        this.mDreamManager = IDreamManager.Stub.asInterface(ServiceManager.checkService("dreams"));
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("device_provisioned"), true, this.mSettingsObserver);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("zen_mode"), false, this.mSettingsObserver);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("lock_screen_show_notifications"), false, this.mSettingsObserver, -1);
        if (ENABLE_LOCK_SCREEN_ALLOW_REMOTE_INPUT) {
            this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("lock_screen_allow_remote_input"), false, this.mSettingsObserver, -1);
        }
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("lock_screen_allow_private_notifications"), true, this.mLockscreenSettingsObserver, -1);
        this.mBarService = IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"));
        this.mRecents = (RecentsComponent) getComponent(Recents.class);
        Configuration configuration = this.mContext.getResources().getConfiguration();
        this.mLocale = configuration.locale;
        this.mLayoutDirection = TextUtils.getLayoutDirectionFromLocale(this.mLocale);
        this.mFontScale = configuration.fontScale;
        this.mDensity = configuration.densityDpi;
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
        this.mLockPatternUtils = new LockPatternUtils(this.mContext);
        this.mCommandQueue = new CommandQueue(this);
        int[] iArr = new int[9];
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        ArrayList arrayList3 = new ArrayList();
        Rect rect = new Rect();
        Rect rect2 = new Rect();
        try {
            this.mBarService.registerStatusBar(this.mCommandQueue, arrayList2, arrayList3, iArr, arrayList, rect, rect2);
        } catch (RemoteException e) {
        }
        createAndAddWindows();
        this.mSettingsObserver.onChange(false);
        disable(iArr[0], iArr[6], false);
        setSystemUiVisibility(iArr[1], iArr[7], iArr[8], -1, rect, rect2);
        topAppWindowChanged(iArr[2] != 0);
        setImeWindowStatus((IBinder) arrayList.get(0), iArr[3], iArr[4], iArr[5] != 0);
        int size = arrayList2.size();
        for (int i = 0; i < size; i++) {
            setIcon((String) arrayList2.get(i), (StatusBarIcon) arrayList3.get(i));
        }
        try {
            this.mNotificationListener.registerAsSystemService(this.mContext, new ComponentName(this.mContext.getPackageName(), getClass().getCanonicalName()), -1);
        } catch (RemoteException e2) {
            Log.e("StatusBar", "Unable to register notification listener", e2);
        }
        this.mCurrentUserId = ActivityManager.getCurrentUser();
        setHeadsUpUser(this.mCurrentUserId);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        intentFilter.addAction("android.intent.action.USER_ADDED");
        intentFilter.addAction("android.intent.action.USER_PRESENT");
        this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("com.android.systemui.statusbar.work_challenge_unlocked_notification_action");
        intentFilter2.addAction("com.android.systemui.statusbar.banner_action_cancel");
        intentFilter2.addAction("com.android.systemui.statusbar.banner_action_setup");
        this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter2, "com.android.systemui.permission.SELF", null);
        IntentFilter intentFilter3 = new IntentFilter();
        intentFilter3.addAction("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED");
        this.mContext.registerReceiverAsUser(this.mAllUsersReceiver, UserHandle.ALL, intentFilter3, null, null);
        updateCurrentProfilesCache();
        try {
            IVrManager.Stub.asInterface(ServiceManager.getService("vrmanager")).registerListener(this.mVrStateCallbacks);
        } catch (RemoteException e3) {
            Slog.e("StatusBar", "Failed to register VR mode state listener: " + e3);
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void startAssist(Bundle bundle) {
        if (this.mAssistManager != null) {
            this.mAssistManager.startAssist(bundle);
        }
    }

    public void startPendingIntentDismissingKeyguard(PendingIntent pendingIntent) {
        if (isDeviceProvisioned()) {
            boolean isShowing = this.mStatusBarKeyguardViewManager.isShowing();
            boolean wouldLaunchResolverActivity = pendingIntent.isActivity() ? PreviewInflater.wouldLaunchResolverActivity(this.mContext, pendingIntent.getIntent(), this.mCurrentUserId) : false;
            dismissKeyguardThenExecute(new AnonymousClass14(this, isShowing, wouldLaunchResolverActivity, pendingIntent), wouldLaunchResolverActivity);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean startWorkChallengeIfNecessary(int i, IntentSender intentSender, String str) {
        Intent createConfirmDeviceCredentialIntent = this.mKeyguardManager.createConfirmDeviceCredentialIntent(null, null, i);
        if (createConfirmDeviceCredentialIntent == null) {
            return false;
        }
        Intent intent = new Intent("com.android.systemui.statusbar.work_challenge_unlocked_notification_action");
        intent.putExtra("android.intent.extra.INTENT", intentSender);
        intent.putExtra("android.intent.extra.INDEX", str);
        intent.setPackage(this.mContext.getPackageName());
        createConfirmDeviceCredentialIntent.putExtra("android.intent.extra.INTENT", PendingIntent.getBroadcast(this.mContext, 0, intent, 1409286144).getIntentSender());
        try {
            ActivityManagerNative.getDefault().startConfirmDeviceCredentialIntent(createConfirmDeviceCredentialIntent);
            return true;
        } catch (RemoteException e) {
            return true;
        }
    }

    protected void toggleKeyboardShortcuts(int i) {
        KeyboardShortcuts.toggle(this.mContext, i);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void toggleKeyboardShortcutsMenu(int i) {
        this.mHandler.removeMessages(1026);
        this.mHandler.obtainMessage(1026, i, 0).sendToTarget();
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void toggleRecentApps() {
        toggleRecents();
    }

    protected void toggleRecents() {
        if (this.mRecents != null) {
            this.mRecents.toggleRecents(this.mDisplay);
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void toggleSplitScreen() {
        toggleSplitScreenMode(-1, -1);
    }

    protected abstract void toggleSplitScreenMode(int i, int i2);

    protected abstract void updateHeadsUp(String str, NotificationData.Entry entry, boolean z, boolean z2);

    public void updateNotification(StatusBarNotification statusBarNotification, NotificationListenerService.RankingMap rankingMap) {
        Log.d("StatusBar", "updateNotification(" + statusBarNotification + ")");
        String key = statusBarNotification.getKey();
        NotificationData.Entry entry = this.mNotificationData.get(key);
        if (entry == null) {
            return;
        }
        this.mHeadsUpEntriesToRemoveOnSwitch.remove(entry);
        this.mRemoteInputEntriesToRemoveOnCollapse.remove(entry);
        Notification notification = statusBarNotification.getNotification();
        this.mNotificationData.updateRanking(rankingMap);
        boolean cacheContentViews = entry.cacheContentViews(this.mContext, statusBarNotification.getNotification());
        boolean shouldPeek = shouldPeek(entry, statusBarNotification);
        boolean alertAgain = alertAgain(entry, notification);
        StatusBarNotification statusBarNotification2 = entry.notification;
        entry.notification = statusBarNotification;
        this.mGroupManager.onEntryUpdated(entry, statusBarNotification2);
        boolean z = false;
        if (cacheContentViews) {
            try {
                if (entry.icon != null) {
                    StatusBarIcon statusBarIcon = new StatusBarIcon(statusBarNotification.getUser(), statusBarNotification.getPackageName(), notification.getSmallIcon(), notification.iconLevel, notification.number, StatusBarIconView.contentDescForNotification(this.mContext, notification));
                    entry.icon.setNotification(notification);
                    if (!entry.icon.set(statusBarIcon)) {
                        handleNotificationError(statusBarNotification, "Couldn't update icon: " + statusBarIcon);
                        return;
                    }
                }
                updateNotificationViews(entry, statusBarNotification);
                z = true;
            } catch (RuntimeException e) {
                Log.w("StatusBar", "Couldn't reapply views for package " + statusBarNotification.getPackageName(), e);
                z = false;
            }
        }
        if (!z) {
            StatusBarIcon statusBarIcon2 = new StatusBarIcon(statusBarNotification.getUser(), statusBarNotification.getPackageName(), notification.getSmallIcon(), notification.iconLevel, notification.number, StatusBarIconView.contentDescForNotification(this.mContext, notification));
            entry.icon.setNotification(notification);
            entry.icon.set(statusBarIcon2);
            inflateViews(entry, this.mStackScroller);
        }
        updateHeadsUp(key, entry, shouldPeek, alertAgain);
        updateNotifications();
        if (!statusBarNotification.isClearable()) {
            this.mStackScroller.snapViewIfNeeded(entry.row);
        }
        setAreThereNotifications();
    }

    protected abstract void updateNotificationRanking(NotificationListenerService.RankingMap rankingMap);

    protected abstract void updateNotifications();

    /* JADX INFO: Access modifiers changed from: protected */
    public void updatePublicContentView(NotificationData.Entry entry, StatusBarNotification statusBarNotification) {
        RemoteViews remoteViews = entry.cachedPublicContentView;
        View publicContentView = entry.getPublicContentView();
        if (!entry.autoRedacted || remoteViews == null || publicContentView == null) {
            return;
        }
        String string = this.mContext.getString(!adminAllowsUnredactedNotifications(entry.notification.getUserId()) ? 17039676 : 17039675);
        TextView textView = (TextView) publicContentView.findViewById(16908310);
        if (textView == null || textView.getText().toString().equals(string)) {
            return;
        }
        remoteViews.setTextViewText(16908310, string);
        remoteViews.reapply(statusBarNotification.getPackageContext(this.mContext), publicContentView, this.mOnClickHandler);
        entry.row.onNotificationUpdated(entry);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void updateRowStates() {
        int i;
        this.mKeyguardIconOverflowContainer.getIconsView().removeAllViews();
        ArrayList<NotificationData.Entry> activeNotifications = this.mNotificationData.getActiveNotifications();
        int size = activeNotifications.size();
        int i2 = 0;
        boolean z = this.mState == 1;
        int i3 = 0;
        if (z) {
            i3 = getMaxKeyguardNotifications(true);
        }
        int i4 = 0;
        while (i4 < size) {
            NotificationData.Entry entry = activeNotifications.get(i4);
            boolean isChildInGroupWithSummary = this.mGroupManager.isChildInGroupWithSummary(entry.notification);
            if (z) {
                entry.row.setOnKeyguard(true);
            } else {
                entry.row.setOnKeyguard(false);
                entry.row.setSystemExpanded(i2 == 0 && !isChildInGroupWithSummary);
            }
            boolean z2 = this.mGroupManager.isSummaryOfSuppressedGroup(entry.notification) ? !entry.row.isRemoved() : false;
            boolean z3 = isChildInGroupWithSummary ? this.mGroupManager.getGroupSummary(entry.notification).getVisibility() == 0 : false;
            boolean shouldShowOnKeyguard = shouldShowOnKeyguard(entry.notification);
            if (z2 || ((isLockscreenPublicMode() && !this.mShowLockscreenNotifications) || (z && !z3 && (i2 >= i3 || !shouldShowOnKeyguard)))) {
                entry.row.setVisibility(8);
                i = i2;
                if (z) {
                    i = i2;
                    if (shouldShowOnKeyguard) {
                        i = i2;
                        if (!isChildInGroupWithSummary) {
                            i = i2;
                            if (!z2) {
                                this.mKeyguardIconOverflowContainer.getIconsView().addNotification(entry);
                                i = i2;
                            }
                        }
                    }
                }
            } else {
                boolean z4 = entry.row.getVisibility() == 8;
                entry.row.setVisibility(0);
                i = i2;
                if (!isChildInGroupWithSummary) {
                    if (entry.row.isRemoved()) {
                        i = i2;
                    } else {
                        if (z4) {
                            this.mStackScroller.generateAddAnimation(entry.row, !shouldShowOnKeyguard);
                        }
                        i = i2 + 1;
                    }
                }
            }
            i4++;
            i2 = i;
        }
        this.mStackScroller.updateOverflowContainerVisibility(z ? this.mKeyguardIconOverflowContainer.getIconsView().getChildCount() > 0 : false);
        this.mStackScroller.changeViewPosition(this.mDismissView, this.mStackScroller.getChildCount() - 1);
        this.mStackScroller.changeViewPosition(this.mEmptyShadeView, this.mStackScroller.getChildCount() - 2);
        this.mStackScroller.changeViewPosition(this.mKeyguardIconOverflowContainer, this.mStackScroller.getChildCount() - 3);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void updateVisibleToUser() {
        boolean z = this.mVisibleToUser;
        this.mVisibleToUser = this.mVisible ? this.mDeviceInteractive : false;
        if (z != this.mVisibleToUser) {
            handleVisibleToUserChanged(this.mVisibleToUser);
        }
    }

    public boolean userAllowsNotificationsInPublic(int i) {
        if (i == -1) {
            return true;
        }
        if (this.mUsersAllowingNotifications.indexOfKey(i) < 0) {
            boolean z = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "lock_screen_show_notifications", 0, i) != 0;
            this.mUsersAllowingNotifications.append(i, z);
            return z;
        }
        return this.mUsersAllowingNotifications.get(i);
    }

    public boolean userAllowsPrivateNotificationsInPublic(int i) {
        if (i == -1) {
            return true;
        }
        if (this.mUsersAllowingPrivateNotifications.indexOfKey(i) < 0) {
            boolean z = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "lock_screen_allow_private_notifications", 0, i) != 0;
            boolean adminAllowsUnredactedNotifications = adminAllowsUnredactedNotifications(i);
            if (!z) {
                adminAllowsUnredactedNotifications = false;
            }
            this.mUsersAllowingPrivateNotifications.append(i, adminAllowsUnredactedNotifications);
            return adminAllowsUnredactedNotifications;
        }
        return this.mUsersAllowingPrivateNotifications.get(i);
    }

    public void userSwitched(int i) {
        setHeadsUpUser(i);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void visibilityChanged(boolean z) {
        if (this.mVisible != z) {
            this.mVisible = z;
            if (!z) {
                dismissPopups();
            }
        }
        updateVisibleToUser();
    }

    protected void workAroundBadLayerDrawableOpacity(View view) {
    }
}
