package com.android.launcher3;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.android.launcher3.compat.PackageInstallerCompat;
/* loaded from: a.zip:com/android/launcher3/BaseRecyclerView.class */
public abstract class BaseRecyclerView extends RecyclerView implements RecyclerView.OnItemTouchListener {
    protected Rect mBackgroundPadding;
    private float mDeltaThreshold;
    private int mDownX;
    private int mDownY;
    int mDy;
    private int mLastY;
    protected BaseRecyclerViewFastScrollBar mScrollbar;

    /* loaded from: a.zip:com/android/launcher3/BaseRecyclerView$ScrollListener.class */
    private class ScrollListener extends RecyclerView.OnScrollListener {
        final BaseRecyclerView this$0;

        public ScrollListener(BaseRecyclerView baseRecyclerView) {
            this.this$0 = baseRecyclerView;
        }

        @Override // android.support.v7.widget.RecyclerView.OnScrollListener
        public void onScrolled(RecyclerView recyclerView, int i, int i2) {
            this.this$0.mDy = i2;
            this.this$0.onUpdateScrollbar(i2);
        }
    }

    /* loaded from: a.zip:com/android/launcher3/BaseRecyclerView$ScrollPositionState.class */
    public static class ScrollPositionState {
        public int itemPos;
        public int rowIndex;
        public int rowTopOffset;
    }

    public BaseRecyclerView(Context context) {
        this(context, null);
    }

    public BaseRecyclerView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public BaseRecyclerView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mDy = 0;
        this.mBackgroundPadding = new Rect();
        this.mDeltaThreshold = getResources().getDisplayMetrics().density * 4.0f;
        this.mScrollbar = new BaseRecyclerViewFastScrollBar(this, getResources());
        setOnScrollListener(new ScrollListener(this));
    }

    private boolean handleTouchEvent(MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        int x = (int) motionEvent.getX();
        int y = (int) motionEvent.getY();
        switch (action) {
            case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                this.mDownX = x;
                this.mLastY = y;
                this.mDownY = y;
                if (shouldStopScroll(motionEvent)) {
                    stopScroll();
                }
                this.mScrollbar.handleTouchEvent(motionEvent, this.mDownX, this.mDownY, this.mLastY);
                break;
            case 1:
            case 3:
                onFastScrollCompleted();
                this.mScrollbar.handleTouchEvent(motionEvent, this.mDownX, this.mDownY, this.mLastY);
                break;
            case 2:
                this.mLastY = y;
                this.mScrollbar.handleTouchEvent(motionEvent, this.mDownX, this.mDownY, this.mLastY);
                break;
        }
        return this.mScrollbar.isDraggingThumb();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.ViewGroup, android.view.View
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        onUpdateScrollbar(0);
        this.mScrollbar.draw(canvas);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public int getAvailableScrollBarHeight() {
        return getVisibleHeight() - this.mScrollbar.getThumbHeight();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public int getAvailableScrollHeight(int i) {
        return ((getPaddingTop() + getTop(i)) + getPaddingBottom()) - getVisibleHeight();
    }

    public Rect getBackgroundPadding() {
        return this.mBackgroundPadding;
    }

    public int getFastScrollerThumbInactiveColor(int i) {
        return i;
    }

    public int getFastScrollerTrackColor(int i) {
        return i;
    }

    public int getMaxScrollbarWidth() {
        return this.mScrollbar.getThumbMaxWidth();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public int getScrollTop(ScrollPositionState scrollPositionState) {
        return (getPaddingTop() + getTop(scrollPositionState.rowIndex)) - scrollPositionState.rowTopOffset;
    }

    protected abstract int getTop(int i);

    protected int getVisibleHeight() {
        return (getHeight() - this.mBackgroundPadding.top) - this.mBackgroundPadding.bottom;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onFastScrollCompleted() {
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        addOnItemTouchListener(this);
    }

    @Override // android.support.v7.widget.RecyclerView.OnItemTouchListener
    public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
        return handleTouchEvent(motionEvent);
    }

    @Override // android.support.v7.widget.RecyclerView.OnItemTouchListener
    public void onRequestDisallowInterceptTouchEvent(boolean z) {
    }

    @Override // android.support.v7.widget.RecyclerView.OnItemTouchListener
    public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
        handleTouchEvent(motionEvent);
    }

    protected abstract void onUpdateScrollbar(int i);

    public void reset() {
        this.mScrollbar.reattachThumbToScroll();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public abstract String scrollToPositionAtProgress(float f);

    protected boolean shouldStopScroll(MotionEvent motionEvent) {
        return motionEvent.getAction() == 0 && ((float) Math.abs(this.mDy)) < this.mDeltaThreshold && getScrollState() != 0;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean supportsFastScrolling() {
        return true;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void synchronizeScrollBarThumbOffsetToViewScroll(ScrollPositionState scrollPositionState, int i) {
        int availableScrollBarHeight = getAvailableScrollBarHeight();
        int availableScrollHeight = getAvailableScrollHeight(i);
        if (availableScrollHeight <= 0) {
            this.mScrollbar.setThumbOffset(-1, -1);
            return;
        }
        int scrollTop = getScrollTop(scrollPositionState);
        this.mScrollbar.setThumbOffset(Utilities.isRtl(getResources()) ? this.mBackgroundPadding.left : (getWidth() - this.mBackgroundPadding.right) - this.mScrollbar.getThumbWidth(), this.mBackgroundPadding.top + ((int) ((scrollTop / availableScrollHeight) * availableScrollBarHeight)));
    }

    public void updateBackgroundPadding(Rect rect) {
        this.mBackgroundPadding.set(rect);
    }
}
