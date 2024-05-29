package com.mediatek.settings.ext;

import android.content.Context;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.Preference;
import android.telecom.PhoneAccountHandle;
import android.telephony.SubscriptionInfo;
import android.view.View;
import android.widget.ImageView;
import java.util.ArrayList;
import java.util.List;
/* loaded from: classes.dex */
public class DefaultSimManagementExt implements ISimManagementExt {
    @Override // com.mediatek.settings.ext.ISimManagementExt
    public void onResume(Context context) {
    }

    @Override // com.mediatek.settings.ext.ISimManagementExt
    public void onPause() {
    }

    public void updateSimEditorPref(PreferenceFragment pref) {
    }

    @Override // com.mediatek.settings.ext.ISimManagementExt
    public void updateDefaultSmsSummary(Preference pref) {
    }

    @Override // com.mediatek.settings.ext.ISimManagementExt
    public void showChangeDataConnDialog(PreferenceFragment prefFragment, boolean isResumed) {
    }

    @Override // com.mediatek.settings.ext.ISimManagementExt
    public void hideSimEditorView(View view, Context context) {
    }

    @Override // com.mediatek.settings.ext.ISimManagementExt
    public void setSmsAutoItemIcon(ImageView view, int dialogId, int position) {
    }

    @Override // com.mediatek.settings.ext.ISimManagementExt
    public void initAutoItemForSms(ArrayList<String> list, ArrayList<SubscriptionInfo> smsSubInfoList) {
    }

    @Override // com.mediatek.settings.ext.ISimManagementExt
    public void setDataState(int subId) {
    }

    @Override // com.mediatek.settings.ext.ISimManagementExt
    public void setDataStateEnable(int subId) {
    }

    @Override // com.mediatek.settings.ext.ISimManagementExt
    public void customizeListArray(List<String> strings) {
    }

    @Override // com.mediatek.settings.ext.ISimManagementExt
    public void customizeSubscriptionInfoArray(List<SubscriptionInfo> subscriptionInfo) {
    }

    @Override // com.mediatek.settings.ext.ISimManagementExt
    public boolean isSimDialogNeeded() {
        return true;
    }

    @Override // com.mediatek.settings.ext.ISimManagementExt
    public boolean useCtTestcard() {
        return false;
    }

    @Override // com.mediatek.settings.ext.ISimManagementExt
    public void setRadioPowerState(int subId, boolean turnOn) {
    }

    @Override // com.mediatek.settings.ext.ISimManagementExt
    public SubscriptionInfo setDefaultSubId(Context context, SubscriptionInfo sir, String type) {
        return sir;
    }

    @Override // com.mediatek.settings.ext.ISimManagementExt
    public PhoneAccountHandle setDefaultCallValue(PhoneAccountHandle phoneAccount) {
        return phoneAccount;
    }

    @Override // com.mediatek.settings.ext.ISimManagementExt
    public void configSimPreferenceScreen(Preference simPref, String type, int size) {
    }

    @Override // com.mediatek.settings.ext.ISimManagementExt
    public void updateList(ArrayList<String> list, ArrayList<SubscriptionInfo> smsSubInfoList, int selectableSubInfoLength) {
    }

    @Override // com.mediatek.settings.ext.ISimManagementExt
    public boolean simDialogOnClick(int id, int value, Context context) {
        return false;
    }

    @Override // com.mediatek.settings.ext.ISimManagementExt
    public void setCurrNetworkIcon(ImageView icon, int id, int position) {
    }

    @Override // com.mediatek.settings.ext.ISimManagementExt
    public void setPrefSummary(Preference simPref, String type) {
    }
}
