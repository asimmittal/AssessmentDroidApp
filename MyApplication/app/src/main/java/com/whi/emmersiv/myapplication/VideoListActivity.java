package com.whi.emmersiv.myapplication;

import java.util.*;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.*;
import android.provider.ContactsContract;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import android.content.Intent;
import android.util.Log;

/**
 * This activity displays a list of modules available for the user to
 * try out. It shows this using a list view where each item is rendered
 * using 'thumbdata_view.xml' and uses ThumbData class for its model
 */
public class VideoListActivity extends Activity {

    ArrayList<ThumbData> thumbData;
    ListView listView;
    RelativeLayout doneContainer;
    Animation slideInAnim;

    boolean areModulesComplete = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);
        thumbData = new ArrayList<ThumbData>();
        listView = (ListView)findViewById(R.id.listThumbData);
        doneContainer = (RelativeLayout) findViewById(R.id.doneContainer);
        slideInAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slidein);

        loadThumbData();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ThumbData itemSelected = thumbData.get(position);
                if(!itemSelected.isDone) {

                    //log: User picks a module to watch
                    DataLogger.getInstance().log(new BaseLogEvent(DataLogger.ModuleSelectedEvent, itemSelected));

                    Intent intent = new Intent(getApplicationContext(), VideoPlayerActivity.class);
                    intent.putExtra("module_key", itemSelected.key);
                    startActivity(intent);
                }
                else {
                    Constants.makeToastWithString(getApplicationContext(), "You're done with that one!!");

                    //log: User is trying to select a complete module again (undesirable behavior)
                    DataLogger.getInstance().log(new BaseLogEvent(DataLogger.RepeatModuleSelectionEvent, itemSelected));
                }
            }
        });

        Log.d("---------> OnCreate in list view. Size of thumbData is",""+thumbData.size());
    }

    /********************************************************************************
     * Activity has resumed, recompute the latest values for the thumbData and bind
     * the new data to the list view
     *******************************************************************************/
    public void onResume(){
        super.onResume();
        Log.d("-------> ON RESUME IN LIST", "");

        //log: loading selection screen
        DataLogger.getInstance().log(new BaseLogEvent(DataLogger.SelectionScreenLoadedEvent,"onResume()"));

        areModulesComplete = true;

        //update the progress stats around any of the thumb data items have changed
        for(ThumbData td : thumbData){
            ThumbData tdNew = Constants.getInstance().getThumbDataForKey(td.key);
            td.isDone = tdNew.isDone;
            td.progress = tdNew.progress;
            if(!td.isDone) areModulesComplete = false;
        }

        //reattach the adapter to the list view
        ThumbDataAdapter tdAdapter = new ThumbDataAdapter(getApplicationContext(),thumbData);
        listView.setAdapter(tdAdapter);
        if(areModulesComplete){
            showCompleteUI();

            //log: user with ID has finished all the modules
            DataLogger.getInstance().log(new BaseLogEvent(DataLogger.AllModulesCompletedEvent, Constants.getInstance().getCurrSubject()));
        }
        else doneContainer.setVisibility(View.INVISIBLE);
    }

    /********************************************************************************
     * Shows the completion UI
     *******************************************************************************/
    private void showCompleteUI(){

        //animate the "done" ui into view
        doneContainer.setVisibility(View.VISIBLE);

        //automatically end the activity after 3 seconds
        Handler endActivityHandler = new Handler();
        endActivityHandler.postDelayed(new Runnable(){
            public void run(){
                finish();
            }
        }, 5000);
    }

    /********************************************************************************
     * Turn the data in the Static HashMap into a list of thumbs i.e. thumbData for
     * easier data binding with the list view
     *******************************************************************************/
    private void loadThumbData(){
        Set keys = Constants.getInstance().getData().keySet();
        thumbData.clear();
        Iterator<String> iter = keys.iterator();
        ArrayList<String> keyList = new ArrayList<String>();

        //get the list of keys from the iterator
        while(iter.hasNext()){
            String key = iter.next();
            keyList.add(key);
        }

        //Randomly grab a key from the list and put its associated thumb data
        //into the 'thumbData' array

        ArrayList<Integer> done = new ArrayList<Integer>();
        int index;
        Random rnd = new Random();

        for(int i = 0; i < keyList.size(); i++){
            do{
                index = rnd.nextInt(keyList.size());
            }while(done.contains(index));
            done.add(index);
            String keyToFind = keyList.get(index);
            ThumbData td = Constants.getInstance().getThumbDataForKey(keyToFind);
            if(td != null) thumbData.add(td);
        }
    }

    /********************************************************************************
     * If the back button is pressed here, all the data will be cleaned up and the current
     * subject ID will be logged out
     *******************************************************************************/
    public void onBackPressed() {
        if(!areModulesComplete) {
            new AlertDialog.Builder(this)
                    .setTitle("Logout " + Constants.getInstance().getCurrSubject() + "?")
                    .setMessage("Are you sure you want to logout?")
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface arg0, int arg1) {
                            VideoListActivity.super.onBackPressed();
                            DataLogger.getInstance().log(new BaseLogEvent(DataLogger.UserLogoutEvent,Constants.getInstance().getCurrSubject()));
                        }
                    }).create().show();
        }
        else{
            finish();
        }
    }
}
