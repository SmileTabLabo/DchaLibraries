package com.android.systemui.statusbar.notification;

import android.util.Pools;
import android.view.View;
import com.android.internal.widget.MessagingGroup;
import com.android.internal.widget.MessagingImageMessage;
import com.android.internal.widget.MessagingLayout;
import com.android.internal.widget.MessagingLinearLayout;
import com.android.internal.widget.MessagingMessage;
import com.android.internal.widget.MessagingPropertyAnimator;
import com.android.systemui.Interpolators;
import com.android.systemui.statusbar.notification.TransformState;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
/* loaded from: classes.dex */
public class MessagingLayoutTransformState extends TransformState {
    private static Pools.SimplePool<MessagingLayoutTransformState> sInstancePool = new Pools.SimplePool<>(40);
    private HashMap<MessagingGroup, MessagingGroup> mGroupMap = new HashMap<>();
    private MessagingLinearLayout mMessageContainer;
    private MessagingLayout mMessagingLayout;
    private float mRelativeTranslationOffset;

    public static MessagingLayoutTransformState obtain() {
        MessagingLayoutTransformState messagingLayoutTransformState = (MessagingLayoutTransformState) sInstancePool.acquire();
        if (messagingLayoutTransformState != null) {
            return messagingLayoutTransformState;
        }
        return new MessagingLayoutTransformState();
    }

    @Override // com.android.systemui.statusbar.notification.TransformState
    public void initFrom(View view, TransformState.TransformInfo transformInfo) {
        super.initFrom(view, transformInfo);
        if (this.mTransformedView instanceof MessagingLinearLayout) {
            this.mMessageContainer = this.mTransformedView;
            this.mMessagingLayout = this.mMessageContainer.getMessagingLayout();
            this.mRelativeTranslationOffset = view.getContext().getResources().getDisplayMetrics().density * 8.0f;
        }
    }

    @Override // com.android.systemui.statusbar.notification.TransformState
    public boolean transformViewTo(TransformState transformState, float f) {
        if (transformState instanceof MessagingLayoutTransformState) {
            transformViewInternal((MessagingLayoutTransformState) transformState, f, true);
            return true;
        }
        return super.transformViewTo(transformState, f);
    }

    @Override // com.android.systemui.statusbar.notification.TransformState
    public void transformViewFrom(TransformState transformState, float f) {
        if (transformState instanceof MessagingLayoutTransformState) {
            transformViewInternal((MessagingLayoutTransformState) transformState, f, false);
        } else {
            super.transformViewFrom(transformState, f);
        }
    }

    private void transformViewInternal(MessagingLayoutTransformState messagingLayoutTransformState, float f, boolean z) {
        float f2;
        ensureVisible();
        ArrayList<MessagingGroup> filterHiddenGroups = filterHiddenGroups(this.mMessagingLayout.getMessagingGroups());
        HashMap<MessagingGroup, MessagingGroup> findPairs = findPairs(filterHiddenGroups, filterHiddenGroups(messagingLayoutTransformState.mMessagingLayout.getMessagingGroups()));
        MessagingGroup messagingGroup = null;
        float f3 = 0.0f;
        float f4 = 0.0f;
        for (int size = filterHiddenGroups.size() - 1; size >= 0; size--) {
            MessagingGroup messagingGroup2 = filterHiddenGroups.get(size);
            MessagingGroup messagingGroup3 = findPairs.get(messagingGroup2);
            if (!isGone(messagingGroup2)) {
                if (messagingGroup3 != null) {
                    transformGroups(messagingGroup2, messagingGroup3, f, z);
                    if (messagingGroup == null) {
                        if (z) {
                            float translationY = messagingGroup3.getAvatar().getTranslationY();
                            f4 = translationY;
                            f3 = translationY - (messagingGroup2.getTop() - messagingGroup3.getTop());
                        } else {
                            f3 = messagingGroup2.getAvatar().getTranslationY();
                            f4 = f3 - (messagingGroup3.getTop() - messagingGroup2.getTop());
                        }
                        messagingGroup = messagingGroup2;
                    }
                } else {
                    if (messagingGroup != null) {
                        adaptGroupAppear(messagingGroup2, f, f3, z);
                        int top = messagingGroup.getTop() - messagingGroup2.getTop();
                        float height = this.mTransformInfo.isAnimating() ? top : messagingGroup2.getHeight() * 0.75f;
                        f2 = Math.max(0.0f, Math.min(1.0f, (f4 - (top - height)) / height));
                        if (z) {
                            f2 = 1.0f - f2;
                        }
                    } else {
                        f2 = f;
                    }
                    if (z) {
                        disappear(messagingGroup2, f2);
                    } else {
                        appear(messagingGroup2, f2);
                    }
                }
            }
        }
    }

    private void appear(MessagingGroup messagingGroup, float f) {
        MessagingLinearLayout messageContainer = messagingGroup.getMessageContainer();
        for (int i = 0; i < messageContainer.getChildCount(); i++) {
            View childAt = messageContainer.getChildAt(i);
            if (!isGone(childAt)) {
                appear(childAt, f);
                setClippingDeactivated(childAt, true);
            }
        }
        appear(messagingGroup.getAvatar(), f);
        appear(messagingGroup.getSenderView(), f);
        appear((View) messagingGroup.getIsolatedMessage(), f);
        setClippingDeactivated(messagingGroup.getSenderView(), true);
        setClippingDeactivated(messagingGroup.getAvatar(), true);
    }

    private void adaptGroupAppear(MessagingGroup messagingGroup, float f, float f2, boolean z) {
        float f3;
        if (z) {
            f3 = f * this.mRelativeTranslationOffset;
        } else {
            f3 = this.mRelativeTranslationOffset * (1.0f - f);
        }
        if (messagingGroup.getSenderView().getVisibility() != 8) {
            f3 *= 0.5f;
        }
        messagingGroup.getMessageContainer().setTranslationY(f3);
        messagingGroup.setTranslationY(f2 * 0.85f);
    }

    private void disappear(MessagingGroup messagingGroup, float f) {
        MessagingLinearLayout messageContainer = messagingGroup.getMessageContainer();
        for (int i = 0; i < messageContainer.getChildCount(); i++) {
            View childAt = messageContainer.getChildAt(i);
            if (!isGone(childAt)) {
                disappear(childAt, f);
                setClippingDeactivated(childAt, true);
            }
        }
        disappear(messagingGroup.getAvatar(), f);
        disappear(messagingGroup.getSenderView(), f);
        disappear((View) messagingGroup.getIsolatedMessage(), f);
        setClippingDeactivated(messagingGroup.getSenderView(), true);
        setClippingDeactivated(messagingGroup.getAvatar(), true);
    }

    private void appear(View view, float f) {
        if (view == null || view.getVisibility() == 8) {
            return;
        }
        TransformState createFrom = TransformState.createFrom(view, this.mTransformInfo);
        createFrom.appear(f, null);
        createFrom.recycle();
    }

    private void disappear(View view, float f) {
        if (view == null || view.getVisibility() == 8) {
            return;
        }
        TransformState createFrom = TransformState.createFrom(view, this.mTransformInfo);
        createFrom.disappear(f, null);
        createFrom.recycle();
    }

    private ArrayList<MessagingGroup> filterHiddenGroups(ArrayList<MessagingGroup> arrayList) {
        ArrayList<MessagingGroup> arrayList2 = new ArrayList<>(arrayList);
        int i = 0;
        while (i < arrayList2.size()) {
            if (isGone(arrayList2.get(i))) {
                arrayList2.remove(i);
                i--;
            }
            i++;
        }
        return arrayList2;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:22:0x0084  */
    /* JADX WARN: Removed duplicated region for block: B:28:0x00ba  */
    /* JADX WARN: Removed duplicated region for block: B:33:0x00cc  */
    /* JADX WARN: Removed duplicated region for block: B:34:0x00d5  */
    /* JADX WARN: Type inference failed for: r9v3 */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private void transformGroups(MessagingGroup messagingGroup, MessagingGroup messagingGroup2, float f, boolean z) {
        View view;
        float f2;
        View view2;
        boolean z2;
        int i = 0;
        int i2 = 1;
        boolean z3 = messagingGroup2.getIsolatedMessage() == null && !this.mTransformInfo.isAnimating();
        boolean z4 = z3;
        transformView(f, z, messagingGroup.getSenderView(), messagingGroup2.getSenderView(), true, z4);
        transformView(f, z, messagingGroup.getAvatar(), messagingGroup2.getAvatar(), true, z4);
        List messages = messagingGroup.getMessages();
        List messages2 = messagingGroup2.getMessages();
        float f3 = f;
        float f4 = 0.0f;
        while (i < messages.size()) {
            View view3 = ((MessagingMessage) messages.get((messages.size() - i2) - i)).getView();
            if (isGone(view3)) {
                f2 = f3;
                z2 = i2;
            } else {
                int size = (messages2.size() - i2) - i;
                if (size >= 0) {
                    View view4 = ((MessagingMessage) messages2.get(size)).getView();
                    if (!isGone(view4)) {
                        view = view4;
                        if (view == null) {
                            f3 = Math.max(0.0f, Math.min(1.0f, ((view3.getTop() + view3.getHeight()) + f4) / view3.getHeight()));
                            if (z) {
                                f2 = 1.0f - f3;
                                view2 = view;
                                transformView(f2, z, view3, view, false, z3);
                                if (f2 == 0.0f || messagingGroup2.getIsolatedMessage() != view2) {
                                    z2 = true;
                                } else {
                                    z2 = true;
                                    messagingGroup.setTransformingImages(true);
                                }
                                if (view2 == null) {
                                    view3.setTranslationY(f4);
                                    setClippingDeactivated(view3, z2);
                                } else {
                                    f4 = z ? view2.getTranslationY() - (((view3.getTop() + messagingGroup.getTop()) - view2.getTop()) - view2.getTop()) : view3.getTranslationY();
                                }
                            }
                        }
                        f2 = f3;
                        view2 = view;
                        transformView(f2, z, view3, view, false, z3);
                        if (f2 == 0.0f) {
                        }
                        z2 = true;
                        if (view2 == null) {
                        }
                    }
                }
                view = null;
                if (view == null) {
                }
                f2 = f3;
                view2 = view;
                transformView(f2, z, view3, view, false, z3);
                if (f2 == 0.0f) {
                }
                z2 = true;
                if (view2 == null) {
                }
            }
            i++;
            i2 = z2;
            f3 = f2;
        }
        messagingGroup.updateClipRect();
    }

    private void transformView(float f, boolean z, View view, View view2, boolean z2, boolean z3) {
        TransformState createFrom = TransformState.createFrom(view, this.mTransformInfo);
        if (z3) {
            createFrom.setDefaultInterpolator(Interpolators.LINEAR);
        }
        createFrom.setIsSameAsAnyView(z2);
        if (z) {
            if (view2 != null) {
                TransformState createFrom2 = TransformState.createFrom(view2, this.mTransformInfo);
                createFrom.transformViewTo(createFrom2, f);
                createFrom2.recycle();
            } else {
                createFrom.disappear(f, null);
            }
        } else if (view2 != null) {
            TransformState createFrom3 = TransformState.createFrom(view2, this.mTransformInfo);
            createFrom.transformViewFrom(createFrom3, f);
            createFrom3.recycle();
        } else {
            createFrom.appear(f, null);
        }
        createFrom.recycle();
    }

    private HashMap<MessagingGroup, MessagingGroup> findPairs(ArrayList<MessagingGroup> arrayList, ArrayList<MessagingGroup> arrayList2) {
        this.mGroupMap.clear();
        int i = Integer.MAX_VALUE;
        for (int size = arrayList.size() - 1; size >= 0; size--) {
            MessagingGroup messagingGroup = arrayList.get(size);
            MessagingGroup messagingGroup2 = null;
            int i2 = 0;
            for (int min = Math.min(arrayList2.size(), i) - 1; min >= 0; min--) {
                MessagingGroup messagingGroup3 = arrayList2.get(min);
                int calculateGroupCompatibility = messagingGroup.calculateGroupCompatibility(messagingGroup3);
                if (calculateGroupCompatibility > i2) {
                    i = min;
                    messagingGroup2 = messagingGroup3;
                    i2 = calculateGroupCompatibility;
                }
            }
            if (messagingGroup2 != null) {
                this.mGroupMap.put(messagingGroup, messagingGroup2);
            }
        }
        return this.mGroupMap;
    }

    private boolean isGone(View view) {
        if (view.getVisibility() == 8) {
            return true;
        }
        MessagingLinearLayout.LayoutParams layoutParams = view.getLayoutParams();
        return (layoutParams instanceof MessagingLinearLayout.LayoutParams) && layoutParams.hide;
    }

    @Override // com.android.systemui.statusbar.notification.TransformState
    public void setVisible(boolean z, boolean z2) {
        super.setVisible(z, z2);
        resetTransformedView();
        ArrayList messagingGroups = this.mMessagingLayout.getMessagingGroups();
        for (int i = 0; i < messagingGroups.size(); i++) {
            MessagingGroup messagingGroup = (MessagingGroup) messagingGroups.get(i);
            if (!isGone(messagingGroup)) {
                MessagingLinearLayout messageContainer = messagingGroup.getMessageContainer();
                for (int i2 = 0; i2 < messageContainer.getChildCount(); i2++) {
                    setVisible(messageContainer.getChildAt(i2), z, z2);
                }
                setVisible(messagingGroup.getAvatar(), z, z2);
                setVisible(messagingGroup.getSenderView(), z, z2);
                MessagingImageMessage isolatedMessage = messagingGroup.getIsolatedMessage();
                if (isolatedMessage != null) {
                    setVisible(isolatedMessage, z, z2);
                }
            }
        }
    }

    private void setVisible(View view, boolean z, boolean z2) {
        if (isGone(view) || MessagingPropertyAnimator.isAnimatingAlpha(view)) {
            return;
        }
        TransformState createFrom = TransformState.createFrom(view, this.mTransformInfo);
        createFrom.setVisible(z, z2);
        createFrom.recycle();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.TransformState
    public void resetTransformedView() {
        super.resetTransformedView();
        ArrayList messagingGroups = this.mMessagingLayout.getMessagingGroups();
        for (int i = 0; i < messagingGroups.size(); i++) {
            MessagingGroup messagingGroup = (MessagingGroup) messagingGroups.get(i);
            if (!isGone(messagingGroup)) {
                MessagingLinearLayout messageContainer = messagingGroup.getMessageContainer();
                for (int i2 = 0; i2 < messageContainer.getChildCount(); i2++) {
                    View childAt = messageContainer.getChildAt(i2);
                    if (!isGone(childAt)) {
                        resetTransformedView(childAt);
                        setClippingDeactivated(childAt, false);
                    }
                }
                resetTransformedView(messagingGroup.getAvatar());
                resetTransformedView(messagingGroup.getSenderView());
                MessagingImageMessage isolatedMessage = messagingGroup.getIsolatedMessage();
                if (isolatedMessage != null) {
                    resetTransformedView(isolatedMessage);
                }
                setClippingDeactivated(messagingGroup.getAvatar(), false);
                setClippingDeactivated(messagingGroup.getSenderView(), false);
                messagingGroup.setTranslationY(0.0f);
                messagingGroup.getMessageContainer().setTranslationY(0.0f);
            }
            messagingGroup.setTransformingImages(false);
            messagingGroup.updateClipRect();
        }
    }

    @Override // com.android.systemui.statusbar.notification.TransformState
    public void prepareFadeIn() {
        super.prepareFadeIn();
        setVisible(true, false);
    }

    private void resetTransformedView(View view) {
        TransformState createFrom = TransformState.createFrom(view, this.mTransformInfo);
        createFrom.resetTransformedView();
        createFrom.recycle();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.TransformState
    public void reset() {
        super.reset();
        this.mMessageContainer = null;
        this.mMessagingLayout = null;
    }

    @Override // com.android.systemui.statusbar.notification.TransformState
    public void recycle() {
        super.recycle();
        this.mGroupMap.clear();
        sInstancePool.release(this);
    }
}
