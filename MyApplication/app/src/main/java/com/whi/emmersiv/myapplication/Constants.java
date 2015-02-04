package com.whi.emmersiv.myapplication;

import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import android.content.Context;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class Constants {
    private static Constants instance = null;
    private static HashMap<String, ArrayList<SceneQuestion>> data = new HashMap<String, ArrayList<SceneQuestion>>();
    private static String currSubject;

    //TODO: change this to "APP" when deploying
    public static String mode = "DEBUG";

    protected Constants(){
        //This is a singleton, so constructor is protected
    }

    public HashMap<String, ArrayList<SceneQuestion>> getData(){
        return data;
    }

    public ArrayList<SceneQuestion> getQuestionsForScene(String key){ return data.get(key); }

    public static Constants getInstance(){
        if(instance == null) instance = new Constants();
        return instance;
    }

    /********************************************************************************
     * set the current subject id
     * @param id
     *******************************************************************************/
    public void setCurrSubject(String id){
        currSubject = id;
    }

    /********************************************************************************
     * return the current subject id
     * @return subjectID for this session
     *******************************************************************************/
    public String getCurrSubject(){
        return currSubject;
    }

    /********************************************************************************
     * for a given key (scene id), return the thumb data that will be used
     * to display this in the video list activity
     * @param key
     * @return
     *******************************************************************************/
    public ThumbData getThumbDataForKey(String key){
        if(data.containsKey(key)){
            String thumb = "thumb" + key + ".png";
            ArrayList<SceneQuestion> list = data.get(key);
            String name = list.get(0).sceneName;
            int countDone = 0;
            for(SceneQuestion ques : list) countDone += (ques.isDone) ? 1 : 0;
            float progress = (float)countDone / (float)list.size();

            return new ThumbData(thumb, name, progress, key);
        }

        return null;
    }

    /********************************************************************************
     * Given a JSON string, deserialize it into objects representing
     * the various scenes and associated questions
     * @param json
     *******************************************************************************/
    public void buildDataModelFromJson(String json){
        try {
            // turn the raw json string into a JSON Object
            JSONObject jsonObj = new JSONObject(json);
            Iterator<String> keys = jsonObj.keys();

            // Parse the json to create the desired Map
            while(keys.hasNext()){
                String key = keys.next();

                if(key != null) {
                    JSONArray list = jsonObj.getJSONArray(key);
                    data.put(key, new ArrayList<SceneQuestion>());

                    for(int i = 0; i < list.length(); i++){
                        JSONObject question = list.getJSONObject(i);
                        if(question != null){
                            SceneQuestion quesObject = new SceneQuestion();
                            quesObject.area = question.getString("area");
                            quesObject.qID = question.getString("q_id");
                            quesObject.question = question.getString("question");
                            quesObject.opt1 = question.getString("opt1");
                            quesObject.opt2 = question.getString("opt2");
                            quesObject.opt3 = question.getString("opt3");
                            quesObject.correct = question.getString("correct");
                            quesObject.scene = key;
                            quesObject.isDone = false;
                            quesObject.sceneName = question.getString("scene_name");
                            quesObject.audioQues = question.getString("audio_ques");
                            quesObject.audioOpt1 = question.getString("audio_opt1");
                            quesObject.audioOpt2 = question.getString("audio_opt2");
                            quesObject.audioOpt3 = question.getString("audio_opt3");
                            data.get(key).add(quesObject);
                        }
                    }
                }
            }
        }catch (Exception e){}
    }

    /********************************************************************************
     * Silly method to make toast notifications
     * @param cxt
     * @param text
     *******************************************************************************/
    public static void makeToastWithString(Context cxt, String text){
        Toast.makeText(cxt, text, Toast.LENGTH_SHORT).show();
    }
}
