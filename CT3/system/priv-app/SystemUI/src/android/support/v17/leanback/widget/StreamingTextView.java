package android.support.v17.leanback.widget;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.support.v17.leanback.R$drawable;
import android.text.SpannableStringBuilder;
import android.text.SpannedString;
import android.text.style.ReplacementSpan;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.EditText;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: a.zip:android/support/v17/leanback/widget/StreamingTextView.class */
public class StreamingTextView extends EditText {
    private static final Pattern SPLIT_PATTERN = Pattern.compile("\\S+");
    private static final Property<StreamingTextView, Integer> STREAM_POSITION_PROPERTY = new Property<StreamingTextView, Integer>(Integer.class, "streamPosition") { // from class: android.support.v17.leanback.widget.StreamingTextView.1
        @Override // android.util.Property
        public Integer get(StreamingTextView streamingTextView) {
            return Integer.valueOf(streamingTextView.getStreamPosition());
        }

        @Override // android.util.Property
        public void set(StreamingTextView streamingTextView, Integer num) {
            streamingTextView.setStreamPosition(num.intValue());
        }
    };
    private Bitmap mOneDot;
    private final Random mRandom;
    private int mStreamPosition;
    private ObjectAnimator mStreamingAnimation;
    private Bitmap mTwoDot;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:android/support/v17/leanback/widget/StreamingTextView$DottySpan.class */
    public class DottySpan extends ReplacementSpan {
        private final int mPosition;
        private final int mSeed;
        final StreamingTextView this$0;

        public DottySpan(StreamingTextView streamingTextView, int i, int i2) {
            this.this$0 = streamingTextView;
            this.mSeed = i;
            this.mPosition = i2;
        }

        @Override // android.text.style.ReplacementSpan
        public void draw(Canvas canvas, CharSequence charSequence, int i, int i2, float f, int i3, int i4, int i5, Paint paint) {
            int measureText = (int) paint.measureText(charSequence, i, i2);
            int width = this.this$0.mOneDot.getWidth();
            int i6 = width * 2;
            int i7 = measureText / i6;
            int i8 = (measureText % i6) / 2;
            boolean isLayoutRtl = StreamingTextView.isLayoutRtl(this.this$0);
            this.this$0.mRandom.setSeed(this.mSeed);
            int alpha = paint.getAlpha();
            for (int i9 = 0; i9 < i7 && this.mPosition + i9 < this.this$0.mStreamPosition; i9++) {
                float f2 = (i9 * i6) + i8 + (width / 2);
                float f3 = isLayoutRtl ? ((measureText + f) - f2) - width : f + f2;
                paint.setAlpha((this.this$0.mRandom.nextInt(4) + 1) * 63);
                if (this.this$0.mRandom.nextBoolean()) {
                    canvas.drawBitmap(this.this$0.mTwoDot, f3, i4 - this.this$0.mTwoDot.getHeight(), paint);
                } else {
                    canvas.drawBitmap(this.this$0.mOneDot, f3, i4 - this.this$0.mOneDot.getHeight(), paint);
                }
            }
            paint.setAlpha(alpha);
        }

        @Override // android.text.style.ReplacementSpan
        public int getSize(Paint paint, CharSequence charSequence, int i, int i2, Paint.FontMetricsInt fontMetricsInt) {
            return (int) paint.measureText(charSequence, i, i2);
        }
    }

    public StreamingTextView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mRandom = new Random();
    }

    public StreamingTextView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mRandom = new Random();
    }

    private void addDottySpans(SpannableStringBuilder spannableStringBuilder, String str, int i) {
        Matcher matcher = SPLIT_PATTERN.matcher(str);
        while (matcher.find()) {
            int start = i + matcher.start();
            spannableStringBuilder.setSpan(new DottySpan(this, str.charAt(matcher.start()), start), start, i + matcher.end(), 33);
        }
    }

    private void cancelStreamAnimation() {
        if (this.mStreamingAnimation != null) {
            this.mStreamingAnimation.cancel();
        }
    }

    private Bitmap getScaledBitmap(int i, float f) {
        Bitmap decodeResource = BitmapFactory.decodeResource(getResources(), i);
        return Bitmap.createScaledBitmap(decodeResource, (int) (decodeResource.getWidth() * f), (int) (decodeResource.getHeight() * f), false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int getStreamPosition() {
        return this.mStreamPosition;
    }

    public static boolean isLayoutRtl(View view) {
        boolean z = true;
        if (Build.VERSION.SDK_INT >= 17) {
            if (1 != view.getLayoutDirection()) {
                z = false;
            }
            return z;
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setStreamPosition(int i) {
        this.mStreamPosition = i;
        invalidate();
    }

    private void startStreamAnimation() {
        cancelStreamAnimation();
        int streamPosition = getStreamPosition();
        int length = length();
        int i = length - streamPosition;
        if (i > 0) {
            if (this.mStreamingAnimation == null) {
                this.mStreamingAnimation = new ObjectAnimator();
                this.mStreamingAnimation.setTarget(this);
                this.mStreamingAnimation.setProperty(STREAM_POSITION_PROPERTY);
            }
            this.mStreamingAnimation.setIntValues(streamPosition, length);
            this.mStreamingAnimation.setDuration(i * 50);
            this.mStreamingAnimation.start();
        }
    }

    private void updateText(CharSequence charSequence) {
        setText(charSequence);
        bringPointIntoView(length());
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mOneDot = getScaledBitmap(R$drawable.lb_text_dot_one, 1.3f);
        this.mTwoDot = getScaledBitmap(R$drawable.lb_text_dot_two, 1.3f);
        reset();
    }

    @Override // android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        accessibilityNodeInfo.setClassName(StreamingTextView.class.getCanonicalName());
    }

    public void reset() {
        this.mStreamPosition = -1;
        cancelStreamAnimation();
        setText("");
    }

    public void updateRecognizedText(String str, String str2) {
        String str3 = str;
        if (str == null) {
            str3 = "";
        }
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(str3);
        if (str2 != null) {
            int length = spannableStringBuilder.length();
            spannableStringBuilder.append((CharSequence) str2);
            addDottySpans(spannableStringBuilder, str2, length);
        }
        this.mStreamPosition = Math.max(str3.length(), this.mStreamPosition);
        updateText(new SpannedString(spannableStringBuilder));
        startStreamAnimation();
    }
}
