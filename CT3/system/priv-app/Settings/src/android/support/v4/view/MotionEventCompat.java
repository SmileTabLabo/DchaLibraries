package android.support.v4.view;

import android.os.Build;
import android.view.MotionEvent;
/* loaded from: classes.dex */
public final class MotionEventCompat {
    static final MotionEventVersionImpl IMPL;

    /* loaded from: classes.dex */
    interface MotionEventVersionImpl {
        int findPointerIndex(MotionEvent motionEvent, int i);

        float getAxisValue(MotionEvent motionEvent, int i);

        int getPointerCount(MotionEvent motionEvent);

        int getPointerId(MotionEvent motionEvent, int i);

        int getSource(MotionEvent motionEvent);

        float getX(MotionEvent motionEvent, int i);

        float getY(MotionEvent motionEvent, int i);
    }

    /* loaded from: classes.dex */
    static class BaseMotionEventVersionImpl implements MotionEventVersionImpl {
        BaseMotionEventVersionImpl() {
        }

        @Override // android.support.v4.view.MotionEventCompat.MotionEventVersionImpl
        public int findPointerIndex(MotionEvent event, int pointerId) {
            return pointerId == 0 ? 0 : -1;
        }

        @Override // android.support.v4.view.MotionEventCompat.MotionEventVersionImpl
        public int getPointerId(MotionEvent event, int pointerIndex) {
            if (pointerIndex == 0) {
                return 0;
            }
            throw new IndexOutOfBoundsException("Pre-Eclair does not support multiple pointers");
        }

        @Override // android.support.v4.view.MotionEventCompat.MotionEventVersionImpl
        public float getX(MotionEvent event, int pointerIndex) {
            if (pointerIndex == 0) {
                return event.getX();
            }
            throw new IndexOutOfBoundsException("Pre-Eclair does not support multiple pointers");
        }

        @Override // android.support.v4.view.MotionEventCompat.MotionEventVersionImpl
        public float getY(MotionEvent event, int pointerIndex) {
            if (pointerIndex == 0) {
                return event.getY();
            }
            throw new IndexOutOfBoundsException("Pre-Eclair does not support multiple pointers");
        }

        @Override // android.support.v4.view.MotionEventCompat.MotionEventVersionImpl
        public int getPointerCount(MotionEvent event) {
            return 1;
        }

        @Override // android.support.v4.view.MotionEventCompat.MotionEventVersionImpl
        public int getSource(MotionEvent event) {
            return 0;
        }

        @Override // android.support.v4.view.MotionEventCompat.MotionEventVersionImpl
        public float getAxisValue(MotionEvent event, int axis) {
            return 0.0f;
        }
    }

    /* loaded from: classes.dex */
    static class EclairMotionEventVersionImpl extends BaseMotionEventVersionImpl {
        EclairMotionEventVersionImpl() {
        }

        @Override // android.support.v4.view.MotionEventCompat.BaseMotionEventVersionImpl, android.support.v4.view.MotionEventCompat.MotionEventVersionImpl
        public int findPointerIndex(MotionEvent event, int pointerId) {
            return MotionEventCompatEclair.findPointerIndex(event, pointerId);
        }

        @Override // android.support.v4.view.MotionEventCompat.BaseMotionEventVersionImpl, android.support.v4.view.MotionEventCompat.MotionEventVersionImpl
        public int getPointerId(MotionEvent event, int pointerIndex) {
            return MotionEventCompatEclair.getPointerId(event, pointerIndex);
        }

        @Override // android.support.v4.view.MotionEventCompat.BaseMotionEventVersionImpl, android.support.v4.view.MotionEventCompat.MotionEventVersionImpl
        public float getX(MotionEvent event, int pointerIndex) {
            return MotionEventCompatEclair.getX(event, pointerIndex);
        }

        @Override // android.support.v4.view.MotionEventCompat.BaseMotionEventVersionImpl, android.support.v4.view.MotionEventCompat.MotionEventVersionImpl
        public float getY(MotionEvent event, int pointerIndex) {
            return MotionEventCompatEclair.getY(event, pointerIndex);
        }

        @Override // android.support.v4.view.MotionEventCompat.BaseMotionEventVersionImpl, android.support.v4.view.MotionEventCompat.MotionEventVersionImpl
        public int getPointerCount(MotionEvent event) {
            return MotionEventCompatEclair.getPointerCount(event);
        }
    }

    /* loaded from: classes.dex */
    static class GingerbreadMotionEventVersionImpl extends EclairMotionEventVersionImpl {
        GingerbreadMotionEventVersionImpl() {
        }

        @Override // android.support.v4.view.MotionEventCompat.BaseMotionEventVersionImpl, android.support.v4.view.MotionEventCompat.MotionEventVersionImpl
        public int getSource(MotionEvent event) {
            return MotionEventCompatGingerbread.getSource(event);
        }
    }

    /* loaded from: classes.dex */
    static class HoneycombMr1MotionEventVersionImpl extends GingerbreadMotionEventVersionImpl {
        HoneycombMr1MotionEventVersionImpl() {
        }

        @Override // android.support.v4.view.MotionEventCompat.BaseMotionEventVersionImpl, android.support.v4.view.MotionEventCompat.MotionEventVersionImpl
        public float getAxisValue(MotionEvent event, int axis) {
            return MotionEventCompatHoneycombMr1.getAxisValue(event, axis);
        }
    }

    /* loaded from: classes.dex */
    private static class ICSMotionEventVersionImpl extends HoneycombMr1MotionEventVersionImpl {
        /* synthetic */ ICSMotionEventVersionImpl(ICSMotionEventVersionImpl iCSMotionEventVersionImpl) {
            this();
        }

        private ICSMotionEventVersionImpl() {
        }
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

    public static int getActionMasked(MotionEvent event) {
        return event.getAction() & 255;
    }

    public static int getActionIndex(MotionEvent event) {
        return (event.getAction() & 65280) >> 8;
    }

    public static int findPointerIndex(MotionEvent event, int pointerId) {
        return IMPL.findPointerIndex(event, pointerId);
    }

    public static int getPointerId(MotionEvent event, int pointerIndex) {
        return IMPL.getPointerId(event, pointerIndex);
    }

    public static float getX(MotionEvent event, int pointerIndex) {
        return IMPL.getX(event, pointerIndex);
    }

    public static float getY(MotionEvent event, int pointerIndex) {
        return IMPL.getY(event, pointerIndex);
    }

    public static int getPointerCount(MotionEvent event) {
        return IMPL.getPointerCount(event);
    }

    public static int getSource(MotionEvent event) {
        return IMPL.getSource(event);
    }

    public static float getAxisValue(MotionEvent event, int axis) {
        return IMPL.getAxisValue(event, axis);
    }

    private MotionEventCompat() {
    }
}
