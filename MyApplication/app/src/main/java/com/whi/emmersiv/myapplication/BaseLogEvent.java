package com.whi.emmersiv.myapplication;

public class BaseLogEvent{
    public long timestamp;
    public String logMsg;
    public Object meta;

    public BaseLogEvent(String msg, Object m){
        logMsg = msg;
        meta = m;
    }
};
