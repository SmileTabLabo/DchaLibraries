package com.android.systemui.qs;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
/* loaded from: a.zip:com/android/systemui/qs/DataUsageGraph.class */
public class DataUsageGraph extends View {
    private long mLimitLevel;
    private final int mMarkerWidth;
    private long mMaxLevel;
    private final int mOverlimitColor;
    private final Paint mTmpPaint;
    private final RectF mTmpRect;
    private final int mTrackColor;
    private final int mUsageColor;
    private long mUsageLevel;
    private final int mWarningColor;
    private long mWarningLevel;

    public DataUsageGraph(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mTmpRect = new RectF();
        this.mTmpPaint = new Paint();
        Resources resources = context.getResources();
        this.mTrackColor = context.getColor(2131558531);
        this.mUsageColor = context.getColor(2131558520);
        this.mOverlimitColor = context.getColor(2131558521);
        this.mWarningColor = context.getColor(2131558532);
        this.mMarkerWidth = resources.getDimensionPixelSize(2131689904);
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        RectF rectF = this.mTmpRect;
        Paint paint = this.mTmpPaint;
        int width = getWidth();
        int height = getHeight();
        boolean z = this.mLimitLevel > 0 && this.mUsageLevel > this.mLimitLevel;
        float f = width * (((float) this.mUsageLevel) / ((float) this.mMaxLevel));
        if (z) {
            f = Math.min(Math.max((width * (((float) this.mLimitLevel) / ((float) this.mMaxLevel))) - (this.mMarkerWidth / 2), this.mMarkerWidth), width - (this.mMarkerWidth * 2));
            rectF.set(this.mMarkerWidth + f, 0.0f, width, height);
            paint.setColor(this.mOverlimitColor);
            canvas.drawRect(rectF, paint);
        } else {
            rectF.set(0.0f, 0.0f, width, height);
            paint.setColor(this.mTrackColor);
            canvas.drawRect(rectF, paint);
        }
        rectF.set(0.0f, 0.0f, f, height);
        paint.setColor(this.mUsageColor);
        canvas.drawRect(rectF, paint);
        float min = Math.min(Math.max((width * (((float) this.mWarningLevel) / ((float) this.mMaxLevel))) - (this.mMarkerWidth / 2), 0.0f), width - this.mMarkerWidth);
        rectF.set(min, 0.0f, this.mMarkerWidth + min, height);
        paint.setColor(this.mWarningColor);
        canvas.drawRect(rectF, paint);
    }

    public void setLevels(long j, long j2, long j3) {
        this.mLimitLevel = Math.max(0L, j);
        this.mWarningLevel = Math.max(0L, j2);
        this.mUsageLevel = Math.max(0L, j3);
        this.mMaxLevel = Math.max(Math.max(Math.max(this.mLimitLevel, this.mWarningLevel), this.mUsageLevel), 1L);
        postInvalidate();
    }
}
