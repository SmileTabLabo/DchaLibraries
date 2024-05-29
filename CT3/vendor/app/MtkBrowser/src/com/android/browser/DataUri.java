package com.android.browser;

import java.net.MalformedURLException;
import libcore.io.Base64;
/* loaded from: b.zip:com/android/browser/DataUri.class */
public class DataUri {
    private byte[] mData;
    private String mMimeType;

    public DataUri(String str) throws MalformedURLException {
        if (!isDataUri(str)) {
            throw new MalformedURLException("Not a data URI");
        }
        int indexOf = str.indexOf(44, "data:".length());
        if (indexOf < 0) {
            throw new MalformedURLException("Comma expected in data URI");
        }
        String substring = str.substring("data:".length(), indexOf);
        this.mData = str.substring(indexOf + 1).getBytes();
        if (substring.contains(";base64")) {
            this.mData = Base64.decode(this.mData);
        }
        int indexOf2 = substring.indexOf(59);
        if (indexOf2 > 0) {
            this.mMimeType = substring.substring(0, indexOf2);
        } else {
            this.mMimeType = substring;
        }
    }

    public static boolean isDataUri(String str) {
        return str.startsWith("data:");
    }

    public byte[] getData() {
        return this.mData;
    }

    public String getMimeType() {
        return this.mMimeType;
    }
}
