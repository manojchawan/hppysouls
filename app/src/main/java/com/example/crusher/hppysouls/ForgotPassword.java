package com.example.crusher.hppysouls;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import net.rimoto.intlphoneinput.IntlPhoneInput;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.pedant.SweetAlert.SweetAlertDialog;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.protocol.HTTP;

public class ForgotPassword extends AppCompatActivity {
    private static final String TAG = ForgotPassword.class.getSimpleName();

    EditText otpView, passwordView;
    Button register;
    private TextView resend, timer;
    IntlPhoneInput phoneInputView;
    private String user_password, user_num, otp_num, token_value;

    SweetAlertDialog successAlert, fail;
    AsyncHttpClient client;
   BroadcastReceiver receiver = null;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        setTitle("Forgot Password");

        successAlert = new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE);

        phoneInputView = (IntlPhoneInput) findViewById(R.id.user_number_register);
        phoneInputView.setEmptyDefault("IN");
        passwordView = (EditText) findViewById(R.id.user_password);
        register = (Button) findViewById(R.id.add_register);
        otpView = (EditText) findViewById(R.id.user_otp_register);
        otpView.setVisibility(View.GONE);
        resend = (TextView) findViewById(R.id.resend_reg);
        resend.setVisibility(View.GONE);
        timer = (TextView) findViewById(R.id.timer);
        timer.setVisibility(View.GONE);

        client = new AsyncHttpClient();

        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                processReceived(context, intent);
            }
        };
        registerReceiver(receiver, filter);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                user_password = passwordView.getText().toString();
                if (!phoneInputView.isValid()) {
                    YoYo.with(Techniques.Tada).playOn(phoneInputView);
                    Snackbar.make(view, "Please Enter Valid Mobile Number", Snackbar.LENGTH_LONG).show();
                } else if (TextUtils.isEmpty(user_password)) {
                    YoYo.with(Techniques.Tada).playOn(passwordView);
                    Snackbar.make(view, "Password cannot be empty", Snackbar.LENGTH_LONG).show();
                } else {
                    user_num = AppFunctions.removeFirstChar(phoneInputView.getNumber());
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("user_number", user_num);
                        jsonObject.put("user_password", user_password);

                        StringEntity entity = new StringEntity(jsonObject.toString());
                        entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

                        final SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(ForgotPassword.this, SweetAlertDialog.PROGRESS_TYPE);
                        sweetAlertDialog.setTitleText("Loading");
                        sweetAlertDialog.show();

                        client.post(ForgotPassword.this, Implementations.forget_url_verify, entity, "application/json", new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                final String response = new String(responseBody, StandardCharsets.UTF_8);
                                sweetAlertDialog.cancel();
                                Log.d(TAG, "on forgot success: " + response);
                                try {
                                    JSONObject respObj = new JSONObject(response);

                                    token_value = respObj.getString("token");

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                successAlert.setTitleText("Success");
                                successAlert.setContentText("Enter OTP to complete verification");

                                phoneInputView.setVisibility(View.GONE);
                                passwordView.setVisibility(View.GONE);
                                otpView.setVisibility(View.VISIBLE);
                                successAlert.show();

                                register.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        otp_num = otpView.getText().toString();

                                        JSONObject jsonobject = new JSONObject();
                                        try {
                                            jsonobject.put("user_number", user_num);
                                            jsonobject.put("user_password", user_password);
                                            jsonobject.put("otp", otp_num);
                                            jsonobject.put("user_token", token_value);
                                            StringEntity stringEntity = new StringEntity(jsonobject.toString());
                                            stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                                            Log.d(TAG, "on auth: " + jsonobject.toString());

                                            client.post(ForgotPassword.this, Implementations.forget_url_auth, stringEntity, "application/json", new AsyncHttpResponseHandler() {
                                                @Override
                                                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                                    sweetAlertDialog.cancel();
                                                    String response = new String(responseBody, StandardCharsets.UTF_8);
                                                    Log.d(TAG, "onSuccess: OTP verified" + response);

                                                    try {
                                                        JSONObject respObj = new JSONObject(response);
                                                        Toast.makeText(ForgotPassword.this, respObj.getString("response"), Toast.LENGTH_SHORT).show();
                                                        gotoFeedsActivity();
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }

                                                @Override
                                                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                                    sweetAlertDialog.cancel();
                                                    String response = new String(responseBody, StandardCharsets.UTF_8);
                                                    try {
                                                        JSONObject respObj = new JSONObject(response);
                                                        otpView.setVisibility(View.VISIBLE);
                                                        resend.setVisibility(View.VISIBLE);
                                                        Log.d(TAG, "onFailure: otp-verify " + respObj.getString("response"));
                                                        fail = new SweetAlertDialog(ForgotPassword.this, SweetAlertDialog.ERROR_TYPE);
                                                        fail.setTitleText(respObj.getString("response"));
                                                        fail.show();
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                        } catch (JSONException | UnsupportedEncodingException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                String response = new String(responseBody, StandardCharsets.UTF_8);
                                Log.d(TAG, "onFailure: NUM & PASS " + response);
                                sweetAlertDialog.cancel();
                                try {
                                    JSONObject respObj = new JSONObject(response);
                                    String str;
                                    str = respObj.getString("response");
                                    fail = new SweetAlertDialog(ForgotPassword.this, SweetAlertDialog.ERROR_TYPE);
                                    fail.setTitleText(str);
                                    fail.show();
                                    if (str.equals("User Already Exists! Try Logging in instead.")) {
                                        Snackbar.make(view, "Already a member?", Snackbar.LENGTH_INDEFINITE)
                                                .setAction("Login", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        Intent intent = new Intent(ForgotPassword.this, LoginActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                })
                                                .setActionTextColor(getResources().getColor(android.R.color.holo_red_light))
                                                .show();
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } catch (JSONException | UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void gotoFeedsActivity() {
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    public void processReceived(Context context, Intent intent) {
        Toast.makeText(context, "xyz", Toast.LENGTH_SHORT).show();
        Bundle bundle = intent.getExtras();
        String message = null;
        Object[] pdusObj = (Object[]) bundle.get("pdus");
        for (int i = 0; i < pdusObj.length; i++) {
            SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
            String senderNum = currentMessage.getOriginatingAddress();
            message = currentMessage.getDisplayMessageBody();
            Log.d(TAG, "processReceived: from: " + senderNum + " MSG: " + message);
        }
        String otp = parseCode(message);
        otpView.setText(otp);
    }

    private String parseCode(String message) {
        Pattern p = Pattern.compile("\\b\\d{4,6}\\b");
        Matcher m = p.matcher(message);
        String code = "";
        while (m.find()) {
            code = m.group(0);
        }
        Log.d(TAG, "parseCode: " + code);
        return code;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void resendOtp(View v) {

        resend.setVisibility(View.GONE);
        timer.setVisibility(View.VISIBLE);
        new CountDownTimer(30000, 1000) {

            public void onTick(long millisUntilFinished) {
                timer.setText("Wait for 0:" + millisUntilFinished / 1000);
            }

            public void onFinish() {
                timer.setVisibility(View.GONE);
                resend.setVisibility(View.VISIBLE);
            }
        }.start();


        AsyncHttpClient client = new AsyncHttpClient();
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("user_number", user_num);
            StringEntity stringEntity = new StringEntity(jsonObj.toString());
            Log.d(TAG, "resend otp: " + jsonObj.toString());

            client.post(ForgotPassword.this, Implementations.resend_otp, stringEntity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "resend Success " + response);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "resend failed " + response);
                }
            });

        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

}
