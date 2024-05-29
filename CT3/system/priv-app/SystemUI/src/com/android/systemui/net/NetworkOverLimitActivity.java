package com.android.systemui.net;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.INetworkPolicyManager;
import android.net.NetworkTemplate;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
/* loaded from: a.zip:com/android/systemui/net/NetworkOverLimitActivity.class */
public class NetworkOverLimitActivity extends Activity {
    private static int getLimitedDialogTitleForTemplate(NetworkTemplate networkTemplate) {
        switch (networkTemplate.getMatchRule()) {
            case 1:
                return 2131493500;
            case 2:
                return 2131493498;
            case 3:
                return 2131493499;
            default:
                return 2131493501;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void snoozePolicy(NetworkTemplate networkTemplate) {
        try {
            INetworkPolicyManager.Stub.asInterface(ServiceManager.getService("netpolicy")).snoozeLimit(networkTemplate);
        } catch (RemoteException e) {
            Log.w("NetworkOverLimitActivity", "problem snoozing network policy", e);
        }
    }

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        NetworkTemplate parcelableExtra = getIntent().getParcelableExtra("android.net.NETWORK_TEMPLATE");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getLimitedDialogTitleForTemplate(parcelableExtra));
        builder.setMessage(2131493502);
        builder.setPositiveButton(17039370, (DialogInterface.OnClickListener) null);
        builder.setNegativeButton(2131493503, new DialogInterface.OnClickListener(this, parcelableExtra) { // from class: com.android.systemui.net.NetworkOverLimitActivity.1
            final NetworkOverLimitActivity this$0;
            final NetworkTemplate val$template;

            {
                this.this$0 = this;
                this.val$template = parcelableExtra;
            }

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                this.this$0.snoozePolicy(this.val$template);
            }
        });
        AlertDialog create = builder.create();
        create.getWindow().setType(2003);
        create.setOnDismissListener(new DialogInterface.OnDismissListener(this) { // from class: com.android.systemui.net.NetworkOverLimitActivity.2
            final NetworkOverLimitActivity this$0;

            {
                this.this$0 = this;
            }

            @Override // android.content.DialogInterface.OnDismissListener
            public void onDismiss(DialogInterface dialogInterface) {
                this.this$0.finish();
            }
        });
        create.show();
    }
}
