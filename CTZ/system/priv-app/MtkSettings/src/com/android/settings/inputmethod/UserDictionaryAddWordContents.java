package com.android.settings.inputmethod;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.UserDictionary;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import com.android.settings.R;
import com.android.settings.Utils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeSet;
/* loaded from: classes.dex */
public class UserDictionaryAddWordContents {
    private static final String[] HAS_WORD_PROJECTION = {"word"};
    private String mLocale;
    private final int mMode;
    private final String mOldShortcut;
    private final String mOldWord;
    private String mSavedShortcut;
    private String mSavedWord;
    private final EditText mShortcutEditText;
    private final EditText mWordEditText;

    /* JADX INFO: Access modifiers changed from: package-private */
    public UserDictionaryAddWordContents(View view, Bundle bundle) {
        this.mWordEditText = (EditText) view.findViewById(R.id.user_dictionary_add_word_text);
        this.mShortcutEditText = (EditText) view.findViewById(R.id.user_dictionary_add_shortcut);
        String string = bundle.getString("word");
        if (string != null) {
            this.mWordEditText.setText(string);
            this.mWordEditText.setSelection(this.mWordEditText.getText().length());
        }
        String string2 = bundle.getString("shortcut");
        if (string2 != null && this.mShortcutEditText != null) {
            this.mShortcutEditText.setText(string2);
        }
        this.mMode = bundle.getInt("mode");
        this.mOldWord = bundle.getString("word");
        this.mOldShortcut = bundle.getString("shortcut");
        updateLocale(bundle.getString("locale"));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public UserDictionaryAddWordContents(View view, UserDictionaryAddWordContents userDictionaryAddWordContents) {
        this.mWordEditText = (EditText) view.findViewById(R.id.user_dictionary_add_word_text);
        this.mShortcutEditText = (EditText) view.findViewById(R.id.user_dictionary_add_shortcut);
        this.mMode = 0;
        this.mOldWord = userDictionaryAddWordContents.mSavedWord;
        this.mOldShortcut = userDictionaryAddWordContents.mSavedShortcut;
        updateLocale(userDictionaryAddWordContents.getCurrentUserDictionaryLocale());
    }

    void updateLocale(String str) {
        if (str == null) {
            str = Locale.getDefault().toString();
        }
        this.mLocale = str;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void saveStateIntoBundle(Bundle bundle) {
        bundle.putString("word", this.mWordEditText.getText().toString());
        bundle.putString("originalWord", this.mOldWord);
        if (this.mShortcutEditText != null) {
            bundle.putString("shortcut", this.mShortcutEditText.getText().toString());
        }
        if (this.mOldShortcut != null) {
            bundle.putString("originalShortcut", this.mOldShortcut);
        }
        bundle.putString("locale", this.mLocale);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void delete(Context context) {
        if (this.mMode == 0 && !TextUtils.isEmpty(this.mOldWord)) {
            UserDictionarySettings.deleteWord(this.mOldWord, this.mOldShortcut, context.getContentResolver());
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: Code restructure failed: missing block: B:13:0x003c, code lost:
        if (android.text.TextUtils.isEmpty(r1) != false) goto L10;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public int apply(Context context, Bundle bundle) {
        String obj;
        if (bundle != null) {
            saveStateIntoBundle(bundle);
        }
        ContentResolver contentResolver = context.getContentResolver();
        if (this.mMode == 0 && !TextUtils.isEmpty(this.mOldWord)) {
            UserDictionarySettings.deleteWord(this.mOldWord, this.mOldShortcut, contentResolver);
        }
        String obj2 = this.mWordEditText.getText().toString();
        if (this.mShortcutEditText != null) {
            obj = this.mShortcutEditText.getText().toString();
        }
        obj = null;
        if (TextUtils.isEmpty(obj2)) {
            return 1;
        }
        this.mSavedWord = obj2;
        this.mSavedShortcut = obj;
        if (TextUtils.isEmpty(obj) && hasWord(obj2, context)) {
            return 2;
        }
        UserDictionarySettings.deleteWord(obj2, null, contentResolver);
        if (!TextUtils.isEmpty(obj)) {
            UserDictionarySettings.deleteWord(obj2, obj, contentResolver);
        }
        UserDictionary.Words.addWord(context, obj2.toString(), 250, obj, TextUtils.isEmpty(this.mLocale) ? null : Utils.createLocaleFromString(this.mLocale));
        return 0;
    }

    private boolean hasWord(String str, Context context) {
        Cursor query;
        if ("".equals(this.mLocale)) {
            query = context.getContentResolver().query(UserDictionary.Words.CONTENT_URI, HAS_WORD_PROJECTION, "word=? AND locale is null", new String[]{str}, null);
        } else {
            query = context.getContentResolver().query(UserDictionary.Words.CONTENT_URI, HAS_WORD_PROJECTION, "word=? AND locale=?", new String[]{str, this.mLocale}, null);
        }
        if (query == null) {
            return false;
        }
        try {
            boolean z = query.getCount() > 0;
            if (query != null) {
                query.close();
            }
            return z;
        } finally {
            if (query != null) {
                query.close();
            }
        }
    }

    /* loaded from: classes.dex */
    public static class LocaleRenderer {
        private final String mDescription;
        private final String mLocaleString;

        public LocaleRenderer(Context context, String str) {
            this.mLocaleString = str;
            if (str == null) {
                this.mDescription = context.getString(R.string.user_dict_settings_more_languages);
            } else if ("".equals(str)) {
                this.mDescription = context.getString(R.string.user_dict_settings_all_languages);
            } else {
                this.mDescription = Utils.createLocaleFromString(str).getDisplayName();
            }
        }

        public String toString() {
            return this.mDescription;
        }
    }

    private static void addLocaleDisplayNameToList(Context context, ArrayList<LocaleRenderer> arrayList, String str) {
        if (str != null) {
            arrayList.add(new LocaleRenderer(context, str));
        }
    }

    public ArrayList<LocaleRenderer> getLocalesList(Activity activity) {
        TreeSet<String> userDictionaryLocalesSet = UserDictionaryList.getUserDictionaryLocalesSet(activity);
        userDictionaryLocalesSet.remove(this.mLocale);
        String locale = Locale.getDefault().toString();
        userDictionaryLocalesSet.remove(locale);
        userDictionaryLocalesSet.remove("");
        ArrayList<LocaleRenderer> arrayList = new ArrayList<>();
        addLocaleDisplayNameToList(activity, arrayList, this.mLocale);
        if (!locale.equals(this.mLocale)) {
            addLocaleDisplayNameToList(activity, arrayList, locale);
        }
        Iterator<String> it = userDictionaryLocalesSet.iterator();
        while (it.hasNext()) {
            addLocaleDisplayNameToList(activity, arrayList, it.next());
        }
        if (!"".equals(this.mLocale)) {
            addLocaleDisplayNameToList(activity, arrayList, "");
        }
        arrayList.add(new LocaleRenderer(activity, null));
        return arrayList;
    }

    public String getCurrentUserDictionaryLocale() {
        return this.mLocale;
    }
}
