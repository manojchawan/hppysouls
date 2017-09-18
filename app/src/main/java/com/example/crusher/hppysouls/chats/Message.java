package com.example.crusher.hppysouls.chats;

import java.util.Date;

/**
 * Created by manoj on 2/12/2017.
 */

public class Message {

    private String mText;
    private String mSender;
    private String mDate;

    public Message(String mText, String mSender, String mDate) {
        this.mText = mText;
        this.mSender = mSender;
        this.mDate = mDate;
    }

    public String getmText() {
        return mText;
    }

    public void setmText(String mText) {
        this.mText = mText;
    }

    public String getmSender() {
        return mSender;
    }

    public void setmSender(String mSender) {
        this.mSender = mSender;
    }

    public String getmDate() {
        return mDate;
    }

    public void setmDate(String mDate) {
        this.mDate = mDate;
    }
}
