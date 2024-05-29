package com.android.settings.datausage;

import android.content.Context;
import android.content.Intent;
import android.net.NetworkPolicy;
import android.net.NetworkTemplate;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.preference.Preference;
import android.util.AttributeSet;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.datausage.CellDataPreference;
import com.android.settings.datausage.TemplatePreference;
/* loaded from: classes.dex */
public class BillingCyclePreference extends Preference implements TemplatePreference {
    private final CellDataPreference.DataStateListener mListener;
    private NetworkPolicy mPolicy;
    private TemplatePreference.NetworkServices mServices;
    private int mSubId;
    private NetworkTemplate mTemplate;

    public BillingCyclePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mListener = new CellDataPreference.DataStateListener() { // from class: com.android.settings.datausage.BillingCyclePreference.1
            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                BillingCyclePreference.this.updateEnabled();
            }
        };
    }

    @Override // android.support.v7.preference.Preference
    public void onAttached() {
        super.onAttached();
        this.mListener.setListener(true, this.mSubId, getContext());
    }

    @Override // android.support.v7.preference.Preference
    public void onDetached() {
        this.mListener.setListener(false, this.mSubId, getContext());
        super.onDetached();
    }

    @Override // com.android.settings.datausage.TemplatePreference
    public void setTemplate(NetworkTemplate template, int subId, TemplatePreference.NetworkServices services) {
        this.mTemplate = template;
        this.mSubId = subId;
        this.mServices = services;
        this.mPolicy = services.mPolicyEditor.getPolicy(this.mTemplate);
        Context context = getContext();
        Object[] objArr = new Object[1];
        objArr[0] = Integer.valueOf(this.mPolicy != null ? this.mPolicy.cycleDay : 1);
        setSummary(context.getString(R.string.billing_cycle_fragment_summary, objArr));
        setIntent(getIntent());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateEnabled() {
        try {
            setEnabled((this.mPolicy != null && this.mServices.mNetworkService.isBandwidthControlEnabled() && this.mServices.mTelephonyManager.getDataEnabled(this.mSubId)) ? this.mServices.mUserManager.isAdminUser() : false);
        } catch (RemoteException e) {
            setEnabled(false);
        }
    }

    @Override // android.support.v7.preference.Preference
    public Intent getIntent() {
        Bundle args = new Bundle();
        args.putParcelable("network_template", this.mTemplate);
        return Utils.onBuildStartFragmentIntent(getContext(), BillingCycleSettings.class.getName(), args, null, 0, getTitle(), false);
    }
}
