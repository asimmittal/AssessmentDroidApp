package com.whi.emmersiv.myapplication;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.content.Intent;
import android.transition.Scene;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import android.widget.LinearLayout;
import android.widget.VideoView;
import android.util.*;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;
import android.os.Handler;

public class VideoPlayerActivity extends Activity {

    static int videoPosition;
    static boolean isInQuestionView;
    static boolean wasKeyInterrupt;

    ArrayList<SceneQuestion> listOfQuestions;
    int curQuestionIndex = 0;
    String curKey;

    MediaPlayer mp;

    VideoView videoPlayer;
    RelativeLayout videoContainer;
    LinearLayout containerQuestions;
    Animation slideInAnim, slideOutAnim;
    ImageButton btnVideo, btnAudio;
    View pauseIcon;
    RadioGroup rdGroupOptions;

    TextView txtQuesTitle, txtQuestion;
    RadioButton opt1, opt2, opt3;
    Button btnSubmit;

    ArrayList<String> audioBugFilter;

    /********************************************************************************************
     * Couple of things going on here. onCreate is called when the activity is loaded. This means
     * that even if the device is rotated (portrait <--> landscape), onCreate will be executed. This
     * will cause a few glitches
     *  - the video player will restart its playback instead of resuming it from where it left off
     *  - the Q&A UI will animate in and out unnecessarily
     *
     * I'm using two static variables to manage these irregularities
     *  - videoPosition : when the device is rotated, onPause is called, and if the video is playing
     *  the current video position is saved and restored later on in onCreate. If the video was
     *  already playing, the videoPosition will be non zero
     *
     *  - isInQuestionView : boolean that is set when the Q&A UI is in view. If the device is
     *  rotated after the Q&A is on the screen, this will help us identify that and disable video
     *  playback.
     * @param savedInstanceState
     *********************************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_and_questions);

        Log.d("---> " + Constants.getCurMethodName(3), "Starting VideoPlayerActivity");
        Log.d("---> " + Constants.getCurMethodName(3), "Is QnA UI in view: "+isInQuestionView);

        wasKeyInterrupt = false;

        /*List to handle the bug where "playAudioQuestion" was being called twice in succession*/
        audioBugFilter = new ArrayList<String>();

        //animation
        slideInAnim = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slidein);

        //grab the parent container
        videoContainer = (RelativeLayout) findViewById(R.id.rootContainer);

        //Grab the layout containing the Q&A interface, hide it if the view isn't supposed to
        //show the Q&A just yet.
        containerQuestions = (LinearLayout) findViewById(R.id.containerQuestions);

        //Grab the button that will allow video to be replayed
        btnVideo = (ImageButton) findViewById(R.id.btnVideo);
        btnAudio = (ImageButton) findViewById(R.id.btnAudio);

        //grab the UI objects
        txtQuestion = (TextView) findViewById(R.id.txtQuestion);
        txtQuesTitle = (TextView) findViewById(R.id.txtTitleQuestion);
        opt1 = (RadioButton) findViewById(R.id.opt1);
        opt2 = (RadioButton) findViewById(R.id.opt2);
        opt3 = (RadioButton) findViewById(R.id.opt3);

        //grab the submit button in the QnA UI
        btnSubmit = (Button) findViewById(R.id.btnSubmit);

        //Radio group containing all the options for the Q&A UI
        rdGroupOptions = (RadioGroup) findViewById(R.id.rdGrpOptions);

        //Grab the pause icon
        pauseIcon = (View) findViewById(R.id.pauseIcon);

        //Grab the video player and configure it to play the video pointed to by "key"
        videoPlayer = (VideoView) findViewById(R.id.videoView);
        videoPlayer.setVisibility(View.VISIBLE);


        //-------------- Event Listeners defined here -----------------

        {
            /**
             * Click listener - when the button to replay the video is pressed
             * hide the Q&A UI, switch to the video player
             */
            btnVideo.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    switchToVideoView();
                }
            });


            /**
             * Click listener for the audio repeat button
             */
            btnAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setQuestionInUI();
                }
            });

            /**
             * listener for when the video has completed playing. Animate the Q&A UI
             * into view
             */
            videoPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    switchToQuestionsView();
                }
            });

            videoPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.d("----> ERROR", "Error in video Player");
                    return false;
                }
            });

            /**
             * When the videoPlayer is asked to seek to a specific time in the clip
             */
            videoPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    mp.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                        public void onSeekComplete(MediaPlayer mp) {
                            videoPlayer.start();
                        }
                    });
                }
            });

            /**
             * click listener for the layout that contains the video player. toggle pause/play
             * for the video playback
             */
            videoContainer.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (videoPlayer.isPlaying()) {
                        videoPlayer.pause();
                        pauseIcon.setVisibility(View.VISIBLE);
                    } else {
                        videoPlayer.start();
                        pauseIcon.setVisibility(View.INVISIBLE);
                    }
                }
            });

            /**
             * click listener for the layout that contains the Q&A interface
             */
            containerQuestions.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    //do nothing - this is in place to beat the videoContainer.onClickListener

                }
            });

            /**
             * Submit button clicked for current question
             */
            btnSubmit.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    handleSubmitForCurQuestion();
                }
            });

            //TODO: Debug only - remove from final build
            videoContainer.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    if (videoPlayer.isPlaying()) videoPlayer.pause();
                    switchToQuestionsView();
                    return true;
                }
            });
        }
        //endregion
    }

    /*******************************************************************************
     * Activity has resumed, see if the video playback was interrupted. If it was
     * seek to the appropriate point and continue playing the video
     *******************************************************************************/
    public void onResume(){
        super.onResume();
        Log.d("----> Resuming VideoPlayerActivity" , "isQuesInView - " + isInQuestionView);

        //manage visibility
        containerQuestions.setVisibility((isInQuestionView) ? View.VISIBLE : View.INVISIBLE);
        pauseIcon.setVisibility(View.INVISIBLE);

        attachClickListenerToRadioGroup();

        //The previous activity has sent the 'key' that uniquely identifies the scene
        //using this key, we can get the name of the video resource.
        Intent intent = getIntent();
        curKey = intent.getStringExtra("module_key");

        //load the questions into local data structure
        loadQuestions(curKey);

        //Decode this name to get its resourceID
        String videoName = "raw/vid" + curKey;
        int resID = getResources().getIdentifier(videoName,null,getPackageName());

        //Using the resID decoded from the key, construct the URI of the video
        String uriPath = "android.resource://" + getPackageName() + "/" + resID;
        videoPlayer.setVideoURI(Uri.parse(uriPath));

        if(!isInQuestionView) {
            if (videoPosition == 0) videoPlayer.start();
            else videoPlayer.seekTo(videoPosition);
        }
    }

    /********************************************************************************
     * When the activity is being left (back pressed / device rotated / home etc.)
     * If the videoPlayer was in the middle of playback, save its current position
     * pause the playback
     *******************************************************************************/
    public void onStop(){
        super.onStop();
        Log.d("---->ON STOP INVOKED","");
        if(!wasKeyInterrupt && videoPlayer.isPlaying()){
            videoPosition = videoPlayer.getCurrentPosition();
            videoPlayer.pause();
        }

        if(mp != null) mp.stop();

        //if we're stopping the activity, we should hide the Q&A ui
        isInQuestionView = false;
        containerQuestions.clearAnimation();
        containerQuestions.setVisibility(View.INVISIBLE);
    }

    public void onDestroy(){
        super.onDestroy();
        Log.d("---->ON DESTROY INVOKED","");
        if(mp != null) mp.release();
    }

    /********************************************************************************
     * Handle submission of answer for the current question
     *******************************************************************************/
    private void handleSubmitForCurQuestion(){
        SceneQuestion currQuestion = listOfQuestions.get(curQuestionIndex);
        currQuestion.isDone = true;
        ArrayList<SceneQuestion> quesForThisScene = Constants.getInstance().getQuestionsForScene(currQuestion.scene);
        for(SceneQuestion q : quesForThisScene){
            if(q.qID == currQuestion.qID){
                q.isDone = true;
                curQuestionIndex++;
                break;
            }
        }

        if(curQuestionIndex < listOfQuestions.size())
            setQuestionInUI();
        else{
            Constants.getInstance();
            finish();
        }
    }

    /********************************************************************************
     * Normalize the typefaces for the options back to default settings
     *******************************************************************************/
    private void normalizeTypefaces(){
        txtQuestion.setTypeface(null, Typeface.NORMAL); txtQuestion.setTextColor(Color.BLACK);
        opt1.setTypeface(null, Typeface.NORMAL); opt1.setTextColor(Color.BLACK);
        opt2.setTypeface(null, Typeface.NORMAL); opt2.setTextColor(Color.BLACK);
        opt3.setTypeface(null, Typeface.NORMAL); opt3.setTextColor(Color.BLACK);
    }

    /********************************************************************************
     * set the enabled status of all radio buttons
     *******************************************************************************/
    private void setOptionsEnabled(boolean stat){
        opt1.setEnabled(stat);
        opt2.setEnabled(stat);
        opt3.setEnabled(stat);
    }

    /********************************************************************************
     * set the visibility status of all radio buttons
     *******************************************************************************/
    private void setOptionsVisible(int stat){
        opt1.setVisibility(stat);
        opt2.setVisibility(stat);
        opt3.setVisibility(stat);
    }

    /********************************************************************************
     * Attach event listener to the radio group in the  UI
     *******************************************************************************/
    private void attachClickListenerToRadioGroup(){

        opt1.setEnabled(true);
        opt2.setEnabled(true);
        opt3.setEnabled(true);

        rdGroupOptions.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(opt1.isChecked() || opt2.isChecked() || opt3.isChecked()) {
                    btnSubmit.setEnabled(true);
                }
            }
        });

    }

    /********************************************************************************
     * Detach the event listener from the radio group in the UI
     *******************************************************************************/
    private void detachClickListenersFromRadioGroup(){
        rdGroupOptions.setOnCheckedChangeListener(null);
        opt1.setEnabled(false);
        opt2.setEnabled(false);
        opt3.setEnabled(false);
    }

    /********************************************************************************
     * state of the various UI components before vocal readout of the question
    *******************************************************************************/
    private void setupUIStateBeforeAudio(){
        txtQuestion.setVisibility(View.INVISIBLE);
        opt1.setVisibility(View.INVISIBLE);
        opt2.setVisibility(View.INVISIBLE);
        opt3.setVisibility(View.INVISIBLE);
        btnSubmit.setEnabled(false);
        btnAudio.setEnabled(false); btnAudio.setVisibility(View.INVISIBLE);
        btnVideo.setEnabled(false); btnVideo.setVisibility(View.INVISIBLE);
        detachClickListenersFromRadioGroup();
    }

    /********************************************************************************
     * state of various UI components after vocal readout is complete
     *******************************************************************************/
    private void setupUIStateAfterAudio(){
        btnAudio.setEnabled(true); btnAudio.setVisibility(View.VISIBLE);
        btnVideo.setEnabled(true); btnVideo.setVisibility(View.VISIBLE);
        attachClickListenerToRadioGroup();
    }

    /********************************************************************************
     * This method causes the UI for the question to be displayed and the questions and options
     * to be vocally read out one after the other
     *******************************************************************************/

    enum QnAState {SHOW_Q, SHOW_1, SHOW_2, SHOW_3, END};
    QnAState state;

    private void setQuestionInUI(){

        Log.d("---> " + Constants.getCurMethodName(3), "Building QnA UI...");
        final SceneQuestion currQuestion = listOfQuestions.get(curQuestionIndex);

        txtQuesTitle.setText("Question " + (curQuestionIndex+1) + "/" + listOfQuestions.size());
        txtQuestion.setText(currQuestion.question);
        rdGroupOptions.clearCheck();

        opt1.setText(currQuestion.opt1);
        opt2.setText(currQuestion.opt2);
        opt3.setText(currQuestion.opt3);


        state = QnAState.SHOW_Q;

        mp = new MediaPlayer();
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.d("--------------------x Completed Playback x----------------","curState: " + state);
                mp.stop();
                mp.reset();
                normalizeTypefaces();

                switch (state){
                    case SHOW_1:
                        if(currQuestion.opt1.length()>0) {
                            opt1.setTextColor(Color.BLUE);
                            opt1.setTypeface(null, Typeface.BOLD);
                            opt1.setVisibility(View.VISIBLE);
                            playAudio(currQuestion.audioOpt1, mp);
                            state = QnAState.SHOW_2;
                        }
                        else setupUIStateAfterAudio();
                        break;

                    case SHOW_2:
                        if(currQuestion.opt2.length() > 0) {
                            opt2.setTextColor(Color.BLUE);
                            opt2.setTypeface(null, Typeface.BOLD);
                            opt2.setVisibility(View.VISIBLE);
                            playAudio(currQuestion.audioOpt2, mp);
                            state = QnAState.SHOW_3;
                        }else setupUIStateAfterAudio();
                        break;

                    case SHOW_3:
                        if(currQuestion.opt3.length() > 0){
                            opt3.setTextColor(Color.BLUE);
                            opt3.setTypeface(null, Typeface.BOLD);
                            opt3.setVisibility(View.VISIBLE);
                            playAudio(currQuestion.audioOpt3, mp);
                            state = QnAState.END;
                        }else setupUIStateAfterAudio();
                        break;

                    case END:
                        setupUIStateAfterAudio();
                        break;
                }
            }
        });

        setupUIStateBeforeAudio();
        txtQuestion.setVisibility(View.VISIBLE);
        txtQuestion.setTextColor(Color.BLUE); txtQuestion.setTypeface(null, Typeface.BOLD);
        playAudio(currQuestion.audioQues, mp);
        state = QnAState.SHOW_1;
    }



    /********************************************************************************
     * Play the audio pointed to by 'path'. Invoke the 'callback' after the audio
     * is done playing completely
     * @param path - string name of the audio resource
     *******************************************************************************/
    private void playAudio(String path, MediaPlayer mPlay){

        try {
                String audQues = "raw/" + path;
                int resID = getResources().getIdentifier(audQues, null, getPackageName());
                String uriPath = "android.resource://" + getPackageName() + "/" + resID;
                mPlay.setDataSource(getApplicationContext(), Uri.parse(uriPath));
                mPlay.prepare();
                mPlay.start();
        }
        catch (Exception e) {
            Log.d("OHHHHHHH NO0","exception");
        }
    }

    /********************************************************************************
     * load the questions for this scene into the local data structure. Randomize the order
     *******************************************************************************/
    private void loadQuestions(String key){
        ArrayList<SceneQuestion> list = Constants.getInstance().getQuestionsForScene(key);
        listOfQuestions = new ArrayList<SceneQuestion>();
        ArrayList<Integer> doneIndices = new ArrayList<Integer>();
        int index;
        Random rnd = new Random();
        for(int i = 0; i < list.size(); i++){
            do {
                index = rnd.nextInt(list.size());
            }while(doneIndices.contains(index));
            doneIndices.add(index);
            SceneQuestion sq = list.get(index);
            if(!sq.isDone) listOfQuestions.add(sq);
        }
    }

    /********************************************************************************
     * Transition to show the video
     *******************************************************************************/
    private void switchToVideoView(){
        containerQuestions.clearAnimation();
        containerQuestions.setVisibility(View.INVISIBLE);
        pauseIcon.setVisibility(View.INVISIBLE);
        videoPlayer.start();
        isInQuestionView = false;
    }

    /********************************************************************************
     * Transition to show the Q&A interface
     *******************************************************************************/
    private void switchToQuestionsView(){
        setQuestionInUI();
        containerQuestions.startAnimation(slideInAnim);
        containerQuestions.setVisibility(View.VISIBLE);
        rdGroupOptions.clearCheck();
        isInQuestionView = true;
    }



    /********************************************************************************
     * Handle key presses (Back / Home) buttons
     *******************************************************************************/
    public boolean onKeyDown(int keyCode, KeyEvent event){

        Log.d("-----> Key PRESSED","" + keyCode);

        switch(keyCode){
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_HOME:
                videoPlayer.stopPlayback();
                if(mp != null) mp.stop();
                isInQuestionView = false;
                videoPosition = 0;
                wasKeyInterrupt = true;
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

}
