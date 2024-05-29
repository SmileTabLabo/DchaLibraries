package android.support.v7.app;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.v7.appcompat.R$styleable;
import android.util.AttributeSet;
import android.view.ViewGroup;
/* loaded from: a.zip:android/support/v7/app/ActionBar$LayoutParams.class */
public class ActionBar$LayoutParams extends ViewGroup.MarginLayoutParams {
    public int gravity;

    public ActionBar$LayoutParams(int i, int i2) {
        super(i, i2);
        this.gravity = 0;
        this.gravity = 8388627;
    }

    public ActionBar$LayoutParams(@NonNull Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.gravity = 0;
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.ActionBarLayout);
        this.gravity = obtainStyledAttributes.getInt(R$styleable.ActionBarLayout_android_layout_gravity, 0);
        obtainStyledAttributes.recycle();
    }

    public ActionBar$LayoutParams(ActionBar$LayoutParams actionBar$LayoutParams) {
        super((ViewGroup.MarginLayoutParams) actionBar$LayoutParams);
        this.gravity = 0;
        this.gravity = actionBar$LayoutParams.gravity;
    }

    public ActionBar$LayoutParams(ViewGroup.LayoutParams layoutParams) {
        super(layoutParams);
        this.gravity = 0;
    }
}
