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

    /********************************************************************************
     * Class constructor
     * @param cxt
     * @param thumbInfo
     *******************************************************************************/
    public ThumbDataAdapter(Context cxt, ArrayList<ThumbData> thumbInfo){
        super(cxt,0,thumbInfo);
    }

    /**
     * The getView method binds the data to the view and returns it
     * @param position
     * @param convertView
     * @param parent
     * @return sub view
     */
    public View getView(int position, View convertView, ViewGroup parent){
        ThumbData item = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.thumbdata_view, parent, false);
        }

        //Get the components that make up this view
        ImageView thumb = (ImageView) convertView.findViewById(R.id.imgThumb);
        TextView title = (TextView) convertView.findViewById(R.id.txtTitle);
        ImageView done = (ImageView) convertView.findViewById(R.id.imgDone);
        LinearLayout bar0 = (LinearLayout) convertView.findViewById(R.id.bar0);
        LinearLayout bar1 = (LinearLayout) convertView.findViewById(R.id.bar1);
        LinearLayout bar2 = (LinearLayout) convertView.findViewById(R.id.bar2);
        LinearLayout bar3 = (LinearLayout) convertView.findViewById(R.id.bar3);
        LinearLayout bar4 = (LinearLayout) convertView.findViewById(R.id.bar4);
        LinearLayout bar5 = (LinearLayout) convertView.findViewById(R.id.bar5);

        LinearLayout[] bars = {bar0, bar1, bar2, bar3, bar4, bar5};
        Context cxt = getContext();

        int colorHighlight = Color.parseColor("#27AE60");
        int colorDim = Color.argb(55,105,105,105);

        //populate the values of those components with data from the ThumbData item
        int resID_thumb = cxt.getResources().getIdentifier(item.thumbUri, "drawable", cxt.getPackageName());
        thumb.setImageResource(resID_thumb);
        title.setText(item.name);
        done.setColorFilter((item.isDone) ? colorHighlight:Color.LTGRAY);

        int numBarsToHighlight = (int)((item.progress) * bars.length);
        for(int i = 0; i < bars.length; i++){
            bars[i].setBackgroundColor((i < numBarsToHighlight) ? colorHighlight : colorDim);
        }

        return convertView;
    }
}
