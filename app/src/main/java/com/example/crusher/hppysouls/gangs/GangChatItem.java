package com.example.crusher.hppysouls.gangs;

/**
 * Created by manoj on 2/17/2017.
 */

public class GangChatItem {

    private String mText;
    private String mSender;
    private String mDate;
    private String mName;

    public GangChatItem(String mText, String mSender, String mDate, String mName) {
        this.mText = mText;
        this.mSender = mSender;
        this.mDate = mDate;
        this.mName = mName;
    }


    public String getmText() {
        return mText;
    }

    public String getmSender() {
        return mSender;
    }

    public String getmDate() {
        return mDate;
    }

    public String getmName() {
        return mName;
    }
}
