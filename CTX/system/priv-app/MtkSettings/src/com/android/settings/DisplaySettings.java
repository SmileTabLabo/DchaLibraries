package com.android.settings;

import android.content.Context;
import android.provider.SearchIndexableResource;
import com.android.internal.hardware.AmbientDisplayConfiguration;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.display.AmbientDisplayPreferenceController;
import com.android.settings.display.BrightnessLevelPreferenceController;
import com.android.settings.display.CameraGesturePreferenceController;
import com.android.settings.display.ColorModePreferenceController;
import com.android.settings.display.LiftToWakePreferenceController;
import com.android.settings.display.NightDisplayPreferenceController;
import com.android.settings.display.NightModePreferenceController;
import com.android.settings.display.ScreenSaverPreferenceController;
import com.android.settings.display.ShowOperatorNamePreferenceController;
import com.android.settings.display.TapToWakePreferenceController;
import com.android.settings.display.ThemePreferenceController;
import com.android.settings.display.TimeoutPreferenceController;
import com.android.settings.display.VrDisplayPreferenceController;
import com.android.settings.display.WallpaperPreferenceController;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.mediatek.settings.display.AodPreferenceController;
import com.mediatek.settings.display.HdmiPreferenceController;
import com.mediatek.settings.display.MiraVisionPreferenceController;
import java.util.ArrayList;
import java.util.List;
/* loaded from: classes.dex */
public class DisplaySettings extends DashboardFragment {
    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() { // from class: com.android.settings.DisplaySettings.1
        @Override // com.android.settings.search.BaseSearchIndexProvider, com.android.settings.search.Indexable.SearchIndexProvider
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean z) {
            ArrayList arrayList = new ArrayList();
            SearchIndexableResource searchIndexableResource = new SearchIndexableResource(context);
            searchIndexableResource.xmlResId = R.xml.display_settings;
            arrayList.add(searchIndexableResource);
            return arrayList;
        }

        @Override // com.android.settings.search.BaseSearchIndexProvider, com.android.settings.search.Indexable.SearchIndexProvider
        public List<String> getNonIndexableKeys(Context context) {
            List<String> nonIndexableKeys = super.getNonIndexableKeys(context);
            nonIndexableKeys.add("display_settings_screen_zoom");
            nonIndexableKeys.add("wallpaper");
            nonIndexableKeys.add("night_display");
            nonIndexableKeys.add("auto_brightness_entry");
            return nonIndexableKeys;
        }

        @Override // com.android.settings.search.BaseSearchIndexProvider
        public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
            return DisplaySettings.buildPreferenceControllers(context, null);
        }
    };

    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 46;
    }

    @Override // com.android.settings.dashboard.DashboardFragment
    protected String getLogTag() {
        return "DisplaySettings";
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.dashboard.DashboardFragment, com.android.settings.core.InstrumentedPreferenceFragment
    public int getPreferenceScreenResId() {
        return R.xml.display_settings;
    }

    @Override // com.android.settings.dashboard.DashboardFragment
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getLifecycle());
    }

    @Override // com.android.settings.support.actionbar.HelpResourceProvider
    public int getHelpResource() {
        return R.string.help_uri_display;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static List<AbstractPreferenceController> buildPreferenceControllers(Context context, Lifecycle lifecycle) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(new CameraGesturePreferenceController(context));
        arrayList.add(new LiftToWakePreferenceController(context));
        arrayList.add(new NightDisplayPreferenceController(context));
        arrayList.add(new NightModePreferenceController(context));
        arrayList.add(new ScreenSaverPreferenceController(context));
        arrayList.add(new AmbientDisplayPreferenceController(context, new AmbientDisplayConfiguration(context), "ambient_display"));
        arrayList.add(new TapToWakePreferenceController(context));
        arrayList.add(new TimeoutPreferenceController(context, "screen_timeout"));
        arrayList.add(new VrDisplayPreferenceController(context));
        arrayList.add(new ShowOperatorNamePreferenceController(context));
        arrayList.add(new WallpaperPreferenceController(context));
        arrayList.add(new ThemePreferenceController(context));
        arrayList.add(new BrightnessLevelPreferenceController(context, lifecycle));
        arrayList.add(new ColorModePreferenceController(context));
        arrayList.add(new MiraVisionPreferenceController(context));
        arrayList.add(new AodPreferenceController(context));
        arrayList.add(new HdmiPreferenceController(context));
        return arrayList;
    }
}
