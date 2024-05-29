package com.google.common.base;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
@GwtCompatible
/* loaded from: a.zip:com/google/common/base/Joiner.class */
public class Joiner {
    private final String separator;

    /* loaded from: a.zip:com/google/common/base/Joiner$MapJoiner.class */
    public static final class MapJoiner {
        private final Joiner joiner;
        private final String keyValueSeparator;

        private MapJoiner(Joiner joiner, String str) {
            this.joiner = joiner;
            this.keyValueSeparator = (String) Preconditions.checkNotNull(str);
        }

        /* synthetic */ MapJoiner(Joiner joiner, String str, MapJoiner mapJoiner) {
            this(joiner, str);
        }

        @Beta
        public <A extends Appendable> A appendTo(A a, Iterator<? extends Map.Entry<?, ?>> it) throws IOException {
            Preconditions.checkNotNull(a);
            if (it.hasNext()) {
                Map.Entry<?, ?> next = it.next();
                a.append(this.joiner.toString(next.getKey()));
                a.append(this.keyValueSeparator);
                a.append(this.joiner.toString(next.getValue()));
                while (it.hasNext()) {
                    a.append(this.joiner.separator);
                    Map.Entry<?, ?> next2 = it.next();
                    a.append(this.joiner.toString(next2.getKey()));
                    a.append(this.keyValueSeparator);
                    a.append(this.joiner.toString(next2.getValue()));
                }
            }
            return a;
        }

        @Beta
        public StringBuilder appendTo(StringBuilder sb, Iterable<? extends Map.Entry<?, ?>> iterable) {
            return appendTo(sb, iterable.iterator());
        }

        @Beta
        public StringBuilder appendTo(StringBuilder sb, Iterator<? extends Map.Entry<?, ?>> it) {
            try {
                appendTo((MapJoiner) sb, it);
                return sb;
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }

        public StringBuilder appendTo(StringBuilder sb, Map<?, ?> map) {
            return appendTo(sb, map.entrySet());
        }
    }

    private Joiner(Joiner joiner) {
        this.separator = joiner.separator;
    }

    /* synthetic */ Joiner(Joiner joiner, Joiner joiner2) {
        this(joiner);
    }

    private Joiner(String str) {
        this.separator = (String) Preconditions.checkNotNull(str);
    }

    public static Joiner on(char c) {
        return new Joiner(String.valueOf(c));
    }

    public static Joiner on(String str) {
        return new Joiner(str);
    }

    public <A extends Appendable> A appendTo(A a, Iterator<?> it) throws IOException {
        Preconditions.checkNotNull(a);
        if (it.hasNext()) {
            a.append(toString(it.next()));
            while (it.hasNext()) {
                a.append(this.separator);
                a.append(toString(it.next()));
            }
        }
        return a;
    }

    public final StringBuilder appendTo(StringBuilder sb, Iterable<?> iterable) {
        return appendTo(sb, iterable.iterator());
    }

    public final StringBuilder appendTo(StringBuilder sb, Iterator<?> it) {
        try {
            appendTo((Joiner) sb, it);
            return sb;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v8, types: [java.lang.CharSequence] */
    CharSequence toString(Object obj) {
        Preconditions.checkNotNull(obj);
        return obj instanceof CharSequence ? (CharSequence) obj : obj.toString();
    }

    @CheckReturnValue
    public Joiner useForNull(String str) {
        Preconditions.checkNotNull(str);
        return new Joiner(this, this, str) { // from class: com.google.common.base.Joiner.1
            final Joiner this$0;
            final String val$nullText;

            {
                this.this$0 = this;
                this.val$nullText = str;
            }

            @Override // com.google.common.base.Joiner
            CharSequence toString(@Nullable Object obj) {
                return obj == null ? this.val$nullText : this.this$0.toString(obj);
            }

            @Override // com.google.common.base.Joiner
            public Joiner useForNull(String str2) {
                throw new UnsupportedOperationException("already specified useForNull");
            }
        };
    }

    @CheckReturnValue
    public MapJoiner withKeyValueSeparator(String str) {
        return new MapJoiner(this, str, null);
    }
}
