package com.android.systemui.qs.tiles;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.qs.QSTile;
import java.util.Arrays;
import java.util.Objects;
/* loaded from: a.zip:com/android/systemui/qs/tiles/IntentTile.class */
public class IntentTile extends QSTile<QSTile.State> {
    private int mCurrentUserId;
    private String mIntentPackage;
    private Intent mLastIntent;
    private PendingIntent mOnClick;
    private String mOnClickUri;
    private PendingIntent mOnLongClick;
    private String mOnLongClickUri;
    private final BroadcastReceiver mReceiver;

    /* loaded from: a.zip:com/android/systemui/qs/tiles/IntentTile$BytesIcon.class */
    private static class BytesIcon extends QSTile.Icon {
        private final byte[] mBytes;

        public BytesIcon(byte[] bArr) {
            this.mBytes = bArr;
        }

        public boolean equals(Object obj) {
            return obj instanceof BytesIcon ? Arrays.equals(((BytesIcon) obj).mBytes, this.mBytes) : false;
        }

        @Override // com.android.systemui.qs.QSTile.Icon
        public Drawable getDrawable(Context context) {
            return new BitmapDrawable(context.getResources(), BitmapFactory.decodeByteArray(this.mBytes, 0, this.mBytes.length));
        }

        public String toString() {
            return String.format("BytesIcon[len=%s]", Integer.valueOf(this.mBytes.length));
        }
    }

    /* loaded from: a.zip:com/android/systemui/qs/tiles/IntentTile$PackageDrawableIcon.class */
    private class PackageDrawableIcon extends QSTile.Icon {
        private final String mPackage;
        private final int mResId;
        final IntentTile this$0;

        public PackageDrawableIcon(IntentTile intentTile, String str, int i) {
            this.this$0 = intentTile;
            this.mPackage = str;
            this.mResId = i;
        }

        public boolean equals(Object obj) {
            if (obj instanceof PackageDrawableIcon) {
                PackageDrawableIcon packageDrawableIcon = (PackageDrawableIcon) obj;
                boolean z = false;
                if (Objects.equals(packageDrawableIcon.mPackage, this.mPackage)) {
                    z = false;
                    if (packageDrawableIcon.mResId == this.mResId) {
                        z = true;
                    }
                }
                return z;
            }
            return false;
        }

        @Override // com.android.systemui.qs.QSTile.Icon
        public Drawable getDrawable(Context context) {
            try {
                return context.createPackageContext(this.mPackage, 0).getDrawable(this.mResId);
            } catch (Throwable th) {
                Log.w(this.this$0.TAG, "Error loading package drawable pkg=" + this.mPackage + " id=" + this.mResId, th);
                return null;
            }
        }

        public String toString() {
            return String.format("PackageDrawableIcon[pkg=%s,id=0x%08x]", this.mPackage, Integer.valueOf(this.mResId));
        }
    }

    private IntentTile(QSTile.Host host, String str) {
        super(host);
        this.mReceiver = new BroadcastReceiver(this) { // from class: com.android.systemui.qs.tiles.IntentTile.1
            final IntentTile this$0;

            {
                this.this$0 = this;
            }

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                this.this$0.refreshState(intent);
            }
        };
        this.mContext.registerReceiver(this.mReceiver, new IntentFilter(str));
    }

    public static QSTile<?> create(QSTile.Host host, String str) {
        if (str != null && str.startsWith("intent(") && str.endsWith(")")) {
            String substring = str.substring("intent(".length(), str.length() - 1);
            if (substring.isEmpty()) {
                throw new IllegalArgumentException("Empty intent tile spec action");
            }
            return new IntentTile(host, substring);
        }
        throw new IllegalArgumentException("Bad intent tile spec: " + str);
    }

    private void sendIntent(String str, PendingIntent pendingIntent, String str2) {
        try {
            if (pendingIntent != null) {
                if (pendingIntent.isActivity()) {
                    getHost().startActivityDismissingKeyguard(pendingIntent);
                } else {
                    pendingIntent.send();
                }
            } else if (str2 == null) {
            } else {
                this.mContext.sendBroadcastAsUser(Intent.parseUri(str2, 1), new UserHandle(this.mCurrentUserId));
            }
        } catch (Throwable th) {
            Log.w(this.TAG, "Error sending " + str + " intent", th);
        }
    }

    @Override // com.android.systemui.qs.QSTile
    public Intent getLongClickIntent() {
        return null;
    }

    @Override // com.android.systemui.qs.QSTile
    public int getMetricsCategory() {
        return 121;
    }

    @Override // com.android.systemui.qs.QSTile
    public CharSequence getTileLabel() {
        return getState().label;
    }

    @Override // com.android.systemui.qs.QSTile
    protected void handleClick() {
        MetricsLogger.action(this.mContext, getMetricsCategory(), this.mIntentPackage);
        sendIntent("click", this.mOnClick, this.mOnClickUri);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSTile
    public void handleDestroy() {
        super.handleDestroy();
        this.mContext.unregisterReceiver(this.mReceiver);
    }

    @Override // com.android.systemui.qs.QSTile
    protected void handleLongClick() {
        sendIntent("long-click", this.mOnLongClick, this.mOnLongClickUri);
    }

    @Override // com.android.systemui.qs.QSTile
    protected void handleUpdateState(QSTile.State state, Object obj) {
        Intent intent = (Intent) obj;
        Intent intent2 = intent;
        if (intent == null) {
            if (this.mLastIntent == null) {
                return;
            }
            intent2 = this.mLastIntent;
        }
        this.mLastIntent = intent2;
        state.contentDescription = intent2.getStringExtra("contentDescription");
        state.label = intent2.getStringExtra("label");
        state.icon = null;
        byte[] byteArrayExtra = intent2.getByteArrayExtra("iconBitmap");
        if (byteArrayExtra != null) {
            try {
                state.icon = new BytesIcon(byteArrayExtra);
            } catch (Throwable th) {
                Log.w(this.TAG, "Error loading icon bitmap, length " + byteArrayExtra.length, th);
            }
        } else {
            int intExtra = intent2.getIntExtra("iconId", 0);
            if (intExtra != 0) {
                String stringExtra = intent2.getStringExtra("iconPackage");
                if (TextUtils.isEmpty(stringExtra)) {
                    state.icon = QSTile.ResourceIcon.get(intExtra);
                } else {
                    state.icon = new PackageDrawableIcon(this, stringExtra, intExtra);
                }
            }
        }
        this.mOnClick = (PendingIntent) intent2.getParcelableExtra("onClick");
        this.mOnClickUri = intent2.getStringExtra("onClickUri");
        this.mOnLongClick = (PendingIntent) intent2.getParcelableExtra("onLongClick");
        this.mOnLongClickUri = intent2.getStringExtra("onLongClickUri");
        this.mIntentPackage = intent2.getStringExtra("package");
        this.mIntentPackage = this.mIntentPackage == null ? "" : this.mIntentPackage;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSTile
    public void handleUserSwitch(int i) {
        super.handleUserSwitch(i);
        this.mCurrentUserId = i;
    }

    @Override // com.android.systemui.qs.QSTile
    public QSTile.State newTileState() {
        return new QSTile.State();
    }

    @Override // com.android.systemui.qs.QSTile
    public void setListening(boolean z) {
    }
}
