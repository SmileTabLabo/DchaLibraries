package android.support.v4.provider;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
/* loaded from: classes.dex */
public abstract class DocumentFile {
    private final DocumentFile mParent;

    public abstract String getName();

    public abstract Uri getUri();

    public abstract long length();

    public abstract DocumentFile[] listFiles();

    /* JADX INFO: Access modifiers changed from: package-private */
    public DocumentFile(DocumentFile parent) {
        this.mParent = parent;
    }

    public static DocumentFile fromTreeUri(Context context, Uri treeUri) {
        if (Build.VERSION.SDK_INT >= 21) {
            return new TreeDocumentFile(null, context, DocumentsContract.buildDocumentUriUsingTree(treeUri, DocumentsContract.getTreeDocumentId(treeUri)));
        }
        return null;
    }

    public DocumentFile findFile(String displayName) {
        DocumentFile[] listFiles;
        for (DocumentFile doc : listFiles()) {
            if (displayName.equals(doc.getName())) {
                return doc;
            }
        }
        return null;
    }
}
