package tw.edu.kuas.wifiautologin.callbacks;

public abstract class GeneralCallback {
	public abstract void onSuccess();

	public abstract void onFail(String reason);
}
