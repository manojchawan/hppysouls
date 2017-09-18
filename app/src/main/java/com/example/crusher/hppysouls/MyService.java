package com.example.crusher.hppysouls;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.crusher.hppysouls.chatLists.ChatList;
import com.example.crusher.hppysouls.chats.ChatActivity;
import com.example.crusher.hppysouls.gangs.GangList;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

/**
 * Created by manoj on 4/9/2017.
 */

public class MyService extends Service {

    public static final String TAG = MyService.class.getSimpleName();
    public static final long NOTIFY_INTERVAL = 10 * 1000; // 10 seconds

    // run on another Thread to avoid crash
    private Handler mHandler = new Handler();
    private Timer mTimer = null;

    AsyncHttpClient client;
    NotificationCompat.Builder notification;
    int uniqueID;
    String user_num;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        SharedPreferences mPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
        user_num =mPreferences.getString("user_number","");
        notification = new NotificationCompat.Builder(this);
        notification.setAutoCancel(true);

        // cancel if already existed
        if (mTimer != null) {
            mTimer.cancel();
        } else {
            // recreate new
            mTimer = new Timer();
        }
        // schedule task
        mTimer.scheduleAtFixedRate(new UpdatesTimerTask(), 0, NOTIFY_INTERVAL);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private class UpdatesTimerTask extends TimerTask {
        @Override
        public void run() {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    checkUpdate();
                }

            });
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        private void checkUpdate() {
            client = new AsyncHttpClient();
            JSONObject obj = new JSONObject();
            try {
                obj.put("user_number", user_num);
                StringEntity stringEntity = new StringEntity(obj.toString());

                client.post(MyService.this, Implementations.notification_url, stringEntity, "application/json", new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        String response = new String(responseBody, StandardCharsets.UTF_8);
                        Log.d(TAG, "Service Success " + response);
                        try {
                            JSONObject resp = new JSONObject(response);
                            JSONArray myArr = resp.getJSONArray("update");
                            int count = myArr.length();
                            if (count > 0) {
                                showNotification(myArr);
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        String response = new String(responseBody, StandardCharsets.UTF_8);
                        Log.d(TAG, "Service fail: " + response);
                    }
                });

            } catch (JSONException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        private void showNotification(JSONArray updArr) {
            int count = updArr.length();
            Log.d(TAG, "showNotification: " + count + "::" + updArr);
            try {
                for (int i = 0; i < count; i++) {
                    JSONObject o = updArr.getJSONObject(i);
                    uniqueID = Integer.parseInt(o.getString("update_id"));
                    String title = o.getString("update_title");
                    String desc = o.getString("update_description");
                    String type = o.getString("update_type");
                    String from = o.getString("from");
                    String pic = o.getString("user_profile_url");
                    String name = o.getString("user_name");
                    user_num = o.getString("user_num");
                    String group_id = o.getString("group_id");

                    notification.setSmallIcon(R.drawable.ic_logo);
                    notification.setTicker(title);
                    notification.setWhen(System.currentTimeMillis());
                    notification.setContentTitle(title);
                    notification.setContentText(desc);
                    Intent intent = new Intent(MyService.this, Feeds.class);

                    if (type.equals("chat")) {
                        intent = new Intent(MyService.this, ChatActivity.class);

                        intent.putExtra("user_name", name);
                        intent.putExtra("chat_num", from);
                        intent.putExtra("user_pic", pic);

                    } else if (type.equals("group")) {
                        intent = new Intent(MyService.this, GangList.class);
                        intent.putExtra("user_num", user_num);
                        intent.putExtra("gang_id", group_id);
                        //intent.putExtra("gang_name", group_name);
                    }

                    PendingIntent pendingIntent = PendingIntent.getActivity(MyService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    notification.setContentIntent(pendingIntent);

                    //BUILDS NOTIFICATION AND ISSUE IT
                    NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    nm.notify(uniqueID, notification.build());

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
}
