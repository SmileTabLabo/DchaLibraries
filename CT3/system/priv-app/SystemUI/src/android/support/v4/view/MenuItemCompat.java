package android.support.v4.view;

import android.os.Build;
import android.support.v4.internal.view.SupportMenuItem;
import android.view.MenuItem;
/* loaded from: a.zip:android/support/v4/view/MenuItemCompat.class */
public final class MenuItemCompat {
    static final MenuVersionImpl IMPL;

    /* loaded from: a.zip:android/support/v4/view/MenuItemCompat$BaseMenuVersionImpl.class */
    static class BaseMenuVersionImpl implements MenuVersionImpl {
        BaseMenuVersionImpl() {
        }

        @Override // android.support.v4.view.MenuItemCompat.MenuVersionImpl
        public boolean expandActionView(MenuItem menuItem) {
            return false;
        }
    }

    /* loaded from: a.zip:android/support/v4/view/MenuItemCompat$HoneycombMenuVersionImpl.class */
    static class HoneycombMenuVersionImpl implements MenuVersionImpl {
        HoneycombMenuVersionImpl() {
        }

        @Override // android.support.v4.view.MenuItemCompat.MenuVersionImpl
        public boolean expandActionView(MenuItem menuItem) {
            return false;
        }
    }

    /* loaded from: a.zip:android/support/v4/view/MenuItemCompat$IcsMenuVersionImpl.class */
    static class IcsMenuVersionImpl extends HoneycombMenuVersionImpl {
        IcsMenuVersionImpl() {
        }

        @Override // android.support.v4.view.MenuItemCompat.HoneycombMenuVersionImpl, android.support.v4.view.MenuItemCompat.MenuVersionImpl
        public boolean expandActionView(MenuItem menuItem) {
            return MenuItemCompatIcs.expandActionView(menuItem);
        }
    }

    /* loaded from: a.zip:android/support/v4/view/MenuItemCompat$MenuVersionImpl.class */
    interface MenuVersionImpl {
        boolean expandActionView(MenuItem menuItem);
    }

    /* loaded from: a.zip:android/support/v4/view/MenuItemCompat$OnActionExpandListener.class */
    public interface OnActionExpandListener {
        boolean onMenuItemActionCollapse(MenuItem menuItem);

        boolean onMenuItemActionExpand(MenuItem menuItem);
    }

    static {
        int i = Build.VERSION.SDK_INT;
        if (i >= 14) {
            IMPL = new IcsMenuVersionImpl();
        } else if (i >= 11) {
            IMPL = new HoneycombMenuVersionImpl();
        } else {
            IMPL = new BaseMenuVersionImpl();
        }
    }

    private MenuItemCompat() {
    }

    public static boolean expandActionView(MenuItem menuItem) {
        return menuItem instanceof SupportMenuItem ? ((SupportMenuItem) menuItem).expandActionView() : IMPL.expandActionView(menuItem);
    }
}
