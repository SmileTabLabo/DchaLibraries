package com.android.launcher3;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.graphics.Rect;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.launcher3.CellLayout;
import com.android.launcher3.DragLayer;
import com.android.launcher3.accessibility.DragViewStateAnnouncer;
import com.android.launcher3.util.FocusLogic;
/* loaded from: a.zip:com/android/launcher3/AppWidgetResizeFrame.class */
public class AppWidgetResizeFrame extends FrameLayout implements View.OnKeyListener {
    private static Rect sTmpRect = new Rect();
    private final int mBackgroundPadding;
    private int mBaselineHeight;
    private int mBaselineWidth;
    private int mBaselineX;
    private int mBaselineY;
    private boolean mBottomBorderActive;
    private final ImageView mBottomHandle;
    private int mBottomTouchRegionAdjustment;
    private final CellLayout mCellLayout;
    private int mDeltaX;
    private int mDeltaXAddOn;
    private int mDeltaY;
    private int mDeltaYAddOn;
    private final int[] mDirectionVector;
    private final DragLayer mDragLayer;
    private final int[] mLastDirectionVector;
    private final Launcher mLauncher;
    private boolean mLeftBorderActive;
    private final ImageView mLeftHandle;
    private int mMinHSpan;
    private int mMinVSpan;
    private int mResizeMode;
    private boolean mRightBorderActive;
    private final ImageView mRightHandle;
    private int mRunningHInc;
    private int mRunningVInc;
    private final DragViewStateAnnouncer mStateAnnouncer;
    private final int[] mTmpPt;
    private boolean mTopBorderActive;
    private final ImageView mTopHandle;
    private int mTopTouchRegionAdjustment;
    private final int mTouchTargetWidth;
    private final Rect mWidgetPadding;
    private final LauncherAppWidgetHostView mWidgetView;

    public AppWidgetResizeFrame(Context context, LauncherAppWidgetHostView launcherAppWidgetHostView, CellLayout cellLayout, DragLayer dragLayer) {
        super(context);
        this.mDirectionVector = new int[2];
        this.mLastDirectionVector = new int[2];
        this.mTmpPt = new int[2];
        this.mTopTouchRegionAdjustment = 0;
        this.mBottomTouchRegionAdjustment = 0;
        this.mLauncher = (Launcher) context;
        this.mCellLayout = cellLayout;
        this.mWidgetView = launcherAppWidgetHostView;
        LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo = (LauncherAppWidgetProviderInfo) launcherAppWidgetHostView.getAppWidgetInfo();
        this.mResizeMode = launcherAppWidgetProviderInfo.resizeMode;
        this.mDragLayer = dragLayer;
        this.mMinHSpan = launcherAppWidgetProviderInfo.minSpanX;
        this.mMinVSpan = launcherAppWidgetProviderInfo.minSpanY;
        this.mStateAnnouncer = DragViewStateAnnouncer.createFor(this);
        setBackgroundResource(2130837567);
        setForeground(getResources().getDrawable(2130837566));
        setPadding(0, 0, 0, 0);
        int dimensionPixelSize = getResources().getDimensionPixelSize(2131230742);
        this.mLeftHandle = new ImageView(context);
        this.mLeftHandle.setImageResource(2130837540);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-2, -2, 19);
        layoutParams.leftMargin = dimensionPixelSize;
        addView(this.mLeftHandle, layoutParams);
        this.mRightHandle = new ImageView(context);
        this.mRightHandle.setImageResource(2130837540);
        FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(-2, -2, 21);
        layoutParams2.rightMargin = dimensionPixelSize;
        addView(this.mRightHandle, layoutParams2);
        this.mTopHandle = new ImageView(context);
        this.mTopHandle.setImageResource(2130837540);
        FrameLayout.LayoutParams layoutParams3 = new FrameLayout.LayoutParams(-2, -2, 49);
        layoutParams3.topMargin = dimensionPixelSize;
        addView(this.mTopHandle, layoutParams3);
        this.mBottomHandle = new ImageView(context);
        this.mBottomHandle.setImageResource(2130837540);
        FrameLayout.LayoutParams layoutParams4 = new FrameLayout.LayoutParams(-2, -2, 81);
        layoutParams4.bottomMargin = dimensionPixelSize;
        addView(this.mBottomHandle, layoutParams4);
        if (launcherAppWidgetProviderInfo.isCustomWidget) {
            int dimensionPixelSize2 = context.getResources().getDimensionPixelSize(2131230741);
            this.mWidgetPadding = new Rect(dimensionPixelSize2, dimensionPixelSize2, dimensionPixelSize2, dimensionPixelSize2);
        } else {
            this.mWidgetPadding = AppWidgetHostView.getDefaultPaddingForWidget(context, launcherAppWidgetHostView.getAppWidgetInfo().provider, null);
        }
        if (this.mResizeMode == 1) {
            this.mTopHandle.setVisibility(8);
            this.mBottomHandle.setVisibility(8);
        } else if (this.mResizeMode == 2) {
            this.mLeftHandle.setVisibility(8);
            this.mRightHandle.setVisibility(8);
        }
        this.mBackgroundPadding = getResources().getDimensionPixelSize(2131230743);
        this.mTouchTargetWidth = this.mBackgroundPadding * 2;
        this.mCellLayout.markCellsAsUnoccupiedForView(this.mWidgetView);
        setOnKeyListener(this);
    }

    public static Rect getWidgetSizeRanges(Launcher launcher, int i, int i2, Rect rect) {
        Rect rect2 = rect;
        if (rect == null) {
            rect2 = new Rect();
        }
        Rect cellLayoutMetrics = Workspace.getCellLayoutMetrics(launcher, 0);
        Rect cellLayoutMetrics2 = Workspace.getCellLayoutMetrics(launcher, 1);
        float f = launcher.getResources().getDisplayMetrics().density;
        int i3 = cellLayoutMetrics.left;
        int i4 = cellLayoutMetrics.top;
        int i5 = cellLayoutMetrics.right;
        int i6 = cellLayoutMetrics.bottom;
        int i7 = (int) (((i * i3) + ((i - 1) * i5)) / f);
        int i8 = (int) (((i2 * i4) + ((i2 - 1) * i6)) / f);
        int i9 = cellLayoutMetrics2.left;
        int i10 = cellLayoutMetrics2.top;
        rect2.set((int) (((i * i9) + ((i - 1) * cellLayoutMetrics2.right)) / f), i8, i7, (int) (((i2 * i10) + ((i2 - 1) * cellLayoutMetrics2.bottom)) / f));
        return rect2;
    }

    /* JADX WARN: Code restructure failed: missing block: B:30:0x0194, code lost:
        if (r9.mRightBorderActive != false) goto L67;
     */
    /* JADX WARN: Code restructure failed: missing block: B:40:0x01dd, code lost:
        if (r9.mBottomBorderActive != false) goto L60;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private void resizeWidgetIfNeeded(boolean z) {
        int i;
        int i2;
        int i3;
        int i4;
        int i5;
        int i6;
        int i7;
        int i8;
        int i9;
        int cellWidth = this.mCellLayout.getCellWidth();
        int widthGap = this.mCellLayout.getWidthGap();
        int cellHeight = this.mCellLayout.getCellHeight();
        int heightGap = this.mCellLayout.getHeightGap();
        int i10 = this.mDeltaX;
        int i11 = this.mDeltaXAddOn;
        int i12 = this.mDeltaY;
        int i13 = this.mDeltaYAddOn;
        float f = (((i10 + i11) * 1.0f) / (cellWidth + widthGap)) - this.mRunningHInc;
        float f2 = (((i12 + i13) * 1.0f) / (cellHeight + heightGap)) - this.mRunningVInc;
        int i14 = 0;
        int i15 = 0;
        int countX = this.mCellLayout.getCountX();
        int countY = this.mCellLayout.getCountY();
        if (Math.abs(f) > 0.66f) {
            i14 = Math.round(f);
        }
        if (Math.abs(f2) > 0.66f) {
            i15 = Math.round(f2);
        }
        if (!z && i14 == 0 && i15 == 0) {
            return;
        }
        CellLayout.LayoutParams layoutParams = (CellLayout.LayoutParams) this.mWidgetView.getLayoutParams();
        int i16 = layoutParams.cellHSpan;
        int i17 = layoutParams.cellVSpan;
        int i18 = layoutParams.useTmpCoords ? layoutParams.tmpCellX : layoutParams.cellX;
        int i19 = layoutParams.useTmpCoords ? layoutParams.tmpCellY : layoutParams.cellY;
        int i20 = 0;
        if (this.mLeftBorderActive) {
            i = Math.min(layoutParams.cellHSpan - this.mMinHSpan, Math.max(-i18, i14));
            i2 = Math.max(-(layoutParams.cellHSpan - this.mMinHSpan), Math.min(i18, i14 * (-1)));
            i20 = -i2;
        } else {
            i = 0;
            i2 = i14;
            if (this.mRightBorderActive) {
                i2 = Math.max(-(layoutParams.cellHSpan - this.mMinHSpan), Math.min(countX - (i18 + i16), i14));
                i20 = i2;
                i = 0;
            }
        }
        if (this.mTopBorderActive) {
            i3 = Math.min(layoutParams.cellVSpan - this.mMinVSpan, Math.max(-i19, i15));
            i5 = Math.max(-(layoutParams.cellVSpan - this.mMinVSpan), Math.min(i19, i15 * (-1)));
            i4 = -i5;
        } else {
            i3 = 0;
            i4 = 0;
            i5 = i15;
            if (this.mBottomBorderActive) {
                i5 = Math.max(-(layoutParams.cellVSpan - this.mMinVSpan), Math.min(countY - (i19 + i17), i15));
                i4 = i5;
                i3 = 0;
            }
        }
        this.mDirectionVector[0] = 0;
        this.mDirectionVector[1] = 0;
        if (!this.mLeftBorderActive) {
            i6 = i18;
            i7 = i16;
        }
        int i21 = i16 + i2;
        int i22 = i18 + i;
        i6 = i22;
        i7 = i21;
        if (i20 != 0) {
            this.mDirectionVector[0] = this.mLeftBorderActive ? -1 : 1;
            i7 = i21;
            i6 = i22;
        }
        if (!this.mTopBorderActive) {
            i8 = i19;
            i9 = i17;
        }
        int i23 = i17 + i5;
        int i24 = i19 + i3;
        i8 = i24;
        i9 = i23;
        if (i4 != 0) {
            this.mDirectionVector[1] = this.mTopBorderActive ? -1 : 1;
            i9 = i23;
            i8 = i24;
        }
        if (!z && i4 == 0 && i20 == 0) {
            return;
        }
        if (z) {
            this.mDirectionVector[0] = this.mLastDirectionVector[0];
            this.mDirectionVector[1] = this.mLastDirectionVector[1];
        } else {
            this.mLastDirectionVector[0] = this.mDirectionVector[0];
            this.mLastDirectionVector[1] = this.mDirectionVector[1];
        }
        if (this.mCellLayout.createAreaForResize(i6, i8, i7, i9, this.mWidgetView, this.mDirectionVector, z)) {
            if (this.mStateAnnouncer != null && (layoutParams.cellHSpan != i7 || layoutParams.cellVSpan != i9)) {
                this.mStateAnnouncer.announce(this.mLauncher.getString(2131558488, new Object[]{Integer.valueOf(i7), Integer.valueOf(i9)}));
            }
            layoutParams.tmpCellX = i6;
            layoutParams.tmpCellY = i8;
            layoutParams.cellHSpan = i7;
            layoutParams.cellVSpan = i9;
            this.mRunningVInc += i4;
            this.mRunningHInc += i20;
            if (!z) {
                updateWidgetSizeRanges(this.mWidgetView, this.mLauncher, i7, i9);
            }
        }
        this.mWidgetView.requestLayout();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void updateWidgetSizeRanges(AppWidgetHostView appWidgetHostView, Launcher launcher, int i, int i2) {
        getWidgetSizeRanges(launcher, i, i2, sTmpRect);
        appWidgetHostView.updateAppWidgetSize(null, sTmpRect.left, sTmpRect.top, sTmpRect.right, sTmpRect.bottom);
    }

    private void visualizeResizeForDelta(int i, int i2, boolean z) {
        updateDeltas(i, i2);
        DragLayer.LayoutParams layoutParams = (DragLayer.LayoutParams) getLayoutParams();
        if (this.mLeftBorderActive) {
            layoutParams.x = this.mBaselineX + this.mDeltaX;
            layoutParams.width = this.mBaselineWidth - this.mDeltaX;
        } else if (this.mRightBorderActive) {
            layoutParams.width = this.mBaselineWidth + this.mDeltaX;
        }
        if (this.mTopBorderActive) {
            layoutParams.y = this.mBaselineY + this.mDeltaY;
            layoutParams.height = this.mBaselineHeight - this.mDeltaY;
        } else if (this.mBottomBorderActive) {
            layoutParams.height = this.mBaselineHeight + this.mDeltaY;
        }
        resizeWidgetIfNeeded(z);
        requestLayout();
    }

    public boolean beginResizeIfPointInRegion(int i, int i2) {
        boolean z = (this.mResizeMode & 1) != 0;
        boolean z2 = (this.mResizeMode & 2) != 0;
        this.mLeftBorderActive = i < this.mTouchTargetWidth ? z : false;
        if (i <= getWidth() - this.mTouchTargetWidth) {
            z = false;
        }
        this.mRightBorderActive = z;
        this.mTopBorderActive = i2 < this.mTouchTargetWidth + this.mTopTouchRegionAdjustment ? z2 : false;
        if (i2 <= (getHeight() - this.mTouchTargetWidth) + this.mBottomTouchRegionAdjustment) {
            z2 = false;
        }
        this.mBottomBorderActive = z2;
        boolean z3 = (this.mLeftBorderActive || this.mRightBorderActive || this.mTopBorderActive) ? true : this.mBottomBorderActive;
        this.mBaselineWidth = getMeasuredWidth();
        this.mBaselineHeight = getMeasuredHeight();
        this.mBaselineX = getLeft();
        this.mBaselineY = getTop();
        if (z3) {
            this.mLeftHandle.setAlpha(this.mLeftBorderActive ? 1.0f : 0.0f);
            this.mRightHandle.setAlpha(this.mRightBorderActive ? 1.0f : 0.0f);
            this.mTopHandle.setAlpha(this.mTopBorderActive ? 1.0f : 0.0f);
            this.mBottomHandle.setAlpha(this.mBottomBorderActive ? 1.0f : 0.0f);
        }
        return z3;
    }

    public void commitResize() {
        resizeWidgetIfNeeded(true);
        requestLayout();
    }

    @Override // android.view.View.OnKeyListener
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        if (FocusLogic.shouldConsume(i)) {
            this.mDragLayer.clearAllResizeFrames();
            this.mWidgetView.requestFocus();
            return true;
        }
        return false;
    }

    public void onTouchUp() {
        int cellWidth = this.mCellLayout.getCellWidth();
        int widthGap = this.mCellLayout.getWidthGap();
        int cellHeight = this.mCellLayout.getCellHeight();
        int heightGap = this.mCellLayout.getHeightGap();
        this.mDeltaXAddOn = this.mRunningHInc * (cellWidth + widthGap);
        this.mDeltaYAddOn = this.mRunningVInc * (cellHeight + heightGap);
        this.mDeltaX = 0;
        this.mDeltaY = 0;
        post(new Runnable(this) { // from class: com.android.launcher3.AppWidgetResizeFrame.1
            final AppWidgetResizeFrame this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.snapToWidget(true);
            }
        });
    }

    public void snapToWidget(boolean z) {
        DragLayer.LayoutParams layoutParams = (DragLayer.LayoutParams) getLayoutParams();
        int width = ((this.mWidgetView.getWidth() + (this.mBackgroundPadding * 2)) - this.mWidgetPadding.left) - this.mWidgetPadding.right;
        int height = ((this.mWidgetView.getHeight() + (this.mBackgroundPadding * 2)) - this.mWidgetPadding.top) - this.mWidgetPadding.bottom;
        this.mTmpPt[0] = this.mWidgetView.getLeft();
        this.mTmpPt[1] = this.mWidgetView.getTop();
        this.mDragLayer.getDescendantCoordRelativeToSelf(this.mCellLayout.getShortcutsAndWidgets(), this.mTmpPt);
        int i = (this.mTmpPt[0] - this.mBackgroundPadding) + this.mWidgetPadding.left;
        int i2 = (this.mTmpPt[1] - this.mBackgroundPadding) + this.mWidgetPadding.top;
        if (i2 < 0) {
            this.mTopTouchRegionAdjustment = -i2;
        } else {
            this.mTopTouchRegionAdjustment = 0;
        }
        if (i2 + height > this.mDragLayer.getHeight()) {
            this.mBottomTouchRegionAdjustment = -((i2 + height) - this.mDragLayer.getHeight());
        } else {
            this.mBottomTouchRegionAdjustment = 0;
        }
        if (z) {
            ObjectAnimator ofPropertyValuesHolder = LauncherAnimUtils.ofPropertyValuesHolder(layoutParams, this, PropertyValuesHolder.ofInt("width", layoutParams.width, width), PropertyValuesHolder.ofInt("height", layoutParams.height, height), PropertyValuesHolder.ofInt("x", layoutParams.x, i), PropertyValuesHolder.ofInt("y", layoutParams.y, i2));
            ObjectAnimator ofFloat = LauncherAnimUtils.ofFloat(this.mLeftHandle, "alpha", 1.0f);
            ObjectAnimator ofFloat2 = LauncherAnimUtils.ofFloat(this.mRightHandle, "alpha", 1.0f);
            ObjectAnimator ofFloat3 = LauncherAnimUtils.ofFloat(this.mTopHandle, "alpha", 1.0f);
            ObjectAnimator ofFloat4 = LauncherAnimUtils.ofFloat(this.mBottomHandle, "alpha", 1.0f);
            ofPropertyValuesHolder.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this) { // from class: com.android.launcher3.AppWidgetResizeFrame.2
                final AppWidgetResizeFrame this$0;

                {
                    this.this$0 = this;
                }

                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    this.this$0.requestLayout();
                }
            });
            AnimatorSet createAnimatorSet = LauncherAnimUtils.createAnimatorSet();
            if (this.mResizeMode == 2) {
                createAnimatorSet.playTogether(ofPropertyValuesHolder, ofFloat3, ofFloat4);
            } else if (this.mResizeMode == 1) {
                createAnimatorSet.playTogether(ofPropertyValuesHolder, ofFloat, ofFloat2);
            } else {
                createAnimatorSet.playTogether(ofPropertyValuesHolder, ofFloat, ofFloat2, ofFloat3, ofFloat4);
            }
            createAnimatorSet.setDuration(150L);
            createAnimatorSet.start();
        } else {
            layoutParams.width = width;
            layoutParams.height = height;
            layoutParams.x = i;
            layoutParams.y = i2;
            this.mLeftHandle.setAlpha(1.0f);
            this.mRightHandle.setAlpha(1.0f);
            this.mTopHandle.setAlpha(1.0f);
            this.mBottomHandle.setAlpha(1.0f);
            requestLayout();
        }
        setFocusableInTouchMode(true);
        requestFocus();
    }

    public void updateDeltas(int i, int i2) {
        if (this.mLeftBorderActive) {
            this.mDeltaX = Math.max(-this.mBaselineX, i);
            this.mDeltaX = Math.min(this.mBaselineWidth - (this.mTouchTargetWidth * 2), this.mDeltaX);
        } else if (this.mRightBorderActive) {
            this.mDeltaX = Math.min(this.mDragLayer.getWidth() - (this.mBaselineX + this.mBaselineWidth), i);
            this.mDeltaX = Math.max((-this.mBaselineWidth) + (this.mTouchTargetWidth * 2), this.mDeltaX);
        }
        if (this.mTopBorderActive) {
            this.mDeltaY = Math.max(-this.mBaselineY, i2);
            this.mDeltaY = Math.min(this.mBaselineHeight - (this.mTouchTargetWidth * 2), this.mDeltaY);
        } else if (this.mBottomBorderActive) {
            this.mDeltaY = Math.min(this.mDragLayer.getHeight() - (this.mBaselineY + this.mBaselineHeight), i2);
            this.mDeltaY = Math.max((-this.mBaselineHeight) + (this.mTouchTargetWidth * 2), this.mDeltaY);
        }
    }

    public void visualizeResizeForDelta(int i, int i2) {
        visualizeResizeForDelta(i, i2, false);
    }
}
