package tw.edu.kuas.wifiautologin.callbacks;

public class Constant {

	public static final String[] EXPECTED_SSIDS = {"kuas_wireless", "KUAS", "KUAS-Dorm"};
	public static final String TAG = "HearSilent";

    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:38.0) Gecko/20100101 Firefox/38.0";
    public static final int TIMEOUT_LOGOUT = 8000;
    public static final int TIMEOUT_LOGIN = 6000;
    public static final String JIANGONG_WIFI_SERVER = "172.16.61.253";
    public static final String YANCHAO_WIFI_SERVER = "172.16.109.253";

	// Notification IDs
	public static final int NOTIFICATION_LOGIN_ID = 100;

	// Memory (Shared Preferences) Keys
	public static final String MEMORY_KEY_USER = "MEMORY_KEY_USER";
	public static final String MEMORY_KEY_PASSWORD = "MEMORY_KEY_PASSWORD";
    public static final String MEMORY_KEY_ERRORTIMES = "MEMORY_KEY_ERRORTIMES";

}
