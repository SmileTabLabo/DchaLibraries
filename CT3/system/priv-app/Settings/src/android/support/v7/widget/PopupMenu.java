package android.support.v7.widget;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.v7.appcompat.R$attr;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupWindow;
/* loaded from: classes.dex */
public class PopupMenu {
    private final View mAnchor;
    private final Context mContext;
    private final MenuBuilder mMenu;
    private OnMenuItemClickListener mMenuItemClickListener;
    private OnDismissListener mOnDismissListener;
    private final MenuPopupHelper mPopup;

    /* loaded from: classes.dex */
    public interface OnDismissListener {
        void onDismiss(PopupMenu popupMenu);
    }

    /* loaded from: classes.dex */
    public interface OnMenuItemClickListener {
        boolean onMenuItemClick(MenuItem menuItem);
    }

    public PopupMenu(@NonNull Context context, @NonNull View anchor) {
        this(context, anchor, 0);
    }

    public PopupMenu(@NonNull Context context, @NonNull View anchor, int gravity) {
        this(context, anchor, gravity, R$attr.popupMenuStyle, 0);
    }

    public PopupMenu(@NonNull Context context, @NonNull View anchor, int gravity, @AttrRes int popupStyleAttr, @StyleRes int popupStyleRes) {
        this.mContext = context;
        this.mAnchor = anchor;
        this.mMenu = new MenuBuilder(context);
        this.mMenu.setCallback(new MenuBuilder.Callback() { // from class: android.support.v7.widget.PopupMenu.1
            @Override // android.support.v7.view.menu.MenuBuilder.Callback
            public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
                if (PopupMenu.this.mMenuItemClickListener != null) {
                    return PopupMenu.this.mMenuItemClickListener.onMenuItemClick(item);
                }
                return false;
            }

            @Override // android.support.v7.view.menu.MenuBuilder.Callback
            public void onMenuModeChange(MenuBuilder menu) {
            }
        });
        this.mPopup = new MenuPopupHelper(context, this.mMenu, anchor, false, popupStyleAttr, popupStyleRes);
        this.mPopup.setGravity(gravity);
        this.mPopup.setOnDismissListener(new PopupWindow.OnDismissListener() { // from class: android.support.v7.widget.PopupMenu.2
            @Override // android.widget.PopupWindow.OnDismissListener
            public void onDismiss() {
                if (PopupMenu.this.mOnDismissListener == null) {
                    return;
                }
                PopupMenu.this.mOnDismissListener.onDismiss(PopupMenu.this);
            }
        });
    }

    @NonNull
    public Menu getMenu() {
        return this.mMenu;
    }

    public void show() {
        this.mPopup.show();
    }
}
