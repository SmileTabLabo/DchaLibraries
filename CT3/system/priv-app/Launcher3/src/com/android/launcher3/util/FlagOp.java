package com.android.launcher3.util;
/* loaded from: a.zip:com/android/launcher3/util/FlagOp.class */
public abstract class FlagOp {
    public static FlagOp NO_OP = new FlagOp() { // from class: com.android.launcher3.util.FlagOp.1
    };

    private FlagOp() {
    }

    /* synthetic */ FlagOp(FlagOp flagOp) {
        this();
    }

    public static FlagOp addFlag(int i) {
        return new FlagOp(i) { // from class: com.android.launcher3.util.FlagOp.2
            final int val$flag;

            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            {
                super(null);
                this.val$flag = i;
            }

            @Override // com.android.launcher3.util.FlagOp
            public int apply(int i2) {
                return this.val$flag | i2;
            }
        };
    }

    public static FlagOp removeFlag(int i) {
        return new FlagOp(i) { // from class: com.android.launcher3.util.FlagOp.3
            final int val$flag;

            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            {
                super(null);
                this.val$flag = i;
            }

            @Override // com.android.launcher3.util.FlagOp
            public int apply(int i2) {
                return (this.val$flag ^ (-1)) & i2;
            }
        };
    }

    public int apply(int i) {
        return i;
    }
}
