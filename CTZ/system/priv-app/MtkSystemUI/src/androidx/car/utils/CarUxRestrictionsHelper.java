package androidx.car.utils;

import android.car.Car;
import android.car.CarNotConnectedException;
import android.car.drivingstate.CarUxRestrictionsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
/* loaded from: classes.dex */
public class CarUxRestrictionsHelper {
    private final Car mCar;
    private CarUxRestrictionsManager mCarUxRestrictionsManager;
    private final CarUxRestrictionsManager.OnUxRestrictionsChangedListener mListener;
    private final ServiceConnection mServiceConnection = new ServiceConnection() { // from class: androidx.car.utils.CarUxRestrictionsHelper.1
        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                CarUxRestrictionsHelper.this.mCarUxRestrictionsManager = (CarUxRestrictionsManager) CarUxRestrictionsHelper.this.mCar.getCarManager("uxrestriction");
                CarUxRestrictionsHelper.this.mCarUxRestrictionsManager.registerListener(CarUxRestrictionsHelper.this.mListener);
                CarUxRestrictionsHelper.this.mListener.onUxRestrictionsChanged(CarUxRestrictionsHelper.this.mCarUxRestrictionsManager.getCurrentCarUxRestrictions());
            } catch (CarNotConnectedException e) {
                e.printStackTrace();
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            CarUxRestrictionsHelper.this.mCarUxRestrictionsManager = null;
        }
    };

    public CarUxRestrictionsHelper(Context context, CarUxRestrictionsManager.OnUxRestrictionsChangedListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null.");
        }
        this.mListener = listener;
        this.mCar = Car.createCar(context, this.mServiceConnection);
    }
}
