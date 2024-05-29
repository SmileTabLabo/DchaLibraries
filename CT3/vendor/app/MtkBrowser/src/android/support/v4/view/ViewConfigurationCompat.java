package android.support.v4.view;

import android.os.Build;
import android.view.ViewConfiguration;
/* loaded from: b.zip:android/support/v4/view/ViewConfigurationCompat.class */
public final class ViewConfigurationCompat {
    static final ViewConfigurationVersionImpl IMPL;

    /* loaded from: b.zip:android/support/v4/view/ViewConfigurationCompat$BaseViewConfigurationVersionImpl.class */
    static class BaseViewConfigurationVersionImpl implements ViewConfigurationVersionImpl {
        BaseViewConfigurationVersionImpl() {
        }

        @Override // android.support.v4.view.ViewConfigurationCompat.ViewConfigurationVersionImpl
        public int getScaledPagingTouchSlop(ViewConfiguration viewConfiguration) {
            return viewConfiguration.getScaledTouchSlop();
        }
    }

    /* loaded from: b.zip:android/support/v4/view/ViewConfigurationCompat$FroyoViewConfigurationVersionImpl.class */
    static class FroyoViewConfigurationVersionImpl extends BaseViewConfigurationVersionImpl {
        FroyoViewConfigurationVersionImpl() {
        }

        @Override // android.support.v4.view.ViewConfigurationCompat.BaseViewConfigurationVersionImpl, android.support.v4.view.ViewConfigurationCompat.ViewConfigurationVersionImpl
        public int getScaledPagingTouchSlop(ViewConfiguration viewConfiguration) {
            return ViewConfigurationCompatFroyo.getScaledPagingTouchSlop(viewConfiguration);
        }
    }

    /* loaded from: b.zip:android/support/v4/view/ViewConfigurationCompat$HoneycombViewConfigurationVersionImpl.class */
    static class HoneycombViewConfigurationVersionImpl extends FroyoViewConfigurationVersionImpl {
        HoneycombViewConfigurationVersionImpl() {
        }
    }

    /* loaded from: b.zip:android/support/v4/view/ViewConfigurationCompat$IcsViewConfigurationVersionImpl.class */
    static class IcsViewConfigurationVersionImpl extends HoneycombViewConfigurationVersionImpl {
        IcsViewConfigurationVersionImpl() {
        }
    }

    /* loaded from: b.zip:android/support/v4/view/ViewConfigurationCompat$ViewConfigurationVersionImpl.class */
    interface ViewConfigurationVersionImpl {
        int getScaledPagingTouchSlop(ViewConfiguration viewConfiguration);
    }

    static {
        if (Build.VERSION.SDK_INT >= 14) {
            IMPL = new IcsViewConfigurationVersionImpl();
        } else if (Build.VERSION.SDK_INT >= 11) {
            IMPL = new HoneycombViewConfigurationVersionImpl();
        } else if (Build.VERSION.SDK_INT >= 8) {
            IMPL = new FroyoViewConfigurationVersionImpl();
        } else {
            IMPL = new BaseViewConfigurationVersionImpl();
        }
    }

    private ViewConfigurationCompat() {
    }

    public static int getScaledPagingTouchSlop(ViewConfiguration viewConfiguration) {
        return IMPL.getScaledPagingTouchSlop(viewConfiguration);
    }
}
