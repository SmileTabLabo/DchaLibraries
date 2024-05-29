package com.android.quicksearchbox.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewDebug;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
/* loaded from: a.zip:com/android/quicksearchbox/ui/CorpusView.class */
public class CorpusView extends RelativeLayout implements Checkable {
    private static final int[] CHECKED_STATE_SET = {16842912};
    private boolean mChecked;
    private ImageView mIcon;
    private TextView mLabel;

    public CorpusView(Context context) {
        super(context);
    }

    public CorpusView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override // android.widget.Checkable
    @ViewDebug.ExportedProperty
    public boolean isChecked() {
        return this.mChecked;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected int[] onCreateDrawableState(int i) {
        int[] onCreateDrawableState = super.onCreateDrawableState(i + 1);
        if (isChecked()) {
            mergeDrawableStates(onCreateDrawableState, CHECKED_STATE_SET);
        }
        return onCreateDrawableState;
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mIcon = (ImageView) findViewById(2131689472);
        this.mLabel = (TextView) findViewById(2131689473);
    }

    @Override // android.widget.Checkable
    public void setChecked(boolean z) {
        if (this.mChecked != z) {
            this.mChecked = z;
            refreshDrawableState();
        }
    }

    @Override // android.widget.Checkable
    public void toggle() {
        setChecked(!this.mChecked);
    }
}
