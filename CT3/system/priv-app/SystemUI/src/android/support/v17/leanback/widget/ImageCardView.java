package android.support.v17.leanback.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.R$attr;
import android.support.v17.leanback.R$id;
import android.support.v17.leanback.R$layout;
import android.support.v17.leanback.R$style;
import android.support.v17.leanback.R$styleable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
/* loaded from: a.zip:android/support/v17/leanback/widget/ImageCardView.class */
public class ImageCardView extends BaseCardView {
    private boolean mAttachedToWindow;
    private ImageView mBadgeImage;
    private TextView mContentView;
    private ImageView mImageView;
    private ViewGroup mInfoArea;
    private TextView mTitleView;

    public ImageCardView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R$attr.imageCardViewStyle);
    }

    public ImageCardView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        buildImageCardView(attributeSet, i, R$style.Widget_Leanback_ImageCardView);
    }

    private void buildImageCardView(AttributeSet attributeSet, int i, int i2) {
        setFocusable(true);
        setFocusableInTouchMode(true);
        LayoutInflater from = LayoutInflater.from(getContext());
        from.inflate(R$layout.lb_image_card_view, this);
        TypedArray obtainStyledAttributes = getContext().obtainStyledAttributes(attributeSet, R$styleable.lbImageCardView, i, i2);
        int i3 = obtainStyledAttributes.getInt(R$styleable.lbImageCardView_lbImageCardViewType, 0);
        boolean z = i3 == 0;
        boolean z2 = (i3 & 1) == 1;
        boolean z3 = (i3 & 2) == 2;
        boolean z4 = (i3 & 4) == 4;
        boolean z5 = !z4 && (i3 & 8) == 8;
        this.mImageView = (ImageView) findViewById(R$id.main_image);
        if (this.mImageView.getDrawable() == null) {
            this.mImageView.setVisibility(4);
        }
        this.mInfoArea = (ViewGroup) findViewById(R$id.info_field);
        if (z) {
            removeView(this.mInfoArea);
            obtainStyledAttributes.recycle();
            return;
        }
        if (z2) {
            this.mTitleView = (TextView) from.inflate(R$layout.lb_image_card_view_themed_title, this.mInfoArea, false);
            this.mInfoArea.addView(this.mTitleView);
        }
        if (z3) {
            this.mContentView = (TextView) from.inflate(R$layout.lb_image_card_view_themed_content, this.mInfoArea, false);
            this.mInfoArea.addView(this.mContentView);
        }
        if (z4 || z5) {
            int i4 = R$layout.lb_image_card_view_themed_badge_right;
            if (z5) {
                i4 = R$layout.lb_image_card_view_themed_badge_left;
            }
            this.mBadgeImage = (ImageView) from.inflate(i4, this.mInfoArea, false);
            this.mInfoArea.addView(this.mBadgeImage);
        }
        if (z2 && !z3 && this.mBadgeImage != null) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.mTitleView.getLayoutParams();
            if (z5) {
                layoutParams.addRule(17, this.mBadgeImage.getId());
            } else {
                layoutParams.addRule(16, this.mBadgeImage.getId());
            }
            this.mTitleView.setLayoutParams(layoutParams);
        }
        if (z3) {
            RelativeLayout.LayoutParams layoutParams2 = (RelativeLayout.LayoutParams) this.mContentView.getLayoutParams();
            if (!z2) {
                layoutParams2.addRule(10);
            }
            if (z5) {
                layoutParams2.removeRule(16);
                layoutParams2.removeRule(20);
                layoutParams2.addRule(17, this.mBadgeImage.getId());
            }
            this.mContentView.setLayoutParams(layoutParams2);
        }
        if (this.mBadgeImage != null) {
            RelativeLayout.LayoutParams layoutParams3 = (RelativeLayout.LayoutParams) this.mBadgeImage.getLayoutParams();
            if (z3) {
                layoutParams3.addRule(8, this.mContentView.getId());
            } else if (z2) {
                layoutParams3.addRule(8, this.mTitleView.getId());
            }
            this.mBadgeImage.setLayoutParams(layoutParams3);
        }
        Drawable drawable = obtainStyledAttributes.getDrawable(R$styleable.lbImageCardView_infoAreaBackground);
        if (drawable != null) {
            setInfoAreaBackground(drawable);
        }
        if (this.mBadgeImage != null && this.mBadgeImage.getDrawable() == null) {
            this.mBadgeImage.setVisibility(8);
        }
        obtainStyledAttributes.recycle();
    }

    private void fadeIn() {
        this.mImageView.setAlpha(0.0f);
        if (this.mAttachedToWindow) {
            this.mImageView.animate().alpha(1.0f).setDuration(this.mImageView.getResources().getInteger(17694720));
        }
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mAttachedToWindow = true;
        if (this.mImageView.getAlpha() == 0.0f) {
            fadeIn();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v17.leanback.widget.BaseCardView, android.view.ViewGroup, android.view.View
    public void onDetachedFromWindow() {
        this.mAttachedToWindow = false;
        this.mImageView.animate().cancel();
        this.mImageView.setAlpha(1.0f);
        super.onDetachedFromWindow();
    }

    public void setInfoAreaBackground(Drawable drawable) {
        if (this.mInfoArea != null) {
            this.mInfoArea.setBackground(drawable);
        }
    }
}
