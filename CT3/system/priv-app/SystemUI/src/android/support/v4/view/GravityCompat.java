package android.support.v4.view;

import android.os.Build;
/* loaded from: a.zip:android/support/v4/view/GravityCompat.class */
public final class GravityCompat {
    static final GravityCompatImpl IMPL;

    /* loaded from: a.zip:android/support/v4/view/GravityCompat$GravityCompatImpl.class */
    interface GravityCompatImpl {
        int getAbsoluteGravity(int i, int i2);
    }

    /* loaded from: a.zip:android/support/v4/view/GravityCompat$GravityCompatImplBase.class */
    static class GravityCompatImplBase implements GravityCompatImpl {
        GravityCompatImplBase() {
        }

        @Override // android.support.v4.view.GravityCompat.GravityCompatImpl
        public int getAbsoluteGravity(int i, int i2) {
            return (-8388609) & i;
        }
    }

    /* loaded from: a.zip:android/support/v4/view/GravityCompat$GravityCompatImplJellybeanMr1.class */
    static class GravityCompatImplJellybeanMr1 implements GravityCompatImpl {
        GravityCompatImplJellybeanMr1() {
        }

        @Override // android.support.v4.view.GravityCompat.GravityCompatImpl
        public int getAbsoluteGravity(int i, int i2) {
            return GravityCompatJellybeanMr1.getAbsoluteGravity(i, i2);
        }
    }

    static {
        if (Build.VERSION.SDK_INT >= 17) {
            IMPL = new GravityCompatImplJellybeanMr1();
        } else {
            IMPL = new GravityCompatImplBase();
        }
    }

    private GravityCompat() {
    }

    public static int getAbsoluteGravity(int i, int i2) {
        return IMPL.getAbsoluteGravity(i, i2);
    }
}
