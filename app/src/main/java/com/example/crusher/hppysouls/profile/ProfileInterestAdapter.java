package com.example.crusher.hppysouls.profile;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.example.crusher.hppysouls.R;

import java.util.List;

import mehdi.sakout.fancybuttons.FancyButton;

/**
 * Created by manoj on 3/27/2017.
 */

public class ProfileInterestAdapter extends ArrayAdapter<InterestItem> {

    public ProfileInterestAdapter(@NonNull Context context, @NonNull List<InterestItem> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;

        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.grid_interest, parent, false);
        }

        InterestItem obj = getItem(position);

        String str = obj.getmInterest();
        FancyButton btn = (FancyButton) listItemView.findViewById(R.id.interestBtn);
        btn.setText(str);

        int f = position % 4;
        switch (f) {
            case 1:
                btn.setBorderColor(getContext().getResources().getColor(R.color.red));
                break;
            case 2:
                btn.setBorderColor(getContext().getResources().getColor(R.color.green));
                break;
            case 3:
                btn.setBorderColor(getContext().getResources().getColor(R.color.blue));
                break;
            case 0:
                btn.setBorderColor(getContext().getResources().getColor(R.color.yellow));
                break;
        }

        return listItemView;

    }
}
