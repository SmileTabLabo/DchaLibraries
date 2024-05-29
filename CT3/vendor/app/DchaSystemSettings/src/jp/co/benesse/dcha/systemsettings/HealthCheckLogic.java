package jp.co.benesse.dcha.systemsettings;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.provider.Settings;
import android.text.TextUtils;
import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import jp.co.benesse.dcha.util.FileUtils;
import jp.co.benesse.dcha.util.Logger;
/* loaded from: s.zip:jp/co/benesse/dcha/systemsettings/HealthCheckLogic.class */
public class HealthCheckLogic {
    private int getNetmask(Context context, int i) {
        int i2;
        Logger.d("HealthCheckLogic", "getNetmask 0001");
        int i3 = 0;
        try {
            byte[] array = ByteBuffer.allocate(4).putInt(i).array();
            int length = array.length - 1;
            for (int i4 = 0; length > i4; i4++) {
                byte b = array[length];
                array[length] = array[i4];
                array[i4] = b;
                length--;
            }
            Iterator<T> it = NetworkInterface.getByInetAddress(InetAddress.getByAddress(array)).getInterfaceAddresses().iterator();
            int i5 = 0;
            while (true) {
                i3 = i5;
                i2 = i5;
                if (!it.hasNext()) {
                    break;
                }
                int i6 = i5;
                InterfaceAddress interfaceAddress = (InterfaceAddress) it.next();
                int i7 = i5;
                Logger.d("HealthCheckLogic", "getNetmask 0002");
                int i8 = i5;
                short networkPrefixLength = interfaceAddress.getNetworkPrefixLength();
                if (networkPrefixLength >= 0 && networkPrefixLength <= 32) {
                    Logger.d("HealthCheckLogic", "getNetmask 0003");
                    int i9 = i5;
                    i5 = Integer.reverseBytes((-1) << (32 - networkPrefixLength));
                }
            }
        } catch (Exception e) {
            Logger.e("HealthCheckLogic", "getNetmask 0004");
            i2 = i3;
        }
        Logger.e("HealthCheckLogic", "getNetmask 0005");
        return i2;
    }

    /* JADX WARN: Code restructure failed: missing block: B:61:0x0289, code lost:
        r12 = java.lang.System.currentTimeMillis() - r0;
     */
    /* JADX WARN: Code restructure failed: missing block: B:62:0x0293, code lost:
        r14 = r35;
     */
    /* JADX WARN: Code restructure failed: missing block: B:63:0x02ac, code lost:
        jp.co.benesse.dcha.util.FileUtils.close(r31);
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void checkDownloadSpeed(Context context, HealthChkMngDto healthChkMngDto, HealthCheckDto healthCheckDto) {
        BufferedInputStream bufferedInputStream;
        InputStream inputStream;
        Logger.d("HealthCheckLogic", "checkDownloadSpeed 0001");
        long j = 0;
        long j2 = 0;
        String[] urlList = getUrlList(healthChkMngDto);
        long j3 = 0;
        long j4 = 0;
        if (urlList != null) {
            Logger.d("HealthCheckLogic", "checkDownloadSpeed 0002");
            long currentTimeMillis = System.currentTimeMillis();
            String substring = healthChkMngDto.url.substring(0, healthChkMngDto.url.lastIndexOf("/") + 1);
            Closeable closeable = null;
            Closeable closeable2 = null;
            Closeable closeable3 = null;
            Closeable closeable4 = null;
            byte[] bArr = new byte[1024];
            int parseInt = Integer.parseInt(urlList[0]) * 1000;
            try {
                try {
                    int length = urlList.length;
                    int i = 1;
                    loop0: while (true) {
                        if (i >= length) {
                            bufferedInputStream = null;
                            inputStream = null;
                            break;
                        }
                        closeable = null;
                        try {
                            URLConnection openConnection = new URL(substring + urlList[i]).openConnection();
                            openConnection.setConnectTimeout(parseInt);
                            openConnection.setReadTimeout(parseInt);
                            openConnection.connect();
                            inputStream = openConnection.getInputStream();
                            bufferedInputStream = new BufferedInputStream(inputStream, 1024);
                            long j5 = j;
                            long currentTimeMillis2 = System.currentTimeMillis();
                            long j6 = 0;
                            while (true) {
                                int read = bufferedInputStream.read(bArr);
                                if (read == -1) {
                                    break;
                                }
                                j6 += read;
                                if (!healthCheckDto.isCancel()) {
                                    if (parseInt < ((int) (System.currentTimeMillis() - currentTimeMillis))) {
                                        long j7 = j;
                                        Logger.d("HealthCheckLogic", "checkDownloadSpeed 0004");
                                        break loop0;
                                    }
                                } else {
                                    long j8 = j;
                                    Logger.d("HealthCheckLogic", "checkDownloadSpeed 0003");
                                    break loop0;
                                }
                            }
                        } catch (Exception e) {
                            e = e;
                            closeable4 = null;
                            closeable2 = null;
                            Logger.d("HealthCheckLogic", "checkDownloadSpeed 0005", e);
                            FileUtils.close(closeable2);
                            FileUtils.close(closeable4);
                            j4 = j2;
                            j3 = j;
                            getDSpeedResult(context, healthCheckDto, j3, j4);
                            Logger.d("HealthCheckLogic", "checkDownloadSpeed 0006");
                        } catch (Throwable th) {
                            th = th;
                            closeable3 = null;
                            FileUtils.close(closeable);
                            FileUtils.close(closeable3);
                            throw th;
                        }
                        FileUtils.close(bufferedInputStream);
                        i++;
                    }
                    FileUtils.close(inputStream);
                    FileUtils.close(bufferedInputStream);
                } catch (Exception e2) {
                    e = e2;
                    j2 = 0;
                    j = 0;
                }
                j4 = j2;
                j3 = j;
            } catch (Throwable th2) {
                th = th2;
            }
        }
        getDSpeedResult(context, healthCheckDto, j3, j4);
        Logger.d("HealthCheckLogic", "checkDownloadSpeed 0006");
    }

    public void checkIpAddress(Context context, DhcpInfo dhcpInfo, HealthCheckDto healthCheckDto) {
        Logger.d("HealthCheckLogic", "checkIpAddress 0001");
        if (dhcpInfo == null || dhcpInfo.ipAddress == 0) {
            Logger.d("HealthCheckLogic", "checkIpAddress 0003");
            healthCheckDto.myIpAddress = context.getString(2131230961);
            healthCheckDto.isCheckedIpAddress = 2131230961;
        } else {
            Logger.d("HealthCheckLogic", "checkIpAddress 0002");
            healthCheckDto.myIpAddress = parseAddress(dhcpInfo.ipAddress);
            healthCheckDto.mySubnetMask = parseAddress(getNetmask(context, dhcpInfo.ipAddress));
            healthCheckDto.myDefaultGateway = parseAddress(dhcpInfo.gateway);
            healthCheckDto.myDns1 = parseAddress(dhcpInfo.dns1);
            healthCheckDto.myDns2 = parseAddress(dhcpInfo.dns2);
            healthCheckDto.isCheckedIpAddress = 2131230960;
        }
        Logger.d("HealthCheckLogic", "checkIpAddress 0004");
    }

    public void checkNetConnection(HealthChkMngDto healthChkMngDto, HealthCheckDto healthCheckDto) {
        Logger.d("HealthCheckLogic", "checkNetConnection 0001");
        if (healthChkMngDto != null) {
            Logger.d("HealthCheckLogic", "checkNetConnection 0002");
            ExecuteHttpTask executeHttpTask = getExecuteHttpTask(healthChkMngDto.url, healthChkMngDto.timeout);
            executeHttpTask.execute();
            if (executeHttpTask.getResponse() != null) {
                Logger.d("HealthCheckLogic", "checkNetConnection 0003");
                healthCheckDto.isCheckedNetConnection = 2131230960;
            } else {
                Logger.d("HealthCheckLogic", "checkNetConnection 0004");
                healthCheckDto.isCheckedNetConnection = 2131230961;
            }
        } else {
            Logger.d("HealthCheckLogic", "checkNetConnection 0005");
            healthCheckDto.isCheckedNetConnection = 2131230961;
        }
        Logger.d("HealthCheckLogic", "checkNetConnection 0006");
    }

    public void checkSsid(Context context, List<WifiConfiguration> list, HealthCheckDto healthCheckDto) {
        String str;
        Logger.d("HealthCheckLogic", "checkSsid 0001");
        Object obj = null;
        if (list != null) {
            Logger.d("HealthCheckLogic", "checkSsid 0002");
            Iterator<T> it = list.iterator();
            String str2 = null;
            while (true) {
                str = str2;
                if (!it.hasNext()) {
                    break;
                }
                WifiConfiguration wifiConfiguration = (WifiConfiguration) it.next();
                if (wifiConfiguration.status == 0) {
                    Logger.d("HealthCheckLogic", "checkSsid 0003");
                    str = wifiConfiguration.SSID;
                    break;
                }
                str2 = wifiConfiguration.SSID;
            }
            obj = parseSsid(str);
        }
        if (TextUtils.isEmpty(obj) || context.getString(2131230963).equals(obj)) {
            Logger.d("HealthCheckLogic", "checkSsid 0004");
            healthCheckDto.isCheckedSsid = 2131230961;
            healthCheckDto.mySsid = context.getString(2131230961);
        } else {
            Logger.d("HealthCheckLogic", "checkSsid 0005");
            healthCheckDto.mySsid = obj;
            healthCheckDto.isCheckedSsid = 2131230960;
        }
        Logger.d("HealthCheckLogic", "checkSsid 0006");
    }

    public void checkWifi(WifiInfo wifiInfo, HealthCheckDto healthCheckDto) {
        Logger.d("HealthCheckLogic", "checkWifi 0001");
        if (wifiInfo == null || !SupplicantState.COMPLETED.equals(wifiInfo.getSupplicantState())) {
            Logger.d("HealthCheckLogic", "checkWifi 0003");
            healthCheckDto.isCheckedWifi = 2131230961;
        } else {
            Logger.d("HealthCheckLogic", "checkWifi 0002");
            healthCheckDto.isCheckedWifi = 2131230960;
        }
        Logger.d("HealthCheckLogic", "checkWifi 0004");
    }

    public void getDSpeedResult(Context context, HealthCheckDto healthCheckDto, long j, long j2) {
        Logger.d("HealthCheckLogic", "getDSpeedResult 0001");
        long j3 = j;
        if (j == 0) {
            Logger.d("HealthCheckLogic", "getDSpeedResult 0002");
            j3 = 1;
        }
        long j4 = (8 * j2) / j3;
        if (j4 < Integer.parseInt(context.getString(2131230964))) {
            Logger.d("HealthCheckLogic", "getDSpeedResult 0003");
            healthCheckDto.myDownloadSpeed = 2131230966;
            healthCheckDto.myDSpeedImage = 2130837565;
        } else if (j4 < Integer.parseInt(context.getString(2131230965))) {
            Logger.d("HealthCheckLogic", "getDSpeedResult 0004");
            healthCheckDto.myDownloadSpeed = 2131230967;
            healthCheckDto.myDSpeedImage = 2130837566;
        } else {
            Logger.d("HealthCheckLogic", "getDSpeedResult 0005");
            healthCheckDto.myDownloadSpeed = 2131230968;
            healthCheckDto.myDSpeedImage = 2130837564;
        }
        healthCheckDto.isCheckedDSpeed = 2131230960;
        Logger.d("HealthCheckLogic", "getDSpeedResult 0006");
    }

    public ExecuteHttpTask getExecuteHttpTask(String str, int i) {
        Logger.d("HealthCheckLogic", "getExecuteHttpTask 0001");
        return new ExecuteHttpTask(str, i);
    }

    public void getMacAddress(Context context, WifiInfo wifiInfo, HealthCheckDto healthCheckDto) {
        Logger.d("HealthCheckLogic", "getMacAddress 0001");
        healthCheckDto.myMacaddress = "";
        try {
            healthCheckDto.myMacaddress = Settings.System.getString(context.getContentResolver(), "bc:mac_address");
        } catch (Exception e) {
            Logger.d("HealthCheckLogic", "getMacAddress 0002", e);
        }
        if (wifiInfo != null && TextUtils.isEmpty(healthCheckDto.myMacaddress)) {
            Logger.d("HealthCheckLogic", "getMacAddress 0003");
            healthCheckDto.myMacaddress = wifiInfo.getMacAddress();
        }
        Logger.d("HealthCheckLogic", "getMacAddress 0004");
    }

    public String[] getUrlList(HealthChkMngDto healthChkMngDto) {
        Logger.d("HealthCheckLogic", "getUrlList 0001");
        String[] strArr = null;
        if (healthChkMngDto != null) {
            Logger.d("HealthCheckLogic", "getUrlList 0002");
            ExecuteHttpTask executeHttpTask = getExecuteHttpTask(healthChkMngDto.url, healthChkMngDto.timeout);
            executeHttpTask.execute();
            HttpResponse response = executeHttpTask.getResponse();
            strArr = null;
            if (response != null) {
                Logger.d("HealthCheckLogic", "getUrlList 0003");
                try {
                    String[] split = response.getEntity().split("\n");
                    Integer.parseInt(split[0]);
                    strArr = split;
                    if (split.length < 2) {
                        Logger.d("HealthCheckLogic", "getUrlList 0004");
                        strArr = null;
                    }
                } catch (Exception e) {
                    Logger.d("HealthCheckLogic", "getUrlList 0005", e);
                    strArr = null;
                }
            }
        }
        Logger.d("HealthCheckLogic", "getUrlList 0006");
        return strArr;
    }

    public String parseAddress(int i) {
        Logger.d("HealthCheckLogic", "parseAddress 0001");
        String str = "";
        if (i != 0) {
            Logger.d("HealthCheckLogic", "parseAddress 0002");
            str = ((i >> 0) & 255) + "." + ((i >> 8) & 255) + "." + ((i >> 16) & 255) + "." + ((i >> 24) & 255);
        }
        Logger.d("HealthCheckLogic", "parseAddress 0003");
        return str;
    }

    public String parseSsid(String str) {
        Logger.d("HealthCheckLogic", "parseSsid 0001");
        if (str != null) {
            int length = str.length();
            if (str.startsWith("0x")) {
                Logger.d("HealthCheckLogic", "parseSsid 0001");
                return str.replaceFirst("0x", "");
            } else if (length > 1 && str.charAt(0) == '\"' && str.charAt(length - 1) == '\"') {
                Logger.d("HealthCheckLogic", "parseSsid 0002");
                return str.substring(1, length - 1);
            }
        }
        Logger.d("HealthCheckLogic", "parseSsid 0003");
        return str;
    }
}
