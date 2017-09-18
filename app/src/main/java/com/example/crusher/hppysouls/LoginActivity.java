package com.example.crusher.hppysouls;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import net.rimoto.intlphoneinput.IntlPhoneInput;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.protocol.HTTP;


public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";


    private EditText passwordtxt;
    private String user_number, fcmtoken, password;
    Handler handler;
    boolean doubleBackToExitPressedOnce = false, threadOn = false;


    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseMessaging.getInstance().subscribeToTopic("test");
        fcmtoken = FirebaseInstanceId.getInstance().getToken();

        setTitle("Login HppySouls");

        final IntlPhoneInput phoneInputView = (IntlPhoneInput) findViewById(R.id.user_number_login);
        phoneInputView.setEmptyDefault("IN");

        passwordtxt = (EditText) findViewById(R.id.otpLogin);

        final AsyncHttpClient client = new AsyncHttpClient();
        Button loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (phoneInputView.isValid()) {
                    user_number = AppFunctions.removeFirstChar(phoneInputView.getNumber());
                    password = passwordtxt.getText().toString();

                    if (!TextUtils.isEmpty(password)) {
                        try {
                            final JSONObject jsonObject = new JSONObject();
                            jsonObject.put("user_number", user_number);
                            jsonObject.put("user_password", password);
                            jsonObject.put("fcm_token", fcmtoken);
                            jsonObject.put("token", AppFunctions.SHA1(AppFunctions.SHA1(user_number)));
                            StringEntity entity = new StringEntity(jsonObject.toString());
                            entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                            Log.d(TAG, "onClick: " + jsonObject.toString());

                            client.post(LoginActivity.this, Implementations.login_url_auth, entity, "application/json", new AsyncHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                    String response = new String(responseBody, StandardCharsets.UTF_8);
                                    Log.d(TAG, "onSuccess: " + response);
                                    try {
                                        JSONObject respObj = new JSONObject(response);
                                        Toast.makeText(LoginActivity.this, respObj.getString("response"), Toast.LENGTH_LONG).show();

                                        SharedPreferences mPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = mPreferences.edit();
                                        editor.putString("user_name", respObj.getJSONObject("user_details").getString("user_name"));
                                        editor.putString("user_pic", respObj.getJSONObject("user_details").getString("user_profile_url"));
                                        editor.putString("user_gender", respObj.getJSONObject("user_details").getString("user_gender"));
                                        editor.putString("user_number", respObj.getJSONObject("user_details").getString("user_number"));
                                        editor.putString("user_age", respObj.getJSONObject("user_details").getString("user_age"));
                                        editor.putString("user_occupation", respObj.getJSONObject("user_details").getString("user_occupation"));
                                        editor.putString("user_organization", respObj.getJSONObject("user_details").getString("user_organization"));
                                        editor.apply();
                                        gotoFeedsActivity();

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                    String response = new String(responseBody, StandardCharsets.UTF_8);
                                    Log.d(TAG, "onFailure: " + response);
                                    try {
                                        JSONObject respObj = new JSONObject(response);
                                        String str = respObj.getString("response");
                                        Toast.makeText(LoginActivity.this, str, Toast.LENGTH_LONG).show();
                                        if(str.equals("Invalid Password")){
                                            YoYo.with(Techniques.Tada).playOn(passwordtxt);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } catch (UnsupportedEncodingException | JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        YoYo.with(Techniques.Tada).playOn(passwordtxt);
                        Snackbar.make(view, "Enter Password", Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    YoYo.with(Techniques.Tada).playOn(phoneInputView);
                    Snackbar.make(view, "Enter a valid mobile number", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        threadOn = true;
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (threadOn)
            handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (threadOn)
            handler.removeCallbacksAndMessages(null);
    }

    public void gotoFeedsActivity() {
        Intent i = new Intent(LoginActivity.this, Feeds.class);
        startActivity(i);
        finish();
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    public void forgot_pwd(View v) {
        Intent i = new Intent(LoginActivity.this, ForgotPassword.class);
        startActivity(i);
    }
}
