package com.android.systemui.statusbar.notification;

import android.util.Pools;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.internal.widget.MessagingImageMessage;
import com.android.internal.widget.MessagingPropertyAnimator;
import com.android.internal.widget.ViewClippingUtil;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.statusbar.CrossFadeHelper;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.TransformableView;
import com.android.systemui.statusbar.ViewTransformationHelper;
/* loaded from: classes.dex */
public class TransformState {
    private boolean mSameAsAny;
    protected TransformInfo mTransformInfo;
    protected View mTransformedView;
    private static Pools.SimplePool<TransformState> sInstancePool = new Pools.SimplePool<>(40);
    private static ViewClippingUtil.ClippingParameters CLIPPING_PARAMETERS = new ViewClippingUtil.ClippingParameters() { // from class: com.android.systemui.statusbar.notification.TransformState.1
        public boolean shouldFinish(View view) {
            if (view instanceof ExpandableNotificationRow) {
                return !((ExpandableNotificationRow) view).isChildInGroup();
            }
            return false;
        }

        public void onClippingStateChanged(View view, boolean z) {
            if (view instanceof ExpandableNotificationRow) {
                ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
                if (z) {
                    expandableNotificationRow.setClipToActualHeight(true);
                } else if (expandableNotificationRow.isChildInGroup()) {
                    expandableNotificationRow.setClipToActualHeight(false);
                }
            }
        }
    };
    private int[] mOwnPosition = new int[2];
    private float mTransformationEndY = -1.0f;
    private float mTransformationEndX = -1.0f;
    protected Interpolator mDefaultInterpolator = Interpolators.FAST_OUT_SLOW_IN;

    /* loaded from: classes.dex */
    public interface TransformInfo {
        boolean isAnimating();
    }

    public void initFrom(View view, TransformInfo transformInfo) {
        this.mTransformedView = view;
        this.mTransformInfo = transformInfo;
    }

    public void transformViewFrom(TransformState transformState, float f) {
        this.mTransformedView.animate().cancel();
        if (sameAs(transformState)) {
            ensureVisible();
        } else {
            CrossFadeHelper.fadeIn(this.mTransformedView, f);
        }
        transformViewFullyFrom(transformState, f);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void ensureVisible() {
        if (this.mTransformedView.getVisibility() == 4 || this.mTransformedView.getAlpha() != 1.0f) {
            this.mTransformedView.setAlpha(1.0f);
            this.mTransformedView.setVisibility(0);
        }
    }

    public void transformViewFullyFrom(TransformState transformState, float f) {
        transformViewFrom(transformState, 17, null, f);
    }

    public void transformViewFullyFrom(TransformState transformState, ViewTransformationHelper.CustomTransformation customTransformation, float f) {
        transformViewFrom(transformState, 17, customTransformation, f);
    }

    public void transformViewVerticalFrom(TransformState transformState, ViewTransformationHelper.CustomTransformation customTransformation, float f) {
        transformViewFrom(transformState, 16, customTransformation, f);
    }

    public void transformViewVerticalFrom(TransformState transformState, float f) {
        transformViewFrom(transformState, 16, null, f);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Code restructure failed: missing block: B:50:0x0098, code lost:
        if (r22.initTransformation(r19, r20) != false) goto L70;
     */
    /* JADX WARN: Removed duplicated region for block: B:68:0x00f0  */
    /* JADX WARN: Removed duplicated region for block: B:70:0x00f5  */
    /* JADX WARN: Removed duplicated region for block: B:72:0x00fa  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void transformViewFrom(TransformState transformState, int i, ViewTransformationHelper.CustomTransformation customTransformation, float f) {
        int[] locationOnScreen;
        float f2;
        float f3;
        Interpolator customInterpolator;
        float f4;
        Interpolator customInterpolator2;
        View view = this.mTransformedView;
        boolean z = (i & 1) != 0;
        boolean z2 = (i & 16) != 0;
        int viewHeight = getViewHeight();
        int viewHeight2 = transformState.getViewHeight();
        boolean z3 = (viewHeight2 == viewHeight || viewHeight2 == 0 || viewHeight == 0) ? false : true;
        int viewWidth = getViewWidth();
        int viewWidth2 = transformState.getViewWidth();
        boolean z4 = (viewWidth2 == viewWidth || viewWidth2 == 0 || viewWidth == 0) ? false : true;
        boolean z5 = transformScale(transformState) && (z3 || z4);
        int i2 = (f > 0.0f ? 1 : (f == 0.0f ? 0 : -1));
        if (i2 == 0 || ((z && getTransformationStartX() == -1.0f) || ((z2 && getTransformationStartY() == -1.0f) || ((z5 && getTransformationStartScaleX() == -1.0f && z4) || (z5 && getTransformationStartScaleY() == -1.0f && z3))))) {
            if (i2 != 0) {
                locationOnScreen = transformState.getLaidOutLocationOnScreen();
            } else {
                locationOnScreen = transformState.getLocationOnScreen();
            }
            int[] laidOutLocationOnScreen = getLaidOutLocationOnScreen();
            if (customTransformation == null) {
            }
            if (z) {
                setTransformationStartX(locationOnScreen[0] - laidOutLocationOnScreen[0]);
            }
            if (z2) {
                setTransformationStartY(locationOnScreen[1] - laidOutLocationOnScreen[1]);
            }
            View transformedView = transformState.getTransformedView();
            if (z5 && z4) {
                setTransformationStartScaleX((viewWidth2 * transformedView.getScaleX()) / viewWidth);
                view.setPivotX(0.0f);
            } else {
                setTransformationStartScaleX(-1.0f);
            }
            if (z5 && z3) {
                setTransformationStartScaleY((viewHeight2 * transformedView.getScaleY()) / viewHeight);
                view.setPivotY(0.0f);
                f2 = -1.0f;
                if (!z) {
                }
                if (!z2) {
                }
                if (!z5) {
                }
                setClippingDeactivated(view, true);
            } else {
                f2 = -1.0f;
                setTransformationStartScaleY(-1.0f);
                if (!z) {
                    setTransformationStartX(f2);
                }
                if (!z2) {
                    setTransformationStartY(f2);
                }
                if (!z5) {
                    setTransformationStartScaleX(f2);
                    setTransformationStartScaleY(f2);
                }
                setClippingDeactivated(view, true);
            }
        }
        float interpolation = this.mDefaultInterpolator.getInterpolation(f);
        if (z) {
            if (customTransformation != null && (customInterpolator2 = customTransformation.getCustomInterpolator(1, true)) != null) {
                f4 = customInterpolator2.getInterpolation(f);
            } else {
                f4 = interpolation;
            }
            view.setTranslationX(NotificationUtils.interpolate(getTransformationStartX(), 0.0f, f4));
        }
        if (z2) {
            if (customTransformation != null && (customInterpolator = customTransformation.getCustomInterpolator(16, true)) != null) {
                f3 = customInterpolator.getInterpolation(f);
            } else {
                f3 = interpolation;
            }
            view.setTranslationY(NotificationUtils.interpolate(getTransformationStartY(), 0.0f, f3));
        }
        if (z5) {
            float transformationStartScaleX = getTransformationStartScaleX();
            if (transformationStartScaleX != -1.0f) {
                view.setScaleX(NotificationUtils.interpolate(transformationStartScaleX, 1.0f, interpolation));
            }
            float transformationStartScaleY = getTransformationStartScaleY();
            if (transformationStartScaleY != -1.0f) {
                view.setScaleY(NotificationUtils.interpolate(transformationStartScaleY, 1.0f, interpolation));
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public int getViewWidth() {
        return this.mTransformedView.getWidth();
    }

    protected int getViewHeight() {
        return this.mTransformedView.getHeight();
    }

    protected boolean transformScale(TransformState transformState) {
        return false;
    }

    public boolean transformViewTo(TransformState transformState, float f) {
        this.mTransformedView.animate().cancel();
        if (sameAs(transformState)) {
            if (this.mTransformedView.getVisibility() == 0) {
                this.mTransformedView.setAlpha(0.0f);
                this.mTransformedView.setVisibility(4);
                return false;
            }
            return false;
        }
        CrossFadeHelper.fadeOut(this.mTransformedView, f);
        transformViewFullyTo(transformState, f);
        return true;
    }

    public void transformViewFullyTo(TransformState transformState, float f) {
        transformViewTo(transformState, 17, null, f);
    }

    public void transformViewFullyTo(TransformState transformState, ViewTransformationHelper.CustomTransformation customTransformation, float f) {
        transformViewTo(transformState, 17, customTransformation, f);
    }

    public void transformViewVerticalTo(TransformState transformState, ViewTransformationHelper.CustomTransformation customTransformation, float f) {
        transformViewTo(transformState, 16, customTransformation, f);
    }

    public void transformViewVerticalTo(TransformState transformState, float f) {
        transformViewTo(transformState, 16, null, f);
    }

    private void transformViewTo(TransformState transformState, int i, ViewTransformationHelper.CustomTransformation customTransformation, float f) {
        float f2;
        float f3;
        View view = this.mTransformedView;
        boolean z = (i & 1) != 0;
        boolean z2 = (i & 16) != 0;
        boolean transformScale = transformScale(transformState);
        if (f == 0.0f) {
            if (z) {
                float transformationStartX = getTransformationStartX();
                if (transformationStartX == -1.0f) {
                    transformationStartX = view.getTranslationX();
                }
                setTransformationStartX(transformationStartX);
            }
            if (z2) {
                float transformationStartY = getTransformationStartY();
                if (transformationStartY == -1.0f) {
                    transformationStartY = view.getTranslationY();
                }
                setTransformationStartY(transformationStartY);
            }
            transformState.getTransformedView();
            if (transformScale && transformState.getViewWidth() != getViewWidth()) {
                setTransformationStartScaleX(view.getScaleX());
                view.setPivotX(0.0f);
            } else {
                setTransformationStartScaleX(-1.0f);
            }
            if (transformScale && transformState.getViewHeight() != getViewHeight()) {
                setTransformationStartScaleY(view.getScaleY());
                view.setPivotY(0.0f);
            } else {
                setTransformationStartScaleY(-1.0f);
            }
            setClippingDeactivated(view, true);
        }
        float interpolation = this.mDefaultInterpolator.getInterpolation(f);
        int[] laidOutLocationOnScreen = transformState.getLaidOutLocationOnScreen();
        int[] laidOutLocationOnScreen2 = getLaidOutLocationOnScreen();
        if (z) {
            float f4 = laidOutLocationOnScreen[0] - laidOutLocationOnScreen2[0];
            if (customTransformation != null) {
                if (customTransformation.customTransformTarget(this, transformState)) {
                    f4 = this.mTransformationEndX;
                }
                Interpolator customInterpolator = customTransformation.getCustomInterpolator(1, false);
                if (customInterpolator != null) {
                    f3 = customInterpolator.getInterpolation(f);
                    view.setTranslationX(NotificationUtils.interpolate(getTransformationStartX(), f4, f3));
                }
            }
            f3 = interpolation;
            view.setTranslationX(NotificationUtils.interpolate(getTransformationStartX(), f4, f3));
        }
        if (z2) {
            float f5 = laidOutLocationOnScreen[1] - laidOutLocationOnScreen2[1];
            if (customTransformation != null) {
                if (customTransformation.customTransformTarget(this, transformState)) {
                    f5 = this.mTransformationEndY;
                }
                Interpolator customInterpolator2 = customTransformation.getCustomInterpolator(16, false);
                if (customInterpolator2 != null) {
                    f2 = customInterpolator2.getInterpolation(f);
                    view.setTranslationY(NotificationUtils.interpolate(getTransformationStartY(), f5, f2));
                }
            }
            f2 = interpolation;
            view.setTranslationY(NotificationUtils.interpolate(getTransformationStartY(), f5, f2));
        }
        if (transformScale) {
            transformState.getTransformedView();
            float transformationStartScaleX = getTransformationStartScaleX();
            if (transformationStartScaleX != -1.0f) {
                view.setScaleX(NotificationUtils.interpolate(transformationStartScaleX, transformState.getViewWidth() / getViewWidth(), interpolation));
            }
            float transformationStartScaleY = getTransformationStartScaleY();
            if (transformationStartScaleY != -1.0f) {
                view.setScaleY(NotificationUtils.interpolate(transformationStartScaleY, transformState.getViewHeight() / getViewHeight(), interpolation));
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setClippingDeactivated(View view, boolean z) {
        ViewClippingUtil.setClippingDeactivated(view, z, CLIPPING_PARAMETERS);
    }

    public int[] getLaidOutLocationOnScreen() {
        int[] locationOnScreen = getLocationOnScreen();
        locationOnScreen[0] = (int) (locationOnScreen[0] - this.mTransformedView.getTranslationX());
        locationOnScreen[1] = (int) (locationOnScreen[1] - this.mTransformedView.getTranslationY());
        return locationOnScreen;
    }

    public int[] getLocationOnScreen() {
        this.mTransformedView.getLocationOnScreen(this.mOwnPosition);
        int[] iArr = this.mOwnPosition;
        iArr[0] = (int) (iArr[0] - ((1.0f - this.mTransformedView.getScaleX()) * this.mTransformedView.getPivotX()));
        int[] iArr2 = this.mOwnPosition;
        iArr2[1] = (int) (iArr2[1] - ((1.0f - this.mTransformedView.getScaleY()) * this.mTransformedView.getPivotY()));
        int[] iArr3 = this.mOwnPosition;
        iArr3[1] = iArr3[1] - (MessagingPropertyAnimator.getTop(this.mTransformedView) - MessagingPropertyAnimator.getLayoutTop(this.mTransformedView));
        return this.mOwnPosition;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean sameAs(TransformState transformState) {
        return this.mSameAsAny;
    }

    public void appear(float f, TransformableView transformableView) {
        if (f == 0.0f) {
            prepareFadeIn();
        }
        CrossFadeHelper.fadeIn(this.mTransformedView, f);
    }

    public void disappear(float f, TransformableView transformableView) {
        CrossFadeHelper.fadeOut(this.mTransformedView, f);
    }

    public static TransformState createFrom(View view, TransformInfo transformInfo) {
        if (view instanceof TextView) {
            TextViewTransformState obtain = TextViewTransformState.obtain();
            obtain.initFrom(view, transformInfo);
            return obtain;
        } else if (view.getId() == 16908685) {
            ActionListTransformState obtain2 = ActionListTransformState.obtain();
            obtain2.initFrom(view, transformInfo);
            return obtain2;
        } else if (view.getId() == 16909125) {
            MessagingLayoutTransformState obtain3 = MessagingLayoutTransformState.obtain();
            obtain3.initFrom(view, transformInfo);
            return obtain3;
        } else if (view instanceof MessagingImageMessage) {
            MessagingImageTransformState obtain4 = MessagingImageTransformState.obtain();
            obtain4.initFrom(view, transformInfo);
            return obtain4;
        } else if (view instanceof ImageView) {
            ImageTransformState obtain5 = ImageTransformState.obtain();
            obtain5.initFrom(view, transformInfo);
            if (view.getId() == 16909229) {
                obtain5.setIsSameAsAnyView(true);
            }
            return obtain5;
        } else if (view instanceof ProgressBar) {
            ProgressTransformState obtain6 = ProgressTransformState.obtain();
            obtain6.initFrom(view, transformInfo);
            return obtain6;
        } else {
            TransformState obtain7 = obtain();
            obtain7.initFrom(view, transformInfo);
            return obtain7;
        }
    }

    public void setIsSameAsAnyView(boolean z) {
        this.mSameAsAny = z;
    }

    public void recycle() {
        reset();
        if (getClass() == TransformState.class) {
            sInstancePool.release(this);
        }
    }

    public void setTransformationEndY(float f) {
        this.mTransformationEndY = f;
    }

    public float getTransformationStartX() {
        Object tag = this.mTransformedView.getTag(R.id.transformation_start_x_tag);
        if (tag == null) {
            return -1.0f;
        }
        return ((Float) tag).floatValue();
    }

    public float getTransformationStartY() {
        Object tag = this.mTransformedView.getTag(R.id.transformation_start_y_tag);
        if (tag == null) {
            return -1.0f;
        }
        return ((Float) tag).floatValue();
    }

    public float getTransformationStartScaleX() {
        Object tag = this.mTransformedView.getTag(R.id.transformation_start_scale_x_tag);
        if (tag == null) {
            return -1.0f;
        }
        return ((Float) tag).floatValue();
    }

    public float getTransformationStartScaleY() {
        Object tag = this.mTransformedView.getTag(R.id.transformation_start_scale_y_tag);
        if (tag == null) {
            return -1.0f;
        }
        return ((Float) tag).floatValue();
    }

    public void setTransformationStartX(float f) {
        this.mTransformedView.setTag(R.id.transformation_start_x_tag, Float.valueOf(f));
    }

    public void setTransformationStartY(float f) {
        this.mTransformedView.setTag(R.id.transformation_start_y_tag, Float.valueOf(f));
    }

    private void setTransformationStartScaleX(float f) {
        this.mTransformedView.setTag(R.id.transformation_start_scale_x_tag, Float.valueOf(f));
    }

    private void setTransformationStartScaleY(float f) {
        this.mTransformedView.setTag(R.id.transformation_start_scale_y_tag, Float.valueOf(f));
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void reset() {
        this.mTransformedView = null;
        this.mTransformInfo = null;
        this.mSameAsAny = false;
        this.mTransformationEndX = -1.0f;
        this.mTransformationEndY = -1.0f;
        this.mDefaultInterpolator = Interpolators.FAST_OUT_SLOW_IN;
    }

    public void setVisible(boolean z, boolean z2) {
        if (z2 || this.mTransformedView.getVisibility() != 8) {
            if (this.mTransformedView.getVisibility() != 8) {
                this.mTransformedView.setVisibility(z ? 0 : 4);
            }
            this.mTransformedView.animate().cancel();
            this.mTransformedView.setAlpha(z ? 1.0f : 0.0f);
            resetTransformedView();
        }
    }

    public void prepareFadeIn() {
        resetTransformedView();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void resetTransformedView() {
        this.mTransformedView.setTranslationX(0.0f);
        this.mTransformedView.setTranslationY(0.0f);
        this.mTransformedView.setScaleX(1.0f);
        this.mTransformedView.setScaleY(1.0f);
        setClippingDeactivated(this.mTransformedView, false);
        abortTransformation();
    }

    public void abortTransformation() {
        this.mTransformedView.setTag(R.id.transformation_start_x_tag, Float.valueOf(-1.0f));
        this.mTransformedView.setTag(R.id.transformation_start_y_tag, Float.valueOf(-1.0f));
        this.mTransformedView.setTag(R.id.transformation_start_scale_x_tag, Float.valueOf(-1.0f));
        this.mTransformedView.setTag(R.id.transformation_start_scale_y_tag, Float.valueOf(-1.0f));
    }

    public static TransformState obtain() {
        TransformState transformState = (TransformState) sInstancePool.acquire();
        if (transformState != null) {
            return transformState;
        }
        return new TransformState();
    }

    public View getTransformedView() {
        return this.mTransformedView;
    }

    public void setDefaultInterpolator(Interpolator interpolator) {
        this.mDefaultInterpolator = interpolator;
    }
}
