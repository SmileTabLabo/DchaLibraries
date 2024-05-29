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
/* loaded from: b.zip:com/android/browser/SuggestionsAdapter.class */
public class SuggestionsAdapter extends BaseAdapter implements Filterable, View.OnClickListener {
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
    final Filter mFilter = new SuggestFilter(this);

    /* loaded from: b.zip:com/android/browser/SuggestionsAdapter$CombinedCursor.class */
    class CombinedCursor extends CursorSource {
        final SuggestionsAdapter this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        CombinedCursor(SuggestionsAdapter suggestionsAdapter) {
            super(suggestionsAdapter);
            this.this$0 = suggestionsAdapter;
        }

        /* JADX WARN: Code restructure failed: missing block: B:5:0x000d, code lost:
            if (android.text.TextUtils.getTrimmedLength(r3) == 0) goto L8;
         */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
        */
        private String getTitle(String str, String str2) {
            String stripUrl;
            if (!TextUtils.isEmpty(str)) {
                stripUrl = str;
            }
            stripUrl = UrlUtils.stripUrl(str2);
            return stripUrl;
        }

        private String getUrl(String str, String str2) {
            if (TextUtils.isEmpty(str) || TextUtils.getTrimmedLength(str) == 0 || str.equals(str2)) {
                return null;
            }
            return UrlUtils.stripUrl(str2);
        }

        @Override // com.android.browser.SuggestionsAdapter.CursorSource
        public SuggestItem getItem() {
            int i = 1;
            if (this.mCursor == null || this.mCursor.isAfterLast()) {
                return null;
            }
            String string = this.mCursor.getString(1);
            String string2 = this.mCursor.getString(2);
            boolean z = this.mCursor.getInt(3) == 1;
            SuggestionsAdapter suggestionsAdapter = this.this$0;
            String title = getTitle(string, string2);
            String url = getUrl(string, string2);
            if (z) {
                i = 0;
            }
            return new SuggestItem(suggestionsAdapter, title, url, i);
        }

        @Override // com.android.browser.SuggestionsAdapter.CursorSource
        public void runQuery(CharSequence charSequence) {
            String[] strArr;
            String str;
            if (this.mCursor != null) {
                this.mCursor.close();
            }
            String str2 = charSequence + "%";
            if (str2.startsWith("http") || str2.startsWith("file")) {
                strArr = new String[]{str2};
                str = "url LIKE ?";
            } else {
                strArr = new String[]{"http://" + str2, "http://www." + str2, "https://" + str2, "https://www." + str2, str2};
                str = "(url LIKE ? OR url LIKE ? OR url LIKE ? OR url LIKE ? OR title LIKE ?)";
            }
            Uri.Builder buildUpon = BrowserProvider2.OmniboxSuggestions.CONTENT_URI.buildUpon();
            buildUpon.appendQueryParameter("limit", Integer.toString(Math.max(this.this$0.mLinesLandscape, this.this$0.mLinesPortrait)));
            this.mCursor = this.this$0.mContext.getContentResolver().query(buildUpon.build(), SuggestionsAdapter.COMBINED_PROJECTION, str, charSequence != null ? strArr : null, null);
            if (this.mCursor != null) {
                this.mCursor.moveToFirst();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: b.zip:com/android/browser/SuggestionsAdapter$CompletionListener.class */
    public interface CompletionListener {
        void onSearch(String str);

        void onSelect(String str, int i, String str2);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: b.zip:com/android/browser/SuggestionsAdapter$CursorSource.class */
    public abstract class CursorSource {
        Cursor mCursor;
        final SuggestionsAdapter this$0;

        CursorSource(SuggestionsAdapter suggestionsAdapter) {
            this.this$0 = suggestionsAdapter;
        }

        public void close() {
            if (this.mCursor != null) {
                this.mCursor.close();
            }
        }

        public int getCount() {
            return this.mCursor != null ? this.mCursor.getCount() : 0;
        }

        public abstract SuggestItem getItem();

        boolean moveToNext() {
            return this.mCursor.moveToNext();
        }

        public abstract void runQuery(CharSequence charSequence);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: b.zip:com/android/browser/SuggestionsAdapter$SlowFilterTask.class */
    public class SlowFilterTask extends AsyncTask<CharSequence, Void, List<SuggestItem>> {
        final SuggestionsAdapter this$0;

        SlowFilterTask(SuggestionsAdapter suggestionsAdapter) {
            this.this$0 = suggestionsAdapter;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public List<SuggestItem> doInBackground(CharSequence... charSequenceArr) {
            SuggestCursor suggestCursor = new SuggestCursor(this.this$0);
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
            this.this$0.mSuggestResults = list;
            this.this$0.mMixedResults = this.this$0.buildSuggestionResults();
            this.this$0.notifyDataSetChanged();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: b.zip:com/android/browser/SuggestionsAdapter$SuggestCursor.class */
    public class SuggestCursor extends CursorSource {
        final SuggestionsAdapter this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        SuggestCursor(SuggestionsAdapter suggestionsAdapter) {
            super(suggestionsAdapter);
            this.this$0 = suggestionsAdapter;
        }

        @Override // com.android.browser.SuggestionsAdapter.CursorSource
        public SuggestItem getItem() {
            if (this.mCursor != null) {
                String string = this.mCursor.getString(this.mCursor.getColumnIndex("suggest_text_1"));
                this.mCursor.getString(this.mCursor.getColumnIndex("suggest_text_2"));
                String string2 = this.mCursor.getString(this.mCursor.getColumnIndex("suggest_text_2_url"));
                this.mCursor.getString(this.mCursor.getColumnIndex("suggest_intent_data"));
                SuggestItem suggestItem = new SuggestItem(this.this$0, string, string2, TextUtils.isEmpty(string2) ? 4 : 2);
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
            SearchEngine searchEngine = this.this$0.mSettings.getSearchEngine();
            if (TextUtils.isEmpty(charSequence)) {
                if (searchEngine.wantsEmptyQuery()) {
                    this.mCursor = searchEngine.getSuggestions(this.this$0.mContext, "");
                }
                this.mCursor = null;
            } else if (searchEngine == null || !searchEngine.supportsSuggestions()) {
            } else {
                this.mCursor = searchEngine.getSuggestions(this.this$0.mContext, charSequence.toString());
                if (this.mCursor != null) {
                    this.mCursor.moveToFirst();
                }
            }
        }
    }

    /* loaded from: b.zip:com/android/browser/SuggestionsAdapter$SuggestFilter.class */
    class SuggestFilter extends Filter {
        final SuggestionsAdapter this$0;

        SuggestFilter(SuggestionsAdapter suggestionsAdapter) {
            this.this$0 = suggestionsAdapter;
        }

        private boolean shouldProcessEmptyQuery() {
            return this.this$0.mSettings.getSearchEngine().wantsEmptyQuery();
        }

        @Override // android.widget.Filter
        public CharSequence convertResultToString(Object obj) {
            if (obj == null) {
                return "";
            }
            SuggestItem suggestItem = (SuggestItem) obj;
            return suggestItem.title != null ? suggestItem.title : suggestItem.url;
        }

        void mixResults(List<SuggestItem> list) {
            int maxLines = this.this$0.getMaxLines();
            int i = 0;
            while (i < this.this$0.mSources.size()) {
                CursorSource cursorSource = this.this$0.mSources.get(i);
                int min = Math.min(cursorSource.getCount(), maxLines);
                int i2 = maxLines - min;
                for (int i3 = 0; i3 < min; i3++) {
                    list.add(cursorSource.getItem());
                    cursorSource.moveToNext();
                }
                cursorSource.close();
                i++;
                maxLines = i2;
            }
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
                for (CursorSource cursorSource : this.this$0.mSources) {
                    cursorSource.runQuery(charSequence);
                }
                mixResults(arrayList);
            }
            synchronized (this.this$0.mResultsLock) {
                this.this$0.mFilterResults = arrayList;
            }
            SuggestionResults buildSuggestionResults = this.this$0.buildSuggestionResults();
            filterResults.count = buildSuggestionResults.getLineCount();
            filterResults.values = buildSuggestionResults;
            return filterResults;
        }

        @Override // android.widget.Filter
        protected void publishResults(CharSequence charSequence, Filter.FilterResults filterResults) {
            if (filterResults.values instanceof SuggestionResults) {
                this.this$0.mMixedResults = (SuggestionResults) filterResults.values;
                this.this$0.notifyDataSetChanged();
            }
        }

        void startSuggestionsAsync(CharSequence charSequence) {
            if (this.this$0.mIncognitoMode) {
                return;
            }
            new SlowFilterTask(this.this$0).execute(charSequence);
        }
    }

    /* loaded from: b.zip:com/android/browser/SuggestionsAdapter$SuggestItem.class */
    public class SuggestItem {
        public String extra;
        final SuggestionsAdapter this$0;
        public String title;
        public int type;
        public String url;

        public SuggestItem(SuggestionsAdapter suggestionsAdapter, String str, String str2, int i) {
            this.this$0 = suggestionsAdapter;
            this.title = str;
            this.url = str2;
            this.type = i;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: b.zip:com/android/browser/SuggestionsAdapter$SuggestionResults.class */
    public class SuggestionResults {
        final SuggestionsAdapter this$0;
        ArrayList<SuggestItem> items = new ArrayList<>(24);
        int[] counts = new int[5];

        SuggestionResults(SuggestionsAdapter suggestionsAdapter) {
            this.this$0 = suggestionsAdapter;
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
            return Math.min(this.this$0.mLandscapeMode ? this.this$0.mLinesLandscape : this.this$0.mLinesPortrait, this.items.size());
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
                sb.append(suggestItem.type).append(": ").append(suggestItem.title);
                if (i < this.items.size() - 1) {
                    sb.append(", ");
                }
            }
            return sb.toString();
        }
    }

    public SuggestionsAdapter(Context context, CompletionListener completionListener) {
        this.mContext = context;
        this.mListener = completionListener;
        this.mLinesPortrait = this.mContext.getResources().getInteger(2131623937);
        this.mLinesLandscape = this.mContext.getResources().getInteger(2131623936);
        addSource(new CombinedCursor(this));
    }

    /* JADX WARN: Code restructure failed: missing block: B:14:0x00ae, code lost:
        if (3 == r6.type) goto L17;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private void bindView(View view, SuggestItem suggestItem) {
        int i;
        int i2;
        view.setTag(suggestItem);
        TextView textView = (TextView) view.findViewById(16908308);
        TextView textView2 = (TextView) view.findViewById(16908309);
        ImageView imageView = (ImageView) view.findViewById(2131558478);
        View findViewById = view.findViewById(2131558520);
        View findViewById2 = view.findViewById(2131558431);
        textView.setText(Html.fromHtml(suggestItem.title));
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
                i = 2130837579;
                break;
            case 1:
                i = 2130837581;
                break;
            case 2:
                i = 2130837580;
                break;
            case 3:
            case 4:
                i = 2130837582;
                break;
            default:
                i = -1;
                break;
        }
        if (i != -1) {
            imageView.setImageDrawable(this.mContext.getResources().getDrawable(i));
        }
        if (4 != suggestItem.type) {
            i2 = 8;
        }
        i2 = 0;
        findViewById.setVisibility(i2);
        findViewById2.setVisibility(findViewById.getVisibility());
        findViewById.setOnClickListener(this);
        view.findViewById(2131558519).setOnClickListener(this);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int getMaxLines() {
        return (int) Math.ceil((this.mLandscapeMode ? this.mLinesLandscape : this.mLinesPortrait) / 2.0d);
    }

    static String getSuggestionTitle(SuggestItem suggestItem) {
        String str = null;
        if (suggestItem.title != null) {
            str = Html.fromHtml(suggestItem.title).toString();
        }
        return str;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static String getSuggestionUrl(SuggestItem suggestItem) {
        return TextUtils.isEmpty(suggestItem.url) ? getSuggestionTitle(suggestItem) : suggestItem.url;
    }

    public void addSource(CursorSource cursorSource) {
        if (this.mSources == null) {
            this.mSources = new ArrayList(5);
        }
        this.mSources.add(cursorSource);
    }

    SuggestionResults buildSuggestionResults() {
        List<SuggestItem> list;
        List<SuggestItem> list2;
        SuggestionResults suggestionResults = new SuggestionResults(this);
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

    public void clearCache() {
        this.mFilterResults = null;
        this.mSuggestResults = null;
        notifyDataSetInvalidated();
    }

    @Override // android.widget.Adapter
    public int getCount() {
        return this.mMixedResults == null ? 0 : this.mMixedResults.getLineCount();
    }

    @Override // android.widget.Filterable
    public Filter getFilter() {
        return this.mFilter;
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
        View view2 = view;
        if (view == null) {
            view2 = from.inflate(2130968625, viewGroup, false);
        }
        bindView(view2, getItem(i));
        return view2;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        SuggestItem suggestItem = (SuggestItem) ((View) view.getParent()).getTag();
        if (2131558520 == view.getId()) {
            this.mListener.onSearch(getSuggestionUrl(suggestItem));
        } else {
            this.mListener.onSelect(getSuggestionUrl(suggestItem), suggestItem.type, suggestItem.extra);
        }
    }

    public void setIncognitoMode(boolean z) {
        this.mIncognitoMode = z;
        clearCache();
    }

    public void setLandscapeMode(boolean z) {
        this.mLandscapeMode = z;
        notifyDataSetChanged();
    }
}
