package com.android.systemui.classifier;

import java.util.ArrayList;
/* loaded from: a.zip:com/android/systemui/classifier/Stroke.class */
public class Stroke {
    private final float mDpi;
    private long mEndTimeNano;
    private float mLength;
    private long mStartTimeNano;
    private final float NANOS_TO_SECONDS = 1.0E9f;
    private ArrayList<Point> mPoints = new ArrayList<>();

    public Stroke(long j, float f) {
        this.mDpi = f;
        this.mEndTimeNano = j;
        this.mStartTimeNano = j;
    }

    public void addPoint(float f, float f2, long j) {
        this.mEndTimeNano = j;
        Point point = new Point(f / this.mDpi, f2 / this.mDpi, j - this.mStartTimeNano);
        if (!this.mPoints.isEmpty()) {
            this.mLength = this.mPoints.get(this.mPoints.size() - 1).dist(point) + this.mLength;
        }
        this.mPoints.add(point);
    }

    public int getCount() {
        return this.mPoints.size();
    }

    public long getDurationNanos() {
        return this.mEndTimeNano - this.mStartTimeNano;
    }

    public float getDurationSeconds() {
        return ((float) getDurationNanos()) / 1.0E9f;
    }

    public float getEndPointLength() {
        return this.mPoints.get(0).dist(this.mPoints.get(this.mPoints.size() - 1));
    }

    public ArrayList<Point> getPoints() {
        return this.mPoints;
    }

    public float getTotalLength() {
        return this.mLength;
    }
}
