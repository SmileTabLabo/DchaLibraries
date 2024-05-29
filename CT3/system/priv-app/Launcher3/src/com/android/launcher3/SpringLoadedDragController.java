package com.android.launcher3;
/* loaded from: a.zip:com/android/launcher3/SpringLoadedDragController.class */
public class SpringLoadedDragController implements OnAlarmListener {
    private Launcher mLauncher;
    private CellLayout mScreen;
    final long ENTER_SPRING_LOAD_HOVER_TIME = 500;
    final long ENTER_SPRING_LOAD_CANCEL_HOVER_TIME = 950;
    final long EXIT_SPRING_LOAD_HOVER_TIME = 200;
    Alarm mAlarm = new Alarm();

    public SpringLoadedDragController(Launcher launcher) {
        this.mLauncher = launcher;
        this.mAlarm.setOnAlarmListener(this);
    }

    public void cancel() {
        this.mAlarm.cancelAlarm();
    }

    @Override // com.android.launcher3.OnAlarmListener
    public void onAlarm(Alarm alarm) {
        if (this.mScreen == null) {
            this.mLauncher.getDragController().cancelDrag();
            return;
        }
        Workspace workspace = this.mLauncher.getWorkspace();
        int indexOfChild = workspace.indexOfChild(this.mScreen);
        if (indexOfChild != workspace.getCurrentPage()) {
            workspace.snapToPage(indexOfChild);
        }
    }

    public void setAlarm(CellLayout cellLayout) {
        this.mAlarm.cancelAlarm();
        this.mAlarm.setAlarm(cellLayout == null ? 950L : 500L);
        this.mScreen = cellLayout;
    }
}
