package com.android.setupwizardlib.template;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.android.setupwizardlib.R;
import com.android.setupwizardlib.TemplateLayout;
/* loaded from: classes.dex */
public class IconMixin implements Mixin {
    private TemplateLayout mTemplateLayout;

    public IconMixin(TemplateLayout templateLayout, AttributeSet attributeSet, int i) {
        this.mTemplateLayout = templateLayout;
        TypedArray obtainStyledAttributes = templateLayout.getContext().obtainStyledAttributes(attributeSet, R.styleable.SuwIconMixin, i, 0);
        int resourceId = obtainStyledAttributes.getResourceId(R.styleable.SuwIconMixin_android_icon, 0);
        if (resourceId != 0) {
            setIcon(resourceId);
        }
        obtainStyledAttributes.recycle();
    }

    public void setIcon(Drawable drawable) {
        ImageView view = getView();
        if (view != null) {
            view.setImageDrawable(drawable);
            view.setVisibility(drawable != null ? 0 : 8);
        }
    }

    public void setIcon(int i) {
        ImageView view = getView();
        if (view != null) {
            view.setImageResource(i);
            view.setVisibility(i != 0 ? 0 : 8);
        }
    }

    public Drawable getIcon() {
        ImageView view = getView();
        if (view != null) {
            return view.getDrawable();
        }
        return null;
    }

    protected ImageView getView() {
        return (ImageView) this.mTemplateLayout.findManagedViewById(R.id.suw_layout_icon);
    }
}
