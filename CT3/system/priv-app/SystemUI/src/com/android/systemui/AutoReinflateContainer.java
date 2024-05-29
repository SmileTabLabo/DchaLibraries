package com.android.systemui;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.LocaleList;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import java.util.ArrayList;
import java.util.List;
/* loaded from: a.zip:com/android/systemui/AutoReinflateContainer.class */
public class AutoReinflateContainer extends FrameLayout {
    private int mDensity;
    private final List<InflateListener> mInflateListeners;
    private final int mLayout;
    private LocaleList mLocaleList;

    /* loaded from: a.zip:com/android/systemui/AutoReinflateContainer$InflateListener.class */
    public interface InflateListener {
        void onInflated(View view);
    }

    public AutoReinflateContainer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mInflateListeners = new ArrayList();
        this.mDensity = context.getResources().getConfiguration().densityDpi;
        this.mLocaleList = context.getResources().getConfiguration().getLocales();
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.AutoReinflateContainer);
        if (!obtainStyledAttributes.hasValue(0)) {
            throw new IllegalArgumentException("AutoReinflateContainer must contain a layout");
        }
        this.mLayout = obtainStyledAttributes.getResourceId(0, 0);
        inflateLayout();
    }

    private void inflateLayout() {
        removeAllViews();
        LayoutInflater.from(getContext()).inflate(this.mLayout, this);
        int size = this.mInflateListeners.size();
        for (int i = 0; i < size; i++) {
            this.mInflateListeners.get(i).onInflated(getChildAt(0));
        }
    }

    public void addInflateListener(InflateListener inflateListener) {
        this.mInflateListeners.add(inflateListener);
        inflateListener.onInflated(getChildAt(0));
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        boolean z = false;
        int i = configuration.densityDpi;
        if (i != this.mDensity) {
            this.mDensity = i;
            z = true;
        }
        LocaleList locales = configuration.getLocales();
        if (locales != this.mLocaleList) {
            this.mLocaleList = locales;
            z = true;
        }
        if (z) {
            inflateLayout();
        }
    }
}
