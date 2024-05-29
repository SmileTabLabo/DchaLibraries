package android.support.v7.widget;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDelegate;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
/* loaded from: a.zip:android/support/v7/widget/TintContextWrapper.class */
public class TintContextWrapper extends ContextWrapper {
    private static final ArrayList<WeakReference<TintContextWrapper>> sCache = new ArrayList<>();
    private Resources mResources;
    private final Resources.Theme mTheme;

    private TintContextWrapper(@NonNull Context context) {
        super(context);
        if (!VectorEnabledTintResources.shouldBeUsed()) {
            this.mTheme = null;
            return;
        }
        this.mTheme = getResources().newTheme();
        this.mTheme.setTo(context.getTheme());
    }

    private static boolean shouldWrap(@NonNull Context context) {
        if ((context instanceof TintContextWrapper) || (context.getResources() instanceof TintResources) || (context.getResources() instanceof VectorEnabledTintResources)) {
            return false;
        }
        return !AppCompatDelegate.isCompatVectorFromResourcesEnabled() || Build.VERSION.SDK_INT <= 20;
    }

    public static Context wrap(@NonNull Context context) {
        if (shouldWrap(context)) {
            int size = sCache.size();
            for (int i = 0; i < size; i++) {
                WeakReference<TintContextWrapper> weakReference = sCache.get(i);
                TintContextWrapper tintContextWrapper = weakReference != null ? weakReference.get() : null;
                if (tintContextWrapper != null && tintContextWrapper.getBaseContext() == context) {
                    return tintContextWrapper;
                }
            }
            TintContextWrapper tintContextWrapper2 = new TintContextWrapper(context);
            sCache.add(new WeakReference<>(tintContextWrapper2));
            return tintContextWrapper2;
        }
        return context;
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public Resources getResources() {
        if (this.mResources == null) {
            this.mResources = this.mTheme == null ? new TintResources(this, super.getResources()) : new VectorEnabledTintResources(this, super.getResources());
        }
        return this.mResources;
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public Resources.Theme getTheme() {
        return this.mTheme == null ? super.getTheme() : this.mTheme;
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public void setTheme(int i) {
        if (this.mTheme == null) {
            super.setTheme(i);
        } else {
            this.mTheme.applyStyle(i, true);
        }
    }
}
