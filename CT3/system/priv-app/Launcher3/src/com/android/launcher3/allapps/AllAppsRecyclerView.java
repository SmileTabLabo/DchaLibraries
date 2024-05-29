package com.android.launcher3.allapps;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import com.android.launcher3.BaseRecyclerView;
import com.android.launcher3.BubbleTextView;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.Stats;
import com.android.launcher3.Utilities;
import com.android.launcher3.allapps.AlphabeticalAppsList;
import java.util.List;
/* loaded from: a.zip:com/android/launcher3/allapps/AllAppsRecyclerView.class */
public class AllAppsRecyclerView extends BaseRecyclerView implements Stats.LaunchSourceProvider {
    private AlphabeticalAppsList mApps;
    private HeaderElevationController mElevationController;
    private AllAppsBackgroundDrawable mEmptySearchBackground;
    private int mEmptySearchBackgroundTopOffset;
    private AllAppsFastScrollHelper mFastScrollHelper;
    private int mIconHeight;
    private int mNumAppsPerRow;
    private int mPredictionIconHeight;
    private BaseRecyclerView.ScrollPositionState mScrollPosState;

    public AllAppsRecyclerView(Context context) {
        this(context, null);
    }

    public AllAppsRecyclerView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public AllAppsRecyclerView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public AllAppsRecyclerView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i);
        this.mScrollPosState = new BaseRecyclerView.ScrollPositionState();
        Resources resources = getResources();
        addOnItemTouchListener(this);
        this.mScrollbar.setDetachThumbOnFastScroll();
        this.mEmptySearchBackgroundTopOffset = resources.getDimensionPixelSize(2131230775);
    }

    private void updateEmptySearchBackgroundBounds() {
        if (this.mEmptySearchBackground == null) {
            return;
        }
        int measuredWidth = (getMeasuredWidth() - this.mEmptySearchBackground.getIntrinsicWidth()) / 2;
        int i = this.mEmptySearchBackgroundTopOffset;
        this.mEmptySearchBackground.setBounds(measuredWidth, i, this.mEmptySearchBackground.getIntrinsicWidth() + measuredWidth, this.mEmptySearchBackground.getIntrinsicHeight() + i);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.launcher3.BaseRecyclerView, android.view.ViewGroup, android.view.View
    public void dispatchDraw(Canvas canvas) {
        canvas.clipRect(this.mBackgroundPadding.left, this.mBackgroundPadding.top, getWidth() - this.mBackgroundPadding.right, getHeight() - this.mBackgroundPadding.bottom);
        super.dispatchDraw(canvas);
    }

    @Override // com.android.launcher3.Stats.LaunchSourceProvider
    public void fillInLaunchSourceData(View view, Bundle bundle) {
        int childPosition;
        bundle.putString("container", "all_apps");
        if (this.mApps.hasFilter()) {
            bundle.putString("sub_container", "search");
        } else if ((view instanceof BubbleTextView) && (childPosition = getChildPosition((BubbleTextView) view)) != -1 && this.mApps.getAdapterItems().get(childPosition).viewType == 2) {
            bundle.putString("sub_container", "prediction");
        } else {
            bundle.putString("sub_container", "a-z");
        }
    }

    protected void getCurScrollState(BaseRecyclerView.ScrollPositionState scrollPositionState, int i) {
        scrollPositionState.rowIndex = -1;
        scrollPositionState.rowTopOffset = -1;
        scrollPositionState.itemPos = -1;
        List<AlphabeticalAppsList.AdapterItem> adapterItems = this.mApps.getAdapterItems();
        if (adapterItems.isEmpty() || this.mNumAppsPerRow == 0) {
            return;
        }
        int childCount = getChildCount();
        for (int i2 = 0; i2 < childCount; i2++) {
            View childAt = getChildAt(i2);
            int childPosition = getChildPosition(childAt);
            if (childPosition != -1) {
                AlphabeticalAppsList.AdapterItem adapterItem = adapterItems.get(childPosition);
                if ((adapterItem.viewType & i) != 0) {
                    scrollPositionState.rowIndex = adapterItem.rowIndex;
                    scrollPositionState.rowTopOffset = getLayoutManager().getDecoratedTop(childAt);
                    scrollPositionState.itemPos = childPosition;
                    return;
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.launcher3.BaseRecyclerView
    public int getTop(int i) {
        if (getChildCount() == 0 || i <= 0) {
            return 0;
        }
        return this.mPredictionIconHeight + ((i - 1) * this.mIconHeight);
    }

    @Override // android.support.v7.widget.RecyclerView, android.view.View
    public void onDraw(Canvas canvas) {
        if (this.mEmptySearchBackground != null && this.mEmptySearchBackground.getAlpha() > 0) {
            canvas.clipRect(this.mBackgroundPadding.left, this.mBackgroundPadding.top, getWidth() - this.mBackgroundPadding.right, getHeight() - this.mBackgroundPadding.bottom);
            this.mEmptySearchBackground.draw(canvas);
        }
        super.onDraw(canvas);
    }

    @Override // com.android.launcher3.BaseRecyclerView
    public void onFastScrollCompleted() {
        super.onFastScrollCompleted();
        this.mFastScrollHelper.onFastScrollCompleted();
    }

    public void onSearchResultsChanged() {
        scrollToTop();
        if (!this.mApps.hasNoFilteredResults()) {
            if (this.mEmptySearchBackground != null) {
                this.mEmptySearchBackground.setBgAlpha(0.0f);
                return;
            }
            return;
        }
        if (this.mEmptySearchBackground == null) {
            this.mEmptySearchBackground = new AllAppsBackgroundDrawable(getContext());
            this.mEmptySearchBackground.setAlpha(0);
            this.mEmptySearchBackground.setCallback(this);
            updateEmptySearchBackgroundBounds();
        }
        this.mEmptySearchBackground.animateBgAlpha(1.0f, 150);
    }

    @Override // android.support.v7.widget.RecyclerView, android.view.View
    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        updateEmptySearchBackgroundBounds();
    }

    @Override // com.android.launcher3.BaseRecyclerView
    public void onUpdateScrollbar(int i) {
        if (this.mApps.getAdapterItems().isEmpty() || this.mNumAppsPerRow == 0) {
            this.mScrollbar.setThumbOffset(-1, -1);
            return;
        }
        int numAppRows = this.mApps.getNumAppRows();
        getCurScrollState(this.mScrollPosState, -1);
        if (this.mScrollPosState.rowIndex < 0) {
            this.mScrollbar.setThumbOffset(-1, -1);
            return;
        }
        int availableScrollBarHeight = getAvailableScrollBarHeight();
        int availableScrollHeight = getAvailableScrollHeight(this.mApps.getNumAppRows());
        if (availableScrollHeight <= 0) {
            this.mScrollbar.setThumbOffset(-1, -1);
            return;
        }
        int scrollTop = this.mBackgroundPadding.top + ((int) ((getScrollTop(this.mScrollPosState) / availableScrollHeight) * availableScrollBarHeight));
        if (!this.mScrollbar.isThumbDetached()) {
            synchronizeScrollBarThumbOffsetToViewScroll(this.mScrollPosState, numAppRows);
            return;
        }
        int width = Utilities.isRtl(getResources()) ? this.mBackgroundPadding.left : (getWidth() - this.mBackgroundPadding.right) - this.mScrollbar.getThumbWidth();
        if (this.mScrollbar.isDraggingThumb()) {
            this.mScrollbar.setThumbOffset(width, (int) this.mScrollbar.getLastTouchY());
            return;
        }
        int i2 = this.mScrollbar.getThumbOffset().y;
        int i3 = scrollTop - i2;
        if (i3 * i <= 0.0f) {
            this.mScrollbar.setThumbOffset(width, i2);
            return;
        }
        int max = Math.max(0, Math.min(availableScrollBarHeight, i < 0 ? i2 + Math.max((int) ((i * i2) / scrollTop), i3) : i2 + Math.min((int) (((availableScrollBarHeight - i2) * i) / (availableScrollBarHeight - scrollTop)), i3)));
        this.mScrollbar.setThumbOffset(width, max);
        if (scrollTop == max) {
            this.mScrollbar.reattachThumbToScroll();
        }
    }

    @Override // com.android.launcher3.BaseRecyclerView
    public String scrollToPositionAtProgress(float f) {
        if (this.mApps.getNumAppRows() == 0) {
            return "";
        }
        stopScroll();
        List<AlphabeticalAppsList.FastScrollSectionInfo> fastScrollerSections = this.mApps.getFastScrollerSections();
        AlphabeticalAppsList.FastScrollSectionInfo fastScrollSectionInfo = fastScrollerSections.get(0);
        for (int i = 1; i < fastScrollerSections.size(); i++) {
            AlphabeticalAppsList.FastScrollSectionInfo fastScrollSectionInfo2 = fastScrollerSections.get(i);
            if (fastScrollSectionInfo2.touchFraction > f) {
                break;
            }
            fastScrollSectionInfo = fastScrollSectionInfo2;
        }
        this.mFastScrollHelper.smoothScrollToSection(getScrollTop(this.mScrollPosState), getAvailableScrollHeight(this.mApps.getNumAppRows()), fastScrollSectionInfo);
        return fastScrollSectionInfo.sectionName;
    }

    public void scrollToTop() {
        if (this.mScrollbar.isThumbDetached()) {
            this.mScrollbar.reattachThumbToScroll();
        }
        scrollToPosition(0);
        if (this.mElevationController != null) {
            this.mElevationController.reset();
        }
    }

    @Override // android.support.v7.widget.RecyclerView
    public void setAdapter(RecyclerView.Adapter adapter) {
        super.setAdapter(adapter);
        this.mFastScrollHelper.onSetAdapter((AllAppsGridAdapter) adapter);
    }

    public void setApps(AlphabeticalAppsList alphabeticalAppsList) {
        this.mApps = alphabeticalAppsList;
        this.mFastScrollHelper = new AllAppsFastScrollHelper(this, alphabeticalAppsList);
    }

    public void setElevationController(HeaderElevationController headerElevationController) {
        this.mElevationController = headerElevationController;
    }

    public void setNumAppsPerRow(DeviceProfile deviceProfile, int i) {
        this.mNumAppsPerRow = i;
        RecyclerView.RecycledViewPool recycledViewPool = getRecycledViewPool();
        int ceil = (int) Math.ceil(deviceProfile.availableHeightPx / deviceProfile.allAppsIconSizePx);
        recycledViewPool.setMaxRecycledViews(3, 1);
        recycledViewPool.setMaxRecycledViews(4, 1);
        recycledViewPool.setMaxRecycledViews(5, 1);
        recycledViewPool.setMaxRecycledViews(1, this.mNumAppsPerRow * ceil);
        recycledViewPool.setMaxRecycledViews(2, this.mNumAppsPerRow);
        recycledViewPool.setMaxRecycledViews(0, ceil);
    }

    public void setPremeasuredIconHeights(int i, int i2) {
        this.mPredictionIconHeight = i;
        this.mIconHeight = i2;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.launcher3.BaseRecyclerView
    public boolean supportsFastScrolling() {
        return !this.mApps.hasFilter();
    }

    @Override // android.view.View
    protected boolean verifyDrawable(Drawable drawable) {
        return drawable != this.mEmptySearchBackground ? super.verifyDrawable(drawable) : true;
    }
}
