package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Booleans;
import java.io.Serializable;
import java.lang.Comparable;
import javax.annotation.Nullable;
/* JADX INFO: Access modifiers changed from: package-private */
@GwtCompatible
/* loaded from: a.zip:com/google/common/collect/Cut.class */
public abstract class Cut<C extends Comparable> implements Comparable<Cut<C>>, Serializable {
    private static final long serialVersionUID = 0;
    final C endpoint;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/google/common/collect/Cut$AboveAll.class */
    public static final class AboveAll extends Cut<Comparable<?>> {
        private static final AboveAll INSTANCE = new AboveAll();
        private static final long serialVersionUID = 0;

        private AboveAll() {
            super(null);
        }

        private Object readResolve() {
            return INSTANCE;
        }

        @Override // com.google.common.collect.Cut
        public int compareTo(Cut<Comparable<?>> cut) {
            return cut == this ? 0 : 1;
        }

        @Override // com.google.common.collect.Cut
        void describeAsLowerBound(StringBuilder sb) {
            throw new AssertionError();
        }

        @Override // com.google.common.collect.Cut
        void describeAsUpperBound(StringBuilder sb) {
            sb.append("+∞)");
        }

        @Override // com.google.common.collect.Cut
        Comparable<?> endpoint() {
            throw new IllegalStateException("range unbounded on this side");
        }

        @Override // com.google.common.collect.Cut
        boolean isLessThan(Comparable<?> comparable) {
            return false;
        }

        public String toString() {
            return "+∞";
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/google/common/collect/Cut$AboveValue.class */
    public static final class AboveValue<C extends Comparable> extends Cut<C> {
        private static final long serialVersionUID = 0;

        AboveValue(C c) {
            super((Comparable) Preconditions.checkNotNull(c));
        }

        @Override // com.google.common.collect.Cut
        void describeAsLowerBound(StringBuilder sb) {
            sb.append('(').append(this.endpoint);
        }

        @Override // com.google.common.collect.Cut
        void describeAsUpperBound(StringBuilder sb) {
            sb.append(this.endpoint).append(']');
        }

        public int hashCode() {
            return this.endpoint.hashCode() ^ (-1);
        }

        @Override // com.google.common.collect.Cut
        boolean isLessThan(C c) {
            boolean z = false;
            if (Range.compareOrThrow(this.endpoint, c) < 0) {
                z = true;
            }
            return z;
        }

        public String toString() {
            return "/" + this.endpoint + "\\";
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/google/common/collect/Cut$BelowAll.class */
    public static final class BelowAll extends Cut<Comparable<?>> {
        private static final BelowAll INSTANCE = new BelowAll();
        private static final long serialVersionUID = 0;

        private BelowAll() {
            super(null);
        }

        private Object readResolve() {
            return INSTANCE;
        }

        @Override // com.google.common.collect.Cut
        public int compareTo(Cut<Comparable<?>> cut) {
            return cut == this ? 0 : -1;
        }

        @Override // com.google.common.collect.Cut
        void describeAsLowerBound(StringBuilder sb) {
            sb.append("(-∞");
        }

        @Override // com.google.common.collect.Cut
        void describeAsUpperBound(StringBuilder sb) {
            throw new AssertionError();
        }

        @Override // com.google.common.collect.Cut
        Comparable<?> endpoint() {
            throw new IllegalStateException("range unbounded on this side");
        }

        @Override // com.google.common.collect.Cut
        boolean isLessThan(Comparable<?> comparable) {
            return true;
        }

        public String toString() {
            return "-∞";
        }
    }

    /* loaded from: a.zip:com/google/common/collect/Cut$BelowValue.class */
    private static final class BelowValue<C extends Comparable> extends Cut<C> {
        private static final long serialVersionUID = 0;

        BelowValue(C c) {
            super((Comparable) Preconditions.checkNotNull(c));
        }

        @Override // com.google.common.collect.Cut
        void describeAsLowerBound(StringBuilder sb) {
            sb.append('[').append(this.endpoint);
        }

        @Override // com.google.common.collect.Cut
        void describeAsUpperBound(StringBuilder sb) {
            sb.append(this.endpoint).append(')');
        }

        public int hashCode() {
            return this.endpoint.hashCode();
        }

        @Override // com.google.common.collect.Cut
        boolean isLessThan(C c) {
            boolean z = false;
            if (Range.compareOrThrow(this.endpoint, c) <= 0) {
                z = true;
            }
            return z;
        }

        public String toString() {
            return "\\" + this.endpoint + "/";
        }
    }

    Cut(@Nullable C c) {
        this.endpoint = c;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static <C extends Comparable> Cut<C> aboveAll() {
        return AboveAll.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static <C extends Comparable> Cut<C> aboveValue(C c) {
        return new AboveValue(c);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static <C extends Comparable> Cut<C> belowAll() {
        return BelowAll.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static <C extends Comparable> Cut<C> belowValue(C c) {
        return new BelowValue(c);
    }

    public int compareTo(Cut<C> cut) {
        if (cut == belowAll()) {
            return 1;
        }
        if (cut == aboveAll()) {
            return -1;
        }
        int compareOrThrow = Range.compareOrThrow(this.endpoint, cut.endpoint);
        return compareOrThrow != 0 ? compareOrThrow : Booleans.compare(this instanceof AboveValue, cut instanceof AboveValue);
    }

    @Override // java.lang.Comparable
    public /* bridge */ /* synthetic */ int compareTo(Object obj) {
        return compareTo((Cut) ((Cut) obj));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract void describeAsLowerBound(StringBuilder sb);

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract void describeAsUpperBound(StringBuilder sb);

    /* JADX INFO: Access modifiers changed from: package-private */
    public C endpoint() {
        return this.endpoint;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj instanceof Cut) {
            try {
                if (compareTo((Cut) ((Cut) obj)) == 0) {
                    z = true;
                }
                return z;
            } catch (ClassCastException e) {
                return false;
            }
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract boolean isLessThan(C c);
}
