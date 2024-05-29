package com.google.common.collect;

import com.google.common.annotations.VisibleForTesting;
import java.lang.Comparable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import javax.annotation.Nullable;
/* JADX INFO: Access modifiers changed from: package-private */
@VisibleForTesting
/* loaded from: a.zip:com/google/common/collect/TreeRangeSet$RangesByUpperBound.class */
public final class TreeRangeSet$RangesByUpperBound<C extends Comparable<?>> extends AbstractNavigableMap<Cut<C>, Range<C>> {
    private final NavigableMap<Cut<C>, Range<C>> rangesByLowerBound;
    private final Range<Cut<C>> upperBoundWindow;

    private TreeRangeSet$RangesByUpperBound(NavigableMap<Cut<C>, Range<C>> navigableMap, Range<Cut<C>> range) {
        this.rangesByLowerBound = navigableMap;
        this.upperBoundWindow = range;
    }

    private NavigableMap<Cut<C>, Range<C>> subMap(Range<Cut<C>> range) {
        return range.isConnected(this.upperBoundWindow) ? new TreeRangeSet$RangesByUpperBound(this.rangesByLowerBound, range.intersection(this.upperBoundWindow)) : ImmutableSortedMap.of();
    }

    @Override // java.util.SortedMap
    public Comparator<? super Cut<C>> comparator() {
        return Ordering.natural();
    }

    @Override // java.util.AbstractMap, java.util.Map
    public boolean containsKey(@Nullable Object obj) {
        return get(obj) != null;
    }

    @Override // com.google.common.collect.AbstractNavigableMap
    Iterator<Map.Entry<Cut<C>, Range<C>>> descendingEntryIterator() {
        PeekingIterator peekingIterator = Iterators.peekingIterator((this.upperBoundWindow.hasUpperBound() ? this.rangesByLowerBound.headMap(this.upperBoundWindow.upperEndpoint(), false).descendingMap().values() : this.rangesByLowerBound.descendingMap().values()).iterator());
        if (peekingIterator.hasNext() && this.upperBoundWindow.upperBound.isLessThan(((Range) peekingIterator.peek()).upperBound)) {
            peekingIterator.next();
        }
        return new AbstractIterator<Map.Entry<Cut<C>, Range<C>>>(this, peekingIterator) { // from class: com.google.common.collect.TreeRangeSet$RangesByUpperBound.2
            final TreeRangeSet$RangesByUpperBound this$1;
            final PeekingIterator val$backingItr;

            {
                this.this$1 = this;
                this.val$backingItr = peekingIterator;
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // com.google.common.collect.AbstractIterator
            public Map.Entry<Cut<C>, Range<C>> computeNext() {
                if (this.val$backingItr.hasNext()) {
                    Range range = (Range) this.val$backingItr.next();
                    return this.this$1.upperBoundWindow.lowerBound.isLessThan(range.upperBound) ? Maps.immutableEntry(range.upperBound, range) : endOfData();
                }
                return (Map.Entry) endOfData();
            }
        };
    }

    @Override // com.google.common.collect.AbstractNavigableMap
    Iterator<Map.Entry<Cut<C>, Range<C>>> entryIterator() {
        Iterator<Range<C>> it;
        if (this.upperBoundWindow.hasLowerBound()) {
            Map.Entry<Cut<C>, Range<C>> lowerEntry = this.rangesByLowerBound.lowerEntry(this.upperBoundWindow.lowerEndpoint());
            it = lowerEntry == null ? this.rangesByLowerBound.values().iterator() : this.upperBoundWindow.lowerBound.isLessThan(lowerEntry.getValue().upperBound) ? this.rangesByLowerBound.tailMap(lowerEntry.getKey(), true).values().iterator() : this.rangesByLowerBound.tailMap(this.upperBoundWindow.lowerEndpoint(), true).values().iterator();
        } else {
            it = this.rangesByLowerBound.values().iterator();
        }
        return new AbstractIterator<Map.Entry<Cut<C>, Range<C>>>(this, it) { // from class: com.google.common.collect.TreeRangeSet$RangesByUpperBound.1
            final TreeRangeSet$RangesByUpperBound this$1;
            final Iterator val$backingItr;

            {
                this.this$1 = this;
                this.val$backingItr = it;
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // com.google.common.collect.AbstractIterator
            public Map.Entry<Cut<C>, Range<C>> computeNext() {
                if (this.val$backingItr.hasNext()) {
                    Range range = (Range) this.val$backingItr.next();
                    return this.this$1.upperBoundWindow.upperBound.isLessThan(range.upperBound) ? (Map.Entry) endOfData() : Maps.immutableEntry(range.upperBound, range);
                }
                return (Map.Entry) endOfData();
            }
        };
    }

    @Override // com.google.common.collect.AbstractNavigableMap, java.util.AbstractMap, java.util.Map
    public Range<C> get(@Nullable Object obj) {
        Map.Entry<Cut<C>, Range<C>> lowerEntry;
        if (obj instanceof Cut) {
            try {
                Cut<C> cut = (Cut) obj;
                if (this.upperBoundWindow.contains(cut) && (lowerEntry = this.rangesByLowerBound.lowerEntry(cut)) != null && lowerEntry.getValue().upperBound.equals(cut)) {
                    return lowerEntry.getValue();
                }
                return null;
            } catch (ClassCastException e) {
                return null;
            }
        }
        return null;
    }

    public NavigableMap<Cut<C>, Range<C>> headMap(Cut<C> cut, boolean z) {
        return subMap(Range.upTo(cut, BoundType.forBoolean(z)));
    }

    @Override // java.util.NavigableMap
    public /* bridge */ /* synthetic */ NavigableMap headMap(Object obj, boolean z) {
        return headMap((Cut) ((Cut) obj), z);
    }

    @Override // java.util.AbstractMap, java.util.Map
    public boolean isEmpty() {
        return this.upperBoundWindow.equals(Range.all()) ? this.rangesByLowerBound.isEmpty() : !entryIterator().hasNext();
    }

    @Override // com.google.common.collect.AbstractNavigableMap, java.util.AbstractMap, java.util.Map
    public int size() {
        return this.upperBoundWindow.equals(Range.all()) ? this.rangesByLowerBound.size() : Iterators.size(entryIterator());
    }

    public NavigableMap<Cut<C>, Range<C>> subMap(Cut<C> cut, boolean z, Cut<C> cut2, boolean z2) {
        return subMap(Range.range(cut, BoundType.forBoolean(z), cut2, BoundType.forBoolean(z2)));
    }

    @Override // java.util.NavigableMap
    public /* bridge */ /* synthetic */ NavigableMap subMap(Object obj, boolean z, Object obj2, boolean z2) {
        return subMap((Cut) ((Cut) obj), z, (Cut) ((Cut) obj2), z2);
    }

    public NavigableMap<Cut<C>, Range<C>> tailMap(Cut<C> cut, boolean z) {
        return subMap(Range.downTo(cut, BoundType.forBoolean(z)));
    }

    @Override // java.util.NavigableMap
    public /* bridge */ /* synthetic */ NavigableMap tailMap(Object obj, boolean z) {
        return tailMap((Cut) ((Cut) obj), z);
    }
}
