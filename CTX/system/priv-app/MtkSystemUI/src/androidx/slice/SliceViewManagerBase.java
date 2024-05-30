package androidx.slice;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.ArrayMap;
import android.util.Pair;
import androidx.slice.SliceViewManager;
import androidx.slice.widget.SliceLiveData;
import java.util.concurrent.Executor;
/* loaded from: classes.dex */
public abstract class SliceViewManagerBase extends SliceViewManager {
    protected final Context mContext;
    private final ArrayMap<Pair<Uri, SliceViewManager.SliceCallback>, SliceListenerImpl> mListenerLookup = new ArrayMap<>();

    /* JADX INFO: Access modifiers changed from: package-private */
    public SliceViewManagerBase(Context context) {
        this.mContext = context;
    }

    @Override // androidx.slice.SliceViewManager
    public void registerSliceCallback(Uri uri, SliceViewManager.SliceCallback callback) {
        final Handler h = new Handler(Looper.getMainLooper());
        registerSliceCallback(uri, new Executor() { // from class: androidx.slice.SliceViewManagerBase.1
            @Override // java.util.concurrent.Executor
            public void execute(Runnable command) {
                h.post(command);
            }
        }, callback);
    }

    public void registerSliceCallback(Uri uri, Executor executor, SliceViewManager.SliceCallback callback) {
        getListener(uri, callback, new SliceListenerImpl(uri, executor, callback)).startListening();
    }

    @Override // androidx.slice.SliceViewManager
    public void unregisterSliceCallback(Uri uri, SliceViewManager.SliceCallback callback) {
        synchronized (this.mListenerLookup) {
            SliceListenerImpl impl = this.mListenerLookup.remove(new Pair(uri, callback));
            if (impl != null) {
                impl.stopListening();
            }
        }
    }

    private SliceListenerImpl getListener(Uri uri, SliceViewManager.SliceCallback callback, SliceListenerImpl listener) {
        Pair<Uri, SliceViewManager.SliceCallback> key = new Pair<>(uri, callback);
        synchronized (this.mListenerLookup) {
            if (this.mListenerLookup.containsKey(key)) {
                this.mListenerLookup.get(key).stopListening();
            }
            this.mListenerLookup.put(key, listener);
        }
        return listener;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class SliceListenerImpl {
        private final SliceViewManager.SliceCallback mCallback;
        private final Executor mExecutor;
        private boolean mPinned;
        private Uri mUri;
        private final Runnable mUpdateSlice = new Runnable() { // from class: androidx.slice.SliceViewManagerBase.SliceListenerImpl.1
            @Override // java.lang.Runnable
            public void run() {
                SliceListenerImpl.this.tryPin();
                final Slice s = Slice.bindSlice(SliceViewManagerBase.this.mContext, SliceListenerImpl.this.mUri, SliceLiveData.SUPPORTED_SPECS);
                SliceListenerImpl.this.mExecutor.execute(new Runnable() { // from class: androidx.slice.SliceViewManagerBase.SliceListenerImpl.1.1
                    @Override // java.lang.Runnable
                    public void run() {
                        SliceListenerImpl.this.mCallback.onSliceUpdated(s);
                    }
                });
            }
        };
        private final ContentObserver mObserver = new ContentObserver(new Handler(Looper.getMainLooper())) { // from class: androidx.slice.SliceViewManagerBase.SliceListenerImpl.2
            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                AsyncTask.execute(SliceListenerImpl.this.mUpdateSlice);
            }
        };

        SliceListenerImpl(Uri uri, Executor executor, SliceViewManager.SliceCallback callback) {
            this.mUri = uri;
            this.mExecutor = executor;
            this.mCallback = callback;
        }

        void startListening() {
            SliceViewManagerBase.this.mContext.getContentResolver().registerContentObserver(this.mUri, true, this.mObserver);
            tryPin();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void tryPin() {
            if (!this.mPinned) {
                try {
                    SliceViewManagerBase.this.pinSlice(this.mUri);
                    this.mPinned = true;
                } catch (SecurityException e) {
                }
            }
        }

        void stopListening() {
            SliceViewManagerBase.this.mContext.getContentResolver().unregisterContentObserver(this.mObserver);
            if (this.mPinned) {
                SliceViewManagerBase.this.unpinSlice(this.mUri);
                this.mPinned = false;
            }
        }
    }
}
