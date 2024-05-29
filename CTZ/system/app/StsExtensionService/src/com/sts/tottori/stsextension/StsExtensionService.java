package com.sts.tottori.stsextension;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.IStsExtensionService;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;
import com.sts.tottori.stsextension.StsExtensionService;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
/* loaded from: classes.dex */
public class StsExtensionService extends Service {
    private int tp_type;
    static final File PROC_NVT_TP_VERSION = new File("/proc/nvt_fw_version");
    static final File FTS_TP_VERSION = new File("/sys/class/i2c-dev/i2c-3/device/3-0038/fts_fw_version");
    private boolean mIsUpdating = false;
    PowerManager mPowerManager = null;
    IStsExtensionService.Stub mStub = new AnonymousClass1();
    Handler mHandler = new Handler(true);
    Context mContext = this;

    static /* synthetic */ boolean access$000(StsExtensionService stsExtensionService) {
        return stsExtensionService.mIsUpdating;
    }

    public StsExtensionService() {
        this.tp_type = -1;
        if (!PROC_NVT_TP_VERSION.exists()) {
            if (!FTS_TP_VERSION.exists()) {
                Log.e("StsExtensionService", "----- TP:Unkown -----");
                return;
            }
            Log.i("StsExtensionService", "----- TP:FTS -----");
            this.tp_type = 1;
            return;
        }
        Log.i("StsExtensionService", "----- TP:NVT -----");
        this.tp_type = 0;
    }

    PowerManager getPowerManager() {
        if (this.mPowerManager == null) {
            this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        }
        return this.mPowerManager;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.sts.tottori.stsextension.StsExtensionService$1  reason: invalid class name */
    /* loaded from: classes.dex */
    public class AnonymousClass1 extends IStsExtensionService.Stub {
        AnonymousClass1() {
        }

        public boolean updateTouchpanelFw(final String str) {
            long clearCallingIdentity = Binder.clearCallingIdentity();
            try {
                if (!new File(str).isFile()) {
                    Log.e("StsExtensionService", "----- putString() : invalid file[" + str + "] -----");
                    return false;
                } else if (StsExtensionService.this.mIsUpdating) {
                    Log.e("StsExtensionService", "----- FW update : already updating! -----");
                    return false;
                } else {
                    StsExtensionService.this.mIsUpdating = true;
                    Log.e("StsExtensionService", "----- updateTouchpanelFw ----- " + StsExtensionService.this.tp_type);
                    if (StsExtensionService.this.tp_type == 0) {
                        new Thread(new Runnable() { // from class: com.sts.tottori.stsextension.-$$Lambda$StsExtensionService$1$2k56XctykEVEWEJ1N9zz89Tl0kM
                            @Override // java.lang.Runnable
                            public final void run() {
                                StsExtensionService.AnonymousClass1.lambda$updateTouchpanelFw$1(StsExtensionService.AnonymousClass1.this, str);
                            }
                        }).start();
                    } else {
                        String substring = str.substring(str.lastIndexOf("/") + 1);
                        if (substring.length() >= 95) {
                            Log.e("StsExtensionService", "----- filename length(" + substring.length() + ") fail -----");
                            StsExtensionService.this.mIsUpdating = false;
                            return false;
                        } else if (substring.indexOf("FT8205") != 0) {
                            Log.e("StsExtensionService", "----- invalid file name [" + substring + "] -----");
                            StsExtensionService.this.mIsUpdating = false;
                            return false;
                        } else {
                            new Thread(new Runnable() { // from class: com.sts.tottori.stsextension.-$$Lambda$StsExtensionService$1$tupqgzeAP6XLKCQr0ErO3KLVrmQ
                                @Override // java.lang.Runnable
                                public final void run() {
                                    StsExtensionService.AnonymousClass1.lambda$updateTouchpanelFw$3(StsExtensionService.AnonymousClass1.this, str);
                                }
                            }).start();
                        }
                    }
                    return true;
                }
            } finally {
                Binder.restoreCallingIdentity(clearCallingIdentity);
            }
        }

        /*  JADX ERROR: JadxRuntimeException in pass: BlockProcessor
            jadx.core.utils.exceptions.JadxRuntimeException: Found unreachable blocks
            	at jadx.core.dex.visitors.blocks.DominatorTree.sortBlocks(DominatorTree.java:35)
            	at jadx.core.dex.visitors.blocks.DominatorTree.compute(DominatorTree.java:25)
            	at jadx.core.dex.visitors.blocks.BlockProcessor.computeDominators(BlockProcessor.java:202)
            	at jadx.core.dex.visitors.blocks.BlockProcessor.processBlocksTree(BlockProcessor.java:45)
            	at jadx.core.dex.visitors.blocks.BlockProcessor.visit(BlockProcessor.java:39)
            */
        public static /* synthetic */ void lambda$updateTouchpanelFw$1(com.sts.tottori.stsextension.StsExtensionService.AnonymousClass1 r10, java.lang.String r11) {
            /*
                Method dump skipped, instructions count: 450
                To view this dump add '--comments-level debug' option
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sts.tottori.stsextension.StsExtensionService.AnonymousClass1.lambda$updateTouchpanelFw$1(com.sts.tottori.stsextension.StsExtensionService$1, java.lang.String):void");
        }

        public static /* synthetic */ void lambda$updateTouchpanelFw$0(AnonymousClass1 anonymousClass1, int i) {
            SystemProperties.set("nvt.nvt_fw_updating", "0");
            StsExtensionService.this.getPowerManager().setKeepAwake(false);
            StsExtensionService.this.mIsUpdating = false;
            StsExtensionService.this.mContext.sendBroadcastAsUser(new Intent("com.panasonic.sanyo.ts.intent.action.TOUCHPANEL_FIRMWARE_UPDATED").putExtra("result", i), UserHandle.ALL);
        }

        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARN: Removed duplicated region for block: B:44:0x00bd A[Catch: Exception -> 0x00b9, TryCatch #5 {Exception -> 0x00b9, blocks: (B:40:0x00b5, B:44:0x00bd, B:46:0x00c2, B:47:0x00c5), top: B:54:0x00b5 }] */
        /* JADX WARN: Removed duplicated region for block: B:46:0x00c2 A[Catch: Exception -> 0x00b9, TryCatch #5 {Exception -> 0x00b9, blocks: (B:40:0x00b5, B:44:0x00bd, B:46:0x00c2, B:47:0x00c5), top: B:54:0x00b5 }] */
        /* JADX WARN: Removed duplicated region for block: B:54:0x00b5 A[EXC_TOP_SPLITTER, SYNTHETIC] */
        /* JADX WARN: Type inference failed for: r1v0, types: [java.lang.String] */
        /* JADX WARN: Type inference failed for: r1v11, types: [java.io.OutputStream, java.io.FileOutputStream] */
        /* JADX WARN: Type inference failed for: r1v12 */
        /* JADX WARN: Type inference failed for: r1v2, types: [java.io.FileOutputStream] */
        /* JADX WARN: Type inference failed for: r1v5 */
        /* JADX WARN: Type inference failed for: r1v6 */
        /* JADX WARN: Type inference failed for: r1v7 */
        /* JADX WARN: Type inference failed for: r1v8, types: [java.io.FileOutputStream] */
        /* JADX WARN: Type inference failed for: r3v0 */
        /* JADX WARN: Type inference failed for: r3v1, types: [java.io.BufferedWriter] */
        /* JADX WARN: Type inference failed for: r3v2 */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
        */
        public static /* synthetic */ void lambda$updateTouchpanelFw$3(final AnonymousClass1 anonymousClass1, String str) {
            ?? r1;
            BufferedWriter bufferedWriter;
            OutputStreamWriter outputStreamWriter;
            String str2 = null;
            r0 = null;
            r0 = null;
            BufferedWriter bufferedWriter2 = null;
            try {
                try {
                    try {
                        StsExtensionService.this.getPowerManager().setKeepAwake(true);
                        r1 = new FileOutputStream("/sys/devices/platform/soc/1100f000.i2c/i2c-3/3-0038/fts_upgrade_bin");
                    } catch (Throwable th) {
                        th = th;
                        bufferedWriter = str2;
                    }
                } catch (Exception e) {
                    e = e;
                    r1 = 0;
                    outputStreamWriter = null;
                } catch (Throwable th2) {
                    th = th2;
                    r1 = 0;
                    outputStreamWriter = null;
                }
                try {
                    outputStreamWriter = new OutputStreamWriter((OutputStream) r1, "UTF-8");
                    try {
                        bufferedWriter = new BufferedWriter(outputStreamWriter);
                    } catch (Exception e2) {
                        e = e2;
                    }
                    try {
                        Log.e("StsExtensionService", "----- fts_upgrade_bin ----- " + str.substring(str.indexOf("FT8205")));
                        bufferedWriter.write(str.substring(str.indexOf("FT8205")));
                        bufferedWriter.write("\n");
                        bufferedWriter.close();
                        outputStreamWriter.close();
                        r1.close();
                        StsExtensionService.this.mHandler.post(new Runnable() { // from class: com.sts.tottori.stsextension.-$$Lambda$StsExtensionService$1$xX5WvrQaI0uqge2KCuBJqzt_JxI
                            @Override // java.lang.Runnable
                            public final void run() {
                                StsExtensionService.AnonymousClass1.lambda$updateTouchpanelFw$2(StsExtensionService.AnonymousClass1.this);
                            }
                        });
                    } catch (Exception e3) {
                        e = e3;
                        bufferedWriter2 = bufferedWriter;
                        Log.e("StsExtensionService", "----- Exception occurred!!! -----", e);
                        if (bufferedWriter2 != null) {
                            bufferedWriter2.close();
                        }
                        if (outputStreamWriter != null) {
                            outputStreamWriter.close();
                        }
                        if (r1 != 0) {
                            r1.close();
                        }
                        StsExtensionService.this.mHandler.post(new Runnable() { // from class: com.sts.tottori.stsextension.-$$Lambda$StsExtensionService$1$xX5WvrQaI0uqge2KCuBJqzt_JxI
                            @Override // java.lang.Runnable
                            public final void run() {
                                StsExtensionService.AnonymousClass1.lambda$updateTouchpanelFw$2(StsExtensionService.AnonymousClass1.this);
                            }
                        });
                    } catch (Throwable th3) {
                        th = th3;
                        if (bufferedWriter != null) {
                            try {
                                bufferedWriter.close();
                            } catch (Exception e4) {
                                Log.e("StsExtensionService", "----- Exception occurred!!! -----", e4);
                                throw th;
                            }
                        }
                        if (outputStreamWriter != null) {
                            outputStreamWriter.close();
                        }
                        if (r1 != 0) {
                            r1.close();
                        }
                        StsExtensionService.this.mHandler.post(new Runnable() { // from class: com.sts.tottori.stsextension.-$$Lambda$StsExtensionService$1$xX5WvrQaI0uqge2KCuBJqzt_JxI
                            @Override // java.lang.Runnable
                            public final void run() {
                                StsExtensionService.AnonymousClass1.lambda$updateTouchpanelFw$2(StsExtensionService.AnonymousClass1.this);
                            }
                        });
                        throw th;
                    }
                } catch (Exception e5) {
                    e = e5;
                    outputStreamWriter = null;
                } catch (Throwable th4) {
                    th = th4;
                    outputStreamWriter = null;
                    r1 = r1;
                    bufferedWriter = outputStreamWriter;
                    if (bufferedWriter != null) {
                    }
                    if (outputStreamWriter != null) {
                    }
                    if (r1 != 0) {
                    }
                    StsExtensionService.this.mHandler.post(new Runnable() { // from class: com.sts.tottori.stsextension.-$$Lambda$StsExtensionService$1$xX5WvrQaI0uqge2KCuBJqzt_JxI
                        @Override // java.lang.Runnable
                        public final void run() {
                            StsExtensionService.AnonymousClass1.lambda$updateTouchpanelFw$2(StsExtensionService.AnonymousClass1.this);
                        }
                    });
                    throw th;
                }
            } catch (Exception e6) {
                str2 = "StsExtensionService";
                r1 = "----- Exception occurred!!! -----";
                Log.e("StsExtensionService", "----- Exception occurred!!! -----", e6);
            }
        }

        public static /* synthetic */ void lambda$updateTouchpanelFw$2(AnonymousClass1 anonymousClass1) {
            StsExtensionService.this.getPowerManager().setKeepAwake(false);
            StsExtensionService.this.mIsUpdating = false;
            StsExtensionService.this.mContext.sendBroadcastAsUser(new Intent("com.panasonic.sanyo.ts.intent.action.TOUCHPANEL_FIRMWARE_UPDATED").putExtra("result", 0), UserHandle.ALL);
        }

        /*  JADX ERROR: JadxRuntimeException in pass: BlockProcessor
            jadx.core.utils.exceptions.JadxRuntimeException: Found unreachable blocks
            	at jadx.core.dex.visitors.blocks.DominatorTree.sortBlocks(DominatorTree.java:35)
            	at jadx.core.dex.visitors.blocks.DominatorTree.compute(DominatorTree.java:25)
            	at jadx.core.dex.visitors.blocks.BlockProcessor.computeDominators(BlockProcessor.java:202)
            	at jadx.core.dex.visitors.blocks.BlockProcessor.processBlocksTree(BlockProcessor.java:45)
            	at jadx.core.dex.visitors.blocks.BlockProcessor.visit(BlockProcessor.java:39)
            */
        public java.lang.String getTouchpanelVersion() {
            /*
                r6 = this;
                com.sts.tottori.stsextension.StsExtensionService r0 = com.sts.tottori.stsextension.StsExtensionService.this
                boolean r0 = com.sts.tottori.stsextension.StsExtensionService.access$000(r0)
                r1 = 0
                if (r0 == 0) goto La
                return r1
            La:
                java.io.File r0 = com.sts.tottori.stsextension.StsExtensionService.PROC_NVT_TP_VERSION
                boolean r0 = r0.exists()
                if (r0 != 0) goto L4f
                java.io.File r0 = com.sts.tottori.stsextension.StsExtensionService.FTS_TP_VERSION
                boolean r0 = r0.exists()
                if (r0 != 0) goto L1d
                java.lang.String r0 = ""
                return r0
            L1d:
                java.io.FileReader r0 = new java.io.FileReader     // Catch: java.lang.Throwable -> L4b
                java.io.File r2 = com.sts.tottori.stsextension.StsExtensionService.FTS_TP_VERSION     // Catch: java.lang.Throwable -> L4b
                r0.<init>(r2)     // Catch: java.lang.Throwable -> L4b
                java.io.BufferedReader r2 = new java.io.BufferedReader     // Catch: java.lang.Throwable -> L45
                r2.<init>(r0)     // Catch: java.lang.Throwable -> L45
                java.lang.String r3 = r2.readLine()     // Catch: java.lang.Throwable -> L39
                $closeResource(r1, r2)     // Catch: java.lang.Throwable -> L45
                $closeResource(r1, r0)     // Catch: java.lang.Throwable -> L4b
                return r3
            L36:
                r3 = move-exception
                r4 = r1
                goto L3f
            L39:
                r3 = move-exception
                throw r3     // Catch: java.lang.Throwable -> L3b
            L3b:
                r4 = move-exception
                r5 = r4
                r4 = r3
                r3 = r5
            L3f:
                $closeResource(r4, r2)     // Catch: java.lang.Throwable -> L45
                throw r3     // Catch: java.lang.Throwable -> L45
            L43:
                r2 = move-exception
                goto L47
            L45:
                r1 = move-exception
                throw r1     // Catch: java.lang.Throwable -> L43
            L47:
                $closeResource(r1, r0)     // Catch: java.lang.Throwable -> L4b
                throw r2     // Catch: java.lang.Throwable -> L4b
            L4b:
                r0 = move-exception
                java.lang.String r0 = ""
                return r0
            L4f:
                java.io.FileReader r0 = new java.io.FileReader     // Catch: java.lang.Throwable -> Lb6
                java.io.File r2 = com.sts.tottori.stsextension.StsExtensionService.PROC_NVT_TP_VERSION     // Catch: java.lang.Throwable -> Lb6
                r0.<init>(r2)     // Catch: java.lang.Throwable -> Lb6
                java.io.BufferedReader r2 = new java.io.BufferedReader     // Catch: java.lang.Throwable -> Lb0
                r2.<init>(r0)     // Catch: java.lang.Throwable -> Lb0
                java.lang.String r3 = r2.readLine()     // Catch: java.lang.Throwable -> La4
                $closeResource(r1, r2)     // Catch: java.lang.Throwable -> Lb0
                $closeResource(r1, r0)     // Catch: java.lang.Throwable -> Lb6
                java.lang.String r0 = "fw_ver="
                int r0 = r3.indexOf(r0)
                java.lang.String r1 = ","
                int r1 = r3.indexOf(r1)
                r2 = -1
                if (r0 == r2) goto L9e
                if (r1 != r2) goto L79
                goto L9e
            L79:
                java.lang.StringBuilder r2 = new java.lang.StringBuilder
                r2.<init>()
                java.lang.String r4 = "0x"
                r2.append(r4)
                java.lang.String r4 = "fw_ver="
                int r4 = r4.length()
                int r0 = r0 + r4
                java.lang.String r0 = r3.substring(r0, r1)
                int r0 = java.lang.Integer.parseInt(r0)
                java.lang.String r0 = java.lang.Integer.toHexString(r0)
                r2.append(r0)
                java.lang.String r0 = r2.toString()
                return r0
            L9e:
                java.lang.String r0 = ""
                return r0
            La1:
                r3 = move-exception
                r4 = r1
                goto Laa
            La4:
                r3 = move-exception
                throw r3     // Catch: java.lang.Throwable -> La6
            La6:
                r4 = move-exception
                r5 = r4
                r4 = r3
                r3 = r5
            Laa:
                $closeResource(r4, r2)     // Catch: java.lang.Throwable -> Lb0
                throw r3     // Catch: java.lang.Throwable -> Lb0
            Lae:
                r2 = move-exception
                goto Lb2
            Lb0:
                r1 = move-exception
                throw r1     // Catch: java.lang.Throwable -> Lae
            Lb2:
                $closeResource(r1, r0)     // Catch: java.lang.Throwable -> Lb6
                throw r2     // Catch: java.lang.Throwable -> Lb6
            Lb6:
                r0 = move-exception
                java.lang.String r0 = ""
                return r0
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sts.tottori.stsextension.StsExtensionService.AnonymousClass1.getTouchpanelVersion():java.lang.String");
        }

        private static /* synthetic */ void $closeResource(Throwable th, AutoCloseable autoCloseable) {
            if (th == null) {
                autoCloseable.close();
                return;
            }
            try {
                autoCloseable.close();
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        }

        public int getPenBattery() {
            return SystemProperties.getInt("persist.sys.nvt.penbattery", 0);
        }
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return this.mStub;
    }
}
