package android.support.v7.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDelegate;
import java.lang.ref.WeakReference;
/* loaded from: a.zip:android/support/v7/widget/VectorEnabledTintResources.class */
public class VectorEnabledTintResources extends Resources {
    private final WeakReference<Context> mContextRef;

    public VectorEnabledTintResources(@NonNull Context context, @NonNull Resources resources) {
        super(resources.getAssets(), resources.getDisplayMetrics(), resources.getConfiguration());
        this.mContextRef = new WeakReference<>(context);
    }

    public static boolean shouldBeUsed() {
        boolean z = false;
        if (AppCompatDelegate.isCompatVectorFromResourcesEnabled()) {
            z = false;
            if (Build.VERSION.SDK_INT <= 20) {
                z = true;
            }
        }
        return z;
    }

    @Override // android.content.res.Resources
    public Drawable getDrawable(int i) throws Resources.NotFoundException {
        Context context = this.mContextRef.get();
        return context != null ? AppCompatDrawableManager.get().onDrawableLoadedFromResources(context, this, i) : super.getDrawable(i);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final Drawable superGetDrawable(int i) {
        return super.getDrawable(i);
    }
}
