package android.support.v4.view;

import android.os.Build;
import android.view.KeyEvent;
/* loaded from: classes.dex */
public final class KeyEventCompat {
    static final KeyEventVersionImpl IMPL;

    /* loaded from: classes.dex */
    interface KeyEventVersionImpl {
        boolean metaStateHasModifiers(int i, int i2);

        boolean metaStateHasNoModifiers(int i);

        void startTracking(KeyEvent keyEvent);
    }

    /* loaded from: classes.dex */
    static class BaseKeyEventVersionImpl implements KeyEventVersionImpl {
        BaseKeyEventVersionImpl() {
        }

        private static int metaStateFilterDirectionalModifiers(int metaState, int modifiers, int basic, int left, int right) {
            boolean wantBasic = (modifiers & basic) != 0;
            int directional = left | right;
            boolean wantLeftOrRight = (modifiers & directional) != 0;
            if (wantBasic) {
                if (wantLeftOrRight) {
                    throw new IllegalArgumentException("bad arguments");
                }
                return (~directional) & metaState;
            } else if (wantLeftOrRight) {
                return (~basic) & metaState;
            } else {
                return metaState;
            }
        }

        public int normalizeMetaState(int metaState) {
            if ((metaState & 192) != 0) {
                metaState |= 1;
            }
            if ((metaState & 48) != 0) {
                metaState |= 2;
            }
            return metaState & 247;
        }

        @Override // android.support.v4.view.KeyEventCompat.KeyEventVersionImpl
        public boolean metaStateHasModifiers(int metaState, int modifiers) {
            return metaStateFilterDirectionalModifiers(metaStateFilterDirectionalModifiers(normalizeMetaState(metaState) & 247, modifiers, 1, 64, 128), modifiers, 2, 16, 32) == modifiers;
        }

        @Override // android.support.v4.view.KeyEventCompat.KeyEventVersionImpl
        public boolean metaStateHasNoModifiers(int metaState) {
            return (normalizeMetaState(metaState) & 247) == 0;
        }

        @Override // android.support.v4.view.KeyEventCompat.KeyEventVersionImpl
        public void startTracking(KeyEvent event) {
        }
    }

    /* loaded from: classes.dex */
    static class EclairKeyEventVersionImpl extends BaseKeyEventVersionImpl {
        EclairKeyEventVersionImpl() {
        }

        @Override // android.support.v4.view.KeyEventCompat.BaseKeyEventVersionImpl, android.support.v4.view.KeyEventCompat.KeyEventVersionImpl
        public void startTracking(KeyEvent event) {
            KeyEventCompatEclair.startTracking(event);
        }
    }

    /* loaded from: classes.dex */
    static class HoneycombKeyEventVersionImpl extends EclairKeyEventVersionImpl {
        HoneycombKeyEventVersionImpl() {
        }

        @Override // android.support.v4.view.KeyEventCompat.BaseKeyEventVersionImpl
        public int normalizeMetaState(int metaState) {
            return KeyEventCompatHoneycomb.normalizeMetaState(metaState);
        }

        @Override // android.support.v4.view.KeyEventCompat.BaseKeyEventVersionImpl, android.support.v4.view.KeyEventCompat.KeyEventVersionImpl
        public boolean metaStateHasModifiers(int metaState, int modifiers) {
            return KeyEventCompatHoneycomb.metaStateHasModifiers(metaState, modifiers);
        }

        @Override // android.support.v4.view.KeyEventCompat.BaseKeyEventVersionImpl, android.support.v4.view.KeyEventCompat.KeyEventVersionImpl
        public boolean metaStateHasNoModifiers(int metaState) {
            return KeyEventCompatHoneycomb.metaStateHasNoModifiers(metaState);
        }
    }

    static {
        if (Build.VERSION.SDK_INT >= 11) {
            IMPL = new HoneycombKeyEventVersionImpl();
        } else {
            IMPL = new BaseKeyEventVersionImpl();
        }
    }

    public static boolean hasModifiers(KeyEvent event, int modifiers) {
        return IMPL.metaStateHasModifiers(event.getMetaState(), modifiers);
    }

    public static boolean hasNoModifiers(KeyEvent event) {
        return IMPL.metaStateHasNoModifiers(event.getMetaState());
    }

    public static void startTracking(KeyEvent event) {
        IMPL.startTracking(event);
    }

    private KeyEventCompat() {
    }
}
