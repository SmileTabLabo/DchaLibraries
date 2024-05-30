package com.android.settings.datetime.timezone.model;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import libcore.util.CountryTimeZones;
/* loaded from: classes.dex */
public class FilteredCountryTimeZones {
    private final CountryTimeZones mCountryTimeZones;
    private final List<String> mTimeZoneIds;

    public FilteredCountryTimeZones(CountryTimeZones countryTimeZones) {
        this.mCountryTimeZones = countryTimeZones;
        this.mTimeZoneIds = Collections.unmodifiableList((List) countryTimeZones.getTimeZoneMappings().stream().filter(new Predicate() { // from class: com.android.settings.datetime.timezone.model.-$$Lambda$FilteredCountryTimeZones$4MxYnMuZMfSQu2iAD-J0AM_CAoE
            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return FilteredCountryTimeZones.lambda$new$0((CountryTimeZones.TimeZoneMapping) obj);
            }
        }).map(new Function() { // from class: com.android.settings.datetime.timezone.model.-$$Lambda$FilteredCountryTimeZones$ISUVeCzEqV6U2C82Sgby5UdDf3Y
            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                String str;
                str = ((CountryTimeZones.TimeZoneMapping) obj).timeZoneId;
                return str;
            }
        }).collect(Collectors.toList()));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ boolean lambda$new$0(CountryTimeZones.TimeZoneMapping timeZoneMapping) {
        return timeZoneMapping.showInPicker && (timeZoneMapping.notUsedAfter == null || timeZoneMapping.notUsedAfter.longValue() >= 1514764800000L);
    }

    public List<String> getTimeZoneIds() {
        return this.mTimeZoneIds;
    }

    public String getRegionId() {
        return TimeZoneData.normalizeRegionId(this.mCountryTimeZones.getCountryIso());
    }
}
