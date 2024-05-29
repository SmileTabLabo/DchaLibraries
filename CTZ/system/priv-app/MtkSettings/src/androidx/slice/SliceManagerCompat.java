package androidx.slice;

import android.content.Context;
import android.net.Uri;
import androidx.slice.compat.SliceProviderCompat;
import java.util.List;
/* loaded from: classes.dex */
class SliceManagerCompat extends SliceManager {
    private final Context mContext;

    /* JADX INFO: Access modifiers changed from: package-private */
    public SliceManagerCompat(Context context) {
        this.mContext = context;
    }

    @Override // androidx.slice.SliceManager
    public List<Uri> getPinnedSlices() {
        return SliceProviderCompat.getPinnedSlices(this.mContext);
    }
}
