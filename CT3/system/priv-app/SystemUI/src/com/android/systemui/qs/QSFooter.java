package com.android.systemui.qs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.BenesseExtension;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.FontSizeUtils;
import com.android.systemui.statusbar.phone.QSTileHost;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import com.android.systemui.statusbar.policy.SecurityController;
/* loaded from: a.zip:com/android/systemui/qs/QSFooter.class */
public class QSFooter implements View.OnClickListener, DialogInterface.OnClickListener {
    protected static final boolean DEBUG = Log.isLoggable("QSFooter", 3);
    private final Context mContext;
    private AlertDialog mDialog;
    private final ImageView mFooterIcon;
    private final TextView mFooterText;
    private int mFooterTextId;
    private Handler mHandler;
    private QSTileHost mHost;
    private boolean mIsIconVisible;
    private boolean mIsVisible;
    private final Handler mMainHandler;
    private final View mRootView;
    private SecurityController mSecurityController;
    private final Callback mCallback = new Callback(this, null);
    private final Runnable mUpdateDisplayState = new Runnable(this) { // from class: com.android.systemui.qs.QSFooter.1
        final QSFooter this$0;

        {
            this.this$0 = this;
        }

        @Override // java.lang.Runnable
        public void run() {
            if (this.this$0.mFooterTextId != 0) {
                this.this$0.mFooterText.setText(this.this$0.mFooterTextId);
            }
            this.this$0.mRootView.setVisibility(this.this$0.mIsVisible ? 0 : 8);
            this.this$0.mFooterIcon.setVisibility(this.this$0.mIsIconVisible ? 0 : 4);
        }
    };

    /* loaded from: a.zip:com/android/systemui/qs/QSFooter$Callback.class */
    private class Callback implements SecurityController.SecurityControllerCallback {
        final QSFooter this$0;

        private Callback(QSFooter qSFooter) {
            this.this$0 = qSFooter;
        }

        /* synthetic */ Callback(QSFooter qSFooter, Callback callback) {
            this(qSFooter);
        }

        @Override // com.android.systemui.statusbar.policy.SecurityController.SecurityControllerCallback
        public void onStateChanged() {
            this.this$0.refreshState();
        }
    }

    /* loaded from: a.zip:com/android/systemui/qs/QSFooter$H.class */
    private class H extends Handler {
        final QSFooter this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        private H(QSFooter qSFooter, Looper looper) {
            super(looper);
            this.this$0 = qSFooter;
        }

        /* synthetic */ H(QSFooter qSFooter, Looper looper, H h) {
            this(qSFooter, looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            String str = null;
            try {
                if (message.what == 1) {
                    str = "handleRefreshState";
                    this.this$0.handleRefreshState();
                } else {
                    str = null;
                    if (message.what == 0) {
                        str = "handleClick";
                        this.this$0.handleClick();
                    }
                }
            } catch (Throwable th) {
                String str2 = "Error in " + str;
                Log.w("QSFooter", str2, th);
                this.this$0.mHost.warn(str2, th);
            }
        }
    }

    public QSFooter(QSPanel qSPanel, Context context) {
        this.mRootView = LayoutInflater.from(context).inflate(2130968773, (ViewGroup) qSPanel, false);
        this.mRootView.setOnClickListener(this);
        this.mFooterText = (TextView) this.mRootView.findViewById(2131886593);
        this.mFooterIcon = (ImageView) this.mRootView.findViewById(2131886594);
        this.mContext = context;
        this.mMainHandler = new Handler();
    }

    private void createDialog() {
        String deviceOwnerName = this.mSecurityController.getDeviceOwnerName();
        String profileOwnerName = this.mSecurityController.getProfileOwnerName();
        String primaryVpnName = this.mSecurityController.getPrimaryVpnName();
        String profileVpnName = this.mSecurityController.getProfileVpnName();
        boolean hasProfileOwner = this.mSecurityController.hasProfileOwner();
        this.mDialog = new SystemUIDialog(this.mContext);
        this.mDialog.setTitle(getTitle(deviceOwnerName));
        this.mDialog.setMessage(getMessage(deviceOwnerName, profileOwnerName, primaryVpnName, profileVpnName, hasProfileOwner));
        this.mDialog.setButton(-1, getPositiveButton(), this);
        if (this.mSecurityController.isVpnEnabled() && !this.mSecurityController.isVpnRestricted()) {
            this.mDialog.setButton(-2, getSettingsButton(), this);
        }
        this.mDialog.show();
    }

    private String getMessage(String str, String str2, String str3, String str4, boolean z) {
        if (str != null) {
            return str3 != null ? this.mContext.getString(2131493674, str, str3) : this.mContext.getString(2131493665, str);
        } else if (str3 != null) {
            return str4 != null ? this.mContext.getString(2131493673, str2, str4, str3) : this.mContext.getString(2131493671, str3);
        } else if (str4 != null) {
            return this.mContext.getString(2131493672, str2, str4);
        } else {
            if (str2 == null || !z) {
                return null;
            }
            return this.mContext.getString(2131493665, str2);
        }
    }

    private String getPositiveButton() {
        return this.mContext.getString(2131493564);
    }

    private String getSettingsButton() {
        return this.mContext.getString(2131493308);
    }

    private int getTitle(String str) {
        return str != null ? 2131493660 : 2131493662;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleClick() {
        this.mHost.collapsePanels();
        createDialog();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleRefreshState() {
        this.mIsIconVisible = this.mSecurityController.isVpnEnabled();
        if (this.mSecurityController.isDeviceManaged()) {
            this.mFooterTextId = 2131493657;
            this.mIsVisible = true;
        } else {
            this.mFooterTextId = 2131493659;
            this.mIsVisible = this.mIsIconVisible;
        }
        this.mMainHandler.post(this.mUpdateDisplayState);
    }

    public View getView() {
        return this.mRootView;
    }

    @Override // android.content.DialogInterface.OnClickListener
    public void onClick(DialogInterface dialogInterface, int i) {
        if (i == -2 && BenesseExtension.getDchaState() == 0) {
            this.mHost.startActivityDismissingKeyguard(new Intent("android.settings.VPN_SETTINGS"));
        }
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        this.mHandler.sendEmptyMessage(0);
    }

    public void onConfigurationChanged() {
        FontSizeUtils.updateFontSize(this.mFooterText, 2131689836);
    }

    public void refreshState() {
        this.mHandler.sendEmptyMessage(1);
    }

    public void setHost(QSTileHost qSTileHost) {
        this.mHost = qSTileHost;
        this.mSecurityController = qSTileHost.getSecurityController();
        this.mHandler = new H(this, qSTileHost.getLooper(), null);
    }

    public void setListening(boolean z) {
        if (z) {
            this.mSecurityController.addCallback(this.mCallback);
        } else {
            this.mSecurityController.removeCallback(this.mCallback);
        }
    }
}
