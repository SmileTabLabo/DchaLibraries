package android.support.v4.view.accessibility;

import android.graphics.Rect;
import android.os.Build;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompatApi21;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompatKitKat;
import android.view.View;
/* loaded from: classes.dex */
public class AccessibilityNodeInfoCompat {
    private static final AccessibilityNodeInfoImpl IMPL;
    private final Object mInfo;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public interface AccessibilityNodeInfoImpl {
        void addAction(Object obj, int i);

        void addAction(Object obj, Object obj2);

        void addChild(Object obj, View view);

        void addChild(Object obj, View view, int i);

        int getActions(Object obj);

        void getBoundsInParent(Object obj, Rect rect);

        void getBoundsInScreen(Object obj, Rect rect);

        int getChildCount(Object obj);

        CharSequence getClassName(Object obj);

        int getCollectionItemColumnIndex(Object obj);

        int getCollectionItemColumnSpan(Object obj);

        Object getCollectionItemInfo(Object obj);

        int getCollectionItemRowIndex(Object obj);

        int getCollectionItemRowSpan(Object obj);

        CharSequence getContentDescription(Object obj);

        CharSequence getPackageName(Object obj);

        CharSequence getText(Object obj);

        String getViewIdResourceName(Object obj);

        boolean isAccessibilityFocused(Object obj);

        boolean isCheckable(Object obj);

        boolean isChecked(Object obj);

        boolean isClickable(Object obj);

        boolean isCollectionItemSelected(Object obj);

        boolean isEnabled(Object obj);

        boolean isFocusable(Object obj);

        boolean isFocused(Object obj);

        boolean isLongClickable(Object obj);

        boolean isPassword(Object obj);

        boolean isScrollable(Object obj);

        boolean isSelected(Object obj);

        boolean isVisibleToUser(Object obj);

        Object newAccessibilityAction(int i, CharSequence charSequence);

        Object obtain();

        Object obtain(View view);

        Object obtain(Object obj);

        Object obtainCollectionInfo(int i, int i2, boolean z, int i3);

        Object obtainCollectionItemInfo(int i, int i2, int i3, int i4, boolean z, boolean z2);

        void recycle(Object obj);

        boolean removeAction(Object obj, Object obj2);

        void setAccessibilityFocused(Object obj, boolean z);

        void setBoundsInParent(Object obj, Rect rect);

        void setBoundsInScreen(Object obj, Rect rect);

        void setCheckable(Object obj, boolean z);

        void setChecked(Object obj, boolean z);

        void setClassName(Object obj, CharSequence charSequence);

        void setClickable(Object obj, boolean z);

        void setCollectionInfo(Object obj, Object obj2);

        void setCollectionItemInfo(Object obj, Object obj2);

        void setContentDescription(Object obj, CharSequence charSequence);

        void setEnabled(Object obj, boolean z);

        void setFocusable(Object obj, boolean z);

        void setFocused(Object obj, boolean z);

        void setLongClickable(Object obj, boolean z);

        void setPackageName(Object obj, CharSequence charSequence);

        void setParent(Object obj, View view);

        void setScrollable(Object obj, boolean z);

        void setSelected(Object obj, boolean z);

        void setSource(Object obj, View view);

        void setSource(Object obj, View view, int i);

        void setVisibleToUser(Object obj, boolean z);
    }

    /* loaded from: classes.dex */
    public static class AccessibilityActionCompat {
        private final Object mAction;
        public static final AccessibilityActionCompat ACTION_FOCUS = new AccessibilityActionCompat(1, null);
        public static final AccessibilityActionCompat ACTION_CLEAR_FOCUS = new AccessibilityActionCompat(2, null);
        public static final AccessibilityActionCompat ACTION_SELECT = new AccessibilityActionCompat(4, null);
        public static final AccessibilityActionCompat ACTION_CLEAR_SELECTION = new AccessibilityActionCompat(8, null);
        public static final AccessibilityActionCompat ACTION_CLICK = new AccessibilityActionCompat(16, null);
        public static final AccessibilityActionCompat ACTION_LONG_CLICK = new AccessibilityActionCompat(32, null);
        public static final AccessibilityActionCompat ACTION_ACCESSIBILITY_FOCUS = new AccessibilityActionCompat(64, null);
        public static final AccessibilityActionCompat ACTION_CLEAR_ACCESSIBILITY_FOCUS = new AccessibilityActionCompat(128, null);
        public static final AccessibilityActionCompat ACTION_NEXT_AT_MOVEMENT_GRANULARITY = new AccessibilityActionCompat(256, null);
        public static final AccessibilityActionCompat ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY = new AccessibilityActionCompat(512, null);
        public static final AccessibilityActionCompat ACTION_NEXT_HTML_ELEMENT = new AccessibilityActionCompat(1024, null);
        public static final AccessibilityActionCompat ACTION_PREVIOUS_HTML_ELEMENT = new AccessibilityActionCompat(2048, null);
        public static final AccessibilityActionCompat ACTION_SCROLL_FORWARD = new AccessibilityActionCompat(4096, null);
        public static final AccessibilityActionCompat ACTION_SCROLL_BACKWARD = new AccessibilityActionCompat(8192, null);
        public static final AccessibilityActionCompat ACTION_COPY = new AccessibilityActionCompat(16384, null);
        public static final AccessibilityActionCompat ACTION_PASTE = new AccessibilityActionCompat(32768, null);
        public static final AccessibilityActionCompat ACTION_CUT = new AccessibilityActionCompat(65536, null);
        public static final AccessibilityActionCompat ACTION_SET_SELECTION = new AccessibilityActionCompat(131072, null);
        public static final AccessibilityActionCompat ACTION_EXPAND = new AccessibilityActionCompat(262144, null);
        public static final AccessibilityActionCompat ACTION_COLLAPSE = new AccessibilityActionCompat(524288, null);
        public static final AccessibilityActionCompat ACTION_DISMISS = new AccessibilityActionCompat(1048576, null);
        public static final AccessibilityActionCompat ACTION_SET_TEXT = new AccessibilityActionCompat(2097152, null);

        public AccessibilityActionCompat(int actionId, CharSequence label) {
            this(AccessibilityNodeInfoCompat.IMPL.newAccessibilityAction(actionId, label));
        }

        private AccessibilityActionCompat(Object action) {
            this.mAction = action;
        }
    }

    /* loaded from: classes.dex */
    public static class CollectionInfoCompat {
        final Object mInfo;

        public static CollectionInfoCompat obtain(int rowCount, int columnCount, boolean hierarchical, int selectionMode) {
            return new CollectionInfoCompat(AccessibilityNodeInfoCompat.IMPL.obtainCollectionInfo(rowCount, columnCount, hierarchical, selectionMode));
        }

        private CollectionInfoCompat(Object info) {
            this.mInfo = info;
        }
    }

    /* loaded from: classes.dex */
    public static class CollectionItemInfoCompat {
        private final Object mInfo;

        /* synthetic */ CollectionItemInfoCompat(Object info, CollectionItemInfoCompat collectionItemInfoCompat) {
            this(info);
        }

        public static CollectionItemInfoCompat obtain(int rowIndex, int rowSpan, int columnIndex, int columnSpan, boolean heading, boolean selected) {
            return new CollectionItemInfoCompat(AccessibilityNodeInfoCompat.IMPL.obtainCollectionItemInfo(rowIndex, rowSpan, columnIndex, columnSpan, heading, selected));
        }

        private CollectionItemInfoCompat(Object info) {
            this.mInfo = info;
        }

        public int getColumnIndex() {
            return AccessibilityNodeInfoCompat.IMPL.getCollectionItemColumnIndex(this.mInfo);
        }

        public int getColumnSpan() {
            return AccessibilityNodeInfoCompat.IMPL.getCollectionItemColumnSpan(this.mInfo);
        }

        public int getRowIndex() {
            return AccessibilityNodeInfoCompat.IMPL.getCollectionItemRowIndex(this.mInfo);
        }

        public int getRowSpan() {
            return AccessibilityNodeInfoCompat.IMPL.getCollectionItemRowSpan(this.mInfo);
        }

        public boolean isSelected() {
            return AccessibilityNodeInfoCompat.IMPL.isCollectionItemSelected(this.mInfo);
        }
    }

    /* loaded from: classes.dex */
    static class AccessibilityNodeInfoStubImpl implements AccessibilityNodeInfoImpl {
        AccessibilityNodeInfoStubImpl() {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public Object newAccessibilityAction(int actionId, CharSequence label) {
            return null;
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public Object obtain() {
            return null;
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public Object obtain(View source) {
            return null;
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public Object obtain(Object info) {
            return null;
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void addAction(Object info, int action) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void addAction(Object info, Object action) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public boolean removeAction(Object info, Object action) {
            return false;
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void addChild(Object info, View child) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void addChild(Object info, View child, int virtualDescendantId) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public int getActions(Object info) {
            return 0;
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void getBoundsInParent(Object info, Rect outBounds) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void getBoundsInScreen(Object info, Rect outBounds) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public int getChildCount(Object info) {
            return 0;
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public CharSequence getClassName(Object info) {
            return null;
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public CharSequence getContentDescription(Object info) {
            return null;
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public CharSequence getPackageName(Object info) {
            return null;
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public CharSequence getText(Object info) {
            return null;
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public boolean isCheckable(Object info) {
            return false;
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public boolean isChecked(Object info) {
            return false;
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public boolean isClickable(Object info) {
            return false;
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public boolean isEnabled(Object info) {
            return false;
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public boolean isFocusable(Object info) {
            return false;
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public boolean isFocused(Object info) {
            return false;
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public boolean isVisibleToUser(Object info) {
            return false;
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public boolean isAccessibilityFocused(Object info) {
            return false;
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public boolean isLongClickable(Object info) {
            return false;
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public boolean isPassword(Object info) {
            return false;
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public boolean isScrollable(Object info) {
            return false;
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public boolean isSelected(Object info) {
            return false;
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void setBoundsInParent(Object info, Rect bounds) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void setBoundsInScreen(Object info, Rect bounds) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void setCheckable(Object info, boolean checkable) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void setChecked(Object info, boolean checked) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void setClassName(Object info, CharSequence className) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void setClickable(Object info, boolean clickable) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void setContentDescription(Object info, CharSequence contentDescription) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void setEnabled(Object info, boolean enabled) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void setFocusable(Object info, boolean focusable) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void setFocused(Object info, boolean focused) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void setVisibleToUser(Object info, boolean visibleToUser) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void setAccessibilityFocused(Object info, boolean focused) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void setLongClickable(Object info, boolean longClickable) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void setPackageName(Object info, CharSequence packageName) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void setParent(Object info, View parent) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void setScrollable(Object info, boolean scrollable) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void setSelected(Object info, boolean selected) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void setSource(Object info, View source) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void setSource(Object info, View root, int virtualDescendantId) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void recycle(Object info) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public String getViewIdResourceName(Object info) {
            return null;
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void setCollectionInfo(Object info, Object collectionInfo) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public Object getCollectionItemInfo(Object info) {
            return null;
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void setCollectionItemInfo(Object info, Object collectionItemInfo) {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public Object obtainCollectionInfo(int rowCount, int columnCount, boolean hierarchical, int selectionMode) {
            return null;
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public Object obtainCollectionItemInfo(int rowIndex, int rowSpan, int columnIndex, int columnSpan, boolean heading, boolean selected) {
            return null;
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public int getCollectionItemColumnIndex(Object info) {
            return 0;
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public int getCollectionItemColumnSpan(Object info) {
            return 0;
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public int getCollectionItemRowIndex(Object info) {
            return 0;
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public int getCollectionItemRowSpan(Object info) {
            return 0;
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public boolean isCollectionItemSelected(Object info) {
            return false;
        }
    }

    /* loaded from: classes.dex */
    static class AccessibilityNodeInfoIcsImpl extends AccessibilityNodeInfoStubImpl {
        AccessibilityNodeInfoIcsImpl() {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public Object obtain() {
            return AccessibilityNodeInfoCompatIcs.obtain();
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public Object obtain(View source) {
            return AccessibilityNodeInfoCompatIcs.obtain(source);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public Object obtain(Object info) {
            return AccessibilityNodeInfoCompatIcs.obtain(info);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void addAction(Object info, int action) {
            AccessibilityNodeInfoCompatIcs.addAction(info, action);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void addChild(Object info, View child) {
            AccessibilityNodeInfoCompatIcs.addChild(info, child);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public int getActions(Object info) {
            return AccessibilityNodeInfoCompatIcs.getActions(info);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void getBoundsInParent(Object info, Rect outBounds) {
            AccessibilityNodeInfoCompatIcs.getBoundsInParent(info, outBounds);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void getBoundsInScreen(Object info, Rect outBounds) {
            AccessibilityNodeInfoCompatIcs.getBoundsInScreen(info, outBounds);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public int getChildCount(Object info) {
            return AccessibilityNodeInfoCompatIcs.getChildCount(info);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public CharSequence getClassName(Object info) {
            return AccessibilityNodeInfoCompatIcs.getClassName(info);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public CharSequence getContentDescription(Object info) {
            return AccessibilityNodeInfoCompatIcs.getContentDescription(info);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public CharSequence getPackageName(Object info) {
            return AccessibilityNodeInfoCompatIcs.getPackageName(info);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public CharSequence getText(Object info) {
            return AccessibilityNodeInfoCompatIcs.getText(info);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public boolean isCheckable(Object info) {
            return AccessibilityNodeInfoCompatIcs.isCheckable(info);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public boolean isChecked(Object info) {
            return AccessibilityNodeInfoCompatIcs.isChecked(info);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public boolean isClickable(Object info) {
            return AccessibilityNodeInfoCompatIcs.isClickable(info);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public boolean isEnabled(Object info) {
            return AccessibilityNodeInfoCompatIcs.isEnabled(info);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public boolean isFocusable(Object info) {
            return AccessibilityNodeInfoCompatIcs.isFocusable(info);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public boolean isFocused(Object info) {
            return AccessibilityNodeInfoCompatIcs.isFocused(info);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public boolean isLongClickable(Object info) {
            return AccessibilityNodeInfoCompatIcs.isLongClickable(info);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public boolean isPassword(Object info) {
            return AccessibilityNodeInfoCompatIcs.isPassword(info);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public boolean isScrollable(Object info) {
            return AccessibilityNodeInfoCompatIcs.isScrollable(info);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public boolean isSelected(Object info) {
            return AccessibilityNodeInfoCompatIcs.isSelected(info);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void setBoundsInParent(Object info, Rect bounds) {
            AccessibilityNodeInfoCompatIcs.setBoundsInParent(info, bounds);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void setBoundsInScreen(Object info, Rect bounds) {
            AccessibilityNodeInfoCompatIcs.setBoundsInScreen(info, bounds);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void setCheckable(Object info, boolean checkable) {
            AccessibilityNodeInfoCompatIcs.setCheckable(info, checkable);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void setChecked(Object info, boolean checked) {
            AccessibilityNodeInfoCompatIcs.setChecked(info, checked);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void setClassName(Object info, CharSequence className) {
            AccessibilityNodeInfoCompatIcs.setClassName(info, className);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void setClickable(Object info, boolean clickable) {
            AccessibilityNodeInfoCompatIcs.setClickable(info, clickable);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void setContentDescription(Object info, CharSequence contentDescription) {
            AccessibilityNodeInfoCompatIcs.setContentDescription(info, contentDescription);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void setEnabled(Object info, boolean enabled) {
            AccessibilityNodeInfoCompatIcs.setEnabled(info, enabled);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void setFocusable(Object info, boolean focusable) {
            AccessibilityNodeInfoCompatIcs.setFocusable(info, focusable);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void setFocused(Object info, boolean focused) {
            AccessibilityNodeInfoCompatIcs.setFocused(info, focused);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void setLongClickable(Object info, boolean longClickable) {
            AccessibilityNodeInfoCompatIcs.setLongClickable(info, longClickable);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void setPackageName(Object info, CharSequence packageName) {
            AccessibilityNodeInfoCompatIcs.setPackageName(info, packageName);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void setParent(Object info, View parent) {
            AccessibilityNodeInfoCompatIcs.setParent(info, parent);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void setScrollable(Object info, boolean scrollable) {
            AccessibilityNodeInfoCompatIcs.setScrollable(info, scrollable);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void setSelected(Object info, boolean selected) {
            AccessibilityNodeInfoCompatIcs.setSelected(info, selected);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void setSource(Object info, View source) {
            AccessibilityNodeInfoCompatIcs.setSource(info, source);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void recycle(Object info) {
            AccessibilityNodeInfoCompatIcs.recycle(info);
        }
    }

    /* loaded from: classes.dex */
    static class AccessibilityNodeInfoJellybeanImpl extends AccessibilityNodeInfoIcsImpl {
        AccessibilityNodeInfoJellybeanImpl() {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void addChild(Object info, View child, int virtualDescendantId) {
            AccessibilityNodeInfoCompatJellyBean.addChild(info, child, virtualDescendantId);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void setSource(Object info, View root, int virtualDescendantId) {
            AccessibilityNodeInfoCompatJellyBean.setSource(info, root, virtualDescendantId);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public boolean isVisibleToUser(Object info) {
            return AccessibilityNodeInfoCompatJellyBean.isVisibleToUser(info);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void setVisibleToUser(Object info, boolean visibleToUser) {
            AccessibilityNodeInfoCompatJellyBean.setVisibleToUser(info, visibleToUser);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public boolean isAccessibilityFocused(Object info) {
            return AccessibilityNodeInfoCompatJellyBean.isAccessibilityFocused(info);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void setAccessibilityFocused(Object info, boolean focused) {
            AccessibilityNodeInfoCompatJellyBean.setAccesibilityFocused(info, focused);
        }
    }

    /* loaded from: classes.dex */
    static class AccessibilityNodeInfoJellybeanMr1Impl extends AccessibilityNodeInfoJellybeanImpl {
        AccessibilityNodeInfoJellybeanMr1Impl() {
        }
    }

    /* loaded from: classes.dex */
    static class AccessibilityNodeInfoJellybeanMr2Impl extends AccessibilityNodeInfoJellybeanMr1Impl {
        AccessibilityNodeInfoJellybeanMr2Impl() {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public String getViewIdResourceName(Object info) {
            return AccessibilityNodeInfoCompatJellybeanMr2.getViewIdResourceName(info);
        }
    }

    /* loaded from: classes.dex */
    static class AccessibilityNodeInfoKitKatImpl extends AccessibilityNodeInfoJellybeanMr2Impl {
        AccessibilityNodeInfoKitKatImpl() {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void setCollectionInfo(Object info, Object collectionInfo) {
            AccessibilityNodeInfoCompatKitKat.setCollectionInfo(info, collectionInfo);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public Object obtainCollectionInfo(int rowCount, int columnCount, boolean hierarchical, int selectionMode) {
            return AccessibilityNodeInfoCompatKitKat.obtainCollectionInfo(rowCount, columnCount, hierarchical, selectionMode);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public Object obtainCollectionItemInfo(int rowIndex, int rowSpan, int columnIndex, int columnSpan, boolean heading, boolean selected) {
            return AccessibilityNodeInfoCompatKitKat.obtainCollectionItemInfo(rowIndex, rowSpan, columnIndex, columnSpan, heading);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public Object getCollectionItemInfo(Object info) {
            return AccessibilityNodeInfoCompatKitKat.getCollectionItemInfo(info);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public int getCollectionItemColumnIndex(Object info) {
            return AccessibilityNodeInfoCompatKitKat.CollectionItemInfo.getColumnIndex(info);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public int getCollectionItemColumnSpan(Object info) {
            return AccessibilityNodeInfoCompatKitKat.CollectionItemInfo.getColumnSpan(info);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public int getCollectionItemRowIndex(Object info) {
            return AccessibilityNodeInfoCompatKitKat.CollectionItemInfo.getRowIndex(info);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public int getCollectionItemRowSpan(Object info) {
            return AccessibilityNodeInfoCompatKitKat.CollectionItemInfo.getRowSpan(info);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void setCollectionItemInfo(Object info, Object collectionItemInfo) {
            AccessibilityNodeInfoCompatKitKat.setCollectionItemInfo(info, collectionItemInfo);
        }
    }

    /* loaded from: classes.dex */
    static class AccessibilityNodeInfoApi21Impl extends AccessibilityNodeInfoKitKatImpl {
        AccessibilityNodeInfoApi21Impl() {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public Object newAccessibilityAction(int actionId, CharSequence label) {
            return AccessibilityNodeInfoCompatApi21.newAccessibilityAction(actionId, label);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoKitKatImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public Object obtainCollectionInfo(int rowCount, int columnCount, boolean hierarchical, int selectionMode) {
            return AccessibilityNodeInfoCompatApi21.obtainCollectionInfo(rowCount, columnCount, hierarchical, selectionMode);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public void addAction(Object info, Object action) {
            AccessibilityNodeInfoCompatApi21.addAction(info, action);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public boolean removeAction(Object info, Object action) {
            return AccessibilityNodeInfoCompatApi21.removeAction(info, action);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoKitKatImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public Object obtainCollectionItemInfo(int rowIndex, int rowSpan, int columnIndex, int columnSpan, boolean heading, boolean selected) {
            return AccessibilityNodeInfoCompatApi21.obtainCollectionItemInfo(rowIndex, rowSpan, columnIndex, columnSpan, heading, selected);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl, android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityNodeInfoImpl
        public boolean isCollectionItemSelected(Object info) {
            return AccessibilityNodeInfoCompatApi21.CollectionItemInfo.isSelected(info);
        }
    }

    /* loaded from: classes.dex */
    static class AccessibilityNodeInfoApi22Impl extends AccessibilityNodeInfoApi21Impl {
        AccessibilityNodeInfoApi22Impl() {
        }
    }

    /* loaded from: classes.dex */
    static class AccessibilityNodeInfoApi24Impl extends AccessibilityNodeInfoApi22Impl {
        AccessibilityNodeInfoApi24Impl() {
        }
    }

    static {
        if (Build.VERSION.SDK_INT >= 24) {
            IMPL = new AccessibilityNodeInfoApi24Impl();
        } else if (Build.VERSION.SDK_INT >= 22) {
            IMPL = new AccessibilityNodeInfoApi22Impl();
        } else if (Build.VERSION.SDK_INT >= 21) {
            IMPL = new AccessibilityNodeInfoApi21Impl();
        } else if (Build.VERSION.SDK_INT >= 19) {
            IMPL = new AccessibilityNodeInfoKitKatImpl();
        } else if (Build.VERSION.SDK_INT >= 18) {
            IMPL = new AccessibilityNodeInfoJellybeanMr2Impl();
        } else if (Build.VERSION.SDK_INT >= 17) {
            IMPL = new AccessibilityNodeInfoJellybeanMr1Impl();
        } else if (Build.VERSION.SDK_INT >= 16) {
            IMPL = new AccessibilityNodeInfoJellybeanImpl();
        } else if (Build.VERSION.SDK_INT >= 14) {
            IMPL = new AccessibilityNodeInfoIcsImpl();
        } else {
            IMPL = new AccessibilityNodeInfoStubImpl();
        }
    }

    static AccessibilityNodeInfoCompat wrapNonNullInstance(Object object) {
        if (object != null) {
            return new AccessibilityNodeInfoCompat(object);
        }
        return null;
    }

    public AccessibilityNodeInfoCompat(Object info) {
        this.mInfo = info;
    }

    public Object getInfo() {
        return this.mInfo;
    }

    public static AccessibilityNodeInfoCompat obtain(View source) {
        return wrapNonNullInstance(IMPL.obtain(source));
    }

    public static AccessibilityNodeInfoCompat obtain() {
        return wrapNonNullInstance(IMPL.obtain());
    }

    public static AccessibilityNodeInfoCompat obtain(AccessibilityNodeInfoCompat info) {
        return wrapNonNullInstance(IMPL.obtain(info.mInfo));
    }

    public void setSource(View source) {
        IMPL.setSource(this.mInfo, source);
    }

    public void setSource(View root, int virtualDescendantId) {
        IMPL.setSource(this.mInfo, root, virtualDescendantId);
    }

    public int getChildCount() {
        return IMPL.getChildCount(this.mInfo);
    }

    public void addChild(View child) {
        IMPL.addChild(this.mInfo, child);
    }

    public void addChild(View root, int virtualDescendantId) {
        IMPL.addChild(this.mInfo, root, virtualDescendantId);
    }

    public int getActions() {
        return IMPL.getActions(this.mInfo);
    }

    public void addAction(int action) {
        IMPL.addAction(this.mInfo, action);
    }

    public void addAction(AccessibilityActionCompat action) {
        IMPL.addAction(this.mInfo, action.mAction);
    }

    public boolean removeAction(AccessibilityActionCompat action) {
        return IMPL.removeAction(this.mInfo, action.mAction);
    }

    public void setParent(View parent) {
        IMPL.setParent(this.mInfo, parent);
    }

    public void getBoundsInParent(Rect outBounds) {
        IMPL.getBoundsInParent(this.mInfo, outBounds);
    }

    public void setBoundsInParent(Rect bounds) {
        IMPL.setBoundsInParent(this.mInfo, bounds);
    }

    public void getBoundsInScreen(Rect outBounds) {
        IMPL.getBoundsInScreen(this.mInfo, outBounds);
    }

    public void setBoundsInScreen(Rect bounds) {
        IMPL.setBoundsInScreen(this.mInfo, bounds);
    }

    public boolean isCheckable() {
        return IMPL.isCheckable(this.mInfo);
    }

    public void setCheckable(boolean checkable) {
        IMPL.setCheckable(this.mInfo, checkable);
    }

    public boolean isChecked() {
        return IMPL.isChecked(this.mInfo);
    }

    public void setChecked(boolean checked) {
        IMPL.setChecked(this.mInfo, checked);
    }

    public boolean isFocusable() {
        return IMPL.isFocusable(this.mInfo);
    }

    public void setFocusable(boolean focusable) {
        IMPL.setFocusable(this.mInfo, focusable);
    }

    public boolean isFocused() {
        return IMPL.isFocused(this.mInfo);
    }

    public void setFocused(boolean focused) {
        IMPL.setFocused(this.mInfo, focused);
    }

    public boolean isVisibleToUser() {
        return IMPL.isVisibleToUser(this.mInfo);
    }

    public void setVisibleToUser(boolean visibleToUser) {
        IMPL.setVisibleToUser(this.mInfo, visibleToUser);
    }

    public boolean isAccessibilityFocused() {
        return IMPL.isAccessibilityFocused(this.mInfo);
    }

    public void setAccessibilityFocused(boolean focused) {
        IMPL.setAccessibilityFocused(this.mInfo, focused);
    }

    public boolean isSelected() {
        return IMPL.isSelected(this.mInfo);
    }

    public void setSelected(boolean selected) {
        IMPL.setSelected(this.mInfo, selected);
    }

    public boolean isClickable() {
        return IMPL.isClickable(this.mInfo);
    }

    public void setClickable(boolean clickable) {
        IMPL.setClickable(this.mInfo, clickable);
    }

    public boolean isLongClickable() {
        return IMPL.isLongClickable(this.mInfo);
    }

    public void setLongClickable(boolean longClickable) {
        IMPL.setLongClickable(this.mInfo, longClickable);
    }

    public boolean isEnabled() {
        return IMPL.isEnabled(this.mInfo);
    }

    public void setEnabled(boolean enabled) {
        IMPL.setEnabled(this.mInfo, enabled);
    }

    public boolean isPassword() {
        return IMPL.isPassword(this.mInfo);
    }

    public boolean isScrollable() {
        return IMPL.isScrollable(this.mInfo);
    }

    public void setScrollable(boolean scrollable) {
        IMPL.setScrollable(this.mInfo, scrollable);
    }

    public CharSequence getPackageName() {
        return IMPL.getPackageName(this.mInfo);
    }

    public void setPackageName(CharSequence packageName) {
        IMPL.setPackageName(this.mInfo, packageName);
    }

    public CharSequence getClassName() {
        return IMPL.getClassName(this.mInfo);
    }

    public void setClassName(CharSequence className) {
        IMPL.setClassName(this.mInfo, className);
    }

    public CharSequence getText() {
        return IMPL.getText(this.mInfo);
    }

    public CharSequence getContentDescription() {
        return IMPL.getContentDescription(this.mInfo);
    }

    public void setContentDescription(CharSequence contentDescription) {
        IMPL.setContentDescription(this.mInfo, contentDescription);
    }

    public void recycle() {
        IMPL.recycle(this.mInfo);
    }

    public String getViewIdResourceName() {
        return IMPL.getViewIdResourceName(this.mInfo);
    }

    public void setCollectionInfo(Object collectionInfo) {
        IMPL.setCollectionInfo(this.mInfo, ((CollectionInfoCompat) collectionInfo).mInfo);
    }

    public void setCollectionItemInfo(Object collectionItemInfo) {
        IMPL.setCollectionItemInfo(this.mInfo, ((CollectionItemInfoCompat) collectionItemInfo).mInfo);
    }

    public CollectionItemInfoCompat getCollectionItemInfo() {
        Object info = IMPL.getCollectionItemInfo(this.mInfo);
        if (info == null) {
            return null;
        }
        return new CollectionItemInfoCompat(info, null);
    }

    public int hashCode() {
        if (this.mInfo == null) {
            return 0;
        }
        return this.mInfo.hashCode();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && getClass() == obj.getClass()) {
            AccessibilityNodeInfoCompat other = (AccessibilityNodeInfoCompat) obj;
            if (this.mInfo == null) {
                if (other.mInfo != null) {
                    return false;
                }
            } else if (!this.mInfo.equals(other.mInfo)) {
                return false;
            }
            return true;
        }
        return false;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString());
        Rect bounds = new Rect();
        getBoundsInParent(bounds);
        builder.append("; boundsInParent: ").append(bounds);
        getBoundsInScreen(bounds);
        builder.append("; boundsInScreen: ").append(bounds);
        builder.append("; packageName: ").append(getPackageName());
        builder.append("; className: ").append(getClassName());
        builder.append("; text: ").append(getText());
        builder.append("; contentDescription: ").append(getContentDescription());
        builder.append("; viewId: ").append(getViewIdResourceName());
        builder.append("; checkable: ").append(isCheckable());
        builder.append("; checked: ").append(isChecked());
        builder.append("; focusable: ").append(isFocusable());
        builder.append("; focused: ").append(isFocused());
        builder.append("; selected: ").append(isSelected());
        builder.append("; clickable: ").append(isClickable());
        builder.append("; longClickable: ").append(isLongClickable());
        builder.append("; enabled: ").append(isEnabled());
        builder.append("; password: ").append(isPassword());
        builder.append("; scrollable: ").append(isScrollable());
        builder.append("; [");
        int actionBits = getActions();
        while (actionBits != 0) {
            int action = 1 << Integer.numberOfTrailingZeros(actionBits);
            actionBits &= ~action;
            builder.append(getActionSymbolicName(action));
            if (actionBits != 0) {
                builder.append(", ");
            }
        }
        builder.append("]");
        return builder.toString();
    }

    private static String getActionSymbolicName(int action) {
        switch (action) {
            case 1:
                return "ACTION_FOCUS";
            case 2:
                return "ACTION_CLEAR_FOCUS";
            case 4:
                return "ACTION_SELECT";
            case 8:
                return "ACTION_CLEAR_SELECTION";
            case 16:
                return "ACTION_CLICK";
            case 32:
                return "ACTION_LONG_CLICK";
            case 64:
                return "ACTION_ACCESSIBILITY_FOCUS";
            case 128:
                return "ACTION_CLEAR_ACCESSIBILITY_FOCUS";
            case 256:
                return "ACTION_NEXT_AT_MOVEMENT_GRANULARITY";
            case 512:
                return "ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY";
            case 1024:
                return "ACTION_NEXT_HTML_ELEMENT";
            case 2048:
                return "ACTION_PREVIOUS_HTML_ELEMENT";
            case 4096:
                return "ACTION_SCROLL_FORWARD";
            case 8192:
                return "ACTION_SCROLL_BACKWARD";
            case 16384:
                return "ACTION_COPY";
            case 32768:
                return "ACTION_PASTE";
            case 65536:
                return "ACTION_CUT";
            case 131072:
                return "ACTION_SET_SELECTION";
            default:
                return "ACTION_UNKNOWN";
        }
    }
}
