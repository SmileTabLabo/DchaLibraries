package com.android.systemui.statusbar.policy;

import android.app.ActivityManagerNative;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.Pair;
import com.android.internal.util.UserIcons;
import com.android.settingslib.drawable.UserIconDrawable;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/systemui/statusbar/policy/UserInfoController.class */
public final class UserInfoController {
    private final Context mContext;
    private Drawable mUserDrawable;
    private AsyncTask<Void, Void, Pair<String, Drawable>> mUserInfoTask;
    private String mUserName;
    private final ArrayList<OnUserInfoChangedListener> mCallbacks = new ArrayList<>();
    private final BroadcastReceiver mReceiver = new BroadcastReceiver(this) { // from class: com.android.systemui.statusbar.policy.UserInfoController.1
        final UserInfoController this$0;

        {
            this.this$0 = this;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                this.this$0.reloadUserInfo();
            }
        }
    };
    private final BroadcastReceiver mProfileReceiver = new BroadcastReceiver(this) { // from class: com.android.systemui.statusbar.policy.UserInfoController.2
        final UserInfoController this$0;

        {
            this.this$0 = this;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.provider.Contacts.PROFILE_CHANGED".equals(action) || "android.intent.action.USER_INFO_CHANGED".equals(action)) {
                try {
                    if (intent.getIntExtra("android.intent.extra.user_handle", getSendingUserId()) == ActivityManagerNative.getDefault().getCurrentUser().id) {
                        this.this$0.reloadUserInfo();
                    }
                } catch (RemoteException e) {
                    Log.e("UserInfoController", "Couldn't get current user id for profile change", e);
                }
            }
        }
    };

    /* loaded from: a.zip:com/android/systemui/statusbar/policy/UserInfoController$OnUserInfoChangedListener.class */
    public interface OnUserInfoChangedListener {
        void onUserInfoChanged(String str, Drawable drawable);
    }

    public UserInfoController(Context context) {
        this.mContext = context;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        this.mContext.registerReceiver(this.mReceiver, intentFilter);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("android.provider.Contacts.PROFILE_CHANGED");
        intentFilter2.addAction("android.intent.action.USER_INFO_CHANGED");
        this.mContext.registerReceiverAsUser(this.mProfileReceiver, UserHandle.ALL, intentFilter2, null, null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void notifyChanged() {
        for (OnUserInfoChangedListener onUserInfoChangedListener : this.mCallbacks) {
            onUserInfoChangedListener.onUserInfoChanged(this.mUserName, this.mUserDrawable);
        }
    }

    private void queryForUserInformation() {
        try {
            UserInfo currentUser = ActivityManagerNative.getDefault().getCurrentUser();
            Context createPackageContextAsUser = this.mContext.createPackageContextAsUser("android", 0, new UserHandle(currentUser.id));
            int i = currentUser.id;
            boolean isGuest = currentUser.isGuest();
            String str = currentUser.name;
            Resources resources = this.mContext.getResources();
            this.mUserInfoTask = new AsyncTask<Void, Void, Pair<String, Drawable>>(this, str, i, Math.max(resources.getDimensionPixelSize(2131689913), resources.getDimensionPixelSize(2131689912)), isGuest, createPackageContextAsUser) { // from class: com.android.systemui.statusbar.policy.UserInfoController.3
                final UserInfoController this$0;
                final int val$avatarSize;
                final Context val$context;
                final boolean val$isGuest;
                final int val$userId;
                final String val$userName;

                {
                    this.this$0 = this;
                    this.val$userName = str;
                    this.val$userId = i;
                    this.val$avatarSize = r7;
                    this.val$isGuest = isGuest;
                    this.val$context = createPackageContextAsUser;
                }

                /* JADX INFO: Access modifiers changed from: protected */
                @Override // android.os.AsyncTask
                public Pair<String, Drawable> doInBackground(Void... voidArr) {
                    UserIconDrawable defaultUserIcon;
                    UserManager userManager = UserManager.get(this.this$0.mContext);
                    String str2 = this.val$userName;
                    Bitmap userIcon = userManager.getUserIcon(this.val$userId);
                    Log.d("UserInfoController", "rawAvatar = " + userIcon);
                    if (userIcon != null) {
                        defaultUserIcon = new UserIconDrawable(this.val$avatarSize).setIcon(userIcon).setBadgeIfManagedUser(this.this$0.mContext, this.val$userId).bake();
                    } else {
                        defaultUserIcon = UserIcons.getDefaultUserIcon(this.val$isGuest ? -10000 : this.val$userId, true);
                    }
                    String str3 = str2;
                    if (userManager.getUsers().size() <= 1) {
                        Cursor query = this.val$context.getContentResolver().query(ContactsContract.Profile.CONTENT_URI, new String[]{"_id", "display_name"}, null, null, null);
                        str3 = str2;
                        if (query != null) {
                            try {
                                if (query.moveToFirst()) {
                                    str2 = query.getString(query.getColumnIndex("display_name"));
                                }
                                query.close();
                                str3 = str2;
                            } catch (Throwable th) {
                                query.close();
                                throw th;
                            }
                        }
                    }
                    return new Pair<>(str3, defaultUserIcon);
                }

                /* JADX INFO: Access modifiers changed from: protected */
                @Override // android.os.AsyncTask
                public void onPostExecute(Pair<String, Drawable> pair) {
                    this.this$0.mUserName = (String) pair.first;
                    this.this$0.mUserDrawable = (Drawable) pair.second;
                    this.this$0.mUserInfoTask = null;
                    this.this$0.notifyChanged();
                }
            };
            this.mUserInfoTask.execute(new Void[0]);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("UserInfoController", "Couldn't create user context", e);
            throw new RuntimeException(e);
        } catch (RemoteException e2) {
            Log.e("UserInfoController", "Couldn't get user info", e2);
            throw new RuntimeException(e2);
        }
    }

    public void addListener(OnUserInfoChangedListener onUserInfoChangedListener) {
        this.mCallbacks.add(onUserInfoChangedListener);
        onUserInfoChangedListener.onUserInfoChanged(this.mUserName, this.mUserDrawable);
    }

    public void onDensityOrFontScaleChanged() {
        reloadUserInfo();
    }

    public void reloadUserInfo() {
        if (this.mUserInfoTask != null) {
            this.mUserInfoTask.cancel(false);
            this.mUserInfoTask = null;
        }
        queryForUserInformation();
    }

    public void remListener(OnUserInfoChangedListener onUserInfoChangedListener) {
        this.mCallbacks.remove(onUserInfoChangedListener);
    }
}
