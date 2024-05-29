package com.android.systemui.statusbar.policy;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.BenesseExtension;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.android.internal.util.UserIcons;
import com.android.settingslib.RestrictedLockUtils;
import com.android.systemui.GuestResumeSessionReceiver;
import com.android.systemui.SystemUI;
import com.android.systemui.SystemUISecondaryUserService;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.tiles.UserDetailView;
import com.android.systemui.statusbar.phone.ActivityStarter;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
/* loaded from: a.zip:com/android/systemui/statusbar/policy/UserSwitcherController.class */
public class UserSwitcherController {
    private final ActivityStarter mActivityStarter;
    private Dialog mAddUserDialog;
    private boolean mAddUsersWhenLocked;
    private final Context mContext;
    private Dialog mExitGuestDialog;
    private final Handler mHandler;
    private final KeyguardMonitor mKeyguardMonitor;
    private boolean mPauseRefreshUsers;
    private PhoneStateListener mPhoneStateListener;
    private Intent mSecondaryUserServiceIntent;
    private boolean mSimpleUserSwitcher;
    private final UserManager mUserManager;
    private final ArrayList<WeakReference<BaseUserAdapter>> mAdapters = new ArrayList<>();
    private final GuestResumeSessionReceiver mGuestResumeSessionReceiver = new GuestResumeSessionReceiver();
    private ArrayList<UserRecord> mUsers = new ArrayList<>();
    private int mLastNonGuestUser = 0;
    private int mSecondaryUser = -10000;
    private SparseBooleanArray mForcePictureLoadForUserId = new SparseBooleanArray(2);
    private BroadcastReceiver mReceiver = new BroadcastReceiver(this) { // from class: com.android.systemui.statusbar.policy.UserSwitcherController.1
        final UserSwitcherController this$0;

        {
            this.this$0 = this;
        }

        private void showLogoutNotification(int i) {
            PendingIntent broadcastAsUser = PendingIntent.getBroadcastAsUser(this.this$0.mContext, 0, new Intent("com.android.systemui.LOGOUT_USER"), 0, UserHandle.SYSTEM);
            Notification.Builder addAction = new Notification.Builder(this.this$0.mContext).setVisibility(-1).setPriority(-2).setSmallIcon(2130837690).setContentTitle(this.this$0.mContext.getString(2131493641)).setContentText(this.this$0.mContext.getString(2131493642)).setContentIntent(broadcastAsUser).setOngoing(true).setShowWhen(false).addAction(2130837638, this.this$0.mContext.getString(2131493643), broadcastAsUser);
            SystemUI.overrideNotificationAppName(this.this$0.mContext, addAction);
            NotificationManager.from(this.this$0.mContext).notifyAsUser("logout_user", 1011, addAction.build(), new UserHandle(i));
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            int i;
            boolean z;
            int i2;
            if ("com.android.systemui.REMOVE_GUEST".equals(intent.getAction())) {
                int currentUser = ActivityManager.getCurrentUser();
                UserInfo userInfo = this.this$0.mUserManager.getUserInfo(currentUser);
                if (userInfo == null || !userInfo.isGuest()) {
                    return;
                }
                this.this$0.showExitGuestDialog(currentUser);
                return;
            }
            if ("com.android.systemui.LOGOUT_USER".equals(intent.getAction())) {
                this.this$0.logoutCurrentUser();
                z = false;
                i = -10000;
            } else if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                if (this.this$0.mExitGuestDialog != null && this.this$0.mExitGuestDialog.isShowing()) {
                    this.this$0.mExitGuestDialog.cancel();
                    this.this$0.mExitGuestDialog = null;
                }
                int intExtra = intent.getIntExtra("android.intent.extra.user_handle", -1);
                UserInfo userInfo2 = this.this$0.mUserManager.getUserInfo(intExtra);
                int size = this.this$0.mUsers.size();
                int i3 = 0;
                while (true) {
                    int i4 = i3;
                    if (i4 >= size) {
                        break;
                    }
                    UserRecord userRecord = (UserRecord) this.this$0.mUsers.get(i4);
                    if (userRecord.info == null) {
                        i2 = i4;
                    } else {
                        boolean z2 = userRecord.info.id == intExtra;
                        if (userRecord.isCurrent != z2) {
                            this.this$0.mUsers.set(i4, userRecord.copyWithIsCurrent(z2));
                        }
                        if (z2 && !userRecord.isGuest) {
                            this.this$0.mLastNonGuestUser = userRecord.info.id;
                        }
                        if (userInfo2 != null) {
                            i2 = i4;
                            if (userInfo2.isAdmin()) {
                            }
                        }
                        i2 = i4;
                        if (userRecord.isRestricted) {
                            this.this$0.mUsers.remove(i4);
                            i2 = i4 - 1;
                        }
                    }
                    i3 = i2 + 1;
                }
                this.this$0.notifyAdapters();
                if (this.this$0.mSecondaryUser != -10000) {
                    context.stopServiceAsUser(this.this$0.mSecondaryUserServiceIntent, UserHandle.of(this.this$0.mSecondaryUser));
                    this.this$0.mSecondaryUser = -10000;
                }
                if (userInfo2 != null && !userInfo2.isPrimary()) {
                    context.startServiceAsUser(this.this$0.mSecondaryUserServiceIntent, UserHandle.of(userInfo2.id));
                    this.this$0.mSecondaryUser = userInfo2.id;
                }
                if (UserManager.isSplitSystemUser() && userInfo2 != null && !userInfo2.isGuest() && userInfo2.id != 0) {
                    showLogoutNotification(intExtra);
                }
                if (userInfo2 != null && userInfo2.isGuest()) {
                    this.this$0.showGuestNotification(intExtra);
                }
                z = true;
                i = -10000;
            } else if ("android.intent.action.USER_INFO_CHANGED".equals(intent.getAction())) {
                i = intent.getIntExtra("android.intent.extra.user_handle", -10000);
                z = false;
            } else {
                i = -10000;
                z = false;
                if ("android.intent.action.USER_UNLOCKED".equals(intent.getAction())) {
                    i = -10000;
                    z = false;
                    if (intent.getIntExtra("android.intent.extra.user_handle", -10000) != 0) {
                        return;
                    }
                }
            }
            this.this$0.refreshUsers(i);
            if (z) {
                this.this$0.mUnpauseRefreshUsers.run();
            }
        }
    };
    private final Runnable mUnpauseRefreshUsers = new Runnable(this) { // from class: com.android.systemui.statusbar.policy.UserSwitcherController.2
        final UserSwitcherController this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.this$0.mHandler.removeCallbacks(this);
            this.this$0.mPauseRefreshUsers = false;
            this.this$0.refreshUsers(-10000);
        }
    };
    private final ContentObserver mSettingsObserver = new ContentObserver(this, new Handler()) { // from class: com.android.systemui.statusbar.policy.UserSwitcherController.3
        final UserSwitcherController this$0;

        {
            this.this$0 = this;
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            this.this$0.mSimpleUserSwitcher = Settings.Global.getInt(this.this$0.mContext.getContentResolver(), "lockscreenSimpleUserSwitcher", 0) != 0;
            this.this$0.mAddUsersWhenLocked = Settings.Global.getInt(this.this$0.mContext.getContentResolver(), "add_users_when_locked", 0) != 0;
            this.this$0.refreshUsers(-10000);
        }
    };
    public final QSTile.DetailAdapter userDetailAdapter = new QSTile.DetailAdapter(this) { // from class: com.android.systemui.statusbar.policy.UserSwitcherController.4
        private final Intent USER_SETTINGS_INTENT = new Intent("android.settings.USER_SETTINGS");
        final UserSwitcherController this$0;

        {
            this.this$0 = this;
        }

        @Override // com.android.systemui.qs.QSTile.DetailAdapter
        public View createDetailView(Context context, View view, ViewGroup viewGroup) {
            UserDetailView userDetailView;
            if (view instanceof UserDetailView) {
                userDetailView = (UserDetailView) view;
            } else {
                userDetailView = UserDetailView.inflate(context, viewGroup, false);
                userDetailView.createAndSetAdapter(this.this$0);
            }
            userDetailView.refreshAdapter();
            return userDetailView;
        }

        @Override // com.android.systemui.qs.QSTile.DetailAdapter
        public int getMetricsCategory() {
            return 125;
        }

        @Override // com.android.systemui.qs.QSTile.DetailAdapter
        public Intent getSettingsIntent() {
            if (BenesseExtension.getDchaState() != 0) {
                return null;
            }
            return this.USER_SETTINGS_INTENT;
        }

        @Override // com.android.systemui.qs.QSTile.DetailAdapter
        public CharSequence getTitle() {
            return this.this$0.mContext.getString(2131493546);
        }

        @Override // com.android.systemui.qs.QSTile.DetailAdapter
        public Boolean getToggleState() {
            return null;
        }

        @Override // com.android.systemui.qs.QSTile.DetailAdapter
        public void setToggleState(boolean z) {
        }
    };
    private final KeyguardMonitor.Callback mCallback = new KeyguardMonitor.Callback(this) { // from class: com.android.systemui.statusbar.policy.UserSwitcherController.5
        final UserSwitcherController this$0;

        {
            this.this$0 = this;
        }

        @Override // com.android.systemui.statusbar.policy.KeyguardMonitor.Callback
        public void onKeyguardChanged() {
            this.this$0.notifyAdapters();
        }
    };

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/statusbar/policy/UserSwitcherController$AddUserDialog.class */
    public final class AddUserDialog extends SystemUIDialog implements DialogInterface.OnClickListener {
        final UserSwitcherController this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public AddUserDialog(UserSwitcherController userSwitcherController, Context context) {
            super(context);
            this.this$0 = userSwitcherController;
            setTitle(2131493644);
            setMessage(context.getString(2131493645));
            setButton(-2, context.getString(17039360), this);
            setButton(-1, context.getString(17039370), this);
        }

        @Override // android.content.DialogInterface.OnClickListener
        public void onClick(DialogInterface dialogInterface, int i) {
            UserInfo createUser;
            if (i == -2) {
                cancel();
                return;
            }
            dismiss();
            if (ActivityManager.isUserAMonkey() || (createUser = this.this$0.mUserManager.createUser(this.this$0.mContext.getString(2131493627), 0)) == null) {
                return;
            }
            int i2 = createUser.id;
            this.this$0.mUserManager.setUserIcon(i2, UserIcons.convertToBitmap(UserIcons.getDefaultUserIcon(i2, false)));
            this.this$0.switchToUserId(i2);
        }
    }

    /* loaded from: a.zip:com/android/systemui/statusbar/policy/UserSwitcherController$BaseUserAdapter.class */
    public static abstract class BaseUserAdapter extends BaseAdapter {
        final UserSwitcherController mController;

        /* JADX INFO: Access modifiers changed from: protected */
        public BaseUserAdapter(UserSwitcherController userSwitcherController) {
            this.mController = userSwitcherController;
            userSwitcherController.mAdapters.add(new WeakReference(this));
        }

        @Override // android.widget.Adapter
        public int getCount() {
            boolean z = false;
            if (this.mController.mKeyguardMonitor.isShowing()) {
                z = false;
                if (this.mController.mKeyguardMonitor.isSecure()) {
                    z = !this.mController.mKeyguardMonitor.canSkipBouncer();
                }
            }
            if (z) {
                int size = this.mController.mUsers.size();
                int i = 0;
                for (int i2 = 0; i2 < size && !((UserRecord) this.mController.mUsers.get(i2)).isRestricted; i2++) {
                    i++;
                }
                return i;
            }
            return this.mController.mUsers.size();
        }

        public Drawable getDrawable(Context context, UserRecord userRecord) {
            return userRecord.isAddUser ? context.getDrawable(2130837621) : UserIcons.getDefaultUserIcon(userRecord.resolveId(), true);
        }

        @Override // android.widget.Adapter
        public UserRecord getItem(int i) {
            return (UserRecord) this.mController.mUsers.get(i);
        }

        @Override // android.widget.Adapter
        public long getItemId(int i) {
            return i;
        }

        public String getName(Context context, UserRecord userRecord) {
            if (!userRecord.isGuest) {
                return userRecord.isAddUser ? context.getString(2131493626) : userRecord.info.name;
            } else if (userRecord.isCurrent) {
                return context.getString(2131493630);
            } else {
                return context.getString(userRecord.info == null ? 2131493629 : 2131493628);
            }
        }

        public void refresh() {
            this.mController.refreshUsers(-10000);
        }

        public void switchTo(UserRecord userRecord) {
            this.mController.switchTo(userRecord);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/statusbar/policy/UserSwitcherController$ExitGuestDialog.class */
    public final class ExitGuestDialog extends SystemUIDialog implements DialogInterface.OnClickListener {
        private final int mGuestId;
        final UserSwitcherController this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public ExitGuestDialog(UserSwitcherController userSwitcherController, Context context, int i) {
            super(context);
            this.this$0 = userSwitcherController;
            setTitle(2131493631);
            setMessage(context.getString(2131493632));
            setButton(-2, context.getString(17039360), this);
            setButton(-1, context.getString(2131493633), this);
            setCanceledOnTouchOutside(false);
            this.mGuestId = i;
        }

        @Override // android.content.DialogInterface.OnClickListener
        public void onClick(DialogInterface dialogInterface, int i) {
            if (i == -2) {
                cancel();
                return;
            }
            dismiss();
            this.this$0.exitGuest(this.mGuestId);
        }
    }

    /* loaded from: a.zip:com/android/systemui/statusbar/policy/UserSwitcherController$UserRecord.class */
    public static final class UserRecord {
        public RestrictedLockUtils.EnforcedAdmin enforcedAdmin;
        public final UserInfo info;
        public final boolean isAddUser;
        public final boolean isCurrent;
        public boolean isDisabledByAdmin;
        public final boolean isGuest;
        public final boolean isRestricted;
        public boolean isSwitchToEnabled;
        public final Bitmap picture;

        public UserRecord(UserInfo userInfo, Bitmap bitmap, boolean z, boolean z2, boolean z3, boolean z4, boolean z5) {
            this.info = userInfo;
            this.picture = bitmap;
            this.isGuest = z;
            this.isCurrent = z2;
            this.isAddUser = z3;
            this.isRestricted = z4;
            this.isSwitchToEnabled = z5;
        }

        public UserRecord copyWithIsCurrent(boolean z) {
            return new UserRecord(this.info, this.picture, this.isGuest, z, this.isAddUser, this.isRestricted, this.isSwitchToEnabled);
        }

        public int resolveId() {
            if (this.isGuest || this.info == null) {
                return -10000;
            }
            return this.info.id;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("UserRecord(");
            if (this.info != null) {
                sb.append("name=\"").append(this.info.name).append("\" id=").append(this.info.id);
            } else if (this.isGuest) {
                sb.append("<add guest placeholder>");
            } else if (this.isAddUser) {
                sb.append("<add user placeholder>");
            }
            if (this.isGuest) {
                sb.append(" <isGuest>");
            }
            if (this.isAddUser) {
                sb.append(" <isAddUser>");
            }
            if (this.isCurrent) {
                sb.append(" <isCurrent>");
            }
            if (this.picture != null) {
                sb.append(" <hasPicture>");
            }
            if (this.isRestricted) {
                sb.append(" <isRestricted>");
            }
            if (this.isDisabledByAdmin) {
                sb.append(" <isDisabledByAdmin>");
                sb.append(" enforcedAdmin=").append(this.enforcedAdmin);
            }
            if (this.isSwitchToEnabled) {
                sb.append(" <isSwitchToEnabled>");
            }
            sb.append(')');
            return sb.toString();
        }
    }

    public UserSwitcherController(Context context, KeyguardMonitor keyguardMonitor, Handler handler, ActivityStarter activityStarter) {
        this.mContext = context;
        this.mGuestResumeSessionReceiver.register(context);
        this.mKeyguardMonitor = keyguardMonitor;
        this.mHandler = handler;
        this.mActivityStarter = activityStarter;
        this.mUserManager = UserManager.get(context);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_ADDED");
        intentFilter.addAction("android.intent.action.USER_REMOVED");
        intentFilter.addAction("android.intent.action.USER_INFO_CHANGED");
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        intentFilter.addAction("android.intent.action.USER_STOPPED");
        intentFilter.addAction("android.intent.action.USER_UNLOCKED");
        this.mContext.registerReceiverAsUser(this.mReceiver, UserHandle.SYSTEM, intentFilter, null, null);
        this.mSecondaryUserServiceIntent = new Intent(context, SystemUISecondaryUserService.class);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("com.android.systemui.REMOVE_GUEST");
        intentFilter2.addAction("com.android.systemui.LOGOUT_USER");
        this.mContext.registerReceiverAsUser(this.mReceiver, UserHandle.SYSTEM, intentFilter2, "com.android.systemui.permission.SELF", null);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("lockscreenSimpleUserSwitcher"), true, this.mSettingsObserver);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("add_users_when_locked"), true, this.mSettingsObserver);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("allow_user_switching_when_system_user_locked"), true, this.mSettingsObserver);
        this.mSettingsObserver.onChange(false);
        keyguardMonitor.addCallback(this.mCallback);
        listenForCallState();
        refreshUsers(-10000);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void checkIfAddUserDisallowedByAdminOnly(UserRecord userRecord) {
        RestrictedLockUtils.EnforcedAdmin checkIfRestrictionEnforced = RestrictedLockUtils.checkIfRestrictionEnforced(this.mContext, "no_add_user", ActivityManager.getCurrentUser());
        if (checkIfRestrictionEnforced == null || RestrictedLockUtils.hasBaseUserRestriction(this.mContext, "no_add_user", ActivityManager.getCurrentUser())) {
            userRecord.isDisabledByAdmin = false;
            userRecord.enforcedAdmin = null;
            return;
        }
        userRecord.isDisabledByAdmin = true;
        userRecord.enforcedAdmin = checkIfRestrictionEnforced;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void exitGuest(int i) {
        int i2 = 0;
        if (this.mLastNonGuestUser != 0) {
            UserInfo userInfo = this.mUserManager.getUserInfo(this.mLastNonGuestUser);
            i2 = 0;
            if (userInfo != null) {
                i2 = 0;
                if (userInfo.isEnabled()) {
                    i2 = 0;
                    if (userInfo.supportsSwitchToByUser()) {
                        i2 = userInfo.id;
                    }
                }
            }
        }
        switchToUserId(i2);
        this.mUserManager.removeUser(i);
    }

    private void listenForCallState() {
        TelephonyManager from = TelephonyManager.from(this.mContext);
        PhoneStateListener phoneStateListener = new PhoneStateListener(this) { // from class: com.android.systemui.statusbar.policy.UserSwitcherController.7
            private int mCallState;
            final UserSwitcherController this$0;

            {
                this.this$0 = this;
            }

            @Override // android.telephony.PhoneStateListener
            public void onCallStateChanged(int i, String str) {
                if (this.mCallState == i) {
                    return;
                }
                this.mCallState = i;
                int currentUser = ActivityManager.getCurrentUser();
                UserInfo userInfo = this.this$0.mUserManager.getUserInfo(currentUser);
                if (userInfo != null && userInfo.isGuest()) {
                    this.this$0.showGuestNotification(currentUser);
                }
                this.this$0.refreshUsers(-10000);
            }
        };
        this.mPhoneStateListener = phoneStateListener;
        from.listen(phoneStateListener, 32);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void notifyAdapters() {
        for (int size = this.mAdapters.size() - 1; size >= 0; size--) {
            BaseUserAdapter baseUserAdapter = this.mAdapters.get(size).get();
            if (baseUserAdapter != null) {
                baseUserAdapter.notifyDataSetChanged();
            } else {
                this.mAdapters.remove(size);
            }
        }
    }

    private void pauseRefreshUsers() {
        if (this.mPauseRefreshUsers) {
            return;
        }
        this.mHandler.postDelayed(this.mUnpauseRefreshUsers, 3000L);
        this.mPauseRefreshUsers = true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Type inference failed for: r0v14, types: [com.android.systemui.statusbar.policy.UserSwitcherController$6] */
    public void refreshUsers(int i) {
        if (i != -10000) {
            this.mForcePictureLoadForUserId.put(i, true);
        }
        if (this.mPauseRefreshUsers) {
            return;
        }
        boolean z = this.mForcePictureLoadForUserId.get(-1);
        SparseArray sparseArray = new SparseArray(this.mUsers.size());
        int size = this.mUsers.size();
        for (int i2 = 0; i2 < size; i2++) {
            UserRecord userRecord = this.mUsers.get(i2);
            if (userRecord != null && userRecord.picture != null && userRecord.info != null && !z && !this.mForcePictureLoadForUserId.get(userRecord.info.id)) {
                sparseArray.put(userRecord.info.id, userRecord.picture);
            }
        }
        this.mForcePictureLoadForUserId.clear();
        new AsyncTask<SparseArray<Bitmap>, Void, ArrayList<UserRecord>>(this, this.mAddUsersWhenLocked) { // from class: com.android.systemui.statusbar.policy.UserSwitcherController.6
            final UserSwitcherController this$0;
            final boolean val$addUsersWhenLocked;

            {
                this.this$0 = this;
                this.val$addUsersWhenLocked = r5;
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public ArrayList<UserRecord> doInBackground(SparseArray<Bitmap>... sparseArrayArr) {
                SparseArray<Bitmap> sparseArray2 = sparseArrayArr[0];
                List<UserInfo> users = this.this$0.mUserManager.getUsers(true);
                if (users == null) {
                    return null;
                }
                ArrayList<UserRecord> arrayList = new ArrayList<>(users.size());
                int currentUser = ActivityManager.getCurrentUser();
                boolean canSwitchUsers = this.this$0.mUserManager.canSwitchUsers();
                UserInfo userInfo = null;
                UserRecord userRecord2 = null;
                for (UserInfo userInfo2 : users) {
                    boolean z2 = currentUser == userInfo2.id;
                    UserInfo userInfo3 = userInfo;
                    if (z2) {
                        userInfo3 = userInfo2;
                    }
                    boolean z3 = !canSwitchUsers ? z2 : true;
                    userInfo = userInfo3;
                    if (userInfo2.isEnabled()) {
                        if (userInfo2.isGuest()) {
                            userRecord2 = new UserRecord(userInfo2, null, true, z2, false, false, canSwitchUsers);
                            userInfo = userInfo3;
                        } else {
                            userInfo = userInfo3;
                            if (userInfo2.supportsSwitchToByUser()) {
                                Bitmap bitmap = sparseArray2.get(userInfo2.id);
                                Bitmap bitmap2 = bitmap;
                                if (bitmap == null) {
                                    Bitmap userIcon = this.this$0.mUserManager.getUserIcon(userInfo2.id);
                                    bitmap2 = userIcon;
                                    if (userIcon != null) {
                                        int dimensionPixelSize = this.this$0.mContext.getResources().getDimensionPixelSize(2131689920);
                                        bitmap2 = Bitmap.createScaledBitmap(userIcon, dimensionPixelSize, dimensionPixelSize, true);
                                    }
                                }
                                arrayList.add(z2 ? 0 : arrayList.size(), new UserRecord(userInfo2, bitmap2, false, z2, false, false, z3));
                                userInfo = userInfo3;
                            }
                        }
                    }
                }
                boolean z4 = !this.this$0.mUserManager.hasBaseUserRestriction("no_add_user", UserHandle.SYSTEM);
                boolean z5 = (userInfo == null || !(userInfo.isAdmin() || userInfo.id == 0)) ? false : z4;
                boolean z6 = z4 ? this.val$addUsersWhenLocked : false;
                boolean z7 = (z5 || z6) ? userRecord2 == null : false;
                boolean canAddMoreUsers = (z5 || z6) ? this.this$0.mUserManager.canAddMoreUsers() : false;
                boolean z8 = !this.val$addUsersWhenLocked;
                if (!this.this$0.mSimpleUserSwitcher) {
                    if (userRecord2 != null) {
                        arrayList.add(userRecord2.isCurrent ? 0 : arrayList.size(), userRecord2);
                    } else if (z7) {
                        UserRecord userRecord3 = new UserRecord(null, null, true, false, false, z8, canSwitchUsers);
                        this.this$0.checkIfAddUserDisallowedByAdminOnly(userRecord3);
                        arrayList.add(userRecord3);
                    }
                }
                if (!this.this$0.mSimpleUserSwitcher && canAddMoreUsers) {
                    UserRecord userRecord4 = new UserRecord(null, null, false, false, true, z8, canSwitchUsers);
                    this.this$0.checkIfAddUserDisallowedByAdminOnly(userRecord4);
                    arrayList.add(userRecord4);
                }
                return arrayList;
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public void onPostExecute(ArrayList<UserRecord> arrayList) {
                if (arrayList != null) {
                    this.this$0.mUsers = arrayList;
                    this.this$0.notifyAdapters();
                }
            }
        }.execute(sparseArray);
    }

    private void showAddUserDialog() {
        if (this.mAddUserDialog != null && this.mAddUserDialog.isShowing()) {
            this.mAddUserDialog.cancel();
        }
        this.mAddUserDialog = new AddUserDialog(this, this.mContext);
        this.mAddUserDialog.show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showExitGuestDialog(int i) {
        if (this.mExitGuestDialog != null && this.mExitGuestDialog.isShowing()) {
            this.mExitGuestDialog.cancel();
        }
        this.mExitGuestDialog = new ExitGuestDialog(this, this.mContext, i);
        this.mExitGuestDialog.show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showGuestNotification(int i) {
        PendingIntent broadcastAsUser = this.mUserManager.canSwitchUsers() ? PendingIntent.getBroadcastAsUser(this.mContext, 0, new Intent("com.android.systemui.REMOVE_GUEST"), 0, UserHandle.SYSTEM) : null;
        Notification.Builder addAction = new Notification.Builder(this.mContext).setVisibility(-1).setPriority(-2).setSmallIcon(2130837690).setContentTitle(this.mContext.getString(2131493638)).setContentText(this.mContext.getString(2131493639)).setContentIntent(broadcastAsUser).setShowWhen(false).addAction(2130837638, this.mContext.getString(2131493640), broadcastAsUser);
        SystemUI.overrideNotificationAppName(this.mContext, addAction);
        NotificationManager.from(this.mContext).notifyAsUser("remove_guest", 1010, addAction.build(), new UserHandle(i));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void switchToUserId(int i) {
        try {
            pauseRefreshUsers();
            ActivityManagerNative.getDefault().switchUser(i);
        } catch (RemoteException e) {
            Log.e("UserSwitcherController", "Couldn't switch user.", e);
        }
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("UserSwitcherController state:");
        printWriter.println("  mLastNonGuestUser=" + this.mLastNonGuestUser);
        printWriter.print("  mUsers.size=");
        printWriter.println(this.mUsers.size());
        for (int i = 0; i < this.mUsers.size(); i++) {
            printWriter.print("    ");
            printWriter.println(this.mUsers.get(i).toString());
        }
    }

    public String getCurrentUserName(Context context) {
        UserRecord userRecord;
        if (this.mUsers.isEmpty() || (userRecord = this.mUsers.get(0)) == null || userRecord.info == null) {
            return null;
        }
        return userRecord.isGuest ? context.getString(2131493628) : userRecord.info.name;
    }

    public boolean isSimpleUserSwitcher() {
        return this.mSimpleUserSwitcher;
    }

    public void logoutCurrentUser() {
        if (ActivityManager.getCurrentUser() != 0) {
            pauseRefreshUsers();
            ActivityManager.logoutCurrentUser();
        }
    }

    public void onDensityOrFontScaleChanged() {
        refreshUsers(-1);
    }

    public void removeUserId(int i) {
        if (i == 0) {
            Log.w("UserSwitcherController", "User " + i + " could not removed.");
            return;
        }
        if (ActivityManager.getCurrentUser() == i) {
            switchToUserId(0);
        }
        if (this.mUserManager.removeUser(i)) {
            refreshUsers(-10000);
        }
    }

    public void startActivity(Intent intent) {
        this.mActivityStarter.startActivity(intent, true);
    }

    public void switchTo(UserRecord userRecord) {
        int i;
        if (userRecord.isGuest && userRecord.info == null) {
            UserInfo createGuest = this.mUserManager.createGuest(this.mContext, this.mContext.getString(2131493628));
            if (createGuest == null) {
                return;
            }
            i = createGuest.id;
        } else if (userRecord.isAddUser) {
            showAddUserDialog();
            return;
        } else {
            i = userRecord.info.id;
        }
        if (ActivityManager.getCurrentUser() != i) {
            switchToUserId(i);
        } else if (userRecord.isGuest) {
            showExitGuestDialog(i);
        }
    }

    public boolean useFullscreenUserSwitcher() {
        boolean z = false;
        int i = Settings.System.getInt(this.mContext.getContentResolver(), "enable_fullscreen_user_switcher", -1);
        if (i != -1) {
            if (i != 0) {
                z = true;
            }
            return z;
        }
        return this.mContext.getResources().getBoolean(2131623966);
    }
}
