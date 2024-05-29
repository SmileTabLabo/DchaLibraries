package com.android.browser;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.net.http.SslCertificate;
import android.net.http.SslError;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.browser.HttpAuthenticationDialog;
/* loaded from: b.zip:com/android/browser/PageDialogsHandler.class */
public class PageDialogsHandler {
    private Context mContext;
    private Controller mController;
    private HttpAuthenticationDialog mHttpAuthenticationDialog;
    private HttpAuthHandler mHttpAuthenticationHandler;
    private AlertDialog mPageInfoDialog;
    private boolean mPageInfoFromShowSSLCertificateOnError;
    private Tab mPageInfoView;
    private AlertDialog mPopupWindowAttemptDialog;
    private boolean mPopupWindowAttemptIsDialog;
    private Message mPopupWindowAttemptMessage;
    private Tab mPopupWindowAttemptView;
    private AlertDialog mSSLCertificateDialog;
    private AlertDialog mSSLCertificateOnErrorDialog;
    private SslError mSSLCertificateOnErrorError;
    private SslErrorHandler mSSLCertificateOnErrorHandler;
    private WebView mSSLCertificateOnErrorView;
    private Tab mSSLCertificateView;
    private String mUrlCertificateOnError;

    public PageDialogsHandler(Context context, Controller controller) {
        this.mContext = context;
        this.mController = controller;
    }

    private void addError(LayoutInflater layoutInflater, LinearLayout linearLayout, int i) {
        TextView textView = (TextView) layoutInflater.inflate(2130968624, (ViewGroup) linearLayout, false);
        textView.setText(i);
        linearLayout.addView(textView);
    }

    private AlertDialog.Builder createSslCertificateDialog(SslCertificate sslCertificate, SslError sslError) {
        int i;
        View inflateCertificateView = sslCertificate.inflateCertificateView(this.mContext);
        LinearLayout linearLayout = (LinearLayout) inflateCertificateView.findViewById(16909320);
        LayoutInflater from = LayoutInflater.from(this.mContext);
        if (sslError == null) {
            i = 2130837547;
            ((TextView) ((LinearLayout) from.inflate(2130968623, linearLayout)).findViewById(2131558517)).setText(17040595);
        } else {
            if (sslError.hasError(3)) {
                addError(from, linearLayout, 2131492973);
            }
            if (sslError.hasError(2)) {
                addError(from, linearLayout, 2131492974);
            }
            if (sslError.hasError(1)) {
                addError(from, linearLayout, 2131492975);
            }
            if (sslError.hasError(0)) {
                addError(from, linearLayout, 2131492976);
            }
            if (sslError.hasError(4)) {
                addError(from, linearLayout, 2131492977);
            }
            if (sslError.hasError(5)) {
                addError(from, linearLayout, 2131492978);
            }
            i = 2130837546;
            if (linearLayout.getChildCount() == 0) {
                addError(from, linearLayout, 2131492979);
                i = 2130837546;
            }
        }
        return new AlertDialog.Builder(this.mContext).setTitle(17040594).setIcon(i).setView(inflateCertificateView);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showSSLCertificate(Tab tab) {
        SslCertificate certificate = tab.getWebView().getCertificate();
        if (certificate == null) {
            return;
        }
        this.mSSLCertificateView = tab;
        this.mSSLCertificateDialog = createSslCertificateDialog(certificate, tab.getSslCertificateError()).setPositiveButton(2131492963, new DialogInterface.OnClickListener(this, tab) { // from class: com.android.browser.PageDialogsHandler.6
            final PageDialogsHandler this$0;
            final Tab val$tab;

            {
                this.this$0 = this;
                this.val$tab = tab;
            }

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                this.this$0.mSSLCertificateDialog = null;
                this.this$0.mSSLCertificateView = null;
                this.this$0.showPageInfo(this.val$tab, false, null);
            }
        }).setOnCancelListener(new DialogInterface.OnCancelListener(this, tab) { // from class: com.android.browser.PageDialogsHandler.7
            final PageDialogsHandler this$0;
            final Tab val$tab;

            {
                this.this$0 = this;
                this.val$tab = tab;
            }

            @Override // android.content.DialogInterface.OnCancelListener
            public void onCancel(DialogInterface dialogInterface) {
                this.this$0.mSSLCertificateDialog = null;
                this.this$0.mSSLCertificateView = null;
                this.this$0.showPageInfo(this.val$tab, false, null);
            }
        }).show();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void destroyDialogs() {
        if (this.mPageInfoDialog != null) {
            this.mPageInfoDialog.dismiss();
            this.mPageInfoDialog = null;
            this.mPageInfoView = null;
        }
        if (this.mSSLCertificateDialog != null) {
            this.mSSLCertificateDialog.dismiss();
            this.mSSLCertificateDialog = null;
            this.mSSLCertificateView = null;
        }
        if (this.mSSLCertificateOnErrorDialog != null) {
            this.mSSLCertificateOnErrorDialog.dismiss();
            ((BrowserWebView) this.mSSLCertificateOnErrorView).getWebViewClient().onReceivedSslError(this.mSSLCertificateOnErrorView, this.mSSLCertificateOnErrorHandler, this.mSSLCertificateOnErrorError);
            this.mSSLCertificateOnErrorDialog = null;
            this.mSSLCertificateOnErrorView = null;
            this.mSSLCertificateOnErrorHandler = null;
            this.mSSLCertificateOnErrorError = null;
        }
        if (this.mHttpAuthenticationDialog != null) {
            this.mHttpAuthenticationHandler.cancel();
            this.mHttpAuthenticationDialog = null;
            this.mHttpAuthenticationHandler = null;
        }
        if (this.mPopupWindowAttemptDialog != null) {
            this.mPopupWindowAttemptDialog.dismiss();
            this.mPopupWindowAttemptDialog = null;
            this.mPopupWindowAttemptView = null;
            this.mPopupWindowAttemptIsDialog = false;
            this.mPopupWindowAttemptMessage = null;
        }
    }

    public void onConfigurationChanged(Configuration configuration) {
        if (this.mPageInfoDialog != null) {
            this.mPageInfoDialog.dismiss();
            showPageInfo(this.mPageInfoView, this.mPageInfoFromShowSSLCertificateOnError, this.mUrlCertificateOnError);
        }
        if (this.mSSLCertificateDialog != null) {
            this.mSSLCertificateDialog.dismiss();
            showSSLCertificate(this.mSSLCertificateView);
        }
        if (this.mSSLCertificateOnErrorDialog != null) {
            this.mSSLCertificateOnErrorDialog.dismiss();
            showSSLCertificateOnError(this.mSSLCertificateOnErrorView, this.mSSLCertificateOnErrorHandler, this.mSSLCertificateOnErrorError);
        }
        if (this.mHttpAuthenticationDialog != null) {
            this.mHttpAuthenticationDialog.reshow();
        }
        if (this.mPopupWindowAttemptDialog != null) {
            this.mPopupWindowAttemptDialog.dismiss();
            showPopupWindowAttempt(this.mPopupWindowAttemptView, this.mPopupWindowAttemptIsDialog, this.mPopupWindowAttemptMessage);
        }
    }

    public void setHttpAuthUsernamePassword(String str, String str2, String str3, String str4) {
        WebView currentTopWebView = this.mController.getCurrentTopWebView();
        if (currentTopWebView != null) {
            currentTopWebView.setHttpAuthUsernamePassword(str, str2, str3, str4);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void showHttpAuthentication(Tab tab, HttpAuthHandler httpAuthHandler, String str, String str2) {
        this.mHttpAuthenticationDialog = new HttpAuthenticationDialog(this.mContext, str, str2);
        this.mHttpAuthenticationHandler = httpAuthHandler;
        this.mHttpAuthenticationDialog.setOkListener(new HttpAuthenticationDialog.OkListener(this, httpAuthHandler) { // from class: com.android.browser.PageDialogsHandler.1
            final PageDialogsHandler this$0;
            final HttpAuthHandler val$handler;

            {
                this.this$0 = this;
                this.val$handler = httpAuthHandler;
            }

            @Override // com.android.browser.HttpAuthenticationDialog.OkListener
            public void onOk(String str3, String str4, String str5, String str6) {
                this.this$0.setHttpAuthUsernamePassword(str3, str4, str5, str6);
                this.val$handler.proceed(str5, str6);
                this.this$0.mHttpAuthenticationDialog = null;
            }
        });
        this.mHttpAuthenticationDialog.setCancelListener(new HttpAuthenticationDialog.CancelListener(this, httpAuthHandler, tab) { // from class: com.android.browser.PageDialogsHandler.2
            final PageDialogsHandler this$0;
            final HttpAuthHandler val$handler;
            final Tab val$tab;

            {
                this.this$0 = this;
                this.val$handler = httpAuthHandler;
                this.val$tab = tab;
            }

            @Override // com.android.browser.HttpAuthenticationDialog.CancelListener
            public void onCancel() {
                this.val$handler.cancel();
                this.this$0.mController.onUpdatedSecurityState(this.val$tab);
                this.this$0.mHttpAuthenticationDialog = null;
            }
        });
        this.mHttpAuthenticationDialog.show();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void showPageInfo(Tab tab, boolean z, String str) {
        if (tab == null) {
            return;
        }
        View inflate = LayoutInflater.from(this.mContext).inflate(2130968611, (ViewGroup) null);
        WebView webView = tab.getWebView();
        String url = z ? str : tab.getUrl();
        String title = tab.getTitle();
        String str2 = url;
        if (url == null) {
            str2 = "";
        }
        String str3 = title;
        if (title == null) {
            str3 = "";
        }
        ((TextView) inflate.findViewById(2131558456)).setText(str2);
        ((TextView) inflate.findViewById(2131558407)).setText(str3);
        this.mPageInfoView = tab;
        this.mPageInfoFromShowSSLCertificateOnError = z;
        this.mUrlCertificateOnError = str;
        AlertDialog.Builder onCancelListener = new AlertDialog.Builder(this.mContext).setTitle(2131492965).setIcon(17301659).setView(inflate).setPositiveButton(2131492963, new DialogInterface.OnClickListener(this, z) { // from class: com.android.browser.PageDialogsHandler.3
            final PageDialogsHandler this$0;
            final boolean val$fromShowSSLCertificateOnError;

            {
                this.this$0 = this;
                this.val$fromShowSSLCertificateOnError = z;
            }

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                this.this$0.mPageInfoDialog = null;
                this.this$0.mPageInfoView = null;
                if (this.val$fromShowSSLCertificateOnError) {
                    this.this$0.showSSLCertificateOnError(this.this$0.mSSLCertificateOnErrorView, this.this$0.mSSLCertificateOnErrorHandler, this.this$0.mSSLCertificateOnErrorError);
                }
            }
        }).setOnCancelListener(new DialogInterface.OnCancelListener(this, z) { // from class: com.android.browser.PageDialogsHandler.4
            final PageDialogsHandler this$0;
            final boolean val$fromShowSSLCertificateOnError;

            {
                this.this$0 = this;
                this.val$fromShowSSLCertificateOnError = z;
            }

            @Override // android.content.DialogInterface.OnCancelListener
            public void onCancel(DialogInterface dialogInterface) {
                this.this$0.mPageInfoDialog = null;
                this.this$0.mPageInfoView = null;
                if (this.val$fromShowSSLCertificateOnError) {
                    this.this$0.showSSLCertificateOnError(this.this$0.mSSLCertificateOnErrorView, this.this$0.mSSLCertificateOnErrorHandler, this.this$0.mSSLCertificateOnErrorError);
                }
            }
        });
        if (z || (webView != null && webView.getCertificate() != null)) {
            onCancelListener.setNeutralButton(2131492971, new DialogInterface.OnClickListener(this, z, tab) { // from class: com.android.browser.PageDialogsHandler.5
                final PageDialogsHandler this$0;
                final boolean val$fromShowSSLCertificateOnError;
                final Tab val$tab;

                {
                    this.this$0 = this;
                    this.val$fromShowSSLCertificateOnError = z;
                    this.val$tab = tab;
                }

                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    this.this$0.mPageInfoDialog = null;
                    this.this$0.mPageInfoView = null;
                    if (this.val$fromShowSSLCertificateOnError) {
                        this.this$0.showSSLCertificateOnError(this.this$0.mSSLCertificateOnErrorView, this.this$0.mSSLCertificateOnErrorHandler, this.this$0.mSSLCertificateOnErrorError);
                    } else {
                        this.this$0.showSSLCertificate(this.val$tab);
                    }
                }
            });
        }
        this.mPageInfoDialog = onCancelListener.show();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void showPopupWindowAttempt(Tab tab, boolean z, Message message) {
        this.mPopupWindowAttemptView = tab;
        this.mPopupWindowAttemptIsDialog = z;
        this.mPopupWindowAttemptMessage = message;
        DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener(this, message, z, tab) { // from class: com.android.browser.PageDialogsHandler.11
            final PageDialogsHandler this$0;
            final boolean val$dialog;
            final Message val$resultMsg;
            final Tab val$tab;

            {
                this.this$0 = this;
                this.val$resultMsg = message;
                this.val$dialog = z;
                this.val$tab = tab;
            }

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                this.this$0.mPopupWindowAttemptDialog = null;
                this.this$0.mPopupWindowAttemptView = null;
                this.this$0.mPopupWindowAttemptIsDialog = false;
                this.this$0.mPopupWindowAttemptMessage = null;
                WebView.WebViewTransport webViewTransport = (WebView.WebViewTransport) this.val$resultMsg.obj;
                if (this.val$dialog) {
                    this.val$tab.createSubWindow();
                    this.this$0.mController.attachSubWindow(this.val$tab);
                    webViewTransport.setWebView(this.val$tab.getSubWebView());
                    this.val$tab.PopupWindowShown(false);
                } else {
                    webViewTransport.setWebView(this.this$0.mController.openTab((String) null, this.val$tab, true, true).getWebView());
                }
                this.val$resultMsg.sendToTarget();
            }
        };
        this.mPopupWindowAttemptDialog = new AlertDialog.Builder(this.mContext).setIconAttribute(16843605).setMessage(2131493210).setPositiveButton(2131493211, onClickListener).setNegativeButton(2131493212, new DialogInterface.OnClickListener(this, message, tab) { // from class: com.android.browser.PageDialogsHandler.12
            final PageDialogsHandler this$0;
            final Message val$resultMsg;
            final Tab val$tab;

            {
                this.this$0 = this;
                this.val$resultMsg = message;
                this.val$tab = tab;
            }

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                this.this$0.mPopupWindowAttemptDialog = null;
                this.this$0.mPopupWindowAttemptView = null;
                this.this$0.mPopupWindowAttemptIsDialog = false;
                this.this$0.mPopupWindowAttemptMessage = null;
                this.val$resultMsg.sendToTarget();
                this.val$tab.PopupWindowShown(false);
            }
        }).setCancelable(false).create();
        this.mPopupWindowAttemptDialog.show();
        if (z) {
            tab.PopupWindowShown(true);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void showSSLCertificateOnError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
        SslCertificate certificate;
        if (sslError == null || (certificate = sslError.getCertificate()) == null) {
            return;
        }
        this.mSSLCertificateOnErrorHandler = sslErrorHandler;
        this.mSSLCertificateOnErrorView = webView;
        this.mSSLCertificateOnErrorError = sslError;
        this.mSSLCertificateOnErrorDialog = createSslCertificateDialog(certificate, sslError).setPositiveButton(2131492963, new DialogInterface.OnClickListener(this, webView, sslErrorHandler, sslError) { // from class: com.android.browser.PageDialogsHandler.8
            final PageDialogsHandler this$0;
            final SslError val$error;
            final SslErrorHandler val$handler;
            final WebView val$view;

            {
                this.this$0 = this;
                this.val$view = webView;
                this.val$handler = sslErrorHandler;
                this.val$error = sslError;
            }

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                this.this$0.mSSLCertificateOnErrorDialog = null;
                this.this$0.mSSLCertificateOnErrorView = null;
                this.this$0.mSSLCertificateOnErrorHandler = null;
                this.this$0.mSSLCertificateOnErrorError = null;
                ((BrowserWebView) this.val$view).getWebViewClient().onReceivedSslError(this.val$view, this.val$handler, this.val$error);
            }
        }).setNeutralButton(2131492966, new DialogInterface.OnClickListener(this, webView, sslError) { // from class: com.android.browser.PageDialogsHandler.9
            final PageDialogsHandler this$0;
            final SslError val$error;
            final WebView val$view;

            {
                this.this$0 = this;
                this.val$view = webView;
                this.val$error = sslError;
            }

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                this.this$0.mSSLCertificateOnErrorDialog = null;
                this.this$0.showPageInfo(this.this$0.mController.getTabControl().getTabFromView(this.val$view), true, this.val$error.getUrl());
            }
        }).setOnCancelListener(new DialogInterface.OnCancelListener(this, webView, sslErrorHandler, sslError) { // from class: com.android.browser.PageDialogsHandler.10
            final PageDialogsHandler this$0;
            final SslError val$error;
            final SslErrorHandler val$handler;
            final WebView val$view;

            {
                this.this$0 = this;
                this.val$view = webView;
                this.val$handler = sslErrorHandler;
                this.val$error = sslError;
            }

            @Override // android.content.DialogInterface.OnCancelListener
            public void onCancel(DialogInterface dialogInterface) {
                this.this$0.mSSLCertificateOnErrorDialog = null;
                this.this$0.mSSLCertificateOnErrorView = null;
                this.this$0.mSSLCertificateOnErrorHandler = null;
                this.this$0.mSSLCertificateOnErrorError = null;
                ((BrowserWebView) this.val$view).getWebViewClient().onReceivedSslError(this.val$view, this.val$handler, this.val$error);
            }
        }).show();
    }
}
