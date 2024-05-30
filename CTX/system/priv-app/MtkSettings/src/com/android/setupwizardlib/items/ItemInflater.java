package com.android.setupwizardlib.items;

import android.content.Context;
/* loaded from: classes.dex */
public class ItemInflater extends ReflectionInflater<ItemHierarchy> {

    /* loaded from: classes.dex */
    public interface ItemParent {
        void addChild(ItemHierarchy itemHierarchy);
    }

    public ItemInflater(Context context) {
        super(context);
        setDefaultPackage(Item.class.getPackage().getName() + ".");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.setupwizardlib.items.SimpleInflater
    public void onAddChildItem(ItemHierarchy itemHierarchy, ItemHierarchy itemHierarchy2) {
        if (itemHierarchy instanceof ItemParent) {
            ((ItemParent) itemHierarchy).addChild(itemHierarchy2);
            return;
        }
        throw new IllegalArgumentException("Cannot add child item to " + itemHierarchy);
    }
}
