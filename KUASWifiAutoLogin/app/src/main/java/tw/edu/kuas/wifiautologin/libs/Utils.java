package tw.edu.kuas.wifiautologin.libs;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import tw.edu.kuas.wifiautologin.callbacks.Constant;

public class Utils {

	public static String getCurrentSsid(Context context) {
		String ssid = null;
		ConnectivityManager connManager =
				(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (networkInfo.isConnected()) {
			final WifiManager wifiManager =
					(WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
			if (connectionInfo != null && connectionInfo.getSSID() != null &&
					!connectionInfo.getSSID().equals("")) {
				ssid = connectionInfo.getSSID();
			}
		}
		return ssid == null ? ssid : ssid.replace("\"", "");
	}

	public static boolean isExpectedSsid(String ssid) {
		for (String expectedSsid : Constant.EXPECTED_SSIDS) {
			if (expectedSsid.equals(ssid)) {
				return true;
			}
		}
		return false;
	}

    public static String tranUser(String user)
    {
        if (user.contains("@kuas.edu.tw") || user.contains("@gm.kuas.edu.tw"))
            if (user.contains("@kuas.edu.tw"))
                return user + ",1,Student";
            else
                return user + ",@gm.kuas.edu.tw,Student";
        else if (user.length() == 10 && !user.substring(0,2).equals("09"))
            if (Integer.parseInt(user.substring(1,4)) <= 102)
                return user + "@kuas.edu.tw" + ",1,Student";
            else
                return user + "@gm.kuas.edu.tw" + ",@gm.kuas.edu.tw,Student";
        else if (user.length() == 5)
            return user + "@kuas.edu.tw" + ",,Teacher";
        else if (user.contains("@") && !user.contains("@guest"))
            return user + ",,Cyber";
        else
            if (user.contains("@guest"))
                return user + ",,Guest";
            else
                return user + "@guest,,Guest";
    }

}
