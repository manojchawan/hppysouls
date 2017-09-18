package com.example.crusher.hppysouls.gangs;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.crusher.hppysouls.AppFunctions;
import com.example.crusher.hppysouls.Implementations;
import com.example.crusher.hppysouls.R;
import com.example.crusher.hppysouls.chatLists.ChatListAdapter;
import com.example.crusher.hppysouls.chatLists.ChatListItem;
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

public class GangSetting extends AppCompatActivity {

    private static final String TAG = GangSetting.class.getSimpleName();
    private ChatListAdapter mAdapter;
    String[] mName, numbers;
    String gangid = Implementations.gang_id;
    String gangName = Implementations.gang_name;
    String user_num = Implementations.user_num;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gang_setting);

        setTitle(gangName);

        ListView userList = (ListView) findViewById(R.id.user_list);
        mAdapter = new ChatListAdapter(this, new ArrayList<ChatListItem>());
        userList.setAdapter(mAdapter);

        Button Leave_Grp = (Button) findViewById(R.id.leave_btn);

        Leave_Grp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(GangSetting.this);
                alertDialog.setTitle("Leave Group");
                alertDialog.setMessage("Do you want to Leave This Group ?");

                alertDialog.setPositiveButton("Leave", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        leaveGroup(user_num);
                    }
                });

                // on pressing cancel button
                alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertDialog.show();
            }
        });

        getUserList();

        userList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (!numbers[position].equals(Implementations.user_num)) {
                    displayAction(numbers[position], mName[position]);
                }
                //   getUserList();
                return true;
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void leaveGroup(String user_num) {
        AsyncHttpClient client = new AsyncHttpClient();
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("group_member", user_num);
            jsonObj.put("group_id", gangid);
            jsonObj.put("token", AppFunctions.SHA1(AppFunctions.SHA1(gangid)));
            StringEntity stringEntity = new StringEntity(jsonObj.toString());
            Log.d(TAG, "Leave Group " + jsonObj.toString());
            client.post(GangSetting.this, Implementations.group_leave, stringEntity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "Leave success " + response);
                    Toast.makeText(GangSetting.this, "Group " + gangName + " Left Successfully", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(GangSetting.this, GangList.class);
                    startActivity(i);
                    finish();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "Leave grp Failure " + response);
                }
            });

        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void addmember(View v) {
        Intent i = new Intent(this, AddMembers.class);
        startActivity(i);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void displayAction(final String number, String name) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(GangSetting.this);
        alertDialog.setTitle("Remove User");
        alertDialog.setMessage("Do you want to Remove " + name + " from Group ?");

        alertDialog.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeFromGroup(number);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void removeFromGroup(String number) {
        AsyncHttpClient client = new AsyncHttpClient();
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("group_member", number);
            jsonObj.put("group_id", gangid);
            jsonObj.put("token", AppFunctions.SHA1(AppFunctions.SHA1(gangid)));
            StringEntity stringEntity = new StringEntity(jsonObj.toString());
            Log.d(TAG, "remove user" + jsonObj.toString());

            client.post(GangSetting.this, Implementations.group_leave, stringEntity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "remove success " + response);
                    getUserList();
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void getUserList() {

        final List<ChatListItem> lists = new ArrayList<>();
        AsyncHttpClient client = new AsyncHttpClient();
        JSONObject jsonObj = new JSONObject();
        String token = AppFunctions.SHA1(AppFunctions.SHA1(Implementations.user_num));
        try {
            jsonObj.put("group_id", Implementations.gang_id);
            jsonObj.put("token", token);
            StringEntity stringEntity = new StringEntity(jsonObj.toString());
            Log.d(TAG, "get userlist" + jsonObj.toString());

            client.post(GangSetting.this, Implementations.group_members, stringEntity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "userlist Success " + response);
                    try {
                        JSONObject respObj = new JSONObject(response);
                        JSONArray msgArr = respObj.getJSONArray("members");
                        int len = msgArr.length();
                        mName = new String[len];
                        numbers = new String[len];

                        for (int i = 0; i < len; i++) {
                            JSONObject currObj = msgArr.getJSONObject(i);
                            String name = currObj.getString("member_name");
                            String num = currObj.getString("member_number");
                            String pic = currObj.getString("member_profile");
                            ChatListItem obj = new ChatListItem(name, pic, num);
                            lists.add(obj);
                            mName[i] = name;
                            numbers[i] = num;
                        }
                        mAdapter.clear();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mAdapter.addAll(lists);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "userlist Failure " + response);
                }
            });

        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
