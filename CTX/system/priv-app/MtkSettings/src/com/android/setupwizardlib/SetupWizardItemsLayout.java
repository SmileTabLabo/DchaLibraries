package com.android.setupwizardlib;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListAdapter;
import com.android.setupwizardlib.items.ItemAdapter;
@Deprecated
/* loaded from: classes.dex */
public class SetupWizardItemsLayout extends SetupWizardListLayout {
    public SetupWizardItemsLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public SetupWizardItemsLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    @Override // com.android.setupwizardlib.SetupWizardListLayout
    public ItemAdapter getAdapter() {
        ListAdapter adapter = super.getAdapter();
        if (adapter instanceof ItemAdapter) {
            return (ItemAdapter) adapter;
        }
        return null;
    }
}
