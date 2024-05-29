package android.support.v17.leanback.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.v17.leanback.R$styleable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: a.zip:android/support/v17/leanback/widget/BaseGridView.class */
public abstract class BaseGridView extends RecyclerView {
    private boolean mAnimateChildLayout;
    private RecyclerView.RecyclerListener mChainedRecyclerListener;
    private boolean mHasOverlappingRendering;
    final GridLayoutManager mLayoutManager;
    private OnKeyInterceptListener mOnKeyInterceptListener;
    private OnMotionInterceptListener mOnMotionInterceptListener;
    private OnTouchInterceptListener mOnTouchInterceptListener;
    private OnUnhandledKeyListener mOnUnhandledKeyListener;

    /* loaded from: a.zip:android/support/v17/leanback/widget/BaseGridView$OnKeyInterceptListener.class */
    public interface OnKeyInterceptListener {
        boolean onInterceptKeyEvent(KeyEvent keyEvent);
    }

    /* loaded from: a.zip:android/support/v17/leanback/widget/BaseGridView$OnMotionInterceptListener.class */
    public interface OnMotionInterceptListener {
        boolean onInterceptMotionEvent(MotionEvent motionEvent);
    }

    /* loaded from: a.zip:android/support/v17/leanback/widget/BaseGridView$OnTouchInterceptListener.class */
    public interface OnTouchInterceptListener {
        boolean onInterceptTouchEvent(MotionEvent motionEvent);
    }

    /* loaded from: a.zip:android/support/v17/leanback/widget/BaseGridView$OnUnhandledKeyListener.class */
    public interface OnUnhandledKeyListener {
        boolean onUnhandledKey(KeyEvent keyEvent);
    }

    public BaseGridView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mAnimateChildLayout = true;
        this.mHasOverlappingRendering = true;
        this.mLayoutManager = new GridLayoutManager(this);
        setLayoutManager(this.mLayoutManager);
        setDescendantFocusability(262144);
        setHasFixedSize(true);
        setChildrenDrawingOrderEnabled(true);
        setWillNotDraw(true);
        setOverScrollMode(2);
        ((SimpleItemAnimator) getItemAnimator()).setSupportsChangeAnimations(false);
        super.setRecyclerListener(new RecyclerView.RecyclerListener(this) { // from class: android.support.v17.leanback.widget.BaseGridView.1
            final BaseGridView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.support.v7.widget.RecyclerView.RecyclerListener
            public void onViewRecycled(RecyclerView.ViewHolder viewHolder) {
                this.this$0.mLayoutManager.onChildRecycled(viewHolder);
                if (this.this$0.mChainedRecyclerListener != null) {
                    this.this$0.mChainedRecyclerListener.onViewRecycled(viewHolder);
                }
            }
        });
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchGenericFocusedEvent(MotionEvent motionEvent) {
        if (this.mOnMotionInterceptListener == null || !this.mOnMotionInterceptListener.onInterceptMotionEvent(motionEvent)) {
            return super.dispatchGenericFocusedEvent(motionEvent);
        }
        return true;
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        if ((this.mOnKeyInterceptListener == null || !this.mOnKeyInterceptListener.onInterceptKeyEvent(keyEvent)) && !super.dispatchKeyEvent(keyEvent)) {
            return this.mOnUnhandledKeyListener != null && this.mOnUnhandledKeyListener.onUnhandledKey(keyEvent);
        }
        return true;
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (this.mOnTouchInterceptListener == null || !this.mOnTouchInterceptListener.onInterceptTouchEvent(motionEvent)) {
            return super.dispatchTouchEvent(motionEvent);
        }
        return true;
    }

    @Override // android.view.View
    public View focusSearch(int i) {
        View findViewByPosition;
        return (!isFocused() || (findViewByPosition = this.mLayoutManager.findViewByPosition(this.mLayoutManager.getSelection())) == null) ? super.focusSearch(i) : focusSearch(findViewByPosition, i);
    }

    @Override // android.support.v7.widget.RecyclerView, android.view.ViewGroup
    public int getChildDrawingOrder(int i, int i2) {
        return this.mLayoutManager.getChildDrawingOrder(this, i, i2);
    }

    public int getSelectedPosition() {
        return this.mLayoutManager.getSelection();
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return this.mHasOverlappingRendering;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void initBaseGridViewAttributes(Context context, AttributeSet attributeSet) {
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.lbBaseGridView);
        this.mLayoutManager.setFocusOutAllowed(obtainStyledAttributes.getBoolean(R$styleable.lbBaseGridView_focusOutFront, false), obtainStyledAttributes.getBoolean(R$styleable.lbBaseGridView_focusOutEnd, false));
        this.mLayoutManager.setFocusOutSideAllowed(obtainStyledAttributes.getBoolean(R$styleable.lbBaseGridView_focusOutSideStart, true), obtainStyledAttributes.getBoolean(R$styleable.lbBaseGridView_focusOutSideEnd, true));
        this.mLayoutManager.setVerticalMargin(obtainStyledAttributes.getDimensionPixelSize(R$styleable.lbBaseGridView_verticalMargin, 0));
        this.mLayoutManager.setHorizontalMargin(obtainStyledAttributes.getDimensionPixelSize(R$styleable.lbBaseGridView_horizontalMargin, 0));
        if (obtainStyledAttributes.hasValue(R$styleable.lbBaseGridView_android_gravity)) {
            setGravity(obtainStyledAttributes.getInt(R$styleable.lbBaseGridView_android_gravity, 0));
        }
        obtainStyledAttributes.recycle();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final boolean isChildrenDrawingOrderEnabledInternal() {
        return isChildrenDrawingOrderEnabled();
    }

    @Override // android.view.View
    protected void onFocusChanged(boolean z, int i, Rect rect) {
        super.onFocusChanged(z, i, rect);
        this.mLayoutManager.onFocusChanged(z, i, rect);
    }

    @Override // android.support.v7.widget.RecyclerView, android.view.ViewGroup
    public boolean onRequestFocusInDescendants(int i, Rect rect) {
        return this.mLayoutManager.gridOnRequestFocusInDescendants(this, i, rect);
    }

    @Override // android.view.View
    public void onRtlPropertiesChanged(int i) {
        this.mLayoutManager.onRtlPropertiesChanged(i);
    }

    public void setGravity(int i) {
        this.mLayoutManager.setGravity(i);
        requestLayout();
    }

    public void setOnChildViewHolderSelectedListener(OnChildViewHolderSelectedListener onChildViewHolderSelectedListener) {
        this.mLayoutManager.setOnChildViewHolderSelectedListener(onChildViewHolderSelectedListener);
    }

    @Override // android.support.v7.widget.RecyclerView
    public void setRecyclerListener(RecyclerView.RecyclerListener recyclerListener) {
        this.mChainedRecyclerListener = recyclerListener;
    }

    public void setSelectedPosition(int i) {
        this.mLayoutManager.setSelection(i, 0);
    }

    public void setSelectedPositionSmooth(int i) {
        this.mLayoutManager.setSelectionSmooth(i);
    }

    public void setWindowAlignment(int i) {
        this.mLayoutManager.setWindowAlignment(i);
        requestLayout();
    }
}
