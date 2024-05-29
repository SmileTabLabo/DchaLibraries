package com.android.browser.homepages;

import android.content.Context;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.android.browser.R;
import com.android.browser.homepages.Template;
import com.android.browser.provider.BrowserContract;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/* loaded from: classes.dex */
public class RequestHandler extends Thread {
    private static final String[] PROJECTION;
    private static final Comparator<File> sFileComparator;
    private static final UriMatcher sUriMatcher = new UriMatcher(-1);
    Context mContext;
    OutputStream mOutput;
    Uri mUri;

    static {
        sUriMatcher.addURI("com.android.browser.home", null, 1);
        sUriMatcher.addURI("com.android.browser.home", "res/*/*", 2);
        PROJECTION = new String[]{"url", "title", "thumbnail"};
        sFileComparator = new Comparator<File>() { // from class: com.android.browser.homepages.RequestHandler.3
            @Override // java.util.Comparator
            public int compare(File file, File file2) {
                if (file.isDirectory() != file2.isDirectory()) {
                    return file.isDirectory() ? -1 : 1;
                }
                return file.getName().compareTo(file2.getName());
            }
        };
    }

    public RequestHandler(Context context, Uri uri, OutputStream outputStream) {
        this.mUri = uri;
        this.mContext = context.getApplicationContext();
        this.mOutput = outputStream;
    }

    @Override // java.lang.Thread, java.lang.Runnable
    public void run() {
        super.run();
        try {
            try {
                doHandleRequest();
            } catch (Exception e) {
                Log.e("RequestHandler", "Failed to handle request: " + this.mUri, e);
            }
        } finally {
            cleanup();
        }
    }

    void doHandleRequest() throws IOException {
        if ("file".equals(this.mUri.getScheme())) {
            writeFolderIndex();
            return;
        }
        switch (sUriMatcher.match(this.mUri)) {
            case 1:
                writeTemplatedIndex();
                return;
            case 2:
                writeResource(getUriResourcePath());
                return;
            default:
                return;
        }
    }

    byte[] htmlEncode(String str) {
        return TextUtils.htmlEncode(str).getBytes();
    }

    void writeTemplatedIndex() throws IOException {
        Template cachedTemplate = Template.getCachedTemplate(this.mContext, R.raw.most_visited);
        Cursor query = this.mContext.getContentResolver().query(BrowserContract.History.CONTENT_URI, PROJECTION, "url NOT LIKE 'content:%' AND thumbnail IS NOT NULL", null, "visits DESC LIMIT 12");
        try {
            if (query.getCount() < 12) {
                query = new MergeCursor(new Cursor[]{query, this.mContext.getContentResolver().query(BrowserContract.Bookmarks.CONTENT_URI, PROJECTION, "url NOT LIKE 'content:%' AND thumbnail IS NOT NULL", null, "created DESC LIMIT 12")}) { // from class: com.android.browser.homepages.RequestHandler.1
                    @Override // android.database.MergeCursor, android.database.AbstractCursor, android.database.Cursor
                    public int getCount() {
                        return Math.min(12, super.getCount());
                    }
                };
            }
            cachedTemplate.assignLoop("most_visited", new Template.CursorListEntityWrapper(query) { // from class: com.android.browser.homepages.RequestHandler.2
                @Override // com.android.browser.homepages.Template.EntityData
                public void writeValue(OutputStream outputStream, String str) throws IOException {
                    Cursor cursor = getCursor();
                    if (str.equals("url")) {
                        outputStream.write(RequestHandler.this.htmlEncode(cursor.getString(0)));
                    } else if (str.equals("title")) {
                        outputStream.write(RequestHandler.this.htmlEncode(cursor.getString(1)));
                    } else if (str.equals("thumbnail")) {
                        outputStream.write("data:image/png;base64,".getBytes());
                        outputStream.write(Base64.encode(cursor.getBlob(2), 0));
                    }
                }
            });
            cachedTemplate.write(this.mOutput);
        } finally {
            query.close();
        }
    }

    void writeFolderIndex() throws IOException {
        File file = new File(this.mUri.getPath());
        final File[] listFiles = file.listFiles();
        Arrays.sort(listFiles, sFileComparator);
        Template cachedTemplate = Template.getCachedTemplate(this.mContext, R.raw.folder_view);
        cachedTemplate.assign("path", this.mUri.getPath());
        cachedTemplate.assign("parent_url", file.getParent() != null ? file.getParent() : file.getPath());
        cachedTemplate.assignLoop("files", new Template.ListEntityIterator() { // from class: com.android.browser.homepages.RequestHandler.4
            int index = -1;

            @Override // com.android.browser.homepages.Template.EntityData
            public void writeValue(OutputStream outputStream, String str) throws IOException {
                File file2 = listFiles[this.index];
                if ("name".equals(str)) {
                    outputStream.write(file2.getName().getBytes());
                }
                if ("url".equals(str)) {
                    outputStream.write(("file://" + file2.getAbsolutePath()).getBytes());
                }
                if ("type".equals(str)) {
                    outputStream.write((file2.isDirectory() ? "dir" : "file").getBytes());
                }
                if ("size".equals(str) && file2.isFile()) {
                    outputStream.write(RequestHandler.readableFileSize(file2.length()).getBytes());
                }
                if ("last_modified".equals(str)) {
                    outputStream.write(DateFormat.getDateTimeInstance(3, 3).format(Long.valueOf(file2.lastModified())).getBytes());
                }
                if ("alt".equals(str) && this.index % 2 == 0) {
                    outputStream.write("alt".getBytes());
                }
            }

            @Override // com.android.browser.homepages.Template.EntityData
            public Template.ListEntityIterator getListIterator(String str) {
                return null;
            }

            @Override // com.android.browser.homepages.Template.ListEntityIterator
            public void reset() {
                this.index = -1;
            }

            @Override // com.android.browser.homepages.Template.ListEntityIterator
            public boolean moveToNext() {
                int i = this.index + 1;
                this.index = i;
                return i < listFiles.length;
            }
        });
        cachedTemplate.write(this.mOutput);
    }

    static String readableFileSize(long j) {
        double d;
        if (j <= 0) {
            return "0";
        }
        int log10 = (int) (Math.log10(j) / Math.log10(1024.0d));
        return new DecimalFormat("#,##0.#").format(d / Math.pow(1024.0d, log10)) + " " + new String[]{"B", "KB", "MB", "GB", "TB"}[log10];
    }

    String getUriResourcePath() {
        Matcher matcher = Pattern.compile("/?res/([\\w/]+)").matcher(this.mUri.getPath());
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return this.mUri.getPath();
    }

    void writeResource(String str) throws IOException {
        Resources resources = this.mContext.getResources();
        int identifier = resources.getIdentifier(str, null, R.class.getPackage().getName());
        if (identifier != 0) {
            InputStream openRawResource = resources.openRawResource(identifier);
            byte[] bArr = new byte[4096];
            while (true) {
                int read = openRawResource.read(bArr);
                if (read > 0) {
                    this.mOutput.write(bArr, 0, read);
                } else {
                    return;
                }
            }
        }
    }

    void cleanup() {
        try {
            this.mOutput.close();
        } catch (Exception e) {
            Log.e("RequestHandler", "Failed to close pipe!", e);
        }
    }
}
