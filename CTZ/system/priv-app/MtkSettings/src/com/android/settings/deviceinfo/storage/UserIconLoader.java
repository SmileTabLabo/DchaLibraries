package com.android.settings.deviceinfo.storage;

import android.content.Context;
import android.content.pm.UserInfo;
import android.graphics.drawable.Drawable;
import android.os.UserManager;
import android.util.SparseArray;
import com.android.internal.util.Preconditions;
import com.android.settings.Utils;
import com.android.settingslib.utils.AsyncLoader;
/* loaded from: classes.dex */
public class UserIconLoader extends AsyncLoader<SparseArray<Drawable>> {
    private FetchUserIconTask mTask;

    /* loaded from: classes.dex */
    public interface FetchUserIconTask {
        SparseArray<Drawable> getUserIcons();
    }

    /* loaded from: classes.dex */
    public interface UserIconHandler {
        void handleUserIcons(SparseArray<Drawable> sparseArray);
    }

    public UserIconLoader(Context context, FetchUserIconTask fetchUserIconTask) {
        super(context);
        this.mTask = (FetchUserIconTask) Preconditions.checkNotNull(fetchUserIconTask);
    }

    @Override // android.content.AsyncTaskLoader
    public SparseArray<Drawable> loadInBackground() {
        return this.mTask.getUserIcons();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.settingslib.utils.AsyncLoader
    public void onDiscardResult(SparseArray<Drawable> sparseArray) {
    }

    public static SparseArray<Drawable> loadUserIconsWithContext(Context context) {
        SparseArray<Drawable> sparseArray = new SparseArray<>();
        UserManager userManager = (UserManager) context.getSystemService(UserManager.class);
        for (UserInfo userInfo : userManager.getUsers()) {
            sparseArray.put(userInfo.id, Utils.getUserIcon(context, userManager, userInfo));
        }
        return sparseArray;
    }
}
