package com.example.crusher.hppysouls.chatLists;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.crusher.hppysouls.AppFunctions;
import com.example.crusher.hppysouls.Feeds;
import com.example.crusher.hppysouls.Implementations;
import com.example.crusher.hppysouls.R;
import com.example.crusher.hppysouls.chats.ChatActivity;
import com.example.crusher.hppysouls.gangs.GangList;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class ChatList extends AppCompatActivity {

    private ChatListAdapter mAdapter;
    String[] mName;
    String[] numbers;
    String[] pics;
    NotificationCompat.Builder notification;
    int uniqueID;

    private String TAG = ChatList.class.getSimpleName();

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);
        setTitle("Chats");

        notification = new NotificationCompat.Builder(ChatList.this);
        notification.setAutoCancel(true);

        View emptyView = findViewById(R.id.empty_chat_list);
        ListView chatlist = (ListView) findViewById(R.id.chatlist);
        chatlist.setEmptyView(emptyView);
        mAdapter = new ChatListAdapter(this, new ArrayList<ChatListItem>());
        chatlist.setAdapter(mAdapter);

        getChatList();

        chatlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ChatList.this, ChatActivity.class);
               // Implementations.chat_num = numbers[position];
                intent.putExtra("chat_num", numbers[position]);
                intent.putExtra("user_name", mName[position]);
                intent.putExtra("user_pic", pics[position]);
                startActivity(intent);
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void getChatList() {
        //Log.d(TAG, "getChat: ");
        final List<ChatListItem> chats = new ArrayList<>();
        String token = AppFunctions.SHA1(AppFunctions.SHA1(Implementations.user_num));
        AsyncHttpClient client = new AsyncHttpClient();
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("user_number", Implementations.user_num);
            jsonObj.put("token", token);
            StringEntity stringEntity = new StringEntity(jsonObj.toString());
            //  Log.d(TAG, "get chat" + jsonObj.toString());
            client.post(ChatList.this, Implementations.chat_list_url, stringEntity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "chatlist Success " + response);
                    try {

                        JSONObject respObj = new JSONObject(response);
                        JSONArray msgArr = respObj.getJSONArray("chat_list");
                        mName = new String[msgArr.length()];
                        numbers = new String[msgArr.length()];
                        pics = new String[msgArr.length()];
                        for (int i = 0; i < msgArr.length(); i++) {
                            JSONObject currObj = msgArr.getJSONObject(i);
                            String name = currObj.getString("user_name");
                            String num = currObj.getString("user_number");
                            String pic = currObj.getString("user_profile");

                            ChatListItem obj = new ChatListItem(name, pic, num);
                            chats.add(obj);

                            mName[i] = name;
                            numbers[i] = num;
                            pics[i] = pic;
                        }
                        mAdapter.clear();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mAdapter.addAll(chats);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "chatlist Failure " + response);
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

                notification.setSmallIcon(R.drawable.ic_logo);
                notification.setTicker(title);
                notification.setWhen(System.currentTimeMillis());
                notification.setContentTitle(title);
                notification.setContentText(desc);
                Intent intent = new Intent(ChatList.this, Feeds.class);
                if (type.equals("chat")) {
                    intent = new Intent(ChatList.this, ChatList.class);
                } else if (type.equals("group")) {
                    intent = new Intent(ChatList.this, GangList.class);
                }
                PendingIntent pendingIntent = PendingIntent.getActivity(ChatList.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                notification.setContentIntent(pendingIntent);

                //BUILDS NOTIFICATION AND ISSUE IT
                NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                nm.notify(uniqueID,notification.build());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
