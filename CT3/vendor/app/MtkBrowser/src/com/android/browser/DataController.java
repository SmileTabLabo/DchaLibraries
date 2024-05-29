package com.android.browser;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.android.browser.provider.BrowserContract;
import com.android.browser.provider.BrowserProvider2;
import com.mediatek.browser.ext.IBrowserFeatureIndexExt;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
/* loaded from: b.zip:com/android/browser/DataController.class */
public class DataController {
    private static DataController sInstance;
    private ByteBuffer mBuffer;
    private Handler mCbHandler;
    private Context mContext;
    private DataControllerHandler mDataHandler = new DataControllerHandler(this);

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: b.zip:com/android/browser/DataController$CallbackContainer.class */
    public static class CallbackContainer {
        Object[] args;
        Object replyTo;

        private CallbackContainer() {
        }

        /* synthetic */ CallbackContainer(CallbackContainer callbackContainer) {
            this();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: b.zip:com/android/browser/DataController$DCMessage.class */
    public static class DCMessage {
        Object obj;
        Object replyTo;
        int what;

        DCMessage(int i, Object obj) {
            this.what = i;
            this.obj = obj;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: b.zip:com/android/browser/DataController$DataControllerHandler.class */
    public class DataControllerHandler extends Thread {
        private BlockingQueue<DCMessage> mMessageQueue;
        final DataController this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public DataControllerHandler(DataController dataController) {
            super("DataControllerHandler");
            this.this$0 = dataController;
            this.mMessageQueue = new LinkedBlockingQueue();
        }

        private void doLoadThumbnail(Tab tab) {
            byte[] blob;
            Cursor cursor = null;
            try {
                Cursor query = this.this$0.mContext.getContentResolver().query(ContentUris.withAppendedId(BrowserProvider2.Thumbnails.CONTENT_URI, tab.getId()), new String[]{"_id", "thumbnail"}, null, null, null);
                if (query.moveToFirst() && !query.isNull(1) && (blob = query.getBlob(1)) != null && blob.length > 0) {
                    cursor = query;
                    tab.updateCaptureFromBlob(blob);
                }
                if (query != null) {
                    query.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
                throw th;
            }
        }

        private void doQueryBookmarkStatus(String str, Object obj) {
            boolean z;
            Cursor cursor = null;
            Cursor cursor2 = null;
            try {
                try {
                    Cursor query = this.this$0.mContext.getContentResolver().query(BookmarkUtils.getBookmarksUri(this.this$0.mContext), new String[]{"url"}, "url == ?", new String[]{str}, null);
                    cursor2 = query;
                    cursor = query;
                    boolean moveToFirst = query.moveToFirst();
                    z = moveToFirst;
                    if (query != null) {
                        query.close();
                        z = moveToFirst;
                    }
                } catch (SQLiteException e) {
                    Cursor cursor3 = cursor2;
                    cursor = cursor2;
                    Log.e("DataController", "Error checking for bookmark: " + e);
                    z = false;
                    if (cursor2 != null) {
                        cursor2.close();
                        z = false;
                    }
                }
                CallbackContainer callbackContainer = new CallbackContainer(null);
                callbackContainer.replyTo = obj;
                callbackContainer.args = new Object[]{str, Boolean.valueOf(z)};
                this.this$0.mCbHandler.obtainMessage(200, callbackContainer).sendToTarget();
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
                throw th;
            }
        }

        private void doSaveThumbnail(Tab tab) {
            byte[] captureBlob = getCaptureBlob(tab);
            if (captureBlob == null) {
                return;
            }
            ContentResolver contentResolver = this.this$0.mContext.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put("_id", Long.valueOf(tab.getId()));
            contentValues.put("thumbnail", captureBlob);
            contentResolver.insert(BrowserProvider2.Thumbnails.CONTENT_URI, contentValues);
        }

        private void doUpdateHistoryTitle(String str, String str2) {
            String findHistoryUrlInBookmark = findHistoryUrlInBookmark(str);
            ContentResolver contentResolver = this.this$0.mContext.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put("title", str2);
            contentValues.put("url", findHistoryUrlInBookmark);
            if (contentResolver.update(BrowserContract.History.CONTENT_URI, contentValues, "url==?", new String[]{str}) > 0 || !findHistoryUrlInBookmark.endsWith("/")) {
                return;
            }
            contentResolver.update(BrowserContract.History.CONTENT_URI, contentValues, "url==?", new String[]{findHistoryUrlInBookmark.substring(0, findHistoryUrlInBookmark.lastIndexOf("/"))});
        }

        private void doUpdateVisitedHistory(String str) {
            String findHistoryUrlInBookmark = findHistoryUrlInBookmark(str);
            ContentResolver contentResolver = this.this$0.mContext.getContentResolver();
            Cursor cursor = null;
            try {
                Cursor query = contentResolver.query(BrowserContract.History.CONTENT_URI, new String[]{"_id", "visits"}, "url==? OR url==?", new String[]{str, str.endsWith("/") ? str.substring(0, str.lastIndexOf("/")) : str + "/"}, null);
                if (query.moveToFirst()) {
                    Log.d("DataController", "update history to " + findHistoryUrlInBookmark);
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("url", findHistoryUrlInBookmark);
                    contentValues.put("visits", Integer.valueOf(query.getInt(1) + 1));
                    contentValues.put("date", Long.valueOf(System.currentTimeMillis()));
                    contentResolver.update(ContentUris.withAppendedId(BrowserContract.History.CONTENT_URI, query.getLong(0)), contentValues, null, null);
                } else {
                    Log.d("DataController", "insert new history to " + findHistoryUrlInBookmark);
                    com.android.browser.provider.Browser.truncateHistory(contentResolver);
                    ContentValues contentValues2 = new ContentValues();
                    contentValues2.put("url", findHistoryUrlInBookmark);
                    contentValues2.put("visits", (Integer) 1);
                    contentValues2.put("date", Long.valueOf(System.currentTimeMillis()));
                    contentValues2.put("title", findHistoryUrlInBookmark);
                    contentValues2.put("created", (Integer) 0);
                    contentValues2.put("user_entered", (Integer) 0);
                    contentResolver.insert(BrowserContract.History.CONTENT_URI, contentValues2);
                }
                if (query != null) {
                    query.close();
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    cursor.close();
                }
                throw th;
            }
        }

        private String findHistoryUrlInBookmark(String str) {
            Cursor cursor = null;
            try {
                Log.d("DataController", "historyUrl is: " + str);
                Cursor query = this.this$0.mContext.getContentResolver().query(BookmarkUtils.getBookmarksUri(this.this$0.mContext), new String[]{"url"}, "url == ? OR url == ?", new String[]{str, str.endsWith("/") ? str.substring(0, str.lastIndexOf("/")) : str + "/"}, null);
                String str2 = str;
                if (query != null) {
                    str2 = str;
                    if (query.moveToNext()) {
                        String string = query.getString(0);
                        Log.d("DataController", "Url in bookmark table is: " + string);
                        str2 = string;
                        cursor = query;
                        Log.d("DataController", "save url to history table is: " + string);
                    }
                }
                if (query != null) {
                    query.close();
                }
                return str2;
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
                throw th;
            }
        }

        private byte[] getCaptureBlob(Tab tab) {
            synchronized (tab) {
                Bitmap screenshot = tab.getScreenshot();
                if (screenshot == null) {
                    return null;
                }
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                screenshot.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                if (this.this$0.mBuffer == null || this.this$0.mBuffer.limit() < byteArrayOutputStream.size()) {
                    this.this$0.mBuffer = ByteBuffer.allocate(byteArrayOutputStream.size());
                }
                this.this$0.mBuffer.put(byteArrayOutputStream.toByteArray());
                this.this$0.mBuffer.rewind();
                return this.this$0.mBuffer.array();
            }
        }

        private void handleMessage(DCMessage dCMessage) {
            switch (dCMessage.what) {
                case IBrowserFeatureIndexExt.CUSTOM_PREFERENCE_LIST /* 100 */:
                    doUpdateVisitedHistory((String) dCMessage.obj);
                    return;
                case 101:
                    String[] strArr = (String[]) dCMessage.obj;
                    doUpdateHistoryTitle(strArr[0], strArr[1]);
                    return;
                case 200:
                    doQueryBookmarkStatus((String) dCMessage.obj, dCMessage.replyTo);
                    return;
                case 201:
                    doLoadThumbnail((Tab) dCMessage.obj);
                    return;
                case 202:
                    doSaveThumbnail((Tab) dCMessage.obj);
                    return;
                case 203:
                    try {
                        this.this$0.mContext.getContentResolver().delete(ContentUris.withAppendedId(BrowserProvider2.Thumbnails.CONTENT_URI, ((Long) dCMessage.obj).longValue()), null, null);
                        return;
                    } catch (Throwable th) {
                        return;
                    }
                default:
                    return;
            }
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            setPriority(1);
            while (true) {
                try {
                    handleMessage(this.mMessageQueue.take());
                } catch (InterruptedException e) {
                    return;
                }
            }
        }

        void sendMessage(int i, Object obj) {
            this.mMessageQueue.add(new DCMessage(i, obj));
        }

        void sendMessage(int i, Object obj, Object obj2) {
            DCMessage dCMessage = new DCMessage(i, obj);
            dCMessage.replyTo = obj2;
            this.mMessageQueue.add(dCMessage);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: b.zip:com/android/browser/DataController$OnQueryUrlIsBookmark.class */
    public interface OnQueryUrlIsBookmark {
        void onQueryUrlIsBookmark(String str, boolean z);
    }

    private DataController(Context context) {
        this.mContext = context.getApplicationContext();
        this.mDataHandler.start();
        this.mCbHandler = new Handler(this) { // from class: com.android.browser.DataController.1
            final DataController this$0;

            {
                this.this$0 = this;
            }

            @Override // android.os.Handler
            public void handleMessage(Message message) {
                CallbackContainer callbackContainer = (CallbackContainer) message.obj;
                switch (message.what) {
                    case 200:
                        ((OnQueryUrlIsBookmark) callbackContainer.replyTo).onQueryUrlIsBookmark((String) callbackContainer.args[0], ((Boolean) callbackContainer.args[1]).booleanValue());
                        return;
                    default:
                        return;
                }
            }
        };
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static DataController getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DataController(context);
        }
        return sInstance;
    }

    public void deleteThumbnail(Tab tab) {
        this.mDataHandler.sendMessage(203, Long.valueOf(tab.getId()));
    }

    public void loadThumbnail(Tab tab) {
        this.mDataHandler.sendMessage(201, tab);
    }

    public void queryBookmarkStatus(String str, OnQueryUrlIsBookmark onQueryUrlIsBookmark) {
        if (str == null || str.trim().length() == 0) {
            onQueryUrlIsBookmark.onQueryUrlIsBookmark(str, false);
        } else {
            this.mDataHandler.sendMessage(200, str.trim(), onQueryUrlIsBookmark);
        }
    }

    public void saveThumbnail(Tab tab) {
        this.mDataHandler.sendMessage(202, tab);
    }

    public void updateHistoryTitle(String str, String str2) {
        this.mDataHandler.sendMessage(101, new String[]{str, str2});
    }

    public void updateVisitedHistory(String str) {
        this.mDataHandler.sendMessage(100, str);
    }
}
