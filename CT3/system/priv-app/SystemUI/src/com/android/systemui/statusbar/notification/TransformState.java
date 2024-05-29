package com.android.systemui.statusbar.notification;

import android.util.ArraySet;
import android.util.Pools;
import android.view.NotificationHeaderView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.systemui.Interpolators;
import com.android.systemui.statusbar.CrossFadeHelper;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.ViewTransformationHelper;
/* loaded from: a.zip:com/android/systemui/statusbar/notification/TransformState.class */
public class TransformState {
    private static Pools.SimplePool<TransformState> sInstancePool = new Pools.SimplePool<>(40);
    protected View mTransformedView;
    private int[] mOwnPosition = new int[2];
    private float mTransformationEndY = -1.0f;
    private float mTransformationEndX = -1.0f;

    public static TransformState createFrom(View view) {
        if (view instanceof TextView) {
            TextViewTransformState obtain = TextViewTransformState.obtain();
            obtain.initFrom(view);
            return obtain;
        } else if (view.getId() == 16909218) {
            ActionListTransformState obtain2 = ActionListTransformState.obtain();
            obtain2.initFrom(view);
            return obtain2;
        } else if (view instanceof NotificationHeaderView) {
            HeaderTransformState obtain3 = HeaderTransformState.obtain();
            obtain3.initFrom(view);
            return obtain3;
        } else if (view instanceof ImageView) {
            ImageTransformState obtain4 = ImageTransformState.obtain();
            obtain4.initFrom(view);
            return obtain4;
        } else if (view instanceof ProgressBar) {
            ProgressTransformState obtain5 = ProgressTransformState.obtain();
            obtain5.initFrom(view);
            return obtain5;
        } else {
            TransformState obtain6 = obtain();
            obtain6.initFrom(view);
            return obtain6;
        }
    }

    public static TransformState obtain() {
        TransformState transformState = (TransformState) sInstancePool.acquire();
        return transformState != null ? transformState : new TransformState();
    }

    public static void setClippingDeactivated(View view, boolean z) {
        if (!(view.getParent() instanceof ViewGroup)) {
            return;
        }
        ViewParent parent = view.getParent();
        while (true) {
            ViewGroup viewGroup = (ViewGroup) parent;
            ArraySet arraySet = (ArraySet) viewGroup.getTag(2131886144);
            ArraySet arraySet2 = arraySet;
            if (arraySet == null) {
                arraySet2 = new ArraySet();
                viewGroup.setTag(2131886144, arraySet2);
            }
            Boolean bool = (Boolean) viewGroup.getTag(2131886143);
            Boolean bool2 = bool;
            if (bool == null) {
                bool2 = Boolean.valueOf(viewGroup.getClipChildren());
                viewGroup.setTag(2131886143, bool2);
            }
            Boolean bool3 = (Boolean) viewGroup.getTag(2131886145);
            Boolean bool4 = bool3;
            if (bool3 == null) {
                bool4 = Boolean.valueOf(viewGroup.getClipToPadding());
                viewGroup.setTag(2131886145, bool4);
            }
            ExpandableNotificationRow expandableNotificationRow = viewGroup instanceof ExpandableNotificationRow ? (ExpandableNotificationRow) viewGroup : null;
            if (z) {
                arraySet2.add(view);
                viewGroup.setClipChildren(false);
                viewGroup.setClipToPadding(false);
                if (expandableNotificationRow != null && expandableNotificationRow.isChildInGroup()) {
                    expandableNotificationRow.setClipToActualHeight(false);
                }
            } else {
                arraySet2.remove(view);
                if (arraySet2.isEmpty()) {
                    viewGroup.setClipChildren(bool2.booleanValue());
                    viewGroup.setClipToPadding(bool4.booleanValue());
                    viewGroup.setTag(2131886144, null);
                    if (expandableNotificationRow != null) {
                        expandableNotificationRow.setClipToActualHeight(true);
                    }
                }
            }
            if (expandableNotificationRow != null && !expandableNotificationRow.isChildInGroup()) {
                return;
            }
            ViewParent parent2 = viewGroup.getParent();
            if (!(parent2 instanceof ViewGroup)) {
                return;
            }
            parent = parent2;
        }
    }

    private void setTransformationStartScaleX(float f) {
        this.mTransformedView.setTag(2131886138, Float.valueOf(f));
    }

    private void setTransformationStartScaleY(float f) {
        this.mTransformedView.setTag(2131886139, Float.valueOf(f));
    }

    private void transformViewFrom(TransformState transformState, int i, ViewTransformationHelper.CustomTransformation customTransformation, float f) {
        View view = this.mTransformedView;
        boolean z = (i & 1) != 0;
        boolean z2 = (i & 16) != 0;
        boolean transformScale = transformScale();
        if (f == 0.0f || ((z && getTransformationStartX() == -1.0f) || ((z2 && getTransformationStartY() == -1.0f) || ((transformScale && getTransformationStartScaleX() == -1.0f) || (transformScale && getTransformationStartScaleY() == -1.0f))))) {
            int[] laidOutLocationOnScreen = f != 0.0f ? transformState.getLaidOutLocationOnScreen() : transformState.getLocationOnScreen();
            int[] laidOutLocationOnScreen2 = getLaidOutLocationOnScreen();
            if (customTransformation == null || !customTransformation.initTransformation(this, transformState)) {
                if (z) {
                    setTransformationStartX(laidOutLocationOnScreen[0] - laidOutLocationOnScreen2[0]);
                }
                if (z2) {
                    setTransformationStartY(laidOutLocationOnScreen[1] - laidOutLocationOnScreen2[1]);
                }
                View transformedView = transformState.getTransformedView();
                if (!transformScale || transformedView.getWidth() == view.getWidth()) {
                    setTransformationStartScaleX(-1.0f);
                } else {
                    setTransformationStartScaleX((transformedView.getWidth() * transformedView.getScaleX()) / view.getWidth());
                    view.setPivotX(0.0f);
                }
                if (!transformScale || transformedView.getHeight() == view.getHeight()) {
                    setTransformationStartScaleY(-1.0f);
                } else {
                    setTransformationStartScaleY((transformedView.getHeight() * transformedView.getScaleY()) / view.getHeight());
                    view.setPivotY(0.0f);
                }
            }
            if (!z) {
                setTransformationStartX(-1.0f);
            }
            if (!z2) {
                setTransformationStartY(-1.0f);
            }
            if (!transformScale) {
                setTransformationStartScaleX(-1.0f);
                setTransformationStartScaleY(-1.0f);
            }
            setClippingDeactivated(view, true);
        }
        float interpolation = Interpolators.FAST_OUT_SLOW_IN.getInterpolation(f);
        if (z) {
            view.setTranslationX(NotificationUtils.interpolate(getTransformationStartX(), 0.0f, interpolation));
        }
        if (z2) {
            view.setTranslationY(NotificationUtils.interpolate(getTransformationStartY(), 0.0f, interpolation));
        }
        if (transformScale) {
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

    private void transformViewTo(TransformState transformState, int i, ViewTransformationHelper.CustomTransformation customTransformation, float f) {
        View view = this.mTransformedView;
        boolean z = (i & 1) != 0;
        boolean z2 = (i & 16) != 0;
        boolean transformScale = transformScale();
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
            View transformedView = transformState.getTransformedView();
            if (!transformScale || transformedView.getWidth() == view.getWidth()) {
                setTransformationStartScaleX(-1.0f);
            } else {
                setTransformationStartScaleX(view.getScaleX());
                view.setPivotX(0.0f);
            }
            if (!transformScale || transformedView.getHeight() == view.getHeight()) {
                setTransformationStartScaleY(-1.0f);
            } else {
                setTransformationStartScaleY(view.getScaleY());
                view.setPivotY(0.0f);
            }
            setClippingDeactivated(view, true);
        }
        float interpolation = Interpolators.FAST_OUT_SLOW_IN.getInterpolation(f);
        int[] laidOutLocationOnScreen = transformState.getLaidOutLocationOnScreen();
        int[] laidOutLocationOnScreen2 = getLaidOutLocationOnScreen();
        if (z) {
            float f2 = laidOutLocationOnScreen[0] - laidOutLocationOnScreen2[0];
            float f3 = f2;
            if (customTransformation != null) {
                f3 = f2;
                if (customTransformation.customTransformTarget(this, transformState)) {
                    f3 = this.mTransformationEndX;
                }
            }
            view.setTranslationX(NotificationUtils.interpolate(getTransformationStartX(), f3, interpolation));
        }
        if (z2) {
            float f4 = laidOutLocationOnScreen[1] - laidOutLocationOnScreen2[1];
            float f5 = f4;
            if (customTransformation != null) {
                f5 = f4;
                if (customTransformation.customTransformTarget(this, transformState)) {
                    f5 = this.mTransformationEndY;
                }
            }
            view.setTranslationY(NotificationUtils.interpolate(getTransformationStartY(), f5, interpolation));
        }
        if (transformScale) {
            View transformedView2 = transformState.getTransformedView();
            float transformationStartScaleX = getTransformationStartScaleX();
            if (transformationStartScaleX != -1.0f) {
                view.setScaleX(NotificationUtils.interpolate(transformationStartScaleX, transformedView2.getWidth() / view.getWidth(), interpolation));
            }
            float transformationStartScaleY = getTransformationStartScaleY();
            if (transformationStartScaleY != -1.0f) {
                view.setScaleY(NotificationUtils.interpolate(transformationStartScaleY, transformedView2.getHeight() / view.getHeight(), interpolation));
            }
        }
    }

    public void abortTransformation() {
        this.mTransformedView.setTag(2131886136, Float.valueOf(-1.0f));
        this.mTransformedView.setTag(2131886137, Float.valueOf(-1.0f));
        this.mTransformedView.setTag(2131886138, Float.valueOf(-1.0f));
        this.mTransformedView.setTag(2131886139, Float.valueOf(-1.0f));
    }

    public int[] getLaidOutLocationOnScreen() {
        int[] locationOnScreen = getLocationOnScreen();
        locationOnScreen[0] = (int) (locationOnScreen[0] - this.mTransformedView.getTranslationX());
        locationOnScreen[1] = (int) (locationOnScreen[1] - this.mTransformedView.getTranslationY());
        return locationOnScreen;
    }

    public int[] getLocationOnScreen() {
        this.mTransformedView.getLocationOnScreen(this.mOwnPosition);
        return this.mOwnPosition;
    }

    public float getTransformationStartScaleX() {
        Object tag = this.mTransformedView.getTag(2131886138);
        return tag == null ? -1.0f : ((Float) tag).floatValue();
    }

    public float getTransformationStartScaleY() {
        Object tag = this.mTransformedView.getTag(2131886139);
        return tag == null ? -1.0f : ((Float) tag).floatValue();
    }

    public float getTransformationStartX() {
        Object tag = this.mTransformedView.getTag(2131886136);
        return tag == null ? -1.0f : ((Float) tag).floatValue();
    }

    public float getTransformationStartY() {
        Object tag = this.mTransformedView.getTag(2131886137);
        return tag == null ? -1.0f : ((Float) tag).floatValue();
    }

    public View getTransformedView() {
        return this.mTransformedView;
    }

    public void initFrom(View view) {
        this.mTransformedView = view;
    }

    public void prepareFadeIn() {
        resetTransformedView();
    }

    public void recycle() {
        reset();
        if (getClass() == TransformState.class) {
            sInstancePool.release(this);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void reset() {
        this.mTransformedView = null;
        this.mTransformationEndX = -1.0f;
        this.mTransformationEndY = -1.0f;
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

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean sameAs(TransformState transformState) {
        return false;
    }

    public void setTransformationEndY(float f) {
        this.mTransformationEndY = f;
    }

    public void setTransformationStartX(float f) {
        this.mTransformedView.setTag(2131886136, Float.valueOf(f));
    }

    public void setTransformationStartY(float f) {
        this.mTransformedView.setTag(2131886137, Float.valueOf(f));
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

    protected boolean transformScale() {
        return false;
    }

    public void transformViewFrom(TransformState transformState, float f) {
        this.mTransformedView.animate().cancel();
        if (!sameAs(transformState)) {
            CrossFadeHelper.fadeIn(this.mTransformedView, f);
        } else if (this.mTransformedView.getVisibility() == 4) {
            this.mTransformedView.setAlpha(1.0f);
            this.mTransformedView.setVisibility(0);
        }
        transformViewFullyFrom(transformState, f);
    }

    public void transformViewFullyFrom(TransformState transformState, float f) {
        transformViewFrom(transformState, 17, null, f);
    }

    public void transformViewFullyTo(TransformState transformState, float f) {
        transformViewTo(transformState, 17, null, f);
    }

    public boolean transformViewTo(TransformState transformState, float f) {
        this.mTransformedView.animate().cancel();
        if (!sameAs(transformState)) {
            CrossFadeHelper.fadeOut(this.mTransformedView, f);
            transformViewFullyTo(transformState, f);
            return true;
        } else if (this.mTransformedView.getVisibility() == 0) {
            this.mTransformedView.setAlpha(0.0f);
            this.mTransformedView.setVisibility(4);
            return false;
        } else {
            return false;
        }
    }

    public void transformViewVerticalFrom(TransformState transformState, float f) {
        transformViewFrom(transformState, 16, null, f);
    }

    public void transformViewVerticalFrom(TransformState transformState, ViewTransformationHelper.CustomTransformation customTransformation, float f) {
        transformViewFrom(transformState, 16, customTransformation, f);
    }

    public void transformViewVerticalTo(TransformState transformState, float f) {
        transformViewTo(transformState, 16, null, f);
    }

    public void transformViewVerticalTo(TransformState transformState, ViewTransformationHelper.CustomTransformation customTransformation, float f) {
        transformViewTo(transformState, 16, customTransformation, f);
    }
}
