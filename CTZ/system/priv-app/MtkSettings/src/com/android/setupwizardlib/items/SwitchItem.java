package com.android.setupwizardlib.items;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import com.android.setupwizardlib.R;
/* loaded from: classes.dex */
public class SwitchItem extends Item implements CompoundButton.OnCheckedChangeListener {
    private boolean mChecked;
    private OnCheckedChangeListener mListener;

    /* loaded from: classes.dex */
    public interface OnCheckedChangeListener {
        void onCheckedChange(SwitchItem switchItem, boolean z);
    }

    public SwitchItem() {
        this.mChecked = false;
    }

    public SwitchItem(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mChecked = false;
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.SuwSwitchItem);
        this.mChecked = obtainStyledAttributes.getBoolean(R.styleable.SuwSwitchItem_android_checked, false);
        obtainStyledAttributes.recycle();
    }

    @Override // com.android.setupwizardlib.items.Item
    protected int getDefaultLayoutResource() {
        return R.layout.suw_items_switch;
    }

    @Override // com.android.setupwizardlib.items.Item, com.android.setupwizardlib.items.IItem
    public void onBindView(View view) {
        super.onBindView(view);
        SwitchCompat switchCompat = (SwitchCompat) view.findViewById(R.id.suw_items_switch);
        switchCompat.setOnCheckedChangeListener(null);
        switchCompat.setChecked(this.mChecked);
        switchCompat.setOnCheckedChangeListener(this);
        switchCompat.setEnabled(isEnabled());
    }

    @Override // android.widget.CompoundButton.OnCheckedChangeListener
    public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
        this.mChecked = z;
        if (this.mListener != null) {
            this.mListener.onCheckedChange(this, z);
        }
    }
}
