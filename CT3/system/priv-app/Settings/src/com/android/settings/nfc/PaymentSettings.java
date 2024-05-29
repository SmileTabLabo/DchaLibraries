package com.android.settings.nfc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.nfc.PaymentBackend;
import java.util.List;
/* loaded from: classes.dex */
public class PaymentSettings extends SettingsPreferenceFragment {
    public static final SummaryLoader.SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = new SummaryLoader.SummaryProviderFactory() { // from class: com.android.settings.nfc.PaymentSettings.1
        @Override // com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory
        public SummaryLoader.SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader);
        }
    };
    private PaymentBackend mPaymentBackend;

    @Override // com.android.settings.InstrumentedPreferenceFragment
    protected int getMetricsCategory() {
        return 70;
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mPaymentBackend = new PaymentBackend(getActivity());
        setHasOptionsMenu(true);
        PreferenceManager manager = getPreferenceManager();
        PreferenceScreen screen = manager.createPreferenceScreen(getActivity());
        List<PaymentBackend.PaymentAppInfo> appInfos = this.mPaymentBackend.getPaymentAppInfos();
        if (appInfos != null && appInfos.size() > 0) {
            NfcPaymentPreference preference = new NfcPaymentPreference(getPrefContext(), this.mPaymentBackend);
            preference.setKey("payment");
            screen.addPreference(preference);
            NfcForegroundPreference foreground = new NfcForegroundPreference(getPrefContext(), this.mPaymentBackend);
            screen.addPreference(foreground);
        }
        setPreferenceScreen(screen);
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewGroup contentRoot = (ViewGroup) getListView().getParent();
        View emptyView = getActivity().getLayoutInflater().inflate(R.layout.nfc_payment_empty, contentRoot, false);
        contentRoot.addView(emptyView);
        setEmptyView(emptyView);
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        this.mPaymentBackend.onResume();
    }

    @Override // com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onPause() {
        super.onPause();
        this.mPaymentBackend.onPause();
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.app.Fragment
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem menuItem = menu.add(R.string.nfc_payment_how_it_works);
        Intent howItWorksIntent = new Intent(getActivity(), HowItWorks.class);
        menuItem.setIntent(howItWorksIntent);
        menuItem.setShowAsActionFlags(0);
    }

    /* loaded from: classes.dex */
    private static class SummaryProvider implements SummaryLoader.SummaryProvider {
        private final Context mContext;
        private final SummaryLoader mSummaryLoader;

        public SummaryProvider(Context context, SummaryLoader summaryLoader) {
            this.mContext = context;
            this.mSummaryLoader = summaryLoader;
        }

        @Override // com.android.settings.dashboard.SummaryLoader.SummaryProvider
        public void setListening(boolean listening) {
            if (!listening || NfcAdapter.getDefaultAdapter(this.mContext) == null) {
                return;
            }
            PaymentBackend paymentBackend = new PaymentBackend(this.mContext);
            paymentBackend.refresh();
            PaymentBackend.PaymentAppInfo app = paymentBackend.getDefaultApp();
            if (app == null) {
                return;
            }
            this.mSummaryLoader.setSummary(this, this.mContext.getString(R.string.payment_summary, app.label));
        }
    }
}
