package com.android.launcher3;

import android.appwidget.AppWidgetHostView;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
/* loaded from: a.zip:com/android/launcher3/DeviceProfile.class */
public class DeviceProfile {
    public int allAppsButtonVisualSize;
    public final int allAppsIconSizePx;
    public final float allAppsIconTextSizeSp;
    public int allAppsNumCols;
    public int allAppsNumPredictiveCols;
    public final int availableHeightPx;
    public final int availableWidthPx;
    public int cellHeightPx;
    public int cellWidthPx;
    private final int defaultPageSpacingPx;
    public final Rect defaultWidgetPadding;
    private int desiredWorkspaceLeftRightMarginPx;
    private float dragViewScale;
    public final int edgeMarginPx;
    public int folderBackgroundOffset;
    public int folderCellHeightPx;
    public int folderCellWidthPx;
    public int folderIconSizePx;
    public final int heightPx;
    private int hotseatBarHeightPx;
    public int hotseatCellHeightPx;
    public int hotseatCellWidthPx;
    public int hotseatIconSizePx;
    public int iconDrawablePaddingOriginalPx;
    public int iconDrawablePaddingPx;
    public int iconSizePx;
    public int iconTextSizePx;
    public final InvariantDeviceProfile inv;
    public final boolean isLandscape;
    public final boolean isLargeTablet;
    public final boolean isPhone;
    public final boolean isTablet;
    private int normalHotseatBarHeightPx;
    private int normalSearchBarBottomPaddingPx;
    private int normalSearchBarSpaceHeightPx;
    private int normalSearchBarTopExtraPaddingPx;
    private final int overviewModeBarItemWidthPx;
    private final int overviewModeBarSpacerWidthPx;
    private final float overviewModeIconZoneRatio;
    private final int overviewModeMaxIconZoneHeightPx;
    private final int overviewModeMinIconZoneHeightPx;
    private final int pageIndicatorHeightPx;
    private int searchBarBottomPaddingPx;
    private int searchBarSpaceHeightPx;
    private int searchBarTopExtraPaddingPx;
    private int searchBarTopPaddingPx;
    private int searchBarWidgetInternalPaddingBottom;
    private int searchBarWidgetInternalPaddingTop;
    private int shortHotseatBarHeightPx;
    private int tallSearchBarBottomPaddingPx;
    private int tallSearchBarNegativeTopPaddingPx;
    private int tallSearchBarSpaceHeightPx;
    public final boolean transposeLayoutWithOrientation;
    public final int widthPx;

    public DeviceProfile(Context context, InvariantDeviceProfile invariantDeviceProfile, Point point, Point point2, int i, int i2, boolean z) {
        this.inv = invariantDeviceProfile;
        this.isLandscape = z;
        Resources resources = context.getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        this.isTablet = resources.getBoolean(2131492865);
        this.isLargeTablet = resources.getBoolean(2131492866);
        this.isPhone = this.isTablet ? false : !this.isLargeTablet;
        this.transposeLayoutWithOrientation = resources.getBoolean(2131492869);
        this.defaultWidgetPadding = AppWidgetHostView.getDefaultPaddingForWidget(context, new ComponentName(context.getPackageName(), getClass().getName()), null);
        this.edgeMarginPx = resources.getDimensionPixelSize(2131230724);
        this.desiredWorkspaceLeftRightMarginPx = this.edgeMarginPx * 2;
        this.pageIndicatorHeightPx = resources.getDimensionPixelSize(2131230734);
        this.defaultPageSpacingPx = resources.getDimensionPixelSize(2131230736);
        this.overviewModeMinIconZoneHeightPx = resources.getDimensionPixelSize(2131230737);
        this.overviewModeMaxIconZoneHeightPx = resources.getDimensionPixelSize(2131230738);
        this.overviewModeBarItemWidthPx = resources.getDimensionPixelSize(2131230739);
        this.overviewModeBarSpacerWidthPx = resources.getDimensionPixelSize(2131230740);
        this.overviewModeIconZoneRatio = resources.getInteger(2131427328) / 100.0f;
        this.iconDrawablePaddingOriginalPx = resources.getDimensionPixelSize(2131230735);
        this.allAppsIconTextSizeSp = invariantDeviceProfile.iconTextSize;
        this.allAppsIconSizePx = Utilities.pxFromDp(invariantDeviceProfile.iconSize, displayMetrics);
        this.widthPx = i;
        this.heightPx = i2;
        if (z) {
            this.availableWidthPx = point2.x;
            this.availableHeightPx = point.y;
        } else {
            this.availableWidthPx = point.x;
            this.availableHeightPx = point2.y;
        }
        updateAvailableDimensions(displayMetrics, resources);
        computeAllAppsButtonSize(context);
    }

    public static int calculateCellHeight(int i, int i2) {
        return i / i2;
    }

    public static int calculateCellWidth(int i, int i2) {
        return i / i2;
    }

    private void computeAllAppsButtonSize(Context context) {
        this.allAppsButtonVisualSize = ((int) (this.hotseatIconSizePx * (1.0f - (context.getResources().getInteger(2131427339) / 100.0f)))) - context.getResources().getDimensionPixelSize(2131230763);
    }

    private int getCurrentHeight() {
        return this.isLandscape ? Math.min(this.widthPx, this.heightPx) : Math.max(this.widthPx, this.heightPx);
    }

    private int getCurrentWidth() {
        return this.isLandscape ? Math.max(this.widthPx, this.heightPx) : Math.min(this.widthPx, this.heightPx);
    }

    private int getSearchBarTotalVerticalPadding() {
        return this.searchBarTopPaddingPx + this.searchBarTopExtraPaddingPx + this.searchBarBottomPaddingPx;
    }

    private int getVisibleChildCount(ViewGroup viewGroup) {
        int i = 0;
        int i2 = 0;
        while (i2 < viewGroup.getChildCount()) {
            int i3 = i;
            if (viewGroup.getChildAt(i2).getVisibility() != 8) {
                i3 = i + 1;
            }
            i2++;
            i = i3;
        }
        return i;
    }

    private int getWorkspacePageSpacing(boolean z) {
        return (isVerticalBarLayout() || this.isLargeTablet) ? this.defaultPageSpacingPx : Math.max(this.defaultPageSpacingPx, getWorkspacePadding(z).left * 2);
    }

    private void updateAvailableDimensions(DisplayMetrics displayMetrics, Resources resources) {
        float f = 1.0f;
        int i = this.iconDrawablePaddingOriginalPx;
        updateIconSize(1.0f, i, resources, displayMetrics);
        float f2 = this.cellHeightPx * this.inv.numRows;
        Rect workspacePadding = getWorkspacePadding(false);
        int i2 = (this.availableHeightPx - workspacePadding.top) - workspacePadding.bottom;
        if (f2 > i2) {
            f = i2 / f2;
            i = 0;
        }
        updateIconSize(f, i, resources, displayMetrics);
    }

    private void updateIconSize(float f, int i, Resources resources, DisplayMetrics displayMetrics) {
        this.iconSizePx = (int) (Utilities.pxFromDp(this.inv.iconSize, displayMetrics) * f);
        this.iconTextSizePx = (int) (Utilities.pxFromSp(this.inv.iconTextSize, displayMetrics) * f);
        this.iconDrawablePaddingPx = i;
        this.hotseatIconSizePx = (int) (Utilities.pxFromDp(this.inv.hotseatIconSize, displayMetrics) * f);
        this.normalSearchBarSpaceHeightPx = resources.getDimensionPixelSize(2131230725);
        this.tallSearchBarSpaceHeightPx = resources.getDimensionPixelSize(2131230726);
        this.searchBarWidgetInternalPaddingTop = resources.getDimensionPixelSize(2131230727);
        this.searchBarWidgetInternalPaddingBottom = resources.getDimensionPixelSize(2131230728);
        this.normalSearchBarTopExtraPaddingPx = resources.getDimensionPixelSize(2131230729);
        this.tallSearchBarNegativeTopPaddingPx = resources.getDimensionPixelSize(2131230730);
        if (!this.isTablet || isVerticalBarLayout()) {
            this.searchBarTopPaddingPx = this.searchBarWidgetInternalPaddingTop;
            this.normalSearchBarBottomPaddingPx = this.searchBarWidgetInternalPaddingBottom + resources.getDimensionPixelSize(2131230731);
            this.tallSearchBarBottomPaddingPx = this.searchBarWidgetInternalPaddingBottom + resources.getDimensionPixelSize(2131230732);
        } else {
            this.searchBarTopPaddingPx = this.searchBarWidgetInternalPaddingTop;
            this.normalSearchBarBottomPaddingPx = this.searchBarWidgetInternalPaddingBottom + resources.getDimensionPixelSize(2131230733);
            this.tallSearchBarBottomPaddingPx = this.normalSearchBarBottomPaddingPx;
        }
        Paint paint = new Paint();
        paint.setTextSize(this.iconTextSizePx);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        this.cellWidthPx = this.iconSizePx;
        this.cellHeightPx = this.iconSizePx + this.iconDrawablePaddingPx + ((int) Math.ceil(fontMetrics.bottom - fontMetrics.top));
        this.dragViewScale = (this.iconSizePx + resources.getDimensionPixelSize(2131230799)) / this.iconSizePx;
        this.normalHotseatBarHeightPx = this.iconSizePx + (this.edgeMarginPx * 4);
        this.shortHotseatBarHeightPx = this.iconSizePx + (this.edgeMarginPx * 2);
        this.hotseatCellWidthPx = this.iconSizePx;
        this.hotseatCellHeightPx = this.iconSizePx;
        this.folderCellWidthPx = Math.min(this.cellWidthPx + ((this.isTablet || this.isLandscape) ? this.edgeMarginPx * 6 : this.edgeMarginPx * 3), (this.availableWidthPx - (this.edgeMarginPx * 4)) / this.inv.numFolderColumns);
        this.folderCellHeightPx = this.cellHeightPx + this.edgeMarginPx;
        this.folderBackgroundOffset = -this.edgeMarginPx;
        this.folderIconSizePx = this.iconSizePx + ((-this.folderBackgroundOffset) * 2);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Rect getHotseatRect() {
        return isVerticalBarLayout() ? new Rect(this.availableWidthPx - this.normalHotseatBarHeightPx, 0, Integer.MAX_VALUE, this.availableHeightPx) : new Rect(0, this.availableHeightPx - this.hotseatBarHeightPx, this.availableWidthPx, Integer.MAX_VALUE);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getOverviewModeButtonBarHeight() {
        return Math.min(this.overviewModeMaxIconZoneHeightPx, Math.max(this.overviewModeMinIconZoneHeightPx, (int) (this.overviewModeIconZoneRatio * this.availableHeightPx)));
    }

    public Rect getSearchBarBounds(boolean z) {
        Rect rect = new Rect();
        if (!isVerticalBarLayout()) {
            int searchBarTotalVerticalPadding = this.searchBarSpaceHeightPx + getSearchBarTotalVerticalPadding();
            if (this.isTablet) {
                int currentWidth = ((getCurrentWidth() - (this.edgeMarginPx * 2)) - (this.inv.numColumns * this.cellWidthPx)) / ((this.inv.numColumns + 1) * 2);
                rect.set(this.edgeMarginPx + currentWidth, 0, this.availableWidthPx - (this.edgeMarginPx + currentWidth), searchBarTotalVerticalPadding);
            } else {
                rect.set(this.desiredWorkspaceLeftRightMarginPx - this.defaultWidgetPadding.left, 0, this.availableWidthPx - (this.desiredWorkspaceLeftRightMarginPx - this.defaultWidgetPadding.right), searchBarTotalVerticalPadding);
            }
        } else if (z) {
            rect.set(this.availableWidthPx - this.normalSearchBarSpaceHeightPx, this.edgeMarginPx, this.availableWidthPx, this.availableHeightPx - this.edgeMarginPx);
        } else {
            rect.set(0, this.edgeMarginPx, this.normalSearchBarSpaceHeightPx, this.availableHeightPx - this.edgeMarginPx);
        }
        return rect;
    }

    public Point getSearchBarDimensForWidgetOpts(Resources resources) {
        Rect searchBarBounds = getSearchBarBounds(Utilities.isRtl(resources));
        if (isVerticalBarLayout()) {
            return new Point(searchBarBounds.width(), searchBarBounds.height());
        }
        return new Point(searchBarBounds.width(), this.searchBarSpaceHeightPx + this.searchBarWidgetInternalPaddingTop + this.searchBarWidgetInternalPaddingBottom);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Rect getWorkspacePadding(boolean z) {
        Rect searchBarBounds = getSearchBarBounds(z);
        Rect rect = new Rect();
        if (!isVerticalBarLayout()) {
            int i = searchBarBounds.bottom;
            int i2 = this.hotseatBarHeightPx + this.pageIndicatorHeightPx;
            if (this.isTablet) {
                float f = (this.dragViewScale - 1.0f) / 2.0f;
                int currentWidth = getCurrentWidth();
                int currentHeight = getCurrentHeight();
                int min = (int) Math.min(Math.max(0, currentWidth - ((int) ((this.inv.numColumns * this.cellWidthPx) + (((this.inv.numColumns - 1) * (1.0f + f)) * this.cellWidthPx)))), currentWidth * 0.14f);
                int max = Math.max(0, ((currentHeight - i) - i2) - ((this.inv.numRows * 2) * this.cellHeightPx));
                rect.set(min / 2, (max / 2) + i, min / 2, (max / 2) + i2);
            } else {
                rect.set(this.desiredWorkspaceLeftRightMarginPx - this.defaultWidgetPadding.left, i, this.desiredWorkspaceLeftRightMarginPx - this.defaultWidgetPadding.right, i2);
            }
        } else if (z) {
            rect.set(this.normalHotseatBarHeightPx, this.edgeMarginPx, searchBarBounds.width(), this.edgeMarginPx);
        } else {
            rect.set(searchBarBounds.width(), this.edgeMarginPx, this.normalHotseatBarHeightPx, this.edgeMarginPx);
        }
        return rect;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isVerticalBarLayout() {
        return this.isLandscape ? this.transposeLayoutWithOrientation : false;
    }

    public void layout(Launcher launcher) {
        boolean isVerticalBarLayout = isVerticalBarLayout();
        boolean isRtl = Utilities.isRtl(launcher.getResources());
        Rect searchBarBounds = getSearchBarBounds(isRtl);
        SearchDropTargetBar searchDropTargetBar = launcher.getSearchDropTargetBar();
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) searchDropTargetBar.getLayoutParams();
        layoutParams.width = searchBarBounds.width();
        layoutParams.height = searchBarBounds.height();
        layoutParams.topMargin = this.searchBarTopExtraPaddingPx;
        if (isVerticalBarLayout) {
            layoutParams.gravity = 3;
            LinearLayout linearLayout = (LinearLayout) searchDropTargetBar.findViewById(2131296307);
            linearLayout.setOrientation(1);
            FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) linearLayout.getLayoutParams();
            layoutParams2.gravity = 48;
            layoutParams2.height = -2;
        } else {
            layoutParams.gravity = 49;
        }
        searchDropTargetBar.setLayoutParams(layoutParams);
        PagedView pagedView = (PagedView) launcher.findViewById(2131296271);
        FrameLayout.LayoutParams layoutParams3 = (FrameLayout.LayoutParams) pagedView.getLayoutParams();
        layoutParams3.gravity = 17;
        Rect workspacePadding = getWorkspacePadding(isRtl);
        pagedView.setLayoutParams(layoutParams3);
        pagedView.setPadding(workspacePadding.left, workspacePadding.top, workspacePadding.right, workspacePadding.bottom);
        pagedView.setPageSpacing(getWorkspacePageSpacing(isRtl));
        View findViewById = launcher.findViewById(2131296291);
        FrameLayout.LayoutParams layoutParams4 = (FrameLayout.LayoutParams) findViewById.getLayoutParams();
        int round = Math.round(((getCurrentWidth() / this.inv.numColumns) - (getCurrentWidth() / this.inv.numHotseatIcons)) / 2.0f);
        if (isVerticalBarLayout) {
            layoutParams4.gravity = 5;
            layoutParams4.width = this.normalHotseatBarHeightPx;
            layoutParams4.height = -1;
            findViewById.findViewById(2131296286).setPadding(0, this.edgeMarginPx * 2, 0, this.edgeMarginPx * 2);
        } else if (this.isTablet) {
            layoutParams4.gravity = 80;
            layoutParams4.width = -1;
            layoutParams4.height = this.hotseatBarHeightPx;
            findViewById.findViewById(2131296286).setPadding(workspacePadding.left + round, 0, workspacePadding.right + round, this.edgeMarginPx * 2);
        } else {
            layoutParams4.gravity = 80;
            layoutParams4.width = -1;
            layoutParams4.height = this.hotseatBarHeightPx;
            findViewById.findViewById(2131296286).setPadding(workspacePadding.left + round, 0, workspacePadding.right + round, 0);
        }
        findViewById.setLayoutParams(layoutParams4);
        View findViewById2 = launcher.findViewById(2131296290);
        if (findViewById2 != null) {
            if (isVerticalBarLayout) {
                findViewById2.setVisibility(8);
            } else {
                FrameLayout.LayoutParams layoutParams5 = (FrameLayout.LayoutParams) findViewById2.getLayoutParams();
                layoutParams5.gravity = 81;
                layoutParams5.width = -2;
                layoutParams5.height = -2;
                layoutParams5.bottomMargin = this.hotseatBarHeightPx;
                findViewById2.setLayoutParams(layoutParams5);
            }
        }
        ViewGroup overviewPanel = launcher.getOverviewPanel();
        if (overviewPanel != null) {
            int overviewModeButtonBarHeight = getOverviewModeButtonBarHeight();
            FrameLayout.LayoutParams layoutParams6 = (FrameLayout.LayoutParams) overviewPanel.getLayoutParams();
            layoutParams6.gravity = 81;
            int visibleChildCount = getVisibleChildCount(overviewPanel);
            int i = visibleChildCount * this.overviewModeBarItemWidthPx;
            layoutParams6.width = Math.min(this.availableWidthPx, i + ((visibleChildCount - 1) * this.overviewModeBarSpacerWidthPx));
            layoutParams6.height = overviewModeButtonBarHeight;
            overviewPanel.setLayoutParams(layoutParams6);
            if (layoutParams6.width <= i || visibleChildCount <= 1) {
                return;
            }
            int i2 = (layoutParams6.width - i) / (visibleChildCount - 1);
            View view = null;
            for (int i3 = 0; i3 < visibleChildCount; i3++) {
                View view2 = view;
                if (view != null) {
                    ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
                    if (isRtl) {
                        marginLayoutParams.leftMargin = i2;
                    } else {
                        marginLayoutParams.rightMargin = i2;
                    }
                    view.setLayoutParams(marginLayoutParams);
                    view2 = null;
                }
                View childAt = overviewPanel.getChildAt(i3);
                view = view2;
                if (childAt.getVisibility() != 8) {
                    view = childAt;
                }
            }
        }
    }

    public void setSearchBarHeight(int i) {
        if (i == 1) {
            this.hotseatBarHeightPx = this.shortHotseatBarHeightPx;
            this.searchBarSpaceHeightPx = this.tallSearchBarSpaceHeightPx;
            this.searchBarBottomPaddingPx = this.tallSearchBarBottomPaddingPx;
            this.searchBarTopExtraPaddingPx = this.isPhone ? this.tallSearchBarNegativeTopPaddingPx : this.normalSearchBarTopExtraPaddingPx;
            return;
        }
        this.hotseatBarHeightPx = this.normalHotseatBarHeightPx;
        this.searchBarSpaceHeightPx = this.normalSearchBarSpaceHeightPx;
        this.searchBarBottomPaddingPx = this.normalSearchBarBottomPaddingPx;
        this.searchBarTopExtraPaddingPx = this.normalSearchBarTopExtraPaddingPx;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean shouldFadeAdjacentWorkspaceScreens() {
        return !isVerticalBarLayout() ? this.isLargeTablet : true;
    }

    public void updateAppsViewNumCols(Resources resources, int i) {
        int dimensionPixelSize = resources.getDimensionPixelSize(2131230764);
        int dimensionPixelSize2 = resources.getDimensionPixelSize(2131230770);
        if (i <= 0) {
            i = this.availableWidthPx;
        }
        int i2 = ((i + dimensionPixelSize2) - dimensionPixelSize) / (this.allAppsIconSizePx + dimensionPixelSize2);
        int max = Math.max(this.inv.minAllAppsPredictionColumns, i2);
        this.allAppsNumCols = i2;
        this.allAppsNumPredictiveCols = max;
    }
}
