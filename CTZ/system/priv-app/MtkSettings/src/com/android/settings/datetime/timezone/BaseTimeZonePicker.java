package com.android.settings.datetime.timezone;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.core.InstrumentedFragment;
import com.android.settings.datetime.timezone.BaseTimeZoneAdapter;
import com.android.settings.datetime.timezone.model.TimeZoneData;
import com.android.settings.datetime.timezone.model.TimeZoneDataLoader;
import java.util.Locale;
/* loaded from: classes.dex */
public abstract class BaseTimeZonePicker extends InstrumentedFragment implements SearchView.OnQueryTextListener {
    private BaseTimeZoneAdapter mAdapter;
    private final boolean mDefaultExpandSearch;
    private RecyclerView mRecyclerView;
    private final boolean mSearchEnabled;
    private final int mSearchHintResId;
    private SearchView mSearchView;
    private TimeZoneData mTimeZoneData;
    private final int mTitleResId;

    /* loaded from: classes.dex */
    public interface OnListItemClickListener<T extends BaseTimeZoneAdapter.AdapterItem> {
        void onListItemClick(T t);
    }

    protected abstract BaseTimeZoneAdapter createAdapter(TimeZoneData timeZoneData);

    /* JADX INFO: Access modifiers changed from: protected */
    public BaseTimeZonePicker(int i, int i2, boolean z, boolean z2) {
        this.mTitleResId = i;
        this.mSearchHintResId = i2;
        this.mSearchEnabled = z;
        this.mDefaultExpandSearch = z2;
    }

    @Override // com.android.settingslib.core.lifecycle.ObservableFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setHasOptionsMenu(true);
        getActivity().setTitle(this.mTitleResId);
    }

    @Override // android.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View inflate = layoutInflater.inflate(R.layout.time_zone_items_list, viewGroup, false);
        this.mRecyclerView = (RecyclerView) inflate.findViewById(R.id.recycler_view);
        this.mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), 1, false));
        this.mRecyclerView.setAdapter(this.mAdapter);
        getLoaderManager().initLoader(0, null, new TimeZoneDataLoader.LoaderCreator(getContext(), new TimeZoneDataLoader.OnDataReadyCallback() { // from class: com.android.settings.datetime.timezone.-$$Lambda$MBKbnic3yruONZHLQGUj0vAB5hk
            @Override // com.android.settings.datetime.timezone.model.TimeZoneDataLoader.OnDataReadyCallback
            public final void onTimeZoneDataReady(TimeZoneData timeZoneData) {
                BaseTimeZonePicker.this.onTimeZoneDataReady(timeZoneData);
            }
        }));
        return inflate;
    }

    public void onTimeZoneDataReady(TimeZoneData timeZoneData) {
        if (this.mTimeZoneData == null && timeZoneData != null) {
            this.mTimeZoneData = timeZoneData;
            this.mAdapter = createAdapter(this.mTimeZoneData);
            if (this.mRecyclerView != null) {
                this.mRecyclerView.setAdapter(this.mAdapter);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public Locale getLocale() {
        return getContext().getResources().getConfiguration().getLocales().get(0);
    }

    @Override // com.android.settingslib.core.lifecycle.ObservableFragment, android.app.Fragment
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        if (this.mSearchEnabled) {
            menuInflater.inflate(R.menu.time_zone_base_search_menu, menu);
            MenuItem findItem = menu.findItem(R.id.time_zone_search_menu);
            this.mSearchView = (SearchView) findItem.getActionView();
            this.mSearchView.setQueryHint(getText(this.mSearchHintResId));
            this.mSearchView.setOnQueryTextListener(this);
            if (this.mDefaultExpandSearch) {
                findItem.expandActionView();
                this.mSearchView.setIconified(false);
                this.mSearchView.setActivated(true);
                this.mSearchView.setQuery("", true);
            }
            TextView textView = (TextView) this.mSearchView.findViewById(16909268);
            textView.setPadding(0, textView.getPaddingTop(), 0, textView.getPaddingBottom());
            View findViewById = this.mSearchView.findViewById(16909264);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) findViewById.getLayoutParams();
            layoutParams.setMarginStart(0);
            layoutParams.setMarginEnd(0);
            findViewById.setLayoutParams(layoutParams);
        }
    }

    @Override // android.widget.SearchView.OnQueryTextListener
    public boolean onQueryTextSubmit(String str) {
        return false;
    }

    @Override // android.widget.SearchView.OnQueryTextListener
    public boolean onQueryTextChange(String str) {
        if (this.mAdapter != null) {
            this.mAdapter.getFilter().filter(str);
            return false;
        }
        return false;
    }
}
