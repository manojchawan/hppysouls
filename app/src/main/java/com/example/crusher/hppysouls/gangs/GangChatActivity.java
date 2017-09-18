package com.example.crusher.hppysouls.gangs;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.crusher.hppysouls.AppFunctions;
import com.example.crusher.hppysouls.Feeds;
import com.example.crusher.hppysouls.Implementations;
import com.example.crusher.hppysouls.R;
import com.example.crusher.hppysouls.chatLists.ChatList;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class GangChatActivity extends AppCompatActivity {

    private static final String TAG = GangChatActivity.class.getSimpleName();
    String gang_id, msgToSend, user_num, gang_name;
    EditText editText;
    Button sendBtn;
    SweetAlertDialog sweetAlertDialog;
    Handler handler;
    AsyncHttpClient client;
    GangChatAdapter mAdapter;
    NotificationCompat.Builder notification;
    int uniqueID;
    boolean flag = false;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gang_chat);

        notification = new NotificationCompat.Builder(GangChatActivity.this);
        notification.setAutoCancel(true);

        Intent i = getIntent();
        Bundle b = i.getExtras();
        if (b != null) {
            gang_name = b.getString("gang_name");
            setTitle(gang_name);
            gang_id = b.getString("gang_id");
            user_num =b.getString("user_num");
        }
        Log.d(TAG, "onCreate: gang " + gang_id + " NAME " + gang_name );


        sendBtn = (Button) findViewById(R.id.send_message);
        editText = (EditText) findViewById(R.id.new_message);
        ListView messageList = (ListView) findViewById(R.id.messages_list);
        mAdapter = new GangChatAdapter(this, new ArrayList<GangChatItem>());
        messageList.setAdapter(mAdapter);

        sweetAlertDialog = new SweetAlertDialog(GangChatActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.setTitleText("Loading");
        sweetAlertDialog.show();

        permissionCheck();

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msgToSend = editText.getText().toString();
                editText.getText().clear();
                if (!TextUtils.isEmpty(msgToSend)) {
                    sendMessage();
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void permissionCheck() {
        String token = AppFunctions.SHA1(AppFunctions.SHA1(user_num));
        client = new AsyncHttpClient();
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("user_number", user_num);
            jsonObj.put("group_id", gang_id);
            jsonObj.put("token", token);
            StringEntity stringEntity = new StringEntity(jsonObj.toString());
            // Log.d(TAG, "get msg: " + jsonObj.toString());
            client.post(GangChatActivity.this, Implementations.group_receive_url, stringEntity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "getchat Success " + response);
                    try {
                        JSONObject respObj = new JSONObject(response);
                        String permission = respObj.getString("permission");

                        if (permission.equals("1")) {
                            startChat();

                        } else {
                            sweetAlertDialog.cancel();
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(GangChatActivity.this);
                            alertDialog.setTitle("Join Permission");
                            alertDialog.setMessage("Someone added you in this group, Do you want to join? You can always leave group later.");

                            alertDialog.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    changeStatus();
                                }
                            });
                            // on pressing cancel button
                            alertDialog.setNegativeButton("Leave", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    removeFromGang(user_num);
                                    dialog.cancel();
                                }
                            });
                            alertDialog.show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "getchat fail  " + response);
                    sweetAlertDialog.cancel();
                }
            });

        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void changeStatus() {

        String token = AppFunctions.SHA1(AppFunctions.SHA1(user_num));
        client = new AsyncHttpClient();
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("user_number", user_num);
            jsonObj.put("group_id", gang_id);
            jsonObj.put("token", token);
            StringEntity stringEntity = new StringEntity(jsonObj.toString());
            Log.d(TAG, "check status " + jsonObj.toString());

            client.post(GangChatActivity.this, Implementations.group_status, stringEntity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "status Success " + response);
                    startChat();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "Status Failure " + response);
                }
            });

        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void startChat() {
        //TODO call this method in thread every 3-5 sec
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                flag = true;
                getChat();
                handler.postDelayed(this, 3000);
            }
        }, 2000);

    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void sendMessage() {
        client = new AsyncHttpClient();
        JSONObject jsonObj = new JSONObject();

        try {
            jsonObj.put("user_number", user_num);
            jsonObj.put("group_id", gang_id);
            jsonObj.put("group_message", msgToSend);
            StringEntity stringEntity = new StringEntity(jsonObj.toString());
            Log.d(TAG, "send msg: " + jsonObj.toString());

            client.post(GangChatActivity.this, Implementations.group_send_url, stringEntity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "send Success " + response);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "send Failure " + response);
                }
            });

        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void getChat() {
        String token = AppFunctions.SHA1(AppFunctions.SHA1(user_num));
        final List<GangChatItem> messages = new ArrayList<>();
        client = new AsyncHttpClient();
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("user_number", user_num);
            jsonObj.put("group_id", gang_id);
            jsonObj.put("token", token);
            StringEntity stringEntity = new StringEntity(jsonObj.toString());
            // Log.d(TAG, "get msg: " + jsonObj.toString());

            client.post(GangChatActivity.this, Implementations.group_receive_url, stringEntity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "getchat Success " + response);
                    sweetAlertDialog.cancel();
                    try {
                        JSONObject respObj = new JSONObject(response);

                        String permission = respObj.getString("permission");

                        if (permission.equals("1")) {
                            JSONArray msgArr = respObj.getJSONArray("message");
                            for (int i = 0; i < msgArr.length(); i++) {
                                JSONObject currObj = msgArr.getJSONObject(i);

                                String sender = currObj.getString("user_number");
                                String message = currObj.getString("message");
                                String time = currObj.getString("time");
                                String name = currObj.getString("user_name");

                                GangChatItem o = new GangChatItem(message, sender, time, name);
                                messages.add(o);
                            }
                        } else {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(GangChatActivity.this);
                            alertDialog.setTitle("Join Permission");
                            alertDialog.setMessage("Someone added you in this group, Do you want to join? You can always leave group later.");

                            alertDialog.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    changeStatus();
                                }
                            });

                            // on pressing cancel button
                            alertDialog.setNegativeButton("Leave", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            alertDialog.show();
                        }

                        mAdapter.clear();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mAdapter.addAll(messages);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "getchat fail  " + response);
                    sweetAlertDialog.cancel();
                }
            });

        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void removeFromGang(String number) {
        AsyncHttpClient client = new AsyncHttpClient();
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("group_member", number);
            jsonObj.put("group_id", gang_id);
            jsonObj.put("token", AppFunctions.SHA1(AppFunctions.SHA1(gang_id)));
            StringEntity stringEntity = new StringEntity(jsonObj.toString());
            Log.d(TAG, "remove user" + jsonObj.toString());

            client.post(GangChatActivity.this, Implementations.group_leave, stringEntity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "remove success " + response);
                    Intent i = new Intent(GangChatActivity.this, GangList.class);
                    startActivity(i);
                    finish();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "remove Failure " + response);
                }
            });

        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.gang_chat_menu, menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        if (id == R.id.menu_add_member) {
            handler.removeCallbacksAndMessages(null);
            Intent i = new Intent(GangChatActivity.this, AddMembers.class);
            startActivity(i);
            return true;
        }
        if (id == R.id.menu_setting) {
            displaySetting();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void displaySetting() {
        handler.removeCallbacksAndMessages(null);
        Intent i = new Intent(GangChatActivity.this, GangSetting.class);
        startActivity(i);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (flag)
            handler.removeCallbacksAndMessages(null);

        finish();
    }
}