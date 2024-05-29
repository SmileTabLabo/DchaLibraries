package com.google.common.base;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;
import java.util.Arrays;
import javax.annotation.CheckReturnValue;
@Beta
@GwtCompatible(emulated = true)
/* loaded from: a.zip:com/google/common/base/CharMatcher.class */
public abstract class CharMatcher implements Predicate<Character> {
    public static final CharMatcher ANY;
    public static final CharMatcher DIGIT;
    public static final CharMatcher INVISIBLE;
    public static final CharMatcher JAVA_DIGIT;
    public static final CharMatcher JAVA_ISO_CONTROL;
    public static final CharMatcher JAVA_LETTER;
    public static final CharMatcher JAVA_LETTER_OR_DIGIT;
    public static final CharMatcher JAVA_LOWER_CASE;
    public static final CharMatcher JAVA_UPPER_CASE;
    private static final String NINES;
    public static final CharMatcher NONE;
    public static final CharMatcher SINGLE_WIDTH;
    public static final CharMatcher WHITESPACE;
    static final int WHITESPACE_SHIFT;
    final String description;
    public static final CharMatcher BREAKING_WHITESPACE = new CharMatcher() { // from class: com.google.common.base.CharMatcher.1
        @Override // com.google.common.base.CharMatcher
        public boolean matches(char c) {
            boolean z = true;
            switch (c) {
                case '\t':
                case '\n':
                case 11:
                case '\f':
                case '\r':
                case ' ':
                case 133:
                case 5760:
                case 8232:
                case 8233:
                case 8287:
                case 12288:
                    return true;
                case 8199:
                    return false;
                default:
                    if (c < 8192 || c > 8202) {
                        z = false;
                    }
                    return z;
            }
        }

        @Override // com.google.common.base.CharMatcher
        public String toString() {
            return "CharMatcher.BREAKING_WHITESPACE";
        }
    };
    public static final CharMatcher ASCII = inRange(0, 127, "CharMatcher.ASCII");

    /* loaded from: a.zip:com/google/common/base/CharMatcher$FastMatcher.class */
    static abstract class FastMatcher extends CharMatcher {
        FastMatcher() {
        }

        FastMatcher(String str) {
            super(str);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/google/common/base/CharMatcher$Or.class */
    public static class Or extends CharMatcher {
        final CharMatcher first;
        final CharMatcher second;

        Or(CharMatcher charMatcher, CharMatcher charMatcher2) {
            this(charMatcher, charMatcher2, "CharMatcher.or(" + charMatcher + ", " + charMatcher2 + ")");
        }

        Or(CharMatcher charMatcher, CharMatcher charMatcher2, String str) {
            super(str);
            this.first = (CharMatcher) Preconditions.checkNotNull(charMatcher);
            this.second = (CharMatcher) Preconditions.checkNotNull(charMatcher2);
        }

        @Override // com.google.common.base.CharMatcher
        public boolean matches(char c) {
            return !this.first.matches(c) ? this.second.matches(c) : true;
        }

        @Override // com.google.common.base.CharMatcher
        CharMatcher withToString(String str) {
            return new Or(this.first, this.second, str);
        }
    }

    /* loaded from: a.zip:com/google/common/base/CharMatcher$RangesMatcher.class */
    private static class RangesMatcher extends CharMatcher {
        private final char[] rangeEnds;
        private final char[] rangeStarts;

        RangesMatcher(String str, char[] cArr, char[] cArr2) {
            super(str);
            this.rangeStarts = cArr;
            this.rangeEnds = cArr2;
            Preconditions.checkArgument(cArr.length == cArr2.length);
            for (int i = 0; i < cArr.length; i++) {
                Preconditions.checkArgument(cArr[i] <= cArr2[i]);
                if (i + 1 < cArr.length) {
                    Preconditions.checkArgument(cArr2[i] < cArr[i + 1]);
                }
            }
        }

        @Override // com.google.common.base.CharMatcher
        public boolean matches(char c) {
            boolean z = true;
            int binarySearch = Arrays.binarySearch(this.rangeStarts, c);
            if (binarySearch >= 0) {
                return true;
            }
            int i = (binarySearch ^ (-1)) - 1;
            if (i < 0 || c > this.rangeEnds[i]) {
                z = false;
            }
            return z;
        }
    }

    static {
        StringBuilder sb = new StringBuilder("0٠۰߀०০੦૦୦௦౦೦൦๐໐༠၀႐០᠐᥆᧐᭐᮰᱀᱐꘠꣐꤀꩐０".length());
        for (int i = 0; i < "0٠۰߀०০੦૦୦௦౦೦൦๐໐༠၀႐០᠐᥆᧐᭐᮰᱀᱐꘠꣐꤀꩐０".length(); i++) {
            sb.append((char) ("0٠۰߀०০੦૦୦௦౦೦൦๐໐༠၀႐០᠐᥆᧐᭐᮰᱀᱐꘠꣐꤀꩐０".charAt(i) + '\t'));
        }
        NINES = sb.toString();
        DIGIT = new RangesMatcher("CharMatcher.DIGIT", "0٠۰߀०০੦૦୦௦౦೦൦๐໐༠၀႐០᠐᥆᧐᭐᮰᱀᱐꘠꣐꤀꩐０".toCharArray(), NINES.toCharArray());
        JAVA_DIGIT = new CharMatcher("CharMatcher.JAVA_DIGIT") { // from class: com.google.common.base.CharMatcher.2
            @Override // com.google.common.base.CharMatcher
            public boolean matches(char c) {
                return Character.isDigit(c);
            }
        };
        JAVA_LETTER = new CharMatcher("CharMatcher.JAVA_LETTER") { // from class: com.google.common.base.CharMatcher.3
            @Override // com.google.common.base.CharMatcher
            public boolean matches(char c) {
                return Character.isLetter(c);
            }
        };
        JAVA_LETTER_OR_DIGIT = new CharMatcher("CharMatcher.JAVA_LETTER_OR_DIGIT") { // from class: com.google.common.base.CharMatcher.4
            @Override // com.google.common.base.CharMatcher
            public boolean matches(char c) {
                return Character.isLetterOrDigit(c);
            }
        };
        JAVA_UPPER_CASE = new CharMatcher("CharMatcher.JAVA_UPPER_CASE") { // from class: com.google.common.base.CharMatcher.5
            @Override // com.google.common.base.CharMatcher
            public boolean matches(char c) {
                return Character.isUpperCase(c);
            }
        };
        JAVA_LOWER_CASE = new CharMatcher("CharMatcher.JAVA_LOWER_CASE") { // from class: com.google.common.base.CharMatcher.6
            @Override // com.google.common.base.CharMatcher
            public boolean matches(char c) {
                return Character.isLowerCase(c);
            }
        };
        JAVA_ISO_CONTROL = inRange((char) 0, (char) 31).or(inRange((char) 127, (char) 159)).withToString("CharMatcher.JAVA_ISO_CONTROL");
        INVISIBLE = new RangesMatcher("CharMatcher.INVISIBLE", "��\u007f\u00ad\u0600\u061c\u06dd\u070f\u1680\u180e\u2000\u2028\u205f\u2066\u2067\u2068\u2069\u206a\u3000�\ufeff\ufff9\ufffa".toCharArray(), "  \u00ad\u0604\u061c\u06dd\u070f\u1680\u180e\u200f \u2064\u2066\u2067\u2068\u2069\u206f\u3000\uf8ff\ufeff\ufff9\ufffb".toCharArray());
        SINGLE_WIDTH = new RangesMatcher("CharMatcher.SINGLE_WIDTH", "��־א׳\u0600ݐ\u0e00Ḁ℀ﭐﹰ｡".toCharArray(), "ӹ־ת״ۿݿ\u0e7f₯℺\ufdff\ufeffￜ".toCharArray());
        ANY = new FastMatcher("CharMatcher.ANY") { // from class: com.google.common.base.CharMatcher.7
            @Override // com.google.common.base.CharMatcher
            public String collapseFrom(CharSequence charSequence, char c) {
                return charSequence.length() == 0 ? "" : String.valueOf(c);
            }

            @Override // com.google.common.base.CharMatcher
            public boolean matches(char c) {
                return true;
            }

            @Override // com.google.common.base.CharMatcher
            public CharMatcher or(CharMatcher charMatcher) {
                Preconditions.checkNotNull(charMatcher);
                return this;
            }
        };
        NONE = new FastMatcher("CharMatcher.NONE") { // from class: com.google.common.base.CharMatcher.8
            @Override // com.google.common.base.CharMatcher
            public String collapseFrom(CharSequence charSequence, char c) {
                return charSequence.toString();
            }

            @Override // com.google.common.base.CharMatcher
            public boolean matches(char c) {
                return false;
            }

            @Override // com.google.common.base.CharMatcher
            public CharMatcher or(CharMatcher charMatcher) {
                return (CharMatcher) Preconditions.checkNotNull(charMatcher);
            }

            @Override // com.google.common.base.CharMatcher
            public String trimLeadingFrom(CharSequence charSequence) {
                return charSequence.toString();
            }
        };
        WHITESPACE_SHIFT = Integer.numberOfLeadingZeros("\u2002\u3000\r\u0085\u200a\u2005\u2000\u3000\u2029\u000b\u3000\u2008\u2003\u205f\u3000\u1680\t \u2006\u2001  \f\u2009\u3000\u2004\u3000\u3000\u2028\n \u3000".length() - 1);
        WHITESPACE = new FastMatcher("WHITESPACE") { // from class: com.google.common.base.CharMatcher.9
            @Override // com.google.common.base.CharMatcher
            public boolean matches(char c) {
                return "\u2002\u3000\r\u0085\u200a\u2005\u2000\u3000\u2029\u000b\u3000\u2008\u2003\u205f\u3000\u1680\t \u2006\u2001  \f\u2009\u3000\u2004\u3000\u3000\u2028\n \u3000".charAt((48906 * c) >>> WHITESPACE_SHIFT) == c;
            }
        };
    }

    protected CharMatcher() {
        this.description = super.toString();
    }

    CharMatcher(String str) {
        this.description = str;
    }

    private String finishCollapseFrom(CharSequence charSequence, int i, int i2, char c, StringBuilder sb, boolean z) {
        while (true) {
            boolean z2 = z;
            if (i >= i2) {
                return sb.toString();
            }
            char charAt = charSequence.charAt(i);
            if (matches(charAt)) {
                z = z2;
                if (!z2) {
                    sb.append(c);
                    z = true;
                }
            } else {
                sb.append(charAt);
                z = false;
            }
            i++;
        }
    }

    public static CharMatcher inRange(char c, char c2) {
        Preconditions.checkArgument(c2 >= c);
        return inRange(c, c2, "CharMatcher.inRange('" + showCharacter(c) + "', '" + showCharacter(c2) + "')");
    }

    static CharMatcher inRange(char c, char c2, String str) {
        return new FastMatcher(str, c, c2) { // from class: com.google.common.base.CharMatcher.14
            final char val$endInclusive;
            final char val$startInclusive;

            {
                this.val$startInclusive = c;
                this.val$endInclusive = c2;
            }

            @Override // com.google.common.base.CharMatcher
            public boolean matches(char c3) {
                boolean z = false;
                if (this.val$startInclusive <= c3) {
                    z = false;
                    if (c3 <= this.val$endInclusive) {
                        z = true;
                    }
                }
                return z;
            }
        };
    }

    private static String showCharacter(char c) {
        char[] cArr = new char[6];
        cArr[0] = '\\';
        cArr[1] = 'u';
        cArr[2] = 0;
        cArr[3] = 0;
        cArr[4] = 0;
        cArr[5] = 0;
        for (int i = 0; i < 4; i++) {
            cArr[5 - i] = "0123456789ABCDEF".charAt(c & 15);
            c = (char) (c >> 4);
        }
        return String.copyValueOf(cArr);
    }

    @Override // com.google.common.base.Predicate
    @Deprecated
    public boolean apply(Character ch) {
        return matches(ch.charValue());
    }

    /* JADX WARN: Code restructure failed: missing block: B:14:0x006a, code lost:
        return finishCollapseFrom(r11, r14 + 1, r0, r12, new java.lang.StringBuilder(r0).append(r11.subSequence(0, r14)).append(r12), true);
     */
    @CheckReturnValue
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public String collapseFrom(CharSequence charSequence, char c) {
        int length = charSequence.length();
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 >= length) {
                return charSequence.toString();
            }
            char charAt = charSequence.charAt(i2);
            int i3 = i2;
            if (matches(charAt)) {
                if (charAt != c || (i2 != length - 1 && matches(charSequence.charAt(i2 + 1)))) {
                    break;
                }
                i3 = i2 + 1;
            }
            i = i3 + 1;
        }
    }

    public abstract boolean matches(char c);

    public CharMatcher or(CharMatcher charMatcher) {
        return new Or(this, (CharMatcher) Preconditions.checkNotNull(charMatcher));
    }

    public String toString() {
        return this.description;
    }

    @CheckReturnValue
    public String trimAndCollapseFrom(CharSequence charSequence, char c) {
        int length = charSequence.length();
        int i = 0;
        while (i < length && matches(charSequence.charAt(i))) {
            i++;
        }
        int i2 = length - 1;
        while (i2 > i && matches(charSequence.charAt(i2))) {
            i2--;
        }
        return (i == 0 && i2 == length - 1) ? collapseFrom(charSequence, c) : finishCollapseFrom(charSequence, i, i2 + 1, c, new StringBuilder((i2 + 1) - i), false);
    }

    @CheckReturnValue
    public String trimLeadingFrom(CharSequence charSequence) {
        int length = charSequence.length();
        for (int i = 0; i < length; i++) {
            if (!matches(charSequence.charAt(i))) {
                return charSequence.subSequence(i, length).toString();
            }
        }
        return "";
    }

    CharMatcher withToString(String str) {
        throw new UnsupportedOperationException();
    }
}
