package tw.edu.kuas.wifiautologin.callbacks;

public abstract class GeneralCallback {
	public abstract void onSuccess(String message);

	public abstract void onFail(String reason);

	public abstract void onAlready();
}
