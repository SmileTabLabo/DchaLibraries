package com.android.settings.display;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import com.android.settings.R;
/* loaded from: classes.dex */
public class MessageBubbleBackground extends LinearLayout {
    private final int mSnapWidthPixels;

    public MessageBubbleBackground(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mSnapWidthPixels = context.getResources().getDimensionPixelSize(R.dimen.conversation_bubble_width_snap);
    }

    @Override // android.widget.LinearLayout, android.view.View
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthPadding = getPaddingLeft() + getPaddingRight();
        int bubbleWidth = getMeasuredWidth() - widthPadding;
        int maxWidth = View.MeasureSpec.getSize(widthMeasureSpec) - widthPadding;
        super.onMeasure(View.MeasureSpec.makeMeasureSpec(Math.min(maxWidth, (int) (Math.ceil(bubbleWidth / this.mSnapWidthPixels) * this.mSnapWidthPixels)) + widthPadding, 1073741824), heightMeasureSpec);
    }
}
