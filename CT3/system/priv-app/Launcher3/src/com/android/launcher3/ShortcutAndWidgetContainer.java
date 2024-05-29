package com.android.launcher3;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import com.android.launcher3.CellLayout;
/* loaded from: a.zip:com/android/launcher3/ShortcutAndWidgetContainer.class */
public class ShortcutAndWidgetContainer extends ViewGroup {
    private int mCellHeight;
    private int mCellWidth;
    private int mCountX;
    private int mCountY;
    private int mHeightGap;
    private boolean mInvertIfRtl;
    private boolean mIsHotseatLayout;
    private Launcher mLauncher;
    private final int[] mTmpCellXY;
    private final WallpaperManager mWallpaperManager;
    private int mWidthGap;

    public ShortcutAndWidgetContainer(Context context) {
        super(context);
        this.mTmpCellXY = new int[2];
        this.mInvertIfRtl = false;
        this.mLauncher = (Launcher) context;
        this.mWallpaperManager = WallpaperManager.getInstance(context);
    }

    @Override // android.view.View
    public void cancelLongPress() {
        super.cancelLongPress();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            getChildAt(i).cancelLongPress();
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getCellContentHeight() {
        DeviceProfile deviceProfile = this.mLauncher.getDeviceProfile();
        return Math.min(getMeasuredHeight(), this.mIsHotseatLayout ? deviceProfile.hotseatCellHeightPx : deviceProfile.cellHeightPx);
    }

    public View getChildAt(int i, int i2) {
        int childCount = getChildCount();
        for (int i3 = 0; i3 < childCount; i3++) {
            View childAt = getChildAt(i3);
            CellLayout.LayoutParams layoutParams = (CellLayout.LayoutParams) childAt.getLayoutParams();
            if (layoutParams.cellX <= i && i < layoutParams.cellX + layoutParams.cellHSpan && layoutParams.cellY <= i2 && i2 < layoutParams.cellY + layoutParams.cellVSpan) {
                return childAt;
            }
        }
        return null;
    }

    public boolean invertLayoutHorizontally() {
        return this.mInvertIfRtl ? Utilities.isRtl(getResources()) : false;
    }

    public void measureChild(View view) {
        DeviceProfile deviceProfile = this.mLauncher.getDeviceProfile();
        int i = this.mCellWidth;
        int i2 = this.mCellHeight;
        CellLayout.LayoutParams layoutParams = (CellLayout.LayoutParams) view.getLayoutParams();
        if (layoutParams.isFullscreen) {
            layoutParams.x = 0;
            layoutParams.y = 0;
            layoutParams.width = getMeasuredWidth();
            layoutParams.height = getMeasuredHeight();
        } else {
            layoutParams.setup(i, i2, this.mWidthGap, this.mHeightGap, invertLayoutHorizontally(), this.mCountX);
            if (!(view instanceof LauncherAppWidgetHostView)) {
                int max = (int) Math.max(0.0f, (layoutParams.height - getCellContentHeight()) / 2.0f);
                int i3 = (int) (deviceProfile.edgeMarginPx / 2.0f);
                view.setPadding(i3, max, i3, 0);
            }
        }
        view.measure(View.MeasureSpec.makeMeasureSpec(layoutParams.width, 1073741824), View.MeasureSpec.makeMeasureSpec(layoutParams.height, 1073741824));
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int childCount = getChildCount();
        for (int i5 = 0; i5 < childCount; i5++) {
            View childAt = getChildAt(i5);
            if (childAt.getVisibility() != 8) {
                CellLayout.LayoutParams layoutParams = (CellLayout.LayoutParams) childAt.getLayoutParams();
                int i6 = layoutParams.x;
                int i7 = layoutParams.y;
                childAt.layout(i6, i7, layoutParams.width + i6, layoutParams.height + i7);
                if (layoutParams.dropped) {
                    layoutParams.dropped = false;
                    int[] iArr = this.mTmpCellXY;
                    getLocationOnScreen(iArr);
                    this.mWallpaperManager.sendWallpaperCommand(getWindowToken(), "android.home.drop", iArr[0] + i6 + (layoutParams.width / 2), iArr[1] + i7 + (layoutParams.height / 2), 0, null);
                }
            }
        }
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        int childCount = getChildCount();
        setMeasuredDimension(View.MeasureSpec.getSize(i), View.MeasureSpec.getSize(i2));
        for (int i3 = 0; i3 < childCount; i3++) {
            View childAt = getChildAt(i3);
            if (childAt.getVisibility() != 8) {
                measureChild(childAt);
            }
        }
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void requestChildFocus(View view, View view2) {
        super.requestChildFocus(view, view2);
        if (view != null) {
            Rect rect = new Rect();
            view.getDrawingRect(rect);
            requestRectangleOnScreen(rect);
        }
    }

    public void setCellDimensions(int i, int i2, int i3, int i4, int i5, int i6) {
        this.mCellWidth = i;
        this.mCellHeight = i2;
        this.mWidthGap = i3;
        this.mHeightGap = i4;
        this.mCountX = i5;
        this.mCountY = i6;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public void setChildrenDrawingCacheEnabled(boolean z) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            childAt.setDrawingCacheEnabled(z);
            if (!childAt.isHardwareAccelerated() && z) {
                childAt.buildDrawingCache(true);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public void setChildrenDrawnWithCacheEnabled(boolean z) {
        super.setChildrenDrawnWithCacheEnabled(z);
    }

    public void setInvertIfRtl(boolean z) {
        this.mInvertIfRtl = z;
    }

    public void setIsHotseat(boolean z) {
        this.mIsHotseatLayout = z;
    }

    public void setupLp(CellLayout.LayoutParams layoutParams) {
        layoutParams.setup(this.mCellWidth, this.mCellHeight, this.mWidthGap, this.mHeightGap, invertLayoutHorizontally(), this.mCountX);
    }

    @Override // android.view.ViewGroup
    public boolean shouldDelayChildPressedState() {
        return false;
    }
}
