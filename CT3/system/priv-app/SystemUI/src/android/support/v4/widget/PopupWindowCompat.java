package android.support.v4.widget;

import android.os.Build;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.widget.PopupWindow;
/* loaded from: a.zip:android/support/v4/widget/PopupWindowCompat.class */
public final class PopupWindowCompat {
    static final PopupWindowImpl IMPL;

    /* loaded from: a.zip:android/support/v4/widget/PopupWindowCompat$Api21PopupWindowImpl.class */
    static class Api21PopupWindowImpl extends KitKatPopupWindowImpl {
        Api21PopupWindowImpl() {
        }

        @Override // android.support.v4.widget.PopupWindowCompat.BasePopupWindowImpl, android.support.v4.widget.PopupWindowCompat.PopupWindowImpl
        public void setOverlapAnchor(PopupWindow popupWindow, boolean z) {
            PopupWindowCompatApi21.setOverlapAnchor(popupWindow, z);
        }
    }

    /* loaded from: a.zip:android/support/v4/widget/PopupWindowCompat$Api23PopupWindowImpl.class */
    static class Api23PopupWindowImpl extends Api21PopupWindowImpl {
        Api23PopupWindowImpl() {
        }

        @Override // android.support.v4.widget.PopupWindowCompat.Api21PopupWindowImpl, android.support.v4.widget.PopupWindowCompat.BasePopupWindowImpl, android.support.v4.widget.PopupWindowCompat.PopupWindowImpl
        public void setOverlapAnchor(PopupWindow popupWindow, boolean z) {
            PopupWindowCompatApi23.setOverlapAnchor(popupWindow, z);
        }

        @Override // android.support.v4.widget.PopupWindowCompat.GingerbreadPopupWindowImpl, android.support.v4.widget.PopupWindowCompat.BasePopupWindowImpl, android.support.v4.widget.PopupWindowCompat.PopupWindowImpl
        public void setWindowLayoutType(PopupWindow popupWindow, int i) {
            PopupWindowCompatApi23.setWindowLayoutType(popupWindow, i);
        }
    }

    /* loaded from: a.zip:android/support/v4/widget/PopupWindowCompat$BasePopupWindowImpl.class */
    static class BasePopupWindowImpl implements PopupWindowImpl {
        BasePopupWindowImpl() {
        }

        @Override // android.support.v4.widget.PopupWindowCompat.PopupWindowImpl
        public void setOverlapAnchor(PopupWindow popupWindow, boolean z) {
        }

        @Override // android.support.v4.widget.PopupWindowCompat.PopupWindowImpl
        public void setWindowLayoutType(PopupWindow popupWindow, int i) {
        }

        @Override // android.support.v4.widget.PopupWindowCompat.PopupWindowImpl
        public void showAsDropDown(PopupWindow popupWindow, View view, int i, int i2, int i3) {
            int i4 = i;
            if ((GravityCompat.getAbsoluteGravity(i3, ViewCompat.getLayoutDirection(view)) & 7) == 5) {
                i4 = i - (popupWindow.getWidth() - view.getWidth());
            }
            popupWindow.showAsDropDown(view, i4, i2);
        }
    }

    /* loaded from: a.zip:android/support/v4/widget/PopupWindowCompat$GingerbreadPopupWindowImpl.class */
    static class GingerbreadPopupWindowImpl extends BasePopupWindowImpl {
        GingerbreadPopupWindowImpl() {
        }

        @Override // android.support.v4.widget.PopupWindowCompat.BasePopupWindowImpl, android.support.v4.widget.PopupWindowCompat.PopupWindowImpl
        public void setWindowLayoutType(PopupWindow popupWindow, int i) {
            PopupWindowCompatGingerbread.setWindowLayoutType(popupWindow, i);
        }
    }

    /* loaded from: a.zip:android/support/v4/widget/PopupWindowCompat$KitKatPopupWindowImpl.class */
    static class KitKatPopupWindowImpl extends GingerbreadPopupWindowImpl {
        KitKatPopupWindowImpl() {
        }

        @Override // android.support.v4.widget.PopupWindowCompat.BasePopupWindowImpl, android.support.v4.widget.PopupWindowCompat.PopupWindowImpl
        public void showAsDropDown(PopupWindow popupWindow, View view, int i, int i2, int i3) {
            PopupWindowCompatKitKat.showAsDropDown(popupWindow, view, i, i2, i3);
        }
    }

    /* loaded from: a.zip:android/support/v4/widget/PopupWindowCompat$PopupWindowImpl.class */
    interface PopupWindowImpl {
        void setOverlapAnchor(PopupWindow popupWindow, boolean z);

        void setWindowLayoutType(PopupWindow popupWindow, int i);

        void showAsDropDown(PopupWindow popupWindow, View view, int i, int i2, int i3);
    }

    static {
        int i = Build.VERSION.SDK_INT;
        if (i >= 23) {
            IMPL = new Api23PopupWindowImpl();
        } else if (i >= 21) {
            IMPL = new Api21PopupWindowImpl();
        } else if (i >= 19) {
            IMPL = new KitKatPopupWindowImpl();
        } else if (i >= 9) {
            IMPL = new GingerbreadPopupWindowImpl();
        } else {
            IMPL = new BasePopupWindowImpl();
        }
    }

    private PopupWindowCompat() {
    }

    public static void setOverlapAnchor(PopupWindow popupWindow, boolean z) {
        IMPL.setOverlapAnchor(popupWindow, z);
    }

    public static void setWindowLayoutType(PopupWindow popupWindow, int i) {
        IMPL.setWindowLayoutType(popupWindow, i);
    }

    public static void showAsDropDown(PopupWindow popupWindow, View view, int i, int i2, int i3) {
        IMPL.showAsDropDown(popupWindow, view, i, i2, i3);
    }
}
