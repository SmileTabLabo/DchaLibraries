package com.android.launcher3.backup.nano;

import com.android.launcher3.compat.PackageInstallerCompat;
import com.google.protobuf.nano.CodedInputByteBufferNano;
import com.google.protobuf.nano.CodedOutputByteBufferNano;
import com.google.protobuf.nano.MessageNano;
import com.google.protobuf.nano.WireFormatNano;
import java.io.IOException;
/* loaded from: a.zip:com/android/launcher3/backup/nano/BackupProtos$Widget.class */
public final class BackupProtos$Widget extends MessageNano {
    public boolean configure;
    public BackupProtos$Resource icon;
    public String label;
    public int minSpanX;
    public int minSpanY;
    public BackupProtos$Resource preview;
    public String provider;

    public BackupProtos$Widget() {
        clear();
    }

    public BackupProtos$Widget clear() {
        this.provider = "";
        this.label = "";
        this.configure = false;
        this.icon = null;
        this.preview = null;
        this.minSpanX = 2;
        this.minSpanY = 2;
        this.cachedSize = -1;
        return this;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.google.protobuf.nano.MessageNano
    public int computeSerializedSize() {
        int computeSerializedSize = super.computeSerializedSize() + CodedOutputByteBufferNano.computeStringSize(1, this.provider);
        int i = computeSerializedSize;
        if (!this.label.equals("")) {
            i = computeSerializedSize + CodedOutputByteBufferNano.computeStringSize(2, this.label);
        }
        int i2 = i;
        if (this.configure) {
            i2 = i + CodedOutputByteBufferNano.computeBoolSize(3, this.configure);
        }
        int i3 = i2;
        if (this.icon != null) {
            i3 = i2 + CodedOutputByteBufferNano.computeMessageSize(4, this.icon);
        }
        int i4 = i3;
        if (this.preview != null) {
            i4 = i3 + CodedOutputByteBufferNano.computeMessageSize(5, this.preview);
        }
        int i5 = i4;
        if (this.minSpanX != 2) {
            i5 = i4 + CodedOutputByteBufferNano.computeInt32Size(6, this.minSpanX);
        }
        int i6 = i5;
        if (this.minSpanY != 2) {
            i6 = i5 + CodedOutputByteBufferNano.computeInt32Size(7, this.minSpanY);
        }
        return i6;
    }

    @Override // com.google.protobuf.nano.MessageNano
    public BackupProtos$Widget mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
        while (true) {
            int readTag = codedInputByteBufferNano.readTag();
            switch (readTag) {
                case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                    return this;
                case 10:
                    this.provider = codedInputByteBufferNano.readString();
                    break;
                case 18:
                    this.label = codedInputByteBufferNano.readString();
                    break;
                case 24:
                    this.configure = codedInputByteBufferNano.readBool();
                    break;
                case 34:
                    if (this.icon == null) {
                        this.icon = new BackupProtos$Resource();
                    }
                    codedInputByteBufferNano.readMessage(this.icon);
                    break;
                case 42:
                    if (this.preview == null) {
                        this.preview = new BackupProtos$Resource();
                    }
                    codedInputByteBufferNano.readMessage(this.preview);
                    break;
                case 48:
                    this.minSpanX = codedInputByteBufferNano.readInt32();
                    break;
                case 56:
                    this.minSpanY = codedInputByteBufferNano.readInt32();
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
        codedOutputByteBufferNano.writeString(1, this.provider);
        if (!this.label.equals("")) {
            codedOutputByteBufferNano.writeString(2, this.label);
        }
        if (this.configure) {
            codedOutputByteBufferNano.writeBool(3, this.configure);
        }
        if (this.icon != null) {
            codedOutputByteBufferNano.writeMessage(4, this.icon);
        }
        if (this.preview != null) {
            codedOutputByteBufferNano.writeMessage(5, this.preview);
        }
        if (this.minSpanX != 2) {
            codedOutputByteBufferNano.writeInt32(6, this.minSpanX);
        }
        if (this.minSpanY != 2) {
            codedOutputByteBufferNano.writeInt32(7, this.minSpanY);
        }
        super.writeTo(codedOutputByteBufferNano);
    }
}
