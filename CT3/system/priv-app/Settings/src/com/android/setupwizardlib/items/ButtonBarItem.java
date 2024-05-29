package com.android.setupwizardlib.items;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import com.android.setupwizardlib.R$layout;
import com.android.setupwizardlib.items.ItemInflater;
import java.util.ArrayList;
/* loaded from: classes.dex */
public class ButtonBarItem extends AbstractItem implements ItemInflater.ItemParent {
    private final ArrayList<ButtonItem> mButtons;
    private boolean mVisible;

    public ButtonBarItem() {
        this.mButtons = new ArrayList<>();
        this.mVisible = true;
    }

    public ButtonBarItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mButtons = new ArrayList<>();
        this.mVisible = true;
    }

    @Override // com.android.setupwizardlib.items.AbstractItem, com.android.setupwizardlib.items.ItemHierarchy
    public int getCount() {
        return isVisible() ? 1 : 0;
    }

    @Override // com.android.setupwizardlib.items.IItem
    public boolean isEnabled() {
        return false;
    }

    @Override // com.android.setupwizardlib.items.IItem
    public int getLayoutResource() {
        return R$layout.suw_items_button_bar;
    }

    public boolean isVisible() {
        return this.mVisible;
    }

    public int getViewId() {
        return getId();
    }

    @Override // com.android.setupwizardlib.items.IItem
    public void onBindView(View view) {
        LinearLayout layout = (LinearLayout) view;
        layout.removeAllViews();
        for (ButtonItem buttonItem : this.mButtons) {
            Button button = buttonItem.createButton(layout);
            layout.addView(button);
        }
        view.setId(getViewId());
    }

    @Override // com.android.setupwizardlib.items.ItemInflater.ItemParent
    public void addChild(ItemHierarchy child) {
        if (child instanceof ButtonItem) {
            this.mButtons.add((ButtonItem) child);
            return;
        }
        throw new UnsupportedOperationException("Cannot add non-button item to Button Bar");
    }

    @Override // com.android.setupwizardlib.items.AbstractItem, com.android.setupwizardlib.items.ItemHierarchy
    public ItemHierarchy findItemById(int id) {
        if (getId() == id) {
            return this;
        }
        for (ButtonItem button : this.mButtons) {
            ItemHierarchy item = button.findItemById(id);
            if (item != null) {
                return item;
            }
        }
        return null;
    }
}
