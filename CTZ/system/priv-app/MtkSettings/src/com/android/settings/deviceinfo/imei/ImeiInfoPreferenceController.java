package com.android.settings.deviceinfo.imei;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnCreate;
import com.android.settingslib.core.lifecycle.events.OnDestroy;
import com.android.settingslib.deviceinfo.AbstractSimStatusImeiInfoPreferenceController;
import java.util.ArrayList;
import java.util.List;
/* loaded from: classes.dex */
public class ImeiInfoPreferenceController extends AbstractSimStatusImeiInfoPreferenceController implements PreferenceControllerMixin, LifecycleObserver, OnCreate, OnDestroy {
    private final Fragment mFragment;
    private final boolean mIsMultiSim;
    private final SubscriptionManager.OnSubscriptionsChangedListener mOnSubscriptionsChangeListener;
    private final List<Preference> mPreferenceList;
    private final SubscriptionManager mSubscriptionManager;
    private final TelephonyManager mTelephonyManager;

    public ImeiInfoPreferenceController(Context context, Fragment fragment, Lifecycle lifecycle) {
        super(context);
        this.mPreferenceList = new ArrayList();
        this.mOnSubscriptionsChangeListener = new SubscriptionManager.OnSubscriptionsChangedListener() { // from class: com.android.settings.deviceinfo.imei.ImeiInfoPreferenceController.1
            @Override // android.telephony.SubscriptionManager.OnSubscriptionsChangedListener
            public void onSubscriptionsChanged() {
                Log.d("ImeiInfoPreferenceController", "onSubscriptionsChanged");
                for (int i = 0; i < ImeiInfoPreferenceController.this.mPreferenceList.size(); i++) {
                    ImeiInfoPreferenceController.this.updatePreference((Preference) ImeiInfoPreferenceController.this.mPreferenceList.get(i), i);
                }
            }
        };
        this.mFragment = fragment;
        this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
        this.mIsMultiSim = this.mTelephonyManager.getPhoneCount() > 1;
        this.mSubscriptionManager = (SubscriptionManager) context.getSystemService("telephony_subscription_service");
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public String getPreferenceKey() {
        return "imei_info";
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public void displayPreference(PreferenceScreen preferenceScreen) {
        super.displayPreference(preferenceScreen);
        Preference findPreference = preferenceScreen.findPreference(getPreferenceKey());
        if (!isAvailable() || findPreference == null || !findPreference.isVisible()) {
            return;
        }
        this.mPreferenceList.add(findPreference);
        updatePreference(findPreference, 0);
        int order = findPreference.getOrder();
        for (int i = 1; i < this.mTelephonyManager.getPhoneCount(); i++) {
            Preference createNewPreference = createNewPreference(preferenceScreen.getContext());
            createNewPreference.setOrder(order + i);
            createNewPreference.setKey("imei_info" + i);
            preferenceScreen.addPreference(createNewPreference);
            this.mPreferenceList.add(createNewPreference);
            updatePreference(createNewPreference, i);
        }
    }

    @Override // com.android.settingslib.core.AbstractPreferenceController
    public boolean handlePreferenceTreeClick(Preference preference) {
        int indexOf = this.mPreferenceList.indexOf(preference);
        if (indexOf == -1) {
            return false;
        }
        ImeiInfoDialogFragment.show(this.mFragment, indexOf, preference.getTitle().toString());
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updatePreference(Preference preference, int i) {
        if (this.mTelephonyManager.getCurrentPhoneTypeForSlot(i) == 2) {
            preference.setTitle(getTitleForCdmaPhone(i));
            preference.setSummary(getMeid(i));
            return;
        }
        preference.setTitle(getTitleForGsmPhone(i));
        preference.setSummary(this.mTelephonyManager.getImei(i));
    }

    private CharSequence getTitleForGsmPhone(int i) {
        return this.mIsMultiSim ? this.mContext.getString(R.string.imei_multi_sim, Integer.valueOf(i + 1)) : this.mContext.getString(R.string.status_imei);
    }

    private CharSequence getTitleForCdmaPhone(int i) {
        return this.mIsMultiSim ? this.mContext.getString(R.string.meid_multi_sim, Integer.valueOf(i + 1)) : this.mContext.getString(R.string.status_meid_number);
    }

    String getMeid(int i) {
        return this.mTelephonyManager.getMeid(i);
    }

    Preference createNewPreference(Context context) {
        return new Preference(context);
    }

    @Override // com.android.settingslib.core.lifecycle.events.OnCreate
    public void onCreate(Bundle bundle) {
        this.mSubscriptionManager.addOnSubscriptionsChangedListener(this.mOnSubscriptionsChangeListener);
    }

    @Override // com.android.settingslib.core.lifecycle.events.OnDestroy
    public void onDestroy() {
        this.mSubscriptionManager.removeOnSubscriptionsChangedListener(this.mOnSubscriptionsChangeListener);
    }
}
