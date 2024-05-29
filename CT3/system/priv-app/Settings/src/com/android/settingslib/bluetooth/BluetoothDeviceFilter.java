package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothUuid;
import android.os.ParcelUuid;
import android.util.Log;
/* loaded from: classes.dex */
public final class BluetoothDeviceFilter {
    public static final Filter ALL_FILTER = new AllFilter(null);
    public static final Filter BONDED_DEVICE_FILTER = new BondedDeviceFilter(null);
    public static final Filter UNBONDED_DEVICE_FILTER = new UnbondedDeviceFilter(null);
    private static final Filter[] FILTERS = {ALL_FILTER, new AudioFilter(null), new TransferFilter(null), new PanuFilter(null), new NapFilter(null)};

    /* loaded from: classes.dex */
    public interface Filter {
        boolean matches(BluetoothDevice bluetoothDevice);
    }

    private BluetoothDeviceFilter() {
    }

    public static Filter getFilter(int filterType) {
        if (filterType >= 0 && filterType < FILTERS.length) {
            return FILTERS[filterType];
        }
        Log.w("BluetoothDeviceFilter", "Invalid filter type " + filterType + " for device picker");
        return ALL_FILTER;
    }

    /* loaded from: classes.dex */
    private static final class AllFilter implements Filter {
        /* synthetic */ AllFilter(AllFilter allFilter) {
            this();
        }

        private AllFilter() {
        }

        @Override // com.android.settingslib.bluetooth.BluetoothDeviceFilter.Filter
        public boolean matches(BluetoothDevice device) {
            return true;
        }
    }

    /* loaded from: classes.dex */
    private static final class BondedDeviceFilter implements Filter {
        /* synthetic */ BondedDeviceFilter(BondedDeviceFilter bondedDeviceFilter) {
            this();
        }

        private BondedDeviceFilter() {
        }

        @Override // com.android.settingslib.bluetooth.BluetoothDeviceFilter.Filter
        public boolean matches(BluetoothDevice device) {
            return device.getBondState() == 12;
        }
    }

    /* loaded from: classes.dex */
    private static final class UnbondedDeviceFilter implements Filter {
        /* synthetic */ UnbondedDeviceFilter(UnbondedDeviceFilter unbondedDeviceFilter) {
            this();
        }

        private UnbondedDeviceFilter() {
        }

        @Override // com.android.settingslib.bluetooth.BluetoothDeviceFilter.Filter
        public boolean matches(BluetoothDevice device) {
            return device.getBondState() != 12;
        }
    }

    /* loaded from: classes.dex */
    private static abstract class ClassUuidFilter implements Filter {
        /* synthetic */ ClassUuidFilter(ClassUuidFilter classUuidFilter) {
            this();
        }

        abstract boolean matches(ParcelUuid[] parcelUuidArr, BluetoothClass bluetoothClass);

        private ClassUuidFilter() {
        }

        @Override // com.android.settingslib.bluetooth.BluetoothDeviceFilter.Filter
        public boolean matches(BluetoothDevice device) {
            return matches(device.getUuids(), device.getBluetoothClass());
        }
    }

    /* loaded from: classes.dex */
    private static final class AudioFilter extends ClassUuidFilter {
        /* synthetic */ AudioFilter(AudioFilter audioFilter) {
            this();
        }

        private AudioFilter() {
            super(null);
        }

        @Override // com.android.settingslib.bluetooth.BluetoothDeviceFilter.ClassUuidFilter
        boolean matches(ParcelUuid[] uuids, BluetoothClass btClass) {
            if (uuids != null) {
                if (BluetoothUuid.containsAnyUuid(uuids, A2dpProfile.SINK_UUIDS) || BluetoothUuid.containsAnyUuid(uuids, HeadsetProfile.UUIDS)) {
                    return true;
                }
            } else if (btClass != null && (btClass.doesClassMatch(1) || btClass.doesClassMatch(0))) {
                return true;
            }
            return false;
        }
    }

    /* loaded from: classes.dex */
    private static final class TransferFilter extends ClassUuidFilter {
        /* synthetic */ TransferFilter(TransferFilter transferFilter) {
            this();
        }

        private TransferFilter() {
            super(null);
        }

        @Override // com.android.settingslib.bluetooth.BluetoothDeviceFilter.ClassUuidFilter
        boolean matches(ParcelUuid[] uuids, BluetoothClass btClass) {
            if (uuids != null && BluetoothUuid.isUuidPresent(uuids, BluetoothUuid.ObexObjectPush)) {
                return true;
            }
            if (btClass != null) {
                return btClass.doesClassMatch(2);
            }
            return false;
        }
    }

    /* loaded from: classes.dex */
    private static final class PanuFilter extends ClassUuidFilter {
        /* synthetic */ PanuFilter(PanuFilter panuFilter) {
            this();
        }

        private PanuFilter() {
            super(null);
        }

        @Override // com.android.settingslib.bluetooth.BluetoothDeviceFilter.ClassUuidFilter
        boolean matches(ParcelUuid[] uuids, BluetoothClass btClass) {
            if (uuids != null && BluetoothUuid.isUuidPresent(uuids, BluetoothUuid.PANU)) {
                return true;
            }
            if (btClass != null) {
                return btClass.doesClassMatch(4);
            }
            return false;
        }
    }

    /* loaded from: classes.dex */
    private static final class NapFilter extends ClassUuidFilter {
        /* synthetic */ NapFilter(NapFilter napFilter) {
            this();
        }

        private NapFilter() {
            super(null);
        }

        @Override // com.android.settingslib.bluetooth.BluetoothDeviceFilter.ClassUuidFilter
        boolean matches(ParcelUuid[] uuids, BluetoothClass btClass) {
            if (uuids != null && BluetoothUuid.isUuidPresent(uuids, BluetoothUuid.NAP)) {
                return true;
            }
            if (btClass != null) {
                return btClass.doesClassMatch(5);
            }
            return false;
        }
    }
}
