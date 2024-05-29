package com.android.browser;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.browser.provider.BrowserProvider2;
import com.android.browser.search.SearchEngine;
import java.util.ArrayList;
import java.util.List;
/* loaded from: classes.dex */
public class SuggestionsAdapter extends BaseAdapter implements View.OnClickListener, Filterable {
    private static final String[] COMBINED_PROJECTION = {"_id", "title", "url", "bookmark"};
    final Context mContext;
    List<SuggestItem> mFilterResults;
    boolean mIncognitoMode;
    boolean mLandscapeMode;
    final int mLinesLandscape;
    final int mLinesPortrait;
    final CompletionListener mListener;
    SuggestionResults mMixedResults;
    List<CursorSource> mSources;
    List<SuggestItem> mSuggestResults;
    final Object mResultsLock = new Object();
    BrowserSettings mSettings = BrowserSettings.getInstance();
    final Filter mFilter = new SuggestFilter();

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public interface CompletionListener {
        void onSearch(String str);

        void onSelect(String str, int i, String str2);
    }

    public SuggestionsAdapter(Context context, CompletionListener completionListener) {
        this.mContext = context;
        this.mListener = completionListener;
        this.mLinesPortrait = this.mContext.getResources().getInteger(R.integer.max_suggest_lines_portrait);
        this.mLinesLandscape = this.mContext.getResources().getInteger(R.integer.max_suggest_lines_landscape);
        addSource(new CombinedCursor());
    }

    public void setLandscapeMode(boolean z) {
        this.mLandscapeMode = z;
        notifyDataSetChanged();
    }

    public void addSource(CursorSource cursorSource) {
        if (this.mSources == null) {
            this.mSources = new ArrayList(5);
        }
        this.mSources.add(cursorSource);
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        SuggestItem suggestItem = (SuggestItem) ((View) view.getParent()).getTag();
        if (R.id.icon2 == view.getId()) {
            this.mListener.onSearch(getSuggestionUrl(suggestItem));
        } else {
            this.mListener.onSelect(getSuggestionUrl(suggestItem), suggestItem.type, suggestItem.extra);
        }
    }

    @Override // android.widget.Filterable
    public Filter getFilter() {
        return this.mFilter;
    }

    @Override // android.widget.Adapter
    public int getCount() {
        if (this.mMixedResults == null) {
            return 0;
        }
        return this.mMixedResults.getLineCount();
    }

    @Override // android.widget.Adapter
    public SuggestItem getItem(int i) {
        if (this.mMixedResults == null) {
            return null;
        }
        return this.mMixedResults.items.get(i);
    }

    @Override // android.widget.Adapter
    public long getItemId(int i) {
        return i;
    }

    @Override // android.widget.Adapter
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater from = LayoutInflater.from(this.mContext);
        if (view == null) {
            view = from.inflate(R.layout.suggestion_item, viewGroup, false);
        }
        bindView(view, getItem(i));
        return view;
    }

    private void bindView(View view, SuggestItem suggestItem) {
        int i;
        view.setTag(suggestItem);
        TextView textView = (TextView) view.findViewById(16908308);
        TextView textView2 = (TextView) view.findViewById(16908309);
        ImageView imageView = (ImageView) view.findViewById(R.id.icon1);
        View findViewById = view.findViewById(R.id.icon2);
        View findViewById2 = view.findViewById(R.id.divider);
        textView.setText(Html.fromHtml(suggestItem.title));
        int i2 = 0;
        if (TextUtils.isEmpty(suggestItem.url)) {
            textView2.setVisibility(8);
            textView.setMaxLines(2);
        } else {
            textView2.setVisibility(0);
            textView2.setText(suggestItem.url);
            textView.setMaxLines(1);
        }
        switch (suggestItem.type) {
            case 0:
                i = R.drawable.ic_search_category_bookmark;
                break;
            case 1:
                i = R.drawable.ic_search_category_history;
                break;
            case 2:
                i = R.drawable.ic_search_category_browser;
                break;
            case 3:
            case 4:
                i = R.drawable.ic_search_category_suggest;
                break;
            default:
                i = -1;
                break;
        }
        if (i != -1) {
            imageView.setImageDrawable(this.mContext.getResources().getDrawable(i));
        }
        if (4 != suggestItem.type && 3 != suggestItem.type) {
            i2 = 8;
        }
        findViewById.setVisibility(i2);
        findViewById2.setVisibility(findViewById.getVisibility());
        findViewById.setOnClickListener(this);
        view.findViewById(R.id.suggestion).setOnClickListener(this);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class SlowFilterTask extends AsyncTask<CharSequence, Void, List<SuggestItem>> {
        SlowFilterTask() {
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public List<SuggestItem> doInBackground(CharSequence... charSequenceArr) {
            SuggestCursor suggestCursor = new SuggestCursor();
            suggestCursor.runQuery(charSequenceArr[0]);
            ArrayList arrayList = new ArrayList();
            int count = suggestCursor.getCount();
            for (int i = 0; i < count; i++) {
                arrayList.add(suggestCursor.getItem());
                suggestCursor.moveToNext();
            }
            suggestCursor.close();
            return arrayList;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(List<SuggestItem> list) {
            SuggestionsAdapter.this.mSuggestResults = list;
            SuggestionsAdapter.this.mMixedResults = SuggestionsAdapter.this.buildSuggestionResults();
            SuggestionsAdapter.this.notifyDataSetChanged();
        }
    }

    SuggestionResults buildSuggestionResults() {
        List<SuggestItem> list;
        List<SuggestItem> list2;
        SuggestionResults suggestionResults = new SuggestionResults();
        synchronized (this.mResultsLock) {
            list = this.mFilterResults;
            list2 = this.mSuggestResults;
        }
        if (list != null) {
            for (SuggestItem suggestItem : list) {
                suggestionResults.addResult(suggestItem);
            }
        }
        if (list2 != null) {
            for (SuggestItem suggestItem2 : list2) {
                suggestionResults.addResult(suggestItem2);
            }
        }
        return suggestionResults;
    }

    /* loaded from: classes.dex */
    class SuggestFilter extends Filter {
        SuggestFilter() {
        }

        @Override // android.widget.Filter
        public CharSequence convertResultToString(Object obj) {
            if (obj == null) {
                return "";
            }
            SuggestItem suggestItem = (SuggestItem) obj;
            if (suggestItem.title != null) {
                return suggestItem.title;
            }
            return suggestItem.url;
        }

        void startSuggestionsAsync(CharSequence charSequence) {
            if (!SuggestionsAdapter.this.mIncognitoMode) {
                new SlowFilterTask().execute(charSequence);
            }
        }

        private boolean shouldProcessEmptyQuery() {
            return SuggestionsAdapter.this.mSettings.getSearchEngine().wantsEmptyQuery();
        }

        @Override // android.widget.Filter
        protected Filter.FilterResults performFiltering(CharSequence charSequence) {
            Filter.FilterResults filterResults = new Filter.FilterResults();
            if (TextUtils.isEmpty(charSequence) && !shouldProcessEmptyQuery()) {
                filterResults.count = 0;
                filterResults.values = null;
                return filterResults;
            }
            startSuggestionsAsync(charSequence);
            ArrayList arrayList = new ArrayList();
            if (charSequence != null) {
                for (CursorSource cursorSource : SuggestionsAdapter.this.mSources) {
                    cursorSource.runQuery(charSequence);
                }
                mixResults(arrayList);
            }
            synchronized (SuggestionsAdapter.this.mResultsLock) {
                SuggestionsAdapter.this.mFilterResults = arrayList;
            }
            SuggestionResults buildSuggestionResults = SuggestionsAdapter.this.buildSuggestionResults();
            filterResults.count = buildSuggestionResults.getLineCount();
            filterResults.values = buildSuggestionResults;
            return filterResults;
        }

        void mixResults(List<SuggestItem> list) {
            int maxLines = SuggestionsAdapter.this.getMaxLines();
            for (int i = 0; i < SuggestionsAdapter.this.mSources.size(); i++) {
                CursorSource cursorSource = SuggestionsAdapter.this.mSources.get(i);
                int min = Math.min(cursorSource.getCount(), maxLines);
                maxLines -= min;
                for (int i2 = 0; i2 < min; i2++) {
                    list.add(cursorSource.getItem());
                    cursorSource.moveToNext();
                }
                cursorSource.close();
            }
        }

        @Override // android.widget.Filter
        protected void publishResults(CharSequence charSequence, Filter.FilterResults filterResults) {
            if (filterResults.values instanceof SuggestionResults) {
                SuggestionsAdapter.this.mMixedResults = (SuggestionResults) filterResults.values;
                SuggestionsAdapter.this.notifyDataSetChanged();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int getMaxLines() {
        return (int) Math.ceil((this.mLandscapeMode ? this.mLinesLandscape : this.mLinesPortrait) / 2.0d);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class SuggestionResults {
        ArrayList<SuggestItem> items = new ArrayList<>(24);
        int[] counts = new int[5];

        SuggestionResults() {
        }

        void addResult(SuggestItem suggestItem) {
            int i = 0;
            while (i < this.items.size() && suggestItem.type >= this.items.get(i).type) {
                i++;
            }
            this.items.add(i, suggestItem);
            int[] iArr = this.counts;
            int i2 = suggestItem.type;
            iArr[i2] = iArr[i2] + 1;
        }

        int getLineCount() {
            return Math.min(SuggestionsAdapter.this.mLandscapeMode ? SuggestionsAdapter.this.mLinesLandscape : SuggestionsAdapter.this.mLinesPortrait, this.items.size());
        }

        public String toString() {
            if (this.items == null) {
                return null;
            }
            if (this.items.size() == 0) {
                return "[]";
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < this.items.size(); i++) {
                SuggestItem suggestItem = this.items.get(i);
                sb.append(suggestItem.type + ": " + suggestItem.title);
                if (i < this.items.size() - 1) {
                    sb.append(", ");
                }
            }
            return sb.toString();
        }
    }

    /* loaded from: classes.dex */
    public class SuggestItem {
        public String extra;
        public String title;
        public int type;
        public String url;

        public SuggestItem(String str, String str2, int i) {
            this.title = str;
            this.url = str2;
            this.type = i;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public abstract class CursorSource {
        Cursor mCursor;

        public abstract SuggestItem getItem();

        public abstract void runQuery(CharSequence charSequence);

        CursorSource() {
        }

        boolean moveToNext() {
            return this.mCursor.moveToNext();
        }

        public int getCount() {
            if (this.mCursor != null) {
                return this.mCursor.getCount();
            }
            return 0;
        }

        public void close() {
            if (this.mCursor != null) {
                this.mCursor.close();
            }
        }
    }

    /* loaded from: classes.dex */
    class CombinedCursor extends CursorSource {
        CombinedCursor() {
            super();
        }

        @Override // com.android.browser.SuggestionsAdapter.CursorSource
        public SuggestItem getItem() {
            int i;
            if (this.mCursor != null && !this.mCursor.isAfterLast()) {
                String string = this.mCursor.getString(1);
                String string2 = this.mCursor.getString(2);
                if (this.mCursor.getInt(3) != 1) {
                    i = 0;
                } else {
                    i = 1;
                }
                return new SuggestItem(getTitle(string, string2), getUrl(string, string2), 1 ^ i);
            }
            return null;
        }

        @Override // com.android.browser.SuggestionsAdapter.CursorSource
        public void runQuery(CharSequence charSequence) {
            String[] strArr;
            String str;
            if (this.mCursor != null) {
                this.mCursor.close();
            }
            String str2 = ((Object) charSequence) + "%";
            if (str2.startsWith("http") || str2.startsWith("file")) {
                strArr = new String[]{str2};
                str = "url LIKE ?";
            } else {
                strArr = new String[]{"http://" + str2, "http://www." + str2, "https://" + str2, "https://www." + str2, str2};
                str = "(url LIKE ? OR url LIKE ? OR url LIKE ? OR url LIKE ? OR title LIKE ?)";
            }
            String str3 = str;
            Uri.Builder buildUpon = BrowserProvider2.OmniboxSuggestions.CONTENT_URI.buildUpon();
            buildUpon.appendQueryParameter("limit", Integer.toString(Math.max(SuggestionsAdapter.this.mLinesLandscape, SuggestionsAdapter.this.mLinesPortrait)));
            this.mCursor = SuggestionsAdapter.this.mContext.getContentResolver().query(buildUpon.build(), SuggestionsAdapter.COMBINED_PROJECTION, str3, charSequence != null ? strArr : null, null);
            if (this.mCursor != null) {
                this.mCursor.moveToFirst();
            }
        }

        private String getTitle(String str, String str2) {
            if (TextUtils.isEmpty(str) || TextUtils.getTrimmedLength(str) == 0) {
                return UrlUtils.stripUrl(str2);
            }
            return str;
        }

        private String getUrl(String str, String str2) {
            if (TextUtils.isEmpty(str) || TextUtils.getTrimmedLength(str) == 0 || str.equals(str2)) {
                return null;
            }
            return UrlUtils.stripUrl(str2);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class SuggestCursor extends CursorSource {
        SuggestCursor() {
            super();
        }

        @Override // com.android.browser.SuggestionsAdapter.CursorSource
        public SuggestItem getItem() {
            if (this.mCursor != null && !this.mCursor.isClosed()) {
                String string = this.mCursor.getString(this.mCursor.getColumnIndex("suggest_text_1"));
                this.mCursor.getString(this.mCursor.getColumnIndex("suggest_text_2"));
                String string2 = this.mCursor.getString(this.mCursor.getColumnIndex("suggest_text_2_url"));
                this.mCursor.getString(this.mCursor.getColumnIndex("suggest_intent_data"));
                SuggestItem suggestItem = new SuggestItem(string, string2, TextUtils.isEmpty(string2) ? 4 : 2);
                suggestItem.extra = this.mCursor.getString(this.mCursor.getColumnIndex("suggest_intent_extra_data"));
                return suggestItem;
            }
            return null;
        }

        @Override // com.android.browser.SuggestionsAdapter.CursorSource
        public void runQuery(CharSequence charSequence) {
            if (this.mCursor != null) {
                this.mCursor.close();
            }
            SearchEngine searchEngine = SuggestionsAdapter.this.mSettings.getSearchEngine();
            if (!TextUtils.isEmpty(charSequence)) {
                if (searchEngine != null && searchEngine.supportsSuggestions()) {
                    this.mCursor = searchEngine.getSuggestions(SuggestionsAdapter.this.mContext, charSequence.toString());
                    if (this.mCursor != null) {
                        this.mCursor.moveToFirst();
                        return;
                    }
                    return;
                }
                return;
            }
            if (searchEngine.wantsEmptyQuery()) {
                this.mCursor = searchEngine.getSuggestions(SuggestionsAdapter.this.mContext, "");
            }
            this.mCursor = null;
        }
    }

    public void clearCache() {
        this.mFilterResults = null;
        this.mSuggestResults = null;
        notifyDataSetInvalidated();
    }

    public void setIncognitoMode(boolean z) {
        this.mIncognitoMode = z;
        clearCache();
    }

    static String getSuggestionTitle(SuggestItem suggestItem) {
        if (suggestItem.title != null) {
            return Html.fromHtml(suggestItem.title).toString();
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static String getSuggestionUrl(SuggestItem suggestItem) {
        String suggestionTitle = getSuggestionTitle(suggestItem);
        if (TextUtils.isEmpty(suggestItem.url)) {
            return suggestionTitle;
        }
        return suggestItem.url;
    }
}
