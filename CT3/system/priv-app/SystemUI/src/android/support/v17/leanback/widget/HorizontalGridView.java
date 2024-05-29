package android.support.v17.leanback.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.v17.leanback.R$styleable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
/* loaded from: a.zip:android/support/v17/leanback/widget/HorizontalGridView.class */
public class HorizontalGridView extends BaseGridView {
    private boolean mFadingHighEdge;
    private boolean mFadingLowEdge;
    private LinearGradient mHighFadeShader;
    private int mHighFadeShaderLength;
    private int mHighFadeShaderOffset;
    private LinearGradient mLowFadeShader;
    private int mLowFadeShaderLength;
    private int mLowFadeShaderOffset;
    private Bitmap mTempBitmapHigh;
    private Bitmap mTempBitmapLow;
    private Paint mTempPaint;
    private Rect mTempRect;

    public HorizontalGridView(Context context) {
        this(context, null);
    }

    public HorizontalGridView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public HorizontalGridView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mTempPaint = new Paint();
        this.mTempRect = new Rect();
        this.mLayoutManager.setOrientation(0);
        initAttributes(context, attributeSet);
    }

    private Bitmap getTempBitmapHigh() {
        if (this.mTempBitmapHigh == null || this.mTempBitmapHigh.getWidth() != this.mHighFadeShaderLength || this.mTempBitmapHigh.getHeight() != getHeight()) {
            this.mTempBitmapHigh = Bitmap.createBitmap(this.mHighFadeShaderLength, getHeight(), Bitmap.Config.ARGB_8888);
        }
        return this.mTempBitmapHigh;
    }

    private Bitmap getTempBitmapLow() {
        if (this.mTempBitmapLow == null || this.mTempBitmapLow.getWidth() != this.mLowFadeShaderLength || this.mTempBitmapLow.getHeight() != getHeight()) {
            this.mTempBitmapLow = Bitmap.createBitmap(this.mLowFadeShaderLength, getHeight(), Bitmap.Config.ARGB_8888);
        }
        return this.mTempBitmapLow;
    }

    private boolean needsFadingHighEdge() {
        if (this.mFadingHighEdge) {
            for (int childCount = getChildCount() - 1; childCount >= 0; childCount--) {
                if (this.mLayoutManager.getOpticalRight(getChildAt(childCount)) > (getWidth() - getPaddingRight()) + this.mHighFadeShaderOffset) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    private boolean needsFadingLowEdge() {
        if (this.mFadingLowEdge) {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                if (this.mLayoutManager.getOpticalLeft(getChildAt(i)) < getPaddingLeft() - this.mLowFadeShaderOffset) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    private void updateLayerType() {
        if (this.mFadingLowEdge || this.mFadingHighEdge) {
            setLayerType(2, null);
            setWillNotDraw(false);
            return;
        }
        setLayerType(0, null);
        setWillNotDraw(true);
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

    @Override // android.support.v7.widget.RecyclerView, android.view.View
    public void draw(Canvas canvas) {
        boolean needsFadingLowEdge = needsFadingLowEdge();
        boolean needsFadingHighEdge = needsFadingHighEdge();
        if (!needsFadingLowEdge) {
            this.mTempBitmapLow = null;
        }
        if (!needsFadingHighEdge) {
            this.mTempBitmapHigh = null;
        }
        if (!needsFadingLowEdge && !needsFadingHighEdge) {
            super.draw(canvas);
            return;
        }
        int paddingLeft = this.mFadingLowEdge ? (getPaddingLeft() - this.mLowFadeShaderOffset) - this.mLowFadeShaderLength : 0;
        int width = this.mFadingHighEdge ? (getWidth() - getPaddingRight()) + this.mHighFadeShaderOffset + this.mHighFadeShaderLength : getWidth();
        int save = canvas.save();
        canvas.clipRect(paddingLeft + (this.mFadingLowEdge ? this.mLowFadeShaderLength : 0), 0, width - (this.mFadingHighEdge ? this.mHighFadeShaderLength : 0), getHeight());
        super.draw(canvas);
        canvas.restoreToCount(save);
        Canvas canvas2 = new Canvas();
        this.mTempRect.top = 0;
        this.mTempRect.bottom = getHeight();
        if (needsFadingLowEdge && this.mLowFadeShaderLength > 0) {
            Bitmap tempBitmapLow = getTempBitmapLow();
            tempBitmapLow.eraseColor(0);
            canvas2.setBitmap(tempBitmapLow);
            int save2 = canvas2.save();
            canvas2.clipRect(0, 0, this.mLowFadeShaderLength, getHeight());
            canvas2.translate(-paddingLeft, 0.0f);
            super.draw(canvas2);
            canvas2.restoreToCount(save2);
            this.mTempPaint.setShader(this.mLowFadeShader);
            canvas2.drawRect(0.0f, 0.0f, this.mLowFadeShaderLength, getHeight(), this.mTempPaint);
            this.mTempRect.left = 0;
            this.mTempRect.right = this.mLowFadeShaderLength;
            canvas.translate(paddingLeft, 0.0f);
            canvas.drawBitmap(tempBitmapLow, this.mTempRect, this.mTempRect, (Paint) null);
            canvas.translate(-paddingLeft, 0.0f);
        }
        if (!needsFadingHighEdge || this.mHighFadeShaderLength <= 0) {
            return;
        }
        Bitmap tempBitmapHigh = getTempBitmapHigh();
        tempBitmapHigh.eraseColor(0);
        canvas2.setBitmap(tempBitmapHigh);
        int save3 = canvas2.save();
        canvas2.clipRect(0, 0, this.mHighFadeShaderLength, getHeight());
        canvas2.translate(-(width - this.mHighFadeShaderLength), 0.0f);
        super.draw(canvas2);
        canvas2.restoreToCount(save3);
        this.mTempPaint.setShader(this.mHighFadeShader);
        canvas2.drawRect(0.0f, 0.0f, this.mHighFadeShaderLength, getHeight(), this.mTempPaint);
        this.mTempRect.left = 0;
        this.mTempRect.right = this.mHighFadeShaderLength;
        canvas.translate(width - this.mHighFadeShaderLength, 0.0f);
        canvas.drawBitmap(tempBitmapHigh, this.mTempRect, this.mTempRect, (Paint) null);
        canvas.translate(-(width - this.mHighFadeShaderLength), 0.0f);
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
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.lbHorizontalGridView);
        setRowHeight(obtainStyledAttributes);
        setNumRows(obtainStyledAttributes.getInt(R$styleable.lbHorizontalGridView_numberOfRows, 1));
        obtainStyledAttributes.recycle();
        updateLayerType();
        this.mTempPaint = new Paint();
        this.mTempPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
    }

    @Override // android.support.v17.leanback.widget.BaseGridView, android.support.v7.widget.RecyclerView, android.view.ViewGroup
    public /* bridge */ /* synthetic */ boolean onRequestFocusInDescendants(int i, Rect rect) {
        return super.onRequestFocusInDescendants(i, rect);
    }

    @Override // android.support.v17.leanback.widget.BaseGridView, android.view.View
    public /* bridge */ /* synthetic */ void onRtlPropertiesChanged(int i) {
        super.onRtlPropertiesChanged(i);
    }

    @Override // android.support.v17.leanback.widget.BaseGridView
    public /* bridge */ /* synthetic */ void setGravity(int i) {
        super.setGravity(i);
    }

    public void setNumRows(int i) {
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

    public void setRowHeight(int i) {
        this.mLayoutManager.setRowHeight(i);
        requestLayout();
    }

    void setRowHeight(TypedArray typedArray) {
        if (typedArray.peekValue(R$styleable.lbHorizontalGridView_rowHeight) != null) {
            setRowHeight(typedArray.getLayoutDimension(R$styleable.lbHorizontalGridView_rowHeight, 0));
        }
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
