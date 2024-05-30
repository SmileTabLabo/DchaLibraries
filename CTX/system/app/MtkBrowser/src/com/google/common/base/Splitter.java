package com.google.common.base;

import java.util.Iterator;
/* loaded from: classes.dex */
public final class Splitter {
    private final int limit;
    private final boolean omitEmptyStrings;
    private final Strategy strategy;
    private final CharMatcher trimmer;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public interface Strategy {
        Iterator<String> iterator(Splitter splitter, CharSequence charSequence);
    }

    private Splitter(Strategy strategy) {
        this(strategy, false, CharMatcher.NONE, Integer.MAX_VALUE);
    }

    private Splitter(Strategy strategy, boolean z, CharMatcher charMatcher, int i) {
        this.strategy = strategy;
        this.omitEmptyStrings = z;
        this.trimmer = charMatcher;
        this.limit = i;
    }

    public static Splitter on(final String str) {
        Preconditions.checkArgument(str.length() != 0, "The separator may not be the empty string.");
        return new Splitter(new Strategy() { // from class: com.google.common.base.Splitter.2
            @Override // com.google.common.base.Splitter.Strategy
            public SplittingIterator iterator(Splitter splitter, CharSequence charSequence) {
                return new SplittingIterator(splitter, charSequence) { // from class: com.google.common.base.Splitter.2.1
                    /* JADX WARN: Code restructure failed: missing block: B:8:0x0027, code lost:
                        r6 = r6 + 1;
                     */
                    @Override // com.google.common.base.Splitter.SplittingIterator
                    /*
                        Code decompiled incorrectly, please refer to instructions dump.
                    */
                    public int separatorStart(int i) {
                        int length = str.length();
                        int length2 = this.toSplit.length() - length;
                        while (i <= length2) {
                            for (int i2 = 0; i2 < length; i2++) {
                                if (this.toSplit.charAt(i2 + i) != str.charAt(i2)) {
                                    break;
                                }
                            }
                            return i;
                        }
                        return -1;
                    }

                    @Override // com.google.common.base.Splitter.SplittingIterator
                    public int separatorEnd(int i) {
                        return i + str.length();
                    }
                };
            }
        });
    }

    public Splitter omitEmptyStrings() {
        return new Splitter(this.strategy, true, this.trimmer, this.limit);
    }

    public Iterable<String> split(final CharSequence charSequence) {
        Preconditions.checkNotNull(charSequence);
        return new Iterable<String>() { // from class: com.google.common.base.Splitter.5
            @Override // java.lang.Iterable
            public Iterator<String> iterator() {
                return Splitter.this.splittingIterator(charSequence);
            }

            public String toString() {
                Joiner on = Joiner.on(", ");
                StringBuilder sb = new StringBuilder();
                sb.append('[');
                StringBuilder appendTo = on.appendTo(sb, this);
                appendTo.append(']');
                return appendTo.toString();
            }
        };
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Iterator<String> splittingIterator(CharSequence charSequence) {
        return this.strategy.iterator(this, charSequence);
    }

    /* loaded from: classes.dex */
    private static abstract class SplittingIterator extends AbstractIterator<String> {
        int limit;
        int offset = 0;
        final boolean omitEmptyStrings;
        final CharSequence toSplit;
        final CharMatcher trimmer;

        abstract int separatorEnd(int i);

        abstract int separatorStart(int i);

        protected SplittingIterator(Splitter splitter, CharSequence charSequence) {
            this.trimmer = splitter.trimmer;
            this.omitEmptyStrings = splitter.omitEmptyStrings;
            this.limit = splitter.limit;
            this.toSplit = charSequence;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // com.google.common.base.AbstractIterator
        public String computeNext() {
            int i = this.offset;
            while (this.offset != -1) {
                int separatorStart = separatorStart(this.offset);
                if (separatorStart == -1) {
                    separatorStart = this.toSplit.length();
                    this.offset = -1;
                } else {
                    this.offset = separatorEnd(separatorStart);
                }
                if (this.offset == i) {
                    this.offset++;
                    if (this.offset >= this.toSplit.length()) {
                        this.offset = -1;
                    }
                } else {
                    while (i < separatorStart && this.trimmer.matches(this.toSplit.charAt(i))) {
                        i++;
                    }
                    while (separatorStart > i && this.trimmer.matches(this.toSplit.charAt(separatorStart - 1))) {
                        separatorStart--;
                    }
                    if (this.omitEmptyStrings && i == separatorStart) {
                        i = this.offset;
                    } else {
                        if (this.limit == 1) {
                            separatorStart = this.toSplit.length();
                            this.offset = -1;
                            while (separatorStart > i && this.trimmer.matches(this.toSplit.charAt(separatorStart - 1))) {
                                separatorStart--;
                            }
                        } else {
                            this.limit--;
                        }
                        return this.toSplit.subSequence(i, separatorStart).toString();
                    }
                }
            }
            return endOfData();
        }
    }
}
