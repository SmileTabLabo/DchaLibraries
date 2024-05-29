package android.support.v4.app;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.RemoteInputCompatBase$RemoteInput;
/* loaded from: classes.dex */
public final class RemoteInput extends RemoteInputCompatBase$RemoteInput {
    public static final RemoteInputCompatBase$RemoteInput.Factory FACTORY;
    private static final Impl IMPL;
    private final boolean mAllowFreeFormInput;
    private final CharSequence[] mChoices;
    private final Bundle mExtras;
    private final CharSequence mLabel;
    private final String mResultKey;

    /* loaded from: classes.dex */
    interface Impl {
    }

    @Override // android.support.v4.app.RemoteInputCompatBase$RemoteInput
    public String getResultKey() {
        return this.mResultKey;
    }

    @Override // android.support.v4.app.RemoteInputCompatBase$RemoteInput
    public CharSequence getLabel() {
        return this.mLabel;
    }

    @Override // android.support.v4.app.RemoteInputCompatBase$RemoteInput
    public CharSequence[] getChoices() {
        return this.mChoices;
    }

    @Override // android.support.v4.app.RemoteInputCompatBase$RemoteInput
    public boolean getAllowFreeFormInput() {
        return this.mAllowFreeFormInput;
    }

    @Override // android.support.v4.app.RemoteInputCompatBase$RemoteInput
    public Bundle getExtras() {
        return this.mExtras;
    }

    /* loaded from: classes.dex */
    static class ImplBase implements Impl {
        ImplBase() {
        }
    }

    /* loaded from: classes.dex */
    static class ImplJellybean implements Impl {
        ImplJellybean() {
        }
    }

    /* loaded from: classes.dex */
    static class ImplApi20 implements Impl {
        ImplApi20() {
        }
    }

    static {
        if (Build.VERSION.SDK_INT >= 20) {
            IMPL = new ImplApi20();
        } else if (Build.VERSION.SDK_INT >= 16) {
            IMPL = new ImplJellybean();
        } else {
            IMPL = new ImplBase();
        }
        FACTORY = new RemoteInputCompatBase$RemoteInput.Factory() { // from class: android.support.v4.app.RemoteInput.1
        };
    }
}
