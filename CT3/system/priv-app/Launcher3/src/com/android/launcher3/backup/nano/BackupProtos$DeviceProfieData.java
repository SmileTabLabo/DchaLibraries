package com.android.launcher3.backup.nano;

import com.android.launcher3.compat.PackageInstallerCompat;
import com.google.protobuf.nano.CodedInputByteBufferNano;
import com.google.protobuf.nano.CodedOutputByteBufferNano;
import com.google.protobuf.nano.MessageNano;
import com.google.protobuf.nano.WireFormatNano;
import java.io.IOException;
/* loaded from: a.zip:com/android/launcher3/backup/nano/BackupProtos$DeviceProfieData.class */
public final class BackupProtos$DeviceProfieData extends MessageNano {
    public int allappsRank;
    public float desktopCols;
    public float desktopRows;
    public float hotseatCount;

    public BackupProtos$DeviceProfieData() {
        clear();
    }

    public BackupProtos$DeviceProfieData clear() {
        this.desktopRows = 0.0f;
        this.desktopCols = 0.0f;
        this.hotseatCount = 0.0f;
        this.allappsRank = 0;
        this.cachedSize = -1;
        return this;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.google.protobuf.nano.MessageNano
    public int computeSerializedSize() {
        return super.computeSerializedSize() + CodedOutputByteBufferNano.computeFloatSize(1, this.desktopRows) + CodedOutputByteBufferNano.computeFloatSize(2, this.desktopCols) + CodedOutputByteBufferNano.computeFloatSize(3, this.hotseatCount) + CodedOutputByteBufferNano.computeInt32Size(4, this.allappsRank);
    }

    @Override // com.google.protobuf.nano.MessageNano
    public BackupProtos$DeviceProfieData mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
        while (true) {
            int readTag = codedInputByteBufferNano.readTag();
            switch (readTag) {
                case PackageInstallerCompat.STATUS_INSTALLED /* 0 */:
                    return this;
                case 13:
                    this.desktopRows = codedInputByteBufferNano.readFloat();
                    break;
                case 21:
                    this.desktopCols = codedInputByteBufferNano.readFloat();
                    break;
                case 29:
                    this.hotseatCount = codedInputByteBufferNano.readFloat();
                    break;
                case 32:
                    this.allappsRank = codedInputByteBufferNano.readInt32();
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
        codedOutputByteBufferNano.writeFloat(1, this.desktopRows);
        codedOutputByteBufferNano.writeFloat(2, this.desktopCols);
        codedOutputByteBufferNano.writeFloat(3, this.hotseatCount);
        codedOutputByteBufferNano.writeInt32(4, this.allappsRank);
        super.writeTo(codedOutputByteBufferNano);
    }
}
