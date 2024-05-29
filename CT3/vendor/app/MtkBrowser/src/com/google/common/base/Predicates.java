package com.google.common.base;

import com.google.common.annotations.GwtCompatible;
import java.io.Serializable;
import java.util.Collection;
import javax.annotation.Nullable;
@GwtCompatible(emulated = true)
/* loaded from: b.zip:com/google/common/base/Predicates.class */
public final class Predicates {
    private static final Joiner COMMA_JOINER = Joiner.on(',');

    /* loaded from: b.zip:com/google/common/base/Predicates$InPredicate.class */
    private static class InPredicate<T> implements Predicate<T>, Serializable {
        private static final long serialVersionUID = 0;
        private final Collection<?> target;

        private InPredicate(Collection<?> collection) {
            this.target = (Collection) Preconditions.checkNotNull(collection);
        }

        /* synthetic */ InPredicate(Collection collection, InPredicate inPredicate) {
            this(collection);
        }

        @Override // com.google.common.base.Predicate
        public boolean apply(@Nullable T t) {
            try {
                return this.target.contains(t);
            } catch (ClassCastException e) {
                return false;
            } catch (NullPointerException e2) {
                return false;
            }
        }

        public boolean equals(@Nullable Object obj) {
            if (obj instanceof InPredicate) {
                return this.target.equals(((InPredicate) obj).target);
            }
            return false;
        }

        public int hashCode() {
            return this.target.hashCode();
        }

        public String toString() {
            return "Predicates.in(" + this.target + ")";
        }
    }

    /* loaded from: b.zip:com/google/common/base/Predicates$IsEqualToPredicate.class */
    private static class IsEqualToPredicate<T> implements Predicate<T>, Serializable {
        private static final long serialVersionUID = 0;
        private final T target;

        private IsEqualToPredicate(T t) {
            this.target = t;
        }

        /* synthetic */ IsEqualToPredicate(Object obj, IsEqualToPredicate isEqualToPredicate) {
            this(obj);
        }

        @Override // com.google.common.base.Predicate
        public boolean apply(T t) {
            return this.target.equals(t);
        }

        public boolean equals(@Nullable Object obj) {
            if (obj instanceof IsEqualToPredicate) {
                return this.target.equals(((IsEqualToPredicate) obj).target);
            }
            return false;
        }

        public int hashCode() {
            return this.target.hashCode();
        }

        public String toString() {
            return "Predicates.equalTo(" + this.target + ")";
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: b.zip:com/google/common/base/Predicates$ObjectPredicate.class */
    public enum ObjectPredicate implements Predicate<Object> {
        ALWAYS_TRUE { // from class: com.google.common.base.Predicates.ObjectPredicate.1
            @Override // com.google.common.base.Predicate
            public boolean apply(@Nullable Object obj) {
                return true;
            }

            @Override // java.lang.Enum
            public String toString() {
                return "Predicates.alwaysTrue()";
            }
        },
        ALWAYS_FALSE { // from class: com.google.common.base.Predicates.ObjectPredicate.2
            @Override // com.google.common.base.Predicate
            public boolean apply(@Nullable Object obj) {
                return false;
            }

            @Override // java.lang.Enum
            public String toString() {
                return "Predicates.alwaysFalse()";
            }
        },
        IS_NULL { // from class: com.google.common.base.Predicates.ObjectPredicate.3
            @Override // com.google.common.base.Predicate
            public boolean apply(@Nullable Object obj) {
                return obj == null;
            }

            @Override // java.lang.Enum
            public String toString() {
                return "Predicates.isNull()";
            }
        },
        NOT_NULL { // from class: com.google.common.base.Predicates.ObjectPredicate.4
            @Override // com.google.common.base.Predicate
            public boolean apply(@Nullable Object obj) {
                return obj != null;
            }

            @Override // java.lang.Enum
            public String toString() {
                return "Predicates.notNull()";
            }
        };

        /* synthetic */ ObjectPredicate(ObjectPredicate objectPredicate) {
            this();
        }

        /* renamed from: values  reason: to resolve conflict with enum method */
        public static ObjectPredicate[] valuesCustom() {
            return values();
        }

        <T> Predicate<T> withNarrowedType() {
            return this;
        }
    }

    private Predicates() {
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v3, types: [com.google.common.base.Predicate] */
    public static <T> Predicate<T> equalTo(@Nullable T t) {
        return t == null ? isNull() : new IsEqualToPredicate(t, null);
    }

    public static <T> Predicate<T> in(Collection<? extends T> collection) {
        return new InPredicate(collection, null);
    }

    @GwtCompatible(serializable = true)
    public static <T> Predicate<T> isNull() {
        return ObjectPredicate.IS_NULL.withNarrowedType();
    }
}
