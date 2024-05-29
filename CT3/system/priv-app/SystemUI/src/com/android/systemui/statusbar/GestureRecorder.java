package com.android.systemui.statusbar;

import android.os.Handler;
import android.os.SystemClock;
import java.util.HashSet;
import java.util.LinkedList;
/* loaded from: a.zip:com/android/systemui/statusbar/GestureRecorder.class */
public class GestureRecorder {
    public static final String TAG = GestureRecorder.class.getSimpleName();
    private Gesture mCurrentGesture;
    private LinkedList<Gesture> mGestures;
    private Handler mHandler;

    /* loaded from: a.zip:com/android/systemui/statusbar/GestureRecorder$Gesture.class */
    public class Gesture {
        final GestureRecorder this$0;
        private LinkedList<Record> mRecords = new LinkedList<>();
        private HashSet<String> mTags = new HashSet<>();
        long mDownTime = -1;
        boolean mComplete = false;

        /* loaded from: a.zip:com/android/systemui/statusbar/GestureRecorder$Gesture$Record.class */
        public abstract class Record {
            final Gesture this$1;
            long time;

            public Record(Gesture gesture) {
                this.this$1 = gesture;
            }
        }

        /* loaded from: a.zip:com/android/systemui/statusbar/GestureRecorder$Gesture$TagRecord.class */
        public class TagRecord extends Record {
            public String info;
            public String tag;
            final Gesture this$1;

            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            public TagRecord(Gesture gesture, long j, String str, String str2) {
                super(gesture);
                this.this$1 = gesture;
                this.time = j;
                this.tag = str;
                this.info = str2;
            }
        }

        public Gesture(GestureRecorder gestureRecorder) {
            this.this$0 = gestureRecorder;
        }

        public void tag(long j, String str, String str2) {
            this.mRecords.add(new TagRecord(this, j, str, str2));
            this.mTags.add(str);
        }
    }

    public void saveLater() {
        this.mHandler.removeMessages(6351);
        this.mHandler.sendEmptyMessageDelayed(6351, 5000L);
    }

    public void tag(long j, String str, String str2) {
        synchronized (this.mGestures) {
            if (this.mCurrentGesture == null) {
                this.mCurrentGesture = new Gesture(this);
                this.mGestures.add(this.mCurrentGesture);
            }
            this.mCurrentGesture.tag(j, str, str2);
        }
        saveLater();
    }

    public void tag(String str, String str2) {
        tag(SystemClock.uptimeMillis(), str, str2);
    }
}
