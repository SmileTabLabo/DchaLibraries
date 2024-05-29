package com.android.systemui.statusbar.car;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import com.android.keyguard.AlphaOptimizedImageButton;
/* loaded from: a.zip:com/android/systemui/statusbar/car/CarNavigationButton.class */
public class CarNavigationButton extends RelativeLayout {
    private AlphaOptimizedImageButton mIcon;
    private AlphaOptimizedImageButton mMoreIcon;

    public CarNavigationButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mIcon = (AlphaOptimizedImageButton) findViewById(2131886268);
        this.mIcon.setClickable(false);
        this.mIcon.setBackgroundColor(17170445);
        this.mIcon.setAlpha(0.7f);
        this.mMoreIcon = (AlphaOptimizedImageButton) findViewById(2131886269);
        this.mMoreIcon.setClickable(false);
        this.mMoreIcon.setBackgroundColor(17170445);
        this.mMoreIcon.setVisibility(4);
        this.mMoreIcon.setImageDrawable(getContext().getDrawable(2130837588));
        this.mMoreIcon.setAlpha(0.7f);
    }

    public void setResources(Drawable drawable) {
        this.mIcon.setImageDrawable(drawable);
    }

    public void setSelected(boolean z, boolean z2) {
        int i = 4;
        if (!z) {
            this.mMoreIcon.setVisibility(4);
            this.mIcon.setAlpha(0.7f);
            return;
        }
        AlphaOptimizedImageButton alphaOptimizedImageButton = this.mMoreIcon;
        if (z2) {
            i = 0;
        }
        alphaOptimizedImageButton.setVisibility(i);
        this.mMoreIcon.setAlpha(1.0f);
        this.mIcon.setAlpha(1.0f);
    }
}
