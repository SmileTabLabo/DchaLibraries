package android.support.v4.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
/* loaded from: a.zip:android/support/v4/view/LayoutInflaterCompatBase.class */
class LayoutInflaterCompatBase {

    /* loaded from: a.zip:android/support/v4/view/LayoutInflaterCompatBase$FactoryWrapper.class */
    static class FactoryWrapper implements LayoutInflater.Factory {
        final LayoutInflaterFactory mDelegateFactory;

        /* JADX INFO: Access modifiers changed from: package-private */
        public FactoryWrapper(LayoutInflaterFactory layoutInflaterFactory) {
            this.mDelegateFactory = layoutInflaterFactory;
        }

        @Override // android.view.LayoutInflater.Factory
        public View onCreateView(String str, Context context, AttributeSet attributeSet) {
            return this.mDelegateFactory.onCreateView(null, str, context, attributeSet);
        }

        public String toString() {
            return getClass().getName() + "{" + this.mDelegateFactory + "}";
        }
    }

    LayoutInflaterCompatBase() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void setFactory(LayoutInflater layoutInflater, LayoutInflaterFactory layoutInflaterFactory) {
        FactoryWrapper factoryWrapper = null;
        if (layoutInflaterFactory != null) {
            factoryWrapper = new FactoryWrapper(layoutInflaterFactory);
        }
        layoutInflater.setFactory(factoryWrapper);
    }
}
