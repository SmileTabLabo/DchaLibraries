package androidx.slice;

import android.content.Context;
import android.net.Uri;
import java.util.List;
/* loaded from: classes.dex */
class SliceManagerWrapper extends SliceManager {
    private final Context mContext;
    private final android.app.slice.SliceManager mManager;

    /* JADX INFO: Access modifiers changed from: package-private */
    public SliceManagerWrapper(Context context) {
        this(context, (android.app.slice.SliceManager) context.getSystemService(android.app.slice.SliceManager.class));
    }

    SliceManagerWrapper(Context context, android.app.slice.SliceManager manager) {
        this.mContext = context;
        this.mManager = manager;
    }

    @Override // androidx.slice.SliceManager
    public List<Uri> getPinnedSlices() {
        return this.mManager.getPinnedSlices();
    }
}
