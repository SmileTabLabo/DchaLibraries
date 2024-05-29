package com.android.settings.notification;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.R;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.ArrayList;
import java.util.List;
/* loaded from: classes.dex */
public class ChannelNotificationSettings extends NotificationSettingsBase {
    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 265;
    }

    @Override // com.android.settings.notification.NotificationSettingsBase, com.android.settings.dashboard.DashboardFragment, com.android.settings.SettingsPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        Bundle arguments = getArguments();
        if (preferenceScreen != null && arguments != null && !arguments.getBoolean("fromSettings", false)) {
            preferenceScreen.setInitialExpandedChildrenCount(Preference.DEFAULT_ORDER);
        }
    }

    @Override // com.android.settings.notification.NotificationSettingsBase, com.android.settings.dashboard.DashboardFragment, com.android.settings.SettingsPreferenceFragment, com.android.settings.core.InstrumentedPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        if (this.mUid < 0 || TextUtils.isEmpty(this.mPkg) || this.mPkgInfo == null || this.mChannel == null) {
            Log.w("ChannelSettings", "Missing package or uid or packageinfo or channel");
            finish();
            return;
        }
        for (NotificationPreferenceController notificationPreferenceController : this.mControllers) {
            notificationPreferenceController.onResume(this.mAppRow, this.mChannel, this.mChannelGroup, this.mSuspendedAppsAdmin);
            notificationPreferenceController.displayPreference(getPreferenceScreen());
        }
        updatePreferenceStates();
    }

    @Override // android.app.Fragment
    public void onActivityResult(int i, int i2, Intent intent) {
        for (NotificationPreferenceController notificationPreferenceController : this.mControllers) {
            if (notificationPreferenceController instanceof PreferenceManager.OnActivityResultListener) {
                ((PreferenceManager.OnActivityResultListener) notificationPreferenceController).onActivityResult(i, i2, intent);
            }
        }
    }

    @Override // com.android.settings.dashboard.DashboardFragment
    protected String getLogTag() {
        return "ChannelSettings";
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.dashboard.DashboardFragment, com.android.settings.core.InstrumentedPreferenceFragment
    public int getPreferenceScreenResId() {
        return R.xml.channel_notification_settings;
    }

    @Override // com.android.settings.dashboard.DashboardFragment
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        this.mControllers = new ArrayList();
        this.mControllers.add(new HeaderPreferenceController(context, this));
        this.mControllers.add(new BlockPreferenceController(context, this.mImportanceListener, this.mBackend));
        this.mControllers.add(new ImportancePreferenceController(context, this.mImportanceListener, this.mBackend));
        this.mControllers.add(new AllowSoundPreferenceController(context, this.mImportanceListener, this.mBackend));
        this.mControllers.add(new SoundPreferenceController(context, this, this.mImportanceListener, this.mBackend));
        this.mControllers.add(new VibrationPreferenceController(context, this.mBackend));
        this.mControllers.add(new AppLinkPreferenceController(context));
        this.mControllers.add(new DescriptionPreferenceController(context));
        this.mControllers.add(new VisibilityPreferenceController(context, new LockPatternUtils(context), this.mBackend));
        this.mControllers.add(new LightsPreferenceController(context, this.mBackend));
        this.mControllers.add(new BadgePreferenceController(context, this.mBackend));
        this.mControllers.add(new DndPreferenceController(context, this.mBackend));
        this.mControllers.add(new NotificationsOffPreferenceController(context));
        return new ArrayList(this.mControllers);
    }
}
