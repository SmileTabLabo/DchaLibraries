package com.android.systemui.classifier;
/* loaded from: a.zip:com/android/systemui/classifier/Point.class */
public class Point {
    public long timeOffsetNano;
    public float x;
    public float y;

    public Point(float f, float f2) {
        this.x = f;
        this.y = f2;
        this.timeOffsetNano = 0L;
    }

    public Point(float f, float f2, long j) {
        this.x = f;
        this.y = f2;
        this.timeOffsetNano = j;
    }

    public float crossProduct(Point point, Point point2) {
        return ((point.x - this.x) * (point2.y - this.y)) - ((point.y - this.y) * (point2.x - this.x));
    }

    public float dist(Point point) {
        return (float) Math.hypot(point.x - this.x, point.y - this.y);
    }

    public float dotProduct(Point point, Point point2) {
        return ((point.x - this.x) * (point2.x - this.x)) + ((point.y - this.y) * (point2.y - this.y));
    }

    public boolean equals(Point point) {
        boolean z = false;
        if (this.x == point.x) {
            z = false;
            if (this.y == point.y) {
                z = true;
            }
        }
        return z;
    }

    public float getAngle(Point point, Point point2) {
        float dist = dist(point);
        float dist2 = dist(point2);
        if (dist == 0.0f || dist2 == 0.0f) {
            return 0.0f;
        }
        float crossProduct = crossProduct(point, point2);
        float acos = (float) Math.acos(Math.min(1.0f, Math.max(-1.0f, (dotProduct(point, point2) / dist) / dist2)));
        float f = acos;
        if (crossProduct < 0.0d) {
            f = 6.2831855f - acos;
        }
        return f;
    }
}
