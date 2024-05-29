package com.android.settings.datausage;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.NetworkTemplate;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.telecom.TelecomManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Checkable;
import android.widget.Toast;
import com.android.internal.logging.MetricsLogger;
import com.android.settings.CustomDialogPreference;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.datausage.TemplatePreference;
import com.mediatek.settings.UtilsExt;
import com.mediatek.settings.cdma.CdmaUtils;
import com.mediatek.settings.ext.IDataUsageSummaryExt;
import com.mediatek.settings.sim.TelephonyUtils;
import java.util.List;
/* loaded from: classes.dex */
public class CellDataPreference extends CustomDialogPreference implements TemplatePreference {
    private boolean mAlertForCdmaCompetition;
    public boolean mChecked;
    private IDataUsageSummaryExt mDataUsageSummaryExt;
    private boolean mIsAirplaneModeOn;
    private final DataStateListener mListener;
    public boolean mMultiSimDialog;
    private BroadcastReceiver mReceiver;
    public int mSubId;
    private SubscriptionManager mSubscriptionManager;
    private TelephonyManager mTelephonyManager;

    public CellDataPreference(Context context, AttributeSet attrs) {
        super(context, attrs, 16843629);
        this.mSubId = -1;
        this.mListener = new DataStateListener() { // from class: com.android.settings.datausage.CellDataPreference.1
            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                CellDataPreference.this.log("data state changed");
                CellDataPreference.this.updateChecked();
            }
        };
        this.mAlertForCdmaCompetition = false;
        this.mReceiver = new BroadcastReceiver() { // from class: com.android.settings.datausage.CellDataPreference.2
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                String action = intent.getAction();
                CellDataPreference.this.log("onReceive broadcast , action =  " + action);
                if (action.equals("android.intent.action.AIRPLANE_MODE")) {
                    CellDataPreference.this.mIsAirplaneModeOn = intent.getBooleanExtra("state", false);
                    CellDataPreference.this.updateScreenEnableState();
                } else if (action.equals("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED")) {
                    CellDataPreference.this.onCdmaCompetitionHandled(intent);
                    CellDataPreference.this.updateScreenEnableState();
                } else if (!action.equals("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE") && !action.equals("android.intent.action.ACTION_SET_RADIO_CAPABILITY_FAILED")) {
                } else {
                    CellDataPreference.this.updateScreenEnableState();
                }
            }
        };
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.preference.Preference
    public void onRestoreInstanceState(Parcelable s) {
        CellDataState state = (CellDataState) s;
        super.onRestoreInstanceState(state.getSuperState());
        this.mTelephonyManager = TelephonyManager.from(getContext());
        this.mChecked = state.mChecked;
        this.mMultiSimDialog = state.mMultiSimDialog;
        if (this.mSubId == -1) {
            this.mSubId = state.mSubId;
            setKey(getKey() + this.mSubId);
        }
        notifyChanged();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.preference.Preference
    public Parcelable onSaveInstanceState() {
        CellDataState state = new CellDataState(super.onSaveInstanceState());
        state.mChecked = this.mChecked;
        state.mMultiSimDialog = this.mMultiSimDialog;
        state.mSubId = this.mSubId;
        return state;
    }

    @Override // android.support.v7.preference.Preference
    public void onAttached() {
        log("onAttached...");
        super.onAttached();
        this.mDataUsageSummaryExt = UtilsExt.getDataUsageSummaryPlugin(getContext().getApplicationContext());
        this.mListener.setListener(true, this.mSubId, getContext());
        this.mIsAirplaneModeOn = TelephonyUtils.isAirplaneModeOn(getContext());
        IntentFilter intentFilter = new IntentFilter("android.intent.action.AIRPLANE_MODE");
        intentFilter.addAction("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE");
        intentFilter.addAction("android.intent.action.ACTION_SET_RADIO_CAPABILITY_FAILED");
        intentFilter.addAction("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
        getContext().registerReceiver(this.mReceiver, intentFilter);
        updateScreenEnableState();
    }

    @Override // android.support.v7.preference.Preference
    public void onDetached() {
        log("onDetached...");
        this.mListener.setListener(false, this.mSubId, getContext());
        super.onDetached();
        getContext().unregisterReceiver(this.mReceiver);
        this.mAlertForCdmaCompetition = false;
    }

    @Override // com.android.settings.datausage.TemplatePreference
    public void setTemplate(NetworkTemplate template, int subId, TemplatePreference.NetworkServices services) {
        if (subId == -1) {
            throw new IllegalArgumentException("CellDataPreference needs a SubscriptionInfo");
        }
        this.mSubscriptionManager = SubscriptionManager.from(getContext());
        this.mTelephonyManager = TelephonyManager.from(getContext());
        if (this.mSubId == -1) {
            this.mSubId = subId;
            setKey(getKey() + subId);
        }
        updateChecked();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateChecked() {
        setChecked(this.mTelephonyManager.getDataEnabled(this.mSubId));
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.preference.Preference
    public void performClick(View view) {
        log("performClick, checked = " + this.mChecked);
        MetricsLogger.action(getContext(), 178, !this.mChecked);
        SubscriptionInfo currentSir = this.mSubscriptionManager.getActiveSubscriptionInfo(this.mSubId);
        SubscriptionInfo nextSir = this.mSubscriptionManager.getDefaultDataSubscriptionInfo();
        if (this.mChecked) {
            if (!Utils.showSimCardTile(getContext()) || (nextSir != null && currentSir != null && currentSir.getSubscriptionId() == nextSir.getSubscriptionId())) {
                setMobileDataEnabled(false);
                if (nextSir != null && currentSir != null && currentSir.getSubscriptionId() == nextSir.getSubscriptionId()) {
                    disableDataForOtherSubscriptions(this.mSubId);
                    return;
                }
                return;
            }
            this.mMultiSimDialog = false;
            super.performClick(view);
        } else if (Utils.showSimCardTile(getContext()) && currentSir != null && ((nextSir != null && currentSir.getSubscriptionId() != nextSir.getSubscriptionId()) || nextSir == null)) {
            if (CdmaUtils.isCdmaCardCompetionForData(getContext())) {
                log("alert Cdma Competition..., subId = " + this.mSubId);
                CdmaUtils.startAlertCdmaDialog(getContext(), this.mSubId, 0);
                this.mAlertForCdmaCompetition = true;
                return;
            }
            this.mMultiSimDialog = true;
            super.performClick(view);
        } else {
            setMobileDataEnabled(true);
        }
    }

    private void setMobileDataEnabled(boolean enabled) {
        log("setMobileDataEnabled(" + enabled + "," + this.mSubId + ")");
        this.mTelephonyManager.setDataEnabled(this.mSubId, enabled);
        setChecked(enabled);
    }

    private void setChecked(boolean checked) {
        log("setChecked " + checked);
        if (this.mChecked == checked) {
            return;
        }
        this.mChecked = checked;
        notifyChanged();
    }

    @Override // android.support.v7.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        View switchView = holder.findViewById(16908352);
        switchView.setClickable(false);
        ((Checkable) switchView).setChecked(this.mChecked);
        this.mDataUsageSummaryExt.onBindViewHolder(getContext(), holder.itemView, new View.OnClickListener() { // from class: com.android.settings.datausage.CellDataPreference.3
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                CellDataPreference.this.performClick(v);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settings.CustomDialogPreference
    public void onPrepareDialogBuilder(AlertDialog.Builder builder, DialogInterface.OnClickListener listener) {
        if (this.mMultiSimDialog) {
            showMultiSimDialog(builder, listener);
        } else if (!this.mDataUsageSummaryExt.onDisablingData(this.mSubId)) {
        } else {
            showDisableDialog(builder, listener);
        }
    }

    private void showDisableDialog(AlertDialog.Builder builder, DialogInterface.OnClickListener listener) {
        builder.setTitle((CharSequence) null).setMessage(R.string.data_usage_disable_mobile).setPositiveButton(17039370, listener).setNegativeButton(17039360, (DialogInterface.OnClickListener) null);
    }

    private void showMultiSimDialog(AlertDialog.Builder builder, DialogInterface.OnClickListener listener) {
        String previousName;
        SubscriptionInfo currentSir = this.mSubscriptionManager.getActiveSubscriptionInfo(this.mSubId);
        SubscriptionInfo nextSir = this.mSubscriptionManager.getDefaultDataSubscriptionInfo();
        if (nextSir == null) {
            previousName = getContext().getResources().getString(R.string.sim_selection_required_pref);
        } else {
            previousName = nextSir.getDisplayName().toString();
        }
        builder.setTitle(R.string.sim_change_data_title);
        Context context = getContext();
        Object[] objArr = new Object[2];
        objArr[0] = String.valueOf(currentSir != null ? currentSir.getDisplayName() : null);
        objArr[1] = previousName;
        builder.setMessage(context.getString(R.string.sim_change_data_message, objArr));
        builder.setPositiveButton(R.string.okay, listener);
        builder.setNegativeButton(R.string.cancel, (DialogInterface.OnClickListener) null);
    }

    private void disableDataForOtherSubscriptions(int subId) {
        List<SubscriptionInfo> subInfoList = this.mSubscriptionManager.getActiveSubscriptionInfoList();
        if (subInfoList == null) {
            return;
        }
        for (SubscriptionInfo subInfo : subInfoList) {
            if (subInfo.getSubscriptionId() != subId) {
                this.mTelephonyManager.setDataEnabled(subInfo.getSubscriptionId(), false);
            }
        }
    }

    @Override // com.android.settings.CustomDialogPreference
    protected void onClick(DialogInterface dialog, int which) {
        if (which != -1) {
            return;
        }
        log("onClick, mMultiSimDialog = " + this.mMultiSimDialog);
        if (this.mMultiSimDialog) {
            if (TelecomManager.from(getContext()).isInCall()) {
                Toast.makeText(getContext(), (int) R.string.default_data_switch_err_msg1, 0).show();
                log("in Call, RETURN!");
                return;
            }
            this.mSubscriptionManager.setDefaultDataSubId(this.mSubId);
            setMobileDataEnabled(true);
            disableDataForOtherSubscriptions(this.mSubId);
            return;
        }
        setMobileDataEnabled(false);
    }

    /* loaded from: classes.dex */
    public static abstract class DataStateListener extends ContentObserver {
        public DataStateListener() {
            super(new Handler(Looper.getMainLooper()));
        }

        public void setListener(boolean listening, int subId, Context context) {
            if (listening) {
                Uri uri = Settings.Global.getUriFor("mobile_data");
                if (TelephonyManager.getDefault().getSimCount() != 1) {
                    uri = Settings.Global.getUriFor("mobile_data" + subId);
                }
                context.getContentResolver().registerContentObserver(uri, false, this);
                return;
            }
            context.getContentResolver().unregisterContentObserver(this);
        }
    }

    /* loaded from: classes.dex */
    public static class CellDataState extends Preference.BaseSavedState {
        public static final Parcelable.Creator<CellDataState> CREATOR = new Parcelable.Creator<CellDataState>() { // from class: com.android.settings.datausage.CellDataPreference.CellDataState.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public CellDataState createFromParcel(Parcel source) {
                return new CellDataState(source);
            }

            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public CellDataState[] newArray(int size) {
                return new CellDataState[size];
            }
        };
        public boolean mChecked;
        public boolean mMultiSimDialog;
        public int mSubId;

        public CellDataState(Parcelable base) {
            super(base);
        }

        public CellDataState(Parcel source) {
            super(source);
            this.mChecked = source.readByte() != 0;
            this.mMultiSimDialog = source.readByte() != 0;
            this.mSubId = source.readInt();
        }

        @Override // android.view.AbsSavedState, android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeByte((byte) (this.mChecked ? 1 : 0));
            dest.writeByte((byte) (this.mMultiSimDialog ? 1 : 0));
            dest.writeInt(this.mSubId);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onCdmaCompetitionHandled(Intent intent) {
        int defaultDataSubId = intent.getIntExtra("subscription", -1);
        log("defaultDataSubId: " + defaultDataSubId + " mAlertForCdmaCompetition: " + this.mAlertForCdmaCompetition);
        if (!this.mAlertForCdmaCompetition || defaultDataSubId != this.mSubId) {
            return;
        }
        setMobileDataEnabled(true);
        disableDataForOtherSubscriptions(this.mSubId);
        this.mAlertForCdmaCompetition = false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateScreenEnableState() {
        boolean isCapabilitySwitching = TelephonyUtils.isCapabilitySwitching();
        log("updateScreenEnableState, mIsAirplaneModeOn = " + this.mIsAirplaneModeOn + ", isCapabilitySwitching = " + isCapabilitySwitching);
        setEnabled((this.mIsAirplaneModeOn || isCapabilitySwitching) ? false : this.mDataUsageSummaryExt.isAllowDataEnable(this.mSubId));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void log(String msg) {
        Log.d("CellDataPreference[" + this.mSubId + "]", msg);
    }
}
