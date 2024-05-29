package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.os.UserManager;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.FrameLayout;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.statusbar.policy.KeyguardUserSwitcher;
import com.android.systemui.statusbar.policy.UserSwitcherController;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/MultiUserSwitch.class */
public class MultiUserSwitch extends FrameLayout implements View.OnClickListener {
    private boolean mKeyguardMode;
    private KeyguardUserSwitcher mKeyguardUserSwitcher;
    private QSPanel mQsPanel;
    private final int[] mTmpInt2;
    private UserSwitcherController.BaseUserAdapter mUserListener;
    final UserManager mUserManager;
    private UserSwitcherController mUserSwitcherController;

    public MultiUserSwitch(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mTmpInt2 = new int[2];
        this.mUserManager = UserManager.get(getContext());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void refreshContentDescription() {
        String str = null;
        if (this.mUserManager.isUserSwitcherEnabled()) {
            str = null;
            if (this.mUserSwitcherController != null) {
                str = this.mUserSwitcherController.getCurrentUserName(this.mContext);
            }
        }
        String str2 = null;
        if (!TextUtils.isEmpty(str)) {
            str2 = this.mContext.getString(2131493449, str);
        }
        if (TextUtils.equals(getContentDescription(), str2)) {
            return;
        }
        setContentDescription(str2);
    }

    private void registerListener() {
        UserSwitcherController userSwitcherController;
        if (this.mUserManager.isUserSwitcherEnabled() && this.mUserListener == null && (userSwitcherController = this.mUserSwitcherController) != null) {
            this.mUserListener = new UserSwitcherController.BaseUserAdapter(this, userSwitcherController) { // from class: com.android.systemui.statusbar.phone.MultiUserSwitch.1
                final MultiUserSwitch this$0;

                {
                    this.this$0 = this;
                }

                @Override // android.widget.Adapter
                public View getView(int i, View view, ViewGroup viewGroup) {
                    return null;
                }

                @Override // android.widget.BaseAdapter
                public void notifyDataSetChanged() {
                    this.this$0.refreshContentDescription();
                }
            };
            refreshContentDescription();
        }
    }

    public boolean hasMultipleUsers() {
        boolean z = false;
        if (this.mUserListener == null) {
            return false;
        }
        if (this.mUserListener.getCount() != 0) {
            z = true;
        }
        return z;
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (!this.mUserManager.isUserSwitcherEnabled()) {
            if (this.mQsPanel != null) {
                this.mQsPanel.getHost().startActivityDismissingKeyguard(ContactsContract.QuickContact.composeQuickContactsIntent(getContext(), view, ContactsContract.Profile.CONTENT_URI, 3, null));
            }
        } else if (this.mKeyguardMode) {
            if (this.mKeyguardUserSwitcher != null) {
                this.mKeyguardUserSwitcher.show(true);
            }
        } else if (this.mQsPanel == null || this.mUserSwitcherController == null) {
        } else {
            View childAt = getChildCount() > 0 ? getChildAt(0) : this;
            childAt.getLocationInWindow(this.mTmpInt2);
            int[] iArr = this.mTmpInt2;
            iArr[0] = iArr[0] + (childAt.getWidth() / 2);
            int[] iArr2 = this.mTmpInt2;
            iArr2[1] = iArr2[1] + (childAt.getHeight() / 2);
            this.mQsPanel.showDetailAdapter(true, this.mUserSwitcherController.userDetailAdapter, this.mTmpInt2);
        }
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        setOnClickListener(this);
        refreshContentDescription();
    }

    @Override // android.view.View
    public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onInitializeAccessibilityEvent(accessibilityEvent);
        accessibilityEvent.setClassName(Button.class.getName());
    }

    @Override // android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        accessibilityNodeInfo.setClassName(Button.class.getName());
    }

    @Override // android.view.View
    public void setClickable(boolean z) {
        super.setClickable(z);
        refreshContentDescription();
    }

    public void setKeyguardMode(boolean z) {
        this.mKeyguardMode = z;
        registerListener();
    }

    public void setKeyguardUserSwitcher(KeyguardUserSwitcher keyguardUserSwitcher) {
        this.mKeyguardUserSwitcher = keyguardUserSwitcher;
    }

    public void setQsPanel(QSPanel qSPanel) {
        this.mQsPanel = qSPanel;
        setUserSwitcherController(qSPanel.getHost().getUserSwitcherController());
    }

    public void setUserSwitcherController(UserSwitcherController userSwitcherController) {
        this.mUserSwitcherController = userSwitcherController;
        registerListener();
        refreshContentDescription();
    }
}
