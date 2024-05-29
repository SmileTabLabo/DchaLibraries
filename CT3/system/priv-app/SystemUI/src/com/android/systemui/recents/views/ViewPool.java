package com.android.systemui.recents.views;

import android.content.Context;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
/* loaded from: a.zip:com/android/systemui/recents/views/ViewPool.class */
public class ViewPool<V, T> {
    Context mContext;
    LinkedList<V> mPool = new LinkedList<>();
    ViewPoolConsumer<V, T> mViewCreator;

    /* loaded from: a.zip:com/android/systemui/recents/views/ViewPool$ViewPoolConsumer.class */
    public interface ViewPoolConsumer<V, T> {
        V createView(Context context);

        boolean hasPreferredData(V v, T t);

        void onPickUpViewFromPool(V v, T t, boolean z);

        void onReturnViewToPool(V v);
    }

    public ViewPool(Context context, ViewPoolConsumer<V, T> viewPoolConsumer) {
        this.mContext = context;
        this.mViewCreator = viewPoolConsumer;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public List<V> getViews() {
        return this.mPool;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public V pickUpViewFromPool(T t, T t2) {
        V v;
        boolean z;
        V v2;
        if (this.mPool.isEmpty()) {
            v2 = this.mViewCreator.createView(this.mContext);
            z = true;
        } else {
            Iterator<V> it = this.mPool.iterator();
            while (true) {
                v = null;
                if (!it.hasNext()) {
                    break;
                }
                v = it.next();
                if (this.mViewCreator.hasPreferredData(v, t)) {
                    it.remove();
                    break;
                }
            }
            z = false;
            v2 = v;
            if (v == null) {
                v2 = this.mPool.pop();
                z = false;
            }
        }
        this.mViewCreator.onPickUpViewFromPool(v2, t2, z);
        return v2;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void returnViewToPool(V v) {
        this.mViewCreator.onReturnViewToPool(v);
        this.mPool.push(v);
    }
}
