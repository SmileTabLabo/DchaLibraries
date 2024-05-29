package com.android.browser;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.android.browser.provider.BrowserContract;
import com.android.browser.provider.BrowserProvider2;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
/* loaded from: classes.dex */
public class DataController {
    private static final boolean DEBUG = Browser.DEBUG;
    private static DataController sInstance;
    private ByteBuffer mBuffer;
    private Handler mCbHandler;
    private Context mContext;
    private DataControllerHandler mDataHandler = new DataControllerHandler();

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public interface OnQueryUrlIsBookmark {
        void onQueryUrlIsBookmark(String str, boolean z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class CallbackContainer {
        Object[] args;
        Object replyTo;

        private CallbackContainer() {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
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
    public static DataController getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DataController(context);
        }
        return sInstance;
    }

    private DataController(Context context) {
        this.mContext = context.getApplicationContext();
        this.mDataHandler.start();
        this.mCbHandler = new Handler() { // from class: com.android.browser.DataController.1
            @Override // android.os.Handler
            public void handleMessage(Message message) {
                CallbackContainer callbackContainer = (CallbackContainer) message.obj;
                if (message.what == 200) {
                    ((OnQueryUrlIsBookmark) callbackContainer.replyTo).onQueryUrlIsBookmark((String) callbackContainer.args[0], ((Boolean) callbackContainer.args[1]).booleanValue());
                }
            }
        };
    }

    public void updateVisitedHistory(String str) {
        this.mDataHandler.sendMessage(100, str);
    }

    public void updateHistoryTitle(String str, String str2) {
        this.mDataHandler.sendMessage(101, new String[]{str, str2});
    }

    public void queryBookmarkStatus(String str, OnQueryUrlIsBookmark onQueryUrlIsBookmark) {
        if (str == null || str.trim().length() == 0) {
            onQueryUrlIsBookmark.onQueryUrlIsBookmark(str, false);
        } else {
            this.mDataHandler.sendMessage(200, str.trim(), onQueryUrlIsBookmark);
        }
    }

    public void loadThumbnail(Tab tab) {
        this.mDataHandler.sendMessage(201, tab);
    }

    public void deleteThumbnail(Tab tab) {
        this.mDataHandler.sendMessage(203, Long.valueOf(tab.getId()));
    }

    public void saveThumbnail(Tab tab) {
        this.mDataHandler.sendMessage(202, tab);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class DataControllerHandler extends Thread {
        private BlockingQueue<DCMessage> mMessageQueue;

        public DataControllerHandler() {
            super("DataControllerHandler");
            this.mMessageQueue = new LinkedBlockingQueue();
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

        private void handleMessage(DCMessage dCMessage) {
            int i = dCMessage.what;
            switch (i) {
                case 100:
                    doUpdateVisitedHistory((String) dCMessage.obj);
                    return;
                case 101:
                    String[] strArr = (String[]) dCMessage.obj;
                    doUpdateHistoryTitle(strArr[0], strArr[1]);
                    return;
                default:
                    switch (i) {
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
                                DataController.this.mContext.getContentResolver().delete(ContentUris.withAppendedId(BrowserProvider2.Thumbnails.CONTENT_URI, ((Long) dCMessage.obj).longValue()), null, null);
                                return;
                            } catch (Throwable th) {
                                return;
                            }
                        default:
                            return;
                    }
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
                if (DataController.this.mBuffer == null || DataController.this.mBuffer.limit() < byteArrayOutputStream.size()) {
                    DataController.this.mBuffer = ByteBuffer.allocate(byteArrayOutputStream.size());
                }
                DataController.this.mBuffer.put(byteArrayOutputStream.toByteArray());
                DataController.this.mBuffer.rewind();
                return DataController.this.mBuffer.array();
            }
        }

        private void doSaveThumbnail(Tab tab) {
            byte[] captureBlob = getCaptureBlob(tab);
            if (captureBlob != null) {
                ContentResolver contentResolver = DataController.this.mContext.getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put("_id", Long.valueOf(tab.getId()));
                contentValues.put("thumbnail", captureBlob);
                contentResolver.insert(BrowserProvider2.Thumbnails.CONTENT_URI, contentValues);
            }
        }

        private void doLoadThumbnail(Tab tab) {
            byte[] blob;
            Cursor cursor = null;
            try {
                Cursor query = DataController.this.mContext.getContentResolver().query(ContentUris.withAppendedId(BrowserProvider2.Thumbnails.CONTENT_URI, tab.getId()), new String[]{"_id", "thumbnail"}, null, null, null);
                try {
                    if (query.moveToFirst() && !query.isNull(1) && (blob = query.getBlob(1)) != null && blob.length > 0) {
                        tab.updateCaptureFromBlob(blob);
                    }
                    if (query != null) {
                        query.close();
                    }
                } catch (Throwable th) {
                    th = th;
                    cursor = query;
                    if (cursor != null) {
                        cursor.close();
                    }
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
            }
        }

        private String findHistoryUrlInBookmark(String str) {
            String str2;
            Cursor cursor = null;
            try {
                if (DataController.DEBUG) {
                    Log.d("DataController", "historyUrl is: " + str);
                }
                ContentResolver contentResolver = DataController.this.mContext.getContentResolver();
                Uri bookmarksUri = BookmarkUtils.getBookmarksUri(DataController.this.mContext);
                String[] strArr = {"url"};
                String[] strArr2 = new String[2];
                strArr2[0] = str;
                if (str.endsWith("/")) {
                    str2 = str.substring(0, str.lastIndexOf("/"));
                } else {
                    str2 = str + "/";
                }
                strArr2[1] = str2;
                Cursor query = contentResolver.query(bookmarksUri, strArr, "url == ? OR url == ?", strArr2, null);
                if (query != null) {
                    try {
                        if (query.moveToNext()) {
                            str = query.getString(0);
                            if (DataController.DEBUG) {
                                Log.d("DataController", "Url in bookmark table is: " + str);
                                Log.d("DataController", "save url to history table is: " + str);
                            }
                        }
                    } catch (Throwable th) {
                        th = th;
                        cursor = query;
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th;
                    }
                }
                if (query != null) {
                    query.close();
                }
                return str;
            } catch (Throwable th2) {
                th = th2;
            }
        }

        private void doUpdateVisitedHistory(String str) {
            Throwable th;
            Cursor cursor;
            String str2;
            String findHistoryUrlInBookmark = findHistoryUrlInBookmark(str);
            ContentResolver contentResolver = DataController.this.mContext.getContentResolver();
            try {
                Uri uri = BrowserContract.History.CONTENT_URI;
                String[] strArr = {"_id", "visits"};
                String[] strArr2 = new String[2];
                strArr2[0] = str;
                if (str.endsWith("/")) {
                    str2 = str.substring(0, str.lastIndexOf("/"));
                } else {
                    str2 = str + "/";
                }
                strArr2[1] = str2;
                cursor = contentResolver.query(uri, strArr, "url==? OR url==?", strArr2, null);
                try {
                    if (cursor.moveToFirst()) {
                        if (DataController.DEBUG) {
                            Log.d("DataController", "update history to " + findHistoryUrlInBookmark);
                        }
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("url", findHistoryUrlInBookmark);
                        contentValues.put("visits", Integer.valueOf(cursor.getInt(1) + 1));
                        contentValues.put("date", Long.valueOf(System.currentTimeMillis()));
                        contentResolver.update(ContentUris.withAppendedId(BrowserContract.History.CONTENT_URI, cursor.getLong(0)), contentValues, null, null);
                    } else {
                        if (DataController.DEBUG) {
                            Log.d("DataController", "insert new history to " + findHistoryUrlInBookmark);
                        }
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
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (cursor != null) {
                        cursor.close();
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                cursor = null;
            }
        }

        /* JADX WARN: Removed duplicated region for block: B:22:0x007e  */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
        */
        private void doQueryBookmarkStatus(String str, Object obj) {
            Cursor cursor;
            boolean z;
            Cursor cursor2 = null;
            try {
                cursor = DataController.this.mContext.getContentResolver().query(BookmarkUtils.getBookmarksUri(DataController.this.mContext), new String[]{"url"}, "url == ?", new String[]{str}, null);
                try {
                    try {
                        z = cursor.moveToFirst();
                        if (cursor != null) {
                            cursor.close();
                        }
                    } catch (SQLiteException e) {
                        e = e;
                        Log.e("DataController", "Error checking for bookmark: " + e);
                        if (cursor != null) {
                            cursor.close();
                        }
                        z = false;
                        CallbackContainer callbackContainer = new CallbackContainer();
                        callbackContainer.replyTo = obj;
                        callbackContainer.args = new Object[]{str, Boolean.valueOf(z)};
                        DataController.this.mCbHandler.obtainMessage(200, callbackContainer).sendToTarget();
                    }
                } catch (Throwable th) {
                    th = th;
                    cursor2 = cursor;
                    if (cursor2 != null) {
                        cursor2.close();
                    }
                    throw th;
                }
            } catch (SQLiteException e2) {
                e = e2;
                cursor = null;
            } catch (Throwable th2) {
                th = th2;
                if (cursor2 != null) {
                }
                throw th;
            }
            CallbackContainer callbackContainer2 = new CallbackContainer();
            callbackContainer2.replyTo = obj;
            callbackContainer2.args = new Object[]{str, Boolean.valueOf(z)};
            DataController.this.mCbHandler.obtainMessage(200, callbackContainer2).sendToTarget();
        }

        private void doUpdateHistoryTitle(String str, String str2) {
            String findHistoryUrlInBookmark = findHistoryUrlInBookmark(str);
            ContentResolver contentResolver = DataController.this.mContext.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put("title", str2);
            contentValues.put("url", findHistoryUrlInBookmark);
            if (contentResolver.update(BrowserContract.History.CONTENT_URI, contentValues, "url==?", new String[]{str}) <= 0 && findHistoryUrlInBookmark.endsWith("/")) {
                contentResolver.update(BrowserContract.History.CONTENT_URI, contentValues, "url==?", new String[]{findHistoryUrlInBookmark.substring(0, findHistoryUrlInBookmark.lastIndexOf("/"))});
            }
        }
    }
}
