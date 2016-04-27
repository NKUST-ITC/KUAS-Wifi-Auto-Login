package tw.edu.kuas.wifiautologin.callbacks;

import android.content.Context;

import tw.edu.kuas.wifiautologin.libs.Utils;

public abstract class GeneralCallback {
	public void onSuccess(Context context, String message) {
		Utils.resetDefaultNetwork(context);
		onSuccess(message);
	}

	public void onFail(Context context, String reason) {
		Utils.resetDefaultNetwork(context);
		onFail(reason);
	}

	public void onAlready(Context context) {
		Utils.resetDefaultNetwork(context);
		onAlready();
	}

	public abstract void onSuccess(String message);

	public abstract void onFail(String reason);

	public abstract void onAlready();
}
