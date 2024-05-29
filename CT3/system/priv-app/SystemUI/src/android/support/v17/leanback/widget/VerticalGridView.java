package android.support.v17.leanback.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.v17.leanback.R$styleable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
/* loaded from: a.zip:android/support/v17/leanback/widget/VerticalGridView.class */
public class VerticalGridView extends BaseGridView {
    public VerticalGridView(Context context) {
        this(context, null);
    }

    public VerticalGridView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public VerticalGridView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mLayoutManager.setOrientation(1);
        initAttributes(context, attributeSet);
    }

    @Override // android.support.v17.leanback.widget.BaseGridView, android.view.ViewGroup, android.view.View
    public /* bridge */ /* synthetic */ boolean dispatchGenericFocusedEvent(MotionEvent motionEvent) {
        return super.dispatchGenericFocusedEvent(motionEvent);
    }

    @Override // android.support.v17.leanback.widget.BaseGridView, android.view.ViewGroup, android.view.View
    public /* bridge */ /* synthetic */ boolean dispatchKeyEvent(KeyEvent keyEvent) {
        return super.dispatchKeyEvent(keyEvent);
    }

    @Override // android.support.v17.leanback.widget.BaseGridView, android.view.ViewGroup, android.view.View
    public /* bridge */ /* synthetic */ boolean dispatchTouchEvent(MotionEvent motionEvent) {
        return super.dispatchTouchEvent(motionEvent);
    }

    @Override // android.support.v17.leanback.widget.BaseGridView, android.view.View
    public /* bridge */ /* synthetic */ View focusSearch(int i) {
        return super.focusSearch(i);
    }

    @Override // android.support.v17.leanback.widget.BaseGridView, android.support.v7.widget.RecyclerView, android.view.ViewGroup
    public /* bridge */ /* synthetic */ int getChildDrawingOrder(int i, int i2) {
        return super.getChildDrawingOrder(i, i2);
    }

    @Override // android.support.v17.leanback.widget.BaseGridView
    public /* bridge */ /* synthetic */ int getSelectedPosition() {
        return super.getSelectedPosition();
    }

    @Override // android.support.v17.leanback.widget.BaseGridView, android.view.View
    public /* bridge */ /* synthetic */ boolean hasOverlappingRendering() {
        return super.hasOverlappingRendering();
    }

    protected void initAttributes(Context context, AttributeSet attributeSet) {
        initBaseGridViewAttributes(context, attributeSet);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.lbVerticalGridView);
        setColumnWidth(obtainStyledAttributes);
        setNumColumns(obtainStyledAttributes.getInt(R$styleable.lbVerticalGridView_numberOfColumns, 1));
        obtainStyledAttributes.recycle();
    }

    @Override // android.support.v17.leanback.widget.BaseGridView, android.support.v7.widget.RecyclerView, android.view.ViewGroup
    public /* bridge */ /* synthetic */ boolean onRequestFocusInDescendants(int i, Rect rect) {
        return super.onRequestFocusInDescendants(i, rect);
    }

    @Override // android.support.v17.leanback.widget.BaseGridView, android.view.View
    public /* bridge */ /* synthetic */ void onRtlPropertiesChanged(int i) {
        super.onRtlPropertiesChanged(i);
    }

    public void setColumnWidth(int i) {
        this.mLayoutManager.setRowHeight(i);
        requestLayout();
    }

    void setColumnWidth(TypedArray typedArray) {
        if (typedArray.peekValue(R$styleable.lbVerticalGridView_columnWidth) != null) {
            setColumnWidth(typedArray.getLayoutDimension(R$styleable.lbVerticalGridView_columnWidth, 0));
        }
    }

    @Override // android.support.v17.leanback.widget.BaseGridView
    public /* bridge */ /* synthetic */ void setGravity(int i) {
        super.setGravity(i);
    }

    public void setNumColumns(int i) {
        this.mLayoutManager.setNumRows(i);
        requestLayout();
    }

    @Override // android.support.v17.leanback.widget.BaseGridView
    public /* bridge */ /* synthetic */ void setOnChildViewHolderSelectedListener(OnChildViewHolderSelectedListener onChildViewHolderSelectedListener) {
        super.setOnChildViewHolderSelectedListener(onChildViewHolderSelectedListener);
    }

    @Override // android.support.v17.leanback.widget.BaseGridView, android.support.v7.widget.RecyclerView
    public /* bridge */ /* synthetic */ void setRecyclerListener(RecyclerView.RecyclerListener recyclerListener) {
        super.setRecyclerListener(recyclerListener);
    }

    @Override // android.support.v17.leanback.widget.BaseGridView
    public /* bridge */ /* synthetic */ void setSelectedPosition(int i) {
        super.setSelectedPosition(i);
    }

    @Override // android.support.v17.leanback.widget.BaseGridView
    public /* bridge */ /* synthetic */ void setSelectedPositionSmooth(int i) {
        super.setSelectedPositionSmooth(i);
    }

    @Override // android.support.v17.leanback.widget.BaseGridView
    public /* bridge */ /* synthetic */ void setWindowAlignment(int i) {
        super.setWindowAlignment(i);
    }
}
