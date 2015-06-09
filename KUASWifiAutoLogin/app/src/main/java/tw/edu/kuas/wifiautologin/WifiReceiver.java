package tw.edu.kuas.wifiautologin;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import tw.edu.kuas.wifiautologin.callbacks.Constant;
import tw.edu.kuas.wifiautologin.callbacks.Memory;
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
					String infoString =
							String.format(context.getString(R.string.connected_to_ssid), ssid);
					Toast.makeText(context, infoString, Toast.LENGTH_SHORT).show();
					String user = Memory.getString(context, Constant.MEMORY_KEY_USER, null);
					String password = Memory.getString(context, Constant.MEMORY_KEY_PASSWORD, null);
					if (user != null && password != null) {
						String userData = tranUser(user);
						LoginHelper.login(context, userData.split(",")[1], userData.split(",")[0],
								password, userData.split(",")[2], null);
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

	private String tranUser(String user)
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
        else if (user.contains("@") && !user.contains("@guest"))
            return user + ",,Cyber";
        else
            if (user.contains("@guest"))
                return user + ",,Guest";
            else
                return user + "@guest,,Guest";
	}
}