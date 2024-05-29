package com.android.quicksearchbox;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.google.common.annotations.VisibleForTesting;
/* loaded from: a.zip:com/android/quicksearchbox/SuggestionUtils.class */
public class SuggestionUtils {
    private SuggestionUtils() {
    }

    public static Intent getSuggestionIntent(SuggestionCursor suggestionCursor, Bundle bundle) {
        String suggestionIntentAction = suggestionCursor.getSuggestionIntentAction();
        String suggestionIntentDataString = suggestionCursor.getSuggestionIntentDataString();
        String suggestionQuery = suggestionCursor.getSuggestionQuery();
        String userQuery = suggestionCursor.getUserQuery();
        String suggestionIntentExtraData = suggestionCursor.getSuggestionIntentExtraData();
        Intent intent = new Intent(suggestionIntentAction);
        intent.addFlags(268435456);
        intent.addFlags(67108864);
        if (suggestionIntentDataString != null) {
            intent.setData(Uri.parse(suggestionIntentDataString));
        }
        intent.putExtra("user_query", userQuery);
        if (suggestionQuery != null) {
            intent.putExtra("query", suggestionQuery);
        }
        if (suggestionIntentExtraData != null) {
            intent.putExtra("intent_extra_data_key", suggestionIntentExtraData);
        }
        if (bundle != null) {
            intent.putExtra("app_data", bundle);
        }
        intent.setComponent(suggestionCursor.getSuggestionIntentComponent());
        return intent;
    }

    @VisibleForTesting
    static String normalizeUrl(String str) {
        int length;
        if (str != null) {
            int indexOf = str.indexOf("://");
            if (indexOf == -1) {
                str = "http://" + str;
                length = "http".length() + "://".length();
            } else {
                length = indexOf + "://".length();
            }
            int length2 = str.length();
            int i = length2;
            if (str.indexOf(47, length) == length2 - 1) {
                i = length2 - 1;
            }
            return str.substring(0, i);
        }
        return str;
    }
}
