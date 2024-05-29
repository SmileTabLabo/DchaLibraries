package com.android.settings.dashboard;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.logging.MetricsLogger;
import com.android.settings.InstrumentedFragment;
import com.android.settings.R;
import com.android.settings.Settings;
import com.android.settings.SettingsActivity;
import com.android.settings.dashboard.conditional.Condition;
import com.android.settings.dashboard.conditional.ConditionAdapterUtils;
import com.android.settings.dashboard.conditional.ConditionManager;
import com.android.settings.dashboard.conditional.FocusRecyclerView;
import com.android.settingslib.HelpUtils;
import com.android.settingslib.SuggestionParser;
import com.android.settingslib.drawer.DashboardCategory;
import com.android.settingslib.drawer.SettingsDrawerActivity;
import com.android.settingslib.drawer.Tile;
import java.util.List;
/* loaded from: classes.dex */
public class DashboardSummary extends InstrumentedFragment implements SettingsDrawerActivity.CategoryListener, ConditionManager.ConditionListener, FocusRecyclerView.FocusListener, FocusRecyclerView.DetachListener {
    public static final String[] INITIAL_ITEMS = {Settings.WifiSettingsActivity.class.getName(), Settings.BluetoothSettingsActivity.class.getName(), Settings.DataUsageSummaryActivity.class.getName(), Settings.PowerUsageSummaryActivity.class.getName(), Settings.ManageApplicationsActivity.class.getName(), Settings.StorageSettingsActivity.class.getName(), Settings.HotKnotSettingsActivity.class.getName()};
    private DashboardAdapter mAdapter;
    private ConditionManager mConditionManager;
    private FocusRecyclerView mDashboard;
    private LinearLayoutManager mLayoutManager;
    private SuggestionParser mSuggestionParser;
    private SuggestionsChecks mSuggestionsChecks;
    private SummaryLoader mSummaryLoader;

    @Override // com.android.settings.InstrumentedFragment
    protected int getMetricsCategory() {
        return 35;
    }

    @Override // com.android.settings.InstrumentedFragment, android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle savedInstanceState) {
        System.currentTimeMillis();
        super.onCreate(savedInstanceState);
        List<DashboardCategory> categories = ((SettingsActivity) getActivity()).getDashboardCategories();
        this.mSummaryLoader = new SummaryLoader(getActivity(), categories);
        setHasOptionsMenu(true);
        Context context = getContext();
        this.mConditionManager = ConditionManager.get(context, false);
        this.mSuggestionParser = new SuggestionParser(context, context.getSharedPreferences("suggestions", 0), R.xml.suggestion_ordering);
        this.mSuggestionsChecks = new SuggestionsChecks(getContext());
    }

    @Override // android.app.Fragment
    public void onDestroy() {
        this.mSummaryLoader.release();
        super.onDestroy();
    }

    @Override // android.app.Fragment
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (getActivity() == null) {
            return;
        }
        HelpUtils.prepareHelpMenuItem(getActivity(), menu, (int) R.string.help_uri_dashboard, getClass().getName());
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onStart() {
        System.currentTimeMillis();
        super.onStart();
        ((SettingsDrawerActivity) getActivity()).addCategoryListener(this);
        this.mSummaryLoader.setListening(true);
        for (Condition c : this.mConditionManager.getConditions()) {
            if (c.shouldShow()) {
                MetricsLogger.visible(getContext(), c.getMetricsConstant());
            }
        }
        if (this.mAdapter.getSuggestions() == null) {
            return;
        }
        for (Tile suggestion : this.mAdapter.getSuggestions()) {
            MetricsLogger.action(getContext(), 384, DashboardAdapter.getSuggestionIdentifier(getContext(), suggestion));
        }
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onStop() {
        super.onStop();
        ((SettingsDrawerActivity) getActivity()).remCategoryListener(this);
        this.mSummaryLoader.setListening(false);
        for (Condition c : this.mConditionManager.getConditions()) {
            if (c.shouldShow()) {
                MetricsLogger.hidden(getContext(), c.getMetricsConstant());
            }
        }
        if (this.mAdapter.getSuggestions() == null) {
            return;
        }
        for (Tile suggestion : this.mAdapter.getSuggestions()) {
            MetricsLogger.action(getContext(), 385, DashboardAdapter.getSuggestionIdentifier(getContext(), suggestion));
        }
    }

    @Override // com.android.settings.dashboard.conditional.FocusRecyclerView.FocusListener
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        System.currentTimeMillis();
        if (hasWindowFocus) {
            this.mConditionManager.addListener(this);
            this.mConditionManager.refreshAll();
            return;
        }
        this.mConditionManager.remListener(this);
    }

    @Override // com.android.settings.dashboard.conditional.FocusRecyclerView.DetachListener
    public void onDetachedFromWindow() {
        this.mConditionManager.remListener(this);
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dashboard, container, false);
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (this.mLayoutManager == null) {
            return;
        }
        outState.putInt("scroll_position", this.mLayoutManager.findFirstVisibleItemPosition());
    }

    @Override // android.support.v14.preference.PreferenceFragment, android.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        System.currentTimeMillis();
        this.mDashboard = (FocusRecyclerView) view.findViewById(R.id.dashboard_container);
        this.mLayoutManager = new LinearLayoutManager(getContext());
        this.mLayoutManager.setOrientation(1);
        if (bundle != null) {
            int scrollPosition = bundle.getInt("scroll_position");
            this.mLayoutManager.scrollToPosition(scrollPosition);
        }
        this.mDashboard.setLayoutManager(this.mLayoutManager);
        this.mDashboard.setHasFixedSize(true);
        this.mDashboard.setListener(this);
        this.mDashboard.setDetachListener(this);
        this.mDashboard.addItemDecoration(new DashboardDecorator(getContext()));
        this.mAdapter = new DashboardAdapter(getContext(), this.mSuggestionParser);
        this.mAdapter.setConditions(this.mConditionManager.getConditions());
        this.mDashboard.setAdapter(this.mAdapter);
        this.mSummaryLoader.setAdapter(this.mAdapter);
        ConditionAdapterUtils.addDismiss(this.mDashboard);
        rebuildUI();
    }

    private void rebuildUI() {
        if (!isAdded()) {
            Log.w("DashboardSummary", "Cannot build the DashboardSummary UI yet as the Fragment is not added");
            return;
        }
        List<DashboardCategory> categories = ((SettingsActivity) getActivity()).getDashboardCategories();
        this.mAdapter.setCategories(categories);
        new SuggestionLoader(this, null).execute(new Void[0]);
    }

    @Override // com.android.settingslib.drawer.SettingsDrawerActivity.CategoryListener
    public void onCategoriesChanged() {
        rebuildUI();
    }

    @Override // com.android.settings.dashboard.conditional.ConditionManager.ConditionListener
    public void onConditionsChanged() {
        Log.d("DashboardSummary", "onConditionsChanged");
        this.mAdapter.setConditions(this.mConditionManager.getConditions());
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class SuggestionLoader extends AsyncTask<Void, Void, List<Tile>> {
        /* synthetic */ SuggestionLoader(DashboardSummary this$0, SuggestionLoader suggestionLoader) {
            this();
        }

        private SuggestionLoader() {
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public List<Tile> doInBackground(Void... params) {
            List<Tile> suggestions = DashboardSummary.this.mSuggestionParser.getSuggestions();
            int i = 0;
            while (i < suggestions.size()) {
                if (DashboardSummary.this.mSuggestionsChecks.isSuggestionComplete(suggestions.get(i))) {
                    DashboardSummary.this.mAdapter.disableSuggestion(suggestions.get(i));
                    suggestions.remove(i);
                    i--;
                }
                i++;
            }
            return suggestions;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(List<Tile> tiles) {
            DashboardSummary.this.mAdapter.setSuggestions(tiles);
        }
    }
}
