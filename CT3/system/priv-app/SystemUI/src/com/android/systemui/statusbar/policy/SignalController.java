package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.SignalController.IconGroup;
import com.android.systemui.statusbar.policy.SignalController.State;
import java.io.PrintWriter;
import java.util.BitSet;
/* loaded from: a.zip:com/android/systemui/statusbar/policy/SignalController.class */
public abstract class SignalController<T extends State, I extends IconGroup> {
    private final CallbackHandler mCallbackHandler;
    protected final Context mContext;
    private int mHistoryIndex;
    protected final NetworkControllerImpl mNetworkController;
    protected final String mTag;
    protected final int mTransportType;
    protected final T mCurrentState = cleanState();
    protected final T mLastState = cleanState();
    private final State[] mHistory = new State[64];

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:com/android/systemui/statusbar/policy/SignalController$IconGroup.class */
    public static class IconGroup {
        final int[] mContentDesc;
        final int mDiscContentDesc;
        final String mName;
        final int mQsDiscState;
        final int[][] mQsIcons;
        final int mQsNullState;
        final int mSbDiscState;
        final int[][] mSbIcons;
        final int mSbNullState;

        public IconGroup(String str, int[][] iArr, int[][] iArr2, int[] iArr3, int i, int i2, int i3, int i4, int i5) {
            this.mName = str;
            this.mSbIcons = iArr;
            this.mQsIcons = iArr2;
            this.mContentDesc = iArr3;
            this.mSbNullState = i;
            this.mQsNullState = i2;
            this.mSbDiscState = i3;
            this.mQsDiscState = i4;
            this.mDiscContentDesc = i5;
        }

        public String toString() {
            return "IconGroup(" + this.mName + ")";
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:com/android/systemui/statusbar/policy/SignalController$State.class */
    public static class State {
        boolean activityIn;
        boolean activityOut;
        boolean connected;
        boolean enabled;
        IconGroup iconGroup;
        int inetCondition;
        int level;
        int rssi;
        long time;

        public void copyFrom(State state) {
            this.connected = state.connected;
            this.enabled = state.enabled;
            this.level = state.level;
            this.iconGroup = state.iconGroup;
            this.inetCondition = state.inetCondition;
            this.activityIn = state.activityIn;
            this.activityOut = state.activityOut;
            this.rssi = state.rssi;
            this.time = state.time;
        }

        public boolean equals(Object obj) {
            if (obj.getClass().equals(getClass())) {
                State state = (State) obj;
                boolean z = false;
                if (state.connected == this.connected) {
                    z = false;
                    if (state.enabled == this.enabled) {
                        z = false;
                        if (state.level == this.level) {
                            z = false;
                            if (state.inetCondition == this.inetCondition) {
                                z = false;
                                if (state.iconGroup == this.iconGroup) {
                                    z = false;
                                    if (state.activityIn == this.activityIn) {
                                        z = false;
                                        if (state.activityOut == this.activityOut) {
                                            z = false;
                                            if (state.rssi == this.rssi) {
                                                z = true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                return z;
            }
            return false;
        }

        public String toString() {
            if (this.time != 0) {
                StringBuilder sb = new StringBuilder();
                toString(sb);
                return sb.toString();
            }
            return "Empty " + getClass().getSimpleName();
        }

        /* JADX INFO: Access modifiers changed from: protected */
        public void toString(StringBuilder sb) {
            sb.append("connected=").append(this.connected).append(',').append("enabled=").append(this.enabled).append(',').append("level=").append(this.level).append(',').append("inetCondition=").append(this.inetCondition).append(',').append("iconGroup=").append(this.iconGroup).append(',').append("activityIn=").append(this.activityIn).append(',').append("activityOut=").append(this.activityOut).append(',').append("rssi=").append(this.rssi).append(',').append("lastModified=").append(DateFormat.format("MM-dd hh:mm:ss", this.time));
        }
    }

    public SignalController(String str, Context context, int i, CallbackHandler callbackHandler, NetworkControllerImpl networkControllerImpl) {
        this.mTag = "NetworkController." + str;
        this.mNetworkController = networkControllerImpl;
        this.mTransportType = i;
        this.mContext = context;
        this.mCallbackHandler = callbackHandler;
        for (int i2 = 0; i2 < 64; i2++) {
            this.mHistory[i2] = cleanState();
        }
    }

    protected abstract T cleanState();

    public void dump(PrintWriter printWriter) {
        printWriter.println("  - " + this.mTag + " -----");
        printWriter.println("  Current State: " + this.mCurrentState);
        int i = 0;
        int i2 = 0;
        while (i2 < 64) {
            int i3 = i;
            if (this.mHistory[i2].time != 0) {
                i3 = i + 1;
            }
            i2++;
            i = i3;
        }
        for (int i4 = (this.mHistoryIndex + 64) - 1; i4 >= (this.mHistoryIndex + 64) - i; i4--) {
            printWriter.println("  Previous State(" + ((this.mHistoryIndex + 64) - i4) + "): " + this.mHistory[i4 & 63]);
        }
    }

    public int getContentDescription() {
        return this.mCurrentState.connected ? getIcons().mContentDesc[this.mCurrentState.level] : getIcons().mDiscContentDesc;
    }

    public int getCurrentIconId() {
        return this.mCurrentState.connected ? getIcons().mSbIcons[this.mCurrentState.inetCondition][this.mCurrentState.level] : this.mCurrentState.enabled ? getIcons().mSbDiscState : getIcons().mSbNullState;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public I getIcons() {
        return (I) this.mCurrentState.iconGroup;
    }

    public int getQsCurrentIconId() {
        return this.mCurrentState.connected ? getIcons().mQsIcons[this.mCurrentState.inetCondition][this.mCurrentState.level] : this.mCurrentState.enabled ? getIcons().mQsDiscState : getIcons().mQsNullState;
    }

    public T getState() {
        return this.mCurrentState;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public String getStringIfExists(int i) {
        return i != 0 ? this.mContext.getString(i) : "";
    }

    public boolean isDirty() {
        if (this.mLastState.equals(this.mCurrentState)) {
            return false;
        }
        Log.d(this.mTag, "Change in state from: " + this.mLastState + "\n\tto: " + this.mCurrentState);
        return true;
    }

    public final void notifyListeners() {
        notifyListeners(this.mCallbackHandler);
    }

    public abstract void notifyListeners(NetworkController.SignalCallback signalCallback);

    public void notifyListenersIfNecessary() {
        if (isDirty()) {
            saveLastState();
            notifyListeners();
        }
    }

    protected void recordLastState() {
        State[] stateArr = this.mHistory;
        int i = this.mHistoryIndex;
        this.mHistoryIndex = i + 1;
        stateArr[i & 63].copyFrom(this.mLastState);
    }

    public void resetLastState() {
        this.mCurrentState.copyFrom(this.mLastState);
    }

    public void saveLastState() {
        recordLastState();
        this.mCurrentState.time = System.currentTimeMillis();
        this.mLastState.copyFrom(this.mCurrentState);
    }

    public void updateConnectivity(BitSet bitSet, BitSet bitSet2) {
        this.mCurrentState.inetCondition = bitSet2.get(this.mTransportType) ? 1 : 0;
        notifyListenersIfNecessary();
    }
}
