package com.android.systemui.statusbar;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
/* loaded from: a.zip:com/android/systemui/statusbar/DismissView.class */
public class DismissView extends StackScrollerDecorView {
    private DismissViewButton mDismissButton;

    public DismissView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override // com.android.systemui.statusbar.StackScrollerDecorView
    protected View findContentView() {
        return findViewById(2131886691);
    }

    public boolean isButtonVisible() {
        return this.mDismissButton.getAlpha() != 0.0f;
    }

    public boolean isOnEmptySpace(float f, float f2) {
        boolean z = true;
        if (f >= this.mContent.getX()) {
            if (f > this.mContent.getX() + this.mContent.getWidth()) {
                z = true;
            } else {
                z = true;
                if (f2 >= this.mContent.getY()) {
                    z = true;
                    if (f2 <= this.mContent.getY() + this.mContent.getHeight()) {
                        z = false;
                    }
                }
            }
        }
        return z;
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mDismissButton.setText(2131493654);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.StackScrollerDecorView, android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mDismissButton = (DismissViewButton) findContentView();
    }

    public void setOnButtonClickListener(View.OnClickListener onClickListener) {
        this.mContent.setOnClickListener(onClickListener);
    }
}
