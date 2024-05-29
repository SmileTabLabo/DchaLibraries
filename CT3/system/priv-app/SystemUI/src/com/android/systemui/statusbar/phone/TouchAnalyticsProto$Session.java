package com.android.systemui.statusbar.phone;

import com.google.protobuf.nano.CodedInputByteBufferNano;
import com.google.protobuf.nano.CodedOutputByteBufferNano;
import com.google.protobuf.nano.InternalNano;
import com.google.protobuf.nano.MessageNano;
import com.google.protobuf.nano.WireFormatNano;
import java.io.IOException;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/TouchAnalyticsProto$Session.class */
public final class TouchAnalyticsProto$Session extends MessageNano {
    private int bitField0_;
    private String build_;
    private long durationMillis_;
    public PhoneEvent[] phoneEvents;
    private int result_;
    public SensorEvent[] sensorEvents;
    private long startTimestampMillis_;
    private int touchAreaHeight_;
    private int touchAreaWidth_;
    public TouchEvent[] touchEvents;
    private int type_;

    /* loaded from: a.zip:com/android/systemui/statusbar/phone/TouchAnalyticsProto$Session$PhoneEvent.class */
    public static final class PhoneEvent extends MessageNano {
        private static volatile PhoneEvent[] _emptyArray;
        private int bitField0_;
        private long timeOffsetNanos_;
        private int type_;

        public PhoneEvent() {
            clear();
        }

        public static PhoneEvent[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new PhoneEvent[0];
                    }
                }
            }
            return _emptyArray;
        }

        public PhoneEvent clear() {
            this.bitField0_ = 0;
            this.type_ = 0;
            this.timeOffsetNanos_ = 0L;
            this.cachedSize = -1;
            return this;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // com.google.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int computeSerializedSize = super.computeSerializedSize();
            int i = computeSerializedSize;
            if ((this.bitField0_ & 1) != 0) {
                i = computeSerializedSize + CodedOutputByteBufferNano.computeInt32Size(1, this.type_);
            }
            int i2 = i;
            if ((this.bitField0_ & 2) != 0) {
                i2 = i + CodedOutputByteBufferNano.computeUInt64Size(2, this.timeOffsetNanos_);
            }
            return i2;
        }

        @Override // com.google.protobuf.nano.MessageNano
        public PhoneEvent mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            while (true) {
                int readTag = codedInputByteBufferNano.readTag();
                switch (readTag) {
                    case 0:
                        return this;
                    case 8:
                        int readInt32 = codedInputByteBufferNano.readInt32();
                        switch (readInt32) {
                            case 0:
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                            case 5:
                            case 6:
                            case 7:
                            case 8:
                            case 9:
                            case 10:
                            case 11:
                            case 12:
                            case 13:
                            case 14:
                            case 15:
                            case 16:
                            case 17:
                            case 18:
                            case 19:
                            case 20:
                            case 21:
                            case 22:
                            case 23:
                            case 24:
                            case 25:
                            case 26:
                            case 27:
                            case 28:
                                this.type_ = readInt32;
                                this.bitField0_ |= 1;
                                continue;
                        }
                    case 16:
                        this.timeOffsetNanos_ = codedInputByteBufferNano.readUInt64();
                        this.bitField0_ |= 2;
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

        public PhoneEvent setTimeOffsetNanos(long j) {
            this.timeOffsetNanos_ = j;
            this.bitField0_ |= 2;
            return this;
        }

        public PhoneEvent setType(int i) {
            this.type_ = i;
            this.bitField0_ |= 1;
            return this;
        }

        @Override // com.google.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if ((this.bitField0_ & 1) != 0) {
                codedOutputByteBufferNano.writeInt32(1, this.type_);
            }
            if ((this.bitField0_ & 2) != 0) {
                codedOutputByteBufferNano.writeUInt64(2, this.timeOffsetNanos_);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }

    /* loaded from: a.zip:com/android/systemui/statusbar/phone/TouchAnalyticsProto$Session$SensorEvent.class */
    public static final class SensorEvent extends MessageNano {
        private static volatile SensorEvent[] _emptyArray;
        private int bitField0_;
        private long timeOffsetNanos_;
        private long timestamp_;
        private int type_;
        public float[] values;

        public SensorEvent() {
            clear();
        }

        public static SensorEvent[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new SensorEvent[0];
                    }
                }
            }
            return _emptyArray;
        }

        public SensorEvent clear() {
            this.bitField0_ = 0;
            this.type_ = 1;
            this.timeOffsetNanos_ = 0L;
            this.values = WireFormatNano.EMPTY_FLOAT_ARRAY;
            this.timestamp_ = 0L;
            this.cachedSize = -1;
            return this;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // com.google.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int computeSerializedSize = super.computeSerializedSize();
            int i = computeSerializedSize;
            if ((this.bitField0_ & 1) != 0) {
                i = computeSerializedSize + CodedOutputByteBufferNano.computeInt32Size(1, this.type_);
            }
            int i2 = i;
            if ((this.bitField0_ & 2) != 0) {
                i2 = i + CodedOutputByteBufferNano.computeUInt64Size(2, this.timeOffsetNanos_);
            }
            int i3 = i2;
            if (this.values != null) {
                i3 = i2;
                if (this.values.length > 0) {
                    i3 = i2 + (this.values.length * 4) + (this.values.length * 1);
                }
            }
            int i4 = i3;
            if ((this.bitField0_ & 4) != 0) {
                i4 = i3 + CodedOutputByteBufferNano.computeUInt64Size(4, this.timestamp_);
            }
            return i4;
        }

        @Override // com.google.protobuf.nano.MessageNano
        public SensorEvent mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            while (true) {
                int readTag = codedInputByteBufferNano.readTag();
                switch (readTag) {
                    case 0:
                        return this;
                    case 8:
                        int readInt32 = codedInputByteBufferNano.readInt32();
                        switch (readInt32) {
                            case 1:
                            case 4:
                            case 5:
                            case 8:
                            case 11:
                                this.type_ = readInt32;
                                this.bitField0_ |= 1;
                                continue;
                        }
                    case 16:
                        this.timeOffsetNanos_ = codedInputByteBufferNano.readUInt64();
                        this.bitField0_ |= 2;
                        break;
                    case 26:
                        int readRawVarint32 = codedInputByteBufferNano.readRawVarint32();
                        int pushLimit = codedInputByteBufferNano.pushLimit(readRawVarint32);
                        int i = readRawVarint32 / 4;
                        int length = this.values == null ? 0 : this.values.length;
                        float[] fArr = new float[length + i];
                        int i2 = length;
                        if (length != 0) {
                            System.arraycopy(this.values, 0, fArr, 0, length);
                            i2 = length;
                        }
                        while (i2 < fArr.length) {
                            fArr[i2] = codedInputByteBufferNano.readFloat();
                            i2++;
                        }
                        this.values = fArr;
                        codedInputByteBufferNano.popLimit(pushLimit);
                        break;
                    case 29:
                        int repeatedFieldArrayLength = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 29);
                        int length2 = this.values == null ? 0 : this.values.length;
                        float[] fArr2 = new float[length2 + repeatedFieldArrayLength];
                        int i3 = length2;
                        if (length2 != 0) {
                            System.arraycopy(this.values, 0, fArr2, 0, length2);
                            i3 = length2;
                        }
                        while (i3 < fArr2.length - 1) {
                            fArr2[i3] = codedInputByteBufferNano.readFloat();
                            codedInputByteBufferNano.readTag();
                            i3++;
                        }
                        fArr2[i3] = codedInputByteBufferNano.readFloat();
                        this.values = fArr2;
                        break;
                    case 32:
                        this.timestamp_ = codedInputByteBufferNano.readUInt64();
                        this.bitField0_ |= 4;
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

        public SensorEvent setTimeOffsetNanos(long j) {
            this.timeOffsetNanos_ = j;
            this.bitField0_ |= 2;
            return this;
        }

        public SensorEvent setTimestamp(long j) {
            this.timestamp_ = j;
            this.bitField0_ |= 4;
            return this;
        }

        public SensorEvent setType(int i) {
            this.type_ = i;
            this.bitField0_ |= 1;
            return this;
        }

        @Override // com.google.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if ((this.bitField0_ & 1) != 0) {
                codedOutputByteBufferNano.writeInt32(1, this.type_);
            }
            if ((this.bitField0_ & 2) != 0) {
                codedOutputByteBufferNano.writeUInt64(2, this.timeOffsetNanos_);
            }
            if (this.values != null && this.values.length > 0) {
                for (int i = 0; i < this.values.length; i++) {
                    codedOutputByteBufferNano.writeFloat(3, this.values[i]);
                }
            }
            if ((this.bitField0_ & 4) != 0) {
                codedOutputByteBufferNano.writeUInt64(4, this.timestamp_);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }

    /* loaded from: a.zip:com/android/systemui/statusbar/phone/TouchAnalyticsProto$Session$TouchEvent.class */
    public static final class TouchEvent extends MessageNano {
        private static volatile TouchEvent[] _emptyArray;
        private int actionIndex_;
        private int action_;
        private int bitField0_;
        public Pointer[] pointers;
        public BoundingBox removedBoundingBox;
        private boolean removedRedacted_;
        private long timeOffsetNanos_;

        /* loaded from: a.zip:com/android/systemui/statusbar/phone/TouchAnalyticsProto$Session$TouchEvent$BoundingBox.class */
        public static final class BoundingBox extends MessageNano {
            private int bitField0_;
            private float height_;
            private float width_;

            public BoundingBox() {
                clear();
            }

            public BoundingBox clear() {
                this.bitField0_ = 0;
                this.width_ = 0.0f;
                this.height_ = 0.0f;
                this.cachedSize = -1;
                return this;
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // com.google.protobuf.nano.MessageNano
            public int computeSerializedSize() {
                int computeSerializedSize = super.computeSerializedSize();
                int i = computeSerializedSize;
                if ((this.bitField0_ & 1) != 0) {
                    i = computeSerializedSize + CodedOutputByteBufferNano.computeFloatSize(1, this.width_);
                }
                int i2 = i;
                if ((this.bitField0_ & 2) != 0) {
                    i2 = i + CodedOutputByteBufferNano.computeFloatSize(2, this.height_);
                }
                return i2;
            }

            @Override // com.google.protobuf.nano.MessageNano
            public BoundingBox mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
                while (true) {
                    int readTag = codedInputByteBufferNano.readTag();
                    switch (readTag) {
                        case 0:
                            return this;
                        case 13:
                            this.width_ = codedInputByteBufferNano.readFloat();
                            this.bitField0_ |= 1;
                            break;
                        case 21:
                            this.height_ = codedInputByteBufferNano.readFloat();
                            this.bitField0_ |= 2;
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
                if ((this.bitField0_ & 1) != 0) {
                    codedOutputByteBufferNano.writeFloat(1, this.width_);
                }
                if ((this.bitField0_ & 2) != 0) {
                    codedOutputByteBufferNano.writeFloat(2, this.height_);
                }
                super.writeTo(codedOutputByteBufferNano);
            }
        }

        /* loaded from: a.zip:com/android/systemui/statusbar/phone/TouchAnalyticsProto$Session$TouchEvent$Pointer.class */
        public static final class Pointer extends MessageNano {
            private static volatile Pointer[] _emptyArray;
            private int bitField0_;
            private int id_;
            private float pressure_;
            public BoundingBox removedBoundingBox;
            private float removedLength_;
            private float size_;
            private float x_;
            private float y_;

            public Pointer() {
                clear();
            }

            public static Pointer[] emptyArray() {
                if (_emptyArray == null) {
                    synchronized (InternalNano.LAZY_INIT_LOCK) {
                        if (_emptyArray == null) {
                            _emptyArray = new Pointer[0];
                        }
                    }
                }
                return _emptyArray;
            }

            public Pointer clear() {
                this.bitField0_ = 0;
                this.x_ = 0.0f;
                this.y_ = 0.0f;
                this.size_ = 0.0f;
                this.pressure_ = 0.0f;
                this.id_ = 0;
                this.removedLength_ = 0.0f;
                this.removedBoundingBox = null;
                this.cachedSize = -1;
                return this;
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // com.google.protobuf.nano.MessageNano
            public int computeSerializedSize() {
                int computeSerializedSize = super.computeSerializedSize();
                int i = computeSerializedSize;
                if ((this.bitField0_ & 1) != 0) {
                    i = computeSerializedSize + CodedOutputByteBufferNano.computeFloatSize(1, this.x_);
                }
                int i2 = i;
                if ((this.bitField0_ & 2) != 0) {
                    i2 = i + CodedOutputByteBufferNano.computeFloatSize(2, this.y_);
                }
                int i3 = i2;
                if ((this.bitField0_ & 4) != 0) {
                    i3 = i2 + CodedOutputByteBufferNano.computeFloatSize(3, this.size_);
                }
                int i4 = i3;
                if ((this.bitField0_ & 8) != 0) {
                    i4 = i3 + CodedOutputByteBufferNano.computeFloatSize(4, this.pressure_);
                }
                int i5 = i4;
                if ((this.bitField0_ & 16) != 0) {
                    i5 = i4 + CodedOutputByteBufferNano.computeInt32Size(5, this.id_);
                }
                int i6 = i5;
                if ((this.bitField0_ & 32) != 0) {
                    i6 = i5 + CodedOutputByteBufferNano.computeFloatSize(6, this.removedLength_);
                }
                int i7 = i6;
                if (this.removedBoundingBox != null) {
                    i7 = i6 + CodedOutputByteBufferNano.computeMessageSize(7, this.removedBoundingBox);
                }
                return i7;
            }

            @Override // com.google.protobuf.nano.MessageNano
            public Pointer mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
                while (true) {
                    int readTag = codedInputByteBufferNano.readTag();
                    switch (readTag) {
                        case 0:
                            return this;
                        case 13:
                            this.x_ = codedInputByteBufferNano.readFloat();
                            this.bitField0_ |= 1;
                            break;
                        case 21:
                            this.y_ = codedInputByteBufferNano.readFloat();
                            this.bitField0_ |= 2;
                            break;
                        case 29:
                            this.size_ = codedInputByteBufferNano.readFloat();
                            this.bitField0_ |= 4;
                            break;
                        case 37:
                            this.pressure_ = codedInputByteBufferNano.readFloat();
                            this.bitField0_ |= 8;
                            break;
                        case 40:
                            this.id_ = codedInputByteBufferNano.readInt32();
                            this.bitField0_ |= 16;
                            break;
                        case 53:
                            this.removedLength_ = codedInputByteBufferNano.readFloat();
                            this.bitField0_ |= 32;
                            break;
                        case 58:
                            if (this.removedBoundingBox == null) {
                                this.removedBoundingBox = new BoundingBox();
                            }
                            codedInputByteBufferNano.readMessage(this.removedBoundingBox);
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

            public Pointer setId(int i) {
                this.id_ = i;
                this.bitField0_ |= 16;
                return this;
            }

            public Pointer setPressure(float f) {
                this.pressure_ = f;
                this.bitField0_ |= 8;
                return this;
            }

            public Pointer setSize(float f) {
                this.size_ = f;
                this.bitField0_ |= 4;
                return this;
            }

            public Pointer setX(float f) {
                this.x_ = f;
                this.bitField0_ |= 1;
                return this;
            }

            public Pointer setY(float f) {
                this.y_ = f;
                this.bitField0_ |= 2;
                return this;
            }

            @Override // com.google.protobuf.nano.MessageNano
            public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
                if ((this.bitField0_ & 1) != 0) {
                    codedOutputByteBufferNano.writeFloat(1, this.x_);
                }
                if ((this.bitField0_ & 2) != 0) {
                    codedOutputByteBufferNano.writeFloat(2, this.y_);
                }
                if ((this.bitField0_ & 4) != 0) {
                    codedOutputByteBufferNano.writeFloat(3, this.size_);
                }
                if ((this.bitField0_ & 8) != 0) {
                    codedOutputByteBufferNano.writeFloat(4, this.pressure_);
                }
                if ((this.bitField0_ & 16) != 0) {
                    codedOutputByteBufferNano.writeInt32(5, this.id_);
                }
                if ((this.bitField0_ & 32) != 0) {
                    codedOutputByteBufferNano.writeFloat(6, this.removedLength_);
                }
                if (this.removedBoundingBox != null) {
                    codedOutputByteBufferNano.writeMessage(7, this.removedBoundingBox);
                }
                super.writeTo(codedOutputByteBufferNano);
            }
        }

        public TouchEvent() {
            clear();
        }

        public static TouchEvent[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new TouchEvent[0];
                    }
                }
            }
            return _emptyArray;
        }

        public TouchEvent clear() {
            this.bitField0_ = 0;
            this.timeOffsetNanos_ = 0L;
            this.action_ = 0;
            this.actionIndex_ = 0;
            this.pointers = Pointer.emptyArray();
            this.removedRedacted_ = false;
            this.removedBoundingBox = null;
            this.cachedSize = -1;
            return this;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // com.google.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int computeSerializedSize = super.computeSerializedSize();
            int i = computeSerializedSize;
            if ((this.bitField0_ & 1) != 0) {
                i = computeSerializedSize + CodedOutputByteBufferNano.computeUInt64Size(1, this.timeOffsetNanos_);
            }
            int i2 = i;
            if ((this.bitField0_ & 2) != 0) {
                i2 = i + CodedOutputByteBufferNano.computeInt32Size(2, this.action_);
            }
            int i3 = i2;
            if ((this.bitField0_ & 4) != 0) {
                i3 = i2 + CodedOutputByteBufferNano.computeInt32Size(3, this.actionIndex_);
            }
            int i4 = i3;
            if (this.pointers != null) {
                i4 = i3;
                if (this.pointers.length > 0) {
                    int i5 = 0;
                    while (true) {
                        i4 = i3;
                        if (i5 >= this.pointers.length) {
                            break;
                        }
                        Pointer pointer = this.pointers[i5];
                        int i6 = i3;
                        if (pointer != null) {
                            i6 = i3 + CodedOutputByteBufferNano.computeMessageSize(4, pointer);
                        }
                        i5++;
                        i3 = i6;
                    }
                }
            }
            int i7 = i4;
            if ((this.bitField0_ & 8) != 0) {
                i7 = i4 + CodedOutputByteBufferNano.computeBoolSize(5, this.removedRedacted_);
            }
            int i8 = i7;
            if (this.removedBoundingBox != null) {
                i8 = i7 + CodedOutputByteBufferNano.computeMessageSize(6, this.removedBoundingBox);
            }
            return i8;
        }

        @Override // com.google.protobuf.nano.MessageNano
        public TouchEvent mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            while (true) {
                int readTag = codedInputByteBufferNano.readTag();
                switch (readTag) {
                    case 0:
                        return this;
                    case 8:
                        this.timeOffsetNanos_ = codedInputByteBufferNano.readUInt64();
                        this.bitField0_ |= 1;
                        break;
                    case 16:
                        int readInt32 = codedInputByteBufferNano.readInt32();
                        switch (readInt32) {
                            case 0:
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                            case 5:
                            case 6:
                                this.action_ = readInt32;
                                this.bitField0_ |= 2;
                                continue;
                        }
                    case 24:
                        this.actionIndex_ = codedInputByteBufferNano.readInt32();
                        this.bitField0_ |= 4;
                        break;
                    case 34:
                        int repeatedFieldArrayLength = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 34);
                        int length = this.pointers == null ? 0 : this.pointers.length;
                        Pointer[] pointerArr = new Pointer[length + repeatedFieldArrayLength];
                        int i = length;
                        if (length != 0) {
                            System.arraycopy(this.pointers, 0, pointerArr, 0, length);
                            i = length;
                        }
                        while (i < pointerArr.length - 1) {
                            pointerArr[i] = new Pointer();
                            codedInputByteBufferNano.readMessage(pointerArr[i]);
                            codedInputByteBufferNano.readTag();
                            i++;
                        }
                        pointerArr[i] = new Pointer();
                        codedInputByteBufferNano.readMessage(pointerArr[i]);
                        this.pointers = pointerArr;
                        break;
                    case 40:
                        this.removedRedacted_ = codedInputByteBufferNano.readBool();
                        this.bitField0_ |= 8;
                        break;
                    case 50:
                        if (this.removedBoundingBox == null) {
                            this.removedBoundingBox = new BoundingBox();
                        }
                        codedInputByteBufferNano.readMessage(this.removedBoundingBox);
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

        public TouchEvent setAction(int i) {
            this.action_ = i;
            this.bitField0_ |= 2;
            return this;
        }

        public TouchEvent setActionIndex(int i) {
            this.actionIndex_ = i;
            this.bitField0_ |= 4;
            return this;
        }

        public TouchEvent setTimeOffsetNanos(long j) {
            this.timeOffsetNanos_ = j;
            this.bitField0_ |= 1;
            return this;
        }

        @Override // com.google.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if ((this.bitField0_ & 1) != 0) {
                codedOutputByteBufferNano.writeUInt64(1, this.timeOffsetNanos_);
            }
            if ((this.bitField0_ & 2) != 0) {
                codedOutputByteBufferNano.writeInt32(2, this.action_);
            }
            if ((this.bitField0_ & 4) != 0) {
                codedOutputByteBufferNano.writeInt32(3, this.actionIndex_);
            }
            if (this.pointers != null && this.pointers.length > 0) {
                for (int i = 0; i < this.pointers.length; i++) {
                    Pointer pointer = this.pointers[i];
                    if (pointer != null) {
                        codedOutputByteBufferNano.writeMessage(4, pointer);
                    }
                }
            }
            if ((this.bitField0_ & 8) != 0) {
                codedOutputByteBufferNano.writeBool(5, this.removedRedacted_);
            }
            if (this.removedBoundingBox != null) {
                codedOutputByteBufferNano.writeMessage(6, this.removedBoundingBox);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }

    public TouchAnalyticsProto$Session() {
        clear();
    }

    public TouchAnalyticsProto$Session clear() {
        this.bitField0_ = 0;
        this.startTimestampMillis_ = 0L;
        this.durationMillis_ = 0L;
        this.build_ = "";
        this.result_ = 0;
        this.touchEvents = TouchEvent.emptyArray();
        this.sensorEvents = SensorEvent.emptyArray();
        this.touchAreaWidth_ = 0;
        this.touchAreaHeight_ = 0;
        this.type_ = 0;
        this.phoneEvents = PhoneEvent.emptyArray();
        this.cachedSize = -1;
        return this;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.google.protobuf.nano.MessageNano
    public int computeSerializedSize() {
        int computeSerializedSize = super.computeSerializedSize();
        int i = computeSerializedSize;
        if ((this.bitField0_ & 1) != 0) {
            i = computeSerializedSize + CodedOutputByteBufferNano.computeUInt64Size(1, this.startTimestampMillis_);
        }
        int i2 = i;
        if ((this.bitField0_ & 2) != 0) {
            i2 = i + CodedOutputByteBufferNano.computeUInt64Size(2, this.durationMillis_);
        }
        int i3 = i2;
        if ((this.bitField0_ & 4) != 0) {
            i3 = i2 + CodedOutputByteBufferNano.computeStringSize(3, this.build_);
        }
        int i4 = i3;
        if ((this.bitField0_ & 8) != 0) {
            i4 = i3 + CodedOutputByteBufferNano.computeInt32Size(4, this.result_);
        }
        int i5 = i4;
        if (this.touchEvents != null) {
            i5 = i4;
            if (this.touchEvents.length > 0) {
                int i6 = 0;
                while (true) {
                    i5 = i4;
                    if (i6 >= this.touchEvents.length) {
                        break;
                    }
                    TouchEvent touchEvent = this.touchEvents[i6];
                    int i7 = i4;
                    if (touchEvent != null) {
                        i7 = i4 + CodedOutputByteBufferNano.computeMessageSize(5, touchEvent);
                    }
                    i6++;
                    i4 = i7;
                }
            }
        }
        int i8 = i5;
        if (this.sensorEvents != null) {
            i8 = i5;
            if (this.sensorEvents.length > 0) {
                int i9 = 0;
                while (true) {
                    i8 = i5;
                    if (i9 >= this.sensorEvents.length) {
                        break;
                    }
                    SensorEvent sensorEvent = this.sensorEvents[i9];
                    int i10 = i5;
                    if (sensorEvent != null) {
                        i10 = i5 + CodedOutputByteBufferNano.computeMessageSize(6, sensorEvent);
                    }
                    i9++;
                    i5 = i10;
                }
            }
        }
        int i11 = i8;
        if ((this.bitField0_ & 16) != 0) {
            i11 = i8 + CodedOutputByteBufferNano.computeInt32Size(9, this.touchAreaWidth_);
        }
        int i12 = i11;
        if ((this.bitField0_ & 32) != 0) {
            i12 = i11 + CodedOutputByteBufferNano.computeInt32Size(10, this.touchAreaHeight_);
        }
        int i13 = i12;
        if ((this.bitField0_ & 64) != 0) {
            i13 = i12 + CodedOutputByteBufferNano.computeInt32Size(11, this.type_);
        }
        int i14 = i13;
        if (this.phoneEvents != null) {
            i14 = i13;
            if (this.phoneEvents.length > 0) {
                int i15 = 0;
                while (true) {
                    i14 = i13;
                    if (i15 >= this.phoneEvents.length) {
                        break;
                    }
                    PhoneEvent phoneEvent = this.phoneEvents[i15];
                    int i16 = i13;
                    if (phoneEvent != null) {
                        i16 = i13 + CodedOutputByteBufferNano.computeMessageSize(12, phoneEvent);
                    }
                    i15++;
                    i13 = i16;
                }
            }
        }
        return i14;
    }

    @Override // com.google.protobuf.nano.MessageNano
    public TouchAnalyticsProto$Session mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
        while (true) {
            int readTag = codedInputByteBufferNano.readTag();
            switch (readTag) {
                case 0:
                    return this;
                case 8:
                    this.startTimestampMillis_ = codedInputByteBufferNano.readUInt64();
                    this.bitField0_ |= 1;
                    break;
                case 16:
                    this.durationMillis_ = codedInputByteBufferNano.readUInt64();
                    this.bitField0_ |= 2;
                    break;
                case 26:
                    this.build_ = codedInputByteBufferNano.readString();
                    this.bitField0_ |= 4;
                    break;
                case 32:
                    int readInt32 = codedInputByteBufferNano.readInt32();
                    switch (readInt32) {
                        case 0:
                        case 1:
                        case 2:
                            this.result_ = readInt32;
                            this.bitField0_ |= 8;
                            continue;
                    }
                case 42:
                    int repeatedFieldArrayLength = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 42);
                    int length = this.touchEvents == null ? 0 : this.touchEvents.length;
                    TouchEvent[] touchEventArr = new TouchEvent[length + repeatedFieldArrayLength];
                    int i = length;
                    if (length != 0) {
                        System.arraycopy(this.touchEvents, 0, touchEventArr, 0, length);
                        i = length;
                    }
                    while (i < touchEventArr.length - 1) {
                        touchEventArr[i] = new TouchEvent();
                        codedInputByteBufferNano.readMessage(touchEventArr[i]);
                        codedInputByteBufferNano.readTag();
                        i++;
                    }
                    touchEventArr[i] = new TouchEvent();
                    codedInputByteBufferNano.readMessage(touchEventArr[i]);
                    this.touchEvents = touchEventArr;
                    break;
                case 50:
                    int repeatedFieldArrayLength2 = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 50);
                    int length2 = this.sensorEvents == null ? 0 : this.sensorEvents.length;
                    SensorEvent[] sensorEventArr = new SensorEvent[length2 + repeatedFieldArrayLength2];
                    int i2 = length2;
                    if (length2 != 0) {
                        System.arraycopy(this.sensorEvents, 0, sensorEventArr, 0, length2);
                        i2 = length2;
                    }
                    while (i2 < sensorEventArr.length - 1) {
                        sensorEventArr[i2] = new SensorEvent();
                        codedInputByteBufferNano.readMessage(sensorEventArr[i2]);
                        codedInputByteBufferNano.readTag();
                        i2++;
                    }
                    sensorEventArr[i2] = new SensorEvent();
                    codedInputByteBufferNano.readMessage(sensorEventArr[i2]);
                    this.sensorEvents = sensorEventArr;
                    break;
                case 72:
                    this.touchAreaWidth_ = codedInputByteBufferNano.readInt32();
                    this.bitField0_ |= 16;
                    break;
                case 80:
                    this.touchAreaHeight_ = codedInputByteBufferNano.readInt32();
                    this.bitField0_ |= 32;
                    break;
                case 88:
                    int readInt322 = codedInputByteBufferNano.readInt32();
                    switch (readInt322) {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                            this.type_ = readInt322;
                            this.bitField0_ |= 64;
                            continue;
                    }
                case 98:
                    int repeatedFieldArrayLength3 = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 98);
                    int length3 = this.phoneEvents == null ? 0 : this.phoneEvents.length;
                    PhoneEvent[] phoneEventArr = new PhoneEvent[length3 + repeatedFieldArrayLength3];
                    int i3 = length3;
                    if (length3 != 0) {
                        System.arraycopy(this.phoneEvents, 0, phoneEventArr, 0, length3);
                        i3 = length3;
                    }
                    while (i3 < phoneEventArr.length - 1) {
                        phoneEventArr[i3] = new PhoneEvent();
                        codedInputByteBufferNano.readMessage(phoneEventArr[i3]);
                        codedInputByteBufferNano.readTag();
                        i3++;
                    }
                    phoneEventArr[i3] = new PhoneEvent();
                    codedInputByteBufferNano.readMessage(phoneEventArr[i3]);
                    this.phoneEvents = phoneEventArr;
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

    public TouchAnalyticsProto$Session setBuild(String str) {
        if (str == null) {
            throw new NullPointerException();
        }
        this.build_ = str;
        this.bitField0_ |= 4;
        return this;
    }

    public TouchAnalyticsProto$Session setDurationMillis(long j) {
        this.durationMillis_ = j;
        this.bitField0_ |= 2;
        return this;
    }

    public TouchAnalyticsProto$Session setResult(int i) {
        this.result_ = i;
        this.bitField0_ |= 8;
        return this;
    }

    public TouchAnalyticsProto$Session setStartTimestampMillis(long j) {
        this.startTimestampMillis_ = j;
        this.bitField0_ |= 1;
        return this;
    }

    public TouchAnalyticsProto$Session setTouchAreaHeight(int i) {
        this.touchAreaHeight_ = i;
        this.bitField0_ |= 32;
        return this;
    }

    public TouchAnalyticsProto$Session setTouchAreaWidth(int i) {
        this.touchAreaWidth_ = i;
        this.bitField0_ |= 16;
        return this;
    }

    public TouchAnalyticsProto$Session setType(int i) {
        this.type_ = i;
        this.bitField0_ |= 64;
        return this;
    }

    @Override // com.google.protobuf.nano.MessageNano
    public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
        if ((this.bitField0_ & 1) != 0) {
            codedOutputByteBufferNano.writeUInt64(1, this.startTimestampMillis_);
        }
        if ((this.bitField0_ & 2) != 0) {
            codedOutputByteBufferNano.writeUInt64(2, this.durationMillis_);
        }
        if ((this.bitField0_ & 4) != 0) {
            codedOutputByteBufferNano.writeString(3, this.build_);
        }
        if ((this.bitField0_ & 8) != 0) {
            codedOutputByteBufferNano.writeInt32(4, this.result_);
        }
        if (this.touchEvents != null && this.touchEvents.length > 0) {
            for (int i = 0; i < this.touchEvents.length; i++) {
                TouchEvent touchEvent = this.touchEvents[i];
                if (touchEvent != null) {
                    codedOutputByteBufferNano.writeMessage(5, touchEvent);
                }
            }
        }
        if (this.sensorEvents != null && this.sensorEvents.length > 0) {
            for (int i2 = 0; i2 < this.sensorEvents.length; i2++) {
                SensorEvent sensorEvent = this.sensorEvents[i2];
                if (sensorEvent != null) {
                    codedOutputByteBufferNano.writeMessage(6, sensorEvent);
                }
            }
        }
        if ((this.bitField0_ & 16) != 0) {
            codedOutputByteBufferNano.writeInt32(9, this.touchAreaWidth_);
        }
        if ((this.bitField0_ & 32) != 0) {
            codedOutputByteBufferNano.writeInt32(10, this.touchAreaHeight_);
        }
        if ((this.bitField0_ & 64) != 0) {
            codedOutputByteBufferNano.writeInt32(11, this.type_);
        }
        if (this.phoneEvents != null && this.phoneEvents.length > 0) {
            for (int i3 = 0; i3 < this.phoneEvents.length; i3++) {
                PhoneEvent phoneEvent = this.phoneEvents[i3];
                if (phoneEvent != null) {
                    codedOutputByteBufferNano.writeMessage(12, phoneEvent);
                }
            }
        }
        super.writeTo(codedOutputByteBufferNano);
    }
}
