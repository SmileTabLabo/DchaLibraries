package android.support.v7.view.menu;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v7.view.menu.MenuPresenter;
import android.support.v7.view.menu.MenuView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
/* loaded from: classes.dex */
public abstract class BaseMenuPresenter implements MenuPresenter {
    private MenuPresenter.Callback mCallback;
    protected Context mContext;
    protected LayoutInflater mInflater;
    private int mItemLayoutRes;
    protected MenuBuilder mMenu;
    protected MenuView mMenuView;
    protected Context mSystemContext;
    protected LayoutInflater mSystemInflater;

    public abstract void bindItemView(MenuItemImpl menuItemImpl, MenuView.ItemView itemView);

    @Override // android.support.v7.view.menu.MenuPresenter
    public void initForMenu(Context context, MenuBuilder menu) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(this.mContext);
        this.mMenu = menu;
    }

    @Override // android.support.v7.view.menu.MenuPresenter
    public void updateMenuView(boolean cleared) {
        ViewGroup parent = (ViewGroup) this.mMenuView;
        if (parent == null) {
            return;
        }
        int childIndex = 0;
        if (this.mMenu != null) {
            this.mMenu.flagActionItems();
            ArrayList<MenuItemImpl> visibleItems = this.mMenu.getVisibleItems();
            int itemCount = visibleItems.size();
            for (int i = 0; i < itemCount; i++) {
                MenuItemImpl item = visibleItems.get(i);
                if (shouldIncludeItem(childIndex, item)) {
                    View convertView = parent.getChildAt(childIndex);
                    MenuItemImpl itemData = convertView instanceof MenuView.ItemView ? ((MenuView.ItemView) convertView).getItemData() : null;
                    View itemView = getItemView(item, convertView, parent);
                    if (item != itemData) {
                        itemView.setPressed(false);
                        ViewCompat.jumpDrawablesToCurrentState(itemView);
                    }
                    if (itemView != convertView) {
                        addItemView(itemView, childIndex);
                    }
                    childIndex++;
                }
            }
        }
        while (childIndex < parent.getChildCount()) {
            if (!filterLeftoverView(parent, childIndex)) {
                childIndex++;
            }
        }
    }

    protected void addItemView(View itemView, int childIndex) {
        ViewGroup currentParent = (ViewGroup) itemView.getParent();
        if (currentParent != null) {
            currentParent.removeView(itemView);
        }
        ((ViewGroup) this.mMenuView).addView(itemView, childIndex);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean filterLeftoverView(ViewGroup parent, int childIndex) {
        parent.removeViewAt(childIndex);
        return true;
    }

    @Override // android.support.v7.view.menu.MenuPresenter
    public void setCallback(MenuPresenter.Callback cb) {
        this.mCallback = cb;
    }

    public MenuPresenter.Callback getCallback() {
        return this.mCallback;
    }

    public MenuView.ItemView createItemView(ViewGroup parent) {
        return (MenuView.ItemView) this.mSystemInflater.inflate(this.mItemLayoutRes, parent, false);
    }

    public View getItemView(MenuItemImpl item, View convertView, ViewGroup parent) {
        MenuView.ItemView itemView;
        if (convertView instanceof MenuView.ItemView) {
            itemView = (MenuView.ItemView) convertView;
        } else {
            itemView = createItemView(parent);
        }
        bindItemView(item, itemView);
        return (View) itemView;
    }

    public boolean shouldIncludeItem(int childIndex, MenuItemImpl item) {
        return true;
    }

    @Override // android.support.v7.view.menu.MenuPresenter
    public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
        if (this.mCallback == null) {
            return;
        }
        this.mCallback.onCloseMenu(menu, allMenusAreClosing);
    }

    @Override // android.support.v7.view.menu.MenuPresenter
    public boolean onSubMenuSelected(SubMenuBuilder menu) {
        if (this.mCallback != null) {
            return this.mCallback.onOpenSubMenu(menu);
        }
        return false;
    }

    @Override // android.support.v7.view.menu.MenuPresenter
    public boolean flagActionItems() {
        return false;
    }

    @Override // android.support.v7.view.menu.MenuPresenter
    public boolean expandItemActionView(MenuBuilder menu, MenuItemImpl item) {
        return false;
    }

    @Override // android.support.v7.view.menu.MenuPresenter
    public boolean collapseItemActionView(MenuBuilder menu, MenuItemImpl item) {
        return false;
    }
}
