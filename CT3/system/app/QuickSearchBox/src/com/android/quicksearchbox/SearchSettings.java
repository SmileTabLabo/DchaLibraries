package com.android.quicksearchbox;
/* loaded from: a.zip:com/android/quicksearchbox/SearchSettings.class */
public interface SearchSettings {
    String getSearchBaseDomain();

    long getSearchBaseDomainApplyTime();

    void setSearchBaseDomain(String str);

    boolean shouldUseGoogleCom();

    void upgradeSettingsIfNeeded();
}
