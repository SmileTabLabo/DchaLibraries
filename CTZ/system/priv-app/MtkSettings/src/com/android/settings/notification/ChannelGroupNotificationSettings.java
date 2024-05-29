package com.android.settings.notification;

import android.app.NotificationChannel;
import android.content.Context;
import android.support.v7.preference.Preference;
import android.util.Log;
import com.android.settings.R;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/* loaded from: classes.dex */
public class ChannelGroupNotificationSettings extends NotificationSettingsBase {
    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 1218;
    }

    @Override // com.android.settings.notification.NotificationSettingsBase, com.android.settings.dashboard.DashboardFragment, com.android.settings.SettingsPreferenceFragment, com.android.settings.core.InstrumentedPreferenceFragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        if (this.mAppRow == null || this.mChannelGroup == null) {
            Log.w("ChannelGroupSettings", "Missing package or uid or packageinfo or group");
            finish();
            return;
        }
        populateChannelList();
        for (NotificationPreferenceController notificationPreferenceController : this.mControllers) {
            notificationPreferenceController.onResume(this.mAppRow, this.mChannel, this.mChannelGroup, this.mSuspendedAppsAdmin);
            notificationPreferenceController.displayPreference(getPreferenceScreen());
        }
        updatePreferenceStates();
    }

    @Override // com.android.settings.dashboard.DashboardFragment
    protected String getLogTag() {
        return "ChannelGroupSettings";
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.dashboard.DashboardFragment, com.android.settings.core.InstrumentedPreferenceFragment
    public int getPreferenceScreenResId() {
        return R.xml.notification_group_settings;
    }

    @Override // com.android.settings.dashboard.DashboardFragment
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        this.mControllers = new ArrayList();
        this.mControllers.add(new HeaderPreferenceController(context, this));
        this.mControllers.add(new BlockPreferenceController(context, this.mImportanceListener, this.mBackend));
        this.mControllers.add(new AppLinkPreferenceController(context));
        this.mControllers.add(new NotificationsOffPreferenceController(context));
        this.mControllers.add(new DescriptionPreferenceController(context));
        return new ArrayList(this.mControllers);
    }

    private void populateChannelList() {
        if (!this.mDynamicPreferences.isEmpty()) {
            Log.w("ChannelGroupSettings", "Notification channel group posted twice to settings - old size " + this.mDynamicPreferences.size() + ", new size " + this.mDynamicPreferences.size());
            for (Preference preference : this.mDynamicPreferences) {
                getPreferenceScreen().removePreference(preference);
            }
        }
        if (this.mChannelGroup.getChannels().isEmpty()) {
            Preference preference2 = new Preference(getPrefContext());
            preference2.setTitle(R.string.no_channels);
            preference2.setEnabled(false);
            getPreferenceScreen().addPreference(preference2);
            this.mDynamicPreferences.add(preference2);
            return;
        }
        List<NotificationChannel> channels = this.mChannelGroup.getChannels();
        Collections.sort(channels, this.mChannelComparator);
        for (NotificationChannel notificationChannel : channels) {
            this.mDynamicPreferences.add(populateSingleChannelPrefs(getPreferenceScreen(), notificationChannel, this.mChannelGroup.isBlocked()));
        }
    }
}
