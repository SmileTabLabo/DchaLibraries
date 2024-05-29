package com.android.quicksearchbox;

import android.text.SpannableString;
import android.text.Spanned;
import com.android.quicksearchbox.util.LevenshteinDistance;
/* loaded from: classes.dex */
public class LevenshteinSuggestionFormatter extends SuggestionFormatter {
    public LevenshteinSuggestionFormatter(TextAppearanceFactory textAppearanceFactory) {
        super(textAppearanceFactory);
    }

    @Override // com.android.quicksearchbox.SuggestionFormatter
    public Spanned formatSuggestion(String str, String str2) {
        int i;
        LevenshteinDistance.Token[] tokenArr = tokenize(normalizeQuery(str));
        LevenshteinDistance.Token[] tokenArr2 = tokenize(str2);
        int[] findMatches = findMatches(tokenArr, tokenArr2);
        SpannableString spannableString = new SpannableString(str2);
        int length = findMatches.length;
        for (int i2 = 0; i2 < length; i2++) {
            LevenshteinDistance.Token token = tokenArr2[i2];
            int i3 = findMatches[i2];
            if (i3 >= 0) {
                i = tokenArr[i3].length();
            } else {
                i = 0;
            }
            applySuggestedTextStyle(spannableString, token.mStart + i, token.mEnd);
            applyQueryTextStyle(spannableString, token.mStart, token.mStart + i);
        }
        return spannableString;
    }

    private String normalizeQuery(String str) {
        return str.toLowerCase();
    }

    int[] findMatches(LevenshteinDistance.Token[] tokenArr, LevenshteinDistance.Token[] tokenArr2) {
        LevenshteinDistance levenshteinDistance = new LevenshteinDistance(tokenArr, tokenArr2);
        levenshteinDistance.calculate();
        int length = tokenArr2.length;
        int[] iArr = new int[length];
        LevenshteinDistance.EditOperation[] targetOperations = levenshteinDistance.getTargetOperations();
        for (int i = 0; i < length; i++) {
            if (targetOperations[i].getType() == 3) {
                iArr[i] = targetOperations[i].getPosition();
            } else {
                iArr[i] = -1;
            }
        }
        return iArr;
    }

    /* JADX WARN: Code restructure failed: missing block: B:20:0x0034, code lost:
        r1[r4] = new com.android.quicksearchbox.util.LevenshteinDistance.Token(r10, r3, r7);
        r4 = r4 + 1;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    LevenshteinDistance.Token[] tokenize(String str) {
        int length = str.length();
        char[] charArray = str.toCharArray();
        LevenshteinDistance.Token[] tokenArr = new LevenshteinDistance.Token[length];
        int i = 0;
        int i2 = 0;
        while (i < length) {
            while (i < length && (charArray[i] == ' ' || charArray[i] == '\t')) {
                i++;
            }
            int i3 = i;
            while (i3 < length && charArray[i3] != ' ' && charArray[i3] != '\t') {
                i3++;
            }
            i = i3;
        }
        LevenshteinDistance.Token[] tokenArr2 = new LevenshteinDistance.Token[i2];
        System.arraycopy(tokenArr, 0, tokenArr2, 0, i2);
        return tokenArr2;
    }
}
