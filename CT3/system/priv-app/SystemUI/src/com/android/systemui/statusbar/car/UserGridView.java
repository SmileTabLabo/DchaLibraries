package com.android.systemui.statusbar.car;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import com.android.systemui.statusbar.UserUtil;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.statusbar.policy.UserSwitcherController;
/* loaded from: a.zip:com/android/systemui/statusbar/car/UserGridView.class */
public class UserGridView extends GridView {
    private Adapter mAdapter;
    private int mPendingUserId;
    private PhoneStatusBar mStatusBar;
    private UserSwitcherController mUserSwitcherController;

    /* loaded from: a.zip:com/android/systemui/statusbar/car/UserGridView$Adapter.class */
    private final class Adapter extends UserSwitcherController.BaseUserAdapter {
        final UserGridView this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public Adapter(UserGridView userGridView, UserSwitcherController userSwitcherController) {
            super(userSwitcherController);
            this.this$0 = userGridView;
        }

        @Override // android.widget.Adapter
        public View getView(int i, View view, ViewGroup viewGroup) {
            View view2 = view;
            if (view == null) {
                view2 = ((LayoutInflater) this.this$0.getContext().getSystemService("layout_inflater")).inflate(2130968607, (ViewGroup) null);
            }
            UserSwitcherController.UserRecord item = getItem(i);
            TextView textView = (TextView) view2.findViewById(2131886264);
            if (item != null) {
                textView.setText(getName(this.this$0.getContext(), item));
                view2.setActivated(item.isCurrent);
            } else {
                textView.setText("Unknown");
            }
            ImageView imageView = (ImageView) view2.findViewById(2131886263);
            if (item == null || item.picture == null) {
                imageView.setImageDrawable(getDrawable(this.this$0.getContext(), item));
            } else {
                imageView.setImageBitmap(item.picture);
            }
            return view2;
        }
    }

    public UserGridView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mPendingUserId = -10000;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showOfflineAuthUi() {
        this.mStatusBar.executeRunnableDismissingKeyguard(null, null, true, true, true);
    }

    public void init(PhoneStatusBar phoneStatusBar, UserSwitcherController userSwitcherController) {
        this.mStatusBar = phoneStatusBar;
        this.mUserSwitcherController = userSwitcherController;
        this.mAdapter = new Adapter(this, this.mUserSwitcherController);
        setAdapter((ListAdapter) this.mAdapter);
        setOnItemClickListener(new AdapterView.OnItemClickListener(this) { // from class: com.android.systemui.statusbar.car.UserGridView.1
            final UserGridView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.widget.AdapterView.OnItemClickListener
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
                this.this$0.mPendingUserId = -10000;
                UserSwitcherController.UserRecord item = this.this$0.mAdapter.getItem(i);
                if (item == null) {
                    return;
                }
                if (item.isGuest || item.isAddUser) {
                    this.this$0.mUserSwitcherController.switchTo(item);
                } else if (item.isCurrent) {
                    this.this$0.showOfflineAuthUi();
                } else {
                    this.this$0.mPendingUserId = item.info.id;
                    this.this$0.mUserSwitcherController.switchTo(item);
                }
            }
        });
        setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(this) { // from class: com.android.systemui.statusbar.car.UserGridView.2
            final UserGridView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.widget.AdapterView.OnItemLongClickListener
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long j) {
                UserSwitcherController.UserRecord item = this.this$0.mAdapter.getItem(i);
                if (item == null || item.isAddUser) {
                    return false;
                }
                if (!item.isGuest) {
                    UserUtil.deleteUserWithPrompt(this.this$0.getContext(), item.info.id, this.this$0.mUserSwitcherController);
                    return true;
                } else if (item.isCurrent) {
                    this.this$0.mUserSwitcherController.switchTo(item);
                    return true;
                } else {
                    return true;
                }
            }
        });
    }

    @Override // android.widget.GridView, android.widget.AbsListView, android.view.View
    protected void onMeasure(int i, int i2) {
        int mode = View.MeasureSpec.getMode(i);
        int size = View.MeasureSpec.getSize(i);
        if (mode == 0) {
            setNumColumns(-1);
        } else {
            setNumColumns(Math.max(1, Math.min(getAdapter() == null ? 0 : getAdapter().getCount(), size / Math.max(1, getRequestedColumnWidth()))));
        }
        super.onMeasure(i, i2);
    }

    public void onUserSwitched(int i) {
        if (this.mPendingUserId == i) {
            post(new Runnable(this) { // from class: com.android.systemui.statusbar.car.UserGridView.3
                final UserGridView this$0;

                {
                    this.this$0 = this;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.this$0.showOfflineAuthUi();
                }
            });
        }
        this.mPendingUserId = -10000;
    }
}
