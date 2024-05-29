package com.android.systemui.statusbar;

import android.util.ArraySet;
import com.android.internal.util.Preconditions;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.phone.StatusBarWindowManager;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/systemui/statusbar/RemoteInputController.class */
public class RemoteInputController {
    private final HeadsUpManager mHeadsUpManager;
    private final ArrayList<WeakReference<NotificationData.Entry>> mOpen = new ArrayList<>();
    private final ArraySet<String> mSpinning = new ArraySet<>();
    private final ArrayList<Callback> mCallbacks = new ArrayList<>(3);

    /* loaded from: a.zip:com/android/systemui/statusbar/RemoteInputController$Callback.class */
    public interface Callback {
        default void onRemoteInputActive(boolean z) {
        }

        default void onRemoteInputSent(NotificationData.Entry entry) {
        }
    }

    public RemoteInputController(StatusBarWindowManager statusBarWindowManager, HeadsUpManager headsUpManager) {
        addCallback(statusBarWindowManager);
        this.mHeadsUpManager = headsUpManager;
    }

    private void apply(NotificationData.Entry entry) {
        this.mHeadsUpManager.setRemoteInputActive(entry, isRemoteInputActive(entry));
        boolean isRemoteInputActive = isRemoteInputActive();
        int size = this.mCallbacks.size();
        for (int i = 0; i < size; i++) {
            this.mCallbacks.get(i).onRemoteInputActive(isRemoteInputActive);
        }
    }

    private boolean pruneWeakThenRemoveAndContains(NotificationData.Entry entry, NotificationData.Entry entry2) {
        boolean z = false;
        for (int size = this.mOpen.size() - 1; size >= 0; size--) {
            NotificationData.Entry entry3 = this.mOpen.get(size).get();
            if (entry3 == null || entry3 == entry2) {
                this.mOpen.remove(size);
            } else if (entry3 == entry) {
                z = true;
            }
        }
        return z;
    }

    public void addCallback(Callback callback) {
        Preconditions.checkNotNull(callback);
        this.mCallbacks.add(callback);
    }

    public void addRemoteInput(NotificationData.Entry entry) {
        Preconditions.checkNotNull(entry);
        if (!pruneWeakThenRemoveAndContains(entry, null)) {
            this.mOpen.add(new WeakReference<>(entry));
        }
        apply(entry);
    }

    public void addSpinning(String str) {
        this.mSpinning.add(str);
    }

    public void closeRemoteInputs() {
        if (this.mOpen.size() == 0) {
            return;
        }
        ArrayList arrayList = new ArrayList(this.mOpen.size());
        for (int size = this.mOpen.size() - 1; size >= 0; size--) {
            NotificationData.Entry entry = this.mOpen.get(size).get();
            if (entry != null && entry.row != null) {
                arrayList.add(entry);
            }
        }
        for (int size2 = arrayList.size() - 1; size2 >= 0; size2--) {
            NotificationData.Entry entry2 = (NotificationData.Entry) arrayList.get(size2);
            if (entry2.row != null) {
                entry2.row.closeRemoteInput();
            }
        }
    }

    public boolean isRemoteInputActive() {
        pruneWeakThenRemoveAndContains(null, null);
        return !this.mOpen.isEmpty();
    }

    public boolean isRemoteInputActive(NotificationData.Entry entry) {
        return pruneWeakThenRemoveAndContains(entry, null);
    }

    public boolean isSpinning(String str) {
        return this.mSpinning.contains(str);
    }

    public void remoteInputSent(NotificationData.Entry entry) {
        int size = this.mCallbacks.size();
        for (int i = 0; i < size; i++) {
            this.mCallbacks.get(i).onRemoteInputSent(entry);
        }
    }

    public void removeRemoteInput(NotificationData.Entry entry) {
        Preconditions.checkNotNull(entry);
        pruneWeakThenRemoveAndContains(null, entry);
        apply(entry);
    }

    public void removeSpinning(String str) {
        this.mSpinning.remove(str);
    }
}
