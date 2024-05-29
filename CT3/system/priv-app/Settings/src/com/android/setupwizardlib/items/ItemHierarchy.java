package com.android.setupwizardlib.items;
/* loaded from: classes.dex */
public interface ItemHierarchy {

    /* loaded from: classes.dex */
    public interface Observer {
        void onChanged(ItemHierarchy itemHierarchy);
    }

    ItemHierarchy findItemById(int i);

    int getCount();

    IItem getItemAt(int i);

    void registerObserver(Observer observer);
}
