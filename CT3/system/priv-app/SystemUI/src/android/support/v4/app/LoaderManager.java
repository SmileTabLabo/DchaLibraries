package android.support.v4.app;

import android.os.Bundle;
import android.support.v4.content.Loader;
/* loaded from: a.zip:android/support/v4/app/LoaderManager.class */
public abstract class LoaderManager {

    /* loaded from: a.zip:android/support/v4/app/LoaderManager$LoaderCallbacks.class */
    public interface LoaderCallbacks<D> {
        Loader<D> onCreateLoader(int i, Bundle bundle);

        void onLoadFinished(Loader<D> loader, D d);

        void onLoaderReset(Loader<D> loader);
    }
}
