package com.android.systemui.volume;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.car.Car;
import android.car.CarNotConnectedException;
import android.car.media.CarAudioManager;
import android.car.media.ICarVolumeCallback;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.ServiceConnection;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.Xml;
import android.view.ContextThemeWrapper;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import androidx.car.widget.ListItem;
import androidx.car.widget.ListItemAdapter;
import androidx.car.widget.ListItemProvider;
import androidx.car.widget.PagedListView;
import androidx.car.widget.SeekbarListItem;
import com.android.systemui.R;
import com.android.systemui.plugins.VolumeDialog;
import com.android.systemui.volume.SystemUIInterpolators;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.xmlpull.v1.XmlPullParserException;
/* loaded from: classes.dex */
public class CarVolumeDialogImpl implements VolumeDialog {
    private static final String TAG = Util.logTag(CarVolumeDialogImpl.class);
    private Car mCar;
    private CarAudioManager mCarAudioManager;
    private final Context mContext;
    private CustomDialog mDialog;
    private boolean mExpanded;
    private boolean mHovering;
    private final KeyguardManager mKeyguard;
    private PagedListView mListView;
    private ListItemAdapter mPagedListAdapter;
    private boolean mShowing;
    private Window mWindow;
    private final H mHandler = new H();
    private final SparseArray<VolumeItem> mVolumeItems = new SparseArray<>();
    private final List<VolumeItem> mAvailableVolumeItems = new ArrayList();
    private final List<ListItem> mVolumeLineItems = new ArrayList();
    private final ICarVolumeCallback mVolumeChangeCallback = new ICarVolumeCallback.Stub() { // from class: com.android.systemui.volume.CarVolumeDialogImpl.1
        public void onGroupVolumeChanged(int i, int i2) {
            VolumeItem volumeItem = (VolumeItem) CarVolumeDialogImpl.this.mAvailableVolumeItems.get(i);
            int seekbarValue = CarVolumeDialogImpl.getSeekbarValue(CarVolumeDialogImpl.this.mCarAudioManager, i);
            if (seekbarValue == volumeItem.progress) {
                return;
            }
            volumeItem.listItem.setProgress(seekbarValue);
            volumeItem.progress = seekbarValue;
            if ((i2 & 1) != 0) {
                CarVolumeDialogImpl.this.show(1);
            }
        }

        public void onMasterMuteChanged(int i) {
        }
    };
    private final ServiceConnection mServiceConnection = new ServiceConnection() { // from class: com.android.systemui.volume.CarVolumeDialogImpl.2
        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            try {
                CarVolumeDialogImpl.this.mExpanded = false;
                CarVolumeDialogImpl.this.mCarAudioManager = (CarAudioManager) CarVolumeDialogImpl.this.mCar.getCarManager("audio");
                int volumeGroupCount = CarVolumeDialogImpl.this.mCarAudioManager.getVolumeGroupCount();
                for (int i = 0; i < volumeGroupCount; i++) {
                    VolumeItem volumeItemForUsages = CarVolumeDialogImpl.this.getVolumeItemForUsages(CarVolumeDialogImpl.this.mCarAudioManager.getUsagesForVolumeGroupId(i));
                    CarVolumeDialogImpl.this.mAvailableVolumeItems.add(volumeItemForUsages);
                    if (i == 0) {
                        volumeItemForUsages.defaultItem = true;
                        CarVolumeDialogImpl.this.addSeekbarListItem(volumeItemForUsages, i, R.drawable.car_ic_keyboard_arrow_down, new ExpandIconListener());
                    }
                }
                if (CarVolumeDialogImpl.this.mPagedListAdapter != null) {
                    CarVolumeDialogImpl.this.mPagedListAdapter.notifyDataSetChanged();
                }
                CarVolumeDialogImpl.this.mCarAudioManager.registerVolumeCallback(CarVolumeDialogImpl.this.mVolumeChangeCallback.asBinder());
            } catch (CarNotConnectedException e) {
                Log.e(CarVolumeDialogImpl.TAG, "Car is not connected!", e);
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            CarVolumeDialogImpl.this.cleanupAudioManager();
        }
    };

    public CarVolumeDialogImpl(Context context) {
        this.mContext = new ContextThemeWrapper(context, (int) R.style.qs_theme);
        this.mKeyguard = (KeyguardManager) this.mContext.getSystemService("keyguard");
        this.mCar = Car.createCar(this.mContext, this.mServiceConnection);
    }

    @Override // com.android.systemui.plugins.VolumeDialog
    public void init(int i, VolumeDialog.Callback callback) {
        initDialog();
        this.mCar.connect();
    }

    @Override // com.android.systemui.plugins.VolumeDialog
    public void destroy() {
        this.mHandler.removeCallbacksAndMessages(null);
        cleanupAudioManager();
        this.mCar.disconnect();
    }

    private void initDialog() {
        loadAudioUsageItems();
        this.mVolumeLineItems.clear();
        this.mDialog = new CustomDialog(this.mContext);
        this.mHovering = false;
        this.mShowing = false;
        this.mExpanded = false;
        this.mWindow = this.mDialog.getWindow();
        this.mWindow.requestFeature(1);
        this.mWindow.setBackgroundDrawable(new ColorDrawable(0));
        this.mWindow.clearFlags(65538);
        this.mWindow.addFlags(17563944);
        this.mWindow.setType(2020);
        this.mWindow.setWindowAnimations(16973828);
        WindowManager.LayoutParams attributes = this.mWindow.getAttributes();
        attributes.format = -3;
        attributes.setTitle(VolumeDialogImpl.class.getSimpleName());
        attributes.gravity = 49;
        attributes.windowAnimations = -1;
        this.mWindow.setAttributes(attributes);
        this.mWindow.setLayout(-2, -2);
        this.mDialog.setCanceledOnTouchOutside(true);
        this.mDialog.setContentView(R.layout.car_volume_dialog);
        this.mDialog.setOnShowListener(new DialogInterface.OnShowListener() { // from class: com.android.systemui.volume.-$$Lambda$CarVolumeDialogImpl$vlKmnqSHXeP1A03lvbFsgzjPtc0
            @Override // android.content.DialogInterface.OnShowListener
            public final void onShow(DialogInterface dialogInterface) {
                CarVolumeDialogImpl.lambda$initDialog$0(CarVolumeDialogImpl.this, dialogInterface);
            }
        });
        this.mListView = (PagedListView) this.mWindow.findViewById(R.id.volume_list);
        this.mListView.setOnHoverListener(new View.OnHoverListener() { // from class: com.android.systemui.volume.-$$Lambda$CarVolumeDialogImpl$ttx6YehS3HDGJCSyF1Z5F3v3yDI
            @Override // android.view.View.OnHoverListener
            public final boolean onHover(View view, MotionEvent motionEvent) {
                return CarVolumeDialogImpl.lambda$initDialog$1(CarVolumeDialogImpl.this, view, motionEvent);
            }
        });
        this.mPagedListAdapter = new ListItemAdapter(this.mContext, new ListItemProvider.ListProvider(this.mVolumeLineItems), 3);
        this.mListView.setAdapter(this.mPagedListAdapter);
        this.mListView.setMaxPages(-1);
    }

    public static /* synthetic */ void lambda$initDialog$0(CarVolumeDialogImpl carVolumeDialogImpl, DialogInterface dialogInterface) {
        carVolumeDialogImpl.mListView.setTranslationY(-carVolumeDialogImpl.mListView.getHeight());
        carVolumeDialogImpl.mListView.setAlpha(0.0f);
        carVolumeDialogImpl.mListView.animate().alpha(1.0f).translationY(0.0f).setDuration(250L).setInterpolator(new SystemUIInterpolators.LogDecelerateInterpolator()).start();
    }

    public static /* synthetic */ boolean lambda$initDialog$1(CarVolumeDialogImpl carVolumeDialogImpl, View view, MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        carVolumeDialogImpl.mHovering = actionMasked == 9 || actionMasked == 7;
        carVolumeDialogImpl.rescheduleTimeoutH();
        return true;
    }

    public void show(int i) {
        this.mHandler.obtainMessage(1, i, 0).sendToTarget();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showH(int i) {
        if (D.BUG) {
            String str = TAG;
            Log.d(str, "showH r=" + Events.DISMISS_REASONS[i]);
        }
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(2);
        rescheduleTimeoutH();
        this.mPagedListAdapter.notifyDataSetChanged();
        if (this.mShowing) {
            return;
        }
        this.mShowing = true;
        this.mDialog.show();
        Events.writeEvent(this.mContext, 0, Integer.valueOf(i), Boolean.valueOf(this.mKeyguard.isKeyguardLocked()));
    }

    protected void rescheduleTimeoutH() {
        this.mHandler.removeMessages(2);
        int computeTimeoutH = computeTimeoutH();
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2, 3, 0), computeTimeoutH);
        if (D.BUG) {
            String str = TAG;
            Log.d(str, "rescheduleTimeout " + computeTimeoutH + " " + Debug.getCaller());
        }
    }

    private int computeTimeoutH() {
        return this.mHovering ? 16000 : 3000;
    }

    protected void dismissH(int i) {
        if (D.BUG) {
            String str = TAG;
            Log.d(str, "dismissH r=" + Events.DISMISS_REASONS[i]);
        }
        this.mHandler.removeMessages(2);
        this.mHandler.removeMessages(1);
        if (!this.mShowing) {
            return;
        }
        this.mListView.animate().cancel();
        this.mShowing = false;
        this.mListView.setTranslationY(0.0f);
        this.mListView.setAlpha(1.0f);
        this.mListView.animate().alpha(0.0f).translationY(-this.mListView.getHeight()).setDuration(250L).setInterpolator(new SystemUIInterpolators.LogAccelerateInterpolator()).withEndAction(new Runnable() { // from class: com.android.systemui.volume.-$$Lambda$CarVolumeDialogImpl$rCIGYbfmmruUBwL8pG_nmk6yEXo
            @Override // java.lang.Runnable
            public final void run() {
                r0.mHandler.postDelayed(new Runnable() { // from class: com.android.systemui.volume.-$$Lambda$CarVolumeDialogImpl$Ta8M0dGuIMjlpBaP0lNmvgYiiqA
                    @Override // java.lang.Runnable
                    public final void run() {
                        CarVolumeDialogImpl.lambda$dismissH$2(CarVolumeDialogImpl.this);
                    }
                }, 50L);
            }
        }).start();
        Events.writeEvent(this.mContext, 1, Integer.valueOf(i));
    }

    public static /* synthetic */ void lambda$dismissH$2(CarVolumeDialogImpl carVolumeDialogImpl) {
        if (D.BUG) {
            Log.d(TAG, "mDialog.dismiss()");
        }
        carVolumeDialogImpl.mDialog.dismiss();
    }

    private void loadAudioUsageItems() {
        try {
            XmlResourceParser xml = this.mContext.getResources().getXml(R.xml.car_volume_items);
            AttributeSet asAttributeSet = Xml.asAttributeSet(xml);
            while (true) {
                int next = xml.next();
                if (next == 1 || next == 2) {
                    break;
                }
            }
            if (!"carVolumeItems".equals(xml.getName())) {
                throw new RuntimeException("Meta-data does not start with carVolumeItems tag");
            }
            int depth = xml.getDepth();
            int i = 0;
            while (true) {
                int next2 = xml.next();
                if (next2 == 1 || (next2 == 3 && xml.getDepth() <= depth)) {
                    break;
                } else if (next2 != 3 && "item".equals(xml.getName())) {
                    TypedArray obtainAttributes = this.mContext.getResources().obtainAttributes(asAttributeSet, R.styleable.carVolumeItems_item);
                    int i2 = obtainAttributes.getInt(1, -1);
                    if (i2 >= 0) {
                        VolumeItem volumeItem = new VolumeItem();
                        volumeItem.usage = i2;
                        volumeItem.rank = i;
                        volumeItem.icon = obtainAttributes.getResourceId(0, 0);
                        this.mVolumeItems.put(i2, volumeItem);
                        i++;
                    }
                    obtainAttributes.recycle();
                }
            }
            if (xml != null) {
                xml.close();
            }
        } catch (IOException | XmlPullParserException e) {
            Log.e(TAG, "Error parsing volume groups configuration", e);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public VolumeItem getVolumeItemForUsages(int[] iArr) {
        int i = Integer.MAX_VALUE;
        VolumeItem volumeItem = null;
        for (int i2 : iArr) {
            VolumeItem volumeItem2 = this.mVolumeItems.get(i2);
            if (volumeItem2.rank < i) {
                i = volumeItem2.rank;
                volumeItem = volumeItem2;
            }
        }
        return volumeItem;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int getSeekbarValue(CarAudioManager carAudioManager, int i) {
        try {
            return carAudioManager.getGroupVolume(i);
        } catch (CarNotConnectedException e) {
            Log.e(TAG, "Car is not connected!", e);
            return 0;
        }
    }

    private static int getMaxSeekbarValue(CarAudioManager carAudioManager, int i) {
        try {
            return carAudioManager.getGroupMaxVolume(i);
        } catch (CarNotConnectedException e) {
            Log.e(TAG, "Car is not connected!", e);
            return 0;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public SeekbarListItem addSeekbarListItem(VolumeItem volumeItem, int i, int i2, View.OnClickListener onClickListener) {
        SeekbarListItem seekbarListItem = new SeekbarListItem(this.mContext);
        seekbarListItem.setMax(getMaxSeekbarValue(this.mCarAudioManager, i));
        int color = this.mContext.getResources().getColor(R.color.car_volume_dialog_tint);
        int seekbarValue = getSeekbarValue(this.mCarAudioManager, i);
        seekbarListItem.setProgress(seekbarValue);
        seekbarListItem.setOnSeekBarChangeListener(new VolumeSeekBarChangeListener(i, this.mCarAudioManager));
        Drawable drawable = this.mContext.getResources().getDrawable(volumeItem.icon);
        drawable.setTint(color);
        seekbarListItem.setPrimaryActionIcon(drawable);
        if (i2 != 0) {
            Drawable drawable2 = this.mContext.getResources().getDrawable(i2);
            drawable2.setTint(color);
            seekbarListItem.setSupplementalIcon(drawable2, true, onClickListener);
        } else {
            seekbarListItem.setSupplementalEmptyIcon(true);
        }
        this.mVolumeLineItems.add(seekbarListItem);
        volumeItem.listItem = seekbarListItem;
        volumeItem.progress = seekbarValue;
        return seekbarListItem;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public VolumeItem findVolumeItem(SeekbarListItem seekbarListItem) {
        for (int i = 0; i < this.mVolumeItems.size(); i++) {
            VolumeItem valueAt = this.mVolumeItems.valueAt(i);
            if (valueAt.listItem == seekbarListItem) {
                return valueAt;
            }
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void cleanupAudioManager() {
        try {
            this.mCarAudioManager.unregisterVolumeCallback(this.mVolumeChangeCallback.asBinder());
        } catch (CarNotConnectedException e) {
            Log.e(TAG, "Car is not connected!", e);
        }
        this.mVolumeLineItems.clear();
        this.mCarAudioManager = null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public final class H extends Handler {
        public H() {
            super(Looper.getMainLooper());
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    CarVolumeDialogImpl.this.showH(message.arg1);
                    return;
                case 2:
                    CarVolumeDialogImpl.this.dismissH(message.arg1);
                    return;
                default:
                    return;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public final class CustomDialog extends Dialog implements DialogInterface {
        public CustomDialog(Context context) {
            super(context, R.style.qs_theme);
        }

        @Override // android.app.Dialog, android.view.Window.Callback
        public boolean dispatchTouchEvent(MotionEvent motionEvent) {
            CarVolumeDialogImpl.this.rescheduleTimeoutH();
            return super.dispatchTouchEvent(motionEvent);
        }

        @Override // android.app.Dialog
        protected void onStart() {
            super.setCanceledOnTouchOutside(true);
            super.onStart();
        }

        @Override // android.app.Dialog
        protected void onStop() {
            super.onStop();
        }

        @Override // android.app.Dialog
        public boolean onTouchEvent(MotionEvent motionEvent) {
            if (isShowing() && motionEvent.getAction() == 4) {
                CarVolumeDialogImpl.this.dismissH(1);
                return true;
            }
            return false;
        }
    }

    /* loaded from: classes.dex */
    private final class ExpandIconListener implements View.OnClickListener {
        private ExpandIconListener() {
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            Animator loadAnimator;
            CarVolumeDialogImpl.this.mExpanded = !CarVolumeDialogImpl.this.mExpanded;
            if (!CarVolumeDialogImpl.this.mExpanded) {
                Iterator it = CarVolumeDialogImpl.this.mVolumeLineItems.iterator();
                while (it.hasNext()) {
                    SeekbarListItem seekbarListItem = (SeekbarListItem) it.next();
                    VolumeItem findVolumeItem = CarVolumeDialogImpl.this.findVolumeItem(seekbarListItem);
                    if (!findVolumeItem.defaultItem) {
                        it.remove();
                    } else {
                        seekbarListItem.setProgress(findVolumeItem.progress);
                    }
                }
                loadAnimator = AnimatorInflater.loadAnimator(CarVolumeDialogImpl.this.mContext, R.anim.car_arrow_fade_in_rotate_down);
            } else {
                for (int i = 0; i < CarVolumeDialogImpl.this.mAvailableVolumeItems.size(); i++) {
                    VolumeItem volumeItem = (VolumeItem) CarVolumeDialogImpl.this.mAvailableVolumeItems.get(i);
                    if (!volumeItem.defaultItem) {
                        CarVolumeDialogImpl.this.addSeekbarListItem(volumeItem, i, 0, null);
                    } else {
                        volumeItem.listItem.setProgress(volumeItem.progress);
                    }
                }
                loadAnimator = AnimatorInflater.loadAnimator(CarVolumeDialogImpl.this.mContext, R.anim.car_arrow_fade_in_rotate_up);
            }
            Animator loadAnimator2 = AnimatorInflater.loadAnimator(CarVolumeDialogImpl.this.mContext, R.anim.car_arrow_fade_out);
            loadAnimator.setStartDelay(100L);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(loadAnimator2, loadAnimator);
            animatorSet.setTarget(view);
            animatorSet.start();
            CarVolumeDialogImpl.this.mPagedListAdapter.notifyDataSetChanged();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public final class VolumeSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        private final CarAudioManager mCarAudioManager;
        private final int mVolumeGroupId;

        private VolumeSeekBarChangeListener(int i, CarAudioManager carAudioManager) {
            this.mVolumeGroupId = i;
            this.mCarAudioManager = carAudioManager;
        }

        @Override // android.widget.SeekBar.OnSeekBarChangeListener
        public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
            if (!z) {
                return;
            }
            try {
                if (this.mCarAudioManager == null) {
                    Log.w(CarVolumeDialogImpl.TAG, "Ignoring volume change event because the car isn't connected");
                    return;
                }
                ((VolumeItem) CarVolumeDialogImpl.this.mAvailableVolumeItems.get(this.mVolumeGroupId)).progress = i;
                this.mCarAudioManager.setGroupVolume(this.mVolumeGroupId, i, 0);
            } catch (CarNotConnectedException e) {
                Log.e(CarVolumeDialogImpl.TAG, "Car is not connected!", e);
            }
        }

        @Override // android.widget.SeekBar.OnSeekBarChangeListener
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override // android.widget.SeekBar.OnSeekBarChangeListener
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class VolumeItem {
        private boolean defaultItem;
        private int icon;
        private SeekbarListItem listItem;
        private int progress;
        private int rank;
        private int usage;

        private VolumeItem() {
            this.defaultItem = false;
        }
    }
}
