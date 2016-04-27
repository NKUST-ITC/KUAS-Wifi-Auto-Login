package tw.edu.kuas.wifiautologin.libs;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import tw.edu.kuas.wifiautologin.models.UserModel;

public class Utils {

	private static ConnectivityManager.NetworkCallback mCallback;
	private static ConnectivityManager mConnectivityManager;

	public static String getCurrentSSID(Context context) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		if (wifiInfo != null && !TextUtils.isEmpty(wifiInfo.getSSID())) {
			String ssid = wifiInfo.getSSID();
			return ssid.startsWith("\"") ? ssid.substring(1, ssid.length() - 1) : null;
		}
		return null;
	}

	public static boolean isExpectedSSID(String ssid) {
		return Constant.EXPECTED_SSIDS.contains(ssid);
	}

	public static UserModel tranUser(String user, String pwd) {
		UserModel model = new UserModel();

		model.userpwd = pwd;
		if (user.endsWith("@kuas.edu.tw")) {
			model.username = user;
			if (user.split("@")[0].length() == 5) {
				model.idtype = "";
				model.loginType = UserModel.LoginType.TEACHER;
			} else {
				model.idtype = "1";
				model.loginType = UserModel.LoginType.STUDENT;
			}
		} else if (user.endsWith("@gm.kuas.edu.tw")) {
			model.username = user;
			model.idtype = "@gm.kuas.edu.tw";
			model.loginType = UserModel.LoginType.STUDENT;
		} else if (user.length() == 10 && !user.substring(0, 2).equals("09")) {
			if (Integer.parseInt(user.substring(1, 4)) <= 102) {
				model.username = user + "@kuas.edu.tw";
				model.idtype = "1";
			} else {
				model.username = user + "@gm.kuas.edu.tw";
				model.idtype = "@gm.kuas.edu.tw";
			}
			model.loginType = UserModel.LoginType.STUDENT;
		} else if (user.length() == 5) {
			model.username = user + "@kuas.edu.tw";
			model.idtype = "";
			model.loginType = UserModel.LoginType.TEACHER;
		} else if (user.contains("@") && !user.contains("@guest")) {
			model.username = user;
			model.idtype = "";
			model.loginType = UserModel.LoginType.CYBER;
		} else {
			model.username = user + (user.contains("@guest") ? "" : "@guest");
			model.idtype = "";
			model.loginType = UserModel.LoginType.GUEST;
		}

		return model;
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public static void requestNetwork(Context context) {
		if (!Utils.postVersion(Build.VERSION_CODES.LOLLIPOP)) {
			return;
		}

		setUpConnectivityManager(context);
		NetworkRequest.Builder builder = new NetworkRequest.Builder();
		builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
		builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);

		if (mCallback != null) {
			try {
				mConnectivityManager.unregisterNetworkCallback(mCallback);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
		mCallback = new ConnectivityManager.NetworkCallback() {
			@Override
			public void onAvailable(Network network) {
				if (mCallback != null) {
					Log.d(Constant.TAG, network.toString());
					if (Utils.postVersion(Build.VERSION_CODES.M)) {
						mConnectivityManager.bindProcessToNetwork(network);
					} else {
						ConnectivityManager.setProcessDefaultNetwork(network);
					}
				}
			}
		};

		mConnectivityManager.requestNetwork(builder.build(), mCallback);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public static void resetDefaultNetwork(Context context) {
		if (!Utils.postVersion(Build.VERSION_CODES.LOLLIPOP)) {
			return;
		}

		setUpConnectivityManager(context);
		if (Utils.postVersion(Build.VERSION_CODES.M)) {
			if (mConnectivityManager.getBoundNetworkForProcess() != null) {
				mConnectivityManager.bindProcessToNetwork(null);
			}
		} else {
			if (ConnectivityManager.getProcessDefaultNetwork() != null) {
				ConnectivityManager.setProcessDefaultNetwork(null);
			}
		}

		if (mCallback != null) {
			try {
				mConnectivityManager.unregisterNetworkCallback(mCallback);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
		mCallback = null;
	}

	private static void setUpConnectivityManager(Context context) {
		if (mConnectivityManager == null) {
			mConnectivityManager =
					(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		}
	}

	public static boolean postVersion(int sdkInt) {
		return Build.VERSION.SDK_INT >= sdkInt;
	}

}
