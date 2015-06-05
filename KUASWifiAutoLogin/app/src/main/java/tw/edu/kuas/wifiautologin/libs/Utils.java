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
}
