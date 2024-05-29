package com.android.systemui.qs;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.MathUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.FontSizeUtils;
import com.android.systemui.qs.QSTile;
import libcore.util.Objects;
/* loaded from: a.zip:com/android/systemui/qs/QSTileView.class */
public class QSTileView extends QSTileBaseView {
    protected final Context mContext;
    protected TextView mLabel;
    private ImageView mPadLock;
    private int mTilePaddingTopPx;
    private final int mTileSpacingPx;

    public QSTileView(Context context, QSIconView qSIconView) {
        this(context, qSIconView, false);
    }

    public QSTileView(Context context, QSIconView qSIconView, boolean z) {
        super(context, qSIconView, z);
        this.mContext = context;
        this.mTileSpacingPx = context.getResources().getDimensionPixelSize(2131689846);
        setClipChildren(false);
        setClickable(true);
        updateTopPadding();
        setId(View.generateViewId());
        createLabel();
        setOrientation(1);
        setGravity(17);
    }

    private void updateTopPadding() {
        Resources resources = getResources();
        int dimensionPixelSize = resources.getDimensionPixelSize(2131689842);
        int dimensionPixelSize2 = resources.getDimensionPixelSize(2131689843);
        float constrain = (MathUtils.constrain(getResources().getConfiguration().fontScale, 1.0f, 1.3f) - 1.0f) / 0.29999995f;
        this.mTilePaddingTopPx = Math.round(((1.0f - constrain) * dimensionPixelSize) + (dimensionPixelSize2 * constrain));
        setPadding(this.mTileSpacingPx, this.mTilePaddingTopPx + this.mTileSpacingPx, this.mTileSpacingPx, this.mTileSpacingPx);
        requestLayout();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void createLabel() {
        View inflate = LayoutInflater.from(this.mContext).inflate(2130968766, (ViewGroup) null);
        this.mLabel = (TextView) inflate.findViewById(2131886589);
        this.mPadLock = (ImageView) inflate.findViewById(2131886590);
        addView(inflate);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public TextView getLabel() {
        return this.mLabel;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSTileBaseView
    public void handleStateChanged(QSTile.State state) {
        int i = 0;
        super.handleStateChanged(state);
        if (!Objects.equal(this.mLabel.getText(), state.label)) {
            this.mLabel.setText(state.label);
        }
        this.mLabel.setEnabled(!state.disabledByPolicy);
        ImageView imageView = this.mPadLock;
        if (!state.disabledByPolicy) {
            i = 8;
        }
        imageView.setVisibility(i);
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        updateTopPadding();
        FontSizeUtils.updateFontSize(this.mLabel, 2131689836);
    }
}
