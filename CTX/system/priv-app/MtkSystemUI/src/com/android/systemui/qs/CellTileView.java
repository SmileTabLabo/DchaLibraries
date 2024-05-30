package com.android.systemui.qs;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import com.android.settingslib.graph.SignalDrawable;
import com.android.systemui.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import java.util.Objects;
/* loaded from: classes.dex */
public class CellTileView extends SignalTileView {
    private final SignalDrawable mSignalDrawable;

    public CellTileView(Context context) {
        super(context);
        this.mSignalDrawable = new SignalDrawable(this.mContext);
        this.mSignalDrawable.setColors(QSTileImpl.getColorForState(context, 0), QSTileImpl.getColorForState(context, 2));
        this.mSignalDrawable.setIntrinsicSize(context.getResources().getDimensionPixelSize(R.dimen.qs_tile_icon_size));
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSIconViewImpl
    public void updateIcon(ImageView imageView, QSTile.State state) {
        if (!(state.icon instanceof SignalIcon)) {
            super.updateIcon(imageView, state);
        } else if (!Objects.equals(state.icon, imageView.getTag(R.id.qs_icon_tag))) {
            this.mSignalDrawable.setLevel(((SignalIcon) state.icon).getState());
            imageView.setImageDrawable(this.mSignalDrawable);
            imageView.setTag(R.id.qs_icon_tag, state.icon);
        }
    }

    /* loaded from: classes.dex */
    public static class SignalIcon extends QSTile.Icon {
        private final int mState;

        public int getState() {
            return this.mState;
        }

        @Override // com.android.systemui.plugins.qs.QSTile.Icon
        public Drawable getDrawable(Context context) {
            SignalDrawable signalDrawable = new SignalDrawable(context);
            signalDrawable.setColors(QSTileImpl.getColorForState(context, 0), QSTileImpl.getColorForState(context, 2));
            signalDrawable.setLevel(getState());
            return signalDrawable;
        }
    }
}
