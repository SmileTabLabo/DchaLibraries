package com.android.quicksearchbox.google;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import com.android.quicksearchbox.AbstractInternalSource;
import com.android.quicksearchbox.CursorBackedSourceResult;
import com.android.quicksearchbox.SourceResult;
import com.android.quicksearchbox.SuggestionCursor;
import com.android.quicksearchbox.util.NamedTaskExecutor;
/* loaded from: a.zip:com/android/quicksearchbox/google/AbstractGoogleSource.class */
public abstract class AbstractGoogleSource extends AbstractInternalSource implements GoogleSource {
    public AbstractGoogleSource(Context context, Handler handler, NamedTaskExecutor namedTaskExecutor) {
        super(context, handler, namedTaskExecutor);
    }

    private SourceResult emptyIfNull(SourceResult sourceResult, String str) {
        CursorBackedSourceResult cursorBackedSourceResult = sourceResult;
        if (sourceResult == null) {
            cursorBackedSourceResult = new CursorBackedSourceResult(this, str);
        }
        return cursorBackedSourceResult;
    }

    @Override // com.android.quicksearchbox.Source
    public Intent createVoiceSearchIntent(Bundle bundle) {
        return createVoiceWebSearchIntent(bundle);
    }

    @Override // com.android.quicksearchbox.Source
    public String getDefaultIntentAction() {
        return "android.intent.action.WEB_SEARCH";
    }

    @Override // com.android.quicksearchbox.Source
    public abstract ComponentName getIntentComponent();

    @Override // com.android.quicksearchbox.SuggestionCursorProvider
    public String getName() {
        return "com.android.quicksearchbox/.google.GoogleSearch";
    }

    @Override // com.android.quicksearchbox.AbstractInternalSource
    protected int getSourceIconResource() {
        return 2130903040;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.android.quicksearchbox.SuggestionCursorProvider
    public SourceResult getSuggestions(String str, int i) {
        return emptyIfNull(queryInternal(str), str);
    }

    @Override // com.android.quicksearchbox.google.GoogleSource
    public abstract SourceResult queryExternal(String str);

    public abstract SourceResult queryInternal(String str);

    @Override // com.android.quicksearchbox.google.GoogleSource
    public abstract SuggestionCursor refreshShortcut(String str, String str2);
}
