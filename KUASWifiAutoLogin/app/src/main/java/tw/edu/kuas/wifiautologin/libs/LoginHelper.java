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
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import tw.edu.kuas.wifiautologin.MainActivity;
import tw.edu.kuas.wifiautologin.R;
import tw.edu.kuas.wifiautologin.callbacks.Constant;
import tw.edu.kuas.wifiautologin.callbacks.GeneralCallback;

public class LoginHelper {
    private static AsyncHttpClient mClient = init();
    private static AsyncHttpClient mTestClient = initTest();

    private static NotificationManager mNotificationManager;
    private static NotificationCompat.Builder mBuilder;

    public static GoogleAnalytics analytics;
    public static Tracker tracker;

    private static AsyncHttpClient init() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("Connection", "Keep-Alive");
        client.setTimeout(7500);
        client.setEnableRedirects(false);
        return client;
    }

    private static AsyncHttpClient initTest() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("Connection", "Keep-Alive");
        client.setTimeout(5000);
        client.setEnableRedirects(false);
        return client;
    }

    public static void login(final Context context, String idType, String user, String password,
                             final String loginType, final GeneralCallback callback) {
        // init GA
        analytics = GoogleAnalytics.getInstance(context);
        analytics.setLocalDispatchPeriod(1);

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

        Log.d(Constant.TAG, getIPAddress(context));

        final RequestParams params = new RequestParams();
        params.put("idtype", idType);
        params.put("username", user);
        params.put("userpwd", password);
        params.put("login", "登入");
        params.put("orig_referer", "http://www.kuas.edu.tw/bin/home.php");

        mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setContentTitle(context.getString(R.string.app_name)).setContentText(
                String.format(context.getString(R.string.login_to_ssid), currentSsid))
                .setSmallIcon(R.drawable.ic_stat_login).setProgress(0, 0, true).setOngoing(false);

        mTestClient.get(context, "http://www.example.com", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] bytes) {
                String resultString = context.getString(R.string.login_ready);
                String _IP = getIPAddress(context);
                if (statusCode == 200) {
                    Log.d(Constant.TAG, "Already Login.");

                    tracker.send(new HitBuilders.EventBuilder()
                            .setCategory("alreadyLogin")
                            .setAction("onSuccess")
                            .setLabel(_IP + "/" + loginType)
                            .build());

                    if (callback != null)
                        callback.onSuccess(resultString);

                    loginSuccess(context, loginType, callback, "建工", resultString, true);
                    Toast.makeText(context, resultString, Toast.LENGTH_SHORT).show();
                } else {
                    mNotificationManager.notify(Constant.NOTIFICATION_LOGIN_ID, mBuilder.build());

                    String loginServer = "";
                    if (headers != null) {
                        for (Header header : headers) {
                            if (header.getName().toLowerCase().equals("location"))
                            {
                                Uri uri = Uri.parse(header.getValue());
                                loginServer = uri.getAuthority();
                                break;
                            }
                        }
                    }

                    if (!loginServer.equals(""))
                        loginWithHeader(context, params, loginType, callback, false, loginServer);
                    else
                        if (_IP.split("\\.")[0].equals("172") && _IP.split("\\.")[1].equals("17"))
                            loginJiangong(context, params, loginType, callback, true);
                        else
                            loginYanchao(context, params, loginType, callback, true);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] bytes, Throwable throwable) {
                mNotificationManager.notify(Constant.NOTIFICATION_LOGIN_ID, mBuilder.build());

                String _IP = getIPAddress(context);
                String loginServer = "";
                if (headers != null) {
                    for (Header header : headers) {
                        if (header.getName().toLowerCase().equals("location"))
                        {
                            Uri uri = Uri.parse(header.getValue());
                            loginServer = uri.getAuthority();
                            break;
                        }
                    }
                }

                if (!loginServer.equals(""))
                    loginWithHeader(context, params, loginType, callback, false, loginServer);
                else
                    if (_IP.split("\\.")[0].equals("172") && _IP.split("\\.")[1].equals("17"))
                        loginJiangong(context, params, loginType, callback, true);
                    else
                        loginYanchao(context, params, loginType, callback, true);
            }
        });
    }

    private static void loginWithHeader(final Context context, final RequestParams params, final String loginType,
                                        final GeneralCallback callback, final boolean firstCheck, final String loginServer) {
        Log.d(Constant.TAG, "loginWithHeader");

        mClient.post(context, "http://" + loginServer + "/cgi-bin/ace_web_auth.cgi", params,
                new AsyncHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] response) {

                        mTestClient.get(context, "http://www.example.com", new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] bytes) {
                                if (statusCode == 200)
                                    loginSuccess(context, loginType, callback, loginServer, "", true);
                                else
                                    retryLogin(context, params, loginType, callback, firstCheck, loginServer, statusCode);
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] bytes, Throwable e) {
                                e.printStackTrace();

                                retryLogin(context, params, loginType, callback, firstCheck, loginServer, statusCode);
                            }
                        });
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                        e.printStackTrace();

                        retryLogin(context, params, loginType, callback, firstCheck, loginServer, statusCode);
                    }
                });
    }

    private static void loginJiangong(final Context context, final RequestParams params,
                                      final String loginType, final GeneralCallback callback, final boolean firstCheck) {
        Log.d(Constant.TAG, "loginJiangong");

        mClient.post(context, "http://172.16.61.253/cgi-bin/ace_web_auth.cgi", params,
            new AsyncHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] response) {

                    mTestClient.get(context, "http://www.example.com", new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] bytes) {
                            if (statusCode == 200)
                                loginSuccess(context, loginType, callback, "建工", "", true);
                            else
                                retryLogin(context, params, loginType, callback, firstCheck, "建工", statusCode);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] bytes, Throwable e) {
                            e.printStackTrace();

                            retryLogin(context, params, loginType, callback, firstCheck, "建工", statusCode);
                        }
                    });
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                    e.printStackTrace();

                    retryLogin(context, params, loginType, callback, firstCheck, "建工", statusCode);
                }
            });
    }

    private static void loginYanchao(final Context context, final RequestParams params,
                                     final String loginType, final GeneralCallback callback, final boolean firstCheck) {
        Log.d(Constant.TAG, "loginYanchao");

        mClient.post(context, "http://172.16.109.253/cgi-bin/ace_web_auth.cgi", params,
            new AsyncHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, final byte[] response) {

                    mTestClient.get(context, "http://www.example.com", new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] bytes) {
                            if (statusCode == 200)
                                loginSuccess(context, loginType, callback, "燕巢", "", true);
                            else
                                retryLogin(context, params, loginType, callback, firstCheck, "燕巢", statusCode);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] bytes, Throwable e) {
                            e.printStackTrace();

                            retryLogin(context, params, loginType, callback, firstCheck, "燕巢", statusCode);
                        }
                    });
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] errorResponse,
                                      Throwable e) {
                    e.printStackTrace();

                    retryLogin(context, params, loginType, callback, firstCheck, "燕巢", statusCode);
                }
            });
    }

    private static void retryLogin(Context context, RequestParams params, String loginType,
                                   GeneralCallback callback, boolean firstCheck, String loginSpace, int statusCode)
    {
        String _IP = getIPAddress(context);

        if (firstCheck) {
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory("retryLogin")
                    .setAction("onTry")
                    .setLabel(loginSpace + statusCode + "/" + _IP + "/" + loginType)
                    .build());

            if (loginSpace.equals("燕巢"))
                loginJiangong(context, params, loginType, callback, false);
            else
                loginYanchao(context, params, loginType, callback, false);
            return;
        }

        String resultString;

        resultString = context.getString(R.string.failed_to_login);
        mBuilder.setContentTitle(context.getString(R.string.app_name))
                .setContentText(resultString).setSmallIcon(R.drawable.ic_stat_login)
                .setContentIntent(getFailPendingIntent(context))
                .setAutoCancel(true)
                .setVibrate(new long[]{300, 200, 300, 200})
                .setLights(Color.RED, 800, 800)
                .setProgress(0, 0, false);

        if (callback != null) {
            callback.onFail(resultString);
        }
        // Show error details in the expanded notification
        mBuilder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(resultString));

        mNotificationManager
                .notify(Constant.NOTIFICATION_LOGIN_ID, mBuilder.build());

        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("UX")
                .setAction("onFailure")
                .setLabel(loginSpace + "/" + _IP + "/" + loginType)
                .build());
    }

    private static void loginSuccess(Context context, String loginType, GeneralCallback callback, String loginSpace, String resultString, boolean vibrate)
    {
        if (resultString.equals(""))
        {
            switch (loginType)
            {
                case "Student":
                    resultString = context.getString(R.string.login_successfully);
                    break;
                case "Cyber":
                    resultString = context.getString(R.string.login_cyber_successfully);
                    break;
                default:
                    resultString = context.getString(R.string.login_guest_successfully);
            }
        }

        if (callback != null)
            callback.onSuccess(resultString);

        mBuilder.setContentTitle(context.getString(R.string.app_name))
                .setContentText(resultString).setSmallIcon(R.drawable.ic_stat_login)
                .setContentIntent(getDefaultPendingIntent(context))
                .setAutoCancel(true)
                .setProgress(0, 0, false);

        mNotificationManager
                .notify(Constant.NOTIFICATION_LOGIN_ID, mBuilder.build());

        if (vibrate)
        {
            mBuilder.setVibrate(new long[]{300, 200, 300, 200})
                    .setLights(Color.GREEN, 800, 800)
                    .setDefaults(Notification.DEFAULT_SOUND);

            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory("UX")
                    .setAction("onSuccess")
                    .setLabel(loginSpace + "/" + loginType)
                    .build());
        }
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