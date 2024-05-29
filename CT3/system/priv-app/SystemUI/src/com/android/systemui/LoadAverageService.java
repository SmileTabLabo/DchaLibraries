package com.android.systemui;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import com.android.internal.os.ProcessCpuTracker;
/* loaded from: a.zip:com/android/systemui/LoadAverageService.class */
public class LoadAverageService extends Service {
    private View mView;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/LoadAverageService$CpuTracker.class */
    public static final class CpuTracker extends ProcessCpuTracker {
        String mLoadText;
        int mLoadWidth;
        private final Paint mPaint;

        CpuTracker(Paint paint) {
            super(false);
            this.mPaint = paint;
        }

        public void onLoadChanged(float f, float f2, float f3) {
            this.mLoadText = f + " / " + f2 + " / " + f3;
            this.mLoadWidth = (int) this.mPaint.measureText(this.mLoadText);
        }

        public int onMeasureProcessName(String str) {
            return (int) this.mPaint.measureText(str);
        }
    }

    /* loaded from: a.zip:com/android/systemui/LoadAverageService$LoadView.class */
    private class LoadView extends View {
        private Paint mAddedPaint;
        private float mAscent;
        private int mFH;
        private Handler mHandler;
        private Paint mIrqPaint;
        private Paint mLoadPaint;
        private int mNeededHeight;
        private int mNeededWidth;
        private Paint mRemovedPaint;
        private Paint mShadow2Paint;
        private Paint mShadowPaint;
        private final CpuTracker mStats;
        private Paint mSystemPaint;
        private Paint mUserPaint;
        final LoadAverageService this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        LoadView(LoadAverageService loadAverageService, Context context) {
            super(context);
            int i;
            this.this$0 = loadAverageService;
            this.mHandler = new Handler(this) { // from class: com.android.systemui.LoadAverageService.LoadView.1
                final LoadView this$1;

                {
                    this.this$1 = this;
                }

                @Override // android.os.Handler
                public void handleMessage(Message message) {
                    if (message.what == 1) {
                        this.this$1.mStats.update();
                        this.this$1.updateDisplay();
                        sendMessageDelayed(obtainMessage(1), 2000L);
                    }
                }
            };
            setPadding(4, 4, 4, 4);
            float f = context.getResources().getDisplayMetrics().density;
            if (f < 1.0f) {
                i = 9;
            } else {
                int i2 = (int) (10.0f * f);
                i = i2;
                if (i2 < 10) {
                    i = 10;
                }
            }
            this.mLoadPaint = new Paint();
            this.mLoadPaint.setAntiAlias(true);
            this.mLoadPaint.setTextSize(i);
            this.mLoadPaint.setARGB(255, 255, 255, 255);
            this.mAddedPaint = new Paint();
            this.mAddedPaint.setAntiAlias(true);
            this.mAddedPaint.setTextSize(i);
            this.mAddedPaint.setARGB(255, 128, 255, 128);
            this.mRemovedPaint = new Paint();
            this.mRemovedPaint.setAntiAlias(true);
            this.mRemovedPaint.setStrikeThruText(true);
            this.mRemovedPaint.setTextSize(i);
            this.mRemovedPaint.setARGB(255, 255, 128, 128);
            this.mShadowPaint = new Paint();
            this.mShadowPaint.setAntiAlias(true);
            this.mShadowPaint.setTextSize(i);
            this.mShadowPaint.setARGB(192, 0, 0, 0);
            this.mLoadPaint.setShadowLayer(4.0f, 0.0f, 0.0f, -16777216);
            this.mShadow2Paint = new Paint();
            this.mShadow2Paint.setAntiAlias(true);
            this.mShadow2Paint.setTextSize(i);
            this.mShadow2Paint.setARGB(192, 0, 0, 0);
            this.mLoadPaint.setShadowLayer(2.0f, 0.0f, 0.0f, -16777216);
            this.mIrqPaint = new Paint();
            this.mIrqPaint.setARGB(128, 0, 0, 255);
            this.mIrqPaint.setShadowLayer(2.0f, 0.0f, 0.0f, -16777216);
            this.mSystemPaint = new Paint();
            this.mSystemPaint.setARGB(128, 255, 0, 0);
            this.mSystemPaint.setShadowLayer(2.0f, 0.0f, 0.0f, -16777216);
            this.mUserPaint = new Paint();
            this.mUserPaint.setARGB(128, 0, 255, 0);
            this.mSystemPaint.setShadowLayer(2.0f, 0.0f, 0.0f, -16777216);
            this.mAscent = this.mLoadPaint.ascent();
            this.mFH = (int) ((this.mLoadPaint.descent() - this.mAscent) + 0.5f);
            this.mStats = new CpuTracker(this.mLoadPaint);
            this.mStats.init();
            updateDisplay();
        }

        @Override // android.view.View
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            this.mHandler.sendEmptyMessage(1);
        }

        @Override // android.view.View
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            this.mHandler.removeMessages(1);
        }

        @Override // android.view.View
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int i = this.mNeededWidth;
            int width = getWidth() - 1;
            CpuTracker cpuTracker = this.mStats;
            int lastUserTime = cpuTracker.getLastUserTime();
            int lastSystemTime = cpuTracker.getLastSystemTime();
            int lastIoWaitTime = cpuTracker.getLastIoWaitTime();
            int lastIrqTime = cpuTracker.getLastIrqTime();
            int lastSoftIrqTime = cpuTracker.getLastSoftIrqTime();
            int lastIdleTime = lastUserTime + lastSystemTime + lastIoWaitTime + lastIrqTime + lastSoftIrqTime + cpuTracker.getLastIdleTime();
            if (lastIdleTime == 0) {
                return;
            }
            int i2 = (lastUserTime * i) / lastIdleTime;
            int i3 = (lastSystemTime * i) / lastIdleTime;
            int i4 = (((lastIoWaitTime + lastIrqTime) + lastSoftIrqTime) * i) / lastIdleTime;
            int paddingRight = getPaddingRight();
            int i5 = width - paddingRight;
            int paddingTop = getPaddingTop() + 2;
            int paddingTop2 = (getPaddingTop() + this.mFH) - 2;
            int i6 = i5;
            if (i4 > 0) {
                canvas.drawRect(i5 - i4, paddingTop, i5, paddingTop2, this.mIrqPaint);
                i6 = i5 - i4;
            }
            int i7 = i6;
            if (i3 > 0) {
                canvas.drawRect(i6 - i3, paddingTop, i6, paddingTop2, this.mSystemPaint);
                i7 = i6 - i3;
            }
            if (i2 > 0) {
                canvas.drawRect(i7 - i2, paddingTop, i7, paddingTop2, this.mUserPaint);
            }
            int paddingTop3 = getPaddingTop() - ((int) this.mAscent);
            canvas.drawText(cpuTracker.mLoadText, ((width - paddingRight) - cpuTracker.mLoadWidth) - 1, paddingTop3 - 1, this.mShadowPaint);
            canvas.drawText(cpuTracker.mLoadText, ((width - paddingRight) - cpuTracker.mLoadWidth) - 1, paddingTop3 + 1, this.mShadowPaint);
            canvas.drawText(cpuTracker.mLoadText, ((width - paddingRight) - cpuTracker.mLoadWidth) + 1, paddingTop3 - 1, this.mShadow2Paint);
            canvas.drawText(cpuTracker.mLoadText, ((width - paddingRight) - cpuTracker.mLoadWidth) + 1, paddingTop3 + 1, this.mShadow2Paint);
            canvas.drawText(cpuTracker.mLoadText, (width - paddingRight) - cpuTracker.mLoadWidth, paddingTop3, this.mLoadPaint);
            int countWorkingStats = cpuTracker.countWorkingStats();
            int i8 = 0;
            while (i8 < countWorkingStats) {
                ProcessCpuTracker.Stats workingStats = cpuTracker.getWorkingStats(i8);
                int i9 = paddingTop3 + this.mFH;
                paddingTop += this.mFH;
                paddingTop2 += this.mFH;
                int i10 = (workingStats.rel_utime * i) / lastIdleTime;
                int i11 = (workingStats.rel_stime * i) / lastIdleTime;
                int i12 = width - paddingRight;
                int i13 = i12;
                if (i11 > 0) {
                    canvas.drawRect(i12 - i11, paddingTop, i12, paddingTop2, this.mSystemPaint);
                    i13 = i12 - i11;
                }
                if (i10 > 0) {
                    canvas.drawRect(i13 - i10, paddingTop, i13, paddingTop2, this.mUserPaint);
                }
                canvas.drawText(workingStats.name, ((width - paddingRight) - workingStats.nameWidth) - 1, i9 - 1, this.mShadowPaint);
                canvas.drawText(workingStats.name, ((width - paddingRight) - workingStats.nameWidth) - 1, i9 + 1, this.mShadowPaint);
                canvas.drawText(workingStats.name, ((width - paddingRight) - workingStats.nameWidth) + 1, i9 - 1, this.mShadow2Paint);
                canvas.drawText(workingStats.name, ((width - paddingRight) - workingStats.nameWidth) + 1, i9 + 1, this.mShadow2Paint);
                Paint paint = this.mLoadPaint;
                if (workingStats.added) {
                    paint = this.mAddedPaint;
                }
                if (workingStats.removed) {
                    paint = this.mRemovedPaint;
                }
                canvas.drawText(workingStats.name, (width - paddingRight) - workingStats.nameWidth, i9, paint);
                i8++;
                paddingTop3 = i9;
            }
        }

        @Override // android.view.View
        protected void onMeasure(int i, int i2) {
            setMeasuredDimension(resolveSize(this.mNeededWidth, i), resolveSize(this.mNeededHeight, i2));
        }

        void updateDisplay() {
            CpuTracker cpuTracker = this.mStats;
            int countWorkingStats = cpuTracker.countWorkingStats();
            int i = cpuTracker.mLoadWidth;
            int i2 = 0;
            while (i2 < countWorkingStats) {
                ProcessCpuTracker.Stats workingStats = cpuTracker.getWorkingStats(i2);
                int i3 = i;
                if (workingStats.nameWidth > i) {
                    i3 = workingStats.nameWidth;
                }
                i2++;
                i = i3;
            }
            int paddingLeft = getPaddingLeft() + getPaddingRight() + i;
            int paddingTop = getPaddingTop() + getPaddingBottom() + (this.mFH * (countWorkingStats + 1));
            if (paddingLeft == this.mNeededWidth && paddingTop == this.mNeededHeight) {
                invalidate();
                return;
            }
            this.mNeededWidth = paddingLeft;
            this.mNeededHeight = paddingTop;
            requestLayout();
        }
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        this.mView = new LoadView(this, this);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, -2, 2015, 24, -3);
        layoutParams.gravity = 8388661;
        layoutParams.setTitle("Load Average");
        ((WindowManager) getSystemService("window")).addView(this.mView, layoutParams);
    }

    @Override // android.app.Service
    public void onDestroy() {
        super.onDestroy();
        ((WindowManager) getSystemService("window")).removeView(this.mView);
        this.mView = null;
    }
}
