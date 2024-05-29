package jp.co.benesse.dcha.databox.plugin;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import java.io.FileNotFoundException;
import java.io.IOException;
import jp.co.benesse.dcha.util.Logger;
/* loaded from: classes.dex */
public class PackageSignatureProvider extends ContentProvider {
    private static final String TAG = PackageSignatureProvider.class.getSimpleName();

    @Override // android.content.ContentProvider
    public int delete(Uri arg0, String arg1, String[] arg2) {
        return 0;
    }

    @Override // android.content.ContentProvider
    public String getType(Uri arg0) {
        return null;
    }

    @Override // android.content.ContentProvider
    public AssetFileDescriptor openAssetFile(Uri uri, String mode) throws FileNotFoundException {
        AssetManager manager = getContext().getResources().getAssets();
        try {
            String[] fileList = manager.list("certs");
            String dstFileName = uri.getLastPathSegment();
            Logger.d(TAG, "openAssetFile dstFileName:" + dstFileName);
            for (String assetFile : fileList) {
                Logger.d(TAG, "openAssetFile assetFile:" + assetFile);
                if (assetFile.startsWith(dstFileName)) {
                    AssetFileDescriptor fd = manager.openFd("certs/" + assetFile);
                    return fd;
                }
            }
            return null;
        } catch (IOException e) {
            Logger.e(TAG, "openAssetFile", e);
            return null;
        }
    }

    @Override // android.content.ContentProvider
    public Uri insert(Uri arg0, ContentValues arg1) {
        return null;
    }

    @Override // android.content.ContentProvider
    public boolean onCreate() {
        return false;
    }

    @Override // android.content.ContentProvider
    public Cursor query(Uri arg0, String[] arg1, String arg2, String[] arg3, String arg4) {
        return null;
    }

    @Override // android.content.ContentProvider
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
