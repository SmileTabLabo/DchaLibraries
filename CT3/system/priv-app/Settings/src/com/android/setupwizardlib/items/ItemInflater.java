package com.android.setupwizardlib.items;

import android.content.Context;
/* loaded from: classes.dex */
public class ItemInflater extends GenericInflater<ItemHierarchy> {
    private final Context mContext;

    /* loaded from: classes.dex */
    public interface ItemParent {
        void addChild(ItemHierarchy itemHierarchy);
    }

    public ItemInflater(Context context) {
        super(context);
        this.mContext = context;
        setDefaultPackage(Item.class.getPackage().getName() + ".");
    }

    @Override // com.android.setupwizardlib.items.GenericInflater
    public Context getContext() {
        return this.mContext;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.setupwizardlib.items.GenericInflater
    public void onAddChildItem(ItemHierarchy parent, ItemHierarchy child) {
        if (parent instanceof ItemParent) {
            ((ItemParent) parent).addChild(child);
            return;
        }
        throw new IllegalArgumentException("Cannot add child item to " + parent);
    }
}
