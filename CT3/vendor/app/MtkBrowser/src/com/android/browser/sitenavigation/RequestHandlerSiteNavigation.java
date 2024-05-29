package com.android.browser.sitenavigation;

import android.content.Context;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.android.browser.R;
import com.android.browser.sitenavigation.TemplateSiteNavigation;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/* loaded from: b.zip:com/android/browser/sitenavigation/RequestHandlerSiteNavigation.class */
public class RequestHandlerSiteNavigation extends Thread {
    private static final UriMatcher S_URI_MATCHER = new UriMatcher(-1);
    Context mContext;
    OutputStream mOutput;
    Uri mUri;

    static {
        S_URI_MATCHER.addURI("com.android.browser.site_navigation", "websites/res/*/*", 2);
        S_URI_MATCHER.addURI("com.android.browser.site_navigation", "websites", 1);
    }

    public RequestHandlerSiteNavigation(Context context, Uri uri, OutputStream outputStream) {
        this.mUri = uri;
        this.mContext = context.getApplicationContext();
        this.mOutput = outputStream;
    }

    private void writeTemplatedIndex() throws IOException {
        TemplateSiteNavigation cachedTemplate = TemplateSiteNavigation.getCachedTemplate(this.mContext, 2131165208);
        Cursor cursor = null;
        try {
            Cursor query = this.mContext.getContentResolver().query(Uri.parse("content://com.android.browser.site_navigation/websites"), new String[]{"url", "title", "thumbnail"}, null, null, null);
            cachedTemplate.assignLoop("site_navigation", new TemplateSiteNavigation.CursorListEntityWrapper(this, query) { // from class: com.android.browser.sitenavigation.RequestHandlerSiteNavigation.1
                final RequestHandlerSiteNavigation this$0;

                {
                    this.this$0 = this;
                }

                /* JADX WARN: Code restructure failed: missing block: B:11:0x003c, code lost:
                    if (r0.length() == 0) goto L14;
                 */
                @Override // com.android.browser.sitenavigation.TemplateSiteNavigation.EntityData
                /*
                    Code decompiled incorrectly, please refer to instructions dump.
                */
                public void writeValue(OutputStream outputStream, String str) throws IOException {
                    String string;
                    Cursor cursor2 = getCursor();
                    if (str.equals("url")) {
                        outputStream.write(this.this$0.htmlEncode(cursor2.getString(0)));
                    } else if (!str.equals("title")) {
                        if (str.equals("thumbnail")) {
                            outputStream.write("data:image/png;base64,".getBytes());
                            outputStream.write(Base64.encode(cursor2.getBlob(2), 0));
                        }
                    } else {
                        String string2 = cursor2.getString(1);
                        if (string2 != null) {
                            string = string2;
                        }
                        string = this.this$0.mContext.getString(2131492880);
                        outputStream.write(this.this$0.htmlEncode(string));
                    }
                }
            });
            cursor = query;
            cachedTemplate.write(this.mOutput);
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

    void cleanup() {
        try {
            this.mOutput.close();
        } catch (IOException e) {
            Log.e("RequestHandlerSiteNavigation", "Failed to close pipe!", e);
        }
    }

    void doHandleRequest() throws IOException {
        switch (S_URI_MATCHER.match(this.mUri)) {
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

    String getUriResourcePath() {
        Matcher matcher = Pattern.compile("/?res/([\\w/]+)").matcher(this.mUri.getPath());
        return matcher.matches() ? matcher.group(1) : this.mUri.getPath();
    }

    byte[] htmlEncode(String str) {
        return TextUtils.htmlEncode(str).getBytes();
    }

    @Override // java.lang.Thread, java.lang.Runnable
    public void run() {
        super.run();
        try {
            try {
                doHandleRequest();
                cleanup();
            } catch (IOException e) {
                Log.e("RequestHandlerSiteNavigation", "Failed to handle request: " + this.mUri, e);
                cleanup();
            }
        } catch (Throwable th) {
            cleanup();
            throw th;
        }
    }

    void writeResource(String str) throws IOException {
        Resources resources = this.mContext.getResources();
        int identifier = resources.getIdentifier(str, null, R.class.getPackage().getName());
        if (identifier == 0) {
            return;
        }
        InputStream openRawResource = resources.openRawResource(identifier);
        byte[] bArr = new byte[4096];
        while (true) {
            int read = openRawResource.read(bArr);
            if (read <= 0) {
                openRawResource.close();
                return;
            }
            this.mOutput.write(bArr, 0, read);
        }
    }
}
