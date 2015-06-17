package tw.edu.kuas.wifiautologin.libs;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.LinkedHashMap;

import tw.edu.kuas.wifiautologin.MainActivity;
import tw.edu.kuas.wifiautologin.R;
import tw.edu.kuas.wifiautologin.callbacks.Constant;
import tw.edu.kuas.wifiautologin.callbacks.GeneralCallback;
import tw.edu.kuas.wifiautologin.callbacks.Memory;

public class LoginHelper {
    private static AsyncHttpClient mClient = init();

    private static NotificationManager mNotificationManager;
    private static NotificationCompat.Builder mBuilder;

    public static GoogleAnalytics analytics;
    public static Tracker tracker;

    private static AsyncHttpClient init() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(Constant.TIMEOUT);
        client.setUserAgent(Constant.USER_AGENT);
        client.setEnableRedirects(false);
        return client;
    }

    public static void login(final Context context, String user, String password,
                             final String loginType, final GeneralCallback callback) {
        // init GA
        analytics = GoogleAnalytics.getInstance(context);
        analytics.setLocalDispatchPeriod(30);

        tracker = analytics.newTracker("UA-46334408-1");
        tracker.enableExceptionReporting(true);
        tracker.enableAdvertisingIdCollection(true);
        tracker.enableAutoActivityTracking(true);

        tracker.setScreenName("LoginHelper");

        String currentSsid = Utils.getCurrentSsid(context);
        if (currentSsid == null || !Utils.isExpectedSsid(currentSsid)) {
            if (currentSsid == null) {
                currentSsid = context.getString(R.string.no_wifi_connection);
                if (callback != null)
                    callback.onFail(currentSsid);
                return;
            }
            if (callback != null)
                callback.onFail(String.format(context.getString(R.string.ssid_no_support), currentSsid));
            return;
        }

        final LinkedHashMap<String, String> paramsMap = new LinkedHashMap<>();
        paramsMap.put("username", user);
        paramsMap.put("userpwd", password);
        paramsMap.put("login", "");
        paramsMap.put("orig_referer", "");

        mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setContentTitle(context.getString(R.string.kuas_wifi_auto_login)).setContentText(
                String.format(context.getString(R.string.login_to_ssid), currentSsid))
                .setSmallIcon(R.drawable.ic_stat_login).setProgress(0, 0, true).setOngoing(false);

        mClient.get(context, "http://www.example.com", new AsyncHttpResponseHandler() {
            String _IP = getIPAddress(context);
            String loginServer = "";

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] bytes) {
                String resultString = context.getString(R.string.login_ready);
                String _IP = getIPAddress(context);
                if (statusCode == HttpStatus.SC_OK) {
                    Log.d(Constant.TAG, "Already Login.");

                    if (callback != null)
                    {
                        loginSuccess(context, loginType, callback,
                                (_IP.split("\\.")[0].equals("172") && _IP.split("\\.")[1].equals("17")) ? Constant.JIANGONG_WIFI_SERVER : Constant.YANCHAO_WIFI_SERVER
                                , resultString, false);
                        Toast.makeText(context, resultString, Toast.LENGTH_SHORT).show();

                        tracker.send(new HitBuilders.EventBuilder()
                                .setCategory("onSuccess")
                                .setAction("alreadyLogin")
                                .setLabel(_IP + "/" + loginType)
                                .build());

                        callback.onSuccess(resultString);
                    }

                } else {
                    mNotificationManager.notify(Constant.NOTIFICATION_LOGIN_ID, mBuilder.build());

                    if (headers != null) {
                        for (Header header : headers) {
                            if (header.getName().toLowerCase().equals("location")) {
                                Uri uri = Uri.parse(header.getValue());
                                loginServer = uri.getAuthority();
                                break;
                            }
                        }
                    }

                    if (!loginServer.equals(""))
                        loginWithHeader(context, paramsMap, loginType, callback, false, loginServer);
                    else if (_IP.split("\\.")[0].equals("172") && _IP.split("\\.")[1].equals("17"))
                        loginWithHeader(context, paramsMap, loginType, callback, true, Constant.JIANGONG_WIFI_SERVER);
                    else
                        loginWithHeader(context, paramsMap, loginType, callback, true, Constant.YANCHAO_WIFI_SERVER);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] bytes, Throwable throwable) {
                mNotificationManager.notify(Constant.NOTIFICATION_LOGIN_ID, mBuilder.build());

                if (headers != null) {
                    for (Header header : headers) {
                        if (header.getName().toLowerCase().equals("location")) {
                            Uri uri = Uri.parse(header.getValue());
                            loginServer = uri.getAuthority();
                            break;
                        }
                    }
                }

                if (!loginServer.equals(""))
                    loginWithHeader(context, paramsMap, loginType, callback, false, loginServer);
                else if (_IP.split("\\.")[0].equals("172") && _IP.split("\\.")[1].equals("17"))
                    loginWithHeader(context, paramsMap, loginType, callback, true, Constant.JIANGONG_WIFI_SERVER);
                else
                    loginWithHeader(context, paramsMap, loginType, callback, true, Constant.YANCHAO_WIFI_SERVER);
            }
        });
    }

    private static void loginWithHeader(final Context context, final LinkedHashMap<String, String> paramsMap, final String loginType,
                                        final GeneralCallback callback, final boolean retry, final String loginServer) {
        final Handler refresh = new Handler(Looper.getMainLooper());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Connection.Response response = Jsoup.connect(String.format("http://%s/cgi-bin/ace_web_auth.cgi", loginServer))
                            .data(paramsMap)
                            .userAgent(Constant.USER_AGENT)
                            .timeout(Constant.TIMEOUT)
                            .followRedirects(false)
                            .ignoreContentType(true)
                            .ignoreHttpErrors(true)
                            .method(Connection.Method.POST)
                            .execute();

                    final String _response = response.body();
                    final int _statusCode = response.statusCode();

                    if(_statusCode == HttpStatus.SC_OK) {
                        refresh.post(new Runnable() {
                            public void run() {
                                mClient.get(context, "http://www.example.com/", new AsyncHttpResponseHandler() {
                                    @Override
                                    public void onSuccess(final int statusCode, Header[] headers, byte[] bytes) {
                                        if (statusCode == HttpStatus.SC_OK)
                                            loginSuccess(context, loginType, callback, loginServer, "", true);
                                        else
                                            retryLogin(context, paramsMap, loginType, callback, retry, loginServer, "",
                                                    retry ? "" : _response);
                                    }

                                    @Override
                                    public void onFailure(final int statusCode, Header[] headers, byte[] bytes, Throwable e) {
                                        e.printStackTrace();

                                        retryLogin(context, paramsMap, loginType, callback, retry, loginServer, "",
                                                retry ? "" : _response);
                                    }
                                });
                            }
                        });
                    }
                    else {
                        if (_statusCode == HttpStatus.SC_MOVED_TEMPORARILY)
                            Log.d(Constant.TAG, Integer.toString(_statusCode));
                        refresh.post(new Runnable() {
                            public void run() {
                                mClient.get(context, "http://www.example.com/", new AsyncHttpResponseHandler() {
                                    @Override
                                    public void onSuccess(final int statusCode, Header[] headers, byte[] bytes) {
                                        if (statusCode == HttpStatus.SC_OK)
                                            loginSuccess(context, loginType, callback, loginServer, "", true);
                                        else
                                            retryLogin(context, paramsMap, loginType, callback, retry, loginServer, "",
                                                    retry ? "" : _response);
                                    }

                                    @Override
                                    public void onFailure(final int statusCode, Header[] headers, byte[] bytes, Throwable e) {
                                        e.printStackTrace();

                                        retryLogin(context, paramsMap, loginType, callback, retry, loginServer, "",
                                                retry ? "" : _response);
                                    }
                                });
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    refresh.post(new Runnable() {
                        public void run() {
                            retryLogin(context, paramsMap, loginType, callback, retry, loginServer,
                                    context.getString(R.string.login_timeout), "Time Out");
                        }
                    });
                }
            }
        }).start();
    }

    private static void retryLogin(Context context, final LinkedHashMap<String, String> paramsMap, String loginType,
                                   GeneralCallback callback, boolean retry, String loginServer, String resultString, String response)
    {
        if (retry) {
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory("retryLogin")
                    .setAction("onTry")
                    .setLabel((loginServer.equals(Constant.JIANGONG_WIFI_SERVER) ? "建工" : "燕巢") +  "/" + loginType)
                    .build());

            if (loginServer.equals(Constant.JIANGONG_WIFI_SERVER))
                loginWithHeader(context, paramsMap, loginType, callback, false, Constant.YANCHAO_WIFI_SERVER);
            else
                loginWithHeader(context, paramsMap, loginType, callback, false, Constant.JIANGONG_WIFI_SERVER);
            return;
        }

        if (resultString.equals(""))
            resultString = context.getString(R.string.failed_to_login);

        mBuilder.setContentTitle(context.getString(R.string.kuas_wifi_auto_login))
                .setContentText(resultString).setSmallIcon(R.drawable.ic_stat_login)
                .setContentIntent(getFailPendingIntent(context))
                .setAutoCancel(true)
                .setVibrate(new long[]{300, 200, 300, 200})
                .setLights(Color.RED, 800, 800)
                .setProgress(0, 0, false);

        if (callback != null)
            callback.onFail(resultString);

        int errorTimes = Memory.getInt(context, Constant.MEMORY_KEY_ERRORTIMES, 0);

        mBuilder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(resultString));

        if (!(callback == null && errorTimes >= 3))
            mNotificationManager
                    .notify(Constant.NOTIFICATION_LOGIN_ID, mBuilder.build());

        errorTimes++;
        Memory.setInt(context, Constant.MEMORY_KEY_ERRORTIMES, errorTimes);

        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("onFailure")
                .setAction((loginServer.equals(Constant.JIANGONG_WIFI_SERVER) ? "建工" : "燕巢") + "/" + loginType)
                .setLabel(response)
                .build());
    }

    private static void loginSuccess(Context context, String loginType, GeneralCallback callback,
                                     String loginServer, String resultString, boolean notify)
    {
        if (resultString.equals(""))
        {
            switch (loginType)
            {
                case "Student":
                    resultString = String.format(context.getString(R.string.login_successfully),
                            (loginServer.equals(Constant.JIANGONG_WIFI_SERVER) ? context.getString(R.string.jiangong) :
                                    context.getString(R.string.yanchao)));
                    break;
                case "Cyber":
                    resultString = String.format(context.getString(R.string.login_cyber_successfully),
                            (loginServer.equals(Constant.JIANGONG_WIFI_SERVER) ? context.getString(R.string.jiangong) :
                                    context.getString(R.string.yanchao)));
                    break;
                case "Dorm":
                    resultString = String.format(context.getString(R.string.login_dorm_successfully),
                            (loginServer.equals(Constant.JIANGONG_WIFI_SERVER) ? context.getString(R.string.jiangong) :
                                    context.getString(R.string.yanchao)));
                    break;
                default:
                    resultString = String.format(context.getString(R.string.login_guest_successfully),
                            (loginServer.equals(Constant.JIANGONG_WIFI_SERVER) ? context.getString(R.string.jiangong) :
                                    context.getString(R.string.yanchao)));
            }
        }

        if (callback != null)
        {
            if (resultString.equals(context.getString(R.string.login_ready)))
                callback.onSuccess(resultString);
            else
                callback.onSuccess(resultString.split(",")[0] + ",\n" + resultString.split(",")[1]);
        }

        mBuilder.setContentTitle(context.getString(R.string.kuas_wifi_auto_login))
                .setContentText(resultString).setSmallIcon(R.drawable.ic_stat_login)
                .setContentIntent(getDefaultPendingIntent(context))
                .setAutoCancel(true)
                .setProgress(0, 0, false);

        Memory.setInt(context, Constant.MEMORY_KEY_ERRORTIMES, 0);

        if (notify)
        {
            mBuilder.setVibrate(new long[]{300, 200, 300, 200})
                    .setLights(Color.GREEN, 800, 800)
                    .setDefaults(Notification.DEFAULT_SOUND);

            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory("onSuccess")
                    .setAction((loginServer.equals(Constant.JIANGONG_WIFI_SERVER) ? "建工" : "燕巢"))
                    .setLabel(loginType)
                    .build());
        }

        if (resultString.equals(context.getString(R.string.login_ready)))
            mBuilder.setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(resultString));
        else
            mBuilder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(resultString.split(",")[0] + ",\n" + resultString.split(",")[1]));

        mNotificationManager
                .notify(Constant.NOTIFICATION_LOGIN_ID, mBuilder.build());
    }

    public static String getIPAddress(Context context) {
        WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        long ip = wifiInf.getIpAddress();
        if( ip != 0 )
            return String.format( "%d.%d.%d.%d",
                    (ip & 0xff),
                    (ip >> 8 & 0xff),
                    (ip >> 16 & 0xff),
                    (ip >> 24 & 0xff));

        return "0.0.0.0";
    }

	private static PendingIntent getDefaultPendingIntent(Context context) {
		return PendingIntent.getActivity(context, 0, new Intent(), 0);
    }

    private static PendingIntent getFailPendingIntent(Context context) {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(context, 0, notificationIntent, 0);
    }
}