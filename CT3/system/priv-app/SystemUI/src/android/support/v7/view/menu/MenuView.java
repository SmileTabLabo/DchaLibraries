package android.support.v7.view.menu;
/* loaded from: a.zip:android/support/v7/view/menu/MenuView.class */
public interface MenuView {

    /* loaded from: a.zip:android/support/v7/view/menu/MenuView$ItemView.class */
    public interface ItemView {
        MenuItemImpl getItemData();

        void initialize(MenuItemImpl menuItemImpl, int i);

        boolean prefersCondensedTitle();
    }
}
