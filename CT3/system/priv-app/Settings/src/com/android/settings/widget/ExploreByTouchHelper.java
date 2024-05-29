package com.android.settings.widget;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;
import java.util.LinkedList;
import java.util.List;
/* loaded from: classes.dex */
public abstract class ExploreByTouchHelper extends View.AccessibilityDelegate {
    private static final String DEFAULT_CLASS_NAME = View.class.getName();
    private Context mContext;
    private final AccessibilityManager mManager;
    private ExploreByTouchNodeProvider mNodeProvider;
    private final View mView;
    private final Rect mTempScreenRect = new Rect();
    private final Rect mTempParentRect = new Rect();
    private final Rect mTempVisibleRect = new Rect();
    private final int[] mTempGlobalRect = new int[2];
    private int mFocusedVirtualViewId = Integer.MIN_VALUE;
    private int mHoveredVirtualViewId = Integer.MIN_VALUE;

    protected abstract int getVirtualViewAt(float f, float f2);

    protected abstract void getVisibleVirtualViews(List<Integer> list);

    protected abstract boolean onPerformActionForVirtualView(int i, int i2, Bundle bundle);

    protected abstract void onPopulateEventForVirtualView(int i, AccessibilityEvent accessibilityEvent);

    protected abstract void onPopulateNodeForVirtualView(int i, AccessibilityNodeInfo accessibilityNodeInfo);

    public ExploreByTouchHelper(View forView) {
        if (forView == null) {
            throw new IllegalArgumentException("View may not be null");
        }
        this.mView = forView;
        this.mContext = forView.getContext();
        this.mManager = (AccessibilityManager) this.mContext.getSystemService("accessibility");
    }

    @Override // android.view.View.AccessibilityDelegate
    public AccessibilityNodeProvider getAccessibilityNodeProvider(View host) {
        if (this.mNodeProvider == null) {
            this.mNodeProvider = new ExploreByTouchNodeProvider(this, null);
        }
        return this.mNodeProvider;
    }

    public boolean dispatchHoverEvent(MotionEvent event) {
        if (this.mManager.isEnabled() && this.mManager.isTouchExplorationEnabled()) {
            switch (event.getAction()) {
                case 7:
                case 9:
                    int virtualViewId = getVirtualViewAt(event.getX(), event.getY());
                    updateHoveredVirtualView(virtualViewId);
                    return virtualViewId != Integer.MIN_VALUE;
                case 8:
                default:
                    return false;
                case 10:
                    if (this.mFocusedVirtualViewId != Integer.MIN_VALUE) {
                        updateHoveredVirtualView(Integer.MIN_VALUE);
                        return true;
                    }
                    return false;
            }
        }
        return false;
    }

    public boolean sendEventForVirtualView(int virtualViewId, int eventType) {
        ViewParent parent;
        if (virtualViewId == Integer.MIN_VALUE || !this.mManager.isEnabled() || (parent = this.mView.getParent()) == null) {
            return false;
        }
        AccessibilityEvent event = createEvent(virtualViewId, eventType);
        return parent.requestSendAccessibilityEvent(this.mView, event);
    }

    private void updateHoveredVirtualView(int virtualViewId) {
        if (this.mHoveredVirtualViewId == virtualViewId) {
            return;
        }
        int previousVirtualViewId = this.mHoveredVirtualViewId;
        this.mHoveredVirtualViewId = virtualViewId;
        sendEventForVirtualView(virtualViewId, 128);
        sendEventForVirtualView(previousVirtualViewId, 256);
    }

    private AccessibilityEvent createEvent(int virtualViewId, int eventType) {
        switch (virtualViewId) {
            case -1:
                return createEventForHost(eventType);
            default:
                return createEventForChild(virtualViewId, eventType);
        }
    }

    private AccessibilityEvent createEventForHost(int eventType) {
        AccessibilityEvent event = AccessibilityEvent.obtain(eventType);
        this.mView.onInitializeAccessibilityEvent(event);
        return event;
    }

    private AccessibilityEvent createEventForChild(int virtualViewId, int eventType) {
        AccessibilityEvent event = AccessibilityEvent.obtain(eventType);
        event.setEnabled(true);
        event.setClassName(DEFAULT_CLASS_NAME);
        onPopulateEventForVirtualView(virtualViewId, event);
        if (event.getText().isEmpty() && event.getContentDescription() == null) {
            throw new RuntimeException("Callbacks must add text or a content description in populateEventForVirtualViewId()");
        }
        event.setPackageName(this.mView.getContext().getPackageName());
        event.setSource(this.mView, virtualViewId);
        return event;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public AccessibilityNodeInfo createNode(int virtualViewId) {
        switch (virtualViewId) {
            case -1:
                return createNodeForHost();
            default:
                return createNodeForChild(virtualViewId);
        }
    }

    private AccessibilityNodeInfo createNodeForHost() {
        AccessibilityNodeInfo node = AccessibilityNodeInfo.obtain(this.mView);
        this.mView.onInitializeAccessibilityNodeInfo(node);
        LinkedList<Integer> virtualViewIds = new LinkedList<>();
        getVisibleVirtualViews(virtualViewIds);
        for (Integer childVirtualViewId : virtualViewIds) {
            node.addChild(this.mView, childVirtualViewId.intValue());
        }
        return node;
    }

    private AccessibilityNodeInfo createNodeForChild(int virtualViewId) {
        AccessibilityNodeInfo node = AccessibilityNodeInfo.obtain();
        node.setEnabled(true);
        node.setClassName(DEFAULT_CLASS_NAME);
        onPopulateNodeForVirtualView(virtualViewId, node);
        if (node.getText() == null && node.getContentDescription() == null) {
            throw new RuntimeException("Callbacks must add text or a content description in populateNodeForVirtualViewId()");
        }
        node.getBoundsInParent(this.mTempParentRect);
        if (this.mTempParentRect.isEmpty()) {
            throw new RuntimeException("Callbacks must set parent bounds in populateNodeForVirtualViewId()");
        }
        int actions = node.getActions();
        if ((actions & 64) != 0) {
            throw new RuntimeException("Callbacks must not add ACTION_ACCESSIBILITY_FOCUS in populateNodeForVirtualViewId()");
        }
        if ((actions & 128) != 0) {
            throw new RuntimeException("Callbacks must not add ACTION_CLEAR_ACCESSIBILITY_FOCUS in populateNodeForVirtualViewId()");
        }
        node.setPackageName(this.mView.getContext().getPackageName());
        node.setSource(this.mView, virtualViewId);
        node.setParent(this.mView);
        if (this.mFocusedVirtualViewId == virtualViewId) {
            node.setAccessibilityFocused(true);
            node.addAction(128);
        } else {
            node.setAccessibilityFocused(false);
            node.addAction(64);
        }
        if (intersectVisibleToUser(this.mTempParentRect)) {
            node.setVisibleToUser(true);
            node.setBoundsInParent(this.mTempParentRect);
        }
        this.mView.getLocationOnScreen(this.mTempGlobalRect);
        int offsetX = this.mTempGlobalRect[0];
        int offsetY = this.mTempGlobalRect[1];
        this.mTempScreenRect.set(this.mTempParentRect);
        this.mTempScreenRect.offset(offsetX, offsetY);
        node.setBoundsInScreen(this.mTempScreenRect);
        return node;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean performAction(int virtualViewId, int action, Bundle arguments) {
        switch (virtualViewId) {
            case -1:
                return performActionForHost(action, arguments);
            default:
                return performActionForChild(virtualViewId, action, arguments);
        }
    }

    private boolean performActionForHost(int action, Bundle arguments) {
        return this.mView.performAccessibilityAction(action, arguments);
    }

    private boolean performActionForChild(int virtualViewId, int action, Bundle arguments) {
        switch (action) {
            case 64:
            case 128:
                return manageFocusForChild(virtualViewId, action, arguments);
            default:
                return onPerformActionForVirtualView(virtualViewId, action, arguments);
        }
    }

    private boolean manageFocusForChild(int virtualViewId, int action, Bundle arguments) {
        switch (action) {
            case 64:
                return requestAccessibilityFocus(virtualViewId);
            case 128:
                return clearAccessibilityFocus(virtualViewId);
            default:
                return false;
        }
    }

    private boolean intersectVisibleToUser(Rect localRect) {
        if (localRect == null || localRect.isEmpty() || this.mView.getWindowVisibility() != 0) {
            return false;
        }
        ViewParent viewParent = this.mView.getParent();
        while (viewParent instanceof View) {
            View view = (View) viewParent;
            if (view.getAlpha() <= 0.0f || view.getVisibility() != 0) {
                return false;
            }
            viewParent = view.getParent();
        }
        if (viewParent != null && this.mView.getLocalVisibleRect(this.mTempVisibleRect)) {
            return localRect.intersect(this.mTempVisibleRect);
        }
        return false;
    }

    private boolean isAccessibilityFocused(int virtualViewId) {
        return this.mFocusedVirtualViewId == virtualViewId;
    }

    private boolean requestAccessibilityFocus(int virtualViewId) {
        AccessibilityManager accessibilityManager = (AccessibilityManager) this.mContext.getSystemService("accessibility");
        if (this.mManager.isEnabled() && accessibilityManager.isTouchExplorationEnabled() && !isAccessibilityFocused(virtualViewId)) {
            this.mFocusedVirtualViewId = virtualViewId;
            this.mView.invalidate();
            sendEventForVirtualView(virtualViewId, 32768);
            return true;
        }
        return false;
    }

    private boolean clearAccessibilityFocus(int virtualViewId) {
        if (isAccessibilityFocused(virtualViewId)) {
            this.mFocusedVirtualViewId = Integer.MIN_VALUE;
            this.mView.invalidate();
            sendEventForVirtualView(virtualViewId, 65536);
            return true;
        }
        return false;
    }

    /* loaded from: classes.dex */
    private class ExploreByTouchNodeProvider extends AccessibilityNodeProvider {
        /* synthetic */ ExploreByTouchNodeProvider(ExploreByTouchHelper this$0, ExploreByTouchNodeProvider exploreByTouchNodeProvider) {
            this();
        }

        private ExploreByTouchNodeProvider() {
        }

        @Override // android.view.accessibility.AccessibilityNodeProvider
        public AccessibilityNodeInfo createAccessibilityNodeInfo(int virtualViewId) {
            return ExploreByTouchHelper.this.createNode(virtualViewId);
        }

        @Override // android.view.accessibility.AccessibilityNodeProvider
        public boolean performAction(int virtualViewId, int action, Bundle arguments) {
            return ExploreByTouchHelper.this.performAction(virtualViewId, action, arguments);
        }
    }
}
