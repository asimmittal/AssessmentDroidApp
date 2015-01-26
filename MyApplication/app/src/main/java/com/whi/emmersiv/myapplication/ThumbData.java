package com.whi.emmersiv.myapplication;

/**
 * This is the model for a single element in the listView for
 * the VideoListActivity screen
 */
public class ThumbData {
    public String thumbUri;
    public String name;
    public float progress;
    public boolean isDone;
    public String key;

    public ThumbData(String thumb, String title, float prog, String k){
        thumbUri = thumb;
        name = title;
        progress = prog;
        isDone = (prog == 1) ? true: false;
        key = k;
    }
}
