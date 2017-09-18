package com.example.crusher.hppysouls.profile;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

import com.example.crusher.hppysouls.R;

import java.util.List;

import mehdi.sakout.fancybuttons.FancyButton;


/**
 * Created by manoj on 2/18/2017.
 */

public class InterestAdapter extends BaseAdapter {

    private Context mContext;
    private final String[] tags;

    public InterestAdapter(Context mContext, String[] tags) {
        this.mContext = mContext;
        this.tags = tags;
    }


    @Override
    public int getCount() {
        return tags.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View grid;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            grid = new View(mContext);
            grid = inflater.inflate(R.layout.grid_interest, null);
            FancyButton btn = (FancyButton) grid.findViewById(R.id.interestBtn);
            btn.setText(tags[position]);

            int f = position % 4;
            switch (f) {
                case 1:
                    btn.setBorderColor(mContext.getResources().getColor(R.color.red));
                    break;
                case 2:
                    btn.setBorderColor(mContext.getResources().getColor(R.color.green));
                    break;
                case 3:
                    btn.setBorderColor(mContext.getResources().getColor(R.color.blue));
                    break;
                case 0:
                    btn.setBorderColor(mContext.getResources().getColor(R.color.yellow));
                    break;
            }

        } else {
            grid = convertView;
        }

        return grid;
    }
}
