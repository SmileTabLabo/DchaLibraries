package com.android.quicksearchbox.util;

import java.io.IOException;
import java.util.Map;
/* loaded from: classes.dex */
public interface HttpHelper {

    /* loaded from: classes.dex */
    public interface UrlRewriter {
        String rewrite(String str);
    }

    String get(GetRequest getRequest) throws IOException, HttpException;

    /* loaded from: classes.dex */
    public static class GetRequest {
        private Map<String, String> mHeaders;
        private String mUrl;

        public GetRequest() {
        }

        public GetRequest(String str) {
            this.mUrl = str;
        }

        public String getUrl() {
            return this.mUrl;
        }

        public Map<String, String> getHeaders() {
            return this.mHeaders;
        }
    }

    /* loaded from: classes.dex */
    public static class HttpException extends IOException {
        private final String mReasonPhrase;
        private final int mStatusCode;

        public HttpException(int i, String str) {
            super(i + " " + str);
            this.mStatusCode = i;
            this.mReasonPhrase = str;
        }
    }
}
