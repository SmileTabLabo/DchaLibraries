package com.android.systemui.qs.customize;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.systemui.qs.QSIconView;
import com.android.systemui.qs.QSTileView;
import libcore.util.Objects;
/* loaded from: a.zip:com/android/systemui/qs/customize/CustomizeTileView.class */
public class CustomizeTileView extends QSTileView {
    private TextView mAppLabel;
    private int mLabelMinLines;

    public CustomizeTileView(Context context, QSIconView qSIconView) {
        super(context, qSIconView);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSTileView
    public void createLabel() {
        super.createLabel();
        this.mLabelMinLines = this.mLabel.getMinLines();
        View inflate = LayoutInflater.from(this.mContext).inflate(2130968766, (ViewGroup) null);
        this.mAppLabel = (TextView) inflate.findViewById(2131886589);
        this.mAppLabel.setAlpha(0.6f);
        this.mAppLabel.setSingleLine(true);
        addView(inflate);
    }

    public TextView getAppLabel() {
        return this.mAppLabel;
    }

    public void setAppLabel(CharSequence charSequence) {
        if (Objects.equal(charSequence, this.mAppLabel.getText())) {
            return;
        }
        this.mAppLabel.setText(charSequence);
    }

    public void setShowAppLabel(boolean z) {
        this.mAppLabel.setVisibility(z ? 0 : 8);
        this.mLabel.setSingleLine(z);
        if (z) {
            return;
        }
        this.mLabel.setMinLines(this.mLabelMinLines);
    }
}
