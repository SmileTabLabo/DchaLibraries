package com.android.settings.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.NetworkStatsHistory;
import android.util.AttributeSet;
import android.view.View;
import com.android.internal.util.Preconditions;
import com.android.settings.R;
/* loaded from: classes.dex */
public class ChartNetworkSeriesView extends View {
    private long mEnd;
    private long mEndTime;
    private boolean mEstimateVisible;
    private ChartAxis mHoriz;
    private long mMax;
    private long mMaxEstimate;
    private Paint mPaintEstimate;
    private Paint mPaintFill;
    private Paint mPaintFillSecondary;
    private Paint mPaintStroke;
    private Path mPathEstimate;
    private Path mPathFill;
    private Path mPathStroke;
    private boolean mPathValid;
    private int mSafeRegion;
    private boolean mSecondary;
    private long mStart;
    private NetworkStatsHistory mStats;
    private ChartAxis mVert;

    public ChartNetworkSeriesView(Context context) {
        this(context, null, 0);
    }

    public ChartNetworkSeriesView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ChartNetworkSeriesView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mEndTime = Long.MIN_VALUE;
        this.mPathValid = false;
        this.mEstimateVisible = false;
        this.mSecondary = false;
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.ChartNetworkSeriesView, i, 0);
        int color = obtainStyledAttributes.getColor(3, -65536);
        int color2 = obtainStyledAttributes.getColor(0, -65536);
        int color3 = obtainStyledAttributes.getColor(1, -65536);
        int dimensionPixelSize = obtainStyledAttributes.getDimensionPixelSize(2, 0);
        setChartColor(color, color2, color3);
        setSafeRegion(dimensionPixelSize);
        setWillNotDraw(false);
        obtainStyledAttributes.recycle();
        this.mPathStroke = new Path();
        this.mPathFill = new Path();
        this.mPathEstimate = new Path();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void init(ChartAxis chartAxis, ChartAxis chartAxis2) {
        this.mHoriz = (ChartAxis) Preconditions.checkNotNull(chartAxis, "missing horiz");
        this.mVert = (ChartAxis) Preconditions.checkNotNull(chartAxis2, "missing vert");
    }

    public void setChartColor(int i, int i2, int i3) {
        this.mPaintStroke = new Paint();
        this.mPaintStroke.setStrokeWidth(4.0f * getResources().getDisplayMetrics().density);
        this.mPaintStroke.setColor(i);
        this.mPaintStroke.setStyle(Paint.Style.STROKE);
        this.mPaintStroke.setAntiAlias(true);
        this.mPaintFill = new Paint();
        this.mPaintFill.setColor(i2);
        this.mPaintFill.setStyle(Paint.Style.FILL);
        this.mPaintFill.setAntiAlias(true);
        this.mPaintFillSecondary = new Paint();
        this.mPaintFillSecondary.setColor(i3);
        this.mPaintFillSecondary.setStyle(Paint.Style.FILL);
        this.mPaintFillSecondary.setAntiAlias(true);
        this.mPaintEstimate = new Paint();
        this.mPaintEstimate.setStrokeWidth(3.0f);
        this.mPaintEstimate.setColor(i3);
        this.mPaintEstimate.setStyle(Paint.Style.STROKE);
        this.mPaintEstimate.setAntiAlias(true);
        this.mPaintEstimate.setPathEffect(new DashPathEffect(new float[]{10.0f, 10.0f}, 1.0f));
    }

    public void setSafeRegion(int i) {
        this.mSafeRegion = i;
    }

    public void setSecondary(boolean z) {
        this.mSecondary = z;
    }

    public void invalidatePath() {
        this.mPathValid = false;
        this.mMax = 0L;
        invalidate();
    }

    private void generatePath() {
        int i;
        long j = 0;
        this.mMax = 0L;
        this.mPathStroke.reset();
        this.mPathFill.reset();
        this.mPathEstimate.reset();
        this.mPathValid = true;
        if (this.mStats == null || this.mStats.size() < 2) {
            return;
        }
        getWidth();
        float height = getHeight();
        long convertToValue = this.mHoriz.convertToValue(0.0f);
        this.mPathStroke.moveTo(0.0f, height);
        this.mPathFill.moveTo(0.0f, height);
        NetworkStatsHistory.Entry entry = null;
        int indexBefore = this.mStats.getIndexBefore(this.mStart);
        int indexAfter = this.mStats.getIndexAfter(this.mEnd);
        float f = height;
        float f2 = 0.0f;
        while (indexBefore <= indexAfter) {
            entry = this.mStats.getValues(indexBefore, entry);
            long j2 = entry.bucketStart;
            long j3 = entry.bucketDuration + j2;
            float convertToPoint = this.mHoriz.convertToPoint(j2);
            int i2 = indexAfter;
            float convertToPoint2 = this.mHoriz.convertToPoint(j3);
            if (convertToPoint2 < 0.0f) {
                i = indexBefore;
            } else {
                i = indexBefore;
                j += entry.rxBytes + entry.txBytes;
                float convertToPoint3 = this.mVert.convertToPoint(j);
                if (convertToValue != j2) {
                    this.mPathStroke.lineTo(convertToPoint, f);
                    this.mPathFill.lineTo(convertToPoint, f);
                }
                this.mPathStroke.lineTo(convertToPoint2, convertToPoint3);
                this.mPathFill.lineTo(convertToPoint2, convertToPoint3);
                f2 = convertToPoint2;
                f = convertToPoint3;
                convertToValue = j3;
            }
            indexBefore = i + 1;
            indexAfter = i2;
        }
        if (convertToValue < this.mEndTime) {
            f2 = this.mHoriz.convertToPoint(this.mEndTime);
            this.mPathStroke.lineTo(f2, f);
            this.mPathFill.lineTo(f2, f);
        }
        this.mPathFill.lineTo(f2, height);
        this.mPathFill.lineTo(0.0f, height);
        this.mMax = j;
        invalidate();
    }

    public void setEndTime(long j) {
        this.mEndTime = j;
    }

    public void setEstimateVisible(boolean z) {
        this.mEstimateVisible = false;
        invalidate();
    }

    public long getMaxEstimate() {
        return this.mMaxEstimate;
    }

    public long getMaxVisible() {
        long j = this.mEstimateVisible ? this.mMaxEstimate : this.mMax;
        if (j <= 0 && this.mStats != null) {
            NetworkStatsHistory.Entry values = this.mStats.getValues(this.mStart, this.mEnd, (NetworkStatsHistory.Entry) null);
            return values.rxBytes + values.txBytes;
        }
        return j;
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        if (!this.mPathValid) {
            generatePath();
        }
        if (this.mEstimateVisible) {
            int save = canvas.save();
            canvas.clipRect(0, 0, getWidth(), getHeight());
            canvas.drawPath(this.mPathEstimate, this.mPaintEstimate);
            canvas.restoreToCount(save);
        }
        Paint paint = this.mSecondary ? this.mPaintFillSecondary : this.mPaintFill;
        int save2 = canvas.save();
        canvas.clipRect(this.mSafeRegion, 0, getWidth(), getHeight() - this.mSafeRegion);
        canvas.drawPath(this.mPathFill, paint);
        canvas.restoreToCount(save2);
    }
}
