package com.android.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
/* loaded from: classes.dex */
public class BandMode extends Activity {
    private static final String[] BAND_NAMES = {"Automatic", "Europe", "United States", "Japan", "Australia", "Australia 2", "Cellular 800", "PCS", "Class 3 (JTACS)", "Class 4 (Korea-PCS)", "Class 5", "Class 6 (IMT2000)", "Class 7 (700Mhz-Upper)", "Class 8 (1800Mhz-Upper)", "Class 9 (900Mhz)", "Class 10 (800Mhz-Secondary)", "Class 11 (Europe PAMR 400Mhz)", "Class 15 (US-AWS)", "Class 16 (US-2500Mhz)"};
    private ListView mBandList;
    private ArrayAdapter mBandListAdapter;
    private DialogInterface mProgressPanel;
    private BandListItem mTargetBand = null;
    private Phone mPhone = null;
    private AdapterView.OnItemClickListener mBandSelectionHandler = new AdapterView.OnItemClickListener() { // from class: com.android.settings.BandMode.1
        @Override // android.widget.AdapterView.OnItemClickListener
        public void onItemClick(AdapterView adapterView, View view, int i, long j) {
            BandMode.this.getWindow().setFeatureInt(5, -1);
            BandMode.this.mTargetBand = (BandListItem) adapterView.getAdapter().getItem(i);
            BandMode.this.mPhone.setBandMode(BandMode.this.mTargetBand.getBand(), BandMode.this.mHandler.obtainMessage(200));
        }
    };
    private Handler mHandler = new Handler() { // from class: com.android.settings.BandMode.2
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 100) {
                BandMode.this.bandListLoaded((AsyncResult) message.obj);
            } else if (i == 200) {
                AsyncResult asyncResult = (AsyncResult) message.obj;
                BandMode.this.getWindow().setFeatureInt(5, -2);
                if (!BandMode.this.isFinishing()) {
                    BandMode.this.displayBandSelectionResult(asyncResult.exception);
                }
            }
        }
    };

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(5);
        setContentView(R.layout.band_mode);
        setTitle(getString(R.string.band_mode_title));
        getWindow().setLayout(-1, -2);
        this.mPhone = PhoneFactory.getDefaultPhone();
        this.mBandList = (ListView) findViewById(R.id.band);
        this.mBandListAdapter = new ArrayAdapter(this, 17367043);
        this.mBandList.setAdapter((ListAdapter) this.mBandListAdapter);
        this.mBandList.setOnItemClickListener(this.mBandSelectionHandler);
        loadBandList();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class BandListItem {
        private int mBandMode;

        public BandListItem(int i) {
            this.mBandMode = 0;
            this.mBandMode = i;
        }

        public int getBand() {
            return this.mBandMode;
        }

        public String toString() {
            if (this.mBandMode >= BandMode.BAND_NAMES.length) {
                return "Band mode " + this.mBandMode;
            }
            return BandMode.BAND_NAMES[this.mBandMode];
        }
    }

    private void loadBandList() {
        this.mProgressPanel = new AlertDialog.Builder(this).setMessage(getString(R.string.band_mode_loading)).show();
        this.mPhone.queryAvailableBandMode(this.mHandler.obtainMessage(100));
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Removed duplicated region for block: B:23:0x004b A[LOOP:1: B:23:0x004b->B:25:0x004f, LOOP_START, PHI: r2 
      PHI: (r2v1 int) = (r2v0 int), (r2v2 int) binds: [B:22:0x0049, B:25:0x004f] A[DONT_GENERATE, DONT_INLINE]] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void bandListLoaded(AsyncResult asyncResult) {
        if (this.mProgressPanel != null) {
            this.mProgressPanel.dismiss();
        }
        clearList();
        boolean z = true;
        if (asyncResult.result != null) {
            int[] iArr = (int[]) asyncResult.result;
            if (iArr.length == 0) {
                Log.wtf("phone", "No Supported Band Modes");
                return;
            }
            int i = iArr[0];
            if (i > 0) {
                this.mBandListAdapter.add(new BandListItem(0));
                for (int i2 = 1; i2 <= i; i2++) {
                    if (iArr[i2] != 0) {
                        this.mBandListAdapter.add(new BandListItem(iArr[i2]));
                    }
                }
                if (!z) {
                    for (int i3 = 0; i3 < 19; i3++) {
                        this.mBandListAdapter.add(new BandListItem(i3));
                    }
                }
                this.mBandList.requestFocus();
            }
        }
        z = false;
        if (!z) {
        }
        this.mBandList.requestFocus();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void displayBandSelectionResult(Throwable th) {
        String str;
        String str2 = getString(R.string.band_mode_set) + " [" + this.mTargetBand.toString() + "] ";
        if (th != null) {
            str = str2 + getString(R.string.band_mode_failed);
        } else {
            str = str2 + getString(R.string.band_mode_succeeded);
        }
        this.mProgressPanel = new AlertDialog.Builder(this).setMessage(str).setPositiveButton(17039370, (DialogInterface.OnClickListener) null).show();
    }

    private void clearList() {
        while (this.mBandListAdapter.getCount() > 0) {
            this.mBandListAdapter.remove(this.mBandListAdapter.getItem(0));
        }
    }
}
