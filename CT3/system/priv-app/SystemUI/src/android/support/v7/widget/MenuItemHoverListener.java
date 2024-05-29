package android.support.v7.widget;

import android.support.annotation.NonNull;
import android.support.v7.view.menu.MenuBuilder;
import android.view.MenuItem;
/* loaded from: a.zip:android/support/v7/widget/MenuItemHoverListener.class */
public interface MenuItemHoverListener {
    void onItemHoverEnter(@NonNull MenuBuilder menuBuilder, @NonNull MenuItem menuItem);

    void onItemHoverExit(@NonNull MenuBuilder menuBuilder, @NonNull MenuItem menuItem);
}
