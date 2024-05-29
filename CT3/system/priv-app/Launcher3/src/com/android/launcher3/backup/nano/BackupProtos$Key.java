package com.android.launcher3.backup.nano;

import com.android.launcher3.compat.PackageInstallerCompat;
import com.google.protobuf.nano.CodedInputByteBufferNano;
import com.google.protobuf.nano.CodedOutputByteBufferNano;
import com.google.protobuf.nano.InternalNano;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.protobuf.nano.MessageNano;
import com.google.protobuf.nano.WireFormatNano;
import java.io.IOException;
/* loaded from: a.zip:com/android/launcher3/backup/nano/BackupProtos$Key.class */
public final class BackupProtos$Key extends MessageNano {
    private static volatile BackupProtos$Key[] _emptyArray;
    public long checksum;
    public long id;
    public String name;
    public int type;

    public BackupProtos$Key() {
        clear();
    }

    public static BackupProtos$Key[] emptyArray() {
        if (_emptyArray == null) {
            synchronized (InternalNano.LAZY_INIT_LOCK) {
                if (_emptyArray == null) {
                    _emptyArray = new BackupProtos$Key[0];
                }
            }
        }
        return _emptyArray;
    }

    public static BackupProtos$Key parseFrom(byte[] bArr) throws InvalidProtocolBufferNanoException {
        return (BackupProtos$Key) MessageNano.mergeFrom(new BackupProtos$Key(), bArr);
    }

    public BackupProtos$Key clear() {
        this.type = 1;
        this.name = "";
        this.id = 0L;
        this.checksum = 0L;
        this.cachedSize = -1;
        return this;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.google.protobuf.nano.MessageNano
    public int computeSerializedSize() {
        int computeSerializedSize = super.computeSerializedSize() + CodedOutputByteBufferNano.computeInt32Size(1, this.type);
        int i = computeSerializedSize;
        if (!this.name.equals("")) {
            i = computeSerializedSize + CodedOutputByteBufferNano.computeStringSize(2, this.name);
        }
        int i2 = i;
        if (this.id != 0) {
            i2 = i + CodedOutputByteBufferNano.computeInt64Size(3, this.id);
        }
        int i3 = i2;
        if (this.checksum != 0) {
            i3 = i2 + CodedOutputByteBufferNano.computeInt64Size(4, this.checksum);
        }
        return i3;
    }

    @Override // com.google.protobuf.nano.MessageNano
    public BackupProtos$Key mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
        while (true) {
            int readTag = codedInputByteBufferNano.readTag();
            switch (readTag) {
                case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                    return this;
                case 8:
                    int readInt32 = codedInputByteBufferNano.readInt32();
                    switch (readInt32) {
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                            this.type = readInt32;
                            continue;
                    }
                case 18:
                    this.name = codedInputByteBufferNano.readString();
                    break;
                case 24:
                    this.id = codedInputByteBufferNano.readInt64();
                    break;
                case 32:
                    this.checksum = codedInputByteBufferNano.readInt64();
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
        codedOutputByteBufferNano.writeInt32(1, this.type);
        if (!this.name.equals("")) {
            codedOutputByteBufferNano.writeString(2, this.name);
        }
        if (this.id != 0) {
            codedOutputByteBufferNano.writeInt64(3, this.id);
        }
        if (this.checksum != 0) {
            codedOutputByteBufferNano.writeInt64(4, this.checksum);
        }
        super.writeTo(codedOutputByteBufferNano);
    }
}
