package com.android.settings.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.MathUtils;
import android.view.MotionEvent;
import android.view.View;
import com.android.internal.util.Preconditions;
import com.android.settings.R;
/* loaded from: classes.dex */
public class ChartSweepView extends View {
    private ChartAxis mAxis;
    private View.OnClickListener mClickListener;
    private Rect mContentOffset;
    private long mDragInterval;
    private int mFollowAxis;
    private int mLabelColor;
    private DynamicLayout mLabelLayout;
    private int mLabelMinSize;
    private float mLabelOffset;
    private float mLabelSize;
    private SpannableStringBuilder mLabelTemplate;
    private int mLabelTemplateRes;
    private long mLabelValue;
    private OnSweepListener mListener;
    private Rect mMargins;
    private float mNeighborMargin;
    private ChartSweepView[] mNeighbors;
    private Paint mOutlinePaint;
    private int mSafeRegion;
    private Drawable mSweep;
    private Point mSweepOffset;
    private Rect mSweepPadding;
    private int mTouchMode;
    private MotionEvent mTracking;
    private float mTrackingStart;
    private long mValidAfter;
    private ChartSweepView mValidAfterDynamic;
    private long mValidBefore;
    private ChartSweepView mValidBeforeDynamic;
    private long mValue;

    /* loaded from: classes.dex */
    public interface OnSweepListener {
        void onSweep(ChartSweepView chartSweepView, boolean z);

        void requestEdit(ChartSweepView chartSweepView);
    }

    public ChartSweepView(Context context) {
        this(context, null);
    }

    public ChartSweepView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ChartSweepView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mSweepPadding = new Rect();
        this.mContentOffset = new Rect();
        this.mSweepOffset = new Point();
        this.mMargins = new Rect();
        this.mOutlinePaint = new Paint();
        this.mTouchMode = 0;
        this.mDragInterval = 1L;
        this.mNeighbors = new ChartSweepView[0];
        this.mClickListener = new View.OnClickListener() { // from class: com.android.settings.widget.ChartSweepView.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                ChartSweepView.this.dispatchRequestEdit();
            }
        };
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.ChartSweepView, i, 0);
        int color = obtainStyledAttributes.getColor(1, -16776961);
        setSweepDrawable(obtainStyledAttributes.getDrawable(6), color);
        setFollowAxis(obtainStyledAttributes.getInt(0, -1));
        setNeighborMargin(obtainStyledAttributes.getDimensionPixelSize(4, 0));
        setSafeRegion(obtainStyledAttributes.getDimensionPixelSize(5, 0));
        setLabelMinSize(obtainStyledAttributes.getDimensionPixelSize(2, 0));
        setLabelTemplate(obtainStyledAttributes.getResourceId(3, 0));
        setLabelColor(color);
        setBackgroundResource(R.drawable.data_usage_sweep_background);
        this.mOutlinePaint.setColor(-65536);
        this.mOutlinePaint.setStrokeWidth(1.0f);
        this.mOutlinePaint.setStyle(Paint.Style.STROKE);
        obtainStyledAttributes.recycle();
        setClickable(true);
        setOnClickListener(this.mClickListener);
        setWillNotDraw(false);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void init(ChartAxis chartAxis) {
        this.mAxis = (ChartAxis) Preconditions.checkNotNull(chartAxis, "missing axis");
    }

    public void setNeighbors(ChartSweepView... chartSweepViewArr) {
        this.mNeighbors = chartSweepViewArr;
    }

    public int getFollowAxis() {
        return this.mFollowAxis;
    }

    public Rect getMargins() {
        return this.mMargins;
    }

    public void setDragInterval(long j) {
        this.mDragInterval = j;
    }

    private float getTargetInset() {
        if (this.mFollowAxis == 1) {
            return this.mSweepPadding.top + (((this.mSweep.getIntrinsicHeight() - this.mSweepPadding.top) - this.mSweepPadding.bottom) / 2.0f) + this.mSweepOffset.y;
        }
        return this.mSweepPadding.left + (((this.mSweep.getIntrinsicWidth() - this.mSweepPadding.left) - this.mSweepPadding.right) / 2.0f) + this.mSweepOffset.x;
    }

    public void addOnSweepListener(OnSweepListener onSweepListener) {
        this.mListener = onSweepListener;
    }

    private void dispatchOnSweep(boolean z) {
        if (this.mListener != null) {
            this.mListener.onSweep(this, z);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dispatchRequestEdit() {
        if (this.mListener != null) {
            this.mListener.requestEdit(this);
        }
    }

    @Override // android.view.View
    public void setEnabled(boolean z) {
        super.setEnabled(z);
        setFocusable(z);
        requestLayout();
    }

    public void setSweepDrawable(Drawable drawable, int i) {
        if (this.mSweep != null) {
            this.mSweep.setCallback(null);
            unscheduleDrawable(this.mSweep);
        }
        if (drawable != null) {
            drawable.setCallback(this);
            if (drawable.isStateful()) {
                drawable.setState(getDrawableState());
            }
            drawable.setVisible(getVisibility() == 0, false);
            this.mSweep = drawable;
            this.mSweep.setTint(i);
            drawable.getPadding(this.mSweepPadding);
        } else {
            this.mSweep = null;
        }
        invalidate();
    }

    public void setFollowAxis(int i) {
        this.mFollowAxis = i;
    }

    public void setLabelMinSize(int i) {
        this.mLabelMinSize = i;
        invalidateLabelTemplate();
    }

    public void setLabelTemplate(int i) {
        this.mLabelTemplateRes = i;
        invalidateLabelTemplate();
    }

    public void setLabelColor(int i) {
        this.mLabelColor = i;
        invalidateLabelTemplate();
    }

    private void invalidateLabelTemplate() {
        if (this.mLabelTemplateRes != 0) {
            CharSequence text = getResources().getText(this.mLabelTemplateRes);
            TextPaint textPaint = new TextPaint(1);
            textPaint.density = getResources().getDisplayMetrics().density;
            textPaint.setCompatibilityScaling(getResources().getCompatibilityInfo().applicationScale);
            textPaint.setColor(this.mLabelColor);
            this.mLabelTemplate = new SpannableStringBuilder(text);
            this.mLabelLayout = DynamicLayout.Builder.obtain(this.mLabelTemplate, textPaint, 1024).setAlignment(Layout.Alignment.ALIGN_RIGHT).setIncludePad(false).setUseLineSpacingFromFallbacks(true).build();
            invalidateLabel();
        } else {
            this.mLabelTemplate = null;
            this.mLabelLayout = null;
        }
        invalidate();
        requestLayout();
    }

    private void invalidateLabel() {
        if (this.mLabelTemplate != null && this.mAxis != null) {
            this.mLabelValue = this.mAxis.buildLabel(getResources(), this.mLabelTemplate, this.mValue);
            setContentDescription(this.mLabelTemplate);
            invalidateLabelOffset();
            invalidate();
            return;
        }
        this.mLabelValue = this.mValue;
    }

    public void invalidateLabelOffset() {
        float f = 0.0f;
        if (this.mFollowAxis == 1) {
            if (this.mValidAfterDynamic != null) {
                this.mLabelSize = Math.max(getLabelWidth(this), getLabelWidth(this.mValidAfterDynamic));
                float labelTop = getLabelTop(this.mValidAfterDynamic) - getLabelBottom(this);
                if (labelTop < 0.0f) {
                    f = labelTop / 2.0f;
                }
            } else if (this.mValidBeforeDynamic != null) {
                this.mLabelSize = Math.max(getLabelWidth(this), getLabelWidth(this.mValidBeforeDynamic));
                float labelTop2 = getLabelTop(this) - getLabelBottom(this.mValidBeforeDynamic);
                if (labelTop2 < 0.0f) {
                    f = (-labelTop2) / 2.0f;
                }
            } else {
                this.mLabelSize = getLabelWidth(this);
            }
        }
        this.mLabelSize = Math.max(this.mLabelSize, this.mLabelMinSize);
        if (f != this.mLabelOffset) {
            this.mLabelOffset = f;
            invalidate();
            if (this.mValidAfterDynamic != null) {
                this.mValidAfterDynamic.invalidateLabelOffset();
            }
            if (this.mValidBeforeDynamic != null) {
                this.mValidBeforeDynamic.invalidateLabelOffset();
            }
        }
    }

    @Override // android.view.View
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (this.mSweep != null) {
            this.mSweep.jumpToCurrentState();
        }
    }

    @Override // android.view.View
    public void setVisibility(int i) {
        super.setVisibility(i);
        if (this.mSweep != null) {
            this.mSweep.setVisible(i == 0, false);
        }
    }

    @Override // android.view.View
    protected boolean verifyDrawable(Drawable drawable) {
        return drawable == this.mSweep || super.verifyDrawable(drawable);
    }

    public ChartAxis getAxis() {
        return this.mAxis;
    }

    public void setValue(long j) {
        this.mValue = j;
        invalidateLabel();
    }

    public long getValue() {
        return this.mValue;
    }

    public long getLabelValue() {
        return this.mLabelValue;
    }

    public float getPoint() {
        if (isEnabled()) {
            return this.mAxis.convertToPoint(this.mValue);
        }
        return 0.0f;
    }

    public void setValidRange(long j, long j2) {
        this.mValidAfter = j;
        this.mValidBefore = j2;
    }

    public void setNeighborMargin(float f) {
        this.mNeighborMargin = f;
    }

    public void setSafeRegion(int i) {
        this.mSafeRegion = i;
    }

    public void setValidRangeDynamic(ChartSweepView chartSweepView, ChartSweepView chartSweepView2) {
        this.mValidAfterDynamic = chartSweepView;
        this.mValidBeforeDynamic = chartSweepView2;
    }

    public boolean isTouchCloserTo(MotionEvent motionEvent, ChartSweepView chartSweepView) {
        return chartSweepView.getTouchDistanceFromTarget(motionEvent) < getTouchDistanceFromTarget(motionEvent);
    }

    private float getTouchDistanceFromTarget(MotionEvent motionEvent) {
        if (this.mFollowAxis == 0) {
            return Math.abs(motionEvent.getX() - (getX() + getTargetInset()));
        }
        return Math.abs(motionEvent.getY() - (getY() + getTargetInset()));
    }

    /* JADX WARN: Code restructure failed: missing block: B:37:0x00f8, code lost:
        if (r12.getX() < r11.mLabelLayout.getWidth()) goto L37;
     */
    /* JADX WARN: Code restructure failed: missing block: B:38:0x00fa, code lost:
        r5 = true;
     */
    /* JADX WARN: Code restructure failed: missing block: B:49:0x0129, code lost:
        if (r12.getY() < r11.mLabelLayout.getHeight()) goto L37;
     */
    @Override // android.view.View
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean z;
        boolean z2;
        Rect parentContentRect;
        Rect computeClampRect;
        long convertToValue;
        if (isEnabled()) {
            View view = (View) getParent();
            switch (motionEvent.getAction()) {
                case 0:
                    if (this.mFollowAxis == 1) {
                        z = motionEvent.getX() > ((float) (getWidth() - (this.mSweepPadding.right * 8)));
                        if (this.mLabelLayout != null) {
                            break;
                        }
                        z2 = false;
                    } else {
                        z = motionEvent.getY() > ((float) (getHeight() - (this.mSweepPadding.bottom * 8)));
                        if (this.mLabelLayout != null) {
                            break;
                        }
                        z2 = false;
                    }
                    MotionEvent copy = motionEvent.copy();
                    copy.offsetLocation(getLeft(), getTop());
                    for (ChartSweepView chartSweepView : this.mNeighbors) {
                        if (isTouchCloserTo(copy, chartSweepView)) {
                            return false;
                        }
                    }
                    if (z) {
                        if (this.mFollowAxis == 1) {
                            this.mTrackingStart = getTop() - this.mMargins.top;
                        } else {
                            this.mTrackingStart = getLeft() - this.mMargins.left;
                        }
                        this.mTracking = motionEvent.copy();
                        this.mTouchMode = 1;
                        if (!view.isActivated()) {
                            view.setActivated(true);
                        }
                        return true;
                    } else if (z2) {
                        this.mTouchMode = 2;
                        return true;
                    } else {
                        this.mTouchMode = 0;
                        return false;
                    }
                case 1:
                    if (this.mTouchMode == 2) {
                        performClick();
                    } else if (this.mTouchMode == 1) {
                        this.mTrackingStart = 0.0f;
                        this.mTracking = null;
                        this.mValue = this.mLabelValue;
                        dispatchOnSweep(true);
                        setTranslationX(0.0f);
                        setTranslationY(0.0f);
                        requestLayout();
                    }
                    this.mTouchMode = 0;
                    return true;
                case 2:
                    if (this.mTouchMode == 2) {
                        return true;
                    }
                    getParent().requestDisallowInterceptTouchEvent(true);
                    if (computeClampRect(getParentContentRect()).isEmpty()) {
                        return true;
                    }
                    if (this.mFollowAxis == 1) {
                        float constrain = MathUtils.constrain(this.mTrackingStart + (motionEvent.getRawY() - this.mTracking.getRawY()), computeClampRect.top, computeClampRect.bottom);
                        setTranslationY(constrain - (getTop() - this.mMargins.top));
                        convertToValue = this.mAxis.convertToValue(constrain - parentContentRect.top);
                    } else {
                        float constrain2 = MathUtils.constrain(this.mTrackingStart + (motionEvent.getRawX() - this.mTracking.getRawX()), computeClampRect.left, computeClampRect.right);
                        setTranslationX(constrain2 - (getLeft() - this.mMargins.left));
                        convertToValue = this.mAxis.convertToValue(constrain2 - parentContentRect.left);
                    }
                    setValue(convertToValue - (convertToValue % this.mDragInterval));
                    dispatchOnSweep(false);
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }

    public void updateValueFromPosition() {
        Rect parentContentRect = getParentContentRect();
        if (this.mFollowAxis == 1) {
            setValue(this.mAxis.convertToValue((getY() - this.mMargins.top) - parentContentRect.top));
            return;
        }
        setValue(this.mAxis.convertToValue((getX() - this.mMargins.left) - parentContentRect.left));
    }

    public int shouldAdjustAxis() {
        return this.mAxis.shouldAdjustAxis(getValue());
    }

    private Rect getParentContentRect() {
        View view = (View) getParent();
        return new Rect(view.getPaddingLeft(), view.getPaddingTop(), view.getWidth() - view.getPaddingRight(), view.getHeight() - view.getPaddingBottom());
    }

    @Override // android.view.View
    public void addOnLayoutChangeListener(View.OnLayoutChangeListener onLayoutChangeListener) {
    }

    @Override // android.view.View
    public void removeOnLayoutChangeListener(View.OnLayoutChangeListener onLayoutChangeListener) {
    }

    private long getValidAfterDynamic() {
        ChartSweepView chartSweepView = this.mValidAfterDynamic;
        if (chartSweepView == null || !chartSweepView.isEnabled()) {
            return Long.MIN_VALUE;
        }
        return chartSweepView.getValue();
    }

    private long getValidBeforeDynamic() {
        ChartSweepView chartSweepView = this.mValidBeforeDynamic;
        if (chartSweepView == null || !chartSweepView.isEnabled()) {
            return Long.MAX_VALUE;
        }
        return chartSweepView.getValue();
    }

    private Rect computeClampRect(Rect rect) {
        Rect buildClampRect = buildClampRect(rect, this.mValidAfter, this.mValidBefore, 0.0f);
        if (!buildClampRect.intersect(buildClampRect(rect, getValidAfterDynamic(), getValidBeforeDynamic(), this.mNeighborMargin))) {
            buildClampRect.setEmpty();
        }
        return buildClampRect;
    }

    private Rect buildClampRect(Rect rect, long j, long j2, float f) {
        if (this.mAxis instanceof InvertedChartAxis) {
            j = j2;
            j2 = j;
        }
        boolean z = false;
        boolean z2 = (j == Long.MIN_VALUE || j == Long.MAX_VALUE) ? false : true;
        if (j2 != Long.MIN_VALUE && j2 != Long.MAX_VALUE) {
            z = true;
        }
        float convertToPoint = this.mAxis.convertToPoint(j) + f;
        float convertToPoint2 = this.mAxis.convertToPoint(j2) - f;
        Rect rect2 = new Rect(rect);
        if (this.mFollowAxis == 1) {
            if (z) {
                rect2.bottom = rect2.top + ((int) convertToPoint2);
            }
            if (z2) {
                rect2.top = (int) (rect2.top + convertToPoint);
            }
        } else {
            if (z) {
                rect2.right = rect2.left + ((int) convertToPoint2);
            }
            if (z2) {
                rect2.left = (int) (rect2.left + convertToPoint);
            }
        }
        return rect2;
    }

    @Override // android.view.View
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (this.mSweep.isStateful()) {
            this.mSweep.setState(getDrawableState());
        }
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        if (isEnabled() && this.mLabelLayout != null) {
            int intrinsicHeight = this.mSweep.getIntrinsicHeight();
            int height = this.mLabelLayout.getHeight();
            this.mSweepOffset.x = 0;
            this.mSweepOffset.y = 0;
            this.mSweepOffset.y = (int) ((height / 2) - getTargetInset());
            setMeasuredDimension(this.mSweep.getIntrinsicWidth(), Math.max(intrinsicHeight, height));
        } else {
            this.mSweepOffset.x = 0;
            this.mSweepOffset.y = 0;
            setMeasuredDimension(this.mSweep.getIntrinsicWidth(), this.mSweep.getIntrinsicHeight());
        }
        if (this.mFollowAxis == 1) {
            this.mMargins.top = -(this.mSweepPadding.top + (((this.mSweep.getIntrinsicHeight() - this.mSweepPadding.top) - this.mSweepPadding.bottom) / 2));
            this.mMargins.bottom = 0;
            this.mMargins.left = -this.mSweepPadding.left;
            this.mMargins.right = this.mSweepPadding.right;
        } else {
            this.mMargins.left = -(this.mSweepPadding.left + (((this.mSweep.getIntrinsicWidth() - this.mSweepPadding.left) - this.mSweepPadding.right) / 2));
            this.mMargins.right = 0;
            this.mMargins.top = -this.mSweepPadding.top;
            this.mMargins.bottom = this.mSweepPadding.bottom;
        }
        this.mContentOffset.set(0, 0, 0, 0);
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        if (this.mFollowAxis == 0) {
            int i3 = measuredWidth * 3;
            setMeasuredDimension(i3, measuredHeight);
            this.mContentOffset.left = (i3 - measuredWidth) / 2;
            int i4 = this.mSweepPadding.bottom * 2;
            this.mContentOffset.bottom -= i4;
            this.mMargins.bottom += i4;
        } else {
            int i5 = measuredHeight * 2;
            setMeasuredDimension(measuredWidth, i5);
            this.mContentOffset.offset(0, (i5 - measuredHeight) / 2);
            int i6 = this.mSweepPadding.right * 2;
            this.mContentOffset.right -= i6;
            this.mMargins.right += i6;
        }
        this.mSweepOffset.offset(this.mContentOffset.left, this.mContentOffset.top);
        this.mMargins.offset(-this.mSweepOffset.x, -this.mSweepOffset.y);
    }

    @Override // android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        invalidateLabelOffset();
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        int i;
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        if (isEnabled() && this.mLabelLayout != null) {
            int save = canvas.save();
            canvas.translate(this.mContentOffset.left + (this.mLabelSize - 1024.0f), this.mContentOffset.top + this.mLabelOffset);
            this.mLabelLayout.draw(canvas);
            canvas.restoreToCount(save);
            i = ((int) this.mLabelSize) + this.mSafeRegion;
        } else {
            i = 0;
        }
        if (this.mFollowAxis == 1) {
            this.mSweep.setBounds(i, this.mSweepOffset.y, width + this.mContentOffset.right, this.mSweepOffset.y + this.mSweep.getIntrinsicHeight());
        } else {
            this.mSweep.setBounds(this.mSweepOffset.x, i, this.mSweepOffset.x + this.mSweep.getIntrinsicWidth(), height + this.mContentOffset.bottom);
        }
        this.mSweep.draw(canvas);
    }

    public static float getLabelTop(ChartSweepView chartSweepView) {
        return chartSweepView.getY() + chartSweepView.mContentOffset.top;
    }

    public static float getLabelBottom(ChartSweepView chartSweepView) {
        return getLabelTop(chartSweepView) + chartSweepView.mLabelLayout.getHeight();
    }

    public static float getLabelWidth(ChartSweepView chartSweepView) {
        return Layout.getDesiredWidth(chartSweepView.mLabelLayout.getText(), chartSweepView.mLabelLayout.getPaint());
    }
}
