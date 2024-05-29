package com.android.launcher3.allapps;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
/* loaded from: a.zip:com/android/launcher3/allapps/HeaderElevationController.class */
public abstract class HeaderElevationController extends RecyclerView.OnScrollListener {
    private int mCurrentY = 0;

    /* loaded from: a.zip:com/android/launcher3/allapps/HeaderElevationController$ControllerV16.class */
    public static class ControllerV16 extends HeaderElevationController {
        private final float mScrollToElevation;
        private final View mShadow;

        public ControllerV16(View view) {
            Resources resources = view.getContext().getResources();
            this.mScrollToElevation = resources.getDimension(2131230779);
            this.mShadow = new View(view.getContext());
            this.mShadow.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{503316480, 0}));
            this.mShadow.setAlpha(0.0f);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-1, resources.getDimensionPixelSize(2131230780));
            layoutParams.topMargin = ((FrameLayout.LayoutParams) view.getLayoutParams()).height;
            ((ViewGroup) view.getParent()).addView(this.mShadow, layoutParams);
        }

        @Override // com.android.launcher3.allapps.HeaderElevationController
        public void onScroll(int i) {
            this.mShadow.setAlpha(Math.min(i, this.mScrollToElevation) / this.mScrollToElevation);
        }

        @Override // com.android.launcher3.allapps.HeaderElevationController
        public void updateBackgroundPadding(Rect rect) {
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mShadow.getLayoutParams();
            layoutParams.leftMargin = rect.left;
            layoutParams.rightMargin = rect.right;
            this.mShadow.requestLayout();
        }
    }

    @TargetApi(21)
    /* loaded from: a.zip:com/android/launcher3/allapps/HeaderElevationController$ControllerVL.class */
    public static class ControllerVL extends HeaderElevationController {
        private final View mHeader;
        private final float mMaxElevation;
        private final float mScrollToElevation;

        public ControllerVL(View view) {
            this.mHeader = view;
            this.mHeader.setOutlineProvider(ViewOutlineProvider.BOUNDS);
            Resources resources = view.getContext().getResources();
            this.mMaxElevation = resources.getDimension(2131230778);
            this.mScrollToElevation = resources.getDimension(2131230779);
        }

        @Override // com.android.launcher3.allapps.HeaderElevationController
        public void onScroll(int i) {
            float min = this.mMaxElevation * (Math.min(i, this.mScrollToElevation) / this.mScrollToElevation);
            if (Float.compare(this.mHeader.getElevation(), min) != 0) {
                this.mHeader.setElevation(min);
            }
        }
    }

    abstract void onScroll(int i);

    @Override // android.support.v7.widget.RecyclerView.OnScrollListener
    public final void onScrolled(RecyclerView recyclerView, int i, int i2) {
        this.mCurrentY += i2;
        onScroll(this.mCurrentY);
    }

    public void reset() {
        this.mCurrentY = 0;
        onScroll(this.mCurrentY);
    }

    public void updateBackgroundPadding(Rect rect) {
    }
}
