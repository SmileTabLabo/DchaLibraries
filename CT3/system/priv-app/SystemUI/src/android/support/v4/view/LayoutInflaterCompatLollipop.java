package android.support.v4.view;

import android.support.v4.view.LayoutInflaterCompatHC;
import android.view.LayoutInflater;
/* loaded from: a.zip:android/support/v4/view/LayoutInflaterCompatLollipop.class */
class LayoutInflaterCompatLollipop {
    LayoutInflaterCompatLollipop() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void setFactory(LayoutInflater layoutInflater, LayoutInflaterFactory layoutInflaterFactory) {
        LayoutInflaterCompatHC.FactoryWrapperHC factoryWrapperHC = null;
        if (layoutInflaterFactory != null) {
            factoryWrapperHC = new LayoutInflaterCompatHC.FactoryWrapperHC(layoutInflaterFactory);
        }
        layoutInflater.setFactory2(factoryWrapperHC);
    }
}
