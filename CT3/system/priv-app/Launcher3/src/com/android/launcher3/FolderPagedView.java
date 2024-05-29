package com.android.launcher3;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import com.android.launcher3.CellLayout;
import com.android.launcher3.FocusHelper;
import com.android.launcher3.PageIndicator;
import com.android.launcher3.Workspace;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
/* loaded from: a.zip:com/android/launcher3/FolderPagedView.class */
public class FolderPagedView extends PagedView {
    private static final int[] sTempPosArray = new int[2];
    private int mAllocatedContentSize;
    private FocusIndicatorView mFocusIndicatorView;
    private Folder mFolder;
    private int mGridCountX;
    private int mGridCountY;
    private final IconCache mIconCache;
    private final LayoutInflater mInflater;
    public final boolean mIsRtl;
    private FocusHelper.PagedFolderKeyEventListener mKeyListener;
    private final int mMaxCountX;
    private final int mMaxCountY;
    private final int mMaxItemsPerPage;
    private PageIndicator mPageIndicator;
    final HashMap<View, Runnable> mPendingAnimations;

    public FolderPagedView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mPendingAnimations = new HashMap<>();
        LauncherAppState launcherAppState = LauncherAppState.getInstance();
        InvariantDeviceProfile invariantDeviceProfile = launcherAppState.getInvariantDeviceProfile();
        if (context.getResources().getConfiguration().orientation == 2) {
            Resources resources = context.getResources();
            this.mMaxCountX = resources.getInteger(2131427350);
            this.mMaxCountY = resources.getInteger(2131427351);
        } else {
            this.mMaxCountX = invariantDeviceProfile.numFolderColumns;
            this.mMaxCountY = invariantDeviceProfile.numFolderRows;
        }
        this.mMaxItemsPerPage = this.mMaxCountX * this.mMaxCountY;
        this.mInflater = LayoutInflater.from(context);
        this.mIconCache = launcherAppState.getIconCache();
        this.mIsRtl = Utilities.isRtl(getResources());
        setImportantForAccessibility(1);
        setEdgeGlowColor(getResources().getColor(2131361799));
    }

    /* JADX WARN: Code restructure failed: missing block: B:15:0x0074, code lost:
        if (r14 >= r9.mMaxItemsPerPage) goto L37;
     */
    @SuppressLint({"RtlHardcoded"})
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private void arrangeChildren(ArrayList<View> arrayList, int i, boolean z) {
        boolean z2;
        ArrayList arrayList2 = new ArrayList();
        for (int i2 = 0; i2 < getChildCount(); i2++) {
            CellLayout cellLayout = (CellLayout) getChildAt(i2);
            cellLayout.removeAllViews();
            arrayList2.add(cellLayout);
        }
        setupContentDimensions(i);
        Iterator it = arrayList2.iterator();
        CellLayout cellLayout2 = null;
        int i3 = 0;
        int i4 = 0;
        int i5 = 0;
        while (i5 < i) {
            BubbleTextView bubbleTextView = arrayList.size() > i5 ? arrayList.get(i5) : null;
            int i6 = cellLayout2 != null ? i3 : 0;
            cellLayout2 = it.hasNext() ? (CellLayout) it.next() : createAndAddNewPage();
            if (bubbleTextView != null) {
                CellLayout.LayoutParams layoutParams = (CellLayout.LayoutParams) bubbleTextView.getLayoutParams();
                int i7 = i6 % this.mGridCountX;
                int i8 = i6 / this.mGridCountX;
                ItemInfo itemInfo = (ItemInfo) bubbleTextView.getTag();
                if (itemInfo.cellX != i7 || itemInfo.cellY != i8 || itemInfo.rank != i4) {
                    itemInfo.cellX = i7;
                    itemInfo.cellY = i8;
                    itemInfo.rank = i4;
                    if (z) {
                        LauncherModel.addOrMoveItemInDatabase(getContext(), itemInfo, this.mFolder.mInfo.id, 0L, itemInfo.cellX, itemInfo.cellY);
                    }
                }
                layoutParams.cellX = itemInfo.cellX;
                layoutParams.cellY = itemInfo.cellY;
                cellLayout2.addViewToCellLayout(bubbleTextView, -1, this.mFolder.mLauncher.getViewIdForItem(itemInfo), layoutParams, true);
                if (i4 < 3 && (bubbleTextView instanceof BubbleTextView)) {
                    bubbleTextView.verifyHighRes();
                }
            }
            i4++;
            i3 = i6 + 1;
            i5++;
        }
        boolean z3 = false;
        while (true) {
            z2 = z3;
            if (!it.hasNext()) {
                break;
            }
            removeView((View) it.next());
            z3 = true;
        }
        if (z2) {
            setCurrentPage(0);
        }
        setEnableOverscroll(getPageCount() > 1);
        this.mPageIndicator.setVisibility(getPageCount() > 1 ? 0 : 8);
        this.mFolder.mFolderName.setGravity(getPageCount() > 1 ? this.mIsRtl ? 5 : 3 : 1);
    }

    private CellLayout createAndAddNewPage() {
        DeviceProfile deviceProfile = ((Launcher) getContext()).getDeviceProfile();
        CellLayout cellLayout = new CellLayout(getContext());
        cellLayout.setCellDimensions(deviceProfile.folderCellWidthPx, deviceProfile.folderCellHeightPx);
        cellLayout.getShortcutsAndWidgets().setMotionEventSplittingEnabled(false);
        cellLayout.setImportantForAccessibility(2);
        cellLayout.setInvertIfRtl(true);
        cellLayout.setGridSize(this.mGridCountX, this.mGridCountY);
        addView(cellLayout, -1, generateDefaultLayoutParams());
        return cellLayout;
    }

    private void setupContentDimensions(int i) {
        boolean z;
        this.mAllocatedContentSize = i;
        if (i >= this.mMaxItemsPerPage) {
            this.mGridCountX = this.mMaxCountX;
            this.mGridCountY = this.mMaxCountY;
            z = true;
        } else {
            z = false;
        }
        while (!z) {
            int i2 = this.mGridCountX;
            int i3 = this.mGridCountY;
            if (this.mGridCountX * this.mGridCountY < i) {
                if ((this.mGridCountX <= this.mGridCountY || this.mGridCountY == this.mMaxCountY) && this.mGridCountX < this.mMaxCountX) {
                    this.mGridCountX++;
                } else if (this.mGridCountY < this.mMaxCountY) {
                    this.mGridCountY++;
                }
                if (this.mGridCountY == 0) {
                    this.mGridCountY++;
                }
            } else if ((this.mGridCountY - 1) * this.mGridCountX >= i && this.mGridCountY >= this.mGridCountX) {
                this.mGridCountY = Math.max(0, this.mGridCountY - 1);
            } else if ((this.mGridCountX - 1) * this.mGridCountY >= i) {
                this.mGridCountX = Math.max(0, this.mGridCountX - 1);
            }
            z = this.mGridCountX == i2 && this.mGridCountY == i3;
        }
        for (int pageCount = getPageCount() - 1; pageCount >= 0; pageCount--) {
            getPageAt(pageCount).setGridSize(this.mGridCountX, this.mGridCountY);
        }
    }

    public void addViewForRank(View view, ShortcutInfo shortcutInfo, int i) {
        int i2 = i % this.mMaxItemsPerPage;
        int i3 = i / this.mMaxItemsPerPage;
        shortcutInfo.rank = i;
        shortcutInfo.cellX = i2 % this.mGridCountX;
        shortcutInfo.cellY = i2 / this.mGridCountX;
        CellLayout.LayoutParams layoutParams = (CellLayout.LayoutParams) view.getLayoutParams();
        layoutParams.cellX = shortcutInfo.cellX;
        layoutParams.cellY = shortcutInfo.cellY;
        getPageAt(i3).addViewToCellLayout(view, -1, this.mFolder.mLauncher.getViewIdForItem(shortcutInfo), layoutParams, true);
    }

    public int allocateRankForNewItem(ShortcutInfo shortcutInfo) {
        int itemCount = getItemCount();
        ArrayList<View> arrayList = new ArrayList<>(this.mFolder.getItemsInReadingOrder());
        arrayList.add(itemCount, null);
        arrangeChildren(arrayList, arrayList.size(), false);
        setCurrentPage(itemCount / this.mMaxItemsPerPage);
        return itemCount;
    }

    public void animateMarkers() {
        int childCount = this.mPageIndicator.getChildCount();
        OvershootInterpolator overshootInterpolator = new OvershootInterpolator(4.9f);
        for (int i = 0; i < childCount; i++) {
            this.mPageIndicator.getChildAt(i).animate().scaleX(1.0f).scaleY(1.0f).setInterpolator(overshootInterpolator).setDuration(400L).setStartDelay((i * 150) + 300);
        }
    }

    public void arrangeChildren(ArrayList<View> arrayList, int i) {
        arrangeChildren(arrayList, i, true);
    }

    public ArrayList<ShortcutInfo> bindItems(ArrayList<ShortcutInfo> arrayList) {
        ArrayList<View> arrayList2 = new ArrayList<>();
        ArrayList<ShortcutInfo> arrayList3 = new ArrayList<>();
        for (ShortcutInfo shortcutInfo : arrayList) {
            arrayList2.add(createNewView(shortcutInfo));
        }
        arrangeChildren(arrayList2, arrayList2.size(), false);
        return arrayList3;
    }

    public void clearScrollHint() {
        if (getScrollX() != getScrollForPage(getNextPage())) {
            snapToPage(getNextPage());
        }
    }

    public void completePendingPageChanges() {
        if (this.mPendingAnimations.isEmpty()) {
            return;
        }
        for (Map.Entry entry : new HashMap(this.mPendingAnimations).entrySet()) {
            ((View) entry.getKey()).animate().cancel();
            ((Runnable) entry.getValue()).run();
        }
    }

    public View createAndAddViewForRank(ShortcutInfo shortcutInfo, int i) {
        View createNewView = createNewView(shortcutInfo);
        addViewForRank(createNewView, shortcutInfo, i);
        return createNewView;
    }

    @SuppressLint({"InflateParams"})
    public View createNewView(ShortcutInfo shortcutInfo) {
        BubbleTextView bubbleTextView = (BubbleTextView) this.mInflater.inflate(2130968589, (ViewGroup) null, false);
        bubbleTextView.applyFromShortcutInfo(shortcutInfo, this.mIconCache);
        bubbleTextView.setOnClickListener(this.mFolder);
        bubbleTextView.setOnLongClickListener(this.mFolder);
        bubbleTextView.setOnFocusChangeListener(this.mFocusIndicatorView);
        bubbleTextView.setOnKeyListener(this.mKeyListener);
        bubbleTextView.setLayoutParams(new CellLayout.LayoutParams(shortcutInfo.cellX, shortcutInfo.cellY, shortcutInfo.spanX, shortcutInfo.spanY));
        return bubbleTextView;
    }

    public int findNearestArea(int i, int i2) {
        int nextPage = getNextPage();
        CellLayout pageAt = getPageAt(nextPage);
        pageAt.findNearestArea(i, i2, 1, 1, sTempPosArray);
        if (this.mFolder.isLayoutRtl()) {
            sTempPosArray[0] = (pageAt.getCountX() - sTempPosArray[0]) - 1;
        }
        return Math.min(this.mAllocatedContentSize - 1, (this.mMaxItemsPerPage * nextPage) + (sTempPosArray[1] * this.mGridCountX) + sTempPosArray[0]);
    }

    public String getAccessibilityDescription() {
        return String.format(getContext().getString(2131558447), Integer.valueOf(this.mGridCountX), Integer.valueOf(this.mGridCountY));
    }

    public int getAllocatedContentSize() {
        return this.mAllocatedContentSize;
    }

    @Override // com.android.launcher3.PagedView
    protected int getChildGap() {
        return getPaddingLeft() + getPaddingRight();
    }

    public CellLayout getCurrentCellLayout() {
        return getPageAt(getNextPage());
    }

    public int getDesiredHeight() {
        int i = 0;
        if (getPageCount() > 0) {
            i = getPageAt(0).getDesiredHeight() + getPaddingTop() + getPaddingBottom();
        }
        return i;
    }

    public int getDesiredWidth() {
        int i = 0;
        if (getPageCount() > 0) {
            i = getPageAt(0).getDesiredWidth() + getPaddingLeft() + getPaddingRight();
        }
        return i;
    }

    @Override // com.android.launcher3.PagedView
    protected void getEdgeVerticalPostion(int[] iArr) {
        iArr[0] = 0;
        iArr[1] = getViewportHeight();
    }

    public View getFirstItem() {
        if (getChildCount() < 1) {
            return null;
        }
        ShortcutAndWidgetContainer shortcutsAndWidgets = getCurrentCellLayout().getShortcutsAndWidgets();
        return this.mGridCountX > 0 ? shortcutsAndWidgets.getChildAt(0, 0) : shortcutsAndWidgets.getChildAt(0);
    }

    public int getItemCount() {
        int childCount = getChildCount() - 1;
        if (childCount < 0) {
            return 0;
        }
        return getPageAt(childCount).getShortcutsAndWidgets().getChildCount() + (this.mMaxItemsPerPage * childCount);
    }

    public View getLastItem() {
        if (getChildCount() < 1) {
            return null;
        }
        ShortcutAndWidgetContainer shortcutsAndWidgets = getCurrentCellLayout().getShortcutsAndWidgets();
        int childCount = shortcutsAndWidgets.getChildCount() - 1;
        return this.mGridCountX > 0 ? shortcutsAndWidgets.getChildAt(childCount % this.mGridCountX, childCount / this.mGridCountX) : shortcutsAndWidgets.getChildAt(childCount);
    }

    @Override // com.android.launcher3.PagedView
    public CellLayout getPageAt(int i) {
        return (CellLayout) getChildAt(i);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.launcher3.PagedView
    public PageIndicator.PageMarkerResources getPageIndicatorMarker(int i) {
        return new PageIndicator.PageMarkerResources(2130837527, 2130837529);
    }

    public boolean isFull() {
        return false;
    }

    public int itemsPerPage() {
        return this.mMaxItemsPerPage;
    }

    public View iterateOverItems(Workspace.ItemOperator itemOperator) {
        for (int i = 0; i < getChildCount(); i++) {
            CellLayout pageAt = getPageAt(i);
            for (int i2 = 0; i2 < pageAt.getCountY(); i2++) {
                for (int i3 = 0; i3 < pageAt.getCountX(); i3++) {
                    View childAt = pageAt.getChildAt(i3, i2);
                    if (childAt != null && itemOperator.evaluate((ItemInfo) childAt.getTag(), childAt, this)) {
                        return childAt;
                    }
                }
            }
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.launcher3.PagedView
    public void notifyPageSwitchListener() {
        super.notifyPageSwitchListener();
        if (this.mFolder != null) {
            this.mFolder.updateTextViewFocus();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.launcher3.PagedView
    public void onPageBeginMoving() {
        super.onPageBeginMoving();
        getVisiblePages(sTempPosArray);
        for (int i = sTempPosArray[0]; i <= sTempPosArray[1]; i++) {
            verifyVisibleHighResIcons(i);
        }
    }

    public boolean rankOnCurrentPage(int i) {
        return i / this.mMaxItemsPerPage == getNextPage();
    }

    public void realTimeReorder(int i, int i2) {
        int i3;
        int i4;
        int i5;
        int i6;
        completePendingPageChanges();
        float f = 30.0f;
        int nextPage = getNextPage();
        int i7 = i2 / this.mMaxItemsPerPage;
        int i8 = i2 % this.mMaxItemsPerPage;
        if (i7 != nextPage) {
            Log.e("FolderPagedView", "Cannot animate when the target cell is invisible");
        }
        int i9 = i % this.mMaxItemsPerPage;
        int i10 = i / this.mMaxItemsPerPage;
        if (i2 == i) {
            return;
        }
        if (i2 > i) {
            i3 = 1;
            if (i10 < nextPage) {
                i4 = i;
                i6 = nextPage * this.mMaxItemsPerPage;
                i5 = 0;
            } else {
                i4 = -1;
                i5 = i9;
                i6 = -1;
            }
        } else {
            i3 = -1;
            if (i10 > nextPage) {
                i4 = i;
                i6 = ((nextPage + 1) * this.mMaxItemsPerPage) - 1;
                i5 = this.mMaxItemsPerPage - 1;
            } else {
                i4 = -1;
                i5 = i9;
                i6 = -1;
            }
        }
        while (i4 != i6) {
            int i11 = i4 + i3;
            int i12 = i11 / this.mMaxItemsPerPage;
            int i13 = i11 % this.mMaxItemsPerPage;
            int i14 = this.mGridCountX;
            int i15 = i13 / this.mGridCountX;
            CellLayout pageAt = getPageAt(i12);
            View childAt = pageAt.getChildAt(i13 % i14, i15);
            if (childAt != null) {
                if (nextPage != i12) {
                    pageAt.removeView(childAt);
                    addViewForRank(childAt, (ShortcutInfo) childAt.getTag(), i4);
                } else {
                    Runnable runnable = new Runnable(this, childAt, childAt.getTranslationX(), i4) { // from class: com.android.launcher3.FolderPagedView.1
                        final FolderPagedView this$0;
                        final int val$newRank;
                        final float val$oldTranslateX;
                        final View val$v;

                        {
                            this.this$0 = this;
                            this.val$v = childAt;
                            this.val$oldTranslateX = r6;
                            this.val$newRank = i4;
                        }

                        @Override // java.lang.Runnable
                        public void run() {
                            this.this$0.mPendingAnimations.remove(this.val$v);
                            this.val$v.setTranslationX(this.val$oldTranslateX);
                            ((CellLayout) this.val$v.getParent().getParent()).removeView(this.val$v);
                            this.this$0.addViewForRank(this.val$v, (ShortcutInfo) this.val$v.getTag(), this.val$newRank);
                        }
                    };
                    childAt.animate().translationXBy((i3 > 0) ^ this.mIsRtl ? -childAt.getWidth() : childAt.getWidth()).setDuration(230L).setStartDelay(0L).withEndAction(runnable);
                    this.mPendingAnimations.put(childAt, runnable);
                }
            }
            i4 = i11;
        }
        if ((i8 - i5) * i3 <= 0) {
            return;
        }
        CellLayout pageAt2 = getPageAt(nextPage);
        int i16 = 0;
        while (i5 != i8) {
            int i17 = i5 + i3;
            View childAt2 = pageAt2.getChildAt(i17 % this.mGridCountX, i17 / this.mGridCountX);
            if (childAt2 != null) {
                ((ItemInfo) childAt2.getTag()).rank -= i3;
            }
            int i18 = i16;
            float f2 = f;
            if (pageAt2.animateChildToPosition(childAt2, i5 % this.mGridCountX, i5 / this.mGridCountX, 230, i16, true, true)) {
                i18 = (int) (i16 + f);
                f2 = f * 0.9f;
            }
            i5 += i3;
            i16 = i18;
            f = f2;
        }
    }

    public void removeItem(View view) {
        for (int childCount = getChildCount() - 1; childCount >= 0; childCount--) {
            getPageAt(childCount).removeView(view);
        }
    }

    public void setFixedSize(int i, int i2) {
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        for (int childCount = getChildCount() - 1; childCount >= 0; childCount--) {
            ((CellLayout) getChildAt(childCount)).setFixedSize(i - (paddingLeft + paddingRight), i2 - (paddingTop + paddingBottom));
        }
    }

    public void setFocusOnFirstChild() {
        View childAt = getCurrentCellLayout().getChildAt(0, 0);
        if (childAt != null) {
            childAt.requestFocus();
        }
    }

    public void setFolder(Folder folder) {
        this.mFolder = folder;
        this.mFocusIndicatorView = (FocusIndicatorView) folder.findViewById(2131296289);
        this.mKeyListener = new FocusHelper.PagedFolderKeyEventListener(folder);
        this.mPageIndicator = (PageIndicator) folder.findViewById(2131296313);
    }

    public void setMarkerScale(float f) {
        int childCount = this.mPageIndicator.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = this.mPageIndicator.getChildAt(i);
            childAt.animate().cancel();
            childAt.setScaleX(f);
            childAt.setScaleY(f);
        }
    }

    public void showScrollHint(int i) {
        int scrollForPage = (getScrollForPage(getNextPage()) + ((int) (getWidth() * ((i == 0) ^ this.mIsRtl ? -0.07f : 0.07f)))) - getScrollX();
        if (scrollForPage != 0) {
            this.mScroller.setInterpolator(new DecelerateInterpolator());
            this.mScroller.startScroll(getScrollX(), 0, scrollForPage, 0, 500);
            invalidate();
        }
    }

    public void verifyVisibleHighResIcons(int i) {
        CellLayout pageAt = getPageAt(i);
        if (pageAt != null) {
            ShortcutAndWidgetContainer shortcutsAndWidgets = pageAt.getShortcutsAndWidgets();
            for (int childCount = shortcutsAndWidgets.getChildCount() - 1; childCount >= 0; childCount--) {
                ((BubbleTextView) shortcutsAndWidgets.getChildAt(childCount)).verifyHighRes();
            }
        }
    }
}
