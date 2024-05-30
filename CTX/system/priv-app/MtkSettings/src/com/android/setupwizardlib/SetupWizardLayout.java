package com.android.setupwizardlib;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import com.android.setupwizardlib.template.HeaderMixin;
import com.android.setupwizardlib.template.NavigationBarMixin;
import com.android.setupwizardlib.template.ProgressBarMixin;
import com.android.setupwizardlib.template.RequireScrollMixin;
import com.android.setupwizardlib.template.ScrollViewScrollHandlingDelegate;
import com.android.setupwizardlib.view.Illustration;
import com.android.setupwizardlib.view.NavigationBar;
/* loaded from: classes.dex */
public class SetupWizardLayout extends TemplateLayout {
    public SetupWizardLayout(Context context) {
        super(context, 0, 0);
        init(null, R.attr.suwLayoutTheme);
    }

    public SetupWizardLayout(Context context, int i, int i2) {
        super(context, i, i2);
        init(null, R.attr.suwLayoutTheme);
    }

    public SetupWizardLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(attributeSet, R.attr.suwLayoutTheme);
    }

    @TargetApi(11)
    public SetupWizardLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init(attributeSet, i);
    }

    private void init(AttributeSet attributeSet, int i) {
        registerMixin(HeaderMixin.class, new HeaderMixin(this, attributeSet, i));
        registerMixin(ProgressBarMixin.class, new ProgressBarMixin(this));
        registerMixin(NavigationBarMixin.class, new NavigationBarMixin(this));
        RequireScrollMixin requireScrollMixin = new RequireScrollMixin(this);
        registerMixin(RequireScrollMixin.class, requireScrollMixin);
        ScrollView scrollView = getScrollView();
        if (scrollView != null) {
            requireScrollMixin.setScrollHandlingDelegate(new ScrollViewScrollHandlingDelegate(requireScrollMixin, scrollView));
        }
        TypedArray obtainStyledAttributes = getContext().obtainStyledAttributes(attributeSet, R.styleable.SuwSetupWizardLayout, i, 0);
        Drawable drawable = obtainStyledAttributes.getDrawable(R.styleable.SuwSetupWizardLayout_suwBackground);
        if (drawable != null) {
            setLayoutBackground(drawable);
        } else {
            Drawable drawable2 = obtainStyledAttributes.getDrawable(R.styleable.SuwSetupWizardLayout_suwBackgroundTile);
            if (drawable2 != null) {
                setBackgroundTile(drawable2);
            }
        }
        Drawable drawable3 = obtainStyledAttributes.getDrawable(R.styleable.SuwSetupWizardLayout_suwIllustration);
        if (drawable3 != null) {
            setIllustration(drawable3);
        } else {
            Drawable drawable4 = obtainStyledAttributes.getDrawable(R.styleable.SuwSetupWizardLayout_suwIllustrationImage);
            Drawable drawable5 = obtainStyledAttributes.getDrawable(R.styleable.SuwSetupWizardLayout_suwIllustrationHorizontalTile);
            if (drawable4 != null && drawable5 != null) {
                setIllustration(drawable4, drawable5);
            }
        }
        int dimensionPixelSize = obtainStyledAttributes.getDimensionPixelSize(R.styleable.SuwSetupWizardLayout_suwDecorPaddingTop, -1);
        if (dimensionPixelSize == -1) {
            dimensionPixelSize = getResources().getDimensionPixelSize(R.dimen.suw_decor_padding_top);
        }
        setDecorPaddingTop(dimensionPixelSize);
        float f = obtainStyledAttributes.getFloat(R.styleable.SuwSetupWizardLayout_suwIllustrationAspectRatio, -1.0f);
        if (f == -1.0f) {
            TypedValue typedValue = new TypedValue();
            getResources().getValue(R.dimen.suw_illustration_aspect_ratio, typedValue, true);
            f = typedValue.getFloat();
        }
        setIllustrationAspectRatio(f);
        obtainStyledAttributes.recycle();
    }

    @Override // android.view.View
    protected Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        savedState.mIsProgressBarShown = isProgressBarShown();
        return savedState;
    }

    @Override // android.view.View
    protected void onRestoreInstanceState(Parcelable parcelable) {
        if (!(parcelable instanceof SavedState)) {
            Log.w("SetupWizardLayout", "Ignoring restore instance state " + parcelable);
            super.onRestoreInstanceState(parcelable);
            return;
        }
        SavedState savedState = (SavedState) parcelable;
        super.onRestoreInstanceState(savedState.getSuperState());
        setProgressBarShown(savedState.mIsProgressBarShown);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.setupwizardlib.TemplateLayout
    public View onInflateTemplate(LayoutInflater layoutInflater, int i) {
        if (i == 0) {
            i = R.layout.suw_template;
        }
        return inflateTemplate(layoutInflater, R.style.SuwThemeMaterial_Light, i);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.setupwizardlib.TemplateLayout
    public ViewGroup findContainer(int i) {
        if (i == 0) {
            i = R.id.suw_layout_content;
        }
        return super.findContainer(i);
    }

    public NavigationBar getNavigationBar() {
        return ((NavigationBarMixin) getMixin(NavigationBarMixin.class)).getNavigationBar();
    }

    public ScrollView getScrollView() {
        View findManagedViewById = findManagedViewById(R.id.suw_bottom_scroll_view);
        if (findManagedViewById instanceof ScrollView) {
            return (ScrollView) findManagedViewById;
        }
        return null;
    }

    public void setHeaderText(int i) {
        ((HeaderMixin) getMixin(HeaderMixin.class)).setText(i);
    }

    public void setHeaderText(CharSequence charSequence) {
        ((HeaderMixin) getMixin(HeaderMixin.class)).setText(charSequence);
    }

    public CharSequence getHeaderText() {
        return ((HeaderMixin) getMixin(HeaderMixin.class)).getText();
    }

    public TextView getHeaderTextView() {
        return ((HeaderMixin) getMixin(HeaderMixin.class)).getTextView();
    }

    public void setIllustration(Drawable drawable) {
        View findManagedViewById = findManagedViewById(R.id.suw_layout_decor);
        if (findManagedViewById instanceof Illustration) {
            ((Illustration) findManagedViewById).setIllustration(drawable);
        }
    }

    private void setIllustration(Drawable drawable, Drawable drawable2) {
        View findManagedViewById = findManagedViewById(R.id.suw_layout_decor);
        if (findManagedViewById instanceof Illustration) {
            ((Illustration) findManagedViewById).setIllustration(getIllustration(drawable, drawable2));
        }
    }

    public void setIllustrationAspectRatio(float f) {
        View findManagedViewById = findManagedViewById(R.id.suw_layout_decor);
        if (findManagedViewById instanceof Illustration) {
            ((Illustration) findManagedViewById).setAspectRatio(f);
        }
    }

    public void setDecorPaddingTop(int i) {
        View findManagedViewById = findManagedViewById(R.id.suw_layout_decor);
        if (findManagedViewById != null) {
            findManagedViewById.setPadding(findManagedViewById.getPaddingLeft(), i, findManagedViewById.getPaddingRight(), findManagedViewById.getPaddingBottom());
        }
    }

    public void setLayoutBackground(Drawable drawable) {
        View findManagedViewById = findManagedViewById(R.id.suw_layout_decor);
        if (findManagedViewById != null) {
            findManagedViewById.setBackgroundDrawable(drawable);
        }
    }

    public void setBackgroundTile(int i) {
        setBackgroundTile(getContext().getResources().getDrawable(i));
    }

    private void setBackgroundTile(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            ((BitmapDrawable) drawable).setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        }
        setLayoutBackground(drawable);
    }

    @SuppressLint({"RtlHardcoded"})
    private Drawable getIllustration(Drawable drawable, Drawable drawable2) {
        if (getContext().getResources().getBoolean(R.bool.suwUseTabletLayout)) {
            if (drawable2 instanceof BitmapDrawable) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable2;
                bitmapDrawable.setTileModeX(Shader.TileMode.REPEAT);
                bitmapDrawable.setGravity(48);
            }
            if (drawable instanceof BitmapDrawable) {
                ((BitmapDrawable) drawable).setGravity(51);
            }
            LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{drawable2, drawable});
            if (Build.VERSION.SDK_INT >= 19) {
                layerDrawable.setAutoMirrored(true);
            }
            return layerDrawable;
        }
        if (Build.VERSION.SDK_INT >= 19) {
            drawable.setAutoMirrored(true);
        }
        return drawable;
    }

    public boolean isProgressBarShown() {
        return ((ProgressBarMixin) getMixin(ProgressBarMixin.class)).isShown();
    }

    public void setProgressBarShown(boolean z) {
        ((ProgressBarMixin) getMixin(ProgressBarMixin.class)).setShown(z);
    }

    public void setProgressBarColor(ColorStateList colorStateList) {
        ((ProgressBarMixin) getMixin(ProgressBarMixin.class)).setColor(colorStateList);
    }

    public ColorStateList getProgressBarColor() {
        return ((ProgressBarMixin) getMixin(ProgressBarMixin.class)).getColor();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* loaded from: classes.dex */
    public static class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() { // from class: com.android.setupwizardlib.SetupWizardLayout.SavedState.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public SavedState createFromParcel(Parcel parcel) {
                return new SavedState(parcel);
            }

            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public SavedState[] newArray(int i) {
                return new SavedState[i];
            }
        };
        boolean mIsProgressBarShown;

        public SavedState(Parcelable parcelable) {
            super(parcelable);
            this.mIsProgressBarShown = false;
        }

        public SavedState(Parcel parcel) {
            super(parcel);
            this.mIsProgressBarShown = false;
            this.mIsProgressBarShown = parcel.readInt() != 0;
        }

        @Override // android.view.View.BaseSavedState, android.view.AbsSavedState, android.os.Parcelable
        public void writeToParcel(Parcel parcel, int i) {
            super.writeToParcel(parcel, i);
            parcel.writeInt(this.mIsProgressBarShown ? 1 : 0);
        }
    }
}
