package com.android.keyguard;
/* loaded from: a.zip:com/android/keyguard/SecurityMessageDisplay.class */
public interface SecurityMessageDisplay {
    void setMessage(int i, boolean z);

    void setMessage(int i, boolean z, Object... objArr);

    void setMessage(CharSequence charSequence, boolean z);

    void setNextMessageColor(int i);

    void setTimeout(int i);
}
