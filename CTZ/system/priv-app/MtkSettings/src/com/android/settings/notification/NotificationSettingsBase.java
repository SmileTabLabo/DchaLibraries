package com.android.settings.notification;

import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.arch.lifecycle.LifecycleObserver;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.notification.NotificationBackend;
import com.android.settings.widget.MasterCheckBoxPreference;
import com.android.settingslib.RestrictedLockUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
/* loaded from: classes.dex */
public abstract class NotificationSettingsBase extends DashboardFragment {
    private static final boolean DEBUG = Log.isLoggable("NotifiSettingsBase", 3);
    protected NotificationBackend.AppRow mAppRow;
    protected Bundle mArgs;
    protected NotificationChannel mChannel;
    protected NotificationChannelGroup mChannelGroup;
    protected Context mContext;
    protected Intent mIntent;
    protected boolean mListeningToPackageRemove;
    protected NotificationManager mNm;
    protected String mPkg;
    protected PackageInfo mPkgInfo;
    protected PackageManager mPm;
    protected RestrictedLockUtils.EnforcedAdmin mSuspendedAppsAdmin;
    protected int mUid;
    protected int mUserId;
    protected NotificationBackend mBackend = new NotificationBackend();
    protected boolean mShowLegacyChannelConfig = false;
    protected List<NotificationPreferenceController> mControllers = new ArrayList();
    protected List<Preference> mDynamicPreferences = new ArrayList();
    protected ImportanceListener mImportanceListener = new ImportanceListener();
    protected final BroadcastReceiver mPackageRemovedReceiver = new BroadcastReceiver() { // from class: com.android.settings.notification.NotificationSettingsBase.2
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String schemeSpecificPart = intent.getData().getSchemeSpecificPart();
            if (NotificationSettingsBase.this.mPkgInfo == null || TextUtils.equals(NotificationSettingsBase.this.mPkgInfo.packageName, schemeSpecificPart)) {
                if (NotificationSettingsBase.DEBUG) {
                    Log.d("NotifiSettingsBase", "Package (" + schemeSpecificPart + ") removed. RemovingNotificationSettingsBase.");
                }
                NotificationSettingsBase.this.onPackageRemoved();
            }
        }
    };
    protected Comparator<NotificationChannel> mChannelComparator = new Comparator() { // from class: com.android.settings.notification.-$$Lambda$NotificationSettingsBase$-zFOM6q-03lCRFkOVmbrRVoBxkk
        @Override // java.util.Comparator
        public final int compare(Object obj, Object obj2) {
            return NotificationSettingsBase.lambda$new$0((NotificationChannel) obj, (NotificationChannel) obj2);
        }
    };

    @Override // com.android.settings.dashboard.DashboardFragment, com.android.settings.core.InstrumentedPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onAttach(Context context) {
        String stringExtra;
        int intExtra;
        super.onAttach(context);
        this.mContext = getActivity();
        this.mIntent = getActivity().getIntent();
        this.mArgs = getArguments();
        this.mPm = getPackageManager();
        this.mNm = NotificationManager.from(this.mContext);
        if (this.mArgs != null && this.mArgs.containsKey("package")) {
            stringExtra = this.mArgs.getString("package");
        } else {
            stringExtra = this.mIntent.getStringExtra("android.provider.extra.APP_PACKAGE");
        }
        this.mPkg = stringExtra;
        if (this.mArgs != null && this.mArgs.containsKey("uid")) {
            intExtra = this.mArgs.getInt("uid");
        } else {
            intExtra = this.mIntent.getIntExtra("app_uid", -1);
        }
        this.mUid = intExtra;
        if (this.mUid < 0) {
            try {
                this.mUid = this.mPm.getPackageUid(this.mPkg, 0);
            } catch (PackageManager.NameNotFoundException e) {
            }
        }
        this.mPkgInfo = findPackageInfo(this.mPkg, this.mUid);
        this.mUserId = UserHandle.getUserId(this.mUid);
        this.mSuspendedAppsAdmin = RestrictedLockUtils.checkIfApplicationIsSuspended(this.mContext, this.mPkg, this.mUserId);
        loadChannel();
        loadAppRow();
        loadChannelGroup();
        collectConfigActivities();
        getLifecycle().addObserver((LifecycleObserver) use(HeaderPreferenceController.class));
        for (NotificationPreferenceController notificationPreferenceController : this.mControllers) {
            notificationPreferenceController.onResume(this.mAppRow, this.mChannel, this.mChannelGroup, this.mSuspendedAppsAdmin);
        }
    }

    @Override // com.android.settings.dashboard.DashboardFragment, com.android.settings.SettingsPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (this.mIntent == null && this.mArgs == null) {
            Log.w("NotifiSettingsBase", "No intent");
            toastAndFinish();
        } else if (this.mUid < 0 || TextUtils.isEmpty(this.mPkg) || this.mPkgInfo == null) {
            Log.w("NotifiSettingsBase", "Missing package or uid or packageinfo");
            toastAndFinish();
        } else {
            startListeningToPackageRemove();
        }
    }

    @Override // com.android.settings.dashboard.DashboardFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onDestroy() {
        stopListeningToPackageRemove();
        super.onDestroy();
    }

    @Override // com.android.settings.dashboard.DashboardFragment, com.android.settings.SettingsPreferenceFragment, com.android.settings.core.InstrumentedPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        if (this.mUid < 0 || TextUtils.isEmpty(this.mPkg) || this.mPkgInfo == null || this.mAppRow == null) {
            Log.w("NotifiSettingsBase", "Missing package or uid or packageinfo");
            finish();
            return;
        }
        loadAppRow();
        if (this.mAppRow == null) {
            Log.w("NotifiSettingsBase", "Can't load package");
            finish();
            return;
        }
        loadChannel();
        loadChannelGroup();
        collectConfigActivities();
    }

    private void loadChannel() {
        Intent intent = getActivity().getIntent();
        String stringExtra = intent != null ? intent.getStringExtra("android.provider.extra.CHANNEL_ID") : null;
        if (stringExtra == null && intent != null) {
            Bundle bundleExtra = intent.getBundleExtra(":settings:show_fragment_args");
            stringExtra = bundleExtra != null ? bundleExtra.getString("android.provider.extra.CHANNEL_ID") : null;
        }
        this.mChannel = this.mBackend.getChannel(this.mPkg, this.mUid, stringExtra);
    }

    private void loadAppRow() {
        this.mAppRow = this.mBackend.loadAppRow(this.mContext, this.mPm, this.mPkgInfo);
    }

    private void loadChannelGroup() {
        NotificationChannelGroup group;
        this.mShowLegacyChannelConfig = this.mBackend.onlyHasDefaultChannel(this.mAppRow.pkg, this.mAppRow.uid) || (this.mChannel != null && "miscellaneous".equals(this.mChannel.getId()));
        if (this.mShowLegacyChannelConfig) {
            this.mChannel = this.mBackend.getChannel(this.mAppRow.pkg, this.mAppRow.uid, "miscellaneous");
        }
        if (this.mChannel != null && !TextUtils.isEmpty(this.mChannel.getGroup()) && (group = this.mBackend.getGroup(this.mPkg, this.mUid, this.mChannel.getGroup())) != null) {
            this.mChannelGroup = group;
        }
    }

    protected void toastAndFinish() {
        Toast.makeText(this.mContext, (int) R.string.app_not_found_dlg_text, 0).show();
        getActivity().finish();
    }

    protected void collectConfigActivities() {
        Intent intent = new Intent("android.intent.action.MAIN").addCategory("android.intent.category.NOTIFICATION_PREFERENCES").setPackage(this.mAppRow.pkg);
        List<ResolveInfo> queryIntentActivities = this.mPm.queryIntentActivities(intent, 0);
        if (DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Found ");
            sb.append(queryIntentActivities.size());
            sb.append(" preference activities");
            sb.append(queryIntentActivities.size() == 0 ? " ;_;" : "");
            Log.d("NotifiSettingsBase", sb.toString());
        }
        for (ResolveInfo resolveInfo : queryIntentActivities) {
            ActivityInfo activityInfo = resolveInfo.activityInfo;
            if (this.mAppRow.settingsIntent != null) {
                if (DEBUG) {
                    Log.d("NotifiSettingsBase", "Ignoring duplicate notification preference activity (" + activityInfo.name + ") for package " + activityInfo.packageName);
                }
            } else {
                this.mAppRow.settingsIntent = intent.setPackage(null).setClassName(activityInfo.packageName, activityInfo.name);
                if (this.mChannel != null) {
                    this.mAppRow.settingsIntent.putExtra("android.intent.extra.CHANNEL_ID", this.mChannel.getId());
                }
                if (this.mChannelGroup != null) {
                    this.mAppRow.settingsIntent.putExtra("android.intent.extra.CHANNEL_GROUP_ID", this.mChannelGroup.getId());
                }
            }
        }
    }

    private PackageInfo findPackageInfo(String str, int i) {
        String[] packagesForUid;
        if (str != null && i >= 0 && (packagesForUid = this.mPm.getPackagesForUid(i)) != null && str != null) {
            for (String str2 : packagesForUid) {
                if (str.equals(str2)) {
                    try {
                        return this.mPm.getPackageInfo(str, 64);
                    } catch (PackageManager.NameNotFoundException e) {
                        Log.w("NotifiSettingsBase", "Failed to load package " + str, e);
                    }
                }
            }
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public Preference populateSingleChannelPrefs(PreferenceGroup preferenceGroup, final NotificationChannel notificationChannel, boolean z) {
        MasterCheckBoxPreference masterCheckBoxPreference = new MasterCheckBoxPreference(getPrefContext());
        masterCheckBoxPreference.setCheckBoxEnabled(this.mSuspendedAppsAdmin == null && isChannelBlockable(notificationChannel) && isChannelConfigurable(notificationChannel) && !z);
        masterCheckBoxPreference.setKey(notificationChannel.getId());
        masterCheckBoxPreference.setTitle(notificationChannel.getName());
        masterCheckBoxPreference.setChecked(notificationChannel.getImportance() != 0);
        Bundle bundle = new Bundle();
        bundle.putInt("uid", this.mUid);
        bundle.putString("package", this.mPkg);
        bundle.putString("android.provider.extra.CHANNEL_ID", notificationChannel.getId());
        bundle.putBoolean("fromSettings", true);
        masterCheckBoxPreference.setIntent(new SubSettingLauncher(getActivity()).setDestination(ChannelNotificationSettings.class.getName()).setArguments(bundle).setTitle(R.string.notification_channel_title).setSourceMetricsCategory(getMetricsCategory()).toIntent());
        masterCheckBoxPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() { // from class: com.android.settings.notification.NotificationSettingsBase.1
            @Override // android.support.v7.preference.Preference.OnPreferenceChangeListener
            public boolean onPreferenceChange(Preference preference, Object obj) {
                notificationChannel.setImportance(((Boolean) obj).booleanValue() ? 2 : 0);
                notificationChannel.lockFields(4);
                NotificationSettingsBase.this.mBackend.updateChannel(NotificationSettingsBase.this.mPkg, NotificationSettingsBase.this.mUid, notificationChannel);
                return true;
            }
        });
        preferenceGroup.addPreference(masterCheckBoxPreference);
        return masterCheckBoxPreference;
    }

    protected boolean isChannelConfigurable(NotificationChannel notificationChannel) {
        if (notificationChannel != null && this.mAppRow != null) {
            return !notificationChannel.getId().equals(this.mAppRow.lockedChannelId);
        }
        return false;
    }

    protected boolean isChannelBlockable(NotificationChannel notificationChannel) {
        if (notificationChannel == null || this.mAppRow == null) {
            return false;
        }
        if (this.mAppRow.systemApp) {
            return notificationChannel.isBlockableSystem() || notificationChannel.getImportance() == 0;
        }
        return true;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isChannelGroupBlockable(NotificationChannelGroup notificationChannelGroup) {
        if (notificationChannelGroup != null && this.mAppRow != null) {
            if (!this.mAppRow.systemApp) {
                return true;
            }
            return notificationChannelGroup.isBlocked();
        }
        return false;
    }

    protected void setVisible(PreferenceGroup preferenceGroup, Preference preference, boolean z) {
        if ((preferenceGroup.findPreference(preference.getKey()) != null) == z) {
            return;
        }
        if (z) {
            preferenceGroup.addPreference(preference);
        } else {
            preferenceGroup.removePreference(preference);
        }
    }

    protected void startListeningToPackageRemove() {
        if (this.mListeningToPackageRemove) {
            return;
        }
        this.mListeningToPackageRemove = true;
        IntentFilter intentFilter = new IntentFilter("android.intent.action.PACKAGE_REMOVED");
        intentFilter.addDataScheme("package");
        getContext().registerReceiver(this.mPackageRemovedReceiver, intentFilter);
    }

    protected void stopListeningToPackageRemove() {
        if (!this.mListeningToPackageRemove) {
            return;
        }
        this.mListeningToPackageRemove = false;
        getContext().unregisterReceiver(this.mPackageRemovedReceiver);
    }

    protected void onPackageRemoved() {
        getActivity().finishAndRemoveTask();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ int lambda$new$0(NotificationChannel notificationChannel, NotificationChannel notificationChannel2) {
        if (notificationChannel.isDeleted() != notificationChannel2.isDeleted()) {
            return Boolean.compare(notificationChannel.isDeleted(), notificationChannel2.isDeleted());
        }
        if (notificationChannel.getId().equals("miscellaneous")) {
            return 1;
        }
        if (notificationChannel2.getId().equals("miscellaneous")) {
            return -1;
        }
        return notificationChannel.getId().compareTo(notificationChannel2.getId());
    }

    /* loaded from: classes.dex */
    protected class ImportanceListener {
        protected ImportanceListener() {
        }

        /* JADX INFO: Access modifiers changed from: protected */
        /* JADX WARN: Code restructure failed: missing block: B:14:0x0043, code lost:
            if (r8.this$0.mChannel.getImportance() == 0) goto L31;
         */
        /* JADX WARN: Removed duplicated region for block: B:26:0x0068  */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
        */
        public void onImportanceChanged() {
            boolean z;
            PreferenceScreen preferenceScreen = NotificationSettingsBase.this.getPreferenceScreen();
            for (NotificationPreferenceController notificationPreferenceController : NotificationSettingsBase.this.mControllers) {
                notificationPreferenceController.displayPreference(preferenceScreen);
            }
            NotificationSettingsBase.this.updatePreferenceStates();
            if (NotificationSettingsBase.this.mAppRow != null && !NotificationSettingsBase.this.mAppRow.banned) {
                if (NotificationSettingsBase.this.mChannel == null) {
                    if (NotificationSettingsBase.this.mChannelGroup != null) {
                        z = NotificationSettingsBase.this.mChannelGroup.isBlocked();
                        for (Preference preference : NotificationSettingsBase.this.mDynamicPreferences) {
                            NotificationSettingsBase.this.setVisible(NotificationSettingsBase.this.getPreferenceScreen(), preference, !z);
                        }
                    }
                }
                z = false;
                while (r3.hasNext()) {
                }
            }
            z = true;
            while (r3.hasNext()) {
            }
        }
    }
}
