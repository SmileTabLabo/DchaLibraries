package com.android.settings.wifi.calling;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.PersistableBundle;
import android.support.v4.graphics.drawable.IconCompat;
import android.support.v4.util.Consumer;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import androidx.slice.Slice;
import androidx.slice.builders.ListBuilder;
import androidx.slice.builders.SliceAction;
import com.android.ims.ImsManager;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.slices.SliceBroadcastReceiver;
import com.android.settings.slices.SliceBuilderUtils;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
/* loaded from: classes.dex */
public class WifiCallingSliceHelper {
    public static final Uri WIFI_CALLING_URI = new Uri.Builder().scheme("content").authority("com.android.settings.slices").appendPath("wifi_calling").build();
    private final Context mContext;
    protected SubscriptionManager mSubscriptionManager;

    @VisibleForTesting
    public WifiCallingSliceHelper(Context context) {
        this.mContext = context;
    }

    public Slice createWifiCallingSlice(Uri uri) {
        int defaultVoiceSubId = getDefaultVoiceSubId();
        String simCarrierName = getSimCarrierName();
        if (defaultVoiceSubId <= -1) {
            Log.d("WifiCallingSliceHelper", "Invalid subscription Id");
            return getNonActionableWifiCallingSlice(this.mContext.getString(R.string.wifi_calling_settings_title), this.mContext.getString(R.string.wifi_calling_not_supported, simCarrierName), uri, getSettingsIntent(this.mContext));
        }
        ImsManager imsManager = getImsManager(defaultVoiceSubId);
        if (!imsManager.isWfcEnabledByPlatform() || !imsManager.isWfcProvisionedOnDevice()) {
            Log.d("WifiCallingSliceHelper", "Wifi calling is either not provisioned or not enabled by Platform");
            return getNonActionableWifiCallingSlice(this.mContext.getString(R.string.wifi_calling_settings_title), this.mContext.getString(R.string.wifi_calling_not_supported, simCarrierName), uri, getSettingsIntent(this.mContext));
        }
        try {
            boolean isWifiCallingEnabled = isWifiCallingEnabled(imsManager);
            if (getWifiCallingCarrierActivityIntent(defaultVoiceSubId) != null && !isWifiCallingEnabled) {
                Log.d("WifiCallingSliceHelper", "Needs Activation");
                return getNonActionableWifiCallingSlice(this.mContext.getString(R.string.wifi_calling_settings_title), this.mContext.getString(R.string.wifi_calling_settings_activation_instructions), uri, getActivityIntent("android.settings.WIFI_CALLING_SETTINGS"));
            }
            return getWifiCallingSlice(uri, this.mContext, isWifiCallingEnabled);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            Log.e("WifiCallingSliceHelper", "Unable to read the current WiFi calling status", e);
            return getNonActionableWifiCallingSlice(this.mContext.getString(R.string.wifi_calling_settings_title), this.mContext.getString(R.string.wifi_calling_turn_on), uri, getActivityIntent("android.settings.WIFI_CALLING_SETTINGS"));
        }
    }

    private boolean isWifiCallingEnabled(final ImsManager imsManager) throws InterruptedException, ExecutionException, TimeoutException {
        FutureTask futureTask = new FutureTask(new Callable<Boolean>() { // from class: com.android.settings.wifi.calling.WifiCallingSliceHelper.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // java.util.concurrent.Callable
            public Boolean call() {
                return Boolean.valueOf(imsManager.isWfcEnabledByUser());
            }
        });
        Executors.newSingleThreadExecutor().execute(futureTask);
        Boolean.valueOf(false);
        return ((Boolean) futureTask.get(2000L, TimeUnit.MILLISECONDS)).booleanValue() && imsManager.isNonTtyOrTtyOnVolteEnabled();
    }

    private Slice getWifiCallingSlice(Uri uri, Context context, final boolean z) {
        final IconCompat createWithResource = IconCompat.createWithResource(context, (int) R.drawable.wifi_signal);
        final String string = context.getString(R.string.wifi_calling_settings_title);
        return new ListBuilder(context, uri, -1L).setColor(R.color.material_blue_500).addRow(new Consumer() { // from class: com.android.settings.wifi.calling.-$$Lambda$WifiCallingSliceHelper$zbtZymRgbM5OtQTuVraAeUKJDfQ
            @Override // android.support.v4.util.Consumer
            public final void accept(Object obj) {
                ListBuilder.RowBuilder rowBuilder = (ListBuilder.RowBuilder) obj;
                rowBuilder.setTitle(r1).addEndItem(new SliceAction(r0.getBroadcastIntent("com.android.settings.wifi.calling.action.WIFI_CALLING_CHANGED"), (CharSequence) null, z)).setPrimaryAction(new SliceAction(WifiCallingSliceHelper.this.getActivityIntent("android.settings.WIFI_CALLING_SETTINGS"), createWithResource, string));
            }
        }).build();
    }

    protected ImsManager getImsManager(int i) {
        return ImsManager.getInstance(this.mContext, SubscriptionManager.getPhoneId(i));
    }

    public void handleWifiCallingChanged(Intent intent) {
        int defaultVoiceSubId = getDefaultVoiceSubId();
        if (defaultVoiceSubId > -1) {
            ImsManager imsManager = getImsManager(defaultVoiceSubId);
            if (imsManager.isWfcEnabledByPlatform() || imsManager.isWfcProvisionedOnDevice()) {
                boolean z = imsManager.isWfcEnabledByUser() && imsManager.isNonTtyOrTtyOnVolteEnabled();
                boolean booleanExtra = intent.getBooleanExtra("android.app.slice.extra.TOGGLE_STATE", z);
                Intent wifiCallingCarrierActivityIntent = getWifiCallingCarrierActivityIntent(defaultVoiceSubId);
                if ((!booleanExtra || wifiCallingCarrierActivityIntent == null) && booleanExtra != z) {
                    imsManager.setWfcSetting(booleanExtra);
                }
            }
        }
        this.mContext.getContentResolver().notifyChange(SliceBuilderUtils.getUri("wifi_calling", false), null);
    }

    private Slice getNonActionableWifiCallingSlice(final String str, final String str2, Uri uri, final PendingIntent pendingIntent) {
        final IconCompat createWithResource = IconCompat.createWithResource(this.mContext, (int) R.drawable.wifi_signal);
        return new ListBuilder(this.mContext, uri, -1L).setColor(R.color.material_blue_500).addRow(new Consumer() { // from class: com.android.settings.wifi.calling.-$$Lambda$WifiCallingSliceHelper$6JNBI7DQgipwzIQhGGlqsYB5PlI
            @Override // android.support.v4.util.Consumer
            public final void accept(Object obj) {
                ListBuilder.RowBuilder rowBuilder = (ListBuilder.RowBuilder) obj;
                rowBuilder.setTitle(r0).setSubtitle(str2).setPrimaryAction(new SliceAction(pendingIntent, createWithResource, str));
            }
        }).build();
    }

    protected CarrierConfigManager getCarrierConfigManager(Context context) {
        return (CarrierConfigManager) context.getSystemService(CarrierConfigManager.class);
    }

    protected int getDefaultVoiceSubId() {
        if (this.mSubscriptionManager == null) {
            this.mSubscriptionManager = (SubscriptionManager) this.mContext.getSystemService(SubscriptionManager.class);
        }
        return SubscriptionManager.getDefaultVoiceSubscriptionId();
    }

    protected Intent getWifiCallingCarrierActivityIntent(int i) {
        PersistableBundle configForSubId;
        ComponentName unflattenFromString;
        CarrierConfigManager carrierConfigManager = getCarrierConfigManager(this.mContext);
        if (carrierConfigManager == null || (configForSubId = carrierConfigManager.getConfigForSubId(i)) == null) {
            return null;
        }
        String string = configForSubId.getString("wfc_emergency_address_carrier_app_string");
        if (TextUtils.isEmpty(string) || (unflattenFromString = ComponentName.unflattenFromString(string)) == null) {
            return null;
        }
        Intent intent = new Intent();
        intent.setComponent(unflattenFromString);
        return intent;
    }

    public static PendingIntent getSettingsIntent(Context context) {
        return PendingIntent.getActivity(context, 0, new Intent("android.settings.SETTINGS"), 0);
    }

    private PendingIntent getBroadcastIntent(String str) {
        Intent intent = new Intent(str);
        intent.setClass(this.mContext, SliceBroadcastReceiver.class);
        return PendingIntent.getBroadcast(this.mContext, 0, intent, 268435456);
    }

    private PendingIntent getActivityIntent(String str) {
        Intent intent = new Intent(str);
        intent.addFlags(268435456);
        return PendingIntent.getActivity(this.mContext, 0, intent, 0);
    }

    private String getSimCarrierName() {
        CharSequence simCarrierIdName = ((TelephonyManager) this.mContext.getSystemService(TelephonyManager.class)).getSimCarrierIdName();
        if (simCarrierIdName == null) {
            return this.mContext.getString(R.string.carrier);
        }
        return simCarrierIdName.toString();
    }
}
