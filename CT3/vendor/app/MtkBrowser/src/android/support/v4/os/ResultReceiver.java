package android.support.v4.os;

import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.os.IResultReceiver;
/* loaded from: b.zip:android/support/v4/os/ResultReceiver.class */
public class ResultReceiver implements Parcelable {
    public static final Parcelable.Creator<ResultReceiver> CREATOR = new Parcelable.Creator<ResultReceiver>() { // from class: android.support.v4.os.ResultReceiver.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public ResultReceiver createFromParcel(Parcel parcel) {
            return new ResultReceiver(parcel);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public ResultReceiver[] newArray(int i) {
            return new ResultReceiver[i];
        }
    };
    IResultReceiver mReceiver;
    final boolean mLocal = false;
    final Handler mHandler = null;

    /* loaded from: b.zip:android/support/v4/os/ResultReceiver$MyResultReceiver.class */
    class MyResultReceiver extends IResultReceiver.Stub {
        final ResultReceiver this$0;

        MyResultReceiver(ResultReceiver resultReceiver) {
            this.this$0 = resultReceiver;
        }

        @Override // android.support.v4.os.IResultReceiver
        public void send(int i, Bundle bundle) {
            if (this.this$0.mHandler != null) {
                this.this$0.mHandler.post(new MyRunnable(this.this$0, i, bundle));
            } else {
                this.this$0.onReceiveResult(i, bundle);
            }
        }
    }

    /* loaded from: b.zip:android/support/v4/os/ResultReceiver$MyRunnable.class */
    class MyRunnable implements Runnable {
        final int mResultCode;
        final Bundle mResultData;
        final ResultReceiver this$0;

        MyRunnable(ResultReceiver resultReceiver, int i, Bundle bundle) {
            this.this$0 = resultReceiver;
            this.mResultCode = i;
            this.mResultData = bundle;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.this$0.onReceiveResult(this.mResultCode, this.mResultData);
        }
    }

    ResultReceiver(Parcel parcel) {
        this.mReceiver = IResultReceiver.Stub.asInterface(parcel.readStrongBinder());
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    protected void onReceiveResult(int i, Bundle bundle) {
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        synchronized (this) {
            if (this.mReceiver == null) {
                this.mReceiver = new MyResultReceiver(this);
            }
            parcel.writeStrongBinder(this.mReceiver.asBinder());
        }
    }
}
