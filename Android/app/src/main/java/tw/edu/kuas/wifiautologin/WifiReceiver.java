package tw.edu.kuas.wifiautologin;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import tw.edu.kuas.wifiautologin.libs.Constant;
import tw.edu.kuas.wifiautologin.libs.Memory;
import tw.edu.kuas.wifiautologin.libs.LoginHelper;
import tw.edu.kuas.wifiautologin.libs.Utils;

public class WifiReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, Intent intent) {
		String action = intent.getAction();

		if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
			WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
			NetworkInfo.State state = networkInfo.getState();

			if (state == NetworkInfo.State.CONNECTED) {
				String ssid = manager.getConnectionInfo().getSSID().replace("\"", "");
				if (Utils.isExpectedSsid(ssid)) {
					// connected
					String user = Memory.getString(context, Constant.MEMORY_KEY_USER, null);
					String password = Memory.getString(context, Constant.MEMORY_KEY_PASSWORD, null);
					if (user != null && password != null) {
						String userData = Utils.tranUser(user);
						if (ssid.equals(Constant.EXPECTED_SSIDS[2])) {
							LoginHelper
									.login(context, userData.split(",")[0], password, "Dorm", null);
						} else {
							LoginHelper.login(context, userData.split(",")[0], password,
									userData.split(",")[2], null);
						}
					}
				}
			}

			if (state == NetworkInfo.State.DISCONNECTED) {
				if (manager.isWifiEnabled()) {
					// disconnected
					String infoString = "Wi-Fi disconnected.";
					Log.i(Constant.TAG, infoString);
					NotificationManager notificationManager = (NotificationManager) context
							.getSystemService(Context.NOTIFICATION_SERVICE);
					notificationManager.cancel(Constant.NOTIFICATION_LOGIN_ID);
				}
			}
		}
	}
}