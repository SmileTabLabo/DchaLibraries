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
        public void onItemClick(AdapterView parent, View v, int position, long id) {
            BandMode.this.getWindow().setFeatureInt(5, -1);
            BandMode.this.mTargetBand = (BandListItem) parent.getAdapter().getItem(position);
            Message msg = BandMode.this.mHandler.obtainMessage(200);
            BandMode.this.mPhone.setBandMode(BandMode.this.mTargetBand.getBand(), msg);
        }
    };
    private Handler mHandler = new Handler() { // from class: com.android.settings.BandMode.2
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    AsyncResult ar = (AsyncResult) msg.obj;
                    BandMode.this.bandListLoaded(ar);
                    return;
                case 200:
                    AsyncResult ar2 = (AsyncResult) msg.obj;
                    BandMode.this.getWindow().setFeatureInt(5, -2);
                    if (BandMode.this.isFinishing()) {
                        return;
                    }
                    BandMode.this.displayBandSelectionResult(ar2.exception);
                    return;
                default:
                    return;
            }
        }
    };

    @Override // android.app.Activity
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
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

        public BandListItem(int bm) {
            this.mBandMode = 0;
            this.mBandMode = bm;
        }

        public int getBand() {
            return this.mBandMode;
        }

        public String toString() {
            return this.mBandMode >= BandMode.BAND_NAMES.length ? "Band mode " + this.mBandMode : BandMode.BAND_NAMES[this.mBandMode];
        }
    }

    private void loadBandList() {
        String str = getString(R.string.band_mode_loading);
        this.mProgressPanel = new AlertDialog.Builder(this).setMessage(str).show();
        Message msg = this.mHandler.obtainMessage(100);
        this.mPhone.queryAvailableBandMode(msg);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void bandListLoaded(AsyncResult result) {
        if (this.mProgressPanel != null) {
            this.mProgressPanel.dismiss();
        }
        clearList();
        boolean addBandSuccess = false;
        if (result.result != null) {
            int[] bands = (int[]) result.result;
            if (bands.length == 0) {
                Log.wtf("phone", "No Supported Band Modes");
                return;
            }
            int size = bands[0];
            if (size > 0) {
                for (int i = 1; i <= size; i++) {
                    BandListItem item = new BandListItem(bands[i]);
                    this.mBandListAdapter.add(item);
                }
                addBandSuccess = true;
            }
        }
        if (!addBandSuccess) {
            for (int i2 = 0; i2 < 19; i2++) {
                BandListItem item2 = new BandListItem(i2);
                this.mBandListAdapter.add(item2);
            }
        }
        this.mBandList.requestFocus();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void displayBandSelectionResult(Throwable ex) {
        String status = getString(R.string.band_mode_set) + " [" + this.mTargetBand.toString() + "] ";
        this.mProgressPanel = new AlertDialog.Builder(this).setMessage(ex != null ? status + getString(R.string.band_mode_failed) : status + getString(R.string.band_mode_succeeded)).setPositiveButton(17039370, (DialogInterface.OnClickListener) null).show();
    }

    private void clearList() {
        while (this.mBandListAdapter.getCount() > 0) {
            this.mBandListAdapter.remove(this.mBandListAdapter.getItem(0));
        }
    }
}
