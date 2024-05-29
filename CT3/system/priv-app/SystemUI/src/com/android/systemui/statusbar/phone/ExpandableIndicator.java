package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/ExpandableIndicator.class */
public class ExpandableIndicator extends ImageView {
    private boolean mExpanded;
    private boolean mIsDefaultDirection;

    public ExpandableIndicator(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mIsDefaultDirection = true;
    }

    private String getContentDescription(boolean z) {
        return z ? this.mContext.getString(2131493907) : this.mContext.getString(2131493906);
    }

    private int getDrawableResourceId(boolean z) {
        int i = 2130837812;
        if (!this.mIsDefaultDirection) {
            return z ? 2130837814 : 2130837812;
        }
        if (!z) {
            i = 2130837814;
        }
        return i;
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        setImageResource(getDrawableResourceId(this.mExpanded));
        setContentDescription(getContentDescription(this.mExpanded));
    }

    public void setExpanded(boolean z) {
        if (z == this.mExpanded) {
            return;
        }
        this.mExpanded = z;
        AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) getContext().getDrawable(getDrawableResourceId(!this.mExpanded)).getConstantState().newDrawable();
        setImageDrawable(animatedVectorDrawable);
        animatedVectorDrawable.forceAnimationOnUI();
        animatedVectorDrawable.start();
        setContentDescription(getContentDescription(z));
    }
}
