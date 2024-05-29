package com.android.settingslib.graph;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.View;
import com.android.settingslib.R$color;
import com.android.settingslib.R$dimen;
/* loaded from: a.zip:com/android/settingslib/graph/UsageGraph.class */
public class UsageGraph extends View {
    private int mAccentColor;
    private final int mCornerRadius;
    private final Drawable mDivider;
    private final int mDividerSize;
    private final Paint mDottedPaint;
    private final Paint mFillPaint;
    private final Paint mLinePaint;
    private final SparseIntArray mLocalPaths;
    private float mMaxX;
    private float mMaxY;
    private float mMiddleDividerLoc;
    private int mMiddleDividerTint;
    private final Path mPath;
    private final SparseIntArray mPaths;
    private boolean mProjectUp;
    private boolean mShowProjection;
    private final Drawable mTintedDivider;
    private int mTopDividerTint;

    public UsageGraph(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mPath = new Path();
        this.mPaths = new SparseIntArray();
        this.mLocalPaths = new SparseIntArray();
        this.mMaxX = 100.0f;
        this.mMaxY = 100.0f;
        this.mMiddleDividerLoc = 0.5f;
        this.mMiddleDividerTint = -1;
        this.mTopDividerTint = -1;
        Resources resources = context.getResources();
        this.mLinePaint = new Paint();
        this.mLinePaint.setStyle(Paint.Style.STROKE);
        this.mLinePaint.setStrokeCap(Paint.Cap.ROUND);
        this.mLinePaint.setStrokeJoin(Paint.Join.ROUND);
        this.mLinePaint.setAntiAlias(true);
        this.mCornerRadius = resources.getDimensionPixelSize(R$dimen.usage_graph_line_corner_radius);
        this.mLinePaint.setPathEffect(new CornerPathEffect(this.mCornerRadius));
        this.mLinePaint.setStrokeWidth(resources.getDimensionPixelSize(R$dimen.usage_graph_line_width));
        this.mFillPaint = new Paint(this.mLinePaint);
        this.mFillPaint.setStyle(Paint.Style.FILL);
        this.mDottedPaint = new Paint(this.mLinePaint);
        this.mDottedPaint.setStyle(Paint.Style.STROKE);
        float dimensionPixelSize = resources.getDimensionPixelSize(R$dimen.usage_graph_dot_size);
        float dimensionPixelSize2 = resources.getDimensionPixelSize(R$dimen.usage_graph_dot_interval);
        this.mDottedPaint.setStrokeWidth(3.0f * dimensionPixelSize);
        this.mDottedPaint.setPathEffect(new DashPathEffect(new float[]{dimensionPixelSize, dimensionPixelSize2}, 0.0f));
        this.mDottedPaint.setColor(context.getColor(R$color.usage_graph_dots));
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(16843284, typedValue, true);
        this.mDivider = context.getDrawable(typedValue.resourceId);
        this.mTintedDivider = context.getDrawable(typedValue.resourceId);
        this.mDividerSize = resources.getDimensionPixelSize(R$dimen.usage_graph_divider_size);
    }

    private void calculateLocalPaths() {
        if (getWidth() == 0) {
            return;
        }
        this.mLocalPaths.clear();
        int i = 0;
        int i2 = -1;
        for (int i3 = 0; i3 < this.mPaths.size(); i3++) {
            int keyAt = this.mPaths.keyAt(i3);
            int valueAt = this.mPaths.valueAt(i3);
            if (valueAt == -1) {
                if (i3 == this.mPaths.size() - 1 && i2 != -1) {
                    this.mLocalPaths.put(i, i2);
                }
                i2 = -1;
                this.mLocalPaths.put(i + 1, -1);
            } else {
                int x = getX(keyAt);
                int y = getY(valueAt);
                i = x;
                if (this.mLocalPaths.size() > 0) {
                    int keyAt2 = this.mLocalPaths.keyAt(this.mLocalPaths.size() - 1);
                    int valueAt2 = this.mLocalPaths.valueAt(this.mLocalPaths.size() - 1);
                    if (valueAt2 != -1 && !hasDiff(keyAt2, x) && !hasDiff(valueAt2, y)) {
                        i2 = y;
                    }
                }
                this.mLocalPaths.put(x, y);
            }
        }
    }

    private void drawDivider(int i, Canvas canvas, int i2) {
        Drawable drawable = this.mDivider;
        if (i2 != -1) {
            this.mTintedDivider.setTint(i2);
            drawable = this.mTintedDivider;
        }
        drawable.setBounds(0, i, canvas.getWidth(), this.mDividerSize + i);
        drawable.draw(canvas);
    }

    private void drawFilledPath(Canvas canvas) {
        this.mPath.reset();
        float keyAt = this.mLocalPaths.keyAt(0);
        this.mPath.moveTo(this.mLocalPaths.keyAt(0), this.mLocalPaths.valueAt(0));
        int i = 1;
        while (i < this.mLocalPaths.size()) {
            int keyAt2 = this.mLocalPaths.keyAt(i);
            int valueAt = this.mLocalPaths.valueAt(i);
            if (valueAt == -1) {
                this.mPath.lineTo(this.mLocalPaths.keyAt(i - 1), getHeight());
                this.mPath.lineTo(keyAt, getHeight());
                this.mPath.close();
                int i2 = i + 1;
                i = i2;
                if (i2 < this.mLocalPaths.size()) {
                    keyAt = this.mLocalPaths.keyAt(i2);
                    this.mPath.moveTo(this.mLocalPaths.keyAt(i2), this.mLocalPaths.valueAt(i2));
                    i = i2;
                }
            } else {
                this.mPath.lineTo(keyAt2, valueAt);
            }
            i++;
        }
        canvas.drawPath(this.mPath, this.mFillPaint);
    }

    private void drawLinePath(Canvas canvas) {
        this.mPath.reset();
        this.mPath.moveTo(this.mLocalPaths.keyAt(0), this.mLocalPaths.valueAt(0));
        int i = 1;
        while (i < this.mLocalPaths.size()) {
            int keyAt = this.mLocalPaths.keyAt(i);
            int valueAt = this.mLocalPaths.valueAt(i);
            if (valueAt == -1) {
                int i2 = i + 1;
                i = i2;
                if (i2 < this.mLocalPaths.size()) {
                    this.mPath.moveTo(this.mLocalPaths.keyAt(i2), this.mLocalPaths.valueAt(i2));
                    i = i2;
                }
            } else {
                this.mPath.lineTo(keyAt, valueAt);
            }
            i++;
        }
        canvas.drawPath(this.mPath, this.mLinePaint);
    }

    private void drawProjection(Canvas canvas) {
        this.mPath.reset();
        this.mPath.moveTo(this.mLocalPaths.keyAt(this.mLocalPaths.size() - 2), this.mLocalPaths.valueAt(this.mLocalPaths.size() - 2));
        this.mPath.lineTo(canvas.getWidth(), this.mProjectUp ? 0 : canvas.getHeight());
        canvas.drawPath(this.mPath, this.mDottedPaint);
    }

    private int getColor(int i, float f) {
        return ((((int) (255.0f * f)) << 24) | 16777215) & i;
    }

    private int getX(float f) {
        return (int) ((f / this.mMaxX) * getWidth());
    }

    private int getY(float f) {
        return (int) (getHeight() * (1.0f - (f / this.mMaxY)));
    }

    private boolean hasDiff(int i, int i2) {
        return Math.abs(i2 - i) >= this.mCornerRadius;
    }

    private void updateGradient() {
        this.mFillPaint.setShader(new LinearGradient(0.0f, 0.0f, 0.0f, getHeight(), getColor(this.mAccentColor, 0.2f), 0, Shader.TileMode.CLAMP));
    }

    public void addPath(SparseIntArray sparseIntArray) {
        for (int i = 0; i < sparseIntArray.size(); i++) {
            this.mPaths.put(sparseIntArray.keyAt(i), sparseIntArray.valueAt(i));
        }
        this.mPaths.put(sparseIntArray.keyAt(sparseIntArray.size() - 1) + 1, -1);
        calculateLocalPaths();
        postInvalidate();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void clearPaths() {
        this.mPaths.clear();
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        if (this.mMiddleDividerLoc != 0.0f) {
            drawDivider(0, canvas, this.mTopDividerTint);
        }
        drawDivider((int) ((canvas.getHeight() - this.mDividerSize) * this.mMiddleDividerLoc), canvas, this.mMiddleDividerTint);
        drawDivider(canvas.getHeight() - this.mDividerSize, canvas, -1);
        if (this.mLocalPaths.size() == 0) {
            return;
        }
        if (this.mShowProjection) {
            drawProjection(canvas);
        }
        drawFilledPath(canvas);
        drawLinePath(canvas);
    }

    @Override // android.view.View
    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        updateGradient();
        calculateLocalPaths();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setAccentColor(int i) {
        this.mAccentColor = i;
        this.mLinePaint.setColor(this.mAccentColor);
        updateGradient();
        postInvalidate();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setMax(int i, int i2) {
        this.mMaxX = i;
        this.mMaxY = i2;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setShowProjection(boolean z, boolean z2) {
        this.mShowProjection = z;
        this.mProjectUp = z2;
        postInvalidate();
    }
}
