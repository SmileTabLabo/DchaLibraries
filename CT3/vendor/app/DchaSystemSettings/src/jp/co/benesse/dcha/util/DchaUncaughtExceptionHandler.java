package jp.co.benesse.dcha.util;

import android.content.Context;
import android.os.Process;
import java.lang.Thread;
/* loaded from: s.zip:jp/co/benesse/dcha/util/DchaUncaughtExceptionHandler.class */
public class DchaUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    private Context mAppContext;

    public DchaUncaughtExceptionHandler(Context context) {
        this.mAppContext = context;
    }

    @Override // java.lang.Thread.UncaughtExceptionHandler
    public void uncaughtException(Thread thread, Throwable th) {
        try {
            Logger.e(this.mAppContext, "uncaughtException", th);
            Process.killProcess(Process.myPid());
        } catch (Throwable th2) {
            Process.killProcess(Process.myPid());
            throw th2;
        }
    }
}
