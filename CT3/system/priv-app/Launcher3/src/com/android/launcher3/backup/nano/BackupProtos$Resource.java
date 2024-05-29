package com.android.launcher3.backup.nano;

import com.android.launcher3.compat.PackageInstallerCompat;
import com.google.protobuf.nano.CodedInputByteBufferNano;
import com.google.protobuf.nano.CodedOutputByteBufferNano;
import com.google.protobuf.nano.MessageNano;
import com.google.protobuf.nano.WireFormatNano;
import java.io.IOException;
/* loaded from: a.zip:com/android/launcher3/backup/nano/BackupProtos$Resource.class */
public final class BackupProtos$Resource extends MessageNano {
    public byte[] data;
    public int dpi;

    public BackupProtos$Resource() {
        clear();
    }

    public BackupProtos$Resource clear() {
        this.dpi = 0;
        this.data = WireFormatNano.EMPTY_BYTES;
        this.cachedSize = -1;
        return this;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.google.protobuf.nano.MessageNano
    public int computeSerializedSize() {
        return super.computeSerializedSize() + CodedOutputByteBufferNano.computeInt32Size(1, this.dpi) + CodedOutputByteBufferNano.computeBytesSize(2, this.data);
    }

    @Override // com.google.protobuf.nano.MessageNano
    public BackupProtos$Resource mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
        while (true) {
            int readTag = codedInputByteBufferNano.readTag();
            switch (readTag) {
                case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                    return this;
                case 8:
                    this.dpi = codedInputByteBufferNano.readInt32();
                    break;
                case 18:
                    this.data = codedInputByteBufferNano.readBytes();
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
        codedOutputByteBufferNano.writeInt32(1, this.dpi);
        codedOutputByteBufferNano.writeBytes(2, this.data);
        super.writeTo(codedOutputByteBufferNano);
    }
}
