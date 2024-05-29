package com.android.browser.widget;

import android.appwidget.AppWidgetManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MergeCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Binder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import com.android.browser.provider.BrowserContract;
import java.io.File;
import java.io.FilenameFilter;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/* loaded from: b.zip:com/android/browser/widget/BookmarkThumbnailWidgetService.class */
public class BookmarkThumbnailWidgetService extends RemoteViewsService {
    private static final String[] PROJECTION = {"_id", "title", "url", "favicon", "folder", "position", "thumbnail", "parent"};

    /* loaded from: b.zip:com/android/browser/widget/BookmarkThumbnailWidgetService$BookmarkFactory.class */
    static class BookmarkFactory implements RemoteViewsService.RemoteViewsFactory {
        private Cursor mBookmarks;
        private Context mContext;
        private int mWidgetId;
        private long mCurrentFolder = -1;
        private long mRootFolder = -1;
        private SharedPreferences mPreferences = null;

        public BookmarkFactory(Context context, int i) {
            this.mContext = context.getApplicationContext();
            this.mWidgetId = i;
        }

        private void resetBookmarks() {
            if (this.mBookmarks != null) {
                this.mBookmarks.close();
                this.mBookmarks = null;
            }
        }

        @Override // android.widget.RemoteViewsService.RemoteViewsFactory
        public int getCount() {
            if (this.mBookmarks == null) {
                return 0;
            }
            return this.mBookmarks.getCount();
        }

        @Override // android.widget.RemoteViewsService.RemoteViewsFactory
        public long getItemId(int i) {
            return i;
        }

        @Override // android.widget.RemoteViewsService.RemoteViewsFactory
        public RemoteViews getLoadingView() {
            return new RemoteViews(this.mContext.getPackageName(), 2130968591);
        }

        @Override // android.widget.RemoteViewsService.RemoteViewsFactory
        public RemoteViews getViewAt(int i) {
            if (this.mBookmarks.moveToPosition(i)) {
                long j = this.mBookmarks.getLong(0);
                String string = this.mBookmarks.getString(1);
                String string2 = this.mBookmarks.getString(2);
                boolean z = this.mBookmarks.getInt(4) != 0;
                RemoteViews remoteViews = z ? new RemoteViews(this.mContext.getPackageName(), 2130968592) : new RemoteViews(this.mContext.getPackageName(), 2130968591);
                String str = string;
                if (TextUtils.isEmpty(string)) {
                    str = string2;
                }
                remoteViews.setTextViewText(2131558424, str);
                if (z) {
                    if (j == this.mCurrentFolder) {
                        j = this.mBookmarks.getLong(7);
                        remoteViews.setImageViewResource(2131558430, 2130837611);
                    } else {
                        remoteViews.setImageViewResource(2131558430, 2130837612);
                    }
                    remoteViews.setImageViewResource(2131558406, 2130837539);
                    remoteViews.setDrawableParameters(2131558430, true, 0, -1, null, -1);
                } else {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    byte[] blob = this.mBookmarks.getBlob(6);
                    remoteViews.setDrawableParameters(2131558430, true, 255, -1, null, -1);
                    if (blob == null || blob.length <= 0) {
                        remoteViews.setImageViewResource(2131558430, 2130837518);
                    } else {
                        remoteViews.setImageViewBitmap(2131558430, BitmapFactory.decodeByteArray(blob, 0, blob.length, options));
                    }
                    byte[] blob2 = this.mBookmarks.getBlob(3);
                    if (blob2 == null || blob2.length <= 0) {
                        remoteViews.setImageViewResource(2131558406, 2130837505);
                    } else {
                        remoteViews.setImageViewBitmap(2131558406, BitmapFactory.decodeByteArray(blob2, 0, blob2.length, options));
                    }
                }
                remoteViews.setOnClickFillInIntent(2131558423, z ? new Intent("com.android.browser.widget.CHANGE_FOLDER").putExtra("appWidgetId", this.mWidgetId).putExtra("_id", j) : !TextUtils.isEmpty(string2) ? new Intent("android.intent.action.VIEW").addCategory("android.intent.category.BROWSABLE").setData(Uri.parse(string2)) : new Intent("show_browser"));
                return remoteViews;
            }
            return null;
        }

        @Override // android.widget.RemoteViewsService.RemoteViewsFactory
        public int getViewTypeCount() {
            return 2;
        }

        @Override // android.widget.RemoteViewsService.RemoteViewsFactory
        public boolean hasStableIds() {
            return false;
        }

        void loadBookmarks() {
            resetBookmarks();
            this.mBookmarks = this.mContext.getContentResolver().query(ContentUris.withAppendedId(BrowserContract.Bookmarks.CONTENT_URI_DEFAULT_FOLDER, this.mCurrentFolder), BookmarkThumbnailWidgetService.PROJECTION, null, null, null);
            if (this.mCurrentFolder != this.mRootFolder) {
                this.mBookmarks = new MergeCursor(new Cursor[]{this.mContext.getContentResolver().query(ContentUris.withAppendedId(BrowserContract.Bookmarks.CONTENT_URI, this.mCurrentFolder), BookmarkThumbnailWidgetService.PROJECTION, null, null, null), this.mBookmarks});
                if (this.mBookmarks.getCount() == 0) {
                    this.mBookmarks = new MergeCursor(new Cursor[]{this.mContext.getContentResolver().query(BrowserContract.Bookmarks.CONTENT_URI, BookmarkThumbnailWidgetService.PROJECTION, "parent=?", new String[]{String.valueOf(this.mRootFolder)}, null), this.mBookmarks});
                }
            }
        }

        @Override // android.widget.RemoteViewsService.RemoteViewsFactory
        public void onCreate() {
        }

        @Override // android.widget.RemoteViewsService.RemoteViewsFactory
        public void onDataSetChanged() {
            long clearCallingIdentity = Binder.clearCallingIdentity();
            syncState();
            if (this.mRootFolder < 0 || this.mCurrentFolder < 0) {
                this.mRootFolder = 1L;
                this.mCurrentFolder = this.mRootFolder;
                saveState();
            }
            loadBookmarks();
            Binder.restoreCallingIdentity(clearCallingIdentity);
        }

        @Override // android.widget.RemoteViewsService.RemoteViewsFactory
        public void onDestroy() {
            if (this.mBookmarks != null) {
                this.mBookmarks.close();
                this.mBookmarks = null;
            }
            BookmarkThumbnailWidgetService.deleteWidgetState(this.mContext, this.mWidgetId);
        }

        void saveState() {
            if (this.mPreferences == null) {
                this.mPreferences = BookmarkThumbnailWidgetService.getWidgetState(this.mContext, this.mWidgetId);
            }
            this.mPreferences.edit().putLong("current_folder", this.mCurrentFolder).putLong("root_folder", this.mRootFolder).commit();
        }

        void syncState() {
            if (this.mPreferences == null) {
                this.mPreferences = BookmarkThumbnailWidgetService.getWidgetState(this.mContext, this.mWidgetId);
            }
            long j = this.mPreferences.getLong("current_folder", -1L);
            this.mRootFolder = this.mPreferences.getLong("root_folder", -1L);
            if (j != this.mCurrentFolder) {
                resetBookmarks();
                this.mCurrentFolder = j;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: b.zip:com/android/browser/widget/BookmarkThumbnailWidgetService$StateFilter.class */
    public static class StateFilter implements FilenameFilter {
        static final Pattern sStatePattern = Pattern.compile("widgetState-(\\d+)\\.xml");
        HashSet<Integer> mWidgetIds = new HashSet<>();

        StateFilter(int[] iArr) {
            for (int i : iArr) {
                this.mWidgetIds.add(Integer.valueOf(i));
            }
        }

        @Override // java.io.FilenameFilter
        public boolean accept(File file, String str) {
            Matcher matcher = sStatePattern.matcher(str);
            if (matcher.matches()) {
                return !this.mWidgetIds.contains(Integer.valueOf(Integer.parseInt(matcher.group(1))));
            }
            return false;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void changeFolder(Context context, Intent intent) {
        int intExtra = intent.getIntExtra("appWidgetId", -1);
        long longExtra = intent.getLongExtra("_id", -1L);
        if (intExtra >= 0 && longExtra >= 0) {
            getWidgetState(context, intExtra).edit().putLong("current_folder", longExtra).commit();
            AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(intExtra, 2131558438);
        }
        BookmarkThumbnailWidgetProvider.refreshWidgets(context);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void deleteWidgetState(Context context, int i) {
        File sharedPrefsFile = context.getSharedPrefsFile(String.format("widgetState-%d", Integer.valueOf(i)));
        if (!sharedPrefsFile.exists() || sharedPrefsFile.delete()) {
            return;
        }
        sharedPrefsFile.deleteOnExit();
    }

    static SharedPreferences getWidgetState(Context context, int i) {
        return context.getSharedPreferences(String.format("widgetState-%d", Integer.valueOf(i)), 0);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void removeOrphanedStates(Context context, int[] iArr) {
        File[] listFiles = context.getSharedPrefsFile("null").getParentFile().listFiles(new StateFilter(iArr));
        if (listFiles != null) {
            for (File file : listFiles) {
                Log.w("BookmarkThumbnailWidgetService", "Found orphaned state: " + file.getName());
                if (!file.delete()) {
                    file.deleteOnExit();
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void setupWidgetState(Context context, int i, long j) {
        getWidgetState(context, i).edit().putLong("current_folder", j).putLong("root_folder", j).apply();
    }

    @Override // android.widget.RemoteViewsService
    public RemoteViewsService.RemoteViewsFactory onGetViewFactory(Intent intent) {
        int intExtra = intent.getIntExtra("appWidgetId", -1);
        if (intExtra < 0) {
            Log.w("BookmarkThumbnailWidgetService", "Missing EXTRA_APPWIDGET_ID!");
            return null;
        }
        return new BookmarkFactory(getApplicationContext(), intExtra);
    }
}
