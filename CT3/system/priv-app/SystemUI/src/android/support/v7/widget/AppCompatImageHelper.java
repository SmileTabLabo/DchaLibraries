package android.support.v7.widget;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.appcompat.R$styleable;
import android.util.AttributeSet;
import android.widget.ImageView;
/* loaded from: a.zip:android/support/v7/widget/AppCompatImageHelper.class */
public class AppCompatImageHelper {
    private final AppCompatDrawableManager mDrawableManager;
    private final ImageView mView;

    public AppCompatImageHelper(ImageView imageView, AppCompatDrawableManager appCompatDrawableManager) {
        this.mView = imageView;
        this.mDrawableManager = appCompatDrawableManager;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean hasOverlappingRendering() {
        return Build.VERSION.SDK_INT < 21 || !(this.mView.getBackground() instanceof RippleDrawable);
    }

    public void loadFromAttributes(AttributeSet attributeSet, int i) {
        TintTypedArray tintTypedArray = null;
        TintTypedArray tintTypedArray2 = null;
        try {
            Drawable drawable = this.mView.getDrawable();
            Drawable drawable2 = drawable;
            if (drawable == null) {
                TintTypedArray obtainStyledAttributes = TintTypedArray.obtainStyledAttributes(this.mView.getContext(), attributeSet, R$styleable.AppCompatImageView, i, 0);
                int resourceId = obtainStyledAttributes.getResourceId(R$styleable.AppCompatImageView_srcCompat, -1);
                tintTypedArray = obtainStyledAttributes;
                drawable2 = drawable;
                if (resourceId != -1) {
                    Drawable drawable3 = this.mDrawableManager.getDrawable(this.mView.getContext(), resourceId);
                    tintTypedArray = obtainStyledAttributes;
                    drawable2 = drawable3;
                    if (drawable3 != null) {
                        this.mView.setImageDrawable(drawable3);
                        drawable2 = drawable3;
                        tintTypedArray = obtainStyledAttributes;
                    }
                }
            }
            if (drawable2 != null) {
                tintTypedArray2 = tintTypedArray;
                DrawableUtils.fixDrawable(drawable2);
            }
            if (tintTypedArray != null) {
                tintTypedArray.recycle();
            }
        } catch (Throwable th) {
            if (tintTypedArray2 != null) {
                tintTypedArray2.recycle();
            }
            throw th;
        }
    }

    public void setImageResource(int i) {
        if (i == 0) {
            this.mView.setImageDrawable(null);
            return;
        }
        Drawable drawable = this.mDrawableManager != null ? this.mDrawableManager.getDrawable(this.mView.getContext(), i) : ContextCompat.getDrawable(this.mView.getContext(), i);
        if (drawable != null) {
            DrawableUtils.fixDrawable(drawable);
        }
        this.mView.setImageDrawable(drawable);
    }
}
