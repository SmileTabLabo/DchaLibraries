package com.android.launcher3;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Choreographer;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.TextView;
import com.android.launcher3.CellLayout;
import com.android.launcher3.DragController;
import com.android.launcher3.DropTarget;
import com.android.launcher3.FolderIcon;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherModel;
import com.android.launcher3.PageIndicator;
import com.android.launcher3.SearchDropTargetBar;
import com.android.launcher3.Stats;
import com.android.launcher3.UninstallDropTarget;
import com.android.launcher3.accessibility.LauncherAccessibilityDelegate;
import com.android.launcher3.accessibility.OverviewScreenAccessibilityDelegate;
import com.android.launcher3.accessibility.WorkspaceAccessibilityHelper;
import com.android.launcher3.compat.AppWidgetManagerCompat;
import com.android.launcher3.compat.PackageInstallerCompat;
import com.android.launcher3.compat.UserHandleCompat;
import com.android.launcher3.util.LongArrayMap;
import com.android.launcher3.util.WallpaperUtils;
import com.android.launcher3.widget.PendingAddShortcutInfo;
import com.android.launcher3.widget.PendingAddWidgetInfo;
import com.mediatek.launcher3.LauncherHelper;
import com.mediatek.launcher3.LauncherLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
/* loaded from: a.zip:com/android/launcher3/Workspace.class */
public class Workspace extends PagedView implements DropTarget, DragSource, DragScroller, View.OnTouchListener, DragController.DragListener, LauncherTransitionable, ViewGroup.OnHierarchyChangeListener, Insettable, UninstallDropTarget.UninstallSource, LauncherAccessibilityDelegate.AccessibilityDragSource, Stats.LaunchSourceProvider {
    private static boolean ENFORCE_DRAG_EVENT_ORDER = false;
    static Rect mLandscapeCellLayoutMetrics = null;
    static Rect mPortraitCellLayoutMetrics = null;
    private static final Rect sTempRect = new Rect();
    boolean mAddNewPageOnDrag;
    private boolean mAddToExistingFolderOnDrop;
    private final Interpolator mAlphaInterpolator;
    boolean mAnimatingViewIntoPlace;
    private final Runnable mBindPages;
    private final Canvas mCanvas;
    boolean mChildrenLayersEnabled;
    private boolean mCreateUserFolderOnDrop;
    private float mCurrentScale;
    Launcher.CustomContentCallbacks mCustomContentCallbacks;
    private String mCustomContentDescription;
    private long mCustomContentShowTime;
    boolean mCustomContentShowing;
    private int mDefaultPage;
    private boolean mDeferDropAfterUninstall;
    boolean mDeferRemoveExtraEmptyScreen;
    Runnable mDeferredAction;
    Runnable mDelayedResizeRunnable;
    private Runnable mDelayedSnapToPageRunnable;
    private Point mDisplaySize;
    DragController mDragController;
    FolderIcon.FolderRingAnimator mDragFolderRingAnimator;
    private CellLayout.CellInfo mDragInfo;
    private int mDragMode;
    Bitmap mDragOutline;
    private FolderIcon mDragOverFolderIcon;
    private int mDragOverX;
    private int mDragOverY;
    private CellLayout mDragOverlappingLayout;
    private ShortcutAndWidgetContainer mDragSourceInternal;
    CellLayout mDragTargetLayout;
    float[] mDragViewVisualCenter;
    private CellLayout mDropToLayout;
    private final Alarm mFolderCreationAlarm;
    IconCache mIconCache;
    private boolean mInScrollArea;
    boolean mIsDragOccuring;
    private boolean mIsSwitchingState;
    private float mLastCustomContentScrollProgress;
    float mLastOverlaySroll;
    int mLastReorderX;
    int mLastReorderY;
    float mLastSetWallpaperOffsetSteps;
    Launcher mLauncher;
    Launcher.LauncherOverlay mLauncherOverlay;
    private LayoutTransition mLayoutTransition;
    private float mMaxDistanceForFolderCreation;
    int mNumPagesForWallpaperParallax;
    private int mOriginalDefaultPage;
    private HolographicOutlineHelper mOutlineHelper;
    private float mOverlayTranslation;
    private float mOverviewModeShrinkFactor;
    private View.AccessibilityDelegate mPagesAccessibilityDelegate;
    Runnable mRemoveEmptyScreenRunnable;
    private final Alarm mReorderAlarm;
    private final ArrayList<Integer> mRestoredPages;
    private SparseArray<Parcelable> mSavedStates;
    ArrayList<Long> mScreenOrder;
    boolean mScrollInteractionBegan;
    private SpringLoadedDragController mSpringLoadedDragController;
    private float mSpringLoadedShrinkFactor;
    boolean mStartedSendingScrollEvents;
    private State mState;
    private WorkspaceStateTransitionAnimation mStateTransitionAnimation;
    private boolean mStripScreensOnPageStopMoving;
    int[] mTargetCell;
    private int[] mTempCell;
    private float[] mTempCellLayoutCenterCoordinates;
    private int[] mTempEstimate;
    private Matrix mTempInverseMatrix;
    private Matrix mTempMatrix;
    private int[] mTempPt;
    private int[] mTempVisiblePagesRange;
    private final int[] mTempXY;
    private long mTouchDownTime;
    private float mTransitionProgress;
    private int mUnboundedScrollX;
    private boolean mUninstallSuccessful;
    boolean mWallpaperIsLiveWallpaper;
    final WallpaperManager mWallpaperManager;
    WallpaperOffsetInterpolator mWallpaperOffset;
    IBinder mWindowToken;
    private boolean mWorkspaceFadeInAdjacentScreens;
    LongArrayMap<CellLayout> mWorkspaceScreens;
    private float mXDown;
    private float mYDown;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/launcher3/Workspace$DeferredWidgetRefresh.class */
    public class DeferredWidgetRefresh implements Runnable {
        private final LauncherAppWidgetHost mHost;
        private final ArrayList<LauncherAppWidgetInfo> mInfos;
        final Workspace this$0;
        private final Handler mHandler = new Handler();
        private boolean mRefreshPending = true;

        public DeferredWidgetRefresh(Workspace workspace, ArrayList<LauncherAppWidgetInfo> arrayList, LauncherAppWidgetHost launcherAppWidgetHost) {
            this.this$0 = workspace;
            this.mInfos = arrayList;
            this.mHost = launcherAppWidgetHost;
            this.mHost.addProviderChangeListener(this);
            this.mHandler.postDelayed(this, 10000L);
        }

        @Override // java.lang.Runnable
        public void run() {
            this.mHost.removeProviderChangeListener(this);
            this.mHandler.removeCallbacks(this);
            if (this.mRefreshPending) {
                this.mRefreshPending = false;
                for (LauncherAppWidgetInfo launcherAppWidgetInfo : this.mInfos) {
                    if (launcherAppWidgetInfo.hostView instanceof PendingAppWidgetHostView) {
                        this.this$0.mLauncher.removeItem((PendingAppWidgetHostView) launcherAppWidgetInfo.hostView, launcherAppWidgetInfo, false);
                        this.this$0.mLauncher.bindAppWidget(launcherAppWidgetInfo);
                    }
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:com/android/launcher3/Workspace$FolderCreationAlarmListener.class */
    public class FolderCreationAlarmListener implements OnAlarmListener {
        int cellX;
        int cellY;
        CellLayout layout;
        final Workspace this$0;

        public FolderCreationAlarmListener(Workspace workspace, CellLayout cellLayout, int i, int i2) {
            this.this$0 = workspace;
            this.layout = cellLayout;
            this.cellX = i;
            this.cellY = i2;
        }

        @Override // com.android.launcher3.OnAlarmListener
        public void onAlarm(Alarm alarm) {
            if (this.this$0.mDragFolderRingAnimator != null) {
                this.this$0.mDragFolderRingAnimator.animateToNaturalState();
            }
            this.this$0.mDragFolderRingAnimator = new FolderIcon.FolderRingAnimator(this.this$0.mLauncher, null);
            this.this$0.mDragFolderRingAnimator.setCell(this.cellX, this.cellY);
            this.this$0.mDragFolderRingAnimator.setCellLayout(this.layout);
            this.this$0.mDragFolderRingAnimator.animateToAcceptState();
            this.layout.showFolderAccept(this.this$0.mDragFolderRingAnimator);
            this.layout.clearDragOutlines();
            this.this$0.setDragMode(1);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:com/android/launcher3/Workspace$ItemOperator.class */
    public interface ItemOperator {
        boolean evaluate(ItemInfo itemInfo, View view, View view2);
    }

    /* loaded from: a.zip:com/android/launcher3/Workspace$ReorderAlarmListener.class */
    class ReorderAlarmListener implements OnAlarmListener {
        View child;
        DropTarget.DragObject dragObject;
        float[] dragViewCenter;
        int minSpanX;
        int minSpanY;
        int spanX;
        int spanY;
        final Workspace this$0;

        public ReorderAlarmListener(Workspace workspace, float[] fArr, int i, int i2, int i3, int i4, DropTarget.DragObject dragObject, View view) {
            this.this$0 = workspace;
            this.dragViewCenter = fArr;
            this.minSpanX = i;
            this.minSpanY = i2;
            this.spanX = i3;
            this.spanY = i4;
            this.child = view;
            this.dragObject = dragObject;
        }

        @Override // com.android.launcher3.OnAlarmListener
        public void onAlarm(Alarm alarm) {
            int[] iArr = new int[2];
            this.this$0.mTargetCell = this.this$0.findNearestArea((int) this.this$0.mDragViewVisualCenter[0], (int) this.this$0.mDragViewVisualCenter[1], this.minSpanX, this.minSpanY, this.this$0.mDragTargetLayout, this.this$0.mTargetCell);
            this.this$0.mLastReorderX = this.this$0.mTargetCell[0];
            this.this$0.mLastReorderY = this.this$0.mTargetCell[1];
            this.this$0.mTargetCell = this.this$0.mDragTargetLayout.performReorder((int) this.this$0.mDragViewVisualCenter[0], (int) this.this$0.mDragViewVisualCenter[1], this.minSpanX, this.minSpanY, this.spanX, this.spanY, this.child, this.this$0.mTargetCell, iArr, 1);
            if (this.this$0.mTargetCell[0] < 0 || this.this$0.mTargetCell[1] < 0) {
                this.this$0.mDragTargetLayout.revertTempState();
            } else {
                this.this$0.setDragMode(3);
            }
            this.this$0.mDragTargetLayout.visualizeDropLocation(this.child, this.this$0.mDragOutline, this.this$0.mTargetCell[0], this.this$0.mTargetCell[1], iArr[0], iArr[1], (iArr[0] == this.spanX && iArr[1] == this.spanY) ? false : true, this.dragObject);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:com/android/launcher3/Workspace$State.class */
    public enum State {
        NORMAL(SearchDropTargetBar.State.SEARCH_BAR, false),
        NORMAL_HIDDEN(SearchDropTargetBar.State.INVISIBLE_TRANSLATED, false),
        SPRING_LOADED(SearchDropTargetBar.State.DROP_TARGET, false),
        OVERVIEW(SearchDropTargetBar.State.INVISIBLE, true),
        OVERVIEW_HIDDEN(SearchDropTargetBar.State.INVISIBLE, true);
        
        public final SearchDropTargetBar.State searchDropTargetBarState;
        public final boolean shouldUpdateWidget;

        State(SearchDropTargetBar.State state, boolean z) {
            this.searchDropTargetBarState = state;
            this.shouldUpdateWidget = z;
        }

        /* renamed from: values  reason: to resolve conflict with enum method */
        public static State[] valuesCustom() {
            return values();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:com/android/launcher3/Workspace$WallpaperOffsetInterpolator.class */
    public class WallpaperOffsetInterpolator implements Choreographer.FrameCallback {
        boolean mAnimating;
        float mAnimationStartOffset;
        long mAnimationStartTime;
        int mNumScreens;
        boolean mWaitingForUpdate;
        final Workspace this$0;
        float mFinalOffset = 0.0f;
        float mCurrentOffset = 0.5f;
        private final int ANIMATION_DURATION = 250;
        private final int MIN_PARALLAX_PAGE_SPAN = 3;
        Choreographer mChoreographer = Choreographer.getInstance();
        Interpolator mInterpolator = new DecelerateInterpolator(1.5f);

        public WallpaperOffsetInterpolator(Workspace workspace) {
            this.this$0 = workspace;
        }

        private void animateToFinal() {
            this.mAnimating = true;
            this.mAnimationStartOffset = this.mCurrentOffset;
            this.mAnimationStartTime = System.currentTimeMillis();
        }

        private int getNumScreensExcludingEmptyAndCustom() {
            return (this.this$0.getChildCount() - numEmptyScreensToIgnore()) - this.this$0.numCustomPages();
        }

        private int numEmptyScreensToIgnore() {
            return (this.this$0.getChildCount() - this.this$0.numCustomPages() < 3 || !this.this$0.hasExtraEmptyScreen()) ? 0 : 1;
        }

        private void scheduleUpdate() {
            if (this.mWaitingForUpdate) {
                return;
            }
            this.mChoreographer.postFrameCallback(this);
            this.mWaitingForUpdate = true;
        }

        private void setWallpaperOffsetSteps() {
            float f = 1.0f / this.this$0.mNumPagesForWallpaperParallax;
            if (f != this.this$0.mLastSetWallpaperOffsetSteps) {
                this.this$0.mWallpaperManager.setWallpaperOffsetSteps(f, 1.0f);
                this.this$0.mLastSetWallpaperOffsetSteps = f;
            }
        }

        private void updateOffset(boolean z) {
            if (this.mWaitingForUpdate || z) {
                this.mWaitingForUpdate = false;
                if (!computeScrollOffset() || this.this$0.mWindowToken == null) {
                    return;
                }
                try {
                    this.this$0.mWallpaperManager.setWallpaperOffsets(this.this$0.mWindowToken, this.this$0.mWallpaperOffset.getCurrX(), 0.5f);
                    setWallpaperOffsetSteps();
                } catch (IllegalArgumentException e) {
                    Log.e("Launcher.Workspace", "Error updating wallpaper offset: " + e);
                }
            }
        }

        private float wallpaperOffsetForCurrentScroll() {
            return wallpaperOffsetForScroll(this.this$0.getScrollX());
        }

        public boolean computeScrollOffset() {
            float f = this.mCurrentOffset;
            if (this.mAnimating) {
                long currentTimeMillis = System.currentTimeMillis() - this.mAnimationStartTime;
                this.mCurrentOffset = this.mAnimationStartOffset + ((this.mFinalOffset - this.mAnimationStartOffset) * this.mInterpolator.getInterpolation(((float) currentTimeMillis) / 250.0f));
                if (this.mCurrentOffset > 1.0f) {
                    this.mCurrentOffset = this.mFinalOffset;
                }
                this.mAnimating = currentTimeMillis < 250;
            } else {
                this.mCurrentOffset = this.mFinalOffset;
            }
            if (Math.abs(this.mCurrentOffset - this.mFinalOffset) > 1.0E-7f) {
                scheduleUpdate();
            }
            return Math.abs(f - this.mCurrentOffset) > 1.0E-7f;
        }

        @Override // android.view.Choreographer.FrameCallback
        public void doFrame(long j) {
            updateOffset(false);
        }

        public float getCurrX() {
            return this.mCurrentOffset;
        }

        public void jumpToFinal() {
            this.mCurrentOffset = this.mFinalOffset;
        }

        public void setFinalX(float f) {
            scheduleUpdate();
            this.mFinalOffset = Math.max(0.0f, Math.min(f, 1.0f));
            if (getNumScreensExcludingEmptyAndCustom() != this.mNumScreens) {
                if (this.mNumScreens > 0) {
                    animateToFinal();
                }
                this.mNumScreens = getNumScreensExcludingEmptyAndCustom();
            }
        }

        public void syncWithScroll() {
            this.this$0.mWallpaperOffset.setFinalX(wallpaperOffsetForCurrentScroll());
            updateOffset(true);
        }

        public float wallpaperOffsetForScroll(int i) {
            int numScreensExcludingEmptyAndCustom = getNumScreensExcludingEmptyAndCustom();
            int max = this.this$0.mWallpaperIsLiveWallpaper ? numScreensExcludingEmptyAndCustom - 1 : Math.max(3, numScreensExcludingEmptyAndCustom - 1);
            this.this$0.mNumPagesForWallpaperParallax = max;
            if (this.this$0.getChildCount() <= 1) {
                if (this.this$0.mIsRtl) {
                    return 1.0f - (1.0f / this.this$0.mNumPagesForWallpaperParallax);
                }
                return 0.0f;
            }
            int numEmptyScreensToIgnore = numEmptyScreensToIgnore();
            int numCustomPages = this.this$0.numCustomPages();
            int childCount = (this.this$0.getChildCount() - 1) - numEmptyScreensToIgnore;
            int i2 = numCustomPages;
            int i3 = childCount;
            if (this.this$0.mIsRtl) {
                i2 = childCount;
                i3 = numCustomPages;
            }
            int scrollForPage = this.this$0.getScrollForPage(i2);
            int scrollForPage2 = this.this$0.getScrollForPage(i3) - scrollForPage;
            if (scrollForPage2 == 0) {
                return 0.0f;
            }
            float max2 = Math.max(0.0f, Math.min(1.0f, ((i - scrollForPage) - this.this$0.getLayoutTransitionOffsetForPage(0)) / scrollForPage2));
            return (this.this$0.mWallpaperIsLiveWallpaper || numScreensExcludingEmptyAndCustom >= 3 || !this.this$0.mIsRtl) ? ((numScreensExcludingEmptyAndCustom - 1) * max2) / max : (((max - numScreensExcludingEmptyAndCustom) + 1) * max2) / max;
        }
    }

    public Workspace(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public Workspace(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mTouchDownTime = -1L;
        this.mCustomContentShowTime = -1L;
        this.mWorkspaceScreens = new LongArrayMap<>();
        this.mScreenOrder = new ArrayList<>();
        this.mDeferRemoveExtraEmptyScreen = false;
        this.mAddNewPageOnDrag = true;
        this.mTargetCell = new int[2];
        this.mDragOverX = -1;
        this.mDragOverY = -1;
        this.mLastCustomContentScrollProgress = -1.0f;
        this.mCustomContentDescription = "";
        this.mDragTargetLayout = null;
        this.mDragOverlappingLayout = null;
        this.mDropToLayout = null;
        this.mTempCell = new int[2];
        this.mTempPt = new int[2];
        this.mTempEstimate = new int[2];
        this.mDragViewVisualCenter = new float[2];
        this.mTempCellLayoutCenterCoordinates = new float[2];
        this.mTempInverseMatrix = new Matrix();
        this.mTempMatrix = new Matrix();
        this.mState = State.NORMAL;
        this.mIsSwitchingState = false;
        this.mAnimatingViewIntoPlace = false;
        this.mIsDragOccuring = false;
        this.mChildrenLayersEnabled = true;
        this.mStripScreensOnPageStopMoving = false;
        this.mInScrollArea = false;
        this.mDragOutline = null;
        this.mTempXY = new int[2];
        this.mTempVisiblePagesRange = new int[2];
        this.mLastSetWallpaperOffsetSteps = 0.0f;
        this.mDisplaySize = new Point();
        this.mFolderCreationAlarm = new Alarm();
        this.mReorderAlarm = new Alarm();
        this.mDragFolderRingAnimator = null;
        this.mDragOverFolderIcon = null;
        this.mCreateUserFolderOnDrop = false;
        this.mAddToExistingFolderOnDrop = false;
        this.mCanvas = new Canvas();
        this.mDragMode = 0;
        this.mLastReorderX = -1;
        this.mLastReorderY = -1;
        this.mRestoredPages = new ArrayList<>();
        this.mLastOverlaySroll = 0.0f;
        this.mBindPages = new Runnable(this) { // from class: com.android.launcher3.Workspace.1
            final Workspace this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mLauncher.getModel().bindRemainingSynchronousPages();
            }
        };
        this.mAlphaInterpolator = new DecelerateInterpolator(3.0f);
        this.mOutlineHelper = HolographicOutlineHelper.obtain(context);
        this.mLauncher = (Launcher) context;
        this.mStateTransitionAnimation = new WorkspaceStateTransitionAnimation(this.mLauncher, this);
        Resources resources = getResources();
        this.mWorkspaceFadeInAdjacentScreens = this.mLauncher.getDeviceProfile().shouldFadeAdjacentWorkspaceScreens();
        this.mFadeInAdjacentScreens = false;
        this.mWallpaperManager = WallpaperManager.getInstance(context);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.Workspace, i, 0);
        this.mSpringLoadedShrinkFactor = resources.getInteger(2131427334) / 100.0f;
        this.mOverviewModeShrinkFactor = resources.getInteger(2131427335) / 100.0f;
        int i2 = obtainStyledAttributes.getInt(0, 1);
        this.mDefaultPage = i2;
        this.mOriginalDefaultPage = i2;
        obtainStyledAttributes.recycle();
        setOnHierarchyChangeListener(this);
        setHapticFeedbackEnabled(false);
        initWorkspace();
        setMotionEventSplittingEnabled(true);
    }

    private void cleanupAddToFolder() {
        if (this.mDragOverFolderIcon != null) {
            this.mDragOverFolderIcon.onDragExit(null);
            this.mDragOverFolderIcon = null;
        }
    }

    private void cleanupFolderCreation() {
        if (this.mDragFolderRingAnimator != null) {
            this.mDragFolderRingAnimator.animateToNaturalState();
            this.mDragFolderRingAnimator = null;
        }
        this.mFolderCreationAlarm.setOnAlarmListener(null);
        this.mFolderCreationAlarm.cancelAlarm();
    }

    private void cleanupReorder(boolean z) {
        if (z) {
            this.mReorderAlarm.cancelAlarm();
        }
        this.mLastReorderX = -1;
        this.mLastReorderY = -1;
    }

    private void convertFinalScreenToEmptyScreenIfNecessary() {
        if (this.mLauncher.isWorkspaceLoading()) {
            Launcher.addDumpLog("Launcher.Workspace", "    - workspace loading, skip", true);
        } else if (hasExtraEmptyScreen() || this.mScreenOrder.size() == 0) {
        } else {
            long longValue = this.mScreenOrder.get(this.mScreenOrder.size() - 1).longValue();
            if (longValue == -301) {
                return;
            }
            CellLayout cellLayout = this.mWorkspaceScreens.get(longValue);
            if (cellLayout.getShortcutsAndWidgets().getChildCount() != 0 || cellLayout.isDropPending()) {
                return;
            }
            this.mWorkspaceScreens.remove(longValue);
            this.mScreenOrder.remove(Long.valueOf(longValue));
            this.mWorkspaceScreens.put(-201L, cellLayout);
            this.mScreenOrder.add(-201L);
            this.mLauncher.getModel().updateWorkspaceScreenOrder(this.mLauncher, this.mScreenOrder);
        }
    }

    private Bitmap createDragOutline(Bitmap bitmap, int i, int i2, int i3, boolean z) {
        int color = getResources().getColor(2131361803);
        Bitmap createBitmap = Bitmap.createBitmap(i2, i3, Bitmap.Config.ARGB_8888);
        this.mCanvas.setBitmap(createBitmap);
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        float min = Math.min((i2 - i) / bitmap.getWidth(), (i3 - i) / bitmap.getHeight());
        int width = (int) (bitmap.getWidth() * min);
        int height = (int) (bitmap.getHeight() * min);
        Rect rect2 = new Rect(0, 0, width, height);
        rect2.offset((i2 - width) / 2, (i3 - height) / 2);
        this.mCanvas.drawBitmap(bitmap, rect, rect2, (Paint) null);
        this.mOutlineHelper.applyExpensiveOutlineWithBlur(createBitmap, this.mCanvas, color, color, z);
        this.mCanvas.setBitmap(null);
        return createBitmap;
    }

    private Bitmap createDragOutline(View view, int i) {
        int color = getResources().getColor(2131361803);
        Bitmap createBitmap = Bitmap.createBitmap(view.getWidth() + i, view.getHeight() + i, Bitmap.Config.ARGB_8888);
        this.mCanvas.setBitmap(createBitmap);
        drawDragView(view, this.mCanvas, i);
        this.mOutlineHelper.applyExpensiveOutlineWithBlur(createBitmap, this.mCanvas, color, color);
        this.mCanvas.setBitmap(null);
        return createBitmap;
    }

    private static void drawDragView(View view, Canvas canvas, int i) {
        Rect rect = sTempRect;
        view.getDrawingRect(rect);
        canvas.save();
        if (view instanceof TextView) {
            Drawable textViewIcon = getTextViewIcon((TextView) view);
            Rect drawableBounds = getDrawableBounds(textViewIcon);
            rect.set(0, 0, drawableBounds.width() + i, drawableBounds.height() + i);
            canvas.translate((i / 2) - drawableBounds.left, (i / 2) - drawableBounds.top);
            textViewIcon.draw(canvas);
        } else {
            boolean z = false;
            if (view instanceof FolderIcon) {
                z = false;
                if (((FolderIcon) view).getTextVisible()) {
                    ((FolderIcon) view).setTextVisible(false);
                    z = true;
                }
            }
            canvas.translate((-view.getScrollX()) + (i / 2), (-view.getScrollY()) + (i / 2));
            canvas.clipRect(rect, Region.Op.REPLACE);
            view.draw(canvas);
            if (z) {
                ((FolderIcon) view).setTextVisible(true);
            }
        }
        canvas.restore();
    }

    private void enableHwLayersOnVisiblePages() {
        if (this.mChildrenLayersEnabled) {
            int childCount = getChildCount();
            getVisiblePages(this.mTempVisiblePagesRange);
            int i = this.mTempVisiblePagesRange[0];
            int i2 = this.mTempVisiblePagesRange[1];
            int i3 = i;
            int i4 = i2;
            if (i == i2) {
                if (i2 < childCount - 1) {
                    i4 = i2 + 1;
                    i3 = i;
                } else {
                    i3 = i;
                    i4 = i2;
                    if (i > 0) {
                        i3 = i - 1;
                        i4 = i2;
                    }
                }
            }
            CellLayout cellLayout = this.mWorkspaceScreens.get(-301L);
            int i5 = 0;
            while (i5 < childCount) {
                CellLayout cellLayout2 = (CellLayout) getPageAt(i5);
                boolean shouldDrawChild = (cellLayout2 == cellLayout || i3 > i5 || i5 > i4) ? false : shouldDrawChild(cellLayout2);
                if (LauncherLog.DEBUG_DRAW) {
                    LauncherLog.d("Launcher.Workspace", "enableHwLayersOnVisiblePages 1: i = " + i5 + ",enableLayer = " + shouldDrawChild + ",leftScreen = " + i3 + ", rightScreen = " + i4 + ", screenCount = " + childCount + ", customScreen = " + cellLayout + ",shouldDrawChild(layout) = " + shouldDrawChild(cellLayout2));
                }
                cellLayout2.enableHardwareLayer(shouldDrawChild);
                i5++;
            }
        }
    }

    private void enfoceDragParity(View view, String str, int i, int i2) {
        Object tag = view.getTag(2131296257);
        int intValue = (tag == null ? 0 : ((Integer) tag).intValue()) + i;
        view.setTag(2131296257, Integer.valueOf(intValue));
        if (intValue != i2) {
            Log.e("Launcher.Workspace", str + ": Drag contract violated: " + intValue);
        }
    }

    private void enfoceDragParity(String str, int i, int i2) {
        enfoceDragParity(this, str, i, i2);
        for (int i3 = 0; i3 < getChildCount(); i3++) {
            enfoceDragParity(getChildAt(i3), str, i, i2);
        }
    }

    private void fadeAndRemoveEmptyScreen(int i, int i2, Runnable runnable, boolean z) {
        PropertyValuesHolder ofFloat = PropertyValuesHolder.ofFloat("alpha", 0.0f);
        PropertyValuesHolder ofFloat2 = PropertyValuesHolder.ofFloat("backgroundAlpha", 0.0f);
        CellLayout cellLayout = this.mWorkspaceScreens.get(-201L);
        this.mRemoveEmptyScreenRunnable = new Runnable(this, cellLayout, z) { // from class: com.android.launcher3.Workspace.3
            final Workspace this$0;
            final CellLayout val$cl;
            final boolean val$stripEmptyScreens;

            {
                this.this$0 = this;
                this.val$cl = cellLayout;
                this.val$stripEmptyScreens = z;
            }

            @Override // java.lang.Runnable
            public void run() {
                if (this.this$0.hasExtraEmptyScreen()) {
                    this.this$0.mWorkspaceScreens.remove(-201L);
                    this.this$0.mScreenOrder.remove((Object) (-201L));
                    this.this$0.removeView(this.val$cl);
                    if (this.val$stripEmptyScreens) {
                        this.this$0.stripEmptyScreens();
                    }
                }
            }
        };
        ObjectAnimator ofPropertyValuesHolder = ObjectAnimator.ofPropertyValuesHolder(cellLayout, ofFloat, ofFloat2);
        ofPropertyValuesHolder.setDuration(i2);
        ofPropertyValuesHolder.setStartDelay(i);
        ofPropertyValuesHolder.addListener(new AnimatorListenerAdapter(this, runnable) { // from class: com.android.launcher3.Workspace.4
            final Workspace this$0;
            final Runnable val$onComplete;

            {
                this.this$0 = this;
                this.val$onComplete = runnable;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (this.this$0.mRemoveEmptyScreenRunnable != null) {
                    this.this$0.mRemoveEmptyScreenRunnable.run();
                }
                if (this.val$onComplete != null) {
                    this.val$onComplete.run();
                }
            }
        });
        ofPropertyValuesHolder.start();
    }

    private CellLayout findMatchingPageForDragOver(DragView dragView, float f, float f2, boolean z) {
        CellLayout cellLayout;
        float f3;
        int childCount = getChildCount();
        CellLayout cellLayout2 = null;
        float f4 = Float.MAX_VALUE;
        int i = 0;
        while (i < childCount) {
            if (this.mScreenOrder.get(i).longValue() == -301) {
                f3 = f4;
                cellLayout = cellLayout2;
            } else {
                CellLayout cellLayout3 = (CellLayout) getChildAt(i);
                float[] fArr = {f, f2};
                cellLayout3.getMatrix().invert(this.mTempInverseMatrix);
                mapPointFromSelfToChild(cellLayout3, fArr, this.mTempInverseMatrix);
                if (fArr[0] >= 0.0f && fArr[0] <= cellLayout3.getWidth() && fArr[1] >= 0.0f && fArr[1] <= cellLayout3.getHeight()) {
                    return cellLayout3;
                }
                cellLayout = cellLayout2;
                f3 = f4;
                if (!z) {
                    float[] fArr2 = this.mTempCellLayoutCenterCoordinates;
                    fArr2[0] = cellLayout3.getWidth() / 2;
                    fArr2[1] = cellLayout3.getHeight() / 2;
                    mapPointFromChildToSelf(cellLayout3, fArr2);
                    fArr[0] = f;
                    fArr[1] = f2;
                    float squaredDistance = squaredDistance(fArr, fArr2);
                    cellLayout = cellLayout2;
                    f3 = f4;
                    if (squaredDistance < f4) {
                        f3 = squaredDistance;
                        cellLayout = cellLayout3;
                    }
                }
            }
            i++;
            cellLayout2 = cellLayout;
            f4 = f3;
        }
        return cellLayout2;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static Rect getCellLayoutMetrics(Launcher launcher, int i) {
        InvariantDeviceProfile invariantDeviceProfile = LauncherAppState.getInstance().getInvariantDeviceProfile();
        Display defaultDisplay = launcher.getWindowManager().getDefaultDisplay();
        Point point = new Point();
        Point point2 = new Point();
        defaultDisplay.getCurrentSizeRange(point, point2);
        int i2 = invariantDeviceProfile.numColumns;
        int i3 = invariantDeviceProfile.numRows;
        boolean isRtl = Utilities.isRtl(launcher.getResources());
        if (i == 0) {
            if (mLandscapeCellLayoutMetrics == null) {
                Rect workspacePadding = invariantDeviceProfile.landscapeProfile.getWorkspacePadding(isRtl);
                int i4 = point2.x;
                int i5 = workspacePadding.left;
                int i6 = workspacePadding.right;
                int i7 = point.y;
                int i8 = workspacePadding.top;
                int i9 = workspacePadding.bottom;
                mLandscapeCellLayoutMetrics = new Rect();
                mLandscapeCellLayoutMetrics.set(DeviceProfile.calculateCellWidth((i4 - i5) - i6, i2), DeviceProfile.calculateCellHeight((i7 - i8) - i9, i3), 0, 0);
            }
            return mLandscapeCellLayoutMetrics;
        } else if (i == 1) {
            if (mPortraitCellLayoutMetrics == null) {
                Rect workspacePadding2 = invariantDeviceProfile.portraitProfile.getWorkspacePadding(isRtl);
                int i10 = point.x;
                int i11 = workspacePadding2.left;
                int i12 = workspacePadding2.right;
                int i13 = point2.y;
                int i14 = workspacePadding2.top;
                int i15 = workspacePadding2.bottom;
                mPortraitCellLayoutMetrics = new Rect();
                mPortraitCellLayoutMetrics.set(DeviceProfile.calculateCellWidth((i10 - i11) - i12, i2), DeviceProfile.calculateCellHeight((i13 - i14) - i15, i3), 0, 0);
            }
            return mPortraitCellLayoutMetrics;
        } else {
            return null;
        }
    }

    private static Rect getDrawableBounds(Drawable drawable) {
        Rect rect = new Rect();
        drawable.copyBounds(rect);
        if (rect.width() == 0 || rect.height() == 0) {
            rect.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        } else {
            rect.offsetTo(0, 0);
        }
        if (drawable instanceof PreloadIconDrawable) {
            int i = -((PreloadIconDrawable) drawable).getOutset();
            rect.inset(i, i);
        }
        return rect;
    }

    private void getFinalPositionForDropAnimation(int[] iArr, float[] fArr, DragView dragView, CellLayout cellLayout, ItemInfo itemInfo, int[] iArr2, boolean z) {
        float f;
        float f2;
        Rect estimateItemPosition = estimateItemPosition(cellLayout, iArr2[0], iArr2[1], itemInfo.spanX, itemInfo.spanY);
        iArr[0] = estimateItemPosition.left;
        iArr[1] = estimateItemPosition.top;
        setFinalTransitionTransform(cellLayout);
        float descendantCoordRelativeToSelf = this.mLauncher.getDragLayer().getDescendantCoordRelativeToSelf(cellLayout, iArr, true);
        resetTransitionTransform(cellLayout);
        if (z) {
            f = (estimateItemPosition.width() * 1.0f) / dragView.getMeasuredWidth();
            f2 = (estimateItemPosition.height() * 1.0f) / dragView.getMeasuredHeight();
        } else {
            f = 1.0f;
            f2 = 1.0f;
        }
        iArr[0] = (int) (iArr[0] - (((dragView.getMeasuredWidth() - (estimateItemPosition.width() * descendantCoordRelativeToSelf)) / 2.0f) - Math.ceil(cellLayout.getUnusedHorizontalSpace() / 2.0f)));
        iArr[1] = (int) (iArr[1] - ((dragView.getMeasuredHeight() - (estimateItemPosition.height() * descendantCoordRelativeToSelf)) / 2.0f));
        fArr[0] = f * descendantCoordRelativeToSelf;
        fArr[1] = f2 * descendantCoordRelativeToSelf;
    }

    private View getFirstMatch(ItemOperator itemOperator) {
        View[] viewArr = new View[1];
        mapOverItems(false, new ItemOperator(this, itemOperator, viewArr) { // from class: com.android.launcher3.Workspace.18
            final Workspace this$0;
            final ItemOperator val$operator;
            final View[] val$value;

            {
                this.this$0 = this;
                this.val$operator = itemOperator;
                this.val$value = viewArr;
            }

            @Override // com.android.launcher3.Workspace.ItemOperator
            public boolean evaluate(ItemInfo itemInfo, View view, View view2) {
                if (this.val$operator.evaluate(itemInfo, view, view2)) {
                    this.val$value[0] = view;
                    return true;
                }
                return false;
            }
        });
        return viewArr[0];
    }

    private void getOverviewModePages(int[] iArr) {
        int numCustomPages = numCustomPages();
        int childCount = getChildCount();
        iArr[0] = Math.max(0, Math.min(numCustomPages, getChildCount() - 1));
        iArr[1] = Math.max(0, childCount - 1);
    }

    private String getPageDescription(int i) {
        int numCustomPages = numCustomPages();
        int childCount = getChildCount() - numCustomPages;
        int indexOf = this.mScreenOrder.indexOf(-201L);
        int i2 = childCount;
        if (indexOf >= 0) {
            i2 = childCount;
            if (childCount > 1) {
                if (i == indexOf) {
                    return getContext().getString(2131558438);
                }
                i2 = childCount - 1;
            }
        }
        return i2 == 0 ? getContext().getString(2131558421) : getContext().getString(2131558437, Integer.valueOf((i + 1) - numCustomPages), Integer.valueOf(i2));
    }

    public static Drawable getTextViewIcon(TextView textView) {
        Drawable[] compoundDrawables = textView.getCompoundDrawables();
        for (int i = 0; i < compoundDrawables.length; i++) {
            if (compoundDrawables[i] != null) {
                return compoundDrawables[i];
            }
        }
        return null;
    }

    private boolean isDragWidget(DropTarget.DragObject dragObject) {
        return !(dragObject.dragInfo instanceof LauncherAppWidgetInfo) ? dragObject.dragInfo instanceof PendingAddWidgetInfo : true;
    }

    private boolean isExternalDragWidget(DropTarget.DragObject dragObject) {
        return dragObject.dragSource != this ? isDragWidget(dragObject) : false;
    }

    private boolean isScrollingOverlay() {
        boolean z = true;
        if (this.mLauncherOverlay == null) {
            z = false;
        } else if ((!this.mIsRtl || this.mUnboundedScrollX <= this.mMaxScrollX) && (this.mIsRtl || this.mUnboundedScrollX >= 0)) {
            z = false;
        }
        return z;
    }

    private void manageFolderFeedback(CellLayout cellLayout, int[] iArr, float f, DropTarget.DragObject dragObject) {
        if (f > this.mMaxDistanceForFolderCreation) {
            return;
        }
        View childAt = this.mDragTargetLayout.getChildAt(this.mTargetCell[0], this.mTargetCell[1]);
        ItemInfo itemInfo = (ItemInfo) dragObject.dragInfo;
        boolean willCreateUserFolder = willCreateUserFolder(itemInfo, childAt, false);
        if (this.mDragMode == 0 && willCreateUserFolder && !this.mFolderCreationAlarm.alarmPending()) {
            FolderCreationAlarmListener folderCreationAlarmListener = new FolderCreationAlarmListener(this, cellLayout, iArr[0], iArr[1]);
            if (dragObject.accessibleDrag) {
                folderCreationAlarmListener.onAlarm(this.mFolderCreationAlarm);
            } else {
                this.mFolderCreationAlarm.setOnAlarmListener(folderCreationAlarmListener);
                this.mFolderCreationAlarm.setAlarm(0L);
            }
            if (dragObject.stateAnnouncer != null) {
                dragObject.stateAnnouncer.announce(WorkspaceAccessibilityHelper.getDescriptionForDropOver(childAt, getContext()));
                return;
            }
            return;
        }
        boolean willAddToExistingUserFolder = willAddToExistingUserFolder(itemInfo, childAt);
        if (!willAddToExistingUserFolder || this.mDragMode != 0) {
            if (this.mDragMode == 2 && !willAddToExistingUserFolder) {
                setDragMode(0);
            }
            if (this.mDragMode != 1 || willCreateUserFolder) {
                return;
            }
            setDragMode(0);
            return;
        }
        if (this.mLauncher != null) {
            this.mLauncher.closeFolder();
        }
        this.mDragOverFolderIcon = (FolderIcon) childAt;
        this.mDragOverFolderIcon.onDragEnter(itemInfo);
        if (cellLayout != null) {
            cellLayout.clearDragOutlines();
        }
        setDragMode(2);
        if (dragObject.stateAnnouncer != null) {
            dragObject.stateAnnouncer.announce(WorkspaceAccessibilityHelper.getDescriptionForDropOver(childAt, getContext()));
        }
    }

    private void moveToScreen(int i, boolean z) {
        if (!workspaceInModalState()) {
            if (z) {
                snapToPage(i);
            } else {
                setCurrentPage(i);
            }
        }
        View childAt = getChildAt(i);
        if (childAt != null) {
            childAt.requestFocus();
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:40:0x0239, code lost:
        if (willAddToExistingUserFolder((com.android.launcher3.ItemInfo) r18.dragInfo, r16, r13.mTargetCell, r0) != false) goto L42;
     */
    /* JADX WARN: Code restructure failed: missing block: B:52:0x02d6, code lost:
        if (r0[1] != r0.spanY) goto L55;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private void onDropExternal(int[] iArr, Object obj, CellLayout cellLayout, boolean z, DropTarget.DragObject dragObject) {
        ShortcutInfo shortcutInfo;
        FolderIcon fromXml;
        boolean z2;
        Runnable runnable = new Runnable(this) { // from class: com.android.launcher3.Workspace.10
            final Workspace this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mLauncher.exitSpringLoadedDragModeDelayed(true, 300, null);
            }
        };
        ItemInfo itemInfo = (ItemInfo) obj;
        int i = itemInfo.spanX;
        int i2 = itemInfo.spanY;
        if (this.mDragInfo != null) {
            i = this.mDragInfo.spanX;
            i2 = this.mDragInfo.spanY;
        }
        long j = this.mLauncher.isHotseatLayout(cellLayout) ? -101 : -100;
        long idForScreen = getIdForScreen(cellLayout);
        if (idForScreen == -201) {
            if (LauncherLog.DEBUG) {
                LauncherLog.d("Launcher.Workspace", "onDropExternal: screenId = " + idForScreen + "mLauncher.isWorkspaceLoading() = " + this.mLauncher.isWorkspaceLoading());
            }
            int pageIndexForScreenId = getPageIndexForScreenId(-201L);
            CellLayout cellLayout2 = this.mWorkspaceScreens.get(-201L);
            this.mWorkspaceScreens.remove(-201L);
            this.mScreenOrder.remove((Object) (-201L));
            long generateNewScreenId = LauncherAppState.getLauncherProvider().generateNewScreenId();
            this.mWorkspaceScreens.put(generateNewScreenId, cellLayout2);
            this.mScreenOrder.add(Long.valueOf(generateNewScreenId));
            if (getPageIndicator() != null) {
                getPageIndicator().updateMarker(pageIndexForScreenId, getPageIndicatorMarker(pageIndexForScreenId));
            }
            this.mLauncher.getModel().updateWorkspaceScreenOrder(this.mLauncher, this.mScreenOrder);
        }
        if (!this.mLauncher.isHotseatLayout(cellLayout) && idForScreen != getScreenIdForPageIndex(this.mCurrentPage) && this.mState != State.SPRING_LOADED) {
            snapToScreenId(idForScreen, null);
        }
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher.Workspace", "onDropExternal: touchXY[0] = " + (iArr != null ? iArr[0] : -1) + ", touchXY[1] = " + (iArr != null ? iArr[1] : -1) + ", dragInfo = " + obj + ",info = " + itemInfo + ", cellLayout = " + cellLayout + ", insertAtFirst = " + z + ", dragInfo = " + dragObject.dragInfo + ", screenId = " + idForScreen + ", container = " + j);
        }
        if (!(itemInfo instanceof PendingAddItemInfo)) {
            switch (itemInfo.itemType) {
                case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                case 1:
                    shortcutInfo = itemInfo;
                    if (itemInfo.container == -1) {
                        shortcutInfo = itemInfo;
                        if (itemInfo instanceof AppInfo) {
                            shortcutInfo = ((AppInfo) itemInfo).makeShortcut();
                        }
                    }
                    fromXml = this.mLauncher.createShortcut(cellLayout, (ShortcutInfo) shortcutInfo);
                    break;
                case 2:
                    shortcutInfo = itemInfo;
                    fromXml = FolderIcon.fromXml(2130968590, this.mLauncher, cellLayout, (FolderInfo) itemInfo, this.mIconCache);
                    break;
                default:
                    throw new IllegalStateException("Unknown item type: " + itemInfo.itemType);
            }
            if (iArr != null) {
                this.mTargetCell = findNearestArea(iArr[0], iArr[1], i, i2, cellLayout, this.mTargetCell);
                float distanceFromCell = cellLayout.getDistanceFromCell(this.mDragViewVisualCenter[0], this.mDragViewVisualCenter[1], this.mTargetCell);
                dragObject.postAnimationRunnable = runnable;
                if (createUserFolderIfNecessary(fromXml, j, cellLayout, this.mTargetCell, distanceFromCell, true, dragObject.dragView, dragObject.postAnimationRunnable) || addToExistingFolderIfNecessary(fromXml, cellLayout, this.mTargetCell, distanceFromCell, dragObject, true)) {
                    return;
                }
            }
            if (iArr != null) {
                this.mTargetCell = cellLayout.performReorder((int) this.mDragViewVisualCenter[0], (int) this.mDragViewVisualCenter[1], 1, 1, 1, 1, null, this.mTargetCell, null, 3);
            } else {
                cellLayout.findCellForSpan(this.mTargetCell, 1, 1);
            }
            LauncherModel.addOrMoveItemInDatabase(this.mLauncher, shortcutInfo, j, idForScreen, this.mTargetCell[0], this.mTargetCell[1]);
            addInScreen(fromXml, j, idForScreen, this.mTargetCell[0], this.mTargetCell[1], shortcutInfo.spanX, shortcutInfo.spanY, z);
            cellLayout.onDropChild(fromXml);
            cellLayout.getShortcutsAndWidgets().measureChild(fromXml);
            if (dragObject.dragView != null) {
                setFinalTransitionTransform(cellLayout);
                this.mLauncher.getDragLayer().animateViewIntoPosition(dragObject.dragView, fromXml, runnable, this);
                resetTransitionTransform(cellLayout);
                return;
            }
            return;
        }
        PendingAddItemInfo pendingAddItemInfo = (PendingAddItemInfo) obj;
        boolean z3 = true;
        if (pendingAddItemInfo.itemType == 1) {
            this.mTargetCell = findNearestArea(iArr[0], iArr[1], i, i2, cellLayout, this.mTargetCell);
            float distanceFromCell2 = cellLayout.getDistanceFromCell(this.mDragViewVisualCenter[0], this.mDragViewVisualCenter[1], this.mTargetCell);
            if (!willCreateUserFolder((ItemInfo) dragObject.dragInfo, cellLayout, this.mTargetCell, distanceFromCell2, true)) {
                z3 = true;
            }
            z3 = false;
        }
        ItemInfo itemInfo2 = (ItemInfo) dragObject.dragInfo;
        boolean z4 = false;
        if (z3) {
            int i3 = itemInfo2.spanX;
            int i4 = itemInfo2.spanY;
            int i5 = i3;
            int i6 = i4;
            if (itemInfo2.minSpanX > 0) {
                i5 = i3;
                i6 = i4;
                if (itemInfo2.minSpanY > 0) {
                    i5 = itemInfo2.minSpanX;
                    i6 = itemInfo2.minSpanY;
                }
            }
            int[] iArr2 = new int[2];
            this.mTargetCell = cellLayout.performReorder((int) this.mDragViewVisualCenter[0], (int) this.mDragViewVisualCenter[1], i5, i6, itemInfo.spanX, itemInfo.spanY, null, this.mTargetCell, iArr2, 3);
            if (iArr2[0] == itemInfo2.spanX) {
                z2 = false;
            }
            z2 = true;
            itemInfo2.spanX = iArr2[0];
            itemInfo2.spanY = iArr2[1];
            z4 = z2;
        }
        Runnable runnable2 = new Runnable(this, pendingAddItemInfo, j, idForScreen, itemInfo2) { // from class: com.android.launcher3.Workspace.11
            final Workspace this$0;
            final long val$container;
            final ItemInfo val$item;
            final PendingAddItemInfo val$pendingInfo;
            final long val$screenId;

            {
                this.this$0 = this;
                this.val$pendingInfo = pendingAddItemInfo;
                this.val$container = j;
                this.val$screenId = idForScreen;
                this.val$item = itemInfo2;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.deferRemoveExtraEmptyScreen();
                this.this$0.mLauncher.addPendingItem(this.val$pendingInfo, this.val$container, this.val$screenId, this.this$0.mTargetCell, this.val$item.spanX, this.val$item.spanY);
            }
        };
        boolean z5 = pendingAddItemInfo.itemType != 4 ? pendingAddItemInfo.itemType == 5 : true;
        AppWidgetHostView appWidgetHostView = z5 ? ((PendingAddWidgetInfo) pendingAddItemInfo).boundWidget : null;
        if (appWidgetHostView != null && z4) {
            AppWidgetResizeFrame.updateWidgetSizeRanges(appWidgetHostView, this.mLauncher, itemInfo2.spanX, itemInfo2.spanY);
        }
        int i7 = 0;
        if (z5) {
            i7 = 0;
            if (((PendingAddWidgetInfo) pendingAddItemInfo).info != null) {
                i7 = 0;
                if (((PendingAddWidgetInfo) pendingAddItemInfo).info.configure != null) {
                    i7 = 1;
                }
            }
        }
        animateWidgetDrop(itemInfo, cellLayout, dragObject.dragView, runnable2, i7, appWidgetHostView, true);
    }

    private void onResetScrollArea() {
        setCurrentDragOverlappingLayout(null);
        this.mInScrollArea = false;
    }

    private void setupLayoutTransition() {
        this.mLayoutTransition = new LayoutTransition();
        this.mLayoutTransition.enableTransitionType(3);
        this.mLayoutTransition.enableTransitionType(1);
        this.mLayoutTransition.disableTransitionType(2);
        this.mLayoutTransition.disableTransitionType(0);
        setLayoutTransition(this.mLayoutTransition);
    }

    private static float squaredDistance(float[] fArr, float[] fArr2) {
        float f = fArr[0] - fArr2[0];
        float f2 = fArr2[1] - fArr2[1];
        return (f * f) + (f2 * f2);
    }

    private void updateAccessibilityFlags(CellLayout cellLayout, int i) {
        if (this.mState != State.OVERVIEW) {
            int i2 = this.mState == State.NORMAL ? 0 : 4;
            cellLayout.setImportantForAccessibility(2);
            cellLayout.getShortcutsAndWidgets().setImportantForAccessibility(i2);
            cellLayout.setContentDescription(null);
            cellLayout.setAccessibilityDelegate(null);
            return;
        }
        cellLayout.setImportantForAccessibility(1);
        cellLayout.getShortcutsAndWidgets().setImportantForAccessibility(4);
        cellLayout.setContentDescription(getPageDescription(i));
        if (this.mPagesAccessibilityDelegate == null) {
            this.mPagesAccessibilityDelegate = new OverviewScreenAccessibilityDelegate(this);
        }
        cellLayout.setAccessibilityDelegate(this.mPagesAccessibilityDelegate);
    }

    private void updatePageAlphaValues(int i) {
        if (!this.mWorkspaceFadeInAdjacentScreens || workspaceInModalState() || this.mIsSwitchingState) {
            return;
        }
        for (int numCustomPages = numCustomPages(); numCustomPages < getChildCount(); numCustomPages++) {
            CellLayout cellLayout = (CellLayout) getChildAt(numCustomPages);
            if (cellLayout != null) {
                cellLayout.getShortcutsAndWidgets().setAlpha(1.0f - Math.abs(getScrollProgress(i, cellLayout, numCustomPages)));
            }
        }
    }

    private void updateStateForCustomContent(int i) {
        float f = 0.0f;
        float f2 = 0.0f;
        float f3 = 0.0f;
        if (hasCustomContent()) {
            int indexOf = this.mScreenOrder.indexOf(-301L);
            int scrollX = (getScrollX() - getScrollForPage(indexOf)) - getLayoutTransitionOffsetForPage(indexOf);
            float scrollForPage = getScrollForPage(indexOf + 1) - getScrollForPage(indexOf);
            float f4 = scrollForPage - scrollX;
            float f5 = (scrollForPage - scrollX) / scrollForPage;
            f2 = this.mIsRtl ? Math.min(0.0f, f4) : Math.max(0.0f, f4);
            f3 = Math.max(0.0f, f5);
        }
        if (Float.compare(f3, this.mLastCustomContentScrollProgress) == 0) {
            return;
        }
        CellLayout cellLayout = this.mWorkspaceScreens.get(-301L);
        if (f3 > 0.0f && cellLayout.getVisibility() != 0 && !workspaceInModalState()) {
            cellLayout.setVisibility(0);
        }
        this.mLastCustomContentScrollProgress = f3;
        if (this.mState == State.NORMAL) {
            DragLayer dragLayer = this.mLauncher.getDragLayer();
            if (f3 != 1.0f) {
                f = 0.8f * f3;
            }
            dragLayer.setBackgroundAlpha(f);
        }
        if (this.mLauncher.getHotseat() != null) {
            this.mLauncher.getHotseat().setTranslationX(f2);
        }
        if (getPageIndicator() != null) {
            getPageIndicator().setTranslationX(f2);
        }
        if (this.mCustomContentCallbacks != null) {
            this.mCustomContentCallbacks.onScrollProgressChanged(f3);
        }
    }

    @Override // com.android.launcher3.DropTarget
    public boolean acceptDrop(DropTarget.DragObject dragObject) {
        int i;
        int i2;
        CellLayout cellLayout = this.mDropToLayout;
        if (dragObject.dragSource != this) {
            if (cellLayout == null || !transitionStateShouldAllowDrop()) {
                return false;
            }
            this.mDragViewVisualCenter = dragObject.getVisualCenter(this.mDragViewVisualCenter);
            if (this.mLauncher.isHotseatLayout(cellLayout)) {
                mapPointFromSelfToHotseatLayout(this.mLauncher.getHotseat(), this.mDragViewVisualCenter);
            } else {
                mapPointFromSelfToChild(cellLayout, this.mDragViewVisualCenter, null);
            }
            if (this.mDragInfo != null) {
                CellLayout.CellInfo cellInfo = this.mDragInfo;
                i = cellInfo.spanX;
                i2 = cellInfo.spanY;
            } else {
                ItemInfo itemInfo = (ItemInfo) dragObject.dragInfo;
                i = itemInfo.spanX;
                i2 = itemInfo.spanY;
            }
            int i3 = i;
            int i4 = i2;
            if (dragObject.dragInfo instanceof PendingAddWidgetInfo) {
                i3 = ((PendingAddWidgetInfo) dragObject.dragInfo).minSpanX;
                i4 = ((PendingAddWidgetInfo) dragObject.dragInfo).minSpanY;
            }
            this.mTargetCell = findNearestArea((int) this.mDragViewVisualCenter[0], (int) this.mDragViewVisualCenter[1], i3, i4, cellLayout, this.mTargetCell);
            float distanceFromCell = cellLayout.getDistanceFromCell(this.mDragViewVisualCenter[0], this.mDragViewVisualCenter[1], this.mTargetCell);
            if (this.mCreateUserFolderOnDrop && willCreateUserFolder((ItemInfo) dragObject.dragInfo, cellLayout, this.mTargetCell, distanceFromCell, true)) {
                return true;
            }
            if (this.mAddToExistingFolderOnDrop && willAddToExistingUserFolder((ItemInfo) dragObject.dragInfo, cellLayout, this.mTargetCell, distanceFromCell)) {
                return true;
            }
            this.mTargetCell = cellLayout.performReorder((int) this.mDragViewVisualCenter[0], (int) this.mDragViewVisualCenter[1], i3, i4, i, i2, null, this.mTargetCell, new int[2], 4);
            if (!(this.mTargetCell[0] >= 0 && this.mTargetCell[1] >= 0)) {
                boolean isHotseatLayout = this.mLauncher.isHotseatLayout(cellLayout);
                if (this.mTargetCell != null && isHotseatLayout) {
                    Hotseat hotseat = this.mLauncher.getHotseat();
                    if (hotseat.isAllAppsButtonRank(hotseat.getOrderInHotseat(this.mTargetCell[0], this.mTargetCell[1]))) {
                        return false;
                    }
                }
                this.mLauncher.showOutOfSpaceMessage(isHotseatLayout);
                return false;
            }
        }
        if (getIdForScreen(cellLayout) == -201) {
            commitExtraEmptyScreen();
            return true;
        }
        return true;
    }

    public boolean addExtraEmptyScreen() {
        if (this.mWorkspaceScreens.containsKey(-201L)) {
            return false;
        }
        insertNewWorkspaceScreen(-201L);
        return true;
    }

    public void addExtraEmptyScreenOnDrag() {
        boolean z = false;
        boolean z2 = false;
        this.mRemoveEmptyScreenRunnable = null;
        boolean z3 = false;
        if (this.mDragSourceInternal != null) {
            if (this.mDragSourceInternal.getChildCount() == 1) {
                z2 = true;
            }
            z3 = false;
            z = z2;
            if (indexOfChild((CellLayout) this.mDragSourceInternal.getParent()) == getChildCount() - 1) {
                z3 = true;
                z = z2;
            }
        }
        if ((z && z3) || this.mWorkspaceScreens.containsKey(-201L)) {
            return;
        }
        insertNewWorkspaceScreen(-201L);
    }

    @Override // com.android.launcher3.PagedView, android.view.ViewGroup, android.view.View
    public void addFocusables(ArrayList<View> arrayList, int i, int i2) {
        if (this.mLauncher.isAppsViewVisible()) {
            return;
        }
        Folder openFolder = getOpenFolder();
        if (openFolder != null) {
            openFolder.addFocusables(arrayList, i);
        } else {
            super.addFocusables(arrayList, i, i2);
        }
    }

    void addInScreen(View view, long j, long j2, int i, int i2, int i3, int i4) {
        addInScreen(view, j, j2, i, i2, i3, i4, false, false);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void addInScreen(View view, long j, long j2, int i, int i2, int i3, int i4, boolean z) {
        addInScreen(view, j, j2, i, i2, i3, i4, z, false);
    }

    void addInScreen(View view, long j, long j2, int i, int i2, int i3, int i4, boolean z, boolean z2) {
        CellLayout screenWithId;
        CellLayout.LayoutParams layoutParams;
        if (j == -100 && getScreenWithId(j2) == null) {
            Log.e("Launcher.Workspace", "Skipping child, screenId " + j2 + " not found");
            new Throwable().printStackTrace();
        } else if (j2 == -201) {
            throw new RuntimeException("Screen id should not be EXTRA_EMPTY_SCREEN_ID");
        } else {
            if (j == -101) {
                screenWithId = this.mLauncher.getHotseat().getLayout();
                view.setOnKeyListener(new HotseatIconKeyEventListener());
                if (view instanceof FolderIcon) {
                    ((FolderIcon) view).setTextVisible(false);
                }
                if (z2) {
                    i = this.mLauncher.getHotseat().getCellXFromOrder((int) j2);
                    i2 = this.mLauncher.getHotseat().getCellYFromOrder((int) j2);
                } else {
                    j2 = this.mLauncher.getHotseat().getOrderInHotseat(i, i2);
                }
            } else {
                if (view instanceof FolderIcon) {
                    ((FolderIcon) view).setTextVisible(true);
                }
                screenWithId = getScreenWithId(j2);
                view.setOnKeyListener(new IconKeyEventListener());
            }
            ViewGroup.LayoutParams layoutParams2 = view.getLayoutParams();
            if (layoutParams2 == null || !(layoutParams2 instanceof CellLayout.LayoutParams)) {
                layoutParams = new CellLayout.LayoutParams(i, i2, i3, i4);
            } else {
                layoutParams = (CellLayout.LayoutParams) layoutParams2;
                layoutParams.cellX = i;
                layoutParams.cellY = i2;
                layoutParams.cellHSpan = i3;
                layoutParams.cellVSpan = i4;
            }
            if (i3 < 0 && i4 < 0) {
                layoutParams.isLockedToGrid = false;
            }
            int viewIdForItem = this.mLauncher.getViewIdForItem((ItemInfo) view.getTag());
            boolean z3 = !(view instanceof Folder);
            if (z3 && !this.mIsDragOccuring && j == -100) {
                for (int i5 = layoutParams.cellX; i5 < layoutParams.cellX + layoutParams.cellHSpan; i5++) {
                    for (int i6 = layoutParams.cellY; i6 < layoutParams.cellY + layoutParams.cellVSpan; i6++) {
                        if (i5 >= screenWithId.getCountX() || i6 >= screenWithId.getCountY()) {
                            Launcher.addDumpLog("Launcher.Workspace", "Position exceeds the bound of this CellLayout.i:" + i5 + ",layout.getCountX():" + screenWithId.getCountX() + ",j:" + i6 + ",layout.getCountY():" + screenWithId.getCountY(), true);
                            return;
                        } else if (screenWithId.isOccupied(i5, i6)) {
                            Launcher.addDumpLog("Launcher.Workspace", "layout.isOccupied, screenId:" + j2 + ",x:" + i5 + ",y:" + i6 + ",lp.cellHSpan:" + layoutParams.cellHSpan + ", lp.cellVSpan" + layoutParams.cellVSpan, true);
                            return;
                        }
                    }
                }
            }
            if (!screenWithId.addViewToCellLayout(view, z ? 0 : -1, viewIdForItem, layoutParams, z3)) {
                Launcher.addDumpLog("Launcher.Workspace", "Failed to add to item at (" + layoutParams.cellX + "," + layoutParams.cellY + ") to CellLayout", true);
            }
            if (!(view instanceof Folder)) {
                view.setHapticFeedbackEnabled(false);
                view.setOnLongClickListener(this.mLongClickListener);
            }
            if (view instanceof DropTarget) {
                this.mDragController.addDropTarget((DropTarget) view);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void addInScreenFromBind(View view, long j, long j2, int i, int i2, int i3, int i4) {
        addInScreen(view, j, j2, i, i2, i3, i4, false, true);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean addToExistingFolderIfNecessary(View view, CellLayout cellLayout, int[] iArr, float f, DropTarget.DragObject dragObject, boolean z) {
        if (f > this.mMaxDistanceForFolderCreation) {
            return false;
        }
        View childAt = cellLayout.getChildAt(iArr[0], iArr[1]);
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher.Workspace", "createUserFolderIfNecessary: newView = " + view + ", target = " + cellLayout + ", targetCell[0] = " + iArr[0] + ", targetCell[1] = " + iArr[1] + ", external = " + z + ", d = " + dragObject + ", dropOverView = " + childAt);
        }
        if (this.mAddToExistingFolderOnDrop) {
            this.mAddToExistingFolderOnDrop = false;
            if (childAt instanceof FolderIcon) {
                FolderIcon folderIcon = (FolderIcon) childAt;
                if (folderIcon.acceptDrop(dragObject.dragInfo)) {
                    folderIcon.onDrop(dragObject);
                    if (!z) {
                        getParentCellLayoutForView(this.mDragInfo.cell).removeView(this.mDragInfo.cell);
                    }
                    if (LauncherLog.DEBUG) {
                        LauncherLog.d("Launcher.Workspace", "addToExistingFolderIfNecessary: fi = " + folderIcon + ", d = " + dragObject);
                        return true;
                    }
                    return true;
                }
                return false;
            }
            return false;
        }
        return false;
    }

    public void animateWidgetDrop(ItemInfo itemInfo, CellLayout cellLayout, DragView dragView, Runnable runnable, int i, View view, boolean z) {
        Rect rect = new Rect();
        this.mLauncher.getDragLayer().getViewRectRelativeToSelf(dragView, rect);
        int[] iArr = new int[2];
        float[] fArr = new float[2];
        boolean z2 = !(itemInfo instanceof PendingAddShortcutInfo);
        getFinalPositionForDropAnimation(iArr, fArr, dragView, cellLayout, itemInfo, this.mTargetCell, z2);
        int integer = this.mLauncher.getResources().getInteger(2131427344) - 200;
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher.Workspace", "animateWidgetDrop: info = " + itemInfo + ", animationType = " + i + ", finalPos = (" + iArr[0] + ", " + iArr[1] + "), scaleXY = (" + fArr[0] + ", " + fArr[1] + "), scalePreview = " + z2 + ",external = " + z);
        }
        if ((view instanceof AppWidgetHostView) && z) {
            this.mLauncher.getDragLayer().removeView(view);
        }
        boolean z3 = itemInfo.itemType != 4 ? itemInfo.itemType == 5 : true;
        if ((i == 2 || z) && view != null) {
            dragView.setCrossFadeBitmap(createWidgetBitmap(itemInfo, view));
            dragView.crossFade((int) (integer * 0.8f));
        } else if (z3 && z) {
            float min = Math.min(fArr[0], fArr[1]);
            fArr[1] = min;
            fArr[0] = min;
        }
        DragLayer dragLayer = this.mLauncher.getDragLayer();
        if (i == 4) {
            this.mLauncher.getDragLayer().animateViewIntoPosition(dragView, iArr, 0.0f, 0.1f, 0.1f, 0, runnable, integer);
        } else {
            dragLayer.animateViewIntoPosition(dragView, rect.left, rect.top, iArr[0], iArr[1], 1.0f, 1.0f, 1.0f, fArr[0], fArr[1], new Runnable(this, view, runnable) { // from class: com.android.launcher3.Workspace.12
                final Workspace this$0;
                final View val$finalView;
                final Runnable val$onCompleteRunnable;

                {
                    this.this$0 = this;
                    this.val$finalView = view;
                    this.val$onCompleteRunnable = runnable;
                }

                @Override // java.lang.Runnable
                public void run() {
                    if (this.val$finalView != null) {
                        this.val$finalView.setVisibility(0);
                    }
                    if (this.val$onCompleteRunnable != null) {
                        this.val$onCompleteRunnable.run();
                    }
                }
            }, i == 1 ? 2 : 0, integer, this);
        }
    }

    @Override // android.view.View
    public void announceForAccessibility(CharSequence charSequence) {
        if (this.mLauncher.isAppsViewVisible()) {
            return;
        }
        super.announceForAccessibility(charSequence);
    }

    public void beginDragShared(View view, Point point, DragSource dragSource, boolean z) {
        int i;
        int i2;
        Point point2;
        view.clearFocus();
        view.setPressed(false);
        this.mDragOutline = createDragOutline(view, 2);
        this.mLauncher.onDragStarted(view);
        AtomicInteger atomicInteger = new AtomicInteger(2);
        Bitmap createDragBitmap = createDragBitmap(view, atomicInteger);
        int width = createDragBitmap.getWidth();
        int height = createDragBitmap.getHeight();
        float locationInDragLayer = this.mLauncher.getDragLayer().getLocationInDragLayer(view, this.mTempXY);
        int round = Math.round(this.mTempXY[0] - ((width - (view.getWidth() * locationInDragLayer)) / 2.0f));
        int round2 = Math.round((this.mTempXY[1] - ((height - (height * locationInDragLayer)) / 2.0f)) - (atomicInteger.get() / 2));
        if (LauncherLog.DEBUG_DRAG) {
            LauncherLog.d("Launcher.Workspace", "beginDragShared: child = " + view + ", source = " + dragSource + ", dragLayerX = " + round + ", dragLayerY = " + round2);
        }
        DeviceProfile deviceProfile = this.mLauncher.getDeviceProfile();
        Rect rect = null;
        if (view instanceof BubbleTextView) {
            BubbleTextView bubbleTextView = (BubbleTextView) view;
            int i3 = deviceProfile.iconSizePx;
            int paddingTop = view.getPaddingTop();
            int i4 = (width - i3) / 2;
            if (bubbleTextView.isLayoutHorizontal()) {
                round = bubbleTextView.getIcon().getBounds().contains(point.x, point.y) ? Math.round(this.mTempXY[0]) : Math.round((this.mTempXY[0] + point.x) - (width / 2));
            }
            i2 = round2 + paddingTop;
            point2 = new Point((-atomicInteger.get()) / 2, atomicInteger.get() / 2);
            rect = new Rect(i4, paddingTop, i4 + i3, paddingTop + i3);
            i = round;
        } else {
            i = round;
            i2 = round2;
            point2 = null;
            if (view instanceof FolderIcon) {
                int i5 = deviceProfile.folderIconSizePx;
                point2 = new Point((-atomicInteger.get()) / 2, (atomicInteger.get() / 2) - view.getPaddingTop());
                rect = new Rect(0, view.getPaddingTop(), view.getWidth(), i5);
                i = round;
                i2 = round2;
            }
        }
        if (view instanceof BubbleTextView) {
            ((BubbleTextView) view).clearPressedBackground();
        }
        if (view.getTag() == null || !(view.getTag() instanceof ItemInfo)) {
            throw new IllegalStateException("Drag started with a view that has no tag set. This will cause a crash (issue 11627249) down the line. View: " + view + "  tag: " + view.getTag());
        }
        if (view.getParent() instanceof ShortcutAndWidgetContainer) {
            this.mDragSourceInternal = (ShortcutAndWidgetContainer) view.getParent();
        }
        this.mDragController.startDrag(createDragBitmap, i, i2, dragSource, view.getTag(), DragController.DRAG_ACTION_MOVE, point2, rect, locationInDragLayer, z).setIntrinsicIconScaleFactor(dragSource.getIntrinsicIconScaleFactor());
        createDragBitmap.recycle();
    }

    public void beginDragShared(View view, DragSource dragSource, boolean z) {
        beginDragShared(view, new Point(), dragSource, z);
    }

    public void buildPageHardwareLayers() {
        updateChildrenLayersEnabled(true);
        if (getWindowToken() != null) {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                ((CellLayout) getChildAt(i)).buildHardwareLayer();
            }
        }
        updateChildrenLayersEnabled(false);
    }

    void clearChildrenCache() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            CellLayout cellLayout = (CellLayout) getChildAt(i);
            cellLayout.setChildrenDrawnWithCacheEnabled(false);
            if (!isHardwareAccelerated()) {
                cellLayout.setChildrenDrawingCacheEnabled(false);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void clearDropTargets() {
        mapOverItems(false, new ItemOperator(this) { // from class: com.android.launcher3.Workspace.19
            final Workspace this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.launcher3.Workspace.ItemOperator
            public boolean evaluate(ItemInfo itemInfo, View view, View view2) {
                if (view instanceof DropTarget) {
                    this.this$0.mDragController.removeDropTarget((DropTarget) view);
                    return false;
                }
                return false;
            }
        });
    }

    public long commitExtraEmptyScreen() {
        if (this.mLauncher.isWorkspaceLoading()) {
            Launcher.addDumpLog("Launcher.Workspace", "    - workspace loading, skip", true);
            return -1L;
        }
        int pageIndexForScreenId = getPageIndexForScreenId(-201L);
        CellLayout cellLayout = this.mWorkspaceScreens.get(-201L);
        this.mWorkspaceScreens.remove(-201L);
        this.mScreenOrder.remove((Object) (-201L));
        long generateNewScreenId = LauncherAppState.getLauncherProvider().generateNewScreenId();
        this.mWorkspaceScreens.put(generateNewScreenId, cellLayout);
        this.mScreenOrder.add(Long.valueOf(generateNewScreenId));
        if (getPageIndicator() != null) {
            getPageIndicator().updateMarker(pageIndexForScreenId, getPageIndicatorMarker(pageIndexForScreenId));
        }
        this.mLauncher.getModel().updateWorkspaceScreenOrder(this.mLauncher, this.mScreenOrder);
        return generateNewScreenId;
    }

    @Override // com.android.launcher3.PagedView, android.view.View
    public void computeScroll() {
        super.computeScroll();
        this.mWallpaperOffset.syncWithScroll();
    }

    public void createCustomContentContainer() {
        CellLayout cellLayout = (CellLayout) this.mLauncher.getLayoutInflater().inflate(2130968612, (ViewGroup) this, false);
        cellLayout.disableDragTarget();
        cellLayout.disableJailContent();
        this.mWorkspaceScreens.put(-301L, cellLayout);
        this.mScreenOrder.add(0, -301L);
        cellLayout.setPadding(0, 0, 0, 0);
        addFullScreenPage(cellLayout);
        this.mDefaultPage = this.mOriginalDefaultPage + 1;
        if (this.mRestorePage != -1001) {
            this.mRestorePage++;
        } else {
            setCurrentPage(getCurrentPage() + 1);
        }
    }

    public Bitmap createDragBitmap(View view, AtomicInteger atomicInteger) {
        Bitmap createBitmap;
        int i = atomicInteger.get();
        if (view instanceof TextView) {
            Rect drawableBounds = getDrawableBounds(getTextViewIcon((TextView) view));
            Bitmap createBitmap2 = Bitmap.createBitmap(drawableBounds.width() + i, drawableBounds.height() + i, Bitmap.Config.ARGB_8888);
            atomicInteger.set((i - drawableBounds.left) - drawableBounds.top);
            createBitmap = createBitmap2;
        } else {
            createBitmap = Bitmap.createBitmap(view.getWidth() + i, view.getHeight() + i, Bitmap.Config.ARGB_8888);
        }
        this.mCanvas.setBitmap(createBitmap);
        drawDragView(view, this.mCanvas, i);
        this.mCanvas.setBitmap(null);
        return createBitmap;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean createUserFolderIfNecessary(View view, long j, CellLayout cellLayout, int[] iArr, float f, boolean z, DragView dragView, Runnable runnable) {
        if (f > this.mMaxDistanceForFolderCreation) {
            return false;
        }
        View childAt = cellLayout.getChildAt(iArr[0], iArr[1]);
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher.Workspace", "createUserFolderIfNecessary: newView = " + view + ", mDragInfo = " + this.mDragInfo + ", container = " + j + ", target = " + cellLayout + ", targetCell[0] = " + iArr[0] + ", targetCell[1] = " + iArr[1] + ", external = " + z + ", dragView = " + dragView + ", v = " + childAt + ", mCreateUserFolderOnDrop = " + this.mCreateUserFolderOnDrop);
        }
        boolean z2 = false;
        if (this.mDragInfo != null) {
            z2 = (this.mDragInfo.cellX == iArr[0] && this.mDragInfo.cellY == iArr[1]) ? getParentCellLayoutForView(this.mDragInfo.cell) == cellLayout : false;
        }
        if (childAt == null || z2 || !this.mCreateUserFolderOnDrop) {
            if (LauncherLog.DEBUG) {
                LauncherLog.d("Launcher.Workspace", "Do not create user folder: hasntMoved = " + z2 + ", mCreateUserFolderOnDrop = " + this.mCreateUserFolderOnDrop + ", v = " + childAt);
                return false;
            }
            return false;
        }
        this.mCreateUserFolderOnDrop = false;
        long idForScreen = getIdForScreen(cellLayout);
        boolean z3 = childAt.getTag() instanceof ShortcutInfo;
        boolean z4 = view.getTag() instanceof ShortcutInfo;
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher.Workspace", "createUserFolderIfNecessary: aboveShortcut = " + z3 + ", willBecomeShortcut = " + z4);
        }
        if (z3 && z4) {
            ShortcutInfo shortcutInfo = (ShortcutInfo) view.getTag();
            ShortcutInfo shortcutInfo2 = (ShortcutInfo) childAt.getTag();
            if (!z) {
                getParentCellLayoutForView(this.mDragInfo.cell).removeView(this.mDragInfo.cell);
            }
            Rect rect = new Rect();
            float descendantRectRelativeToSelf = this.mLauncher.getDragLayer().getDescendantRectRelativeToSelf(childAt, rect);
            cellLayout.removeView(childAt);
            FolderIcon addFolder = this.mLauncher.addFolder(cellLayout, j, idForScreen, iArr[0], iArr[1]);
            shortcutInfo2.cellX = -1;
            shortcutInfo2.cellY = -1;
            shortcutInfo.cellX = -1;
            shortcutInfo.cellY = -1;
            if (dragView != null) {
                addFolder.performCreateAnimation(shortcutInfo2, childAt, shortcutInfo, dragView, rect, descendantRectRelativeToSelf, runnable);
                return true;
            }
            addFolder.addItem(shortcutInfo2);
            addFolder.addItem(shortcutInfo);
            return true;
        }
        return false;
    }

    public Bitmap createWidgetBitmap(ItemInfo itemInfo, View view) {
        int[] estimateItemSize = this.mLauncher.getWorkspace().estimateItemSize(itemInfo, false);
        int visibility = view.getVisibility();
        view.setVisibility(0);
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(estimateItemSize[0], 1073741824);
        int makeMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(estimateItemSize[1], 1073741824);
        Bitmap createBitmap = Bitmap.createBitmap(estimateItemSize[0], estimateItemSize[1], Bitmap.Config.ARGB_8888);
        this.mCanvas.setBitmap(createBitmap);
        view.measure(makeMeasureSpec, makeMeasureSpec2);
        view.layout(0, 0, estimateItemSize[0], estimateItemSize[1]);
        view.draw(this.mCanvas);
        this.mCanvas.setBitmap(null);
        view.setVisibility(visibility);
        return createBitmap;
    }

    @Override // com.android.launcher3.UninstallDropTarget.UninstallSource
    public void deferCompleteDropAfterUninstallActivity() {
        this.mDeferDropAfterUninstall = true;
    }

    public void deferRemoveExtraEmptyScreen() {
        this.mDeferRemoveExtraEmptyScreen = true;
    }

    @Override // com.android.launcher3.PagedView
    protected void determineScrollingStart(MotionEvent motionEvent) {
        if (isFinishedSwitchingState()) {
            float x = motionEvent.getX() - this.mXDown;
            float abs = Math.abs(x);
            float abs2 = Math.abs(motionEvent.getY() - this.mYDown);
            if (Float.compare(abs, 0.0f) == 0) {
                return;
            }
            float atan = (float) Math.atan(abs2 / abs);
            if (abs > this.mTouchSlop || abs2 > this.mTouchSlop) {
                cancelCurrentPageLongPress();
            }
            boolean z = this.mTouchDownTime - this.mCustomContentShowTime > 200;
            boolean z2 = !this.mIsRtl ? x <= 0.0f : x >= 0.0f;
            boolean z3 = getScreenIdForPageIndex(getCurrentPage()) == -301;
            if (z2 && z3 && z) {
                return;
            }
            if ((!z3 || this.mCustomContentCallbacks == null || this.mCustomContentCallbacks.isScrollingAllowed()) && atan <= 1.0471976f) {
                if (atan > 0.5235988f) {
                    super.determineScrollingStart(motionEvent, (4.0f * ((float) Math.sqrt((atan - 0.5235988f) / 0.5235988f))) + 1.0f);
                } else {
                    super.determineScrollingStart(motionEvent, 0.5f);
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.launcher3.PagedView
    public void determineScrollingStart(MotionEvent motionEvent, float f) {
        if (isSwitchingState()) {
            return;
        }
        super.determineScrollingStart(motionEvent, f);
    }

    void disableLayoutTransitions() {
        setLayoutTransition(null);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> sparseArray) {
        this.mSavedStates = sparseArray;
    }

    @Override // com.android.launcher3.PagedView, android.view.ViewGroup, android.view.View
    public boolean dispatchUnhandledMove(View view, int i) {
        if (workspaceInModalState() || !isFinishedSwitchingState()) {
            return false;
        }
        return super.dispatchUnhandledMove(view, i);
    }

    @Override // com.android.launcher3.accessibility.LauncherAccessibilityDelegate.AccessibilityDragSource
    @TargetApi(21)
    public void enableAccessibleDrag(boolean z) {
        for (int i = 0; i < getChildCount(); i++) {
            ((CellLayout) getChildAt(i)).enableAccessibleDrag(z, 2);
        }
        if (z) {
            setOnClickListener(null);
        } else {
            setOnClickListener(this.mLauncher);
        }
        this.mLauncher.getSearchDropTargetBar().enableAccessibleDrag(z);
        this.mLauncher.getHotseat().getLayout().enableAccessibleDrag(z, 2);
    }

    void enableChildrenCache(int i, int i2) {
        int i3 = i;
        int i4 = i2;
        if (i > i2) {
            i4 = i;
            i3 = i2;
        }
        int childCount = getChildCount();
        int min = Math.min(i4, childCount - 1);
        for (int max = Math.max(i3, 0); max <= min; max++) {
            CellLayout cellLayout = (CellLayout) getChildAt(max);
            cellLayout.setChildrenDrawnWithCacheEnabled(true);
            cellLayout.setChildrenDrawingCacheEnabled(true);
        }
    }

    void enableLayoutTransitions() {
        setLayoutTransition(this.mLayoutTransition);
    }

    public Rect estimateItemPosition(CellLayout cellLayout, int i, int i2, int i3, int i4) {
        Rect rect = new Rect();
        cellLayout.cellToRect(i, i2, i3, i4, rect);
        return rect;
    }

    public int[] estimateItemSize(ItemInfo itemInfo, boolean z) {
        int[] iArr = new int[2];
        if (getChildCount() <= 0) {
            iArr[0] = Integer.MAX_VALUE;
            iArr[1] = Integer.MAX_VALUE;
            return iArr;
        }
        Rect estimateItemPosition = estimateItemPosition((CellLayout) getChildAt(numCustomPages()), 0, 0, itemInfo.spanX, itemInfo.spanY);
        iArr[0] = estimateItemPosition.width();
        iArr[1] = estimateItemPosition.height();
        if (z) {
            iArr[0] = (int) (iArr[0] * this.mSpringLoadedShrinkFactor);
            iArr[1] = (int) (iArr[1] * this.mSpringLoadedShrinkFactor);
        }
        return iArr;
    }

    public void exitWidgetResizeMode() {
        this.mLauncher.getDragLayer().clearAllResizeFrames();
    }

    @Override // com.android.launcher3.Stats.LaunchSourceProvider
    public void fillInLaunchSourceData(View view, Bundle bundle) {
        bundle.putString("container", "homescreen");
        bundle.putInt("container_page", getCurrentPage());
    }

    int[] findNearestArea(int i, int i2, int i3, int i4, CellLayout cellLayout, int[] iArr) {
        return cellLayout.findNearestArea(i, i2, i3, i4, iArr);
    }

    ArrayList<ShortcutAndWidgetContainer> getAllShortcutAndWidgetContainers() {
        ArrayList<ShortcutAndWidgetContainer> arrayList = new ArrayList<>();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            arrayList.add(((CellLayout) getChildAt(i)).getShortcutsAndWidgets());
        }
        if (this.mLauncher.getHotseat() != null) {
            arrayList.add(this.mLauncher.getHotseat().getLayout().getShortcutsAndWidgets());
        }
        return arrayList;
    }

    public CellLayout getCurrentDropLayout() {
        return (CellLayout) getChildAt(getNextPage());
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.launcher3.PagedView
    public String getCurrentPageDescription() {
        if (hasCustomContent() && getNextPage() == 0) {
            return this.mCustomContentDescription;
        }
        return getPageDescription(this.mNextPage != -1 ? this.mNextPage : this.mCurrentPage);
    }

    public int getCurrentPageOffsetFromCustomContent() {
        return getNextPage() - numCustomPages();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public Launcher.CustomContentCallbacks getCustomContentCallbacks() {
        return this.mCustomContentCallbacks;
    }

    @Override // android.view.ViewGroup
    public int getDescendantFocusability() {
        if (workspaceInModalState()) {
            return 393216;
        }
        return super.getDescendantFocusability();
    }

    @Override // com.android.launcher3.PagedView
    protected void getEdgeVerticalPostion(int[] iArr) {
        View childAt = getChildAt(getPageCount() - 1);
        iArr[0] = childAt.getTop();
        iArr[1] = childAt.getBottom();
    }

    public Folder getFolderForTag(Object obj) {
        return (Folder) getFirstMatch(new ItemOperator(this, obj) { // from class: com.android.launcher3.Workspace.14
            final Workspace this$0;
            final Object val$tag;

            {
                this.this$0 = this;
                this.val$tag = obj;
            }

            @Override // com.android.launcher3.Workspace.ItemOperator
            public boolean evaluate(ItemInfo itemInfo, View view, View view2) {
                return ((view instanceof Folder) && ((Folder) view).getInfo() == this.val$tag) ? ((Folder) view).getInfo().opened : false;
            }
        });
    }

    @Override // com.android.launcher3.PagedView
    protected void getFreeScrollPageRange(int[] iArr) {
        getOverviewModePages(iArr);
    }

    @Override // com.android.launcher3.DropTarget
    public void getHitRectRelativeToDragLayer(Rect rect) {
        this.mLauncher.getDragLayer().getDescendantRectRelativeToSelf(this, rect);
    }

    public View getHomescreenIconByItemId(long j) {
        return getFirstMatch(new ItemOperator(this, j) { // from class: com.android.launcher3.Workspace.15
            final Workspace this$0;
            final long val$id;

            {
                this.this$0 = this;
                this.val$id = j;
            }

            @Override // com.android.launcher3.Workspace.ItemOperator
            public boolean evaluate(ItemInfo itemInfo, View view, View view2) {
                boolean z = false;
                if (itemInfo != null) {
                    z = false;
                    if (itemInfo.id == this.val$id) {
                        z = true;
                    }
                }
                return z;
            }
        });
    }

    public long getIdForScreen(CellLayout cellLayout) {
        int indexOfValue = this.mWorkspaceScreens.indexOfValue(cellLayout);
        if (indexOfValue != -1) {
            return this.mWorkspaceScreens.keyAt(indexOfValue);
        }
        return -1L;
    }

    @Override // com.android.launcher3.DragSource
    public float getIntrinsicIconScaleFactor() {
        return 1.0f;
    }

    public Folder getOpenFolder() {
        DragLayer dragLayer = this.mLauncher.getDragLayer();
        int childCount = dragLayer.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = dragLayer.getChildAt(i);
            if (childAt instanceof Folder) {
                Folder folder = (Folder) childAt;
                if (folder.getInfo().opened) {
                    return folder;
                }
            }
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getOverviewModeTranslationY() {
        DeviceProfile deviceProfile = this.mLauncher.getDeviceProfile();
        Rect workspacePadding = deviceProfile.getWorkspacePadding(Utilities.isRtl(getResources()));
        int overviewModeButtonBarHeight = deviceProfile.getOverviewModeButtonBarHeight();
        int normalChildHeight = (int) (this.mOverviewModeShrinkFactor * getNormalChildHeight());
        int i = this.mInsets.top + workspacePadding.top;
        int viewportHeight = getViewportHeight();
        int i2 = this.mInsets.bottom;
        int i3 = workspacePadding.bottom;
        int i4 = this.mInsets.top;
        return (-(i + (((((viewportHeight - i2) - i3) - i) - normalChildHeight) / 2))) + i4 + (((((getViewportHeight() - this.mInsets.bottom) - overviewModeButtonBarHeight) - i4) - normalChildHeight) / 2);
    }

    public void getPageAreaRelativeToDragLayer(Rect rect) {
        CellLayout cellLayout = (CellLayout) getChildAt(getNextPage());
        if (cellLayout == null) {
            return;
        }
        ShortcutAndWidgetContainer shortcutsAndWidgets = cellLayout.getShortcutsAndWidgets();
        this.mTempXY[0] = getViewportOffsetX() + getPaddingLeft() + shortcutsAndWidgets.getLeft();
        this.mTempXY[1] = cellLayout.getTop() + shortcutsAndWidgets.getTop();
        float descendantCoordRelativeToSelf = this.mLauncher.getDragLayer().getDescendantCoordRelativeToSelf(this, this.mTempXY);
        rect.set(this.mTempXY[0], this.mTempXY[1], (int) (this.mTempXY[0] + (shortcutsAndWidgets.getMeasuredWidth() * descendantCoordRelativeToSelf)), (int) (this.mTempXY[1] + (shortcutsAndWidgets.getMeasuredHeight() * descendantCoordRelativeToSelf)));
    }

    public int getPageIndexForScreenId(long j) {
        return indexOfChild(this.mWorkspaceScreens.get(j));
    }

    @Override // com.android.launcher3.PagedView
    protected View.OnClickListener getPageIndicatorClickListener() {
        if (((AccessibilityManager) getContext().getSystemService("accessibility")).isTouchExplorationEnabled()) {
            return new View.OnClickListener(this) { // from class: com.android.launcher3.Workspace.6
                final Workspace this$0;

                {
                    this.this$0 = this;
                }

                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    this.this$0.mLauncher.showOverviewMode(true);
                }
            };
        }
        return null;
    }

    @Override // com.android.launcher3.PagedView
    protected String getPageIndicatorDescription() {
        return getCurrentPageDescription() + ", " + getResources().getString(2131558455);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.launcher3.PagedView
    public PageIndicator.PageMarkerResources getPageIndicatorMarker(int i) {
        return (getScreenIdForPageIndex(i) != -201 || this.mScreenOrder.size() - numCustomPages() <= 1) ? super.getPageIndicatorMarker(i) : new PageIndicator.PageMarkerResources(2130837526, 2130837525);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.launcher3.PagedView
    public Matrix getPageShiftMatrix() {
        if (Float.compare(this.mOverlayTranslation, 0.0f) != 0) {
            this.mTempMatrix.set(getMatrix());
            this.mTempMatrix.postTranslate(-this.mOverlayTranslation, 0.0f);
            return this.mTempMatrix;
        }
        return super.getPageShiftMatrix();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public CellLayout getParentCellLayoutForView(View view) {
        for (CellLayout cellLayout : getWorkspaceAndHotseatCellLayouts()) {
            if (cellLayout.getShortcutsAndWidgets().indexOfChild(view) > -1) {
                return cellLayout;
            }
        }
        return null;
    }

    public long getScreenIdForPageIndex(int i) {
        if (i < 0 || i >= this.mScreenOrder.size()) {
            return -1L;
        }
        return this.mScreenOrder.get(i).longValue();
    }

    public ArrayList<Long> getScreenOrder() {
        return this.mScreenOrder;
    }

    public CellLayout getScreenWithId(long j) {
        return this.mWorkspaceScreens.get(j);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public State getState() {
        return this.mState;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.launcher3.PagedView
    public int getUnboundedScrollX() {
        return isScrollingOverlay() ? this.mUnboundedScrollX : super.getUnboundedScrollX();
    }

    public View getViewForTag(Object obj) {
        return getFirstMatch(new ItemOperator(this, obj) { // from class: com.android.launcher3.Workspace.16
            final Workspace this$0;
            final Object val$tag;

            {
                this.this$0 = this;
                this.val$tag = obj;
            }

            @Override // com.android.launcher3.Workspace.ItemOperator
            public boolean evaluate(ItemInfo itemInfo, View view, View view2) {
                return itemInfo == this.val$tag;
            }
        });
    }

    public LauncherAppWidgetHostView getWidgetForAppWidgetId(int i) {
        return (LauncherAppWidgetHostView) getFirstMatch(new ItemOperator(this, i) { // from class: com.android.launcher3.Workspace.17
            final Workspace this$0;
            final int val$appWidgetId;

            {
                this.this$0 = this;
                this.val$appWidgetId = i;
            }

            @Override // com.android.launcher3.Workspace.ItemOperator
            public boolean evaluate(ItemInfo itemInfo, View view, View view2) {
                boolean z = false;
                if (itemInfo instanceof LauncherAppWidgetInfo) {
                    z = false;
                    if (((LauncherAppWidgetInfo) itemInfo).appWidgetId == this.val$appWidgetId) {
                        z = true;
                    }
                }
                return z;
            }
        });
    }

    ArrayList<CellLayout> getWorkspaceAndHotseatCellLayouts() {
        ArrayList<CellLayout> arrayList = new ArrayList<>();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            arrayList.add((CellLayout) getChildAt(i));
        }
        if (this.mLauncher.getHotseat() != null) {
            arrayList.add(this.mLauncher.getHotseat().getLayout());
        }
        return arrayList;
    }

    public boolean hasCustomContent() {
        return this.mScreenOrder.size() > 0 && this.mScreenOrder.get(0).longValue() == -301;
    }

    public boolean hasExtraEmptyScreen() {
        boolean z = true;
        int childCount = getChildCount();
        int numCustomPages = numCustomPages();
        if (!this.mWorkspaceScreens.containsKey(-201L) || childCount - numCustomPages <= 1) {
            z = false;
        }
        return z;
    }

    void hideCustomContentIfNecessary() {
        if ((this.mState != State.NORMAL) && hasCustomContent()) {
            disableLayoutTransitions();
            this.mWorkspaceScreens.get(-301L).setVisibility(4);
            enableLayoutTransitions();
        }
    }

    protected void initWorkspace() {
        this.mCurrentPage = this.mDefaultPage;
        LauncherAppState launcherAppState = LauncherAppState.getInstance();
        DeviceProfile deviceProfile = this.mLauncher.getDeviceProfile();
        this.mIconCache = launcherAppState.getIconCache();
        setWillNotDraw(false);
        setClipChildren(false);
        setClipToPadding(false);
        setChildrenDrawnWithCacheEnabled(true);
        setMinScale(this.mOverviewModeShrinkFactor);
        setupLayoutTransition();
        this.mWallpaperOffset = new WallpaperOffsetInterpolator(this);
        this.mLauncher.getWindowManager().getDefaultDisplay().getSize(this.mDisplaySize);
        this.mMaxDistanceForFolderCreation = deviceProfile.iconSizePx * 0.55f;
        setWallpaperDimension();
        setEdgeGlowColor(getResources().getColor(2131361798));
    }

    public long insertNewWorkspaceScreen(long j) {
        return insertNewWorkspaceScreen(j, getChildCount());
    }

    public long insertNewWorkspaceScreen(long j, int i) {
        if (this.mWorkspaceScreens.containsKey(j)) {
            throw new RuntimeException("Screen id " + j + " already exists!");
        }
        CellLayout cellLayout = (CellLayout) this.mLauncher.getLayoutInflater().inflate(2130968612, (ViewGroup) this, false);
        cellLayout.setOnLongClickListener(this.mLongClickListener);
        cellLayout.setOnClickListener(this.mLauncher);
        cellLayout.setSoundEffectsEnabled(false);
        this.mWorkspaceScreens.put(j, cellLayout);
        this.mScreenOrder.add(i, Long.valueOf(j));
        addView(cellLayout, i);
        LauncherAccessibilityDelegate accessibilityDelegate = LauncherAppState.getInstance().getAccessibilityDelegate();
        if (accessibilityDelegate != null && accessibilityDelegate.isInAccessibleDrag()) {
            cellLayout.enableAccessibleDrag(true, 2);
        }
        return j;
    }

    public long insertNewWorkspaceScreenBeforeEmptyScreen(long j) {
        int indexOf = this.mScreenOrder.indexOf(-201L);
        int i = indexOf;
        if (indexOf < 0) {
            i = this.mScreenOrder.size();
        }
        return insertNewWorkspaceScreen(j, i);
    }

    @Override // com.android.launcher3.DropTarget
    public boolean isDropEnabled() {
        return true;
    }

    public boolean isFinishedSwitchingState() {
        boolean z = true;
        if (this.mIsSwitchingState) {
            z = this.mTransitionProgress > 0.5f;
        }
        return z;
    }

    public boolean isInOverviewMode() {
        return this.mState == State.OVERVIEW;
    }

    public boolean isOnOrMovingToCustomContent() {
        boolean z = false;
        if (hasCustomContent()) {
            z = false;
            if (getNextPage() == 0) {
                z = false;
                if (this.mRestorePage == -1001) {
                    z = true;
                }
            }
        }
        return z;
    }

    boolean isPointInSelfOverHotseat(int i, int i2, Rect rect) {
        if (rect == null) {
            new Rect();
        }
        this.mTempPt[0] = i;
        this.mTempPt[1] = i2;
        this.mLauncher.getDragLayer().getDescendantCoordRelativeToSelf(this, this.mTempPt, true);
        return this.mLauncher.getDeviceProfile().getHotseatRect().contains(this.mTempPt[0], this.mTempPt[1]);
    }

    public boolean isSwitchingState() {
        return this.mIsSwitchingState;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isTouchActive() {
        boolean z = false;
        if (this.mTouchState != 0) {
            z = true;
        }
        return z;
    }

    void mapOverItems(boolean z, ItemOperator itemOperator) {
        ArrayList<ShortcutAndWidgetContainer> allShortcutAndWidgetContainers = getAllShortcutAndWidgetContainers();
        int size = allShortcutAndWidgetContainers.size();
        for (int i = 0; i < size; i++) {
            ShortcutAndWidgetContainer shortcutAndWidgetContainer = allShortcutAndWidgetContainers.get(i);
            int childCount = shortcutAndWidgetContainer.getChildCount();
            for (int i2 = 0; i2 < childCount; i2++) {
                View childAt = shortcutAndWidgetContainer.getChildAt(i2);
                ItemInfo itemInfo = (ItemInfo) childAt.getTag();
                if (z && (itemInfo instanceof FolderInfo) && (childAt instanceof FolderIcon)) {
                    FolderIcon folderIcon = (FolderIcon) childAt;
                    ArrayList<View> itemsInReadingOrder = folderIcon.getFolder().getItemsInReadingOrder();
                    int size2 = itemsInReadingOrder.size();
                    for (int i3 = 0; i3 < size2; i3++) {
                        View view = itemsInReadingOrder.get(i3);
                        if (itemOperator.evaluate((ItemInfo) view.getTag(), view, folderIcon)) {
                            return;
                        }
                    }
                    continue;
                } else if (itemOperator.evaluate(itemInfo, childAt, null)) {
                    return;
                }
            }
        }
    }

    void mapPointFromChildToSelf(View view, float[] fArr) {
        fArr[0] = fArr[0] + view.getLeft();
        fArr[1] = fArr[1] + view.getTop();
    }

    void mapPointFromSelfToChild(View view, float[] fArr, Matrix matrix) {
        fArr[0] = fArr[0] - view.getLeft();
        fArr[1] = fArr[1] - view.getTop();
    }

    void mapPointFromSelfToHotseatLayout(Hotseat hotseat, float[] fArr) {
        this.mTempPt[0] = (int) fArr[0];
        this.mTempPt[1] = (int) fArr[1];
        this.mLauncher.getDragLayer().getDescendantCoordRelativeToSelf(this, this.mTempPt, true);
        this.mLauncher.getDragLayer().mapCoordInSelfToDescendent(hotseat.getLayout(), this.mTempPt);
        fArr[0] = this.mTempPt[0];
        fArr[1] = this.mTempPt[1];
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void moveToDefaultScreen(boolean z) {
        moveToScreen(this.mDefaultPage, z);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.launcher3.PagedView
    public void notifyPageSwitchListener() {
        super.notifyPageSwitchListener();
        if (hasCustomContent() && getNextPage() == 0 && !this.mCustomContentShowing) {
            this.mCustomContentShowing = true;
            if (this.mCustomContentCallbacks != null) {
                this.mCustomContentCallbacks.onShow(false);
                this.mCustomContentShowTime = System.currentTimeMillis();
            }
        } else if (hasCustomContent() && getNextPage() != 0 && this.mCustomContentShowing) {
            this.mCustomContentShowing = false;
            if (this.mCustomContentCallbacks != null) {
                this.mCustomContentCallbacks.onHide();
            }
        }
    }

    public int numCustomPages() {
        return hasCustomContent() ? 1 : 0;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.launcher3.PagedView, android.view.ViewGroup, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mWindowToken = getWindowToken();
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher.Workspace", "onAttachedToWindow: mWindowToken = " + this.mWindowToken);
        }
        computeScroll();
        if (this.mDragController != null) {
            this.mDragController.setWindowToken(this.mWindowToken);
        }
    }

    @Override // com.android.launcher3.PagedView, android.view.ViewGroup.OnHierarchyChangeListener
    public void onChildViewAdded(View view, View view2) {
        if (!(view2 instanceof CellLayout)) {
            throw new IllegalArgumentException("A Workspace can only have CellLayout children.");
        }
        CellLayout cellLayout = (CellLayout) view2;
        cellLayout.setOnInterceptTouchListener(this);
        cellLayout.setClickable(true);
        cellLayout.setImportantForAccessibility(2);
        super.onChildViewAdded(view, view2);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.launcher3.PagedView, android.view.ViewGroup, android.view.View
    public void onDetachedFromWindow() {
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher.Workspace", "onDetachedFromWindow: mWindowToken = " + this.mWindowToken);
        }
        super.onDetachedFromWindow();
        this.mWindowToken = null;
    }

    @Override // com.android.launcher3.DragController.DragListener
    public void onDragEnd() {
        if (LauncherLog.DEBUG_DRAG) {
            LauncherLog.d("Launcher.Workspace", "onDragEnd: mIsDragOccuring = " + this.mIsDragOccuring);
        }
        if (ENFORCE_DRAG_EVENT_ORDER) {
            enfoceDragParity("onDragEnd", 0, 0);
        }
        if (!this.mDeferRemoveExtraEmptyScreen) {
            removeExtraEmptyScreen(true, this.mDragSourceInternal != null);
        }
        this.mIsDragOccuring = false;
        updateChildrenLayersEnabled(false);
        this.mLauncher.unlockScreenOrientation(false);
        InstallShortcutReceiver.disableAndFlushInstallQueue(getContext());
        this.mDragSourceInternal = null;
        this.mLauncher.onInteractionEnd();
    }

    @Override // com.android.launcher3.DropTarget
    public void onDragEnter(DropTarget.DragObject dragObject) {
        if (LauncherLog.DEBUG_DRAG) {
            LauncherLog.d("Launcher.Workspace", "onDragEnter: d = " + dragObject + ", mDragTargetLayout = " + this.mDragTargetLayout);
        }
        if (ENFORCE_DRAG_EVENT_ORDER) {
            enfoceDragParity("onDragEnter", 1, 1);
        }
        this.mCreateUserFolderOnDrop = false;
        this.mAddToExistingFolderOnDrop = false;
        this.mDropToLayout = null;
        CellLayout currentDropLayout = getCurrentDropLayout();
        setCurrentDropLayout(currentDropLayout);
        setCurrentDragOverlappingLayout(currentDropLayout);
        if (workspaceInModalState()) {
            return;
        }
        this.mLauncher.getDragLayer().showPageHints();
    }

    @Override // com.android.launcher3.DropTarget
    public void onDragExit(DropTarget.DragObject dragObject) {
        if (LauncherLog.DEBUG_DRAG) {
            LauncherLog.d("Launcher.Workspace", "onDragExit: d = " + dragObject);
        }
        if (ENFORCE_DRAG_EVENT_ORDER) {
            enfoceDragParity("onDragExit", -1, 0);
        }
        if (!this.mInScrollArea) {
            this.mDropToLayout = this.mDragTargetLayout;
        } else if (isPageMoving()) {
            this.mDropToLayout = (CellLayout) getPageAt(getNextPage());
        } else {
            this.mDropToLayout = this.mDragOverlappingLayout;
        }
        if (this.mDragMode == 1) {
            this.mCreateUserFolderOnDrop = true;
        } else if (this.mDragMode == 2) {
            this.mAddToExistingFolderOnDrop = true;
        }
        onResetScrollArea();
        if (LauncherLog.DEBUG_DRAG) {
            LauncherLog.d("Launcher.Workspace", "doDragExit: drag source = " + (dragObject != null ? dragObject.dragSource : null) + ", drag info = " + (dragObject != null ? dragObject.dragInfo : null) + ", mDragTargetLayout = " + this.mDragTargetLayout + ", mIsPageMoving = " + this.mIsPageMoving);
        }
        setCurrentDropLayout(null);
        setCurrentDragOverlappingLayout(null);
        this.mSpringLoadedDragController.cancel();
        this.mLauncher.getDragLayer().hidePageHints();
    }

    @Override // com.android.launcher3.DropTarget
    public void onDragOver(DropTarget.DragObject dragObject) {
        if (LauncherLog.DEBUG_DRAG) {
            LauncherLog.d("Launcher.Workspace", "onDragOver: d = " + dragObject + ", dragInfo = " + dragObject.dragInfo + ", mInScrollArea = " + this.mInScrollArea + ", mIsSwitchingState = " + this.mIsSwitchingState);
        }
        if (this.mInScrollArea || !transitionStateShouldAllowDrop()) {
            return;
        }
        Rect rect = new Rect();
        ItemInfo itemInfo = (ItemInfo) dragObject.dragInfo;
        if (itemInfo == null) {
            if (LauncherAppState.isDogfoodBuild()) {
                throw new NullPointerException("DragObject has null info");
            }
        } else if (itemInfo.spanX < 0 || itemInfo.spanY < 0) {
            throw new RuntimeException("Improper spans found");
        } else {
            this.mDragViewVisualCenter = dragObject.getVisualCenter(this.mDragViewVisualCenter);
            View view = this.mDragInfo == null ? null : this.mDragInfo.cell;
            if (workspaceInModalState()) {
                CellLayout cellLayout = null;
                if (this.mLauncher.getHotseat() != null) {
                    if (isExternalDragWidget(dragObject)) {
                        cellLayout = null;
                    } else {
                        cellLayout = null;
                        if (isPointInSelfOverHotseat(dragObject.x, dragObject.y, rect)) {
                            cellLayout = this.mLauncher.getHotseat().getLayout();
                        }
                    }
                }
                CellLayout cellLayout2 = cellLayout;
                if (cellLayout == null) {
                    cellLayout2 = findMatchingPageForDragOver(dragObject.dragView, dragObject.x, dragObject.y, false);
                }
                if (cellLayout2 != this.mDragTargetLayout) {
                    setCurrentDropLayout(cellLayout2);
                    setCurrentDragOverlappingLayout(cellLayout2);
                    if (this.mState == State.SPRING_LOADED) {
                        if (this.mLauncher.isHotseatLayout(cellLayout2)) {
                            this.mSpringLoadedDragController.cancel();
                        } else {
                            this.mSpringLoadedDragController.setAlarm(this.mDragTargetLayout);
                        }
                    }
                }
            } else {
                CellLayout cellLayout3 = null;
                if (this.mLauncher.getHotseat() != null) {
                    if (isDragWidget(dragObject)) {
                        cellLayout3 = null;
                    } else {
                        cellLayout3 = null;
                        if (isPointInSelfOverHotseat(dragObject.x, dragObject.y, rect)) {
                            cellLayout3 = this.mLauncher.getHotseat().getLayout();
                        }
                    }
                }
                CellLayout cellLayout4 = cellLayout3;
                if (cellLayout3 == null) {
                    cellLayout4 = getCurrentDropLayout();
                }
                if (cellLayout4 != this.mDragTargetLayout) {
                    setCurrentDropLayout(cellLayout4);
                    setCurrentDragOverlappingLayout(cellLayout4);
                }
            }
            if (this.mDragTargetLayout != null) {
                if (this.mLauncher.isHotseatLayout(this.mDragTargetLayout)) {
                    mapPointFromSelfToHotseatLayout(this.mLauncher.getHotseat(), this.mDragViewVisualCenter);
                } else {
                    mapPointFromSelfToChild(this.mDragTargetLayout, this.mDragViewVisualCenter, null);
                }
                int i = itemInfo.spanX;
                int i2 = itemInfo.spanY;
                int i3 = i;
                int i4 = i2;
                if (itemInfo.minSpanX > 0) {
                    i3 = i;
                    i4 = i2;
                    if (itemInfo.minSpanY > 0) {
                        i3 = itemInfo.minSpanX;
                        i4 = itemInfo.minSpanY;
                    }
                }
                this.mTargetCell = findNearestArea((int) this.mDragViewVisualCenter[0], (int) this.mDragViewVisualCenter[1], i3, i4, this.mDragTargetLayout, this.mTargetCell);
                int i5 = this.mTargetCell[0];
                int i6 = this.mTargetCell[1];
                setCurrentDropOverCell(this.mTargetCell[0], this.mTargetCell[1]);
                manageFolderFeedback(this.mDragTargetLayout, this.mTargetCell, this.mDragTargetLayout.getDistanceFromCell(this.mDragViewVisualCenter[0], this.mDragViewVisualCenter[1], this.mTargetCell), dragObject);
                boolean isNearestDropLocationOccupied = this.mDragTargetLayout.isNearestDropLocationOccupied((int) this.mDragViewVisualCenter[0], (int) this.mDragViewVisualCenter[1], itemInfo.spanX, itemInfo.spanY, view, this.mTargetCell);
                if (!isNearestDropLocationOccupied) {
                    this.mDragTargetLayout.visualizeDropLocation(view, this.mDragOutline, this.mTargetCell[0], this.mTargetCell[1], itemInfo.spanX, itemInfo.spanY, false, dragObject);
                } else if ((this.mDragMode == 0 || this.mDragMode == 3) && !this.mReorderAlarm.alarmPending() && (this.mLastReorderX != i5 || this.mLastReorderY != i6)) {
                    this.mDragTargetLayout.performReorder((int) this.mDragViewVisualCenter[0], (int) this.mDragViewVisualCenter[1], i3, i4, itemInfo.spanX, itemInfo.spanY, view, this.mTargetCell, new int[2], 0);
                    this.mReorderAlarm.setOnAlarmListener(new ReorderAlarmListener(this, this.mDragViewVisualCenter, i3, i4, itemInfo.spanX, itemInfo.spanY, dragObject, view));
                    this.mReorderAlarm.setAlarm(350L);
                }
                if ((this.mDragMode == 1 || this.mDragMode == 2 || !isNearestDropLocationOccupied) && this.mDragTargetLayout != null) {
                    this.mDragTargetLayout.revertTempState();
                }
            }
        }
    }

    @Override // com.android.launcher3.DragController.DragListener
    public void onDragStart(DragSource dragSource, Object obj, int i) {
        if (LauncherLog.DEBUG_DRAG) {
            LauncherLog.d("Launcher.Workspace", "onDragStart: source = " + dragSource + ", info = " + obj + ", dragAction = " + i);
        }
        if (ENFORCE_DRAG_EVENT_ORDER) {
            enfoceDragParity("onDragStart", 0, 0);
        }
        this.mIsDragOccuring = true;
        updateChildrenLayersEnabled(false);
        this.mLauncher.lockScreenOrientation();
        this.mLauncher.onInteractionBegin();
        InstallShortcutReceiver.enableInstallQueue();
        if (this.mAddNewPageOnDrag) {
            this.mDeferRemoveExtraEmptyScreen = false;
            addExtraEmptyScreenOnDrag();
        }
    }

    public void onDragStartedWithItem(PendingAddItemInfo pendingAddItemInfo, Bitmap bitmap, boolean z) {
        int[] estimateItemSize = estimateItemSize(pendingAddItemInfo, false);
        this.mDragOutline = createDragOutline(bitmap, 2, estimateItemSize[0], estimateItemSize[1], z);
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        post(this.mBindPages);
    }

    /* JADX WARN: Code restructure failed: missing block: B:81:0x03d0, code lost:
        if (r0[1] != r0.spanY) goto L73;
     */
    @Override // com.android.launcher3.DropTarget
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void onDrop(DropTarget.DragObject dragObject) {
        this.mDragViewVisualCenter = dragObject.getVisualCenter(this.mDragViewVisualCenter);
        CellLayout cellLayout = this.mDropToLayout;
        if (cellLayout != null) {
            if (this.mLauncher.isHotseatLayout(cellLayout)) {
                mapPointFromSelfToHotseatLayout(this.mLauncher.getHotseat(), this.mDragViewVisualCenter);
            } else {
                mapPointFromSelfToChild(cellLayout, this.mDragViewVisualCenter, null);
            }
        }
        if (LauncherLog.DEBUG_DRAG) {
            LauncherLog.d("Launcher.Workspace", "onDrop 1: drag view = " + dragObject.dragView + ", dragInfo = " + dragObject.dragInfo + ", dragSource  = " + dragObject.dragSource + ", dropTargetLayout = " + cellLayout + ", mDragInfo = " + this.mDragInfo + ", mInScrollArea = " + this.mInScrollArea + ", this = " + this);
        }
        if (dragObject.dragSource != this) {
            onDropExternal(new int[]{(int) this.mDragViewVisualCenter[0], (int) this.mDragViewVisualCenter[1]}, dragObject.dragInfo, cellLayout, false, dragObject);
        } else if (this.mDragInfo != null) {
            View view = this.mDragInfo.cell;
            boolean z = false;
            Runnable runnable = null;
            int i = -1;
            if (cellLayout != null) {
                if (dragObject.cancelled) {
                    i = -1;
                    runnable = null;
                    z = false;
                } else {
                    boolean z2 = getParentCellLayoutForView(view) != cellLayout;
                    boolean isHotseatLayout = this.mLauncher.isHotseatLayout(cellLayout);
                    long j = isHotseatLayout ? -101 : -100;
                    long idForScreen = this.mTargetCell[0] < 0 ? this.mDragInfo.screenId : getIdForScreen(cellLayout);
                    int i2 = this.mDragInfo != null ? this.mDragInfo.spanX : 1;
                    int i3 = this.mDragInfo != null ? this.mDragInfo.spanY : 1;
                    this.mTargetCell = findNearestArea((int) this.mDragViewVisualCenter[0], (int) this.mDragViewVisualCenter[1], i2, i3, cellLayout, this.mTargetCell);
                    float distanceFromCell = cellLayout.getDistanceFromCell(this.mDragViewVisualCenter[0], this.mDragViewVisualCenter[1], this.mTargetCell);
                    if (LauncherLog.DEBUG_DRAG) {
                        LauncherLog.d("Launcher.Workspace", "onDrop 2: cell = " + view + ", screenId = " + idForScreen + ", mInScrollArea = " + this.mInScrollArea + ", mTargetCell = " + this.mTargetCell + ", this = " + this);
                    }
                    if ((!this.mInScrollArea && createUserFolderIfNecessary(view, j, cellLayout, this.mTargetCell, distanceFromCell, false, dragObject.dragView, null)) || addToExistingFolderIfNecessary(view, cellLayout, this.mTargetCell, distanceFromCell, dragObject, false)) {
                        return;
                    }
                    ItemInfo itemInfo = (ItemInfo) dragObject.dragInfo;
                    int i4 = itemInfo.spanX;
                    int i5 = itemInfo.spanY;
                    int i6 = i4;
                    int i7 = i5;
                    if (itemInfo.minSpanX > 0) {
                        i6 = i4;
                        i7 = i5;
                        if (itemInfo.minSpanY > 0) {
                            i6 = itemInfo.minSpanX;
                            i7 = itemInfo.minSpanY;
                        }
                    }
                    int[] iArr = new int[2];
                    this.mTargetCell = cellLayout.performReorder((int) this.mDragViewVisualCenter[0], (int) this.mDragViewVisualCenter[1], i6, i7, i2, i3, view, this.mTargetCell, iArr, 2);
                    boolean z3 = (this.mTargetCell[0] < 0 || this.mTargetCell[1] < 0 || iArr[0] <= 0) ? false : iArr[1] > 0;
                    if (LauncherLog.DEBUG) {
                        LauncherLog.d("Launcher.Workspace", "onDrop 3: foundCell = " + z3 + "mTargetCell = (" + this.mTargetCell[0] + ", " + this.mTargetCell[1] + "), resultSpan = (" + iArr[0] + "," + iArr[1] + "), item.span = (" + itemInfo.spanX + ", " + itemInfo.spanY + ") ,item.minSpan = (" + itemInfo.minSpanX + ", " + itemInfo.minSpanY + "),minSpan = (" + i6 + "," + i7 + ").");
                    }
                    boolean z4 = false;
                    if (z3) {
                        z4 = false;
                        if (view instanceof AppWidgetHostView) {
                            if (iArr[0] == itemInfo.spanX) {
                                z4 = false;
                            }
                            z4 = true;
                            itemInfo.spanX = iArr[0];
                            itemInfo.spanY = iArr[1];
                            AppWidgetResizeFrame.updateWidgetSizeRanges((AppWidgetHostView) view, this.mLauncher, iArr[0], iArr[1]);
                        }
                    }
                    i = -1;
                    if (getScreenIdForPageIndex(this.mCurrentPage) != idForScreen) {
                        if (isHotseatLayout) {
                            i = -1;
                        } else {
                            i = getPageIndexForScreenId(idForScreen);
                            snapToPage(i);
                        }
                    }
                    if (z3) {
                        ItemInfo itemInfo2 = (ItemInfo) view.getTag();
                        if (z2) {
                            CellLayout parentCellLayoutForView = getParentCellLayoutForView(view);
                            if (parentCellLayoutForView != null) {
                                parentCellLayoutForView.removeView(view);
                            } else if (LauncherAppState.isDogfoodBuild()) {
                                throw new NullPointerException("mDragInfo.cell has null parent");
                            }
                            addInScreen(view, j, idForScreen, this.mTargetCell[0], this.mTargetCell[1], itemInfo2.spanX, itemInfo2.spanY);
                        }
                        CellLayout.LayoutParams layoutParams = (CellLayout.LayoutParams) view.getLayoutParams();
                        int i8 = this.mTargetCell[0];
                        layoutParams.tmpCellX = i8;
                        layoutParams.cellX = i8;
                        int i9 = this.mTargetCell[1];
                        layoutParams.tmpCellY = i9;
                        layoutParams.cellY = i9;
                        layoutParams.cellHSpan = itemInfo.spanX;
                        layoutParams.cellVSpan = itemInfo.spanY;
                        layoutParams.isLockedToGrid = true;
                        runnable = null;
                        if (j != -101) {
                            runnable = null;
                            if (view instanceof LauncherAppWidgetHostView) {
                                LauncherAppWidgetHostView launcherAppWidgetHostView = (LauncherAppWidgetHostView) view;
                                AppWidgetProviderInfo appWidgetInfo = launcherAppWidgetHostView.getAppWidgetInfo();
                                runnable = null;
                                if (appWidgetInfo != null) {
                                    runnable = null;
                                    if (appWidgetInfo.resizeMode != 0) {
                                        runnable = dragObject.accessibleDrag ? null : new Runnable(this, new Runnable(this, itemInfo2, launcherAppWidgetHostView, cellLayout) { // from class: com.android.launcher3.Workspace.7
                                            final Workspace this$0;
                                            final CellLayout val$cellLayout;
                                            final LauncherAppWidgetHostView val$hostView;
                                            final ItemInfo val$info;

                                            {
                                                this.this$0 = this;
                                                this.val$info = itemInfo2;
                                                this.val$hostView = launcherAppWidgetHostView;
                                                this.val$cellLayout = cellLayout;
                                            }

                                            @Override // java.lang.Runnable
                                            public void run() {
                                                this.this$0.mLauncher.getDragLayer().addResizeFrame(this.val$info, this.val$hostView, this.val$cellLayout);
                                            }
                                        }) { // from class: com.android.launcher3.Workspace.8
                                            final Workspace this$0;
                                            final Runnable val$addResizeFrame;

                                            {
                                                this.this$0 = this;
                                                this.val$addResizeFrame = r5;
                                            }

                                            @Override // java.lang.Runnable
                                            public void run() {
                                                if (!this.this$0.isPageMoving()) {
                                                    this.val$addResizeFrame.run();
                                                    return;
                                                }
                                                this.this$0.mDelayedResizeRunnable = this.val$addResizeFrame;
                                            }
                                        };
                                    }
                                }
                            }
                        }
                        LauncherModel.modifyItemInDatabase(this.mLauncher, itemInfo2, j, idForScreen, layoutParams.cellX, layoutParams.cellY, itemInfo.spanX, itemInfo.spanY);
                        z = z4;
                    } else {
                        CellLayout.LayoutParams layoutParams2 = (CellLayout.LayoutParams) view.getLayoutParams();
                        this.mTargetCell[0] = layoutParams2.cellX;
                        this.mTargetCell[1] = layoutParams2.cellY;
                        ((CellLayout) view.getParent().getParent()).markCellsAsOccupiedForView(view);
                        z = z4;
                        runnable = null;
                    }
                }
            }
            if (view.getParent() == null) {
                LauncherLog.e("Launcher.Workspace", "error ,cell.getParent() == null");
                return;
            }
            CellLayout cellLayout2 = (CellLayout) view.getParent().getParent();
            Runnable runnable2 = new Runnable(this, runnable) { // from class: com.android.launcher3.Workspace.9
                final Workspace this$0;
                final Runnable val$finalResizeRunnable;

                {
                    this.this$0 = this;
                    this.val$finalResizeRunnable = runnable;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$0.mAnimatingViewIntoPlace = false;
                    this.this$0.updateChildrenLayersEnabled(false);
                    if (this.val$finalResizeRunnable != null) {
                        this.val$finalResizeRunnable.run();
                    }
                }
            };
            this.mAnimatingViewIntoPlace = true;
            if (dragObject.dragView.hasDrawn()) {
                ItemInfo itemInfo3 = (ItemInfo) view.getTag();
                if (itemInfo3.itemType != 4 ? itemInfo3.itemType == 5 : true) {
                    animateWidgetDrop(itemInfo3, cellLayout2, dragObject.dragView, runnable2, z ? 2 : 0, view, false);
                } else {
                    this.mLauncher.getDragLayer().animateViewIntoPosition(dragObject.dragView, view, i < 0 ? -1 : 300, runnable2, this);
                }
            } else {
                dragObject.deferDragViewCleanupPostAnimation = false;
                view.setVisibility(0);
            }
            cellLayout2.onDropChild(view);
        }
    }

    @Override // com.android.launcher3.DragSource
    public void onDropCompleted(View view, DropTarget.DragObject dragObject, boolean z, boolean z2) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher.Workspace", "onDropCompleted: target = " + view + ", d = " + dragObject + ", isFlingToDelete = " + z + ", mDragInfo = " + this.mDragInfo + ", success = " + z2);
        }
        if (this.mDeferDropAfterUninstall) {
            this.mDeferredAction = new Runnable(this, view, dragObject, z, z2) { // from class: com.android.launcher3.Workspace.13
                final Workspace this$0;
                final DropTarget.DragObject val$d;
                final boolean val$isFlingToDelete;
                final boolean val$success;
                final View val$target;

                {
                    this.this$0 = this;
                    this.val$target = view;
                    this.val$d = dragObject;
                    this.val$isFlingToDelete = z;
                    this.val$success = z2;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$0.onDropCompleted(this.val$target, this.val$d, this.val$isFlingToDelete, this.val$success);
                    this.this$0.mDeferredAction = null;
                }
            };
            return;
        }
        boolean z3 = this.mDeferredAction != null;
        if (!z2 || (z3 && !this.mUninstallSuccessful)) {
            if (this.mDragInfo != null && view != null) {
                CellLayout cellLayout = this.mLauncher.getCellLayout(this.mDragInfo.container, this.mDragInfo.screenId);
                if (cellLayout != null) {
                    cellLayout.onDropChild(this.mDragInfo.cell);
                } else if (LauncherAppState.isDogfoodBuild()) {
                    throw new RuntimeException("Invalid state: cellLayout == null in Workspace#onDropCompleted. Please file a bug. ");
                }
            }
        } else if (view != this && this.mDragInfo != null) {
            removeWorkspaceItem(this.mDragInfo.cell);
        }
        if ((dragObject.cancelled || (z3 && !this.mUninstallSuccessful)) && this.mDragInfo.cell != null) {
            this.mDragInfo.cell.setVisibility(0);
        }
        this.mDragOutline = null;
        this.mDragInfo = null;
    }

    @Override // com.android.launcher3.PagedView
    public void onEndReordering() {
        super.onEndReordering();
        if (this.mLauncher.isWorkspaceLoading()) {
            return;
        }
        this.mScreenOrder.clear();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            this.mScreenOrder.add(Long.valueOf(getIdForScreen((CellLayout) getChildAt(i))));
        }
        this.mLauncher.getModel().updateWorkspaceScreenOrder(this.mLauncher, this.mScreenOrder);
        enableLayoutTransitions();
    }

    @Override // com.android.launcher3.DragScroller
    public boolean onEnterScrollArea(int i, int i2, int i3) {
        boolean z = !this.mLauncher.getDeviceProfile().isLandscape;
        if (this.mLauncher.getHotseat() != null && z) {
            Rect rect = new Rect();
            this.mLauncher.getHotseat().getHitRect(rect);
            if (rect.contains(i, i2)) {
                return false;
            }
        }
        boolean z2 = false;
        if (!workspaceInModalState()) {
            if (this.mIsSwitchingState) {
                z2 = false;
            } else {
                z2 = false;
                if (getOpenFolder() == null) {
                    this.mInScrollArea = true;
                    int nextPage = getNextPage();
                    int i4 = 1;
                    if (i3 == 0) {
                        i4 = -1;
                    }
                    int i5 = nextPage + i4;
                    setCurrentDropLayout(null);
                    z2 = false;
                    if (i5 >= 0) {
                        z2 = false;
                        if (i5 < getChildCount()) {
                            if (getScreenIdForPageIndex(i5) == -301) {
                                return false;
                            }
                            setCurrentDragOverlappingLayout((CellLayout) getChildAt(i5));
                            invalidate();
                            z2 = true;
                        }
                    }
                }
            }
        }
        return z2;
    }

    @Override // com.android.launcher3.DragScroller
    public boolean onExitScrollArea() {
        boolean z = false;
        if (this.mInScrollArea) {
            invalidate();
            CellLayout currentDropLayout = getCurrentDropLayout();
            setCurrentDropLayout(currentDropLayout);
            setCurrentDragOverlappingLayout(currentDropLayout);
            z = true;
            this.mInScrollArea = false;
        }
        return z;
    }

    @Override // com.android.launcher3.DropTarget
    public void onFlingToDelete(DropTarget.DragObject dragObject, PointF pointF) {
    }

    @Override // com.android.launcher3.DragSource
    public void onFlingToDeleteCompleted() {
    }

    @Override // com.android.launcher3.PagedView, android.view.View
    public boolean onGenericMotionEvent(MotionEvent motionEvent) {
        if (getScreenIdForPageIndex(getCurrentPage()) != -301 || this.mCustomContentCallbacks == null || this.mCustomContentCallbacks.isScrollingAllowed()) {
            return super.onGenericMotionEvent(motionEvent);
        }
        return false;
    }

    @Override // com.android.launcher3.PagedView, android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (LauncherLog.DEBUG_MOTION) {
            LauncherLog.d("Launcher.Workspace", "onInterceptTouchEvent: ev = " + motionEvent + ", mScrollX = " + getScrollX());
        }
        switch (motionEvent.getAction() & 255) {
            case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                LauncherHelper.beginSection("Workspace.ACTION_DOWN");
                this.mXDown = motionEvent.getX();
                this.mYDown = motionEvent.getY();
                this.mTouchDownTime = System.currentTimeMillis();
                LauncherHelper.endSection();
                break;
            case 1:
            case 6:
                LauncherHelper.beginSection("Workspace.ACTION_UP");
                if (this.mTouchState == 0 && ((CellLayout) getChildAt(this.mCurrentPage)) != null) {
                    onWallpaperTap(motionEvent);
                }
                LauncherHelper.endSection();
                break;
        }
        return super.onInterceptTouchEvent(motionEvent);
    }

    @Override // com.android.launcher3.LauncherTransitionable
    public void onLauncherTransitionEnd(Launcher launcher, boolean z, boolean z2) {
        LauncherHelper.beginSection("Workspace.onLauncherTransitionEnd");
        this.mIsSwitchingState = false;
        updateChildrenLayersEnabled(false);
        showCustomContentIfNecessary();
        LauncherHelper.endSection();
    }

    @Override // com.android.launcher3.LauncherTransitionable
    public void onLauncherTransitionPrepare(Launcher launcher, boolean z, boolean z2) {
        this.mIsSwitchingState = true;
        this.mTransitionProgress = 0.0f;
        invalidate();
        updateChildrenLayersEnabled(false);
        hideCustomContentIfNecessary();
    }

    @Override // com.android.launcher3.LauncherTransitionable
    public void onLauncherTransitionStart(Launcher launcher, boolean z, boolean z2) {
    }

    @Override // com.android.launcher3.LauncherTransitionable
    public void onLauncherTransitionStep(Launcher launcher, float f) {
        this.mTransitionProgress = f;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.launcher3.PagedView, android.view.ViewGroup, android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        if (this.mFirstLayout && this.mCurrentPage >= 0 && this.mCurrentPage < getChildCount()) {
            this.mWallpaperOffset.syncWithScroll();
            this.mWallpaperOffset.jumpToFinal();
        }
        super.onLayout(z, i, i2, i3, i4);
        if (LauncherLog.DEBUG_LAYOUT) {
            LauncherLog.d("Launcher.Workspace", "onLayout: changed = " + z + ", left = " + i + ", top = " + i2 + ", right = " + i3 + ", bottom = " + i4);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.launcher3.PagedView
    public void onPageBeginMoving() {
        super.onPageBeginMoving();
        if (isHardwareAccelerated()) {
            updateChildrenLayersEnabled(false);
        } else if (this.mNextPage != -1) {
            enableChildrenCache(this.mCurrentPage, this.mNextPage);
        } else {
            enableChildrenCache(this.mCurrentPage - 1, this.mCurrentPage + 1);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.launcher3.PagedView
    public void onPageEndMoving() {
        super.onPageEndMoving();
        if (isHardwareAccelerated()) {
            updateChildrenLayersEnabled(false);
        } else {
            clearChildrenCache();
        }
        if (this.mDragController.isDragging() && workspaceInModalState()) {
            this.mDragController.forceTouchMove();
        }
        if (this.mDelayedResizeRunnable != null) {
            this.mDelayedResizeRunnable.run();
            this.mDelayedResizeRunnable = null;
        }
        if (this.mDelayedSnapToPageRunnable != null) {
            this.mDelayedSnapToPageRunnable.run();
            this.mDelayedSnapToPageRunnable = null;
        }
        if (this.mStripScreensOnPageStopMoving) {
            stripEmptyScreens();
            this.mStripScreensOnPageStopMoving = false;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.launcher3.PagedView, android.view.ViewGroup
    public boolean onRequestFocusInDescendants(int i, Rect rect) {
        if (this.mLauncher.isAppsViewVisible()) {
            return false;
        }
        Folder openFolder = getOpenFolder();
        return openFolder != null ? openFolder.requestFocus(i, rect) : super.onRequestFocusInDescendants(i, rect);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onResume() {
        View.OnClickListener pageIndicatorClickListener;
        if (getPageIndicator() != null && (pageIndicatorClickListener = getPageIndicatorClickListener()) != null) {
            getPageIndicator().setOnClickListener(pageIndicatorClickListener);
        }
        if (LauncherAppState.getInstance().hasWallpaperChangedSinceLastCheck()) {
            setWallpaperDimension();
        }
        this.mWallpaperIsLiveWallpaper = this.mWallpaperManager.getWallpaperInfo() != null;
        this.mLastSetWallpaperOffsetSteps = 0.0f;
    }

    @Override // com.android.launcher3.PagedView
    protected void onScrollInteractionBegin() {
        super.onScrollInteractionEnd();
        this.mScrollInteractionBegan = true;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.launcher3.PagedView
    public void onScrollInteractionEnd() {
        super.onScrollInteractionEnd();
        this.mScrollInteractionBegan = false;
        if (this.mStartedSendingScrollEvents) {
            this.mStartedSendingScrollEvents = false;
            this.mLauncherOverlay.onScrollInteractionEnd();
        }
    }

    @Override // com.android.launcher3.PagedView
    public void onStartReordering() {
        super.onStartReordering();
        disableLayoutTransitions();
    }

    @Override // android.view.View.OnTouchListener
    @SuppressLint({"ClickableViewAccessibility"})
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (LauncherLog.DEBUG_MOTION) {
            LauncherLog.d("Launcher.Workspace", "onTouch: v = " + view + ", event = " + motionEvent + ", isFinishedSwitchingState() = " + isFinishedSwitchingState() + ", mState = " + this.mState + ", mScrollX = " + getScrollX());
        }
        boolean z = true;
        if (!workspaceInModalState()) {
            z = true;
            if (isFinishedSwitchingState()) {
                z = (workspaceInModalState() || indexOfChild(view) == this.mCurrentPage) ? false : true;
            }
        }
        return z;
    }

    @Override // com.android.launcher3.PagedView, android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                LauncherLog.d("Launcher.Workspace", "onTouchEvent: Set HW layer on in touch down event");
                updateChildrenLayersEnabled(true);
                break;
            case 1:
                LauncherLog.d("Launcher.Workspace", "onTouchEvent: Set HW layer off in touch up event");
                updateChildrenLayersEnabled(false);
                break;
        }
        return super.onTouchEvent(motionEvent);
    }

    @Override // com.android.launcher3.UninstallDropTarget.UninstallSource
    public void onUninstallActivityReturned(boolean z) {
        this.mDeferDropAfterUninstall = false;
        this.mUninstallSuccessful = z;
        if (this.mDeferredAction != null) {
            this.mDeferredAction.run();
        }
    }

    protected void onWallpaperTap(MotionEvent motionEvent) {
        int[] iArr = this.mTempCell;
        getLocationOnScreen(iArr);
        int actionIndex = motionEvent.getActionIndex();
        iArr[0] = iArr[0] + ((int) motionEvent.getX(actionIndex));
        iArr[1] = iArr[1] + ((int) motionEvent.getY(actionIndex));
        this.mWallpaperManager.sendWallpaperCommand(getWindowToken(), motionEvent.getAction() == 1 ? "android.wallpaper.tap" : "android.wallpaper.secondaryTap", iArr[0], iArr[1], 0, null);
    }

    @Override // android.view.View
    protected void onWindowVisibilityChanged(int i) {
        this.mLauncher.onWindowVisibilityChanged(i);
    }

    @Override // com.android.launcher3.PagedView
    protected void overScroll(float f) {
        boolean z = (f > 0.0f || (hasCustomContent() && !this.mIsRtl)) ? f >= 0.0f && !(hasCustomContent() && this.mIsRtl) : true;
        boolean z2 = this.mLauncherOverlay != null ? (f > 0.0f || this.mIsRtl) ? f >= 0.0f ? this.mIsRtl : false : true : false;
        boolean z3 = (this.mLauncherOverlay == null || this.mLastOverlaySroll == 0.0f) ? false : (f < 0.0f || this.mIsRtl) ? f <= 0.0f ? this.mIsRtl : false : true;
        if (z2) {
            if (!this.mStartedSendingScrollEvents && this.mScrollInteractionBegan) {
                this.mStartedSendingScrollEvents = true;
                this.mLauncherOverlay.onScrollInteractionBegin();
            }
            this.mLastOverlaySroll = Math.abs(f / getViewportWidth());
            this.mLauncherOverlay.onScrollChange(this.mLastOverlaySroll, this.mIsRtl);
        } else if (z) {
            dampedOverScroll(f);
        }
        if (z3) {
            this.mLauncherOverlay.onScrollChange(0.0f, this.mIsRtl);
        }
    }

    @Override // com.android.launcher3.DropTarget
    public void prepareAccessibilityDrop() {
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void reinflateWidgetsIfNecessary() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            ShortcutAndWidgetContainer shortcutsAndWidgets = ((CellLayout) getChildAt(i)).getShortcutsAndWidgets();
            int childCount2 = shortcutsAndWidgets.getChildCount();
            for (int i2 = 0; i2 < childCount2; i2++) {
                View childAt = shortcutsAndWidgets.getChildAt(i2);
                if (childAt != null && (childAt.getTag() instanceof LauncherAppWidgetInfo)) {
                    LauncherAppWidgetInfo launcherAppWidgetInfo = (LauncherAppWidgetInfo) childAt.getTag();
                    LauncherAppWidgetHostView launcherAppWidgetHostView = (LauncherAppWidgetHostView) launcherAppWidgetInfo.hostView;
                    if (launcherAppWidgetHostView != null && launcherAppWidgetHostView.isReinflateRequired()) {
                        this.mLauncher.removeItem(launcherAppWidgetHostView, launcherAppWidgetInfo, false);
                        this.mLauncher.bindAppWidget(launcherAppWidgetInfo);
                    }
                }
            }
        }
    }

    public void removeAbandonedPromise(String str, UserHandleCompat userHandleCompat) {
        HashSet<String> hashSet = new HashSet<>(1);
        hashSet.add(str);
        LauncherModel.deletePackageFromDatabase(this.mLauncher, str, userHandleCompat);
        removeItemsByPackageName(hashSet, userHandleCompat);
    }

    public void removeAllWorkspaceScreens() {
        disableLayoutTransitions();
        if (hasCustomContent()) {
            removeCustomContentPage();
        }
        removeAllViews();
        this.mScreenOrder.clear();
        this.mWorkspaceScreens.clear();
        enableLayoutTransitions();
    }

    public void removeCustomContentPage() {
        CellLayout screenWithId = getScreenWithId(-301L);
        if (screenWithId == null) {
            throw new RuntimeException("Expected custom content screen to exist");
        }
        this.mWorkspaceScreens.remove(-301L);
        this.mScreenOrder.remove((Object) (-301L));
        screenWithId.clear();
        removeView(screenWithId);
        if (this.mCustomContentCallbacks != null) {
            this.mCustomContentCallbacks.onScrollProgressChanged(0.0f);
            this.mCustomContentCallbacks.onHide();
        }
        this.mCustomContentCallbacks = null;
        this.mDefaultPage = this.mOriginalDefaultPage - 1;
        if (this.mRestorePage != -1001) {
            this.mRestorePage--;
        } else {
            setCurrentPage(getCurrentPage() - 1);
        }
    }

    public void removeExtraEmptyScreen(boolean z, boolean z2) {
        removeExtraEmptyScreenDelayed(z, null, 0, z2);
    }

    public void removeExtraEmptyScreenDelayed(boolean z, Runnable runnable, int i, boolean z2) {
        if (this.mLauncher.isWorkspaceLoading()) {
            Launcher.addDumpLog("Launcher.Workspace", "    - workspace loading, skip", true);
        } else if (i > 0) {
            postDelayed(new Runnable(this, z, runnable, z2) { // from class: com.android.launcher3.Workspace.2
                final Workspace this$0;
                final boolean val$animate;
                final Runnable val$onComplete;
                final boolean val$stripEmptyScreens;

                {
                    this.this$0 = this;
                    this.val$animate = z;
                    this.val$onComplete = runnable;
                    this.val$stripEmptyScreens = z2;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$0.removeExtraEmptyScreenDelayed(this.val$animate, this.val$onComplete, 0, this.val$stripEmptyScreens);
                }
            }, i);
        } else {
            convertFinalScreenToEmptyScreenIfNecessary();
            if (!hasExtraEmptyScreen()) {
                if (z2) {
                    stripEmptyScreens();
                }
                if (runnable != null) {
                    runnable.run();
                    return;
                }
                return;
            }
            if (getNextPage() == this.mScreenOrder.indexOf(-201L)) {
                snapToPage(getNextPage() - 1, 400);
                fadeAndRemoveEmptyScreen(400, 150, runnable, z2);
                return;
            }
            snapToPage(getNextPage(), 0);
            fadeAndRemoveEmptyScreen(0, 150, runnable, z2);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void removeItemsByComponentName(HashSet<ComponentName> hashSet, UserHandleCompat userHandleCompat) {
        for (CellLayout cellLayout : getWorkspaceAndHotseatCellLayouts()) {
            ShortcutAndWidgetContainer shortcutsAndWidgets = cellLayout.getShortcutsAndWidgets();
            HashMap hashMap = new HashMap();
            for (int i = 0; i < shortcutsAndWidgets.getChildCount(); i++) {
                View childAt = shortcutsAndWidgets.getChildAt(i);
                hashMap.put((ItemInfo) childAt.getTag(), childAt);
            }
            ArrayList<View> arrayList = new ArrayList();
            HashMap hashMap2 = new HashMap();
            LauncherModel.filterItemInfos(hashMap.keySet(), new LauncherModel.ItemInfoFilter(this, hashSet, userHandleCompat, hashMap2, arrayList, hashMap) { // from class: com.android.launcher3.Workspace.21
                final Workspace this$0;
                final HashMap val$children;
                final ArrayList val$childrenToRemove;
                final HashSet val$componentNames;
                final HashMap val$folderAppsToRemove;
                final UserHandleCompat val$user;

                {
                    this.this$0 = this;
                    this.val$componentNames = hashSet;
                    this.val$user = userHandleCompat;
                    this.val$folderAppsToRemove = hashMap2;
                    this.val$childrenToRemove = arrayList;
                    this.val$children = hashMap;
                }

                @Override // com.android.launcher3.LauncherModel.ItemInfoFilter
                public boolean filterItem(ItemInfo itemInfo, ItemInfo itemInfo2, ComponentName componentName) {
                    ArrayList arrayList2;
                    if (itemInfo instanceof FolderInfo) {
                        if (this.val$componentNames.contains(componentName) && itemInfo2.user.equals(this.val$user)) {
                            FolderInfo folderInfo = (FolderInfo) itemInfo;
                            if (this.val$folderAppsToRemove.containsKey(folderInfo)) {
                                arrayList2 = (ArrayList) this.val$folderAppsToRemove.get(folderInfo);
                            } else {
                                arrayList2 = new ArrayList();
                                this.val$folderAppsToRemove.put(folderInfo, arrayList2);
                            }
                            arrayList2.add((ShortcutInfo) itemInfo2);
                            return true;
                        }
                        return false;
                    } else if (this.val$componentNames.contains(componentName) && itemInfo2.user.equals(this.val$user)) {
                        this.val$childrenToRemove.add((View) this.val$children.get(itemInfo2));
                        return true;
                    } else {
                        Iterator it = this.val$componentNames.iterator();
                        while (it.hasNext()) {
                            if (((ComponentName) it.next()).getPackageName().equals(componentName.getPackageName())) {
                                this.val$childrenToRemove.add((View) this.val$children.get(itemInfo2));
                                return true;
                            }
                        }
                        return false;
                    }
                }
            });
            for (FolderInfo folderInfo : hashMap2.keySet()) {
                for (ShortcutInfo shortcutInfo : (ArrayList) hashMap2.get(folderInfo)) {
                    folderInfo.remove(shortcutInfo);
                }
            }
            for (View view : arrayList) {
                cellLayout.removeViewInLayout(view);
                if (view instanceof DropTarget) {
                    this.mDragController.removeDropTarget((DropTarget) view);
                }
            }
            if (arrayList.size() > 0) {
                shortcutsAndWidgets.requestLayout();
                shortcutsAndWidgets.invalidate();
            }
        }
        removeExtraEmptyScreen(false, true);
        stripEmptyScreens();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void removeItemsByPackageName(HashSet<String> hashSet, UserHandleCompat userHandleCompat) {
        HashSet hashSet2 = new HashSet();
        HashSet<ComponentName> hashSet3 = new HashSet<>();
        for (CellLayout cellLayout : getWorkspaceAndHotseatCellLayouts()) {
            ShortcutAndWidgetContainer shortcutsAndWidgets = cellLayout.getShortcutsAndWidgets();
            int childCount = shortcutsAndWidgets.getChildCount();
            for (int i = 0; i < childCount; i++) {
                hashSet2.add((ItemInfo) shortcutsAndWidgets.getChildAt(i).getTag());
            }
        }
        LauncherModel.filterItemInfos(hashSet2, new LauncherModel.ItemInfoFilter(this, hashSet, userHandleCompat, hashSet3) { // from class: com.android.launcher3.Workspace.20
            final Workspace this$0;
            final HashSet val$cns;
            final HashSet val$packageNames;
            final UserHandleCompat val$user;

            {
                this.this$0 = this;
                this.val$packageNames = hashSet;
                this.val$user = userHandleCompat;
                this.val$cns = hashSet3;
            }

            @Override // com.android.launcher3.LauncherModel.ItemInfoFilter
            public boolean filterItem(ItemInfo itemInfo, ItemInfo itemInfo2, ComponentName componentName) {
                if (this.val$packageNames.contains(componentName.getPackageName()) && itemInfo2.user.equals(this.val$user)) {
                    this.val$cns.add(componentName);
                    return true;
                }
                return false;
            }
        });
        removeItemsByComponentName(hashSet3, userHandleCompat);
    }

    public void removeWorkspaceItem(View view) {
        CellLayout parentCellLayoutForView = getParentCellLayoutForView(view);
        if (parentCellLayoutForView != null) {
            parentCellLayoutForView.removeView(view);
        } else if (LauncherAppState.isDogfoodBuild()) {
            Log.e("Launcher.Workspace", "mDragInfo.cell has null parent");
        }
        if (view instanceof DropTarget) {
            this.mDragController.removeDropTarget((DropTarget) view);
        }
    }

    public void resetTransitionTransform(CellLayout cellLayout) {
        if (isSwitchingState()) {
            setScaleX(this.mCurrentScale);
            setScaleY(this.mCurrentScale);
        }
    }

    public void restoreInstanceStateForChild(int i) {
        if (this.mSavedStates != null) {
            this.mRestoredPages.add(Integer.valueOf(i));
            CellLayout cellLayout = (CellLayout) getChildAt(i);
            if (cellLayout != null) {
                cellLayout.restoreInstanceState(this.mSavedStates);
            }
        }
    }

    public void restoreInstanceStateForRemainingPages() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (!this.mRestoredPages.contains(Integer.valueOf(i))) {
                restoreInstanceStateForChild(i);
            }
        }
        this.mRestoredPages.clear();
        this.mSavedStates = null;
    }

    @Override // com.android.launcher3.PagedView
    protected void screenScrolled(int i) {
        updatePageAlphaValues(i);
        updateStateForCustomContent(i);
        enableHwLayersOnVisiblePages();
        if (getChildCount() == 0 && LauncherLog.DEBUG) {
            LauncherLog.d("Launcher.Workspace", "screenScrolled: getChildCount() = " + getChildCount());
        }
    }

    @Override // com.android.launcher3.PagedView, com.android.launcher3.DragScroller
    public void scrollLeft() {
        if (!workspaceInModalState() && !this.mIsSwitchingState) {
            super.scrollLeft();
        }
        Folder openFolder = getOpenFolder();
        if (openFolder != null) {
            openFolder.completeDragExit();
        }
    }

    @Override // com.android.launcher3.PagedView, com.android.launcher3.DragScroller
    public void scrollRight() {
        if (!workspaceInModalState() && !this.mIsSwitchingState) {
            super.scrollRight();
        }
        Folder openFolder = getOpenFolder();
        if (openFolder != null) {
            openFolder.completeDragExit();
        }
    }

    @Override // com.android.launcher3.PagedView, android.view.View
    public void scrollTo(int i, int i2) {
        this.mUnboundedScrollX = i;
        super.scrollTo(i, i2);
    }

    public void setAddNewPageOnDrag(boolean z) {
        this.mAddNewPageOnDrag = z;
    }

    void setCurrentDragOverlappingLayout(CellLayout cellLayout) {
        if (this.mDragOverlappingLayout != null) {
            this.mDragOverlappingLayout.setIsDragOverlapping(false);
        }
        this.mDragOverlappingLayout = cellLayout;
        if (this.mDragOverlappingLayout != null) {
            this.mDragOverlappingLayout.setIsDragOverlapping(true);
        }
        invalidate();
    }

    void setCurrentDropLayout(CellLayout cellLayout) {
        if (this.mDragTargetLayout != null) {
            this.mDragTargetLayout.revertTempState();
            this.mDragTargetLayout.onDragExit();
        }
        this.mDragTargetLayout = cellLayout;
        if (this.mDragTargetLayout != null) {
            this.mDragTargetLayout.onDragEnter();
        }
        cleanupReorder(true);
        cleanupFolderCreation();
        setCurrentDropOverCell(-1, -1);
    }

    void setCurrentDropOverCell(int i, int i2) {
        if (i == this.mDragOverX && i2 == this.mDragOverY) {
            return;
        }
        this.mDragOverX = i;
        this.mDragOverY = i2;
        setDragMode(0);
    }

    void setDragMode(int i) {
        if (i != this.mDragMode) {
            if (i == 0) {
                cleanupAddToFolder();
                cleanupReorder(false);
                cleanupFolderCreation();
            } else if (i == 2) {
                cleanupReorder(true);
                cleanupFolderCreation();
            } else if (i == 1) {
                cleanupAddToFolder();
                cleanupReorder(true);
            } else if (i == 3) {
                cleanupAddToFolder();
                cleanupFolderCreation();
            }
            this.mDragMode = i;
        }
    }

    public void setFinalTransitionTransform(CellLayout cellLayout) {
        if (isSwitchingState()) {
            this.mCurrentScale = getScaleX();
            setScaleX(this.mStateTransitionAnimation.getFinalScale());
            setScaleY(this.mStateTransitionAnimation.getFinalScale());
        }
    }

    @Override // com.android.launcher3.Insettable
    public void setInsets(Rect rect) {
        this.mInsets.set(rect);
        CellLayout screenWithId = getScreenWithId(-301L);
        if (screenWithId != null) {
            View childAt = screenWithId.getShortcutsAndWidgets().getChildAt(0);
            if (childAt instanceof Insettable) {
                ((Insettable) childAt).setInsets(this.mInsets);
            }
        }
    }

    public Animator setStateWithAnimation(State state, int i, boolean z, HashMap<View, Integer> hashMap) {
        AnimatorSet animationToState = this.mStateTransitionAnimation.getAnimationToState(this.mState, state, i, z, hashMap);
        boolean z2 = !this.mState.shouldUpdateWidget ? state.shouldUpdateWidget : false;
        this.mState = state;
        updateAccessibilityFlags();
        if (z2) {
            this.mLauncher.notifyWidgetProvidersChanged();
        }
        return animationToState;
    }

    /* JADX WARN: Type inference failed for: r0v1, types: [com.android.launcher3.Workspace$5] */
    protected void setWallpaperDimension() {
        if (LauncherLog.DEBUG) {
            LauncherLog.d("Launcher.Workspace", "setWallpaperDimension");
        }
        new AsyncTask<Void, Void, Void>(this) { // from class: com.android.launcher3.Workspace.5
            final Workspace this$0;

            {
                this.this$0 = this;
            }

            @Override // android.os.AsyncTask
            public Void doInBackground(Void... voidArr) {
                if (LauncherLog.DEBUG) {
                    LauncherLog.d("Launcher.Workspace", "setWallpaperDimension.doInBackground");
                }
                WallpaperUtils.suggestWallpaperDimension(this.this$0.mLauncher.getResources(), this.this$0.mLauncher.getSharedPreferences("com.android.launcher3.WallpaperCropActivity", 4), this.this$0.mLauncher.getWindowManager(), this.this$0.mWallpaperManager, this.this$0.mLauncher.overrideWallpaperDimensions());
                return null;
            }
        }.executeOnExecutor(Utilities.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setup(DragController dragController) {
        this.mSpringLoadedDragController = new SpringLoadedDragController(this.mLauncher);
        this.mDragController = dragController;
        updateChildrenLayersEnabled(false);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.launcher3.PagedView
    public boolean shouldDrawChild(View view) {
        boolean z;
        CellLayout cellLayout = (CellLayout) view;
        if (super.shouldDrawChild(view)) {
            z = true;
            if (!this.mIsSwitchingState) {
                if (cellLayout.getShortcutsAndWidgets().getAlpha() > 0.0f) {
                    z = true;
                } else {
                    z = true;
                    if (cellLayout.getBackgroundAlpha() <= 0.0f) {
                        z = false;
                    }
                }
            }
        } else {
            z = false;
        }
        return z;
    }

    void showCustomContentIfNecessary() {
        if ((this.mState == State.NORMAL) && hasCustomContent()) {
            this.mWorkspaceScreens.get(-301L).setVisibility(0);
        }
    }

    public void showOutlinesTemporarily() {
        if (this.mIsPageMoving || isTouchActive()) {
            return;
        }
        snapToPage(this.mCurrentPage);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.launcher3.PagedView
    public void snapToDestination() {
        if (!isScrollingOverlay()) {
            super.snapToDestination();
            return;
        }
        int i = this.mIsRtl ? this.mMaxScrollX : 0;
        this.mWasInOverscroll = false;
        scrollTo(i, getScrollY());
    }

    protected void snapToPage(int i, int i2, Runnable runnable) {
        if (this.mDelayedSnapToPageRunnable != null) {
            this.mDelayedSnapToPageRunnable.run();
        }
        this.mDelayedSnapToPageRunnable = runnable;
        snapToPage(i, i2);
    }

    protected void snapToPage(int i, Runnable runnable) {
        snapToPage(i, 950, runnable);
    }

    public void snapToScreenId(long j) {
        snapToScreenId(j, null);
    }

    protected void snapToScreenId(long j, Runnable runnable) {
        snapToPage(getPageIndexForScreenId(j), runnable);
    }

    public void startDrag(CellLayout.CellInfo cellInfo) {
        startDrag(cellInfo, false);
    }

    @Override // com.android.launcher3.accessibility.LauncherAccessibilityDelegate.AccessibilityDragSource
    public void startDrag(CellLayout.CellInfo cellInfo, boolean z) {
        View view = cellInfo.cell;
        if (LauncherLog.DEBUG_DRAG) {
            LauncherLog.d("Launcher.Workspace", "startDrag cellInfo = " + cellInfo + ",child = " + view);
        }
        if (view != null && view.getTag() == null) {
            LauncherLog.d("Launcher.Workspace", "Abnormal start drag: cellInfo = " + cellInfo + ",child = " + view);
        } else if (!view.isInTouchMode()) {
            if (LauncherLog.DEBUG) {
                LauncherLog.i("Launcher.Workspace", "The child " + view + " is not in touch mode.");
            }
        } else if (!(view.getParent().getParent() instanceof CellLayout)) {
            LauncherLog.e("Launcher.Workspace", "child = " + view + ",  child.getParent() = " + view.getParent() + "  ,child.getParent().getParent() = " + view.getParent().getParent());
        } else {
            this.mDragInfo = cellInfo;
            view.setVisibility(4);
            ((CellLayout) view.getParent().getParent()).prepareChildForDrag(view);
            beginDragShared(view, this, z);
        }
    }

    public void stripEmptyScreens() {
        if (this.mLauncher.isWorkspaceLoading()) {
            Launcher.addDumpLog("Launcher.Workspace", "    - workspace loading, skip", true);
        } else if (isPageMoving()) {
            this.mStripScreensOnPageStopMoving = true;
        } else {
            int nextPage = getNextPage();
            ArrayList<Long> arrayList = new ArrayList();
            int size = this.mWorkspaceScreens.size();
            for (int i = 0; i < size; i++) {
                long keyAt = this.mWorkspaceScreens.keyAt(i);
                CellLayout valueAt = this.mWorkspaceScreens.valueAt(i);
                if (keyAt >= 0 && valueAt.getShortcutsAndWidgets().getChildCount() == 0) {
                    arrayList.add(Long.valueOf(keyAt));
                }
            }
            LauncherAccessibilityDelegate accessibilityDelegate = LauncherAppState.getInstance().getAccessibilityDelegate();
            int numCustomPages = numCustomPages();
            int i2 = 0;
            for (Long l : arrayList) {
                CellLayout cellLayout = this.mWorkspaceScreens.get(l.longValue());
                this.mWorkspaceScreens.remove(l.longValue());
                this.mScreenOrder.remove(l);
                if (getChildCount() > numCustomPages + 1) {
                    int i3 = i2;
                    if (indexOfChild(cellLayout) < nextPage) {
                        i3 = i2 + 1;
                    }
                    if (accessibilityDelegate != null && accessibilityDelegate.isInAccessibleDrag()) {
                        cellLayout.enableAccessibleDrag(false, 2);
                    }
                    removeView(cellLayout);
                    i2 = i3;
                } else {
                    this.mRemoveEmptyScreenRunnable = null;
                    this.mWorkspaceScreens.put(-201L, cellLayout);
                    this.mScreenOrder.add(-201L);
                }
            }
            if (!arrayList.isEmpty()) {
                this.mLauncher.getModel().updateWorkspaceScreenOrder(this.mLauncher, this.mScreenOrder);
            }
            if (i2 >= 0) {
                setCurrentPage(nextPage - i2);
            }
        }
    }

    @Override // com.android.launcher3.DragSource
    public boolean supportsAppInfoDropTarget() {
        return false;
    }

    @Override // com.android.launcher3.DragSource
    public boolean supportsDeleteDropTarget() {
        return true;
    }

    @Override // com.android.launcher3.DragSource
    public boolean supportsFlingToDelete() {
        return true;
    }

    public boolean transitionStateShouldAllowDrop() {
        boolean z = false;
        if (!isSwitchingState() || this.mTransitionProgress > 0.5f) {
            z = true;
            if (this.mState != State.NORMAL) {
                z = this.mState == State.SPRING_LOADED;
            }
        }
        return z;
    }

    public void updateAccessibilityFlags() {
        if (!Utilities.ATLEAST_LOLLIPOP) {
            setImportantForAccessibility(this.mState == State.NORMAL ? 0 : 4);
            return;
        }
        int pageCount = getPageCount();
        for (int numCustomPages = numCustomPages(); numCustomPages < pageCount; numCustomPages++) {
            updateAccessibilityFlags((CellLayout) getPageAt(numCustomPages), numCustomPages);
        }
        setImportantForAccessibility((this.mState == State.NORMAL || this.mState == State.OVERVIEW) ? 0 : 4);
    }

    public void updateChildrenLayersEnabled(boolean z) {
        boolean isPageMoving = (z || (this.mState != State.OVERVIEW ? this.mIsSwitchingState : true) || this.mAnimatingViewIntoPlace) ? true : isPageMoving();
        if (isPageMoving != this.mChildrenLayersEnabled) {
            this.mChildrenLayersEnabled = isPageMoving;
            if (this.mChildrenLayersEnabled) {
                enableHwLayersOnVisiblePages();
                return;
            }
            for (int i = 0; i < getPageCount(); i++) {
                ((CellLayout) getChildAt(i)).enableHardwareLayer(false);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void updateCustomContentVisibility() {
        int i = this.mState == State.NORMAL ? 0 : 4;
        if (hasCustomContent()) {
            this.mWorkspaceScreens.get(-301L).setVisibility(i);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void updateItemLocationsInDatabase(CellLayout cellLayout) {
        int childCount = cellLayout.getShortcutsAndWidgets().getChildCount();
        long idForScreen = getIdForScreen(cellLayout);
        int i = -100;
        if (this.mLauncher.isHotseatLayout(cellLayout)) {
            idForScreen = -1;
            i = -101;
        }
        for (int i2 = 0; i2 < childCount; i2++) {
            ItemInfo itemInfo = (ItemInfo) cellLayout.getShortcutsAndWidgets().getChildAt(i2).getTag();
            if (itemInfo != null && itemInfo.requiresDbUpdate) {
                itemInfo.requiresDbUpdate = false;
                LauncherModel.modifyItemInDatabase(this.mLauncher, itemInfo, i, idForScreen, itemInfo.cellX, itemInfo.cellY, itemInfo.spanX, itemInfo.spanY);
            }
        }
    }

    public void updateRestoreItems(HashSet<ItemInfo> hashSet) {
        mapOverItems(true, new ItemOperator(this, hashSet) { // from class: com.android.launcher3.Workspace.23
            final Workspace this$0;
            final HashSet val$updates;

            {
                this.this$0 = this;
                this.val$updates = hashSet;
            }

            @Override // com.android.launcher3.Workspace.ItemOperator
            public boolean evaluate(ItemInfo itemInfo, View view, View view2) {
                if ((itemInfo instanceof ShortcutInfo) && (view instanceof BubbleTextView) && this.val$updates.contains(itemInfo)) {
                    ((BubbleTextView) view).applyState(false);
                    return false;
                } else if ((view instanceof PendingAppWidgetHostView) && (itemInfo instanceof LauncherAppWidgetInfo) && this.val$updates.contains(itemInfo)) {
                    ((PendingAppWidgetHostView) view).applyState();
                    return false;
                } else {
                    return false;
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void updateShortcuts(ArrayList<ShortcutInfo> arrayList) {
        mapOverItems(true, new ItemOperator(this, new HashSet(arrayList)) { // from class: com.android.launcher3.Workspace.22
            final Workspace this$0;
            final HashSet val$updates;

            {
                this.this$0 = this;
                this.val$updates = r5;
            }

            @Override // com.android.launcher3.Workspace.ItemOperator
            public boolean evaluate(ItemInfo itemInfo, View view, View view2) {
                if ((itemInfo instanceof ShortcutInfo) && (view instanceof BubbleTextView) && this.val$updates.contains(itemInfo)) {
                    ShortcutInfo shortcutInfo = (ShortcutInfo) itemInfo;
                    BubbleTextView bubbleTextView = (BubbleTextView) view;
                    Drawable textViewIcon = Workspace.getTextViewIcon(bubbleTextView);
                    bubbleTextView.applyFromShortcutInfo(shortcutInfo, this.this$0.mIconCache, shortcutInfo.isPromise() != (textViewIcon instanceof PreloadIconDrawable ? ((PreloadIconDrawable) textViewIcon).hasNotCompleted() : false));
                    if (view2 != null) {
                        view2.invalidate();
                        return false;
                    }
                    return false;
                }
                return false;
            }
        });
    }

    public void widgetsRestored(ArrayList<LauncherAppWidgetInfo> arrayList) {
        if (arrayList.isEmpty()) {
            return;
        }
        DeferredWidgetRefresh deferredWidgetRefresh = new DeferredWidgetRefresh(this, arrayList, this.mLauncher.getAppWidgetHost());
        LauncherAppWidgetInfo launcherAppWidgetInfo = arrayList.get(0);
        if ((launcherAppWidgetInfo.hasRestoreFlag(1) ? AppWidgetManagerCompat.getInstance(this.mLauncher).findProvider(launcherAppWidgetInfo.providerName, launcherAppWidgetInfo.user) : AppWidgetManagerCompat.getInstance(this.mLauncher).getAppWidgetInfo(launcherAppWidgetInfo.appWidgetId)) != null) {
            deferredWidgetRefresh.run();
            return;
        }
        for (LauncherAppWidgetInfo launcherAppWidgetInfo2 : arrayList) {
            if (launcherAppWidgetInfo2.hostView instanceof PendingAppWidgetHostView) {
                launcherAppWidgetInfo2.installProgress = 100;
                ((PendingAppWidgetHostView) launcherAppWidgetInfo2.hostView).applyState();
            }
        }
    }

    boolean willAddToExistingUserFolder(Object obj, View view) {
        if (view != null) {
            CellLayout.LayoutParams layoutParams = (CellLayout.LayoutParams) view.getLayoutParams();
            if (layoutParams.useTmpCoords && (layoutParams.tmpCellX != layoutParams.cellX || layoutParams.tmpCellY != layoutParams.cellY)) {
                return false;
            }
        }
        return (view instanceof FolderIcon) && ((FolderIcon) view).acceptDrop(obj);
    }

    boolean willAddToExistingUserFolder(Object obj, CellLayout cellLayout, int[] iArr, float f) {
        if (f > this.mMaxDistanceForFolderCreation) {
            return false;
        }
        return willAddToExistingUserFolder(obj, cellLayout.getChildAt(iArr[0], iArr[1]));
    }

    boolean willCreateUserFolder(ItemInfo itemInfo, View view, boolean z) {
        if (view != null) {
            CellLayout.LayoutParams layoutParams = (CellLayout.LayoutParams) view.getLayoutParams();
            if (layoutParams.useTmpCoords && (layoutParams.tmpCellX != layoutParams.cellX || layoutParams.tmpCellY != layoutParams.cellY)) {
                return false;
            }
        }
        boolean z2 = false;
        if (this.mDragInfo != null) {
            z2 = view == this.mDragInfo.cell;
        }
        if (view == null || z2) {
            return false;
        }
        if (!z || this.mCreateUserFolderOnDrop) {
            boolean z3 = view.getTag() instanceof ShortcutInfo;
            boolean z4 = itemInfo.itemType != 0 ? itemInfo.itemType == 1 : true;
            if (!z3) {
                z4 = false;
            }
            return z4;
        }
        return false;
    }

    boolean willCreateUserFolder(ItemInfo itemInfo, CellLayout cellLayout, int[] iArr, float f, boolean z) {
        if (f > this.mMaxDistanceForFolderCreation) {
            return false;
        }
        return willCreateUserFolder(itemInfo, cellLayout.getChildAt(iArr[0], iArr[1]), z);
    }

    public boolean workspaceInModalState() {
        return this.mState != State.NORMAL;
    }
}
