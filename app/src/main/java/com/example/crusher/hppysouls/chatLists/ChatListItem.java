package com.example.crusher.hppysouls.chatLists;

/**
 * Created by manoj on 2/15/2017.
 */

public class ChatListItem {

    private String mName;
    private String mPic;

    private String mNumber;

    public ChatListItem(String mName, String mPic, String mNumber) {
        this.mName = mName;
        this.mPic = mPic;
        this.mNumber = mNumber;

    }

    public String getmNumber() {
        return mNumber;
    }

    public String getmName() {
        return mName;
    }

    public String getmPic() {
        return mPic;
    }
}
