package android.support.v7.widget.helper;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.animation.AnimatorCompatHelper;
import android.support.v4.animation.AnimatorListenerCompat;
import android.support.v4.animation.AnimatorUpdateListenerCompat;
import android.support.v4.animation.ValueAnimatorCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.recyclerview.R$dimen;
import android.support.v7.recyclerview.R$id;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.view.animation.Interpolator;
import java.util.ArrayList;
import java.util.List;
/* loaded from: a.zip:android/support/v7/widget/helper/ItemTouchHelper.class */
public class ItemTouchHelper extends RecyclerView.ItemDecoration implements RecyclerView.OnChildAttachStateChangeListener {
    Callback mCallback;
    private List<Integer> mDistances;
    private long mDragScrollStartTimeInMs;
    float mDx;
    float mDy;
    private GestureDetectorCompat mGestureDetector;
    float mInitialTouchX;
    float mInitialTouchY;
    float mMaxSwipeVelocity;
    private RecyclerView mRecyclerView;
    int mSelectedFlags;
    float mSelectedStartX;
    float mSelectedStartY;
    private int mSlop;
    private List<RecyclerView.ViewHolder> mSwapTargets;
    float mSwipeEscapeVelocity;
    private Rect mTmpRect;
    private VelocityTracker mVelocityTracker;
    final List<View> mPendingCleanup = new ArrayList();
    private final float[] mTmpPosition = new float[2];
    RecyclerView.ViewHolder mSelected = null;
    int mActivePointerId = -1;
    int mActionState = 0;
    List<RecoverAnimation> mRecoverAnimations = new ArrayList();
    private final Runnable mScrollRunnable = new Runnable(this) { // from class: android.support.v7.widget.helper.ItemTouchHelper.1
        final ItemTouchHelper this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            if (this.this$0.mSelected == null || !this.this$0.scrollIfNecessary()) {
                return;
            }
            if (this.this$0.mSelected != null) {
                this.this$0.moveIfNecessary(this.this$0.mSelected);
            }
            this.this$0.mRecyclerView.removeCallbacks(this.this$0.mScrollRunnable);
            ViewCompat.postOnAnimation(this.this$0.mRecyclerView, this);
        }
    };
    private RecyclerView.ChildDrawingOrderCallback mChildDrawingOrderCallback = null;
    private View mOverdrawChild = null;
    private int mOverdrawChildPosition = -1;
    private final RecyclerView.OnItemTouchListener mOnItemTouchListener = new RecyclerView.OnItemTouchListener(this) { // from class: android.support.v7.widget.helper.ItemTouchHelper.2
        final ItemTouchHelper this$0;

        {
            this.this$0 = this;
        }

        @Override // android.support.v7.widget.RecyclerView.OnItemTouchListener
        public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
            int findPointerIndex;
            RecoverAnimation findAnimation;
            boolean z = true;
            this.this$0.mGestureDetector.onTouchEvent(motionEvent);
            int actionMasked = MotionEventCompat.getActionMasked(motionEvent);
            if (actionMasked == 0) {
                this.this$0.mActivePointerId = MotionEventCompat.getPointerId(motionEvent, 0);
                this.this$0.mInitialTouchX = motionEvent.getX();
                this.this$0.mInitialTouchY = motionEvent.getY();
                this.this$0.obtainVelocityTracker();
                if (this.this$0.mSelected == null && (findAnimation = this.this$0.findAnimation(motionEvent)) != null) {
                    this.this$0.mInitialTouchX -= findAnimation.mX;
                    this.this$0.mInitialTouchY -= findAnimation.mY;
                    this.this$0.endRecoverAnimation(findAnimation.mViewHolder, true);
                    if (this.this$0.mPendingCleanup.remove(findAnimation.mViewHolder.itemView)) {
                        this.this$0.mCallback.clearView(this.this$0.mRecyclerView, findAnimation.mViewHolder);
                    }
                    this.this$0.select(findAnimation.mViewHolder, findAnimation.mActionState);
                    this.this$0.updateDxDy(motionEvent, this.this$0.mSelectedFlags, 0);
                }
            } else if (actionMasked == 3 || actionMasked == 1) {
                this.this$0.mActivePointerId = -1;
                this.this$0.select(null, 0);
            } else if (this.this$0.mActivePointerId != -1 && (findPointerIndex = MotionEventCompat.findPointerIndex(motionEvent, this.this$0.mActivePointerId)) >= 0) {
                this.this$0.checkSelectForSwipe(actionMasked, motionEvent, findPointerIndex);
            }
            if (this.this$0.mVelocityTracker != null) {
                this.this$0.mVelocityTracker.addMovement(motionEvent);
            }
            if (this.this$0.mSelected == null) {
                z = false;
            }
            return z;
        }

        @Override // android.support.v7.widget.RecyclerView.OnItemTouchListener
        public void onRequestDisallowInterceptTouchEvent(boolean z) {
            if (z) {
                this.this$0.select(null, 0);
            }
        }

        /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
        @Override // android.support.v7.widget.RecyclerView.OnItemTouchListener
        public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
            this.this$0.mGestureDetector.onTouchEvent(motionEvent);
            if (this.this$0.mVelocityTracker != null) {
                this.this$0.mVelocityTracker.addMovement(motionEvent);
            }
            if (this.this$0.mActivePointerId == -1) {
                return;
            }
            int actionMasked = MotionEventCompat.getActionMasked(motionEvent);
            int findPointerIndex = MotionEventCompat.findPointerIndex(motionEvent, this.this$0.mActivePointerId);
            if (findPointerIndex >= 0) {
                this.this$0.checkSelectForSwipe(actionMasked, motionEvent, findPointerIndex);
            }
            RecyclerView.ViewHolder viewHolder = this.this$0.mSelected;
            if (viewHolder == null) {
                return;
            }
            switch (actionMasked) {
                case 1:
                    break;
                case 2:
                    if (findPointerIndex >= 0) {
                        this.this$0.updateDxDy(motionEvent, this.this$0.mSelectedFlags, findPointerIndex);
                        this.this$0.moveIfNecessary(viewHolder);
                        this.this$0.mRecyclerView.removeCallbacks(this.this$0.mScrollRunnable);
                        this.this$0.mScrollRunnable.run();
                        this.this$0.mRecyclerView.invalidate();
                        return;
                    }
                    return;
                case 3:
                    if (this.this$0.mVelocityTracker != null) {
                        this.this$0.mVelocityTracker.clear();
                        break;
                    }
                    break;
                case 4:
                case 5:
                default:
                    return;
                case 6:
                    int actionIndex = MotionEventCompat.getActionIndex(motionEvent);
                    if (MotionEventCompat.getPointerId(motionEvent, actionIndex) == this.this$0.mActivePointerId) {
                        this.this$0.mActivePointerId = MotionEventCompat.getPointerId(motionEvent, actionIndex == 0 ? 1 : 0);
                        this.this$0.updateDxDy(motionEvent, this.this$0.mSelectedFlags, actionIndex);
                        return;
                    }
                    return;
            }
            this.this$0.select(null, 0);
            this.this$0.mActivePointerId = -1;
        }
    };

    /* loaded from: a.zip:android/support/v7/widget/helper/ItemTouchHelper$Callback.class */
    public static abstract class Callback {
        private static final Interpolator sDragScrollInterpolator = new Interpolator() { // from class: android.support.v7.widget.helper.ItemTouchHelper.Callback.1
            @Override // android.animation.TimeInterpolator
            public float getInterpolation(float f) {
                return f * f * f * f * f;
            }
        };
        private static final Interpolator sDragViewScrollCapInterpolator = new Interpolator() { // from class: android.support.v7.widget.helper.ItemTouchHelper.Callback.2
            @Override // android.animation.TimeInterpolator
            public float getInterpolation(float f) {
                float f2 = f - 1.0f;
                return (f2 * f2 * f2 * f2 * f2) + 1.0f;
            }
        };
        private static final ItemTouchUIUtil sUICallback;
        private int mCachedMaxScrollSpeed = -1;

        static {
            if (Build.VERSION.SDK_INT >= 21) {
                sUICallback = new ItemTouchUIUtilImpl$Honeycomb() { // from class: android.support.v7.widget.helper.ItemTouchUIUtilImpl$Lollipop
                    private float findMaxElevation(RecyclerView recyclerView, View view) {
                        float f;
                        int childCount = recyclerView.getChildCount();
                        float f2 = 0.0f;
                        int i = 0;
                        while (i < childCount) {
                            View childAt = recyclerView.getChildAt(i);
                            if (childAt == view) {
                                f = f2;
                            } else {
                                float elevation = ViewCompat.getElevation(childAt);
                                f = f2;
                                if (elevation > f2) {
                                    f = elevation;
                                }
                            }
                            i++;
                            f2 = f;
                        }
                        return f2;
                    }

                    @Override // android.support.v7.widget.helper.ItemTouchUIUtilImpl$Honeycomb, android.support.v7.widget.helper.ItemTouchUIUtil
                    public void clearView(View view) {
                        Object tag = view.getTag(R$id.item_touch_helper_previous_elevation);
                        if (tag != null && (tag instanceof Float)) {
                            ViewCompat.setElevation(view, ((Float) tag).floatValue());
                        }
                        view.setTag(R$id.item_touch_helper_previous_elevation, null);
                        super.clearView(view);
                    }

                    @Override // android.support.v7.widget.helper.ItemTouchUIUtilImpl$Honeycomb, android.support.v7.widget.helper.ItemTouchUIUtil
                    public void onDraw(Canvas canvas, RecyclerView recyclerView, View view, float f, float f2, int i, boolean z) {
                        if (z && view.getTag(R$id.item_touch_helper_previous_elevation) == null) {
                            float elevation = ViewCompat.getElevation(view);
                            ViewCompat.setElevation(view, 1.0f + findMaxElevation(recyclerView, view));
                            view.setTag(R$id.item_touch_helper_previous_elevation, Float.valueOf(elevation));
                        }
                        super.onDraw(canvas, recyclerView, view, f, f2, i, z);
                    }
                };
            } else if (Build.VERSION.SDK_INT >= 11) {
                sUICallback = new ItemTouchUIUtilImpl$Honeycomb();
            } else {
                sUICallback = new ItemTouchUIUtil() { // from class: android.support.v7.widget.helper.ItemTouchUIUtilImpl$Gingerbread
                    private void draw(Canvas canvas, RecyclerView recyclerView, View view, float f, float f2) {
                        canvas.save();
                        canvas.translate(f, f2);
                        recyclerView.drawChild(canvas, view, 0L);
                        canvas.restore();
                    }

                    @Override // android.support.v7.widget.helper.ItemTouchUIUtil
                    public void clearView(View view) {
                        view.setVisibility(0);
                    }

                    @Override // android.support.v7.widget.helper.ItemTouchUIUtil
                    public void onDraw(Canvas canvas, RecyclerView recyclerView, View view, float f, float f2, int i, boolean z) {
                        if (i != 2) {
                            draw(canvas, recyclerView, view, f, f2);
                        }
                    }

                    @Override // android.support.v7.widget.helper.ItemTouchUIUtil
                    public void onDrawOver(Canvas canvas, RecyclerView recyclerView, View view, float f, float f2, int i, boolean z) {
                        if (i == 2) {
                            draw(canvas, recyclerView, view, f, f2);
                        }
                    }

                    @Override // android.support.v7.widget.helper.ItemTouchUIUtil
                    public void onSelected(View view) {
                        view.setVisibility(4);
                    }
                };
            }
        }

        public static int convertToRelativeDirection(int i, int i2) {
            int i3 = i & 789516;
            if (i3 == 0) {
                return i;
            }
            int i4 = i & (i3 ^ (-1));
            return i2 == 0 ? i4 | (i3 << 2) : i4 | ((i3 << 1) & (-789517)) | (((i3 << 1) & 789516) << 2);
        }

        private int getMaxDragScroll(RecyclerView recyclerView) {
            if (this.mCachedMaxScrollSpeed == -1) {
                this.mCachedMaxScrollSpeed = recyclerView.getResources().getDimensionPixelSize(R$dimen.item_touch_helper_max_drag_scroll_per_frame);
            }
            return this.mCachedMaxScrollSpeed;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public boolean hasDragFlag(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            boolean z = false;
            if ((16711680 & getAbsoluteMovementFlags(recyclerView, viewHolder)) != 0) {
                z = true;
            }
            return z;
        }

        public static int makeFlag(int i, int i2) {
            return i2 << (i * 8);
        }

        public static int makeMovementFlags(int i, int i2) {
            return makeFlag(0, i2 | i) | makeFlag(1, i2) | makeFlag(2, i);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void onDraw(Canvas canvas, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, List<RecoverAnimation> list, int i, float f, float f2) {
            int size = list.size();
            for (int i2 = 0; i2 < size; i2++) {
                RecoverAnimation recoverAnimation = list.get(i2);
                recoverAnimation.update();
                int save = canvas.save();
                onChildDraw(canvas, recyclerView, recoverAnimation.mViewHolder, recoverAnimation.mX, recoverAnimation.mY, recoverAnimation.mActionState, false);
                canvas.restoreToCount(save);
            }
            if (viewHolder != null) {
                int save2 = canvas.save();
                onChildDraw(canvas, recyclerView, viewHolder, f, f2, i, true);
                canvas.restoreToCount(save2);
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void onDrawOver(Canvas canvas, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, List<RecoverAnimation> list, int i, float f, float f2) {
            int size = list.size();
            for (int i2 = 0; i2 < size; i2++) {
                RecoverAnimation recoverAnimation = list.get(i2);
                int save = canvas.save();
                onChildDrawOver(canvas, recyclerView, recoverAnimation.mViewHolder, recoverAnimation.mX, recoverAnimation.mY, recoverAnimation.mActionState, false);
                canvas.restoreToCount(save);
            }
            if (viewHolder != null) {
                int save2 = canvas.save();
                onChildDrawOver(canvas, recyclerView, viewHolder, f, f2, i, true);
                canvas.restoreToCount(save2);
            }
            boolean z = false;
            for (int i3 = size - 1; i3 >= 0; i3--) {
                RecoverAnimation recoverAnimation2 = list.get(i3);
                if (recoverAnimation2.mEnded && !recoverAnimation2.mIsPendingCleanup) {
                    list.remove(i3);
                } else if (!recoverAnimation2.mEnded) {
                    z = true;
                }
            }
            if (z) {
                recyclerView.invalidate();
            }
        }

        public boolean canDropOver(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder2) {
            return true;
        }

        public RecyclerView.ViewHolder chooseDropTarget(RecyclerView.ViewHolder viewHolder, List<RecyclerView.ViewHolder> list, int i, int i2) {
            int width = viewHolder.itemView.getWidth();
            int height = viewHolder.itemView.getHeight();
            RecyclerView.ViewHolder viewHolder2 = null;
            int i3 = -1;
            int left = i - viewHolder.itemView.getLeft();
            int top = i2 - viewHolder.itemView.getTop();
            int size = list.size();
            for (int i4 = 0; i4 < size; i4++) {
                RecyclerView.ViewHolder viewHolder3 = list.get(i4);
                RecyclerView.ViewHolder viewHolder4 = viewHolder2;
                int i5 = i3;
                if (left > 0) {
                    int right = viewHolder3.itemView.getRight() - (i + width);
                    viewHolder4 = viewHolder2;
                    i5 = i3;
                    if (right < 0) {
                        viewHolder4 = viewHolder2;
                        i5 = i3;
                        if (viewHolder3.itemView.getRight() > viewHolder.itemView.getRight()) {
                            int abs = Math.abs(right);
                            viewHolder4 = viewHolder2;
                            i5 = i3;
                            if (abs > i3) {
                                i5 = abs;
                                viewHolder4 = viewHolder3;
                            }
                        }
                    }
                }
                RecyclerView.ViewHolder viewHolder5 = viewHolder4;
                int i6 = i5;
                if (left < 0) {
                    int left2 = viewHolder3.itemView.getLeft() - i;
                    viewHolder5 = viewHolder4;
                    i6 = i5;
                    if (left2 > 0) {
                        viewHolder5 = viewHolder4;
                        i6 = i5;
                        if (viewHolder3.itemView.getLeft() < viewHolder.itemView.getLeft()) {
                            int abs2 = Math.abs(left2);
                            viewHolder5 = viewHolder4;
                            i6 = i5;
                            if (abs2 > i5) {
                                i6 = abs2;
                                viewHolder5 = viewHolder3;
                            }
                        }
                    }
                }
                RecyclerView.ViewHolder viewHolder6 = viewHolder5;
                int i7 = i6;
                if (top < 0) {
                    int top2 = viewHolder3.itemView.getTop() - i2;
                    viewHolder6 = viewHolder5;
                    i7 = i6;
                    if (top2 > 0) {
                        viewHolder6 = viewHolder5;
                        i7 = i6;
                        if (viewHolder3.itemView.getTop() < viewHolder.itemView.getTop()) {
                            int abs3 = Math.abs(top2);
                            viewHolder6 = viewHolder5;
                            i7 = i6;
                            if (abs3 > i6) {
                                i7 = abs3;
                                viewHolder6 = viewHolder3;
                            }
                        }
                    }
                }
                viewHolder2 = viewHolder6;
                i3 = i7;
                if (top > 0) {
                    int bottom = viewHolder3.itemView.getBottom() - (i2 + height);
                    viewHolder2 = viewHolder6;
                    i3 = i7;
                    if (bottom < 0) {
                        viewHolder2 = viewHolder6;
                        i3 = i7;
                        if (viewHolder3.itemView.getBottom() > viewHolder.itemView.getBottom()) {
                            int abs4 = Math.abs(bottom);
                            viewHolder2 = viewHolder6;
                            i3 = i7;
                            if (abs4 > i7) {
                                i3 = abs4;
                                viewHolder2 = viewHolder3;
                            }
                        }
                    }
                }
            }
            return viewHolder2;
        }

        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            sUICallback.clearView(viewHolder.itemView);
        }

        public int convertToAbsoluteDirection(int i, int i2) {
            int i3 = i & 3158064;
            if (i3 == 0) {
                return i;
            }
            int i4 = i & (i3 ^ (-1));
            return i2 == 0 ? i4 | (i3 >> 2) : i4 | ((i3 >> 1) & (-3158065)) | (((i3 >> 1) & 3158064) >> 2);
        }

        final int getAbsoluteMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return convertToAbsoluteDirection(getMovementFlags(recyclerView, viewHolder), ViewCompat.getLayoutDirection(recyclerView));
        }

        public long getAnimationDuration(RecyclerView recyclerView, int i, float f, float f2) {
            RecyclerView.ItemAnimator itemAnimator = recyclerView.getItemAnimator();
            if (itemAnimator == null) {
                return i == 8 ? 200 : 250;
            }
            return i == 8 ? itemAnimator.getMoveDuration() : itemAnimator.getRemoveDuration();
        }

        public int getBoundingBoxMargin() {
            return 0;
        }

        public float getMoveThreshold(RecyclerView.ViewHolder viewHolder) {
            return 0.5f;
        }

        public abstract int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder);

        public float getSwipeEscapeVelocity(float f) {
            return f;
        }

        public float getSwipeThreshold(RecyclerView.ViewHolder viewHolder) {
            return 0.5f;
        }

        public float getSwipeVelocityThreshold(float f) {
            return f;
        }

        public int interpolateOutOfBoundsScroll(RecyclerView recyclerView, int i, int i2, int i3, long j) {
            int signum = (int) (((int) (((int) Math.signum(i2)) * getMaxDragScroll(recyclerView) * sDragViewScrollCapInterpolator.getInterpolation(Math.min(1.0f, (Math.abs(i2) * 1.0f) / i)))) * sDragScrollInterpolator.getInterpolation(j > 2000 ? 1.0f : ((float) j) / 2000.0f));
            if (signum == 0) {
                return i2 > 0 ? 1 : -1;
            }
            return signum;
        }

        public boolean isItemViewSwipeEnabled() {
            return true;
        }

        public boolean isLongPressDragEnabled() {
            return true;
        }

        public void onChildDraw(Canvas canvas, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float f, float f2, int i, boolean z) {
            sUICallback.onDraw(canvas, recyclerView, viewHolder.itemView, f, f2, i, z);
        }

        public void onChildDrawOver(Canvas canvas, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float f, float f2, int i, boolean z) {
            sUICallback.onDrawOver(canvas, recyclerView, viewHolder.itemView, f, f2, i, z);
        }

        public abstract boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder2);

        public void onMoved(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, int i, RecyclerView.ViewHolder viewHolder2, int i2, int i3, int i4) {
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager instanceof ViewDropHandler) {
                ((ViewDropHandler) layoutManager).prepareForDrop(viewHolder.itemView, viewHolder2.itemView, i3, i4);
                return;
            }
            if (layoutManager.canScrollHorizontally()) {
                if (layoutManager.getDecoratedLeft(viewHolder2.itemView) <= recyclerView.getPaddingLeft()) {
                    recyclerView.scrollToPosition(i2);
                }
                if (layoutManager.getDecoratedRight(viewHolder2.itemView) >= recyclerView.getWidth() - recyclerView.getPaddingRight()) {
                    recyclerView.scrollToPosition(i2);
                }
            }
            if (layoutManager.canScrollVertically()) {
                if (layoutManager.getDecoratedTop(viewHolder2.itemView) <= recyclerView.getPaddingTop()) {
                    recyclerView.scrollToPosition(i2);
                }
                if (layoutManager.getDecoratedBottom(viewHolder2.itemView) >= recyclerView.getHeight() - recyclerView.getPaddingBottom()) {
                    recyclerView.scrollToPosition(i2);
                }
            }
        }

        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int i) {
            if (viewHolder != null) {
                sUICallback.onSelected(viewHolder.itemView);
            }
        }

        public abstract void onSwiped(RecyclerView.ViewHolder viewHolder, int i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:android/support/v7/widget/helper/ItemTouchHelper$ItemTouchHelperGestureListener.class */
    public class ItemTouchHelperGestureListener extends GestureDetector.SimpleOnGestureListener {
        final ItemTouchHelper this$0;

        private ItemTouchHelperGestureListener(ItemTouchHelper itemTouchHelper) {
            this.this$0 = itemTouchHelper;
        }

        /* synthetic */ ItemTouchHelperGestureListener(ItemTouchHelper itemTouchHelper, ItemTouchHelperGestureListener itemTouchHelperGestureListener) {
            this(itemTouchHelper);
        }

        @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
        public boolean onDown(MotionEvent motionEvent) {
            return true;
        }

        @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
        public void onLongPress(MotionEvent motionEvent) {
            RecyclerView.ViewHolder childViewHolder;
            View findChildView = this.this$0.findChildView(motionEvent);
            if (findChildView == null || (childViewHolder = this.this$0.mRecyclerView.getChildViewHolder(findChildView)) == null || !this.this$0.mCallback.hasDragFlag(this.this$0.mRecyclerView, childViewHolder) || MotionEventCompat.getPointerId(motionEvent, 0) != this.this$0.mActivePointerId) {
                return;
            }
            int findPointerIndex = MotionEventCompat.findPointerIndex(motionEvent, this.this$0.mActivePointerId);
            float x = MotionEventCompat.getX(motionEvent, findPointerIndex);
            float y = MotionEventCompat.getY(motionEvent, findPointerIndex);
            this.this$0.mInitialTouchX = x;
            this.this$0.mInitialTouchY = y;
            ItemTouchHelper itemTouchHelper = this.this$0;
            this.this$0.mDy = 0.0f;
            itemTouchHelper.mDx = 0.0f;
            if (this.this$0.mCallback.isLongPressDragEnabled()) {
                this.this$0.select(childViewHolder, 2);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:android/support/v7/widget/helper/ItemTouchHelper$RecoverAnimation.class */
    public class RecoverAnimation implements AnimatorListenerCompat {
        final int mActionState;
        private final int mAnimationType;
        private float mFraction;
        public boolean mIsPendingCleanup;
        final float mStartDx;
        final float mStartDy;
        final float mTargetX;
        final float mTargetY;
        final RecyclerView.ViewHolder mViewHolder;
        float mX;
        float mY;
        final ItemTouchHelper this$0;
        boolean mOverridden = false;
        private boolean mEnded = false;
        private final ValueAnimatorCompat mValueAnimator = AnimatorCompatHelper.emptyValueAnimator();

        public RecoverAnimation(ItemTouchHelper itemTouchHelper, RecyclerView.ViewHolder viewHolder, int i, int i2, float f, float f2, float f3, float f4) {
            this.this$0 = itemTouchHelper;
            this.mActionState = i2;
            this.mAnimationType = i;
            this.mViewHolder = viewHolder;
            this.mStartDx = f;
            this.mStartDy = f2;
            this.mTargetX = f3;
            this.mTargetY = f4;
            this.mValueAnimator.addUpdateListener(new AnimatorUpdateListenerCompat(this) { // from class: android.support.v7.widget.helper.ItemTouchHelper.RecoverAnimation.1
                final RecoverAnimation this$1;

                {
                    this.this$1 = this;
                }

                @Override // android.support.v4.animation.AnimatorUpdateListenerCompat
                public void onAnimationUpdate(ValueAnimatorCompat valueAnimatorCompat) {
                    this.this$1.setFraction(valueAnimatorCompat.getAnimatedFraction());
                }
            });
            this.mValueAnimator.setTarget(viewHolder.itemView);
            this.mValueAnimator.addListener(this);
            setFraction(0.0f);
        }

        public void cancel() {
            this.mValueAnimator.cancel();
        }

        @Override // android.support.v4.animation.AnimatorListenerCompat
        public void onAnimationCancel(ValueAnimatorCompat valueAnimatorCompat) {
            setFraction(1.0f);
        }

        @Override // android.support.v4.animation.AnimatorListenerCompat
        public void onAnimationEnd(ValueAnimatorCompat valueAnimatorCompat) {
            if (!this.mEnded) {
                this.mViewHolder.setIsRecyclable(true);
            }
            this.mEnded = true;
        }

        @Override // android.support.v4.animation.AnimatorListenerCompat
        public void onAnimationRepeat(ValueAnimatorCompat valueAnimatorCompat) {
        }

        @Override // android.support.v4.animation.AnimatorListenerCompat
        public void onAnimationStart(ValueAnimatorCompat valueAnimatorCompat) {
        }

        public void setDuration(long j) {
            this.mValueAnimator.setDuration(j);
        }

        public void setFraction(float f) {
            this.mFraction = f;
        }

        public void start() {
            this.mViewHolder.setIsRecyclable(false);
            this.mValueAnimator.start();
        }

        public void update() {
            if (this.mStartDx == this.mTargetX) {
                this.mX = ViewCompat.getTranslationX(this.mViewHolder.itemView);
            } else {
                this.mX = this.mStartDx + (this.mFraction * (this.mTargetX - this.mStartDx));
            }
            if (this.mStartDy == this.mTargetY) {
                this.mY = ViewCompat.getTranslationY(this.mViewHolder.itemView);
            } else {
                this.mY = this.mStartDy + (this.mFraction * (this.mTargetY - this.mStartDy));
            }
        }
    }

    /* loaded from: a.zip:android/support/v7/widget/helper/ItemTouchHelper$ViewDropHandler.class */
    public interface ViewDropHandler {
        void prepareForDrop(View view, View view2, int i, int i2);
    }

    public ItemTouchHelper(Callback callback) {
        this.mCallback = callback;
    }

    private void addChildDrawingOrderCallback() {
        if (Build.VERSION.SDK_INT >= 21) {
            return;
        }
        if (this.mChildDrawingOrderCallback == null) {
            this.mChildDrawingOrderCallback = new RecyclerView.ChildDrawingOrderCallback(this) { // from class: android.support.v7.widget.helper.ItemTouchHelper.5
                final ItemTouchHelper this$0;

                {
                    this.this$0 = this;
                }

                @Override // android.support.v7.widget.RecyclerView.ChildDrawingOrderCallback
                public int onGetChildDrawingOrder(int i, int i2) {
                    if (this.this$0.mOverdrawChild == null) {
                        return i2;
                    }
                    int i3 = this.this$0.mOverdrawChildPosition;
                    int i4 = i3;
                    if (i3 == -1) {
                        i4 = this.this$0.mRecyclerView.indexOfChild(this.this$0.mOverdrawChild);
                        this.this$0.mOverdrawChildPosition = i4;
                    }
                    if (i2 == i - 1) {
                        return i4;
                    }
                    if (i2 >= i4) {
                        i2++;
                    }
                    return i2;
                }
            };
        }
        this.mRecyclerView.setChildDrawingOrderCallback(this.mChildDrawingOrderCallback);
    }

    private int checkHorizontalSwipe(RecyclerView.ViewHolder viewHolder, int i) {
        if ((i & 12) != 0) {
            int i2 = this.mDx > 0.0f ? 8 : 4;
            if (this.mVelocityTracker != null && this.mActivePointerId > -1) {
                this.mVelocityTracker.computeCurrentVelocity(1000, this.mCallback.getSwipeVelocityThreshold(this.mMaxSwipeVelocity));
                float xVelocity = VelocityTrackerCompat.getXVelocity(this.mVelocityTracker, this.mActivePointerId);
                float yVelocity = VelocityTrackerCompat.getYVelocity(this.mVelocityTracker, this.mActivePointerId);
                int i3 = xVelocity > 0.0f ? 8 : 4;
                float abs = Math.abs(xVelocity);
                if ((i3 & i) != 0 && i2 == i3 && abs >= this.mCallback.getSwipeEscapeVelocity(this.mSwipeEscapeVelocity) && abs > Math.abs(yVelocity)) {
                    return i3;
                }
            }
            float width = this.mRecyclerView.getWidth();
            float swipeThreshold = this.mCallback.getSwipeThreshold(viewHolder);
            if ((i & i2) == 0 || Math.abs(this.mDx) <= width * swipeThreshold) {
                return 0;
            }
            return i2;
        }
        return 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean checkSelectForSwipe(int i, MotionEvent motionEvent, int i2) {
        RecyclerView.ViewHolder findSwipedView;
        int absoluteMovementFlags;
        if (this.mSelected != null || i != 2 || this.mActionState == 2 || !this.mCallback.isItemViewSwipeEnabled() || this.mRecyclerView.getScrollState() == 1 || (findSwipedView = findSwipedView(motionEvent)) == null || (absoluteMovementFlags = (65280 & this.mCallback.getAbsoluteMovementFlags(this.mRecyclerView, findSwipedView)) >> 8) == 0) {
            return false;
        }
        float x = MotionEventCompat.getX(motionEvent, i2);
        float y = MotionEventCompat.getY(motionEvent, i2);
        float f = x - this.mInitialTouchX;
        float f2 = y - this.mInitialTouchY;
        float abs = Math.abs(f);
        float abs2 = Math.abs(f2);
        if (abs >= this.mSlop || abs2 >= this.mSlop) {
            if (abs > abs2) {
                if (f < 0.0f && (absoluteMovementFlags & 4) == 0) {
                    return false;
                }
                if (f > 0.0f && (absoluteMovementFlags & 8) == 0) {
                    return false;
                }
            } else if (f2 < 0.0f && (absoluteMovementFlags & 1) == 0) {
                return false;
            } else {
                if (f2 > 0.0f && (absoluteMovementFlags & 2) == 0) {
                    return false;
                }
            }
            this.mDy = 0.0f;
            this.mDx = 0.0f;
            this.mActivePointerId = MotionEventCompat.getPointerId(motionEvent, 0);
            select(findSwipedView, 1);
            return true;
        }
        return false;
    }

    private int checkVerticalSwipe(RecyclerView.ViewHolder viewHolder, int i) {
        if ((i & 3) != 0) {
            int i2 = this.mDy > 0.0f ? 2 : 1;
            if (this.mVelocityTracker != null && this.mActivePointerId > -1) {
                this.mVelocityTracker.computeCurrentVelocity(1000, this.mCallback.getSwipeVelocityThreshold(this.mMaxSwipeVelocity));
                float xVelocity = VelocityTrackerCompat.getXVelocity(this.mVelocityTracker, this.mActivePointerId);
                float yVelocity = VelocityTrackerCompat.getYVelocity(this.mVelocityTracker, this.mActivePointerId);
                int i3 = yVelocity > 0.0f ? 2 : 1;
                float abs = Math.abs(yVelocity);
                if ((i3 & i) != 0 && i3 == i2 && abs >= this.mCallback.getSwipeEscapeVelocity(this.mSwipeEscapeVelocity) && abs > Math.abs(xVelocity)) {
                    return i3;
                }
            }
            float height = this.mRecyclerView.getHeight();
            float swipeThreshold = this.mCallback.getSwipeThreshold(viewHolder);
            if ((i & i2) == 0 || Math.abs(this.mDy) <= height * swipeThreshold) {
                return 0;
            }
            return i2;
        }
        return 0;
    }

    private void destroyCallbacks() {
        this.mRecyclerView.removeItemDecoration(this);
        this.mRecyclerView.removeOnItemTouchListener(this.mOnItemTouchListener);
        this.mRecyclerView.removeOnChildAttachStateChangeListener(this);
        for (int size = this.mRecoverAnimations.size() - 1; size >= 0; size--) {
            this.mCallback.clearView(this.mRecyclerView, this.mRecoverAnimations.get(0).mViewHolder);
        }
        this.mRecoverAnimations.clear();
        this.mOverdrawChild = null;
        this.mOverdrawChildPosition = -1;
        releaseVelocityTracker();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int endRecoverAnimation(RecyclerView.ViewHolder viewHolder, boolean z) {
        for (int size = this.mRecoverAnimations.size() - 1; size >= 0; size--) {
            RecoverAnimation recoverAnimation = this.mRecoverAnimations.get(size);
            if (recoverAnimation.mViewHolder == viewHolder) {
                recoverAnimation.mOverridden |= z;
                if (!recoverAnimation.mEnded) {
                    recoverAnimation.cancel();
                }
                this.mRecoverAnimations.remove(size);
                return recoverAnimation.mAnimationType;
            }
        }
        return 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public RecoverAnimation findAnimation(MotionEvent motionEvent) {
        if (this.mRecoverAnimations.isEmpty()) {
            return null;
        }
        View findChildView = findChildView(motionEvent);
        for (int size = this.mRecoverAnimations.size() - 1; size >= 0; size--) {
            RecoverAnimation recoverAnimation = this.mRecoverAnimations.get(size);
            if (recoverAnimation.mViewHolder.itemView == findChildView) {
                return recoverAnimation;
            }
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public View findChildView(MotionEvent motionEvent) {
        float x = motionEvent.getX();
        float y = motionEvent.getY();
        if (this.mSelected != null) {
            View view = this.mSelected.itemView;
            if (hitTest(view, x, y, this.mSelectedStartX + this.mDx, this.mSelectedStartY + this.mDy)) {
                return view;
            }
        }
        for (int size = this.mRecoverAnimations.size() - 1; size >= 0; size--) {
            RecoverAnimation recoverAnimation = this.mRecoverAnimations.get(size);
            View view2 = recoverAnimation.mViewHolder.itemView;
            if (hitTest(view2, x, y, recoverAnimation.mX, recoverAnimation.mY)) {
                return view2;
            }
        }
        return this.mRecyclerView.findChildViewUnder(x, y);
    }

    private List<RecyclerView.ViewHolder> findSwapTargets(RecyclerView.ViewHolder viewHolder) {
        if (this.mSwapTargets == null) {
            this.mSwapTargets = new ArrayList();
            this.mDistances = new ArrayList();
        } else {
            this.mSwapTargets.clear();
            this.mDistances.clear();
        }
        int boundingBoxMargin = this.mCallback.getBoundingBoxMargin();
        int round = Math.round(this.mSelectedStartX + this.mDx) - boundingBoxMargin;
        int round2 = Math.round(this.mSelectedStartY + this.mDy) - boundingBoxMargin;
        int width = viewHolder.itemView.getWidth() + round + (boundingBoxMargin * 2);
        int height = viewHolder.itemView.getHeight() + round2 + (boundingBoxMargin * 2);
        int i = (round + width) / 2;
        int i2 = (round2 + height) / 2;
        RecyclerView.LayoutManager layoutManager = this.mRecyclerView.getLayoutManager();
        int childCount = layoutManager.getChildCount();
        for (int i3 = 0; i3 < childCount; i3++) {
            View childAt = layoutManager.getChildAt(i3);
            if (childAt != viewHolder.itemView && childAt.getBottom() >= round2 && childAt.getTop() <= height && childAt.getRight() >= round && childAt.getLeft() <= width) {
                RecyclerView.ViewHolder childViewHolder = this.mRecyclerView.getChildViewHolder(childAt);
                if (this.mCallback.canDropOver(this.mRecyclerView, this.mSelected, childViewHolder)) {
                    int abs = Math.abs(i - ((childAt.getLeft() + childAt.getRight()) / 2));
                    int abs2 = Math.abs(i2 - ((childAt.getTop() + childAt.getBottom()) / 2));
                    int i4 = (abs * abs) + (abs2 * abs2);
                    int i5 = 0;
                    int size = this.mSwapTargets.size();
                    for (int i6 = 0; i6 < size && i4 > this.mDistances.get(i6).intValue(); i6++) {
                        i5++;
                    }
                    this.mSwapTargets.add(i5, childViewHolder);
                    this.mDistances.add(i5, Integer.valueOf(i4));
                }
            }
        }
        return this.mSwapTargets;
    }

    private RecyclerView.ViewHolder findSwipedView(MotionEvent motionEvent) {
        View findChildView;
        RecyclerView.LayoutManager layoutManager = this.mRecyclerView.getLayoutManager();
        if (this.mActivePointerId == -1) {
            return null;
        }
        int findPointerIndex = MotionEventCompat.findPointerIndex(motionEvent, this.mActivePointerId);
        float x = MotionEventCompat.getX(motionEvent, findPointerIndex);
        float f = this.mInitialTouchX;
        float y = MotionEventCompat.getY(motionEvent, findPointerIndex);
        float f2 = this.mInitialTouchY;
        float abs = Math.abs(x - f);
        float abs2 = Math.abs(y - f2);
        if (abs >= this.mSlop || abs2 >= this.mSlop) {
            if (abs <= abs2 || !layoutManager.canScrollHorizontally()) {
                if ((abs2 <= abs || !layoutManager.canScrollVertically()) && (findChildView = findChildView(motionEvent)) != null) {
                    return this.mRecyclerView.getChildViewHolder(findChildView);
                }
                return null;
            }
            return null;
        }
        return null;
    }

    private void getSelectedDxDy(float[] fArr) {
        if ((this.mSelectedFlags & 12) != 0) {
            fArr[0] = (this.mSelectedStartX + this.mDx) - this.mSelected.itemView.getLeft();
        } else {
            fArr[0] = ViewCompat.getTranslationX(this.mSelected.itemView);
        }
        if ((this.mSelectedFlags & 3) != 0) {
            fArr[1] = (this.mSelectedStartY + this.mDy) - this.mSelected.itemView.getTop();
        } else {
            fArr[1] = ViewCompat.getTranslationY(this.mSelected.itemView);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean hasRunningRecoverAnim() {
        int size = this.mRecoverAnimations.size();
        for (int i = 0; i < size; i++) {
            if (!this.mRecoverAnimations.get(i).mEnded) {
                return true;
            }
        }
        return false;
    }

    private static boolean hitTest(View view, float f, float f2, float f3, float f4) {
        boolean z = false;
        if (f >= f3) {
            z = false;
            if (f <= view.getWidth() + f3) {
                z = false;
                if (f2 >= f4) {
                    z = false;
                    if (f2 <= view.getHeight() + f4) {
                        z = true;
                    }
                }
            }
        }
        return z;
    }

    private void initGestureDetector() {
        if (this.mGestureDetector != null) {
            return;
        }
        this.mGestureDetector = new GestureDetectorCompat(this.mRecyclerView.getContext(), new ItemTouchHelperGestureListener(this, null));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void moveIfNecessary(RecyclerView.ViewHolder viewHolder) {
        if (!this.mRecyclerView.isLayoutRequested() && this.mActionState == 2) {
            float moveThreshold = this.mCallback.getMoveThreshold(viewHolder);
            int i = (int) (this.mSelectedStartX + this.mDx);
            int i2 = (int) (this.mSelectedStartY + this.mDy);
            if (Math.abs(i2 - viewHolder.itemView.getTop()) >= viewHolder.itemView.getHeight() * moveThreshold || Math.abs(i - viewHolder.itemView.getLeft()) >= viewHolder.itemView.getWidth() * moveThreshold) {
                List<RecyclerView.ViewHolder> findSwapTargets = findSwapTargets(viewHolder);
                if (findSwapTargets.size() == 0) {
                    return;
                }
                RecyclerView.ViewHolder chooseDropTarget = this.mCallback.chooseDropTarget(viewHolder, findSwapTargets, i, i2);
                if (chooseDropTarget == null) {
                    this.mSwapTargets.clear();
                    this.mDistances.clear();
                    return;
                }
                int adapterPosition = chooseDropTarget.getAdapterPosition();
                int adapterPosition2 = viewHolder.getAdapterPosition();
                if (this.mCallback.onMove(this.mRecyclerView, viewHolder, chooseDropTarget)) {
                    this.mCallback.onMoved(this.mRecyclerView, viewHolder, adapterPosition2, chooseDropTarget, adapterPosition, i, i2);
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void obtainVelocityTracker() {
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
        }
        this.mVelocityTracker = VelocityTracker.obtain();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void postDispatchSwipe(RecoverAnimation recoverAnimation, int i) {
        this.mRecyclerView.post(new Runnable(this, recoverAnimation, i) { // from class: android.support.v7.widget.helper.ItemTouchHelper.4
            final ItemTouchHelper this$0;
            final RecoverAnimation val$anim;
            final int val$swipeDir;

            {
                this.this$0 = this;
                this.val$anim = recoverAnimation;
                this.val$swipeDir = i;
            }

            @Override // java.lang.Runnable
            public void run() {
                if (this.this$0.mRecyclerView == null || !this.this$0.mRecyclerView.isAttachedToWindow() || this.val$anim.mOverridden || this.val$anim.mViewHolder.getAdapterPosition() == -1) {
                    return;
                }
                RecyclerView.ItemAnimator itemAnimator = this.this$0.mRecyclerView.getItemAnimator();
                if ((itemAnimator == null || !itemAnimator.isRunning(null)) && !this.this$0.hasRunningRecoverAnim()) {
                    this.this$0.mCallback.onSwiped(this.val$anim.mViewHolder, this.val$swipeDir);
                } else {
                    this.this$0.mRecyclerView.post(this);
                }
            }
        });
    }

    private void releaseVelocityTracker() {
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void removeChildDrawingOrderCallbackIfNecessary(View view) {
        if (view == this.mOverdrawChild) {
            this.mOverdrawChild = null;
            if (this.mChildDrawingOrderCallback != null) {
                this.mRecyclerView.setChildDrawingOrderCallback(null);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean scrollIfNecessary() {
        if (this.mSelected == null) {
            this.mDragScrollStartTimeInMs = Long.MIN_VALUE;
            return false;
        }
        long currentTimeMillis = System.currentTimeMillis();
        long j = this.mDragScrollStartTimeInMs == Long.MIN_VALUE ? 0L : currentTimeMillis - this.mDragScrollStartTimeInMs;
        RecyclerView.LayoutManager layoutManager = this.mRecyclerView.getLayoutManager();
        if (this.mTmpRect == null) {
            this.mTmpRect = new Rect();
        }
        layoutManager.calculateItemDecorationsForChild(this.mSelected.itemView, this.mTmpRect);
        int i = 0;
        if (layoutManager.canScrollHorizontally()) {
            int i2 = (int) (this.mSelectedStartX + this.mDx);
            i = (i2 - this.mTmpRect.left) - this.mRecyclerView.getPaddingLeft();
            if (this.mDx >= 0.0f || i >= 0) {
                i = 0;
                if (this.mDx > 0.0f) {
                    int width = ((this.mSelected.itemView.getWidth() + i2) + this.mTmpRect.right) - (this.mRecyclerView.getWidth() - this.mRecyclerView.getPaddingRight());
                    i = 0;
                    if (width > 0) {
                        i = width;
                    }
                }
            }
        }
        int i3 = 0;
        if (layoutManager.canScrollVertically()) {
            int i4 = (int) (this.mSelectedStartY + this.mDy);
            i3 = (i4 - this.mTmpRect.top) - this.mRecyclerView.getPaddingTop();
            if (this.mDy >= 0.0f || i3 >= 0) {
                i3 = 0;
                if (this.mDy > 0.0f) {
                    int height = ((this.mSelected.itemView.getHeight() + i4) + this.mTmpRect.bottom) - (this.mRecyclerView.getHeight() - this.mRecyclerView.getPaddingBottom());
                    i3 = 0;
                    if (height > 0) {
                        i3 = height;
                    }
                }
            }
        }
        int i5 = i;
        if (i != 0) {
            i5 = this.mCallback.interpolateOutOfBoundsScroll(this.mRecyclerView, this.mSelected.itemView.getWidth(), i, this.mRecyclerView.getWidth(), j);
        }
        int i6 = i3;
        if (i3 != 0) {
            i6 = this.mCallback.interpolateOutOfBoundsScroll(this.mRecyclerView, this.mSelected.itemView.getHeight(), i3, this.mRecyclerView.getHeight(), j);
        }
        if (i5 == 0 && i6 == 0) {
            this.mDragScrollStartTimeInMs = Long.MIN_VALUE;
            return false;
        }
        if (this.mDragScrollStartTimeInMs == Long.MIN_VALUE) {
            this.mDragScrollStartTimeInMs = currentTimeMillis;
        }
        this.mRecyclerView.scrollBy(i5, i6);
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void select(RecyclerView.ViewHolder viewHolder, int i) {
        float f;
        float signum;
        if (viewHolder == this.mSelected && i == this.mActionState) {
            return;
        }
        this.mDragScrollStartTimeInMs = Long.MIN_VALUE;
        int i2 = this.mActionState;
        endRecoverAnimation(viewHolder, true);
        this.mActionState = i;
        if (i == 2) {
            this.mOverdrawChild = viewHolder.itemView;
            addChildDrawingOrderCallback();
        }
        boolean z = false;
        if (this.mSelected != null) {
            RecyclerView.ViewHolder viewHolder2 = this.mSelected;
            if (viewHolder2.itemView.getParent() != null) {
                int swipeIfNecessary = i2 == 2 ? 0 : swipeIfNecessary(viewHolder2);
                releaseVelocityTracker();
                switch (swipeIfNecessary) {
                    case 1:
                    case 2:
                        f = 0.0f;
                        signum = Math.signum(this.mDy) * this.mRecyclerView.getHeight();
                        break;
                    case 4:
                    case 8:
                    case 16:
                    case 32:
                        signum = 0.0f;
                        f = Math.signum(this.mDx) * this.mRecyclerView.getWidth();
                        break;
                    default:
                        f = 0.0f;
                        signum = 0.0f;
                        break;
                }
                int i3 = i2 == 2 ? 8 : swipeIfNecessary > 0 ? 2 : 4;
                getSelectedDxDy(this.mTmpPosition);
                float f2 = this.mTmpPosition[0];
                float f3 = this.mTmpPosition[1];
                RecoverAnimation recoverAnimation = new RecoverAnimation(this, this, viewHolder2, i3, i2, f2, f3, f, signum, swipeIfNecessary, viewHolder2) { // from class: android.support.v7.widget.helper.ItemTouchHelper.3
                    final ItemTouchHelper this$0;
                    final RecyclerView.ViewHolder val$prevSelected;
                    final int val$swipeDir;

                    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
                    {
                        super(this, viewHolder2, i3, i2, f2, f3, f, signum);
                        this.this$0 = this;
                        this.val$swipeDir = swipeIfNecessary;
                        this.val$prevSelected = viewHolder2;
                    }

                    @Override // android.support.v7.widget.helper.ItemTouchHelper.RecoverAnimation, android.support.v4.animation.AnimatorListenerCompat
                    public void onAnimationEnd(ValueAnimatorCompat valueAnimatorCompat) {
                        super.onAnimationEnd(valueAnimatorCompat);
                        if (this.mOverridden) {
                            return;
                        }
                        if (this.val$swipeDir <= 0) {
                            this.this$0.mCallback.clearView(this.this$0.mRecyclerView, this.val$prevSelected);
                        } else {
                            this.this$0.mPendingCleanup.add(this.val$prevSelected.itemView);
                            this.mIsPendingCleanup = true;
                            if (this.val$swipeDir > 0) {
                                this.this$0.postDispatchSwipe(this, this.val$swipeDir);
                            }
                        }
                        if (this.this$0.mOverdrawChild == this.val$prevSelected.itemView) {
                            this.this$0.removeChildDrawingOrderCallbackIfNecessary(this.val$prevSelected.itemView);
                        }
                    }
                };
                recoverAnimation.setDuration(this.mCallback.getAnimationDuration(this.mRecyclerView, i3, f - f2, signum - f3));
                this.mRecoverAnimations.add(recoverAnimation);
                recoverAnimation.start();
                z = true;
            } else {
                removeChildDrawingOrderCallbackIfNecessary(viewHolder2.itemView);
                this.mCallback.clearView(this.mRecyclerView, viewHolder2);
                z = false;
            }
            this.mSelected = null;
        }
        if (viewHolder != null) {
            this.mSelectedFlags = (this.mCallback.getAbsoluteMovementFlags(this.mRecyclerView, viewHolder) & ((1 << ((i * 8) + 8)) - 1)) >> (this.mActionState * 8);
            this.mSelectedStartX = viewHolder.itemView.getLeft();
            this.mSelectedStartY = viewHolder.itemView.getTop();
            this.mSelected = viewHolder;
            if (i == 2) {
                this.mSelected.itemView.performHapticFeedback(0);
            }
        }
        ViewParent parent = this.mRecyclerView.getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(this.mSelected != null);
        }
        if (!z) {
            this.mRecyclerView.getLayoutManager().requestSimpleAnimationsInNextLayout();
        }
        this.mCallback.onSelectedChanged(this.mSelected, this.mActionState);
        this.mRecyclerView.invalidate();
    }

    private void setupCallbacks() {
        this.mSlop = ViewConfiguration.get(this.mRecyclerView.getContext()).getScaledTouchSlop();
        this.mRecyclerView.addItemDecoration(this);
        this.mRecyclerView.addOnItemTouchListener(this.mOnItemTouchListener);
        this.mRecyclerView.addOnChildAttachStateChangeListener(this);
        initGestureDetector();
    }

    private int swipeIfNecessary(RecyclerView.ViewHolder viewHolder) {
        if (this.mActionState == 2) {
            return 0;
        }
        int movementFlags = this.mCallback.getMovementFlags(this.mRecyclerView, viewHolder);
        int convertToAbsoluteDirection = (this.mCallback.convertToAbsoluteDirection(movementFlags, ViewCompat.getLayoutDirection(this.mRecyclerView)) & 65280) >> 8;
        if (convertToAbsoluteDirection == 0) {
            return 0;
        }
        int i = (movementFlags & 65280) >> 8;
        if (Math.abs(this.mDx) > Math.abs(this.mDy)) {
            int checkHorizontalSwipe = checkHorizontalSwipe(viewHolder, convertToAbsoluteDirection);
            if (checkHorizontalSwipe > 0) {
                return (i & checkHorizontalSwipe) == 0 ? Callback.convertToRelativeDirection(checkHorizontalSwipe, ViewCompat.getLayoutDirection(this.mRecyclerView)) : checkHorizontalSwipe;
            }
            int checkVerticalSwipe = checkVerticalSwipe(viewHolder, convertToAbsoluteDirection);
            if (checkVerticalSwipe > 0) {
                return checkVerticalSwipe;
            }
            return 0;
        }
        int checkVerticalSwipe2 = checkVerticalSwipe(viewHolder, convertToAbsoluteDirection);
        if (checkVerticalSwipe2 > 0) {
            return checkVerticalSwipe2;
        }
        int checkHorizontalSwipe2 = checkHorizontalSwipe(viewHolder, convertToAbsoluteDirection);
        if (checkHorizontalSwipe2 > 0) {
            return (i & checkHorizontalSwipe2) == 0 ? Callback.convertToRelativeDirection(checkHorizontalSwipe2, ViewCompat.getLayoutDirection(this.mRecyclerView)) : checkHorizontalSwipe2;
        }
        return 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateDxDy(MotionEvent motionEvent, int i, int i2) {
        float x = MotionEventCompat.getX(motionEvent, i2);
        float y = MotionEventCompat.getY(motionEvent, i2);
        this.mDx = x - this.mInitialTouchX;
        this.mDy = y - this.mInitialTouchY;
        if ((i & 4) == 0) {
            this.mDx = Math.max(0.0f, this.mDx);
        }
        if ((i & 8) == 0) {
            this.mDx = Math.min(0.0f, this.mDx);
        }
        if ((i & 1) == 0) {
            this.mDy = Math.max(0.0f, this.mDy);
        }
        if ((i & 2) == 0) {
            this.mDy = Math.min(0.0f, this.mDy);
        }
    }

    public void attachToRecyclerView(@Nullable RecyclerView recyclerView) {
        if (this.mRecyclerView == recyclerView) {
            return;
        }
        if (this.mRecyclerView != null) {
            destroyCallbacks();
        }
        this.mRecyclerView = recyclerView;
        if (this.mRecyclerView != null) {
            Resources resources = recyclerView.getResources();
            this.mSwipeEscapeVelocity = resources.getDimension(R$dimen.item_touch_helper_swipe_escape_velocity);
            this.mMaxSwipeVelocity = resources.getDimension(R$dimen.item_touch_helper_swipe_escape_max_velocity);
            setupCallbacks();
        }
    }

    @Override // android.support.v7.widget.RecyclerView.ItemDecoration
    public void getItemOffsets(Rect rect, View view, RecyclerView recyclerView, RecyclerView.State state) {
        rect.setEmpty();
    }

    @Override // android.support.v7.widget.RecyclerView.OnChildAttachStateChangeListener
    public void onChildViewAttachedToWindow(View view) {
    }

    @Override // android.support.v7.widget.RecyclerView.OnChildAttachStateChangeListener
    public void onChildViewDetachedFromWindow(View view) {
        removeChildDrawingOrderCallbackIfNecessary(view);
        RecyclerView.ViewHolder childViewHolder = this.mRecyclerView.getChildViewHolder(view);
        if (childViewHolder == null) {
            return;
        }
        if (this.mSelected != null && childViewHolder == this.mSelected) {
            select(null, 0);
            return;
        }
        endRecoverAnimation(childViewHolder, false);
        if (this.mPendingCleanup.remove(childViewHolder.itemView)) {
            this.mCallback.clearView(this.mRecyclerView, childViewHolder);
        }
    }

    @Override // android.support.v7.widget.RecyclerView.ItemDecoration
    public void onDraw(Canvas canvas, RecyclerView recyclerView, RecyclerView.State state) {
        this.mOverdrawChildPosition = -1;
        float f = 0.0f;
        float f2 = 0.0f;
        if (this.mSelected != null) {
            getSelectedDxDy(this.mTmpPosition);
            f = this.mTmpPosition[0];
            f2 = this.mTmpPosition[1];
        }
        this.mCallback.onDraw(canvas, recyclerView, this.mSelected, this.mRecoverAnimations, this.mActionState, f, f2);
    }

    @Override // android.support.v7.widget.RecyclerView.ItemDecoration
    public void onDrawOver(Canvas canvas, RecyclerView recyclerView, RecyclerView.State state) {
        float f = 0.0f;
        float f2 = 0.0f;
        if (this.mSelected != null) {
            getSelectedDxDy(this.mTmpPosition);
            f = this.mTmpPosition[0];
            f2 = this.mTmpPosition[1];
        }
        this.mCallback.onDrawOver(canvas, recyclerView, this.mSelected, this.mRecoverAnimations, this.mActionState, f, f2);
    }

    public void startDrag(RecyclerView.ViewHolder viewHolder) {
        if (!this.mCallback.hasDragFlag(this.mRecyclerView, viewHolder)) {
            Log.e("ItemTouchHelper", "Start drag has been called but swiping is not enabled");
        } else if (viewHolder.itemView.getParent() != this.mRecyclerView) {
            Log.e("ItemTouchHelper", "Start drag has been called with a view holder which is not a child of the RecyclerView which is controlled by this ItemTouchHelper.");
        } else {
            obtainVelocityTracker();
            this.mDy = 0.0f;
            this.mDx = 0.0f;
            select(viewHolder, 2);
        }
    }
}
