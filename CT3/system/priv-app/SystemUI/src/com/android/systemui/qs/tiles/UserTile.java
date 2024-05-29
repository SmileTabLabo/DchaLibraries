package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.BenesseExtension;
import android.util.Pair;
import com.android.systemui.qs.QSTile;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.UserSwitcherController;
/* loaded from: a.zip:com/android/systemui/qs/tiles/UserTile.class */
public class UserTile extends QSTile<QSTile.State> implements UserInfoController.OnUserInfoChangedListener {
    private Pair<String, Drawable> mLastUpdate;
    private final UserInfoController mUserInfoController;
    private final UserSwitcherController mUserSwitcherController;

    public UserTile(QSTile.Host host) {
        super(host);
        this.mUserSwitcherController = host.getUserSwitcherController();
        this.mUserInfoController = host.getUserInfoController();
    }

    @Override // com.android.systemui.qs.QSTile
    public QSTile.DetailAdapter getDetailAdapter() {
        return this.mUserSwitcherController.userDetailAdapter;
    }

    @Override // com.android.systemui.qs.QSTile
    public Intent getLongClickIntent() {
        if (BenesseExtension.getDchaState() != 0) {
            return null;
        }
        return new Intent("android.settings.USER_SETTINGS");
    }

    @Override // com.android.systemui.qs.QSTile
    public int getMetricsCategory() {
        return 260;
    }

    @Override // com.android.systemui.qs.QSTile
    public CharSequence getTileLabel() {
        return getState().label;
    }

    @Override // com.android.systemui.qs.QSTile
    protected void handleClick() {
        showDetail(true);
    }

    @Override // com.android.systemui.qs.QSTile
    protected void handleUpdateState(QSTile.State state, Object obj) {
        Pair<String, Drawable> pair = obj != null ? (Pair) obj : this.mLastUpdate;
        if (pair != null) {
            state.label = (CharSequence) pair.first;
            state.contentDescription = (CharSequence) pair.first;
            state.icon = new QSTile.Icon(this, pair) { // from class: com.android.systemui.qs.tiles.UserTile.1
                final UserTile this$0;
                final Pair val$p;

                {
                    this.this$0 = this;
                    this.val$p = pair;
                }

                @Override // com.android.systemui.qs.QSTile.Icon
                public Drawable getDrawable(Context context) {
                    return (Drawable) this.val$p.second;
                }
            };
        }
    }

    @Override // com.android.systemui.qs.QSTile
    public QSTile.State newTileState() {
        return new QSTile.State();
    }

    @Override // com.android.systemui.statusbar.policy.UserInfoController.OnUserInfoChangedListener
    public void onUserInfoChanged(String str, Drawable drawable) {
        this.mLastUpdate = new Pair<>(str, drawable);
        refreshState(this.mLastUpdate);
    }

    @Override // com.android.systemui.qs.QSTile
    public void setListening(boolean z) {
        if (z) {
            this.mUserInfoController.addListener(this);
        } else {
            this.mUserInfoController.remListener(this);
        }
    }
}
