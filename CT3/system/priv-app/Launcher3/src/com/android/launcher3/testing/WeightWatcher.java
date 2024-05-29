package com.android.launcher3.testing;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.launcher3.testing.MemoryTracker;
/* loaded from: a.zip:com/android/launcher3/testing/WeightWatcher.class */
public class WeightWatcher extends LinearLayout {
    Handler mHandler;
    MemoryTracker mMemoryService;

    /* loaded from: a.zip:com/android/launcher3/testing/WeightWatcher$ProcessWatcher.class */
    public class ProcessWatcher extends LinearLayout {
        MemoryTracker.ProcessMemInfo mMemInfo;
        int mPid;
        GraphView mRamGraph;
        TextView mText;
        final WeightWatcher this$0;

        /* loaded from: a.zip:com/android/launcher3/testing/WeightWatcher$ProcessWatcher$GraphView.class */
        public class GraphView extends View {
            Paint headPaint;
            Paint pssPaint;
            final ProcessWatcher this$1;
            Paint ussPaint;

            public GraphView(ProcessWatcher processWatcher, Context context) {
                this(processWatcher, context, null);
            }

            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            public GraphView(ProcessWatcher processWatcher, Context context, AttributeSet attributeSet) {
                super(context, attributeSet);
                this.this$1 = processWatcher;
                this.pssPaint = new Paint();
                this.pssPaint.setColor(-6697984);
                this.ussPaint = new Paint();
                this.ussPaint.setColor(-6750208);
                this.headPaint = new Paint();
                this.headPaint.setColor(-1);
            }

            @Override // android.view.View
            public void onDraw(Canvas canvas) {
                int width = canvas.getWidth();
                int height = canvas.getHeight();
                if (this.this$1.mMemInfo == null) {
                    return;
                }
                int length = this.this$1.mMemInfo.pss.length;
                float f = width / length;
                float max = Math.max(1.0f, f);
                float f2 = height / ((float) this.this$1.mMemInfo.max);
                for (int i = 0; i < length; i++) {
                    float f3 = i * f;
                    canvas.drawRect(f3, height - (((float) this.this$1.mMemInfo.pss[i]) * f2), f3 + max, height, this.pssPaint);
                    canvas.drawRect(f3, height - (((float) this.this$1.mMemInfo.uss[i]) * f2), f3 + max, height, this.ussPaint);
                }
                float f4 = this.this$1.mMemInfo.head * f;
                canvas.drawRect(f4, 0.0f, f4 + max, height, this.headPaint);
            }
        }

        public ProcessWatcher(WeightWatcher weightWatcher, Context context) {
            this(weightWatcher, context, null);
        }

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public ProcessWatcher(WeightWatcher weightWatcher, Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            this.this$0 = weightWatcher;
            float f = getResources().getDisplayMetrics().density;
            this.mText = new TextView(getContext());
            this.mText.setTextColor(-1);
            this.mText.setTextSize(0, 10.0f * f);
            this.mText.setGravity(19);
            int i = (int) (2.0f * f);
            setPadding(i, 0, i, 0);
            this.mRamGraph = new GraphView(this, getContext());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, (int) (14.0f * f), 1.0f);
            addView(this.mText, layoutParams);
            layoutParams.leftMargin = (int) (4.0f * f);
            layoutParams.weight = 0.0f;
            layoutParams.width = (int) (200.0f * f);
            addView(this.mRamGraph, layoutParams);
        }

        public int getPid() {
            return this.mPid;
        }

        public String getUptimeString() {
            long uptime = this.mMemInfo.getUptime() / 1000;
            StringBuilder sb = new StringBuilder();
            long j = uptime / 86400;
            long j2 = uptime;
            if (j > 0) {
                j2 = uptime - (86400 * j);
                sb.append(j);
                sb.append("d");
            }
            long j3 = j2 / 3600;
            long j4 = j2;
            if (j3 > 0) {
                j4 = j2 - (3600 * j3);
                sb.append(j3);
                sb.append("h");
            }
            long j5 = j4 / 60;
            long j6 = j4;
            if (j5 > 0) {
                j6 = j4 - (60 * j5);
                sb.append(j5);
                sb.append("m");
            }
            sb.append(j6);
            sb.append("s");
            return sb.toString();
        }

        public void setPid(int i) {
            this.mPid = i;
            this.mMemInfo = this.this$0.mMemoryService.getMemInfo(this.mPid);
            if (this.mMemInfo == null) {
                Log.v("WeightWatcher", "Missing info for pid " + this.mPid + ", removing view: " + this);
                this.this$0.initViews();
            }
        }

        public void update() {
            this.mText.setText("(" + this.mPid + (this.mPid == Process.myPid() ? "/A" : "/S") + ") up " + getUptimeString() + " P=" + this.mMemInfo.currentPss + " U=" + this.mMemInfo.currentUss);
            this.mRamGraph.invalidate();
        }
    }

    public WeightWatcher(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mHandler = new Handler(this) { // from class: com.android.launcher3.testing.WeightWatcher.1
            final WeightWatcher this$0;

            {
                this.this$0 = this;
            }

            @Override // android.os.Handler
            public void handleMessage(Message message) {
                switch (message.what) {
                    case 1:
                        this.this$0.mHandler.sendEmptyMessage(3);
                        return;
                    case 2:
                        this.this$0.mHandler.removeMessages(3);
                        return;
                    case 3:
                        int[] trackedProcesses = this.this$0.mMemoryService.getTrackedProcesses();
                        int childCount = this.this$0.getChildCount();
                        if (trackedProcesses.length != childCount) {
                            this.this$0.initViews();
                        } else {
                            int i = 0;
                            while (true) {
                                if (i < childCount) {
                                    ProcessWatcher processWatcher = (ProcessWatcher) this.this$0.getChildAt(i);
                                    if (WeightWatcher.indexOf(trackedProcesses, processWatcher.getPid()) < 0) {
                                        this.this$0.initViews();
                                    } else {
                                        processWatcher.update();
                                        i++;
                                    }
                                }
                            }
                        }
                        this.this$0.mHandler.sendEmptyMessageDelayed(3, 5000L);
                        return;
                    default:
                        return;
                }
            }
        };
        context.bindService(new Intent(context, MemoryTracker.class), new ServiceConnection(this) { // from class: com.android.launcher3.testing.WeightWatcher.2
            final WeightWatcher this$0;

            {
                this.this$0 = this;
            }

            @Override // android.content.ServiceConnection
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                this.this$0.mMemoryService = ((MemoryTracker.MemoryTrackerInterface) iBinder).getService();
                this.this$0.initViews();
            }

            @Override // android.content.ServiceConnection
            public void onServiceDisconnected(ComponentName componentName) {
                this.this$0.mMemoryService = null;
            }
        }, 1);
        setOrientation(1);
        setBackgroundColor(-1073741824);
    }

    static int indexOf(int[] iArr, int i) {
        for (int i2 = 0; i2 < iArr.length; i2++) {
            if (iArr[i2] == i) {
                return i2;
            }
        }
        return -1;
    }

    public void initViews() {
        removeAllViews();
        for (int i : this.mMemoryService.getTrackedProcesses()) {
            ProcessWatcher processWatcher = new ProcessWatcher(this, getContext());
            processWatcher.setPid(i);
            addView(processWatcher);
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mHandler.sendEmptyMessage(1);
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mHandler.sendEmptyMessage(2);
    }
}
