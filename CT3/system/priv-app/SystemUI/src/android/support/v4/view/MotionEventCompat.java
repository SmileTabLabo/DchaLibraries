package android.support.v4.view;

import android.os.Build;
import android.view.MotionEvent;
/* loaded from: a.zip:android/support/v4/view/MotionEventCompat.class */
public final class MotionEventCompat {
    static final MotionEventVersionImpl IMPL;

    /* loaded from: a.zip:android/support/v4/view/MotionEventCompat$BaseMotionEventVersionImpl.class */
    static class BaseMotionEventVersionImpl implements MotionEventVersionImpl {
        BaseMotionEventVersionImpl() {
        }

        @Override // android.support.v4.view.MotionEventCompat.MotionEventVersionImpl
        public int findPointerIndex(MotionEvent motionEvent, int i) {
            return i == 0 ? 0 : -1;
        }

        @Override // android.support.v4.view.MotionEventCompat.MotionEventVersionImpl
        public float getAxisValue(MotionEvent motionEvent, int i) {
            return 0.0f;
        }

        @Override // android.support.v4.view.MotionEventCompat.MotionEventVersionImpl
        public int getPointerCount(MotionEvent motionEvent) {
            return 1;
        }

        @Override // android.support.v4.view.MotionEventCompat.MotionEventVersionImpl
        public int getPointerId(MotionEvent motionEvent, int i) {
            if (i == 0) {
                return 0;
            }
            throw new IndexOutOfBoundsException("Pre-Eclair does not support multiple pointers");
        }

        @Override // android.support.v4.view.MotionEventCompat.MotionEventVersionImpl
        public int getSource(MotionEvent motionEvent) {
            return 0;
        }

        @Override // android.support.v4.view.MotionEventCompat.MotionEventVersionImpl
        public float getX(MotionEvent motionEvent, int i) {
            if (i == 0) {
                return motionEvent.getX();
            }
            throw new IndexOutOfBoundsException("Pre-Eclair does not support multiple pointers");
        }

        @Override // android.support.v4.view.MotionEventCompat.MotionEventVersionImpl
        public float getY(MotionEvent motionEvent, int i) {
            if (i == 0) {
                return motionEvent.getY();
            }
            throw new IndexOutOfBoundsException("Pre-Eclair does not support multiple pointers");
        }
    }

    /* loaded from: a.zip:android/support/v4/view/MotionEventCompat$EclairMotionEventVersionImpl.class */
    static class EclairMotionEventVersionImpl extends BaseMotionEventVersionImpl {
        EclairMotionEventVersionImpl() {
        }

        @Override // android.support.v4.view.MotionEventCompat.BaseMotionEventVersionImpl, android.support.v4.view.MotionEventCompat.MotionEventVersionImpl
        public int findPointerIndex(MotionEvent motionEvent, int i) {
            return MotionEventCompatEclair.findPointerIndex(motionEvent, i);
        }

        @Override // android.support.v4.view.MotionEventCompat.BaseMotionEventVersionImpl, android.support.v4.view.MotionEventCompat.MotionEventVersionImpl
        public int getPointerCount(MotionEvent motionEvent) {
            return MotionEventCompatEclair.getPointerCount(motionEvent);
        }

        @Override // android.support.v4.view.MotionEventCompat.BaseMotionEventVersionImpl, android.support.v4.view.MotionEventCompat.MotionEventVersionImpl
        public int getPointerId(MotionEvent motionEvent, int i) {
            return MotionEventCompatEclair.getPointerId(motionEvent, i);
        }

        @Override // android.support.v4.view.MotionEventCompat.BaseMotionEventVersionImpl, android.support.v4.view.MotionEventCompat.MotionEventVersionImpl
        public float getX(MotionEvent motionEvent, int i) {
            return MotionEventCompatEclair.getX(motionEvent, i);
        }

        @Override // android.support.v4.view.MotionEventCompat.BaseMotionEventVersionImpl, android.support.v4.view.MotionEventCompat.MotionEventVersionImpl
        public float getY(MotionEvent motionEvent, int i) {
            return MotionEventCompatEclair.getY(motionEvent, i);
        }
    }

    /* loaded from: a.zip:android/support/v4/view/MotionEventCompat$GingerbreadMotionEventVersionImpl.class */
    static class GingerbreadMotionEventVersionImpl extends EclairMotionEventVersionImpl {
        GingerbreadMotionEventVersionImpl() {
        }

        @Override // android.support.v4.view.MotionEventCompat.BaseMotionEventVersionImpl, android.support.v4.view.MotionEventCompat.MotionEventVersionImpl
        public int getSource(MotionEvent motionEvent) {
            return MotionEventCompatGingerbread.getSource(motionEvent);
        }
    }

    /* loaded from: a.zip:android/support/v4/view/MotionEventCompat$HoneycombMr1MotionEventVersionImpl.class */
    static class HoneycombMr1MotionEventVersionImpl extends GingerbreadMotionEventVersionImpl {
        HoneycombMr1MotionEventVersionImpl() {
        }

        @Override // android.support.v4.view.MotionEventCompat.BaseMotionEventVersionImpl, android.support.v4.view.MotionEventCompat.MotionEventVersionImpl
        public float getAxisValue(MotionEvent motionEvent, int i) {
            return MotionEventCompatHoneycombMr1.getAxisValue(motionEvent, i);
        }
    }

    /* loaded from: a.zip:android/support/v4/view/MotionEventCompat$ICSMotionEventVersionImpl.class */
    private static class ICSMotionEventVersionImpl extends HoneycombMr1MotionEventVersionImpl {
        private ICSMotionEventVersionImpl() {
        }

        /* synthetic */ ICSMotionEventVersionImpl(ICSMotionEventVersionImpl iCSMotionEventVersionImpl) {
            this();
        }
    }

    /* loaded from: a.zip:android/support/v4/view/MotionEventCompat$MotionEventVersionImpl.class */
    interface MotionEventVersionImpl {
        int findPointerIndex(MotionEvent motionEvent, int i);

        float getAxisValue(MotionEvent motionEvent, int i);

        int getPointerCount(MotionEvent motionEvent);

        int getPointerId(MotionEvent motionEvent, int i);

        int getSource(MotionEvent motionEvent);

        float getX(MotionEvent motionEvent, int i);

        float getY(MotionEvent motionEvent, int i);
    }

    static {
        if (Build.VERSION.SDK_INT >= 14) {
            IMPL = new ICSMotionEventVersionImpl(null);
        } else if (Build.VERSION.SDK_INT >= 12) {
            IMPL = new HoneycombMr1MotionEventVersionImpl();
        } else if (Build.VERSION.SDK_INT >= 9) {
            IMPL = new GingerbreadMotionEventVersionImpl();
        } else if (Build.VERSION.SDK_INT >= 5) {
            IMPL = new EclairMotionEventVersionImpl();
        } else {
            IMPL = new BaseMotionEventVersionImpl();
        }
    }

    private MotionEventCompat() {
    }

    public static int findPointerIndex(MotionEvent motionEvent, int i) {
        return IMPL.findPointerIndex(motionEvent, i);
    }

    public static int getActionIndex(MotionEvent motionEvent) {
        return (motionEvent.getAction() & 65280) >> 8;
    }

    public static int getActionMasked(MotionEvent motionEvent) {
        return motionEvent.getAction() & 255;
    }

    public static float getAxisValue(MotionEvent motionEvent, int i) {
        return IMPL.getAxisValue(motionEvent, i);
    }

    public static int getPointerCount(MotionEvent motionEvent) {
        return IMPL.getPointerCount(motionEvent);
    }

    public static int getPointerId(MotionEvent motionEvent, int i) {
        return IMPL.getPointerId(motionEvent, i);
    }

    public static int getSource(MotionEvent motionEvent) {
        return IMPL.getSource(motionEvent);
    }

    public static float getX(MotionEvent motionEvent, int i) {
        return IMPL.getX(motionEvent, i);
    }

    public static float getY(MotionEvent motionEvent, int i) {
        return IMPL.getY(motionEvent, i);
    }
}
