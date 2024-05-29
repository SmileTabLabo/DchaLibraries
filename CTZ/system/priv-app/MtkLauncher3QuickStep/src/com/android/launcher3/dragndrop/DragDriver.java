package com.android.launcher3.dragndrop;

import android.content.Context;
import android.view.DragEvent;
import android.view.MotionEvent;
import com.android.launcher3.DropTarget;
import com.android.launcher3.Utilities;
/* loaded from: classes.dex */
public abstract class DragDriver {
    protected final EventListener mEventListener;

    /* loaded from: classes.dex */
    public interface EventListener {
        void onDriverDragCancel();

        void onDriverDragEnd(float f, float f2);

        void onDriverDragExitWindow();

        void onDriverDragMove(float f, float f2);
    }

    public abstract boolean onDragEvent(DragEvent dragEvent);

    public DragDriver(EventListener eventListener) {
        this.mEventListener = eventListener;
    }

    public void onDragViewAnimationEnd() {
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case 1:
                this.mEventListener.onDriverDragMove(motionEvent.getX(), motionEvent.getY());
                this.mEventListener.onDriverDragEnd(motionEvent.getX(), motionEvent.getY());
                return true;
            case 2:
                this.mEventListener.onDriverDragMove(motionEvent.getX(), motionEvent.getY());
                return true;
            case 3:
                this.mEventListener.onDriverDragCancel();
                return true;
            default:
                return true;
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        if (action == 1) {
            this.mEventListener.onDriverDragEnd(motionEvent.getX(), motionEvent.getY());
        } else if (action == 3) {
            this.mEventListener.onDriverDragCancel();
        }
        return true;
    }

    public static DragDriver create(Context context, DragController dragController, DropTarget.DragObject dragObject, DragOptions dragOptions) {
        if (Utilities.ATLEAST_NOUGAT && dragOptions.systemDndStartPoint != null) {
            return new SystemDragDriver(dragController, context, dragObject);
        }
        return new InternalDragDriver(dragController);
    }
}
