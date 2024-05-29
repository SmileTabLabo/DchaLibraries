package com.android.settings.users;

import android.app.Activity;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SimpleAdapter;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.DimmableIconPreference;
import com.android.settings.OwnerInfoSettings;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settings.users.EditUserInfoController;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedSwitchPreference;
import com.android.settingslib.drawable.CircleFramedDrawable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
/* loaded from: classes.dex */
public class UserSettings extends SettingsPreferenceFragment implements Preference.OnPreferenceClickListener, View.OnClickListener, DialogInterface.OnDismissListener, Preference.OnPreferenceChangeListener, EditUserInfoController.OnContentChangedCallback, Indexable {
    private DimmableIconPreference mAddUser;
    private RestrictedSwitchPreference mAddUserWhenLocked;
    private boolean mAddingUser;
    private String mAddingUserName;
    private Drawable mDefaultIconDrawable;
    private ProgressDialog mDeletingUserDialog;
    private Preference mEmergencyInfoPreference;
    private PreferenceGroup mLockScreenSettings;
    private UserPreference mMePreference;
    private UserCapabilities mUserCaps;
    private PreferenceGroup mUserListCategory;
    private UserManager mUserManager;
    public static final SummaryLoader.SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = new SummaryLoader.SummaryProviderFactory() { // from class: com.android.settings.users.UserSettings.3
        @Override // com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory
        public SummaryLoader.SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader);
        }
    };
    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() { // from class: com.android.settings.users.UserSettings.4
        @Override // com.android.settings.search.BaseSearchIndexProvider, com.android.settings.search.Indexable.SearchIndexProvider
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            int i;
            List<SearchIndexableRaw> result = new ArrayList<>();
            UserCapabilities userCaps = UserCapabilities.create(context);
            if (!userCaps.mEnabled) {
                return result;
            }
            Resources res = context.getResources();
            SearchIndexableRaw data = new SearchIndexableRaw(context);
            data.title = res.getString(R.string.user_settings_title);
            data.screenTitle = res.getString(R.string.user_settings_title);
            result.add(data);
            if (userCaps.mCanAddUser || userCaps.mDisallowAddUserSetByAdmin) {
                SearchIndexableRaw data2 = new SearchIndexableRaw(context);
                if (userCaps.mCanAddRestrictedProfile) {
                    i = R.string.user_add_user_or_profile_menu;
                } else {
                    i = R.string.user_add_user_menu;
                }
                data2.title = res.getString(i);
                data2.screenTitle = res.getString(R.string.user_settings_title);
                result.add(data2);
            }
            return result;
        }
    };
    private int mRemovingUserId = -1;
    private int mAddedUserId = 0;
    private boolean mShouldUpdateUserList = true;
    private final Object mUserLock = new Object();
    private SparseArray<Bitmap> mUserIcons = new SparseArray<>();
    private EditUserInfoController mEditUserInfoController = new EditUserInfoController();
    private boolean mUpdateUserListOperate = false;
    private Handler mHandler = new Handler() { // from class: com.android.settings.users.UserSettings.1
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    UserSettings.this.updateUserList();
                    return;
                case 2:
                    UserSettings.this.onUserCreated(msg.arg1);
                    return;
                case 3:
                    UserSettings.this.onManageUserClicked(msg.arg1, true);
                    return;
                default:
                    return;
            }
        }
    };
    private BroadcastReceiver mUserChangeReceiver = new BroadcastReceiver() { // from class: com.android.settings.users.UserSettings.2
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            int userHandle;
            if (intent.getAction().equals("android.intent.action.USER_REMOVED")) {
                UserSettings.this.dismissDeleteUserDialog();
                UserSettings.this.mRemovingUserId = -1;
            } else if (intent.getAction().equals("android.intent.action.USER_INFO_CHANGED") && (userHandle = intent.getIntExtra("android.intent.extra.user_handle", -1)) != -1) {
                UserSettings.this.mUserIcons.remove(userHandle);
            }
            UserSettings.this.mHandler.sendEmptyMessage(1);
        }
    };

    @Override // com.android.settings.InstrumentedPreferenceFragment
    protected int getMetricsCategory() {
        return 96;
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (icicle != null) {
            if (icicle.containsKey("adding_user")) {
                this.mAddedUserId = icicle.getInt("adding_user");
            }
            if (icicle.containsKey("removing_user")) {
                this.mRemovingUserId = icicle.getInt("removing_user");
            }
            this.mEditUserInfoController.onRestoreInstanceState(icicle);
        }
        Context context = getActivity();
        this.mUserCaps = UserCapabilities.create(context);
        this.mUserManager = (UserManager) context.getSystemService("user");
        if (!this.mUserCaps.mEnabled) {
            return;
        }
        int myUserId = UserHandle.myUserId();
        addPreferencesFromResource(R.xml.user_settings);
        this.mUserListCategory = (PreferenceGroup) findPreference("user_list");
        this.mMePreference = new UserPreference(getPrefContext(), null, myUserId, null, null);
        this.mMePreference.setKey("user_me");
        this.mMePreference.setOnPreferenceClickListener(this);
        if (this.mUserCaps.mIsAdmin) {
            this.mMePreference.setSummary(R.string.user_admin);
        }
        this.mAddUser = (DimmableIconPreference) findPreference("user_add");
        if (this.mUserCaps.mCanAddUser && Utils.isDeviceProvisioned(getActivity())) {
            this.mAddUser.setOnPreferenceClickListener(this);
            if (!this.mUserCaps.mCanAddRestrictedProfile) {
                this.mAddUser.setTitle(R.string.user_add_user_menu);
            }
        }
        this.mLockScreenSettings = (PreferenceGroup) findPreference("lock_screen_settings");
        this.mAddUserWhenLocked = (RestrictedSwitchPreference) findPreference("add_users_when_locked");
        this.mEmergencyInfoPreference = findPreference("emergency_info");
        setHasOptionsMenu(true);
        IntentFilter filter = new IntentFilter("android.intent.action.USER_REMOVED");
        filter.addAction("android.intent.action.USER_INFO_CHANGED");
        context.registerReceiverAsUser(this.mUserChangeReceiver, UserHandle.ALL, filter, null, this.mHandler);
        loadProfile();
        updateUserList();
        this.mShouldUpdateUserList = false;
        if (Settings.Global.getInt(getContext().getContentResolver(), "device_provisioned", 0) != 0) {
            return;
        }
        getActivity().finish();
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        if (!this.mUserCaps.mEnabled || !this.mShouldUpdateUserList) {
            return;
        }
        this.mUserCaps.updateAddUserCapabilities(getActivity());
        loadProfile();
        updateUserList();
    }

    @Override // com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onPause() {
        this.mShouldUpdateUserList = true;
        super.onPause();
    }

    @Override // android.app.Fragment
    public void onDestroy() {
        super.onDestroy();
        if (this.mUserCaps.mEnabled) {
            getActivity().unregisterReceiver(this.mUserChangeReceiver);
        }
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        this.mEditUserInfoController.onSaveInstanceState(outState);
        outState.putInt("adding_user", this.mAddedUserId);
        outState.putInt("removing_user", this.mRemovingUserId);
    }

    @Override // android.app.Fragment
    public void startActivityForResult(Intent intent, int requestCode) {
        this.mEditUserInfoController.startingActivityForResult();
        super.startActivityForResult(intent, requestCode);
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.app.Fragment
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        UserManager um = (UserManager) getContext().getSystemService(UserManager.class);
        boolean allowRemoveUser = !um.hasUserRestriction("no_remove_user");
        boolean canSwitchUsers = um.canSwitchUsers();
        if (!this.mUserCaps.mIsAdmin && allowRemoveUser && canSwitchUsers) {
            String nickname = this.mUserManager.getUserName();
            MenuItem removeThisUser = menu.add(0, 1, 0, getResources().getString(R.string.user_remove_user_menu, nickname));
            removeThisUser.setShowAsAction(0);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override // android.app.Fragment
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == 1) {
            onRemoveUserClicked(UserHandle.myUserId());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /* JADX WARN: Type inference failed for: r0v2, types: [com.android.settings.users.UserSettings$5] */
    private void loadProfile() {
        if (this.mUserCaps.mIsGuest) {
            this.mMePreference.setIcon(getEncircledDefaultIcon());
            this.mMePreference.setTitle(R.string.user_exit_guest_title);
            return;
        }
        new AsyncTask<Void, Void, String>() { // from class: com.android.settings.users.UserSettings.5
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public void onPostExecute(String result) {
                UserSettings.this.finishLoadProfile(result);
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public String doInBackground(Void... values) {
                Context context;
                UserInfo user = UserSettings.this.mUserManager.getUserInfo(UserHandle.myUserId());
                if ((user.iconPath == null || user.iconPath.equals("")) && (context = UserSettings.this.getActivity()) != null) {
                    Utils.copyMeProfilePhoto(context.getApplicationContext(), user);
                }
                return user.name;
            }
        }.execute(new Void[0]);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void finishLoadProfile(String profileName) {
        if (getActivity() == null) {
            return;
        }
        this.mMePreference.setTitle(getString(R.string.user_you, new Object[]{profileName}));
        int myUserId = UserHandle.myUserId();
        Bitmap b = this.mUserManager.getUserIcon(myUserId);
        if (b == null) {
            return;
        }
        this.mMePreference.setIcon(encircle(b));
        this.mUserIcons.put(myUserId, b);
    }

    private boolean hasLockscreenSecurity() {
        LockPatternUtils lpu = new LockPatternUtils(getActivity());
        return lpu.isSecure(UserHandle.myUserId());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void launchChooseLockscreen() {
        Intent chooseLockIntent = new Intent("android.app.action.SET_NEW_PASSWORD");
        chooseLockIntent.putExtra("minimum_quality", 65536);
        startActivityForResult(chooseLockIntent, 10);
    }

    @Override // android.app.Fragment
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10) {
            if (resultCode == 0 || !hasLockscreenSecurity()) {
                return;
            }
            addUserNow(2);
            return;
        }
        this.mEditUserInfoController.onActivityResult(requestCode, resultCode, data);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onAddUserClicked(int userType) {
        synchronized (this.mUserLock) {
            if (this.mRemovingUserId == -1 && !this.mAddingUser) {
                switch (userType) {
                    case 1:
                        showDialog(2);
                        break;
                    case 2:
                        if (hasLockscreenSecurity()) {
                            addUserNow(2);
                            break;
                        } else {
                            showDialog(7);
                            break;
                        }
                }
            }
        }
    }

    private void onRemoveUserClicked(int userId) {
        synchronized (this.mUserLock) {
            if (this.mRemovingUserId == -1 && !this.mAddingUser) {
                this.mRemovingUserId = userId;
                showDialog(1);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public UserInfo createRestrictedProfile() {
        UserInfo newUserInfo = this.mUserManager.createRestrictedProfile(this.mAddingUserName);
        Utils.assignDefaultPhoto(getActivity(), newUserInfo.id);
        return newUserInfo;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public UserInfo createTrustedUser() {
        UserInfo newUserInfo = this.mUserManager.createUser(this.mAddingUserName, 0);
        if (newUserInfo != null) {
            Utils.assignDefaultPhoto(getActivity(), newUserInfo.id);
        }
        return newUserInfo;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onManageUserClicked(int userId, boolean newUser) {
        this.mAddingUser = false;
        if (userId == -11) {
            Bundle extras = new Bundle();
            extras.putBoolean("guest_user", true);
            ((SettingsActivity) getActivity()).startPreferencePanel(UserDetailsSettings.class.getName(), extras, R.string.user_guest, null, null, 0);
            return;
        }
        UserInfo info = this.mUserManager.getUserInfo(userId);
        if (info.isRestricted() && this.mUserCaps.mIsAdmin) {
            Bundle extras2 = new Bundle();
            extras2.putInt("user_id", userId);
            extras2.putBoolean("new_user", newUser);
            ((SettingsActivity) getActivity()).startPreferencePanel(RestrictedProfileSettings.class.getName(), extras2, R.string.user_restrictions_title, null, null, 0);
        } else if (info.id == UserHandle.myUserId()) {
            OwnerInfoSettings.show(this);
        } else if (!this.mUserCaps.mIsAdmin) {
        } else {
            Bundle extras3 = new Bundle();
            extras3.putInt("user_id", userId);
            ((SettingsActivity) getActivity()).startPreferencePanel(UserDetailsSettings.class.getName(), extras3, -1, info.name, null, 0);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onUserCreated(int userId) {
        this.mAddedUserId = userId;
        this.mAddingUser = false;
        if (this.mUserManager.getUserInfo(userId).isRestricted()) {
            showDialog(4);
        } else {
            showDialog(3);
        }
    }

    @Override // com.android.settings.SettingsPreferenceFragment
    public void onDialogShowing() {
        super.onDialogShowing();
        setOnDismissListener(this);
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settings.DialogCreatable
    public Dialog onCreateDialog(int dialogId) {
        Context context = getActivity();
        if (context == null) {
            return null;
        }
        switch (dialogId) {
            case 1:
                Dialog dlg = UserDialogs.createRemoveDialog(getActivity(), this.mRemovingUserId, new DialogInterface.OnClickListener() { // from class: com.android.settings.users.UserSettings.6
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialog, int which) {
                        UserSettings.this.removeUserNow();
                    }
                });
                return dlg;
            case 2:
                final SharedPreferences preferences = getActivity().getPreferences(0);
                final boolean longMessageDisplayed = preferences.getBoolean("key_add_user_long_message_displayed", false);
                int messageResId = longMessageDisplayed ? R.string.user_add_user_message_short : R.string.user_add_user_message_long;
                int userType = dialogId == 2 ? 1 : 2;
                final int i = userType;
                Dialog dlg2 = new AlertDialog.Builder(context).setTitle(R.string.user_add_user_title).setMessage(messageResId).setPositiveButton(17039370, new DialogInterface.OnClickListener() { // from class: com.android.settings.users.UserSettings.7
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialog, int which) {
                        UserSettings.this.addUserNow(i);
                        if (longMessageDisplayed) {
                            return;
                        }
                        preferences.edit().putBoolean("key_add_user_long_message_displayed", true).apply();
                    }
                }).setNegativeButton(17039360, (DialogInterface.OnClickListener) null).create();
                return dlg2;
            case 3:
                Dialog dlg3 = new AlertDialog.Builder(context).setTitle(R.string.user_setup_dialog_title).setMessage(R.string.user_setup_dialog_message).setPositiveButton(R.string.user_setup_button_setup_now, new DialogInterface.OnClickListener() { // from class: com.android.settings.users.UserSettings.8
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialog, int which) {
                        UserSettings.this.switchUserNow(UserSettings.this.mAddedUserId);
                    }
                }).setNegativeButton(R.string.user_setup_button_setup_later, (DialogInterface.OnClickListener) null).create();
                return dlg3;
            case 4:
                Dialog dlg4 = new AlertDialog.Builder(context).setMessage(R.string.user_setup_profile_dialog_message).setPositiveButton(17039370, new DialogInterface.OnClickListener() { // from class: com.android.settings.users.UserSettings.9
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialog, int which) {
                        UserSettings.this.switchUserNow(UserSettings.this.mAddedUserId);
                    }
                }).setNegativeButton(17039360, (DialogInterface.OnClickListener) null).create();
                return dlg4;
            case 5:
                return new AlertDialog.Builder(context).setMessage(R.string.user_cannot_manage_message).setPositiveButton(17039370, (DialogInterface.OnClickListener) null).create();
            case 6:
                List<HashMap<String, String>> data = new ArrayList<>();
                HashMap<String, String> addUserItem = new HashMap<>();
                addUserItem.put("title", getString(R.string.user_add_user_item_title));
                addUserItem.put("summary", getString(R.string.user_add_user_item_summary));
                HashMap<String, String> addProfileItem = new HashMap<>();
                addProfileItem.put("title", getString(R.string.user_add_profile_item_title));
                addProfileItem.put("summary", getString(R.string.user_add_profile_item_summary));
                data.add(addUserItem);
                data.add(addProfileItem);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                SimpleAdapter adapter = new SimpleAdapter(builder.getContext(), data, R.layout.two_line_list_item, new String[]{"title", "summary"}, new int[]{R.id.title, R.id.summary});
                builder.setTitle(R.string.user_add_user_type_title);
                builder.setAdapter(adapter, new DialogInterface.OnClickListener() { // from class: com.android.settings.users.UserSettings.10
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialog, int which) {
                        int i2;
                        UserSettings userSettings = UserSettings.this;
                        if (which == 0) {
                            i2 = 1;
                        } else {
                            i2 = 2;
                        }
                        userSettings.onAddUserClicked(i2);
                    }
                });
                return builder.create();
            case 7:
                Dialog dlg5 = new AlertDialog.Builder(context).setMessage(R.string.user_need_lock_message).setPositiveButton(R.string.user_set_lock_button, new DialogInterface.OnClickListener() { // from class: com.android.settings.users.UserSettings.11
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialog, int which) {
                        UserSettings.this.launchChooseLockscreen();
                    }
                }).setNegativeButton(17039360, (DialogInterface.OnClickListener) null).create();
                return dlg5;
            case 8:
                Dialog dlg6 = new AlertDialog.Builder(context).setTitle(R.string.user_exit_guest_confirm_title).setMessage(R.string.user_exit_guest_confirm_message).setPositiveButton(R.string.user_exit_guest_dialog_remove, new DialogInterface.OnClickListener() { // from class: com.android.settings.users.UserSettings.12
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialog, int which) {
                        UserSettings.this.exitGuest();
                    }
                }).setNegativeButton(17039360, (DialogInterface.OnClickListener) null).create();
                return dlg6;
            case 9:
                Dialog dlg7 = this.mEditUserInfoController.createDialog(this, this.mMePreference.getIcon(), this.mMePreference.getTitle(), R.string.profile_info_settings_title, this, Process.myUserHandle());
                return dlg7;
            default:
                return null;
        }
    }

    private boolean emergencyInfoActivityPresent() {
        Intent intent = new Intent("android.settings.EDIT_EMERGENGY_INFO").setPackage("com.android.emergency");
        List<ResolveInfo> infos = getContext().getPackageManager().queryIntentActivities(intent, 0);
        return (infos == null || infos.isEmpty()) ? false : true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Type inference failed for: r0v1, types: [com.android.settings.users.UserSettings$13] */
    public void removeUserNow() {
        if (this.mRemovingUserId == UserHandle.myUserId()) {
            removeThisUser();
            return;
        }
        showDeleteUserDialog();
        new Thread() { // from class: com.android.settings.users.UserSettings.13
            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                synchronized (UserSettings.this.mUserLock) {
                    UserSettings.this.mUserManager.removeUser(UserSettings.this.mRemovingUserId);
                    UserSettings.this.mHandler.sendEmptyMessage(1);
                }
            }
        }.start();
    }

    private void removeThisUser() {
        if (!this.mUserManager.canSwitchUsers()) {
            Log.w("UserSettings", "Cannot remove current user when switching is disabled");
            return;
        }
        try {
            ActivityManagerNative.getDefault().switchUser(0);
            ((UserManager) getContext().getSystemService(UserManager.class)).removeUser(UserHandle.myUserId());
        } catch (RemoteException e) {
            Log.e("UserSettings", "Unable to remove self user");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Type inference failed for: r0v5, types: [com.android.settings.users.UserSettings$14] */
    public void addUserNow(final int userType) {
        synchronized (this.mUserLock) {
            this.mAddingUser = true;
            this.mAddingUserName = userType == 1 ? getString(R.string.user_new_user_name) : getString(R.string.user_new_profile_name);
            new Thread() { // from class: com.android.settings.users.UserSettings.14
                @Override // java.lang.Thread, java.lang.Runnable
                public void run() {
                    UserInfo user;
                    if (userType == 1) {
                        user = UserSettings.this.createTrustedUser();
                    } else {
                        user = UserSettings.this.createRestrictedProfile();
                    }
                    if (user == null) {
                        UserSettings.this.mAddingUser = false;
                        return;
                    }
                    synchronized (UserSettings.this.mUserLock) {
                        if (userType == 1) {
                            UserSettings.this.mHandler.sendEmptyMessage(1);
                            UserSettings.this.mHandler.sendMessage(UserSettings.this.mHandler.obtainMessage(2, user.id, user.serialNumber));
                        } else {
                            UserSettings.this.mHandler.sendMessage(UserSettings.this.mHandler.obtainMessage(3, user.id, user.serialNumber));
                        }
                    }
                }
            }.start();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void switchUserNow(int userId) {
        try {
            ActivityManagerNative.getDefault().switchUser(userId);
        } catch (RemoteException e) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void exitGuest() {
        if (!this.mUserCaps.mIsGuest) {
            return;
        }
        removeThisUser();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateUserList() {
        PreferenceGroup groupToAddUsers;
        UserPreference pref;
        this.mUpdateUserListOperate = true;
        if (getActivity() == null) {
            return;
        }
        List<UserInfo> users = this.mUserManager.getUsers(true);
        Context context = getActivity();
        boolean voiceCapable = Utils.isVoiceCapable(context);
        ArrayList<Integer> missingIcons = new ArrayList<>();
        ArrayList<UserPreference> userPreferences = new ArrayList<>();
        userPreferences.add(this.mMePreference);
        for (UserInfo user : users) {
            if (user.supportsSwitchToByUser()) {
                if (user.id == UserHandle.myUserId()) {
                    pref = this.mMePreference;
                } else if (!user.isGuest()) {
                    boolean showSettings = this.mUserCaps.mIsAdmin ? !voiceCapable ? user.isRestricted() : true : false;
                    boolean showDelete = this.mUserCaps.mIsAdmin ? (voiceCapable || user.isRestricted() || user.isGuest()) ? false : true : false;
                    pref = new UserPreference(getPrefContext(), null, user.id, showSettings ? this : null, showDelete ? this : null);
                    pref.setKey("id=" + user.id);
                    userPreferences.add(pref);
                    if (user.isAdmin()) {
                        pref.setSummary(R.string.user_admin);
                    }
                    pref.setTitle(user.name);
                    pref.setSelectable(false);
                }
                if (pref != null) {
                    if (!isInitialized(user)) {
                        if (user.isRestricted()) {
                            pref.setSummary(R.string.user_summary_restricted_not_set_up);
                        } else {
                            pref.setSummary(R.string.user_summary_not_set_up);
                        }
                        pref.setOnPreferenceClickListener(this);
                        pref.setSelectable(true);
                    } else if (user.isRestricted()) {
                        pref.setSummary(R.string.user_summary_restricted_profile);
                    }
                    if (user.iconPath == null) {
                        pref.setIcon(getEncircledDefaultIcon());
                    } else if (this.mUserIcons.get(user.id) == null) {
                        missingIcons.add(Integer.valueOf(user.id));
                        pref.setIcon(getEncircledDefaultIcon());
                    } else {
                        setPhotoId(pref, user);
                    }
                }
            }
        }
        if (this.mAddingUser) {
            UserPreference pref2 = new UserPreference(getPrefContext(), null, -10, null, null);
            pref2.setEnabled(false);
            pref2.setTitle(this.mAddingUserName);
            pref2.setIcon(getEncircledDefaultIcon());
            userPreferences.add(pref2);
        }
        if (!this.mUserCaps.mIsGuest && (this.mUserCaps.mCanAddGuest || this.mUserCaps.mDisallowAddUserSetByAdmin)) {
            UserPreference pref3 = new UserPreference(getPrefContext(), null, -11, (this.mUserCaps.mIsAdmin && voiceCapable) ? this : null, null);
            pref3.setTitle(R.string.user_guest);
            pref3.setIcon(getEncircledDefaultIcon());
            userPreferences.add(pref3);
            pref3.setDisabledByAdmin(this.mUserCaps.mDisallowAddUser ? this.mUserCaps.mEnforcedAdmin : null);
            pref3.setSelectable(false);
        }
        Collections.sort(userPreferences, UserPreference.SERIAL_NUMBER_COMPARATOR);
        getActivity().invalidateOptionsMenu();
        if (missingIcons.size() > 0) {
            loadIconsAsync(missingIcons);
        }
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        preferenceScreen.removeAll();
        if (this.mUserCaps.mCanAddRestrictedProfile) {
            this.mUserListCategory.removeAll();
            this.mUserListCategory.setOrder(Integer.MAX_VALUE);
            preferenceScreen.addPreference(this.mUserListCategory);
            groupToAddUsers = this.mUserListCategory;
        } else {
            groupToAddUsers = preferenceScreen;
        }
        for (UserPreference userPreference : userPreferences) {
            userPreference.setOrder(Integer.MAX_VALUE);
            groupToAddUsers.addPreference(userPreference);
        }
        if ((this.mUserCaps.mCanAddUser || this.mUserCaps.mDisallowAddUserSetByAdmin) && Utils.isDeviceProvisioned(getActivity())) {
            boolean moreUsers = this.mUserManager.canAddMoreUsers();
            this.mAddUser.setOrder(Integer.MAX_VALUE);
            preferenceScreen.addPreference(this.mAddUser);
            this.mAddUser.setEnabled(moreUsers && !this.mAddingUser);
            if (moreUsers) {
                this.mAddUser.setSummary((CharSequence) null);
            } else {
                this.mAddUser.setSummary(getString(R.string.user_add_max_count, new Object[]{Integer.valueOf(getMaxRealUsers())}));
            }
            if (this.mAddUser.isEnabled()) {
                this.mAddUser.setDisabledByAdmin(this.mUserCaps.mDisallowAddUser ? this.mUserCaps.mEnforcedAdmin : null);
            }
        }
        if (this.mUserCaps.mIsAdmin && (!this.mUserCaps.mDisallowAddUser || this.mUserCaps.mDisallowAddUserSetByAdmin)) {
            this.mLockScreenSettings.setOrder(Integer.MAX_VALUE);
            preferenceScreen.addPreference(this.mLockScreenSettings);
            this.mAddUserWhenLocked.setChecked(Settings.Global.getInt(getContentResolver(), "add_users_when_locked", 0) == 1);
            this.mAddUserWhenLocked.setOnPreferenceChangeListener(this);
            this.mAddUserWhenLocked.setDisabledByAdmin(this.mUserCaps.mDisallowAddUser ? this.mUserCaps.mEnforcedAdmin : null);
        }
        if (emergencyInfoActivityPresent()) {
            this.mEmergencyInfoPreference.setOnPreferenceClickListener(this);
            this.mEmergencyInfoPreference.setOrder(Integer.MAX_VALUE);
            preferenceScreen.addPreference(this.mEmergencyInfoPreference);
        }
        this.mUpdateUserListOperate = false;
    }

    private int getMaxRealUsers() {
        int maxUsersAndGuest = UserManager.getMaxSupportedUsers() + 1;
        List<UserInfo> users = this.mUserManager.getUsers();
        int managedProfiles = 0;
        for (UserInfo user : users) {
            if (user.isManagedProfile()) {
                managedProfiles++;
            }
        }
        return maxUsersAndGuest - managedProfiles;
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.settings.users.UserSettings$15] */
    private void loadIconsAsync(List<Integer> missingIcons) {
        new AsyncTask<List<Integer>, Void, Void>() { // from class: com.android.settings.users.UserSettings.15
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public void onPostExecute(Void result) {
                UserSettings.this.updateUserList();
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public Void doInBackground(List<Integer>... values) {
                for (Integer num : values[0]) {
                    int userId = num.intValue();
                    Bitmap bitmap = UserSettings.this.mUserManager.getUserIcon(userId);
                    if (bitmap == null) {
                        bitmap = Utils.getDefaultUserIconAsBitmap(userId);
                    }
                    UserSettings.this.mUserIcons.append(userId, bitmap);
                }
                return null;
            }
        }.execute(missingIcons);
    }

    private Drawable getEncircledDefaultIcon() {
        if (this.mDefaultIconDrawable == null) {
            this.mDefaultIconDrawable = encircle(Utils.getDefaultUserIconAsBitmap(-10000));
        }
        return this.mDefaultIconDrawable;
    }

    private void setPhotoId(Preference pref, UserInfo user) {
        Bitmap bitmap = this.mUserIcons.get(user.id);
        if (bitmap == null) {
            return;
        }
        pref.setIcon(encircle(bitmap));
    }

    @Override // android.support.v7.preference.Preference.OnPreferenceClickListener
    public boolean onPreferenceClick(Preference pref) {
        if (pref == this.mMePreference) {
            if (this.mUserCaps.mIsGuest) {
                showDialog(8);
                return true;
            } else if (this.mUserManager.isLinkedUser()) {
                onManageUserClicked(UserHandle.myUserId(), false);
            } else {
                showDialog(9);
            }
        } else if (pref instanceof UserPreference) {
            int userId = ((UserPreference) pref).getUserId();
            UserInfo user = this.mUserManager.getUserInfo(userId);
            if (!isInitialized(user)) {
                this.mHandler.sendMessage(this.mHandler.obtainMessage(2, user.id, user.serialNumber));
            }
        } else if (pref == this.mAddUser) {
            if (this.mUserCaps.mCanAddRestrictedProfile) {
                showDialog(6);
            } else {
                onAddUserClicked(1);
            }
        } else if (pref == this.mEmergencyInfoPreference) {
            Intent intent = new Intent("android.settings.EDIT_EMERGENGY_INFO");
            intent.setFlags(67108864);
            startActivity(intent);
        }
        return false;
    }

    private boolean isInitialized(UserInfo user) {
        return (user.flags & 16) != 0;
    }

    private Drawable encircle(Bitmap icon) {
        Drawable circled = CircleFramedDrawable.getInstance(getActivity(), icon);
        return circled;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View v) {
        if (!(v.getTag() instanceof UserPreference)) {
            return;
        }
        int userId = ((UserPreference) v.getTag()).getUserId();
        switch (v.getId()) {
            case R.id.manage_user /* 2131886574 */:
                onManageUserClicked(userId, false);
                return;
            case R.id.divider_delete /* 2131886575 */:
            default:
                return;
            case R.id.trash_user /* 2131886576 */:
                if (this.mUpdateUserListOperate) {
                    return;
                }
                RestrictedLockUtils.EnforcedAdmin removeDisallowedAdmin = RestrictedLockUtils.checkIfRestrictionEnforced(getContext(), "no_remove_user", UserHandle.myUserId());
                if (removeDisallowedAdmin != null) {
                    RestrictedLockUtils.sendShowAdminSupportDetailsIntent(getContext(), removeDisallowedAdmin);
                    return;
                } else {
                    onRemoveUserClicked(userId);
                    return;
                }
        }
    }

    @Override // android.content.DialogInterface.OnDismissListener
    public void onDismiss(DialogInterface dialog) {
        synchronized (this.mUserLock) {
            this.mRemovingUserId = -1;
            updateUserList();
        }
    }

    @Override // android.support.v7.preference.Preference.OnPreferenceChangeListener
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int i = 0;
        if (preference == this.mAddUserWhenLocked) {
            Boolean value = (Boolean) newValue;
            ContentResolver contentResolver = getContentResolver();
            if (value != null && value.booleanValue()) {
                i = 1;
            }
            Settings.Global.putInt(contentResolver, "add_users_when_locked", i);
            return true;
        }
        return false;
    }

    @Override // com.android.settings.SettingsPreferenceFragment
    public int getHelpResource() {
        return R.string.help_url_users;
    }

    @Override // com.android.settings.users.EditUserInfoController.OnContentChangedCallback
    public void onPhotoChanged(Drawable photo) {
        this.mMePreference.setIcon(photo);
    }

    @Override // com.android.settings.users.EditUserInfoController.OnContentChangedCallback
    public void onLabelChanged(CharSequence label) {
        this.mMePreference.setTitle(label);
    }

    private void showDeleteUserDialog() {
        if (this.mDeletingUserDialog == null) {
            this.mDeletingUserDialog = new ProgressDialog(getActivity());
            this.mDeletingUserDialog.setMessage(getResources().getString(R.string.data_enabler_waiting_message));
            this.mDeletingUserDialog.setIndeterminate(true);
            this.mDeletingUserDialog.setCancelable(false);
        }
        if (this.mDeletingUserDialog.isShowing()) {
            return;
        }
        this.mDeletingUserDialog.show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dismissDeleteUserDialog() {
        if (this.mDeletingUserDialog == null || !this.mDeletingUserDialog.isShowing()) {
            return;
        }
        this.mDeletingUserDialog.dismiss();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class UserCapabilities {
        boolean mCanAddGuest;
        boolean mDisallowAddUser;
        boolean mDisallowAddUserSetByAdmin;
        RestrictedLockUtils.EnforcedAdmin mEnforcedAdmin;
        boolean mIsAdmin;
        boolean mIsGuest;
        boolean mEnabled = true;
        boolean mCanAddUser = true;
        boolean mCanAddRestrictedProfile = true;

        private UserCapabilities() {
        }

        public static UserCapabilities create(Context context) {
            UserManager userManager = (UserManager) context.getSystemService("user");
            UserCapabilities caps = new UserCapabilities();
            if (!UserManager.supportsMultipleUsers() || Utils.isMonkeyRunning()) {
                caps.mEnabled = false;
                return caps;
            }
            UserInfo myUserInfo = userManager.getUserInfo(UserHandle.myUserId());
            caps.mIsGuest = myUserInfo.isGuest();
            caps.mIsAdmin = myUserInfo.isAdmin();
            DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService("device_policy");
            if (dpm.isDeviceManaged() || Utils.isVoiceCapable(context)) {
                caps.mCanAddRestrictedProfile = false;
            }
            caps.updateAddUserCapabilities(context);
            return caps;
        }

        public void updateAddUserCapabilities(Context context) {
            this.mEnforcedAdmin = RestrictedLockUtils.checkIfRestrictionEnforced(context, "no_add_user", UserHandle.myUserId());
            boolean hasBaseUserRestriction = RestrictedLockUtils.hasBaseUserRestriction(context, "no_add_user", UserHandle.myUserId());
            this.mDisallowAddUserSetByAdmin = (this.mEnforcedAdmin == null || hasBaseUserRestriction) ? false : true;
            if (this.mEnforcedAdmin != null) {
                hasBaseUserRestriction = true;
            }
            this.mDisallowAddUser = hasBaseUserRestriction;
            this.mCanAddUser = true;
            if (!this.mIsAdmin || UserManager.getMaxSupportedUsers() < 2 || !UserManager.supportsMultipleUsers() || this.mDisallowAddUser) {
                this.mCanAddUser = false;
            }
            boolean canAddUsersWhenLocked = this.mIsAdmin || Settings.Global.getInt(context.getContentResolver(), "add_users_when_locked", 0) == 1;
            if (this.mIsGuest || this.mDisallowAddUser) {
                canAddUsersWhenLocked = false;
            }
            this.mCanAddGuest = canAddUsersWhenLocked;
        }

        public String toString() {
            return "UserCapabilities{mEnabled=" + this.mEnabled + ", mCanAddUser=" + this.mCanAddUser + ", mCanAddRestrictedProfile=" + this.mCanAddRestrictedProfile + ", mIsAdmin=" + this.mIsAdmin + ", mIsGuest=" + this.mIsGuest + ", mCanAddGuest=" + this.mCanAddGuest + ", mDisallowAddUser=" + this.mDisallowAddUser + ", mEnforcedAdmin=" + this.mEnforcedAdmin + '}';
        }
    }

    /* loaded from: classes.dex */
    private static class SummaryProvider implements SummaryLoader.SummaryProvider {
        private final Context mContext;
        private final SummaryLoader mSummaryLoader;

        public SummaryProvider(Context context, SummaryLoader summaryLoader) {
            this.mContext = context;
            this.mSummaryLoader = summaryLoader;
        }

        @Override // com.android.settings.dashboard.SummaryLoader.SummaryProvider
        public void setListening(boolean listening) {
            if (!listening) {
                return;
            }
            UserInfo info = ((UserManager) this.mContext.getSystemService(UserManager.class)).getUserInfo(UserHandle.myUserId());
            this.mSummaryLoader.setSummary(this, this.mContext.getString(R.string.user_summary, info.name));
        }
    }
}
