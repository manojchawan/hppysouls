package com.example.crusher.hppysouls.favourite;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.crusher.hppysouls.AppFunctions;
import com.example.crusher.hppysouls.Feeds;
import com.example.crusher.hppysouls.Implementations;
import com.example.crusher.hppysouls.R;
import com.example.crusher.hppysouls.chatLists.ChatList;
import com.example.crusher.hppysouls.chatLists.ChatListAdapter;
import com.example.crusher.hppysouls.chatLists.ChatListItem;
import com.example.crusher.hppysouls.gangs.GangList;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class Favourites extends AppCompatActivity {

    private static final String TAG = Favourites.class.getSimpleName();

    String[] tags = {"N/A"}, numbers, names, pics, orgs, age, occupations;
    String[][] interests, degrees, institutes;
    static int current;
    int count, uniqueID;
    private String token, user_num;
    final ArrayList<ChatListItem> lists = new ArrayList<>();
    private ChatListAdapter mAdapter;


    private SwipeRefreshLayout swipeContainer;
    AsyncHttpClient client;
    NotificationCompat.Builder notification;

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setTitle("Favourites");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        notification = new NotificationCompat.Builder(Favourites.this);
        notification.setAutoCancel(true);
        user_num = Implementations.user_num;
        token = AppFunctions.SHA1(AppFunctions.SHA1(user_num));

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainerFavorites);

        View emptyView = findViewById(R.id.empty_favView);
        ListView userList = (ListView) findViewById(R.id.favorite_list);
        userList.setEmptyView(emptyView);
        mAdapter = new ChatListAdapter(this, new ArrayList<ChatListItem>());
        userList.setAdapter(mAdapter);

        doSuggestAll();

        userList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(Favourites.this, UserProfile.class);
                intent.putExtra("position", position);
                startActivity(intent);
                Log.d(TAG, "onItemClick: "+position+" name: "+names[position]);
            }
        });

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                doSuggestAll();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void doSuggestAll() {
        current = 0;
        client = new AsyncHttpClient();
        JSONObject obj = new JSONObject();
        try {
            obj.put("user_number", user_num);
            obj.put("token", token);
            StringEntity stringEntity = new StringEntity(obj.toString());
            Log.d(TAG, "liked users: " + obj.toString());

            final SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(Favourites.this, SweetAlertDialog.PROGRESS_TYPE);
            sweetAlertDialog.setTitleText("Loading");
            sweetAlertDialog.show();

            client.post(Favourites.this, Implementations.liked_user, stringEntity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    sweetAlertDialog.cancel();
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "onSuccess-Response " + response);
                    try {
                        JSONObject resp = new JSONObject(response);
                        JSONObject respObj = resp.getJSONObject("match_number");

                        if (respObj.getJSONArray("user_number") == null) {
                            Toast.makeText(Favourites.this, "Oh Snap! Nobody's available now, Please come back after some time", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        JSONArray numArr = respObj.getJSONArray("user_number");
                        JSONArray nameArr = respObj.getJSONArray("user_name");
                        JSONArray picArr = respObj.getJSONArray("user_profile");
                        JSONArray interestArr = respObj.getJSONArray("interests");
                        JSONArray orgArr = respObj.getJSONArray("organization");
                        JSONArray ageArr = respObj.getJSONArray("user_age");
                        JSONArray eduArr = respObj.getJSONArray("education");

                        count = numArr.length();

                        numbers = new String[count];
                        names = new String[count];
                        pics = new String[count];
                        age = new String[count];
                        orgs = new String[count];
                        occupations = new String[count];
                        interests = new String[count][];
                        degrees = new String[count][];
                        institutes = new String[count][];

                        // FOR USER DETAILS
                        for (int i = 0; i < count; i++) {
                            numbers[i] = numArr.optString(i);
                            names[i] = nameArr.optString(i);
                            pics[i] = picArr.optString(i);
                            age[i] = ageArr.optString(i);

                            ChatListItem obj = new ChatListItem(names[i], pics[i], numbers[i]);
                            lists.add(obj);
                        }

                        // FOR OCCUPATION AND ORG
                        for (int i = 0; i < count; i++) {
                            JSONObject org = orgArr.getJSONObject(i);
                            String str = org.getString("organization_name");
                            String occ = org.getString("user_occupation");

                            if (TextUtils.isEmpty(str) | str.equals("null"))
                                orgs[i] = "N/A";
                            else
                                orgs[i] = str;

                            if (TextUtils.isEmpty(occ) | occ.equals("null"))
                                occupations[i] = "N/A";
                            else
                                occupations[i] = occ;
                        }

                        //FOR INTERESTS
                        for (int i = 0; i < count; i++) {
                            JSONArray tags = interestArr.getJSONArray(i);
                            int c = tags.length();
                            if (c == 0) {
                                interests[i] = new String[1];
                                interests[i][0] = "N/A";
                            } else {
                                interests[i] = new String[c];
                                for (int j = 0; j < c; j++) {
                                    JSONObject obj = tags.getJSONObject(j);
                                    String tag = obj.getString("interest");
                                    interests[i][j] = tag;
                                }
                            }
                        }
                        //EDUCATION DETAILS
                        for (int i = 0; i < count; i++) {
                            JSONArray myArr = eduArr.getJSONArray(i);
                            int c = myArr.length();
                            Log.d(TAG, "EDUCATION: " + c);
                            if (c == 0) {
                                degrees[i] = new String[1];
                                institutes[i] = new String[1];
                                degrees[i][0] = "N/A";
                                institutes[i][0] = "N/A";
                            } else {
                                degrees[i] = new String[c];
                                institutes[i] = new String[c];
                                for (int j = 0; j < c; j++) {
                                    JSONObject obj = myArr.getJSONObject(j);
                                    degrees[i][j] = obj.getString("education");
                                    institutes[i][j] = obj.getString("institute_name");
                                }
                            }
                        }


                        if (numbers.length > 0) {
                            mAdapter.clear();
                            mAdapter.addAll(lists);
                        } else {
                            Toast.makeText(Favourites.this, "Sorry nobody is available, pls come back after some time!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    swipeContainer.setRefreshing(false);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    sweetAlertDialog.cancel();
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "onFailure: " + response);
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
                Intent intent = new Intent(Favourites.this, Feeds.class);
                if (type.equals("chat")) {
                    intent = new Intent(Favourites.this, ChatList.class);
                } else if (type.equals("group")) {
                    intent = new Intent(Favourites.this, GangList.class);
                }
                PendingIntent pendingIntent = PendingIntent.getActivity(Favourites.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                notification.setContentIntent(pendingIntent);

                //BUILDS NOTIFICATION AND ISSUE IT
                NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                nm.notify(uniqueID, notification.build());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.feeds, menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_chatList) {
            Intent intent = new Intent(Favourites.this, ChatList.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
