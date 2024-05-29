package com.android.quicksearchbox;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import com.android.quicksearchbox.ui.SearchActivityView;
import com.android.quicksearchbox.ui.SuggestionClickListener;
import com.android.quicksearchbox.ui.SuggestionsAdapter;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CharMatcher;
import java.io.File;
/* loaded from: a.zip:com/android/quicksearchbox/SearchActivity.class */
public class SearchActivity extends Activity {
    private Bundle mAppSearchData;
    private OnDestroyListener mDestroyListener;
    private int mOnCreateLatency;
    private LatencyTracker mOnCreateTracker;
    private SearchActivityView mSearchActivityView;
    private Source mSource;
    private LatencyTracker mStartLatencyTracker;
    private boolean mStarting;
    private boolean mTookAction;
    private boolean mTraceStartUp;
    private final Handler mHandler = new Handler();
    private final Runnable mUpdateSuggestionsTask = new Runnable(this) { // from class: com.android.quicksearchbox.SearchActivity.1
        final SearchActivity this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.this$0.updateSuggestions();
        }
    };
    private final Runnable mShowInputMethodTask = new Runnable(this) { // from class: com.android.quicksearchbox.SearchActivity.2
        final SearchActivity this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.this$0.mSearchActivityView.showInputMethodForQuery();
        }
    };

    /* loaded from: a.zip:com/android/quicksearchbox/SearchActivity$ClickHandler.class */
    private class ClickHandler implements SuggestionClickListener {
        final SearchActivity this$0;

        private ClickHandler(SearchActivity searchActivity) {
            this.this$0 = searchActivity;
        }

        /* synthetic */ ClickHandler(SearchActivity searchActivity, ClickHandler clickHandler) {
            this(searchActivity);
        }

        @Override // com.android.quicksearchbox.ui.SuggestionClickListener
        public void onSuggestionClicked(SuggestionsAdapter<?> suggestionsAdapter, long j) {
            this.this$0.launchSuggestion(suggestionsAdapter, j);
        }

        @Override // com.android.quicksearchbox.ui.SuggestionClickListener
        public void onSuggestionQueryRefineClicked(SuggestionsAdapter<?> suggestionsAdapter, long j) {
            this.this$0.refineSuggestion(suggestionsAdapter, j);
        }
    }

    /* loaded from: a.zip:com/android/quicksearchbox/SearchActivity$OnDestroyListener.class */
    public interface OnDestroyListener {
        void onDestroyed();
    }

    private Config getConfig() {
        return getQsbApplication().getConfig();
    }

    private String getCorpusNameFromUri(Uri uri) {
        if (uri != null && "qsb.corpus".equals(uri.getScheme())) {
            return uri.getAuthority();
        }
        return null;
    }

    private Logger getLogger() {
        return getQsbApplication().getLogger();
    }

    private QsbApplication getQsbApplication() {
        return QsbApplication.get(this);
    }

    private SuggestionsProvider getSuggestionsProvider() {
        return getQsbApplication().getSuggestionsProvider();
    }

    private void gotSuggestions(Suggestions suggestions) {
        if (this.mStarting) {
            this.mStarting = false;
            String stringExtra = getIntent().getStringExtra("source");
            getLogger().logStart(this.mOnCreateLatency, this.mStartLatencyTracker.getLatency(), stringExtra);
            getQsbApplication().onStartupComplete();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean launchSuggestion(SuggestionsAdapter<?> suggestionsAdapter, long j) {
        SuggestionPosition currentSuggestions = getCurrentSuggestions(suggestionsAdapter, j);
        if (currentSuggestions == null) {
            return false;
        }
        this.mTookAction = true;
        getLogger().logSuggestionClick(j, currentSuggestions.getCursor(), 0);
        launchSuggestion(currentSuggestions.getCursor(), currentSuggestions.getPosition());
        return true;
    }

    private void recordOnCreateDone() {
        this.mOnCreateLatency = this.mOnCreateTracker.getLatency();
    }

    private void recordStartTime() {
        this.mStartLatencyTracker = new LatencyTracker();
        this.mOnCreateTracker = new LatencyTracker();
        this.mStarting = true;
        this.mTookAction = false;
    }

    private void setupFromIntent(Intent intent) {
        getCorpusNameFromUri(intent.getData());
        String stringExtra = intent.getStringExtra("query");
        Bundle bundleExtra = intent.getBundleExtra("app_data");
        setQuery(stringExtra, intent.getBooleanExtra("select_query", false));
        this.mAppSearchData = bundleExtra;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateSuggestionsBuffered() {
        this.mHandler.removeCallbacks(this.mUpdateSuggestionsTask);
        this.mHandler.postDelayed(this.mUpdateSuggestionsTask, getConfig().getTypingUpdateSuggestionsDelayMillis());
    }

    public void createMenuItems(Menu menu, boolean z) {
        getQsbApplication().getHelp().addHelpMenuItem(menu, "search");
    }

    protected SuggestionCursor getCurrentSuggestions() {
        if (this.mSearchActivityView == null || this.mSearchActivityView.getSuggestions() == null || this.mSearchActivityView.getSuggestions().getResult() == null) {
            return null;
        }
        return this.mSearchActivityView.getSuggestions().getResult();
    }

    protected SuggestionPosition getCurrentSuggestions(SuggestionsAdapter<?> suggestionsAdapter, long j) {
        SuggestionPosition suggestion = suggestionsAdapter.getSuggestion(j);
        if (suggestion == null) {
            return null;
        }
        SuggestionCursor cursor = suggestion.getCursor();
        int position = suggestion.getPosition();
        if (cursor == null) {
            return null;
        }
        int count = cursor.getCount();
        if (position < 0 || position >= count) {
            Log.w("QSB.SearchActivity", "Invalid suggestion position " + position + ", count = " + count);
            return null;
        }
        cursor.moveTo(position);
        return suggestion;
    }

    protected String getQuery() {
        return this.mSearchActivityView.getQuery();
    }

    protected void launchIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        try {
            startActivity(intent);
        } catch (RuntimeException e) {
            Log.e("QSB.SearchActivity", "Failed to start " + intent.toUri(0), e);
        }
    }

    protected void launchSuggestion(SuggestionCursor suggestionCursor, int i) {
        suggestionCursor.moveTo(i);
        launchIntent(SuggestionUtils.getSuggestionIntent(suggestionCursor, this.mAppSearchData));
    }

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        this.mTraceStartUp = getIntent().hasExtra("trace_start_up");
        if (this.mTraceStartUp) {
            String absolutePath = new File(getDir("traces", 0), "qsb-start.trace").getAbsolutePath();
            Log.i("QSB.SearchActivity", "Writing start-up trace to " + absolutePath);
            Debug.startMethodTracing(absolutePath);
        }
        recordStartTime();
        super.onCreate(bundle);
        QsbApplication.get(this).getSearchBaseUrlHelper();
        this.mSource = QsbApplication.get(this).getGoogleSource();
        this.mSearchActivityView = setupContentView();
        if (getConfig().showScrollingResults()) {
            this.mSearchActivityView.setMaxPromotedResults(getConfig().getMaxPromotedResults());
        } else {
            this.mSearchActivityView.limitResultsToViewHeight();
        }
        this.mSearchActivityView.setSearchClickListener(new SearchActivityView.SearchClickListener(this) { // from class: com.android.quicksearchbox.SearchActivity.3
            final SearchActivity this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.quicksearchbox.ui.SearchActivityView.SearchClickListener
            public boolean onSearchClicked(int i) {
                return this.this$0.onSearchClicked(i);
            }
        });
        this.mSearchActivityView.setQueryListener(new SearchActivityView.QueryListener(this) { // from class: com.android.quicksearchbox.SearchActivity.4
            final SearchActivity this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.quicksearchbox.ui.SearchActivityView.QueryListener
            public void onQueryChanged() {
                this.this$0.updateSuggestionsBuffered();
            }
        });
        this.mSearchActivityView.setSuggestionClickListener(new ClickHandler(this, null));
        this.mSearchActivityView.setVoiceSearchButtonClickListener(new View.OnClickListener(this) { // from class: com.android.quicksearchbox.SearchActivity.5
            final SearchActivity this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                this.this$0.onVoiceSearchClicked();
            }
        });
        this.mSearchActivityView.setExitClickListener(new View.OnClickListener(this) { // from class: com.android.quicksearchbox.SearchActivity.6
            final SearchActivity this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                this.this$0.finish();
            }
        });
        setupFromIntent(getIntent());
        restoreInstanceState(bundle);
        this.mSearchActivityView.start();
        recordOnCreateDone();
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        this.mSearchActivityView.destroy();
        super.onDestroy();
        if (this.mDestroyListener != null) {
            this.mDestroyListener.onDestroyed();
        }
    }

    @Override // android.app.Activity
    protected void onNewIntent(Intent intent) {
        recordStartTime();
        setIntent(intent);
        setupFromIntent(intent);
    }

    @Override // android.app.Activity
    protected void onPause() {
        this.mSearchActivityView.onPause();
        super.onPause();
    }

    @Override // android.app.Activity
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        createMenuItems(menu, true);
        return true;
    }

    @Override // android.app.Activity
    protected void onRestart() {
        super.onRestart();
    }

    @Override // android.app.Activity
    protected void onResume() {
        super.onResume();
        updateSuggestionsBuffered();
        this.mSearchActivityView.onResume();
        if (this.mTraceStartUp) {
            Debug.stopMethodTracing();
        }
    }

    @Override // android.app.Activity
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putString("query", getQuery());
    }

    protected boolean onSearchClicked(int i) {
        String trimAndCollapseFrom = CharMatcher.WHITESPACE.trimAndCollapseFrom(getQuery(), ' ');
        if (TextUtils.getTrimmedLength(trimAndCollapseFrom) == 0) {
            return false;
        }
        this.mTookAction = true;
        getLogger().logSearch(i, trimAndCollapseFrom.length());
        startSearch(this.mSource, trimAndCollapseFrom);
        return true;
    }

    @Override // android.app.Activity
    protected void onStop() {
        if (!this.mTookAction && getCurrentSuggestions() != null) {
            getLogger().logExit(getCurrentSuggestions(), getQuery().length());
        }
        this.mSearchActivityView.clearSuggestions();
        this.mSearchActivityView.onStop();
        super.onStop();
    }

    protected void onVoiceSearchClicked() {
        this.mTookAction = true;
        getLogger().logVoiceSearch();
        launchIntent(this.mSource.createVoiceSearchIntent(this.mAppSearchData));
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public void onWindowFocusChanged(boolean z) {
        super.onWindowFocusChanged(z);
        if (z) {
            this.mHandler.postDelayed(this.mShowInputMethodTask, 0L);
        }
    }

    protected void refineSuggestion(SuggestionsAdapter<?> suggestionsAdapter, long j) {
        SuggestionPosition currentSuggestions = getCurrentSuggestions(suggestionsAdapter, j);
        if (currentSuggestions == null) {
            return;
        }
        String suggestionQuery = currentSuggestions.getSuggestionQuery();
        if (TextUtils.isEmpty(suggestionQuery)) {
            return;
        }
        getLogger().logSuggestionClick(j, currentSuggestions.getCursor(), 1);
        setQuery(suggestionQuery + ' ', false);
        updateSuggestions();
        this.mSearchActivityView.focusQueryTextView();
    }

    protected void restoreInstanceState(Bundle bundle) {
        if (bundle == null) {
            return;
        }
        setQuery(bundle.getString("query"), false);
    }

    @VisibleForTesting
    public void setOnDestroyListener(OnDestroyListener onDestroyListener) {
        this.mDestroyListener = onDestroyListener;
    }

    protected void setQuery(String str, boolean z) {
        this.mSearchActivityView.setQuery(str, z);
    }

    protected SearchActivityView setupContentView() {
        setContentView(2130968579);
        return (SearchActivityView) findViewById(2131689485);
    }

    protected void showSuggestions(Suggestions suggestions) {
        this.mSearchActivityView.setSuggestions(suggestions);
    }

    protected void startSearch(Source source, String str) {
        launchIntent(source.createSearchIntent(str, this.mAppSearchData));
    }

    public void updateSuggestions() {
        updateSuggestions(CharMatcher.WHITESPACE.trimLeadingFrom(getQuery()), this.mSource);
    }

    protected void updateSuggestions(String str, Source source) {
        Suggestions suggestions = getSuggestionsProvider().getSuggestions(str, source);
        gotSuggestions(suggestions);
        showSuggestions(suggestions);
    }
}
