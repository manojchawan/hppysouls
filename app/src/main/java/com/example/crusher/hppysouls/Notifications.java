package com.example.crusher.hppysouls;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.crusher.hppysouls.gangs.GangListAdapter;

public class Notifications extends AppCompatActivity {

    ListView list;
    String[] web = {
            "Jai Bhanushali has liked you",
            "Ankit Mistry has liked you"
    };
    Integer[] imageId = {
            R.drawable.ic_like,
            R.drawable.ic_like,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("Notifications");

    }
}
