package com.android.launcher3.allapps;

import android.content.Intent;
import android.net.Uri;
import android.os.BenesseExtension;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import com.android.launcher3.ExtendedEditText;
import com.android.launcher3.Launcher;
import com.android.launcher3.Utilities;
import com.android.launcher3.util.ComponentKey;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/launcher3/allapps/AllAppsSearchBarController.class */
public abstract class AllAppsSearchBarController implements TextWatcher, TextView.OnEditorActionListener, ExtendedEditText.OnBackKeyListener {
    protected AlphabeticalAppsList mApps;
    protected Callbacks mCb;
    protected ExtendedEditText mInput;
    protected InputMethodManager mInputMethodManager;
    protected Launcher mLauncher;
    protected DefaultAppSearchAlgorithm mSearchAlgorithm;

    /* loaded from: a.zip:com/android/launcher3/allapps/AllAppsSearchBarController$Callbacks.class */
    public interface Callbacks {
        void clearSearchResult();

        void onSearchResult(String str, ArrayList<ComponentKey> arrayList);
    }

    @Override // android.text.TextWatcher
    public void afterTextChanged(Editable editable) {
        String editable2 = editable.toString();
        if (editable2.isEmpty()) {
            this.mSearchAlgorithm.cancel(true);
            this.mCb.clearSearchResult();
            return;
        }
        this.mSearchAlgorithm.cancel(false);
        this.mSearchAlgorithm.doSearch(editable2, this.mCb);
    }

    @Override // android.text.TextWatcher
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
    }

    public Intent createMarketSearchIntent(String str) {
        return new Intent("android.intent.action.VIEW").setData(Uri.parse("market://search").buildUpon().appendQueryParameter("c", "apps").appendQueryParameter("q", str).build());
    }

    public void focusSearchField() {
        this.mInput.requestFocus();
        this.mInputMethodManager.showSoftInput(this.mInput, 1);
    }

    public final void initialize(AlphabeticalAppsList alphabeticalAppsList, ExtendedEditText extendedEditText, Launcher launcher, Callbacks callbacks) {
        this.mApps = alphabeticalAppsList;
        this.mCb = callbacks;
        this.mLauncher = launcher;
        this.mInput = extendedEditText;
        this.mInput.addTextChangedListener(this);
        this.mInput.setOnEditorActionListener(this);
        this.mInput.setOnBackKeyListener(this);
        this.mInputMethodManager = (InputMethodManager) this.mInput.getContext().getSystemService("input_method");
        this.mSearchAlgorithm = onInitializeSearch();
    }

    public boolean isSearchFieldFocused() {
        return this.mInput.isFocused();
    }

    @Override // com.android.launcher3.ExtendedEditText.OnBackKeyListener
    public boolean onBackKey() {
        if (Utilities.trim(this.mInput.getEditableText().toString()).isEmpty() || this.mApps.hasNoFilteredResults()) {
            reset();
            return true;
        }
        return false;
    }

    @Override // android.widget.TextView.OnEditorActionListener
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        if (i != 3) {
            return false;
        }
        String charSequence = textView.getText().toString();
        if (charSequence.isEmpty()) {
            return false;
        }
        if (BenesseExtension.getDchaState() == 0) {
            return this.mLauncher.startActivitySafely(textView, createMarketSearchIntent(charSequence), null);
        }
        return true;
    }

    protected abstract DefaultAppSearchAlgorithm onInitializeSearch();

    @Override // android.text.TextWatcher
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
    }

    public void reset() {
        unfocusSearchField();
        this.mCb.clearSearchResult();
        this.mInput.setText("");
        this.mInputMethodManager.hideSoftInputFromWindow(this.mInput.getWindowToken(), 0);
    }

    protected void unfocusSearchField() {
        View focusSearch = this.mInput.focusSearch(130);
        if (focusSearch != null) {
            focusSearch.requestFocus();
        }
    }
}
