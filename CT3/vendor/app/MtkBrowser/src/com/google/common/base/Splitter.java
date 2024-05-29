package com.google.common.base;

import com.google.common.annotations.GwtCompatible;
import java.util.Iterator;
import javax.annotation.CheckReturnValue;
@GwtCompatible(emulated = true)
/* loaded from: b.zip:com/google/common/base/Splitter.class */
public final class Splitter {
    private final int limit;
    private final boolean omitEmptyStrings;
    private final Strategy strategy;
    private final CharMatcher trimmer;

    /* loaded from: b.zip:com/google/common/base/Splitter$SplittingIterator.class */
    private static abstract class SplittingIterator extends AbstractIterator<String> {
        int limit;
        int offset = 0;
        final boolean omitEmptyStrings;
        final CharSequence toSplit;
        final CharMatcher trimmer;

        protected SplittingIterator(Splitter splitter, CharSequence charSequence) {
            this.trimmer = splitter.trimmer;
            this.omitEmptyStrings = splitter.omitEmptyStrings;
            this.limit = splitter.limit;
            this.toSplit = charSequence;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // com.google.common.base.AbstractIterator
        public String computeNext() {
            int i;
            int i2;
            int i3;
            int i4 = this.offset;
            while (this.offset != -1) {
                int i5 = i4;
                int separatorStart = separatorStart(this.offset);
                if (separatorStart == -1) {
                    i = this.toSplit.length();
                    this.offset = -1;
                } else {
                    i = separatorStart;
                    this.offset = separatorEnd(separatorStart);
                }
                if (this.offset == i4) {
                    this.offset++;
                    if (this.offset >= this.toSplit.length()) {
                        this.offset = -1;
                    }
                } else {
                    while (true) {
                        i2 = i;
                        if (i5 >= i) {
                            break;
                        }
                        i2 = i;
                        if (!this.trimmer.matches(this.toSplit.charAt(i5))) {
                            break;
                        }
                        i5++;
                    }
                    while (i2 > i5 && this.trimmer.matches(this.toSplit.charAt(i2 - 1))) {
                        i2--;
                    }
                    if (!this.omitEmptyStrings || i5 != i2) {
                        if (this.limit == 1) {
                            int length = this.toSplit.length();
                            this.offset = -1;
                            while (true) {
                                i3 = length;
                                if (length <= i5) {
                                    break;
                                }
                                i3 = length;
                                if (!this.trimmer.matches(this.toSplit.charAt(length - 1))) {
                                    break;
                                }
                                length--;
                            }
                        } else {
                            this.limit--;
                            i3 = i2;
                        }
                        return this.toSplit.subSequence(i5, i3).toString();
                    }
                    i4 = this.offset;
                }
            }
            return endOfData();
        }

        abstract int separatorEnd(int i);

        abstract int separatorStart(int i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: b.zip:com/google/common/base/Splitter$Strategy.class */
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

    public static Splitter on(String str) {
        boolean z = false;
        if (str.length() != 0) {
            z = true;
        }
        Preconditions.checkArgument(z, "The separator may not be the empty string.");
        return new Splitter(new Strategy(str) { // from class: com.google.common.base.Splitter.2
            final String val$separator;

            {
                this.val$separator = str;
            }

            @Override // com.google.common.base.Splitter.Strategy
            public SplittingIterator iterator(Splitter splitter, CharSequence charSequence) {
                return new SplittingIterator(this, splitter, charSequence, this.val$separator) { // from class: com.google.common.base.Splitter.2.1
                    final AnonymousClass2 this$1;
                    final String val$separator;

                    {
                        this.this$1 = this;
                        this.val$separator = r8;
                    }

                    @Override // com.google.common.base.Splitter.SplittingIterator
                    public int separatorEnd(int i) {
                        return this.val$separator.length() + i;
                    }

                    /* JADX WARN: Code restructure failed: missing block: B:10:0x003b, code lost:
                        r5 = r5 + 1;
                     */
                    @Override // com.google.common.base.Splitter.SplittingIterator
                    /*
                        Code decompiled incorrectly, please refer to instructions dump.
                    */
                    public int separatorStart(int i) {
                        int length = this.val$separator.length();
                        int length2 = this.toSplit.length();
                        while (i <= length2 - length) {
                            for (int i2 = 0; i2 < length; i2++) {
                                if (this.toSplit.charAt(i2 + i) != this.val$separator.charAt(i2)) {
                                    break;
                                }
                            }
                            return i;
                        }
                        return -1;
                    }
                };
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Iterator<String> splittingIterator(CharSequence charSequence) {
        return this.strategy.iterator(this, charSequence);
    }

    @CheckReturnValue
    public Splitter omitEmptyStrings() {
        return new Splitter(this.strategy, true, this.trimmer, this.limit);
    }

    public Iterable<String> split(CharSequence charSequence) {
        Preconditions.checkNotNull(charSequence);
        return new Iterable<String>(this, charSequence) { // from class: com.google.common.base.Splitter.5
            final Splitter this$0;
            final CharSequence val$sequence;

            {
                this.this$0 = this;
                this.val$sequence = charSequence;
            }

            @Override // java.lang.Iterable
            public Iterator<String> iterator() {
                return this.this$0.splittingIterator(this.val$sequence);
            }

            public String toString() {
                return Joiner.on(", ").appendTo(new StringBuilder().append('['), this).append(']').toString();
            }
        };
    }
}
