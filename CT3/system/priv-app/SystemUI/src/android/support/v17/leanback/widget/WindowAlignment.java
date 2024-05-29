package android.support.v17.leanback.widget;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: a.zip:android/support/v17/leanback/widget/WindowAlignment.class */
public class WindowAlignment {
    private int mOrientation = 0;
    public final Axis vertical = new Axis("vertical");
    public final Axis horizontal = new Axis("horizontal");
    private Axis mMainAxis = this.horizontal;
    private Axis mSecondAxis = this.vertical;

    /* loaded from: a.zip:android/support/v17/leanback/widget/WindowAlignment$Axis.class */
    public static class Axis {
        private int mMaxEdge;
        private int mMaxScroll;
        private int mMinEdge;
        private int mMinScroll;
        private String mName;
        private int mPaddingHigh;
        private int mPaddingLow;
        private boolean mReversedFlow;
        private float mScrollCenter;
        private int mSize;
        private int mWindowAlignment = 3;
        private int mWindowAlignmentOffset = 0;
        private float mWindowAlignmentOffsetPercent = 50.0f;

        public Axis(String str) {
            reset();
            this.mName = str;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void reset() {
            this.mScrollCenter = -2.14748365E9f;
            this.mMinEdge = Integer.MIN_VALUE;
            this.mMaxEdge = Integer.MAX_VALUE;
        }

        public final int getClientSize() {
            return (this.mSize - this.mPaddingLow) - this.mPaddingHigh;
        }

        public final int getMaxEdge() {
            return this.mMaxEdge;
        }

        public final int getMaxScroll() {
            return this.mMaxScroll;
        }

        public final int getMinEdge() {
            return this.mMinEdge;
        }

        public final int getMinScroll() {
            return this.mMinScroll;
        }

        public final int getPaddingHigh() {
            return this.mPaddingHigh;
        }

        public final int getPaddingLow() {
            return this.mPaddingLow;
        }

        public final int getSize() {
            return this.mSize;
        }

        /* JADX WARN: Code restructure failed: missing block: B:21:0x006c, code lost:
            if (r6 == false) goto L32;
         */
        /* JADX WARN: Code restructure failed: missing block: B:23:0x0077, code lost:
            if ((r5 - r4.mMinEdge) > r9) goto L38;
         */
        /* JADX WARN: Code restructure failed: missing block: B:25:0x0083, code lost:
            return r4.mMinEdge - r4.mPaddingLow;
         */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
        */
        public final int getSystemScrollPos(int i, boolean z, boolean z2) {
            int i2;
            if (this.mReversedFlow) {
                int i3 = this.mWindowAlignmentOffset >= 0 ? (this.mSize - this.mWindowAlignmentOffset) - this.mPaddingLow : (-this.mWindowAlignmentOffset) - this.mPaddingLow;
                i2 = i3;
                if (this.mWindowAlignmentOffsetPercent != -1.0f) {
                    i2 = i3 - ((int) ((this.mSize * this.mWindowAlignmentOffsetPercent) / 100.0f));
                }
            } else {
                int i4 = this.mWindowAlignmentOffset >= 0 ? this.mWindowAlignmentOffset - this.mPaddingLow : (this.mSize + this.mWindowAlignmentOffset) - this.mPaddingLow;
                i2 = i4;
                if (this.mWindowAlignmentOffsetPercent != -1.0f) {
                    i2 = i4 + ((int) ((this.mSize * this.mWindowAlignmentOffsetPercent) / 100.0f));
                }
            }
            int clientSize = getClientSize();
            boolean isMinUnknown = isMinUnknown();
            boolean isMaxUnknown = isMaxUnknown();
            if (!isMinUnknown && !isMaxUnknown && (this.mWindowAlignment & 3) == 3 && this.mMaxEdge - this.mMinEdge <= clientSize) {
                return this.mReversedFlow ? (this.mMaxEdge - this.mPaddingLow) - clientSize : this.mMinEdge - this.mPaddingLow;
            }
            if (!isMinUnknown) {
                if (!this.mReversedFlow) {
                }
            }
            return (isMaxUnknown || (this.mReversedFlow ? (this.mWindowAlignment & 1) == 0 : (this.mWindowAlignment & 2) == 0) || (!z2 && this.mMaxEdge - i > clientSize - i2)) ? (i - i2) - this.mPaddingLow : (this.mMaxEdge - this.mPaddingLow) - clientSize;
        }

        public final void invalidateScrollMax() {
            this.mMaxEdge = Integer.MAX_VALUE;
            this.mMaxScroll = Integer.MAX_VALUE;
        }

        public final void invalidateScrollMin() {
            this.mMinEdge = Integer.MIN_VALUE;
            this.mMinScroll = Integer.MIN_VALUE;
        }

        public final boolean isMaxUnknown() {
            return this.mMaxEdge == Integer.MAX_VALUE;
        }

        public final boolean isMinUnknown() {
            return this.mMinEdge == Integer.MIN_VALUE;
        }

        public final void setMaxEdge(int i) {
            this.mMaxEdge = i;
        }

        public final void setMaxScroll(int i) {
            this.mMaxScroll = i;
        }

        public final void setMinEdge(int i) {
            this.mMinEdge = i;
        }

        public final void setMinScroll(int i) {
            this.mMinScroll = i;
        }

        public final void setPadding(int i, int i2) {
            this.mPaddingLow = i;
            this.mPaddingHigh = i2;
        }

        public final void setReversedFlow(boolean z) {
            this.mReversedFlow = z;
        }

        public final void setSize(int i) {
            this.mSize = i;
        }

        public final void setWindowAlignment(int i) {
            this.mWindowAlignment = i;
        }

        public String toString() {
            return "center: " + this.mScrollCenter + " min:" + this.mMinEdge + " max:" + this.mMaxEdge;
        }
    }

    public final Axis mainAxis() {
        return this.mMainAxis;
    }

    public final void reset() {
        mainAxis().reset();
    }

    public final Axis secondAxis() {
        return this.mSecondAxis;
    }

    public final void setOrientation(int i) {
        this.mOrientation = i;
        if (this.mOrientation == 0) {
            this.mMainAxis = this.horizontal;
            this.mSecondAxis = this.vertical;
            return;
        }
        this.mMainAxis = this.vertical;
        this.mSecondAxis = this.horizontal;
    }

    public String toString() {
        return new StringBuffer().append("horizontal=").append(this.horizontal.toString()).append("; vertical=").append(this.vertical.toString()).toString();
    }
}
