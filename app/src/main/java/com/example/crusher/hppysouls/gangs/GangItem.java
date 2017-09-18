package com.example.crusher.hppysouls.gangs;

/**
 * Created by manoj on 2/16/2017.
 */

public class GangItem {
    private String mName;
    private String mType;

    private String mId;

    public GangItem(String mName, String mType, String mId) {
        this.mName = mName;
        this.mType = mType;
        this.mId = mId;

    }



    public String getmName() {
        return mName;
    }

    public String getmType() {
        return mType;
    }

    public String getmId() {
        return mId;
    }
}
