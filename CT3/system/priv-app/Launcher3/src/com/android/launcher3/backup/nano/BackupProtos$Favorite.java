package com.android.launcher3.backup.nano;

import com.android.launcher3.compat.PackageInstallerCompat;
import com.google.protobuf.nano.CodedInputByteBufferNano;
import com.google.protobuf.nano.CodedOutputByteBufferNano;
import com.google.protobuf.nano.MessageNano;
import com.google.protobuf.nano.WireFormatNano;
import java.io.IOException;
import java.util.Arrays;
/* loaded from: a.zip:com/android/launcher3/backup/nano/BackupProtos$Favorite.class */
public final class BackupProtos$Favorite extends MessageNano {
    public int appWidgetId;
    public String appWidgetProvider;
    public int cellX;
    public int cellY;
    public int container;
    public int displayMode;
    public byte[] icon;
    public String iconPackage;
    public String iconResource;
    public int iconType;
    public long id;
    public String intent;
    public int itemType;
    public int rank;
    public int screen;
    public int spanX;
    public int spanY;
    public int targetType;
    public String title;
    public String uri;

    public BackupProtos$Favorite() {
        clear();
    }

    public BackupProtos$Favorite clear() {
        this.id = 0L;
        this.itemType = 0;
        this.title = "";
        this.container = 0;
        this.screen = 0;
        this.cellX = 0;
        this.cellY = 0;
        this.spanX = 0;
        this.spanY = 0;
        this.displayMode = 0;
        this.appWidgetId = 0;
        this.appWidgetProvider = "";
        this.intent = "";
        this.uri = "";
        this.iconType = 0;
        this.iconPackage = "";
        this.iconResource = "";
        this.icon = WireFormatNano.EMPTY_BYTES;
        this.targetType = 0;
        this.rank = 0;
        this.cachedSize = -1;
        return this;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.google.protobuf.nano.MessageNano
    public int computeSerializedSize() {
        int computeSerializedSize = super.computeSerializedSize() + CodedOutputByteBufferNano.computeInt64Size(1, this.id) + CodedOutputByteBufferNano.computeInt32Size(2, this.itemType);
        int i = computeSerializedSize;
        if (!this.title.equals("")) {
            i = computeSerializedSize + CodedOutputByteBufferNano.computeStringSize(3, this.title);
        }
        int i2 = i;
        if (this.container != 0) {
            i2 = i + CodedOutputByteBufferNano.computeInt32Size(4, this.container);
        }
        int i3 = i2;
        if (this.screen != 0) {
            i3 = i2 + CodedOutputByteBufferNano.computeInt32Size(5, this.screen);
        }
        int i4 = i3;
        if (this.cellX != 0) {
            i4 = i3 + CodedOutputByteBufferNano.computeInt32Size(6, this.cellX);
        }
        int i5 = i4;
        if (this.cellY != 0) {
            i5 = i4 + CodedOutputByteBufferNano.computeInt32Size(7, this.cellY);
        }
        int i6 = i5;
        if (this.spanX != 0) {
            i6 = i5 + CodedOutputByteBufferNano.computeInt32Size(8, this.spanX);
        }
        int i7 = i6;
        if (this.spanY != 0) {
            i7 = i6 + CodedOutputByteBufferNano.computeInt32Size(9, this.spanY);
        }
        int i8 = i7;
        if (this.displayMode != 0) {
            i8 = i7 + CodedOutputByteBufferNano.computeInt32Size(10, this.displayMode);
        }
        int i9 = i8;
        if (this.appWidgetId != 0) {
            i9 = i8 + CodedOutputByteBufferNano.computeInt32Size(11, this.appWidgetId);
        }
        int i10 = i9;
        if (!this.appWidgetProvider.equals("")) {
            i10 = i9 + CodedOutputByteBufferNano.computeStringSize(12, this.appWidgetProvider);
        }
        int i11 = i10;
        if (!this.intent.equals("")) {
            i11 = i10 + CodedOutputByteBufferNano.computeStringSize(13, this.intent);
        }
        int i12 = i11;
        if (!this.uri.equals("")) {
            i12 = i11 + CodedOutputByteBufferNano.computeStringSize(14, this.uri);
        }
        int i13 = i12;
        if (this.iconType != 0) {
            i13 = i12 + CodedOutputByteBufferNano.computeInt32Size(15, this.iconType);
        }
        int i14 = i13;
        if (!this.iconPackage.equals("")) {
            i14 = i13 + CodedOutputByteBufferNano.computeStringSize(16, this.iconPackage);
        }
        int i15 = i14;
        if (!this.iconResource.equals("")) {
            i15 = i14 + CodedOutputByteBufferNano.computeStringSize(17, this.iconResource);
        }
        int i16 = i15;
        if (!Arrays.equals(this.icon, WireFormatNano.EMPTY_BYTES)) {
            i16 = i15 + CodedOutputByteBufferNano.computeBytesSize(18, this.icon);
        }
        int i17 = i16;
        if (this.targetType != 0) {
            i17 = i16 + CodedOutputByteBufferNano.computeInt32Size(19, this.targetType);
        }
        int i18 = i17;
        if (this.rank != 0) {
            i18 = i17 + CodedOutputByteBufferNano.computeInt32Size(20, this.rank);
        }
        return i18;
    }

    @Override // com.google.protobuf.nano.MessageNano
    public BackupProtos$Favorite mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
        while (true) {
            int readTag = codedInputByteBufferNano.readTag();
            switch (readTag) {
                case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                    return this;
                case 8:
                    this.id = codedInputByteBufferNano.readInt64();
                    break;
                case 16:
                    this.itemType = codedInputByteBufferNano.readInt32();
                    break;
                case 26:
                    this.title = codedInputByteBufferNano.readString();
                    break;
                case 32:
                    this.container = codedInputByteBufferNano.readInt32();
                    break;
                case 40:
                    this.screen = codedInputByteBufferNano.readInt32();
                    break;
                case 48:
                    this.cellX = codedInputByteBufferNano.readInt32();
                    break;
                case 56:
                    this.cellY = codedInputByteBufferNano.readInt32();
                    break;
                case 64:
                    this.spanX = codedInputByteBufferNano.readInt32();
                    break;
                case 72:
                    this.spanY = codedInputByteBufferNano.readInt32();
                    break;
                case 80:
                    this.displayMode = codedInputByteBufferNano.readInt32();
                    break;
                case 88:
                    this.appWidgetId = codedInputByteBufferNano.readInt32();
                    break;
                case 98:
                    this.appWidgetProvider = codedInputByteBufferNano.readString();
                    break;
                case 106:
                    this.intent = codedInputByteBufferNano.readString();
                    break;
                case 114:
                    this.uri = codedInputByteBufferNano.readString();
                    break;
                case 120:
                    this.iconType = codedInputByteBufferNano.readInt32();
                    break;
                case 130:
                    this.iconPackage = codedInputByteBufferNano.readString();
                    break;
                case 138:
                    this.iconResource = codedInputByteBufferNano.readString();
                    break;
                case 146:
                    this.icon = codedInputByteBufferNano.readBytes();
                    break;
                case 152:
                    int readInt32 = codedInputByteBufferNano.readInt32();
                    switch (readInt32) {
                        case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                        case 6:
                            this.targetType = readInt32;
                            continue;
                    }
                case 160:
                    this.rank = codedInputByteBufferNano.readInt32();
                    break;
                default:
                    if (WireFormatNano.parseUnknownField(codedInputByteBufferNano, readTag)) {
                        break;
                    } else {
                        return this;
                    }
            }
        }
    }

    @Override // com.google.protobuf.nano.MessageNano
    public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
        codedOutputByteBufferNano.writeInt64(1, this.id);
        codedOutputByteBufferNano.writeInt32(2, this.itemType);
        if (!this.title.equals("")) {
            codedOutputByteBufferNano.writeString(3, this.title);
        }
        if (this.container != 0) {
            codedOutputByteBufferNano.writeInt32(4, this.container);
        }
        if (this.screen != 0) {
            codedOutputByteBufferNano.writeInt32(5, this.screen);
        }
        if (this.cellX != 0) {
            codedOutputByteBufferNano.writeInt32(6, this.cellX);
        }
        if (this.cellY != 0) {
            codedOutputByteBufferNano.writeInt32(7, this.cellY);
        }
        if (this.spanX != 0) {
            codedOutputByteBufferNano.writeInt32(8, this.spanX);
        }
        if (this.spanY != 0) {
            codedOutputByteBufferNano.writeInt32(9, this.spanY);
        }
        if (this.displayMode != 0) {
            codedOutputByteBufferNano.writeInt32(10, this.displayMode);
        }
        if (this.appWidgetId != 0) {
            codedOutputByteBufferNano.writeInt32(11, this.appWidgetId);
        }
        if (!this.appWidgetProvider.equals("")) {
            codedOutputByteBufferNano.writeString(12, this.appWidgetProvider);
        }
        if (!this.intent.equals("")) {
            codedOutputByteBufferNano.writeString(13, this.intent);
        }
        if (!this.uri.equals("")) {
            codedOutputByteBufferNano.writeString(14, this.uri);
        }
        if (this.iconType != 0) {
            codedOutputByteBufferNano.writeInt32(15, this.iconType);
        }
        if (!this.iconPackage.equals("")) {
            codedOutputByteBufferNano.writeString(16, this.iconPackage);
        }
        if (!this.iconResource.equals("")) {
            codedOutputByteBufferNano.writeString(17, this.iconResource);
        }
        if (!Arrays.equals(this.icon, WireFormatNano.EMPTY_BYTES)) {
            codedOutputByteBufferNano.writeBytes(18, this.icon);
        }
        if (this.targetType != 0) {
            codedOutputByteBufferNano.writeInt32(19, this.targetType);
        }
        if (this.rank != 0) {
            codedOutputByteBufferNano.writeInt32(20, this.rank);
        }
        super.writeTo(codedOutputByteBufferNano);
    }
}
