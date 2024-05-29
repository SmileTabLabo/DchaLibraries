package com.android.launcher3.backup.nano;

import com.android.launcher3.compat.PackageInstallerCompat;
import com.google.protobuf.nano.CodedInputByteBufferNano;
import com.google.protobuf.nano.CodedOutputByteBufferNano;
import com.google.protobuf.nano.MessageNano;
import com.google.protobuf.nano.WireFormatNano;
import java.io.IOException;
/* loaded from: a.zip:com/android/launcher3/backup/nano/BackupProtos$Screen.class */
public final class BackupProtos$Screen extends MessageNano {
    public long id;
    public int rank;

    public BackupProtos$Screen() {
        clear();
    }

    public BackupProtos$Screen clear() {
        this.id = 0L;
        this.rank = 0;
        this.cachedSize = -1;
        return this;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.google.protobuf.nano.MessageNano
    public int computeSerializedSize() {
        int computeSerializedSize = super.computeSerializedSize() + CodedOutputByteBufferNano.computeInt64Size(1, this.id);
        int i = computeSerializedSize;
        if (this.rank != 0) {
            i = computeSerializedSize + CodedOutputByteBufferNano.computeInt32Size(2, this.rank);
        }
        return i;
    }

    @Override // com.google.protobuf.nano.MessageNano
    public BackupProtos$Screen mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
        while (true) {
            int readTag = codedInputByteBufferNano.readTag();
            switch (readTag) {
                case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                    return this;
                case 8:
                    this.id = codedInputByteBufferNano.readInt64();
                    break;
                case 16:
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
        if (this.rank != 0) {
            codedOutputByteBufferNano.writeInt32(2, this.rank);
        }
        super.writeTo(codedOutputByteBufferNano);
    }
}
