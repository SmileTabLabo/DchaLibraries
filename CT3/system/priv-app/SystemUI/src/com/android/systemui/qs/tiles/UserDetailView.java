package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.logging.MetricsLogger;
import com.android.settingslib.RestrictedLockUtils;
import com.android.systemui.qs.PseudoGridView;
import com.android.systemui.statusbar.policy.UserSwitcherController;
/* loaded from: a.zip:com/android/systemui/qs/tiles/UserDetailView.class */
public class UserDetailView extends PseudoGridView {
    private Adapter mAdapter;

    /* loaded from: a.zip:com/android/systemui/qs/tiles/UserDetailView$Adapter.class */
    public static class Adapter extends UserSwitcherController.BaseUserAdapter implements View.OnClickListener {
        private final Context mContext;
        private final UserSwitcherController mController;

        public Adapter(Context context, UserSwitcherController userSwitcherController) {
            super(userSwitcherController);
            this.mContext = context;
            this.mController = userSwitcherController;
        }

        @Override // android.widget.Adapter
        public View getView(int i, View view, ViewGroup viewGroup) {
            UserSwitcherController.UserRecord item = getItem(i);
            UserDetailItemView convertOrInflate = UserDetailItemView.convertOrInflate(this.mContext, view, viewGroup);
            if (convertOrInflate != view) {
                convertOrInflate.setOnClickListener(this);
            }
            String name = getName(this.mContext, item);
            if (item.picture == null) {
                convertOrInflate.bind(name, getDrawable(this.mContext, item), item.resolveId());
            } else {
                convertOrInflate.bind(name, item.picture, item.info.id);
            }
            convertOrInflate.setActivated(item.isCurrent);
            convertOrInflate.setDisabledByAdmin(item.isDisabledByAdmin);
            if (!item.isSwitchToEnabled) {
                convertOrInflate.setEnabled(false);
            }
            convertOrInflate.setTag(item);
            return convertOrInflate;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            UserSwitcherController.UserRecord userRecord = (UserSwitcherController.UserRecord) view.getTag();
            if (userRecord.isDisabledByAdmin) {
                Intent showAdminSupportDetailsIntent = RestrictedLockUtils.getShowAdminSupportDetailsIntent(this.mContext, userRecord.enforcedAdmin);
                if (showAdminSupportDetailsIntent != null) {
                    this.mController.startActivity(showAdminSupportDetailsIntent);
                }
            } else if (userRecord.isSwitchToEnabled) {
                MetricsLogger.action(this.mContext, 156);
                switchTo(userRecord);
            }
        }
    }

    public UserDetailView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public static UserDetailView inflate(Context context, ViewGroup viewGroup, boolean z) {
        return (UserDetailView) LayoutInflater.from(context).inflate(2130968768, viewGroup, z);
    }

    public void createAndSetAdapter(UserSwitcherController userSwitcherController) {
        this.mAdapter = new Adapter(this.mContext, userSwitcherController);
        PseudoGridView.ViewGroupAdapterBridge.link(this, this.mAdapter);
    }

    public void refreshAdapter() {
        this.mAdapter.refresh();
    }
}
