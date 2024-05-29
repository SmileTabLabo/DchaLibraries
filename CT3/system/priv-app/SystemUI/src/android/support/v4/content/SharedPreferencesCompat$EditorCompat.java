package android.support.v4.content;

import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
/* loaded from: a.zip:android/support/v4/content/SharedPreferencesCompat$EditorCompat.class */
public final class SharedPreferencesCompat$EditorCompat {
    private static SharedPreferencesCompat$EditorCompat sInstance;
    private final Helper mHelper;

    /* loaded from: a.zip:android/support/v4/content/SharedPreferencesCompat$EditorCompat$EditorHelperApi9Impl.class */
    private static class EditorHelperApi9Impl implements Helper {
        private EditorHelperApi9Impl() {
        }

        /* synthetic */ EditorHelperApi9Impl(EditorHelperApi9Impl editorHelperApi9Impl) {
            this();
        }

        @Override // android.support.v4.content.SharedPreferencesCompat$EditorCompat.Helper
        public void apply(@NonNull SharedPreferences.Editor editor) {
            EditorCompatGingerbread.apply(editor);
        }
    }

    /* loaded from: a.zip:android/support/v4/content/SharedPreferencesCompat$EditorCompat$EditorHelperBaseImpl.class */
    private static class EditorHelperBaseImpl implements Helper {
        private EditorHelperBaseImpl() {
        }

        /* synthetic */ EditorHelperBaseImpl(EditorHelperBaseImpl editorHelperBaseImpl) {
            this();
        }

        @Override // android.support.v4.content.SharedPreferencesCompat$EditorCompat.Helper
        public void apply(@NonNull SharedPreferences.Editor editor) {
            editor.commit();
        }
    }

    /* loaded from: a.zip:android/support/v4/content/SharedPreferencesCompat$EditorCompat$Helper.class */
    private interface Helper {
        void apply(@NonNull SharedPreferences.Editor editor);
    }

    private SharedPreferencesCompat$EditorCompat() {
        if (Build.VERSION.SDK_INT >= 9) {
            this.mHelper = new EditorHelperApi9Impl(null);
        } else {
            this.mHelper = new EditorHelperBaseImpl(null);
        }
    }

    public static SharedPreferencesCompat$EditorCompat getInstance() {
        if (sInstance == null) {
            sInstance = new SharedPreferencesCompat$EditorCompat();
        }
        return sInstance;
    }

    public void apply(@NonNull SharedPreferences.Editor editor) {
        this.mHelper.apply(editor);
    }
}
