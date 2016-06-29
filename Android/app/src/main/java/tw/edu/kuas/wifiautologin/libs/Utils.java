package tw.edu.kuas.wifiautologin.libs;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import java.util.Locale;

import tw.edu.kuas.wifiautologin.R;
import tw.edu.kuas.wifiautologin.models.UserModel;

@SuppressWarnings("unused") public class Utils {

	private static ConnectivityManager.NetworkCallback mCallback;
	private static ConnectivityManager mConnectivityManager;

	public static String getCurrentSSID(Context context) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		if (wifiInfo != null && !TextUtils.isEmpty(wifiInfo.getSSID())) {
			String ssid = wifiInfo.getSSID();
			if (postVersion(Build.VERSION_CODES.JELLY_BEAN_MR1)) {
				if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
					return ssid.substring(1, ssid.length() - 1);
				} else {
					return null;
				}
			} else {
				return ssid;
			}
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

	@SuppressWarnings("deprecation")
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

	@SuppressWarnings("deprecation")
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

	public static boolean isPermissionGranted(Context context, String permission) {
		return ContextCompat.checkSelfPermission(context, permission) ==
				PackageManager.PERMISSION_GRANTED;
	}

	public static boolean isPermissionsGranted(Context context, String... permissions) {
		for (String permission : permissions) {
			if (!isPermissionGranted(context, permission)) {
				return false;
			}
		}
		return true;
	}

	public static boolean isPermissionAlwaysDenied(Activity activity, String permission) {
		return ContextCompat.checkSelfPermission(activity, permission) ==
				PackageManager.PERMISSION_DENIED &&
				!ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
	}

	public static boolean isPermissionsAlwaysDenied(Activity activity, String... permissions) {
		for (String permission : permissions) {
			if (!isPermissionAlwaysDenied(activity, permission)) {
				return false;
			}
		}
		return true;
	}

	public static boolean checkGoogleBug() {
		return postVersion(Build.VERSION_CODES.LOLLIPOP) && Build.VERSION.RELEASE.equals("6.0");
	}

	/**
	 * The above WRITE_SETTINGS checks are only required for 6.0
	 */
	@TargetApi(Build.VERSION_CODES.M)
	public static boolean checkSystemWritePermission(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.RELEASE.equals("6.0")) {
			return Settings.System.canWrite(context);
		}
		// This is no longer needed in Android 6.0.1 or lower than Android 6.0 version
		return true;
	}

	@TargetApi(23)
	public static void showSystemWritePermissionDialog(final Activity activity,
	                                                   final int requestCode) {
		if (Utils.postVersion(Build.VERSION_CODES.M)) {
			new AlertDialog.Builder(activity).setTitle(R.string.permission_request_6_0_title)
					.setMessage(activity.getString(R.string.permission_request_6_0_message,
							"\uD83D\uDE09"))
					.setPositiveButton(R.string.setting, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
							intent.setData(Uri.parse("package:" + activity.getPackageName()));
							activity.startActivityForResult(intent, requestCode);
						}
					}).setNegativeButton(R.string.cancel, null).setCancelable(false).show();
		}
	}

	public static String getPhoneName() {
		return String.format(Locale.getDefault(), "%s %s %s", Build.MANUFACTURER, Build.MODEL,
				Build.VERSION.RELEASE);
	}

}
