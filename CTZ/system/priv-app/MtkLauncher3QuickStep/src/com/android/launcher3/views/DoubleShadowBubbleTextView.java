package com.android.launcher3.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.v4.graphics.ColorUtils;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.launcher3.BubbleTextView;
import com.android.launcher3.R;
/* loaded from: classes.dex */
public class DoubleShadowBubbleTextView extends BubbleTextView {
    private final ShadowInfo mShadowInfo;

    public DoubleShadowBubbleTextView(Context context) {
        this(context, null);
    }

    public DoubleShadowBubbleTextView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public DoubleShadowBubbleTextView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mShadowInfo = new ShadowInfo(context, attributeSet, i);
        setShadowLayer(this.mShadowInfo.ambientShadowBlur, 0.0f, 0.0f, this.mShadowInfo.ambientShadowColor);
    }

    @Override // com.android.launcher3.BubbleTextView, android.widget.TextView, android.view.View
    public void onDraw(Canvas canvas) {
        if (this.mShadowInfo.skipDoubleShadow(this)) {
            super.onDraw(canvas);
            return;
        }
        int alpha = Color.alpha(getCurrentTextColor());
        getPaint().setShadowLayer(this.mShadowInfo.ambientShadowBlur, 0.0f, 0.0f, ColorUtils.setAlphaComponent(this.mShadowInfo.ambientShadowColor, alpha));
        drawWithoutBadge(canvas);
        canvas.save();
        canvas.clipRect(getScrollX(), getScrollY() + getExtendedPaddingTop(), getScrollX() + getWidth(), getScrollY() + getHeight());
        getPaint().setShadowLayer(this.mShadowInfo.keyShadowBlur, 0.0f, this.mShadowInfo.keyShadowOffset, ColorUtils.setAlphaComponent(this.mShadowInfo.keyShadowColor, alpha));
        drawWithoutBadge(canvas);
        canvas.restore();
        drawBadgeIfNecessary(canvas);
    }

    /* loaded from: classes.dex */
    public static class ShadowInfo {
        public final float ambientShadowBlur;
        public final int ambientShadowColor;
        public final float keyShadowBlur;
        public final int keyShadowColor;
        public final float keyShadowOffset;

        public ShadowInfo(Context context, AttributeSet attributeSet, int i) {
            TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.ShadowInfo, i, 0);
            this.ambientShadowBlur = obtainStyledAttributes.getDimension(0, 0.0f);
            this.ambientShadowColor = obtainStyledAttributes.getColor(1, 0);
            this.keyShadowBlur = obtainStyledAttributes.getDimension(2, 0.0f);
            this.keyShadowOffset = obtainStyledAttributes.getDimension(4, 0.0f);
            this.keyShadowColor = obtainStyledAttributes.getColor(3, 0);
            obtainStyledAttributes.recycle();
        }

        public boolean skipDoubleShadow(TextView textView) {
            int alpha = Color.alpha(textView.getCurrentTextColor());
            int alpha2 = Color.alpha(this.keyShadowColor);
            int alpha3 = Color.alpha(this.ambientShadowColor);
            if (alpha == 0 || (alpha2 == 0 && alpha3 == 0)) {
                textView.getPaint().clearShadowLayer();
                return true;
            } else if (alpha3 > 0) {
                textView.getPaint().setShadowLayer(this.ambientShadowBlur, 0.0f, 0.0f, ColorUtils.setAlphaComponent(this.ambientShadowColor, alpha));
                return true;
            } else if (alpha2 > 0) {
                textView.getPaint().setShadowLayer(this.keyShadowBlur, 0.0f, this.keyShadowOffset, ColorUtils.setAlphaComponent(this.keyShadowColor, alpha));
                return true;
            } else {
                return false;
            }
        }
    }
}
