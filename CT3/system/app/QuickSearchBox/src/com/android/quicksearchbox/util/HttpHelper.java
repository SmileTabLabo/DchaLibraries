package com.android.quicksearchbox.util;

import java.io.IOException;
import java.util.Map;
/* loaded from: a.zip:com/android/quicksearchbox/util/HttpHelper.class */
public interface HttpHelper {

    /* loaded from: a.zip:com/android/quicksearchbox/util/HttpHelper$GetRequest.class */
    public static class GetRequest {
        private Map<String, String> mHeaders;
        private String mUrl;

        public GetRequest() {
        }

        public GetRequest(String str) {
            this.mUrl = str;
        }

        public Map<String, String> getHeaders() {
            return this.mHeaders;
        }

        public String getUrl() {
            return this.mUrl;
        }
    }

    /* loaded from: a.zip:com/android/quicksearchbox/util/HttpHelper$HttpException.class */
    public static class HttpException extends IOException {
        private final String mReasonPhrase;
        private final int mStatusCode;

        public HttpException(int i, String str) {
            super(i + " " + str);
            this.mStatusCode = i;
            this.mReasonPhrase = str;
        }
    }

    /* loaded from: a.zip:com/android/quicksearchbox/util/HttpHelper$UrlRewriter.class */
    public interface UrlRewriter {
        String rewrite(String str);
    }

    String get(GetRequest getRequest) throws IOException, HttpException;
}
