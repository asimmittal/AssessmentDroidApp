package com.whi.emmersiv.myapplication;

public class BaseLogEvent{
    public long timestamp;
    public String userId;
    public String sessionId;
    public String tag;
    public Object meta;

    public BaseLogEvent(String t, Object m){
        tag = t;
        meta = m;
    }
};
