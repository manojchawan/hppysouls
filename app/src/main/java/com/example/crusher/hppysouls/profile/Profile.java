package com.example.crusher.hppysouls.profile;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.example.crusher.hppysouls.AppFunctions;
import com.example.crusher.hppysouls.Feeds;
import com.example.crusher.hppysouls.Implementations;
import com.example.crusher.hppysouls.R;
import com.example.crusher.hppysouls.chatLists.ChatList;
import com.example.crusher.hppysouls.gangs.GangList;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import de.hdodenhof.circleimageview.CircleImageView;
import mehdi.sakout.fancybuttons.FancyButton;

public class Profile extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = Profile.class.getSimpleName();

    Float x1, x2;
    static final int MIN_DISTANCE = 150;
    LinearLayout slider;
    ListView eduList;
    GridView gridView;
    String[] tags = {"Your Interests"}, tagId, edu, insti, eduId;
    ProfileInterestAdapter mAdapter;
    FancyButton addBtn;

    private EducationAdapter eduAdapter;
    private MaterialSpinner mGenSpinner, mGenPreference;
    private EditText Age, Name, interest, Organization, Occupation, Degree, Institute;
    private CircleImageView user_pic;
    private int PICK_IMAGE_REQUEST = 1;
    private String mGender, mName, mNum, mAge, mPrefer, mPic, mOccupation, mOrg, token;
    private boolean flag = false;
    private Bitmap bitmap;
    private String KEY_IMAGE = "image";
    private String KEY_NAME = "name";
    private String KEY_NUMBER = "user_number";

    Uri selectedImage;
    NotificationCompat.Builder notification;
    int uniqueID;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                float deltaX = x2 - x1;

                if (Math.abs(deltaX) > MIN_DISTANCE) {
                    // Left to Right swipe action
                    if (x2 > x1) {
//                        Toast.makeText(this, "Left to Right swipe [Next]", Toast.LENGTH_SHORT).show ();
                        slider.setBackgroundResource(R.drawable.back_test_2);
                        YoYo.with(Techniques.BounceInLeft).duration(200).playOn(slider);
                    }

                    // Right to left swipe action
                    else {
                        slider.setBackgroundResource(R.drawable.back_test);
                        YoYo.with(Techniques.BounceInRight).duration(200).playOn(slider);

                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        setTitle("Profile");
        slider = (LinearLayout) findViewById(R.id.slider);
        notification = new NotificationCompat.Builder(Profile.this);
        notification.setAutoCancel(true);

        user_pic = (CircleImageView) findViewById(R.id.user_pic);
        mGenSpinner = (MaterialSpinner) findViewById(R.id.spinner_gender);
        mGenPreference = (MaterialSpinner) findViewById(R.id.spinner_preference);
        Name = (EditText) findViewById(R.id.user_name_editText);
        Age = (EditText) findViewById(R.id.age_editText);
        Organization = (EditText) findViewById(R.id.org_editText);
        Occupation = (EditText) findViewById(R.id.occupation_editText);

        // TO DISPLAY EDUCATION LIST
        Button edu_Btn = (Button) findViewById(R.id.edu_btn);
        Degree = (EditText) findViewById(R.id.edu_degree);
        Institute = (EditText) findViewById(R.id.edu_institute);

        eduList = (ListView) findViewById(R.id.edu_list);
        eduAdapter = new EducationAdapter(this, new ArrayList<Education>());
        eduList.setAdapter(eduAdapter);


        SharedPreferences mPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
        mName = mPreferences.getString("user_name", "");
        mAge = mPreferences.getString("user_age", "");
        mPic = mPreferences.getString("user_pic", "http://www.murketing.com/journal/wp-content/uploads/2009/04/vimeo.jpg");
        mGender = mPreferences.getString("user_gender", getString(R.string.select));
        mPrefer = mPreferences.getString("user_preference", getString(R.string.select));
        mNum = mPreferences.getString("user_number", "");
        mOccupation = mPreferences.getString("user_occupation", "");
        mOrg = mPreferences.getString("user_organization", "");
        token = AppFunctions.SHA1(AppFunctions.SHA1(mNum));

        Name.setText(mName);
        Age.setText(mAge);
        Organization.setText(mOrg);
        Occupation.setText(mOccupation);
        Picasso.with(Profile.this).load(mPic).into(user_pic);

        interest = (EditText) findViewById(R.id.interest);
        interest.setVisibility(View.INVISIBLE);
        addBtn = (FancyButton) findViewById(R.id.addBtn);
        gridView = (GridView) findViewById(R.id.Grid1);
        mAdapter = new ProfileInterestAdapter(Profile.this, new ArrayList<InterestItem>());
        gridView.setAdapter(mAdapter);

        setupSpinner();
        getEducation();
        interestFetch();
        user_pic.setOnClickListener(this);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewInterest();
            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(Profile.this, "hello", Toast.LENGTH_SHORT).show();
            }
        });

        edu_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addEducation();
            }
        });

        eduList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                deleteEdu(eduId[position]);
                return true;
            }
        });

    }

    private void setupSpinner() {

        mGenSpinner.setItems("Select", "Male", "Female", "Other");

        mGenPreference.setItems("Select", "Male", "Female", "Other");

        switch (mGender) {
            case "Male":
            case "male":
                mGenSpinner.setSelectedIndex(1);
                break;
            case "Female":
            case "female":
                mGenSpinner.setSelectedIndex(2);
                break;
            case "Other":
            case "other":
                mGenSpinner.setSelectedIndex(3);
                break;
            default:
                mGenSpinner.setSelectedIndex(0);
                break;
        }

        mGenSpinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                mGender = item.toString();
            }
        });

        switch (mPrefer) {
            case "Male":
                mGenPreference.setSelectedIndex(1);
                break;
            case "Female":
                mGenPreference.setSelectedIndex(2);
                break;
            case "Other":
                mGenPreference.setSelectedIndex(3);
                break;
            default:
                mGenPreference.setSelectedIndex(0);
                break;
        }

        mGenPreference.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                mPrefer = item.toString();
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void updateProfile() {

        mName = Name.getText().toString().trim();
        mAge = Age.getText().toString().trim();
        mOrg = Organization.getText().toString().trim();
        mOccupation = Occupation.getText().toString().trim();
        if (TextUtils.isEmpty(mAge)) {
            mAge = " ";
        }


        if (mGender.equals("Select")) {
            YoYo.with(Techniques.Tada).playOn(mGenSpinner);
            Toast.makeText(Profile.this, "Select Gender", Toast.LENGTH_SHORT).show();
        } else if (mPrefer.equals("Select")) {
            YoYo.with(Techniques.Tada).playOn(mGenPreference);
            Toast.makeText(Profile.this, "Select Gender Preference", Toast.LENGTH_SHORT).show();
            // Snackbar.make(v, "Please Select Your Gender Preference First", Snackbar.LENGTH_SHORT).show();
        } else {
            //Log.d(TAG, "update: NUM: " + mNum + "Gen- " + mGender + " prefer- " + mPrefer + " Age- " + mAge + " Name- " + mName + " OCCUPATION: " + mOccupation + " ORG: " + mOrg+" PIC: " + mPic);
            Log.d(TAG, mPic);
            updateProcess();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void updateProcess() {
        JSONObject jsonObj = new JSONObject();
        AsyncHttpClient client = new AsyncHttpClient();

        try {
            jsonObj.put("user_number", mNum);
            jsonObj.put("user_name", mName);
            jsonObj.put("user_age", mAge);
            jsonObj.put("user_gender", mGender);
            jsonObj.put("user_occupation", mOccupation);
            jsonObj.put("user_organization", mOrg);
            jsonObj.put("token", token);
            StringEntity stringEntity = new StringEntity(jsonObj.toString());
            Log.d(TAG, "Update-Person: " + jsonObj.toString());

            client.post(Profile.this, Implementations.update_profile, stringEntity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "update success " + response);
                    SharedPreferences mPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
                    SharedPreferences.Editor editor = mPreferences.edit();
                    editor.putString("user_name", mName);
                    editor.putString("user_age", mAge);
                    editor.putString("user_gender", mGender);
                    editor.putString("user_preference", mPrefer);
                    editor.putString("user_occupation", mOccupation);
                    editor.putString("user_organization", mOrg);
                    editor.apply();

                    Intent i = new Intent(Profile.this, Feeds.class);
                    startActivity(i);
                    finish();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "update fail " + response);
                }
            });

        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == user_pic) {
            showFileChooser();
        }
    }

    private void showFileChooser() {
        Intent photoPickerIntent = new Intent();
        photoPickerIntent.setType("image/*");
        photoPickerIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(photoPickerIntent, "Complete Action Using"), PICK_IMAGE_REQUEST);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImage = data.getData();
            Log.d(TAG, "onActivityResult: URI " + selectedImage);
            try {
                //Getting the Bitmap from Gallery
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                uploadPic();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }


    private void uploadPic() {
        //Showing the progress dialog

        final SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(Profile.this, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.setTitleText("Uploading");
        sweetAlertDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Implementations.update_pic, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Disimissing the progress dialog
                Log.d(TAG, "onResponse: " + response);
                try {
                    JSONObject respObj = new JSONObject(response);
                    mPic = respObj.getString("url");
                    Picasso.with(Profile.this).load(mPic).into(user_pic);

                    SharedPreferences mPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
                    SharedPreferences.Editor editor = mPreferences.edit();
                    editor.putString("user_pic", mPic);
                    editor.apply();

                    bitmap.recycle();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                sweetAlertDialog.cancel();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                //Showing toast
                Toast.makeText(Profile.this, error.getMessage(), Toast.LENGTH_LONG).show();
                Log.d(TAG, "onErrorResponse: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //Converting Bitmap to String
                String image = getStringImage(bitmap);
                //Creating parameters
                Map<String, String> params = new Hashtable<String, String>();

                //Adding parameters
                params.put(KEY_IMAGE, image);
                params.put(KEY_NAME, mName);
                params.put(KEY_NUMBER, mNum);

                //returning parameters
                return params;
            }
        };
        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }


    //Stack OVERFLOW
   /* private String getPath(Uri selectedImage) {

        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(selectedImage,
                filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        Log.d(TAG, "getPath: " + picturePath);
        return picturePath;
        *//*Cursor cursor = getContentResolver().query(selectedImage, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();
        Log.d(TAG, "getPath: "+path);
        return path;*//*
    }*/

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void getEducation() {
        final List<Education> education = new ArrayList<>();

        AsyncHttpClient client = new AsyncHttpClient();
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("user_number", mNum);
            jsonObj.put("token", token);
            StringEntity stringEntity = new StringEntity(jsonObj.toString());
            Log.d(TAG, "get education" + jsonObj.toString());
            client.post(Profile.this, Implementations.education_fetch, stringEntity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "EDUCATION Success " + response);
                    try {
                        JSONObject respObj = new JSONObject(response);
                        JSONArray eduArr = respObj.getJSONArray("user_education");
                        int len = eduArr.length();
                        edu = new String[len];
                        eduId = new String[len];
                        insti = new String[len];
                        for (int i = 0; i < len; i++) {
                            JSONObject currObj = eduArr.getJSONObject(i);
                            String id = currObj.getString("education_id");
                            String deg = currObj.getString("education");
                            String inst = currObj.getString("institute_name");
                            Education lists = new Education(deg, inst);
                            education.add(lists);

                            edu[i] = deg;
                            insti[i] = inst;
                            eduId[i] = id;
                        }
                        eduAdapter.clear();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    eduAdapter.addAll(education);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "get_EDU fail  " + response);
                    eduAdapter.clear();
                }
            });

        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void addEducation() {
        String deg = Degree.getText().toString().trim();
        String inst = Institute.getText().toString().trim();

        if (!TextUtils.isEmpty(deg) && !TextUtils.isEmpty(inst)) {
            JSONObject jsonObj = new JSONObject();
            AsyncHttpClient client = new AsyncHttpClient();
            try {
                jsonObj.put("user_number", mNum);
                jsonObj.put("token", token);
                jsonObj.put("education", deg);
                jsonObj.put("institute_name", inst);
                StringEntity stringEntity = new StringEntity(jsonObj.toString());
                Log.d(TAG, "add Education: " + jsonObj.toString());

                client.post(Profile.this, Implementations.education_add, stringEntity, "application/json", new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        String response = new String(responseBody, StandardCharsets.UTF_8);
                        Log.d(TAG, "Education success " + response);
                        Degree.getText().clear();
                        Institute.getText().clear();
                        getEducation();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        String response = new String(responseBody, StandardCharsets.UTF_8);
                        Log.d(TAG, "Education Failed " + response);
                    }
                });

            } catch (JSONException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void deleteEdu(String id) {
        JSONObject jsonObj = new JSONObject();
        AsyncHttpClient client = new AsyncHttpClient();
        try {
            jsonObj.put("token", token);
            jsonObj.put("education_id", id);
            StringEntity stringEntity = new StringEntity(jsonObj.toString());
            Log.d(TAG, "delete Education: " + jsonObj.toString());

            client.post(Profile.this, Implementations.education_delete, stringEntity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "Edu delete success " + response);
                    getEducation();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "edu delete failed" + response);
                }
            });
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void interestFetch() {
        final List<InterestItem> interests = new ArrayList<>();
        JSONObject jsonObj = new JSONObject();
        AsyncHttpClient client = new AsyncHttpClient();
        try {
            jsonObj.put("user_number", mNum);
            jsonObj.put("token", token);
            StringEntity stringEntity = new StringEntity(jsonObj.toString());
            Log.d(TAG, "show interest: " + jsonObj.toString());

            client.post(Profile.this, Implementations.interest_fetch, stringEntity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "interest fetch success " + response);
                    try {
                        JSONObject respObj = new JSONObject(response);


                        if (respObj.getJSONArray("user_interest") != null) {
                            JSONArray myArr = respObj.getJSONArray("user_interest");
                            int len = myArr.length();
                            tags = new String[len];
                            tagId = new String[len];
                            for (int i = 0; i < len; i++) {
                                JSONObject obj = myArr.getJSONObject(i);
                                String tagid = obj.getString("id");
                                String tag = obj.getString("interest");

                                InterestItem in = new InterestItem(tag);
                                interests.add(in);

                                tagId[i] = tagid;
                                tags[i] = tag;
                            }
                        }
                        mAdapter.clear();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mAdapter.addAll(interests);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "fetch failed " + response);
                }
            });
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void deleteInterest(String id) {
        JSONObject jsonObj = new JSONObject();
        AsyncHttpClient client = new AsyncHttpClient();
        try {
            jsonObj.put("token", token);
            jsonObj.put("interest_id", id);
            StringEntity stringEntity = new StringEntity(jsonObj.toString());
            Log.d(TAG, "delete interest: " + jsonObj.toString());

            client.post(Profile.this, Implementations.interest_delete, stringEntity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "interest delete success " + response);
                    interestFetch();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "interest delete failed " + response);
                }
            });
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void addNewInterest() {
        Log.d(TAG, "addNewInterest: ");
        if (addBtn.getText().equals(" + ")) {
            addBtn.setText("ADD");
            addBtn.setTextSize(18);
            interest.setVisibility(View.VISIBLE);
        } else {
            addBtn.setText(" + ");
            addBtn.setTextSize(22);
            interest.setVisibility(View.INVISIBLE);
            String str = interest.getText().toString().trim();
            interest.getText().clear();

            if (!TextUtils.isEmpty(str)) {
                JSONObject jsonObj = new JSONObject();
                AsyncHttpClient client = new AsyncHttpClient();
                try {
                    jsonObj.put("user_number", mNum);
                    jsonObj.put("token", token);
                    jsonObj.put("user_interest", str);
                    StringEntity stringEntity = new StringEntity(jsonObj.toString());
                    Log.d(TAG, "add interest: " + jsonObj.toString());

                    client.post(Profile.this, Implementations.interest_send, stringEntity, "application/json", new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            String response = new String(responseBody, StandardCharsets.UTF_8);
                            Log.d(TAG, "interest success " + response);
                            interestFetch();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            String response = new String(responseBody, StandardCharsets.UTF_8);
                            Log.d(TAG, "interest Failed " + response);
                        }
                    });
                } catch (JSONException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
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
                Intent intent = new Intent(Profile.this, Feeds.class);
                if (type.equals("chat")) {
                    intent = new Intent(Profile.this, ChatList.class);
                } else if (type.equals("group")) {
                    intent = new Intent(Profile.this, GangList.class);
                }
                PendingIntent pendingIntent = PendingIntent.getActivity(Profile.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
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
        getMenuInflater().inflate(R.menu.profile, menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_save) {
            updateProfile();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
