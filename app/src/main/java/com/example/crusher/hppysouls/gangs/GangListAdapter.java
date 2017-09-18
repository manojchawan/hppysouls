package com.example.crusher.hppysouls.gangs;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.crusher.hppysouls.R;
import com.example.crusher.hppysouls.chatLists.ChatListItem;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by crusher on 17/1/17.
 */
public class GangListAdapter extends ArrayAdapter<GangItem> {

    public GangListAdapter(Context context, List<GangItem> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        View listItemView = view;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.chat_list_item, parent, false);
        }
        GangItem currObj = getItem(position);
        String name = currObj.getmName();
        String pic = currObj.getmType();
        String id = currObj.getmId();

        TextView nameView = (TextView) listItemView.findViewById(R.id.chatlist_name);
        CircleImageView picView = (CircleImageView) listItemView.findViewById(R.id.chatlist_pic);

        nameView.setText(name);


        switch (pic) {
            case "Personal":
                picView.setImageResource(R.drawable.personal);
                //Picasso.with(getContext()).load(R.drawable.personal).into(picView);
                break;
            case "Work":
                picView.setImageResource(R.drawable.work);
                break;
            case "Events":
                picView.setImageResource(R.drawable.events);
                break;
            default:
                break;
        }

        return listItemView;
    }
}
