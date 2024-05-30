package com.android.launcher3.dragndrop;

import com.android.launcher3.Alarm;
import com.android.launcher3.CellLayout;
import com.android.launcher3.Launcher;
import com.android.launcher3.OnAlarmListener;
import com.android.launcher3.Workspace;
/* loaded from: classes.dex */
public class SpringLoadedDragController implements OnAlarmListener {
    private Launcher mLauncher;
    private CellLayout mScreen;
    final long ENTER_SPRING_LOAD_HOVER_TIME = 500;
    final long ENTER_SPRING_LOAD_CANCEL_HOVER_TIME = 950;
    Alarm mAlarm = new Alarm();

    public SpringLoadedDragController(Launcher launcher) {
        this.mLauncher = launcher;
        this.mAlarm.setOnAlarmListener(this);
    }

    public void cancel() {
        this.mAlarm.cancelAlarm();
    }

    public void setAlarm(CellLayout cellLayout) {
        this.mAlarm.cancelAlarm();
        this.mAlarm.setAlarm(cellLayout == null ? 950L : 500L);
        this.mScreen = cellLayout;
    }

    @Override // com.android.launcher3.OnAlarmListener
    public void onAlarm(Alarm alarm) {
        if (this.mScreen != null) {
            Workspace workspace = this.mLauncher.getWorkspace();
            int indexOfChild = workspace.indexOfChild(this.mScreen);
            if (indexOfChild != workspace.getCurrentPage()) {
                workspace.snapToPage(indexOfChild);
                return;
            }
            return;
        }
        this.mLauncher.getDragController().cancelDrag();
    }
}
