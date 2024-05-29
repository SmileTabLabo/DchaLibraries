package com.android.systemui.qs;

import android.content.Context;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/systemui/qs/PageIndicator.class */
public class PageIndicator extends ViewGroup {
    private boolean mAnimating;
    private final Runnable mAnimationDone;
    private final int mPageDotWidth;
    private final int mPageIndicatorHeight;
    private final int mPageIndicatorWidth;
    private int mPosition;
    private final ArrayList<Integer> mQueuedPositions;

    public PageIndicator(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mQueuedPositions = new ArrayList<>();
        this.mPosition = -1;
        this.mAnimationDone = new Runnable(this) { // from class: com.android.systemui.qs.PageIndicator.1
            final PageIndicator this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mAnimating = false;
                if (this.this$0.mQueuedPositions.size() != 0) {
                    this.this$0.setPosition(((Integer) this.this$0.mQueuedPositions.remove(0)).intValue());
                }
            }
        };
        this.mPageIndicatorWidth = (int) this.mContext.getResources().getDimension(2131689833);
        this.mPageIndicatorHeight = (int) this.mContext.getResources().getDimension(2131689834);
        this.mPageDotWidth = (int) (this.mPageIndicatorWidth * 0.4f);
    }

    private void animate(int i, int i2) {
        int i3 = i >> 1;
        int i4 = i2 >> 1;
        setIndex(i3);
        boolean z = (i & 1) != 0;
        boolean z2 = !z ? i >= i2 : i <= i2;
        int min = Math.min(i3, i4);
        int max = Math.max(i3, i4);
        int i5 = max;
        if (max == min) {
            i5 = max + 1;
        }
        ImageView imageView = (ImageView) getChildAt(min);
        ImageView imageView2 = (ImageView) getChildAt(i5);
        if (imageView == null || imageView2 == null) {
            return;
        }
        imageView2.setTranslationX(imageView.getX() - imageView2.getX());
        playAnimation(imageView, getTransition(z, z2, false));
        imageView.setAlpha(getAlpha(false));
        playAnimation(imageView2, getTransition(z, z2, true));
        imageView2.setAlpha(getAlpha(true));
        this.mAnimating = true;
    }

    private float getAlpha(boolean z) {
        return z ? 1.0f : 0.3f;
    }

    private int getTransition(boolean z, boolean z2, boolean z3) {
        return z3 ? z ? z2 ? 2130837898 : 2130837900 : z2 ? 2130837896 : 2130837902 : z ? z2 ? 2130837908 : 2130837906 : z2 ? 2130837910 : 2130837904;
    }

    private void playAnimation(ImageView imageView, int i) {
        AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) getContext().getDrawable(i);
        imageView.setImageDrawable(animatedVectorDrawable);
        animatedVectorDrawable.forceAnimationOnUI();
        animatedVectorDrawable.start();
        postDelayed(this.mAnimationDone, 250L);
    }

    private void setIndex(int i) {
        int childCount = getChildCount();
        int i2 = 0;
        while (i2 < childCount) {
            ImageView imageView = (ImageView) getChildAt(i2);
            imageView.setTranslationX(0.0f);
            imageView.setImageResource(2130837895);
            imageView.setAlpha(getAlpha(i2 == i));
            i2++;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setPosition(int i) {
        if (isVisibleToUser() && Math.abs(this.mPosition - i) == 1) {
            animate(this.mPosition, i);
        } else {
            setIndex(i >> 1);
        }
        this.mPosition = i;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int childCount = getChildCount();
        if (childCount == 0) {
            return;
        }
        for (int i5 = 0; i5 < childCount; i5++) {
            int i6 = (this.mPageIndicatorWidth - this.mPageDotWidth) * i5;
            getChildAt(i5).layout(i6, 0, this.mPageIndicatorWidth + i6, this.mPageIndicatorHeight);
        }
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        int childCount = getChildCount();
        if (childCount == 0) {
            super.onMeasure(i, i2);
            return;
        }
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(this.mPageIndicatorWidth, 1073741824);
        int makeMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(this.mPageIndicatorHeight, 1073741824);
        for (int i3 = 0; i3 < childCount; i3++) {
            getChildAt(i3).measure(makeMeasureSpec, makeMeasureSpec2);
        }
        setMeasuredDimension(((this.mPageIndicatorWidth - this.mPageDotWidth) * childCount) + this.mPageDotWidth, this.mPageIndicatorHeight);
    }

    public void setLocation(float f) {
        int i = 1;
        int i2 = (int) f;
        setContentDescription(getContext().getString(2131493913, Integer.valueOf(i2 + 1), Integer.valueOf(getChildCount())));
        if (f == i2) {
            i = 0;
        }
        int i3 = (i2 << 1) | i;
        int i4 = this.mPosition;
        if (this.mQueuedPositions.size() != 0) {
            i4 = this.mQueuedPositions.get(this.mQueuedPositions.size() - 1).intValue();
        }
        if (i3 == i4) {
            return;
        }
        if (this.mAnimating) {
            this.mQueuedPositions.add(Integer.valueOf(i3));
        } else {
            setPosition(i3);
        }
    }

    public void setNumPages(int i) {
        setVisibility(i > 1 ? 0 : 4);
        if (this.mAnimating) {
            Log.w("PageIndicator", "setNumPages during animation");
        }
        while (i < getChildCount()) {
            removeViewAt(getChildCount() - 1);
        }
        while (i > getChildCount()) {
            ImageView imageView = new ImageView(this.mContext);
            imageView.setImageResource(2130837903);
            addView(imageView, new ViewGroup.LayoutParams(this.mPageIndicatorWidth, this.mPageIndicatorHeight));
        }
        setIndex(this.mPosition >> 1);
    }
}
