package com.mediatek.settings.ext;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.view.Menu;
import java.util.ArrayList;
/* loaded from: classes.dex */
public class DefaultApnSettingsExt implements IApnSettingsExt {
    private static final String TAG = "DefaultApnSettingsExt";

    @Override // com.mediatek.settings.ext.IApnSettingsExt
    public void onDestroy() {
    }

    @Override // com.mediatek.settings.ext.IApnSettingsExt
    public void initTetherField(PreferenceFragment pref) {
    }

    @Override // com.mediatek.settings.ext.IApnSettingsExt
    public boolean isAllowEditPresetApn(String type, String apn, String numeric, int sourcetype) {
        Log.d(TAG, "isAllowEditPresetApn");
        return true;
    }

    @Override // com.mediatek.settings.ext.IApnSettingsExt
    public void customizeTetherApnSettings(PreferenceScreen root) {
    }

    @Override // com.mediatek.settings.ext.IApnSettingsExt
    public String getFillListQuery(String where, String mccmnc) {
        return where;
    }

    @Override // com.mediatek.settings.ext.IApnSettingsExt
    public void updateTetherState() {
    }

    @Override // com.mediatek.settings.ext.IApnSettingsExt
    public Uri getPreferCarrierUri(Uri defaultUri, int subId) {
        return defaultUri;
    }

    @Override // com.mediatek.settings.ext.IApnSettingsExt
    public void setApnTypePreferenceState(Preference preference, String apnType) {
    }

    @Override // com.mediatek.settings.ext.IApnSettingsExt
    public Uri getUriFromIntent(Uri defaultUri, Context context, Intent intent) {
        return defaultUri;
    }

    @Override // com.mediatek.settings.ext.IApnSettingsExt
    public String[] getApnTypeArray(String[] defaultApnArray, Context context, String apnType) {
        return defaultApnArray;
    }

    @Override // com.mediatek.settings.ext.IApnSettingsExt
    public boolean isSelectable(String type) {
        return true;
    }

    @Override // com.mediatek.settings.ext.IApnSettingsExt
    public boolean getScreenEnableState(int subId, Activity activity) {
        return true;
    }

    @Override // com.mediatek.settings.ext.IApnSettingsExt
    public void updateMenu(Menu menu, int newMenuId, int restoreMenuId, String numeric) {
    }

    @Override // com.mediatek.settings.ext.IApnSettingsExt
    public void addApnTypeExtra(Intent it) {
    }

    @Override // com.mediatek.settings.ext.IApnSettingsExt
    public void updateFieldsStatus(int subId, int sourceType, PreferenceScreen root) {
    }

    @Override // com.mediatek.settings.ext.IApnSettingsExt
    public void setPreferenceTextAndSummary(int subId, String text) {
    }

    @Override // com.mediatek.settings.ext.IApnSettingsExt
    public void customizePreference(int subId, PreferenceScreen root) {
    }

    @Override // com.mediatek.settings.ext.IApnSettingsExt
    public String[] customizeApnProjection(String[] projection) {
        return projection;
    }

    @Override // com.mediatek.settings.ext.IApnSettingsExt
    public void saveApnValues(ContentValues contentValues) {
    }

    @Override // com.mediatek.settings.ext.IApnSettingsExt
    public String updateApnName(String name, int sourcetype) {
        return name;
    }

    @Override // com.mediatek.settings.ext.IApnSettingsExt
    public long replaceApn(long defaultReplaceNum, Context context, Uri uri, String apn, String name, ContentValues values, String numeric) {
        return defaultReplaceNum;
    }

    @Override // com.mediatek.settings.ext.IApnSettingsExt
    public void customizeUnselectableApn(String type, ArrayList<Preference> mnoApnList, ArrayList<Preference> mvnoApnList, int subId) {
    }

    @Override // com.mediatek.settings.ext.IApnSettingsExt
    public void setMvnoPreferenceState(Preference mvnoType, Preference mvnoMatchData) {
    }

    @Override // com.mediatek.settings.ext.IApnSettingsExt
    public String getApnSortOrder(String order) {
        return order;
    }

    @Override // com.mediatek.settings.ext.IApnSettingsExt
    public String getOperatorNumericFromImpi(String defaultValue, int phoneId) {
        return defaultValue;
    }
}
