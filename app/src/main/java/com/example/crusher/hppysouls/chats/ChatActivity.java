package com.example.crusher.hppysouls.chats;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.crusher.hppysouls.AppFunctions;
import com.example.crusher.hppysouls.Feeds;
import com.example.crusher.hppysouls.Implementations;
import com.example.crusher.hppysouls.R;
import com.example.crusher.hppysouls.chatLists.ChatList;
import com.example.crusher.hppysouls.gangs.GangList;
import com.example.crusher.hppysouls.profile.Profile;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.squareup.picasso.Picasso;

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
import de.hdodenhof.circleimageview.CircleImageView;
import mehdi.sakout.fancybuttons.FancyButton;

public class ChatActivity extends AppCompatActivity {

    private final String TAG = ChatActivity.class.getSimpleName();
    private MessageAdapter mAdapter;
    public Menu menu;
    public MenuItem item;
    Button sendBtn;
    SweetAlertDialog sweetAlertDialog;
    EditText editText;
    String msgToSend, user_num, receiver_num, token, pic;
    static String action = "block", itemName = "Block";
    Handler handler;
    AsyncHttpClient client;
    boolean threadOn = false;

    NotificationCompat.Builder notification;
    int uniqueID;


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        notification = new NotificationCompat.Builder(ChatActivity.this);
        notification.setAutoCancel(true);

        user_num = Implementations.user_num;
        receiver_num = Implementations.chat_num;
        Intent i = getIntent();
        Bundle b = i.getExtras();
        if (b != null) {
            String title = b.getString("user_name");
            receiver_num = b.getString("chat_num");
            pic = b.getString("user_pic");
            setTitle(title);
        }

        token = AppFunctions.SHA1(AppFunctions.SHA1(user_num));
        sendBtn = (Button) findViewById(R.id.send_message);
        editText = (EditText) findViewById(R.id.new_message);
        FancyButton greeting = (FancyButton) findViewById(R.id.send_greeting);
        CircleImageView chatimg = (CircleImageView) findViewById(R.id.chatImage);
        Picasso.with(ChatActivity.this).load(pic).into(chatimg);

        View emptyView = findViewById(R.id.emptyView);
        ListView messageList = (ListView) findViewById(R.id.messages_list);
        messageList.setEmptyView(emptyView);
        mAdapter = new MessageAdapter(this, new ArrayList<Message>());
        messageList.setAdapter(mAdapter);

        greeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        sweetAlertDialog = new SweetAlertDialog(ChatActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.setTitleText("Loading");
        sweetAlertDialog.show();

        checkUserBlock();

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msgToSend = editText.getText().toString().trim();
                editText.getText().clear();
                if (!TextUtils.isEmpty(msgToSend)) {
                    sendMessage();
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void startCalling() {
        //TODO call this method in thread every 5 sec
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                threadOn = true;
                getChat();
                handler.postDelayed(this, 3000);
            }
        }, 1000);
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void checkUserBlock() {
        client = new AsyncHttpClient();
        JSONObject jsonObj = new JSONObject();

        try {
            jsonObj.put("user_number", user_num);
            jsonObj.put("to_user_number", receiver_num);
            jsonObj.put("token", token);
            StringEntity stringEntity = new StringEntity(jsonObj.toString());
            Log.d(TAG, "check is_user_block: " + jsonObj.toString());
            client.post(ChatActivity.this, Implementations.isUserBlock, stringEntity, "application/json", new AsyncHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "check is_blocked : " + response);
                    try {
                        sweetAlertDialog.cancel();
                        JSONObject obj = new JSONObject(response);
                        String str = obj.getString("block");
                        // item = menu.findItem(R.id.menu_block);
                        if (str.equals("true")) {
                            //            item.setTitle("UnBlock");
                            action = "unblock";
                            itemName = "Unblock";
                            Toast.makeText(ChatActivity.this, "User is Blocked", Toast.LENGTH_SHORT).show();
                        } else {
                            //    item.setTitle("Block");
                            action = "block";
                            itemName = "Block";
                            startCalling();
                        }
                        invalidateOptionsMenu();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "is block failed: " + response);
                }
            });
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void sendMessage() {

        client = new AsyncHttpClient();
        JSONObject jsonObj = new JSONObject();

        try {
            jsonObj.put("user_number", user_num);
            jsonObj.put("to_user_number", receiver_num);
            jsonObj.put("message", msgToSend);
            jsonObj.put("token", token);
            StringEntity stringEntity = new StringEntity(jsonObj.toString());
            Log.d(TAG, "send msg: " + jsonObj.toString());
            client.post(ChatActivity.this, Implementations.chat_url, stringEntity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "send Success " + response);
                    try {
                        JSONObject respObj = new JSONObject(response);
                        String res = respObj.getString("response");
                        if (!res.equals("OK")) {
                            Toast.makeText(ChatActivity.this, res, Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "send Failure " + response);
                    try {
                        JSONObject respObj = new JSONObject(response);
                        String res = respObj.getString("response");
                        Toast.makeText(ChatActivity.this, res, Toast.LENGTH_SHORT).show();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void getChat() {

        final List<Message> messages = new ArrayList<>();
        client = new AsyncHttpClient();
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("user_number", user_num);
            jsonObj.put("to_user_number", receiver_num);
            jsonObj.put("token", token);
            StringEntity stringEntity = new StringEntity(jsonObj.toString());
            //  Log.d(TAG, "get chat" + jsonObj.toString());
            client.post(ChatActivity.this, Implementations.chat_receive, stringEntity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "getchat Success " + response);
                    sweetAlertDialog.cancel();
                    try {
                        JSONObject respObj = new JSONObject(response);

                        JSONArray msgArr = respObj.getJSONArray("message");

                        for (int i = 0; i < msgArr.length(); i++) {
                            JSONObject currObj = msgArr.getJSONObject(i);
                            String sender = currObj.getString("user_number");
                            String message = currObj.getString("message");
                            String time = currObj.getString("time");

                            Message msgs = new Message(message, sender, time);
                            messages.add(msgs);
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
        } catch (UnsupportedEncodingException | JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.chat_block, menu);
        this.menu = menu;
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        if (id == R.id.menu_block) {
            blockUser();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void blockUser() {
        client = new AsyncHttpClient();
        JSONObject jsonObj = new JSONObject();

        try {
            jsonObj.put("user_number", user_num);
            jsonObj.put("to_user_number", receiver_num);
            jsonObj.put("token", token);
            jsonObj.put("action", action);
            StringEntity stringEntity = new StringEntity(jsonObj.toString());
            Log.d(TAG, "block user: " + jsonObj.toString());
            client.post(ChatActivity.this, Implementations.chatBlock, stringEntity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "block success: " + response);
                    item = menu.findItem(R.id.menu_block);
                    try {
                        JSONObject obj = new JSONObject(response);
                        String str = obj.getString("response");
                        if (str.equals("Account has been blocked")) {
                            Toast.makeText(ChatActivity.this, "User has been blocked", Toast.LENGTH_SHORT).show();
                            item.setTitle("UnBlock");
                            action = "unblock";
                            itemName = "Unblock";
                            handler.removeCallbacksAndMessages(null);
                        } else {
                            item.setTitle("Block");
                            action = "block";
                            itemName = "Block";
                            startCalling();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "block fail: " + response);
                }
            });
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem mItem = menu.findItem(R.id.menu_block);
        mItem.setTitle(itemName);

        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (threadOn)
            handler.removeCallbacksAndMessages(null);

        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (threadOn)
            handler.removeCallbacksAndMessages(null);
    }
}
