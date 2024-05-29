package android.support.v4.widget;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.AccessibilityDelegateCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewParentCompat;
import android.support.v4.view.accessibility.AccessibilityEventCompat;
import android.support.v4.view.accessibility.AccessibilityManagerCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.view.accessibility.AccessibilityNodeProviderCompat;
import android.support.v4.view.accessibility.AccessibilityRecordCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import java.util.ArrayList;
import java.util.List;
/* loaded from: classes.dex */
public abstract class ExploreByTouchHelper extends AccessibilityDelegateCompat {
    private static final Rect INVALID_PARENT_BOUNDS = new Rect(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
    private static final FocusStrategy$BoundsAdapter<AccessibilityNodeInfoCompat> NODE_ADAPTER = new FocusStrategy$BoundsAdapter<AccessibilityNodeInfoCompat>() { // from class: android.support.v4.widget.ExploreByTouchHelper.1
    };
    private static final FocusStrategy$CollectionAdapter<SparseArrayCompat<AccessibilityNodeInfoCompat>, AccessibilityNodeInfoCompat> SPARSE_VALUES_ADAPTER = new FocusStrategy$CollectionAdapter<SparseArrayCompat<AccessibilityNodeInfoCompat>, AccessibilityNodeInfoCompat>() { // from class: android.support.v4.widget.ExploreByTouchHelper.2
    };
    private final View mHost;
    private final AccessibilityManager mManager;
    private MyNodeProvider mNodeProvider;
    private final Rect mTempScreenRect = new Rect();
    private final Rect mTempParentRect = new Rect();
    private final Rect mTempVisibleRect = new Rect();
    private final int[] mTempGlobalRect = new int[2];
    private int mAccessibilityFocusedVirtualViewId = Integer.MIN_VALUE;
    private int mKeyboardFocusedVirtualViewId = Integer.MIN_VALUE;
    private int mHoveredVirtualViewId = Integer.MIN_VALUE;

    protected abstract int getVirtualViewAt(float f, float f2);

    protected abstract void getVisibleVirtualViews(List<Integer> list);

    protected abstract boolean onPerformActionForVirtualView(int i, int i2, Bundle bundle);

    protected abstract void onPopulateNodeForVirtualView(int i, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat);

    public ExploreByTouchHelper(View host) {
        if (host == null) {
            throw new IllegalArgumentException("View may not be null");
        }
        this.mHost = host;
        Context context = host.getContext();
        this.mManager = (AccessibilityManager) context.getSystemService("accessibility");
        host.setFocusable(true);
        if (ViewCompat.getImportantForAccessibility(host) != 0) {
            return;
        }
        ViewCompat.setImportantForAccessibility(host, 1);
    }

    @Override // android.support.v4.view.AccessibilityDelegateCompat
    public AccessibilityNodeProviderCompat getAccessibilityNodeProvider(View host) {
        if (this.mNodeProvider == null) {
            this.mNodeProvider = new MyNodeProvider(this, null);
        }
        return this.mNodeProvider;
    }

    public final boolean dispatchHoverEvent(@NonNull MotionEvent event) {
        if (this.mManager.isEnabled() && AccessibilityManagerCompat.isTouchExplorationEnabled(this.mManager)) {
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
                    if (this.mAccessibilityFocusedVirtualViewId != Integer.MIN_VALUE) {
                        updateHoveredVirtualView(Integer.MIN_VALUE);
                        return true;
                    }
                    return false;
            }
        }
        return false;
    }

    public final boolean sendEventForVirtualView(int virtualViewId, int eventType) {
        ViewParent parent;
        if (virtualViewId == Integer.MIN_VALUE || !this.mManager.isEnabled() || (parent = this.mHost.getParent()) == null) {
            return false;
        }
        AccessibilityEvent event = createEvent(virtualViewId, eventType);
        return ViewParentCompat.requestSendAccessibilityEvent(parent, this.mHost, event);
    }

    public final void invalidateRoot() {
        invalidateVirtualView(-1, 1);
    }

    public final void invalidateVirtualView(int virtualViewId, int changeTypes) {
        ViewParent parent;
        if (virtualViewId == Integer.MIN_VALUE || !this.mManager.isEnabled() || (parent = this.mHost.getParent()) == null) {
            return;
        }
        AccessibilityEvent event = createEvent(virtualViewId, 2048);
        AccessibilityEventCompat.setContentChangeTypes(event, changeTypes);
        ViewParentCompat.requestSendAccessibilityEvent(parent, this.mHost, event);
    }

    protected void onVirtualViewKeyboardFocusChanged(int virtualViewId, boolean hasFocus) {
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
        ViewCompat.onInitializeAccessibilityEvent(this.mHost, event);
        return event;
    }

    @Override // android.support.v4.view.AccessibilityDelegateCompat
    public void onInitializeAccessibilityEvent(View host, AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(host, event);
        onPopulateEventForHost(event);
    }

    private AccessibilityEvent createEventForChild(int virtualViewId, int eventType) {
        AccessibilityEvent event = AccessibilityEvent.obtain(eventType);
        AccessibilityRecordCompat record = AccessibilityEventCompat.asRecord(event);
        AccessibilityNodeInfoCompat node = obtainAccessibilityNodeInfo(virtualViewId);
        record.getText().add(node.getText());
        record.setContentDescription(node.getContentDescription());
        record.setScrollable(node.isScrollable());
        record.setPassword(node.isPassword());
        record.setEnabled(node.isEnabled());
        record.setChecked(node.isChecked());
        onPopulateEventForVirtualView(virtualViewId, event);
        if (event.getText().isEmpty() && event.getContentDescription() == null) {
            throw new RuntimeException("Callbacks must add text or a content description in populateEventForVirtualViewId()");
        }
        record.setClassName(node.getClassName());
        record.setSource(this.mHost, virtualViewId);
        event.setPackageName(this.mHost.getContext().getPackageName());
        return event;
    }

    /* JADX INFO: Access modifiers changed from: private */
    @NonNull
    public AccessibilityNodeInfoCompat obtainAccessibilityNodeInfo(int virtualViewId) {
        if (virtualViewId == -1) {
            return createNodeForHost();
        }
        return createNodeForChild(virtualViewId);
    }

    @NonNull
    private AccessibilityNodeInfoCompat createNodeForHost() {
        AccessibilityNodeInfoCompat info = AccessibilityNodeInfoCompat.obtain(this.mHost);
        ViewCompat.onInitializeAccessibilityNodeInfo(this.mHost, info);
        ArrayList<Integer> virtualViewIds = new ArrayList<>();
        getVisibleVirtualViews(virtualViewIds);
        int realNodeCount = info.getChildCount();
        if (realNodeCount > 0 && virtualViewIds.size() > 0) {
            throw new RuntimeException("Views cannot have both real and virtual children");
        }
        int count = virtualViewIds.size();
        for (int i = 0; i < count; i++) {
            info.addChild(this.mHost, virtualViewIds.get(i).intValue());
        }
        return info;
    }

    @Override // android.support.v4.view.AccessibilityDelegateCompat
    public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
        super.onInitializeAccessibilityNodeInfo(host, info);
        onPopulateNodeForHost(info);
    }

    @NonNull
    private AccessibilityNodeInfoCompat createNodeForChild(int virtualViewId) {
        AccessibilityNodeInfoCompat node = AccessibilityNodeInfoCompat.obtain();
        node.setEnabled(true);
        node.setFocusable(true);
        node.setClassName("android.view.View");
        node.setBoundsInParent(INVALID_PARENT_BOUNDS);
        node.setBoundsInScreen(INVALID_PARENT_BOUNDS);
        onPopulateNodeForVirtualView(virtualViewId, node);
        if (node.getText() == null && node.getContentDescription() == null) {
            throw new RuntimeException("Callbacks must add text or a content description in populateNodeForVirtualViewId()");
        }
        node.getBoundsInParent(this.mTempParentRect);
        if (this.mTempParentRect.equals(INVALID_PARENT_BOUNDS)) {
            throw new RuntimeException("Callbacks must set parent bounds in populateNodeForVirtualViewId()");
        }
        int actions = node.getActions();
        if ((actions & 64) != 0) {
            throw new RuntimeException("Callbacks must not add ACTION_ACCESSIBILITY_FOCUS in populateNodeForVirtualViewId()");
        }
        if ((actions & 128) != 0) {
            throw new RuntimeException("Callbacks must not add ACTION_CLEAR_ACCESSIBILITY_FOCUS in populateNodeForVirtualViewId()");
        }
        node.setPackageName(this.mHost.getContext().getPackageName());
        node.setSource(this.mHost, virtualViewId);
        node.setParent(this.mHost);
        if (this.mAccessibilityFocusedVirtualViewId == virtualViewId) {
            node.setAccessibilityFocused(true);
            node.addAction(128);
        } else {
            node.setAccessibilityFocused(false);
            node.addAction(64);
        }
        boolean isFocused = this.mKeyboardFocusedVirtualViewId == virtualViewId;
        if (isFocused) {
            node.addAction(2);
        } else if (node.isFocusable()) {
            node.addAction(1);
        }
        node.setFocused(isFocused);
        if (intersectVisibleToUser(this.mTempParentRect)) {
            node.setVisibleToUser(true);
            node.setBoundsInParent(this.mTempParentRect);
        }
        node.getBoundsInScreen(this.mTempScreenRect);
        if (this.mTempScreenRect.equals(INVALID_PARENT_BOUNDS)) {
            this.mHost.getLocationOnScreen(this.mTempGlobalRect);
            node.getBoundsInParent(this.mTempScreenRect);
            this.mTempScreenRect.offset(this.mTempGlobalRect[0] - this.mHost.getScrollX(), this.mTempGlobalRect[1] - this.mHost.getScrollY());
            node.setBoundsInScreen(this.mTempScreenRect);
        }
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
        return ViewCompat.performAccessibilityAction(this.mHost, action, arguments);
    }

    private boolean performActionForChild(int virtualViewId, int action, Bundle arguments) {
        switch (action) {
            case 1:
                return requestKeyboardFocusForVirtualView(virtualViewId);
            case 2:
                return clearKeyboardFocusForVirtualView(virtualViewId);
            case 64:
                return requestAccessibilityFocus(virtualViewId);
            case 128:
                return clearAccessibilityFocus(virtualViewId);
            default:
                return onPerformActionForVirtualView(virtualViewId, action, arguments);
        }
    }

    private boolean intersectVisibleToUser(Rect localRect) {
        if (localRect == null || localRect.isEmpty() || this.mHost.getWindowVisibility() != 0) {
            return false;
        }
        ViewParent viewParent = this.mHost.getParent();
        while (viewParent instanceof View) {
            View view = (View) viewParent;
            if (ViewCompat.getAlpha(view) <= 0.0f || view.getVisibility() != 0) {
                return false;
            }
            viewParent = view.getParent();
        }
        if (viewParent != null && this.mHost.getLocalVisibleRect(this.mTempVisibleRect)) {
            return localRect.intersect(this.mTempVisibleRect);
        }
        return false;
    }

    private boolean requestAccessibilityFocus(int virtualViewId) {
        if (this.mManager.isEnabled() && AccessibilityManagerCompat.isTouchExplorationEnabled(this.mManager) && this.mAccessibilityFocusedVirtualViewId != virtualViewId) {
            if (this.mAccessibilityFocusedVirtualViewId != Integer.MIN_VALUE) {
                clearAccessibilityFocus(this.mAccessibilityFocusedVirtualViewId);
            }
            this.mAccessibilityFocusedVirtualViewId = virtualViewId;
            this.mHost.invalidate();
            sendEventForVirtualView(virtualViewId, 32768);
            return true;
        }
        return false;
    }

    private boolean clearAccessibilityFocus(int virtualViewId) {
        if (this.mAccessibilityFocusedVirtualViewId == virtualViewId) {
            this.mAccessibilityFocusedVirtualViewId = Integer.MIN_VALUE;
            this.mHost.invalidate();
            sendEventForVirtualView(virtualViewId, 65536);
            return true;
        }
        return false;
    }

    public final boolean requestKeyboardFocusForVirtualView(int virtualViewId) {
        if ((this.mHost.isFocused() || this.mHost.requestFocus()) && this.mKeyboardFocusedVirtualViewId != virtualViewId) {
            if (this.mKeyboardFocusedVirtualViewId != Integer.MIN_VALUE) {
                clearKeyboardFocusForVirtualView(this.mKeyboardFocusedVirtualViewId);
            }
            this.mKeyboardFocusedVirtualViewId = virtualViewId;
            onVirtualViewKeyboardFocusChanged(virtualViewId, true);
            sendEventForVirtualView(virtualViewId, 8);
            return true;
        }
        return false;
    }

    public final boolean clearKeyboardFocusForVirtualView(int virtualViewId) {
        if (this.mKeyboardFocusedVirtualViewId != virtualViewId) {
            return false;
        }
        this.mKeyboardFocusedVirtualViewId = Integer.MIN_VALUE;
        onVirtualViewKeyboardFocusChanged(virtualViewId, false);
        sendEventForVirtualView(virtualViewId, 8);
        return true;
    }

    protected void onPopulateEventForVirtualView(int virtualViewId, AccessibilityEvent event) {
    }

    protected void onPopulateEventForHost(AccessibilityEvent event) {
    }

    protected void onPopulateNodeForHost(AccessibilityNodeInfoCompat node) {
    }

    /* loaded from: classes.dex */
    private class MyNodeProvider extends AccessibilityNodeProviderCompat {
        /* synthetic */ MyNodeProvider(ExploreByTouchHelper this$0, MyNodeProvider myNodeProvider) {
            this();
        }

        private MyNodeProvider() {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeProviderCompat
        public AccessibilityNodeInfoCompat createAccessibilityNodeInfo(int virtualViewId) {
            AccessibilityNodeInfoCompat node = ExploreByTouchHelper.this.obtainAccessibilityNodeInfo(virtualViewId);
            return AccessibilityNodeInfoCompat.obtain(node);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeProviderCompat
        public boolean performAction(int virtualViewId, int action, Bundle arguments) {
            return ExploreByTouchHelper.this.performAction(virtualViewId, action, arguments);
        }
    }
}
