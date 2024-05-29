package com.android.browser.widget;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.android.browser.AddBookmarkPage;
import com.android.browser.provider.BrowserContract;
/* loaded from: b.zip:com/android/browser/widget/BookmarkWidgetConfigure.class */
public class BookmarkWidgetConfigure extends ListActivity implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {
    private ArrayAdapter<AddBookmarkPage.BookmarkAccount> mAccountAdapter;
    private int mAppWidgetId = 0;

    /* loaded from: b.zip:com/android/browser/widget/BookmarkWidgetConfigure$AccountsLoader.class */
    static class AccountsLoader extends CursorLoader {
        static final String[] PROJECTION = {"account_name", "account_type", "root_id"};

        public AccountsLoader(Context context) {
            super(context, BrowserContract.Accounts.CONTENT_URI.buildUpon().appendQueryParameter("allowEmptyAccounts", "false").build(), PROJECTION, null, null, null);
        }
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        finish();
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setResult(0);
        setVisible(false);
        setContentView(2130968637);
        findViewById(2131558462).setOnClickListener(this);
        this.mAccountAdapter = new ArrayAdapter<>(this, 17367043);
        setListAdapter(this.mAccountAdapter);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            this.mAppWidgetId = extras.getInt("appWidgetId", 0);
        }
        if (this.mAppWidgetId == 0) {
            finish();
        } else {
            getLoaderManager().initLoader(1, null, this);
        }
    }

    @Override // android.app.LoaderManager.LoaderCallbacks
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new AccountsLoader(this);
    }

    @Override // android.app.ListActivity
    protected void onListItemClick(ListView listView, View view, int i, long j) {
        pickAccount(this.mAccountAdapter.getItem(i).rootFolderId);
    }

    @Override // android.app.LoaderManager.LoaderCallbacks
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            pickAccount(1L);
        } else if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            pickAccount(cursor.getLong(2));
        } else {
            this.mAccountAdapter.clear();
            while (cursor.moveToNext()) {
                this.mAccountAdapter.add(new AddBookmarkPage.BookmarkAccount(this, cursor));
            }
            setVisible(true);
        }
        getLoaderManager().destroyLoader(1);
    }

    @Override // android.app.LoaderManager.LoaderCallbacks
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    void pickAccount(long j) {
        BookmarkThumbnailWidgetService.setupWidgetState(this, this.mAppWidgetId, j);
        Intent intent = new Intent();
        intent.putExtra("appWidgetId", this.mAppWidgetId);
        setResult(-1, intent);
        finish();
    }
}
