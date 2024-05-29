package jp.co.benesse.dcha.systemsettings;

import java.io.Serializable;
import jp.co.benesse.dcha.util.Logger;
/* loaded from: s.zip:jp/co/benesse/dcha/systemsettings/HealthCheckDto.class */
public class HealthCheckDto implements Serializable {
    private static final long serialVersionUID = 4031444211707627400L;
    public int isHealthChecked = 2131230962;
    private boolean cancelFlag = false;
    public int isCheckedSsid = 2131230962;
    public String myMacaddress = "";
    public String mySsid = "";
    public int isCheckedWifi = 2131230962;
    public int isCheckedIpAddress = 2131230962;
    public String myIpAddress = "";
    public String mySubnetMask = "";
    public String myDefaultGateway = "";
    public String myDns1 = "";
    public String myDns2 = "";
    public int isCheckedNetConnection = 2131230962;
    public int isCheckedDSpeed = 2131230962;
    public int myDownloadSpeed = 2131230966;
    public int myDSpeedImage = 2130837565;

    public void cancel() {
        synchronized (this) {
            Logger.d("HealthCheckDto", "cancel 0001");
            this.cancelFlag = true;
        }
    }

    public boolean isCancel() {
        boolean z;
        synchronized (this) {
            Logger.d("HealthCheckDto", "isCancel 0001");
            z = this.cancelFlag;
        }
        return z;
    }
}
