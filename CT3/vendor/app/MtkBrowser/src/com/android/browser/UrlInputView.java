package com.android.browser;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Patterns;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import com.android.browser.SuggestionsAdapter;
import com.android.browser.search.SearchEngines;
import com.mediatek.common.search.SearchEngine;
/* loaded from: b.zip:com/android/browser/UrlInputView.class */
public class UrlInputView extends AutoCompleteTextView implements TextView.OnEditorActionListener, SuggestionsAdapter.CompletionListener, AdapterView.OnItemClickListener, TextWatcher {
    private static final boolean DEBUG = Browser.DEBUG;
    private SuggestionsAdapter mAdapter;
    private View mContainer;
    private boolean mIncognitoMode;
    private InputMethodManager mInputManager;
    private boolean mLandscape;
    private UrlInputListener mListener;
    private boolean mNeedsUpdate;
    private int mState;
    private StateListener mStateListener;
    private UiController mUiController;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: b.zip:com/android/browser/UrlInputView$StateListener.class */
    public interface StateListener {
        void onStateChanged(int i);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: b.zip:com/android/browser/UrlInputView$UrlInputListener.class */
    public interface UrlInputListener {
        void onAction(String str, String str2, String str3);

        void onCopySuggestion(String str);

        void onDismiss();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: b.zip:com/android/browser/UrlInputView$UrlInsertActionMode.class */
    public class UrlInsertActionMode implements ActionMode.Callback {
        final UrlInputView this$0;

        public UrlInsertActionMode(UrlInputView urlInputView) {
            this.this$0 = urlInputView;
        }

        @Override // android.view.ActionMode.Callback
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            return false;
        }

        @Override // android.view.ActionMode.Callback
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            return this.this$0.mState != 0;
        }

        @Override // android.view.ActionMode.Callback
        public void onDestroyActionMode(ActionMode actionMode) {
        }

        @Override // android.view.ActionMode.Callback
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }
    }

    public UrlInputView(Context context) {
        this(context, null);
    }

    public UrlInputView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 16842859);
    }

    public UrlInputView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init(context);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void changeState(int i) {
        if (DEBUG) {
            Log.d("browser", "UrlInputView.changeState()--->newState = " + i);
        }
        this.mState = i;
        if (this.mStateListener != null) {
            this.mStateListener.onStateChanged(this.mState);
        }
    }

    private void finishInput(String str, String str2, String str3) {
        SearchEngine searchEngineInfo;
        if (DEBUG) {
            Log.d("browser", "UrlInputView.finishInput()--->url = " + str + ", extra = " + str2 + ", source = " + str3);
        }
        this.mNeedsUpdate = true;
        dismissDropDown();
        this.mInputManager.hideSoftInputFromWindow(getWindowToken(), 0);
        if (TextUtils.isEmpty(str)) {
            this.mListener.onDismiss();
            return;
        }
        String str4 = str;
        if (this.mIncognitoMode) {
            str4 = str;
            if (isSearch(str)) {
                com.android.browser.search.SearchEngine searchEngine = BrowserSettings.getInstance().getSearchEngine();
                if (searchEngine == null || (searchEngineInfo = SearchEngines.getSearchEngineInfo(this.mContext, searchEngine.getName())) == null) {
                    return;
                }
                str4 = searchEngineInfo.getSearchUriForQuery(str);
            }
        }
        this.mListener.onAction(str4, str2, str3);
    }

    private void init(Context context) {
        this.mInputManager = (InputMethodManager) context.getSystemService("input_method");
        setOnEditorActionListener(this);
        this.mAdapter = new SuggestionsAdapter(context, this);
        setAdapter(this.mAdapter);
        setSelectAllOnFocus(true);
        onConfigurationChanged(context.getResources().getConfiguration());
        setThreshold(1);
        setOnItemClickListener(this);
        this.mNeedsUpdate = false;
        addTextChangedListener(this);
        setDropDownAnchor(2131558528);
        this.mState = 0;
    }

    @Override // android.text.TextWatcher
    public void afterTextChanged(Editable editable) {
    }

    @Override // android.text.TextWatcher
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void clearNeedsUpdate() {
        this.mNeedsUpdate = false;
    }

    @Override // android.widget.AutoCompleteTextView
    public void dismissDropDown() {
        super.dismissDropDown();
        this.mAdapter.clearCache();
    }

    @Override // android.widget.AutoCompleteTextView
    public SuggestionsAdapter getAdapter() {
        return this.mAdapter;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getState() {
        return this.mState;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void hideIME() {
        this.mInputManager.hideSoftInputFromWindow(getWindowToken(), 0);
    }

    boolean isSearch(String str) {
        String trim = UrlUtils.fixUrl(str).trim();
        return (TextUtils.isEmpty(trim) || Patterns.WEB_URL.matcher(trim).matches() || UrlUtils.ACCEPTED_URI_SCHEMA.matcher(trim).matches()) ? false : true;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean needsUpdate() {
        return this.mNeedsUpdate;
    }

    @Override // android.widget.TextView, android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mLandscape = (configuration.orientation & 2) != 0;
        this.mAdapter.setLandscapeMode(this.mLandscape);
        if (isPopupShowing() && getVisibility() == 0) {
            dismissDropDown();
            showDropDown();
            performFiltering(getText(), 0);
        }
    }

    @Override // android.widget.TextView.OnEditorActionListener
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        finishInput(getText().toString(), null, "browser-type");
        return true;
    }

    @Override // android.widget.AutoCompleteTextView, android.widget.TextView, android.view.View
    protected void onFocusChanged(boolean z, int i, Rect rect) {
        int i2;
        if (DEBUG) {
            Log.d("browser", "UrlInputView.onFocusChanged()--->focused = " + z + ", direction = " + i + ", prevRect = " + rect);
        }
        super.onFocusChanged(z, i, rect);
        if (z) {
            i2 = hasSelection() ? 1 : 2;
            showIME();
        } else {
            i2 = 0;
            hideIME();
        }
        post(new Runnable(this, i2) { // from class: com.android.browser.UrlInputView.1
            final UrlInputView this$0;
            final int val$s;

            {
                this.this$0 = this;
                this.val$s = i2;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.changeState(this.val$s);
            }
        });
    }

    @Override // android.widget.AdapterView.OnItemClickListener
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
        SuggestionsAdapter.SuggestItem item = this.mAdapter.getItem(i);
        onSelect(SuggestionsAdapter.getSuggestionUrl(item), item.type, item.extra);
    }

    @Override // android.widget.AutoCompleteTextView, android.widget.TextView, android.view.View, android.view.KeyEvent.Callback
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (i != 111 || isInTouchMode()) {
            return super.onKeyDown(i, keyEvent);
        }
        finishInput(null, null, null);
        return true;
    }

    @Override // com.android.browser.SuggestionsAdapter.CompletionListener
    public void onSearch(String str) {
        if (DEBUG) {
            Log.d("browser", "UrlInputView.onSearch()--->search = " + str);
        }
        this.mListener.onCopySuggestion(str);
    }

    @Override // com.android.browser.SuggestionsAdapter.CompletionListener
    public void onSelect(String str, int i, String str2) {
        finishInput(str, str2, "browser-suggest");
    }

    @Override // android.widget.TextView, android.text.TextWatcher
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        if (DEBUG) {
            Log.d("browser", "UrlInputView.onTextChanged()--->new string : " + charSequence);
        }
        if (1 == this.mState) {
            changeState(2);
        }
    }

    @Override // android.widget.TextView, android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean hasSelection = hasSelection();
        boolean hasFocus = hasFocus();
        boolean onTouchEvent = super.onTouchEvent(motionEvent);
        if (motionEvent.getActionMasked() == 0 && hasSelection) {
            postDelayed(new Runnable(this) { // from class: com.android.browser.UrlInputView.2
                final UrlInputView this$0;

                {
                    this.this$0 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$0.changeState(2);
                }
            }, 100L);
        }
        if (!hasFocus && hasFocus()) {
            selectAll();
        }
        return onTouchEvent;
    }

    @Override // android.view.View
    public boolean requestRectangleOnScreen(Rect rect, boolean z) {
        return false;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setContainer(View view) {
        this.mContainer = view;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setController(UiController uiController) {
        this.mUiController = uiController;
        setCustomSelectionActionModeCallback(new UrlSelectionActionMode(uiController));
        setCustomInsertionActionModeCallback(new UrlInsertActionMode(this));
    }

    public void setIncognitoMode(boolean z) {
        this.mIncognitoMode = z;
        this.mAdapter.setIncognitoMode(this.mIncognitoMode);
    }

    public void setStateListener(StateListener stateListener) {
        this.mStateListener = stateListener;
        changeState(this.mState);
    }

    public void setUrlInputListener(UrlInputListener urlInputListener) {
        this.mListener = urlInputListener;
    }

    @Override // android.widget.AutoCompleteTextView
    public void showDropDown() {
        if (getVisibility() == 8) {
            return;
        }
        super.showDropDown();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void showIME() {
        if (this.mUiController == null || this.mUiController.getUi().isWebShowing()) {
            this.mInputManager.restartInput(this);
            this.mInputManager.showSoftInput(this, 0);
        }
    }
}
