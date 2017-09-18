package com.example.crusher.hppysouls;

/**
 * Created by crusher on 16/1/17.
 */
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomList extends ArrayAdapter<String>{

    private final Activity context;
    private final String[] web;
    private final String[] imageId;

    public CustomList(Activity context, String[] web, String[] imageId) {
        super(context, R.layout.edu_list_item, web);
        this.context = context;
        this.web = web;
        this.imageId = imageId;

    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.edu_list_item, null, true);

        TextView txtDeg = (TextView) rowView.findViewById(R.id.degree);
        TextView txtInst = (TextView) rowView.findViewById(R.id.institute);

        txtDeg.setText(web[position]);
        txtInst.setText(imageId[position]);

        return rowView;
    }
}
