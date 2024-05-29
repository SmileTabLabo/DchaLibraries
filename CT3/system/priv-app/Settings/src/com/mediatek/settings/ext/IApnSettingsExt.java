package com.mediatek.settings.ext;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.view.Menu;
import java.util.ArrayList;
/* loaded from: classes.dex */
public interface IApnSettingsExt {
    void addApnTypeExtra(Intent intent);

    String[] customizeApnProjection(String[] strArr);

    void customizePreference(int i, PreferenceScreen preferenceScreen);

    void customizeTetherApnSettings(PreferenceScreen preferenceScreen);

    void customizeUnselectableApn(String str, ArrayList<Preference> arrayList, ArrayList<Preference> arrayList2, int i);

    String getApnSortOrder(String str);

    String[] getApnTypeArray(String[] strArr, Context context, String str);

    String getFillListQuery(String str, String str2);

    String getOperatorNumericFromImpi(String str, int i);

    Uri getPreferCarrierUri(Uri uri, int i);

    boolean getScreenEnableState(int i, Activity activity);

    Uri getUriFromIntent(Uri uri, Context context, Intent intent);

    void initTetherField(PreferenceFragment preferenceFragment);

    boolean isAllowEditPresetApn(String str, String str2, String str3, int i);

    boolean isSelectable(String str);

    void onDestroy();

    long replaceApn(long j, Context context, Uri uri, String str, String str2, ContentValues contentValues, String str3);

    void saveApnValues(ContentValues contentValues);

    void setApnTypePreferenceState(Preference preference, String str);

    void setMvnoPreferenceState(Preference preference, Preference preference2);

    void setPreferenceTextAndSummary(int i, String str);

    String updateApnName(String str, int i);

    void updateFieldsStatus(int i, int i2, PreferenceScreen preferenceScreen);

    void updateMenu(Menu menu, int i, int i2, String str);

    void updateTetherState();
}
