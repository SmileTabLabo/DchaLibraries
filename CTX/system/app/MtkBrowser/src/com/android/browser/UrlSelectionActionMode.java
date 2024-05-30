package com.android.browser;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
/* loaded from: classes.dex */
public class UrlSelectionActionMode implements ActionMode.Callback {
    private UiController mUiController;

    public UrlSelectionActionMode(UiController uiController) {
        this.mUiController = uiController;
    }

    @Override // android.view.ActionMode.Callback
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        actionMode.setTitleOptionalHint(false);
        return true;
    }

    @Override // android.view.ActionMode.Callback
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        if (menuItem.getItemId() != 16908341) {
            return false;
        }
        if (this.mUiController.getCurrentTopWebView() != null) {
            ((InputMethodManager) this.mUiController.getActivity().getSystemService("input_method")).hideSoftInputFromWindow(this.mUiController.getCurrentTopWebView().getWindowToken(), 0);
        }
        this.mUiController.shareCurrentPage();
        actionMode.finish();
        return true;
    }

    @Override // android.view.ActionMode.Callback
    public void onDestroyActionMode(ActionMode actionMode) {
    }

    @Override // android.view.ActionMode.Callback
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return true;
    }
}
