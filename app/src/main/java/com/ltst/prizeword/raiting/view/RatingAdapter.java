package com.ltst.prizeword.raiting.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class RatingAdapter extends ArrayAdapter<String>
{

    public RatingAdapter(Context context, int resource, int textViewResourceId, String[] objects)
    {
        super(context, resource, textViewResourceId, objects);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {


        return super.getView(position, convertView, parent);
    }

}
