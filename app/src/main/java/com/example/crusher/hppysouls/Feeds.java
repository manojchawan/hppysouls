package com.example.crusher.hppysouls;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;

import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.crusher.hppysouls.chatLists.ChatList;
import com.example.crusher.hppysouls.chats.ChatActivity;
import com.example.crusher.hppysouls.favourite.Favourites;
import com.example.crusher.hppysouls.gangs.GangList;
import com.example.crusher.hppysouls.profile.Profile;
import com.example.crusher.hppysouls.profile.InterestAdapter;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
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

public class Feeds extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LocationListener {

    private static String TAG = Feeds.class.getSimpleName();

    Toolbar toolbar;
    LinearLayout drawer_pic;
    TextView user_name, user_age, drawer_name, drawer_gender, user_occupation, user_organization;
    ListView list;
    ImageView like, chat, cancel, profileImg;
    private SwipeRefreshLayout swipeContainer;
    Toast mToast;

    String[] degree = {"N/A"}, institute = {"N/A"};
    String[] tags = {"N/A"}, numbers, names, pics, orgs, age, occupations;
    String[][] interests, degrees, institutes;

    GridView gridView;
    InterestAdapter mAdapter;
    CustomList adapter;

    static int current;
    int count;
    private String token, user_num, user_preference;
    AsyncHttpClient client;
    NotificationCompat.Builder notification;
    int uniqueID;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notification = new NotificationCompat.Builder(Feeds.this);
        notification.setAutoCancel(true);

        profileImg = (ImageView) findViewById(R.id.profileImg);
        profileImg.setScaleType(ImageView.ScaleType.FIT_CENTER);
        user_name = (TextView) findViewById(R.id.user_name);
        user_age = (TextView) findViewById(R.id.user_age);
        user_occupation = (TextView) findViewById(R.id.user_occupation);
        user_organization = (TextView) findViewById(R.id.user_organization);
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        like = (ImageView) findViewById(R.id.like);
        chat = (ImageView) findViewById(R.id.comment);
        cancel = (ImageView) findViewById(R.id.cancel);

        // FOR INTEREST GRID VIEW
        gridView = (GridView) findViewById(R.id.grid_tags);
        mAdapter = new InterestAdapter(Feeds.this, tags);
        gridView.setAdapter(mAdapter);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.drawable.ic_logo);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        //FOR NAVIGATION DRAWER
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);

        drawer_name = (TextView) header.findViewById(R.id.drawer_name);
        drawer_gender = (TextView) header.findViewById(R.id.drawer_gender);
        drawer_pic = (LinearLayout) header.findViewById(R.id.drawer_img);

        Iconify.with(new FontAwesomeModule());

        list = (ListView) findViewById(R.id.education_list);
        adapter = new CustomList(Feeds.this, degree, institute);
        list.setAdapter(adapter);


      /*  //FOR GPS COORDINATES
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onCreate: Permissions");
            return;
        }
        // locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 10, 0, this);
*/
        SharedPreferences mPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
        String gender = mPreferences.getString("user_gender", getString(R.string.select));
        user_preference = mPreferences.getString("user_preference", getString(R.string.select));
        user_num = mPreferences.getString("user_number", "");
        String first_time = mPreferences.getString("first_time", "YES");
        String name = mPreferences.getString("user_name", "");
        String user_pic = mPreferences.getString("user_pic", "http://www.murketing.com/journal/wp-content/uploads/2009/04/vimeo.jpg");

        Implementations.user_num = user_num;
        token = AppFunctions.SHA1(AppFunctions.SHA1(user_num));

        if (gender.equals("Select") | user_preference.equals("Select") | TextUtils.isEmpty(user_preference)) {
            doProfileUpdate();
            return;
        } else {
            drawer_name.setText(name);
            drawer_gender.setText(gender);
            new LoadNewBackgroundImg(user_pic, "androidfigure", drawer_pic).execute();
            doSuggestAll();
        }

        startService(new Intent(this, MyService.class));

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


        /*if (first_time.equals("YES")) {
            new TapTargetSequence(this)
                    .targets(
                            TapTarget.forView(findViewById(R.id.like), "Like", " Click here to mark the person as favorite")
                                    .outerCircleColor(R.color.yellow)
                                    .targetCircleColor(R.color.black)
                                    .transparentTarget(true)
                                    .textColor(R.color.black),
                            TapTarget.forView(findViewById(R.id.comment), "Chat", "Click here to start Chatting & also checkout upper menus to scroll through other users.")
                                    .outerCircleColor(R.color.blue)
                                    .transparentTarget(true)
                                    .targetCircleColor(R.color.white)
                                    .textColor(R.color.white)).start();

            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putString("first_time", "NO");
            editor.apply();
        }
*/
    }

    public void doProfileUpdate() {
        Toast.makeText(Feeds.this, "Select Gender & Preference First", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(Feeds.this, Profile.class);
        startActivity(i);
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void doSuggestAll() {
        current = 0;
        client = new AsyncHttpClient();
        JSONObject obj = new JSONObject();
        try {
            obj.put("user_number", user_num);
            obj.put("token", token);
            obj.put("gender_preference", user_preference);
            StringEntity stringEntity = new StringEntity(obj.toString());
            Log.d(TAG, "doSuggestAll: " + obj.toString());

            final SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(Feeds.this, SweetAlertDialog.PROGRESS_TYPE);
            sweetAlertDialog.setTitleText("Loading");
            sweetAlertDialog.show();

            client.post(Feeds.this, Implementations.suggest_all, stringEntity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    sweetAlertDialog.cancel();
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "suggest onSuccess " + response);
                    try {
                        JSONObject resp = new JSONObject(response);
                        JSONObject respObj = resp.getJSONObject("match_number");

                        if (respObj.getJSONArray("user_number") == null) {
                            Toast.makeText(Feeds.this, "Oh Snap! Nobody's available now, Please come back after some time", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(Feeds.this, "Sorry nobody is available, pls come back after some time!", Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    // Now we call setRefreshing(false) to signal refresh has finished
                    swipeContainer.setRefreshing(false);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                                      byte[] responseBody, Throwable error) {
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
        Log.d(TAG, "Display Personal Info ");

        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hitLike(numbers[i]);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextMenu();
            }
        });

        final String n = names[i];
        final String pic = pics[i];
        String org = orgs[i];
        String occ = occupations[i];
        String a = age[i];


        mAdapter = new InterestAdapter(Feeds.this, interests[i]);
        gridView.setAdapter(mAdapter);

        adapter = new CustomList(Feeds.this, degrees[i], institutes[i]);
        list.setAdapter(adapter);

        user_name.setText(n);
        user_age.setText(a);
        user_organization.setText(org);
        user_occupation.setText(occ);
        Picasso.with(Feeds.this).load(pic).into(profileImg);
        // new LoadNewBackgroundImg(pic, "androidfigure", linearProfileImg).execute();

        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Implementations.chat_num = numbers[i];
                Implementations.user_num = user_num;

                Intent i = new Intent(Feeds.this, ChatActivity.class);
                i.putExtra("user_name", n);
                i.putExtra("user_pic", pic);
                startActivity(i);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void hitLike(String like_number) {
        client = new AsyncHttpClient();
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("user_number", user_num);
            jsonObj.put("token", token);
            jsonObj.put("like_number", like_number);
            StringEntity stringEntity = new StringEntity(jsonObj.toString());

            Log.d(TAG, "hitLike: " + jsonObj.toString());
            client.post(Feeds.this, Implementations.like_url, stringEntity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "like success: " + response);
                    Toast.makeText(Feeds.this, "Person Added to Favourites", Toast.LENGTH_SHORT).show();
                    nextMenu();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "like Fail: " + response);
                }
            });

        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void nextMenu() {
        if (current >= count - 1) {
            if (mToast != null)
                mToast.cancel();
            mToast = Toast.makeText(Feeds.this, "This is LAST Profile", Toast.LENGTH_SHORT);
            mToast.show();
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
            if (mToast != null)
                mToast.cancel();
            mToast = Toast.makeText(Feeds.this, "This is FIRST Profile", Toast.LENGTH_SHORT);
            mToast.show();
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
            Intent intent = new Intent(Feeds.this, ChatList.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_gang) {
            Log.d(TAG, "onNavigationItemSelected: " + id);
            Intent intent = new Intent(Feeds.this, GangList.class);
            startActivity(intent);
        } else if (id == R.id.nav_favorites) {
            Intent intent = new Intent(Feeds.this, Favourites.class);
            startActivity(intent);
        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent(Feeds.this, Profile.class);
            startActivity(intent);
        } else if (id == R.id.nav_share) {
            Intent intent = new Intent(Feeds.this, Share.class);
            startActivity(intent);
        } else if (id == R.id.nav_support) {
            Intent intent = new Intent(Feeds.this, SupportActivity.class);
            startActivity(intent);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged: " + location.getLatitude());
      /*  latitude = Double.toString(location.getLatitude());
        longitude = Double.toString(location.getLongitude());*/
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, " status " + provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "Location Provider enable " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "Location disable " + provider);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("GPS settings");
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        alertDialog.setPositiveButton("Favourites", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }
}