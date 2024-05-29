package com.mediatek.keyguard.PowerOffAlarm.multiwaveview;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.Log;
import java.util.ArrayList;
/* loaded from: a.zip:com/mediatek/keyguard/PowerOffAlarm/multiwaveview/PointCloud.class */
public class PointCloud {
    private float mCenterX;
    private float mCenterY;
    private Drawable mDrawable;
    private float mOuterRadius;
    private ArrayList<Point> mPointCloud = new ArrayList<>();
    private float mScale = 1.0f;
    WaveManager waveManager = new WaveManager(this);
    GlowManager glowManager = new GlowManager(this);
    private Paint mPaint = new Paint();

    /* loaded from: a.zip:com/mediatek/keyguard/PowerOffAlarm/multiwaveview/PointCloud$GlowManager.class */
    public class GlowManager {
        final PointCloud this$0;
        private float x;
        private float y;
        private float radius = 0.0f;
        private float alpha = 0.0f;

        public GlowManager(PointCloud pointCloud) {
            this.this$0 = pointCloud;
        }

        public void setRadius(float f) {
            this.radius = f;
        }

        public void setX(float f) {
            this.x = f;
        }

        public void setY(float f) {
            this.y = f;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:com/mediatek/keyguard/PowerOffAlarm/multiwaveview/PointCloud$Point.class */
    public class Point {
        float radius;
        final PointCloud this$0;
        float x;
        float y;

        public Point(PointCloud pointCloud, float f, float f2, float f3) {
            this.this$0 = pointCloud;
            this.x = f;
            this.y = f2;
            this.radius = f3;
        }
    }

    /* loaded from: a.zip:com/mediatek/keyguard/PowerOffAlarm/multiwaveview/PointCloud$WaveManager.class */
    public class WaveManager {
        final PointCloud this$0;
        private float radius = 50.0f;
        private float width = 200.0f;
        private float alpha = 0.0f;

        public WaveManager(PointCloud pointCloud) {
            this.this$0 = pointCloud;
        }

        public void setAlpha(float f) {
            this.alpha = f;
        }

        public void setRadius(float f) {
            this.radius = f;
        }
    }

    public PointCloud(Drawable drawable) {
        this.mPaint.setFilterBitmap(true);
        this.mPaint.setColor(Color.rgb(255, 255, 255));
        this.mPaint.setAntiAlias(true);
        this.mPaint.setDither(true);
        this.mDrawable = drawable;
        if (this.mDrawable != null) {
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        }
    }

    private static float hypot(float f, float f2) {
        return (float) Math.hypot(f, f2);
    }

    private float interp(float f, float f2, float f3) {
        return ((f2 - f) * f3) + f;
    }

    private static float max(float f, float f2) {
        if (f <= f2) {
            f = f2;
        }
        return f;
    }

    public void draw(Canvas canvas) {
        ArrayList<Point> arrayList = this.mPointCloud;
        canvas.save(1);
        canvas.scale(this.mScale, this.mScale, this.mCenterX, this.mCenterY);
        for (int i = 0; i < arrayList.size(); i++) {
            Point point = arrayList.get(i);
            float interp = interp(4.0f, 2.0f, point.radius / this.mOuterRadius);
            float f = point.x + this.mCenterX;
            float f2 = point.y + this.mCenterY;
            int alphaForPoint = getAlphaForPoint(point);
            if (alphaForPoint != 0) {
                if (this.mDrawable != null) {
                    canvas.save(1);
                    float intrinsicWidth = this.mDrawable.getIntrinsicWidth();
                    float intrinsicHeight = this.mDrawable.getIntrinsicHeight();
                    float f3 = interp / 4.0f;
                    canvas.scale(f3, f3, f, f2);
                    canvas.translate(f - (intrinsicWidth * 0.5f), f2 - (intrinsicHeight * 0.5f));
                    this.mDrawable.setAlpha(alphaForPoint);
                    this.mDrawable.draw(canvas);
                    canvas.restore();
                } else {
                    this.mPaint.setAlpha(alphaForPoint);
                    canvas.drawCircle(f, f2, interp, this.mPaint);
                }
            }
        }
        canvas.restore();
    }

    public int getAlphaForPoint(Point point) {
        float hypot = hypot(this.glowManager.x - point.x, this.glowManager.y - point.y);
        float f = 0.0f;
        if (hypot < this.glowManager.radius) {
            f = this.glowManager.alpha * max(0.0f, (float) Math.pow(Math.cos((hypot * 0.7853981633974483d) / this.glowManager.radius), 10.0d));
        }
        float hypot2 = hypot(point.x, point.y) - this.waveManager.radius;
        float f2 = 0.0f;
        if (hypot2 < this.waveManager.width * 0.5f) {
            f2 = 0.0f;
            if (hypot2 < 0.0f) {
                f2 = this.waveManager.alpha * max(0.0f, (float) Math.pow(Math.cos((hypot2 * 0.7853981633974483d) / this.waveManager.width), 20.0d));
            }
        }
        return (int) (max(f, f2) * 255.0f);
    }

    public void makePointCloud(float f, float f2) {
        if (f == 0.0f) {
            Log.w("PointCloud", "Must specify an inner radius");
            return;
        }
        this.mOuterRadius = f2;
        this.mPointCloud.clear();
        float f3 = f2 - f;
        float f4 = (6.2831855f * f) / 8.0f;
        int round = Math.round(f3 / f4);
        float f5 = f3 / round;
        int i = 0;
        while (i <= round) {
            int i2 = (int) ((6.2831855f * f) / f4);
            float f6 = 1.5707964f;
            float f7 = 6.2831855f / i2;
            for (int i3 = 0; i3 < i2; i3++) {
                float cos = (float) Math.cos(f6);
                float sin = (float) Math.sin(f6);
                f6 += f7;
                this.mPointCloud.add(new Point(this, f * cos, f * sin, f));
            }
            i++;
            f += f5;
        }
    }

    public void setCenter(float f, float f2) {
        this.mCenterX = f;
        this.mCenterY = f2;
    }

    public void setScale(float f) {
        this.mScale = f;
    }
}
