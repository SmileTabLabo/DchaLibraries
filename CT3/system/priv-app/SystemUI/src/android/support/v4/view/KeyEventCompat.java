package android.support.v4.view;

import android.os.Build;
import android.view.KeyEvent;
/* loaded from: a.zip:android/support/v4/view/KeyEventCompat.class */
public final class KeyEventCompat {
    static final KeyEventVersionImpl IMPL;

    /* loaded from: a.zip:android/support/v4/view/KeyEventCompat$BaseKeyEventVersionImpl.class */
    static class BaseKeyEventVersionImpl implements KeyEventVersionImpl {
        BaseKeyEventVersionImpl() {
        }

        private static int metaStateFilterDirectionalModifiers(int i, int i2, int i3, int i4, int i5) {
            boolean z = (i2 & i3) != 0;
            int i6 = i4 | i5;
            boolean z2 = (i2 & i6) != 0;
            if (!z) {
                return z2 ? (i3 ^ (-1)) & i : i;
            } else if (z2) {
                throw new IllegalArgumentException("bad arguments");
            } else {
                return (i6 ^ (-1)) & i;
            }
        }

        @Override // android.support.v4.view.KeyEventCompat.KeyEventVersionImpl
        public boolean metaStateHasModifiers(int i, int i2) {
            boolean z = true;
            if (metaStateFilterDirectionalModifiers(metaStateFilterDirectionalModifiers(normalizeMetaState(i) & 247, i2, 1, 64, 128), i2, 2, 16, 32) != i2) {
                z = false;
            }
            return z;
        }

        @Override // android.support.v4.view.KeyEventCompat.KeyEventVersionImpl
        public boolean metaStateHasNoModifiers(int i) {
            boolean z = false;
            if ((normalizeMetaState(i) & 247) == 0) {
                z = true;
            }
            return z;
        }

        public int normalizeMetaState(int i) {
            int i2 = i;
            if ((i & 192) != 0) {
                i2 = i | 1;
            }
            int i3 = i2;
            if ((i2 & 48) != 0) {
                i3 = i2 | 2;
            }
            return i3 & 247;
        }

        @Override // android.support.v4.view.KeyEventCompat.KeyEventVersionImpl
        public void startTracking(KeyEvent keyEvent) {
        }
    }

    /* loaded from: a.zip:android/support/v4/view/KeyEventCompat$EclairKeyEventVersionImpl.class */
    static class EclairKeyEventVersionImpl extends BaseKeyEventVersionImpl {
        EclairKeyEventVersionImpl() {
        }

        @Override // android.support.v4.view.KeyEventCompat.BaseKeyEventVersionImpl, android.support.v4.view.KeyEventCompat.KeyEventVersionImpl
        public void startTracking(KeyEvent keyEvent) {
            KeyEventCompatEclair.startTracking(keyEvent);
        }
    }

    /* loaded from: a.zip:android/support/v4/view/KeyEventCompat$HoneycombKeyEventVersionImpl.class */
    static class HoneycombKeyEventVersionImpl extends EclairKeyEventVersionImpl {
        HoneycombKeyEventVersionImpl() {
        }

        @Override // android.support.v4.view.KeyEventCompat.BaseKeyEventVersionImpl, android.support.v4.view.KeyEventCompat.KeyEventVersionImpl
        public boolean metaStateHasModifiers(int i, int i2) {
            return KeyEventCompatHoneycomb.metaStateHasModifiers(i, i2);
        }

        @Override // android.support.v4.view.KeyEventCompat.BaseKeyEventVersionImpl, android.support.v4.view.KeyEventCompat.KeyEventVersionImpl
        public boolean metaStateHasNoModifiers(int i) {
            return KeyEventCompatHoneycomb.metaStateHasNoModifiers(i);
        }

        @Override // android.support.v4.view.KeyEventCompat.BaseKeyEventVersionImpl
        public int normalizeMetaState(int i) {
            return KeyEventCompatHoneycomb.normalizeMetaState(i);
        }
    }

    /* loaded from: a.zip:android/support/v4/view/KeyEventCompat$KeyEventVersionImpl.class */
    interface KeyEventVersionImpl {
        boolean metaStateHasModifiers(int i, int i2);

        boolean metaStateHasNoModifiers(int i);

        void startTracking(KeyEvent keyEvent);
    }

    static {
        if (Build.VERSION.SDK_INT >= 11) {
            IMPL = new HoneycombKeyEventVersionImpl();
        } else {
            IMPL = new BaseKeyEventVersionImpl();
        }
    }

    private KeyEventCompat() {
    }

    public static boolean hasModifiers(KeyEvent keyEvent, int i) {
        return IMPL.metaStateHasModifiers(keyEvent.getMetaState(), i);
    }

    public static boolean hasNoModifiers(KeyEvent keyEvent) {
        return IMPL.metaStateHasNoModifiers(keyEvent.getMetaState());
    }

    public static void startTracking(KeyEvent keyEvent) {
        IMPL.startTracking(keyEvent);
    }
}
