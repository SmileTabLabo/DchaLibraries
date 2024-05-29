package com.android.settings.display;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import com.android.settings.PreviewSeekBarPreferenceFragment;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settingslib.display.DisplayDensityUtils;
import java.util.ArrayList;
import java.util.List;
/* loaded from: classes.dex */
public class ScreenZoomSettings extends PreviewSeekBarPreferenceFragment implements Indexable {
    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() { // from class: com.android.settings.display.ScreenZoomSettings.1
        @Override // com.android.settings.search.BaseSearchIndexProvider, com.android.settings.search.Indexable.SearchIndexProvider
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            Resources res = context.getResources();
            SearchIndexableRaw data = new SearchIndexableRaw(context);
            data.title = res.getString(R.string.screen_zoom_title);
            data.screenTitle = res.getString(R.string.screen_zoom_title);
            data.keywords = res.getString(R.string.screen_zoom_keywords);
            List<SearchIndexableRaw> result = new ArrayList<>(1);
            result.add(data);
            return result;
        }
    };
    private int mDefaultDensity;
    private int[] mValues;

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mActivityLayoutResId = R.layout.screen_zoom_activity;
        this.mPreviewSampleResIds = new int[]{R.layout.screen_zoom_preview_1, R.layout.screen_zoom_preview_2, R.layout.screen_zoom_preview_settings};
        DisplayDensityUtils density = new DisplayDensityUtils(getContext());
        int initialIndex = density.getCurrentIndex();
        if (initialIndex < 0) {
            int densityDpi = getResources().getDisplayMetrics().densityDpi;
            this.mValues = new int[]{densityDpi};
            this.mEntries = new String[]{getString(DisplayDensityUtils.SUMMARY_DEFAULT)};
            this.mInitialIndex = 0;
            this.mDefaultDensity = densityDpi;
            return;
        }
        this.mValues = density.getValues();
        this.mEntries = density.getEntries();
        this.mInitialIndex = initialIndex;
        this.mDefaultDensity = density.getDefaultDensity();
    }

    @Override // com.android.settings.PreviewSeekBarPreferenceFragment
    protected Configuration createConfig(Configuration origConfig, int index) {
        Configuration config = new Configuration(origConfig);
        config.densityDpi = this.mValues[index];
        return config;
    }

    @Override // com.android.settings.PreviewSeekBarPreferenceFragment
    protected void commit() {
        int densityDpi = this.mValues[this.mCurrentIndex];
        if (densityDpi == this.mDefaultDensity) {
            DisplayDensityUtils.clearForcedDisplayDensity(0);
        } else {
            DisplayDensityUtils.setForcedDisplayDensity(0, densityDpi);
        }
    }

    @Override // com.android.settings.InstrumentedPreferenceFragment
    protected int getMetricsCategory() {
        return 339;
    }
}
