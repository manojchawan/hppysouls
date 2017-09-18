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
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.crusher.hppysouls.AppFunctions;
import com.example.crusher.hppysouls.CustomList;
import com.example.crusher.hppysouls.Feeds;
import com.example.crusher.hppysouls.Implementations;
import com.example.crusher.hppysouls.R;
import com.example.crusher.hppysouls.chatLists.ChatList;
import com.example.crusher.hppysouls.chats.ChatActivity;
import com.example.crusher.hppysouls.gangs.GangList;
import com.example.crusher.hppysouls.profile.InterestAdapter;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import cn.pedant.SweetAlert.SweetAlertDialog;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class UserProfile extends AppCompatActivity {
    private static final String TAG = UserProfile.class.getSimpleName();
    TextView user_name, user_age, user_occupation, user_organization;
    ImageView chat, profileImg;
    private SwipeRefreshLayout swipeContainer;

    ListView list;
    String[] degree = {"N/A"};
    String[] institute = {"N/A"};
    String[] tags = {"N/A"}, numbers, names, pics, orgs, age, occupations;
    String[][] interests, degrees, institutes;

    GridView gridView;
    InterestAdapter mAdapter;
    CustomList adapter;

    static int current;
    int count;
    private String token, user_num;
    AsyncHttpClient client;
    NotificationCompat.Builder notification;
    int uniqueID;

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        notification = new NotificationCompat.Builder(UserProfile.this);
        notification.setAutoCancel(true);

        Intent i = getIntent();
        Bundle b = i.getExtras();
        if (b != null) {
            current = b.getInt("position");
            Log.d(TAG, "onCreate: "+current);
        }

        profileImg = (ImageView) findViewById(R.id.profileImg);
        profileImg.setScaleType(ImageView.ScaleType.FIT_CENTER);
        user_name = (TextView) findViewById(R.id.user_name);
        user_age = (TextView) findViewById(R.id.user_age);
        user_occupation = (TextView) findViewById(R.id.user_occupation);
        user_organization = (TextView) findViewById(R.id.user_organization);
        chat = (ImageView) findViewById(R.id.comment);
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainerUser);

        // FOR INTEREST GRID VIEW
        gridView = (GridView) findViewById(R.id.grid_tags);
        mAdapter = new InterestAdapter(UserProfile.this, tags);
        gridView.setAdapter(mAdapter);

        user_num = Implementations.user_num;
        token = AppFunctions.SHA1(AppFunctions.SHA1(user_num));

        // FOR EDUCATION LIST
        list = (ListView) findViewById(R.id.education_list);
        adapter = new CustomList(UserProfile.this, degree, institute);
        list.setAdapter(adapter);

        doSuggestAll();

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
        client = new AsyncHttpClient();
        JSONObject obj = new JSONObject();
        try {
            obj.put("user_number", user_num);
            obj.put("token", token);
            StringEntity stringEntity = new StringEntity(obj.toString());
            Log.d(TAG, "liked users: " + obj.toString());

            final SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(UserProfile.this, SweetAlertDialog.PROGRESS_TYPE);
            sweetAlertDialog.setTitleText("Loading");
            sweetAlertDialog.show();

            client.post(UserProfile.this, Implementations.liked_user, stringEntity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    sweetAlertDialog.cancel();
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "onSuccess-Response " + response);
                    try {
                        JSONObject resp = new JSONObject(response);
                        JSONObject respObj = resp.getJSONObject("match_number");

                        if (respObj.getJSONArray("user_number") == null) {
                            Toast.makeText(UserProfile.this, "Oh Snap! Nobody's available now, Please come back after some time", Toast.LENGTH_SHORT).show();
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

                        // FOR OCCUPATION AND ORG
                        for (int i = 0; i < count; i++) {
                            numbers[i] = numArr.optString(i);
                            names[i] = nameArr.optString(i);
                            pics[i] = picArr.optString(i);
                            age[i] = ageArr.optString(i);

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
                            displayPerson(current);
                        } else {
                            Toast.makeText(UserProfile.this, "Sorry nobody is available, pls come back after some time!", Toast.LENGTH_SHORT).show();
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void displayPerson(final int i) {

        Log.d(TAG, "Display Info ");

        final String n = names[i];
        final String pic = pics[i];
        String org = orgs[i];
        String occ = occupations[i];
        String a = age[i];

        mAdapter = new InterestAdapter(UserProfile.this, interests[i]);
        gridView.setAdapter(mAdapter);

        adapter = new CustomList(UserProfile.this, degrees[i], institutes[i]);
        list.setAdapter(adapter);

        user_name.setText(n);
        user_age.setText(a);
        user_organization.setText(org);
        user_occupation.setText(occ);
        Picasso.with(UserProfile.this).load(pic).into(profileImg);
        //  new LoadNewBackgroundImg(pic, "androidfigure", linearProfileImg).execute();

        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Implementations.chat_num = numbers[i];
                Implementations.user_num = user_num;

                Intent i = new Intent(UserProfile.this, ChatActivity.class);
                i.putExtra("user_name", n);
                i.putExtra("user_pic", pic);
                startActivity(i);
            }
        });

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
                Intent intent = new Intent(UserProfile.this, Feeds.class);
                if (type.equals("chat")) {
                    intent = new Intent(UserProfile.this, ChatList.class);
                } else if (type.equals("group")) {
                    intent = new Intent(UserProfile.this, GangList.class);
                }
                PendingIntent pendingIntent = PendingIntent.getActivity(UserProfile.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                notification.setContentIntent(pendingIntent);

                //BUILDS NOTIFICATION AND ISSUE IT
                NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                nm.notify(uniqueID, notification.build());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void nextMenu() {
        if (current >= count - 1) {
            Log.d("menu Next: ", "Current: " + current);
            Toast.makeText(UserProfile.this, "This is Last Profile", Toast.LENGTH_SHORT).show();
        } else {
            current++;
            Log.d("menu Next: ", "Current: " + current);
            displayPerson(current);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void prevMenu() {
        if (current <= 0) {
            Log.d("menu Previous: ", "Current: " + current);
            Toast.makeText(UserProfile.this, "This is Last Profile", Toast.LENGTH_SHORT).show();
        } else {
            current--;
            Log.d("menu Previous: ", "Current: " + current);
            displayPerson(current);
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
            Intent intent = new Intent(UserProfile.this, ChatList.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

}
