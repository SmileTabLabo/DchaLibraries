package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import java.io.Serializable;
import java.lang.Comparable;
import javax.annotation.Nullable;
@GwtCompatible
/* loaded from: a.zip:com/google/common/collect/Range.class */
public final class Range<C extends Comparable> implements Predicate<C>, Serializable {

    /* renamed from: -com-google-common-collect-BoundTypeSwitchesValues  reason: not valid java name */
    private static final int[] f2comgooglecommoncollectBoundTypeSwitchesValues = null;
    private static final long serialVersionUID = 0;
    final Cut<C> lowerBound;
    final Cut<C> upperBound;
    private static final Function<Range, Cut> LOWER_BOUND_FN = new Function<Range, Cut>() { // from class: com.google.common.collect.Range.1
        @Override // com.google.common.base.Function
        public Cut apply(Range range) {
            return range.lowerBound;
        }
    };
    private static final Function<Range, Cut> UPPER_BOUND_FN = new Function<Range, Cut>() { // from class: com.google.common.collect.Range.2
        @Override // com.google.common.base.Function
        public Cut apply(Range range) {
            return range.upperBound;
        }
    };
    static final Ordering<Range<?>> RANGE_LEX_ORDERING = new Ordering<Range<?>>() { // from class: com.google.common.collect.Range.3
        @Override // com.google.common.collect.Ordering, java.util.Comparator
        public int compare(Range<?> range, Range<?> range2) {
            return ComparisonChain.start().compare(range.lowerBound, range2.lowerBound).compare(range.upperBound, range2.upperBound).result();
        }
    };
    private static final Range<Comparable> ALL = new Range<>(Cut.belowAll(), Cut.aboveAll());

    /* renamed from: -getcom-google-common-collect-BoundTypeSwitchesValues  reason: not valid java name */
    private static /* synthetic */ int[] m90getcomgooglecommoncollectBoundTypeSwitchesValues() {
        if (f2comgooglecommoncollectBoundTypeSwitchesValues != null) {
            return f2comgooglecommoncollectBoundTypeSwitchesValues;
        }
        int[] iArr = new int[BoundType.valuesCustom().length];
        try {
            iArr[BoundType.CLOSED.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[BoundType.OPEN.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        f2comgooglecommoncollectBoundTypeSwitchesValues = iArr;
        return iArr;
    }

    private Range(Cut<C> cut, Cut<C> cut2) {
        if (cut.compareTo((Cut) cut2) > 0 || cut == Cut.aboveAll() || cut2 == Cut.belowAll()) {
            throw new IllegalArgumentException("Invalid range: " + toString(cut, cut2));
        }
        this.lowerBound = (Cut) Preconditions.checkNotNull(cut);
        this.upperBound = (Cut) Preconditions.checkNotNull(cut2);
    }

    public static <C extends Comparable<?>> Range<C> all() {
        return (Range<C>) ALL;
    }

    public static <C extends Comparable<?>> Range<C> atLeast(C c) {
        return create(Cut.belowValue(c), Cut.aboveAll());
    }

    public static <C extends Comparable<?>> Range<C> atMost(C c) {
        return create(Cut.belowAll(), Cut.aboveValue(c));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int compareOrThrow(Comparable comparable, Comparable comparable2) {
        return comparable.compareTo(comparable2);
    }

    static <C extends Comparable<?>> Range<C> create(Cut<C> cut, Cut<C> cut2) {
        return new Range<>(cut, cut2);
    }

    public static <C extends Comparable<?>> Range<C> downTo(C c, BoundType boundType) {
        switch (m90getcomgooglecommoncollectBoundTypeSwitchesValues()[boundType.ordinal()]) {
            case 1:
                return atLeast(c);
            case 2:
                return greaterThan(c);
            default:
                throw new AssertionError();
        }
    }

    public static <C extends Comparable<?>> Range<C> greaterThan(C c) {
        return create(Cut.aboveValue(c), Cut.aboveAll());
    }

    public static <C extends Comparable<?>> Range<C> lessThan(C c) {
        return create(Cut.belowAll(), Cut.belowValue(c));
    }

    public static <C extends Comparable<?>> Range<C> range(C c, BoundType boundType, C c2, BoundType boundType2) {
        Preconditions.checkNotNull(boundType);
        Preconditions.checkNotNull(boundType2);
        return create(boundType == BoundType.OPEN ? Cut.aboveValue(c) : Cut.belowValue(c), boundType2 == BoundType.OPEN ? Cut.belowValue(c2) : Cut.aboveValue(c2));
    }

    private static String toString(Cut<?> cut, Cut<?> cut2) {
        StringBuilder sb = new StringBuilder(16);
        cut.describeAsLowerBound(sb);
        sb.append((char) 8229);
        cut2.describeAsUpperBound(sb);
        return sb.toString();
    }

    public static <C extends Comparable<?>> Range<C> upTo(C c, BoundType boundType) {
        switch (m90getcomgooglecommoncollectBoundTypeSwitchesValues()[boundType.ordinal()]) {
            case 1:
                return atMost(c);
            case 2:
                return lessThan(c);
            default:
                throw new AssertionError();
        }
    }

    @Deprecated
    public boolean apply(C c) {
        return contains(c);
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.google.common.base.Predicate
    public /* bridge */ /* synthetic */ boolean apply(Object obj) {
        return apply((Range<C>) ((Comparable) obj));
    }

    public boolean contains(C c) {
        Preconditions.checkNotNull(c);
        boolean z = false;
        if (this.lowerBound.isLessThan(c)) {
            z = !this.upperBound.isLessThan(c);
        }
        return z;
    }

    public boolean equals(@Nullable Object obj) {
        boolean z = false;
        if (obj instanceof Range) {
            Range range = (Range) obj;
            if (this.lowerBound.equals(range.lowerBound)) {
                z = this.upperBound.equals(range.upperBound);
            }
            return z;
        }
        return false;
    }

    public boolean hasLowerBound() {
        return this.lowerBound != Cut.belowAll();
    }

    public boolean hasUpperBound() {
        return this.upperBound != Cut.aboveAll();
    }

    public int hashCode() {
        return (this.lowerBound.hashCode() * 31) + this.upperBound.hashCode();
    }

    public Range<C> intersection(Range<C> range) {
        int compareTo = this.lowerBound.compareTo((Cut) range.lowerBound);
        int compareTo2 = this.upperBound.compareTo((Cut) range.upperBound);
        if (compareTo < 0 || compareTo2 > 0) {
            if (compareTo > 0 || compareTo2 < 0) {
                return create(compareTo >= 0 ? this.lowerBound : range.lowerBound, compareTo2 <= 0 ? this.upperBound : range.upperBound);
            }
            return range;
        }
        return this;
    }

    public boolean isConnected(Range<C> range) {
        boolean z = false;
        if (this.lowerBound.compareTo((Cut) range.upperBound) <= 0) {
            z = false;
            if (range.lowerBound.compareTo((Cut) this.upperBound) <= 0) {
                z = true;
            }
        }
        return z;
    }

    public C lowerEndpoint() {
        return this.lowerBound.endpoint();
    }

    Object readResolve() {
        return equals(ALL) ? all() : this;
    }

    public String toString() {
        return toString(this.lowerBound, this.upperBound);
    }

    public C upperEndpoint() {
        return this.upperBound.endpoint();
    }
}
