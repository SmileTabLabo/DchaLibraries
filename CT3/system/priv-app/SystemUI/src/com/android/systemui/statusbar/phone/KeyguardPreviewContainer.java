package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.WindowInsets;
import android.widget.FrameLayout;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/KeyguardPreviewContainer.class */
public class KeyguardPreviewContainer extends FrameLayout {
    private Drawable mBlackBarDrawable;

    public KeyguardPreviewContainer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mBlackBarDrawable = new Drawable(this) { // from class: com.android.systemui.statusbar.phone.KeyguardPreviewContainer.1
            final KeyguardPreviewContainer this$0;

            {
                this.this$0 = this;
            }

            @Override // android.graphics.drawable.Drawable
            public void draw(Canvas canvas) {
                canvas.save();
                canvas.clipRect(0, this.this$0.getHeight() - this.this$0.getPaddingBottom(), this.this$0.getWidth(), this.this$0.getHeight());
                canvas.drawColor(-16777216);
                canvas.restore();
            }

            @Override // android.graphics.drawable.Drawable
            public int getOpacity() {
                return -1;
            }

            @Override // android.graphics.drawable.Drawable
            public void setAlpha(int i) {
            }

            @Override // android.graphics.drawable.Drawable
            public void setColorFilter(ColorFilter colorFilter) {
            }
        };
        setBackground(this.mBlackBarDrawable);
    }

    @Override // android.view.View
    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        setPadding(0, 0, 0, windowInsets.getStableInsetBottom());
        return super.onApplyWindowInsets(windowInsets);
    }
}
