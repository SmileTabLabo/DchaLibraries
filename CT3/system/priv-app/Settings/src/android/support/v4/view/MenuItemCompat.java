package android.support.v4.view;

import android.os.Build;
import android.support.v4.internal.view.SupportMenuItem;
import android.view.MenuItem;
/* loaded from: classes.dex */
public final class MenuItemCompat {
    static final MenuVersionImpl IMPL;

    /* loaded from: classes.dex */
    interface MenuVersionImpl {
        boolean expandActionView(MenuItem menuItem);
    }

    /* loaded from: classes.dex */
    public interface OnActionExpandListener {
        boolean onMenuItemActionCollapse(MenuItem menuItem);

        boolean onMenuItemActionExpand(MenuItem menuItem);
    }

    /* loaded from: classes.dex */
    static class BaseMenuVersionImpl implements MenuVersionImpl {
        BaseMenuVersionImpl() {
        }

        @Override // android.support.v4.view.MenuItemCompat.MenuVersionImpl
        public boolean expandActionView(MenuItem item) {
            return false;
        }
    }

    /* loaded from: classes.dex */
    static class HoneycombMenuVersionImpl implements MenuVersionImpl {
        HoneycombMenuVersionImpl() {
        }

        @Override // android.support.v4.view.MenuItemCompat.MenuVersionImpl
        public boolean expandActionView(MenuItem item) {
            return false;
        }
    }

    /* loaded from: classes.dex */
    static class IcsMenuVersionImpl extends HoneycombMenuVersionImpl {
        IcsMenuVersionImpl() {
        }

        @Override // android.support.v4.view.MenuItemCompat.HoneycombMenuVersionImpl, android.support.v4.view.MenuItemCompat.MenuVersionImpl
        public boolean expandActionView(MenuItem item) {
            return MenuItemCompatIcs.expandActionView(item);
        }
    }

    static {
        int version = Build.VERSION.SDK_INT;
        if (version >= 14) {
            IMPL = new IcsMenuVersionImpl();
        } else if (version >= 11) {
            IMPL = new HoneycombMenuVersionImpl();
        } else {
            IMPL = new BaseMenuVersionImpl();
        }
    }

    public static boolean expandActionView(MenuItem item) {
        if (item instanceof SupportMenuItem) {
            return ((SupportMenuItem) item).expandActionView();
        }
        return IMPL.expandActionView(item);
    }

    private MenuItemCompat() {
    }
}
