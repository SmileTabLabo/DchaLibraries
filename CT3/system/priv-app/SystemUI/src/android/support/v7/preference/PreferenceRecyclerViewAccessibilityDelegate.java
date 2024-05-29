package android.support.v7.preference;

import android.os.Bundle;
import android.support.v4.view.AccessibilityDelegateCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerViewAccessibilityDelegate;
import android.view.View;
/* loaded from: a.zip:android/support/v7/preference/PreferenceRecyclerViewAccessibilityDelegate.class */
public class PreferenceRecyclerViewAccessibilityDelegate extends RecyclerViewAccessibilityDelegate {
    final AccessibilityDelegateCompat mDefaultItemDelegate;
    final AccessibilityDelegateCompat mItemDelegate;
    final RecyclerView mRecyclerView;

    public PreferenceRecyclerViewAccessibilityDelegate(RecyclerView recyclerView) {
        super(recyclerView);
        this.mDefaultItemDelegate = super.getItemDelegate();
        this.mItemDelegate = new AccessibilityDelegateCompat(this) { // from class: android.support.v7.preference.PreferenceRecyclerViewAccessibilityDelegate.1
            final PreferenceRecyclerViewAccessibilityDelegate this$0;

            {
                this.this$0 = this;
            }

            @Override // android.support.v4.view.AccessibilityDelegateCompat
            public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
                Preference item;
                this.this$0.mDefaultItemDelegate.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfoCompat);
                int childAdapterPosition = this.this$0.mRecyclerView.getChildAdapterPosition(view);
                RecyclerView.Adapter adapter = this.this$0.mRecyclerView.getAdapter();
                if ((adapter instanceof PreferenceGroupAdapter) && (item = ((PreferenceGroupAdapter) adapter).getItem(childAdapterPosition)) != null) {
                    item.onInitializeAccessibilityNodeInfo(accessibilityNodeInfoCompat);
                }
            }

            @Override // android.support.v4.view.AccessibilityDelegateCompat
            public boolean performAccessibilityAction(View view, int i, Bundle bundle) {
                return this.this$0.mDefaultItemDelegate.performAccessibilityAction(view, i, bundle);
            }
        };
        this.mRecyclerView = recyclerView;
    }

    @Override // android.support.v7.widget.RecyclerViewAccessibilityDelegate
    public AccessibilityDelegateCompat getItemDelegate() {
        return this.mItemDelegate;
    }
}
