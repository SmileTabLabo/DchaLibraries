package jp.co.benesse.dcha.systemsettings;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.provider.Settings;
import android.text.TextUtils;
import java.io.BufferedInputStream;
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
/* loaded from: classes.dex */
public class HealthCheckLogic {
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

    public void checkSsid(Context context, List<WifiConfiguration> list, HealthCheckDto healthCheckDto) {
        Logger.d("HealthCheckLogic", "checkSsid 0001");
        String str = null;
        if (list != null) {
            Logger.d("HealthCheckLogic", "checkSsid 0002");
            Iterator<WifiConfiguration> it = list.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                WifiConfiguration next = it.next();
                if (next.status == 0) {
                    Logger.d("HealthCheckLogic", "checkSsid 0003");
                    str = next.SSID;
                    break;
                }
                str = next.SSID;
            }
            str = parseSsid(str);
        }
        if (TextUtils.isEmpty(str) || context.getString(R.string.unknown_ssid).equals(str)) {
            Logger.d("HealthCheckLogic", "checkSsid 0004");
            healthCheckDto.isCheckedSsid = R.string.health_check_ng;
            healthCheckDto.mySsid = context.getString(R.string.health_check_ng);
        } else {
            Logger.d("HealthCheckLogic", "checkSsid 0005");
            healthCheckDto.mySsid = str;
            healthCheckDto.isCheckedSsid = R.string.health_check_ok;
        }
        Logger.d("HealthCheckLogic", "checkSsid 0006");
    }

    public void checkWifi(WifiInfo wifiInfo, HealthCheckDto healthCheckDto) {
        Logger.d("HealthCheckLogic", "checkWifi 0001");
        if (wifiInfo != null && SupplicantState.COMPLETED.equals(wifiInfo.getSupplicantState())) {
            Logger.d("HealthCheckLogic", "checkWifi 0002");
            healthCheckDto.isCheckedWifi = R.string.health_check_ok;
        } else {
            Logger.d("HealthCheckLogic", "checkWifi 0003");
            healthCheckDto.isCheckedWifi = R.string.health_check_ng;
        }
        Logger.d("HealthCheckLogic", "checkWifi 0004");
    }

    public void checkIpAddress(Context context, DhcpInfo dhcpInfo, HealthCheckDto healthCheckDto) {
        Logger.d("HealthCheckLogic", "checkIpAddress 0001");
        if (dhcpInfo != null && dhcpInfo.ipAddress != 0) {
            Logger.d("HealthCheckLogic", "checkIpAddress 0002");
            healthCheckDto.myIpAddress = parseAddress(dhcpInfo.ipAddress);
            healthCheckDto.mySubnetMask = parseAddress(getNetmask(context, dhcpInfo.ipAddress));
            healthCheckDto.myDefaultGateway = parseAddress(dhcpInfo.gateway);
            healthCheckDto.myDns1 = parseAddress(dhcpInfo.dns1);
            healthCheckDto.myDns2 = parseAddress(dhcpInfo.dns2);
            healthCheckDto.isCheckedIpAddress = R.string.health_check_ok;
        } else {
            Logger.d("HealthCheckLogic", "checkIpAddress 0003");
            healthCheckDto.myIpAddress = context.getString(R.string.health_check_ng);
            healthCheckDto.isCheckedIpAddress = R.string.health_check_ng;
        }
        Logger.d("HealthCheckLogic", "checkIpAddress 0004");
    }

    private int getNetmask(Context context, int i) {
        int i2;
        Logger.d("HealthCheckLogic", "getNetmask 0001");
        try {
            byte[] array = ByteBuffer.allocate(4).putInt(i).array();
            int length = array.length - 1;
            for (int i3 = 0; length > i3; i3++) {
                byte b = array[length];
                array[length] = array[i3];
                array[i3] = b;
                length--;
            }
            i2 = 0;
            for (InterfaceAddress interfaceAddress : NetworkInterface.getByInetAddress(InetAddress.getByAddress(array)).getInterfaceAddresses()) {
                try {
                    Logger.d("HealthCheckLogic", "getNetmask 0002");
                    short networkPrefixLength = interfaceAddress.getNetworkPrefixLength();
                    if (networkPrefixLength >= 0 && networkPrefixLength <= 32) {
                        Logger.d("HealthCheckLogic", "getNetmask 0003");
                        i2 = Integer.reverseBytes((-1) << (32 - networkPrefixLength));
                    }
                } catch (Exception e) {
                    Logger.e("HealthCheckLogic", "getNetmask 0004");
                    Logger.e("HealthCheckLogic", "getNetmask 0005");
                    return i2;
                }
            }
        } catch (Exception e2) {
            i2 = 0;
        }
        Logger.e("HealthCheckLogic", "getNetmask 0005");
        return i2;
    }

    public void checkNetConnection(HealthChkMngDto healthChkMngDto, HealthCheckDto healthCheckDto) {
        Logger.d("HealthCheckLogic", "checkNetConnection 0001");
        if (healthChkMngDto != null) {
            Logger.d("HealthCheckLogic", "checkNetConnection 0002");
            ExecuteHttpTask executeHttpTask = getExecuteHttpTask(healthChkMngDto.url, healthChkMngDto.timeout);
            executeHttpTask.execute();
            if (executeHttpTask.getResponse() != null) {
                Logger.d("HealthCheckLogic", "checkNetConnection 0003");
                healthCheckDto.isCheckedNetConnection = R.string.health_check_ok;
            } else {
                Logger.d("HealthCheckLogic", "checkNetConnection 0004");
                healthCheckDto.isCheckedNetConnection = R.string.health_check_ng;
            }
        } else {
            Logger.d("HealthCheckLogic", "checkNetConnection 0005");
            healthCheckDto.isCheckedNetConnection = R.string.health_check_ng;
        }
        Logger.d("HealthCheckLogic", "checkNetConnection 0006");
    }

    /* JADX WARN: Code restructure failed: missing block: B:27:0x00c6, code lost:
        r23 = r6;
     */
    /* JADX WARN: Code restructure failed: missing block: B:28:0x00cc, code lost:
        r15 = java.lang.System.currentTimeMillis() - r19;
     */
    /* JADX WARN: Code restructure failed: missing block: B:29:0x00cf, code lost:
        jp.co.benesse.dcha.util.FileUtils.close(r12);
     */
    /* JADX WARN: Code restructure failed: missing block: B:30:0x00d3, code lost:
        r4 = r23;
     */
    /* JADX WARN: Code restructure failed: missing block: B:31:0x00d5, code lost:
        jp.co.benesse.dcha.util.FileUtils.close(r4);
     */
    /* JADX WARN: Code restructure failed: missing block: B:32:0x00d9, code lost:
        r14 = r14 + 1;
        r17 = r21;
        r4 = 1024;
     */
    /* JADX WARN: Code restructure failed: missing block: B:33:0x00e2, code lost:
        r0 = th;
     */
    /* JADX WARN: Code restructure failed: missing block: B:35:0x00e4, code lost:
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:36:0x00e5, code lost:
        r17 = r21;
     */
    /* JADX WARN: Code restructure failed: missing block: B:37:0x00e8, code lost:
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:38:0x00e9, code lost:
        r4 = r23;
        r17 = r21;
     */
    /* JADX WARN: Code restructure failed: missing block: B:49:0x00fc, code lost:
        r12 = null;
     */
    /* JADX WARN: Code restructure failed: missing block: B:55:0x010f, code lost:
        r12 = null;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void checkDownloadSpeed(Context context, HealthChkMngDto healthChkMngDto, HealthCheckDto healthCheckDto) {
        long j;
        long j2;
        BufferedInputStream bufferedInputStream;
        InputStream inputStream;
        long j3;
        long j4;
        int length;
        int i;
        BufferedInputStream bufferedInputStream2;
        BufferedInputStream bufferedInputStream3;
        long j5;
        BufferedInputStream bufferedInputStream4;
        Logger.d("HealthCheckLogic", "checkDownloadSpeed 0001");
        String[] urlList = getUrlList(healthChkMngDto);
        if (urlList != null) {
            Logger.d("HealthCheckLogic", "checkDownloadSpeed 0002");
            long currentTimeMillis = System.currentTimeMillis();
            String substring = healthChkMngDto.url.substring(0, healthChkMngDto.url.lastIndexOf("/") + 1);
            int i2 = 1024;
            byte[] bArr = new byte[1024];
            int parseInt = Integer.parseInt(urlList[0]) * 1000;
            try {
                try {
                    length = urlList.length;
                    i = 1;
                    j3 = 0;
                    j4 = 0;
                } catch (Throwable th) {
                    th = th;
                    bufferedInputStream = null;
                }
            } catch (Exception e) {
                e = e;
                bufferedInputStream = null;
                inputStream = null;
                j3 = 0;
                j4 = 0;
            }
            loop0: while (i < length) {
                try {
                    URLConnection openConnection = new URL(substring + urlList[i]).openConnection();
                    openConnection.setConnectTimeout(parseInt);
                    openConnection.setReadTimeout(parseInt);
                    openConnection.connect();
                    inputStream = openConnection.getInputStream();
                    try {
                        bufferedInputStream3 = new BufferedInputStream(inputStream, i2);
                        try {
                            long currentTimeMillis2 = System.currentTimeMillis();
                            j5 = 0;
                        } catch (Exception e2) {
                            e = e2;
                            bufferedInputStream = bufferedInputStream3;
                        } catch (Throwable th2) {
                            th = th2;
                            bufferedInputStream = bufferedInputStream3;
                        }
                    } catch (Exception e3) {
                        e = e3;
                        bufferedInputStream = null;
                    } catch (Throwable th3) {
                        th = th3;
                        bufferedInputStream = null;
                    }
                } catch (Exception e4) {
                    e = e4;
                    bufferedInputStream = null;
                }
                while (true) {
                    int read = bufferedInputStream3.read(bArr);
                    if (read == -1) {
                        break;
                    }
                    bufferedInputStream4 = bufferedInputStream3;
                    j5 += read;
                    try {
                        try {
                            if (healthCheckDto.isCancel()) {
                                Logger.d("HealthCheckLogic", "checkDownloadSpeed 0003");
                                break loop0;
                            } else if (parseInt < ((int) (System.currentTimeMillis() - currentTimeMillis))) {
                                Logger.d("HealthCheckLogic", "checkDownloadSpeed 0004");
                                break loop0;
                            } else {
                                bufferedInputStream3 = bufferedInputStream4;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            bufferedInputStream = bufferedInputStream4;
                            FileUtils.close(inputStream);
                            FileUtils.close(bufferedInputStream);
                            throw th;
                        }
                    } catch (Exception e5) {
                        e = e5;
                        bufferedInputStream = bufferedInputStream4;
                    }
                    try {
                        Logger.d("HealthCheckLogic", "checkDownloadSpeed 0005", e);
                        FileUtils.close(inputStream);
                        FileUtils.close(bufferedInputStream);
                        j = j3;
                        j2 = j4;
                    } catch (Throwable th5) {
                        th = th5;
                        FileUtils.close(inputStream);
                        FileUtils.close(bufferedInputStream);
                        throw th;
                    }
                }
                bufferedInputStream2 = bufferedInputStream4;
                break loop0;
            }
            bufferedInputStream2 = null;
            inputStream = null;
            FileUtils.close(inputStream);
            FileUtils.close(bufferedInputStream2);
            j = j3;
            j2 = j4;
        } else {
            j = 0;
            j2 = 0;
        }
        getDSpeedResult(context, healthCheckDto, j, j2);
        Logger.d("HealthCheckLogic", "checkDownloadSpeed 0006");
    }

    public String parseSsid(String str) {
        Logger.d("HealthCheckLogic", "parseSsid 0001");
        if (str != null) {
            int length = str.length();
            if (str.startsWith("0x")) {
                Logger.d("HealthCheckLogic", "parseSsid 0001");
                return str.replaceFirst("0x", "");
            } else if (length > 1 && str.charAt(0) == '\"') {
                int i = length - 1;
                if (str.charAt(i) == '\"') {
                    Logger.d("HealthCheckLogic", "parseSsid 0002");
                    return str.substring(1, i);
                }
            }
        }
        Logger.d("HealthCheckLogic", "parseSsid 0003");
        return str;
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

    public String[] getUrlList(HealthChkMngDto healthChkMngDto) {
        Logger.d("HealthCheckLogic", "getUrlList 0001");
        String[] strArr = null;
        if (healthChkMngDto != null) {
            Logger.d("HealthCheckLogic", "getUrlList 0002");
            ExecuteHttpTask executeHttpTask = getExecuteHttpTask(healthChkMngDto.url, healthChkMngDto.timeout);
            executeHttpTask.execute();
            HttpResponse response = executeHttpTask.getResponse();
            if (response != null) {
                Logger.d("HealthCheckLogic", "getUrlList 0003");
                try {
                    String[] split = response.getEntity().split("\n");
                    Integer.parseInt(split[0]);
                    if (split.length < 2) {
                        Logger.d("HealthCheckLogic", "getUrlList 0004");
                    } else {
                        strArr = split;
                    }
                } catch (Exception e) {
                    Logger.d("HealthCheckLogic", "getUrlList 0005", e);
                }
            }
        }
        Logger.d("HealthCheckLogic", "getUrlList 0006");
        return strArr;
    }

    public void getDSpeedResult(Context context, HealthCheckDto healthCheckDto, long j, long j2) {
        Logger.d("HealthCheckLogic", "getDSpeedResult 0001");
        if (j == 0) {
            Logger.d("HealthCheckLogic", "getDSpeedResult 0002");
            j = 1;
        }
        long j3 = (j2 * 8) / j;
        if (j3 < Integer.parseInt(context.getString(R.string.mast_download_speed))) {
            Logger.d("HealthCheckLogic", "getDSpeedResult 0003");
            healthCheckDto.myDownloadSpeed = R.string.h_check_low_speed;
            healthCheckDto.myDSpeedImage = R.drawable.health_check_speed_low;
        } else if (j3 < Integer.parseInt(context.getString(R.string.recommended_d_speed))) {
            Logger.d("HealthCheckLogic", "getDSpeedResult 0004");
            healthCheckDto.myDownloadSpeed = R.string.h_check_middle_speed;
            healthCheckDto.myDSpeedImage = R.drawable.health_check_speed_middle;
        } else {
            Logger.d("HealthCheckLogic", "getDSpeedResult 0005");
            healthCheckDto.myDownloadSpeed = R.string.h_check_high_speed;
            healthCheckDto.myDSpeedImage = R.drawable.health_check_speed_high;
        }
        healthCheckDto.isCheckedDSpeed = R.string.health_check_ok;
        Logger.d("HealthCheckLogic", "getDSpeedResult 0006");
    }

    public ExecuteHttpTask getExecuteHttpTask(String str, int i) {
        Logger.d("HealthCheckLogic", "getExecuteHttpTask 0001");
        return new ExecuteHttpTask(str, i);
    }
}
