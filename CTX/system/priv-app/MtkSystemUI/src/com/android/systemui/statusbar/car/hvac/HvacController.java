package com.android.systemui.statusbar.car.hvac;

import android.car.Car;
import android.car.CarNotConnectedException;
import android.car.hardware.CarPropertyValue;
import android.car.hardware.hvac.CarHvacManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
/* loaded from: classes.dex */
public class HvacController {
    private Car mCar;
    private Context mContext;
    private Handler mHandler;
    private CarHvacManager mHvacManager;
    private HashMap<HvacKey, List<TemperatureView>> mTempComponents = new HashMap<>();
    private ServiceConnection mServiceConnection = new ServiceConnection() { // from class: com.android.systemui.statusbar.car.hvac.HvacController.1
        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            try {
                iBinder.linkToDeath(HvacController.this.mRestart, 0);
                HvacController.this.mHvacManager = (CarHvacManager) HvacController.this.mCar.getCarManager("hvac");
                HvacController.this.mHvacManager.registerCallback(HvacController.this.mHardwareCallback);
                HvacController.this.initComponents();
            } catch (Exception e) {
                Log.e("HvacController", "Failed to correctly connect to HVAC", e);
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            HvacController.this.destroyHvacManager();
        }
    };
    private final IBinder.DeathRecipient mRestart = new AnonymousClass2();
    private final CarHvacManager.CarHvacEventCallback mHardwareCallback = new CarHvacManager.CarHvacEventCallback() { // from class: com.android.systemui.statusbar.car.hvac.HvacController.3
        public void onChangeEvent(CarPropertyValue carPropertyValue) {
            try {
                int areaId = carPropertyValue.getAreaId();
                List<TemperatureView> list = (List) HvacController.this.mTempComponents.get(new HvacKey(carPropertyValue.getPropertyId(), areaId));
                if (list != null && !list.isEmpty()) {
                    float floatValue = ((Float) carPropertyValue.getValue()).floatValue();
                    for (TemperatureView temperatureView : list) {
                        temperatureView.setTemp(floatValue);
                    }
                }
            } catch (Exception e) {
                Log.e("HvacController", "Failed handling hvac change event", e);
            }
        }

        public void onErrorEvent(int i, int i2) {
            Log.d("HvacController", "HVAC error event, propertyId: " + i + " zone: " + i2);
        }
    };

    public HvacController(Context context) {
        this.mContext = context;
    }

    public void connectToCarService() {
        this.mHandler = new Handler();
        this.mCar = Car.createCar(this.mContext, this.mServiceConnection, this.mHandler);
        if (this.mCar != null) {
            this.mCar.connect();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void destroyHvacManager() {
        if (this.mHvacManager != null) {
            this.mHvacManager.unregisterCallback(this.mHardwareCallback);
            this.mHvacManager = null;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.android.systemui.statusbar.car.hvac.HvacController$2  reason: invalid class name */
    /* loaded from: classes.dex */
    public class AnonymousClass2 implements IBinder.DeathRecipient {
        AnonymousClass2() {
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            Log.d("HvacController", "Death of HVAC triggering a restart");
            if (HvacController.this.mCar != null) {
                HvacController.this.mCar.disconnect();
            }
            HvacController.this.destroyHvacManager();
            HvacController.this.mHandler.postDelayed(new Runnable() { // from class: com.android.systemui.statusbar.car.hvac.-$$Lambda$HvacController$2$iHT8Tpg8uVwfmNfI8JJMJYOK4tk
                @Override // java.lang.Runnable
                public final void run() {
                    HvacController.this.mCar.connect();
                }
            }, 5000L);
        }
    }

    public void addHvacTextView(TemperatureView temperatureView) {
        HvacKey hvacKey = new HvacKey(temperatureView.getPropertyId(), temperatureView.getAreaId());
        if (!this.mTempComponents.containsKey(hvacKey)) {
            this.mTempComponents.put(hvacKey, new ArrayList());
        }
        this.mTempComponents.get(hvacKey).add(temperatureView);
        initComponent(temperatureView);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void initComponents() {
        for (Map.Entry<HvacKey, List<TemperatureView>> entry : this.mTempComponents.entrySet()) {
            for (TemperatureView temperatureView : entry.getValue()) {
                initComponent(temperatureView);
            }
        }
    }

    private void initComponent(TemperatureView temperatureView) {
        int propertyId = temperatureView.getPropertyId();
        int areaId = temperatureView.getAreaId();
        try {
            if (this.mHvacManager != null && this.mHvacManager.isPropertyAvailable(propertyId, areaId)) {
                temperatureView.setTemp(this.mHvacManager.getFloatProperty(propertyId, areaId));
                return;
            }
            temperatureView.setTemp(Float.NaN);
        } catch (CarNotConnectedException e) {
            temperatureView.setTemp(Float.NaN);
            Log.e("HvacController", "Failed to get value from hvac service", e);
        }
    }

    public void removeAllComponents() {
        this.mTempComponents.clear();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class HvacKey {
        int mAreaId;
        int mPropertyId;

        public HvacKey(int i, int i2) {
            this.mPropertyId = i;
            this.mAreaId = i2;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            HvacKey hvacKey = (HvacKey) obj;
            if (this.mPropertyId == hvacKey.mPropertyId && this.mAreaId == hvacKey.mAreaId) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return Objects.hash(Integer.valueOf(this.mPropertyId), Integer.valueOf(this.mAreaId));
        }
    }
}
