package com.android.settings.inputmethod;

import com.android.internal.app.LocalePicker;
/* loaded from: classes.dex */
public class UserDictionaryLocalePicker extends LocalePicker {
    public UserDictionaryLocalePicker(UserDictionaryAddWordFragment parent) {
        setLocaleSelectionListener(parent);
    }
}
