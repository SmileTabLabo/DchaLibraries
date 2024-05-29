package com.android.systemui.screenshot;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
/* loaded from: a.zip:com/android/systemui/screenshot/DeleteImageInBackgroundTask.class */
class DeleteImageInBackgroundTask extends AsyncTask<Uri, Void, Void> {
    private Context mContext;

    /* JADX INFO: Access modifiers changed from: package-private */
    public DeleteImageInBackgroundTask(Context context) {
        this.mContext = context;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    public Void doInBackground(Uri... uriArr) {
        if (uriArr.length != 1) {
            return null;
        }
        this.mContext.getContentResolver().delete(uriArr[0], null, null);
        return null;
    }
}
