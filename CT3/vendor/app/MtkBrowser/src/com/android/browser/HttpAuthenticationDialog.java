package com.android.browser;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
/* loaded from: b.zip:com/android/browser/HttpAuthenticationDialog.class */
public class HttpAuthenticationDialog {
    private CancelListener mCancelListener;
    private final Context mContext;
    private AlertDialog mDialog;
    private final String mHost;
    private OkListener mOkListener;
    private TextView mPasswordView;
    private final String mRealm;
    private TextView mUsernameView;

    /* loaded from: b.zip:com/android/browser/HttpAuthenticationDialog$CancelListener.class */
    public interface CancelListener {
        void onCancel();
    }

    /* loaded from: b.zip:com/android/browser/HttpAuthenticationDialog$OkListener.class */
    public interface OkListener {
        void onOk(String str, String str2, String str3, String str4);
    }

    public HttpAuthenticationDialog(Context context, String str, String str2) {
        this.mContext = context;
        this.mHost = str;
        this.mRealm = str2;
        createDialog();
    }

    private void createDialog() {
        View inflate = LayoutInflater.from(this.mContext).inflate(2130968605, (ViewGroup) null);
        this.mUsernameView = (TextView) inflate.findViewById(2131558488);
        this.mPasswordView = (TextView) inflate.findViewById(2131558489);
        this.mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener(this) { // from class: com.android.browser.HttpAuthenticationDialog.1
            final HttpAuthenticationDialog this$0;

            {
                this.this$0 = this;
            }

            @Override // android.widget.TextView.OnEditorActionListener
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == 6) {
                    this.this$0.mDialog.getButton(-1).performClick();
                    return true;
                }
                return false;
            }
        });
        this.mDialog = new AlertDialog.Builder(this.mContext).setTitle(this.mContext.getText(2131492956).toString().replace("%s1", this.mHost).replace("%s2", this.mRealm)).setIconAttribute(16843605).setView(inflate).setPositiveButton(2131492959, new DialogInterface.OnClickListener(this) { // from class: com.android.browser.HttpAuthenticationDialog.2
            final HttpAuthenticationDialog this$0;

            {
                this.this$0 = this;
            }

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                if (this.this$0.mOkListener != null) {
                    this.this$0.mOkListener.onOk(this.this$0.mHost, this.this$0.mRealm, this.this$0.getUsername(), this.this$0.getPassword());
                }
            }
        }).setNegativeButton(2131492962, new DialogInterface.OnClickListener(this) { // from class: com.android.browser.HttpAuthenticationDialog.3
            final HttpAuthenticationDialog this$0;

            {
                this.this$0 = this;
            }

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                if (this.this$0.mCancelListener != null) {
                    this.this$0.mCancelListener.onCancel();
                }
            }
        }).setOnCancelListener(new DialogInterface.OnCancelListener(this) { // from class: com.android.browser.HttpAuthenticationDialog.4
            final HttpAuthenticationDialog this$0;

            {
                this.this$0 = this;
            }

            @Override // android.content.DialogInterface.OnCancelListener
            public void onCancel(DialogInterface dialogInterface) {
                if (this.this$0.mCancelListener != null) {
                    this.this$0.mCancelListener.onCancel();
                }
            }
        }).create();
        this.mDialog.getWindow().setSoftInputMode(4);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String getPassword() {
        return this.mPasswordView.getText().toString();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String getUsername() {
        return this.mUsernameView.getText().toString();
    }

    public void reshow() {
        String username = getUsername();
        String password = getPassword();
        int id = this.mDialog.getCurrentFocus().getId();
        this.mDialog.dismiss();
        createDialog();
        this.mDialog.show();
        if (username != null) {
            this.mUsernameView.setText(username);
        }
        if (password != null) {
            this.mPasswordView.setText(password);
        }
        if (id != 0) {
            this.mDialog.findViewById(id).requestFocus();
        } else {
            this.mUsernameView.requestFocus();
        }
    }

    public void setCancelListener(CancelListener cancelListener) {
        this.mCancelListener = cancelListener;
    }

    public void setOkListener(OkListener okListener) {
        this.mOkListener = okListener;
    }

    public void show() {
        this.mDialog.show();
        this.mUsernameView.requestFocus();
    }
}
