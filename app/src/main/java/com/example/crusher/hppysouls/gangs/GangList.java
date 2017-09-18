package com.example.crusher.hppysouls.gangs;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

public class GangList extends AppCompatActivity {

    private static final String TAG = GangList.class.getSimpleName();
    GangListAdapter adapter;
    String[] gangId, gangName, gangType;
    NotificationCompat.Builder notification;
    int uniqueID;
    String user_num;


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gang_list);
        setTitle("Gangs");
        notification = new NotificationCompat.Builder(GangList.this);
        notification.setAutoCancel(true);

        SharedPreferences mPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
        user_num = mPreferences.getString("user_number", "");

        View emptyView = findViewById(R.id.empty_Gang_list);
        ListView list = (ListView) findViewById(R.id.gangListView);
        list.setEmptyView(emptyView);

        adapter = new GangListAdapter(this, new ArrayList<GangItem>());
        list.setAdapter(adapter);

        getGangList();

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Log.d(TAG, "onItemClick: " + gangId[position] + " NAME: " + gangName[position]);
                Intent intent = new Intent(GangList.this, GangChatActivity.class);
                intent.putExtra("gang_id", gangId[position]);
                intent.putExtra("gang_name", gangName[position]);
                intent.putExtra("user_num", user_num);
                startActivity(intent);
            }
        });

        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                displayAction(gangId[position], gangName[position]);
                return true;
            }
        });


    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void getGangList() {
        final List<GangItem> gangs = new ArrayList<>();
        String token = AppFunctions.SHA1(AppFunctions.SHA1(Implementations.user_num));
        AsyncHttpClient client = new AsyncHttpClient();
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("user_number", user_num);
            jsonObj.put("token", token);
            StringEntity stringEntity = new StringEntity(jsonObj.toString());

            client.post(GangList.this, Implementations.gang_list_url, stringEntity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "ganglist Success " + response);
                    try {
                        JSONObject respObj = new JSONObject(response);

                        JSONArray msgArr = respObj.getJSONArray("group_list");
                        gangId = new String[msgArr.length()];
                        gangName = new String[msgArr.length()];
                        gangType = new String[msgArr.length()];

                        for (int i = 0; i < msgArr.length(); i++) {
                            JSONObject currObj = msgArr.getJSONObject(i);
                            String id = currObj.getString("group_id");

                            if (id.equals("false")){
                                adapter.clear();
                                return;
                            }

                            String name = currObj.getString("group_name");
                            String type = currObj.getString("group_type");
                            GangItem obj = new GangItem(name, type, id);
                            gangs.add(obj);
                            gangId[i] = id;
                            gangType[i] = type;
                            gangName[i] = name;
                        }
                        adapter.clear();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    adapter.addAll(gangs);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "ganglist Failed " + response);

                }
            });

        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void displayAction(final String id, String gangName) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(GangList.this);
        alertDialog.setTitle("Delete Gang");
        alertDialog.setMessage("Do you want to Delete " + gangName + " from List ?");

        alertDialog.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteGang(id);
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
    private void deleteGang(String gangid){
        AsyncHttpClient client = new AsyncHttpClient();
        JSONObject jsonObj = new JSONObject();
        try{
            jsonObj.put("group_id", gangid);
            jsonObj.put("token", AppFunctions.SHA1(AppFunctions.SHA1(gangid)));
            StringEntity stringEntity = new StringEntity(jsonObj.toString());
            Log.d(TAG, "delete Gang" + jsonObj.toString());

            client.post(GangList.this, Implementations.group_delete, stringEntity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "delete success " + response);
                    Toast.makeText(GangList.this,"Group Deleted",Toast.LENGTH_SHORT).show();
                    getGangList();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "delete failure " + response);
                }
            });
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.gang_list, menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        if (id == R.id.action_addGang) {
            Intent intent = new Intent(GangList.this, AddGang.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
