package com.android.systemui.analytics;

import android.hardware.SensorEvent;
import android.os.Build;
import android.view.MotionEvent;
import com.android.systemui.statusbar.phone.TouchAnalyticsProto$Session;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/systemui/analytics/SensorLoggerSession.class */
public class SensorLoggerSession {
    private long mEndTimestampMillis;
    private final long mStartSystemTimeNanos;
    private final long mStartTimestampMillis;
    private int mTouchAreaHeight;
    private int mTouchAreaWidth;
    private ArrayList<TouchAnalyticsProto$Session.TouchEvent> mMotionEvents = new ArrayList<>();
    private ArrayList<TouchAnalyticsProto$Session.SensorEvent> mSensorEvents = new ArrayList<>();
    private ArrayList<TouchAnalyticsProto$Session.PhoneEvent> mPhoneEvents = new ArrayList<>();
    private int mResult = 2;
    private int mType = 3;

    public SensorLoggerSession(long j, long j2) {
        this.mStartTimestampMillis = j;
        this.mStartSystemTimeNanos = j2;
    }

    private TouchAnalyticsProto$Session.TouchEvent motionEventToProto(MotionEvent motionEvent) {
        int pointerCount = motionEvent.getPointerCount();
        TouchAnalyticsProto$Session.TouchEvent touchEvent = new TouchAnalyticsProto$Session.TouchEvent();
        touchEvent.setTimeOffsetNanos(motionEvent.getEventTimeNano() - this.mStartSystemTimeNanos);
        touchEvent.setAction(motionEvent.getActionMasked());
        touchEvent.setActionIndex(motionEvent.getActionIndex());
        touchEvent.pointers = new TouchAnalyticsProto$Session.TouchEvent.Pointer[pointerCount];
        for (int i = 0; i < pointerCount; i++) {
            TouchAnalyticsProto$Session.TouchEvent.Pointer pointer = new TouchAnalyticsProto$Session.TouchEvent.Pointer();
            pointer.setX(motionEvent.getX(i));
            pointer.setY(motionEvent.getY(i));
            pointer.setSize(motionEvent.getSize(i));
            pointer.setPressure(motionEvent.getPressure(i));
            pointer.setId(motionEvent.getPointerId(i));
            touchEvent.pointers[i] = pointer;
        }
        return touchEvent;
    }

    private TouchAnalyticsProto$Session.PhoneEvent phoneEventToProto(int i, long j) {
        TouchAnalyticsProto$Session.PhoneEvent phoneEvent = new TouchAnalyticsProto$Session.PhoneEvent();
        phoneEvent.setType(i);
        phoneEvent.setTimeOffsetNanos(j - this.mStartSystemTimeNanos);
        return phoneEvent;
    }

    private TouchAnalyticsProto$Session.SensorEvent sensorEventToProto(SensorEvent sensorEvent, long j) {
        TouchAnalyticsProto$Session.SensorEvent sensorEvent2 = new TouchAnalyticsProto$Session.SensorEvent();
        sensorEvent2.setType(sensorEvent.sensor.getType());
        sensorEvent2.setTimeOffsetNanos(j - this.mStartSystemTimeNanos);
        sensorEvent2.setTimestamp(sensorEvent.timestamp);
        sensorEvent2.values = (float[]) sensorEvent.values.clone();
        return sensorEvent2;
    }

    public void addMotionEvent(MotionEvent motionEvent) {
        this.mMotionEvents.add(motionEventToProto(motionEvent));
    }

    public void addPhoneEvent(int i, long j) {
        this.mPhoneEvents.add(phoneEventToProto(i, j));
    }

    public void addSensorEvent(SensorEvent sensorEvent, long j) {
        this.mSensorEvents.add(sensorEventToProto(sensorEvent, j));
    }

    public void end(long j, int i) {
        this.mResult = i;
        this.mEndTimestampMillis = j;
    }

    public int getResult() {
        return this.mResult;
    }

    public long getStartTimestampMillis() {
        return this.mStartTimestampMillis;
    }

    public void setTouchArea(int i, int i2) {
        this.mTouchAreaWidth = i;
        this.mTouchAreaHeight = i2;
    }

    public TouchAnalyticsProto$Session toProto() {
        TouchAnalyticsProto$Session touchAnalyticsProto$Session = new TouchAnalyticsProto$Session();
        touchAnalyticsProto$Session.setStartTimestampMillis(this.mStartTimestampMillis);
        touchAnalyticsProto$Session.setDurationMillis(this.mEndTimestampMillis - this.mStartTimestampMillis);
        touchAnalyticsProto$Session.setBuild(Build.FINGERPRINT);
        touchAnalyticsProto$Session.setResult(this.mResult);
        touchAnalyticsProto$Session.setType(this.mType);
        touchAnalyticsProto$Session.sensorEvents = (TouchAnalyticsProto$Session.SensorEvent[]) this.mSensorEvents.toArray(touchAnalyticsProto$Session.sensorEvents);
        touchAnalyticsProto$Session.touchEvents = (TouchAnalyticsProto$Session.TouchEvent[]) this.mMotionEvents.toArray(touchAnalyticsProto$Session.touchEvents);
        touchAnalyticsProto$Session.phoneEvents = (TouchAnalyticsProto$Session.PhoneEvent[]) this.mPhoneEvents.toArray(touchAnalyticsProto$Session.phoneEvents);
        touchAnalyticsProto$Session.setTouchAreaWidth(this.mTouchAreaWidth);
        touchAnalyticsProto$Session.setTouchAreaHeight(this.mTouchAreaHeight);
        return touchAnalyticsProto$Session;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("Session{");
        sb.append("mStartTimestampMillis=").append(this.mStartTimestampMillis);
        sb.append(", mStartSystemTimeNanos=").append(this.mStartSystemTimeNanos);
        sb.append(", mEndTimestampMillis=").append(this.mEndTimestampMillis);
        sb.append(", mResult=").append(this.mResult);
        sb.append(", mTouchAreaHeight=").append(this.mTouchAreaHeight);
        sb.append(", mTouchAreaWidth=").append(this.mTouchAreaWidth);
        sb.append(", mMotionEvents=[size=").append(this.mMotionEvents.size()).append("]");
        sb.append(", mSensorEvents=[size=").append(this.mSensorEvents.size()).append("]");
        sb.append(", mPhoneEvents=[size=").append(this.mPhoneEvents.size()).append("]");
        sb.append('}');
        return sb.toString();
    }
}
