package com.whi.emmersiv.myapplication;

import android.graphics.Color;
import android.widget.*;
import android.content.*;
import android.view.*;

import java.util.ArrayList;

/*
    This is the data adapter for the ListView in VideoListActivity. It will display
    all ThumbData types using "thumbdata_view.xml" layout
 */
public class ThumbDataAdapter extends ArrayAdapter<ThumbData> {

    public ThumbDataAdapter(Context cxt, ArrayList<ThumbData> thumbInfo){
        super(cxt,0,thumbInfo);
    }

    public View getView(int position, View convertView, ViewGroup parent){
        ThumbData item = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.thumbdata_view, parent, false);
        }

        //Get the components that make up this view
        ImageView thumb = (ImageView) convertView.findViewById(R.id.imgThumb);
        TextView title = (TextView) convertView.findViewById(R.id.txtTitle);
        ProgressBar pgBar = (ProgressBar) convertView.findViewById(R.id.pgBar);
        ImageView done = (ImageView) convertView.findViewById(R.id.imgDone);

        Context cxt = getContext();

        //populate the values of those components with data from the ThumbData item
        int resID_thumb = cxt.getResources().getIdentifier(item.thumbUri, "drawable", cxt.getPackageName());
        thumb.setImageResource(resID_thumb);
        title.setText(item.name);
        pgBar.setProgress((int)(item.progress * 100));
        done.setColorFilter((item.isDone) ? Color.BLUE:Color.LTGRAY);

        return convertView;
    }
}
