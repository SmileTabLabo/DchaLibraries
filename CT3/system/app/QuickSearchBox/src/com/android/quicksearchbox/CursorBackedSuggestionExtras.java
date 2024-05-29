package com.android.quicksearchbox;

import android.database.Cursor;
import android.util.Log;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
/* loaded from: a.zip:com/android/quicksearchbox/CursorBackedSuggestionExtras.class */
public class CursorBackedSuggestionExtras extends AbstractSuggestionExtras {
    private static final HashSet<String> DEFAULT_COLUMNS = new HashSet<>();
    private final Cursor mCursor;
    private final int mCursorPosition;
    private final List<String> mExtraColumns;

    static {
        DEFAULT_COLUMNS.addAll(Arrays.asList(SuggestionCursorBackedCursor.COLUMNS));
    }

    private CursorBackedSuggestionExtras(Cursor cursor, int i, List<String> list) {
        super(null);
        this.mCursor = cursor;
        this.mCursorPosition = i;
        this.mExtraColumns = list;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static CursorBackedSuggestionExtras createExtrasIfNecessary(Cursor cursor, int i) {
        List<String> extraColumns = getExtraColumns(cursor);
        if (extraColumns != null) {
            return new CursorBackedSuggestionExtras(cursor, i, extraColumns);
        }
        return null;
    }

    static String[] getCursorColumns(Cursor cursor) {
        try {
            return cursor.getColumnNames();
        } catch (RuntimeException e) {
            Log.e("QSB.CursorBackedSuggestionExtras", "getColumnNames() failed, ", e);
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static List<String> getExtraColumns(Cursor cursor) {
        String[] cursorColumns = getCursorColumns(cursor);
        if (cursorColumns == null) {
            return null;
        }
        ArrayList arrayList = null;
        int i = 0;
        int length = cursorColumns.length;
        while (i < length) {
            String str = cursorColumns[i];
            ArrayList arrayList2 = arrayList;
            if (!DEFAULT_COLUMNS.contains(str)) {
                arrayList2 = arrayList;
                if (arrayList == null) {
                    arrayList2 = new ArrayList();
                }
                arrayList2.add(str);
            }
            i++;
            arrayList = arrayList2;
        }
        return arrayList;
    }

    @Override // com.android.quicksearchbox.AbstractSuggestionExtras
    public String doGetExtra(String str) {
        try {
            this.mCursor.moveToPosition(this.mCursorPosition);
            int columnIndex = this.mCursor.getColumnIndex(str);
            if (columnIndex < 0) {
                return null;
            }
            return this.mCursor.getString(columnIndex);
        } catch (RuntimeException e) {
            Log.e("QSB.CursorBackedSuggestionExtras", "getExtra(" + str + ") failed, ", e);
            return null;
        }
    }
}
