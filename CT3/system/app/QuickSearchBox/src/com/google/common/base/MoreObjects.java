package com.google.common.base;

import com.google.common.annotations.GwtCompatible;
import javax.annotation.Nullable;
@GwtCompatible
/* loaded from: a.zip:com/google/common/base/MoreObjects.class */
public final class MoreObjects {

    /* loaded from: a.zip:com/google/common/base/MoreObjects$ToStringHelper.class */
    public static final class ToStringHelper {
        private final String className;
        private ValueHolder holderHead;
        private ValueHolder holderTail;
        private boolean omitNullValues;

        /* JADX INFO: Access modifiers changed from: private */
        /* loaded from: a.zip:com/google/common/base/MoreObjects$ToStringHelper$ValueHolder.class */
        public static final class ValueHolder {
            String name;
            ValueHolder next;
            Object value;

            private ValueHolder() {
            }

            /* synthetic */ ValueHolder(ValueHolder valueHolder) {
                this();
            }
        }

        private ToStringHelper(String str) {
            this.holderHead = new ValueHolder(null);
            this.holderTail = this.holderHead;
            this.omitNullValues = false;
            this.className = (String) Preconditions.checkNotNull(str);
        }

        /* synthetic */ ToStringHelper(String str, ToStringHelper toStringHelper) {
            this(str);
        }

        private ValueHolder addHolder() {
            ValueHolder valueHolder = new ValueHolder(null);
            this.holderTail.next = valueHolder;
            this.holderTail = valueHolder;
            return valueHolder;
        }

        private ToStringHelper addHolder(@Nullable Object obj) {
            addHolder().value = obj;
            return this;
        }

        private ToStringHelper addHolder(String str, @Nullable Object obj) {
            ValueHolder addHolder = addHolder();
            addHolder.value = obj;
            addHolder.name = (String) Preconditions.checkNotNull(str);
            return this;
        }

        public ToStringHelper add(String str, int i) {
            return addHolder(str, String.valueOf(i));
        }

        public ToStringHelper add(String str, @Nullable Object obj) {
            return addHolder(str, obj);
        }

        public ToStringHelper addValue(@Nullable Object obj) {
            return addHolder(obj);
        }

        public String toString() {
            String str;
            boolean z = this.omitNullValues;
            String str2 = "";
            StringBuilder append = new StringBuilder(32).append(this.className).append('{');
            ValueHolder valueHolder = this.holderHead.next;
            while (valueHolder != null) {
                if (z) {
                    str = str2;
                    if (valueHolder.value == null) {
                        valueHolder = valueHolder.next;
                        str2 = str;
                    }
                }
                append.append(str2);
                str = ", ";
                if (valueHolder.name != null) {
                    append.append(valueHolder.name).append('=');
                }
                append.append(valueHolder.value);
                valueHolder = valueHolder.next;
                str2 = str;
            }
            return append.append('}').toString();
        }
    }

    private MoreObjects() {
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static <T> T firstNonNull(@Nullable T t, @Nullable T t2) {
        if (t == null) {
            t = Preconditions.checkNotNull(t2);
        }
        return t;
    }

    static String simpleName(Class<?> cls) {
        String replaceAll = cls.getName().replaceAll("\\$[0-9]+", "\\$");
        int lastIndexOf = replaceAll.lastIndexOf(36);
        int i = lastIndexOf;
        if (lastIndexOf == -1) {
            i = replaceAll.lastIndexOf(46);
        }
        return replaceAll.substring(i + 1);
    }

    public static ToStringHelper toStringHelper(Object obj) {
        return new ToStringHelper(simpleName(obj.getClass()), null);
    }
}
