package com.android.browser.util;

import android.app.LoaderManager;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import java.lang.ref.WeakReference;
/* loaded from: b.zip:com/android/browser/util/ThreadedCursorAdapter.class */
public abstract class ThreadedCursorAdapter<T> extends BaseAdapter {
    private Context mContext;
    private CursorAdapter mCursorAdapter;
    private Object mCursorLock = new Object();
    private long mGeneration;
    private Handler mHandler;
    private boolean mHasCursor;
    private Handler mLoadHandler;
    private T mLoadingObject;
    private int mSize;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: b.zip:com/android/browser/util/ThreadedCursorAdapter$LoadContainer.class */
    public class LoadContainer {
        T bind_object;
        long generation;
        boolean loaded;
        Adapter owner;
        int position;
        final ThreadedCursorAdapter this$0;
        WeakReference<View> view;

        private LoadContainer(ThreadedCursorAdapter threadedCursorAdapter) {
            this.this$0 = threadedCursorAdapter;
        }

        /* synthetic */ LoadContainer(ThreadedCursorAdapter threadedCursorAdapter, LoadContainer loadContainer) {
            this(threadedCursorAdapter);
        }
    }

    public ThreadedCursorAdapter(Context context, Cursor cursor) {
        this.mContext = context;
        this.mHasCursor = cursor != null;
        this.mCursorAdapter = new CursorAdapter(this, context, cursor, 0) { // from class: com.android.browser.util.ThreadedCursorAdapter.1
            final ThreadedCursorAdapter this$0;

            {
                this.this$0 = this;
            }

            @Override // android.widget.CursorAdapter
            public void bindView(View view, Context context2, Cursor cursor2) {
                throw new IllegalStateException("not supported");
            }

            @Override // android.widget.CursorAdapter
            public View newView(Context context2, Cursor cursor2, ViewGroup viewGroup) {
                throw new IllegalStateException("not supported");
            }

            @Override // android.widget.BaseAdapter
            public void notifyDataSetChanged() {
                super.notifyDataSetChanged();
                this.this$0.mSize = getCount();
                this.this$0.mGeneration++;
                this.this$0.notifyDataSetChanged();
            }

            @Override // android.widget.BaseAdapter
            public void notifyDataSetInvalidated() {
                super.notifyDataSetInvalidated();
                this.this$0.mSize = getCount();
                this.this$0.mGeneration++;
                this.this$0.notifyDataSetInvalidated();
            }
        };
        this.mSize = this.mCursorAdapter.getCount();
        HandlerThread handlerThread = new HandlerThread("threaded_adapter_" + this, 10);
        handlerThread.start();
        this.mLoadHandler = new Handler(this, handlerThread.getLooper()) { // from class: com.android.browser.util.ThreadedCursorAdapter.2
            final ThreadedCursorAdapter this$0;

            {
                this.this$0 = this;
            }

            @Override // android.os.Handler
            public void handleMessage(Message message) {
                this.this$0.loadRowObject(message.what, (LoadContainer) message.obj);
            }
        };
        this.mHandler = new Handler(this) { // from class: com.android.browser.util.ThreadedCursorAdapter.3
            final ThreadedCursorAdapter this$0;

            {
                this.this$0 = this;
            }

            @Override // android.os.Handler
            public void handleMessage(Message message) {
                View view;
                LoadContainer loadContainer = (LoadContainer) message.obj;
                if (loadContainer != null && (view = loadContainer.view.get()) != null && loadContainer.owner == this.this$0 && loadContainer.position == message.what && view.getWindowToken() != null && loadContainer.generation == this.this$0.mGeneration) {
                    loadContainer.loaded = true;
                    this.this$0.bindView(view, loadContainer.bind_object);
                }
            }
        };
    }

    private T cachedLoadObject() {
        if (this.mLoadingObject == null) {
            this.mLoadingObject = getLoadingObject();
        }
        return this.mLoadingObject;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void loadRowObject(int i, ThreadedCursorAdapter<T>.LoadContainer loadContainer) {
        if (loadContainer == null || loadContainer.position != i || loadContainer.owner != this || loadContainer.view.get() == null) {
            return;
        }
        synchronized (this.mCursorLock) {
            Cursor cursor = (Cursor) this.mCursorAdapter.getItem(i);
            if (cursor == null || cursor.isClosed()) {
                return;
            }
            loadContainer.bind_object = getRowObject(cursor, loadContainer.bind_object);
            this.mHandler.obtainMessage(i, loadContainer).sendToTarget();
        }
    }

    public abstract void bindView(View view, T t);

    public void changeCursor(Cursor cursor) {
        this.mLoadHandler.removeCallbacksAndMessages(null);
        this.mHandler.removeCallbacksAndMessages(null);
        synchronized (this.mCursorLock) {
            this.mHasCursor = cursor != null;
            this.mCursorAdapter.changeCursor(cursor);
        }
    }

    @Override // android.widget.Adapter
    public int getCount() {
        return this.mSize;
    }

    @Override // android.widget.Adapter
    public Cursor getItem(int i) {
        return (Cursor) this.mCursorAdapter.getItem(i);
    }

    @Override // android.widget.Adapter
    public long getItemId(int i) {
        long itemId;
        synchronized (this.mCursorLock) {
            itemId = getItemId(getItem(i));
        }
        return itemId;
    }

    protected abstract long getItemId(Cursor cursor);

    public abstract T getLoadingObject();

    public abstract T getRowObject(Cursor cursor, T t);

    @Override // android.widget.Adapter
    public View getView(int i, View view, ViewGroup viewGroup) {
        View view2 = view;
        if (view == null) {
            view2 = newView(this.mContext, viewGroup);
        }
        LoadContainer loadContainer = (LoadContainer) view2.getTag(2131558405);
        LoadContainer loadContainer2 = loadContainer;
        if (loadContainer == null) {
            loadContainer2 = new LoadContainer(this, null);
            loadContainer2.view = new WeakReference<>(view2);
            view2.setTag(2131558405, loadContainer2);
        }
        if (loadContainer2.position == i && loadContainer2.owner == this && loadContainer2.loaded && loadContainer2.generation == this.mGeneration) {
            bindView(view2, loadContainer2.bind_object);
        } else {
            bindView(view2, cachedLoadObject());
            if (this.mHasCursor) {
                loadContainer2.position = i;
                loadContainer2.loaded = false;
                loadContainer2.owner = this;
                loadContainer2.generation = this.mGeneration;
                this.mLoadHandler.obtainMessage(i, loadContainer2).sendToTarget();
            }
        }
        return view2;
    }

    public abstract View newView(Context context, ViewGroup viewGroup);

    public void releaseCursor(LoaderManager loaderManager, int i) {
        synchronized (this.mCursorLock) {
            changeCursor(null);
            loaderManager.destroyLoader(i);
        }
    }
}
