package com.example.crusher.hppysouls.chatLists;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.crusher.hppysouls.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by manoj on 2/15/2017.
 */

public class ChatListAdapter extends ArrayAdapter<ChatListItem> {

    private ArrayList<ChatListItem> arrayList = new ArrayList<>();

    public ChatListAdapter(Context context, ArrayList<ChatListItem> objects) {
        super(context, 0, objects);
        arrayList = objects;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.chat_list_item, parent, false);
        }
        ChatListItem currObj = getItem(position);

        String name = currObj.getmName();
        String pic = currObj.getmPic();

        TextView nameView = (TextView) listItemView.findViewById(R.id.chatlist_name);
        CircleImageView picView = (CircleImageView) listItemView.findViewById(R.id.chatlist_pic);
        nameView.setText(name);
        Picasso.with(getContext()).load(pic).into(picView);


        return listItemView;
    }

    public void setFilter(ArrayList<ChatListItem> newList) {
        arrayList = new ArrayList<>();
        arrayList.addAll(newList);
        notifyDataSetChanged();
        Log.d("ADAPTER", "setFilter:");
    }
}
