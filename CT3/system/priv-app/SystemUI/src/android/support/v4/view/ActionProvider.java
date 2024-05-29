package android.support.v4.view;

import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
/* loaded from: a.zip:android/support/v4/view/ActionProvider.class */
public abstract class ActionProvider {
    private SubUiVisibilityListener mSubUiVisibilityListener;

    /* loaded from: a.zip:android/support/v4/view/ActionProvider$SubUiVisibilityListener.class */
    public interface SubUiVisibilityListener {
        void onSubUiVisibilityChanged(boolean z);
    }

    public boolean hasSubMenu() {
        return false;
    }

    public boolean isVisible() {
        return true;
    }

    public abstract View onCreateActionView();

    public View onCreateActionView(MenuItem menuItem) {
        return onCreateActionView();
    }

    public boolean onPerformDefaultAction() {
        return false;
    }

    public void onPrepareSubMenu(SubMenu subMenu) {
    }

    public boolean overridesItemVisibility() {
        return false;
    }

    public void setSubUiVisibilityListener(SubUiVisibilityListener subUiVisibilityListener) {
        this.mSubUiVisibilityListener = subUiVisibilityListener;
    }

    public void subUiVisibilityChanged(boolean z) {
        if (this.mSubUiVisibilityListener != null) {
            this.mSubUiVisibilityListener.onSubUiVisibilityChanged(z);
        }
    }
}
