package com.example.crusher.hppysouls;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.facebook.*;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.protocol.HTTP;

public class SplashScreen extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = SplashScreen.class.getSimpleName();
    CallbackManager mCallbackManager;
    private LoginButton fbLoginButton;
    private ProfileTracker mProfileTracker;

    private SignInButton googleLoginButton;
    private GoogleSignInOptions googleSignInOptions;
    private GoogleApiClient googleApiClient;
    private static final int REQ_CODE_GOOGLE = 10;

    private String gender, email, user_name, user_pic;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);


        Button signup = (Button) findViewById(R.id.signup);

        SharedPreferences mPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
        String mNum = mPreferences.getString("user_number", "");

        if (!TextUtils.isEmpty(mNum)) {
            Intent i = new Intent(this, Feeds.class);
            startActivity(i);
            finish();
        }

        // INITIALIZE FB BUTTON,CallbackManager AND ALSO ASK FOR ANY EXTRA PERMISSIONS
        mCallbackManager = CallbackManager.Factory.create();
        fbLoginButton = (LoginButton) findViewById(R.id.fb_login_button);
        fbLoginButton.setReadPermissions("public_profile");
        /*fbLoginButton.setReadPermissions(Arrays.asList(
                "public_profile", "email"));*/


        //INITIALIZE GOOGLE LOGIN BUTTON
        googleLoginButton = (SignInButton) findViewById(R.id.google_login_button);
        googleLoginButton.setSize(SignInButton.SIZE_WIDE);


        //CHECK PERMISSIONS &ASK TO GRANT Them IF THEY ARE NOT PROVIDED
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS,
                    Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
        }


        YoYo.with(Techniques.FadeInUp).duration(700).playOn(findViewById(R.id.splash_logo));
        YoYo.with(Techniques.BounceInLeft).duration(900).playOn(findViewById(R.id.signup));

        View parentLayout = findViewById(R.id.content_splash_screen);
        Snackbar.make(parentLayout, "Already a member?", Snackbar.LENGTH_INDEFINITE)
                .setAction("Login", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .setActionTextColor(getResources().getColor(android.R.color.holo_red_light))
                .show();

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SplashScreen.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });


        // FACEBOOK lOGIN VALIDATION BEGINS FROM HERE
        fbLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = loginResult.getAccessToken();
                Log.d(TAG, "AccessToken User ID: " + accessToken.getUserId());
                displayFbLogin();
                GraphRequest request = GraphRequest.newMeRequest(
                        accessToken, new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.d(TAG, "FB Success onCompleted: " + response.toString());
                                try {
                                    email = object.getString("email");
                                    gender = object.getString("gender");

                                    SharedPreferences mPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = mPreferences.edit();
                                    editor.putString("user_gender", gender);
                                    editor.putString("user_email", email);
                                    editor.apply();


                                    gotoRegActivity();

                                    //  birthday = object.getString("birthday"); // 01/31/1980 format
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Log.d("Fb login ", "Login cancelled");

            }

            @Override
            public void onError(FacebookException error) {
                Log.d("LoginError", error.getMessage());
                Log.d("LoginError", error.toString());
                Log.d(TAG, "onError: ");
            }
        });


        // GOOGLE VALIDATION BEGINS FROM HERE.
        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .addApi(Plus.API).build();

        googleLoginButton.setScopes(googleSignInOptions.getScopeArray());

        googleLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(signInIntent, REQ_CODE_GOOGLE);
            }
        });
    }


    // SENDS THE RESULT OF FB LOGIN.
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE_GOOGLE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        } else {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
        Log.d(TAG, "onActivityResult: " + requestCode);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onResume() {
        super.onResume();
        displayFbLogin();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void displayFbLogin() {

        Profile mProfile = Profile.getCurrentProfile(); // <-This Profile is from com.facebook package, not from hppysouls class
        if (Profile.getCurrentProfile() == null) {
            mProfileTracker = new ProfileTracker() {
                @Override
                protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                    Log.d(TAG, "onCurrentProfileChanged: " + currentProfile.getName());

                    SharedPreferences mPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
                    SharedPreferences.Editor editor = mPreferences.edit();
                    editor.putString("user_name", currentProfile.getName());
                    editor.putString("user_pic", currentProfile.getProfilePictureUri(300, 300).toString());
                    editor.apply();

                    mProfileTracker.stopTracking();
                    Log.d(TAG, "tracking stopped.");
                    gotoRegActivity();
                }
            };
        } else {
            Log.d(TAG, "Display fb_profile : " + mProfile.getName() + "photo, " + mProfile.getProfilePictureUri(300, 300));

            SharedPreferences mPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putString("user_name", mProfile.getName());
            editor.putString("user_pic", mProfile.getProfilePictureUri(300, 300).toString());
            editor.apply();
            gotoRegActivity();
        }

    }

    //HANDLES GOOGLE LOGIN
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());

        // SIGNED IN SUCCESSFULLY, SHOW AUTHENTICATED UI.
        GoogleSignInAccount acct = result.getSignInAccount();
        Person person = Plus.PeopleApi.getCurrentPerson(googleApiClient);
        if (person.getGender() == 0)
            gender = "Male";
        else if (person.getGender() == 1)
            gender = "Female";

        email = acct.getEmail();
        user_name = acct.getDisplayName();
        user_pic = String.valueOf(acct.getPhotoUrl());
        Log.d(TAG, "handleSignInResult: Google Data= " + user_name + ", " + email + ", Gender : " + gender + ", " + user_pic + ", " + acct.getIdToken());

        SharedPreferences mPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString("user_name", user_name);
        editor.putString("user_pic", user_pic);
        editor.putString("user_gender", gender);
        editor.putString("user_email", email);
        editor.apply();

        gotoRegActivity();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    //REDIRECTING TO NEXT ACTIVITY
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void gotoRegActivity() {

        if (email == null | TextUtils.isEmpty(email)) {
            sendtoreg();
            return;
        }

        Implementations.user_email = email;
        final AsyncHttpClient client = new AsyncHttpClient();
        final JSONObject obj = new JSONObject();
        try {
            obj.put("user_mail", email);
            final StringEntity stringEntity = new StringEntity(obj.toString());
            stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            Log.d(TAG, "is User Check: " + obj.toString());
            client.post(SplashScreen.this, Implementations.isUser, stringEntity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "onSuccess: " + response);
                    try {
                        JSONObject respObj = new JSONObject(response);
                        boolean auth = respObj.getBoolean("auth");
                        if (auth) {
                            Intent i = new Intent(SplashScreen.this, LoginActivity.class);
                            startActivity(i);
                            finish();
                        } else {
                            sendtoreg();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "on_failure: " + response);

                    sendtoreg();
                }
            });
        } catch (UnsupportedEncodingException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendtoreg() {
        Intent i = new Intent(SplashScreen.this, RegisterActivity.class);
        startActivity(i);
        finish();
    }


    //FOR PERMISSION
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
            } else {
                // User refused to grant permission. You can add AlertDialog here
                Toast.makeText(this, "You didn't give permission to access device location", Toast.LENGTH_LONG).show();
                startInstalledAppDetailsActivity();
            }
        }
    }

    //TO SHOW APP INFO
    private void startInstalledAppDetailsActivity() {
        Intent i = new Intent();
        i.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }
}