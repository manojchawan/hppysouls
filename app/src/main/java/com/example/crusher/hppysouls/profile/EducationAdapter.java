package com.example.crusher.hppysouls.profile;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.crusher.hppysouls.R;

import java.util.List;

/**
 * Created by manoj on 3/20/2017.
 */

public class EducationAdapter extends ArrayAdapter<Education> {

    public EducationAdapter(Context context,List<Education> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.edu_list_item, parent, false);
        }

        Education currObj = getItem(position);

        String deg = currObj.getmDegree();
        String insti =currObj.getmInstitute();

        TextView degText = (TextView) listItemView.findViewById(R.id.degree);
        TextView instiText = (TextView) listItemView.findViewById(R.id.institute);

        degText.setText(deg);
        instiText.setText(insti);

        return listItemView;
    }
}
