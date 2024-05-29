package com.android.setupwizardlib.items;

import android.content.Context;
import android.util.AttributeSet;
/* loaded from: classes.dex */
public abstract class AbstractItem extends AbstractItemHierarchy implements IItem {
    public AbstractItem() {
    }

    public AbstractItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override // com.android.setupwizardlib.items.ItemHierarchy
    public int getCount() {
        return 1;
    }

    @Override // com.android.setupwizardlib.items.ItemHierarchy
    public IItem getItemAt(int position) {
        return this;
    }

    @Override // com.android.setupwizardlib.items.ItemHierarchy
    public ItemHierarchy findItemById(int id) {
        if (id == getId()) {
            return this;
        }
        return null;
    }
}
