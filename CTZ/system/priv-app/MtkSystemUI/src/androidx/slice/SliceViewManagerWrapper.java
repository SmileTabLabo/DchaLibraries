package androidx.slice;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.slice.widget.SliceLiveData;
import java.util.Set;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class SliceViewManagerWrapper extends SliceViewManagerBase {
    private final android.app.slice.SliceManager mManager;
    private final Set<android.app.slice.SliceSpec> mSpecs;

    /* JADX INFO: Access modifiers changed from: package-private */
    public SliceViewManagerWrapper(Context context) {
        this(context, (android.app.slice.SliceManager) context.getSystemService(android.app.slice.SliceManager.class));
    }

    SliceViewManagerWrapper(Context context, android.app.slice.SliceManager manager) {
        super(context);
        this.mManager = manager;
        this.mSpecs = SliceConvert.unwrap(SliceLiveData.SUPPORTED_SPECS);
    }

    @Override // androidx.slice.SliceViewManager
    public void pinSlice(Uri uri) {
        this.mManager.pinSlice(uri, this.mSpecs);
    }

    @Override // androidx.slice.SliceViewManager
    public void unpinSlice(Uri uri) {
        this.mManager.unpinSlice(uri);
    }

    @Override // androidx.slice.SliceViewManager
    public Slice bindSlice(Uri uri) {
        return SliceConvert.wrap(this.mManager.bindSlice(uri, this.mSpecs));
    }

    @Override // androidx.slice.SliceViewManager
    public Slice bindSlice(Intent intent) {
        return SliceConvert.wrap(this.mManager.bindSlice(intent, this.mSpecs));
    }
}
