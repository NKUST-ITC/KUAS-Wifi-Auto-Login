package tw.edu.kuas.wifiautologin.libs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Constant {

	public static final List<String> EXPECTED_SSIDS =
			new ArrayList<>(Arrays.asList("kuas_wireless", "KUAS", "KUAS-Dorm"));
	public static final String TAG = "HearSilent";

	public static final String JIANGONG_WIFI_SERVER = "172.16.61.253";
	public static final String YANCHAO_WIFI_SERVER = "172.16.109.253";

	public static final String DEFAULT_GUEST_ACCOUNT = "0937808285@guest";
	public static final String DEFAULT_GUEST_PWD = "1306";

	// Notification IDs
	public static final int NOTIFICATION_LOGIN_ID = 121;
	public static final int NOTIFICATION_SUCCESS_ID = 122;
	public static final int NOTIFICATION_FAIL_ID = 123;
	public static final int NOTIFICATION_ALREADY_ID = 124;

	// Memory (Shared Preferences) Keys
	public static final String MEMORY_KEY_USER = "MEMORY_KEY_USER";
	public static final String MEMORY_KEY_PASSWORD = "MEMORY_KEY_PASSWORD";
	public static final String MEMORY_KEY_ERRORTIMES = "MEMORY_KEY_ERRORTIMES";

}
