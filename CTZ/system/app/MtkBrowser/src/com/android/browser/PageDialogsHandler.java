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
/* loaded from: classes.dex */
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

    /* JADX INFO: Access modifiers changed from: package-private */
    public void showHttpAuthentication(final Tab tab, final HttpAuthHandler httpAuthHandler, String str, String str2) {
        this.mHttpAuthenticationDialog = new HttpAuthenticationDialog(this.mContext, str, str2);
        this.mHttpAuthenticationHandler = httpAuthHandler;
        this.mHttpAuthenticationDialog.setOkListener(new HttpAuthenticationDialog.OkListener() { // from class: com.android.browser.PageDialogsHandler.1
            @Override // com.android.browser.HttpAuthenticationDialog.OkListener
            public void onOk(String str3, String str4, String str5, String str6) {
                PageDialogsHandler.this.setHttpAuthUsernamePassword(str3, str4, str5, str6);
                httpAuthHandler.proceed(str5, str6);
                PageDialogsHandler.this.mHttpAuthenticationDialog = null;
            }
        });
        this.mHttpAuthenticationDialog.setCancelListener(new HttpAuthenticationDialog.CancelListener() { // from class: com.android.browser.PageDialogsHandler.2
            @Override // com.android.browser.HttpAuthenticationDialog.CancelListener
            public void onCancel() {
                httpAuthHandler.cancel();
                PageDialogsHandler.this.mController.onUpdatedSecurityState(tab);
                PageDialogsHandler.this.mHttpAuthenticationDialog = null;
            }
        });
        this.mHttpAuthenticationDialog.show();
    }

    public void setHttpAuthUsernamePassword(String str, String str2, String str3, String str4) {
        WebView currentTopWebView = this.mController.getCurrentTopWebView();
        if (currentTopWebView != null) {
            currentTopWebView.setHttpAuthUsernamePassword(str, str2, str3, str4);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void showPageInfo(final Tab tab, final boolean z, String str) {
        String url;
        if (tab == null) {
            return;
        }
        View inflate = LayoutInflater.from(this.mContext).inflate(R.layout.page_info, (ViewGroup) null);
        WebView webView = tab.getWebView();
        if (!z) {
            url = tab.getUrl();
        } else {
            url = str;
        }
        String title = tab.getTitle();
        if (url == null) {
            url = "";
        }
        if (title == null) {
            title = "";
        }
        ((TextView) inflate.findViewById(R.id.address)).setText(url);
        ((TextView) inflate.findViewById(R.id.title)).setText(title);
        this.mPageInfoView = tab;
        this.mPageInfoFromShowSSLCertificateOnError = z;
        this.mUrlCertificateOnError = str;
        AlertDialog.Builder onCancelListener = new AlertDialog.Builder(this.mContext).setTitle(R.string.page_info).setIcon(17301659).setView(inflate).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() { // from class: com.android.browser.PageDialogsHandler.4
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                PageDialogsHandler.this.mPageInfoDialog = null;
                PageDialogsHandler.this.mPageInfoView = null;
                if (z) {
                    PageDialogsHandler.this.showSSLCertificateOnError(PageDialogsHandler.this.mSSLCertificateOnErrorView, PageDialogsHandler.this.mSSLCertificateOnErrorHandler, PageDialogsHandler.this.mSSLCertificateOnErrorError);
                }
            }
        }).setOnCancelListener(new DialogInterface.OnCancelListener() { // from class: com.android.browser.PageDialogsHandler.3
            @Override // android.content.DialogInterface.OnCancelListener
            public void onCancel(DialogInterface dialogInterface) {
                PageDialogsHandler.this.mPageInfoDialog = null;
                PageDialogsHandler.this.mPageInfoView = null;
                if (z) {
                    PageDialogsHandler.this.showSSLCertificateOnError(PageDialogsHandler.this.mSSLCertificateOnErrorView, PageDialogsHandler.this.mSSLCertificateOnErrorHandler, PageDialogsHandler.this.mSSLCertificateOnErrorError);
                }
            }
        });
        if (z || (webView != null && webView.getCertificate() != null)) {
            onCancelListener.setNeutralButton(R.string.view_certificate, new DialogInterface.OnClickListener() { // from class: com.android.browser.PageDialogsHandler.5
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    PageDialogsHandler.this.mPageInfoDialog = null;
                    PageDialogsHandler.this.mPageInfoView = null;
                    if (!z) {
                        PageDialogsHandler.this.showSSLCertificate(tab);
                    } else {
                        PageDialogsHandler.this.showSSLCertificateOnError(PageDialogsHandler.this.mSSLCertificateOnErrorView, PageDialogsHandler.this.mSSLCertificateOnErrorHandler, PageDialogsHandler.this.mSSLCertificateOnErrorError);
                    }
                }
            });
        }
        this.mPageInfoDialog = onCancelListener.show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showSSLCertificate(final Tab tab) {
        SslCertificate certificate = tab.getWebView().getCertificate();
        if (certificate == null) {
            return;
        }
        this.mSSLCertificateView = tab;
        this.mSSLCertificateDialog = createSslCertificateDialog(certificate, tab.getSslCertificateError()).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() { // from class: com.android.browser.PageDialogsHandler.7
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                PageDialogsHandler.this.mSSLCertificateDialog = null;
                PageDialogsHandler.this.mSSLCertificateView = null;
                PageDialogsHandler.this.showPageInfo(tab, false, null);
            }
        }).setOnCancelListener(new DialogInterface.OnCancelListener() { // from class: com.android.browser.PageDialogsHandler.6
            @Override // android.content.DialogInterface.OnCancelListener
            public void onCancel(DialogInterface dialogInterface) {
                PageDialogsHandler.this.mSSLCertificateDialog = null;
                PageDialogsHandler.this.mSSLCertificateView = null;
                PageDialogsHandler.this.showPageInfo(tab, false, null);
            }
        }).show();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void showSSLCertificateOnError(final WebView webView, final SslErrorHandler sslErrorHandler, final SslError sslError) {
        SslCertificate certificate;
        if (sslError == null || (certificate = sslError.getCertificate()) == null) {
            return;
        }
        this.mSSLCertificateOnErrorHandler = sslErrorHandler;
        this.mSSLCertificateOnErrorView = webView;
        this.mSSLCertificateOnErrorError = sslError;
        this.mSSLCertificateOnErrorDialog = createSslCertificateDialog(certificate, sslError).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() { // from class: com.android.browser.PageDialogsHandler.10
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                PageDialogsHandler.this.mSSLCertificateOnErrorDialog = null;
                PageDialogsHandler.this.mSSLCertificateOnErrorView = null;
                PageDialogsHandler.this.mSSLCertificateOnErrorHandler = null;
                PageDialogsHandler.this.mSSLCertificateOnErrorError = null;
                ((BrowserWebView) webView).getWebViewClient().onReceivedSslError(webView, sslErrorHandler, sslError);
            }
        }).setNeutralButton(R.string.page_info_view, new DialogInterface.OnClickListener() { // from class: com.android.browser.PageDialogsHandler.9
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                PageDialogsHandler.this.mSSLCertificateOnErrorDialog = null;
                PageDialogsHandler.this.showPageInfo(PageDialogsHandler.this.mController.getTabControl().getTabFromView(webView), true, sslError.getUrl());
            }
        }).setOnCancelListener(new DialogInterface.OnCancelListener() { // from class: com.android.browser.PageDialogsHandler.8
            @Override // android.content.DialogInterface.OnCancelListener
            public void onCancel(DialogInterface dialogInterface) {
                PageDialogsHandler.this.mSSLCertificateOnErrorDialog = null;
                PageDialogsHandler.this.mSSLCertificateOnErrorView = null;
                PageDialogsHandler.this.mSSLCertificateOnErrorHandler = null;
                PageDialogsHandler.this.mSSLCertificateOnErrorError = null;
                ((BrowserWebView) webView).getWebViewClient().onReceivedSslError(webView, sslErrorHandler, sslError);
            }
        }).show();
    }

    private AlertDialog.Builder createSslCertificateDialog(SslCertificate sslCertificate, SslError sslError) {
        int i;
        View inflateCertificateView = sslCertificate.inflateCertificateView(this.mContext);
        LinearLayout linearLayout = (LinearLayout) inflateCertificateView.findViewById(16909187);
        LayoutInflater from = LayoutInflater.from(this.mContext);
        if (sslError == null) {
            i = R.drawable.ic_dialog_browser_certificate_secure;
            ((TextView) ((LinearLayout) from.inflate(R.layout.ssl_success, linearLayout)).findViewById(R.id.success)).setText(17040907);
        } else {
            if (sslError.hasError(3)) {
                addError(from, linearLayout, R.string.ssl_untrusted);
            }
            if (sslError.hasError(2)) {
                addError(from, linearLayout, R.string.ssl_mismatch);
            }
            if (sslError.hasError(1)) {
                addError(from, linearLayout, R.string.ssl_expired);
            }
            if (sslError.hasError(0)) {
                addError(from, linearLayout, R.string.ssl_not_yet_valid);
            }
            if (sslError.hasError(4)) {
                addError(from, linearLayout, R.string.ssl_date_invalid);
            }
            if (sslError.hasError(5)) {
                addError(from, linearLayout, R.string.ssl_invalid);
            }
            if (linearLayout.getChildCount() == 0) {
                addError(from, linearLayout, R.string.ssl_unknown);
            }
            i = R.drawable.ic_dialog_browser_certificate_partially_secure;
        }
        return new AlertDialog.Builder(this.mContext).setTitle(17040906).setIcon(i).setView(inflateCertificateView);
    }

    private void addError(LayoutInflater layoutInflater, LinearLayout linearLayout, int i) {
        TextView textView = (TextView) layoutInflater.inflate(R.layout.ssl_warning, (ViewGroup) linearLayout, false);
        textView.setText(i);
        linearLayout.addView(textView);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void showPopupWindowAttempt(final Tab tab, final boolean z, final Message message) {
        this.mPopupWindowAttemptView = tab;
        this.mPopupWindowAttemptIsDialog = z;
        this.mPopupWindowAttemptMessage = message;
        DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() { // from class: com.android.browser.PageDialogsHandler.11
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                PageDialogsHandler.this.mPopupWindowAttemptDialog = null;
                PageDialogsHandler.this.mPopupWindowAttemptView = null;
                PageDialogsHandler.this.mPopupWindowAttemptIsDialog = false;
                PageDialogsHandler.this.mPopupWindowAttemptMessage = null;
                WebView.WebViewTransport webViewTransport = (WebView.WebViewTransport) message.obj;
                if (z) {
                    tab.createSubWindow();
                    PageDialogsHandler.this.mController.attachSubWindow(tab);
                    webViewTransport.setWebView(tab.getSubWebView());
                    tab.PopupWindowShown(false);
                } else {
                    webViewTransport.setWebView(PageDialogsHandler.this.mController.openTab((String) null, tab, true, true).getWebView());
                }
                message.sendToTarget();
            }
        };
        this.mPopupWindowAttemptDialog = new AlertDialog.Builder(this.mContext).setIconAttribute(16843605).setMessage(R.string.popup_window_attempt).setPositiveButton(R.string.allow, onClickListener).setNegativeButton(R.string.block, new DialogInterface.OnClickListener() { // from class: com.android.browser.PageDialogsHandler.12
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                PageDialogsHandler.this.mPopupWindowAttemptDialog = null;
                PageDialogsHandler.this.mPopupWindowAttemptView = null;
                PageDialogsHandler.this.mPopupWindowAttemptIsDialog = false;
                PageDialogsHandler.this.mPopupWindowAttemptMessage = null;
                message.sendToTarget();
                tab.PopupWindowShown(false);
            }
        }).setCancelable(false).create();
        this.mPopupWindowAttemptDialog.show();
        if (z) {
            tab.PopupWindowShown(true);
        }
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
}
