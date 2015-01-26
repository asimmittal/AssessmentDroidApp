package com.whi.emmersiv.myapplication;

import android.app.*;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Scene;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import android.content.Intent;
import android.content.*;
import android.content.DialogInterface.*;

/**
    First Activity of the app. Asks User to enter SubjectID. The app data (JSON file)
    is also read and stored in a Singleton called "Constants" for use throughout the app
 */
public class SelectionActivity extends Activity{

    EditText txtSubjectId;
    Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //display the layout on the screen
        setContentView(R.layout.activity_selection);

        //get UI components
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        txtSubjectId = (EditText) findViewById(R.id.txtSubjectId);

        //the submit button is initially disabled
        btnSubmit.setEnabled(false);

        //enable the submit button when the user enters some valid text
        txtSubjectId.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = txtSubjectId.getText().toString().trim();
                if(input.length() > 0) btnSubmit.setEnabled(true); else btnSubmit.setEnabled(false);
            }
        });

        //handle button click - navigate to the next activity viz. "VideoListAcitivity"
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String text = txtSubjectId.getText().toString().trim().toUpperCase();
                if(text.length() > 0){
                    Constants.getInstance().setCurrSubject(text);
                    Intent intent = new Intent(getApplicationContext(), VideoListActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    /**
     * Whenever the activity resumes, make sure we have the latest
     * JSON data from the appdata file
     */
    public void onResume(){
        super.onResume();

        //read the json data and build the data model for the app
        String json = readJSON();
        Constants.getInstance().buildDataModelFromJson(json);
        txtSubjectId.setText("");
    }

    /**
     * Read the JSON file named 'appdata' and return the string
     * @return string containing the json
     */
    private String readJSON(){
        // read the JSON file titled 'appdata'
        InputStream is = getResources().openRawResource(R.raw.appdata);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String json = "", line = "";
        try{
            do{
                line = reader.readLine();
                json += line;
            }while(line != null);
        }catch (IOException e){}

        return json;
    }

}
