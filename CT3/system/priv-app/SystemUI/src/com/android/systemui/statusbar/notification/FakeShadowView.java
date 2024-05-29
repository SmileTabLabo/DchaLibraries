package com.android.systemui.statusbar.notification;

import android.content.Context;
import android.graphics.Outline;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.LinearLayout;
import com.android.systemui.statusbar.AlphaOptimizedFrameLayout;
/* loaded from: a.zip:com/android/systemui/statusbar/notification/FakeShadowView.class */
public class FakeShadowView extends AlphaOptimizedFrameLayout {
    private View mFakeShadow;
    private float mOutlineAlpha;
    private final int mShadowMinHeight;

    public FakeShadowView(Context context) {
        this(context, null);
    }

    public FakeShadowView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public FakeShadowView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public FakeShadowView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mFakeShadow = new View(context);
        this.mFakeShadow.setVisibility(4);
        this.mFakeShadow.setLayoutParams(new LinearLayout.LayoutParams(-1, (int) (getResources().getDisplayMetrics().density * 48.0f)));
        this.mFakeShadow.setOutlineProvider(new ViewOutlineProvider(this) { // from class: com.android.systemui.statusbar.notification.FakeShadowView.1
            final FakeShadowView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.ViewOutlineProvider
            public void getOutline(View view, Outline outline) {
                outline.setRect(0, 0, this.this$0.getWidth(), this.this$0.mFakeShadow.getHeight());
                outline.setAlpha(this.this$0.mOutlineAlpha);
            }
        });
        addView(this.mFakeShadow);
        this.mShadowMinHeight = Math.max(1, context.getResources().getDimensionPixelSize(2131689876));
    }

    public void setFakeShadowTranslationZ(float f, float f2, int i, int i2) {
        if (f == 0.0f) {
            this.mFakeShadow.setVisibility(4);
            return;
        }
        this.mFakeShadow.setVisibility(0);
        this.mFakeShadow.setTranslationZ(Math.max(this.mShadowMinHeight, f));
        this.mFakeShadow.setTranslationX(i2);
        this.mFakeShadow.setTranslationY(i - this.mFakeShadow.getHeight());
        if (f2 != this.mOutlineAlpha) {
            this.mOutlineAlpha = f2;
            this.mFakeShadow.invalidateOutline();
        }
    }
}
