package com.android.browser;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.browser.UrlInputView;
import com.mediatek.browser.ext.IBrowserUrlExt;
/* loaded from: b.zip:com/android/browser/NavigationBarBase.class */
public class NavigationBarBase extends LinearLayout implements View.OnClickListener, UrlInputView.UrlInputListener, View.OnFocusChangeListener, TextWatcher {
    protected BaseUi mBaseUi;
    private IBrowserUrlExt mBrowserUrlExt;
    private ImageView mFavicon;
    protected boolean mInVoiceMode;
    private ImageView mLockIcon;
    protected TitleBar mTitleBar;
    protected UiController mUiController;
    protected UrlInputView mUrlInput;

    public NavigationBarBase(Context context) {
        super(context);
        this.mInVoiceMode = false;
        this.mBrowserUrlExt = null;
    }

    public NavigationBarBase(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mInVoiceMode = false;
        this.mBrowserUrlExt = null;
    }

    public NavigationBarBase(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mInVoiceMode = false;
        this.mBrowserUrlExt = null;
    }

    @Override // android.text.TextWatcher
    public void afterTextChanged(Editable editable) {
    }

    @Override // android.text.TextWatcher
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void clearCompletions() {
        this.mUrlInput.dismissDropDown();
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchKeyEventPreIme(KeyEvent keyEvent) {
        if (keyEvent.getKeyCode() == 4) {
            stopEditingUrl();
            return true;
        }
        return super.dispatchKeyEventPreIme(keyEvent);
    }

    public UrlInputView getUrlInputView() {
        return this.mUrlInput;
    }

    public boolean isEditingUrl() {
        return this.mUrlInput.hasFocus();
    }

    public boolean isMenuShowing() {
        return false;
    }

    @Override // com.android.browser.UrlInputView.UrlInputListener
    public void onAction(String str, String str2, String str3) {
        stopEditingUrl();
        if ("browser-type".equals(str3)) {
            String smartUrlFilter = UrlUtils.smartUrlFilter(str, false);
            Tab activeTab = this.mBaseUi.getActiveTab();
            if (smartUrlFilter != null && activeTab != null && smartUrlFilter.startsWith("javascript:")) {
                this.mUiController.loadUrl(activeTab, smartUrlFilter);
                setDisplayTitle(str);
                return;
            }
        }
        Intent intent = new Intent();
        if (str != null && str.startsWith("content://")) {
            intent.setAction("android.intent.action.VIEW");
            intent.setData(Uri.parse(str));
        } else if (str != null && str.startsWith("rtsp://")) {
            intent.setAction("android.intent.action.VIEW");
            intent.setData(Uri.parse(str.replaceAll(" ", "%20")));
            intent.addFlags(268435456);
        } else if (str != null && str.startsWith("wtai://wp/mc;")) {
            intent.setAction("android.intent.action.VIEW");
            intent.setData(Uri.parse("tel:" + str.substring("wtai://wp/mc;".length())));
        } else if (str != null && str.startsWith("file://")) {
            return;
        } else {
            intent.setAction("android.intent.action.SEARCH");
            intent.putExtra("query", str);
            if (str2 != null) {
                intent.putExtra("intent_extra_data_key", str2);
            }
            if (str3 != null) {
                Bundle bundle = new Bundle();
                bundle.putString("source", str3);
                intent.putExtra("app_data", bundle);
            }
        }
        this.mUiController.handleNewIntent(intent);
        setDisplayTitle(str);
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
    }

    @Override // com.android.browser.UrlInputView.UrlInputListener
    public void onCopySuggestion(String str) {
        this.mUrlInput.setText((CharSequence) str, true);
        if (str != null) {
            this.mUrlInput.setSelection(str.length());
        }
    }

    @Override // com.android.browser.UrlInputView.UrlInputListener
    public void onDismiss() {
        Tab activeTab = this.mBaseUi.getActiveTab();
        this.mBaseUi.hideTitleBar();
        post(new Runnable(this, activeTab) { // from class: com.android.browser.NavigationBarBase.1
            final NavigationBarBase this$0;
            final Tab val$currentTab;

            {
                this.this$0 = this;
                this.val$currentTab = activeTab;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.clearFocus();
                if (this.val$currentTab != null) {
                    this.this$0.setDisplayTitle(this.val$currentTab.getUrl());
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mLockIcon = (ImageView) findViewById(2131558527);
        this.mFavicon = (ImageView) findViewById(2131558406);
        this.mUrlInput = (UrlInputView) findViewById(2131558408);
        this.mUrlInput.setUrlInputListener(this);
        this.mUrlInput.setOnFocusChangeListener(this);
        this.mUrlInput.setSelectAllOnFocus(true);
        this.mUrlInput.addTextChangedListener(this);
        this.mBrowserUrlExt = Extensions.getUrlPlugin(this.mContext);
        InputFilter[] checkUrlLengthLimit = this.mBrowserUrlExt.checkUrlLengthLimit(this.mContext);
        if (checkUrlLengthLimit != null) {
            this.mUrlInput.setFilters(checkUrlLengthLimit);
        }
    }

    @Override // android.view.View.OnFocusChangeListener
    public void onFocusChange(View view, boolean z) {
        Tab currentTab;
        if (z || view.isInTouchMode() || this.mUrlInput.needsUpdate()) {
            setFocusState(z);
        }
        if (z) {
            this.mBaseUi.showTitleBar();
        } else if (!this.mUrlInput.needsUpdate()) {
            this.mUrlInput.dismissDropDown();
            this.mUrlInput.hideIME();
            if (this.mUrlInput.getText().length() == 0 && (currentTab = this.mUiController.getTabControl().getCurrentTab()) != null) {
                setDisplayTitle(currentTab.getUrl());
            }
            this.mBaseUi.suggestHideTitleBar();
        }
        this.mUrlInput.clearNeedsUpdate();
    }

    public void onProgressStarted() {
    }

    public void onProgressStopped() {
    }

    public void onTabDataChanged(Tab tab) {
    }

    @Override // android.text.TextWatcher
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
    }

    public void onVoiceResult(String str) {
        startEditingUrl(true, true);
        onCopySuggestion(str);
    }

    public void setCurrentUrlIsBookmark(boolean z) {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setDisplayTitle(String str) {
        if (isEditingUrl()) {
            return;
        }
        if (str.startsWith("about:blank")) {
            this.mUrlInput.setText((CharSequence) "about:blank", false);
        } else {
            this.mUrlInput.setText((CharSequence) str, false);
        }
    }

    public void setFavicon(Bitmap bitmap) {
        if (this.mFavicon == null) {
            return;
        }
        this.mFavicon.setImageDrawable(this.mBaseUi.getFaviconDrawable(bitmap));
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setFocusState(boolean z) {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setIncognitoMode(boolean z) {
        this.mUrlInput.setIncognitoMode(z);
    }

    public void setLock(Drawable drawable) {
        if (this.mLockIcon == null) {
            return;
        }
        if (drawable == null) {
            this.mLockIcon.setVisibility(8);
            return;
        }
        this.mLockIcon.setImageDrawable(drawable);
        this.mLockIcon.setVisibility(0);
    }

    public void setTitleBar(TitleBar titleBar) {
        this.mTitleBar = titleBar;
        this.mBaseUi = this.mTitleBar.getUi();
        this.mUiController = this.mTitleBar.getUiController();
        this.mUrlInput.setController(this.mUiController);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void startEditingUrl(boolean z, boolean z2) {
        setVisibility(0);
        if (this.mTitleBar.useQuickControls()) {
            this.mTitleBar.getProgressView().setVisibility(8);
        }
        if (!this.mUrlInput.hasFocus()) {
            this.mUrlInput.requestFocus();
        }
        if (z) {
            this.mUrlInput.setText("");
        }
        if (z2) {
            this.mUrlInput.showIME();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void stopEditingUrl() {
        WebView currentTopWebView = this.mUiController.getCurrentTopWebView();
        if (currentTopWebView != null) {
            currentTopWebView.requestFocus();
        }
    }
}
