package com.android.settings.search;

import android.provider.SearchIndexableResource;
import com.android.settings.DateTimeSettings;
import com.android.settings.DevelopmentSettings;
import com.android.settings.DeviceInfoSettings;
import com.android.settings.DisplaySettings;
import com.android.settings.LegalSettings;
import com.android.settings.PrivacySettings;
import com.android.settings.R;
import com.android.settings.ScreenPinningSettings;
import com.android.settings.SecuritySettings;
import com.android.settings.WallpaperTypeSettings;
import com.android.settings.WirelessSettings;
import com.android.settings.accessibility.AccessibilitySettings;
import com.android.settings.accounts.AccountSettings;
import com.android.settings.applications.AdvancedAppSettings;
import com.android.settings.applications.SpecialAccessSettings;
import com.android.settings.bluetooth.BluetoothSettings;
import com.android.settings.datausage.DataUsageMeteredSettings;
import com.android.settings.datausage.DataUsageSummary;
import com.android.settings.deviceinfo.StorageSettings;
import com.android.settings.display.ScreenZoomSettings;
import com.android.settings.fuelgauge.BatterySaverSettings;
import com.android.settings.fuelgauge.PowerUsageSummary;
import com.android.settings.inputmethod.InputMethodAndLanguageSettings;
import com.android.settings.location.LocationSettings;
import com.android.settings.location.ScanningSettings;
import com.android.settings.notification.ConfigureNotificationSettings;
import com.android.settings.notification.OtherSoundSettings;
import com.android.settings.notification.SoundSettings;
import com.android.settings.notification.ZenModePrioritySettings;
import com.android.settings.notification.ZenModeSettings;
import com.android.settings.notification.ZenModeVisualInterruptionSettings;
import com.android.settings.print.PrintSettingsFragment;
import com.android.settings.sim.SimSettings;
import com.android.settings.users.UserSettings;
import com.android.settings.wifi.AdvancedWifiSettings;
import com.android.settings.wifi.SavedAccessPointsWifiSettings;
import com.android.settings.wifi.WifiSettings;
import com.mediatek.audioprofile.SoundEnhancement;
import com.mediatek.nfc.NfcSettings;
import com.mediatek.search.SearchExt;
import com.mediatek.settings.hotknot.HotKnotSettings;
import java.util.Collection;
import java.util.HashMap;
/* loaded from: classes.dex */
public final class SearchIndexableResources {
    public static int NO_DATA_RES_ID = 0;
    private static HashMap<String, SearchIndexableResource> sResMap = new HashMap<>();

    static {
        sResMap.put(WifiSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(WifiSettings.class.getName()), NO_DATA_RES_ID, WifiSettings.class.getName(), (int) R.drawable.ic_settings_wireless));
        sResMap.put(AdvancedWifiSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(AdvancedWifiSettings.class.getName()), (int) R.xml.wifi_advanced_settings, AdvancedWifiSettings.class.getName(), (int) R.drawable.ic_settings_wireless));
        sResMap.put(SavedAccessPointsWifiSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(SavedAccessPointsWifiSettings.class.getName()), (int) R.xml.wifi_display_saved_access_points, SavedAccessPointsWifiSettings.class.getName(), (int) R.drawable.ic_settings_wireless));
        sResMap.put(BluetoothSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(BluetoothSettings.class.getName()), NO_DATA_RES_ID, BluetoothSettings.class.getName(), (int) R.drawable.ic_settings_bluetooth));
        sResMap.put(SimSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(SimSettings.class.getName()), NO_DATA_RES_ID, SimSettings.class.getName(), (int) R.drawable.ic_sim_sd));
        sResMap.put(DataUsageSummary.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(DataUsageSummary.class.getName()), NO_DATA_RES_ID, DataUsageSummary.class.getName(), (int) R.drawable.ic_settings_data_usage));
        sResMap.put(DataUsageMeteredSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(DataUsageMeteredSettings.class.getName()), NO_DATA_RES_ID, DataUsageMeteredSettings.class.getName(), (int) R.drawable.ic_settings_data_usage));
        sResMap.put(WirelessSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(WirelessSettings.class.getName()), NO_DATA_RES_ID, WirelessSettings.class.getName(), (int) R.drawable.ic_settings_more));
        sResMap.put(ScreenZoomSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(ScreenZoomSettings.class.getName()), NO_DATA_RES_ID, ScreenZoomSettings.class.getName(), (int) R.drawable.ic_settings_display));
        sResMap.put(DisplaySettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(DisplaySettings.class.getName()), NO_DATA_RES_ID, DisplaySettings.class.getName(), (int) R.drawable.ic_settings_display));
        sResMap.put(WallpaperTypeSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(WallpaperTypeSettings.class.getName()), NO_DATA_RES_ID, WallpaperTypeSettings.class.getName(), (int) R.drawable.ic_settings_display));
        sResMap.put(ConfigureNotificationSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(ConfigureNotificationSettings.class.getName()), (int) R.xml.configure_notification_settings, ConfigureNotificationSettings.class.getName(), (int) R.drawable.ic_settings_notifications));
        sResMap.put(SoundSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(SoundSettings.class.getName()), NO_DATA_RES_ID, SoundSettings.class.getName(), (int) R.drawable.ic_settings_sound));
        sResMap.put(OtherSoundSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(OtherSoundSettings.class.getName()), NO_DATA_RES_ID, OtherSoundSettings.class.getName(), (int) R.drawable.ic_settings_sound));
        sResMap.put(SoundEnhancement.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(SoundEnhancement.class.getName()), NO_DATA_RES_ID, SoundEnhancement.class.getName(), (int) R.drawable.ic_settings_sound));
        sResMap.put(ZenModeSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(ZenModeSettings.class.getName()), (int) R.xml.zen_mode_settings, ZenModeSettings.class.getName(), (int) R.drawable.ic_settings_notifications));
        sResMap.put(ZenModePrioritySettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(ZenModePrioritySettings.class.getName()), (int) R.xml.zen_mode_priority_settings, ZenModePrioritySettings.class.getName(), (int) R.drawable.ic_settings_notifications));
        sResMap.put(StorageSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(StorageSettings.class.getName()), NO_DATA_RES_ID, StorageSettings.class.getName(), (int) R.drawable.ic_settings_storage));
        sResMap.put(PowerUsageSummary.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(PowerUsageSummary.class.getName()), (int) R.xml.power_usage_summary, PowerUsageSummary.class.getName(), (int) R.drawable.ic_settings_battery));
        sResMap.put(BatterySaverSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(BatterySaverSettings.class.getName()), (int) R.xml.battery_saver_settings, BatterySaverSettings.class.getName(), (int) R.drawable.ic_settings_battery));
        sResMap.put(AdvancedAppSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(AdvancedAppSettings.class.getName()), NO_DATA_RES_ID, AdvancedAppSettings.class.getName(), (int) R.drawable.ic_settings_applications));
        sResMap.put(SpecialAccessSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(SpecialAccessSettings.class.getName()), (int) R.xml.special_access, SpecialAccessSettings.class.getName(), (int) R.drawable.ic_settings_applications));
        sResMap.put(UserSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(UserSettings.class.getName()), NO_DATA_RES_ID, UserSettings.class.getName(), (int) R.drawable.ic_settings_multiuser));
        sResMap.put(LocationSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(LocationSettings.class.getName()), (int) R.xml.location_settings, LocationSettings.class.getName(), (int) R.drawable.ic_settings_location));
        sResMap.put(ScanningSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(ScanningSettings.class.getName()), (int) R.xml.location_scanning, ScanningSettings.class.getName(), (int) R.drawable.ic_settings_location));
        sResMap.put(SecuritySettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(SecuritySettings.class.getName()), NO_DATA_RES_ID, SecuritySettings.class.getName(), (int) R.drawable.ic_settings_security));
        sResMap.put(ScreenPinningSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(ScreenPinningSettings.class.getName()), NO_DATA_RES_ID, ScreenPinningSettings.class.getName(), (int) R.drawable.ic_settings_security));
        sResMap.put(AccountSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(AccountSettings.class.getName()), NO_DATA_RES_ID, AccountSettings.class.getName(), (int) R.drawable.ic_settings_accounts));
        sResMap.put(InputMethodAndLanguageSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(InputMethodAndLanguageSettings.class.getName()), NO_DATA_RES_ID, InputMethodAndLanguageSettings.class.getName(), (int) R.drawable.ic_settings_language));
        sResMap.put(PrivacySettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(PrivacySettings.class.getName()), NO_DATA_RES_ID, PrivacySettings.class.getName(), (int) R.drawable.ic_settings_backup));
        sResMap.put(DateTimeSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(DateTimeSettings.class.getName()), (int) R.xml.date_time_prefs, DateTimeSettings.class.getName(), (int) R.drawable.ic_settings_date_time));
        sResMap.put(AccessibilitySettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(AccessibilitySettings.class.getName()), NO_DATA_RES_ID, AccessibilitySettings.class.getName(), (int) R.drawable.ic_settings_accessibility));
        sResMap.put(PrintSettingsFragment.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(PrintSettingsFragment.class.getName()), NO_DATA_RES_ID, PrintSettingsFragment.class.getName(), (int) R.drawable.ic_settings_print));
        sResMap.put(DevelopmentSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(DevelopmentSettings.class.getName()), NO_DATA_RES_ID, DevelopmentSettings.class.getName(), (int) R.drawable.ic_settings_development));
        sResMap.put(DeviceInfoSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(DeviceInfoSettings.class.getName()), NO_DATA_RES_ID, DeviceInfoSettings.class.getName(), (int) R.drawable.ic_settings_about));
        sResMap.put(LegalSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(LegalSettings.class.getName()), NO_DATA_RES_ID, LegalSettings.class.getName(), (int) R.drawable.ic_settings_about));
        sResMap.put(ZenModeVisualInterruptionSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(ZenModeVisualInterruptionSettings.class.getName()), (int) R.xml.zen_mode_visual_interruptions_settings, ZenModeVisualInterruptionSettings.class.getName(), (int) R.drawable.ic_settings_notifications));
        sResMap.put(SearchExt.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(SearchExt.class.getName()), NO_DATA_RES_ID, SearchExt.class.getName(), (int) R.mipmap.ic_launcher_settings));
        sResMap.put(HotKnotSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(HotKnotSettings.class.getName()), NO_DATA_RES_ID, HotKnotSettings.class.getName(), (int) R.drawable.ic_settings_hotknot));
        sResMap.put(NfcSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(NfcSettings.class.getName()), NO_DATA_RES_ID, NfcSettings.class.getName(), (int) R.drawable.ic_settings_wireless));
    }

    private SearchIndexableResources() {
    }

    public static SearchIndexableResource getResourceByName(String className) {
        return sResMap.get(className);
    }

    public static Collection<SearchIndexableResource> values() {
        return sResMap.values();
    }
}
