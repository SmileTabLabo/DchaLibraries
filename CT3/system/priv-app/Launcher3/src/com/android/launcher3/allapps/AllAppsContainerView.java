package com.android.launcher3.allapps;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.method.TextKeyListener;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import com.android.launcher3.AppInfo;
import com.android.launcher3.BaseContainerView;
import com.android.launcher3.BubbleTextView;
import com.android.launcher3.CellLayout;
import com.android.launcher3.DeleteDropTarget;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.DragSource;
import com.android.launcher3.DropTarget;
import com.android.launcher3.ExtendedEditText;
import com.android.launcher3.Folder;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherTransitionable;
import com.android.launcher3.Utilities;
import com.android.launcher3.Workspace;
import com.android.launcher3.allapps.AllAppsSearchBarController;
import com.android.launcher3.allapps.AlphabeticalAppsList;
import com.android.launcher3.allapps.HeaderElevationController;
import com.android.launcher3.compat.PackageInstallerCompat;
import com.android.launcher3.util.ComponentKey;
import java.util.ArrayList;
import java.util.List;
/* loaded from: a.zip:com/android/launcher3/allapps/AllAppsContainerView.class */
public class AllAppsContainerView extends BaseContainerView implements DragSource, LauncherTransitionable, View.OnTouchListener, View.OnLongClickListener, AllAppsSearchBarController.Callbacks {
    private final AllAppsGridAdapter mAdapter;
    private final AlphabeticalAppsList mApps;
    private AllAppsRecyclerView mAppsRecyclerView;
    private final Point mBoundsCheckLastTouchDownPos;
    private final Rect mContentBounds;
    private HeaderElevationController mElevationController;
    private final Point mIconLastTouchPos;
    private final RecyclerView.ItemDecoration mItemDecoration;
    private final Launcher mLauncher;
    private final RecyclerView.LayoutManager mLayoutManager;
    private int mNumAppsPerRow;
    private int mNumPredictedAppsPerRow;
    private int mRecyclerViewTopBottomPadding;
    private AllAppsSearchBarController mSearchBarController;
    private View mSearchContainer;
    private ExtendedEditText mSearchInput;
    private SpannableStringBuilder mSearchQueryBuilder;
    private int mSectionNamesMargin;

    public AllAppsContainerView(Context context) {
        this(context, null);
    }

    public AllAppsContainerView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public AllAppsContainerView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mContentBounds = new Rect();
        this.mSearchQueryBuilder = null;
        this.mBoundsCheckLastTouchDownPos = new Point(-1, -1);
        this.mIconLastTouchPos = new Point();
        Resources resources = context.getResources();
        this.mLauncher = (Launcher) context;
        this.mSectionNamesMargin = resources.getDimensionPixelSize(2131230764);
        this.mApps = new AlphabeticalAppsList(context);
        this.mAdapter = new AllAppsGridAdapter(this.mLauncher, this.mApps, this, this.mLauncher, this);
        this.mApps.setAdapter(this.mAdapter);
        this.mLayoutManager = this.mAdapter.getLayoutManager();
        this.mItemDecoration = this.mAdapter.getItemDecoration();
        this.mRecyclerViewTopBottomPadding = resources.getDimensionPixelSize(2131230773);
        this.mSearchQueryBuilder = new SpannableStringBuilder();
        Selection.setSelection(this.mSearchQueryBuilder, 0);
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    private boolean handleTouchEvent(MotionEvent motionEvent) {
        DeviceProfile deviceProfile = this.mLauncher.getDeviceProfile();
        int x = (int) motionEvent.getX();
        int y = (int) motionEvent.getY();
        switch (motionEvent.getAction()) {
            case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                if (this.mContentBounds.isEmpty()) {
                    if (motionEvent.getX() < getPaddingLeft() || motionEvent.getX() > getWidth() - getPaddingRight()) {
                        this.mBoundsCheckLastTouchDownPos.set(x, y);
                        return true;
                    }
                    return false;
                }
                Rect rect = new Rect(this.mContentBounds);
                rect.inset((-deviceProfile.allAppsIconSizePx) / 2, 0);
                if (motionEvent.getX() < rect.left || motionEvent.getX() > rect.right) {
                    this.mBoundsCheckLastTouchDownPos.set(x, y);
                    return true;
                }
                return false;
            case 1:
                if (this.mBoundsCheckLastTouchDownPos.x > -1) {
                    ViewConfiguration viewConfiguration = ViewConfiguration.get(getContext());
                    if (((float) Math.hypot(motionEvent.getX() - this.mBoundsCheckLastTouchDownPos.x, motionEvent.getY() - this.mBoundsCheckLastTouchDownPos.y)) < viewConfiguration.getScaledTouchSlop()) {
                        ((Launcher) getContext()).showWorkspace(true);
                        return true;
                    }
                }
                break;
            case 2:
            default:
                return false;
            case 3:
                break;
        }
        this.mBoundsCheckLastTouchDownPos.set(-1, -1);
        return false;
    }

    public void addApps(List<AppInfo> list) {
        this.mApps.addApps(list);
    }

    @Override // com.android.launcher3.allapps.AllAppsSearchBarController.Callbacks
    public void clearSearchResult() {
        if (this.mApps.setOrderedFilter(null)) {
            this.mAppsRecyclerView.onSearchResultsChanged();
        }
        this.mSearchQueryBuilder.clear();
        this.mSearchQueryBuilder.clearSpans();
        Selection.setSelection(this.mSearchQueryBuilder, 0);
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        boolean z = false;
        if (!this.mSearchBarController.isSearchFieldFocused() && keyEvent.getAction() == 0) {
            int unicodeChar = keyEvent.getUnicodeChar();
            if (unicodeChar <= 0 || Character.isWhitespace(unicodeChar)) {
                z = false;
            } else if (!Character.isSpaceChar(unicodeChar)) {
                z = true;
            }
            if (z && TextKeyListener.getInstance().onKeyDown(this, this.mSearchQueryBuilder, keyEvent.getKeyCode(), keyEvent) && this.mSearchQueryBuilder.length() > 0) {
                this.mSearchBarController.focusSearchField();
            }
        }
        return super.dispatchKeyEvent(keyEvent);
    }

    @Override // com.android.launcher3.DragSource
    public float getIntrinsicIconScaleFactor() {
        DeviceProfile deviceProfile = this.mLauncher.getDeviceProfile();
        return deviceProfile.allAppsIconSizePx / deviceProfile.iconSizePx;
    }

    @Override // com.android.launcher3.DragSource
    public void onDropCompleted(View view, DropTarget.DragObject dragObject, boolean z, boolean z2) {
        if (z || !z2 || (view != this.mLauncher.getWorkspace() && !(view instanceof DeleteDropTarget) && !(view instanceof Folder))) {
            this.mLauncher.exitSpringLoadedDragModeDelayed(true, 300, null);
        }
        this.mLauncher.unlockScreenOrientation(false);
        if (z2) {
            return;
        }
        boolean z3 = false;
        if (view instanceof Workspace) {
            CellLayout cellLayout = (CellLayout) ((Workspace) view).getChildAt(this.mLauncher.getCurrentWorkspaceScreen());
            ItemInfo itemInfo = (ItemInfo) dragObject.dragInfo;
            z3 = false;
            if (cellLayout != null) {
                z3 = !cellLayout.findCellForSpan(null, itemInfo.spanX, itemInfo.spanY);
            }
        }
        if (z3) {
            this.mLauncher.showOutOfSpaceMessage(false);
        }
        dragObject.deferDragViewCleanupPostAnimation = false;
    }

    @Override // com.android.launcher3.BaseContainerView, android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        getContentView().setOnFocusChangeListener(new View.OnFocusChangeListener(this) { // from class: com.android.launcher3.allapps.AllAppsContainerView.1
            final AllAppsContainerView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnFocusChangeListener
            public void onFocusChange(View view, boolean z) {
                if (z) {
                    this.this$0.mAppsRecyclerView.requestFocus();
                }
            }
        });
        this.mSearchContainer = findViewById(2131296277);
        this.mSearchInput = (ExtendedEditText) findViewById(2131296278);
        this.mElevationController = Utilities.ATLEAST_LOLLIPOP ? new HeaderElevationController.ControllerVL(this.mSearchContainer) : new HeaderElevationController.ControllerV16(this.mSearchContainer);
        this.mAppsRecyclerView = (AllAppsRecyclerView) findViewById(2131296276);
        this.mAppsRecyclerView.setApps(this.mApps);
        this.mAppsRecyclerView.setLayoutManager(this.mLayoutManager);
        this.mAppsRecyclerView.setAdapter(this.mAdapter);
        this.mAppsRecyclerView.setHasFixedSize(true);
        this.mAppsRecyclerView.addOnScrollListener(this.mElevationController);
        this.mAppsRecyclerView.setElevationController(this.mElevationController);
        if (this.mItemDecoration != null) {
            this.mAppsRecyclerView.addItemDecoration(this.mItemDecoration);
        }
        LayoutInflater from = LayoutInflater.from(getContext());
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(getResources().getDisplayMetrics().widthPixels, Integer.MIN_VALUE);
        int makeMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(getResources().getDisplayMetrics().heightPixels, Integer.MIN_VALUE);
        BubbleTextView bubbleTextView = (BubbleTextView) from.inflate(2130968581, (ViewGroup) this, false);
        bubbleTextView.applyDummyInfo();
        bubbleTextView.measure(makeMeasureSpec, makeMeasureSpec2);
        BubbleTextView bubbleTextView2 = (BubbleTextView) from.inflate(2130968582, (ViewGroup) this, false);
        bubbleTextView2.applyDummyInfo();
        bubbleTextView2.measure(makeMeasureSpec, makeMeasureSpec2);
        this.mAppsRecyclerView.setPremeasuredIconHeights(bubbleTextView2.getMeasuredHeight(), bubbleTextView.getMeasuredHeight());
        updateBackgroundAndPaddings();
    }

    @Override // com.android.launcher3.DragSource
    public void onFlingToDeleteCompleted() {
        this.mLauncher.exitSpringLoadedDragModeDelayed(true, 300, null);
        this.mLauncher.unlockScreenOrientation(false);
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        return handleTouchEvent(motionEvent);
    }

    @Override // com.android.launcher3.LauncherTransitionable
    public void onLauncherTransitionEnd(Launcher launcher, boolean z, boolean z2) {
        if (z2) {
            reset();
        }
    }

    @Override // com.android.launcher3.LauncherTransitionable
    public void onLauncherTransitionPrepare(Launcher launcher, boolean z, boolean z2) {
    }

    @Override // com.android.launcher3.LauncherTransitionable
    public void onLauncherTransitionStart(Launcher launcher, boolean z, boolean z2) {
    }

    @Override // com.android.launcher3.LauncherTransitionable
    public void onLauncherTransitionStep(Launcher launcher, float f) {
    }

    @Override // android.view.View.OnLongClickListener
    public boolean onLongClick(View view) {
        if (view.isInTouchMode() && this.mLauncher.isAppsViewVisible() && !this.mLauncher.getWorkspace().isSwitchingState() && this.mLauncher.isDraggingEnabled()) {
            this.mLauncher.getWorkspace().beginDragShared(view, this.mIconLastTouchPos, this, false);
            this.mLauncher.enterSpringLoadedDragMode();
            return false;
        }
        return false;
    }

    @Override // android.widget.FrameLayout, android.view.View
    protected void onMeasure(int i, int i2) {
        this.mContentBounds.set(this.mContentPadding.left, this.mContentPadding.top, View.MeasureSpec.getSize(i) - this.mContentPadding.right, View.MeasureSpec.getSize(i2) - this.mContentPadding.bottom);
        int width = (!this.mContentBounds.isEmpty() ? this.mContentBounds.width() : View.MeasureSpec.getSize(i)) - (this.mAppsRecyclerView.getMaxScrollbarWidth() * 2);
        DeviceProfile deviceProfile = this.mLauncher.getDeviceProfile();
        deviceProfile.updateAppsViewNumCols(getResources(), width);
        if (this.mNumAppsPerRow != deviceProfile.allAppsNumCols || this.mNumPredictedAppsPerRow != deviceProfile.allAppsNumPredictiveCols) {
            this.mNumAppsPerRow = deviceProfile.allAppsNumCols;
            this.mNumPredictedAppsPerRow = deviceProfile.allAppsNumPredictiveCols;
            AlphabeticalAppsList.MergeAlgorithm fullMergeAlgorithm = this.mSectionNamesMargin == 0 || !deviceProfile.isPhone ? new FullMergeAlgorithm() : new SimpleSectionMergeAlgorithm((int) Math.ceil(this.mNumAppsPerRow / 2.0f), 3, 2);
            if (this.mNumAppsPerRow > 0) {
                this.mAppsRecyclerView.setNumAppsPerRow(deviceProfile, this.mNumAppsPerRow);
                this.mAdapter.setNumAppsPerRow(this.mNumAppsPerRow);
                this.mApps.setNumAppsPerRow(this.mNumAppsPerRow, this.mNumPredictedAppsPerRow, fullMergeAlgorithm);
                int i3 = ((width / this.mNumAppsPerRow) - deviceProfile.allAppsIconSizePx) / 2;
                this.mSearchInput.setPaddingRelative(i3, 0, i3, 0);
            }
        }
        super.onMeasure(i, i2);
    }

    @Override // com.android.launcher3.allapps.AllAppsSearchBarController.Callbacks
    public void onSearchResult(String str, ArrayList<ComponentKey> arrayList) {
        if (arrayList != null) {
            if (this.mApps.setOrderedFilter(arrayList)) {
                this.mAppsRecyclerView.onSearchResultsChanged();
            }
            this.mAdapter.setLastSearchQuery(str);
        }
    }

    @Override // android.view.View.OnTouchListener
    @SuppressLint({"ClickableViewAccessibility"})
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
            case 2:
                this.mIconLastTouchPos.set((int) motionEvent.getX(), (int) motionEvent.getY());
                return false;
            case 1:
            default:
                return false;
        }
    }

    @Override // android.view.View
    @SuppressLint({"ClickableViewAccessibility"})
    public boolean onTouchEvent(MotionEvent motionEvent) {
        return handleTouchEvent(motionEvent);
    }

    @Override // com.android.launcher3.BaseContainerView
    protected void onUpdateBgPadding(Rect rect, Rect rect2) {
        this.mAppsRecyclerView.updateBackgroundPadding(rect2);
        this.mAdapter.updateBackgroundPadding(rect2);
        this.mElevationController.updateBackgroundPadding(rect2);
        int maxScrollbarWidth = this.mAppsRecyclerView.getMaxScrollbarWidth();
        int max = Math.max(this.mSectionNamesMargin, maxScrollbarWidth);
        int i = this.mRecyclerViewTopBottomPadding;
        if (Utilities.isRtl(getResources())) {
            this.mAppsRecyclerView.setPadding(rect.left + maxScrollbarWidth, i, rect.right + max, i);
        } else {
            this.mAppsRecyclerView.setPadding(rect.left + max, i, rect.right + maxScrollbarWidth, i);
        }
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mSearchContainer.getLayoutParams();
        marginLayoutParams.leftMargin = rect.left;
        marginLayoutParams.rightMargin = rect.right;
        this.mSearchContainer.setLayoutParams(marginLayoutParams);
    }

    public void removeApps(List<AppInfo> list) {
        this.mApps.removeApps(list);
    }

    public void reset() {
        this.mSearchBarController.reset();
        this.mAppsRecyclerView.reset();
    }

    public void scrollToTop() {
        this.mAppsRecyclerView.scrollToTop();
    }

    public void setApps(List<AppInfo> list) {
        this.mApps.setApps(list);
    }

    public void setPredictedApps(List<ComponentKey> list) {
        this.mApps.setPredictedApps(list);
    }

    public void setSearchBarController(AllAppsSearchBarController allAppsSearchBarController) {
        if (this.mSearchBarController != null) {
            throw new RuntimeException("Expected search bar controller to only be set once");
        }
        this.mSearchBarController = allAppsSearchBarController;
        this.mSearchBarController.initialize(this.mApps, this.mSearchInput, this.mLauncher, this);
        this.mAdapter.setSearchController(this.mSearchBarController);
        updateBackgroundAndPaddings();
    }

    public void startAppsSearch() {
        if (this.mSearchBarController != null) {
            this.mSearchBarController.focusSearchField();
        }
    }

    @Override // com.android.launcher3.DragSource
    public boolean supportsAppInfoDropTarget() {
        return true;
    }

    @Override // com.android.launcher3.DragSource
    public boolean supportsDeleteDropTarget() {
        return false;
    }

    @Override // com.android.launcher3.DragSource
    public boolean supportsFlingToDelete() {
        return true;
    }

    public void updateApps(List<AppInfo> list) {
        this.mApps.updateApps(list);
    }
}
