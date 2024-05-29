package com.android.quicksearchbox;

import android.app.Application;
/* loaded from: a.zip:com/android/quicksearchbox/QsbApplicationWrapper.class */
public class QsbApplicationWrapper extends Application {
    private QsbApplication mApp;

    protected QsbApplication createQsbApplication() {
        return new QsbApplication(this);
    }

    public QsbApplication getApp() {
        QsbApplication qsbApplication;
        synchronized (this) {
            if (this.mApp == null) {
                this.mApp = createQsbApplication();
            }
            qsbApplication = this.mApp;
        }
        return qsbApplication;
    }

    @Override // android.app.Application
    public void onTerminate() {
        synchronized (this) {
            if (this.mApp != null) {
                this.mApp.close();
            }
        }
        super.onTerminate();
    }
}
