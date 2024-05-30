package com.android.systemui.statusbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.INotificationManager;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Property;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.statusbar.NotificationGuts;
import java.util.List;
/* loaded from: classes.dex */
public class NotificationInfo extends LinearLayout implements NotificationGuts.GutsContent {
    private String mAppName;
    private OnAppSettingsClickListener mAppSettingsClickListener;
    private int mAppUid;
    private CheckSaveListener mCheckSaveListener;
    private int mChosenImportance;
    private String mExitReason;
    private AnimatorSet mExpandAnimation;
    private NotificationGuts mGutsContainer;
    private INotificationManager mINotificationManager;
    private boolean mIsForBlockingHelper;
    private boolean mIsForeground;
    private boolean mIsNonblockable;
    private boolean mIsSingleDefaultChannel;
    private MetricsLogger mMetricsLogger;
    private boolean mNegativeUserSentiment;
    private int mNumUniqueChannelsInRow;
    private View.OnClickListener mOnKeepShowing;
    private OnSettingsClickListener mOnSettingsClickListener;
    private View.OnClickListener mOnStopOrMinimizeNotifications;
    private View.OnClickListener mOnUndo;
    private String mPackageName;
    private PackageManager mPm;
    private StatusBarNotification mSbn;
    private NotificationChannel mSingleNotificationChannel;
    private int mStartingUserImportance;

    /* loaded from: classes.dex */
    public interface CheckSaveListener {
        void checkSave(Runnable runnable, StatusBarNotification statusBarNotification);
    }

    /* loaded from: classes.dex */
    public interface OnAppSettingsClickListener {
        void onClick(View view, Intent intent);
    }

    /* loaded from: classes.dex */
    public interface OnSettingsClickListener {
        void onClick(View view, NotificationChannel notificationChannel, int i);
    }

    public static /* synthetic */ void lambda$new$0(NotificationInfo notificationInfo, View view) {
        notificationInfo.mExitReason = "blocking_helper_keep_showing";
        notificationInfo.closeControls(view);
    }

    public static /* synthetic */ void lambda$new$1(NotificationInfo notificationInfo, View view) {
        notificationInfo.mExitReason = "blocking_helper_stop_notifications";
        notificationInfo.swapContent(false);
    }

    public static /* synthetic */ void lambda$new$2(NotificationInfo notificationInfo, View view) {
        notificationInfo.mExitReason = "blocking_helper_dismissed";
        notificationInfo.logBlockingHelperCounter("blocking_helper_undo");
        notificationInfo.swapContent(true);
    }

    public NotificationInfo(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mExitReason = "blocking_helper_dismissed";
        this.mOnKeepShowing = new View.OnClickListener() { // from class: com.android.systemui.statusbar.-$$Lambda$NotificationInfo$OE4i5uy2V1VNOIOCPk_l9LEjxGg
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                NotificationInfo.lambda$new$0(NotificationInfo.this, view);
            }
        };
        this.mOnStopOrMinimizeNotifications = new View.OnClickListener() { // from class: com.android.systemui.statusbar.-$$Lambda$NotificationInfo$-_FAMjCKcojvnymBpf3GU21SM4A
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                NotificationInfo.lambda$new$1(NotificationInfo.this, view);
            }
        };
        this.mOnUndo = new View.OnClickListener() { // from class: com.android.systemui.statusbar.-$$Lambda$NotificationInfo$7kxiIPS-E1y-_HRLaUJLceph_TY
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                NotificationInfo.lambda$new$2(NotificationInfo.this, view);
            }
        };
    }

    @VisibleForTesting
    void bindNotification(PackageManager packageManager, INotificationManager iNotificationManager, String str, NotificationChannel notificationChannel, int i, StatusBarNotification statusBarNotification, CheckSaveListener checkSaveListener, OnSettingsClickListener onSettingsClickListener, OnAppSettingsClickListener onAppSettingsClickListener, boolean z) throws RemoteException {
        bindNotification(packageManager, iNotificationManager, str, notificationChannel, i, statusBarNotification, checkSaveListener, onSettingsClickListener, onAppSettingsClickListener, z, false, false);
    }

    public void bindNotification(PackageManager packageManager, INotificationManager iNotificationManager, String str, NotificationChannel notificationChannel, int i, StatusBarNotification statusBarNotification, CheckSaveListener checkSaveListener, OnSettingsClickListener onSettingsClickListener, OnAppSettingsClickListener onAppSettingsClickListener, boolean z, boolean z2, boolean z3) throws RemoteException {
        this.mINotificationManager = iNotificationManager;
        this.mMetricsLogger = (MetricsLogger) Dependency.get(MetricsLogger.class);
        this.mPackageName = str;
        this.mNumUniqueChannelsInRow = i;
        this.mSbn = statusBarNotification;
        this.mPm = packageManager;
        this.mAppSettingsClickListener = onAppSettingsClickListener;
        this.mAppName = this.mPackageName;
        this.mCheckSaveListener = checkSaveListener;
        this.mOnSettingsClickListener = onSettingsClickListener;
        this.mSingleNotificationChannel = notificationChannel;
        int importance = this.mSingleNotificationChannel.getImportance();
        this.mChosenImportance = importance;
        this.mStartingUserImportance = importance;
        this.mNegativeUserSentiment = z3;
        this.mIsNonblockable = z;
        boolean z4 = false;
        this.mIsForeground = (this.mSbn.getNotification().flags & 64) != 0;
        this.mIsForBlockingHelper = z2;
        this.mAppUid = this.mSbn.getUid();
        int numNotificationChannelsForPackage = this.mINotificationManager.getNumNotificationChannelsForPackage(str, this.mAppUid, false);
        if (this.mNumUniqueChannelsInRow == 0) {
            throw new IllegalArgumentException("bindNotification requires at least one channel");
        }
        if (this.mNumUniqueChannelsInRow == 1 && this.mSingleNotificationChannel.getId().equals("miscellaneous") && numNotificationChannelsForPackage == 1) {
            z4 = true;
        }
        this.mIsSingleDefaultChannel = z4;
        bindHeader();
        bindPrompt();
        bindButtons();
    }

    private void bindHeader() throws RemoteException {
        Drawable defaultActivityIcon;
        NotificationChannelGroup notificationChannelGroupForPackage;
        CharSequence charSequence = null;
        try {
            ApplicationInfo applicationInfo = this.mPm.getApplicationInfo(this.mPackageName, 795136);
            if (applicationInfo != null) {
                this.mAppName = String.valueOf(this.mPm.getApplicationLabel(applicationInfo));
                defaultActivityIcon = this.mPm.getApplicationIcon(applicationInfo);
            } else {
                defaultActivityIcon = null;
            }
        } catch (PackageManager.NameNotFoundException e) {
            defaultActivityIcon = this.mPm.getDefaultActivityIcon();
        }
        ((ImageView) findViewById(R.id.pkgicon)).setImageDrawable(defaultActivityIcon);
        ((TextView) findViewById(R.id.pkgname)).setText(this.mAppName);
        if (this.mSingleNotificationChannel != null && this.mSingleNotificationChannel.getGroup() != null && (notificationChannelGroupForPackage = this.mINotificationManager.getNotificationChannelGroupForPackage(this.mSingleNotificationChannel.getGroup(), this.mPackageName, this.mAppUid)) != null) {
            charSequence = notificationChannelGroupForPackage.getName();
        }
        TextView textView = (TextView) findViewById(R.id.group_name);
        TextView textView2 = (TextView) findViewById(R.id.pkg_group_divider);
        if (charSequence != null) {
            textView.setText(charSequence);
            textView.setVisibility(0);
            textView2.setVisibility(0);
        } else {
            textView.setVisibility(8);
            textView2.setVisibility(8);
        }
        View findViewById = findViewById(R.id.info);
        if (this.mAppUid >= 0 && this.mOnSettingsClickListener != null) {
            findViewById.setVisibility(0);
            final int i = this.mAppUid;
            findViewById.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.statusbar.-$$Lambda$NotificationInfo$pxVS_YLdCQFH6VceUnIC5mpFX7A
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    NotificationInfo.lambda$bindHeader$3(NotificationInfo.this, i, view);
                }
            });
            return;
        }
        findViewById.setVisibility(8);
    }

    public static /* synthetic */ void lambda$bindHeader$3(NotificationInfo notificationInfo, int i, View view) {
        notificationInfo.logBlockingHelperCounter("blocking_helper_notif_settings");
        notificationInfo.mOnSettingsClickListener.onClick(view, notificationInfo.mNumUniqueChannelsInRow > 1 ? null : notificationInfo.mSingleNotificationChannel, i);
    }

    private void bindPrompt() {
        TextView textView = (TextView) findViewById(R.id.block_prompt);
        bindName();
        if (this.mIsNonblockable) {
            textView.setText(R.string.notification_unblockable_desc);
        } else if (this.mNegativeUserSentiment) {
            textView.setText(R.string.inline_blocking_helper);
        } else if (this.mIsSingleDefaultChannel || this.mNumUniqueChannelsInRow > 1) {
            textView.setText(R.string.inline_keep_showing_app);
        } else {
            textView.setText(R.string.inline_keep_showing);
        }
    }

    private void bindName() {
        TextView textView = (TextView) findViewById(R.id.channel_name);
        if (this.mIsSingleDefaultChannel || this.mNumUniqueChannelsInRow > 1) {
            textView.setVisibility(8);
        } else {
            textView.setText(this.mSingleNotificationChannel.getName());
        }
    }

    @VisibleForTesting
    void logBlockingHelperCounter(String str) {
        if (this.mIsForBlockingHelper) {
            this.mMetricsLogger.count(str, 1);
        }
    }

    private boolean hasImportanceChanged() {
        return (this.mSingleNotificationChannel == null || this.mStartingUserImportance == this.mChosenImportance) ? false : true;
    }

    private void saveImportance() {
        if (!this.mIsNonblockable) {
            if (this.mCheckSaveListener != null && "blocking_helper_stop_notifications".equals(this.mExitReason)) {
                this.mCheckSaveListener.checkSave(new Runnable() { // from class: com.android.systemui.statusbar.-$$Lambda$NotificationInfo$FcHfx_eVn3zstWC-BeoEQcmo1DY
                    @Override // java.lang.Runnable
                    public final void run() {
                        NotificationInfo.this.updateImportance();
                    }
                }, this.mSbn);
            } else {
                updateImportance();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateImportance() {
        MetricsLogger.action(this.mContext, 291, this.mChosenImportance - this.mStartingUserImportance);
        new Handler((Looper) Dependency.get(Dependency.BG_LOOPER)).post(new UpdateImportanceRunnable(this.mINotificationManager, this.mPackageName, this.mAppUid, this.mNumUniqueChannelsInRow == 1 ? this.mSingleNotificationChannel : null, this.mStartingUserImportance, this.mChosenImportance));
    }

    private void bindButtons() {
        View findViewById = findViewById(R.id.block);
        TextView textView = (TextView) findViewById(R.id.keep);
        View findViewById2 = findViewById(R.id.minimize);
        findViewById(R.id.undo).setOnClickListener(this.mOnUndo);
        findViewById.setOnClickListener(this.mOnStopOrMinimizeNotifications);
        textView.setOnClickListener(this.mOnKeepShowing);
        findViewById2.setOnClickListener(this.mOnStopOrMinimizeNotifications);
        if (this.mIsNonblockable) {
            textView.setText(17039370);
            findViewById.setVisibility(8);
            findViewById2.setVisibility(8);
        } else if (this.mIsForeground) {
            findViewById.setVisibility(8);
            findViewById2.setVisibility(0);
        } else if (!this.mIsForeground) {
            findViewById.setVisibility(0);
            findViewById2.setVisibility(8);
        }
        TextView textView2 = (TextView) findViewById(R.id.app_settings);
        final Intent appSettingsIntent = getAppSettingsIntent(this.mPm, this.mPackageName, this.mSingleNotificationChannel, this.mSbn.getId(), this.mSbn.getTag());
        if (!this.mIsForBlockingHelper && appSettingsIntent != null && !TextUtils.isEmpty(this.mSbn.getNotification().getSettingsText())) {
            textView2.setVisibility(0);
            textView2.setText(this.mContext.getString(R.string.notification_app_settings));
            textView2.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.statusbar.-$$Lambda$NotificationInfo$uIoadfdpLliB-jGM6g1VRlhmUaQ
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    NotificationInfo.this.mAppSettingsClickListener.onClick(view, appSettingsIntent);
                }
            });
            return;
        }
        textView2.setVisibility(8);
    }

    private void swapContent(final boolean z) {
        if (this.mExpandAnimation != null) {
            this.mExpandAnimation.cancel();
        }
        final View findViewById = findViewById(R.id.prompt);
        final ViewGroup viewGroup = (ViewGroup) findViewById(R.id.confirmation);
        TextView textView = (TextView) findViewById(R.id.confirmation_text);
        View findViewById2 = findViewById(R.id.header);
        if (z) {
            this.mChosenImportance = this.mStartingUserImportance;
        } else if (this.mIsForeground) {
            this.mChosenImportance = 1;
            textView.setText(R.string.notification_channel_minimized);
        } else {
            this.mChosenImportance = 0;
            textView.setText(R.string.notification_channel_disabled);
        }
        Property property = View.ALPHA;
        float[] fArr = new float[2];
        fArr[0] = findViewById.getAlpha();
        fArr[1] = z ? 1.0f : 0.0f;
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(findViewById, property, fArr);
        ofFloat.setInterpolator(z ? Interpolators.ALPHA_IN : Interpolators.ALPHA_OUT);
        Property property2 = View.ALPHA;
        float[] fArr2 = new float[2];
        fArr2[0] = viewGroup.getAlpha();
        fArr2[1] = z ? 0.0f : 1.0f;
        ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(viewGroup, property2, fArr2);
        ofFloat2.setInterpolator(z ? Interpolators.ALPHA_OUT : Interpolators.ALPHA_IN);
        findViewById.setVisibility(z ? 0 : 8);
        viewGroup.setVisibility(z ? 8 : 0);
        findViewById2.setVisibility(z ? 0 : 8);
        this.mExpandAnimation = new AnimatorSet();
        this.mExpandAnimation.playTogether(ofFloat, ofFloat2);
        this.mExpandAnimation.setDuration(150L);
        this.mExpandAnimation.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.NotificationInfo.1
            boolean cancelled = false;

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                this.cancelled = true;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (!this.cancelled) {
                    findViewById.setVisibility(z ? 0 : 8);
                    viewGroup.setVisibility(z ? 8 : 0);
                }
            }
        });
        this.mExpandAnimation.start();
    }

    @Override // android.view.View
    public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onInitializeAccessibilityEvent(accessibilityEvent);
        if (this.mGutsContainer != null && accessibilityEvent.getEventType() == 32) {
            if (this.mGutsContainer.isExposed()) {
                accessibilityEvent.getText().add(this.mContext.getString(R.string.notification_channel_controls_opened_accessibility, this.mAppName));
            } else {
                accessibilityEvent.getText().add(this.mContext.getString(R.string.notification_channel_controls_closed_accessibility, this.mAppName));
            }
        }
    }

    private Intent getAppSettingsIntent(PackageManager packageManager, String str, NotificationChannel notificationChannel, int i, String str2) {
        Intent intent = new Intent("android.intent.action.MAIN").addCategory("android.intent.category.NOTIFICATION_PREFERENCES").setPackage(str);
        List<ResolveInfo> queryIntentActivities = packageManager.queryIntentActivities(intent, 65536);
        if (queryIntentActivities == null || queryIntentActivities.size() == 0 || queryIntentActivities.get(0) == null) {
            return null;
        }
        ActivityInfo activityInfo = queryIntentActivities.get(0).activityInfo;
        intent.setClassName(activityInfo.packageName, activityInfo.name);
        if (notificationChannel != null) {
            intent.putExtra("android.intent.extra.CHANNEL_ID", notificationChannel.getId());
        }
        intent.putExtra("android.intent.extra.NOTIFICATION_ID", i);
        intent.putExtra("android.intent.extra.NOTIFICATION_TAG", str2);
        return intent;
    }

    @VisibleForTesting
    void closeControls(View view) {
        int[] iArr = new int[2];
        int[] iArr2 = new int[2];
        this.mGutsContainer.getLocationOnScreen(iArr);
        view.getLocationOnScreen(iArr2);
        int i = iArr2[0] - iArr[0];
        int height = (iArr2[1] - iArr[1]) + (view.getHeight() / 2);
        this.mGutsContainer.closeControls(i + (view.getWidth() / 2), height, true, false);
    }

    @Override // com.android.systemui.statusbar.NotificationGuts.GutsContent
    public void setGutsParent(NotificationGuts notificationGuts) {
        this.mGutsContainer = notificationGuts;
    }

    @Override // com.android.systemui.statusbar.NotificationGuts.GutsContent
    public boolean willBeRemoved() {
        return hasImportanceChanged();
    }

    @Override // com.android.systemui.statusbar.NotificationGuts.GutsContent
    public boolean shouldBeSaved() {
        return hasImportanceChanged();
    }

    @Override // com.android.systemui.statusbar.NotificationGuts.GutsContent
    public View getContentView() {
        return this;
    }

    @Override // com.android.systemui.statusbar.NotificationGuts.GutsContent
    public boolean handleCloseControls(boolean z, boolean z2) {
        if (z) {
            saveImportance();
        }
        logBlockingHelperCounter(this.mExitReason);
        return false;
    }

    @Override // com.android.systemui.statusbar.NotificationGuts.GutsContent
    public int getActualHeight() {
        return getHeight();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class UpdateImportanceRunnable implements Runnable {
        private final int mAppUid;
        private final NotificationChannel mChannelToUpdate;
        private final int mCurrentImportance;
        private final INotificationManager mINotificationManager;
        private final int mNewImportance;
        private final String mPackageName;

        public UpdateImportanceRunnable(INotificationManager iNotificationManager, String str, int i, NotificationChannel notificationChannel, int i2, int i3) {
            this.mINotificationManager = iNotificationManager;
            this.mPackageName = str;
            this.mAppUid = i;
            this.mChannelToUpdate = notificationChannel;
            this.mCurrentImportance = i2;
            this.mNewImportance = i3;
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                if (this.mChannelToUpdate != null) {
                    this.mChannelToUpdate.setImportance(this.mNewImportance);
                    this.mChannelToUpdate.lockFields(4);
                    this.mINotificationManager.updateNotificationChannelForPackage(this.mPackageName, this.mAppUid, this.mChannelToUpdate);
                } else {
                    this.mINotificationManager.setNotificationsEnabledWithImportanceLockForPackage(this.mPackageName, this.mAppUid, this.mNewImportance >= this.mCurrentImportance);
                }
            } catch (RemoteException e) {
                Log.e("InfoGuts", "Unable to update notification importance", e);
            }
        }
    }
}
