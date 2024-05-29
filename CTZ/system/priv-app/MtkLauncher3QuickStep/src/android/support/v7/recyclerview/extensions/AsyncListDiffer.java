package android.support.v7.recyclerview.extensions;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.recyclerview.extensions.AsyncDifferConfig;
import android.support.v7.util.AdapterListUpdateCallback;
import android.support.v7.util.DiffUtil;
import android.support.v7.util.ListUpdateCallback;
import android.support.v7.widget.RecyclerView;
import java.util.Collections;
import java.util.List;
/* loaded from: classes.dex */
public class AsyncListDiffer<T> {
    private final AsyncDifferConfig<T> mConfig;
    @Nullable
    private List<T> mList;
    private int mMaxScheduledGeneration;
    @NonNull
    private List<T> mReadOnlyList = Collections.emptyList();
    private final ListUpdateCallback mUpdateCallback;

    public AsyncListDiffer(@NonNull RecyclerView.Adapter adapter, @NonNull DiffUtil.ItemCallback<T> diffCallback) {
        this.mUpdateCallback = new AdapterListUpdateCallback(adapter);
        this.mConfig = new AsyncDifferConfig.Builder(diffCallback).build();
    }

    public AsyncListDiffer(@NonNull ListUpdateCallback listUpdateCallback, @NonNull AsyncDifferConfig<T> config) {
        this.mUpdateCallback = listUpdateCallback;
        this.mConfig = config;
    }

    @NonNull
    public List<T> getCurrentList() {
        return this.mReadOnlyList;
    }

    public void submitList(@Nullable final List<T> newList) {
        if (newList == this.mList) {
            return;
        }
        final int runGeneration = this.mMaxScheduledGeneration + 1;
        this.mMaxScheduledGeneration = runGeneration;
        if (newList == null) {
            int countRemoved = this.mList.size();
            this.mList = null;
            this.mReadOnlyList = Collections.emptyList();
            this.mUpdateCallback.onRemoved(0, countRemoved);
        } else if (this.mList == null) {
            this.mList = newList;
            this.mReadOnlyList = Collections.unmodifiableList(newList);
            this.mUpdateCallback.onInserted(0, newList.size());
        } else {
            final List<T> oldList = this.mList;
            this.mConfig.getBackgroundThreadExecutor().execute(new Runnable() { // from class: android.support.v7.recyclerview.extensions.AsyncListDiffer.1
                @Override // java.lang.Runnable
                public void run() {
                    final DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() { // from class: android.support.v7.recyclerview.extensions.AsyncListDiffer.1.1
                        @Override // android.support.v7.util.DiffUtil.Callback
                        public int getOldListSize() {
                            return oldList.size();
                        }

                        @Override // android.support.v7.util.DiffUtil.Callback
                        public int getNewListSize() {
                            return newList.size();
                        }

                        @Override // android.support.v7.util.DiffUtil.Callback
                        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                            Object obj = oldList.get(oldItemPosition);
                            Object obj2 = newList.get(newItemPosition);
                            if (obj == null || obj2 == null) {
                                return obj == null && obj2 == null;
                            }
                            return AsyncListDiffer.this.mConfig.getDiffCallback().areItemsTheSame(obj, obj2);
                        }

                        @Override // android.support.v7.util.DiffUtil.Callback
                        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                            Object obj = oldList.get(oldItemPosition);
                            Object obj2 = newList.get(newItemPosition);
                            if (obj != null && obj2 != null) {
                                return AsyncListDiffer.this.mConfig.getDiffCallback().areContentsTheSame(obj, obj2);
                            }
                            if (obj == null && obj2 == null) {
                                return true;
                            }
                            throw new AssertionError();
                        }

                        @Override // android.support.v7.util.DiffUtil.Callback
                        @Nullable
                        public Object getChangePayload(int oldItemPosition, int newItemPosition) {
                            Object obj = oldList.get(oldItemPosition);
                            Object obj2 = newList.get(newItemPosition);
                            if (obj != null && obj2 != null) {
                                return AsyncListDiffer.this.mConfig.getDiffCallback().getChangePayload(obj, obj2);
                            }
                            throw new AssertionError();
                        }
                    });
                    AsyncListDiffer.this.mConfig.getMainThreadExecutor().execute(new Runnable() { // from class: android.support.v7.recyclerview.extensions.AsyncListDiffer.1.2
                        @Override // java.lang.Runnable
                        public void run() {
                            if (AsyncListDiffer.this.mMaxScheduledGeneration == runGeneration) {
                                AsyncListDiffer.this.latchList(newList, result);
                            }
                        }
                    });
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void latchList(@NonNull List<T> newList, @NonNull DiffUtil.DiffResult diffResult) {
        this.mList = newList;
        this.mReadOnlyList = Collections.unmodifiableList(newList);
        diffResult.dispatchUpdatesTo(this.mUpdateCallback);
    }
}
