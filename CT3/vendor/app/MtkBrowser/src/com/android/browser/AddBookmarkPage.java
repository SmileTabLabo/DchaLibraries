package com.android.browser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ParseException;
import android.net.Uri;
import android.net.WebAddress;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.android.browser.BreadCrumbView;
import com.android.browser.addbookmark.FolderSpinner;
import com.android.browser.addbookmark.FolderSpinnerAdapter;
import com.android.browser.provider.BrowserContract;
import com.mediatek.browser.ext.IBrowserBookmarkExt;
import com.mediatek.browser.ext.IBrowserFeatureIndexExt;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
/* loaded from: b.zip:com/android/browser/AddBookmarkPage.class */
public class AddBookmarkPage extends Activity implements View.OnClickListener, TextView.OnEditorActionListener, AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor>, BreadCrumbView.Controller, FolderSpinner.OnSetSelectionListener, AdapterView.OnItemSelectedListener {
    private ArrayAdapter<BookmarkAccount> mAccountAdapter;
    private Spinner mAccountSpinner;
    private FolderAdapter mAdapter;
    private View mAddNewFolder;
    private View mAddSeparator;
    private EditText mAddress;
    private IBrowserBookmarkExt mBookmarkExt;
    private TextView mButton;
    private View mCancelButton;
    private View mCrumbHolder;
    private BreadCrumbView mCrumbs;
    private long mCurrentId;
    private View mDefaultView;
    private boolean mEditingExisting;
    private boolean mEditingFolder;
    private TextView mFakeTitle;
    private View mFakeTitleHolder;
    private FolderSpinner mFolder;
    private FolderSpinnerAdapter mFolderAdapter;
    private View mFolderCancel;
    private EditText mFolderNamer;
    private View mFolderNamerHolder;
    private View mFolderSelector;
    private Handler mHandler;
    private Drawable mHeaderIcon;
    private boolean mIsFolderNamerShowing;
    private CustomListView mListView;
    private Bundle mMap;
    private String mOriginalUrl;
    private View mRemoveLink;
    private long mRootFolder;
    private EditText mTitle;
    private TextView mTopLevelLabel;
    private String mTouchIconUrl;
    private AlertDialog mWarningDialog;
    private final String LOGTAG = "Bookmarks";
    private final int LOADER_ID_ACCOUNTS = 0;
    private final int LOADER_ID_FOLDER_CONTENTS = 1;
    private final int LOADER_ID_EDIT_INFO = 2;
    private long mCurrentFolder = -1;
    private boolean mSaveToHomeScreen = false;
    private long mOverwriteBookmarkId = -1;
    private int mRestoreFolder = -2;
    private DialogInterface.OnClickListener mAlertDlgOk = new DialogInterface.OnClickListener(this) { // from class: com.android.browser.AddBookmarkPage.1
        final AddBookmarkPage this$0;

        {
            this.this$0 = this;
        }

        @Override // android.content.DialogInterface.OnClickListener
        public void onClick(DialogInterface dialogInterface, int i) {
            if (this.this$0.save()) {
                this.this$0.finish();
            }
        }
    };
    private LoaderManager.LoaderCallbacks<EditBookmarkInfo> mEditInfoLoaderCallbacks = new LoaderManager.LoaderCallbacks<EditBookmarkInfo>(this) { // from class: com.android.browser.AddBookmarkPage.2
        final AddBookmarkPage this$0;

        {
            this.this$0 = this;
        }

        @Override // android.app.LoaderManager.LoaderCallbacks
        public Loader<EditBookmarkInfo> onCreateLoader(int i, Bundle bundle) {
            return new EditBookmarkInfoLoader(this.this$0, this.this$0.mMap);
        }

        @Override // android.app.LoaderManager.LoaderCallbacks
        public void onLoadFinished(Loader<EditBookmarkInfo> loader, EditBookmarkInfo editBookmarkInfo) {
            boolean z = false;
            if (editBookmarkInfo.id != -1) {
                this.this$0.mEditingExisting = true;
                this.this$0.showRemoveButton();
                if (this.this$0.mEditingFolder) {
                    this.this$0.mFakeTitle.setText(2131492991);
                } else {
                    this.this$0.mFakeTitle.setText(2131493002);
                }
                this.this$0.mFolderAdapter.setOtherFolderDisplayText(editBookmarkInfo.parentTitle);
                this.this$0.mMap.putLong("_id", editBookmarkInfo.id);
                this.this$0.setAccount(editBookmarkInfo.accountName, editBookmarkInfo.accountType);
                this.this$0.mCurrentFolder = editBookmarkInfo.parentId;
                this.this$0.onCurrentFolderFound();
                z = true;
                if (this.this$0.mRestoreFolder >= 0) {
                    this.this$0.mFolder.setSelectionIgnoringSelectionChange(this.this$0.mRestoreFolder);
                    this.this$0.mRestoreFolder = -2;
                    z = true;
                }
            }
            boolean z2 = z;
            if (editBookmarkInfo.lastUsedId != -1) {
                z2 = z;
                if (editBookmarkInfo.lastUsedId != editBookmarkInfo.id) {
                    if (this.this$0.mEditingFolder) {
                        z2 = z;
                    } else if (z && editBookmarkInfo.lastUsedId != this.this$0.mRootFolder && TextUtils.equals(editBookmarkInfo.lastUsedAccountName, editBookmarkInfo.accountName) && TextUtils.equals(editBookmarkInfo.lastUsedAccountType, editBookmarkInfo.accountType)) {
                        this.this$0.mFolderAdapter.addRecentFolder(editBookmarkInfo.lastUsedId, editBookmarkInfo.lastUsedTitle);
                        z2 = z;
                    } else {
                        z2 = z;
                        if (!z) {
                            this.this$0.setAccount(editBookmarkInfo.lastUsedAccountName, editBookmarkInfo.lastUsedAccountType);
                            if (editBookmarkInfo.lastUsedId != this.this$0.mRootFolder) {
                                this.this$0.mFolderAdapter.addRecentFolder(editBookmarkInfo.lastUsedId, editBookmarkInfo.lastUsedTitle);
                            }
                            z2 = true;
                            if (this.this$0.mRestoreFolder >= 0) {
                                this.this$0.mFolder.setSelectionIgnoringSelectionChange(this.this$0.mRestoreFolder);
                                this.this$0.mRestoreFolder = -2;
                                z2 = true;
                            }
                        }
                    }
                }
            }
            if (z2) {
                return;
            }
            this.this$0.mAccountSpinner.setSelection(0);
        }

        @Override // android.app.LoaderManager.LoaderCallbacks
        public void onLoaderReset(Loader<EditBookmarkInfo> loader) {
        }
    };

    /* loaded from: b.zip:com/android/browser/AddBookmarkPage$AccountsLoader.class */
    static class AccountsLoader extends CursorLoader {
        static final String[] PROJECTION = {"account_name", "account_type", "root_id"};

        public AccountsLoader(Context context) {
            super(context, BrowserContract.Accounts.CONTENT_URI, PROJECTION, null, null, null);
        }
    }

    /* loaded from: b.zip:com/android/browser/AddBookmarkPage$BookmarkAccount.class */
    public static class BookmarkAccount {
        String accountName;
        String accountType;
        private String mLabel;
        public long rootFolderId;

        public BookmarkAccount(Context context, Cursor cursor) {
            this.accountName = cursor.getString(0);
            this.accountType = cursor.getString(1);
            this.rootFolderId = cursor.getLong(2);
            this.mLabel = this.accountName;
            if (TextUtils.isEmpty(this.mLabel)) {
                this.mLabel = context.getString(2131493284);
            }
        }

        public String toString() {
            return this.mLabel;
        }
    }

    /* loaded from: b.zip:com/android/browser/AddBookmarkPage$CustomListView.class */
    public static class CustomListView extends ListView {
        private EditText mEditText;

        public CustomListView(Context context) {
            super(context);
        }

        public CustomListView(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
        }

        public CustomListView(Context context, AttributeSet attributeSet, int i) {
            super(context, attributeSet, i);
        }

        public void addEditText(EditText editText) {
            this.mEditText = editText;
        }

        @Override // android.widget.AbsListView, android.view.View
        public boolean checkInputConnectionProxy(View view) {
            return view == this.mEditText;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: b.zip:com/android/browser/AddBookmarkPage$EditBookmarkInfo.class */
    public static class EditBookmarkInfo {
        String accountName;
        String accountType;
        String lastUsedAccountName;
        String lastUsedAccountType;
        String lastUsedTitle;
        String parentTitle;
        String title;
        long id = -1;
        long parentId = -1;
        long lastUsedId = -1;

        EditBookmarkInfo() {
        }
    }

    /* loaded from: b.zip:com/android/browser/AddBookmarkPage$EditBookmarkInfoLoader.class */
    static class EditBookmarkInfoLoader extends AsyncTaskLoader<EditBookmarkInfo> {
        private Context mContext;
        private Bundle mMap;

        public EditBookmarkInfoLoader(Context context, Bundle bundle) {
            super(context);
            this.mContext = context.getApplicationContext();
            this.mMap = bundle;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.content.AsyncTaskLoader
        public EditBookmarkInfo loadInBackground() {
            ContentResolver contentResolver = this.mContext.getContentResolver();
            EditBookmarkInfo editBookmarkInfo = new EditBookmarkInfo();
            Cursor cursor = null;
            try {
                String string = this.mMap.getString("url");
                editBookmarkInfo.id = this.mMap.getLong("_id", -1L);
                Cursor cursor2 = null;
                if (this.mMap.getBoolean("check_for_dupe")) {
                    cursor2 = null;
                    if (editBookmarkInfo.id == -1) {
                        if (TextUtils.isEmpty(string)) {
                            cursor2 = null;
                        } else {
                            cursor2 = contentResolver.query(BrowserContract.Bookmarks.CONTENT_URI, new String[]{"_id"}, "url=?", new String[]{string}, null);
                            if (cursor2.getCount() == 1 && cursor2.moveToFirst()) {
                                editBookmarkInfo.id = cursor2.getLong(0);
                            }
                            cursor2.close();
                        }
                    }
                }
                Cursor cursor3 = cursor2;
                if (editBookmarkInfo.id != -1) {
                    Cursor cursor4 = cursor2;
                    Cursor query = contentResolver.query(ContentUris.withAppendedId(BrowserContract.Bookmarks.CONTENT_URI, editBookmarkInfo.id), new String[]{"parent", "account_name", "account_type", "title"}, null, null, null);
                    if (query.moveToFirst()) {
                        editBookmarkInfo.parentId = query.getLong(0);
                        editBookmarkInfo.accountName = query.getString(1);
                        editBookmarkInfo.accountType = query.getString(2);
                        editBookmarkInfo.title = query.getString(3);
                    }
                    query.close();
                    cursor3 = contentResolver.query(ContentUris.withAppendedId(BrowserContract.Bookmarks.CONTENT_URI, editBookmarkInfo.parentId), new String[]{"title"}, null, null, null);
                    if (cursor3.moveToFirst()) {
                        editBookmarkInfo.parentTitle = cursor3.getString(0);
                    }
                    cursor3.close();
                }
                Cursor cursor5 = cursor3;
                Cursor query2 = contentResolver.query(BrowserContract.Bookmarks.CONTENT_URI, new String[]{"parent"}, null, null, "modified DESC LIMIT 1");
                Cursor cursor6 = query2;
                if (query2.moveToFirst()) {
                    long j = query2.getLong(0);
                    query2.close();
                    cursor6 = contentResolver.query(BrowserContract.Bookmarks.CONTENT_URI, new String[]{"title", "account_name", "account_type"}, "_id=?", new String[]{Long.toString(j)}, null);
                    if (cursor6.moveToFirst()) {
                        editBookmarkInfo.lastUsedId = j;
                        editBookmarkInfo.lastUsedTitle = cursor6.getString(0);
                        editBookmarkInfo.lastUsedAccountName = cursor6.getString(1);
                        editBookmarkInfo.lastUsedAccountType = cursor6.getString(2);
                    }
                    cursor = cursor6;
                    cursor6.close();
                }
                return editBookmarkInfo;
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        @Override // android.content.Loader
        protected void onStartLoading() {
            forceLoad();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: b.zip:com/android/browser/AddBookmarkPage$Folder.class */
    public static class Folder {
        long Id;
        String Name;

        Folder(String str, long j) {
            this.Name = str;
            this.Id = j;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: b.zip:com/android/browser/AddBookmarkPage$FolderAdapter.class */
    public class FolderAdapter extends CursorAdapter {
        final AddBookmarkPage this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public FolderAdapter(AddBookmarkPage addBookmarkPage, Context context) {
            super(context, null);
            this.this$0 = addBookmarkPage;
        }

        @Override // android.widget.CursorAdapter
        public void bindView(View view, Context context, Cursor cursor) {
            ((TextView) view.findViewById(16908308)).setText(cursor.getString(cursor.getColumnIndexOrThrow("title")));
        }

        @Override // android.widget.BaseAdapter, android.widget.Adapter
        public boolean isEmpty() {
            boolean z = false;
            if (super.isEmpty()) {
                z = !this.this$0.mIsFolderNamerShowing;
            }
            return z;
        }

        @Override // android.widget.CursorAdapter
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            return LayoutInflater.from(context).inflate(2130968599, (ViewGroup) null);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: b.zip:com/android/browser/AddBookmarkPage$SaveBookmarkRunnable.class */
    public class SaveBookmarkRunnable implements Runnable {
        private Context mContext;
        private Message mMessage;
        final AddBookmarkPage this$0;

        public SaveBookmarkRunnable(AddBookmarkPage addBookmarkPage, Context context, Message message) {
            this.this$0 = addBookmarkPage;
            this.mContext = context.getApplicationContext();
            this.mMessage = message;
        }

        @Override // java.lang.Runnable
        public void run() {
            Bundle data = this.mMessage.getData();
            String string = data.getString("title");
            String string2 = data.getString("url");
            Bitmap bitmap = data.getBoolean("remove_thumbnail") ? null : (Bitmap) data.getParcelable("thumbnail");
            String string3 = data.getString("touch_icon_url");
            try {
                ContentResolver contentResolver = this.this$0.getContentResolver();
                Log.i("Bookmarks", "mCurrentFolder: " + this.this$0.mCurrentFolder);
                Bookmarks.addBookmark(this.this$0, false, string2, string, bitmap, this.this$0.mCurrentFolder);
                if (string3 != null) {
                    new DownloadTouchIcon(this.mContext, contentResolver, string2).execute(this.this$0.mTouchIconUrl);
                }
                this.mMessage.arg1 = 1;
            } catch (IllegalStateException e) {
                this.mMessage.arg1 = 0;
            }
            this.mMessage.sendToTarget();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: b.zip:com/android/browser/AddBookmarkPage$UpdateBookmarkTask.class */
    public static class UpdateBookmarkTask extends AsyncTask<ContentValues, Void, Void> {
        long mBookmarkCurrentId;
        Context mContext;
        Long mId;

        public UpdateBookmarkTask(Context context, long j) {
            this.mContext = context.getApplicationContext();
            this.mId = Long.valueOf(j);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public Void doInBackground(ContentValues... contentValuesArr) {
            if (contentValuesArr.length < 1) {
                throw new IllegalArgumentException("No ContentValues provided!");
            }
            this.mContext.getContentResolver().update(ContentUris.withAppendedId(BookmarkUtils.getBookmarksUri(this.mContext), this.mId.longValue()), contentValuesArr[0], null, null);
            Log.d("browser/AddBookmarkPage", "UpdateBookmarkTask doInBackground:");
            if (contentValuesArr.length > 1) {
                this.mBookmarkCurrentId = contentValuesArr[1].getAsLong("bookmark_current_id").longValue();
                return null;
            }
            this.mBookmarkCurrentId = -1L;
            return null;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(Void r10) {
            Log.d("browser/AddBookmarkPage", "UpdateBookmarkTask onPostExecute mBookmarkCurrentId:" + this.mBookmarkCurrentId);
            if (this.mBookmarkCurrentId > 0) {
                this.mContext.getContentResolver().delete(BrowserContract.Bookmarks.CONTENT_URI, "_id = ?", new String[]{String.valueOf(this.mBookmarkCurrentId)});
            }
        }
    }

    private long addFolderToCurrent(String str) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("title", str);
        contentValues.put("folder", (Integer) 1);
        Folder folder = null;
        if (this.mCrumbs != null) {
            folder = this.mCrumbs.getTopData();
        }
        contentValues.put("parent", Long.valueOf(folder != null ? folder.Id : this.mRootFolder));
        Uri insert = getContentResolver().insert(BrowserContract.Bookmarks.CONTENT_URI, contentValues);
        if (insert != null) {
            return ContentUris.parseId(insert);
        }
        return -1L;
    }

    public static long addFolderToRoot(Context context, String str) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("title", str);
        contentValues.put("folder", (Integer) 1);
        contentValues.put("parent", (Long) 1L);
        Uri insert = context.getContentResolver().insert(BrowserContract.Bookmarks.CONTENT_URI, contentValues);
        return insert != null ? ContentUris.parseId(insert) : getIdFromName(context, str);
    }

    private void completeOrCancelFolderNaming(boolean z) {
        getInputMethodManager().hideSoftInputFromWindow(this.mListView.getWindowToken(), 0);
        if (!z && !TextUtils.isEmpty(this.mFolderNamer.getText())) {
            descendInto(this.mFolderNamer.getText().toString(), addFolderToCurrent(this.mFolderNamer.getText().toString()));
        }
        setShowFolderNamer(false);
        this.mBookmarkExt.showCustomizedEditFolderNewFolderView(this.mAddNewFolder, this.mAddSeparator, this.mMap);
    }

    private void createHandler() {
        if (this.mHandler == null) {
            this.mHandler = new Handler(this) { // from class: com.android.browser.AddBookmarkPage.5
                final AddBookmarkPage this$0;

                {
                    this.this$0 = this;
                }

                @Override // android.os.Handler
                public void handleMessage(Message message) {
                    switch (message.what) {
                        case IBrowserFeatureIndexExt.CUSTOM_PREFERENCE_LIST /* 100 */:
                            if (1 == message.arg1) {
                                Toast.makeText(this.this$0, 2131493009, 1).show();
                                return;
                            } else {
                                Toast.makeText(this.this$0, 2131493010, 1).show();
                                return;
                            }
                        case 101:
                            Bundle data = message.getData();
                            this.this$0.sendBroadcast(BookmarkUtils.createAddToHomeIntent(this.this$0, data.getString("url"), data.getString("title"), (Bitmap) data.getParcelable("touch_icon"), (Bitmap) data.getParcelable("favicon")));
                            return;
                        case 102:
                            this.this$0.finish();
                            return;
                        default:
                            return;
                    }
                }
            };
        }
    }

    private void descendInto(String str, long j) {
        if (j == -1) {
            Toast.makeText(getApplicationContext(), 2131492888, 1).show();
            return;
        }
        this.mCrumbs.pushView(str, new Folder(str, j));
        this.mCrumbs.notifyController();
    }

    private void displayAlertDialogForExistingBookmark() {
        new AlertDialog.Builder(this).setTitle(2131492886).setIcon(17301543).setMessage(getText(2131492887).toString()).setPositiveButton(2131492963, this.mAlertDlgOk).setNegativeButton(2131492962, (DialogInterface.OnClickListener) null).show();
    }

    private void displayToastForExistingFolder() {
        Toast.makeText(getApplicationContext(), 2131492888, 1).show();
    }

    private InputFilter[] generateInputFilter(int i) {
        return new InputFilter[]{new InputFilter.LengthFilter(this, i, i) { // from class: com.android.browser.AddBookmarkPage.3
            final AddBookmarkPage this$0;
            final int val$nLimit;

            {
                this.this$0 = this;
                this.val$nLimit = i;
            }

            @Override // android.text.InputFilter.LengthFilter, android.text.InputFilter
            public CharSequence filter(CharSequence charSequence, int i2, int i3, Spanned spanned, int i4, int i5) {
                int length = this.val$nLimit - (spanned.length() - (i5 - i4));
                if (length <= 0) {
                    this.this$0.showWarningDialog();
                    return "";
                } else if (length >= i3 - i2) {
                    return null;
                } else {
                    if (length < charSequence.length()) {
                        this.this$0.showWarningDialog();
                    }
                    return charSequence.subSequence(i2, i2 + length);
                }
            }
        }};
    }

    private static long getIdFromName(Context context, String str) {
        long j = -1;
        Cursor cursor = null;
        try {
            Cursor query = context.getContentResolver().query(BrowserContract.Bookmarks.CONTENT_URI, new String[]{"_id"}, "title = ? AND deleted = ? AND folder = ? AND parent = ?", new String[]{str, "0", "1", "1"}, null);
            long j2 = -1;
            if (query != null) {
                j2 = -1;
                if (query.getCount() != 0) {
                    while (true) {
                        j2 = j;
                        cursor = query;
                        if (!query.moveToNext()) {
                            break;
                        }
                        j = query.getLong(0);
                    }
                }
            }
            if (query != null) {
                query.close();
            }
            return j2;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    private InputMethodManager getInputMethodManager() {
        return (InputMethodManager) getSystemService("input_method");
    }

    private String getNameFromId(long j) {
        Cursor cursor = null;
        try {
            Cursor query = getApplicationContext().getContentResolver().query(BrowserContract.Bookmarks.CONTENT_URI, new String[]{"title"}, "_id = ? AND deleted = ? AND folder = ? ", new String[]{String.valueOf(j), "0", "1"}, null);
            String str = "";
            if (query != null) {
                str = "";
                if (query.moveToNext()) {
                    cursor = query;
                    str = query.getString(0);
                }
            }
            if (query != null) {
                query.close();
            }
            Log.d("browser/AddBookmarkPage", "title :" + str);
            return str;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    private String getTitleFromId(long j) {
        Cursor cursor = null;
        try {
            Cursor query = getApplicationContext().getContentResolver().query(BrowserContract.Bookmarks.CONTENT_URI, new String[]{"title"}, "_id = ? AND deleted = ? AND folder = ?", new String[]{j + "", "0", "1"}, null);
            String str = null;
            if (query != null) {
                str = null;
                if (query.getCount() != 0) {
                    String str2 = null;
                    while (true) {
                        str = str2;
                        cursor = query;
                        if (!query.moveToNext()) {
                            break;
                        }
                        str2 = query.getString(0);
                    }
                }
            }
            if (query != null) {
                query.close();
            }
            return str;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    private Uri getUriForFolder(long j) {
        BookmarkAccount bookmarkAccount = (BookmarkAccount) this.mAccountSpinner.getSelectedItem();
        return (j != this.mRootFolder || bookmarkAccount == null) ? BrowserContract.Bookmarks.buildFolderUri(j) : BookmarksLoader.addAccount(BrowserContract.Bookmarks.CONTENT_URI_DEFAULT_FOLDER, bookmarkAccount.accountType, bookmarkAccount.accountName);
    }

    private int haveToOverwriteBookmarkId(String str, String str2, long j) {
        if (this.mSaveToHomeScreen || this.mEditingFolder) {
            return -1;
        }
        Log.d("browser/AddBookmarkPage", "Add bookmark page haveToOverwriteBookmarkId mCurrentId:" + this.mCurrentId);
        return Bookmarks.getIdByNameOrUrl(getContentResolver(), str, str2, j, this.mCurrentId);
    }

    private void hideSoftInput() {
        Log.d("browser/AddBookmarkPage", "hideSoftInput");
        getInputMethodManager().hideSoftInputFromWindow(this.mListView.getWindowToken(), 0);
    }

    /* JADX WARN: Removed duplicated region for block: B:27:0x00c2  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private boolean isFolderExist(long j, String str) {
        boolean z;
        Log.e("browser/AddBookmarkPage", "BrowserProvider2.isValidAccountName parentId:" + j + " title:" + str);
        if (j <= 0 || str == null || str.length() == 0) {
            return false;
        }
        Cursor cursor = null;
        try {
            Cursor query = getApplicationContext().getContentResolver().query(BrowserContract.Bookmarks.CONTENT_URI, new String[]{"_id"}, "parent = ? AND deleted = ? AND folder = ? AND title = ?", new String[]{j + "", "0", "1", str}, null);
            if (query != null) {
                cursor = query;
                if (query.getCount() != 0) {
                    z = true;
                    if (query != null) {
                        query.close();
                    }
                    return z;
                }
            }
            z = false;
            if (query != null) {
            }
            return z;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onCurrentFolderFound() {
        int i = 0;
        LoaderManager loaderManager = getLoaderManager();
        if (!this.mSaveToHomeScreen) {
            if (this.mCurrentFolder == -1 || this.mCurrentFolder == this.mRootFolder) {
                setShowBookmarkIcon(true);
                if (this.mBookmarkExt.shouldSetCustomizedEditFolderSelection(this.mMap, !this.mEditingFolder)) {
                    FolderSpinner folderSpinner = this.mFolder;
                    if (!this.mEditingFolder) {
                        i = 1;
                    }
                    folderSpinner.setSelectionIgnoringSelectionChange(i);
                }
            } else {
                this.mFolder.setSelectionIgnoringSelectionChange(this.mEditingFolder ? 1 : 2);
            }
        }
        loaderManager.restartLoader(1, null, this);
    }

    private void onRootFolderFound(long j) {
        this.mRootFolder = j;
        if (this.mCurrentFolder == -1 || this.mEditingExisting) {
            this.mCurrentFolder = this.mRootFolder;
        }
        setupTopCrumb();
        onCurrentFolderFound();
    }

    private void setShowBookmarkIcon(boolean z) {
        this.mTopLevelLabel.setCompoundDrawablesWithIntrinsicBounds(z ? this.mHeaderIcon : null, (Drawable) null, (Drawable) null, (Drawable) null);
    }

    private void setShowFolderNamer(boolean z) {
        if (z != this.mIsFolderNamerShowing) {
            this.mIsFolderNamerShowing = z;
            if (z) {
                this.mListView.addFooterView(this.mFolderNamerHolder);
            } else {
                this.mListView.removeFooterView(this.mFolderNamerHolder);
            }
            this.mListView.setAdapter((ListAdapter) this.mAdapter);
            if (z) {
                this.mListView.setSelection(this.mListView.getCount() - 1);
            }
        }
    }

    private void setupTopCrumb() {
        this.mCrumbs.clear();
        String string = getString(2131493025);
        this.mTopLevelLabel = (TextView) this.mCrumbs.pushView(string, false, new Folder(string, this.mRootFolder));
        this.mTopLevelLabel.setCompoundDrawablePadding(6);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showRemoveButton() {
        findViewById(2131558450).setVisibility(0);
        this.mRemoveLink = findViewById(2131558451);
        this.mRemoveLink.setVisibility(0);
        this.mRemoveLink.setOnClickListener(this);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showWarningDialog() {
        if (this.mWarningDialog == null || this.mWarningDialog.isShowing()) {
            return;
        }
        this.mWarningDialog.setTitle(2131492913);
        this.mWarningDialog.setMessage(getString(2131492912));
        this.mWarningDialog.setButton(getString(2131492914), new DialogInterface.OnClickListener(this) { // from class: com.android.browser.AddBookmarkPage.4
            final AddBookmarkPage this$0;

            {
                this.this$0 = this;
            }

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        this.mWarningDialog.show();
    }

    private void switchToDefaultView(boolean z) {
        int i = 0;
        this.mFolderSelector.setVisibility(8);
        this.mDefaultView.setVisibility(0);
        this.mCrumbHolder.setVisibility(8);
        this.mFakeTitleHolder.setVisibility(0);
        if (z) {
            Object topData = this.mCrumbs.getTopData();
            if (topData != null) {
                Folder folder = (Folder) topData;
                this.mCurrentFolder = folder.Id;
                if (this.mCurrentFolder != this.mRootFolder) {
                    this.mFolderAdapter.setOtherFolderDisplayText(folder.Name);
                    return;
                }
                FolderSpinner folderSpinner = this.mFolder;
                if (!this.mEditingFolder) {
                    i = 1;
                }
                folderSpinner.setSelectionIgnoringSelectionChange(i);
            }
        } else if (this.mSaveToHomeScreen) {
            this.mFolder.setSelectionIgnoringSelectionChange(0);
        } else if (this.mCurrentFolder == this.mRootFolder) {
            this.mFolder.setSelectionIgnoringSelectionChange(this.mEditingFolder ? 0 : 1);
        } else {
            Object topData2 = this.mCrumbs.getTopData();
            if (topData2 != null && ((Folder) topData2).Id == this.mCurrentFolder) {
                this.mFolderAdapter.setOtherFolderDisplayText(((Folder) topData2).Name);
                return;
            }
            setupTopCrumb();
            getLoaderManager().restartLoader(1, null, this);
        }
    }

    private void switchToFolderSelector() {
        this.mListView.setSelection(0);
        this.mDefaultView.setVisibility(8);
        this.mFolderSelector.setVisibility(0);
        this.mCrumbHolder.setVisibility(0);
        this.mFakeTitleHolder.setVisibility(8);
        this.mBookmarkExt.showCustomizedEditFolderNewFolderView(this.mAddNewFolder, this.mAddSeparator, this.mMap);
        getInputMethodManager().hideSoftInputFromWindow(this.mListView.getWindowToken(), 0);
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (view == this.mButton) {
            if (this.mFolderSelector.getVisibility() != 0) {
                this.mOverwriteBookmarkId = -1L;
                if (save()) {
                    finish();
                }
            } else if (this.mIsFolderNamerShowing) {
                completeOrCancelFolderNaming(false);
            } else {
                this.mSaveToHomeScreen = false;
                switchToDefaultView(true);
            }
        } else if (view == this.mCancelButton) {
            if (this.mIsFolderNamerShowing) {
                completeOrCancelFolderNaming(true);
            } else if (this.mFolderSelector.getVisibility() == 0) {
                switchToDefaultView(false);
            } else {
                finish();
            }
        } else if (view == this.mFolderCancel) {
            completeOrCancelFolderNaming(true);
        } else if (view == this.mAddNewFolder) {
            setShowFolderNamer(true);
            this.mFolderNamer.setText(2131492990);
            this.mFolderNamer.setSelection(this.mFolderNamer.length());
            this.mFolderNamer.requestFocus();
            this.mAddNewFolder.setVisibility(8);
            this.mAddSeparator.setVisibility(8);
            InputMethodManager inputMethodManager = getInputMethodManager();
            inputMethodManager.focusIn(this.mListView);
            inputMethodManager.showSoftInput(this.mFolderNamer, 1);
        } else if (view == this.mRemoveLink) {
            if (!this.mEditingExisting) {
                throw new AssertionError("Remove button should not be shown for new bookmarks");
            }
            long j = this.mMap.getLong("_id");
            createHandler();
            Message obtain = Message.obtain(this.mHandler, 102);
            if (this.mEditingFolder) {
                BookmarkUtils.displayRemoveFolderDialog(j, this.mTitle.getText().toString(), this, obtain);
            } else {
                BookmarkUtils.displayRemoveBookmarkDialog(j, this.mTitle.getText().toString(), this, obtain);
            }
        }
    }

    @Override // android.app.Activity, android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(1);
        this.mMap = getIntent().getExtras();
        setContentView(2130968594);
        Window window = getWindow();
        if (BrowserActivity.isTablet(this)) {
            window.setSoftInputMode(16);
        }
        this.mBookmarkExt = Extensions.getBookmarkPlugin(getApplicationContext());
        String str = null;
        String str2 = null;
        this.mFakeTitle = (TextView) findViewById(2131558449);
        if (this.mMap != null) {
            Bundle bundle2 = this.mMap.getBundle("bookmark");
            if (bundle2 != null) {
                this.mEditingFolder = this.mMap.getBoolean("is_folder", false);
                this.mMap = bundle2;
                this.mEditingExisting = this.mBookmarkExt.customizeEditExistingFolderState(this.mMap, true);
                this.mCurrentId = this.mMap.getLong("_id", -1L);
                Log.d("browser/AddBookmarkPage", "Add bookmark page onCreate mCurrentId:" + this.mCurrentId);
                if (this.mEditingFolder) {
                    this.mFakeTitle.setText(this.mBookmarkExt.getCustomizedEditFolderFakeTitleString(this.mMap, getString(2131492991)));
                    findViewById(2131558454).setVisibility(8);
                } else {
                    this.mFakeTitle.setText(2131493002);
                    showRemoveButton();
                }
            } else {
                int i = this.mMap.getInt("gravity", -1);
                if (i != -1) {
                    WindowManager.LayoutParams attributes = window.getAttributes();
                    attributes.gravity = i;
                    window.setAttributes(attributes);
                }
            }
            str = this.mMap.getString("title");
            str2 = this.mMap.getString("url");
            this.mOriginalUrl = str2;
            this.mTouchIconUrl = this.mMap.getString("touch_icon_url");
            this.mCurrentFolder = this.mMap.getLong("parent", -1L);
            Log.i("Bookmarks", "CurrentFolderId: " + this.mCurrentFolder);
        }
        this.mWarningDialog = new AlertDialog.Builder(this).create();
        this.mTitle = (EditText) findViewById(2131558407);
        this.mTitle.setFilters(generateInputFilter(getResources().getInteger(2131623945)));
        this.mTitle.setText(str);
        if (str != null) {
            this.mTitle.setSelection(this.mTitle.getText().length());
        }
        this.mAddress = (EditText) findViewById(2131558456);
        Context applicationContext = getApplicationContext();
        InputFilter[] checkUrlLengthLimit = Extensions.getUrlPlugin(applicationContext).checkUrlLengthLimit(applicationContext);
        if (checkUrlLengthLimit != null) {
            this.mAddress.setFilters(checkUrlLengthLimit);
        }
        this.mAddress.setText(str2);
        this.mButton = (TextView) findViewById(2131558463);
        this.mButton.setOnClickListener(this);
        this.mCancelButton = findViewById(2131558462);
        this.mCancelButton.setOnClickListener(this);
        this.mFolder = (FolderSpinner) findViewById(2131558458);
        this.mFolderAdapter = new FolderSpinnerAdapter(this, !this.mEditingFolder);
        this.mFolder.setAdapter((SpinnerAdapter) this.mFolderAdapter);
        this.mFolder.setOnSetSelectionListener(this);
        if (this.mCurrentFolder == -1 || this.mCurrentFolder == 1) {
            this.mFolder.setSelectionIgnoringSelectionChange(this.mEditingFolder ? 0 : 1);
        } else {
            this.mFolder.setSelectionIgnoringSelectionChange(this.mEditingFolder ? 1 : 2);
            this.mFolderAdapter.setOtherFolderDisplayText(getNameFromId(this.mCurrentFolder));
        }
        this.mDefaultView = findViewById(2131558452);
        this.mFolderSelector = findViewById(2131558459);
        this.mFolderNamerHolder = getLayoutInflater().inflate(2130968610, (ViewGroup) null);
        this.mFolderNamer = (EditText) this.mFolderNamerHolder.findViewById(2131558498);
        this.mFolderNamer.setFilters(generateInputFilter(getResources().getInteger(2131623945)));
        this.mFolderNamer.setOnEditorActionListener(this);
        this.mFolderCancel = this.mFolderNamerHolder.findViewById(2131558499);
        this.mFolderCancel.setOnClickListener(this);
        this.mAddNewFolder = findViewById(2131558447);
        this.mAddNewFolder.setOnClickListener(this);
        this.mAddSeparator = findViewById(2131558446);
        this.mBookmarkExt.showCustomizedEditFolderNewFolderView(this.mAddNewFolder, this.mAddSeparator, this.mMap);
        this.mCrumbs = (BreadCrumbView) findViewById(2131558436);
        this.mCrumbs.setUseBackButton(true);
        this.mCrumbs.setController(this);
        this.mHeaderIcon = getResources().getDrawable(2130837550);
        this.mCrumbHolder = findViewById(2131558422);
        this.mCrumbs.setMaxVisible(2);
        this.mAdapter = new FolderAdapter(this, this);
        this.mListView = (CustomListView) findViewById(2131558460);
        this.mListView.setEmptyView(findViewById(2131558461));
        this.mListView.setAdapter((ListAdapter) this.mAdapter);
        this.mListView.setOnItemClickListener(this);
        this.mListView.addEditText(this.mFolderNamer);
        this.mAccountAdapter = new ArrayAdapter<>(this, 17367048);
        this.mAccountAdapter.setDropDownViewResource(17367049);
        this.mAccountSpinner = (Spinner) findViewById(2131558457);
        this.mAccountSpinner.setAdapter((SpinnerAdapter) this.mAccountAdapter);
        this.mAccountSpinner.setOnItemSelectedListener(this);
        this.mFakeTitleHolder = findViewById(2131558448);
        if (!window.getDecorView().isInTouchMode()) {
            this.mButton.requestFocus();
        }
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override // android.app.LoaderManager.LoaderCallbacks
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        switch (i) {
            case 0:
                return new AccountsLoader(this);
            case 1:
                String str = "folder != 0";
                String[] strArr = null;
                if (this.mEditingFolder) {
                    str = "folder != 0 AND _id != ?";
                    strArr = new String[]{Long.toString(this.mMap.getLong("_id"))};
                }
                Object topData = this.mCrumbs.getTopData();
                return new CursorLoader(this, getUriForFolder(topData != null ? ((Folder) topData).Id : this.mRootFolder), new String[]{"_id", "title", "folder"}, str, strArr, "_id ASC");
            default:
                throw new AssertionError("Asking for nonexistant loader!");
        }
    }

    @Override // android.widget.TextView.OnEditorActionListener
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        if (textView == this.mFolderNamer) {
            if (textView.getText().length() > 0) {
                if (i == 6 || i == 0) {
                    hideSoftInput();
                    return true;
                }
                return true;
            }
            return true;
        }
        return false;
    }

    @Override // android.widget.AdapterView.OnItemClickListener
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
        descendInto(((TextView) view.findViewById(16908308)).getText().toString(), j);
    }

    @Override // android.widget.AdapterView.OnItemSelectedListener
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long j) {
        if (this.mAccountSpinner == adapterView) {
            long j2 = this.mAccountAdapter.getItem(i).rootFolderId;
            if (j2 != this.mRootFolder) {
                this.mCurrentFolder = -1L;
                onRootFolderFound(j2);
                this.mFolderAdapter.clearRecentFolder();
            }
        }
    }

    @Override // android.app.LoaderManager.LoaderCallbacks
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case 0:
                this.mAccountAdapter.clear();
                while (cursor.moveToNext()) {
                    this.mAccountAdapter.add(new BookmarkAccount(this, cursor));
                }
                getLoaderManager().destroyLoader(0);
                getLoaderManager().restartLoader(2, null, this.mEditInfoLoaderCallbacks);
                return;
            case 1:
                this.mAdapter.changeCursor(cursor);
                return;
            default:
                return;
        }
    }

    @Override // android.app.LoaderManager.LoaderCallbacks
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case 1:
                this.mAdapter.changeCursor(null);
                return;
            default:
                return;
        }
    }

    @Override // android.widget.AdapterView.OnItemSelectedListener
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    @Override // android.app.Activity
    protected void onPause() {
        super.onPause();
        this.mRestoreFolder = this.mFolder.getSelectedItemPosition();
    }

    @Override // android.app.Activity
    protected void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        Resources resources = getResources();
        if (bundle != null && bundle.getBoolean("titleHasError") && this.mTitle != null && this.mTitle.getText().toString().trim().length() == 0) {
            if (this.mEditingFolder) {
                this.mTitle.setError(resources.getText(2131492925));
            } else {
                this.mTitle.setError(resources.getText(2131493012));
            }
        }
        if (bundle == null || !bundle.getBoolean("addrHasError") || this.mAddress == null || this.mAddress.getText().toString().trim().length() != 0) {
            return;
        }
        this.mAddress.setError(resources.getText(2131493013));
    }

    @Override // android.app.Activity
    protected void onResume() {
        super.onResume();
    }

    @Override // android.app.Activity
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        if (this.mTitle != null && this.mTitle.getError() != null) {
            bundle.putBoolean("titleHasError", true);
        }
        if (this.mAddress == null || this.mAddress.getError() == null) {
            return;
        }
        bundle.putBoolean("addrHasError", true);
    }

    @Override // com.android.browser.addbookmark.FolderSpinner.OnSetSelectionListener
    public void onSetSelection(long j) {
        switch ((int) j) {
            case 0:
                this.mSaveToHomeScreen = true;
                return;
            case 1:
                this.mCurrentFolder = this.mRootFolder;
                this.mSaveToHomeScreen = false;
                return;
            case 2:
                switchToFolderSelector();
                return;
            case 3:
                this.mCurrentFolder = this.mFolderAdapter.recentFolderId();
                this.mSaveToHomeScreen = false;
                getLoaderManager().restartLoader(1, null, this);
                return;
            default:
                return;
        }
    }

    @Override // com.android.browser.BreadCrumbView.Controller
    public void onTop(BreadCrumbView breadCrumbView, int i, Object obj) {
        boolean z = true;
        if (obj == null) {
            return;
        }
        long j = ((Folder) obj).Id;
        CursorLoader cursorLoader = (CursorLoader) getLoaderManager().getLoader(1);
        cursorLoader.setUri(getUriForFolder(j));
        cursorLoader.forceLoad();
        if (this.mIsFolderNamerShowing) {
            completeOrCancelFolderNaming(true);
        }
        if (i != 1) {
            z = false;
        }
        setShowBookmarkIcon(z);
    }

    boolean save() {
        Bitmap bitmap;
        Bitmap bitmap2;
        String titleFromId;
        createHandler();
        String trim = this.mTitle.getText().toString().trim();
        String fixUrl = UrlUtils.fixUrl(this.mAddress.getText().toString());
        boolean z = trim.length() == 0;
        boolean z2 = fixUrl.trim().length() == 0;
        Resources resources = getResources();
        if (z || (z2 && !this.mEditingFolder)) {
            if (z) {
                if (this.mEditingFolder) {
                    this.mTitle.setError(resources.getText(2131492925));
                } else {
                    this.mTitle.setError(resources.getText(2131493012));
                }
            }
            if (z2) {
                this.mAddress.setError(resources.getText(2131493013));
                return false;
            }
            return false;
        }
        Boolean saveCustomizedEditFolder = this.mBookmarkExt.saveCustomizedEditFolder(getApplicationContext(), trim, this.mCurrentFolder, this.mMap, getString(2131492888));
        if (saveCustomizedEditFolder != null) {
            if (saveCustomizedEditFolder.booleanValue()) {
                setResult(-1);
            }
            return saveCustomizedEditFolder.booleanValue();
        }
        String trim2 = fixUrl.trim();
        String str = trim2;
        if (!this.mEditingFolder) {
            str = trim2;
            try {
                if (!trim2.toLowerCase().startsWith("javascript:")) {
                    String encode = URLEncoder.encode(trim2);
                    String scheme = new URI(encode).getScheme();
                    if (!Bookmarks.urlHasAcceptableScheme(fixUrl.trim())) {
                        if (scheme != null) {
                            this.mAddress.setError(resources.getText(2131493015));
                            return false;
                        }
                        try {
                            WebAddress webAddress = new WebAddress(fixUrl);
                            if (webAddress.getHost().length() == 0) {
                                throw new URISyntaxException("", "");
                            }
                            encode = webAddress.toString();
                        } catch (ParseException e) {
                            throw new URISyntaxException("", "");
                        }
                    }
                    str = URLDecoder.decode(encode);
                }
            } catch (URISyntaxException e2) {
                this.mAddress.setError(resources.getText(2131493014));
                return false;
            }
        }
        boolean equals = str.equals(this.mOriginalUrl);
        if (this.mOverwriteBookmarkId > 0) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("bookmark_current_id", Long.valueOf(this.mCurrentId));
            ContentValues contentValues2 = new ContentValues();
            contentValues2.put("title", trim);
            contentValues2.put("parent", Long.valueOf(this.mCurrentFolder));
            contentValues2.put("url", str);
            if (!equals) {
                contentValues2.putNull("thumbnail");
            }
            if (contentValues2.size() > 0) {
                new UpdateBookmarkTask(getApplicationContext(), this.mOverwriteBookmarkId).execute(contentValues2, contentValues);
            }
            this.mOverwriteBookmarkId = -1L;
            setResult(-1);
            return true;
        }
        this.mOverwriteBookmarkId = haveToOverwriteBookmarkId(trim, str, this.mCurrentFolder);
        if (this.mOverwriteBookmarkId > 0) {
            displayAlertDialogForExistingBookmark();
            return false;
        }
        if (this.mEditingExisting && this.mEditingFolder) {
            Log.d("browser/AddBookmarkPage", "editing folder save");
            long j = this.mMap.getLong("_id", -1L);
            long j2 = this.mMap.getLong("parent", -1L);
            long j3 = this.mCurrentFolder;
            long j4 = j2;
            if (j2 == -1) {
                j4 = this.mRootFolder;
            }
            long j5 = j3;
            if (j3 == -1) {
                j5 = this.mRootFolder;
            }
            if (j4 == j5 && (titleFromId = getTitleFromId(j)) != null && titleFromId.equals(trim)) {
                Log.d("browser/AddBookmarkPage", "edit folder save, does not change anything");
                return true;
            }
        }
        if (this.mEditingFolder && isFolderExist(this.mCurrentFolder, trim)) {
            displayToastForExistingFolder();
            return false;
        }
        if (this.mSaveToHomeScreen) {
            this.mEditingExisting = false;
        }
        if (this.mEditingExisting) {
            long j6 = this.mMap.getLong("_id");
            ContentValues contentValues3 = new ContentValues();
            contentValues3.put("title", trim);
            contentValues3.put("parent", Long.valueOf(this.mCurrentFolder));
            if (!this.mEditingFolder) {
                contentValues3.put("url", str);
                if (!equals) {
                    contentValues3.putNull("thumbnail");
                }
            }
            if (contentValues3.size() > 0) {
                new UpdateBookmarkTask(getApplicationContext(), Long.valueOf(j6).longValue()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, contentValues3);
            }
            setResult(-1);
            return true;
        }
        if (equals) {
            bitmap = (Bitmap) this.mMap.getParcelable("thumbnail");
            bitmap2 = (Bitmap) this.mMap.getParcelable("favicon");
        } else {
            bitmap = null;
            bitmap2 = null;
        }
        Bundle bundle = new Bundle();
        bundle.putString("title", trim);
        bundle.putString("url", str);
        bundle.putParcelable("favicon", bitmap2);
        if (!this.mSaveToHomeScreen) {
            bundle.putParcelable("thumbnail", bitmap);
            bundle.putBoolean("remove_thumbnail", !equals);
            bundle.putString("touch_icon_url", this.mTouchIconUrl);
            Message obtain = Message.obtain(this.mHandler, 100);
            obtain.setData(bundle);
            new Thread(new SaveBookmarkRunnable(this, getApplicationContext(), obtain)).start();
        } else if (this.mTouchIconUrl == null || !equals) {
            sendBroadcast(BookmarkUtils.createAddToHomeIntent(this, str, trim, null, bitmap2));
        } else {
            Message obtain2 = Message.obtain(this.mHandler, 101);
            obtain2.setData(bundle);
            new DownloadTouchIcon(this, obtain2, this.mMap.getString("user_agent")).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this.mTouchIconUrl);
        }
        setResult(-1);
        LogTag.logBookmarkAdded(str, "bookmarkview");
        return true;
    }

    void setAccount(String str, String str2) {
        for (int i = 0; i < this.mAccountAdapter.getCount(); i++) {
            BookmarkAccount item = this.mAccountAdapter.getItem(i);
            if (TextUtils.equals(item.accountName, str) && TextUtils.equals(item.accountType, str2)) {
                this.mAccountSpinner.setSelection(i);
                onRootFolderFound(item.rootFolderId);
                return;
            }
        }
    }
}
