package com.android.setupwizardlib;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
/* loaded from: classes.dex */
public class TemplateLayout extends FrameLayout {
    private ViewGroup mContainer;

    public TemplateLayout(Context context, int template, int containerId) {
        super(context);
        init(template, containerId, null, R$attr.suwLayoutTheme);
    }

    public TemplateLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(0, 0, attrs, R$attr.suwLayoutTheme);
    }

    @TargetApi(11)
    public TemplateLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(0, 0, attrs, defStyleAttr);
    }

    private void init(int template, int containerId, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R$styleable.SuwTemplateLayout, defStyleAttr, 0);
        if (template == 0) {
            template = a.getResourceId(R$styleable.SuwTemplateLayout_android_layout, 0);
        }
        if (containerId == 0) {
            containerId = a.getResourceId(R$styleable.SuwTemplateLayout_suwContainer, 0);
        }
        inflateTemplate(template, containerId);
        a.recycle();
    }

    @Override // android.view.ViewGroup
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        this.mContainer.addView(child, index, params);
    }

    private void addViewInternal(View child) {
        super.addView(child, -1, generateDefaultLayoutParams());
    }

    private void inflateTemplate(int templateResource, int containerId) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View templateRoot = onInflateTemplate(inflater, templateResource);
        addViewInternal(templateRoot);
        this.mContainer = findContainer(containerId);
        if (this.mContainer == null) {
            throw new IllegalArgumentException("Container cannot be null in TemplateLayout");
        }
        onTemplateInflated();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public View onInflateTemplate(LayoutInflater inflater, int template) {
        if (template == 0) {
            throw new IllegalArgumentException("android:layout not specified for TemplateLayout");
        }
        return inflater.inflate(template, (ViewGroup) this, false);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public ViewGroup findContainer(int containerId) {
        if (containerId == 0) {
            containerId = getContainerId();
        }
        return (ViewGroup) findViewById(containerId);
    }

    protected void onTemplateInflated() {
    }

    @Deprecated
    protected int getContainerId() {
        return 0;
    }
}
