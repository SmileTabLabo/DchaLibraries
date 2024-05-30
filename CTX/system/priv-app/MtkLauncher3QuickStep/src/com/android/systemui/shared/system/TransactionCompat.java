package com.android.systemui.shared.system;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.IBinder;
import android.view.Surface;
import android.view.SurfaceControl;
/* loaded from: classes.dex */
public class TransactionCompat {
    private final float[] mTmpValues = new float[9];
    private final SurfaceControl.Transaction mTransaction = new SurfaceControl.Transaction();

    public void apply() {
        this.mTransaction.apply();
    }

    public TransactionCompat show(SurfaceControlCompat surfaceControl) {
        this.mTransaction.show(surfaceControl.mSurfaceControl);
        return this;
    }

    public TransactionCompat hide(SurfaceControlCompat surfaceControl) {
        this.mTransaction.hide(surfaceControl.mSurfaceControl);
        return this;
    }

    public TransactionCompat setPosition(SurfaceControlCompat surfaceControl, float x, float y) {
        this.mTransaction.setPosition(surfaceControl.mSurfaceControl, x, y);
        return this;
    }

    public TransactionCompat setSize(SurfaceControlCompat surfaceControl, int w, int h) {
        this.mTransaction.setSize(surfaceControl.mSurfaceControl, w, h);
        return this;
    }

    public TransactionCompat setLayer(SurfaceControlCompat surfaceControl, int z) {
        this.mTransaction.setLayer(surfaceControl.mSurfaceControl, z);
        return this;
    }

    public TransactionCompat setAlpha(SurfaceControlCompat surfaceControl, float alpha) {
        this.mTransaction.setAlpha(surfaceControl.mSurfaceControl, alpha);
        return this;
    }

    public TransactionCompat setMatrix(SurfaceControlCompat surfaceControl, float dsdx, float dtdx, float dtdy, float dsdy) {
        this.mTransaction.setMatrix(surfaceControl.mSurfaceControl, dsdx, dtdx, dtdy, dsdy);
        return this;
    }

    public TransactionCompat setMatrix(SurfaceControlCompat surfaceControl, Matrix matrix) {
        this.mTransaction.setMatrix(surfaceControl.mSurfaceControl, matrix, this.mTmpValues);
        return this;
    }

    public TransactionCompat setWindowCrop(SurfaceControlCompat surfaceControl, Rect crop) {
        this.mTransaction.setWindowCrop(surfaceControl.mSurfaceControl, crop);
        return this;
    }

    public TransactionCompat setFinalCrop(SurfaceControlCompat surfaceControl, Rect crop) {
        this.mTransaction.setFinalCrop(surfaceControl.mSurfaceControl, crop);
        return this;
    }

    public TransactionCompat deferTransactionUntil(SurfaceControlCompat surfaceControl, IBinder handle, long frameNumber) {
        this.mTransaction.deferTransactionUntil(surfaceControl.mSurfaceControl, handle, frameNumber);
        return this;
    }

    public TransactionCompat deferTransactionUntil(SurfaceControlCompat surfaceControl, Surface barrier, long frameNumber) {
        this.mTransaction.deferTransactionUntilSurface(surfaceControl.mSurfaceControl, barrier, frameNumber);
        return this;
    }

    public TransactionCompat setEarlyWakeup() {
        this.mTransaction.setEarlyWakeup();
        return this;
    }

    public TransactionCompat setColor(SurfaceControlCompat surfaceControl, float[] color) {
        this.mTransaction.setColor(surfaceControl.mSurfaceControl, color);
        return this;
    }
}
