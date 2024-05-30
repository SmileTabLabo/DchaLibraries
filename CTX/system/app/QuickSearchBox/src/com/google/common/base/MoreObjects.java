package com.google.common.base;
/* loaded from: classes.dex */
public final class MoreObjects {
    public static <T> T firstNonNull(T t, T t2) {
        return t != null ? t : (T) Preconditions.checkNotNull(t2);
    }

    public static ToStringHelper toStringHelper(Object obj) {
        return new ToStringHelper(simpleName(obj.getClass()));
    }

    static String simpleName(Class<?> cls) {
        String replaceAll = cls.getName().replaceAll("\\$[0-9]+", "\\$");
        int lastIndexOf = replaceAll.lastIndexOf(36);
        if (lastIndexOf == -1) {
            lastIndexOf = replaceAll.lastIndexOf(46);
        }
        return replaceAll.substring(lastIndexOf + 1);
    }

    /* loaded from: classes.dex */
    public static final class ToStringHelper {
        private final String className;
        private ValueHolder holderHead;
        private ValueHolder holderTail;
        private boolean omitNullValues;

        private ToStringHelper(String str) {
            this.holderHead = new ValueHolder();
            this.holderTail = this.holderHead;
            this.omitNullValues = false;
            this.className = (String) Preconditions.checkNotNull(str);
        }

        public ToStringHelper add(String str, Object obj) {
            return addHolder(str, obj);
        }

        public ToStringHelper add(String str, int i) {
            return addHolder(str, String.valueOf(i));
        }

        public ToStringHelper addValue(Object obj) {
            return addHolder(obj);
        }

        public String toString() {
            boolean z = this.omitNullValues;
            String str = "";
            StringBuilder sb = new StringBuilder(32);
            sb.append(this.className);
            sb.append('{');
            for (ValueHolder valueHolder = this.holderHead.next; valueHolder != null; valueHolder = valueHolder.next) {
                if (!z || valueHolder.value != null) {
                    sb.append(str);
                    str = ", ";
                    if (valueHolder.name != null) {
                        sb.append(valueHolder.name);
                        sb.append('=');
                    }
                    sb.append(valueHolder.value);
                }
            }
            sb.append('}');
            return sb.toString();
        }

        private ValueHolder addHolder() {
            ValueHolder valueHolder = new ValueHolder();
            this.holderTail.next = valueHolder;
            this.holderTail = valueHolder;
            return valueHolder;
        }

        private ToStringHelper addHolder(Object obj) {
            addHolder().value = obj;
            return this;
        }

        private ToStringHelper addHolder(String str, Object obj) {
            ValueHolder addHolder = addHolder();
            addHolder.value = obj;
            addHolder.name = (String) Preconditions.checkNotNull(str);
            return this;
        }

        /* JADX INFO: Access modifiers changed from: private */
        /* loaded from: classes.dex */
        public static final class ValueHolder {
            String name;
            ValueHolder next;
            Object value;

            private ValueHolder() {
            }
        }
    }
}
