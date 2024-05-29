package com.android.systemui.statusbar.notification;

import android.util.Pools;
import android.view.NotificationHeaderView;
import android.view.View;
import com.android.systemui.statusbar.CrossFadeHelper;
/* loaded from: a.zip:com/android/systemui/statusbar/notification/HeaderTransformState.class */
public class HeaderTransformState extends TransformState {
    private static Pools.SimplePool<HeaderTransformState> sInstancePool = new Pools.SimplePool<>(40);
    private View mExpandButton;
    private View mWorkProfileIcon;
    private TransformState mWorkProfileState;

    public static HeaderTransformState obtain() {
        HeaderTransformState headerTransformState = (HeaderTransformState) sInstancePool.acquire();
        return headerTransformState != null ? headerTransformState : new HeaderTransformState();
    }

    @Override // com.android.systemui.statusbar.notification.TransformState
    public void initFrom(View view) {
        super.initFrom(view);
        if (view instanceof NotificationHeaderView) {
            NotificationHeaderView notificationHeaderView = (NotificationHeaderView) view;
            this.mExpandButton = notificationHeaderView.getExpandButton();
            this.mWorkProfileState = TransformState.obtain();
            this.mWorkProfileIcon = notificationHeaderView.getWorkProfileIcon();
            this.mWorkProfileState.initFrom(this.mWorkProfileIcon);
        }
    }

    @Override // com.android.systemui.statusbar.notification.TransformState
    public void prepareFadeIn() {
        super.prepareFadeIn();
        if (this.mTransformedView instanceof NotificationHeaderView) {
            NotificationHeaderView notificationHeaderView = this.mTransformedView;
            int childCount = notificationHeaderView.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childAt = notificationHeaderView.getChildAt(i);
                if (childAt.getVisibility() != 8) {
                    childAt.animate().cancel();
                    childAt.setVisibility(0);
                    childAt.setAlpha(1.0f);
                    if (childAt == this.mWorkProfileIcon) {
                        childAt.setTranslationX(0.0f);
                        childAt.setTranslationY(0.0f);
                    }
                }
            }
        }
    }

    @Override // com.android.systemui.statusbar.notification.TransformState
    public void recycle() {
        super.recycle();
        sInstancePool.release(this);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.TransformState
    public void reset() {
        super.reset();
        this.mExpandButton = null;
        this.mWorkProfileState = null;
        if (this.mWorkProfileState != null) {
            this.mWorkProfileState.recycle();
            this.mWorkProfileState = null;
        }
    }

    @Override // com.android.systemui.statusbar.notification.TransformState
    public void setVisible(boolean z, boolean z2) {
        super.setVisible(z, z2);
        if (this.mTransformedView instanceof NotificationHeaderView) {
            NotificationHeaderView notificationHeaderView = this.mTransformedView;
            int childCount = notificationHeaderView.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childAt = notificationHeaderView.getChildAt(i);
                if (z2 || childAt.getVisibility() != 8) {
                    childAt.animate().cancel();
                    if (childAt.getVisibility() != 8) {
                        childAt.setVisibility(z ? 0 : 4);
                    }
                    if (childAt == this.mExpandButton) {
                        childAt.setAlpha(z ? 1.0f : 0.0f);
                    }
                    if (childAt == this.mWorkProfileIcon) {
                        childAt.setTranslationX(0.0f);
                        childAt.setTranslationY(0.0f);
                    }
                }
            }
        }
    }

    @Override // com.android.systemui.statusbar.notification.TransformState
    public void transformViewFrom(TransformState transformState, float f) {
        if (this.mTransformedView instanceof NotificationHeaderView) {
            NotificationHeaderView notificationHeaderView = this.mTransformedView;
            notificationHeaderView.setVisibility(0);
            notificationHeaderView.setAlpha(1.0f);
            int childCount = notificationHeaderView.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childAt = notificationHeaderView.getChildAt(i);
                if (childAt.getVisibility() != 8) {
                    if (childAt == this.mExpandButton) {
                        CrossFadeHelper.fadeIn(this.mExpandButton, f);
                    } else {
                        childAt.setVisibility(0);
                        if (childAt == this.mWorkProfileIcon) {
                            this.mWorkProfileState.transformViewFullyFrom(((HeaderTransformState) transformState).mWorkProfileState, f);
                        }
                    }
                }
            }
        }
    }

    @Override // com.android.systemui.statusbar.notification.TransformState
    public boolean transformViewTo(TransformState transformState, float f) {
        if (this.mTransformedView instanceof NotificationHeaderView) {
            NotificationHeaderView notificationHeaderView = this.mTransformedView;
            int childCount = notificationHeaderView.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childAt = notificationHeaderView.getChildAt(i);
                if (childAt.getVisibility() != 8) {
                    if (childAt != this.mExpandButton) {
                        childAt.setVisibility(4);
                    } else {
                        CrossFadeHelper.fadeOut(this.mExpandButton, f);
                    }
                }
            }
            return true;
        }
        return false;
    }
}
