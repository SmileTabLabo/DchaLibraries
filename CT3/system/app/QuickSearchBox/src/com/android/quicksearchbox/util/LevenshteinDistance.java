package com.android.quicksearchbox.util;
/* loaded from: a.zip:com/android/quicksearchbox/util/LevenshteinDistance.class */
public class LevenshteinDistance {
    private final int[][] mDistanceTable;
    private final int[][] mEditTypeTable;
    private final Token[] mSource;
    private final Token[] mTarget;

    /* loaded from: a.zip:com/android/quicksearchbox/util/LevenshteinDistance$EditOperation.class */
    public static final class EditOperation {
        private final int mPosition;
        private final int mType;

        public EditOperation(int i, int i2) {
            this.mType = i;
            this.mPosition = i2;
        }

        public int getPosition() {
            return this.mPosition;
        }

        public int getType() {
            return this.mType;
        }
    }

    /* loaded from: a.zip:com/android/quicksearchbox/util/LevenshteinDistance$Token.class */
    public static final class Token implements CharSequence {
        private final char[] mContainer;
        public final int mEnd;
        public final int mStart;

        public Token(char[] cArr, int i, int i2) {
            this.mContainer = cArr;
            this.mStart = i;
            this.mEnd = i2;
        }

        @Override // java.lang.CharSequence
        public char charAt(int i) {
            return this.mContainer[this.mStart + i];
        }

        @Override // java.lang.CharSequence
        public int length() {
            return this.mEnd - this.mStart;
        }

        public boolean prefixOf(Token token) {
            int length = length();
            if (length > token.length()) {
                return false;
            }
            int i = this.mStart;
            int i2 = token.mStart;
            char[] cArr = this.mContainer;
            char[] cArr2 = token.mContainer;
            for (int i3 = 0; i3 < length; i3++) {
                if (cArr[i + i3] != cArr2[i2 + i3]) {
                    return false;
                }
            }
            return true;
        }

        @Override // java.lang.CharSequence
        public String subSequence(int i, int i2) {
            return new String(this.mContainer, this.mStart + i, length());
        }

        @Override // java.lang.CharSequence
        public String toString() {
            return subSequence(0, length());
        }
    }

    public LevenshteinDistance(Token[] tokenArr, Token[] tokenArr2) {
        int length = tokenArr.length;
        int length2 = tokenArr2.length;
        int[][] iArr = new int[length + 1][length2 + 1];
        int[][] iArr2 = new int[length + 1][length2 + 1];
        iArr[0][0] = 3;
        iArr2[0][0] = 0;
        for (int i = 1; i <= length; i++) {
            iArr[i][0] = 0;
            iArr2[i][0] = i;
        }
        for (int i2 = 1; i2 <= length2; i2++) {
            iArr[0][i2] = 1;
            iArr2[0][i2] = i2;
        }
        this.mEditTypeTable = iArr;
        this.mDistanceTable = iArr2;
        this.mSource = tokenArr;
        this.mTarget = tokenArr2;
    }

    public int calculate() {
        Token[] tokenArr = this.mSource;
        Token[] tokenArr2 = this.mTarget;
        int length = tokenArr.length;
        int length2 = tokenArr2.length;
        int[][] iArr = this.mDistanceTable;
        int[][] iArr2 = this.mEditTypeTable;
        for (int i = 1; i <= length; i++) {
            Token token = tokenArr[i - 1];
            for (int i2 = 1; i2 <= length2; i2++) {
                int i3 = token.prefixOf(tokenArr2[i2 - 1]) ? 0 : 1;
                int i4 = iArr[i - 1][i2] + 1;
                int i5 = 0;
                int i6 = iArr[i][i2 - 1];
                int i7 = i4;
                if (i6 + 1 < i4) {
                    i7 = i6 + 1;
                    i5 = 1;
                }
                int i8 = iArr[i - 1][i2 - 1];
                int i9 = i7;
                if (i8 + i3 < i7) {
                    i9 = i8 + i3;
                    i5 = i3 == 0 ? 3 : 2;
                }
                iArr[i][i2] = i9;
                iArr2[i][i2] = i5;
            }
        }
        return iArr[length][length2];
    }

    public EditOperation[] getTargetOperations() {
        int length = this.mTarget.length;
        EditOperation[] editOperationArr = new EditOperation[length];
        int length2 = this.mSource.length;
        int[][] iArr = this.mEditTypeTable;
        while (length > 0) {
            int i = iArr[length2][length];
            switch (i) {
                case 0:
                    length2--;
                    break;
                case 1:
                    length--;
                    editOperationArr[length] = new EditOperation(i, length2);
                    break;
                case 2:
                case 3:
                    length--;
                    length2--;
                    editOperationArr[length] = new EditOperation(i, length2);
                    break;
            }
        }
        return editOperationArr;
    }
}
