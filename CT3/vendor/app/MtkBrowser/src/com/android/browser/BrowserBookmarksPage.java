package com.android.browser;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.Toast;
import com.android.browser.BreadCrumbView;
import com.android.browser.provider.BrowserContract;
import com.android.browser.view.BookmarkExpandableView;
import com.mediatek.browser.ext.IBrowserBookmarkExt;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;
/* loaded from: b.zip:com/android/browser/BrowserBookmarksPage.class */
public class BrowserBookmarksPage extends Fragment implements View.OnCreateContextMenuListener, LoaderManager.LoaderCallbacks<Cursor>, BreadCrumbView.Controller, ExpandableListView.OnChildClickListener {
    static ThreadLocal<BitmapFactory.Options> sOptions = new ThreadLocal<BitmapFactory.Options>() { // from class: com.android.browser.BrowserBookmarksPage.1
        /* JADX INFO: Access modifiers changed from: protected */
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // java.lang.ThreadLocal
        public BitmapFactory.Options initialValue() {
            return new BitmapFactory.Options();
        }
    };
    BookmarksPageCallbacks mCallbacks;
    boolean mDisableNewWindow;
    View mEmptyView;
    BookmarkExpandableView mGrid;
    View mRoot;
    JSONObject mState;
    boolean mEnableContextMenu = true;
    HashMap<Integer, BrowserBookmarksAdapter> mBookmarkAdapters = new HashMap<>();
    long mCurrentFolderId = 1;
    private IBrowserBookmarkExt mBrowserBookmarkExt = null;
    private MenuItem.OnMenuItemClickListener mContextItemClickListener = new MenuItem.OnMenuItemClickListener(this) { // from class: com.android.browser.BrowserBookmarksPage.2
        final BrowserBookmarksPage this$0;

        {
            this.this$0 = this;
        }

        @Override // android.view.MenuItem.OnMenuItemClickListener
        public boolean onMenuItemClick(MenuItem menuItem) {
            return this.this$0.onContextItemSelected(menuItem);
        }
    };

    /* loaded from: b.zip:com/android/browser/BrowserBookmarksPage$AccountsLoader.class */
    static class AccountsLoader extends CursorLoader {
        static String[] ACCOUNTS_PROJECTION = {"account_name", "account_type"};

        public AccountsLoader(Context context) {
            super(context, BrowserContract.Accounts.CONTENT_URI.buildUpon().appendQueryParameter("allowEmptyAccounts", "true").build(), ACCOUNTS_PROJECTION, null, null, null);
        }
    }

    /* loaded from: b.zip:com/android/browser/BrowserBookmarksPage$CombinedBookmarksCallbackWrapper.class */
    private static class CombinedBookmarksCallbackWrapper implements BookmarksPageCallbacks {
        private CombinedBookmarksCallbacks mCombinedCallback;

        private CombinedBookmarksCallbackWrapper(CombinedBookmarksCallbacks combinedBookmarksCallbacks) {
            this.mCombinedCallback = combinedBookmarksCallbacks;
        }

        /* synthetic */ CombinedBookmarksCallbackWrapper(CombinedBookmarksCallbacks combinedBookmarksCallbacks, CombinedBookmarksCallbackWrapper combinedBookmarksCallbackWrapper) {
            this(combinedBookmarksCallbacks);
        }

        @Override // com.android.browser.BookmarksPageCallbacks
        public boolean onBookmarkSelected(Cursor cursor, boolean z) {
            if (z) {
                return false;
            }
            this.mCombinedCallback.openUrl(BrowserBookmarksPage.getUrl(cursor));
            return true;
        }

        @Override // com.android.browser.BookmarksPageCallbacks
        public boolean onOpenInNewWindow(String... strArr) {
            this.mCombinedCallback.openInNewTab(strArr);
            return true;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: b.zip:com/android/browser/BrowserBookmarksPage$LookupBookmarkCount.class */
    public static class LookupBookmarkCount extends AsyncTask<Long, Void, Integer> {
        Context mContext;
        BookmarkItem mHeader;

        public LookupBookmarkCount(Context context, BookmarkItem bookmarkItem) {
            this.mContext = context.getApplicationContext();
            this.mHeader = bookmarkItem;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public Integer doInBackground(Long... lArr) {
            if (lArr.length != 1) {
                throw new IllegalArgumentException("Missing folder id!");
            }
            Cursor cursor = null;
            int i = 0;
            try {
                Cursor query = this.mContext.getContentResolver().query(BookmarkUtils.getBookmarksUri(this.mContext), null, "parent=? AND folder ==0", new String[]{lArr[0].toString()}, null);
                if (query != null) {
                    cursor = query;
                    i = query.getCount();
                }
                if (query != null) {
                    query.close();
                }
                return Integer.valueOf(i);
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
                throw th;
            }
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(Integer num) {
            if (num.intValue() > 0) {
                this.mHeader.setUrl(this.mContext.getString(2131493034, num));
            } else if (num.intValue() == 0) {
                this.mHeader.setUrl(this.mContext.getString(2131493035));
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: b.zip:com/android/browser/BrowserBookmarksPage$OpenAllInTabsTask.class */
    public class OpenAllInTabsTask extends AsyncTask<Void, Void, ArrayList<String>> {
        long mFolderId;
        ArrayList<String> mUrls = new ArrayList<>();
        final BrowserBookmarksPage this$0;

        public OpenAllInTabsTask(BrowserBookmarksPage browserBookmarksPage, long j) {
            this.this$0 = browserBookmarksPage;
            this.mFolderId = j;
        }

        private void getChildrenUrls(Context context, long j) {
            Cursor query = context.getContentResolver().query(BookmarkUtils.getBookmarksUri(context), BookmarksLoader.PROJECTION, "parent=?", new String[]{Long.toString(j)}, null);
            if (query != null && query.getCount() == 0) {
                query.close();
            } else if (query != null) {
                while (query.moveToNext()) {
                    if (query.getInt(6) == 0) {
                        this.mUrls.add(query.getString(1));
                    } else {
                        getChildrenUrls(context, query.getLong(0));
                    }
                }
                query.close();
            }
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public ArrayList<String> doInBackground(Void... voidArr) {
            Activity activity = this.this$0.getActivity();
            if (activity == null) {
                return null;
            }
            getChildrenUrls(activity, this.mFolderId);
            return this.mUrls;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(ArrayList<String> arrayList) {
            if (arrayList != null && arrayList.size() == 0) {
                Activity activity = this.this$0.getActivity();
                Toast.makeText(activity, activity.getString(2131493035), 1).show();
            } else if (this.this$0.mCallbacks == null || arrayList == null || arrayList.size() <= 0) {
            } else {
                this.this$0.mCallbacks.onOpenInNewWindow((String[]) this.mUrls.toArray(new String[0]));
            }
        }
    }

    private void copy(CharSequence charSequence) {
        ((ClipboardManager) getActivity().getSystemService("clipboard")).setPrimaryClip(ClipData.newRawUri(null, Uri.parse(charSequence.toString())));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static Intent createShortcutIntent(Context context, Cursor cursor) {
        return BookmarkUtils.createAddToHomeIntent(context, cursor.getString(1), cursor.getString(2), getBitmap(cursor, 5), getBitmap(cursor, 3));
    }

    private void displayRemoveBookmarkDialog(BrowserBookmarksAdapter browserBookmarksAdapter, int i) {
        Cursor item = browserBookmarksAdapter.getItem(i);
        long j = item.getLong(0);
        String string = item.getString(2);
        Activity activity = getActivity();
        boolean z = false;
        if (item.getInt(6) != 0) {
            z = true;
        }
        if (z) {
            BookmarkUtils.displayRemoveFolderDialog(j, string, activity, null);
        } else {
            BookmarkUtils.displayRemoveBookmarkDialog(j, string, activity, null);
        }
    }

    private void editBookmark(BrowserBookmarksAdapter browserBookmarksAdapter, int i) {
        boolean z = true;
        Intent intent = new Intent(getActivity(), AddBookmarkPage.class);
        Cursor item = browserBookmarksAdapter.getItem(i);
        Bundle bundle = new Bundle();
        bundle.putString("title", item.getString(2));
        bundle.putString("url", item.getString(1));
        byte[] blob = item.getBlob(3);
        if (blob != null) {
            Bitmap decodeByteArray = BitmapFactory.decodeByteArray(blob, 0, blob.length);
            Bitmap bitmap = decodeByteArray;
            if (decodeByteArray != null) {
                bitmap = decodeByteArray;
                if (decodeByteArray.getWidth() > 60) {
                    bitmap = Bitmap.createScaledBitmap(decodeByteArray, 60, 60, true);
                }
            }
            bundle.putParcelable("favicon", bitmap);
        }
        bundle.putLong("_id", item.getLong(0));
        bundle.putLong("parent", item.getLong(8));
        intent.putExtra("bookmark", bundle);
        if (item.getInt(6) != 1) {
            z = false;
        }
        intent.putExtra("is_folder", z);
        startActivity(intent);
    }

    static Bitmap getBitmap(Cursor cursor, int i) {
        return getBitmap(cursor, i, null);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static Bitmap getBitmap(Cursor cursor, int i, Bitmap bitmap) {
        byte[] blob = cursor.getBlob(i);
        if (blob == null) {
            return null;
        }
        BitmapFactory.Options options = sOptions.get();
        options.inBitmap = bitmap;
        options.inSampleSize = 1;
        options.inScaled = false;
        try {
            return BitmapFactory.decodeByteArray(blob, 0, blob.length, options);
        } catch (IllegalArgumentException e) {
            return BitmapFactory.decodeByteArray(blob, 0, blob.length);
        }
    }

    private BreadCrumbView getBreadCrumbs(int i) {
        return this.mGrid.getBreadCrumbs(i);
    }

    private BrowserBookmarksAdapter getChildAdapter(int i) {
        return this.mGrid.getChildAdapter(i);
    }

    static String getUrl(Cursor cursor) {
        return cursor.getString(1);
    }

    private String getUrl(BrowserBookmarksAdapter browserBookmarksAdapter, int i) {
        return getUrl(browserBookmarksAdapter.getItem(i));
    }

    private void loadFolder(int i, Uri uri) {
        BookmarksLoader bookmarksLoader = (BookmarksLoader) getLoaderManager().getLoader(i + 100);
        bookmarksLoader.setUri(uri);
        bookmarksLoader.forceLoad();
    }

    private void loadUrl(BrowserBookmarksAdapter browserBookmarksAdapter, int i) {
        if (this.mCallbacks == null || browserBookmarksAdapter == null) {
            return;
        }
        String url = getUrl(browserBookmarksAdapter.getItem(i));
        if (!url.startsWith("rtsp://")) {
            this.mCallbacks.onBookmarkSelected(browserBookmarksAdapter.getItem(i), false);
            return;
        }
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.setData(Uri.parse(url.replaceAll(" ", "%20")));
        intent.addFlags(268435456);
        getActivity().startActivity(intent);
    }

    private void openInNewWindow(BrowserBookmarksAdapter browserBookmarksAdapter, int i) {
        if (this.mCallbacks != null) {
            Cursor item = browserBookmarksAdapter.getItem(i);
            if (item.getInt(6) == 1) {
                new OpenAllInTabsTask(this, item.getLong(0)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
                return;
            }
            String url = getUrl(item);
            if (url == null) {
                Toast.makeText(getActivity(), 2131493014, 1).show();
            } else if (!url.startsWith("rtsp://")) {
                this.mCallbacks.onOpenInNewWindow(getUrl(item));
            } else {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.setData(Uri.parse(url.replaceAll(" ", "%20")));
                intent.addFlags(268435456);
                getActivity().startActivity(intent);
            }
        }
    }

    private void populateBookmarkItem(Cursor cursor, BookmarkItem bookmarkItem, boolean z) {
        bookmarkItem.setName(cursor.getString(2));
        if (!z) {
            bookmarkItem.setUrl(cursor.getString(1));
            bookmarkItem.setFavicon(getBitmap(cursor, 3));
            return;
        }
        bookmarkItem.setUrl(null);
        bookmarkItem.setFavicon(BitmapFactory.decodeResource(getResources(), 2130837550));
        new LookupBookmarkCount(getActivity(), bookmarkItem).execute(Long.valueOf(cursor.getLong(0)));
    }

    boolean canEdit(Cursor cursor) {
        int i = cursor.getInt(9);
        boolean z = true;
        if (i != 1) {
            z = i == 2;
        }
        return z;
    }

    public boolean handleContextItem(int i, int i2, int i3) {
        Activity activity = getActivity();
        BrowserBookmarksAdapter childAdapter = getChildAdapter(i2);
        if (getUrl(childAdapter.getItem(i3)) == null && (i == 2131558433 || i == 2131558567 || i == 2131558566 || i == 2131558565)) {
            Toast.makeText(getActivity(), 2131493014, 1).show();
            return true;
        }
        switch (i) {
            case 2131558433:
                loadUrl(childAdapter, i3);
                return true;
            case 2131558434:
                openInNewWindow(childAdapter, i3);
                return true;
            case 2131558564:
                editBookmark(childAdapter, i3);
                return true;
            case 2131558565:
                activity.sendBroadcast(createShortcutIntent(getActivity(), childAdapter.getItem(i3)));
                return true;
            case 2131558566:
                Cursor item = childAdapter.getItem(i3);
                Controller.sharePage(activity, item.getString(2), item.getString(1), getBitmap(item, 3), getBitmap(item, 4));
                return true;
            case 2131558567:
                copy(getUrl(childAdapter, i3));
                return true;
            case 2131558568:
                displayRemoveBookmarkDialog(childAdapter, i3);
                return true;
            case 2131558569:
                BrowserSettings.getInstance().setHomePage(getUrl(childAdapter, i3));
                BrowserSettings.getInstance().setHomePagePicker("other");
                Toast.makeText(activity, 2131493011, 1).show();
                return true;
            case 2131558643:
                Cursor item2 = childAdapter.getItem(i3);
                String string = item2.getString(2);
                Bookmarks.removeFromBookmarks(activity, activity.getContentResolver(), item2.getString(1), string);
                return true;
            default:
                return false;
        }
    }

    @Override // android.widget.ExpandableListView.OnChildClickListener
    public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i2, long j) {
        Cursor item = getChildAdapter(i).getItem(i2);
        boolean z = item.getInt(6) != 0;
        String url = getUrl(item);
        if (url != null && url.startsWith("rtsp://") && this.mCallbacks != null) {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            intent.setData(Uri.parse(url.replaceAll(" ", "%20")));
            intent.addFlags(268435456);
            getActivity().startActivity(intent);
            return true;
        } else if ((this.mCallbacks == null || !this.mCallbacks.onBookmarkSelected(item, z)) && z) {
            String string = item.getString(2);
            Uri withAppendedId = ContentUris.withAppendedId(BrowserContract.Bookmarks.CONTENT_URI_DEFAULT_FOLDER, j);
            BreadCrumbView breadCrumbs = getBreadCrumbs(i);
            if (breadCrumbs != null) {
                breadCrumbs.pushView(string, withAppendedId);
                breadCrumbs.setVisibility(0);
                Object topData = breadCrumbs.getTopData();
                this.mCurrentFolderId = topData != null ? ContentUris.parseId((Uri) topData) : -1L;
            }
            loadFolder(i, withAppendedId);
            return true;
        } else {
            return true;
        }
    }

    @Override // android.app.Fragment, android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        Resources resources = getActivity().getResources();
        this.mGrid.setColumnWidthFromLayout(2130968585);
        this.mRoot.setPadding(0, (int) resources.getDimension(2131427366), 0, 0);
        getActivity().invalidateOptionsMenu();
    }

    @Override // android.app.Fragment
    public boolean onContextItemSelected(MenuItem menuItem) {
        BookmarkExpandableView.BookmarkContextMenuInfo bookmarkContextMenuInfo;
        if ((menuItem.getMenuInfo() instanceof BookmarkExpandableView.BookmarkContextMenuInfo) && (bookmarkContextMenuInfo = (BookmarkExpandableView.BookmarkContextMenuInfo) menuItem.getMenuInfo()) != null) {
            if (handleContextItem(menuItem.getItemId(), bookmarkContextMenuInfo.groupPosition, bookmarkContextMenuInfo.childPosition)) {
                return true;
            }
            return super.onContextItemSelected(menuItem);
        }
        return false;
    }

    @Override // android.app.Fragment
    public void onCreate(Bundle bundle) {
        boolean z = false;
        super.onCreate(bundle);
        SharedPreferences preferences = BrowserSettings.getInstance().getPreferences();
        try {
            this.mState = new JSONObject(preferences.getString("bbp_group_state", "{}"));
        } catch (JSONException e) {
            preferences.edit().remove("bbp_group_state").apply();
            this.mState = new JSONObject();
        }
        Bundle arguments = getArguments();
        if (arguments != null) {
            z = arguments.getBoolean("disable_new_window", false);
        }
        this.mDisableNewWindow = z;
        setHasOptionsMenu(true);
        if (this.mCallbacks == null && (getActivity() instanceof CombinedBookmarksCallbacks)) {
            this.mCallbacks = new CombinedBookmarksCallbackWrapper((CombinedBookmarksCallbacks) getActivity(), null);
        }
    }

    @Override // android.app.Fragment, android.view.View.OnCreateContextMenuListener
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        BookmarkExpandableView.BookmarkContextMenuInfo bookmarkContextMenuInfo = (BookmarkExpandableView.BookmarkContextMenuInfo) contextMenuInfo;
        Cursor item = getChildAdapter(bookmarkContextMenuInfo.groupPosition).getItem(bookmarkContextMenuInfo.childPosition);
        if (canEdit(item)) {
            boolean z = item.getInt(6) != 0;
            Activity activity = getActivity();
            activity.getMenuInflater().inflate(2131755009, contextMenu);
            if (z) {
                contextMenu.setGroupVisible(2131558570, true);
            } else {
                contextMenu.setGroupVisible(2131558563, true);
                if (this.mDisableNewWindow) {
                    contextMenu.findItem(2131558434).setVisible(false);
                }
            }
            BookmarkItem bookmarkItem = new BookmarkItem(activity);
            bookmarkItem.setEnableScrolling(true);
            populateBookmarkItem(item, bookmarkItem, z);
            contextMenu.setHeaderView(bookmarkItem);
            int size = contextMenu.size();
            for (int i = 0; i < size; i++) {
                contextMenu.getItem(i).setOnMenuItemClickListener(this.mContextItemClickListener);
            }
        }
    }

    @Override // android.app.LoaderManager.LoaderCallbacks
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        if (i == 1) {
            return new AccountsLoader(getActivity());
        }
        if (i >= 100) {
            return new BookmarksLoader(getActivity(), bundle.getString("account_type"), bundle.getString("account_name"));
        }
        throw new UnsupportedOperationException("Unknown loader id " + i);
    }

    @Override // android.app.Fragment
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        this.mBrowserBookmarkExt = Extensions.getBookmarkPlugin(getActivity());
        this.mBrowserBookmarkExt.createBookmarksPageOptionsMenu(menu, menuInflater);
    }

    @Override // android.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        this.mRoot = layoutInflater.inflate(2130968586, viewGroup, false);
        this.mEmptyView = this.mRoot.findViewById(16908292);
        this.mGrid = (BookmarkExpandableView) this.mRoot.findViewById(2131558432);
        this.mGrid.setOnChildClickListener(this);
        this.mGrid.setColumnWidthFromLayout(2130968585);
        this.mGrid.setBreadcrumbController(this);
        setEnableContextMenu(this.mEnableContextMenu);
        getLoaderManager().restartLoader(1, null, this);
        return this.mRoot;
    }

    @Override // android.app.Fragment
    public void onDestroyView() {
        super.onDestroyView();
        this.mGrid.setBreadcrumbController(null);
        this.mGrid.clearAccounts();
        LoaderManager loaderManager = getLoaderManager();
        loaderManager.destroyLoader(1);
        for (Integer num : this.mBookmarkAdapters.keySet()) {
            int intValue = num.intValue();
            this.mBookmarkAdapters.get(Integer.valueOf(intValue)).releaseCursor(loaderManager, intValue);
        }
        this.mBookmarkAdapters.clear();
    }

    @Override // android.app.LoaderManager.LoaderCallbacks
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        boolean z = false;
        if (cursor.getCount() == 0) {
            z = true;
        }
        if (loader.getId() == 1) {
            LoaderManager loaderManager = getLoaderManager();
            int i = 100;
            while (cursor.moveToNext()) {
                String string = cursor.getString(0);
                String string2 = cursor.getString(1);
                Bundle bundle = new Bundle();
                bundle.putString("account_name", string);
                bundle.putString("account_type", string2);
                BrowserBookmarksAdapter browserBookmarksAdapter = new BrowserBookmarksAdapter(getActivity());
                this.mBookmarkAdapters.put(Integer.valueOf(i), browserBookmarksAdapter);
                boolean z2 = true;
                try {
                    z2 = this.mState.getBoolean(string != null ? string : "local");
                } catch (JSONException e) {
                }
                this.mGrid.addAccount(string, browserBookmarksAdapter, z2);
                loaderManager.restartLoader(i, bundle, this);
                i++;
            }
            getLoaderManager().destroyLoader(1);
        } else if (loader.getId() >= 100) {
            BrowserBookmarksAdapter browserBookmarksAdapter2 = this.mBookmarkAdapters.get(Integer.valueOf(loader.getId()));
            browserBookmarksAdapter2.changeCursor(cursor);
            if (browserBookmarksAdapter2.getCount() != 0) {
                this.mCurrentFolderId = browserBookmarksAdapter2.getItem(0).getLong(8);
            }
        }
        this.mEmptyView.setVisibility(z ? 0 : 8);
    }

    @Override // android.app.LoaderManager.LoaderCallbacks
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override // android.app.Fragment
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        this.mBrowserBookmarkExt = Extensions.getBookmarkPlugin(getActivity());
        if (this.mBrowserBookmarkExt.bookmarksPageOptionsMenuItemSelected(menuItem, getActivity(), this.mCurrentFolderId)) {
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override // android.app.Fragment
    public void onPause() {
        super.onPause();
        try {
            this.mState = this.mGrid.saveGroupState();
            BrowserSettings.getInstance().getPreferences().edit().putString("bbp_group_state", this.mState.toString()).apply();
        } catch (JSONException e) {
        }
    }

    @Override // com.android.browser.BreadCrumbView.Controller
    public void onTop(BreadCrumbView breadCrumbView, int i, Object obj) {
        int intValue = ((Integer) breadCrumbView.getTag(2131558400)).intValue();
        Uri uri = (Uri) obj;
        Uri uri2 = uri;
        if (uri == null) {
            uri2 = BrowserContract.Bookmarks.CONTENT_URI_DEFAULT_FOLDER;
        }
        loadFolder(intValue, uri2);
        if (i <= 1) {
            breadCrumbView.setVisibility(8);
        } else {
            breadCrumbView.setVisibility(0);
        }
    }

    public void setCallbackListener(BookmarksPageCallbacks bookmarksPageCallbacks) {
        this.mCallbacks = bookmarksPageCallbacks;
    }

    public void setEnableContextMenu(boolean z) {
        this.mEnableContextMenu = z;
        if (this.mGrid != null) {
            if (this.mEnableContextMenu) {
                registerForContextMenu(this.mGrid);
                return;
            }
            unregisterForContextMenu(this.mGrid);
            this.mGrid.setLongClickable(false);
        }
    }
}
