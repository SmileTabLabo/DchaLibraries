package com.android.launcher3.backup.nano;

import com.android.launcher3.compat.PackageInstallerCompat;
import com.google.protobuf.nano.CodedInputByteBufferNano;
import com.google.protobuf.nano.CodedOutputByteBufferNano;
import com.google.protobuf.nano.MessageNano;
import com.google.protobuf.nano.WireFormatNano;
import java.io.IOException;
/* loaded from: a.zip:com/android/launcher3/backup/nano/BackupProtos$Journal.class */
public final class BackupProtos$Journal extends MessageNano {
    public int appVersion;
    public int backupVersion;
    public long bytes;
    public BackupProtos$Key[] key;
    public BackupProtos$DeviceProfieData profile;
    public int rows;
    public long t;

    public BackupProtos$Journal() {
        clear();
    }

    public BackupProtos$Journal clear() {
        this.appVersion = 0;
        this.t = 0L;
        this.bytes = 0L;
        this.rows = 0;
        this.key = BackupProtos$Key.emptyArray();
        this.backupVersion = 1;
        this.profile = null;
        this.cachedSize = -1;
        return this;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.google.protobuf.nano.MessageNano
    public int computeSerializedSize() {
        int computeSerializedSize = super.computeSerializedSize() + CodedOutputByteBufferNano.computeInt32Size(1, this.appVersion) + CodedOutputByteBufferNano.computeInt64Size(2, this.t);
        int i = computeSerializedSize;
        if (this.bytes != 0) {
            i = computeSerializedSize + CodedOutputByteBufferNano.computeInt64Size(3, this.bytes);
        }
        int i2 = i;
        if (this.rows != 0) {
            i2 = i + CodedOutputByteBufferNano.computeInt32Size(4, this.rows);
        }
        int i3 = i2;
        if (this.key != null) {
            i3 = i2;
            if (this.key.length > 0) {
                int i4 = 0;
                while (true) {
                    i3 = i2;
                    if (i4 >= this.key.length) {
                        break;
                    }
                    BackupProtos$Key backupProtos$Key = this.key[i4];
                    int i5 = i2;
                    if (backupProtos$Key != null) {
                        i5 = i2 + CodedOutputByteBufferNano.computeMessageSize(5, backupProtos$Key);
                    }
                    i4++;
                    i2 = i5;
                }
            }
        }
        int i6 = i3;
        if (this.backupVersion != 1) {
            i6 = i3 + CodedOutputByteBufferNano.computeInt32Size(6, this.backupVersion);
        }
        int i7 = i6;
        if (this.profile != null) {
            i7 = i6 + CodedOutputByteBufferNano.computeMessageSize(7, this.profile);
        }
        return i7;
    }

    @Override // com.google.protobuf.nano.MessageNano
    public BackupProtos$Journal mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
        while (true) {
            int readTag = codedInputByteBufferNano.readTag();
            switch (readTag) {
                case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                    return this;
                case 8:
                    this.appVersion = codedInputByteBufferNano.readInt32();
                    break;
                case 16:
                    this.t = codedInputByteBufferNano.readInt64();
                    break;
                case 24:
                    this.bytes = codedInputByteBufferNano.readInt64();
                    break;
                case 32:
                    this.rows = codedInputByteBufferNano.readInt32();
                    break;
                case 42:
                    int repeatedFieldArrayLength = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 42);
                    int length = this.key == null ? 0 : this.key.length;
                    BackupProtos$Key[] backupProtos$KeyArr = new BackupProtos$Key[length + repeatedFieldArrayLength];
                    int i = length;
                    if (length != 0) {
                        System.arraycopy(this.key, 0, backupProtos$KeyArr, 0, length);
                        i = length;
                    }
                    while (i < backupProtos$KeyArr.length - 1) {
                        backupProtos$KeyArr[i] = new BackupProtos$Key();
                        codedInputByteBufferNano.readMessage(backupProtos$KeyArr[i]);
                        codedInputByteBufferNano.readTag();
                        i++;
                    }
                    backupProtos$KeyArr[i] = new BackupProtos$Key();
                    codedInputByteBufferNano.readMessage(backupProtos$KeyArr[i]);
                    this.key = backupProtos$KeyArr;
                    break;
                case 48:
                    this.backupVersion = codedInputByteBufferNano.readInt32();
                    break;
                case 58:
                    if (this.profile == null) {
                        this.profile = new BackupProtos$DeviceProfieData();
                    }
                    codedInputByteBufferNano.readMessage(this.profile);
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
        codedOutputByteBufferNano.writeInt32(1, this.appVersion);
        codedOutputByteBufferNano.writeInt64(2, this.t);
        if (this.bytes != 0) {
            codedOutputByteBufferNano.writeInt64(3, this.bytes);
        }
        if (this.rows != 0) {
            codedOutputByteBufferNano.writeInt32(4, this.rows);
        }
        if (this.key != null && this.key.length > 0) {
            for (int i = 0; i < this.key.length; i++) {
                BackupProtos$Key backupProtos$Key = this.key[i];
                if (backupProtos$Key != null) {
                    codedOutputByteBufferNano.writeMessage(5, backupProtos$Key);
                }
            }
        }
        if (this.backupVersion != 1) {
            codedOutputByteBufferNano.writeInt32(6, this.backupVersion);
        }
        if (this.profile != null) {
            codedOutputByteBufferNano.writeMessage(7, this.profile);
        }
        super.writeTo(codedOutputByteBufferNano);
    }
}
