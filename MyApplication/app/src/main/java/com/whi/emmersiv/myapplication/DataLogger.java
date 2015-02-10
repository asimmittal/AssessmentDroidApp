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
    private File file;

    protected DataLogger(){

    }

    /********************************************************************************
     * Parameterized constructor - protected to prevent direct instantiation

     *******************************************************************************/
    public void createNewLog(){
        gson = new GsonBuilder().serializeNulls().create();
        long curTimeStamp = System.currentTimeMillis() / 1000L;
        String dirName = "Emmersiv Logs";
        String uname = Constants.getInstance().getCurrSubject();
        String sessionid = Constants.getInstance().getCurrSessionId();
        String filename = uname + "_" + curTimeStamp + "_assessment_" + sessionid +".log";

        File publicDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File folder = new File(publicDirectory,dirName);
        if(!folder.exists()){
            boolean success = folder.mkdir();
            if(success){
                file = new File(folder, filename);
            }
            else file = new File(publicDirectory, filename);
        }
        else file = new File(folder, filename);

        Log.d("xxx-----> LOGGER. File",file.getAbsolutePath());
    }

    /********************************************************************************
     * Return the instance of this singleton
     * @return
     *******************************************************************************/
    public static DataLogger getInstance(){
        if(instance == null) instance = new DataLogger();
        return instance;
    }


    /********************************************************************************
     * log the specified event
     * @param evt
     *******************************************************************************/
    public void log(BaseLogEvent evt){
        evt.timestamp = System.currentTimeMillis()/1000L;
        evt.userId = Constants.getInstance().getCurrSubject();
        evt.sessionId = Constants.getInstance().getCurrSessionId();
        String json = evt.timestamp + " " + gson.toJson(evt)+"\n";
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

    /*Static tags for data logging*/
    public static final String LoginEvent = "SubjectLoginEvent";                            //user logs in with a subject id (meta) - SelectionActivity
    public static final String ModuleSelectedEvent = "ModuleSelectedEvent";                 //user selects a module (meta) - VideoListActivity
    public static final String RepeatModuleSelectionEvent = "RepeatModuleSelectionEvent";   //user tries to repeatedly select a completed module (meta)
    public static final String SelectionScreenLoadedEvent = "SelectionScreenLoadedEvent";   //module selection screen loaded
    public static final String AllModulesCompletedEvent = "AllModulesCompletedEvent";       //user with id (meta) has finished answering questions for all modules
    public static final String UserLogoutEvent = "UserLogoutEvent";                         //user with id (meta) logged out manually
    public static final String AnswerSubmittedEvent = "AnswerSubmittedEvent";               //user with id (meta) has answered a question (both q and a are meta along with isCorrect)
    public static final String RepeatAudioEvent = "RepeatAudioEvent";                       //user selected to repeat the audio for the question (meta) being displayed
    public static final String VideoPlaybackCompleteEvent = "VideoPlaybackCompleteEvent";   //the video has finished playing for module with key
    public static final String QnAScreenDestroyedEvent = "QnAScreenDestroyedEvent";         //the QnA activity : onDestroy() invoked
    public static final String KeyInterruptEvent = "KeyInterruptEvent";                     //the QnA Activity was interrupted by user key press of home or back button (key code is meta)
    public static final String QnAScreenLoadedEvent = "QnAScreenLoadedEvent";               //the QnA activity was loaded
    public static final String QnAScreenStoppedEvent = "QnAScreenStoppedEvent";             //the QnA Activity was stopped (onStop() invoked)
    public static final String QuestionLoadComplete = "QuestionLoadComplete";               //question loaded on the screen - question is meta
    public static final String QuestionLoadStarted = "QuestionLoadStarted";                 //the question began showing up on screen - question is meta
}
