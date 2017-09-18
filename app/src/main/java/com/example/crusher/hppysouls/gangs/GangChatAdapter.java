package com.example.crusher.hppysouls.gangs;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.crusher.hppysouls.Implementations;
import com.example.crusher.hppysouls.R;

import java.util.List;

/**
 * Created by manoj on 2/17/2017.
 */

public class GangChatAdapter extends ArrayAdapter<GangChatItem> {


    public GangChatAdapter(Context context, List<GangChatItem> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.message_item, parent, false);
        }
        GangChatItem currObj = getItem(position);

        String sender = currObj.getmSender();
        String msg = currObj.getmText();
        String date = currObj.getmDate();
        String name = currObj.getmName();

        TextView nameView = (TextView) listItemView.findViewById(R.id.sender_name);
        TextView msgView = (TextView) listItemView.findViewById(R.id.message);
        TextView dateView = (TextView) listItemView.findViewById(R.id.date);
        msgView.setText(msg);
        dateView.setText(date);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) msgView.getLayoutParams();
        LinearLayout.LayoutParams layoutParamsDate = (LinearLayout.LayoutParams) dateView.getLayoutParams();

        if (sender.equals(Implementations.user_num)) {
            msgView.setBackgroundResource(R.color.user_chat_background);
            layoutParams.gravity = Gravity.RIGHT;
            layoutParamsDate.gravity = Gravity.RIGHT;

        } else {
            msgView.setBackgroundResource(R.color.receiver_chat_background);
            layoutParams.gravity = Gravity.LEFT;
            layoutParamsDate.gravity = Gravity.LEFT;
            nameView.setText(name);
        }
        msgView.setLayoutParams(layoutParams);
        dateView.setLayoutParams(layoutParamsDate);

        return listItemView;
    }
}
