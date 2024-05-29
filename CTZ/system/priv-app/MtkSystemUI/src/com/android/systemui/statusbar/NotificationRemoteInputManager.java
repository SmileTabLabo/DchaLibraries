package com.android.systemui.statusbar;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserManager;
import android.service.notification.StatusBarNotification;
import android.util.ArraySet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RemoteViews;
import android.widget.TextView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.statusbar.NotificationVisibility;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.RemoteInputController;
import com.android.systemui.statusbar.policy.RemoteInputView;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Set;
/* loaded from: classes.dex */
public class NotificationRemoteInputManager implements Dumpable {
    public static final boolean ENABLE_REMOTE_INPUT = SystemProperties.getBoolean("debug.enable_remote_input", true);
    public static final boolean FORCE_REMOTE_INPUT_HISTORY = SystemProperties.getBoolean("debug.force_remoteinput_history", true);
    protected Callback mCallback;
    protected final Context mContext;
    protected NotificationEntryManager mEntryManager;
    protected NotificationPresenter mPresenter;
    protected RemoteInputController mRemoteInputController;
    private final UserManager mUserManager;
    protected final ArraySet<NotificationData.Entry> mRemoteInputEntriesToRemoveOnCollapse = new ArraySet<>();
    protected final NotificationLockscreenUserManager mLockscreenUserManager = (NotificationLockscreenUserManager) Dependency.get(NotificationLockscreenUserManager.class);
    private final RemoteViews.OnClickHandler mOnClickHandler = new AnonymousClass1();
    protected IStatusBarService mBarService = IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"));

    /* loaded from: classes.dex */
    public interface Callback {
        boolean handleRemoteViewClick(View view, PendingIntent pendingIntent, Intent intent, ClickHandler clickHandler);

        void onLockedRemoteInput(ExpandableNotificationRow expandableNotificationRow, View view);

        void onLockedWorkRemoteInput(int i, ExpandableNotificationRow expandableNotificationRow, View view);

        void onMakeExpandedVisibleForRemoteInput(ExpandableNotificationRow expandableNotificationRow, View view);

        boolean shouldHandleRemoteInput(View view, PendingIntent pendingIntent);
    }

    /* loaded from: classes.dex */
    public interface ClickHandler {
        boolean handleClick();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.android.systemui.statusbar.NotificationRemoteInputManager$1  reason: invalid class name */
    /* loaded from: classes.dex */
    public class AnonymousClass1 extends RemoteViews.OnClickHandler {
        AnonymousClass1() {
        }

        public boolean onClickHandler(final View view, final PendingIntent pendingIntent, final Intent intent) {
            NotificationRemoteInputManager.this.mPresenter.wakeUpIfDozing(SystemClock.uptimeMillis(), view);
            if (handleRemoteInput(view, pendingIntent)) {
                return true;
            }
            logActionClick(view);
            try {
                ActivityManager.getService().resumeAppSwitches();
            } catch (RemoteException e) {
            }
            return NotificationRemoteInputManager.this.mCallback.handleRemoteViewClick(view, pendingIntent, intent, new ClickHandler() { // from class: com.android.systemui.statusbar.-$$Lambda$NotificationRemoteInputManager$1$dzxY8UkafLAnu6kZIoh3HbriWqA
                @Override // com.android.systemui.statusbar.NotificationRemoteInputManager.ClickHandler
                public final boolean handleClick() {
                    boolean superOnClickHandler;
                    superOnClickHandler = NotificationRemoteInputManager.AnonymousClass1.this.superOnClickHandler(view, pendingIntent, intent);
                    return superOnClickHandler;
                }
            });
        }

        private void logActionClick(View view) {
            ViewParent parent = view.getParent();
            String notificationKeyForParent = getNotificationKeyForParent(parent);
            if (notificationKeyForParent == null) {
                Log.w("NotificationRemoteInputManager", "Couldn't determine notification for click.");
                return;
            }
            int i = -1;
            if (view.getId() == 16908661 && parent != null && (parent instanceof ViewGroup)) {
                i = ((ViewGroup) parent).indexOfChild(view);
            }
            try {
                NotificationRemoteInputManager.this.mBarService.onNotificationActionClick(notificationKeyForParent, i, NotificationVisibility.obtain(notificationKeyForParent, NotificationRemoteInputManager.this.mEntryManager.getNotificationData().getRank(notificationKeyForParent), NotificationRemoteInputManager.this.mEntryManager.getNotificationData().getActiveNotifications().size(), true));
            } catch (RemoteException e) {
            }
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

        /* JADX INFO: Access modifiers changed from: private */
        public boolean superOnClickHandler(View view, PendingIntent pendingIntent, Intent intent) {
            return super.onClickHandler(view, pendingIntent, intent, 4);
        }

        private boolean handleRemoteInput(View view, PendingIntent pendingIntent) {
            RemoteInput[] remoteInputArr;
            RemoteInputView remoteInputView;
            if (NotificationRemoteInputManager.this.mCallback.shouldHandleRemoteInput(view, pendingIntent)) {
                return true;
            }
            Object tag = view.getTag(16909223);
            ExpandableNotificationRow expandableNotificationRow = null;
            if (tag instanceof RemoteInput[]) {
                remoteInputArr = (RemoteInput[]) tag;
            } else {
                remoteInputArr = null;
            }
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
                if (parent != null) {
                    if (parent instanceof View) {
                        View view2 = (View) parent;
                        if (view2.isRootNamespace()) {
                            remoteInputView = findRemoteInputView(view2);
                            break;
                        }
                    }
                    parent = parent.getParent();
                } else {
                    remoteInputView = null;
                    break;
                }
            }
            while (true) {
                if (parent == null) {
                    break;
                } else if (parent instanceof ExpandableNotificationRow) {
                    expandableNotificationRow = (ExpandableNotificationRow) parent;
                    break;
                } else {
                    parent = parent.getParent();
                }
            }
            if (expandableNotificationRow == null) {
                return false;
            }
            expandableNotificationRow.setUserExpanded(true);
            if (!NotificationRemoteInputManager.this.mLockscreenUserManager.shouldAllowLockscreenRemoteInput()) {
                int identifier = pendingIntent.getCreatorUserHandle().getIdentifier();
                if (!NotificationRemoteInputManager.this.mLockscreenUserManager.isLockscreenPublicMode(identifier)) {
                    if (NotificationRemoteInputManager.this.mUserManager.getUserInfo(identifier).isManagedProfile() && NotificationRemoteInputManager.this.mPresenter.isDeviceLocked(identifier)) {
                        NotificationRemoteInputManager.this.mCallback.onLockedWorkRemoteInput(identifier, expandableNotificationRow, view);
                        return true;
                    }
                } else {
                    NotificationRemoteInputManager.this.mCallback.onLockedRemoteInput(expandableNotificationRow, view);
                    return true;
                }
            }
            if (remoteInputView == null) {
                remoteInputView = findRemoteInputView(expandableNotificationRow.getPrivateLayout().getExpandedChild());
                if (remoteInputView == null) {
                    return false;
                }
                if (!expandableNotificationRow.getPrivateLayout().getExpandedChild().isShown()) {
                    NotificationRemoteInputManager.this.mCallback.onMakeExpandedVisibleForRemoteInput(expandableNotificationRow, view);
                    return true;
                }
            }
            int width = view.getWidth();
            if (view instanceof TextView) {
                TextView textView = (TextView) view;
                if (textView.getLayout() != null) {
                    width = Math.min(width, ((int) textView.getLayout().getLineWidth(0)) + textView.getCompoundPaddingLeft() + textView.getCompoundPaddingRight());
                }
            }
            int left = view.getLeft() + (width / 2);
            int top = view.getTop() + (view.getHeight() / 2);
            int width2 = remoteInputView.getWidth();
            int height = remoteInputView.getHeight() - top;
            int i = width2 - left;
            remoteInputView.setRevealParameters(left, top, Math.max(Math.max(left + top, left + height), Math.max(i + top, i + height)));
            remoteInputView.setPendingIntent(pendingIntent);
            remoteInputView.setRemoteInput(remoteInputArr, remoteInput);
            remoteInputView.focusAnimated();
            return true;
        }

        private RemoteInputView findRemoteInputView(View view) {
            if (view == null) {
                return null;
            }
            return (RemoteInputView) view.findViewWithTag(RemoteInputView.VIEW_TAG);
        }
    }

    public NotificationRemoteInputManager(Context context) {
        this.mContext = context;
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
    }

    public void setUpWithPresenter(NotificationPresenter notificationPresenter, NotificationEntryManager notificationEntryManager, Callback callback, RemoteInputController.Delegate delegate) {
        this.mPresenter = notificationPresenter;
        this.mEntryManager = notificationEntryManager;
        this.mCallback = callback;
        this.mRemoteInputController = new RemoteInputController(delegate);
        this.mRemoteInputController.addCallback(new AnonymousClass2());
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.android.systemui.statusbar.NotificationRemoteInputManager$2  reason: invalid class name */
    /* loaded from: classes.dex */
    public class AnonymousClass2 implements RemoteInputController.Callback {
        AnonymousClass2() {
        }

        @Override // com.android.systemui.statusbar.RemoteInputController.Callback
        public void onRemoteInputSent(final NotificationData.Entry entry) {
            if (NotificationRemoteInputManager.FORCE_REMOTE_INPUT_HISTORY && NotificationRemoteInputManager.this.mEntryManager.isNotificationKeptForRemoteInput(entry.key)) {
                NotificationRemoteInputManager.this.mEntryManager.removeNotification(entry.key, null);
            } else if (NotificationRemoteInputManager.this.mRemoteInputEntriesToRemoveOnCollapse.contains(entry)) {
                NotificationRemoteInputManager.this.mPresenter.getHandler().postDelayed(new Runnable() { // from class: com.android.systemui.statusbar.-$$Lambda$NotificationRemoteInputManager$2$upPlaSspsvwkYecy75h02dyePMo
                    @Override // java.lang.Runnable
                    public final void run() {
                        NotificationRemoteInputManager.AnonymousClass2.lambda$onRemoteInputSent$0(NotificationRemoteInputManager.AnonymousClass2.this, entry);
                    }
                }, 200L);
            }
            try {
                NotificationRemoteInputManager.this.mBarService.onNotificationDirectReplied(entry.notification.getKey());
            } catch (RemoteException e) {
            }
        }

        public static /* synthetic */ void lambda$onRemoteInputSent$0(AnonymousClass2 anonymousClass2, NotificationData.Entry entry) {
            if (NotificationRemoteInputManager.this.mRemoteInputEntriesToRemoveOnCollapse.remove(entry)) {
                NotificationRemoteInputManager.this.mEntryManager.removeNotification(entry.key, null);
            }
        }
    }

    public RemoteInputController getController() {
        return this.mRemoteInputController;
    }

    public void onUpdateNotification(NotificationData.Entry entry) {
        this.mRemoteInputEntriesToRemoveOnCollapse.remove(entry);
    }

    public boolean onRemoveNotification(NotificationData.Entry entry) {
        if (entry != null && this.mRemoteInputController.isRemoteInputActive(entry) && entry.row != null && !entry.row.isDismissed()) {
            this.mRemoteInputEntriesToRemoveOnCollapse.add(entry);
            return true;
        }
        return false;
    }

    public void onPerformRemoveNotification(StatusBarNotification statusBarNotification, NotificationData.Entry entry) {
        if (this.mRemoteInputController.isRemoteInputActive(entry)) {
            this.mRemoteInputController.removeRemoteInput(entry, null);
        }
    }

    public void removeRemoteInputEntriesKeptUntilCollapsed() {
        for (int i = 0; i < this.mRemoteInputEntriesToRemoveOnCollapse.size(); i++) {
            NotificationData.Entry valueAt = this.mRemoteInputEntriesToRemoveOnCollapse.valueAt(i);
            this.mRemoteInputController.removeRemoteInput(valueAt, null);
            this.mEntryManager.removeNotification(valueAt.key, this.mEntryManager.getLatestRankingMap());
        }
        this.mRemoteInputEntriesToRemoveOnCollapse.clear();
    }

    public void checkRemoteInputOutside(MotionEvent motionEvent) {
        if (motionEvent.getAction() == 4 && motionEvent.getX() == 0.0f && motionEvent.getY() == 0.0f && this.mRemoteInputController.isRemoteInputActive()) {
            this.mRemoteInputController.closeRemoteInputs();
        }
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("NotificationRemoteInputManager state:");
        printWriter.print("  mRemoteInputEntriesToRemoveOnCollapse: ");
        printWriter.println(this.mRemoteInputEntriesToRemoveOnCollapse);
    }

    public void bindRow(ExpandableNotificationRow expandableNotificationRow) {
        expandableNotificationRow.setRemoteInputController(this.mRemoteInputController);
        expandableNotificationRow.setRemoteViewClickHandler(this.mOnClickHandler);
    }

    @VisibleForTesting
    public Set<NotificationData.Entry> getRemoteInputEntriesToRemoveOnCollapse() {
        return this.mRemoteInputEntriesToRemoveOnCollapse;
    }
}
