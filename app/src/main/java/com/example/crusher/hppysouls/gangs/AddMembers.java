package com.example.crusher.hppysouls.gangs;

import android.content.DialogInterface;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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

public class AddMembers extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private static final String TAG = AddMembers.class.getSimpleName();

    String gang_id, gang_name, gang_type;
    private ChatListAdapter mAdapter;
    String[] mName, numbers;
    final ArrayList<ChatListItem> lists = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_members);
        setTitle("Contacts");

        gang_id = Implementations.gang_id;
        gang_name = Implementations.gang_name;
        gang_type = Implementations.gang_type;

        ListView userList = (ListView) findViewById(R.id.chatlist_add);
        mAdapter = new ChatListAdapter(this, new ArrayList<ChatListItem>());
        userList.setAdapter(mAdapter);

        getUserList();

        userList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                displayAction(numbers[position], mName[position]);
            }
        });

        userList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                displayAction(numbers[position], mName[position]);
                //   getUserList();
                return true;
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void displayAction(final String number, final String name) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(AddMembers.this);
        alertDialog.setTitle("Add User");
        alertDialog.setMessage("Do you want to Add " + name + " in " + gang_name + " ?");

        alertDialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addMember(number, name);
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
    private void addMember(String number, final String name) {
        AsyncHttpClient client = new AsyncHttpClient();
        JSONObject jsonObj = new JSONObject();
        String token = AppFunctions.SHA1(AppFunctions.SHA1(gang_id));
        try {
            jsonObj.put("group_id", gang_id);
            jsonObj.put("group_name", gang_name);
            jsonObj.put("group_type", gang_type);
            jsonObj.put("token", token);
            jsonObj.put("group_member", number);
            StringEntity stringEntity = new StringEntity(jsonObj.toString());
            Log.d(TAG, "add Member" + jsonObj.toString());

            client.post(AddMembers.this, Implementations.add_gang_member, stringEntity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "add member success " + response);
                    try {
                        JSONObject respObj = new JSONObject(response);
                        String res = respObj.getString("response");
                        if (res.equals("OK"))
                            Toast.makeText(AddMembers.this, name + " has been added to " + gang_name, Toast.LENGTH_LONG).show();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "add member Failure " + response);
                }
            });

        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void getUserList() {

        AsyncHttpClient client = new AsyncHttpClient();
        JSONObject jsonObj = new JSONObject();
        String token = AppFunctions.SHA1(AppFunctions.SHA1(Implementations.user_num));
        try {
            jsonObj.put("user_number", Implementations.user_num);
            jsonObj.put("token", token);
            StringEntity stringEntity = new StringEntity(jsonObj.toString());
            Log.d(TAG, "get userlist" + jsonObj.toString());

            client.post(AddMembers.this, Implementations.fetch_favorites, stringEntity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "userlist Success " + response);
                    try {
                        JSONObject respObj = new JSONObject(response);
                        JSONArray msgArr = respObj.getJSONArray("group_list");
                        int len = msgArr.length();
                        mName = new String[len];
                        numbers = new String[len];
                        for (int i = 0; i < len; i++) {
                            JSONObject currObj = msgArr.getJSONObject(i);
                            String name = currObj.getString("user_name");
                            String num = currObj.getString("like_number");
                            String pic = currObj.getString("user_profile");
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_member, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        newText = newText.toLowerCase();
        ArrayList<ChatListItem> newList = new ArrayList<>();
        int i = 0;

        for (ChatListItem item : lists) {
            String name = item.getmName().toLowerCase();
            String num = item.getmNumber();
            if (name.contains(newText)) {
                newList.add(item);
                mName[i] = name;
                numbers[i] = num;
                i++;
            }
        }
        mAdapter.clear();
        mAdapter.addAll(newList);

        //mAdapter.setFilter(newList);

        return true;
    }
}
