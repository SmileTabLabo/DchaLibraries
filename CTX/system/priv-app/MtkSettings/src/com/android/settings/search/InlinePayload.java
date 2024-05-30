package com.android.settings.search;

import android.content.Intent;
import android.os.Parcel;
/* loaded from: classes.dex */
public abstract class InlinePayload extends ResultPayload {
    final int mDefaultvalue;
    final boolean mIsDeviceSupported;
    private final String mSettingKey;
    final int mSettingSource;

    public InlinePayload(String str, int i, Intent intent, boolean z, int i2) {
        super(intent);
        this.mSettingKey = str;
        this.mSettingSource = i;
        this.mIsDeviceSupported = z;
        this.mDefaultvalue = i2;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public InlinePayload(Parcel parcel) {
        super((Intent) parcel.readParcelable(Intent.class.getClassLoader()));
        this.mSettingKey = parcel.readString();
        this.mSettingSource = parcel.readInt();
        this.mIsDeviceSupported = parcel.readInt() == 1;
        this.mDefaultvalue = parcel.readInt();
    }

    @Override // com.android.settings.search.ResultPayload, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeString(this.mSettingKey);
        parcel.writeInt(this.mSettingSource);
        parcel.writeInt(this.mIsDeviceSupported ? 1 : 0);
        parcel.writeInt(this.mDefaultvalue);
    }
}
