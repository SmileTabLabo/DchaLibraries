package android.support.v4.view;

import android.os.Build;
/* loaded from: classes.dex */
public final class GravityCompat {
    static final GravityCompatImpl IMPL;

    /* loaded from: classes.dex */
    interface GravityCompatImpl {
        int getAbsoluteGravity(int i, int i2);
    }

    /* loaded from: classes.dex */
    static class GravityCompatImplBase implements GravityCompatImpl {
        GravityCompatImplBase() {
        }

        @Override // android.support.v4.view.GravityCompat.GravityCompatImpl
        public int getAbsoluteGravity(int gravity, int layoutDirection) {
            return (-8388609) & gravity;
        }
    }

    /* loaded from: classes.dex */
    static class GravityCompatImplJellybeanMr1 implements GravityCompatImpl {
        GravityCompatImplJellybeanMr1() {
        }

        @Override // android.support.v4.view.GravityCompat.GravityCompatImpl
        public int getAbsoluteGravity(int gravity, int layoutDirection) {
            return GravityCompatJellybeanMr1.getAbsoluteGravity(gravity, layoutDirection);
        }
    }

    static {
        int version = Build.VERSION.SDK_INT;
        if (version >= 17) {
            IMPL = new GravityCompatImplJellybeanMr1();
        } else {
            IMPL = new GravityCompatImplBase();
        }
    }

    public static int getAbsoluteGravity(int gravity, int layoutDirection) {
        return IMPL.getAbsoluteGravity(gravity, layoutDirection);
    }

    private GravityCompat() {
    }
}
