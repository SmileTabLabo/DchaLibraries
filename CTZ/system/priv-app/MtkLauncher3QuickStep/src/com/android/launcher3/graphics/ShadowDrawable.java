package com.android.launcher3.graphics;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
@TargetApi(26)
/* loaded from: classes.dex */
public class ShadowDrawable extends Drawable {
    private final Paint mPaint;
    private final ShadowDrawableState mState;

    public ShadowDrawable() {
        this(new ShadowDrawableState());
    }

    private ShadowDrawable(ShadowDrawableState shadowDrawableState) {
        this.mPaint = new Paint(3);
        this.mState = shadowDrawableState;
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();
        if (bounds.isEmpty()) {
            return;
        }
        if (this.mState.mLastDrawnBitmap == null) {
            regenerateBitmapCache();
        }
        canvas.drawBitmap(this.mState.mLastDrawnBitmap, (Rect) null, bounds, this.mPaint);
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int i) {
        this.mPaint.setAlpha(i);
        invalidateSelf();
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter colorFilter) {
        this.mPaint.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override // android.graphics.drawable.Drawable
    public Drawable.ConstantState getConstantState() {
        return this.mState;
    }

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        return -3;
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicHeight() {
        return this.mState.mIntrinsicHeight;
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicWidth() {
        return this.mState.mIntrinsicWidth;
    }

    @Override // android.graphics.drawable.Drawable
    public boolean canApplyTheme() {
        return this.mState.canApplyTheme();
    }

    @Override // android.graphics.drawable.Drawable
    public void applyTheme(Resources.Theme theme) {
        TypedArray obtainStyledAttributes = theme.obtainStyledAttributes(new int[]{R.attr.isWorkspaceDarkText});
        boolean z = obtainStyledAttributes.getBoolean(0, false);
        obtainStyledAttributes.recycle();
        if (this.mState.mIsDark != z) {
            this.mState.mIsDark = z;
            this.mState.mLastDrawnBitmap = null;
            invalidateSelf();
        }
    }

    private void regenerateBitmapCache() {
        Bitmap createBitmap = Bitmap.createBitmap(this.mState.mIntrinsicWidth, this.mState.mIntrinsicHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        Drawable mutate = this.mState.mChildState.newDrawable().mutate();
        mutate.setBounds(this.mState.mShadowSize, this.mState.mShadowSize, this.mState.mIntrinsicWidth - this.mState.mShadowSize, this.mState.mIntrinsicHeight - this.mState.mShadowSize);
        mutate.setTint(this.mState.mIsDark ? this.mState.mDarkTintColor : -1);
        mutate.draw(canvas);
        if (!this.mState.mIsDark) {
            Paint paint = new Paint(3);
            paint.setMaskFilter(new BlurMaskFilter(this.mState.mShadowSize, BlurMaskFilter.Blur.NORMAL));
            int[] iArr = new int[2];
            Bitmap extractAlpha = createBitmap.extractAlpha(paint, iArr);
            paint.setMaskFilter(null);
            paint.setColor(this.mState.mShadowColor);
            createBitmap.eraseColor(0);
            canvas.drawBitmap(extractAlpha, iArr[0], iArr[1], paint);
            mutate.draw(canvas);
        }
        if (Utilities.ATLEAST_OREO) {
            createBitmap = createBitmap.copy(Bitmap.Config.HARDWARE, false);
        }
        this.mState.mLastDrawnBitmap = createBitmap;
    }

    @Override // android.graphics.drawable.Drawable
    public void inflate(Resources resources, XmlPullParser xmlPullParser, AttributeSet attributeSet, Resources.Theme theme) throws XmlPullParserException, IOException {
        TypedArray obtainStyledAttributes;
        super.inflate(resources, xmlPullParser, attributeSet, theme);
        if (theme != null) {
            obtainStyledAttributes = theme.obtainStyledAttributes(attributeSet, R.styleable.ShadowDrawable, 0, 0);
        } else {
            obtainStyledAttributes = resources.obtainAttributes(attributeSet, R.styleable.ShadowDrawable);
        }
        try {
            Drawable drawable = obtainStyledAttributes.getDrawable(0);
            if (drawable == null) {
                throw new XmlPullParserException("missing src attribute");
            }
            this.mState.mShadowColor = obtainStyledAttributes.getColor(1, ViewCompat.MEASURED_STATE_MASK);
            this.mState.mShadowSize = obtainStyledAttributes.getDimensionPixelSize(2, 0);
            this.mState.mDarkTintColor = obtainStyledAttributes.getColor(3, ViewCompat.MEASURED_STATE_MASK);
            this.mState.mIntrinsicHeight = drawable.getIntrinsicHeight() + (this.mState.mShadowSize * 2);
            this.mState.mIntrinsicWidth = drawable.getIntrinsicWidth() + (2 * this.mState.mShadowSize);
            this.mState.mChangingConfigurations = drawable.getChangingConfigurations();
            this.mState.mChildState = drawable.getConstantState();
        } finally {
            obtainStyledAttributes.recycle();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class ShadowDrawableState extends Drawable.ConstantState {
        int mChangingConfigurations;
        Drawable.ConstantState mChildState;
        int mDarkTintColor;
        int mIntrinsicHeight;
        int mIntrinsicWidth;
        boolean mIsDark;
        Bitmap mLastDrawnBitmap;
        int mShadowColor;
        int mShadowSize;

        private ShadowDrawableState() {
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public Drawable newDrawable() {
            return new ShadowDrawable(this);
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public int getChangingConfigurations() {
            return this.mChangingConfigurations;
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public boolean canApplyTheme() {
            return true;
        }
    }
}
