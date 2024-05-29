package com.android.launcher3.backup.nano;

import com.android.launcher3.compat.PackageInstallerCompat;
import com.google.protobuf.nano.CodedInputByteBufferNano;
import com.google.protobuf.nano.CodedOutputByteBufferNano;
import com.google.protobuf.nano.MessageNano;
import com.google.protobuf.nano.WireFormatNano;
import java.io.IOException;
/* loaded from: a.zip:com/android/launcher3/backup/nano/BackupProtos$CheckedMessage.class */
public final class BackupProtos$CheckedMessage extends MessageNano {
    public long checksum;
    public byte[] payload;

    public BackupProtos$CheckedMessage() {
        clear();
    }

    public BackupProtos$CheckedMessage clear() {
        this.payload = WireFormatNano.EMPTY_BYTES;
        this.checksum = 0L;
        this.cachedSize = -1;
        return this;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.google.protobuf.nano.MessageNano
    public int computeSerializedSize() {
        return super.computeSerializedSize() + CodedOutputByteBufferNano.computeBytesSize(1, this.payload) + CodedOutputByteBufferNano.computeInt64Size(2, this.checksum);
    }

    @Override // com.google.protobuf.nano.MessageNano
    public BackupProtos$CheckedMessage mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
        while (true) {
            int readTag = codedInputByteBufferNano.readTag();
            switch (readTag) {
                case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                    return this;
                case 10:
                    this.payload = codedInputByteBufferNano.readBytes();
                    break;
                case 16:
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
        codedOutputByteBufferNano.writeBytes(1, this.payload);
        codedOutputByteBufferNano.writeInt64(2, this.checksum);
        super.writeTo(codedOutputByteBufferNano);
    }
}
