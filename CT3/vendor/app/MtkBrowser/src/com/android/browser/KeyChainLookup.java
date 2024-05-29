package com.android.browser;

import android.content.Context;
import android.os.AsyncTask;
import android.security.KeyChain;
import android.security.KeyChainException;
import android.webkit.ClientCertRequest;
/* loaded from: b.zip:com/android/browser/KeyChainLookup.class */
final class KeyChainLookup extends AsyncTask<Void, Void, Void> {
    private final String mAlias;
    private final Context mContext;
    private final ClientCertRequest mHandler;

    /* JADX INFO: Access modifiers changed from: package-private */
    public KeyChainLookup(Context context, ClientCertRequest clientCertRequest, String str) {
        this.mContext = context.getApplicationContext();
        this.mHandler = clientCertRequest;
        this.mAlias = str;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    public Void doInBackground(Void... voidArr) {
        try {
            this.mHandler.proceed(KeyChain.getPrivateKey(this.mContext, this.mAlias), KeyChain.getCertificateChain(this.mContext, this.mAlias));
            return null;
        } catch (KeyChainException e) {
            this.mHandler.ignore();
            return null;
        } catch (InterruptedException e2) {
            this.mHandler.ignore();
            return null;
        }
    }
}
