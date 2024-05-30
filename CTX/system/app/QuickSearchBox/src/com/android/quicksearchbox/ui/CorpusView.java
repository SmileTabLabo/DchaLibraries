package com.android.quicksearchbox.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewDebug;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.quicksearchbox.R;
/* loaded from: classes.dex */
public class CorpusView extends RelativeLayout implements Checkable {
    private static final int[] CHECKED_STATE_SET = {16842912};
    private boolean mChecked;
    private ImageView mIcon;
    private TextView mLabel;

    public CorpusView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public CorpusView(Context context) {
        super(context);
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mIcon = (ImageView) findViewById(R.id.source_icon);
        this.mLabel = (TextView) findViewById(R.id.source_label);
    }

    @Override // android.widget.Checkable
    @ViewDebug.ExportedProperty
    public boolean isChecked() {
        return this.mChecked;
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

    @Override // android.view.ViewGroup, android.view.View
    protected int[] onCreateDrawableState(int i) {
        int[] onCreateDrawableState = super.onCreateDrawableState(i + 1);
        if (isChecked()) {
            mergeDrawableStates(onCreateDrawableState, CHECKED_STATE_SET);
        }
        return onCreateDrawableState;
    }
}
