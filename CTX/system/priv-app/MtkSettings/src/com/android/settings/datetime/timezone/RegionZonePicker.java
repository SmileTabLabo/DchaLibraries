package com.android.settings.datetime.timezone;

import android.content.Intent;
import android.icu.text.Collator;
import android.icu.text.LocaleDisplayNames;
import android.icu.util.TimeZone;
import android.os.Bundle;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.datetime.timezone.TimeZoneInfo;
import com.android.settings.datetime.timezone.model.FilteredCountryTimeZones;
import com.android.settings.datetime.timezone.model.TimeZoneData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;
/* loaded from: classes.dex */
public class RegionZonePicker extends BaseTimeZoneInfoPicker {
    private String mRegionName;

    public RegionZonePicker() {
        super(R.string.date_time_set_timezone_title, R.string.search_settings, true, false);
    }

    @Override // com.android.settingslib.core.instrumentation.Instrumentable
    public int getMetricsCategory() {
        return 1356;
    }

    @Override // com.android.settings.datetime.timezone.BaseTimeZonePicker, com.android.settingslib.core.lifecycle.ObservableFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        String string;
        super.onCreate(bundle);
        LocaleDisplayNames localeDisplayNames = LocaleDisplayNames.getInstance(getLocale());
        if (getArguments() != null) {
            string = getArguments().getString("com.android.settings.datetime.timezone.region_id");
        } else {
            string = null;
        }
        this.mRegionName = string != null ? localeDisplayNames.regionDisplayName(string) : null;
    }

    @Override // com.android.settings.datetime.timezone.BaseTimeZoneInfoPicker
    protected CharSequence getHeaderText() {
        return this.mRegionName;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.datetime.timezone.BaseTimeZoneInfoPicker
    public Intent prepareResultData(TimeZoneInfo timeZoneInfo) {
        Intent prepareResultData = super.prepareResultData(timeZoneInfo);
        prepareResultData.putExtra("com.android.settings.datetime.timezone.result_region_id", getArguments().getString("com.android.settings.datetime.timezone.region_id"));
        return prepareResultData;
    }

    @Override // com.android.settings.datetime.timezone.BaseTimeZoneInfoPicker
    public List<TimeZoneInfo> getAllTimeZoneInfos(TimeZoneData timeZoneData) {
        if (getArguments() == null) {
            Log.e("RegionZoneSearchPicker", "getArguments() == null");
            getActivity().finish();
            return Collections.emptyList();
        }
        String string = getArguments().getString("com.android.settings.datetime.timezone.region_id");
        FilteredCountryTimeZones lookupCountryTimeZones = timeZoneData.lookupCountryTimeZones(string);
        if (lookupCountryTimeZones == null) {
            Log.e("RegionZoneSearchPicker", "region id is not valid: " + string);
            getActivity().finish();
            return Collections.emptyList();
        }
        return getRegionTimeZoneInfo(lookupCountryTimeZones.getTimeZoneIds());
    }

    public List<TimeZoneInfo> getRegionTimeZoneInfo(Collection<String> collection) {
        TimeZoneInfo.Formatter formatter = new TimeZoneInfo.Formatter(getLocale(), new Date());
        TreeSet treeSet = new TreeSet(new TimeZoneInfoComparator(Collator.getInstance(getLocale()), new Date()));
        for (String str : collection) {
            TimeZone frozenTimeZone = TimeZone.getFrozenTimeZone(str);
            if (!frozenTimeZone.getID().equals("Etc/Unknown")) {
                treeSet.add(formatter.format(frozenTimeZone));
            }
        }
        return Collections.unmodifiableList(new ArrayList(treeSet));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static class TimeZoneInfoComparator implements Comparator<TimeZoneInfo> {
        private Collator mCollator;
        private final Date mNow;

        TimeZoneInfoComparator(Collator collator, Date date) {
            this.mCollator = collator;
            this.mNow = date;
        }

        @Override // java.util.Comparator
        public int compare(TimeZoneInfo timeZoneInfo, TimeZoneInfo timeZoneInfo2) {
            int compare = Integer.compare(timeZoneInfo.getTimeZone().getOffset(this.mNow.getTime()), timeZoneInfo2.getTimeZone().getOffset(this.mNow.getTime()));
            if (compare == 0) {
                compare = Integer.compare(timeZoneInfo.getTimeZone().getRawOffset(), timeZoneInfo2.getTimeZone().getRawOffset());
            }
            if (compare == 0) {
                compare = this.mCollator.compare(timeZoneInfo.getExemplarLocation(), timeZoneInfo2.getExemplarLocation());
            }
            if (compare == 0 && timeZoneInfo.getGenericName() != null && timeZoneInfo2.getGenericName() != null) {
                return this.mCollator.compare(timeZoneInfo.getGenericName(), timeZoneInfo2.getGenericName());
            }
            return compare;
        }
    }
}
