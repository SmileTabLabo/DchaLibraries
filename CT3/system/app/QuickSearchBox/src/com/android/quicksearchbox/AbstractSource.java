package com.android.quicksearchbox;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import com.android.quicksearchbox.util.NamedTaskExecutor;
import com.android.quicksearchbox.util.NowOrLater;
/* loaded from: a.zip:com/android/quicksearchbox/AbstractSource.class */
public abstract class AbstractSource implements Source {
    private final Context mContext;
    private IconLoader mIconLoader;
    private final NamedTaskExecutor mIconLoaderExecutor;
    private final Handler mUiThread;

    public AbstractSource(Context context, Handler handler, NamedTaskExecutor namedTaskExecutor) {
        this.mContext = context;
        this.mUiThread = handler;
        this.mIconLoaderExecutor = namedTaskExecutor;
    }

    public static Intent createSourceSearchIntent(ComponentName componentName, String str, Bundle bundle) {
        if (componentName == null) {
            Log.w("QSB.AbstractSource", "Tried to create search intent with no target activity");
            return null;
        }
        Intent intent = new Intent("android.intent.action.SEARCH");
        intent.setComponent(componentName);
        intent.addFlags(268435456);
        intent.addFlags(67108864);
        intent.putExtra("user_query", str);
        intent.putExtra("query", str);
        if (bundle != null) {
            intent.putExtra("app_data", bundle);
        }
        return intent;
    }

    @Override // com.android.quicksearchbox.Source
    public Intent createSearchIntent(String str, Bundle bundle) {
        return createSourceSearchIntent(getIntentComponent(), str, bundle);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public Intent createVoiceWebSearchIntent(Bundle bundle) {
        return QsbApplication.get(this.mContext).getVoiceSearch().createVoiceWebSearchIntent(bundle);
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Source)) {
            return false;
        }
        Source root = ((Source) obj).getRoot();
        if (root.getClass().equals(getClass())) {
            return root.getName().equals(getName());
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public Context getContext() {
        return this.mContext;
    }

    @Override // com.android.quicksearchbox.Source
    public NowOrLater<Drawable> getIcon(String str) {
        return getIconLoader().getIcon(str);
    }

    protected IconLoader getIconLoader() {
        if (this.mIconLoader == null) {
            this.mIconLoader = new CachingIconLoader(new PackageIconLoader(this.mContext, getIconPackage(), this.mUiThread, this.mIconLoaderExecutor));
        }
        return this.mIconLoader;
    }

    protected abstract String getIconPackage();

    @Override // com.android.quicksearchbox.Source
    public Uri getIconUri(String str) {
        return getIconLoader().getIconUri(str);
    }

    @Override // com.android.quicksearchbox.Source
    public Source getRoot() {
        return this;
    }

    public int hashCode() {
        return getName().hashCode();
    }

    public String toString() {
        return "Source{name=" + getName() + "}";
    }
}
