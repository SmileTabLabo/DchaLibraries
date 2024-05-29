package android.support.v4.content;

import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
/* loaded from: classes.dex */
public final class SharedPreferencesCompat$EditorCompat {
    private static SharedPreferencesCompat$EditorCompat sInstance;
    private final Helper mHelper;

    /* loaded from: classes.dex */
    private interface Helper {
        void apply(@NonNull SharedPreferences.Editor editor);
    }

    /* loaded from: classes.dex */
    private static class EditorHelperBaseImpl implements Helper {
        /* synthetic */ EditorHelperBaseImpl(EditorHelperBaseImpl editorHelperBaseImpl) {
            this();
        }

        private EditorHelperBaseImpl() {
        }

        @Override // android.support.v4.content.SharedPreferencesCompat$EditorCompat.Helper
        public void apply(@NonNull SharedPreferences.Editor editor) {
            editor.commit();
        }
    }

    /* loaded from: classes.dex */
    private static class EditorHelperApi9Impl implements Helper {
        /* synthetic */ EditorHelperApi9Impl(EditorHelperApi9Impl editorHelperApi9Impl) {
            this();
        }

        private EditorHelperApi9Impl() {
        }

        @Override // android.support.v4.content.SharedPreferencesCompat$EditorCompat.Helper
        public void apply(@NonNull SharedPreferences.Editor editor) {
            EditorCompatGingerbread.apply(editor);
        }
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
