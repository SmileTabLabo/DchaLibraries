package com.android.systemui.statusbar;

import android.content.Context;
import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
/* loaded from: a.zip:com/android/systemui/statusbar/ExpandableOutlineView.class */
public abstract class ExpandableOutlineView extends ExpandableView {
    private boolean mCustomOutline;
    private float mOutlineAlpha;
    private final Rect mOutlineRect;
    ViewOutlineProvider mProvider;

    public ExpandableOutlineView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mOutlineRect = new Rect();
        this.mOutlineAlpha = -1.0f;
        this.mProvider = new ViewOutlineProvider(this) { // from class: com.android.systemui.statusbar.ExpandableOutlineView.1
            final ExpandableOutlineView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.ViewOutlineProvider
            public void getOutline(View view, Outline outline) {
                int translation = (int) this.this$0.getTranslation();
                if (this.this$0.mCustomOutline) {
                    outline.setRect(this.this$0.mOutlineRect);
                } else {
                    outline.setRect(translation, this.this$0.mClipTopAmount, this.this$0.getWidth() + translation, Math.max(this.this$0.getActualHeight(), this.this$0.mClipTopAmount));
                }
                outline.setAlpha(this.this$0.mOutlineAlpha);
            }
        };
        setOutlineProvider(this.mProvider);
    }

    @Override // com.android.systemui.statusbar.ExpandableView
    public float getOutlineAlpha() {
        return this.mOutlineAlpha;
    }

    @Override // com.android.systemui.statusbar.ExpandableView
    public int getOutlineTranslation() {
        return this.mCustomOutline ? this.mOutlineRect.left : (int) getTranslation();
    }

    @Override // com.android.systemui.statusbar.ExpandableView
    public void setActualHeight(int i, boolean z) {
        super.setActualHeight(i, z);
        invalidateOutline();
    }

    @Override // com.android.systemui.statusbar.ExpandableView
    public void setClipTopAmount(int i) {
        super.setClipTopAmount(i);
        invalidateOutline();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setOutlineAlpha(float f) {
        if (f != this.mOutlineAlpha) {
            this.mOutlineAlpha = f;
            invalidateOutline();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setOutlineRect(float f, float f2, float f3, float f4) {
        this.mCustomOutline = true;
        setClipToOutline(true);
        this.mOutlineRect.set((int) f, (int) f2, (int) f3, (int) f4);
        this.mOutlineRect.bottom = (int) Math.max(f2, this.mOutlineRect.bottom);
        this.mOutlineRect.right = (int) Math.max(f, this.mOutlineRect.right);
        invalidateOutline();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setOutlineRect(RectF rectF) {
        if (rectF != null) {
            setOutlineRect(rectF.left, rectF.top, rectF.right, rectF.bottom);
            return;
        }
        this.mCustomOutline = false;
        setClipToOutline(false);
        invalidateOutline();
    }

    public void updateOutline() {
        if (this.mCustomOutline) {
            return;
        }
        boolean z = true;
        if (isChildInGroup()) {
            z = isGroupExpanded() && !isGroupExpansionChanging();
        } else if (isSummaryWithChildren()) {
            z = isGroupExpanded() ? isGroupExpansionChanging() : true;
        }
        setOutlineProvider(z ? this.mProvider : null);
    }
}
