package com.android.launcher3;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Parcelable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import com.android.launcher3.BubbleTextView;
import com.android.launcher3.DropTarget;
import com.android.launcher3.FolderIcon;
import com.android.launcher3.accessibility.DragAndDropAccessibilityDelegate;
import com.android.launcher3.accessibility.FolderAccessibilityHelper;
import com.android.launcher3.accessibility.WorkspaceAccessibilityHelper;
import com.android.launcher3.compat.PackageInstallerCompat;
import com.android.launcher3.util.ParcelableSparseArray;
import com.mediatek.launcher3.LauncherLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
/* loaded from: a.zip:com/android/launcher3/CellLayout.class */
public class CellLayout extends ViewGroup implements BubbleTextView.BubbleTextShadowHandler {
    private static final Paint sPaint = new Paint();
    private final TransitionDrawable mBackground;
    private float mBackgroundAlpha;
    int mCellHeight;
    int mCellWidth;
    int mCountX;
    int mCountY;
    private int[] mDirectionVector;
    private final int[] mDragCell;
    float[] mDragOutlineAlphas;
    private InterruptibleInOutAnimator[] mDragOutlineAnims;
    private int mDragOutlineCurrent;
    private final Paint mDragOutlinePaint;
    Rect[] mDragOutlines;
    private boolean mDragging;
    private boolean mDropPending;
    private TimeInterpolator mEaseOutInterpolator;
    private int mFixedCellHeight;
    private int mFixedCellWidth;
    private int mFixedHeight;
    private int mFixedWidth;
    private int[] mFolderLeaveBehindCell;
    private ArrayList<FolderIcon.FolderRingAnimator> mFolderOuterRings;
    int mHeightGap;
    private float mHotseatScale;
    private View.OnTouchListener mInterceptTouchListener;
    private ArrayList<View> mIntersectingViews;
    private boolean mIsDragOverlapping;
    private boolean mIsDragTarget;
    private boolean mIsHotseat;
    private boolean mItemPlacementDirty;
    private boolean mJailContent;
    private Launcher mLauncher;
    private int mMaxGap;
    boolean[][] mOccupied;
    private Rect mOccupiedRect;
    private int mOriginalHeightGap;
    private int mOriginalWidthGap;
    int[] mPreviousReorderDirection;
    HashMap<LayoutParams, Animator> mReorderAnimators;
    float mReorderPreviewAnimationMagnitude;
    HashMap<View, ReorderPreviewAnimation> mShakeAnimators;
    private ShortcutAndWidgetContainer mShortcutsAndWidgets;
    private StylusEventHelper mStylusEventHelper;
    final int[] mTempLocation;
    private final Rect mTempRect;
    private final Stack<Rect> mTempRectStack;
    boolean[][] mTmpOccupied;
    final int[] mTmpPoint;
    private final ClickShadowView mTouchFeedbackView;
    private DragAndDropAccessibilityDelegate mTouchHelper;
    private boolean mUseTouchHelper;
    int mWidthGap;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/launcher3/CellLayout$CellAndSpan.class */
    public class CellAndSpan {
        int spanX;
        int spanY;
        final CellLayout this$0;
        int x;
        int y;

        public CellAndSpan(CellLayout cellLayout) {
            this.this$0 = cellLayout;
        }

        public CellAndSpan(CellLayout cellLayout, int i, int i2, int i3, int i4) {
            this.this$0 = cellLayout;
            this.x = i;
            this.y = i2;
            this.spanX = i3;
            this.spanY = i4;
        }

        public void copy(CellAndSpan cellAndSpan) {
            cellAndSpan.x = this.x;
            cellAndSpan.y = this.y;
            cellAndSpan.spanX = this.spanX;
            cellAndSpan.spanY = this.spanY;
        }

        public String toString() {
            return "(" + this.x + ", " + this.y + ": " + this.spanX + ", " + this.spanY + ")";
        }
    }

    /* loaded from: a.zip:com/android/launcher3/CellLayout$CellInfo.class */
    public static final class CellInfo {
        View cell;
        int cellX;
        int cellY;
        long container;
        long screenId;
        int spanX;
        int spanY;

        public CellInfo(View view, ItemInfo itemInfo) {
            this.cellX = -1;
            this.cellY = -1;
            this.cell = view;
            this.cellX = itemInfo.cellX;
            this.cellY = itemInfo.cellY;
            this.spanX = itemInfo.spanX;
            this.spanY = itemInfo.spanY;
            this.screenId = itemInfo.screenId;
            this.container = itemInfo.container;
        }

        public String toString() {
            return "Cell[view=" + (this.cell == null ? "null" : this.cell.getClass()) + ", x=" + this.cellX + ", y=" + this.cellY + "]";
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:com/android/launcher3/CellLayout$ItemConfiguration.class */
    public class ItemConfiguration {
        int dragViewSpanX;
        int dragViewSpanY;
        int dragViewX;
        int dragViewY;
        ArrayList<View> intersectingViews;
        final CellLayout this$0;
        HashMap<View, CellAndSpan> map = new HashMap<>();
        private HashMap<View, CellAndSpan> savedMap = new HashMap<>();
        ArrayList<View> sortedViews = new ArrayList<>();
        boolean isSolution = false;

        ItemConfiguration(CellLayout cellLayout) {
            this.this$0 = cellLayout;
        }

        void add(View view, CellAndSpan cellAndSpan) {
            this.map.put(view, cellAndSpan);
            this.savedMap.put(view, new CellAndSpan(this.this$0));
            this.sortedViews.add(view);
        }

        int area() {
            return this.dragViewSpanX * this.dragViewSpanY;
        }

        void restore() {
            for (View view : this.savedMap.keySet()) {
                this.savedMap.get(view).copy(this.map.get(view));
            }
        }

        void save() {
            for (View view : this.map.keySet()) {
                this.map.get(view).copy(this.savedMap.get(view));
            }
        }
    }

    /* loaded from: a.zip:com/android/launcher3/CellLayout$LayoutParams.class */
    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        public boolean canReorder;
        @ViewDebug.ExportedProperty
        public int cellHSpan;
        @ViewDebug.ExportedProperty
        public int cellVSpan;
        @ViewDebug.ExportedProperty
        public int cellX;
        @ViewDebug.ExportedProperty
        public int cellY;
        boolean dropped;
        public boolean isFullscreen;
        public boolean isLockedToGrid;
        public int tmpCellX;
        public int tmpCellY;
        public boolean useTmpCoords;
        @ViewDebug.ExportedProperty
        int x;
        @ViewDebug.ExportedProperty
        int y;

        public LayoutParams(int i, int i2, int i3, int i4) {
            super(-1, -1);
            this.isLockedToGrid = true;
            this.isFullscreen = false;
            this.canReorder = true;
            this.cellX = i;
            this.cellY = i2;
            this.cellHSpan = i3;
            this.cellVSpan = i4;
        }

        public LayoutParams(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            this.isLockedToGrid = true;
            this.isFullscreen = false;
            this.canReorder = true;
            this.cellHSpan = 1;
            this.cellVSpan = 1;
        }

        public LayoutParams(ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
            this.isLockedToGrid = true;
            this.isFullscreen = false;
            this.canReorder = true;
            this.cellHSpan = 1;
            this.cellVSpan = 1;
        }

        public int getHeight() {
            return this.height;
        }

        public int getWidth() {
            return this.width;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        public void setHeight(int i) {
            this.height = i;
        }

        public void setWidth(int i) {
            this.width = i;
        }

        public void setX(int i) {
            this.x = i;
        }

        public void setY(int i) {
            this.y = i;
        }

        public void setup(int i, int i2, int i3, int i4, boolean z, int i5) {
            if (this.isLockedToGrid) {
                int i6 = this.cellHSpan;
                int i7 = this.cellVSpan;
                int i8 = this.useTmpCoords ? this.tmpCellX : this.cellX;
                int i9 = this.useTmpCoords ? this.tmpCellY : this.cellY;
                int i10 = i8;
                if (z) {
                    i10 = (i5 - i8) - this.cellHSpan;
                }
                this.width = (((i6 * i) + ((i6 - 1) * i3)) - this.leftMargin) - this.rightMargin;
                this.height = (((i7 * i2) + ((i7 - 1) * i4)) - this.topMargin) - this.bottomMargin;
                this.x = ((i + i3) * i10) + this.leftMargin;
                this.y = ((i2 + i4) * i9) + this.topMargin;
            }
        }

        public String toString() {
            return "(" + this.cellX + ", " + this.cellY + ")";
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:com/android/launcher3/CellLayout$ReorderPreviewAnimation.class */
    public class ReorderPreviewAnimation {
        Animator a;
        View child;
        float finalDeltaX;
        float finalDeltaY;
        float finalScale;
        float initDeltaX;
        float initDeltaY;
        float initScale;
        int mode;
        boolean repeating = false;
        final CellLayout this$0;

        public ReorderPreviewAnimation(CellLayout cellLayout, View view, int i, int i2, int i3, int i4, int i5, int i6, int i7) {
            this.this$0 = cellLayout;
            cellLayout.regionToCenterPoint(i2, i3, i6, i7, cellLayout.mTmpPoint);
            int i8 = cellLayout.mTmpPoint[0];
            int i9 = cellLayout.mTmpPoint[1];
            cellLayout.regionToCenterPoint(i4, i5, i6, i7, cellLayout.mTmpPoint);
            int i10 = cellLayout.mTmpPoint[0] - i8;
            int i11 = cellLayout.mTmpPoint[1] - i9;
            this.finalDeltaX = 0.0f;
            this.finalDeltaY = 0.0f;
            int i12 = i == 0 ? -1 : 1;
            if (i10 != i11 || i10 != 0) {
                if (i11 == 0) {
                    this.finalDeltaX = (-i12) * Math.signum(i10) * cellLayout.mReorderPreviewAnimationMagnitude;
                } else if (i10 == 0) {
                    this.finalDeltaY = (-i12) * Math.signum(i11) * cellLayout.mReorderPreviewAnimationMagnitude;
                } else {
                    double atan = Math.atan(i11 / i10);
                    this.finalDeltaX = (int) ((-i12) * Math.signum(i10) * Math.abs(Math.cos(atan) * cellLayout.mReorderPreviewAnimationMagnitude));
                    this.finalDeltaY = (int) ((-i12) * Math.signum(i11) * Math.abs(Math.sin(atan) * cellLayout.mReorderPreviewAnimationMagnitude));
                }
            }
            this.mode = i;
            this.initDeltaX = view.getTranslationX();
            this.initDeltaY = view.getTranslationY();
            this.finalScale = cellLayout.getChildrenScale() - (4.0f / view.getWidth());
            this.initScale = view.getScaleX();
            this.child = view;
        }

        private void cancel() {
            if (this.a != null) {
                this.a.cancel();
            }
        }

        void animate() {
            if (this.this$0.mShakeAnimators.containsKey(this.child)) {
                this.this$0.mShakeAnimators.get(this.child).cancel();
                this.this$0.mShakeAnimators.remove(this.child);
                if (this.finalDeltaX == 0.0f && this.finalDeltaY == 0.0f) {
                    completeAnimationImmediately();
                    return;
                }
            }
            if (this.finalDeltaX == 0.0f && this.finalDeltaY == 0.0f) {
                return;
            }
            ValueAnimator ofFloat = LauncherAnimUtils.ofFloat(this.child, 0.0f, 1.0f);
            this.a = ofFloat;
            if (!Utilities.isPowerSaverOn(this.this$0.getContext())) {
                ofFloat.setRepeatMode(2);
                ofFloat.setRepeatCount(-1);
            }
            ofFloat.setDuration(this.mode == 0 ? 350 : 300);
            ofFloat.setStartDelay((int) (Math.random() * 60.0d));
            ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this) { // from class: com.android.launcher3.CellLayout.ReorderPreviewAnimation.1
                final ReorderPreviewAnimation this$1;

                {
                    this.this$1 = this;
                }

                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                    float f = (this.this$1.mode == 0 && this.this$1.repeating) ? 1.0f : floatValue;
                    float f2 = this.this$1.finalDeltaX;
                    float f3 = this.this$1.initDeltaX;
                    float f4 = this.this$1.finalDeltaY;
                    float f5 = this.this$1.initDeltaY;
                    this.this$1.child.setTranslationX((f2 * f) + ((1.0f - f) * f3));
                    this.this$1.child.setTranslationY((f4 * f) + ((1.0f - f) * f5));
                    float f6 = (this.this$1.finalScale * floatValue) + ((1.0f - floatValue) * this.this$1.initScale);
                    this.this$1.child.setScaleX(f6);
                    this.this$1.child.setScaleY(f6);
                }
            });
            ofFloat.addListener(new AnimatorListenerAdapter(this) { // from class: com.android.launcher3.CellLayout.ReorderPreviewAnimation.2
                final ReorderPreviewAnimation this$1;

                {
                    this.this$1 = this;
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationRepeat(Animator animator) {
                    this.this$1.initDeltaX = 0.0f;
                    this.this$1.initDeltaY = 0.0f;
                    this.this$1.initScale = this.this$1.this$0.getChildrenScale();
                    this.this$1.repeating = true;
                }
            });
            this.this$0.mShakeAnimators.put(this.child, this);
            ofFloat.start();
        }

        void completeAnimationImmediately() {
            if (this.a != null) {
                this.a.cancel();
            }
            AnimatorSet createAnimatorSet = LauncherAnimUtils.createAnimatorSet();
            this.a = createAnimatorSet;
            createAnimatorSet.playTogether(LauncherAnimUtils.ofFloat(this.child, "scaleX", this.this$0.getChildrenScale()), LauncherAnimUtils.ofFloat(this.child, "scaleY", this.this$0.getChildrenScale()), LauncherAnimUtils.ofFloat(this.child, "translationX", 0.0f), LauncherAnimUtils.ofFloat(this.child, "translationY", 0.0f));
            createAnimatorSet.setDuration(150L);
            createAnimatorSet.setInterpolator(new DecelerateInterpolator(1.5f));
            createAnimatorSet.start();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/launcher3/CellLayout$ViewCluster.class */
    public class ViewCluster {
        int[] bottomEdge;
        boolean bottomEdgeDirty;
        boolean boundingRectDirty;
        ItemConfiguration config;
        int[] leftEdge;
        boolean leftEdgeDirty;
        int[] rightEdge;
        boolean rightEdgeDirty;
        final CellLayout this$0;
        int[] topEdge;
        boolean topEdgeDirty;
        ArrayList<View> views;
        Rect boundingRect = new Rect();
        PositionComparator comparator = new PositionComparator(this);

        /* JADX INFO: Access modifiers changed from: package-private */
        /* loaded from: a.zip:com/android/launcher3/CellLayout$ViewCluster$PositionComparator.class */
        public class PositionComparator implements Comparator<View> {
            final ViewCluster this$1;
            int whichEdge = 0;

            PositionComparator(ViewCluster viewCluster) {
                this.this$1 = viewCluster;
            }

            @Override // java.util.Comparator
            public int compare(View view, View view2) {
                CellAndSpan cellAndSpan = this.this$1.config.map.get(view);
                CellAndSpan cellAndSpan2 = this.this$1.config.map.get(view2);
                switch (this.whichEdge) {
                    case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                        return (cellAndSpan2.x + cellAndSpan2.spanX) - (cellAndSpan.x + cellAndSpan.spanX);
                    case 1:
                        return (cellAndSpan2.y + cellAndSpan2.spanY) - (cellAndSpan.y + cellAndSpan.spanY);
                    case 2:
                        return cellAndSpan.x - cellAndSpan2.x;
                    default:
                        return cellAndSpan.y - cellAndSpan2.y;
                }
            }
        }

        public ViewCluster(CellLayout cellLayout, ArrayList<View> arrayList, ItemConfiguration itemConfiguration) {
            this.this$0 = cellLayout;
            this.leftEdge = new int[this.this$0.mCountY];
            this.rightEdge = new int[this.this$0.mCountY];
            this.topEdge = new int[this.this$0.mCountX];
            this.bottomEdge = new int[this.this$0.mCountX];
            this.views = (ArrayList) arrayList.clone();
            this.config = itemConfiguration;
            resetEdges();
        }

        public void addView(View view) {
            this.views.add(view);
            resetEdges();
        }

        void computeEdge(int i, int[] iArr) {
            int size = this.views.size();
            for (int i2 = 0; i2 < size; i2++) {
                CellAndSpan cellAndSpan = this.config.map.get(this.views.get(i2));
                switch (i) {
                    case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                        int i3 = cellAndSpan.x;
                        for (int i4 = cellAndSpan.y; i4 < cellAndSpan.y + cellAndSpan.spanY; i4++) {
                            if (i3 < iArr[i4] || iArr[i4] < 0) {
                                iArr[i4] = i3;
                            }
                        }
                        break;
                    case 1:
                        int i5 = cellAndSpan.y;
                        for (int i6 = cellAndSpan.x; i6 < cellAndSpan.x + cellAndSpan.spanX; i6++) {
                            if (i5 < iArr[i6] || iArr[i6] < 0) {
                                iArr[i6] = i5;
                            }
                        }
                        break;
                    case 2:
                        int i7 = cellAndSpan.x + cellAndSpan.spanX;
                        for (int i8 = cellAndSpan.y; i8 < cellAndSpan.y + cellAndSpan.spanY; i8++) {
                            if (i7 > iArr[i8]) {
                                iArr[i8] = i7;
                            }
                        }
                        break;
                    case 3:
                        int i9 = cellAndSpan.y + cellAndSpan.spanY;
                        for (int i10 = cellAndSpan.x; i10 < cellAndSpan.x + cellAndSpan.spanX; i10++) {
                            if (i9 > iArr[i10]) {
                                iArr[i10] = i9;
                            }
                        }
                        break;
                }
            }
        }

        public int[] getBottomEdge() {
            if (this.bottomEdgeDirty) {
                computeEdge(3, this.bottomEdge);
            }
            return this.bottomEdge;
        }

        public Rect getBoundingRect() {
            if (this.boundingRectDirty) {
                boolean z = true;
                for (View view : this.views) {
                    CellAndSpan cellAndSpan = this.config.map.get(view);
                    if (z) {
                        this.boundingRect.set(cellAndSpan.x, cellAndSpan.y, cellAndSpan.x + cellAndSpan.spanX, cellAndSpan.y + cellAndSpan.spanY);
                        z = false;
                    } else {
                        this.boundingRect.union(cellAndSpan.x, cellAndSpan.y, cellAndSpan.x + cellAndSpan.spanX, cellAndSpan.y + cellAndSpan.spanY);
                    }
                }
            }
            return this.boundingRect;
        }

        public int[] getEdge(int i) {
            switch (i) {
                case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                    return getLeftEdge();
                case 1:
                    return getTopEdge();
                case 2:
                    return getRightEdge();
                default:
                    return getBottomEdge();
            }
        }

        public int[] getLeftEdge() {
            if (this.leftEdgeDirty) {
                computeEdge(0, this.leftEdge);
            }
            return this.leftEdge;
        }

        public int[] getRightEdge() {
            if (this.rightEdgeDirty) {
                computeEdge(2, this.rightEdge);
            }
            return this.rightEdge;
        }

        public int[] getTopEdge() {
            if (this.topEdgeDirty) {
                computeEdge(1, this.topEdge);
            }
            return this.topEdge;
        }

        boolean isViewTouchingEdge(View view, int i) {
            CellAndSpan cellAndSpan = this.config.map.get(view);
            int[] edge = getEdge(i);
            switch (i) {
                case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                    for (int i2 = cellAndSpan.y; i2 < cellAndSpan.y + cellAndSpan.spanY; i2++) {
                        if (edge[i2] == cellAndSpan.x + cellAndSpan.spanX) {
                            return true;
                        }
                    }
                    return false;
                case 1:
                    for (int i3 = cellAndSpan.x; i3 < cellAndSpan.x + cellAndSpan.spanX; i3++) {
                        if (edge[i3] == cellAndSpan.y + cellAndSpan.spanY) {
                            return true;
                        }
                    }
                    return false;
                case 2:
                    for (int i4 = cellAndSpan.y; i4 < cellAndSpan.y + cellAndSpan.spanY; i4++) {
                        if (edge[i4] == cellAndSpan.x) {
                            return true;
                        }
                    }
                    return false;
                case 3:
                    for (int i5 = cellAndSpan.x; i5 < cellAndSpan.x + cellAndSpan.spanX; i5++) {
                        if (edge[i5] == cellAndSpan.y) {
                            return true;
                        }
                    }
                    return false;
                default:
                    return false;
            }
        }

        void resetEdges() {
            for (int i = 0; i < this.this$0.mCountX; i++) {
                this.topEdge[i] = -1;
                this.bottomEdge[i] = -1;
            }
            for (int i2 = 0; i2 < this.this$0.mCountY; i2++) {
                this.leftEdge[i2] = -1;
                this.rightEdge[i2] = -1;
            }
            this.leftEdgeDirty = true;
            this.rightEdgeDirty = true;
            this.bottomEdgeDirty = true;
            this.topEdgeDirty = true;
            this.boundingRectDirty = true;
        }

        void shift(int i, int i2) {
            for (View view : this.views) {
                CellAndSpan cellAndSpan = this.config.map.get(view);
                switch (i) {
                    case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                        cellAndSpan.x -= i2;
                        break;
                    case 1:
                        cellAndSpan.y -= i2;
                        break;
                    case 2:
                        cellAndSpan.x += i2;
                        break;
                    default:
                        cellAndSpan.y += i2;
                        break;
                }
            }
            resetEdges();
        }

        public void sortConfigurationForEdgePush(int i) {
            this.comparator.whichEdge = i;
            Collections.sort(this.config.sortedViews, this.comparator);
        }
    }

    public CellLayout(Context context) {
        this(context, null);
    }

    public CellLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public CellLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mDropPending = false;
        this.mIsDragTarget = true;
        this.mJailContent = true;
        this.mTmpPoint = new int[2];
        this.mTempLocation = new int[2];
        this.mFolderOuterRings = new ArrayList<>();
        this.mFolderLeaveBehindCell = new int[]{-1, -1};
        this.mFixedWidth = -1;
        this.mFixedHeight = -1;
        this.mIsDragOverlapping = false;
        this.mDragOutlines = new Rect[4];
        this.mDragOutlineAlphas = new float[this.mDragOutlines.length];
        this.mDragOutlineAnims = new InterruptibleInOutAnimator[this.mDragOutlines.length];
        this.mDragOutlineCurrent = 0;
        this.mDragOutlinePaint = new Paint();
        this.mReorderAnimators = new HashMap<>();
        this.mShakeAnimators = new HashMap<>();
        this.mItemPlacementDirty = false;
        this.mDragCell = new int[2];
        this.mDragging = false;
        this.mIsHotseat = false;
        this.mHotseatScale = 1.0f;
        this.mIntersectingViews = new ArrayList<>();
        this.mOccupiedRect = new Rect();
        this.mDirectionVector = new int[2];
        this.mPreviousReorderDirection = new int[2];
        this.mTempRect = new Rect();
        this.mUseTouchHelper = false;
        this.mTempRectStack = new Stack<>();
        setWillNotDraw(false);
        setClipToPadding(false);
        this.mLauncher = (Launcher) context;
        DeviceProfile deviceProfile = this.mLauncher.getDeviceProfile();
        this.mCellHeight = -1;
        this.mCellWidth = -1;
        this.mFixedCellHeight = -1;
        this.mFixedCellWidth = -1;
        this.mOriginalWidthGap = 0;
        this.mWidthGap = 0;
        this.mOriginalHeightGap = 0;
        this.mHeightGap = 0;
        this.mMaxGap = Integer.MAX_VALUE;
        this.mCountX = deviceProfile.inv.numColumns;
        this.mCountY = deviceProfile.inv.numRows;
        this.mOccupied = new boolean[this.mCountX][this.mCountY];
        this.mTmpOccupied = new boolean[this.mCountX][this.mCountY];
        this.mPreviousReorderDirection[0] = -100;
        this.mPreviousReorderDirection[1] = -100;
        setAlwaysDrawnWithCacheEnabled(false);
        Resources resources = getResources();
        this.mHotseatScale = deviceProfile.hotseatIconSizePx / deviceProfile.iconSizePx;
        this.mBackground = (TransitionDrawable) resources.getDrawable(2130837508);
        this.mBackground.setCallback(this);
        this.mBackground.setAlpha((int) (this.mBackgroundAlpha * 255.0f));
        this.mReorderPreviewAnimationMagnitude = deviceProfile.iconSizePx * 0.12f;
        this.mEaseOutInterpolator = new DecelerateInterpolator(2.5f);
        int[] iArr = this.mDragCell;
        this.mDragCell[1] = -1;
        iArr[0] = -1;
        for (int i2 = 0; i2 < this.mDragOutlines.length; i2++) {
            this.mDragOutlines[i2] = new Rect(-1, -1, -1, -1);
        }
        int integer = resources.getInteger(2131427341);
        float integer2 = resources.getInteger(2131427342);
        Arrays.fill(this.mDragOutlineAlphas, 0.0f);
        for (int i3 = 0; i3 < this.mDragOutlineAnims.length; i3++) {
            InterruptibleInOutAnimator interruptibleInOutAnimator = new InterruptibleInOutAnimator(this, integer, 0.0f, integer2);
            interruptibleInOutAnimator.getAnimator().setInterpolator(this.mEaseOutInterpolator);
            interruptibleInOutAnimator.getAnimator().addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this, interruptibleInOutAnimator, i3) { // from class: com.android.launcher3.CellLayout.1
                final CellLayout this$0;
                final InterruptibleInOutAnimator val$anim;
                final int val$thisIndex;

                {
                    this.this$0 = this;
                    this.val$anim = interruptibleInOutAnimator;
                    this.val$thisIndex = i3;
                }

                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    if (((Bitmap) this.val$anim.getTag()) == null) {
                        valueAnimator.cancel();
                        return;
                    }
                    this.this$0.mDragOutlineAlphas[this.val$thisIndex] = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                    this.this$0.invalidate(this.this$0.mDragOutlines[this.val$thisIndex]);
                }
            });
            interruptibleInOutAnimator.getAnimator().addListener(new AnimatorListenerAdapter(this, interruptibleInOutAnimator) { // from class: com.android.launcher3.CellLayout.2
                final CellLayout this$0;
                final InterruptibleInOutAnimator val$anim;

                {
                    this.this$0 = this;
                    this.val$anim = interruptibleInOutAnimator;
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    if (((Float) ((ValueAnimator) animator).getAnimatedValue()).floatValue() == 0.0f) {
                        this.val$anim.setTag(null);
                    }
                }
            });
            this.mDragOutlineAnims[i3] = interruptibleInOutAnimator;
        }
        this.mShortcutsAndWidgets = new ShortcutAndWidgetContainer(context);
        this.mShortcutsAndWidgets.setCellDimensions(this.mCellWidth, this.mCellHeight, this.mWidthGap, this.mHeightGap, this.mCountX, this.mCountY);
        this.mStylusEventHelper = new StylusEventHelper(this);
        this.mTouchFeedbackView = new ClickShadowView(context);
        addView(this.mTouchFeedbackView);
        addView(this.mShortcutsAndWidgets);
    }

    private boolean addViewToTempLocation(View view, Rect rect, int[] iArr, ItemConfiguration itemConfiguration) {
        CellAndSpan cellAndSpan = itemConfiguration.map.get(view);
        markCellsForView(cellAndSpan.x, cellAndSpan.y, cellAndSpan.spanX, cellAndSpan.spanY, this.mTmpOccupied, false);
        markCellsForRect(rect, this.mTmpOccupied, true);
        findNearestArea(cellAndSpan.x, cellAndSpan.y, cellAndSpan.spanX, cellAndSpan.spanY, iArr, this.mTmpOccupied, null, this.mTempLocation);
        boolean z = false;
        if (this.mTempLocation[0] >= 0) {
            z = false;
            if (this.mTempLocation[1] >= 0) {
                cellAndSpan.x = this.mTempLocation[0];
                cellAndSpan.y = this.mTempLocation[1];
                z = true;
            }
        }
        markCellsForView(cellAndSpan.x, cellAndSpan.y, cellAndSpan.spanX, cellAndSpan.spanY, this.mTmpOccupied, true);
        return z;
    }

    private boolean addViewsToTempLocation(ArrayList<View> arrayList, Rect rect, int[] iArr, View view, ItemConfiguration itemConfiguration) {
        if (arrayList.size() == 0) {
            return true;
        }
        Rect rect2 = null;
        for (View view2 : arrayList) {
            CellAndSpan cellAndSpan = itemConfiguration.map.get(view2);
            if (rect2 == null) {
                rect2 = new Rect(cellAndSpan.x, cellAndSpan.y, cellAndSpan.x + cellAndSpan.spanX, cellAndSpan.y + cellAndSpan.spanY);
            } else {
                rect2.union(cellAndSpan.x, cellAndSpan.y, cellAndSpan.x + cellAndSpan.spanX, cellAndSpan.y + cellAndSpan.spanY);
            }
        }
        for (View view3 : arrayList) {
            CellAndSpan cellAndSpan2 = itemConfiguration.map.get(view3);
            markCellsForView(cellAndSpan2.x, cellAndSpan2.y, cellAndSpan2.spanX, cellAndSpan2.spanY, this.mTmpOccupied, false);
        }
        boolean[][] zArr = new boolean[rect2.width()][rect2.height()];
        int i = rect2.top;
        int i2 = rect2.left;
        for (View view4 : arrayList) {
            CellAndSpan cellAndSpan3 = itemConfiguration.map.get(view4);
            markCellsForView(cellAndSpan3.x - i2, cellAndSpan3.y - i, cellAndSpan3.spanX, cellAndSpan3.spanY, zArr, true);
        }
        markCellsForRect(rect, this.mTmpOccupied, true);
        findNearestArea(rect2.left, rect2.top, rect2.width(), rect2.height(), iArr, this.mTmpOccupied, zArr, this.mTempLocation);
        boolean z = false;
        if (this.mTempLocation[0] >= 0) {
            z = false;
            if (this.mTempLocation[1] >= 0) {
                int i3 = this.mTempLocation[0];
                int i4 = rect2.left;
                int i5 = this.mTempLocation[1];
                int i6 = rect2.top;
                for (View view5 : arrayList) {
                    CellAndSpan cellAndSpan4 = itemConfiguration.map.get(view5);
                    cellAndSpan4.x += i3 - i4;
                    cellAndSpan4.y += i5 - i6;
                }
                z = true;
            }
        }
        for (View view6 : arrayList) {
            CellAndSpan cellAndSpan5 = itemConfiguration.map.get(view6);
            markCellsForView(cellAndSpan5.x, cellAndSpan5.y, cellAndSpan5.spanX, cellAndSpan5.spanY, this.mTmpOccupied, true);
        }
        return z;
    }

    private void animateItemsToSolution(ItemConfiguration itemConfiguration, View view, boolean z) {
        CellAndSpan cellAndSpan;
        boolean[][] zArr = this.mTmpOccupied;
        for (int i = 0; i < this.mCountX; i++) {
            for (int i2 = 0; i2 < this.mCountY; i2++) {
                zArr[i][i2] = false;
            }
        }
        int childCount = this.mShortcutsAndWidgets.getChildCount();
        for (int i3 = 0; i3 < childCount; i3++) {
            View childAt = this.mShortcutsAndWidgets.getChildAt(i3);
            if (childAt != view && (cellAndSpan = itemConfiguration.map.get(childAt)) != null) {
                animateChildToPosition(childAt, cellAndSpan.x, cellAndSpan.y, 150, 0, false, false);
                markCellsForView(cellAndSpan.x, cellAndSpan.y, cellAndSpan.spanX, cellAndSpan.spanY, zArr, true);
            }
        }
        if (z) {
            markCellsForView(itemConfiguration.dragViewX, itemConfiguration.dragViewY, itemConfiguration.dragViewSpanX, itemConfiguration.dragViewSpanY, zArr, true);
        }
    }

    private boolean attemptPushInDirection(ArrayList<View> arrayList, Rect rect, int[] iArr, View view, ItemConfiguration itemConfiguration) {
        if (Math.abs(iArr[0]) + Math.abs(iArr[1]) <= 1) {
            if (pushViewsToTempLocation(arrayList, rect, iArr, view, itemConfiguration)) {
                return true;
            }
            iArr[0] = iArr[0] * (-1);
            iArr[1] = iArr[1] * (-1);
            if (pushViewsToTempLocation(arrayList, rect, iArr, view, itemConfiguration)) {
                return true;
            }
            iArr[0] = iArr[0] * (-1);
            iArr[1] = iArr[1] * (-1);
            int i = iArr[1];
            iArr[1] = iArr[0];
            iArr[0] = i;
            if (pushViewsToTempLocation(arrayList, rect, iArr, view, itemConfiguration)) {
                return true;
            }
            iArr[0] = iArr[0] * (-1);
            iArr[1] = iArr[1] * (-1);
            if (pushViewsToTempLocation(arrayList, rect, iArr, view, itemConfiguration)) {
                return true;
            }
            iArr[0] = iArr[0] * (-1);
            iArr[1] = iArr[1] * (-1);
            int i2 = iArr[1];
            iArr[1] = iArr[0];
            iArr[0] = i2;
            return false;
        }
        int i3 = iArr[1];
        iArr[1] = 0;
        if (pushViewsToTempLocation(arrayList, rect, iArr, view, itemConfiguration)) {
            return true;
        }
        iArr[1] = i3;
        int i4 = iArr[0];
        iArr[0] = 0;
        if (pushViewsToTempLocation(arrayList, rect, iArr, view, itemConfiguration)) {
            return true;
        }
        iArr[0] = i4;
        iArr[0] = iArr[0] * (-1);
        iArr[1] = iArr[1] * (-1);
        int i5 = iArr[1];
        iArr[1] = 0;
        if (pushViewsToTempLocation(arrayList, rect, iArr, view, itemConfiguration)) {
            return true;
        }
        iArr[1] = i5;
        int i6 = iArr[0];
        iArr[0] = 0;
        if (pushViewsToTempLocation(arrayList, rect, iArr, view, itemConfiguration)) {
            return true;
        }
        iArr[0] = i6;
        iArr[0] = iArr[0] * (-1);
        iArr[1] = iArr[1] * (-1);
        return false;
    }

    private void beginOrAdjustReorderPreviewAnimations(ItemConfiguration itemConfiguration, View view, int i, int i2) {
        int childCount = this.mShortcutsAndWidgets.getChildCount();
        for (int i3 = 0; i3 < childCount; i3++) {
            View childAt = this.mShortcutsAndWidgets.getChildAt(i3);
            if (childAt != view) {
                CellAndSpan cellAndSpan = itemConfiguration.map.get(childAt);
                boolean z = (i2 != 0 || itemConfiguration.intersectingViews == null) ? false : !itemConfiguration.intersectingViews.contains(childAt);
                LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
                if (cellAndSpan != null && !z) {
                    new ReorderPreviewAnimation(this, childAt, i2, layoutParams.cellX, layoutParams.cellY, cellAndSpan.x, cellAndSpan.y, cellAndSpan.spanX, cellAndSpan.spanY).animate();
                }
            }
        }
    }

    private void clearOccupiedCells() {
        for (int i = 0; i < this.mCountX; i++) {
            for (int i2 = 0; i2 < this.mCountY; i2++) {
                this.mOccupied[i][i2] = false;
            }
        }
    }

    private void commitTempPlacement() {
        for (int i = 0; i < this.mCountX; i++) {
            for (int i2 = 0; i2 < this.mCountY; i2++) {
                this.mOccupied[i][i2] = this.mTmpOccupied[i][i2];
            }
        }
        int childCount = this.mShortcutsAndWidgets.getChildCount();
        for (int i3 = 0; i3 < childCount; i3++) {
            View childAt = this.mShortcutsAndWidgets.getChildAt(i3);
            LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
            ItemInfo itemInfo = (ItemInfo) childAt.getTag();
            if (itemInfo != null) {
                if (itemInfo.cellX != layoutParams.tmpCellX || itemInfo.cellY != layoutParams.tmpCellY || itemInfo.spanX != layoutParams.cellHSpan || itemInfo.spanY != layoutParams.cellVSpan) {
                    itemInfo.requiresDbUpdate = true;
                }
                int i4 = layoutParams.tmpCellX;
                layoutParams.cellX = i4;
                itemInfo.cellX = i4;
                int i5 = layoutParams.tmpCellY;
                layoutParams.cellY = i5;
                itemInfo.cellY = i5;
                itemInfo.spanX = layoutParams.cellHSpan;
                itemInfo.spanY = layoutParams.cellVSpan;
            }
        }
        this.mLauncher.getWorkspace().updateItemLocationsInDatabase(this);
    }

    private void completeAndClearReorderPreviewAnimations() {
        for (ReorderPreviewAnimation reorderPreviewAnimation : this.mShakeAnimators.values()) {
            reorderPreviewAnimation.completeAnimationImmediately();
        }
        this.mShakeAnimators.clear();
    }

    private void computeDirectionVector(float f, float f2, int[] iArr) {
        double atan = Math.atan(f2 / f);
        iArr[0] = 0;
        iArr[1] = 0;
        if (Math.abs(Math.cos(atan)) > 0.5d) {
            iArr[0] = (int) Math.signum(f);
        }
        if (Math.abs(Math.sin(atan)) > 0.5d) {
            iArr[1] = (int) Math.signum(f2);
        }
    }

    private void copyCurrentStateToSolution(ItemConfiguration itemConfiguration, boolean z) {
        int childCount = this.mShortcutsAndWidgets.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = this.mShortcutsAndWidgets.getChildAt(i);
            LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
            itemConfiguration.add(childAt, z ? new CellAndSpan(this, layoutParams.tmpCellX, layoutParams.tmpCellY, layoutParams.cellHSpan, layoutParams.cellVSpan) : new CellAndSpan(this, layoutParams.cellX, layoutParams.cellY, layoutParams.cellHSpan, layoutParams.cellVSpan));
        }
    }

    private void copyOccupiedArray(boolean[][] zArr) {
        for (int i = 0; i < this.mCountX; i++) {
            for (int i2 = 0; i2 < this.mCountY; i2++) {
                zArr[i][i2] = this.mOccupied[i][i2];
            }
        }
    }

    private void copySolutionToTempState(ItemConfiguration itemConfiguration, View view) {
        for (int i = 0; i < this.mCountX; i++) {
            for (int i2 = 0; i2 < this.mCountY; i2++) {
                this.mTmpOccupied[i][i2] = false;
            }
        }
        int childCount = this.mShortcutsAndWidgets.getChildCount();
        for (int i3 = 0; i3 < childCount; i3++) {
            View childAt = this.mShortcutsAndWidgets.getChildAt(i3);
            if (childAt != view) {
                LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
                CellAndSpan cellAndSpan = itemConfiguration.map.get(childAt);
                if (cellAndSpan != null) {
                    layoutParams.tmpCellX = cellAndSpan.x;
                    layoutParams.tmpCellY = cellAndSpan.y;
                    layoutParams.cellHSpan = cellAndSpan.spanX;
                    layoutParams.cellVSpan = cellAndSpan.spanY;
                    markCellsForView(cellAndSpan.x, cellAndSpan.y, cellAndSpan.spanX, cellAndSpan.spanY, this.mTmpOccupied, true);
                }
            }
        }
        markCellsForView(itemConfiguration.dragViewX, itemConfiguration.dragViewY, itemConfiguration.dragViewSpanX, itemConfiguration.dragViewSpanY, this.mTmpOccupied, true);
    }

    private ItemConfiguration findConfigurationNoShuffle(int i, int i2, int i3, int i4, int i5, int i6, View view, ItemConfiguration itemConfiguration) {
        int[] iArr = new int[2];
        int[] iArr2 = new int[2];
        findNearestVacantArea(i, i2, i3, i4, i5, i6, iArr, iArr2);
        if (iArr[0] < 0 || iArr[1] < 0) {
            itemConfiguration.isSolution = false;
        } else {
            copyCurrentStateToSolution(itemConfiguration, false);
            itemConfiguration.dragViewX = iArr[0];
            itemConfiguration.dragViewY = iArr[1];
            itemConfiguration.dragViewSpanX = iArr2[0];
            itemConfiguration.dragViewSpanY = iArr2[1];
            itemConfiguration.isSolution = true;
        }
        return itemConfiguration;
    }

    private int[] findNearestArea(int i, int i2, int i3, int i4, int i5, int i6, boolean z, int[] iArr, int[] iArr2) {
        int[] iArr3;
        boolean z2;
        boolean z3;
        boolean z4;
        int i7;
        int i8;
        lazyInitTempRectStack();
        int i9 = (int) (i - (((this.mCellWidth + this.mWidthGap) * (i5 - 1)) / 2.0f));
        int i10 = (int) (i2 - (((this.mCellHeight + this.mHeightGap) * (i6 - 1)) / 2.0f));
        if (iArr == null) {
            iArr = new int[2];
        }
        double d = Double.MAX_VALUE;
        Rect rect = new Rect(-1, -1, -1, -1);
        Stack<Rect> stack = new Stack<>();
        int i11 = this.mCountX;
        int i12 = this.mCountY;
        if (i3 <= 0 || i4 <= 0 || i5 <= 0 || i6 <= 0 || i5 < i3 || i6 < i4) {
            return iArr;
        }
        for (int i13 = 0; i13 < i12 - (i4 - 1); i13++) {
            for (int i14 = 0; i14 < i11 - (i3 - 1); i14++) {
                int i15 = -1;
                int i16 = -1;
                if (z) {
                    for (int i17 = 0; i17 < i3; i17++) {
                        for (int i18 = 0; i18 < i4; i18++) {
                            if (this.mOccupied[i14 + i17][i13 + i18]) {
                                break;
                            }
                        }
                    }
                    i16 = i3;
                    i15 = i4;
                    boolean z5 = true;
                    boolean z6 = i3 >= i5;
                    boolean z7 = i4 >= i6;
                    while (true) {
                        if (z6 ? z7 : false) {
                            break;
                        }
                        if (!z5 || z6) {
                            z3 = z6;
                            z4 = z7;
                            i7 = i16;
                            i8 = i15;
                            if (!z7) {
                                for (int i19 = 0; i19 < i16; i19++) {
                                    if (i13 + i15 > i12 - 1 || this.mOccupied[i14 + i19][i13 + i15]) {
                                        z7 = true;
                                    }
                                }
                                z3 = z6;
                                z4 = z7;
                                i7 = i16;
                                i8 = i15;
                                if (!z7) {
                                    i8 = i15 + 1;
                                    z3 = z6;
                                    z4 = z7;
                                    i7 = i16;
                                }
                            }
                        } else {
                            for (int i20 = 0; i20 < i15; i20++) {
                                if (i14 + i16 > i11 - 1 || this.mOccupied[i14 + i16][i13 + i20]) {
                                    z6 = true;
                                }
                            }
                            z3 = z6;
                            z4 = z7;
                            i7 = i16;
                            i8 = i15;
                            if (!z6) {
                                i7 = i16 + 1;
                                i8 = i15;
                                z4 = z7;
                                z3 = z6;
                            }
                        }
                        z6 = z3 | (i7 >= i5);
                        z7 = z4 | (i8 >= i6);
                        if (z5) {
                            z5 = false;
                            i16 = i7;
                            i15 = i8;
                        } else {
                            z5 = true;
                            i16 = i7;
                            i15 = i8;
                        }
                    }
                    if (i16 >= i5) {
                    }
                    if (i15 >= i6) {
                    }
                }
                cellToCenterPoint(i14, i13, this.mTmpPoint);
                Rect pop = this.mTempRectStack.pop();
                pop.set(i14, i13, i14 + i16, i13 + i15);
                Iterator<T> it = stack.iterator();
                while (true) {
                    z2 = false;
                    if (it.hasNext()) {
                        if (((Rect) it.next()).contains(pop)) {
                            z2 = true;
                            break;
                        }
                    } else {
                        break;
                    }
                }
                stack.push(pop);
                double hypot = Math.hypot(iArr3[0] - i9, iArr3[1] - i10);
                if ((hypot <= d && !z2) || pop.contains(rect)) {
                    d = hypot;
                    iArr[0] = i14;
                    iArr[1] = i13;
                    if (iArr2 != null) {
                        iArr2[0] = i16;
                        iArr2[1] = i15;
                    }
                    rect.set(pop);
                }
            }
        }
        if (d == Double.MAX_VALUE) {
            iArr[0] = -1;
            iArr[1] = -1;
        }
        recycleTempRects(stack);
        return iArr;
    }

    private int[] findNearestArea(int i, int i2, int i3, int i4, int[] iArr, boolean[][] zArr, boolean[][] zArr2, int[] iArr2) {
        float f;
        int i5;
        if (iArr2 == null) {
            iArr2 = new int[2];
        }
        float f2 = Float.MAX_VALUE;
        int i6 = Integer.MIN_VALUE;
        int i7 = this.mCountX;
        int i8 = this.mCountY;
        for (int i9 = 0; i9 < i8 - (i4 - 1); i9++) {
            int i10 = 0;
            while (i10 < i7 - (i3 - 1)) {
                int i11 = 0;
                while (true) {
                    if (i11 < i3) {
                        for (int i12 = 0; i12 < i4; i12++) {
                            if (zArr[i10 + i11][i9 + i12]) {
                                i5 = i6;
                                f = f2;
                                if (zArr2 == null) {
                                    break;
                                } else if (zArr2[i11][i12]) {
                                    f = f2;
                                    i5 = i6;
                                    break;
                                }
                            }
                        }
                        i11++;
                    } else {
                        float hypot = (float) Math.hypot(i10 - i, i9 - i2);
                        int[] iArr3 = this.mTmpPoint;
                        computeDirectionVector(i10 - i, i9 - i2, iArr3);
                        int i13 = (iArr[0] * iArr3[0]) + (iArr[1] * iArr3[1]);
                        if ((!(iArr[0] == iArr3[0] ? iArr[0] == iArr3[0] : false) && 0 != 0) || Float.compare(hypot, f2) >= 0) {
                            i5 = i6;
                            f = f2;
                            if (Float.compare(hypot, f2) == 0) {
                                i5 = i6;
                                f = f2;
                                if (i13 <= i6) {
                                }
                            }
                        }
                        f = hypot;
                        i5 = i13;
                        iArr2[0] = i10;
                        iArr2[1] = i9;
                    }
                }
                i10++;
                i6 = i5;
                f2 = f;
            }
        }
        if (f2 == Float.MAX_VALUE) {
            iArr2[0] = -1;
            iArr2[1] = -1;
        }
        return iArr2;
    }

    private ItemConfiguration findReorderSolution(int i, int i2, int i3, int i4, int i5, int i6, int[] iArr, View view, boolean z, ItemConfiguration itemConfiguration) {
        copyCurrentStateToSolution(itemConfiguration, false);
        copyOccupiedArray(this.mTmpOccupied);
        int[] findNearestArea = findNearestArea(i, i2, i5, i6, new int[2]);
        if (rearrangementExists(findNearestArea[0], findNearestArea[1], i5, i6, iArr, view, itemConfiguration)) {
            itemConfiguration.isSolution = true;
            itemConfiguration.dragViewX = findNearestArea[0];
            itemConfiguration.dragViewY = findNearestArea[1];
            itemConfiguration.dragViewSpanX = i5;
            itemConfiguration.dragViewSpanY = i6;
        } else if (i5 > i3 && (i4 == i6 || z)) {
            return findReorderSolution(i, i2, i3, i4, i5 - 1, i6, iArr, view, false, itemConfiguration);
        } else {
            if (i6 > i4) {
                return findReorderSolution(i, i2, i3, i4, i5, i6 - 1, iArr, view, true, itemConfiguration);
            }
            itemConfiguration.isSolution = false;
        }
        return itemConfiguration;
    }

    private void getDirectionVectorForDrop(int i, int i2, int i3, int i4, View view, int[] iArr) {
        int[] iArr2 = new int[2];
        findNearestArea(i, i2, i3, i4, iArr2);
        Rect rect = new Rect();
        regionToRect(iArr2[0], iArr2[1], i3, i4, rect);
        rect.offset(i - rect.centerX(), i2 - rect.centerY());
        Rect rect2 = new Rect();
        getViewsIntersectingRegion(iArr2[0], iArr2[1], i3, i4, view, rect2, this.mIntersectingViews);
        int width = rect2.width();
        int height = rect2.height();
        regionToRect(rect2.left, rect2.top, rect2.width(), rect2.height(), rect2);
        int centerX = (rect2.centerX() - i) / i3;
        int centerY = (rect2.centerY() - i2) / i4;
        if (width == this.mCountX || i3 == this.mCountX) {
            centerX = 0;
        }
        if (height == this.mCountY || i4 == this.mCountY) {
            centerY = 0;
        }
        if (centerX != 0 || centerY != 0) {
            computeDirectionVector(centerX, centerY, iArr);
            return;
        }
        iArr[0] = 1;
        iArr[1] = 0;
    }

    private ParcelableSparseArray getJailedArray(SparseArray<Parcelable> sparseArray) {
        Parcelable parcelable = sparseArray.get(2131296259);
        return parcelable instanceof ParcelableSparseArray ? (ParcelableSparseArray) parcelable : new ParcelableSparseArray();
    }

    private void getViewsIntersectingRegion(int i, int i2, int i3, int i4, View view, Rect rect, ArrayList<View> arrayList) {
        if (rect != null) {
            rect.set(i, i2, i + i3, i2 + i4);
        }
        arrayList.clear();
        Rect rect2 = new Rect(i, i2, i + i3, i2 + i4);
        Rect rect3 = new Rect();
        int childCount = this.mShortcutsAndWidgets.getChildCount();
        for (int i5 = 0; i5 < childCount; i5++) {
            View childAt = this.mShortcutsAndWidgets.getChildAt(i5);
            if (childAt != view) {
                LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
                rect3.set(layoutParams.cellX, layoutParams.cellY, layoutParams.cellX + layoutParams.cellHSpan, layoutParams.cellY + layoutParams.cellVSpan);
                if (Rect.intersects(rect2, rect3)) {
                    this.mIntersectingViews.add(childAt);
                    if (rect != null) {
                        rect.union(rect3);
                    }
                }
            }
        }
    }

    private void lazyInitTempRectStack() {
        if (this.mTempRectStack.isEmpty()) {
            for (int i = 0; i < this.mCountX * this.mCountY; i++) {
                this.mTempRectStack.push(new Rect());
            }
        }
    }

    private void markCellsForRect(Rect rect, boolean[][] zArr, boolean z) {
        markCellsForView(rect.left, rect.top, rect.width(), rect.height(), zArr, z);
    }

    private void markCellsForView(int i, int i2, int i3, int i4, boolean[][] zArr, boolean z) {
        if (i < 0 || i2 < 0) {
            return;
        }
        for (int i5 = i; i5 < i + i3 && i5 < this.mCountX; i5++) {
            for (int i6 = i2; i6 < i2 + i4 && i6 < this.mCountY; i6++) {
                zArr[i5][i6] = z;
            }
        }
    }

    private boolean pushViewsToTempLocation(ArrayList<View> arrayList, Rect rect, int[] iArr, View view, ItemConfiguration itemConfiguration) {
        int i;
        int i2;
        boolean z;
        ViewCluster viewCluster = new ViewCluster(this, arrayList, itemConfiguration);
        Rect boundingRect = viewCluster.getBoundingRect();
        boolean z2 = false;
        if (iArr[0] < 0) {
            i = 0;
            i2 = boundingRect.right - rect.left;
        } else if (iArr[0] > 0) {
            i = 2;
            i2 = rect.right - boundingRect.left;
        } else if (iArr[1] < 0) {
            i = 1;
            i2 = boundingRect.bottom - rect.top;
        } else {
            i = 3;
            i2 = rect.bottom - boundingRect.top;
        }
        if (i2 <= 0) {
            return false;
        }
        for (View view2 : arrayList) {
            CellAndSpan cellAndSpan = itemConfiguration.map.get(view2);
            markCellsForView(cellAndSpan.x, cellAndSpan.y, cellAndSpan.spanX, cellAndSpan.spanY, this.mTmpOccupied, false);
        }
        itemConfiguration.save();
        viewCluster.sortConfigurationForEdgePush(i);
        while (i2 > 0 && !z2) {
            Iterator<T> it = itemConfiguration.sortedViews.iterator();
            while (true) {
                z = z2;
                if (it.hasNext()) {
                    View view3 = (View) it.next();
                    if (!viewCluster.views.contains(view3) && view3 != view && viewCluster.isViewTouchingEdge(view3, i)) {
                        if (!((LayoutParams) view3.getLayoutParams()).canReorder) {
                            z = true;
                            break;
                        }
                        viewCluster.addView(view3);
                        CellAndSpan cellAndSpan2 = itemConfiguration.map.get(view3);
                        markCellsForView(cellAndSpan2.x, cellAndSpan2.y, cellAndSpan2.spanX, cellAndSpan2.spanY, this.mTmpOccupied, false);
                    }
                }
            }
            i2--;
            viewCluster.shift(i, 1);
            z2 = z;
        }
        boolean z3 = false;
        Rect boundingRect2 = viewCluster.getBoundingRect();
        if (z2 || boundingRect2.left < 0 || boundingRect2.right > this.mCountX || boundingRect2.top < 0 || boundingRect2.bottom > this.mCountY) {
            itemConfiguration.restore();
        } else {
            z3 = true;
        }
        for (View view4 : viewCluster.views) {
            CellAndSpan cellAndSpan3 = itemConfiguration.map.get(view4);
            markCellsForView(cellAndSpan3.x, cellAndSpan3.y, cellAndSpan3.spanX, cellAndSpan3.spanY, this.mTmpOccupied, true);
        }
        return z3;
    }

    private boolean rearrangementExists(int i, int i2, int i3, int i4, int[] iArr, View view, ItemConfiguration itemConfiguration) {
        CellAndSpan cellAndSpan;
        if (i < 0 || i2 < 0) {
            return false;
        }
        this.mIntersectingViews.clear();
        this.mOccupiedRect.set(i, i2, i + i3, i2 + i4);
        if (view != null && (cellAndSpan = itemConfiguration.map.get(view)) != null) {
            cellAndSpan.x = i;
            cellAndSpan.y = i2;
        }
        Rect rect = new Rect(i, i2, i + i3, i2 + i4);
        Rect rect2 = new Rect();
        for (View view2 : itemConfiguration.map.keySet()) {
            if (view2 != view) {
                CellAndSpan cellAndSpan2 = itemConfiguration.map.get(view2);
                LayoutParams layoutParams = (LayoutParams) view2.getLayoutParams();
                rect2.set(cellAndSpan2.x, cellAndSpan2.y, cellAndSpan2.x + cellAndSpan2.spanX, cellAndSpan2.y + cellAndSpan2.spanY);
                if (!Rect.intersects(rect, rect2)) {
                    continue;
                } else if (!layoutParams.canReorder) {
                    return false;
                } else {
                    this.mIntersectingViews.add(view2);
                }
            }
        }
        itemConfiguration.intersectingViews = new ArrayList<>(this.mIntersectingViews);
        if (attemptPushInDirection(this.mIntersectingViews, this.mOccupiedRect, iArr, view, itemConfiguration) || addViewsToTempLocation(this.mIntersectingViews, this.mOccupiedRect, iArr, view, itemConfiguration)) {
            return true;
        }
        for (View view3 : this.mIntersectingViews) {
            if (!addViewToTempLocation(view3, this.mOccupiedRect, iArr, itemConfiguration)) {
                return false;
            }
        }
        return true;
    }

    private void recycleTempRects(Stack<Rect> stack) {
        while (!stack.isEmpty()) {
            this.mTempRectStack.push(stack.pop());
        }
    }

    private void setUseTempCoords(boolean z) {
        int childCount = this.mShortcutsAndWidgets.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ((LayoutParams) this.mShortcutsAndWidgets.getChildAt(i).getLayoutParams()).useTmpCoords = z;
        }
    }

    public boolean addViewToCellLayout(View view, int i, int i2, LayoutParams layoutParams, boolean z) {
        if (view instanceof BubbleTextView) {
            ((BubbleTextView) view).setTextVisibility(!this.mIsHotseat);
        }
        view.setScaleX(getChildrenScale());
        view.setScaleY(getChildrenScale());
        if (layoutParams.cellX < 0 || layoutParams.cellX > this.mCountX - 1 || layoutParams.cellY < 0 || layoutParams.cellY > this.mCountY - 1) {
            return false;
        }
        if (layoutParams.cellHSpan < 0) {
            layoutParams.cellHSpan = this.mCountX;
        }
        if (layoutParams.cellVSpan < 0) {
            layoutParams.cellVSpan = this.mCountY;
        }
        view.setId(i2);
        try {
            this.mShortcutsAndWidgets.addView(view, i, layoutParams);
        } catch (IllegalStateException e) {
        }
        if (z) {
            markCellsAsOccupiedForView(view);
            return true;
        }
        return true;
    }

    public boolean animateChildToPosition(View view, int i, int i2, int i3, int i4, boolean z, boolean z2) {
        ShortcutAndWidgetContainer shortcutsAndWidgets = getShortcutsAndWidgets();
        boolean[][] zArr = this.mOccupied;
        if (!z) {
            zArr = this.mTmpOccupied;
        }
        if (shortcutsAndWidgets.indexOfChild(view) != -1) {
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            ItemInfo itemInfo = (ItemInfo) view.getTag();
            if (this.mReorderAnimators.containsKey(layoutParams)) {
                this.mReorderAnimators.get(layoutParams).cancel();
                this.mReorderAnimators.remove(layoutParams);
            }
            int i5 = layoutParams.x;
            int i6 = layoutParams.y;
            if (z2) {
                zArr[layoutParams.cellX][layoutParams.cellY] = false;
                zArr[i][i2] = true;
            }
            layoutParams.isLockedToGrid = true;
            if (z) {
                itemInfo.cellX = i;
                layoutParams.cellX = i;
                itemInfo.cellY = i2;
                layoutParams.cellY = i2;
            } else {
                layoutParams.tmpCellX = i;
                layoutParams.tmpCellY = i2;
            }
            shortcutsAndWidgets.setupLp(layoutParams);
            layoutParams.isLockedToGrid = false;
            int i7 = layoutParams.x;
            int i8 = layoutParams.y;
            layoutParams.x = i5;
            layoutParams.y = i6;
            if (i5 == i7 && i6 == i8) {
                layoutParams.isLockedToGrid = true;
                return true;
            }
            ValueAnimator ofFloat = LauncherAnimUtils.ofFloat(view, 0.0f, 1.0f);
            ofFloat.setDuration(i3);
            this.mReorderAnimators.put(layoutParams, ofFloat);
            ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this, layoutParams, i5, i7, i6, i8, view) { // from class: com.android.launcher3.CellLayout.3
                final CellLayout this$0;
                final View val$child;
                final LayoutParams val$lp;
                final int val$newX;
                final int val$newY;
                final int val$oldX;
                final int val$oldY;

                {
                    this.this$0 = this;
                    this.val$lp = layoutParams;
                    this.val$oldX = i5;
                    this.val$newX = i7;
                    this.val$oldY = i6;
                    this.val$newY = i8;
                    this.val$child = view;
                }

                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                    this.val$lp.x = (int) (((1.0f - floatValue) * this.val$oldX) + (this.val$newX * floatValue));
                    this.val$lp.y = (int) (((1.0f - floatValue) * this.val$oldY) + (this.val$newY * floatValue));
                    this.val$child.requestLayout();
                }
            });
            ofFloat.addListener(new AnimatorListenerAdapter(this, layoutParams, view) { // from class: com.android.launcher3.CellLayout.4
                boolean cancelled = false;
                final CellLayout this$0;
                final View val$child;
                final LayoutParams val$lp;

                {
                    this.this$0 = this;
                    this.val$lp = layoutParams;
                    this.val$child = view;
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationCancel(Animator animator) {
                    this.cancelled = true;
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    if (!this.cancelled) {
                        this.val$lp.isLockedToGrid = true;
                        this.val$child.requestLayout();
                    }
                    if (this.this$0.mReorderAnimators.containsKey(this.val$lp)) {
                        this.this$0.mReorderAnimators.remove(this.val$lp);
                    }
                }
            });
            ofFloat.setStartDelay(i4);
            ofFloat.start();
            return true;
        }
        return false;
    }

    public void buildHardwareLayer() {
        this.mShortcutsAndWidgets.buildLayer();
    }

    @Override // android.view.View
    public void cancelLongPress() {
        super.cancelLongPress();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            getChildAt(i).cancelLongPress();
        }
    }

    void cellToCenterPoint(int i, int i2, int[] iArr) {
        regionToCenterPoint(i, i2, 1, 1, iArr);
    }

    void cellToPoint(int i, int i2, int[] iArr) {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        iArr[0] = ((this.mCellWidth + this.mWidthGap) * i) + paddingLeft;
        iArr[1] = ((this.mCellHeight + this.mHeightGap) * i2) + paddingTop;
    }

    public void cellToRect(int i, int i2, int i3, int i4, Rect rect) {
        int i5 = this.mCellWidth;
        int i6 = this.mCellHeight;
        int i7 = this.mWidthGap;
        int i8 = this.mHeightGap;
        int paddingLeft = getPaddingLeft() + ((i5 + i7) * i);
        int paddingTop = getPaddingTop() + ((i6 + i8) * i2);
        rect.set(paddingLeft, paddingTop, paddingLeft + (i3 * i5) + ((i3 - 1) * i7), paddingTop + (i4 * i6) + ((i4 - 1) * i8));
    }

    @Override // android.view.ViewGroup
    protected boolean checkLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return layoutParams instanceof LayoutParams;
    }

    public void clear() {
        int length = this.mDragOutlineAnims.length;
        for (int i = 0; i < length; i++) {
            this.mDragOutlineAnims[i].getAnimator().removeAllListeners();
            this.mDragOutlineAnims[i].getAnimator().removeAllUpdateListeners();
        }
    }

    public void clearDragOutlines() {
        this.mDragOutlineAnims[this.mDragOutlineCurrent].animateOut();
        int[] iArr = this.mDragCell;
        this.mDragCell[1] = -1;
        iArr[0] = -1;
    }

    public void clearFolderLeaveBehind() {
        this.mFolderLeaveBehindCell[0] = -1;
        this.mFolderLeaveBehindCell[1] = -1;
        invalidate();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean createAreaForResize(int i, int i2, int i3, int i4, View view, int[] iArr, boolean z) {
        int[] iArr2 = new int[2];
        regionToCenterPoint(i, i2, i3, i4, iArr2);
        ItemConfiguration findReorderSolution = findReorderSolution(iArr2[0], iArr2[1], i3, i4, i3, i4, iArr, view, true, new ItemConfiguration(this));
        setUseTempCoords(true);
        if (findReorderSolution != null && findReorderSolution.isSolution) {
            copySolutionToTempState(findReorderSolution, view);
            setItemPlacementDirty(true);
            animateItemsToSolution(findReorderSolution, view, z);
            if (z) {
                commitTempPlacement();
                completeAndClearReorderPreviewAnimations();
                setItemPlacementDirty(false);
            } else {
                beginOrAdjustReorderPreviewAnimations(findReorderSolution, view, 150, 1);
            }
            this.mShortcutsAndWidgets.requestLayout();
        }
        return findReorderSolution.isSolution;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void disableDragTarget() {
        this.mIsDragTarget = false;
    }

    public void disableJailContent() {
        this.mJailContent = false;
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchHoverEvent(MotionEvent motionEvent) {
        if (this.mUseTouchHelper && this.mTouchHelper.dispatchHoverEvent(motionEvent)) {
            return true;
        }
        return super.dispatchHoverEvent(motionEvent);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> sparseArray) {
        ParcelableSparseArray parcelableSparseArray = sparseArray;
        if (this.mJailContent) {
            parcelableSparseArray = getJailedArray(sparseArray);
        }
        super.dispatchRestoreInstanceState(parcelableSparseArray);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> sparseArray) {
        if (!this.mJailContent) {
            super.dispatchSaveInstanceState(sparseArray);
            return;
        }
        ParcelableSparseArray jailedArray = getJailedArray(sparseArray);
        super.dispatchSaveInstanceState(jailedArray);
        sparseArray.put(2131296259, jailedArray);
    }

    @TargetApi(21)
    public void enableAccessibleDrag(boolean z, int i) {
        this.mUseTouchHelper = z;
        if (z) {
            if (i == 2 && !(this.mTouchHelper instanceof WorkspaceAccessibilityHelper)) {
                this.mTouchHelper = new WorkspaceAccessibilityHelper(this);
            } else if (i == 1 && !(this.mTouchHelper instanceof FolderAccessibilityHelper)) {
                this.mTouchHelper = new FolderAccessibilityHelper(this);
            }
            ViewCompat.setAccessibilityDelegate(this, this.mTouchHelper);
            setImportantForAccessibility(1);
            getShortcutsAndWidgets().setImportantForAccessibility(1);
            setOnClickListener(this.mTouchHelper);
        } else {
            ViewCompat.setAccessibilityDelegate(this, null);
            setImportantForAccessibility(2);
            getShortcutsAndWidgets().setImportantForAccessibility(2);
            setOnClickListener(this.mLauncher);
        }
        if (getParent() != null) {
            getParent().notifySubtreeAccessibilityStateChanged(this, this, 1);
        }
    }

    public void enableHardwareLayer(boolean z) {
        this.mShortcutsAndWidgets.setLayerType(z ? 2 : 0, sPaint);
    }

    public boolean findCellForSpan(int[] iArr, int i, int i2) {
        boolean z;
        int i3;
        boolean z2 = false;
        int i4 = this.mCountX;
        int i5 = this.mCountY;
        int i6 = 0;
        while (i6 < i5 - (i2 - 1) && !z2) {
            int i7 = 0;
            while (true) {
                int i8 = i7;
                z = z2;
                if (i8 < i4 - (i - 1)) {
                    i3 = 0;
                    while (i3 < i) {
                        for (int i9 = 0; i9 < i2; i9++) {
                            if (this.mOccupied[i8 + i3][i6 + i9]) {
                                break;
                            }
                        }
                        i3++;
                    }
                    if (iArr != null) {
                        iArr[0] = i8;
                        iArr[1] = i6;
                    }
                    z = true;
                }
                i7 = i8 + i3 + 1;
            }
            i6++;
            z2 = z;
        }
        return z2;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int[] findNearestArea(int i, int i2, int i3, int i4, int[] iArr) {
        return findNearestArea(i, i2, i3, i4, i3, i4, false, iArr, null);
    }

    int[] findNearestVacantArea(int i, int i2, int i3, int i4, int i5, int i6, int[] iArr, int[] iArr2) {
        return findNearestArea(i, i2, i3, i4, i5, i6, true, iArr, iArr2);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int[] findNearestVacantArea(int i, int i2, int i3, int i4, int[] iArr) {
        return findNearestVacantArea(i, i2, i3, i4, i3, i4, iArr, null);
    }

    @Override // android.view.ViewGroup
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        return new LayoutParams(getContext(), attributeSet);
    }

    @Override // android.view.ViewGroup
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return new LayoutParams(layoutParams);
    }

    public float getBackgroundAlpha() {
        return this.mBackgroundAlpha;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getCellHeight() {
        return this.mCellHeight;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getCellWidth() {
        return this.mCellWidth;
    }

    public View getChildAt(int i, int i2) {
        return this.mShortcutsAndWidgets.getChildAt(i, i2);
    }

    public float getChildrenScale() {
        return this.mIsHotseat ? this.mHotseatScale : 1.0f;
    }

    public int getCountX() {
        return this.mCountX;
    }

    public int getCountY() {
        return this.mCountY;
    }

    public int getDesiredHeight() {
        return getPaddingTop() + getPaddingBottom() + (this.mCountY * this.mCellHeight) + (Math.max(this.mCountY - 1, 0) * this.mHeightGap);
    }

    public int getDesiredWidth() {
        return getPaddingLeft() + getPaddingRight() + (this.mCountX * this.mCellWidth) + (Math.max(this.mCountX - 1, 0) * this.mWidthGap);
    }

    public float getDistanceFromCell(float f, float f2, int[] iArr) {
        cellToCenterPoint(iArr[0], iArr[1], this.mTmpPoint);
        return (float) Math.hypot(f - this.mTmpPoint[0], f2 - this.mTmpPoint[1]);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getHeightGap() {
        return this.mHeightGap;
    }

    public boolean getIsDragOverlapping() {
        return this.mIsDragOverlapping;
    }

    public ShortcutAndWidgetContainer getShortcutsAndWidgets() {
        return this.mShortcutsAndWidgets;
    }

    public int getUnusedHorizontalSpace() {
        return ((getMeasuredWidth() - getPaddingLeft()) - getPaddingRight()) - (this.mCountX * this.mCellWidth);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getWidthGap() {
        return this.mWidthGap;
    }

    public void hideFolderAccept(FolderIcon.FolderRingAnimator folderRingAnimator) {
        if (this.mFolderOuterRings.contains(folderRingAnimator)) {
            this.mFolderOuterRings.remove(folderRingAnimator);
        }
        invalidate();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isDragTarget() {
        return this.mIsDragTarget;
    }

    public boolean isDropPending() {
        return this.mDropPending;
    }

    public boolean isHotseat() {
        return this.mIsHotseat;
    }

    boolean isItemPlacementDirty() {
        return this.mItemPlacementDirty;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isNearestDropLocationOccupied(int i, int i2, int i3, int i4, View view, int[] iArr) {
        int[] findNearestArea = findNearestArea(i, i2, i3, i4, iArr);
        getViewsIntersectingRegion(findNearestArea[0], findNearestArea[1], i3, i4, view, null, this.mIntersectingViews);
        return !this.mIntersectingViews.isEmpty();
    }

    public boolean isOccupied(int i, int i2) {
        if (i == -1 || i2 == -1) {
            Log.d("CellLayout", "x = " + i + ",y = " + i2);
            return true;
        } else if (i >= this.mCountX || i2 >= this.mCountY) {
            throw new RuntimeException("Position exceeds the bound of this CellLayout");
        } else {
            return this.mOccupied[i][i2];
        }
    }

    public boolean isRegionVacant(int i, int i2, int i3, int i4) {
        int i5 = (i + i3) - 1;
        int i6 = (i2 + i4) - 1;
        if (i < 0 || i2 < 0 || i5 >= this.mCountX || i6 >= this.mCountY) {
            return false;
        }
        while (i <= i5) {
            for (int i7 = i2; i7 <= i6; i7++) {
                if (this.mOccupied[i][i7]) {
                    return false;
                }
            }
            i++;
        }
        return true;
    }

    public void markCellsAsOccupiedForView(View view) {
        if (view == null || view.getParent() != this.mShortcutsAndWidgets) {
            return;
        }
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        markCellsForView(layoutParams.cellX, layoutParams.cellY, layoutParams.cellHSpan, layoutParams.cellVSpan, this.mOccupied, true);
    }

    public void markCellsAsUnoccupiedForView(View view) {
        if (view == null || view.getParent() != this.mShortcutsAndWidgets) {
            return;
        }
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        markCellsForView(layoutParams.cellX, layoutParams.cellY, layoutParams.cellHSpan, layoutParams.cellVSpan, this.mOccupied, false);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onDragEnter() {
        if (LauncherLog.DEBUG_DRAG) {
            LauncherLog.d("CellLayout", "onDragEnter: mDragging = " + this.mDragging);
        }
        this.mDragging = true;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onDragExit() {
        if (LauncherLog.DEBUG_DRAG) {
            LauncherLog.d("CellLayout", "onDragExit: mDragging = " + this.mDragging);
        }
        if (this.mDragging) {
            this.mDragging = false;
        }
        int[] iArr = this.mDragCell;
        this.mDragCell[1] = -1;
        iArr[0] = -1;
        this.mDragOutlineAnims[this.mDragOutlineCurrent].animateOut();
        this.mDragOutlineCurrent = (this.mDragOutlineCurrent + 1) % this.mDragOutlineAnims.length;
        revertTempState();
        setIsDragOverlapping(false);
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        if (this.mIsDragTarget) {
            if (this.mBackgroundAlpha > 0.0f) {
                this.mBackground.draw(canvas);
            }
            Paint paint = this.mDragOutlinePaint;
            for (int i = 0; i < this.mDragOutlines.length; i++) {
                float f = this.mDragOutlineAlphas[i];
                if (f > 0.0f) {
                    this.mTempRect.set(this.mDragOutlines[i]);
                    Utilities.scaleRectAboutCenter(this.mTempRect, getChildrenScale());
                    paint.setAlpha((int) (0.5f + f));
                    canvas.drawBitmap((Bitmap) this.mDragOutlineAnims[i].getTag(), (Rect) null, this.mTempRect, paint);
                }
            }
            int i2 = FolderIcon.FolderRingAnimator.sPreviewSize;
            DeviceProfile deviceProfile = this.mLauncher.getDeviceProfile();
            for (int i3 = 0; i3 < this.mFolderOuterRings.size(); i3++) {
                FolderIcon.FolderRingAnimator folderRingAnimator = this.mFolderOuterRings.get(i3);
                cellToPoint(folderRingAnimator.mCellX, folderRingAnimator.mCellY, this.mTempLocation);
                View childAt = getChildAt(folderRingAnimator.mCellX, folderRingAnimator.mCellY);
                if (childAt != null) {
                    int i4 = this.mTempLocation[0] + (this.mCellWidth / 2);
                    int paddingTop = this.mTempLocation[1] + (i2 / 2) + childAt.getPaddingTop() + deviceProfile.folderBackgroundOffset;
                    Drawable drawable = FolderIcon.FolderRingAnimator.sSharedOuterRingDrawable;
                    int outerRingSize = (int) (folderRingAnimator.getOuterRingSize() * getChildrenScale());
                    canvas.save();
                    canvas.translate(i4 - (outerRingSize / 2), paddingTop - (outerRingSize / 2));
                    drawable.setBounds(0, 0, outerRingSize, outerRingSize);
                    drawable.draw(canvas);
                    canvas.restore();
                    Drawable drawable2 = FolderIcon.FolderRingAnimator.sSharedInnerRingDrawable;
                    int innerRingSize = (int) (folderRingAnimator.getInnerRingSize() * getChildrenScale());
                    canvas.save();
                    canvas.translate(i4 - (innerRingSize / 2), paddingTop - (innerRingSize / 2));
                    drawable2.setBounds(0, 0, innerRingSize, innerRingSize);
                    drawable2.draw(canvas);
                    canvas.restore();
                }
            }
            if (this.mFolderLeaveBehindCell[0] < 0 || this.mFolderLeaveBehindCell[1] < 0) {
                return;
            }
            Drawable drawable3 = FolderIcon.sSharedFolderLeaveBehind;
            int intrinsicWidth = drawable3.getIntrinsicWidth();
            int intrinsicHeight = drawable3.getIntrinsicHeight();
            cellToPoint(this.mFolderLeaveBehindCell[0], this.mFolderLeaveBehindCell[1], this.mTempLocation);
            View childAt2 = getChildAt(this.mFolderLeaveBehindCell[0], this.mFolderLeaveBehindCell[1]);
            if (childAt2 != null) {
                int i5 = this.mTempLocation[0];
                int i6 = this.mCellWidth / 2;
                int i7 = this.mTempLocation[1];
                int i8 = i2 / 2;
                int paddingTop2 = childAt2.getPaddingTop();
                int i9 = deviceProfile.folderBackgroundOffset;
                canvas.save();
                canvas.translate((i5 + i6) - (intrinsicWidth / 2), (((i7 + i8) + paddingTop2) + i9) - (intrinsicWidth / 2));
                drawable3.setBounds(0, 0, intrinsicWidth, intrinsicHeight);
                drawable3.draw(canvas);
                canvas.restore();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onDropChild(View view) {
        if (LauncherLog.DEBUG_DRAG) {
            LauncherLog.d("CellLayout", "onDropChild: child = " + view);
        }
        if (view != null) {
            ((LayoutParams) view.getLayoutParams()).dropped = true;
            view.requestLayout();
            markCellsAsOccupiedForView(view);
        }
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        if (LauncherLog.DEBUG_MOTION) {
            LauncherLog.d("CellLayout", "onInterceptTouchEvent: action = " + action + ", mInterceptTouchListener = " + this.mInterceptTouchListener);
        }
        if (this.mUseTouchHelper) {
            return true;
        }
        return this.mInterceptTouchListener != null && this.mInterceptTouchListener.onTouch(this, motionEvent);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        boolean z2 = this.mShortcutsAndWidgets.getChildCount() > 0 ? ((LayoutParams) this.mShortcutsAndWidgets.getChildAt(0).getLayoutParams()).isFullscreen : false;
        int paddingLeft = getPaddingLeft();
        int i5 = paddingLeft;
        if (!z2) {
            i5 = paddingLeft + ((int) Math.ceil(getUnusedHorizontalSpace() / 2.0f));
        }
        int paddingTop = getPaddingTop();
        this.mTouchFeedbackView.layout(i5, paddingTop, this.mTouchFeedbackView.getMeasuredWidth() + i5, this.mTouchFeedbackView.getMeasuredHeight() + paddingTop);
        this.mShortcutsAndWidgets.layout(i5, paddingTop, (i5 + i3) - i, (paddingTop + i4) - i2);
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        int mode = View.MeasureSpec.getMode(i);
        int mode2 = View.MeasureSpec.getMode(i2);
        int size = View.MeasureSpec.getSize(i);
        int size2 = View.MeasureSpec.getSize(i2);
        int paddingLeft = size - (getPaddingLeft() + getPaddingRight());
        int paddingTop = size2 - (getPaddingTop() + getPaddingBottom());
        if (this.mFixedCellWidth < 0 || this.mFixedCellHeight < 0) {
            int calculateCellWidth = DeviceProfile.calculateCellWidth(paddingLeft, this.mCountX);
            int calculateCellHeight = DeviceProfile.calculateCellHeight(paddingTop, this.mCountY);
            if (calculateCellWidth != this.mCellWidth || calculateCellHeight != this.mCellHeight) {
                this.mCellWidth = calculateCellWidth;
                this.mCellHeight = calculateCellHeight;
                this.mShortcutsAndWidgets.setCellDimensions(this.mCellWidth, this.mCellHeight, this.mWidthGap, this.mHeightGap, this.mCountX, this.mCountY);
            }
        }
        int i3 = paddingLeft;
        int i4 = paddingTop;
        if (this.mFixedWidth > 0 && this.mFixedHeight > 0) {
            i3 = this.mFixedWidth;
            i4 = this.mFixedHeight;
        } else if (mode == 0 || mode2 == 0) {
            throw new RuntimeException("CellLayout cannot have UNSPECIFIED dimensions");
        }
        int i5 = this.mCountX - 1;
        int i6 = this.mCountY - 1;
        if (this.mOriginalWidthGap < 0 || this.mOriginalHeightGap < 0) {
            int i7 = this.mCountX;
            int i8 = this.mCellWidth;
            int i9 = this.mCountY;
            int i10 = this.mCellHeight;
            this.mWidthGap = Math.min(this.mMaxGap, i5 > 0 ? (paddingLeft - (i7 * i8)) / i5 : 0);
            this.mHeightGap = Math.min(this.mMaxGap, i6 > 0 ? (paddingTop - (i9 * i10)) / i6 : 0);
            this.mShortcutsAndWidgets.setCellDimensions(this.mCellWidth, this.mCellHeight, this.mWidthGap, this.mHeightGap, this.mCountX, this.mCountY);
        } else {
            this.mWidthGap = this.mOriginalWidthGap;
            this.mHeightGap = this.mOriginalHeightGap;
        }
        this.mTouchFeedbackView.measure(View.MeasureSpec.makeMeasureSpec(this.mCellWidth + this.mTouchFeedbackView.getExtraSize(), 1073741824), View.MeasureSpec.makeMeasureSpec(this.mCellHeight + this.mTouchFeedbackView.getExtraSize(), 1073741824));
        this.mShortcutsAndWidgets.measure(View.MeasureSpec.makeMeasureSpec(i3, 1073741824), View.MeasureSpec.makeMeasureSpec(i4, 1073741824));
        int measuredWidth = this.mShortcutsAndWidgets.getMeasuredWidth();
        int measuredHeight = this.mShortcutsAndWidgets.getMeasuredHeight();
        if (this.mFixedWidth <= 0 || this.mFixedHeight <= 0) {
            setMeasuredDimension(size, size2);
        } else {
            setMeasuredDimension(measuredWidth, measuredHeight);
        }
    }

    @Override // android.view.View
    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        this.mBackground.getPadding(this.mTempRect);
        this.mBackground.setBounds(-this.mTempRect.left, -this.mTempRect.top, this.mTempRect.right + i, this.mTempRect.bottom + i2);
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                if (getParent() instanceof Workspace) {
                    ((Workspace) getParent()).updateChildrenLayersEnabled(true);
                    break;
                }
                break;
            case 1:
                if (getParent() instanceof Workspace) {
                    ((Workspace) getParent()).updateChildrenLayersEnabled(false);
                    break;
                }
                break;
        }
        boolean onTouchEvent = super.onTouchEvent(motionEvent);
        if (this.mLauncher.mWorkspace.isInOverviewMode() && this.mStylusEventHelper.checkAndPerformStylusEvent(motionEvent)) {
            return true;
        }
        return onTouchEvent;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: Code restructure failed: missing block: B:55:0x01dc, code lost:
        if (r24 == 3) goto L39;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public int[] performReorder(int i, int i2, int i3, int i4, int i5, int i6, View view, int[] iArr, int[] iArr2, int i7) {
        boolean z;
        int[] findNearestArea = findNearestArea(i, i2, i5, i6, iArr);
        int[] iArr3 = iArr2;
        if (iArr2 == null) {
            iArr3 = new int[2];
        }
        if ((i7 == 2 || i7 == 3 || i7 == 4) && this.mPreviousReorderDirection[0] != -100) {
            this.mDirectionVector[0] = this.mPreviousReorderDirection[0];
            this.mDirectionVector[1] = this.mPreviousReorderDirection[1];
            if (i7 == 2 || i7 == 3) {
                this.mPreviousReorderDirection[0] = -100;
                this.mPreviousReorderDirection[1] = -100;
            }
        } else {
            getDirectionVectorForDrop(i, i2, i5, i6, view, this.mDirectionVector);
            this.mPreviousReorderDirection[0] = this.mDirectionVector[0];
            this.mPreviousReorderDirection[1] = this.mDirectionVector[1];
        }
        ItemConfiguration findReorderSolution = findReorderSolution(i, i2, i3, i4, i5, i6, this.mDirectionVector, view, true, new ItemConfiguration(this));
        ItemConfiguration findConfigurationNoShuffle = findConfigurationNoShuffle(i, i2, i3, i4, i5, i6, view, new ItemConfiguration(this));
        if (!findReorderSolution.isSolution || findReorderSolution.area() < findConfigurationNoShuffle.area()) {
            findReorderSolution = null;
            if (findConfigurationNoShuffle.isSolution) {
                findReorderSolution = findConfigurationNoShuffle;
            }
        }
        if (i7 == 0) {
            if (findReorderSolution != null) {
                beginOrAdjustReorderPreviewAnimations(findReorderSolution, view, 0, 0);
                findNearestArea[0] = findReorderSolution.dragViewX;
                findNearestArea[1] = findReorderSolution.dragViewY;
                iArr3[0] = findReorderSolution.dragViewSpanX;
                iArr3[1] = findReorderSolution.dragViewSpanY;
            } else {
                iArr3[1] = -1;
                iArr3[0] = -1;
                findNearestArea[1] = -1;
                findNearestArea[0] = -1;
            }
            return findNearestArea;
        }
        setUseTempCoords(true);
        if (findReorderSolution != null) {
            findNearestArea[0] = findReorderSolution.dragViewX;
            findNearestArea[1] = findReorderSolution.dragViewY;
            iArr3[0] = findReorderSolution.dragViewSpanX;
            iArr3[1] = findReorderSolution.dragViewSpanY;
            if (i7 != 1 && i7 != 2) {
                z = true;
            }
            copySolutionToTempState(findReorderSolution, view);
            setItemPlacementDirty(true);
            animateItemsToSolution(findReorderSolution, view, i7 == 2);
            if (i7 == 2 || i7 == 3) {
                commitTempPlacement();
                completeAndClearReorderPreviewAnimations();
                setItemPlacementDirty(false);
                z = true;
            } else {
                beginOrAdjustReorderPreviewAnimations(findReorderSolution, view, 150, 1);
                z = true;
            }
        } else {
            z = false;
            iArr3[1] = -1;
            iArr3[0] = -1;
            findNearestArea[1] = -1;
            findNearestArea[0] = -1;
        }
        if (i7 == 2 || !z) {
            setUseTempCoords(false);
        }
        this.mShortcutsAndWidgets.requestLayout();
        return findNearestArea;
    }

    public void pointToCellExact(int i, int i2, int[] iArr) {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        iArr[0] = (i - paddingLeft) / (this.mCellWidth + this.mWidthGap);
        iArr[1] = (i2 - paddingTop) / (this.mCellHeight + this.mHeightGap);
        int i3 = this.mCountX;
        int i4 = this.mCountY;
        if (iArr[0] < 0) {
            iArr[0] = 0;
        }
        if (iArr[0] >= i3) {
            iArr[0] = i3 - 1;
        }
        if (iArr[1] < 0) {
            iArr[1] = 0;
        }
        if (iArr[1] >= i4) {
            iArr[1] = i4 - 1;
        }
    }

    public void prepareChildForDrag(View view) {
        markCellsAsUnoccupiedForView(view);
    }

    void regionToCenterPoint(int i, int i2, int i3, int i4, int[] iArr) {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        iArr[0] = ((this.mCellWidth + this.mWidthGap) * i) + paddingLeft + (((this.mCellWidth * i3) + ((i3 - 1) * this.mWidthGap)) / 2);
        iArr[1] = ((this.mCellHeight + this.mHeightGap) * i2) + paddingTop + (((this.mCellHeight * i4) + ((i4 - 1) * this.mHeightGap)) / 2);
    }

    void regionToRect(int i, int i2, int i3, int i4, Rect rect) {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int i5 = paddingLeft + ((this.mCellWidth + this.mWidthGap) * i);
        int i6 = paddingTop + ((this.mCellHeight + this.mHeightGap) * i2);
        rect.set(i5, i6, (this.mCellWidth * i3) + ((i3 - 1) * this.mWidthGap) + i5, (this.mCellHeight * i4) + ((i4 - 1) * this.mHeightGap) + i6);
    }

    @Override // android.view.ViewGroup
    public void removeAllViews() {
        clearOccupiedCells();
        this.mShortcutsAndWidgets.removeAllViews();
    }

    @Override // android.view.ViewGroup
    public void removeAllViewsInLayout() {
        if (this.mShortcutsAndWidgets.getChildCount() > 0) {
            clearOccupiedCells();
            this.mShortcutsAndWidgets.removeAllViewsInLayout();
        }
    }

    @Override // android.view.ViewGroup, android.view.ViewManager
    public void removeView(View view) {
        markCellsAsUnoccupiedForView(view);
        this.mShortcutsAndWidgets.removeView(view);
    }

    @Override // android.view.ViewGroup
    public void removeViewAt(int i) {
        markCellsAsUnoccupiedForView(this.mShortcutsAndWidgets.getChildAt(i));
        this.mShortcutsAndWidgets.removeViewAt(i);
    }

    @Override // android.view.ViewGroup
    public void removeViewInLayout(View view) {
        markCellsAsUnoccupiedForView(view);
        this.mShortcutsAndWidgets.removeViewInLayout(view);
    }

    @Override // android.view.ViewGroup
    public void removeViews(int i, int i2) {
        for (int i3 = i; i3 < i + i2; i3++) {
            markCellsAsUnoccupiedForView(this.mShortcutsAndWidgets.getChildAt(i3));
        }
        this.mShortcutsAndWidgets.removeViews(i, i2);
    }

    @Override // android.view.ViewGroup
    public void removeViewsInLayout(int i, int i2) {
        for (int i3 = i; i3 < i + i2; i3++) {
            markCellsAsUnoccupiedForView(this.mShortcutsAndWidgets.getChildAt(i3));
        }
        this.mShortcutsAndWidgets.removeViewsInLayout(i, i2);
    }

    public void restoreInstanceState(SparseArray<Parcelable> sparseArray) {
        try {
            dispatchRestoreInstanceState(sparseArray);
        } catch (IllegalArgumentException e) {
            if (LauncherAppState.isDogfoodBuild()) {
                throw e;
            }
            Log.e("CellLayout", "Ignoring an error while restoring a view instance state", e);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void revertTempState() {
        completeAndClearReorderPreviewAnimations();
        if (isItemPlacementDirty()) {
            int childCount = this.mShortcutsAndWidgets.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childAt = this.mShortcutsAndWidgets.getChildAt(i);
                LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
                if (layoutParams.tmpCellX != layoutParams.cellX || layoutParams.tmpCellY != layoutParams.cellY) {
                    layoutParams.tmpCellX = layoutParams.cellX;
                    layoutParams.tmpCellY = layoutParams.cellY;
                    animateChildToPosition(childAt, layoutParams.cellX, layoutParams.cellY, 150, 0, false, false);
                }
            }
            setItemPlacementDirty(false);
        }
    }

    public void setBackgroundAlpha(float f) {
        if (this.mBackgroundAlpha != f) {
            this.mBackgroundAlpha = f;
            this.mBackground.setAlpha((int) (this.mBackgroundAlpha * 255.0f));
        }
    }

    public void setCellDimensions(int i, int i2) {
        this.mCellWidth = i;
        this.mFixedCellWidth = i;
        this.mCellHeight = i2;
        this.mFixedCellHeight = i2;
        this.mShortcutsAndWidgets.setCellDimensions(this.mCellWidth, this.mCellHeight, this.mWidthGap, this.mHeightGap, this.mCountX, this.mCountY);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public void setChildrenDrawingCacheEnabled(boolean z) {
        this.mShortcutsAndWidgets.setChildrenDrawingCacheEnabled(z);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public void setChildrenDrawnWithCacheEnabled(boolean z) {
        this.mShortcutsAndWidgets.setChildrenDrawnWithCacheEnabled(z);
    }

    public void setDropPending(boolean z) {
        this.mDropPending = z;
    }

    public void setFixedSize(int i, int i2) {
        this.mFixedWidth = i;
        this.mFixedHeight = i2;
    }

    public void setFolderLeaveBehindCell(int i, int i2) {
        this.mFolderLeaveBehindCell[0] = i;
        this.mFolderLeaveBehindCell[1] = i2;
        invalidate();
    }

    public void setGridSize(int i, int i2) {
        this.mCountX = i;
        this.mCountY = i2;
        this.mOccupied = new boolean[this.mCountX][this.mCountY];
        this.mTmpOccupied = new boolean[this.mCountX][this.mCountY];
        this.mTempRectStack.clear();
        this.mShortcutsAndWidgets.setCellDimensions(this.mCellWidth, this.mCellHeight, this.mWidthGap, this.mHeightGap, this.mCountX, this.mCountY);
        requestLayout();
    }

    public void setInvertIfRtl(boolean z) {
        this.mShortcutsAndWidgets.setInvertIfRtl(z);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setIsDragOverlapping(boolean z) {
        if (this.mIsDragOverlapping != z) {
            this.mIsDragOverlapping = z;
            if (this.mIsDragOverlapping) {
                this.mBackground.startTransition(120);
            } else if (this.mBackgroundAlpha > 0.0f) {
                this.mBackground.reverseTransition(120);
            } else {
                this.mBackground.resetTransition();
            }
            invalidate();
        }
    }

    public void setIsHotseat(boolean z) {
        this.mIsHotseat = z;
        this.mShortcutsAndWidgets.setIsHotseat(z);
    }

    void setItemPlacementDirty(boolean z) {
        this.mItemPlacementDirty = z;
    }

    public void setOnInterceptTouchListener(View.OnTouchListener onTouchListener) {
        this.mInterceptTouchListener = onTouchListener;
    }

    @Override // com.android.launcher3.BubbleTextView.BubbleTextShadowHandler
    public void setPressedIcon(BubbleTextView bubbleTextView, Bitmap bitmap) {
        if (bubbleTextView == null || bitmap == null) {
            this.mTouchFeedbackView.setBitmap(null);
            this.mTouchFeedbackView.animate().cancel();
        } else if (this.mTouchFeedbackView.setBitmap(bitmap)) {
            this.mTouchFeedbackView.alignWithIconView(bubbleTextView, this.mShortcutsAndWidgets);
            this.mTouchFeedbackView.animateShadow();
        }
    }

    public void setShortcutAndWidgetAlpha(float f) {
        this.mShortcutsAndWidgets.setAlpha(f);
    }

    @Override // android.view.ViewGroup
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    public void showFolderAccept(FolderIcon.FolderRingAnimator folderRingAnimator) {
        this.mFolderOuterRings.add(folderRingAnimator);
    }

    @Override // android.view.View
    protected boolean verifyDrawable(Drawable drawable) {
        boolean z = true;
        if (!super.verifyDrawable(drawable)) {
            z = this.mIsDragTarget && drawable == this.mBackground;
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void visualizeDropLocation(View view, Bitmap bitmap, int i, int i2, int i3, int i4, boolean z, DropTarget.DragObject dragObject) {
        int width;
        int height;
        int i5 = this.mDragCell[0];
        int i6 = this.mDragCell[1];
        if (bitmap == null && view == null) {
            return;
        }
        if (i == i5 && i2 == i6) {
            return;
        }
        Point dragVisualizeOffset = dragObject.dragView.getDragVisualizeOffset();
        Rect dragRegion = dragObject.dragView.getDragRegion();
        this.mDragCell[0] = i;
        this.mDragCell[1] = i2;
        int[] iArr = this.mTmpPoint;
        cellToPoint(i, i2, iArr);
        int i7 = iArr[0];
        int i8 = iArr[1];
        if (view != null && dragVisualizeOffset == null) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            int i9 = marginLayoutParams.leftMargin;
            height = i8 + marginLayoutParams.topMargin + ((view.getHeight() - bitmap.getHeight()) / 2);
            width = i7 + i9 + ((((this.mCellWidth * i3) + ((i3 - 1) * this.mWidthGap)) - bitmap.getWidth()) / 2);
        } else if (dragVisualizeOffset == null || dragRegion == null) {
            width = i7 + ((((this.mCellWidth * i3) + ((i3 - 1) * this.mWidthGap)) - bitmap.getWidth()) / 2);
            height = i8 + ((((this.mCellHeight * i4) + ((i4 - 1) * this.mHeightGap)) - bitmap.getHeight()) / 2);
        } else {
            width = i7 + dragVisualizeOffset.x + ((((this.mCellWidth * i3) + ((i3 - 1) * this.mWidthGap)) - dragRegion.width()) / 2);
            height = i8 + dragVisualizeOffset.y + ((int) Math.max(0.0f, (this.mCellHeight - getShortcutsAndWidgets().getCellContentHeight()) / 2.0f));
        }
        int i10 = this.mDragOutlineCurrent;
        this.mDragOutlineAnims[i10].animateOut();
        this.mDragOutlineCurrent = (i10 + 1) % this.mDragOutlines.length;
        Rect rect = this.mDragOutlines[this.mDragOutlineCurrent];
        rect.set(width, height, bitmap.getWidth() + width, bitmap.getHeight() + height);
        if (z) {
            cellToRect(i, i2, i3, i4, rect);
        }
        this.mDragOutlineAnims[this.mDragOutlineCurrent].setTag(bitmap);
        this.mDragOutlineAnims[this.mDragOutlineCurrent].animateIn();
        if (dragObject.stateAnnouncer != null) {
            dragObject.stateAnnouncer.announce(isHotseat() ? getContext().getString(2131558472, Integer.valueOf(Math.max(i, i2) + 1)) : getContext().getString(2131558470, Integer.valueOf(i2 + 1), Integer.valueOf(i + 1)));
        }
    }
}
