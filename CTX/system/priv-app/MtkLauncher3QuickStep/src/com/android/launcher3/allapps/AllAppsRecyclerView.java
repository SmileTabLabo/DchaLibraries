package com.android.launcher3.allapps;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.View;
import com.android.launcher3.BaseRecyclerView;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.R;
import com.android.launcher3.allapps.AlphabeticalAppsList;
import com.android.launcher3.graphics.DrawableFactory;
import com.android.launcher3.logging.UserEventDispatcher;
import com.android.launcher3.userevent.nano.LauncherLogProto;
import com.android.launcher3.views.RecyclerViewFastScroller;
import java.util.List;
/* loaded from: classes.dex */
public class AllAppsRecyclerView extends BaseRecyclerView implements UserEventDispatcher.LogContainerProvider {
    private AlphabeticalAppsList mApps;
    private SparseIntArray mCachedScrollPositions;
    private AllAppsBackgroundDrawable mEmptySearchBackground;
    private int mEmptySearchBackgroundTopOffset;
    private AllAppsFastScrollHelper mFastScrollHelper;
    private final int mNumAppsPerRow;
    private SparseIntArray mViewHeights;

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
        this.mViewHeights = new SparseIntArray();
        this.mCachedScrollPositions = new SparseIntArray();
        this.mEmptySearchBackgroundTopOffset = getResources().getDimensionPixelSize(R.dimen.all_apps_empty_search_bg_top_offset);
        this.mNumAppsPerRow = LauncherAppState.getIDP(context).numColumns;
    }

    public void setApps(AlphabeticalAppsList alphabeticalAppsList, boolean z) {
        this.mApps = alphabeticalAppsList;
        this.mFastScrollHelper = new AllAppsFastScrollHelper(this, alphabeticalAppsList);
    }

    public AlphabeticalAppsList getApps() {
        return this.mApps;
    }

    private void updatePoolSize() {
        DeviceProfile deviceProfile = Launcher.getLauncher(getContext()).getDeviceProfile();
        RecyclerView.RecycledViewPool recycledViewPool = getRecycledViewPool();
        recycledViewPool.setMaxRecycledViews(4, 1);
        recycledViewPool.setMaxRecycledViews(16, 1);
        recycledViewPool.setMaxRecycledViews(8, 1);
        recycledViewPool.setMaxRecycledViews(2, ((int) Math.ceil(deviceProfile.availableHeightPx / deviceProfile.allAppsIconSizePx)) * this.mNumAppsPerRow);
        this.mViewHeights.clear();
        this.mViewHeights.put(2, deviceProfile.allAppsCellHeightPx);
    }

    public void scrollToTop() {
        if (this.mScrollbar != null) {
            this.mScrollbar.reattachThumbToScroll();
        }
        scrollToPosition(0);
    }

    @Override // android.support.v7.widget.RecyclerView, android.view.View
    public void onDraw(Canvas canvas) {
        if (this.mEmptySearchBackground != null && this.mEmptySearchBackground.getAlpha() > 0) {
            this.mEmptySearchBackground.draw(canvas);
        }
        super.onDraw(canvas);
    }

    @Override // android.view.View
    protected boolean verifyDrawable(Drawable drawable) {
        return drawable == this.mEmptySearchBackground || super.verifyDrawable(drawable);
    }

    @Override // android.support.v7.widget.RecyclerView, android.view.View
    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        updateEmptySearchBackgroundBounds();
        updatePoolSize();
    }

    @Override // com.android.launcher3.logging.UserEventDispatcher.LogContainerProvider
    public void fillInLogContainerData(View view, ItemInfo itemInfo, LauncherLogProto.Target target, LauncherLogProto.Target target2) {
        if (this.mApps.hasFilter()) {
            target2.containerType = 8;
        } else {
            target2.containerType = 4;
        }
    }

    public void onSearchResultsChanged() {
        scrollToTop();
        if (this.mApps.hasNoFilteredResults()) {
            if (this.mEmptySearchBackground == null) {
                this.mEmptySearchBackground = DrawableFactory.get(getContext()).getAllAppsBackground(getContext());
                this.mEmptySearchBackground.setAlpha(0);
                this.mEmptySearchBackground.setCallback(this);
                updateEmptySearchBackgroundBounds();
            }
            this.mEmptySearchBackground.animateBgAlpha(1.0f, 150);
        } else if (this.mEmptySearchBackground != null) {
            this.mEmptySearchBackground.setBgAlpha(0.0f);
        }
    }

    @Override // android.support.v7.widget.RecyclerView, android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        boolean onInterceptTouchEvent = super.onInterceptTouchEvent(motionEvent);
        if (!onInterceptTouchEvent && motionEvent.getAction() == 0 && this.mEmptySearchBackground != null && this.mEmptySearchBackground.getAlpha() > 0) {
            this.mEmptySearchBackground.setHotspot(motionEvent.getX(), motionEvent.getY());
        }
        return onInterceptTouchEvent;
    }

    @Override // com.android.launcher3.BaseRecyclerView
    public String scrollToPositionAtProgress(float f) {
        if (this.mApps.getNumAppRows() == 0) {
            return "";
        }
        stopScroll();
        List<AlphabeticalAppsList.FastScrollSectionInfo> fastScrollerSections = this.mApps.getFastScrollerSections();
        AlphabeticalAppsList.FastScrollSectionInfo fastScrollSectionInfo = fastScrollerSections.get(0);
        int i = 1;
        while (i < fastScrollerSections.size()) {
            AlphabeticalAppsList.FastScrollSectionInfo fastScrollSectionInfo2 = fastScrollerSections.get(i);
            if (fastScrollSectionInfo2.touchFraction > f) {
                break;
            }
            i++;
            fastScrollSectionInfo = fastScrollSectionInfo2;
        }
        this.mFastScrollHelper.smoothScrollToSection(getCurrentScrollY(), getAvailableScrollHeight(), fastScrollSectionInfo);
        return fastScrollSectionInfo.sectionName;
    }

    @Override // com.android.launcher3.BaseRecyclerView
    public void onFastScrollCompleted() {
        super.onFastScrollCompleted();
        this.mFastScrollHelper.onFastScrollCompleted();
    }

    @Override // android.support.v7.widget.RecyclerView
    public void setAdapter(RecyclerView.Adapter adapter) {
        super.setAdapter(adapter);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() { // from class: com.android.launcher3.allapps.AllAppsRecyclerView.1
            @Override // android.support.v7.widget.RecyclerView.AdapterDataObserver
            public void onChanged() {
                AllAppsRecyclerView.this.mCachedScrollPositions.clear();
            }
        });
        this.mFastScrollHelper.onSetAdapter((AllAppsGridAdapter) adapter);
    }

    @Override // android.view.View
    protected float getBottomFadingEdgeStrength() {
        return 0.0f;
    }

    @Override // android.view.View
    protected boolean isPaddingOffsetRequired() {
        return true;
    }

    @Override // android.view.View
    protected int getTopPaddingOffset() {
        return -getPaddingTop();
    }

    @Override // com.android.launcher3.BaseRecyclerView
    public void onUpdateScrollbar(int i) {
        int min;
        if (this.mApps == null) {
            return;
        }
        if (this.mApps.getAdapterItems().isEmpty() || this.mNumAppsPerRow == 0) {
            this.mScrollbar.setThumbOffsetY(-1);
            return;
        }
        int currentScrollY = getCurrentScrollY();
        if (currentScrollY < 0) {
            this.mScrollbar.setThumbOffsetY(-1);
            return;
        }
        int availableScrollBarHeight = getAvailableScrollBarHeight();
        int availableScrollHeight = getAvailableScrollHeight();
        if (availableScrollHeight <= 0) {
            this.mScrollbar.setThumbOffsetY(-1);
        } else if (this.mScrollbar.isThumbDetached()) {
            if (!this.mScrollbar.isDraggingThumb()) {
                int i2 = (int) ((currentScrollY / availableScrollHeight) * availableScrollBarHeight);
                int thumbOffsetY = this.mScrollbar.getThumbOffsetY();
                int i3 = i2 - thumbOffsetY;
                if (i3 * i > 0.0f) {
                    if (i < 0) {
                        min = thumbOffsetY + Math.max((int) ((i * thumbOffsetY) / i2), i3);
                    } else {
                        min = thumbOffsetY + Math.min((int) ((i * (availableScrollBarHeight - thumbOffsetY)) / (availableScrollBarHeight - i2)), i3);
                    }
                    int max = Math.max(0, Math.min(availableScrollBarHeight, min));
                    this.mScrollbar.setThumbOffsetY(max);
                    if (i2 == max) {
                        this.mScrollbar.reattachThumbToScroll();
                        return;
                    }
                    return;
                }
                this.mScrollbar.setThumbOffsetY(thumbOffsetY);
            }
        } else {
            synchronizeScrollBarThumbOffsetToViewScroll(currentScrollY, availableScrollHeight);
        }
    }

    @Override // com.android.launcher3.BaseRecyclerView
    public boolean supportsFastScrolling() {
        return !this.mApps.hasFilter();
    }

    @Override // com.android.launcher3.BaseRecyclerView
    public int getCurrentScrollY() {
        View childAt;
        int childPosition;
        if (this.mApps.getAdapterItems().isEmpty() || this.mNumAppsPerRow == 0 || getChildCount() == 0 || (childPosition = getChildPosition((childAt = getChildAt(0)))) == -1) {
            return -1;
        }
        return getPaddingTop() + getCurrentScrollY(childPosition, getLayoutManager().getDecoratedTop(childAt));
    }

    public int getCurrentScrollY(int i, int i2) {
        List<AlphabeticalAppsList.AdapterItem> adapterItems = this.mApps.getAdapterItems();
        AlphabeticalAppsList.AdapterItem adapterItem = i < adapterItems.size() ? adapterItems.get(i) : null;
        int i3 = this.mCachedScrollPositions.get(i, -1);
        if (i3 < 0) {
            int i4 = 0;
            for (int i5 = 0; i5 < i; i5++) {
                AlphabeticalAppsList.AdapterItem adapterItem2 = adapterItems.get(i5);
                if (AllAppsGridAdapter.isIconViewType(adapterItem2.viewType)) {
                    if (adapterItem != null && adapterItem.viewType == adapterItem2.viewType && adapterItem.rowIndex == adapterItem2.rowIndex) {
                        break;
                    } else if (adapterItem2.rowAppIndex == 0) {
                        i4 += this.mViewHeights.get(adapterItem2.viewType, 0);
                    }
                } else {
                    int i6 = this.mViewHeights.get(adapterItem2.viewType);
                    if (i6 == 0) {
                        RecyclerView.ViewHolder findViewHolderForAdapterPosition = findViewHolderForAdapterPosition(i5);
                        if (findViewHolderForAdapterPosition == null) {
                            RecyclerView.ViewHolder createViewHolder = getAdapter().createViewHolder(this, adapterItem2.viewType);
                            getAdapter().onBindViewHolder(createViewHolder, i5);
                            createViewHolder.itemView.measure(0, 0);
                            i6 = createViewHolder.itemView.getMeasuredHeight();
                            getRecycledViewPool().putRecycledView(createViewHolder);
                        } else {
                            i6 = findViewHolderForAdapterPosition.itemView.getMeasuredHeight();
                        }
                    }
                    i4 += i6;
                }
            }
            this.mCachedScrollPositions.put(i, i4);
            i3 = i4;
        }
        return i3 - i2;
    }

    @Override // com.android.launcher3.BaseRecyclerView
    protected int getAvailableScrollHeight() {
        return ((getPaddingTop() + getCurrentScrollY(getAdapter().getItemCount(), 0)) - getHeight()) + getPaddingBottom();
    }

    @Override // com.android.launcher3.BaseRecyclerView
    public int getScrollBarTop() {
        return getResources().getDimensionPixelOffset(R.dimen.all_apps_header_top_padding);
    }

    @Override // com.android.launcher3.BaseRecyclerView
    public RecyclerViewFastScroller getScrollbar() {
        return this.mScrollbar;
    }

    private void updateEmptySearchBackgroundBounds() {
        if (this.mEmptySearchBackground == null) {
            return;
        }
        int measuredWidth = (getMeasuredWidth() - this.mEmptySearchBackground.getIntrinsicWidth()) / 2;
        int i = this.mEmptySearchBackgroundTopOffset;
        this.mEmptySearchBackground.setBounds(measuredWidth, i, this.mEmptySearchBackground.getIntrinsicWidth() + measuredWidth, this.mEmptySearchBackground.getIntrinsicHeight() + i);
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }
}
