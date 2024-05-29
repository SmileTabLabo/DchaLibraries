package com.android.systemui.recents.views;

import android.content.Context;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
/* loaded from: classes.dex */
public class ViewPool<V, T> {
    Context mContext;
    LinkedList<V> mPool = new LinkedList<>();
    ViewPoolConsumer<V, T> mViewCreator;

    /* loaded from: classes.dex */
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
    public void returnViewToPool(V v) {
        this.mViewCreator.onReturnViewToPool(v);
        this.mPool.push(v);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public V pickUpViewFromPool(T t, T t2) {
        V v;
        boolean z = false;
        if (this.mPool.isEmpty()) {
            v = this.mViewCreator.createView(this.mContext);
            z = true;
        } else {
            Iterator<V> it = this.mPool.iterator();
            while (true) {
                if (it.hasNext()) {
                    V next = it.next();
                    if (this.mViewCreator.hasPreferredData(next, t)) {
                        it.remove();
                        v = next;
                        break;
                    }
                } else {
                    v = null;
                    break;
                }
            }
            if (v == null) {
                v = this.mPool.pop();
            }
        }
        this.mViewCreator.onPickUpViewFromPool(v, t2, z);
        return v;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public List<V> getViews() {
        return this.mPool;
    }
}
