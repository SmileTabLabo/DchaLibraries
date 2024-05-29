package com.android.systemui.statusbar;

import android.app.INotificationManager;
import android.app.NotificationChannel;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.BenesseExtension;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.service.notification.StatusBarNotification;
import android.util.ArraySet;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin;
import com.android.systemui.statusbar.AppOpsInfo;
import com.android.systemui.statusbar.NotificationGuts;
import com.android.systemui.statusbar.NotificationInfo;
import com.android.systemui.statusbar.phone.StatusBar;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Objects;
/* loaded from: classes.dex */
public class NotificationGutsManager implements Dumpable {
    private final AccessibilityManager mAccessibilityManager;
    private NotificationInfo.CheckSaveListener mCheckSaveListener;
    private final Context mContext;
    protected NotificationEntryManager mEntryManager;
    private NotificationMenuRowPlugin.MenuItem mGutsMenuItem;
    private String mKeyToRemoveOnGutsClosed;
    private NotificationListContainer mListContainer;
    private NotificationGuts mNotificationGutsExposed;
    private OnSettingsClickListener mOnSettingsClickListener;
    protected NotificationPresenter mPresenter;
    private final MetricsLogger mMetricsLogger = (MetricsLogger) Dependency.get(MetricsLogger.class);
    private final NotificationLockscreenUserManager mLockscreenUserManager = (NotificationLockscreenUserManager) Dependency.get(NotificationLockscreenUserManager.class);

    /* loaded from: classes.dex */
    public interface OnSettingsClickListener {
        void onClick(String str);
    }

    public NotificationGutsManager(Context context) {
        this.mContext = context;
        context.getResources();
        this.mAccessibilityManager = (AccessibilityManager) this.mContext.getSystemService("accessibility");
    }

    public void setUpWithPresenter(NotificationPresenter notificationPresenter, NotificationEntryManager notificationEntryManager, NotificationListContainer notificationListContainer, NotificationInfo.CheckSaveListener checkSaveListener, OnSettingsClickListener onSettingsClickListener) {
        this.mPresenter = notificationPresenter;
        this.mEntryManager = notificationEntryManager;
        this.mListContainer = notificationListContainer;
        this.mCheckSaveListener = checkSaveListener;
        this.mOnSettingsClickListener = onSettingsClickListener;
    }

    public String getKeyToRemoveOnGutsClosed() {
        return this.mKeyToRemoveOnGutsClosed;
    }

    public void setKeyToRemoveOnGutsClosed(String str) {
        this.mKeyToRemoveOnGutsClosed = str;
    }

    public void onDensityOrFontScaleChanged(ExpandableNotificationRow expandableNotificationRow) {
        setExposedGuts(expandableNotificationRow.getGuts());
        bindGuts(expandableNotificationRow);
    }

    private void startAppNotificationSettingsActivity(String str, int i, NotificationChannel notificationChannel, ExpandableNotificationRow expandableNotificationRow) {
        if (BenesseExtension.getDchaState() != 0) {
            return;
        }
        Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
        intent.setData(Uri.fromParts("package", str, null));
        intent.putExtra("android.provider.extra.APP_PACKAGE", str);
        intent.putExtra("app_uid", i);
        if (notificationChannel != null) {
            intent.putExtra(":settings:fragment_args_key", notificationChannel.getId());
        }
        this.mPresenter.startNotificationGutsIntent(intent, i, expandableNotificationRow);
    }

    protected void startAppOpsSettingsActivity(String str, int i, ArraySet<Integer> arraySet, ExpandableNotificationRow expandableNotificationRow) {
        if (arraySet.contains(24)) {
            if (arraySet.contains(26) || arraySet.contains(27)) {
                startAppNotificationSettingsActivity(str, i, null, expandableNotificationRow);
            } else if (BenesseExtension.getDchaState() != 0) {
            } else {
                Intent intent = new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION");
                intent.setData(Uri.fromParts("package", str, null));
                this.mPresenter.startNotificationGutsIntent(intent, i, expandableNotificationRow);
            }
        } else if (arraySet.contains(26) || arraySet.contains(27)) {
            Intent intent2 = new Intent("android.intent.action.MANAGE_APP_PERMISSIONS");
            intent2.putExtra("android.intent.extra.PACKAGE_NAME", str);
            this.mPresenter.startNotificationGutsIntent(intent2, i, expandableNotificationRow);
        }
    }

    public void bindGuts(ExpandableNotificationRow expandableNotificationRow) {
        bindGuts(expandableNotificationRow, this.mGutsMenuItem);
    }

    private void bindGuts(final ExpandableNotificationRow expandableNotificationRow, NotificationMenuRowPlugin.MenuItem menuItem) {
        final StatusBarNotification statusBarNotification = expandableNotificationRow.getStatusBarNotification();
        expandableNotificationRow.inflateGuts();
        expandableNotificationRow.setGutsView(menuItem);
        expandableNotificationRow.setTag(statusBarNotification.getPackageName());
        expandableNotificationRow.getGuts().setClosedListener(new NotificationGuts.OnGutsClosedListener() { // from class: com.android.systemui.statusbar.-$$Lambda$NotificationGutsManager$5BbQHmFW6nuVeiAwlAjRPvwSbrU
            @Override // com.android.systemui.statusbar.NotificationGuts.OnGutsClosedListener
            public final void onGutsClosed(NotificationGuts notificationGuts) {
                NotificationGutsManager.lambda$bindGuts$0(NotificationGutsManager.this, expandableNotificationRow, statusBarNotification, notificationGuts);
            }
        });
        View gutsView = menuItem.getGutsView();
        if (gutsView instanceof NotificationSnooze) {
            initializeSnoozeView(expandableNotificationRow, (NotificationSnooze) gutsView);
        } else if (gutsView instanceof AppOpsInfo) {
            initializeAppOpsInfo(expandableNotificationRow, (AppOpsInfo) gutsView);
        } else if (gutsView instanceof NotificationInfo) {
            initializeNotificationInfo(expandableNotificationRow, (NotificationInfo) gutsView);
        }
    }

    public static /* synthetic */ void lambda$bindGuts$0(NotificationGutsManager notificationGutsManager, ExpandableNotificationRow expandableNotificationRow, StatusBarNotification statusBarNotification, NotificationGuts notificationGuts) {
        expandableNotificationRow.onGutsClosed();
        if (!notificationGuts.willBeRemoved() && !expandableNotificationRow.isRemoved()) {
            notificationGutsManager.mListContainer.onHeightChanged(expandableNotificationRow, !notificationGutsManager.mPresenter.isPresenterFullyCollapsed());
        }
        if (notificationGutsManager.mNotificationGutsExposed == notificationGuts) {
            notificationGutsManager.mNotificationGutsExposed = null;
            notificationGutsManager.mGutsMenuItem = null;
        }
        String key = statusBarNotification.getKey();
        if (key.equals(notificationGutsManager.mKeyToRemoveOnGutsClosed)) {
            notificationGutsManager.mKeyToRemoveOnGutsClosed = null;
            notificationGutsManager.mEntryManager.removeNotification(key, notificationGutsManager.mEntryManager.getLatestRankingMap());
        }
    }

    private void initializeSnoozeView(final ExpandableNotificationRow expandableNotificationRow, NotificationSnooze notificationSnooze) {
        NotificationGuts guts = expandableNotificationRow.getGuts();
        StatusBarNotification statusBarNotification = expandableNotificationRow.getStatusBarNotification();
        notificationSnooze.setSnoozeListener(this.mListContainer.getSwipeActionHelper());
        notificationSnooze.setStatusBarNotification(statusBarNotification);
        notificationSnooze.setSnoozeOptions(expandableNotificationRow.getEntry().snoozeCriteria);
        guts.setHeightChangedListener(new NotificationGuts.OnHeightChangedListener() { // from class: com.android.systemui.statusbar.-$$Lambda$NotificationGutsManager$cVmDFdPRr0bMQ_wKyHQ8UdoctmE
            @Override // com.android.systemui.statusbar.NotificationGuts.OnHeightChangedListener
            public final void onHeightChanged(NotificationGuts notificationGuts) {
                NotificationGutsManager.this.mListContainer.onHeightChanged(r1, expandableNotificationRow.isShown());
            }
        });
    }

    private void initializeAppOpsInfo(final ExpandableNotificationRow expandableNotificationRow, AppOpsInfo appOpsInfo) {
        final NotificationGuts guts = expandableNotificationRow.getGuts();
        StatusBarNotification statusBarNotification = expandableNotificationRow.getStatusBarNotification();
        PackageManager packageManagerForUser = StatusBar.getPackageManagerForUser(this.mContext, statusBarNotification.getUser().getIdentifier());
        AppOpsInfo.OnSettingsClickListener onSettingsClickListener = new AppOpsInfo.OnSettingsClickListener() { // from class: com.android.systemui.statusbar.-$$Lambda$NotificationGutsManager$sf1-N5qoeH1wYt_-Wu4HHDalZDE
            @Override // com.android.systemui.statusbar.AppOpsInfo.OnSettingsClickListener
            public final void onClick(View view, String str, int i, ArraySet arraySet) {
                NotificationGutsManager.lambda$initializeAppOpsInfo$2(NotificationGutsManager.this, guts, expandableNotificationRow, view, str, i, arraySet);
            }
        };
        if (!expandableNotificationRow.getEntry().mActiveAppOps.isEmpty()) {
            appOpsInfo.bindGuts(packageManagerForUser, onSettingsClickListener, statusBarNotification, expandableNotificationRow.getEntry().mActiveAppOps);
        }
    }

    public static /* synthetic */ void lambda$initializeAppOpsInfo$2(NotificationGutsManager notificationGutsManager, NotificationGuts notificationGuts, ExpandableNotificationRow expandableNotificationRow, View view, String str, int i, ArraySet arraySet) {
        notificationGutsManager.mMetricsLogger.action(1346);
        notificationGuts.resetFalsingCheck();
        notificationGutsManager.startAppOpsSettingsActivity(str, i, arraySet, expandableNotificationRow);
    }

    void initializeNotificationInfo(final ExpandableNotificationRow expandableNotificationRow, NotificationInfo notificationInfo) {
        NotificationInfo.OnSettingsClickListener onSettingsClickListener;
        final NotificationGuts guts = expandableNotificationRow.getGuts();
        final StatusBarNotification statusBarNotification = expandableNotificationRow.getStatusBarNotification();
        final String packageName = statusBarNotification.getPackageName();
        UserHandle user = statusBarNotification.getUser();
        PackageManager packageManagerForUser = StatusBar.getPackageManagerForUser(this.mContext, user.getIdentifier());
        INotificationManager asInterface = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
        NotificationInfo.OnAppSettingsClickListener onAppSettingsClickListener = new NotificationInfo.OnAppSettingsClickListener() { // from class: com.android.systemui.statusbar.-$$Lambda$NotificationGutsManager$5ccSwmHai9tNnko8L2PlieI593k
            @Override // com.android.systemui.statusbar.NotificationInfo.OnAppSettingsClickListener
            public final void onClick(View view, Intent intent) {
                NotificationGutsManager.lambda$initializeNotificationInfo$3(NotificationGutsManager.this, guts, statusBarNotification, expandableNotificationRow, view, intent);
            }
        };
        boolean isBlockingHelperShowing = expandableNotificationRow.isBlockingHelperShowing();
        if (!user.equals(UserHandle.ALL) || this.mLockscreenUserManager.getCurrentUserId() == 0) {
            onSettingsClickListener = new NotificationInfo.OnSettingsClickListener() { // from class: com.android.systemui.statusbar.-$$Lambda$NotificationGutsManager$CuJJD4QSv95ak-chMNjvs4UJcoM
                @Override // com.android.systemui.statusbar.NotificationInfo.OnSettingsClickListener
                public final void onClick(View view, NotificationChannel notificationChannel, int i) {
                    NotificationGutsManager.lambda$initializeNotificationInfo$4(NotificationGutsManager.this, guts, statusBarNotification, packageName, expandableNotificationRow, view, notificationChannel, i);
                }
            };
        } else {
            onSettingsClickListener = null;
        }
        try {
            notificationInfo.bindNotification(packageManagerForUser, asInterface, packageName, expandableNotificationRow.getEntry().channel, expandableNotificationRow.getNumUniqueChannels(), statusBarNotification, this.mCheckSaveListener, onSettingsClickListener, onAppSettingsClickListener, expandableNotificationRow.getIsNonblockable(), isBlockingHelperShowing, expandableNotificationRow.getEntry().userSentiment == -1);
        } catch (RemoteException e) {
            Log.e("NotificationGutsManager", e.toString());
        }
    }

    public static /* synthetic */ void lambda$initializeNotificationInfo$3(NotificationGutsManager notificationGutsManager, NotificationGuts notificationGuts, StatusBarNotification statusBarNotification, ExpandableNotificationRow expandableNotificationRow, View view, Intent intent) {
        notificationGutsManager.mMetricsLogger.action(206);
        notificationGuts.resetFalsingCheck();
        notificationGutsManager.mPresenter.startNotificationGutsIntent(intent, statusBarNotification.getUid(), expandableNotificationRow);
    }

    public static /* synthetic */ void lambda$initializeNotificationInfo$4(NotificationGutsManager notificationGutsManager, NotificationGuts notificationGuts, StatusBarNotification statusBarNotification, String str, ExpandableNotificationRow expandableNotificationRow, View view, NotificationChannel notificationChannel, int i) {
        notificationGutsManager.mMetricsLogger.action(205);
        notificationGuts.resetFalsingCheck();
        notificationGutsManager.mOnSettingsClickListener.onClick(statusBarNotification.getKey());
        notificationGutsManager.startAppNotificationSettingsActivity(str, i, notificationChannel, expandableNotificationRow);
    }

    public void closeAndSaveGuts(boolean z, boolean z2, boolean z3, int i, int i2, boolean z4) {
        if (this.mNotificationGutsExposed != null) {
            this.mNotificationGutsExposed.closeControls(z, z3, i, i2, z2);
        }
        if (z4) {
            this.mListContainer.resetExposedMenuView(false, true);
        }
    }

    public NotificationGuts getExposedGuts() {
        return this.mNotificationGutsExposed;
    }

    public void setExposedGuts(NotificationGuts notificationGuts) {
        this.mNotificationGutsExposed = notificationGuts;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean openGuts(View view, final int i, final int i2, final NotificationMenuRowPlugin.MenuItem menuItem) {
        if (view instanceof ExpandableNotificationRow) {
            if (view.getWindowToken() == null) {
                Log.e("NotificationGutsManager", "Trying to show notification guts, but not attached to window");
                return false;
            }
            final ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
            if (expandableNotificationRow.isDark()) {
                return false;
            }
            view.performHapticFeedback(0);
            if (expandableNotificationRow.areGutsExposed()) {
                closeAndSaveGuts(false, false, true, -1, -1, true);
                return false;
            }
            bindGuts(expandableNotificationRow, menuItem);
            final NotificationGuts guts = expandableNotificationRow.getGuts();
            if (guts == null) {
                return false;
            }
            this.mMetricsLogger.action(204);
            guts.setVisibility(4);
            guts.post(new Runnable() { // from class: com.android.systemui.statusbar.NotificationGutsManager.1
                @Override // java.lang.Runnable
                public void run() {
                    if (expandableNotificationRow.getWindowToken() == null) {
                        Log.e("NotificationGutsManager", "Trying to show notification guts in post(), but not attached to window");
                        return;
                    }
                    NotificationGutsManager.this.closeAndSaveGuts(true, true, true, -1, -1, false);
                    guts.setVisibility(0);
                    boolean z = NotificationGutsManager.this.mPresenter.isPresenterLocked() && !NotificationGutsManager.this.mAccessibilityManager.isTouchExplorationEnabled();
                    NotificationGuts notificationGuts = guts;
                    boolean z2 = !expandableNotificationRow.isBlockingHelperShowing();
                    int i3 = i;
                    int i4 = i2;
                    final ExpandableNotificationRow expandableNotificationRow2 = expandableNotificationRow;
                    Objects.requireNonNull(expandableNotificationRow2);
                    notificationGuts.openControls(z2, i3, i4, z, new Runnable() { // from class: com.android.systemui.statusbar.-$$Lambda$A0nRQ1BmXNDkvl3KYSKAx5O94nc
                        @Override // java.lang.Runnable
                        public final void run() {
                            ExpandableNotificationRow.this.onGutsOpened();
                        }
                    });
                    expandableNotificationRow.closeRemoteInput();
                    NotificationGutsManager.this.mListContainer.onHeightChanged(expandableNotificationRow, true);
                    NotificationGutsManager.this.mNotificationGutsExposed = guts;
                    NotificationGutsManager.this.mGutsMenuItem = menuItem;
                }
            });
            return true;
        }
        return false;
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("NotificationGutsManager state:");
        printWriter.print("  mKeyToRemoveOnGutsClosed: ");
        printWriter.println(this.mKeyToRemoveOnGutsClosed);
    }
}
