package com.android.systemui.statusbar.policy;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.UserHandle;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/systemui/statusbar/policy/NextAlarmController.class */
public class NextAlarmController extends BroadcastReceiver {
    private AlarmManager mAlarmManager;
    private final ArrayList<NextAlarmChangeCallback> mChangeCallbacks = new ArrayList<>();
    private AlarmManager.AlarmClockInfo mNextAlarm;

    /* loaded from: a.zip:com/android/systemui/statusbar/policy/NextAlarmController$NextAlarmChangeCallback.class */
    public interface NextAlarmChangeCallback {
        void onNextAlarmChanged(AlarmManager.AlarmClockInfo alarmClockInfo);
    }

    public NextAlarmController(Context context) {
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        intentFilter.addAction("android.app.action.NEXT_ALARM_CLOCK_CHANGED");
        context.registerReceiverAsUser(this, UserHandle.ALL, intentFilter, null, null);
        updateNextAlarm();
    }

    private void fireNextAlarmChanged() {
        int size = this.mChangeCallbacks.size();
        for (int i = 0; i < size; i++) {
            this.mChangeCallbacks.get(i).onNextAlarmChanged(this.mNextAlarm);
        }
    }

    private void updateNextAlarm() {
        this.mNextAlarm = this.mAlarmManager.getNextAlarmClock(-2);
        fireNextAlarmChanged();
    }

    public void addStateChangedCallback(NextAlarmChangeCallback nextAlarmChangeCallback) {
        this.mChangeCallbacks.add(nextAlarmChangeCallback);
        nextAlarmChangeCallback.onNextAlarmChanged(this.mNextAlarm);
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("NextAlarmController state:");
        printWriter.print("  mNextAlarm=");
        printWriter.println(this.mNextAlarm);
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals("android.intent.action.USER_SWITCHED") || action.equals("android.app.action.NEXT_ALARM_CLOCK_CHANGED")) {
            updateNextAlarm();
        }
    }

    public void removeStateChangedCallback(NextAlarmChangeCallback nextAlarmChangeCallback) {
        this.mChangeCallbacks.remove(nextAlarmChangeCallback);
    }
}
