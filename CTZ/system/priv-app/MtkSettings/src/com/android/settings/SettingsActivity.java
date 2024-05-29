package com.android.settings;

import android.app.ActionBar;
import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v14.preference.PreferenceFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.text.TextUtils;
import android.transition.TransitionManager;
import android.util.FeatureFlagUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toolbar;
import com.android.internal.util.ArrayUtils;
import com.android.settings.Settings;
import com.android.settings.applications.manageapplications.ManageApplications;
import com.android.settings.backup.BackupSettingsActivity;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.core.gateway.SettingsGateway;
import com.android.settings.dashboard.DashboardFeatureProvider;
import com.android.settings.dashboard.DashboardSummary;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.search.DeviceIndexFeatureProvider;
import com.android.settings.wfd.WifiDisplaySettings;
import com.android.settings.widget.SwitchBar;
import com.android.settingslib.core.instrumentation.Instrumentable;
import com.android.settingslib.core.instrumentation.SharedPreferencesLogger;
import com.android.settingslib.development.DevelopmentSettingsEnabler;
import com.android.settingslib.drawer.DashboardCategory;
import com.android.settingslib.drawer.SettingsDrawerActivity;
import com.android.settingslib.utils.ThreadUtils;
import java.util.ArrayList;
import java.util.List;
/* loaded from: classes.dex */
public class SettingsActivity extends SettingsDrawerActivity implements FragmentManager.OnBackStackChangedListener, PreferenceFragment.OnPreferenceStartFragmentCallback, PreferenceManager.OnPreferenceTreeClickListener, ButtonBarHandler {
    private ViewGroup mContent;
    private DashboardFeatureProvider mDashboardFeatureProvider;
    private BroadcastReceiver mDevelopmentSettingsListener;
    private String mFragmentClass;
    private CharSequence mInitialTitle;
    private int mInitialTitleResId;
    private boolean mIsShowingDashboard;
    private Button mNextButton;
    private SwitchBar mSwitchBar;
    private boolean mBatteryPresent = true;
    private BroadcastReceiver mBatteryInfoReceiver = new BroadcastReceiver() { // from class: com.android.settings.SettingsActivity.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            boolean isBatteryPresent;
            if ("android.intent.action.BATTERY_CHANGED".equals(intent.getAction()) && SettingsActivity.this.mBatteryPresent != (isBatteryPresent = Utils.isBatteryPresent(intent))) {
                SettingsActivity.this.mBatteryPresent = isBatteryPresent;
                SettingsActivity.this.updateTilesList();
            }
        }
    };
    private ArrayList<DashboardCategory> mCategories = new ArrayList<>();

    public SwitchBar getSwitchBar() {
        return this.mSwitchBar;
    }

    @Override // android.support.v14.preference.PreferenceFragment.OnPreferenceStartFragmentCallback
    public boolean onPreferenceStartFragment(PreferenceFragment preferenceFragment, Preference preference) {
        int i;
        SubSettingLauncher arguments = new SubSettingLauncher(this).setDestination(preference.getFragment()).setArguments(preference.getExtras());
        if (preferenceFragment instanceof Instrumentable) {
            i = ((Instrumentable) preferenceFragment).getMetricsCategory();
        } else {
            i = 0;
        }
        arguments.setSourceMetricsCategory(i).setTitle(-1).launch();
        return true;
    }

    @Override // android.support.v7.preference.PreferenceManager.OnPreferenceTreeClickListener
    public boolean onPreferenceTreeClick(Preference preference) {
        return false;
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public SharedPreferences getSharedPreferences(String str, int i) {
        if (str.equals(getPackageName() + "_preferences")) {
            return new SharedPreferencesLogger(this, getMetricsTag(), FeatureFactory.getFactory(this).getMetricsFeatureProvider());
        }
        return super.getSharedPreferences(str, i);
    }

    private String getMetricsTag() {
        String name = getClass().getName();
        if (getIntent() != null && getIntent().hasExtra(":settings:show_fragment")) {
            name = getIntent().getStringExtra(":settings:show_fragment");
        }
        if (name.startsWith("com.android.settings.")) {
            return name.replace("com.android.settings.", "");
        }
        return name;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settingslib.drawer.SettingsDrawerActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        View findViewById;
        int i;
        super.onCreate(bundle);
        Log.d("SettingsActivity", "Starting onCreate");
        if (isLockTaskModePinned() && !isSettingsRunOnTop() && !isLaunchableInTaskModePinned()) {
            Log.w("SettingsActivity", "Devices lock task mode pinned.");
            finish();
        }
        if (!isFinishing() && Utils.isMonkeyRunning()) {
            Log.d("SettingsActivity", "finish due to monkey user");
            finish();
            return;
        }
        System.currentTimeMillis();
        this.mDashboardFeatureProvider = FeatureFactory.getFactory(this).getDashboardFeatureProvider(this);
        getMetaData();
        Intent intent = getIntent();
        if (intent.hasExtra("settings:ui_options")) {
            getWindow().setUiOptions(intent.getIntExtra("settings:ui_options", 0));
        }
        String stringExtra = intent.getStringExtra(":settings:show_fragment");
        intent.getComponent().getClassName();
        this.mIsShowingDashboard = false;
        boolean z = (this instanceof SubSettings) || intent.getBooleanExtra(":settings:show_fragment_as_subsetting", false);
        if (z) {
            setTheme(2131952096);
        }
        setContentView(this.mIsShowingDashboard ? R.layout.settings_main_dashboard : R.layout.settings_main_prefs);
        this.mContent = (ViewGroup) findViewById(R.id.main_content);
        getFragmentManager().addOnBackStackChangedListener(this);
        if (bundle != null) {
            setTitleFromIntent(intent);
            ArrayList parcelableArrayList = bundle.getParcelableArrayList(":settings:categories");
            if (parcelableArrayList != null) {
                this.mCategories.clear();
                this.mCategories.addAll(parcelableArrayList);
                setTitleFromBackStack();
            }
        } else {
            launchSettingFragment(stringExtra, z, intent);
        }
        boolean isDeviceProvisioned = Utils.isDeviceProvisioned(this);
        if (this.mIsShowingDashboard) {
            View findViewById2 = findViewById(R.id.search_bar);
            if (isDeviceProvisioned) {
                i = 0;
            } else {
                i = 4;
            }
            findViewById2.setVisibility(i);
            findViewById(R.id.action_bar).setVisibility(8);
            Toolbar toolbar = (Toolbar) findViewById(R.id.search_action_bar);
            FeatureFactory.getFactory(this).getSearchFeatureProvider().initSearchToolbar(this, toolbar);
            setActionBar(toolbar);
            View navigationView = toolbar.getNavigationView();
            navigationView.setClickable(false);
            navigationView.setImportantForAccessibility(2);
            navigationView.setBackground(null);
        }
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(isDeviceProvisioned);
            actionBar.setHomeButtonEnabled(isDeviceProvisioned);
            actionBar.setDisplayShowTitleEnabled(!this.mIsShowingDashboard);
        }
        this.mSwitchBar = (SwitchBar) findViewById(R.id.switch_bar);
        if (this.mSwitchBar != null) {
            this.mSwitchBar.setMetricsTag(getMetricsTag());
        }
        if (intent.getBooleanExtra("extra_prefs_show_button_bar", false) && (findViewById = findViewById(R.id.button_bar)) != null) {
            findViewById.setVisibility(0);
            Button button = (Button) findViewById(R.id.back_button);
            button.setOnClickListener(new View.OnClickListener() { // from class: com.android.settings.SettingsActivity.2
                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    SettingsActivity.this.setResult(0, null);
                    SettingsActivity.this.finish();
                }
            });
            Button button2 = (Button) findViewById(R.id.skip_button);
            button2.setOnClickListener(new View.OnClickListener() { // from class: com.android.settings.SettingsActivity.3
                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    SettingsActivity.this.setResult(-1, null);
                    SettingsActivity.this.finish();
                }
            });
            this.mNextButton = (Button) findViewById(R.id.next_button);
            this.mNextButton.setOnClickListener(new View.OnClickListener() { // from class: com.android.settings.SettingsActivity.4
                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    SettingsActivity.this.setResult(-1, null);
                    SettingsActivity.this.finish();
                }
            });
            if (intent.hasExtra("extra_prefs_set_next_text")) {
                String stringExtra2 = intent.getStringExtra("extra_prefs_set_next_text");
                if (TextUtils.isEmpty(stringExtra2)) {
                    this.mNextButton.setVisibility(8);
                } else {
                    this.mNextButton.setText(stringExtra2);
                }
            }
            if (intent.hasExtra("extra_prefs_set_back_text")) {
                String stringExtra3 = intent.getStringExtra("extra_prefs_set_back_text");
                if (TextUtils.isEmpty(stringExtra3)) {
                    button.setVisibility(8);
                } else {
                    button.setText(stringExtra3);
                }
            }
            if (intent.getBooleanExtra("extra_prefs_show_skip", false)) {
                button2.setVisibility(0);
            }
        }
    }

    void launchSettingFragment(String str, boolean z, Intent intent) {
        if (!this.mIsShowingDashboard && str != null) {
            setTitleFromIntent(intent);
            switchToFragment(str, intent.getBundleExtra(":settings:show_fragment_args"), true, false, this.mInitialTitleResId, this.mInitialTitle, false);
            return;
        }
        this.mInitialTitleResId = R.string.dashboard_title;
        switchToFragment(DashboardSummary.class.getName(), null, false, false, this.mInitialTitleResId, this.mInitialTitle, false);
    }

    private void setTitleFromIntent(Intent intent) {
        Log.d("SettingsActivity", "Starting to set activity title");
        int intExtra = intent.getIntExtra(":settings:show_fragment_title_resid", -1);
        if (intExtra > 0) {
            this.mInitialTitle = null;
            this.mInitialTitleResId = intExtra;
            String stringExtra = intent.getStringExtra(":settings:show_fragment_title_res_package_name");
            if (stringExtra != null) {
                try {
                    this.mInitialTitle = createPackageContextAsUser(stringExtra, 0, new UserHandle(UserHandle.myUserId())).getResources().getText(this.mInitialTitleResId);
                    setTitle(this.mInitialTitle);
                    this.mInitialTitleResId = -1;
                    return;
                } catch (PackageManager.NameNotFoundException e) {
                    Log.w("SettingsActivity", "Could not find package" + stringExtra);
                }
            } else {
                setTitle(this.mInitialTitleResId);
            }
        } else {
            this.mInitialTitleResId = -1;
            CharSequence stringExtra2 = intent.getStringExtra(":settings:show_fragment_title");
            if (stringExtra2 == null) {
                stringExtra2 = getTitle();
            }
            this.mInitialTitle = stringExtra2;
            setTitle(this.mInitialTitle);
        }
        Log.d("SettingsActivity", "Done setting title");
    }

    @Override // android.app.FragmentManager.OnBackStackChangedListener
    public void onBackStackChanged() {
        setTitleFromBackStack();
    }

    private void setTitleFromBackStack() {
        int backStackEntryCount = getFragmentManager().getBackStackEntryCount();
        if (backStackEntryCount == 0) {
            if (this.mInitialTitleResId > 0) {
                setTitle(this.mInitialTitleResId);
                return;
            } else {
                setTitle(this.mInitialTitle);
                return;
            }
        }
        setTitleFromBackStackEntry(getFragmentManager().getBackStackEntryAt(backStackEntryCount - 1));
    }

    private void setTitleFromBackStackEntry(FragmentManager.BackStackEntry backStackEntry) {
        CharSequence breadCrumbTitle;
        int breadCrumbTitleRes = backStackEntry.getBreadCrumbTitleRes();
        if (breadCrumbTitleRes > 0) {
            breadCrumbTitle = getText(breadCrumbTitleRes);
        } else {
            breadCrumbTitle = backStackEntry.getBreadCrumbTitle();
        }
        if (breadCrumbTitle != null) {
            setTitle(breadCrumbTitle);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        saveState(bundle);
    }

    void saveState(Bundle bundle) {
        if (this.mCategories.size() > 0) {
            bundle.putParcelableArrayList(":settings:categories", this.mCategories);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settingslib.drawer.SettingsDrawerActivity, android.app.Activity
    public void onResume() {
        super.onResume();
        this.mDevelopmentSettingsListener = new BroadcastReceiver() { // from class: com.android.settings.SettingsActivity.5
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                SettingsActivity.this.updateTilesList();
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(this.mDevelopmentSettingsListener, new IntentFilter("com.android.settingslib.development.DevelopmentSettingsEnabler.SETTINGS_CHANGED"));
        registerReceiver(this.mBatteryInfoReceiver, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        updateTilesList();
        updateDeviceIndex();
    }

    @Override // com.android.settingslib.drawer.SettingsDrawerActivity, android.app.Activity
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mDevelopmentSettingsListener);
        this.mDevelopmentSettingsListener = null;
        unregisterReceiver(this.mBatteryInfoReceiver);
    }

    @Override // android.app.Activity
    public void setTaskDescription(ActivityManager.TaskDescription taskDescription) {
        taskDescription.setIcon(getBitmapFromXmlResource(R.drawable.ic_launcher_settings));
        super.setTaskDescription(taskDescription);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isValidFragment(String str) {
        for (int i = 0; i < SettingsGateway.ENTRY_FRAGMENTS.length; i++) {
            if (SettingsGateway.ENTRY_FRAGMENTS[i].equals(str)) {
                return true;
            }
        }
        return false;
    }

    @Override // android.app.Activity
    public Intent getIntent() {
        Bundle bundle;
        Intent intent = super.getIntent();
        String startingFragmentClass = getStartingFragmentClass(intent);
        if (startingFragmentClass != null) {
            Intent intent2 = new Intent(intent);
            intent2.putExtra(":settings:show_fragment", startingFragmentClass);
            Bundle bundleExtra = intent.getBundleExtra(":settings:show_fragment_args");
            if (bundleExtra != null) {
                bundle = new Bundle(bundleExtra);
            } else {
                bundle = new Bundle();
            }
            bundle.putParcelable("intent", intent);
            intent2.putExtra(":settings:show_fragment_args", bundle);
            return intent2;
        }
        return intent;
    }

    private String getStartingFragmentClass(Intent intent) {
        if (this.mFragmentClass != null) {
            return this.mFragmentClass;
        }
        String className = intent.getComponent().getClassName();
        if (className.equals(getClass().getName())) {
            return null;
        }
        if ("com.android.settings.RunningServices".equals(className) || "com.android.settings.applications.StorageUse".equals(className)) {
            return ManageApplications.class.getName();
        }
        return className;
    }

    public void finishPreferencePanel(int i, Intent intent) {
        setResult(i, intent);
        finish();
    }

    private Fragment switchToFragment(String str, Bundle bundle, boolean z, boolean z2, int i, CharSequence charSequence, boolean z3) {
        Log.d("SettingsActivity", "Switching to fragment " + str);
        if (z && !isValidFragment(str)) {
            throw new IllegalArgumentException("Invalid fragment for this activity: " + str);
        }
        Fragment instantiate = Fragment.instantiate(this, str, bundle);
        FragmentTransaction beginTransaction = getFragmentManager().beginTransaction();
        beginTransaction.replace(R.id.main_content, instantiate);
        if (z3) {
            TransitionManager.beginDelayedTransition(this.mContent);
        }
        if (z2) {
            beginTransaction.addToBackStack(":settings:prefs");
        }
        if (i > 0) {
            beginTransaction.setBreadCrumbTitle(i);
        } else if (charSequence != null) {
            beginTransaction.setBreadCrumbTitle(charSequence);
        }
        beginTransaction.commitAllowingStateLoss();
        getFragmentManager().executePendingTransactions();
        Log.d("SettingsActivity", "Executed frag manager pendingTransactions");
        return instantiate;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateTilesList() {
        AsyncTask.execute(new Runnable() { // from class: com.android.settings.SettingsActivity.6
            @Override // java.lang.Runnable
            public void run() {
                SettingsActivity.this.doUpdateTilesList();
            }
        });
    }

    private void updateDeviceIndex() {
        final DeviceIndexFeatureProvider deviceIndexFeatureProvider = FeatureFactory.getFactory(this).getDeviceIndexFeatureProvider();
        ThreadUtils.postOnBackgroundThread(new Runnable() { // from class: com.android.settings.-$$Lambda$SettingsActivity$HXMcoLHGNmdxTucTgqvnp3fY_K8
            @Override // java.lang.Runnable
            public final void run() {
                deviceIndexFeatureProvider.updateIndex(SettingsActivity.this, false);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void doUpdateTilesList() {
        boolean z;
        PackageManager packageManager = getPackageManager();
        UserManager userManager = UserManager.get(this);
        boolean isAdminUser = userManager.isAdminUser();
        FeatureFactory factory = FeatureFactory.getFactory(this);
        String packageName = getPackageName();
        StringBuilder sb = new StringBuilder();
        boolean z2 = setTileEnabled(sb, new ComponentName(packageName, Settings.PowerUsageSummaryActivity.class.getName()), this.mBatteryPresent, isAdminUser) || (setTileEnabled(sb, new ComponentName(packageName, Settings.SimSettingsActivity.class.getName()), Utils.showSimCardTile(this), isAdminUser) || (setTileEnabled(sb, new ComponentName(packageName, Settings.ConnectedDeviceDashboardActivity.class.getName()), UserManager.isDeviceInDemoMode(this) ^ true, isAdminUser) || (setTileEnabled(sb, new ComponentName(packageName, Settings.DataUsageSummaryActivity.class.getName()), Utils.isBandwidthControlEnabled(), isAdminUser) || (setTileEnabled(sb, new ComponentName(packageName, Settings.BluetoothSettingsActivity.class.getName()), packageManager.hasSystemFeature("android.hardware.bluetooth"), isAdminUser) || (setTileEnabled(sb, new ComponentName(packageName, Settings.WifiSettingsActivity.class.getName()), packageManager.hasSystemFeature("android.hardware.wifi"), isAdminUser))))));
        boolean isEnabled = FeatureFlagUtils.isEnabled(this, "settings_data_usage_v2");
        boolean z3 = setTileEnabled(sb, new ComponentName(packageName, Settings.DateTimeSettingsActivity.class.getName()), UserManager.isDeviceInDemoMode(this) ^ true, isAdminUser) || (setTileEnabled(sb, new ComponentName(packageName, Settings.NetworkDashboardActivity.class.getName()), UserManager.isDeviceInDemoMode(this) ^ true, isAdminUser) || (setTileEnabled(sb, new ComponentName(packageName, Settings.UserSettingsActivity.class.getName()), UserManager.supportsMultipleUsers() && !Utils.isMonkeyRunning(), isAdminUser) || (setTileEnabled(sb, new ComponentName(packageName, Settings.DataUsageSummaryLegacyActivity.class.getName()), Utils.isBandwidthControlEnabled() && !isEnabled, isAdminUser) || (setTileEnabled(sb, new ComponentName(packageName, Settings.DataUsageSummaryActivity.class.getName()), Utils.isBandwidthControlEnabled() && isEnabled, isAdminUser) || z2))));
        boolean z4 = DevelopmentSettingsEnabler.isDevelopmentSettingsEnabled(this) && !Utils.isMonkeyRunning();
        boolean z5 = userManager.isAdminUser() || userManager.isDemoUser();
        boolean z6 = setTileEnabled(sb, new ComponentName(packageName, Settings.WifiDisplaySettingsActivity.class.getName()), WifiDisplaySettings.isAvailable(this), isAdminUser) || (setTileEnabled(sb, new ComponentName(packageName, BackupSettingsActivity.class.getName()), true, isAdminUser) || (setTileEnabled(sb, new ComponentName(packageName, Settings.DevelopmentSettingsDashboardActivity.class.getName()), z4, z5) || z3));
        boolean isAboutPhoneV2Enabled = factory.getAccountFeatureProvider().isAboutPhoneV2Enabled(this);
        boolean z7 = setTileEnabled(sb, new ComponentName(packageName, Settings.DeviceInfoSettingsActivity.class.getName()), isAboutPhoneV2Enabled ^ true, isAdminUser) || (setTileEnabled(sb, new ComponentName(packageName, Settings.MyDeviceInfoActivity.class.getName()), isAboutPhoneV2Enabled, isAdminUser) || z6);
        if (!isAdminUser) {
            List<DashboardCategory> allCategories = this.mDashboardFeatureProvider.getAllCategories();
            synchronized (allCategories) {
                for (DashboardCategory dashboardCategory : allCategories) {
                    int tilesCount = dashboardCategory.getTilesCount();
                    boolean z8 = z7;
                    for (int i = 0; i < tilesCount; i++) {
                        ComponentName component = dashboardCategory.getTile(i).intent.getComponent();
                        String className = component.getClassName();
                        if (!ArrayUtils.contains(SettingsGateway.SETTINGS_FOR_RESTRICTED, className) && (!z5 || !Settings.DevelopmentSettingsDashboardActivity.class.getName().equals(className))) {
                            z = false;
                            if (packageName.equals(component.getPackageName()) && !z) {
                                if (!setTileEnabled(sb, component, false, isAdminUser) && !z8) {
                                    z8 = false;
                                }
                                z8 = true;
                            }
                        }
                        z = true;
                        if (packageName.equals(component.getPackageName())) {
                            if (!setTileEnabled(sb, component, false, isAdminUser)) {
                                z8 = false;
                            }
                            z8 = true;
                        }
                    }
                    z7 = z8;
                }
            }
        }
        if (!z7) {
            Log.d("SettingsActivity", "No enabled state changed, skipping updateCategory call");
            return;
        }
        Log.d("SettingsActivity", "Enabled state changed for some tiles, reloading all categories " + sb.toString());
        updateCategories();
    }

    private boolean setTileEnabled(StringBuilder sb, ComponentName componentName, boolean z, boolean z2) {
        if (!z2 && getPackageName().equals(componentName.getPackageName()) && !ArrayUtils.contains(SettingsGateway.SETTINGS_FOR_RESTRICTED, componentName.getClassName())) {
            z = false;
        }
        boolean tileEnabled = setTileEnabled(componentName, z);
        if (tileEnabled) {
            sb.append(componentName.toShortString());
            sb.append(",");
        }
        return tileEnabled;
    }

    private void getMetaData() {
        try {
            ActivityInfo activityInfo = getPackageManager().getActivityInfo(getComponentName(), 128);
            if (activityInfo != null && activityInfo.metaData != null) {
                this.mFragmentClass = activityInfo.metaData.getString("com.android.settings.FRAGMENT_CLASS");
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("SettingsActivity", "Cannot get Metadata for: " + getComponentName().toString());
        }
    }

    @Override // com.android.settings.ButtonBarHandler
    public boolean hasNextButton() {
        return this.mNextButton != null;
    }

    @Override // com.android.settings.ButtonBarHandler
    public Button getNextButton() {
        return this.mNextButton;
    }

    public boolean isLaunchableInTaskModePinned() {
        return false;
    }

    Bitmap getBitmapFromXmlResource(int i) {
        Drawable drawable = getResources().getDrawable(i, getTheme());
        Canvas canvas = new Canvas();
        Bitmap createBitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(createBitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return createBitmap;
    }

    private boolean isLockTaskModePinned() {
        return ((ActivityManager) getApplicationContext().getSystemService(ActivityManager.class)).getLockTaskModeState() == 2;
    }

    private boolean isSettingsRunOnTop() {
        return TextUtils.equals(getPackageName(), ((ActivityManager) getApplicationContext().getSystemService(ActivityManager.class)).getRunningTasks(1).get(0).baseActivity.getPackageName());
    }
}
