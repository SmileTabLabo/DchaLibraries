package com.android.setupwizardlib;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
/* loaded from: classes.dex */
public class SetupWizardPreferenceLayout extends SetupWizardRecyclerLayout {
    private RecyclerView mRecyclerView;

    public SetupWizardPreferenceLayout(Context context) {
        super(context);
    }

    public SetupWizardPreferenceLayout(Context context, int template, int containerId) {
        super(context, template, containerId);
    }

    public SetupWizardPreferenceLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SetupWizardPreferenceLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override // com.android.setupwizardlib.SetupWizardRecyclerLayout
    public RecyclerView getRecyclerView() {
        return this.mRecyclerView;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.setupwizardlib.SetupWizardRecyclerLayout, com.android.setupwizardlib.SetupWizardLayout, com.android.setupwizardlib.TemplateLayout
    public ViewGroup findContainer(int containerId) {
        if (containerId == 0) {
            containerId = R$id.suw_layout_content;
        }
        return super.findContainer(containerId);
    }

    public RecyclerView onCreateRecyclerView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return this.mRecyclerView;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.setupwizardlib.SetupWizardRecyclerLayout, com.android.setupwizardlib.SetupWizardLayout, com.android.setupwizardlib.TemplateLayout
    public View onInflateTemplate(LayoutInflater inflater, int template) {
        if (template == 0) {
            template = R$layout.suw_preference_template;
        }
        return super.onInflateTemplate(inflater, template);
    }

    @Override // com.android.setupwizardlib.SetupWizardRecyclerLayout, com.android.setupwizardlib.TemplateLayout
    protected void onTemplateInflated() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        this.mRecyclerView = (RecyclerView) inflater.inflate(R$layout.suw_preference_recycler_view, (ViewGroup) this, false);
        initRecyclerView(this.mRecyclerView);
    }
}
