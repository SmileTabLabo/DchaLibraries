package com.android.systemui.tuner;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
/* loaded from: a.zip:com/android/systemui/tuner/ClipboardView.class */
public class ClipboardView extends ImageView implements ClipboardManager.OnPrimaryClipChangedListener {
    private final ClipboardManager mClipboardManager;
    private ClipData mCurrentClip;

    public ClipboardView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mClipboardManager = (ClipboardManager) context.getSystemService(ClipboardManager.class);
    }

    private void setBackgroundDragTarget(boolean z) {
        setBackgroundColor(z ? 1308622847 : 0);
    }

    @Override // android.widget.ImageView, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startListening();
    }

    @Override // android.widget.ImageView, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopListening();
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // android.view.View
    public boolean onDragEvent(DragEvent dragEvent) {
        switch (dragEvent.getAction()) {
            case 3:
                this.mClipboardManager.setPrimaryClip(dragEvent.getClipData());
                break;
            case 4:
            case 6:
                break;
            case 5:
                setBackgroundDragTarget(true);
                return true;
            default:
                return true;
        }
        setBackgroundDragTarget(false);
        return true;
    }

    @Override // android.content.ClipboardManager.OnPrimaryClipChangedListener
    public void onPrimaryClipChanged() {
        this.mCurrentClip = this.mClipboardManager.getPrimaryClip();
        setImageResource(this.mCurrentClip != null ? 2130837590 : 2130837589);
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getActionMasked() == 0 && this.mCurrentClip != null) {
            startPocketDrag();
        }
        return super.onTouchEvent(motionEvent);
    }

    public void startListening() {
        this.mClipboardManager.addPrimaryClipChangedListener(this);
        onPrimaryClipChanged();
    }

    public void startPocketDrag() {
        startDragAndDrop(this.mCurrentClip, new View.DragShadowBuilder(this), null, 256);
    }

    public void stopListening() {
        this.mClipboardManager.removePrimaryClipChangedListener(this);
    }
}
