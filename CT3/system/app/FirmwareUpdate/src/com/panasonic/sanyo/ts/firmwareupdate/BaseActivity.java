package com.panasonic.sanyo.ts.firmwareupdate;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.StatFs;
import android.provider.Downloads;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.HashSet;
import java.util.Iterator;
/* loaded from: com.zip:com/panasonic/sanyo/ts/firmwareupdate/BaseActivity.class */
public class BaseActivity extends Activity implements Runnable {
    static Thread thread = null;
    private AlertDialog.Builder dlg;
    private ProgressDialog progressDialog;
    private int status = 0;
    private int level = 0;
    private BroadcastReceiver BatteryBroadcastReceiver = null;
    private BroadcastReceiver MediaBroadcastReceiver = null;
    private boolean ReceverRegistered = false;
    protected boolean UpdateCancel = false;
    private boolean ProgressDialogActive = false;
    private boolean UpdateMediaEject = false;
    protected String SDPath = "/mnt/m_external_sd/update.zip";
    private Handler handler = new AnonymousClass1(this);

    /* renamed from: com.panasonic.sanyo.ts.firmwareupdate.BaseActivity$1  reason: invalid class name */
    /* loaded from: com.zip:com/panasonic/sanyo/ts/firmwareupdate/BaseActivity$1.class */
    class AnonymousClass1 extends Handler {
        final BaseActivity this$0;

        AnonymousClass1(BaseActivity baseActivity) {
            this.this$0 = baseActivity;
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            this.this$0.CancelAction(true);
            this.this$0.dlg.setCancelable(false);
            switch (message.what) {
                case 0:
                case 1:
                    this.this$0.dlg.setTitle("アップデートデータの読込みに失敗しました");
                    this.this$0.dlg.setMessage("SDカードが正常に読み込めません。\nSDカードが挿入されているか確認してください。");
                    break;
                case 2:
                case 3:
                case 4:
                    this.this$0.dlg.setTitle("アップデート処理に失敗しました");
                    break;
                case 5:
                    this.this$0.dlg.setTitle("充電してください");
                    this.this$0.dlg.setMessage("ローバッテリーになるとシステムアップデートが失敗する場合があります。\n電源を挿してからもう一度やり直してください。");
                    break;
            }
            this.this$0.dlg.setPositiveButton("OK", new DialogInterface.OnClickListener(this) { // from class: com.panasonic.sanyo.ts.firmwareupdate.BaseActivity.1.1
                final AnonymousClass1 this$1;

                {
                    this.this$1 = this;
                }

                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    this.this$1.this$0.finish();
                }
            });
            this.this$0.dlg.show();
        }
    }

    private void CacheDirFileCheck() {
        File[] listFiles = Environment.getDownloadCacheDirectory().listFiles();
        if (listFiles == null) {
            return;
        }
        HashSet hashSet = new HashSet();
        for (int i = 0; i < listFiles.length; i++) {
            if (!listFiles[i].getName().equals("lost+found") && !listFiles[i].getName().equalsIgnoreCase("recovery")) {
                hashSet.add(listFiles[i].getPath());
            }
        }
        Cursor query = getContentResolver().query(Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI, new String[]{"_data"}, null, null, null);
        if (query != null) {
            if (query.moveToFirst()) {
                do {
                    hashSet.remove(query.getString(0));
                } while (query.moveToNext());
                query.close();
            } else {
                query.close();
            }
        }
        Iterator it = hashSet.iterator();
        while (it.hasNext()) {
            delete(new File((String) it.next()));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void CancelAction(boolean z) {
        Log.v("CancelAction", "start");
        File file = new File("/cache/update.zip");
        if (file.exists()) {
            file.delete();
        }
        File file2 = new File("/cache/recovery/command");
        if (file2.exists()) {
            file2.delete();
        }
        if (z && this.ProgressDialogActive) {
            Log.v("CancelAction", "progressDialog.dismiss");
            this.ProgressDialogActive = false;
            this.progressDialog.dismiss();
        }
    }

    private void UpdateStart() {
        Log.v("UpdateStart", "バッテリーチェック\n");
        if (this.UpdateCancel) {
            CancelAction(true);
        } else if (this.status != 5 && this.status != 2) {
            this.handler.sendEmptyMessage(5);
        } else {
            Log.v("UpdateStart", "容量比較\n");
            if (this.UpdateCancel) {
                CancelAction(true);
                return;
            }
            Log.v("SDPath", "SDPath:" + this.SDPath);
            File file = new File(this.SDPath);
            if (!file.exists()) {
                this.handler.sendEmptyMessage(1);
                return;
            }
            long length = file.length();
            StatFs statFs = new StatFs("/cache");
            if (length > statFs.getAvailableBlocks() * statFs.getBlockSize()) {
                CacheDirFileCheck();
            }
            Log.v("UpdateStart", "ファイルチェック\n");
            if (this.UpdateCancel) {
                CancelAction(true);
                return;
            }
            CancelAction(false);
            try {
                Log.v("UpdateCancel", "update.zipコピー start\n");
                FileChannel channel = new FileInputStream(this.SDPath).getChannel();
                FileChannel channel2 = new FileOutputStream("/cache/update.zip").getChannel();
                long j = 0;
                try {
                    long size = channel.size();
                    do {
                        if (j >= size) {
                            break;
                        }
                        long transferTo = channel.transferTo(j, 1048576L, channel2);
                        if (transferTo != 0) {
                            j += transferTo;
                            Log.v("transferTo", "残り：" + (size - j));
                            if (this.UpdateCancel) {
                                break;
                            }
                        } else {
                            Log.v("UpdateStart", "IOException-throw\n");
                            throw new IOException();
                        }
                    } while (!this.UpdateMediaEject);
                    Log.v("transferTo", "中断されました");
                    channel.close();
                    channel2.close();
                    Log.v("UpdateStart", "/recoveryディレクトリチェック\n");
                    if (this.UpdateCancel) {
                        CancelAction(true);
                        return;
                    }
                    File file2 = new File("/cache/recovery");
                    if (!file2.exists() && !file2.mkdir()) {
                        this.handler.sendEmptyMessage(3);
                        return;
                    }
                    Log.v("UpdateStart", "commandファイルチェック\n");
                    if (this.UpdateCancel) {
                        CancelAction(true);
                        return;
                    }
                    File file3 = new File("/cache/recovery/command");
                    if (!file3.exists()) {
                        try {
                            if (!file3.createNewFile()) {
                                this.handler.sendEmptyMessage(4);
                                return;
                            }
                            FileWriter fileWriter = new FileWriter(file3);
                            fileWriter.write("boot-recovery\n");
                            fileWriter.write("--update_package=/cache/update.zip\n");
                            fileWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            this.handler.sendEmptyMessage(2);
                            return;
                        }
                    }
                    Log.v("UpdateStart", "リブートチェック\n");
                    if (this.UpdateCancel) {
                        CancelAction(true);
                    } else if (this.UpdateMediaEject) {
                        this.UpdateMediaEject = false;
                        this.handler.sendEmptyMessage(2);
                    } else {
                        ((PowerManager) getSystemService("power")).reboot("recovery-update");
                        if (this.ProgressDialogActive) {
                            Log.v("UpdateStart", "progressDialog.dismiss");
                            this.ProgressDialogActive = false;
                            this.progressDialog.dismiss();
                        }
                    }
                } catch (IOException e2) {
                    channel.close();
                    channel2.close();
                    Log.v("UpdateStart", "IOException-transfer\n");
                    e2.printStackTrace();
                    this.handler.sendEmptyMessage(2);
                }
            } catch (FileNotFoundException e3) {
                Log.v("UpdateStart", "FileNotFoundException\n");
                e3.printStackTrace();
                this.handler.sendEmptyMessage(1);
            } catch (IOException e4) {
                Log.v("UpdateStart", "IOException\n");
                e4.printStackTrace();
                this.handler.sendEmptyMessage(2);
            }
        }
    }

    private static void delete(File file) {
        File[] listFiles;
        if (file.isFile()) {
            file.delete();
        }
        if (!file.isDirectory() || (listFiles = file.listFiles()) == null) {
            return;
        }
        for (File file2 : listFiles) {
            delete(file2);
        }
        file.delete();
    }

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().addFlags(128);
        this.dlg = new AlertDialog.Builder(this);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onPause() {
        Log.v("onPause", "onPause");
        super.onPause();
        this.UpdateCancel = true;
        if (this.ReceverRegistered) {
            this.ReceverRegistered = false;
            unregisterReceiver(this.BatteryBroadcastReceiver);
            unregisterReceiver(this.MediaBroadcastReceiver);
        }
        if (this.ProgressDialogActive) {
            Log.v("onPause", "progressDialog.dismiss");
            this.ProgressDialogActive = false;
            this.progressDialog.dismiss();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onResume() {
        super.onResume();
        this.BatteryBroadcastReceiver = new BroadcastReceiver(this) { // from class: com.panasonic.sanyo.ts.firmwareupdate.BaseActivity.2
            final BaseActivity this$0;

            {
                this.this$0 = this;
            }

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.intent.action.BATTERY_CHANGED")) {
                    this.this$0.status = intent.getIntExtra("status", 0);
                    this.this$0.level = intent.getIntExtra("level", 0);
                    Log.v("status", "status:" + this.this$0.status);
                    Log.v("level", "level:" + this.this$0.level);
                }
            }
        };
        this.MediaBroadcastReceiver = new BroadcastReceiver(this) { // from class: com.panasonic.sanyo.ts.firmwareupdate.BaseActivity.3
            final BaseActivity this$0;

            {
                this.this$0 = this;
            }

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.intent.action.MEDIA_EJECT")) {
                    Log.v("MediaBroadcastReceiver", "SDカードが抜かれました\n");
                    if (BaseActivity.thread != null) {
                        this.this$0.UpdateMediaEject = true;
                    }
                }
            }
        };
        if (this.ReceverRegistered) {
            return;
        }
        this.ReceverRegistered = true;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.BATTERY_CHANGED");
        registerReceiver(this.BatteryBroadcastReceiver, intentFilter);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("android.intent.action.MEDIA_EJECT");
        intentFilter2.addDataScheme("file");
        registerReceiver(this.MediaBroadcastReceiver, intentFilter2);
    }

    @Override // java.lang.Runnable
    public void run() {
        Log.d("runProcess", "run");
        try {
            Thread.sleep(500L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        UpdateStart();
        thread = null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void startprogress() {
        this.ProgressDialogActive = true;
        this.progressDialog = new ProgressDialog(this);
        this.progressDialog.setProgressStyle(0);
        this.progressDialog.setTitle("処理中");
        this.progressDialog.setMessage("お待ちください･･･");
        this.progressDialog.setCancelable(false);
        this.progressDialog.show();
        Log.v("thread", "thread" + thread);
        thread = new Thread(this);
        thread.start();
    }
}
