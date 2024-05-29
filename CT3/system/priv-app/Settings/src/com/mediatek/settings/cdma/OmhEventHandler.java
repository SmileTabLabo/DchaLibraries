package com.mediatek.settings.cdma;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
/* loaded from: classes.dex */
public class OmhEventHandler extends Handler {
    private static OmhEventHandler mHandler;
    private Context mContext;
    private int mState;

    private OmhEventHandler(Context context) {
        super(context.getMainLooper());
        this.mState = 0;
        this.mContext = context;
    }

    private static synchronized void createInstance(Context context) {
        synchronized (OmhEventHandler.class) {
            if (mHandler == null) {
                mHandler = new OmhEventHandler(context);
            }
        }
    }

    public static OmhEventHandler getInstance(Context context) {
        if (mHandler == null) {
            createInstance(context);
        }
        return mHandler;
    }

    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        Log.d("OmhEventHandler", "handleMessage, msg = " + msg + ", while state = " + this.mState);
        switch (msg.what) {
            case 100:
                if (this.mState == 0) {
                    this.mState = 1;
                    return;
                } else {
                    Log.w("OmhEventHandler", "SET_BUSY when state = " + this.mState);
                    return;
                }
            case 101:
                if (this.mState == 0) {
                    if (msg.arg1 == 1000) {
                        this.mState = 3;
                        CdmaUtils.startOmhWarningDialog(this.mContext);
                        return;
                    } else if (msg.arg1 != 1001) {
                        return;
                    } else {
                        CdmaUtils.startOmhDataPickDialog(this.mContext, msg.arg2);
                        return;
                    }
                } else if (this.mState == 1) {
                    if (msg.arg1 != 1000) {
                        return;
                    }
                    this.mState = 2;
                    return;
                } else {
                    Log.w("OmhEventHandler", "NEW_REQUEST when state = " + this.mState);
                    return;
                }
            case 102:
                if (this.mState == 1) {
                    this.mState = 0;
                    return;
                } else if (this.mState == 2) {
                    this.mState = 3;
                    CdmaUtils.startOmhWarningDialog(this.mContext);
                    return;
                } else {
                    Log.w("OmhEventHandler", "CLEAR_BUSY when state = " + this.mState);
                    return;
                }
            case 103:
                if (this.mState == 3) {
                    this.mState = 0;
                    return;
                }
                Log.w("OmhEventHandler", "FINISH_REQUEST when state = " + this.mState);
                this.mState = 0;
                return;
            default:
                return;
        }
    }
}
