package com.whi.emmersiv.myapplication;

import android.content.*;
import android.os.Environment;
import android.util.Log;

import com.google.gson.*;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by Asim on 1/26/2015.
 */
public class DataLogger {

    private static DataLogger instance = null;
    private Gson gson;
    private Context cxt;
    private File file;

    protected DataLogger(){

    }

    protected DataLogger(Context cxt){
        gson = new GsonBuilder().serializeNulls().create();
        long curTimeStamp = System.currentTimeMillis() / 1000L;
        String dirName = "Emmersiv Logs";
        String filename = "log_" + curTimeStamp;
        File publicDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File folder = new File(publicDirectory,dirName);
        if(!folder.exists()){
            boolean success = folder.mkdir();
            if(success){
                file = new File(folder, filename);
            }
            else file = new File(publicDirectory, filename);
        }
        else file = new File(publicDirectory, filename);

        Log.d("xxx-----> LOGGER. File",file.getAbsolutePath());
    }

    public static DataLogger getInstance(Context cxt){
        if(instance == null) instance = new DataLogger(cxt);
        return instance;
    }

    public void log(BaseLogEvent evt){
        evt.timestamp = System.currentTimeMillis()/1000L;
        String json = Constants.mode + ":" + gson.toJson(evt)+"\n";
        Log.d("xxxx----> LOGGER", json);

        try {
            FileOutputStream out = new FileOutputStream(file,true);
            out.write(json.getBytes());
            out.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

}
