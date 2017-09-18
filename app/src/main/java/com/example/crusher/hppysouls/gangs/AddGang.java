package com.example.crusher.hppysouls.gangs;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.example.crusher.hppysouls.Implementations;
import com.example.crusher.hppysouls.R;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class AddGang extends AppCompatActivity {

    private static final String TAG = AddGang.class.getSimpleName();
    Button add_gang;
    EditText gang_name;
    String name, type;


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_gang);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        type = getString(R.string.gang_select);
        gang_name = (EditText) findViewById(R.id.gang_name);
        add_gang = (Button) findViewById(R.id.add_gang);

        final MaterialSpinner spinner = (MaterialSpinner) findViewById(R.id.gang_type);
        spinner.setItems("Select", "Personal", "Work", "Events");
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                type = item.toString();
            }
        });

        add_gang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = gang_name.getText().toString();

                if (TextUtils.isEmpty(name)) {
                    YoYo.with(Techniques.Tada).duration(200).playOn(gang_name);
                    Snackbar.make(view, "Blank Field Not Allowed", Snackbar.LENGTH_SHORT).show();
                } else if (type.equals("Select")) {
                    YoYo.with(Techniques.Tada).duration(200).playOn(spinner);
                } else {
                    createGang();
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void createGang() {
        JSONObject jsonObj = new JSONObject();
        AsyncHttpClient client = new AsyncHttpClient();
        try {
            jsonObj.put("group_name", name);
            jsonObj.put("group_members", Implementations.user_num);
            jsonObj.put("group_type", type);
            StringEntity stringEntity = new StringEntity(jsonObj.toString());
            Log.d(TAG, "create: " + jsonObj.toString());
            client.post(AddGang.this, Implementations.add_gang_url, stringEntity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "success: " + response);

                    Intent intent = new Intent(AddGang.this, GangList.class);
                    startActivity(intent);

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    String response = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d(TAG, "Failure: " + response);
                }
            });

        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }


    }
}
