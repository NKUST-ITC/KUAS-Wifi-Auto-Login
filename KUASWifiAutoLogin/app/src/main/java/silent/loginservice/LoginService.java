package silent.loginservice;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.client.ClientProtocolException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import silent.kuaswifiautologin.R;
import silent.network.NetworkStatus;

public class LoginService extends Service {
    private static final String TAG = "silent.loginservice.LoginService";
    private NetworkStatus networkStatus;

    private static final String PREF = "ACCOUNT_PREF";
    private static final String PREF_USERNAME = "USERNAME";
    private static final String PREF_PWD = "PASSWORD";
    private static final int TIMEOUT = 1000;
    @Override
    public void onCreate() {
        Log.i(TAG, "Service onCreate");
        networkStatus = new NetworkStatus(this);
    }

    public void showToast(final String toast) {
        Toast.makeText(LoginService.this, toast, Toast.LENGTH_SHORT).show();
    }

    public void AutoLogin() {
        Log.v(TAG, "Checking SSID");
        // Check SSID
        if (!checkSSID())
            return;

        new Thread() {
            @Override
            public void run() {
                // Connection Testing
                Log.v(TAG, "Testing Connection");
                if (isConnect()) {
                    return;
                }
                Log.v(TAG, "End test connection");

                Resources resources = getResources();
                String login_msg = getString(R.string.login_successfully);
                String[] params_name = resources.getStringArray(R.array.params_name);
                String[] params_value = resources.getStringArray(R.array.params_value);
                SharedPreferences setting = getSharedPreferences(PREF, 0);
                String username = setting.getString(PREF_USERNAME, "");
                String password = setting.getString(PREF_PWD, "");
                params_value[0] = username;
                params_value[1] = password;
                // Check params
                if (params_name.length != params_value.length)
                    Log.e(getString(R.string.app_name), "Config file error!");

                try {
                    // Check if using guest login
                    if (params_value[0].compareTo("") == 0 ||
                            params_value[1].compareTo("") == 0) {
                        Log.v(TAG, "use guest login");
                        login_msg = getString(R.string.login_guest_successfully);
                        LoginKUAS_guest(params_name);
                    } else {
                        LoginKUAS(params_name, params_value);
                    }
                    Log.v(TAG, "CHECK IS CONNECT: " + isConnect());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    showToast(getString(R.string.timeout));
                    return;
                }
                // Check if login finish
                if (isConnect()) {
                    NotificationManager mNotificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);//獲取一個notificationmanager
                    Notification notification=new Notification(R.drawable.ic_launcher, "高應無線通",  System.currentTimeMillis());
                    notification.defaults=Notification.DEFAULT_ALL;
                    notification.setLatestEventInfo(LoginService.this,"高應無線通", "Wi-Fi 登入成功！", null);
                    mNotificationManager.notify(1000, notification);
                    showToast(login_msg);
                } else {
                    return;
                }
            };
        }.start();
    }

    public boolean isConnect() {
        try {
            if (isLogin()) {
                Log.v(TAG, "is connected");
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            Log.v(TAG, e.toString());
            return false;
        }
    }

    public boolean isLogin() throws Exception {
        Document res = Jsoup.connect("http://www.example.com").timeout(TIMEOUT).ignoreContentType(true).method(Connection.Method.GET).get();
        if (res.title().compareTo("Example Domain") == 0)
            return true;
        else
            return false;
    }

    public void LoginKUAS(String[] params_name, String[] params_value) throws IOException {
        String url = getString(R.string.url_KUAS);
        HashMap<String, String> postDatas = new HashMap<>();
        // POST It!!
        // Try kuas.edu.tw
        if (params_value[0].length() > 4 && Integer.parseInt(params_value[0].substring(1, 4)) <= 102) {
            Log.v(TAG, "lower then 102");
            postDatas.put(params_name[0], params_value[0] + "@kuas.edu.tw");
            postDatas.put(params_name[1], params_value[1]);
            postDatas.put(params_name[2], params_value[2]);
            Jsoup.connect(url).timeout(TIMEOUT).data(postDatas).ignoreContentType(true).method(Connection.Method.POST).execute();
        } else {
            Log.v(TAG, "upper then 102");
            postDatas.put(params_name[0], params_value[0] + "@gm.kuas.edu.tw");
            postDatas.put(params_name[1], params_value[1]);
            postDatas.put(params_name[2], params_value[2]);
            Jsoup.connect(url).timeout(TIMEOUT).data(postDatas).ignoreContentType(true).method(Connection.Method.POST).execute();
        }
    }
    public void LoginKUAS_guest(String[] params_name) throws IOException {
        String url = getString(R.string.url_KUAS);
        // POST It!!
        HashMap<String, String> postDatas = new HashMap<>();
        postDatas.put(params_name[0], "0937808285@guest");
        postDatas.put(params_name[1], "1306");
        postDatas.put(params_name[2], "login");
        postDatas.put(params_name[3], "");
        Jsoup.connect(url).timeout(TIMEOUT).data(postDatas).ignoreContentType(true).method(Connection.Method.POST).execute();
    }

    public boolean checkSSID() {
        String ssid = networkStatus.getSSID();
        if (ssid.compareTo(getString(R.string.ssid_kuas_wireless)) != 0
                && ssid.compareTo(getString(R.string.ssid_KUAS)) != 0
                && ssid.compareTo(getString(R.string.ssid_KUAS_Dorm)) != 0)
            return false;
        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service onStartCommand");
        if (checkSSID())
            AutoLogin();
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        Log.i(TAG, "Service onBind");
        return null;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Service onDestroy");
    }
}