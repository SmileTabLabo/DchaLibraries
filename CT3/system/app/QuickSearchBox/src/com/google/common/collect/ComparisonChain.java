package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import javax.annotation.Nullable;
@GwtCompatible
/* loaded from: a.zip:com/google/common/collect/ComparisonChain.class */
public abstract class ComparisonChain {
    private static final ComparisonChain ACTIVE = new ComparisonChain() { // from class: com.google.common.collect.ComparisonChain.1
        ComparisonChain classify(int i) {
            return i < 0 ? ComparisonChain.LESS : i > 0 ? ComparisonChain.GREATER : ComparisonChain.ACTIVE;
        }

        @Override // com.google.common.collect.ComparisonChain
        public ComparisonChain compare(Comparable comparable, Comparable comparable2) {
            return classify(comparable.compareTo(comparable2));
        }

        @Override // com.google.common.collect.ComparisonChain
        public int result() {
            return 0;
        }
    };
    private static final ComparisonChain LESS = new InactiveComparisonChain(-1);
    private static final ComparisonChain GREATER = new InactiveComparisonChain(1);

    /* loaded from: a.zip:com/google/common/collect/ComparisonChain$InactiveComparisonChain.class */
    private static final class InactiveComparisonChain extends ComparisonChain {
        final int result;

        InactiveComparisonChain(int i) {
            super(null);
            this.result = i;
        }

        @Override // com.google.common.collect.ComparisonChain
        public ComparisonChain compare(@Nullable Comparable comparable, @Nullable Comparable comparable2) {
            return this;
        }

        @Override // com.google.common.collect.ComparisonChain
        public int result() {
            return this.result;
        }
    }

    private ComparisonChain() {
    }

    /* synthetic */ ComparisonChain(ComparisonChain comparisonChain) {
        this();
    }

    public static ComparisonChain start() {
        return ACTIVE;
    }

    public abstract ComparisonChain compare(Comparable<?> comparable, Comparable<?> comparable2);

    public abstract int result();
}
