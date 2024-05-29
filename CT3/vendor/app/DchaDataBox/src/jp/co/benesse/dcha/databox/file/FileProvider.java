package jp.co.benesse.dcha.databox.file;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import java.io.IOException;
import jp.co.benesse.dcha.util.Logger;
/* loaded from: classes.dex */
public final class FileProvider extends ContentProvider {
    private static final String TAG = FileProvider.class.getSimpleName();
    public static final String AUTHORITY = FileProvider.class.getName();
    private static final UriMatcher uriMatcher = new UriMatcher(-1);

    static {
        uriMatcher.addURI(AUTHORITY, ContractFile.TOP_DIR.pathName, ContractFile.TOP_DIR.codeForMany);
    }

    @Override // android.content.ContentProvider
    public final boolean onCreate() {
        return true;
    }

    @Override // android.content.ContentProvider
    public final String getType(Uri uri) {
        int code = uriMatcher.match(uri);
        if (code == ContractFile.TOP_DIR.codeForMany) {
            String path = getContext().getFilesDir().getAbsolutePath();
            try {
                String path2 = getContext().getFilesDir().getCanonicalPath();
                return path2;
            } catch (IOException e) {
                Logger.e(TAG, e);
                return path;
            }
        }
        throw new IllegalArgumentException("unknown uri : " + uri);
    }

    @Override // android.content.ContentProvider
    public final Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override // android.content.ContentProvider
    public final Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override // android.content.ContentProvider
    public final int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override // android.content.ContentProvider
    public final int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }
}
