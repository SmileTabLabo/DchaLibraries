package com.mediatek.settings.hotknot;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settings.widget.SwitchBar;
import com.mediatek.hotknot.HotKnotAdapter;
import java.util.ArrayList;
import java.util.List;
/* loaded from: classes.dex */
public class HotKnotSettings extends SettingsPreferenceFragment implements Indexable {
    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() { // from class: com.mediatek.settings.hotknot.HotKnotSettings.2
        @Override // com.android.settings.search.BaseSearchIndexProvider, com.android.settings.search.Indexable.SearchIndexProvider
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            List<SearchIndexableRaw> result = new ArrayList<>();
            Resources res = context.getResources();
            HotKnotAdapter adapter = HotKnotAdapter.getDefaultAdapter(context);
            if (adapter != null) {
                SearchIndexableRaw data = new SearchIndexableRaw(context);
                data.title = res.getString(R.string.hotknot_settings_title);
                data.screenTitle = res.getString(R.string.hotknot_settings_title);
                data.keywords = res.getString(R.string.hotknot_settings_title);
                result.add(data);
            }
            return result;
        }
    };
    public static final SummaryLoader.SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = new SummaryLoader.SummaryProviderFactory() { // from class: com.mediatek.settings.hotknot.HotKnotSettings.3
        @Override // com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory
        public SummaryLoader.SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader);
        }
    };
    private HotKnotAdapter mAdapter;
    private HotKnotEnabler mHotKnotEnabler;
    private IntentFilter mIntentFilter;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() { // from class: com.mediatek.settings.hotknot.HotKnotSettings.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            intent.getAction();
        }
    };
    private SwitchBar mSwitchBar;

    @Override // com.android.settings.InstrumentedPreferenceFragment
    protected int getMetricsCategory() {
        return 100002;
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SettingsActivity activity = (SettingsActivity) getActivity();
        this.mAdapter = HotKnotAdapter.getDefaultAdapter(activity);
        if (this.mAdapter == null) {
            Log.d("@M_HotKnotSettings", "Hotknot adapter is null, finish Hotknot settings");
            getActivity().finish();
        }
        this.mIntentFilter = new IntentFilter("com.mediatek.hotknot.action.ADAPTER_STATE_CHANGED");
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onStart() {
        super.onStart();
        SettingsActivity activity = (SettingsActivity) getActivity();
        this.mSwitchBar = activity.getSwitchBar();
        Log.d("@M_HotKnotSettings", "onCreate, mSwitchBar = " + this.mSwitchBar);
        this.mHotKnotEnabler = new HotKnotEnabler(activity, this.mSwitchBar);
    }

    @Override // com.android.settings.SettingsPreferenceFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.hotknot_settings, container, false);
        TextView textView = (TextView) view.findViewById(R.id.hotknot_warning_msg);
        if (textView != null) {
            textView.setText(getString(R.string.hotknot_charging_warning, new Object[]{getString(R.string.hotknot_settings_title)}));
        }
        return view;
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onDestroyView() {
        super.onDestroyView();
        if (this.mHotKnotEnabler == null) {
            return;
        }
        this.mHotKnotEnabler.teardownSwitchBar();
    }

    @Override // com.android.settings.SettingsPreferenceFragment, com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        if (this.mHotKnotEnabler != null) {
            this.mHotKnotEnabler.resume();
        }
        getActivity().registerReceiver(this.mReceiver, this.mIntentFilter);
    }

    @Override // com.android.settings.InstrumentedPreferenceFragment, android.app.Fragment
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(this.mReceiver);
        if (this.mHotKnotEnabler == null) {
            return;
        }
        this.mHotKnotEnabler.pause();
    }

    /* loaded from: classes.dex */
    private static class SummaryProvider extends BroadcastReceiver implements SummaryLoader.SummaryProvider {
        private HotKnotAdapter mAdapter;
        private final Context mContext;
        private final SummaryLoader mSummaryLoader;

        public SummaryProvider(Context context, SummaryLoader summaryLoader) {
            this.mContext = context;
            this.mSummaryLoader = summaryLoader;
            this.mAdapter = HotKnotAdapter.getDefaultAdapter(context);
        }

        @Override // com.android.settings.dashboard.SummaryLoader.SummaryProvider
        public void setListening(boolean listening) {
            if (listening) {
                if (this.mAdapter == null) {
                    return;
                }
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction("com.mediatek.hotknot.action.ADAPTER_STATE_CHANGED");
                this.mContext.registerReceiver(this, intentFilter);
                int initState = this.mAdapter.isEnabled() ? 2 : 1;
                this.mSummaryLoader.setSummary(this, getSummary(initState));
            } else if (this.mAdapter == null) {
            } else {
                this.mContext.unregisterReceiver(this);
            }
        }

        private String getSummary(int state) {
            switch (state) {
                case 1:
                    String summary = this.mContext.getResources().getString(R.string.switch_off_text);
                    return summary;
                case 2:
                    String summary2 = this.mContext.getResources().getString(R.string.switch_on_text);
                    return summary2;
                default:
                    return null;
            }
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra("com.mediatek.hotknot.extra.ADAPTER_STATE", -1);
            Log.d("HotKnotSettings", "HotKnot state changed to " + state);
            this.mSummaryLoader.setSummary(this, getSummary(state));
        }
    }
}
