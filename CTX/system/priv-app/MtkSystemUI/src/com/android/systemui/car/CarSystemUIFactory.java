package com.android.systemui.car;

import android.content.Context;
import android.util.ArrayMap;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.ViewMediatorCallback;
import com.android.systemui.Dependency;
import com.android.systemui.SystemUIFactory;
import com.android.systemui.statusbar.NotificationEntryManager;
import com.android.systemui.statusbar.car.CarFacetButtonController;
import com.android.systemui.statusbar.car.CarStatusBarKeyguardViewManager;
import com.android.systemui.statusbar.car.hvac.HvacController;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
/* loaded from: classes.dex */
public class CarSystemUIFactory extends SystemUIFactory {
    @Override // com.android.systemui.SystemUIFactory
    public StatusBarKeyguardViewManager createStatusBarKeyguardViewManager(Context context, ViewMediatorCallback viewMediatorCallback, LockPatternUtils lockPatternUtils) {
        return new CarStatusBarKeyguardViewManager(context, viewMediatorCallback, lockPatternUtils);
    }

    @Override // com.android.systemui.SystemUIFactory
    public void injectDependencies(ArrayMap<Object, Dependency.DependencyProvider> arrayMap, final Context context) {
        super.injectDependencies(arrayMap, context);
        arrayMap.put(NotificationEntryManager.class, new Dependency.DependencyProvider() { // from class: com.android.systemui.car.-$$Lambda$CarSystemUIFactory$zPpvA4l4np15nluBhyI66mlexBs
            @Override // com.android.systemui.Dependency.DependencyProvider
            public final Object createDependency() {
                return CarSystemUIFactory.lambda$injectDependencies$0(context);
            }
        });
        arrayMap.put(CarFacetButtonController.class, new Dependency.DependencyProvider() { // from class: com.android.systemui.car.-$$Lambda$CarSystemUIFactory$AxN4qggzdnVRrvEYcqEOS87TAsg
            @Override // com.android.systemui.Dependency.DependencyProvider
            public final Object createDependency() {
                return CarSystemUIFactory.lambda$injectDependencies$1(context);
            }
        });
        arrayMap.put(HvacController.class, new Dependency.DependencyProvider() { // from class: com.android.systemui.car.-$$Lambda$CarSystemUIFactory$cECZooCw_L3zo-kWCmXHsuhl54E
            @Override // com.android.systemui.Dependency.DependencyProvider
            public final Object createDependency() {
                return CarSystemUIFactory.lambda$injectDependencies$2(context);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ Object lambda$injectDependencies$0(Context context) {
        return new CarNotificationEntryManager(context);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ Object lambda$injectDependencies$1(Context context) {
        return new CarFacetButtonController(context);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ Object lambda$injectDependencies$2(Context context) {
        return new HvacController(context);
    }
}
