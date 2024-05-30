package com.android.settings.users;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.ContactsContract;
import android.provider.SearchIndexableResource;
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
import com.android.internal.util.UserIcons;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.users.EditUserInfoController;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedPreference;
import com.android.settingslib.drawable.CircleFramedDrawable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
/* loaded from: classes.dex */
public class UserSettings extends SettingsPreferenceFragment implements DialogInterface.OnDismissListener, Preference.OnPreferenceClickListener, View.OnClickListener, Indexable, EditUserInfoController.OnContentChangedCallback {
    private RestrictedPreference mAddUser;
    private AddUserWhenLockedPreferenceController mAddUserWhenLockedPreferenceController;
    private boolean mAddingUser;
    private String mAddingUserName;
    private Drawable mDefaultIconDrawable;
    private ProgressDialog mDeletingUserDialog;
    private UserPreference mMePreference;
    private UserCapabilities mUserCaps;
    private PreferenceGroup mUserListCategory;
    private UserManager mUserManager;
    private static SparseArray<Bitmap> sDarkDefaultUserBitmapCache = new SparseArray<>();
    public static final SummaryLoader.SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = new SummaryLoader.SummaryProviderFactory() { // from class: com.android.settings.users.-$$Lambda$UserSettings$Eg6plZiaX7G7UUvF4Q46lU8PMRw
        @Override // com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory
        public final SummaryLoader.SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
            return UserSettings.lambda$static$1(activity, summaryLoader);
        }
    };
    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() { // from class: com.android.settings.users.UserSettings.14
        @Override // com.android.settings.search.BaseSearchIndexProvider
        protected boolean isPageSearchEnabled(Context context) {
            return UserCapabilities.create(context).mEnabled;
        }

        @Override // com.android.settings.search.BaseSearchIndexProvider, com.android.settings.search.Indexable.SearchIndexProvider
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean z) {
            ArrayList arrayList = new ArrayList();
            SearchIndexableResource searchIndexableResource = new SearchIndexableResource(context);
            searchIndexableResource.xmlResId = R.xml.user_settings;
            arrayList.add(searchIndexableResource);
            return arrayList;
        }

        @Override // com.android.settings.search.BaseSearchIndexProvider
        public List<String> getNonIndexableKeysFromXml(Context context, int i) {
            List<String> nonIndexableKeysFromXml = super.getNonIndexableKeysFromXml(context, i);
            new AddUserWhenLockedPreferenceController(context, "user_settings_add_users_when_locked", null).updateNonIndexableKeys(nonIndexableKeysFromXml);
            new AutoSyncDataPreferenceController(context, null).updateNonIndexableKeys(nonIndexableKeysFromXml);
            new AutoSyncPersonalDataPreferenceController(context, null).updateNonIndexableKeys(nonIndexableKeysFromXml);
            new AutoSyncWorkDataPreferenceController(context, null).updateNonIndexableKeys(nonIndexableKeysFromXml);
            return nonIndexableKeysFromXml;
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
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    UserSettings.this.updateUserList();
                    return;
                case 2:
                    UserSettings.this.onUserCreated(message.arg1);
                    return;
                case 3:
                    UserSettings.this.onManageUserClicked(message.arg1, true);
                    return;
                default:
                    return;
            }
        }
    };
    private BroadcastReceiver mUserChangeReceiver = new BroadcastReceiver() { // from class: com.android.settings.users.UserSettings.2
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            int intExtra;
            if (intent.getAction().equals("android.intent.action.USER_REMOVED")) {
                UserSettings.this.dismissDeleteUserDialog();
                UserSettings.this.mRemovingUserId = -1;
            } else if (intent.getAction().equals("android.intent.action.USER_INFO_CHANGED") && (intExtra = intent.getIntExtra("android.intent.extra.user_handle", -1)) != -1) {
                UserSettings.this.mUserIcons.remove(intExtra);
            }
            UserSettings.this.mHandler.sendEmptyMessage(1);
        }
    };

    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 96;
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.user_settings);
        if (Settings.Global.getInt(getContext().getContentResolver(), "device_provisioned", 0) == 0) {
            getActivity().finish();
            return;
        }
        Activity activity = getActivity();
        this.mAddUserWhenLockedPreferenceController = new AddUserWhenLockedPreferenceController(activity, "user_settings_add_users_when_locked", getLifecycle());
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        this.mAddUserWhenLockedPreferenceController.displayPreference(preferenceScreen);
        preferenceScreen.findPreference(this.mAddUserWhenLockedPreferenceController.getPreferenceKey()).setOnPreferenceChangeListener(this.mAddUserWhenLockedPreferenceController);
        if (bundle != null) {
            if (bundle.containsKey("adding_user")) {
                this.mAddedUserId = bundle.getInt("adding_user");
            }
            if (bundle.containsKey("removing_user")) {
                this.mRemovingUserId = bundle.getInt("removing_user");
            }
            this.mEditUserInfoController.onRestoreInstanceState(bundle);
        }
        this.mUserCaps = UserCapabilities.create(activity);
        this.mUserManager = (UserManager) activity.getSystemService("user");
        if (!this.mUserCaps.mEnabled) {
            return;
        }
        int myUserId = UserHandle.myUserId();
        this.mUserListCategory = (PreferenceGroup) findPreference("user_list");
        this.mMePreference = new UserPreference(getPrefContext(), null, myUserId, null, null);
        this.mMePreference.setKey("user_me");
        this.mMePreference.setOnPreferenceClickListener(this);
        if (this.mUserCaps.mIsAdmin) {
            this.mMePreference.setSummary(R.string.user_admin);
        }
        this.mAddUser = (RestrictedPreference) findPreference("user_add");
        this.mAddUser.useAdminDisabledSummary(false);
        if (this.mUserCaps.mCanAddUser && Utils.isDeviceProvisioned(getActivity())) {
            this.mAddUser.setVisible(true);
            this.mAddUser.setOnPreferenceClickListener(this);
            if (!this.mUserCaps.mCanAddRestrictedProfile) {
                this.mAddUser.setTitle(R.string.user_add_user_menu);
            }
        } else {
            this.mAddUser.setVisible(false);
        }
        IntentFilter intentFilter = new IntentFilter("android.intent.action.USER_REMOVED");
        intentFilter.addAction("android.intent.action.USER_INFO_CHANGED");
        activity.registerReceiverAsUser(this.mUserChangeReceiver, UserHandle.ALL, intentFilter, null, this.mHandler);
        loadProfile();
        updateUserList();
        this.mShouldUpdateUserList = false;
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settings.core.InstrumentedPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        if (!this.mUserCaps.mEnabled) {
            return;
        }
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (this.mAddUserWhenLockedPreferenceController.isAvailable()) {
            this.mAddUserWhenLockedPreferenceController.updateState(preferenceScreen.findPreference(this.mAddUserWhenLockedPreferenceController.getPreferenceKey()));
        }
        if (this.mShouldUpdateUserList) {
            this.mUserCaps.updateAddUserCapabilities(getActivity());
            loadProfile();
            updateUserList();
        }
    }

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onPause() {
        this.mShouldUpdateUserList = true;
        super.onPause();
    }

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onDestroy() {
        super.onDestroy();
        if (this.mUserCaps == null || !this.mUserCaps.mEnabled) {
            return;
        }
        getActivity().unregisterReceiver(this.mUserChangeReceiver);
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        this.mEditUserInfoController.onSaveInstanceState(bundle);
        bundle.putInt("adding_user", this.mAddedUserId);
        bundle.putInt("removing_user", this.mRemovingUserId);
    }

    @Override // android.app.Fragment
    public void startActivityForResult(Intent intent, int i) {
        this.mEditUserInfoController.startingActivityForResult();
        super.startActivityForResult(intent, i);
    }

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        UserManager userManager = (UserManager) getContext().getSystemService(UserManager.class);
        boolean z = !userManager.hasUserRestriction("no_remove_user");
        boolean canSwitchUsers = userManager.canSwitchUsers();
        if (!this.mUserCaps.mIsAdmin && z && canSwitchUsers) {
            menu.add(0, 1, 0, getResources().getString(R.string.user_remove_user_menu, this.mUserManager.getUserName())).setShowAsAction(0);
        }
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    @Override // com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == 1) {
            onRemoveUserClicked(UserHandle.myUserId());
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    /* JADX WARN: Type inference failed for: r0v2, types: [com.android.settings.users.UserSettings$3] */
    private void loadProfile() {
        if (this.mUserCaps.mIsGuest) {
            this.mMePreference.setIcon(getEncircledDefaultIcon());
            this.mMePreference.setTitle(R.string.user_exit_guest_title);
            return;
        }
        new AsyncTask<Void, Void, String>() { // from class: com.android.settings.users.UserSettings.3
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public void onPostExecute(String str) {
                UserSettings.this.finishLoadProfile(str);
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public String doInBackground(Void... voidArr) {
                Activity activity;
                UserInfo userInfo = UserSettings.this.mUserManager.getUserInfo(UserHandle.myUserId());
                if ((userInfo.iconPath == null || userInfo.iconPath.equals("")) && (activity = UserSettings.this.getActivity()) != null) {
                    UserSettings.copyMeProfilePhoto(activity.getApplicationContext(), userInfo);
                }
                return userInfo.name;
            }
        }.execute(new Void[0]);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void finishLoadProfile(String str) {
        if (getActivity() == null) {
            return;
        }
        this.mMePreference.setTitle(getString(R.string.user_you, new Object[]{str}));
        int myUserId = UserHandle.myUserId();
        Bitmap userIcon = this.mUserManager.getUserIcon(myUserId);
        if (userIcon != null) {
            this.mMePreference.setIcon(encircle(userIcon));
            this.mUserIcons.put(myUserId, userIcon);
        }
    }

    private boolean hasLockscreenSecurity() {
        return new LockPatternUtils(getActivity()).isSecure(UserHandle.myUserId());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void launchChooseLockscreen() {
        Intent intent = new Intent("android.app.action.SET_NEW_PASSWORD");
        intent.putExtra("minimum_quality", 65536);
        startActivityForResult(intent, 10);
    }

    @Override // android.app.Fragment
    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i == 10) {
            if (i2 != 0 && hasLockscreenSecurity()) {
                addUserNow(2);
                return;
            }
            return;
        }
        this.mEditUserInfoController.onActivityResult(i, i2, intent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onAddUserClicked(int i) {
        synchronized (this.mUserLock) {
            if (this.mRemovingUserId == -1 && !this.mAddingUser) {
                switch (i) {
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

    private void onRemoveUserClicked(int i) {
        synchronized (this.mUserLock) {
            if (this.mRemovingUserId == -1 && !this.mAddingUser) {
                this.mRemovingUserId = i;
                showDialog(1);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public UserInfo createRestrictedProfile() {
        UserInfo createRestrictedProfile = this.mUserManager.createRestrictedProfile(this.mAddingUserName);
        if (createRestrictedProfile != null && !assignDefaultPhoto(getActivity(), createRestrictedProfile.id)) {
            return null;
        }
        return createRestrictedProfile;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public UserInfo createTrustedUser() {
        UserInfo createUser = this.mUserManager.createUser(this.mAddingUserName, 0);
        if (createUser != null && !assignDefaultPhoto(getActivity(), createUser.id)) {
            return null;
        }
        return createUser;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onManageUserClicked(int i, boolean z) {
        this.mAddingUser = false;
        if (i == -11) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("guest_user", true);
            new SubSettingLauncher(getContext()).setDestination(UserDetailsSettings.class.getName()).setArguments(bundle).setTitle(R.string.user_guest).setSourceMetricsCategory(getMetricsCategory()).launch();
            return;
        }
        UserInfo userInfo = this.mUserManager.getUserInfo(i);
        if (userInfo.isRestricted() && this.mUserCaps.mIsAdmin) {
            Bundle bundle2 = new Bundle();
            bundle2.putInt("user_id", i);
            bundle2.putBoolean("new_user", z);
            new SubSettingLauncher(getContext()).setDestination(RestrictedProfileSettings.class.getName()).setArguments(bundle2).setTitle(R.string.user_restrictions_title).setSourceMetricsCategory(getMetricsCategory()).launch();
        } else if (userInfo.id == UserHandle.myUserId()) {
            OwnerInfoSettings.show(this);
        } else if (this.mUserCaps.mIsAdmin) {
            Bundle bundle3 = new Bundle();
            bundle3.putInt("user_id", i);
            new SubSettingLauncher(getContext()).setDestination(UserDetailsSettings.class.getName()).setArguments(bundle3).setTitle(userInfo.name).setSourceMetricsCategory(getMetricsCategory()).launch();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onUserCreated(int i) {
        this.mAddedUserId = i;
        this.mAddingUser = false;
        if (!isResumed()) {
            Log.w("UserSettings", "Cannot show dialog after onPause");
        } else if (this.mUserManager.getUserInfo(i).isRestricted()) {
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
    public Dialog onCreateDialog(int i) {
        int i2;
        Activity activity = getActivity();
        if (activity == null) {
            return null;
        }
        switch (i) {
            case 1:
                return UserDialogs.createRemoveDialog(getActivity(), this.mRemovingUserId, new DialogInterface.OnClickListener() { // from class: com.android.settings.users.UserSettings.4
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i3) {
                        UserSettings.this.removeUserNow();
                    }
                });
            case 2:
                final SharedPreferences preferences = getActivity().getPreferences(0);
                final boolean z = preferences.getBoolean("key_add_user_long_message_displayed", false);
                if (z) {
                    i2 = R.string.user_add_user_message_short;
                } else {
                    i2 = R.string.user_add_user_message_long;
                }
                final int i3 = i == 2 ? 1 : 2;
                return new AlertDialog.Builder(activity).setTitle(R.string.user_add_user_title).setMessage(i2).setPositiveButton(17039370, new DialogInterface.OnClickListener() { // from class: com.android.settings.users.UserSettings.5
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i4) {
                        UserSettings.this.addUserNow(i3);
                        if (!z) {
                            preferences.edit().putBoolean("key_add_user_long_message_displayed", true).apply();
                        }
                    }
                }).setNegativeButton(17039360, (DialogInterface.OnClickListener) null).create();
            case 3:
                return new AlertDialog.Builder(activity).setTitle(R.string.user_setup_dialog_title).setMessage(R.string.user_setup_dialog_message).setPositiveButton(R.string.user_setup_button_setup_now, new DialogInterface.OnClickListener() { // from class: com.android.settings.users.UserSettings.6
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i4) {
                        UserSettings.this.switchUserNow(UserSettings.this.mAddedUserId);
                    }
                }).setNegativeButton(R.string.user_setup_button_setup_later, (DialogInterface.OnClickListener) null).create();
            case 4:
                return new AlertDialog.Builder(activity).setMessage(R.string.user_setup_profile_dialog_message).setPositiveButton(17039370, new DialogInterface.OnClickListener() { // from class: com.android.settings.users.UserSettings.7
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i4) {
                        UserSettings.this.switchUserNow(UserSettings.this.mAddedUserId);
                    }
                }).setNegativeButton(17039360, (DialogInterface.OnClickListener) null).create();
            case 5:
                return new AlertDialog.Builder(activity).setMessage(R.string.user_cannot_manage_message).setPositiveButton(17039370, (DialogInterface.OnClickListener) null).create();
            case 6:
                ArrayList arrayList = new ArrayList();
                HashMap hashMap = new HashMap();
                hashMap.put("title", getString(R.string.user_add_user_item_title));
                hashMap.put("summary", getString(R.string.user_add_user_item_summary));
                HashMap hashMap2 = new HashMap();
                hashMap2.put("title", getString(R.string.user_add_profile_item_title));
                hashMap2.put("summary", getString(R.string.user_add_profile_item_summary));
                arrayList.add(hashMap);
                arrayList.add(hashMap2);
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                SimpleAdapter simpleAdapter = new SimpleAdapter(builder.getContext(), arrayList, R.layout.two_line_list_item, new String[]{"title", "summary"}, new int[]{R.id.title, R.id.summary});
                builder.setTitle(R.string.user_add_user_type_title);
                builder.setAdapter(simpleAdapter, new DialogInterface.OnClickListener() { // from class: com.android.settings.users.UserSettings.8
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i4) {
                        int i5;
                        UserSettings userSettings = UserSettings.this;
                        if (i4 == 0) {
                            i5 = 1;
                        } else {
                            i5 = 2;
                        }
                        userSettings.onAddUserClicked(i5);
                    }
                });
                return builder.create();
            case 7:
                return new AlertDialog.Builder(activity).setMessage(R.string.user_need_lock_message).setPositiveButton(R.string.user_set_lock_button, new DialogInterface.OnClickListener() { // from class: com.android.settings.users.UserSettings.9
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i4) {
                        UserSettings.this.launchChooseLockscreen();
                    }
                }).setNegativeButton(17039360, (DialogInterface.OnClickListener) null).create();
            case 8:
                return new AlertDialog.Builder(activity).setTitle(R.string.user_exit_guest_confirm_title).setMessage(R.string.user_exit_guest_confirm_message).setPositiveButton(R.string.user_exit_guest_dialog_remove, new DialogInterface.OnClickListener() { // from class: com.android.settings.users.UserSettings.10
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i4) {
                        UserSettings.this.exitGuest();
                    }
                }).setNegativeButton(17039360, (DialogInterface.OnClickListener) null).create();
            case 9:
                return this.mEditUserInfoController.createDialog(this, null, this.mMePreference.getTitle(), R.string.profile_info_settings_title, this, Process.myUserHandle());
            default:
                return null;
        }
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settings.DialogCreatable
    public int getDialogMetricsCategory(int i) {
        switch (i) {
            case 1:
                return 591;
            case 2:
                return 595;
            case 3:
                return 596;
            case 4:
                return 597;
            case 5:
                return 594;
            case 6:
                return 598;
            case 7:
                return 599;
            case 8:
                return 600;
            case 9:
                return 601;
            default:
                return 0;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Type inference failed for: r0v1, types: [com.android.settings.users.UserSettings$11] */
    public void removeUserNow() {
        if (this.mRemovingUserId == UserHandle.myUserId()) {
            removeThisUser();
            return;
        }
        showDeleteUserDialog();
        new Thread() { // from class: com.android.settings.users.UserSettings.11
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
            ActivityManager.getService().switchUser(0);
            ((UserManager) getContext().getSystemService(UserManager.class)).removeUser(UserHandle.myUserId());
        } catch (RemoteException e) {
            Log.e("UserSettings", "Unable to remove self user");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Type inference failed for: r1v4, types: [com.android.settings.users.UserSettings$12] */
    public void addUserNow(final int i) {
        synchronized (this.mUserLock) {
            this.mAddingUser = true;
            this.mAddingUserName = i == 1 ? getString(R.string.user_new_user_name) : getString(R.string.user_new_profile_name);
            new Thread() { // from class: com.android.settings.users.UserSettings.12
                @Override // java.lang.Thread, java.lang.Runnable
                public void run() {
                    UserInfo createTrustedUser = i == 1 ? UserSettings.this.createTrustedUser() : UserSettings.this.createRestrictedProfile();
                    if (createTrustedUser == null) {
                        UserSettings.this.mAddingUser = false;
                        return;
                    }
                    synchronized (UserSettings.this.mUserLock) {
                        if (i == 1) {
                            UserSettings.this.mHandler.sendEmptyMessage(1);
                            if (!UserSettings.this.mUserCaps.mDisallowSwitchUser) {
                                UserSettings.this.mHandler.sendMessage(UserSettings.this.mHandler.obtainMessage(2, createTrustedUser.id, createTrustedUser.serialNumber));
                            }
                        } else {
                            UserSettings.this.mHandler.sendMessage(UserSettings.this.mHandler.obtainMessage(3, createTrustedUser.id, createTrustedUser.serialNumber));
                        }
                    }
                }
            }.start();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void switchUserNow(int i) {
        try {
            ActivityManager.getService().switchUser(i);
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
        Preference preference;
        this.mUpdateUserListOperate = true;
        if (getActivity() == null) {
            return;
        }
        List users = this.mUserManager.getUsers(true);
        Activity activity = getActivity();
        boolean isVoiceCapable = Utils.isVoiceCapable(activity);
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        final int i = -11;
        arrayList2.add(this.mMePreference);
        Iterator it = users.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            UserInfo userInfo = (UserInfo) it.next();
            if (userInfo.supportsSwitchToByUser()) {
                if (userInfo.id == UserHandle.myUserId()) {
                    preference = this.mMePreference;
                } else if (userInfo.isGuest()) {
                    i = userInfo.id;
                } else {
                    UserPreference userPreference = new UserPreference(getPrefContext(), null, userInfo.id, this.mUserCaps.mIsAdmin && (isVoiceCapable || userInfo.isRestricted()) ? this : null, this.mUserCaps.mIsAdmin && !isVoiceCapable && !userInfo.isRestricted() && !userInfo.isGuest() ? this : null);
                    userPreference.setKey("id=" + userInfo.id);
                    arrayList2.add(userPreference);
                    if (userInfo.isAdmin()) {
                        userPreference.setSummary(R.string.user_admin);
                    }
                    userPreference.setTitle(userInfo.name);
                    userPreference.setSelectable(false);
                    preference = userPreference;
                }
                if (preference != null) {
                    if (!isInitialized(userInfo)) {
                        if (userInfo.isRestricted()) {
                            preference.setSummary(R.string.user_summary_restricted_not_set_up);
                        } else {
                            preference.setSummary(R.string.user_summary_not_set_up);
                        }
                        if (!this.mUserCaps.mDisallowSwitchUser) {
                            preference.setOnPreferenceClickListener(this);
                            preference.setSelectable(true);
                        }
                    } else if (userInfo.isRestricted()) {
                        preference.setSummary(R.string.user_summary_restricted_profile);
                    }
                    if (userInfo.iconPath != null) {
                        if (this.mUserIcons.get(userInfo.id) == null) {
                            arrayList.add(Integer.valueOf(userInfo.id));
                            preference.setIcon(getEncircledDefaultIcon());
                        } else {
                            setPhotoId(preference, userInfo);
                        }
                    } else {
                        preference.setIcon(getEncircledDefaultIcon());
                    }
                }
            }
        }
        if (this.mAddingUser) {
            UserPreference userPreference2 = new UserPreference(getPrefContext(), null, -10, null, null);
            userPreference2.setEnabled(false);
            userPreference2.setTitle(this.mAddingUserName);
            userPreference2.setIcon(getEncircledDefaultIcon());
            arrayList2.add(userPreference2);
        }
        if (!this.mUserCaps.mIsGuest && (this.mUserCaps.mCanAddGuest || this.mUserCaps.mDisallowAddUserSetByAdmin)) {
            UserPreference userPreference3 = new UserPreference(getPrefContext(), null, -11, (this.mUserCaps.mIsAdmin && isVoiceCapable) ? this : null, null);
            userPreference3.setTitle(R.string.user_guest);
            userPreference3.setIcon(getEncircledDefaultIcon());
            arrayList2.add(userPreference3);
            if (this.mUserCaps.mDisallowAddUser) {
                userPreference3.setDisabledByAdmin(this.mUserCaps.mEnforcedAdmin);
            } else if (this.mUserCaps.mDisallowSwitchUser) {
                userPreference3.setDisabledByAdmin(RestrictedLockUtils.getDeviceOwner(activity));
            } else {
                userPreference3.setDisabledByAdmin(null);
            }
            userPreference3.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { // from class: com.android.settings.users.-$$Lambda$UserSettings$wMqzhBHYMbgNNY7TSuzlNB8n9UY
                @Override // android.support.v7.preference.Preference.OnPreferenceClickListener
                public final boolean onPreferenceClick(Preference preference2) {
                    return UserSettings.lambda$updateUserList$0(UserSettings.this, i, preference2);
                }
            });
        }
        Collections.sort(arrayList2, UserPreference.SERIAL_NUMBER_COMPARATOR);
        getActivity().invalidateOptionsMenu();
        if (arrayList.size() > 0) {
            loadIconsAsync(arrayList);
        }
        this.mUserListCategory.removeAll();
        if (this.mUserCaps.mCanAddRestrictedProfile) {
            this.mUserListCategory.setTitle(R.string.user_list_title);
        } else {
            this.mUserListCategory.setTitle((CharSequence) null);
        }
        Iterator it2 = arrayList2.iterator();
        while (it2.hasNext()) {
            UserPreference userPreference4 = (UserPreference) it2.next();
            userPreference4.setOrder(Preference.DEFAULT_ORDER);
            this.mUserListCategory.addPreference(userPreference4);
        }
        if ((this.mUserCaps.mCanAddUser || this.mUserCaps.mDisallowAddUserSetByAdmin) && Utils.isDeviceProvisioned(getActivity())) {
            boolean canAddMoreUsers = this.mUserManager.canAddMoreUsers();
            this.mAddUser.setEnabled(canAddMoreUsers && !this.mAddingUser);
            if (!canAddMoreUsers) {
                this.mAddUser.setSummary(getString(R.string.user_add_max_count, new Object[]{Integer.valueOf(getMaxRealUsers())}));
            } else {
                this.mAddUser.setSummary((CharSequence) null);
            }
            if (this.mAddUser.isEnabled()) {
                this.mAddUser.setDisabledByAdmin(this.mUserCaps.mDisallowAddUser ? this.mUserCaps.mEnforcedAdmin : null);
            }
        }
        this.mUpdateUserListOperate = false;
    }

    public static /* synthetic */ boolean lambda$updateUserList$0(UserSettings userSettings, int i, Preference preference) {
        UserInfo createGuest;
        if (i == -11 && (createGuest = userSettings.mUserManager.createGuest(userSettings.getContext(), preference.getTitle().toString())) != null) {
            i = createGuest.id;
        }
        try {
            ActivityManager.getService().switchUser(i);
            return true;
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
            return true;
        }
    }

    private int getMaxRealUsers() {
        int maxSupportedUsers = UserManager.getMaxSupportedUsers() + 1;
        int i = 0;
        for (UserInfo userInfo : this.mUserManager.getUsers()) {
            if (userInfo.isManagedProfile()) {
                i++;
            }
        }
        return maxSupportedUsers - i;
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.settings.users.UserSettings$13] */
    private void loadIconsAsync(List<Integer> list) {
        new AsyncTask<List<Integer>, Void, Void>() { // from class: com.android.settings.users.UserSettings.13
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public void onPostExecute(Void r1) {
                UserSettings.this.updateUserList();
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public Void doInBackground(List<Integer>... listArr) {
                for (Integer num : listArr[0]) {
                    int intValue = num.intValue();
                    Bitmap userIcon = UserSettings.this.mUserManager.getUserIcon(intValue);
                    if (userIcon == null) {
                        userIcon = UserSettings.getDefaultUserIconAsBitmap(UserSettings.this.getContext().getResources(), intValue);
                    }
                    UserSettings.this.mUserIcons.append(intValue, userIcon);
                }
                return null;
            }
        }.execute(list);
    }

    private Drawable getEncircledDefaultIcon() {
        if (this.mDefaultIconDrawable == null) {
            this.mDefaultIconDrawable = encircle(getDefaultUserIconAsBitmap(getContext().getResources(), -10000));
        }
        return this.mDefaultIconDrawable;
    }

    private void setPhotoId(Preference preference, UserInfo userInfo) {
        Bitmap bitmap = this.mUserIcons.get(userInfo.id);
        if (bitmap != null) {
            preference.setIcon(encircle(bitmap));
        }
    }

    @Override // android.support.v7.preference.Preference.OnPreferenceClickListener
    public boolean onPreferenceClick(Preference preference) {
        if (preference == this.mMePreference) {
            if (this.mUserCaps.mIsGuest) {
                showDialog(8);
                return true;
            } else if (this.mUserManager.isLinkedUser()) {
                onManageUserClicked(UserHandle.myUserId(), false);
            } else {
                showDialog(9);
            }
        } else if (preference instanceof UserPreference) {
            UserInfo userInfo = this.mUserManager.getUserInfo(((UserPreference) preference).getUserId());
            if (!isInitialized(userInfo)) {
                this.mHandler.sendMessage(this.mHandler.obtainMessage(2, userInfo.id, userInfo.serialNumber));
            }
        } else if (preference == this.mAddUser) {
            if (this.mUserCaps.mCanAddRestrictedProfile) {
                showDialog(6);
            } else {
                onAddUserClicked(1);
            }
        }
        return false;
    }

    private boolean isInitialized(UserInfo userInfo) {
        return (userInfo.flags & 16) != 0;
    }

    private Drawable encircle(Bitmap bitmap) {
        return CircleFramedDrawable.getInstance(getActivity(), bitmap);
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (view.getTag() instanceof UserPreference) {
            int userId = ((UserPreference) view.getTag()).getUserId();
            int id = view.getId();
            if (id != R.id.manage_user) {
                if (id != R.id.trash_user || this.mUpdateUserListOperate) {
                    return;
                }
                RestrictedLockUtils.EnforcedAdmin checkIfRestrictionEnforced = RestrictedLockUtils.checkIfRestrictionEnforced(getContext(), "no_remove_user", UserHandle.myUserId());
                if (checkIfRestrictionEnforced != null) {
                    RestrictedLockUtils.sendShowAdminSupportDetailsIntent(getContext(), checkIfRestrictionEnforced);
                    return;
                } else {
                    onRemoveUserClicked(userId);
                    return;
                }
            }
            onManageUserClicked(userId, false);
        }
    }

    @Override // android.content.DialogInterface.OnDismissListener
    public void onDismiss(DialogInterface dialogInterface) {
        synchronized (this.mUserLock) {
            this.mRemovingUserId = -1;
            updateUserList();
        }
    }

    @Override // com.android.settings.support.actionbar.HelpResourceProvider
    public int getHelpResource() {
        return R.string.help_url_users;
    }

    @Override // com.android.settings.users.EditUserInfoController.OnContentChangedCallback
    public void onPhotoChanged(Drawable drawable) {
        this.mMePreference.setIcon(drawable);
    }

    @Override // com.android.settings.users.EditUserInfoController.OnContentChangedCallback
    public void onLabelChanged(CharSequence charSequence) {
        this.mMePreference.setTitle(charSequence);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static Bitmap getDefaultUserIconAsBitmap(Resources resources, int i) {
        Bitmap bitmap = sDarkDefaultUserBitmapCache.get(i);
        if (bitmap == null) {
            Bitmap convertToBitmap = UserIcons.convertToBitmap(UserIcons.getDefaultUserIcon(resources, i, false));
            sDarkDefaultUserBitmapCache.put(i, convertToBitmap);
            return convertToBitmap;
        }
        return bitmap;
    }

    static boolean assignDefaultPhoto(Context context, int i) {
        if (context == null) {
            return false;
        }
        ((UserManager) context.getSystemService("user")).setUserIcon(i, getDefaultUserIconAsBitmap(context.getResources(), i));
        return true;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void copyMeProfilePhoto(Context context, UserInfo userInfo) {
        Uri uri = ContactsContract.Profile.CONTENT_URI;
        int myUserId = userInfo != null ? userInfo.id : UserHandle.myUserId();
        InputStream openContactPhotoInputStream = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), uri, true);
        if (openContactPhotoInputStream == null) {
            assignDefaultPhoto(context, myUserId);
            return;
        }
        ((UserManager) context.getSystemService("user")).setUserIcon(myUserId, BitmapFactory.decodeStream(openContactPhotoInputStream));
        try {
            openContactPhotoInputStream.close();
        } catch (IOException e) {
        }
    }

    private void showDeleteUserDialog() {
        if (this.mDeletingUserDialog == null) {
            this.mDeletingUserDialog = new ProgressDialog(getActivity());
            this.mDeletingUserDialog.setMessage(getResources().getString(R.string.master_clear_progress_text));
            this.mDeletingUserDialog.setIndeterminate(true);
            this.mDeletingUserDialog.setCancelable(false);
        }
        if (!this.mDeletingUserDialog.isShowing()) {
            this.mDeletingUserDialog.show();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dismissDeleteUserDialog() {
        if (this.mDeletingUserDialog != null && this.mDeletingUserDialog.isShowing()) {
            this.mDeletingUserDialog.dismiss();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class SummaryProvider implements SummaryLoader.SummaryProvider {
        private final Context mContext;
        private final SummaryLoader mSummaryLoader;

        public SummaryProvider(Context context, SummaryLoader summaryLoader) {
            this.mContext = context;
            this.mSummaryLoader = summaryLoader;
        }

        @Override // com.android.settings.dashboard.SummaryLoader.SummaryProvider
        public void setListening(boolean z) {
            if (z) {
                this.mSummaryLoader.setSummary(this, this.mContext.getString(R.string.users_summary, ((UserManager) this.mContext.getSystemService(UserManager.class)).getUserInfo(UserHandle.myUserId()).name));
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ SummaryLoader.SummaryProvider lambda$static$1(Activity activity, SummaryLoader summaryLoader) {
        return new SummaryProvider(activity, summaryLoader);
    }
}
