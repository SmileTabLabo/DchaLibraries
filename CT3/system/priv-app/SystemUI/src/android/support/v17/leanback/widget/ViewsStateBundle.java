package android.support.v17.leanback.widget;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.util.LruCache;
import android.util.SparseArray;
import android.view.View;
import java.util.Map;
/* loaded from: a.zip:android/support/v17/leanback/widget/ViewsStateBundle.class */
class ViewsStateBundle {
    private LruCache<String, SparseArray<Parcelable>> mChildStates;
    private int mSavePolicy = 0;
    private int mLimitNumber = 100;

    static String getSaveStatesKey(int i) {
        return Integer.toString(i);
    }

    public void clear() {
        if (this.mChildStates != null) {
            this.mChildStates.evictAll();
        }
    }

    public final void loadFromBundle(Bundle bundle) {
        if (this.mChildStates == null || bundle == null) {
            return;
        }
        this.mChildStates.evictAll();
        for (String str : bundle.keySet()) {
            this.mChildStates.put(str, bundle.getSparseParcelableArray(str));
        }
    }

    public final void loadView(View view, int i) {
        if (this.mChildStates != null) {
            SparseArray<Parcelable> remove = this.mChildStates.remove(getSaveStatesKey(i));
            if (remove != null) {
                view.restoreHierarchyState(remove);
            }
        }
    }

    public void remove(int i) {
        if (this.mChildStates == null || this.mChildStates.size() == 0) {
            return;
        }
        this.mChildStates.remove(getSaveStatesKey(i));
    }

    public final Bundle saveAsBundle() {
        if (this.mChildStates == null || this.mChildStates.size() == 0) {
            return null;
        }
        Map<String, SparseArray<Parcelable>> snapshot = this.mChildStates.snapshot();
        Bundle bundle = new Bundle();
        for (Map.Entry<String, SparseArray<Parcelable>> entry : snapshot.entrySet()) {
            bundle.putSparseParcelableArray(entry.getKey(), entry.getValue());
        }
        return bundle;
    }

    public final void saveOffscreenView(View view, int i) {
        switch (this.mSavePolicy) {
            case 1:
                remove(i);
                return;
            case 2:
            case 3:
                saveViewUnchecked(view, i);
                return;
            default:
                return;
        }
    }

    public final Bundle saveOnScreenView(Bundle bundle, View view, int i) {
        Bundle bundle2 = bundle;
        if (this.mSavePolicy != 0) {
            String saveStatesKey = getSaveStatesKey(i);
            SparseArray<Parcelable> sparseArray = new SparseArray<>();
            view.saveHierarchyState(sparseArray);
            Bundle bundle3 = bundle;
            if (bundle == null) {
                bundle3 = new Bundle();
            }
            bundle3.putSparseParcelableArray(saveStatesKey, sparseArray);
            bundle2 = bundle3;
        }
        return bundle2;
    }

    protected final void saveViewUnchecked(View view, int i) {
        if (this.mChildStates != null) {
            String saveStatesKey = getSaveStatesKey(i);
            SparseArray<Parcelable> sparseArray = new SparseArray<>();
            view.saveHierarchyState(sparseArray);
            this.mChildStates.put(saveStatesKey, sparseArray);
        }
    }
}
