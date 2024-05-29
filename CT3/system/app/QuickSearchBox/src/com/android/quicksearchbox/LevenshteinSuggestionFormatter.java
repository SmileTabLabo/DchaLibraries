package com.android.quicksearchbox;

import android.text.SpannableString;
import android.text.Spanned;
import com.android.quicksearchbox.util.LevenshteinDistance;
import com.google.common.annotations.VisibleForTesting;
/* loaded from: a.zip:com/android/quicksearchbox/LevenshteinSuggestionFormatter.class */
public class LevenshteinSuggestionFormatter extends SuggestionFormatter {
    public LevenshteinSuggestionFormatter(TextAppearanceFactory textAppearanceFactory) {
        super(textAppearanceFactory);
    }

    private String normalizeQuery(String str) {
        return str.toLowerCase();
    }

    @VisibleForTesting
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

    @Override // com.android.quicksearchbox.SuggestionFormatter
    public Spanned formatSuggestion(String str, String str2) {
        LevenshteinDistance.Token[] tokenArr = tokenize(normalizeQuery(str));
        LevenshteinDistance.Token[] tokenArr2 = tokenize(str2);
        int[] findMatches = findMatches(tokenArr, tokenArr2);
        SpannableString spannableString = new SpannableString(str2);
        int length = findMatches.length;
        for (int i = 0; i < length; i++) {
            LevenshteinDistance.Token token = tokenArr2[i];
            int i2 = 0;
            int i3 = findMatches[i];
            if (i3 >= 0) {
                i2 = tokenArr[i3].length();
            }
            applySuggestedTextStyle(spannableString, token.mStart + i2, token.mEnd);
            applyQueryTextStyle(spannableString, token.mStart, token.mStart + i2);
        }
        return spannableString;
    }

    @VisibleForTesting
    LevenshteinDistance.Token[] tokenize(String str) {
        int i = 0;
        int length = str.length();
        char[] charArray = str.toCharArray();
        LevenshteinDistance.Token[] tokenArr = new LevenshteinDistance.Token[length];
        int i2 = 0;
        while (i < length) {
            while (i < length && (charArray[i] == ' ' || charArray[i] == '\t')) {
                i++;
            }
            int i3 = i;
            while (i3 < length && charArray[i3] != ' ' && charArray[i3] != '\t') {
                i3++;
            }
            if (i != i3) {
                tokenArr[i2] = new LevenshteinDistance.Token(charArray, i, i3);
                i2++;
            }
            i = i3;
        }
        LevenshteinDistance.Token[] tokenArr2 = new LevenshteinDistance.Token[i2];
        System.arraycopy(tokenArr, 0, tokenArr2, 0, i2);
        return tokenArr2;
    }
}
